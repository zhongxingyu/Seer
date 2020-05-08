 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates.commands;
 
 import edu.wpi.first.wpilibj.Joystick;
 
 /**
  *
  * @author Robotics
  */
 public class RunClimber extends CommandBase {
     
     public RunClimber() {
         requires(climber);
     }
 
     // Called just before this Command runs the first time
     protected void initialize() {
         climber.stop();
     }
 
     // Called repeatedly when this Command is scheduled to run
     protected void execute() {
        double climbSpeed = oi.getDriveJoystick().getAxisChannel(Joystick.AxisType.kThrottle);
         System.out.println(climbSpeed);
     }
 
     // Make this return true when this Command no longer needs to run execute()
     protected boolean isFinished() {
         return false;
     }
 
     // Called once after isFinished returns true
     protected void end() {
     }
 
     // Called when another command which requires one or more of the same
     // subsystems is scheduled to run
     protected void interrupted() {
     }
 }
