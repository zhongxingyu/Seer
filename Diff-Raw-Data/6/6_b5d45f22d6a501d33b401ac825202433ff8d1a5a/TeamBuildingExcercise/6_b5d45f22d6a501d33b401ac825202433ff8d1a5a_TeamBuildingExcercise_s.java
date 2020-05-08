 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package devfortress.view.animation.events;
 
 import com.tabuto.j2dgf.Game2D;
 import com.tabuto.j2dgf.Group;
 import devfortress.view.animation.GameSprite;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 
 /**
  *
  * @author Sherlock
  */
 public class TeamBuildingExcercise extends Game2D {
 
     private BufferedImage BGR_IMAGE, MIGHT_GUY_IMAGE, ROCK_LEE_FACE_IMAGE, ROCK_LEE_EXCERCISE_IMAGE;
     private Group<GameSprite> sprites;
     private double widthScale, heightScale;
     private GameSprite bgr, rockLee, mightGuy, rockLeeExcercise;
    private int ROCK_LEE_SEQUENCE[] = new int[]{0, 0, 0, 1, 1, 1, 0, 0, 0, 2, 2, 2, 3, 3, 3, 0, 0, 0, 4, 4, 4, 5, 5, 5};
     private long timestamp = 0;
     private boolean isWaiting = false;
 
     public TeamBuildingExcercise(Dimension dim) {
         super(dim);
     }
 
     /**
      * Activate/Re-activate this engine.
      */
     @Override
     public void activate() {
         super.activate();
     }
 
     /**
      * Deactivate this engine and reset it to original state.
      */
     @Override
     public void deactivate() {
         super.deactivate();
         timestamp = 0;
         sprites.clear();
         sprites.add(bgr);
         sprites.add(mightGuy);
         sprites.add(rockLee);
         sprites.add(rockLeeExcercise);
         try {
             rockLeeExcercise.setFrameIndex(0);
         } catch (Exception ex) {
             Logger.getLogger(Holiday.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     @Override
     public void drawStuff(Graphics g) {
         sprites.move();
         try {
            System.out.println("draw");

             /*
              * Draw sprites.
              */
 
             System.out.println(timestamp);
             if (timestamp != 0 && System.currentTimeMillis() >= timestamp + 2000 && isWaiting != true) {
 //            g.clearRect(0, 0, DIM.width, DIM.height);
                 sprites.remove(mightGuy);
                 rockLee = new GameSprite(DIM, 0, 0, ROCK_LEE_FACE_IMAGE);
                 rockLee.setScales(widthScale, heightScale);
                 sprites.add(rockLee);
                 timestamp = System.currentTimeMillis();
                 System.out.println("end");
                 isWaiting = true;
             }
 
             if (timestamp != 0 && System.currentTimeMillis() >= timestamp + 2000) {
 //            g.clearRect(0, 0, DIM.width, DIM.height);
                 System.out.println("no");
                 sprites.remove(rockLee);
                 bgr = new GameSprite(DIM, 0, 0, BGR_IMAGE);
                 bgr.setScales(widthScale, heightScale);
                 sprites.add(bgr);
 
                 rockLeeExcercise = new GameSprite(DIM, 200, 10, ROCK_LEE_EXCERCISE_IMAGE, 23, 45);
                 rockLeeExcercise.setScales(4, 4);
                 rockLeeExcercise.setFrameSequence(ROCK_LEE_SEQUENCE);
                 rockLeeExcercise.setFrameIndex(0);
                 rockLeeExcercise.setAngleDegree(0);
                 rockLeeExcercise.setSpeed(0);
                 sprites.add(rockLeeExcercise);
                 timestamp = 0;
             }
             if (!sprites.isEmpty()) {
                 sprites.draw(g);
             }
 
         } catch (Exception ex) {
             Logger.getLogger(DeveloperIsSickEventAnimation.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 
     @Override
     public void initGame() {
         try {
             URL bgrURL = getClass().getResource("../../resources/sick_floor.png"),
                     rockLeeSequenceURL = getClass().getResource("../../resources/rockLeeSequence.png"),
                     mightGuyURL = getClass().getResource("../../resources/mightGuy.png"),
                     rockLeeURL = getClass().getResource("../../resources/rockLee.png");
 
             BGR_IMAGE = ImageIO.read(bgrURL);
             ROCK_LEE_EXCERCISE_IMAGE = ImageIO.read(rockLeeSequenceURL);
             MIGHT_GUY_IMAGE = ImageIO.read(mightGuyURL);
             ROCK_LEE_FACE_IMAGE = ImageIO.read(rockLeeURL);
 
             widthScale = (double) DIM.width / MIGHT_GUY_IMAGE.getWidth(null);
             heightScale = (double) DIM.height / MIGHT_GUY_IMAGE.getHeight(null);
             sprites = new Group<GameSprite>();
 
             /*
              * Add sprites.
              */
 
 
             mightGuy = new GameSprite(DIM, 0, 0, MIGHT_GUY_IMAGE);
             mightGuy.setScales(widthScale, heightScale);
             sprites.add(mightGuy);
             timestamp = System.currentTimeMillis();
 
         } catch (Exception ex) {
             Logger.getLogger(WorkIsHacked.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }
