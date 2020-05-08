 package asteroids;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Polygon;
 import java.awt.Toolkit;
 import java.awt.geom.AffineTransform;
 
 public class Rock
 {
     private AffineTransform identity = new AffineTransform();
     private double rockHeading = 0;//in radians
     private double rockXspeed = 0;
     private double rockYspeed = 0;
     double rockXpos = 200;
     double rockYpos = 200;
     int width = Toolkit.getDefaultToolkit().getScreenSize().width;
     int height = Toolkit.getDefaultToolkit().getScreenSize().height;
     int[] rockXpoints =
     {
         0, 3, 3, 0, -2, -2, 0
     };
     int[] rockYpoints =
     {
         -3, -1, 0, 2, 0, -1, -3
     };
     Polygon rockOutline = new Polygon(rockXpoints, rockYpoints, rockXpoints.length);
 
     public Rock()
     {
         rockXpos = Math.random() * 1000;
         rockYpos = Math.random() * 1000;
     }
     public void paintself(Graphics2D g2)
     {
        g2.setTransform(identity);
         g2.setColor(Color.WHITE);
         g2.translate(rockXpos, rockYpos);
         g2.scale(30, 30);
         g2.fill(rockOutline);
         g2.setStroke(new BasicStroke(.5f));
         g2.draw(rockOutline);
     }
 
     public void moveSelf()
     {
         rockXpos = rockXpos + 1;
//        rockYpos = rockYpos + 1;
     }
             
 }   
