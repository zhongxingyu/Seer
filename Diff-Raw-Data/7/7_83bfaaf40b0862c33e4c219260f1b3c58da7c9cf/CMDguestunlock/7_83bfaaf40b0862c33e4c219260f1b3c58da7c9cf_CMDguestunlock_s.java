 package se.myller.GuestUnlock.Commands;
 
 import java.util.logging.Level;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import se.myller.GuestUnlock.Main;
 
 
 public class CMDguestunlock implements Command {
 
 	private Main plugin;
 	
 	public CMDguestunlock(Main instance) {
     	plugin = instance;
     }
 	
 	/*
 	 * 
 	 * Handle our commands
 	 * 
 	 */
 	public boolean cmdSend(CommandSender s, String pwd) {
 		s.sendMessage(ChatColor.GREEN + plugin.config.getString("Guest.Responds.Correct"));
 		plugin.log(s.getName() + " has sent the correct password!", false, Level.INFO);
 		Player[] players = plugin.getServer().getOnlinePlayers();
 		int foundModerators = 0;
 		for (Player p: players) {
 			if (p.hasPermission("GuestUnlock.moderator")) {
 				foundModerators++;
 				p.sendMessage(ChatColor.AQUA + "[GuestUnlock] " + ChatColor.GREEN + "The player " + ChatColor.YELLOW + s.getName() + ChatColor.GREEN + " sent the correct password, " + ChatColor.DARK_AQUA + pwd);			
 			}
 		}
 		if (foundModerators < 1) {
 			s.sendMessage(ChatColor.RED + "[GuestUnlock] You typed the correct password but no moderators was online, please try again when someone logs on!");
 		} else if (foundModerators == 1) {
 			s.sendMessage(ChatColor.AQUA + "[GuestUnlock] " + ChatColor.BLUE + "There is one moderator online, he will check your password shortly.");
 		} else {
 			s.sendMessage(ChatColor.AQUA + "[GuestUnlock] " + ChatColor.BLUE + "There are more than 2 moderators online, they will check you password shortly.");
 		}
 		if (plugin.config.getBoolean("Permissions.PermissionsEx.Enable")) {
 			Player player = (Player)s; 
 			plugin.permissionsEx.setGroupPEX(player);
 		}
 		else if (plugin.config.getBoolean("Permissions.GroupManager.Enable")) {
 			Player player = (Player)s; 
 			plugin.groupManager.setGroupGM(player);
 		}
 		else if (plugin.config.getBoolean("Permissions.bPermissions.Enable")) {
 			Player player = (Player)s; 
 			plugin.bPermissions.setGroupBP(player);
 		}
 		return true;
 	}
 	public boolean cmdFail(CommandSender s, String pwd) {
 		s.sendMessage(ChatColor.RED + plugin.config.getString("Guest.Responds.Incorrect"));
 		if(plugin.config.getBoolean("Admin.SendIncorrectMessageToMods") == true) {
 			plugin.log("The player " + s.getName() + " tried to send this password: " + pwd, false, Level.WARNING);
 			Player[] players = plugin.getServer().getOnlinePlayers();
 			for (Player player: players) {
 				if(player.hasPermission("GuestUnlock.moderator")) {
 					player.sendMessage(ChatColor.AQUA + "[GuestUnlock] " + ChatColor.RED + "The player " + ChatColor.YELLOW + s.getName() + ChatColor.RED + " tried to send this password: " + ChatColor.DARK_AQUA + pwd);	
 				}
 			}
 			return true;		
 		}
 		return true;
 	}
 
 	@Override
 	public void onCommandFail(Player p) {
 		p.sendMessage(ChatColor.RED + "Invalid arguments/syntax, try '/guestunlock help'.");
 		return;
 	}
 	@Override
 	public void onCommandHelp(Player p) {
 		p.sendMessage(ChatColor.BLUE + "Usage: /guestunlock <password>");
 		p.sendMessage(ChatColor.BLUE + "Example: '/guestunlock hello' this would send the password: hello, to the moderators.");
 		return;
 	}
 	
 }
