 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.usfirst.frc3946.UltimateAscent.commands;
 
 import edu.wpi.first.wpilibj.GenericHID;
 
 /**
  *
  * @author Gustave Michel
  */
 public class TankDrive extends CommandBase {
     
     public TankDrive() {
         // Use requires() here to declare subsystem dependencies
         // eg. requires(chassis);
         requires(driveTrain);
     }
 
     // Called just before this Command runs the first time
     protected void initialize() {
     }
 
     // Called repeatedly when this Command is scheduled to run
     protected void execute() {
        driveTrain.tankDrive(oi.getXbox().getY(GenericHID.Hand.kLeft), -1*oi.getXbox().getY(GenericHID.Hand.kRight));
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
