 
 package me.heldplayer.ModeratorGui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import me.heldplayer.ModeratorGui.tables.Bans;
 import me.heldplayer.ModeratorGui.tables.Demotions;
 import me.heldplayer.ModeratorGui.tables.Issues;
 import me.heldplayer.ModeratorGui.tables.Lists;
 import me.heldplayer.ModeratorGui.tables.Promotions;
 import me.heldplayer.ModeratorGui.tables.Unbans;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.TabCompleter;
 
 public class ReportCommand implements CommandExecutor, TabCompleter {
 
     private final ModeratorGui main;
 
     public ReportCommand(ModeratorGui plugin) {
         main = plugin;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
         if (args.length <= 0) {
             return false;
         }
 
         long timeStamp = System.currentTimeMillis();
 
         if (args[0].equalsIgnoreCase("issue") && args.length > 2 && sender.hasPermission("moderatorgui.issue")) {
             List<String> matchedNames = ModeratorGui.getPlayerMatches(args[1]);
 
             String name = "";
 
             if (matchedNames.size() == 1) {
                 name = matchedNames.get(0);
             }
             else if (matchedNames.size() == 0) {
                 sender.sendMessage(ChatColor.RED + "No match found for '" + args[1] + "'");
 
                 return true;
             }
             else {
                 sender.sendMessage(ChatColor.GREEN + "Multiple matches found: ");
 
                 String matches = "";
 
                 for (String matched : matchedNames) {
                     matches += ", " + matched;
                 }
 
                 matches = matches.replaceFirst(", ", "").trim();
 
                 sender.sendMessage(ChatColor.GRAY + matches);
 
                 return true;
             }
 
             String issue = "";
 
             for (int i = 2; i < args.length; i++) {
                 issue += " " + args[i];
             }
 
             issue = issue.trim();
 
             Issues row = new Issues();
             row.setIssue(issue);
             row.setReported(name);
             row.setReporter(sender.getName());
             row.setTimestamp(timeStamp);
 
             main.getDatabase().save(row);
 
             Issues created = main.getDatabase().find(Issues.class).where().eq("timestamp", timeStamp).findUnique();
             report(created.getId(), ReportType.ISSUE, sender.getName(), name);
             main.performCommands("issue", sender, row.getId(), row.getReported(), row.getReporter(), row.getIssue(), row.getTimestamp(), null, null);
 
             String reportString = main.formatReport(main.displayStrings[0], row.getId(), row.getReported(), row.getReporter(), row.getIssue(), row.getTimestamp(), null, null);
 
             main.getServer().broadcast(ChatColor.GRAY + sender.getName() + " reported a new issue.", "moderatorgui.viewreported");
             main.getServer().broadcast(reportString, "moderatorgui.viewreported");
 
             sender.sendMessage(ChatColor.GREEN + "Reported!");
 
             return true;
         }
 
         if (args[0].equalsIgnoreCase("ban") && args.length > 2 && sender.hasPermission("moderatorgui.ban")) {
             List<String> matchedNames = ModeratorGui.getPlayerMatches(args[1]);
 
             String name = "";
 
             if (matchedNames.size() == 1) {
                 name = matchedNames.get(0);
             }
             else if (matchedNames.size() == 0) {
                 sender.sendMessage(ChatColor.RED + "No match found for '" + args[1] + "'");
 
                 return true;
             }
             else {
                 sender.sendMessage(ChatColor.GREEN + "Multiple matches found: ");
 
                 String matches = "";
 
                 for (String matched : matchedNames) {
                     matches += ", " + matched;
                 }
 
                 matches = matches.replaceFirst(", ", "").trim();
 
                 sender.sendMessage(ChatColor.GRAY + matches);
 
                 return true;
             }
             String reason = "";
 
             for (int i = 2; i < args.length; i++) {
                 reason += " " + args[i];
             }
 
             reason = reason.trim();
 
             Bans row = new Bans();
             row.setReason(reason);
             row.setReported(name);
             row.setReporter(sender.getName());
             row.setTimestamp(timeStamp);
 
             main.getDatabase().save(row);
 
             Bans created = main.getDatabase().find(Bans.class).where().eq("timestamp", timeStamp).findUnique();
             report(created.getId(), ReportType.BAN, sender.getName(), name);
             main.performCommands("ban", sender, row.getId(), row.getReported(), row.getReporter(), row.getReason(), row.getTimestamp(), null, null);
 
             String reportString = main.formatReport(main.displayStrings[4], row.getId(), row.getReported(), row.getReporter(), row.getReason(), row.getTimestamp(), null, null);
 
             main.getServer().broadcast(ChatColor.GRAY + sender.getName() + " reported a new ban.", "moderatorgui.viewreported");
             main.getServer().broadcast(reportString, "moderatorgui.viewreported");
 
             sender.sendMessage(ChatColor.GREEN + "Reported!");
 
             return true;
         }
 
         if (args[0].equalsIgnoreCase("unban") && args.length > 2 && sender.hasPermission("moderatorgui.unban")) {
             List<String> matchedNames = ModeratorGui.getPlayerMatches(args[1]);
 
             String name = "";
 
             if (matchedNames.size() == 1) {
                 name = matchedNames.get(0);
             }
             else if (matchedNames.size() == 0) {
                 sender.sendMessage(ChatColor.RED + "No match found for '" + args[1] + "'");
 
                 return true;
             }
             else {
                 sender.sendMessage(ChatColor.GREEN + "Multiple matches found: ");
 
                 String matches = "";
 
                 for (String matched : matchedNames) {
                     matches += ", " + matched;
                 }
 
                 matches = matches.replaceFirst(", ", "").trim();
 
                 sender.sendMessage(ChatColor.GRAY + matches);
 
                 return true;
             }
 
             String reason = "";
 
             for (int i = 2; i < args.length; i++) {
                 reason += " " + args[i];
             }
 
             reason = reason.trim();
 
             Unbans row = new Unbans();
             row.setReason(reason);
             row.setReported(name);
             row.setReporter(sender.getName());
             row.setTimestamp(timeStamp);
 
             main.getDatabase().save(row);
 
             Unbans created = main.getDatabase().find(Unbans.class).where().eq("timestamp", timeStamp).findUnique();
             report(created.getId(), ReportType.UNBAN, sender.getName(), name);
             main.performCommands("unban", sender, row.getId(), row.getReported(), row.getReporter(), row.getReason(), row.getTimestamp(), null, null);
 
             String reportString = main.formatReport(main.displayStrings[5], row.getId(), row.getReported(), row.getReporter(), row.getReason(), row.getTimestamp(), null, null);
 
             main.getServer().broadcast(ChatColor.GRAY + sender.getName() + " reported a new unban.", "moderatorgui.viewreported");
             main.getServer().broadcast(reportString, "moderatorgui.viewreported");
 
             sender.sendMessage(ChatColor.GREEN + "Reported!");
 
             return true;
         }
 
         if (args[0].equalsIgnoreCase("promote") && args.length > 4 && sender.hasPermission("moderatorgui.promote")) {
             List<String> matchedNames = ModeratorGui.getPlayerMatches(args[1]);
 
             String name = "";
 
             if (matchedNames.size() == 1) {
                 name = matchedNames.get(0);
             }
             else if (matchedNames.size() == 0) {
                 sender.sendMessage(ChatColor.RED + "No match found for '" + args[1] + "'");
 
                 return true;
             }
             else {
                 sender.sendMessage(ChatColor.GREEN + "Multiple matches found: ");
 
                 String matches = "";
 
                 for (String matched : matchedNames) {
                     matches += ", " + matched;
                 }
 
                 matches = matches.replaceFirst(", ", "").trim();
 
                 sender.sendMessage(ChatColor.GRAY + matches);
 
                 return true;
             }
 
             List<String> matchedRanks1 = ModeratorGui.getRankMatches(args[2]);
 
             String rank1 = "";
 
             if (matchedRanks1.size() == 1) {
                 rank1 = matchedRanks1.get(0);
             }
             else if (matchedRanks1.size() == 0) {
                 sender.sendMessage(ChatColor.RED + "No match found for '" + args[2] + "'");
 
                 return true;
             }
             else {
                 sender.sendMessage(ChatColor.GREEN + "Multiple matches found: ");
 
                 String matches = "";
 
                 for (String matched : matchedRanks1) {
                     matches += ", " + matched;
                 }
 
                 matches = matches.replaceFirst(", ", "").trim();
 
                 sender.sendMessage(ChatColor.GRAY + matches);
 
                 return true;
             }
 
             List<String> matchedRanks2 = ModeratorGui.getRankMatches(args[3]);
 
             String rank2 = "";
 
             if (matchedRanks2.size() == 1) {
                 rank2 = matchedRanks2.get(0);
             }
             else if (matchedRanks2.size() == 0) {
                 sender.sendMessage(ChatColor.RED + "No match found for '" + args[3] + "'");
 
                 return true;
             }
             else {
                 sender.sendMessage(ChatColor.GREEN + "Multiple matches found: ");
 
                 String matches = "";
 
                 for (String matched : matchedRanks2) {
                     matches += ", " + matched;
                 }
 
                 matches = matches.replaceFirst(", ", "").trim();
 
                 sender.sendMessage(ChatColor.GRAY + matches);
 
                 return true;
             }
 
             String reason = "";
 
             for (int i = 4; i < args.length; i++) {
                 reason += " " + args[i];
             }
 
             reason = reason.trim();
 
             Promotions row = new Promotions();
             row.setReason(reason);
             row.setReported(name);
             row.setReporter(sender.getName());
             row.setPrevRank(rank1);
             row.setNewRank(rank2);
             row.setTimestamp(timeStamp);
 
             main.getDatabase().save(row);
 
             Promotions created = main.getDatabase().find(Promotions.class).where().eq("timestamp", timeStamp).findUnique();
             report(created.getId(), ReportType.PROMOTE, sender.getName(), name);
             main.performCommands("promote", sender, row.getId(), row.getReported(), row.getReporter(), row.getReason(), row.getTimestamp(), row.getPrevRank(), row.getNewRank());
 
             String reportString = main.formatReport(main.displayStrings[2], row.getId(), row.getReported(), row.getReporter(), row.getReason(), row.getTimestamp(), row.getPrevRank(), row.getNewRank());
 
             main.getServer().broadcast(ChatColor.GRAY + sender.getName() + " reported a new promotion.", "moderatorgui.viewreported");
             main.getServer().broadcast(reportString, "moderatorgui.viewreported");
 
             sender.sendMessage(ChatColor.GREEN + "Reported!");
 
             return true;
         }
 
         if (args[0].equalsIgnoreCase("demote") && args.length > 4 && sender.hasPermission("moderatorgui.demote")) {
             List<String> matchedNames = ModeratorGui.getPlayerMatches(args[1]);
 
             String name = "";
 
             if (matchedNames.size() == 1) {
                 name = matchedNames.get(0);
             }
             else if (matchedNames.size() == 0) {
                 sender.sendMessage(ChatColor.RED + "No match found for '" + args[1] + "'");
 
                 return true;
             }
             else {
                 sender.sendMessage(ChatColor.GREEN + "Multiple matches found: ");
 
                 String matches = "";
 
                 for (String matched : matchedNames) {
                     matches += ", " + matched;
                 }
 
                 matches = matches.replaceFirst(", ", "").trim();
 
                 sender.sendMessage(ChatColor.GRAY + matches);
 
                 return true;
             }
 
             List<String> matchedRanks1 = ModeratorGui.getRankMatches(args[2]);
 
             String rank1 = "";
 
             if (matchedRanks1.size() == 1) {
                 rank1 = matchedRanks1.get(0);
             }
             else if (matchedRanks1.size() == 0) {
                 sender.sendMessage(ChatColor.RED + "No match found for '" + args[2] + "'");
 
                 return true;
             }
             else {
                 sender.sendMessage(ChatColor.GREEN + "Multiple matches found: ");
 
                 String matches = "";
 
                 for (String matched : matchedRanks1) {
                     matches += ", " + matched;
                 }
 
                 matches = matches.replaceFirst(", ", "").trim();
 
                 sender.sendMessage(ChatColor.GRAY + matches);
 
                 return true;
             }
 
             List<String> matchedRanks2 = ModeratorGui.getRankMatches(args[3]);
 
             String rank2 = "";
 
             if (matchedRanks2.size() == 1) {
                 rank2 = matchedRanks2.get(0);
             }
             else if (matchedRanks2.size() == 0) {
                 sender.sendMessage(ChatColor.RED + "No match found for '" + args[3] + "'");
 
                 return true;
             }
             else {
                 sender.sendMessage(ChatColor.GREEN + "Multiple matches found: ");
 
                 String matches = "";
 
                 for (String matched : matchedRanks2) {
                     matches += ", " + matched;
                 }
 
                 matches = matches.replaceFirst(", ", "").trim();
 
                 sender.sendMessage(ChatColor.GRAY + matches);
 
                 return true;
             }
 
             String reason = "";
 
             for (int i = 4; i < args.length; i++) {
                 reason += " " + args[i];
             }
 
             reason = reason.trim();
 
             Demotions row = new Demotions();
             row.setReason(reason);
             row.setReported(name);
             row.setReporter(sender.getName());
             row.setPrevRank(rank1);
             row.setNewRank(rank2);
             row.setTimestamp(timeStamp);
 
             main.getDatabase().save(row);
 
             Demotions created = main.getDatabase().find(Demotions.class).where().eq("timestamp", timeStamp).findUnique();
             report(created.getId(), ReportType.DEMOTE, sender.getName(), name);
             main.performCommands("demote", sender, row.getId(), row.getReported(), row.getReporter(), row.getReason(), row.getTimestamp(), row.getPrevRank(), row.getNewRank());
 
            sender.sendMessage(ChatColor.GREEN + "Reported!");

             String reportString = main.formatReport(main.displayStrings[3], row.getId(), row.getReported(), row.getReporter(), row.getReason(), row.getTimestamp(), row.getPrevRank(), row.getNewRank());
 
             main.getServer().broadcast(ChatColor.GRAY + sender.getName() + " reported a new demotion.", "moderatorgui.viewreported");
             main.getServer().broadcast(reportString, "moderatorgui.viewreported");
 
             return true;
         }
 
         if (args[0].equalsIgnoreCase("help")) {
             if (sender.hasPermission("moderatorgui.issue"))
                 sender.sendMessage(ChatColor.GRAY + "/" + alias + " issue <playername> <issue>");
             if (sender.hasPermission("moderatorgui.ban"))
                 sender.sendMessage(ChatColor.GRAY + "/" + alias + " ban <playername> <reason>");
             if (sender.hasPermission("moderatorgui.unban"))
                 sender.sendMessage(ChatColor.GRAY + "/" + alias + " unban <playername> <reason>");
             if (sender.hasPermission("moderatorgui.promote"))
                 sender.sendMessage(ChatColor.GRAY + "/" + alias + " promote <playername> <oldrank> <newrank> <reason>");
             if (sender.hasPermission("moderatorgui.demote"))
                 sender.sendMessage(ChatColor.GRAY + "/" + alias + " demote <playername> <oldrank> <newrank> <reason>");
 
             return true;
         }
 
         return false;
     }
 
     @Override
     public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
         ArrayList<String> possibles = new ArrayList<String>();
 
         if (sender.hasPermission("moderatorgui.ban")) {
             if (args.length == 1) {
                 possibles.add("ban");
             }
         }
 
         if (args.length == 2) {
             return null;
         }
 
         if (sender.hasPermission("moderatorgui.demote")) {
             if (args.length == 1) {
                 possibles.add("demote");
             }
             if (args[0].equalsIgnoreCase("demote") && (args.length == 3 || args.length == 4)) {
                 possibles.addAll(ModeratorGui.getRankMatches(null));
             }
         }
 
         if (args.length == 1) {
             possibles.add("help");
         }
 
         if (sender.hasPermission("moderatorgui.issue")) {
             if (args.length == 1) {
                 possibles.add("issue");
             }
         }
 
         if (sender.hasPermission("moderatorgui.promote")) {
             if (args.length == 1) {
                 possibles.add("promote");
             }
             if (args[0].equalsIgnoreCase("promote") && (args.length == 3 || args.length == 4)) {
                 possibles.addAll(ModeratorGui.getRankMatches(null));
             }
         }
 
         if (sender.hasPermission("moderatorgui.unban")) {
             if (args.length == 1) {
                 possibles.add("unban");
             }
         }
 
         ArrayList<String> result = new ArrayList<String>();
 
         for (String possible : possibles) {
             if (possible.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                 result.add(possible);
             }
         }
 
         return result;
     }
 
     private void report(int id, ReportType type, String reporter, String target) {
         Lists listRow = new Lists();
 
         listRow.setReportId(id);
         listRow.setType(type.getId());
         listRow.setReporter(reporter);
         listRow.setTarget(target);
 
         main.getDatabase().save(listRow);
     }
 
 }
