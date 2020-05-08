 /**
  * (C) Matt McCouaig 2012.
  * 
  * TODO: Command to access all of the maps, + map names.
  * 		 Command to access all paintings.
  */
 
 package uk.co.eclipsion.Eclipsi;
 
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class EclipsiMain extends JavaPlugin {
 	Logger log = Logger.getLogger("Minecraft");
 
 	//public static Location deathLocation = null;
 	//public static boolean backAlready = false;
	public static Map<Player, Location> deathLocation;
	public static Map<Player, Boolean> backAlready;
 
 	// public final Server server = this.getServer();
 
 	public void onEnable() {
 		getServer().getPluginManager().registerEvents(
 				new EclipsiListener(this), this);
 		
 		getServer().addRecipe(Recipes.iceBlock());
 		getServer().addRecipe(Recipes.spongeBlock());
 		getServer().addRecipe(Recipes.fireBlock());
 		getServer().addRecipe(Recipes.mossyBlock());
 		getServer().addRecipe(Recipes.zombieFood());
 		
 		log.info("[Eclipsi] Ready for takeoff. Get to the choppa'!");
 	}
 
 	public void onDisable() {
 		log.info("[Eclipsi] Disabled.");
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd,
 			String commandLabel, String[] args) {
 		if (sender.isOp() == true) {
 			if (cmd.getName().equalsIgnoreCase("Ignite")) {
 				if (args.length == 2) {
 					Commands.Ignition(sender, args[0], args[1]);
 				} else {
 					sender.sendMessage("Invalid number of arguments for this command.");
 				}
 			} else if (cmd.getName().equalsIgnoreCase("Smite")) {
 				if (args.length == 1) {
 					Commands.Smite(sender, args[0]);
 				} else {
 					sender.sendMessage("Invalid number of arguments for this command.");
 				}
 			} else if (cmd.getName().equalsIgnoreCase("Spawntree")) {
 				if (args.length == 1) {
 					Commands.SpawnTree(sender, args[0]);
 				} else {
 					sender.sendMessage("Invalid number of arguments for this command.");
 				}
 			} else if (cmd.getName().equalsIgnoreCase("Weather")) {
 				if (args.length == 1) {
 					Commands.Weather(sender, args[0]);
 				} else {
 					sender.sendMessage("Invalid number of arguments for this command.");
 				}
 			} else if (cmd.getName().equalsIgnoreCase("Spawnmob")) {
 				if (args.length == 1) {
 					Commands.SpawnEntity(sender, args[0]);
 				} else {
 					sender.sendMessage("Invalid number of arguments for this command.");
 				}
 			} else if (cmd.getName().equalsIgnoreCase("Rules")) {
 				List<String> rules = getConfig().getStringList("rules");
 				for (String s : rules) {
 					sender.sendMessage(s);
 				}
 			} else if (cmd.getName().equalsIgnoreCase("Back")) {
 				Commands.Back(sender);
 			} else if (cmd.getName().equalsIgnoreCase("Fly")) {
 				if(args.length == 0) {
 					Commands.Fly(sender, this);
 				} else if (args.length == 1) {
 					Commands.FlyTarget(sender, this, args[0]);
 				} else {
 					sender.sendMessage("Invalid number of arguments for this command.");
 				} 
 			} else if (cmd.getName().equalsIgnoreCase("Map")) {
 				if(args.length == 2) {
 					Commands.Map(sender, args[0], args[1]);
 				} else {
 					sender.sendMessage(ChatColor.AQUA + "TRY AGAIN");
 				}
 			} else if (cmd.getName().equalsIgnoreCase("SetHome")) {
 				Commands.setHome(sender);
 			} else if (cmd.getName().equalsIgnoreCase("GoHome")) {
 				Commands.goHome(sender);
 			} else if (cmd.getName().equalsIgnoreCase("Timber")) {
 				if(args.length == 1) {
 					Commands.setTimber(sender, this, args[0]);
 				} else {
 					sender.sendMessage("Invalid number of arguments for this command.");
 				}
 			}
 			return true;
 		} else {
 			sender.sendMessage("Invalid command. Type \"/help\" for more information.");
 			return false;
 		}
 	}
 }
