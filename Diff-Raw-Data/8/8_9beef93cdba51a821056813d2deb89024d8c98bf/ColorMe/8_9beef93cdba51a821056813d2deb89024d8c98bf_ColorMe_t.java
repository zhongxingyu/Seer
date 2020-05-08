 package com.sparkedia.valrix.ColorMe;
 
 import java.io.File;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ColorMe extends JavaPlugin {
 	private final ColorPlayerListener pListener = new ColorPlayerListener(this);
 	protected static final Logger log = Logger.getLogger("Minecraft");
 	public static Property colors = null;
 	public String pName = null;
 	private Property config = null; // need at least one config option for non-OP use
 	
 	public void onDisable() {
 		PluginDescriptionFile pdf = this.getDescription();
 		log.info("["+pName+"] v"+pdf.getVersion()+" has been disabled.");
 	}
 
 	public void onEnable() {
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_CHAT, this.pListener, Event.Priority.Normal, this);
 		
 		PluginDescriptionFile pdf = this.getDescription();
 		pName = pdf.getName();
 		log.info("["+pName+"] v"+pdf.getVersion()+" has been enabled.");
 		
 		if (!this.getDataFolder().isDirectory()) {
 			this.getDataFolder().mkdir();
 		}
 		if (colors == null) {
 			colors = new Property(this.getDataFolder()+"/players.color", this);
 		}
 		if (config == null) {
 			//Does the config exist, if not then make a new blank one
 			if (!(new File(this.getDataFolder()+"/config.txt").exists())) {
 				config = new Property(this.getDataFolder()+"/config.txt", this);
 				config.setBoolean("OP", true); //OP only by default
 			} else {
 				config = new Property(this.getDataFolder()+"/config.txt", this);
 			}
 		}
 		
 		// /color <color/name> [name] (name is optional since you can color your own name)
 		getCommand("colorme").setExecutor(new CommandExecutor() {
 			public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
 				if (sender instanceof Player) {
 					if (cmd.getName().equalsIgnoreCase("colorme")) {
 						if (((Player)sender).isOp() || !config.getBoolean("OP")) { //only OP can use unless OP=false
 							if (args.length == 1) {
 								if (args[0].equalsIgnoreCase("list")) {
 									// /color list
 									((Player)sender).sendMessage("Color List:");
 									String color;
 									String msg1 = "";
 									String msg2 = "";
 									for (int i = 0; i <= 15; i++) {
 										color = ChatColor.getByCode(i).name();
 										if (i == 0) {
 											msg1 = ChatColor.valueOf(color)+color.toLowerCase().replace("_", "");
 										} else if (i > 0 && i < 7) {
 											msg1 += " "+ChatColor.valueOf(color)+color.toLowerCase().replace("_", "");
 										} else if (i == 7) {
 											msg2 = ChatColor.valueOf(color)+color.toLowerCase().replace("_", "");
 										} else {
 											msg2 += " "+ChatColor.valueOf(color)+color.toLowerCase().replace("_", "");
 										}
 									}
 									((Player)sender).sendMessage(msg1);
 									((Player)sender).sendMessage(msg2);
 									return true;
 								} else if (ColorMe.colors.keyExists(args[0].toLowerCase())) { //player's name
 									// if just a name is found, remove color preset
 									ColorMe.colors.remove(args[0].toLowerCase());
 									return true;
 								} else {
 									// only chose a color (hopefully, let's check)
 									String col = args[0].toLowerCase();
 									String color;
 									for (int i = 0; i <= 15; i++) {
 										color = ChatColor.getByCode(i).name().toLowerCase().replace("_", "");
 										if (col.equalsIgnoreCase(color)) {
 											// only chose a color and it matched, set sender's name color
 											colors.setString(((Player)sender).getName().toLowerCase(), col);
 											return true;
 										}
 									}
 									((Player)sender).sendMessage(ChatColor.RED+"Could not find that color.");
 									return true;
 								}
 							} else if (args.length == 2) {
 								// /color <color> [name]
 								// Only OP can change another player's
 								String name = args[1].toLowerCase();
								if (((Player)sender).isOp() || name.equalsIgnoreCase(((Player)sender).getName().toLowerCase())) {
 									String col = args[0].toLowerCase();
 									colors.setString(name, col); //name=color
									((Player)sender).sendMessage("Gave "+name+" the color "+col);
 									return true;
 								}
 								return true;
 							}
						} else {
							((Player)sender).sendMessage("You're not an OP or /colorme isn't allowed.");
							return true;
 						}
 					}
 				}
 				return false;
 			}
 		});
 	}
 }
