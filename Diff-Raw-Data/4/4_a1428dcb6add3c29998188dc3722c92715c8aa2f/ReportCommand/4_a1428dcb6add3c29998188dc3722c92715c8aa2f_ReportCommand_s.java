 package me.heldplayer.ModeratorGui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import me.heldplayer.ModeratorGui.tables.Bans;
 import me.heldplayer.ModeratorGui.tables.Issues;
 import me.heldplayer.ModeratorGui.tables.Unbans;
 
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class ReportCommand implements CommandExecutor {
 
 	private final ModeratorGui main;
 
 	public ReportCommand(ModeratorGui plugin) {
 		main = plugin;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
 		if (args[0].equalsIgnoreCase("issue") && args.length > 2 && sender.hasPermission("moderatorgui.issue")) {
 			List<String> matchedNames = getPlayerMatches(args[1]);
 
 			String name = "";
 
 			if (matchedNames.size() == 1) {
 				name = matchedNames.get(0);
 			} else if (matchedNames.size() == 0) {
 				sender.sendMessage(ChatColor.RED + "No match found for '" + args[1] + "'");
 
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.GREEN + "Multiple matches found: ");
 
 				String matches = "";
 
 				for (String matched : matchedNames) {
 					matches = ", " + matched;
 				}
 
 				matches = matches.replaceFirst(", ", "").trim();
 
 				return true;
 			}
 
 			String issue = "";
 
 			for (int i = 2; i < args.length; i++) {
 				issue += " " + args[i];
 			}
 
 			issue = issue.trim();
 
 			Issues issueRow = new Issues();
 			issueRow.setIssue(issue);
 			issueRow.setReported(name);
 			issueRow.setReporter(sender.getName());
 			issueRow.setTimestamp(System.currentTimeMillis());
 
 			main.getDatabase().save(issueRow);
 
 			return true;
 		}
 
 		if (args[0].equalsIgnoreCase("ban") && args.length > 2 && sender.hasPermission("moderatorgui.ban")) {
 			List<String> matchedNames = getPlayerMatches(args[1]);
 
 			String name = "";
 
 			if (matchedNames.size() == 1) {
 				name = matchedNames.get(0);
 			} else if (matchedNames.size() == 0) {
 				sender.sendMessage(ChatColor.RED + "No match found for '" + args[1] + "'");
 
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.GREEN + "Multiple matches found: ");
 
 				String matches = "";
 
 				for (String matched : matchedNames) {
 					matches = ", " + matched;
 				}
 
 				matches = matches.replaceFirst(", ", "").trim();
 
 				return true;
 			}
 			String reason = "";
 
 			for (int i = 2; i < args.length; i++) {
 				reason += " " + args[i];
 			}
 
 			reason = reason.trim();
 
 			Bans banRow = new Bans();
 			banRow.setReason(reason);
 			banRow.setBanned(name);
 			banRow.setBanner(sender.getName());
 			banRow.setTimestamp(System.currentTimeMillis());
 
 			main.getDatabase().save(banRow);
 
 			return true;
 		}
 
 		if (args[0].equalsIgnoreCase("unban") && args.length > 2 && sender.hasPermission("moderatorgui.unban")) {
 			List<String> matchedNames = getPlayerMatches(args[1]);
 
 			String name = "";
 
 			if (matchedNames.size() == 1) {
 				name = matchedNames.get(0);
 			} else if (matchedNames.size() == 0) {
 				sender.sendMessage(ChatColor.RED + "No match found for '" + args[1] + "'");
 
 				return true;
 			} else {
 				sender.sendMessage(ChatColor.GREEN + "Multiple matches found: ");
 
 				String matches = "";
 
 				for (String matched : matchedNames) {
 					matches = ", " + matched;
 				}
 
 				matches = matches.replaceFirst(", ", "").trim();
 
 				return true;
 			}
 
 			String reason = "";
 
 			for (int i = 2; i < args.length; i++) {
 				reason += " " + args[i];
 			}
 
 			reason = reason.trim();
 
 			Unbans unbanRow = new Unbans();
 			unbanRow.setReason(reason);
 			unbanRow.setUnbanned(name);
 			unbanRow.setUnbanner(sender.getName());
 			unbanRow.setTimestamp(System.currentTimeMillis());
 
 			main.getDatabase().save(unbanRow);
 
 			return true;
 		}
 
 		return false;
 	}
 
 	private List<String> getPlayerMatches(String name) {
 		OfflinePlayer[] players = main.getServer().getOfflinePlayers();
 
 		List<String> matched = new ArrayList<String>();
 
 		for (OfflinePlayer player : players) {
 			if (player.getName().substring(0, name.length()).equalsIgnoreCase(name)) {
 				matched.add(player.getName());
 			}
 		}
 
 		return matched;
 	}
 }
