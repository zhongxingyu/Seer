 package org.usfirst.frc4682.Audacity;
 
 import edu.wpi.first.wpilibj.buttons.Button;
 import edu.wpi.first.wpilibj.buttons.JoystickButton;
 //import edu.wpi.first.wpilibj.buttons.DigitalIOButton;
 import edu.wpi.first.wpilibj.Joystick;
 import org.usfirst.frc4682.Audacity.commands.StopShooter;
 
 /**
  * This class is the glue that binds the controls on the physical operator
  * interface to the commands and command groups that allow control of the robot.
  */
 public class OI {
     Joystick leftStick = new Joystick(1);
     Joystick rightStick = new Joystick(2);
 
     Button leftButton2 = new JoystickButton(leftStick, 2);
     Button rightButton2 = new JoystickButton(rightStick, 2);
     
     public OI() {
         leftButton2.whenPressed(new StopShooter());
         rightButton2.whenPressed(new StopShooter());
     }
     
     public double getLeftThrottle() {
        return (leftStick.getRawAxis(3) + 1)/2;
     }
     
     public double getRightThrottle() {
        return (rightStick.getRawAxis(3) + 1)/2;
     }
 }
 
