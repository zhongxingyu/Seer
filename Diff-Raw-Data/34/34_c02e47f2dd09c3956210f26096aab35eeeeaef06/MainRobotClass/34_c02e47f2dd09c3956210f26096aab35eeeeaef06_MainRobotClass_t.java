 package edu.first3729.frc2012;
 
 /**
  * \file MainRobotClass.java
  * \brief The main class from which execution starts, as mandated by WPILib
  * 
  */
 
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DriverStationLCD;
import edu.wpi.first.wpilibj.IterativeRobot;
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the IterativeRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class MainRobotClass extends IterativeRobot
 {
     private Input input_manager;
     private DriverStationLCD screen;
     private Drive drive;
     private Teleoperated teleop;
     private Manipulator manip;
     private DigitalInput intake_limit;
     private int loop;
 	/**
      * This function is run when the robot is first started up and should be
      * used for any initialization code.
      */
     public void robotInit()
     {
     	this.input_manager = new Input();
     	this.input_manager.setMode(Params.default_drive_mode);
     	this.drive = new Drive();
         this.drive.lock_motors();
         this.manip = new Manipulator();
         this.manip.init();
         this.teleop = new Teleoperated(input_manager, drive, manip);
         this.getWatchdog().setExpiration(Params.default_watchdog_time);
         loop = 0;
     }
     
     public void disabledInit()
     {
         // Nothing
     }
     
     public void disabledPeriodic()
     {
         // Nothing
     }
     
     public void teleopInit()
     {
         loop = 0;
         teleop.init();
     }
     
     /**
      * This function is called periodically during operator control
      */
     public void teleopPeriodic()
     {
         //System.out.println("Loop " + loop);
         //++loop;
         this.getWatchdog().feed();
         teleop.run();
     }
     
     public void teleopContinuous()
     {
         
     }
     
 }
