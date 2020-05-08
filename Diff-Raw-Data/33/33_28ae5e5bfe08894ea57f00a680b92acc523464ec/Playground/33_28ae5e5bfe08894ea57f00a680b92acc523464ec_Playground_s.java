 package propra2012.gruppe33.graphics.sprite;
 
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 
 
 //Just my little Playground... You dont need to understand what happens here....
 public class Playground extends JFrame {
 	
 	private BufferedImage im;
 	private SpriteImpl spriteTest;
 	
 	private Timer timer = new Timer();
 	
 
 	public Playground() throws Exception {
 
 		
 		spriteTest = new SpriteImpl();
 		spriteTest.setImage(ImageIO.read(new File("C:/male.png")), 4, 4);
 		
		spriteTest.addAnimation("test", 1000, new Point(0,0),new Point(1,0),new Point(2,0),new Point(3,0));
 		
 		timer.schedule(new TimerTask() {
 			
 			@Override
 			public void run() {
 				repaint();
 			}
 		}, 0, 33);
 
 		
 		
 		
 		setVisible(true);
 	}
 
 	public void paint(Graphics g) {
 
 		super.paint(g);

		g.drawImage(spriteTest.getImage("test"), 25, 25, null);
 	}
 
 
 	
 	public static void main(String[] args) throws Exception{
 		Playground pg = new Playground();
 		
 		
 		
 		
 	}
 }
