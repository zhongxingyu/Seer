 package com.redshape.persistence.jms;
 
import com.redshape.persistence.DaoContextHolder;
 import com.redshape.persistence.dao.DAOException;
 import com.redshape.persistence.dao.query.IQuery;
 import com.redshape.persistence.dao.query.IQueryBuilder;
 import com.redshape.persistence.dao.query.executors.result.IExecutorResult;
 import com.redshape.persistence.dao.query.executors.result.IExecutorResultFactory;
 import com.redshape.persistence.dao.query.executors.services.IQueryExecutorService;
 import com.redshape.persistence.entities.DtoUtils;
 import com.redshape.persistence.entities.IEntity;
 import com.redshape.persistence.jms.protocol.IQueryMarshaller;
 import com.redshape.persistence.jms.protocol.ProtocolException;
 import com.redshape.utils.Commons;
 import com.redshape.utils.Constants;
 import org.apache.log4j.Logger;
 
 import javax.jms.*;
 import java.lang.IllegalStateException;
 import java.util.Date;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 /**
  * Default DAO requests handling services
  *
  * @author Cyril A. Karpenko <self@nikelin.ru>
  * @package com.redshape.persistence.jms
  * @date 1/25/12 {3:37 PM}
  */
 public class RequestsHandlingService implements IRequestHandlingService {
 
     private static final Logger log = Logger.getLogger(RequestsHandlingService.class);
     public static int MSG_MAX_PROCESSING_TIME = Constants.TIME_SECOND * 5;
 
     private Integer receiveTimeout;
     private String queueName;
     private QueueSession session;
     private QueueConnection connection;
     private IQueryExecutorService executionService;
     private MessageConsumer consumer;
     private IQueryBuilder builder;
     private IExecutorResultFactory resultsFactory;
     private ExecutorService service;
 
     private boolean state = true;
 
     private IQueryMarshaller protocol;
 
     public RequestsHandlingService( QueueConnection connection,
                                     IQueryExecutorService service,
                                     IQueryBuilder builder,
                                     IQueryMarshaller marshaller,
                                     String queueId )
         throws DAOException {
         this(null, connection, service, builder, marshaller, queueId );
     }
 
     public RequestsHandlingService( IExecutorResultFactory resultsFactory,
                                     QueueConnection connection,
                                     IQueryExecutorService service,
                                     IQueryBuilder builder,
                                     IQueryMarshaller marshaller,
                                     String queueId )
         throws DAOException {
         this.resultsFactory = resultsFactory;
         this.builder = builder;
         this.connection = connection;
         this.protocol = marshaller;
         this.executionService = service;
         this.queueName = queueId;
         this.service = Executors.newFixedThreadPool(200);
         this.checkFields();
         this.init();
     }
 
     public Integer getReceiveTimeout() {
         return Commons.select(receiveTimeout, 100);
     }
 
     public void setReceiveTimeout(Integer receiveTimeout) {
         this.receiveTimeout = receiveTimeout;
     }
 
     protected IExecutorResultFactory getResultsFactory() {
         return this.resultsFactory;
     }
 
     protected QueueSession getSession() {
         return this.session;
     }
 
     @Override
     public boolean isRunning() {
         return this.state;
     }
 
     protected IQueryBuilder getBuilder() {
         return this.builder;
     }
 
     protected IQueryExecutorService getExecutionService() {
         return this.executionService;
     }
 
     protected String getQueueName() {
         return this.queueName;
     }
 
     protected QueueConnection getConnection() {
         return this.connection;
     }
 
     protected IQueryMarshaller getProtocol() {
         return this.protocol;
     }
 
     protected void createSession() throws JMSException {
         this.session = this.getConnection().createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
         if ( this.session == null ) {
             throw new JMSException("Session initialization failed");
         }
 
         this.session.run();
     }
 
 
     protected void checkSession() throws JMSException {
         try {
             this.session.run();
         } catch ( Throwable e ) {
             if ( !( e instanceof javax.jms.IllegalStateException ) ) {
                 throw new IllegalStateException(e);
             }
 
             this.createSession();
         }
     }
 
     protected void init() throws DAOException {
         try {
             this.connection.start();
 
             this.createSession();
 
             this.consumer = this.getSession().createConsumer(this.getSession().createQueue( this.getQueueName() ));
         } catch ( JMSException e ) {
             throw new DAOException("Queue consumer creating failed", e );
         }
     }
 
     protected void checkFields() {
         if ( this.getConnection() == null ) {
             throw new IllegalStateException("<null>: connection object not provided");
         }
 
         if ( this.getProtocol() == null ) {
             throw new IllegalStateException("<null>: queries marshaller not provided");
         }
 
         if ( this.getExecutionService() == null ) {
             throw new IllegalStateException("<null>: queries execution services not provided");
         }
     }
 
     @Override
     public void stop() {
         this.state = false;
     }
 
     protected boolean isExpired( Message message ) throws JMSException {
         return new Date().getTime() >  message.getJMSTimestamp() + MSG_MAX_PROCESSING_TIME;
     }
 
     protected <T extends IEntity> Message processRequest( Message message ) throws DAOException {
         try {
             this.checkSession();
             Message result = this.getSession().createObjectMessage();
             result.setJMSCorrelationID(message.getJMSCorrelationID());
 
             log.info("Income message correlation ID: " + message.getJMSCorrelationID() );
 
             log.info("Setting message expiration time to " + MSG_MAX_PROCESSING_TIME + "ms..." );
             result.setJMSExpiration( MSG_MAX_PROCESSING_TIME );
             log.info("Setting message target destination...");
             result.setJMSDestination( message.getJMSReplyTo() );
 
             IQuery<T> query = this.getProtocol().unmarshalQuery(this.getBuilder(), message);
             if ( query.entity() != null ) {
                 log.info("Hydrating DTO object from JMS query request...");
                 query.entity( DtoUtils.<T>fromDTO(query.entity()) );
             }
 
            if ( query.getEntityClass().isInterface() ) {
                query.setEntityClass(
                        (Class<T>) DaoContextHolder.instance().getContext().getBean(query.getEntityClass()).getClass() );
            }

             log.info("Executing income query...");
             IExecutorResult execResult = this.getExecutionService().execute(query);
             log.info("Query processed successfully...");
             this.getProtocol().marshal(
                 result,
                 this.getResultsFactory().createResult( execResult.getResultsList() )
             );
 
             log.info("Result message correlation ID: " + result.getJMSCorrelationID() );
 
             return result;
         } catch ( ProtocolException e ) {
             throw new DAOException("Failed to marshal/unmarshal message!", e );
         } catch ( JMSException e ) {
             throw new DAOException("JMS interaction failed", e );
         } finally {
             try {
                 message.setJMSRedelivered(false);
             } catch ( JMSException e ) {
                 log.error( e.getMessage(), e );
             }
         }
     }
 
     protected void sendRespond( Queue destination, Message message ) throws JMSException{
         MessageProducer sender = this.getSession().createProducer(destination);
         sender.send(message);
         sender.close();
     }
 
     protected void execute( Message message ) throws JMSException, DAOException {
         log.info("Sending acknowledge on received message...");
         message.acknowledge();
 
         log.info( "Received new JMS processing request...");
         if ( !this.isExpired(message) ) {
             Destination replyDestination = message.getJMSReplyTo();
             if ( replyDestination == null ) {
                 log.info("Reply destination not specified...");
                 return;
             }
 
             this.sendRespond( (Queue) replyDestination, this.processRequest( message) );
         } else {
             log.info("JMS DAO request has been expired...");
         }
     }
 
     @Override
     public void run() {
         try {
             while ( this.isRunning() ) {
                 final Message message = this.consumer.receive(this.getReceiveTimeout());
                 if ( message != null ) {
                     this.service.execute(
                         new Runnable() {
                             @Override
                             public void run() {
                                 try {
                                     long start = System.currentTimeMillis();
                                     RequestsHandlingService.this.execute(message);
                                     log.info("Processed in: " + ( System.currentTimeMillis() - start ) + "ms" );
                                 } catch ( Throwable e ){
                                     log.error( e.getMessage(), e );
                                 }
                             }
                         }
                     );
                 }
             }
         } catch ( JMSException e ) {
             log.error( e.getMessage(), e );
         }
     }
 }
