 package org.eclipse.ecf.example.chat.config;
 
import java.net.URI;
 import java.util.Dictionary;
 
 import org.eclipse.ecf.core.ContainerFactory;
 import org.eclipse.ecf.core.IContainer;
 import org.eclipse.ecf.provider.zookeeper.core.ZooDiscoveryContainerInstantiator;
 import org.osgi.service.cm.ConfigurationException;
 import org.osgi.service.cm.ManagedService;
 
 public class ZooDiscoveryExtender implements ManagedService {
 
 	private boolean init = false;
 	
	private void startZooKeeper(URI server) {
 		try {
 			final IContainer singleton = ContainerFactory.getDefault().createContainer(
 					ZooDiscoveryContainerInstantiator.NAME);
 			if (singleton.getConnectedID() != null) {
 				singleton.disconnect();
 			}
			String string = null;
			if (server.getPort() != -1) {
				string  = server.getHost() + ":" + server.getPort();
			} else {
				string = server.getHost() + ":2181";
			}
 			singleton.connect(
 					singleton.getConnectNamespace().createInstance(
							new String[] { "zoodiscovery.flavor.centralized=" + string }), null);
 		} catch (Exception doesNotHappen) {
 			doesNotHappen.printStackTrace();
 		}
 	}
 
 	@Override
 	public synchronized void updated(Dictionary<String, ?> properties)
 			throws ConfigurationException {
 		if (properties != null && !init) {
 			init = true;
			URI server = URI.create((String) properties.get("SERVER"));
 			startZooKeeper(server);
 		}
 	}
 }
