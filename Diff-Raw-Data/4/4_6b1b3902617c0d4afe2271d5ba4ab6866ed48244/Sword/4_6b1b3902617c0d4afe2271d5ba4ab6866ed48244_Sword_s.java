 package weapons;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 
 public class Sword extends Weapon {
 
 	public Sword(float x, float y) {
 		super();
 		this.x = x;
 		this.y = y;
 		damage = 5;
 		attackWidth = 100;
 		attackHeight = 6;
 		attackTime = 1000;
 		delayTime = 500;
 	}
 	
 	@Override
 	public void init() throws SlickException {
 		super.init();
 		//weaponSheet = new SpriteSheet("Assets/Weapons/Sword/sheet.png", 64, 64);
 		//defaultSprite = weaponSheet.getSprite(0, 0);		
 		//initAnimations();
 		groundSprite = new Image("Assets/Weapons/Sword/sword.png");
 	}
 	
 	@Override
 	public void attack() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	protected boolean updateAttack(Attack attack) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
