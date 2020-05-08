 package com.edinarobotics.zed.commands;
 
 import com.edinarobotics.utils.math.Point2;
 
 public class FixedPointVisionTrackingCommand extends VisionTrackingCommand {
     public static final double PYRAMID_BACK_MIDDLE_GOAL_WIDTH = 0.632219;
     public static final double PYRAMID_BACK_MIDDLE_GOAL_HEIGHT = 0.142857;
     
     private double fixedXSetpoint;
     private double fixedYSetpoint;
     
     public static final Point2 PYRAMID_BACK_MIDDLE_NEW = new Point2(-0.018, -0.25);
     public static final Point2 PYRAMID_BACK_RIGHT = new Point2(-0.063829, -0.226891);
     public static final Point2 PYRAMID_BACK_MIDDLE = new Point2(0.0273556, -0.29411);
     public static final Point2 PYRAMID_BACK_MIDDLE_NEW2 = new Point2(-0.143, -0.241);
    public static final Point2 PYRAMID_BACK_MIDDLE_TUNNEL = new Point2(PYRAMID_BACK_MIDDLE_NEW.getX() + (1.5 * PYRAMID_BACK_MIDDLE_GOAL_WIDTH),
            PYRAMID_BACK_MIDDLE_NEW.getY() - (0.93 * PYRAMID_BACK_MIDDLE_GOAL_HEIGHT));
     //public static final Point2 PYRAMID_BACK_MIDDLE_AUTO = new Point2(PYRAMID_BACK_MIDDLE_NEW.getX() - (0.5 * PYRAMID_BACK_MIDDLE_GOAL_WIDTH),
     //        PYRAMID_BACK_MIDDLE_NEW.getY() - (0.87 * PYRAMID_BACK_MIDDLE_GOAL_HEIGHT));
     
     /*
      * TO RESET VT POINT: -0.5 goal width and -0.5 goal height
      */
     
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
     
     public FixedPointVisionTrackingCommand(Point2 point2) {
         this(point2.getX(), point2.getY());
     }
     
     public FixedPointVisionTrackingCommand(Point2 point2, byte goalType) {
         this(point2.getX(), point2.getY(), goalType);
     }
     
     protected double getXSetpoint(double distance) {
         return fixedXSetpoint;
     }
     
     protected double getYSetpoint(double distance) {
         return fixedYSetpoint;
     }
 }
