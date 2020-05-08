 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package com.edinarobotics.zed;
 
 
 import com.edinarobotics.utils.commands.MaintainStateCommand;
 import com.edinarobotics.utils.commands.RepeatCommand;
 import com.edinarobotics.utils.gamepad.Gamepad;
 import com.edinarobotics.utils.pid.PIDTuningManager;
 import com.edinarobotics.zed.commands.AugerRotateCommand;
 import com.edinarobotics.zed.commands.ConveyorPulseSequenceCommand;
import com.edinarobotics.zed.commands.FixedPointVisionTrackingCommand;
 import com.edinarobotics.zed.commands.GamepadDriveRotationCommand;
 import com.edinarobotics.zed.commands.GamepadDriveStrafeCommand;
 import com.edinarobotics.zed.commands.SetCollectorToLimitCommand;
 import com.edinarobotics.zed.commands.SetConveyorCommand;
 import com.edinarobotics.zed.commands.SetShooterCommand;
 import com.edinarobotics.zed.commands.VisionTrackingCommand;
 import com.edinarobotics.zed.subsystems.Auger;
 import com.edinarobotics.zed.subsystems.Collector;
 import com.edinarobotics.zed.subsystems.Conveyor;
 import com.edinarobotics.zed.subsystems.DrivetrainRotation;
 import com.edinarobotics.zed.subsystems.DrivetrainStrafe;
 import com.edinarobotics.zed.subsystems.Lifter;
 import com.edinarobotics.zed.subsystems.Shooter;
 import edu.wpi.first.wpilibj.DriverStation;
 import edu.wpi.first.wpilibj.IterativeRobot;
 import edu.wpi.first.wpilibj.command.Command;
 import edu.wpi.first.wpilibj.command.CommandGroup;
 import edu.wpi.first.wpilibj.command.PrintCommand;
 import edu.wpi.first.wpilibj.command.Scheduler;
 import edu.wpi.first.wpilibj.command.WaitCommand;
 import edu.wpi.first.wpilibj.command.WaitForChildren;
 import edu.wpi.first.wpilibj.livewindow.LiveWindow;
 
 public class Zed extends IterativeRobot {
     private Command autonomousCommand;
     
     /**
      * This function is called when the robot switches between modes (eg. Autonomous,
      * Teleop) to reset running subsystems.
      */
     public void betweenModes() {
         DrivetrainStrafe drivetrainStrafe = Components.getInstance().drivetrainStrafe;
         drivetrainStrafe.setDefaultCommand(new MaintainStateCommand(drivetrainStrafe));
         DrivetrainRotation drivetrainRotation = Components.getInstance().drivetrainRotation;
         drivetrainRotation.setDefaultCommand(new MaintainStateCommand(drivetrainRotation));
         stop();
         if(autonomousCommand != null) {
             autonomousCommand.cancel();
         }
     }
     
     /**
      * This function is run when the robot is first started up and should be
      * used for any initialization code.
      */
     public void robotInit() {
         Components.getInstance(); //Create all robot subsystems.
         Controls.getInstance(); //Create all robot controls.
         System.out.println("Robot initialized.");
     }
     
     public void disabledInit() {
         betweenModes();
     }
     
     public void disabledPeriodic() {
         stop();
     }
     
     /**
      * This function is called once at the start of autonomous mode.
      */
     public void autonomousInit(){
         DriverStation driverStation = DriverStation.getInstance();
         double delayTime = driverStation.getAnalogIn(1);
         byte goalType = VisionTrackingCommand.ANY_GOAL;
         
         boolean trackHighGoal = driverStation.getDigitalIn(1);
         boolean trackMiddleGoal = driverStation.getDigitalIn(2);
         boolean shootInAuto = true;
         
         if(trackHighGoal && trackMiddleGoal){
             goalType = VisionTrackingCommand.ANY_GOAL;
         }
         else if(trackHighGoal){
             goalType = VisionTrackingCommand.HIGH_GOAL;
         }
         else if(trackMiddleGoal){
             goalType = VisionTrackingCommand.MIDDLE_GOAL;
         }
         else{
             shootInAuto = false;
         }
         
         betweenModes();
         DrivetrainStrafe drivetrainStrafe = Components.getInstance().drivetrainStrafe;
         drivetrainStrafe.setDefaultCommand(new MaintainStateCommand(drivetrainStrafe));
         DrivetrainRotation drivetrainRotation = Components.getInstance().drivetrainRotation;
         drivetrainRotation.setDefaultCommand(new MaintainStateCommand(drivetrainRotation));
         
         CommandGroup fastAugerSequence = new CommandGroup();
         fastAugerSequence.addSequential(new PrintCommand("Dispensing auger"));
         fastAugerSequence.addSequential(new AugerRotateCommand(Auger.AugerDirection.AUGER_DOWN));
         fastAugerSequence.addSequential(new WaitCommand(0.8));
         
         CommandGroup augerSequence = new CommandGroup();
         augerSequence.addSequential(new PrintCommand("Dispensing auger"));
         augerSequence.addSequential(new AugerRotateCommand(Auger.AugerDirection.AUGER_DOWN));
         augerSequence.addSequential(new WaitCommand(2));
         
         CommandGroup autoCommand = new CommandGroup();
         autoCommand.addSequential(new PrintCommand("Starting autonomous"));
         autoCommand.addSequential(new WaitCommand(delayTime));
         if(shootInAuto){
            autoCommand.addSequential(new FixedPointVisionTrackingCommand(FixedPointVisionTrackingCommand.PYRAMID_BACK_MIDDLE_X,
                    FixedPointVisionTrackingCommand.PYRAMID_BACK_MIDDLE_Y, VisionTrackingCommand.HIGH_GOAL), 2);
             autoCommand.addSequential(new SetShooterCommand(Shooter.SHOOTER_ON));
             autoCommand.addSequential(new WaitCommand(2));
             autoCommand.addSequential(new SetConveyorCommand(Conveyor.CONVEYOR_SHOOT_IN));
             autoCommand.addSequential(new WaitCommand(3));
             autoCommand.addSequential(fastAugerSequence);
             autoCommand.addSequential(new RepeatCommand(augerSequence, 4));
             autoCommand.addSequential(new WaitForChildren());
             autoCommand.addSequential(new WaitCommand(2));
             autoCommand.addSequential(new SetConveyorCommand(Conveyor.CONVEYOR_STOP));
             autoCommand.addSequential(new SetShooterCommand(Shooter.SHOOTER_OFF));
         }
         
         autonomousCommand = autoCommand;
         autonomousCommand.start();
     }
 
     /**
      * This function is called periodically during autonomous.
      */
     public void autonomousPeriodic() {
         Scheduler.getInstance().run();
     }
     
     /**
      * This function is called once at the start of teleop mode.
      */
     public void teleopInit(){
         betweenModes();
         Gamepad driveGamepad = Controls.getInstance().gamepad1;
         Gamepad shootGamepad = Controls.getInstance().gamepad2;
         Components.getInstance().drivetrainStrafe.setDefaultCommand(new GamepadDriveStrafeCommand(driveGamepad));
         Components.getInstance().drivetrainRotation.setDefaultCommand(new GamepadDriveRotationCommand(driveGamepad, shootGamepad));
     }
 
     /**
      * This function is called periodically during operator control.
      */
     public void teleopPeriodic() {
         Scheduler.getInstance().run();
     }
     
     /**
      * This function is called once at the start of test mode.
      */
     public void testInit() {
         betweenModes();
         LiveWindow.setEnabled(false);
         teleopInit();
     }
     
     /**
      * This function is called periodically during test mode.
      */
     public void testPeriodic() {
         teleopPeriodic();
         PIDTuningManager.getInstance().runTuning();
     }
     
     /**
      * This function is called to stop <i>each</i> subsystem.
      */
     public void stop() {
         Components.getInstance().auger.setAugerDirection(Auger.AugerDirection.AUGER_STOP);
         Components.getInstance().collector.setCollectorDirection(Collector.CollectorDirection.COLLECTOR_STOP);
         Components.getInstance().collector.setCollectorLiftDirection(Collector.CollectorLiftDirection.COLLECTOR_LIFT_STOP);
         Components.getInstance().conveyor.setConveyorSpeed(0);
         Components.getInstance().drivetrainRotation.mecanumPolarRotate(0);
         Components.getInstance().drivetrainStrafe.mecanumPolarStrafe(0, 0);
         Components.getInstance().lifter.setLifterDirection(Lifter.LifterDirection.LIFTER_STOP);
         Components.getInstance().shooter.setShooterVelocity(0);
         Components.getInstance().climber.setClimberDeployed(false);
     }
     
 }
