 package com.yetanotherx.xbot.bots.aiv;
 
 import org.joda.time.Period;
 import com.yetanotherx.xbot.NewWiki.User;
 import com.yetanotherx.xbot.NewWiki.LogEntry;
 import com.yetanotherx.xbot.XBotDebug;
 import com.yetanotherx.xbot.bots.BotJob;
 import com.yetanotherx.xbot.util.Util;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class CheckUserIsBlockedJob extends BotJob<AIVBot> {
 
     private String page;
     private String user;
 
     public CheckUserIsBlockedJob(AIVBot bot, String user, String page) {
         super(bot);
         this.user = user;
         this.page = page;
     }
 
     @Override
     public void doRun() {
         try {
             LogEntry[] logs = bot.getParent().getWiki().getIPBlockList(user);
             if( logs.length > 0 ) { // should never be >1
                 // User is blocked
                 LogEntry log = logs[0];
                 if( !log.getType().equals("block") ) {
                     return; // WTF???
                 }
                 
                 //BLOCK_LOG new Object[] { boolean anononly, boolean nocreate, boolean noautoblock, boolean noemail, boolean nousertalk, String duration }
                 Object[] details = (Object[]) log.getDetails();
                 boolean paramAO = (Boolean) details[0];
                 boolean paramNC = (Boolean) details[1];
                 boolean paramNAB = (Boolean) details[2];
                 boolean paramNEM = (Boolean) details[3];
                 boolean paramNUT = (Boolean) details[4];
                 String expiry = (String) details[5];
                 
                 User blocker = log.getUser();
                 String duration = "indef";
                 
                 if( !expiry.equals("infinity") ) {
                     //2013-01-02T19:39:27Z
                     int[] expiryDate = Util.parseTZDate(expiry);
                     if( expiryDate == null ) {
                         return;
                     }
                     long timeStampMS = log.getTimestamp().getTimeInMillis();
                     long expiryMS = Util.dateToLong(expiryDate);
                     
                    Period period = new Period(timeStampMS, expiryMS + 1);
                    duration = Util.periodFormatter.print(period) + " ";
                 }
                 
                 List<String> flags = new ArrayList<String>();
                 if( paramAO ) {
                     flags.add("AO");
                 }
                 if( paramNC ) {
                     flags.add("ACB");
                 }
                 if( paramNAB ) {
                     flags.add("ABD");
                 }
                 
                 String blockType = "";
                 if( !flags.isEmpty() ) {
                     blockType = "[[User:HBC AIV helperbot/Legend|(" + Util.join(" ", flags) + ")]]";
                 }
                 
                 this.bot.addJob(new RemoveNameJob(bot, user, blocker, duration, blockType, page));
             }
             
             
             String content = bot.getParent().getWiki().getPageText(page);
 
             if (!content.isEmpty()) {
                 
             }
         } catch (IOException ex) {
             XBotDebug.error("AIV", "Could not read from wiki.", ex);
         }
     }
 
     @Override
     public void doShutdown() {
     }
 }
