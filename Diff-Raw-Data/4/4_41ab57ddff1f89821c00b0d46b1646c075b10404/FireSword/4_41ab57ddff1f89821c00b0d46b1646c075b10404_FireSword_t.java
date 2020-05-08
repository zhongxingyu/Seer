 public class FireSword extends Equipment {
 	private double hitbox_xdiff = -0.2;
 	private double hitbox_ydiff = 0.5;
 	private double hitbox_width = 0.4;
 	private double hitbox_height = 1;
 	private double hitbox_depth = 1;
 	
 	public FireSword() {
 		setItemID(1001);
		setItemName("firesword");
 		
 		setStat_Dmg(10);
 		setStat_MagicDmg(0);
 		setStat_atkSpeed(0.75);
 		
 		setStat_Type(1);
 		
 		setHitbox_xdiff(hitbox_xdiff);
 		setHitbox_ydiff(hitbox_ydiff);
 		setHitbox_width(hitbox_width);
 		setHitbox_height(hitbox_height);
 		setHitbox_depth(hitbox_depth);
 	}
 }
