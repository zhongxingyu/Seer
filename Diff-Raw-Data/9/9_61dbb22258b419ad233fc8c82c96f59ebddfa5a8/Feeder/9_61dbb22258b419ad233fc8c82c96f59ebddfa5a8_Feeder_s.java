 
 package edu.wpi.first.wpilibj.templates.subsystems;
 
 import edu.wpi.first.wpilibj.command.Subsystem;
 import edu.wpi.first.wpilibj.templates.RobotMap;
 import edu.wpi.first.wpilibj.Victor;
 import edu.wpi.first.wpilibj.templates.commands.FeedBallOff;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 
 
 
 /**
  * A Subsystem that contains methods for controlling the feeder
  * @author Jessie and Emma
  */
 public class Feeder extends Subsystem {
     // Put methods for controlling this subsystem
     // here. Call these from Commands.
     private Victor feedMotor = new Victor(RobotMap.feedMotor);
 
 
     /**
      * Default: PickupBall feeder speed is zero, feeder is off
      */
     public void initDefaultCommand() {
        // Set the default command for a subsystem here.
         //setDefaultCommand(new MySpecialCommand());
         
         //default is set to zero
         setDefaultCommand(new FeedBallOff());
     }
     
     /**
      * sets the feeder speed
      * @param speed double between -1 and 1
      */
     public void setFeederSpeed(double speed){
            feedMotor.set(speed);
     }
 }
 
