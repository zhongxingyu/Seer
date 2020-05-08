 /* Currently managed by Tarun Sunkaraneni, and Ben Rose
  * This class manages all motor writing and managment. May need to deal with complex Gyro
  * input. 
  */
 package edu.ames.frc.robot;
 
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Relay;
 import edu.wpi.first.wpilibj.Victor;
 
 public class MotorControl {
     static RobotMap rm = new RobotMap();
     static Victor A;
     static Victor B;
     static Victor C;
     static Relay col;
     static Jaguar shoot;
     
 
     void init() {
         A = new Victor(rm.Apin);
         B = new Victor(rm.Bpin);
         C = new Victor(rm.Cpin);
         col = new Relay(5);
         shoot = new Jaguar(4);
     }
     
     void drive(double[] mv) {
         A.set(limit(mv[0]));
         B.set(limit(mv[1]));
         C.set(limit(mv[2]));
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
 
     public void shooter(double power) {
         if (power < -1) {
             power = -1;
         }
         if (power > 1) {
             power = 1;
         }
         shoot.set(power);
     }
 
     /* Make sure the motors don't go full blast all the time */
     double[] setSpeedCap(double[] in) {
         for (int i = 0; i < in.length; i++) {
             in[i] = in[i] * rm.speedcap;
         }
         return in;
     }
     /* This converts the direction we want to go (from 0 to 1, relative to the robot's base)
      * and speed (from 0 to 1) directly to values for the three omni-wheeled motors.
      */
     public void rotationDirection(int state) {
         if (state > 0) {
             col.set(Relay.Value.kForward);
         } else if (state < 0) {
             col.set(Relay.Value.kReverse);
         } else if (state == 0) {
             col.set(Relay.Value.kOff);
         }
     }
 
     //the col motor either goes front, back or stays there.
     double[] convertHeadingToMotorCommands(double direction, double speed, double pivot) {
         double[] motorvalue = new double[3];
         /* so, we'll define the direction we want to go as "forward". There are
          * 3 different points where only two motors will need to run (if the direction
          * is parallel to a motor's axle).
          */
         // 0 is what we define as the "front" motodrivemotorvalues - what we measure our heading angle from,
         // 1 is the motor one position clockwise from that, and
         // 2 is the motor one position counter-clockwise from 0.
         motorvalue[0] = speed * Math.sin(direction);
         motorvalue[1] = speed * Math.sin(direction - (2 * Math.PI / 3));
         motorvalue[2] = speed * Math.sin(direction + (2 * Math.PI / 3));
         
        pivot += RobotMap.pivotconstant;
         
         motorvalue[0] += pivot;
         motorvalue[1] += pivot;
         motorvalue[2] += pivot;
 
         /*
          if (pivot < 0) {
          motorvalue[0] = -pivot;
          motorvalue[1] = -pivot;
          motorvalue[2] = -pivot;
          }
          if (pivot > 0) {
          motorvalue[0] = +pivot;
          motorvalue[1] = +pivot;
          motorvalue[2] = +pivot;
          }
          */
 
         return motorvalue;
     }
 }
