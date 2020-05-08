 package com.github.kaitoyuuki.LastCall;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public final class LCMain extends JavaPlugin {
 
 	@Override
 	public void onEnable() {
 		getConfig();
 		getCommand("play").setExecutor(new LastCallCommands(this));
 		getCommand("lastcall").setExecutor(new LastCallCommands(this));
 		getCommand("lc").setExecutor(new LastCallCommands(this));
 	}
 	
 	@Override
 	public void onDisable() {
 		// TODO Insert logic to be performed when the plugin is disabled
 	}
 	
 }
