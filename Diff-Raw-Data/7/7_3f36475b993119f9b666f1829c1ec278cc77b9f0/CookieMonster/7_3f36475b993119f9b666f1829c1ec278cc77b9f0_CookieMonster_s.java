 package com.pi.coelho.CookieMonster;
 
 import com.jascotty2.util.Str;
 import com.pi.coelho.CookieMonster.protectcheck.*;
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class CookieMonster extends JavaPlugin {
 
     protected final static Logger logger = Logger.getLogger("Minecraft");
     public static final String name = "CookieMonster";
     protected static CMConfig config = new CMConfig();
     private static Server server;
     private static CMBlockListener blockListener = null;
 	private static BlockProtectListener blockProtectListener = new BlockProtectListener();
     private static CMEntityListener entityListener = null;
 	private static EntityProtectListener entityProtectListener = new EntityProtectListener();
 	protected static CMPlayerListener playerListener = new CMPlayerListener();
     private static CMRewardHandler rewardHandler = null;
 	private static CMEcon economyPluginListener;
     protected static CMRegions regions = null;
     protected static CMCampTracker killTracker = null;
     private static WorldEditPlugin worldEdit = null;
 
     @Override
     public void onEnable() {
 
         // Grab plugin details
         server = getServer();
         PluginManager pm = server.getPluginManager();
         PluginDescriptionFile pdfFile = this.getDescription();
 
         // w.e.
         Plugin we = pm.getPlugin("WorldEdit");
         if (we != null && we instanceof WorldEditPlugin) {
             worldEdit = (WorldEditPlugin) we;
         } else {
             Log(Level.INFO, "Failed to find WorldEdit: regions cannot be defined");
         }
 
         try {
             regions = new CMRegions(server, getDataFolder());
         } catch (NoClassDefFoundError e) {
             if (worldEdit == null) {
                 Log(Level.INFO, "to enable existing regions, put a copy of WorldEdit in the CookieMonster folder, "
                         + "or install WorldEdit to the server");
             } else {
                 Log(Level.WARNING, "Unexpected error while loading region manager");
             }
             regions = null;
         }
 
         // Directory
         getDataFolder().mkdir();
         getDataFolder().setWritable(true);
         getDataFolder().setExecutable(true);
         //CMConfig.Plugin_Directory = getDataFolder().getPath();
 
         // Configuration
         if (!config.load()) {
             server.getPluginManager().disablePlugin(this);
             Log(Level.SEVERE, "Failed to retrieve configuration from directory.");
             Log(Level.SEVERE, "Please back up your current settings and let CookieMonster recreate it.");
             return;
         }
 
         if (regions != null) {
             regions.load();//getServer(), getDataFolder());//new File(getDataFolder(), "regions.yml"));
         }
 
         // Initializing Listeners
         entityListener = new CMEntityListener(getServer());
         blockListener = new CMBlockListener();
         rewardHandler = new CMRewardHandler();
 
         if (config.campTrackingEnabled
 				|| config.globalCampTrackingEnabled) {
             killTracker = new CMCampTracker();
         }
 		
 		economyPluginListener = new CMEcon(this);		
 
         // Event Registration
 		pm.registerEvent(Type.ENTITY_DAMAGE, entityProtectListener, Priority.Low, this);
        pm.registerEvent(Type.ENTITY_DEATH, entityListener, Priority.High, this);
        pm.registerEvent(Type.ENTITY_DAMAGE, entityListener, Priority.High, this);
 //		pm.registerEvent(Type.PROJECTILE_HIT, entityListener, Priority.Normal, this);
 
         pm.registerEvent(Type.BLOCK_BREAK, blockProtectListener, Priority.Low, this);
        pm.registerEvent(Type.BLOCK_BREAK, blockListener, Priority.High, this);
 		
 		pm.registerEvent(Type.PLAYER_LOGIN, playerListener, Priority.Normal, this);
 		pm.registerEvent(Type.PLAYER_RESPAWN, playerListener, Priority.Normal, this);
 
 		pm.registerEvent(Type.PLUGIN_ENABLE, economyPluginListener, Priority.Monitor, this);
 		pm.registerEvent(Type.PLUGIN_DISABLE, economyPluginListener, Priority.Monitor, this);
 
         // Console Detail
         Log(" v" + pdfFile.getVersion() + " loaded successfully.");
         Log(" Developed by: " + pdfFile.getAuthors());
     }
 
     @Override
     public void onDisable() {
         if (regions != null) {
             regions.globalRegionManager.unload();
         }
         if (killTracker != null) {
             killTracker.save();
         }
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (command.getName().equalsIgnoreCase("cookiemonster")) {
             if (!sender.isOp()) {
                 sender.sendMessage("You are not an OP!");
                 return true;
             }
             if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                 if (!config.load()) {
                     sender.sendMessage("Reload Failed!");
                 } else {
                     if ((config.campTrackingEnabled || config.globalCampTrackingEnabled)
 						&& killTracker == null) {
                         killTracker = new CMCampTracker();
                     } else if ((config.campTrackingEnabled || config.globalCampTrackingEnabled)
 						&& killTracker != null) {
                         //todo:save
                         killTracker = null;
                     }
                 }
                 sender.sendMessage("Settings Reloaded");
             } else if (args.length >= 1 && args[0].equalsIgnoreCase("region")) {
                 if ((args.length == 2 || args.length == 3) && args[1].equalsIgnoreCase("list")) {
                     regions.list(sender, args);
                 } else if (args.length != 3 || !Str.isIn(args[1], "define,def,d,remove,delete,del,rem")) {
                     sender.sendMessage("Usage: ");
                     sender.sendMessage("/" + label + " region define <id>    - define a cookiemonster region");
                     sender.sendMessage("/" + label + " region list <page>    - list regions");
                     sender.sendMessage("/" + label + " region remove <id>   - remove a cookiemonster region");
                 } else if (Str.isIn(args[1], "define,def,d")) {//args[1].equalsIgnoreCase("define")) {
                     if (worldEdit == null) {
                         sender.sendMessage("WorldEdit (required to define regions) is not installed");
                     } else if (!(sender instanceof Player)) {
                         sender.sendMessage("can only be done in-game");
                     } else {
                         regions.define((Player) sender, args, worldEdit.getSelection((Player) sender));
                     }
                 } else if (Str.isIn(args[1], "remove,delete,del,rem")) {//args[1].equalsIgnoreCase("remove")) {
                     regions.remove(sender, args);
                 }
             } else {
                 return false;
             }
         }
         return true;
     }
 
     public static Server getBukkitServer() {
         return server;
     }
 
     public static CMRewardHandler getRewardHandler() {
         return rewardHandler;
     }
 	
 	public static CMConfig getSettings() {
 		return config;
 	}
 
     public static void Log(String txt) {
         logger.log(Level.INFO, String.format("[%s] %s", name, txt));
     }
 
     public static void Log(Level loglevel, String txt) {
         Log(loglevel, txt, true);
     }
 
     public static void Log(Level loglevel, String txt, boolean sendReport) {
         logger.log(loglevel, String.format("[%s] %s", name, txt == null ? "" : txt));
     }
 
     public static void Log(Level loglevel, String txt, Exception params) {
         if (txt == null) {
             Log(loglevel, params);
         } else {
             logger.log(loglevel, String.format("[%s] %s", name, txt == null ? "" : txt), params);
         }
     }
 
     public static void Log(Level loglevel, Exception err) {
         logger.log(loglevel, String.format("[%s] %s", name, err == null ? "? unknown exception ?" : err.getMessage()), err);
     }
 }
