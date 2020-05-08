 package org.usfirst.frc2022.subsystems;
 
 import edu.wpi.first.wpilibj.*;
 import edu.wpi.first.wpilibj.command.Subsystem;
 import org.usfirst.frc2022.RobotMap;
 
 
 /**
 * This code is used to control the speed of Jaguars controlling the shooter for
 * picking up Frisbees by taking input as the rotation speed of the encoder And
 * for output giving ability to modify speed. Note: names with "_" are from the
 * program, no "_" are from RobotMap
 *
  * @author Malachi Loviska
  */
 public class DriveEncoderSubsystem extends Subsystem {
 
     Encoder Drive_Encoder1;
     Encoder Drive_Encoder2;
 
     /*
      * Initialize  Encoders 
      */
     /**
      * Constructs the Jaguar, Encoder, and Solenoid and assigns ports from robot
      * map. It also starts the Encoder
      */
     public DriveEncoderSubsystem() {
 
 
 
         Drive_Encoder1 = new Encoder(RobotMap.DriveEncoder1[5],
                 RobotMap.DriveEncoder1[6]);
         Drive_Encoder2 = new Encoder(RobotMap.DriveEncoder2[7],
                 RobotMap.DriveEncoder1[8]);
 
 
 
 
         this.init();
 
     }
     public void initDefaultCommand(){
         
     }
 
     public void init() {
 
         Drive_Encoder1.setDistancePerPulse(1.0472 / 360);
         Drive_Encoder2.setDistancePerPulse(1.0472 / 360);
         Drive_Encoder1.start();
         Drive_Encoder2.start();
     }
 
     public void reset() {
 
         Drive_Encoder1.reset();
         Drive_Encoder2.reset();
     }
 
     public void getRightSpeed() {
         Drive_Encoder1.getRate();
 
     }
 
     public void getLeftSpeed() {
         Drive_Encoder2.getRate();
     }
 
     public void GetRightDistance() {
         Drive_Encoder1.getDistance();
     }
 
     public void GetLeftDistance() {
         Drive_Encoder2.getDistance();
     }
 }
