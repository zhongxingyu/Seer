 package us.fitzpatricksr.cownet;
 
 import org.bukkit.GameMode;
 import org.bukkit.command.Command;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.*;
 import org.bukkit.event.server.PluginDisableEvent;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.io.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 public class LoginHistory extends CowNetThingy implements Listener {
     enum Filter {
         IN,
         OUT,
         INOUT,
         CHAT,
         ALL;
 
         public boolean shouldDisplayEntry(LogEntry entry, String filter) {
             return ((this == ALL) ||
                     (this == IN || this == INOUT) && entry.isJoin()) ||
                     ((this == OUT || this == INOUT) && entry.isQuit()) ||
                     ((this == CHAT) && entry.isChat(filter));
         }
     }
 
     private static final String JOIN_STRING = "<JOIN>";
     private static final String QUIT_STRING = "<QUIT>";
     private static final int MAX_QUEUE_SIZE = 50;
     private Plugin plugin;
     private HashMap<String, GameMode> gameModeSave = new HashMap<String, GameMode>();
     private PrintWriter log;
     private LinkedList<LogEntry> recentLogEntries = new LinkedList<LogEntry>();
 
     public LoginHistory(JavaPlugin plugin, String permissionRoot, String trigger) {
         super(plugin, permissionRoot, trigger);
         this.plugin = plugin;
         if (isEnabled()) {
             PluginManager pm = plugin.getServer().getPluginManager();
             pm.registerEvents(this, plugin);
         }
     }
 
     @Override
     protected String getHelpString(Player player) {
         return "usage: <command> [-in|-out|-inout] | [<filter>]";
     }
 
     @EventHandler(priority = EventPriority.LOW)
     public void onEnable(PluginEnableEvent event) {
         Plugin pl = event.getPlugin();
         if (pl.equals(plugin)) {
             try {
                 BufferedReader reader = new BufferedReader(new FileReader(getLogFile()));
                 for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                     LogEntry entry = new LogEntry(line);
                     queueEntry(entry);
                    logInfo("Logins loaded: " + line + " as: " + entry.toHumanReadableString());
                 }
                 reader.close();
                 log = new PrintWriter(new BufferedWriter(new FileWriter(getLogFile(), true)));
             } catch (IOException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
         }
     }
 
     @EventHandler(priority = EventPriority.LOW)
     public void onDisable(PluginDisableEvent event) {
         Plugin pl = event.getPlugin();
         if (pl.equals(plugin)) {
             if (log != null) {
                 log.close();
                 log = null;
             }
         }
     }
 
     @EventHandler(priority = EventPriority.LOW)
     public void onPlayerLogin(PlayerLoginEvent event) {
         if (log != null) {
             LogEntry entry = new LogEntry().forUser(event.getPlayer()).forMessage(JOIN_STRING);
             log.println(entry.toString());
             log.flush();
             queueEntry(entry);
         }
         setGameMode(event.getPlayer());
     }
 
     @EventHandler(priority = EventPriority.LOW)
     public void onPlayerQuit(PlayerQuitEvent event) {
         if (log != null) {
             LogEntry entry = new LogEntry().forUser(event.getPlayer()).forMessage(QUIT_STRING);
             log.println(entry.toString());
             log.flush();
             queueEntry(entry);
         }
         saveGameMode(event.getPlayer());
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerChat(PlayerChatEvent event) {
         if (log != null) {
             LogEntry entry = new LogEntry().forUser(event.getPlayer()).forMessage(event.getMessage());
             log.println(entry.toString());
             log.flush();
             queueEntry(entry);
         }
     }
 
     @EventHandler(priority = EventPriority.LOW)
     public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
         if (log != null) {
             LogEntry entry = new LogEntry().forUser(event.getPlayer()).forMessage(event.getMessage());
             log.println(entry.toString());
             log.flush();
             queueEntry(entry);
         }
     }
 
     @EventHandler(priority = EventPriority.LOW)
     public void onPlayerCommand(PlayerGameModeChangeEvent event) {
         if (log != null) {
             LogEntry entry = new LogEntry().forUser(event.getPlayer()).forMessage("set game mode: " + event.getNewGameMode());
             log.println(entry.toString());
             log.flush();
             queueEntry(entry);
         }
     }
 
     @EventHandler(priority = EventPriority.LOW)
     public void onPlayerTeleport(PlayerTeleportEvent event) {
         if (log != null) {
             LogEntry entry = new LogEntry().forUser(event.getPlayer()).forMessage("Teleported: " + event.getPlayer().getWorld().getName());
             log.println(entry.toString());
             log.flush();
             queueEntry(entry);
         }
     }
 
     @Override
     protected boolean onCommand(Command cmd, String[] args) {
         return onCommand(null, cmd, args);
     }
 
     @Override
     protected boolean onCommand(Player player, Command cmd, String[] args) {
         if (args.length > 1) return false;
         Filter filterType = Filter.INOUT;
         String filterString = null;
         if (args.length > 0) {
             filterString = args[0];
             // it's -in, -out, or a filter
             if (filterString.equalsIgnoreCase("-in")) {
                 filterType = Filter.IN;
             } else if (filterString.equalsIgnoreCase("-out")) {
                 filterType = Filter.OUT;
             } else if (filterString.equalsIgnoreCase("-inout")) {
                 filterType = Filter.INOUT;
             } else if (filterString.equalsIgnoreCase("-all")) {
                 filterType = Filter.ALL;
             } else {
                 filterType = Filter.CHAT;
             }
         }
 
         for (LogEntry entry : recentLogEntries) {
             if (filterType.shouldDisplayEntry(entry, filterString)) {
                 if (player != null) {
                     player.sendMessage(entry.toHumanReadableString());
                 } else {
                     logInfo(entry.toHumanReadableString());
                 }
             }
         }
 
         return true;
     }
 
     private File getLogFile() throws IOException {
         File folder = plugin.getDataFolder();
         if (!folder.exists()) {
             folder.mkdir();
         }
         File file = new File(folder, "ChatLog-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".txt");
         if (!file.exists()) {
             file.createNewFile();
         }
         return file;
     }
 
     private void queueEntry(LogEntry e) {
         recentLogEntries.addLast(e);
         if (recentLogEntries.size() > MAX_QUEUE_SIZE) {
             recentLogEntries.removeFirst();
         }
     }
 
     private static class LogEntry {
         private static DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
         private String playerName;
         private String worldName;
         private String time;
         private String message;
 
         public LogEntry() {
             time = dateFormat.format(new Date());
         }
 
         public LogEntry(String line) {
             fromString(line);
         }
 
         public LogEntry forUser(Player p) {
             playerName = p.getName();
             worldName = p.getWorld().getName();
             return this;
         }
 
         public LogEntry forMessage(String message) {
             this.message = message;
             return this;
         }
 
        public LogEntry fromString(String line) {
            String[] args = line.split("\\|");
             time = args[0];
             worldName = args[1];
             playerName = args[2];
             message = args[3];
             return this;
         }
 
         public String toString() {
             return time + "|" + worldName + "|" + playerName + "|" + message;
         }
 
         private String toHumanReadableString() {
            return time + " [" + worldName + "] " + playerName + ": " + message;
         }
 
         public boolean isJoin() {
             return JOIN_STRING.equalsIgnoreCase(message);
         }
 
         public boolean isQuit() {
             return QUIT_STRING.equalsIgnoreCase(message);
         }
 
         public boolean isChat() {
             return isChat(null);
         }
 
         public boolean isChat(String filter) {
             if (isJoin() || isQuit()) return false;
             return (filter == null) || toHumanReadableString().contains(filter) || filter.equals("-chat");
         }
 
         public String getMessage() {
             return message;
         }
     }
 
     //--------------------------------------------------
     private void setGameMode(Player player) {
         GameMode oldMode = gameModeSave.get(player.getPlayerListName());
         if (oldMode != null) {
             player.setGameMode(oldMode);
             logInfo("Restoring game mode for " + player.getPlayerListName() + " " + oldMode);
         }
     }
 
     private void saveGameMode(Player player) {
         gameModeSave.put(player.getPlayerListName(), player.getGameMode());
     }
 
 }
