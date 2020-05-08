 package agents;
 import agents.include.*;
 import agents.interfaces.*;
 import java.util.*;
 
 import state.*;
 
 import java.util.concurrent.Semaphore; 
 import state.transducers.*;
 
 public class PartRobotAgent extends Agent implements PartRobot, TReceiver {
 
 	/* v0 Design
 	 * 
 	 * The PartRobot will process kits in batches.
 	 * It will not take any kits until the current kits are done and send to the KitRobot
 	 * 
 	 * Additionally, the PartRobot will only be alerted by a nest, until all of the parts
 	 * requested are there
 	 * 
 	 * Note: Agents don't care about which nest agent is what...just the type of them
 	 * 
 	 * Some v1 features have been implemented, but seemed appropriate to code now
 	 * 
 	 */
 	
     /*** Data Structures **/
     private String name; // needed for agents name (nice print msgs)
     
     public boolean needSecond; // flag for when we need another iteration (and flush parts)
     private int completeKits; // # of completedkits
 
     public List<Nest> nests; // lists of the nests
     public List<Map<String, Integer>> configs; // list of the current configs to fulfill
     public List<Part> received; // list of parts received from nests
     private KitStand kitStand; // shared data
 
     /* Has states, but not a state machine...
      * and we're usually sitting in the waitingRequests state 
      * waitng for parts to be fulfilled */
     public enum PartRobotState { inactive, startOrder, waitingRequests, none};
     public PartRobotState state;
 
     private Transducer transducer;
     
     /*** Constructor **/
 
     // factoryState only for v0!
     public PartRobotAgent(String name, List<Nest> nestAgents, KitStand kitStand, Transducer transducer) {
         this.name = name;
     	this.nests = nestAgents;
         this.kitStand = kitStand;
         this.needSecond = false;
         this.completeKits = 0;
         this.received = new ArrayList<Part>();
         this.transducer = transducer;
         transducer.register(this, TChannel.Agents);
         state = PartRobotState.inactive;
     }
     
     //constructor for part A (kitStand needs partsRobot to message to)
     public PartRobotAgent(String name, List<Nest> nests, KitStand kitStand) {
         this.name = name;
     	this.nests = nests;
         this.kitStand = kitStand;
         //this.factoryState = factoryState;
         this.needSecond = false;
         this.completeKits = 0;
         this.received = new ArrayList<Part>();
         state = PartRobotState.inactive;
 
     }
 
     /*** Messages / Public API ***/
 
     /*  (KitRobot) Message to ask PartRobot to create a new kit */
     public void msgMakeKits() {
         configs = kitStand.getPartConfig();
         state = PartRobotState.startOrder;
     	print("Received message to start making kits");
 
         stateChanged();
     }
 
     /*  (NestAgent) Message passing alot parts to PartRobot */
     public void msgHereAreParts(List<Part> sendParts, Nest n) {
         received.addAll(sendParts);
         //print("Processing " + sendParts.size() + " of " + n.getPartType() + " from " + n.getName());
         stateChanged();
     }
 
     /* (GUI) Message once picked up parts animation is done */
     public void DoPickedUpParts()
     {
     	print("Parts placed in kits");
         verify.release();
     }
     
     /* Getter for name */
     public String getName() {
         return name;
     }        
     
     /*** Scheduler ***/
 
     public boolean pickAndExecuteAnAction() 
     {
     	// received start msg -> request parts
         if(state == PartRobotState.startOrder)
         {
             requestParts();
             state = PartRobotState.waitingRequests;
             return true;
         }
 
         // if received some parts pick them up!
         if(state == PartRobotState.waitingRequests)
         {
             if(received.size() > 0)
             {
                 pickUpParts();
                 return true;
             }
         }
 
         return false;
     }
 
     /*** Actions **/
 
     /* Request Parts
      * -> requests the number of parts it needs for its current configs
      * it'll send requests to flush and send info to the nests
      * 
      * also manages whether we need a second iteration or not to get the 2nd kit
      */
     private void requestParts() {
 
         Map<String, Integer> use = configs.remove(0);
 
         // if we have a 2nd kit left
         if(configs.size() > 0)
         {
             Map<String, Integer> total = new HashMap<String, Integer>();
 
             // for types in 2nd kit
             for(String key : configs.get(0).keySet())
             {
                 int val = configs.get(0).get(key);
                 if(use.containsKey(key))
                 {
                     val += use.get(key);
                 }
                 total.put(key, val);
             }
 
             // for types not in 2nd kit
             for(String key : use.keySet())
             {
             	if(!total.containsKey(key))
             	{
             		total.put(key,use.get(key));
             	}
             }
             
             // if we can process with given lanes
             if(total.keySet().size()<=nests.size()) 
             {
                 configs.remove(0);
                 use = total;
             }
             else 
             {
             	print("Need second iteration for current configs");
             	needSecond = true;
             }
         }
 
         List<String> needLane = new ArrayList<String>();
         List<Nest> needNests = new ArrayList<Nest>();
         for(Nest n : nests) needNests.add(n);
 
         // figuring out which nests need to be reconfigured
         for(String key : use.keySet())
         {
             boolean need = true;
             for(Nest n : nests)
             {
                 if(n.getPartType().equals(key))
                 {
                     n.msgRequestParts(key, use.get(key));
                     needNests.remove(n);
                     need = false;
                     break;
                 }
             }
             if(need) needLane.add(key);
         }
 
         // flush nests if needed!
         for(String n : needLane)
         {
             needNests.remove(0).msgRequestParts(n, use.get(n));
         }
         
         // print nest configuration
         /*
         for(int i=0; i<nests.size(); i++)
         {
             if(nests.get(i).getPartType() != null)
             {
                 print("Nest " + i + " assgined with " + nests.get(i).getPartType());
             }
         }*/
 
         stateChanged();
     }
 
     /* Pick Up Parts
      * -> pick up parts, add to kit, call gui pickup
      * and check for complete kits
      */
     private void pickUpParts()
     {
     	// create list of parts to actually pickup
         List<Part> partsToKit = new ArrayList<Part>();
         int j = 0;
         for(int i = 0; i< received.size(); i++)
         {
             partsToKit.add(received.get(i));
             j++;
             if(j>=4) break;             
         }
         
         // remove from "alerted" list
         for(int i=0; i<j; i++)
         {
         	received.remove(0);
         }
         
         // insert parts into kitstand and get Map of parts we need
         // this map has a mapping of part to its kitstand location
         Map<Part, Integer> partsSend = kitStand.insertPartsIntoKits(partsToKit);
 
         // call gui
         DoPickUpPartsFromNest(partsSend);
 
         // check for complete kits
         int kitCompleteKits = kitStand.completeKits();
         if(kitCompleteKits != completeKits)
         {
             completeKits = kitCompleteKits;
             print("Done with a kit");
             //kitRobot.msgKitIsDone();
 
             if(needSecond)
             {
                 needSecond = false;
                 requestParts();
                 state = PartRobotState.waitingRequests;
             }
             else if(kitCompleteKits == 2)
             {
                 state = PartRobotState.inactive;
                 print("Done with all current kits");
 
                 // done for now
                 completeKits = 0;
                 received = new ArrayList<Part>();
             }
         } 
 
         
         // v0 stub
         /*Map<Part, Integer> partsSend = new HashMap<Part, Integer>();
         for(Part p : partsToKit)
         {
             partsSend.put(p, 1);
         }
 
         DoPickUpPartsFromNest(partsSend);*/
         
         stateChanged();
 
     }
 
     public Semaphore verify = new Semaphore(0);
     private void DoPickUpPartsFromNest(Map<Part, Integer> partsSend)
     {
     	
         for(Part key : partsSend.keySet())
         {
         	print("Picking up " + key.getPartName() + " into Kit " + partsSend.get(key));
         }
     	
         Object[] temp = new Object[1];
         temp[0] = partsSend;
         transducer.fireEvent(TChannel.GUI,TEvent.SendPart,temp);
        //factoryState.doPickUpParts(partsSend);
         try {
             verify.acquire();
         }
         catch(Exception ex) { }
 
         // released by DoPickedUpParts once animation is done
     }
     
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args)
 	{
 		
 		if(channel == TChannel.Agents)
 		{
 			if(event == TEvent.DonePickingUpParts)
 			{
 				DoPickedUpParts();
 			}
 		}
 	}
 }
