 package com.edinarobotics.utils.gamepad;
 
 /**
  * This is a convenience class that makes it easier to implement a toggle
  * button.
  */
 public class ToggleHelper {
     private boolean toggleReady;
     
     /**
      * Constructs a new ToggleHelper.
      */
     public ToggleHelper(){
         toggleReady = false;
     }
     
     /**
      * Checks if the boolean value is toggled with respect to previous values.
      * @param latestState the current boolean value.
      * @return {@code true} if the value is toggled, {@code false} otherwise.
      */
     public boolean isToggled(boolean latestState){
        if(!latestState){
             toggleReady = true;
         }
        else if(latestState && toggleReady){
             toggleReady = false;
             return true;
         }
         else{
             toggleReady = false;
         }
         return false;
     }
 }
