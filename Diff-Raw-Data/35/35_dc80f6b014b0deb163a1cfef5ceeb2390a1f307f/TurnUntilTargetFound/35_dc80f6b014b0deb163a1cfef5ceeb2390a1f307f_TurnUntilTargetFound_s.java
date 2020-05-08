 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates.commands.Shooting;
 
 import edu.wpi.first.wpilibj.command.CommandGroup;
 import edu.wpi.first.wpilibj.templates.commands.CommandBase;
 import edu.wpi.first.wpilibj.templates.commands.Driving.RotateToAngle;
 
 /**
  *
  * @author Robotics
  */
 public class TurnUntilTargetFound extends CommandGroup{
     
    private static final double rotationSpeed = .7;
    private static final double maxAngle = 80;
     private static final double minAngle = 20;
     
     public TurnUntilTargetFound(boolean rotateRight)
     {
         if(rotateRight)
         {
             addSequential(new RotateToAngle(maxAngle,rotationSpeed));
             addSequential(new RotateToAngle(-minAngle,rotationSpeed));
         }else{
             addSequential(new RotateToAngle(-maxAngle,rotationSpeed));
             addSequential(new RotateToAngle(minAngle,rotationSpeed));
         }
     }
     
     public boolean isFinished()
     {
         //stop when target is found
         return super.isFinished() || CommandBase.visionProcessor.hasTarget();
     }
    
 }
