 package emasher.sockets;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraft.util.EnumChatFormatting;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.common.Property;
 import net.minecraftforge.oredict.OreDictionary;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 import net.minecraftforge.oredict.ShapelessOreRecipe;
 import net.minecraftforge.fluids.*;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import emasher.api.CentrifugeRecipeRegistry;
 import emasher.api.GrinderRecipeRegistry;
 import emasher.api.IModuleRegistrationManager;
 import emasher.api.MixerRecipeRegistry;
 import emasher.api.ModuleRegistry;
 import emasher.api.MultiSmelterRecipeRegistry;
 import emasher.api.Registry;
 import emasher.api.Util;
 import emasher.core.EmasherCore;
 import emasher.core.item.ItemEmasherGeneric;
 import emasher.core.item.ItemPondScum;
 import emasher.sockets.client.ClientPacketHandler;
 import emasher.sockets.items.*;
 import emasher.sockets.modules.*;
 import emasher.sockets.client.ClientProxy;
 
@Mod(modid="eng_toolbox", name="Engineer's Toolbox", version="1.1.4.3", dependencies = "required-after:emashercore")
 @NetworkMod(clientSideRequired=true, serverSideRequired=false,
 clientPacketHandlerSpec =
 @SidedPacketHandler(channels = {"Emasher_Sockets" }, packetHandler = ClientPacketHandler.class),
 serverPacketHandlerSpec =
 @SidedPacketHandler(channels = {"Emasher_Sockets" }, packetHandler = PacketHandler.class))
 public class SocketsMod
 {
 	@Instance("Sockets")
 	public static SocketsMod instance;
 	
 	@SidedProxy(clientSide="emasher.sockets.client.ClientProxy", serverSide="emasher.sockets.CommonProxy")
 	public static CommonProxy proxy;
 	
 	
 	
 	//Blocks
 	public static Block socket;
 	public static Block tempRS;
 	public static Block paintedPlanks;
 	public static Block groundLimestone;
 	public static Block blockSlickwater;
 	
 	//Fluids
 	
 	public static Fluid fluidSlickwater;
 	
 	//Items
 	
 	public static Item module;
 	public static Item remote;
 	public static Item blankSide;
 	public static Item engWrench;
 	public static Item rsWand;
 	public static Item rsShooter;
 	public static Item handPiston;
 	public static Item cattleProd;
 	public static Item handboiler;
 	public static ItemPaintCan[] paintCans = new ItemPaintCan[16];
 	public static Item dusts;
 	public static Item slickBucket;
 	
 	//Ids
 	
 	public int socketID;
 	public int tempRSID;
 	public int paintedPlankID;
 	public int groundLimestoneID;
 	public int slickwaterID;
 	public int moduleID;
 	public int remoteID;
 	public int blankID;
 	public int wrenchID;
 	public int rsWandID;
 	public int handPistonID;
 	public int rsShooterID;
 	public int cattleProdID;
 	public int handboilerID;
 	public int paintCanID;
 	public int dustsID;
 	public int slickBucketID;
 	
 	
 	public boolean vanillaCircuitRecipe;
 	public static boolean ic2First;
 	public boolean enableGrinder;
 	public boolean enableSolars;
 	public boolean enableWaterIntake;
 	public boolean enableHydro;
 	public boolean enablePiezo;
 	public boolean enableMultiSmelter;
 	public boolean enableKiln;
 	public boolean enableCentrifuge;
 	public static boolean cbTextures;
 	public static boolean smeltSand;
 	
 	public static double EUPerMJ;
 	
 	public static String[] colours = new String[]
 	{
 		"Black",
 		"Red",
 		"Green",
 		"Brown",
 		"Blue",
 		"Purple",
 		"Cyan",
 		"Light Gray",
 		"Gray",
 		"Pink",
 		"Lime",
 		"Yellow",
 		"Light Blue",
 		"Magenta",
 		"Orange",
 		"White"
 	};
 	
 	String[] dyes =
         {
             "dyeBlack",
             "dyeRed",
             "dyeGreen",
             "dyeBrown",
             "dyeBlue",
             "dyePurple",
             "dyeCyan",
             "dyeLightGray",
             "dyeGray",
             "dyePink",
             "dyeLime",
             "dyeYellow",
             "dyeLightBlue",
             "dyeMagenta",
             "dyeOrange",
             "dyeWhite"
         };
 	
 	public static Object PREF_BLUE = EnumChatFormatting.BLUE;
 	public static Object PREF_GREEN = EnumChatFormatting.GREEN;
 	public static Object PREF_RED = EnumChatFormatting.RED;
 	public static Object PREF_DARK_PURPLE = EnumChatFormatting.DARK_PURPLE;
 	public static Object PREF_YELLOW = EnumChatFormatting.YELLOW;
 	public static Object PREF_AQUA = EnumChatFormatting.AQUA;
 	public static Object PREF_WHITE = EnumChatFormatting.WHITE;
 	
 	public static CreativeTabs tabSockets = new CreativeTabs("tabSockets")
 	{
 		public ItemStack getIconItemStack()
 		{
 			return new ItemStack(rsWand);
 		}
 	};
 	
 	@EventHandler
 	public void preInit(FMLPreInitializationEvent event) 
 	{
 		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
 		Property temp;
 		
 		config.load();
 		
 		socketID = config.get(Configuration.CATEGORY_BLOCK, "Socket ID", 4070).getInt();
 		tempRSID = config.get(Configuration.CATEGORY_BLOCK, "tempRS ID", 4071).getInt();
 		paintedPlankID = config.get(Configuration.CATEGORY_BLOCK, "Painted Plank ID", 4072).getInt();
 		groundLimestoneID = config.get(Configuration.CATEGORY_BLOCK, "Ground Limestone ID", 4073).getInt();
 		slickwaterID = config.get(Configuration.CATEGORY_BLOCK, "Slickwater Block ID", 4074).getInt();
 		
 		moduleID = config.get(Configuration.CATEGORY_ITEM, "Module ID", 4170).getInt();
 		remoteID = config.get(Configuration.CATEGORY_ITEM, "Remote ID", 4172).getInt();
 		blankID = config.get(Configuration.CATEGORY_ITEM, "Blank Module ID", 4171).getInt();
 		wrenchID = config.get(Configuration.CATEGORY_ITEM, "Wrench ID", 4173).getInt();
 		rsWandID = config.get(Configuration.CATEGORY_ITEM, "Redstone Wand ID", 4174).getInt();
 		handboilerID = config.get(Configuration.CATEGORY_ITEM, "Hand Boiler ID", 4175).getInt();
 		temp = config.get(Configuration.CATEGORY_ITEM, "Paint Can ID", 4176);
 		temp.comment = "This ID and the 15 following it will be taken up";
 		paintCanID = temp.getInt();
 		dustsID = config.get(Configuration.CATEGORY_ITEM, "Dusts ID", 4192).getInt();
 		slickBucketID = config.get(Configuration.CATEGORY_ITEM, "Slickwater Bucket ID", 4193).getInt();
 		//rsShooterID = config.get(Configuration.CATEGORY_ITEM, "Redstone Shooter ID", 4175).getInt();
 		//handPistonID = config.get(Configuration.CATEGORY_ITEM, "Hand Piston ID", 4176).getInt();
 		//cattleProdID = config.get(Configuration.CATEGORY_ITEM, "Cattle Prod ID", 4177).getInt();
 		
 		//vanillaCircuitRecipe = config.get(Configuration.CATEGORY_GENERAL, "Enable Vanilla Circuit Recipe", true).getBoolean(true);
 		//ic2First = config.get(Configuration.CATEGORY_GENERAL, "Look for ic2 Macerator Recipes First", false).getBoolean(false);
 		enableGrinder = config.get(Configuration.CATEGORY_GENERAL, "Enable Grinder Module", true).getBoolean(true);
 		enableKiln = config.get(Configuration.CATEGORY_GENERAL, "Enable Kiln Module", true).getBoolean(true);
 		enableMultiSmelter = config.get(Configuration.CATEGORY_GENERAL, "Enable Multi Smelter Module", true).getBoolean(true);
 		enableCentrifuge = config.get(Configuration.CATEGORY_GENERAL, "Enable Centrifuge Module", true).getBoolean(true);
 		enableSolars = config.get(Configuration.CATEGORY_GENERAL, "Enable Solar Panel Modules", true).getBoolean(true);
 		enableHydro = config.get(Configuration.CATEGORY_GENERAL, "Enable Hydroelectric Turbines", true).getBoolean(true);
 		enablePiezo = config.get(Configuration.CATEGORY_GENERAL, "Enable Piezo Electric Tiles", true).getBoolean(true);
 		enableWaterIntake = config.get(Configuration.CATEGORY_GENERAL, "Enable Water Intake", true).getBoolean(true);
 		cbTextures = config.get(Configuration.CATEGORY_GENERAL, "Enable Colour Blind Mode", false).getBoolean(false);
 		EUPerMJ = config.get(Configuration.CATEGORY_GENERAL, "EU Per MJ", 2.44).getDouble(2.44);
 		smeltSand = config.get(Configuration.CATEGORY_GENERAL, "Hand boiler smelts sand", false).getBoolean(false);
 		
 		config.save();
 		
 		if(cbTextures)
 		{
 			PREF_BLUE = "Blue: ";
 			PREF_GREEN = "Green: ";
 			PREF_RED = "Red: ";
 			PREF_DARK_PURPLE = "Purple: ";
 			PREF_YELLOW = "MO: ";
 			PREF_AQUA = "EN: ";
 			PREF_WHITE = "GEN: ";
 		}
 		
 		proxy.registerRenderers();
 		
 	}
 	
 	@EventHandler
 	public void load(FMLInitializationEvent event) 
 	{
 		MinecraftForge.EVENT_BUS.register(new Util());
 		MinecraftForge.EVENT_BUS.register(new BucketEventHandler());
 		
 		LanguageRegistry.instance().addStringLocalization("itemGroup.tabSockets", "en_US", "Engineer's Toolbox");
 		
 		ModuleRegistry.registerModule(new ModBlank(0));
 		ModuleRegistry.registerModule(new ModItemInput(1));
 		ModuleRegistry.registerModule(new ModItemOutput(2));
 		ModuleRegistry.registerModule(new ModItemExtractor(3));
 		ModuleRegistry.registerModule(new ModFluidInput(4));
 		ModuleRegistry.registerModule(new ModFluidOutput(5));
 		ModuleRegistry.registerModule(new ModFluidExtractor(6));
 		ModuleRegistry.registerModule(new ModEnergyInput(7));
 		ModuleRegistry.registerModule(new ModEnergyOutput(8));
 		ModuleRegistry.registerModule(new ModMultiInput(9));
 		ModuleRegistry.registerModule(new ModMultiOutput(10));
 		ModuleRegistry.registerModule(new ModItemDetector(11));
 		ModuleRegistry.registerModule(new ModFluidDetector(12));
 		ModuleRegistry.registerModule(new ModItemDistributor(13));
 		ModuleRegistry.registerModule(new ModFluidDistributor(14));
 		ModuleRegistry.registerModule(new ModItemEjector(15));
 		ModuleRegistry.registerModule(new ModRSInput(16));
 		ModuleRegistry.registerModule(new ModRSOutput(17));
 		ModuleRegistry.registerModule(new ModRSAND(18));
 		ModuleRegistry.registerModule(new ModRSOR(19));
 		ModuleRegistry.registerModule(new ModRSNOT(20));
 		ModuleRegistry.registerModule(new ModRSNAND(21));
 		ModuleRegistry.registerModule(new ModRSNOR(22));
 		ModuleRegistry.registerModule(new ModRSXOR(23));
 		ModuleRegistry.registerModule(new ModRSXNOR(24));
 		ModuleRegistry.registerModule(new ModLatchToggle(32));
 		ModuleRegistry.registerModule(new ModLatchSet(33));
 		ModuleRegistry.registerModule(new ModLatchReset(34));
 		ModuleRegistry.registerModule(new ModTimer(35));
 		ModuleRegistry.registerModule(new ModDelayer(36));
 		ModuleRegistry.registerModule(new ModStateCell(37));
 		ModuleRegistry.registerModule(new ModPressurePlate(38));
 		ModuleRegistry.registerModule(new ModBurner(64));
 		ModuleRegistry.registerModule(new ModBreaker(65));
 		if(enableWaterIntake) ModuleRegistry.registerModule(new ModOsPump(66));
 		ModuleRegistry.registerModule(new ModSpring(67));
 		ModuleRegistry.registerModule(new ModDFBlade(68));
 		ModuleRegistry.registerModule(new ModVacuum(69));
 		ModuleRegistry.registerModule(new ModAdvancedBreaker(70));
 		ModuleRegistry.registerModule(new ModFurnace(71));
 		if(enableGrinder) ModuleRegistry.registerModule(new ModGrinder(72));
 		ModuleRegistry.registerModule(new ModEnergyIndicator(73));
 		ModuleRegistry.registerModule(new ModEnergyExpansion(74));
 		if(enableSolars) ModuleRegistry.registerModule(new ModSolar(75));
 		ModuleRegistry.registerModule(new ModButton(76));
 		ModuleRegistry.registerModule(new ModBUD(78));
 		ModuleRegistry.registerModule(new ModAdvancedEnergyExpansion(79));
 		ModuleRegistry.registerModule(new ModWaterCooler(80));
 		ModuleRegistry.registerModule(new ModFreezer(81));
 		ModuleRegistry.registerModule(new ModLavaIntake(82));
 		if(enablePiezo) ModuleRegistry.registerModule(new ModPiezo(83));
 		if(enableHydro) ModuleRegistry.registerModule(new ModWaterMill(84));
 		ModuleRegistry.registerModule(new ModSelfDestruct(85));
 		ModuleRegistry.registerModule(new ModItemDisplay(86));
 		ModuleRegistry.registerModule(new ModTankDisplay(87));
 		ModuleRegistry.registerModule(new ModCharger(88));
 		ModuleRegistry.registerModule(new ModBlockPlacer(89));
 		ModuleRegistry.registerModule(new ModMachineOutput(90));
 		if(enableKiln) ModuleRegistry.registerModule(new ModKiln(91));
 		if(enableMultiSmelter) ModuleRegistry.registerModule(new ModMultiSmelter(92));
 		if(enableCentrifuge) ModuleRegistry.registerModule(new ModCentrifuge(93));
 		ModuleRegistry.registerModule(new ModMixer(94));
 		
 		//Register 3rd party modules
 		for(IModuleRegistrationManager reg : ModuleRegistry.registers)
 		{
 			reg.registerModules();
 		}
 		
 		socket = new BlockSocket(socketID).setResistance(8.0F).setHardness(2.0F).setStepSound(Block.soundMetalFootstep).setUnlocalizedName("modular_socket");;
 		//GameRegistry.registerBlock(socket, "modular_socket");
 		LanguageRegistry.addName(socket, "Modular Socket");
 		GameRegistry.registerTileEntity(TileSocket.class, "modular_socket");
 		Item.itemsList[socket.blockID] = new ItemBlockSocket(socket.blockID - 256);
 		
 		tempRS = new BlockTempRS(tempRSID).setBlockUnbreakable();
 		GameRegistry.registerBlock(tempRS, "tempRS");
 		LanguageRegistry.addName(tempRS, "TempRS");
 		GameRegistry.registerTileEntity(TileTempRS.class, "TempRS");
 		
 		paintedPlanks = (new BlockPaintedWood(paintedPlankID, 0, Material.wood))
 				.setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep)
 				.setUnlocalizedName("paintedPlanks");
 		
 		Item.itemsList[paintedPlanks.blockID] = new ItemBlockPaintedWood(paintedPlanks.blockID - 256);
 		
 		registerPaintedPlankNames();
 		
 		for(int i = 0; i < 16; i++)
 		{
 			OreDictionary.registerOre("plankWood", new ItemStack(this.paintedPlanks, 1, i));
 		}
 		
 		groundLimestone = new BlockGroundLimestone(groundLimestoneID).setHardness(0.6F).setStepSound(Block.soundGravelFootstep).setUnlocalizedName("groundLimestone");
 		GameRegistry.registerBlock(groundLimestone, "groundLimestone");
 		LanguageRegistry.addName(groundLimestone, "Ground Limestone");
 		
 		handboiler = new ItemHandboiler(handboilerID, "", "");
 		LanguageRegistry.addName(handboiler, "Hand Boiler");
 		
 		for(int i = 0; i<16; i++)
 		{
 			paintCans[i] = new ItemPaintCan(paintCanID + i, i);
 			LanguageRegistry.instance().addStringLocalization("item.paintCan." + colours[i] + ".name", colours[i] + " Spray Paint");
 			
 		}
 		
 		remote = new ItemSocketRemote(remoteID);
 		LanguageRegistry.addName(remote, "Socket Remote");
 		
 		rsWand = new ItemRSWand(rsWandID);
 		LanguageRegistry.addName(rsWand, "Redstone Wand");
 		
 		fluidSlickwater = new FluidSlickwater();
 		FluidRegistry.registerFluid(fluidSlickwater);
 		
 		blockSlickwater = new BlockSlickwater(slickwaterID, fluidSlickwater);
 		GameRegistry.registerBlock(blockSlickwater, "slickwater");
 		slickBucket = new ItemSlickBucket(slickBucketID);
 		slickBucket.setMaxStackSize(1);
 		slickBucket.setCreativeTab(this.tabSockets);
 		
 		LanguageRegistry.addName(slickBucket, "Slickwater Bucket");
 		LanguageRegistry.addName(blockSlickwater, "Slickwater");
 		
 		FluidContainerRegistry.registerFluidContainer(new FluidStack(fluidSlickwater, FluidContainerRegistry.BUCKET_VOLUME), new ItemStack(slickBucket), new ItemStack(Item.bucketEmpty));
 		
 		//cattleProd = new ItemCattleProd(cattleProdID);
 		//LanguageRegistry.addName(cattleProd, "Cattle Prod");
 		
 		blankSide = new ItemEmasherGeneric(blankID, "sockets:blankmod", "blankSide");
 		LanguageRegistry.addName(blankSide, "Blank Module");
 		blankSide.setCreativeTab(tabSockets);
 		
 		module = new ItemModule(moduleID);
 		
 		for(int i = 0; i < ModuleRegistry.numModules; i++)
 		{
 			if(ModuleRegistry.getModule(i) != null)
 			{
 				LanguageRegistry.instance().addStringLocalization("item.socket_module." + i + ".name", ModuleRegistry.getModule(i).getLocalizedName());
 			}
 		}
 		
 		engWrench = new ItemEngWrench(wrenchID);
 		LanguageRegistry.addName(engWrench, "Engineer's Wrench");
 		
 		dusts = new ItemDusts(dustsID);
 		for(int i = 0; i < ItemDusts.NUM_ITEMS; i++)
 		{
 			LanguageRegistry.instance().addStringLocalization("item.e_dusts." + "e_" + ItemDusts.NAMES[i] + ".name", ItemDusts.NAMES_LOC[i]);
 			OreDictionary.registerOre(ItemDusts.ORE_NAMES[i], new ItemStack(dusts, 1, i));
 		}
 		
 		registerOreRecipes();
 		
 		GameRegistry.addRecipe(new ItemStack(socket), new Object[]
 				{
 					" b ", "pmc", " h ", Character.valueOf('m'), EmasherCore.machine, Character.valueOf('h'), Block.chest,
 					Character.valueOf('b'), Item.bucketEmpty, Character.valueOf('p'), EmasherCore.psu,
 					Character.valueOf('i'), Item.ingotIron, Character.valueOf('c'), EmasherCore.circuit
 				});
 		
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(blankSide, 12), "ggg", "reb", "sms", Character.valueOf('m'), EmasherCore.machine,
 				Character.valueOf('g'), Block.thinGlass, Character.valueOf('s'), Item.glowstone, Character.valueOf('r'), "dyeRed", Character.valueOf('e'), "dyeGreen", Character.valueOf('b'), "dyeBlue"));
 		
 		GameRegistry.addRecipe(new ItemStack(remote), new Object[]
 				{
 					"e", "c", "s", Character.valueOf('e'), Item.enderPearl, Character.valueOf('c'), EmasherCore.circuit,
 					Character.valueOf('s'), blankSide
 				});
 		
 		GameRegistry.addRecipe(new ItemStack(engWrench), new Object[]
 				{
 					" i ", "ii ", "  b", Character.valueOf('i'), Item.ingotIron, Character.valueOf('b'), Block.stoneButton
 				});
 		
 		GameRegistry.addRecipe(new ItemStack(rsWand), new Object[]
 				{
 					"rt ", "tc ", "  w", Character.valueOf('r'), Block.blockRedstone, Character.valueOf('t'), Block.torchRedstoneActive,
 					Character.valueOf('c'), EmasherCore.circuit, Character.valueOf('w'), engWrench
 				});
 		
 		if(! Loader.isModLoaded("gascraft"))for(int i = 0; i < 16; i++)
 		{
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(paintCans[i], 1),
 						"i", "p", "d", Character.valueOf('i'), Block.stoneButton, Character.valueOf('p'), Item.glassBottle, Character.valueOf('d'), dyes[i])
 					);
 		}
 		
 		GameRegistry.addRecipe(new ItemStack(handboiler, 1), new Object[]
 				{
 					"bbb", "ici", " n ", Character.valueOf('b'), Item.blazeRod, Character.valueOf('c'), Item.fireballCharge,
 					Character.valueOf('i'), Item.ingotIron, Character.valueOf('n'), EmasherCore.circuit
 				});
 		
 		for(int i = 0; i < ModuleRegistry.numModules; i++)
 		{
 			if(ModuleRegistry.getModule(i) != null)
 			{
 				ModuleRegistry.getModule(i).addRecipe();
 			}
 		}
 		
 		registerInRegistry();
 	}
 	
 	private void registerPaintedPlankNames()
 	{
 		
 		LanguageRegistry.instance().addStringLocalization("tile.paintedPlanks.blackPlanks.name", "Black Planks");
 		LanguageRegistry.instance().addStringLocalization("tile.paintedPlanks.redPlanks.name", "Red Planks");
 		LanguageRegistry.instance().addStringLocalization("tile.paintedPlanks.greenPlanks.name", "Green Planks");
 		LanguageRegistry.instance().addStringLocalization("tile.paintedPlanks.brownPlanks.name", "Brown Planks");
 		LanguageRegistry.instance().addStringLocalization("tile.paintedPlanks.bluePlanks.name", "Blue Planks");
 		LanguageRegistry.instance().addStringLocalization("tile.paintedPlanks.purplePlanks.name", "Purple Planks");
 		LanguageRegistry.instance().addStringLocalization("tile.paintedPlanks.cyanPlanks.name", "Cyan Planks");
 		LanguageRegistry.instance().addStringLocalization("tile.paintedPlanks.lightGrayPlanks.name", "Light Gray Planks");
 		LanguageRegistry.instance().addStringLocalization("tile.paintedPlanks.grayPlanks.name", "Gray Planks");
 		LanguageRegistry.instance().addStringLocalization("tile.paintedPlanks.pinkPlanks.name", "Pink Planks");
 		LanguageRegistry.instance().addStringLocalization("tile.paintedPlanks.limePlanks.name", "Lime Planks");
 		LanguageRegistry.instance().addStringLocalization("tile.paintedPlanks.yellowPlanks.name", "Yellow Planks");
 		LanguageRegistry.instance().addStringLocalization("tile.paintedPlanks.lightBluePlanks.name", "Light Blue Planks");
 		LanguageRegistry.instance().addStringLocalization("tile.paintedPlanks.magentaPlanks.name", "Magenta Planks");
 		LanguageRegistry.instance().addStringLocalization("tile.paintedPlanks.orangePlanks.name", "Orange Planks");
 		LanguageRegistry.instance().addStringLocalization("tile.paintedPlanks.whitePlanks.name", "White Planks");
 	}
 	
 	private void registerOreRecipes()
 	{
 		//Grinder
 		
 		GrinderRecipeRegistry.registerRecipe(new ItemStack(EmasherCore.limestone), new ItemStack(groundLimestone));
 		
 		GrinderRecipeRegistry.registerRecipe("oreGold", new ItemStack(dusts, 1, ItemDusts.Const.groundGold.ordinal()));
 		GrinderRecipeRegistry.registerRecipe("oreIron", new ItemStack(dusts, 1, ItemDusts.Const.groundIron.ordinal()));
 		GrinderRecipeRegistry.registerRecipe("oreAluminum", new ItemStack(dusts, 1, ItemDusts.Const.groundBauxite.ordinal()));
 		GrinderRecipeRegistry.registerRecipe("oreTin", new ItemStack(dusts, 1, ItemDusts.Const.groundCassiterite.ordinal()));
 		GrinderRecipeRegistry.registerRecipe("oreCopper", new ItemStack(dusts, 1, ItemDusts.Const.groundNativeCopper.ordinal()));
 		GrinderRecipeRegistry.registerRecipe("oreNickel", new ItemStack(dusts, 1, ItemDusts.Const.groundPentlandite.ordinal()));
 		GrinderRecipeRegistry.registerRecipe("oreLead", new ItemStack(dusts, 1, ItemDusts.Const.groundGalena.ordinal()));
 		GrinderRecipeRegistry.registerRecipe("oreSilver", new ItemStack(dusts, 1, ItemDusts.Const.groundSilver.ordinal()));
 		
 		GrinderRecipeRegistry.registerRecipe("oreLapis", new ItemStack(Item.dyePowder, 16, 4));
 		GrinderRecipeRegistry.registerRecipe(new ItemStack(Block.oreCoal), new ItemStack(Item.coal, 2));
 		GrinderRecipeRegistry.registerRecipe("oreDiamond", new ItemStack(Item.diamond, 2));
 		GrinderRecipeRegistry.registerRecipe("oreEmerald", new ItemStack(Item.emerald, 2));
 		GrinderRecipeRegistry.registerRecipe("oreRedstone", new ItemStack(Item.redstone, 8));
 		GrinderRecipeRegistry.registerRecipe("oreQuartz", new ItemStack(Item.netherQuartz, 2));
 		GrinderRecipeRegistry.registerRecipe("oreEmery", new ItemStack(EmasherCore.gem, 4, 0));
 		GrinderRecipeRegistry.registerRecipe("oreRuby", new ItemStack(EmasherCore.gem, 2, 1));
 		GrinderRecipeRegistry.registerRecipe("oreSapphire", new ItemStack(EmasherCore.gem, 2, 2));
 		
 		//Multi Smelter
 		
 		MultiSmelterRecipeRegistry.registerRecipe("dustQuicklime", "groundGold", new ItemStack(dusts, 3, ItemDusts.Const.impureGoldDust.ordinal()));
 		MultiSmelterRecipeRegistry.registerRecipe("dustQuicklime", "groundIron", new ItemStack(dusts, 3, ItemDusts.Const.impureIronDust.ordinal()));
 		MultiSmelterRecipeRegistry.registerRecipe("dustQuicklime", "groundAluminum", new ItemStack(dusts, 3, ItemDusts.Const.impureAluminiumDust.ordinal()));
 		MultiSmelterRecipeRegistry.registerRecipe("dustQuicklime", "groundTin", new ItemStack(dusts, 3, ItemDusts.Const.impureTinDust.ordinal()));
 		MultiSmelterRecipeRegistry.registerRecipe("dustQuicklime", "groundCopper", new ItemStack(dusts, 3, ItemDusts.Const.impureCopperDust.ordinal()));
 		MultiSmelterRecipeRegistry.registerRecipe("dustQuicklime", "groundNickel", new ItemStack(dusts, 3, ItemDusts.Const.impureNickelDust.ordinal()));
 		MultiSmelterRecipeRegistry.registerRecipe("dustQuicklime", "groundLead", new ItemStack(dusts, 3, ItemDusts.Const.impureLeadDust.ordinal()));
 		MultiSmelterRecipeRegistry.registerRecipe("dustQuicklime", "groundSilver", new ItemStack(dusts, 3, ItemDusts.Const.impureSilverDust.ordinal()));
 		
 		MultiSmelterRecipeRegistry.registerRecipe("ingotCopper", "ingotTin", new ItemStack(EmasherCore.ingot, 1, 1));
 		
 		
 		//Centrifuge
 		
 		CentrifugeRecipeRegistry.registerRecipe("dustImpureGold", new ItemStack(dusts, 1, ItemDusts.Const.pureGoldDust.ordinal()), new ItemStack(dusts, 1, ItemDusts.Const.pureSilverDust.ordinal()), 5);
 		CentrifugeRecipeRegistry.registerRecipe("dustImpureIron", new ItemStack(dusts, 1, ItemDusts.Const.pureIronDust.ordinal()), new ItemStack(dusts, 1, ItemDusts.Const.pureIronDust.ordinal()), 33);
 		CentrifugeRecipeRegistry.registerRecipe("dustImpureAluminum", new ItemStack(dusts, 1, ItemDusts.Const.pureAluminiumDust.ordinal()), new ItemStack(dusts, 1, ItemDusts.Const.pureIronDust.ordinal()), 5);
 		CentrifugeRecipeRegistry.registerRecipe("dustImpureTin", new ItemStack(dusts, 1, ItemDusts.Const.pureTinDust.ordinal()), new ItemStack(dusts, 1, ItemDusts.Const.pureTinDust.ordinal()), 33);
 		CentrifugeRecipeRegistry.registerRecipe("dustImpureCopper", new ItemStack(dusts, 1, ItemDusts.Const.pureCopperDust.ordinal()), new ItemStack(dusts, 1, ItemDusts.Const.pureCopperDust.ordinal()), 33);
 		CentrifugeRecipeRegistry.registerRecipe("dustImpureNickel", new ItemStack(dusts, 1, ItemDusts.Const.pureNickelDust.ordinal()), new ItemStack(dusts, 1, ItemDusts.Const.purePlatinumDust.ordinal()), 5);
 		CentrifugeRecipeRegistry.registerRecipe("dustImpureLead", new ItemStack(dusts, 1, ItemDusts.Const.pureLeadDust.ordinal()), new ItemStack(dusts, 1, ItemDusts.Const.pureSilverDust.ordinal()), 33);
 		CentrifugeRecipeRegistry.registerRecipe("dustImpureSilver", new ItemStack(dusts, 1, ItemDusts.Const.pureSilverDust.ordinal()), new ItemStack(dusts, 1, ItemDusts.Const.pureLeadDust.ordinal()), 10);
 		
 		//Furnace
 		
 		FurnaceRecipes.smelting().addSmelting(dusts.itemID, ItemDusts.Const.groundGold.ordinal(), new ItemStack(Item.ingotGold, 2), 1);
 		FurnaceRecipes.smelting().addSmelting(dusts.itemID, ItemDusts.Const.groundIron.ordinal(), new ItemStack(Item.ingotIron, 2), 1);
 		FurnaceRecipes.smelting().addSmelting(dusts.itemID, ItemDusts.Const.groundBauxite.ordinal(), new ItemStack(EmasherCore.ingot, 2, 0), 1);
 		FurnaceRecipes.smelting().addSmelting(dusts.itemID, ItemDusts.Const.groundCassiterite.ordinal(), new ItemStack(EmasherCore.ingot, 2, 8), 1);
 		FurnaceRecipes.smelting().addSmelting(dusts.itemID, ItemDusts.Const.groundNativeCopper.ordinal(), new ItemStack(EmasherCore.ingot, 2, 2), 1);
 		FurnaceRecipes.smelting().addSmelting(dusts.itemID, ItemDusts.Const.groundPentlandite.ordinal(), new ItemStack(EmasherCore.ingot, 2, 4), 1);
 		FurnaceRecipes.smelting().addSmelting(dusts.itemID, ItemDusts.Const.groundGalena.ordinal(), new ItemStack(EmasherCore.ingot, 2, 3), 1);
 		FurnaceRecipes.smelting().addSmelting(dusts.itemID, ItemDusts.Const.groundSilver.ordinal(), new ItemStack(EmasherCore.ingot, 2, 6), 1);
 		
 		FurnaceRecipes.smelting().addSmelting(dusts.itemID, ItemDusts.Const.impureGoldDust.ordinal(), new ItemStack(Item.ingotGold), 1);
 		FurnaceRecipes.smelting().addSmelting(dusts.itemID, ItemDusts.Const.impureIronDust.ordinal(), new ItemStack(Item.ingotIron), 1);
 		FurnaceRecipes.smelting().addSmelting(dusts.itemID, ItemDusts.Const.impureAluminiumDust.ordinal(), new ItemStack(EmasherCore.ingot, 1, 0), 1);
 		FurnaceRecipes.smelting().addSmelting(dusts.itemID, ItemDusts.Const.impureTinDust.ordinal(), new ItemStack(EmasherCore.ingot, 1, 8), 1);
 		FurnaceRecipes.smelting().addSmelting(dusts.itemID, ItemDusts.Const.impureCopperDust.ordinal(), new ItemStack(EmasherCore.ingot, 1, 2), 1);
 		FurnaceRecipes.smelting().addSmelting(dusts.itemID, ItemDusts.Const.impureNickelDust.ordinal(), new ItemStack(EmasherCore.ingot, 1, 4), 1);
 		FurnaceRecipes.smelting().addSmelting(dusts.itemID, ItemDusts.Const.impureLeadDust.ordinal(), new ItemStack(EmasherCore.ingot, 1, 3), 1);
 		FurnaceRecipes.smelting().addSmelting(dusts.itemID, ItemDusts.Const.impureSilverDust.ordinal(), new ItemStack(EmasherCore.ingot, 1, 6), 1);
 		
 		FurnaceRecipes.smelting().addSmelting(dusts.itemID, ItemDusts.Const.pureGoldDust.ordinal(), new ItemStack(Item.ingotGold), 1);
 		FurnaceRecipes.smelting().addSmelting(dusts.itemID, ItemDusts.Const.pureIronDust.ordinal(), new ItemStack(Item.ingotIron), 1);
 		FurnaceRecipes.smelting().addSmelting(dusts.itemID, ItemDusts.Const.pureAluminiumDust.ordinal(), new ItemStack(EmasherCore.ingot, 1, 0), 1);
 		FurnaceRecipes.smelting().addSmelting(dusts.itemID, ItemDusts.Const.pureTinDust.ordinal(), new ItemStack(EmasherCore.ingot, 1, 8), 1);
 		FurnaceRecipes.smelting().addSmelting(dusts.itemID, ItemDusts.Const.pureCopperDust.ordinal(), new ItemStack(EmasherCore.ingot, 1, 2), 1);
 		FurnaceRecipes.smelting().addSmelting(dusts.itemID, ItemDusts.Const.pureNickelDust.ordinal(), new ItemStack(EmasherCore.ingot, 1, 4), 1);
 		FurnaceRecipes.smelting().addSmelting(dusts.itemID, ItemDusts.Const.pureLeadDust.ordinal(), new ItemStack(EmasherCore.ingot, 1, 3), 1);
 		FurnaceRecipes.smelting().addSmelting(dusts.itemID, ItemDusts.Const.pureSilverDust.ordinal(), new ItemStack(EmasherCore.ingot, 1, 6), 1);
 		FurnaceRecipes.smelting().addSmelting(dusts.itemID, ItemDusts.Const.purePlatinumDust.ordinal(), new ItemStack(EmasherCore.ingot, 1, 5), 1);
 		
 		//Mixer
 		
 		MixerRecipeRegistry.registerRecipe(new ItemStack(EmasherCore.mixedDirt), new FluidStack(FluidRegistry.WATER, 1000), new FluidStack(fluidSlickwater, 1000));
 		MixerRecipeRegistry.registerRecipe(new ItemStack(EmasherCore.mixedSand), new FluidStack(FluidRegistry.WATER, 1000), new FluidStack(fluidSlickwater, 1000));
 		MixerRecipeRegistry.registerRecipe(new ItemStack(Block.sand), new FluidStack(FluidRegistry.WATER, 1000), new FluidStack(fluidSlickwater, 1000));
 		MixerRecipeRegistry.registerRecipe(new ItemStack(groundLimestone), new FluidStack(FluidRegistry.WATER, 1000), new FluidStack(fluidSlickwater, 1000));
 		
 	}
 	
 	private void registerInRegistry()
 	{
 		Registry.addBlock("socket", socket);
 		Registry.addBlock("colouredPlanks", paintedPlanks);
 		Registry.addBlock("groundLimestone", groundLimestone);
 		Registry.addBlock("Slickwater", blockSlickwater);
 		
 		Registry.addItem("module", module);
 		Registry.addItem("blankModule", blankSide);
 		Registry.addItem("socketRemote", remote);
 		Registry.addItem("wrench", engWrench);
 		Registry.addItem("redstoneWand", this.rsWand);
 		Registry.addItem("handBoiler", this.handboiler);
 		for(int i = 0; i < 16; i++) Registry.addItem("paint" + colours[i], this.paintCans[i]);
 		Registry.addItem("dust", this.dusts);
 		Registry.addItem("slickwaterBucket", this.slickBucket);
 	}
 }
