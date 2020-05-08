 /*
  * This file is part of QuarterBukkit-Integration.
  * Copyright (c) 2012 QuarterCode <http://www.quartercode.com/>
  *
  * QuarterBukkit-Integration is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * QuarterBukkit-Integration is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with QuarterBukkit-Integration. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.quartercode.quarterbukkit;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.InvalidDescriptionException;
 import org.bukkit.plugin.InvalidPluginException;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.UnknownDependencyException;
 import com.quartercode.quarterbukkit.api.FileUtils;
 import com.quartercode.quarterbukkit.api.query.FilesQuery;
 import com.quartercode.quarterbukkit.api.query.FilesQuery.ProjectFile;
 import com.quartercode.quarterbukkit.api.query.FilesQuery.VersionParser;
 import com.quartercode.quarterbukkit.api.query.QueryException;
 
 /**
  * This class is used for integrating QuarterBukkit into a {@link Plugin}.
  */
 public class QuarterBukkitIntegration {
 
     private static final String PLUGIN_NAME = "QuarterBukkit-Plugin";
     private static final int    PROJECT_ID  = 47006;
 
     // The plugins which called the integrate() method
     private static Set<Plugin>  callers     = new HashSet<Plugin>();
     // Determinates if the integration process was already invoked
     private static boolean      invoked     = false;
 
     /**
      * Call this method in onEnable() for integrating QuarterBukkit into your plugin.
      * It creates a config where the user has to turn a value to "Yes" for the actual installation.
      * The class notfies him on the console and every time an op joins to the server.
      * 
      * @param plugin The {@link Plugin} which tries to integrate QuarterBukkit.
      * @return True if QuarterBukkit can be used after the call, false if not.
      */
     public static boolean integrate(final Plugin plugin) {
 
         // Register caller
         callers.add(plugin);
 
         if (!Bukkit.getPluginManager().isPluginEnabled(PLUGIN_NAME)) {
             if (!invoked) {
                 // Block this part (it should only be called once)
                 invoked = true;
 
                 // Read installation confirmation file
                 File installConfigFile = new File("plugins/" + PLUGIN_NAME, "install.yml");
 
                 try {
                     if (!installConfigFile.exists()) {
                         // No installation confirmation file -> create a new one and wait until restart
                         YamlConfiguration installConfig = new YamlConfiguration();
                         installConfig.set("install-" + PLUGIN_NAME, true);
                         installConfig.save(installConfigFile);
                     } else {
                         YamlConfiguration installConfig = YamlConfiguration.loadConfiguration(installConfigFile);
                         if (installConfig.isBoolean("install-" + PLUGIN_NAME) && installConfig.getBoolean("install-" + PLUGIN_NAME)) {
                             // Installation confirmed -> install
                             installConfigFile.delete();
                             install(new File("plugins", PLUGIN_NAME + ".jar"));
                             return true;
                         }
                     }
 
                     // Schedule with a time because the integrating plugin might get disabled
                     new Timer().schedule(new TimerTask() {
 
                         @Override
                         public void run() {
 
                             Bukkit.broadcastMessage(ChatColor.YELLOW + "===============[ " + PLUGIN_NAME + " Installation ]===============");
                             String plugins = "";
                             for (Plugin caller : callers) {
                                 plugins += ", " + caller.getName();
                             }
                             plugins = plugins.substring(2);
                            Bukkit.broadcastMessage(ChatColor.RED + "For using " + plugins + " which requires " + PLUGIN_NAME + ", you should " + ChatColor.DARK_AQUA + "restart" + ChatColor.RED + " the server!");
                         }
                    }, 100, 3 * 1000);
                 }
                 catch (UnknownHostException e) {
                     Bukkit.getLogger().warning("Can't connect to dev.bukkit.org for installing " + PLUGIN_NAME + "!");
                 }
                 catch (Exception e) {
                     Bukkit.getLogger().severe("An error occurred while installing " + PLUGIN_NAME + " (" + e + ")");
                     e.printStackTrace();
                 }
             }
 
             return false;
         } else {
             return true;
         }
     }
 
     private static void install(File target) throws QueryException, IOException, UnknownDependencyException, InvalidPluginException, InvalidDescriptionException {
 
         // ----- Get Latest Version -----
 
         Bukkit.getLogger().info("===============[ " + PLUGIN_NAME + " Installation ]===============");
 
         Bukkit.getLogger().info("Querying server mods api ...");
 
         // Get latest version
         List<ProjectFile> avaiableFiles = new FilesQuery(PROJECT_ID, new VersionParser() {
 
             @Override
             public String parseVersion(ProjectFile file) {
 
                 return file.getName().replace("QuarterBukkit ", "");
             }
         }).execute();
         if (avaiableFiles.size() == 0) {
             // No file avaiable
             return;
         }
         ProjectFile latestFile = avaiableFiles.get(avaiableFiles.size() - 1);
 
         Bukkit.getLogger().info("Found the latest version of " + PLUGIN_NAME + ": " + latestFile.getVersion());
 
         // ----- Download and Installation -----
 
         Bukkit.getLogger().info("Installing " + PLUGIN_NAME + " " + latestFile.getVersion());
 
         // Variables
         File pluginDir = callers.iterator().next().getDataFolder().getParentFile();
 
         // Download zip
         File zip = new File(pluginDir, latestFile.getFileName());
         FileUtils.download(latestFile.getLocation().toURL(), zip);
 
         // Unzip zip
         File unzipDir = new File(zip.getParent(), zip.getName() + "_extract");
         FileUtils.unzip(zip, unzipDir);
         FileUtils.delete(zip);
 
         // Get inner directory
         File innerUnzipDir = unzipDir.listFiles()[0];
 
         // Overwrite current plugin jar
         File pluginJar = new File(pluginDir, PLUGIN_NAME + ".jar");
         // FileUtils.delete(pluginJar);
         FileUtils.copy(new File(innerUnzipDir, pluginJar.getName()), pluginJar);
 
         // Delete temporary unzip dir
         FileUtils.delete(unzipDir);
 
         // Load plugin from file
         Bukkit.getPluginManager().enablePlugin(Bukkit.getPluginManager().loadPlugin(pluginJar));
 
         Bukkit.getLogger().info("Successfully installed " + PLUGIN_NAME + " " + latestFile.getVersion() + "!");
     }
 
 }
