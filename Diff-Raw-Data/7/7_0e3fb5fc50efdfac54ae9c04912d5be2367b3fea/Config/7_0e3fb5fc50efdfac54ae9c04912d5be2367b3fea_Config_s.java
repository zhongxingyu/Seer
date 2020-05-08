 package me.limebyte.battlenight.core.util.config;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 
 import me.limebyte.battlenight.core.BattleNight;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 public class Config {
     private final String fileName;
 
     private File configFile;
    private FileConfiguration fileConfiguration;
 
     public Config(String fileName) {
         this.fileName = fileName;
     }
 
     public void reloadConfig() {
         if (configFile == null) {
             File dataFolder = BattleNight.instance.getDataFolder();
             if (dataFolder == null) throw new IllegalStateException();
             configFile = new File(dataFolder, fileName);
         }
         fileConfiguration = YamlConfiguration.loadConfiguration(configFile);
 
         // Look for defaults in the jar
         InputStream defConfigStream = BattleNight.instance.getResource(fileName);
         if (defConfigStream != null) {
             YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            defConfig.options().copyHeader(true);
            defConfig.options().indent(4);
             fileConfiguration.setDefaults(defConfig);
             fileConfiguration.options().copyDefaults(true);
         }
     }
 
     public FileConfiguration getConfig() {
         if (fileConfiguration == null) {
             this.reloadConfig();
         }
         return fileConfiguration;
     }
 
     public void saveConfig() {
         if (fileConfiguration == null || configFile == null) {
             return;
         } else {
             try {
                 getConfig().save(configFile);
             } catch (IOException ex) {
                 BattleNight.log.severe("Could not save config to " + configFile + ": " + ex.getMessage());
             }
         }
     }
 
     public void saveDefaultConfig() {
         if (!configFile.exists()) {
             BattleNight.instance.saveResource(fileName, false);
         }
     }
 }
