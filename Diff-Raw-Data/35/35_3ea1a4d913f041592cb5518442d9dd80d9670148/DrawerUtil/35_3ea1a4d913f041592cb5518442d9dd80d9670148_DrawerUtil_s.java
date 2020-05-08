 package net.stuffrepos.tactics16.util.image;
 
import java.awt.Polygon;
 import org.newdawn.slick.Graphics;
 
 /**
  *
  * @author Eduardo H. Bogoni <eduardobogoni@gmail.com>
  */
 public class DrawerUtil {
 
     public static void fill3dRect(Graphics g, int x, int y, int width, int height, boolean b) {
         //TO-DO: replace by real 3d rectangle.
         g.fillRect(x, y, width, height);        
    }       

    public static void fillPolygon(Graphics g, Polygon value) {
        throw new UnsupportedOperationException("Not yet implemented");
     }

     
 }
