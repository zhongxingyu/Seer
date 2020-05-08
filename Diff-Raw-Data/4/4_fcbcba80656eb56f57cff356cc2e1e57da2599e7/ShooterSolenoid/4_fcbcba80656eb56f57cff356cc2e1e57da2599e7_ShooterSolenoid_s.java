 package edu.wpi.first.wpilibj.templates.subsystems;
 
 import edu.wpi.first.wpilibj.Solenoid;
 import edu.wpi.first.wpilibj.command.Subsystem;
 import edu.wpi.first.wpilibj.templates.commands.RunShooterSolenoid;
 import edu.wpi.first.wpilibj.templates.debugging.DebugInfo;
 import edu.wpi.first.wpilibj.templates.debugging.DebugInfoGroup;
 import edu.wpi.first.wpilibj.templates.debugging.Debuggable;
 import edu.wpi.first.wpilibj.templates.variablestores.VstM;
 
 /**
  *
  * @author Robotics
  */
 public class ShooterSolenoid extends Subsystem implements Debuggable {
     // Put methods for controlling this subsystem
     // here. Call these from Commands.
 
     private Solenoid shooterSolenoidSide1 = new Solenoid(VstM.Solenoids.SHOOTER_SOLENOID_SIDE_1);
     private Solenoid shooterSolenoidSide2 = new Solenoid(VstM.Solenoids.SHOOTER_SOLENOID_SIDE_2);
 
     public void initDefaultCommand() {
         setDefaultCommand(new RunShooterSolenoid());
     }
 
     public void extend() {
         shooterSolenoidSide1.set(true);
         shooterSolenoidSide2.set(false);
     }
 
     public void retract() {
         shooterSolenoidSide1.set(false);
         shooterSolenoidSide2.set(true);
     }
    
    public boolean isExtended() {
        return shooterSolenoidSide1.get();
    }
 
     public DebugInfoGroup getStatus() {
         DebugInfo info = new DebugInfo("ShooterSolenoid:Extending", shooterSolenoidSide1.get() ? "yes" : "no");
         return new DebugInfoGroup(info);
     }
 }
