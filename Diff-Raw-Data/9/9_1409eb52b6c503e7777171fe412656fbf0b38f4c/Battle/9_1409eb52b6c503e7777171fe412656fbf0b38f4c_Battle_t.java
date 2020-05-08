 package com.censoredsoftware.Demigods.Engine.Object.Battle;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Tameable;
 import org.bukkit.util.Vector;
 
 import redis.clients.johm.*;
 
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.Exceptions.SpigotNotFoundException;
 import com.censoredsoftware.Demigods.Engine.Object.General.DemigodsLocation;
 import com.censoredsoftware.Demigods.Engine.Object.Mob.TameableWrapper;
 import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerCharacter;
 import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerWrapper;
 import com.censoredsoftware.Demigods.Engine.Utility.LocationUtility;
 import com.censoredsoftware.Demigods.Engine.Utility.MiscUtility;
 
 @Model
 public class Battle
 {
 	@Id
 	private Long Id;
 	@Reference
 	private BattleMeta meta;
 	@Reference
 	private DemigodsLocation startLoc;
 	@Attribute
 	@Indexed
 	private boolean active;
 	@Attribute
 	private double range;
 	@Attribute
 	private long duration;
 	@Attribute
 	private int minKills;
 	@Attribute
 	private int maxKills;
 	@Attribute
 	private long startTime;
 	@Attribute
 	private long deleteTime;
 
 	public static Battle create(BattleParticipant damager, BattleParticipant damaged)
 	{
 		Battle battle = new Battle();
 		battle.setStartLocation(damager.getCurrentLocation().toVector().getMidpoint(damaged.getCurrentLocation().toVector()).toLocation(damager.getCurrentLocation().getWorld()));
 		battle.setStartTime(System.currentTimeMillis());
 
 		int default_range = Demigods.config.getSettingInt("battles.min_range");
 		double range = damager.getCurrentLocation().distance(damaged.getCurrentLocation());
 		if(range < default_range) battle.setRange(default_range);
 		else battle.setRange(range);
 
 		battle.setActive();
 
 		battle.setDuration(Demigods.config.getSettingInt("battles.min_duration") * 1000);
 		battle.setMinKills(Demigods.config.getSettingInt("battles.min_kills"));
 		battle.setMaxKills(Demigods.config.getSettingInt("battles.max_kills"));
 
 		BattleMeta meta = BattleMeta.create(damager);
 		meta.addParticipant(damager);
 		meta.addParticipant(damaged);
 		battle.setMeta(meta);
 		Battle.save(battle);
 
 		Demigods.message.broadcast("Battle created."); // TODO Debug.
 
 		return battle;
 	}
 
 	void setMeta(BattleMeta meta)
 	{
 		this.meta = meta;
 	}
 
 	public void setRange(double range)
 	{
 		this.range = range;
		save(this);
 	}
 
 	public void setActive()
 	{
 		this.active = true;
		save(this);
 	}
 
 	public void setInactive()
 	{
 		this.active = false;
		save(this);
 	}
 
 	public void setDuration(long duration)
 	{
 		this.duration = duration;
		save(this);
 	}
 
 	public void setMinKills(int kills)
 	{
 		this.minKills = kills;
 		save(this);
 	}
 
 	public void setMaxKills(int kills)
 	{
 		this.maxKills = kills;
 		save(this);
 	}
 
 	void setStartLocation(Location location)
 	{
 		this.startLoc = DemigodsLocation.create(location);
 	}
 
 	void setStartTime(long time)
 	{
 		this.startTime = time;
 	}
 
 	void setDeleteTime(long time)
 	{
 		this.deleteTime = time;
		save(this);
 	}
 
 	public long getId()
 	{
 		return this.Id;
 	}
 
 	public double getRange()
 	{
 		return this.range;
 	}
 
 	public boolean isActive()
 	{
 		return this.active;
 	}
 
 	public long getDuration()
 	{
 		return this.duration;
 	}
 
 	public int getMinKills()
 	{
 		return this.minKills;
 	}
 
 	public int getMaxKills()
 	{
 		return this.maxKills;
 	}
 
 	public BattleMeta getMeta()
 	{
 		return this.meta;
 	}
 
 	public Location getStartLocation()
 	{
 		return this.startLoc.toLocation();
 	}
 
 	public long getStartTime()
 	{
 		return this.startTime;
 	}
 
 	public long getDeleteTime()
 	{
 		return this.deleteTime;
 	}
 
 	public static Battle get(Long id)
 	{
 		return JOhm.get(Battle.class, id);
 	}
 
 	public static Set<Battle> getAll()
 	{
 		return JOhm.getAll(Battle.class);
 	}
 
 	public static List<Battle> getAllActive()
 	{
 		return JOhm.find(Battle.class, "active", true);
 	}
 
 	public static List<Battle> getAllInactive()
 	{
 		return JOhm.find(Battle.class, "active", false);
 	}
 
 	public static void save(Battle battle)
 	{
 		JOhm.save(battle);
 	}
 
 	public void end()
 	{
 		// Prepare for graceful delete
 		setDeleteTime(System.currentTimeMillis() + 3000L);
 		setInactive();
 
 		Demigods.message.broadcast(ChatColor.YELLOW + "A battle has ended."); // TODO
 	}
 
 	public void delete()
 	{
 		getMeta().delete();
 		JOhm.delete(Battle.class, getId());
 	}
 
 	public static boolean existsInRadius(Location location)
 	{
 		return getInRadius(location) != null;
 	}
 
 	public static Battle getInRadius(Location location)
 	{
 		for(Battle battle : Battle.getAllActive())
 		{
 			if(battle.getStartLocation().distance(location) <= battle.getRange()) return battle;
 		}
 		return null;
 	}
 
 	public static boolean isInBattle(BattleParticipant participant)
 	{
 		for(Battle battle : Battle.getAllActive())
 		{
 			if(battle.getMeta().getParticipants().contains(participant)) return true;
 		}
 		return false;
 	}
 
 	public static Battle getBattle(BattleParticipant participant)
 	{
 		for(Battle battle : Battle.getAllActive())
 		{
 			if(battle.getMeta().getParticipants().contains(participant)) return battle;
 		}
 		return null;
 	}
 
 	public static boolean existsNear(Location location)
 	{
 		return getNear(location) != null;
 	}
 
 	public static Battle getNear(Location location)
 	{
 		for(Battle battle : Battle.getAllActive())
 		{
 			double distance = battle.getStartLocation().distance(location);
 			if(distance > battle.getRange() && distance <= Demigods.config.getSettingInt("battles.merge_range")) return battle;
 		}
 		return null;
 	}
 
 	public Set<Location> battleBorder()
 	{
 		if(!Demigods.runningSpigot()) throw new SpigotNotFoundException();
 		return new HashSet<Location>()
 		{
 			{
 				for(Location point : LocationUtility.getCirclePoints(getStartLocation(), getRange(), 120))
 					add(new Location(point.getWorld(), point.getBlockX(), point.getWorld().getHighestBlockYAt(point), point.getBlockZ()));
 			}
 		};
 	}
 
 	public static Location randomRespawnPoint(Battle battle)
 	{
 		List<Location> respawnPoints = getSafeRespawnPoints(battle);
 		if(respawnPoints.size() == 0) return battle.getStartLocation();
 
 		Location target = respawnPoints.get(MiscUtility.generateIntRange(0, respawnPoints.size() - 1));
 
 		Vector direction = target.toVector().subtract(battle.getStartLocation().toVector()).normalize();
 		double X = direction.getX();
 		double Y = direction.getY();
 		double Z = direction.getZ();
 
 		// Now change the angle
 		Location changed = target.clone();
 		changed.setYaw(180 - LocationUtility.toDegree(Math.atan2(X, Y)));
 		changed.setPitch(90 - LocationUtility.toDegree(Math.acos(Z)));
 		return changed;
 	}
 
 	public static boolean isSafeLocation(Location reference, Location checking)
 	{
 		if(reference.getBlock().getType().isSolid() || reference.getBlock().getType().equals(Material.LAVA)) return false;
 		double referenceY = reference.getY();
 		double checkingY = checking.getY();
 		if(Math.abs(referenceY - checkingY) > 5) return false;
 		return true;
 	}
 
 	public static List<Location> getSafeRespawnPoints(final Battle battle)
 	{
 		return new ArrayList<Location>()
 		{
 			{
 				for(Location location : LocationUtility.getCirclePoints(battle.getStartLocation(), battle.getRange() - 1.5, 20))
 				{
 					if(isSafeLocation(battle.getStartLocation(), location)) add(location);
 				}
 			}
 		};
 	}
 
 	public static boolean canParticipate(Entity entity)
 	{
 		if(!(entity instanceof Player) && !(entity instanceof Tameable)) return false;
 		if(entity instanceof Player && PlayerWrapper.getPlayer((Player) entity).getCurrent() == null) return false;
 		if(entity instanceof Tameable && TameableWrapper.getTameable((LivingEntity) entity) == null) return false;
 		return true;
 	}
 
 	public static BattleParticipant defineParticipant(Entity entity)
 	{
 		if(entity instanceof Player) return PlayerWrapper.getPlayer((Player) entity).getCurrent();
 		return TameableWrapper.getTameable((LivingEntity) entity);
 	}
 
 	public static void teleportIfNeeded(BattleParticipant participant, Battle battle)
 	{
 		if(participant.getRelatedCharacter().getOfflinePlayer().isOnline() && !existsInRadius(participant.getRelatedCharacter().getOfflinePlayer().getPlayer().getLocation())) participant.getRelatedCharacter().getOfflinePlayer().getPlayer().teleport(randomRespawnPoint(battle));
 	}
 
 	public static boolean battleDeath(BattleParticipant damager, BattleParticipant damagee, Battle battle)
 	{
 		if(damager instanceof PlayerCharacter) ((PlayerCharacter) damager).addKill();
 		if(damager.getRelatedCharacter().getOfflinePlayer().isOnline()) damager.getRelatedCharacter().getOfflinePlayer().getPlayer().sendMessage(ChatColor.GREEN + "+1 Kill.");
 		battle.getMeta().addKill(damager);
 		return battleDeath(damagee, battle);
 	}
 
 	public static boolean battleDeath(BattleParticipant damagee, Battle battle)
 	{
 		damagee.getEntity().setHealth(damagee.getEntity().getMaxHealth());
 		damagee.getEntity().teleport(randomRespawnPoint(battle));
 		if(damagee instanceof PlayerCharacter) ((PlayerCharacter) damagee).addDeath();
 		if(damagee.getRelatedCharacter().getOfflinePlayer().isOnline()) damagee.getRelatedCharacter().getOfflinePlayer().getPlayer().sendMessage(ChatColor.RED + "+1 Death.");
 		battle.getMeta().addDeath(damagee);
 		return true;
 	}
 }
