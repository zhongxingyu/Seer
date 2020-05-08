 package org.team751.cheesy;
 
 /**
  * A utility class that stores constants for Cheesy Drive algorithms
 * @author Sam Crow
  */
 public class CheesyDriveConstants {
     
     public static final double kWheelNonLinearity = 0.8;
     
     public static final double kNegInertiaLowMore = 2.5;
     
     public static final double kNegInertiaLowLessExt = 5;
     
     public static final double kNegInertiaLowLess = 3;
     
     public static final double kSenseLow = 1.1;
     
     public static final double kSenseCutoff = 0.1;
     
     public static final double kQuickStopTimeConstant = 0.1;
     
     public static final double kQuickStopStickScalar = 5;
     
     private CheesyDriveConstants() {}
 }
