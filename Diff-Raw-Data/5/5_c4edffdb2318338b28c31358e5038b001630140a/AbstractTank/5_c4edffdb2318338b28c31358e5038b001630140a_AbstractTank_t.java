 package chalmers.TDA367.B17.model;
 
 import org.newdawn.slick.geom.*;
 
 public abstract class AbstractTank extends MovableEntity{
 
 	private String name;
 	private double health;
 	private AbstractWeapon currentWeapon;
 	private AbstractPowerUp currentPowerUp;
 	private float turnSpeed; // How many degrees the tank will turn each update
 	private Vector2f turretDirection;
 	
 	public AbstractTank(int id, Vector2f velocity, float maxSpeed, float minSpeed, float reverseSpeed) {
 		super(id, velocity, maxSpeed, minSpeed, reverseSpeed);
 		turnSpeed = 3f;
 		//currentWeapon = Weapons.DEFAULT_WEAPON;
 		// TODO
 	}
 	
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public double getHealth() {
 		return health;
 	}
 
 	public void setHealth(double health) {
 		this.health = health;
 	}
 
 	public AbstractWeapon getCurrentWeapon() {
 		return currentWeapon;
 	}
 
 	public void setCurrentWeapon(AbstractWeapon currentWeapon) {
 		this.currentWeapon = currentWeapon;
 	}
 
 	public AbstractPowerUp getCurrentPowerUp() {
 		return currentPowerUp;
 	}
 
 	public void setCurrentPowerUp(AbstractPowerUp currentPowerUp) {
 		this.currentPowerUp = currentPowerUp;
 	}
 
 	public float getTurnSpeed() {
 		return turnSpeed;
 	}
 
 	public void setTurnSpeed(float turnSpeed) {
 		this.turnSpeed = turnSpeed;
 	}
 
 	public Vector2f getTurretDirection() {
 		return turretDirection;
 	}
 	
 	public void turnLeft(int delta){
		setDirection(getDirection().add(-turnSpeed * delta/60 * (Math.abs(speed)*0.2f + 0.7)));
 	}
 	
 	public void turnRight(int delta){
		setDirection(getDirection().add(turnSpeed * delta/60 * (Math.abs(speed)*0.2f + 0.7)));
 	}
 
 	public void setTurretDirection(Vector2f turretDirection) {
 		this.turretDirection = turretDirection;
 	}
 }
