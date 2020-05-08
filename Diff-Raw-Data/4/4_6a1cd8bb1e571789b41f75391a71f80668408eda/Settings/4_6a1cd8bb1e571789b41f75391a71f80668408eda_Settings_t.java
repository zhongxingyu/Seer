 package net.tgxn.bukkit.backup.config;
 
 import java.io.*;
 import java.util.logging.Level;
 import net.tgxn.bukkit.backup.utils.LogUtils;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.Plugin;
 
 /**
  * Loads all settings for the plugin.
  * 
  * @author Domenic Horner (gamerx)
  */
 public final class Settings {
 
     private Plugin plugin;
     private Strings strings;
     
     private File configFile;
     private FileConfiguration fileSettingConfiguration;
     
     
     public Settings(Plugin plugin, File configFile, Strings strings) {
         this.plugin = plugin;
         this.configFile = configFile;
         this.strings = strings;
         
         checkAndCreate();
         
         loadProperties();
         
         checkConfigVersion(true);
     }
     
     /**
      * Check if the config file exists, if it does not, create it from the JAR.
      * 
      */
     private void checkAndCreate() {
         try {
             if (!configFile.exists()) {
                 LogUtils.sendLog(Level.WARNING, strings.getString("newconfigfile"));
                 createDefaultSettings();
             }
         } catch (NullPointerException npe) {
             LogUtils.exceptionLog(npe.getStackTrace(), "Failed to create default configuration file.");
         } catch (SecurityException se) {
             LogUtils.exceptionLog(se.getStackTrace(), "Failed to create default configuration file.");
         }
     }
     
     /**
      * Load the properties to memory from the configFile.
      */
     private void loadProperties() {
         fileSettingConfiguration = new YamlConfiguration();
         try {
             fileSettingConfiguration.load(configFile);
         } catch (InvalidConfigurationException ice) {
             LogUtils.exceptionLog(ice.getStackTrace(), "Failed to load configuration.");
         } catch (IOException ioe) {
             LogUtils.exceptionLog(ioe.getStackTrace(), "Failed to load configuration.");
         }
     }
     
     /**
      * Checks configuration version, and return true if it requires an update.
      * 
      * @return False for no update done, True for update done.
      */
     public boolean checkConfigVersion(boolean notify) {
         
         boolean needsUpgrade = false;
         
         // Check configuration is loaded.
         if (fileSettingConfiguration != null) {
 
             // Get the version information from the file.
             String configVersion = fileSettingConfiguration.getString("version", plugin.getDescription().getVersion());
             String pluginVersion = plugin.getDescription().getVersion();
 
             // Check we got a version from the config file.
             if (configVersion == null) {
                 LogUtils.sendLog(strings.getString("failedtogetpropsver"), Level.SEVERE, true);
                 needsUpgrade = true;
             }
 
             // Check if the config is outdated.
             if (!configVersion.equals(pluginVersion))
                 needsUpgrade = true;
 
             // After we have checked the versions, we have determined that we need to update.
             if (needsUpgrade && notify) {
                 LogUtils.sendLog(Level.SEVERE, strings.getString("configupdate"));
             }
         }
         return needsUpgrade;
     }
     
     /**
      * Used to upgrade the configuration file.
      */
     public void doConfigurationUpgrade() {
         LogUtils.sendLog(strings.getString("updatingconf"), true);
         if (configFile.exists()) {
             configFile.delete();
         }
         createDefaultSettings();
         LogUtils.sendLog(strings.getString("updatingconf"), true);
     }
     
     /**
      * Load the properties file from the JAR and place it in the backup DIR.
      */
     private void createDefaultSettings() {
         
         BufferedReader bReader = null;
         BufferedWriter bWriter = null;
         String line;
         try {
             // Open a stream to the properties file in the jar, because we can only access over the class loader.
             bReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/settings/config.yml")));
             bWriter = new BufferedWriter(new FileWriter(configFile));
 
             // Copy the content to the configfile location.
             while ((line = bReader.readLine()) != null) {
                 bWriter.write(line);
                 bWriter.newLine();
             }
         } catch (IOException ioe) {
             LogUtils.exceptionLog(ioe.getStackTrace(), "Error opening stream.");
         }
         
         finally {
             try {
                 if (bReader != null) {
                     bReader.close();
                 }
                 if (bWriter != null) {
                     bWriter.close();
                 }
             } catch (IOException ioe) {
                 LogUtils.exceptionLog(ioe.getStackTrace(), "Error closing stream.");
             }
         }
     }
 
     /**
      * Gets the value of a integer property.
      * 
      * @param property The name of the property.
      * @return The value of the property, defaults to -1.
      */
     public int getIntProperty(String property) {
         return fileSettingConfiguration.getInt(property, -1);
     }
 
     /**
      * Gets the value of a boolean property.
      * 
      * @param property The name of the property.
      * @return The value of the property, defaults to true.
      */
     public boolean getBooleanProperty(String property) {
         return fileSettingConfiguration.getBoolean(property, true);
     }
 
     /**
      * Gets a value of the string property and make sure it is not null.
      * 
      * @param property The name of the property.
      * @return The value of the property.
      */
     public String getStringProperty(String property) {
         return fileSettingConfiguration.getString(property, "");
     }
     
     /**
      * Method to get and interpret the interval.
      * 
      * @return minutes between backups.
      */
     public int getIntervalInMinutes() {
         String settingBackupInterval = getStringProperty("backupinterval");
         
         if(settingBackupInterval.trim().equals("-1") || settingBackupInterval == null) {
             return 0;
         }
         
         String lastLetter = settingBackupInterval.substring(settingBackupInterval.length()-1, settingBackupInterval.length());
         int amountTime =  Integer.parseInt(settingBackupInterval.substring(0, settingBackupInterval.length()-1));
         if(lastLetter.equals("H"))
             amountTime = (amountTime * 60);
         else if(lastLetter.equals("D"))
             amountTime = (amountTime * 1440);
         else if(lastLetter.equals("W"))
             amountTime = (amountTime * 10080);
        else {
            amountTime = 0;
            LogUtils.sendLog(strings.getString("checkbackupinterval"));
        }
         return amountTime;
     }
 }
