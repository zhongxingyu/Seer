 
 package me.heldplayer.mods.wecui;
 
 import me.heldplayer.mods.wecui.client.Color;
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
 import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
 import cpw.mods.fml.relauncher.Side;
 
 @Mod(modid = Objects.MOD_ID, name = Objects.MOD_NAME, version = Objects.MOD_VERSION)
 @NetworkMod(clientSideRequired = false, serverSideRequired = false, clientPacketHandlerSpec = @SidedPacketHandler(channels = { Objects.MOD_CHANNEL }, packetHandler = PacketHandler.class))
 public class ModWECUI extends HeldCoreMod {
 
     @Instance(value = Objects.MOD_ID)
     public static ModWECUI instance;
 
     @SidedProxy(clientSide = Objects.CLIENT_PROXY, serverSide = Objects.SERVER_PROXY)
     public static CommonProxy proxy;
 
     // HeldCore Objects
     public static ConfigValue<Color> colorCuboidPoint1;
     public static ConfigValue<Color> colorCuboidPoint2;
     public static ConfigValue<Color> colorCuboidOutline;
     public static ConfigValue<Color> colorCuboidGrid;
     public static ConfigValue<Color> colorPolygonPoint;
     public static ConfigValue<Color> colorPolygonOutline;
     public static ConfigValue<Color> colorPolygonGrid;
     public static ConfigValue<Color> colorEllipsoidCenter;
     public static ConfigValue<Color> colorEllipsoidGrid;
     public static ConfigValue<Color> colorCylinderCenter;
     public static ConfigValue<Color> colorCylinderOutline;
     public static ConfigValue<Color> colorCylinderGrid;
 
     @Override
     @EventHandler
     public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

         Objects.log = event.getModLog();
 
         // Config
         colorCuboidPoint1 = new ConfigValue<Color>("colorCuboidPoint1", Configuration.CATEGORY_GENERAL, Side.CLIENT, new Color(0.2F, 0.8F, 0.2F), "");
         colorCuboidPoint2 = new ConfigValue<Color>("colorCuboidPoint2", Configuration.CATEGORY_GENERAL, Side.CLIENT, new Color(0.2F, 0.2F, 0.8F), "");
         colorCuboidOutline = new ConfigValue<Color>("colorCuboidOutline", Configuration.CATEGORY_GENERAL, Side.CLIENT, new Color(0.8F, 0.3F, 0.3F), "");
         colorCuboidGrid = new ConfigValue<Color>("colorCuboidGrid", Configuration.CATEGORY_GENERAL, Side.CLIENT, new Color(0.8F, 0.2F, 0.2F), "");
         colorPolygonPoint = new ConfigValue<Color>("colorPolygonPoint", Configuration.CATEGORY_GENERAL, Side.CLIENT, new Color(0.2F, 0.8F, 0.8F), "");
         colorPolygonOutline = new ConfigValue<Color>("colorPolygonOutline", Configuration.CATEGORY_GENERAL, Side.CLIENT, new Color(0.8F, 0.3F, 0.3F), "");
         colorPolygonGrid = new ConfigValue<Color>("colorPolygonGrid", Configuration.CATEGORY_GENERAL, Side.CLIENT, new Color(0.8F, 0.2F, 0.2F), "");
         colorEllipsoidCenter = new ConfigValue<Color>("colorEllipsoidCenter", Configuration.CATEGORY_GENERAL, Side.CLIENT, new Color(0.8F, 0.8F, 0.2F), "");
         colorEllipsoidGrid = new ConfigValue<Color>("colorEllipsoidGrid", Configuration.CATEGORY_GENERAL, Side.CLIENT, new Color(0.8F, 0.3F, 0.3F), "");
         colorCylinderCenter = new ConfigValue<Color>("colorCylinderCenter", Configuration.CATEGORY_GENERAL, Side.CLIENT, new Color(0.8F, 0.2F, 0.8F), "");
         colorCylinderOutline = new ConfigValue<Color>("colorCylinderOutline", Configuration.CATEGORY_GENERAL, Side.CLIENT, new Color(0.8F, 0.3F, 0.3F), "");
         colorCylinderGrid = new ConfigValue<Color>("colorCylinderGrid", Configuration.CATEGORY_GENERAL, Side.CLIENT, new Color(0.8F, 0.2F, 0.2F), "");
         this.config = new Config(event.getSuggestedConfigurationFile());
         this.config.addConfigKey(colorCuboidPoint1);
         this.config.addConfigKey(colorCuboidPoint2);
         this.config.addConfigKey(colorCuboidOutline);
         this.config.addConfigKey(colorCuboidGrid);
         this.config.addConfigKey(colorPolygonPoint);
         this.config.addConfigKey(colorPolygonOutline);
         this.config.addConfigKey(colorPolygonGrid);
         this.config.addConfigKey(colorEllipsoidCenter);
         this.config.addConfigKey(colorEllipsoidGrid);
         this.config.addConfigKey(colorCylinderCenter);
         this.config.addConfigKey(colorCylinderOutline);
         this.config.addConfigKey(colorCylinderGrid);
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
