 package grygrflzr.mods.glowstonewire;
 
 import net.minecraft.block.Block;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.common.config.Configuration;
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 @Mod(modid = "GrygrFlzr_GlowstoneWire", name = "Glowstone Wire", version = "build 111")
 public class GlowstoneWireMod {
     public static Block glowstoneWire;
     public static int gsWireColorR = 255;
     public static int gsWireColorG = 255;
     public static int gsWireColorB = 0;
 
     @SidedProxy(clientSide = "grygrflzr.mods.glowstonewire.ClientProxy", serverSide = "grygrflzr.mods.glowstonewire.CommonProxy")
     public static CommonProxy proxy;
     @Instance("GrygrFlzr_GlowstoneWire")
     public static GlowstoneWireMod instance;
     @EventHandler
     public void preload(FMLPreInitializationEvent event) {
         Configuration config = new Configuration(event.getSuggestedConfigurationFile());
         config.load();
         config.addCustomCategoryComment("color", "Color Range: 0-255, uses default values if out of range");
         gsWireColorR = config.get("color", "Wire Color Red", 255).getInt();
         gsWireColorG = config.get("color", "Wire Color Green", 255).getInt();
         gsWireColorB = config.get("color", "Wire Color Blue", 0).getInt();
         
         if(gsWireColorG > 255 || gsWireColorG < 0 ||
            gsWireColorR > 255 || gsWireColorR < 0 ||
            gsWireColorB > 255 || gsWireColorB < 0)
         { //use default if ANY value is out of range.
         	FMLLog.warning("Glowstone Wire: Invalid colors, resetting to default");
             config.removeCategory(config.getCategory("color"));
             config.addCustomCategoryComment("color", "Color Range: 0-255, uses default values if out of range");
             gsWireColorR = config.get("color", "Wire Color Red", 255).getInt();
             gsWireColorG = config.get("color", "Wire Color Green", 255).getInt();
             gsWireColorB = config.get("color", "Wire Color Blue", 0).getInt();
         }
         config.save();
         
        glowstoneWire = (BlockGlowstoneWire)(new BlockGlowstoneWire(gsWireColorR, gsWireColorG, gsWireColorB)).func_149711_c/*setHardness*/(0.0F).func_149711_c/*setLightValue*/(0.625F).func_149672_a/*setStepSound*/(Block.field_149769_e/*soundPowderFootstep*/).func_149663_c/*setUnlocalizedName*/("glowstoneDust");
     }
     @EventHandler
     public void load(FMLInitializationEvent event) {
        GameRegistry.registerBlock(glowstoneWire, "glowstoneDust");
         MinecraftForge.EVENT_BUS.register(new GlowstoneWireEventHook());
        proxy.registerRenderInformation();
     }
 }
