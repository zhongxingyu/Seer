 package weapons;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Shape;
 import org.newdawn.slick.state.StateBasedGame;
 
 import projectiles.Bolt;
 import projectiles.Projectile;
 
 import dudes.Player;
 
 public class Wizard extends RangeWeapon {
 
 	public Animation lightning;
 	
 	public Wizard(float x, float y) {
 		super(x, y);
 		this.x = x;
 		this.y = y;
 		damage = 12;
 		attackWidth = 75;
 		attackHeight = 75;
 		delayTime = 500;
 		spriteSizeX = 64;
 		spriteSizeY = 64;
 		playerSizeX = 64;
 		playerSizeY = 64;
 		offsetX = 0;
 		attackOffsetY = -50;
 		attackOffsetX = 70;
 		
 		name = "Wizard";
 	}
 	
 	@Override
 	public void init() throws SlickException {
 		SpriteSheet ln = new SpriteSheet("Assets/Weapons/Wand/lightning.png", 100, 500);
 		lightning = new Animation(ln, 0, 0, 9, 0, true, 40, true);
 		weaponSheet = new SpriteSheet("Assets/Weapons/Wand/player" + ((Player)owner).playerID + "Wizard.png", spriteSizeX, spriteSizeY);
 		defaultSprite[0] = weaponSheet.getSprite(0, 5);
 		defaultSprite[1] = weaponSheet.getSprite(0, 4);
 		initAnimations();
 	}
 	
 	@Override
 	public void attack() throws SlickException {
 		super.attack();
		System.out.println("hi");
 		lightning.restart();
 	}
 	
 	@Override
 	public void createGroundSprite() throws SlickException {
 		groundSprite = new Image("Assets/Weapons/Wand/groundWand.png");
 	}
 	
 	@Override
 	public void initAnimations() {
 		// flinch left
 		anims[0] = new Animation(weaponSheet, 0, 0, 3, 0, true, 500, true);
 		anims[0].setLooping(false);
 		// flinch right
 		anims[1] = new Animation(weaponSheet, 0, 1, 3, 1, true, 500, true);
 		anims[1].setLooping(false);
 
 		// punch left
 		anims[2] = new Animation(weaponSheet, 0, 2, 3, 2, true, 100, true);
 		anims[2].setLooping(false);
 		// punch right
 		anims[3] = new Animation(weaponSheet, 0, 3, 3, 3, true, 100, true);
 		anims[3].setLooping(false);
 
 		// walk left
 		anims[4] = new Animation(weaponSheet, 0, 4, 3, 4, true, 80, true);
 		// walk right
 		anims[5] = new Animation(weaponSheet, 0, 5, 3, 5, true, 80, true);
 	}
 	
 	public void render(GameContainer container, StateBasedGame game, Graphics g) {
 		if (owner.isAttacking) {
 			if (owner.isRight) {
 				lightning.draw(owner.pos[0] + 75, owner.pos[1] - 450);
 			} else {
 				lightning.draw(owner.pos[0] - spriteSizeX - 75, owner.pos[1] - 450);
 			}
 		}
 	}
 }
