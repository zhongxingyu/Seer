 package karel;
 
 import java.net.*;
 import javax.swing.*;
 import java.awt.*;
 
 /**
 
  @author H-Town
  */
 public class home extends Entity
 {
     private Image image;
     //constructor
     public home(int x,int y)
     {
         super(x,y);
        URL loc = this.getClass().getResource("home.png");
         ImageIcon iia = new ImageIcon(loc);
         image = iia.getImage();
         this.setImage(image);
     }
 }
