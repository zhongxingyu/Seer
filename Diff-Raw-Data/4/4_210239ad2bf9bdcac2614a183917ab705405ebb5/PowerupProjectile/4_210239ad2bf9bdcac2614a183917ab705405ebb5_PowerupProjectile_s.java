 package uk.thecodingbadgers.minekart.powerup;
 
 import java.io.File;
 
 import org.bukkit.Location;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.util.Vector;
 
 import uk.thecodingbadgers.minekart.jockey.Jockey;
 
 public class PowerupProjectile extends Powerup {
 
 	/** Type of projectile to launch **/
 	private EntityType type;
 	
 	/** The speed of the projectile **/
 	private double speed;
 	
 	/**
 	 * Class constructor
 	 */
 	public PowerupProjectile() {
 		this.useMode = PowerupUseMode.Projectile;
 	}
 	
 	/**
 	 * Copy constructor
 	 * @param powerup Powerup to copy from
 	 */
 	public PowerupProjectile(PowerupProjectile powerup) {
 		super(powerup);
 		this.speed = powerup.speed;
 		this.type = powerup.type;
 	}
 	
 	/**
 	 * Load the powerup
 	 * @param file The file containing the powerup data
 	 */
 	public void load(File configfile) {
 		super.load(configfile);
 		
 		FileConfiguration file = YamlConfiguration.loadConfiguration(configfile);
 		
 		this.speed = file.getDouble("powerup.projectile.speed");		
 		this.type = EntityType.valueOf(file.getString("powerup.projectile.type"));
 		
 	}
 
 	/**
 	 * Called when the powerup is used
 	 * @param player The player who used it
 	 */
 	@Override
 	public void onUse(Jockey jockey) {
 		
 		this.amount--;
 		
 		final Player player = jockey.getPlayer();
 		
 		Location spawnLocation = player.getLocation();
 		Vector mountDirection = jockey.getMount().getBukkitEntity().getLocation().getDirection();
		spawnLocation = spawnLocation.add(mountDirection.multiply(this.speed < 0 ? -2.0f : 2.0f));
 		
 		Projectile projectile = (Projectile)player.getWorld().spawnEntity(spawnLocation, this.type);
 		projectile.setVelocity(mountDirection.multiply(this.speed));
 		projectile.setShooter(player);
 		
 	}
 
 }
