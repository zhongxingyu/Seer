 package qs.hungergames;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.World;
 import org.bukkit.WorldCreator;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.Vector;
 
 public class Main extends JavaPlugin implements Listener {
 
 	public static final String WINNERS_FILE = "winners.txt";
 	
 	protected Random rand = new Random();
 	
 	public boolean isAutomated = true;
 
 	private Countdown lobbyTimer;
 	private Countdown countdown;
 
 	public boolean gameStarted = false;
 	public boolean inSafePeriod = false;
 	
 	public double arenaSize;
 	public double arenaSizeSquared;
 
 	public World gameWorld;
 	public Location gameSpawn;
 
 	public Set<OfflinePlayer> alive = new HashSet<OfflinePlayer>();
 	public Set<OfflinePlayer> bypass = new HashSet<OfflinePlayer>();
 	public Map<OfflinePlayer, Integer> loggedOut = new HashMap<OfflinePlayer, Integer>();
 
	public Map<Player, Integer> trackUses = new HashMap<Player, Integer>(); //static for access in Kit.java
	public Map<Player, Kit> kits = new HashMap<Player, Kit>();
 	
 	private Logger log;
 	
 	@Override
 	public void onEnable() { 
 		getServer().getPluginManager().registerEvents(this, this);
 		log = this.getLogger();
 		alive.clear();
 		
 		final File dataFolder = this.getDataFolder();
 		if (!dataFolder.exists()) dataFolder.mkdir();
 		
 		File winners = new File(dataFolder, WINNERS_FILE);
 		try {
 			winners.createNewFile();
 		} catch (IOException e) { e.printStackTrace(); }
 		
 		isAutomated = getConfig().getBoolean("automated", false);
 		
 		if (isAutomated) {
 			startLobbyTimer();
 			log.info("Automation is enabled.");
 		} else {
 			log.info("Automation is disabled. Use /hg start to start the game.");
 		}
 		
 		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 			@Override
 			public void run() {
 				try {
 					FileInputStream fstream = new FileInputStream(new File(dataFolder, WINNERS_FILE));
 					BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
 					String strLine = br.readLine();
 					if (strLine != null)
 						getServer().broadcastMessage(ChatColor.AQUA + "Congratulations to our last champion, " + strLine);
 				} catch (FileNotFoundException e) {
 					log.warning("Internal exception fetching winner:");
 					e.printStackTrace();
 				} catch (IOException e) {
 					log.warning("Internal exception fetching winner:");
 					e.printStackTrace();
 				}
 			}
 		}, 1200L, 6000L);
 		
 		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 			@Override
 			public void run() {
 				if (!gameStarted) {
 					getServer().broadcastMessage(ChatColor.AQUA + "Use /hg kit <kit name> to select a kit");
 				}
 			}
 		}, 2100L, 3000L);
 		
 		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 			@Override
 			public void run() {
 				if (!gameStarted) {
 					getServer().broadcastMessage(ChatColor.RED + "HACKING WILL GET YOU PERMA-BANNED, NO EXCEPTIONS, NO REPEALS, ETC");
 				}
 			}
 		}, 3000L, 3000L);
 	}
 	
 	@Override
 	public void onDisable() { }
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if (command.getName().equals("hungergames")) {
 			boolean isPlayer = sender instanceof Player;
 			boolean isAdmin = sender.hasPermission("hungergames.admin");
 			
 			if (args.length < 1 || args[0].equalsIgnoreCase("help")) {
 				sender.sendMessage(ChatColor.DARK_RED + "-------------------" + ChatColor.GOLD + "[Hunger Games]" + ChatColor.DARK_RED + "--------------------");
 				sender.sendMessage(ChatColor.DARK_RED + "/hg is an alias for /hungergames");
 				if (isPlayer)
 					sender.sendMessage(ChatColor.DARK_RED + "/hg track - tracks a player's coords in final five");
 					sender.sendMessage(ChatColor.DARK_RED + "/hg kit <kit> - sets the items you recieve upon spawining");
 					sender.sendMessage(ChatColor.DARK_RED + "/hg info <kit> - shows the description of a kit");
 					sender.sendMessage(ChatColor.DARK_RED + "/hg list - lists the names of all kits");
 				sender.sendMessage(ChatColor.DARK_RED + "/hg lobby - shows the time until the game starts");
 				if (isAdmin) {
 					sender.sendMessage(ChatColor.DARK_RED + "/hg start - starts the game");
 					sender.sendMessage(ChatColor.DARK_RED + "/hg end - forces the game to end with no winner");
 					if (isPlayer)
 						sender.sendMessage(ChatColor.DARK_RED + "/hg bypass - removes you from games, but keeps you online");
 				}
 				return true;
 			}
 
 			String action = args[0].toLowerCase();
 
 			if (isAdmin) {
 				if (action.equals("start") || action.equals("startgame")) {
 					startGame(false);
 					return true;
 				}
 				if (action.equals("end") || action.equals("forceend")) {
 					endGame(null);
 					return true;
 				}
 				if (action.equals("forcestart")) {
 					startGame(true);
 					return true;
 				}
 			}
 			if ((action.equalsIgnoreCase("lobby") || action.equals("lobbytimer")) && 
 					!gameStarted && !inSafePeriod) {
 				if (lobbyTimer == null) {
 					sendError(sender, "There is no lobby");
 					return true;
 				}
 				int time = lobbyTimer.time;
 				sender.sendMessage(ChatColor.GOLD + (
 						time > 60 ? String.format("%d minutes remaining", time / 60) :
 						String.format("%d seconds remaining", time)));
 				return true;
 			}
 			if (isPlayer) {
 				Player pl = (Player) sender;
 				if (action.equals("bypass") && isAdmin) {
 					if (!gameStarted) {
 						if (!bypass.contains(pl)) {
 							bypass.add((Player) sender);
 							sender.sendMessage(ChatColor.AQUA + "bypass enabled");
 							return true;
 						}
 						bypass.remove(pl);
 						sender.sendMessage(ChatColor.AQUA + "bypass disabled");
 						return true;
 					}
 					sendError(pl, "You may not leave, or enter bypass mode mid game!");
 					return true;
 				}
 				if (gameStarted && action.equals("track") && (alive.size() <= 5 || kits.get(pl) == Kit.TRACKER)
 						&& trackUses.get(pl) > 0 && alive.size() > 1){
 					double count = Double.MAX_VALUE;
 					Player trackee = null;
 					for (Player player: getServer().getOnlinePlayers()){
 						if (bypass.contains(player) || pl == player) continue;
 						double distance = player.getLocation().distanceSquared(pl.getLocation());
 						if (distance < count) {
 							trackee = player;
 							count = distance;
 						}
 					}
 					trackUses.put(pl, (trackUses.get(pl) - 1));
 					pl.sendMessage(ChatColor.BLUE + "You use your tracking skills to find the nearest player! (" +
 							trackUses.get(pl) + " uses left)");
 					pl.getPlayer().sendMessage("X: " + trackee.getLocation().getX() + " Y: " + trackee.getLocation().getY() +
 							" Z: " + trackee.getLocation().getZ());
 					return true;
 				}
 				if (action.equals("kit") && !gameStarted && !inSafePeriod) {
 					if(args.length < 2) {
 						sendError(pl, "Invalid arguments");
 						return true;
 					}
 					Kit kit = Kit.getKitFromName(args[1]);
 					if (kit == null) {
 						sendError(pl, "That is not a valid kit! Use /hg list for a list");
 						return true;
 					}
 					kits.put(pl, kit);
 					pl.sendMessage(ChatColor.GOLD + "You have selected: " + kit.getName() + " as your starting kit");
 					return true;
 				}
 				if (action.equals("info")) {
 					if (args.length < 2) {
 						sendError(pl, "Invalid arguments");
 						return true;
 					}
 					Kit kit = Kit.getKitFromName(args[1]);
 					if (kit == null) {
 						sendError(pl, "That is not a valid kit! use /hg list for a list of kits");
 						return true;
 					}
 					pl.sendMessage(ChatColor.GOLD + kit.getName() + ": " + kit.description);
 					return true;
 				}
 				if (action.equals("list")) {
 					pl.sendMessage(ChatColor.DARK_RED + "Use /hg info <kit-name> for more info on each kit");
 					pl.sendMessage(ChatColor.GOLD + Kit.FIGHTER.getName());
 					pl.sendMessage(ChatColor.GOLD + Kit.HUNTER.getName());
 					pl.sendMessage(ChatColor.GOLD + Kit.SNIPER.getName());
 					pl.sendMessage(ChatColor.GOLD + Kit.ELECTRICIAN.getName());
 					pl.sendMessage(ChatColor.GOLD + Kit.PYROMANIAC.getName());
 					pl.sendMessage(ChatColor.GOLD + Kit.PIRATEMANIAC.getName());
 					pl.sendMessage(ChatColor.GOLD + Kit.SURVIVOR.getName());
 					pl.sendMessage(ChatColor.GOLD + Kit.BEASTMASTER.getName());
 					pl.sendMessage(ChatColor.GOLD + Kit.GATHERER.getName());
 					
 					return true;
 				}
 			} else {
 				if (action.equals("automated")) {
 					if (isAutomated) {
 						this.getConfig().set("automated", isAutomated = false);
 						this.saveConfig();
 						sender.sendMessage(ChatColor.GOLD + "The game is no longer automated");
 						if (lobbyTimer != null) lobbyTimer.stop();
 						return true;
 					}
 					this.getConfig().set("automated", isAutomated = true);
 					this.saveConfig();
 					sender.sendMessage(ChatColor.GOLD + "The game is now automated");
 					startLobbyTimer();
 					return true;
 				}
 				if (action.equals("setbypass")) {
 					if (args.length < 3) {
 						sender.sendMessage(ChatColor.RED + "Invalid arguments");
 						return true;
 					}
 					OfflinePlayer player = getServer().getOfflinePlayer(args[1]);
 					if (Boolean.valueOf(args[2])) {
 						alive.remove(player);
 						if (bypass.add(player) && player.isOnline())
 							((CommandSender) player).sendMessage("You were put into bypass mode");
 						if (!player.equals(sender))
 							sender.sendMessage(ChatColor.AQUA + "Player " + player.getName() + " was put into bypass mode");
 						return true;
 					}
 					if (bypass.remove(player) && player.isOnline())
 						((CommandSender) player).sendMessage("You were removed from bypass mode");
 					if (!player.equals(sender))
 						sender.sendMessage(ChatColor.AQUA + "Player " + player.getName() + " was removed from bypass mode");
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Starts a game.
 	 * @param forced whether the start was forced or effected by a lobby timeout
 	 * @return a boolean indicating success
 	 */
 	public boolean startGame(boolean forced){
 		if (gameStarted) return false;
 		if (lobbyTimer != null) lobbyTimer.stop();
 
 		gameStarted = true;
 		
 		WorldCreator wc = new WorldCreator("HungerGames");
 		wc.seed(rand.nextLong());
 		gameWorld = wc.createWorld();
 		gameSpawn = gameWorld.getSpawnLocation();
 				
 		alive.clear();
 		Player[] players = getServer().getOnlinePlayers();
 		final int playerCount = players.length;
 		final double
 			distance = 3.0,
 			angleStep = Math.PI * 2.0 / playerCount,
 			radius = distance * playerCount / (2.0 * Math.PI);
 		double angle = 0.0;
 		for (Player pl : players) {
 			if (isAutomated || pl.hasPermission("hungergames.canplay") || bypass.contains(pl)) {
 				pl.teleport(gameSpawn.clone().add(radius * Math.cos(angle), 0, radius * Math.sin(angle)));
 				angle += angleStep;
 				
 				PlayerInventory inv = pl.getInventory();
 				inv.clear();
 				inv.setArmorContents(Kit.emptyArmor);
 				pl.setHealth(20);
 				pl.setFoodLevel(20);
 				Kit kit = kits.get(pl);
 				if (kit != null) {
 					kit.applyKit(pl, this);
 				} else {
 					Kit.defaultKit.applyKit(pl, this);
 					kits.put(pl, Kit.defaultKit);
 				}
 				if (!bypass.contains(pl))
 					alive.add(pl);
 			} else {
 				pl.kickPlayer("You must pay $4 to participate in the hungergames tournament!");
 			}
 		}
 		
 		final int safeTime = 120 + playerCount * 9 / 4;
 		arenaSize = 300 + playerCount * 45 / 7;
 		arenaSizeSquared = arenaSize * arenaSize;
 		
 		if (forced) {
 			getServer().broadcastMessage(ChatColor.DARK_RED + "LET THE GAMES BEGIN!");
 			gameStarted = true;
 			inSafePeriod = false;
 		} else {
 			this.getServer().broadcastMessage(ChatColor.BLUE + "Welcome to the arena! You have a brief period of invincibility until the killing begins!");
 			inSafePeriod = true;
 
 			if (countdown != null) countdown.stop();
 			countdown = new Countdown(this, safeTime) {
 				@Override
 				public void sendTimeAlert(int time) {
 					getServer().broadcastMessage(ChatColor.GOLD + (
 						time > 60 ? String.format("%d minutes remaining", time / 60) :
 						time == 60 ? "One minute remaining" :
 						time == 30 ? "30 seconds remaining" :
 							Integer.toString(time)));
 				}
 
 				@Override
 				public void start() {
 					getServer().broadcastMessage(ChatColor.DARK_RED + "LET THE GAMES BEGIN!");
 					gameStarted = true;
 					inSafePeriod = false;
 					stop();
 				}
 			};
 		}
 		return true;
 	}
 	
 	/**
 	 * Ends a game.
 	 * @param winner the winner of the game (can be null)
 	 * @return a boolean indicating success
 	 */
 	public boolean endGame(Player winner) {
 		if (!gameStarted) return false;
 
 		if (countdown != null) countdown.stop();
 		World w = getServer().getWorld("world");
 		gameStarted = false;
 		for (Player pl : getServer().getOnlinePlayers()) {
 			pl.teleport(w.getSpawnLocation());
 			pl.getInventory().clear();
 			pl.getInventory().setArmorContents(Kit.emptyArmor);
 		}
 		if (winner == null) {
 			getServer().broadcastMessage("The game was stopped!");
 		} else {
 			getServer().broadcastMessage(ChatColor.AQUA + winner.getName() + " has won! Congradulations!");
 			getServer().broadcastMessage(ChatColor.AQUA + "celebratory music will play!");
 			w.playEffect(w.getSpawnLocation(), Effect.RECORD_PLAY, 2258);
 			
 			ArrayList<String> original = new ArrayList<String>();
 			try {
 				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(getDataFolder(), WINNERS_FILE))));
 				String line;
 				while ((line = br.readLine()) != null) original.add(line);
 				br.close();
 			} catch (IOException e) {
 				log.warning("Failed to update winners.txt:");
 				e.printStackTrace();
 				return false;
 			}
 			
 			PrintWriter out;
 			try {
 				out = new PrintWriter(new FileWriter(new File(getDataFolder(), WINNERS_FILE)));
 				
 				out.println(winner.getName());
 				for (String line : original) out.println(line);
 				out.close();
 			} catch (IOException e) {
 				log.warning("Failed to update winners.txt:");
 				e.printStackTrace();
 				return false;
 			}
 		}
 		
 		alive.clear();
 		
 		File file = gameWorld.getWorldFolder();
 		if (gameWorld.equals(getServer().getWorlds().get(0))){
 			log.info("the Hungergames world is the main world!");
 		}
 		if (!getServer().unloadWorld(gameWorld, false) ||
 				!deleteDir(file)) {
 			log.warning("Failed to delete the Hunger Games world");
 			
 			File f = new File("deleteworld.bat");
 			if (f.exists()) {
 				log.info("Found a batch file to restart the server with a new world.");
 				log.info("The server will be shut down and this file will be run.");
 				try {
 					Runtime.getRuntime().exec("cmd /c start deleteworld.bat");
 					getServer().shutdown();
 				} catch (IOException e) {
 					log.warning("Execution of 'deleteworld.bat' failed:");
 					e.printStackTrace();
 				}
 				return false;
 			}
 			log.info("No batch file was found to reload the world.");
 			log.info("You can download or write 'deleteworld.bat' to perform this operation.");
 			return false;
 		}
 		if (isAutomated) startLobbyTimer();
 		return true;
 	}
 	
 	/**
 	 * Sends an error to a command sender.
 	 * @param the command sender
 	 * @param message the error message
 	 */
 	public void sendError(CommandSender sender, String message){
 		sender.sendMessage(ChatColor.RED + message);
 	}
 	
 	/**
 	 * Recursively deletes a directory.
 	 * @param dir the directory.
 	 * @return a boolean indicating success
 	 */
 	public static boolean deleteDir(File dir) {
 		if (dir.isDirectory())
 			for (File f : dir.listFiles())
 				if (!deleteDir(f))
 					return false;
 		return dir.delete();
 	}
 	
 	/**
 	 * Starts the timer for automatically starting the game.
 	 */
 	public void startLobbyTimer() {
 		lobbyTimer = new Countdown(this, getConfig().getInt("lobbyTime")) {
 			@Override
 			public void sendTimeAlert(int time) {
 				getServer().broadcastMessage(ChatColor.GOLD + (
 					time > 60 ? String.format("%d minutes until game start", time / 60) :
 					time == 60 ? "One minute remaining!" :
 						String.format("%d seconds remaining!", time)));
 			}
 
 			@Override
 			public void start() {
 				startGame(false);
 			}
 		};
 	}
 
 
 	@EventHandler
 	public void onBlockClick(PlayerInteractEvent event) {
 		if (!gameStarted && !inSafePeriod && !(event.getPlayer().hasPermission("hungergames.admin"))) {
 			sendError(event.getPlayer(), "You may not modify blocks until the game begins");
 			event.setCancelled(true);
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST)
 	public void onPreProcessCommand(PlayerCommandPreprocessEvent event){
 		if ((!event.getMessage().startsWith("/hg")) &&
 				(!event.getMessage().startsWith("/hungergames")) &&
 				(!event.getPlayer().hasPermission("hungergames.admin"))){
 			event.setCancelled(true);
 			event.getPlayer().sendMessage(ChatColor.RED + "You may only use Hunger Games commands");
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerDamage(EntityDamageEvent event){
 		if (!gameStarted || inSafePeriod || event.getEntity() instanceof Player && 
 				bypass.contains((Player) event.getEntity()))
 				event.setCancelled(true);
 	}
 	
 	@EventHandler
 	public void onPlayerDeath(PlayerDeathEvent event){
 		Player player = event.getEntity();
 		if (gameStarted == true && !bypass.contains(player)) {
 			alive.remove(player);
 			log.info(player.getName() + " was killed");
 			event.getEntity().kickPlayer("You were killed");
 			
 			for (Player pl: getServer().getOnlinePlayers()) {
 				Location loc = pl.getLocation();
 				loc.setY(loc.getY() + 15);
 				gameWorld.createExplosion(loc, 0F);
 			}
 			if (alive.size() == 1) {
 				endGame((Player) alive.toArray()[0]);
 				return;
 			}
 			if (alive.size() == 0) {
 				endGame(null);
 				return;
 			}
 			if (alive.size() == 5) {
 				getServer().broadcastMessage("There are five players left! You can use /hg track to hunt down another player.");
 				return;
 			}
 			getServer().broadcastMessage(String.format("A tribute has died. There are %d tributes remaining.", alive.size()));
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		Player pl = event.getPlayer();
 		if (!gameStarted) {
 			event.getPlayer().getInventory().clear();
 			return;
 		}
 		Integer i = loggedOut.remove(event.getPlayer());
 		if (i != null)
 			getServer().getScheduler().cancelTask(loggedOut.get(event.getPlayer()));
 		
 		if (!alive.contains(pl) && !bypass.contains(pl))
 			pl.kickPlayer("You must wait until the next round to join");
 	}
 	@EventHandler
 	public void onTntExplode(EntityExplodeEvent event) {
 		if (!gameStarted)
 			event.setCancelled(true);
 	}
 	
 	@EventHandler
 	public void onPlayerMove (PlayerMoveEvent event){
 		Player pl = event.getPlayer();
 		if (!gameStarted || pl.getWorld() != gameWorld) return;
 		
 		Location loc = event.getTo();
 		double distance = gameSpawn.distanceSquared(loc);
 		if (distance <= arenaSizeSquared) return;
 		
 		final double base = 2.0,
 			power = 0.6;
 		Location diff = gameSpawn.clone().subtract(loc);
 		Vector force = diff.subtract(diff.toVector().normalize().multiply(arenaSize - base)).multiply(power).toVector();
 		pl.damage(4);
 		pl.setFireTicks(2);
 		pl.setVelocity(pl.getVelocity().add(force));
 		sendError(pl, "You may not leave the arena!");
 	}
 	
 	@EventHandler
 	public void onPlayerLeave(PlayerQuitEvent event) {
 		if(gameStarted || inSafePeriod) {
 			final Player player = event.getPlayer();
 			loggedOut.put(event.getPlayer(), getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 				@Override
 				public void run() {
 					if(loggedOut.containsKey(player)) {
 						alive.remove(player);
 						loggedOut.remove(player);
 						alive.remove(player);
 						getServer().broadcastMessage(player.getName() + " logged out for too long and was removed!");
 						
 						for (Player pl: getServer().getOnlinePlayers()) {
 							Location loc = pl.getLocation();
 							loc.setY(loc.getY() + 15);
 							gameWorld.createExplosion(loc, 0F);
 						}
 						if (alive.size() == 1) {
 							endGame((Player) alive.toArray()[0]);
 							return;
 						}
 						if (alive.size() == 0) {
 							endGame(null);
 							return;
 						}
 						getServer().broadcastMessage(String.format("A tribute has died. There are %d tributes remaining.", alive.size()));
 						if (alive.size() == 5) {
 							getServer().broadcastMessage("There are five players left! You can use /hg track to hunt down another player.");
 							return;
 						}
 					}
 				}
 			}, 120L));
 		}
 	}
 }
