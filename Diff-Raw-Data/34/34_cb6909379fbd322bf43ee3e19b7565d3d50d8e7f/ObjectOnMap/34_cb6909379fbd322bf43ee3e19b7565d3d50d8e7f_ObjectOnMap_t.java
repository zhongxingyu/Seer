 import com.sun.xml.internal.bind.annotation.XmlIsSet;
 import sun.security.x509.CRLDistributionPointsExtension;
 
 import java.awt.*;
 import java.util.ArrayList;
 
 public class ObjectOnMap {
 
     protected Vec position = new Vec(0, 0);
     protected Vec velocity;
     protected boolean is_destroyed = false;
 
 
     private boolean bullet_hit = false;
 
     protected ArrayList<Vec> Points = new ArrayList<Vec>();
 
 
     public void render(Graphics2D g) {
 
     }
 
     /**
      * Put in Geom Help
      */
     static boolean linesIntersect(Vec a1, Vec a2, Vec b1, Vec b2) {

        float a = (a1.getX() * b1.getY() - b1.getX() * a1.getY() - a1.getX() * b2.getY() + b2.getX() * a1.getY() + b1.getX() * b2.getY() - b2.getX() * b1.getY()) / (a1.getX() * b1.getY() - b1.getX() * a1.getY() - a1.getX() * b2.getY() - a2.getX() * b1.getY() + b1.getX() * a2.getY() + b2.getX() * a1.getY() + a2.getX() * b2.getY() - b2.getX() * a2.getY());
        float b = -(a1.getX() * a2.getY() - a2.getX() * a1.getY() - a1.getX() * b1.getY() + b1.getX() * a1.getY() + a2.getX() * b1.getY() - b1.getX() * a2.getY()) / (a1.getX() * b1.getY() - b1.getX() * a1.getY() - a1.getX() * b2.getY() - a2.getX() * b1.getY() + b1.getX() * a2.getY() + b2.getX() * a1.getY() + a2.getX() * b2.getY() - b2.getX() * a2.getY());

        return (a > 0 && a < 1 && b > 0 && b < 1);
     }
 
     protected void destroy() {
         is_destroyed = true;
     }
 
     protected void update() {
         checkCollision();
     }
 
 
     public boolean checkCollision() {
 
         for (int j = Game.Bullets.size() - 1; j >= 0; j--)
             for (int i = 0; i < Points.size() - 1; i++) {
                 if (linesIntersect(Game.Bullets.get(j).getPos(), Game.Bullets.get(j).getNextPos(), Points.get(i), Points.get(i + 1))) {
                     System.out.println("HIT");
                     destroy();
                     Game.Bullets.remove(j);
                     break;
                 }
             }
 
         return false;
 
     }
 
 
 }
