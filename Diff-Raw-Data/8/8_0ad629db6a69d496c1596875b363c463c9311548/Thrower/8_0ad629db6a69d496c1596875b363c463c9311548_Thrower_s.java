 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Robot.FoximusPrime.UltimateAscent.RobotParts;
 
 import Robot.FoximusPrime.UltimateAscent.UltimateAscentRobot;
 import edu.wpi.first.wpilibj.DoubleSolenoid;
 import edu.wpi.first.wpilibj.GenericHID;
 import edu.wpi.first.wpilibj.Jaguar;
 
 /**
  *
  * @author edward
  */
 public class Thrower extends UltimateAscentRobotPart{
     
     private Jaguar wheel;
     private double rate;
     private double pk = .05;
     private DoubleSolenoid loader;
     
     public Thrower(UltimateAscentRobot robot){
         super(robot);
         loader = new DoubleSolenoid(1,2);
         wheel = new Jaguar(3);
         rate = 0;
     }
     
     public void updateTeleop(){
         
         if(!robot.getSensors().getRightJoystick().getRawButton(9))
            wheel.set(robot.getSensors().getRightJoystick().getY() < 0?robot.getSensors().getRightJoystick().getY():0);
         
         if(robot.getSensors().getRightJoystick().getY() == 0){
             controlRate();
         }
         
         if(robot.getSensors().getRightJoystick().getRawButton(2))
             rate = 0;
         if(robot.getSensors().getRightJoystick().getRawButton(2))
             rate = robot.getSensors().getRightJoystick().getY();
         
         if(robot.getSensors().getRightJoystick().getTrigger())
            loader.set(DoubleSolenoid.Value.kForward);
         else
            loader.set(DoubleSolenoid.Value.kOff);
     }
     
     private void controlRate(){
         wheel.set(wheel.getSpeed()+((rate-robot.getSensors().getFrontEncoder().getRate())*pk));
     }
     
     public void setRate(double nrate){
         rate = nrate;
     }
 }
