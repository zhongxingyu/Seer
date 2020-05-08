 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST Team 2035, 2012. All Rights Reserved.                  */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package edu.wpi.first.wpilibj.templates.commands;
 
 import edu.wpi.first.wpilibj.templates.ScraperBike;
 import edu.wpi.first.wpilibj.templates.subsystems.Shooter;
 
 /**runs motor that turns the metal stick that reloads the shooter either way, depending on which button you press.
  *
  * @author Team 2035 Programmers
  */
 public class Reload extends CommandBase {
     private Shooter shooter;
     private int direction;
     
     public Reload(int direction) {
         shooter = ScraperBike.getShooterController();
        //requires(shooter);
         this.direction = direction;
         // Use requires() here to declare subsystem dependencies
         // eg. requires(chassis);
     }
 
     // Called just before this Command runs the first time
     protected void initialize() {
     }
 
     // Called repeatedly when this Command is scheduled to run
     protected void execute() {
        shooter.setReloadMotor(direction * 0.75);
     }
 
     // Make this return true when this Command no longer needs to run execute()
     protected boolean isFinished() {
         return false;
     }
 
     // Called once after isFinished returns true
     protected void end() {
         shooter.setReloadMotor(0.0);
     }
 
     // Called when another command which requires one or more of the same
     // subsystems is scheduled to run
     protected void interrupted() {
         shooter.setReloadMotor(0.0);
     }
 }
