 package me.furt.industrial;
 
 import java.util.logging.Level;
 
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class IndustrialInc extends JavaPlugin {
 	public CustomBlocks cb;
 	public CustomItems ci;
 	@Override
 	public void onEnable() {
		cb = new CustomBlocks(this);
		cb.init();
 		ci = new CustomItems(this);
 		ci.init();
 		PluginDescriptionFile pdf = this.getDescription();
 		this.getLogger().log(Level.INFO,
 				"v" + pdf.getVersion() + " is now enabled!");
 	}
 	
 	@Override
 	public void onDisable() {
 		this.getLogger().log(Level.INFO, "Disabled");
 	}
 
 }
