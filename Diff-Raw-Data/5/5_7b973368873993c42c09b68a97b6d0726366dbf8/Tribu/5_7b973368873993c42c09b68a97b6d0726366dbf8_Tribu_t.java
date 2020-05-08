 /*******************************************************************************
  * Copyright or  or Copr. Quentin Godron (2011)
  * 
  * cafe.en.grain@gmail.com
  * 
  * This software is a computer program whose purpose is to create zombie 
  * survival games on Bukkit's server. 
  * 
  * This software is governed by the CeCILL-C license under French law and
  * abiding by the rules of distribution of free software.  You can  use, 
  * modify and/ or redistribute the software under the terms of the CeCILL-C
  * license as circulated by CEA, CNRS and INRIA at the following URL
  * "http://www.cecill.info". 
  * 
  * As a counterpart to the access to the source code and  rights to copy,
  * modify and redistribute granted by the license, users are provided only
  * with a limited warranty  and the software's author,  the holder of the
  * economic rights,  and the successive licensors  have only  limited
  * liability. 
  * 
  * In this respect, the user's attention is drawn to the risks associated
  * with loading,  using,  modifying and/or developing or reproducing the
  * software by the user in light of its specific status of free software,
  * that may mean  that it is complicated to manipulate,  and  that  also
  * therefore means  that it is reserved for developers  and  experienced
  * professionals having in-depth computer knowledge. Users are therefore
  * encouraged to load and test the software's suitability as regards their
  * requirements in conditions enabling the security of their systems and/or 
  * data to be ensured and,  more generally, to use and operate it in the 
  * same conditions as regards security. 
  * 
  * The fact that you are presently reading this means that you have had
  * knowledge of the CeCILL-C license and that you accept its terms.
  ******************************************************************************/
 package graindcafe.tribu;
 
 import graindcafe.tribu.Configuration.Constants;
 import graindcafe.tribu.Configuration.TribuConfig;
 import graindcafe.tribu.Executors.CmdDspawn;
 import graindcafe.tribu.Executors.CmdIspawn;
 import graindcafe.tribu.Executors.CmdTribu;
 import graindcafe.tribu.Executors.CmdZspawn;
 import graindcafe.tribu.Inventory.TribuInventory;
 import graindcafe.tribu.Inventory.TribuTempInventory;
 import graindcafe.tribu.Level.LevelFileLoader;
 import graindcafe.tribu.Level.LevelSelector;
 import graindcafe.tribu.Level.TribuLevel;
 import graindcafe.tribu.Listeners.TribuBlockListener;
 import graindcafe.tribu.Listeners.TribuEntityListener;
 import graindcafe.tribu.Listeners.TribuPlayerListener;
 import graindcafe.tribu.Listeners.TribuWorldListener;
 import graindcafe.tribu.Rollback.ChunkMemory;
 import graindcafe.tribu.TribuZombie.EntityTribuZombie;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Method;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Random;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import me.graindcafe.gls.DefaultLanguage;
 import me.graindcafe.gls.Language;
 import net.minecraft.server.EntityTypes;
 import net.minecraft.server.EntityZombie;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Villager;
 import org.bukkit.entity.Wolf;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * @author Graindcafe
  * 
  */
 public class Tribu extends JavaPlugin {
 
 	public static String getExceptionMessage(Exception e) {
 		String message = e.getLocalizedMessage() + "\n";
 		for (StackTraceElement st : e.getStackTrace())
 			message += "[" + st.getFileName() + ":" + st.getLineNumber() + "] " + st.getClassName() + "->" + st.getMethodName() + "\n";
 		return message;
 	}
 
 	/**
 	 * Send a message to a player or the console
 	 * 
 	 * @param sender
 	 *            The one to send a message
 	 * @param message
 	 *            The message
 	 */
 	public static void messagePlayer(CommandSender sender, String message) {
 		if (!message.isEmpty())
 			if (sender == null)
 				Logger.getLogger("Minecraft").info(ChatColor.stripColor(message));
 			else
 				sender.sendMessage(message);
 	}
 
 	private int aliveCount;
 	private TribuBlockListener blockListener;
 
 	private TribuConfig config;
 	private TribuEntityListener entityListener;
 	private TribuInventory inventorySave;
 	private boolean isRunning;
 	private Language language;
 	private TribuLevel level;
 
 	private LevelFileLoader levelLoader;
 
 	private LevelSelector levelSelector;
 	private Logger log;
 
 	private ChunkMemory memory;
 	private TribuPlayerListener playerListener;
 	private HashMap<Player, PlayerStats> players;
 
 	private Random rnd;
 	private LinkedList<PlayerStats> sortedStats;
 
 	private TribuSpawner spawner;
 	private HashMap<Player, Location> spawnPoint;
 	private SpawnTimer spawnTimer;
 	private HashMap<Player, TribuTempInventory> tempInventories;
 	private int waitingPlayers = 0;
 
 	private WaveStarter waveStarter;
 	private TribuWorldListener worldListener;
 
 	/**
 	 * Add packages from config file to the level
 	 */
 	public void addDefaultPackages() {
 		if (level != null && this.config.DefaultPackages != null)
 			for (Package pck : this.config.DefaultPackages) {
 				level.addPackage(pck);
 			}
 	}
 
 	/**
 	 * Add a player to the game
 	 * 
 	 * @param player
 	 *            player to add
 	 */
 	public void addPlayer(Player player) {
 		if (player != null && !players.containsKey(player)) {
 
 			if (config.PlayersStoreInventory) {
 				inventorySave.addInventory(player);
 				player.getInventory().clear();
 			}
 			PlayerStats stats = new PlayerStats(player);
 			players.put(player, stats);
 			sortedStats.add(stats);
 			messagePlayer(player, this.getLocale("Message.YouJoined"));
 			if (waitingPlayers != 0) {
 				waitingPlayers--;
 				if (waitingPlayers == 0) {
 					// No need to delay if everyone is playing
 					if (config.PluginModeServerExclusive)
 						startRunning();
 					else
 						startRunning(config.LevelStartDelay);
 				}
 			} else if (getLevel() != null && isRunning) {
 				player.teleport(level.getDeathSpawn());
 				messagePlayer(player, language.get("Message.GameInProgress"));
 			}
 		}
 	}
 
 	/**
 	 * Add a player to the game in timeout seconds
 	 * 
 	 * @param player
 	 *            Player to add
 	 * @param timeout
 	 *            Time to wait in seconds
 	 */
 	public void addPlayer(final Player player, final double timeout) {
 		this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 			public void run() {
 				addPlayer(player);
 			}
 		}, Math.round(Constants.TicksBySecond * timeout));
 	}
 
 	/**
 	 * Broadcast message to every players on the server
 	 * 
 	 * @param message
 	 *            Message to broadcast
 	 */
 	public void broadcast(String msg) {
 		if (msg.isEmpty())
 			getServer().broadcastMessage(msg);
 	}
 
 	/**
 	 * Broadcast message to every players on the server after formating the
 	 * given language node
 	 * 
 	 * @param languageNode
 	 *            The language node to format
 	 * @param params
 	 *            The arguments to pass to the language node
 	 */
 	public void broadcast(String languageNode, Object... params) {
 		broadcast(String.format(getLocale(languageNode), params));
 	}
 
 	/**
 	 * Broadcast message to every players on the server with given permission
 	 * 
 	 * @param message
 	 *            Message to broadcast
 	 * @param permission
 	 *            Permission to have
 	 */
 	public void broadcast(String message, String permission) {
 		if (message.isEmpty())
 			getServer().broadcast(message, permission);
 	}
 
 	/**
 	 * Broadcast message to every players on the server with the given
 	 * permission after formating the given language node
 	 * 
 	 * @param languageNode
 	 *            The language node to format
 	 * @param params
 	 *            The arguments to pass to the language node
 	 */
 	public void broadcast(String languageNode, String permission, Object... params) {
 		broadcast(String.format(getLocale(languageNode), params), permission);
 	}
 
 	public void checkAliveCount() {
 		// log.info("checking alive count " + aliveCount);
 
 		if (this.aliveCount == 0 && isRunning) {
 			messagePlayers(language.get("Message.ZombieHavePrevailed"));
 			messagePlayers(String.format(language.get("Message.YouHaveReachedWave"), String.valueOf(getWaveStarter().getWaveNumber())));
 			stopRunning(true);
 			if (getPlayersCount() != 0)
 				getLevelSelector().startVote(Constants.VoteDelay);
 		}
 	}
 
 	/**
 	 * Get the current TribuConfig
 	 * 
 	 * @return
 	 */
 	public TribuConfig config() {
 		return config;
 	}
 
 	/**
 	 * Get the number of players that are not dead
 	 * 
 	 * @return
 	 */
 	public int getAliveCount() {
 		return aliveCount;
 	}
 
 	public ChunkMemory getChunkMemory() {
 		return memory;
 	}
 
 	public TribuLevel getLevel() {
 		return level;
 	}
 
 	public LevelFileLoader getLevelLoader() {
 		return levelLoader;
 	}
 
 	public LevelSelector getLevelSelector() {
 		return levelSelector;
 	}
 
 	public String getLocale(String key) {
 		return language.get(key);
 	}
 
 	/**
 	 * Get the nearest player from a location
 	 * 
 	 * @param location
 	 * @return nearest player
 	 */
 	public Player getNearestPlayer(Location location) {
 		Player minPlayer = null;
 		double minVal = -1d;
 		double d;
 		for (Player p : players.keySet()) {
 			d = location.distanceSquared(p.getLocation());
 			if (minVal > d) {
 				minVal = d;
 				minPlayer = p;
 			}
 		}
 		return minPlayer;
 	}
 
 	public Set<Player> getPlayers() {
 		return this.players.keySet();
 	}
 
 	public int getPlayersCount() {
 		return this.players.size();
 	}
 
 	public Player getRandomPlayer() {
 		return sortedStats.get(rnd.nextInt(sortedStats.size())).getPlayer();
 	}
 
 	public LinkedList<PlayerStats> getSortedStats() {
 		Collections.sort(this.sortedStats);
 		return this.sortedStats;
 	}
 
 	public TribuSpawner getSpawner() {
 		return spawner;
 	}
 
 	public SpawnTimer getSpawnTimer() {
 		return spawnTimer;
 	}
 
 	public PlayerStats getStats(Player player) {
 		return players.get(player);
 	}
 
 	public WaveStarter getWaveStarter() {
 		return waveStarter;
 	}
 
 	/**
 	 * Init default language and if there is a config the chosen language
 	 */
 	private void initLanguage() {
 		DefaultLanguage.setAuthor("Graindcafe");
 		DefaultLanguage.setName("English");
 		DefaultLanguage.setVersion(Constants.LanguageFileVersion);
 		DefaultLanguage.setLanguagesFolder(Constants.languagesFolder);
 		DefaultLanguage.setLocales(new HashMap<String, String>() {
 			private static final long serialVersionUID = 9166935722459443352L;
 			{
 				put("File.DefaultLanguageFile",
 						"# This is your default language file \n# You should not edit it !\n# Create another language file (custom.yml) \n# and put 'Default: english' if your default language is english\n");
 				put("File.LanguageFileComplete", "# Your language file is complete\n");
 				put("File.TranslationsToDo", "# Translations to do in this language file\n");
 				put("Sign.Buy", "Buy");
 				put("Sign.ToggleSpawner", "Spawn's switch");
 				put("Sign.Spawner", "Zombie Spawner");
 				put("Sign.HighscoreNames", "Top Names");
 				put("Sign.HighscorePoints", "Top Points");
 				put("Sign.TollSign", "Pay");
 				put("Message.Stats", ChatColor.GREEN + "Ranking of  best zombies killers : ");
 				put("Message.UnknownItem", ChatColor.YELLOW + "Sorry, unknown item");
 				put("Message.ZombieSpawnList", ChatColor.GREEN + "%s");
 				put("Message.ConfirmDeletion", ChatColor.YELLOW + "Please confirm the deletion of the %s level by redoing the command");
 				put("Message.ThisOperationIsNotCancellable", ChatColor.RED + "This operation is not cancellable!");
 				put("Message.LevelUnloaded", ChatColor.GREEN + "Level successfully unloaded");
 				put("Message.InvalidVote", ChatColor.RED + "Invalid vote");
 				put("Message.ThankyouForYourVote", ChatColor.GREEN + "Thank you for your vote");
 				put("Message.YouCannotVoteAtThisTime", ChatColor.RED + "You cannot vote at this time");
 				put("Message.LevelLoadedSuccessfully", ChatColor.GREEN + "Level loaded successfully");
 				put("Message.LevelIsAlreadyTheCurrentLevel", ChatColor.RED + "Level %s is already the current level");
 				put("Message.UnableToSaveLevel", ChatColor.RED + "Unable to save level, try again later");
 				put("Message.UnableToCreatePackage", ChatColor.RED + "Unable to create package, try again later");
 				put("Message.UnableToLoadLevel", ChatColor.RED + "Unable to load level");
 				put("Message.NoLevelLoaded", ChatColor.YELLOW + "No level loaded, type '/tribu load' to load one,");
 				put("Message.NoLevelLoaded2", ChatColor.YELLOW + "or '/tribu create' to create a new one,");
 				put("Message.TeleportedToDeathSpawn", ChatColor.GREEN + "Teleported to death spawn");
 				put("Message.DeathSpawnSet", ChatColor.GREEN + "Death spawn set.");
 				put("Message.TeleportedToInitialSpawn", ChatColor.GREEN + "Teleported to initial spawn");
 				put("Message.InitialSpawnSet", ChatColor.GREEN + "Initial spawn set.");
 				put("Message.UnableToSaveCurrentLevel", ChatColor.RED + "Unable to save current level.");
 				put("Message.LevelSaveSuccessful", ChatColor.GREEN + "Level save successful");
 				put("Message.LevelCreated", ChatColor.GREEN + "Level " + ChatColor.LIGHT_PURPLE + "%s" + ChatColor.GREEN + " created");
 				put("Message.UnableToDeleteLevel", ChatColor.RED + "Unable to delete current level.");
 				put("Message.PackageCreated", ChatColor.RED + "Package created successfully");
 				put("Message.LevelDeleted", ChatColor.GREEN + "Level deleted successfully.");
 				put("Message.Levels", ChatColor.GREEN + "Levels: %s");
 				put("Message.UnknownLevel", ChatColor.RED + "Unknown level: %s");
 				put("Message.MaybeNotSaved", ChatColor.YELLOW + "Maybe you have not saved this level or you have not set anything in.");
 				put("Message.ZombieModeEnabled", ChatColor.GREEN + "Zombie Mode enabled!");
 				put("Message.ZombieModeDisabled", ChatColor.RED + "Zombie Mode disabled!");
 				put("Message.SpawnpointAdded", ChatColor.GREEN + "Spawnpoint added");
 				put("Message.SpawnpointRemoved", ChatColor.GREEN + "Spawnpoint removed");
 				put("Message.InvalidSpawnName", ChatColor.RED + "Invalid spawn name");
 				put("Message.TeleportedToZombieSpawn", ChatColor.GREEN + "Teleported to zombie spawn " + ChatColor.LIGHT_PURPLE + "%s");
 				put("Message.UnableToGiveYouThatItem", ChatColor.RED + "Unable to give you that item...");
 				put("Message.PurchaseSuccessfulMoney", ChatColor.GREEN + "Purchase successful." + ChatColor.DARK_GRAY + " Money: " + ChatColor.GRAY
 						+ "%s $");
 				put("Message.YouDontHaveEnoughMoney", ChatColor.DARK_RED + "You don't have enough money for that!");
 				put("Message.MoneyPoints", ChatColor.DARK_GRAY + "Money: " + ChatColor.GRAY + "%s $" + ChatColor.DARK_GRAY + " Points: "
 						+ ChatColor.GRAY + "%s");
 				put("Message.GameInProgress", ChatColor.YELLOW + "Game in progress, you will spawn next round");
 				put("Message.ZombieHavePrevailed", ChatColor.DARK_RED + "Zombies have prevailed!");
 				put("Message.YouHaveReachedWave", ChatColor.RED + "You have reached wave " + ChatColor.YELLOW + "%s");
 				put("Message.YouJoined", ChatColor.GOLD + "You joined the human strengths against zombies.");
 				put("Message.YouLeft", ChatColor.GOLD + "You left the fight against zombies.");
 				put("Message.TribuSignAdded", ChatColor.GREEN + "Tribu sign successfully added.");
 				put("Message.TribuSignRemoved", ChatColor.GREEN + "Tribu sign successfully removed.");
 				put("Message.ProtectedBlock", ChatColor.YELLOW + "Sorry, this sign is protected, please ask an operator to remove it.");
 				put("Message.CannotPlaceASpecialSign", ChatColor.YELLOW + "Sorry, you cannot place a special signs, please ask an operator to do it.");
 				put("Message.ConfigFileReloaded", ChatColor.GREEN + "Config files have been reloaded.");
 				put("Message.PckNotFound", ChatColor.YELLOW + "Package %s not found in this level.");
 				put("Message.PckNeedName", ChatColor.YELLOW + "You have to specify the name of the package.");
 				put("Message.PckNeedOpen", ChatColor.YELLOW + "You have to open or create a package first.");
 				put("Message.PckNeedId", ChatColor.YELLOW + "You have to specify the at least the id.");
 				put("Message.PckNeedSubId", ChatColor.YELLOW + "You have to specify the id and subid.");
 				put("Message.PckCreated", ChatColor.GREEN + "The package %s has been created.");
 				put("Message.PckOpened", ChatColor.GREEN + "The package %s has been opened.");
 				put("Message.PckSaved", ChatColor.GREEN + "The package %s has been saved and closed.");
 				put("Message.PckRemoved", ChatColor.GREEN + "The package has been removed.");
 				put("Message.PckItemDeleted", ChatColor.GREEN + "The item has been deleted.");
 				put("Message.PckItemAdded", ChatColor.GREEN + "The item \"%s\" has been successfully added.");
 				put("Message.PckItemAddFailed", ChatColor.YELLOW + "The item \"%s\" could not be added.");
 				put("Message.PckList", ChatColor.GREEN + "Packages of this level : %s.");
 				put("Message.PckNoneOpened", ChatColor.YELLOW + "none opened/specified");
 				put("Message.LevelNotReady", ChatColor.YELLOW
 						+ "The level is not ready to run. Make sure you create/load a level and that it contains zombie spawns.");
 				put("Message.Deny", ChatColor.RED + "A zombie denied your action, sorry.");
 				put("Message.PlayerDied", ChatColor.LIGHT_PURPLE + "%s" + ChatColor.RED + " has died.");
 				put("Message.PlayerRevive", ChatColor.GREEN + "You have been revived.");
 				put("Message.PlayerDSpawnLeaveWarning", ChatColor.GOLD + "You cannot leave until a new round starts.");
 
 				put("Message.AlreadyIn", ChatColor.YELLOW + "You are already in.");
 				put("Message.Died", ChatColor.GRAY + "%s died.");
 				put("Broadcast.MapChosen", ChatColor.DARK_BLUE + "Level " + ChatColor.LIGHT_PURPLE + "%s" + ChatColor.DARK_BLUE + " has been chosen");
 				put("Broadcast.MapVoteStarting", ChatColor.DARK_AQUA + "Level vote starting,");
 				put("Broadcast.Type", ChatColor.DARK_AQUA + "Type ");
 				put("Broadcast.SlashVoteForMap", ChatColor.GOLD + "'/tribu vote %s'" + ChatColor.DARK_AQUA + " for map " + ChatColor.BLUE + "%s");
 				put("Broadcast.VoteClosingInSeconds", ChatColor.DARK_AQUA + "Vote closing in %s seconds");
 				put("Broadcast.StartingWave", ChatColor.GRAY + "Starting wave " + ChatColor.DARK_RED + "%s" + ChatColor.GRAY + ", "
 						+ ChatColor.DARK_RED + "%s" + ChatColor.GRAY + " Zombies @ " + ChatColor.DARK_RED + "%s" + ChatColor.GRAY + " health");
 				put("Broadcast.Wave", ChatColor.DARK_GRAY + "Wave " + ChatColor.DARK_RED + "%s" + ChatColor.DARK_GRAY + " starting in "
 						+ ChatColor.DARK_RED + "%s" + ChatColor.DARK_GRAY + " seconds.");
 				put("Broadcast.WaveComplete", ChatColor.GOLD + "Wave Complete");
 				put("Info.LevelFound", ChatColor.YELLOW + "%s levels found");
 				put("Info.Enable", ChatColor.WHITE + "Starting " + ChatColor.DARK_RED + "Tribu" + ChatColor.WHITE
 						+ " by Graindcafe, original author : samp20");
 				put("Info.Disable", ChatColor.YELLOW + "Stopping Tribu");
 				put("Info.LevelSaved", ChatColor.GREEN + "Level saved");
 				put("Info.ChosenLanguage", ChatColor.YELLOW + "Chosen language : %s (default). Provided by : %s.");
 				put("Info.LevelFolderDoesntExist", ChatColor.RED + "Level folder doesn't exist");
 				put("Warning.AllSpawnsCurrentlyUnloaded", ChatColor.YELLOW + "All zombies spawns are currently unloaded.");
 				put("Warning.UnableToSaveLevel", ChatColor.RED + "Unable to save level");
 				put("Warning.ThisCommandCannotBeUsedFromTheConsole", ChatColor.RED + "This command cannot be used from the console");
 				put("Warning.IOErrorOnFileDelete", ChatColor.RED + "IO error on file delete");
 				put("Warning.LanguageFileOutdated", ChatColor.RED + "Your current language file is outdated");
 				put("Warning.LanguageFileMissing", ChatColor.RED + "The chosen language file is missing");
 				put("Warning.UnableToAddSign", ChatColor.RED + "Unable to add sign, maybe you've changed your locales, or signs' tags.");
 				put("Warning.UnknownFocus",
 						ChatColor.RED
 								+ "The string given for the configuration Zombies.Focus is not recognized : %s . It could be 'None','Nearest','Random','DeathSpawn','InitialSpawn'.");
 				put("Warning.NoSpawns", ChatColor.RED + "You didn't set any zombie spawn.");
 				put("Severe.CannotCopyLanguages", ChatColor.RED + "Cannot copy languages files.");
 				put("Severe.TribuCantMkdir",
 						ChatColor.RED
 								+ "Tribu can't make dirs so it cannot create the level directory, you would not be able to save levels ! You can't use Tribu !");
 				put("Severe.WorldInvalidFileVersion", ChatColor.RED + "World invalid file version");
 				put("Severe.WorldDoesntExist", ChatColor.RED + "World doesn't exist");
 				put("Severe.ErrorDuringLevelLoading", ChatColor.RED + "Error during level loading : %s");
 				put("Severe.ErrorDuringLevelSaving", ChatColor.RED + "Error during level saving : %s");
 				put("Severe.PlayerHaveNotRetrivedHisItems", ChatColor.RED
 						+ "The player %s have not retrieved his items, they will be deleted ! Items list : %s");
 				put("Severe.Exception", ChatColor.RED + "Exception: %s");
 
 				put("Severe.PlayerDidntGetInvBack", ChatColor.RED
 						+ "didn't get his inventory back because he was returned null. (Maybe he was not in server?)");
 				put("Prefix.Broadcast", "[Tribu] ");
 				put("Prefix.Message", "");
 				put("Prefix.Info", "[Tribu] ");
 				put("Prefix.Warning", "[Tribu] ");
 				put("Prefix.Severe", "[Tribu] ");
 			}
 		});
 		if (config != null) {
 			language = Language.init(config.PluginModeLanguage);
 			if (language.isLoaded())
 				LogWarning(language.get("Warning.LanguageFileMissing"));
 			if (language.isOutdated())
 				LogWarning(language.get("Warning.LanguageOutdated"));
 			LogInfo(String.format(language.get("Info.ChosenLanguage"), language.getName(), language.getAuthor()));
 		} else
 			language = new DefaultLanguage();
 		language.setPrefix("Message.", language.get("Prefix.Message"));
 		language.setPrefix("Broadcast.", language.get("Prefix.Broadcast"));
 		language.setPrefix("Info.", language.get("Prefix.Info"));
 		language.setPrefix("Warning.", language.get("Prefix.Warning"));
 		language.setPrefix("Severe.", language.get("Prefix.Severe"));
 		Constants.MessageMoneyPoints = language.get("Message.MoneyPoints");
 		Constants.MessageZombieSpawnList = language.get("Message.ZombieSpawnList");
 	}
 
 	public boolean isAlive(Player player) {
 		return players.get(player).isalive();
 	}
 
 	/**
 	 * Check if Tribu is running and there is a level if it's server exclusive
 	 * or world exclusive & in the good world or if it's near the initial spawn
 	 * (radius "LevelClearZone")
 	 * 
 	 * @param loc
 	 *            Location to check
 	 * @return is inside level
 	 */
 	public boolean isInsideLevel(Location loc) {
 
 		return isInsideLevel(loc, false);
 	}
 
 	/**
 	 * Check if Tribu is running and there is a level if it's server exclusive
 	 * or world exclusive & in the good world or if it's near the initial spawn
 	 * (radius "LevelClearZone")
 	 * 
 	 * @param loc
 	 *            Location to check
 	 * @param dontCheckRunning
 	 *            Do not check if the plugin is running
 	 * @return is inside level
 	 */
 	public boolean isInsideLevel(Location loc, boolean dontCheckRunning) {
 
 		if ((dontCheckRunning || isRunning) && level != null)
			return config.PluginModeServerExclusive || 
					loc.getWorld().equals(level.getInitialSpawn().getWorld()) && 
					(config.PluginModeWorldExclusive|| loc.distance(level.getInitialSpawn()) < config.LevelClearZone);
 		else
 			return false;
 	}
 
 	/**
 	 * Is this player playing Tribu ?
 	 * 
 	 * @param player
 	 * @return Is this player playing Tribu ?
 	 */
 	public boolean isPlaying(Player p) {
 		return players.containsKey(p);
 	}
 
 	/**
 	 * Is the plugin running ? (not waiting for players but really running)
 	 * 
 	 * @return Is the plugin running ?
 	 */
 	public boolean isRunning() {
 		return isRunning;
 	}
 
 	public void keepTempInv(Player p, ItemStack[] items) {
 		// log.info("Keep " + items.length + " items for " +
 		// p.getDisplayName());
 		tempInventories.put(p, new TribuTempInventory(p, items));
 	}
 
 	/**
 	 * Load the custom config files, "per-world" and "per-level"
 	 */
 	protected void loadCustomConf() {
 		TribuLevel level = this.getLevel();
 		if (level == null)
 			return;
 		File worldFile = null, levelFile = null, worldDir, levelDir;
 		worldDir = new File(Constants.perWorldFolder);
 		levelDir = new File(Constants.perLevelFolder);
 		String levelName = level.getName() + ".yml";
 		String worldName = level.getInitialSpawn().getWorld().getName() + ".yml";
 		if (!levelDir.exists())
 			levelDir.mkdirs();
 		if (!worldDir.exists())
 			worldDir.mkdirs();
 
 		for (File file : levelDir.listFiles()) {
 			if (file.getName().equalsIgnoreCase(levelName)) {
 				levelFile = file;
 				break;
 			}
 		}
 		for (File file : worldDir.listFiles()) {
 			if (file.getName().equalsIgnoreCase(worldName)) {
 				worldFile = file;
 				break;
 			}
 		}
 		if (levelFile != null)
 			if (worldFile != null)
 				this.config = new TribuConfig(levelFile, new TribuConfig(worldFile));
 			else
 				this.config = new TribuConfig(levelFile);
 		else
 			this.config = new TribuConfig();
 		this.initLanguage();
 		if (config.PluginModeServerExclusive) {
 			for (Player p : this.getServer().getOnlinePlayers())
 				this.addPlayer(p);
 		}
 		if (config.PluginModeWorldExclusive && level != null) {
 			for (Player d : level.getInitialSpawn().getWorld().getPlayers()) {
 				this.addPlayer(d);
 			}
 		}
 		if (config.PluginModeAutoStart)
 			startRunning();
 	}
 
 	public void LogInfo(String message) {
 		log.info(message);
 	}
 
 	public void LogSevere(String message) {
 		log.severe(message);
 	}
 
 	public void LogWarning(String message) {
 		log.warning(message);
 
 	}
 
 	/**
 	 * Send a message after formating the given languageNode with given
 	 * arguments
 	 * 
 	 * @param sender
 	 *            The one to send a message
 	 * @param languageNode
 	 *            The language node to format
 	 * @param params
 	 *            The arguments to pass to the language node
 	 */
 	public void messagePlayer(CommandSender sender, String languageNode, Object... params) {
 		messagePlayer(sender, String.format(getLocale(languageNode), params));
 	}
 
 	/**
 	 * Broadcast message to playing players
 	 * 
 	 * @param msg
 	 */
 	public void messagePlayers(String msg) {
 		if (!msg.isEmpty())
 			for (Player p : players.keySet()) {
 				p.sendMessage(msg);
 			}
 	}
 
 	/**
 	 * Broadcast message to playing players after formating the given language
 	 * node
 	 * 
 	 * @param languageNode
 	 *            The language node to format
 	 * @param params
 	 *            The arguments to pass to the language node
 	 */
 	public void messagePlayers(String languageNode, Object... params) {
 		messagePlayers(String.format(getLocale(languageNode), params));
 	}
 
 	@Override
 	public void onDisable() {
 		inventorySave.restoreInventories();
 		players.clear();
 		sortedStats.clear();
 		memory.restoreAll();
 		stopRunning();
 		LogInfo(language.get("Info.Disable"));
 	}
 
 	@Override
 	public void onEnable() {
 		log = Logger.getLogger("Minecraft");
 		rnd = new Random();
 		boolean mkdirs = Constants.rebuildPath(getDataFolder().getPath() + File.separatorChar);
 		boolean langCopy = true;
 		for (String name : Constants.languages) {
 			InputStream fis = this.getClass().getResourceAsStream("/res/languages/" + name + ".yml");
 			FileOutputStream fos = null;
 			File f = new File(Constants.languagesFolder + name + ".yml");
 			if (!f.exists()) {
 				try {
 					fos = new FileOutputStream(f);
 					byte[] buf = new byte[1024];
 					int i = 0;
 					while ((i = fis.read(buf)) != -1) {
 						fos.write(buf, 0, i);
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 					langCopy = false;
 				} finally {
 					try {
 						if (fis != null) {
 							fis.close();
 						}
 						if (fos != null) {
 							fos.close();
 						}
 					} catch (Exception e) {
 					}
 				}
 			}
 		}
 		try {
 			@SuppressWarnings("rawtypes")
 			Class[] args = { Class.class, String.class, Integer.TYPE, Integer.TYPE, Integer.TYPE };
 			Method a = EntityTypes.class.getDeclaredMethod("a", args);
 			a.setAccessible(true);
 
 			a.invoke(a, EntityTribuZombie.class, "Zombie", 54, '\uafaf', 7969893);
 			a.invoke(a, EntityZombie.class, "Zombie", 54, '\uafaf', 7969893);
 
 		} catch (Exception e) {
 			setEnabled(false);
 			e.printStackTrace();
 			return;
 		}
 		// isRunning set to true to prevent start running at "loadCustomConf"
 		isRunning = true;
 		aliveCount = 0;
 		level = null;
 		// A language should be loaded BEFORE levelLoader uses
 		initLanguage();
 		levelLoader = new LevelFileLoader(this);
 		// The level loader have to be ready
 		this.reloadConf();
 		isRunning = false;
 		tempInventories = new HashMap<Player, TribuTempInventory>();
 		inventorySave = new TribuInventory();
 		players = new HashMap<Player, PlayerStats>();
 		spawnPoint = new HashMap<Player, Location>();
 		sortedStats = new LinkedList<PlayerStats>();
 
 		levelSelector = new LevelSelector(this);
 
 		spawner = new TribuSpawner(this);
 		spawnTimer = new SpawnTimer(this);
 		waveStarter = new WaveStarter(this);
 
 		// Create listeners
 		playerListener = new TribuPlayerListener(this);
 		entityListener = new TribuEntityListener(this);
 		blockListener = new TribuBlockListener(this);
 		worldListener = new TribuWorldListener(this);
 
 		memory = new ChunkMemory();
 
 		getServer().getPluginManager().registerEvents(playerListener, this);
 		getServer().getPluginManager().registerEvents(entityListener, this);
 		getServer().getPluginManager().registerEvents(blockListener, this);
 		getServer().getPluginManager().registerEvents(worldListener, this);
 
 		getCommand("dspawn").setExecutor(new CmdDspawn(this));
 		getCommand("zspawn").setExecutor(new CmdZspawn(this));
 		getCommand("ispawn").setExecutor(new CmdIspawn(this));
 		getCommand("tribu").setExecutor(new CmdTribu(this));
 		if (!mkdirs)
 			LogSevere(getLocale("Severe.TribuCantMkdir"));
 		if (!langCopy)
 			LogSevere(getLocale("Severe.CannotCopyLanguages"));
 		LogInfo(language.get("Info.Enable"));
 		if (config.PluginModeAutoStart)
 			startRunning();
 	}
 
 	public void reloadConf() {
 		// Reload the main config file from disk
 		this.reloadConfig();
 		// Parse again the file
 		this.config = new TribuConfig(getConfig());
 		// Create the file if it doesn't exist
 		try {
 			getConfig().save(Constants.configFile);
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		// Before "loadCustom"
 		if (config.PluginModeDefaultLevel != "")
 			setLevel(levelLoader.loadLevel(config.PluginModeDefaultLevel));
 		// After loading the level from main file
 		else
 			this.loadCustomConf();
 	}
 
 	/**
 	 * Remove a player from the game
 	 * 
 	 * @param player
 	 */
 	public void removePlayer(Player player) {
 		if (player != null && players.containsKey(player)) {
 			if (isAlive(player)) {
 				aliveCount--;
 			}
 			sortedStats.remove(players.get(player));
 			inventorySave.restoreInventory(player);
 			players.remove(player);
 			Tribu.messagePlayer(player, getLocale("Message.YouLeft"));
 			if (player.isOnline() && spawnPoint.containsKey(player)) {
 				player.setBedSpawnLocation(spawnPoint.remove(player));
 			}
 			// check alive AFTER player remove
 			checkAliveCount();
 			if (!player.isDead())
 				restoreInventory(player);
 
 		}
 	}
 
 	/**
 	 * Set that the player spawn has been reseted and should be set it back when
 	 * reviving
 	 * 
 	 * @param p
 	 *            The player
 	 * @param point
 	 *            The previous spawn
 	 */
 	public void resetedSpawnAdd(Player p, Location point) {
 		spawnPoint.put(p, point);
 	}
 
 	public void restoreInventory(Player p) {
 		// log.info("Restore items for " + p.getDisplayName());
 		inventorySave.restoreInventory(p);
 	}
 
 	public void restoreTempInv(Player p) {
 		// log.info("Restore items for " + p.getDisplayName());
 		if (tempInventories.containsKey(p))
 			tempInventories.remove(p).restore();
 	}
 
 	/**
 	 * Revive a player
 	 * 
 	 * @param player
 	 */
 	public void revivePlayer(Player player) {
 		if (spawnPoint.containsKey(player)) {
 			player.setBedSpawnLocation(spawnPoint.remove(player));
 		}
 		players.get(player).revive();
 		if (config.WaveStartHealPlayers)
 			player.setHealth(20);
 		restoreTempInv(player);
 		aliveCount++;
 
 	}
 
 	/**
 	 * Revive all players
 	 * 
 	 * @param teleportAll
 	 *            Teleport everyone or just dead people
 	 */
 	public void revivePlayers(boolean teleportAll) {
 		aliveCount = 0;
 		for (Player player : players.keySet()) {
 			revivePlayer(player);
 			if (isRunning && level != null && (teleportAll || !isAlive(player))) {
 				player.teleport(level.getInitialSpawn());
 			}
 		}
 	}
 
 	/**
 	 * Mark a player as dead and do all necessary stuff
 	 * 
 	 * @param player
 	 */
 	public void setDead(Player player) {
 		if (players.containsKey(player)) {
 			if (isAlive(player)) {
 				aliveCount--;
 				PlayerStats p = players.get(player);
 				p.resetMoney();
 				p.subtractmoney(config.StatsOnPlayerDeathMoney);
 				p.subtractPoints(config.StatsOnPlayerDeathPoints);
 				p.msgStats();
 				messagePlayers("Message.Died", player.getName());
 			}
 			players.get(player).kill();
 			if (getLevel() != null && isRunning) {
 				checkAliveCount();
 			}
 		}
 	}
 
 	/**
 	 * Set the current level
 	 * 
 	 * @param level
 	 */
 	public void setLevel(TribuLevel level) {
 		this.level = level;
 		this.loadCustomConf();
 	}
 
 	/**
 	 * Start a new game
 	 * 
 	 * @return if the game can start
 	 */
 	public boolean startRunning() {
 		if (!isRunning && getLevel() != null) {
 			if (players.isEmpty()) {
 				waitingPlayers = config.WaveStartMinPlayers;
 			} else {
 				// Before (next instruction) it will saves current default
 				// packages to the level, saving theses packages with the level
 				this.addDefaultPackages();
 				// Make sure no data is lost if server decides to die
 				// during a game and player forgot to /level save
 				if (!getLevelLoader().saveLevel(getLevel())) {
 					LogWarning(language.get("Warning.UnableToSaveLevel"));
 				} else {
 					LogInfo(language.get("Info.LevelSaved"));
 				}
 				if (this.getLevel().getSpawns().isEmpty()) {
 					LogWarning(language.get("Warning.NoSpawns"));
 					return false;
 				}
 
 				isRunning = true;
 				if (config.PluginModeServerExclusive || config.PluginModeWorldExclusive)
 					for (LivingEntity e : level.getInitialSpawn().getWorld().getLivingEntities()) {
 						if (!(e instanceof Player) && !(e instanceof Wolf) && !(e instanceof Villager))
 							e.damage(Integer.MAX_VALUE);
 					}
 				else
 					for (LivingEntity e : level.getInitialSpawn().getWorld().getLivingEntities()) {
 						if ((e.getLocation().distance(level.getInitialSpawn())) < config.LevelClearZone && !(e instanceof Player)
 								&& !(e instanceof Wolf) && !(e instanceof Villager))
 							e.damage(Integer.MAX_VALUE);
 					}
 				if (config.PlayersRollback) {
 					// If there is a restoring operation currently, do it
 					// quickly
 					memory.getReady();
 					memory.startCapturing();
 					// Pre-cache level
 					memory.add(level.getInitialSpawn().getChunk());
 					memory.add(level.getDeathSpawn().getChunk());
 					for (Location l : level.getZombieSpawns())
 						memory.add(l.getChunk());
 				}
 				getLevel().initSigns();
 				this.sortedStats.clear();
 
 				for (PlayerStats stat : players.values()) {
 					stat.resetPoints();
 					stat.resetMoney();
 					this.sortedStats.add(stat);
 				}
 				getWaveStarter().resetWave();
 				revivePlayers(true);
 				getWaveStarter().scheduleWave(Constants.TicksBySecond * config.WaveStartDelay);
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Start the game in n seconds
 	 * 
 	 * @param timeout
 	 *            Delay in seconds
 	 */
 	public void startRunning(final double timeout) {
 		this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 			public void run() {
 				startRunning();
 			}
 		}, Math.round(Constants.TicksBySecond * timeout));
 	}
 
 	/**
 	 * End the game
 	 */
 	public void stopRunning() {
 		stopRunning(false);
 	}
 
 	public void stopRunning(boolean rerun) {
 		getLevelSelector().cancelVote();
 		if (isRunning) {
 			isRunning = false;
 			getSpawnTimer().Stop();
 			getWaveStarter().cancelWave();
 			getSpawner().clearZombies();
 
 			if (config.PlayersRollback)
 				memory.startRestoring(this, config.AdvancedRestoringSpeed);
 			this.level.finishSigns();
 			// Teleports all players to spawn when game ends
 			for (Player p : players.keySet()) {
 				p.teleport(level.getInitialSpawn());
 			}
 			if (!rerun) {
 				if (config.PlayersStoreInventory)
 					inventorySave.restoreInventories();
 
 				if (!config.PluginModeServerExclusive && !config.PluginModeWorldExclusive) {
 					players.clear();
 				}
 			}
 		}
 
 	}
 }
