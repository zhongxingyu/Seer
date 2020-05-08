 package me.heldplayer.ModeratorGui;
 
 import java.text.SimpleDateFormat;
 
 import me.heldplayer.ModeratorGui.tables.*;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class ReviewCommand implements CommandExecutor {
 
 	private final ModeratorGui main;
 	private final SimpleDateFormat dateFormat;
 
 	public ReviewCommand(ModeratorGui plugin) {
 		main = plugin;
 
 		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
 		if (args.length <= 0) {
 			int rowCount = main.getDatabase().find(Lists.class).findRowCount();
 
			String[] results = new String[Math.min(2, Math.min(10, rowCount + 2))];
 
 			results[0] = ChatColor.GRAY + "Types: " + ChatColor.YELLOW + "Issue " + ChatColor.DARK_RED + "Ban " + ChatColor.DARK_GREEN + "Unban " + ChatColor.RED + "Promote " + ChatColor.GREEN + "Demote";
			results[1] = ChatColor.GRAY + "Current time: " + dateFormat.format(Long.valueOf(System.currentTimeMillis())) + ChatColor.ITALIC + " All times are MM-dd-yyyy HH:mm:ss";
 
 			int sideI = 2;
 			for (int i = rowCount; i > Math.max(rowCount - 10, rowCount); i--) {
 				Lists list = main.getDatabase().find(Lists.class).where().eq("id", i).findUnique();
 
 				if (list == null) {
 					continue;
 				}
 
 				int id = list.getReportId();
 				ReportType type = ReportType.getType(list.getType());
 
 				switch (type) {
 				case ISSUE:
 					Issues issue = main.getDatabase().find(Issues.class).where().eq("id", id).findUnique();
 
 					results[sideI] = ChatColor.AQUA + issue.getReported() + ChatColor.YELLOW + ", by " + ChatColor.AQUA + issue.getReporter() + ChatColor.YELLOW + " at " + ChatColor.AQUA + dateFormat.format(Long.valueOf(issue.getTimestamp())) + ChatColor.YELLOW + ": " + ChatColor.AQUA + issue.getIssue();
 					break;
 				case BAN:
 					Bans ban = main.getDatabase().find(Bans.class).where().eq("id", id).findUnique();
 
 					results[sideI] = ChatColor.AQUA + ban.getBanned() + ChatColor.YELLOW + ", by " + ChatColor.AQUA + ban.getBanner() + ChatColor.YELLOW + " at " + ChatColor.AQUA + dateFormat.format(Long.valueOf(ban.getTimestamp())) + ChatColor.YELLOW + ": " + ChatColor.AQUA + ban.getReason();
 					break;
 				case UNBAN:
 					Unbans unban = main.getDatabase().find(Unbans.class).where().eq("id", id).findUnique();
 
 					results[sideI] = ChatColor.AQUA + unban.getUnbanned() + ChatColor.YELLOW + ", by " + ChatColor.AQUA + unban.getUnbanner() + ChatColor.YELLOW + " at " + ChatColor.AQUA + dateFormat.format(Long.valueOf(unban.getTimestamp())) + ChatColor.YELLOW + ": " + ChatColor.AQUA + unban.getReason();
 					break;
 				case PROMOTE:
 					Promotions promote = main.getDatabase().find(Promotions.class).where().eq("id", id).findUnique();
 
 					results[sideI] = ChatColor.AQUA + promote.getPromoted() + ChatColor.YELLOW + ", by " + ChatColor.AQUA + promote.getPromoter() + ChatColor.YELLOW + " at " + ChatColor.AQUA + dateFormat.format(Long.valueOf(promote.getTimestamp())) + ChatColor.YELLOW + ": " + ChatColor.AQUA + promote.getReason();
 					break;
 				case DEMOTE:
 					Demotions demote = main.getDatabase().find(Demotions.class).where().eq("id", id).findUnique();
 
 					results[sideI] = ChatColor.AQUA + demote.getDemoted() + ChatColor.YELLOW + ", by " + ChatColor.AQUA + demote.getDemoter() + ChatColor.YELLOW + " at " + ChatColor.AQUA + dateFormat.format(Long.valueOf(demote.getTimestamp())) + ChatColor.YELLOW + ": " + ChatColor.AQUA + demote.getReason();
 					break;
 				default:
 					results[sideI] = ChatColor.DARK_GRAY + "Unspecified action happened";
 					break;
 				}
 
 				sideI++;
 			}
 			
 			sender.sendMessage(results);
 
 			return true;
 		}
 
 		return false;
 	}
 }
