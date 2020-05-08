 package com.bukkit.erbros.Lottery;
 //All the imports
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Logger;
 
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.util.config.Configuration;
 
 
 
 public class Lottery extends JavaPlugin{
 
 	protected HashMap<String, Boolean> status;
 	protected LotteryPlayerListener playerListener;
 	protected Integer cost;
 	protected Integer hours;
 	protected Long nextexec;
 	protected Boolean timerStarted;
 	// Starting timer we are going to use for scheduling.
 	Timer timer;
 	
 	// Doing some logging. Thanks cyklo 
 	protected final Logger log;
 	
 	public Lottery() {
 		log = Logger.getLogger("Minecraft");
 		cost = 5;
 		hours = 24;
 		timerStarted = false;
 	}
 	
 	@Override
 	public void onDisable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " has been unloaded." );
 		//log.info(getDescription().getName() + ": has been disabled.");
 	}
 
 	@Override
 	public void onEnable() {
 		
 		// Gets version number and writes out starting line to console.
 		PluginDescriptionFile pdfFile = this.getDescription();
 		System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled" );
 		
 		// Start Registration. Thanks TheYeti.
 		getDataFolder().mkdirs();
 		
 		// Does config exist? If not, make it.
 		
 		Configuration c = getConfiguration();
 		
 		if(c.getProperty("cost") == null || c.getProperty("hours") == null) {
 			
 			c.setProperty("cost", 5);
 			c.setProperty("hours", 24);
 			
 	        if (!getConfiguration().save())
 	        {
 	            getServer().getLogger().warning("Unable to persist configuration files, changes will not be saved.");
 	        }
 		}
 		
 		String convert = c.getProperty("cost").toString();
 		cost = Integer.parseInt(convert);
 		convert = c.getProperty("hours").toString();
 		hours = Integer.parseInt(convert);
 		
 		// Listen for some player interaction perhaps? Thanks to cyklo :)
 		PluginManager pm = getServer().getPluginManager();
 		
 		pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Priority.Normal, this);
 		
 		// Is the date we are going to draw the lottery set? If not, we should do it.
 		if(c.getProperty("nextexec") == null) {
 			
 			// Set first time to be config hours later? Millisecs, * 1000.
 			nextexec = System.currentTimeMillis() + ExtendTime();
 			c.setProperty("nextexec", nextexec);
 			
 	        if (!getConfiguration().save())
 	        {
 	            getServer().getLogger().warning("Unable to persist configuration files, changes will not be saved.");
 	        }
 		} else {
 			nextexec = Long.parseLong(c.getProperty("nextexec").toString());
 		}
 		
 		// Start the timer for the first time.
 		StartTimerSchedule();
 		
 		// This could, and should, probably be fixed nicer, but for now it'll have to do.
 		// Adding timer that waits the time between nextexec and time now.
 		
 		
 		// Make clock that waits 24 hours?
 		
 		// Have the time we are waiting for come? 
 		// Calendar.getInstance().getTime().after(nextexec)
 		
 		// This shows the date in a human friendly way. 
 		// Date humandate = new Date(nextexec);
 		
 		
 	}
 
 	private long ExtendTime() {
 		Configuration c = getConfiguration();
 		hours = Integer.parseInt(c.getProperty("hours").toString());
 		Long extendTime = Long.parseLong(hours.toString()) * 5 * 1000;
 		return extendTime;
 	}
 
 	class LotteryDraw extends TimerTask {
 		public void run() {
 			// Cancel timer.
 			// Get new config.
 			Configuration c = getConfiguration();
 			nextexec = Long.parseLong(c.getProperty("nextexec").toString());
 			
 			if(nextexec > 0 && System.currentTimeMillis() > nextexec) {
 				// Did anyone buy tickets?
 				System.out.println("LOTTERY TIME!");
 				nextexec = System.currentTimeMillis() + ExtendTime();
 	
 				c.setProperty("nextexec",nextexec);
 				if (!getConfiguration().save())
 		        {
 		            getServer().getLogger().warning("Unable to persist configuration files, changes will not be saved.");
 		        }
 			}
 			// Call a new timer.
			//StartTimerSchedule();
 		}
 	}

 	private void StartTimerSchedule() {
 		
 		//Cancel any existing timers.
 		if(timerStarted == true) {
 			timer.cancel();
 			timer.purge();
 		}
 		// Start new timer.
		timer = new Timer();
		timer.schedule(new LotteryDraw(), ExtendTime());
 		// Timer is now started, let it know.
 		timerStarted = true;
 			
 	}
 	
 }
