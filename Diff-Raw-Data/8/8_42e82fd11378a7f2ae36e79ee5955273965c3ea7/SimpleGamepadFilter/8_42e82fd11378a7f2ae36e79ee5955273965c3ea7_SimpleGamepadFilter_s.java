 package com.edinarobotics.utils.gamepad.gamepadfilters;
 
 import com.edinarobotics.utils.gamepad.GamepadAxisState;
 import com.edinarobotics.utils.math.Vector2;
 
 /**
  * SimpleGamepadFilter is an abstract class used to quickly implement a gamepad
  * filter that applies a single function to each axis of the gamepad.
  * To implement a full GamepadFilter from SimpleGamepadFilter it is only
  * necessary to override {@link #applyFilter(double)}.
  */
 public abstract class SimpleGamepadFilter implements GamepadFilter {
     
     /**
      * The filter function of SimpleGamepadFilter applies
      * {@link #applyFilter(double)} to the values of each axis of the gamepad.
      * @param toFilter The GamepadAxisState to be filtered.
      * @return A new GamepadAxisState representing the filtered values of the
      * axes.
      */
     public GamepadAxisState filter(GamepadAxisState toFilter){
        double leftX = toFilter.getLeftJoystick().getX();
        double leftY = toFilter.getLeftJoystick().getY();
        double rightX = toFilter.getRightJoystick().getX();
        double rightY = toFilter.getRightJoystick().getY();
         Vector2 left = new Vector2(leftX, leftY);
         Vector2 right = new Vector2(rightX, rightY);
         return new GamepadAxisState(left, right);
     }
     
     /**
      * This function is called with the values of each gamepad axis in order
      * to produce the new filtered values. This function must be overridden
      * by SimpleGamepadFilter subclasses to produce a full filter.
      * @param value The value of the gamepad axis.
      * @return The filtered value of the gamepad axis.
      */
     protected abstract double applyFilter(double value);
 }
