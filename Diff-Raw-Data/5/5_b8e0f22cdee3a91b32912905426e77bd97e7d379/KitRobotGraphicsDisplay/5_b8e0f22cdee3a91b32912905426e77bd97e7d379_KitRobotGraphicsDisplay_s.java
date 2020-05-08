 package DeviceGraphicsDisplay;
 
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.geom.AffineTransform;
 import java.util.ArrayList;
 
 import javax.swing.JComponent;
 
 import Networking.Client;
 import Networking.Request;
 import Utils.Constants;
 import Utils.Location;
 import factory.KitConfig;
 
 public class KitRobotGraphicsDisplay extends DeviceGraphicsDisplay {
 
 	int animCount;
 	boolean reverse;
 	boolean invisibleCloud;
 	int seq;
 	Image img;
 	// Messages
 
 	public enum Message {
 		sendGoodConveyorDoneMessage, sendStand1DoneMessage, sendStand2DoneMessage, sendInspectionDoneMessage
 	};
 
 	Message sendMessage;
 
 	// Positions
 	public enum Position {
 		conveyorPosition, goodConveyorPosition, inspectionPosition, location1Position, location2Position
 	};
 
 	Position position;
 
 	// Commands
 	public enum Command {
 		moveToConveyor, moveToGoodConveyor, moveToInspectionStand, moveToLocation1, moveToLocation2
 	};
 
 	Command moveToInitialPosition; // initial command
 	Command moveToFinalPosition; // final command
 	Command moveToPosition; // current command
 
 	boolean initialJob;
 	boolean finalJob;
 	boolean returnJob;
 	boolean jobIsDone;
 
 	Location kitLeaveLocation;
 	Location kitEnterLocation;
 	Location inspectionLocation;
 	Location location1;
 	Location location2;
 	Location kitRobotLocation;
 	
 	int velocityDivide;
 
 	public void setVelocityDivide(int velocityDivide) {
 		this.velocityDivide = velocityDivide;
 	}
 
 	int stepCountDown;
 
 	int kitRobotPositionX;
 	int kitRobotPositionY;
 
 	int kitMagicX;
 	int kitMagicY;
 	
 	int xStep;
 	int yStep;
 	
 	AffineTransform trans;
 
 	Client kitRobotClient;
 	ArrayList<KitGraphicsDisplay> kits = new ArrayList<KitGraphicsDisplay>();
 	KitGraphicsDisplay currentKit;
 
 	public KitRobotGraphicsDisplay(Client cli) {
 
 		invisibleCloud = true;
 		kitRobotLocation = Constants.KIT_ROBOT_LOC;
 		kitRobotClient = cli;
 
 		moveToInitialPosition = Command.moveToInspectionStand;
 		moveToFinalPosition = Command.moveToConveyor;
 		position = Position.conveyorPosition;
 		initialJob = false;
 		finalJob = false;
 
 		sendMessage = Message.sendGoodConveyorDoneMessage;
 
 		returnJob = false;
 
 		jobIsDone = true;
 		
 		animCount = 70;
 		reverse = false;
 		seq = 1;
 
 		velocityDivide = Constants.KIT_VELOCITY_DIVIDE;
 
 		kitRobotPositionX = Constants.KIT_ROBOT_LOC.getX()
 				+ kitRobotClient.getOffset();
 		kitRobotPositionY = Constants.KIT_ROBOT_LOC.getY();
 		
 		kitMagicX = Constants.KIT_CONVEYOR_LOC.getX();
 		kitMagicY = Constants.KIT_CONVEYOR_LOC.getY();
 		
 		kitLeaveLocation = Constants.KIT_LEAVE_LOC;
 		kitEnterLocation = Constants.KIT_CONVEYOR_LOC;
 		inspectionLocation = Constants.INSPECTION_LOC;
 		location1 = Constants.STAND1_LOC;
 		location2 = Constants.STAND2_LOC;
 		
 		currentKit = new KitGraphicsDisplay(cli);
 
 	}
 
 	public void setCommands(Command initialCommand, Command finalCommand) {
 		jobIsDone = false;
 		initialJob = true;
 		this.moveToInitialPosition = initialCommand;
 		this.moveToFinalPosition = finalCommand;
 	}
 
 	// begin paths
 	public void InspectionToLocation1() {
 		setCommands(Command.moveToInspectionStand, Command.moveToLocation1);
 		moveToInitialOrFinal();
 	}
 
 	public void InspectionToLocation2() {
 		setCommands(Command.moveToInspectionStand, Command.moveToLocation2);
 		moveToInitialOrFinal();
 	}
 
 	public void InspectionToGoodConveyor() {
 		setCommands(Command.moveToInspectionStand, Command.moveToGoodConveyor);
 		moveToInitialOrFinal();
 	}
 
 	public void ConveyorToLocation1() {
 		setCommands(Command.moveToConveyor, Command.moveToLocation1);
 		moveToInitialOrFinal();
 	}
 
 	public void ConveyorToLocation2() {
 		setCommands(Command.moveToConveyor, Command.moveToLocation2);
 		moveToInitialOrFinal();
 	}
 
 	public void Location1ToInspectionStand() {
 		setCommands(Command.moveToLocation1, Command.moveToInspectionStand);
 		moveToInitialOrFinal();
 	}
 
 	public void Location2ToInspectionStand() {
 		setCommands(Command.moveToLocation2, Command.moveToInspectionStand);
 		moveToInitialOrFinal();
 	}
 
 	public void InspectionStandToGoodConveyor() {
 		setCommands(Command.moveToInspectionStand, Command.moveToGoodConveyor);
 		moveToInitialOrFinal();
 	}
 
 	public void Location1ToLocation2() {
 		setCommands(Command.moveToLocation1, Command.moveToLocation2);
 		moveToInitialOrFinal();
 	}
 
 	// end paths
 	
 	/*
 	 * sets the rotation configurations based on the commands
 	 */
 	public void moveToInitialOrFinal() {
 
 		if (initialJob) {
 			moveToPosition = moveToInitialPosition;
 		} else if (finalJob) {
 			moveToPosition = moveToFinalPosition;
 		} else if (returnJob) {
 			moveToPosition = Command.moveToConveyor;
 		}
 
 		if (position.equals(Position.conveyorPosition)) {
 			if (moveToPosition.equals(Command.moveToInspectionStand)) {
 				setVectorConfigurations(inspectionLocation.getX() -kitEnterLocation.getX() , inspectionLocation.getY() -kitEnterLocation.getY() , Position.inspectionPosition);
 			} else if (moveToPosition.equals(Command.moveToLocation1)) {
 				setVectorConfigurations(location1.getX() -kitEnterLocation.getX() , location1.getY() -kitEnterLocation.getY() , Position.location1Position);
 			} else if (moveToPosition.equals(Command.moveToLocation2)) {
 				setVectorConfigurations(location2.getX() -kitEnterLocation.getX() , location2.getY() -kitEnterLocation.getY() , Position.location2Position);
 			} else if (moveToPosition.equals(Command.moveToGoodConveyor)) {
 				setVectorConfigurations(kitLeaveLocation.getX() -kitEnterLocation.getX()  , kitLeaveLocation.getY() -kitEnterLocation.getY()  , Position.goodConveyorPosition);
 			} else {
 				setVectorConfigurations(0,0, Position.conveyorPosition);
 			}
 		} else if (position.equals(Position.inspectionPosition)) {
 			if (moveToPosition.equals(Command.moveToLocation1)) {
 				setVectorConfigurations(location1.getX() -inspectionLocation.getX() , location1.getY() -inspectionLocation.getY() , Position.location1Position);
 			} else if (moveToPosition.equals(Command.moveToLocation2)) {
 				setVectorConfigurations(location2.getX() -inspectionLocation.getX() , location2.getY() -inspectionLocation.getY() , Position.location2Position);
 			} else if (moveToPosition.equals(Command.moveToConveyor)) {
 				setVectorConfigurations(kitEnterLocation.getX() -inspectionLocation.getX() , kitEnterLocation.getY() -inspectionLocation.getY() , Position.conveyorPosition);
 			} else if (moveToPosition.equals(Command.moveToGoodConveyor)) {
 				setVectorConfigurations(kitLeaveLocation.getX() -inspectionLocation.getX() , kitLeaveLocation.getY() -inspectionLocation.getY() , Position.goodConveyorPosition);
 			} else {
 				setVectorConfigurations(0,0, Position.inspectionPosition);
 			}
 		} else if (position.equals(Position.location1Position)) {
 			if (moveToPosition.equals(Command.moveToLocation2)) {
 				setVectorConfigurations(location2.getX() - location1.getX() , location2.getY() -location2.getY() , Position.location2Position);
 			} else if (moveToPosition.equals(Command.moveToConveyor)) {
 				setVectorConfigurations(kitEnterLocation.getX() -location1.getX() , kitEnterLocation.getY() -location1.getY() , Position.conveyorPosition);
 			} else if (moveToPosition.equals(Command.moveToInspectionStand)) {
 				setVectorConfigurations(inspectionLocation.getX() -location1.getX() , inspectionLocation.getY() -location1.getY() , Position.inspectionPosition);
 			} else if (moveToPosition.equals(Command.moveToGoodConveyor)) {
 				setVectorConfigurations(kitLeaveLocation.getX() -location1.getX() , kitLeaveLocation.getY() -location1.getY() , Position.goodConveyorPosition);
 			} else {
 				setVectorConfigurations(0,0, Position.location1Position);
 			}
 		} else if (position.equals(Position.location2Position)) {
 			if (moveToPosition.equals(Command.moveToConveyor)) {
 				setVectorConfigurations(kitEnterLocation.getX() -location2.getX() , kitEnterLocation.getY() -location2.getY() , Position.conveyorPosition);
 			} else if (moveToPosition.equals(Command.moveToInspectionStand)) {
 				setVectorConfigurations(inspectionLocation.getX() -location2.getX() , inspectionLocation.getY() -location2.getY() , Position.inspectionPosition);
 			} else if (moveToPosition.equals(Command.moveToLocation1)) {
 				setVectorConfigurations(location1.getX() -location2.getX() , location1.getY() -location2.getY() , Position.location1Position);
 			} else if (moveToPosition.equals(Command.moveToGoodConveyor)) {
 				setVectorConfigurations(kitEnterLocation.getX() -location2.getX() , kitEnterLocation.getY() -location2.getY() , Position.goodConveyorPosition);
 			} else {
 				setVectorConfigurations(0,0, Position.location2Position);
 			}
 		} else if (position.equals(Position.goodConveyorPosition)) {
 			if (moveToPosition.equals(Command.moveToConveyor)) {
 				setVectorConfigurations(kitEnterLocation.getX() -kitLeaveLocation.getX() , kitEnterLocation.getY() -kitLeaveLocation.getY() , Position.conveyorPosition);
 			} else if (moveToPosition.equals(Command.moveToInspectionStand)) {
 				setVectorConfigurations(inspectionLocation.getX() -kitLeaveLocation.getX() , inspectionLocation.getY() -kitLeaveLocation.getY() , Position.inspectionPosition);
 			} else if (moveToPosition.equals(Command.moveToLocation1)) {
 				setVectorConfigurations(location1.getX() -kitLeaveLocation.getX() , location1.getY() -kitLeaveLocation.getY() , Position.location1Position);
 			} else if (moveToPosition.equals(Command.moveToLocation2)) {
 				setVectorConfigurations(location2.getX() -kitLeaveLocation.getX() , location2.getY() -kitLeaveLocation.getY() , Position.location2Position);
 			} else {
 				setVectorConfigurations(0,0, Position.goodConveyorPosition);
 			}
 		}
 
 	}
 
 	/**
 	 * set the rotation configurations. degreeCountdown-increments till it
 	 * reaches 0. designates how much to rotate position- sets the position in
 	 * the statemachine logic
 	 */
 	public void setVectorConfigurations(int xStep, int yStep  , Position position) {
 		this.xStep = xStep/velocityDivide;
 		this.yStep = yStep/velocityDivide;
 		if(xStep==0 && yStep ==0)
 		{
 			this.stepCountDown = 0;
 		}
 		else
 		{
 			this.stepCountDown = velocityDivide;
 		}
 		this.position = position;
 	}
 
 	/**
 	 * sends the done messages when the degreecountdown reaches 0 the done
 	 * messages are based on what position the kit robot reaches
 	 */
 	public void setDoneMessage() {
 		if (position.equals(Position.location1Position)){
 			this.sendMessage = Message.sendStand1DoneMessage;
 		} else if(position.equals(Position.location2Position)) {
 			this.sendMessage = Message.sendStand2DoneMessage;
 		} else if (position.equals(Position.goodConveyorPosition)) {
 			this.sendMessage = Message.sendGoodConveyorDoneMessage;
 		} else if (position.equals(Position.inspectionPosition)) {
 			this.sendMessage = Message.sendInspectionDoneMessage;
 		}
 	}
 
 	public void sendDoneMessage() {
 		if (this.sendMessage.equals(Message.sendStand1DoneMessage)) {
 			kits.remove(currentKit);
 			kitRobotClient.sendData(new Request(
 					Constants.KIT_ROBOT_ON_STAND1_DONE,
 					Constants.KIT_ROBOT_TARGET, currentKit.getKitConfig()));
 		} else if(this.sendMessage.equals(Message.sendStand2DoneMessage)){ 
 			kits.remove(currentKit);
 			kitRobotClient.sendData(new Request(Constants.KIT_ROBOT_ON_STAND2_DONE,
 					Constants.KIT_ROBOT_TARGET, currentKit.getKitConfig()));
 		} else if (this.sendMessage.equals(Message.sendGoodConveyorDoneMessage)) {
 			kits.remove(currentKit);
 			currentKit.setPosition(2);
 			kitRobotClient.sendData(new Request(
 					Constants.KIT_ROBOT_ON_CONVEYOR_DONE,
 					Constants.KIT_ROBOT_TARGET, currentKit.getKitConfig()));
 		} else if (this.sendMessage.equals(Message.sendInspectionDoneMessage)) {
 			kits.remove(currentKit);
 			kitRobotClient.sendData(new Request(
 					Constants.KIT_ROBOT_ON_INSPECTION_DONE,
 					Constants.KIT_ROBOT_TARGET, currentKit.getKitConfig()));
 		}
 		
 		invisibleCloud = true;
 
 	}
 
 	/**
 	 * changes which job it's doing based on the booleans
 	 */
 	public void checkDegrees() {
 
 		if (stepCountDown == 0) {
 			if (initialJob) {
 				initialJob = false;
 				finalJob = true;
 				moveToInitialOrFinal();
 			} else if (finalJob) {
 				finalJob = false;
 				returnJob = true;
 				setDoneMessage();
 				moveToInitialOrFinal();
 			} else if (returnJob) {
 				returnJob = false;
 				jobIsDone = true;
 				sendDoneMessage();
 			}
 
 		}
 	}
 
 	public void setKitConfigurations(KitGraphicsDisplay kit,int position) {
 		currentKit=kit;
 		currentKit.setPosition(position);
 	}
 
 	/*
 	 * receives data and does stuff based on the request command (non-Javadoc)
 	 * 
 	 * @see
 	 * DeviceGraphicsDisplay.DeviceGraphicsDisplay#receiveData(Networking.Request
 	 * )
 	 */
 	public void receiveData(Request req) {
 		String command = req.getCommand();
 		String target = req.getTarget();
 		Object obj = req.getData();
 
 		if (target.equals(Constants.KIT_ROBOT_TARGET)) {
 			if (command
 					.equals(Constants.KIT_ROBOT_DISPLAY_PICKS_CONVEYOR_TO_LOCATION1)) {
 				
 				KitGraphicsDisplay tempKit = new KitGraphicsDisplay(new KitConfig("DummyKit"));
 				tempKit.setLocation(kitEnterLocation);
 				setKitConfigurations(tempKit, 1 );
 				kits.add(currentKit);
 				ConveyorToLocation1();
 			} else if (command
 					.equals(Constants.KIT_ROBOT_DISPLAY_PICKS_CONVEYOR_TO_LOCATION2)) {
 				KitGraphicsDisplay tempKit = new KitGraphicsDisplay(new KitConfig("DummyKit"));
 				tempKit.setLocation(kitEnterLocation);
 				setKitConfigurations(tempKit, 2 );
 				kits.add(currentKit);
 				ConveyorToLocation2();
 			} else if (command
 					.equals(Constants.KIT_ROBOT_DISPLAY_PICKS_INSPECTION_TO_GOOD_CONVEYOR)) {
 				KitGraphicsDisplay tempKit = new KitGraphicsDisplay((KitConfig)obj);
 				tempKit.setLocation(inspectionLocation);
 				setKitConfigurations(tempKit, 3 );
 				kits.add(currentKit);
 				InspectionToGoodConveyor();
 			} else if (command
 					.equals(Constants.KIT_ROBOT_DISPLAY_PICKS_LOCATION1_TO_INSPECTION)) {
 				KitGraphicsDisplay tempKit = new KitGraphicsDisplay((KitConfig)obj);
 				KitConfig kitConfig=(KitConfig)obj;
 				tempKit.setLocation(kitConfig.getLocation());
 				setKitConfigurations(tempKit, 0);
 				kits.add(currentKit);
 				Location1ToInspectionStand();
 			} else if (command
 					.equals(Constants.KIT_ROBOT_DISPLAY_PICKS_LOCATION2_TO_INSPECTION)) {
 				KitGraphicsDisplay tempKit = new KitGraphicsDisplay((KitConfig)obj);
 				tempKit.setLocation(location2);
 				setKitConfigurations(tempKit, 0 );
 				kits.add(currentKit);
 				Location2ToInspectionStand();
 			} else if (command
 					.equals(Constants.KIT_ROBOT_DISPLAY_PICKS_INSPECTION_TO_LOCATION1)) {
 				KitGraphicsDisplay tempKit = new KitGraphicsDisplay((KitConfig)obj);
 				tempKit.setLocation(inspectionLocation);
 				setKitConfigurations(tempKit,1);
 				kits.add(currentKit);
 				InspectionToLocation1();
 			}  else if(command.equals(Constants.KIT_ROBOT_DISPLAY_PICKS_INSPECTION_TO_LOCATION2)){
 				KitGraphicsDisplay tempKit = new KitGraphicsDisplay((KitConfig)obj);
 				tempKit.setLocation(inspectionLocation);
 				setKitConfigurations(tempKit,2);
 				kits.add(currentKit);
 				InspectionToLocation2();
 			}
 			
 			invisibleCloud = false;
 
 		}
 
 	}
 
 	/**
 	 * rotates the kit robot if you are doing a job
 	 */
 	public void doJob() {
 		if (!jobIsDone) {
 			kitMagicX += this.xStep;
 			kitMagicY += this.yStep;
 			stepCountDown -= 1;
 			if(finalJob)
 			{
 				currentKit.setLocation(new Location(currentKit.getLocation().getX() + this.xStep, currentKit.getLocation().getY()+this.yStep));
 			}
 			// System.out.println("currentDegree: " + currentDegree);
 		}
 	}
 
 	/*
 	 * draws the kit robot and kit (non-Javadoc)
 	 * @see
 	 * DeviceGraphicsDisplay.DeviceGraphicsDisplay#draw(javax.swing.JComponent,
 	 * java.awt.Graphics2D)
 	 */
 	public void draw(JComponent c, Graphics2D g) {
 		checkDegrees();
 		doJob();
 		if(!invisibleCloud)
 		{
 			g.drawImage(Constants.KIT_CLOUD_IMAGE, kitMagicX, kitMagicY, null);
 		}
 		
 		drawtheKits(c, g);
 		//g.drawImage(Constants.KIT_ROBOT_IMAGE_FLICKER, kitRobotPositionX, kitRobotPositionY, c);
 		animateRobot(g,c);
 	}
 
 	/*
 	 * draws the kits in the kit robot area
 	 */
 	public void drawtheKits(JComponent c, Graphics2D g) {
 		for (int i = 0; i < kits.size(); i++) {
 			kits.get(i).drawKit(c,g);
 		}
 	}
 	
 	public void animateRobot(Graphics2D g, JComponent c) {
			if (animCount % 7 == 0) {
 				//// TODO: change 1 to parttypenum later;
 				sequenceIncrease();
 			} else if (animCount < 1) {
 				animCount = 76;
 			}
 			
			img = Toolkit.getDefaultToolkit().getImage(Constants.KIT_ROBOT_IMAGE + seq + ".png");
 			animCount--;
 			g.drawImage(img, kitRobotPositionX, kitRobotPositionY, c);
 	}
 	
 	public void sequenceIncrease() {
 	    if (reverse == false && seq == 1) {
 	    	seq = 2;
 	    }
 	    else if (reverse == false && seq == 2){
 	    	seq = 3;
 	    }
 	    else if (reverse == false && seq == 3) {
 	    	seq = 2;
 	    	reverse = true;
 	    }
 	    else if (reverse == false && seq == 4) {
 	    	seq = 1;
 	    }
 	    else if (reverse == false && seq == 5) {
 	    	seq = 4;
 	    }
 	    else if (reverse == true && seq == 1) {
 		    seq = 4;
 	    }
 	    else if (reverse == true && seq == 2){
 		    seq = 1;
 	    }
 	    else if (reverse == true && seq == 3){
 	    	seq = 2;
 	    }
 	    else if (reverse == true && seq == 4){
 	    	seq = 5;
 	    }
 	    else if (reverse == true && seq == 5){
 	    	seq = 4;
 	    	reverse = false;
 	    }
 	}
 
 	@Override
 	public void setLocation(Location newLocation) {
 		location = newLocation;
 	}
 
 }
