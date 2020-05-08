 package com.martinbrook.tesseractuhc;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.EnderDragon;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Ghast;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.MagmaCube;
 import org.bukkit.entity.Monster;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Slime;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.potion.PotionEffect;
 
 import com.martinbrook.tesseractuhc.countdown.BorderCountdown;
 import com.martinbrook.tesseractuhc.countdown.MatchCountdown;
 import com.martinbrook.tesseractuhc.countdown.PVPCountdown;
 import com.martinbrook.tesseractuhc.notification.UhcNotification;
 import com.martinbrook.tesseractuhc.startpoint.LargeGlassStartPoint;
 import com.martinbrook.tesseractuhc.startpoint.SmallGlassStartPoint;
 import com.martinbrook.tesseractuhc.startpoint.UhcStartPoint;
 import com.martinbrook.tesseractuhc.util.FileUtils;
 import com.martinbrook.tesseractuhc.util.TeleportUtils;
 import com.martinbrook.tesseractuhc.util.MatchUtils;
 
 public class UhcMatch {
 
 	private World startingWorld;
 	private HashMap<Integer, UhcStartPoint> startPoints = new HashMap<Integer, UhcStartPoint>();
 	private Location lastNotifierLocation;
 	private Location lastDeathLocation;
 	private Location lastEventLocation;
 	private Location lastLogoutLocation;
 
 	private ArrayList<String> chatScript;
 	private Boolean chatMuted = false;
 	private Boolean permaday = false;
 	private int permadayTaskId;
 	
 	private ArrayList<UhcStartPoint> availableStartPoints = new ArrayList<UhcStartPoint>();
 	private HashMap<String, UhcPlayer> uhcPlayers = new HashMap<String, UhcPlayer>(32);
 	private HashMap<String, UhcTeam> uhcTeams = new HashMap<String, UhcTeam>(32);
 	
 	private ArrayList<String> launchQueue = new ArrayList<String>();
 	public static String DEFAULT_MATCHDATA_FILE = "uhcmatch.yml";
 	public static int GOLD_LAYER = 32;
 	public static int DIAMOND_LAYER = 16;
 	private int playersInMatch = 0;
 	private int teamsInMatch = 0;
 	private Calendar matchStartTime;
 	private int matchTimer = -1;
 	private ArrayList<Location> calculatedStarts = null;
 	private boolean pvp = false;
 	private int spawnKeeperTask = -1;
 	private YamlConfiguration md; // Match data
 	private TesseractUHC plugin;
 	private Server server;
 	private Configuration defaults;
 	private ItemStack[] bonusChest = new ItemStack[27];
 	private MatchPhase matchPhase = MatchPhase.PRE_MATCH;
 	private MatchCountdown matchCountdown;
 	private BorderCountdown borderCountdown;
 
 	
 	public UhcMatch(TesseractUHC plugin, World startingWorld, Configuration defaults) {
 
 		this.startingWorld = startingWorld;
 		this.plugin = plugin;
 		this.server = plugin.getServer();
 		this.defaults = defaults;
 		
 		this.loadMatchParameters();
 		this.setPermaday(true);
 		this.setPVP(false);
 		this.setVanish();
 		this.enableSpawnKeeper();
 		
 	}
 	
 	/**
 	 * Load match data from the default file. If it does not exist, load defaults.
 	 */
 	public void loadMatchParameters() { 
 		try {
 			md = YamlConfiguration.loadConfiguration(FileUtils.getDataFile(startingWorld.getWorldFolder(), DEFAULT_MATCHDATA_FILE, true));
 
 		} catch (Exception e) {
 			md = new YamlConfiguration();
 		}
 		
 		setDefaultMatchParameters();
 		
 		// Load start points
 		startPoints.clear();
 		availableStartPoints.clear();
 		
 		List<String> startData = md.getStringList("starts");
 		for (String startDataEntry : startData) {
 			String[] data = startDataEntry.split(",");
 			if (data.length == 4) {
 				try {
 					int n = Integer.parseInt(data[0]);
 					double x = Double.parseDouble(data[1]);
 					double y = Double.parseDouble(data[2]);
 					double z = Double.parseDouble(data[3]);
 					UhcStartPoint sp = createStartPoint (n, startingWorld, x, y, z, false);
 					if (sp == null) {
 						adminBroadcast("Duplicate start point: " + n);
 
 					}
 				} catch (NumberFormatException e) {
 					adminBroadcast("Bad start point definition in match data file: " + startDataEntry);
 				}
 
 			} else {
 				adminBroadcast("Bad start point definition in match data file: " + startDataEntry);
 			}
 		}
 		
 		// Load saved bonus chest
 		List<?> data = md.getList("bonuschest");
 		
 		if (data != null) {
 		
 			for (int i = 0; i < 27; i++) {
 				Object o = data.get(i);
 				if (o != null && o instanceof ItemStack)
 					bonusChest[i] = (ItemStack) o;
 			}
 		}
 	}
 	
 	
 	/**
 	 * Set up a default matchdata object
 	 */
 	private void setDefaultMatchParameters() {
 		
 		Map<String, Object> mapDefaults = defaults.getValues(true);
 		for (Map.Entry<String, Object> m : mapDefaults.entrySet()) {
 			if (!md.contains(m.getKey())) {
 				md.set(m.getKey(), m.getValue());
 			}
 		}
 		
 		this.saveMatchParameters();
 	}
 
 	
 	/**
 	 * Save start points to the default file
 	 * 
 	 * @return Whether the operation succeeded
 	 */
 	public void saveMatchParameters() {
 		ArrayList<String> startData = new ArrayList<String>();
 		for (UhcStartPoint sp : startPoints.values()) {
 			startData.add(sp.getNumber() + "," + sp.getX() + "," + sp.getY() + "," + sp.getZ());
 		}
 		
 		md.set("starts",startData);
 		
 		try {
 			md.save(FileUtils.getDataFile(startingWorld.getWorldFolder(), DEFAULT_MATCHDATA_FILE, false));
 		} catch (IOException e) {
 			adminBroadcast(TesseractUHC.ALERT_COLOR + "Warning: Could not save match data");
 		}
 	}
 
 
 	/**
 	 * Reset all match parameters to default values
 	 */
 	public void resetMatchParameters() {
 		startPoints.clear();
 		availableStartPoints.clear();
 		md = new YamlConfiguration();
 		this.setDefaultMatchParameters();
 	}
 
 	/**
 	 * Send a message to all ops
 	 * 
 	 * @param string The message to be sent
 	 */
 	public void adminBroadcast(String string) {
 		broadcast(string,Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
 	}
 	
 	/**
 	 * Send a message to all players on the server
 	 * 
 	 * @param string The message to be sent
 	 */
 	public void broadcast(String string) {
 		broadcast(string,Server.BROADCAST_CHANNEL_USERS);
 	}
 
 	/**
 	 * Send a message to specific players on the server
 	 * 
 	 * @param string The message to be sent
 	 * @param permission The permission level to send the message to
 	 */
 	private void broadcast(String string, String permission) {
 		server.broadcast(string, permission);
 	}
 	
 	/**
 	 * Set time to midday, to keep permaday in effect.
 	 */
 	private void keepPermaday() {
 		this.startingWorld.setTime(6000);
 	}
 
 	/**
 	 * Enables / disables PVP on overworld
 	 * 
 	 * @param pvp Whether PVP is to be allowed
 	 */
 	public void setPVP(boolean pvp) {
 		this.pvp = pvp;
 		startingWorld.setPVP(pvp);
 
 		adminBroadcast(TesseractUHC.OK_COLOR + "PVP has been " + (pvp ? "enabled" : "disabled") + "!");
 	
 	}
 	
 	/**
 	 * @return Whether PVP is enabled
 	 */
 	public boolean getPVP() {
 		return this.pvp;
 	}
 
 	/**
 	 * Enables / disables permaday
 	 * 
 	 * @param p whether permaday is to be on or off
 	 */
 	public void setPermaday(boolean p) {
 		if (p == permaday) return;
 		
 		this.permaday=p;
 	
 		adminBroadcast(TesseractUHC.OK_COLOR + "Permaday has been " + (permaday ? "enabled" : "disabled") + "!");
 		
 		
 		if (permaday) {
 			startingWorld.setTime(6000);
 			permadayTaskId = server.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
 				public void run() {
 					keepPermaday();
 				}
 			}, 1200L, 1200L);
 			
 		} else {
 			server.getScheduler().cancelTask(permadayTaskId);
 		}
 	}
 	
 
 	/**
 	 * @return Whether permaday is enabled
 	 */
 	public boolean getPermaday() {
 		return this.permaday;
 	}
 
 
 
 	/**
 	 * Try to find a start point from a user-provided search string.
 	 * 
 	 * @param searchParam The string to search for - a player name, or a start number may be sent
 	 * @return The start point, or null if not found.
 	 */
 	public UhcStartPoint findStartPoint(String searchParam) {
 		UhcPlayer up = this.getUhcPlayer(searchParam);
 		if (up != null) {
 			// Argument matches a player
 			return up.getStartPoint();
 			
 		} else {
 			try {
 				int i = Integer.parseInt(searchParam);
 				return startPoints.get(i);
 			} catch (Exception e) {
 				return null;
 			}
 		}
 		
 	}
 
 	/**
 	 * Set a death location for teleporters
 	 * 
 	 * @param l The location to be stored
 	 */
 	public void setLastDeathLocation(Location l) {
 		lastDeathLocation = l;
 		lastEventLocation = l;
 	}
 
 	/**
 	 * Set a notification location for teleporters
 	 * 
 	 * @param l The location to be stored
 	 */
 	private void setLastNotifierLocation(Location l) {
 		lastNotifierLocation = l;
 		lastEventLocation = l;
 	}
 
 	/**
 	 * Set a logout location for teleporters
 	 * 
 	 * @param l The location to be stored
 	 */
 	public void setLastLogoutLocation(Location l) {
 		lastLogoutLocation = l;
 	}
 	
 
 	
 	/**
 	 * Remove all hostile mobs in the overworld
 	 */
 	public void butcherHostile() {
 		for (Entity entity : startingWorld.getEntitiesByClass(LivingEntity.class)) {
 			if (entity instanceof Monster || entity instanceof MagmaCube || entity instanceof Slime || entity instanceof EnderDragon
 					|| entity instanceof Ghast)
 				entity.remove();
 		}
 	}
 	
 	/**
 	 * Heal, feed, clear XP, inventory and potion effects of the given player
 	 * 
 	 * @param p The player to be renewed
 	 */
 	public void renew(Player p) {
 		heal(p);
 		feed(p);
 		clearXP(p);
 		clearPotionEffects(p);
 		clearInventory(p);
 	}
 
 
 	/**
 	 * Heal the given player
 	 * 
 	 * @param p The player to be healed
 	 */
 	public void heal(Player p) {
 		p.setHealth(20);
 	}
 
 	/**
 	 * Feed the given player
 	 * 
 	 * @param p The player to be fed
 	 */
 	public void feed(Player p) {
 		p.setFoodLevel(20);
 		p.setExhaustion(0.0F);
 		p.setSaturation(5.0F);
 	}
 
 	/**
 	 * Reset XP of the given player
 	 * 
 	 * @param p The player
 	 */
 	public void clearXP(Player p) {
 		p.setTotalExperience(0);
 		p.setExp(0);
 		p.setLevel(0);
 	}
 
 	/**
 	 * Clear potion effects of the given player
 	 * 
 	 * @param p The player
 	 */
 	public void clearPotionEffects(Player p) {
 		for (PotionEffect pe : p.getActivePotionEffects()) {
 			p.removePotionEffect(pe.getType());
 		}
 	}
 
 	/**
 	 * Clear inventory and ender chest of the given player
 	 * 
 	 * @param player
 	 */
 	public void clearInventory(Player player) {
 		PlayerInventory i = player.getInventory();
 		i.clear();
 		i.setHelmet(null);
 		i.setChestplate(null);
 		i.setLeggings(null);
 		i.setBoots(null);
 		
 		player.getEnderChest().clear();
 		
 	}
 	
 	/**
 	 * Start the match
 	 * 
 	 * Butcher hostile mobs, turn off permaday, turn on PVP, put all players in survival and reset all players.
 	 */
 	public void startMatch() {
 		this.matchCountdown = null;
 		matchPhase = MatchPhase.MATCH;
 		startingWorld.setTime(0);
 		butcherHostile();
		for (Player p : server.getOnlinePlayers()) {
			if (p.getGameMode() != GameMode.CREATIVE) {
 				feed(p);
 				clearXP(p);
 				clearPotionEffects(p);
 				heal(p);
 				p.setGameMode(GameMode.SURVIVAL);
 			}
 		}
 		setPermaday(false);
 		startMatchTimer();
 		setVanish();
 		
 		// Set up pvp countdown
 		if (getNopvp() > 0) {
 			new PVPCountdown(getNopvp(), plugin, this);
 		} else {
 			setPVP(true);
 		}
 	}
 	
 	/**
 	 * End the match
 	 * 
 	 * Announce the total match duration
 	 */
 	public void endMatch() {
 		announceMatchTime(true);
 		stopMatchTimer();
 		matchPhase = MatchPhase.POST_MATCH;
 		// Put all players into creative
 		for (Player p : server.getOnlinePlayers()) p.setGameMode(GameMode.CREATIVE);
 		setVanish();
 
 	}
 	
 
 	public boolean worldReduce(int nextRadius) {
 		return MatchUtils.setWorldRadius(startingWorld,nextRadius);
 	}
 	
 	
 	/**
 	 * Starts the match timer
 	 */
 	private void startMatchTimer() {
 		matchStartTime = Calendar.getInstance();
 		matchTimer = server.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
 			public void run() {
 				announceMatchTime(false);
 			}
 		}, 36000L, 36000L);
 	}
 	
 	/**
 	 * Stops the match timer
 	 */
 	private void stopMatchTimer() {
 		if (matchTimer != -1) {
 			server.getScheduler().cancelTask(matchTimer);
 		}
 	}
 	
 	/**
 	 * Announce the current match time in chat
 	 * 
 	 * @param precise Whether to give a precise time (00:00:00) instead of (xx minutes)
 	 */
 	public void announceMatchTime(boolean precise) {
 		broadcast(TesseractUHC.MAIN_COLOR + "Match time: " + TesseractUHC.SIDE_COLOR + MatchUtils.formatDuration(matchStartTime, Calendar.getInstance(), precise));
 	}
 	
 
 	/**
 	 * Plays a chat script
 	 * 
 	 * @param filename The file to read the chat script from
 	 * @param muteChat Whether other chat should be muted
 	 */
 	public void playChatScript(String filename, boolean muteChat) {
 		if (muteChat) this.setChatMuted(true);
 		chatScript = FileUtils.readFile(filename);
 		if (chatScript != null)
 			continueChatScript();
 	}
 	
 	/**
 	 * Output next line of current chat script, unmuting the chat if it's finished.
 	 */
 	private void continueChatScript() {
 		broadcast(ChatColor.GREEN + chatScript.get(0));
 		chatScript.remove(0);
 		if (chatScript.size() > 0) {
 			server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 				public void run() {
 					continueChatScript();
 				}
 			}, 30L);
 		} else {
 			this.setChatMuted(false);
 			chatScript = null;
 		}
 		
 	}
 	
 	/**
 	 * Get all players currently registered with the game
 	 * 
 	 * @return All registered players
 	 */
 	public Collection<UhcPlayer> getUhcPlayers() {
 		return uhcPlayers.values();
 	}
 	
 	private UhcTeam createTeam(String identifier, String name, UhcStartPoint startPoint) {
 		// Fail if team exists
 		if (existsTeam(identifier)) return null;
 		
 		UhcTeam team = new UhcTeam(identifier, name, startPoint);
 		uhcTeams.put(identifier.toLowerCase(), team);
 		return team;
 	}
 	
 	/**
 	 * Create a new player and add them to the game
 	 * 
 	 * @param name The player's name
 	 * @param sp The player's start point
 	 * @return The newly created player, or null if they already existed
 	 */
 	private UhcPlayer createPlayer(String name, UhcTeam team) {
 		// Fail if player exists
 		if (existsUhcPlayer(name)) return null;
 		
 		UhcPlayer up = new UhcPlayer(name, team);
 		team.addPlayer(up);
 		uhcPlayers.put(name.toLowerCase(), up);
 		return up;
 	}
 	
 
 	
 	/**
 	 * Check if a player exists
 	 * 
 	 * @param name Player name to check (case insensitive)
 	 * @return Whether the player exists
 	 */
 	public boolean existsUhcPlayer(String name) {
 		return uhcPlayers.containsKey(name.toLowerCase());
 	}
 	
 	/**
 	 * Check if a team exists
 	 * 
 	 * @param identifier The team identifier to check (case-insensitive)
 	 * @return Whether the team exists
 	 */
 	public boolean existsTeam(String identifier) {
 		return uhcTeams.containsKey(identifier.toLowerCase());
 	}
 
 	/**
 	 * Get a specific UhcTeam by identifier
 	 * 
 	 * @param name The exact name of the player to be found  (case insensitive)
 	 * @return The UhcPlayer, or null if not found
 	 */
 	public UhcTeam getTeam(String identifier) {
 		return uhcTeams.get(identifier.toLowerCase());
 	}
 	
 	/**
 	 * Get a specific UhcPlayer by name
 	 * 
 	 * @param name The exact name of the player to be found  (case insensitive)
 	 * @return The UhcPlayer, or null if not found
 	 */
 	public UhcPlayer getUhcPlayer(String name) {
 		return uhcPlayers.get(name.toLowerCase());
 	}
 
 	
 	/**
 	 * Get a specific UhcPlayer matching the given Bukkit Player
 	 * 
 	 * @param playerToGet The Player to look for
 	 * @return The UhcPlayer, or null if not found
 	 */
 	public UhcPlayer getUhcPlayer(Player playerToGet) {
 		return getUhcPlayer(playerToGet.getName());
 	}
 	
 	/**
 	 * Add the team as detailed, and assign them a start point
 	 * 
 	 * @param identifier The team's short identifier
 	 * @param name The full name of the team
 	 * @return success or failure
 	 */
 	public boolean addTeam(String identifier, String name) {
 		// Check that there are available start points
 		if (!roomForAnotherTeam()) return false;
 		
 		// Check that the team doesn't exist already 
 		if (existsTeam(identifier)) return false;
 		
 		// Get them a start point
 		Random rand = new Random();
 		UhcStartPoint start = availableStartPoints.remove(rand.nextInt(availableStartPoints.size()));
 		
 		// Create the player
 		UhcTeam team = createTeam(identifier, name, start);
 		start.setTeam(team);
 		teamsInMatch++;
 		
 		start.makeSign();
 		start.fillChest(bonusChest);
 
 		return true;
 	}
 	
 	/**
 	 * Checks if there is room for another team to be created
 	 * 
 	 * @return Whether there is another start point
 	 */
 	public boolean roomForAnotherTeam() {
 		return (availableStartPoints.size()>0);
 	}
 
 	/**
 	 * Add the supplied player to the specified team
 	 * 
 	 * @param p The player to add
 	 * @param teamIdentifier The team to add them to
 	 * @return success or failure
 	 */
 	public boolean addPlayer(Player p, String teamIdentifier) {
 		// If player is op, fail
 		if (p.isOp()) return false;
 		
 		// If player already exists, fail 
 		if (existsUhcPlayer(p.getName())) return false;
 		
 		// Get the team
 		UhcTeam team = getTeam(teamIdentifier);
 		
 		// If team doesn't exist, fail
 		if (team == null) return false;
 		
 		// Create the player
 		UhcPlayer up = createPlayer(p.getName(), team);
 
 		// If player wasn't created, fail
 		if (up == null) return false;
 		
 		playersInMatch++;
 		return true;
 	}
 	
 	/**
 	 * Add the supplied player as a team of one, creating the team and assigning it a start point
 	 * 
 	 * @param p The player to add
 	 * @return success or failure
 	 */
 	public boolean addSoloPlayer (Player p) {
 		// If player is op, fail
 		if (p.isOp()) return false;
 		
 		// If player already exists, fail 
 		if (existsUhcPlayer(p.getName())) return false;
 		
 		// Create a team of one for the player
 		String teamName = p.getName(); 
 		if (!addTeam(teamName, teamName)) return false;
 		
 		// Add the new player to the team of one, and return the result
 		return addPlayer(p, teamName);
 		
 	}
 	
 	
 	/**
 	 * Launch the specified player only
 	 * 
 	 * @param p The UhcPlayer to be launched
 	 * @return success or failure
 	 */
 	public boolean launch(UhcPlayer up) {
 
 		// If player already launched, ignore
 		if (up.isLaunched()) return false;
 		
 		// Get the player
 		Player p = server.getPlayer(up.getName());
 		
 		// If player not online, return
 		if (p == null) return false;
 		
 		
 		sendToStartPoint(p);
 		
 		up.setLaunched(true);
 
 		return true;
 
 
 		
 	}
 	
 	/**
 	 * Re-teleport the specified player
 	 * 
 	 * @param p The player to be relaunched
 	 */
 	public boolean sendToStartPoint(Player p) {
 		UhcPlayer up = getUhcPlayer(p);
 		if (up == null) return false;
 		
 		// Teleport the player to the start point
 		p.setGameMode(GameMode.ADVENTURE);
 		TeleportUtils.doTeleport(p, up.getStartPoint().getLocation());
 		renew(p);
 				
 		return true;
 	}
 	
 	/**
 	 * Remove the given player, removing them from the match, and their team.
 	 * 
 	 * The player will be teleported back to spawn if they are still on the server
 	 * 
 	 * @param name The player to be removed
 	 * @return Whether the removal succeeded
 	 */
 	public boolean removePlayer(String name) {
 		UhcPlayer up = uhcPlayers.remove(name.toLowerCase());
 		Player p = server.getPlayer(name);
 		
 		if (up != null) {
 			// Remove them from their team
 			up.getTeam().removePlayer(up);
 			playersInMatch--;
 			
 			// If match is ffa, also remove the empty team
 			if (isFFA())
 				this.removeTeam(name);
 			
 			
 			if (matchPhase == MatchPhase.MATCH) {
 				broadcast(ChatColor.GOLD + up.getName() + " has left the match");
 				announcePlayersRemaining();
 			}
 		}
 		
 		// Teleport the player if possible
 		if (p != null) TeleportUtils.doTeleport(p,startingWorld.getSpawnLocation());
 		
 		return true;
 	}
 	
 	/**
 	 * Remove the given team, which must be empty, from the match, and free up its start point.
 	 * 
 	 * @param identifier The team to remove
 	 * @return Whether the removal succeeded
 	 */
 	public boolean removeTeam(String identifier) {
 		UhcTeam team = uhcTeams.remove(identifier.toLowerCase());
 		
 		// If team not found, fail
 		if (team == null) return false;
 		
 		// If team not empty, fail
 		if (team.playerCount()>0) return false;
 		
 		teamsInMatch--;
 		
 		// Free up the start point
 		UhcStartPoint sp = team.getStartPoint();
 		sp.setTeam(null);
 		sp.makeSign();
 		sp.emptyChest();
 		availableStartPoints.add(sp);
 		
 		return true;
 	}
 
 
 	/**
 	 * Start the launching phase, and launch all players who have been added to the game
 	 */
 	public void launchAll() {
 		if (matchPhase == MatchPhase.PRE_MATCH) matchPhase = MatchPhase.LAUNCHING;
 		disableSpawnKeeper();
 		setVanish(); // Update vanish status
 
 		// Add all players to the launch queue
 		for(UhcPlayer up : getUhcPlayers())
 			if (!up.isLaunched()) addToLaunchQueue(up);
 
 		// Begin launching
 		launchNext();
 	}
 	
 	
 	private void launchNext() {
 		if (this.launchQueue.size()==0) return;
 		
 		String playerName = this.launchQueue.remove(0);
 		UhcPlayer up = this.getUhcPlayer(playerName);
 		launch(up);
 		
 		server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 			public void run() {
 				launchNext();
 			}
 		}, 20L);
 		
 	}
 
 	private void addToLaunchQueue(UhcPlayer up) {
 		this.launchQueue.add(up.getName().toLowerCase());
 	}
 	
 	
 
 	public void enableSpawnKeeper() {
 		spawnKeeperTask = server.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
 			public void run() {
 				runSpawnKeeper();
 			}
 		}, 20L, 20L);
 	}
 
 	public void disableSpawnKeeper() {
 		server.getScheduler().cancelTask(spawnKeeperTask);
 	}
 	
 	public void runSpawnKeeper() {
 		for (Player p : server.getOnlinePlayers()) {
 			if (!p.isOp() && p.getLocation().getY() < 128) {
 				TeleportUtils.doTeleport(p, startingWorld.getSpawnLocation());
 			}
 		}
 	}
 	
 	/**
 	 * Create a new start point at a given location
 	 * 
 	 * @param number The start point's number
 	 * @param l The location of the start point
 	 * @param buildTrough Whether to add a starting trough
 	 * @return The created start point
 	 */
 	private UhcStartPoint createStartPoint(int number, Location l, Boolean buildTrough) {
 		// Check there is not already a start point with this number		
 		if (startPoints.containsKey(number))
 			return null;
 		
 		UhcStartPoint sp;
 		if (this.isFFA())
 			sp = new SmallGlassStartPoint(number, l, true);
 		else
 			sp = new LargeGlassStartPoint(number, l, true);
 		
 		if (buildTrough) sp.buildStartingTrough();
 		
 		startPoints.put(number,  sp);
 		availableStartPoints.add(sp);
 		
 		return sp;
 	}
 	
 	/**
 	 * Create a new start point at a given location, with optional starting trough
 	 * 
 	 * @param number The start point's number
 	 * @param world The world to create the start point
 	 * @param x x coordinate of the start point
 	 * @param y y coordinate of the start point
 	 * @param z z coordinate of the start point
 	 * @param buildTrough Whether to add a starting trough
 	 * @return The created start point
 	 */
 	private UhcStartPoint createStartPoint(int number, World world, Double x, Double y, Double z, Boolean buildTrough) {
 		return createStartPoint(number, new Location(world, x, y, z), buildTrough);
 	}
 	
 	/**
 	 * Create a new start point at a given location, giving it the next available number
 	 * 
 	 * @param l The location of the start point
 	 * @param buildTrough Whether to add a starting trough
 	 * @return The created start point
 	 */
 	private UhcStartPoint createStartPoint(Location l, Boolean buildTrough) {
 		return createStartPoint(getNextAvailableStartNumber(), l, buildTrough);
 	}
 
 	/**
 	 * Add a new start point at a given location, giving it the next available number.
 	 * 
 	 * This function will also update the saved match data.
 	 * 
 	 * @param x x coordinate of the start point
 	 * @param y y coordinate of the start point
 	 * @param z z coordinate of the start point
 	 * @param buildTrough Whether to add a starting trough
 	 * @return The created start point
 	 */
 	public UhcStartPoint addStartPoint(Double x, Double y, Double z, Boolean buildTrough) {
 		UhcStartPoint sp = createStartPoint(new Location(startingWorld, x, y, z), buildTrough);
 		if (sp != null) this.saveMatchParameters();
 		return sp;
 	}
 	
 		
 	/**
 	 * Determine the lowest unused start number
 	 * 
 	 * @return The lowest available start point number
 	 */
 	public int getNextAvailableStartNumber() {
 		int n = 1;
 		while (startPoints.containsKey(n))
 			n++;
 		return n;
 	}
 	
 
 	
 
 	
 	/**
 	 * @return Whether chat is currently muted
 	 */
 	public boolean isChatMuted() {
 		return chatMuted;
 	}
 	
 	/**
 	 * Mute or unmute chat
 	 * 
 	 * @param muted Status to be set
 	 */
 	public void setChatMuted(Boolean muted) {
 		chatMuted = muted;
 	}
 
 
 	/**
 	 * Apply the mining fatigue game mechanic
 	 * 
 	 * Players who mine stone below a certain depth increase their hunger
 	 * 
 	 * @param player The player to act upon
 	 * @param blockY The Y coordinate of the mined block
 	 */
 	public void doMiningFatigue(Player player, int blockY) {
 		Double exhaustion = 0.0;
 		
 		if (blockY < DIAMOND_LAYER) {
 			exhaustion = this.getMiningFatigueDiamond(); 
 		} else if (blockY < GOLD_LAYER) {
 			exhaustion = this.getMiningFatigueGold();
 		}
 		
 		if (exhaustion > 0)
 			player.setExhaustion((float) (player.getExhaustion() + exhaustion));
 
 				
 	}
 
 
 	/**
 	 * @return The number of players still in the match
 	 */
 	public int getPlayersInMatch() {
 		return playersInMatch;
 	}
 
 
 	/**
 	 * @return The number of teams still in the match
 	 */
 	public int getTeamsInMatch() {
 		return teamsInMatch;
 	}
 
 	
 	/**
 	 * Process the death of a player
 	 * 
 	 * @param up The player who died
 	 */
 	public void handlePlayerDeath(UhcPlayer up) {
 		// TODO teamify. It may be necessary to decrement teamsInMatch here too, if that was the end of a team
 		if (up.isDead()) return;
 		up.setDead(true);
 		playersInMatch--;
 		server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 			public void run() {
 				announcePlayersRemaining();
 			}
 		});
 	}
 
 	/**
 	 * Publicly announce how many players are still in the match 
 	 */
 	private void announcePlayersRemaining() {
 		// TODO teamify. Announce teams remaining too.
 		// Make no announcement if final player was killed
 		if (playersInMatch < 1) return;
 		
 		String message;
 		if (playersInMatch == 1) {
 			message = getSurvivingPlayerList() + " is the winner!";
 			endMatch();
 		} else if (playersInMatch <= 4) {
 			message = playersInMatch + " players remain: " + getSurvivingPlayerList();
 		} else {
 			message = playersInMatch + " players remain";
 		}
 		
 		broadcast(TesseractUHC.OK_COLOR + message);
 	}
 
 	/**
 	 * Get a list of surviving players
 	 * 
 	 * @return A comma-separated list of surviving players
 	 */
 	private String getSurvivingPlayerList() {
 		// TODO teamify
 		String survivors = "";
 		
 		for (UhcPlayer up : getUhcPlayers())
 			if (up.isLaunched() && !up.isDead()) survivors += up.getName() + ", ";;
 		
 		if (survivors.length() > 2)
 			survivors = survivors.substring(0,survivors.length()-2);
 		
 		return survivors;
 		
 	}
 	
 
 
 
 	/**
 	 * Show a spectator the contents of a player's inventory.
 	 * 
 	 * @param spectator The player who is asking to see the inventory
 	 * @param player The player being observed
 	 */
 	public boolean showInventory(Player spectator, Player player) {
 
 		Inventory i = getInventoryView(player);
 		if (i == null) return false;
 		
 		spectator.openInventory(i);
 		return true;
 	}
 
 	/**
 	 * Gets a copy of a player's current inventory, including armor/health/hunger details.
 	 *
 	 * @author AuthorBlues
 	 * @param player The player to be viewed
 	 * @return inventory The player's inventory
 	 *
 	 */
 	public Inventory getInventoryView(Player player)
 	{
 
 		PlayerInventory pInventory = player.getInventory();
 		Inventory inventoryView = Bukkit.getServer().createInventory(null,
 			pInventory.getSize() + 9, player.getDisplayName() + "'s Inventory");
 
 		ItemStack[] oldContents = pInventory.getContents();
 		ItemStack[] newContents = inventoryView.getContents();
 
 		for (int i = 0; i < oldContents.length; ++i)
 			if (oldContents[i] != null) newContents[i] = oldContents[i];
 
 		newContents[oldContents.length + 0] = pInventory.getHelmet();
 		newContents[oldContents.length + 1] = pInventory.getChestplate();
 		newContents[oldContents.length + 2] = pInventory.getLeggings();
 		newContents[oldContents.length + 3] = pInventory.getBoots();
 
 		newContents[oldContents.length + 7] = new ItemStack(Material.APPLE, player.getHealth());
 		newContents[oldContents.length + 8] = new ItemStack(Material.COOKED_BEEF, player.getFoodLevel());
 
 		for (int i = 0; i < oldContents.length; ++i)
 			if (newContents[i] != null) newContents[i] = newContents[i].clone();
 
 		inventoryView.setContents(newContents);
 		return inventoryView;
 	}
 	
 
 	/**
 	 * Set the correct vanish status for all players on the server
 	 * 
 	 * @param p1
 	 */
 	public void setVanish() {
 		for(Player p : server.getOnlinePlayers()) {
 			setVanish(p);
 		}
 	}
 
 	/**
 	 * Set the correct vanish status for the player in relation to all other players
 	 * 
 	 * @param p The player to update
 	 */
 	public void setVanish(Player p) {
 		for (Player p2 : server.getOnlinePlayers()) {
 			setVanish(p, p2);
 			setVanish(p2, p);
 		}
 	}
 	
 	/**
 	 * Set the correct vanish status between two players
 	 * 
 	 * @param viewer Player viewing
 	 * @param viewed Player being viewed
 	 */
 	public void setVanish(Player viewer, Player viewed) {
 		if (viewer == viewed) return;
 		
 		// An op should be invisible to a non-op if the match is launching and not ended
 		if (!viewer.isOp() && viewed.isOp() && (matchPhase == MatchPhase.LAUNCHING || matchPhase == MatchPhase.MATCH)) {
 			viewer.hidePlayer(viewed);
 		} else {
 			viewer.showPlayer(viewed);
 		}
 	}
 
 	public ArrayList<Location> getCalculatedStarts() {
 		return calculatedStarts;
 	}
 
 	public void setCalculatedStarts(ArrayList<Location> calculatedStarts) {
 		this.calculatedStarts = calculatedStarts;
 	}
 
 	public HashMap<Integer, UhcStartPoint> getStartPoints() {
 		return startPoints;
 	}
 
 	public int countAvailableStartPoints() {
 		return availableStartPoints.size();
 	}
 
 	public World getStartingWorld() {
 		return startingWorld;
 	}
 
 	public int countPlayers() {
 		return this.playersInMatch;
 	}
 
 	public Location getLastEventLocation() {
 		return lastEventLocation;
 	}
 
 	public void setLastEventLocation(Location lastEventLocation) {
 		this.lastEventLocation = lastEventLocation;
 	}
 
 	public Location getLastNotifierLocation() {
 		return lastNotifierLocation;
 	}
 
 	public Location getLastDeathLocation() {
 		return lastDeathLocation;
 	}
 
 	public Location getLastLogoutLocation() {
 		return lastLogoutLocation;
 	}
 	
 
 	/**
 	 * Set the length of the initial no-PVP period
 	 * 
 	 * @param nopvp The duration of the no-PVP period, in seconds
 	 */
 	public void setNopvp(int nopvp) {
 		md.set("nopvp", nopvp);
 		saveMatchParameters();
 	}
 
 	/**
 	 * Get the length of the initial no-PVP period
 	 * 
 	 * @return The duration of the no-PVP period, in seconds
 	 */
 	public int getNopvp() {
 		return md.getInt("nopvp");
 	}
 
 	/**
 	 * Set the mining fatigue penalties.
 	 * 
 	 * @param gold Exhaustion penalty to add when mining at the gold layer
 	 * @param diamond Exhaustion penalty to add when mining at the diamond layer
 	 */
 	public void setMiningFatigue(double gold, double diamond) {
 		md.set("miningfatigue.gold", gold);
 		md.set("miningfatigue.diamond", diamond);
 		saveMatchParameters();
 	}
 
 
 	/**
 	 * Get the current exhaustion penalty for mining at the gold layer
 	 * 
 	 * @return Exhaustion penalty to add when mining at the gold layer
 	 */
 	public double getMiningFatigueGold() {
 		return md.getDouble("miningfatigue.gold");
 	}
 
 	/**
 	 * Get the current exhaustion penalty for mining at the diamond layer
 	 * 
 	 * @return Exhaustion penalty to add when mining at the diamond layer
 	 */
 	public double getMiningFatigueDiamond() {
 		return md.getDouble("miningfatigue.diamond");
 	}
 
 	/**
 	 * Set the bonus items dropped in a PVP kill. One of the specified item will be dropped.
 	 * 
 	 * @param id The item ID to give a pvp killer
 	 */
 	public void setKillerBonus(int id) { setKillerBonus(id,1); }
 	
 	/**
 	 * Set the bonus items dropped in a PVP kill.
 	 * 
 	 * @param id The item ID to give a pvp killer
 	 * @param quantity The number of items to drop
 	 */
 	public void setKillerBonus(int id, int quantity) {
 		if (id == 0) quantity = 0;
 		md.set("killerbonus.id", id);
 		md.set("killerbonus.quantity", quantity);
 		saveMatchParameters();
 		
 	}
 
 	/**
 	 * Get the bonus items to be dropped by a PVP-killed player in addition to their inventory
 	 * 
 	 * @return The ItemStack to be dropped
 	 */
 	public ItemStack getKillerBonus() {
 		int id = md.getInt("killerbonus.id");
 		int quantity = md.getInt("killerbonus.quantity");
 		
 		if (id == 0 || quantity == 0) return null;
 		
 		return new ItemStack(id, quantity);
 	}
 
 	/**
 	 * Set deathban on/off
 	 * 
 	 * @param d Whether deathban is to be enabled
 	 */
 	public void setDeathban(boolean d) {
 		md.set("deathban", d);
 		this.saveMatchParameters();
 		adminBroadcast(TesseractUHC.OK_COLOR + "Deathban has been " + (d ? "enabled" : "disabled") + "!");
 	}
 
 	/**
 	 * Check whether deathban is in effect
 	 * 
 	 * @return Whether deathban is enabled
 	 */
 	public boolean getDeathban() {
 		return md.getBoolean("deathban");
 	}
 	
 	
 	/**
 	 * Set FFA on/off
 	 * 
 	 * @param d Whether FFA is to be enabled
 	 */
 	public void setFFA(boolean d) {
 		md.set("ffa", d);
 		this.saveMatchParameters();
 		adminBroadcast(TesseractUHC.OK_COLOR + "FFA has been " + (d ? "enabled" : "disabled") + "!");
 	}
 
 	/**
 	 * Check whether this is an FFA match
 	 * 
 	 * @return Whether this is FFA
 	 */
 	public boolean isFFA() {
 		return md.getBoolean("ffa");
 	}
 	/**
 	 * Update the contents of the match "bonus chest"
 	 * 
 	 * @param p The player
 	 */
 	public void setBonusChest(ItemStack [] bonusChest) {
 		this.bonusChest = bonusChest;
 		md.set("bonuschest", bonusChest);
 		this.saveMatchParameters();
 		
 	}
 
 
 	/**
 	 * Get the contents of the match "bonus chest"
 	 * 
 	 * @return The contents of the bonus chest
 	 */
 	public ItemStack[] getBonusChest() {
 		return bonusChest;
 	}
 
 	public MatchPhase getMatchPhase() {
 		return matchPhase;
 	}
 
 	public void sendNotification(UhcNotification n, Location l) {
 		setLastNotifierLocation(l);
 		this.broadcast(n.formatForPlayers());
 	}
 
 	public boolean startMatchCountdown(int countLength) {
 		if (this.matchCountdown == null && (this.matchPhase == MatchPhase.LAUNCHING || this.matchPhase == MatchPhase.PRE_MATCH)) {
 			this.matchCountdown = new MatchCountdown(countLength, plugin, this);
 			return true;
 		}
 		return false;
 		
 	}
 	
 	public void cancelCountdown() {
 		if (this.matchCountdown != null) matchCountdown.cancel();
 		
 	}
 
 	public boolean startBorderCountdown(int countLength, int newRadius) {
 		if (this.borderCountdown == null && this.matchPhase == MatchPhase.MATCH) {
 			this.borderCountdown = new BorderCountdown(countLength, plugin, this, newRadius);
 			return true;
 		}
 		return false;
 	}
 
 	public Collection<UhcTeam> getTeams() {
 		return uhcTeams.values();
 	}
 
 }
