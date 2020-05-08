 package io.github.basicmark.ems;
 
 import io.github.basicmark.config.ConfigUtils;
 import io.github.basicmark.config.PlayerState;
 import io.github.basicmark.config.PlayerStateLoader;
 import io.github.basicmark.config.ReferenceInventory;
 import io.github.basicmark.config.ReferenceInventoryLoader;
 import io.github.basicmark.ems.EMSArenaState;
 import io.github.basicmark.ems.arenaevents.EMSArenaEvent;
 import io.github.basicmark.ems.arenaevents.EMSAutoEnd;
 import io.github.basicmark.ems.arenaevents.EMSCheckTeamPlayerCount;
 import io.github.basicmark.ems.arenaevents.EMSClearRegion;
 import io.github.basicmark.ems.arenaevents.EMSEventBlock;
 import io.github.basicmark.ems.arenaevents.EMSFillRegion;
 import io.github.basicmark.ems.arenaevents.EMSLightningEffect;
 import io.github.basicmark.ems.arenaevents.EMSMessenger;
 import io.github.basicmark.ems.arenaevents.EMSPotionEffect;
 import io.github.basicmark.ems.arenaevents.EMSSpawnEntity;
 import io.github.basicmark.ems.arenaevents.EMSTeleport;
 import io.github.basicmark.ems.arenaevents.EMSTimer;
 import io.github.basicmark.ems.arenaevents.EMSTracker;
 import io.github.basicmark.util.ChunkWorkBlockLocation;
 import io.github.basicmark.util.DeferChunkWork;
 import io.github.basicmark.util.PlayerDeathInventory;
 import io.github.basicmark.util.TeleportQueue;
 
 import java.util.Date;
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
 import org.bukkit.Chunk;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.entity.Entity;
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
 	protected Location joinSignLocation;
 	protected String welcomeMessage;
 	protected String leaveMessage;
 	protected boolean autoStartEnable;
 	protected int autoStartMinPlayer;
 	protected int autoStartCountdown;
 	protected Set<Entity> arenaEntities;
 	protected boolean allowRejoin;
 	protected int timeLimit;
 	protected int perPeriodLimit;
 	protected Date startTime;
 	protected int timerUpdate;
 	protected boolean disableTeamChat;
 	protected HashMap<String, ArrayList<Player>> joinQueues;
 	
 	
 	/*
 	 *  Note:
 	 *  playersInLobby can be accessed from multiple threads (server thread & client threads
 	 *  for chat message processing) and thus must use a thread safe set implementation
 	 */
 	private Set<Player> playersInLobby;
 	private HashMap<Player, Location> playersLoc;
 	protected Set<Location> protections;
 	protected HashMap<Player, PlayerDeathInventory> playersDeathInv;
 	protected int fullTeams;
 	protected Set<Player> readyPlayers;
 	protected DeferChunkWork<Location, ChunkWorkBlockLocation> deferredBlockUpdates;
 	protected HashMap<Player, EMSPlayerTimeData> playerTimes;
 	protected TeleportQueue teleportQueue;
 	protected EMSManager manager;
 	protected ReferenceInventoryLoader refInvLoader;
 	
 	PlayerStateLoader playerLoader;
 	EMSPlayerRejoinDataLoader playerRejoinLoader;
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
 		this.readyPlayers = new HashSet<Player>();
 		this.arenaEntities = new HashSet<Entity>();
 		this.playerTimes = new HashMap<Player, EMSPlayerTimeData>();
 		this.joinQueues = new HashMap<String, ArrayList<Player>>();
 		this.fullTeams = 0;
 		this.deferredBlockUpdates = null;
 		this.timeLimit = 0;
 		this.perPeriodLimit = 0;
 		this.timerUpdate = -1;
 		this.disableTeamChat = false;
 		this.refInvLoader = null;
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
 		this.joinSignLocation = null;
 		this.welcomeMessage = null;
 		this.welcomeMessage = null;
 		this.autoStartEnable = false;
 		this.autoStartMinPlayer = 0;
 		this.autoStartCountdown = 0;
 		this.allowRejoin = false;
 		
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
 		try {
 			this.autoStartEnable = (boolean) values.get("autostartenable");
 		} catch (Exception e) {
 			this.autoStartEnable = false;
 		}
 		try {
 			this.autoStartMinPlayer = (int) values.get("autostartminplayer");
 		} catch (Exception e) {
 			this.autoStartMinPlayer = 0;
 		}
 		try {
 			this.autoStartCountdown = (int) values.get("autostartcountdown");
 		} catch (Exception e) {
 			this.autoStartCountdown = 0;
 		}
 		try {
 			this.allowRejoin = (boolean) values.get("allowrejoin");
 		} catch (Exception e) {
 			this.allowRejoin = false;
 		}	
 
 		try {
 			this.timeLimit = (int) values.get("timelimit");
 		} catch (Exception e) {
 			this.timeLimit = 0;
 		}
 		try {
 			this.perPeriodLimit = (int) values.get("perperiodlimit");
 		} catch (Exception e) {
 			this.perPeriodLimit = 0;
 		}
 
 		try {
 			this.startTime = (Date) values.get("starttime");
 		} catch (Exception e) {
 			this.startTime = null;
 		}
 
 		try {
 			this.disableTeamChat = (boolean) values.get("disableteamchat");
 		} catch (Exception e) {
 			this.disableTeamChat = false;
 		}
 		
 		// Get optional config data for join sign
 		Map<String, Object> joinSignData;
 		try {
 			joinSignData = (Map<String, Object>) values.get("joinsign");
 		} catch (Exception e) {
 			joinSignData = null;
 		}
 
 		this.joinSignLocation = ConfigUtils.DeserializeLocation(joinSignData);
 		if (this.joinSignLocation != null) {
 			addProtection(joinSignLocation);
 		}
 		
 		try {
 			this.welcomeMessage = (String) values.get("welcomemessage");
 		} catch (Exception e) {
 			welcomeMessage = null;
 		} 
 
 		try {
 			this.leaveMessage = (String) values.get("leavemessage");
 		} catch (Exception e) {
 			leaveMessage = null;
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
 		arenaState = EMSArenaState.fromString((String) values.get("state"));
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
 		values.put("autostartenable", autoStartEnable);
 		values.put("autostartminplayer", autoStartMinPlayer);
 		values.put("autostartcountdown", autoStartCountdown);
 		values.put("allowrejoin", allowRejoin);
 		values.put("timelimit", timeLimit);
 		values.put("perperiodlimit", perPeriodLimit);
 		values.put("starttime", startTime);
 		values.put("disableteamchat", disableTeamChat);
 
 		if (joinSignLocation != null) {
 			values.put("joinsign", ConfigUtils.SerializeLocation(joinSignLocation));
 		}
 
 		if (welcomeMessage != null) {
 			values.put("welcomemessage", welcomeMessage);
 		}
 		if (leaveMessage != null) {
 			values.put("leavemessage", leaveMessage);
 		}
 		
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
 		// The player state when they joined is saved in ems/save/players
 		playerLoader = new PlayerStateLoader(plugin.getDataFolder()+"/save/players");
 		// The player state when they leave an arena is saved in ems/save/arena/<arenaname>
 		playerRejoinLoader = new EMSPlayerRejoinDataLoader(plugin.getDataFolder()+ "/save/arena/" + name);
 		teleportQueue = new TeleportQueue(plugin);
 		
 		if (this.lobby != null) {
 			refInvLoader = new ReferenceInventoryLoader(plugin.getDataFolder() + "/arenas/" + lobby.getWorld().getName() + "/" + name + "_refinvs");
 		}
 		
 		/*
 		 * !!!Bukkit is broken!!!
 		 * It turns out that Bukkit will tell you that a chunk is loaded before
 		 * it is "fully loaded", what this means is that the block data is available
 		 * but the NBT info might not be.
 		 * This is extremely bad news as the whole point of the DeferChunkWork was
 		 * to defer update on blocks which have data attached (in the NBT).
 		 * If we try to access block state in the ChunkLoad event then there is a
 		 * chance we get a bukkit NPE crash in the constructor for the blockstate
 		 * we're trying to access. Full details can be found at:
 		 * https://bukkit.atlassian.net/browse/BUKKIT-1033
 		 * https://forums.bukkit.org/threads/block-getstate-npe-storing-blocks-in-a-list.104828/
 		 * 
 		 * So for the workaround we now have to defer the deferred work another server tick and
 		 * hope that the NBT will be loaded by then. This means that we need to provide the
 		 * plugin data to the DeferWorkChunk class which means we can't update any signs until
 		 * we get to this point where as we really wanted to do it after the arena load :/
 		 */
 		this.deferredBlockUpdates = new DeferChunkWork<Location, ChunkWorkBlockLocation>(plugin);
 		updateStatus(arenaState);
 		if ((arenaState == EMSArenaState.ACTIVE) && allowRejoin) {
 			signalEvent("restart");
 			if ((timerUpdate == -1) && (timeLimit != 0)) {
 				timerUpdate = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new EMSTimeChecker(this), 20 * 60, 20 * 60);
 				if (timerUpdate == -1) {
 					plugin.getServer().getLogger().info("[EMS] Failed to start timer task");
 				}
 			}
 		}
 	}
 	
 	public void setManager(EMSManager manager) {
 		this.manager = manager;
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
 		// TODO: Move elsewhere. We should know the world from the start of the arena creation
 		if (lobby == null) {
			refInvLoader = new ReferenceInventoryLoader(plugin.getDataFolder() + "/arenas/" + lobby.getWorld().getName() + "/" + name + "_refinvs");
 		}
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
 	
 	public void setWelcomeMessage(String message) {
 		welcomeMessage = message;
 	}
 
 	public void setLeaveMessage(String message) {
 		leaveMessage = message;
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
 	
 	public boolean arenaSetAutoStart(boolean enable, int minPlayer, int countdown) {
 		this.autoStartEnable = enable;
 		this.autoStartMinPlayer = minPlayer;
 		this.autoStartCountdown = countdown;
 		return true;
 	}
 	
 	public boolean arenaAllowRejoin(boolean allowRejoin) {
 		this.allowRejoin = allowRejoin;
 		return true;
 	}
 	
 	public boolean arenaTimeLimit(int time, int perPeroid) {
 		this.timeLimit = time;
 		this.perPeriodLimit = perPeroid;
 		return true;
 	}
 	
 	public boolean arenaDisableTeamChat(boolean disable) {
 		this.disableTeamChat = disable;
 		return true;
 	}
 	
 	public boolean arenaSetTeamplayerRespawnLimit(String teamName, boolean teamplayer, int count)
 	{
 		EMSTeam team = teams.get(teamName);
 		team.setTeamRespawnLimit(teamplayer, count);
 		return true;
 	}
 	
 	public boolean arenaAddReferenceInventory(Player player, String name) {
 		refInvLoader.save(name, new ReferenceInventory(player));
 		return true;
 	}
 	
 	public boolean arenaRemoveReferenceInventory(String name) {
 		refInvLoader.delete(name);
 		return true;
 	}
 	
 	public boolean arenaSetTeamReferenceInventory(String teamName, String invName) {
 		if (!teams.containsKey(teamName)) {
 			return false;
 		}
 		
 		if (!refInvLoader.exists(invName)) {
 			return false;
 		}
 		
 		EMSTeam team = teams.get(teamName);
 		team.setReferenceInventory(invName);
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
 	public boolean addTimer(String eventTrigger, String createName, boolean inSeconds, boolean repeat, String timeString, String displayName) {
 		EMSTimer timerEvent = new EMSTimer(this, eventTrigger, createName, inSeconds, repeat, timeString, displayName);
 		if (!timerEvent.parseTimeString()) {
 			return false;
 		}
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
 	
 	public boolean addFillRegion(String triggerName, Location pos1, Location pos2, String blockType) {
 		EMSFillRegion eventFillRegion = new EMSFillRegion(this, triggerName, pos1, pos2, blockType);
 		events.add(eventFillRegion);
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
 	
 	public boolean addLightningEffect(String eventTrigger, Location location) {
 		EMSLightningEffect lightningEffect = new EMSLightningEffect(this, eventTrigger, location);
 		events.add(lightningEffect);
 		return true;
 	}
 	
 	public boolean addPotionEffect(String eventTrigger, PotionEffectType effect, int duration, int amplifier) {
 		EMSPotionEffect potionEffect = new EMSPotionEffect(this, eventTrigger, effect, duration, amplifier);
 		events.add(potionEffect);
 		return true;
 	}
 	
 	// Sign management
 	private boolean setJoinSign(Location location) {
 		if (location != null) {
 			if (joinSignLocation != null) {
 				// Removing protection on the old location
 				removeProtection(joinSignLocation);
 			}
 			// Adding the join sign so add it to the protected block list
 			addProtection(location);
 			joinSignLocation = location;
 			updateStatusSign();
 		} else {
 			// Removing the join sign so remove it from the protected block list
 			if (joinSignLocation != null) {
 				removeProtection(joinSignLocation);
 			}
 			joinSignLocation = null;
 		}
 		return false;
 	}
 
 	private Location getJoinSign() {
 		return joinSignLocation;
 	}
 	
 	public void updateStatusSign() {
 		if (getJoinSign() == null) {
 			return;
 		}
 		
 		String lines[] = new String[4];
 		lines[0] = arenaState.toColourString();
 		lines[1] = name;
 		lines[2] = "";
 		lines[3] = playersLoc.size() + " players";
 		updateSign(getJoinSign(), lines);
 	}
 
 	
 	public boolean signUpdated(Block block, String[] lines) {
 		String line2 = lines[2];
 		if (line2.toLowerCase().equals("[join]")) {
 			setJoinSign(block.getLocation());
 			return true;
 		} else if (teams.containsKey(line2)) {
 			EMSTeam team = teams.get(line2);
 
 			team.setJoinSign(block.getLocation());
 			return true;
 		}
 
 		return false;
 	}
 	
 	public void updateStatus(EMSArenaState newState) {
 		Iterator<EMSTeam> i = teams.values().iterator();
 		
 		// Update the per team signs
 		arenaState = newState;
 
 		// Update the arena join sign
 		updateStatusSign();
 
 		// Update the per team signs
 		while(i.hasNext()) {
 			EMSTeam team = i.next();
 			team.updateStatus(newState);
 		}
 	}
 
 	public boolean signClicked(Sign sign, Player player) {
 		boolean consumeEvent = false;
 		String teamDisplayName = sign.getLine(2);		
 
 		// If there is no team name then it must be an arena join sign
 		if (teamDisplayName.equals("")) {
 			if (playerJoinArena(player)) {
 				consumeEvent = true;
 			}
 		} else {
 			if (playerJoinTeam(player, teamDisplayName)) {
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
 			// Check if the block is the join sign of the arena
 			if (getJoinSign() != null) {
 				if (getJoinSign().equals(location)) {
 					setJoinSign(null);
 				}
 			}
 			
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
 	
 	public void chunkLoad(Chunk chunk) {
 		deferredBlockUpdates.chunkLoad(chunk);
 	}
 	
 	public void chunkUnload(Chunk chunk) {
 		if (arenaState == EMSArenaState.ACTIVE) {
 			if (chunk.getEntities() != null) {
 				Entity chunkEntities[] = chunk.getEntities();
 
 				/*
 				 * TODO:
 				 * It would be better to restrict this check to chunks which
 				 * are within the game field but up till now we haven't
 				 * needed such a concept.
 				 * 
 				 * Check if an entity which is about to be unloaded was created
 				 * by this arena, if it was then remove it from both the chunk
 				 * and our knowledge. This stops entities which where spawned
 				 * by the arena hanging around after the arena event is over
 				 * by being in unloaded chunks which then get loaded back later 
 				 */
 				for (int i=0;i<chunk.getEntities().length;i++) {
 					if (arenaEntities.contains(chunkEntities[i])) {
 						chunkEntities[i].remove();
 						arenaEntities.remove(chunkEntities[i]);
 					}
 				}
 			}
 		}
 	}
 	
 	public boolean editOpen() {
 		if (arenaState == EMSArenaState.EDITING) {
 			// The arena is already in edit state
 			return false;
 		} else if (arenaState != EMSArenaState.CLOSED) {
 			disable(true);
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
 	
 	public void disable(boolean playerRequested) {
 		// TODO Unify arena disable and player leave logic
 		// The arena is being disabled, check if we need to save the players state to allow rejoin
 		if (allowRejoin && (!playerRequested)) {
 			Iterator<Player> pri = getAllPlayers().iterator();
 			while (pri.hasNext()) {
 				Player player = pri.next();
 				EMSPlayerTimeData time = playerTimes.get(player);
 				time.update();
 				EMSPlayerRejoinData rejoin = new EMSPlayerRejoinData(player, playerGetTeam(player), true, time.getActiveTime());
 
 				// Store the players state for if they rejoin
 				playerRejoinLoader.save(player.getName(), rejoin);
 			}
 		}
 		
 		// Disable all the teams
 		endEvent();
 		
 		// Then remove the players from the arena
 		Iterator<Player> pi = playersInLobby.iterator();
 		while(pi.hasNext()) {
 			Player player = pi.next();
 
 			player.sendMessage(ChatColor.RED + "[EMS] The arena you're in is being disabled");
 			playerLeaveArenaDo(player);
 			pi.remove();
 		}
 		
 		if (allowRejoin) {
 			if (playerRequested) {
 				playerRejoinLoader.deleteAll();
 				updateStatus(EMSArenaState.CLOSED);
 			}
 		} else {
 			updateStatus(EMSArenaState.CLOSED);
 		}
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
 		// Only start an event if the previous state of the arena was open
 		if (arenaState != EMSArenaState.OPEN) {
 			sender.sendMessage(ChatColor.RED + "[EMS] The event is already in progress");
 		} else {
 			startEvent();
 		}
 	}
 	
 	protected void startEvent()
 	{
 		Iterator<EMSTeam> i = teams.values().iterator();
 
 		while(i.hasNext()) {
 			EMSTeam team = i.next();
 			team.spawnPlayers(teleportQueue);
 		}
 
 		// TODO
 		// For now signal active although players are pending in the teleport
 		// queue as it will only take a few seconds to process unless we've
 		// 100's of players.
 		// Later we might want to have a new STARTING state and only switch to
 		// active once all players have been teleported
 		teleportQueue.startTeleport();
 
 		updateStatus(EMSArenaState.ACTIVE);
 		signalEvent("start");
 		
 		startTime = new Date();
 
 		if ((timerUpdate == -1) && (timeLimit != 0)) {
 			timerUpdate = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new EMSTimeChecker(this), 20 * 60, 20 * 60);
 			if (timerUpdate == -1) {
 				plugin.getServer().getLogger().info("[EMS] Failed to start timer task");
 			}
 		}
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
 
 		// Remove any remaining entities which the arena knows about
 		for (Entity entity : arenaEntities) {
 			entity.remove();
 		}
 		
 		updateStatus(EMSArenaState.OPEN);
 		if (timerUpdate != -1) {
 			plugin.getServer().getScheduler().cancelTask(timerUpdate);
 			timerUpdate = -1;
 		}
 		
 		// The event has ended so process the join queue
 		processJoinQueue();
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
 	
 	public boolean KickTeamplayer(boolean teamplayer, String name) {
 		Player player = null;
 		
 		if (!teamplayer) {
 			player = plugin.getServer().getPlayer(name);
 			if (player == null) {
 				return false;
 			}
 		}
 
 		Iterator<EMSTeam> i = teams.values().iterator();
 		while(i.hasNext()) {
 			EMSTeam team = i.next();
 			if (teamplayer) {
 				if (team.getName().equals(name)) {
 					team.removeAllPlayers();
 					return true;
 				}
 			} else {
 				if (team.isPlayerInTeam(player)) {
 					team.removePlayer(player);
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	// Player functions
 	@SuppressWarnings("deprecation")
 	public void playerJoinArenaCommon(Player player, boolean playerRequested) {
 		EMSPlayerRejoinData rejoinData = null;
 	
 		// Only try to load the rejoin data if rejoining is allowed
 		if (allowRejoin) {
 			rejoinData = playerRejoinLoader.load(player.getName());
 		}
 
 		if ((!playerRequested) && (rejoinData == null)) {
 			// The player didn't request this join and we haven't seen this player before so let them be
 			return;
 		}
 	
 		if (rejoinData != null) {
 			if ((playerRequested) || ((!playerRequested ) && rejoinData.getAutoRejoin())) {
 				if (timeLimit != 0) {
 					if (rejoinData.getActiveTime() >= getAllowedTime()) {
 						player.sendMessage(ChatColor.GOLD + "[EMS] You've reached your time-limit and are not allowed to rejoin the arena!");
 						return;
 					}
 				}
 				
 				// The player has been here before, should we make them rejoin the arena?
 				playersInLobby.add(player);
 				playersLoc.put(player, player.getLocation());
 				playerLoader.save(player, new PlayerState(player, saveInventory, saveXP, saveHealth));
 				
 				playerTimes.put(player, new EMSPlayerTimeData(player, rejoinData.getActiveTime()));
 				rejoinData.restore(player);
 				if (rejoinData.getTeam() != null) {
 					playerJoinTeamCommon(player, rejoinData.getTeam());
 				}
 				teleportPlayer(player,rejoinData.getLocation());
 				player.sendMessage(ChatColor.GREEN + "[EMS] You have rejoined " + name);
 				if (welcomeMessage != null) {
 					player.sendMessage(ChatColor.GOLD + welcomeMessage);
 				}
 			} else {
 				// The player left of their own accord and didn't request this rejoin so let them be
 				return;
 			}
 		} else {
 			// New player, take them into the lobby
 			playersInLobby.add(player);
 			playersLoc.put(player, player.getLocation());
 			playerLoader.save(player, new PlayerState(player, saveInventory, saveXP, saveHealth));
 
 			playerTimes.put(player, new EMSPlayerTimeData(player));
 			
 			teleportPlayer(player, lobby);
 			player.sendMessage(ChatColor.GREEN + "[EMS] You have joined " + name);
 			if (welcomeMessage != null) {
 				player.sendMessage(ChatColor.GOLD + welcomeMessage);
 			}
 		}
 		
 		// Force an inventory update as if the play joins via a sign although their inventory
 		// gets cleared the player will still see it a ghost form, the only way to do this is
 		// to use a deprecated function :(
 		player.updateInventory();
 		
 		// Update the arena status sign
 		updateStatusSign();
 	}
 
 	public boolean playerJoinArena(Player player) {
 		if (!arenaCommandValid(player, false)) {
 			return false;
 		}
 		
 		/* Before we add the player to our arena check they are not already in one */
 		if (manager.playerInArena(player)) {
 			player.sendMessage(ChatColor.RED + "[EMS] You're already in a different arena. Do /ems leave before joining a new arena");
 			return false;
 		}
 
 		playerJoinArenaCommon(player, true);
 
 		return true;
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
 		player.sendMessage(ChatColor.GREEN + "[EMS] You have now left the arena");
 		if (leaveMessage != null) {
 			player.sendMessage(ChatColor.GOLD + leaveMessage);
 		}
 		teleportPlayer(player,playersLoc.get(player));
 		playersLoc.remove(player);
 		playersDeathInv.remove(player);
 		playerTimes.remove(player);
 
 		PlayerState state = playerLoader.load(player);
 		if (state != null) {
 			state.restore(player);
 			playerLoader.delete(player);
 		}
 
 		// Update the arena status sign
 		updateStatusSign();
 	}
 	
 	public void playerJoinServer(Player player) {
 		// The common function will do the right thing
 		if (arenaState == EMSArenaState.OPEN) {
 			playerJoinArenaCommon(player, false);
 		}
 	}
 	
 	public void playerLeaveServer(Player player) {
 		// If the player is in the arena force them to leave
 		if (playerInArena(player)) {
 			playerLeaveArenaCommon(player, false);
 		}
 	}
 
 	public void playerLeaveArenaCommon(Player player, boolean playerRequested) {
 		/*
 		 * Some arenas allow players to rejoin them. In this case we still go
 		 * through the normal process of restoring their state when they leave
 		 * as it simplifies things in the case where the player is not online when
 		 * the arena is closed.
 		 * However, what we do is save the players current state within the arena
 		 * so when they rejoin we can restore them to the state they where in
 		 * when they left.
 		 */
 		if (allowRejoin) {
 			EMSPlayerTimeData time = playerTimes.get(player);
 			time.update();
 			EMSPlayerRejoinData rejoin = new EMSPlayerRejoinData(player, playerGetTeam(player), !playerRequested, time.getActiveTime());
 
 			// Store the players state for if they rejoin
 			playerRejoinLoader.save(player.getName(), rejoin);
 		}
 		
 		/*
 		 *  Remove the player from the team they are in (if any) which will
 		 *  kick them back into the lobby
 		 */
 		playerLeaveTeam(player);
 
 		/*
 		 *  Check if a player is queueing
 		 */
 		Iterator<ArrayList<Player>> i = joinQueues.values().iterator();
 		while(i.hasNext()) {
 			ArrayList<Player> tmp = i.next();
 			if (tmp.contains(player)) {
 				tmp.remove(player);
 				player.sendMessage(ChatColor.GREEN + "[EMS] Removed you from your current queue as you're leaving");
 			}
 		}
 		
 		/*
 		 * Then check if they are in the lobby and remove them from there
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
 		} else if (arenaState == EMSArenaState.OPEN) {
 			// The arena is open and a player left so check if we can let someone else in
 			processJoinQueue();
 		}
 	}
 
 	public void playerLeaveArena(Player player) {
 		if (!arenaCommandValid(player, true)) {
 			return;
 		}
 
 		playerLeaveArenaCommon(player, true);
 	}
 	
 	public boolean playerJoinTeam(Player player, String teamDisplayName) {
 		if (!playersInLobby.contains(player)) {
 			// Only if a player is in the lobby can they join a team
 			player.sendMessage(ChatColor.RED + "You're not in the lobby!");
 			return false;
 		}
 
 		// The the event is not open then you can't join a team
 		if (!arenaState.isJoinable()) {
 			player.sendMessage(ChatColor.RED + "[EMS] arena is not joinable (" + arenaState.toString().toLowerCase() + ")");
 			return false;
 		}
 		
 		if (!playerJoinTeamCommon(player, teamDisplayName)) {
 			//player.sendMessage(ChatColor.RED + " failed to add you to team " + teamDisplayName);
 			//return false;
 			
 			// Find the array list for the team the player wishes to join
 			ArrayList<Player> list;
 			if (!joinQueues.containsKey(teamDisplayName)) {
 				list = new ArrayList<Player>();
 				joinQueues.put(teamDisplayName, list);
 			} else {
 				list = joinQueues.get(teamDisplayName);
 			}
 			
 			// Check that they are not already in the queue for another team
 			Iterator<ArrayList<Player>> i = joinQueues.values().iterator();
 			while(i.hasNext()) {
 				ArrayList<Player> tmp = i.next();
 				if (tmp.contains(player) && (!tmp.equals(list))) {
 					tmp.remove(player);
 					player.sendMessage(ChatColor.GREEN + "[EMS] Removed you from your current queue as you're joining a different team");
 				}
 			}
 
 			// Finally check if they have already joined the queue for this team
 			if (!list.contains(player)) {
 				list.add(player);
 				player.sendMessage(ChatColor.GREEN + "[EMS] Added you to queue (possition " + list.indexOf(player) + ")");
 			} else {
 				player.sendMessage(ChatColor.RED + "[EMS] You're already in the queue!");
 			}
 		}
 		return true;
 	}
 
 	
 	public void processJoinQueue() {
 		// Walk all the teams trying to add the players into team
 		Iterator<String> i = joinQueues.keySet().iterator();
 		while(i.hasNext()) {
 			String teamDisplayName = i.next();
 			ArrayList<Player> list = joinQueues.get(teamDisplayName);
 			// Walk all the players in the queue until none remain, or the team is full 
 			Iterator<Player> ip = list.iterator();
 			while(ip.hasNext()) {
 				Player player = ip.next();
 				if (!playerJoinTeamCommon(player, teamDisplayName)) {
 					break;
 				}
 				ip.remove();
 			}
 		}
 	}
 	
 	public boolean playerJoinTeamCommon(Player player, String teamDisplayName) {
 		Iterator<EMSTeam> i = teams.values().iterator();
 		while (i.hasNext()) {
 			EMSTeam team = i.next();
 
 			if (team.getDisplayName().equals(teamDisplayName)) {
 				if (team.addPlayer(player)) {
 					playersInLobby.remove(player);
 					if ((fullTeams == teams.size()) && autoStartEnable) {
 						startEvent();
 					}
 					return true;
 				}
 			}
 		}
 		return false;
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
 	
 	public String playerGetTeam(Player player) {
 		// Check all teams for the player and remove them if found
 		Iterator<EMSTeam> i = teams.values().iterator();
 		while(i.hasNext()) {
 			EMSTeam team = i.next();
 			if (team.isPlayerInTeam(player)) {
 				return team.getDisplayName();
 			}
 		}
 		return null;
 	}
 	
 	public List<String> getPlayersList() {
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
 				if (autoStartEnable && (autoStartMinPlayer != 0) && (arenaState.equals(EMSArenaState.OPEN))) {
 					String readyMessage = " (not ready)";
 					Player p = plugin.getServer().getPlayer(player);
 
 					if (readyPlayers.contains(p)) {
 						readyMessage = " (ready)";
 					}
 					playerData.add("-" + player + readyMessage);
 				} else {
 					playerData.add("-" + player);
 				}
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
 					boolean isDead = team.playerDeath(player);
 					Location loc = null;
 
 					// Reset the health here before the death screen gets displayed
 					player.setHealth(player.getMaxHealth());
 					signalEvent("player-death");
 
 					if (!isDead) {
 						loc = team.getRespawnLocation();
 						if (loc == null) {
 							plugin.getServer().getLogger().info("[EMS] failed to respawn player in arena " + name + " as there is no respawn location!");
 							isDead = true;
 						}
 					}
 					if (!isDead) {
 						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, (Runnable) new EMSDeathRunner(player, this, loc, team.getReferenceInventory()), 1);
 						if (keepInvAfterDeath) {
 							playersDeathInv.put(player, new PlayerDeathInventory(player));
 						}
 					} else {
 						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, (Runnable) new EMSDeathRunner(player, this), 1);
 						if (keepInvAfterDeath) {
 							playersDeathInv.put(player, new PlayerDeathInventory(player));
 						}
 
 						/*
 						 * Although the player is in limbo for 1 tick we can signal death & leave
 						 * events here as the player has already been removed from the team
 						 */
 						signalEvent("player-leave");
 						inTeam = true;
 					}
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
 				if (playersInLobby.contains(player) || disableTeamChat) {
 					broadcast("[" + ChatColor.GREEN + "lobby" + ChatColor.RESET + "] " + player.getDisplayName() + ": " + message);
 				} else {
 					Iterator<EMSTeam> i = teams.values().iterator();
 					while(i.hasNext()) {
 						EMSTeam team = i.next();
 						if (team.isPlayerInTeam(player)) {
 							team.broadcast("[" + team.getDisplayName() + ChatColor.RESET + "] " + player.getDisplayName() + ": " + ChatColor.RESET + message);
 							
 							/*
 							 * If the arena is open then players can toggle their ready state if this arena has
 							 * auto-start enabled with a min player limit set
 							 */
 							if (arenaState.equals(EMSArenaState.OPEN) && autoStartEnable && (autoStartMinPlayer != 0)) {
 								if (stringArray[0].equalsIgnoreCase("ready")) {
 									if (!readyPlayers.contains(player)) {
 										readyPlayers.add(player);
 										broadcast(ChatColor.GRAY + player.getName() + " is ready");
 									} else {
 										readyPlayers.remove(player);
 										broadcast(ChatColor.GRAY + player.getName() + " is not ready");
 									}
 
 									if (readyPlayers.size() >= autoStartMinPlayer) {
 										startEvent();
 									} else {
 										broadcast(ChatColor.GRAY + "" + (autoStartMinPlayer - readyPlayers.size()) + " more player(s) required");
 									}
 								}
 							}
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
 		readyPlayers.remove(player);
 		restorePlayer(player);
 		teleportPlayer(player, lobby);
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
 	
 	public Set<Entity> spawnEntities(Location location, EntityType type, int count) {
 		Set<Entity> entities = new HashSet<Entity>();
 
 		for(int i=0;i<count;i++) {
 			Entity entity = location.getWorld().spawnEntity(location, type);
 			if (entity != null) {
 				entities.add(entity);
 			}
 		}
 		arenaEntities.addAll(entities);
 		return entities;
 	}
 
 	public void removeEntities(Set<Entity> entites) {
 		Iterator<Entity> i = entites.iterator();
 
 		while (i.hasNext()) {
 			Entity entity= i.next();
 			if (arenaEntities.contains(entity)) {
 				if (entity.isValid()) {
 					entity.remove();
 				}
 				arenaEntities.remove(entity);
 			}
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
 
 	public Set<Player> getAllPlayers() {
 		Set<Player> players = new HashSet<Player>();
 		Iterator<EMSTeam> i = teams.values().iterator();
 		
 		players.addAll(playersInLobby);
 		while(i.hasNext()) {
 			EMSTeam team = i.next();
 			players.addAll(team.getPlayers());
 		}
 		return players;
 	}
 	
 	public Iterator<EMSPlayerTimeData> getTimeDataIterator() {
 		return playerTimes.values().iterator();
 	}
 	
 	public int getAllowedTime() {
 		Date now = new Date();
 		
 		if (perPeriodLimit == 0) {
 			return timeLimit;
 		} else {
 			/*
 			 * Workout how long the arena has been active, then the number of time periods
 			 * this is before then multiplying that (+1 so we don't have to wait a time period
 			 * before we can join) by the time limit per period.
 			 */
 			int total = (int) ((now.getTime() / (60 * 1000)) - (startTime.getTime() / (60 * 1000)));
 			total /= perPeriodLimit;
 			total = (total + 1) * timeLimit;
 			return total;
 		}
 	}
 	
 	public void teleportPlayer(Player player, Location location) {
 		teleportQueue.removePlayer(player);
 		player.leaveVehicle();
 		player.teleport(location);
 	}
 	
 	static public void updateSignDo(Location location, String[] lines) {
 		Block block = location.getBlock();
 
 		if ((block.getType() == Material.WALL_SIGN) || (block.getType() == Material.SIGN_POST)) {
 			BlockState state = block.getState();
 			Sign sign = (Sign) state;
 
 			sign.setLine(0, lines[0]);
 			sign.setLine(1, lines[1]);
 			sign.setLine(2, lines[2]);
 			sign.setLine(3, lines[3]);
 			sign.update(true);	
 		}
 	}
 	
 	public void updateSign(Location location, String[] lines) {
 		if (location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
 			updateSignDo(location, lines);
 		} else {
 			if (deferredBlockUpdates != null) {
 				deferredBlockUpdates.addWork(location, location, new EMSSignUpdate(location, lines), ChunkWorkBlockLocation.class);
 			}
 		}
 	}
 	
 	public void updateTeamFullStatus(boolean full) {
 		if (full) {
 			fullTeams++;
 		} else {
 			fullTeams--;
 		}
 	}
 	
 	// Private functions
 	private void restorePlayer(Player player) {
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
 				player.sendMessage(ChatColor.RED + "[EMS] You're not in an arena!");
 				return false;
 			}
 		} else {
 			if (playerInArena(player)) {
 				player.sendMessage(ChatColor.RED + "[EMS] You're already in this arena!");
 				return false;
 			}
 		}
 		return true;
 	}
 
 	void arenaRemoveAllEntities() {
 		Iterator<Entity> i = arenaEntities.iterator();
 
 		while (i.hasNext()) {
 			Entity entity= i.next();
 
 			if (entity.isValid()) {
 				entity.remove();
 			}
 			arenaEntities.remove(entity);
 		}
 	}
 	
 	public class EMSDeathRunner implements Runnable {
 		Player player;
 		EMSArena arena;
 		Location location;
 		String referenceInventory;
 
 		public EMSDeathRunner(Player player, EMSArena arena) {
 			this.player = player;
 			this.arena = arena;
 			this.location = null;
 		}
 		
 		public EMSDeathRunner(Player player, EMSArena arena, Location location, String referenceInventory) {
 			this.player = player;
 			this.arena = arena;
 			this.location = location;
 			this.referenceInventory = referenceInventory;
 		}		
 
 		@Override
 		public void run() {
 			//Reset the player to a good state after their death
 			player.setHealth(player.getMaxHealth());
 			player.setFireTicks(0);
 			for (PotionEffect effect : player.getActivePotionEffects()) {
 				player.removePotionEffect(effect.getType());
 			}
 			player.setVelocity(new Vector(0, 0, 0));
 			
 			// Create the respawn event which is now missing as we reset the health to full on death
 			if (location == null) {
 				arena.sendToLobby(player);
 			} else {
 				if (referenceInventory != null) {
 					ReferenceInventory refInv = arena.refInvLoader.load(referenceInventory);
 					refInv.load(player);
 				}
 				teleportPlayer(player, location);
 			}
 			//Bukkit.getPluginManager().callEvent(new PlayerRespawnEvent(player, player.getLocation(), false));
 		}
 	}
 	
 	private class EMSSignUpdate implements Runnable {
 		Location location;
 		String[] lines;
 		
 		public EMSSignUpdate(Location location, String[] lines) {
 			this.location = location;
 			this.lines = lines;
 		}
 		
 		public void run() {
 			EMSArena.updateSignDo(location, lines);
 		}
 	}
 	
 	private class EMSPlayerTimeData {
 		Player player;
 		Date sampleTime;
 		int activeTime;
 
 		EMSPlayerTimeData(Player player, int playedTime) {
 			this.player = player;
 			activeTime = playedTime;
 			sampleTime = new Date();
 		}
 		
 		EMSPlayerTimeData(Player player) {
 			this.player = player;
 			activeTime = 0;
 			sampleTime = new Date();
 		}
 		
 		void update() {
 			Date now = new Date();
 			activeTime += (now.getTime() - sampleTime.getTime()) / (1000 * 60);
 			sampleTime = now;
 		}
 		
 		int getActiveTime() {
 			return activeTime;
 		}
 		
 		Player getPlayer() {
 			return player;
 		}
 	}
 	
 	private class EMSTimeChecker implements Runnable {
 		EMSArena arena;
 		EMSTimeChecker(EMSArena arena) {
 			this.arena = arena;
 		}
 		
 		public void run() {
 			Iterator<EMSPlayerTimeData> i = arena.getTimeDataIterator();
 			
 			while(i.hasNext()) {
 				EMSPlayerTimeData timeData = i.next();
 
 				timeData.update();
 				Bukkit.getServer().getLogger().info("Processed update for player " +timeData.getPlayer().getName() + ": Allowed = " + arena.getAllowedTime() + ", active = " + timeData.getActiveTime());
 				if (timeData.getActiveTime() >= arena.getAllowedTime()) {
 					Player player = timeData.getPlayer();
 					player.sendMessage(ChatColor.GOLD + "[EMS] You've reached your time-limit and have been kicked from the arena!");
 					arena.playerLeaveArena(player);
 				}
 			}
 			Bukkit.getServer().getLogger().info("Processed timer for arena " + arena.getName());
 		}
 	}
 }
