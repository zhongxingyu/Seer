 /**
  * Copyright 2012. FRC Team 3807.
  */
 
 package edu.wpi.first.wpilibj.templates.commands;
 
 //import edu.wpi.first.wpilibj.camera.AxisCamera;
 import edu.wpi.first.wpilibj.command.Command;
 import edu.wpi.first.wpilibj.templates.subsystems.Chassis;
 import edu.wpi.first.wpilibj.templates.OI;
 import edu.wpi.first.wpilibj.templates.RobotMap;
 import edu.wpi.first.wpilibj.templates.subsystems.BallPicker;
 import edu.wpi.first.wpilibj.templates.subsystems.BridgeManipulator;
 import edu.wpi.first.wpilibj.templates.subsystems.GyroAccelerometer;
 import edu.wpi.first.wpilibj.templates.subsystems.Shooter;
 import edu.wpi.first.wpilibj.templates.subsystems.Targeting;
 
 /**
  * The base for all commands. All atomic commands should subclass CommandBase.
  * CommandBase stores creates and stores each control system. To access a
  * subsystem elsewhere in your code in your code use CommandBase.exampleSubsystem
  * @author Author
  */
 public abstract class CommandBase extends Command {
 
     // The OI class is the Operator Interface class that holds all of the
     // controls and methods for interacting with the robot.
     public static OI oi;
 
     // Create a single static instance of all of your subsystems
     public static Chassis chassis;
     public static GyroAccelerometer gyro;
     public static BallPicker ballPicker;
     public static Shooter shooter;
     public static BridgeManipulator arm;
     public static Targeting targeting;
     //public static AxisCamera camera;
 
     /**
      * The init() method initializes all subsystems and the Operator Interface
      * class. This method must be called before
      */
     public static void init() {
         // Initialize all the subsystems.
         chassis = new Chassis(RobotMap.CHASSIS_FRONT_LEFT_CONTROLLER_PORT, //Breakpoint here?
                         RobotMap.CHASSIS_REAR_LEFT_CONTROLLER_PORT,
                           RobotMap.CHASSIS_FRONT_RIGHT_CONTROLLER_PORT,
                             RobotMap.CHASSIS_REAR_RIGHT_CONTROLLER_PORT);
         ballPicker = new BallPicker(RobotMap.BALL_PICKER_SPIKE_RELAY_PORT_NUMBER);
         shooter = new Shooter(RobotMap.SHOOTER_BOTTOM_RELAY_PORT,
                               RobotMap.SHOOTER_TOP_RELAY_PORT, RobotMap.SHOOTER_LIMIT_SWITCH_PORT);
         arm = new BridgeManipulator(RobotMap.ARM_SOLENOID_FORWARD_PORT,
                                     RobotMap.ARM_SOLENOID_REVERSE_PORT,
                                     RobotMap.ARM_COMPRESSOR_PRESSURE_SWITCH_PORT,
                                     RobotMap.ARM_COMPRESSOR_RELAY_PORT);
           
 
             //camera = AxisCamera.getInstance();
 
 
         targeting = new Targeting();
 //        gyro = new GyroAccelerometer(RobotMap.GYRO_PORT,
 //                                     RobotMap.ACCELEROMETERX_PORT,
 //                                     RobotMap.ACCELEROMETERY_PORT);
 
         // This MUST be here. If the OI creates Commands (which it very likely
         // will), constructing it during the construction of CommandBase (from
         // which commands extend), subsystems are not guaranteed to be
         // yet. Thus, their requires() statements may grab null pointers. Bad
         // news. Don't move it.
         oi = new OI();
 
         // Show what command your subsystem is running on the SmartDashboard
      //   SmartDashboard.putData(chassis);
         //SmartDashboard.putData(gyro);
         //SmartDashboard.putData(ballPicker);
         //SmartDashboard.putData(shooter);
       //  SmartDashboard.putData(arm);
        // SmartDashboard.putData(targeting);
       //  SmartDashboard.putData("SchedulerData", Scheduler.getInstance());
     }
 
     public CommandBase(String name) {
         super(name);
     }
 
     public CommandBase() {
         super();
     }
 }
