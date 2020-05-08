 package FRC2115.commands;
 
 import edu.wpi.first.wpilibj.command.CommandGroup;
 import edu.wpi.first.wpilibj.command.WaitCommand;
 
 public class Autonomous extends CommandGroup
 {
     private final double TIME_INITIAL_WAIT = 4.0,
             TIME_TO_REV = 3.0,
             TIME_WHILE_SHOOTING = 6.0,
             TIME_PUSH_BRIDGE_DOWN = 2.0,
            TIME_DRIVE_BACK = 3;
     private final boolean SHOULD_TIP_BRIDGE = true;
     
     public Autonomous()
     {
         //Wait for some pressure buildup
         //addSequential(new WaitCommand(TIME_INITIAL_WAIT));
         
         //Align and rev up shooter
         //addSequential(new AutoAlign());
         addSequential(new AutoShooterSpeed(), TIME_TO_REV);
         
         //Run shooter while deploying plunger
         addParallel(new AutoShooterSpeed(), TIME_WHILE_SHOOTING);
         
         for(int i = 0; i < 2; i++)
         {
             addSequential(new DeployPlunger(0.5));
             //Wait for plunger to come back down
             addSequential(new WaitCommand(2.0)); 
         }
         
         if(SHOULD_TIP_BRIDGE)
         {   
            addSequential(new DriveBackwards(TIME_DRIVE_BACK), TIME_DRIVE_BACK);
             addSequential(new ExtendArm(), TIME_PUSH_BRIDGE_DOWN);
         }
         
         //That's all folks!
     }
 }
