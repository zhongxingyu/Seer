 package FRC2115.commands;
 
 import edu.wpi.first.wpilibj.command.CommandGroup;
 import edu.wpi.first.wpilibj.command.WaitCommand;
 
 public class Autonomous extends CommandGroup
 {
     private final double TIME_INITIAL_WAIT = 4.0;
     private final double TIME_TO_REV = 3.0;
     private final double TIME_WHILE_SHOOTING = 15.0;
     
     public Autonomous()
     {
         //Wait for some pressure buildup
         //addSequential(new WaitCommand(TIME_INITIAL_WAIT));
         
         //Align and rev up shooter
         //addSequential(new AutoAlign());
        //addSequential(new AutoShooterSpeed(), TIME_TO_REV);
         
         //Run shooter while deploying plunger
         addParallel(new AutoShooterSpeed(), TIME_WHILE_SHOOTING);
         
        for(int i = 0; i <  3; i++)
         {
             addSequential(new DeployPlunger(1.0));
             //Wait for plunger to come back down
             addSequential(new WaitCommand(1.0)); 
         }
         
         //That's all folks!
     }
 }
