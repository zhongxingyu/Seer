 package Items;
 
 import java.awt.image.BufferedImage;
 
 import Core.Player;
 
 public class Shrooms extends Item implements PickUpItem {
 
 	public Shrooms(BufferedImage image) {
 		super(image);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void playerConsume(Player p) {
		p.getHigh();
 	}
 
 }
