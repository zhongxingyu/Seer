 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.ames.frc.robot;
 //We need to test this.
 import com.sun.squawk.util.MathUtils;
 import edu.wpi.first.wpilibj.Joystick;
 /* List of buttons/toggles needed
  * Manual pivot toggle: 2
  * Speed boost button: Active joystick push
  * Force shoot button: 4 
  * Pivot Left : 5
  * Pivot Right : 6
  * Force Realign button: 7
  * Stop auto-target toggle: 10
  * Activate frisbee grab button: 8
  * Launch climb procedure: Simultaneously &  5,6,7,8,9, and 10.
  *
  */
 
 public class InputManager {
 //Git is good
 
     static RobotMap rm = new RobotMap();
     protected static Joystick ps2cont;
     // protected static boolean dzactive  = false; // In case we want to check for deadzoneing being active
     //  protected static double[] axisOC = new double[2]; // Stores the original copies of the axis reads, for use elsewhere.
     protected static button manpivot;
     protected static button fireButton;
     protected static button pivotRight;
     protected static button pivotLeft;
     protected static button realign;
     protected static button infrisbee;
     protected static button autotarg;
     protected static button speedBoost;
 
     void init() {
         ps2cont = new Joystick(1);
         manpivot = new button(true, 2);
         fireButton = new button(false, 4);
         pivotRight = new button(false, 6);
         pivotLeft = new button(false, 5);
         realign = new button(false, 7);
         infrisbee = new button(false, 8);//Activates the frisbee retriever 
         autotarg = new button(true, 10);
         speedBoost = new button(false, 11);
     }
 
     public static double[] getPureAxis() { // Gets, stores, and returns the status of the joysticks on the PS2 Controller
         /* We will use a double dimension arry to hold the joystick data so that everything can be sent to other functions.
          * Both of the first dimensions will hold 2 doulbes, the first is the x & y axis of the first (paning) joystick
          * The second dimension holds the x & y for the second (pivoting) joystick
          */
        // double[] axis = new double[2];// Variable for storing all that data
         double[] dir = new double[2];
         dir[0] = -ps2cont.getRawAxis(1);// X
         dir[1] = ps2cont.getRawAxis(2);// Y
       //  axis[2] = -ps2cont.getRawAxis(3);// X
         //      axisOC[0] = axis[0][0]; 
         //    axisOC[1] = axis[0][1];
         //       axis[1][1] = PS2Cont.getRawAxis(4);// Y We dont actually need this value
         dir = deadzone(dir);
         //dir = ramp(dir);
         dir = translate(dir);
         return (dir); // Returns axis data to the caller.
     }
     public static double getPivot(){
         double pivot = -ps2cont.getRawAxis(3);
         pivot = rampSingle(pivot);
         return (pivot);
     }
 
     protected static double[] deadzone(double[] axis) {// Checks for deadzone
         //This is a skeleton of the deadzone funtion. Mark should fill this in.
 
         // for(byte li = 0; li <= axis.length; li++){//Loops through first dimesion of array
         for (byte si = 0; si < axis.length; si++) {//loops through second dimension of array.
             if (axis[si] <= rm.deadzone && axis[si] >= -rm.deadzone) {
                 axis[si] = 0;
             }
         }
         //  }
         return (axis);
     }
 
     protected static double[] ramp(double[] axis) {
         for (byte ri = 0; ri < axis.length; ri++) {
            axis[ri] = MathUtils.pow(axis[ri], rm.expo_ramp);
           // axis[ri] = (2/3)*MathUtils.pow(axis[ri], RobotMap.expo_ramp)+(1/3)*axis[ri];
         }
         return (axis);
     }
     protected static double rampSingle(double axis) {
         
            axis = MathUtils.pow(axis, rm.expo_ramp);
           // axis[ri] = (2/3)*MathUtils.pow(axis[ri], RobotMap.expo_ramp)+(1/3)*axis[ri];
         return (axis);
     }
     protected static double[] translate(double[] axis) {// Translates final input values into a format for use by the rest of the code.
         //This is a skeleton of the ramp funtion. Mark should fill this in
         double[] vect = new double[2];
         double speed = 0;
         double angle = 0;
         //     double hypo = 0;
         speed = Math.sqrt(MathUtils.pow(axis[0], 2) + MathUtils.pow(axis[1], 2));
         //angle = RobotArithmetic.arcTangent(axis[0], axis[1]);
         angle = MathUtils.atan2(axis[0], axis[1]);
 
         if (angle < 0) {
             angle = (2 * Math.PI) - Math.abs(angle);
         }
 
         vect[0] = angle;
         vect[1] = speed;
         return (vect);
     }
 
     public static class button {
 
         public button(boolean isToggle, int pin) {
             toggle = isToggle;
             bpin = pin;
         }
         boolean state;
         int bpin;
         boolean toggle;
     }
     //protected static double[] translate(double[][] axis){// Translates deadzoned and scaled inputs into whatever exact type of input MotorControl needs/wants.
     //This is a skeleton of the translate funtion. Mark should fill this in.
     //return (ABC);
     // }
 }
