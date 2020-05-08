 package net.stuffrepos.tactics16.util.image;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Transparency;
 import java.awt.image.BufferedImage;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.ImageBuffer;
 
 /**
  *
  * @author Eduardo H. Bogoni <eduardobogoni@gmail.com>
  */
 public class ImageUtil {
 
     public static BufferedImage scaleImage(BufferedImage source, double scale) {
         Dimension newSize = new Dimension(
                 (int) (source.getWidth() * scale),
                 (int) (source.getHeight() * scale));
         Image image = source.getScaledInstance(
                 newSize.width,
                 newSize.height,
                 BufferedImage.SCALE_SMOOTH);
 
         return ImageUtil.copyBitmask(image);
     }
 
     public static BufferedImage copyBitmask(Image input) {
         final BufferedImage output = new BufferedImage(
                 input.getWidth(null),
                 input.getHeight(null),
                 Transparency.BITMASK);
 
         Graphics2D g = output.createGraphics();
         g.drawImage(input, 0, 0, null);
         g.dispose();
 
         new PixelImageIterator(output) {
 
             @Override
             public void iterate(int x, int y, int rgb) {
                 output.setRGB(x, y, ColorUtil.getRgbBitmask(rgb));
             }
         };
 
         return output;
     }
 
     public static BufferedImage slickToAwt(final org.newdawn.slick.Image source) {
         
         final BufferedImage awt = new BufferedImage(
                 source.getWidth(),
                 source.getHeight(),
                 BufferedImage.BITMASK);
         
         new PixelImageIterator(awt) {
 
             @Override
             public void iterate(int x, int y, int rgb) {
                 awt.setRGB(x, y, ColorUtil.rgba(source.getColor(x, y)));
             }
         };
         
         return awt;
     }
 
     public static org.newdawn.slick.Image awtToSlick(BufferedImage source) {
         final ImageBuffer slick = new ImageBuffer(source.getWidth(), source.getHeight());
         
         new PixelImageIterator(source) {
 
             @Override
             public void iterate(int x, int y, int rgb) {                
                 slick.setRGBA(
                         x, 
                         y, 
                         ColorUtil.getRed(rgb), 
                         ColorUtil.getGreen(rgb), 
                        ColorUtil.getBlue(rgb),
                        ColorUtil.getAlpha(rgb)
                         );                
             }
         };
         
         return new org.newdawn.slick.Image(slick);
     }
 }
