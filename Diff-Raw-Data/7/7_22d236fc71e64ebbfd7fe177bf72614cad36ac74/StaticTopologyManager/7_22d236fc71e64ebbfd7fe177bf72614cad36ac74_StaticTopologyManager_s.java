 package pegasus.eventbus.topology;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pegasus.eventbus.amqp.RoutingInfo;
 import pegasus.eventbus.amqp.TopologyManager;
 import pegasus.eventbus.topology.event.RegisterClient;
 import pegasus.eventbus.topology.event.TopologyUpdate;
 import pegasus.eventbus.topology.event.UnregisterClient;
 
 import pegasus.eventbus.client.EventManager;
 
 
 
 public class StaticTopologyManager implements TopologyManager {
 
     protected static final Logger      LOG                       = LoggerFactory.getLogger(StaticTopologyManager.class);
     private static final String        DEFAULT_TOPOLOGY_EXCHANGE = "topology";
 
     private String                     topologyExchange          = DEFAULT_TOPOLOGY_EXCHANGE;
     private Map<String, RoutingInfo>   topologyEventRegistry     = new HashMap<String, RoutingInfo>();
     private Map<String, RoutingInfo[]> topologyEventSetRegistry  = new HashMap<String, RoutingInfo[]>();
 
     public StaticTopologyManager() {
         this.topologyExchange = DEFAULT_TOPOLOGY_EXCHANGE;
         initializeTopologyRegistries();
     }
 
     public StaticTopologyManager(String topologyExchange) {
         this.topologyExchange = topologyExchange;
         initializeTopologyRegistries();
     }
 
     @Override
     public void start(EventManager eventManager) {
      // do nothing - static topology manager doesn't care
     }
 
     @Override
     public void close() {
         // do nothing - static topology manager doesn't care
     }
 
     private void initializeTopologyRegistries() {
     	registerType(RegisterClient.class);
     	registerType(UnregisterClient.class);
    	registerType(TopologyUpdate.class);
        
         //TODO: This should not be here.  All named routes need to be configured.  At the very least this one would need to be "all-topology."
         RoutingInfo[] allRoutes = { new RoutingInfo(topologyExchange, RoutingInfo.ExchangeType.Topic, true, "#") };
         topologyEventSetRegistry.put("ALL", allRoutes);
     }
 
     private void registerType(Class<?> eventType){
        String topic = RegisterClient.class.getCanonicalName();
         RoutingInfo route = new RoutingInfo(topologyExchange, RoutingInfo.ExchangeType.Topic, true, topic);
         topologyEventRegistry.put(topic, route);
     }
     
     @Override
     public RoutingInfo getRoutingInfoForEvent(Class<?> eventType) {
 
         LOG.info("Looking for route for event type [{}] in static topology mapper.", eventType.getCanonicalName());
 
         RoutingInfo route = null;
         String topic = eventType.getCanonicalName();
         if (topologyEventRegistry.containsKey(topic)) {
             route = topologyEventRegistry.get(topic);
 
             LOG.info("Found route [{}] in static topology mapper.", route);
         }
         return route;
     }
 
     @Override
     public RoutingInfo[] getRoutingInfoForNamedEventSet(String eventSetName) {
 
         LOG.info("Looking for routes for event set name [{}] in static topology mapper.", eventSetName);
 
         RoutingInfo[] routes = null;
         if (topologyEventSetRegistry.containsKey(eventSetName)) {
             routes = topologyEventSetRegistry.get(eventSetName);
 
             LOG.info("Found routes [{}] in static topology mapper.", routes);
         }
         return routes;
     }
 
 }
