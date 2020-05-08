 package vooga.rts.gui.menus.gamesubmenus;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.util.Observable;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.InteractiveEntity;
 import vooga.rts.gui.Button;
 import vooga.rts.gui.Window;
 import util.Location;
 
 
 public class InfoSubMenu extends SubMenu {
 
     public InfoSubMenu (String image, Dimension size, Location pos) {
         super(image, size, pos);
     }
 
     @Override
     public void paint (Graphics2D pen) {
         super.paint(pen);
         pen.setFont(new Font("Arial", Font.PLAIN, 24));
         try {
             if (mySelectedEntity != null) {
                 if (mySelectedEntity.getInfo() != null) {
                     pen.setColor(Color.WHITE);
                     pen.drawString(mySelectedEntity.getInfo().getName(), (int) 280,
                                    (int) Window.D_Y - 90);
                     pen.setFont(new Font("Arial", Font.PLAIN, 20));
                     pen.setColor(Color.GRAY);
                     pen.drawString(mySelectedEntity.getInfo().getDescription(), (int) 280,
                                    (int) Window.D_Y - 65);
                    if (mySelectedEntity.getInfo().getButtonImage() != null) {                        
                         pen.drawImage(mySelectedEntity.getInfo().getButtonImage(), (int) 220,
                                      (int) Window.D_Y - 90, 50, 50, null);
                     }
                 }
             }
         }
         catch (NullPointerException np) {
             // catch weird error
         }
     }
 
     @Override
     public void processClick (int x, int y) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void processHover (int x, int y) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void update (Observable o, Object arg) {
         // TODO Auto-generated method stub
 
     }
 
 }
