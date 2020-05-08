 
 package me.heldplayer.HeldStone;
 
 import java.util.logging.Logger;
 
 import me.heldplayer.HeldStone.command.HeldStoneCommand;
 import me.heldplayer.HeldStone.sign.TriggerType;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class HeldStone extends JavaPlugin {
     /**
      * Standard output stream for HeldStone
      */
     private static Logger logger;
     /**
      * Determines if debug messages are shown
      */
     private static boolean debugging = false;
     /**
      * The PluginDescriptionFile instance for HeldStone
      */
     private PluginDescriptionFile pdf;
     /**
      * Manages player info.
      * Used for locations, chat IC-messages and potential snow height.
      */
     public PlayerManager pmng;
     /**
      * Manages HeldSign instances.
      */
     public SignManager smng;
     public MainListener mainListener;
     public PlayerListener playerListener;
     public ActionScheduler scheduler;
     public FileConfiguration config;
 
     public static Permission permission = null;
 
     /**
      * Called when this plugin is disabled
      */
     public void onDisable() {
         pmng.purge();
         smng.save();
     }
 
     /**
      * Called when this plugin is enabled
      */
     public void onEnable() {
         long start = System.currentTimeMillis();
 
         config = getConfig();
         logger = this.getLogger();
         pdf = this.getDescription();
 
         debugging = config.getBoolean("debuging", false);
 
         pmng = new PlayerManager();
         smng = new SignManager(this);
 
         smng.load();
 
         mainListener = new MainListener(this);
         playerListener = new PlayerListener(this);
 
        getServer().getScheduler().runTaskTimerAsynchronously(this, new PlayerTimedCleanup(this), 1200, 1200);
        getServer().getScheduler().runTaskTimer(this, scheduler = new ActionScheduler(this), 1, 1);
 
         getCommand("heldstone").setExecutor(new HeldStoneCommand(this));
 
         getServer().getPluginManager().registerEvents(mainListener, this);
         getServer().getPluginManager().registerEvents(playerListener, this);
 
         if (!setupPermissions()) {
             warning("No permissions system found, some signs might not work.");
         }
 
         long end = System.currentTimeMillis();
 
         scheduler.schedule(new ScheduledAction(20, true) {
             public void trigger(HeldStone main) {
                 smng.trigger(TriggerType.VALID_CHECK, null);
             }
 
             public void cancel() {}
         });
 
         smng.announce();
 
         info(pdf.getFullName() + " finished loading! (" + (end - start) + "ms)");
     }
 
     private boolean setupPermissions() {
         RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
         if (permissionProvider != null) {
             permission = permissionProvider.getProvider();
         }
         return (permission != null);
     }
 
     public static boolean isInGroup(org.bukkit.entity.Player player, String group) {
         if (permission != null) {
             return permission.playerInGroup(player, group);
         }
 
         return false;
     }
 
     /**
      * Triggers an event on the Bukkit server
      * 
      * @param event
      *        The event to run
      */
     public static void doEvent(Event event) {
         Bukkit.getServer().getPluginManager().callEvent(event);
     }
 
     /**
      * Logs a message of type {@link java.util.logging.Level#INFO Level.INFO}
      * 
      * @param message
      *        The message to be displayed
      */
     public static void info(String message) {
         logger.info(message);
     }
 
     /**
      * Logs a message of type {@link java.util.logging.Level#WARNING
      * Level.WARNING}
      * 
      * @param message
      *        The message to be displayed
      */
     public static void warning(String message) {
         logger.warning(message);
     }
 
     /**
      * Logs a message of type {@link java.util.logging.Level#SEVERE
      * Level.SEVERE}
      * 
      * @param message
      *        The message to be displayed
      */
     public static void severe(String message) {
         logger.severe(message);
     }
 
     /**
      * Logs a message of type {@link java.util.logging.Level#INFO Level.INFO}
      * with a prefix <code>[Debug]</code>.
      * The message is only displayed if debugging is set to true
      * 
      * @param message
      *        The message to be displayed
      */
     public static void debug(String message) {
         if (debugging)
             logger.info("[Debug] " + message);
     }
 
     /**
      * Gives a boolean value indicating wether HeldStone is debugging
      * 
      * @return <code>true</code> if HeldStone is in debugging mode,
      *         <code>false</code> otherwise
      */
     public static boolean isDebugging() {
         return debugging;
     }
 }
