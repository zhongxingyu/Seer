 package edu.first3729.frc2012;
 
import edu.wpi.first.wpilibj.Relay;
 
 /**
  * 
  * @class Manipulator
  * @brief Class that abstracts manipulator
  * 
  * Manages a bunch of relays that do stuff.
  */
 public class Manipulator
 {
     
     private Relay shooter1;
     private Relay shooter2;
     private Relay elevator;
     private Relay bridge;
     private Relay intake;
     private Relay net;
     
     public Manipulator()
     {
         shooter1 = new Relay(Params.shooter1_relay_port);
         shooter2 = new Relay(Params.shooter2_relay_port);
         elevator = new Relay(Params.elevator_relay_port);
         bridge = new Relay(Params.bridge_relay_port);
         intake = new Relay(Params.intake_relay_port);
         net = new Relay(Params.net_relay_port);
     }
     
     public void init()
     {
         shooter1.setDirection(Relay.Direction.kBoth);
         shooter1.set(Relay.Value.kOff);
         shooter2.setDirection(Relay.Direction.kBoth);
         shooter2.set(Relay.Value.kOff);
         elevator.setDirection(Relay.Direction.kBoth);
         elevator.set(Relay.Value.kOff);
         bridge.setDirection(Relay.Direction.kBoth);
         bridge.set(Relay.Value.kOff);
         intake.setDirection(Relay.Direction.kBoth);
         intake.set(Relay.Value.kOff);
     }
     
     public void shoot(boolean state)
     {
         if (state) {
             shooter1.set(Relay.Value.kForward);
             shooter2.set(Relay.Value.kForward);
         }
         else {
             shooter1.set(Relay.Value.kOff);
             shooter2.set(Relay.Value.kOff);
         }
         
     }
     
     public void lift(boolean state)
     {
         if (state)
             elevator.set(Relay.Value.kForward);
         else
             elevator.set(Relay.Value.kOff);
     }
     
     public void intake(Relay.Value state)
     {
         // Turn on until limit switch is pressed, then off
         this.intake.set(state);
         
     }
     
     public void bridge(Relay.Value state)
     {
         bridge.set(state);
     }
     
     public void lift_net(Relay.Value state)
     {
         net.set(state);
     }
 }
