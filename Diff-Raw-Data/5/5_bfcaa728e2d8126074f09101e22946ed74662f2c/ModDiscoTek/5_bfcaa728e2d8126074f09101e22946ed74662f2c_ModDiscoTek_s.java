 
 package net.specialattack.discotek;
 
 import java.io.File;
 
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
 
 @Mod(modid = Objects.MOD_ID, name = Objects.MOD_NAME, version = Objects.MOD_VERSION)
 @NetworkMod(clientSideRequired = true, serverSideRequired = false, channels = { Objects.MOD_CHANNEL }, packetHandler = PacketHandler.class)
 public class ModDiscoTek extends HeldCoreMod {
 
     @Instance(Objects.MOD_ID)
     public ModDiscoTek instance;
     @SidedProxy(serverSide = Objects.COMMON_PROXY, clientSide = Objects.CLIENT_PROXY)
     public static CommonProxy proxy;
 
     // HeldCore Objects
     public static ConfigValue<Integer> blockLightId;
     public static ConfigValue<Integer> blockTrussId;
     public static ConfigValue<Integer> blockDecorationId;
     public static ConfigValue<Integer> blockControllerId;
     public static ConfigValue<Integer> blockColoredLampOnId;
     public static ConfigValue<Integer> blockColoredLampOffId;
     public static ConfigValue<Integer> itemDebugId;
     public static ConfigValue<Integer> itemLensId;
     public static ConfigValue<Integer> itemWirelessLinkerId;
     public static ConfigValue<Integer> itemOrienterId;
     public static ConfigValue<Integer> itemCraftingId;
 
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
         blockLightId = new ConfigValue<Integer>("blockLightId", Configuration.CATEGORY_BLOCK, null, 2080, "");
         blockTrussId = new ConfigValue<Integer>("blockTrussId", Configuration.CATEGORY_BLOCK, null, 2081, "");
         blockDecorationId = new ConfigValue<Integer>("blockDecorationId", Configuration.CATEGORY_BLOCK, null, 2082, "");
         blockControllerId = new ConfigValue<Integer>("blockControllerId", Configuration.CATEGORY_BLOCK, null, 2083, "");
         blockColoredLampOnId = new ConfigValue<Integer>("blockColoredLampOnId", Configuration.CATEGORY_BLOCK, null, 2084, "");
         blockColoredLampOffId = new ConfigValue<Integer>("blockColoredLampOffId", Configuration.CATEGORY_BLOCK, null, 2085, "");
         itemDebugId = new ConfigValue<Integer>("itemDebugId", Configuration.CATEGORY_ITEM, null, 5070, "");
         itemLensId = new ConfigValue<Integer>("itemLensId", Configuration.CATEGORY_ITEM, null, 5071, "");
         itemWirelessLinkerId = new ConfigValue<Integer>("itemWirelessLinkerId", Configuration.CATEGORY_ITEM, null, 5072, "");
         itemOrienterId = new ConfigValue<Integer>("itemOrienterId", Configuration.CATEGORY_ITEM, null, 5073, "");
         itemCraftingId = new ConfigValue<Integer>("itemCraftingId", Configuration.CATEGORY_ITEM, null, 5074, "");
 
         this.config = new Config(event.getSuggestedConfigurationFile());
 
         this.config.addConfigKey(blockLightId);
         this.config.addConfigKey(blockLightId);
         this.config.addConfigKey(blockLightId);
         this.config.addConfigKey(blockLightId);
         this.config.addConfigKey(blockLightId);
         this.config.addConfigKey(blockLightId);
         this.config.addConfigKey(blockLightId);
         this.config.addConfigKey(blockLightId);
         this.config.addConfigKey(blockLightId);
         this.config.addConfigKey(blockLightId);
         this.config.addConfigKey(blockLightId);
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
