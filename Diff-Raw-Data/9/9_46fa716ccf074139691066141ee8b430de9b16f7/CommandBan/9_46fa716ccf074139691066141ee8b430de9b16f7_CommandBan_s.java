 package dev.xyzcraft.net.bkbw;
 
 import net.md_5.bungee.BungeeCord;
 import net.md_5.bungee.ChatColor;
 import net.md_5.bungee.Permission;
 import net.md_5.bungee.UserConnection;
 import net.md_5.bungee.command.CommandSender;
 import net.md_5.bungee.plugin.JavaPlugin;
 
 public class CommandBan extends MacCommand{
 
 	public CommandBan(JavaPlugin pl) {
 		super(pl);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void execute(CommandSender arg0, String[] arg1) {
 		if (getPermission(arg0) == Permission.MODERATOR || getPermission(arg0) == Permission.ADMIN) {
 		// TODO Auto-generated method stub
 		if (arg1.length < 1) {
 			arg0.sendMessage(StringFormatter.error("You need to specify a player to ban."));
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
 			fullPlayer = partialPlayer;
 		}
 		if (fullPlayer.equals(arg0.getName())) {
 			arg0.sendMessage(StringFormatter.error("You can't ban yourself!"));
 			return;
 		}
 		if (BungeeCord.instance.config.admins.contains(fullPlayer) || BungeeCord.instance.config.moderators.contains(fullPlayer)) {
 			arg0.sendMessage(StringFormatter.error("Player cannot be banned!"));
 			return;
 		}
 		String reason = "Banned: ";
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
 		reason = ChatColor.RED + ((arg1.length > 1) ? reason : reason + "You have been banned!");
 		((WBKMain)this.plugin).bandb.ban(fullPlayer, reason);
 		PlayerKicker.kickPlayer(fullPlayer, reason);
 		for (UserConnection uc : BungeeCord.instance.connections.values()) {
 			uc.sendMessage(ChatColor.RED + "The Player " + ChatColor.AQUA + fullPlayer + ChatColor.RED + " has been banned for " + ChatColor.AQUA + reason + ChatColor.RED + " by " + ChatColor.AQUA + arg0.getName() );
 		}
 	}
 	else {
 		arg0.sendMessage(StringFormatter.error("No Permission"));
 	}
 	}
 }
