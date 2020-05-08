 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates.commands;
 
 import edu.wpi.first.wpilibj.templates.OI;
 import edu.wpi.first.wpilibj.templates.variablestores.VstM;
 
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
         double climbSpeed = OI.getDriveJoystick().getRawAxis(VstM.Joysticks.Xbox.TRIGGERS);
         if (!VstM.Climber.isRetracting) {
             climbSpeed *= -1;
         }
         climber.runLadder(climbSpeed);
     }
 
     // Make this return true when this Command no longer needs to run execute()
     protected boolean isFinished() {
         return false;
     }
 
     // Called once after isFinished returns true
     protected void end() {
         climber.stop();
     }
 
     // Called when another command which requires one or more of the same
     // subsystems is scheduled to run
     protected void interrupted() {
         this.end();
     }
 }
