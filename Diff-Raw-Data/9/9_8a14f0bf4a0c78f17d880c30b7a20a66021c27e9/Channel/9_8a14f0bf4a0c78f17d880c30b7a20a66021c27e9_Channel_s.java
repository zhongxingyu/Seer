 /*******************************************************************************
  * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
  * program and the accompanying materials are made available under the terms of
  * the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Composent, Inc. - initial API and implementation
  ******************************************************************************/
 package org.eclipse.ecf.provider.jms.channel;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.ConnectException;
 import java.net.SocketAddress;
 import java.util.HashMap;
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
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.ecf.core.identity.ID;
 import org.eclipse.ecf.core.util.ECFException;
 import org.eclipse.ecf.core.util.Trace;
 import org.eclipse.ecf.internal.provider.jms.JmsDebugOptions;
 import org.eclipse.ecf.internal.provider.jms.JmsPlugin;
 import org.eclipse.ecf.provider.comm.AsynchEvent;
 import org.eclipse.ecf.provider.comm.DisconnectEvent;
 import org.eclipse.ecf.provider.comm.IConnectionListener;
 import org.eclipse.ecf.provider.comm.ISynchAsynchConnection;
 import org.eclipse.ecf.provider.comm.ISynchAsynchEventHandler;
 import org.eclipse.ecf.provider.jms.identity.JMSID;
 
 public abstract class Channel extends SocketAddress implements
 		ISynchAsynchConnection {
 
 	public static final String DEFAULT_USER = "defaultUser";
 
 	public static final String DEFAULT_PASSWORD = "defaultPassword";
 
 	private static final int HARD_DISCONNECT_ERROR_CODE = 31001;
 
 	private static final int CLOSE_ERROR_CODE = 31002;
 
 	private static final int HANDLE_ASYNCH_ERROR_CODE = 31003;
 
 	private static final int INTERRUPTED_ERROR_CODE = 31004;
 
 	private static final int HANDLE_SYNCH_ERROR_CODE = 31005;
 
 	private static final int GET_CONNECTIONID_ERROR_CODE = 31006;
 
 	private static final int ONMESSAGE_ERROR_CODE = 0;
 
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
 
 	protected ISynchAsynchEventHandler handler;
 
 	protected int keepAlive = -1;
 
 	String topicName;
 
 	Map properties = new HashMap();
 
 	public Channel(ISynchAsynchEventHandler hand, int keepAlive) {
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
 
 	/**
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.core.comm.IConnection#connect(org.eclipse.ecf.core.identity.ID,
 	 *      java.lang.Object, int)
 	 */
 	public abstract Object connect(ID remote, Object data, int timeout)
 			throws ECFException;
 
 	protected void onJMSException(JMSException except) {
 		if (isConnected() && isStarted()) {
 			handler.handleDisconnectEvent(new DisconnectEvent(this, except,
 					null));
 		}
 	}
 
 	protected void setup() throws IOException {
 		Trace.entering(JmsPlugin.PLUGIN_ID,
 				JmsDebugOptions.METHODS_ENTERING, this.getClass(), "setup");
 		try {
 			ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(
 					username, password, url);
 			connection = factory.createConnection();
 			connection.setExceptionListener(new ExceptionListener() {
 				public void onException(JMSException arg0) {
 					onJMSException(arg0);
 				}
 			});
 			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 			topicDest = session.createTopic(topicName);
 			topicConsumer = session.createConsumer(topicDest);
 			topicProducer = session.createProducer(topicDest);
 			topicConsumer.setMessageListener(new TopicReceiver());
 			connected = true;
 			connection.start();
 		} catch (JMSException e) {
 			Trace.catching(JmsPlugin.PLUGIN_ID,
 					JmsDebugOptions.EXCEPTIONS_CATCHING, this.getClass(),
 					"setup", e);
 			hardDisconnect();
 			throwIOException("setup", "Exception in channel setup", e);
 		}
 		Trace.exiting(JmsPlugin.PLUGIN_ID, JmsDebugOptions.METHODS_EXITING,
 				this.getClass(), "setup");
 	}
 
 	public void sendAsynch(ID recipient, Object obj) throws IOException {
 		queueObject(recipient, (Serializable) obj);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.provider.comm.IAsynchConnection#sendAsynch(org.eclipse.ecf.core.identity.ID,
 	 *      byte[])
 	 */
 	public void sendAsynch(ID recipient, byte[] obj) throws IOException {
 		queueObject(recipient, obj);
 	}
 
 	public synchronized void queueObject(ID recipient, Serializable obj)
 			throws IOException {
 		Trace.entering(JmsPlugin.PLUGIN_ID,
 				JmsDebugOptions.METHODS_ENTERING, this.getClass(),
 				"queueObject", new Object[] { recipient, obj });
 		if (!isConnected())
 			throw new ConnectException("Not connected");
 		ObjectMessage msg = null;
 		try {
 			msg = session.createObjectMessage(new JMSMessage(getConnectionID(),
 					getLocalID(), recipient, obj));
 			topicProducer.send(msg);
 		} catch (JMSException e) {
 			Trace.catching(JmsPlugin.PLUGIN_ID,
 					JmsDebugOptions.EXCEPTIONS_CATCHING, this.getClass(),
 					"queueObject", e);
 			disconnect();
 			throwIOException("queueObject", "Exception in queueObject", e);
 		}
 		Trace.exiting(JmsPlugin.PLUGIN_ID, JmsDebugOptions.METHODS_EXITING,
 				this.getClass(), "queueObject");
 	}
 
 	protected void onTopicException(JMSException except) {
 		Trace.entering(JmsPlugin.PLUGIN_ID,
 				JmsDebugOptions.METHODS_ENTERING, this.getClass(),
 				"onTopicException", new Object[] { except });
 		if (isConnected() && isStarted()) {
 			handler.handleDisconnectEvent(new DisconnectEvent(this, except,
 					null));
 		}
 		Trace.exiting(JmsPlugin.PLUGIN_ID, JmsDebugOptions.METHODS_EXITING,
 				this.getClass(), "onTopicException");
 	}
 
 	protected void throwIOException(String method, String msg, Throwable t)
 			throws IOException {
 		Trace
 				.throwing(JmsPlugin.PLUGIN_ID,
 						JmsDebugOptions.EXCEPTIONS_CATCHING, this.getClass(),
 						method, t);
 		IOException except = new IOException(msg + ": " + t.getMessage());
 		except.setStackTrace(t.getStackTrace());
 		throw except;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.provider.comm.IConnection#isConnected()
 	 */
 	public synchronized boolean isConnected() {
 		return connected;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.provider.comm.IConnection#isStarted()
 	 */
 	public synchronized boolean isStarted() {
 		return started;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.provider.comm.IConnection#getProperties()
 	 */
 	public Map getProperties() {
 		return properties;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.core.comm.IConnection#addCommEventListener(org.eclipse.ecf.core.comm.IConnectionListener)
 	 */
 	public void addListener(IConnectionListener listener) {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.core.comm.IConnection#removeCommEventListener(org.eclipse.ecf.core.comm.IConnectionListener)
 	 */
 	public void removeListener(IConnectionListener listener) {
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
 		Trace
 				.entering(JmsPlugin.PLUGIN_ID,
 						JmsDebugOptions.METHODS_ENTERING, this.getClass(),
 						"disconnect");
 		connected = false;
 		stop();
 		notifyAll();
 	}
 
 	protected void hardDisconnect() {
 		try {
 			disconnect();
 		} catch (Exception e) {
 			traceAndLogExceptionCatch(HARD_DISCONNECT_ERROR_CODE,
 					"hardDisconnect", e);
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
 			traceAndLogExceptionCatch(CLOSE_ERROR_CODE, "close", e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.provider.comm.IConnection#stop()
 	 */
 	public synchronized void stop() {
 		close();
 		started = false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.provider.comm.IConnection#start()
 	 */
 	public synchronized void start() {
 		started = true;
 	}
 
 	protected void handleTopicMessage(Message msg, JMSMessage jmsmsg) {
 		Trace.entering(JmsPlugin.PLUGIN_ID,
 				JmsDebugOptions.METHODS_ENTERING, this.getClass(),
 				"handleTopicMessage", new Object[] { msg, jmsmsg });
 		if (isConnected() && isStarted()) {
 			try {
 				Object o = jmsmsg.getData();
 				handler.handleAsynchEvent(new AsynchEvent(this, o));
 			} catch (IOException e) {
 				Trace.catching(JmsPlugin.PLUGIN_ID,
 						JmsDebugOptions.EXCEPTIONS_CATCHING, this.getClass(),
 						"handleTopicMessage", e);
 				JmsPlugin.getDefault().log(
 						new Status(IStatus.ERROR, JmsPlugin.PLUGIN_ID,
 								HANDLE_ASYNCH_ERROR_CODE,
 								"Exception on handleTopicMessage", e));
 				hardDisconnect();
 			}
 		}
 		Trace.exiting(JmsPlugin.PLUGIN_ID, JmsDebugOptions.METHODS_EXITING,
 				this.getClass(), "handleTopicMessage");
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
 		Trace.entering(JmsPlugin.PLUGIN_ID,
 				JmsDebugOptions.METHODS_ENTERING, this.getClass(),
 				"sendAndWait", new Object[] { obj, new Integer(waitDuration) });
 		synchronized (synch) {
 			try {
 				ObjectMessage msg = session.createObjectMessage(obj);
 				correlation = String.valueOf(getNextCorrelationID());
 				msg.setJMSCorrelationID(correlation);
 				topicProducer.send(msg);
 				synch.wait(waitDuration);
 			} catch (JMSException e) {
 				Trace.catching(JmsPlugin.PLUGIN_ID,
 						JmsDebugOptions.EXCEPTIONS_CATCHING, this.getClass(),
 						"sendAndWait", e);
 				throwIOException("sendAndWait", "JMSException in sendAndWait",
 						e);
 			} catch (InterruptedException e) {
 				traceAndLogExceptionCatch(INTERRUPTED_ERROR_CODE,
 						"handleTopicMessage", e);
 			}
 			Trace.exiting(JmsPlugin.PLUGIN_ID,
 					JmsDebugOptions.METHODS_EXITING, this.getClass(),
 					"sendAndWait", reply);
 			return reply;
 		}
 	}
 
 	protected String removeLeadingSlashes(String path) {
 		while (path.indexOf('/') != -1) {
 			path = path.substring(1);
 		}
 		return path;
 	}
 
 	protected void handleSynchMessage(ObjectMessage msg, ECFMessage ecfmsg) {
 		Trace.entering(JmsPlugin.PLUGIN_ID,
 				JmsDebugOptions.METHODS_ENTERING, this.getClass(),
 				"handleSynchMessage", new Object[] { msg, ecfmsg });
 		synchronized (synch) {
 			if (correlation == null)
 				return;
 			try {
 				if (correlation.equals(msg.getJMSCorrelationID())) {
 					reply = msg.getObject();
 					synch.notify();
 				}
 			} catch (JMSException e) {
 				traceAndLogExceptionCatch(HANDLE_SYNCH_ERROR_CODE,
 						"handleTopicMessage", e);
 			}
 		}
 		Trace.exiting(JmsPlugin.PLUGIN_ID, JmsDebugOptions.METHODS_EXITING,
 				this.getClass(), "handleSynchMessage");
 	}
 
 	protected void traceAndLogExceptionCatch(int code, String method,
 			Throwable e) {
 		Trace
 				.catching(JmsPlugin.PLUGIN_ID,
 						JmsDebugOptions.EXCEPTIONS_CATCHING, this.getClass(),
 						method, e);
 		JmsPlugin.getDefault().log(
 						new Status(IStatus.ERROR, JmsPlugin.PLUGIN_ID, code,
 								method, e));
 	}
 
 	protected String getConnectionID() {
 		String res = null;
 		try {
 			res = connection.getClientID();
 			return res;
 		} catch (Exception e) {
 			traceAndLogExceptionCatch(GET_CONNECTIONID_ERROR_CODE,
 					"getConnectionID", e);
 			return null;
 		}
 	}
 
 	public abstract Object sendSynch(ID target, byte[] data) throws IOException;
 
 	protected abstract void respondToRequest(ObjectMessage omsg, ECFMessage o);
 
 	class TopicReceiver implements MessageListener {
 
 		public void onMessage(Message msg) {
 			Trace.entering(JmsPlugin.PLUGIN_ID,
 					JmsDebugOptions.METHODS_ENTERING, this.getClass(),
 					"handleSynchMessage", new Object[] { msg });
 			try {
 				if (msg instanceof ObjectMessage) {
 					ObjectMessage omg = (ObjectMessage) msg;
 					Object o = omg.getObject();
 					if (o instanceof ECFMessage) {
 						ECFMessage ecfmsg = (ECFMessage) o;
 						ID fromID = ecfmsg.getSenderID();
 						if (fromID == null) {
 							Trace.exiting(JmsPlugin.PLUGIN_ID,
 									JmsDebugOptions.METHODS_ENTERING, this
 											.getClass(),
 									"onMessage.fromID=null");
 							return;
 						}
 						if (fromID.equals(getLocalID())) {
 							Trace.exiting(JmsPlugin.PLUGIN_ID,
 									JmsDebugOptions.METHODS_ENTERING, this
 											.getClass(),
 									"onMessage.fromID=getLocalID()");
 							return;
 						}
 						ID targetID = ecfmsg.getTargetID();
 						if (targetID == null) {
 							if (ecfmsg instanceof JMSMessage)
 								handleTopicMessage(msg, (JMSMessage) ecfmsg);
 							else
 								Trace
 										.trace(JmsPlugin.PLUGIN_ID,
 												"onMessage.received invalid message to group");
 						} else {
 							if (targetID.equals(getLocalID())) {
 								if (ecfmsg instanceof JMSMessage)
 									handleTopicMessage(msg, (JMSMessage) ecfmsg);
 								else if (ecfmsg instanceof SynchRequest)
 									respondToRequest(omg, ecfmsg);
 								else if (ecfmsg instanceof SynchResponse)
 									handleSynchMessage(omg, ecfmsg);
 								else
 									Trace.trace(JmsPlugin.PLUGIN_ID,
 											"onMessage.msg invalid message to "
 													+ targetID);
 							}
 						}
 					} else
 						// received bogus message...ignore
 						Trace.trace(JmsPlugin.PLUGIN_ID,
 								"onMessage received non-ECFMessage...ignoring: "
 										+ o);
 				} else
 					Trace.trace(JmsPlugin.PLUGIN_ID,
 							"onMessage.non object message received: " + msg);
 			} catch (Exception e) {
 				traceAndLogExceptionCatch(ONMESSAGE_ERROR_CODE, "onMessage", e);
 				hardDisconnect();
 			}
 			Trace.exiting(JmsPlugin.PLUGIN_ID,
 					JmsDebugOptions.METHODS_EXITING, this.getClass(),
 					"onMessage");
 
 		}
 	}
 }
