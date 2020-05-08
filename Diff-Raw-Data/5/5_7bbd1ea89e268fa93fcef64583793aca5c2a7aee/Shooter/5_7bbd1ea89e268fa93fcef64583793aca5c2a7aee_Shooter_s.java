 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates.subsystems;
 
 import edu.wpi.first.wpilibj.AnalogChannel;
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Talon;
 import edu.wpi.first.wpilibj.command.Subsystem;
 //import edu.wpi.first.wpilibj.templates.MagneticEncoder;
 import edu.wpi.first.wpilibj.templates.RobotMap;
 import edu.wpi.first.wpilibj.templates.OI;
 
 /**
  *
  * @author abbottk
  */
 public class Shooter extends Subsystem {
 
    public Talon ShootTalon;
     public Talon ReloadTalon;
     public AnalogChannel RangeFinder;
     
     //private AnalogChannel ultrasonic = new AnalogChannel(RobotMap.ultrasonicInput);
     
 //    private static MagneticEncoder magneticencoder = new MagneticEncoder(RobotMap.shooterEncoderPos);
 
     public Shooter() {
        ShootTalon = new Talon(RobotMap.ShooterMotor);
         ReloadTalon = new Talon(RobotMap.ReloadMotor);
     }
     protected void initDefaultCommand() {
     
     
     }
     
     public void setShooterMotor(double speed){
         ShootTalon.set(speed);
         
     }
     
     public void setReloadMotor(double speed){
         ReloadTalon.set(speed);
     }    
     
     public double getRange(){
         
         double range = this.RangeFinder.getVoltage()/.098;
         return range;
     }
     
     public double getRangeFT(){
         
         return (getRange()/12);
     }
     
 //    public double getUltrasonic(){
 //     
 //        return ultrasonic.getVoltage();
 //    }
     
 //    public double getUltrasonicRange(){
 //        
 //        double temp = ((5/512)*ultrasonic.getVoltage());
 //        ScraperBike.debugPrint(temp);
 //        return (temp);
 //    }
 
 //    public double getRotationsPeriod(){
 //        return magneticencoder.getPeriod();
 //    }
 //    
 //    public double getRotations(){
 //        return magneticencoder.getIntegerCounter();
 //    }
 //    
 //    public double getRotationsDouble(){
 //        return magneticencoder.getDoubleCounter();
 //    }
 
 }
