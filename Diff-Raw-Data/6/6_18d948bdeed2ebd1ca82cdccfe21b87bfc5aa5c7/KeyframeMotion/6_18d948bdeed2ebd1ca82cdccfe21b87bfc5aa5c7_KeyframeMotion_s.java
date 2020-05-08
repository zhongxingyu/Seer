 package keyframeMotion;
 
 import agentIO.EffectorOutput;
 import agentIO.PerceptorInput;
 import keyframeMotion.util.Keyframe;
 import keyframeMotion.util.KeyframeFileHandler;
 import keyframeMotion.util.KeyframeSequence;
 import util.Logger;
 import util.RobotConsts;
 
 /** 
  * Motions for the robot by processing of keyframe sequences.
  * 
  * An object of class KeyframeMotion loads robot movements (like "walking two 
  * steps" or "turning the head" etc.) from .txt-files and provides methods for 
  * applying these movements. 
  * 
  * Usage (usually during the think()-method of the agent class):
  * 1) Method ready(): Before a movement can be chosen, call ready() to check,
  * if the robot currently executes a move.
  * 2) Methods set...(): Choose a movement with one of the set...() methods 
  * (like setWalkForward()). This can be done only after the ready()-call, 
  * because otherwise the current movement would be aborted suddenly, so for 
  * example the robot would fall down while doing a walking step.
  * The setTest() method is a specific feature for testing new keyframe 
  * sequences. 
  * 3) At any time the method setLogging(...) can be executed to turn the 
  * logging of chosen moves on or off. If the logging is turned on, every time, 
  * when a move is set, the name of the move is logged. 
  *  
  * Required context in the agent class: 
  * The agent class must have an object of class EffectorOutput and one of class
  * PerceptorInput, and they must be used correctly as explained in the comments
  * on the classes.
  * In the act()-method of the agent class: method 
  * KeyframeMotion.executeKeyframeSequence() must be called before the
  * EffectorOutput-method sendAgentMessage().
  * An example for a correct usage context of KeyframeMotion is class
  * Agent_SimpleWalkToBall in package examples.agentSimpleWalkToBall.
  * 
  * Integrating new motions to the implementation of this class:
  * 1) Save the new keyframe sequence in folder 
  * "[RoboNewbie project folder]/keyframes/" .
  * 2) Add a new class variable to KeyframeMotion just like WALK_FORWARD_SEQUENCE.
  * 3) Extend the constructor to load the new sequence into the new variable.
  * 4) Add a new set...() method just like setWalkForward(). (Not like setTest()!)
  * 
  * Using KeyframeMotion together with other motion implementations:
  * As mentioned above, method executeKeyframeSequence() has to be called in
  * every cycle. After this call, there are set commands for every joint
  * effector. Other motion classes can overwrite the commands with the
  * EffectorOutput instance of the agent.
  * See also the comment on method executeKeyframeSequence() and on class
  * EffectorOutput. Example: In Agent_SimpleSoccer.java (especially method act())
  * in package examples.agentSimpleSoccer another motion class is used together
  * with KeyframeMotion.
  */
 public class KeyframeMotion {
 
   private enum MotionState {
     IN_FRAME, BETWEEN_FRAMES, READY_TO_MOVE
   }
   private static final double ANGLE_TOLLERANCE = 8f;
   
   //time between two server messages in ms
   private static final int TIME_STEMP_INTERVAL = 20; 
   
   private final PerceptorInput percIn;
   private final EffectorOutput effOut;
   private final Logger log;
   boolean loggingOn;
   private static KeyframeSequence WALK_FORWARD_SEQUENCE;
   private static KeyframeSequence FALL_BACK_SEQUENCE;
   private static KeyframeSequence FALL_FORWARD_SEQUENCE;
   private static KeyframeSequence STAND_UP_FROM_BACK_SEQUENCE;
   private static KeyframeSequence ROLL_OVER_TO_BACK_SEQUENCE;
   private static KeyframeSequence STOP_WALKING_SEQUENCE;
   private static KeyframeSequence TURN_RIGHT_SEQUENCE;
   private static KeyframeSequence TURN_LEFT_SEQUENCE;
   private static KeyframeSequence TURN_RIGHT_SMALL_SEQUENCE;
   private static KeyframeSequence TURN_LEFT_SMALL_SEQUENCE;
   private static KeyframeSequence SIDE_STEP_RIGHT_SEQUENCE;
   private static KeyframeSequence SIDE_STEP_LEFT_SEQUENCE;
   private static KeyframeSequence TURN_HEAD_LEFT_SEQUENCE;
   private static KeyframeSequence TURN_HEAD_RIGHT_SEQUENCE;
   private static KeyframeSequence TURN_HEAD_DOWN_SEQUENCE;
   private static KeyframeSequence WAVE_SEQUENCE;
   
   private static KeyframeSequence WALK_FORWARD_BEGIN_SEQUENCE;
   private static KeyframeSequence WALK_FORWARD_LEFT_SEQUENCE;
   private static KeyframeSequence WALK_FORWARD_LEFT_END_SEQUENCE;
   private static KeyframeSequence WALK_FORWARD_RIGHT_SEQUENCE;
   private static KeyframeSequence WALK_FORWARD_RIGHT_END_SEQUENCE;
   private static KeyframeSequence KICK_THE_BALL_SEQUENCE;
   private static KeyframeSequence SIDE_STEP_LEFT_KIKA_SEQUENCE;
   private static KeyframeSequence SIDE_STEP_RIGHT_KIKA_SEQUENCE;
   
  
   private Keyframe actualKeyframe = null;           // Mit diesen drei Variablen
   private int leftCyclesForActualFrame = 0;         // könnte man state
   private KeyframeSequence actualSequence = null;   // ersetzen, aber so ist der Code verständlicher. 
   private MotionState state = MotionState.READY_TO_MOVE;
   
   private double[] lastCycleAngles = new double[RobotConsts.JointsCount];
 
   /**
    * Constructor, initialize dependencies and load movements. 
    * 
    * Sets the required dependencies for logging and sending 
    * effector commands to the server and loads all movements from the keyframe 
    * sequence files. 
    * 
    * @param effOut Has to be already initialized, cannot be null. 
    * @param percIn Has to be already initialized, cannot be null. 
    * @param logger Has to be already initialized, cannot be null. 
    * 
    */
   public KeyframeMotion(EffectorOutput effOut, PerceptorInput percIn, Logger logger) {
     super();
     this.effOut = effOut;
     this.percIn = percIn;
     log = logger;
     loggingOn = false;
     for (int i = 0; i < lastCycleAngles.length; i++) 
       lastCycleAngles[i] = 0;
 
     KeyframeFileHandler keyframeReader = new KeyframeFileHandler();
 
     WALK_FORWARD_SEQUENCE = keyframeReader.getSequenceFromFile("walk_forward-flemming-nika.txt");
     FALL_BACK_SEQUENCE = keyframeReader.getSequenceFromFile("nika_fall_back.txt");
     FALL_FORWARD_SEQUENCE = keyframeReader.getSequenceFromFile("fall_forward.txt");
     STAND_UP_FROM_BACK_SEQUENCE = keyframeReader.getSequenceFromFile("stand_up_from_back.txt");
     ROLL_OVER_TO_BACK_SEQUENCE = keyframeReader.getSequenceFromFile("roll_over_to_back.txt");
     STOP_WALKING_SEQUENCE = keyframeReader.getSequenceFromFile("nika_stop_walking.txt");
     TURN_RIGHT_SEQUENCE = keyframeReader.getSequenceFromFile("turn-right.txt");
     TURN_LEFT_SEQUENCE = keyframeReader.getSequenceFromFile("turn-left.txt");
     TURN_RIGHT_SMALL_SEQUENCE = keyframeReader.getSequenceFromFile("turn-right-small-nika.txt");
     TURN_LEFT_SMALL_SEQUENCE = keyframeReader.getSequenceFromFile("turn-left-small-nika.txt");
     SIDE_STEP_RIGHT_SEQUENCE = keyframeReader.getSequenceFromFile("side-step-right-nika.txt");
     SIDE_STEP_LEFT_SEQUENCE = keyframeReader.getSequenceFromFile("side-step-left-nika.txt");
     TURN_HEAD_LEFT_SEQUENCE = keyframeReader.getSequenceFromFile("turn-head-left.txt");
     TURN_HEAD_RIGHT_SEQUENCE = keyframeReader.getSequenceFromFile("turn-head-right.txt");
     TURN_HEAD_DOWN_SEQUENCE = keyframeReader.getSequenceFromFile("turn-head-down.txt");
     WAVE_SEQUENCE = keyframeReader.getSequenceFromFile("wave_nika.txt");
     
     WALK_FORWARD_BEGIN_SEQUENCE = keyframeReader.getSequenceFromFile("walk_forward-begin.txt");
     WALK_FORWARD_LEFT_SEQUENCE = keyframeReader.getSequenceFromFile("walk_forward-left.txt");
     WALK_FORWARD_LEFT_END_SEQUENCE = keyframeReader.getSequenceFromFile("walk_forward-left-end.txt");
     WALK_FORWARD_RIGHT_SEQUENCE = keyframeReader.getSequenceFromFile("walk_forward-right.txt");
     WALK_FORWARD_RIGHT_END_SEQUENCE = keyframeReader.getSequenceFromFile("walk_forward-right-end.txt");
     KICK_THE_BALL_SEQUENCE = keyframeReader.getSequenceFromFile("kick_the_ball.txt");
     SIDE_STEP_LEFT_KIKA_SEQUENCE = keyframeReader.getSequenceFromFile("side-step-left-kika.txt");
     SIDE_STEP_RIGHT_KIKA_SEQUENCE = keyframeReader.getSequenceFromFile("side-step-right-kika.txt");
   }
 
   protected String currentPosture_ = "standing";
   
   public String currentPosture () {
     if (percIn.getAcc().getZ() < 7)
       currentPosture_ = "laying-down";
     return currentPosture_;
   }
   
   /**
    * Turn logging of set moves on or off. 
    * 
    * When the logging is turned on, a log entry is generated every time when a 
    * move is set.
    * 
    * @param b Pass b=true to turn the logging on, and b=false to turn it off.  
    */
   public void setLogging(boolean b){
     loggingOn = b;
   }
   
   /**
    * Set move to turn the robots head down.
    * 
    * Assumed posture before this move: could be any.
    */
   public void setTurnHeadDown() {
     if (loggingOn) log.log("motion turn head down \n");
     actualSequence = TURN_HEAD_DOWN_SEQUENCE;
     state = MotionState.BETWEEN_FRAMES;
   }
 
   /**
    * Set move to turn the robots head to the left as far as possible.
    * 
    * Assumed posture before this move: could be any.
    */
   public void setTurnHeadLeft() {
     if (loggingOn) log.log("motion turn head left \n");
     actualSequence = TURN_HEAD_LEFT_SEQUENCE;
     state = MotionState.BETWEEN_FRAMES;
   }
   
   /**
    * Set move to turn the robots head to the right as far as possible.
    * 
    * Assumed posture before this move: could be any. 
    */
   public void setTurnHeadRight() {
     if (loggingOn) log.log("motion turn head right \n");
     actualSequence = TURN_HEAD_RIGHT_SEQUENCE;
     state = MotionState.BETWEEN_FRAMES;
   }
   
   
   /**
    * Set move for walking one turning right step (about 30 degrees).
    * 
    * Assumed posture before this move: the robot is standing on parallel feet.
    */
   public void setTurnRight() {
     if (loggingOn) log.log("motion turn right \n");
     if (isWalking()) {    
       setStopWalking();
       return;
     }
     actualSequence = TURN_RIGHT_SEQUENCE;
     state = MotionState.BETWEEN_FRAMES;
   }
 
   /**
    * Set move for walking one turning left step (about 30 degrees).
    * 
    * Assumed posture before this move: the robot is standing on closed parallel 
    * feet.
    */
   public void setTurnLeft() {
     if (loggingOn) log.log("motion turn left \n");
     if (isWalking()) {    
       setStopWalking();
       return;
     }
     actualSequence = TURN_LEFT_SEQUENCE;
     state = MotionState.BETWEEN_FRAMES;
   }
   
   /**
    * Set move for walking one small turning right step (about 6-8 degrees).
    * 
    * Assumed posture before this move: the robot is standing on parallel feet.
    */
   public void setTurnRightSmall() {
     if (loggingOn) log.log("motion turn right small\n");
     if (isWalking()) {    
       setStopWalking();
       return;
     }
     actualSequence = TURN_RIGHT_SMALL_SEQUENCE;
     state = MotionState.BETWEEN_FRAMES;
   }
 
   /**
    * Set move for walking one small turning left step (about 6-8 degrees).
    * 
    * Assumed posture before this move: the robot is standing on closed parallel 
    * feet.
    */
   public void setTurnLeftSmall() {
     if (loggingOn) log.log("motion turn left small\n");
     if (isWalking()) {    
       setStopWalking();
       return;
     }
     actualSequence = TURN_LEFT_SMALL_SEQUENCE;
     state = MotionState.BETWEEN_FRAMES;
   }
   
     /**
    * Set move for walking one side step to the right.
    * 
    * Assumed posture before this move: the robot is standing on parallel feet.
    */
   public void setSideStepRight() {
     if (loggingOn) log.log("motion side step right \n");
     if (isWalking()) {    
       setStopWalking();
       return;
     }
     actualSequence = SIDE_STEP_RIGHT_KIKA_SEQUENCE;
     state = MotionState.BETWEEN_FRAMES;
   }
 
   /**
    * Set move for walking one side step to the left.
    * 
    * Assumed posture before this move: the robot is standing on closed parallel 
    * feet.
    */
   public void setSideStepLeft() {
     if (loggingOn) log.log("motion side step left \n");
     if (isWalking()) {    
       setStopWalking();
       return;
     }
     actualSequence = SIDE_STEP_LEFT_KIKA_SEQUENCE;
     state = MotionState.BETWEEN_FRAMES;
   }
 
   /**
    * Set move for walking two steps forward.
    * 
    * Assumed posture before this move: the robot is standing on closed parallel 
    * feet or has just completed the same walk forward movement.
    * 
    * To return to parallel feet after walking use method setStopWalking().
    */
   public void setWalkForward() {
     if (loggingOn) log.log("motion walk forward \n");
     if (currentPosture() == "standing") {
       actualSequence = WALK_FORWARD_BEGIN_SEQUENCE;
       currentPosture_ = "walking-begin";
     } else if (currentPosture() == "walking-begin"
         || currentPosture() == "walking-left-leg") {
       actualSequence = WALK_FORWARD_LEFT_SEQUENCE;
       currentPosture_ = "walking-right-leg";      
     } else if (currentPosture() == "walking-right-leg") {
       actualSequence = WALK_FORWARD_RIGHT_SEQUENCE;
       currentPosture_ = "walking-left-leg";      
     } else {
       assert(false);
     }
     state = MotionState.BETWEEN_FRAMES;
   }
   
   public boolean isWalking () {
     return currentPosture() == "walking-left-leg" ||
       currentPosture() == "walking-right-leg";
   }
 
   /**
    * Set move to let the robot stop with parallel feed after walking forward.
    * 
    * Assumed posture before this move: the robot has just completed the walking
    * sequence and stands on one foot stretched out to the front and one to the
    * back.
    * 
    * After this move the robot stands on closed parallel feet.
    */
   public void setStopWalking() {  
     if (loggingOn) log.log("motion stop walking \n");
     if (currentPosture() == "walking-left-leg") {
       actualSequence = WALK_FORWARD_LEFT_END_SEQUENCE;
       currentPosture_ = "standing";      
     } else if (currentPosture() == "walking-right-leg") {
       actualSequence = WALK_FORWARD_RIGHT_END_SEQUENCE;
       currentPosture_ = "standing";      
     } else {
       assert(false);
     }
     state = MotionState.BETWEEN_FRAMES;
   }
   
   /**
    * Set move to let the robot fall down on its back.
    * 
    * Assumed posture before this move: the robot is in an upright position.
    */
   public void setFallBack() {
     if (loggingOn) log.log("motion fall back \n");
     actualSequence = FALL_BACK_SEQUENCE;
     state = MotionState.BETWEEN_FRAMES;
   }
   
   /**
    * Set move to let the robot fall down on its front side.
    * 
    * Assumed posture before this move: the robot is in an upright position.
    */
   public void setFallForward() {
     if (loggingOn) log.log("motion fall forward \n");
     actualSequence = FALL_FORWARD_SEQUENCE;
     state = MotionState.BETWEEN_FRAMES;
   }
   
   public void setStandUp () {
     if (percIn.getAcc().getY() > 0)
       setStandUpFromBack();
     else
       setRollOverToBack();    
   }
   
     
 
   /**
    * Set move for standing up, when the robot lies on its back.
    * 
    * Assumed posture before this move: the robot lies on its back.
    * After this move the robot stands on closed parallel feet.
    */
   public void setStandUpFromBack() {
     if (loggingOn) log.log("motion stand up from back \n");
     actualSequence = STAND_UP_FROM_BACK_SEQUENCE;
     currentPosture_ = "standing";
     state = MotionState.BETWEEN_FRAMES;
   }
   
   /**
    * Set move for rolling over the lying robot to its back.
    * 
    * Assumed posture before this move: the robot lies on its front. 
    * This move might have to be executed several times.
    */
    public void setRollOverToBack() {
     if (loggingOn) log.log("motion roll over from back \n");
     actualSequence = ROLL_OVER_TO_BACK_SEQUENCE;
     state = MotionState.BETWEEN_FRAMES;
   }
    
   /**
    * Set move waving.
    * 
    * Assumed posture before this move: the robot is in an upright position.
    */
    public void setWave() {
     if (loggingOn) log.log("motion wave \n");
     actualSequence = WAVE_SEQUENCE;
     state = MotionState.BETWEEN_FRAMES;
   }
 
   public void setKickTheBall() {
     if (loggingOn) log.log("motion kick the ball \n");
     if (isWalking()) {    
       setStopWalking();
       return;
     }
     actualSequence = KICK_THE_BALL_SEQUENCE;
     state = MotionState.BETWEEN_FRAMES;
   }
 
   /**
    * Load a test motion from file test.txt and set it for execution. 
    * 
    * The motion has to be in file "[RoboNewbie project folder]/keyframes/test.txt".
    * 
    * This setter method differs from the other ones concerning the loading: The
    * keyframe-file test.txt is read with each new call of the motion "Test". 
    * So the file test.txt can be edited during the runtime of the agent 
    * program and changes in the movement are visible immediately after the next
    * call of this method. This allows to change the keyframe-file without a new 
    * start of the agent.
    * It is used in this way by the class Agent_KeyframeDeveloper. 
    */
   public void setTest() {
     if (loggingOn) log.log("motion Test\n");
     if (isWalking()) {    
       setStopWalking();
       return;
     }
     KeyframeFileHandler keyframeReader 
             = new KeyframeFileHandler();
     actualSequence = keyframeReader.getSequenceFromFile("test.txt");
     state = MotionState.BETWEEN_FRAMES;
   }
 
   /**
    * States, whether the robot is ready to start a new movement or is currently 
    * busy with a movement.
    * 
    * @return True, if the robot is ready for setting a new movement. Else false,
    * if the robot currently executes a sequence. 
    */
   public boolean ready() {
     if (state == MotionState.READY_TO_MOVE) {
       return true;
     } else {
       return false;
     }
   }
 
   /**
    * Executes the actually set movement (also callable if there is not set any 
    * movement).
    * 
    * Continues the current movement over all planned server cycles, means that
    * this method activates the functionalities to 
    * - translate the data in a keyframe sequence to effector commands for the
    * actual server cycle
    * and 
    * - set the joint commands, which should be sent to the server in the actual
    * server cycle.
    * 
    * Note that here are set commands for all joints of the robot, even for the
    * joints, that are not changed during the set move. E.g. if the walking move
    * is chosen for execution, than the commands for the head will be set, too, 
    * in every server cycle, when this method is executed. 
    * 
    * If there is not set any movement, there are not set any commands. 
    */
   public void executeKeyframeSequence() {
     switch (state) {
       case READY_TO_MOVE:
         break;
       case BETWEEN_FRAMES:
         setActualKeyframe();
         break;
       case IN_FRAME:
         executeActualKeyframe();
         break;
     }
   }
 
   /**
    * Internal method for choosing the actual keyframe from the actual sequence.
    * 
    * -------------------------
    * Setzt den Frame, der in der fortlaufenden Bewegung als Nächstes folgt. 
    * Außerdem setzt diese Methode den internen Zustand von KeyframeMotion, der
    * angibt, ob sich der Roboter immernoch innerhalb einer Bewegung befindet, 
    * oder die Bewegung beendet hat.
    */
   private void setActualKeyframe() {
 
 //    if (actualKeyframe != null) {
 //      log.log("setActKey");
 //      log.log(actualKeyframe.getDebugString());
 //      log.log(percIn.getJointsDebugString() + "\n");
 //    }
 
    actualKeyframe = actualSequence.getNextFrame();
     if (actualKeyframe == null) {
       actualSequence = null;
       state = MotionState.READY_TO_MOVE;
       //log.log("ende der seq");
     } else {
       for (int i = 0; i < lastCycleAngles.length; i++) 
         lastCycleAngles[i] = 0;
       leftCyclesForActualFrame = actualKeyframe.getTransitionTime() / TIME_STEMP_INTERVAL;
       
       state = MotionState.IN_FRAME;
       executeActualKeyframe();
 
 //            log.log("setActKeyfr.. frame:");
 //            StringBuilder returnString = new StringBuilder();
 //            returnString.append(actualKeyframe.getTransitionTime()).append(' ');            
 //            for (int i = 0; i < RobotConsts.JointsCount; i++){
 //                returnString.append(RobotConsts.getEffectorName(i));
 //                returnString.append('=');
 //                returnString.append(actualKeyframe.getAngle(i));
 //                returnString.append(' ');
 //            }
 //            log.log(returnString.toString());
     }
   }
 
   /**
    * Internal method for initiating the translation from a keyframe sequence to 
    * effector commands and setting the actual commands for this cycle.
    * 
    * -------------------------
    * Schickt die Motorenbefehle für einen Takt an den Simulatios-Server. Ein 
    * Keyframe wird innerhalb mehrerer Takte erreicht. Die Anzahl der Takte hängt
    * von der im Keyframe angegebenen Transitionszeit ab.
    * Außerdem setzt diese Methode den internen Zustand von KeyframeMotion, der
    * angibt ob sich der Roboter immernoch innerhalb eines Keyframes einer 
    * Bewegung befindet, oder ob das aktuelle Keyframe beendet ist.
    * 
    * Die Methode könnte für sinnvolles Bewegungs-Debugging zurück liefern,
    * welche Werte erreicht wurden, und welche nicht, am besten auch noch mit 
    * der nummer des frames innerhalb der Sequenz.
    */
   private void executeActualKeyframe() {
 
     double[] newCommands = new double[RobotConsts.JointsCount];
     state = MotionState.BETWEEN_FRAMES;
 
     for (int i = 0; i < RobotConsts.JointsCount; i++) {
       newCommands[i] =
               getSpeedFromAngleAndTime(i);
       if (newCommands[i] != 0f) {
         state = MotionState.IN_FRAME;
       }
     }
 //      Debug output for effector commands (see also below)
 //        StringBuilder builder = new StringBuilder();
 //        builder.append("cycles left ").append(leftCyclesForActualFrame).append(' ');
 //        for (int i = 0; i < RobotConsts.JointsCount; i++) {
 //            builder.append(RobotConsts.getEffectorName(i)).append(' ');
 //            if (Math.abs(newCommands[i]) < 0.001) 
 //                builder.append(0);
 //            else
 //                builder.append(newCommands[i]);
 //            builder.append("  ");
 //        }
 //        log.log(builder.toString());   
         //log.log(percIn.getJointsDebugString());
         
     effOut.setAllJointCommands(newCommands);
     leftCyclesForActualFrame--;
 
 
 //        Debug output für verification of a keyframe reader or for testing of a new sequence. 
 
 //    if (state == MotionState.BETWEEN_FRAMES) {
 //      log.log("exeActKey");
 //      log.log(actualKeyframe.getDebugString());
 //      log.log(percIn.getJointsDebugString());
 //      log.log("\n");
 //    }
   }
 
   /**
    * Internal method for translating data from a keyframe sequence to effector
    * commands.
    * 
    * Calculates for a joint the effector command, that should be executed in the
    * actual server cycle. 
    * 
    * @param angleIndex Index of the joint as defined in util.RobotConsts
    * @see util.RobotConsts
    */
   private double getSpeedFromAngleAndTime(int angleIndex) {
     double speed = 0f;
 
     double targetAngle = actualKeyframe.getAngle(angleIndex);
     double sensedAngle = Math.toDegrees(percIn.getJoint(angleIndex));
     
     if (leftCyclesForActualFrame > 1) {
       double angleDifference = 
               targetAngle - (sensedAngle + lastCycleAngles[angleIndex]);
               // Considering lastCycleAngles is important, because the 
               // server sends joint perceptor values (like all perceptor values)
               // delayed by one simulation step.
       double thisCycleAngle = 
               (angleDifference ) / (double)(leftCyclesForActualFrame - 1);
               // cycles - 1, because the last command is always 0
       speed =  (Math.toRadians(thisCycleAngle) / (double) TIME_STEMP_INTERVAL) * (double)1000;
               // multiplication by 1000, because speed has to be per sec, 
               // not per ms.
       lastCycleAngles[angleIndex] = thisCycleAngle;
     }
 
     return speed;
   }
 }
