 package me.menexia.dynafish;
 
 import java.io.File;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class DynaFish extends JavaPlugin {
 	public static DynaFish plugin;
 	
 	public final Logger logger = Logger.getLogger("Minecraft");
 	private final DFEntityListener entityListener = new DFEntityListener(this);
 	public Set<Player> user = new HashSet<Player>();
 	// HashSet is a command tracker.
 	ChatColor ese = ChatColor.RED;
 	
	boolean ENABLED_FOR_ALL = false; // ignores all commands and permissions
 	// overrides permissions and commands check
 	// variables are mentioned for reference
 	
 @Override
 public void onDisable() {
 	this.logger.info("[DynaFish] disabled!");
 }
 
 @Override
 public void onEnable() {
 	try {
 		File fileconfig = new File(getDataFolder(), "config.yml");
 		if (!fileconfig.exists()) {
 			getDataFolder().mkdir();
 			new File(getDataFolder(), "config.yml");
 			this.getConfig().options().copyDefaults(true);
 			this.saveConfig();
 		}
 	} catch (Exception e1){
 		e1.printStackTrace();
 	}
 	emap_asym();
 	PluginManager pm = this.getServer().getPluginManager();
 	pm.registerEvent(Event.Type.ENTITY_EXPLODE, this.entityListener, Event.Priority.Monitor, this);
 	pm.registerEvent(Event.Type.ENTITY_DAMAGE, this.entityListener, Event.Priority.Monitor, this);
 	PluginDescriptionFile pdf = this.getDescription();
 	this.logger.info( "[DynaFish]" + " version " + pdf.getVersion() + " by MeneXia is enabled!" );
 	this.logger.info("[DynaFish] Permissions will default to op if SuperPerms is not present.");
 }
 
 public boolean hasUser(Player player) {
 	return user.contains(player);
 }
 
 public void setUser(Player player, boolean enabled) {
 	if (enabled) {
 		user.add(player);
 		player.sendMessage(ese + "[DynaFish]" + ChatColor.WHITE + " enabled.");
 	} else {
 		user.remove(player);
 		player.sendMessage(ese + "[DynaFish]" + ChatColor.WHITE + " disabled.");
 	}
 }
 
 public boolean onCommand(CommandSender sender, Command cmd, String zhf, String[] args) {
 		if (!(sender instanceof Player)) {
 			sender.sendMessage("You must be in-game to use DynaFish.");
 			return true; //return ends current code.
 			}
 	if (ENABLED_FOR_ALL == true) {return false;}
 		if(sender.hasPermission("dynafish.use")) {
 			if (zhf.equalsIgnoreCase("dynafish") || zhf.equalsIgnoreCase("df")) {
 				if (sender instanceof Player) {
 				Player player = (Player)sender;
 				setUser(player, !hasUser(player));
 					}
 				}
 			} else {
 				sender.sendMessage(ese + "Sorry, no DynaFish for you!");
 				}
 		return true;
 		}
 
 public void emap_asym() {
 	ENABLED_FOR_ALL = this.getConfig().getBoolean("ENABLED_FOR_ALL");
 }
 
 
 }
