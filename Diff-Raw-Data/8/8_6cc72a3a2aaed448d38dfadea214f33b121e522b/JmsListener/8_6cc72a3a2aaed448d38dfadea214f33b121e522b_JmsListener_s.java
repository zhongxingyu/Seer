 package org.kisst.gft.mq.jms;
 
 import javax.jms.Destination;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageConsumer;
 import javax.jms.MessageProducer;
 import javax.jms.Session;
 
 import org.kisst.cfg4j.Props;
 import org.kisst.gft.admin.rest.Representable;
 import org.kisst.gft.mq.MessageHandler;
 import org.kisst.gft.mq.QueueListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class JmsListener implements Runnable, QueueListener, Representable {
	private final static Logger logger=LoggerFactory.getLogger(JmsListener.class); 
 	
 	private final JmsSystem system;
 	private final Props props;
 	private final String queue;
 	private final String errorqueue;
 	private boolean running=false;
 	private MessageHandler handler=null;
 	private Thread thread=null;
 	
 	public JmsListener(JmsSystem system, Props props) {
 		this.system=system;
 		this.props=props;
 		this.queue=props.getString("queue");
 		this.errorqueue=props.getString("errorqueue");
 	}
 	
 	public String toString() { return "JmsListener("+queue+")"; }
 	public String getRepresentation() { return props.toString(); }
 	public void stop() {
 		logger.info("Stopping listening to queue {}", queue);
 		running=false;
 	}
 	public void run()  {
 		if (thread!=null)
 			throw new RuntimeException("Listener already running");
 		long interval=props.getLong("interval",5000);
 		thread=Thread.currentThread();
 		try {
 			Session session = system.getConnection().createSession(true, Session.SESSION_TRANSACTED);
 			logger.info("Opening queue {}",queue);
 
 			Destination destination = session.createQueue(queue);
 			MessageConsumer consumer = session.createConsumer(destination);
 			running=true;
 			while (running) {
 				Message message = consumer.receive(interval);
 				try {
 					if (message!=null) {
 						handler.handle(new JmsQueue.JmsMessage(message));
 						session.commit();
 					}
 				}
 				catch (Exception e) {
					e.printStackTrace();
 					Destination errordestination = session.createQueue(errorqueue);
 					MessageProducer producer = session.createProducer(errordestination);
 					producer.send(message);
 					session.commit();
 					producer.close();
 				}
 			}
 			consumer.close();
 			session.close();
 			logger.info("Stopped listening to queue {}", queue);
 		}
 		catch (JMSException e) {throw new RuntimeException(e); }
 		finally {
 			thread=null;
 			running=false;
 		}
 	}
 
 
 	public boolean listening() { return thread!=null; }
 	public void stopListening() { running=false; }
 	public void listen(MessageHandler handler) {
 		this.handler=handler;
 		running=true;
 		new Thread(this).start();
 	}
 }
