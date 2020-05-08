 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.image.ParticleAnalysisReport;
 import edu.wpi.first.wpilibj.DriverStationLCD;
 
 /**
  *
  * @author Rajath
  */
 public class ParticleFilters {
     ParticleAnalysisReport particles[];
     private double FOVradians = Math.toRadians(35);
     private double maxHeight = 480;
     private double cameraTilt = Math.toRadians(15);
     private double levelPixel = FOVradians / 2 + toPixels(cameraTilt);
     
     double[] current;
     double[] newangle;
     
     private Messager msg;
     
     final double VerticalFOV_p = 480; // field of view, pixels
     final double HorizontalFOV_p = 640;
 
     final double targetHeight = 18.0;
 
     final double heightToBottomOfTopTarget = 100;
     final double heightToTopOfTopTarget =
             heightToBottomOfTopTarget + targetHeight;
 
     final double heightToBottomOfBottomTarget = 30;
     final double heightToTopOfBottomTarget =
             heightToBottomOfBottomTarget + targetHeight;
 
     final double heightToBottomOfMiddleTarget = 56;
     final double heightToTopOfMiddleTarget =
             heightToBottomOfMiddleTarget + targetHeight;
 
     final double cameraHeight = 54;
     
     public ParticleFilters() {
         msg = new Messager();
         newangle = new double[1000];
         current = new double[1000];
     }
     public void setParticle(ParticleAnalysisReport[] particles) {
         this.particles = particles;
     }
     public double[] getDistances(ParticleAnalysisReport[] particles) {
         int iter = 0;
         if (particles.length == 0) {
             msg.printLn("no image");
             return new double[]{8, 6, 7, 5, 3, 0, 9};
         } else {
             for (int i = 0; i < particles.length; i++) {
                 ParticleAnalysisReport par = particles[i];
                 int iterator = 0;
                 while (iterator <= 3) {
                     double temp1 = getDistance1(par.center_mass_y, par.boundingRectHeight, iterator);
                     double temp2 = getDistance2(par.center_mass_y, par.boundingRectHeight, iterator);
                     msg.printOnLn(temp1 + "", DriverStationLCD.Line.kUser2);
                     msg.printOnLn(temp2 + "", DriverStationLCD.Line.kUser3);
                     newangle[iter] = temp1;
                     newangle[iter + 1] = temp2;
                     iter += 2;
                     iterator++;
                 }
             }
         }
         return newangle;
     }
     public void setArray() {
         current = newangle;
     }
 
     public double sumuparray(double[] stuffy) {
         double total = 0;
         for (int i = 0; i < stuffy.length; i++) {
             total = total + stuffy[i];
         }
         return total;
     }
 
     public void compare() {
        if (sumuparray(current) > sumuparray(newangle) + 14) {
             msg.printLn("I am in the origninal part");
         } else {
             msg.printOnLn("I am in the wrong f****** place", DriverStationLCD.Line.kUser4);
         }
     }
 
     private double toPixels(double radians) {
         return radians / FOVradians * maxHeight;
     }
     private double toRadians(double pixels) {
         return pixels / maxHeight * FOVradians;
     }
 
     private double toUpperPixel(double center, double height) {
         return center - (height / 2);
     }
 
     private double toLowerPixel(double center, double height) {
         return center + (height / 2);
     }
 
     private double convertToLevel(double pixel) {
         return levelPixel - pixel;
     }
 
     public double adjacent(double opposite, double radians) {
         return opposite / Math.tan(radians);
     }
     
     public double getDistance1(double centerOfMassY, double rectangleHeight,
             int iterator) {
         double radians = toRadians(convertToLevel(toUpperPixel(centerOfMassY,
                 rectangleHeight)));
         switch (iterator) {
             case 1:
                 return adjacent(heightToTopOfTopTarget - cameraHeight, radians);
             case 2:
                 return adjacent(heightToTopOfMiddleTarget - cameraHeight, radians);
             case 3:
                 return adjacent(heightToTopOfBottomTarget - cameraHeight, radians);
         }
         return iterator * -1;
     }
 
     public double getDistance2(double centerOfMassY, double rectangleHeight,
             int iterator) {
         double radians = toRadians(convertToLevel(toLowerPixel(centerOfMassY,
                 rectangleHeight)));
         switch (iterator) {
             case 1:
                 return adjacent(heightToBottomOfTopTarget - cameraHeight, radians);
             case 2:
                 return adjacent(heightToBottomOfMiddleTarget - cameraHeight, radians);
             case 3:
                 return adjacent(heightToBottomOfBottomTarget - cameraHeight, radians);
         }
         return iterator * -1;
     }
 }
