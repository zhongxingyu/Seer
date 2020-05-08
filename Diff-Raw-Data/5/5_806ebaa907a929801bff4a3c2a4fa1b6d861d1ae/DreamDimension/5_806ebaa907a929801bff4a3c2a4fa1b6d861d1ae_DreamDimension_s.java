 package com.github.ubiquitousspice.dreamdimension;
 
 import com.github.ubiquitousspice.dreamdimension.blocks.BlockBooster;
 import com.github.ubiquitousspice.dreamdimension.blocks.BlockCheatyPortal;
 import com.github.ubiquitousspice.dreamdimension.client.CreativeTabDream;
 import com.github.ubiquitousspice.dreamdimension.dimension.WorldProviderMod;
 import com.github.ubiquitousspice.dreamdimension.entities.EntityLargeSheep;
 import com.github.ubiquitousspice.dreamdimension.world.BiomeGenDream;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.registry.EntityRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityEggInfo;
 import net.minecraft.entity.EntityList;
 import net.minecraft.item.ItemBlockWithMetadata;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.DimensionManager;
 
 import java.util.logging.Logger;
 
 //import com.github.ubiquitousspice.dreamdimension.blocks.BlockDreamDirt;
 
 @Mod(modid = DreamDimension.MODID, version = DreamDimension.VERSION, name = "The Dream Dimension")
 public class DreamDimension
 {
     public static final String MODID = "dreamdimension";
     public static final String VERSION = "0.1";
 
     @Mod.Instance
     public static DreamDimension instance;
 
     @SidedProxy(modId = MODID, clientSide = "com.github.ubiquitousspice.dreamdimension.client.ProxyClient",
             serverSide = "com.github.ubiquitousspice.dreamdimension.ProxyCommon")
     public static ProxyCommon proxy;
 
     // Random Stuff
     public static Logger logger;
     public static Material material;
     public static boolean dreamMaterialBreakable = false;
     public static BiomeGenBase dreamy;
     public static CreativeTabDream tabDream;
 
     // IDS
     public static int dimensionID;
     static int startEntityId = 300;
     private int idDreamDirt;
     private int idDreamBooster;
     private int idPortalBlock;
 
     // blocks
     public static Block dreamDirt;
     public static Block boosterBlock;
     public static Block portalBlock;
 
     @EventHandler
     public void preInit(FMLPreInitializationEvent event)
     {
         // get logger
         logger = event.getModLog();
 
         // mess with material
         material = (new MaterialDream());
 
         // CONFIGURATION STUFF
         {
             Configuration config = new Configuration(event.getSuggestedConfigurationFile());
 
             // config blockIDs
             int genId = 200;
             int baseId = 300;
             idDreamDirt = config.getTerrainBlock(Configuration.CATEGORY_BLOCK, "DreamDirt", genId++, "Base dirt for Dream Dimension").getInt();
             idDreamBooster = config.getTerrainBlock(Configuration.CATEGORY_BLOCK, "DreamLauncher", genId++, "Base dirt for Dream Dimension").getInt();
             idPortalBlock = config.getBlock(Configuration.CATEGORY_BLOCK, "PortalBlock", baseId++).getInt();
 
             // config itemIDs
 
             // config dimension
             dimensionID = config.get(Configuration.CATEGORY_GENERAL, "Dream Dimension Idea", 2).getInt();
 
             // config other
             dreamMaterialBreakable = config.get("Adventure", "dreamMaterialBreakable", false).getBoolean(false);
 
             // save it.
             if (config.hasChanged())
             {
                 config.save();
             }
         }
     }
 
     @EventHandler
     public void init(FMLInitializationEvent event)
     {
         // creative tab
         tabDream = new CreativeTabDream();
 
         // do blocks and stuff here.
        dreamDirt = new Block(idDreamDirt, material).setUnlocalizedName(MODID + ":dreamDirt").setCreativeTab(tabDream);
         boosterBlock = new BlockBooster(idDreamBooster).setCreativeTab(tabDream);
        portalBlock = new BlockCheatyPortal(idPortalBlock).setUnlocalizedName(MODID + ".portalBlock");
 
         // registrations
         GameRegistry.registerBlock(dreamDirt, "dreamDirt");
         GameRegistry.registerBlock(boosterBlock, ItemBlockWithMetadata.class, "dreamBooster");
         GameRegistry.registerBlock(portalBlock, "portalBlock");
 
         // dimension stuff
         dreamy = new BiomeGenDream(25);
         DimensionManager.registerProviderType(dimensionID, WorldProviderMod.class, true);
         DimensionManager.registerDimension(dimensionID, dimensionID);
 
         // Entity stuff
         registerEntity(EntityLargeSheep.class, "LargeSheep", 15198183, 16758197);
         LanguageRegistry.instance().addStringLocalization("entity.dreamdimension.LargeSheep.name", "King Lambchop");
 
         //registerEntity(EntityGiantItem.class, "GiantItem");
 
         // entities
         proxy.registerRenderers();
     }
 
     /**
      * registers an entity
      *
      * @param entityClass Entity Class
      * @param entityName  Entity Name
      * @param fgColor     Primary foreground egg color
      * @param bgColor     Secondary background egg color
      */
     public void registerEntity(Class<? extends Entity> entityClass, String entityName, int fgColor, int bgColor)
     {
         int id = EntityRegistry.findGlobalUniqueEntityId();
 
         EntityRegistry.registerGlobalEntityID(entityClass, entityName, id);
 
         EntityList.entityEggs.put(Integer.valueOf(id), new EntityEggInfo(id, bgColor, fgColor));
     }
 
     public void registerEntity(Class<? extends Entity> entityClass, String entityName)
     {
         int id = EntityRegistry.findGlobalUniqueEntityId();
 
         EntityRegistry.registerGlobalEntityID(entityClass, entityName, id);
     }
 }
