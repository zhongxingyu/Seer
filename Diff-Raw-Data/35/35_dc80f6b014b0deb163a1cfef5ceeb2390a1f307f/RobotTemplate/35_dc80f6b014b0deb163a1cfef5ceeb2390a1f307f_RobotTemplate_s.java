 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.Compressor;
 import edu.wpi.first.wpilibj.IterativeRobot;
 import edu.wpi.first.wpilibj.Preferences;
 import edu.wpi.first.wpilibj.command.Command;
 import edu.wpi.first.wpilibj.command.Scheduler;
 import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 import edu.wpi.first.wpilibj.templates.commands.AutonomousModes.Fire3Frisbees;
 import edu.wpi.first.wpilibj.templates.commands.AutonomousModes.Fire3FrisbeesCenterPyramid;
 import edu.wpi.first.wpilibj.templates.commands.AutonomousModes.Fire3FrisbeesLeftOfPyramid;
 import edu.wpi.first.wpilibj.templates.commands.AutonomousModes.Fire3FrisbeesRightOfPyramid;
 import edu.wpi.first.wpilibj.templates.commands.AutonomousModes.RotationTest;
 import edu.wpi.first.wpilibj.templates.commands.Climbing.ResetClimberEncoders;
 import edu.wpi.first.wpilibj.templates.commands.CommandBase;
 import edu.wpi.first.wpilibj.templates.commands.Driving.ResetDrivetrainEncoders;
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the IterativeRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class RobotTemplate extends IterativeRobot {
 
     Command autonomousCommand;
     //Autonomous Chooser
     SendableChooser autoChooser;
 
     /**
      * This function is run when the robot is first started up and should be
      * used for any initialization code.
      */
     public void robotInit() {
         // Initialize all subsystems
         CommandBase.init();
 
         //start the compressor thread
         new Compressor(RobotMap.compressorPressureSwitch, RobotMap.compressorSpike).start();
 
         //Radio Button chooser on the Smart Dashboard
         autoChooser = new SendableChooser();
         queueAutonomousCommands();
 
         //add commands to the SmartDashboard
         SmartDashboard.putData(Scheduler.getInstance());
         SmartDashboard.putData(new ResetDrivetrainEncoders());
         SmartDashboard.putData(new ResetClimberEncoders());
         SmartDashboard.putData("Choose Autonomous Mode", autoChooser);
     }
 
     public void disabledInit() {
         super.disabledInit();
 
         Scheduler.getInstance().removeAll();
 
         //cancel running autonomous commands
 //        if(autonomousCommand != null)
 //            autonomousCommand.cancel();
 
         //reset autonomous commands
         queueAutonomousCommands();
     }
 
     public void disabledPeriodic() {
         outputStatistics();
     }
 
     public void autonomousInit() {
         //Set the autonomous command to the selected option on the smart dashboard
         autonomousCommand = ((Command)autoChooser.getSelected());
 //        autonomousCommand = new Fire3FrisbeesCenterPyramid();
         autonomousCommand.start();
     }
 
     /**
      * This function is called periodically during autonomous
      */
     public void autonomousPeriodic() {
         outputStatistics();
         Scheduler.getInstance().run();
     }
 
     public void teleopInit() {
         // This makes sure that the autonomous stops running when
         // teleop starts running. If you want the autonomous to 
         // continue until interrupted by another command, remove
         // this line or comment it out.
 
         if (autonomousCommand != null) {
             autonomousCommand.cancel();
         }
     }
 
     /**
      * This function is called periodically during operator control
      */
     public void teleopPeriodic() {
         outputStatistics();
         Scheduler.getInstance().run();
     }
 
     private void queueAutonomousCommands() {
         autoChooser.addDefault("Center of Pyramid", new Fire3FrisbeesCenterPyramid());
        autoChooser.addObject("Left of Pyramid", new Fire3FrisbeesLeftOfPyramid());
         autoChooser.addObject("Right Of Pyramid", new Fire3FrisbeesRightOfPyramid());
         autoChooser.addObject("Just Fire 3 Frisbees", new Fire3Frisbees());
         autoChooser.addObject("Rotation Test", new RotationTest());
 //        autoChooser.addObject("Locker Pod Test", new DriveAroundLockerPods(6));
 //        autoChooser.addObject("Drive Meter Stick", new DriveForDistance(50.0,1.0,0.0));
 //        autoChooser.addObject("Chase Target", new ChaseTarget());
     }
 
     private void outputStatistics() {
         /*
          * Check the Preferences file to filter out unecessary debug messages on the SmartDashboard.
          */
         if (Preferences.getInstance().getBoolean("GamepadDebug", false)) {
             CommandBase.oi.sendGamepads();
         }
 
         if (Preferences.getInstance().getBoolean("DrivetrainDebug", false)) {
             CommandBase.drivetrain.sendToDash();
         }
 
         if (Preferences.getInstance().getBoolean("ClimberDebug", false)) {
             CommandBase.leftArm.sendToDash();
             CommandBase.rightArm.sendToDash();
         }
 
         if (Preferences.getInstance().getBoolean("ShooterDebug", false)) {
             CommandBase.shooter.sendToDash();
         }
 
         if (Preferences.getInstance().getBoolean("VisionDebug", false)) {
             CommandBase.visionProcessor.processData();
             CommandBase.visionProcessor.sendToDash();
         }
     }
 }
