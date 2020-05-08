 package AP2DX.planner;
 
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.concurrent.TimeUnit;
 
 import sun.reflect.generics.tree.BottomSignature;
 
 import AP2DX.AP2DXBase;
 import AP2DX.AP2DXMessage;
 import AP2DX.Message;
 import AP2DX.Module;
 import AP2DX.specializedMessages.ActionMotorMessage;
 import AP2DX.specializedMessages.ActionMotorMessage.ActionType;
 import AP2DX.specializedMessages.ClearMessage;
 import AP2DX.specializedMessages.InsSensorMessage;
 import AP2DX.specializedMessages.OdometrySensorMessage;
 import AP2DX.specializedMessages.ResetMessage;
 import AP2DX.specializedMessages.SonarSensorMessage;
 
 public class Program extends AP2DXBase {
 
 	/**
 	 * threshold for turning in angle direction, robot should drive in a
 	 * direction between destinationAngle - ANGLEUNCERTAIN and destinationAngle
 	 * + ANGLEUNCERTAIN
 	 */
 	private static final double ANGLEUNCERTAIN = Math.toRadians(5);
 
 	/**
 	 * This much should two sonardata results of one side sensor differ,
 	 * before it will check the hole
 	 */
 	private static final double TURNTHRESHOLD = 1.0;
 	
 	/**
 	 * Mid four sonar sensors should  be at least this deep to drive into hole
 	 */
 	private static final double NEEDEDDEPTH = 0.5;
 
 	/**
 	 * drive this much past a hole before restarting scan of enviroment
 	 */
 	private static final double PASSHOLEDISTANCE = 0.3;
 
 	/**
 	 * drive this much past a hole before scanning it
 	 */
 	private static final double PASSHOLECORNER = 0.3;
 
 	/**
 	 * If distance from bot to wall (side sonar sensors) is larger than this,
 	 * don't do hole checking.
 	 */
 	private static final double IGNOREDISTANCE = 0.8;
 
 	/**
 	 * if movement is only this much between two INS data, then it is 'no movement'
 	 */
 	private static final double MOVEMENTTHRESHOLD = 0.2;
 
 	/**
 	 * Did not move after this many INS data? Then action has to be taken.
 	 */
 	private static final int NOMOVECOUNT = 50;
 
 	/**
 	 * If stuck, drive this much back
 	 */
 	private static final double BACKWARDDISTANCE = 0.4;
 
 	private InsLocationData locData;
 
 	private double travelDistance;
 
 	private double distanceGoal;
 
 	/** Tho start the first drive after spawning */
 	private boolean firstMessage = false;
 
 	private double startAngle;
 
 	private double currentAngle;
 
 	private double destinationAngle;
 
 	private boolean startedTurning = false;
 
 	/** permission to save sonardata for direction determining */
 	private boolean sonarPermission = false;
 
 	/** will contain sonar scan data for determining direction */
 	private double[] sonarData;
 	private double[] lastSonarData;
 
 	private boolean doHoleScan = false;
 
 	private int lastDirection;
 
 	private boolean startedTurningBack;
 
 	private boolean waitPassingHole;
 
 	private boolean passHole;
 
 	private boolean stopBot;
 
 	private boolean cruising;
 
 	private boolean reverse;
 	
 	private int noMovementCount = 0;
 
 	private boolean stuck; 
 
 	/**
 	 * Entrypoint of planner
 	 */
 	public static void main(String[] args) {
 		new Program();
 	}
 
 	/**
 	 * constructor
 	 */
 	public Program() {
 		super(Module.PLANNER); // explicitly calls base constructor
 
 		System.out.println(" Running Planner... ");
 	}
 
 	@Override
 	public ArrayList<AP2DXMessage> componentLogic(Message message) {
 		ArrayList<AP2DXMessage> messageList = new ArrayList<AP2DXMessage>();
 		// System.out.println("Received message " + message.getMessageString());
 		// System.out.println(String.format("In Queue: %s",
 		// this.getReceiveQueue().size()));
 
 		switch (message.getMsgType()) {
 		case AP2DX_PLANNER_STOP:
 
 			startAngle = currentAngle;
 
 			sonarPermission = true;
 
 			messageList.add(new ResetMessage(IAM, Module.REFLEX));
 
 			stopBot = true;
 			stuck = false;
 			reverse = false;
 			
 			break;
 		case AP2DX_SENSOR_INS:
 
 			InsSensorMessage msg = (InsSensorMessage) message;
 			double[] loc = msg.getLocation();
 			double[] ori = msg.getOrientation();
 
 			if (locData == null) {
 
 				locData = new InsLocationData(loc, ori);
 			} else {
 				locData.setLocationData(loc, ori);
 
 				setTravelDistance(getTravelDistance()
 						+ locData.travelDistance());
 
				if (locData.travelDistance() < MOVEMENTTHRESHOLD && !stuck && !reverse) {
 					noMovementCount++;
 				} else {
 					noMovementCount = 0;
 				}
 				
 				if (getDistanceGoal() > 0) {
 					if (getTravelDistance() >= getDistanceGoal() && passHole && !reverse) {
 						System.out.println("Passed the hole (we hope)");
 						passHole = false;
 						doHoleScan = false;
 						cruising = true;
 						
 						setDistanceGoal(0);
 						
 						
 					} else if (getTravelDistance() >= getDistanceGoal() && waitPassingHole && !reverse) {
 						System.out.println("Maybe infront of hole");
 
 						waitPassingHole = false;
 
 						startedTurning = true;
 						
 						messageList.add(new ResetMessage(IAM, Module.REFLEX));
 						
 						AP2DXMessage msg7 = new ActionMotorMessage(IAM,
 								Module.REFLEX,
 								ActionMotorMessage.ActionType.TURN, lastDirection);
 						msg7.compileMessage();
 						messageList.add(msg7);
 						
 						setDistanceGoal(0);
 				}
 				else if (reverse && (getTravelDistance() >= getDistanceGoal())) {
 				//else if (reverse && (getDistanceGoal() >= getTravelDistance())) {
 						System.out.println("Wend backward for distanceGoal meters...");
 						startAngle = currentAngle;
 
 						sonarPermission = true;
 
 						messageList.add(new ResetMessage(IAM, Module.REFLEX));
 
 						Random rand = new Random();
 						
 						int direction = (rand.nextBoolean() ? 1 : -1);
 						
 						startAngle = currentAngle;
 						destinationAngle = startAngle + direction * 0.75 * Math.PI ;
 						
 						if (destinationAngle < -Math.PI) {
 							destinationAngle = Math.PI + (destinationAngle + Math.PI);
 						} else if (destinationAngle > Math.PI) {
 							destinationAngle = -Math.PI + (destinationAngle - Math.PI);
 						}
 						
 						AP2DXMessage msg7 = new ActionMotorMessage(IAM,
 								Module.REFLEX,
 								ActionMotorMessage.ActionType.TURN, direction);
 						msg7.compileMessage();
 						messageList.add(msg7);
 						
 						stopBot = true;
 						stuck = true;
 						reverse = false;
 					}
 				}
 				
 				if (noMovementCount > NOMOVECOUNT && !stopBot && !reverse) {
 					System.out.println("No movement, going backward");
 					
 					reverse = true;
 					cruising = false;
 					passHole = false; 
 					
 					AP2DXMessage msg7 = new ActionMotorMessage(IAM,
 							Module.REFLEX,
 							ActionMotorMessage.ActionType.BACKWARD, 10.0);
 					msg7.compileMessage();
 					messageList.add(msg7);
 					
 					setDistanceGoal(BACKWARDDISTANCE);
 					setTravelDistance(0.0);
 					
 					noMovementCount = 0;
 				}
 			}
 			break;
 		case AP2DX_SENSOR_SONAR:
 			SonarSensorMessage msgs = (SonarSensorMessage) message;
 
 			lastSonarData = sonarData;
 			sonarData = msgs.getRangeArray();
 			
 			if (stopBot) {
 				System.out.println("Bot stopped!");
 				/*
 				 * First field is the value of the sonar, second field is the index
 				 * of the value in sonarData
 				 */
 				double longestSonar[] = { 0, 0 };
 
 				// IMPORTANT: do or don't use outer most sonar sensors?
 				for (int i = 1; i < sonarData.length-1; i++) {
 					if (sonarData[i] > longestSonar[0]) {
 						longestSonar[0] = sonarData[i];
 						longestSonar[1] = i;
 					}
 				}
 
 				/*
 				 * Decides on the last acquired sonarData if to turn right or left
 				 * Default is right
 				 */
 				if (longestSonar[1] < Math.round(sonarData.length / 2)) {
 					AP2DXMessage msgt = new ActionMotorMessage(IAM, Module.REFLEX,
 							ActionMotorMessage.ActionType.TURN, -1);
 					msgt.compileMessage();
 					messageList.add(msgt);
 				} else {
 					AP2DXMessage msgt = new ActionMotorMessage(IAM, Module.REFLEX,
 							ActionMotorMessage.ActionType.TURN, 1);
 					msgt.compileMessage();
 					messageList.add(msgt);
 				}
 				
 				stopBot = false;
 			}
 			else if (sonarPermission && !reverse) {
 				System.out.println("Having sonarPermissions");
 				boolean[] space = new boolean[4];
 				for (int i = 2; i < 6; i++) {
 					space[i-2] = (sonarData[i] >= NEEDEDDEPTH);  
 				}
 				boolean succeed = true;
 				for (boolean hole : space) {
 					if (hole == false) {
 						succeed = false;
 						break;
 					}
 				}
 				if (succeed) {
 					AP2DXMessage msg5 = new ClearMessage(IAM, Module.REFLEX);
 					msg5.compileMessage();
 					messageList.add(msg5);
 
 					AP2DXMessage msg6 = new ActionMotorMessage(IAM,
 							Module.REFLEX,
 							ActionMotorMessage.ActionType.FORWARD, 10.0);
 					msg6.compileMessage();
 					messageList.add(msg6);
 
 					sonarPermission = false;
 				}
 			} else if (doHoleScan && !reverse) {
 				System.out.println("Doing a holescan");
 				doHoleScan = false;
 				
 				boolean[] space = new boolean[4];
 				for (int i = 2; i < 6; i++) {
 					space[i-2] = (sonarData[i] >= NEEDEDDEPTH);  
 				}
 				boolean succeed = true;
 				for (boolean hole : space) {
 					if (hole == false) {
 						succeed = false;
 						break;
 					}
 				}
 				if (succeed) {
 					System.out.println("Yay a hole");
 					
 					startedTurningBack = false;
 					startedTurning = false;
 					
 					AP2DXMessage msg8 = new ClearMessage(IAM, Module.REFLEX);
 					msg8.compileMessage();
 					messageList.add(msg8);
 					
 					AP2DXMessage msg5 = new ActionMotorMessage(IAM, Module.REFLEX,
 							ActionMotorMessage.ActionType.FORWARD, 10.0);
 					msg5.compileMessage();
 					messageList.add(msg5);
 				}
 				else {
 					System.out.println("Not a nice hole, going back");
 					
 					startedTurningBack = true;
 					startedTurning = false;
 					
 					destinationAngle = startAngle;
 					startAngle = currentAngle;
 					
 					AP2DXMessage msg5 = new ActionMotorMessage(IAM, Module.REFLEX,
 							ActionMotorMessage.ActionType.TURN, -lastDirection);
 					msg5.compileMessage();
 					messageList.add(msg5);
 					
 					
 				}
 			}
 			else if(cruising && !reverse) {
 				System.out.println("Just cruising");
 				// 0 = no hole
 				int hole = 0;
 				
 				//hole on the left
 				if (lastSonarData[0] < IGNOREDISTANCE && (sonarData[0] - lastSonarData[0]) >= TURNTHRESHOLD) {
 					hole = -1;
 				}
 				//hole on the right
 				else if (lastSonarData[lastSonarData.length-1] < IGNOREDISTANCE && (sonarData[sonarData.length-1] - lastSonarData[lastSonarData.length-1]) >= TURNTHRESHOLD) {
 					hole = 1;
 				}
 				
 				// there is a hole
 				if (hole != 0) {
 					System.out.println("Hey, is that a hole?");
 					
 					waitPassingHole = true;
 					cruising = false;
 					
 					startAngle = currentAngle;
 					
 					
 					if (hole == -1) {
 						destinationAngle = startAngle - (0.5*Math.PI);
 						if (destinationAngle < -Math.PI) {
 							destinationAngle = Math.PI + (destinationAngle + Math.PI);
 						}
 					}
 					else {
 						destinationAngle = startAngle + (0.5*Math.PI);
 						if (destinationAngle > Math.PI) {
 							destinationAngle = -Math.PI + (destinationAngle - Math.PI);
 						}
 					}
 					
 					lastDirection = hole;
 					
 					waitPassingHole = true;
 					
 					setDistanceGoal(PASSHOLECORNER);
 					setTravelDistance(0);
 				}
 			}
 			break;
 		case AP2DX_SENSOR_ODOMETRY:
 			//System.out.println("parsing odometry message in planner");
 
 			OdometrySensorMessage msgo = (OdometrySensorMessage) message;
 
 			currentAngle = msgo.getTheta();
 			if (stuck && !reverse)
 			{
 				System.out.println("turning away from stuck position");
 				
 				boolean success = false;
 				
 				double minAngle = destinationAngle - ANGLEUNCERTAIN;
 				double maxAngle = destinationAngle + ANGLEUNCERTAIN;
 				
 				// check if the current angle is +- the destination angle
 				if (minAngle >= -Math.PI && maxAngle <= Math.PI) {
 					if (currentAngle >= minAngle && currentAngle <= maxAngle) {
 						success = true;
 					}
 				}
 				// if current value exceeds whole circle, something ingenious is needed
 				else {
 					boolean min = false, max = false;
 					double minPart1 = 0, maxPart1 = 0, minPart2 = 0, maxPart2 = 0 ;
 					
 					if (minAngle < -Math.PI) {
 						minPart1 = -Math.PI;
 						maxPart1 = Math.PI + (minAngle + Math.PI);
 						min = true;
 					}
 					if (maxAngle > Math.PI) {
 						minPart2 = -Math.PI + (maxAngle - Math.PI);
 						maxPart2 = Math.PI;
 						max = true;
 					}
 					
 					if (min && (currentAngle >= minPart1 || currentAngle >= maxPart1)) {
 						if(max && (currentAngle <= minPart2 || currentAngle <= maxPart2)) {
 							success = true;
 						}
 						else if (currentAngle <= maxAngle) {
 							success = true;
 						}
 					}
 					else if (max && (currentAngle <= minPart2 || currentAngle <= maxPart2)) {
 						if (currentAngle >= minAngle) {
 							success = true;
 						}
 					}					
 				}
 				
 				if (success) {
 					
 					stuck = false;
 
 					sonarPermission = true;
 
 					System.out.println("turned successfully away from stuck position");
 				}
 				
 			}
 			else if (startedTurning && !reverse) {
 				System.out.println("Checking hole");
 				boolean success = false;
 				
 				double minAngle = destinationAngle - ANGLEUNCERTAIN;
 				double maxAngle = destinationAngle + ANGLEUNCERTAIN;
 				
 				// check if the current angle is +- the destination angle
 				if (minAngle >= -Math.PI && maxAngle <= Math.PI) {
 					if (currentAngle >= minAngle && currentAngle <= maxAngle) {
 						success = true;
 					}
 				}
 				// if current value exceeds whole circle, something ingenious is needed
 				else {
 					boolean min = false, max = false;
 					double minPart1 = 0, maxPart1 = 0, minPart2 = 0, maxPart2 = 0 ;
 					
 					if (minAngle < -Math.PI) {
 						minPart1 = -Math.PI;
 						maxPart1 = Math.PI + (minAngle + Math.PI);
 						min = true;
 					}
 					if (maxAngle > Math.PI) {
 						minPart2 = -Math.PI + (maxAngle - Math.PI);
 						maxPart2 = Math.PI;
 						max = true;
 					}
 					
 					if (min && (currentAngle >= minPart1 || currentAngle >= maxPart1)) {
 						if(max && (currentAngle <= minPart2 || currentAngle <= maxPart2)) {
 							success = true;
 						}
 						else if (currentAngle <= maxAngle) {
 							success = true;
 						}
 					}
 					else if (max && (currentAngle <= minPart2 || currentAngle <= maxPart2)) {
 						if (currentAngle >= minAngle) {
 							success = true;
 						}
 					}					
 				}
 				
 				if (success) {
 					
 					startedTurning = false;
 					
 					doHoleScan  = true;
 
 					AP2DXMessage msg6 = new ActionMotorMessage(IAM,
 							Module.REFLEX,
 							ActionMotorMessage.ActionType.STOP, 666);
 					msg6.compileMessage();
 					messageList.add(msg6);
 
 					System.out
 							.println("Sending stop message for scanning");
 				}
 			}
 			else if (startedTurningBack && !reverse) {
 				System.out.println("Hole was not interesting");
 				
 				boolean success = false;
 				
 				double minAngle = destinationAngle - ANGLEUNCERTAIN;
 				double maxAngle = destinationAngle + ANGLEUNCERTAIN;
 				
 				// check if the current angle is +- the destination angle
 				if (minAngle >= -Math.PI && maxAngle <= Math.PI) {
 					if (currentAngle >= minAngle && currentAngle <= maxAngle) {
 						success = true;
 					}
 				}
 				// if current value exceeds whole circle, something ingenious is needed
 				else {
 					boolean min = false, max = false;
 					double minPart1 = 0, maxPart1 = 0, minPart2 = 0, maxPart2 = 0 ;
 					
 					if (minAngle < -Math.PI) {
 						minPart1 = -Math.PI;
 						maxPart1 = Math.PI + (minAngle + Math.PI);
 						min = true;
 					}
 					if (maxAngle > Math.PI) {
 						minPart2 = -Math.PI + (maxAngle - Math.PI);
 						maxPart2 = Math.PI;
 						max = true;
 					}
 					
 					if (min && (currentAngle >= minPart1 || currentAngle >= maxPart1)) {
 						if(max && (currentAngle <= minPart2 || currentAngle <= maxPart2)) {
 							success = true;
 						}
 						else if (currentAngle <= maxAngle) {
 							success = true;
 						}
 					}
 					else if (max && (currentAngle <= minPart2 || currentAngle <= maxPart2)) {
 						if (currentAngle >= minAngle) {
 							success = true;
 						}
 					}					
 				}
 				
 				if (success) {	
 					startedTurningBack = false;
 
 					AP2DXMessage msg5 = new ClearMessage(IAM, Module.REFLEX);
 					msg5.compileMessage();
 					messageList.add(msg5);
 					
 					AP2DXMessage msg6 = new ActionMotorMessage(IAM,
 							Module.REFLEX,
 							ActionMotorMessage.ActionType.FORWARD, 10.0);
 					msg6.compileMessage();
 					messageList.add(msg6);
 					
 					passHole = true;
 					
 					setDistanceGoal(PASSHOLEDISTANCE);
 					setTravelDistance(0);
 
 					System.out.println("Turned back, going forward again");
 				}
 			}
 			if (!firstMessage) {
 				// for now, lets just drive forward, OKAY?!
 				AP2DXMessage msg5 = new ActionMotorMessage(IAM, Module.REFLEX,
 						ActionMotorMessage.ActionType.FORWARD, 10.0);
 				msg5.compileMessage();
 				messageList.add(msg5);
 
 				System.out.println("Sending message first message");
 
 				cruising = true;
 				
 				firstMessage = true;
 
 			}
 
 			break;
 		default:
 			AP2DXBase.logger
 					.severe("Error in AP2DX.reflex.Program.componentLogic(Message message) Couldn't deal with message: "
 							+ message.getMsgType());
 		}
 
 		return messageList;
 	}
 
 	/**
 	 * @param travelDistance
 	 *            the travelDistance to set
 	 */
 	public void setTravelDistance(double travelDistance) {
 		this.travelDistance = travelDistance;
 	}
 
 	/**
 	 * @return the travelDistance
 	 */
 	public double getTravelDistance() {
 		return travelDistance;
 	}
 
 	/**
 	 * @param distanceGoal
 	 *            the distanceGoal to set
 	 */
 	public void setDistanceGoal(double distanceGoal) {
 		this.distanceGoal = distanceGoal;
 	}
 
 	/**
 	 * @return the distanceGoal
 	 */
 	public double getDistanceGoal() {
 		return distanceGoal;
 	}
 }
