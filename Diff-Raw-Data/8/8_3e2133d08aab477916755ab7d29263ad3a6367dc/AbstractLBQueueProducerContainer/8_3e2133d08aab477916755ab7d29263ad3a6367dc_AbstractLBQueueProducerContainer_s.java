 /*******************************************************************************
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
 package org.eclipse.ecf.provider.jms.container;
 
 import javax.jms.*;
 import org.eclipse.ecf.core.identity.ID;
 import org.eclipse.ecf.core.identity.IDFactory;
 import org.eclipse.ecf.provider.jms.identity.JMSID;
 import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
 
 public abstract class AbstractLBQueueProducerContainer extends AbstractJMSServer implements IJMSQueueContainer {
 
 	private Object queueConnectLock = new Object();
 
 	// JMS
 	private Connection connection;
 	private Session session;
 	private Queue queue;
 	private MessageProducer messageProducer;
 	private TemporaryQueue responseQueue;
 
 	private LBRegistrySharedObject lbRegistry;
 
 	private JMSID queueID;
 
 	public AbstractLBQueueProducerContainer(JMSContainerConfig config, JMSID queueID) {
 		super(config);
 		setQueueID(queueID);
 	}
 
 	protected JMSID getQueueID() {
 		return queueID;
 	}
 
 	protected void setQueueID(JMSID queueID) {
 		this.queueID = queueID;
 	}
 
 	public Session getSession() {
 		return session;
 	}
 
 	public MessageProducer getMessageProducer() {
 		return messageProducer;
 	}
 
 	public TemporaryQueue getResponseQueue() {
 		return responseQueue;
 	}
 
 	protected Object getQueueConnectLock() {
 		return queueConnectLock;
 	}
 
 	protected void setupJMSQueueProducer(JMSID jmsTargetID) throws JMSException {
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
 			this.messageProducer = this.session.createProducer(queue);
 			// Setup a temporary queue to handle responses from servers
 			this.responseQueue = this.session.createTemporaryQueue();
 		}
 	}
 
 	public Object getAdapter(Class adapter) {
 		if (adapter == null)
 			return null;
 		if (adapter.isAssignableFrom(IRemoteServiceContainerAdapter.class)) {
 			return getRegistry();
 		}
 		return super.getAdapter(adapter);
 	}
 
 	Object registryLock = new Object();
 
 	LBRegistrySharedObject createAndAddLBRegistry() {
 		ID soID = IDFactory.getDefault().createStringID(LBRegistrySharedObject.class.getName());
		LBRegistrySharedObject registry = new LBRegistrySharedObject(this);
 		try {
 			getSharedObjectManager().addSharedObject(soID, lbRegistry, null);
 		} catch (Exception e) {
 			// Should not occur
 			throw new RuntimeException("createAndAddLBRegistry cannot add shared object"); //$NON-NLS-1$
 		}
		return registry;
 	}
 
 	private synchronized Object getRegistry() {
 		synchronized (registryLock) {
 			if (lbRegistry == null) {
				lbRegistry = createAndAddLBRegistry();
 			}
 			return lbRegistry;
 		}
 	}
 }
