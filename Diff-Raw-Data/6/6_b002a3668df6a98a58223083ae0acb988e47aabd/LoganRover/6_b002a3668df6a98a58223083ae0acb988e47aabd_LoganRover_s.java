 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package robot;
 
 import edu.wpi.first.wpilibj.DigitalInput;
 import edu.wpi.first.wpilibj.IterativeRobot;
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.RobotDrive;
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.Victor;
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the IterativeRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class LoganRover extends IterativeRobot {
     public static final double AUTO_CORNER_FORWARD_TIME = 2000; //Time for autonomous, in milliseconds
     public static final double AUTO_MIDDLE_FORWARD_TIME = 1000;
 
     private Joystick joystickOne;                   //Drive/Manipulator Joystick
     
     private DigitalInput leftAutoSwitch;            //Left Auto Switch
     private DigitalInput rightAutoSwitch;           //Right Auto Switch
 
     private Timer autoTimer;                        //Normally would use match time, but can't find that.
     
     private RobotDrive drivetrain;                  //Motors on Drivetrain
     private Intake intake;                          //Intake System
 
     private Jaguar leftDrive, rightDrive;           //Drivetrain Motors
     private Victor intakeMotorOne, intakeMotorTwo;  //Intake Motors
     private Victor unobtaniumMotor;                 //Also part of intake system
 
     /**
      * This function is run when the robot is first started up and should be
      * used for any initialization code.
      */
     public void robotInit() {
         joystickOne = new Joystick(1);
         leftAutoSwitch = new DigitalInput(1);
         rightAutoSwitch = new DigitalInput(2);
 
         autoTimer = new Timer();
 
         leftDrive = new Jaguar(3);
         rightDrive = new Jaguar(1);
         intakeMotorOne = new Victor(2);
         intakeMotorTwo = new Victor(4);
         unobtaniumMotor = new Victor(5);
 
         drivetrain = new RobotDrive(leftDrive, rightDrive);
         drivetrain.setInvertedMotor(RobotDrive.MotorType.kFrontLeft, true);
 
         intake = new Intake(joystickOne, intakeMotorOne, intakeMotorTwo, unobtaniumMotor);
     }
 
     /**
      * This function is called before the beginning of autonomous.
      */
     public void autonomousInit() {
         autoTimer.start();
     }
 
     /**
      * This function is called periodically during autonomous
      */
     public void autonomousPeriodic() {
         //TODO: Is SimpleRobot better for our autonomous? Do I need to call isAutonomous()?
         //TODO: Clean up repetitive code in autonomous
         getWatchdog().feed();
 
         double elapsedTime = autoTimer.get() * 0.001; //Convert time to milliseconds
 
         //Perform whichever autonomous mode we chose from the switches
         if (leftAutoSwitch.get() == true && rightAutoSwitch.get() == false)
         {
             //Put us into left autonomous
             if (elapsedTime < AUTO_CORNER_FORWARD_TIME) {
                 //Move forward
                 drivetrain.arcadeDrive(1, 0);
             } else {
                 //Spin!
                 drivetrain.arcadeDrive(0, 1);
             }
         } else if (leftAutoSwitch.get() == false && rightAutoSwitch.get() == true) {
             //Put us into right autonomous
             if (elapsedTime < AUTO_CORNER_FORWARD_TIME) {
                 //Move forward
                 drivetrain.arcadeDrive(1, 0);
             } else {
                 //Spin!
                 drivetrain.arcadeDrive(0, -1);
             }
         } else {
             //Middle position (both switches on or off)
             if (elapsedTime < AUTO_MIDDLE_FORWARD_TIME) {
                 //Move forward
                 drivetrain.arcadeDrive(1, 0);
             } else {
                 //Spin!
                 drivetrain.arcadeDrive(0, 1);
             }
         }
     }
 
     /*
      * This function is called before the beginning of teleop
      */
     public void teleopInit() {
         autoTimer.stop();
     }
 
     /**
      * This function is called periodically during operator control
      */
     public void teleopPeriodic() {
         getWatchdog().feed();
 
         drivetrain.arcadeDrive(joystickOne);
 
         intake.doAction();
     }
     
 }
