 package com.edinarobotics.utils.wheel;
 
 import com.edinarobotics.utils.common.Updatable;
 import edu.wpi.first.wpilibj.SpeedController;
 
 /**
  * Wheel represents an abstract wheel that has a {@link SpeedController}
  * implementation.
  */
 public class Wheel implements Updatable {
     private SpeedController speedController;
     private String name;
     private double power;
     
     /**
      * Constructs a Wheel object.
      * @param name A name for the PID Tuning Bench to identify this wheel.
      * Eg. "FRONT_LEFT"
      * @param speedController The speed controller for this wheel's motor.
      */
     public Wheel(String name, SpeedController speedController) {
         this.name = name;
         this.speedController = speedController;
         this.power = 0;
     }
     
     /**
      * Returns the speed controller associated with this wheel.
      * @return The speed controller object.
      */
     public SpeedController getSpeedController() {
         return speedController;
     }
 
     /**
      * Returns the name of this wheel.
      * @return The wheel's name.
      */
     public String getName() {
         return name;
     }
 
     /**
      * Returns the current power of this wheel.
      * @return Current power of the wheel
      */
     public double getPower() {
         return power;
     }
     
     /**
      * Sets the power of the speed controller.
      * @param power 
      */
     public void setPower(double power) {
        this.power = power;
         update();
     }
     
     /**
      * Updates the speed controller to the set speed.
      */
     public void update() {
         speedController.set(power);
     }
     
 }
