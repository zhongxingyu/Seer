 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates.subsystems;
 
 import edu.wpi.first.wpilibj.Accelerometer;
 import edu.wpi.first.wpilibj.Gyro;
 import edu.wpi.first.wpilibj.command.Subsystem;
 
 /**
  *
  * @author bhuang
  */
 public class GyroAccelerometer extends Subsystem {
     // Put methods for controlling this subsystem
     // here. Call these from Commands.
     private Gyro gyro;
     private Accelerometer accelerometerX;
     private Accelerometer accelerometerY;
 
 
     public void initDefaultCommand() {
         // Set the default command for a subsystem here.
         //setDefaultCommand(new MySpecialCommand());
     }
 
     public GyroAccelerometer(int gyroChannel,int accelerometerXChannel,int accelerometerYChannel){
        gyro = new Gyro(1, gyroChannel);
        accelerometerX = new Accelerometer(accelerometerXChannel);
        accelerometerY = new Accelerometer(accelerometerYChannel);
     }
 
     public double getAngle(){
         return gyro.getAngle();
     }
 
     public double getAccelerationX(){
         return accelerometerX.getAcceleration();
     }
 
     public double getAccelerationY(){
         return accelerometerY.getAcceleration();
     }
 
 }
