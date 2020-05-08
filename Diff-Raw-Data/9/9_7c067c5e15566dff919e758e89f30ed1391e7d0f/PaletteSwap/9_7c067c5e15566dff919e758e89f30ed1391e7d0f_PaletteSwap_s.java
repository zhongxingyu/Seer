 package vooga.fighter.view;
 
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import sun.awt.image.ToolkitImage;
import vooga.fighter.util.Paintable;
 
 /**
  * This class is to be used for changing the colors of images. Currently,
  * PaletteSwap can only be used to set images to GrayScale
  * 
  * @author Bill Muensterman
  * 
  */
 
 public class PaletteSwap {
 
 	public void setImageToGrayScale(Paintable paintable) {
 		BufferedImage buffered = ((ToolkitImage) paintable).getBufferedImage();
 		BufferedImage temp = new BufferedImage(buffered.getWidth(),
 				buffered.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
 		temp.getScaledInstance(buffered.getWidth(), buffered.getHeight(), 0);
 		Graphics g = temp.getGraphics();
 		g.drawImage(buffered, 0, 0, null);
 		buffered = temp;
 		g.dispose();
 		paintable = (Paintable) buffered;
 	}
 }
