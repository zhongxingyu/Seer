 package net.n4th4.bukkit.nuxsms;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 
 import org.bukkit.ChatColor;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 public class NuxSMS extends JavaPlugin {
     private final NSPlayerListener playerListener = new NSPlayerListener(this);
    public final Logger            log            = this.getServer().getLogger();
     public String                  message        = "";
     public Pattern                 regex          = Pattern.compile(".*(MDR)|(LOL).*", Pattern.CASE_INSENSITIVE);
 
     public void onEnable() {
         loadConfig();
 
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Normal, this);
     }
 
     public void onDisable() {
     }
 
     private void loadConfig() {
         File configFile = new File("plugins/NuxSMS/config.yml");
         if (configFile.exists()) {
             Configuration config = new Configuration(configFile);
             config.load();
             message = ChatColor.RED + "[NuxSMS] " + config.getString("message", "");
             ArrayList<String> regexsList = (ArrayList<String>) config.getStringList("words", new ArrayList<String>());
             String regexString = ".*(";
             for (int i = 0; i < regexsList.size(); i++) {
                 regexString += regexsList.get(i) + "|";
             }
             regexString = regexString.substring(0, regexString.length() - 1);
             regexString += ").*";
             regex = Pattern.compile(regexString, Pattern.CASE_INSENSITIVE);
         } else {
             log.severe("[NuxSMS] File not found : plugins/NuxSMS/config.yml");
         }
     }
 }
