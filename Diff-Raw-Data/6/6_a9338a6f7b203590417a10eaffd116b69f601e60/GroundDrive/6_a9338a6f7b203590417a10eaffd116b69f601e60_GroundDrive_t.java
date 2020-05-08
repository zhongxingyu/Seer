 package org.ingrahamrobotics.robot2013.subsystems;
 
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.RobotDrive;
 import edu.wpi.first.wpilibj.command.Subsystem;
 import org.ingrahamrobotics.robot2013.commands.grounddrive.RunGroundDrive;
 import org.ingrahamrobotics.robot2013.debugging.infos.DebugStatus;
 import org.ingrahamrobotics.robot2013.debugging.DebugInfoGroup;
 import org.ingrahamrobotics.robot2013.debugging.DebugLevel;
 import org.ingrahamrobotics.robot2013.debugging.DebugOutput;
 import org.ingrahamrobotics.robot2013.debugging.Debuggable;
 import org.ingrahamrobotics.robot2013.variablestores.VstM;
 import org.ingrahamrobotics.robot2013.vstj.VstJ;
 
 /**
  * Robot SubSystem GroundDrive. This is the main Driving SubSystem.
  */
 public class GroundDrive extends Subsystem implements Debuggable {
 
     private boolean driveReversed = false;
     private static final Jaguar leftMotor = new Jaguar(VstM.PWM.LEFT_MOTOR_PORT);
     private static final Jaguar rightMotor = new Jaguar(VstM.PWM.RIGHT_MOTOR_PORT);
     private static final RobotDrive roboDrive = new RobotDrive(leftMotor, rightMotor);
 
     static {
         roboDrive.setSafetyEnabled(false);
         roboDrive.stopMotor();
     }
 
     public GroundDrive() {
         System.out.println("SubSystem Created: GroundDrive");
     }
 
     public void initDefaultCommand() {
         setDefaultCommand(new RunGroundDrive());
     }
 
     /**
      * Sets a variable that will be multiplied by the input from whatever
      * joystick there is. Must be between 0 and 1.
      */
     public void setSpeedMutliplier(double d, boolean reversed) {
         driveReversed = reversed;
         if (d < 0 || d > 1) {
             throw new IllegalArgumentException();
         }
         multiplier = d;
     }
     private double multiplier;
 
     public void arcadeDriveRefresh() {
         arcadeDriveRefresh(VstJ.getArcadeDriveJoystick());
     }
 
     public void tankDriveRefresh() {
         tankDriveRefresh(VstJ.getTankDriveLeftJoystick(), VstJ.getTankDriveRightJoystick());
     }
 
     /**
      * The values will be changed by the speed multiplier and turn.
      */
     private void arcadeDriveRefresh(Joystick js) {
         if (js == null) {
             return;
         }
         double turn = (js.getX() * multiplier);
         double speed = (js.getY() * multiplier * (driveReversed ? -1 : 1));
         roboDrive.arcadeDrive(speed, turn);
     }
 
     private void tankDriveRefresh(Joystick leftJoystick, Joystick rightJoystick) {
         double leftSpeed = (leftJoystick.getY() * multiplier * (driveReversed ? -1 : 1));
         double rightSpeed = (rightJoystick.getY() * multiplier * (driveReversed ? -1 : 1));
        if (driveReversed) {
            roboDrive.tankDrive(rightSpeed, leftSpeed);
        } else {
            roboDrive.tankDrive(leftSpeed, rightSpeed);
        }
     }
 
     /**
      * The values will NOT be changed by the speed multiplier or turn.
      */
     public void arcadeDriveWithRaw(double speed, double turn) {
         roboDrive.arcadeDrive(speed, turn);
     }
 
     public void stop() {
         roboDrive.stopMotor();
     }
 
     public static void disabled() {
         roboDrive.stopMotor();
     }
 
     /**
      * Get Current Status Info.
      */
     public DebugOutput getStatus() {
         DebugStatus[] infoList = new DebugStatus[4];
         infoList[0] = new DebugStatus("GroundDrive:LeftMotor:Speed", leftMotor.get(), DebugLevel.MID);
         infoList[1] = new DebugStatus("GroundDrive:RightMotor:Speed", rightMotor.get(), DebugLevel.MID);
         infoList[2] = new DebugStatus("GroundDrive:Reversed", driveReversed, DebugLevel.MID);
         infoList[3] = new DebugStatus("GroundDrive:SpeedMultiplier", multiplier, DebugLevel.MID);
         return new DebugInfoGroup(infoList);
     }
 }
