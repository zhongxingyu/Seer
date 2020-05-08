 /* 
  * Course and Semester : 18-649 Fall 2013
  * Group No: 16
  * Group Members : Jiangtian Nie(jnie) , Yue Chen(yuechen),
  *                 Sally Stevenson(ststeven) , Sri Harsha Koppaka(skoppaka)
  * Author : Jiangtian Nie
  * AndrewID : jnie
  */
 
 package simulator.elevatorcontrol;
 
 import java.util.LinkedHashMap;
 import jSimPack.SimTime;
 import simulator.elevatormodules.*;
 import simulator.framework.*;
 import simulator.payloads.*;
 import simulator.payloads.CanMailbox.ReadableCanMailbox;
 import simulator.payloads.CanMailbox.WriteableCanMailbox;
 import simulator.payloads.translators.IntegerCanPayloadTranslator;
 import simulator.payloads.CarPositionIndicatorPayload.*;
 
 public class CarPositionControl extends Controller {
 
 	private SimTime period ;
 	// Translator for AtFloor message -- Specific to Message
 
 	private enum State {
 		STATE_DISPLAY;
 	}
 	
 	//physical CarPosition indicator;
 	private WriteableCarPositionIndicatorPayload localCPI;
 	//network CarPosition indicator
 	private IntegerCanPayloadTranslator mCPI;
 	
 	//network message for future extension
 	private WriteableCanMailbox networkCPI;
 
 	//define all mAtFloor in an array;
 	private ReadableCanMailbox[][] networkAtFloor=new ReadableCanMailbox[8][2];
 	//define a HashMap to represent 4 status of hallway. 
 	LinkedHashMap<Integer, Hallway> hallway=new LinkedHashMap<Integer, Hallway>();
 	AtFloorCanPayloadTranslator mAtFloor;
 	
 	//current state
 	State state = State.STATE_DISPLAY;
 
 	public CarPositionControl(SimTime period, boolean verbose) {
 		super("CarPositionControl", verbose);
 		
 		this.period = period;
 		
 		//Initializing the Hallway values
 		hallway.put(1, Hallway.FRONT);
 		hallway.put(2, Hallway.BACK);
 		hallway.put(3, Hallway.BOTH);
 		hallway.put(4, Hallway.NONE);
 		
 		//Send Physical message to CarPositionIndicator
 		localCPI = CarPositionIndicatorPayload.getWriteablePayload();
 		//Register PayLoad to be sent periodically
 		physicalInterface.sendTimeTriggered(localCPI, period);
 		
 		
 		networkCPI = CanMailbox
 				.getWriteableCanMailbox(MessageDictionary.CAR_POSITION_CAN_ID);
 		//Network Message for CarPositionIndicator
 		mCPI = new IntegerCanPayloadTranslator(networkCPI);
 		//Register mailbox to be sent onto network periodically
 		canInterface.sendTimeTriggered(networkCPI, period);
 		
 		
 		//Message for CarLevel Position Indicator - Future Expansion
 		ReadableCanMailbox networkCarLevelPosIndicator = CanMailbox
 				.getReadableCanMailbox(MessageDictionary.CAR_LEVEL_POSITION_CAN_ID);
		new CarLevelPositionCanPayloadTranslator(
 				networkCarLevelPosIndicator);
 		canInterface.registerTimeTriggered(networkCarLevelPosIndicator);
 		
 		//Set Current State to Display
 		state = State.STATE_DISPLAY;
 		
 		//register all mAtFloor
 		for (int i = 1; i < 9; i++) {
 			for (int j = 1; j < 3; j++) {
 				networkAtFloor[i-1][j-1] = CanMailbox.getReadableCanMailbox(
 		                MessageDictionary.AT_FLOOR_BASE_CAN_ID +
 		                ReplicationComputer.computeReplicationId(i, hallway.get(j)));
 				canInterface.registerTimeTriggered(networkAtFloor[i-1][j-1]);
 			}
 		}
 		
 		//Start timer
 		timer.start(period);
 
 	}
 
 	@Override
 	public void timerExpired(Object callbackData) {
 		State newState = state;
 		
 		switch (state) {		
 			case STATE_DISPLAY:
 				//look up the current floor.
 				for (int i = 1; i < 9; i++) {
 					for (int j = 1; j < 3; j++) {
 						//# transition 'T10.1'
 						if ((new AtFloorCanPayloadTranslator(networkAtFloor[i-1][j-1], i,
 								hallway.get(j))).getValue()) {
 							mAtFloor=new AtFloorCanPayloadTranslator(networkAtFloor[i-1][j-1],
 								i, hallway.get(j));
 							// State Actions to Display Current Floor
 							localCPI.set(i);
 							mCPI.set(i);
 							break;
 						}
 					}
 				}
 			
 				//Sets State to iterate Continuously 
 				newState = State.STATE_DISPLAY;
 			
 				break;
 			default:
 				throw new RuntimeException("State" + state
 					+ "was not recognized");
 		}
 		
 		if (state == newState) {
             log("Remaining in state: ",state);
         }
 		
 		state = newState;
 		setState(STATE_KEY, state.toString());
 		
         // Schedule the next iteration of the controller
         // You must do this at the end of the timer callback in order to
         // restart the timer
 		timer.start(period);
 	}
 
 }
