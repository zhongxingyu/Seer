 package org.team691.meccanum;
 
 import edu.wpi.first.wpilibj.Encoder;
 import edu.wpi.first.wpilibj.SpeedController;
 
 public class PIDVelocityMotor {
 
     //Init data
     private String name;
     private SpeedController motor;
     private Encoder enc;
     //PIDMotor input
     private double target = 0.0;
     private double error = 0.0;
     private double deltaTime = 0.0;
     //PIDMotor scale
     private double kp = 0.0;
     private double ki = 0.0;
     private double kd = 0.0;
     private double max = 0.0;
     //PIDMotor out
     private double integral = 0.0;
     private double derivative = 0.0;
     private double out = 0.0;
     //PIDMotor loop
     private double lastError = 0.0;
     private long lastTime = 0;
 
     public PIDVelocityMotor(String name, SpeedController motor, Encoder enc, double[] pid) {
         this.name = name;
         this.motor = motor;
         this.enc = enc;
         this.kp = pid[0];
         this.ki = pid[1];
         this.kd = pid[2];
         this.max = pid[3];
     }
 
     //PIDMotor control
     public void run() {
         if(System.currentTimeMillis() - 10 > lastTime) {
             error = target - (enc.getRate() / 60);
             if(target == 0.0) {
                 integral = 0.0;
             }
             deltaTime = System.currentTimeMillis() - lastTime;
 
             integral += error * deltaTime;
             derivative = (error - lastError) / deltaTime;
             out = (kp * error) + (ki * integral) + (kd * derivative);
             motor.set(out / max);
            System.out.println("Name: " + name + " KP: " + kp + " Target: " + target + " CurrentRPM: " + (enc.getRate() / 60) + " Error: " + error + " Get(): " + enc.get() + " Out: " + out + "\n");
 
             lastError = error;
             lastTime = System.currentTimeMillis();
         }
     }
     
     //PIDMotor control
     public void run(double rpm) {
         target = rpm;
         run();
     }
     
     public boolean atTarget() {
        if(Math.abs(error - target) <= 5) {   //Test on final shooter!
             return true;
         } else {
             return false;
         }
     }
 }
