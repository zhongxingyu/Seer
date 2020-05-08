 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package edu.wpi.first.wpilibj.templates;
 
 
 import edu.wpi.first.wpilibj.DriverStationLCD;
 import edu.wpi.first.wpilibj.IterativeRobot;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.Encoder;
 import edu.wpi.first.wpilibj.Watchdog;
 import edu.wpi.first.wpilibj.camera.AxisCamera;
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the IterativeRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class TPARobot extends IterativeRobot {
     AxisCamera theAxisCamera;                       // The camera
     DriverStationLCD theDriverStationLCD;           // Object representing the driver station   
     // Drive mode selection
     int theDriveMode;                               // The actual drive mode that is currently selected.
     static final int UNINITIALIZED_DRIVE = 0;       // Value when no drive mode is selected
     static final int ARCADE_DRIVE = 1;              // Value when arcade mode is selected 
     static final int TANK_DRIVE = 2;                // Value when tank drive is selected
     public double theMaxSpeed;                      // Multiplier for speed, determined by Z-Axis on left stick
     static final boolean DEBUG = true;              // Debug Trigger
     static final double STOP_VALUE = 0.1;           // Value sent to each motor when the robot is stopping
     Encoder theFrontLeftEncoder;                    // The front left E4P
     Encoder theRearLeftEncoder;                     // The rear left E4P
     Encoder theFrontRightEncoder;                   // The front right E4P
     Encoder theRearRightEncoder;                    // The rear right E4P
     double theFrontLeftOutput;                      // The output sent to the front left motor
     double theRearLeftOutput;                       // The output sent to the rear left motor
     double theFrontRightOutput;                     // The output sent to the front right motor
     double theRearRightOutput;                      // The output sent to the rear right motor
     Joystick theRightStick;                         // Right joystick
     Joystick theLeftStick;                          // Left joystick
     TPARobotDriver theRobotDrive;                   // Robot Drive System
     double theDriveDirection;                       // Direction the robot will move
     double theDriveMagnitude;                       // Speed the robot will move at
     double theDriveRotation;                        // Value the robot will rotate
 
    
 
 
    
     
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Marissa Beene
      * Date:    1/13/2012
      * Purpose: Robot Initialization Function. This function is run once when the
      *          robot is first started up and should be used for any initialization
      *          code.
      * Inputs:  None
      * Outputs: None
      */
     public void robotInit() {
                
         // Define joysticks being used at USB port #1 and USB port #2 on the Drivers Station
 	theRightStick = new Joystick(1);
 	theLeftStick = new Joystick(2);
         if (DEBUG == true){
            System.out.println("The Joysticks constructed successfully"); 
         }
         
         // Define a robot drive object with the front left motor at port 1, rear left at port 2, front right at port 3, and rear right at port 4
         theRobotDrive = new TPARobotDriver(1,2,3,4);
         if (DEBUG == true){
             System.out.println("theRobotDrive constructed successfully");
         }
         
 /*
         // Defines four E4P Motion Sensors at ports 1,2,3,4,5,6,7, and 8
         theFrontLeftEncoder = new Encoder(1,2);
         theRearLeftEncoder = new Encoder(3,4);
         theFrontRightEncoder = new Encoder(5,6);
         theRearRightEncoder = new Encoder(7,8);
         if (DEBUG == true){
             System.out.println("The Encoders constructed successfully");
         }
  */
 
         //Initialize the DriverStationLCD
         theDriverStationLCD = DriverStationLCD.getInstance();
         if (DEBUG) {
             System.out.println("DriverStationLCD initialized");
         }
         
         //Initialize the AxisCamera
         theAxisCamera = AxisCamera.getInstance(); 
         theAxisCamera.writeResolution(AxisCamera.ResolutionT.k320x240);        
         theAxisCamera.writeBrightness(50);
         if (DEBUG) {
             System.out.println("AxisCamera initialized");
         }
      
         // Initialize the Drive Mode to Uninitialized
         theDriveMode = UNINITIALIZED_DRIVE;
         
         // Default the robot to not move
         theDriveDirection = 0;
         theDriveMagnitude = 0;
         theDriveRotation = 0;
         if (DEBUG == true){
             System.out.println("The robot set to not move");
         }
         
         if (DEBUG == true){
         System.out.println("RobotInit() completed.\n");
         }
     }
     /*--------------------------------------------------------------------------*/
     
     
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Marissa Beene
      * Date:    1/13/2012
      * Purpose: TPARobot Constructor
      * Inputs:  None
      * Outputs: None
      */
     public void TPARobot(){
     }
     /*--------------------------------------------------------------------------*/
     
 
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Team
      * Date:    1/13/2012
      * Purpose: This function is called periodically during autonomous mode. Allows
      *          for autonomous operation of the robot.
      * Inputs:  None
      * Outputs: None
      */
 
     public void autonomousPeriodic() {
         
         Watchdog.getInstance().feed();
         theDriverStationLCD.println(DriverStationLCD.Line.kMain6, 1, "Autonomous Mode Called");
         theDriverStationLCD.updateLCD();    //Displays a message to DriverStationLCD when entering Autonomous mode
     }
     /*--------------------------------------------------------------------------*/
     
 
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Team
      * Date:    1/13/2012
      * Purpose: This function is called periodically during autonomous mode. Allows
      *          for autonomous operation of the robot.
      * Inputs:  None
      * Outputs: None
      */
     public void teleopPeriodic() {
        
         // Feed the user watchdog at every period when in autonomous
         Watchdog.getInstance().feed();
         if (DEBUG == true){
             System.out.println("Teleop Periodic Watchdog Fed");
         }
         
         //Set the multiplier for max speed
         setMaxSpeed();
         if (DEBUG == true) {
             System.out.println("setMaxSpeed called");
         }
         
         //Get the image from the Axis Camera
         DriverStationLCD.getInstance().updateLCD();
         if(DEBUG) {
             System.out.println("getCameraImage called");
         }
 
         // Drive the robot with FPS control
         driveRobot();
         if(DEBUG == true){
             System.out.println("driveRobot called");
         }
 
/*
         // Brake the robot if no joysick input.
         brakeOnNeutral();
         if(DEBUG == true) {
             System.out.println("brakeOnNeutral called");
         }
 */        
     }
     /*--------------------------------------------------------------------------*/
     
     
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Marissa Beene
      * Date:    1/14/2012
      * Purpose: To drive the robot with mecanum wheels with a FPS control system.
      *          The left stick will control translational movement and the right
      *          stick will control rotation.
      * Inputs:  None
      * Outputs: None
      */    
     public void driveRobot() {
         theDriveDirection = theLeftStick.getDirectionDegrees(); // Set the direction to the value of the left stick
         theDriveMagnitude = theLeftStick.getMagnitude();    // Set the magnitude to the value of the left stick
         theDriveRotation = (theRightStick.getX()); // Set the rotation to the value of the right stick
         theRobotDrive.mecanumDrive_Polar(theDriveMagnitude, theDriveDirection, theDriveRotation);
         
         if (DEBUG == true){
         System.out.println("The drive rotation in degrees" + theDriveRotation);
         System.out.println("The drive magnitude is" + theDriveMagnitude);
         System.out.println("The drive direction is" + theDriveDirection);
         }
     }   
 
     /*--------------------------------------------------------------------------*/
     
     
   
     
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Gennaro De Luca
      * Date:    11/26/2011 (Gennaro De Luca)
      * Purpose: To determine the speed multiplier based on the "Z" wheel on 
      *          the left joystick. If the "Z" wheel is up (negative), the multiplier remains at 1.
      *          Otherwise, the multiplier is set to one-half.
      * Inputs:  None
      * Outputs: None
      */    
     public void setMaxSpeed(){
         
         if (theLeftStick.getZ() <= 0) {    // Logitech Attack3 has z-polarity reversed; up is negative
             theMaxSpeed = 1;               //set the multiplier to default value of 1
             if (DEBUG == true){
                 System.out.println("theLeftStick.getZ called");
             }
         }
         else if (theLeftStick.getZ() > 0) {
             theMaxSpeed = 0.5;             //set the multiplier to half default, 0.5
             if (DEBUG == true) {
                 System.out.println("theLeftStick.getZ called");
             }
         }
         theRobotDrive.setMaxSpeed(theMaxSpeed); //tests the multiplier
     }
     /*--------------------------------------------------------------------------*/
 
 
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  
      * Date:    
      * Purpose: 
      * Inputs:  
      * Outputs: 
      */    
     
     /*--------------------------------------------------------------------------*/
 
 
     
     /*--------------------------------------------------------------------------*/
     
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  
      * Date:    
      * Purpose: 
      * Inputs:  
      * Outputs: 
      */    
     
     /*--------------------------------------------------------------------------*/
 
 
 
 }
