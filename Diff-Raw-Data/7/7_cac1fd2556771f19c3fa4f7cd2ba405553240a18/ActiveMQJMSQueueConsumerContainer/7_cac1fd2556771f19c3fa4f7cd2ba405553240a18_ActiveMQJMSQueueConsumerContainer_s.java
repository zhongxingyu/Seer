 /*******************************************************************************
  * Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
  * program and the accompanying materials are made available under the terms of
  * the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Composent, Inc. - initial API and implementation
  ******************************************************************************/
 package org.eclipse.ecf.provider.jms.activemq.container;
 
 import javax.jms.ConnectionFactory;
 import javax.jms.JMSException;
 
 import org.apache.activemq.ActiveMQConnectionFactory;
 import org.eclipse.ecf.core.util.ECFException;
 import org.eclipse.ecf.provider.jms.container.AbstractJMSQueueConsumerContainer;
 import org.eclipse.ecf.provider.jms.identity.JMSID;
 
 public class ActiveMQJMSQueueConsumerContainer extends
 		AbstractJMSQueueConsumerContainer {
 
 	public ActiveMQJMSQueueConsumerContainer(JMSID containerID, JMSID queueID) {
 		super(containerID, queueID);
 	}
 
 	public ConnectionFactory getQueueConnectionFactory(String target,
 			Object[] jmsConfiguration) {
 		String username = null;
 		String password = null;
 		if (jmsConfiguration != null && jmsConfiguration.length > 0) {
 			username = (String) jmsConfiguration[0];
 			if (jmsConfiguration.length > 1) {
 				password = (String) jmsConfiguration[1];
 			}
 		}
 		return new ActiveMQConnectionFactory(
 				(username == null) ? ActiveMQConnectionFactory.DEFAULT_USER
 						: username,
 				(password == null) ? ActiveMQConnectionFactory.DEFAULT_PASSWORD
 						: password, target);
 	}
 
 	public void start() throws ECFException {
 		try {
 			setupJMSQueueConsumer(getQueueID());
 		} catch (JMSException e) {
 			throw new ECFException("Could not connect to queueID=" + queueID, e); //$NON-NLS-1$
 		}
 	}
 
 }
