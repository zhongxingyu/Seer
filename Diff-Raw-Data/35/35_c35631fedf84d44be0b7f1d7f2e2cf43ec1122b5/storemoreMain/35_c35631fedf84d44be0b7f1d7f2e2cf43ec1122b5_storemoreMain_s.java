 package mods.storemore;
 
 import java.util.logging.Level;
 
 import mods.storemore.blocks.blockitems.packedblocksIIIItem;
 import mods.storemore.blocks.blockitems.packedblocksIIItem;
 import mods.storemore.blocks.blockitems.packedblocksIItem;
 import mods.storemore.blocks.blockitems.packedblocksIVItem;
 import mods.storemore.blocks.blockitems.packedblocksIXItem;
 import mods.storemore.blocks.blockitems.packedblocksVIIIItem;
 import mods.storemore.blocks.blockitems.packedblocksVIIItem;
 import mods.storemore.blocks.blockitems.packedblocksVIItem;
 import mods.storemore.blocks.blockitems.packedblocksVItem;
 import mods.storemore.blocks.blockitems.packedglassItem;
 import mods.storemore.client.CProxy;
 import mods.storemore.fuels.packedcharcoalIIIItem;
 import mods.storemore.fuels.packedcharcoalIIItem;
 import mods.storemore.fuels.packedcharcoalIItem;
 import mods.storemore.fuels.packedcharcoalIVItem;
 import mods.storemore.fuels.packedcoalIIIItem;
 import mods.storemore.fuels.packedcoalIIItem;
 import mods.storemore.fuels.packedcoalIItem;
 import mods.storemore.fuels.packedcoalIVItem;
 import mods.storemore.handlers.SProxy;
 import mods.storemore.handlers.StoreMoreTab;
 import mods.storemore.handlers.sm_config;
 import mods.storemore.handlers.sm_naming;
 import mods.storemore.handlers.sm_recipes;
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
@Mod(modid = storemoreMain.modId, name = storemoreMain.modName, version = storemoreMain.version)
 
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class storemoreMain
 
 
 {
 	
 	public static final String modId = "StoreMore";
	public static final String modName = "StoreMore";
 	public static final String version = "1.3.1";
 
 	@SidedProxy(clientSide = "mods.storemore.client.CProxy", serverSide = "mods.storemore.common.SProxy")
 		public static SProxy proxy;
 	
 	//Block Registering
 		public static Block packedblocksI;
 		public static Block packedblocksII;
 		public static Block packedblocksIII;
 		public static Block packedblocksIV;
 		public static Block packedblocksV;
 		public static Block packedglass;
 		public static Block packedcoalI;
 		public static Block packedcoalII;
 		public static Block packedcoalIII;
 		public static Block packedcoalIV;
 		public static Block packedcharcoalI;
 		public static Block packedcharcoalII;
 		public static Block packedcharcoalIII;
 		public static Block packedcharcoalIV;
 		public static Block packedblocksVI;
 		public static Block packedblocksVII;
 		public static Block packedblocksVIII;
 		public static Block packedblocksIX;
 		
 		
 		
 		public static Block ic2blocksI;
 		
 		
 	
 	
 	@PreInit()
     public void preInit(FMLPreInitializationEvent event) {
 
 	      sm_config.init();
           
 	        sm_config.initialize(event.getSuggestedConfigurationFile());
 	          
 	        sm_config.save(); 
 		
 	}
 	@Init
 	public void load(FMLInitializationEvent event){
 	proxy.registerRenderers();
     sm_naming.init();
     sm_recipes.initRecipes();
     sm_modhandler.initIC2Recipes();
     sm_ic2handler.initIC2();
     sm_tehandler.initTE();
    
 	}
 
 	public static CreativeTabs StoreMoreTab = new StoreMoreTab(CreativeTabs.getNextID(), "Store More");
 
 	{
 	    
 	    
 //Multiblocks	
 		
 		packedblocksI = new mods.storemore.blocks.packedblocksI(sm_config.packedblocksIID).setHardness(3.5f).setResistance(120.0F).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedblocksI");
 		packedblocksII = new mods.storemore.blocks.packedblocksII(sm_config.packedblocksIIID).setHardness(2.0f).setResistance(120.0F).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedblocksII");
 		packedblocksIII = new mods.storemore.blocks.packedblocksIII(sm_config.packedblocksIIIID).setHardness(2.0f).setResistance(120.0F).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedblocksIII");
 		packedblocksIV = new mods.storemore.blocks.packedblocksIV(sm_config.packedblocksIVID).setHardness(2.0f).setResistance(120.0F).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedblocksIV");
 		packedblocksV = new mods.storemore.blocks.packedblocksV(sm_config.packedblocksVID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedblocksV");
 		packedglass = new mods.storemore.blocks.packedglass(sm_config.packedglassID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedglass");
 		packedcoalI = new mods.storemore.fuels.packedcoalI(sm_config.packedcoalIID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedcoalI");
 		packedcoalII = new mods.storemore.fuels.packedcoalII(sm_config.packedcoalIIID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedcoalII");
 		packedcoalIII = new mods.storemore.fuels.packedcoalIII(sm_config.packedcoalIIIID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedcoalIII");
 		packedcoalIV = new mods.storemore.fuels.packedcoalIV(sm_config.packedcoalIVID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedcoalIV");
 		packedcharcoalI = new mods.storemore.fuels.packedcharcoalI(sm_config.packedcharcoalIID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedcharcoalI");
 		packedcharcoalII = new mods.storemore.fuels.packedcharcoalII(sm_config.packedcharcoalIIID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedcharcoalII");
 		packedcharcoalIII = new mods.storemore.fuels.packedcharcoalIII(sm_config.packedcharcoalIIIID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedcharcoalIII");
 		packedcharcoalIV = new mods.storemore.fuels.packedcharcoalIV(sm_config.packedcharcoalIVID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedcharcoalIV");
 		packedblocksVI = new mods.storemore.blocks.packedblocksVI(sm_config.packedblocksVIID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedblocksVI");
 		packedblocksVII = new mods.storemore.blocks.packedblocksVII(sm_config.packedblocksVIIID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedblocksVII");
 		packedblocksVIII = new mods.storemore.blocks.packedblocksVIII(sm_config.packedblocksVIIIID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedblocksVIII");
 		packedblocksIX = new mods.storemore.blocks.packedblocksIX(sm_config.packedblocksIXID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("packedblocksIX");
		//ic2blocksI = new mods.storemore.blocks.ic2blocksI(sm_config.ic2blocksIID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("ic2blocksI");
 		
 
 //Multiblock ItemBlocks	
 
 Item.itemsList[sm_config.packedblocksIID]	= new packedblocksIItem(sm_config.packedblocksIID-256).setUnlocalizedName("packedblocksI");
 Item.itemsList[sm_config.packedblocksIIID]	= new packedblocksIIItem(sm_config.packedblocksIIID-256).setUnlocalizedName("packedblocksII");
 Item.itemsList[sm_config.packedblocksIIIID] = new packedblocksIIIItem(sm_config.packedblocksIIIID-256).setUnlocalizedName("packedblocksIII");
 Item.itemsList[sm_config.packedblocksIVID] = new packedblocksIVItem(sm_config.packedblocksIVID-256).setUnlocalizedName("packedblocksIV");
 Item.itemsList[sm_config.packedblocksVID] = new packedblocksVItem(sm_config.packedblocksVID-256).setUnlocalizedName("packedblocksV");
 Item.itemsList[sm_config.packedblocksVIID] = new packedblocksVIItem(sm_config.packedblocksVIID-256).setUnlocalizedName("packedblocksVI");
 Item.itemsList[sm_config.packedblocksVIIID] = new packedblocksVIIItem(sm_config.packedblocksVIIID-256).setUnlocalizedName("packedblocksVII");
 Item.itemsList[sm_config.packedblocksVIIIID] = new packedblocksVIIIItem(sm_config.packedblocksVIIIID-256).setUnlocalizedName("packedblocksVIII");
 Item.itemsList[sm_config.packedblocksIXID] = new packedblocksIXItem(sm_config.packedblocksIXID-256).setUnlocalizedName("packedblocksIX");
 Item.itemsList[sm_config.packedglassID] = new packedglassItem(sm_config.packedglassID-256).setUnlocalizedName("packedglass");
 Item.itemsList[sm_config.packedcoalIID] = new packedcoalIItem(sm_config.packedcoalIID-256).setUnlocalizedName("packedcoalI");
 Item.itemsList[sm_config.packedcoalIIID] = new packedcoalIIItem(sm_config.packedcoalIIID-256).setUnlocalizedName("packedcoalII");
 Item.itemsList[sm_config.packedcoalIIIID] = new packedcoalIIIItem(sm_config.packedcoalIIIID-256).setUnlocalizedName("packedcoalIII");
 Item.itemsList[sm_config.packedcoalIVID] = new packedcoalIVItem(sm_config.packedcoalIVID-256).setUnlocalizedName("packedcoalIV");
 Item.itemsList[sm_config.packedcharcoalIID] = new packedcharcoalIItem(sm_config.packedcharcoalIID-256).setUnlocalizedName("packedcharcoalI");
 Item.itemsList[sm_config.packedcharcoalIIID] = new packedcharcoalIIItem(sm_config.packedcharcoalIIID-256).setUnlocalizedName("packedcharcoalII");
 Item.itemsList[sm_config.packedcharcoalIIIID] = new packedcharcoalIIIItem(sm_config.packedcharcoalIIIID-256).setUnlocalizedName("packedcharcoalIII");
 Item.itemsList[sm_config.packedcharcoalIVID] = new packedcharcoalIVItem(sm_config.packedcharcoalIVID-256).setUnlocalizedName("packedcharcoalIV");
//Item.itemsList[sm_config.ic2blocksIID] = new ic2blocksIItem(sm_config.ic2blocksIID-256).setUnlocalizedName("ic2blocksI");
 	}
 	
 {
 
 GameRegistry.registerFuelHandler(new storemoreFuels());
 	}
 @PostInit
 public void postInit(FMLPostInitializationEvent event) {
 	 
 	        sm_modhandler.init();
 	        
 }
 
 }
