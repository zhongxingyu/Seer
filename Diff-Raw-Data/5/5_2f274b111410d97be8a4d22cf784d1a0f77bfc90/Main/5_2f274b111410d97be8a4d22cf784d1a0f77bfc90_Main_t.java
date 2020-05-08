 package org.vika.routing;
 
 import jade.core.Profile;
 import jade.core.ProfileImpl;
 import jade.core.Runtime;
 import jade.tools.sniffer.Agent;
 import jade.tools.sniffer.Sniffer;
 import jade.util.ExtendedProperties;
 import jade.util.leap.ArrayList;
 import jade.util.leap.Properties;
 import jade.wrapper.AgentContainer;
 import jade.wrapper.ControllerException;
 import org.vika.routing.network.Network;
 import org.vika.routing.network.Node;
 import org.vika.routing.network.Parser;
 import org.vika.routing.network.jade.NodeAgent;
 import org.vika.routing.network.jade.TrafficAgent;
 
 import java.io.IOException;
 import java.util.List;
 
 /**
  * @author oleg
  */
 public class Main {
 
     private static final int TIME = 100;
     private static final int QUANTUM_TIME=50;
     private static final int NODE_LOAD_RANGE = 10;
     private static final int EDGE_LOAD_RANGE = 10;
     private static final int MESSAGES = 10;
 
     public static void main(String[] args) throws IOException, ControllerException, InterruptedException {
         // Create empty profile
         final Properties props = new ExtendedProperties();
         // props.setProperty(Profile.GUI, "true");
         final Profile p = new ProfileImpl(props);
         // Start a new JADE runtime system
         final AgentContainer container = Runtime.instance().createMainContainer(p);
 
         // Now we have successfully launched Agents platform
         final String fileName = "C:/work/routing/tests/org/vika/routing/network/network.txt";
         final Network network = Parser.parse(fileName);
         final Node[] nodes = network.nodes;
         final NodeAgent[] nodeAgents = new NodeAgent[nodes.length];
 
         // Generate random system load
         final LoadManager.Load load =
                 LoadManager.generate(TIME, nodes.length, network.edges, NODE_LOAD_RANGE, EDGE_LOAD_RANGE);
         final LoadManager loadManager = new LoadManager(load);
 
         // Create routing manager
         final RoutingManager routingManager = new RoutingManager(network, loadManager);
 
         final TimeManager timeManager = new TimeManager(TIME, QUANTUM_TIME);
         // Initiate traffic agent
         final List<TrafficManager.TrafficEvent> traffic = TrafficManager.generate(nodes.length, MESSAGES, TIME);
         final TrafficAgent trafficAgent = new TrafficAgent(nodeAgents, new TrafficManager(traffic), timeManager);
 
 
         // Create JADE backend
         // Register all the agents according to the network
         final ArrayList nodeAgentsList = new ArrayList(nodeAgents.length);
         for (int i = 0; i < nodes.length; i++) {
             final NodeAgent nodeAgent = new NodeAgent(i, nodeAgents, loadManager, routingManager);
             nodeAgents[i] = nodeAgent;
             nodeAgentsList.add(new Agent("agent" + i));
             container.acceptNewAgent("agent" + i, nodeAgent).start();
         }
 
         // SnifferAgent creating
         final Sniffer sniffer = new Sniffer();
         sniffer.sniffMsg(nodeAgentsList, Sniffer.SNIFF_ON);
        container.acceptNewAgent("sniffer", sniffer).start();

        // Start traffic
         container.acceptNewAgent("trafficAgent", trafficAgent).start();
 
         // Spin lock to kill the runtime on time limit
         while (timeManager.getTime() < TIME){
             Thread.sleep(1000);
         }
         Runtime.instance().shutDown();
     }
 
 }
