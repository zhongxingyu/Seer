 package edu.smartdashboard.krunchcv;
 
 import com.googlecode.javacv.CanvasFrame;
 import com.googlecode.javacv.cpp.opencv_core;
 import com.googlecode.javacv.cpp.opencv_core.*;
 import com.googlecode.javacv.cpp.opencv_imgproc;
 import com.googlecode.javacv.cpp.opencv_imgproc.*;
 import edu.wpi.first.smartdashboard.camera.WPICameraExtension;
 import edu.wpi.first.smartdashboard.robot.Robot;
 import edu.wpi.first.wpijavacv.DaisyExtensions;
 import edu.wpi.first.wpijavacv.WPIBinaryImage;
 import edu.wpi.first.wpijavacv.WPIColor;
 import edu.wpi.first.wpijavacv.WPIColorImage;
 import edu.wpi.first.wpijavacv.WPIContour;
 import edu.wpi.first.wpijavacv.WPIImage;
 import edu.wpi.first.wpijavacv.WPIPoint;
 import edu.wpi.first.wpijavacv.WPIPolygon;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
 import java.util.TreeMap;
 import javax.imageio.ImageIO;
 
 /**
  *
  * @author sebastian
  */
 public class KrunchCVWidget extends WPICameraExtension
 {
     public static final String NAME = "Krunch Target Tracker"; // Name of widget in View->Add list
     private WPIColor targetColor = new WPIColor(0, 255, 0); // Target color to be tracked from retro-reflectors
 
     // Constants that need to be tuned
     private static final double kNearlyHorizontalSlope = Math.tan(Math.toRadians(20)); // Slope of an acceptable horizontal line in degrees
     private static final double kNearlyVerticalSlope = Math.tan(Math.toRadians(90-20)); // Slope of an acceptable vertical line in degrees
     private static final int kMinWidth = 20; // Contour ratio min width
    private static final int kMaxWidth = 200; // Contour ratio max width
     private static final double kRangeOffset = 0.0;
     private static final int kHoleClosingIterations = 9; // Number of iterations of morphology operation
 
     private static final double kShooterOffsetDeg = -1.55;
     private static final double kHorizontalFOVDeg = 47.0;
 
     private static final double kVerticalFOVDeg = 480.0/640.0*kHorizontalFOVDeg;
     private static final double kCameraHeightIn = 54.0;
     private static final double kCameraPitchDeg = 21.0;
     private static final double kTopTargetHeightIn = 98.0 + 2.0 + 9.0; // 98 to rim, +2 to bottom of target, +9 to center of target
 
     private TreeMap<Double, Double> rangeTable;
 
     private boolean m_debugMode = false;
 
     // Store JavaCV temporaries as members to reduce memory management during processing
     private CvSize size = null;
     private WPIContour[] contours;
     private ArrayList<WPIPolygon> polygons;
     private IplConvKernel morphKernel;
     private IplImage bin;
     private IplImage hsv;
     private IplImage hue;
     private IplImage sat;
     private IplImage val;
     private WPIPoint linePt1;
     private WPIPoint linePt2;
     private int horizontalOffsetPixels;
 
     public KrunchCVWidget()
     {
         this(false);
     }
 
     public KrunchCVWidget(boolean debug)
     {
         m_debugMode = debug;
         morphKernel = IplConvKernel.create(3, 3, 1, 1, opencv_imgproc.CV_SHAPE_RECT, null);
 
         rangeTable = new TreeMap<Double,Double>();
         //rangeTable.put(110.0, 3800.0+kRangeOffset);
         //rangeTable.put(120.0, 3900.0+kRangeOffset);
         //rangeTable.put(130.0, 4000.0+kRangeOffset);
         rangeTable.put(140.0, 3434.0+kRangeOffset);
         rangeTable.put(150.0, 3499.0+kRangeOffset);
         rangeTable.put(160.0, 3544.0+kRangeOffset);
         rangeTable.put(170.0, 3574.0+kRangeOffset);
         rangeTable.put(180.0, 3609.0+kRangeOffset);
         rangeTable.put(190.0, 3664.0+kRangeOffset);
         rangeTable.put(200.0, 3854.0+kRangeOffset);
         rangeTable.put(210.0, 4034.0+kRangeOffset);
         rangeTable.put(220.0, 4284.0+kRangeOffset);
         rangeTable.put(230.0, 4434.0+kRangeOffset);
         rangeTable.put(240.0, 4584.0+kRangeOffset);
         rangeTable.put(250.0, 4794.0+kRangeOffset);
         rangeTable.put(260.0, 5034.0+kRangeOffset);
         rangeTable.put(270.0, 5234.0+kRangeOffset);
 
         DaisyExtensions.init();
     }
 
 
     public double getRPMsForRange(double range)
     {
         double lowKey = -1.0;
         double lowVal = -1.0;
         for( double key : rangeTable.keySet() )
         {
             if( range < key )
             {
                 double highVal = rangeTable.get(key);
                 if( lowKey > 0.0 )
                 {
                     double m = (range-lowKey)/(key-lowKey);
                     return lowVal+m*(highVal-lowVal);
                 }
                 else
                     return highVal;
             }
             lowKey = key;
             lowVal = rangeTable.get(key);
         }
 
         return 5234.0+kRangeOffset;
     }
     
     @Override
     public WPIImage processImage(WPIColorImage rawImage)
     {
         double heading = 0.0;
         
         // Get the current heading of the robot first
         if( !m_debugMode )
         {
             try
             {
                 heading = Robot.getTable().getDouble("Heading");
             }
             catch( NoSuchElementException e)
             {
             }
             catch( IllegalArgumentException e )
             {
             }
         }
 
         // If size hasn't been initialized yet
         if( size == null || size.width() != rawImage.getWidth() || size.height() != rawImage.getHeight() )
         {
             size = opencv_core.cvSize(rawImage.getWidth(),rawImage.getHeight());
             bin = IplImage.create(size, 8, 1); // CvSize, depth, number of channels
             hsv = IplImage.create(size, 8, 3);
             hue = IplImage.create(size, 8, 1);
             sat = IplImage.create(size, 8, 1);
             val = IplImage.create(size, 8, 1);
             horizontalOffsetPixels =  (int)Math.round(kShooterOffsetDeg*(size.width()/kHorizontalFOVDeg));
             
             // Line points for line that goes down the middle of the image when outputed on the dashboard
             linePt1 = new WPIPoint(size.width()/2+horizontalOffsetPixels,size.height()-1);
             linePt2 = new WPIPoint(size.width()/2+horizontalOffsetPixels,0);
         }
         // Get the raw IplImages for OpenCV
         IplImage input = DaisyExtensions.getIplImage(rawImage);
 
         // Convert to HSV color space
         opencv_imgproc.cvCvtColor(input, hsv, opencv_imgproc.CV_BGR2HSV);
         opencv_core.cvSplit(hsv, hue, sat, val, null);
 
         // Threshold each component separately
         // Hue
         // NOTE: Red is at the end of the color space, so you need to OR together
         // a thresh and inverted thresh in order to get points that are red
         opencv_imgproc.cvThreshold(hue, bin, 60-50, 255, opencv_imgproc.CV_THRESH_BINARY);
         opencv_imgproc.cvThreshold(hue, hue, 60+50, 255, opencv_imgproc.CV_THRESH_BINARY_INV);
 
         // Saturation
         opencv_imgproc.cvThreshold(sat, sat, 200-50, 255, opencv_imgproc.CV_THRESH_BINARY);
 
         // Value
         opencv_imgproc.cvThreshold(val, val, 55, 255-100, opencv_imgproc.CV_THRESH_BINARY);
 
         // Combine the results to obtain our binary image which should for the most
         // part only contain pixels that we care about
         opencv_core.cvAnd(hue, bin, bin, null);
         opencv_core.cvAnd(bin, sat, bin, null);
         opencv_core.cvAnd(bin, val, bin, null);
 
         // Uncomment the next two lines to see the raw binary image
         //CanvasFrame result = new CanvasFrame("binary");
         //result.showImage(bin.getBufferedImage());
 
         // Fill in any gaps using binary morphology
         // Changing the 5th parameter changes the method, and changing the 6th parameter changes the number of iterations
         // of the pixel extrapolation process.
         opencv_imgproc.cvMorphologyEx(bin, bin, null, morphKernel, opencv_imgproc.CV_MOP_CLOSE, kHoleClosingIterations);
 
         // Uncomment the next two lines to see the image post-morphology
         //CanvasFrame result2 = new CanvasFrame("morph");
         //result2.showImage(bin.getBufferedImage());
 
         // Find contours
         WPIBinaryImage binWpi = DaisyExtensions.makeWPIBinaryImage(bin);
         contours = DaisyExtensions.findConvexContours(binWpi);
 
         polygons = new ArrayList<WPIPolygon>();
         for (WPIContour c : contours)
         {
             double ratio = ((double) c.getHeight()) / ((double) c.getWidth());
             if (ratio < 1.0 && ratio > 0.5 && c.getWidth() > kMinWidth && c.getWidth() < kMaxWidth)
             {
                 polygons.add(c.approxPolygon(20));
             }
         }
 
         WPIPolygon square = null;
         int highest = Integer.MAX_VALUE;
 
         for (WPIPolygon p : polygons)
         {
             if (p.isConvex() && p.getNumVertices() == 4)
             {
                 // We passed the first test...we fit a rectangle to the polygon
                 // Now do some more tests
 
                 WPIPoint[] points = p.getPoints();
                 // We expect to see a top line that is nearly horizontal, and two side lines that are nearly vertical
                 int numNearlyHorizontal = 0;
                 int numNearlyVertical = 0;
                 for( int i = 0; i < 4; i++ )
                 {
                     double dy = points[i].getY() - points[(i+1) % 4].getY(); // Change in Y from one point to the next
                     double dx = points[i].getX() - points[(i+1) % 4].getX(); // Change in X from one point to the next
                     double slope = Double.MAX_VALUE;
                     // If slope not 0, in other words not perfectly horizontal
                     if( dx != 0 )
                         slope = Math.abs(dy/dx); // Find slope of line
 
                     // Increment number of horizontal or vertical sides depending on if the slope is
                    // closer to being horizontal or if the slope is closer to being vertical.
                     if( slope < kNearlyHorizontalSlope )
                         ++numNearlyHorizontal;
                     else if( slope > kNearlyVerticalSlope )
                         ++numNearlyVertical;
                 }
 
                 // Since we assume the top line is horizontal, the funciton only requires that
                // we have 1 nearly horizontal side and 2 nearly vertical sides to consider it
                 // a target.
                 if(numNearlyHorizontal >= 1 && numNearlyVertical == 2)
                 {
                     // Draw a polygon overlay on the image
                     rawImage.drawPolygon(p, WPIColor.BLUE, 2);
 
                     // Get center of polygon
                     int pCenterX = (p.getX() + (p.getWidth() / 2));
                     int pCenterY = (p.getY() + (p.getHeight() / 2));
 
                     // Draw a large point emphasizing the center of the polygon
                     rawImage.drawPoint(new WPIPoint(pCenterX, pCenterY), targetColor, 5);
                     
                     // Picks the highest target
                     // The origin of the coordinate system is at the top-left of the image,
                     // which is why the comparison is less than. The height values get smaller
                     // when they are higher up in reality.
                     if (pCenterY < highest) // Because coord system is funny
                     {
                         square = p;
                         highest = pCenterY;
                     }
                 }
             }
             else
             {
                 // Not an acceptable target
                 rawImage.drawPolygon(p, WPIColor.YELLOW, 1);
             }
         }
 
         // If a target has been found
         if (square != null)
         {
             double x = square.getX() + (square.getWidth() / 2);
             x = (2 * (x / size.width())) - 1;
             double y = square.getY() + (square.getHeight() / 2);
             y = -((2 * (y / size.height())) - 1);
 
             // Find azimuth (horizontal degrees needed to line up with target). This is given as -180 being completely left,
             // +180 being completely right, and 0 being completely lined up.
             double azimuth = this.boundAngle0to180DegreesWithDirection(x*kHorizontalFOVDeg/2.0 + heading - kShooterOffsetDeg);
             double range = (kTopTargetHeightIn-kCameraHeightIn)/Math.tan((y*kVerticalFOVDeg/2.0 + kCameraPitchDeg)*Math.PI/180.0);
             double rpms = getRPMsForRange(range);
 
             if (!m_debugMode)
             {
                   // Code commented out by Sebastian because some functions
                   // weren't found.
 //                Robot.getTable().beginTransaction();
                 // Outputs values to the dashboard
                 Robot.getTable().putBoolean("found", true);
                 Robot.getTable().putValue("azimuth", azimuth);
                 Robot.getTable().putValue("range", range);
                 Robot.getTable().putValue("rpms", rpms);
 //                Robot.getTable().endTransaction();
             } else
             {
                 System.out.println("Target found");
                 System.out.println("x: " + x);
                 System.out.println("y: " + y);
                 System.out.println("azimuth: " + azimuth);
                 System.out.println("range: " + range);
                 System.out.println("rpms: " + rpms);
             }
             rawImage.drawPolygon(square, targetColor, 7);
         } else
         {
 
             if (!m_debugMode)
             {
                 Robot.getTable().putBoolean("found", false);
             } else
             {
                 System.out.println("Target not found");
             }
         }
 
         // Draw a crosshair (line down the middle)
         rawImage.drawLine(linePt1, linePt2, targetColor, 2);
 
         DaisyExtensions.releaseMemory();
 
         //System.gc();
 
         return rawImage;
     }
 
     private double boundAngle0to180DegreesWithDirection(double angle)
     {
         // Naive algorithm
         while(angle >= 360.0)
         {
             angle -= 360.0;
         }
         while(angle < 0.0)
         {
             angle += 360.0;
         }
         
         return angle - 180;
     }
 
     public static void main(String[] args)
     {
         if (args.length == 0)
         {
             System.out.println("Usage: Arguments are paths to image files to test the program on");
             return;
         }
 
         // Create the widget
         KrunchCVWidget widget = new KrunchCVWidget(true);
 
         long totalTime = 0;
         for (int i = 0; i < args.length; i++)
         {
             // Load the image
             WPIColorImage rawImage = null;
             try
             {
                 rawImage = new WPIColorImage(ImageIO.read(new File(args[i%args.length])));
             } catch (IOException e)
             {
                 System.err.println("Could not find file!");
                 return;
             }
             
             //shows the raw image before processing to eliminate the possibility
             //that both may be the modified image.
             CanvasFrame original = new CanvasFrame("Raw");
             original.showImage(rawImage.getBufferedImage());
 
             WPIImage resultImage = null;
 
             // Process image
             long startTime, endTime;
             startTime = System.nanoTime();
             resultImage = widget.processImage(rawImage);
             endTime = System.nanoTime();
 
             // Display results
             totalTime += (endTime - startTime);
             double milliseconds = (double) (endTime - startTime) / 1000000.0;
             System.out.format("Processing took %.2f milliseconds%n", milliseconds);
             System.out.format("(%.2f frames per second)%n", 1000.0 / milliseconds);
             
             CanvasFrame result = new CanvasFrame("Result");
             result.showImage(resultImage.getBufferedImage());
 
             System.out.println("Waiting for ENTER to continue to next image or exit...");
             Scanner console = new Scanner(System.in);
             console.nextLine();
 
             if (original.isVisible())
             {
                 original.setVisible(false);
                 original.dispose();
             }
             if (result.isVisible())
             {
                 result.setVisible(false);
                 result.dispose();
             }
         }
 
         double milliseconds = (double) (totalTime) / 1000000.0 / (args.length);
         System.out.format("AVERAGE:%.2f milliseconds%n", milliseconds);
         System.out.format("(%.2f frames per second)%n", 1000.0 / milliseconds);
         System.exit(0);
     }
 }
