 package org.eclipse.ecf.example.chat.config;
 
 import java.util.Dictionary;
 
 import org.eclipse.ecf.core.ContainerFactory;
 import org.eclipse.ecf.core.IContainer;
 import org.eclipse.ecf.provider.zookeeper.core.ZooDiscoveryContainerInstantiator;
 import org.osgi.service.cm.ConfigurationException;
 import org.osgi.service.cm.ManagedService;
 
 public class ZooDiscoveryExtender implements ManagedService {
 
 	private boolean init = false;
 	
	private void startZooKeeper(String server) {
 		try {
 			final IContainer singleton = ContainerFactory.getDefault().createContainer(
 					ZooDiscoveryContainerInstantiator.NAME);
 			if (singleton.getConnectedID() != null) {
 				singleton.disconnect();
 			}
 			singleton.connect(
 					singleton.getConnectNamespace().createInstance(
							new String[] { "zoodiscovery.flavor.centralized=" + server }), null);
 		} catch (Exception doesNotHappen) {
 			doesNotHappen.printStackTrace();
 		}
 	}
 
 	@Override
 	public synchronized void updated(Dictionary<String, ?> properties)
 			throws ConfigurationException {
 		if (properties != null && !init) {
 			init = true;
			String server = (String) properties.get("SERVER");
 			startZooKeeper(server);
 		}
 	}
 }
