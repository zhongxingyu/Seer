 package me.asofold.bukkit.fattnt;
 
 import java.io.File;
 import java.util.List;
 
 import me.asofold.bukkit.fattnt.config.Defaults;
 import me.asofold.bukkit.fattnt.config.Settings;
 import me.asofold.bukkit.fattnt.config.compatlayer.CompatConfig;
 import me.asofold.bukkit.fattnt.config.compatlayer.NewConfig;
 import me.asofold.bukkit.fattnt.effects.DamageProcessor;
 import me.asofold.bukkit.fattnt.effects.ExplosionManager;
 import me.asofold.bukkit.fattnt.propagation.Propagation;
 import me.asofold.bukkit.fattnt.propagation.PropagationFactory;
 import me.asofold.bukkit.fattnt.stats.Stats;
 import me.asofold.bukkit.fattnt.utils.Utils;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Item;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityCombustEvent;
 import org.bukkit.event.entity.ExplosionPrimeEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitScheduler;
 
 /**
  * Experimental plugin to replace explosions completely.
  * 
  * @author mc_dev
  * @license See project folder, either LICENSE.TXT or fattnt.lists.
  *
  */
 public class FatTnt extends JavaPlugin implements Listener {
 	
 	public static final boolean DEBUG = false;
 	public static final boolean DEBUG_LOTS = false;
 	
 	private static final Stats stats = new Stats(Defaults.msgPrefix.trim()+"[STATS]");
 	public static final Integer statsGetBlocks = stats.getNewId("get_blocks");
 	public static final Integer statsApplyBlocks = stats.getNewId("apply_blocks");
 	public static final Integer statsExplodeEvent = stats.getNewId("event_explode");
 	public static final Integer statsApplyEntities = stats.getNewId("apply_entities");
 	public static final Integer statsNearbyEntities = stats.getNewId("nearby_entities");
 	public static final Integer statsBlocksVisited = stats.getNewId("blocks_visited");
 	public static final Integer statsBlocksCollected = stats.getNewId("blocks_collected");
 	public static final Integer statsStrength = stats.getNewId("strength");
 	public static final Integer statsDamage = stats.getNewId("damage");
 	public static final Integer statsAll = stats.getNewId("all");
 	static {
 		stats.setLogStats(DEBUG);
 		ExplosionManager.setStats(stats);
 	}
 	
 //	ExplosionPrimeEvent waitingEP = null;
 	
 	private final Settings settings = new Settings(stats);
 	private DamageProcessor damageProcessor = new DamageProcessor(settings);
 	
 	private Propagation propagation = null;
 	
 	public FatTnt(){
 		super();
 	}
 	
 	@Override
 	public void onEnable() {
 		reloadSettings();
 		getServer().getPluginManager().registerEvents(this, this);
 		System.out.println(Defaults.msgPrefix+getDescription().getFullName()+" is enabled.");
 	}
 	
 	@Override
 	public void onDisable() {
 		System.out.println(Defaults.msgPrefix+getDescription().getFullName()+" is disabled.");
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		label = label.toLowerCase();
 		if ( !label.equals("fattnt") && !label.equals("ftnt")) return false;
 		int len = args.length;
 		if (len==1 && args[0].equalsIgnoreCase("reload")){
 			if ( !Utils.checkPerm(sender, "fattnt.cmd.reload")) return true;
 			reloadSettings();
 			Utils.send(sender, "Settings reloaded.");
 			return true;
 		} 
 		else if (len==1 && args[0].equalsIgnoreCase("enable")){
 			if ( !Utils.checkPerm(sender, "fattnt.cmd.enable")) return true;
 			settings.setHandleExplosions(true);
 			Utils.send( sender, "Explosions will be handled by FatTnt."); 
 			return true;
 		}
 		else if (len==1 && args[0].equalsIgnoreCase("disable")){
 			if ( !Utils.checkPerm(sender, "fattnt.cmd.disable")) return true;
 			settings.setHandleExplosions(false);
 			Utils.send( sender, "Explosions are back to default behavior (disregarding other plugins)."); 
 			return true;
 		}
 		else if (len==1 && (args[0].equalsIgnoreCase("stats") || args[0].equalsIgnoreCase("st"))){
 			if ( !Utils.checkPerm(sender, "fattnt.cmd.stats.see")) return true;
 			Utils.send(sender, stats.getStatsStr(true), false);
 			return true;
 		}
 		else if (len==2 && (args[0].equalsIgnoreCase("stats") || args[0].equalsIgnoreCase("st")) && args[1].equalsIgnoreCase("reset")){
 			if ( !Utils.checkPerm(sender, "fattnt.cmd.stats.reset")) return true;
 			stats.clear();
 			Utils.send(sender, "Stats reset.");
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Reload and apply settings from the default configuration file.
 	 * (uses applySettings)
 	 */
 	public void reloadSettings() {
 		BukkitScheduler sched = getServer().getScheduler();
 		sched.cancelTasks(this);
 		File file = new File (getDataFolder(), "config.yml");
 		boolean exists = file.exists();
		reloadConfig();
 		CompatConfig cfg = new NewConfig(file);
 		boolean changed = Defaults.addDefaultSettings(cfg);
		if (!exists || changed) saveConfig();
 		applySettings(cfg);
 		sched.scheduleSyncRepeatingTask(this, new Runnable(){
 			@Override
 			public void run() {
 				onIdle();
 			}
 		}, 217, 217);
 	}
 	
 	private void onIdle() {
 		propagation.onIdle();
 	}
 	
 	/**
 	 * Apply the settings.
 	 * @param cfg
 	 */
 	public void applySettings(CompatConfig cfg){
 		settings.applyConfig(cfg);
 		// TODO: propagation pbased on config (Factory)
 		propagation = PropagationFactory.getPropagation(settings);
 		setDamageProcessor(new DamageProcessor(settings));
 	}
 
 	/**
 	 * API
 	 * @param damageProcessor
 	 */
 	public void setDamageProcessor(DamageProcessor damageProcessor) {
 		this.damageProcessor = damageProcessor;
 	}
 
 	@EventHandler(priority=EventPriority.HIGHEST)
 	void onExplosionPrimeLowest(ExplosionPrimeEvent event){
 //		waitingEP = null;
 		if (!settings.handleExplosions) return;
 		else if (event.isCancelled()) return;
 		else if (!settings.handledEntities.contains(event.getEntityType())) return;
 		// TODO: maybe apply delay setting here ...
 		// do prepare to handle this explosion:
 		event.setCancelled(true);
 //		waitingEP = event;
 //	}
 //	
 //	@EventHandler(priority=EventPriority.MONITOR)
 //	void onExplosionPrime(ExplosionPrimeEvent event){
 //		// check event 
 //		if ( waitingEP != event) return;
 //		waitingEP = null;
 //		// event is to be handled:
 //		if ( !event.isCancelled()) event.setCancelled(true); // just in case other plugins mess with this one.
 		EntityType type = event.getEntityType();
 		Entity entity = event.getEntity();
 		Location loc = entity.getLocation().clone();
 		double x = loc.getX();
 		double y = loc.getY();
 		double z = loc.getZ();
 		if (!entity.isDead()) entity.remove();
 		createExplosion(loc.getWorld(), x, y, z, event.getRadius(), event.getFire(), entity, type);
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST)
 	void onEntityCombust(EntityCombustEvent event){
 		// TODO:
 		if (event.isCancelled()) return;
 		if (!settings.itemTnt) return;
 		Entity entity = event.getEntity();
 		if ( !(entity instanceof Item)) return;
 		Item item = (Item) entity;
 		ItemStack stack = item.getItemStack();
 		if ( stack.getType() != Material.TNT) return;
 		event.setCancelled(true);
 		ExplosionManager.replaceByTNTPrimed(item);		
 	}
 	
 	/**
 	 * API
 	 * @param loc
 	 * @param radius As in World.createExplosion
 	 * @param fire
 	 * @param explEntity
 	 * @param entityType May be null, might be unused.
 	 */
 	public void createExplosion(Location loc, float radius, boolean fire, Entity explEntity, EntityType entityType){
 		World world = loc.getWorld();
 		double x = loc.getX();
 		double y = loc.getY();
 		double z = loc.getZ();
 		createExplosion(world, x, y, z, radius, fire, explEntity, entityType);
 	}
 	
 	/**
 	 * API
 	 * (called from event handling as well)
 	 * @param world
 	 * @param x
 	 * @param y
 	 * @param z
 	 * @param radius As in World.createExplosion
 	 * @param fire
 	 * @param explEntity Not sure what happens if this is null: the events use this.
 	 * @param entityType May be null, might be unused.
 	 */
 	public void createExplosion(World world, double x, double y, double z, float radius, boolean fire, Entity explEntity, EntityType entityType){
 		// create a fake explosion
 		ExplosionManager.createExplosionEffect(world, x, y, z, radius, fire);
 		if (radius==0.0f) return;
 		// calculate effects
 		// WORKAROUND:
 		float realRadius = radius*settings.radiusMultiplier;
 		List<Entity> nearbyEntities;
 		long ms = System.nanoTime();
 		if (explEntity==null) nearbyEntities = Utils.getNearbyEntities(world, x,y,z, realRadius*settings.entityRadiusMultiplier);
 		else nearbyEntities = explEntity.getNearbyEntities(realRadius, realRadius, realRadius*settings.entityRadiusMultiplier);
 		stats.addStats(statsNearbyEntities, System.nanoTime()-ms);
 		applyExplosionEffects(world, x, y, z, realRadius, fire, explEntity, entityType, nearbyEntities);
 	}
 
 	/**
 	 * API
 	 * @param loc
 	 * @param radius As in World.createExplosion
 	 * @param fire
 	 */
 	public void createExplosion(Location loc, float radius, boolean fire){
 		createExplosion(loc, radius, fire, null, null);
 	}
 
 	/**
 	 * API
 	 * @param world
 	 * @param x
 	 * @param y
 	 * @param z
 	 * @param radius As in World.createExplosion
 	 * @param fire
 	 */
 	public void createExplosion(World world, double x, double y, double z, float radius, boolean fire) {
 		createExplosion(world, x, y, z, radius, fire, null, null);
 	}
 	
 	/**
 	 * This method only considers the given entities for damage.
 	 * API
 	 * (used internally)
 	 * (this method uses seqMax, such that it should not get manipulated anywhere but inside of getExplodingBlocks)
 	 * @param world
 	 * @param x
 	 * @param y
 	 * @param z
 	 * @param realRadius intended to be the block distance rather, so that resistance of 1.0 (AIR) would lead to the full range.
 	 * @param fire
 	 * @param entityType Causing the explosion.
 	 * @param nearbyEntities List of entities that can be affected , should be within radius (x,y,z independently), damage depends on settings.
 	 */
 	public void applyExplosionEffects(World world, double x, double y, double z, float realRadius, boolean fire, Entity explEntity, EntityType entityType,
 			List<Entity> nearbyEntities) {
 		applyExplosionEffects(world, x, y, z, realRadius, fire, explEntity, entityType, nearbyEntities, 1.0f);
 	}
 	
 	/**
 	 * This method only considers the given entities for damage, but allows for specification of an extra damage multiplier.
 	 * API
 	 * (used internally)
 	 * (this method uses seqMax, such that it should not get manipulated anywhere but inside of getExplodingBlocks)
 	 * @param world
 	 * @param x
 	 * @param y
 	 * @param z
 	 * @param realRadius
 	 * @param fire
 	 * @param explEntity
 	 * @param entityType
 	 * @param nearbyEntities
 	 * @param damageMultiplier
 	 */
 	public void applyExplosionEffects(World world, double x, double y, double z, float realRadius, boolean fire, Entity explEntity, EntityType entityType,
 			List<Entity> nearbyEntities, float damageMultiplier) {
 		long ns = System.nanoTime();
 		ExplosionManager.applyExplosionEffects(world, x, y, z, realRadius, fire, explEntity, entityType, nearbyEntities, damageMultiplier, settings, propagation, damageProcessor);
 		stats.addStats(statsAll, System.nanoTime()-ns);
 	}
 
 	/**
 	 * Get exploding blocks for a world, explosion center, explosion strength.
 	 * This will use the default propagation model.
 	 * This does not change the world in any way, it just collects the block. However it changes FatTnt internals.
 	 * NEVER CALL THIS ASYNCHRONOUSLY OR DURING EVENT PROCESSING (ExplosionPrime, EntityDamageEvent, EntityExplodeEvent).
 	 * Command based access should be ok, for instance.
 	 * (API)
 	 * (Used internally)
 	 * @param world
 	 * @param cx Center ...
 	 * @param cy
 	 * @param cz
 	 * @param realRadius
 	 * @return
 	 */
 	public List<Block> getExplodingBlocks(World world, double cx, double cy,
 			double cz, float realRadius) {
 		return propagation.getExplodingBlocks(world, cx, cy, cz, realRadius);
 	}
 	
 	/**
 	 * This only should be called after an explosion has occurred, 
 	 * or after getExplodinBlocks or any of the createExplosion methods has been called.
 	 * (API)
 	 * @param loc
 	 * @return
 	 */
 	public final float getExplosionStrength(final Location loc){
 		return propagation.getStrength(loc);
 	}
 	
 	/**
 	 * This only should be called after an explosion has occurred, 
 	 * or after getExplodinBlocks or any of the createExplosion methods has been called.
 	 * (API)
 	 * @param x
 	 * @param y
 	 * @param z
 	 * @return
 	 */
 	public final float getExplosionStrength(double x, double y, double z){
 		return propagation.getStrength(x, y, z);
 	}
 	
 	/**
 	 * Convenience.
 	 * (API)
 	 * @return
 	 */
 	public static FatTnt getInstance(){
 		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("FatTnt");
 		if ( plugin instanceof FatTnt ) return  (FatTnt) plugin;
 		else return null;
 	}
 	
 	/**
 	 * Get the default settings in use.
 	 * HANDLE WITH CARE, DO NOT MANIPULATE.
 	 * (API)
 	 * @return
 	 */
 	public Settings getSettings(){
 		return settings;
 	}
 	
 }
