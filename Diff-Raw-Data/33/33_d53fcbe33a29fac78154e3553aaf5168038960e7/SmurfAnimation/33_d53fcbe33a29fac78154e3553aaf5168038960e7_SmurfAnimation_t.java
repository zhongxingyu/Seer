 package se.chalmers.kangaroo.view;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Toolkit;
 
 import se.chalmers.kangaroo.model.creatures.SmurfCreature;
import se.chalmers.kangaroo.model.utils.Direction;
 
 public class SmurfAnimation implements Animation {
 	
 	private Image leftSheet;
 	private Image rightSheet;
 	
	private int tick;
	private int currentSprite;
	
 	private int width;
	private int height;
 	
 	private SmurfCreature sc;
 	
 	public SmurfAnimation(SmurfCreature sc) {
 		leftSheet = Toolkit.getDefaultToolkit().getImage("resources/sheets/smurf_32x32_left.png");
 		rightSheet = Toolkit.getDefaultToolkit().getImage("resources/sheets/smurf_32x32_right.png");
 		this.width = 32;
		this.height = 32;
 		this.sc = sc;
 	}
 
 	@Override
 	public void drawSprite(Graphics g, int x, int y) {
		if(tick == 10) {
			tick = 0;
			currentSprite++;
			currentSprite = currentSprite % 3;
		}
 		
		if (sc.getDirection() == Direction.DIRECTION_EAST) {
			g.drawImage(rightSheet, x, y, x+width, y+height, currentSprite*32, 0, currentSprite*32+width, height, null, null);
		}
		if (sc.getDirection() == Direction.DIRECTION_WEST) {
			g.drawImage(leftSheet, x, y, x+width, y+height, currentSprite*32, 0, currentSprite*32+width, height, null, null);
		}
 		
 	}
 
 }
