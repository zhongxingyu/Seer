 package me.limebyte.battlenight.core.tosort;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 
 import me.limebyte.battlenight.core.BattleNight;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 public class Configuration {
     private final String fileName;
     private final String directory;
     private final boolean copyDefaults;
 
     private File file;
     private YamlConfiguration fileConfig;
 
     public Configuration(String fileName) {
         this(fileName, true);
     }
 
     public Configuration(String fileName, boolean copyDefaults) {
         this.fileName = fileName;
         directory = BattleNight.instance.getDataFolder().getAbsolutePath();
         this.copyDefaults = copyDefaults;
     }
 
     public Configuration(String fileName, String directory) {
         this(fileName, directory, true);
     }
 
     public Configuration(String fileName, String directory, boolean copyDefaults) {
         this.fileName = fileName;
         this.directory = BattleNight.instance.getDataFolder().getAbsolutePath() + File.separator + directory;
         this.copyDefaults = copyDefaults;
     }
 
     public FileConfiguration get() {
         if (fileConfig == null) {
             reload();
         }
         return fileConfig;
     }
 
     public void reload() {
         if (file == null) {
             file = new File(directory, fileName);
         }
         fileConfig = YamlConfiguration.loadConfiguration(file);
 
         // Look for non-existent defaults
         InputStream defConfigStream = BattleNight.instance.getResource(fileName);
         if (defConfigStream != null) {
             YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            fileConfig.setDefaults(defConfig);
             fileConfig.options().indent(4);
            fileConfig.options().copyDefaults(!file.exists() || copyDefaults);
         }
     }
 
     public void save() {
        if (fileConfig == null || file == null)
            return;
         else {
             try {
                 get().save(file);
             } catch (IOException ex) {
             }
         }
     }
 }
