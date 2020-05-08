 package org.theglicks.bukkit.BDchat;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.theglicks.bukkit.BDchat.commandListeners.CmdBDchatListener;
 import org.theglicks.bukkit.BDchat.commandListeners.CmdccListener;
 import org.theglicks.bukkit.BDchat.events.ChatListener;
 import org.theglicks.bukkit.BDchat.events.LoginListener;
 import org.theglicks.bukkit.BDchat.events.PlayerLeaveListener;
 
 public class BDchat extends JavaPlugin {
 
 	public static JavaPlugin thisPlugin;
 	public static ConfigAccessor channelConfig;
 	public static ConfigAccessor mainConfig;
 
 	public static List<String> helpMessage = new ArrayList<String>();
 	public static HashMap<String, BDchatPlayer> BDchatPlayerList = new HashMap<String, BDchatPlayer>();
 	public static HashMap<String, Channel> channelList = new HashMap<String, Channel>();
 
 	@Override
 	public void onEnable() {
 		thisPlugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin("BDchat");
 		channelConfig = new ConfigAccessor(thisPlugin, "channelConfig.yml");
 		mainConfig = new ConfigAccessor(thisPlugin, "mainConfig.yml");
 
 		channelConfig.saveDefaultConfig();
 		channelConfig.getConfig().options().copyDefaults(true);
 		mainConfig.saveDefaultConfig();
 		mainConfig.getConfig().options().copyDefaults(true);
 
 		getCommand("cc").setExecutor(new CmdccListener(this));
 		getCommand("BDchat").setExecutor(new CmdBDchatListener(this));
 
 		getServer().getPluginManager().registerEvents(new LoginListener(), this);
 		getServer().getPluginManager().registerEvents(new ChatListener(), this);
 		getServer().getPluginManager().registerEvents(new PlayerLeaveListener(), this);
 		
 		for (String line: mainConfig.getConfig().getConfigurationSection("helpMessage").getKeys(false)){
 			helpMessage.add(mainConfig.getConfig().getString("helpMessage." + line));
 		}
 
 		for (String channel : channelConfig.getConfig().getConfigurationSection("channels").getKeys(false)) {
 			Channel BDchannel = new Channel(channel);
 			channelList.put(channel, BDchannel);
 		}
		
		for (Player p: Bukkit.getOnlinePlayers()){
			BDchatPlayer BDplayer = new BDchatPlayer(p.getName());
			BDplayer.setChannel(mainConfig.getConfig().getString("defaultChannel"), true);
			BDchatPlayerList.put(p.getName(), BDplayer);
		}
 	}
 
 	@Override
 	public void onDisable() {
 
 	}
 }
