 package org.dirtymechanics.frc.test.control;
 
 import edu.wpi.first.wpilibj.Joystick;
 
 /**
  * Essentially the same as the <CODE>edu.wpi.first.wpilibj.Joystick</CODE> class
  * with support for toggle buttons.
  */
 public class SmartJoystick extends Joystick {
     
     private ToggleButton[] buttons = new ToggleButton[12];
     
     public SmartJoystick(int id) {
         super(id);
     }
     
     /**
      * Registers a button as a toggle button.
      * @param button The button id.
      */
     public void registerToggleButton(int button) {
         buttons[button] = new ToggleButton(button);
     }
     
     /**
      * Gets the current state of the button, true for pressed, false for open.
      * If a toggle button entry exists it will use the state based on toggle
      * state rather than raw state.
      * @param id The id of the button.
      * @return The current state of the button.
      */
     public boolean getButtonState(int id) {
         boolean result = getRawButton(id);
         if (buttons[id] != null) {
             result = buttons[id].update();
         }
         return result;
     }
     
 
     /**
      * Represents a button that's state is controlled by a toggle switch.
      */
     private class ToggleButton {
         private final int id;
         private int flip = 0;
         private boolean lastState;
         private boolean current;
         private boolean state;
         
         public ToggleButton(int id) {
             this.id = id;
         }
         
         /**
          * Gets the current state of the button and does toggle calculations.
          * @return The current state.
          */
         public boolean update() {
             if (lastState != getRawButton(id)) {
                 if (flip++ % 2 == 0) {
                     state = !state;
                 }
             }
            lastState = current;
             return state;
         }
     }
 }
