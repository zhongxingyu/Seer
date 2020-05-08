 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.camera.AxisCamera;
 import edu.wpi.first.wpilibj.image.BinaryImage;
 import edu.wpi.first.wpilibj.image.ColorImage;
 import edu.wpi.first.wpilibj.image.ParticleAnalysisReport;
 import edu.wpi.first.wpilibj.image.CriteriaCollection;
 import edu.wpi.first.wpilibj.image.NIVision.MeasurementType;
 
 /**
  *
  * @author Rajath
  * find targets
  */
 public class ImageProcessing {
 
     ParticleAnalysisReport particles[];
     Physics imageCalculations;
     CriteriaCollection criteriaCollection = new CriteriaCollection();
     ParticleAnalysisReport bottomTarget, topTarget, middleTargetLeft, 
             middleTargetRight;
     Messager msg = new Messager();
     
     final double numberOfDegreesInVerticalFieldOfView = 33;
     final double numberOfPixelsVerticalInFieldOfView = 240;
     final double numberOfPixelsHorizontalInFieldOfView = 640;
     
     double targetHeight = 18;
 
     final double heightToTopOfTopTarget = 100;
     final double heightToBottomOfTopTarget = heightToTopOfTopTarget 
             + targetHeight;
     
     final double heightToBottomOfBottomTarget = 30;
     final double heightToTopOfBottomTarget = heightToBottomOfBottomTarget 
             + targetHeight;
     
     final double heightToBottomOfMiddleTarget = 56;
     final double heightToTopOfMiddleTarget = heightToBottomOfMiddleTarget + 
             targetHeight;
         
     final double cameraAngleOffset = 12;
 
     final double cameraHeight = 45;
 
     public ImageProcessing() {
         criteriaCollection.addCriteria(
                 MeasurementType.IMAQ_MT_BOUNDING_RECT_WIDTH, 30, 400, false);
         criteriaCollection.addCriteria(
                 MeasurementType.IMAQ_MT_BOUNDING_RECT_HEIGHT, 40, 400, false);
         imageCalculations = new Physics(true);
     }
     public double pixlesToAngles(double pixles)
     {
         return pixles*numberOfDegreesInVerticalFieldOfView
                 /numberOfPixelsVerticalInFieldOfView;
     }
     public double getPixelsFromLevelToBottomOfATarget(
             ParticleAnalysisReport particle) {
         double PixelsFromLevelToBottomOfTopTarget =
                 numberOfPixelsVerticalInFieldOfView - particle.center_mass_y
                 - (particle.boundingRectHeight / 2);
         System.out.println("PixelsFromLevelToBottomOfTopTarget"+
                 PixelsFromLevelToBottomOfTopTarget);
         return PixelsFromLevelToBottomOfTopTarget;
     }
     public double getPixelsFromLevelToTopOfATarget(
             ParticleAnalysisReport particle) {
         /*TODO take into account the fact that level is not
          * always at the bottom of the field of view
          */
         /*TODO don't use global variable PixelsFromLevelToBottomOfTopTarget,
          * instead use local variables, return values, and parameters
          */
         double PixelsFromLevelToTopOfTopTarget =
                 numberOfPixelsVerticalInFieldOfView - particle.center_mass_y
                 + (particle.boundingRectHeight / 2);
         System.out.println("PixelsFromLevelToTopOfTopTarget" + 
                 PixelsFromLevelToTopOfTopTarget);
         return PixelsFromLevelToTopOfTopTarget;
     }
     public double getPhi(double PixelsFromLevelToTopOfTopTarget) {
         /* TODO: consider simply returning the calculated value,
          * instead of assigning it to a variable and then returning that
          * variable
          */
         return pixlesToAngles(PixelsFromLevelToTopOfTopTarget)
                 + cameraAngleOffset;   
     }
     public double getTheta(double PixelsFromLevelToBottomOfATarget) {
         return  pixlesToAngles(PixelsFromLevelToBottomOfATarget)  + cameraAngleOffset;        
     }
     public static ParticleAnalysisReport getTopMost(ParticleAnalysisReport[] particles)
     {
         ParticleAnalysisReport greatest = particles[0];
         for (int i=0;i < particles.length; i++)
         {
             ParticleAnalysisReport particle = particles[i];
             
             if(particle.center_mass_y > greatest.center_mass_y){
                 greatest = particle;
             }       
         }
         return greatest;
     }
     public static ParticleAnalysisReport getBottomMost
             (ParticleAnalysisReport[] particles)
     {
        ParticleAnalysisReport lowest = particles[0];
         for (int i=0;i < particles.length; i++)
         {
             ParticleAnalysisReport particle = particles[i];
             
             if(particle.center_mass_y < lowest.center_mass_y){
                 lowest = particle;
             }       
         }
         return lowest;
     }
     public static ParticleAnalysisReport getRightMost
             (ParticleAnalysisReport[] particles)
     {
        ParticleAnalysisReport rightistTarget = particles[0];
         for (int i=0;i < particles.length; i++)
         {
             ParticleAnalysisReport particle = particles[i];
             
             if(particle.center_mass_x > rightistTarget.center_mass_x){
                 rightistTarget = particle;
             }       
         }
         return rightistTarget;
     }
     public static ParticleAnalysisReport getLeftMost
             (ParticleAnalysisReport[] particles)
     {
        ParticleAnalysisReport leftistTarget = particles[0];
         for (int i=0;i < particles.length; i++)
         {
             ParticleAnalysisReport particle = particles[i];
             
             if(particle.center_mass_x < leftistTarget.center_mass_x){
                 leftistTarget = particle;
             }       
         }
         return leftistTarget;
     }
     public void setTargets(ParticleAnalysisReport[] particles)
     {
         topTarget = getTopMost(particles);
         bottomTarget = getBottomMost(particles);
         
     }
     public double getHypotneuse0(double angle, int ref) { //ref-reference number
                                                           //1 Top
                                                           //2 Middle
         double opposite0 = 0;                             //3 Bottom
         switch (ref){
             case 1: opposite0 = heightToBottomOfTopTarget - cameraHeight;
                 break;
             case 2: opposite0 = heightToBottomOfMiddleTarget - cameraHeight;
                 break;
             case 3: opposite0 = heightToBottomOfBottomTarget - cameraHeight;
                 break;
         }
         double hypotneuse_0 = opposite0
                 / MathX.sin(getTheta(angle));
         System.out.println("Phi " + getTheta(angle));
         return hypotneuse_0;
     }
     public double getHypotneuse1(double angle, int targetSelector) { //ref-reference number
                                                           //1 Top
                                                           //2 Middle
         double opposite1 = 0;                             //3 Bottom
         switch (targetSelector){
             case 1: opposite1 = heightToTopOfTopTarget - cameraHeight;
                 break;
             case 2: opposite1 = heightToTopOfMiddleTarget - cameraHeight;
                 break;
             case 3: opposite1 = heightToTopOfBottomTarget - cameraHeight;
                 break;
         }
         double hypotneuse_1 =
                 opposite1
                 / MathX.sin(getPhi(angle));
        System.out.println("Phi " + getPhi(angle));
         return hypotneuse_1;
     }
     public double getAdjacent1(double phiAngle,double hypotneuse){
         return MathX.cos(phiAngle) * hypotneuse;
     }
     
     
     public double getAdjacent0(double thetaAngle,double hypotneuse){
         return MathX.cos(thetaAngle) * hypotneuse;   
     }
     public double idTarget(ParticleAnalysisReport particle, int iterator) {                  
             double phi = getPhi(getPixelsFromLevelToTopOfATarget(particle));
             double theta = getTheta
                     (getPixelsFromLevelToBottomOfATarget(particle));
                 
             double adjacent1 = getAdjacent1(phi,getHypotneuse1(phi,iterator));
             double adjacent0 = getAdjacent0(theta,getHypotneuse0(theta,iterator));
             double disparity = Math.abs(adjacent1 - adjacent0);
             
             msg.printLn("Bottom Adjacent0 : " + adjacent0);
             msg.printLn("Bottom Adjacent1 : " + adjacent1);
             msg.printLn("The disperity is " + disparity);
             System.out.println("---------------------------------------------");
             return disparity;
     }
     public void getTheParticles(AxisCamera camera) throws Exception {
         int erosionCount = 2;
         // true means use connectivity 8, true means connectivity 4
         boolean connectivity8Or4 = false;
         ColorImage colorImage;
         BinaryImage binaryImage;
         BinaryImage cleanImage;
         BinaryImage convexHullImage;
         BinaryImage filteredImage;
 
         colorImage = camera.getImage();
         //seperate the light and dark image
         binaryImage = colorImage.thresholdRGB(0, 42, 71, 255, 0, 255);
         cleanImage = binaryImage.removeSmallObjects(
                 connectivity8Or4, erosionCount);
         //fill the rectangles that were created
         convexHullImage = cleanImage.convexHull(connectivity8Or4);
         filteredImage = convexHullImage.particleFilter(criteriaCollection);
         particles = filteredImage.getOrderedParticleAnalysisReports();
         colorImage.free();
         binaryImage.free();
         cleanImage.free();
         convexHullImage.free();
         filteredImage.free();
     }
     public void orginizeTheParticles (ParticleAnalysisReport[] particles)
     {
         ParticleAnalysisReport midTargetTemp1 = particles[0], 
                 midTargetTemp2 = particles[1];
         for (int i=0;i < particles.length; i++)
         {
             ParticleAnalysisReport particle = particles[i];
             for(int j=1;j < 4; j++)
             {
                 double currentDisperity = idTarget(particle,j);
                 if (j == 1 && currentDisperity < 100)
                 {
                     topTarget = particle;
                     msg.printLn("Top target found");
                 }
                 else if (j == 2 && currentDisperity < 100)
                 {
                     if (middleTargetLeft == null)
                     {
                         middleTargetLeft = particle;
                         midTargetTemp1 = particle;
                         msg.printLn("left target found");
                         
                     }
                     else
                     {
                         middleTargetRight = particle;
                         midTargetTemp2 = particle;
                         msg.printLn("right target found");
                     }
                 }
                 else if (j == 3 && currentDisperity < 100)
                 {
                     bottomTarget = particle;
                     msg.printLn("bottom target found");
                 }
             }
             
         }
         if(middleTargetLeft != null && middleTargetRight != null) {
             middleTargetRight = getRightMost(new ParticleAnalysisReport[] 
                          {midTargetTemp1,midTargetTemp2});
         middleTargetLeft = getLeftMost(new ParticleAnalysisReport[] 

                          {middleTargetLeft,middleTargetRight});
             
         }
         
         
     }
 
     public double CameraCorrection(ParticleAnalysisReport particle,String target){
         
        
         
         if("top".equals(target)){
            targetHeight = 109;
         }
         
         if("middle".equals(target)){
            targetHeight = 72;
         }
         if("bottom".equals(target)){
             targetHeight = 39;
         }
         double delta =  targetHeight  - cameraHeight;
         double lambda = numberOfPixelsVerticalInFieldOfView/
                 numberOfDegreesInVerticalFieldOfView;
         double pixelHeightBetweenReflectiveTape = 
                 getPixelsFromLevelToTopOfATarget(particle) - 
                 getPixelsFromLevelToBottomOfATarget(particle);
         double ph_fixed = pixelHeightBetweenReflectiveTape;
         
         double R = 18/MathX.tan(ph_fixed/lambda);
         
         double Distance = 0;
         for(int i =1; i<= 4; i++)
         {
             double theta = MathX.asin(delta/R);
             double ph_new = ph_fixed/MathX.cos(theta);
             R = 18/MathX.tan(ph_new/lambda);
             Distance = MathX.sqrt(R*R-delta*delta);
         }
         
         return Distance;
         
        }
 
 }
