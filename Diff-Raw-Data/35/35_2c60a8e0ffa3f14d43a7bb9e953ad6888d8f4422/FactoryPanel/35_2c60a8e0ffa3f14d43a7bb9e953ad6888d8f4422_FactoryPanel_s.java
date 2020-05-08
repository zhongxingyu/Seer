 package gui.panels;
 
 import engine.agent.*;
 import engine.agent.david.misc.ConveyorFamilyEntity;
 import engine.agent.evan.ConveyorFamilyImplementation;
 import engine.agent.tim.misc.ConveyorFamilyImp;
 import gui.drivers.*;
 import gui.test.*;
 import java.util.*;
 import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 import shared.Glass;
 import shared.enums.MachineType;
 import shared.interfaces.OfflineWorkstation;
 import transducer.*;
 
 /**
  * The FactoryPanel is highest level panel in the actual kitting cell. The FactoryPanel makes all the back end components, connects them to the GuiComponents in the DisplayPanel. It is responsible for
  * handling communication between the back and front end.
  */
 @SuppressWarnings("serial")
 public class FactoryPanel extends JPanel {
 	private enum RunMode{ OFFLINE_CF_TEST, FINAL_SUBMISSION }
 	private static final RunMode RUN_MODE = RunMode.FINAL_SUBMISSION;
 //	private static final RunMode RUN_MODE = OFFLINE_CF_TEST;
 	
 	/** The frame connected to the FactoryPanel */
 	private FactoryFrame parent;
 
 	/** The control system for the factory, displayed on right */
 	private ControlPanel cPanel;
 
 	/** The graphical representation for the factory, displayed on left */
 	private DisplayPanel dPanel;
 
 	/** Allows the control panel to communicate with the back end and give commands */
 	private Transducer transducer;
 
 	/**
 	 * THE AGENTS
 	 */
 	// Initial robot agent
 	private BinRobotAgent binRobot;
 
 	/* ConveyorFamilies & accompanying workstation */
 	// Cutter
 	private BigOnlineConveyorFamilyImp cutterFamily;
 
 	// Breakout
 	private SmallOnlineConveyorFamilyImp breakoutFamily;
 
 	// Manual Breakout
 	private BigOnlineConveyorFamilyImp manualBreakoutFamily;
 	
 	// DRILL
 	private OfflineWorkstationAgent drillWorkstation[];//, crossSeamerWorkstation[]; // just for now extra families
 	private ConveyorFamilyImplementation drillFamily;
 
 	//ConveyorFamilyImp crossSeamerFamily;
 
 	// CrossSeamer - Tim's
 	private OfflineWorkstationAgent crossSeamerWorkstation[];
 	private ConveyorFamilyImp crossSeamerFamily;
 
 	// Grinder - David's
 	private OfflineWorkstationAgent grinderWorkstation[];
 	private ConveyorFamilyEntity grinderFamily;
 
 	// Washer
 	private BigOnlineConveyorFamilyImp washerFamily;
 
 	// Painter
 	private SmallOnlineConveyorFamilyImp painterFamily;
 
 	// UV Lamp
 	private BigOnlineConveyorFamilyImp lampFamily;
 
 	// Oven
 	private BigOnlineConveyorFamilyImp ovenFamily;
 	
 	// TRUCK
 	private TruckAgent truck;
 
 	/**
 	 * Constructor links this panel to its frame
 	 */
 	public FactoryPanel(FactoryFrame fFrame) {
 		parent = fFrame;
 
 		// initialize transducer
 		transducer = new Transducer();
 		transducer.startTransducer();
 
 		// use default layout
 		// dPanel = new DisplayPanel(this);
 		// dPanel.setDefaultLayout();
 		// dPanel.setTimerListeners();
 
 		// initialize and run
 		this.initialize();
 		this.initializeBackEnd();
 	}
 
 	/**
 	 * Initializes all elements of the front end, including the panels, and lays them out
 	 */
 	private void initialize() {
 		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
 
 		// initialize control panel
 		cPanel = new ControlPanel(this, transducer);
 
 		// initialize display panel
 		dPanel = new DisplayPanel(this, transducer); // gui popups and such in here
 
 		// add panels in
 		// JPanel tempPanel = new JPanel();
 		// tempPanel.setPreferredSize(new Dimension(830, 880));
 		// this.add(tempPanel);
 
 		this.add(dPanel);
 		this.add(cPanel);
 	}
 
 	/**
 	 * Feel free to use this method to start all the Agent threads at the same time
 	 */
 	private void initializeBackEnd() {
 		// ===========================================================================
 		// Initialize and start Agent threads
 		// ===========================================================================
 
 		if (RUN_MODE == RunMode.FINAL_SUBMISSION) {
 			/* Instantiate Agents */
 
 			// Initial robot that has the glasses
 			binRobot = new BinRobotAgent("Bin Robot", transducer);
 			cPanel.setBinRobot(binRobot);
 
 			// Cutter
 			cutterFamily = new BigOnlineConveyorFamilyImp(MachineType.CUTTER, transducer, 0);
 
 			// Breakout
 			breakoutFamily = new SmallOnlineConveyorFamilyImp(MachineType.BREAKOUT, transducer, 2);
 			
 			// Manual Breakout
 			manualBreakoutFamily = new BigOnlineConveyorFamilyImp(MachineType.MANUAL_BREAKOUT, transducer, 3);
 			
 			// Drill
 			drillWorkstation = new OfflineWorkstationAgent[2];
 			drillFamily = new ConveyorFamilyImplementation(transducer, drillWorkstation, MachineType.DRILL, 5, 0);
 			for (int i = 0; i < 2; ++i) {
 				drillWorkstation[i] = new OfflineWorkstationAgent(MachineType.DRILL.toString() + i, MachineType.DRILL, i, transducer);
 				drillWorkstation[i].setPopupWorkstationInteraction(drillFamily);
 			}
 			
 			// Cross Seamer
 			crossSeamerWorkstation = new OfflineWorkstationAgent[2];
 			for (int i = 0; i < 2; ++i)
 				crossSeamerWorkstation[i] = new OfflineWorkstationAgent(MachineType.CROSS_SEAMER.toString() + i, MachineType.CROSS_SEAMER, i, transducer);
 			crossSeamerFamily = new ConveyorFamilyImp("Cross Seamer Family", transducer, "Sensors", 12, 13, "Conveyor", 6, "PopUp", 1, crossSeamerWorkstation, MachineType.CROSS_SEAMER);
 			for (int i = 0; i < 2; ++i)
 				crossSeamerWorkstation[i].setPopupWorkstationInteraction(crossSeamerFamily.getPopUp());
 			
 			// Grinder
 			grinderWorkstation = new OfflineWorkstationAgent[2];
 			for (int i = 0; i < 2; ++i)
 				grinderWorkstation[i] = new OfflineWorkstationAgent(MachineType.GRINDER.toString() + i, MachineType.GRINDER, i, transducer);
 			grinderFamily = new ConveyorFamilyEntity(transducer, 7, 2, grinderWorkstation[0], grinderWorkstation[1]);
 			for (int i = 0; i < 2; ++i)
 				grinderWorkstation[i].setPopupWorkstationInteraction(grinderFamily);
 			
 			// Washer
 			washerFamily = new BigOnlineConveyorFamilyImp(MachineType.WASHER, transducer, 8);
 
 			// Painter
 			painterFamily = new SmallOnlineConveyorFamilyImp(MachineType.PAINT, transducer, 10);
 
 			// UV Lamp
 			lampFamily = new BigOnlineConveyorFamilyImp(MachineType.UV_LAMP, transducer, 11);
 
 			// Oven
 			ovenFamily = new BigOnlineConveyorFamilyImp(MachineType.OVEN, transducer, 13);
 			
 			// Truck
 			truck = new TruckAgent("Truck", transducer);
 			
 			// Connect them!
 			binRobot.setNextLineComponent(cutterFamily);
 			cutterFamily.setPreviousLineComponent(binRobot);
 			
 			cutterFamily.setNextLineComponent(breakoutFamily);
 			breakoutFamily.setPreviousLineComponent(cutterFamily);
 			
 			breakoutFamily.setNextLineComponent(manualBreakoutFamily);
 			manualBreakoutFamily.setPreviousLineComponent(breakoutFamily);
 			
 			manualBreakoutFamily.setNextLineComponent(drillFamily);
 			drillFamily.setPreviousLineComponent(manualBreakoutFamily);
 			
 			drillFamily.setNextLineComponent(crossSeamerFamily);
 			crossSeamerFamily.setPrevCF(drillFamily);
 			
 			crossSeamerFamily.setNextCF(grinderFamily);
 			grinderFamily.setPreviousLineComponent(crossSeamerFamily);
 			
 			grinderFamily.setNextLineComponent(washerFamily);
 			washerFamily.setPreviousLineComponent(grinderFamily);
 			
 			washerFamily.setNextLineComponent(painterFamily);
 			painterFamily.setPreviousLineComponent(washerFamily);
 			
 			painterFamily.setNextLineComponent(lampFamily);
 			lampFamily.setPreviousLineComponent(painterFamily);
 			
 			lampFamily.setNextLineComponent(ovenFamily);
 			ovenFamily.setPreviousLineComponent(lampFamily);
 			
 			ovenFamily.setNextLineComponent(truck);
 			truck.setPrevLineComponent(ovenFamily);
 			
 			// Set things in motion!
 			createGlasses();
 			startAgentThreads();
 		} else if (RUN_MODE == RunMode.OFFLINE_CF_TEST) {
 			System.err.println("Running in OFFLINE TEST MODE");
 			TimsOfflineCFIntegrationTest tTest = new TimsOfflineCFIntegrationTest(transducer);
 			//DavidsOfflineCFIntegrationTest dTest = new DavidsOfflineCFIntegrationTest(transducer);
 		}
 		
 		System.out.println("Backend initialization finished.");
 	}
 
 	/**
 	 * Create glasses of various types.
 	 */
 	private void createGlasses() {
 		// Create some glasses to be run through the glassline, and give them to the initial robot (the bin robot)
 		List<Glass> glasses = new ArrayList<Glass>();
 		glasses.add(new Glass(new MachineType[] { MachineType.BREAKOUT, MachineType.DRILL, MachineType.CROSS_SEAMER,
 				MachineType.GRINDER, MachineType.OVEN}));
 		glasses.add(new Glass(new MachineType[] { MachineType.CUTTER, MachineType.BREAKOUT, /*MachineType.MANUAL_BREAKOUT,*/
 				MachineType.DRILL, MachineType.CROSS_SEAMER, MachineType.GRINDER, MachineType.WASHER, MachineType.PAINT,
 				MachineType.UV_LAMP, MachineType.OVEN}));
 		glasses.add(new Glass(new MachineType[] { MachineType.BREAKOUT, MachineType.DRILL, MachineType.CROSS_SEAMER,
 				MachineType.GRINDER, MachineType.OVEN}));
//		glasses.add(new Glass(new MachineType[] { MachineType.BREAKOUT, MachineType.DRILL, MachineType.CROSS_SEAMER,
//				MachineType.GRINDER, MachineType.OVEN}));
//		glasses.add(new Glass(new MachineType[] { MachineType.CUTTER, MachineType.BREAKOUT, /*MachineType.MANUAL_BREAKOUT,*/
//				MachineType.DRILL, MachineType.CROSS_SEAMER, MachineType.GRINDER, MachineType.WASHER, MachineType.PAINT,
//				MachineType.UV_LAMP, MachineType.OVEN}));
 //		glasses.add(new Glass(new MachineType[] { MachineType.BREAKOUT, MachineType.DRILL, MachineType.CROSS_SEAMER,
 //				MachineType.GRINDER, MachineType.OVEN}));
 //		glasses.add(new Glass(new MachineType[] { MachineType.BREAKOUT, MachineType.DRILL, MachineType.CROSS_SEAMER,
 //				MachineType.GRINDER, MachineType.OVEN}));
 //		glasses.add(new Glass(new MachineType[] { MachineType.CUTTER, MachineType.BREAKOUT, /*MachineType.MANUAL_BREAKOUT,*/
 //				MachineType.DRILL, MachineType.CROSS_SEAMER, MachineType.GRINDER, MachineType.WASHER, MachineType.PAINT,
 //				MachineType.UV_LAMP, MachineType.OVEN}));
 //		glasses.add(new Glass(new MachineType[] { MachineType.BREAKOUT, MachineType.DRILL, MachineType.CROSS_SEAMER,
 //				MachineType.GRINDER, MachineType.OVEN}));
 		
 		binRobot.seedGlasses(glasses);
 	}
 	
 	private void startAgentThreads() {
 		binRobot.startThread();
 		cutterFamily.startThreads();
 		breakoutFamily.startThreads();
 		manualBreakoutFamily.startThreads();
 		drillFamily.startThreads();
 		for (int i = 0; i < 2; ++i)
 			drillWorkstation[i].startThread();
 		crossSeamerFamily.startThreads();
 		for (int i = 0; i < 2; ++i)
 			crossSeamerWorkstation[i].startThread();
 		grinderFamily.startThreads();
 		for (int i = 0; i < 2; ++i)
 			grinderWorkstation[i].startThread();
 		washerFamily.startThreads();
 		painterFamily.startThreads();
 		lampFamily.startThreads();
 		ovenFamily.startThreads();
 		truck.startThread();
 	}
 
 	/**
 	 * Returns the parent frame of this panel
 	 * 
 	 * @return the parent frame
 	 */
 	public FactoryFrame getGuiParent() {
 		return parent;
 	}
 
 	/**
 	 * Returns the control panel
 	 * 
 	 * @return the control panel
 	 */
 	public ControlPanel getControlPanel() {
 		return cPanel;
 	}
 
 	/**
 	 * Returns the display panel
 	 * 
 	 * @return the display panel
 	 */
 	public DisplayPanel getDisplayPanel() {
 		return dPanel;
 	}
 }
