 package Core;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 import java.awt.image.ImageObserver;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 
 
 public class Player {
 
 	public final int POS_X;
 	public final int POS_Y;
 	private int IMG_WIDTH;
 	private int IMG_HEIGHT;
 	public static final int STEP_SIZE = 10;
 
 	public final static int MAX_SOBRIETY = 100;
 	public final static int MIN_SOBRIETY = 0;
 
 	public static int health;
 	public static int sobriety = MAX_SOBRIETY;
 	public static int score = 0;
 	
 	private BufferedImage model;
 	private ImageObserver modelObserver;
 	private static Canvas canvas;
 
 	/**
 	 * @return the canvas
 	 */
 	public Canvas getCanvas() {
 		return canvas;
 	}
 
 	public Player(BufferedImage model, Canvas canvas){
 		this.model = model;
 		this.IMG_WIDTH = model.getWidth();
 		this.IMG_HEIGHT = model.getHeight();
 		this.canvas = canvas;
 		this.POS_X = canvas.getWidth()/2 - IMG_WIDTH/2;
 		this.POS_Y = (int)(canvas.getHeight()*0.65 - IMG_HEIGHT/2);
 		//this.sobriety = MAX_SOBRIETY;
 	}
 	
 	public int getWidth(){
 		return model.getWidth();
 	}
 
 
 	public void draw(Graphics g, int x, int y){
 		g.drawImage(model,POS_X - x,POS_Y - y,null);
 	}
 
 	public void setImage(String filename){
 
 		try{
 			this.model = ImageIO.read(new FileInputStream("src"+File.separatorChar+"data"+File.separatorChar+filename));	
 		}
 		catch(IOException e){
 			e.printStackTrace();
 		}
 		this.IMG_HEIGHT = model.getHeight();
 		this.IMG_WIDTH = model.getWidth();
 	}
 
 	/**
 	 * checks if the player gets too drunk, they black out 
 	 * and their sobriety level resets.
 	 */
 	public static  void blackOut(){
 
		
 		if (sobriety <= MIN_SOBRIETY){
 			//Black out - need to do this
 			//reset sobriety
 			sobriety = MAX_SOBRIETY;
 			
 		}
		canvas.setOpacity(1.0f-((float)sobriety/(float)MAX_SOBRIETY));
	}
	
	public void getHigh(){
		canvas.getHigh();
 	}
 	
 }
