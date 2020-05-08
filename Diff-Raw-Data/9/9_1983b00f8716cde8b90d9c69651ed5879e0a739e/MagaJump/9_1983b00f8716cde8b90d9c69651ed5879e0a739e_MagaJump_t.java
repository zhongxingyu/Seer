 package me.flungo.bukkit.MegaJump;
 
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class MagaJump extends JavaPlugin {
 	
 	public static MagaJump plugin;
 	
 	public final Logger logger = Logger.getLogger("MineCraft");
 	
 	public final PlayerListeners playerListener = new PlayerListeners(this);
 	
 	public HashMap<Player, Integer> activePlayers = new HashMap<Player, Integer>();
 	
 	public int defaultMultiplier;
 	
 	public boolean debug;
 	
 	public void onDisable() {
 		PluginDescriptionFile pdffile = this.getDescription();
 		this.logger.info(pdffile.getName() + " is now disabled");
 	}
 	
 	public void onEnable() {
 		enable();
 		PluginDescriptionFile pdffile = this.getDescription();
 		this.logger.info(pdffile.getName() + " version " + pdffile.getVersion() + " is enabled.");
 	}
 	
 	private void enable() {
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(this.playerListener, this);
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 		defaultMultiplier = getConfig().getInt("default-multiplier");
 		debug = getConfig().getBoolean("debug");
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if (cmd.getName().equalsIgnoreCase("mj")) {	
 			int amp;		
 			if (sender instanceof Player == false) {
 				logMessage("The /mj command can only be used in game.");
 				//TODO: Check for a player name argument and set the amplifier argument.
 				
 				return true;
 			} else {
 				if (args.length == 0) {
 					Player p = (Player) sender;
 					amp = defaultMultiplier;
 					if (activePlayers.containsKey(p)) {
 						p.sendMessage(ChatColor.RED + "MegaJump disabled");
 						activePlayers.remove(p);
 						return true;
 					} else {
 						p.sendMessage(ChatColor.RED + "MegaJump enabled");
 						activePlayers.put(p,amp);
 						return true;
 					}
 				} else if (args.length == 1) {
 					String pName = null;
 					try {
 						amp = Integer.parseInt(args[0]);
 					} catch (NumberFormatException e1) {
						amp = defaultMultiplier;
 						pName = args[0];
 					}
 					if (pName == null) {
 						Player p = (Player) sender;
 						p.sendMessage(ChatColor.RED + "MegaJump enabled with amplification factor " + amp);
 						activePlayers.put(p,amp);
 						return true;
 					} else {
 						Player p1 = (Player) sender;
 						Player p2 = getServer().getPlayerExact(pName);
 						if (p2 == null) {
 							p1.sendMessage("MegaJump cannot find specified user");
 						} else {
 							p1.sendMessage("MegaJump enabled for " + pName);
 							p2.sendMessage(ChatColor.RED + "MegaJump enabled");
 							activePlayers.put(p2,amp);
 						}
 						return true;
 					}
 				} else if (args.length == 2) {
 					String pName = args[0];
 					Player p1 = (Player) sender;
 					Player p2 = getServer().getPlayerExact(pName);
 					if (p2 == null) {
 						p1.sendMessage("MegaJump cannot find specified user");
 					} else {
 						try {
 							amp = Integer.parseInt(args[1]);
 						} catch (NumberFormatException e1) {
 							return false;
 						}
 						p1.sendMessage("MegaJump enabled for " + pName + " with amplification factor " + amp);
 						p2.sendMessage(ChatColor.RED + "MegaJump enabled with amplification factor " + amp);
 						activePlayers.put(p2,amp);
 					}
 				}
 			}
 		}
 		
 		return false;
 	}
 
 	public void logMessage(String msg) {
 		PluginDescriptionFile pdFile = this.getDescription();
 		this.logger.info(pdFile.getName() + " " + pdFile.getVersion() + " " + msg);
 	}
 }
