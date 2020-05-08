 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package evopaint;
 
 import evopaint.pixel.Pixel;
 import evopaint.gui.MainFrame;
 
 import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
 
 /**
  *
  * @author tam
  */
 public class EvoPaint {
 
     private Configuration configuration;
     private Perception perception;
     MainFrame frame;
 
     public MainFrame getFrame() {
         return frame;
     }
 
     public void setFrame(MainFrame frame) {
         this.frame = frame;
     }
 
     public Perception getPerception() {
         return perception;
     }
 
     public void setConfiguration(Configuration configuration) {
         this.configuration = configuration;
     }
 
     public void work() {
         this.perception.createImage(configuration.world);
         frame.getShowcase().repaint();
         long timeStamp = System.currentTimeMillis();
 
         while (true) {
 
            if (configuration.runLevel == Configuration.RUNLEVEL_RUNNING) {
                 configuration.world.step();
             }
            else if (configuration.runLevel == Configuration.RUNLEVEL_PAINTING_ONLY) {
                long timedifference = System.currentTimeMillis() - timeStamp;
                if (timedifference < 1000 / configuration.fps) {
                    try {
                    Thread.sleep(timedifference);
                    } catch (InterruptedException ex) {
                    }
                }
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                }
                 continue;
             }
 
             long currentTime = System.currentTimeMillis();
             if (currentTime - timeStamp > 1000 / configuration.fps) {
                 timeStamp = currentTime;
                 this.perception.createImage(configuration.world);
                 frame.getShowcase().repaint();
             }
         }
     }
 
     public Configuration getConfiguration() {
         return configuration;
     }
 
     public static void main(String args[]) {
         EvoPaint evopaint = new EvoPaint();
 
         evopaint.work();
     }
     
     public EvoPaint() {
         this.configuration = new Configuration();
 
         // create empty world
         long time = 0;
         this.configuration.world = new World(
                 new Pixel[configuration.dimension.width * configuration.dimension.height],
                 time, configuration);
 
         this.perception = new Perception(new BufferedImage(configuration.dimension.width, configuration.dimension.height,
                 BufferedImage.TYPE_INT_RGB));
 
         this.perception.createImage(configuration.world);
 
         this.frame = new MainFrame(configuration, this);
     }
 }
