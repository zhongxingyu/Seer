 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.usfirst.frc2022.subsystems;
 
 import edu.wpi.first.wpilibj.Relay;
 import edu.wpi.first.wpilibj.Relay.Direction;

 import edu.wpi.first.wpilibj.command.Subsystem;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 import org.usfirst.frc2022.RobotMap;
import org.usfirst.frc2022.commands.PickupDumbCommand;
 
 /**
  *
  * @author Michael
  */
 public class Pickup extends Subsystem{
 
     static int Forward = 1;
     static int Backward = 2;
     static int Stopped = 0;
     
     Relay[] spikes;
     
             
             
     protected void initDefaultCommand() {
        this.setDefaultCommand(new PickupDumbCommand());
     }
     
     /*
      * Constructor for the pickup device. Register the spikes at
      * the assigned ports if they match the number defined in
      * RobotMap.java
      */
     public Pickup(){
         if (RobotMap.pickupPorts.length == RobotMap.pickupPortNum){
             for(int i=0; i<RobotMap.pickupPortNum;i++){
                 spikes[i] = new Relay(RobotMap.pickupPorts[i]);
             }
             SmartDashboard.putString("Pickup: ", "Working Fine");
         } else {
             SmartDashboard.putString("Pickup: ", "Does not match specified number in RobotMap.java");
         }
     }
     
     /**
      * Set each of the motors to either forward, backward, or stop
      * 
      * @param states An array holding the states.
      */
     public void manualSet(int[] states){
         for(int i=0; i<states.length;i++){
             Direction dirs = getDirs(states[i]);
             spikes[i].setDirection(dirs);
         }
     }
     
     /**
      * Set the pickup device forward.
      */
     public void roll(){
         for(int i=0;i<spikes.length;i++){
             spikes[i].setDirection(Relay.Direction.kForward);
         }
     }
     
     /**
      * Set the pickup device backward.
      */
     public void drop(){
         for(int i=0;i<spikes.length;i++){
             spikes[i].setDirection(Relay.Direction.kForward);
         }
     }
     
     /**
      * Stop the pickup device.
      */
     public void stop(){
         for(int i=0;i<spikes.length;i++){
             spikes[i].setDirection(Relay.Direction.kBoth);
         }
     }
     
     //if(stop()&&drop()&&roll()) {fire!!!!!!!!!}
     
     /**
      * Convert direction from an integer to a direction.
      * 
      * @param dir The direction in integer form.
      * @return The direction
      */
     public Direction getDirs(int dir){
         if(dir == Forward){return Relay.Direction.kForward;}
         else if (dir == Backward) {return Relay.Direction.kReverse;}
         else {return Relay.Direction.kBoth;}
     }

    
     
 }
