 package rsmg.controller;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.Renderable;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.SpriteSheet;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.state.transition.FadeInTransition;
 
 import rsmg.io.IO;
 import rsmg.model.Level;
 import rsmg.model.ObjectName;
 import rsmg.model.TileGrid;
 import rsmg.model.object.bullet.Bullet;
 import rsmg.model.object.item.Item;
 import rsmg.model.object.unit.Enemy;
 import rsmg.model.object.unit.PCharacter;
 
 /**
  * The state where the levels are played out.
  * @author Daniel Jonsson
  *
  */
 class LevelState extends State {
 
 	
 	/**
 	 * The background image behind the tile grid.
 	 */
 	private Image background;
 	
 	/**
 	 * The health bar images.
 	 */
 	private Image healthBar;
 	private Rectangle healthBarOverlayRectangle;
 	private Graphics healthBarOverlayGraphics;
 	
 	/**
 	 * Maps containing images.
 	 */
 	private Map<String, Renderable> tiles;
 	private Map<String, Renderable> bullets;
 	private Map<String, Renderable> items;
 	private Map<String, Renderable> enemies;
 	
 	/**
 	 * The character that the player controls.
 	 */
 	private Renderable character;
 	private Animation characterRunningR;
 	private Animation characterRunningL;
 	private Image characterStandingR;
 	private Image characterStandingL;
 	private Image characterJumpingR;
 	private Image characterJumpingL;
 	private Image characterDashingR;
 	private Image characterDashingL;
 	
 	/**
 	 * Reference to the level model.
 	 */
 	private Level level;
 	
 	/**
 	 * Track if the up key is down or not.
 	 */
 	private boolean upKeyIsDown;
 	
 	/**
 	 * Store how much everything should be scaled in the view.
 	 */
 	private int scale;
 	
 	/**
 	 * Construct the level.
 	 * @param stateID The ID to the state.
 	 */
 	LevelState(int stateID) {
 		super(stateID);
 	}
 	
 	/**
 	 * Initialize level images and the level model.
 	 */
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg)
 			throws SlickException {
 
 		scale = (int) ((float)gc.getWidth() / 480);
 		int filter = Image.FILTER_NEAREST;
 		String folderPath = "res/sprites/level/";
 		
 		/**
 		 * The background image in the level.
 		 */
 		background = new Image(folderPath+"bg.jpg", false, filter).getScaledCopy(scale);
 		
 		healthBar = new Image(folderPath+"healthBar.png", false, filter);
		healthBarOverlayRectangle = new Rectangle(23, 23, 147, 17);
 		healthBarOverlayGraphics = new Graphics();
 		healthBarOverlayGraphics.setColor(new Color(0.85f, 0.3f, 0.3f, 0.5f));
 		
 		/**
 		 * Make an animation for when the character is running to the right.
 		 */
 		Image characterImage = new Image(folderPath+"charPistolRunningSheet.png", false, filter);
 		SpriteSheet characterSheet = new SpriteSheet(characterImage.getScaledCopy(scale), 32*scale, 23*scale);
 		characterRunningR = new Animation(characterSheet, 140);
 		
 		/**
 		 * Make an animation for when the character is running to the left.
 		 */
 		characterImage = characterImage.getFlippedCopy(true, false);
 		characterSheet = new SpriteSheet(characterImage.getScaledCopy(scale), 32*scale, 23*scale);
 		characterRunningL = new Animation(characterSheet, 140);
 		
 		/**
 		 * Make an image for when the character is standing still facing the right.
 		 */
 		characterStandingR = new Image(folderPath+"charPistolStanding.png", false, filter).getScaledCopy(scale);
 		
 		/**
 		 * Make an image for when the character is standing still facing to the left.
 		 */
 		characterStandingL = characterStandingR.getFlippedCopy(true, false);
 		
 		/**
 		 * Make an image for when the character is standing still facing the right.
 		 */
 		character = characterJumpingR = new Image(folderPath+"charPistolJumping.png", false, filter).getScaledCopy(scale);
 		
 		/**
 		 * Make an image for when the character is standing still facing to the left.
 		 */
 		characterJumpingL = characterJumpingR.getFlippedCopy(true, false);
 		
 		/**
 		 * Make an image for when the character is dashing to the right
 		 */
 		characterDashingR = new Image(folderPath+"charDashing.png", false, filter).getScaledCopy(scale);
 		
 		/**
 		 * Make an image for when the character is dashing to the right
 		 */
 		characterDashingL = characterDashingR.getFlippedCopy(true, false);
 		
 		/**
 		 * Create a map with all enemy images.
 		 */
 		enemies = new HashMap<String, Renderable>();
 		Image tankbot = new Image(folderPath+"tankbot.png", false, filter).getScaledCopy(scale);
 		enemies.put(ObjectName.TANKBOT, tankbot);
 		
 		/**
 		 * Create a map with all item images.
 		 */
 		items = new HashMap<String, Renderable>();
 		Image healthPack = new Image(folderPath+"healthPack.png", false, filter).getScaledCopy(scale);	
 		Image laserPistol = new Image(folderPath+"laserPistol.png", false, filter).getScaledCopy(scale);
 		items.put(ObjectName.HEALTH_PACK, healthPack);
 		items.put(ObjectName.LASER_PISTOL, laserPistol);
 		
 		/**
 		 * Create a map with all bullet images.
 		 */
 		bullets = new HashMap<String, Renderable>();
 		Image laserBullet = new Image(folderPath+"laserBullet.png", false, filter).getScaledCopy(scale);
 		bullets.put(ObjectName.LASER_BULLET, laserBullet);
 		
 		/**
 		 * create an animation for the rocket
 		 */
 		Image rocketImage = new Image(folderPath+"rocketSheet.png", false, filter);
 		SpriteSheet rocketSheet = new SpriteSheet(rocketImage.getScaledCopy(scale), 15*scale, 14*scale);
 		Animation rocket = new Animation(rocketSheet, 140);
 		bullets.put("rocket", rocket);
 		
 		/**
 		 * create an animation for explosions
 		 */
 		Image explosionImage = new Image(folderPath+"explosion.png", false, filter);
 		SpriteSheet explosionSheet = new SpriteSheet(explosionImage.getScaledCopy(scale), 30*scale, 30*scale);
 		Animation explosion = new Animation(explosionSheet,140);
 		bullets.put("explosion", explosion);
 		
 		/**
 		 * Create a map with all tile images.
 		 */
 		tiles = new HashMap<String, Renderable>();
 		Image boxTile = new Image(folderPath+"boxTile.png", false, filter).getScaledCopy(scale);
 		Image airTile = new Image(folderPath+"airTile.png", false, filter).getScaledCopy(scale);
 		tiles.put(ObjectName.BOX_TILE, boxTile);
 		tiles.put(ObjectName.AIR_TILE, airTile);
 		
 	}
 	
 	/**
 	 * Initialize a level.
 	 * @param levelNumber The level's number.
 	 */
 	public void initLevel(int levelNumber) {
 		IO io = new IO();
 		level = new Level(new TileGrid(io.getLevel(levelNumber)), io.getItemList(), io.getEnemyList());
 	}
 
 	@Override
 	public void enter(GameContainer gc, StateBasedGame sbg)
 			throws SlickException {
 		super.enter(gc, sbg);
 	}
 
 	@Override
 	public void leave(GameContainer gc, StateBasedGame sbg)
 			throws SlickException {
 		super.leave(gc, sbg);
 	}
 
 	/**
 	 * Draw everything from the game model on the screen.
 	 */
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
 			throws SlickException {
 
 		drawBackground();
 		drawEnvironment();
 		drawCharacter();
 		drawBullets();
 		drawEnemies();
 		drawItems();
 		drawHealthBar();
 	}
 
 	/**
 	 * Draw a background image behind the tile grid.
 	 */
 	private void drawBackground() {
 		background.draw(0, 0);
 	}
 	
 	/**
 	 * Draw bullets on the screen.
 	 */
 	private void drawBullets() {
 		for (Bullet bullet : level.getBulletList())
 			bullets.get(bullet.getName()).draw((float)bullet.getX()*scale, (float)bullet.getY()*scale);
 	}
 	
 	/**
 	 * Draw enemies on the screen.
 	 */
 	private void drawEnemies() {
 		for (Enemy enemy : level.getEnemies())
 			enemies.get(enemy.getName()).draw((float)enemy.getX()*scale, (float)enemy.getY()*scale);
 	}
 	
 	/**
 	 * Draw items on the screen.
 	 */
 	private void drawItems() {
 		for(Item item : level.getItemList())
 			items.get(item.getName()).draw((float)item.getX()*scale, (float)item.getY()*scale);
 	}
 	
 	/**
 	 * Draw the environment which consists of the tiles.
 	 */
 	private void drawEnvironment() {
 		for (int y = 0; y < level.getTileGrid().getHeight(); y++)
 			for (int x = 0; x < level.getTileGrid().getWidth(); x++)
 				tiles.get(level.getTileGrid().getFromCoord(x, y).getName()).draw(x*32*scale, y*32*scale);
 	}
 
 	/**
 	 * Draw the character/protagonist on the screen.
 	 */
 	private void drawCharacter() {
 		character.draw(((float)level.getCharacter().getX()-6)*scale, (float)level.getCharacter().getY()*scale);
 	}
 	
 	/**
 	 * Draw the health bar.
 	 */
 	private void drawHealthBar() {
 		healthBar.draw(20, 20);
 		healthBarOverlayGraphics.fill(healthBarOverlayRectangle);
 	}
 
 	/**
 	 * Handle inputs from the user and update the model.
 	 */
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta)
 			throws SlickException {
 		
 		handleKeyboardEvents(gc.getInput(), sbg);
 		
 		/**
 		 * Update the model and give it the time that has passed since last
 		 * update as seconds.
 		 */
 		level.update((double)delta / 1000);
 		
 		/**
 		 * Fix so the right character sprite is shown on the next render()
 		 */
 		if(level.getCharacter().isDashing()) {
 			if(level.getCharacter().isFacingRight()) {
 				character = characterDashingR;
 			} else {
 				character = characterDashingL;
 			}
 			
 		} else {
 			if (level.getCharacter().isAirborne()) {
 		
 				if (level.getCharacter().isFacingRight())
 					character = characterJumpingR;
 				else
 					character = characterJumpingL;
 		
 			} else if (level.getCharacter().isStandingStill()) {
 		
 				if (level.getCharacter().isFacingRight())
 					character = characterStandingR;
 				else
 					character = characterStandingL;
 		
 			} else { // is running
 		
 				if (level.getCharacter().isFacingRight())
 					character = characterRunningR;
 				else
 					character = characterRunningL;
 		
 			}
 			
 			/**
 			 * Update health bar's overlay size.
 			 */
			healthBarOverlayRectangle.setWidth(147 * level.getCharacter().getHealth() / level.getCharacter().getMaxHealth());
 		}
 		
 		/**
 		 * Play a sound if the character has fired his weapon.
 		 */
 		if (level.getCharacter().getWeapon().shot())
 			new Sound("res/sounds/shot.wav").play();
 	}
 	
 	/**
 	 * Handle keyboard events.
 	 * @param input
 	 */
 	public void handleKeyboardEvents(Input input, StateBasedGame sbg) {
 		
 		PCharacter modelCharacter = level.getCharacter();
 		
 		if (input.isKeyDown(Input.KEY_LEFT))
 			if(!modelCharacter.isDashing())
 				modelCharacter.moveLeft();
 		
 		if (input.isKeyDown(Input.KEY_RIGHT))
 			if(!modelCharacter.isDashing())
 				modelCharacter.moveRight();
 
 		if (input.isKeyDown(Input.KEY_UP)) {
 			if (!upKeyIsDown)
 				modelCharacter.jump();
 			
 			upKeyIsDown = true;
 		} else if (upKeyIsReleased())
 			modelCharacter.jumpReleased();
 
 		if (input.isKeyPressed(Input.KEY_SPACE))
 			modelCharacter.attack();
 		
 		if (input.isKeyPressed(Input.KEY_ESCAPE)) {
 			sbg.enterState(Controller.PAUSE_MENU_STATE, null, new FadeInTransition());
 		}
 		if (input.isKeyPressed(Input.KEY_X)) {
 			if (modelCharacter.canDash()) {
 				modelCharacter.setDashing(true);
 			}
 		}
 	}
 
 	/**
 	 * Returns if the up key has been released since last loop.
 	 * @return If the up key has been released.
 	 */
 	private boolean upKeyIsReleased() {
 		if (upKeyIsDown) {
 			upKeyIsDown = false;
 			return true;
 		}
 		return false;
 	}
 }
