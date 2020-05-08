 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package laboonrace;
 
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.Random;
 import javax.swing.ImageIcon;
 
 /**
  *
  * @author eddy
  */
 public abstract class Item extends AnimatedLabel{
     
     public static final Dimension mySize = new Dimension(Global.size.width/8, 
                                                    Global.size.height/8);
     
     private int minSpeed;
     private int maxSpeed;
     protected int mySpeed;
     private long iconIntervalMax;
     private long iconInterval = 0;
     private boolean disappearing = false;
     
     public Item(String imageName, int speed, long iconIntervalMax, int index) {
         this(imageName, speed, speed, iconIntervalMax, index);
     }
     
     public Item(String imageName, int minSpeed, int maxSpeed, 
             long iconIntervalMax, int index) {
         this.minSpeed = minSpeed;
         this.maxSpeed = maxSpeed;
         this.iconIntervalMax = iconIntervalMax;
        super.imageIndex = index;
         initComponents();
         initImages(getImagesNormal());
     }
     
     public void update() {
         updateIcon();
         if(!isDisappearing()) {
             updateLocation();
         }
         if(isFinished()) {
             if(isDisappearing()) {
                 setVisible(false);
             } else {
                 initImages(getImagesNormal());
                 start(0, getNumberOfImages()-1, true);
             }
         }
     }
     
     public Rectangle getRect() {
         return new Rectangle(getLocation(), getSize());
     }
     
     public boolean isDisappearing() {
         return disappearing;
     }
     public void setDisappearing(boolean bool) {
         disappearing = bool;
     }
     
     public void disappear() {
         setDisappearing(true);
         initImages(getImagesDisappear());
         start(0, getNumberOfImages()-1, false);
     }
     
     public int getLifeFactor() {
         return 0;
     }
     public boolean isLootGiver() {
         return false;
     }
     public abstract void touched(Item item);
     public abstract ArrayList<ImageIcon> getImagesNormal();
     public abstract ArrayList<ImageIcon> getImagesDisappear();
     ////////////////////////////////////////////////////////////////////////////
     
     
     protected abstract void updateLocation();
     
     @Override
     protected void updateIcon() {
         if(iconInterval == iconIntervalMax) {
             super.updateIcon();
             if(!isEnabled()) {
                 setEnabled(true);
             }
             iconInterval = 0;
         } else {
             iconInterval++;
         }
     }
     
     protected void initComponents() {
         setSize(mySize);
         Point myPoint = getRandomPosition();
         mySpeed = getRandomSpeed();
         
         setLocation(myPoint);
         setOpaque(false);
         setEnabled(false);
     }
     
     private Point getRandomPosition() {
         Random rand = new Random();
         Point randPoint = 
                 new Point(rand.nextInt(Global.size.width - getSize().width),
                 -getSize().height);
         return randPoint;
     }
 
     private int getRandomSpeed() {
         Random rand = new Random();
         int randSpeed = maxSpeed;
         if(maxSpeed != minSpeed) {
             randSpeed = minSpeed + rand.nextInt(maxSpeed - minSpeed);
         }
         return randSpeed;
     }
 }
