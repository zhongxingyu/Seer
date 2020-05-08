 package me.ellbristow.WalkTheWalk;
 
 import java.util.HashMap;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class WalkTheWalk extends JavaPlugin implements Listener {
 	
 	public static WalkTheWalk plugin;
 	protected FileConfiguration config;
 	public HashMap<String, Double> xpRounding = new HashMap<>();
 	
 	@Override
 	public void onDisable() {
 	}
 
 	@Override
 	public void onEnable() {
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(this, this);
 		config = getConfig();
 		int walkingBoots = config.getInt("walking_boots", 3);
 		config.set("walking_booots", walkingBoots);
 		int xpBoost = config.getInt("xp_boost", 1);
 		config.set("xp_boost", xpBoost);
 		int boostBlocks = config.getInt("boost_blocks", 10);
 		config.set("boost_blocks", boostBlocks);
 		boolean announce = config.getBoolean("announce_changes", true);
 		config.set("announce_changes", announce);
 		saveConfig();
 	}
 	
         @Override
 	public boolean onCommand (CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (commandLabel.equalsIgnoreCase("wtw")) {
 			if (args.length == 0) {
 				// Help
 				PluginDescriptionFile pdfFile = getDescription();
 				sender.sendMessage(ChatColor.GOLD + pdfFile.getName() + " by " + pdfFile.getAuthors());
 				sender.sendMessage(ChatColor.GOLD + " /wtw announce [yes|no] " + ChatColor.GRAY + ": Set how many blocks give XP");
 				sender.sendMessage(ChatColor.GOLD + " /wtw boots [0:1:2:3:4:5] " + ChatColor.GRAY + ": Set which boots give XP");
 				sender.sendMessage(ChatColor.GRAY + "   0=ALL, 1=Leather, 2=Iron, 3=Gold, 4=Diamond, 5=Chain");
 				sender.sendMessage(ChatColor.GOLD + " /wtw blocks [Number Of Blocks] " + ChatColor.GRAY + ": Set how many blocks give XP");
 				sender.sendMessage(ChatColor.GOLD + " /wtw get " + ChatColor.GRAY + ": See the current settings");
 				sender.sendMessage(ChatColor.GOLD + " /wtw xp [XP Amount] " + ChatColor.GRAY + ": Set how much XP is given");
 				return true;
 			}
 			else if (args.length == 1 && args[0].equalsIgnoreCase("get")) {
 				PluginDescriptionFile pdfFile = getDescription();
 				sender.sendMessage(ChatColor.GOLD + pdfFile.getName() + " by " + pdfFile.getAuthors());
 				int walkingBoots = config.getInt("walking_boots", 3);
 				int xpBoost = config.getInt("xp_boost", 1);
 				boolean announce = config.getBoolean("announce_changes", true);
 				String ann = "No";
 				if (announce) {
 					ann = "Yes";
 				}
 				int boostBlocks = config.getInt("boost_blocks", 10);
 				String bootType = "ALL";
 				if (walkingBoots == 1) {
 					bootType = "Leather";
 				}
 				else if (walkingBoots == 2) {
 					bootType = "Iron";
 				}
 				else if (walkingBoots == 3) {
 					bootType = "Gold";
 				}
 				else if (walkingBoots == 4) {
 					bootType = "Diamond";
 				}
 				else if (walkingBoots == 5) {
 					bootType = "Chain";
 				}
 				sender.sendMessage(ChatColor.GOLD + " Announce Changes: " + ann);
 				sender.sendMessage(ChatColor.GOLD + " Current Boot Type: " + bootType);
 				sender.sendMessage(ChatColor.GOLD + " Current Required Blocks: " + boostBlocks);
 				sender.sendMessage(ChatColor.GOLD + " Current XP Boost: " + xpBoost);
 				return true;
 			}
 			else if (args.length != 2) {
 				// Command incorrectly formatted
 				sender.sendMessage(ChatColor.RED + "You must specify all required arguments.");
 				return false;
 			}
 			if (args[0].equalsIgnoreCase("boots")) {
 				// Set Boot Type
 				int boots = 0;
 				try {
 					boots = Integer.parseInt(args[1]);
 				} catch (NumberFormatException e) {
 					sender.sendMessage(ChatColor.RED + "Boot type must be an integer!");
 					return false;
 				}
 				String newBoots = "";
 				if (boots == 0) {
 					newBoots = "ALL";
 				}
 				else if (boots == 1) {
 					newBoots = "Leather";
 				}
 				else if (boots == 2) {
 					newBoots = "Iron";
 				}
 				else if (boots == 3) {
 					newBoots = "Gold";
 				}
 				else if (boots == 4) {
 					newBoots = "Diamond";
 				}
 				else if (boots == 5) {
 					newBoots = "Chain";
 				}
 				if ("".equals(newBoots)) {
 					sender.sendMessage(ChatColor.RED + "Boot type can only be a number from 0 to 5!");
 					return false;
 				}
 				config.set("walking_boots", boots);
 				saveConfig();
 				sender.sendMessage(ChatColor.GOLD + newBoots + " boots are now Walking Boots!");
 				boolean announce = config.getBoolean("announce_changes", true);
 				if (announce) {
 					Player[] players = getServer().getOnlinePlayers();
 					for (Player player : players) {
 						if (!player.getName().equals(sender.getName())) {
 							player.sendMessage(ChatColor.GOLD + newBoots + " boots are now Walking Boots!");
 						}
 					}
 				}
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("blocks")) {
 				// Set XP Boost
 				int blocks = 1;
 				try {
 					blocks = Integer.parseInt(args[1]);
 				} catch (NumberFormatException e) {
 					sender.sendMessage(ChatColor.RED + "Number of Blocks must be an integer!");
 					return false;
 				}
 				if (blocks == 0) {
 					sender.sendMessage(ChatColor.RED + "Number of Blocks must be at least 1!");
 					return false;
 				}
 				config.set("boost_blocks", blocks);
 				saveConfig();
 				sender.sendMessage(ChatColor.GOLD + "Players must now travel " + blocks + " blocks to get XP!");
 				boolean announce = config.getBoolean("announce_changes", true);
 				if (announce) {
 					Player[] players = getServer().getOnlinePlayers();
 					for (Player player : players) {
 						if (!player.getName().equals(sender.getName())) {
 							player.sendMessage(ChatColor.GOLD + "Players must now travel " + blocks + " blocks in Walking Boots to get XP!");
 						}
 					}
 				}
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("xp")) {
 				// Set Number of Block
 				int xp = 1;
 				try {
 					xp = Integer.parseInt(args[1]);
 				} catch (NumberFormatException e) {
 					sender.sendMessage(ChatColor.RED + "XP amount must be an integer!");
 					return false;
 				}
 				if (xp == 0) {
 					sender.sendMessage(ChatColor.RED + "XP amount must be at least 1!");
 					return false;
 				}
 				config.set("xp_boost", xp);
 				saveConfig();
 				sender.sendMessage(ChatColor.GOLD + "XP boost now set to " + xp + "!");
 				boolean announce = config.getBoolean("announce_changes", true);
 				if (announce) {
 					Player[] players = getServer().getOnlinePlayers();
 					for (Player player : players) {
 						if (!player.getName().equals(sender.getName())) {
 							player.sendMessage(ChatColor.GOLD + "Walking Boot XP boost now set to " + xp + "!");
 						}
 					}
 				}
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("announce")) {
 				// Set Number of Block
 				String message = "";
 				if (args[1].equalsIgnoreCase("yes")) {
 					config.set("announce_changes", true);
 					saveConfig();
 					message = ChatColor.GOLD + "Players will now be told about changes to Walking Boots!";
 					sender.sendMessage(message);
 					return true;
 				}
 				else if (args[1].equalsIgnoreCase("no")) {
 					config.set("announce_changes", false);
 					saveConfig();
 					message = ChatColor.GOLD + "Players will now "+ ChatColor.RED + "NOT" + ChatColor.GOLD + " be told about changes to Walking Boots!";
 					sender.sendMessage(message);
 					return true;
 				}
 				else {
 					sender.sendMessage(ChatColor.RED + "Setting must be 'yes' or 'no'!");
 				}
 				Player[] players = getServer().getOnlinePlayers();
 				for (Player player : players) {
 					if (!player.getName().equals(sender.getName())) {
 						player.sendMessage(message);
 					}
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	@EventHandler (priority = EventPriority.NORMAL)
 	public void playerWalk (PlayerMoveEvent event) {
 		// Player Moved
 		int walkingBoots = config.getInt("walking_boots", 3);
 		Player player = event.getPlayer();
 		Location fromLoc = event.getFrom();
 		Location toLoc = event.getTo();
 		int fromX = (int)fromLoc.getX();
 		int fromZ = (int)fromLoc.getZ();
 		int toX = (int)toLoc.getX();
 		int toZ = (int)toLoc.getZ();
 		if ( (fromX != toX || fromZ != toZ) && player.hasPermission("walkthewalk.use") ) {
 			// Player moved to a new block, and has permission
 			boolean rightBoots = false;
 			if (walkingBoots != 0) {
				int invBoots = player.getInventory().getBoots().getTypeId();
 				if (invBoots == 301 && walkingBoots == 1) {
 					// Leather Boots
 					rightBoots = true;
 				}
 				else if (invBoots == 309 && walkingBoots == 2) {
 					// Iron Boots
 					rightBoots = true;
 				}
 				else if (invBoots == 317 && walkingBoots == 3) {
 					// Gold Boots
 					rightBoots = true;
 				}
 				else if (invBoots == 313 && walkingBoots == 4) {
 					// Diamond Boots
 					rightBoots = true;
 				}
 				else if (invBoots == 305 && walkingBoots == 5) {
 					// Chain Boots
 					rightBoots = true;
 				}
 			} else {
 				rightBoots = true;
 			}
 			if (rightBoots) {
 				// Player was wearing the right boots
 				int xpBoost = config.getInt("xp_boost", 1);
 				int boostBlocks = config.getInt("boost_blocks", 10);
 				double rounding = 0.00;
 				if (xpRounding.get(player.getName()) != null) {
 					rounding = xpRounding.get(player.getName());
 				}
 				rounding += (double)xpBoost/boostBlocks;
 				if (rounding >= xpBoost) {
 					rounding -= xpBoost;
 					player.giveExp(xpBoost);
 				}
 				xpRounding.put(player.getName(), rounding);
 			}
 		}
 	}
 	
 }
