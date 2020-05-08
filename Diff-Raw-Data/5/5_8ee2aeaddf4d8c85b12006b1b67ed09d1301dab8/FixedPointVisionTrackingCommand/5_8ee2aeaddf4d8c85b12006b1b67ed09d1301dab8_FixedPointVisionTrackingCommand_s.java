 package com.edinarobotics.zed.commands;
 
 public class FixedPointVisionTrackingCommand extends VisionTrackingCommand {
     private double fixedXSetpoint;
     private double fixedYSetpoint;
     
     public FixedPointVisionTrackingCommand(double xSetpoint, double ySetpoint) {
         super();
         this.fixedXSetpoint = xSetpoint;
         this.fixedYSetpoint = ySetpoint;
     }
     
     public FixedPointVisionTrackingCommand(double xSetpoint, double ySetpoint, byte goalType) {
         super(goalType);
         this.fixedXSetpoint = xSetpoint;
         this.fixedYSetpoint = ySetpoint;
     }
     
     protected double getXSetpoint(double distance) {
         return fixedXSetpoint;
     }
     
     protected double getYSetpoint(double distance) {
         return fixedYSetpoint;
     }
 }
