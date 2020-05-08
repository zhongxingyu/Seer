 /*******************************************************************************
  * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
  * program and the accompanying materials are made available under the terms of
  * the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Composent, Inc. - initial API and implementation
  ******************************************************************************/
 package org.eclipse.ecf.internal.provider.jms.activemq;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.ecf.core.ContainerCreateException;
 import org.eclipse.ecf.core.ContainerTypeDescription;
 import org.eclipse.ecf.core.IContainer;
 import org.eclipse.ecf.core.identity.IDFactory;
 import org.eclipse.ecf.provider.generic.GenericContainerInstantiator;
 import org.eclipse.ecf.provider.jms.activemq.container.ActiveMQJMSServerContainer;
 import org.eclipse.ecf.provider.jms.container.JMSContainerConfig;
 import org.eclipse.ecf.provider.jms.identity.JMSID;
 import org.eclipse.ecf.provider.jms.identity.JMSNamespace;
 
 public class ActiveMQJMSServerContainerInstantiator extends
 		GenericContainerInstantiator {
 
 	protected static final String [] jmsIntents = { "JMS" };
 	
 	public ActiveMQJMSServerContainerInstantiator() {
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ecf.core.provider.IContainerInstantiator#createInstance(org.eclipse.ecf.core.ContainerTypeDescription,
 	 *      java.lang.Object[])
 	 */
 	public IContainer createInstance(ContainerTypeDescription description,
 			Object[] args) throws ContainerCreateException {
 		try {
 			Integer ka = new Integer(
 					ActiveMQJMSServerContainer.DEFAULT_KEEPALIVE);
 			String name = null;
 			if (args.length == 0)
 				throw new ContainerCreateException(
 						"no server id provided for creation");
 			name = (String) args[0];
 			JMSID serverID = (JMSID) IDFactory.getDefault().createID(
 					JMSNamespace.NAME, name);
 			if (args.length > 1)
 				ka = getIntegerFromArg(args[1]);
 			if (ka == null)
 				ka = new Integer(ActiveMQJMSServerContainer.DEFAULT_KEEPALIVE);
 			ActiveMQJMSServerContainer server = new ActiveMQJMSServerContainer(new JMSContainerConfig(
 					serverID, ka.intValue(), null));
 			server.start();
 			return server;
 		} catch (Exception e) {
 			throw new ContainerCreateException(
					"Exception creating weblogic server container", e);
 		}
 	}
 	
 	public String[] getSupportedIntents(ContainerTypeDescription description) {
 		List results = new ArrayList();
 		for(int i=0; i < genericProviderIntents.length; i++) {
 			results.add(genericProviderIntents[i]);
 		}
 		for(int i=0; i < jmsIntents.length; i++) {
 			results.add(jmsIntents[i]);
 		}
 		return (String []) results.toArray(new String[] {});
 	}
 
 }
