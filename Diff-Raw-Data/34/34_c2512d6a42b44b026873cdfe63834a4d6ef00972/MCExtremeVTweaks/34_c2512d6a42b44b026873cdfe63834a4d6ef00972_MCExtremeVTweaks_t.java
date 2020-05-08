 package mcextreme.vtweaks;
 
 import java.io.File;
 
 import mcextreme.core.config.MCExtremeConfig;
 import mcextreme.core.MCExtremeCore;
 import mcextreme.core.utils.CreativeTab;
 import mcextreme.vtweaks.blocks.BlockNonSolidLeaves;
 import mcextreme.vtweaks.item.ItemsVTweaks;
 import net.minecraft.block.Block;
 import net.minecraft.client.Minecraft;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.ModMetadata;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 
@Mod(modid="MCExtremeVTweaks", name="MC-X Vanilla Tweaks", version=MCExtremeCore.version, dependencies = "required-after:MCExtremeCore")
 @NetworkMod(clientSideRequired=true, serverSideRequired=false)
 public class MCExtremeVTweaks
 {	
     @Instance("MCExtremeVTweaks")
     public static MCExtremeVTweaks instance;
     
     @EventHandler
     public void preInit(FMLPreInitializationEvent event)
     {
         MCExtremeCore.tabVanillaTweaks = new CreativeTab("mcx.vtweaks");
         
     	ModMetadata modMeta = event.getModMetadata();
  		modMeta.parent = "MCExtremeCore";
  		
  		MCExtremeConfig.loadConfig(new Configuration(new File(event.getModConfigurationDirectory() + "/mcextreme", "mcx-vanillatweaks.cfg")), "VTWEAKS");
     }
     
     @EventHandler
     public void load(FMLInitializationEvent event)
     {
     	Block.blocksList[18] = null;
     	ItemsVTweaks.initItems();
         CraftingVTweaks.addRecipes();
         Block blockLeaves = new BlockNonSolidLeaves(18).setHardness(0.2F).setLightOpacity(1).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("leaves").setTextureName("leaves");
         
         ((CreativeTab)MCExtremeCore.tabVanillaTweaks).setIconItemStack(new ItemStack(ItemsVTweaks.itemLink));
     }
 }
