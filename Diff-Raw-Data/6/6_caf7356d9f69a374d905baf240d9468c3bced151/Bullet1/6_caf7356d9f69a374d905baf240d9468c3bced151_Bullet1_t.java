 package bullets;
 
 import java.awt.Image;
 
import jgame.ImageCache;
import dtb.Defend;

 public class Bullet1 extends Bullet {
 
 	public Bullet1() {
		super(ImageCache.forClass(Defend.class).get("arrow_fix"));
 		// TODO Auto-generated constructor stub
 	}
 
 }
