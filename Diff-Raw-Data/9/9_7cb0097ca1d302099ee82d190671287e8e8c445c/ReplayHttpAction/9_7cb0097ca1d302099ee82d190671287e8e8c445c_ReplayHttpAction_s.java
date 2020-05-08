 package http;
 
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import polly.logging.MyPlugin;
 
 import core.LogFormatter;
 import core.PollyLoggingManager;
 import core.filters.DateLogFilter;
 import core.filters.LogFilter;
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.UserManager;
 import de.skuzzle.polly.sdk.exceptions.DatabaseException;
 import de.skuzzle.polly.sdk.http.HttpAction;
 import de.skuzzle.polly.sdk.http.HttpEvent;
 import de.skuzzle.polly.sdk.http.HttpTemplateContext;
 import de.skuzzle.polly.sdk.http.HttpTemplateException;
 import entities.LogEntry;
 
 public class ReplayHttpAction extends HttpAction {
 
     private PollyLoggingManager logManager;
     private LogFormatter logFormatter;
     
     
     
     public ReplayHttpAction(MyPolly myPolly, LogFormatter logFormatter,
             PollyLoggingManager logManager) {
         super("/Replay", myPolly);
         this.logFormatter = logFormatter;
         this.logManager = logManager;
         this.permissions.add(MyPlugin.REPLAY_PERMISSION);
     }
 
     
     
     @Override
     public HttpTemplateContext execute(HttpEvent e) throws HttpTemplateException {
         HttpTemplateContext c = new HttpTemplateContext("pages/replay.html");
         
         String action = e.getProperty("action");
         
         UserManager userManager = this.getMyPolly().users();
        if (userManager.isSignedOn(e.getSession().getUser())) {
             e.throwTemplateException("You are not logged in", 
                 "You are currently not logged in via irc on polly and thus are not " +
                 "allowed to see the IRC replay.");
         }
         
         if (action != null && action.equals("mark")) {
             e.getSession().getUser().setLastMessageTime(
                     this.myPolly.currentTimeMillis());
             // HACK: do this twice!
             e.getSession().getUser().setLastMessageTime(
                     this.myPolly.currentTimeMillis());
         }
         
         Map<String, List<LogEntry>> logs = new TreeMap<String, List<LogEntry>>();
         
         LogFilter dateFilter = new DateLogFilter(
                 new Date(e.getSession().getUser().getLastIdleTime()));
         
         for (String channel : this.myPolly.irc().getChannels()) {
             if (this.myPolly.irc().isOnChannel(channel, 
                     e.getSession().getUser().getCurrentNickName())) {
                 
                 try {
                     List<LogEntry> channelLogs = this.logManager.preFilterChannel(channel);
                     
                     // XXX: no security filter is used here, because we only query for
                     //      those channels that the user has joined
                     channelLogs = this.logManager.postFilter(channelLogs, dateFilter);
                     Collections.reverse(channelLogs);
                     logs.put(channel, channelLogs);
                 } catch (DatabaseException e1) {
                     e.throwTemplateException(e1);
                 }
             }
         }
         
         c.put("logFormatter", this.logFormatter);
         c.put("logs", logs);
         return c;
     }
 
 }
