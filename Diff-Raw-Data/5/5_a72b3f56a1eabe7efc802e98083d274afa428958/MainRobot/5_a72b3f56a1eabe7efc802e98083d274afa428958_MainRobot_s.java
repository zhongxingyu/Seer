 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package edu.wpi.first.wpilibj.templates;
 
 
 import edu.wpi.first.wpilibj.templates.Logic.Teleoperated;
 import edu.wpi.first.wpilibj.templates.Logic.Disabled;
 import edu.wpi.first.wpilibj.templates.Logic.Autonomous;
 import edu.wpi.first.wpilibj.templates.Logic.ICPProtocol;
 import edu.wpi.first.wpilibj.templates.Components.Arm;
 import edu.wpi.first.wpilibj.templates.Components.DriveTrain;
 import edu.wpi.first.wpilibj.templates.Components.LineSensorManager;
 import edu.wpi.first.wpilibj.templates.Components.ForkLift;
 import edu.wpi.first.wpilibj.templates.Components.EncoderMangager;
 import edu.wpi.first.wpilibj.templates.Components.CompressorManager;
 import edu.wpi.first.wpilibj.templates.Components.XboxGamepad;
 import edu.wpi.first.wpilibj.Encoder;
 import edu.wpi.first.wpilibj.Gyro;
 import edu.wpi.first.wpilibj.IterativeRobot;
 import edu.wpi.first.wpilibj.templates.Dashboard.SimpleDashboard;
 import edu.wpi.first.wpilibj.Relay;
 import edu.wpi.first.wpilibj.Solenoid;
 
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the IterativeRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class MainRobot extends IterativeRobot
 {
     DriveTrain drive;
     CompressorManager compressor;
 
     // Sensors
     Gyro gyro;
     LineSensorManager lineSensors;
 
     // Controls
     XboxGamepad con1;
     XboxGamepad con2;
     ForkLift forkLift;
     Arm arm;
     
     // Encoders
     //Encoder frontLeftEncoder = new Encoder(1,1,2,2);
     //Encoder frontRightEncoder = new Encoder(3,3,4,4);
     //Encoder forkLiftEncoder = new Encoder(5,5,5,6);
     //EncoderMangager EncoderManagerMain = new EncoderMangager();
 
     int[] lineSensorChannels;
 
     ICPProtocol autonomous;
     ICPProtocol teleoperated;
     ICPProtocol disabled;
 
     SimpleDashboard dashboard;
 
     Solenoid solenoid;
 
     /**
      * Initialize the robots control parts
      */
     public MainRobot()
     {
         drive = new DriveTrain(1,3,2,4);
         drive.setGyro(gyro);
         //lineSensors = new LineSensorManager(lineSensorChannels);
         //compressor = new BlowholePnumaticCompressorManager();
 
         // Controllers
         con1 = new XboxGamepad(1);
         con1.controllerSetup();
 
         con2 = new XboxGamepad(2);
         con2.controllerSetup();
 
         // Forklift
         forkLift = new ForkLift();
 
         // Arm
         arm = new Arm(7);
 
         dashboard = new SimpleDashboard();
        gyro = new Gyro(1);
         compressor = new CompressorManager(1,1);
 
         solenoid = new Solenoid(8);
 //        EncoderManagerMain.addEncoder(frontLeftEncoder);
 //        EncoderManagerMain.addEncoder(frontRightEncoder);
 //        EncoderManagerMain.addEncoder(rearLeftEnc);
 //        EncoderManagerMain.addEncoder(rearRightEnc);
 
 
 
 
         /*
          * Game Section Modules
          */
         
         // Autonomous Code
         autonomous = new Autonomous();
         autonomous.setRobot(this);
 
         // Teleoperated Code
         teleoperated = new Teleoperated();
         teleoperated.setRobot(this);
 
         // Disabled Code
         disabled = new Disabled();
         disabled.setRobot(this);
 
         /*
          * Controls (Drive, Forklift, Arm, Minibot)
          */
 
         // Drive (steerController, turnController)
         drive.setController(con1.lStick, con1.rStick);
 
         // Forklift (stick) | (buttonUp, buttonDown)
         forkLift.setController(con1.Y, con1.A);
 
         // Arm (stick) | (buttonForward, buttonBackward)
         arm.setController(con2.rStick);
     }
     
     public void robotInit()
     {
     }
 
     /*
      * Teleoperated
      */
     public void teleopInit()
     {
         teleoperated.init();
     }
 
     public void teleopContinuous()
     {
         teleoperated.continuous();
         dashboard.update();
     }
 
     public void teleopPeriodic()
     {
         teleoperated.periodic();
     }
 
 
     /*
      * Autonomous
      */
     public void autonomousInit()
     {
         autonomous.init();
     }
 
     public void autonomousContinuous()
     {
         autonomous.continuous();
         dashboard.update();
 
     }
 
     public void autonomousPeriodic()
     {
         autonomous.periodic();
     }
     
 
     /*
      * Disabled
      */
     public void disabledInit()
     {
         disabled.init();
     }
 
     public void disabledContinuous()
     {
         disabled.continuous();
     }
 
     public void disabledPeriodic()
     {
         disabled.periodic();
     }
 
     /*
      * Setters/Getters
      */
     public DriveTrain getDrive(){
         return drive;
     }
 
     public LineSensorManager getLineSensors(){
         return lineSensors;
     }
     
     public XboxGamepad getController1(){
         return con1;
     }
 
     public XboxGamepad getController2(){
         return con2;
     }
 
     public ForkLift getForkLift(){
         return forkLift;
     }
 
     public Arm getArm(){
         return arm;
     }
 
     public Gyro getGyro(){
         return gyro;
     }
 
     public CompressorManager getCompressor(){
         return compressor;
     }
 
     public Solenoid getSolenoid()
     {
         return solenoid;
     }
 }
