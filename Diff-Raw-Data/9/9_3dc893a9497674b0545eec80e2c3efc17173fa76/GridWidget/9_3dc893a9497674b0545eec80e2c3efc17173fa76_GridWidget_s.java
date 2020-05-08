 import java.awt.*;
 import javax.swing.*;
 import java.util.*;
 
 public class GridWidget extends JComponent {
     private GridSquare gridSquare;
     //private static final Color bg = new Color(64, 64, 16);
     private Color bg = new Color(64, 64, 16);
     private static final Color fg = new Color(64, 64, 64);
 
     public Dimension getMinimumSize()   { return new Dimension(24, 24); }
     public Dimension getPreferredSize() { return new Dimension(24, 24); }
 
     public GridWidget(GridSquare gridSquare) {
         this.gridSquare = gridSquare;
         bg = Util.randomColor();
     }
 
     public void paintComponent(Graphics g) {
         paintComponent((Graphics2D)g);
     }
 
     public void paintComponent(Graphics2D g) {
         final int w = getWidth();
         final int h = getHeight();
         final int q = Math.min(w, h);
         //g.setBackground(bg);
         //g.clearRect(0, 0, w, h);
         //g.setColor(fg);
         //g.fillOval(0, 0, w, h);
 
         // The animal takes up 16px when the plant is 24px,
         // but it also scales nicely.
         final int aw = (int)Math.ceil(q*0.66);
         final int ah = (int)Math.ceil(q*0.66);
         final Plant  plant  = gridSquare.getPlant();
         final Animal animal = gridSquare.getAnimal();
         g.drawImage(plant == null? Resources.dirtImage: plant.getImage(),
             0, 0, w, h, null);
        g.drawImage(animal == null? animal: animal.getImage(),
             w/2 - aw/2, h/2 - ah/2,
             aw, ah, null);
         //g.drawImage(Resources.gridSquareOverlayImage,
             //0, 0, w, h, null);
     }
 }
