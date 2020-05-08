 package gt.general.character;
 
 import gt.general.aura.Aura;
 import gt.plugin.meta.Hello;
 
 import java.util.Vector;
 
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Zombie;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.Listener;
 
 /**
  * a Manager for Zombies of an instance 
  * @author philipp
  *
  */
 public class ZombieManager implements Listener, Runnable{
 	
 	private LivingEntity target;
 	private Vector<ZombieCharacter> zombies;
 	private final World world;
 	private int taskID;
 	
 	/**
 	 * Creates a new ZombieManager
 	 * @param world the world, where the Zombies will be spawned
 	 */
 	public ZombieManager(final World world) {
 		zombies = new Vector<ZombieCharacter>();
 		this.world = world;
 	}
 	
 	/**
 	 * set the id of this task
 	 * @param id task id
 	 */
 	public void setTaskID(final int id) {
 		taskID = id;
 	}
 
 	
 	/**
 	 *  Handles events, when Zombie hits or is hit
 	 * @param event an EntityDamageByEntity Event
 	 */
 	@EventHandler
 	public void damageEntityEvent(final EntityDamageByEntityEvent event) {
 		//On hit, Zombie drains 1/3 MaxHealth
 		if (event.getDamager() instanceof Zombie) {
 			LivingEntity p = (LivingEntity) event.getEntity();
 			event.setDamage(p.getMaxHealth()/3);
 		}
 		
 		//Zombies cannot be harmed by Players
 		if (event.getEntity() instanceof Zombie) {
 			if (event.getDamager() instanceof Player) {
 			event.setCancelled(true);
 			}
 		}
 		
 	}
 	
 	/**
 	 * 
 	 * @return current Target of Zombies
 	 */
 	public LivingEntity getTarget() {
 		return target;
 	}
 
 	/**
 	 * Set a new target for Zombies
 	 * @param hero the new target
 	 */
 	public void setTarget(final Hero hero) {
 		
 		if(hero != null) {
 			target = hero.getPlayer();
 		}		
 	}
 
 	/**
 	 * Spawns a Zombie
 	 * @param spawnpoint location, where zombie should be spawned
 	 */
 	public void spawnZombie(final Location spawnpoint) {
 		spawnZombie(spawnpoint,1.0);
 	}
 	
 	/**
 	 * Spawns a Zombie with different speed
 	 * @param spawnpoint location, where zombie should be spawned
 	 * @param speed the Zombies basic speed-multiplicator
 	 */
 	public void spawnZombie(final Location spawnpoint, final double speed) {
 		spawnZombie(spawnpoint, null, speed);
 	}
 	
 	/**
 	 * Spawns a Zombie with different speed and an aura
 	 * @param spawnpoint location, where zombie should be spawned
 	 * @param aura an Aura which originates from the zombie 
 	 * @param speed the Zombies basic speed-multiplicator
 	 * 
 	 */
 	public void spawnZombie(final Location spawnpoint, final Aura aura, final double speed) {
 		ZombieCharacter zombie = new ZombieCharacter(world.spawn(spawnpoint, Zombie.class));
 		zombie.setAttribute(CharacterAttributes.SPEED, speed);
 		//make sure not to add null-auras
 		if (aura != null) {
 			zombie.addAura(aura);
 		}
 		zombies.add(zombie);
 	}
 	
 	/**
 	 * remove all zombies & cancel the schedule
 	 */
 	public void cleanup() {
 		Hello.cancelScheduledTask(taskID);
 		clearZombies();
 	}
 
 	/**
 	 * Kills and removes all Zombies
 	 */
 	public void clearZombies() {
 		for (ZombieCharacter zombie : zombies) {
 			zombie.getZombie().getWorld().playEffect(
 					zombie.getZombie().getLocation(), Effect.POTION_BREAK, 10);
 			//zombie.getZombie().damage(20);
 			zombie.getZombie().remove();
 		}
 		zombies.clear();
 	}
 	
 	/**
 	 * Modifies Speed of all Zombies
 	 * @param value Value to be added to Zombie speed
 	 */
 	public void addSpeedAll(final Double value) {
 		for (ZombieCharacter zombie : zombies) {
 			zombie.addToAttribute(CharacterAttributes.SPEED, value);
 		}
 	}
 	
 
 	
 	
 	/**
 	 * Scheduled Method to make sure zombies attack right target
 	 */
 	@Override
 	public void run() {
 		if (target == null) {
 			return;
 		}
 		
 		for (ZombieCharacter zombieChar : zombies) {
 			Zombie zombie = zombieChar.getZombie();
 			//See if any Players are nearby
 			for (Entity entity : zombie.getNearbyEntities(1.5, 1.5, 1.5)) {
 				if (entity.getType() == EntityType.PLAYER) {
 					//target players who are to close
 					if (zombie.getTarget() == null ||!zombie.getTarget().equals(entity)) {
						zombie.setTarget((LivingEntity) entity);
 					}
 					return;
 				}
 			}
 			
 			//If no player is to close, target the target
 			if (zombie.getTarget() == null || !zombie.getTarget().equals(target)) {
				zombie.setTarget(target);
 			}
 			
 		}
 	}
 
 }
