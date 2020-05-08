 package karel;
 
 //Objects karel can pick up
 
 import java.awt.Image;
 import java.net.URL;
 import javax.swing.ImageIcon;
 
 public class Gem extends Entity
 {
     //constructor
     public Gem(int x,int y)
     {
         super(x,y);
         
        URL loc = this.getClass().getResource("gem.png");
         ImageIcon iia = new ImageIcon(loc);
         Image image = iia.getImage();
         this.setImage(image);
     }
 }
