 package org.vika.routing.network.jade;
 
 import jade.core.Agent;
 import jade.core.behaviours.OneShotBehaviour;
 import org.vika.routing.Message;
 import org.vika.routing.TimeManager;
 import org.vika.routing.TrafficManager;
 
 /**
  * @author oleg
  */
 public class TrafficAgent extends Agent {
     private final NodeAgent[] myAgents;
     private TrafficManager myTrafficManager;
     private final TimeManager myTimeManager;
 
     public TrafficAgent(final NodeAgent[] agents, final TrafficManager trafficManager, final TimeManager timeManager) {
         myAgents = agents;
         myTrafficManager = trafficManager;
         myTimeManager = timeManager;
     }
 
     public void setup() {
         System.out.println("Traffic manager starting traffic.");
         addBehaviour(new OneShotBehaviour() {
             public void action() {
                 while (!myTrafficManager.end()){
                     final int delay = (int) myTrafficManager.getDelay();
                     final int initialAgent = myTrafficManager.getInitialAgent();
                     final Message message = myTrafficManager.getMessage();
                     // Setup initial time
                     message.time = myTimeManager.getCurrentTime();
                     AgentsUtil.sendMessage(myAgents, initialAgent, message);
                     // Wait for the delay number of quantum time
                    try {
                        Thread.sleep(delay * myTimeManager.getQuantumTime());
                    } catch (InterruptedException e) {
                        // Ignore, we should never face with
                    }
                     myTrafficManager.nextMessage();
                 }
                 System.out.println("Traffic manager is done.");
                 doDelete();
             }
         });
         // Start Time manager
         myTimeManager.start();
     }
 }
