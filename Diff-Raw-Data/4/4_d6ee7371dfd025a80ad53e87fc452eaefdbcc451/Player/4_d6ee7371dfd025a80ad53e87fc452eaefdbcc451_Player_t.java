 package entities.players;
 
 import java.util.Map;
 
 import map.Cell;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.geom.Rectangle;
 
 import sounds.Sounds;
 import utils.MapLoader;
 
 import entities.Entity;
 import entities.players.abilities.AbilityFinder;
 import entities.players.abilities.IPlayerAbility;
 import game.config.Config;
 
 public class Player extends Entity {
 	
 	private final Animation left, right;
 	private Animation sprite;
 	private final Map<String, IPlayerAbility> abilities = AbilityFinder.initialiseAbilities();
 	private static final Sound SOUND_JUMP;
 	private float speed = 0.3f;
 	
 	static {
 		final String path = "data/sounds/jump.ogg";
 		Sound s = null;
 		try {
 			s = new Sound(path);
 		} catch (SlickException e) {
 			System.out.println("Sound file for " + Player.class.getSimpleName() + " not found or failed to load.");
 			System.out.println("Tried to load from: " + path);
 			e.printStackTrace();
 		}
 		SOUND_JUMP = s;
 	}
 
 	public Player(Rectangle hitbox, int maxhealth) {
 		super(hitbox, maxhealth);
 		Image[] movementRight = null;
 		Image[] movementLeft = null;
 		try {
 			movementRight = new Image[]{new Image("data/images/dvl1_rt1.png"), new Image("data/images/dvl1_rt2.png")};
 			movementLeft = new Image[]{new Image("data/images/dvl1_lf1.png"), new Image("data/images/dvl1_lf2.png")};
 		} catch (SlickException e) {
 			//do shit all
 		}
 		int[] duration = {200,200};
 		right = new Animation(movementRight, duration, false);
 		left = new Animation(movementLeft, duration, false);
 		sprite = right;
 	}
 
 	@Override
 	protected Player clone() {
 		return new Player(new Rectangle(getX(), getY(), getWidth(), getHeight()),getMaxHealth());
 	}
 	
 	/**
 	 * Checks if the player has the ability, if the player does the ability is 'used'. 
 	 * @param key The name of the ability (equal to the name of the ability class without the "Ability.java" bit on the end.<br />
 	 * For example, DoubleJumpAbility.java would be referenced to be calling useAbility("DoubleJump").<br />
 	 * The key is not case sensitive.
 	 */
 	public void useAbility(String key)
 	{
 		//lookup the key in the allowed abilities
 		//if the key is in the map then ability.use(this)
 		IPlayerAbility tempability = abilities.get(key.toLowerCase());
 		if(tempability != null)
 		{
 			tempability.use(this);
 		}
 		
 	}
 	
 	private void playerJump() {
 		useAbility("doublejump");
 		if (isOnGround()) {
 			super.jump();
//			SOUND_JUMP.play();
			Sounds.play(SOUND_JUMP);
 		}
 	}
 	
 	@Override
 	public void stop_sounds(){
 		super.stop_sounds();
 		SOUND_JUMP.stop();
 		for(IPlayerAbility ability : abilities.values()){
 			ability.stop_sounds();
 		}
 	}
 
 	/**
 	 * This is the only method that needs to be called to update the player.
 	 * Input is assumed to already have been checked for being pressed
 	 * the previous frame.
 	 * @param input The keyboard input at the time of this frame.
 	 * @param delta The time in microseconds since the last update.
 	 */
 	@Override
 	public void update(Input input) {
 		
 		if (input.isKeyPressed(Input.KEY_SPACE)) {
 			playerJump();
 		}
 		if (input.isKeyDown(Input.KEY_A)) {
 			moveX(-speed);
 			sprite = left;
 			sprite.update(DELTA);
 		}
 		else if (input.isKeyDown(Input.KEY_D)) {
 			moveX(speed);
 			sprite = right;
 			sprite.update(DELTA);
 		}
 		
 		frameMove();
 		checkMapChanged();
 	}
 	
 	/**
 	 * Checks the player's x and y position to see if they have reached the edge of the map. 
 	 * @return -1 for no change, 0 for up, 1 for right, 2 for down, 3 for left
 	 */
 	public void checkMapChanged() {
 		Cell currentCell = MapLoader.getCurrentCell();
 		//check top
 		if (getY() < 1 && getdY() < 0) {
 			currentCell = MapLoader.setCurrentCell(this,MapLoader.getCurrentX(), MapLoader.getCurrentY() - 1);
 			setPosition(getX(), currentCell.getHeight() - getHeight() - 1);
 		}
 		//right
 		if (getX() >= currentCell.getWidth() - 2 && getdX() > 0) {
 			currentCell = MapLoader.setCurrentCell(this,MapLoader.getCurrentX() + 1, MapLoader.getCurrentY());
 			setPosition(1, getY());
 		}
 		//bottom
 		if (getY() >= currentCell.getHeight() - 2 && getdY() > 0) {
 			currentCell = MapLoader.setCurrentCell(this,MapLoader.getCurrentX(), MapLoader.getCurrentY() + 1);
 			setPosition(getX(), 1);
 		}
 		//left
 		if (getX() < 1 && getdX() < 0) {
 			currentCell = MapLoader.setCurrentCell(this,MapLoader.getCurrentX() - 1, MapLoader.getCurrentY());
 			setPosition(currentCell.getWidth() - getWidth() - 1, getY());
 		}
 	}
 	
 	@Override
 	public void render() {
 		sprite.draw((int)((getX()-1)*Config.getTileSize()), (int)((getY()-1)*Config.getTileSize()), new Color(255,255,255));
 	}
 }
