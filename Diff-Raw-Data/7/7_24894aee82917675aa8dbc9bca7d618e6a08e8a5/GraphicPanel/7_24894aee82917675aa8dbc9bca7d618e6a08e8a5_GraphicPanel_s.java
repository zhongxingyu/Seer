 package factory.graphics;
 
 import java.awt.*;
 
 import javax.swing.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 
 import factory.Part;
 import factory.client.*;
 
 public abstract class GraphicPanel extends JPanel implements ActionListener{
 	
 	public static final int WIDTH = 1100, HEIGHT = 720;
 	public static final int delay = 50;
 	
 	protected Client am; //The Client that holds this
 	
 	protected boolean isLaneManager;
 	protected boolean isGantryRobotManager;
 	protected boolean isKitAssemblyManager;
 	protected boolean isFactoryProductionManager;
 	
 	// LANE MANAGER
 	protected GraphicLaneManager [] lane;
 	
 	// CAMERA
 	protected int flashCounter;
 	protected int flashFeederIndex;
 	protected static Image flashImage;
 	
 	// KIT MANAGER
 	protected GraphicConveyorBelt belt; //The conveyer belt
 	protected GraphicKittingStation station; //The kitting station
 	protected GraphicKittingRobot kitRobot;
 	
 	// PARTS MANAGER
 	protected ArrayList<GraphicNest> nests;
 	protected GraphicPartsRobot partsRobot;
 	
 	// GANTRY
 	protected GraphicGantryRobot gantryRobot;
 	
 	public GraphicPanel() {
 		am = null;
 		lane = null;
 		belt = null;
 		station = null;
 		kitRobot = null;
 		nests = null;
 		partsRobot = null;
 		gantryRobot = null;
 		
 		isLaneManager = false;
 		isGantryRobotManager = false;
 		isKitAssemblyManager = false;
 		isFactoryProductionManager = false;
 		
 		flashImage = Toolkit.getDefaultToolkit().getImage("Images/flash3x3.png");
 	}
 	
 	public void newEmptyKit() {
 		//Adds a kit into the factory via conveyer belt
 		//if (!belt.kitin())
 		if (isKitAssemblyManager || isFactoryProductionManager)
 			belt.inKit();
 	}
 	
 	public void moveEmptyKitToSlot(int target) {
 		//Sends robot to pick up kit from belt and move to designated slot in the station
 		//if (belt.pickUp() && !kitRobot.kitted() && station.getKit(target) == null) {
 		if (isKitAssemblyManager || isFactoryProductionManager) {
 			kitRobot.setFromBelt(true);
 			kitRobot.setStationTarget(target);
 		}
 	}
 	
 	public void moveKitToInspection(int target) {
 		//Sends robot to move kit from designated slot in the station to inspection station
 		//if (!kitRobot.kitted() && station.getKit(target) != null) {
 		if (isKitAssemblyManager || isFactoryProductionManager) {
 			kitRobot.setCheckKit(true);
 			kitRobot.setStationTarget(target);
 		}
 	}
 	
 	public void takePictureOfInspectionSlot() {
 		//Triggers the camera flash
 		//if (station.hasCheck())
 		if (isKitAssemblyManager || isFactoryProductionManager)
 			station.checkKit();
 	}
 	
 	public void dumpKitAtInspection() {
 		//Sends robot to move kit from inspection station to trash
 		//if (!kitRobot.kitted() && station.getCheck() != null)
 		if (isKitAssemblyManager || isFactoryProductionManager)
 			kitRobot.setPurgeKit(true);
 	}
 	
 	public void moveKitFromInspectionToConveyor() {
 		//Sends a kit out of the factory via conveyer belt
 		//if (station.getCheck() != null && !kitRobot.kitted())
 		if (isKitAssemblyManager || isFactoryProductionManager)
 			kitRobot.setFromCheck(true);
 	}
 	
 	public void exportKit() {
 		if (isKitAssemblyManager || isFactoryProductionManager)
 			belt.exportKit();
 	}
 	
 	public void cameraFlash(int nestIndex) {
 		flashCounter = 10;
 		flashFeederIndex = nestIndex;
 	}
 	
 	public void moveGantryRobotToPickup(String path)
 	{
 		gantryRobot.setState(1);
 		gantryRobot.setDestination(WIDTH-100,-100);
 	}
 	
 	public void moveGantryRobotToFeederForDropoff(int feederIndex)
 	{
 		// Error checking code has temporarily(?) been commented out as requested by Alfonso
 		//if(lane[feederIndex].hasBin())
 		//{
 			//System.out.println("1");
 			//System.err.println("Can't dropoff: feeder " + feederIndex + " (0-based index) already has a bin!");
 			//gantryRobotArrivedAtFeederForDropoff();
 		//}
 		//else if(!gantryRobot.hasBin())
 		//{
 			//System.out.println("2");
 			//System.err.println("Can't dropoff: gantry robot does not have a bin!");
 			//gantryRobotArrivedAtFeederForDropoff();
 		//}
 		//else
 		//{
 			//System.out.println("3");
 			gantryRobot.setState(3);
 			gantryRobot.setDestinationFeeder(feederIndex);
 			gantryRobot.setDestination(lane[feederIndex].feederX+95, lane[feederIndex].feederY+15);
 			//gantryRobotArrivedAtFeederForDropoff();
 		//}
 	}
 	
 	public void moveGantryRobotToFeederForPickup(int feederIndex)
 	{
 		// Error checking
 		//if(!lane[feederIndex].hasBin())
 		//{
 		//	System.err.println("Can't pickup: no bin at feeder " + feederIndex + " (0-based index)!");
 		//	gantryRobotArrivedAtFeederForPickup();
 		//}
 		//else
 		//{
 			gantryRobot.setState(5);
 			gantryRobot.setDestinationFeeder(feederIndex);
 			gantryRobot.setDestination(lane[feederIndex].feederX+95, lane[feederIndex].feederY+15);
 		//}
 	}
 	
 	//CHANGE TO 0 BASE
 	public void movePartsRobotToNest(int nestIndex) {
 		partsRobot.setState(1);
 		partsRobot.adjustShift(5);
 		partsRobot.setDestination(nests.get(nestIndex).getX()-nests.get(nestIndex).getImageWidth()-10,nests.get(nestIndex).getY()-15);
 		partsRobot.setDestinationNest(nestIndex);
 	}
 	
 	public void movePartsRobotToKit(int kitIndex) {
 		partsRobot.setState(3);
 		partsRobot.setDestination(station.getX()+35,station.getY()-station.getY()%5);
 		partsRobot.setDestinationKit(kitIndex);
 	}
 	
 	public void movePartsRobotToCenter() {
 		partsRobot.setState(5);
 		partsRobot.setDestination(WIDTH/2-200, HEIGHT/2);
 	}
 	
 	public void feedFeeder(int feederNum) {
		feedLane(feederNum*2);
 	}
 	
 	public void feedLane(int laneNum){ //FEEDS THE LANE! Lane 0-7
 		/*//Testing for quick feed
 		lane[(laneNum) / 2].bin = new GraphicBin(new Part("eyes"));
 		lane[(laneNum) / 2].binExist = true;
 		//end Test*/
 		if(!lane[(laneNum) / 2].lane1PurgeOn){	//If purging is on, cannot feed!
 			if(lane[(laneNum) / 2].hasBin() && lane[(laneNum) / 2].bin.getBinItems().size() > 0){
 				lane[(laneNum) / 2].laneStart = true;
 				lane[(laneNum) / 2].divergeUp = ((laneNum) % 2 == 0);
 				lane[(laneNum) / 2].feederOn = true;
 			}
 		}
 		//System.out.println("bin size " + lane[(laneNum) / 2].bin.getBinItems().size());
 	}
 	
 	public void startLane(int laneNum){
 		lane[(laneNum) / 2].laneStart = true;
 	}
 	
 	public void switchLane(int laneNum){
 		lane[(laneNum) / 2].divergeUp = !lane[(laneNum) / 2].divergeUp;
 		lane[(laneNum) / 2].vY = -(lane[(laneNum) / 2].vY);
 	}
 	
 	public void switchFeederLane(int feederNum){
 		lane[feederNum].divergeUp = !lane[feederNum].divergeUp;
 		lane[feederNum].vY = -(lane[feederNum].vY);
 		switchFeederLaneDone(feederNum);
 	}
 	
 	public void stopLane(int laneNum){
 		lane[(laneNum) / 2].laneStart = false;
 	}
 	
 	public void turnFeederOn(int feederNum){
 		lane[feederNum].feederOn = true;
 		startFeederDone(feederNum);
 	}
 
 	public void turnFeederOff(int feederNum){
 		lane[feederNum].feederOn = false;
 		stopFeederDone(feederNum);
 	}
 	
 	public void purgeFeeder(int feederNum){ // takes in lane 0 - 4
 		lane[(feederNum)].bin = null;
 		lane[(feederNum)].binExists = false;
 		lane[(feederNum)].feederOn = false;
 		
 	}
 	
 	public void purgeTopLane(int feederNum){
 		lane[feederNum].lane1PurgeOn = true;
 		lane[feederNum].feederOn = false;
 		lane[feederNum].laneStart = true;
 	}
 	
 	public void purgeBottomLane(int feederNum){
 		lane[feederNum].lane2PurgeOn = true;
 		lane[feederNum].feederOn = false;
 		lane[feederNum].laneStart = true;
 	}
 	
 	public void partsRobotStateCheck() {
 		// Has robot arrived at its destination?
 		//System.out.println(partsRobot.getState());
 		if(partsRobot.getState() == 2)		// partsRobot has arrived at nest
 		{
 			// Give item to partsRobot
 			if(partsRobot.getSize() < 4)
 			{
 				if (nests.get(partsRobot.getDestinationNest()).hasItem())
 					partsRobot.addItem(nests.get(partsRobot.getDestinationNest()).popItem());
 				partsRobot.setState(0);
 			}
 			partsRobotArrivedAtNest();
 		}
 		else if(partsRobot.getState() == 4)	// partsRobot has arrived at kitting station
 		{
 			System.out.println("Size:"+partsRobot.getSize());
 			int numberOfParts = partsRobot.getSize();
 			for(int i = 0; i < numberOfParts; i++)
 			{
 				System.out.println("Adding part to kit: " + i);
 				station.addItem(partsRobot.popItem(),partsRobot.getDestinationKit());
 			}
 			partsRobot.setState(0);
 			partsRobotArrivedAtStation();
 		}
 		else if(partsRobot.getState() == 6)
 		{
 			partsRobot.setState(0);
 			partsRobotArrivedAtCenter();
 		}
 	}
 	
 	public void gantryRobotStateCheck() {
 		if(gantryRobot.getState() == 2)				// gantry robot reached bin pickup point
 		{
 			gantryRobot.setState(0);
 			// Give gantry robot a bin
 			gantryRobot.giveBin(new GraphicBin(new Part("Test Item")));
 			gantryRobotArrivedAtPickup();
 		}
 		else if(gantryRobot.getState() == 4)		// gantry robot reached feeder for dropoff
 		{
 			gantryRobot.setState(0);
 			lane[gantryRobot.getDestinationFeeder()].setBin(gantryRobot.popBin());
 			gantryRobotArrivedAtFeederForDropoff();
 		}
 		else if(gantryRobot.getState() == 6)		// gantry robot reached feeder for pickup
 		{
 			gantryRobot.setState(0);
 			gantryRobot.giveBin(lane[gantryRobot.getDestinationFeeder()].popBin());
 			gantryRobotArrivedAtFeederForPickup();
 		}
 	}
 	
 	public void moveLanes() {
 		for (int i = 0; i < lane.length; i++)
 			lane[i].moveLane();
 	}
 	
 	public void sendMessage(String command) {
 		//if (am == null)
 			//return;
 		//asdfasd
 		String message;
 		if (isLaneManager)
 			message = "lm ";
 		else if (isGantryRobotManager)
 			message = "grm ";
 		else if (isKitAssemblyManager)
 			message = "kam ";
 		else if (isFactoryProductionManager)
 			message = "fpm ";
 		else
 			return;
 		
 		if (am != null)
 			am.sendCommand(message + command);
 		else
 			System.out.println(message + command);
 	}
 	
 	public void newEmptyKitDone() {
 		sendMessage("cca cnf");
 	}
 	
 	public void moveEmptyKitToSlotDone() {
 		sendMessage("kra cnf");
 	}
 
 	public void moveKitToInspectionDone() {
 		sendMessage("kra cnf");
 	}
 	
 	public void takePictureOfInspectionSlotDone() {
 		sendMessage("va cnf");
 	}
 
 	public void dumpKitAtInspectionDone() {
 		sendMessage("kra cnf");
 	}
 
 	public void moveKitFromInspectionToConveyorDone() {
 		sendMessage("kra cnf");
 	}
 
 	public void exportKitDone() {
 		sendMessage("ca cnf");
 	}
 	
 	public void cameraFlashDone() {
 		sendMessage("FILLER");
 	}
 
 	public void gantryRobotArrivedAtPickup() {
 		sendMessage("ga cnf");
 	}
 
 	public void gantryRobotArrivedAtFeederForDropoff() {
 		sendMessage("ga cnf");
 	}
 
 	public void gantryRobotArrivedAtFeederForPickup()
 	{
 		sendMessage("ga cnf");
 	}
 	
 	public void partsRobotArrivedAtNest() {
 		sendMessage("FILLER");
 	}
 
 	public void partsRobotArrivedAtStation() {
 		sendMessage("FILLER");
 	}
 
 	public void partsRobotArrivedAtCenter() {
 		sendMessage("FILLER");
 	}
 
 	public void feedLaneDone(int laneNum){
 		sendMessage("FILLER");
 	}
 	
 	public void purgeTopLaneDone(int feederNum) {
 		sendMessage("fa cnf " + feederNum);
 	}
 	
 	public void purgeBottomLaneDone(int feederNum) {
 		sendMessage("fa cnf " + feederNum);
 	}
 	
 	public void purgeFeederDone(int feederNum) {
 		sendMessage("fa cnf " + feederNum);
 	}
 	
 	public void startFeederDone(int feederNum) {
 		sendMessage("fa cnf " + feederNum);
 	}
 	
 	public void stopFeederDone(int feederNum) {
 		sendMessage("fa cnf " + feederNum);
 	}
 	
 	public void switchFeederLaneDone(int feederNum) {
 		sendMessage("fa cnf " + feederNum);
 	}
 	
 	public GraphicKittingStation getStation() {
 		return station;
 	}
 	public GraphicConveyorBelt getBelt() {
 		return belt;
 	}
 	public ArrayList<GraphicNest> getNest(){
 		return nests;
 	}
 	public GraphicLaneManager getLane(int index) {
 		return lane[index];
 	}
 	
 	public void paint(Graphics g) {
 		g.setColor(new Color(200, 200, 200));
 		g.fillRect(0, 0, getWidth(), getHeight());
 		
 		if (isKitAssemblyManager || isFactoryProductionManager) {
 			belt.paint(g);
 			station.paint(g);
 			kitRobot.paint(g);
 		}
 		
 		if (isLaneManager || isFactoryProductionManager) {
 			for (int i = 0; i < lane.length; i++) {
 				//if (lane[i] != null)
 				lane[i].paintLane(g);
 			}
 		}
 		
 		if (isLaneManager || isFactoryProductionManager || isGantryRobotManager) {
 			for (int i = 0; i < lane.length; i++) {
 				//if (lane[i] != null)
 				lane[i].paintFeeder(g);
 			}
 		}
 		
 		// Parts robot client
 		// Draw the nests
 		if (isLaneManager || isFactoryProductionManager)
 		for(int i = 0; i < nests.size(); i++) {
 			GraphicNest currentNest = nests.get(i);
 			currentNest.paint(g);
 		}
 		
 		if(isLaneManager || isFactoryProductionManager) {
 			if(flashCounter >= 0)
 			{
 				int flashX = nests.get(flashFeederIndex*2).getX();
 				int flashY = nests.get(flashFeederIndex*2).getY();
 				//System.out.println("==="+flashX+" "+flashY);
 				g.drawImage(flashImage, flashX, flashY, null);
 				flashX = nests.get(flashFeederIndex*2+1).getX();
 				flashY = nests.get(flashFeederIndex*2+1).getY();
 				g.drawImage(flashImage, flashX, flashY, null);
 				flashCounter --;
 			}
 		}
 		
 		// Draw the parts robot
 		if (isFactoryProductionManager) {
 			final Graphics2D g3 = (Graphics2D)g.create();
 			g3.rotate(Math.toRadians(360-partsRobot.getAngle()), partsRobot.getX()+partsRobot.getImageWidth()/2, partsRobot.getY()+partsRobot.getImageHeight()/2);
 			// Draw items partsRobot is carrying
 			partsRobot.paint(g3);
 			g3.dispose();
 		}
 		
 		// Draw the Gantry Robot
 		if (isGantryRobotManager || isFactoryProductionManager) {
 			final Graphics2D g4 = (Graphics2D)g.create();
 			g4.rotate(Math.toRadians(360-gantryRobot.getAngle()), gantryRobot.getX()+gantryRobot.getImageWidth()/2, gantryRobot.getY()+gantryRobot.getImageHeight()/2);
 			gantryRobot.paint(g4);
 			// Draw bin gantryRobot is carrying
 			
 			g4.dispose();
 		}
 	}
 	
 	public boolean isKitAssemblyManager() {
 		return isKitAssemblyManager;
 	}
 	
 	public boolean isLaneManager() {
 		return isLaneManager;
 	}
 	
 	public boolean isGantryRobotManager() {
 		return isGantryRobotManager;
 	}
 	
 	public boolean isFactoryProductionManager() {
 		return isFactoryProductionManager;
 	}
 	
 	public void actionPerformed(ActionEvent arg0) {
 	// TODO Auto-generated method stub
 			
 	}
 
 }
