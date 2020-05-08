 package edgruberman.bukkit.simpleblacklist;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 
 import edgruberman.bukkit.messagemanager.MessageLevel;
 import edgruberman.bukkit.messagemanager.MessageManager;
 
 public class Main extends org.bukkit.plugin.java.JavaPlugin {
     
     static ConfigurationFile configurationFile;
     static MessageManager messageManager;
     
     private static Map<Integer, BlacklistEntry> blacklist = new HashMap<Integer, BlacklistEntry>();
     
     public void onLoad() {
         Main.configurationFile = new ConfigurationFile(this);
         Main.configurationFile.load();
         
         Main.messageManager = new MessageManager(this);
         Main.messageManager.log("Version " + this.getDescription().getVersion());
     }
     
     public void onEnable() {
         this.loadBlacklist();
         
         new PlayerListener(this);
         new BlockListener(this);
 
         Main.messageManager.log("Plugin Enabled");
     }
     
     public void onDisable() {
         Main.messageManager.log("Plugin Disabled");
     }
     
     @SuppressWarnings("unchecked")
     private void loadBlacklist() {
         for (String id : Main.configurationFile.getConfig().getConfigurationSection("blacklist").getKeys(false)) {
             String message = Main.configurationFile.getConfig().getString("blacklist." + id + ".message", null);
            List<String> access = Main.configurationFile.getConfig().getList("blacklist." + id + ".allow", null);
             Main.blacklist.put(Integer.parseInt(id), new BlacklistEntry(Integer.parseInt(id), message, access));
         }
     }
     
     static boolean isAllowed(Player player, Material item) {
         // For material not in the blacklist, it's allowed.
         if (!Main.blacklist.containsKey(item.getId())) return true;
         
         // For players specifically defined, it's allowed.
         if (Main.blacklist.get(item.getId()).isAllowed(player)) return true;
         
         // Otherwise, no!
         return false;
     }
     
     static void notify(Player player, Material material) {
         Main.messageManager.log(
                 player.getName() + " attempted to use blacklisted " + material.toString()
                 , MessageLevel.RIGHTS
         );
         
         String message = Main.blacklist.get(material.getId()).getMessage();
         if (message.length() != 0)
             Main.messageManager.send(player, message, MessageLevel.RIGHTS);
     }
 }
