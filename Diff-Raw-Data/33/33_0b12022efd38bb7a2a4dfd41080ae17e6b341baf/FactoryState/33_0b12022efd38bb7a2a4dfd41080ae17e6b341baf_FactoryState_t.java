 package state;
 
 import java.awt.event.*;
 import java.util.concurrent.Semaphore;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import javax.swing.Timer;
 import state.transducers.*;
 import gui.*;
 import agents.*;
 import agents.interfaces.*;
 
 public class FactoryState implements ActionListener, TReceiver {
 	
 	/*
 	 * README!!!
 	 * Factory State rules:
 	 * 
 	 * 1. All objects in FactoryState that are displayed should have a GUI
 	 * object declaration immediately after its declaration.
 	 * 2. Everything should be public--this is our global state, it makes no
 	 * sense to have it as private.
 	 * 3. Everything needs to be initialized IN THE SAME ORDER that it is
 	 * declared.
 	 * 4. Make things neat, this is code we all have to work with.
 	 */
 
 	
 	//*** Variable Declarations ***
 	
 	//This component list should only have TOP-LEVEL objects. Any objects
 	//"inside" others are owned by that object and should be sub-drawings (this
 	//should only be kits and parts).
 	public ArrayList<GUI_Component>	compList;
 	public ArrayList<Drawing>		fpmDrawList, kamDrawList, gmDrawList, lmDrawList;
 	
 	public ArrayList<Part>			partList;
 	public java.util.List<GUI_Part> guiPartList = Collections.synchronizedList(new ArrayList<GUI_Part>());
 	public ArrayList<Kit>			kitList;
 	public ArrayList<GUI_Kit>		guiKitList;
 
 	public Transducer				transducer;
 
 	//Part A declarations here.
 
 	public FCSAgent					fcs;
 	public KitVisionAgent			kitVision;    
 
     public ConveyorAgent			conveyor;
 	public GUI_Conveyor				guiConveyor;
 	public KitRobotAgent			kitRobot;
 	public GUI_KitRobot				guiKitRobot;
 
	public GUI_Camerca;		
	
     public Semaphore partsRobotFillsKits = new Semaphore(0);
     public void partsRobotFillsKitLockRelease(){ partsRobotFillsKits.release(); }
 	
     //Part B declarations here.
 	
 	public PartVisionAgent			partVision;
 	
 	public ArrayList<Nest> 			nests;
 	public ArrayList<NestAgent>		nestList;
 	public ArrayList<GUI_Nest>		guiNestList;
 	public KitStand					kitStand;
 	public GUI_KitStand				guiKitStand;
 	public PartRobotAgent			partRobot;
 	public GUI_PartRobot			guiPartRobot;
 	
 	
 	//Part C declarations here.
 	
 	public ArrayList<FeederAgent>	feederList;
 	public ArrayList<GUI_Feeder>	guiFeederList;
 	public GantryAgent				gantryRobot;
 	public GUI_GantryRobot			guiGantryRobot;
 	public ArrayList<LaneAgent>		laneList;
 	public ArrayList<GUI_Lane>		guiLaneList;
 	
 	//No agent?
 	public GUI_Bin 					guiBin;
 	
 	//*** Methods ***
 	
 	/*
 	 * This is the master actionPerformed() method that calls updateGraphics on
 	 * all top level objects. All top level objects are responsible for the
 	 * objects they own.
 	 */
 	public void actionPerformed(ActionEvent ae) {
 		for(GUI_Component c : compList)
 			c.updateGraphics();
 	}
 	
 	public FactoryState() {		
 		transducer = new Transducer();
 		transducer.register(this, TChannel.GUI);
 		
 		compList = new ArrayList<GUI_Component>();
 		fpmDrawList = new ArrayList<Drawing>();
 		kamDrawList = new ArrayList<Drawing>();
 		gmDrawList = new ArrayList<Drawing>();
 		
 		partList = new ArrayList<Part>();
 		guiPartList = new ArrayList<GUI_Part>();
 		kitList = new ArrayList<Kit>();
 		guiKitList = new ArrayList<GUI_Kit>();
 
 		nests = new ArrayList<Nest>(8);
 		nestList = new ArrayList<NestAgent>(8);
 		guiNestList = new ArrayList<GUI_Nest>(8);
 		kitStand = new KitStand();
 		guiKitStand = new GUI_KitStand(100, 100);
 		
 		
 		partRobot = new PartRobotAgent("PartRobot", nests, kitStand, transducer);
 		guiPartRobot = new GUI_PartRobot(this, partRobot, 0, 0);
 		
 
 	
 		//Part A
 		fcs = new FCSAgent();
 		kitVision = new KitVisionAgent("InspectionCamera", kitStand);
 
 	    conveyor = new ConveyorAgent("ConveyorAgent", fcs, this);
 		guiConveyor = new GUI_Conveyor();
 		fcs.setConveyor(conveyor);
 
 		kitRobot = new KitRobotAgent("KitRobotAgent", conveyor, kitVision, kitStand, partRobot, this);
 		guiKitRobot = new GUI_KitRobot(guiConveyor, guiKitStand);
 		kitVision.setKitRobot(kitRobot);
 		conveyor.setKitRobot(kitRobot);
 		conveyor.setConveyorGUI(guiConveyor);
 		
 		addToList(guiConveyor);
 		addToList(guiKitStand);
 		addToList(guiKitRobot);
 		
 		//Part B
 		
		partVision = new PartVisionAgent("PartCamera");
 		
 		addNests();
 		
 		addToList(guiKitStand);
 		addToList(guiPartRobot);
 		
 		//Part C
 		
 		feederList = new ArrayList<FeederAgent>();
 		guiFeederList = new ArrayList<GUI_Feeder>();
 		gantryRobot = new GantryAgent("The Gantry");
 		guiGantryRobot = new GUI_GantryRobot(gantryRobot);
 		laneList = new ArrayList<LaneAgent>();
 		guiLaneList = new ArrayList<GUI_Lane>();
 		
 		guiBin = new GUI_Bin();
 		
 		
 		
 		//delete below
 		// test simulation for v0
 
 		Map<String,Integer> kitConfig1 = new HashMap<String,Integer>();
 		kitConfig1.put("clown", 2);
 		kitConfig1.put("angler", 2);
 		kitConfig1.put("puffer",4);
 		Kit sampleKit1 = new Kit(kitConfig1, 0);
 		GUI_Kit gk1 = new GUI_Kit(sampleKit1, 100, 100*0);
 		guiKitStand.addkit(gk1, 0);
 		
 		Map<String,Integer> kitConfig2 = new HashMap<String,Integer>();
 		kitConfig2.put("clown", 1);
 		kitConfig2.put("angler", 1);
 		kitConfig2.put("puffer",2);
 		Kit sampleKit2 = new Kit(kitConfig2, 1);		
 		GUI_Kit gk2 = new GUI_Kit(sampleKit2, 100, 100*1);
 		guiKitStand.addkit(gk2, 1);		
 			
 
 		kitStand.insertEmptyKit(sampleKit1);
 		kitStand.insertEmptyKit(sampleKit2);
    		partRobot.msgMakeKits();
 
    		// delete above
 		
 		//Start agent threads...
 		
 		fcs.startThread();
 		conveyor.startThread();
 		kitRobot.startThread();
 		kitVision.startThread();
 		
 		partVision.startThread();
 		partRobot.startThread();
 		
 		for(NestAgent nest : nestList) nest.startThread();
 		
 		
    		// lane 0 becomes clown
    		// lane 1 becomes angler
    		// lane 2 becomes puffer
    		//nestAgents.get(0).msgHereIsPart(new Part("clown"));
    		//nestAgents.get(0).msgHereIsPart(new Part("clown"));
    		//nestAgents.get(0).msgHereIsPart(new Part("clown"));
 
 		for(int i=0; i<8; i++) nestList.get(0).msgHereIsPart(new Part("clown"));
 		for(int i=0; i<15; i++) nestList.get(1).msgHereIsPart(new Part("angler"));
 		for(int i=0; i<10; i++) nestList.get(2).msgHereIsPart(new Part("puffer"));
 		
 		
 		//Finally, make the timer for FactoryState and start it.
 		new Timer(33, this).start();
 	}
 	
 	void addNests() {
 		for(int i=0; i<4; i++) {
 			GUI_NestPair p = new GUI_NestPair(400, 2 * i * GUI_Nest.HEIGHT + 20, this);
 			
 			//Nest 1
 			NestAgent n1 = new NestAgent("Nest " + (i*2), "DummyType", partRobot, p.getNest1(), partVision);
 			p.getNest1().setNestAgent(n1);
 			nests.add(n1);
 			nestList.add(n1);
 			guiNestList.add(p.getNest1());
 			
 			//Nest 2
 			NestAgent n2 = new NestAgent("Nest " + (i*2+1), "DummyType", partRobot, p.getNest2(), partVision);
 			p.getNest2().setNestAgent(n2);
 			nests.add(n2);
 			nestList.add(n2);
 			guiNestList.add(p.getNest2());
 			
 			addToList(p);
 		}
 	}
 	
 	void addToList(GUI_Component g, ListOwner... listOwners) {
 		//Add to the requested lists
 		for(ListOwner l : listOwners) {
 			switch(l) {
 			case GM:
 				gmDrawList.add(g.myDrawing);
 				break;
 			case KAM:
 				kamDrawList.add(g.myDrawing);
 				break;
 			case LM:
 				lmDrawList.add(g.myDrawing);
 				break;
 			}
 		}
 		
 		//Then add to FPM's list (always).
 		fpmDrawList.add(g.myDrawing);
 		compList.add(g);
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args)
 	{
 		if(channel == TChannel.GUI)
 		{
 			if(event == TEvent.SendPart)
 			{
 				Map<Part, Integer> parts = (Map<Part, Integer>) args[0];
 				doPickUpParts(parts);
 
 			}
 		}
 	}
 
 	//*** DoXXX API ***
 	public void doPickUpParts(Map<Part, Integer> parts) {
                 
 		ArrayList<GUI_Part> partsToGet = new ArrayList<GUI_Part>();
 		ArrayList<Integer> nestIndices = new ArrayList<Integer>();
 		synchronized(guiPartList) {
 			for (GUI_Part p : guiPartList) {
 				if (parts.containsKey(p.agentPart)) {	                                
 					partsToGet.add(p);
 					nestIndices.add(parts.get(p.agentPart));
 				}
 
 			}
 		}
 		//guiPartRobot.doTransferParts(partsToGet, nestIndices);
		
		//this should be called by the GUI once we do guiPartRobot.doTransferParts...
         transducer.fireEvent(TChannel.Agents, TEvent.DonePickingUpParts, null);
 
 	}    
 	
 }
