 package stefan.blocklocator;
 
 import java.util.LinkedList;
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Main class of the plugin.
  * 
  * @author Stefan Bossbaly
  * 
  */
 public class BlockLocator extends JavaPlugin {
 
 	/**
 	 * The tag that will be displayed in the logs for this plugin. Helps the
 	 * user/server admin distinguish between logs of different plugins.
 	 */
 	public static final String TAG = "[BlockLocator]";
 
 	/**
 	 * Logger that will be used to output messages to the log.
 	 */
 	public static final Logger log = Logger.getLogger("Minecraft");
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void onDisable() {
 		log.info(TAG + " onDisable() called. Shutting down ...");
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void onEnable() {
 		log.info(TAG + " onEnabled() called. Starting up ...");
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 
 		if (command.getName().equalsIgnoreCase("bl")) {
 
 			// The console can not call this command
 			if (!(sender instanceof Player)) {
 				sender.sendMessage(TAG + " I can not get you location and therefore can't find blocks around you ...");
 				return true;
 			}
 
 			// Make sure that we have enough parameters
			if (args.length <= 1)
				return false;
 
 			int blockId;
 
 			// Try to parse the int
 			try {
 				blockId = Integer.parseInt(args[0]);
 			} catch (NumberFormatException e) {
 				sender.sendMessage(TAG + " This is not a valid integer!");
 				return true;
 			}
 
 			// Get all the fun stuff
 			Player player = (Player) sender;
 			Location loc = player.getLocation();
 			World world = player.getWorld();
 
 			// List that will hold the block that were found
 			LinkedList<Location> locs = new LinkedList<Location>();
 
 			// Log this command took place
 			log.info(TAG + "Player: " + player.getName() + " has called the locate command from location " + loc);
 
 			// TODO change the search radius
 			for (int x = loc.getBlockX() - 10; x < loc.getBlockX() + 10; x++) {
 				for (int y = loc.getBlockY() - 10; y < loc.getBlockY() + 10; y++) {
 					for (int z = loc.getBlockZ() - 10; z < loc.getBlockZ() + 10; z++) {
 						if (world.getBlockTypeIdAt(x, y, z) == blockId)
 							locs.add(world.getBlockAt(x, y, z).getLocation());
 					}
 				}
 			}
 
 			player.sendMessage(TAG + " Found " + locs.size() + " many with these locations");
 
 			return true;
 		}
 
 		return false;
 	}
 
 }
