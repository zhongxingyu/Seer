 import javax.swing.*;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 
 /**
  * Simple poke-able grayscale image for displaying grid maps.
  *
  * @author zjb 3/09
  * @author jmd 11/05/11
  */
 public class GridMap extends JFrame {
 
     private BufferedImage theMap;
     private int imwidth, imheight;
     private double scale;
 
     /**
      * Construct a map image of given size and resolution.
      * Map's <b>center</b> will be at (0,0) and coordinates right-handed.
      * @param width map width in meters
      * @param height map height in meters
      * @param mpp Resolution in meters per pixel
      */
     public GridMap(int width, int height, double mpp) {
         imwidth = (int)(width/mpp);
         imheight = (int)(height/mpp);
         scale = mpp;
         theMap = new BufferedImage(imwidth,imheight,
                                    BufferedImage.TYPE_INT_ARGB);
         int midgray = (0xff << 24) | (180 << 16) | (180 << 8) | (180);
         for (int x = 0; x < imwidth; x++)
             for (int y = 0; y < imheight; y++)
                 theMap.setRGB(x,y,midgray);
 
         MapPanel mp = new MapPanel();
         add(mp);
     }
 
     /**
      * Update the map.
      * @param x X location to update (global coords, in meters)
      * @param y Y location to update (global coords, in meters)
      * @param value New map value (0->255)
      */
     void setVal(double x, double y, int value) {
         if (value < 0 || value > 255)
             return;
         int imx = (int)(x/scale + imwidth/2);
         // flip y to go from right-handed world to left-handed image
         int imy = (int)(imheight/2 - y/scale);
         //int rgbval = (0xff << 24) | (ival << 16) | (ival << 8) | ival;
         int rgbval = (0xff << 24) | (value << 16) | (value << 8) | value;
         if (imx >= 0 && imx < imwidth && imy >= 0 && imy < imheight)
             theMap.setRGB(imx,imy,rgbval);
     }
     
     void setParticle(double x, double y) {
         int imx = (int)(x/scale + imwidth/2);
         // flip y to go from right-handed world to left-handed image
         int imy = (int)(imheight/2 - y/scale);
         if (imx >= 0 && imx < imwidth && imy >= 0 && imy < imheight)
            theMap.setRGB(imx,imy,Color.RED.getRGB());
     }
     
     void clearParticle(double x, double y) {
         int imx = (int)(x/scale + imwidth/2);
         // flip y to go from right-handed world to left-handed image
         int imy = (int)(imheight/2 - y/scale);
         if (imx >= 0 && imx < imwidth && imy >= 0 && imy < imheight)
            theMap.setRGB(imx,imy,Color.WHITE.getRGB());
     }
 
     class MapPanel extends JPanel {
 
         protected void paintComponent(Graphics g) {
             g.drawImage(theMap,0,0,null);
         }
         public Dimension getPreferredSize() {
             return new Dimension(imwidth,imheight);
         }
     }
 }
