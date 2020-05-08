 package org.dirtymechanics.frc.test.control.impl;
 
 import org.dirtymechanics.frc.test.control.ControlScheme;
 import org.dirtymechanics.frc.test.control.SmartJoystick;
 
 public class StandardControlScheme implements ControlScheme {
     private static final int TRANSMISSION_SWITCH = 3;
     private static final int SHOOTING_SWITCH = 1;
     
     private final SmartJoystick left;
     private final SmartJoystick right;
     private final SmartJoystick pad;
 
     public StandardControlScheme(SmartJoystick left, SmartJoystick right, SmartJoystick pad) {
         this.left = left;
         this.right = right;
         this.pad = pad;
         right.registerToggleButton(SHOOTING_SWITCH);
     }
     
 
     public double getLeftDrivePower() {
         return -left.getY();
     }
 
     public double getRightDrivePower() {
         return -right.getY();
     }
     
     public boolean getTransmissionState() {
         return right.getButtonState(TRANSMISSION_SWITCH);
     }
 
    public boolean isShooting() {
         return right.getButtonState(SHOOTING_SWITCH);
     }
 }
