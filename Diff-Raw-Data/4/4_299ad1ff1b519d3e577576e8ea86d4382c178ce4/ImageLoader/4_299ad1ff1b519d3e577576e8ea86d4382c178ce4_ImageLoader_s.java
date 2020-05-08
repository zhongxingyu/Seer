 /**
  * @author Jenny Zhen
  * @author Grant Kurtz
  * date: 04.27.13
  * language: Java
  * file: ImageLoader.java
  * project: Pac-man
  *          Implementation of the classic Pac-Man game in Java.
  */
 
 package views;
 
 import javax.imageio.ImageIO;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 public class ImageLoader {
 
 	private BufferedImage compositeImage;
 
 	private Image[][] pacManImages;
 
 	private static ImageLoader loader;
 
 	private ImageLoader(){
 		try{
			compositeImage = ImageIO.read(new File("images/pacman.png"));
 		}
 		catch(IOException e){
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 
 	public static ImageLoader getImageLoaderInstance(){
 		if(loader == null)
 			loader = new ImageLoader();
 		return loader;
 	}
 
 	public Image[][] getPacManImages(){
 		if(pacManImages == null){
 			pacManImages = new Image[4][2];
 			pacManImages[0][0] = compositeImage.getSubimage(2, 2, 15, 15);
 			pacManImages[0][1] = compositeImage.getSubimage(25, 2, 15, 15);
 			pacManImages[1][0] = compositeImage.getSubimage(43, 2, 15, 15);
 			pacManImages[1][1] = compositeImage.getSubimage(2, 23, 15, 15);
 			pacManImages[2][0] = compositeImage.getSubimage(21, 23, 15, 15);
 			pacManImages[2][1] = compositeImage.getSubimage(3, 3, 15, 15);
 			pacManImages[3][0] = compositeImage.getSubimage(3, 3, 15, 15);
 			pacManImages[3][1] = compositeImage.getSubimage(3, 3, 15, 15);
 		}
 		return pacManImages;
 	}
 }
