 package pegasus.eventbus.rabbitmq;
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.commons.lang.time.StopWatch;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.rabbitmq.client.Channel;
 import com.rabbitmq.client.Connection;
 import com.rabbitmq.client.ConnectionFactory;
 import com.rabbitmq.client.ShutdownListener;
 import com.rabbitmq.client.ShutdownSignalException;
 
 import pegasus.eventbus.amqp.AmqpConnectionParameters;
 
 public class RabbitConnection implements ShutdownListener {
 
     private static final Logger          LOG                      = LoggerFactory.getLogger(RabbitConnection.class);
     private static final long            DEFAULT_RETRY_TIMEOUT    = 30000;
 
     private ConnectionFactory            connectionFactory        = new ConnectionFactory();
     protected Connection                   connection;
     private long                         retryTimeout             = DEFAULT_RETRY_TIMEOUT;
     private Set<UnexpectedCloseListener> unexpectedCloseListeners = new HashSet<UnexpectedCloseListener>();
 
     private volatile boolean             isClosing;
     private volatile boolean             isInConnectionErrorState;
 
     public RabbitConnection(AmqpConnectionParameters connectionParameters) {
         connectionFactory.setUsername(connectionParameters.getUsername());
         connectionFactory.setPassword(connectionParameters.getPassword());
         connectionFactory.setVirtualHost(connectionParameters.getVHost());
         connectionFactory.setHost(connectionParameters.getHost());
         connectionFactory.setPort(connectionParameters.getPort());
     }
 
     public void open() throws IOException {
         if (connection == null || !connection.isOpen()) {
             isClosing = false;
 
             LOG.trace("Grabbing the connection instance from the factory.");
 
             connection = connectionFactory.newConnection();
 
             LOG.trace("Adding ShutdownListener to connection.");
 
             connection.addShutdownListener(this);
         }
     }
 
     public void setConnection(Connection connection) {
         this.connection = connection;
     }
 
     public boolean isOpen() {
         return connection != null ? connection.isOpen() : false;
     }
 
     public void close() throws IOException {
         if (connection.isOpen()) {
             isClosing = true;
             connection.close();
         }
     }
 
     public Channel createChannel() throws IOException {
         open();
         Channel channel = connection.createChannel();
 
         return channel;
     }
 
     public void shutdownCompleted(ShutdownSignalException signal) {
         if(isClosing){
             
         	LOG.info("Connection shutdown notice received as part of routine bus shutdown.  Taking no action.");
         	
         	return;
         } else {
 	    	if (signal == null) {
 	
 	            LOG.info("Connection shutdown notice received unexpectedly.");
 	
 	        } else {
 	
 	            LOG.error("Connection shutdown exception received unexpectedly.", signal);
 	        }
         }
 
         if (isInConnectionErrorState || isClosing) {
             return;
         }
 
         isInConnectionErrorState = true;
 
         StopWatch watch = new StopWatch();
         watch.start();
         try {
 
             while (watch.getTime() < retryTimeout) {
                 
                 LOG.info("Attempting to reopen connection.");
                 
                 try {
                     open();
 
                     LOG.info("Connection successfully reopened.");
 
                     isInConnectionErrorState = false;
                     break;
                } catch (Exception e) {
 
                     LOG.error("Attempt to reopen connection failed with error: " + e.getMessage(), e);
 
                     try {
                         Thread.sleep(100);
                     } catch (InterruptedException e1) {
 
                         LOG.warn("Attempt to reopen connection canceled because thread has been interrupted.");
 
                         // should we notify listeners in this case?
                         break;
                     }
                 }
             }
             if (isInConnectionErrorState) {
 
                 LOG.warn("Attempt to reopen connection permanently failed.");
 
             }
 
             notifyUnexpectedCloseListeners(!isInConnectionErrorState);
 
         } finally {
             watch.stop();
         }
     }
 
     public long getRetryTimeout() {
         return retryTimeout;
     }
 
     public void setRetryTimeout(long retryTimeout) {
         this.retryTimeout = retryTimeout;
     }
 
     public void attachUnexpectedCloseListener(UnexpectedCloseListener listener) {
         unexpectedCloseListeners.add(listener);
     }
 
     public void detachUnexpectedCloseListener(UnexpectedCloseListener listener) {
         unexpectedCloseListeners.remove(listener);
     }
 
     private void notifyUnexpectedCloseListeners(boolean successfullyReopened) {
         for (UnexpectedCloseListener listener : unexpectedCloseListeners) {
             listener.onUnexpectedClose(successfullyReopened);
         }
     }
 
     public interface UnexpectedCloseListener {
 
         void onUnexpectedClose(boolean successfullyReopened);
 
     }
 
 }
