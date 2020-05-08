 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.badrobots.y2012.technetium.commands;
 
 
 /**
  *
 * @author Jon Buckley
  */
 public class AutoOrient extends CommandBase
 {
     double distance = 0;
 
     public AutoOrient()
     {
        // requires(sensors);
         requires(driveTrain);
     }
 
     // Called just before this Command runs the first time
     protected void initialize() 
     {
         
     }
 
     // Called repeatedly when this Command is scheduled to run
     protected void execute()
     {
         distance = sensors.getDifferenceInSensorsFromWall(true);
 
         if (distance > 3)
            driveTrain.polarMechanum(0.3, 0, Math.sin(distance)); // sin has range of -1 to 1
         else if(distance < -3)
            driveTrain.polarMechanum(0.3, 0, -(Math.sin(distance)));
         else
             driveTrain.autoMechanumDrive(0.5, 0.0, 0.0);
     }
 
     // Make this return true when this Command no longer needs to run execute()
     protected boolean isFinished()
     {
        // if (sensors.getDifferenceInSensorsFromWall(true) < 3) // difference is negligble
             return true;
 
        // return false;
     }
 
     // Called once after isFinished returns true
     protected void end() {
     }
 
     // Called when another command which requires one or more of the same
     // subsystems is scheduled to run
     protected void interrupted() {
     }
 }
