 package net.worldoftomorrow.nala.ni;
 
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class NoItem extends JavaPlugin {
 
     protected Config config;
     protected Vault vault;
     private static NoItem plugin;
 
     public void onEnable() {
 	plugin = this;
 	this.config = new Config(this);
 	this.vault = new Vault(this);
 	CommandListener cl = new CommandListener(this);
 	this.getCommand("noitem").setExecutor(cl);
 
 	Updater updater = new Updater(this.getDescription().getVersion());
 	if (!updater.isLatest()) {
 	    Log.info("--------------------No Item--------------------");
 	    Log.info("There is a new version ( " + updater.getLatest()
 		    + " ) available.");
 	    Log.info("Download it at: " + this.getDescription().getWebsite());
 	    if (Config.pluginChannel().equalsIgnoreCase("dev")) {
 		Log.info("You are using the development version channel!");
 	    }
 	    Log.info("-----------------------------------------------");
 	}
 
 	PluginManager pm = getServer().getPluginManager();
 
 	boolean craftListen = Config.stopCrafting();
 	boolean pickupListen = Config.stopItemPickup();
 	boolean brewListen = Config.stopPotionBrew();
 	boolean toolListen = Config.stopToolUse();
 	boolean holdListen = Config.stopItemHold();
 	boolean wearListen = Config.stopArmourWear();
 	boolean cookListen = Config.stopItemCook();
 	boolean dropListen = Config.stopItemDrop();
 
 	if (craftListen) {
 	    pm.registerEvents(new CraftingListener(), this);
 	}
 	if (pickupListen) {
 	    pm.registerEvents(new PickupListener(), this);
 	}
 	if (brewListen) {
 	    pm.registerEvents(new BrewingListener(), this);
 	}
 	if (toolListen) {
 	    pm.registerEvents(new NoUseListener(), this);
 	}
 	if (holdListen) {
 	    pm.registerEvents(new HoldListener(), this);
 	}
 	if (wearListen) {
 	    pm.registerEvents(new ArmorListener(), this);
 	}
 	if (cookListen) {
 	    pm.registerEvents(new FurnaceListener(), this);
 	}
 	if (dropListen) {
 	    pm.registerEvents(new DropListener(), this);
 	}
	Log.info("[NoItem] Configs loaded, events registered, and cake baked.");
 
 	if ((!craftListen) && (!pickupListen) && (!brewListen) && (!toolListen)
 		&& (!holdListen) && (!wearListen) && (!cookListen)) {
	    Log.info("[NoItem] No listeners active! Shutting down plugin.");
 	    getPluginLoader().disablePlugin(this);
 	}
     }
 
     public void onDisable() {
	Log.info("[NoItem] Configs unloaded, events unregisterededed, and cake eaten.");
     }
 
     public static NoItem getPlugin() {
 	return plugin;
     }
 
     public Vault getVault() {
 	return vault;
     }
 }
