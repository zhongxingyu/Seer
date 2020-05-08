 package Graphics;
 /**
  * @author Knut Hartmann <BR>
  *         Flensburg University of Applied Sciences <BR>
 import GUI.JFrameDemo;
  *         Knut.Hartmann@FH-Flensburg.DE
  * 
  * Courtesy of the ship image to Jonathan S. Harbour
  * @see http
  *      ://jharbour.com/wordpress/portfolio/beginning-java-se-6-game-programming
  *      -3rd-ed/
  * 
  * @version October 26, 2012
  */
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
import GUI.JFrameDemo;
 
 @SuppressWarnings("serial")
 public class StaticAnimationDemo extends JFrameDemo implements Runnable {
 
 	private StaticAnimation player = null;
 	
 	private Thread gameLoop;
 	private float animationFrequency = 10.0f;
 	private int frameTime = Math.round(1000 / animationFrequency);
 
 	public StaticAnimationDemo() {
 		initCanvas("Static Animation Demo", 800, 600);
 		player = new StaticAnimation(this);
 		player.addSprite("Images/ship_shield.png");
 		player.addSprite("Images/ship_thrust.png");
 		setVisible(true);
 		startGameLoop();
 	}
 	
 	private void startGameLoop() {
 		gameLoop = new Thread(this);
 		gameLoop.start();		
 	}
 
 	@Override
 	public void run() {
 		while (true) {
 			repaint();
 			try {
 				Thread.sleep(frameTime);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}	
 	
 	/**
 	 * All draw / paint operations must work with a hidden workingCanvas that
 	 * contains the backbuffer image. Finally, the backbuffer image is displayed.
 	 */
 	@Override
 	public void paint(Graphics g) {
 		Graphics2D canvas = (Graphics2D) g;
 		clearCanvas(canvas);
 		player.draw(canvas);
 	}
 
 	public static void main(String[] args) {
 		new StaticAnimationDemo();
 	}
 }
