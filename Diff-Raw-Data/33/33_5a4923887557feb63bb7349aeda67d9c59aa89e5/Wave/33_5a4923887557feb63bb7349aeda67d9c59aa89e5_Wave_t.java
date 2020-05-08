 package com.thesky;
 
 import android.graphics.Bitmap;
 
 import java.util.Random;
 
 public class Wave {
 
     //wave parameters
     private int length = 20;
     private int speed = 20;
     private int angle = 45;
     private double variance = 380;
     private int multiplication_factor = 10000;
 
     private int x = 0;
     private int y = 0;
 
     private int width;
     private int height;
 
     private int[] elevations;
 
     private boolean first_start = true;
 
     private static Random random = new Random(System.currentTimeMillis());
 
     private boolean verify_pos() {
         return (x <= -width || y <= -height || x >= 2 * width || y >= 2 * height);
     }
 
     public void randomize_params() {
         if (verify_pos()) {
             if (!first_start) {
                 randomize_angle();
                 selectCorner();
                 findElevations();
             } else {
                 first_start = false;
             }
         }
     }
 
     private void selectCorner() {
         if ((angle >= 0) && (angle < 90)) {
             x = 0;
             y = 0;
         } else if ((angle >= 90) && (angle < 180)) {
             x = width;
             y = 0;
         } else if ((angle >= 180) && (angle < 270)) {
             x = width;
             y = height;
         } else if ((angle >= 270) && (angle < 360)) {
             x = 0;
             y = height;
         }
     }
 
     private void randomize_angle() {
         angle = random.nextInt(360);
     }
 
     /**
      * Bell-shaped Normal Distribution function for
      * realistic representation of wave distortion
      *
      * @param x
      * @return
      */
     private double getElevation(int x) {
         return ((1 / Math.sqrt(2 * Math.PI * variance)) * (Math.exp(-Math.pow(x, 2) / (2 * variance))));
     }
 
     private void findElevations() {
         elevations = new int[length];
         for (int i = 0; i < length; i++) {
             elevations[i] = (int) (getElevation(i) * multiplication_factor);
         }
     }
 
     private int getCrestX(int y0) {
         return (int) (x + (y - y0) * 1.0 / Math.tan(Math.toRadians(angle)));
     }
 
     private int getCrestY(int x0) {
         return (int) (y - (x0 - x) * Math.tan(Math.toRadians(angle)));
     }
 
     public void propagate() {
         x += speed * Math.cos(Math.toRadians(90 - angle));
         y += speed * Math.sin(Math.toRadians(90 - angle));
     }
 
    public Wave(int height, int width, int angle) {
         this.height = height;
         this.width = width;
        this.angle = angle;
         findElevations();
     }
 
     public synchronized void stretch(Bitmap image_original, Bitmap image_to_draw) {
 
         double displacement;
 
         /**
          *  coefficients for correct wave propagation representation
          *  under different angles
          */
         double dx = Math.cos(Math.toRadians(90 - angle));
         double dy = Math.sin(Math.toRadians(90 - angle));
 
         final double[] x_displacements = new double[length * 2 + 1];
         final double[] y_displacements = new double[length * 2 + 1];
 
         /**
          * for efficiency, two arrays of displacements
          * should be filled only once, with processed values
          * from the array of Normal Distribution function iterations
          */
         for (int i = 1; i < length - 1; i++) {
             displacement = dx * (elevations[i] - elevations[i + 1]);
             x_displacements[length - i] = displacement;
             x_displacements[length + i] = displacement;
             displacement = dy * (elevations[i] - elevations[i + 1]);
             y_displacements[length - i] = displacement;
             y_displacements[length + i] = displacement;
         }
 
         class WaveDeformationProcessor {
 
             public void vertical_process(Bitmap image_original, Bitmap image_to_draw) {
                 //vertical processing
                 //better for vertical wave propagation
                 for (int x = 1; x < width; x++) {
                     int crest_y = getCrestY(x);
                     for (int y0 = 0; y0 < length - 1; y0++) {
                         int y_down = crest_y + y0;
                         int y_up = crest_y - y0;
                         int y_down_dis = (int) y_displacements[length - y0];
                         int y_up_dis = (int) y_displacements[length + y0];
                         int x_left_dis = (int) (x_displacements[length - y0]);
                         int x_right_dis = (int) (x_displacements[length + y0]);
                         try {
                             image_to_draw.setPixel(x, y_down, image_original.getPixel(x + x_left_dis, y_down + y_down_dis));
                             image_to_draw.setPixel(x, y_up, image_original.getPixel(x + x_right_dis, y_up + y_up_dis));
                         } catch (Exception e) {
                             break;
                         }
                     }
                 }
             }
 
             public void horizontal_process(Bitmap image_original, Bitmap image_to_draw) {
 
                 //horizontal processing
                 //better for horizontal wave propagation
                 for (int y = 1; y < height; y++) {
                     int crest_x = getCrestX(y);
                     for (int x0 = 0; x0 < length - 1; x0++) {
                         int x_left = crest_x + x0;
                         int x_right = crest_x - x0;
                         int x_left_dis = (int) x_displacements[length - x0];
                         int x_right_dis = (int) x_displacements[length + x0];
                         int y_up_dis = (int) (y_displacements[length - x0]);
                         int y_down_dis = (int) (y_displacements[length + x0]);
                         try {
                             image_to_draw.setPixel(x_left, y, image_original.getPixel(x_left + x_left_dis, y + y_up_dis));
                             image_to_draw.setPixel(x_right, y, image_original.getPixel(x_right + x_right_dis, y + y_down_dis));
                         } catch (Exception e) {
                             break;
                         }
                     }
                 }
             }
 
 
         }
 
         //selection of proper algorithm depending on wave`s crest inclination angle
         int disp_x = (int) (Math.abs(Math.cos(Math.toRadians(90 - angle))) * 10000);
         int disp_y = (int) (Math.abs(Math.sin(Math.toRadians(90 - angle))) * 10000);
 
         if (disp_x < disp_y) {
             new WaveDeformationProcessor().vertical_process(image_original, image_to_draw);
         } else {
             new WaveDeformationProcessor().horizontal_process(image_original, image_to_draw);
         }
 
     }
 
 }
