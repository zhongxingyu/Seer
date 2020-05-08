 package in.mDev.MiracleM4n.DragonSpawn;
 
 import org.bukkit.World;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.EnderDragon;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.io.File;
 import java.util.*;
 
 public class DragonSpawn extends JavaPlugin {
     // Default Plugin Data
     public PluginManager pm;
     public PluginDescriptionFile pdfFile;
 
     // Configuration
     public YamlConfiguration config = null;
 
     // Configuration Files
     public File configF = null;
 
     // Timers
     long sTime1;
     long sTime2;
    float sDiff;
 
     public List<Long> spawn = new ArrayList<Long>();
 
     public void onEnable() {
         // 1st Startup Timer
         sTime1 = new Date().getTime();
 
         // Initialize Plugin Data
         pm = getServer().getPluginManager();
         pdfFile = getDescription();
 
         // Initialize Configs
         configF = new File(getDataFolder(), "config.yml");
 
         config = YamlConfiguration.loadConfiguration(configF);
 
         // Manage Config options
         config.options().indent(4);
 
         // Setup Config
         setupConfig();
 
         // Register Events
         setupTimers();
 
         // Setup Command
         getCommand("dragonspawn").setExecutor(new CommandSender(this));
 
         // Setup Event
         pm.registerEvents(new EntityListener(this), this);
 
         // 2nd Startup Timer
         sTime2 = new Date().getTime();
 
         // Calculate Startup Timer
        sDiff = ((float) sTime2 - sTime1) / 1000;
 
        getAPI().log("[" + pdfFile.getName() + "] " + pdfFile.getName() + " v" + pdfFile.getVersion() + " is enabled! [" + sDiff + "s]");
     }
 
     public void onDisable() {
         getServer().getScheduler().cancelTasks(this);
 
         getAPI().log("[" + pdfFile.getName() + "] " + pdfFile.getName() + " v" + pdfFile.getVersion() + " is disabled!");
     }
 
     public void configReload() {
         config = YamlConfiguration.loadConfiguration(configF);
         setupConfig();
 
         getServer().getScheduler().cancelTasks(this);
         setupTimers();
     }
 
     // API
     public API getAPI() {
         return new API(this);
     }
 
     void setupTimers() {
         getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
             public void run() {
                 World world = getServer().getWorld(config.getString("spawn.world"));
 
                 if (world == null)
                     return;
 
                 Integer count = 0;
 
                 for (Entity entity : world.getEntities())
                     if (entity instanceof EnderDragon)
                         count++;
 
                 if (count >= config.getInt("int.maxInWorld"))
                     return;
 
                 if (spawn.size() >= config.getInt("int.maxSpawnPerDay"))
                     return;
 
                 if (getAPI().isPercent(config.getInt("int.spawnChance"))) {
                     world.spawnCreature(world.getSpawnLocation().add(0, 200, 0), EntityType.ENDER_DRAGON);
                     getServer().broadcastMessage(getAPI().addColour(getAPI().getRandomString(config.getList("message.dSpawned"))).replace("%world%", world.getName()));
                     spawn.add(new Date().getTime());
                 }
             }
         }, 20L * 60L * config.getInt("int.timerLoop"), 20L * 60L * config.getInt("int.timerLoop"));
 
         getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
             public void run() {
                 spawn = new ArrayList<Long>();
             }
         }, 20L * 60L * config.getInt("int.spawnDay"), 20L * 60L * config.getInt("int.spawnDay"));
 
         getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
             public void run() {
                 World world = getServer().getWorld(config.getString("spawn.world"));
 
                 if (world == null)
                     return;
 
                 Integer count = 0;
 
                 for (Entity entity : world.getEntities())
                     if (entity instanceof EnderDragon)
                         count++;
 
                 if (count > 0)
                     getServer().broadcastMessage(getAPI().addColour(getAPI().getRandomString(config.getList("message.isAlive"))).replace("%world%", world.getName()));
             }
         }, 20L * config.getInt("int.messageReplay"), 20L * config.getInt("int.messageReplay"));
     }
 
     void setupConfig() {
         ArrayList<String> fList = new ArrayList<String>();
         ArrayList<String> dList = new ArrayList<String>();
 
         fList.add("&6[Server] Get to %world% a dragon has spawned.");
         fList.add("&6[Server] A dragon just spawned in %world%, Hurry before it is killed.");
         fList.add("&6[Server] Better get to %world% a DRAGON has just spawned.");
 
         dList.add("&6[Server] There is still a Dragon alive in %world% kill it Nao.");
         dList.add("&6[Server] A Dragon is lurking in %world% end it.");
         dList.add("&6[Server] What A Dragon you say? Get to %world% to destroy it.");
 
         checkOption("message.dSpawned", fList);
         checkOption("message.isAlive", dList);
 
         checkOption("int.maxInWorld", 2);
         checkOption("int.maxSpawnPerDay", 2);
         checkOption("int.spawnChance", 10);
         checkOption("int.timerLoop", 60);
         checkOption("int.spawnDay", 1440);
         checkOption("int.messageReplay", 60);
 
         checkOption("spawn.world", "Reconfigure_This_World");
 
         checkOption("dragon.droppedExp", 20000);
     }
 
     void checkOption(String option, Object defValue) {
         if (!config.isSet(option)) {
             config.set(option, defValue);
 
             try {
                 config.save(configF);
             } catch (Exception ignored) {}
         }
     }
 }
 
