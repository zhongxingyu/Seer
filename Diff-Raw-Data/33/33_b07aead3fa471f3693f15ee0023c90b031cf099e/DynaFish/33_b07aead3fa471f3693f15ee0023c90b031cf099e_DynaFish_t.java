 package me.menexia.dynafish;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.block.Block;
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
 	//Defined entity listener
 	public final HashMap<Player, ArrayList<Block>> DFUsers = new HashMap();
 	//Creating the HashMap
 	private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
 	//Create HashMap Debugee
 
 @Override
 public void onDisable() {
 	this.logger.info("DynaFish disabled!");
 }
 
 @Override
 public void onEnable() {
 PluginManager pm = this.getServer().getPluginManager();
 pm.registerEvent(Event.Type.ENTITY_EXPLODE, this.entityListener, Event.Priority.Normal, this);
 PluginDescriptionFile pdf = this.getDescription();
 this.logger.info( pdf.getName() + " version " + pdf.getVersion() + " is enabled!" );
 }
 // End of onEnable method.
 
 public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (commandLabel.equalsIgnoreCase("dynafish")
 			|| commandLabel.equalsIgnoreCase("df"))
 		{
 		toggleVision((Player) sender);
 		}
	return true;
 		}
 // End of onCommand method.
 
 public boolean isDebugging(final Player player) {
 	if (debugees.containsKey(player)) {
 		return debugees.get(player);
 	} else {
 		return false;
 	}
 }
 // End of debugging method.
 
 public void setDebuging(final Player player, final boolean value) {
 	debugees.put(player, value);
 }
 // The method enabled which checks to see if the player is in the hashmap.
 public boolean enabled(Player player) {
 	return this.DFUsers.containsKey(player);
 	}
	// Check if it has player within key.
	
 	public void toggleVision(Player player) {
 		if (enabled(player)) {
 			this.DFUsers.remove(player);
 			player.sendMessage("DynaFish disabled!");
 		} else {
 			this.DFUsers.put(player, null);
 			player.sendMessage("DynaFish enabled!");
 		}
 	}
	// End of toggleVision method	
 }
