 package state;
 
 import java.awt.event.*;
 import java.util.ArrayList;
 import java.util.concurrent.Semaphore;
 
 import javax.swing.Timer;
 
 import state.transducers.Transducer;
 
 import gui.*;
 import agents.*;
 import agents.interfaces.Nest;
 
 public class FactoryState implements ActionListener {
 	
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
 	public ArrayList<GUI_Part>		guiPartList;
 	public ArrayList<Kit>			kitList;
 	public ArrayList<GUI_Kit>		guiKitList;
 	
 	//Part A declarations here.
 
 	//No gui?
 	public FCSAgent					fcs;
 	public KitVisionAgent			kitVision;    
 
     public ConveyorAgent			conveyor;
 	public GUI_Conveyor				guiConveyor;
 	public KitRobotAgent			kitRobot;
 	public GUI_KitRobot				guiKitRobot;
 
     public Semaphore partsRobotFillsKits = new Semaphore(0);
     public void partsRobotFillsKitLockRelease(){ partsRobotFillsKits.release(); }
 	
     //Part B declarations here.
 	
 	//No gui
 	public PartVisionAgent			partVision;
 	
 
 	public ArrayList<NestAgent>		nestList;
 	public ArrayList<GUI_Nest>		guiNestList;
 	public KitStand					kitStand;
 	public GUI_KitStand				guiKitStand;
 	public PartRobotAgent			partRobot;
 	public GUI_PartRobot			guiPartRobot;
 	
 	public Transducer				transducer;
 	
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
 		
 		compList = new ArrayList<GUI_Component>();
 		fpmDrawList = new ArrayList<Drawing>();
 		kamDrawList = new ArrayList<Drawing>();
 		gmDrawList = new ArrayList<Drawing>();
 		
 		partList = new ArrayList<Part>();
 		guiPartList = new ArrayList<GUI_Part>();
 		kitList = new ArrayList<Kit>();
 		guiKitList = new ArrayList<GUI_Kit>();
 		
 		nestList = new ArrayList<NestAgent>(8);
 		guiNestList = new ArrayList<GUI_Nest>(8);
 		kitStand = new KitStand();
 		guiKitStand = new GUI_KitStand(100, 100);
 		
 		partRobot = new PartRobotAgent("PartRobot", nestList, kitStand, transducer);
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
 		
 		partVision = new PartVisionAgent("Camera");
 		
 		addNests();
 		
 		addToList(guiKitStand);
 		addToList(guiPartRobot);
 		
 		//Part C
 		
 		//Start agent threads...
 		
 		fcs.startThread();
 		conveyor.startThread();
 		kitRobot.startThread();
 		kitVision.startThread();
 		
 		partVision.startThread();
 		partRobot.startThread();
 		
 		for(NestAgent nest : nestList)
 			nest.startThread();
 		
 		//Finally, make the timer for FactoryState and start it.
 		new Timer(33, this).start();
 	}
 	
 	void addNests() {
 		for(int i=0; i<4; i++) {
 			GUI_NestPair p = new GUI_NestPair(400, 2 * i * GUI_Nest.HEIGHT + 20, this);
 			
 			//Nest 1
 			NestAgent n1 = new NestAgent("Nest " + (i*2), "DummyType", partRobot, p.getNest1(), partVision);
 			p.getNest1().setNestAgent(n1);
 			nestList.add(n1);
 			guiNestList.add(p.getNest1());
 			
 			//Nest 2
 			NestAgent n2 = new NestAgent("Nest " + (i*2+1), "DummyType", partRobot, p.getNest2(), partVision);
 			p.getNest2().setNestAgent(n2);
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
 }
