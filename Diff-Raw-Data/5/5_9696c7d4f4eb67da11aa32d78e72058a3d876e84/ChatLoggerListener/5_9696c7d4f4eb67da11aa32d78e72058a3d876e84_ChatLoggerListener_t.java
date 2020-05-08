 package org.pircbotx.listeners;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 import static org.pircbotx.listeners.ChatLoggerListener.ChatLoggerEvent.CHANNEL_MODE;
 import static org.pircbotx.listeners.ChatLoggerListener.ChatLoggerEvent.DISCONNECT;
 import static org.pircbotx.listeners.ChatLoggerListener.ChatLoggerEvent.JOIN;
 import static org.pircbotx.listeners.ChatLoggerListener.ChatLoggerEvent.KICK;
 import static org.pircbotx.listeners.ChatLoggerListener.ChatLoggerEvent.KICK_YOU;
 import static org.pircbotx.listeners.ChatLoggerListener.ChatLoggerEvent.MESSAGE;
 import static org.pircbotx.listeners.ChatLoggerListener.ChatLoggerEvent.MODE;
 import static org.pircbotx.listeners.ChatLoggerListener.ChatLoggerEvent.PART;
 import static org.pircbotx.listeners.ChatLoggerListener.ChatLoggerEvent.QUIT;
 import static org.pircbotx.listeners.ChatLoggerListener.ChatLoggerEvent.TOPIC;
 import static org.pircbotx.listeners.ChatLoggerListener.ChatLoggerEvent.TOPIC_CHANGED;
 import static org.pircbotx.listeners.ChatLoggerListener.ChatLoggerEvent.TOPIC_SET_BY;
 import static org.pircbotx.listeners.ChatLoggerListener.ChatLoggerEvent.USER_MODE;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.pircbotx.Channel;
 import org.pircbotx.PircBotX;
 import org.pircbotx.User;
 import org.pircbotx.UserLevel;
 import org.pircbotx.hooks.ListenerAdapter;
 import org.pircbotx.hooks.events.DisconnectEvent;
 import org.pircbotx.hooks.events.JoinEvent;
 import org.pircbotx.hooks.events.KickEvent;
 import org.pircbotx.hooks.events.MessageEvent;
 import org.pircbotx.hooks.events.ModeEvent;
 import org.pircbotx.hooks.events.PartEvent;
 import org.pircbotx.hooks.events.QuitEvent;
 import org.pircbotx.hooks.events.TopicEvent;
 import org.pircbotx.hooks.events.UserModeEvent;
 import org.pircbotx.util.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.ImmutableMap;
 
 /**
  * A listener that logs everything that the bot sees (or almost...) into log files on the machine it
  * is running. Files are automatically renamed each day to keep one file name per day.
  * <p>
  * Chat events (= things the bot sees) all have a default log format that can be overridden if
  * needed. All supported events are described in the {@link ChatLoggerEvent} enumeration.
  * <p>
  * If you want to override a chat event log format, use
  * {@link #setEventFormat(ChatLoggerEvent, String)}. If you want to add timestamps to your log
  * files, use {@link #setTimestampFormat(String)}.
  *
  * @author Emmanuel Cron
  */
 public class ChatLoggerListener extends ListenerAdapter<PircBotX> implements
     StoppableListener {
 
   private static final Logger LOGGER = LoggerFactory.getLogger(ChatLoggerListener.class);
 
   private static final Map<UserLevel, String> USER_LEVEL_PREFIXES = ImmutableMap.of(
       // Keep it in order of most important to less important
       UserLevel.OWNER, "~",
       UserLevel.SUPEROP, "&",
       UserLevel.OP, "@",
       UserLevel.HALFOP, "%",
       UserLevel.VOICE, "+");
 
   /**
    * List of chat messages that are supported by the chat logger listener.
    * <p>
    * Each chat message has a default log format that may be overridden by a custom value.
    *
    * @author Emmanuel Cron
    */
   public enum ChatLoggerEvent {
     /**
      * When the topic is sent to the bot.
      *
      * <pre>
      * Topic is {topic}
      * </pre>
      */
     TOPIC("* Topic is '%s'"),
     /**
      * When the information about whom sent the last topic is sent to the bot.
      *
      * <pre>
      * Set by {nick} on {date}
      * </pre>
      */
     TOPIC_SET_BY("* Set by %s on %s"),
     /**
      * When someone changes the topic of the channel.
      *
      * <pre>
      * {nick} changes topic to {newtopic}
      * </pre>
      */
     TOPIC_CHANGED("* %s changes topic to '%s'"),
     /**
      * When someone says something on the channel.
      *
      * <pre>
      * &lt;{prefix}{nick}&gt; {message}
      * </pre>
      */
     MESSAGE("<%s%s> %s"),
     /**
      * When someone joins the channel.
      *
      * <pre>
      * {nick} ({login}@{host} has joined {channel}
      * </pre>
      */
     JOIN("* %s (%s@%s) has joined %s"),
     /**
      * When someone leaves the channel.
      *
      * <pre>
      * {prefix}{nick} ({login}@{host} has left {channel}
      * </pre>
      */
     PART("* %s%s (%s@%s) has left %s"),
     /**
      * When someone is kicked from a channel.
      *
      * <pre>
      * * %s was kicked by %s (%s)
      * </pre>
      */
     KICK("* %s was kicked by %s (%s)"),
     /**
      * When the bot is kicked from a channel.
      *
      * <pre>
      * * You were kicked from %s by %s (%s)
      * </pre>
      */
     KICK_YOU("* You were kicked from %s by %s (%s)"),
     /**
      * When the bot is disconnected from the server.
      *
      * <pre>
      * * Disconnected
      * </pre>
      */
     DISCONNECT("* Disconnected"),
     /**
      * When someone quits the server.
      *
      * <pre>
      * {prefix}{nick} ({login}@{host} Quit ({quitmessage})
      * </pre>
      */
     QUIT("* %s%s (%s@%s) Quit (%s)"),
     /**
      * When the channel modes (moderated, private, ...) are changed.
      *
      * <pre>
      * {nick} sets mode: {modes}
      * </pre>
      */
     MODE("* %s sets mode: %s"),
     /**
      * When the modes of a channel are received.
      *
      * <pre>
      * {channel} {modes}
      * </pre>
      */
     CHANNEL_MODE("* Channel modes: %s"),
     /**
      * When user modes (op, voice, ...) are changed.
      *
      * <pre>
      * {nick} sets mode: {nick} {modes}
      * </pre>
      */
     USER_MODE("* %s sets mode: %s %s");
 
     private String defaultFormat;
 
     private int requiredReplacements;
 
     private ChatLoggerEvent(String defaultFormat) {
       this.defaultFormat = defaultFormat;
       this.requiredReplacements = StringUtils.countMatches(defaultFormat, "%s");
     }
 
     /**
      * Number of replacement strings ({@code %s}) expected by this chat event.
      *
      * @return the number of required replacement strings in the format of this chat event
      */
     public int getRequiredReplacements() {
       return requiredReplacements;
     }
 
     /**
      * The default log format of this chat event.
      *
      * @return the default log format
      */
     public String getDefaultFormat() {
       return defaultFormat;
     }
   }
 
   private Map<ChatLoggerEvent, String> eventFormats = new HashMap<ChatLoggerEvent, String>();
 
   private Path logsPath;
 
   private DateTime logFileDate;
 
   private File logFile;
 
   private BufferedWriter logFileWriter;
 
   private String charset;
 
   private DateTimeFormatter timestampFormat;
 
   private boolean checkedFormats;
 
   /**
    * Creates a new chat logger listener.
    *
    * @param logsPath the folder where to store the chat log files
    * @param charset the charset to use when writing in the log files
    */
   public ChatLoggerListener(Path logsPath, String charset) {
     checkNotNull(logsPath, "No chat logs path specified");
     checkArgument(Files.isDirectory(logsPath), "Logs path is not a directory: %s",
         logsPath.toString());
     checkArgument(!Strings.isNullOrEmpty(charset), "No chat logs file encoding specified");
 
     this.logsPath = logsPath;
     this.charset = charset;
   }
 
   /**
    * Sets the format to use for one of the {@link ChatLoggerEvent}s supported by this listener. The
    * given format must contain at least the number of required string replacements of the event
    * (specified by {@link ChatLoggerEvent#getRequiredReplacements()}).
    * <p>
    * The exact data used to replace these string replacements depends on the event logged. They have
    * been hugely inspired by the way the <a href="http://www.mirc.com/">mIRC</a> IRC client displays
    * them.
    * <p>
    * Each event provides a default format that should fit for most situations. However, you can use
    * this method to override this format. You may also use it to disable a particular event by
    * specifying an empty or <tt>null</tt> format.
    * <p>
    * You may get all default formats by calling {@link ChatLoggerEvent#getDefaultFormat()} on each
    * chat event.
    *
    * @param event the event for which set a new format
    * @param format the format; it may be empty or <tt>null</tt> if you wish to disable logging for
    *        this event
    */
   public void setEventFormat(ChatLoggerEvent event, String format) {
     checkNotNull(event);
 
     if (Strings.isNullOrEmpty(format)) {
       eventFormats.put(event, null);
       return;
     }
 
     int countMatches = StringUtils.countMatches(format, "%s");
     if (countMatches < event.getRequiredReplacements()) {
       LOGGER.warn("Number of replacements strings is fewer than the expected count,"
           + " some data may not be logged; event: {}, expected: {}, format: '{}'", new Object[] {
           event.name(), event.getRequiredReplacements(), format});
     } else if (countMatches > event.getRequiredReplacements()) {
       // Fail safe
 
       LOGGER.error("WILL USE DEFAULT LOG FORMAT - Number of replacements strings exceeds"
           + " expected count; event: {}, expected: {}, format: '{}'", new Object[] {event.name(),
           event.getRequiredReplacements(), format});
       // Not inserting anything in map to use default format
       return;
     }
     eventFormats.put(event, format);
   }
 
   /**
    * Sets the timestamp format to use in the chat logs. If the given pattern is empty (or if you
    * never call this method), no timestamp will be used in the logs.
    * <p>
    * Format must be compatible with a {@link DateTimeFormat}.
    *
    * @param pattern the date/time pattern to use for each message in the chat logs
    *
    * @throws IllegalArgumentException if the pattern is invalid
    */
   public void setTimestampFormat(String pattern) {
     if (Strings.isNullOrEmpty(pattern)) {
       timestampFormat = null;
     } else {
       timestampFormat = DateTimeFormat.forPattern(pattern);
     }
   }
 
   @Override
   public void stop() {
     if (logFileWriter != null) {
       try {
         logFileWriter.close();
       } catch (IOException ioe) {
         LOGGER.error("Could not close chat log file writer, will be done when Java system exists",
             ioe);
       }
     }
   }
 
   @Override
   public void onTopic(TopicEvent<PircBotX> event) {
     if (event.isChanged()) {
       log(event.getChannel(), TOPIC_CHANGED, event.getUser().getNick(), event.getTopic());
     } else {
       String formattedDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss", Locale.ENGLISH).format(
           new Date(event.getTimestamp()));
       log(event.getChannel(), TOPIC, event.getTopic());
       log(event.getChannel(), TOPIC_SET_BY, event.getUser().getNick(), formattedDate);
     }
   }
 
   @Override
   public void onMessage(MessageEvent<PircBotX> event) {
     log(event.getChannel(), MESSAGE, getUserPrefix(event.getChannel(), event.getUser()), event
         .getUser().getNick(), event.getMessage());
   }
 
   @Override
   public void onJoin(JoinEvent<PircBotX> event) {
     log(event.getChannel(), JOIN, event.getUser().getNick(), event.getUser().getLogin(), event
         .getUser().getHostmask(), event.getChannel().getName());
   }
 
   @Override
   public void onPart(PartEvent<PircBotX> event) {
     log(event.getChannel(), PART, getUserPrefix(event.getChannel(), event.getUser()), event
         .getUser().getNick(), event.getUser().getLogin(), event.getUser().getHostmask(), event
         .getChannel().getName());
   }
 
   @Override
   public void onDisconnect(DisconnectEvent<PircBotX> event) {
     for (Channel channel : event.getBot().getUserBot().getChannels()) {
       log(channel, DISCONNECT);
     }
   }
 
   @Override
   public void onQuit(QuitEvent<PircBotX> event) {
     for (Channel channel : event.getBot().getUserBot().getChannels()) {
       log(channel, QUIT, getUserPrefix(channel, event.getUser()), event.getUser().getNick(), event
           .getUser().getLogin(), event.getUser().getHostmask(), event.getReason());
     }
   }
 
   @Override
   public void onMode(ModeEvent<PircBotX> event) {
     if (event.getUser() == null) {
       log(event.getChannel(), CHANNEL_MODE, event.getMode());
     } else {
       log(event.getChannel(), MODE, event.getUser().getNick(), event.getMode());
     }
   }
 
   @Override
   public void onUserMode(UserModeEvent<PircBotX> event) {
     for (Channel channel : event.getBot().getUserBot().getChannels()) {
       log(channel, USER_MODE, event.getUser().getNick(), event.getMode(), event.getRecipient()
           .getNick());
     }
   }
 
   @Override
   public void onKick(KickEvent<PircBotX> event) {
     if (event.getRecipient().equals(event.getUser())) {
       log(event.getChannel(), KICK_YOU, event.getChannel().getName(), event.getUser().getNick(),
           event.getReason());
     } else {
       log(event.getChannel(), KICK, event.getRecipient().getNick(), event.getUser().getNick(),
           event.getReason());
     }
   }
 
   // internal helpers
 
   private synchronized void log(Channel channel, ChatLoggerEvent event, Object... args) {
     if (!checkedFormats) {
       for (ChatLoggerEvent checkEvent : ChatLoggerEvent.values()) {
         if (eventFormats.containsKey(checkEvent) && eventFormats.get(checkEvent) == null) {
           LOGGER
               .info("Format of event {} has been forced to nothing; it will not be logged", event);
         }
       }
       checkedFormats = true;
     }
 
     // Get custom format (may be blank) or default if not set
     String format =
         eventFormats.containsKey(event) ? eventFormats.get(event) : event.getDefaultFormat();
 
     // Cannot be blank here, check is done in setEventFormat()
     if (format == null) {
       // Means a key was found but it was set to nothing (= wishing not to log these events)
       return;
     }
 
     String message = String.format(format, args);
     if (timestampFormat != null) {
       message = "[" + timestampFormat.print(System.currentTimeMillis()) + "] " + message;
     }
 
     // Create new file when date changes or if none exist
     if (logFileDate == null || new DateTime().withTimeAtStartOfDay().isAfter(logFileDate)) {
       // First close old writer if it exists
       if (logFileWriter != null) {
         try {
           logFileWriter.close();
         } catch (IOException ioe) {
           LOGGER.warn("Could not close writer to previous chat log file", ioe);
         }
       }
       logFileWriter = null;
 
       logFileDate = new DateTime().withTimeAtStartOfDay();
       String logFileDateStr = DateTimeFormat.forPattern("yyyy_MM_dd").print(logFileDate);
       logFile =
           logsPath.resolve(
               Paths.get(channel.getName().toLowerCase() + "-" + logFileDateStr + ".log")).toFile();
 
       try {
         OutputStream outputStream = new FileOutputStream(logFile, true);
         Writer writer = new OutputStreamWriter(outputStream, charset);
         logFileWriter = new BufferedWriter(writer);
       } catch (IOException ioe) {
         LOGGER.error("LOGGING DISABLED: Could not create writer to chat log file", ioe);
         return;
       }
     }
 
     // Now log it, baby!
     try {
       if (logFile.length() > 0L) {
         logFileWriter.append("\n");
       }
       logFileWriter.append(message);
       logFileWriter.flush();
     } catch (IOException ioe) {
       LOGGER.error("Could not write message to chat log file: " + message, ioe);
     }
   }
 
   private String getUserPrefix(Channel channel, User user) {
    if (!user.getChannels().contains(channel)) {
      // No more on this channel
      return "";
    }

     Set<UserLevel> userLevels = user.getUserLevels(channel);
     if (userLevels.size() > 0) {
       for (Entry<UserLevel, String> entry : USER_LEVEL_PREFIXES.entrySet()) {
          if (userLevels.contains(entry.getKey())) {
            return entry.getValue();
          }
       }
     }
     return "";
   }
 }
