 package com.yetanotherx.xbot.bots.aiv;
 
 import java.util.Calendar;
 import java.util.ArrayList;
 import com.yetanotherx.xbot.util.Util;
 import java.util.Arrays;
 import com.yetanotherx.xbot.NewWiki.User;
 import com.yetanotherx.xbot.XBotDebug;
 import com.yetanotherx.xbot.bots.BotJob;
 import com.yetanotherx.xbot.console.ChatColor;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Pattern;
 import static com.yetanotherx.xbot.util.RegexUtil.*;
 
 public class RemoveNameJob extends BotJob<AIVBot> {
 
     private String user;
     private User blocker;
     private String duration;
     private String blockType;
     private String page;
 
     public RemoveNameJob(AIVBot bot, String user, User blocker, String duration, String blockType, String page) {
         super(bot);
         this.user = user;
         this.blocker = blocker;
         this.duration = duration;
         this.blockType = blockType;
         this.page = page;
     }
 
     @Override
     public void doRun() {
         try {
             String content = bot.getParent().getWiki().getPageText(page);
             String originalContent = content.toString();
             Calendar time = bot.getParent().getWiki().getTimestamp();
             
             if (!content.isEmpty()) {
                 int ipsLeft = 0;
                 int usersLeft = 0;
                 boolean found = false;
                 int linesSkipped = 0;
                 List<String> newContent = new LinkedList<String>();
                 boolean inComment = false;
 
                 List<String> contentList = new ArrayList<String>(Arrays.asList(content.split("\n")));
                 while (contentList.size() > 0) {
                     String line = contentList.remove(0);
 
                     String[] comment = AIVBot.parseComment(line, inComment);
                     inComment = Boolean.parseBoolean(comment[0]);
                     String bareLine = comment[1];
                     String remainder = comment[2];
 
                     if (inComment || !matches("\\{\\{((?:ip)?vandal|userlinks|user-uaa)\\|\\s*(?:1=|user=)?\\Q" + user + "\\E\\s*\\}\\}", line, Pattern.CASE_INSENSITIVE)) {
                         newContent.add(line);
                         if (inComment && line.equals(bareLine)) {
                             continue;
                         }
                         if (bareLine.contains("{{IPvandal|")) {
                             ipsLeft++;
                         }
                         if (matches("\\{\\{(vandal|userlinks|user-uaa)\\|", bareLine, Pattern.CASE_INSENSITIVE)) {
                             usersLeft++;
                         }
                     } else {
                         found = true;
                         if (!remainder.isEmpty()) {
                             newContent.add(remainder);
                         }
 
                         while (contentList.size() > 0
                                 && !matches("\\{\\{((?:ip)?vandal|userlinks|user-uaa)\\|", contentList.get(0), Pattern.CASE_INSENSITIVE)
                                 && !contentList.get(0).startsWith("<!--")
                                 && !contentList.get(0).startsWith("=")) {
 
                             String removed = contentList.remove(0);
                             if (!removed.isEmpty()) {
                                 linesSkipped++;
                                 inComment = Boolean.parseBoolean(AIVBot.parseComment(removed, inComment)[0]);
                             }
                         }
                     }
                 }
 
                 content = Util.join("\n", newContent);
                 if (!found || content.isEmpty()) {
                     return;
                 }
 
                 String length = " ";
                 if (!duration.isEmpty()) {
                     if (duration.equals("indef")) {
                         length = " indef ";
                     } else {
                         length = " " + duration;
                     }
                 }
 
                 String tally = "Empty.";
                 if (ipsLeft != 0 || usersLeft != 0) {
                     String ipNote = ipsLeft + " IP" + ((ipsLeft != 1) ? "s" : "");
                     String userNote = usersLeft + " user" + ((usersLeft != 1) ? "s" : "");
 
                     if (usersLeft == 0) { // Only IPs left
                         tally = ipNote + " left.";
                     } else if (ipsLeft == 0) { // Only users left
                         tally = userNote + " left.";
                     } else { // Users and ips left
                         tally = ipNote + " & " + userNote + " left.";
                     }
                 }
 
                 String skipped = "";
                 if (linesSkipped > 0) {
                     skipped = " " + linesSkipped + " comment(s) removed.";
                 }
 
                 String summary = tally + " rm [[Special:Contributions/" + user + "|" + user + "]] (blocked" + length + "by [[User:" + blocker.getUsername() + "|" + blocker.getUsername() + "]] " + blockType + "). " + skipped;
                 if (!originalContent.equals(bot.getParent().getWiki().getPageText(page))) {
                    XBotDebug.warn("AIV", ChatColor.BLUE + page + ChatColor.YELLOW + " has changed since we read it, not changing.", time);
                     return;
                 } else {
                    bot.getParent().getWiki().doEdit(page, content, summary, false);
                 }
                 XBotDebug.info("AIV", ChatColor.GOLD + "Removed " + ChatColor.YELLOW + user + ChatColor.GOLD + " on " + ChatColor.BLUE + page);
             }
 
         } catch (IOException ex) {
             XBotDebug.error("AIV", "Could not read from wiki.", ex);
         }
     }
 
     @Override
     public void doShutdown() {
     }
 }
