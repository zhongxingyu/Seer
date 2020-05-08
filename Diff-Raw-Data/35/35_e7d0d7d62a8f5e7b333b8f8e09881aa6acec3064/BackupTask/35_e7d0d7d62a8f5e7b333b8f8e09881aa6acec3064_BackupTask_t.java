 /*
  *  Backup - CraftBukkit server Backup plugin (continued)
  *  Copyright (C) 2011 Domenic Horner <https://github.com/gamerx/Backup>
  *  Copyright (C) 2011 Lycano <https://github.com/gamerx/Backup>
  *
  *  Backup - CraftBukkit server Backup plugin (original author)
  *  Copyright (C) 2011 Kilian Gaertner <https://github.com/Meldanor/Backup>
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.tgxn.bukkit.backup.threading;
 
 import java.util.List;
 import java.io.FileFilter;
 import org.bukkit.entity.Player;
 import net.tgxn.bukkit.backup.BackupMain;
 import net.tgxn.bukkit.backup.config.Settings;
 import net.tgxn.bukkit.backup.config.Strings;
 import net.tgxn.bukkit.backup.utils.FileUtils;
 import net.tgxn.bukkit.backup.utils.LogUtils;
 import org.bukkit.Server;
 import org.bukkit.plugin.Plugin;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.LinkedList;
 import java.util.logging.Level;
 
 import static net.tgxn.bukkit.backup.utils.FileUtils.FILE_SEPARATOR;
 
 /**
  * The Task copies and backups the worlds and delete older backups. This task
  * is only runes once in backup and doing all the thread safe options.
  * The PrepareBackupTask and BackupTask are two threads to find a compromise between
  * security and performance.
  *
  * @author Kilian Gaertner
  */
 public class BackupTask implements Runnable {
 
     private final Settings settings;
     private Strings strings;
     private Plugin plugin;
     private final LinkedList<String> worldsToBackup;
     private List<String> skippedPlugins;
     private final Server server;
     public boolean splitbackup;
     public boolean ShouldZIP;
 
     /**
      * The main backup constructor.
      * 
      * @param settings The settings object, to get the plugins settings.
      * @param strings Strings object, for all string values
      * @param worldsToBackup The list of worlds that need to be backed up.
      * @param server The server we are backing up.
      */
     public BackupTask(Settings settings, Strings strings, LinkedList<String> worldsToBackup, Server server) {
         this.settings = settings;
         this.worldsToBackup = worldsToBackup;
         this.server = server;
         this.plugin = server.getPluginManager().getPlugin("Backup");
         this.strings = strings;
     }
 
     @Override
     public void run() {
         backup();
     }
 
     /**
      * Run the backup.
      */
     public void backup() {
 
         // Settings.
         String backupPath = settings.getStringProperty("backuppath").concat(FILE_SEPARATOR);
         String backupDirName = backupPath.concat(getFolderName());
         boolean BackupWorlds = settings.getBooleanProperty("backupworlds");
         boolean BackupPlugins = settings.getBooleanProperty("backupplugins");
         boolean backupeverything = settings.getBooleanProperty("backupeverything");
 
         // Globals.
         splitbackup = settings.getBooleanProperty("splitbackup");
         ShouldZIP = settings.getBooleanProperty("zipbackup");
         skippedPlugins = Arrays.asList(settings.getStringProperty("skipplugins").split(";"));
 
         // We are performing a full server backup.
         if (backupeverything) {
 
             // Setup FileFilter to exclude the backups path.
             FileFilter ff = new FileFilter() {
 
                 /**
                  * Files to accept/deny.
                  */
                 @Override
                 public boolean accept(File f) {
 
                     // Disallow server.log and the backuppath.
                     if (f.getName().equals(settings.getStringProperty("backuppath"))) {
                         return false;
                     }
 
                     if (f.getName().equals("server.log")) {
                         return false;
                     }
 
                     return true;
                 }
             };
 
             // Setup Source and destination DIR's.
             File srcDIR = new File("./");
             File destDIR = new File(backupDirName);
 
             // Copy this world into the backup directory, in a folder called the worlds name.
             try {
 
                 // Copy the directory.
                 FileUtils.copyDirectory(srcDIR, destDIR, ff, true);
 
                 // Perform the zipping action. 
                 doZIP(backupDirName);
 
             } catch (FileNotFoundException fnfe) {
 
                 LogUtils.sendLog("Full backup: Source not found.");
                 //fnfe.printStackTrace(System.out);
 
             } catch (IOException ioe) {
 
                 LogUtils.sendLog("Error occurred while performing backup: IO Exception.");
                 server.broadcastMessage(strings.getString("backupfailed"));
 
                 //ioe.printStackTrace(System.out);
             }
 
             // If we are just backing up worlds/plugins.
         } else {
 
             // If there are worlds to backup, and we are performing world backups.
             if ((worldsToBackup != null) && (BackupWorlds)) {
 
                 // While we have a worlds to backup, this loops each world.
                 while (!worldsToBackup.isEmpty()) {
 
                     // Remove first world from the array and put it into a var.
                     String worldName = worldsToBackup.removeFirst();
 
 
                     if (splitbackup) {
                         // Split into world folders.
 
                         // Check this worlds folder exists.
                         File woldBUfolder = new File(backupPath.concat(FILE_SEPARATOR).concat(worldName));
 
                         // Try to create the folder.
                         try {
                             if (!woldBUfolder.exists()) {
                                 woldBUfolder.mkdirs();
                             }
                         } catch (SecurityException se) {
 
                             // Advise this failed.
                             LogUtils.sendLog(Level.SEVERE, "Failed to make the world folder: Security Exception.");
                             //se.printStackTrace(System.out);
                         }
 
                         // "backups/world/30092011-142238"
                         String thisbackupfname = backupPath.concat(worldName).concat(FILE_SEPARATOR).concat(getFolderName());
 
                         // Copy the world into its backup folder.
                         try {
                             FileUtils.copyDirectory(worldName, thisbackupfname);
                         } catch (IOException ioe) {
 
                             // Advise this failed.
                             LogUtils.sendLog(Level.SEVERE, "Failed to Copy the world's folder: IO Exception.");
                             //ioe.printStackTrace(System.out);
                         }
                         // ZIP if required.
                         doZIP(thisbackupfname);
 
                     } else {
                         // Not split backup.
 
                         try {
 
                             // Copy this world into the backup directory, in a folder called the worlds name.
                             FileUtils.copyDirectory(worldName, backupDirName.concat(FILE_SEPARATOR).concat(worldName));
 
                         } catch (FileNotFoundException ex) {
                             ex.printStackTrace(System.out);
                         } catch (IOException e) {
                             LogUtils.sendLog(Level.WARNING, strings.getStringWOPT("errorcreatetemp", worldName), true);
                             /** @TODO create exception classes **/
                             e.printStackTrace(System.out);
                             server.broadcastMessage(strings.getString("backupfailed"));
                         }
                     }
                 }
             } else {
                 LogUtils.sendLog(Level.INFO, strings.getString("skipworlds"), true);
             }
 
             // We are backing up plugins.
             if (BackupPlugins) {
 
                 // Setup FileFilter to exclude the some plugins.
                 FileFilter ffplugins = new FileFilter() {
 
                     /**
                      * Files to accept/deny.
                      */
                     @Override
                     public boolean accept(File f) {
 
                         // Check if there are ignored plugins
                         if (skippedPlugins.size() > 0 && !skippedPlugins.get(0).isEmpty()) {
 
                             // Loop each plugin.
                             for (int i = 0; i < skippedPlugins.size(); i++) {
 
                                 // Check if the current plugin matches the string.
                                 if (skippedPlugins.get(i).equals(f.getName())) {
                                     return false;
                                 }
                             }
                         }
                         return true;
                     }
                 };
 
                 // Setup Source and destination DIR's.
                 File srcDIR = new File("plugins");
 
                 // Touch the folder to update the modified date.
                 srcDIR.setLastModified(System.currentTimeMillis());
                 String destDIR;
 
                 if (splitbackup) {
                     // Splitting backup.
 
                     // "backups/plugins/30092011-142238"
                     String thisbackupfname = backupPath.concat("plugins").concat(FILE_SEPARATOR).concat(getFolderName());
 
                     destDIR = thisbackupfname;
                 } else {
                     destDIR = backupDirName.concat(FILE_SEPARATOR).concat("plugins");
                 }
 
                 // Copy this world into the backup directory, in a folder called the worlds name.
                 try {
                     if (skippedPlugins.size() > 0 && !skippedPlugins.get(0).isEmpty()) {
                         // Log what plugins are disabled.
                         LogUtils.sendLog(strings.getString("disabledplugins"));
                         LogUtils.sendLog(skippedPlugins.toString());
 
                     }
 
                     // erform the copy.
                     FileUtils.copyDirectory(srcDIR, new File(destDIR), ffplugins, true);
 
                     // Perform ZIp.
                     if (splitbackup) {
                         doZIP(destDIR);
                     }
 
 
                 } catch (FileNotFoundException ex) {
                     ex.printStackTrace(System.out);
                 } catch (IOException e) {
                     LogUtils.sendLog("Error backing up plugins.");
                     /** @TODO create exception classes **/
                     e.printStackTrace(System.out);
                     server.broadcastMessage(strings.getString("backupfailed"));
                 }
 
 
             } else {
                 LogUtils.sendLog(Level.INFO, strings.getString("skipplugins"), true);
             }
 
             // All in one folder.
             if (!splitbackup) {
                 doZIP(backupDirName);
             }
 
         }
 
         // Delete old backups.
         if(!deleteOldBackups())
             LogUtils.sendLog("Failed to delete old backups.");
 
         // Clean up.
         finish();
 
     }
 
     /**
      * Get the name of this backups folder.
      * 
      * @return The name, as a string.
      */
     private String getFolderName() {
 
         // Get the calendar, and initalize the date format string.
         Calendar calendar = Calendar.getInstance();
         String formattedDate;
 
         // Java string (and date) formatting:
         // http://download.oracle.com/javase/1.5.0/docs/api/java/util/Formatter.html#syntax
         try {
             formattedDate = String.format(settings.getStringProperty("dateformat"), calendar);
         } catch (Exception e) {
             LogUtils.sendLog(Level.WARNING, strings.getString("errordateformat"), true);
             formattedDate = String.format("%1$td%1$tm%1$tY-%1$tH%1$tM%1$tS", calendar);
 
             // @TODO write exception class
             System.out.println(e);
         }
         return formattedDate;
     }
 
     /**
      * ZIP the path specified.
      * 
      * @param path The path to ZIP and delete.
      */
     private void doZIP(String path) {
 
         // Check we are ZIPing the backups.
         if (ShouldZIP) {
             try {
 
                 // Add backup folder to a ZIP.
                 FileUtils.zipDir(path, path);
 
                 // Delete the original backup directory.
                 FileUtils.deleteDirectory(new File(path));
             } catch (IOException ioe) {
 
                 // Advise this failed.
                 LogUtils.sendLog("Failed to ZIP Backup: IO Exception.");
                 //ioe.printStackTrace(System.out);
             }
         }
     }
 
     /**
      * Check whether there are more backups as allowed to store. 
      * When this case is true, it deletes oldest ones.
      */
     private boolean deleteOldBackups() {
        
         // Get the backup's directory.
         File backupDir = new File(settings.getStringProperty("backuppath"));
        
         // Check if split backup or not.
         if (splitbackup) {
            try {
                // Loop the folders, and clean for each.
                File[] foldersToClean = backupDir.listFiles();
                for (int l = 0; l < foldersToClean.length; l++)
                     cleanFolder(foldersToClean[l]);
            } catch (IOException ioe) {
                //ioe.printStackTrace(System.out);
                return false;
            } catch (NullPointerException npe) {
                //npe.printStackTrace(System.out);
                return false;
             }

         } else {
 
             // Clean entire directory.
             try {
                 cleanFolder(backupDir);
             } catch (IOException ioe) {
                 //ioe.printStackTrace(System.out);
                 return false;
            } catch (NullPointerException npe) {
                //npe.printStackTrace(System.out);
                return false;
             }
         }
         return true;
     }
 
    private void cleanFolder(File backupDir) throws IOException, NullPointerException {
 
         // Get properties.
         try {
             final int maxBackups = settings.getIntProperty("maxbackups");
 
             // Store all backup files in an array.
             File[] filesList = backupDir.listFiles();
 
             // If the amount of files exceeds the max backups to keep.
             if (filesList.length > maxBackups) {
                 ArrayList<File> backupList = new ArrayList<File>(filesList.length);
                 backupList.addAll(Arrays.asList(filesList));
 
                 int maxModifiedIndex;
                 long maxModified;
 
                 //Remove the newst backups from the list.
                 for (int i = 0; i < maxBackups; ++i) {
                     maxModifiedIndex = 0;
                     maxModified = backupList.get(0).lastModified();
                     for (int j = 1; j < backupList.size(); ++j) {
                         File currentFile = backupList.get(j);
                         if (currentFile.lastModified() > maxModified) {
                             maxModified = currentFile.lastModified();
                             maxModifiedIndex = j;
                         }
                     }
                     backupList.remove(maxModifiedIndex);
                 }
 
                 // Inform the user what backups are being deleted.
                 LogUtils.sendLog(strings.getString("removeold"));
                 LogUtils.sendLog(Arrays.toString(backupList.toArray()));
 
                 // Finally delete the backups.
                 for (File backupToDelete : backupList) {
                     deleteDir(backupToDelete);
                 }
             }
         } catch (Exception e) {
             //@TODO write exception class
             e.printStackTrace(System.out);
         }
 
 
 
     }
 
     public boolean deleteDir(File dir) {
         if (dir.isDirectory()) {
             String[] children = dir.list();
             for (int i = 0; i < children.length; i++) {
                 boolean success = deleteDir(new File(dir, children[i]));
                 if (!success) {
                     return false;
                 }
             }
         }
         return dir.delete();
     }
 
     /**
      * Creates a temporary Runnable that is running on the main thread by the scheduler to prevent thread problems.
      */
     private void finish() {
         Runnable run = new Runnable() {
 
             @Override
             public void run() {
                 if (settings.getBooleanProperty("enableautosave")) {
                     server.dispatchCommand(server.getConsoleSender(), "save-on");
                 }
 
                 // Inform players backup has finished.
                 String completedBackupMessage = strings.getString("backupfinished");
 
                 if (completedBackupMessage != null && !completedBackupMessage.trim().isEmpty()) {
 
                     // Verify Permissions
                     if (BackupMain.Permissions != null) {
 
                         // Get all players.
                         Player[] players = server.getOnlinePlayers();
 
                         // Loop through all online players.
                         for (int i = 0; i < players.length; i++) {
                             Player currentplayer = players[i];
 
                             // If the current player has the right permissions, notify them.
                             if (BackupMain.Permissions.has(currentplayer, "backup.notify")) {
                                 currentplayer.sendMessage(completedBackupMessage);
                             }
                         }
 
                         // Send message to log, to be sure.
                         LogUtils.sendLog(completedBackupMessage);
 
                     } else {
 
                         // If there are no permissions, notify all.
                         server.broadcastMessage(completedBackupMessage);
                     }
                 }
             }
         };
         server.getScheduler().scheduleSyncDelayedTask(plugin, run);
     }
 }
