 package de.craftlancer.unhealthydeath;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import de.craftlancer.unhealthydeath.metrics.Metrics;
 
 public class UnhealthyDeath extends JavaPlugin
 {
     private UnhealthyListener listener;
     private FileConfiguration config;
     private HashMap<String, UnhealthyGroup> groupMap = new HashMap<String, UnhealthyGroup>();
     
     @Override
     public void onEnable()
     {
         loadConfig();
         
         listener = new UnhealthyListener(this);
         getServer().getPluginManager().registerEvents(listener, this);
         
         try
         {
            Metrics metrics = new Metrics(this);            
             metrics.start();
         }
         catch (IOException e)
         {
         }
     }
     
     @Override
     public void onDisable()
     {
         config = null;
         getServer().getScheduler().cancelTasks(this);
     }
     
     private void loadConfig()
     {
         if (!new File(getDataFolder().getPath(), "config.yml").exists())
             saveDefaultConfig();
         
         config = getConfig();
         
        if (config.getConfigurationSection("group").getKeys(false).isEmpty())
             update031Config();
         
         for (String key : config.getConfigurationSection("group").getKeys(false))
         {
             double health = config.getDouble("group." + key + ".sethealth", 20);
             boolean keepset = config.getString("group." + key + ".foodchange", "keep").equalsIgnoreCase("set");
             int minfood = config.getInt("group." + key + ".minfood", 0);
             int amount = config.getInt("group." + key + ".foodamount", keepset ? 20 : 0);
             List<String> worlds = config.getStringList("group." + key + ".worlds");
             
             groupMap.put(key, new UnhealthyGroup(health, keepset, minfood, amount, worlds));
         }
     }
     
     private void update031Config()
     {
         boolean b = config.getString("food.foodchange", "keep").equalsIgnoreCase("set");
         
         config.set("group.default.sethealth", config.getDouble("health.sethealth", 20));
         config.set("group.default.foodchange", config.getString("food.foodchange", "keep"));
         config.set("group.default.minfood", config.getInt("food.minfood", 0));
         config.set("group.default.foodamount", b ? config.getInt("food.setfood", 20) : config.getInt("food.subtractfood", 0));
         config.set("group.default.worlds", config.getStringList("worlds"));
         
         config.set("health", null);
         config.set("food", null);
         config.set("worlds", null);
         
         saveConfig();
     }
     
     protected UnhealthyGroup getGroup(Player p)
     {
         for (String key : groupMap.keySet())
             if (p.hasPermission("unhealthydeath.group." + key))
                 return groupMap.get(key);
         
         return groupMap.get("default");
     }
 }
