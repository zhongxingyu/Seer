 package Panda.VisionMapping;
 
 import Panda.util.*;
 import java.io.*;
 
 import lcmtypes.*;
 import lcm.lcm.*;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.image.BufferedImage;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.List;
 import java.io.IOException;
 import java.awt.geom.*;
 
 import april.jcam.ImageSource;
 import april.jcam.ImageConvert;
 import april.jcam.ImageSourceFormat;
 import april.jcam.ImageSourceFile;
 import april.jmat.Matrix;
 
 public class BarrierMap{
 
     // args
     private LineDetectionDetector   detector;
 
     // PIXEL COORDINATE LOCATIONS
     private int[] boundaryMap;
     private ArrayList<int[][]> rectifiedMap;    // rectified boundary map
     private ArrayList<double[][]> realWorldMap;    // in real world coordinates
 
     // constants
     final static double binaryThresh = 155;
     final static double lwPassThresh = 100;
     private double[] calibrationMatrix;
     //public map_t map_msg;
 
     private PandaPositioning pp;
 
 	public BarrierMap( BufferedImage image, double[] cm, PandaPositioning pp ) {
 
 		this.calibrationMatrix = cm;
 
 		// RECTIFICATION
 		BufferedImage im2 = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
         double cx = image.getWidth() / 2.0;
         double cy = image.getHeight() / 2.0;
 
         double B = -0.000910;
         double A = 1.527995;
 
         for (int y = 0; y < image.getHeight(); y++) {
             for (int x = 0; x < image.getWidth(); x++) {
 
                 double dy = y - cy;
                 double dx = x - cx;
 
                 double theta = Math.atan2(dy, dx);
                 double r = Math.sqrt(dy*dy+dx*dx);
 
                 double rp = A*r + B*r*r;
 
                 int nx = (int) Math.round(cx + rp*Math.cos(theta));
                 int ny = (int) Math.round(cy + rp*Math.sin(theta));
 
                 if (nx >= 0 && nx < image.getWidth() && ny >= 0 && ny < image.getHeight()) {
                         im2.setRGB(x, y, image.getRGB((int) nx, (int) ny));
                 }
                 else {
                     im2.setRGB(x, y, 0xffffffff);
                 }
             }
         }
 
         	// detect and retrieve boundary map
         	detector = new LineDetectionDetector(im2, binaryThresh, lwPassThresh);
         	boundaryMap = detector.getBoundaryMap();
 
 		// PERFORM "SPLIT AND FIT" TO FIND LINE SEGMENTS
 		// RUN IN LINEDETECTIONSEGMENTATION
 
         	// segment points
         	LineDetectionSegmentation lds = new LineDetectionSegmentation(boundaryMap, detector.getProcessedImage());
         	ArrayList<int[][]> segments = lds.getSegments();
 		    BufferedImage im3 = lds.getImage();
         	// TRANSFORM LINE SEGMENTS INTO REAL WORLD COORDINATES
 		// ADD REAL WORLD LINE POINTS TO REAL WORLD MAP
         	// from calibrationMatrix
         	// coordinates should now be in 3D
         	realWorldMap = new ArrayList<double[][]>();
 
         	for (int i = 0; i < segments.size(); i++) {
                 int[][] segment = segments.get(i);
 
                 double[] init_point = {segment[0][0], segment[0][1]};
                 double[] fin_point = {segment[1][0], segment[1][1]};
                 // Ray Projection Implementation
                Matrix init_vec = pp.getGlobalPoint(calibrationMatrix, init_point);
                Matrix fin_vec = pp.getGlobalPoint(calibrationMatrix, fin_point);
 
                     // add real world coordinate
                 double[][] real_segment = {{init_vec.get(0,0), init_vec.get(1,0), init_vec.get(2,0)},
                                 {fin_vec.get(0,0), fin_vec.get(1,0), fin_vec.get(2,0)}};
                 realWorldMap.add(real_segment);
 
 
 
 System.out.println("Pixel Coordinates:");
 System.out.println("initial: " + init_point[0] + ", " + init_point[1]);
 System.out.println("final: " + fin_point[0] + ", " + fin_point[1]);
 System.out.println("Boundary Line:");
 System.out.println("initial: (" + real_segment[0][0] + ", " + real_segment[0][1] + ", " + real_segment[0][2] + ")");
 System.out.println("final: (" + real_segment[1][0] + ", " + real_segment[1][1] + ", " + real_segment[1][2] + ")");
 
             }
 
     }
 
     public ArrayList<double[][]> getBarriers() {
         // Method returns real world map
         return realWorldMap;
     }
 
 }
