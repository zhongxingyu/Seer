 /*
  * Copyright (C) 2013 LordRalex
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.lordralex.totalpermissions;
 
 import com.lordralex.totalpermissions.commands.CommandHandler;
 import com.lordralex.totalpermissions.configuration.Configuration;
 import com.lordralex.totalpermissions.listeners.TPListener;
 import com.lordralex.totalpermissions.mcstats.Metrics;
 import com.lordralex.totalpermissions.permission.utils.Update;
 import java.io.File;
 import java.util.logging.Level;
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * @version 0.1
  * @author Lord_Ralex
  * @since 0.1
  */
 public class TotalPermissions extends JavaPlugin {
 
     private static String BUKKIT_VERSION = "NONE";
     private static final String[] ACCEPTABLE_VERSIONS = new String[]{
         "v1_5_R2",
         "v1_5_R1",
         "v1_4_R1"
     };
     protected FileConfiguration permFile;
     protected FileConfiguration configFile;
     protected PermissionManager manager;
     protected Configuration config;
     protected TPListener listener;
     protected Metrics metrics;
     protected CommandHandler commands;
 
     @Override
     public void onLoad() {
         try {
             getLogger().info("Beginning initial preperations");

             for (String version : ACCEPTABLE_VERSIONS) {
                 try {
                     Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftHumanEntity");
                     BUKKIT_VERSION = version;
                     break;
                 } catch (ClassNotFoundException e) {
                 }
             }
             if (BUKKIT_VERSION.equalsIgnoreCase("NONE")) {
                 getLogger().severe("You are using a version of Craftbukkit that differs from what this is tested on. Please update.");
                 getLogger().severe("While this will run, advanced features such as debug and reflection will be disabled");
                 config.disableRefection();
             }
             if (!getDataFolder().exists()) {
                 getDataFolder().mkdirs();
 
             }
             if (!(new File(getDataFolder(), "config.yml").exists())) {
                 this.saveResource("config.yml", true);
             }
             if (!(new File(getDataFolder(), "permissions.yml").exists())) {
                 this.saveResource("permissions.yml", true);
             }
 
            configFile = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "config.yml"));
            config = new Configuration();

             permFile = new YamlConfiguration();
             try {
                 permFile.load(new File(this.getDataFolder(), "permissions.yml"));
                 Update update = new Update();
                 update.backup(true);
                 update.runUpdate();
             } catch (InvalidConfigurationException e) {
                 getLogger().log(Level.SEVERE, "YAML error in your permissions.yml file");
                 getLogger().log(Level.SEVERE, "-> " + e.getMessage());
                 getLogger().log(Level.WARNING, "Attempting to load backup permission file");
                 try {
                     permFile = new YamlConfiguration();
                     permFile.load(new File(this.getLastBackupFolder(), "permissions.yml"));
                     getLogger().log(Level.WARNING, "Loaded an older perms files. Please repair your permissions before doing anything else though.");
                     getLogger().log(Level.WARNING, "I will also disable timed backups so you can fix your file and (hopefully) not back up bugged copies");
                 } catch (InvalidConfigurationException e2) {
                     getLogger().log(Level.SEVERE, "Errors in the last backup up, cannot load");
                     getLogger().log(Level.SEVERE, "Shutting down perms plugin as there is nothing I can do ;~;");
                     throw e2;
                 }
             }
 
             getLogger().info("Initial preperations complete");
         } catch (Exception e) {
             getLogger().log(Level.SEVERE, "Error in starting up " + getName() + " (Version " + this.getDescription().getVersion() + ")", e);
             this.getPluginLoader().disablePlugin(this);
         }
     }
 
     @Override
     public void onEnable() {
         try {
             getLogger().config("Creating perms manager");
             manager = new PermissionManager();
             manager.load();
             getLogger().config("Creating player listener");
             listener = new TPListener(this);
             Bukkit.getPluginManager().registerEvents(listener, this);
             getLogger().config("Creating command handler");
             commands = new CommandHandler();
             getCommand("totalpermissions").setExecutor(commands);
             metrics = new Metrics(this);
             if (metrics.start()) {
                 getLogger().info("Plugin Metrics is on, you can opt-out in the PluginMetrics config");
             }
         } catch (Exception e) {
             if (e instanceof InvalidConfigurationException) {
                 getLogger().log(Level.SEVERE, "YAML error in your permissions file");
                 getLogger().log(Level.SEVERE, ((InvalidConfigurationException) e).getMessage());
             } else {
                 getLogger().log(Level.SEVERE, "Error in starting up " + getName() + " (Version " + this.getDescription().getVersion() + ")", e);
             }
             this.getPluginLoader().disablePlugin(this);
         }
     }
 
     @Override
     public void onDisable() {
         if (manager != null) {
             manager.unload();
         }
     }
 
     /**
      * Gets the {@link PermissionManager} for this plugin.
      *
      * @return The {@link PermissionManager} that this is using
      *
      * @since 1.0
      */
     public PermissionManager getManager() {
         return manager;
     }
 
     /**
      * Gets the permissions file. This does not load the file, this only
      * provides the stored object.
      *
      * @return The perm file in the FileConfiguration form.
      *
      * @since 1.0
      */
     public FileConfiguration getPermFile() {
         return permFile;
     }
 
     /**
      * Gets the configuration file. This does not load the file, this only
      * provides the stored object.
      *
      * @return The configuration file in the FileConfiguration form.
      *
      * @since 1.0
      */
     public FileConfiguration getConfigFile() {
         return configFile;
     }
 
     /**
      * Gets the instance of the plugin.
      *
      * @return Instance of the plugin
      */
     public static TotalPermissions getPlugin() {
         return (TotalPermissions) Bukkit.getPluginManager().getPlugin("TotalPermissions");
     }
 
     public TPListener getListener() {
         return listener;
     }
 
     /**
      * Returns the {@link Configuration} that is loaded
      *
      * @return The {@link Configuration} in use
      *
      * @since 1.0
      */
     public Configuration getConfiguration() {
         return config;
     }
 
     /**
      * Returns the backup folder
      *
      * @return The backup folder
      *
      * @since 1.0
      */
     public File getBackupFolder() {
         return new File(this.getDataFolder(), "backups");
     }
 
     /**
      * Returns the location of the last folder used to back up perms. This may
      * not be exact, but uses the folder numbers.
      *
      * @return The File instance of the folder that contains the last backed up
      * files
      *
      * @since 1.0
      */
     public File getLastBackupFolder() {
         int highest = 0;
         File[] files = getBackupFolder().listFiles();
         if (files != null) {
             for (File file : files) {
                 if (file == null) {
                     continue;
                 }
                 if (!file.isDirectory()) {
                     continue;
                 }
                 try {
                     int num = Integer.parseInt(file.getName());
                     if (highest < num) {
                         highest = num;
                     }
                 } catch (NumberFormatException e) {
                 }
             }
         }
         return new File(getBackupFolder(), Integer.toString(highest));
     }
 
     public static String getBukkitVersion() {
         return BUKKIT_VERSION;
     }
 }
