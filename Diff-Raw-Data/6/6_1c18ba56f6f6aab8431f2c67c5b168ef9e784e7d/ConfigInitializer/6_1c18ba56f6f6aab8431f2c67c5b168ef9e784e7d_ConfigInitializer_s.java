 /**
  * Copyright (C) 2011 (nick @ objectdefinitions.com)
  *
  * This file is part of JTimeseries.
  *
  * JTimeseries is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * JTimeseries is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with JTimeseries.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.od.jtimeseries.ui.config;
 
 import com.od.jtimeseries.util.logging.LogMethods;
 import com.od.jtimeseries.util.logging.LogUtils;
 import od.configutil.*;
 
 import javax.swing.*;
 import java.io.File;
 
 /**
  * Created by IntelliJ IDEA.
  * User: nick
  * Date: 18-Nov-2010
  * Time: 21:24:48
  * To change this template use File | Settings | File Templates.
  *
  * Manage the user preferences which define the main config directory
  */
 public class ConfigInitializer {
 
     private static LogMethods logMethods = LogUtils.getLogMethods(ConfigInitializer.class);
 
     private final String MAIN_CONFIG_NAME = "timeSerious";
 
     //the user preference settings, which store the location of the config file directory in an os safe manner
     private PreferenceSettings preferenceSettings = new PreferenceSettings("jtimeseries/timeserious/ui/configs", "configProperties");
     private ConfigManager configManager;
 
     public ConfigInitializer() {
         preferenceSettings.load();
     }
 
     /**
      * Find a config folder from user preferences and create a config manager to load configs
      * Configs may be loaded even if the folder is no longer writable to timeserious
      *
      * @return the config loaded, or the default config
      */
     public TimeSeriousConfig loadConfig() throws ConfigManagerException {
         TimeSeriousConfig result = new TimeSeriousConfig();
 
         if ( preferenceSettings.isConfigDirectorySet()) {
             configManager = createConfigManager(preferenceSettings.getMainConfigDirectory());
             try {
                 result = configManager.loadConfig(MAIN_CONFIG_NAME, TimeSeriousConfig.class);
             } catch (NoConfigFoundException nfe) {
                 logMethods.logWarning("Could not find a config " + MAIN_CONFIG_NAME + " config, will use default config");
             } catch (ConfigManagerException n) {
                 logMethods.logWarning("Could not load " + MAIN_CONFIG_NAME + " config, will use default config", n);
             }
         }
         return result;
     }
 
     /**
      * Check that a configManager has been initialized and is writable
      * If not, prompt the user for a config directory and create a new configManager
      * to save the config
      *
      * @return true, if config was saved, or false if user decides not to save
      * @throws ConfigManagerException if config save fails
      */
     public boolean saveConfig(JFrame mainFrame, TimeSeriousConfig config) throws ConfigManagerException {
         boolean canSave = initializeConfigManager(mainFrame);
         if ( canSave ) {
             doSave(config);
         } else {
             logMethods.logInfo("Not saving, config manager not initialized");
         }
         return canSave;
     }
 
     /**
      * @return true, if a config file directory has been determined and a ConfigManager created so that we can load and save configs
      */
     private boolean isInitializedAndWritable() {
         return configManager != null && configManager.canWrite();
     }
 
     /**
      * If not initialized already, prompt the user to choose a config directory, and use it to initialize the
      * ConfigManager so that we can load and save configs
      * @return true, if the configManager is initialized and we can save
      */
     private boolean initializeConfigManager(JFrame mainFrame) {
         if ( ! isInitializedAndWritable() ) {
             createConfigManagerAtDefaultDir();
 
             //if creating the default config directory has failed, or
             //the default directory is not writable, prompt the user to choose one
             if ( configManager == null || ! configManager.canWrite()) {
                 configManager = showConfigDirectorySelector(mainFrame);
             }
 
             //if the user doesn't select a location, we may still not have a writable
             //config dir, no point storing it
             if ( configManager != null ) {
                 saveConfigManagerPreferences(configManager);
             }
         }
         return isInitializedAndWritable();
     }
 
     private void createConfigManagerAtDefaultDir() {
         File defaultConfigFolder = new File(System.getProperty("user.home"), ".timeSerious");
         if ( ! defaultConfigFolder.exists()) {
              boolean created = defaultConfigFolder.mkdir();
              if ( created) {
                  logMethods.logInfo("Created default config directory at " + defaultConfigFolder);
                  configManager = createConfigManager(defaultConfigFolder);
              } else {
                  logMethods.logInfo("Could not create default config directory at " + defaultConfigFolder);
              }
         }
     }
 
     //the config directory is saved using Preferences mechanism
     //this gives us the ability to find the main config file again on startup
     private void saveConfigManagerPreferences(ConfigManager m) {
         ConfigDirectorySourceAndSink fileSink = (ConfigDirectorySourceAndSink)m.getConfigSink();
         preferenceSettings.setMainConfigDirectory(fileSink.getConfigDirectory());
         preferenceSettings.store();
     }
 
     private ConfigManager showConfigDirectorySelector(JFrame mainFrame) {
         logMethods.logInfo("Showing config directory selector");
         ConfigDirectorySelector s = new ConfigDirectorySelector(mainFrame);
         s.showSelectorDialog();
         File f = s.getSelectedDirectory();
         ConfigManager result = null;
         //if user selects a directory, proceed, or assume they don't want to save
         if(f != null) {
             result = createConfigManager(f);
             if ( ! result.canWrite()) {
                 JOptionPane.showMessageDialog(mainFrame,
                     "Cannot write to this location. Please choose another directory to save the config",
                     "Cannot write config",
                     JOptionPane.WARNING_MESSAGE
                 );
                 result = showConfigDirectorySelector(mainFrame);
             }
         }
         logMethods.logInfo("User selected directory " + f + " configManager initialized: " + isInitializedAndWritable());
         return result;
     }
 
     private ConfigManager createConfigManager(File configDirectory) {
         ConfigManager configManager = new ConfigManagerForTimeSerious();
         configManager.setConfigDirectory(configDirectory);
         return configManager;
     }
 
     private void doSave(TimeSeriousConfig config) throws ConfigManagerException {
         configManager.saveConfig(MAIN_CONFIG_NAME, config);
     }
 
     public void exportConfig(JFrame mainFrame, TimeSeriousConfig c, File selectedFile) {
         try {
             configManager.saveConfig(MAIN_CONFIG_NAME, c, new FileSink(selectedFile));
         } catch (ConfigManagerException e) {
             logMethods.logError("Failed to export config to file " + selectedFile, e);
             JOptionPane.showMessageDialog(mainFrame, "Failed to export config", "Error exporting config", JOptionPane.ERROR_MESSAGE);
         }
 
     }
 
     public TimeSeriousConfig importConfig(JFrame mainFrame, File selectedFile) {
         TimeSeriousConfig result = null;
         try {
            result = configManager.loadConfig(MAIN_CONFIG_NAME, TimeSeriousConfig.class, new FileSource(selectedFile));
         } catch (ConfigManagerException e) {
             logMethods.logError("Failed to import config to file " + selectedFile, e);
             JOptionPane.showMessageDialog(mainFrame, "Failed to import config", "Error importing config", JOptionPane.ERROR_MESSAGE);
         }
         return result;
     }
 }
