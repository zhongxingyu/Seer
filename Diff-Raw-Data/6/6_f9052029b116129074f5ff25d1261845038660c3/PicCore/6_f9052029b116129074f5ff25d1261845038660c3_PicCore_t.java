 package me.asofold.bukkit.pic.core;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import me.asofold.bukkit.pic.config.Settings;
 import me.asofold.bukkit.pic.stats.Stats;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 /**
  * Core functionality.
  * @author mc_dev
  *
  */
 public final class PicCore{
 	
 	
 	
 	static final Stats stats = new Stats("[PIC]");
 	
 	static final Integer idPPCubes = stats.getNewId("pp_ncubes");
 	static final Integer idPPRemCubes= stats.getNewId("pp_remcubes");
 	static final Integer idPPSeen = stats.getNewId("pp_insight");
 	static final Integer idPPRemove = stats.getNewId("pp_offsight");
 	
 	static{
 		stats.setLogStats(false);
 		stats.setShowRange(true);
 	}
 	
 	/**
 	 * Settings.
 	 */
 	private Settings settings = new Settings();
 	
 	/**
 	 * World specific CubeServer.
 	 */
 	private final Map<String, CubeServer> cubeServers = new HashMap<String, CubeServer>(53);
 	 
 	/**
 	 * Player specific data / lookup.
 	 */
 	private final Map<String, PicPlayer> players = new HashMap<String, PicPlayer>(517);
 	
 	private File dataFolder = null;
 	
 	private boolean enabled = true;
 
 	/**
 	 * Quit, kick.
 	 * @param player
 	 */
 	public final void checkOut(final Player player) {
 		if (!enabled) return;
 		final String playerName = player.getName();
 		final PicPlayer pp = players.get(playerName);
 		if (pp == null){ // contract ?
 			for (final PicPlayer opp : players.values()){
 				if (opp.playerName.equals(playerName)) continue;
 				if (opp.bPlayer.canSee(player)) opp.bPlayer.hidePlayer(player);
 				if (player.canSee(opp.bPlayer)) player.hidePlayer(opp.bPlayer);
 			}
 		}
 		else{
 			if (pp.world != null) getCubeServer(pp.world).players.remove(player);
 			renderBlind(pp, pp.checkOut());
 			players.remove(pp.playerName); // TODO: maybe hold data longer.
 		}
 		
 	}
 
 	public final boolean reload() {
 		final File file = new File(dataFolder, "config.yml");
 		final Settings settings = Settings.load(file);
 		if (settings != null){
 			applySettings(settings);
 			// Consider logging that settings were reloaded.
 			return true;
 		}
 		else return false;
 	}
 
 	/**
 	 * Join or respawn.
 	 * @param player
 	 * @param location 
 	 */
 	public final void checkIn(final Player player, Location location) {
 		checkOut(player);
 		check(player, player.getLocation());
 	}
 	
 	/**
 	 * Get PicPlayer, put to internals, if not present.
 	 * @param player
 	 * @return
 	 */
 	private final PicPlayer getPicPlayer(final Player player){
 		final String name = player.getName();
 		final PicPlayer pp = players.get(name);
 		if (pp != null) return pp;
 		final PicPlayer npp = new PicPlayer(player);
 		players.put(name, npp);
 		return npp;
 	}
 	
 
 	public final void renderBlind(final PicPlayer pp, final Collection<String> names) {
 		final Player player = pp.bPlayer;
 		for (final String name : names){
 			final PicPlayer opp = players.get(name);
 			// if (opp == null) continue; // ERROR
 			if (player.canSee(opp.bPlayer)) player.hidePlayer(opp.bPlayer);
 			if (opp.bPlayer.canSee(player)) opp.bPlayer.hidePlayer(player);
 		}
 	}
 	
 	public final void renderSeen(final PicPlayer pp, final Collection<String> names) {
 		final Player player = pp.bPlayer;
 		for (final String name : names){
 			final PicPlayer opp = players.get(name);
 			// if (opp == null) continue; // ERROR
 			if (!player.canSee(opp.bPlayer)) player.showPlayer(opp.bPlayer);
 			if (!opp.bPlayer.canSee(player)) opp.bPlayer.showPlayer(player);
 		}
 	}
 
 	/**
 	 * Lighter check: Use this for set up players.
 	 * @param player
 	 * @param to NOT NULL
 	 */
 	public final void check(final Player player, final Location to) {
 		if (!enabled) return;
 		final PicPlayer pp =  getPicPlayer(player);
 		final String world = to.getWorld().getName();
 		if (settings.ignoreWorlds.contains(world)){
 			// Detect world change (!):
 			if (pp.world == null || !pp.world.equals(world)){
 				final CubeServer server = getCubeServer(world);
 				if (!server.players.isEmpty()) renderSeen(pp, server.players);
 				server.players.add(pp.playerName);
 				pp.world = world;
 				pp.tsLoc = 0; // necessary.
 			}
 			// else: simply ignore.
 			return;
 		}
 		final int x = to.getBlockX();
 		final int y = to.getBlockY();
 		final int z = to.getBlockZ();
 		final long ts = System.currentTimeMillis();
 		
 		// Check if to set the postion:
 		if (pp.cubes.isEmpty()){
 			// Set new.
 		}
 		else if (settings.durExpireData > 0  && ts - pp.tsLoc > settings.durExpireData){
 			// Expired, set new.
 		}
 		else if (!world.equals(pp.world)){
 			// World change, invalidate player.
 			if (pp.world == null){
 				getCubeServer(world).players.add(pp.playerName);
 			}
 			else{
 				// Check if it was an ignored world:
 				if (settings.ignoreWorlds.contains(pp.world)){
 					final CubeServer oldServer = cubeServers.get(pp.world);
 					if (oldServer != null && !oldServer.players.isEmpty()){ // null is unlikely.
 						oldServer.players.remove(pp.playerName);
 						renderBlind(pp, oldServer.players);
 					}
 				}
 			}
 			pp.checkOut();
 		}
 		else if (pp.inRange(x, y, z, settings.distLazy)){
 			// still in range, quick return !
 			return;
 		}
 		else{
 			// Out of range set new !
 		}
 		
 		// Set position.
 		pp.tsLoc = ts;
 		pp.world = world;
 		pp.x = x;
 		pp.y = y;
 		pp.z = z;
 		
 		// Add player to all cubes !
 		getCubeServer(world).update(pp, settings.distCube);
 	}
 
 	/**
 	 * Get the cube server for the world, will create it if not yet existent.
 	 * @param world Exact case.
 	 * @return
 	 */
 	private final CubeServer getCubeServer(final String world) {
 		CubeServer server = cubeServers.get(world);
 		if (server == null){
 			server = new CubeServer(world, this, settings.cubeSize);
 			cubeServers.put(world, server);
 		}
 		return server;
 	}
 	
 	public final Stats getStats(){
 		return stats;
 	}
 	
 	private final void applySettings(final Settings settings) {
 		// Later: maybe try for a lazy transition or a hard one depending on changes.
 		this.settings = settings;
 		cleanup();
 		enabled = settings.enabled;
 	}
 
 	/**
 	 * Remove all players, remove all data, check in all players again.
 	 */
 	public final void cleanup() {
 		clear(true);
 		checkAllOnlinePlayers();
 	}
 	
 	public final void checkAllOnlinePlayers() {
 		if (!enabled) return;
 		for (final Player player : Bukkit.getOnlinePlayers()){
 			check(player, player.getLocation());
 		}
 	}
 
 	public final void clear(final boolean blind){
 		removeAllPlayers(blind);
 		for (final CubeServer server : cubeServers.values()){
 			server.clear();
 		}
 		cubeServers.clear();
 	}
 
 	/**
 	 * 
 	 * @param blind Render players blind or let all see all again (very expensive).
 	 */
 	private final void removeAllPlayers(final boolean blind) {
 		if (blind){
 			// "Efficient" way: only render those blind, that are seen.
 			for (final PicPlayer pp : players.values()){
 				renderBlind(pp, pp.checkOut());
 			}
 		}
 		else{
 			// Costly: basically quadratic time all vs. all.
 			final Player[] online = Bukkit.getOnlinePlayers();
 			for (final PicPlayer pp : players.values()){
 				pp.checkOut(); // Ignore return value.
 				for (final Player other : online){
 					if (!other.canSee(pp.bPlayer)) other.showPlayer(pp.bPlayer);
 					if (!pp.bPlayer.canSee(other)) pp.bPlayer.showPlayer(other);
 				}
 			}
 		}
 		players.clear();
 	}
 
 	public final String getInfoMessage() {
 		final StringBuilder b = new StringBuilder();
 		b.append("[PIC][INFO] PlayersInCubes is ");
 		b.append((enabled ? "enabled.":"DISABLED."));
 		b.append(" cube.size=" + settings.cubeSize);
 		b.append(" cube.distance=" + settings.distCube);
 		b.append(" lazy.distance=" + settings.distLazy);
 		b.append(" lazy.lifetime=" + (settings.durExpireData / 1000));
 //		b.append(" | ");
 		b.append(" | (More: /pic stats)");
 		return b.toString();
 	}
 
 	/**
 	 * This will alter and save the settings, unless no change is done.
 	 * @param enabled
 	 * @return If this was a state change.
 	 */
 	public final boolean setEnabled(final boolean enabled) {
		File file = new File(dataFolder, "config.yml");
 		if (!(enabled ^ this.enabled)){
 			if (!file.exists()) settings.save(file);
 			return false;
 		}
 		this.enabled = enabled;
 		if (enabled) checkAllOnlinePlayers();
 		else clear(false); // Renders all visible.
 		settings.enabled = this.enabled;
		settings.save(file);
 		return true;
 	}
 
 	public final void setDataFolder(final File dataFolder) {
 		this.dataFolder = dataFolder;
 	}
 
 }
