 /**
  * TeamSparkle - a bukkit plugin
  * Copyright (C) 2013 dmulloy2
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package net.dmulloy2.teamsparkle;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.MissingResourceException;
 import java.util.logging.Level;
 
 import lombok.Getter;
 import net.dmulloy2.teamsparkle.commands.CmdBuy;
 import net.dmulloy2.teamsparkle.commands.CmdGiveTokens;
 import net.dmulloy2.teamsparkle.commands.CmdHelp;
 import net.dmulloy2.teamsparkle.commands.CmdInvite;
 import net.dmulloy2.teamsparkle.commands.CmdLeaderboard;
 import net.dmulloy2.teamsparkle.commands.CmdReload;
 import net.dmulloy2.teamsparkle.commands.CmdShop;
 import net.dmulloy2.teamsparkle.commands.CmdStats;
 import net.dmulloy2.teamsparkle.handlers.CommandHandler;
 import net.dmulloy2.teamsparkle.handlers.LogHandler;
 import net.dmulloy2.teamsparkle.handlers.PermissionHandler;
 import net.dmulloy2.teamsparkle.handlers.ResourceHandler;
 import net.dmulloy2.teamsparkle.handlers.ShopHandler;
 import net.dmulloy2.teamsparkle.io.PlayerDataCache;
 import net.dmulloy2.teamsparkle.listeners.PlayerListener;
 import net.dmulloy2.teamsparkle.types.PlayerData;
 import net.dmulloy2.teamsparkle.util.FormatUtil;
 import net.dmulloy2.teamsparkle.util.Util;
 
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitRunnable;
 
 /**
  * @author dmulloy2
  */
 
 public class TeamSparkle extends JavaPlugin
 {
 	/** Handlers **/
 	private @Getter PermissionHandler permissionHandler;
 	private @Getter CommandHandler commandHandler;
 	private @Getter ResourceHandler resourceHandler;
 	private @Getter ShopHandler shopHandler;
 	private @Getter LogHandler logHandler;
 	
 	/** Data Cache **/
 	private @Getter PlayerDataCache playerDataCache;
 
 	/** Sparkled Player HashMap. Format: Sparkler, Sparkled **/
 	private @Getter HashMap<String, String> sparkled = new HashMap<String, String>();
 
 	/** Global Prefix **/
 	private @Getter String prefix = FormatUtil.format("&6[&4&lTS&6] ");
 
 	@Override
 	public void onEnable()
 	{
 		long start = System.currentTimeMillis();
 
		logHandler = new LogHandler(this);

 		/** Configuration **/
 		saveDefaultConfig();
 		reloadConfig();
 
 		/** Register Handlers **/
 		saveResource("messages.properties", true);
 		resourceHandler = new ResourceHandler(this, getClassLoader());
 
 		permissionHandler = new PermissionHandler(this);
 		commandHandler = new CommandHandler(this);
 		shopHandler = new ShopHandler(this);
 
 		playerDataCache = new PlayerDataCache(this);
 
 		/** Register Commands **/
 		commandHandler.setCommandPrefix("ts");
 		commandHandler.registerPrefixedCommand(new CmdBuy(this));
 		commandHandler.registerPrefixedCommand(new CmdGiveTokens(this));
 		commandHandler.registerPrefixedCommand(new CmdHelp(this));
 		commandHandler.registerPrefixedCommand(new CmdInvite(this));
 		commandHandler.registerPrefixedCommand(new CmdLeaderboard(this));
 		commandHandler.registerPrefixedCommand(new CmdReload(this));
 		commandHandler.registerPrefixedCommand(new CmdShop(this));
 		commandHandler.registerPrefixedCommand(new CmdStats(this));
 
 		/** Register Listener **/
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(new PlayerListener(this), this);
 
 		/** Deploy Auto Save task **/
 		if (getConfig().getBoolean("autoSave.enabled"))
 		{
 			int interval = 20 * 60 * getConfig().getInt("autoSave.interval");
 			
 			new BukkitRunnable()
 			{
 				@Override
 				public void run()
 				{
 					playerDataCache.save();
 				}
 			}.runTaskTimerAsynchronously(this, interval, interval);
 		}
 
 		/** Hourly Rewards **/ // TODO: Move this to async?
 		if (! getConfig().getStringList("hourlyRewards").isEmpty())
 			new HourlyRewardTask().runTaskTimer(this, 72000L, 72000L);
 
 		long finish = System.currentTimeMillis();
 
 		outConsole(getMessage("log_enabled"), getDescription().getFullName(), finish - start);
 	}
 
 	@Override
 	public void onDisable()
 	{
 		long start = System.currentTimeMillis();
 
 		/** Save Data **/
 		playerDataCache.save();
 
 		/** Clear Memory **/
 		sparkled.clear();
 		shopHandler.onDisable();
 
 		/** Cancel tasks / services **/
 		getServer().getServicesManager().unregisterAll(this);
 		getServer().getScheduler().cancelTasks(this);
 
 		long finish = System.currentTimeMillis();
 
 		outConsole(getMessage("log_disabled"), getDescription().getFullName(), finish - start);
 	}
 
 	/** Console logging **/
 	public void outConsole(String string, Object... objects)
 	{
 		logHandler.log(string, objects);
 	}
 
 	public void outConsole(Level level, String string, Object... objects)
 	{
 		logHandler.log(level, string, objects);
 	}
 
 	public void debug(String string, Object... objects)
 	{
 		logHandler.debug(string, objects);
 	}
 
 	/** Get messages **/
 	public String getMessage(String string)
 	{
 		try
 		{
 			return resourceHandler.getMessages().getString(string);
 		}
 		catch (MissingResourceException ex)
 		{
 			outConsole(Level.WARNING, getMessage("log_message_null"), string); // messageception :3
 			return null;
 		}
 	}
 
 	public boolean isSparkled(Player sparkledPlayer)
 	{
 		for (Entry<String, String> entry : sparkled.entrySet())
 		{
 			if (entry.getValue().equalsIgnoreCase(sparkledPlayer.getName()))
 				return true;
 		}
 
 		return false;
 	}
 
 	public String getSparkler(Player sparkledPlayer)
 	{
 		for (Entry<String, String> entry : sparkled.entrySet())
 		{
 			if (entry.getValue().equalsIgnoreCase(sparkledPlayer.getName()))
 				return entry.getKey();
 		}
 
 		return null;
 	}
 
 	public void rewardSparkledPlayer(Player sparkledPlayer)
 	{
 		String sparkler = getSparkler(sparkledPlayer);
 		if (sparkler != null)
 		{
 			List<String> commands = getConfig().getStringList("sparkledRewards");
 			if (! commands.isEmpty())
 			{
 				for (String command : commands)
 				{
 					getServer().dispatchCommand(getServer().getConsoleSender(), command.replaceAll("%p", sparkledPlayer.getName()));
 				}
 
 				sparkledPlayer.sendMessage(FormatUtil.format(getMessage("sparkled_welcome"), sparkledPlayer.getName()));
 			}
 			else
 			{
 				outConsole(Level.WARNING, "Could not reward new player {0}: Rewards list is empty!", sparkledPlayer.getName());
 			}
 
 			OfflinePlayer sparklerp = Util.matchOfflinePlayer(sparkler);
 
 			PlayerData data = playerDataCache.getData(sparkler);
 			data.setTokens(data.getTokens() + 1);
 			data.setTotalSparkles(data.getTotalSparkles() + 1);
 
 			if (sparklerp.isOnline())
 			{
 				((Player) sparklerp).sendMessage(FormatUtil.format(getMessage("sparkler_thanks"), sparkledPlayer.getName()));
 			}
 
 			sparkled.remove(sparkler);
 		}
 	}
 
 	/** Gives a player an item **/
 	@SuppressWarnings("deprecation")
 	public void giveItem(Player player, ItemStack stack)
 	{
 		player.getInventory().addItem(stack);
 		player.updateInventory();
 	}
 
 	/** Hourly Reward Task **/
 	public class HourlyRewardTask extends BukkitRunnable
 	{
 		@Override
 		public void run()
 		{
 			for (Player player : getServer().getOnlinePlayers())
 			{
 				List<String> rewards = getConfig().getStringList("hourlyRewards");
 				if (! rewards.isEmpty())
 				{
 					int rand = Util.random(rewards.size());
 					String entry = rewards.get(rand);
 					if (entry != null)
 					{
 						String command = entry.split(";")[0].replaceAll("%p", player.getName());
 						getServer().dispatchCommand(getServer().getConsoleSender(), command);
 
 						player.sendMessage(FormatUtil.format(getMessage("hourly_reward"), getConfig().getString("serverName"),
 								entry.split(";")[1]));
 					}
 				}
 			}
 		}
 	}
 }
