 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.usfirst.frc2022.commands;
 
 import org.usfirst.frc2022.Joysticks.Attack3;
 
 
 /**
  *
  * @author Michael
  */
 public class SweetSpotCommand extends CommandBase{
 
     public SweetSpotCommand(){
         requires(shooter);
         requires(shooterPitch);
         requires(shooterRotation);
     }
     
     protected void initialize() {
         shooter.enable();
         shooterPitch.enable();
         shooterRotation.enable();
         shooterRotation.setSetpoint(0);
         shooterPitch.setSetpoint(0);
         shooter.setSetpoint(0);
     }
 
     protected void execute() {
        shooter.setSetpoint(Math.E / 5.0);
        shooterPitch.setSetpoint(Math.floor(Math.E / 2.0));
        shooterRotation.setSetpoint(Math.PI/5.0);
     }
 
     protected boolean isFinished() {
         return false;
     }
 
     protected void end() {
         shooter.disable();
         shooterPitch.disable();
         shooterRotation.disable();
         
     }
 
     protected void interrupted() {
         shooter.disable();
         shooterPitch.disable();
         shooterRotation.disable();
         
     }
     
     
     
 }
