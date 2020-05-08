 /*******************************************************************************
  * Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
  * program and the accompanying materials are made available under the terms of
  * the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Composent, Inc. - initial API and implementation
  ******************************************************************************/
 package org.eclipse.ecf.provider.jms.container;
 
 import java.util.Dictionary;
 import javax.jms.*;
 import org.eclipse.ecf.core.AbstractContainer;
 import org.eclipse.ecf.core.ContainerConnectException;
 import org.eclipse.ecf.core.events.ContainerConnectedEvent;
 import org.eclipse.ecf.core.events.ContainerConnectingEvent;
 import org.eclipse.ecf.core.identity.*;
 import org.eclipse.ecf.core.security.IConnectContext;
 import org.eclipse.ecf.core.util.ECFException;
 import org.eclipse.ecf.provider.jms.identity.JMSID;
 import org.eclipse.ecf.provider.jms.identity.JMSNamespace;
 import org.eclipse.ecf.remoteservice.*;
 import org.eclipse.equinox.concurrent.future.IFuture;
 import org.osgi.framework.InvalidSyntaxException;
 
 public abstract class AbstractJMSQueueConsumerContainer extends AbstractContainer implements MessageListener, IRemoteServiceContainerAdapter, IJMSQueueContainer {
 
 	private JMSID id;
 	private JMSID connectedID;
 	private Object queueConnectLock = new Object();
 
 	// JMS
 	private Connection connection;
 	private Session session;
 	private Queue queue;
 	private MessageProducer messageProducer;
 	private TemporaryQueue responseQueue;
 
 	private LBRegistrySharedObject registry;
 
 	public JMSID queueID;
 
 	public AbstractJMSQueueConsumerContainer(JMSID containerID, JMSID queueID) {
 		this.id = containerID;
 		this.queueID = queueID;
 		registry = new LBRegistrySharedObject(IDFactory.getDefault().createStringID(LBRegistrySharedObject.class.getName()), this);
 	}
 
 	protected JMSID getQueueID() {
 		return queueID;
 	}
 
 	protected void setQueueID(JMSID queueID) {
 		this.queueID = queueID;
 	}
 
 	public abstract void start() throws ECFException;
 
 	public Session getSession() {
 		return session;
 	}
 
 	public MessageProducer getMessageProducer() {
 		return messageProducer;
 	}
 
 	public TemporaryQueue getResponseQueue() {
 		return responseQueue;
 	}
 
 	public void connect(ID targetID, IConnectContext connectContext) throws ContainerConnectException {
 		if (targetID == null)
 			throw new ContainerConnectException("targetID cannot be null"); //$NON-NLS-1$
 		if (!(targetID instanceof JMSID))
 			throw new ContainerConnectException("targetID not JMSID type"); //$NON-NLS-1$
 		JMSID jmsTargetID = (JMSID) targetID;
 		synchronized (getQueueConnectLock()) {
 			fireContainerEvent(new ContainerConnectingEvent(getID(), jmsTargetID));
 			try {
 				setupJMSQueueConsumer(jmsTargetID);
 			} catch (JMSException e) {
 				throw new ContainerConnectException("Could not connect to targetID=" + targetID, e); //$NON-NLS-1$
 			}
 			this.connectedID = jmsTargetID;
 		}
 		fireContainerEvent(new ContainerConnectedEvent(getID(), jmsTargetID));
 	}
 
 	protected Object getQueueConnectLock() {
 		return queueConnectLock;
 	}
 
 	protected void setupJMSQueueConsumer(JMSID jmsTargetID) throws JMSException {
 		synchronized (getQueueConnectLock()) {
 			String jmsServerString = jmsTargetID.getBroker();
 			String messageQueueName = jmsTargetID.getTopicOrQueueName();
 			ConnectionFactory connectionFactory = getQueueConnectionFactory(jmsServerString, null);
 			connection = connectionFactory.createConnection();
 			connection.start();
 			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 			queue = this.session.createQueue(messageQueueName);
 			//Setup a message producer to respond to messages from clients, we will get the destination 
 			//to send to from the JMSReplyTo header field from a Message 
 			this.messageProducer = this.session.createProducer(null);
 			// Setup a temporary queue to handle responses from servers
 			this.responseQueue = this.session.createTemporaryQueue();
 
 			MessageConsumer consumer = this.session.createConsumer(queue);
 			consumer.setMessageListener(this);
 		}
 	}
 
 	protected void shutdownJMSQueueConsumer() {
 		synchronized (getQueueConnectLock()) {
 			if (connection != null) {
 				try {
 					messageProducer.close();
 				} catch (JMSException e1) {
 					// TODO log
 					e1.printStackTrace();
 				}
 				messageProducer = null;
 				queue = null;
 				try {
 					session.close();
 				} catch (JMSException e1) {
 					// TODO log
 					e1.printStackTrace();
 				}
 				session = null;
 				try {
 					connection.close();
 				} catch (JMSException e) {
 					// TODO log
 					e.printStackTrace();
 				}
 				connection = null;
 			}
 		}
 	}
 
 	public void disconnect() {
 		synchronized (getQueueConnectLock()) {
 			shutdownJMSQueueConsumer();
 			connectedID = null;
 		}
 	}
 
 	public void dispose() {
 		disconnect();
 		super.dispose();
 		registry.dispose(getID());
 		registry = null;
 	}
 
 	public Namespace getConnectNamespace() {
 		return IDFactory.getDefault().getNamespaceByName(JMSNamespace.NAME);
 	}
 
 	public ID getConnectedID() {
 		return connectedID;
 	}
 
 	public ID getID() {
 		return id;
 	}
 
 	// Message Listener implementation
 	public void onMessage(Message message) {
 		registry.handleJMSMessage(message);
 	}
 
 	public void addRemoteServiceListener(IRemoteServiceListener listener) {
 		registry.addRemoteServiceListener(listener);
 	}
 
 	// these lookup methods are not needed
 	public IFuture asyncGetRemoteServiceReferences(ID[] idFilter, String clazz, String filter) {
 		return registry.asyncGetRemoteServiceReferences(idFilter, clazz, filter);
 	}
 
 	public IFuture asyncGetRemoteServiceReferences(ID target, String clazz, String filter) {
 		return registry.asyncGetRemoteServiceReferences(target, clazz, filter);
 	}
 
	public IFuture asyncGetRemoteServiceReferences(ID target, ID[] idFilter, String clazz, String filter) {
		return registry.asyncGetRemoteServiceReferences(target, idFilter, clazz, filter);
	}

 	public IRemoteFilter createRemoteFilter(String filter) throws InvalidSyntaxException {
 		return registry.createRemoteFilter(filter);
 	}
 
 	public IRemoteServiceReference[] getAllRemoteServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
 		return registry.getAllRemoteServiceReferences(clazz, filter);
 	}
 
 	public IRemoteService getRemoteService(IRemoteServiceReference reference) {
 		return registry.getRemoteService(reference);
 	}
 
 	public IRemoteServiceID getRemoteServiceID(ID containerID, long containerRelativeID) {
 		return registry.getRemoteServiceID(containerID, containerRelativeID);
 	}
 
 	public Namespace getRemoteServiceNamespace() {
 		return registry.getRemoteServiceNamespace();
 	}
 
 	public IRemoteServiceReference getRemoteServiceReference(IRemoteServiceID serviceID) {
 		return registry.getRemoteServiceReference(serviceID);
 	}
 
 	public IRemoteServiceReference[] getRemoteServiceReferences(ID[] idFilter, String clazz, String filter) throws InvalidSyntaxException {
 		return registry.getRemoteServiceReferences(idFilter, clazz, filter);
 	}
 
 	public IRemoteServiceReference[] getRemoteServiceReferences(ID target, String clazz, String filter) throws InvalidSyntaxException, ContainerConnectException {
 		return registry.getRemoteServiceReferences(target, clazz, filter);
 	}
 
	public IRemoteServiceReference[] getRemoteServiceReferences(ID target, ID[] idFilter, String clazz, String filter) throws InvalidSyntaxException, ContainerConnectException {
		return registry.getRemoteServiceReferences(target, idFilter, clazz, filter);
	}

 	public IRemoteServiceRegistration registerRemoteService(String[] clazzes, Object service, Dictionary properties) {
 		return registry.registerRemoteService(clazzes, service, properties);
 	}
 
 	public void removeRemoteServiceListener(IRemoteServiceListener listener) {
 		registry.removeRemoteServiceListener(listener);
 	}
 
 	public void setConnectContextForAuthentication(IConnectContext connectContext) {
 		registry.setConnectContextForAuthentication(connectContext);
 	}
 
 	public boolean ungetRemoteService(IRemoteServiceReference reference) {
 		return registry.ungetRemoteService(reference);
 	}
 }
