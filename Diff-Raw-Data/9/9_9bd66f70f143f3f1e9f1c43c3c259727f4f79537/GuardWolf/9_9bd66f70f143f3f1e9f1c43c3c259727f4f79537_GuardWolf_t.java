 
 package net.loadingchunks.plugins.GuardWolf;
 
 import java.io.File;
 import java.util.HashMap;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.util.config.Configuration;
 
 /**
  * GuardWolf Ban System plugin for Bukkit
  *
  * @author Cue
  */
 public class GuardWolf extends JavaPlugin {
 	public final HashMap<String, String> gwConfig = new HashMap<String, String>();
 	public final GWSQL sql = new GWSQL(this);
 	private final GWPlayerListener playerListener = new GWPlayerListener(this);
 
     public void onDisable() {
         System.out.println("Goodbye world!");
     }
 
     public void onEnable() {
         // Register our commands
         getCommand("gw").setExecutor(new GWCommand(this));
         getCommand("ban").setExecutor(new GWBan(this));
         getCommand("unban").setExecutor(new GWBan(this));
         getCommand("banlist").setExecutor(new GWBan(this));
         
         // Register events
         
         PluginManager pm = getServer().getPluginManager();
         
        pm.registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Priority.High, this);
         
         // Get the config.
         
         System.out.println("Loading config file plugins/GuardWolf/config.yml...");
         Configuration _config = new Configuration(new File("plugins/GuardWolf/config.yml"));
         
         _config.load();
         
         System.out.println("Loaded GuardWolf Config Successfully!");
         
         gwConfig.put("db_address", _config.getString("gw.db.address"));
         gwConfig.put("db_user", _config.getString("gw.db.user"));
         gwConfig.put("db_pass", _config.getString("gw.db.pass"));
         gwConfig.put("db_table", _config.getString("gw.db.table"));
         gwConfig.put("limit_l1", _config.getString("gw.limit.level1"));
         gwConfig.put("limit_l2", _config.getString("gw.limit.level2"));
         gwConfig.put("limit_l3", _config.getString("gw.limit.level3"));
         
         sql.Connect();
         
         sql.Stats();
         
         System.out.println("GuardWolf Config saved to memory.");
 
         // EXAMPLE: Custom code, here we just output some info so we can check all is well
         PluginDescriptionFile pdfFile = this.getDescription();
         System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
     }
 }
