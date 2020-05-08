 /*
  * Backup - CraftBukkit server Backup plugin (continued)
  * Copyright (C) 2011 Domenic Horner <https://github.com/gamerx/Backup>
  * Copyright (C) 2011 Lycano <https://github.com/gamerx/Backup>
  *
  * Wormhole X-Treme Worlds Plugin for Bukkit
  * Copyright (C) 2011 Lycano <https://github.com/lycano/Wormhole-X-Treme/>
  *
  * Wormhole X-Treme Worlds Plugin for Bukkit
  * Copyright (C) 2011 Dean Bailey <https://github.com/alron/Wormhole-X-Treme-Worlds>
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
 
 package net.tgxn.bukkit.backup.utils;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Date;
 import org.bukkit.plugin.Plugin;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 public class LogUtils {
     
     private static Level logLevel = Level.INFO;
     private static Logger logger;
     private static Plugin plugin;
     private static File logFile = null;
     private static boolean logReady = false;
     private static FileWriter appendFile;
     private static boolean shouldDisplayLog = true;
 
     /**
      * Main Constructor for LogUtils.
      * Creates logger, sets default log level and variables.
      * 
      * @param plugin The plugin's object.
      */
     public static void initLogUtils(Plugin plugin) {
         if (LogUtils.logger == null) {
             if (plugin != null) {
                 LogUtils.logger = Logger.getLogger(plugin.getServer().getLogger().getName() + "." + plugin.getServer().getName());
             }
             
             LogUtils.logLevel = Level.INFO;
             LogUtils.logger.setLevel(Level.INFO);
             LogUtils.plugin = plugin;
         }
     }
 
     public static void finishInitLogUtils(String logName, boolean shouldDisplay) {
         shouldDisplayLog = shouldDisplay;
         logFile = new File(plugin.getDataFolder() + "/" + logName);
         logReady = true;
         try {
            appendFile  = new FileWriter(logFile, true);
         } catch (IOException ioe) {
 
         }
     }
 
     /**
      * Log messages to file. The message to log.
      * @param message
      */
     public static void logLineToFile(String message) {
         if(logReady) {
             
             Date dateObj = new Date();
             String timeString = dateObj.toString();
 
             String line = timeString + ": " + message;
 
             try {
                 BufferedWriter out =  new BufferedWriter(appendFile);
                 out.write(line);
                 out.close();
             } catch (IOException ioe) {
             }
         }
     }
 
     /**
      * Sets the default loglevel.
      * 
      * @param logLevel 
      */
     public static void setLogLevel(Level logLevel) {
         LogUtils.logLevel = logLevel;
         LogUtils.logger.setLevel(logLevel);
     }
 
     /**
      * Sends log message.
      * 
      * @param message 
      */
     public static void sendLog(String message) {
         sendLog(Level.INFO, message, true);
     }
     /**
      * Sends log message.
      * 
      * @param message
      * @param tags 
      */
     public static void sendLog(String message, boolean tags) {
         sendLog(Level.INFO, message, tags);
     }
     
     /**
      * Sends log message.
      * 
      * @param message
      * @param tags 
      */
     public static void sendLog(Level level, String message) {
         sendLog(level, message, true);
     }
 
     /**
      * Sends log message.
      * 
      * @param logLevel
      * @param message
      * @param tags 
      */
     public static void sendLog(final Level logLevel, String message, boolean tags) {
         final String nameTag = ("[" + plugin.getDescription().getName()  + "] ");
         if(tags)
             message = nameTag + message;
         if(shouldDisplayLog)
             logger.log(logLevel, message);
        // Quick fix for issue #46, Will fix in next release.
        //logLineToFile(message);
     }
     
     /**
      * Gets the current loglevel.
      * 
      * @return The current log level.
      */
     public static Level getLogLevel() {
         return logLevel;
     }
 
 }
