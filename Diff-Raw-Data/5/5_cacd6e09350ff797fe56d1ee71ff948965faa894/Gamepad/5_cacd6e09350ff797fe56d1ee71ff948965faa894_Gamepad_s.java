 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.edinarobotics.utils.gamepad;
 
 import edu.wpi.first.wpilibj.Joystick;
 
 /**
  * Represents a gamepad with two joysticks and several buttons.
  * Makes interacting with a gamepad easier.
  */
 public class Gamepad extends Joystick{
     public static final int LEFT_X_AXIS = 1;
    public static final int LEFT_Y_AXIS = 2;
     public static final int RIGHT_X_AXIS = 3;
    public static final int RIGHT_Y_AXIS = 5;
     
     public static final int LEFT_BUMPER = 5;
     public static final int LEFT_TRIGGER = 7;
     public static final int RIGHT_BUMPER = 6;
     public static final int RIGHT_TRIGGER = 8;
     public static final int BUTTON_1 = 1;
     public static final int BUTTON_2 = 2;
     public static final int BUTTON_3 = 3;
     public static final int BUTTON_4 = 4;
     public static final int BUTTON_9 = 9;
     public static final int BUTTON_10 = 10;
                     
     
     public Gamepad(int port){
         super(port);
     }
     
     public double getLeftX(){
         return this.getRawAxis(LEFT_X_AXIS);
     }
     
     public double getLeftY(){
         return this.getRawAxis(LEFT_Y_AXIS);
     }
     
     public double getRightX(){
         return this.getRawAxis(RIGHT_X_AXIS);
     }
     
     public double getRightY(){
         return this.getRawAxis(RIGHT_Y_AXIS);
     }
 }
