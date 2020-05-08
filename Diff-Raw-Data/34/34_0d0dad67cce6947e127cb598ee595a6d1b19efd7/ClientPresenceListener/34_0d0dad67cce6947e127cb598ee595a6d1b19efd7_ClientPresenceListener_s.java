 package me.itzgeoff.vidsync.server;
 
 import javax.annotation.PostConstruct;
 
 import me.itzgeoff.vidsync.common.ServiceDescription;
 import me.itzgeoff.vidsync.common.ServiceDiscovery.ServiceInstance;
 import me.itzgeoff.vidsync.common.ServiceListener;
 import me.itzgeoff.vidsync.common.VidSyncConstants;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.core.task.TaskExecutor;
 import org.springframework.stereotype.Component;
 
import com.yammer.metrics.Counter;
import com.yammer.metrics.MetricRegistry;
 
 @Component
 @ServiceDescription(names=VidSyncConstants.MDNS_NAME_VIDSYNC_CLIENT)
 public class ClientPresenceListener implements ServiceListener {
 	
 	private static final Logger logger = LoggerFactory.getLogger(ClientPresenceListener.class);
 	
 	@Autowired
 	private TaskExecutor taskExecutor;
 	
 	@Autowired
 	private ServerViewOfClientManager clientManager;
 	
 	@Autowired
 	private ClientDistributor distributor;
 	
     @Autowired
     private MetricRegistry metrics;
 
     private Counter counterClientsAdded;
 
     private Counter counterClientsConnected;
 
 	@PostConstruct
 	public void init() {
 	    counterClientsAdded = metrics.counter(MetricRegistry.name(ClientPresenceListener.class, "clients-added-total"));
 	    counterClientsConnected = metrics.counter(MetricRegistry.name(ClientPresenceListener.class, "clients-connected-current"));
 	}
 
     /* (non-Javadoc)
      * @see me.itzgeoff.vidsync.common.ServiceListener#serviceAdded(me.itzgeoff.vidsync.common.ServiceDiscovery.ServiceInstance)
      */
     @Override
     public void serviceAdded(ServiceInstance service) {
         logger.debug("Resolved {}", service);
         counterClientsAdded.inc();
         counterClientsConnected.inc();
         
         ServerViewOfClientInstance viewOfClient = clientManager.createViewOfClient(service);
         
         distributor.addClient(viewOfClient);
 
     }
 
     /* (non-Javadoc)
      * @see me.itzgeoff.vidsync.common.ServiceListener#serviceRemoved(me.itzgeoff.vidsync.common.ServiceDiscovery.ServiceInstance)
      */
     @Override
     public void serviceRemoved(ServiceInstance service) {
         logger.debug("Saw removal of {}", service);
         counterClientsConnected.dec();
 
         distributor.removeClient(service);
     }
 
     /* (non-Javadoc)
      * @see me.itzgeoff.vidsync.common.ServiceListener#serviceTimedOut(me.itzgeoff.vidsync.common.ServiceDiscovery.ServiceInstance)
      */
     @Override
     public void serviceTimedOut(ServiceInstance service) {
         logger.debug("Saw timeout of {}", service);
         counterClientsConnected.dec();
 
         distributor.removeClient(service);
     }
 
 }
