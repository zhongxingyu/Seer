 package me.lucariatias.plugins.itemenchanting;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ItemEnchanting extends JavaPlugin {
 	
 	public void onEnable() {
 		this.getLogger().info("ItemEnchanting has been enabled!");
 		if (this.getConfig().getBoolean("legacy.use-legacy-mode")) {
 			this.getServer().getPluginManager().registerEvents(new LegacyEnchantmentListener(this), this);
			this.getCommand("enchant").setExecutor(new LegacyEnchantmentCommand(this));
 		} else {
 			this.getServer().getPluginManager().registerEvents(new EnchantmentListener(this), this);
			this.getCommand("enchant").setExecutor(new EnchantmentCommand(this));
 		}
 		this.getConfig().options().copyDefaults(true);
 		this.saveConfig();
 	}
 	
 	public void onDisable(){
 		this.getLogger().info("ItemEnchanting has been disabled.");
 	}
 
 }
