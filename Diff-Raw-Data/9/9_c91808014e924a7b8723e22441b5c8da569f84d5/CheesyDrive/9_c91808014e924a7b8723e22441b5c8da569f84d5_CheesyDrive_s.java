 package org.team751.cheesy;
 
 /**
  * Calculates values for drivetrain control. Ported from team 254's C++
  * implementation in their 2012 code.
  *
 * @author Sam Crow
  */
 public class CheesyDrive {
 
     /**
      * Rotation value from the last loop
      */
     private double oldWheel = 0;
     /*
      * TODO: Document this
      */
     private double quickStopAccumulator = 0;
     /*
      * TODO: document this
      */
     private double neg_inertia_accumulator = 0;
 
     //<editor-fold defaultstate="collapsed" desc="MotorOutputs class">
     /**
      * Holds a left motor output ratio and a right motor output ratio
      */
     public static class MotorOutputs {
 
         /**
          * The left motor output ratio, -1 to 1
          */
         public double left;
         /**
          * The right motor output ratio, -1 to 1
          */
         public double right;
 
         /**
          * Constructor
          *
          * @param left The left motor output ratio, -1 to 1
          * @param right The right motor output ratio, -1 to 1
          */
         public MotorOutputs(double left, double right) {
             if (Math.abs(left) > 1) {
                 throw new IllegalArgumentException("Left value must be -1 to 1");
             }
             if (Math.abs(right) > 1) {
                 throw new IllegalArgumentException("Right value must be -1 to 1");
             }
 
             this.left = left;
             this.right = right;
         }
     }
     //</editor-fold>
 
     /**
      * Calculate motor power values, using team 254's algorithm
      *
      * @param throttle Move value: +1 is full speed forwards, -1 is full speed
      * backwards
      * @param wheel Rotation value: -1 is full left, +1 is full right
      * @param isQuickTurn If quick turn mode should be enabled
      * @return the motor outputs that should be set
      */
     public MotorOutputs cheesyDrive(double throttle, double wheel, boolean isQuickTurn) {
 
         double wheelNonLinearity = CheesyDriveConstants.kWheelNonLinearity;
 
         double neg_inertia = wheel - oldWheel;
         oldWheel = wheel;
 
         // Apply a sin function that's scaled to make it feel better.
         wheel = Math.sin((Math.PI / 2.0) * wheelNonLinearity * wheel) / Math.sin((Math.PI / 2.0) * wheelNonLinearity);
         wheel = Math.sin((Math.PI / 2.0) * wheelNonLinearity * wheel) / Math.sin((Math.PI / 2.0) * wheelNonLinearity);
         wheel = Math.sin((Math.PI / 2.0) * wheelNonLinearity * wheel) / Math.sin((Math.PI / 2.0) * wheelNonLinearity);
 
         double left_pwm, right_pwm, overPower;
         double sensitivity;
 
         double angular_power;
         double linear_power;
 
         // Negative inertia!
         double neg_inertia_scalar;
 
         if (wheel * neg_inertia > 0) {
             neg_inertia_scalar = CheesyDriveConstants.kNegInertiaLowMore;
         } else {
             if (Math.abs(wheel) > 0.65) {
                 neg_inertia_scalar = CheesyDriveConstants.kNegInertiaLowLessExt;
             } else {
                 neg_inertia_scalar = CheesyDriveConstants.kNegInertiaLowLess;
             }
         }
         sensitivity = CheesyDriveConstants.kSenseLow;
 
         if (Math.abs(throttle) > CheesyDriveConstants.kSenseCutoff) {
             sensitivity = 1 - (1 - sensitivity) / Math.abs(throttle);
         }
 
         double neg_inertia_power = neg_inertia * neg_inertia_scalar;
         neg_inertia_accumulator += neg_inertia_power;
         wheel = wheel + neg_inertia_accumulator;
         if (neg_inertia_accumulator > 1) {
             neg_inertia_accumulator -= 1;
         } else if (neg_inertia_accumulator < -1) {
             neg_inertia_accumulator += 1;
         } else {
             neg_inertia_accumulator = 0;
         }
         linear_power = throttle;
         // Quickturn!
         if (isQuickTurn) {
             if (Math.abs(linear_power) < 0.2) {
                 double alpha = CheesyDriveConstants.kQuickStopTimeConstant;
                 quickStopAccumulator = (1 - alpha) * quickStopAccumulator + alpha * pwmLimit(wheel) * CheesyDriveConstants.kQuickStopStickScalar;
             }
             overPower = 1.0;
             angular_power = wheel;
         } else {
             overPower = 0.0;
             angular_power = Math.abs(throttle) * wheel * sensitivity - quickStopAccumulator;
             if (quickStopAccumulator > 1) {
                 quickStopAccumulator -= 1;
             } else if (quickStopAccumulator < -1) {
                 quickStopAccumulator += 1;
             } else {
                 quickStopAccumulator = 0.0;
             }
         }
         right_pwm = left_pwm = linear_power;
         left_pwm += angular_power;
         right_pwm -= angular_power;
         if (left_pwm > 1.0) {
             right_pwm -= overPower * (left_pwm - 1.0);
             left_pwm = 1.0;
         } else if (right_pwm > 1.0) {
             left_pwm -= overPower * (right_pwm - 1.0);
             right_pwm = 1.0;
         } else if (left_pwm < -1.0) {
             right_pwm += overPower * (-1.0 - left_pwm);
             left_pwm = -1.0;
         } else if (right_pwm < -1.0) {
             left_pwm += overPower * (-1.0 - right_pwm);
             right_pwm = -1.0;
         }
 
         return new MotorOutputs(left_pwm, right_pwm);
     }
 
     /**
      * Limits a PWM to plus or minus 1
      *
      * @param value the input PWM
      * @return the capped PWM value
      */
     private static double pwmLimit(double value) {
         if (value > 1) {
             return 1;
         }
         if (value < -1) {
             return -1;
         }
         return value;
     }
 }
