 package net.catharos.lib.plugin;
 
 import net.catharos.lib.cLib;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 public abstract class Plugin extends JavaPlugin {
 	
 	@Override
 	public boolean onCommand(CommandSender cs, Command cmnd, String label, String[] args) {
 		return cLib.getInstance().getCommandManager().onCommand(cs, cmnd, label, args);
 	}
 	
 }
