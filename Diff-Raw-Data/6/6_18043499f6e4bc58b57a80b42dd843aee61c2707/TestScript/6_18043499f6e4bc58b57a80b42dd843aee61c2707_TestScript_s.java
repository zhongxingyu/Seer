 package TestPackaging;
 
 import TestPackaging.Conversions.PaintUtils.PaintUtils;
 import org.powerbot.event.PaintListener;
 import org.powerbot.script.AbstractScript;
 import org.powerbot.script.Manifest;
import org.powerbot.script.methods.Environment;
import org.powerbot.script.methods.Game;
import org.powerbot.script.wrappers.*;
 import org.powerbot.script.wrappers.Component;
 
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.awt.image.BufferedImageOp;
 import java.awt.image.ConvolveOp;
 import java.awt.image.Kernel;
 
 @Manifest(authors = { "Inf3cti0us -bro433-" }, name = "Name Sensor", description = "Sensors Your accounts name!", version = 0.2, topic = 2)
 public class TestScript extends AbstractScript implements PaintListener {
 
     //Old -> WidgetChild wUsername = new Widget(137).getChild(53);
    final Component componentUsername = ctx.widgets.get(137, 53);
 
     /** Meh
     WidgetChild slot1KeyBind = new Widget(640).getChild(70);
     WidgetChild slot2KeyBind = new Widget(640).getChild(75);
     WidgetChild slot3KeyBind = new Widget(640).getChild(80);
     WidgetChild slot4KeyBind = new Widget(640).getChild(85);
     WidgetChild slot5KeyBind = new Widget(640).getChild(90);
     WidgetChild slot6KeyBind = new Widget(640).getChild(95);
     WidgetChild slot7KeyBind = new Widget(640).getChild(100);
     WidgetChild slot8KeyBind = new Widget(640).getChild(105);
     WidgetChild slot9KeyBind = new Widget(640).getChild(110);
     WidgetChild slot10KeyBind = new Widget(640).getChild(115);
     WidgetChild slot11KeyBind = new Widget(640).getChild(120);
     WidgetChild slot12KeyBind = new Widget(640).getChild(125);
     */
 
 
     public BufferedImage sensoredUsername;
     public BufferedImage dataBuffer;
 
     boolean isSmeared = false;
     Rectangle noUsername = new Rectangle(548, 424, 189, 31);
 
     @Override
     public void run() {
         if(ctx.game.isLoggedIn()){ //Game.isLoggedIn()){
             if(!isSmeared && componentUsername.isVisible()){
             createBlurredImage(5);
                 isSmeared = true;
             }
 
             while(isSmeared){
                sleep((int) 100L);         //TODO Find out what replaces this..
                }
         }
         //return 43; //Best Number In the world..
     }
 
     public BufferedImage getCanvasImage() {
         Canvas meh = ctx.getClient().getCanvas();
         int w = meh.getWidth();
         int h = meh.getHeight();
         BufferedImage image = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
         Graphics2D g2 = image.createGraphics();
         meh.paint(g2);
         g2.dispose();
         return image;
     }
 
     /**
      *
      * @param blurFactor How much blur is added to
      */
     public void createBlurredImage(int blurFactor) {
 
         //Capture Username
                             sensoredUsername = getCanvasImage().getSubimage(
                                     componentUsername.getAbsoluteLocation().x,
                                     componentUsername.getAbsoluteLocation().y,
                                     componentUsername.getWidth()-15,
                                     componentUsername.getHeight());
         /*
         sensoredUsername = Environment.captureScreen().getSubimage(wUsername.getAbsoluteX(), wUsername.getAbsoluteY(),
                 wUsername.getWidth()-15, wUsername.getHeight());
                 */
 
         //
         dataBuffer = new BufferedImage(sensoredUsername.getWidth(null),
                 sensoredUsername.getHeight(null),
                 BufferedImage.TYPE_INT_BGR);
 
         //Getting the graphics
         Graphics g = dataBuffer.getGraphics();
 
         //Drawing the sensoredUsername to 500x300
         g.drawImage(sensoredUsername, 500, 300, null);      // g.drawImage(mshi, 455, 255, null);hh
 
         //Declare the blurKernel
         float[] blurKernel = {
                 1 / 9f, 1 / 9f, 1 / 9f,
                 1 / 9f, 1 / 9f, 1 / 9f,
                 1 / 9f, 1 / 9f, 1 / 9f
         };
 
         //Initialize the blur
         BufferedImageOp blur = new ConvolveOp(new Kernel(3, 3, blurKernel));     //new Kernel 3,3,blurKernel
 
         //Blur the username blurFactor times (lol)
         for(int i=0; i < blurFactor; i++){
         sensoredUsername = blur.filter(sensoredUsername, new BufferedImage(sensoredUsername.getWidth()-1,
                 sensoredUsername.getHeight()-1, sensoredUsername.getType()));
         }
          //Dispose of the graphics.. We are done blurring!
         g.dispose();
     }
 
 
     @Override
     public void repaint(Graphics graphics) {
         Graphics2D g = (Graphics2D)graphics;
         Font meh = g.getFont();
                if(ctx.game.isLoggedIn() && componentUsername!=null && componentUsername.isOnScreen() && componentUsername.isVisible()){
                   g.drawImage(sensoredUsername, componentUsername.getAbsoluteLocation().x-2, componentUsername.getAbsoluteLocation().y+1,
                           componentUsername.getWidth()-16, componentUsername.getHeight()-4,null);
 
         } else{
                    PaintUtils.drawStringCentered("No username to block!", meh, noUsername, Color.WHITE, g);
                    PaintUtils.drawGrid(g, 550, 257, 45, 20, 4, 12);    //TODO Work on drawing a grid..
 
                }
 
     }
 
 }
