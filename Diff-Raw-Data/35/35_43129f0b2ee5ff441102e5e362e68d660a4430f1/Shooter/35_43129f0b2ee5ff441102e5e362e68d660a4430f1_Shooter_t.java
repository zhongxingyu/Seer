 package org.dirtymechanics.frc.test.shooter;
 
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Relay;
 import org.dirtymechanics.frc.test.control.ControlScheme;
 
 /**
  * Contains all functions relevant to the shooting mechanism.
  */
 public class Shooter {
     private final Jaguar jaguar;
     private  Relay lowLift;
     private  Relay highLift;
     private final ControlScheme controls;
     
     /**
      * @param jaguar The channel for the shooter motor's jaguar.
      * @param lowlift The channel for the low lift's spike.
      * @param highLift The channel for the high lift's spike.
     */
     public Shooter(int jaguar, int lowLift, int highLift, ControlScheme controls) {
         this.jaguar = new Jaguar(jaguar);
         this.controls = controls;
         //this.lowLift = new Relay(lowLift);
         //this.highLift = new Relay(highLift);
     }
 
     public Shooter(Jaguar shooterJag, ControlScheme controls) {
         this.jaguar = shooterJag;
         this.controls = controls;
     }
     
     public void update() {
        System.out.println(controls.getShooterWheelState());
        if (controls.getShooterWheelState()) {
             setSpeed(1.0D);
         } else {
             setSpeed(0.0D);
         }
     }
     
     /**
      * Sets the speed of the shooter motor.
      * @param speed The desired speed.
      */
     public void setSpeed(double speed) {
         jaguar.set(speed);
     }
     
     /**
      * Raises all lifts.
      */
     public void raiseLift() {
         lowLift.set(Relay.Value.kForward);
         highLift.set(Relay.Value.kForward);
     }
     
     /**
      * Disables all lifts.
      */
     public void lowerLift() {
         lowLift.set(Relay.Value.kOff);
         highLift.set(Relay.Value.kOff);
     }
     
     /**
      * Sets the state of the low lift.
      * @param state True is on, false is off.
      */
     public void setLowLift(boolean state) {
         if (state) {
             lowLift.set(Relay.Value.kForward);
         } else {
             lowLift.set(Relay.Value.kOff);
         }
     }
     
     /**
      * Sets the state of the high lift.
      * @param state True is on, false is off.
      */
     public void setHighLift(boolean state) {
         if (state) {
             highLift.set(Relay.Value.kForward);
         } else {
             highLift.set(Relay.Value.kOff);
         }
     }
 }
