 package com.censoredsoftware.Demigods;
 
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.Utility.DataUtility;
 import com.censoredsoftware.Demigods.Engine.Utility.SchedulerUtility;
 import com.censoredsoftware.Demigods.Episodes.Demo.EpisodeDemo;
 
 /**
  * Class for all plugins of Demigods.
  */
 public class DemigodsPlugin extends JavaPlugin
 {
 	/**
 	 * The Bukkit enable method.
 	 */
 	@Override
 	public void onEnable()
 	{
 		// Load the game engine, passing in the game data.
 		new Demigods(this, EpisodeDemo.Deities.values(), EpisodeDemo.Tasks.values(), EpisodeDemo.Structures.values());
 
 		// Start game threads.
 		SchedulerUtility.startThreads(this);
 
 		Demigods.message.info("Successfully enabled.");
 	}
 
 	/**
 	 * The Bukkit disable method.
 	 */
 	@Override
 	public void onDisable()
 	{
 		// Save all the data.
 		DataUtility.save();
 
 		// Cancel all threads, callAbilityEvent calls, and connections.
 		SchedulerUtility.stopThreads(this);
 		HandlerList.unregisterAll(this);
 		DataUtility.disconnect();
 
 		Demigods.message.info("Successfully disabled.");
 	}
 }
