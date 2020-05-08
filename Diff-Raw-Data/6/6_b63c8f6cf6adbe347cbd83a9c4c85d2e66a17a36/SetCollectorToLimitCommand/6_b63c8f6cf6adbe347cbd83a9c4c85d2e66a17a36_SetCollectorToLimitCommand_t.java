 package com.edinarobotics.zed.commands;
 
 import com.edinarobotics.zed.Components;
 import com.edinarobotics.zed.subsystems.Collector;
 import edu.wpi.first.wpilibj.command.Command;
 
 public class SetCollectorToLimitCommand extends Command {
     private Collector collector;
     private Collector.CollectorLiftDirection collectorLiftDirection;
     
     public SetCollectorToLimitCommand(Collector.CollectorLiftDirection collectorLiftDirection) {
         super("SetCollectorToLimit");
         this.collector = Components.getInstance().collector;
         this.collectorLiftDirection = collectorLiftDirection;
         setTimeout(2.0);
         requires(collector);
     }
     
     protected void initialize() {
        collector.setCollectorLiftDirection(collectorLiftDirection);
     }
 
     protected void execute() {
         collector.setCollectorLiftDirection(collectorLiftDirection);
     }
 
     protected boolean isFinished() {
         boolean limitSwitchValue = false;
         if(collectorLiftDirection.equals(Collector.CollectorLiftDirection.COLLECTOR_LIFT_UP)) {
             limitSwitchValue = collector.getUpperLimitSwitch();
         }
         if(collectorLiftDirection.equals(Collector.CollectorLiftDirection.COLLECTOR_LIFT_DOWN)) {
             limitSwitchValue = collector.getLowerLimitSwitch();
         }
        return limitSwitchValue || isTimedOut() || collectorLiftDirection.equals(Collector.CollectorLiftDirection.COLLECTOR_LIFT_STOP);
     }
 
     protected void end() {
         collector.setCollectorLiftDirection(Collector.CollectorLiftDirection.COLLECTOR_LIFT_STOP);
     }
 
     protected void interrupted() {
         end();
     }
     
 }
