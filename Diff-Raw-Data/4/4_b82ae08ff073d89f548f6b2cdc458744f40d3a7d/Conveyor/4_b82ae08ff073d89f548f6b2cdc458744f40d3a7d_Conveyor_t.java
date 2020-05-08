 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.stuy.subsystems;
 
 import edu.wpi.first.wpilibj.DigitalInput;
 import edu.wpi.first.wpilibj.Victor;
 import edu.stuy.Constants;
 import edu.stuy.util.Gamepad;
 import edu.wpi.first.wpilibj.Timer;
 
 /**
  *
  * @author Arfan
  */
 public class Conveyor {
     
     private static Conveyor instance;
     private Victor roller;
     private DigitalInput lowerSensor;
     private double lastTime = 0.;
     private boolean isConveying;
     
     public static Conveyor getInstance() {
         if (instance == null)
             instance = new Conveyor();
         return instance;
     }
     
     private Conveyor() {
         roller = new Victor(Constants.CONVEYOR_CHANNEL);
         lowerSensor = new DigitalInput(Constants.LOWER_CONVEYOR_SENSOR);
         isConveying = false;
     }
     
     public void roll(double speed) {
         roller.set(speed);
     }
     
     public void convey() {
         roll(1);
         isConveying = true;
     }
     
     public void reverseConvey() {
         roll(-1);
         isConveying = false;
     }
     
     public void stop() {
         roll(0);
         isConveying = false;
     }
     public boolean isConveying() {
         return isConveying;
     }
     
     public boolean isBottomDiscDetected() {
         return lowerSensor.get();
     }
     
     public double getRoller() {
         return roller.get();
     }
     
     public void conveyAutomatic() {
         double time = Timer.getFPGATimestamp();
         if (Acquirer.getInstance().isAcquiring() && (isBottomDiscDetected())) {
             Lights.getInstance().setWhiteSignalLight(true);
             isConveying = true;
            lastTime = time;
         }
         if (isConveying()) {
             convey();        
         }
         if (time - lastTime >= 1.0) {
             stop();
             Lights.getInstance().setWhiteSignalLight(false);
            isConveying = false;
         }
             
     }
     
     public void manualConveyorControl(Gamepad gamepad) {
         if(gamepad.getRightTrigger()) {
             Acquirer.getInstance().acquire();
             conveyAutomatic();
         }
         else if(gamepad.getLeftAnalogButton()){
             convey();
         }
         else {
             stop();
         }
     }
     
     public void setLightsOn() {
         if (isBottomDiscDetected()) {
             Lights.getInstance().setWhiteSignalLight(true);
             Lights.getInstance().setColoredSignalLight(true);   
         }
     }
     
 }
 
