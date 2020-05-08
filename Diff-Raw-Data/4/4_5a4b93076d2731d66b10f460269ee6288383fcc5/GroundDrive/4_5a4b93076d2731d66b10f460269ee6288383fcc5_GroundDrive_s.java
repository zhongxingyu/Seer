 package edu.wpi.first.wpilibj.templates.subsystems;
 
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.RobotDrive;
 import edu.wpi.first.wpilibj.command.Subsystem;
 import edu.wpi.first.wpilibj.templates.commands.GroundDriveCommand;
 import edu.wpi.first.wpilibj.templates.debugging.DebugStatus;
 import edu.wpi.first.wpilibj.templates.debugging.DebugInfoGroup;
 import edu.wpi.first.wpilibj.templates.debugging.DebugOutput;
 import edu.wpi.first.wpilibj.templates.debugging.Debuggable;
 import edu.wpi.first.wpilibj.templates.variablestores.VstM;
 import edu.wpi.first.wpilibj.templates.vstj.VstJ;
 
 /**
  * Robot SubSystem GroundDrive.
  */
 public class GroundDrive extends Subsystem implements Debuggable {
 
     private static Jaguar leftMotor = new Jaguar(VstM.PWM.LEFT_MOTOR_PORT);
     private static Jaguar rightMotor = new Jaguar(VstM.PWM.RIGHT_MOTOR_PORT);
     private static RobotDrive roboDrive;
 
     public GroundDrive() {
         roboDrive = new RobotDrive(leftMotor, rightMotor);
         roboDrive.stopMotor();
     }
 
     public void initDefaultCommand() {
         setDefaultCommand(new GroundDriveCommand());
     }
 
     /**
      * Sets a variable that will be multiplied by the input from whatever
      * joystick there is. Must be between 0 and 1.
      */
     public void setSpeedMutliplier(double d) {
         if (d < 0 || d > 1) {
             throw new IllegalArgumentException();
         }
         multiplier = d;
     }
     private double multiplier;
 
     public void driveWithDefaultController() {
         driveWithController(VstJ.getDefaultJoystick());
     }
     private Joystick lastController;
 
     public void driveWithController(Joystick js) {
         if (js == null) {
             return;
         }
         lastController = js;
        double speed = multiplier * js.getX();
        double turn = multiplier * js.getY();
         roboDrive.arcadeDrive(speed, turn);
     }
 
     public void driveWithLast() {
         driveWithController(lastController);
     }
 
     /**
      * Get Current Status Info.
      */
     public DebugOutput getStatus() {
         DebugStatus[] infoList = new DebugStatus[3];
         infoList[0] = new DebugStatus("GroundDrive:LeftMotor:Speed", leftMotor.get());
         infoList[1] = new DebugStatus("GroundDrive:RightMotor:Speed", rightMotor.get());
         infoList[2] = new DebugStatus("GroundDrive:SpeedMultiplier", multiplier);
         return new DebugInfoGroup(infoList);
     }
 
     public void stop() {
         roboDrive.stopMotor();
     }
 }
