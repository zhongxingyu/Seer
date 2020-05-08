 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.nationsmc.chunkloader;
 
 import com.nationsmc.tasks.loadChunks;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.Chunk;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author Koo
  */
 public class ChunkLoader extends JavaPlugin {
 
     protected static Logger logger = null;
     public static boolean debugMode = false;
     public static boolean runLoader = false;
     public Configuration configuration;
     protected Server server;
     protected PluginManager pm;
     protected File dataFolder;
     private loadChunks lc;
     static final int PROGRESSBAR_LENGTH = 20;
 
     @Override
     public void onEnable() {
         logger = this.getLogger();
 
         this.server = this.getServer();
         this.pm = this.server.getPluginManager();
         this.dataFolder = this.getDataFolder();
 
         this.dataFolder.mkdirs();
 
         configuration = this.getConfig();
         configuration.addDefault("debug", false);
         configuration.addDefault("runLoader", true);
         debugMode = configuration.getBoolean("debug");
         runLoader = configuration.getBoolean("runLoader");
 
         this.saveConfig();
         loadAllChunks();
 
         log("Version " + this.getDescription().getVersion() + " enabled");
     }
 
     @Override
     public void onDisable() {
         log("Version " + this.getDescription().getVersion() + " disabled");
     }
 
     public static void log(String msg) {
         logger.log(Level.INFO, msg);
     }
 
     public static void error(String msg) {
         logger.log(Level.SEVERE, msg);
     }
 
     public static void error(String msg, Throwable t) {
         logger.log(Level.SEVERE, msg, t);
     }
 
     public static void debug(String msg) {
         if (debugMode) {
             log("[debug] " + msg);
         }
     }
 
     public void depricateChunk(Chunk chunk) {
         Block block;
         List<Block> oreList = new ArrayList();
         for (int x = 0; x < 16; x++) {
             for (int z = 0; z < 16; z++) {
                 for (int y = 0; y < chunk.getWorld().getMaxHeight(); y++) {
                     block = chunk.getBlock(x, y, z);
                     if (block.getTypeId() == 14 || block.getTypeId() == 15 || block.getTypeId() == 16 || block.getTypeId() == 21 || block.getTypeId() == 56 || block.getTypeId() == 73 || block.getTypeId() == 140 || block.getTypeId() == 247 || block.getTypeId() == 248 || block.getTypeId() == 249) {
                         oreList.add(block);
                         if (this.debugMode) {
                             Bukkit.getLogger().info("[ChunkLoader] Found Ore: " + block.getTypeId());
                         }
                     }
                 }
             }
         }
         int b = 0;
         for (int a = 0; a < Math.round(oreList.size() * .6); a++) {
             int Min = 0;
             int Max = oreList.size();
             Block blockChange = null;
             try {
                 blockChange = oreList.get(Min + (int) (Math.random() * ((Max - Min) + 1)));
             } catch (Exception ex) {
                 continue;
             }
             if (this.debugMode) {
                 Bukkit.getLogger().info("[ChunkLoader] Replaced Ore: " + blockChange.getTypeId());
             }
             blockChange.setType(blockChange.getWorld().getBlockAt(blockChange.getX() - 5, blockChange.getY(), blockChange.getZ() + 5).getType());
             b++;
         }
     }
 
     public static void drawProgressBar(int numerator, int denominator) {
         int percent = (int) (((double) numerator / (double) denominator) * 100);
 
         String bar = "[";
         int lines = round((PROGRESSBAR_LENGTH * numerator) / denominator);
         int blanks = PROGRESSBAR_LENGTH - lines;
 
         for (int i = 0; i < lines; i++) {
             bar += "|";
         }
 
         for (int i = 0; i < blanks; i++) {
             bar += " ";
         }
 
         bar += "] " + percent + "%";
 
         System.out.print(bar + "\r");
     }
 
     private static int round(double dbl) {
         int noDecimal = (int) dbl;
         double decimal = dbl - noDecimal;
 
         if (decimal >= 0.5) {
             return noDecimal + 1;
         } else {
             return noDecimal;
         }
     }
 
     public void loadAllChunks() {
         Chunk chunk;
         World world = Bukkit.getWorlds().get(0);
         int a = 0;
         if (runLoader) {
             //250 = 4000 x 4000 block map.
            for (int x = 0; x < 250; x++) {
                for (int z = 0; z < 250; z++) {
                     chunk = world.getChunkAt(x, z);
                     chunk.load(true);
                     chunk.unload(true, true);
                     if (this.debugMode) {
                         if (world.isChunkLoaded(x, z)) {
                             Bukkit.getLogger().info(x + " " + z + " is saved");
                         }
                     }
                     depricateChunk(chunk);
                     drawProgressBar(a,250*250);
 //                    System.out.println("[ChunkLoader] " + a + " chunks saved\r");
                     a++;
                 }
             }
             Bukkit.getLogger().info("[ChunkLoader] " + a + " chunks saved!");
             Bukkit.getLogger().info("[ChunkLoader] All chunks saved!");
             this.configuration.set("runLoader", false);
             this.saveConfig();
         }
     }
 }
