 package bspkrs.worldstatecheckpoints;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.multiplayer.WorldClient;
 import net.minecraft.src.mod_WorldStateCheckpoints;
 import net.minecraft.world.WorldServer;
 
 public class CheckpointManager
 {
     private Minecraft            mc;
     private WorldServer          world;
     public Properties            autoSaveConfig;
     private static Properties    autoSaveConfigDefaults = new Properties();
     private File                 autoConfigFile;
     private File                 autoConfigDir;
     private OutputStream         cfgOutput;
     private InputStream          cfgInput;
     public int                   tickCount;
     public int                   maxTickCount;
     public boolean               autoSaveEnabled;
     public int                   maxAutoSavesToKeep;
     public static final String   ENABLED                = "enabled";
     public static final String   MAX_AUTO_SAVE_ID       = "maxAutoSaveID";
     public static final String   MAX_AUTO_SAVES_TO_KEEP = "maxAutoSavesToKeep";
     public static final String   AUTO_SAVE_PERIOD       = "autoSavePeriod";
     public static final String   PERIOD_UNIT            = "periodUnit";
     public static final String   UNIT_MINUTES           = "minutes";
     public static final String   UNIT_HOURS             = "hours";
     public static final String   UNIT_SECONDS           = "seconds";
     
     public static final String   CHECKPOINT_DIR_NAME    = "checkpoints";
     public static final String   AUTOSAVES_DIR_NAME     = "autosaves";
     public static final String   AUTOSAVES_PREFIX       = "autosave";
     public static final String[] IGNORE_DELETE          = { CHECKPOINT_DIR_NAME, AUTOSAVES_DIR_NAME };
     public static final String[] IGNORE_COPY            = { CHECKPOINT_DIR_NAME, AUTOSAVES_DIR_NAME, "session.lock" };
     public static final String[] IGNORE_NULL            = {};
     
     public CheckpointManager(Minecraft minecraft)
     {
         mc = minecraft;
         world = mc.isIntegratedServerRunning() ? mc.getIntegratedServer().worldServerForDimension(mc.thePlayer.dimension) : null;
         
         autoSaveConfigDefaults.setProperty(ENABLED, mod_WorldStateCheckpoints.autoSaveEnabledDefault);
         autoSaveConfigDefaults.setProperty(MAX_AUTO_SAVE_ID, "0");
         autoSaveConfigDefaults.setProperty(MAX_AUTO_SAVES_TO_KEEP, mod_WorldStateCheckpoints.maxAutoSavesToKeepDefault + "");
         autoSaveConfigDefaults.setProperty(AUTO_SAVE_PERIOD, mod_WorldStateCheckpoints.autoSavePeriodDefault + "");
         autoSaveConfigDefaults.setProperty(PERIOD_UNIT, mod_WorldStateCheckpoints.periodUnitDefault);
         
         loadAutoConfig();
         
         tickCount = maxTickCount - 600;
     }
     
     /**
      * Get this world's current max auto-save ID + 1 and update the max auto-save ID
      * 
      * @return the next available auto-save ID for this world
      */
     public int getNextAutoSaveID()
     {
         int id = 0;
         try
         {
             id = Integer.valueOf(autoSaveConfig.getProperty(MAX_AUTO_SAVE_ID));
         }
         catch (Throwable e)
         {
             e.printStackTrace();
         }
         
         autoSaveConfig.setProperty(MAX_AUTO_SAVE_ID, String.valueOf(++id));
         
         saveAutoConfig(autoSaveConfig);
         
         return id;
     }
     
     public void incrementTickCount()
     {
         if (++tickCount >= maxTickCount)
         {
             setCheckpoint("", true);
             tickCount = 0;
         }
     }
     
     public void saveAutoConfig(Properties prop)
     {
         try
         {
             cfgOutput = new FileOutputStream(autoConfigFile);
             prop.store(cfgOutput, "");
             cfgOutput.close();
         }
         catch (Throwable e)
         {
             e.printStackTrace();
         }
     }
     
     public void loadAutoConfig()
     {
         autoConfigDir = getAutoCheckpointsPath();
         
         if (!autoConfigDir.exists())
             autoConfigDir.mkdirs();
         
         autoConfigFile = chainDirs(autoConfigDir.toString(), "autosave.cfg");
         if (!autoConfigFile.exists())
         {
             try
             {
                 autoConfigFile.createNewFile();
                 this.saveAutoConfig(autoSaveConfigDefaults);
             }
             catch (Throwable e)
             {
                 e.printStackTrace();
             }
         }
         
         try
         {
             cfgInput = new FileInputStream(autoConfigFile);
             autoSaveConfig = new Properties(autoSaveConfigDefaults);
             autoSaveConfig.load(cfgInput);
             cfgInput.close();
         }
         catch (Throwable e)
         {
             e.printStackTrace();
         }
         
         if (!autoSaveConfig.containsKey(MAX_AUTO_SAVES_TO_KEEP))
         {
             autoSaveConfig.setProperty(MAX_AUTO_SAVES_TO_KEEP, "10");
             saveAutoConfig(autoSaveConfig);
         }
         
         maxAutoSavesToKeep = Integer.valueOf(autoSaveConfig.getProperty(MAX_AUTO_SAVES_TO_KEEP));
         
         String unit = autoSaveConfig.getProperty(PERIOD_UNIT);
         
         if (unit.equals(UNIT_SECONDS))
             maxTickCount = Integer.valueOf(autoSaveConfig.getProperty(AUTO_SAVE_PERIOD)) * 20;
         else if (unit.equals(UNIT_HOURS))
             maxTickCount = Integer.valueOf(autoSaveConfig.getProperty(AUTO_SAVE_PERIOD)) * 20 * 60 * 60;
         else
         {
             if (!autoSaveConfig.getProperty(this.PERIOD_UNIT).equalsIgnoreCase(this.UNIT_MINUTES))
             {
                 autoSaveConfig.setProperty(PERIOD_UNIT, UNIT_MINUTES);
                 this.saveAutoConfig(autoSaveConfig);
             }
             maxTickCount = Integer.valueOf(autoSaveConfig.getProperty(AUTO_SAVE_PERIOD)) * 20 * 60;
         }
         
         autoSaveEnabled = autoSaveConfig.getProperty(ENABLED).equalsIgnoreCase("on");
     }
     
     /**
      * Unload the world. You should open menu screen or load different world afterwards.
      */
     public void unloadWorldSilent()
     {
         world = null;
         mc.theWorld.sendQuittingDisconnectingPacket();
         mc.loadWorld((WorldClient) null);
         while (mc.isIntegratedServerRunning())
             try
             {
                 Thread.sleep(20);
             }
             catch (InterruptedException e)
             {}
     }
     
     /**
      * Unload the world with info message shown. You should open menu screen or load different world afterwards.
      */
     public void unloadWorld(String msg)
     {
         world = null;
         mc.theWorld.sendQuittingDisconnectingPacket();
         mc.loadWorld((WorldClient) null, msg);
         while (mc.isIntegratedServerRunning())
             try
             {
                 Thread.sleep(20);
             }
             catch (InterruptedException e)
             {}
     }
     
     /**
      * Start world
      * 
      * @param folder
      *            save dir name
      * @param name
      *            user-entered world name
      */
     public void startWorld(String folder, String name)
     {
         mc.launchIntegratedServer(folder, name, null);
         mc = null;
     }
     
     public String getWorldSavePath()
     {
         return Minecraft.getMinecraftDir() + getWorldSaveRelativePath();
     }
     
     public String getWorldSaveRelativePath()
     {
         return "/saves/" + world.getSaveHandler().getSaveDirectoryName();
     }
     
     /**
      * Get world's save directory
      * 
      * @return the folder
      */
     public File getWorldPath()
     {
         return new File(Minecraft.getMinecraftDir() + getWorldSaveRelativePath());
     }
     
     /**
      * Get world's name
      * 
      * @return the name of the world
      */
     public String getWorldName()
     {
         return world.getWorldInfo().getWorldName();
     }
     
     /**
      * Get world's save directory + the checkpoint directory
      * 
      * @return the folder
      */
     public File getCheckpointsPath(boolean isAutoSave)
     {
         if (isAutoSave)
             return chainDirs(getWorldPath().toString(), AUTOSAVES_DIR_NAME);
         else
             return chainDirs(getWorldPath().toString(), CHECKPOINT_DIR_NAME);
     }
     
     /**
      * Get world's save directory + the checkpoint directory + the auto-save directory
      * 
      * @return the folder
      */
     public File getAutoCheckpointsPath()
     {
         return chainDirs(getWorldPath().toString(), AUTOSAVES_DIR_NAME);
     }
     
     /**
      * Create a checkpoint (snapshot) of the currently loaded world, using the given name as suffix after current date and time.
      * 
      * @param name
      *            user-provided name
      * @param isAutoSave
      *            whether or not this is an auto-saved checkpoint
      */
     public void setCheckpoint(String name, boolean isAutoSave)
     {
         try
         {
             mc.getIntegratedServer().getConfigurationManager().saveAllPlayerData();
             boolean canNotSave = world.canNotSave;
             world.canNotSave = false;
             world.saveAllChunks(true, null);
             world.canNotSave = canNotSave;
             
             File targetDir;
             String checkpointName;
             
             File worldDir = getWorldPath();
             if (isAutoSave)
                 checkpointName = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + "!" + AUTOSAVES_PREFIX + " " + getNextAutoSaveID();
             else
                 checkpointName = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + "!" + name;
             
             targetDir = chainDirs(getCheckpointsPath(isAutoSave).toString(), checkpointName);
             
             new DirectoryCopier(worldDir, targetDir, IGNORE_COPY, isAutoSave);
             
             if (isAutoSave)
             {
                 if (maxAutoSavesToKeep > 0 && getCheckpointsCount(isAutoSave) > maxAutoSavesToKeep)
                     new OldAutosaveRemover();
             }
         }
         catch (Throwable e)
         {
             e.printStackTrace();
         }
     }
     
     /**
      * Save checkpoint into an existing directory, preserving the filename.
      * 
      * @param dirname
      */
     public void saveCheckpointInto(String dirname)
     {
         saveCheckpointInto(dirname, dirname);
     }
     
     /**
      * Save checkpoint into already existing folder. Delete the folder and recreate it with modified name.
      * 
      * @param dirname_orig
      *            original name
      * @param dirname_new
      *            new modified name
      */
     public void saveCheckpointInto(String dirname_orig, String dirname_new)
     {
         deleteDirAndContents(chainDirs(getCheckpointsPath(false).toString(), dirname_orig));
         try
         {
             world.saveAllChunks(true, null);
             File worldDir = getWorldPath();
             File targetDir = chainDirs(getCheckpointsPath(false).toString(), dirname_new);
             copyDirectory(worldDir, targetDir, IGNORE_COPY);
         }
         catch (Throwable e)
         {
             e.printStackTrace();
         }
     }
     
     /**
      * Delete single checkpoint
      * 
      * @param dirname
      *            checkpoint to delete (only dir name)
      */
     public void deleteCheckpoint(String dirname, boolean isAutoSave)
     {
         deleteDirAndContents(chainDirs(getCheckpointsPath(isAutoSave).toString(), dirname));
     }
     
     /**
      * Gets the folder name of the oldest checkpoint
      * 
      * @param isAutoSave
      *            whether we want an autosave checkpoint or a named checkpoint directory name
      * @return the directory name of the oldest named or autosaved checkpoint
      */
     public String getOldestCheckpointDirName(boolean isAutoSave)
     {
         File[] files = getCheckpointsPath(isAutoSave).listFiles(new DirFilter());
         
         if (files.length > 0)
             Arrays.sort(files);
         else
             return "invalidDirName";
         
         return files[0].getName();
     }
     
     /**
      * Load checkpoint with the given directory name
      * 
      * @param checkpointName
      *            the directory with checkpoint
      */
     public void loadCheckpoint(String checkpointName, boolean isAutoSave)
     {
         File worldDir = getWorldPath();
         String worldName = getWorldName();
         
         File checkpointDir = chainDirs(getCheckpointsPath(isAutoSave).toString(), checkpointName);
         
         unloadWorld("Loading checkpoint...");
         deleteDirContents(worldDir, IGNORE_DELETE);
         copyDirectory(checkpointDir, worldDir, IGNORE_NULL);
         startWorld(worldDir.getName(), worldName);
     }
     
     /**
      * Connect strings using the default OS folder separator to a File object.
      * 
      * @param files
      *            strings representing folders or files.
      * @return the File obtained by joining these strings.
      */
     public File chainDirs(String... files)
     {
         if (files.length == 1)
             return new File(files[0]);
         else
         {
             File tmp = new File(files[0]);
             for (int i = 1; i < files.length; i++)
                 tmp = new File(tmp, files[i]);
             
             return tmp;
         }
     }
     
     /**
      * Copy directory from source to target location recursively, ignoring strings in the "ignore" array. Target location will be created if
      * needed. Source directory is not copied, only its contents.
      * 
      * @param sourceLocation
      *            source
      * @param targetLocation
      *            target
      * @param ignore
      *            array of ignored names (strings)
      * @throws IOException
      */
     public void copyDirectory(File sourceLocation, File targetLocation, String[] ignore)
     {
         if (sourceLocation.isDirectory())
         {
             if (!targetLocation.exists())
                 targetLocation.mkdirs();
             
             String[] children = sourceLocation.list();
             for (int i = 0; i < children.length; i++)
             {
                 boolean ignored = false;
                 for (String str : ignore)
                     if (str.equals(children[i]))
                     {
                         ignored = true;
                         break;
                     }
                 
                 if (!ignored)
                     copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]), ignore);
             }
         }
         else
         {
             boolean ignored = false;
             for (String str : ignore)
                 if (str.equals(sourceLocation.getName()))
                 {
                     ignored = true;
                     break;
                 }
             
             if (!ignored)
             {
                 try
                 {
                     InputStream in = new FileInputStream(sourceLocation);
                     OutputStream out = new FileOutputStream(targetLocation);
                     
                     // Copy the bits from instream to outstream
                     byte[] buf = new byte[1024];
                     int len;
                     while ((len = in.read(buf)) > 0)
                         out.write(buf, 0, len);
                     
                     in.close();
                     out.close();
                 }
                 catch (IOException ioe)
                 {
                     ioe.printStackTrace();
                 }
             }
         }
     }
     
     /**
      * Delete directory and all contents recursively.
      * 
      * @param dir
      * @return
      */
     public boolean deleteDirAndContents(File dir)
     {
         if (dir.isDirectory())
         {
             String[] children = dir.list();
             for (int i = 0; i < children.length; i++)
             {
                 boolean success = deleteDirAndContents(new File(dir, children[i]));
                 if (!success)
                     return false;
             }
         }
         return dir.delete();
     }
     
     /**
      * Delete directory contents recursively. Leaves the specified starting directory empty. Ignores files / dirs listed in "ignore" array.
      * 
      * @param dir
      *            directory to delete
      * @param ignore
      *            ignored files
      * @return true on success
      */
     public boolean deleteDirContents(File dir, String[] ignore)
     {
         if (dir.isDirectory())
         {
             String[] children = dir.list();
             for (int i = 0; i < children.length; i++)
             {
                 boolean ignored = false;
                 for (String str : ignore)
                     if (str.equals(children[i]))
                     {
                         ignored = true;
                         break;
                     }
                 
                 if (!ignored)
                 {
                     boolean success = deleteDirContents(new File(dir, children[i]), ignore);
                     if (!success)
                         return false;
                 }
             }
         }
         else
         {
             dir.delete();
         }
         return true;
     }
     
     /**
      * @return count of checkpoints saved
      */
     public int getCheckpointsCount(boolean isAutoSave)
     {
         File chpdir = getCheckpointsPath(isAutoSave);
         
         return (chpdir.listFiles(new DirFilter()) != null ? chpdir.listFiles(new DirFilter()).length : 0);
     }
     
     /**
      * @return true if any checkpoints are stored
      */
     public boolean getHasCheckpoints(boolean isAutoSave)
     {
         return getCheckpointsCount(isAutoSave) > 0;
     }
     
     private class DirFilter implements FileFilter
     {
         @Override
         public boolean accept(File pathname)
         {
             return pathname.isDirectory();
         }
     }
     
     /**
      * Get all checkpoints stored.
      * 
      * @return File[] all checkpoints.
      */
     public File[] getCheckpoints(boolean isAutoSave)
     {
         File[] files = getCheckpointsPath(isAutoSave).listFiles(new DirFilter());
         
         if (files.length > 0)
             Arrays.sort(files);
         
         List<File> list = Arrays.asList(files);
         Collections.reverse(list);
         return list.toArray(new File[list.size()]);
     }
     
     private class DirectoryCopier implements Runnable
     {
         File     src, tgt;
         String[] ignoreList;
         boolean  isAutoSave;
         Thread   t;
         
         DirectoryCopier(File sourceLocation, File targetLocation, String[] ignore, boolean isAutoSave)
         {
             src = sourceLocation;
             tgt = targetLocation;
             ignoreList = ignore;
             this.isAutoSave = isAutoSave;
             t = new Thread(this, "DirectoryCopier");
             t.start();
         }
         
         @Override
         public void run()
         {
             if (isAutoSave)
                mc.thePlayer.addChatMessage("WSC: Saving checkpoint...");
             try
             {
                 copyDirectory(src, tgt, ignoreList);
                 if (isAutoSave)
                    mc.thePlayer.addChatMessage("WSC: Checkpoint \"" + tgt.getName().split("!")[1] + "\" saved.");
             }
             catch (Throwable e)
             {
                mc.thePlayer.addChatMessage("Error encountered during checkpoint save!  Checkpoint saved in \"" + tgt.getName() + "\" may be invalid! See log for details.");
                e.printStackTrace();
             }
         }
     }
     
     private class OldAutosaveRemover implements Runnable
     {
         Thread t;
         
         OldAutosaveRemover()
         {
             t = new Thread(this, "OldAutosaveRemover");
             t.start();
         }
         
         @Override
         public void run()
         {
             while (maxAutoSavesToKeep > 0 && getCheckpointsCount(true) > maxAutoSavesToKeep)
                 deleteCheckpoint(getOldestCheckpointDirName(true), true);
         }
     }
 }
