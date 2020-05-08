 package org.eclipse.ecf.tests.provider.jms.activemq;
 
 import org.eclipse.ecf.core.ContainerFactory;
 import org.eclipse.ecf.core.IContainer;
 import org.eclipse.ecf.core.identity.ID;
 import org.eclipse.ecf.core.identity.IDFactory;
 import org.eclipse.ecf.tests.provider.jms.JMSContainerAbstractTestCase;
 
 public class ActiveMQJMSContainerTest extends JMSContainerAbstractTestCase {
 
	@Override
 	protected String getClientContainerName() {
 		return ActiveMQ.CLIENT_CONTAINER_NAME;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ecf.tests.provider.jms.JMSContainerAbstractTestCase#getServerContainerName()
 	 */
	@Override
 	protected String getServerContainerName() {
 		return ActiveMQ.SERVER_CONTAINER_NAME;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ecf.tests.provider.jms.JMSContainerAbstractTestCase#getServerIdentity()
 	 */
	@Override
 	protected String getServerIdentity() {
 		return ActiveMQ.TARGET_NAME;
 	}
 
 	protected IContainer createServer() throws Exception {
 		return ContainerFactory.getDefault().createContainer(
 				getServerContainerName(), new Object[] { getServerIdentity() });
 	}
 
 	public void testConnectClient() throws Exception {
 		IContainer client = getClients()[0];
 		ID targetID = IDFactory.getDefault().createID(client.getConnectNamespace(),new Object [] { getServerIdentity() });
 		client.connect(targetID, null);
 		Thread.sleep(3000);
 	}
 
 }
