 package com.edinarobotics.zed.commands;
 
 public class FixedPointVisionTrackingCommand extends VisionTrackingCommand {
     private double fixedXSetpoint;
     private double fixedYSetpoint;
     
    public static final double PYRAMID_BACK_MIDDLE_X = 0.0273556;
    public static final double PYRAMID_BACK_MIDDLE_Y = -0.29411;
    public static final double PYRAMID_BACK_RIGHT_X = -0.063829;
    public static final double PYRAMID_BACK_RIGHT_Y = -0.226891;
    
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
