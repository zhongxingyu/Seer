 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.ames.frc.robot;
 
 import edu.wpi.first.wpilibj.Joystick;
 import com.sun.squawk.util.MathUtils;
 /* List of buttons/toggles needed
  * Manual pivot toggle: 2
  * Speed boost button: Active joystick push
  * Force shoot button: 4 
  * Force Realign button: 7
  * Stop auto-target toggle: 10
  * Activate frisbee grab button: 8
  * Launch climb procedure: Simultaneously &  5,6,7,8,9, and 10.
  *
  */
 
 public class InputManager {
 //Git is good       double[] ABC = new double[3]; // Element 0 is the direction for A, the relative front end of the robot. The other two elements are arranged clockwise so the bottom right corner of the triangle is B and the other is C
        // ABC[0] = 
    protected static Joystick ps2control = new Joystick(1);
     protected static RobotMap Rm = new RobotMap();
     protected static boolean dzactive  = false; // In case we want to check for deadzoneing being active
     
     public static class directions{
         public directions(){
         }
     }
     
     public static double[] GetPureAxis() { // Gets, stores, and returns the status of the joysticks on the PS2 Controller
         /* We will use a double dimension arry to hold the joystick data so that everything can be sent to other functions.
          * Both of the first dimensions will hold 2 doulbes, the first is the x & y axis of the first (paning) joystick
          * The second dimension holds the x & y for the second (pivoting) joystick
          */
         double[][] axis = new double[2][2];// Variable for storing all that data
         double[] pwr = new double [3];
        axis[0][0] = ps2control.getRawAxis(1);// X
        axis[0][1] = ps2control.getRawAxis(2);// Y
        axis[1][0] = ps2control.getRawAxis(3);// X
  //       axis[1][1] = PS2Cont.getRawAxis(4);// Y We dont actually need this value
         axis = deadzone(axis);
         return (pwr); // Returns axis data to the caller.
     }
 
     protected static double[][] deadzone(double[][] axis) {// Checks for deadzone
         //This is a skeleton of the deadzone funtion. Mark should fill this in.
         if(axis[0][0] <= Rm.deadzone | axis[0][0] >= -Rm.deadzone){
             axis[0][0] = 0;
             dzactive = true;
         }
         if(axis[0][1] <= Rm.deadzone | axis[0][1] >= -Rm.deadzone){
             axis[0][1] = 0;
             dzactive = true;
     }
         if(axis[1][0] <= Rm.deadzone | axis[1][0] >= -Rm.deadzone){
             axis[1][0] = 0;
             dzactive = true;
         }
  /*       if(axis[1][1] <= Rm.deadzone | axis[1][1] >= -Rm.deadzone){
             axis[1][1] = 0;
             dzactive = true;
     }
         */
         return (axis);
     }
     protected static double[] translate(double[][] axis){// Ramps inputs so that they curve all happy like.
         //This is a skeleton of the ramp funtion. Mark should fill this in.
         double speed = 0;
         double angle = 0;
         double hypo = 0;
         double[] vect = new double[2];
         speed = Math.sqrt(MathUtils.pow(axis[0][0],2) + MathUtils.pow(axis[0][1], 2));
         //Tangent thingy
         vect[0] = angle;
         vect[1] = speed;
         return (vect);
     }
     //protected static double[] translate(double[][] axis){// Translates deadzoned and scaled inputs into whatever exact type of input MotorControl needs/wants.
         
         //This is a skeleton of the translate funtion. Mark should fill this in.
         
         //return (ABC);
    // }
 }
