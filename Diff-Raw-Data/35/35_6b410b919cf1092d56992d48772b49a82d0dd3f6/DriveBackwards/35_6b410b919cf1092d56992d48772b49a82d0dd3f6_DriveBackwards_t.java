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
     
    private double m_timeout;
    private double m_speed;
    
    public DriveBackwards(double a_Timeout, double speed) 
     {
         requires(chassis);
        m_timeout = a_Timeout;
        m_speed = -speed;
     }
 
     // Called just before this Command runs the first time
     protected void initialize() 
     {
        setTimeout(m_timeout);    
     }
 
     // Called repeatedly when this Command is scheduled to run
     protected void execute() 
     {
        chassis.driveStraight(m_speed);
     }
 
     // Make this return true when this Command no longer needs to run execute()
     protected boolean isFinished() 
     {
         return isTimedOut();
     }
 
     // Called once after isFinished returns true
     protected void end() 
     {
        //Stop driving!
        chassis.driveStraight(0);
     }
 
     // Called when another command which requires one or more of the same
     // subsystems is scheduled to run
     protected void interrupted() 
     {
        end();
     }
 }
