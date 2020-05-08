 
 package edu.wpi.first.wpilibj.technobots;
 
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.buttons.Button;
 import edu.wpi.first.wpilibj.buttons.DigitalIOButton;
 import edu.wpi.first.wpilibj.buttons.JoystickButton;
 import edu.wpi.first.wpilibj.technobots.commands.SweeperOn;
 
 /**
  * This class is the glue that binds the controls on the physical operator
  * interface to the commands and command groups that allow control of the robot.
  */
 public class OI {
 
     Joystick xbox;
    Button button1;
 
     public OI() {
 
         xbox = new Joystick(1);
        button1 = new  JoystickButton(xbox,1);
 
        button1.whenPressed(new SweeperOn());
     }
 }
 
