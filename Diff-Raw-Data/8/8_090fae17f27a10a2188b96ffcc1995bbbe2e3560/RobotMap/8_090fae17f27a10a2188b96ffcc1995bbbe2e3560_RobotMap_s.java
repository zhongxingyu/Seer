 package com.badrobots.y2012.technetium;
 
 /**
  * The RobotMap is a mapping from the ports sensors and actuators are wired into
  * to a variable name. This provides flexibility changing wiring, makes checking
  * the wiring easier and significantly reduces the number of magic numbers
  * floating around.
  */
 public class RobotMap
 {
     public static final int cRIOsidecar = 1; // port from the cRIO to the digital
                                              // sidecar
 
    public static final int lFront = 4;
    public static final int rFront = 1;
    public static final int lBack = 3;
    public static final int rBack = 2;
 
     public static final int leftJoystick = 1;
     public static final int rightJoystick = 2;
     public static final int controller = 3;
 
     public static final int shooterJoystick = 4;
     public static final int ultrasonicOut = 0; // TODO
     public static final int ultrasonicIn = 0; // TODO
     public static final int shooterMotor = 8;
 
 
     public static final int camera = 4;
     
     public static final int gyro = 1;
 
 }
