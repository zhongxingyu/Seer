 package fr.ethilvan.bukkit.drops;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Random;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Entity;
 
 public class EcoConfig extends YamlConfiguration {
 
     public EcoConfig(DropsPlugin plugin) {
         String filename = "eco.yml";
         File path = new File(plugin.getDataFolder(), filename);
         try {
             load(path);
         } catch (IOException exc) {
             plugin.getLogger().warning("Unable to load " + path);
         } catch (InvalidConfigurationException exc) {
             plugin.getLogger().warning("Unable to read " + path);
         }
        try {
            YamlConfiguration defaults = new YamlConfiguration();
            defaults.load(plugin.getResource("eco.yml"));
            setDefaults(defaults);
        } catch (IOException exc) {
            plugin.getLogger().warning("Unable to load " + path);
        } catch (InvalidConfigurationException exc) { 
            plugin.getLogger().warning("Unable to load " + path);
        }
     }
 
     public int getMoneyDrop(Entity entity) {
         String name = entity.getClass().getSimpleName().toLowerCase()
                 .replaceFirst("craft", "");
         if (isConfigurationSection(name)) {
             ConfigurationSection ecoConfig = getConfigurationSection(name);
             Random rand = new Random();
             int chance = ecoConfig.getInt("chance", 1);
             int min = ecoConfig.getInt("min", 0);
             int max = ecoConfig.getInt("max", 0);
             if (rand.nextInt(100) < chance-1) {
                 if (min < max) { 
                     return rand.nextInt(max - min) + min; 
                 } else {
                     return min;
                 }
             }
         }
         return 0;
     }
 
 }
