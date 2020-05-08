 package dev.xyzcraft.net.bkbw;
 
 import net.md_5.bungee.BungeeCord;
 import net.md_5.bungee.ChatColor;
 import net.md_5.bungee.Permission;
 import net.md_5.bungee.UserConnection;
 import net.md_5.bungee.command.CommandSender;
 import net.md_5.bungee.plugin.JavaPlugin;
 
 public class CommandKick extends MacCommand{
 
 	public CommandKick(JavaPlugin pl) {
 		super(pl);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void execute(CommandSender arg0, String[] arg1) {
 		// TODO Auto-generated method stub
 		if (getPermission(arg0) == Permission.ADMIN || getPermission(arg0) == Permission.MODERATOR) {
 			if (arg1.length < 1) {
 				arg0.sendMessage(StringFormatter.error("You need to specify a player to kick."));
 				return;
 			}
 			String partialPlayer = arg1[0];
 			String fullPlayer = null;
 			for (String u : BungeeCord.instance.connections.keySet()) {
				if (u.startsWith(partialPlayer)) {
 					fullPlayer = u;
 					break;
 				}
 			}
 			if (fullPlayer == null) {
 				arg0.sendMessage(StringFormatter.error("Player does not exist, or is not online"));
 				return;
 			}
 			if (fullPlayer.equals(arg0.getName())) {
 				arg0.sendMessage(StringFormatter.error("You can't kick yourself!"));
 				return;
 			}
 			String reason = "Kicked: ";
 			if (arg1.length > 1) {
 				boolean first = true;
 				for (String partArg : arg1) {
 					if (first) {
 						first = false;
 						continue;
 					}
 					reason = reason + partArg + " ";
 				}
 				reason = reason.substring(0, reason.length()-1);
 			}
 			reason = ChatColor.RED + ((arg1.length > 1) ? reason : reason + "You have been kicked!");
 			PlayerKicker.kickPlayer(fullPlayer, reason);
 			for (UserConnection uc : BungeeCord.instance.connections.values()) {
 				if (BungeeCord.instance.config.admins.contains(uc.getName()) || BungeeCord.instance.config.moderators.contains(uc.getName())) {
 					uc.sendMessage(ChatColor.RED + "The Player " + ChatColor.AQUA + fullPlayer + ChatColor.RED + " has been kicked for " + ChatColor.AQUA + reason + ChatColor.RED + " by " + ChatColor.AQUA + arg0.getName() );
 				}
 			}
 		}
 		else {
 			arg0.sendMessage(StringFormatter.error("No Permission"));
 		}
 	}
 
 }
