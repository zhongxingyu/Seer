 package streamoutlining;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
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
      * Note: topImg and bottomImg should have matching dimensions.
      * @param topImg - image to be placed on top of bottomImg
      * @param bottomImg - image placed underneath the topImg
      * @return a new composite image resulting from the overlay.
      */
     public static BufferedImage overlay(BufferedImage topImg, BufferedImage bottomImg)
     {
        BufferedImage combined = new BufferedImage(bottomImg.getWidth(), bottomImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
 
         // paint each image onto the new canvas
         Graphics g = combined.getGraphics();
         g.drawImage(bottomImg, 0, 0, null);
         g.drawImage(topImg, 0, 0, null);
 
         return combined;
     }
     
     /**
      * 
      * @param img - Image to be searched
      * @param color - color of interest
      * @return a list of points containing (x,y) for each pixel in the image 
      *         that exactly matches the color (and alpha transparency) specified.
      *         e.g., if the color argument is entirely opaque (non-transparent, alpha=255)
      *          then this method will only return fully opaque pixel matches.
      */
     public static ArrayList<Point> getPixelListForColor(BufferedImage img, Color color) 
     {
         ArrayList<Point> pixelList = new ArrayList<Point>();
     
         for(int y = 0; y < img.getHeight(); y++)
         {
             for(int x = 0; x < img.getWidth(); x++)
             {
                 // IMPORTANT NOTE: 
                 // the second argument needs to be true so we read in the ALPHA transparency
                 // of the pixel -- otherwise we can pick up false positives of pixels that
                 // used to be the certain color, but then were transparent
                 Color pixelColor = new Color(img.getRGB(x,y),true); 
                 // check if the pixel color (and alpha) matches
                 if (pixelColor.equals(color))
                 {
                     pixelList.add(new Point(x,y));
                 }
             }
         }
         return pixelList;
     }
     
     /**
      *  Modifies the given image by expanding each pixel of a certain color by
      *  drawing a circle of a larger radius on top of it.
      * 
      * @param img - image to be modified
      * @param pointList - list of (x,y) points to enlarge
      * @param color - color to use for filling in circles
      * @param enlargeAmount - radius of enlargement.
      */
     public static void enlargePoints(BufferedImage img, ArrayList<Point> pointList, Color pointColor, int enlargeAmount)
     {
         Graphics g = img.getGraphics();
         for (Point p: pointList)
         {
             //draw a circle at each point, with the radius (enlargeAmount + 1)
             g.setColor(pointColor);
             g.fillOval((int)(p.getX()-enlargeAmount), (int) (p.getY()-enlargeAmount), 
                        2*enlargeAmount+1, 2*enlargeAmount+1);
         }
     }
  
     // Just testing out these methods separately from the whole program
     public static void main(String args[]) throws IOException
     {
         BufferedImage imgOutline = ImageIO.read(new File("SampleImages/DSCN0152outline.png"));
         BufferedImage imgPoints = ImageIO.read(new File("SampleImages/DSCN0152points.png"));
         ArrayList<Point> squareCornersList = getPixelListForColor(imgPoints, Color.yellow);
         ArrayList<Point> controlPointList = getPixelListForColor(imgPoints, Color.red);
         ArrayList<Point> outlinePointList = getPixelListForColor(imgOutline, Color.magenta);
 
         BufferedImage combinedImg = overlay(imgPoints, imgOutline);
         enlargePoints(combinedImg, outlinePointList, Color.magenta, 1);
         enlargePoints(combinedImg, controlPointList, Color.red, 3);
         enlargePoints(combinedImg, squareCornersList, Color.yellow, 3);
         
         
        JLabel pictureLabel = new JLabel(new ImageIcon(imgOutline));
         pictureLabel.setBackground(Color.black);
         pictureLabel.setOpaque(true);
         JFrame testFrame = new JFrame("ImageUtils Tester");
         testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         testFrame.getContentPane().add(pictureLabel);
         testFrame.pack();
         JScrollPane scrollBar=new JScrollPane(pictureLabel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                 JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
         testFrame.add(scrollBar);
         testFrame.setLocation(350,0);
         testFrame.setSize(900,700);
         testFrame.setVisible(true);
     }
    
 } 
