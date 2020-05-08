 package amp.topology.client;
 
import java.util.List;
 import java.util.Map;
 
 import amp.messaging.EnvelopeHeaderConstants;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import amp.rabbit.topology.BaseRoute;
 import amp.rabbit.topology.ITopologyService;
 import amp.rabbit.topology.RoutingInfo;
 
 
 /**
  * An implementation of ITopologyService that utilizes a central
  * configuration and management system for topology, called
  * the "Global Topology Service".  This implementation relies on 
  * an external source to provide routing info.  If the routing info
  * is not found, the Service will default to a "fallback" configuration.
  * 
  * @author Richard Clayton (Berico Technologies)
  */
 public class GlobalTopologyService implements ITopologyService {
 
 	private static final Logger LOG = LoggerFactory.getLogger(GlobalTopologyService.class);
 
 	private IRoutingInfoRetriever routingInfoRetriever;
 	private FallbackRoutingInfoProvider fallbackProvider = null;
 
 
 	public GlobalTopologyService(IRoutingInfoRetriever routingInfoRetriever) {
 
         this.initialize(routingInfoRetriever, null);
 	}
 
 	public GlobalTopologyService(
 		IRoutingInfoRetriever routingInfoRetriever,
 		FallbackRoutingInfoProvider fallbackProvider){
 
         this.initialize(routingInfoRetriever, fallbackProvider);
 	}
 
 
     public void initialize(
             IRoutingInfoRetriever routingInfoRetriever,
             FallbackRoutingInfoProvider fallbackProvider) {
 
         this.routingInfoRetriever = routingInfoRetriever;
         this.fallbackProvider = fallbackProvider;
     }
 
 	@Override
 	public RoutingInfo getRoutingInfo(Map<String, String> routingHints) {
 		
 		String topic = routingHints.get(EnvelopeHeaderConstants.MESSAGE_TOPIC);
		LOG.debug("Getting routing info for hints: {}", routingHints);	
		LOG.info("   >> Topic (in hints) to get routing info for: {}", topic);
 		
 		RoutingInfo routingInfo = this.routingInfoRetriever.retrieveRoutingInfo(topic);
 
         if (routingInfoAbsentOrNotValid(routingInfo)
                 && this.fallbackProvider != null) {
 
            routingInfo = this.fallbackProvider.getFallbackRoute(routingHints);
 
             LOG.info("Falling back to routing info: {}", routingInfo.toString());
         }
 
         // If the RoutingInfo is still null, end this program's life!
         if (routingInfo == null) {
 
             throw new RoutingInfoNotFoundException(topic);
         }
 		
 		return routingInfo;
 	}
 
 	@Override
 	public void dispose() {}
 
 
 
     /**
      * Determine if the routing info is absent or invalid (i.e. no routes).
      * @param routingInfo RoutingInfo returned from Retreiver.
      * @return true is Absent or Invalid.
      */
     protected boolean routingInfoAbsentOrNotValid(RoutingInfo routingInfo){
 
         LOG.debug("Determining if Routing Info is absent or invalid.");
 
         if (routingInfo != null){
 
             LOG.debug("Routing info is not null.");
 
            List<BaseRoute> baseRoutes = routingInfo.getAllRoutes();
            if ( baseRoutes!= null){
 
                 LOG.debug("Routes are not null.");
 
                if (baseRoutes.iterator().hasNext()){
 
                    LOG.debug("Routes are not empty.");
                     return false;
                 }
             }
         }
 
         return true;
     }
 }
