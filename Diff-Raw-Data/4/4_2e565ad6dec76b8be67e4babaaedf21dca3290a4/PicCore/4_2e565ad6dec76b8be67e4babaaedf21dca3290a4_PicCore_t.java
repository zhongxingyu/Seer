 package me.asofold.bukkit.pic.core;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import me.asofold.bukkit.pic.config.Settings;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 /**
  * Core functionality.
  * @author mc_dev
  *
  */
 public final class PicCore{
 
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
 
 	/**
 	 * Quit, kick.
 	 * @param player
 	 */
 	public final void checkOut(final Player player) {
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
 			pp.checkOut();
			players.remove(pp.playerName); // TODO: maybe hold data longer.
 		}
		
 	}
 
 	public boolean reload(File dataFolder) {
 		Settings settings = Settings.load(dataFolder);
 		if (settings != null){
 			applySettings(settings);
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
 	
 
 	public final void renderBlind(final PicPlayer pp, final Set<String> names) {
 		final Player player = pp.bPlayer;
 		for (final String name : names){
 			final PicPlayer opp = players.get(name);
 			// if (opp == null) continue; // ERROR
 			if (player.canSee(opp.bPlayer)) player.hidePlayer(opp.bPlayer);
 			if (opp.bPlayer.canSee(player)) opp.bPlayer.hidePlayer(player);
 		}
 	}
 	
 	public final void renderSeen(final PicPlayer pp, final Set<String> names) {
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
 		final PicPlayer pp =  getPicPlayer(player);
 		
 		final String world = to.getWorld().getName();
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
 		
 		// Remove cubes that are too far.
 		pp.checkCubes(settings.distCube);
 		
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
 			server = new CubeServer(this, settings.cubeSize);
 			cubeServers.put(world, server);
 		}
 		return server;
 	}
 	
 	public void clear() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	private void applySettings(Settings settings) {
 		this.settings = settings;
 		// ...
 	}
 
 }
