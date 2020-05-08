 package org.vika.routing.routing;
 
 import org.vika.routing.LoadManager;
 import org.vika.routing.Message;
 import org.vika.routing.Pair;
 import org.vika.routing.TimeLogManager;
 import org.vika.routing.network.Channel;
 import org.vika.routing.network.Network;
 import org.vika.routing.network.NeuroNetwork;
 import org.vika.routing.network.jade.NodeAgent;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author oleg
  */
 public class NeuroRoutingManager extends AbstractRoutingManager implements RoutingManager {
     private static final float DEFAULT_NODE_ACTIVATION = 0.6f;
     private final Network myNetwork;
     private final LoadManager myLoadManager;
     private final NeuroNetwork myNeuroNetwork;
     private final TimeLogManager myTimeManager;
 
     public NeuroRoutingManager(final Network network, final NeuroNetwork neuroNetwork, final LoadManager loadManager,
                                final TimeLogManager timeManager, final int totalMessages) {
         super(totalMessages);
         System.out.println("Neuro network based routing manager is used!");
         myNetwork = network;
         myNeuroNetwork = neuroNetwork;
         myLoadManager = loadManager;
         myTimeManager = timeManager;
     }
 
     public void route(final NodeAgent agent, final Message message) {
         myTimeManager.log("Request from " + agent.getId() + " to route " + message);
         final int currentTime = Math.round(myTimeManager.getCurrentTime());
         final int agentId = agent.getId();
         if (agentId == message.receiver) {
             myTimeManager.messageReceived(message);
             messageReceived(message);
             return;
         }
         final Map<Integer, Channel> adjacentNodes = myNetwork.nodes[agentId].adjacentNodes;
         // Send message to the adjacent node it receiver is one of them
         if (adjacentNodes.containsKey(message.receiver)) {
             // We should add non-blocking transmit message with given time
             final float channelTime = adjacentNodes.get(message.receiver).time;
             myTimeManager.log("Sending " +  message + " to " + message.receiver + " channel time " + channelTime);
             agent.sendMessageAfterDelay(message.receiver, message, channelTime);
             return;
         }
         final Map<Integer, Float> activationLevels = new HashMap<Integer, Float>();
         final Map<Pair<Integer,Integer>, Float> wValues = myNeuroNetwork.neuroNodes[agentId].wValues;
 
         while (true) {
             for (Map.Entry<Integer, Channel> entry : adjacentNodes.entrySet()) {
                 final int adjacentNodeId = entry.getKey();
                 final Channel channel = entry.getValue();
                 // Calculate activation levels
                 final Pair<Integer, Integer> key = new Pair<Integer, Integer>(adjacentNodeId, message.receiver);
                 final Float wValue = wValues.get(key);
                 final float channelLoad = myLoadManager.getEdgeLoad(channel.id, currentTime);
                 activationLevels.put(adjacentNodeId, wValue - channelLoad - DEFAULT_NODE_ACTIVATION);
             }
             // Once we are done with activation levels, we can choose maximum of them
             int maxId = -1;
             float maxActivationLevel = Float.MIN_VALUE;
             for (Map.Entry<Integer, Float> entry : activationLevels.entrySet()) {
                 final Float value = entry.getValue();
                if (value > maxActivationLevel){
                   maxActivationLevel = value;
                   maxId = entry.getKey();
                 }
             }
            if (maxActivationLevel < 0){
                 myTimeManager.log("All the activation levels are below zero, waiting...");
                 myTimeManager.sleep(1);
             } else {
                 // Ok we have maximum activation level id, send message there.
                 final float channelTime = adjacentNodes.get(maxId).time;
                 final float edgeLoad = myLoadManager.getEdgeLoad(adjacentNodes.get(maxId).id, currentTime);
                 final float deliveryTime = edgeLoad + channelTime;
                 myTimeManager.log("Sending " + message + " to " + maxId + " channel time " + deliveryTime);
                 agent.sendMessageAfterDelay(maxId, message, deliveryTime);
                 return;
             }
         }
     }
 }
