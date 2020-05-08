 package MysticWorld;
 
 import java.io.File;
 import java.lang.reflect.Array;
 import java.util.Random;
 import java.util.Set;
 
 import MysticWorld.Biome.BiomeHandler;
 import MysticWorld.Blocks.BlockHandler;
 import MysticWorld.Entity.EntityHandler;
 import MysticWorld.Items.ItemHandler$1;
 import MysticWorld.Lib.Strings;
 import MysticWorld.TileEntity.TileEntityHandler;
 import MysticWorld.WorldGen.*;
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.client.resources.ResourceLocation;
 import net.minecraft.world.WorldType;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraftforge.common.ChestGenHooks;
 import net.minecraftforge.common.DimensionManager;
 import net.minecraftforge.common.MinecraftForge;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.Mod.ServerStarted;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerStartedEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.common.registry.VillagerRegistry;
 import cpw.mods.fml.relauncher.Side;
 
 @Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION_NUMBER)
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 
 public class MysticWorld
 {
 	@Instance(Reference.MOD_ID)
     public static MysticWorld instance;
 	
 	@SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
     public static CommonProxy proxy;
 	
 	private TickHandler tickHandler;
 	
 	public static CreativeTabs MysticWorldTab = new TabMysticWorld(CreativeTabs.getNextID(), "MysticWorldTab");
 	
 	@EventHandler
 	public void preInit(FMLPreInitializationEvent event)
 	{	
 		Config.initialize(new File(event.getModConfigurationDirectory(), "Mystic Mods/Mystic World.cfg"));
         Config.save();
         BiomeHandler.init();
         instance = this;
     }
 	
 	@EventHandler
 	public void load(FMLInitializationEvent event)
 	{
 		proxy.registerRenders();
 		
 		this.tickHandler = new TickHandler();
 			
 	    TickRegistry.registerTickHandler(this.tickHandler, Side.SERVER);
 	    VillagerRegistry.instance().registerVillagerSkin(98943, new ResourceLocation("/mods/MysticTextures/textures/mob/MysticVillager.png"));
         VillagerRegistry.instance().registerVillageTradeHandler(98943, new VillageHutHandler());
         VillagerRegistry.instance().registerVillageCreationHandler(new VillageHutHandler());
 		BlockHandler.init();
 		ItemHandler$1.init();
 		TileEntityHandler.init();
 		WorldGenHandler$1.init();
 		EntityHandler.init();
 		RecipeHandler.init();
 		MysticHutUtil.addLoot();
 		Lang.init();
 	}
 	
 	@EventHandler
     public void postInit(FMLPostInitializationEvent event) 
 	{
     }
 }
