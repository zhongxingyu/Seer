 package edu.wpi.first.wpilibj.templates.subsystems;
 
 import edu.wpi.first.wpilibj.Solenoid;
 import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.templates.commands.RunFrisbeeDumperSolenoid;
 import edu.wpi.first.wpilibj.templates.variablestores.VstM;
 
 /**
  *
  * @author daboross
  */
 public class FrisbeeDumperSolenoid extends Subsystem {
 
     private Solenoid solenoid;
 
     public FrisbeeDumperSolenoid() {
         System.out.println("FrisbeeDumperSolenoid: Created");
         solenoid = new Solenoid(VstM.SOLENOID.FRISBEE_DUMP);
         solenoid.set(false);
     }
 
     protected void initDefaultCommand() {
        setDefaultCommand(new RunFrisbeeDumperSolenoid());
     }
 
     public void startExpand() {
         solenoid.set(true);
     }
 }
