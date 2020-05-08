 /* 18649 Fall 2012
  * (Group  17)
  * Jesse Salazar (jessesal)
  * Rajeev Sharma (rdsharma) 
  * Collin Buchan (cbuchan)
  * Jessica Tiu   (jtiu) - Author
  */
 
 
 package simulator.elevatorcontrol;
 
 import jSimPack.SimTime;
 import simulator.elevatorcontrol.Utility.AtFloorArray;
 import simulator.elevatorcontrol.Utility.DoorClosedHallwayArray;
 import simulator.elevatormodules.CarWeightCanPayloadTranslator;
 import simulator.elevatormodules.LevelingCanPayloadTranslator;
 import simulator.framework.Controller;
 import simulator.framework.Direction;
 import simulator.framework.Elevator;
 import simulator.framework.Hallway;
 import simulator.framework.ReplicationComputer;
 import simulator.framework.Speed;
 import simulator.payloads.CanMailbox;
 import simulator.payloads.CanMailbox.ReadableCanMailbox;
 import simulator.payloads.CanMailbox.WriteableCanMailbox;
 import simulator.payloads.DrivePayload;
 import simulator.payloads.DrivePayload.WriteableDrivePayload;
 import simulator.payloads.translators.BooleanCanPayloadTranslator;
 
 /**
  * There is one DriveControl, which controls the elevator Drive
  * (the main motor moving Car Up and Down). For simplicity we will assume
  * this node never fails, although the system could be implemented with
  * two such nodes, one per each of the Drive windings.
  *
  * @author Jessica Tiu
  */
 public class DriveControl extends Controller {
 
     /**
      * ************************************************************************
      * Declarations
      * ************************************************************************
      */
     //note that inputs are Readable objects, while outputs are Writeable objects
 
     //local physical state
     private WriteableDrivePayload localDrive;
 
     //output network messages
     private WriteableCanMailbox networkDriveOut;
     private WriteableCanMailbox networkDriveSpeedOut;
 
     //translators for output network messages
     private DriveCommandCanPayloadTranslator mDrive;
     private DriveSpeedCanPayloadTranslator mDriveSpeed;
 
     //input network messages
     private ReadableCanMailbox networkLevelUp;
     private ReadableCanMailbox networkLevelDown;
     private ReadableCanMailbox networkEmergencyBrake;
     private ReadableCanMailbox networkCarWeight;
     private ReadableCanMailbox networkDesiredFloor;
     private DoorClosedHallwayArray networkDoorClosedFront;
     private DoorClosedHallwayArray networkDoorClosedBack;
     private AtFloorArray networkAtFloorArray;
 
     //translators for input network messages
     private LevelingCanPayloadTranslator mLevelUp;
     private LevelingCanPayloadTranslator mLevelDown;
     private BooleanCanPayloadTranslator mEmergencyBrake;
     private CarWeightCanPayloadTranslator mCarWeight;
     private DesiredFloorCanPayloadTranslator mDesiredFloor;
 
     //store the period for the controller
     private SimTime period;
 
     //enumerate states
     private enum State {
         STATE_DRIVE_STOPPED,
         STATE_DRIVE_LEVEL_UP,
         STATE_DRIVE_LEVEL_DOWN,
         STATE_DRIVE_SLOW,
     }
 
     //state variable initialized to the initial state DRIVE_STOPPED
     private State state = State.STATE_DRIVE_STOPPED;
     private Direction desiredDir = Direction.UP;
 
     //returns the desired direction based on current floor and desired floor by dispatcher
     private Direction getDesiredDir(Direction curDirection) {
 
         Direction desiredDirection = curDirection;
 
         int currentFloor = networkAtFloorArray.getCurrentFloor();
         int desiredFloor = mDesiredFloor.getFloor();
 
         //check car is not between floors
         if (currentFloor != MessageDictionary.NONE) {
 
             //current floor below desired floor
             if (currentFloor < desiredFloor) {
                 desiredDirection = Direction.UP;
             }
             //current floor above desired floor
             else if (currentFloor > desiredFloor) {
                 desiredDirection = Direction.DOWN;
             }
             //current floor is desired floor
             else {
                 desiredDirection = Direction.STOP;
             }
 
         }
         return desiredDirection;
     }
 
     /**
      * The arguments listed in the .cf configuration file should match the order and
      * type given here.
      * <p/>
      * For your elevator controllers, you should make sure that the constructor matches
      * the method signatures in ControllerBuilder.makeAll().
      * <p/>
      * controllers.add(createControllerObject("DriveControl",
      * MessageDictionary.DRIVE_CONTROL_PERIOD, verbose));
      */
     public DriveControl(SimTime period, boolean verbose) {
         //call to the Controller superclass constructor is required
         super("DriveControl", verbose);
         this.period = period;
 
         /* 
         * The log() method is inherited from the Controller class.  It takes an
         * array of objects which will be converted to strings and concatenated
         * only if the log message is actually written.
         *
         * For performance reasons, call with comma-separated lists, e.g.:
         *   log("object=",object);
         * Do NOT call with concatenated objects like:
         *   log("object=" + object);
         */
         log("Created DriveControl with period = ", period);
 
         //create an output payload
         localDrive = DrivePayload.getWriteablePayload();
 
         //register the payload to be sent periodically
         physicalInterface.sendTimeTriggered(localDrive, period);
 
         //create CAN mailbox for output network messages
         networkDriveOut = CanMailbox.getWriteableCanMailbox(MessageDictionary.DRIVE_COMMAND_CAN_ID);
         networkDriveSpeedOut = CanMailbox.getWriteableCanMailbox(MessageDictionary.DRIVE_SPEED_CAN_ID);
 
         /*
         * Create a translator with a reference to the CanMailbox.  Use the
         * translator to read and write values to the mailbox
         */
         mDrive = new DriveCommandCanPayloadTranslator(networkDriveOut);
         mDriveSpeed = new DriveSpeedCanPayloadTranslator(networkDriveSpeedOut);
 
         //register the mailbox to have its value broadcast on the network periodically
         //with a period specified by the period parameter.
         canInterface.sendTimeTriggered(networkDriveOut, period);
         canInterface.sendTimeTriggered(networkDriveSpeedOut, period);
 
         /*
          * To register for network messages from the smart sensors or other objects
          * defined in elevator modules, use the translators already defined in
          * elevatormodules package.  These translators are specific to one type
          * of message.
          */
         networkLevelUp =
                 CanMailbox.getReadableCanMailbox(MessageDictionary.LEVELING_BASE_CAN_ID +
                         ReplicationComputer.computeReplicationId(Direction.UP));
         networkLevelDown =
                 CanMailbox.getReadableCanMailbox(MessageDictionary.LEVELING_BASE_CAN_ID +
                         ReplicationComputer.computeReplicationId(Direction.DOWN));
         networkEmergencyBrake =
                 CanMailbox.getReadableCanMailbox(MessageDictionary.EMERGENCY_BRAKE_CAN_ID);
         networkCarWeight =
                 CanMailbox.getReadableCanMailbox(MessageDictionary.CAR_WEIGHT_CAN_ID);
         networkDesiredFloor =
                 CanMailbox.getReadableCanMailbox(MessageDictionary.DESIRED_FLOOR_CAN_ID);
         networkDoorClosedFront = new Utility.DoorClosedHallwayArray(Hallway.FRONT, canInterface);
         networkDoorClosedBack = new Utility.DoorClosedHallwayArray(Hallway.BACK, canInterface);
         networkAtFloorArray = new Utility.AtFloorArray(canInterface);
 
         mLevelUp =
                 new LevelingCanPayloadTranslator(networkLevelUp, Direction.UP);
         mLevelDown =
                 new LevelingCanPayloadTranslator(networkLevelDown, Direction.DOWN);
         mEmergencyBrake =
                 new BooleanCanPayloadTranslator(networkEmergencyBrake);
         mCarWeight =
                 new CarWeightCanPayloadTranslator(networkCarWeight);
         // used to calculate desiredDir
         mDesiredFloor =
                 new DesiredFloorCanPayloadTranslator(networkDesiredFloor);
 
         //register to receive periodic updates to the mailbox via the CAN network
         //the period of updates will be determined by the sender of the message
         canInterface.registerTimeTriggered(networkLevelUp);
         canInterface.registerTimeTriggered(networkLevelDown);
         canInterface.registerTimeTriggered(networkEmergencyBrake);
         canInterface.registerTimeTriggered(networkCarWeight);
         canInterface.registerTimeTriggered(networkDesiredFloor);
 
         /* issuing the timer start method with no callback data means a NULL value 
         * will be passed to the callback later.  Use the callback data to distinguish
         * callbacks from multiple calls to timer.start() (e.g. if you have multiple
         * timers.
         */
         timer.start(period);
     }
 
     /*
      * The timer callback is where the main controller code is executed.  For time
      * triggered design, this consists mainly of a switch block with a case blcok for
      * each state.  Each case block executes actions for that state, then executes
      * a transition to the next state if the transition conditions are met.
      */
     public void timerExpired(Object callbackData) {
         State newState = state;
 
         switch (state) {
 
             case STATE_DRIVE_STOPPED:
 
                 desiredDir = getDesiredDir(desiredDir);
 
                 //state actions for DRIVE_STOPPED
                 localDrive.set(Speed.STOP, Direction.STOP);
                 mDrive.set(Speed.STOP, Direction.STOP);
                 mDriveSpeed.set(Speed.STOP, desiredDir);
 
                 //transitions
 
                 //#transition 'T6.1'
                 if (!mLevelUp.getValue() && mLevelDown.getValue()) {
                     newState = State.STATE_DRIVE_LEVEL_UP;
                 }
 
                 //#transition 'T6.3'
                 else if (!mLevelDown.getValue() && mLevelUp.getValue()) {
                     newState = State.STATE_DRIVE_LEVEL_DOWN;
                 }
 
                 //#transition 'T6.9' 
                 else if (networkDoorClosedFront.getAllClosed() && networkDoorClosedBack.getAllClosed() &&
                         !desiredDir.equals(Direction.STOP) &&
                         mCarWeight.getWeight() < Elevator.MaxCarCapacity &&
                         !mEmergencyBrake.getValue()) {
 
                     newState = State.STATE_DRIVE_SLOW;
                 } else {
                     newState = state;
                 }
 
                 break;
 
             case STATE_DRIVE_LEVEL_UP:
 
                 desiredDir = getDesiredDir(desiredDir);
 
                 //state actions for DRIVE_LEVEL_UP
                 localDrive.set(Speed.LEVEL, Direction.UP);
                 mDrive.set(Speed.LEVEL, Direction.UP);
                 mDriveSpeed.set(Speed.LEVEL, Direction.UP);
 
                 //transitions
 
                 //#transition 'T6.2'
                 if (mCarWeight.getWeight() >= Elevator.MaxCarCapacity ||
                         mEmergencyBrake.getValue() ||
                         !networkDoorClosedFront.getAllClosed() || !networkDoorClosedBack.getAllClosed() ||
                         (mLevelUp.getValue() && mLevelDown.getValue() &&
                                 desiredDir.equals(Direction.STOP))) {
                     newState = State.STATE_DRIVE_STOPPED;
                 }
 
                 //#transition 'T6.5'
                 else if (desiredDir.equals(Direction.STOP) &&
                         !mLevelDown.getValue() && mLevelUp.getValue()) {
                     newState = State.STATE_DRIVE_LEVEL_DOWN;
 
                 } else {
                     newState = state;
                 }
 
                 break;
 
             case STATE_DRIVE_LEVEL_DOWN:
 
                 desiredDir = getDesiredDir(desiredDir);
 
                 //state actions for DRIVE_LEVEL_DOWN
                 localDrive.set(Speed.LEVEL, Direction.DOWN);
                 mDrive.set(Speed.LEVEL, Direction.DOWN);
                 mDriveSpeed.set(Speed.LEVEL, Direction.DOWN);
 
                 //transitions
 
                 //#transition 'T6.4'
                 if (mCarWeight.getWeight() >= Elevator.MaxCarCapacity ||
                         mEmergencyBrake.getValue() ||
                         !networkDoorClosedFront.getAllClosed() || !networkDoorClosedBack.getAllClosed() ||
                         (mLevelUp.getValue() && mLevelDown.getValue() &&
                                 desiredDir.equals(Direction.STOP))) {
                     newState = State.STATE_DRIVE_STOPPED;
                 }
 
                 //#transition 'T6.6'
                 else if (desiredDir.equals(Direction.STOP) &&
                         !mLevelUp.getValue() && mLevelDown.getValue()) {
                     newState = State.STATE_DRIVE_LEVEL_UP;
 
                 } else {
                     newState = state;
                 }
 
                 break;
 
             case STATE_DRIVE_SLOW:
 
                 desiredDir = getDesiredDir(desiredDir);
 
                 //state actions for DRIVE_SLOW
                 localDrive.set(Speed.SLOW, desiredDir);
                 mDrive.set(Speed.SLOW, desiredDir);
                 mDriveSpeed.set(Speed.SLOW, desiredDir);
 
                 //transitions
 
                 //#transition 'T6.7'
                 if (desiredDir.equals(Direction.STOP) &&
                         !mLevelUp.getValue() && mLevelDown.getValue()) {
                     newState = State.STATE_DRIVE_LEVEL_UP;
                 }
 
                 //#transition 'T6.8'
                 else if (desiredDir.equals(Direction.STOP) &&
                         !mLevelDown.getValue() && mLevelUp.getValue()) {
                     newState = State.STATE_DRIVE_LEVEL_DOWN;
                 }
 
                 //#transition 'T6.10'
                 else if (mCarWeight.getWeight() >= Elevator.MaxCarCapacity ||
                         mEmergencyBrake.getValue() ||
                         !networkDoorClosedFront.getAllClosed() || !networkDoorClosedBack.getAllClosed()) {
                     newState = State.STATE_DRIVE_STOPPED;
 
                 } else {
                     newState = state;
                 }
 
                 break;
             default:
                 throw new RuntimeException("State " + state + " was not recognized.");
         }
 
         //log the results of this iteration
         if (state == newState) {
             log("remains in state: ", state);
         } else {
             log("Transition:", state, "->", newState);
         }
 
         //update the state variable
         state = newState;
 
         //report the current state
         setState(STATE_KEY, newState.toString());
 
         //schedule the next iteration of the controller
         //you must do this at the end of the timer callback in order to restart
         //the timer
         timer.start(period);
     }
 }
 
