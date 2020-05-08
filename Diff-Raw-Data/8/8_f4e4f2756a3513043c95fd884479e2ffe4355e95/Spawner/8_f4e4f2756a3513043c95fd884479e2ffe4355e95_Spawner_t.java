 package chalmers.TDA367.B17.spawnpoints;
 
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Vector2f;
 
 import chalmers.TDA367.B17.controller.GameController;
 import chalmers.TDA367.B17.model.AbstractPowerUp;
 import chalmers.TDA367.B17.model.AbstractSpawnPoint;
 import chalmers.TDA367.B17.model.AbstractTank;
 import chalmers.TDA367.B17.model.Entity;
 import chalmers.TDA367.B17.powerups.PowerUpFactory;
 import chalmers.TDA367.B17.weaponPickups.AbstractWeaponPickup;
 import chalmers.TDA367.B17.weaponPickups.WeaponFactory;
 
 public class Spawner {
 	
 	//The time between each powerup spawn.
	private int powerupSpawnTime = 10000;
 
 	//The time between each weapon spawn.
	private int weaponSpawnTime = 15000;
 	
 	private int powerupTimer;
 	private int weaponTimer;
 	
 	//Keeps track of how many powerups there are on the map.
 	private int powerupCount = 0;
 
 	//Keeps track of how many weapons there are on the map.
 	private int weaponCount = 0;
 
 	/**
 	 * Create a new Spawner.
 	 */
 	public Spawner() {
 		powerupTimer = powerupSpawnTime;
 		weaponTimer = weaponSpawnTime;
 	}
 	
 	/**
 	 * Used for updating the TankSpawner object.
 	 * @param delta The time that has passed since the last update
 	 */
 	public void update(int delta){
 		if(!GameController.getInstance().getGameConditions().isGameOver()){
 			powerupTimer -= delta;
 			weaponTimer -= delta;
 			if(powerupSpawnTime > 0 && powerupCount < GameController.getInstance().getGameConditions().getPowerupLimit()){
 				if(powerupTimer <= 0){
 					powerupTimer = powerupSpawnTime;
 					Vector2f p = null;
 					while(p == null){
 						p = generateSpawnPoint();
 					}
 					PowerUpFactory.getRandomPowerUp(p);
 				}
 			}
 			
 	
			if(weaponSpawnTime > 0 && weaponCount < GameController.getInstance().getGameConditions().getWeaponLimit()){
 				if(weaponTimer <= 0){
 					weaponTimer = weaponSpawnTime;
 					Vector2f p = null;
 					while(p == null){
 						p = generateSpawnPoint();
 					}
 					WeaponFactory.getRandomWeaponPickup(p);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Generate a new random spawn point that isn't blocked by an entity.
 	 * @return A random spawn point
 	 */
 	public Vector2f generateSpawnPoint(){
 		final Vector2f spawnpoint;
 		int width = GameController.getInstance().SCREEN_WIDTH;
 		int height = GameController.getInstance().SCREEN_HEIGHT;
 		
 		int x = (int)(Math.random()*width);
 		int y = (int)(Math.random()*height);
 		
 		if(x < 35)
 			x += 35;
 		if(x > width-35)
 			x -= 35;
 		if(y < 35)
 			y += 35;
 		if(y > height-35)
 			y -= 35;
 			
 		spawnpoint = new Vector2f(x, y);
 		
 		//Dummy class for checking collision
 		class Dummy extends Entity{
 			
 			boolean blocked = false;
 			
 			public Dummy(){
 				Vector2f size = new Vector2f(75f, 75f);
 				setPosition(spawnpoint);
 				setShape(new Rectangle(getPosition().x-size.getX()/2, getPosition().y-size.getY()/2, size.getX(), size.getY()));
 			}
 			
 			public void didCollideWith(Entity entity){
 				if(entity instanceof AbstractSpawnPoint || entity instanceof AbstractTank 
 						|| entity instanceof AbstractPowerUp || entity instanceof AbstractWeaponPickup){
 					blocked = true;
 				}
 			}
 		}
 		
 		Dummy d = new Dummy();
 		GameController.getInstance().getWorld().checkCollisionsFor(d);
 		
 		//Returns the generated point if it's not blocked, otherwise null.
 		if(!d.blocked){
 			return spawnpoint;
 		}else{
 			return null;
 		}
 	}
 
 	/**
 	 * Get the powerup count.
 	 * @return The powerup count
 	 */
 	public int getPowerupCount() {
 		return powerupCount;
 	}
 
 	/**
 	 * Set the powerup count.
 	 * @param powerupCount The new powerup count
 	 */
 	public void setPowerupCount(int powerupCount) {
 		this.powerupCount = powerupCount;
 	}
 
 	/**
 	 * Get the weapon count.
 	 * @return The weapon count
 	 */
 	public int getWeaponCount() {
 		return weaponCount;
 	}
 
 	/**
 	 * Set the weapon count.
 	 * @param weaponCount The new weapon count
 	 */
 	public void setWeaponCount(int weaponCount) {
 		this.weaponCount = weaponCount;
 	}
 
 }
