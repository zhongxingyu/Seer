 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package FRC2115.commands;
 
 /**
  *
  * @author Jordan
  */
 public class DriveBackwards extends CommandBase {
     
    public DriveBackwards(double a_Timeout) 
     {
         requires(chassis);
        setTimeout(a_Timeout);
     }
 
     // Called just before this Command runs the first time
     protected void initialize() 
     {
        
     }
 
     // Called repeatedly when this Command is scheduled to run
     protected void execute() 
     {
        chassis.driveStraight(-1);
     }
 
     // Make this return true when this Command no longer needs to run execute()
     protected boolean isFinished() 
     {
         return isTimedOut();
     }
 
     // Called once after isFinished returns true
     protected void end() 
     {
        
     }
 
     // Called when another command which requires one or more of the same
     // subsystems is scheduled to run
     protected void interrupted() 
     {
        
     }
 }
