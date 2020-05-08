 package net.evtr.bombersteve;
 
 import java.io.File;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Logger;
 
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.Vector;
 
 import com.evanreidland.e.Settings;
 
 public class BomberSteve extends JavaPlugin {
 	
 	private EntityListener entityListener;
 	private BlockListener blockListener;
 	
 	private Timer logicTimer;
 	
 	public Logger log = Logger.getLogger("Minecraft");
 	
 	public java.util.Vector<BomberGame> games;
 	public java.util.Vector<BomberPlayer> players;
 	
 	public int sizeX = 16,
 			   sizeY = 6,
 			   sizeZ = 16,
 			   density = 20,
 			   hDensity = 5,
 			   columnIncrement = 4,
 			   selectedGame = 0, 
 			   readyMargin = 60,
 			   defaultNPCs = 2,
 			   sizePer = 8,
 			   powerRarity = 5;
 	
 	public void loadData() {
 		File dataFolder = getDataFolder();
 		if ( !dataFolder.exists() ) {
 			dataFolder.mkdir();
 			return;
 		}
 		
 		Settings settings = new Settings();
 		settings.readFromFile(getDataFolder() + File.separator + "settings.txt");
 		
 		sizeX = settings.getSetting("sizeX").asInt(16);
 		sizeY = settings.getSetting("sizeY").asInt(8);
 		sizeZ = settings.getSetting("sizeZ").asInt(16);
 		density = settings.getSetting("density").asInt(20);
 		hDensity = settings.getSetting("hdensity").asInt(5);
 		columnIncrement = settings.getSetting("increment").asInt(4);
 		readyMargin = settings.getSetting("readyMargin").asInt(60);
 		defaultNPCs = settings.getSetting("defaultNPCs").asInt(2);
 		sizePer = settings.getSetting("sizePerPlayer").asInt(8);
 		defaultAutoScale = settings.getSetting("autoScale").asBool(false);
 		powerRarity = settings.getSetting("powerRarity").asInt(5);
 		
 		
 		settings = new Settings();
 		settings.readFromFile(getDataFolder() + File.separator + "regions.txt");
 		
 		int i = 1;
 		String prefix = "g" + i + ".";
 		while ( settings.getSetting(prefix + "exists").asBool(false) ) {
 			int x = settings.getSetting(prefix + "x").asInt();
 			int y = settings.getSetting(prefix + "y").asInt();
 			int z = settings.getSetting(prefix + "z").asInt();
 			int sx = settings.getSetting(prefix + "sx").asInt(sizeX);
 			int sy = settings.getSetting(prefix + "sy").asInt(sizeY);
 			int sz = settings.getSetting(prefix + "sz").asInt(sizeZ);
 			
 			BomberGame game = new BomberGame(this, newID(), new Vector(x, y, z), new Vector(sx, sy, sz));
 			
 			game.hardSpacing = settings.getSetting(prefix + "increment").asInt(columnIncrement);
 			game.softDensity = settings.getSetting(prefix + "density").asInt(density);
 			game.hardDensity = settings.getSetting(prefix + "hdensity").asInt(hDensity);
 			game.maxNPCs = settings.getSetting(prefix + "npcs").asInt(defaultNPCs);
 			game.sizePerPlayer = settings.getSetting(prefix + "sizePer").asInt(sizePer);
 			game.autoScale = settings.getSetting(prefix + "autoScale").asBool(defaultAutoScale);
 			game.powerRarity = settings.getSetting(prefix + "powerRarity").asInt(powerRarity);
 			
 			games.add(game);
 			
 			prefix = "g" + (++i) + ".";
 		}
 			
 		
 		
 	}
 	
 	public void saveData() {
 		File dataFolder = getDataFolder();
 		if ( !dataFolder.exists() ) {
 			dataFolder.mkdir();
 		}
 		
 		Settings settings = new Settings();
 		settings.addSetting("sizeX", sizeX);
 		settings.addSetting("sizeY", sizeY);
 		settings.addSetting("sizeZ", sizeZ);
 		settings.addSetting("density", density);
 		settings.addSetting("hdensity", hDensity);
 		settings.addSetting("increment", columnIncrement);
 		settings.addSetting("readyMargin", readyMargin);
 		settings.addSetting("defaultNPCs", defaultNPCs);
 		settings.addSetting("sizePerPlayer", sizePer);
 		settings.addSetting("autoScale", defaultAutoScale);
 		
 		settings.writeToFile(getDataFolder() + File.separator + "settings.txt");
 		
 		settings = new Settings();
 		for ( int i = 0; i < games.size(); i++ ) {
 			String prefix = "g" + (i + 1) + ".";
 			settings.addSetting(prefix + "exists", true);
 			
 			BomberGame game = games.get(i);
 			settings.addSetting(prefix + "x", game.getBottomLeft().getBlockX());
 			settings.addSetting(prefix + "y", game.getBottomLeft().getBlockY());
 			settings.addSetting(prefix + "z", game.getBottomLeft().getBlockZ());
 			settings.addSetting(prefix + "sx", game.getSize().getBlockX());
 			settings.addSetting(prefix + "sy", game.getSize().getBlockY());
 			settings.addSetting(prefix + "sz", game.getSize().getBlockZ());
 			settings.addSetting(prefix + "increment", game.hardSpacing);
 			settings.addSetting(prefix + "density", game.softDensity);
 			settings.addSetting(prefix + "hdensity", game.hardDensity);
 			settings.addSetting(prefix + "npcs", game.maxNPCs);
 			settings.addSetting(prefix + "sizePer", game.sizePerPlayer);
 			settings.addSetting(prefix + "autoScale", game.autoScale);
 			settings.addSetting(prefix + "powerRarity", game.powerRarity);
 		}
 		settings.writeToFile(getDataFolder() + File.separator + "regions.txt");
 	}
 	
 	public boolean defaultAutoScale = false;
 	
 	public Location victoryLocation;
 	
 	public void onEnable()
 	{
 		victoryLocation = getServer().getWorld("world").getSpawnLocation();
 		entityListener = new EntityListener(this);
 		blockListener = new BlockListener(this);
 		getServer().getPluginManager().registerEvents(blockListener, this);
 		getServer().getPluginManager().registerEvents(entityListener, this);
 		
 		loadData();
 		
 		logicTimer = new Timer();
 		logicTimer.scheduleAtFixedRate(new TimerTask() {
 			public void run() {
 				removeMissingPlayers();
 				for ( int i = 0; i < games.size(); i ++) {
 					games.get(i).onTimer();
 				}
 			}
 		}, 1000, 1000);
 	}
 	
 	public void onDisable() {
 		saveData();
 	}
 	
 	public int getActiveGameCount() {
 		int count = 0;
 		for ( int i = 0; i < games.size(); i++ ) {
 			if ( games.get(i).bStarted )  {
 				count++;
 			}
 		}
 		return count;
 	}
 	
 	public BomberGame getGame(int id) {
 		for ( int i = 0; i < games.size(); i++ ) {
 			if ( games.get(i).getID() == id ) {
 				return games.get(i);
 			}
 		}
 		return null;
 	}
 	public BomberGame getGame(int x, int y, int z) {
 		for ( int i = 0; i < games.size(); i++ ) {
 			if ( games.get(i).containsBlock(x, y, z) ) {
 				return games.get(i);
 			}
 		}
 		return null;
 	}
 	
 	public BomberGame getGame(Location loc) {
 		return getGame(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
 	}
 	
 	public BomberGame getGame(Block b) {
 		return getGame(b.getX(), b.getY(), b.getZ());
 	}
 	
 	public int newID() {
 		int id = 1;
 		while ( getGame(id) != null ) {
 			id++;
 		}
 		return id;
 	}
 	
 	public boolean containsBlock(int x, int y, int z) {
 		for ( int i = 0; i < games.size(); i++ ) {
 			if ( games.get(i).containsBlock(x, y, z) ) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean containsBlock(Block b) {
 		return containsBlock(b.getX(), b.getY(), b.getZ());
 	}
 	
 	public void removeMissingPlayers() {
 		java.util.Vector<BomberPlayer> toRemove = new java.util.Vector<BomberPlayer>();
 		for ( int i = 0; i < players.size(); i++ ) {
 			if ( !players.get(i).player.isOnline() ) {
 				toRemove.add(players.get(i));
 			}
 		}
 		
 		for ( int i = 0; i < toRemove.size(); i++ ) {
 			BomberPlayer rPlayer = toRemove.get(i);
 			BomberGame game = getGame(rPlayer.gameID);
 			if ( game != null ) {
 				game.removePlayer(rPlayer);
 			}
 			players.remove(rPlayer);
 		}
 	}
 	
 	public BomberPlayer getPlayer(Player player) {
 		for (int i = 0; i < players.size(); i++ ) {
 			if ( players.get(i).player == player ) {
 				return players.get(i);
 			}
 		}
 		
 		BomberPlayer newPlayer = new BomberPlayer(player, 0);
 		players.add(newPlayer);
 		return newPlayer;
 	}
 	
 	public boolean isPlayerInGame(Player player) {
 		BomberPlayer bsPlayer = getPlayer(player);
 		return bsPlayer.gameID != 0;
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		String command = cmd.getName();
 		
 		//essentially only allowing players to send messages
 		if(!(sender instanceof Player))
 		{
 			return false;
 		}
 		
 		Player player = (Player)sender;
 		
 		if ( command.equalsIgnoreCase("bs") ) {
 			if ( args.length > 0 ) {
 				if ( sender.isOp() ) {
 					if ( args[0].equalsIgnoreCase("new") ) {
 						BomberGame game = new BomberGame(this, newID(), player.getLocation().toVector().add(new Vector(-sizeX/2, -1, -sizeZ/2)), new Vector(sizeX, sizeY, sizeZ));
 						game.hardDensity = hDensity;
 						game.softDensity = density;
 						game.hardSpacing = columnIncrement;
 						game.maxNPCs = defaultNPCs;
 						game.autoScale = defaultAutoScale;
 						game.sizePerPlayer = sizePer;
 						game.powerRarity = powerRarity;
 						games.add(game);
 						sender.sendMessage(ChatColor.GREEN + "New game created with id " + game.getID() + ". Initializing region...");
 						game.initRegion();
 						player.teleport(new Location(player.getWorld(), player.getLocation().getX(), game.getBottomLeft().getBlockY() + game.getSize().getBlockY(), player.getLocation().getZ()));
 						sender.sendMessage(ChatColor.GREEN + "Region initialized. Use /bs join " + ChatColor.GOLD + game.getID() + ChatColor.GREEN + " to join.");
 					} else if ( args[0].equalsIgnoreCase("start") ) {
 						if ( args.length > 1 ) {
 							try {
 								int id = Integer.valueOf(args[1]);
 								BomberGame game = getGame(id);
 								if ( game != null ) {
 									if ( !game.bStarted ) {
 										game.startGame();
 										if ( game.players.size() > 1 ) {
 											getServer().broadcastMessage(ChatColor.GOLD + "Game " + ChatColor.GREEN + game.getID() + ChatColor.GOLD + " was started by " + sender.getName() + ".");
 										} else {
 											sender.sendMessage(ChatColor.RED + "Can't start game " + ChatColor.GOLD + game.getID() + ChatColor.RED + ". Not enough players.");
 										}
 									}
 								} else {
 									sender.sendMessage(ChatColor.RED + "Game " + ChatColor.GOLD + id + ChatColor.RED + " does not exist.");
 								}
 							} catch ( Exception e ) {
 								sender.sendMessage(ChatColor.RED + "Exception: " + e.getMessage());
 							}
 						} else {
 							BomberGame game = getGame(selectedGame);
 							if ( game != null ) {
 								game.startGame();
 								getServer().broadcastMessage(ChatColor.GOLD + "Game " + ChatColor.GREEN + game.getID() + ChatColor.GOLD + " was started by " + sender.getName() + ".");
 							} else {
 								sender.sendMessage(ChatColor.RED + "No game selected.");
 							}
 						}
 					} else if ( args[0].equalsIgnoreCase("stop") ) {
 						if ( args.length > 1 ) {
 							try {
 								int id = Integer.valueOf(args[1]);
 								BomberGame game = getGame(id);
 								if ( game != null ) {
 									if ( game.bStarted ) {
 										game.stopGame();
 										getServer().broadcastMessage(ChatColor.GOLD + "Game " + ChatColor.GREEN + game.getID() + ChatColor.GOLD + " was force stopped by " + sender.getName() + ".");
 									}
 								} else {
 									sender.sendMessage(ChatColor.RED + "Game " + ChatColor.GOLD + id + ChatColor.RED + " does not exist.");
 								}
 							} catch ( Exception e ) {
 								sender.sendMessage(ChatColor.RED + "Exception: " + e.getMessage());
 							}
 						} else {
 							BomberGame game = getGame(selectedGame);
 							if ( game != null ) {
 								if ( game.bStarted ) {
 									game.stopGame();
 									getServer().broadcastMessage(ChatColor.GOLD + "Game " + ChatColor.GREEN + game.getID() + ChatColor.GOLD + " was force stopped by " + sender.getName() + ".");
 								} else {
 									sender.sendMessage(ChatColor.RED + "Game " + ChatColor.GOLD + selectedGame + ChatColor.GREEN + " is not even in progress.");
 								}
 							} else {
 								sender.sendMessage(ChatColor.RED + "No game selected.");
 							}
 						}
 					} else if ( args[0].equalsIgnoreCase("clear") ) {
 						if ( args.length > 1 ) {
 							try {
 								int id = Integer.valueOf(args[1]);
 								BomberGame game = getGame(id);
 								if ( game != null ) {
 									game.clearRegion();
 								} else {
 									sender.sendMessage(ChatColor.RED + "Game " + ChatColor.GOLD + id + ChatColor.RED + " does not exist.");
 								}
 							} catch ( Exception e ) {
 								sender.sendMessage(ChatColor.RED + "Exception: " + e.getMessage());
 							}
 						} else {
 							BomberGame game = getGame(selectedGame);
 							if ( game != null ) {
 								game.clearRegion();
 							} else {
 								sender.sendMessage(ChatColor.RED + "No game selected.");
 							}
 						}
 					} else if ( args[0].equalsIgnoreCase("delete") ) {
 						if ( args.length > 1 ) {
 							try {
 								int id = Integer.valueOf(args[1]);
 								BomberGame game = getGame(id);
 								if ( game != null ) {
 									getServer().broadcastMessage(ChatColor.GOLD + "Game " + ChatColor.GREEN + game.getID() + ChatColor.GOLD + " was deleted by " + sender.getName() + ".");
 									game.deleteEverything();
 									games.remove(game);
 								} else {
 									sender.sendMessage(ChatColor.RED + "Game " + ChatColor.GOLD + id + ChatColor.RED + " does not exist.");
 								}
 							} catch ( Exception e ) {
 								sender.sendMessage(ChatColor.RED + "Exception: " + e.getMessage());
 							}
 						} else {
 							BomberGame game = getGame(selectedGame);
 							if ( game != null ) {
 								getServer().broadcastMessage(ChatColor.GOLD + "Game " + ChatColor.GREEN + game.getID() + ChatColor.GOLD + " was deleted by " + sender.getName() + ".");
 								game.deleteEverything();
 								games.remove(game);
 								selectedGame = 0;
 							} else {
 								sender.sendMessage(ChatColor.RED + "No game selected.");
 							}
 						}
 					} else if ( args[0].equalsIgnoreCase("fill") ) {
 						if ( args.length > 1 ) {
 							try {
 								int id = Integer.valueOf(args[1]);
 								BomberGame game = getGame(id);
 								if ( game != null ) {
 									game.clearRegion();
 									game.addComplexity();
 								} else {
 									sender.sendMessage(ChatColor.RED + "Game " + ChatColor.GOLD + id + ChatColor.RED + " does not exist.");
 								}
 							} catch ( Exception e ) {
 								sender.sendMessage(ChatColor.RED + "Exception: " + e.getMessage());
 							}
 						} else {
 							BomberGame game = getGame(selectedGame);
 							if ( game != null ) {
 								game.clearRegion();
 								game.addComplexity();
 							} else {
 								sender.sendMessage(ChatColor.RED + "No game selected.");
 							}
 						}
 					} else if ( args[0].equalsIgnoreCase("sel") ) {
 						if ( args.length > 1 ) {
 							try {
 								selectedGame = Integer.valueOf(args[1]);
 								BomberGame game = getGame(selectedGame);
 								if ( game != null ) {
 									sender.sendMessage(ChatColor.GREEN + "Selected game " + ChatColor.GOLD + selectedGame + ChatColor.GREEN + ".");
 								} else {
 									sender.sendMessage(ChatColor.RED + "Game " + ChatColor.GOLD + selectedGame + ChatColor.RED + " does not exist.");
 								}
 							} catch ( Exception e ) {
 								sender.sendMessage(ChatColor.RED + "Exception: " + e.getMessage());
 							}
 						} else {
 							sender.sendMessage(ChatColor.YELLOW + "Currently selected game: " + ChatColor.GOLD + selectedGame + ChatColor.YELLOW + ".");
 						}
 					} else if ( args[0].equalsIgnoreCase("repair") ) {
 						if ( args.length > 1 ) {
 							try {
 								int id = Integer.valueOf(args[1]);
 								BomberGame game = getGame(id);
 								if ( game != null ) {
 									sender.sendMessage(ChatColor.GREEN + "Repairing game " + ChatColor.GOLD + id + ChatColor.GREEN + ".");
 									game.repair();
 								}
 							} catch ( Exception e ) {
 								sender.sendMessage(ChatColor.RED + "Exception: " + e.getMessage());
 							}
 						} else {
 							BomberGame game = getGame(selectedGame);
 							if ( game != null ) {
 								sender.sendMessage(ChatColor.GREEN + "Repairing game " + ChatColor.GOLD + selectedGame + ChatColor.GREEN + ".");
 								game.repair();
 							} else {
 								sender.sendMessage(ChatColor.RED + "No game selected.");
 							}
 						}
 					} else if ( args[0].equalsIgnoreCase("size") ) {
 						try {
 							if ( args.length > 3 ) {
 								sizeX = Integer.valueOf(args[1]);
 								sizeZ = Integer.valueOf(args[2]);
 								sizeY = Integer.valueOf(args[3]);
 							} else if ( args.length > 2) {
 								sizeX = Integer.valueOf(args[1]);
 								sizeZ = Integer.valueOf(args[2]);
 							} else if ( args.length > 1 ) {
 								sizeX = sizeZ = Integer.valueOf(args[1]);
 							}
 							
 							if ( sizeX < 8 ) sizeX = 8;
 							if ( sizeY < 4) sizeY = 4;
 							if ( sizeZ < 8 ) sizeZ = 8;
 							
 							sender.sendMessage(ChatColor.YELLOW + "Current default region size: " + sizeX + "x" + sizeZ + " and " + sizeY + " tall.");
 						} catch ( Exception e ) {
 							e.printStackTrace();
 							sender.sendMessage(ChatColor.RED + "Exception: " + e.getMessage());
 						}
 					} else if ( args[0].equalsIgnoreCase("density") ) {
 						try {
 							if ( args.length > 1 ) {
 								density = Integer.valueOf(args[1]);
 							}
 							if ( density > 100 ) {
 								density = 100;
 							} else if ( density < 0 ) {
 								density = 0;
 							}
 							sender.sendMessage(ChatColor.YELLOW + "Current default soft density: " + density);
 							BomberGame game = getGame(selectedGame);
 							if ( game != null ) {
 								game.softDensity = density;
 								
 								sender.sendMessage(ChatColor.GREEN + "Set value for game " + ChatColor.GOLD + selectedGame + ChatColor.GREEN + ".");
 							}
 						} catch ( Exception e ) {
 							e.printStackTrace();
 							sender.sendMessage(ChatColor.RED + "Exception: " + e.getMessage());
 						}
 					} else if ( args[0].equalsIgnoreCase("hdensity") ) {
 						try {
 							if ( args.length > 1 ) {
 								hDensity = Integer.valueOf(args[1]);
 							}
 							if ( hDensity > 100 ) {
 								hDensity = 100;
 							} else if ( hDensity < 0 ) {
 								hDensity = 0;
 							}
 							sender.sendMessage(ChatColor.YELLOW + "Current default hard (unbreakable) density: " + hDensity);
 							BomberGame game = getGame(selectedGame);
 							if ( game != null ) {
 								game.hardDensity = hDensity;
 								
 								sender.sendMessage(ChatColor.GREEN + "Set value for game " + ChatColor.GOLD + selectedGame + ChatColor.GREEN + ".");
 							}
 						} catch ( Exception e ) {
 							e.printStackTrace();
 							sender.sendMessage(ChatColor.RED + "Exception: " + e.getMessage());
 						}
 					} else if ( args[0].equalsIgnoreCase("increment") ) {
 						try {
 							if ( args.length > 1 ) {
 								columnIncrement = Integer.valueOf(args[1]);
 							}
 							if ( columnIncrement < 2 ) {
 								columnIncrement = 2;
 							}
 							sender.sendMessage(ChatColor.YELLOW + "Current default column increment: " + columnIncrement);
 							
 							BomberGame game = getGame(selectedGame);
 							if ( game != null ) {
 								game.hardSpacing = columnIncrement;
 								
 								sender.sendMessage(ChatColor.GREEN + "Set value for game " + ChatColor.GOLD + selectedGame + ChatColor.GREEN + ".");
 							}
 						} catch ( Exception e ) {
 							e.printStackTrace();
 							sender.sendMessage(ChatColor.RED + "Exception: " + e.getMessage());
 						}
 					} else if ( args[0].equalsIgnoreCase("margin") ) {
 						try {
 							if ( args.length > 1 ) {
 								readyMargin = Integer.valueOf(args[1]);
 							}
 							if ( readyMargin > 100 ) {
 								readyMargin = 100;
 							} else if ( density < 1 ) {
 								density = 1;
 							}
 							sender.sendMessage(ChatColor.YELLOW + "Current ready percent required to start a game: %" + readyMargin);
 						} catch ( Exception e ) {
 							e.printStackTrace();
 							sender.sendMessage(ChatColor.RED + "Exception: " + e.getMessage());
 						}
 					} else if ( args[0].equalsIgnoreCase("npcs") ) {
 						try {
 							if ( args.length > 1 ) {
 								defaultNPCs = Integer.valueOf(args[1]);
 							}
 							if ( defaultNPCs < 0 ) defaultNPCs = 0; // No high cap because people should have the freedom to break their servers.
 							
 							sender.sendMessage(ChatColor.YELLOW + "Current default npc count: " + defaultNPCs);
 							
 							BomberGame game = getGame(selectedGame);
 							if ( game != null ) {
 								game.maxNPCs = defaultNPCs;
 								
 								sender.sendMessage(ChatColor.GREEN + "Set value for game " + ChatColor.GOLD + selectedGame + ChatColor.GREEN + ".");
 							}
 						} catch ( Exception e ) {
 							e.printStackTrace();
 							sender.sendMessage(ChatColor.RED + "Exception: " + e.getMessage());
 						}
 					} else if ( args[0].equalsIgnoreCase("sizeper") ) {
 						try {
 							if ( args.length > 1 ) {
 								sizePer = Integer.valueOf(args[1]);
 							}
 							if ( sizePer < 3 ) sizePer = 3;
 							
 							sender.sendMessage(ChatColor.YELLOW + "Current size per player: " + sizePer);
 							
 							BomberGame game = getGame(selectedGame);
 							if ( game != null ) {
 								game.sizePerPlayer = sizePer;
 								
 								sender.sendMessage(ChatColor.GREEN + "Set value for game " + ChatColor.GOLD + selectedGame + ChatColor.GREEN + ".");
 							}
 						} catch ( Exception e ) {
 							e.printStackTrace();
 							sender.sendMessage(ChatColor.RED + "Exception: " + e.getMessage());
 						}
 					} else if ( args[0].equalsIgnoreCase("autoscale") ) {
 						boolean bFailed = false;
 						try {
 							if ( args.length > 1 ) {
 								defaultAutoScale = (Integer.valueOf(args[1]) == 1);
 							}
 						} catch ( Exception e ) {
 							try {
 								defaultAutoScale = Boolean.valueOf(args[1]);
 							} catch ( Exception e2 ) {
 								e2.printStackTrace();
 								sender.sendMessage(ChatColor.RED + "Exception: " + e.getMessage());
 								bFailed = true;
 							}
 						}
 						sender.sendMessage(ChatColor.YELLOW + "Current auto-scale state: " + defaultAutoScale);
 						if ( !bFailed ) {
 							BomberGame game = getGame(selectedGame);
 							if ( game != null ) {
 								game.autoScale = defaultAutoScale;
 								
 								sender.sendMessage(ChatColor.GREEN + "Set value for game " + ChatColor.GOLD + selectedGame + ChatColor.GREEN + ".");
 							}
 						}
 					} else if ( args[0].equalsIgnoreCase("powers") ) {
 						try {
 							if ( args.length > 1 ) {
 								powerRarity = Integer.valueOf(args[1]);
 							}
 							if ( powerRarity < -1 ) powerRarity = -1; // No powers at all.
 							
 							sender.sendMessage(ChatColor.YELLOW + "Current powerup rarity (2 means every block has a powerup): " + powerRarity);
 							
 							BomberGame game = getGame(selectedGame);
 							if ( game != null ) {
 								game.powerRarity = powerRarity;
 								sender.sendMessage(ChatColor.GREEN + "Set value for game " + ChatColor.GOLD + selectedGame + ChatColor.GREEN + ".");
 							}
 						} catch ( Exception e ) {
 							e.printStackTrace();
 							sender.sendMessage(ChatColor.RED + "Exception: " + e.getMessage());
 						}
 					} 
 				}
 				BomberPlayer bsPlayer = getPlayer(player);
 				if ( args[0].equalsIgnoreCase("join") ) {
 					if ( args.length > 1 ) {
 						BomberGame currentGame = getGame(bsPlayer.gameID);
 						if ( currentGame == null || !currentGame.bStarted ) {
 							try {
 								int id = Integer.valueOf(args[1]);
 								if ( id > 0 ) {
 									BomberGame game = getGame(id);
 									if ( game != null ) {
 										if ( game.bStarted ) {
 											sender.sendMessage(ChatColor.RED + "You cannot join game " + ChatColor.GREEN + id + ChatColor.RED + " because it is in progress.");
 										} else {
 											if ( currentGame != null ) {
 												currentGame.players.remove(bsPlayer);
 											}
 											game.addPlayer(bsPlayer);
 											int numPlayers = game.players.size();
 											getServer().broadcastMessage(ChatColor.GREEN + player.getDisplayName() + ChatColor.GOLD  + " joined game " + ChatColor.GREEN + id + ChatColor.GOLD + " (" + (numPlayers) + " total).");
 											
 										}
 									} else {
 										sender.sendMessage(ChatColor.RED + "Game id " + id + " does not exist.");
 									}
 								}
 							} catch ( Exception e ) {
 								sender.sendMessage(ChatColor.RED + "The format is /bs join <number>");
 							}
 						} else {
 							sender.sendMessage(ChatColor.RED + "You are already in an active game. Use /bs leave to leave");
 						}
 					} else {
 						int id = getPlayer(player).gameID;
 						BomberGame game = getGame(id);
 						if ( game != null) {
 							sender.sendMessage(ChatColor.YELLOW + "Your current game ID is " + ChatColor.GREEN + id);
 						} else {
 							sender.sendMessage(ChatColor.YELLOW + "You are not in a game. Please use /bs join <gameid>");
 						}
 					}
 				} else if ( args[0].equalsIgnoreCase("leave") ) {
 					BomberGame game = getGame(bsPlayer.gameID);
 					if ( game != null ) {
 						game.removePlayer(bsPlayer);
 						bsPlayer.gameID = 0;
 						if ( game.bStarted ) {
 							game.sendMessage(ChatColor.RED + player.getDisplayName() + ChatColor.YELLOW + " left the bomber game. ");
 							sender.sendMessage(ChatColor.RED + "Leaver! :(");
 						} else {
 							sender.sendMessage(ChatColor.GREEN + "Left game " + ChatColor.GOLD + game.getID() + ChatColor.GREEN + ".");
 							game.sendMessage(ChatColor.RED + player.getDisplayName() + ChatColor.YELLOW + " left the idle bomber game. ");
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "You are not even in a game. Use /bs join <gameid>");
 					}
 				} else if ( args[0].equalsIgnoreCase("ready") ) {
 					BomberGame game = getGame(bsPlayer.gameID);
 					if ( game != null ) {
 						bsPlayer.isReady = true;
 						if ( !game.bStarted ) {
 							game.sendMessage(ChatColor.GREEN + player.getDisplayName() + ChatColor.GOLD + " is ready! Use \"/bs ready\" to ready up.");
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "You are not even in a game. Use /bs join <gameid>");
 					}
 				} else if ( args[0].equalsIgnoreCase("unready") ) {
 					BomberGame game = getGame(bsPlayer.gameID);
 					if ( game != null ) {
 						bsPlayer.isReady = false;
 						if ( !game.bStarted ) {
 							game.sendMessage(ChatColor.GREEN + player.getDisplayName() + ChatColor.GOLD + " is not ready. Use \"/bs ready\" to ready up.");
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "You are not even in a game. Use /bs join <gameid>");
 					}
 				}
 			} else {
 				int numGames = getActiveGameCount();
 				sender.sendMessage(ChatColor.YELLOW + "There " + (numGames == 0 || numGames != 1 ? "are " : "is ") + (numGames == 0 ? ChatColor.RED : ChatColor.GOLD) + numGames + ChatColor.YELLOW + " active " + (numGames == 0 || numGames != 1 ? "games." : "game."));
 				String messageStr = ChatColor.GOLD + "[";
 				for ( int i = 0; i < games.size(); i++ ) {
 					BomberGame game = games.get(i);
 					messageStr += game.bStarted ? ChatColor.RED : ChatColor.GREEN;
 					messageStr += game.getID();
 					messageStr += (game.players.size() == 0 ? ChatColor.YELLOW : ChatColor.BLUE) + " (" + game.players.size() + ")";
 					if ( i < games.size() - 1) {
 						messageStr += ChatColor.GOLD + ", ";
 					}
 				}
 				messageStr += ChatColor.GOLD + "]";
 				sender.sendMessage(messageStr);
 				
 			}
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public BomberSteve() {
 		logicTimer = null;
 		entityListener = null;
 		blockListener = null;
 		games = new java.util.Vector<BomberGame>();
 		players = new java.util.Vector<BomberPlayer>();
 	}
 }
