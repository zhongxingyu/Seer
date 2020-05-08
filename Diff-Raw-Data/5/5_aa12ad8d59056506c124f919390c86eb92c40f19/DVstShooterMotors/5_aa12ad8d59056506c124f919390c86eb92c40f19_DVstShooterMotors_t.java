 package edu.wpi.first.wpilibj.templates.variablestores.dynamic;
 
 import edu.wpi.first.wpilibj.templates.DisableNotifable;
 import edu.wpi.first.wpilibj.templates.RobotMain;
 import edu.wpi.first.wpilibj.templates.debugging.DebugLevel;
 import edu.wpi.first.wpilibj.templates.debugging.DebugStatus;
 import edu.wpi.first.wpilibj.templates.debugging.RobotDebugger;
 import edu.wpi.first.wpilibj.templates.subsystems.ShooterMotors;
 
 /**
  *
  * @author daboross
  */
 public class DVstShooterMotors {
 
     private static final int REGULAR_CHANGE = 5;
     private static final int MAX_SPEED = 60;
 
     static {
         Notif n = new Notif();
         RobotMain.addDisableNotifable(n);
     }
     private static int speedPercentage;
 
     public static void addRegularAmount(ShooterMotors shooterMotor) {
         addPercentage(REGULAR_CHANGE, shooterMotor);
     }
 
     public static void subtractRegularAmount(ShooterMotors shooterMotor) {
         addPercentage(-REGULAR_CHANGE, shooterMotor);
     }
 
     public static void addPercentage(int percentage, ShooterMotors shooterMotor) {
         if (percentage != 0) {
            if (percentage > 0) {
                 if (speedPercentage != MAX_SPEED) {
                     if (speedPercentage + percentage > MAX_SPEED) {
                         speedPercentage = MAX_SPEED;
                     } else {
                         speedPercentage += percentage;
                     }
                     shooterMotor.setSpeed(speedPercentage / 100d);
                     pushState();
                 }
             } else {
                if (speedPercentage != 0) {
                     if (speedPercentage + percentage < 0) {
                         speedPercentage = 0;
                     } else {
                         speedPercentage += percentage;
                     }
                     shooterMotor.setSpeed(speedPercentage / 100d);
                     pushState();
                 }
             }
         }
     }
 
     private static void pushState() {
         RobotDebugger.push(new DebugStatus("ShooterMotors:Speed", "%" + speedPercentage, DebugLevel.HIGHEST));
 
     }
 
     public static class Notif implements DisableNotifable {
 
         private Notif() {
         }
 
         public void disable() {
             speedPercentage = 0;
             pushState();
         }
     }
 }
