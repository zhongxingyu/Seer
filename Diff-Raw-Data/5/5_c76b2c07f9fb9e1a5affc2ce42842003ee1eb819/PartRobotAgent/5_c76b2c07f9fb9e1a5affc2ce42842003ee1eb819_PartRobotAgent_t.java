 package agents;
 import agents.include.*;
 import agents.interfaces.*;
 import java.util.*;
 
 import state.*;
 
 import java.util.concurrent.Semaphore; 
 
 public class PartRobotAgent extends Agent implements PartRobot {
 
 	/* Part Robot
 	 * 
 	 * The PartRobot will process kits in batches.
 	 * It will not take any kits until the current kits are done and send to the KitRobot
 	 * 
 	 * Additionally, the PartRobot will only be alerted by a nest until all of the parts
 	 * requested are there
 	 * 
 	 * Note: Agents don't care about which nest agent is what...just the type of them
 	 * 	 
 	 */
 	
     /*** Data Structures **/
     private String name; // needed for agents name (nice print msgs)
     public String getName() { return name; }   
 
     public boolean needSecond; // flag for when we need another iteration (and flush parts)
     public int completeKits; // # of completedkits
 
     public List<Nest> nests; // lists of the nests
     public List<Map<String, Integer>> configs; // list of the current configs to fulfill
     public List<Map<String, Integer>> needFix;
     public List<Integer> needFixKitNum;
     public List<Part> received; // list of parts received from nests
     public List<NestAgent> photoBombs;
     private KitStand kitStand; // shared data
     private KitRobot kitRobot;
     public void setKitRobot(KitRobot kitRobot) { this.kitRobot = kitRobot; }    
     
     /* Has states, but not a state machine...
      * and we're usually sitting in the waitingRequests state 
      * waiting for parts to be fulfilled */
     public enum PartRobotState { inactive, startOrder, waitingRequests, none, error};
     public PartRobotState state;
     private FactoryState factoryState;
     
     /*** Constructor **/
 
     public PartRobotAgent(String name, List<Nest> nestAgents, KitStand kitStand, FactoryState factoryState) {
         this.name = name;
     	this.nests = nestAgents;
         this.kitStand = kitStand;
         this.needSecond = false;
         this.completeKits = 0;
         this.received = new ArrayList<Part>();
         this.needFix = new ArrayList<Map<String, Integer>>();
         this.needFixKitNum = new ArrayList<Integer>();
         this.factoryState = factoryState;
         this.photoBombs = new ArrayList<NestAgent>();
         state = PartRobotState.inactive;
     }
     
     // constructor for part A (kitStand needs partsRobot to message to)
     public PartRobotAgent(String name, List<Nest> nests, KitStand kitStand) {
         this.name = name;
     	this.nests = nests;
         this.kitStand = kitStand;
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
         FactoryState.out.println("PartsRobot received message to start making kits");
         stateChanged();
     }
 
     /*  (Kit Robot) Message to tell PartRobot that kit needs fixing (non-normative) */
     public void msgFixKit(int num, Map<String, Integer> newFix)
     {
     	needFix.add(newFix);
     	needFixKitNum.add(num);
     	print("Received order to fix kits");
     	FactoryState.out.println("PartsRobot received message to fix kit");
     	stateChanged();
     }
     
     /*  (NestAgent) Message passing a lot parts to PartRobot -> all needed */
     public void msgHereAreParts(List<Part> sendParts, Nest n) {
         received.addAll(sendParts);
         stateChanged();
     }
 
     /* (GUI) Message once picked up parts animation is done */
     public void DoPickedUpParts()
     {
     	print("Parts placed in kits");
         verify.release();
     }
     
     /*  (NestAgent) Message to photobomb (non-normative) */
     public void msgPhotoBomb(NestAgent n)
     {
     	print("Photobomb!");
     	photoBombs.add(n);
     	stateChanged();
     }
     
     /*** Scheduler ***/
 
     public boolean pickAndExecuteAnAction() 
     {
     	// received start msg -> request parts
         if(state == PartRobotState.startOrder)
         {
             requestParts();
             return true;
         }
 
         // if received some parts pick them up!
         if(state == PartRobotState.waitingRequests)
         {
 
         	if(photoBombs.size() > 0)
         	{
         		photoBomb(photoBombs.remove(0));
         		return true;
         	}
         	
             if(received.size() > 0)
             {
                 pickUpParts();
                 return true;
             }
         }
         
         // if need fixing, fix kits by requesting parts
         if(state == PartRobotState.inactive && needFix.size()>0)
         {
         	configs.add(needFix.remove(0));
         	int num = needFixKitNum.remove(0);
         	kitStand.setKitGood(num);
         	print("Fixing the kit now at position " + num);
         	requestParts();
         	return true;
         }
         
         // error state
         if(state == PartRobotState.error)
         {
         	System.out.println("Error");
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
 
         // check for valid config
         
         int numPartsZero = 0;
         for(String key : use.keySet())
         {
         	int temp = use.get(key);
         	numPartsZero += temp;
         }
         
         if(numPartsZero > 8)
         {
             state = PartRobotState.error;
             System.out.println("Invalid Config");
         	stateChanged();
         	return;
         }
         
         if(configs.size() > 0)
         {
             int numPartsOne = 0;
             for(String key : configs.get(0).keySet())
             {
             	int temp = configs.get(0).get(key);
             	numPartsOne += temp;
             }
             
             if(numPartsOne > 8)
             {
                 state = PartRobotState.error;
                 System.out.println("Invalid Config");
             	stateChanged();
             	return;
             } 
         }
         
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
             
         	if(!needSecond)
         	{
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
 
         state = PartRobotState.waitingRequests;
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
         
         completeKits = kitStand.updateCompleteKits();
         Map<Part, Integer> partsSend = kitStand.insertPartsIntoKits(partsToKit);
   
         // call gui
         DoPickUpPartsFromNest(partsSend);
 
         // check for complete kits
         int kitCompleteKits = kitStand.updateCompleteKits();
 
         if(kitCompleteKits != completeKits)
         {
        	if(kitCompleteKits == 2 && completeKits == 0)
        	{
                if(kitRobot!=null) kitRobot.msgKitIsDone();
        	}
        	
             completeKits = kitCompleteKits;
             print("Done with a kit");
 
             if(needSecond)
             {
                 needSecond = false;
                 requestParts();
             }
             else if(kitCompleteKits == 2)
             {
                 state = PartRobotState.inactive;
                 if(needFix.size() == 0) print("Done with all current kits");
 
                 // done for now
                 completeKits = 0;
                 received = new ArrayList<Part>();
             }
             if(kitRobot!=null) kitRobot.msgKitIsDone();
             // don't need for unit testing
 
         } 
         
         stateChanged();
 
     }
 
     public Semaphore verify = new Semaphore(0);
     private void DoPickUpPartsFromNest(Map<Part, Integer> partsSend)
     {
     	
         for(Part key : partsSend.keySet())
         {
         	print("Picking up " + key.getPartName() + " into Kit " + (2-partsSend.get(key)));
         }
     	
         if(factoryState!=null) 
         {
         	factoryState.doPickUpParts(partsSend);
             try {
                 verify.acquire();
             }
             catch(Exception ex) { }
             // released by DoPickedUpParts once animation is done
         }
     }
     
     /* Photobombing
      * -> photobombing non-normative
      * get in the way of nest, picture taken, move away, then picture and back to normal
      */   
     public Semaphore nowPhotobombing = new Semaphore(0);
     public void msgNowPhotobombing() { nowPhotobombing.release(); }
 
     public Semaphore takenPicture = new Semaphore(0);
     public void msgTakenPicture() { takenPicture.release(); }
     
     public Semaphore goneHome = new Semaphore(0);
     public void msgGoneHome() { goneHome.release(); }
     private void photoBomb(NestAgent n)
     {
 		factoryState.guiPartRobot.doPhotobomb(n.getNum());
 
 		try{
 			nowPhotobombing.acquire();
 		}
 		catch(Exception ex) { }
 
 		n.msgNowPhotobombing();
 
 		try{
 			takenPicture.acquire();
 		}
 		catch(Exception ex) { }
 
 		factoryState.guiPartRobot.donePhotobomb();
 		
 		try{
 			goneHome.acquire();
 		}
 		catch(Exception ex) { }
 	
 		n.msgDonePhotobombing();
     }
 }
