 package se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 
 /**
  * This view renders a robot.
  * 
  * @author 
  *
  */
 public class RobotView extends Image  {
 
 	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
 
 	/**
 	 * The number of lives the robot starts with.
 	 */
 	public static final int MAX_LIVES = 3;
 	/**
 	 * The maximum number of damage tokens a robot can have.
 	 */
 	public static final int MAX_DAMAGE = 9;
 	/**
 	 * When the number of damage tokens change.
 	 */
 	public static final String EVENT_DAMAGE_CHANGE = "damageChange";
 	/**
 	 * When the number of lives change.
 	 */
 	public static final String EVENT_LIFE_CHANGE = "lifeChange";
 	/**
 	 * When the player reaches a new checkpoint.
 	 */
 	public static final String EVENT_CHECKPOINT_CHANGE = "checkPointChange";
 
 	private int damage = 0, lives = MAX_LIVES;
 	private final int robotID;
 	private final LaserView laser;
 	private boolean hasFinished = false;
 	private int reachedCheckPoint = 0;
 	private final String name;
 	private boolean isDead = false;
 
 	/**
 	 * Creates a new instance of a robot with the specified ID-number.
 	 * @param robotID The ID-number of this robot.
 	 * @param texture The texture to use.
 	 * @param laser The laser this robot should use.
 	 * @param name The name of the robot.
 	 */
 	public RobotView(int robotID, TextureRegion texture, LaserView laser, String name) {
 		super(texture);
 		this.setSize(40, 40);
 		this.robotID = robotID;
 		this.laser = laser;
 		this.name = name;
 		laser.setIsOuter(true);
 	}
 	
 	/**
 	 * Gives the laser this robot has.
 	 * @return This robot's laser.
 	 */
 	public LaserView getLaser() {
 		laser.setSize(40, 40);
 		laser.setPosition(this.getX(), this.getY());
 		laser.setOrigin(20, 20);
 		laser.setRotation(getRotation());
 		return laser;
 	}
 	
 	public void setDead(boolean isDead) {
 		this.isDead = isDead;
 		setVisible(!isDead);
		System.out.println("Robot " + robotID + ", visible: " + !isDead);
 	}
 	
 	public boolean isDead() {
 		return this.isDead;
 	}
 	
 	public String getName() {
 		return this.name;
 	}
 	
 	public int getReachedCheckPoint() {
 		return this.reachedCheckPoint;
 	}
 	
 	public void setReachedCheckPoint(int checkPoint) {
 		pcs.firePropertyChange(EVENT_CHECKPOINT_CHANGE, this.reachedCheckPoint, checkPoint);
 		this.reachedCheckPoint = checkPoint;
 	}
 	
 	public boolean hasFinished() {
 		return hasFinished;
 	}
 	
 	public boolean isGameDead() {
 		return lives <= 0;
 	}
 	
 	public void setHasFinished(boolean hasFinished) {
 		this.hasFinished = hasFinished;
 	}
 	
 	/**
 	 * Gives the number of lives the robot has left.
 	 * @return The number of lives the robot has left.
 	 */
 	public int getLives() {
 		return this.lives;
 	}
 	
 	/**
 	 * Sets how many lives the robot has left.
 	 * @param lives The number of lives the robot has left.
 	 */
 	public void setLives(int lives) {
 		pcs.firePropertyChange(EVENT_LIFE_CHANGE, this.lives, lives);
 		this.lives = lives;
 	}
 	
 	/**
 	 * Gives the number of damage tokens this robot has.
 	 * @return The number of damage tokens this robot has.
 	 */
 	public int getDamage() {
 		return this.damage;
 	}
 	
 	/**
 	 * Sets how many damage tokens this robot has.
 	 * @param damage The number of tokens this robot has.
 	 */
 	public void setDamage(int damage) {
 		pcs.firePropertyChange(EVENT_DAMAGE_CHANGE, this.damage, damage);
 		this.damage = damage;
 	}
 	
 	/**
 	 * Gives the ID-number of the robot.
 	 * @return The ID-number of the robot.
 	 */
 	public int getRobotID() {
 		return robotID;
 	}
 	
 	/**
 	 * Adds the specified listener.
 	 * @param listener The listener to add.
 	 */
 	public void addListener(PropertyChangeListener listener) {
 		pcs.addPropertyChangeListener(listener);
 	}
 	
 	/**
 	 * Removes the specified listener.
 	 * @param listener The listener to remove.
 	 */
 	public void removeListener(PropertyChangeListener listener) {
 		pcs.removePropertyChangeListener(listener);
 	}
 }
