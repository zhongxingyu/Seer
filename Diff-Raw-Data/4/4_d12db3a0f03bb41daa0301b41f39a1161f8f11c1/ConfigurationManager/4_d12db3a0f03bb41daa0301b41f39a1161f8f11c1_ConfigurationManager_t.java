 package edgruberman.bukkit.simpletemplate;
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import org.bukkit.plugin.Plugin;
 
 public class ConfigurationManager {
     
     private static final int TICKS_PER_SECOND = 20;
     
     // Name of configuration file. (Used for both default supplied in JAR and the active one in the file system.)
     private static String FILE = "config.yml";
     
     // Path to default configuration file supplied in JAR.
     private static String DEFAULT_PATH = "/defaults/" + ConfigurationManager.FILE;
     
     // Shortest duration in seconds a configuration file save can occur in for non-forced save requests.
     private static int MAX_SAVE = 10;
     
     private static Calendar LAST_SAVE = null;
     private static Integer taskSave = null;
     
     private Plugin plugin;
     
     protected ConfigurationManager(Plugin plugin) {
         this.plugin = plugin;
     }
     
     /**
      * Create configuration file from default if necessary and then load.
      * 
      * @param plugin Plugin to load configuration for.
      */
     protected void load() {
         File fileConfig = new File(this.plugin.getDataFolder(), ConfigurationManager.FILE);
         if (!fileConfig.exists()) {
             try {
                 this.extract(this.plugin.getClass().getResource(ConfigurationManager.DEFAULT_PATH), fileConfig);
             } catch (Exception e) {
                 System.err.println("Unable to extract default configuration file.");
                 e.printStackTrace();
             }
         }
         
         this.plugin.getConfiguration().load();
     }
     
     /**
      * Extract a file from the JAR to the local file system.
      * 
      * @param source Location of file in JAR.
      * @param destination File system path to save file to.
      * @throws Exception
      */
     private void extract(URL source, File destination) throws Exception {
         InputStream in = source.openStream();
         
         destination.getParentFile().mkdir();
         OutputStream out = new FileOutputStream(destination);
         
         byte[] buf = new byte[1024];
         int len;
         while ((len = in.read(buf)) > 0) {
             out.write(buf, 0, len);
         }
         in.close();
         out.close();
     }
     
     /**
      * Force a save of the configuration file immediately.
      */
     protected void save() {
         this.save(true);
     }
     
     /**
      * Save the configuration file
      * 
      * @param force true to force a save of the configuration file immediately.
      */
     protected void save(boolean force) {
         if (!force) {
             // Determine how long since last save.
             long lastSave = -1;
             if (ConfigurationManager.LAST_SAVE != null)
                 lastSave = (System.currentTimeMillis() - ConfigurationManager.LAST_SAVE.getTimeInMillis()) / 1000;
             
             // Schedule task to run if last save was less than MAX_SAVE.
             if (lastSave >= 0 && lastSave < ConfigurationManager.MAX_SAVE) {
                 // If task already scheduled return and let currently scheduled task run when expected.
                 if (this.plugin.getServer().getScheduler().isQueued(ConfigurationManager.taskSave)) return;
                 
                 // Schedule task to force save.
                final ConfigurationManager configurationManager = this;
                 ConfigurationManager.taskSave = this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(
                           this.plugin
                        , new Runnable() { public void run() { configurationManager.save(true); } }
                         , (ConfigurationManager.MAX_SAVE - lastSave) * ConfigurationManager.TICKS_PER_SECOND
                 );
             
                 return;
             }
         }
         
         this.plugin.getConfiguration().save();
         ConfigurationManager.LAST_SAVE = new GregorianCalendar();
     }
 }
