 
 package edu.wpi.first.wpilibj.templates;
 
 //imports from First
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.buttons.JoystickButton;
 
 //imports from our own code
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 import edu.wpi.first.wpilibj.templates.commands.StartPickupBall;
 import edu.wpi.first.wpilibj.templates.commands.StopPickupBall;
 import edu.wpi.first.wpilibj.templates.commands.ShootBall;
 import edu.wpi.first.wpilibj.templates.RobotMap;
 import edu.wpi.first.wpilibj.templates.commands.TestButton;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 
 
 /**
  * the Operator Interface, defines port numbers, joysticks, and commands
  * @author 2399 Programmers
  */
 public class OI {
     // Process operator interface input here.
     
     public static int leftStickNum = 1;
     public static int rightStickNum = 3;
     public static int feedButtPort = 4;
     public static int shooterStickNum = 2;
     public static int shooterButtNum = 1;
     
     
     
     Joystick leftStick = new Joystick(leftStickNum);
     Joystick rightStick = new Joystick(rightStickNum);
     Joystick shooterStick = new Joystick(shooterStickNum);
     StartPickupBall feedOn = new StartPickupBall();
     StopPickupBall feedOff = new StopPickupBall();
     ShootBall shoot = new ShootBall();
     
     private final JoystickButton feedButt = new JoystickButton(leftStick, feedButtPort);
     private final JoystickButton shooterButt = new JoystickButton(shooterStick,shooterButtNum);
     
     
     /**
      * When the PickupBall feeder button "feedbutt" is pressed, the PickupBall feeder is turned on,
      * and when it is released the feeder stops.
      */
     public OI(){
         feedButt.whenPressed(feedOn);
         feedButt.whenReleased(feedOff);
         shooterButt.whenPressed(shoot);
         
<<<<<<< HEAD
       
=======
>>>>>>> b5d1ff07f9f033b9d813e85ca5a31c89a716f689
       
           
     }
     
     /**
      * gets the speed of the left joystick based on the y-axis position
      * @return the y position of the left joystick
      */
     public double getLeftSpeed() {
         //System.out.println("leftStick.getY() returns" + leftStick.getY());
         return -leftStick.getY();
     }
     
     /**
      * gets the speed of the right joystick based on the y-axis position
      * @return the y position of the right joystick
      */
     public double getRightSpeed() {
         //System.out.println("rightStick.getY() returns" + rightStick.getY());
         return -rightStick.getY();
     }
 }
 
