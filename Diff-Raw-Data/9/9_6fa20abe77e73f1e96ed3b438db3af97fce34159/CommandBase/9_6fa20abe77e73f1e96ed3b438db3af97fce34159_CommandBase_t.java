 package com.titankingdoms.nodinchan.titanchat.command;
 
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 
 import com.nodinchan.ncbukkit.loader.Loadable;
 import com.titankingdoms.nodinchan.titanchat.TitanChat;
 import com.titankingdoms.nodinchan.titanchat.TitanChat.MessageLevel;
 import com.titankingdoms.nodinchan.titanchat.addon.Addon;
 import com.titankingdoms.nodinchan.titanchat.channel.Channel;
 import com.titankingdoms.nodinchan.titanchat.util.Debugger;
 
 /*     Copyright (C) 2012  Nodin Chan <nodinchan@live.com>
  * 
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  * 
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  * 
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 public class CommandBase extends Loadable implements Listener {
 
 	protected final TitanChat plugin;
 	
 	protected static final Debugger db = new Debugger(3);
 	
 	protected static final MessageLevel INFO = MessageLevel.INFO;
 	protected static final MessageLevel NONE = MessageLevel.NONE;
 	protected static final MessageLevel PLUGIN = MessageLevel.PLUGIN;
 	protected static final MessageLevel WARNING = MessageLevel.WARNING;
 	
 	public CommandBase() {
 		super("");
 		this.plugin = TitanChat.getInstance();
 	}
 	
 	public final String getDisplayName(OfflinePlayer player) {
 		return plugin.getDisplayNameChanger().getDisplayName(player);
 	}
 	
 	public final boolean hasPermission(CommandSender sender, String permission) {
 		if (!(sender instanceof Player))
 			return true;
 		
 		return plugin.getPermissionsHandler().has((Player) sender, permission);
 	}
 	
 	public final void invalidArgLength(CommandSender sender, String name) {
 		plugin.send(MessageLevel.WARNING, sender, "Invalid Argument Length");
 		usage(sender, name);
 	}
 	
 	public final boolean isOffline(CommandSender sender, OfflinePlayer player) {
 		if (player == null || !player.isOnline())
 			plugin.send(MessageLevel.WARNING, sender, getDisplayName(player) + " is offline");
 		
		return player == null || !player.isOnline();
 	}
 	
 	public final void register(Addon addon) {
 		plugin.getManager().getAddonManager().register(addon);
 	}
 	
 	public final void register(Channel channel) {
 		plugin.getManager().getChannelManager().register(channel);
 	}
 	
 	public final void register(Listener listener) {
 		plugin.register(listener);
 	}
 	
 	public final void usage(CommandSender sender, String name) {
 		Executor executor = plugin.getManager().getCommandManager().getCommandExecutor(name);
 		
 		if (!executor.getUsage().equals(""))
 			plugin.send(MessageLevel.INFO, sender, "Usage: /titanchat <@><channel> " + executor.getUsage());
 	}
 }
