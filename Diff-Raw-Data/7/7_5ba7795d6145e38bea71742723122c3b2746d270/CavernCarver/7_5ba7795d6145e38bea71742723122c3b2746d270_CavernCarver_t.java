 package ca.mcpnet.CavernCarver;
 
 import java.util.logging.Logger;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class CavernCarver extends JavaPlugin {
 
	static public final String VERSION = "0.2";
 	static Logger logger = Logger.getLogger("Minecraft");
 	static public void log(String msg) {
 		logger.info("[CavernCarver] "+msg);
 	}
 	
 	public void onEnable() {
		log("CavernCarver v"+VERSION+" Plugin Enabled!");
 		getConfig().options().copyDefaults(true);
 		getConfig().options().copyHeader(true);
 		saveConfig();
 	}
 
 	public void onDisable() {
		log("CavernCarver v"+VERSION+" Plugin Disabled!");		
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd,
 			String label, String[] args) {
 		Player player = null;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		}
 
 		// carve out a cavern from the players location outwards
 		if (cmd.getName().equals("cc_cavern")) {
 			if (args.length > 2) {
 				sender.sendMessage("Too many arguments!");
 				return false;
 			}
 			if (args.length == 2) {
 				player = getServer().getPlayer(args[1]);
 				if (player == null) {
 					sender.sendMessage("Player not found!");
 					return false;
 				}
 			}
 			if (player == null) {
 				sender.sendMessage("Must specify player when calling from server console!");
 				return false;
 			}
 			int radius = 0;
 			try {
 				radius = Integer.decode(args[0]);
 			} catch (NumberFormatException e) {
 				sender.sendMessage("Must specify the size of the cavern");
 				return false;
 			}
 			if (radius > getConfig().getInt("max_cavern_radius")) {
 				sender.sendMessage("Maximum allowable radius of "+getConfig().getInt("max_cavern_radius"));
 				return true;
 			}
 			CavernCarverTask task = new CavernCarverTask(this, sender, player, player.getLocation(), radius);
 			task.setTaskid(getServer().getScheduler().scheduleSyncRepeatingTask(this, task, 10L, 2L));
 			sender.sendMessage("Scheduled cavern carver job "+task.getTaskid()+", radius "+ radius);
 			return true;
 		}
 		if (cmd.getName().equals("cc_maxradius")) {
 			if (args.length != 1) {
 				return false;
 			}
 			int radius = 0;
 			try {
 				radius = Integer.decode(args[0]);
 			} catch (NumberFormatException e) {
 				sender.sendMessage("Radius must be a positive integer!");
 				return false;
 			}
 			if (radius <= 0) {
 				sender.sendMessage("Radius must be a positive integer!");				
 			}
 			getConfig().set("max_cavern_radius", radius);
 			saveConfig();
 			sender.sendMessage("Maximum radius set to "+radius);
 			return true;
 		}
 		return false;
 	}
 }
