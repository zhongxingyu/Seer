 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package core;
 import edu.wpi.first.wpilibj.Solenoid;
 /**
  *
  * @author Troy-Jameson(Trevor)
  */
 public class Piston extends Solenoid {
    //Creates solenoids
     Solenoid solenoidOne;
     Solenoid solenoidTwo;
     
     public Piston(int portOne, int portTwo, int portThree, int portFour) {
         super(portOne, portTwo);
        //Declares solenoids
         solenoidOne = new Solenoid(portOne, portTwo);
         solenoidTwo = new Solenoid(portThree, portFour);
     }
     public void solenoidSwitch(){
         //Switches the solenoids, one is true, one false, just switches which in is which
         if (solenoidOne.get()) {
             solenoidOne.set(false);
             
         }
         //Switches in the other instance, when solenoidTwo is true, not solenoidOne
         if (solenoidTwo.get()) {
             solenoidTwo.set(false);
         }
     }   
     public void on() {
         //Sets solenoids in on position
         solenoidOne.set(true);
         solenoidTwo.set(false);
     }
     public void off() {
         //Sets solenoids in off position
         solenoidOne.set(false);
         solenoidTwo.set(true);
     }
 }
