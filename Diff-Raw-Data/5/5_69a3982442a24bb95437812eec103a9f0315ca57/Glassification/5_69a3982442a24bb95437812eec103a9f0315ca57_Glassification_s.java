 package littlegruz.glasser;
 
 import java.util.logging.Logger;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Glassification extends JavaPlugin{
    Logger log = Logger.getLogger("This is MINECRAFT!");
 
    public void onEnable(){
       getServer().getPluginManager().registerEvents(new GlassBlockListener(this), this);
      log.info("Glassification v1.2 enabled");
    }
 
    public void onDisable(){
      log.info("Glassification v1.2 disabled");
    }
 }
