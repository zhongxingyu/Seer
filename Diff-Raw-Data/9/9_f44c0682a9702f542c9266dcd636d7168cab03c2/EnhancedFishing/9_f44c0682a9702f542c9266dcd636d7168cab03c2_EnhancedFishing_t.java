 package com.norcode.bukkit.enhancedfishing;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Random;
 
 import net.h31ix.updater.Updater;
 import net.h31ix.updater.Updater.UpdateType;
 
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.MemorySection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.permissions.Permission;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.norcode.bukkit.enhancedfishing.anvil.CraftingListener;
 
 public class EnhancedFishing extends JavaPlugin implements Listener {
 
     private List<Permission> loadedPermissions = null;
     private WorldConfiguration defaultConfiguration;
     private HashMap<String, WorldConfiguration> worldConfigurations;
     private ConfigAccessor treasureConfig;
     private static Random random = new Random();
     private Updater updater;
     private static HashSet<String> globalKeys = new HashSet<String>();
     static {
         globalKeys.add("default");
         globalKeys.add("auto-update");
         globalKeys.add("enable-anvils");
     }
     public WorldConfiguration getDefaultConfiguration() {
         return defaultConfiguration;
     }
 
     @Override
     public void onEnable() {
         treasureConfig = new ConfigAccessor(this, "treasure.yml");
         Player p;
 
         FileConfiguration config = getConfig();
         config.options().copyDefaults(true);
         saveConfig();
         getServer().getPluginManager().registerEvents(this, this);
         getServer().getPluginManager().registerEvents(new FishingListener(this), this);
         worldConfigurations = new HashMap<String, WorldConfiguration>();
         loadConfig();
         doUpdater();
         if (getConfig().getBoolean("enable-anvils", true)) {
             try { 
                 Class.forName("net.minecraft.server.v1_6_R2.Enchantment");
                 getServer().getPluginManager().registerEvents(new CraftingListener(this), this);
             } catch (ClassNotFoundException e) {
                 getLogger().warning("CraftBukkit version mismatch.  Anvil functionality will NOT be available.");
             }
         }
     }
 
     @Override
     public void onDisable() {
         loadedPermissions = null;
         treasureConfig = null;
         worldConfigurations = null;
         defaultConfiguration = null;
         random = null;
         globalKeys = null;
     }
 
     private void loadConfig() {
         worldConfigurations.clear();
         defaultConfiguration = new WorldConfiguration(this, "default");
         for (String key: getConfig().getKeys(false)) {
             if (!globalKeys .contains(key.toLowerCase())) {
                 getLogger().info("Loading custom world configuration for " + key);
                 World world = getServer().getWorld(key);
                 if (world == null) {
                     getLogger().warning("world '" + key + "' does not exist.");
                     continue;
                 }
                 worldConfigurations.put(key.toLowerCase(), new WorldConfiguration(this, key));
             }
         }
     }
 
 
     public void doUpdater() {
         String autoUpdate = getConfig().getString("auto-update", "notify-only").toLowerCase();
         if (autoUpdate.equals("true")) {
             updater = new Updater(this, "enhancedfishing", this.getFile(), UpdateType.DEFAULT, true);
         } else if (autoUpdate.equals("false")) {
             getLogger().info("Auto-updater is disabled.  Skipping check.");
         } else {
             updater = new Updater(this, "enhancedfishing", this.getFile(), UpdateType.NO_DOWNLOAD, true);
         }
     }
 
     public WorldConfiguration getWorldConfiguration(World world) {
         return getWorldConfiguration(world.getName());
     }
 
     public WorldConfiguration getWorldConfiguration(String world) {
         WorldConfiguration cfg = worldConfigurations.get(world.toLowerCase());
         if (cfg == null) {
             cfg = defaultConfiguration;
         }
         return cfg;
     }
 
     public List<Permission> getLoadedPermissions() {
         return loadedPermissions;
     }
 
     public void setLoadedPermissions(List<Permission> loadedPermissions) {
         this.loadedPermissions = loadedPermissions;
     }
 
     @EventHandler()
     public void onPluginEnable(PluginEnableEvent event) {
         if (event.getPlugin().getName().equals("EnhancedFishingAnvilAddon")) {
             getLogger().warning("Detected EnhancedFishingAnvilAddon.  That plugin is obsolete, and it's functionality is now built in to EnhancedFishing.  We're going to disable it for you, but you should remove it.");
             getServer().getPluginManager().disablePlugin(event.getPlugin());
         }
     }
 
     @EventHandler(ignoreCancelled=true)
     public void onPlayerLogin(PlayerLoginEvent event) {
         if (event.getPlayer().hasPermission("enhancedfishing.admin")) {
             final String playerName = event.getPlayer().getName();
             if (updater == null) return;
             getServer().getScheduler().runTaskLaterAsynchronously(this, new Runnable() {
                 public void run() {
                     Player player = getServer().getPlayer(playerName);
                     if (player != null && player.isOnline()) {
                         switch (updater.getResult()) {
                         case UPDATE_AVAILABLE:
                             player.sendMessage("An update is available at http://dev.bukkit.org/server-mods/enhancedfishing/");
                             break;
                         case SUCCESS:
                             player.sendMessage("An update has been downloaded and will take effect when the server restarts.");
                             break;
                         default:
                             // nothing
                         }
                     }
                 }
             }, random.nextInt(20)+20);
         }
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command,
             String label, String[] args) {
        if (args.length == 0) {
            return false;
        }
         if (args[0].toLowerCase().equals("reload")) {
             if (sender.hasPermission("enhancedfishing.admin")) {
                 this.loadConfig();
                 sender.sendMessage("EnhancedFishing config has been reloaded from disk.");
                 return true;
             }
         }
         return false;
     }
 
     public ConfigAccessor getTreasureConfig() {
         return treasureConfig;
     }
 }
