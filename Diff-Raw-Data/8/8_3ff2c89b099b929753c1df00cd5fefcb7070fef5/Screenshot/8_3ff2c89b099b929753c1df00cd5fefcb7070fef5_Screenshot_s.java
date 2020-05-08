 package rhinoceros;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.Robot;
 import java.awt.Toolkit;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import javax.swing.JOptionPane;
 
 public class Screenshot
 {
 	private static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 	private static Rectangle screenRectangle = new Rectangle(screenSize);
 	private static Robot robot;
 
 	
 	public static BufferedImage getScreenshot() {
 		try {
 			BufferedImage image = Screenshot.getRobot().createScreenCapture(screenRectangle);
 			return image;
 		}
 		catch (Exception e) {
 			return getErrorImage();
 		}
 	}
 	
 	public static BufferedImage getThumbnail() {
 		BufferedImage image = getScreenshot();
		image = resize(image, 500, 500);
 		return image;
 	}
 	
 	private static BufferedImage getErrorImage() {        
         BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);        
         img = new BufferedImage(150, 40, BufferedImage.TYPE_INT_RGB);  
         Graphics2D g2d = (Graphics2D) img.getGraphics();
         //g2d.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));  
         //g2d.setColor(Color.WHITE);  
         g2d.drawString("Doh.. Error!", 0, 0);
         g2d.dispose();
         return img;
 	}
 	
 	private static BufferedImage resize(BufferedImage image, int width, int height) {
		BufferedImage resizedImage = new BufferedImage(width, height,
		BufferedImage.TYPE_INT_ARGB);
 		Graphics2D g = resizedImage.createGraphics();
 		g.drawImage(image, 0, 0, width, height, null);
 		g.dispose();
 		return resizedImage;
 	}
 	
 	private static Robot getRobot() throws Exception {
 		if(Screenshot.robot == null) {
 			Screenshot.robot	= new Robot();
 		}
 		
 		return Screenshot.robot;
 	}
 	
 }
