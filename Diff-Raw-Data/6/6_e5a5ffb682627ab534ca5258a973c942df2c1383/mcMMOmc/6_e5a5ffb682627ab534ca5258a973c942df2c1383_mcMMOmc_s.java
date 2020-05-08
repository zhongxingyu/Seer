 package org.mcmmo.mcmmomc;
 
 import java.io.IOException;
 import java.util.HashMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.mcmmo.mcmmomc.commands.TradeCommand;
 import org.mcstats.Metrics;
 
 public class mcMMOmc extends JavaPlugin {
 	// Player -> Channel
 	private HashMap<String, String> enabled = new HashMap<String, String>();
 
 	@Override
 	public void onEnable() {
 		PluginCommand tradeCommand = getCommand("tradechat");
 		tradeCommand.setPermission("mcmmomc.trade");
 		tradeCommand.setPermissionMessage(ChatColor.DARK_RED + "Insufficient permissions.");
 		tradeCommand.setExecutor(new TradeCommand(this));
 
 		PluginCommand miscCommand = getCommand("miscchat");
		tradeCommand.setPermission("mcmmomc.misc");
		tradeCommand.setPermissionMessage(ChatColor.DARK_RED + "Insufficient permissions.");
		tradeCommand.setExecutor(new MiscCommand(this));
 
 		metrics();
 
 		getLogger().info("Finished Loading " + getDescription().getFullName());
 	}
 
 	@Override
 	public void onDisable() {
 		getLogger().info("Finished Unloading " + getDescription().getFullName());
 	}
 
 	public boolean hasEnabled(String playerName, String channelName) {
 		return enabled.containsKey(playerName) && enabled.get(playerName) != null && enabled.get(playerName).equals(channelName);
 	}
 
 	public void enable(String playerName, String channelName) {
 		enabled.put(playerName, channelName);
 	}
 
 	public void disable(String playerName, String channelName) {
 		if(hasEnabled(playerName, channelName)) enabled.remove(playerName);
 	}
 
 	private void metrics() {
 		try {
 			Metrics metrics = new Metrics(this);
 			metrics.start();
 		} catch (IOException e) { }
 	}
 }
