 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates.commands.AutonomousModes;
 
 import edu.wpi.first.wpilibj.command.CommandGroup;
 import edu.wpi.first.wpilibj.templates.commands.CommandBase;
 import edu.wpi.first.wpilibj.templates.commands.Driving.DriveForDistance;
 import edu.wpi.first.wpilibj.templates.commands.Driving.ResetDrivetrainEncoders;
 import edu.wpi.first.wpilibj.templates.commands.Shooting.Aim;
 import edu.wpi.first.wpilibj.templates.commands.Shooting.Shoot;
 import edu.wpi.first.wpilibj.templates.commands.Shooting.SpinDown;
 import edu.wpi.first.wpilibj.templates.commands.Shooting.SpinUp;
 import edu.wpi.first.wpilibj.templates.commands.Wait;
 
 /**
  * Maneuvers the robot to line up for a shot in autonomous,
  * then fires all frisbees.
  * @author Connor Willison
  */
 public class Fire3FrisbeesCenterPyramid extends CommandGroup {
     
     public Fire3FrisbeesCenterPyramid() {
        
        //insert code to maneuver robot before first shot here.
 //        addSequential(new ResetDrivetrainEncoders());
        addSequential(new DriveForDistance(-2.0,.7,0.0));
         
         addSequential(new Fire3Frisbees());
         
     }
     
     public void end()
     {
         super.end();
         
         CommandBase.shooter.setSpeed(0.0);
     }
 }
