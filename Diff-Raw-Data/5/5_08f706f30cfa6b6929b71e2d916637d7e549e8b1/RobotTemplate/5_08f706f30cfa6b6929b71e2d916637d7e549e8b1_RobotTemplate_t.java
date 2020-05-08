 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package edu.wpi.first.wpilibj.templates;
 
 
 import edu.wpi.first.wpilibj.*;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the SimpleRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class RobotTemplate extends SimpleRobot {
     Solenoid in = new Solenoid(1),
             out = new Solenoid(2);
     Relay valve = new Relay(1);
     DigitalInput input = new DigitalInput(2);
             
     Joystick controller = new Joystick(1);
     /**
      * This function is called once each time the robot enters autonomous mode.
      */
     public void autonomous() {
         
     }
 
     /**
      * This function is called once each time the robot enters operator control.
      */
     boolean aButtonPressed = false,
             bButtonPressed = false,
             xButtonPressed = false,
             yButtonPressed = false;
     
     public void operatorControl() {
         while (this.isEnabled())
         {
             if (controller.getRawButton(1))
             {
                 aButtonPressed = true;
             }
             
             else if (aButtonPressed)
             {
                 aButtonPressed = false;
 
                 if (!aButtonPressed)
                 {
                     in.set(false);
                     out.set(false);
                 }
             }
 
             if (controller.getRawButton(2))
             {
                 bButtonPressed = true;
             }
             
             else if (bButtonPressed)
             {
                 bButtonPressed = false;
 
                 if (!bButtonPressed)
                 {
                     in.set(false);
                     out.set(false);
                 }
             }
 
             if (controller.getRawButton(3))
             {
                 xButtonPressed = true;
             }
             
             else if (xButtonPressed)
             {
                 xButtonPressed = false;
                 
                 if (!xButtonPressed)
                     valve.set(Relay.Value.kOff);
             }
             
             if (controller.getRawButton(4))
             {
                System.out.println("Controller y pressed");
                 yButtonPressed = true;
             }
             
             else if (yButtonPressed)
             {
                 yButtonPressed = false;
                 
                 valve.set(Relay.Value.kOff);
             }
 
             if (aButtonPressed)
             {
                 out.set(false);
                 in.set(true);
             }
 
             if (bButtonPressed)
             {
                 in.set(false);
                 out.set(true);
             }
 
             if (xButtonPressed)
             {
                 SmartDashboard.putBoolean("compressorOn", true);
                 valve.set(Relay.Value.kOn);
             }
             
            if (yButtonPressed)
                valve.setDirection(Relay.Direction.kBoth);
            
             
             SmartDashboard.putBoolean("compressorOn", xButtonPressed);
             SmartDashboard.putBoolean("pistonOut", aButtonPressed);
             SmartDashboard.putBoolean("pistonIn", bButtonPressed);
             
             SmartDashboard.putBoolean("pressure relief", input.get());
             //System.out.println("output " + input.get());
             
             if (input.get())
             {
                 System.out.println("INPUT RETURNED TRUE, SETTING VALVE TO OFF");
                 valve.set(Relay.Value.kOff);
             }
             
             Timer.delay(.1);
         }
     }
     
     /**
      * This function is called once each time the robot enters test mode.
      */
     public void test() {
     
     }
 }
