 package edu.wpi.first.wpilibj.templates.commands;
 
 import edu.wpi.first.wpilibj.templates.debugging.DebugLevel;
 import edu.wpi.first.wpilibj.templates.debugging.DebugOutput;
 import edu.wpi.first.wpilibj.templates.debugging.Debuggable;
 import edu.wpi.first.wpilibj.templates.debugging.InfoState;
 import edu.wpi.first.wpilibj.templates.debugging.RobotDebugger;
 import edu.wpi.first.wpilibj.templates.variablestores.dynamic.DVstPressure;
 
 /**
  * Autonomous Command.
  *
  * @author daboross
  */
 public class AutoCommand extends CommandBase implements Debuggable {
 
     /**
      * This is how long the robot should keep the solenoid extended before
      * retracting it again (In milliseconds).
      */
     private static final long timeSolenoidExtendedMillis = 500;
     /**
      * This is how long the robot should keep the solenoid retracted before
      * extending it again (In milliseconds).
      */
     private static final long timeSolenoidRetractedMillis = 1000;
     /**
      * This is the minimum amount of time the robot will wait before shooting
      * first time (In milliseconds).
      */
     private static final long timeTillFirstMillis = 5000;
     /**
      * This is the maximum amount of time the robot will wait before shooting
      * first time (In milliseconds).
      */
     private static final long maxWaitTimeMillis = 10000;
     /**
      * 0 is just started.
      *
      * 1 is solenoid retracted.
      *
      * 2 is solenoid extended.
      */
     private int state = 0;
     /**
      * This is the start time when the robot last changed state (Extended or
      * Retracted the solenoid).
      */
     private long lastStateChangeTime;
     private long startTime;
 
     private String getReadableState() {
         if (state == 0) {
             return "Speeding Up";
         } else if (state == 1) {
             return "Solenoid Retracting";
         } else if (state == 2) {
             return "Solenoid Extending";
         } else {
             return "?\"" + state + "\"?";
         }
     }
 
     /**
      * First check if the minimum time before shooting has passed. If it hasn't,
      * then return false. Otherwise, if the maximum time has passed, return
      * true. Otherwise, if the time is at least minimum and not yet maximum
      * waiting time, return whether or not the compression system is at
      * pressure.
      */
     private boolean isReadyToShoot() {
         long timeSinceStart = System.currentTimeMillis() - startTime;
         return (timeSinceStart < timeTillFirstMillis) ? false : ((timeSinceStart > maxWaitTimeMillis) ? true : DVstPressure.atPressure());
     }
 
     private boolean readyForNextAction() {
         if (state == 0) {
             return isReadyToShoot();
         } else if (state == 1) {
            return System.currentTimeMillis() - lastStateChangeTime <= timeSolenoidRetractedMillis;
         } else if (state == 2) {
            return System.currentTimeMillis() - lastStateChangeTime <= timeSolenoidExtendedMillis;
         }
         System.out.println("[AutoCommand] readyForNextAction() called while state is " + state);
         return true;
     }
 
     public AutoCommand() {
         requires(groundDrive);
         requires(shooterMotors);
         requires(shooterSolenoids);
     }
 
     public void reInitValues() {
         initialize();
     }
 
     protected void initialize() {
         setState(0);
         groundDrive.stop();
         shooterSolenoids.extend();
         shooterMotors.setSpeed(1.0);
     }
 
     protected void execute() {
         if (state == 0) {
             if (readyForNextAction()) {
                 setState(1);
             }
         } else if (state == 1) {
             if (readyForNextAction()) {
                 setState(2);
             }
         } else if (state == 2) {
             if (readyForNextAction()) {
                 setState(1);
             }
         }
     }
 
     /**
      * Sets the state variable, as well as does some changes to other variables
      * according to which state. This calls shooterSolenoids.retract() if
      * setState is 1, and shooterSolenoids.extend() if setState is 2. This also
      * calls RobotDebugger.push(this).
      */
     private void setState(int setState) {
         if (setState == 0) {
             startTime = System.currentTimeMillis();
         } else if (setState == 1) {
             lastStateChangeTime = System.currentTimeMillis();
             shooterSolenoids.retract();
         } else if (setState == 2) {
             lastStateChangeTime = System.currentTimeMillis();
             shooterSolenoids.extend();
         } else {
             throw new IllegalArgumentException("Invalid State");
         }
         this.state = setState;
         RobotDebugger.push(this);
     }
 
     protected boolean isFinished() {
         return false;
     }
 
     protected void end() {
         shooterMotors.setSpeed(0);
         setState(0);
     }
 
     public DebugOutput getStatus() {
         return new InfoState("AutoCommand", getReadableState(), DebugLevel.HIGH);
     }
 }
