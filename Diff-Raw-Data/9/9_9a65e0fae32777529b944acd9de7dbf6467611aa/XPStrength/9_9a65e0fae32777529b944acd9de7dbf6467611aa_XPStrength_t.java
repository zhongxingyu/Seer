 package com.jamesmonger.XPStrength;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import com.jamesmonger.XPStrength.handlers.Commands;
 import com.jamesmonger.XPStrength.handlers.Events;
 import com.jamesmonger.XPStrength.player.AccountManager;
 import com.jamesmonger.XPStrength.util.Metrics;
 import com.jamesmonger.XPStrength.util.Settings;
 import com.jamesmonger.XPStrength.util.Languages;
 
 public class XPStrength extends JavaPlugin
 {
 	public static int levelCap;
 	public static boolean xpDrain;
 	
 	public static Map<String, Boolean> player_toggled = new HashMap<String, Boolean>();
 	public static AccountManager accountManager;
 
 	public void onEnable()
 	{
 		new Events(this);
 		accountManager = new AccountManager(this);
 		getCommand("xpbonus").setExecutor(new Commands(this));
 		try
 		{
 			File file;
 			file = new File("" + getDataFolder());
 			if (!file.exists())
 			{
 				file.mkdir();
 			}
 			file = new File("" + getDataFolder() + "/users/");
 			if (!file.exists())
 			{
 				file.mkdir();
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
		Settings.loadSettings(this);
 		Languages.loadLanguages(this);
 		
 		for(Player player : this.getServer().getOnlinePlayers())
 		{
 			XPStrength.accountManager.loadPlayer(player);
 		}
 		
 		try
 		{
 		    Metrics metrics = new Metrics(this);
 		    metrics.start();
 		}
 		catch (IOException e)
 		{
 		    getServer().getLogger().info("MCMetrics connection error occured.");
 		}
 	}
 
 	public void onDisable()
 	{
 		for(Player player : this.getServer().getOnlinePlayers())
 		{
 			XPStrength.accountManager.savePlayer(player);
 		}
 	}
 
 }
