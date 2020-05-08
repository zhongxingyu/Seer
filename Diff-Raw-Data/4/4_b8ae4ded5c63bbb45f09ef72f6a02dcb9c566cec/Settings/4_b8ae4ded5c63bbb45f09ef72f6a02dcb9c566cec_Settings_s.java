 package grokswell.hypermerchant;
 
 //import static java.lang.System.out;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.bukkit.configuration.file.YamlConfiguration;
 
 public class Settings {
 	private static File dataFolder;
 	static private HyperMerchantPlugin plugin;
     
     //Defaults
 	static Boolean ENABLE_COMMAND = true;
     static Boolean ENABLE_NPCS = true;
     static Boolean OFFDUTY = false;
     static String WELCOME = "Welcome to my little shop.";
     static String FAREWELL = "I thank you for your continued patronage.";
     static String DENIAL = "I'm afraid you are not a shop member. " +
     		"I am not authorized to do business with you.";
     static String CLOSED = "I am sorry, I am closed for business at this time.";
     static Boolean NPC_FOR_HIRE = true;
     static Double NPC_COMMISSION = 0.10;
     static Boolean RIGHT_CLICK_PLAYER_SHOP = true;
     static Boolean ONDUTY_IN_SHOP_ONLY = true;
     
     
     public Settings(HyperMerchantPlugin plgn) {
         plugin = plgn;
 		dataFolder = plugin.getDataFolder();
 		if (!dataFolder.isDirectory()) dataFolder.mkdir();
         loadConfig();
         saveConfig();
         //config = new YamlStorage(new File(plugin.getDataFolder() + File.separator + "config.yml"), "HyperMerchant Configuration");
     }
     	  
     private static void loadConfig() {
 		File configFile = null;
 		InputStream defConfigStream = null;
 		YamlConfiguration defConfig = null;
 		YamlConfiguration config = null;
 		
 		defConfigStream = plugin.getResource("config.yml");
     	configFile = new File(dataFolder, "config.yml");
 	    config = YamlConfiguration.loadConfiguration(configFile);
 	 
 		// Look for defaults in the jar
 	    if (defConfigStream != null) {
 	        defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
 	        defConfigStream = null;
 	    }
 	    if (defConfig != null) {
 	    	config.setDefaults(defConfig);
 	    }
 
         ENABLE_COMMAND = config.getBoolean("Main.enable-command");
         ENABLE_NPCS = config.getBoolean("Main.enable-npcs");
         OFFDUTY = config.getBoolean("Main.offduty");
         WELCOME = config.getString("Messages.welcome");
         FAREWELL = config.getString("Messages.farewell");
         DENIAL = config.getString("Messages.denial");
         CLOSED = config.getString("Messages.closed");
         NPC_FOR_HIRE = config.getBoolean("PlayerShops.npc-for-hire");
         NPC_COMMISSION = config.getDouble("PlayerShops.npc-commission");
         RIGHT_CLICK_PLAYER_SHOP = config.getBoolean("PlayerShops.right-click-player-shop");
         ONDUTY_IN_SHOP_ONLY = config.getBoolean("PlayerShops.onduty-in-shop-only");
     }
 	  
 	public static void saveConfig() {
 		File configFile = null;
 		YamlConfiguration config = null;
 		
 		configFile = new File(dataFolder, "config.yml");
 		config = YamlConfiguration.loadConfiguration(configFile);
 
 	    config.set("Main.enable-command", ENABLE_COMMAND);
	    config.set("Main.offduty", ENABLE_NPCS);
	    config.set("Messages.offduty", OFFDUTY);
 	    config.set("Messages.welcome", WELCOME);
 	    config.set("Messages.farewell", FAREWELL);
 	    config.set("Messages.denial", DENIAL);
 	    config.set("Messages.closed", CLOSED);
 	    config.set("PlayerShops.npc-for-hire", NPC_FOR_HIRE);
 	    config.set("PlayerShops.npc-commission", NPC_COMMISSION);
 	    config.set("PlayerShops.right-click-player-shop", RIGHT_CLICK_PLAYER_SHOP);
 	    config.set("PlayerShops.onduty-in-shop-only", ONDUTY_IN_SHOP_ONLY);
 	    
 		try {
 			config.save(configFile);
 		}
 		catch(IOException ex) {
 			plugin.getLogger().severe("Cannot save to config.yml");
 		}
 		}
 	
 //		FileConfiguration cleanConfig = new YamlConfiguration();
 //
 //		cleanConfig.set("Main.enable-command", "true");
 //		cleanConfig.set("Main.enable-npcs", "true");
 //		cleanConfig.set("Main.offduty", "false");
 //		cleanConfig.set("Messages.welcome", "Welcome to my little shop.");
 //		cleanConfig.set("Messages.farewell", "I thank you for your continued patronage.");
 //		cleanConfig.set("Messages.denial", ("I'm afraid you are not a shop member. " +
 //        		"I am not authorized to do business with you."));
 //		cleanConfig.set("Messages.closed", "I am sorry, I am closed for business at this time.");
 //		cleanConfig.set("PlayerShops.npc-for-hire", "true");
 //		cleanConfig.set("PlayerShops.npc-commission", "10");
 //		cleanConfig.set("PlayerShops.right-click-player-shop", "true");
 //		cleanConfig.set("PlayerShops.onduty-in-shop-only", "true");
 //		
 //
 //		try {
 //
 //			if (configFile.exists()) {
 //				cleanConfig.load(configFile);
 //			} else {
 //				cleanConfig.save(configFile);
 //			}
 //		}
 //		catch (InvalidConfigurationException e) {
 //			plugin.getLogger().severe("Invalid configuration found in config.yml");
 //		} 
 //    	catch(IOException ex) {
 //			plugin.getLogger().severe("Cannot save config.yml");
 //    	}
     
 }
