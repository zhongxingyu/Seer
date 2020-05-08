 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package util;
 
 /*  Shor Hand Names
     chn = channel
     enc = encoder
     bt = button
     ls = limit switch
 */
/**
 * All of the configurations.
 */
 public class Config
 {
     // Drive Related
     public static final int driveMaxSpeed = 10;     // Feet per second
     public static final int chnDriveLeft1 = 7;
     public static final int chnDriveRight1 = 8;
     
     // Ejector Related
     public static final double ejectorSpeed = 0.76536;
     public static final int ejectorTime = 2;
     public static final int chnLsClose = 14;
     public static final int chnLsFar = 13;
     public static final int chnEjector = 10;
     
     // Joystick Related
     public static final int chnJoystick = 1;
     public static final int buttonsOnJoystick = 12;
     public static final int btEjector = 3;
     
     // Output Related
     public static final int driveId = 1;
     public static final int ejectorId = 2;
     public static final int joystickId = 3;
     public static final int talonId = 4;
     public static int[] outputArray = { driveId, ejectorId, joystickId, talonId };
 }
