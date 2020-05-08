 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package edu.wpi.first.wpilibj.templates;
 
 
 import edu.wpi.first.wpilibj.Compressor;
 import edu.wpi.first.wpilibj.DriverStationLCD;
 import edu.wpi.first.wpilibj.IterativeRobot;
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.Relay;
 import edu.wpi.first.wpilibj.RobotDrive;
 import edu.wpi.first.wpilibj.Solenoid;
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.DigitalInput;
 
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the IterativeRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class RobotTemplate extends IterativeRobot {
     /*public Solenoid pistonUp;
     public Solenoid pistonDown;*/
     Solenoid solA, solB;
     RobotDrive drivetrain;
     Relay spikeA;
     Joystick leftStick;
     Joystick rightStick;
     //public String controlScheme = "twostick";
     int leftStickX, leftStickY;
     Compressor compressorA;    
     Jaguar leftJag;
     Jaguar rightJag;
     DriverStationLCD userMessages;
     String controlScheme = "twostick";
     Timer timer;
     DigitalInput switchA;
     
     /**
      * This function is run when the robot is first started up and should be
      * used for any initialization code.
      */
     public void robotInit() {
         //Instantialize objects for RobotTemplate
         rightStick = new Joystick(1);
         leftStick  = new Joystick(2);
         userMessages = DriverStationLCD.getInstance();
         
         //2-Wheel tank drive
         spikeA = new Relay(1);
         compressorA = new Compressor(1,2);
         drivetrain = new RobotDrive(1,2);
         solA = new Solenoid(1);
         solB = new Solenoid(2);
         //leftJag = new Jaguar(1);
         //rightJag = new Jaguar(2);
 
         /*pistonUp = new Solenoid(1);
         pistonDown = new Solenoid(2);
         sol3 = new Solenoid(3);
         sol4 = new Solenoid(4);
         sol5 = new Solenoid(5);*/
         
         //4-Wheel tank drive
         //Motors must be set in the following order:
         //LeftFront=1; LeftRear=2; RightFront=3; RightRear=4;
         //drivetrain = new RobotDrive(1,2,3,4);
         //drivetrain.tankDrive(leftStick, rightStick);
         /*pistonDown.set(true);
         pistonUp.set(true);*/
         switchA = new DigitalInput(2);//remember to check port
     }
 
     /**
      * This function is called periodically during autonomous
      */
     public void autonomousPeriodic() {
         drivetrain.drive(1, 0);
         Timer.delay(1000);
         drivetrain.drive(0, 0);
     }
 
     public void telopInit() {
         //drivetrain.setSafetyEnabled(true);
         //drivetrain.tankDrive(leftStick.getY(), rightStick.getY());
         //compressorA.start();
         //printMsg("Compressor started.");
     }
     
     /**
      * This function is called periodically during operator control
      */
     public void teleopPeriodic() {
         //getWatchdog().setEnabled(true);
         drivetrain.tankDrive(rightStick.getY(), leftStick.getY());
         
         if(!compressorA.enabled()){
         	compressorA.start();
                printMsg("Compressor started.");
         }
         
         /*
          * if (compressorA.getPressureSwitchValue() == true) {
             compressorA.stop();
             printMsg("Compressor stopped.  PressureSwitchValue \"True\".");
         }
         */
         
         //Pneumatics test code
         if (leftStick.getTrigger()) {
             solA.set(true);
             solB.set(false);
             printMsg("Solenoid opened.");
             //test for limit switch
             if(!switchA.get()){//if switch isn't tripped
             	printMsg("Moving motor.");
             	//move motor
             }
             else{
             	//stop motor/do stuff that needs to happen when motor stops
             }
         } 
         else {
             solA.set(false);
             solB.set(true);
            printMsg("Solenoid closed.");
         }
         
         if (rightStick.getTrigger()) {
         }
         else {
         }
         
         
         //Switch between "onestick" and "twostick" control schemes
         if (leftStick.getRawButton(6)) {
             controlScheme = "twostick";
         }
         if (leftStick.getRawButton(7)) {
             controlScheme = "onestick";
         }
         
         if (controlScheme.equals("twostick")) {
             drivetrain.tankDrive(rightStick, leftStick);
             printMsg("Tankdrive activated.");
         }
         else if (controlScheme.equals("onestick")) {
             drivetrain.arcadeDrive(leftStick);
             printMsg("Arcade drive activated.");
         }
         
         //Rotate in-place left and right, respectively
         if (leftStick.getRawButton(8)) {
             drivetrain.setLeftRightMotorOutputs(-1.0, 1.0);
             printMsg("Rotating counterclockwise in place.");
         }
         if (leftStick.getRawButton(9)) {
             drivetrain.setLeftRightMotorOutputs(1.0, -1.0);
             printMsg("Rotating clockwise in place.");
         }
 
         //userMessages.println(DriverStationLCD.Line.kMain6, 1, "This is a test" );
         userMessages.updateLCD();
     }
     
     /*public void disabledInit() {
         compressorA.stop();
     }*/
     
     public void printMsg(String message) {
         userMessages.println(DriverStationLCD.Line.kMain6, 1, message );
     }
 }
