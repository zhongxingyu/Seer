 package deathrat.mods.btbees;
 
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import powercrystals.core.updater.IUpdateableMod;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 import deathrat.mods.btbees.blocks.BlockRicePlant;
 import deathrat.mods.btbees.blocks.TileEntityRicePlant;
 import deathrat.mods.btbees.gui.BTBGuiHandler;
 import deathrat.mods.btbees.gui.BTBTab;
 import deathrat.mods.btbees.items.ItemRiceFood;
 import deathrat.mods.btbees.items.ItemRiceFoodBowl;
 import deathrat.mods.btbees.items.ItemRiceSeeds;
 import deathrat.mods.btbees.network.BTBConnectionHandler;
 import deathrat.mods.btbees.network.ServerPacketHandler;
 import deathrat.mods.btbees.proxies.CommonProxy;
 import deathrat.mods.btbees.render.RiceBaseRender;
 import deathrat.mods.btbees.updater.UpdateManager;
 
@Mod(modid = "btbees", name = BetterThanBees.modName, version = BetterThanBees.version, dependencies = "required-after:PowerCrystalsCore")
@NetworkMod(serverSideRequired=true, clientSideRequired=true, channels={"btbees"}, packetHandler=ServerPacketHandler.class, connectionHandler=BTBConnectionHandler.class)
 public class BetterThanBees implements IUpdateableMod
 {
 	@Instance("BetterThanBees")
 	public static BetterThanBees instance = new BetterThanBees();
 	@SidedProxy(clientSide = "deathrat.mods.btbees.client.ClientProxy", serverSide = "deathrat.mods.btbees.common.CommonProxy")
 	public static CommonProxy proxy;
 
 	public final static String modId = "BetterThanBees";
 	public final static String version = "1.4.7R0.2.0";
 	public final static String modName = "Better Than Bees";
 
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event)
 	{
 		proxy.preInit();
 
 		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
 		config.load();
 
 		initializeItemConfig(config);
 		initializeBlockConfig(config);
 
 		config.save();
 	}
 
 	private void initializeBlockConfig(Configuration config)
 	{
 		ricePlantID = config.getBlock("ricePlant", 3876).getInt();
 	}
 
 	private void initializeItemConfig(Configuration config)
 	{
 		uncookedRiceID = config.getItem("uncookedRice", 8023).getInt();
 		cookedRiceBallID = config.getItem("riceBall", 8024).getInt();
 		cookedRiceBowlID = config.getItem("riceBowl", 8025).getInt();
 		cookedRiceRollID = config.getItem("riceRoll", 8026).getInt();
 	}
 
 	@Init
 	public void init(FMLInitializationEvent event)
 	{
 		initalizeItems();
 		initializeBlocks();
 		initializeLanguageSetup();
 		initializeRecipes();
 		initializeCustomCreative();
 
 		TickRegistry.registerScheduledTickHandler(new UpdateManager(this), Side.CLIENT);
 
 		proxy.init();
 	}
 
 	private void initializeCustomCreative()
 	{
 		customTab = new BTBTab(CreativeTabs.getNextID(), "btbTab");
 		uncookedRice.setCreativeTab(customTab);
 		cookedRiceBall.setCreativeTab(customTab);
 		cookedRiceBowl.setCreativeTab(customTab);
 		cookedRiceRoll.setCreativeTab(customTab);
 	}
 
 	private void initializeRecipes()
 	{
 		GameRegistry.addSmelting(uncookedRiceID, new ItemStack(cookedRiceBall), 0.0F);
 	}
 
 	private void initializeLanguageSetup()
 	{
 		LanguageRegistry.addName(uncookedRice, "Uncooked Rice");
 		LanguageRegistry.addName(cookedRiceBall, "Rice Ball");
 		LanguageRegistry.addName(cookedRiceBowl, "Bowl of Rice");
 		LanguageRegistry.addName(cookedRiceRoll, "California Roll");
 	}
 
 	private void initializeBlocks()
 	{
 		ricePlant = new BlockRicePlant(ricePlantID, 0, TileEntityRicePlant.class);
 		GameRegistry.registerBlock(ricePlant, "ricePlant");
 		GameRegistry.registerTileEntity(TileEntityRicePlant.class, "RicePlant");
 	}
 
 	private void initalizeItems()
 	{
 		cookedRiceBowl = new ItemRiceFoodBowl(cookedRiceBowlID, 4, 5, false).setIconIndex(0).setItemName("riceBowl");
 		cookedRiceBall = new ItemRiceFood(cookedRiceBallID, 4, 3, false).setIconIndex(1).setItemName("riceBall");
 		cookedRiceRoll = new ItemRiceFood(cookedRiceRollID, 4, 7, false).setIconIndex(2).setItemName("riceRoll");
 		uncookedRice = new ItemRiceSeeds(uncookedRiceID, ricePlantID).setIconIndex(3).setItemName("uncookedRice");
 		MinecraftForge.addGrassSeed(new ItemStack(uncookedRice),  8);
 	}
 
 	private void initializeGui()
 	{
 		NetworkRegistry.instance().registerGuiHandler(this, new BTBGuiHandler());
 	}
 
 	@PostInit
 	public void postInit(FMLPostInitializationEvent event)
 	{
 		proxy.postInit();
 	}
 
 	public static String getTerrainTextures()
 	{
 		return btbTerrainTextures;
 	}
 
 	public static String getItemTextures()
 	{
 		return btbItemsTextures;
 	}
 
 	//Texture files
 	public static String btbTerrainTextures = "/deathrat/mods/btbees/btb_terrain2.png";
 	public static String btbItemsTextures = "/deathrat/mods/btbees/btb_items.png";
 
 	//Item IDs
 	public static int uncookedRiceID;
 	public static int cookedRiceBallID;
 	public static int cookedRiceBowlID;
 	public static int cookedRiceRollID;
 
 	//Block IDs
 	public static int ricePlantID;
 
 	//Items
 	public static Item uncookedRice;
 	public static Item cookedRiceBall;
 	public static Item cookedRiceBowl;
 	public static Item cookedRiceRoll;
 
 	//Blocks
 	public static Block ricePlant;
 
 	//Creative Tab
 	public static CreativeTabs customTab;
 
 	public static RiceBaseRender riceRender = new RiceBaseRender();
 
 	@Override
     public String getModId()
     {
 	    return modId;
     }
 
 	@Override
     public String getModName()
     {
 	    return modName;
     }
 
 	@Override
     public String getModFolder()
     {
 	    return "Better-Than-Bees";
     }
 
 	@Override
     public String getModVersion()
     {
 	    return version;
     }
 }
