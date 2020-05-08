 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.usfirst.frc4682.Audacity.subsystems;
 
 import edu.wpi.first.wpilibj.Talon;
 import edu.wpi.first.wpilibj.SpeedController;
 import org.usfirst.frc4682.Audacity.RobotMap;
 /**
  *
  * @author luis
  */
 
 // This subsystem should work.
 public class Shooter extends BaseSubsystem {
     SpeedController wheels = new Talon(RobotMap.leftShooterPort);
 
     public void initDefaultCommand() {
         
     }
     
     public void setSpeed(double speed) {
         if (enabled == false) {
             speed = 0;
         }
         wheels.set(speed);
     }
     
     public void stop() {
         setSpeed(0.0);
     }
     
     public double get() {
         return wheels.get();
     }
 }
