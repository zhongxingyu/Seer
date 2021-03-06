 package in.mDev.MiracleM4n.mChatSuite;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.Plugin;
 
 import java.io.File;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.UUID;
 import java.util.logging.*;
 
 public class Stats {
     static File configFile = new File("plugins/mChatSuite/stats.yml");
     static String logFile = "plugins/mChatSuite/statsLog/stats.log";
     static YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
     static Logger logger = Logger.getLogger("net.D3GN");
     static FileHandler logFileHandler;
 
     public static void init(Plugin plugin) {
         if (new File("plugins/mChat/").isDirectory()) {
             configFile = new File("plugins/mChat/stats.yml");
             logFile = "plugins/mChat/statsLog/stats.log";
         } else {
             configFile = new File("plugins/mChatSuite/stats.yml");
             logFile = "plugins/mChatSuite/statsLog/stats.log";
         }
 
         if (configExists(plugin) && logExists() && !config.getBoolean("opt-out")) {
             plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Pinger(plugin, config.getString("guid"), logger), 10L, 20L * 60L * 60 * 24);
             System.out.println("[" + plugin.getDescription().getName() + "] Stats are being kept for this plugin. To opt-out check stats.yml.");
         }
     }
 
     public static void unload() {
         if (logFileHandler != null)
             logFileHandler.close();
     }
 
     static Boolean configExists(Plugin plugin) {
         config.addDefault("opt-out", false);
         config.addDefault("guid", UUID.randomUUID().toString());
        if (!configFile.exists() || config.get("guid") == null) {
             System.out.println("[" + plugin.getDescription().getName() + "] Stats is initializing for the first time. To opt-out check stats.yml.");
             try {
                 config.options().copyDefaults(true);
                 config.save(configFile);
             } catch (Exception ignored) {
                 System.out.println("[" + plugin.getDescription().getName() + "] Error creating Stats configuration file.");
                 return false;
             }
         }

         return true;
     }
 
     @SuppressWarnings({"ResultOfMethodCallIgnored"})
     static Boolean logExists() {
         try {
             File log;
 
             if (new File("plugins/mChat/").isDirectory())
                 log = new File("plugins/mChat/statsLog/");
             else
                 log = new File("plugins/mChatSuite/statsLog/");
 
 
             log.mkdir();
 
             logFileHandler = new FileHandler(logFile, true);
 
             logFileHandler.setFormatter(new Formatter() {
                 @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
                 public String format(LogRecord record) {
                     StringBuilder builder = new StringBuilder();
                     Throwable ex = record.getThrown();
 
                     builder.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(record.getMillis()));
                     builder.append(" [");
                     builder.append(record.getLevel().getLocalizedName().toUpperCase());
                     builder.append("] ");
                     builder.append(record.getMessage());
                     builder.append('\n');
 
                     if (ex != null) {
                         StringWriter writer = new StringWriter();
                         ex.printStackTrace(new PrintWriter(writer));
                         builder.append(writer);
                     }
 
                     return builder.toString();
                 }
             });
 
             for (Handler gHandler: logger.getHandlers())
                 logger.removeHandler(gHandler);
 
             logger.setUseParentHandlers(false);
             logger.addHandler(logFileHandler);
         } catch (Exception ex) {
             System.out.println("Error creating Stats log file.");
             logger.log(Level.SEVERE, ex.getMessage(), ex);
 
             return false;
         }
 
         return true;
     }
 }
 
 class Pinger implements Runnable {
     Plugin plugin;
     String guid;
     Logger logger;
 
     public Pinger(Plugin plugin, String guid, Logger theLogger) {
         this.plugin = plugin;
         this.guid = guid;
         this.logger = theLogger;
     }
 
     public void run() {
         pingServer();
     }
 
     void pingServer() {
         String authors = "";
         String plugins = "";
 
         try {
             for (Plugin pluginZ : plugin.getServer().getPluginManager().getPlugins())
                 plugins += " " + pluginZ.getDescription().getName() + " " + pluginZ.getDescription().getVersion() + ",";
 
             for (String auth : plugin.getDescription().getAuthors())
                 authors += " " + auth + ",";
 
             plugins = plugins.trim();
             plugins = plugins.substring(0, (plugins.length() - 1)) + ".";
 
             authors = authors.trim();
             authors = authors.substring(0, (authors.length() - 1)) + ".";
 
             String url = String.format("http://mdev.in/update.php?server=%s&ip=%s&port=%s&hash=%s&bukkit=%s&players=%s&name=%s&authors=%s&plugins=%s&version=%s",
                     URLEncoder.encode(plugin.getServer().getServerName(), "UTF-8"),
                     URLEncoder.encode(plugin.getServer().getIp(), "UTF-8"),
                     plugin.getServer().getPort(),
                     URLEncoder.encode(guid, "UTF-8"),
                     URLEncoder.encode(Bukkit.getVersion(), "UTF-8"),
                     plugin.getServer().getOnlinePlayers().length,
                     URLEncoder.encode(plugin.getDescription().getName(), "UTF-8"),
                     URLEncoder.encode(authors, "UTF-8"),
                     URLEncoder.encode(plugins, "UTF-8"),
                     URLEncoder.encode(plugin.getDescription().getVersion(), "UTF-8"));
 
             new URL(url).openConnection().getInputStream();
             logger.log(Level.INFO, "Stats pinged the central server.");
 
         } catch (Exception ex) {
             logger.log(Level.SEVERE, ex.getMessage(), ex);
         }
     }
 }
