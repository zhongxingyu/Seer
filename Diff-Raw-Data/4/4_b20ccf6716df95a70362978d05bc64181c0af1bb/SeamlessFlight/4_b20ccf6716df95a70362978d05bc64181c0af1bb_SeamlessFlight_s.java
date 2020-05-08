 package me.asofold.bpl.seamlessflight;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import me.asofold.bpl.seamlessflight.config.compatlayer.CompatConfig;
 import me.asofold.bpl.seamlessflight.config.compatlayer.CompatConfigFactory;
 import me.asofold.bpl.seamlessflight.flymode.FlyConfig;
 import me.asofold.bpl.seamlessflight.flymode.FlyMode;
 import me.asofold.bpl.seamlessflight.flymode.FlyResult;
 import me.asofold.bpl.seamlessflight.flymode.FlyState;
 import me.asofold.bpl.seamlessflight.hooks.NoCheatPlusHooks;
 import me.asofold.bpl.seamlessflight.plshared.actions.ActionType;
 import me.asofold.bpl.seamlessflight.settings.Settings;
 import me.asofold.bpl.seamlessflight.settings.combat.CombatSymmetry;
 import me.asofold.bpl.seamlessflight.settings.combat.CombatSymmetrySettings;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.entity.PotionSplashEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.event.player.PlayerToggleSneakEvent;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitScheduler;
 
 /**
  * Flying plugin, detached from CustomPlg, uses plshared.
  * @author mc_dev
  *
  */
 public class SeamlessFlight extends JavaPlugin implements Listener{
 	
 	/////////////////////////
 	// Static
 	/////////////////////////
 	
 	public static final String TAG = "[SeamlessFlight]";
 	
 	/**
 	 * Return trim/lower case label of command or given label (prefers command).
 	 * @param command Can be null.
 	 * @param label
 	 * @return
 	 */
 	public static String getLabel(Command command, String label){
 		if (command != null) label = command.getLabel();
 		return label.trim().toLowerCase();
 	}
 	
 	/////////////////////////
 	// Instance
 	/////////////////////////
 	
 	/** Core flying functionality. */
 	private final FlyMode flyMode;
 	
 	/** If to use polling to smoothen flying */
 	private boolean usePolling = true;
 	
 	/** NoCheatPlus plugin present. */
 	private boolean ncpPresent = false;
 	/** Id of the NCP hook in use. */
 	private Integer ncpHookId = null;
 	
 	/** Lower case name to FlyConfig. */
 	private final Map<String, FlyConfig> flyConfigs = new HashMap<String, FlyConfig>(50);
 	
 	/** Lower case names of players that are flying right now to Player. */
 	private final Map<String, Player> flying = new LinkedHashMap<String, Player>(50);
 	
 	private final Set<String> flyCmds = new LinkedHashSet<String>();
 
 	private Settings settings;
 	
 	public SeamlessFlight(){
 		for (String cmd : new String[]{
 				"on", "off", "enable", "disable", "1", "0", "start", "stop",
 		}){
 			flyCmds.add(cmd);
 		}
 		flyMode = new FlyMode() {
 			@Override
 			public final FlyConfig getFlyConfig(final String playerName) {
 				final String lcName = playerName.toLowerCase();
 				final FlyConfig fc = flyConfigs.get(lcName);
 				if (fc != null) return fc;
 				final Player player = Bukkit.getPlayer(playerName);
 				if (player == null) return null;
 				if (!player.hasPermission(flyMode.rootPerm + ".use")) return null;
 				final FlyConfig newFc = new FlyConfig();
 				flyConfigs.put(lcName, newFc);
 				return newFc;
 			}
 			@Override
 			public final void onActionCallBack(final String user, final int fold, final ActionType actionType) {
 				super.onActionCallBack(user, fold, actionType);
 				onToggleSneakCallBack(user);
 			}
 		};
 		flyMode.rootPerm = "seamlessflight.fly";
 	}
 	
 	@Override
 	public void onEnable() {
 		
 		// Register listeners.
 		final PluginManager pm = Bukkit.getPluginManager();
 		pm.registerEvents(this, this);
 		
 		// Set up tasks.
 		// Tick task:
 		final BukkitScheduler sched = Bukkit.getScheduler();
 		sched.scheduleSyncRepeatingTask(this, new Runnable() {	
 			@Override
 			public final void run() {
 				onTick();
 			}
 		}, 1, 1);
 		
 		// Register action checker.
 		flyMode.registerActionChecker(this);
 		
 		// Load settings.
 		reloadSettings(null);
 		
 		// Register hooks.
 		// NoCheatPlus
 		try{
 			ncpHookId = NoCheatPlusHooks.registerNCPHook(this);
 			ncpPresent = true;
 			Bukkit.getLogger().info(TAG + " Registered NoCheatPlus hook.");
 		}
 		catch (Throwable t){}
 		
 		// Load flystates.
 		// TODO: check settings.
 		loadFlyStates();
 		
 		// Done.
 		Bukkit.getLogger().info(TAG + " " + getDescription().getFullName() + " is enabled.");
 	}
 	
 	@Override
 	public void onDisable() {
 		// Cancel tasks.
 		Bukkit.getScheduler().cancelTasks(this);
 		// Cancel ActionChecker.
 		flyMode.cancelActionChecker();
 		// Save flystates.
 		// TODO: check settings.
 		saveFlyStates();
 		flyConfigs.clear();
 		// Unregister hooks.
 		// NoCheatPlus
 		if (ncpPresent){
 			try{
 				if (ncpHookId != null) NoCheatPlusHooks.unregisterHook(ncpHookId);
 				Bukkit.getLogger().info(TAG + " Unregistered NoCheatPlus hook.");
 			}
 			catch (Throwable t){}
 			ncpPresent = false;
 		}
 		// Done.
 		Bukkit.getLogger().info(TAG + " " + getDescription().getFullName() + " is disabled.");
 	}
 	
 	public void saveFlyStates(){
 		final CompatConfig cfg = CompatConfigFactory.getConfig(getFlyStatesFile());
 		for (final Entry<String, FlyConfig> entry : flyConfigs.entrySet()){
 			final FlyConfig fc = entry.getValue();
 			if (fc.flyState != FlyState.DISABLED) cfg.setProperty(entry.getKey(), fc.flyState.toString() );
 		}
 		cfg.save();
 	}
 	
 	public void loadFlyStates(){
 		// TODO: policy about removing old entries ?
 		final CompatConfig cfg = CompatConfigFactory.getConfig(getFlyStatesFile());
 		cfg.load();
 		for (final String key : cfg.getStringKeys()){
 			final String value = cfg.getString(key);
 			if (value == null) continue;
 			FlyState flyState = null;
 			try{
 				flyState = FlyState.valueOf(value.toUpperCase());
 			} catch (Throwable t){};
 			if ( flyState != null && flyState != FlyState.DISABLED){ // different than default values.
 				FlyConfig fc = new FlyConfig();
 				fc.flyState = flyState;
 				flyConfigs.put(key.trim().toLowerCase(), fc);
 			}
 		}
 	}
 	
 	public File getFlyStatesFile() {
 		return new File(getDataFolder(), "flystates.yml");
 	}
 	
 //	public void reloadSettings(){
 //		// TODO: integrate this.
 //		this.useFlyPolling = cfg.getBoolean("flymode.use-polling", true);
 //		flyMode.msFlyCheck = cfg.getLong( "flymode.ms-perm-check", 500L);;
 //		flyMode.msFullCheck = cfg.getLong("flymode.ms-full-check", 43L);
 //		flyMode.periodActionChecker = cfg.getLong("flymode.period-actionchecker", 5L);
 //		flyMode.smoothing = cfg.getBoolean("flymode.smoothing.enable", true);
 //		flyMode.smoothingWeight = cfg.getDouble("flymode.smoothing.weight", 0.5);
 //
 //	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
 	{
 		label = getLabel(command, label);
 		String cmd = null;
 		if (args.length > 0) cmd = args[0].trim().toLowerCase();
 		if (label.equals("sfreload")){
 			if (!sender.hasPermission("seamlessflight.reload")){
 				sender.sendMessage(ChatColor.DARK_RED + "No permission for reload.");
 				return true;
 			}
 			reloadSettings(sender);
 			return true;
 		}
 		else if (label.equals("seamlessflight")){
 			if ((sender instanceof Player) && args.length == 0 || args.length == 1 && flyCmds.contains(cmd)){
 				final Player player = (Player) sender;
 				if (!player.hasPermission(flyMode.rootPerm + ".use")){
 					player.sendMessage(ChatColor.DARK_RED + "No permission for flying.");
 					return true;
 				}
 				boolean nofly = isFlying(player);
 				if ( args.length == 0 ){
 					// toggle 
 					nofly = toggleNoFly(player);
 				} else if (args.length == 1){
 					if (cmd.equals("1") || cmd.equals("on") || cmd.equals("enable") || cmd.equals("start")){
 						nofly = setNoFly(player, false);
 					} else if ( cmd.equals("0") || cmd.equals("off") || cmd.equals("disable") || cmd.equals("stop")){
 						nofly = setNoFly(player, true);
 					}
 				}
 				if (nofly){
 					player.sendMessage(ChatColor.YELLOW+"FLY: "+ChatColor.DARK_RED+"disabled");
 				} else{
 					player.sendMessage(ChatColor.YELLOW+"FLY: "+ChatColor.DARK_GREEN+"enabled");
 				}
 				return true;
 			}
 			return false;
 		}
 		else return false;
 	}
 	
 	public void reloadSettings(CommandSender notify) {
 		String msg;
 		try{
 			this.settings = Settings.readSettings(new File(getDataFolder(), "config.yml"), true);
 			msg = "[SeamlessFlight] Settings reloaded.";
 		}
 		catch(Throwable t){
 			msg = "[SeamlessFlight] Error while loading the config (keep old/defaults), see log file: " + t.getClass().getSimpleName() + "/" + t.getMessage();
 			getServer().getLogger().severe(msg);
 			t.printStackTrace();
 		}
 		if (notify != null){
 			notify.sendMessage(msg);
 		}
 	}
 
 	@Override
 	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
 	{
 		alias = getLabel(command, alias);
 		if (alias.equals("seamlessflight")){
 			if (args.length == 0) return Arrays.asList("on", "off");
 			else if (args.length == 1){
 				final String arg = args[0].trim().toLowerCase();
 				final List<String> cmds = new ArrayList<String>(flyCmds.size());
 				for (final String flyCmd : flyCmds){
 					if (flyCmd.startsWith(arg) || arg.startsWith(flyCmd)) cmds.add(flyCmd);
 				}
 				if (cmds.isEmpty()) cmds.addAll(flyCmds);
 				return cmds;
 			}
 		}
 		return null;
 	}
 	
 	public final boolean isFlying(final Player player) {
 		return flying.containsKey(player.getName().toLowerCase());
 	}
 	
 	public final boolean isFlying(final String playerName) {
 		return flying.containsKey(playerName.toLowerCase());
 	}
 	
 	public final boolean isFlyingLc(final String lcName) {
 		return flying.containsKey(lcName);
 	}
 	
 	/**
 	 * 
 	 * @param player
 	 * @return If disabled.
 	 */
 	public boolean toggleNoFly(final Player player){
 		final FlyConfig fc = flyMode.getFlyConfig(player.getName());
 		if (fc == null) return true;
 		return setNoFly(player, fc.flyState != FlyState.DISABLED);
 	}
 
 	/**
 	 * 
 	 * @param player
 	 * @param nofly
 	 * @return If disabled.
 	 */
 	public boolean setNoFly(final Player player, boolean nofly) {
 		final String lcName = player.getName().toLowerCase();
 		final FlyConfig fc = flyMode.getFlyConfig(lcName);
 		if (fc == null) return true;
 		if(nofly ^ (fc.flyState == FlyState.DISABLED)){
 			fc.setNofly(nofly);
 		}
 		updateFlyState(player);
 		return fc.flyState == FlyState.DISABLED;
 	}
 	
 	/** 
 	 * Update if player is monitored for moving and also adapt allowFlight/flying settings.
 	 * @param player
 	 * @return If flying.
 	 */
 	public boolean updateFlyState(final Player player){
 		final String lcName = player.getName().toLowerCase();
 		final FlyConfig fc = flyConfigs.get(lcName);
 		flyMode.adapt(player, fc);
 		if (fc == null) return false;
 		if (player.getGameMode() == GameMode.CREATIVE){
 			fc.setFlying(false);
 		}
 		// no permission check here - contention period till next check. TODO: always check first move
 		final boolean isFlying = fc.isFlying();
 		if (isFlying) flying.put(lcName, player);
 		else flying.remove(lcName);
 		return isFlying;
 	}
 	
 	/**
 	 * Force a player to fly (if permission is given). Messages the player if started.
 	 * @param player
 	 * @return If successful (only false on permission not given).
 	 */
 	public boolean startFly(final Player player){
 		final FlyConfig fc = flyMode.getFlyConfig(player.getName());
 		if (fc == null) return false;
 		else if (fc.isFlying()) return true;
 		else{
 			fc.setFlying(true);
 			updateFlyState(player);
 			player.sendMessage(ChatColor.YELLOW+"FLY: "+ChatColor.GREEN+"on"); // TODO
 			return true;
 		}
 	}
 	
 	/**
 	 * Force player not to fly. MEssages the player if stopped.
 	 * @param player
 	 * @return If the player was flying.
 	 */
 	public boolean stopFly(final Player player) {
 		final String lcName = player.getName().toLowerCase();
 		final FlyConfig fc = flyConfigs.get(lcName);
 		if (fc == null) return false;
 		if (!fc.isFlying()){
 			if (!takesFallDamage(player)) reset(player);
 			return false;
 		}
 		reset(player);
 		fc.setFlying(false);
 		updateFlyState(player);
 		player.sendMessage(ChatColor.YELLOW+"FLY: "+ChatColor.RED+"off"); // TODO
 		return true;
 	}
 	
 	/**
 	 * Reset violations and similar.
 	 * @param player
 	 */
 	public void reset(Player player) {
 		player.setFallDistance(0f);
 		if (ncpPresent){
 			try{
 				NoCheatPlusHooks.resetViolations(player);
 			} catch (Throwable t){}
 		}
 	}
 
 	/**
 	 * Checks FlyConfig.
 	 * @param player
 	 * @return
 	 */
 	public boolean takesFallDamage(final Player player) {
 		final FlyConfig fc = flyConfigs.get(player.getName().toLowerCase());
 		if (fc == null) return true;
 		if (fc.isFlying()) return false;
 		if (fc.noFallBlock == null) return true;
 		final Location loc = player.getLocation();
 		if (fc.useFallDamage(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) return true;
 		else return false;
 	}
 	
 	private final FlyResult processFly(final Player player, final Location lFrom, final Location lTo, final boolean forceFull){
 		final String lcName = player.getName().toLowerCase();
 		final FlyConfig fc = flyMode.getFlyConfig(lcName);
 		if (fc == null){
 			flying.remove(lcName);
 			final FlyResult res = new FlyResult();
 			res.configChanged = false; // TODO: policy ?
 			res.removeSurvey = true;
 			res.setTo = null;
 			flyMode.adapt(player, fc);
 			return res;
 		}
 		final FlyResult res = flyMode.processMove(player, fc,lFrom, lTo, forceFull);
 		if (res.removeSurvey){
 			if (res.configChanged){
 				if ( player.getGameMode() == GameMode.CREATIVE) player.sendMessage(ChatColor.GRAY+"(Turn off creative-mode to use SeamlessFlight.)");
 				player.sendMessage(ChatColor.YELLOW+"FLY: "+ChatColor.RED+"off"); // TODO
 			}
 			flying.remove(lcName);
 			reset(player);
 			flyMode.adapt(player, fc);
 		} 
 //		if (res.configChanged) ...
 		return res;
 	}
 	
 	/**
 	 * Call back from FlyMode.
 	 * @param user
 	 */
 	public void onToggleSneakCallBack(final String user) {
 		final Player player = getServer().getPlayerExact(user);
 		if (player == null) return;
 		final String lcName = user.toLowerCase();
 		final FlyConfig fc = flyMode.getFlyConfig(lcName);
 		if (fc == null) flying.remove(lcName);
 		else if (fc.isFlying()) flying.put(lcName, player);
 	}
 	
 	/**
 	 * Called by TickTask
 	 */
 	public final void onTick() {
 		if (!usePolling) return;
 		final Player[] players = new Player[flying.size()];
 		flying.values().toArray(players);
 		for (final Player player : players){
 			processFly(player, null, null, false);
 		}
 	}
 	
 	protected void onLeave(final Player player) {
 		final String lcName = player.getName().toLowerCase();
 		flying.remove(lcName);
 		final FlyConfig fc = flyConfigs.get(lcName);
 		if (fc == null || fc.flyState == FlyState.DISABLED){
 			flyConfigs.remove(lcName);
 		}
 	}
 	
 	/////////////////////
 	// listener methods
 	/////////////////////
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void processPlayerJoin(final PlayerJoinEvent event){
 		updateFlyState(event.getPlayer());
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public  final void processPlayerToggleSneak(final PlayerToggleSneakEvent event) {
 		final Player player = event.getPlayer();
 		final boolean isSneaking = event.isSneaking();
 		flyMode.actionChecker.onToggleAction(player.getName().toLowerCase(), isSneaking);
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public final void onPlayerMove(final PlayerMoveEvent event) {
 		// TODO: priority
 		final Player player = event.getPlayer();
 		if (!flying.containsKey(player.getName().toLowerCase())) return;
 		final FlyResult res = processFly(player,  event.getFrom(), event.getTo(), true);
 		if (res.setTo!=null) event.setTo(res.setTo);
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerKick(final PlayerKickEvent event){
 		onLeave(event.getPlayer());
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerQuit(final PlayerQuitEvent event){
 		onLeave(event.getPlayer());
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerChangedWorld(final PlayerChangedWorldEvent event){
 		stopFly(event.getPlayer());
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
 	public void onPlayerTeleport(final PlayerTeleportEvent event){
 		stopFly(event.getPlayer());
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
 	public void onPlayerDeath(final PlayerDeathEvent event){
 		stopFly(event.getEntity());
 	}
 	
 	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onEntityDamage(final EntityDamageEvent event){
 		// 
 		if (event instanceof EntityDamageByEntityEvent){
 			onEntityDamage((EntityDamageByEntityEvent) event);
 			return;
 		}
 		// Further only process fall damage:
 		if (event.getCause() != DamageCause.FALL) return;
 		final Entity entity = event.getEntity();
 		if (!(entity instanceof Player)) return;
 		// currently also call for cancelled events for resetting no-fall block.
 		if (!takesFallDamage((Player) entity)){
 			reset((Player) entity);
 			event.setCancelled(true);
 		}
 	}
 
 	/**
 	 * Not an event handler (!). Called form EntityDamageEvent handler.
 	 * @param event
 	 */
 	private void onEntityDamage(final EntityDamageByEntityEvent event)
 	{
 		// Don't rule out damage reasons at present.
 		final Entity damaged = event.getEntity();
 		final Entity damager = event.getDamager();
 		if (damager instanceof Projectile){
 			final Entity shooter = ((Projectile) damager).getShooter();
 			if (!getCombatSymmetrySettings(shooter, damaged).allowProjectiles){
 				event.setCancelled(true);
 			}
 		}
 		else{
 			// Not a projectile.
 			if (!getCombatSymmetrySettings(damager, damaged).allowCloseCombat){
 				event.setCancelled(true);
 			}
 		}
 		
 	}
 	
 	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onPotionSplash(final PotionSplashEvent event){
 		final Entity thrower = event.getEntity();
 		final List<LivingEntity> rem = new LinkedList<LivingEntity>();
 		final Collection<LivingEntity> affected = event.getAffectedEntities();
 		final boolean isPlayer,  isFlying;
 		if (thrower instanceof Player){
 			isPlayer = true;
 			isFlying = isFlying((Player) thrower);
 		}
 		else{
 			isPlayer = isFlying = false;
 		}
 		for (final LivingEntity other : event.getAffectedEntities()){
 			if (!getCombatSymmetrySettings(isPlayer, isFlying, other).allowPotions){
 				rem.add(other);
 			}
 		}
 		if (!rem.isEmpty()) affected.removeAll(rem);
 	}
 	
 	public final CombatSymmetrySettings getCombatSymmetrySettings(final Entity damager, final Entity damaged){
 		final boolean isPlayer,  isFlying;
 		if (damager instanceof Player){
 			isPlayer = true;
 			isFlying = isFlying((Player) damager);
 		}
 		else{
 			isPlayer = isFlying = false;
 		}
 		return getCombatSymmetrySettings(isPlayer, isFlying, damaged);
 	}
 	
 	private final CombatSymmetrySettings getCombatSymmetrySettings(final boolean isPlayer, final boolean isFlying, final Entity damaged){
 		final boolean isPlayer2,  isFlying2;
 		if (damaged instanceof Player){
 			isPlayer2 = true;
 			isFlying2 = isFlying((Player) damaged);
 		}
 		else{
 			isPlayer2 = isFlying2 = false;
 		}
 		return settings.combat.getSymmetrySettings(CombatSymmetry.getSymmetry(isPlayer, isFlying, isPlayer2, isFlying2));
 	}
 	
 }
