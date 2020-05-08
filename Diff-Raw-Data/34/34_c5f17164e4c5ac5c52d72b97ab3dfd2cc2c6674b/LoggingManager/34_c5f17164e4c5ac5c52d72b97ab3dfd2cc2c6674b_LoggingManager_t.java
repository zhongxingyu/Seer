 /*
  * Copyright (c) 2006-2014 DMDirc Developers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.logging;
 
 import com.dmdirc.Channel;
 import com.dmdirc.ClientModule.GlobalConfig;
 import com.dmdirc.DMDircMBassador;
 import com.dmdirc.FrameContainer;
 import com.dmdirc.Query;
 import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
 import com.dmdirc.events.BaseChannelActionEvent;
 import com.dmdirc.events.BaseChannelMessageEvent;
 import com.dmdirc.events.BaseQueryActionEvent;
 import com.dmdirc.events.BaseQueryMessageEvent;
 import com.dmdirc.events.ChannelClosedEvent;
 import com.dmdirc.events.ChannelGottopicEvent;
 import com.dmdirc.events.ChannelJoinEvent;
 import com.dmdirc.events.ChannelKickEvent;
 import com.dmdirc.events.ChannelModechangeEvent;
 import com.dmdirc.events.ChannelNickchangeEvent;
 import com.dmdirc.events.ChannelOpenedEvent;
 import com.dmdirc.events.ChannelPartEvent;
 import com.dmdirc.events.ChannelQuitEvent;
 import com.dmdirc.events.ChannelTopicChangeEvent;
 import com.dmdirc.events.QueryClosedEvent;
 import com.dmdirc.events.QueryOpenedEvent;
 import com.dmdirc.events.UserErrorEvent;
 import com.dmdirc.interfaces.PrivateChat;
 import com.dmdirc.interfaces.config.AggregateConfigProvider;
 import com.dmdirc.interfaces.config.ConfigChangeListener;
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.parser.interfaces.ChannelClientInfo;
 import com.dmdirc.parser.interfaces.ChannelInfo;
 import com.dmdirc.parser.interfaces.ClientInfo;
 import com.dmdirc.parser.interfaces.Parser;
 import com.dmdirc.plugins.PluginDomain;
 import com.dmdirc.ui.WindowManager;
 import com.dmdirc.ui.messages.ColourManagerFactory;
 import com.dmdirc.ui.messages.Styliser;
 import com.dmdirc.util.URLBuilder;
 import com.dmdirc.util.io.ReverseFileReader;
 import com.dmdirc.util.io.StreamUtils;
 
 import java.awt.Color;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Stack;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.annotation.Nullable;
 import javax.inject.Inject;
 import javax.inject.Provider;
 import javax.inject.Singleton;
 
 import net.engio.mbassy.listener.Handler;
 
 /**
  * Manages logging activities.
  */
 @Singleton
 public class LoggingManager implements ConfigChangeListener {
 
     /** Date format used for "File Opened At" log. */
     private static final DateFormat OPENED_AT_FORMAT = new SimpleDateFormat(
             "EEEE MMMM dd, yyyy - HH:mm:ss");
     /** Object for synchronising access to the date forma.t */
     private static final Object FORMAT_LOCK = new Object();
     /** This plugin's plugin info. */
     private final String domain;
     /** Global config. */
     private final AggregateConfigProvider config;
     /** The manager to add history windows to. */
     private final WindowManager windowManager;
     /** Map of open files. */
     private final Map<String, OpenFile> openFiles = Collections.synchronizedMap(
             new HashMap<String, OpenFile>());
     private final URLBuilder urlBuilder;
     private final DMDircMBassador eventBus;
     private final Provider<String> directoryProvider;
     private final ColourManagerFactory colourManagerFactory;
     /** Timer used to close idle files. */
     private Timer idleFileTimer;
     /** Cached boolean settings. */
     private boolean networkfolders;
     private boolean filenamehash;
     private boolean addtime;
     private boolean stripcodes;
     private boolean channelmodeprefix;
     private boolean autobackbuffer;
     private boolean backbufferTimestamp;
     private boolean usedate;
     /** Cached string settings. */
     private String timestamp;
     private String usedateformat;
     private String colour;
     /** Cached int settings. */
     private int historyLines;
     private int backbufferLines;
 
     @Inject
     public LoggingManager(@PluginDomain(LoggingPlugin.class) final String domain,
             @GlobalConfig final AggregateConfigProvider globalConfig,
             final WindowManager windowManager, final URLBuilder urlBuilder, final DMDircMBassador eventBus,
             @Directory(LoggingModule.LOGS_DIRECTORY) final Provider<String> directoryProvider,
             final ColourManagerFactory colourManagerFactory) {
         this.domain = domain;
         this.config = globalConfig;
         this.windowManager = windowManager;
         this.urlBuilder = urlBuilder;
         this.eventBus = eventBus;
         this.directoryProvider = directoryProvider;
         this.colourManagerFactory = colourManagerFactory;
     }
 
     public void load() {
         setCachedSettings();
 
         final File dir = new File(directoryProvider.get());
         if (dir.exists()) {
             if (!dir.isDirectory()) {
                 eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, null,
                         "Unable to create logging dir (file exists instead)", ""));
             }
         } else {
             if (!dir.mkdirs()) {
                 eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, null,
                         "Unable to create logging dir", ""));
             }
         }
 
         config.addChangeListener(domain, this);
 
         // Close idle files every hour.
         idleFileTimer = new Timer("LoggingPlugin Timer");
         idleFileTimer.schedule(new TimerTask() {
 
             @Override
             public void run() {
                 timerTask();
             }
         }, 3600000);
 
         eventBus.subscribe(this);
     }
 
     public void unload() {
         if (idleFileTimer != null) {
             idleFileTimer.cancel();
             idleFileTimer.purge();
         }
 
         synchronized (openFiles) {
             for (OpenFile file : openFiles.values()) {
                 StreamUtils.close(file.writer);
             }
             openFiles.clear();
         }
 
         eventBus.unsubscribe(this);
     }
 
     /**
      * What to do every hour when the timer fires.
      */
     protected void timerTask() {
         // Oldest time to allow
         final long oldestTime = System.currentTimeMillis() - 3480000;
 
         synchronized (openFiles) {
             final Collection<String> old = new ArrayList<>(openFiles.size());
             for (Map.Entry<String, OpenFile> entry : openFiles.entrySet()) {
                 if (entry.getValue().lastUsedTime < oldestTime) {
                     StreamUtils.close(entry.getValue().writer);
                     old.add(entry.getKey());
                 }
             }
 
             openFiles.keySet().removeAll(old);
         }
     }
 
     @Handler
     public void handleQueryOpened(final QueryOpenedEvent event) {
         final Parser parser = event.getQuery().getConnection().getParser();
         final ClientInfo client = parser.getClient(event.getQuery().getHost());
         final String filename = getLogFile(client);
         if (autobackbuffer) {
             showBackBuffer(event.getQuery(), filename);
         }
 
         synchronized (FORMAT_LOCK) {
             appendLine(filename, "*** Query opened at: %s", OPENED_AT_FORMAT.format(new Date()));
             appendLine(filename, "*** Query with User: %s", event.getQuery().getHost());
             appendLine(filename, "");
         }
     }
 
     @Handler
     public void handleQueryClosed(final QueryClosedEvent event) {
         final Parser parser = event.getQuery().getConnection().getParser();
         final ClientInfo client = parser.getClient(event.getQuery().getHost());
         final String filename = getLogFile(client);
 
         synchronized (FORMAT_LOCK) {
             appendLine(filename, "*** Query closed at: %s", OPENED_AT_FORMAT.format(new Date()));
         }
 
         if (openFiles.containsKey(filename)) {
             StreamUtils.close(openFiles.get(filename).writer);
             openFiles.remove(filename);
         }
     }
 
     @Handler
     public void handleQueryActions(final BaseQueryActionEvent event) {
         final ClientInfo client = event.getClient();
         final String filename = getLogFile(client);
         appendLine(filename, "* %s %s", client.getNickname(), event.getMessage());
     }
 
     @Handler
     public void handleQueryMessages(final BaseQueryMessageEvent event) {
         final ClientInfo client = event.getClient();
         final String filename = getLogFile(client);
         appendLine(filename, "<%s> %s", client.getNickname(), event.getMessage());
     }
 
     @Handler
     public void handleChannelMessage(final BaseChannelMessageEvent event) {
         final String filename = getLogFile(event.getChannel().getChannelInfo());
         appendLine(filename, "<%s> %s", getDisplayName(event.getClient()), event.getMessage());
     }
 
     @Handler
     public void handleChannelAction(final BaseChannelActionEvent event) {
         final String filename = getLogFile(event.getChannel().getChannelInfo());
         appendLine(filename, "* %s %s", getDisplayName(event.getClient()), event.getMessage());
     }
 
     @Handler
     public void handleChannelGotTopic(final ChannelGottopicEvent event) {
         final String filename = getLogFile(event.getChannel().getChannelInfo());
         final ChannelInfo channel = event.getChannel().getChannelInfo();
         final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
         final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
 
         appendLine(filename, "*** Topic is: %s", channel.getTopic());
         appendLine(filename, "*** Set at: %s on %s by %s", timeFormat.format(1000 * channel.
                 getTopicTime()), dateFormat.format(1000 * channel.getTopicTime()), channel.
                 getTopicSetter());
     }
 
     @Handler
     public void handleChannelTopicChange(final ChannelTopicChangeEvent event) {
         final String filename = getLogFile(event.getChannel().getChannelInfo());
         final ChannelClientInfo channelClient = event.getClient();
         appendLine(filename, "*** %s Changed the topic to: %s",
                 getDisplayName(channelClient), event.getTopic());
     }
 
     @Handler
     public void handleChannelJoin(final ChannelJoinEvent event) {
         final String filename = getLogFile(event.getChannel().getChannelInfo());
         final ChannelClientInfo channelClient = event.getClient();
         final ClientInfo client = channelClient.getClient();
         appendLine(filename, "*** %s (%s) joined the channel",
                 getDisplayName(channelClient), client.toString());
     }
 
     @Handler
     public void handleChannelPart(final ChannelPartEvent event) {
         final String filename = getLogFile(event.getChannel().getChannelInfo());
         final String message = event.getMessage();
         final ChannelClientInfo channelClient = event.getClient();
         final ClientInfo client = channelClient.getClient();
         if (message.isEmpty()) {
             appendLine(filename, "*** %s (%s) left the channel",
                     getDisplayName(channelClient), client.toString());
         } else {
             appendLine(filename, "*** %s (%s) left the channel (%s)",
                     getDisplayName(channelClient), client.toString(), message);
         }
     }
 
     @Handler
     public void handleChannelQuit(final ChannelQuitEvent event) {
         final String filename = getLogFile(event.getChannel().getChannelInfo());
         final String reason = event.getMessage();
         final ChannelClientInfo channelClient = event.getClient();
         final ClientInfo client = channelClient.getClient();
         if (reason.isEmpty()) {
             appendLine(filename, "*** %s (%s) Quit IRC",
                     getDisplayName(channelClient), client.toString());
         } else {
             appendLine(filename, "*** %s (%s) Quit IRC (%s)",
                     getDisplayName(channelClient), client.toString(), reason);
         }
     }
 
     @Handler
     public void handleChannelKick(final ChannelKickEvent event) {
         final ChannelClientInfo victim = event.getVictim();
         final ChannelClientInfo perpetrator = event.getClient();
         final String reason = event.getReason();
         final String filename = getLogFile(event.getChannel().getChannelInfo());
 
         if (reason.isEmpty()) {
             appendLine(filename, "*** %s was kicked by %s",
                     getDisplayName(victim), getDisplayName(perpetrator));
         } else {
             appendLine(filename, "*** %s was kicked by %s (%s)",
                     getDisplayName(victim), getDisplayName(perpetrator), reason);
         }
     }
 
     @Handler
     public void handleNickChange(final ChannelNickchangeEvent event) {
         final String filename = getLogFile(event.getChannel().getChannelInfo());
         appendLine(filename, "*** %s is now %s", getDisplayName(event.getClient(),
                 event.getOldNick()), getDisplayName(event.getClient()));
     }
 
     @Handler
     public void handleModeChange(final ChannelModechangeEvent event) {
         final String filename = getLogFile(event.getChannel().getChannelInfo());
         if (event.getClient().getClient().getNickname().isEmpty()) {
             appendLine(filename, "*** Channel modes are: %s", event.getModes());
         } else {
             appendLine(filename, "*** %s set modes: %s",
                     getDisplayName(event.getClient()), event.getModes());
         }
     }
 
     @Override
     public void configChanged(final String domain, final String key) {
         setCachedSettings();
     }
 
     @Handler
     public void handleChannelOpened(final ChannelOpenedEvent event) {
         final String filename = getLogFile(event.getChannel().getName());
 
         if (autobackbuffer) {
             showBackBuffer(event.getChannel(), filename);
         }
 
         synchronized (FORMAT_LOCK) {
             appendLine(filename, "*** Channel opened at: %s", OPENED_AT_FORMAT.format(new Date()));
             appendLine(filename, "");
         }
     }
 
     @Handler
     public void handleChannelClosed(final ChannelClosedEvent event) {
         final String filename = getLogFile(event.getChannel().getName());
 
         synchronized (FORMAT_LOCK) {
             appendLine(filename, "*** Channel closed at: %s", OPENED_AT_FORMAT.format(new Date()));
         }
 
         if (openFiles.containsKey(filename)) {
             StreamUtils.close(openFiles.get(filename).writer);
             openFiles.remove(filename);
         }
     }
 
     /**
      * Add a backbuffer to a frame.
      *
      * @param frame    The frame to add the backbuffer lines to
      * @param filename File to get backbuffer from
      */
     protected void showBackBuffer(final FrameContainer frame, final String filename) {
         if (frame == null) {
             eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, null, "Given a null frame", ""));
             return;
         }
 
         final Path testFile = Paths.get(filename);
         if (Files.exists(testFile)) {
             try {
                 final ReverseFileReader file = new ReverseFileReader(testFile);
                 // Because the file includes a newline char at the end, an empty line
                 // is returned by getLines. To counter this, we call getLines(1) and do
                 // nothing with the output.
                 file.getLines(1);
                 final Stack<String> lines = file.getLines(backbufferLines);
                 while (!lines.empty()) {
                     frame.addLine(getColouredString(colour, lines.pop()), backbufferTimestamp);
                 }
                 file.close();
                 frame.addLine(getColouredString(colour, "--- End of backbuffer\n"),
                         backbufferTimestamp);
             } catch (IOException | SecurityException e) {
                 eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, e,
                         "Unable to show backbuffer (Filename: " + filename + "): " + e.getMessage(),
                         ""));
             }
         }
     }
 
     /**
      * Get a coloured String. If colour is invalid, IRC Colour 14 will be used.
      *
      * @param colour The colour the string should be (IRC Colour or 6-digit hex colour)
      * @param line   the line to colour
      *
      * @return The given line with the appropriate irc codes appended/prepended to colour it.
      */
     protected static String getColouredString(final String colour, final String line) {
         String res = null;
         if (colour.length() < 3) {
             int num;
 
             try {
                 num = Integer.parseInt(colour);
             } catch (NumberFormatException ex) {
                 num = -1;
             }
 
             if (num >= 0 && num <= 15) {
                 res = String.format("%c%02d%s%1$c", Styliser.CODE_COLOUR, num, line);
             }
         } else if (colour.length() == 6) {
             try {
                 Color.decode('#' + colour);
                 res = String.format("%c%s%s%1$c", Styliser.CODE_HEXCOLOUR, colour, line);
             } catch (NumberFormatException ex) { /* Do Nothing */ }
         }
 
         if (res == null) {
             res = String.format("%c%02d%s%1$c", Styliser.CODE_COLOUR, 14, line);
         }
         return res;
     }
 
     /**
      * Add a line to a file.
      *
      * @param filename Name of file to write to
      * @param format   Format of line to add. (NewLine will be added Automatically)
      * @param args     Arguments for format
      *
      * @return true on success, else false.
      */
     protected boolean appendLine(final String filename, final String format, final Object... args) {
         return appendLine(filename, String.format(format, args));
     }
 
     /**
      * Add a line to a file.
      *
      * @param filename Name of file to write to
      * @param line     Line to add. (NewLine will be added Automatically)
      *
      * @return true on success, else false.
      */
     protected boolean appendLine(final String filename, final String line) {
         final StringBuilder finalLine = new StringBuilder();
 
         if (addtime) {
             String dateString;
             try {
                 final DateFormat dateFormat = new SimpleDateFormat(timestamp);
                 dateString = dateFormat.format(new Date()).trim();
             } catch (IllegalArgumentException iae) {
                 // Default to known good format
                 final DateFormat dateFormat = new SimpleDateFormat("[dd/MM/yyyy HH:mm:ss]");
                 dateString = dateFormat.format(new Date()).trim();
 
                 eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, iae,
                         "Dateformat String '" + timestamp + "' is invalid. For more information: "
                         + "http://java.sun.com/javase/6/docs/api/java/text/SimpleDateFormat.html",
                         ""));
             }
             finalLine.append(dateString);
             finalLine.append(' ');
         }
 
         if (stripcodes) {
             finalLine.append(Styliser.stipControlCodes(line));
         } else {
             finalLine.append(line);
         }
 
         try {
             final BufferedWriter out;
             if (openFiles.containsKey(filename)) {
                 final OpenFile of = openFiles.get(filename);
                 of.lastUsedTime = System.currentTimeMillis();
                 out = of.writer;
             } else {
                 out = new BufferedWriter(new FileWriter(filename, true));
                 openFiles.put(filename, new OpenFile(out));
             }
             out.write(finalLine.toString());
             out.newLine();
             out.flush();
             return true;
         } catch (IOException e) {
             /*
              * Do Nothing
              *
              * Makes no sense to keep adding errors to the logger when we can't write to the file,
              * as chances are it will happen on every incomming line.
              */
         }
         return false;
     }
 
     /**
      * Get the name of the log file for a specific object.
      *
      * @param channel Channel to get the name for
      *
      * @return the name of the log file to use for this object.
      */
     protected String getLogFile(final ChannelInfo channel) {
         final StringBuffer directory = getLogDirectory();
         final StringBuffer file = new StringBuffer();
         if (channel.getParser() != null) {
             addNetworkDir(directory, file, channel.getParser().getNetworkName());
         }
         file.append(sanitise(channel.getName().toLowerCase()));
         return getPath(directory, file, channel.getName());
     }
 
     /**
      * Get the name of the log file for a specific object.
      *
      * @param client Client to get the name for
      *
      * @return the name of the log file to use for this object.
      */
     protected String getLogFile(final ClientInfo client) {
         final StringBuffer directory = getLogDirectory();
         final StringBuffer file = new StringBuffer();
         if (client.getParser() != null) {
             addNetworkDir(directory, file, client.getParser().getNetworkName());
         }
         file.append(sanitise(client.getNickname().toLowerCase()));
         return getPath(directory, file, client.getNickname());
     }
 
     /**
      * Get the name of the log file for a specific object.
      *
      * @param descriptor Description of the object to get a log file for.
      *
      * @return the name of the log file to use for this object.
      */
     protected String getLogFile(@Nullable final String descriptor) {
         final StringBuffer directory = getLogDirectory();
         final StringBuffer file = new StringBuffer();
         final String md5String;
         if (descriptor == null) {
             file.append("null.log");
             md5String = "";
         } else {
             file.append(sanitise(descriptor.toLowerCase()));
             md5String = descriptor;
         }
         return getPath(directory, file, md5String);
     }
 
     /**
      * Gets the path for the given file and directory. Only intended to be used from getLogFile
      * methods.
      *
      * @param directory Log file directory
      * @param file      Log file path
      * @param md5String Log file object MD5 hash
      *
      * @return Name of the log file
      */
     protected String getPath(final StringBuffer directory, final StringBuffer file,
             final String md5String) {
         if (usedate) {
             final String dateFormat = usedateformat;
             final String dateDir = new SimpleDateFormat(dateFormat).format(new Date());
             directory.append(dateDir);
             if (directory.charAt(directory.length() - 1) != File.separatorChar) {
                 directory.append(File.separatorChar);
             }
 
             if (!new File(directory.toString()).exists()
                     && !new File(directory.toString()).mkdirs()) {
                 eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, null,
                         "Unable to create date dirs", ""));
             }
         }
 
         if (filenamehash) {
             file.append('.');
             file.append(md5(md5String));
         }
         file.append(".log");
 
         return directory + file.toString();
     }
 
     /**
      * Sanitises the log file directory.
      *
      * @return Log directory
      */
     private StringBuffer getLogDirectory() {
         final StringBuffer directory = new StringBuffer();
         directory.append(directoryProvider.get());
         if (directory.charAt(directory.length() - 1) != File.separatorChar) {
             directory.append(File.separatorChar);
         }
         return directory;
     }
 
     /**
      * This function adds the networkName to the log file. It first tries to create a directory for
      * each network, if that fails it will prepend the networkName to the filename instead.
      *
      * @param directory   Current directory name
      * @param file        Current file name
      * @param networkName Name of network
      */
     protected void addNetworkDir(final StringBuffer directory, final StringBuffer file,
             final String networkName) {
         if (!networkfolders) {
             return;
         }
 
         final String network = sanitise(networkName.toLowerCase());
 
         boolean prependNetwork = false;
 
         // Check dir exists
         final File dir = new File(directory + network + System.getProperty(
                 "file.separator"));
         if (dir.exists() && !dir.isDirectory()) {
             eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, null,
                     "Unable to create networkfolders dir (file exists instead)", ""));
             // Prepend network name to file instead.
             prependNetwork = true;
         } else if (!dir.exists() && !dir.mkdirs()) {
             eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, null,
                     "Unable to create networkfolders dir", ""));
             prependNetwork = true;
         }
 
         if (prependNetwork) {
             file.insert(0, " -- ");
             file.insert(0, network);
         } else {
             directory.append(network);
             directory.append(System.getProperty("file.separator"));
         }
     }
 
     /**
      * Sanitise a string to be used as a filename.
      *
      * @param name String to sanitise
      *
      * @return Sanitised version of name that can be used as a filename.
      */
     protected static String sanitise(final String name) {
         // Replace illegal chars with
         return name.replaceAll("[^\\w\\.\\s\\-#&_]", "_");
     }
 
     /**
      * Get the md5 hash of a string.
      *
      * @param string String to hash
      *
      * @return md5 hash of given string
      */
     protected static String md5(final String string) {
         try {
             final MessageDigest m = MessageDigest.getInstance("MD5");
             m.update(string.getBytes(), 0, string.length());
             return new BigInteger(1, m.digest()).toString(16);
         } catch (NoSuchAlgorithmException e) {
             return "";
         }
     }
 
     /**
      * Get name to display for channelClient (Taking into account the channelmodeprefix setting).
      *
      * @param channelClient The client to get the display name for
      *
      * @return name to display
      */
     protected String getDisplayName(final ChannelClientInfo channelClient) {
         return getDisplayName(channelClient, "");
     }
 
     /**
      * Get name to display for channelClient (Taking into account the channelmodeprefix setting).
      *
      * @param channelClient The client to get the display name for
      * @param overrideNick  Nickname to display instead of real nickname
      *
      * @return name to display
      */
     protected String getDisplayName(final ChannelClientInfo channelClient, final String overrideNick) {
         if (channelClient == null) {
             return overrideNick.isEmpty() ? "Unknown Client" : overrideNick;
         } else if (overrideNick.isEmpty()) {
             return channelmodeprefix ? channelClient.toString() : channelClient.getClient().
                     getNickname();
         } else {
             return channelmodeprefix ? channelClient.getImportantModePrefix() + overrideNick
                     : overrideNick;
         }
     }
 
     /**
      * Shows the history window for the specified target, if available.
      *
      * @param target The window whose history we're trying to open
      *
      * @return True if the history is available, false otherwise
      */
     protected boolean showHistory(final FrameContainer target) {
         final String descriptor;
 
         if (target instanceof Channel) {
             descriptor = target.getName();
         } else if (target instanceof Query) {
             final Parser parser = target.getConnection().getParser();
             descriptor = parser.getClient(((PrivateChat) target).getHost()).getNickname();
         } else {
             // Unknown component
             return false;
         }
 
         final Path log = Paths.get(getLogFile(descriptor));
 
         if (!Files.exists(log)) {
             // File doesn't exist
             return false;
         }
 
        windowManager.addWindow(target, new HistoryWindow("History", log, target, urlBuilder,
                eventBus, colourManagerFactory, historyLines));
 
         return true;
     }
 
     /** Updates cached settings. */
     public void setCachedSettings() {
         networkfolders = config.getOptionBool(domain, "general.networkfolders");
         filenamehash = config.getOptionBool(domain, "advanced.filenamehash");
         addtime = config.getOptionBool(domain, "general.addtime");
         stripcodes = config.getOptionBool(domain, "general.stripcodes");
         channelmodeprefix = config.getOptionBool(domain, "general.channelmodeprefix");
         autobackbuffer = config.getOptionBool(domain, "backbuffer.autobackbuffer");
         backbufferTimestamp = config.getOptionBool(domain, "backbuffer.timestamp");
         usedate = config.getOptionBool(domain, "advanced.usedate");
         timestamp = config.getOption(domain, "general.timestamp");
         usedateformat = config.getOption(domain, "advanced.usedateformat");
         historyLines = config.getOptionInt(domain, "history.lines");
         colour = config.getOption(domain, "backbuffer.colour");
         backbufferLines = config.getOptionInt(domain, "backbuffer.lines");
     }
 
     /** Open File. */
     private static class OpenFile {
 
         /** Last used time. */
         public long lastUsedTime = System.currentTimeMillis();
         /** Open file's writer. */
         public final BufferedWriter writer;
 
         /**
          * Creates a new open file.
          *
          * @param writer Writer that has file open
          */
         protected OpenFile(final BufferedWriter writer) {
             this.writer = writer;
         }
 
     }
 
 }
