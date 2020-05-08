 
 package me.heldplayer.mods.Smartestone;
 
 import me.heldplayer.mods.Smartestone.packet.PacketHandler;
 import me.heldplayer.mods.Smartestone.util.Objects;
 import me.heldplayer.util.HeldCore.HeldCoreMod;
 import me.heldplayer.util.HeldCore.HeldCoreProxy;
 import me.heldplayer.util.HeldCore.ModInfo;
 import me.heldplayer.util.HeldCore.config.Config;
 import me.heldplayer.util.HeldCore.config.ConfigValue;
 import net.minecraftforge.common.Configuration;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.relauncher.Side;
 
 @Mod(name = Objects.MOD_NAME, modid = Objects.MOD_ID, version = Objects.MOD_VERSION)
 @NetworkMod(clientSideRequired = true, serverSideRequired = false, channels = { Objects.MOD_CHANNEL }, packetHandler = PacketHandler.class)
 public class ModSmartestone extends HeldCoreMod {
 
     @Instance(value = Objects.MOD_ID)
     public static ModSmartestone instance;
     @SidedProxy(clientSide = Objects.CLIENT_PROXY, serverSide = Objects.SERVER_PROXY)
     public static CommonProxy proxy;
 
     // HeldCore Objects
     public static ConfigValue<Integer> blockMulti1Id;
     public static ConfigValue<Integer> blockMulti2Id;
     public static ConfigValue<Integer> blockMicroId;
     public static ConfigValue<Integer> itemRotatorId;
     public static ConfigValue<Integer> itemMicroBlockId;
     public static ConfigValue<Integer> itemWaterCoreId;
     public static ConfigValue<Boolean> HDTextures;
 
     @Override
     @EventHandler
     public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

         Objects.log = event.getModLog();
 
         // Config
         blockMulti1Id = new ConfigValue<Integer>("MultiBlock1", Configuration.CATEGORY_BLOCK, null, 2100, "");
         blockMulti2Id = new ConfigValue<Integer>("MultiBlock2", Configuration.CATEGORY_BLOCK, null, 2101, "");
         blockMicroId = new ConfigValue<Integer>("MicroId", Configuration.CATEGORY_BLOCK, null, 2102, "");
         itemRotatorId = new ConfigValue<Integer>("Rotator", Configuration.CATEGORY_ITEM, null, 5240, "");
         itemMicroBlockId = new ConfigValue<Integer>("MicroBlockItem", Configuration.CATEGORY_ITEM, null, 5241, "");
         itemWaterCoreId = new ConfigValue<Integer>("WaterCore", Configuration.CATEGORY_ITEM, null, 5242, "");
         HDTextures = new ConfigValue<Boolean>("HD-Textures", Configuration.CATEGORY_GENERAL, Side.CLIENT, Boolean.FALSE, "");
 
         this.config = new Config(event.getSuggestedConfigurationFile());
         this.config.addConfigKey(blockMulti1Id);
         this.config.addConfigKey(blockMulti2Id);
         this.config.addConfigKey(blockMicroId);
         this.config.addConfigKey(itemRotatorId);
         this.config.addConfigKey(itemMicroBlockId);
         this.config.addConfigKey(itemWaterCoreId);
         this.config.addConfigKey(HDTextures);
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
