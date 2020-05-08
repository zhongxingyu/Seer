 package Items;
 
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 
 import Core.Canvas;
 import Core.Player;
 
 public class Vodka extends Item implements PickUpItem {
 
 	public Vodka(BufferedImage image) {
 		super(image);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void playerConsume(Player p) {
 		// TODO Auto-generated method stub
 		//randomize UP, Left keys
 		int setUP = p.getCanvas().getPressUP();
 		int setDown = p.getCanvas().getPressDown();
 		double rand = Math.random();
		if (rand > 0.4) {
 			p.getCanvas().setPressLeft(setDown);
 			p.getCanvas().setPressRight(setUP);
 		}
 				
 				int amount = 20;
 				//increment score
 				Player.score += amount;
 				//check if the minimum sobriety is not reached if so set to 0
 				if(Player.sobriety - amount <= Player.MIN_SOBRIETY){
 					Player.sobriety = 0;
 					Player.blackOut();
 				}
 				//minus 10 off sobriety
 				else{
 					Player.sobriety -= amount;
 					Player.blackOut();
 				}
 				
 				// TODO BLUR
 		
 	}
 
 
 }
