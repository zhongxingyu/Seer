 /* Currently managed by Tarun Sunkaraneni, and Ben Rose
  * This class manages all motor writing and managment. May need to deal with complex Gyro
  * input. 
  */
 package edu.ames.frc.robot;
 
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Relay;
 import edu.wpi.first.wpilibj.Victor;
 
 public class MotorControl {
 /*Motor control check list
  *Drive: Check
  *Shooter: Check
  *
  */
     //static RobotMap rm = new RobotMap();
     static Victor A;
     static Victor B;
     static Victor C;
     static Victor climb;
     static Relay push;
     static Jaguar shoot;
     static Jaguar tilt;
     static Jaguar asstclimb;
 
     void init() {
         A = new Victor(RobotMap.Apin);
         B = new Victor(RobotMap.Bpin);
         C = new Victor(RobotMap.Cpin);
         climb = new Victor(RobotMap.climbpin);
         asstclimb = new Jaguar(RobotMap.assistclimb);
        // push = new Relay(RobotMap.pushpin);
         shoot = new Jaguar(RobotMap.pushpin);
         tilt = new Jaguar(RobotMap.tiltpin);
     }
 
     void drive(double[] mv) {
         A.set(limit(mv[0]));
         B.set(limit(mv[1]));
         C.set(limit(mv[2]));
     }
     
     void climb(double power){
         power = limit(power);
         climb.set(power);
         asstclimb.set(power);
     }
 
     static double limit(double value) {
         if (value < -1) {
             value = -1;
         }
         if (value > 1) {
             value = 1;
         }
         return (value);
     }
 
     static double Climblimit(double inpow) {
        inpow = inpow * .1;

         if (inpow > .1) {
             inpow = .1;
         } else if (inpow < -.1) {
             inpow = -.1;
         }
         return inpow;
     }
 
     public void shooter(double power) {
         power = limit(power);
         shoot.set(power);
     }
 
     public void pusher(boolean active) {
         if (active) {
             push.set(Relay.Value.kForward);
         } else {
             push.set(Relay.Value.kOff);
         }
     }
 
     public void shootertilt(double tilt) {
         MotorControl.tilt.set(tilt);
     }
 
     public double[] addPivot(double[] motorval, double pivot) {
         pivot += RobotMap.pivotconstant;
         motorval[0] += pivot;
         motorval[1] += pivot;
         motorval[2] += pivot;
         return motorval;
     }
     /* Make sure the motors don't go full blast all the time */
 
     double[] setSpeedCap(double[] in, boolean boosted) {
         if (!boosted) {
 
             for (int i = 0; i < in.length; i++) {
                 in[i] = in[i] * RobotMap.speedcap;
             }
         } else if (boosted) {
             for (int i = 0; i < in.length & !boosted; i++) {
                 in[i] = in[i] * (RobotMap.speedcap - .2);
             }
         }
         return in;
     }
   
     /* This converts the direction we want to go (from 0 to 1, relative to the robot's base)
      * and speed (from 0 to 1) directly to values for the three omni-wheeled motors.
      */
     double[] convertHeadingToMotorCommands(double direction, double speed) {
         double[] motorvalue = new double[3];
         /* so, we'll define the direction we want to go as "forward". There are
          * 3 different points where only two motors will need to run (if the direction
          * is parallel to a motor's axle).
          */
         // 0 is what we define as the "front" motodrivemotorvalues - what we measure our heading angle from,
         // 1 is the motor one position clockwise from that, and
         // 2 is the motor one position counter-clockwise from 0.
         /*
         motorvalue[0] = speed * Math.sin(direction);
         motorvalue[1] = speed * Math.sin(direction - (2 * Math.PI / 3));
         motorvalue[2] = speed * Math.sin(direction + (2 * Math.PI / 3));
         */
         
         motorvalue[0] = speed * Math.cos(direction - (Math.PI / 4));
         motorvalue[1] = speed * -Math.sin(direction);
         motorvalue[2] = speed * -Math.cos(direction);
         
         return motorvalue;
     }
 }
