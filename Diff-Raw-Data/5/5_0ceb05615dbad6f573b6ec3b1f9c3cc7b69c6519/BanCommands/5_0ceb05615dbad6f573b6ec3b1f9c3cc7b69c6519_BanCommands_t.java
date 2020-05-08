 package eu.icecraft.iceban.commands;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import eu.icecraft.iceban.BanInfo;
 import eu.icecraft.iceban.BanInfo.BanType;
 import eu.icecraft.iceban.IceBan;
 import eu.icecraft.iceban.Utils;
 
 public class BanCommands implements CommandExecutor {
 	public IceBan plugin;
 	public Pattern timePattern;
 
 	public BanCommands(IceBan iceBan) {
 		this.plugin = iceBan;
 		timePattern = Pattern.compile("^(.+?) (.+?) \\-t (.+?)$");
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String cmdLbl, String[] args) {
 		if(!sender.hasPermission("iceban." + cmdLbl)) return false;
 
 		if(cmdLbl.equals("ban") || cmdLbl.equals("sbh")) {
 			boolean forceBan = false;
 			boolean permanent = false;
 			boolean silent = false;
 
 			String nick = "";
 			String reason = "";
 			String time = "";
 
 			String allArgs = "";
 			for (String word : args) {
 				if(word.equals("-f")) { forceBan = true; continue; }
 				if(word.equals("-p")) { permanent = true; continue; }
 				if(word.equals("-s")) { silent = true; continue; }
 				allArgs = allArgs + " " + word;
 			}
 
 			Matcher m = timePattern.matcher(allArgs);
 			if (m.find() && !permanent) {
 				nick = m.group(1).trim(); // nick
 				reason = m.group(2).trim(); // reason
 				time = m.group(3).trim(); // time
 			} else {
 				nick = args[0];
 				int i = 0;
 				for (String word : args) {
 					i++;
 					if(i == 1 || word.equals("-f") || word.equals("-p") || word.equals("-s")) continue;
 					reason = reason + " " + word;
 				}
 				reason =  reason.trim();
 
 				if(!sender.hasPermission("iceban.longbans")) time = "1d";
 				else time = "30d";
 			}
 
 			int banTime;
 			if(sender.hasPermission("iceban.permabans") && permanent) banTime = 0;
 			else {
 				banTime = Utils.parseTimeSpec(time.trim()) * 60;
 				if(banTime < 0) {
 					sender.sendMessage(ChatColor.RED + "Invalid time string!");
 					return true;
 				}
 
 				if(!sender.hasPermission("iceban.longbans") && banTime > 86400) {
					sender.sendMessage(ChatColor.RED + "Ban length was above your limit, setting to 1 day");
 					banTime = 86400;
 				}
 
 				banTime += (int) (System.currentTimeMillis() / 1000L); 
 			}
 
 			if(reason.equals("") || reason.startsWith("-t") || reason.startsWith("-s") || reason.startsWith("-f") || reason.startsWith("-p")) {
 				sender.sendMessage(ChatColor.RED + "Invalid reason.");
 				return true;
 			}
 
 			String ip = plugin.iceAuth.getIP(nick);
 			if(ip == null && !forceBan) {
 				sender.sendMessage(ChatColor.RED + "Player IP not found in history! Use -f to override");
 				return true;
 			}
 
 			BanInfo oldBan = plugin.getNameBan(nick.toLowerCase());
 			if(oldBan.getBanType() != BanType.NOT_BANNED) {
 				sender.sendMessage(ChatColor.RED + "Player has previous active bans, marking them as past.");
 				plugin.unban(oldBan);
 			}
 
 			plugin.sql.ban(nick, ip, banTime, reason, sender.getName());
 			BanInfo newBan = plugin.getNameBan(nick.toLowerCase());
 
 			for(Player currPlayer : Bukkit.getServer().getOnlinePlayers()) {
 				if(currPlayer.getName().equalsIgnoreCase(nick)) currPlayer.kickPlayer(plugin.getKickMessage(newBan));
 
				if(!silent || !currPlayer.isOp()) currPlayer.sendMessage(ChatColor.RED + "IceBan: " + ChatColor.AQUA + nick + " was banned by " + sender.getName());
 			}
 
 			System.out.println(sender.getName() + " banned " + nick + " with reason " + reason + " for " + (permanent ? "a long time" : time) + " ban id: " + newBan.getBanID());
 			sender.sendMessage(ChatColor.GREEN + "Banned " + nick + " with reason " + reason + " for " + (permanent ? "a long time" : time) + " ban id: " + newBan.getBanID());
 
 			return true;
 		}
 
 		if(cmdLbl.equals("unban") || cmdLbl.equals("sunban")) {
 			BanInfo ban = plugin.getNameBan(args[0]);
 			if(ban.isNameBanned()) {
 				plugin.unban(ban);
 				sender.sendMessage(ChatColor.GOLD + "Player " + ChatColor.GRAY + args[0] + ChatColor.GOLD + " unbanned sucessfully.");
 			} else {
 				sender.sendMessage(ChatColor.GOLD + "Player " + ChatColor.GRAY + args[0] + ChatColor.GOLD + " isn't banned.");
 			}
 			return true;
 		}
 
 		return false;
 	}
 
 }
