 package com.github.lankylord.SimpleHomes;
 
 import com.github.lankylord.SimpleHomes.commands.*;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SimpleHomes extends JavaPlugin {
 
     static final Logger logger = Logger.getLogger("Minecraft");
 
     @Override
     public void onEnable() {
         logger.info("SimpleHomes Enabled!");
         saveConfig();
         saveHomes();
        
         getCommand("home").setExecutor(new HomeCommand(this));
         getCommand("sethome").setExecutor(new SetHomeCommand(this));
         getCommand("otherhome").setExecutor(new OtherHomeCommand(this));
     }
 
     @Override
     public void onDisable() {
         logger.info("SimpleHomes Disabled!");
         saveConfig();
         saveHomes();
     }
    
     private FileConfiguration Homes = null;
     private File HomesFile = null;
 
     public void reloadHomes() {
         if (HomesFile == null) {
             HomesFile = new File(getDataFolder(), "Homes.yml");
         }
         Homes = YamlConfiguration.loadConfiguration(HomesFile);
 
         InputStream defHomes = this.getResource("Homes.yml");
         if (defHomes != null) {
             YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defHomes);
             Homes.setDefaults(defConfig);
         }
     }
 
     public FileConfiguration getHomes() {
         if (Homes == null) {
             this.reloadHomes();
         }
         return Homes;
     }
 
     public void saveHomes() {
         if (Homes == null || HomesFile == null) {
             return;
         }
         try {
             getHomes().save(HomesFile);
         } catch (IOException ex) {
             this.getLogger().log(Level.SEVERE, "Could not save config to " + HomesFile, ex);
         }
     }
 }
