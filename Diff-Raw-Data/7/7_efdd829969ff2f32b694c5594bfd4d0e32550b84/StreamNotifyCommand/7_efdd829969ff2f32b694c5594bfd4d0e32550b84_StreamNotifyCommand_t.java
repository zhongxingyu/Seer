 package com.chasechocolate.streamnotify.cmds;
 
 import java.util.ArrayList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 import com.chasechocolate.streamnotify.StreamNotify;
 import com.chasechocolate.streamnotify.stream.TwitchStream;
 
 public class StreamNotifyCommand implements CommandExecutor {
 	private StreamNotify plugin;
 
 	public StreamNotifyCommand(StreamNotify plugin) {
 		this.plugin = plugin;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label,
 			String[] args) {
 		if (cmd.getName().equalsIgnoreCase("streamnotify")) {
 			if (args.length > 0) {
 				for (TwitchStream stream : plugin.streams)
 					if (stream.getChannel().equalsIgnoreCase(args[1])) {
 						if (stream.isOnline()) {
 							String msg = plugin.getConfig().getString(
 									"playercheck.online");
 							msg = ChatColor.translateAlternateColorCodes('&',
 									msg);
 							if (msg.contains("%channel%"))
 								msg = msg.replace("%channel%",
 										stream.getChannel());
 							if (msg.contains("%url%"))
 								msg = msg.replace("%url%", stream.getChannel());
 							sender.sendMessage(msg);
 						} else {
 							String msg = plugin.getConfig().getString(
 									"playercheck.offline");
 							msg = ChatColor.translateAlternateColorCodes('&',
 									msg);
 							if (msg.contains("%channel%"))
 								msg = msg.replace("%channel%",
 										stream.getChannel());
 							if (msg.contains("%url%"))
 								msg = msg.replace("%url%", stream.getChannel());
 							sender.sendMessage(msg);
 						}
 					} else {
 						String msg = plugin.getConfig().getString(
 								"playercheck.notlisted");
 						msg = ChatColor.translateAlternateColorCodes('&', msg);
 						if (msg.contains("%channel%"))
 							msg = msg.replace("%channel%", stream.getChannel());
 						sender.sendMessage(msg);
 					}
 			} else {
 				ArrayList<String> online = new ArrayList<String>();
 				for (TwitchStream stream : plugin.streams)
 					if (stream.isOnline())
 						online.add(stream.getDisplayUrl());
 				if (!online.isEmpty()) {
 					String msg = plugin.getConfig().getString(
 							"broadcast.message-start");
 					msg = ChatColor.translateAlternateColorCodes('&', msg);
 					String color = plugin.getConfig().getString(
 							"broadcast.urlcolor");
 					color = ChatColor.translateAlternateColorCodes('&', color);
 					sender.sendMessage(msg);
 					for (String url : online)
						sender.sendMessage(color + " - " + url);
				} else {
					String msg = plugin.getConfig().getString(
							"playercheck.alloffline");
					msg = ChatColor.translateAlternateColorCodes('&', msg);
					sender.sendMessage(msg);
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 }
