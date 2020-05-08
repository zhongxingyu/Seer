 package ca.wacos.wacosbans;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.sql.SQLException;
 import java.util.logging.Logger;
 
 public class WacosBans extends JavaPlugin implements Listener {
 	public static Logger logger;
 	public void onEnable() {
 		logger = Bukkit.getLogger();
 		this.saveDefaultConfig();
 		if (!checkConfig() || !initDb()) {
 			disablePlugin();
 		}
		getServer().getPluginManager().registerEvents(this, this);
 	}
 	public boolean checkConfig() {
 		String[] paths = {"dbhost", "dbname", "dbusername", "dbpassword"};
 		for (String path : paths)
 			if (getConfig().getString(path).trim().equals("none")) return false;
 		return true;
 	}
 	public boolean initDb() {
 		String host = getConfig().getString("dbhost");
 		String name = getConfig().getString("dbname");
 		String user = getConfig().getString("dbusername");
 		String pass = getConfig().getString("dbpassword");
 		try {
 			SQLManager.init(user, pass, name, host);
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 	public void disablePlugin() {
 		logger.severe("Could not initialize WacosBans! This plugin is disabling.");
 		getPluginLoader().disablePlugin(this);
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if (command.getName().equals("banip") || command.getName().equals("pardonip") || command.getName().equals("unbanip")) {
 			sender.sendMessage(ChatColor.RED + "This command is disabled.");
 		}
 		else if (command.getName().equals("ban")) {
 			if (args.length >= 2) {
 				String reason = "";
 				for (int t = 1; t < args.length; t++) {
 					if (t != args.length - 1)
 						reason += args[t] + " ";
 					else reason += args[t];
 				}
 
 				Player p = Bukkit.getPlayer(args[0]);
 				String target = p == null ? args[0] : p.getName();
 
 				if (!SQLManager.isBanned(target)) {
 					SQLManager.createBan(target, reason, sender.getName());
 					sender.sendMessage(ChatColor.YELLOW + "Permanently banned " + target + " for reason: " + reason);
 				}
 				else {
 					sender.sendMessage(ChatColor.YELLOW + "Player is already banned: " + target);
 					return true;
 				}
 
 				if (p != null)
					p.kickPlayer("Permanently banned: " + reason);
 			}
 			else {
 				sender.sendMessage(ChatColor.YELLOW + "/ban <player> <reason>");
 			}
 		}
 		else if (command.getName().equals("pardon") || command.getName().equals("unban")) {
 			if (args.length >= 1) {
 
 				Player p = Bukkit.getPlayer(args[0]);
 				String target = p == null ? args[0] : p.getName();
 
 				SQLManager.deactivateBans(target, sender.getName());
 				sender.sendMessage(ChatColor.YELLOW + "All bans disabled for player: " + target);
 			}
 			else {
 				sender.sendMessage(ChatColor.YELLOW + "/pardon <player>");
 			}
 			return true;
 		}
 		else if (command.getName().equals("tempban")) {
 			if (args.length >= 4) {
 				int v;
 				try {
 					v = Integer.parseInt(args[1]);
 				}
 				catch (Exception e) {
 					sender.sendMessage(ChatColor.YELLOW + "Invalid value: " + args[1]);
 					return true;
 				}
				if (args[2].equalsIgnoreCase("hour") || args[2].equalsIgnoreCase("hours") ||  args[2].equalsIgnoreCase("hr") ||  args[2].equalsIgnoreCase("hrs")) {
 					v *= 60;
 				}
				else if (args[2].equalsIgnoreCase("day") || args[2].equalsIgnoreCase("days")) {
 					v *= 60 * 24;
 				}
				else if (!args[2].equalsIgnoreCase("min") && !args[2].equalsIgnoreCase("mins") && !args[2].equalsIgnoreCase("minute") && !args[2].equalsIgnoreCase("minutes")) {
 					sender.sendMessage(ChatColor.YELLOW + "Invalid unit: " + args[2]);
 					return true;
 				}
 				String reason = "";
 				for (int t = 3; t < args.length; t++) {
 					if (t != args.length - 1)
 						reason += args[t] + " ";
 					else reason += args[t];
 				}
 				Player p = Bukkit.getPlayer(args[0]);
 				String target = p == null ? args[0] : p.getName();
 
 				if (!SQLManager.isBanned(target)) {
 					SQLManager.createTempBan(target, reason, sender.getName(), v);
 					sender.sendMessage(ChatColor.YELLOW + "Temporarily banned " + target + " for reason: " + reason);
 				}
 				else {
 					sender.sendMessage(ChatColor.YELLOW + "Player is already banned: " + target);
 					return true;
 				}
 
 				if (p != null)
 					p.kickPlayer("Temporarily banned: " + reason);
 			}
 			else {
 				sender.sendMessage(ChatColor.YELLOW + "/tempban <player> <value> <unit> <reason>");
 				sender.sendMessage(ChatColor.YELLOW + "Examples: ");
 				sender.sendMessage(ChatColor.YELLOW + "/tempban player168 20 min Swearing");
 				sender.sendMessage(ChatColor.YELLOW + "/tempban player248 48 hour Advertising");
 				sender.sendMessage(ChatColor.YELLOW + "/tempban felipepcjr 365 day Being a faggot");
 			}
 			return true;
 		}
 		else if (command.getName().equals("warn")) {
 
 			if (args.length >= 2) {
 				String reason = "";
				for (int t = 1; t < args.length; t++) {
 					if (t != args.length - 1)
 						reason += args[t] + " ";
 					else reason += args[t];
 				}
 
 				Player p = Bukkit.getPlayer(args[0]);
 				String target = p == null ? args[0] : p.getName();
 
 				SQLManager.createWarning(target, reason, sender.getName(), 0);
 				sender.sendMessage(ChatColor.YELLOW + "Warned " + target + ": " + reason);
 
 				if (p != null) {
 					p.sendMessage(ChatColor.RED + "You have received a warning: " + ChatColor.WHITE + reason);
 					p.sendMessage(ChatColor.YELLOW + "Warnings add up and can result in a ban, be careful!");
 				}
 			}
 			else {
 				sender.sendMessage(ChatColor.YELLOW + "/warn <player> <warning>");
 			}
 			return true;
 		}
 		else if (command.getName().equals("dump") || command.getName().equals("history")) {
 			if (args.length >= 1) {
 				sender.sendMessage(SQLManager.dumpInfo(args[0]));
 			}
 			else {
 				sender.sendMessage(ChatColor.YELLOW + "/dump <player>");
 			}
 		}
 		return true;
 	}
 
 	@EventHandler
 	void onPlayerLogin(PlayerLoginEvent e) {
 		if (SQLManager.isBanned(e.getPlayer().getName())) {
			e.disallow(PlayerLoginEvent.Result.KICK_BANNED, SQLManager.getBanMessage(e.getPlayer().getName()));
 		}
 	}
 }
