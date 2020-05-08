 
 package me.heldplayer.plugins.nei.mystcraft;
 
 import java.io.File;
 
 import me.heldplayer.util.HeldCore.HeldCoreMod;
 import me.heldplayer.util.HeldCore.HeldCoreProxy;
 import me.heldplayer.util.HeldCore.ModInfo;
 import me.heldplayer.util.HeldCore.config.Config;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 
 /**
  * Main mod class
  * 
  * @author heldplayer
  * 
  */
 @Mod(modid = Objects.MOD_ID, name = Objects.MOD_NAME, version = Objects.MOD_VERSION, dependencies = Objects.MOD_DEPENCIES)
 public class PluginNEIMystcraft extends HeldCoreMod {
 
     @Instance(value = Objects.MOD_ID)
     public static PluginNEIMystcraft instance;
     @SidedProxy(clientSide = Objects.CLIENT_PROXY, serverSide = Objects.SERVER_PROXY)
     public static CommonProxy proxy;
 
     @Instance("Mystcraft")
     public static Object mystcraft;
 
     // HeldCore Objects
     //public static ConfigValue<Boolean> silentUpdates;
 
     @Override
     @EventHandler
     public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

         File file = new File(event.getModConfigurationDirectory(), "HeldCore");
 
         if (!file.exists()) {
             file.mkdirs();
         }
 
         Objects.log = event.getModLog();
 
         // Config
         //silentUpdates = new ConfigValue<Boolean>("silentUpdates", Configuration.CATEGORY_GENERAL, null, Boolean.TRUE, "Set this to true to hide update messages in the main menu");
         this.config = new Config(event.getSuggestedConfigurationFile());
         //this.config.addConfigKey(silentUpdates);
     }
 
     @Override
     @EventHandler
     public void init(FMLInitializationEvent event) {
         super.init(event);
     }
 
     @Override
     @EventHandler
     public void postInit(FMLPostInitializationEvent event) {
         super.postInit(event);
     }
 
     @Override
     public ModInfo getModInfo() {
         return Objects.MOD_INFO;
     }
 
     @Override
     public HeldCoreProxy getProxy() {
         return proxy;
     }
 
 }
