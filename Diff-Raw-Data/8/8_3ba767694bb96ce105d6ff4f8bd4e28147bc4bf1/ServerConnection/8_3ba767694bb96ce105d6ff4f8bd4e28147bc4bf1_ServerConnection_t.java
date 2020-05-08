 
 package me.heldplayer.irc;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import me.heldplayer.irc.api.BotAPI;
 import me.heldplayer.irc.api.IRCMessage;
 import me.heldplayer.irc.api.IServerConnection;
 import me.heldplayer.irc.api.Network;
 import me.heldplayer.irc.api.event.EventHandler;
 import me.heldplayer.irc.api.event.connection.ServerConnectedEvent;
 import me.heldplayer.irc.api.event.connection.ServerDisconnectedEvent;
 import me.heldplayer.irc.api.event.connection.ServerLoggedInEvent;
 import me.heldplayer.irc.api.event.user.CommandEvent;
 import me.heldplayer.irc.api.event.user.RawMessageEvent;
 import me.heldplayer.irc.api.event.user.SelfNicknameChangedEvent;
 
 class ServerConnection implements IServerConnection {
 
     private static final Logger log = Logger.getLogger("RawIRC");
 
     private List<String> sendQueue;
     private boolean connected;
     private boolean initialized;
     private boolean shouldQuit;
 
     private Network network;
 
     public Socket socket;
     public PrintWriter out;
     public BufferedReader in;
     private long lastRead;
 
     private String host;
     private int port;
     private String localHost;
     private String nickname;
 
     private RunnableCommitReader commitReader;
 
     public ServerConnection(String host, int port, String localHost) {
         this.sendQueue = Collections.synchronizedList(new LinkedList<String>());
         this.connected = false;
         this.initialized = false;
         this.shouldQuit = false;
         this.host = host;
         this.port = port;
         this.localHost = localHost;
     }
 
     public void connect(String nickname) throws UnknownHostException, IOException {
         this.network = new Network(host);
 
         InetAddress remote = InetAddress.getByName(this.host);
 
         if (this.localHost != null && !this.localHost.isEmpty()) {
             InetAddress local = InetAddress.getByName(this.localHost);
             this.socket = new Socket(remote, this.port, local, 0);
         }
         else {
             this.socket = new Socket(remote, this.port);
         }
 
         this.socket.setKeepAlive(true);
         this.out = new PrintWriter(this.socket.getOutputStream(), true);
         this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));
 
         this.nickname = nickname;
         this.connected = true;
         this.lastRead = System.currentTimeMillis();
 
         BotAPI.eventBus.equals(new ServerConnectedEvent(this));
 
         this.addToSendQueue("NICK " + nickname);
         this.addToSendQueue("USER HeldBot 0 * :" + nickname);
     }
 
     @EventHandler
     public void onCommand(CommandEvent event) {
         if (event.command.equals("GIT")) {
             if (event.params.length == 1) {
                 this.commitReader = new RunnableCommitReader(event.params[0]);
 
                 Thread commitReader = new Thread(this.commitReader);
                 commitReader.setName("Commit Reader");
                 commitReader.start();
             }
             else {
                 BotAPI.console.log(Level.WARNING, "Expected 1 parameter for command /git");
             }
             event.setHandled();
         }
     }
 
     @EventHandler
     public void onRawMessage(RawMessageEvent event) {
         if (event.message.command.equals("PING")) {
             BotAPI.serverConnection.addToSendQueue("PONG :" + event.message.trailing);
             event.setHandled();
         }
         else if (event.message.command.equals("ERROR")) {
             this.connected = this.initialized = false;
             BotAPI.eventBus.postEvent(new ServerDisconnectedEvent(this));
         }
         else if (event.message.command.equals("001")) {
             this.initialized = true;
 
             List<String> perform = IRCBotLauncher.readPerform();
             for (String action : perform) {
                 BotAPI.console.handleConsoleInput(action);
             }
 
             BotAPI.eventBus.postEvent(new ServerLoggedInEvent(this));
         }
         else if (event.message.command.equals("433")) {
             this.nickname += "_";
             this.addToSendQueue("NICK " + this.nickname);
         }
         else if (event.message.command.equals("005")) {
             for (int i = 1; i < event.message.params.length; i++) {
                 String param = event.message.params[i];
                 String[] parts = param.split("=", 2);
                 if (parts[0].startsWith("NETWORK")) {
                     this.network.name = parts[1];
                 }
                 else if (parts[0].startsWith("MODES")) {
                     this.network.maxChannelModes = Integer.parseInt(parts[1]);
                 }
                 else if (parts[0].startsWith("CHANTYPES")) {
                     this.network.availableChannelTypes = parts[1].toCharArray();
                 }
                 else if (parts[0].startsWith("PREFIX")) {
                     String modeNames = parts[1].substring(parts[1].indexOf('(') + 1, parts[1].indexOf(')'));
                     String mdePrefixes = parts[1].substring(parts[1].indexOf(')') + 1);
                     char[] modes = modeNames.toCharArray();
                     char[] prefixes = mdePrefixes.toCharArray();
                     this.network.prefixes = new char[modes.length][];
                     for (int j = 0; j < modes.length; j++) {
                         this.network.prefixes[j] = new char[] { modes[j], prefixes[j] };
                     }
                 }
                 else if (parts[0].startsWith("CHANMODES")) {
                     char[] modes = parts[1].toCharArray();
                     int count = modes.length;
                     for (char mode : modes) {
                         if (mode == ',') {
                             count--;
                         }
                     }
                     this.network.availableChannelModes = new char[count];
                     count = 0;
                     for (char mode : modes) {
                         if (mode != ',') {
                             this.network.availableChannelModes[count] = mode;
                             count++;
                         }
                     }
                 }
             }
         }
         else if (event.message.command.equals("NICK")) {
             BotAPI.eventBus.postEvent(new SelfNicknameChangedEvent(event.message.trailing, this.nickname));
             this.nickname = event.message.trailing;
         }
         else if (event.message.command.equals("PRIVMSG")) {
             event.setHandled();
             String[] sender = event.message.prefix.split("!");
             BotAPI.console.sendMessageToConsole("[" + event.message.params[0] + "] <" + sender[0] + "> " + event.message.trailing);
         }
     }
 
     @EventHandler
     public void onServerDisconnected(ServerDisconnectedEvent event) {
         if (event.connection == this) {
             this.commitReader.running = false;
             try {
                 this.in.close();
                 this.out.close();
                 this.socket.close();
             }
             catch (IOException e) {
                 e.printStackTrace();
             }
 
             Thread consoleReader = new Thread(new RunnableReconnectTimeout(this));
             consoleReader.setName("Reconnect Thread");
             consoleReader.setDaemon(true);
             consoleReader.start();
         }
     }
 
     @Override
     public void addToSendQueue(String command) {
         synchronized (this.sendQueue) {
             IRCMessage message = new IRCMessage(command);
             if (message.command.equalsIgnoreCase("QUIT")) {
                 this.shouldQuit = true;
             }
             else if (message.command.equalsIgnoreCase("ERROR")) {
                 this.shouldQuit = true;
                 command = "QUIT :Errored: " + message.trailing;
             }
             this.sendQueue.add(command);
         }
     }
 
     @Override
     public void processQueue() {
         try {
             while (this.connected && this.in.ready()) {
                 String line = this.in.readLine();
                 IRCMessage message = new IRCMessage(line);
                 RawMessageEvent event = new RawMessageEvent(message);
                 if (BotAPI.eventBus.postEvent(event)) {
                     BotAPI.console.log(Level.FINER, "-> " + message.toString());
                 }
                 log.log(Level.INFO, "-> " + line);
                 this.lastRead = System.currentTimeMillis();
             }
         }
         catch (IOException e) {
             throw new RuntimeException("Error parsing incoming data", e);
         }
 
         if (this.connected) {
             if (System.currentTimeMillis() - this.lastRead > 300000L) {
                 this.disconnect("Connection timed out");
                 BotAPI.eventBus.postEvent(new ServerDisconnectedEvent(this));
                 return;
             }
 
             synchronized (this.sendQueue) {
                 Iterator<String> iterator = this.sendQueue.iterator();
                 long incremental = 0L;
                 while (iterator.hasNext()) {
                     String command = iterator.next();
 
                     this.out.println(command.trim());
 
                     if (!command.startsWith("PING") && !command.startsWith("PONG")) {
                         BotAPI.console.log(Level.FINER, "<- " + command);
                     }
                     log.log(Level.INFO, "<- " + command);
 
                     try {
                         Thread.sleep(250L * incremental);
                     }
                     catch (InterruptedException e) {}
                     incremental++;
                    this.lastRead = System.currentTimeMillis();
                 }
                 this.sendQueue.clear();
             }
         }
 
         if (this.shouldQuit) {
             try {
                 Thread.sleep(1000L);
             }
             catch (InterruptedException e) {
                 e.printStackTrace();
             }
             BotAPI.console.shutdown();
         }
     }
 
     @Override
     public Network getNetwork() {
         return this.network;
     }
 
     @Override
     public String getNickname() {
         return this.nickname;
     }
 
     @Override
     public void setNickname(String nickname) {
         if (this.connected) {
             this.addToSendQueue("NICK " + nickname);
         }
         else {
             this.nickname = nickname;
         }
     }
 
     @Override
     public void disconnect() {
         this.sendQueue.add("QUIT");
         this.connected = this.initialized = false;
     }
 
     @Override
     public void disconnect(String reason) {
         this.sendQueue.add("QUIT :" + reason);
         this.connected = this.initialized = false;
     }
 
     @Override
     public boolean isConnected() {
         return this.connected;
     }
 
     @Override
     public boolean isLoggedIn() {
         return this.initialized;
     }
 
 }
