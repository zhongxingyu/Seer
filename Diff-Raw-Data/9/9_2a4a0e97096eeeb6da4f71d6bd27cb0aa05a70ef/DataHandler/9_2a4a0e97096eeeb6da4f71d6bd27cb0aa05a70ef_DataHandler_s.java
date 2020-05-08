 package de.minestar.core.units;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.logging.Logger;
 
 import net.minecraft.server.v1_7_R1.EntityPlayer;
 import net.minecraft.server.v1_7_R1.NBTTagCompound;
 import de.minestar.core.MinestarCore;
import de.minestar.minestarlibrary.data.tools.CompressedStreamTools;
 
 public class DataHandler {
     /** Reference to the logger. */
     private static final Logger logger = Logger.getLogger("Minecraft");
 
     private final File worldDir;
 
     /** The directory in which to save player data. */
 
     public DataHandler() {
         this.worldDir = new File(MinestarCore.dataFolder, "worlds");
         this.worldDir.mkdirs();
     }
 
     public boolean playerDataExists(String worldName, String playerName) {
         File playersDirectory = new File(this.worldDir, worldName);
         playersDirectory.mkdirs();
         File var4 = new File(playersDirectory, playerName + ".dat");
         return var4.exists();
     }
 
     /**
      * Writes the player data to disk from the specified PlayerEntityMP.
      */
     public void savePlayerData(String worldName, EntityPlayer entityPlayer) {
         try {
             File playersDirectory = new File(this.worldDir, worldName);
             playersDirectory.mkdirs();
 
             NBTTagCompound var2 = new NBTTagCompound();
             entityPlayer.b(var2);
             File var3 = new File(playersDirectory, entityPlayer.getName() + ".dat.tmp");
             File var4 = new File(playersDirectory, entityPlayer.getName() + ".dat");
            CompressedStreamTools.writeGzippedCompoundToOutputStream(var2, new FileOutputStream(var3));
 
             if (var4.exists()) {
                 var4.delete();
             }
 
             var3.renameTo(var4);
         } catch (Exception var5) {
             logger.warning("Failed to save player data for " + entityPlayer.getName());
         }
     }
 
     /**
      * Reads the player data from disk into the specified PlayerEntityMP.
      */
     public void loadPlayerData(String worldName, EntityPlayer entityPlayer) {
         NBTTagCompound par1NBTTagCompound = this.loadPlayerDataFromFile(worldName, entityPlayer.getName());
 
         if (par1NBTTagCompound != null) {
             entityPlayer.a(par1NBTTagCompound);
         }
     }
 
     /**
      * Gets the player data for the given playername as a NBTTagCompound.
      */
     private NBTTagCompound loadPlayerDataFromFile(String worldName, String playerName) {
         try {
             File playersDirectory = new File(this.worldDir, worldName);
             playersDirectory.mkdirs();
 
             File var2 = new File(playersDirectory, playerName + ".dat");
 
             if (var2.exists()) {
                return CompressedStreamTools.loadGzippedCompoundFromOutputStream(new FileInputStream(var2));
             }
         } catch (Exception var3) {
             logger.warning("Failed to load player data for " + playerName);
         }
 
         return null;
     }
 
 }
