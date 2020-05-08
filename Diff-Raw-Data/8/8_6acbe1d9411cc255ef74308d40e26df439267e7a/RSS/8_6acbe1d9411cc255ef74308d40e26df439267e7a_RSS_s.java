 package addon.rss;
 
 import bashoid.Addon;
 import bashoid.Message;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ListIterator;
 import org.joox.Match;
 
 import static utils.Constants.*;
 
 
 public class RSS extends Addon {
 
     private enum Cmds {
         INVALID, LIST, RELOAD, SHOW;
     }
 
     private static final String configKeyCount = "showMsgsCount";
     private static final int MESSAGE_MAX_LENGTH = 450;
 
     private List<Feed> feeds = new ArrayList<>();
     private byte showMsgsCount;
     private boolean firstRun = true;
 
 
     public RSS() {
         try {
             showMsgsCount = Integer.valueOf( config.getValue(configKeyCount) ).byteValue();
         } catch(NumberFormatException e) {
             showMsgsCount = 5;
         }
 
         addFeeds();
         checkFeeds();
         setPeriodicUpdate(60000);
     }
 
     private void addFeeds() {
         Match configFeeds = config.getMatch("feeds feed");
 
         for ( Match feed : configFeeds.each() ) {
             String name = feed.find("name").text();
             String url = feed.find("url").text();
             feeds.add( new Feed(name, url) );
         }
 
         feeds.add( new EzFeed() );
     }
 
     private void removeFeeds() {
         feeds.clear();
     }
 
     private void reloadFeeds() {
         removeFeeds();
         reloadConfig();
         firstRun = true;
         addFeeds();
     }
 
     private void checkFeeds() {
         for(Feed f : feeds) {
             try {
                 List<String> msgs = f.check(showMsgsCount);
                 if( !msgs.isEmpty() && !firstRun )
                     sendChainedMessages(f, msgs);
             } catch(IOException e) {
                 setError(e);
             }
         }
         firstRun = false;
     }
 
     private String executeCmd(Cmds cmd, String message, String author) {
         if(feeds.isEmpty())
             return "No channels available";
 
         switch(cmd) {
             case LIST:
             {
                 String msg = "";
                 for(Feed f : feeds)
                     msg += f.getName() + " ";
                 return msg;
             }
             case SHOW:
             {
                String channel;
                 int index = message.indexOf(" ", message.indexOf("show"));
                int end = message.indexOf(" ", index+1);
                if(end == NOT_FOUND)
                    end = message.length();
 
                channel = message.substring(index+1, end);
                 for(Feed f : feeds) {
                     if(channel.equalsIgnoreCase(f.getName())) {
                         List<String> messages = f.getLastMessages(showMsgsCount);
                         sendMessage(author, "Last " +  showMsgsCount + " messages for rss channel \"" + f.getName() + "\":");
                         for(String s : messages)
                             sendMessage(author, s);
                         break;
                     }
                 }
                 return null;
             }
             case RELOAD:
             {
                 reloadFeeds();
                 sendMessageToChannels("Feeds have been reloaded.");
             }
         }
         return null;
     }
 
     private Cmds getCommand(String message) {
         int begin = message.indexOf(' ') + 1;
         int end = message.indexOf(' ', begin);
         if(end == NOT_FOUND)
             end = message.length();
 
         String cmd = message.substring(begin, end).toUpperCase();
         try {
             return Cmds.valueOf(cmd);
         } catch (IllegalArgumentException iae) {
             return Cmds.INVALID;
         }
     }
 
     private void sendChainedMessages(Feed feed, List<String> messages) {
         String chain = feed.getName() + ": ";
         final String SEPARATOR = " | ";
         boolean isFirstMessageInChain = true;
 
         ListIterator<String> iterator = messages.listIterator( messages.size() );
         while ( iterator.hasPrevious() ) {
             String message = iterator.previous();
             if ( message.length() > MESSAGE_MAX_LENGTH ) {
                 sendMessageToChannels(chain);
                 sendMessageToChannels(message);
                 chain = "";
                 isFirstMessageInChain = true;
             } else if ( message.length() + chain.length() > MESSAGE_MAX_LENGTH ) {
                 sendMessageToChannels(chain);
                 chain = message;
                 isFirstMessageInChain = false;
             } else {
                 if (!isFirstMessageInChain)
                     chain += SEPARATOR;
                 chain += message;
                 isFirstMessageInChain = false;
             }
         }
 
         if ( chain.length() > 0 )
             sendMessageToChannels(chain);
     }
 
     @Override
     public boolean shouldReact(Message message) {
          return message.text.startsWith("rss") && getCommand(message.text) != Cmds.INVALID;
     }
 
     @Override
     protected void setReaction(Message message) {
         Cmds cmd = getCommand(message.text);
         if(cmd == Cmds.INVALID)
             return;
         String result = executeCmd(cmd, message.text, message.author);
         if(result != null)
             reaction.add(result);
     }
 
     @Override
     public void periodicAddonUpdate() {
         checkFeeds();
     }
 
 }
