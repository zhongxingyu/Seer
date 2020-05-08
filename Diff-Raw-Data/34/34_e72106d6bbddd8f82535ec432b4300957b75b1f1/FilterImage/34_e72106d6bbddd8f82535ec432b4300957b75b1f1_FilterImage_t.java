 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.slidellrobotics.reboundrumble.commands;
 
 import edu.wpi.first.wpilibj.Relay;
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.image.BinaryImage;
 import edu.wpi.first.wpilibj.image.ColorImage;
 import edu.wpi.first.wpilibj.image.NIVisionException;
 import edu.wpi.first.wpilibj.image.ParticleAnalysisReport;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 
 /**
  *
  * @author Allister Wright
  */
 public class FilterImage extends CommandBase {
 
     double totalWidth = 0;
     double totalHeight = 0;
     double launchSpeed;
     double distanceToTarget;
     double lastTime;
     ParticleAnalysisReport[] reports = null;
     ParticleAnalysisReport targetGoal = null;
 
     public FilterImage() {
         requires(camera);
         requires(lazySusan);
         requires(leftShootingMotors);
         requires(rightShootingMotors);
         System.out.println("Filter image Init");    //States that the camera initialized
         lastTime = Timer.getFPGATimestamp();
     }
 
     protected void initialize() {
     }
 
     protected void execute() {
         try {
             //run this at a slower pace to not eat up all the processor time
             if (Timer.getFPGATimestamp() - lastTime > 0.5) {
 
 
                 getImage();                 //loops getting a fresh image
                 selectGoal();
                 findDistance();
                 findAngle();
                 updateStatus();
                 lastTime = Timer.getFPGATimestamp();
             }
         } catch (Exception ex) {
         }
 
     }
 
     protected boolean isFinished() {
         return false;                //never ends
     }
 
     protected void end() {
     }
 
     protected void interrupted() {
     }
 
     public void getImage() {
 
         ColorImage pic = null;
         BinaryImage thresholdHSL = null;
         BinaryImage convexHullImage = null;
         
         try {
             pic = camera.getImageFromCamera();      //Declares pic variable
             totalWidth = pic.getWidth();
             totalHeight = pic.getHeight();
             thresholdHSL = pic.thresholdHSL(165, 185, 50, 90, 95, 110);      //Sets a Blue light threshold
             convexHullImage = thresholdHSL.convexHull(false);        //Fills in the bounding boxes for the targets            
             reports = convexHullImage.getOrderedParticleAnalysisReports();        //Sets "reports" to the nuber of particles
         } catch (NIVisionException ex) {
         } catch (Exception ex) {
         }
 
 
         //need to free memory on all pic variables
         try {
 
             if (pic != null) {
                 pic.free();
             }
 
             if (convexHullImage != null) {
                 convexHullImage.free();
             }
 
             if (thresholdHSL != null) {
                 thresholdHSL.free();
             }
         } catch (Exception ex) {
             System.out.println(ex);
         }
 
     }
 
     // Called repeatedly when this Command is scheduled to run
     protected void findAngle() {
 
         if (targetGoal == null){
             lazySusan.setRelay(Relay.Value.kOff);   //turn off
             SmartDashboard.putString("LazySusan", "Off");
             System.out.println("No target set");
             return;
         }
         
         double targetLocale;
         double horCenter;
         horCenter = (totalWidth / 2);     //Finds the pixel value of the horizontal center
         targetLocale = targetGoal.center_mass_x;        //Finds the center of our target
         double targetDiff = Math.abs(targetLocale - horCenter); // see how far away we are
 
         //todo: tune the 10 pixels to the right number
         //there is always going to be a little error, but we want some small window
         //where the lazy suzan stops moving to we can make an accurate shot.
 
         if (targetDiff < 50) {
             lazySusan.setRelay(Relay.Value.kOff);   //turn off
             SmartDashboard.putString("LazySusan", "Off");
         } else if (targetLocale > horCenter) {                  //and if we are facing right
             lazySusan.setRelay(Relay.Value.kReverse);   //turn left
             SmartDashboard.putString("LazySusan", "Reverse");
         } else {                                        //if we face left
             lazySusan.setRelay(Relay.Value.kForward);   //turn right
             SmartDashboard.putString("LazySusan", "Forward");
         }
 
     }
 
     public void selectGoal() {
         ParticleAnalysisReport leftGoal;    
         ParticleAnalysisReport rightGoal;  
         //reset target goal
         targetGoal = null;
         
         //todo set to 4 for comp
         if (reports == null) {
             return;
         }
         
         
         if (reports.length < 3) {
             System.out.println("Not enough goals");
             targetGoal = reports[0];
         } else {
             //we we have four goals in view index 1 is the left and index 2 is right
             leftGoal = reports[1];     //Recognizes the
             rightGoal = reports[2];    //middle goals.
             double leftWidth = leftGoal.boundingRectWidth;     //Finds the widths of
             double rightWidth = rightGoal.boundingRectWidth;   //both middle goals.
             if (leftWidth <= rightWidth) {        //
                 targetGoal = rightGoal;             //Decides which goal we are
             }
             if (rightWidth > leftWidth) {        //closer to and targets it.
                 targetGoal = leftGoal;              //
             }
         }
     }
 
     public void findDistance() {
         if (targetGoal == null){
             leftShootingMotors.setSetpoint(100);
             rightShootingMotors.setSetpoint(100);
             System.out.println("No target set");
             return;
         }
         
         double targetHeight = targetGoal.boundingRectHeight;   //Sets the height of our target.
         double targetHeightFeet = 1.5;
         double vertFOV = targetHeightFeet / targetHeight * totalHeight; // //Gets the foot equivalent of our vertical Field of View
 
         double camearVerticalFOV = 48;
         double cameraHorizontalFOV = 84;
 
         double targetWidth = targetGoal.boundingRectWidth;   //Sets the height of our target.
         double targetWidthFeet = 2.0;
         double horFOV = targetWidthFeet / targetWidth * totalWidth;
 
 
         double d1 = (vertFOV / 2) / Math.tan(camearVerticalFOV / 2);
         double d2 = (horFOV / 2) / Math.tan(cameraHorizontalFOV / 2);
 
         distanceToTarget = (d1 + d2) / 2;  //take the average to try get a more accurate measurement
        //if distance to target is invalid, justset it to some number
        if (distanceToTarget > 60 || distanceToTarget <= 0)
            distanceToTarget = 60;
        
         double d = distanceToTarget;
 
         launchSpeed = 60 * (d / Math.sqrt(((11 / 6) - d) / -16) / ((2 / 3) * 3.1415926));  //Calcs the required rpms for firing
         leftShootingMotors.setSetpoint(launchSpeed);
         rightShootingMotors.setSetpoint(launchSpeed);
     }
 
     public void updateStatus() {
         try {
             SmartDashboard.putDouble("Pic Height", totalHeight);
             SmartDashboard.putDouble("Pic Width", totalWidth);
             SmartDashboard.putDouble("launchSpeed", launchSpeed);
             SmartDashboard.putDouble("Distance", distanceToTarget);
 
             if (reports != null) {
                 for (int i = 0; i < reports.length; i++) {                          //Systematically prints the                                   
                     System.out.println("Particle: " + i + ": Center of mass x:" + //geometric center
                             reports[i].center_mass_x);                                       //of mass per particle.
                 }
             }
 
 
         } catch (Exception ex) {
         }
 
     }
 
     private class CalibrationPoint {
 
         double distance;
         double rpms;
 
         public CalibrationPoint(double distance, double rpms) {
             this.distance = distance;
             this.rpms = rpms;
         }
     }
 
     //just incase the formula doesn't work out here is a test based
     //interpolation function
     private double distanceToRMP(double distance) {
 
 
         //load the test data when practing with the real launcher
        // points MUST be inorder of closest to furthest away
         CalibrationPoint calibrationPoints[] = {new CalibrationPoint(5, 100),
             new CalibrationPoint(10, 200),
             new CalibrationPoint(15, 500),
             new CalibrationPoint(40, 1500)};
 
        //find the two calibration points were the input distance is between them
         int upperIndex;
        for (upperIndex = 1; upperIndex < calibrationPoints.length; upperIndex++) {
            if (distance <= calibrationPoints[upperIndex].distance &&
                distance > calibrationPoints[upperIndex-1].distance) {
                 break;
             }
         }
        
        //interpolate the point
        double slope =  (calibrationPoints[upperIndex].rpms - calibrationPoints[upperIndex - 1].rpms)
                      / (calibrationPoints[upperIndex].distance - calibrationPoints[upperIndex - 1].distance);
        double intercept = calibrationPoints[upperIndex].rpms - slope * calibrationPoints[upperIndex].distance;
        return slope * distance + intercept;
 
        
     }
 } // end of class
