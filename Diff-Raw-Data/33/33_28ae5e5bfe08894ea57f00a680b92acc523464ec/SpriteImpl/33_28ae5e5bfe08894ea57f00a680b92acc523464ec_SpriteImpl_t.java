 package propra2012.gruppe33.graphics.sprite;
 
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 import javax.imageio.ImageIO;
 
 
 //This Class handels the SpriteImages for different Game Objects
 //Every Sprite can add multiple Animations
 
 
 public class SpriteImpl implements Sprite {
 
 	private Map<String, Animation> animations = new HashMap<String, Animation>();
 	private List<BufferedImage> imageList = new ArrayList<BufferedImage>();
 	private BufferedImage image;
 	private int rasterX, rasterY;
 	
 
 	//Initiation of a new main Image for this Sprite
 
 	public void setImage(BufferedImage image, int rasterX, int rasterY) {
 		this.image = image;
 		this.rasterX = rasterX;
 		this.rasterY = rasterY;
 	}
 
 
 	
 	//Retuns a Sub Image of a Main Image
 
 	public  BufferedImage getSubImage(BufferedImage image, int rasterX,int rasterY, int x, int y) {
 		
 		//Calculating the Size of one Sub Image
 		int sizeXPlate = image.getWidth() / rasterX;
 		int sizeYPlate = image.getHeight() / rasterY;
 		
 		//Creates the actual SubImage
		BufferedImage subImage = image.getSubimage(x*sizeXPlate, y*sizeYPlate, sizeXPlate, sizeYPlate);
		
 		
 		//Retun the SubImage
 		return subImage;
 	}
 	
 	//This Method adds a new Animation Que to a Sprite
 
 	public void addAnimation(String name, long time, Point... points) {
 		
 		//Create a new List for every SubImage of one Animation
 		List<BufferedImage> animationImages = new ArrayList<BufferedImage>();
 		
 		//Creating every single SubImage 
 		for(int i = 0; i < points.length; i++){
 			animationImages.add(getSubImage(image, rasterX, rasterY, points[i].x, points[i].y));
 		}
 		
 		//Generate a new Animation with a name, the generated SubImages, and a time for one Frame of an Animation
 		Animation newAnimation = new Animation(name, animationImages,time);
 		
 		//Add the new Animation to the HashMap
 		animations.put(name, newAnimation);
 
 	}
 
 	
 	//This Method return the actual Image of an Animation for the Renderer
 	@Override
 	public BufferedImage getImage(String name) {
 		Animation animation = animations.get(name);
 
 		return animation.getAnimationImage();
 	}
 
 }
