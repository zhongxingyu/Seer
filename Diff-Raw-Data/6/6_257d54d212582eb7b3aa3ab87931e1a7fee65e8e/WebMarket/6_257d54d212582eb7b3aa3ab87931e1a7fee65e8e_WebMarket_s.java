 package com.mike724.webmarket;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import com.mike724.webmarket.util.Settings;
 import com.mike724.webmarket.util.VaultManager;
 
 public class WebMarket extends JavaPlugin {
 
 	public void onEnable() {
 		Log.setLogger(this.getLogger());
 		
 		//Get economy instance from Vault, disable if failed.
 		if(VaultManager.setupEconomy(this)) {
 			Log.log.info("Setup economy correctly");
 		} else {
 			Log.log.severe("Could not setup economy!");
             getServer().getPluginManager().disablePlugin(this);
             return;
 		}
 		
 		//Set settings
 		Settings.setSK(this.getConfig().getString("secret-key"));
 		Settings.setPort(this.getConfig().getInt("http-port"));
 		
 		//Run player info HTTP server
 		try {
 			PlayerHttpServer phs = new PlayerHttpServer();
 			phs.start();
 		} catch (Exception e) {
 			Log.log.severe("COULD NOT SETUP HTTP SERVER, IS THE PORT OPEN?");
             getServer().getPluginManager().disablePlugin(this);
             return;
 		}
 		Log.log.info("Enabled sucessfully");
 	}
 	@Override
 	public void onDisable() {
 		Log.log.info("Disabled");
 	}
 }
