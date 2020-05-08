 package se.chalmers.kangaroo.view;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Toolkit;
 /**
  * A class representing the animation of the Kangaroo.
  * @author simonal
  *
  */
 public class KangarooAnimation implements Animation {
 
 	private Image rightSheet;
 	private Image leftSheet;
 	private int widthPerFrame;
 	private int height;
 	private int tick;
 	private int currentFrame;
 	/**
 	 * Constructor taking the pathname and width and height of the animation.
 	 * @param pathName
 	 * @param width
 	 * @param height
 	 */
 	public KangarooAnimation(int width, int height) {
 		rightSheet = Toolkit.getDefaultToolkit().getImage("resources/sheets/kangaroo_58x64_right.png");
 		leftSheet = Toolkit.getDefaultToolkit().getImage("resources/sheets/kangaroo_58x64_right.png");
 		currentFrame = 0;
 		this.widthPerFrame = width;
 		this.height = height;
 
 		tick = 0;
 	}
 	/**
 	 * Draws the animation once every halv second if the game is running in 60 ups.
 	 */
 	@Override
 	public void drawSprite(Graphics g, int x, int y) {
		if (tick == 30) {
 			tick = 0;
			currentFrame = (currentFrame % 3) + 1;
 		}
 		g.drawImage(leftSheet, x-widthPerFrame/2, y-height, x+widthPerFrame/2, y,
 				(currentFrame * widthPerFrame), 1,
 				(currentFrame * widthPerFrame) + widthPerFrame,
 				height, null, null);
 		tick++;
 
 	}
 
 }
