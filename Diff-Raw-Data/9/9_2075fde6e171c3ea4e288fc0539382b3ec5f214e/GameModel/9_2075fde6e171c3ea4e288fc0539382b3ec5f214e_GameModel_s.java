 package model;
 
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.Calendar;
 import java.util.List;
 
 import model.items.weapons.Projectile;
 import model.sprites.Player;
 import model.world.Tile;
 import model.world.World;
 import model.world.WorldBuilder;
 
 
 
 /**
  * This is the main model class. This will hold all data.
  * 
  * @author
  *
  */
 public class GameModel implements PropertyChangeListener {
 	
 	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
 	private World world;
	private Player player;
 	private List<Tile> spawnPoints;
 	
 	/**
 	 * The message sent when a new sprite is added.
 	 */
 	public final static String ADDED_SPRITE = "addedsprite";
 	/**
 	 * The message sent when an object is removed.
 	 */
 	public final static String REMOVED_OBJECT = "removeobject";
 	/**
 	 * The message sent when a projectile is added.
 	 */
 	public final static String ADDED_PROJECTILE = "addedprojectile";
 	/**
 	 * The message sent when a food supply is added
 	 */
 	public final static String ADDED_SUPPLY = "addedsupply";
 
 //	/**
 //	 * Creates a new default game model.
 //	 */
 //	public GameModel(){
 //		this(Calendar.getInstance().getTimeInMillis());
 //	}
 //	
 //	/**
 //	 * Creates a new game model with the specified seed.
 //	 * @param seed the seed to use when creating the world.
 //	 */
 //	public GameModel() {
 //		this.seed = seed;
 //		world = new World();
 //		WorldBuilder wb = new WorldBuilder();
 //		world.setTiles(wb.getNewWorld(400, 400));
 //		spawnPoints = wb.getSpawnPoints();
 //		this.world.addListener(this);
 //	}
 	
 	public GameModel(World world) {
 		this.world = world;
 		this.world.addListener(this);
 	}
 	
 	public void setSpawns(List<Tile> spawns) {
 		this.spawnPoints = spawns;
 	}
 	
 	/**
 	 * Gives the seed used when creating the world.
 	 * @return the seed used when creating the world.
 	 */
 //	public long getSeed() {
 //		return this.seed;
 //	}
 	
 	/**
 	 * Sets the player of the game.
 	 * @param player the player of the game.
 	 */
 	public void setPlayer(Player player) {
		world.removeSprite(player);
		this.player = player;
		world.addSprite(player);
 	}
 	
 	/**
 	 * Adds the specified listener.
 	 * @param pcl the new listener.
 	 */
 	public void addListener(PropertyChangeListener pcl) {
 		this.pcs.addPropertyChangeListener(pcl);
 	}
 	
 	/**
 	 * Removes the specified listener.
 	 * @param pcl the listener to remove.
 	 */
 	public void removeListener(PropertyChangeListener pcl) {
 		this.pcs.removePropertyChangeListener(pcl);
 	}
 	
 	/**
 	 * Gives the player this model holds.
 	 * @return the player this model holds.
 	 */
 	public Player getPlayer() {
		return this.player;
 	}
 	
 	/**
 	 * Gives the world this model holds.
 	 * @return the world this model holds.
 	 */
 	public World getWorld() {
 		return this.world;
 	}
 	
 	/**
 	 * Updates the model.
 	 */
 	public void update() {
 		world.update();
 	}
 	
 	/**
 	 * Return the spawnPoints for items.
 	 * @return the spawnPoints for items.
 	 */
 	public List<Tile> getSpawnPoints(){
 		return spawnPoints;
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt) {
 		//Sends the event down
 		this.pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
 	}
 }
