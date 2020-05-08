 package com.oneofthesevenbillion.ziah.ZCord;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Level;
 
 import com.oneofthesevenbillion.ziah.ZCord.utils.ArrayUtils;
 import com.oneofthesevenbillion.ziah.ZCord.utils.ColorUtils;
 import com.oneofthesevenbillion.ziah.ZCord.utils.Utils;
 
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.CommandSender;
 import net.md_5.bungee.api.ProxyServer;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.plugin.Command;
 
 public class CommandPM extends Command {
 	public CommandPM() {
 		super("privatemessage", "zcord.command.privatemessage", "pm");
 	}
 
 	@Override
 	public void execute(CommandSender sender, String[] args) {
 		if (args.length >= 2) {
 			List<ProxiedPlayer> candidates = Utils.getPlayersFromInputName(args[0]);
			if (candidates.size() > 0) {
 				sender.sendMessage(ChatColor.RED + "Too many players" + ChatColor.GRAY + ":");
 				for (ProxiedPlayer player : candidates) {
 					sender.sendMessage(player.getName());
 				}
 			}else{
 	            ProxiedPlayer client = candidates.get(0);
 	            String message = ColorUtils.fakeMCtoRealMCColor(ArrayUtils.join(Arrays.copyOfRange(args, 1, args.length), " ").trim());
 	            if (client != null) {
 	            	sender.sendMessage(ChatColor.GOLD + "You " + ChatColor.GRAY + ">" + ChatColor.GOLD + " " + client.getName() + ChatColor.GRAY + ": " + ChatColor.RESET + message);
 	            	client.sendMessage(ChatColor.GOLD + sender.getName() + " " + ChatColor.GRAY + ">" + ChatColor.GOLD + " You" + ChatColor.GRAY + ": " + ChatColor.RESET + message);
 	            	System.out.println(ColorUtils.removeAllColors(ChatColor.GOLD + sender.getName() + " " + ChatColor.GRAY + ">" + ChatColor.GOLD + " " + client.getName() + ChatColor.GRAY + ": " + ChatColor.RESET + message));
 	            	ProxyServer.getInstance().getLogger().log(Level.INFO, ColorUtils.removeAllColors(ChatColor.GOLD + sender.getName() + " " + ChatColor.GRAY + ">" + ChatColor.GOLD + " " + client.getName() + ChatColor.GRAY + ": " + ChatColor.RESET + message));
 	            }else
 	            if (args[0].equalsIgnoreCase("console")) {
 	            	sender.sendMessage(ChatColor.GOLD + "You " + ChatColor.GRAY + ">" + ChatColor.GOLD + " Console" + ChatColor.GRAY + ": " + ChatColor.RESET + message);
 	            	System.out.println(ColorUtils.removeAllColors(ChatColor.GOLD + sender.getName() + " " + ChatColor.GRAY + ">" + ChatColor.GOLD + " You" + ChatColor.GRAY + ": " + ChatColor.RESET + message));
 	            	ProxyServer.getInstance().getLogger().log(Level.INFO, ColorUtils.removeAllColors(ChatColor.GOLD + sender.getName() + " " + ChatColor.GRAY + ">" + ChatColor.GOLD + " Console" + ChatColor.GRAY + ": " + ChatColor.RESET + message));
 	            }else{
 	                sender.sendMessage(ChatColor.RED + "Player not found!");
 	            }
 			}
 		}else{
 			sender.sendMessage(ChatColor.RED + "Invalid arguments!");
 		}
 	}
 }
