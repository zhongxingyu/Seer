 package view;
 
 import java.awt.Graphics2D;
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsEnvironment;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.io.InputStream;
 import javax.imageio.ImageIO;
 
 /**
  * Class used to load resources.
  * 
  * @author Calleberg
  *
  */
 public class Resources {
 
 	public static final GraphicsConfiguration CONFIG = GraphicsEnvironment
 			.getLocalGraphicsEnvironment().getDefaultScreenDevice()
 			.getDefaultConfiguration();
 	
 	/**
 	 * Gives an array of all the images on the texture sheet at the specified path.
 	 * @param path the path to the texture sheet.
 	 * @param col the number of columns.
 	 * @param row the number of rows.
 	 * @return an array of all the images on the sheet.
 	 */
 	public static BufferedImage[] splitImages(String path, int col, int row) {
 		BufferedImage image = null;
 		try{
 			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			InputStream input = classLoader.getResourceAsStream(path);	
 			image = ImageIO.read(input);
 		}catch (IOException e) {
 			System.out.println("Could not find resources");
 		}
 		int frame = 0; //counter, or array position.
 		int width = image.getWidth() / col;
 		int height = image.getHeight() / row;
 		BufferedImage[] images = new BufferedImage[col * row];
 		
 		for(int y = 0; y < row; y++) {
 			for(int x = 0; x < col; x++) {
 				images[frame] = new BufferedImage(width, height, image.getTransparency());
 				Graphics2D g = images[frame].createGraphics();
 				g.drawImage(image, 0, 0, width, height,	//destination of image.
 						x * width, y * height, (x + 1) * width, (y + 1) * height, //source of the image on the sheet.
 						null);
 				g.dispose();
 				frame++;
 			}//x
 		}//y
 		
 		return images;
 	}
 	
 	/**
 	 * Loads the image at the specified path.
 	 * @param path the path to the image.
 	 * @return the image, <code>null</code> if the image couldn't be located or found.
 	 */
 	public static BufferedImage getSingleImage(String path) {
 		try{
 			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
 			InputStream input = classLoader.getResourceAsStream(path);
 			BufferedImage image = ImageIO.read(input);
 			if(image != null) {
 				return image;
 			}
 		}catch (IOException e) {
 			System.out.println("Could not find resources");
 		}
 		return null;
 	}
 }
