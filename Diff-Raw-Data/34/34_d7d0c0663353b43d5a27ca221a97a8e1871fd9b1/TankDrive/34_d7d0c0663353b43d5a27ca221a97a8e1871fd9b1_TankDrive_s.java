 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.usfirst.frc4682.Audacity.commands;
 
 import com.sun.squawk.util.MathUtils;
 /**
  *
  * @author luis
  */
 public class TankDrive extends CommandBase {
     double leftSpeed;
     double rightSpeed;
     
     public TankDrive() {
         requires(driveTrain);
     }
 
     // Called just before this Command runs the first time
     protected void initialize() {
         System.out.print("Starting tank drive...\n");
         if (oi.getSecondThrottle() == 0.0) {
             System.out.print("WARNING: Your drive throttle reductor is set to 0.\n");
         }
     }
     
 
     // Called repeatedly when this Command is scheduled to run
     protected void execute() {
 
 
         if (shooterCanControl()) { // shooter guy takes over drive
             driveTrain.arcadeDrive(0, oi.thirdStick.getX() * 0.6);
             // 0.6 is the reductor value for the shooter override because this
             // is a precision thing
         }
         else {
             leftSpeed = oi.getFirstY(); // -1 to 1
             rightSpeed = oi.getSecondY();
             
             leftSpeed = curve(leftSpeed);
             rightSpeed = curve(rightSpeed);
             driveTrain.tankDrive(leftSpeed, rightSpeed);
         }
     }
 
     protected double curve(double StickY) {
         StickY *= reducer();
        return MathUtils.pow(StickY, 1.0/3.0);
     }
     
     protected double reducer() {
         double throttle = oi.getSecondThrottle();
         double YStickDiff = Math.abs(oi.getFirstY() - oi.getSecondY());
         return (0.5 * throttle + 0.5)/*must add up to 1, added val is the new 0*/ * (1.0 - (0.2 * throttle * YStickDiff));
     }           /*   0.6*throttle +0.4   0.4 is the zero*/                          /*^  -( ^ * 2 )is max reduction val */
     
     protected boolean shooterCanControl() {
         boolean shooterMoving = (oi.getThirdX() < -0.2 || oi.getThirdX() > 0.2); // correct
         
         // override threshold is 0.3
         boolean shooterOverrides = Math.abs(oi.getFirstY()) < 0.3; // correct
         shooterOverrides = shooterOverrides && Math.abs(oi.getSecondY()) < 0.3; // correct
         return shooterMoving && shooterOverrides;
     }
     
     // Make this return true when this Command no longer needs to run execute()
     protected boolean isFinished() {
         return false;
     }
 
     // Called once after isFinished returns true
     protected void end() {
     }
 
     // Called when another command which requires one or more of the same
     // subsystems is scheduled to run
     protected void interrupted() {
         System.out.print("Tank Drive interrupted!\n");
     }
 }
