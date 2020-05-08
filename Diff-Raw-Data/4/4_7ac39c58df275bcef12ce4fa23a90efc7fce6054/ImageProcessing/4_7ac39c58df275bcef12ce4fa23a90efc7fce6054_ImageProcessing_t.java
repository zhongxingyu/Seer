 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.DriverStationLCD;
 import edu.wpi.first.wpilibj.camera.AxisCamera;
 import edu.wpi.first.wpilibj.image.BinaryImage;
 import edu.wpi.first.wpilibj.image.ColorImage;
 import edu.wpi.first.wpilibj.image.CriteriaCollection;
 import edu.wpi.first.wpilibj.image.NIVision.MeasurementType;
 import edu.wpi.first.wpilibj.image.ParticleAnalysisReport;
 
 /**
  *
  * @author Rajath, Michael
  */
 public class ImageProcessing {
 
     ParticleAnalysisReport particles[];
     CriteriaCollection criteriaCollection = new CriteriaCollection();
     ParticleAnalysisReport bottomTarget, topTarget, middleTargetLeft,
             middleTargetRight;
     Messager msg = new Messager();
     static final double FOV = 35.25;//camera field of view in degrees
    static final double camResWidth = 640;
    static final double camResHeight = 480;
     static final double targetHeight = 18.0;
     static final double cameraAngle = 12;
     static final double cameraHeight = 26;
     static final double maxDisparity = .5;
     static final double lambda = 14.55;
     static final double topTargetHeight = 109;//inches to middle
     static final double middleTargetHeight = 72;//inches to middle
     static final double bottomTargetHeight = 39;//inches to middle
     static final double T_topTargetHeight = 118;//inches to top of tape
     static final double T_middleTargetHeight = 81;//inches to top of tape
     static final double T_bottomTargetHeight = 48;//inches to top of tape
     static final double B_topTargetHeight = 100;//inches to bottom of tape
     static final double B_middleTargetHeight = 63;//inches to bottom of tape
     static final double B_bottomTargetHeight = 30;//inches to bottom of tape
 
     public ImageProcessing() {
         criteriaCollection.addCriteria(
                 MeasurementType.IMAQ_MT_BOUNDING_RECT_WIDTH, 30, 400, false);
         criteriaCollection.addCriteria(
                 MeasurementType.IMAQ_MT_BOUNDING_RECT_HEIGHT, 40, 400, false);
 
     }
 
     public static double getHorizontalAngle(ParticleAnalysisReport particle) {
         double p = (camResWidth / 2) - particle.center_mass_x;
         double angle = p / lambda;
         return angle;
 
     }
 
     public static ParticleAnalysisReport getTopMost(ParticleAnalysisReport[] particles) {
         ParticleAnalysisReport greatest = particles[0];
         for (int i = 0; i < particles.length; i++) {
             ParticleAnalysisReport particle = particles[i];
 
             if (particle.center_mass_y > greatest.center_mass_y) {
                 greatest = particle;
             }
         }
         return greatest;
     }
 
     public static ParticleAnalysisReport getBottomMost(ParticleAnalysisReport[] particles) {
         ParticleAnalysisReport lowest = particles[0];
         for (int i = 0; i < particles.length; i++) {
             ParticleAnalysisReport particle = particles[i];
 
             if (particle.center_mass_y < lowest.center_mass_y) {
                 lowest = particle;
             }
         }
         return lowest;
     }
 
     public static ParticleAnalysisReport getRightMost(ParticleAnalysisReport[] particles) {
         ParticleAnalysisReport rightistTarget = particles[0];
         for (int i = 0; i < particles.length; i++) {
             ParticleAnalysisReport particle = particles[i];
 
             if (particle.center_mass_x > rightistTarget.center_mass_x) {
                 rightistTarget = particle;
             }
         }
         return rightistTarget;
     }
 
     public static ParticleAnalysisReport getLeftMost(ParticleAnalysisReport[] particles) {
         ParticleAnalysisReport leftistTarget = particles[0];
         for (int i = 0; i < particles.length; i++) {
             ParticleAnalysisReport particle = particles[i];
 
             if (particle.center_mass_x < leftistTarget.center_mass_x) {
                 leftistTarget = particle;
             }
         }
         return leftistTarget;
     }
 
     /**
      * Fills the array (particles) with all found particle analysis reports
      * @param camera the camera to get the particle analysis report from
      * @throws Exception 
      */
     public void getTheParticles(AxisCamera camera) throws Exception {
         int erosionCount = 2;
         // true means use connectivity 8, false means connectivity 4
         boolean useConnectivity8 = false;
         ColorImage colorImage;
         BinaryImage binaryImage;
         BinaryImage cleanImage;
         BinaryImage convexHullImage;
         BinaryImage filteredImage;
 
         colorImage = camera.getImage();
         //seperate the light and dark image
         binaryImage = colorImage.thresholdRGB(0, 42, 71, 255, 0, 255);
         cleanImage = binaryImage.removeSmallObjects(
                 useConnectivity8, erosionCount);
         //fill the rectangles that were created
         convexHullImage = cleanImage.convexHull(useConnectivity8);
         filteredImage = convexHullImage.particleFilter(criteriaCollection);
         particles = filteredImage.getOrderedParticleAnalysisReports();
         colorImage.free();
         binaryImage.free();
         cleanImage.free();
         convexHullImage.free();
         filteredImage.free();
     }
 
     /**
      * Get the horizontal distance to the target
      * @param part the particle analysis report to get the report from
      * @param height the height of the target to get the report from
      * @return distance to target, in inches
      */
     public double getDistance(ParticleAnalysisReport part, double height) {
         double ph = part.boundingRectHeight;
         double delta = height - cameraHeight;
         double R = 18 / MathX.tan(ph / lambda);
         double D = 0;
 
         for (int i = 0; i < 4; i++) {
             double theta = MathX.asin(delta / R);
             double new_ph = ph / MathX.cos(theta);
             R = 18 / MathX.tan(new_ph / lambda);
             D = MathX.sqrt(R * R - delta * delta);
         }
 
         return D;
     }
 
     public boolean isTopTarget(ParticleAnalysisReport part) {
         double D1 = getDistance(part, T_topTargetHeight);
         double D2 = getDistance(part, B_topTargetHeight);
         double disparity = MathX.abs(D1 / D2 - 1);
 
         if (disparity < maxDisparity) {
             return true;
         } else {
             return false;
         }
     }
     
     public ParticleAnalysisReport getTopTarget() throws Exception {
         for(int i=0;i<particles.length;i++) {
             if(isTopTarget(particles[i])) {
                 return particles[i];
             }
         }
         throw new Exception("Top target not found!");
     }
 }
