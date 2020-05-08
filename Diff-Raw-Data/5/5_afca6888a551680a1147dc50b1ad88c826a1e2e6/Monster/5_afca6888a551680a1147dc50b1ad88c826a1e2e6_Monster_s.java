 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 public class Monster{
 	static File file = new File("b.png");
 	File file2 = new File("./image2.txt");
 	static int sleep = 1000;
 	public Graphics g = null;
 	private int x_position = 0;
 	private int y_position = 0;
 	private static boolean live = false;
 	private static BufferedImage image1 = null;
 	public Monster(int x, int y, Graphics g, int health, int speed){
		set_monster(x, y, g);
 	}
 	//sets the private position variables with the ones
 	//passed from the map
 	public void set_monster(int x, int y, Graphics g, int health, int speed){
 		this.x_position = x;
 		this.y_position = y;
 		this.g = g;
                 this.health = health;
                 this.speed = speed;
 		paint();
 	
 		try{
 			image1 = ImageIO.read(file);
 			//BufferedImage image2  = ImageIO.read(file2);
 			}
 		catch(IOException e){
 			
 		}
 	}
 	
 	//draws the tower on the map
 	public void paint(){
 	//System.out.println(x_position + " " + y_position);
 		g.drawImage(image1, x_position, y_position, null);
 
 	}
 	
 	//draws attack animation
 	/*public static void paint2(){
 		
 		g.drawImage(image1, x_position, y_position, null);
 	}*/
 	// this gets called after placement of first tower
 }
