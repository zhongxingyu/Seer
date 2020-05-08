 package controller;
 
 import core.EventController;
 import core.HouseMech;
 import event.events.ButtonEvent;
 import event.listeners.ButtonListener;
 import java.util.Vector;
 import sensor.GRTXBoxJoystick;
 
 /**
  * Test controller for the haunted house mechanisms.
  * 
  * Controlled with an xbox joystick--buttons 4/5 change the mechanism
  * controlled, while button 0 toggles the selected mechanism.
  * 
  * @author agd
  */
 public class HauntedHouseTestController extends EventController implements ButtonListener {
 
     private GRTXBoxJoystick stick;
     private Vector mechanisms;
     private int currentControlledMech = 0;	//The index of the current mechanism we are controlling.
 
     /**
      * Instantiates a new haunted house.
      * 
      * @param mechanisms Vector with mechanisms
      * @param xbox XBox controller with which to control
      * @param name name of this controller
      */
     public HauntedHouseTestController(Vector mechanisms, GRTXBoxJoystick xbox, String name) {
         super(name);
         stick = xbox;
         this.mechanisms = mechanisms;
     }
     
     /**
      * Instantiates a new haunted house with no mechanisms
      * to begin with.
      * 
      * @param xbox XBox controller with which to control
      * @param name name of this controller
      */
     public HauntedHouseTestController(GRTXBoxJoystick xbox, String name) {
         this(new Vector(), xbox, name);
     }
     
     /**
      * Add a mechanism to the list of controlled mechanisms.
      * 
      * @param mech mechanism to control
      */
     public void addMech(HouseMech mech){
        mechanisms.add(mech);
     }
 
     protected void startListening() {
         this.stick.addButtonListener(this);
     }
 
     protected void stopListening() {
         this.stick.removeButtonListener(this);
     }
 
     public void buttonPressed(ButtonEvent e) {
         
         switch (e.getButtonID()) {
             case GRTXBoxJoystick.KEY_BUTTON_0:
                ((HouseMech) mechanisms.get(currentControlledMech)).toggle();
                 break;
             case GRTXBoxJoystick.KEY_BUTTON_4:
                 currentControlledMech++;
                 break;
             case GRTXBoxJoystick.KEY_BUTTON_5:
                 currentControlledMech--;
         }
         
         currentControlledMech %= mechanisms.size();
     }
 
     public void buttonReleased(ButtonEvent e) {
         // TODO Auto-generated method stub
     }
 }
