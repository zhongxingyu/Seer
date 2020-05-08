 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.javascool.proglets.game;
 
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import org.javascool.tools.Macros;
 
 /**
  * This class defines a sprite that can be drawn in the render area and addressed events
  * @author gmatheron
  */
 public class Sprite extends Geometry implements Drawable {
     //TODO random(int, int);
     //TODO kbd events
     //TODO member vars
     /**
      * The image
      */
     private BufferedImage m_image;
 
     /**
      * Creates the image and registers it into the render area
      * @param x The X position of the topleft corner of the image
      * @param y The Y position of the topleft corner of the image
      * @param w The width of the image
      * @param h The height of the image
      */
     public Sprite(double x, double y, double w, double h) {
         super(x, y, w, h);
         ((Panel) Macros.getProgletPanel()).addItem(this);
     }
 
     /**
      * Loads the image from a file. This must be done before drawing starts.
      * If the file is not found a bug will be reported
      *      //TODO make this exception catchable
      * @param fileName  The image file from which the image will be loaded
      *      //TODO test supported formats
      */
     public void load(String fileName) {
         try {
             m_image = ImageIO.read(new File(fileName));
         } catch (IOException e) {
             org.javascool.JvsMain.reportBug(e); //TODO
         }
     }
 
     /**
      * Draws the sprite to the specified Graphics buffer. It the image is not loaded
      * it won't be displayed but it can still catch events !
      * @param g The Graphics buffer on which to draw the sprite
      */
     @Override
     public void draw(Graphics g) {
         if (m_image != null) {
             g.drawImage(m_image, (int)getX(), (int)getY(), (int)getWidth(), (int)getHeight(), null);
         }
     }
     
    public void destroy() {
        
    }
    
     private static final Logger LOG = Logger.getLogger(Sprite.class.getName());
 }
