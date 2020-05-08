 package org.usfirst.frc2022.commands;
 
 import edu.wpi.first.wpilibj.command.Command;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 import org.usfirst.frc2022.OI;
 import org.usfirst.frc2022.RobotMap;
 import org.usfirst.frc2022.subsystems.CameraServos;
import org.usfirst.frc2022.subsystems.Handling;
 import org.usfirst.frc2022.subsystems.PWM_Generic;
 import org.usfirst.frc2022.subsystems.Pickup;
 import org.usfirst.frc2022.subsystems.Robocam;
 import org.usfirst.frc2022.subsystems.ShooterInjector;
 import org.usfirst.frc2022.subsystems.ShooterPitch;
 import org.usfirst.frc2022.subsystems.ShooterRotation;
 
 
 
 /**
  * The base for all commands. All atomic commands should subclass CommandBase.
  * CommandBase stores creates and stores each control system. To access a
  * subsystem elsewhere in your code in your code use
  * CommandBase.exampleSubsystem
  *
  * @author Titan Robotics (2022)
  */
 public abstract class CommandBase extends Command {
 
     // Static instance of Operator Interface
     public static OI oi;
     
     
     // Create a single static instance of all of your subsystems here
     public static Robocam cam = new Robocam("10.20.22.11");
     public static CameraServos camServos = new CameraServos();
     public static PWM_Generic pwmDriveBase = new PWM_Generic(RobotMap.portsJaguar);
 
     public static Pickup pickup = new Pickup();
   
 
     public static ShooterInjector shooterInjector = new ShooterInjector();
     public static ShooterPitch shooterPitch = new ShooterPitch();
     public static ShooterRotation shooterRotation = new ShooterRotation();
    public static Handling handlingSpike = new Handling();
 
 
     public static void init() {
         // This MUST be here. If the OI creates Commands (which it very likely
         // will), constructing it during the construction of CommandBase (from
         // which commands extend), subsystems are not guaranteed to be
         // yet. Thus, their requires() statements may grab null pointers. Bad
         // news. Don't move it.
         oi = new OI();
         
         // Show what command your subsystem is running on the SmartDashboard
         SmartDashboard.putData(cam);
         SmartDashboard.putData(camServos);
         SmartDashboard.putData(pwmDriveBase);
     }
 
     public CommandBase(String name) {
         super(name);
     }
 
     public CommandBase() {
         super();
     }
 }
