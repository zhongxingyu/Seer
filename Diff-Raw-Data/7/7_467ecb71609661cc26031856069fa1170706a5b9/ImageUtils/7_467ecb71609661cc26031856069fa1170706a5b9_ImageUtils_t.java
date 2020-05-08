 package streamoutlining;
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 
 /**
  *
  * @author forrest
  */
 public class ImageUtils {
 
     /**
      * @param p - the point that we're measuring all the distances from
      * @param listOfMarkers - a list of points to check the distance from point P
      * @return the Point in the list that was closest to p.
      */
     public static ImageMarker getNearestMarkerFromList(Point p, List<ImageMarker> listOfMarkers)
     {
         double shortestDistanceSoFar = Double.MAX_VALUE;
         ImageMarker nearestMarker = null;        
         
         for (ImageMarker marker : listOfMarkers)
         {
             double distToThisPoint = p.distance(marker.getLocation());
             if (distToThisPoint < shortestDistanceSoFar)
             {
                if(distToThisPoint<200.0)
                {
                    shortestDistanceSoFar = distToThisPoint;
                    nearestMarker = marker;
                }
             }
         }
         return nearestMarker;
     }
     
     /**
      * @param originalImage - image to be resized
      * @param shrinkFactor - how many times smaller should the new image be?
      * @return The new rescaled image.
      */
     public static BufferedImage makeShrunkImageCopy(BufferedImage originalImage, 
     		double shrinkFactor)
     {
         int scaledWidth = (int) (originalImage.getWidth()  / shrinkFactor);
         int scaledHeight = (int) (originalImage.getHeight()  / shrinkFactor);
     	BufferedImage scaledImg = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
     	Graphics2D g = scaledImg.createGraphics();
         g.setComposite(AlphaComposite.Src);
     	g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null); 
     	g.dispose();
     	return scaledImg;
     }
     
     /**
      * Note: topImg and bottomImg should have matching dimensions.
      * @param topImg - image to be placed on top of bottomImg
      * @param bottomImg - image placed underneath the topImg
      * @return a new composite image resulting from the overlay.
      */
     public static BufferedImage overlay(BufferedImage topImg, BufferedImage bottomImg) {
         BufferedImage combined = new BufferedImage(bottomImg.getWidth(),
                 bottomImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
 
         // paint each image onto the new canvas
         Graphics2D g = combined.createGraphics();
         g.drawImage(bottomImg, 0, 0, null);
         g.drawImage(topImg, 0, 0, null);
         g.dispose();
         return combined;
     }
 
     /**
      *
      * @param img - Image to be searched
      * @param color - color of interest
      * @return a list of points containing (x,y) for each pixel in the image
      * that exactly matches the color (and alpha transparency) specified. e.g.,
      * if the color argument is entirely opaque (non-transparent, alpha=255)
      * then this method will only return fully opaque pixel matches.
      */
     public static ArrayList<Point> getPixelListForColor(BufferedImage img, Color color) {
         ArrayList<Point> pixelList = new ArrayList<Point>();
 
         for (int y = 0; y < img.getHeight(); y++) {
             for (int x = 0; x < img.getWidth(); x++) {
                 // IMPORTANT NOTE: 
                 // the second argument needs to be true so we read in the ALPHA transparency
                 // of the pixel -- otherwise we can pick up false positives of pixels that
                 // used to be the certain color, but then were transparent
                 Color pixelColor = new Color(img.getRGB(x, y), true);
                 // check if the pixel color (and alpha) matches
                 if (pixelColor.equals(color)) {
                     pixelList.add(new Point(x, y));
                 }
             }
         }
         return pixelList;
     }
 
         public static ArrayList<ImageMarker> extractColoredMarkers(BufferedImage img) {
             ArrayList<ImageMarker> markerList = new ArrayList<ImageMarker>();
 
             for (int y = 0; y < img.getHeight(); y++) {
                 for (int x = 0; x < img.getWidth(); x++) {
                     // IMPORTANT NOTE: 
                     // the second argument needs to be true so we read in the ALPHA transparency
                     // of the pixel -- otherwise we can pick up false positives of pixels that
                     // used to be the certain color, but then were transparent
                     Color pixelColor = new Color(img.getRGB(x, y), true);
                     // check if the pixel color (and alpha) matches
                     if (pixelColor.equals(Color.yellow)) {
                         markerList.add(new ImageMarker(ImageMarker.MarkerType.CORNER_POINT, new Point(x, y)));
                     }
                     else if (pixelColor.equals(Color.red)) {
                         markerList.add(new ImageMarker(ImageMarker.MarkerType.CONTROL_POINT, new Point(x, y)));
                     }
                     else if (pixelColor.equals(Color.magenta)) {
                         markerList.add(new ImageMarker(ImageMarker.MarkerType.OUTLINE_POINT, new Point(x, y)));                        
                     }
 //                    else if (pixelColor.getAlpha() > 0)
 //                    {
 //                        // we found an incorrectly colored non-transparent pixel, sound error!
 //                        throw new RuntimeException("Image contained non-transparent pixel of invalid color: " + pixelColor);
 //                    }
                 }
             }
         return markerList;
     }
 
     
     /**
      * Modifies the given image by expanding each pixel of a certain color by
      * drawing a circle of a larger radius on top of it.
      *
      * @param img - image to be modified
      * @param pointList - list of (x,y) points to enlarge
      * @param color - color to use for filling in circles
      * @param enlargeAmount - radius of enlargement.
      */
     public static void enlargePoints(BufferedImage img, ArrayList<ImageMarker> markerList, int enlargeAmount, double imageScaledFactor) {
         Graphics g = img.getGraphics();
         for (ImageMarker marker : markerList) {
             //draw a circle at each point, with the radius (enlargeAmount + 1)
             if (marker.getType() == ImageMarker.MarkerType.CORNER_POINT)
             {
                 g.setColor(Color.yellow);
             }
             else if (marker.getType() == ImageMarker.MarkerType.CONTROL_POINT)
             {
                 g.setColor(Color.red);
             }
             else if (marker.getType() == ImageMarker.MarkerType.OUTLINE_POINT)
             {
                 g.setColor(Color.magenta);
             }
             if (enlargeAmount == 0)
             {
                 g.fillRect((int) (marker.getLocation().x / imageScaledFactor), 
                        (int) (marker.getLocation().y / imageScaledFactor),
                        1, 1);
             }
             else
             {
                 g.fillOval((int) (marker.getLocation().x / imageScaledFactor - enlargeAmount), 
                        (int) (marker.getLocation().y / imageScaledFactor - enlargeAmount),
                        2 * enlargeAmount + 1, 2 * enlargeAmount + 1);
             }
         }
     }
 
     // Just testing out these methods separately from the whole program
     public static void main(String args[]) throws IOException {
         BufferedImage imgOutline = ImageIO.read(new File("SampleImages/DSCN0152outline.png"));
         BufferedImage imgPoints = ImageIO.read(new File("SampleImages/DSCN0152points.png"));
         ArrayList<Point> squareCornersList = getPixelListForColor(imgPoints, Color.yellow);
         ArrayList<Point> controlPointList = getPixelListForColor(imgPoints, Color.red);
         ArrayList<Point> outlinePointList = getPixelListForColor(imgOutline, Color.magenta);
         
         BufferedImage combinedImg = overlay(imgPoints, imgOutline);
         BufferedImage combinedScaledImg = makeShrunkImageCopy(combinedImg, 4);
         
 //        enlargePoints(combinedScaledImg, outlinePointList, Color.magenta, 1, 4);
 //        enlargePoints(combinedScaledImg, controlPointList, Color.red, 3, 4);
 //        enlargePoints(combinedScaledImg, squareCornersList, Color.yellow, 3, 4);
 
 
         JLabel pictureLabel = new JLabel(new ImageIcon(combinedScaledImg));
         pictureLabel.setBackground(Color.black);
         pictureLabel.setOpaque(true);
         JFrame testFrame = new JFrame("ImageUtils Tester");
         testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         testFrame.getContentPane().add(pictureLabel);
         testFrame.pack();
         JScrollPane scrollBar = new JScrollPane(pictureLabel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                 JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
         testFrame.add(scrollBar);
         testFrame.setLocation(350, 0);
         testFrame.setSize(900, 700);
         testFrame.setVisible(true);
     }
 }
