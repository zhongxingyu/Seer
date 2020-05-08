 package net.catharos.lib.plugin;
 
 import net.catharos.lib.cLib;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 public abstract class Plugin extends JavaPlugin {
	private static Plugin instance;
	
	
	@Override
	public void onLoad() {
		Plugin.instance = this;
	}
	
	/**
	 * Returns the plugin instance.
	 * 
	 * @return The current plugin instance
	 */
	public static Plugin getPlugin() {
		return Plugin.instance;
	}
 	
 	@Override
 	public boolean onCommand(CommandSender cs, Command cmnd, String label, String[] args) {
 		return cLib.getInstance().getCommandManager().onCommand(cs, cmnd, label, args);
 	}
 	
 }
