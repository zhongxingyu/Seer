 package org.usfirst.frc4682.Audacity;
 
 /**
  * The RobotMap is a mapping from the ports sensors and actuators are wired into
  * to a variable name. This provides flexibility changing wiring, makes checking
  * the wiring easier and significantly reduces the number of magic numbers
  * floating around.
  */
 
 /*
  * Fix shooting bug when enabled
  * Make drive reductor throttle start at 50
  * x axis arcade mode for tank drive from shooter joystick
  * reduce speed when turning in trank drive
  *  button to toggle reduction on shooter stick drive from constant to stick 2 throttle
  * 
  * when the shooter stick runs, the tank drive sticks enabled
  */
 public class RobotMap {
     
     // PWM ports
     public static final int leftShooterPort = 1;
     public static final int rightShooterPort = 2;
     public static final int FeederPort = 8;
     public static final int leftDrivePort = 9;
     public static final int rightDrivePort = 10;
     
     // Digital I/O ports
     public static final int startLimitSwitchPort = 1;
     public static final int endLimitSwitchPort = 2;
     
     // constants
    public static final double feederSpeed = 0.4; // 0.5 is too fast
     public static final double shooterSpeed = 0.9;
     
     // oi
 }
