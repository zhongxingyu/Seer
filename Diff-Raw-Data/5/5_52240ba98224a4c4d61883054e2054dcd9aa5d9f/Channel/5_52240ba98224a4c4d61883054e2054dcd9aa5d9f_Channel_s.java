 package org.eclipse.ecf.provider.jms.channel;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.ConnectException;
 import java.net.SocketAddress;
 import java.net.URI;
 import java.util.Map;
 import javax.jms.Connection;
 import javax.jms.Destination;
 import javax.jms.ExceptionListener;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageConsumer;
 import javax.jms.MessageListener;
 import javax.jms.MessageProducer;
 import javax.jms.ObjectMessage;
 import javax.jms.Session;
 import org.activemq.ActiveMQConnectionFactory;
 import org.eclipse.ecf.core.comm.AsynchConnectionEvent;
 import org.eclipse.ecf.core.comm.DisconnectConnectionEvent;
 import org.eclipse.ecf.core.comm.IConnectionEventHandler;
 import org.eclipse.ecf.core.comm.ISynchAsynchConnection;
 import org.eclipse.ecf.core.comm.ISynchAsynchConnectionEventHandler;
 import org.eclipse.ecf.core.identity.ID;
 import org.eclipse.ecf.provider.jms.Trace;
 import org.eclipse.ecf.provider.jms.identity.JMSID;
 
 public abstract class Channel extends SocketAddress implements
 		ISynchAsynchConnection {
 	public static final Trace trace = Trace.create("channel");
 	public static final String DEFAULT_USER = "defaultUser";
 	public static final String DEFAULT_PASSWORD = "defaultPassword";
 	protected static long correlationID = 0;
 	String username = DEFAULT_USER;
 	String password = DEFAULT_PASSWORD;
 	String url = null;
 	Connection connection = null;
 	Session session = null;
 	Destination topicDest = null;
 	MessageConsumer topicConsumer = null;
 	MessageProducer topicProducer = null;
 	protected JMSID managerID = null;
 	protected ID containerID;
 	boolean connected = false;
 	boolean started = false;
 	protected ISynchAsynchConnectionEventHandler handler;
 	protected int keepAlive = -1;
 	String topicName;
 
 	public Channel(ISynchAsynchConnectionEventHandler hand, int keepAlive) {
 		this.handler = hand;
 		this.containerID = hand.getEventHandlerID();
 		this.keepAlive = keepAlive;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.core.comm.IConnection#getLocalID()
 	 */
 	public ID getLocalID() {
 		return containerID;
 	}
 
 	public synchronized void setUsername(String username) {
 		this.username = username;
 	}
 
 	public synchronized void setPassword(String password) {
 		this.password = password;
 	}
 
 	protected static long getNextCorrelationID() {
 		return correlationID++;
 	}
 
 	public void trace(String msg) {
 		if (trace != null && Trace.ON) {
 			trace.msg(msg);
 		}
 	}
 
 	public void dumpStack(String msg, Throwable t) {
 		if (trace != null && Trace.ON) {
 			trace.dumpStack(t, msg);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.core.comm.IConnection#connect(org.eclipse.ecf.core.identity.ID,
 	 *      java.lang.Object, int)
 	 */
 	public abstract Object connect(ID remote, Object data, int timeout)
 			throws IOException;
 
	protected void onException(JMSException except) {
 		trace("onException(" + except + ")");
 		if (isConnected() && isStarted()) {
 			handler.handleDisconnectEvent(new DisconnectConnectionEvent(this,
 					except, null));
 		}
 	}
 
 	protected void setup() throws IOException {
 		trace("setup()");
 		try {
 			ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(
 					username, password, url);
 			connection = factory.createConnection();
 			connection.setExceptionListener(new ExceptionListener() {
 				public void onException(JMSException arg0) {
					onException(arg0);
 				}
 			});
 			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 			topicDest = session.createTopic(topicName);
 			topicConsumer = session.createConsumer(topicDest);
 			topicProducer = session.createProducer(topicDest);
 			topicConsumer.setMessageListener(new TopicReceiver());
 			connected = true;
 			connection.start();
 			trace("channel url=" + url + ",topic=" + topicName + ",clientid="
 					+ connection.getClientID());
 		} catch (JMSException e) {
 			dumpStack("Exception in setup", e);
 			hardDisconnect();
 			throwIOException("Exception in channel setup", e);
 		}
 	}
 
 	public void sendAsynch(ID recipient, Object obj) throws IOException {
 		queueObject(recipient, (Serializable) obj);
 	}
 
 	public void sendAsynch(ID recipient, byte[] obj) throws IOException {
 		queueObject(recipient, obj);
 	}
 
 	public synchronized void queueObject(ID recipient, Serializable obj)
 			throws IOException {
 		trace("queueObject(" + recipient + "," + obj + ")");
 		if (!isConnected())
 			throw new ConnectException("Not connected");
 		ObjectMessage msg = null;
 		try {
 			msg = session.createObjectMessage(new JMSMessage(getConnectionID(),
 					getLocalID(), recipient, obj));
 			topicProducer.send(msg);
 		} catch (JMSException e) {
 			dumpStack("Exception publishing message", e);
 			disconnect();
 			throwIOException("Exception in queueObject", e);
 		}
 	}
 
 	protected void onTopicException(JMSException except) {
 		trace("onTopicException(" + except + ")");
 		if (isConnected() && isStarted()) {
 			handler.handleDisconnectEvent(new DisconnectConnectionEvent(this,
 					except, null));
 		}
 	}
 
 	public void throwIOException(String msg, Throwable t) throws IOException {
 		IOException except = new IOException(msg + ": " + t.getMessage());
 		except.setStackTrace(t.getStackTrace());
 		dumpStack("Exception setting up topic", except);
 		throw except;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.core.comm.IConnection#isConnected()
 	 */
 	public synchronized boolean isConnected() {
 		return connected;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.core.comm.IConnection#isStarted()
 	 */
 	public synchronized boolean isStarted() {
 		return started;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.core.comm.IConnection#getProperties()
 	 */
 	public Map getProperties() {
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.core.comm.IConnection#addCommEventListener(org.eclipse.ecf.core.comm.IConnectionEventHandler)
 	 */
 	public void addCommEventListener(IConnectionEventHandler listener) {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.core.comm.IConnection#removeCommEventListener(org.eclipse.ecf.core.comm.IConnectionEventHandler)
 	 */
 	public void removeCommEventListener(IConnectionEventHandler listener) {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.core.comm.IConnection#getAdapter(java.lang.Class)
 	 */
 	public Object getAdapter(Class clazz) {
 		return null;
 	}
 
 	// protected abstract void handleQueueMessage(Message msg);
 	public synchronized void disconnect() throws IOException {
 		trace("disconnect()");
 		connected = false;
 		stop();
 		notifyAll();
 	}
 
 	protected void hardDisconnect() {
 		try {
 			disconnect();
 		} catch (Exception e1) {
 			dumpStack("Exception in disconnect", e1);
 		}
 	}
 
 	protected void close() {
 		try {
 			if (connection != null) {
 				connection.stop();
 				connection.close();
 				connection = null;
 			}
 		} catch (Exception e) {
 			dumpStack("Exception in close", e);
 		}
 	}
 
 	public synchronized void stop() {
 		// closeTopic();
 		// closeQueue();
 		close();
 		started = false;
 	}
 
 	public synchronized void start() {
 		started = true;
 	}
 
 	protected void handleTopicMessage(Message msg, JMSMessage jmsmsg) {
 		trace("handleTopicMessage(" + msg + "," + jmsmsg + ")");
 		if (isConnected() && isStarted()) {
 			try {
 				Object o = jmsmsg.getData();
 				handler.handleAsynchEvent(new AsynchConnectionEvent(this, o));
 			} catch (IOException e) {
 				dumpStack("Exception in handleAsynchEvent", e);
 				hardDisconnect();
 			}
 		} else {
 			trace("Not started...discarding message " + jmsmsg);
 		}
 	}
 	protected Object synch = new Object();
 	protected String correlation = null;
 	protected Serializable reply = null;
 
 	protected Serializable getReply() {
 		return reply;
 	}
 
 	protected Serializable sendAndWait(Serializable obj) throws IOException {
 		return sendAndWait(obj, keepAlive);
 	}
 
 	protected Serializable sendAndWait(Serializable obj, int waitDuration)
 			throws IOException {
 		synchronized (synch) {
 			try {
 				ObjectMessage msg = session.createObjectMessage(obj);
 				correlation = String.valueOf(getNextCorrelationID());
 				msg.setJMSCorrelationID(correlation);
 				topicProducer.send(msg);
 				synch.wait(waitDuration);
 			} catch (JMSException e) {
 				dumpStack("exception in sendAndWait", e);
 				throwIOException("JMSException in sendAndWait", e);
 			} catch (InterruptedException e1) {
 				dumpStack("Interrupted in sendAndWait", e1);
 			}
 			return reply;
 		}
 	}
 
 	protected String removeLeadingSlashes(URI uri) {
 		String name = uri.getPath();
 		while (name.indexOf('/') != -1) {
 			name = name.substring(1);
 		}
 		return name;
 	}
 
 	protected void handleSynchMessage(ObjectMessage msg, ECFMessage ecfmsg) {
 		trace("handleSynchMessage(" + ecfmsg + ")");
 		synchronized (synch) {
 			if (correlation == null)
 				return;
 			try {
 				if (correlation.equals(msg.getJMSCorrelationID())) {
 					reply = msg.getObject();
 					synch.notify();
 				}
 			} catch (JMSException e) {
 				dumpStack("JMSException in handleSynchMessage", e);
 			}
 		}
 	}
 
 	protected String getConnectionID() {
 		String res = null;
 		try {
 			res = connection.getClientID();
 			return res;
 		} catch (Exception e) {
 			dumpStack("Exception in getConnectionID", e);
 			return null;
 		}
 	}
 
 	public abstract Object sendSynch(ID target, byte[] data) throws IOException;
 
 	protected abstract void respondToRequest(ObjectMessage omsg, ECFMessage o);
 	class TopicReceiver implements MessageListener {
 		public void onMessage(Message msg) {
 			trace("onMessage(" + msg + ")");
 			try {
 				if (msg instanceof ObjectMessage) {
 					ObjectMessage omg = (ObjectMessage) msg;
 					Object o = omg.getObject();
 					if (o instanceof ECFMessage) {
 						ECFMessage ecfmsg = (ECFMessage) o;
 						ID fromID = ecfmsg.getSenderID();
 						if (fromID == null) {
 							trace("onMessage.msg invalid null sender");
 							return;
 						}
 						if (fromID.equals(getLocalID())) {
 							trace("onMessage.msg from "+fromID+" discarding message from us");
 							return;
 						}
 						ID targetID = ecfmsg.getTargetID();
 						if (targetID == null) {
 							if (ecfmsg instanceof JMSMessage) handleTopicMessage(msg, (JMSMessage) ecfmsg);
 							else trace("onMessage.received invalid message to group");
 						} else {
 							if (targetID.equals(getLocalID())) {
 								if (ecfmsg instanceof JMSMessage) handleTopicMessage(msg, (JMSMessage) ecfmsg);
 								else if (ecfmsg instanceof SynchRequest) respondToRequest(omg,ecfmsg);
 								else if (ecfmsg instanceof SynchResponse) handleSynchMessage(omg,ecfmsg);
 								else trace("onMessage.msg invalid message to "+targetID);
 							}
 						}
 					} else {
 						// received bogus message...ignore
 						trace("onMessage received non-ECFMessage...ignoring: "
 								+ o);
 					}
 				} else {
 					trace("onMessage.non object message received: " + msg);
 				}
 			} catch (Exception e) {
 				dumpStack("Exception handling topic message: " + msg
 						+ ". Disconnecting", e);
 				hardDisconnect();
 			}
 		}
 	}
 }
