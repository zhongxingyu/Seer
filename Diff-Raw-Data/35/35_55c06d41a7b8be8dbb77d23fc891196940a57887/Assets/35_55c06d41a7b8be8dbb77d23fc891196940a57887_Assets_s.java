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
		TILE_ONE = new Image("bin/data/grass.png");
		ROCK = new Image("bin/data/new rock.png");
 
		CREATURE = new Image("bin/data/creature.png");
 
		PLAYER = new Image("bin/data/overlap test.png");
 
		FENCE = new Image("bin/data/fence.png");
 
		TREE = new Image("bin/data/Tree no shadow.png");
 
		SHADOW = new Image("bin/data/shadow.png");
		BULLET = new Image("bin/data/Bullet.png");
		EXPLOSION = new Image("bin/data/splosion.png");
		MINECRAFT = new Image("bin/data/minecraft.png");
 
		TORCH = new Image("bin/data/torch.png");
		IRISH_DRESS = new Image("bin/data/irishdress.png");
 
		HEALTH_BAR = new Image("bin/data/healthBar.png");
		HEALTH_BAR_BASE = new Image("bin/data/healthBarBase.png");
 	}
 }
