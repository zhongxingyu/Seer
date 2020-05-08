 package com.bukkit.Innectis.Metatool;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.entity.Player;
 import org.bukkit.Server;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
 
import org.bukkit.material.MaterialData;
 import org.bukkit.plugin.*;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.command.*;
 
 /**
  * Innectis Metatool plugin class
  *
  * Features:
  *   None yet! All stuff is coming soon.
  *
  * @author Innectis
  */
 public class MetatoolMain extends JavaPlugin
 {
 	public static MetatoolMain main;
 
 	private final static Logger logger = Logger.getLogger("Minecraft");
 	private final HashMap<Player, Integer> metaSet = new HashMap<Player, Integer>();
 	private final BlockListener blockListener = new BlockListener();
 
 	public MetatoolMain(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader)
 	{
 		super(pluginLoader, instance, desc, folder, plugin, cLoader);
 		main = this;
 	}
 
 	public void onEnable()
 	{
 		// Register events
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.BLOCK_RIGHTCLICKED, blockListener, Priority.High, this);
 
 		// Say hi
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log(pdfFile.getName() + " v" + pdfFile.getVersion() + " enabled");
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
 	{
 		if (!sender.isOp() || !(sender instanceof Player)) {
 			return false;
 		}
 		Player player = (Player) sender;
 
 		if (args.length == 0) {
 			return false;
		} else if (args[0] == "stop") {
 			setStatus(player, -1);
 			player.sendMessage("7No longer setting");
 			log("[MTOOL] SpaceManiac stopped setting");
 			return true;
 		} else {
 			byte b = 0;
 			try {
 				b = Byte.parseByte(args[0]);
 			} catch(java.lang.NumberFormatException e) {
 				player.sendMessage("7Must provide a byte!");
 				return false;
 			}
 			
 			player.sendMessage("7Right-click block to set, use '/meta stop' to stop");
 			log("[MTOOL] SpaceManiac issued command: /meta " + b);
 			setStatus(player, b);
 			return true;
 		}
		return true;
 	}
 
 	public void onDisable()
 	{
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log(pdfFile.getName() + " disabled");
 	}
 
 	public int getStatus(Player player)
 	{
 		if (metaSet.containsKey(player)) {
 			return (int) metaSet.get(player);
 		} else {
 			return -1;
 		}
 	}
 
 	public void setStatus(Player player, int status)
 	{
 		metaSet.put(player, status);
 	}
 
 	public void log(String text)
 	{
 		logger.log(Level.INFO, text);
 	}
 }
