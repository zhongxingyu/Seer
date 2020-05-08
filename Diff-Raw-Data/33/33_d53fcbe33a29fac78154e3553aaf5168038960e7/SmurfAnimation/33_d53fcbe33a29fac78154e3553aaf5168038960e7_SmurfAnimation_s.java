 package se.chalmers.kangaroo.view;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Toolkit;
 
 import se.chalmers.kangaroo.model.creatures.SmurfCreature;
 
 public class SmurfAnimation implements Animation {
 	
 	private Image leftSheet;
 	private Image rightSheet;
 	
 	private int width;
	private int heigh;
 	
 	private SmurfCreature sc;
 	
 	public SmurfAnimation(SmurfCreature sc) {
 		leftSheet = Toolkit.getDefaultToolkit().getImage("resources/sheets/smurf_32x32_left.png");
 		rightSheet = Toolkit.getDefaultToolkit().getImage("resources/sheets/smurf_32x32_right.png");
 		this.width = 32;
		this.heigh = 32;
 		this.sc = sc;
 	}
 
 	@Override
 	public void drawSprite(Graphics g, int x, int y) {
 		
 		
 	}
 
 }
