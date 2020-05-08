 package me.f1337.lcfarming;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.Server;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.EventExecutor;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 public class LCFarming extends JavaPlugin
 {
   public final Logger logger = Logger.getLogger("Minecraft");
   private final LCBlockListener blockListener = new LCBlockListener(this);
   private final LCPlayerListener playerListener = new LCPlayerListener(this);
   final LCConfiguration LCConfiguration = new LCConfiguration(this);
   public String ConfigurationFileString = "plugins/LevelCraftCore/Configs/Farming.cfg";
   public File ConfigurationFile = new File("plugins/LevelCraftCore/Configs/Farming.cfg");
   public Plugin thisPlug;
 
   public void onDisable()
   {
   }
 
   public void onEnable()
   {
     new File("plugins/LevelCraftCore/Configs/").mkdirs();
     try {
       this.ConfigurationFile.createNewFile();
     } catch (IOException e) {
       this.logger.log(Level.SEVERE, "[LC] " + e);
     }
     this.LCConfiguration.loadConfig();
 
     String[] Args = { "Fm", "Seeds", "farm", "farming" };
     getConfiguration().setProperty("ReferenceKeys", Args);
 
     String[] Unlocks = { "Wooden Hoe = " + this.LCConfiguration.WoodHoe, "Stone Hone = " + this.LCConfiguration.StoneHoe, 
       "Iron Hoe = " + this.LCConfiguration.IronHoe, "Gold Hoe = " + this.LCConfiguration.GoldHoe, "Diamond Hoe = " + this.LCConfiguration.DiamondHoe };
     getConfiguration().setProperty("LevelUnlocks", Unlocks);
 
     getConfiguration().setProperty("LevelName", "Farming");
 
     getConfiguration().setProperty("ReferenceIndex", "Fm");
 
    getConfiguration().setProperty("Author", "Torrent Now f1337_m4573r");
 
     int[] UnlocksLevel = { this.LCConfiguration.WoodHoe, this.LCConfiguration.StoneHoe, 
       this.LCConfiguration.IronHoe, this.LCConfiguration.GoldHoe, this.LCConfiguration.DiamondHoe };
 
     String[] Exp = { 
       "Exp Per Till " + this.LCConfiguration.ExpPerTill, 
       "Exp Per Harvest " + this.LCConfiguration.ExpPerHarvest, 
       "Exp Per SugarCane " + this.LCConfiguration.ExpPerSugarCane, 
       "Exp Per Apple " + this.LCConfiguration.ExpPerApple, 
       "Exp Per GoldenApple " + this.LCConfiguration.ExpPerGoldenApple,
       "Exp Per Cactus" + this.LCConfiguration.ExpPerCactus, 
       "Exp Per Sapling" + this.LCConfiguration.ExpPerSapling, 
       "Exp Per Yellow Flower" + this.LCConfiguration.ExpPerYellowFlower, 
       "Exp Per Red Flower" + this.LCConfiguration.ExpPerRedRose, 
       "Exp Per Mushroom" + this.LCConfiguration.ExpPerMushroom, 
       "Exp Per Nether Wart" + this.LCConfiguration.ExpPerNetherWart, };
 
     getConfiguration().setProperty("LevelExpPer", Exp);
     getConfiguration().setProperty("LevelUnlocksLevel", UnlocksLevel);
     this.thisPlug = getServer().getPluginManager().getPlugin("LCFarming");
     Plugin LevelCraftCore = getServer().getPluginManager().getPlugin(
       "LevelCraftCore");
     if (LevelCraftCore == null) {
       this.logger.log(Level.SEVERE, 
         "[LC] Could not fine LevelCraftCore. Disabling.");
       getServer().getPluginManager().disablePlugin(this);
     } else {
       registerEvents();
       this.logger.log(Level.INFO, "[LC] Level Farming Loaded");
     }
   }
 
   private void registerEvents()
   {
     PluginManager pm = getServer().getPluginManager();
     pm.registerEvent(Event.Type.BLOCK_BREAK, this.blockListener, 
       Event.Priority.Highest, this);
     pm.registerEvent(Event.Type.PLAYER_INTERACT, this.playerListener, 
       Event.Priority.Highest, this);
     pm.registerEvent(Event.Type.BLOCK_PLACE, this.blockListener,
     		Event.Priority.Highest, this);
     pm.registerEvent(Event.Type.PLAYER_EGG_THROW, this.playerListener,
     		Event.Priority.Normal, this);
   }
 }
