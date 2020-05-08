 package com.gmail.jameshealey1994.simplepvptoggle.localisation;
 
 import com.gmail.jameshealey1994.simplepvptoggle.SimplePVPToggle;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.nio.charset.StandardCharsets;
 import java.util.HashMap;
 import java.util.logging.Level;
 import org.bukkit.ChatColor;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 /**
  * Class to help with localisation and custom messages.
  *
  * @author JamesHealey94 <jameshealey1994.gmail.com>
  */
 public class Localisation {
 
     /**
      * The default localisation filename.
      */
     public static final String DEFAULT_FILENAME = "localisation.yml";
 
     /**
      * Plugin associated with the localisation.
      */
     private final SimplePVPToggle plugin;
 
     /**
      * Initialises plugin variable and localisations.
      *
      * @param plugin    plugin used for config file, used for filename
      */
     public Localisation(SimplePVPToggle plugin) {
         if (plugin == null) {
             throw new IllegalArgumentException("plugin cannot be null");
         }
         this.plugin = plugin;
     }
 
     /**
      * Gets the corresponding localisation to the key passed.
      *
      * @param key   key used to obtain localisation value
      * @return      value obtained, or error message if no valid value is found
      */
     public String get(LocalisationEntry key) {
         final HashMap<String, Object> localisations = getLocalisations();
 
         if (localisations.containsKey(key.getName())) {
             return addColor(String.valueOf(localisations.get(key.getName())));
         } else {
            plugin.getLogger().log(Level.WARNING, ChatColor.RED + "Missing localisation: {0}", key.getName());
            return ChatColor.RED + "ERROR: Missing localisation: '" + key.getName() + "'";
         }
     }
 
     /**
      * Adds colours to the passed string.
      *
      * @param string    string to add colours to
      * @return          passed string with colours added
      */
     public static String addColor(String string) {
         return ChatColor.translateAlternateColorCodes('&', string);
     }
 
     /**
      * Returns either a custom or default HashMap of localisations.
      *
      * @return  custom HashMap of localisations if one has been provided, else a
      *          default is returned
      */
     private HashMap<String, Object> getLocalisations() {
         return (HashMap<String, Object>) getConfig().getValues(true);
     }
 
     /**
      * Returns the FileConfiguration of getFile().
      *
      * @return the FileConfiguration of getFile()
      */
     public FileConfiguration getConfig() {
         return YamlConfiguration.loadConfiguration(getFile());
     }
 
     /**
      * Returns the file containing localisation values.
      * If the file with the filename specified in the config.yml does not exist,
      * a file is created with that name and filled with default values.
      *
      * @return  the file containing localisation values
      */
     private File getFile() {
         final File file = new File(plugin.getDataFolder(), getFilename());
         if (!(file.exists())) {
             createDefaultConfig();
         }
         return file;
     }
 
     /**
      * Fills the file specified in the config with default localisation values.
      */
     private void createDefaultConfig() {
         final String title = plugin.getName() + " localisation configuration";
         final File file = new File(plugin.getDataFolder(), getFilename());
 
         try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
             writer.write("# " + title + "\n");
             for (LocalisationEntry entry : LocalisationEntry.values()) {
                 writer.write(entry.toString());
             }
         } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, ChatColor.RED + "IOException when creating new localisation file: {0}", title);
             plugin.getLogger().severe(ex.getMessage());
         }
     }
 
     /**
      * Returns the filename as specified in config.yml.
      *
      * @return  filename as specified in config.yml
      */
     public final String getFilename() {
         return plugin.getConfig().getString("Localisation Filename", DEFAULT_FILENAME);
     }
 }
