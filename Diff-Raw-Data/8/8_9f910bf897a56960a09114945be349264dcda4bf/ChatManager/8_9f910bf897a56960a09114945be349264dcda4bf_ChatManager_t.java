 package com.caved_in.chatmanager;
 
 import com.caved_in.chatmanager.config.Configuration;
 import com.caved_in.chatmanager.events.ChannelJoinEvent;
 import com.caved_in.chatmanager.events.handler.ChannelEventHandler;
import com.caved_in.chatmanager.handlers.chat.channels.ChatChannel;
 import com.caved_in.chatmanager.handlers.player.PlayerHandler;
 import com.caved_in.chatmanager.commands.CommandRegister;
 import com.caved_in.chatmanager.handlers.chat.ChannelHandler;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.HandlerList;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.caved_in.chatmanager.listeners.BukkitListener;
 import com.caved_in.chatmanager.runnables.RunnableManager;
 import org.simpleframework.xml.Serializer;
 import org.simpleframework.xml.core.Persister;
 
 import java.io.File;
 import java.io.IOException;
 
 public class ChatManager extends JavaPlugin
 {
 	public static ChannelHandler channelHandler = new ChannelHandler();
 	public static final String GLOBAL_CHAT_CHANNEL = "GLOBAL";
 	public static RunnableManager runnableManager;
 	public static Configuration channelConfig;
 
 	@Override
 	public void onEnable()
 	{
 		//Load the configuration
 		loadConfig();
 		//Instance variables, commands, listeners, and handlers
 		runnableManager = new RunnableManager(this);
 		new BukkitListener(this);
 		//new ChatManagerListener(this);
 		new CommandRegister(this);
 
 		//Load all the data asynchronously for the currently online players
 		for (Player player : Bukkit.getOnlinePlayers())
 		{
 			final Player cPlayer = player;
 			final String playerName = cPlayer.getName();
 
 			//Add data for the player
 			PlayerHandler.addData(playerName);
 
 			//Asynchronous thread to call the channel join for each player
 			ChatManager.runnableManager.runTaskLaterAsynch(new Runnable()
 			{
 				@Override
 				public void run()
 				{
 					ChannelJoinEvent channelJoinEvent = new ChannelJoinEvent(ChatManager.channelHandler.getChannel(PlayerHandler.getData(playerName).getChatChannel()), cPlayer);
 					ChannelEventHandler.handleChannelJoinEvent(channelJoinEvent);
 				}
 			}, 40);
 		}
 	}
 
 	@Override
 	public void onDisable()
 	{
 		HandlerList.unregisterAll(this);
 		Bukkit.getServer().getScheduler().cancelTasks(this);
 	}
 
 	public void loadConfig()
 	{
 		try
 		{
 			Serializer serializer = new Persister();
 			File configFolder = new File("plugins/ChatManager");
 			if (!configFolder.exists())
 			{
 				//Make plugin directory if it doesn't already exist
 				configFolder.mkdir();
 			}
 
 			File configLocation = new File("plugins/ChatManager/Config.xml");
 			if (!configLocation.exists())
 			{
 				//Create channel config if it doesn't already exist
 				configLocation.createNewFile();
 				serializer.write(new Configuration(), configLocation);
 				Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Default ChatManager configuration has been created.");
 			}
 
 			//Load the channel configuration
 			channelConfig = serializer.read(Configuration.class,configLocation);

			for (ChatChannel chatChannel : channelConfig.getChatChannels())
			{
				channelHandler.addChannel(chatChannel);
			}

 			Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Loaded " + channelConfig.getXmlChatChannels().size() + " chat channels");
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 }
