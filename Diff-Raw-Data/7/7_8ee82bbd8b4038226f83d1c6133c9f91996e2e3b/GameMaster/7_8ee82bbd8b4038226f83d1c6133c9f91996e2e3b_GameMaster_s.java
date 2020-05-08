 package net.amoebaman.gamemaster;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import net.amoebaman.gamemaster.api.AutoGame;
 import net.amoebaman.gamemaster.api.GameMap;
 import net.amoebaman.gamemaster.enums.MasterStatus;
 import net.amoebaman.gamemaster.enums.PlayerStatus;
 import net.amoebaman.gamemaster.modules.MessagerModule;
 import net.amoebaman.gamemaster.utils.ChatUtils;
 import net.amoebaman.gamemaster.utils.CommandController;
 import net.amoebaman.gamemaster.utils.PlayerMap;
 import net.amoebaman.gamemaster.utils.S_Loc;
 import net.amoebaman.gamemaster.utils.ChatUtils.ColorScheme;
 import net.amoebaman.statmaster.StatMaster;
 import net.amoebaman.statmaster.Statistic;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Chest;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.scoreboard.Criterias;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Scoreboard;
 
 
 /**
  * 
  * The main functioning class of GameMaster that stores and manages most important information.
  * This is a complete rewrite of the code based on a better-functioning and more flexible system for maps that was devised later.
  * The potential sloppiness of surgically inserting the new map system made a full refactoring seem more appealing to me.
  * For the old code, see <code>com.amoebaman.gamemaster.GameMaster.java</code>
  * 
  * @author Dennison Richter
  *
  */
 public class GameMaster extends JavaPlugin{
 	
 	protected static final Set<AutoGame> games = new HashSet<AutoGame>();
 	protected static final Set<GameMap> maps = new HashSet<GameMap>();
 	public static final List<GameMap> mapHistory = new ArrayList<GameMap>();
 	
 	public static final PlayerMap<PlayerStatus> players = new PlayerMap<PlayerStatus>(PlayerStatus.PLAYING);
 	public static final PlayerMap<Long> lastDamage = new PlayerMap<Long>(0L);
 	public static final Set<Player> respawning = new HashSet<Player>();
 	public static final Set<Player> teamChatters = new HashSet<Player>();
 	
 	public static final Map<CommandSender, String> votes = new HashMap<CommandSender, String>();
 	
 	public static AutoGame activeGame, nextGame, lastGame;
 	public static GameMap activeMap, nextMap;
 	
 	public static MasterStatus status;
 	public static Location mainLobby, fireworksLaunch;
 	public static GameMap editMap;
 	public static int recurringOpsTaskID, worldTimeLock;
 	public static long gameStart;
 	public static boolean debugCycle;
 	
 	
 	protected static String mainDir;
 	protected static File configFile, mapsFile, repairFile;
 	
 	public void onEnable(){
 		/*
 		 * Initialize listeners
 		 */
 		Bukkit.getPluginManager().registerEvents(new EventListener(), this);
 		CommandController.registerCommands(new CommandListener(), this);
 		/*
 		 * Establish files and directories
 		 */
 		getDataFolder().mkdirs();
 		mainDir = getDataFolder().getPath();
 		configFile = getConfigFile("config");
 		mapsFile = getConfigFile("maps");
 		repairFile = getConfigFile("repair");
 		/*
 		 * Load up configurations
 		 */
 		try{
 			getConfig().options().pathSeparator('/');
 			getConfig().load(configFile);
 			getConfig().options().copyDefaults();
 			getConfig().save(configFile);
 			mainLobby = S_Loc.stringLoad(getConfig().getString("main-lobby"));
 			if(mainLobby == null)
 				mainLobby = new Location(Bukkit.getWorlds().get(0), 0.5, 80, 0.5);
 			fireworksLaunch = S_Loc.stringLoad(getConfig().getString("fireworks-launch"));
 			if(fireworksLaunch == null)
 				fireworksLaunch = mainLobby.clone();
 			
 			YamlConfiguration mapsYaml = new YamlConfiguration();
 			mapsYaml.options().pathSeparator('/');
 			mapsYaml.load(mapsFile);
 			for(String name : mapsYaml.getKeys(false)){
 				ConfigurationSection mapSection = mapsYaml.getConfigurationSection(name);
 				GameMap map = new GameMap(name);
 				for(String key : mapSection.getKeys(true))
 					if(!mapSection.isConfigurationSection(key))
 						map.properties.set(key, mapSection.get(key));
 				registerMap(map);
 			}
 			
 			repair();
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 		/*
 		 * Update the statuses of players with admin permissions
 		 * This is needed in the event of a reload
 		 */
 		Bukkit.setDefaultGameMode(GameMode.SURVIVAL);
 		for(Player player : Bukkit.getOnlinePlayers())
 			if(player.hasPermission("arenamaster.admin"))
 				changeStatus(player, PlayerStatus.ADMIN);
 			else{
 				changeStatus(player, PlayerStatus.PLAYING);
 				player.teleport(mainLobby);
 			}
 		/*
 		 * Register statistics with the StatMaster
 		 */
 		StatMaster.getHandler().registerStat(new Statistic("Charges", 0));
 		StatMaster.getHandler().registerStat(new Statistic("Wins", 0, "games", "default"));
 		StatMaster.getHandler().registerStat(new Statistic("Losses", 0, "games", "default"));
 		StatMaster.getHandler().registerCommunityStat(new Statistic("Big games", 0));
 		StatMaster.getHandler().registerCommunityStat(new Statistic("Votes", 0));
 		/*
 		 * Schedule recurring ops
 		 */
 		recurringOpsTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new RecurringOps(), 0, 10L);
 		/*
 		 * Add player health to the scoreboard
 		 */
 		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
 		Objective objective = board.getObjective("health");
 		if(objective == null)
 			objective = board.registerNewObjective("health", Criterias.HEALTH);
 		objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
 		objective.setDisplayName(" Heath");
 		/*
 		 * Start the works
 		 */
 		GameFlow.startIntermission();
 	}
 	
 	public void onDisable(){
 		/*
 		 * Save up configurations
 		 */
 		try{
 			getConfig().set("main-lobby", S_Loc.stringSave(mainLobby));
 			getConfig().set("fireworks-launch", S_Loc.stringSave(fireworksLaunch));
 			getConfig().save(configFile);
 			
 			YamlConfiguration mapsYaml = new YamlConfiguration();
 			mapsYaml.options().pathSeparator('/');
 			for(GameMap map : maps)
 				mapsYaml.createSection(map.name, map.properties.getValues(true));
 			mapsYaml.save(mapsFile);
 			
 			repair();
 		}
 		catch(Exception e){ e.printStackTrace(); }
 	}
 	
 	public static File getConfigFile(String name){
 		try{
 			File file = new File(plugin().getDataFolder().getPath() + File.separator + name + ".yml");
 			if(!file.exists()){
 				plugin().getLogger().info("Loading pre-defined contents of " + name + ".yml");
 				file.createNewFile();
 				file.setWritable(true);
 				InputStream preset = GameMaster.class.getResourceAsStream("/" + name + ".yml");
 				if(preset != null){
 					BufferedReader reader = new BufferedReader(new InputStreamReader(preset));
 					BufferedWriter writer = new BufferedWriter(new FileWriter(file));
 					while(reader.ready()){
 						writer.write(reader.readLine());
 						writer.newLine();
 					}
 					reader.close();
 					writer.close();
 				}
 			}
 			return file;
 		}
 		catch(Exception e){
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public static JavaPlugin plugin(){ return (JavaPlugin) Bukkit.getPluginManager().getPlugin("GameMaster"); }
 	
 	public static Logger logger(){ return plugin().getLogger(); }
 	
 	public static void registerGame(AutoGame game){
 		if(game != null){
 			if(games.add(game))
 				logger().info("Successfully registered arena game named " + game.getGameName());
 			else
 				logger().warning("Failed to re-register arena game named " + game.getGameName());
 		}
 		else
 			logger().warning("Failed to register null arena game");
 	}
 	
 	protected static void deregisterGame(AutoGame game){
 		games.remove(game);
 	}
 	
 	public static AutoGame getRegisteredGame(String name){
 		if(name == null)
 			return null;
 		name = name.replace('-', ' ').replace('_', ' ').toLowerCase();
 		for(AutoGame game : games)
 			if(game.getGameName().equalsIgnoreCase(name))
 				return game;
 		for(AutoGame game : games)
 			if(game.getGameName().toLowerCase().startsWith(name))
 				return game;
 		for(AutoGame game : games)
 			for(String alias : game.getAliases())
 				if(alias.equalsIgnoreCase(name))
 					return game;
 		return null;
 	}
 	
 	public static void registerMap(GameMap map){
 		if(map != null){
 			if(maps.add(map))
 				logger().info("Successfully loaded arena map named " + map.name);
 			else
 				logger().warning("Failed to re-load arena map named " + map.name);
 		}
 		else
 			logger().warning("Failed to load null arena map");
 	}
 	
 	protected static void deregisterMap(GameMap map){
 		maps.remove(map);
 	}
 	
 	public static GameMap getRegisteredMap(String name){
 		for(GameMap map : maps)
 			if(map.name.equalsIgnoreCase(name))
 				return map;
 		for(GameMap map : maps)
 			if(map.name.toLowerCase().startsWith(name.toLowerCase()))
 				return map;
 		return null;
 	}
 	
 	public static Set<GameMap> getCompatibleMaps(AutoGame game){
 		Set<GameMap> set = new HashSet<GameMap>();
 		for(GameMap map : maps)
 			if(game.isCompatible(map))
 				set.add(map);
 		return set;
 	}
 	
 	public static PlayerStatus getStatus(Player player){
 		return players.get(player);
 	}
 	
 	public static void changeStatus(Player player, PlayerStatus pStatus){
 		players.put(player, pStatus == null ? PlayerStatus.PLAYING : pStatus);
 		resetPlayer(player);
 		lastDamage.remove(player);
 		respawning.remove(player);
 		if(status.active){
 			switch(pStatus){
 				case ADMIN:
 				case EXTERIOR:
 					activeGame.removePlayer(player);
 					break;
 				case PLAYING:
 					activeGame.addPlayer(player);
 					if(activeGame instanceof MessagerModule){
 						player.sendMessage(ChatUtils.spacerLine());
 						for(String line : ((MessagerModule) activeGame).getSpawnMessage(player))
 							player.sendMessage(ChatUtils.centerAlign(ChatUtils.format(line, ColorScheme.HIGHLIGHT)));
 						player.sendMessage(ChatUtils.spacerLine());
 					}
 					break;
 			}
 		}
 		else if(pStatus == PlayerStatus.PLAYING)
 			player.teleport(mainLobby);
 		switch(pStatus){
 			case ADMIN:
 				player.setGameMode(GameMode.CREATIVE);
 				break;
 			case EXTERIOR:
 			case PLAYING:
 				player.setGameMode(GameMode.SURVIVAL);
 				break;
 		}
 	}
 	
 	/**
 	 * Gets a list of all currently online players who are participanting in the game.
 	 * This method guarantees to return no null elements in the set, in addition to no offline players.
 	 * @return a set containing all participating players
 	 */
 	public static Set<Player> getPlayers(){
 		Set<Player> players = new HashSet<Player>();
 		for(Player player : Bukkit.getOnlinePlayers())
 			if(player.isOnline() && getStatus(player) == PlayerStatus.PLAYING)
 				players.add(player);
 		players.remove(null);
 		return players;
 	}
 	
 	public static void clearInventory(Player player){
 		player.closeInventory();
 		player.getInventory().clear();
 		player.getInventory().setArmorContents(null);
 		for(PotionEffect effect : player.getActivePotionEffects())	
 			player.removePotionEffect(effect.getType());
 	}
 	
 	public static void resetPlayer(Player player){
 		clearInventory(player);
 		player.setDisplayName(player.getName());
 		player.setPlayerListName(player.getName());
 		lastDamage.remove(player);
 		respawning.remove(player);
 	}
 	
 	public static void updatePlayerColors(){
 		for(Player player : Bukkit.getOnlinePlayers()){
 			String colorName = player.getName();
 			if(getStatus(player) == PlayerStatus.PLAYING && status.active)
 				colorName = GameMaster.activeGame.getNameColor(player) + colorName;
 			player.setDisplayName(colorName);
 		}
 	}
 	
 	/**
 	 * Runs through the votes map and returns the option that has recieved the most votes.
 	 * @return the most voted-for option
 	 */
 	public static String getMostVoted(){
 		Map<String, Integer> tally = new HashMap<String, Integer>();
 		for(String vote : votes.values()){
 			if(!tally.containsKey(vote))
 				tally.put(vote, 0);
 			tally.put(vote, tally.get(vote) + 1);
 		}
 		String mostVoted = null;
 		int mostVotes = 0;
 		for(String vote : tally.keySet())
 			if(tally.get(vote) > mostVotes){
 				mostVoted = vote;
 				mostVotes = tally.get(vote);
 			}
 		return mostVoted;
 	}
 	
 	/**
 	 * Gets the killer of a player.
 	 * This method will first try to use Player.getKiller().
 	 * If that call returns null, it will examine the player's last damage cause and attempt to determine the killer from that.
 	 * If no killer can be found, it will return null.
 	 * @param victim the recently deceased
 	 * @return the player responsible for the death, or null if none was found
 	 */
 	public static Player getKiller(Player victim){
 		Player killer = victim.getKiller();
 		if(killer == null && victim.getLastDamageCause() instanceof EntityDamageByEntityEvent){
 			EntityDamageByEntityEvent damage = (EntityDamageByEntityEvent) victim.getLastDamageCause();
 			if(damage.getDamager() instanceof Player)
 				killer = (Player) damage.getDamager();
 			if(damage.getDamager() instanceof Wolf && ((Wolf) damage.getDamager()).getOwner() instanceof Player)
 				killer = (Player) ((Wolf) damage.getDamager()).getOwner();
 			if(damage.getDamager() instanceof Projectile && ((Projectile) damage.getDamager()).getShooter() instanceof Player)
 				killer = (Player) ((Projectile) damage.getDamager()).getShooter();
 		}
 		return killer;
 	}
 	
	public static void defRepair(Set<BlockState> states){
 		YamlConfiguration repairYaml = new YamlConfiguration();
 		repairYaml.options().pathSeparator('/');
 		for(BlockState state : states)
 			repairYaml.set(S_Loc.stringSave(state.getLocation()), state.getTypeId() + " " + state.getRawData());
 		try {
 	        repairYaml.save(repairFile);
         }
         catch (Exception e) {
 	        e.printStackTrace();
         }
 	}
 	
	public static void repair(){
 		YamlConfiguration repairYaml = new YamlConfiguration();
 		repairYaml.options().pathSeparator('/');
 		try {
 	        repairYaml.load(repairFile);
 	        repairFile.delete();
 	        repairFile.createNewFile();
         }
         catch (Exception e) {
 	        e.printStackTrace();
 	        return;
         }
 		for(String key : repairYaml.getKeys(false)){
 			Block block = S_Loc.stringLoad(key).getBlock();
 			String[] split = repairYaml.getString(key).split(" ");
 			if(block.getType() == Material.CHEST)
 				((Chest) block.getState()).getBlockInventory().clear();
 			block.setTypeIdAndData(Integer.parseInt(split[0]), Byte.parseByte(split[1]), false);
 		}
 	}
 }
