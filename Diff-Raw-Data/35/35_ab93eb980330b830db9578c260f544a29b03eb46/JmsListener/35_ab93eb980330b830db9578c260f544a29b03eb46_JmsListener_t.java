 package org.kisst.jms;
 
 import java.util.Enumeration;
 
 import javax.jms.Destination;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageConsumer;
 import javax.jms.MessageProducer;
 import javax.jms.Queue;
 import javax.jms.QueueBrowser;
 import javax.jms.QueueSession;
 import javax.jms.Session;
 import javax.jms.TextMessage;
 
 import org.kisst.cfg4j.Props;
 import org.kisst.gft.FunctionalException;
 import org.kisst.gft.RetryableException;
 import org.kisst.util.TemplateUtil;
 import org.kisst.util.TimeWindowList;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class JmsListener implements Runnable {
 	private final static Logger logger=LoggerFactory.getLogger(JmsListener.class); 
 
 	private final JmsSystem system;
 	private final MessageHandler handler;
 	private final Props props;
 	public final String queue;
 	public final String errorqueue;
 	public final String retryqueue;
 	private final int receiveErrorRetries;
 	private final int receiveErrorRetryDelay;
 	private final long interval;
 	private final TimeWindowList forbiddenTimes;
 
 	private QueueSession session = null;
 	private Queue destination = null;
 	private MessageConsumer consumer = null;
 	private boolean browseMode=true; 
 	private boolean running=false;
 
 
 	Thread thread;
 
 	public JmsListener(JmsSystem system, MessageHandler handler, Props props, Object context) {
 		this.system=system;
 		this.props=props;
 		this.handler=handler;
 		this.interval=props.getLong("interval",5000);
 
 		this.queue=TemplateUtil.processTemplate(props.getString("queue"), context);
 		this.errorqueue=TemplateUtil.processTemplate(props.getString("errorqueue"), context);
 		this.retryqueue=TemplateUtil.processTemplate(props.getString("retryqueue"), context);
 		this.receiveErrorRetries = props.getInt("receiveErrorRetries", 1000);
 		this.receiveErrorRetryDelay = props.getInt("receiveErrorRetryDelay", 60000);
 		String timewindow=props.getString("forbiddenTimes", null);
 		if (timewindow==null)
 			this.forbiddenTimes=null;
 		else
 			this.forbiddenTimes=new TimeWindowList(timewindow);
 	}
 
 	public String toString() { return "JmsListener("+queue+")"; }
 
 	public boolean isForbiddenTime() {
 		return forbiddenTimes!=null && forbiddenTimes.isTimeInWindow();
 	} 
 
 	public synchronized void start() {
 		logger.info("Starting Listener {}",this);
 		if (thread!=null)
 			logger.warn("Listener already started");
 		else {
 			running=true;
 			thread=new Thread(this);
 			thread.start();
 		}
 	}
 	public void stop() { 
 		logger.info("Stopping Listener {}",this);
 		running=false;
 		Thread t=thread;
 		if (t!=null) {
 			try {
 				t.join();
 			} catch (InterruptedException e) { throw new RuntimeException(e);}
 			logger.info("Stopped Listener {}",this);
 		}
 	}
 
 	public void run() {
 		// Always start in browse mode, to check for stop messages
 		// If one would receive the stop message, other threads/machines might temporarily 
 		// not see it, and pick-up the next message
 		logger.info("Started Listener on queue {}",queue);
 		enterBrowseMode(); 
 		try {
 			while (running) {
 				Message message=null;
 				message = getMessage();
 				if (message!=null) {
 					if (logger.isDebugEnabled())
 						logger.debug("handling message {}",message.getJMSMessageID());
 					handleMessage(message);
 				}
 			}
 		}
 		catch (JMSException e) {
 			logger.error("Unrecoverable error during listening, stopped listening", e);
 			if (props.getBoolean("exitOnUnrecoverableListenerError", false))
 				System.exit(1);
 		}
 		finally {
 			thread=null;
 			logger.info("Stopped listening to queue {}", queue);
 			closeSession();
 		}
 	}
 
 	private Message getMessage() throws JMSException {
 		if (isForbiddenTime()) {
 			logger.debug("Sleeping for {} millisecs because of forbiddenTime", interval);
 			sleepSomeTime(interval);
 			return null;
 		}
 		int retryCount=0;
 		try {
 			if (session==null)
 				openSession();
 			if (checkBrowseMode())
 				return null;
 			Message message = consumer.receive(interval);
 			if (message!=null) {
 				if (isStopMessage(message)) {
 					logger.info("Received a stop message on queue {}, rolling back the stop message",queue);
 					session.rollback(); // put the message back on the queue
 					closeSession(); // recover the session so it will see the stop message
 					enterBrowseMode();
 					return null;
 				}
 				else if (isStartMessage(message)) {
 					message.acknowledge(); // remove the start message
 					return null;
 				}
 				return message;
 			}
 			retryCount=0;
 		}
 		catch (Exception e) {
 			logger.error("Error when receiving JMS message on queue "+queue, e);
 			if (retryCount++ > receiveErrorRetries)
 				throw new RuntimeException("too many receive retries for queue "+queue);
 			closeSession();
 			logger.info("sleeping for "+receiveErrorRetryDelay+" milliseconds for retrying listening to "+queue);
 			sleepSomeTime(receiveErrorRetryDelay);
 		}
 		return null;
 	}
 
 	private void enterBrowseMode() {
 		logger.info("Entering browse mode on queue {}",queue);
 		browseMode=true;
 	}
 	private void exitBrowseMode() {
 		logger.info("Exiting browse mode on queue {}",queue);
 		browseMode=false;
 	}
 
 	/**
 	 * Will browse the top of the queue for starting or stopping messages 
 	 * If multiple starting/stopping message are available all but the last will be removed
 	 * If the last message is a start message it will be removed as well
 	 */
 	private boolean checkBrowseMode() {
 		if (! browseMode)
 			return false;
 		QueueBrowser browser=null;
 		try {
 			if (session==null)
 				openSession();
 			browser = session.createBrowser(destination);
 
 			logger.debug("browsing for a message");
 			Enumeration<?> e = browser.getEnumeration();
 			Message lastMessage=null;
 			while (e.hasMoreElements()) {
 				Message msg = (Message) e.nextElement();
 				if (logger.isDebugEnabled())
 					logger.debug("browsing message with id {}, content {}",msg.getJMSMessageID(),((TextMessage) msg).getText());
 				if (isStopMessage(msg) || isStartMessage(msg)) {
 					if (lastMessage!=null) {
						browser.close();
						resetSession();
 						logger.info("Removing control message from queue {} because there is a newer control messages",queue);
 						if (!removeSpecificMessage(lastMessage)) {
 							logger.warn("Could not remove stop message, stopped looking for more control messages");
 							return true;
 						}
 					}
 					lastMessage=msg;
 				}
 				else 
 					break;
 			}
 			if (lastMessage==null) // there were no control message on queue, no reason to stay in browsemode
 				exitBrowseMode(); 
 			if (isStopMessage(lastMessage)) {
 				sleepSomeTime(this.interval);
 				return true;
 			}
 			if (isStartMessage(lastMessage)) { // startMessage may be removed
 				logger.info("Removing start message from queue {} because it is the newest start message",queue);
				browser.close();
				resetSession();
 				if (removeSpecificMessage(lastMessage))
 					exitBrowseMode();
 				else
 					logger.warn("Could not remove start message, staying in browse mode",queue);
 
 			}		
 			return false;
 		}
 		catch (JMSException e) { throw new RuntimeException(e);}
 		finally {
 			try {
 				if (browser!=null)
 					browser.close();
 			} catch (JMSException e) { throw new RuntimeException(e); }
 		}
 	}
 
 	private boolean removeSpecificMessage(Message m) {
 		try {
 			String msgid=m.getJMSMessageID();
			String selector = "JMSMessageID='" +msgid+  "'";
 			if (logger.isInfoEnabled()) {
 				String body = ((TextMessage)m).getText();
 				logger.info("Removing message [{}] from queue {} with content\n"+body,selector, queue);
 			}
			consumer.close();
			Message msg = session.createConsumer(destination, selector).receive(2000);
 			if (msg!=null) {
				session.commit();
				resetSession();
 				return true;
 			}
 			else {
 				logger.warn("Could not find for removal a message with id {} ",msgid);
				resetSession();
 				return false;
 			}
 		}
 		catch (JMSException e) { throw new RuntimeException(e);}
 	}
 	private boolean isStopMessage(Message msg) {
 		try {
 			if (msg==null)
 				return false;
 			String body = ((TextMessage)msg).getText();
 			return body.startsWith("6") && body.indexOf("Stop")>0;
 		}
 		catch (JMSException e) { throw new RuntimeException(e);}
 	}
 	private boolean isStartMessage(Message msg) {
 		try{
 			if (msg==null)
 				return false;
 			String body = ((TextMessage)msg).getText();
 			return body.startsWith("6") && body.indexOf("Start")>0;
 		}
 		catch (JMSException e) { throw new RuntimeException(e);}
 	}
 
 	private void sleepSomeTime(long delay) {
 		try {
 			Thread.sleep(delay);
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
 
	private void resetSession() throws JMSException {
		consumer.close();
		session.close();
		//session = system.getConnection().createQueueSession(true, Session.SESSION_TRANSACTED);
		//destination = session.createQueue(queue);
		//consumer=null;
		openSession();
	}
 	private void openSession() throws JMSException {
 		session = system.getConnection().createQueueSession(true, Session.SESSION_TRANSACTED);
 		destination = session.createQueue(queue);
 		consumer = session.createConsumer(destination);
 	}
 
 
 	private void handleMessage(Message message) {
 		try {
 			logger.debug("Handling {}",message.getJMSMessageID());
 			handler.handle(new JmsMessage(message)); 
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
 				Destination errordestination = session.createQueue(queue+system.sendParams);
 				MessageProducer producer = session.createProducer(errordestination);
 				Message errmsg=JmsUtil.cloneMessage(session, message);
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
 
 }
 
