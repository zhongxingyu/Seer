 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.rmr662.frc2012.component;
 
 import com.rmr662.frc2012.generic.Component;
 import com.rmr662.frc2012.physical.RMRJaguar;
 import edu.wpi.first.wpilibj.DigitalInput;
 
 /**
  *
  * @author RMR Programming
  */
 public class ShooterArm extends Component {
 
     private static ShooterArm instance;
     
     //TODO: put in motor channel, switch channel, and motor speed
     private static int MOTOR_CHANNEL;
     private static int SWITCH_CHANNEL;
     private static double MOTOR_SPEED;
     
     private static boolean MOTOR_IS_WIRED_WRONG = false;
     
     private RMRJaguar motor;
     private DigitalInput limitSwitch;
     private boolean shooting = false;
 
     public ShooterArm() {
         motor = new RMRJaguar(MOTOR_CHANNEL);
         motor.setInverted(MOTOR_IS_WIRED_WRONG);
         limitSwitch = new DigitalInput(SWITCH_CHANNEL);
     }
 
     public void update() {
         boolean localShooting;
         synchronized (this) {
             localShooting = shooting;
         }
         if (limitSwitch.get() && !localShooting) {
             motor.stopMotor();
         } else {
             motor.set(MOTOR_SPEED);
         }
     }
 
     public void reset() {
         synchronized (this) {
             shooting = false;
         }
 
         motor.stopMotor();
     }
 
     public String getRMRName() {
         return "Shooter Arm";
     }
 
     public static ShooterArm getInstance() {
         if (instance == null) {
             instance = new ShooterArm();
         }
         return instance;
     }
 
     public synchronized void setShooting(boolean shoot) {
        shooting = shoot;
     }
 }
