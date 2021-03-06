 /*
  * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
 
 package uk.org.ownage.dmdirc;
 
 import java.awt.Color;
 import java.beans.PropertyVetoException;
 import java.net.URL;
 import java.util.Hashtable;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.swing.ImageIcon;
 import javax.swing.JInternalFrame;
 import javax.swing.SwingUtilities;
 import javax.swing.event.InternalFrameEvent;
 import javax.swing.event.InternalFrameListener;
 
 import uk.org.ownage.dmdirc.actions.ActionManager;
 import uk.org.ownage.dmdirc.actions.CoreActionType;
 import uk.org.ownage.dmdirc.commandparser.CommandManager;
 import uk.org.ownage.dmdirc.commandparser.CommandWindow;
 import uk.org.ownage.dmdirc.identities.ConfigManager;
 import uk.org.ownage.dmdirc.identities.ConfigSource;
 import uk.org.ownage.dmdirc.logger.ErrorLevel;
 import uk.org.ownage.dmdirc.logger.Logger;
 import uk.org.ownage.dmdirc.parser.ChannelInfo;
 import uk.org.ownage.dmdirc.parser.ClientInfo;
 import uk.org.ownage.dmdirc.parser.IRCParser;
 import uk.org.ownage.dmdirc.parser.MyInfo;
 import uk.org.ownage.dmdirc.parser.ParserError;
 import uk.org.ownage.dmdirc.parser.ServerInfo;
 import uk.org.ownage.dmdirc.parser.callbacks.CallbackNotFound;
 import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IChannelSelfJoin;
 import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IErrorInfo;
 import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IGotNetwork;
 import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IMOTDEnd;
 import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IMOTDLine;
 import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IMOTDStart;
 import uk.org.ownage.dmdirc.parser.callbacks.interfaces.INumeric;
 import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IPingFailed;
 import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IPrivateAction;
 import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IPrivateCTCP;
 import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IPrivateCTCPReply;
 import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IPrivateMessage;
 import uk.org.ownage.dmdirc.parser.callbacks.interfaces.IPrivateNotice;
 import uk.org.ownage.dmdirc.parser.callbacks.interfaces.ISocketClosed;
 import uk.org.ownage.dmdirc.ui.MainFrame;
 import uk.org.ownage.dmdirc.ui.ServerFrame;
 import uk.org.ownage.dmdirc.ui.input.TabCompleter;
 import uk.org.ownage.dmdirc.ui.messages.ColourManager;
 import uk.org.ownage.dmdirc.ui.messages.Formatter;
 
 /**
  * The Server class represents the client's view of a server. It maintains
  * a list of all channels, queries, etc, and handles parser callbacks pertaining
  * to the server.
  * @author chris
  */
 public final class Server implements IChannelSelfJoin, IPrivateMessage,
         IPrivateAction, IErrorInfo, IPrivateCTCP, IPrivateCTCPReply,
         InternalFrameListener, ISocketClosed, IPrivateNotice, IMOTDStart,
         IMOTDLine, IMOTDEnd, INumeric, IGotNetwork, IPingFailed,
         FrameContainer {
     
     /**
      * The callbacks that should be registered for server instances.
      */
     private final static String[] callbacks = {
         "OnChannelSelfJoin", "OnErrorInfo", "OnPrivateMessage",
         "OnPrivateAction", "OnPrivateCTCP", "OnPrivateNotice",
         "OnPrivateCTCPReply", "OnSocketClosed", "OnGotNetwork", "OnNumeric",
         "OnMOTDStart", "OnMOTDLine", "OnMOTDEnd", "OnPingFailed"
     };
     
     /**
      * Open channels that currently exist on the server.
      */
     private final Map<String, Channel> channels  = new Hashtable<String, Channel>();
     
     /**
      * Open query windows on the server.
      */
     private final Map<String, Query> queries = new Hashtable<String, Query>();
     
     /**
      * The ServerFrame corresponding to this server.
      */
     private ServerFrame frame;
     
     /**
      * The IRC Parser instance handling this server.
      */
     private IRCParser parser;
     
     /**
      * The raw frame used for this server instance.
      */
     private Raw raw;
     
     /**
      * The name of the server we're connecting to.
      */
     private String serverName;
     
     /**
      * Used to indicate that this server is in the process of closing all of its
      * windows, and thus requests for individual ones to be closed should be
      * ignored.
      */
     private boolean closing;
     
     /**
      * The tabcompleter used for this server.
      */
     private final TabCompleter tabCompleter = new TabCompleter();
     
     /**
      * The icon being used for this server.
      */
     private ImageIcon imageIcon;
     
     /**
      * The last activated internal frame for this server.
      */
     private FrameContainer activeFrame = this;
     
     /**
      * The config manager for this server.
      */
     private ConfigManager configManager;
     
     /**
      * Creates a new instance of Server.
      * @param server The hostname/ip of the server to connect to
      * @param port The port to connect to
      * @param password The server password
      * @param ssl Whether to use SSL or not
      * @param profile The profile to use
      */
     public Server(final String server, final int port, final String password,
             final boolean ssl, final ConfigSource profile) {
         
         serverName = server;
         
         ServerManager.getServerManager().registerServer(this);
         
         configManager = new ConfigManager("", "", server);
         
         frame = new ServerFrame(this);
         frame.setTitle(server + ":" + port);
         frame.setTabCompleter(tabCompleter);
         frame.addInternalFrameListener(this);
         frame.setFrameIcon(imageIcon);
         MainFrame.getMainFrame().addChild(frame);
         
         frame.open();
         
         tabCompleter.addEntries(CommandManager.getServerCommandNames());
         
         connect(server, port, password, ssl, profile);
     }
     
     /**
      * Connects to a new server with the specified details.
      * @param server The hostname/ip of the server to connect to
      * @param port The port to connect to
      * @param password The server password
      * @param ssl Whether to use SSL or not
      * @param profile The profile to use
      */
     public void connect(final String server, final int port, final String password,
             final boolean ssl, final ConfigSource profile) {
         
         if (parser != null && parser.getSocketState() == parser.STATE_OPEN) {
             disconnect(configManager.getOption("general", "quitmessage"));
         }
         
         serverName = server;
         
         configManager = new ConfigManager("", "", server);
         
         final ClassLoader cldr = this.getClass().getClassLoader();
         URL imageURL;
         if (ssl) {
             imageURL = cldr.getResource("uk/org/ownage/dmdirc/res/secure-server.png");
         } else {
             imageURL = cldr.getResource("uk/org/ownage/dmdirc/res/server.png");
         }
         imageIcon = new ImageIcon(imageURL);
         
         // TODO: Use formatter
         frame.addLine("Connecting to " + server + ":" + port);
         sendNotification();
         
         final MyInfo myInfo = new MyInfo();
         myInfo.setNickname(profile.getOption("profile", "nickname"));
         myInfo.setRealname(profile.getOption("profile", "realname"));
         
         final ServerInfo serverInfo = new ServerInfo(server, port, password);
         serverInfo.setSSL(ssl);
         parser = new IRCParser(myInfo, serverInfo);
         
         if (raw == null) {
             raw = new Raw(this);
             MainFrame.getMainFrame().getFrameManager().addRaw(this, raw);
         }
         
         try {
             for(String callback : callbacks) {
                 parser.getCallbackManager().addCallback(callback, this);
             }
             
             parser.getCallbackManager().addCallback("OnDataIn", raw);
             parser.getCallbackManager().addCallback("OnDataOut", raw);
         } catch (CallbackNotFound ex) {
             Logger.error(ErrorLevel.FATAL, "Unable to register server event handlers", ex);
         }
         
         try {
             new Thread(parser).start();
         } catch (IllegalThreadStateException ex) {
             frame.addLine("ERROR: " + ex.getMessage());
         }
         
         updateIgnoreList();
     }
     
     /**
      * Updates the ignore list for this server.
      */
     public void updateIgnoreList() {
         parser.getIgnoreList().clear();
         
         if (configManager.hasOption("network", "ignorelist")) {
             for (String line : configManager.getOption("network", "ignorelist").split("\n")) {
                 parser.getIgnoreList().add(line);
             }
         }
     }
     
     /**
      * Determines whether the server knows of the specified channel.
      * @param channel The channel to be checked
      * @return True iff the channel is known, false otherwise
      */
     public boolean hasChannel(final String channel) {
         return channels.containsKey(channel.toLowerCase(Locale.getDefault()));
     }
     
     /**
      * Retrieves the specified channel belonging to this server.
      * @param channel The channel to be retrieved
      * @return The appropriate channel object
      */
     public Channel getChannel(final String channel) {
         return channels.get(channel.toLowerCase(Locale.getDefault()));
     }
     
     /**
      * Retrieves the parser used for this connection.
      * @return IRCParser this connection's parser
      */
     public IRCParser getParser() {
         return parser;
     }
     
     /**
      * Retrieves the name of this server.
      * @return The name of this server
      */
     public String getName() {
         return serverName;
     }
     
     /**
      * Retrieves the name of this server's network.
      * @return The name of this server's network
      */
     public String getNetwork() {
         return parser.getNetworkName();
     }
     
     /**
      * Retrieves the name of this server's IRCd.
      * @return The name of this server's IRCd
      */
     public String getIrcd() {
         return parser.getIRCD(true);
     }
     
     /**
      * Returns the tab completer for this connection.
      * @return The tab completer for this server
      */
     public TabCompleter getTabCompleter() {
         return tabCompleter;
     }
     
     /**
      * Returns the internal frame belonging to this object.
      * @return This object's internal frame
      */
     public CommandWindow getFrame() {
         return frame;
     }
     
     /**
      * Returns the config manager for this server.
      * @return This server's config manager
      */
     public ConfigManager getConfigManager() {
         return configManager;
     }
     
     /**
      * Adds a line to the server window.
      * @param line line to be added
      */
     public void addLine(final String line) {
         frame.addLine(line);
     }
     
     /**
      * closes this server connection and associated windows.
      * @param reason reason for closing
      */
     public void close(final String reason) {
         // Unregister parser callbacks
         for(String callback : callbacks) {
             parser.getCallbackManager().delCallback(callback, this);
         }
         // Unregister frame callbacks
         frame.removeInternalFrameListener(this);
         // Disconnect from the server
         disconnect(reason);
         // Close all channel windows
         closeChannels();
         // Close all query windows
         closeQueries();
         // Close the raw window
         if (raw != null) {
             raw.close();
         }
         // Unregister ourselves with the server manager
         ServerManager.getServerManager().unregisterServer(this);
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 // Close our own window
                 frame.setVisible(false);
                 MainFrame.getMainFrame().delChild(frame);
                 frame = null;
             }
         });
         // Ditch the parser
         parser = null;
     }
     
     /**
      * Closes this server connection and associated reason, with the default
      * quit message.
      */
     public void close() {
         close(configManager.getOption("general", "quitmessage"));
     }
     
     /**
      * Disconnects from the server.
      * @param reason disconnect reason
      */
     public void disconnect(final String reason) {
         parser.quit(reason);
         
         if (Boolean.parseBoolean(configManager.getOption("general", "closechannelsonquit"))) {
             closeChannels();
         }
         
         if (Boolean.parseBoolean(configManager.getOption("general", "closequeriesonquit"))) {
             closeQueries();
         }
     }
     
     /**
      * closes all open channel windows associated with this server.
      */
     private void closeChannels() {
         closing = true;
         for (Channel channel : channels.values()) {
             channel.closeWindow();
         }
         channels.clear();
         closing = false;
     }
     
     /**
      * closes all open query windows associated with this server.
      */
     private void closeQueries() {
         closing = true;
         for (Query query : queries.values()) {
             query.close();
         }
         queries.clear();
         closing = false;
     }
     
     /**
      * Removes our reference to the raw object (presumably after it has been
      * closed).
      */
     public void delRaw() {
         MainFrame.getMainFrame().getFrameManager().delRaw(this, raw);
         raw = null;
     }
     
     /**
      * Removes a specific channel and window from this server.
      * @param chan channel to remove
      */
     public void delChannel(final String chan) {
         tabCompleter.removeEntry(chan);
         MainFrame.getMainFrame().getFrameManager().delChannel(
                 this, channels.get(chan.toLowerCase(Locale.getDefault())));
         if (!closing) {
             channels.remove(chan.toLowerCase(Locale.getDefault()));
         }
     }
     
     /**
      * Adds a specific channel and window to this server.
      * @param chan channel to add
      */
     private void addChannel(final ChannelInfo chan) {
         final Channel newChan = new Channel(this, chan);
         
         tabCompleter.addEntry(chan.getName());
         channels.put(chan.getName().toLowerCase(), newChan);
         MainFrame.getMainFrame().getFrameManager().addChannel(this, newChan);
         
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 newChan.show();
             }
         });
     }
     
     /**
      * Adds a query query to this server.
      * @param host host of the remote client being queried
      */
     private void addQuery(final String host) {
         final Query newQuery = new Query(this, host);
         
         tabCompleter.addEntry(ClientInfo.parseHost(host));
         queries.put(ClientInfo.parseHost(host), newQuery);
         MainFrame.getMainFrame().getFrameManager().addQuery(this, newQuery);
     }
     
     /**
      * Deletes a query from this server.
      * @param host host of the remote client being queried
      */
     public void delQuery(final String host) {
         tabCompleter.removeEntry(ClientInfo.parseHost(host));
         MainFrame.getMainFrame().getFrameManager().delQuery(this, queries.get(ClientInfo.parseHost(host)));
         if (!closing) {
             queries.remove(ClientInfo.parseHost(host));
         }
     }
     
     /**
      * Determines if the specified frame is owned by this server.
      * @param target internalframe to be checked for ownership
      * @return boolean ownership status
      */
     public boolean ownsFrame(final JInternalFrame target) {
         // Check if it's our server frame
         if (frame.equals(target)) { return true; }
         // Check if it's the raw frame
         if (raw.ownsFrame(target)) { return true; }
         // Check if it's a channel frame
         for (Channel channel : channels.values()) {
             if (channel.ownsFrame(target)) { return true; }
         }
         // Check if it's a query frame
         for (Query query : queries.values()) {
             if (query.ownsFrame(target)) { return true; }
         }
         return false;
     }
     
     /**
      * Sets the specified frame as the most-recently activated.
      * @param source The frame that was activated
      */
     public void setActiveFrame(final FrameContainer source) {
         activeFrame = source;
     }
     
     /**
      * Passes the arguments to the most recently activated frame for this
      * server. If the frame isn't know, or isn't visible, use this frame
      * instead.
      * @param messageType The type of message to send
      * @param args The arguments for the message
      */
     public void addLineToActive(final String messageType, final Object... args) {
         if (activeFrame == null || !activeFrame.getFrame().isVisible()) {
             activeFrame = this;
         }
         
         activeFrame.getFrame().addLine(messageType, args);
         activeFrame.sendNotification();
     }
     
     /**
      * Passes the arguments to all frames for this server.
      * @param messageType The type of message to send
      * @param args The arguments of the message
      */
     public void addLineToAll(final String messageType, final Object... args) {
         for (Channel channel : channels.values()) {
             channel.addLine(messageType, args);
             channel.sendNotification();
         }
         for (Query query : queries.values()) {
             query.addLine(messageType, args);
             query.sendNotification();
         }
         frame.addLine(messageType, args);
         sendNotification();
     }
     
     /**
      * Handles general server notifications (i.e., ones note tied to a
      * specific window). The user can select where the notifications should
      * go in their config.
      * @param messageType The type of message that is being sent
      * @param args The arguments for the message
      */
     public void handleNotification(final String messageType, final Object... args) {
         String target = "server";
         if (configManager.hasOption("notifications", messageType)) {
             final String newTarget = configManager.getOption("notifications", messageType);
             if ("server".equals(newTarget) || "all".equals(newTarget) || "active".equals(newTarget)) {
                 target = newTarget;
             }
         }
         if ("server".equals(target)) {
             frame.addLine(messageType, args);
             sendNotification();
         } else if ("all".equals(target)) {
             addLineToAll(messageType, args);
         } else if ("active".equals(target)) {
             addLineToActive(messageType, args);
         }
     }
     
     /**
      * Event when client joins a channel, creates new channel object and opens
      * a channel window.
      * @param tParser parser instance triggering event
      * @param cChannel Channel being joined
      */
     public void onChannelSelfJoin(final IRCParser tParser, final ChannelInfo cChannel) {
         if (hasChannel(cChannel.getName())) {
             getChannel(cChannel.getName()).setChannelInfo(cChannel);
             getChannel(cChannel.getName()).selfJoin();
         } else {
             addChannel(cChannel);
         }
     }
     
     /**
      * Private message event, creates a new query object and opens a new query
      * window if one doesnt exist.
      * @param tParser parser instance triggering event
      * @param message private message being received
      * @param host host of the remote client
      */
     public void onPrivateMessage(final IRCParser tParser, final String message,
             final String host) {
         if (!queries.containsKey(ClientInfo.parseHost(host))) {
             addQuery(host);
         }
     }
     
     /**
      * Private action event, creates a new query object and opens a new query
      * window if one doesnt exist.
      * @param action action text being received
      * @param tParser parser instance triggering event
      * @param host host of remote client
      */
     public void onPrivateAction(final IRCParser tParser, final String action, final String host) {
         if (!queries.containsKey(ClientInfo.parseHost(host))) {
             addQuery(host);
         }
     }
     
     /**
      * Called when we receive a CTCP request.
      * @param tParser The associated IRC parser
      * @param sType The type of the CTCP
      * @param sMessage The contents of the CTCP
      * @param sHost The source host
      */
     public void onPrivateCTCP(final IRCParser tParser, final String sType,
             final String sMessage, final String sHost) {
         final String[] parts = ClientInfo.parseHostFull(sHost);
         
         handleNotification("privateCTCP", parts[0], parts[1], parts[2], sType, sMessage);
         
         sendCTCPReply(parts[0], sType, sMessage);
     }
     
     /**
      * Replies to an incoming CTCP message.
      * @param source The source of the message
      * @param type The CTCP type
      * @param args The CTCP arguments
      */
     public void sendCTCPReply(final String source, final String type, final String args) {
         if (type.equalsIgnoreCase("VERSION")) {
             parser.sendCTCPReply(source, "VERSION", "DMDirc " + Main.VERSION
                     + " - http://www.dmdirc.com/");
         } else if (type.equalsIgnoreCase("PING")) {
             parser.sendCTCPReply(source, "PING", args);
         } else if (type.equalsIgnoreCase("CLIENTINFO")) {
             parser.sendCTCPReply(source, "CLIENTINFO", "VERSION PING CLIENTINFO");
         }
     }
     
     /**
      * Called when we receive a CTCP reply.
      * @param tParser The associated IRC parser
      * @param sType The type of the CTCPR
      * @param sMessage The contents of the CTCPR
      * @param sHost The source host
      */
     public void onPrivateCTCPReply(final IRCParser tParser, final String sType,
             final String sMessage, final String sHost) {
         final String[] parts = ClientInfo.parseHostFull(sHost);
         handleNotification("privateCTCPreply", parts[0], parts[1], parts[2], sType, sMessage);
     }
     
     /**
      * Called when we receive a notice.
      * @param tParser The associated IRC parser
      * @param sMessage The notice content
      * @param sHost The source of the message
      */
     public void onPrivateNotice(final IRCParser tParser, final String sMessage,
             final String sHost) {
         final String[] parts = ClientInfo.parseHostFull(sHost);
         handleNotification("privateNotice", parts[0], parts[1], parts[2], sMessage);
     }
     
     /**
      * Called when the parser has determined the network and ircd version.
      * @param tParser The associated IRC parser
      * @param networkName The name of the network
      * @param ircdVersion The version of the IRCd
      * @param ircdType The type of the IRCd
      */
     public void onGotNetwork(final IRCParser tParser, final String networkName,
             final String ircdVersion, final String ircdType) {
         configManager = new ConfigManager(ircdType, networkName, serverName);
         
         updateIgnoreList();
     }
     
     /**
      * Called when the server's MOTD is starting to be sent.
      * @param tParser The associated IRC parser
      * @param sData The message at the start of the MOTD
      */
     public void onMOTDStart(final IRCParser tParser, final String sData) {
         frame.addLine("motdStart", sData);
         sendNotification();
     }
     
     /**
      * Called when a line of the server's MOTD has been received.
      * @param tParser The associated IRC parser
      * @param sData The line of the MOTD
      */
     public void onMOTDLine(final IRCParser tParser, final String sData) {
         frame.addLine("motdLine", sData);
         sendNotification();
     }
     
     /**
      * Called when the server's MOTD is over.
      * @param tParser The associated IRC parser
      * @param noMOTD Indicates that there was no MOTD
      */
     public void onMOTDEnd(final IRCParser tParser, final boolean noMOTD) {
         frame.addLine("motdEnd", "End of server's MOTD.");
         sendNotification();
     }
     
     /**
      * Called when we receive a numeric response from the server.
      * @param tParser The associated IRC parser
      * @param numeric The numeric of the message
      * @param line The tokenised line
      */
     public void onNumeric(final IRCParser tParser, final int numeric,
             final String[] line) {
         final String withIrcd = "numeric_" + tParser.getIRCD(true) + "_" + numeric;
         final String sansIrcd = "numeric_" + numeric;
         String target = null;
         
         if (Formatter.hasFormat(withIrcd)) {
             target = withIrcd;
         } else if (Formatter.hasFormat(sansIrcd)) {
             target = sansIrcd;
         } else if (Formatter.hasFormat("numeric_unknown")) {
             target = "numeric_unknown";
         }
         
         if (target != null) {
             handleNotification(target, (Object[]) line);
         }
         
         if (numeric == 1) {
             ActionManager.processEvent(CoreActionType.SERVER_CONNECTED, this);
         }
     }
     
     /**
      * Called when the IRC socket is closed for any reason.
      * @param tParser The IRC parser for this server
      */
     public void onSocketClosed(final IRCParser tParser) {
         handleNotification("socketClosed", serverName);
         
         if (Boolean.parseBoolean(configManager.getOption("general", "closechannelsondisconnect"))) {
             closeChannels();
         }
         
         if (Boolean.parseBoolean(configManager.getOption("general", "closequeriesondisconnect"))) {
             closeQueries();
         }
     }
     
     /**
      * Called when the parser misses a ping reply from the server.
      * @param parser The IRC parser for this server
      */
     public void onPingFailed(final IRCParser parser) {
         MainFrame.getMainFrame().getStatusBar().setMessage("No ping reply from "
                 + serverName + " for over "
                + Math.floor(parser.getPingTime(false) / 1000.0) + " seconds.", null, 10);
     }
     
     /**
      * Parses the parser error and notifies the Logger.
      * @param tParser parser instance triggering event
      * @param errorInfo Parser error object
      */
     public void onErrorInfo(final IRCParser tParser, final ParserError errorInfo) {
         ErrorLevel errorLevel;
         if (errorInfo.isFatal()) {
             errorLevel = ErrorLevel.FATAL;
         } else if (errorInfo.isError()) {
             errorLevel = ErrorLevel.ERROR;
         } else if (errorInfo.isWarning()) {
             errorLevel = ErrorLevel.WARNING;
         } else {
             Logger.error(ErrorLevel.WARNING,
                     "Unknown error level for parser error: " + errorInfo.getData());
             return;
         }
         
         if (errorInfo.isException()) {
             Logger.error(errorLevel, errorInfo.getData(), errorInfo.getException());
         } else {
             Logger.error(errorLevel, errorInfo.getData());
         }
     }
     
     /**
      * Called when the server frame is opened. Checks config settings to
      * determine if the window should be maximised.
      * @param internalFrameEvent The event that triggered this callback
      */
     public void internalFrameOpened(final InternalFrameEvent internalFrameEvent) {
         final Boolean pref = Boolean.parseBoolean(configManager.getOption("ui", "maximisewindows"));
         if (pref || MainFrame.getMainFrame().getMaximised()) {
             try {
                 frame.setMaximum(true);
             } catch (PropertyVetoException ex) {
                 Logger.error(ErrorLevel.WARNING, "Unable to maximise server", ex);
             }
         }
     }
     
     /**
      * Called when the server frame is being closed. Has the parser quit
      * the server, close all channels, and free all resources.
      * @param internalFrameEvent The event that triggered this callback
      */
     public void internalFrameClosing(final InternalFrameEvent internalFrameEvent) {
         close(configManager.getOption("general", "quitmessage"));
     }
     
     /**
      * Called when the server frame is actually closed. Not implemented.
      * @param internalFrameEvent The event that triggered this callback
      */
     public void internalFrameClosed(final InternalFrameEvent internalFrameEvent) {
         //Ignore.
     }
     
     /**
      * Called when the server frame is iconified. Not implemented.
      * @param internalFrameEvent The event that triggered this callback
      */
     public void internalFrameIconified(final InternalFrameEvent internalFrameEvent) {
         //Ignore.
     }
     
     /**
      * Called when the server frame is deiconified. Not implemented.
      * @param internalFrameEvent The event that triggered this callback
      */
     public void internalFrameDeiconified(final InternalFrameEvent internalFrameEvent) {
         //Ignore.
     }
     
     /**
      * Called when the server frame is activated. Maximises the frame if it
      * needs to be.
      * @param internalFrameEvent The event that triggered this callback
      */
     public void internalFrameActivated(final InternalFrameEvent internalFrameEvent) {
         if (MainFrame.getMainFrame().getMaximised()) {
             try {
                 frame.setMaximum(true);
             } catch (PropertyVetoException ex) {
                 Logger.error(ErrorLevel.WARNING, "Unable to maximise server", ex);
             }
         }
         MainFrame.getMainFrame().getFrameManager().setSelected(this);
         setActiveFrame(this);
         clearNotification();
     }
     
     /**
      * Called when the server frame is deactivated. Not implemented.
      * @param internalFrameEvent The event that triggered this callback
      */
     public void internalFrameDeactivated(final InternalFrameEvent internalFrameEvent) {
         //Ignore.
     }
     
     /**
      * Returns this server's name.
      * @return A string representation of this server (i.e., its name)
      */
     public String toString() {
         return serverName;
     }
     
     /**
      * Requests that this object's frame be activated.
      */
     public void activateFrame() {
         MainFrame.getMainFrame().setActiveFrame(frame);
     }
     
     /**
      * Returns the server frame's icon.
      * @return The server frame's icon
      */
     public ImageIcon getIcon() {
         return imageIcon;
     }
     
     /**
      * Sends a notification to the frame manager if this frame isn't active.
      */
     public void sendNotification() {
         if (!MainFrame.getMainFrame().getActiveFrame().equals(frame)) {
             final Color colour = ColourManager.getColour(4);
             MainFrame.getMainFrame().getFrameManager().showNotification(this, colour);
         }
     }
     
     /**
      * Clears any outstanding notifications this frame has set.
      */
     private void clearNotification() {
         MainFrame.getMainFrame().getFrameManager().clearNotification(this);
     }
     
     /**
      * Returns the server instance associated with this frame.
      *
      * @return the associated server connection
      */
     public Server getServer() {
         return this;
     }
     
 }
