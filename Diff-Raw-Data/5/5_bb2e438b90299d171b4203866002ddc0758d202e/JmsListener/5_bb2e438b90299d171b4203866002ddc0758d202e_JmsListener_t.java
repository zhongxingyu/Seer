 package org.kisst.gft.mq.jms;
 
 import java.util.Enumeration;
 
 import javax.jms.BytesMessage;
 import javax.jms.Destination;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageConsumer;
 import javax.jms.MessageProducer;
 import javax.jms.Session;
 import javax.jms.TextMessage;
 
 import org.kisst.cfg4j.Props;
 import org.kisst.gft.FunctionalException;
 import org.kisst.gft.RetryableException;
 import org.kisst.gft.admin.rest.Representable;
 import org.kisst.gft.mq.MessageHandler;
 import org.kisst.gft.mq.QueueListener;
 import org.kisst.util.TemplateUtil;
 import org.kisst.util.TimeWindowList;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class JmsListener implements QueueListener, Representable {
 
 	private final static Logger logger=LoggerFactory.getLogger(JmsListener.class); 
 
 	private final JmsSystem system;
 	private final Props props;
 	private final String queue;
 	private final String errorqueue;
 	private final String retryqueue;
 	private final int receiveErrorRetries;
 	private final int receiveErrorRetryDelay;
 	private final int nrofThreads;
 	private final TimeWindowList forbiddenTimes;
 
 
 	private boolean running=false;
 	private MessageHandler handler=null;
 	private Thread[] threads=null;
 	//private final ExecutorService pool;
 
 	public JmsListener(JmsSystem system, Props props, Object context) {
 		this.system=system;
 		this.props=props;
 		this.queue=TemplateUtil.processTemplate(props.getString("queue"), context);
 		this.errorqueue=TemplateUtil.processTemplate(props.getString("errorqueue"), context);
 		this.retryqueue=TemplateUtil.processTemplate(props.getString("retryqueue"), context);
 		this.receiveErrorRetries = props.getInt("receiveErrorRetries", 1000);
 		this.receiveErrorRetryDelay = props.getInt("receiveErrorRetryDelay", 60000);
 		nrofThreads = props.getInt("nrofThreads",2);
 		String timewindow=props.getString("forbiddenTimes", null);
 		if (timewindow==null)
 			this.forbiddenTimes=null;
 		else
 			this.forbiddenTimes=new TimeWindowList(timewindow);
 	}
 
 	public String toString() { return "JmsListener("+queue+")"; }
 	public String getRepresentation() { return props.toString(); }
 	public void stop() {
 		logger.info("Stopping listening to queue {}", queue);
 		running=false;
 	}
 	public void listen(MessageHandler handler)  {
 		if (threads!=null)
 			throw new RuntimeException("Listener already running");
 		threads =new Thread[nrofThreads];
 		running=true;
 		this.handler=handler;
 		for (int i=0; i<nrofThreads; i++) {
 			threads[i]=new Thread(new MyMessageHandler());
 			threads[i].setName("JmsListener-"+i);
 			threads[i].start();
 		}
 	}
 
 	private final class MyMessageHandler implements Runnable {
 		private Session session = null;
 		private Destination destination = null;
 		private MessageConsumer consumer = null;
 
 		public void run() {
 			try {
 				logger.info("Opening queue {}",queue);
 				while (running) {
 					Message message=null;
 					message = getMessage();
 					if (message!=null) {
 						handleMessage(message);
 					}
 				}
 				logger.info("Stopped listening to queue {}", queue);
 				closeSession();
 			}
 			catch (JMSException e) {
 				logger.error("Unrecoverable error during listening, stopped listening", e);
 				if (props.getBoolean("exitOnUnrecoverableListenerError", false))
 					System.exit(1);
 			}
 		}
 
 		private Message getMessage() throws JMSException {
 			long interval=props.getLong("interval",5000);
 			if (forbiddenTimes!=null && forbiddenTimes.isTimeInWindow()) {
 				try {
 					Thread.sleep(interval);
 				} catch (InterruptedException e) {/* ignore */}
 				return null;
 			}
 			int retryCount=0;
 			try {
				if (session==null)
					openSession();
 				Message message = consumer.receive(interval);
 				if (message!=null)
 					return message;
 				retryCount=0;
 			}
 			catch (Exception e) {
 				logger.error("Error when receiving JMS message on queue "+queue, e);
 				if (retryCount++ > receiveErrorRetries)
 					throw new RuntimeException("too many receive retries for queue "+queue);
 				closeSession();
 				sleepSomeTime();
 			}
 			return null;
 		}
 
 		private void sleepSomeTime() {
 			logger.info("sleeping for "+receiveErrorRetryDelay/1000+" secs for retrying listening to "+queue);
 			try {
 				Thread.sleep(receiveErrorRetryDelay);
 			}
 			catch (InterruptedException e1) { throw new RuntimeException(e1); }
 		}
 
 		private void closeSession() {
 			if (session==null)
 				return;
 			try {
 				consumer.close();
 			}
 			catch (Exception e) {
 				logger.warn("Ignoring error when trying to close already suspicious consumer",e);
 			}
 			try {
 				session.close();
 			}
 			catch (Exception e) {
 				logger.warn("Ignoring error when trying to close already suspicious session",e);
 			}
 			session=null;
 		}
 
 		private void openSession() throws JMSException {
 			session = system.getConnection().createSession(true, Session.SESSION_TRANSACTED);
 			destination = session.createQueue(queue);
 			consumer = session.createConsumer(destination);
 		}
 
 
 		public void handleMessage(Message message) {
 			try {
 				logger.debug("Handling {}",message.getJMSMessageID());
 				handler.handle(new JmsQueue.JmsMessage(message)); 
 			}
 			catch (Exception e) {
 				try {
 					String code="TECHERR";
 					if (e instanceof FunctionalException)
 						code="FUNCERR";
 					logger.error(code+": "+e.getMessage()+". When handling JMS message "+((TextMessage) message).getText(),e);
 					String queue=errorqueue;
 					if (e instanceof RetryableException)
 						queue=retryqueue;
 					Destination errordestination = session.createQueue(queue);
 					MessageProducer producer = session.createProducer(errordestination);
 					Message errmsg=cloneMessage(message);
 					producer.send(errmsg);
 					
 					producer.close();
 					logger.info("message send to queue {}",queue);
 				}
 				catch (JMSException e2) {throw new RuntimeException(e2); }
 			}
 			finally {
 				if (message!=null)
 					try {
 						logger.debug("committing session with message {}", message.getJMSMessageID());
 						session.commit();
 					} catch (JMSException e) { throw new RuntimeException(e); }
 			}
 		}
 
 
 		private Message cloneMessage( Message src ) {
 			try {
 				Message dest;
 				if( src instanceof BytesMessage ) {
 					dest = session.createBytesMessage();
 					int len = (int) ((BytesMessage) src ).getBodyLength();
 					byte[] msg = new byte[ len ];
 					( (BytesMessage) src ).readBytes( msg );
 					( (BytesMessage) dest ).writeBytes( msg );
 
 				} 
 				else if( src instanceof TextMessage ) {
 					dest = session.createTextMessage();
 					( (TextMessage) dest).setText( ( (TextMessage) src).getText() );
 				}
 				else  
 					throw new RuntimeException( "Unsupported message format: "+ src.getClass().getName() );
 
 				if( src.getJMSMessageID() != null ) dest.setJMSMessageID( src.getJMSMessageID() );
 				if( src.getJMSCorrelationID() != null ) dest.setJMSCorrelationID( src.getJMSCorrelationID() );
 				if( src.getJMSReplyTo() != null ) dest.setJMSReplyTo( src.getJMSReplyTo() );
 				if( src.getJMSType() != null ) dest.setJMSType( src.getJMSType() );
 				dest.setJMSDeliveryMode( src.getJMSDeliveryMode() );
 				dest.setJMSExpiration( src.getJMSExpiration() );
 				dest.setJMSPriority( src.getJMSPriority() );
 				dest.setJMSRedelivered( src.getJMSRedelivered() );
 				dest.setJMSTimestamp( src.getJMSTimestamp() );
 
 				Enumeration<?> properties = src.getPropertyNames();
 				while( properties.hasMoreElements() ) {
 					String key = (String) properties.nextElement();
 					if( key.startsWith( "JMSX" ) ) continue;
 					dest.setObjectProperty( key, src.getObjectProperty( key ) );
 				} 
 				return dest;
 			}
 			catch (JMSException e) { throw new RuntimeException(e);}
 		}
 	}
 
 
 	public boolean listening() { return threads!=null; }
 	public void stopListening() { running=false; } 
 
 }
