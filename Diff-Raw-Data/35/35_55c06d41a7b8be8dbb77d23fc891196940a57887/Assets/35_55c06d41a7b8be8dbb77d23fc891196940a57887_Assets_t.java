 package util;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 public class Assets {
 
 	public static Image TILE_ONE;
 	public static Image ROCK;
 	public static Image FENCE;
 
 	public static Image CREATURE;
 
 	public static Image PLAYER;
 	public static Image IRISH_DRESS;
 
 	public static Image TREE;
 	public static Image TORCH;
 
 	public static Image SHADOW;
 
 	public static Image BULLET;
 	public static Image EXPLOSION;
 
 	public static Image MINECRAFT;
 
 	public static Image HEALTH_BAR;
 	public static Image HEALTH_BAR_BASE;
 
 	public Assets() throws SlickException {
		TILE_ONE = new Image("data/grass.png");
		ROCK = new Image("data/new rock.png");
 
		CREATURE = new Image("data/creature.png");
 
		PLAYER = new Image("data/overlap test.png");
 
		FENCE = new Image("data/fence.png");
 
		TREE = new Image("data/Tree no shadow.png");
 
		SHADOW = new Image("data/shadow.png");
		BULLET = new Image("data/Bullet.png");
		EXPLOSION = new Image("data/splosion.png");
		MINECRAFT = new Image("data/minecraft.png");
 
		TORCH = new Image("data/torch.png");
		IRISH_DRESS = new Image("data/irishdress.png");
 
		HEALTH_BAR = new Image("data/healthBar.png");
		HEALTH_BAR_BASE = new Image("data/healthBarBase.png");
 	}
 }
