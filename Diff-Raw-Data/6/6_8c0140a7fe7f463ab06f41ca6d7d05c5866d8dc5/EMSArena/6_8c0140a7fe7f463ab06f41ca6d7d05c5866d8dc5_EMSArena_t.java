 package io.github.basicmark.ems;
 
 import io.github.basicmark.config.ConfigUtils;
 import io.github.basicmark.config.PlayerState;
 import io.github.basicmark.config.PlayerStateLoader;
 import io.github.basicmark.ems.EMSArenaState;
 import io.github.basicmark.ems.arenaevents.EMSArenaEvent;
 import io.github.basicmark.ems.arenaevents.EMSAutoEnd;
 import io.github.basicmark.ems.arenaevents.EMSCheckTeamPlayerCount;
 import io.github.basicmark.ems.arenaevents.EMSClearRegion;
 import io.github.basicmark.ems.arenaevents.EMSEventBlock;
 import io.github.basicmark.ems.arenaevents.EMSMessenger;
 import io.github.basicmark.ems.arenaevents.EMSPotionEffect;
 import io.github.basicmark.ems.arenaevents.EMSSpawnEntity;
 import io.github.basicmark.ems.arenaevents.EMSTeleport;
 import io.github.basicmark.ems.arenaevents.EMSTimer;
 import io.github.basicmark.ems.arenaevents.EMSTracker;
 import io.github.basicmark.util.PlayerDeathInventory;
 
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.util.Vector;
 import org.bukkit.Location;
 
 public class EMSArena implements ConfigurationSerializable {
 	// Static data
 	protected String name;
 	protected EMSArenaState arenaState;
 	protected Location lobby;
 	protected boolean requiresTeamLobby;
 	protected boolean requiresTeamSpawn;
 	protected boolean saveInventory;
 	protected boolean saveXP;
 	protected boolean saveHealth;
 	protected boolean keepInvAfterEvent;
 	protected boolean lobbyRespawn;
 	protected boolean keepInvAfterDeath;
 	protected HashMap<String, EMSTeam> teams;
 	protected List<EMSArenaEvent> events;
 	
 	/*
 	 *  Note:
 	 *  playersInLobby can be accessed from multiple threads (server thread & client threads
 	 *  for chat message processing) and thus must use a thread safe set implementation
 	 */
 	private Set<Player> playersInLobby;
 	private HashMap<Player, Location> playersLoc;
 	protected Set<Location> protections;
 	protected HashMap<Player, PlayerDeathInventory> playersDeathInv;
 	
 	PlayerStateLoader playerLoader;
 	JavaPlugin plugin;	// Required to schedule the player teleport after death
 	
 	protected void commonInit() {
 		// Allocate resources for managing static data
 		this.teams = new HashMap<String, EMSTeam>();
 		this.events = new ArrayList<EMSArenaEvent>();
 
 		// Allocate resources for managing dynamic data
 		this.playersInLobby = new HashSet<Player>();	//new ConcurrentSkipListSet<Player>();
 		this.playersLoc = new HashMap<Player, Location>();
 		this.protections = new HashSet<Location>();
 		this.playersDeathInv = new HashMap<Player, PlayerDeathInventory>();
 	}
 	
 	public EMSArena(String name) {
 		commonInit();
 
 		// Init all the "static" data to their default values
 		this.name = name;
 		this.arenaState = EMSArenaState.CLOSED;
 		this.lobby = null;
 		this.requiresTeamLobby = false;
 		this.requiresTeamSpawn = false;
 		this.saveInventory = true;
 		this.saveXP = true;
 		this.saveHealth= true; 
 		this.keepInvAfterEvent = false;
 		this.lobbyRespawn = false;
 		this.keepInvAfterDeath = false;
 		
 		// Add tracking and auto-end by default
 		events.add(new EMSTracker(this));
 		events.add(new EMSAutoEnd(this));
 	}
 
 	@SuppressWarnings("unchecked")
 	public EMSArena(Map<String, Object> values) {
 		commonInit();
 
 		// Load all the state data from the object data
 		this.name = (String) values.get("name");
 		this.lobby = ConfigUtils.DeserializeLocation((Map<String, Object>) values.get("lobby"));
 		this.requiresTeamLobby = (boolean) values.get("requiresteamlobby");
 
 		/* Some older config's don't have the restore info so set it to the default */
 		try {
 			this.saveInventory = (boolean) values.get("saveinventory");
 		} catch (Exception e) {
 			this.saveInventory = true;
 		}
 		try {
 			this.saveXP = (boolean) values.get("savexp");
 		} catch (Exception e) {
 			this.saveXP = true;
 		}
 		try {
 			this.saveHealth = (boolean) values.get("savehealth");
 		} catch (Exception e) {
 			this.saveHealth = true;
 		}
 		try {
 			this.keepInvAfterEvent = (boolean) values.get("keepinvafterevent");
 		} catch (Exception e) {
 			this.keepInvAfterEvent = false;
 		}
 		try {
 			this.lobbyRespawn = (boolean) values.get("lobbyrespawn");
 		} catch (Exception e) {
 			this.lobbyRespawn = false;
 		}
 		try {
 			this.keepInvAfterDeath = (boolean) values.get("keepinvafterdeath");
 		} catch (Exception e) {
 			this.keepInvAfterDeath = false;
 		}
 		
 		this.teams = new HashMap<String, EMSTeam>();
 		int teamCount = (int) values.get("teamcount");
 		for (int i=0;i<teamCount;i++) {
 			EMSTeam newTeam = new EMSTeam((Map<String, Object>) values.get("team"+i), this);
 			
 			this.teams.put(newTeam.getName(), newTeam);
 		}
 
 		// Events are optional
 		int eventCount = 0;
 		try {
 			eventCount = (int) values.get("eventcount");
 		} catch (Exception e) {
 			eventCount = 0;
 		}
 		for (int i=0;i<eventCount;i++) {
 			EMSArenaEvent newEvent = (EMSArenaEvent) values.get("event"+i);
 			newEvent.setArena(this);
 			this.events.add(newEvent);
 		}
 		
 		// Update the signs to reflect the loaded state
 		updateStatus(EMSArenaState.fromString((String) values.get("state")));
 	}
 
 	public Map<String, Object> serialize() {
 		Map<String, Object> values = new LinkedHashMap<String, Object>();
 		values.put("name", name);
 		values.put("state", arenaState.toString().toLowerCase());
 		if (lobby != null) {
 			values.put("lobby", ConfigUtils.SerializeLocation(lobby));
 		}
 
 		/* Player management settings */
 		values.put("saveinventory", saveInventory);
 		values.put("savexp", saveXP);
 		values.put("savehealth", saveHealth);
 		values.put("keepinvafterevent", keepInvAfterEvent);
 		values.put("lobbyrespawn", lobbyRespawn);
 		values.put("keepinvafterdeath", keepInvAfterDeath);
 
 		/* Team management settings */
 		values.put("requiresteamlobby", requiresTeamLobby);
 		values.put("teamcount", teams.size());
 
 		Iterator<EMSTeam> it = teams.values().iterator();
 		int j=0;
 		while (it.hasNext()) {
 			EMSTeam team = it.next();
 
 			values.put("team" + j, team.serialize());
 			j++;
 		}
 
 		/* Event settings */
 		values.put("eventcount", events.size());
 		Iterator<EMSArenaEvent> ie = events.iterator();
 		j=0;
 		while (ie.hasNext()) {
 			EMSArenaEvent event = ie.next();
 			values.put("event" + j, event);
 			j++;
 		}
 
 		return values;
 	}
 	
 	public void setPlugin(JavaPlugin plugin) {
 		this.plugin = plugin;
 		playerLoader = new PlayerStateLoader(plugin.getDataFolder()+"/players");
 	}
 
 	public String getName() {
 		return name;
 	}
 	
 	public EMSArenaState getState() {
 		return arenaState;
 	}
 	
 	public String getWorld() {
 		return lobby.getWorld().getName(); 
 	}
 	
 	// Arena setup functions
 	public void setLobby(Player player) {
 		lobby = player.getLocation();
 	}
 	
 	public void setLobbyRespawn(boolean lobbyRespawn) {
 		this.lobbyRespawn = lobbyRespawn;
 	}
 	
 	public void addTeam(String name, String displayName) {
 		EMSTeam newTeam = new EMSTeam(name, displayName, this);
 		teams.put(name, newTeam);
 	}
 
 	public void setRequiresTeamLobby(boolean required) {
 		requiresTeamLobby = required;
 	}
 
 	public void setRequiresTeamSpawn(boolean required) {
 		requiresTeamSpawn = required;
 	}
 
 	public boolean removeTeam(String teamName) {
 		if (!teams.containsKey(teamName)) {
 			return false;
 		}
 
 		teams.remove(teamName);
 		return true;
 	}
 	
 	public boolean setTeamLobby(String teamName, Player player) {
 		if (!teams.containsKey(teamName)) {
 			return false;
 		}
 		
 		EMSTeam team = teams.get(teamName);
 		team.setLobby(player);
 		return true;
 	}
 
 	public boolean addTeamSpawn(String teamName, Player player) {
 		if (!teams.containsKey(teamName)) {
 			return false;
 		}
 		
 		EMSTeam team = teams.get(teamName);
 		team.addSpawn(player);
 		return true;
 	}
 
 	public boolean clearTeamSpawn(String teamName) {
 		if (!teams.containsKey(teamName)) {
 			return false;
 		}
 		
 		EMSTeam team = teams.get(teamName);
 		team.clearSpawns();
 		return true;
 	}
 
 	public int getTeamSpawnCount(String teamName) {
 		if (!teams.containsKey(teamName)) {
 			return 0;
 		}
 		
 		EMSTeam team = teams.get(teamName);
 		return team.getSpawnCount();
 	}
 	
 	public boolean setTeamSpawnMethod(String teamName, String method) {
 		if (!teams.containsKey(teamName)) {
 			return false;
 		}
 		
 		EMSTeam team = teams.get(teamName);
 		return team.setSpawnMethod(method);
 	}
 	
 	public boolean setTeamCap (String teamName, int capSize) {
 		if (!teams.containsKey(teamName)) {
 			return false;
 		}
 		
 		EMSTeam team = teams.get(teamName);
 		return team.setCap(capSize);
 	}
 	
 	public boolean setSaveInventory(boolean save) {
 		this.saveInventory = save;
 		return true;
 	}
 	
 	public boolean setSaveXP(boolean save) {
 		this.saveXP = save;
 		return true;
 	}
 	
 	public boolean setSaveHealth(boolean save) {
 		this.saveHealth = save;
 		return true;
 	}
 
 	public boolean setKeepInvAfterEvent(boolean keep) {
 		this.keepInvAfterEvent = keep;
 		return true;
 	}
 	
 	public boolean setKeepInvAfterDeath(boolean keep) {
 		this.keepInvAfterDeath = keep;
 		return true;
 	}
 
 	public boolean listEvents(Player player) {
 		Iterator<EMSArenaEvent> i = events.iterator();
 		int j = 0;
 		while (i.hasNext()) {
 			EMSArenaEvent event = i.next();
 			player.sendMessage(j + ") " + event.getListInfo());
 			j++;
 		}
 		return true;
 	}
 	
 	public boolean removeEvent(Player player, int eventIndex) {
 		if (eventIndex > events.size()) {
 			return false;
 		}
 
 		EMSArenaEvent removal = events.get(eventIndex);
 		removal.destroy();
 		events.remove(eventIndex);
 		return true;
 	}
 	
 	public boolean addMessage(String eventTrigger, String message) {
 		EMSMessenger messageEvent = new EMSMessenger(this, eventTrigger, message);
 		events.add(messageEvent);
 		return true;
 	}
 	
 	public boolean addTimer(String eventTrigger, String createName, boolean inSeconds, int[] timeArray, String displayName) {
 		EMSTimer timerEvent = new EMSTimer(this, eventTrigger, createName, inSeconds, timeArray, displayName);
 		events.add(timerEvent);
 		return true;
 	}
 	
 	public boolean addEventBlock(Block block, String eventTrigger) {
 		boolean inverted;
 		if (block.getType() == Material.IRON_BLOCK) {
 			inverted = false;
 		} else if (block.getType() == Material.REDSTONE_BLOCK) {
 			inverted = true;
 		} else {
 			return false;
 		}
 
 		EMSEventBlock eventBlock = new EMSEventBlock(this, block.getLocation(), eventTrigger, inverted);
 		events.add(eventBlock);
 		return true;
 	}
 	
 	public boolean addClearRegion(String triggerName, Location pos1, Location pos2) {
 		EMSClearRegion eventClearRegion = new EMSClearRegion(this, triggerName, pos1, pos2);
 		events.add(eventClearRegion);
 		return true;
 	}
 	
 	public boolean addTeleport(String triggerName, Location location) {
 		EMSTeleport teleport = new EMSTeleport(this, triggerName, location);
 		events.add(teleport);
 		return true;
 	}
 	
 	public boolean addEntitySpawn(String eventTrigger, Location location, EntityType entity, int count) {
 		EMSSpawnEntity spawnEntity = new EMSSpawnEntity(this, eventTrigger, location, entity, count);
 		events.add(spawnEntity);
 		return true;	
 	}
 	
 	public boolean addCheckTeamPlayerCount(boolean team, String eventTrigger, int count, String createEvent) {
 		EMSCheckTeamPlayerCount checkTeamPlayerCount = new EMSCheckTeamPlayerCount(this, team, eventTrigger, count, createEvent);
 		events.add(checkTeamPlayerCount);
 		return true;
 	}
 	
 	public boolean addPotionEffect(String eventTrigger, PotionEffectType effect, int duration, int amplifier) {
 		EMSPotionEffect potionEffect = new EMSPotionEffect(this, eventTrigger, effect, duration, amplifier);
 		events.add(potionEffect);
 		return true;
 	}
 	
 	// Sign management
 	public boolean signUpdated(Block block, String[] lines) {
 		String teamName = lines[2];
 		if (teams.containsKey(teamName)) {
 			EMSTeam team = teams.get(teamName);
 
 			team.setJoinSign(block.getLocation());
 			/*
 			updateSign(block, team);
 			*/
 			return true;
 		}
 
 		return false;
 	}
 	
 	/*
 	public void updateSign(Block block, EMSTeam team) {
 
 		if ((block.getType() == Material.SIGN) || (block.getType() == Material.SIGN_POST)) {
 			BlockState state = block.getState();
 			Sign sign = (Sign) state;
 
 			sign.setLine(0, team.getState.toColourString());
 			sign.setLine(1, name);
 
 			sign.setLine(2, team.getDisplayName());
 			sign.setLine(3, "Click to join!");
 			sign.update(true);	
 		} else {
 			// The sign has gone! Remove it from the team so we don't try again.
 			team.setJoinSign(null);
 		}
 	}
 	*/
 	
 	public void updateStatus(EMSArenaState newState) {
 		Iterator<EMSTeam> i = teams.values().iterator();
 
 		arenaState = newState;
 		while(i.hasNext()) {
 			EMSTeam team = i.next();
 			team.updateStatus(newState);
 			/*
 			Location signLoc = team.getJoinSign();
 			if (signLoc != null) {
 				updateSign(signLoc.getBlock(), team);
 			}
 			*/
 		}
 	}
 
 	public boolean signClicked(Sign sign, Player player) {
 		boolean consumeEvent = false;
 		if (!playersInLobby.contains(player)) {
 			// Only if a player is in the lobby can they join a team
 			player.sendMessage(ChatColor.RED + "Your not in the lobby!");
 			return false;
 		}
 		
 		if (arenaState != EMSArenaState.OPEN) {
 			player.sendMessage(ChatColor.RED + " arena is " + arenaState.toString().toLowerCase());
 			return false;
 		}
 
 		String teamName = sign.getLine(2);
 		Iterator<EMSTeam> i = teams.values().iterator();
 		while (i.hasNext()) {
 			EMSTeam team = i.next();
 
 			if (team.getDisplayName().equals(teamName)) {
 				if (team.addPlayer(player)) {
 					playersInLobby.remove(player);
 				} else {
 					player.sendMessage(ChatColor.RED + " failed to add you to team " + teamName);
 				}
 				consumeEvent = true;
 			}
 		}
 
 		return consumeEvent;
 	}
 	
 	public boolean blockBroken(Player player, Location location) {
 		if (arenaState != EMSArenaState.EDITING) {
 			// We're not editing so just check if we know this block
 			if (protections.contains(location)) {
 				player.sendMessage(ChatColor.RED + "[EMS] That block is protected in arena " + name);
 				return true;
 			}
 		} else {
 			// Check if the block is the join sign of a team
 			Iterator<EMSTeam> i = teams.values().iterator();
 			while (i.hasNext()) {
 				EMSTeam team = i.next();
 				if (team.getJoinSign() != null) {
 					if (team.getJoinSign().equals(location)) {
 						team.setJoinSign(null);
 					}
 				}
 			}
 		}
 
 		return false;
 	}
 	
 	public boolean editOpen() {
 		if (arenaState == EMSArenaState.EDITING) {
 			// The arena is already in edit state
 			return false;
 		} else if (arenaState != EMSArenaState.CLOSED) {
 			disable();
 		}
 		
 		updateStatus(EMSArenaState.EDITING);
 		return true;
 	}
 	
 	public void editClose() {
 		// Set the arena to closed after its been edited
 		updateStatus(EMSArenaState.CLOSED);
 	}
 
 	public boolean enable() {
 		// Check if the setup is complete
 		if (lobby == null) {
 			// We require a lobby for the arena
 			return false;
 		}
 		if (teams.isEmpty()) {
 			// We need at least one team
 			return false;
 		}
 
 		if (requiresTeamLobby) {
 			Iterator<EMSTeam> i = teams.values().iterator();
 
 			while(i.hasNext()) {
 				EMSTeam team = i.next();
 				if (!team.hasLobby() || !team.hasJoinSign()) {
 					// All teams need a lobby and a way to get there
 					return false;
 				}
 			}
 		}
 
 		if (requiresTeamSpawn) {
 			Iterator<EMSTeam> i = teams.values().iterator();
 
 			while(i.hasNext()) {
 				EMSTeam team = i.next();
 				if (!team.hasSpawn()) {
 					// All teams need a lobby
 					return false;
 				}
 			}
 		}
 		updateStatus(EMSArenaState.OPEN);
 		return true;
 	}
 	
 	public void disable() {
 		// Disable all the teams
 		endEvent();
 
 		// Then remove the players from the arena
 		Iterator<Player> pi = playersInLobby.iterator();
 		while(pi.hasNext()) {
 			Player player = pi.next();
 
 			player.sendMessage(ChatColor.RED + "[EMS] The arena your in is being disabled");
 			playerLeaveArenaDo(player);
 			pi.remove();
 		}
 		updateStatus(EMSArenaState.CLOSED);
 	}
 
 	public void destroy() {
 		Iterator<EMSArenaEvent> i = events.iterator();
 		
 		/*
 		 * Destroy any events which where created
 		 */
 		while (i.hasNext()) {
 			EMSArenaEvent removal = i.next();
 			removal.destroy();
 		}
 	}
 	
 	public void forceTeamCap(int capSize) {
 		Iterator<EMSTeam> i = teams.values().iterator();
 		// We can only adjust the cap while the arena is open
 		if (arenaState != EMSArenaState.OPEN) {
 			return;
 		}
 
 		while(i.hasNext()) {
 			EMSTeam team = i.next();
 			team.setForceCap(capSize);
 		}
 	}
 	
 	public void startEvent(CommandSender sender) {
 		Iterator<EMSTeam> i = teams.values().iterator();
 		
 		// Only start an event if the previous state of the arena was open
 		if (arenaState != EMSArenaState.OPEN) {
 			sender.sendMessage(ChatColor.RED + "[EMS] The event is already in progress");
 		}
 
 		while(i.hasNext()) {
 			EMSTeam team = i.next();
 			team.spawnPlayers();
 		}
 
 		updateStatus(EMSArenaState.ACTIVE);
 		signalEvent("start");
 	}
 	
 	public void endEvent() {
 		// Ending an event is always possible, even if one hasn't started
 
 		// Stop all the arena events
 		Iterator<EMSArenaEvent> ie = events.iterator();
 		while(ie.hasNext()) {
 			EMSArenaEvent event = ie.next();
 			event.cancelEvent();
 		}	
 
 		Iterator<EMSTeam> it = teams.values().iterator();
 		while(it.hasNext()) {
 			EMSTeam team = it.next();
 			team.removeAllPlayers();
 		}
 
 		updateStatus(EMSArenaState.OPEN);
 	}
 
 	public boolean startTracking() {
 		if (arenaState == EMSArenaState.ACTIVE) {
 			signalEvent("start-tracking");
 			return true;
 		}
 		return false;
 	}
 	
 	public void endTracking() {
 		if (arenaState == EMSArenaState.ACTIVE) {
 			signalEvent("end-tracking");
 		}
 	}
 	
 	// Player functions
 	public void playerJoinArena(Player player) {
 		if (!arenaCommandValid(player, false)) {
 			return;
 		}
 
 		playersInLobby.add(player);
 		playersLoc.put(player, player.getLocation());
 		player.teleport(lobby);
 		playerLoader.save(player, new PlayerState(player, saveInventory, saveXP, saveHealth));
 		player.sendMessage(ChatColor.GREEN + "[EMS] Welcome to arena " + name);
 	}
 
 	public boolean playerInArena(Player player) {
 		if (playersInLobby.contains(player)) {
 			return true;
 		}
 
 		Iterator<EMSTeam> i = teams.values().iterator();
 		while(i.hasNext()) {
 			EMSTeam team = i.next();
 			if (team.isPlayerInTeam(player)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/*
 	 * Set the player in the right state when they leave the arena
 	 */
 	public void playerLeaveArenaDo(Player player) {
 		player.sendMessage(ChatColor.GREEN + "[EMS] Bye, thanks for playing!");
 		player.teleport(playersLoc.get(player));
 		playersLoc.remove(player);
		playersDeathInv.remove(player);
 		PlayerState state = playerLoader.load(player);
 		if (state != null) {
 			state.restore(player);
 			playerLoader.delete(player);
 		}
 	}
 	
 	/*
 	 * Player is leaving the arena remove them from the lobby/team
 	 * and set their state right
 	 */
 	public void playerLeaveArena(Player player) {
 		if (!arenaCommandValid(player, true)) {
 			return;
 		}
 	
 		/*
 		 *  Remove the player from the team they are in (if any) which will
 		 *  kick them back into the lobby
 		 */
 		playerLeaveTeam(player);
 		
 		/*
 		 * Then check if they are in the lobby and remove them from their
 		 */
 		if (playersInLobby.contains(player)) {
 			playersInLobby.remove(player);
 			playerLeaveArenaDo(player);
 		}
 		
 		/*
 		 * If the player left an active event then signal to that event
 		 * that a player has left so it can act accordingly
 		 */
 		if (arenaState == EMSArenaState.ACTIVE) {
 			signalEvent("player-leave");
 		}
 	}
 
 	public void playerJoinTeam(Player player, String teamName) {
 		if (!arenaCommandValid(player, false)) {
 			return;
 		}
 		
 		// Check the player isn't already in another team
 		Iterator<EMSTeam> i = teams.values().iterator();
 		while(i.hasNext()) {
 			EMSTeam team = i.next();
 			if (team.isPlayerInTeam(player)) {
 				player.sendMessage("[EMS] Your already in team " + team.getName());
 				return;
 			}
 		}
 
 		EMSTeam team = teams.get(teamName);
 		team.addPlayer(player);
 	}
 
 	public void playerLeaveTeam(Player player) {
 		if (!arenaCommandValid(player, true)) {
 			return;
 		}
 		
 		// Check all teams for the player and remove them if found
 		Iterator<EMSTeam> i = teams.values().iterator();
 		while(i.hasNext()) {
 			EMSTeam team = i.next();
 			if (team.isPlayerInTeam(player)) {
 				team.removePlayer(player);
 				player.sendMessage(ChatColor.GREEN + "[EMS] Leaving team " + team.getName());
 				return;
 			}
 		}
 
 		return;
 	}
 	
 	public List<String> getPlayers() {
 		List<String> playerData = new ArrayList<String>();
 		
 		
 		playerData.add(ChatColor.WHITE + "Lobby");
 		Iterator<Player> pi=playersInLobby.iterator();
 		while (pi.hasNext()) {
 			Player player = pi.next();
 			playerData.add("-" + player.getName());
 		}
 
 		Iterator<EMSTeam> i = teams.values().iterator();
 		while(i.hasNext()) {
 			EMSTeam team = i.next();
 
 			playerData.add(team.getDisplayName());
 			List<String> tmpTeamMembers = team.getPlayerList();
 			Iterator<String> tpi =  tmpTeamMembers.iterator();
 			while (tpi.hasNext()) {
 				String player = tpi.next();
 				playerData.add("-" + player);
 			}
 		}	
 		return playerData;
 	}
 	
 	public boolean playerDeath(Player player) {
 		if (playerInArena(player)) {
 			boolean inTeam = false;
 			Iterator<EMSTeam> i = teams.values().iterator();
 			while(i.hasNext()) {
 				EMSTeam team = i.next();
 				if (team.isPlayerInTeam(player)) {
 					team.playerDeath(player);
 
 					// Reset the health here before the death screen gets displayed
 					player.setHealth(player.getMaxHealth());
 					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, (Runnable) new EMSDeathRunner(player, this), 1);
 					if (keepInvAfterDeath) {
 						playersDeathInv.put(player, new PlayerDeathInventory(player));
 					}
 
 					/*
 					 * Although the player is in limbo for 1 tick we can signal death & leave
 					 * events here as the player has already been removed from the team
 					 */
 					signalEvent("player-death");
 					signalEvent("player-leave");
 					inTeam = true;
 				}
 			}
 			if ((!inTeam) && lobbyRespawn) {
 				// Reset the health here before the death screen gets displayed
 				player.setHealth(player.getMaxHealth());
 				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, (Runnable) new EMSDeathRunner(player, this), 1);
 				if (keepInvAfterDeath) {
 					playersDeathInv.put(player, new PlayerDeathInventory(player));
 				}
 			}
 			return keepInvAfterDeath;
 		}
 		return false;
 	}
 	
 	public void playerRespawn(Player player) {
		if (playerInArena(player) && keepInvAfterDeath && playersDeathInv.containsKey(player)) {
 			PlayerDeathInventory deathInv = playersDeathInv.get(player);
 			deathInv.restore();
			playersDeathInv.remove(player);
 		}
 	}
 	
 	public EMSChatResponse playerChat(Player player, String message) {
 		EMSChatResponse response = null;
 
 		if (playerInArena(player)) {	
 			String[] stringArray = message.split("\\s", 2);
 			if (!stringArray[0].equalsIgnoreCase("shout")) {
 				if (playersInLobby.contains(player)) {
 					broadcast("[" + ChatColor.GREEN + "lobby" + ChatColor.RESET + "] " + player.getDisplayName() + ": " + message);
 				} else {
 					Iterator<EMSTeam> i = teams.values().iterator();
 					while(i.hasNext()) {
 						EMSTeam team = i.next();
 						if (team.isPlayerInTeam(player)) {
 							team.broadcast("[" + team.getDisplayName() + ChatColor.RESET + "] " + player.getDisplayName() + ": " + ChatColor.RESET + message);
 						}
 					}
 				}
 				/*
 				 * The message was from a player in this arena and wasn't a shout so EMS has
 				 * consumed the message and thus we cancel the event
 				 */
 				response = new EMSChatResponse(true, null);
 			} else if (stringArray.length == 2){
 				/*
 				 * Although the player is in an arena it had shout prefix so remove the shout
 				 * part of the message and allow the event to continue
 				 */
 				response = new EMSChatResponse(false, stringArray[1]);
 			}
 		}
 		return response;
 	}
 	
 	public void sendToLobby(Player player) {
 		// Unlike playerJoinArena the previous location of the player should not be saved 
 		playersInLobby.add(player);
 		restorePlayer(player);
 		player.teleport(lobby);
 	}
 	
 	public void broadcast(String message) {
 		Iterator<Player> ip = playersInLobby.iterator();
 		while (ip.hasNext()) {
 			Player player = ip.next();
 			player.sendMessage(message);
 		}
 
 		Iterator<EMSTeam> it = teams.values().iterator();
 		while(it.hasNext()) {
 			EMSTeam team = it.next();
 			team.broadcast(message);
 		}
 	}
 	
 	public void signalEvent(String eventName) {
 		Iterator<EMSArenaEvent> i = events.iterator();
 
 		while(i.hasNext()) {
 			EMSArenaEvent event = i.next();
 			event.signalEvent(eventName);
 		}
 	}
 	
 	public void addProtection(Location location) { 
 		protections.add(location);
 	}
 
 	public void removeProtection(Location location) { 
 		protections.remove(location);
 	}
 	
 	public int getActiveTeamCount() {
 		Iterator<EMSTeam> i = teams.values().iterator();
 		int activeTeams = 0;
 
 		while(i.hasNext()) {
 			EMSTeam team = i.next();
 			if (team.getPlayerCount() > 0) {
 				activeTeams++;
 			}
 		}
 		return activeTeams;
 	}
 
 	public int getActivePlayerCount() {
 		Iterator<EMSTeam> i = teams.values().iterator();
 		int activePlayers = 0;
 
 		while(i.hasNext()) {
 			EMSTeam team = i.next();
 
 			activePlayers += team.getPlayerCount();
 		}
 		return activePlayers;
 	}
 	
 	public JavaPlugin getPlugin() {
 		return plugin;
 	}
 	
 	public Set<Player> getActivePlayers() {
 		Set<Player> players = new HashSet<Player>();
 		Iterator<EMSTeam> i = teams.values().iterator();
 		
 		while(i.hasNext()) {
 			EMSTeam team = i.next();
 			players.addAll(team.getPlayers());
 		}
 		return players;
 	}
 	
 	// Private functions
 	private void restorePlayer(Player player) {
 		//Reset the player to a good state after their death
 		player.setHealth(player.getMaxHealth());
 		player.setFireTicks(0);
 		for (PotionEffect effect : player.getActivePotionEffects()) {
 			player.removePotionEffect(effect.getType());
 		}
 		player.setVelocity(new Vector(0, 0, 0));
 
 		/* Clear down a players inventory when they rejoin the lobby if required */
 		if (!keepInvAfterEvent) {
 			player.getInventory().clear();
 			player.getInventory().setHelmet(null);
 			player.getInventory().setChestplate(null);
 			player.getInventory().setLeggings(null);
 			player.getInventory().setBoots(null);
 		}
 	}
 
 	private boolean arenaCommandValid(Player player, boolean shouldBeInArena) {
 		if (arenaState == EMSArenaState.CLOSED) {
 			player.sendMessage(ChatColor.RED + "[EMS] " + name + " is not open");
 			return false;
 		}
 
 		if (shouldBeInArena) {
 			if (!playerInArena(player)) {
 				player.sendMessage(ChatColor.RED + "[EMS] Your not in an arena!");
 				return false;
 			}
 		} else {
 			if (playerInArena(player)) {
 				player.sendMessage(ChatColor.RED + "[EMS] Your already in this arena!");
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public class EMSDeathRunner implements Runnable {
 		Player player;
 		EMSArena arena;
 
 		public EMSDeathRunner(Player player, EMSArena arena) {
 			this.player = player;
 			this.arena = arena;
 		}
 
 		@Override
 		public void run() {
 			// Create the respawn event which is now missing as we reset the health to full on death
 			arena.sendToLobby(player);
 			Bukkit.getPluginManager().callEvent(new PlayerRespawnEvent(player, player.getLocation(), false));
 		}
 	}
 }
