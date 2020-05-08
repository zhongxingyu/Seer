 package com.bukkit.jason.pitfall;
 
 import java.util.HashMap;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Road for Bukkit
  * 
  * @author <Your Name>
  */
 public class Pitfall extends JavaPlugin
 {
 	private final PitfallPlayerListener playerListener = new PitfallPlayerListener(this);
 	private final PitfallBlockListener blockListener = new PitfallBlockListener(this);
 	private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
	public final static String version="0.4";
     
 	public void onEnable()
 	{
		PitfallSettings.load();
 		
 		// Register our events
 		PluginManager pm = getServer().getPluginManager();
 		
 		
 		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.REDSTONE_CHANGE, blockListener, Priority.Normal, this);
 		// EXAMPLE: Custom code, here we just output some info so we can check
 		// all is well
		PluginDescriptionFile pdfFile = this.getDescription();
		System.out.println(pdfFile.getName() + " version " + Pitfall.version + " is enabled!");
 	}
 	
 	public void onDisable()
 	{
 		System.out.println("Goodbye world!");
 	}
 
 	public boolean isDebugging(final Player player)
 	{
 		if (debugees.containsKey(player))
 		{
 			return debugees.get(player);
 		}
 		else
 		{
 			return false;
 		}
 	}
 
 	public void setDebugging(final Player player, final boolean value)
 	{
 		debugees.put(player, value);
 	}
 }
