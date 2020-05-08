 /*
  * Copyright (c) 2011. KloudTek Ltd
  */
 
 package com.kloudtek.util;
 
 import javax.jms.*;
 import java.io.EOFException;
 import java.net.ConnectException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 
 /**
  * This is used to create a java applications that connect to a JMS Broker and handle incoming messages.
  */
 public abstract class JMSApplication implements Runnable {
     protected final Logger logger;
     private State state = State.UNCONNECTED;
     protected Connection connection;
     protected ExecutorService executorService;
     protected final SessionType sessionType;
     protected final long connectRetry;
     protected String clientId;
 
     /**
      * Constructor
      *
      * @param sessionType       Type of JMS Session
      * @param noCleanLogManager If this is true, we will attempt to set the Log Manager to use {@link NoCleanLogManager}, so
      *                          that the cleanup thread may use logging. This will only work if LogManager hasn't been used up till now
      * @param loggerName        Logger name that will be used for all logging
      */
     public JMSApplication(SessionType sessionType, boolean noCleanLogManager, String loggerName) {
         this(sessionType, noCleanLogManager, loggerName, 20000);
     }
 
     public JMSApplication(SessionType sessionType, boolean noCleanLogManager, String loggerName, long connectRetry) {
         this.sessionType = sessionType;
         this.connectRetry = connectRetry;
         if (noCleanLogManager) {
             System.setProperty("java.util.logging.manager", "com.kloudtek.util.NoCleanLogManager");
         }
         if (loggerName != null) {
             logger = Logger.getLogger(loggerName);
         } else {
             logger = Logger.getAnonymousLogger();
         }
         Runtime.getRuntime().addShutdownHook(new Thread() {
             @Override
             public void run() {
                 close();
             }
         });
     }
 
     public synchronized boolean isClosed() {
         return state == State.CLOSED;
     }
 
     public void closeConnection(boolean quiet) {
         try {
             if (connection != null) {
                 connection.close();
             }
             state = State.UNCONNECTED;
         } catch (JMSException e) {
             if (!quiet) {
                 logger.log(Level.SEVERE, "Error while closing JMS connection", e);
             }
         }
     }
 
     public void close() {
         logger.info("Shutdown initiated, closing connections and awaiting for all current tasks to complete");
         synchronized (this) {
             state = State.CLOSED;
         }
         closeConnection(false);
         executorService.shutdown();
         try {
             executorService.awaitTermination(1, TimeUnit.MINUTES);
         } catch (InterruptedException e) {
             logger.log(Level.SEVERE, e.getMessage(), e);
         }
         logger.info("Shutdown completed");
         final LogManager logManager = LogManager.getLogManager();
         if (logManager instanceof NoCleanLogManager) {
             ((NoCleanLogManager) logManager).clean();
         }
     }
 
     private synchronized boolean connect() throws JMSException {
         if (state == State.CONNECTED) {
             return false;
         }
         state = State.CONNECTING;
         try {
             connection = createConnectionFactory().createConnection();
             if (clientId != null) {
                 connection.setClientID(clientId);
             }
             connection.start();
             logger.info("Connected to message broker");
             state = State.CONNECTED;
             return true;
         } catch (JMSException e) {
             if (isClosed()) {
                 return false;
             }
             state = State.UNCONNECTED;
             throw e;
         }
     }
 
     public void run() {
         executorService = Executors.newSingleThreadExecutor();
         logger.info("Connecting to message broker");
         if (isClosed()) {
             return;
         }
        try {
            new MessageHandler().run();
        } catch (Throwable e) {
            logger.log(Level.SEVERE, e.getMessage(), e );
        }
     }
 
     class MessageHandler implements Runnable {
         private Message message;
         private Session session;
         private Destination destination;
         private MessageConsumer consumer;
 
         MessageHandler() {
         }
 
         private void init() throws JMSException {
             if( connect() ) {
                 session = connection.createSession(sessionType == SessionType.TRANSACTED, sessionType.acknowledgeMode);
                 destination = createDestination(session);
                 consumer = createConsumer(session, destination);
             }
         }
 
         @SuppressWarnings({"ConstantConditions"})
         public void run() {
             for (; ; ) {
                 if (isClosed()) {
                     return;
                 }
                 try {
                     init();
                     for (;;) {
                         if (isClosed()) {
                             return;
                         }
                         message = consumer.receive();
                         if (message != null) {
                             if (isClosed()) {
                                 return;
                             }
                             logger.log(Level.FINER, "Handling JMS message {0}",message.getJMSMessageID());
                             handleMessage(session, message);
                             logger.log(Level.FINER, "Finished handling JMS message {0}",message.getJMSMessageID());
                             if (sessionType == SessionType.TRANSACTED) {
                                 logger.finer("Committing JMS session");
                                 session.commit();
                             }
                         }
                     }
                 } catch (JMSException e) {
                     if (isClosed()) {
                         return;
                     }
                     if (e.getCause() instanceof EOFException) {
                         logger.log(Level.SEVERE, "Lost connection to JMS broker, retrying to connect in " + connectRetry + " ms");
                     } else if (e.getCause() instanceof ConnectException) {
                         logger.log(Level.SEVERE, "Unable to connect to JMS broker (" + e.getCause().getMessage() + "), retrying to connect in " + connectRetry + " ms");
                     } else {
                         logger.log(Level.SEVERE, "JMS Error occured, retrying to connect to broker in " + connectRetry + " ms", e);
                     }
                     closeConnection(true);
                     ThreadUtils.sleep(connectRetry);
                 } catch (Exception e) {
                     logger.log(Level.SEVERE, e.getMessage(), e);
                     rollbackIfTransacted();
                 }
             }
         }
 
         private void rollbackIfTransacted() {
             if (sessionType == SessionType.TRANSACTED) {
                 try {
                     session.rollback();
                 } catch (JMSException ex) {
                     logger.log(Level.WARNING, "Session rollback failed", ex);
                 }
             }
         }
     }
 
     public enum SessionType {
         TRANSACTED(Session.SESSION_TRANSACTED), AUTO_ACKNOWLEDGE(Session.AUTO_ACKNOWLEDGE),
         CLIENT_ACKNOWLEDGE(Session.CLIENT_ACKNOWLEDGE), DUPS_OK_ACKNOWLEDGE(Session.DUPS_OK_ACKNOWLEDGE);
         private int acknowledgeMode;
 
         SessionType(int acknowledgeMode) {
             this.acknowledgeMode = acknowledgeMode;
         }
     }
 
     public enum State {
         UNCONNECTED, CONNECTED, CONNECTING, CLOSED
     }
 
     protected MessageConsumer createConsumer(Session session, Destination destination) throws JMSException {
         return session.createConsumer(destination);
     }
 
     protected abstract Destination createDestination(Session session) throws JMSException;
 
     protected abstract void handleMessage(Session session, Message message) throws Exception;
 
     protected abstract ConnectionFactory createConnectionFactory();
 }
