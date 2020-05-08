 package agents;
 
 import agents.include.*;
 import agents.interfaces.*;
 import java.util.*;
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.TimeUnit;
 import state.*;
 import gui.*;
 
 public class KitRobotAgent extends Agent implements KitRobot {
 
     /*** Data Structures **/
 
     Conveyor conveyor;
     KitVision vision;
     KitStand kitStand;
     PartRobot partRobot;
     public GUI_KitRobot guiKitRobot;
     public GUI_KitStand guiKitStand;
     FactoryState factory;
     
     public boolean testing = false;
 
     public List<Kit> kits;
 
     KitRobotState state;
     enum KitRobotState { none, waitingRequests, receivedEmpty, kitting }
 
     public int current = 2;
     public int numRequests = 0;
     public int needInspection = 0;
     
     String name;
     
     /*** Constructor **/
     
     public KitRobotAgent(String name, Conveyor c, KitVision v, KitStand k, PartRobot p, FactoryState factoryState) {
         this.name = name;
     	this.conveyor = c;
         this.vision = v;
         this.kitStand = k;
         this.partRobot = p;
         this.factory = factoryState;
         this.kits = new ArrayList<Kit>();
         this.state = KitRobotState.waitingRequests;
     }
     
     //constructor2 for PartA unit testing
     public KitRobotAgent(String name, Conveyor c, KitVision v, KitStand k, PartRobot p) {
         this.name = name;
     	this.conveyor = c;
         this.vision = v;
         this.kitStand = k;
         this.partRobot = p;
         this.kits = new ArrayList<Kit>();
         this.state = KitRobotState.waitingRequests;
     }
 
     /*** Messages **/
 
     /*  Message to request to deliver empty kit
      *  Source: Conveyor
      */ 
     public void msgRequestMoveKit() {
         numRequests++;
         print("received request from conveyor to grab kit");
         stateChanged();
     }
 
     /*  Message to deliver empty kit
      *  Source: Conveyor
      */ 
     public void msgHeresKits(List<Kit> k) {
         kits.addAll(k);        
         state = KitRobotState.receivedEmpty;
         current = k.size();
         print("received list of kit(s) from conveyor holding " + current + " kit(s).");
         stateChanged();
     }
 
     /*  Message that kits have been made
      *  Source: PartRobot
      */ 
     synchronized public void msgKitIsDone() {
         print("PartRobot filled a kit on stand");
         needInspection++;
         state = KitRobotState.kitting;
         stateChanged();
     }
 
     /*** Scheduler ***/
 
     public boolean pickAndExecuteAnAction() {
 
         if(state == KitRobotState.waitingRequests && numRequests > 0)
         {
             state = KitRobotState.none;
             processRequests();
             return true;
         }
 
         if(state == KitRobotState.receivedEmpty)
         {
             state = KitRobotState.none;
             putKitsOnStand();
             partRobot.msgMakeKits();
             print("waiting for parts robot to fill kits");
             return true;                       
         }
         if(state == KitRobotState.kitting)
         {       
         	boolean passed = true;
         	while (needInspection > 0 ){
         		passed = inspectionProcess();
         		needInspection--;
         	}
         	if (passed) {
         		state = KitRobotState.waitingRequests;
         		stateChanged();
         	}
         	else state = KitRobotState.none;
             return true;
         }
 
         return false;
     }
     /*** Actions ***/
 
     synchronized private void processRequests()
     {
         // processRequests
         if(numRequests == 1) 
         {
             current = 1;
             numRequests = 0;
             conveyor.msgGiveMeKits(1);
         }
         else // numRequests >=2 
         {
             current = 2;
             numRequests -= 2;
             conveyor.msgGiveMeKits(2);
         }
         stateChanged();
     }
     
     private void putKitsOnStand()
     {
         for(Kit kit : kits)
         {
         	if(guiKitRobot != null ) DoPlaceOnKitStand(kit);
             kitStand.insertEmptyKit(kit);
             print("guiRobot placed kit on stand.");            	
         }
         
         kits = new ArrayList<Kit>();
         stateChanged();
     }
 
     public Semaphore visionLock = new Semaphore(0);
     public Semaphore visionLock() { return visionLock; }
     public Semaphore waitForNextKit = new Semaphore(0);
 
     private boolean inspectionProcess()
     {
     	boolean pass = true;
     	Kit kit;
     	kit = kitStand.removeCompleteKit();
     	if (guiKitStand != null){
     	DoMoveFromKitStandToInspection(kit);}	
         kitStand.placeInspection(kit);
         //print("gui placed kit from filling to inspection");
         
         kit = kitStand.inspectKit();
         
        	if (guiKitRobot != null){
         	vision.msgKitReadyForInspection();
         	try {
         		visionLock.tryAcquire(15, TimeUnit.SECONDS);
         	}
         	catch(Exception ex) { print("fml"); }
         }
        	else //bypass sema4 for testing
            	pass = vision.inspectKit();
        
         // after release
         kit = kitStand.removeInspection();
         pass = vision.getInspectionResults();
         
         if (pass){
         	print("inspection pass function");
         	MoveKitToConveyor(kit);
         	if (guiKitRobot != null){
 				try {
 					waitForNextKit.tryAcquire(4, TimeUnit.SECONDS);
 				} catch (InterruptedException e) {
 					print("mother efer");
 				}
 				DoResetPosition();
         	}
         }
         else {
         	print("Inspection failed function");
         	MoveKitBackToStand(kit);  
         }
 		    
 		stateChanged();
 		return pass;
         
     }
     
     private void MoveKitBackToStand(Kit kit){
     	kit.complete = false;
     	kit.setKitBad();
     	if(guiKitRobot != null) 
     	DoMoveKitBackToStand(kit);
     	kitStand.insertEmptyKit(kit);
     	partRobot.msgFixKit(kitStand.count-1, vision.getMissingParts());
     }
     private void MoveKitToConveyor(Kit kit){
         if(guiKitRobot != null) 
         DoMoveFromInspectionToConveyor(kit);
 	    conveyor.msgHereIsCompleteKit(kit);        	
     }
     
     //setters
     public void setGUIKitRobot(GUI_KitRobot r){
     	guiKitRobot = r;
     }
     public void setGUIKitStand(GUI_KitStand stand){
     	guiKitStand = stand;
     }
     public void setCamera(KitVisionAgent cam){
     	vision = cam;
     }
     
     //extra
     public String getName(){ return name; }
     public void setInTest() { testing = true; }
 
     public Semaphore AnimationLock = new Semaphore(0);
     public void AnimationLockRelease(){ 
     	AnimationLock.release();
     }
     public Semaphore displayKitOnStandLock = new Semaphore(0);
     public void kitOnStandRelease() { displayKitOnStandLock.release(); }
     
     //GUI
     private void DoPlaceOnKitStand(Kit kit) { 
     	print("guiKitRobot told to place kit on stand");
     	guiKitRobot.DoPlaceOnKitStand(kit, this);
     	try {
 			AnimationLock.acquire();
 		} catch (InterruptedException e) {
 			print("fack");
 		}
     	guiKitStand.DoAddKit(kit, this);
     	try {
     		displayKitOnStandLock.acquire();
     	} catch (InterruptedException e) {
 			print("fack2");
 		}
     	
     }
 
     public Semaphore inspectLock = new Semaphore(0);
     public void inspectLockRelease(){ inspectLock.release(); }
     
     private void DoMoveFromKitStandToInspection(Kit kit) { 
     	print("telling guikit robot to move from stand to inspection");
     	guiKitRobot.DoMoveFromKitStandToInspection(kit, this);
     	try {
 			inspectLock.acquire();
 		} catch (InterruptedException e) {
 			print("Shiet!");
 		}
     }
     
     public Semaphore inspectionClear = new Semaphore(0);
     public void inspectionToConveyorRelease(){ inspectionClear.release(); } 
 
     private void DoMoveFromInspectionToConveyor(Kit kit) {
     	guiKitStand.DoRemoveKit(kit);
     	guiKitRobot.DoMoveFromInspectionToConveyor(kit);
     	try {
 			inspectionClear.acquire();
 		} catch (InterruptedException e) {
 			print("Shiet!");
 		}
     }
     public GUI_KitStand getGUIKitStand(){ return guiKitStand; }
     public void beReadyForNextKitSoon(){
         conveyor.provideNextKit();
     }
     
     private void DoMoveKitBackToStand(Kit kit){
     	/*guiKitRobot.DoMoveKitBackToStand(kit);
     	try {
 			AnimationLock.acquire();
 		} catch (InterruptedException e) {
 			print("fack");
 		}
     	guiKitStand.DoAddKit(kit, this);
     	try {
     		displayKitOnStandLock.acquire();
     	} catch (InterruptedException e) {
 			print("fack2");
 		}
 		*/
     }
     
     private void DoResetPosition(){
     	guiKitRobot.DoGoHome();
     }
 
 }
