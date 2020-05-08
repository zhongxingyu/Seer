 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 //Standard for inceptus code
 package org.inceptus;
 
 //Imports
 import edu.wpi.first.wpilibj.IterativeRobot;
 import edu.wpi.first.wpilibj.DriverStation;
 import edu.wpi.first.wpilibj.IterativeRobot;
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.Watchdog;
 import edu.wpi.first.wpilibj.DriverStationLCD;
 
 //Mecanum class. We are using IterativeRobot as SimpleRobot was not working.
 public class Mecanum extends IterativeRobot {
     //Get the joysticks (keep joy* naming system so it can be adjusted later)
     Joystick joy1 = new Joystick(1);
     Joystick joy2 = new Joystick(2);
     Joystick joy3 = new Joystick(3);
     
     //Get the jaguars
     Jaguar front_right = new Jaguar(2);
     Jaguar front_left = new Jaguar(1);
     Jaguar rear_right = new Jaguar(3);
     Jaguar rear_left = new Jaguar(4);
     
     //If using iOS controls
     boolean iOS = false;
     
     //When robot starts
     public void robotInit() {
         //Disable the Watchdog as it can cause issues with this version of java.
         Watchdog.getInstance().setEnabled(false);
     }
     
     //When robot is disabled
     public void disabledInit() {
         //Stop the motors for safety
         rear_left.set(0);
         rear_right.set(0);
         front_left.set(0);
         front_right.set(0);
         
         //Log disabled status
         DriverStationLCD.getInstance().println(DriverStationLCD.Line.kUser2, 1, "Disabled");
     }
 
     //Periodically called during autonomous
     public void autonomousPeriodic() {
 
     }
     
     //When robot is started in autonomous
     public void autonomousInit() {
         //Set the iOS boolean to the opposite of digital input 1.
         iOS = !(DriverStation.getInstance().getDigitalIn(1));
         //Log initiation success
         DriverStationLCD.getInstance().println(DriverStationLCD.Line.kUser2, 1, "Autonomous Initiated");
     }
     
     //Called at the start of teleop
     public void teleopInit() {
         //Log initiation success
         DriverStationLCD.getInstance().println(DriverStationLCD.Line.kUser2, 1, "Teleop Initiated");
     }
     
     //Periodically called during teleop
     public void teleopPeriodic() {
         //Initiate the X and Y vars to be set later
         double X, Y, sensitivity;
         //Initiate the left and right booleans
         boolean turn_right = false;
         boolean turn_left = false;
         //Check if using iOS interface or not
         if(iOS){
             //Axis id's from http://comets.firstobjective.org/DSHelp.html
             X = joy1.getRawAxis(1);
             Y = joy1.getRawAxis(2);
             //Instead of buttons use joystick X axis
             if(joy1.getRawAxis(3) > .5){
                 turn_left = true;
             }else if(joy1.getRawAxis(3) < -.5){
                 turn_right = true;
             }
             //Make it 1 in iOS for now
             sensitivity = 1;
         }else{
             //Standard controls
             X = joy1.getX();
             Y = joy1.getY();
             //Use real buttons
             turn_left = joy1.getRawButton(4);
             turn_right = joy1.getRawButton(5);
            //Get sensitivity from Z axis and normalize to be out of 1 - 0
            sensitivity = (joy1.getZ()+1)/2;
            //Make sure it's not at 0 where nothing happens. It could be confusing later
            if(sensitivity < .3){
                sensitivity = .3;
            }
         }
         if(turn_right){
             inceptusDrive(-1, -1, -1, -1, sensitivity);
         }else if(turn_left){
             inceptusDrive(1, 1, 1, 1, sensitivity);
         }else{
             //Standard mecanum eqations without rotation
             double FL = (Y + X);
             double FR = (Y - X);
             double RL = (Y - X);
             double RR = (Y + X);
             //Setup max vlaue to be normalized
             double max = Math.abs(FL);
             if (Math.abs(FR)>max) max = Math.abs(FR);
             if (Math.abs(RL)>max) max = Math.abs(RL);
             if (Math.abs(RR)>max) max = Math.abs(RR);
             //If the valuess need to be normalized
             if (max>1){
                 FL/=max;
                 RL/=max;
                 FR/=max;
                 RR/=max;
             }
             
             inceptusDrive(FL, RL, FR, RR, sensitivity);
         }
     }
     
     //Routine to simplify driving the 4 motors
     public void inceptusDrive(double FL, double RL, double FR, double RR, double sensitivity)
     {
         //Drive
         front_left.set(FL * sensitivity);
         rear_left.set(RL * sensitivity);
         front_right.set(FR * sensitivity);
         rear_right.set(RR * sensitivity);
     }
 }
