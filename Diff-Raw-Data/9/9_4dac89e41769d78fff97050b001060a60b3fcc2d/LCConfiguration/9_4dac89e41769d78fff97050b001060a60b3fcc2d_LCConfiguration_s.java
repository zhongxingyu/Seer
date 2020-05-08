 package me.f1337.lcfarming;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import me.f1337.levelcraftcore.util.Properties;
 
 public class LCConfiguration
 {
   public LCFarming plugin;
   public int WoodHoe;
   public int StoneHoe;
   public int IronHoe;
   public int GoldHoe;
   public int DiamondHoe;
   public double ExpPerTill;
   public double ExpPerHarvest;
   public double ExpPerSugarCane;
   public double ExpPerApple;
   public double ExpPerGoldenApple;
   public double ExpPerCactus;
   public double ExpPerSapling;
   public double ExpPerRedRose;
   public double ExpPerYellowFlower;
   public double ExpPerMushroom;
   public double ExpPerWheat;
   public double ExpPerNetherWart;
   public int TillLevel;
   public int HarvestLevel;
   public int SugarCaneLevel;
   public int AppleLevel;
   public int GoldenAppleLevel;
   public int CactusLevel;
   public int SaplingLevel;
   public int RedRoseLevel;
   public int YellowFlowerLevel;
   public int MushroomLevel;
   public int AnimalFeedLevel;
   public int NetherWartLevel;
 
   public LCConfiguration(LCFarming instance)
   {
     this.plugin = instance;
   }
 
   public void loadConfig()
   {
     Properties properties = new Properties(this.plugin.ConfigurationFileString);
     try {
       properties.load();
     } catch (IOException e) {
       this.plugin.logger.log(Level.SEVERE, "[LC] " + e);
     }
 
     this.WoodHoe = properties.getInteger("WoodenHoeLevel", 0);
     this.StoneHoe = properties.getInteger("StoneHoeLevel", 5);
     this.IronHoe = properties.getInteger("IronHoeLevel", 10);
     this.GoldHoe = properties.getInteger("GoldHoeLevel", 20);
     this.DiamondHoe = properties.getInteger("DiamondHoeLevel", 30);
 
     this.ExpPerTill = properties.getDouble("ExpPerTill", 1.0D);
     this.ExpPerHarvest = properties.getDouble("ExpPerHarvest", 5.0D);
     this.ExpPerApple = properties.getDouble("ExpPerApples", 3.0D);
     this.ExpPerGoldenApple = properties.getDouble("ExpPerGoldenApple", 100.0D);
     this.ExpPerSugarCane = properties.getDouble("ExpPerSugarCane", 2.0D);
     this.ExpPerCactus = properties.getDouble ("ExpPerCactus", 3.0D);
     this.ExpPerSapling = properties.getDouble ("ExpPerSapling", 0.5D);
     this.ExpPerRedRose = properties.getDouble ("ExpPerRedRose", 0.1D);
     this.ExpPerYellowFlower = properties.getDouble ("ExpPerYellowFlower", 0.1D);
     this.ExpPerMushroom = properties.getDouble ("ExpPerMushroom", 0.1D);
     this.ExpPerWheat = properties.getDouble ("ExpPerWheat", 2.0D);
     this.ExpPerNetherWart = properties.getDouble ("ExpPerNetherWart", 5.00D);
 
     this.TillLevel = properties.getInteger("LevelForTill", 0);
     this.HarvestLevel = properties.getInteger("LevelForHarvest", 0);
     this.AppleLevel = properties.getInteger("LevelForApple", 0);
     this.SugarCaneLevel = properties.getInteger("LevelForSugarCane", 0);
     this.GoldenAppleLevel = properties.getInteger("LevelForGoldenApple", 0);
     this.CactusLevel = properties.getInteger("LevelForCacti", 0);
     this.SaplingLevel = properties.getInteger("LevelForSapling", 0);
     this.RedRoseLevel = properties.getInteger("LevelForRedRose", 0);
     this.YellowFlowerLevel = properties.getInteger("LevelForYellowFlower", 0);
     this.MushroomLevel = properties.getInteger("LevelForMushroom", 0);
     this.AnimalFeedLevel = properties.getInteger("LevelForWheat", 0);
    this.NetherWartLevel = properties.getInteger("LevelForNetherWart", 0);
   }
 }
