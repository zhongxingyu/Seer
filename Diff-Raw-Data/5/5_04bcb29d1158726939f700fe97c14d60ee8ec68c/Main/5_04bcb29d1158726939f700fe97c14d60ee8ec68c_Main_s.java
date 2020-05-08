 package org.vika.routing;
 
 import jade.core.*;
 import jade.core.Runtime;
 import jade.domain.AMSService;
 import jade.util.ExtendedProperties;
 import jade.util.leap.Properties;
 import jade.wrapper.*;
 import jade.wrapper.AgentContainer;
 import org.vika.routing.network.Network;
 import org.vika.routing.network.Node;
 import org.vika.routing.network.NodeAgent;
 import org.vika.routing.network.Parser;
 
 import java.io.*;
 
 /**
  * @author oleg
  */
 public class Main {
     public static void main(String[] args) throws IOException, StaleProxyException {
         // Create empty profile
         final Properties props = new ExtendedProperties();
         props.setProperty(Profile.GUI, "true");
         final Profile p = new ProfileImpl(props);
         // Start a new JADE runtime system
        final AgentContainer container = Runtime.instance().createAgentContainer(p);
        // Now we have successfully launched Agents platform
 
         final String fileName = "C:/work/routing/tests/org/vika/routing/network/network.txt";
         final Node[] nodes = Parser.parse(fileName);
         final Network network = new Network(nodes);
         final NodeAgent[] nodeAgents = new NodeAgent[nodes.length];
         final LoadManager loadManager = new LoadManager();
         final RoutingManager routingManager = new RoutingManager(network, loadManager);
         // Register all the agents according to the network
         for (int i = 0; i < nodes.length; i++) {
             final NodeAgent nodeAgent = new NodeAgent(i, nodeAgents, loadManager, routingManager);
             nodeAgents[i] = nodeAgent;
             // Register agents
             container.acceptNewAgent("agent" + i, nodeAgent);
         }
     }
 
 }
