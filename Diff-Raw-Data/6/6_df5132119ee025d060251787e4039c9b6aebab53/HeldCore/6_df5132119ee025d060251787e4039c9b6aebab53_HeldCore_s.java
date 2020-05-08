 
 package me.heldplayer.util.HeldCore;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import me.heldplayer.util.HeldCore.config.Config;
 import me.heldplayer.util.HeldCore.config.ConfigValue;
 import net.minecraftforge.common.Configuration;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 
@Mod(modid = "HeldCore")
 public class HeldCore {
 
     public static Logger log;
     public static File configFolder;
     // Config
     private Config config;
     public static ConfigValue<String> modPack;
     public static ConfigValue<Boolean> optOut;
 
     @EventHandler
     public void preInit(FMLPreInitializationEvent event) {
         HeldCore.log = event.getModLog();
 
         configFolder = new File(event.getModConfigurationDirectory(), "HeldCore");
 
         if (!configFolder.exists()) {
             configFolder.mkdir();
         }
 
         // Config
         modPack = new ConfigValue<String>("modPack", Configuration.CATEGORY_GENERAL, null, "", "If this mod is running in a modpack, please set this config value to the name of the modpack");
         optOut = new ConfigValue<Boolean>("optOut", Configuration.CATEGORY_GENERAL, null, Boolean.FALSE, "Set this to true to opt-out from statistics gathering. If you are configuring this mod for a modpack, please leave it set to false");
         this.config = new Config(event.getSuggestedConfigurationFile());
         this.config.addConfigKey(modPack);
         this.config.addConfigKey(optOut);
         this.config.load();
         this.config.saveOnChange();
     }
 
     public void postInit(FMLPostInitializationEvent event) {}
 
     public static void initializeReporter(String modId, String modVersion) {
         try {
             File file = new File(configFolder, modId + ".version");
 
             if (!file.exists()) {
                 file.createNewFile();
             }
 
             UsageReporter reporter = new UsageReporter(modId, modVersion, modPack.getValue(), FMLCommonHandler.instance().getSide(), configFolder);
 
             Thread thread = new Thread(reporter, "Mod usage reporter for " + modId);
             thread.setDaemon(true);
             thread.start();
         }
         catch (IOException e) {
             e.printStackTrace();
         }
     }
 
 }
