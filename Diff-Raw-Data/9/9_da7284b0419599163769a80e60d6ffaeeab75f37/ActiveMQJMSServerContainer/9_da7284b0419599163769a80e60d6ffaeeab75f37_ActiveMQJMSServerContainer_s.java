 /*******************************************************************************
  * Copyright (c) 2004, 2007 Composent, Inc. and others. All rights reserved. This
  * program and the accompanying materials are made available under the terms of
  * the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Composent, Inc. - initial API and implementation
  ******************************************************************************/
 package org.eclipse.ecf.provider.jms.activemq.container;
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 
 import javax.jms.ConnectionFactory;
 
 import org.activemq.ActiveMQConnectionFactory;
 import org.eclipse.ecf.core.util.ECFException;
 import org.eclipse.ecf.provider.comm.ISynchAsynchConnection;
 import org.eclipse.ecf.provider.jms.channel.AbstractJMSServerChannel;
 import org.eclipse.ecf.provider.jms.container.AbstractJMSServer;
 import org.eclipse.ecf.provider.jms.container.JMSContainerConfig;
 import org.eclipse.ecf.provider.jms.identity.JMSID;
 
 public class ActiveMQJMSServerContainer extends AbstractJMSServer {
 
 	public static final String PASSWORD_PROPERTY = "password";
 	public static final String USERNAME_PROPERTY = "username";
 	public static final String DEFAULT_PASSWORD = "defaultPassword";
 	public static final String DEFAULT_USERNAME = "defaultUsername";
 
 	public ActiveMQJMSServerContainer(JMSContainerConfig config) {
 		super(config);
 	}
 
 	class ActiveMQServerChannel extends AbstractJMSServerChannel {
 
 		private static final long serialVersionUID = -2348383004973299553L;
 
 		/**
 		 * @param handler
 		 * @param keepAlive
 		 * @throws IOException
 		 * @throws URISyntaxException
 		 */
 		public ActiveMQServerChannel() throws ECFException {
 			super(getReceiver(), getJMSContainerConfig().getKeepAlive());
 		}
 
 		protected ConnectionFactory createJMSConnectionFactory(JMSID targetID)
 				throws IOException {
 			return new ActiveMQConnectionFactory(getActiveMQUsername(targetID),
 					getActiveMQPassword(targetID), targetID.getName());
 		}
 
 		private String getActiveMQPassword(JMSID targetID) {
 			String pw = (String) getJMSContainerConfig().getProperties().get(
 					PASSWORD_PROPERTY);
 			return (pw == null) ? "defaultPassword" : pw;
 		}
 
 		private String getActiveMQUsername(JMSID targetID) {
 			String username = (String) getJMSContainerConfig().getProperties()
 					.get(USERNAME_PROPERTY);
 			return (username == null) ? "defaultUsername" : username;
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.provider.jms.container.AbstractJMSServer#start()
 	 */
 	public void start() throws ECFException {
 		try {
 			ISynchAsynchConnection connection = new ActiveMQServerChannel();
 			setConnection(connection);
 			connection.start();
 		} catch (Exception e) {
			throw new ECFException("exception starting server", e);
 		}
 	}
 
 	public void dispose() {
 		super.dispose();
 		getConnection().disconnect();
 		setConnection(null);
 	}
 
 }
