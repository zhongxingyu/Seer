 /**
  * Plug-in class for CombatFlags
  * @author dririan
  */
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class CombatFlags extends Plugin {
     static final CombatFlagsListener listener = new CombatFlagsListener();
     private static final Logger logger = Logger.getLogger("Minecraft");
     public String pluginName = "CombatFlags";
    public String pluginVersion = "0.1";    // TODO: Add some sort of build ID
 
     /**
      * Logs the unloading of the plug-in.
      */
     @SuppressWarnings("LoggerStringConcat")
     public void disable() {
         logger.log(Level.INFO, "Unloaded " + pluginName);
     }
 
     /**
      * Logs the loading of the plug-in.
      */
     @SuppressWarnings("LoggerStringConcat")
     public void enable() {
         PropertiesFile propfile = new PropertiesFile("server.properties");
         try {
             propfile.load();
         } catch (IOException ex) {
             logger.log(Level.SEVERE, null, ex);
             logger.log(Level.SEVERE, pluginName + " NOT loaded");
             return;
         }
         if(!propfile.keyExists("pvp-default-enabled")) {
             logger.log(Level.WARNING, "[CombatFlags] Adding pvp-default-enabled to server.properties");
             propfile.setBoolean("pvp-default-enabled", true);
             propfile.save();
         }
         if(!propfile.keyExists("pvp-disable-commands")) {
             logger.log(Level.WARNING, "[CombatFlags] Adding pvp-disable-commands to server.properties");
             propfile.setString("pvp-disable-commands", "/home,/pvp,/spawn,/warp");
             propfile.save();
         }
         if(!propfile.keyExists("pvp-disable-commands-exempt-groups")) {
             logger.log(Level.WARNING, "[CombatFlags] Adding pvp-disable-commands-exempt-groups to server.properties");
             propfile.setString("pvp-disable-commands-exempt-groups", "admins,mods");
         }
         if(!propfile.keyExists("pvp-disable-commands-time")) {
             logger.log(Level.WARNING, "[CombatFlags] Adding pvp-disable-commands-time to server.properties");
             propfile.setLong("pvp-disable-commands-time", 300);
             propfile.save();
         }
         logger.log(Level.INFO, "Loaded " + pluginName + " v" + pluginVersion);
         listener.prepare();
     }
 
     /**
      * Registers hooks for all the events the plug-in wants to receive.
      */
     @Override
     public void initialize() {
         etc.getLoader().addListener(PluginLoader.Hook.COMMAND, listener, this, PluginListener.Priority.CRITICAL);
         etc.getLoader().addListener(PluginLoader.Hook.DAMAGE, listener, this, PluginListener.Priority.HIGH);
         etc.getLoader().addListener(PluginLoader.Hook.HEALTH_CHANGE, listener, this, PluginListener.Priority.HIGH);
     }
 }
