 package clashsoft.mods.moreminerals;
 
 import java.util.Arrays;
 
 import clashsoft.clashsoftapi.CustomBlock;
 import clashsoft.clashsoftapi.CustomCreativeTab;
 import clashsoft.clashsoftapi.CustomItem;
 import clashsoft.clashsoftapi.ItemCustomBlock;
 import clashsoft.clashsoftapi.datatools.*;
 import clashsoft.clashsoftapi.util.*;
 import clashsoft.clashsoftapi.util.CSItems.DataToolSet;
 import clashsoft.mods.moreminerals.block.*;
 import clashsoft.mods.moreminerals.client.MMMClientProxy;
 import clashsoft.mods.moreminerals.common.MMMCommonProxy;
 import clashsoft.mods.moreminerals.orecrusher.OreCrusherRecipes;
 import clashsoft.mods.moreminerals.tileentity.TileEntityOreCrusher;
 import clashsoft.mods.moreminerals.world.gen.MMMOreGenerator;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.EntityJoinWorldEvent;
 
 @Mod(modid = "MoreMineralsMod", name = "More Minerals Mod", version = MoreMineralsMod.VERSION)
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class MoreMineralsMod
 {
 	public static final int			REVISION				= 4;
 	public static final String		VERSION					= CSUpdate.CURRENT_VERSION + "-" + REVISION;
 	
 	@Instance("MoreMineralsMod")
 	public static MoreMineralsMod	INSTANCE;
 	
 	@SidedProxy(clientSide = "clashsoft.mods.moreminerals.client.MMMClientProxy", serverSide = "clashsoft.mods.moreminerals.common.MMMCommonProxy")
 	public static MMMCommonProxy	proxy;
 	
 	public static CustomCreativeTab	stoneOresTab			= new CustomCreativeTab("MM_stoneores");
 	public static CustomCreativeTab	netherOresTab			= new CustomCreativeTab("MM_netherores");
 	public static CustomCreativeTab	endOresTab				= new CustomCreativeTab("MM_endores");
 	public static CustomCreativeTab	dirtOresTab				= new CustomCreativeTab("MM_dirtores");
 	public static CustomCreativeTab	sandOresTab				= new CustomCreativeTab("MM_sandores");
 	public static CustomCreativeTab	rawMaterialsTab			= new CustomCreativeTab("MM_rawmaterials");
 	public static CustomCreativeTab	toolsTab				= new CustomCreativeTab("MM_tools");
 	
 	public static int				OreCrusher_TEID			= 15;
 	
 	public static int				vanillaOres_ID			= 1200;
 	public static int				vanillaOres_ID2			= 1201;
 	public static int				oreCrusher_ID			= 1250;
 	public static int				oreCrusherActive_ID		= 1251;
 	
 	public static int				stoneOres_ID1			= 1202;
 	public static int				stoneOres_ID2			= 1203;
 	public static int				stoneOres_ID3			= 1204;
 	public static int				stoneOres_ID4			= 1205;
 	public static int				stoneOres_ID5			= 1206;
 	public static int				stoneOres_ID6			= 1207;
 	public static int				stoneOres_ID7			= 1208;
 	public static int				stoneOres_ID8			= 1209;
 	public static int				netherOres_ID1			= 1210;
 	public static int				netherOres_ID2			= 1211;
 	public static int				netherOres_ID3			= 1212;
 	public static int				netherOres_ID4			= 1213;
 	public static int				netherOres_ID5			= 1214;
 	public static int				netherOres_ID6			= 1215;
 	public static int				netherOres_ID7			= 1216;
 	public static int				netherOres_ID8			= 1217;
 	public static int				endOres_ID1				= 1218;
 	public static int				endOres_ID2				= 1219;
 	public static int				endOres_ID3				= 1220;
 	public static int				endOres_ID4				= 1221;
 	public static int				endOres_ID5				= 1222;
 	public static int				endOres_ID6				= 1223;
 	public static int				endOres_ID7				= 1224;
 	public static int				endOres_ID8				= 1225;
 	public static int				dirtOres_ID1			= 1226;
 	public static int				dirtOres_ID2			= 1227;
 	public static int				dirtOres_ID3			= 1228;
 	public static int				dirtOres_ID4			= 1229;
 	public static int				dirtOres_ID5			= 1230;
 	public static int				dirtOres_ID6			= 1231;
 	public static int				dirtOres_ID7			= 1232;
 	public static int				dirtOres_ID8			= 1233;
 	public static int				sandOres_ID1			= 1234;
 	public static int				sandOres_ID2			= 1235;
 	public static int				sandOres_ID3			= 1236;
 	public static int				sandOres_ID4			= 1237;
 	public static int				sandOres_ID5			= 1238;
 	public static int				sandOres_ID6			= 1239;
 	public static int				sandOres_ID7			= 1240;
 	public static int				sandOres_ID8			= 1241;
 	public static int				storageBlocks_ID1		= 1242;
 	public static int				storageBlocks_ID2		= 1243;
 	public static int				storageBlocks_ID3		= 1244;
 	public static int				storageBlocks_ID4		= 1245;
 	public static int				storageBlocks_ID5		= 1246;
 	public static int				storageBlocks_ID6		= 1247;
 	public static int				storageBlocks_ID7		= 1248;
 	public static int				storageBlocks_ID8		= 1249;
 	
 	public static int				vanillaSpecialItems_ID	= 11000;
 	public static int				ingots_ID				= 11001;
 	public static int				dusts_ID				= 11002;
 	public static int				nuggets_ID				= 11003;
 	public static int				gems_ID					= 11004;
 	
 	public static int				dataSword_ID			= 11005;
 	public static int				dataSpade_ID			= 11006;
 	public static int				dataPickaxe_ID			= 11007;
 	public static int				dataAxe_ID				= 11008;
 	public static int				dataHoe_ID				= 11009;
 	
 	public static int[]				overworldGen			= new int[] { 32, 32, 0, 0, 0, 28, 13, 0, // Silicon
 			0, 0, 0, 0, 0, 12, 32, 16, // Chrome
 			24, 0, 0, 32, 64, 0, 0, 0, // Germanium
 			0, 0, 0, 0, 0, 0, 0, 16, // Molybdenum
 			0, 0, 0, 0, 32, 0, 0, 64, // Tin
 			0, 0, 0, 0, 0, 0, 0, 0, // Preasodynmium
 			0, 0, 0, 0, 0, 0, 0, 0, // Holmium
 			0, 0, 0, 0, 0, 0, 16, 0, // Rhemium
 			0, 12, 16, 0, 0, 0, 48, 0, // Bismuth
 			0, 0, 0, 0, 0, 0, 0, 24, // Uranium
 			0, 0, 12, 14, 20, 16, 24, 24, // Ruby
 			24, 24, 16, 16, 0, 0, 0, 0, // -
 			0, 0, 0, 0, 0, 0, 0, 0, // -
 			0, 0, 0, 0, 0, 0, 0, 0, // -
 			0, 0, 0, 0, 0, 0, 0, 0, // -
 			0, 0, 0, 0, 0, 0, 0, 0							// -
 															};
 	public static int[]				netherGen				= overworldGen, endGen = overworldGen, dirtGen = overworldGen, sandGen = overworldGen;
 	
 	public static int[]				vanillaGen				= new int[] { 128, 16, 32, 32, 64, 32, 16 };
 	
 	public static String[]			allnames				= new String[] { "Lithium", "Beryllium", "%&Boron", "%&Carbon", "%&Sodium", "Magnesium", "Aluminium", "%&Silicon", // -
 			"%&Phosphorus", "%&Sulfur", "%&Potassium", "%&Calcium", "%&Scandium", "Titanium", "Vanadium", "Chrome", // -
 			"Manganese", "%&Iron", "%&Cobalt", "Nickel", "Copper", "%&Zinc", "%&Gallium", "%&Germanium", // -
 			"%&Arsenic", "%&Selenium", "%&Rubidium", "%&Strontium", "%&Yttrium", "%&Zirconium", "%&Niobium", "Molybdenum", // -
 			"%&Technetium", "%&Ruthenium", "%&Rhodium", "%&Palladium", "Silver", "%&Cadmium", "%&Indium", "Tin", // -
 			"%&Antimony", "%&Tellurium", "%&Iodine", "%&Caesium", "%&Barium", "%&Lanthanum", "%&Cerium", "%&Praseodymium", // -
 			"%&Neodynium", "%&Promethium", "%&Samarium", "%&Europium", "%&Gadolinium", "%&Terbium", "%&Dysprosium", "%&Holmium", // -
 			"%&Erbium", "%&Thulium", "%&Ytterbium", "%&Lutetium", "%&Hafnium", "%&Tantalum", "Tungsten", "%&Rhenium", // -
 			"%&Osmium", "Iridium", "Platinum", "%&Gold", "%&Mercury", "%&Thallium", "Lead", "%&Bismuth", // -
 			"%&Polonium", "%&Astatine", "%&Francium", "%&Radium", "%&Actinium", "%&Thorium", "%&Protactinium", "Uranium", // -
 			"%&Neptunium", "%&Plutonium", "Adamantite", "Cobalt", "Demonite", "Mythril", "Amethyst", "Ruby", // -
 			"Sapphire", "Topaz", "Spinel", "Opal", "%&", "%&", "%&", "%&", // -
 			"%&", "%&", "%&", "%&", "%&", "%&", "%&", "%&", // -
 			"%&", "%&", "%&", "%&", "%&", "%&", "%&", "%&", // -
 			"%&", "%&", "%&", "%&", "%&", "%&", "%&", "%&", // -
 			"%&", "%&", "%&", "%&", "%&", "%&", "%&", "%&", };
 	public static String[][]		splitnames				= CSArrays.split(allnames, 16);
 	public static String[]			names1					= splitnames[0];
 	public static String[]			names2					= splitnames[1];
 	public static String[]			names3					= splitnames[2];
 	public static String[]			names4					= splitnames[3];
 	public static String[]			names5					= splitnames[4];
 	public static String[]			names6					= splitnames[5];
 	public static String[]			names7					= splitnames[6];
 	public static String[]			names8					= splitnames[7];
 	public static String[]			gemnames				= new String[] { "Amethyst", "Ruby", "Sapphire", "Topaz", "Spinel", "Opal" };
 	public static String[]			vanillanames			= new String[] { "Coal", "Diamond", "Emerald", "Gold", "Iron", "Lapislazuli", "Redstone" };
 	
 	public static String[]			alloverlays				= CSArrays.caseAll(allnames, 0);
 	public static String[]			overlays1				= CSArrays.caseAll(names1, 0);
 	public static String[]			overlays2				= CSArrays.caseAll(names2, 0);
 	public static String[]			overlays3				= CSArrays.caseAll(names3, 0);
 	public static String[]			overlays4				= CSArrays.caseAll(names4, 0);
 	public static String[]			overlays5				= CSArrays.caseAll(names5, 0);
 	public static String[]			overlays6				= CSArrays.caseAll(names6, 0);
 	public static String[]			overlays7				= CSArrays.caseAll(names7, 0);
 	public static String[]			overlays8				= CSArrays.caseAll(names8, 0);
 	public static String[]			gemoverlays				= CSArrays.caseAll(gemnames, 0);
 	public static String[]			vanillaoverlays			= CSArrays.caseAll(vanillanames, 0);
 	
 	private static int[]			gemids					= new int[] { CSArrays.indexOf(allnames, "Amethyst"), CSArrays.indexOf(allnames, "Ruby"), CSArrays.indexOf(allnames, "Sapphire"), CSArrays.indexOf(allnames, "Topaz"), CSArrays.indexOf(allnames, "Spinel"), CSArrays.indexOf(allnames, "Opal") };
 	
 	public static CustomBlock		vanillaSpecialOres1;
 	public static CustomBlock		vanillaSpecialOres2;
 	public static CustomBlock		stoneOres1;
 	public static CustomBlock		stoneOres2;
 	public static CustomBlock		stoneOres3;
 	public static CustomBlock		stoneOres4;
 	public static CustomBlock		stoneOres5;
 	public static CustomBlock		stoneOres6;
 	public static CustomBlock		stoneOres7;
 	public static CustomBlock		stoneOres8;
 	public static CustomBlock		netherOres1;
 	public static CustomBlock		netherOres2;
 	public static CustomBlock		netherOres3;
 	public static CustomBlock		netherOres4;
 	public static CustomBlock		netherOres5;
 	public static CustomBlock		netherOres6;
 	public static CustomBlock		netherOres7;
 	public static CustomBlock		netherOres8;
 	public static CustomBlock		endOres1;
 	public static CustomBlock		endOres2;
 	public static CustomBlock		endOres3;
 	public static CustomBlock		endOres4;
 	public static CustomBlock		endOres5;
 	public static CustomBlock		endOres6;
 	public static CustomBlock		endOres7;
 	public static CustomBlock		endOres8;
 	public static CustomBlock		dirtOres1;
 	public static CustomBlock		dirtOres2;
 	public static CustomBlock		dirtOres3;
 	public static CustomBlock		dirtOres4;
 	public static CustomBlock		dirtOres5;
 	public static CustomBlock		dirtOres6;
 	public static CustomBlock		dirtOres7;
 	public static CustomBlock		dirtOres8;
 	public static CustomBlock		sandOres1;
 	public static CustomBlock		sandOres2;
 	public static CustomBlock		sandOres3;
 	public static CustomBlock		sandOres4;
 	public static CustomBlock		sandOres5;
 	public static CustomBlock		sandOres6;
 	public static CustomBlock		sandOres7;
 	public static CustomBlock		sandOres8;
 	public static CustomBlock		storageBlocks1;
 	public static CustomBlock		storageBlocks2;
 	public static CustomBlock		storageBlocks3;
 	public static CustomBlock		storageBlocks4;
 	public static CustomBlock		storageBlocks5;
 	public static CustomBlock		storageBlocks6;
 	public static CustomBlock		storageBlocks7;
 	public static CustomBlock		storageBlocks8;
 	
 	public static BlockOreCrusher	oreCrusher;
 	public static BlockOreCrusher	oreCrusherActive;
 	
 	public static CustomItem		vanillaSpecialItems;
 	public static CustomItem		ingots;
 	public static CustomItem		dusts;
 	public static CustomItem		nuggets;
 	public static CustomItem		gems;
 	
 	public static ItemDataSword		dataSword;
 	public static ItemDataSpade		dataSpade;
 	public static ItemDataPickaxe	dataPickaxe;
 	public static ItemDataAxe		dataAxe;
 	public static ItemDataHoe		dataHoe;
 	
 	public static EnumToolMaterial	adamantite;
 	public static EnumToolMaterial	cobalt;
 	public static EnumToolMaterial	demonite;
 	public static EnumToolMaterial	mythril;
 	public static EnumToolMaterial	aluminium;
 	public static EnumToolMaterial	chrome;
 	public static EnumToolMaterial	copper;
 	public static EnumToolMaterial	silver;
 	public static EnumToolMaterial	tin;
 	public static EnumToolMaterial	titanium;
 	public static EnumToolMaterial	emerald;
 	public static EnumToolMaterial	ruby;
 	public static EnumToolMaterial	sapphire;
 	
 	@EventHandler
 	public void preInit(FMLPreInitializationEvent event)
 	{
 		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
 		config.load();
 		
 		overworldGen = config.get("Generation", "Overworld Max Height", overworldGen, "Ores: [" + Arrays.toString(allnames) + "] Note: %& = not implemented, 0 = does not generate").getIntList();
 		netherGen = config.get("Generation", "Nether Max Height", netherGen, "Nether Ores: [" + Arrays.toString(allnames) + "] Note: %& = not implemented, 0 = does not generate").getIntList();
 		endGen = config.get("Generation", "End Max Height", endGen, "End Ores: [" + Arrays.toString(allnames) + "] Note: %& = not implemented, 0 = does not generate").getIntList();
 		dirtGen = config.get("Generation", "Dirt Max Height", dirtGen, "Dirt Ores: [" + Arrays.toString(allnames) + "] Note: %& = not implemented, 0 = does not generate").getIntList();
 		sandGen = config.get("Generation", "Sand Max Height", sandGen, "Sand Ores: [" + Arrays.toString(allnames) + "] Note: %& = not implemented, 0 = does not generate").getIntList();
 		
 		vanillaOres_ID = config.getBlock("Special Vanilla Ores ID 1", 1200).getInt();
 		vanillaOres_ID2 = config.getBlock("Special Vanilla Ores ID 2", 1201).getInt();
 		stoneOres_ID1 = config.getBlock("Stone-Based Ores ID 1", 1202).getInt();
 		stoneOres_ID2 = config.getBlock("Stone-Based Ores ID 2", 1203).getInt();
 		stoneOres_ID3 = config.getBlock("Stone-Based Ores ID 3", 1204).getInt();
 		stoneOres_ID4 = config.getBlock("Stone-Based Ores ID 4", 1205).getInt();
 		stoneOres_ID5 = config.getBlock("Stone-Based Ores ID 5", 1206).getInt();
 		stoneOres_ID6 = config.getBlock("Stone-Based Ores ID 6", 1207).getInt();
 		stoneOres_ID7 = config.getBlock("Stone-Based Ores ID 7", 1208).getInt();
 		stoneOres_ID8 = config.getBlock("Stone-Based Ores ID 8", 1209).getInt();
 		netherOres_ID1 = config.getBlock("Netherrack-Based Ores ID 1", 1210).getInt();
 		netherOres_ID2 = config.getBlock("Netherrack-Based Ores ID 2", 1211).getInt();
 		netherOres_ID3 = config.getBlock("Netherrack-Based Ores ID 3", 1212).getInt();
 		netherOres_ID4 = config.getBlock("Netherrack-Based Ores ID 4", 1213).getInt();
 		netherOres_ID5 = config.getBlock("Netherrack-Based Ores ID 5", 1214).getInt();
 		netherOres_ID6 = config.getBlock("Netherrack-Based Ores ID 6", 1215).getInt();
 		netherOres_ID7 = config.getBlock("Netherrack-Based Ores ID 7", 1216).getInt();
 		netherOres_ID8 = config.getBlock("Netherrack-Based Ores ID 8", 1217).getInt();
 		endOres_ID1 = config.getBlock("Endstone-Based Ores ID 1", 1218).getInt();
 		endOres_ID2 = config.getBlock("Endstone-Based Ores ID 2", 1219).getInt();
 		endOres_ID3 = config.getBlock("Endstone-Based Ores ID 3", 1220).getInt();
 		endOres_ID4 = config.getBlock("Endstone-Based Ores ID 4", 1221).getInt();
 		endOres_ID5 = config.getBlock("Endstone-Based Ores ID 5", 1222).getInt();
 		endOres_ID6 = config.getBlock("Endstone-Based Ores ID 6", 1223).getInt();
 		endOres_ID7 = config.getBlock("Endstone-Based Ores ID 7", 1224).getInt();
 		endOres_ID8 = config.getBlock("Endstone-Based Ores ID 8", 1225).getInt();
 		dirtOres_ID1 = config.getBlock("Dirt-Based Ores ID 1", 1226).getInt();
 		dirtOres_ID2 = config.getBlock("Dirt-Based Ores ID 2", 1227).getInt();
 		dirtOres_ID3 = config.getBlock("Dirt-Based Ores ID 3", 1228).getInt();
 		dirtOres_ID4 = config.getBlock("Dirt-Based Ores ID 4", 1229).getInt();
 		dirtOres_ID5 = config.getBlock("Dirt-Based Ores ID 5", 1230).getInt();
 		dirtOres_ID6 = config.getBlock("Dirt-Based Ores ID 6", 1231).getInt();
 		dirtOres_ID7 = config.getBlock("Dirt-Based Ores ID 7", 1232).getInt();
 		dirtOres_ID8 = config.getBlock("Dirt-Based Ores ID 8", 1233).getInt();
 		sandOres_ID1 = config.getBlock("Sand-Based Ores ID 1", 1234).getInt();
 		sandOres_ID2 = config.getBlock("Sand-Based Ores ID 2", 1235).getInt();
 		sandOres_ID3 = config.getBlock("Sand-Based Ores ID 3", 1236).getInt();
 		sandOres_ID4 = config.getBlock("Sand-Based Ores ID 4", 1237).getInt();
 		sandOres_ID5 = config.getBlock("Sand-Based Ores ID 5", 1238).getInt();
 		sandOres_ID6 = config.getBlock("Sand-Based Ores ID 6", 1239).getInt();
 		sandOres_ID7 = config.getBlock("Sand-Based Ores ID 7", 1240).getInt();
 		sandOres_ID8 = config.getBlock("Sand-Based Ores ID 8", 1241).getInt();
 		storageBlocks_ID1 = config.getBlock("Storage Blocks ID 1", 1242).getInt();
 		storageBlocks_ID2 = config.getBlock("Storage Blocks ID 2", 1243).getInt();
 		storageBlocks_ID3 = config.getBlock("Storage Blocks ID 3", 1244).getInt();
 		storageBlocks_ID4 = config.getBlock("Storage Blocks ID 4", 1245).getInt();
 		storageBlocks_ID5 = config.getBlock("Storage Blocks ID 5", 1246).getInt();
 		storageBlocks_ID6 = config.getBlock("Storage Blocks ID 6", 1247).getInt();
 		storageBlocks_ID7 = config.getBlock("Storage Blocks ID 7", 1248).getInt();
 		storageBlocks_ID8 = config.getBlock("Storage Blocks ID 8", 1249).getInt();
 		oreCrusher_ID = config.getBlock("Ore Crusher (Idle) ID", 1250).getInt();
 		oreCrusherActive_ID = config.getBlock("Ore Crusher (Active) ID", 1251).getInt();
 		
 		OreCrusher_TEID = config.get("Tile Entity IDs", "Ore Crusher Tile Entity ID", 15).getInt();
 		
 		vanillaSpecialItems_ID = config.getItem("Special Vanilla Items ID", 11000).getInt();
 		ingots_ID = config.getItem("Ingots ID", 11001).getInt();
 		dusts_ID = config.getItem("Dusts ID", 11002).getInt();
 		nuggets_ID = config.getItem("Nuggets ID", 11003).getInt();
 		gems_ID = config.getItem("Gems ID", 11004).getInt();
 		
 		dataSword_ID = config.getItem("Swords ID", 11005).getInt();
 		dataSpade_ID = config.getItem("Shovels ID", 11006).getInt();
 		dataPickaxe_ID = config.getItem("Pickaxes ID", 11007).getInt();
 		dataAxe_ID = config.getItem("Axes ID", 11008).getInt();
 		dataHoe_ID = config.getItem("Hoes ID", 11009).getInt();
 		
 		config.save();
 	}
 	
 	@EventHandler
 	public void load(FMLInitializationEvent event)
 	{
 		CSUtil.log("[MoreMineralsMod] Loading More Minerals Mod");
 		CSUtil.log("[MoreMineralsMod] " + allnames.length + " Materials added");
 		
 		vanillaSpecialItems = (CustomItem) new CustomItem(vanillaSpecialItems_ID, CSArrays.combine(CSArrays.combine(CSArrays.addToAll(vanillanames, "", " Ingot"), CSArrays.addToAll(vanillanames, "", " Dust")), CSArrays.addToAll(vanillanames, "", " Nugget")), CSArrays.combine(CSArrays.combine(CSArrays.addToAll(vanillaoverlays, "ingot", ""), CSArrays.addToAll(vanillaoverlays, "dust", "")), CSArrays.addToAll(vanillaoverlays, "nugget", ""))).disableMetadata(3, 4, 13, 17).setUnlocalizedName("MM_VanillaSpecialItems").setCreativeTab(rawMaterialsTab);
 		ingots = (CustomItem) new CustomItem(ingots_ID, CSArrays.addToAll(allnames, "", " Ingot"), CSArrays.addToAll(alloverlays, "ingot", "")).disableMetadata(gemids).setUnlocalizedName("MM_Ingots").setCreativeTab(rawMaterialsTab);
 		dusts = (CustomItem) new CustomItem(dusts_ID, CSArrays.addToAll(allnames, "", " Dust"), CSArrays.addToAll(alloverlays, "dust", "")).setUnlocalizedName("MM_Dusts").setCreativeTab(rawMaterialsTab);
 		nuggets = (CustomItem) new CustomItem(nuggets_ID, CSArrays.addToAll(allnames, "", " Nugget"), CSArrays.addToAll(alloverlays, "nugget", "")).setUnlocalizedName("MM_Nuggets").setCreativeTab(rawMaterialsTab);
 		gems = (CustomItem) new CustomItem(gems_ID, gemnames, CSArrays.addToAll(gemoverlays, "gem", "")).setUnlocalizedName("MM_Gems").setCreativeTab(rawMaterialsTab);
 		
 		dataSword = (ItemDataSword) new ItemDataSword(dataSword_ID, EnumToolMaterial.IRON).setCreativeTab(toolsTab).setUnlocalizedName("MM_Swords");
 		dataSpade = (ItemDataSpade) new ItemDataSpade(dataSpade_ID, EnumToolMaterial.IRON).setCreativeTab(toolsTab).setUnlocalizedName("MM_Spades");
 		dataPickaxe = (ItemDataPickaxe) new ItemDataPickaxe(dataPickaxe_ID, EnumToolMaterial.IRON).setCreativeTab(toolsTab).setUnlocalizedName("MM_Pickaxes");
 		dataAxe = (ItemDataAxe) new ItemDataAxe(dataAxe_ID, EnumToolMaterial.IRON).setCreativeTab(toolsTab).setUnlocalizedName("MM_Axes");
 		dataHoe = (ItemDataHoe) new ItemDataHoe(dataHoe_ID, EnumToolMaterial.IRON).setCreativeTab(toolsTab).setUnlocalizedName("MM_Hoes");
 		
 		setupToolMaterials(new CSItems.DataToolSet(dataSword, dataSpade, dataPickaxe, dataAxe, dataHoe));
 		
 		proxy.registerBlockRenderers();
 		proxy.registerItemRenderers();
 		
 		String[] vanillaNames = CSArrays.combine(CSArrays.addToAll(vanillanames, "Nether ", " Ore"), CSArrays.addToAll(vanillanames, "End ", " Ore"));
 		String[] vanillaNames2 = CSArrays.combine(CSArrays.addToAll(vanillanames, "Dirt-Based ", " Ore"), CSArrays.addToAll(vanillanames, "Sand-Based ", " Ore"));
 		vanillaSpecialOres1 = (CustomBlock) new CustomBlock(vanillaOres_ID, Material.rock, vanillaNames, CSArrays.addToAll(CSArrays.combine(vanillaoverlays, vanillaoverlays), "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { netherOresTab, netherOresTab, netherOresTab, netherOresTab, netherOresTab, netherOresTab, netherOresTab, endOresTab, endOresTab, endOresTab, endOresTab, endOresTab, endOresTab, endOresTab, endOresTab }).setUnlocalizedName("MM_VanillaSpecialOres").setHardness(2.15F);
 		vanillaSpecialOres2 = (CustomBlock) new BlockVanillaSpecialOre(vanillaOres_ID2, Material.ground, vanillaNames2, CSArrays.addToAll(CSArrays.combine(vanillaoverlays, vanillaoverlays), "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { dirtOresTab, dirtOresTab, dirtOresTab, dirtOresTab, dirtOresTab, dirtOresTab, dirtOresTab, sandOresTab, sandOresTab, sandOresTab, sandOresTab, sandOresTab, sandOresTab, sandOresTab }).setUnlocalizedName("MM_VanillaSpecialOres2").setHardness(1.7F);
 		
 		stoneOres1 = (CustomBlock) new CustomBlock(stoneOres_ID1, Material.rock, CSArrays.addToAll(names1, "", " Ore"), CSArrays.addToAll(overlays1, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { stoneOresTab }).setUnlocalizedName("MM_StoneOres1").setHardness(2.3F);
 		stoneOres2 = (CustomBlock) new CustomBlock(stoneOres_ID2, Material.rock, CSArrays.addToAll(names2, "", " Ore"), CSArrays.addToAll(overlays2, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { stoneOresTab }).setUnlocalizedName("MM_StoneOres2").setHardness(2.3F);
 		stoneOres3 = (CustomBlock) new CustomBlock(stoneOres_ID3, Material.rock, CSArrays.addToAll(names3, "", " Ore"), CSArrays.addToAll(overlays3, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { stoneOresTab }).setUnlocalizedName("MM_StoneOres3").setHardness(2.3F);
 		stoneOres4 = (CustomBlock) new CustomBlock(stoneOres_ID4, Material.rock, CSArrays.addToAll(names4, "", " Ore"), CSArrays.addToAll(overlays4, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { stoneOresTab }).setUnlocalizedName("MM_StoneOres4").setHardness(2.3F);
 		stoneOres5 = (CustomBlock) new CustomBlock(stoneOres_ID5, Material.rock, CSArrays.addToAll(names5, "", " Ore"), CSArrays.addToAll(overlays5, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { stoneOresTab }).setUnlocalizedName("MM_StoneOres5").setHardness(2.3F);
 		stoneOres6 = (CustomBlock) new CustomBlock(stoneOres_ID6, Material.rock, CSArrays.addToAll(names6, "", " Ore"), CSArrays.addToAll(overlays6, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { stoneOresTab }).setUnlocalizedName("MM_StoneOres6").setHardness(2.3F);
 		stoneOres7 = (CustomBlock) new CustomBlock(stoneOres_ID7, Material.rock, CSArrays.addToAll(names7, "", " Ore"), CSArrays.addToAll(overlays7, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { stoneOresTab }).setUnlocalizedName("MM_StoneOres7").setHardness(2.3F);
 		stoneOres8 = (CustomBlock) new CustomBlock(stoneOres_ID8, Material.rock, CSArrays.addToAll(names8, "", " Ore"), CSArrays.addToAll(overlays8, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { stoneOresTab }).setUnlocalizedName("MM_StoneOres8").setHardness(2.3F);
 		
 		netherOres1 = (CustomBlock) new CustomBlock(netherOres_ID1, Material.rock, CSArrays.addToAll(names1, "Nether ", " Ore"), CSArrays.addToAll(overlays1, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { netherOresTab }).setUnlocalizedName("MM_NetherOres1").setHardness(2.1F);
 		netherOres2 = (CustomBlock) new CustomBlock(netherOres_ID2, Material.rock, CSArrays.addToAll(names2, "Nether ", " Ore"), CSArrays.addToAll(overlays2, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { netherOresTab }).setUnlocalizedName("MM_NetherOres2").setHardness(2.1F);
 		netherOres3 = (CustomBlock) new CustomBlock(netherOres_ID3, Material.rock, CSArrays.addToAll(names3, "Nether ", " Ore"), CSArrays.addToAll(overlays3, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { netherOresTab }).setUnlocalizedName("MM_NetherOres3").setHardness(2.1F);
 		netherOres4 = (CustomBlock) new CustomBlock(netherOres_ID4, Material.rock, CSArrays.addToAll(names4, "Nether ", " Ore"), CSArrays.addToAll(overlays4, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { netherOresTab }).setUnlocalizedName("MM_NetherOres4").setHardness(2.1F);
 		netherOres5 = (CustomBlock) new CustomBlock(netherOres_ID5, Material.rock, CSArrays.addToAll(names5, "Nether ", " Ore"), CSArrays.addToAll(overlays5, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { netherOresTab }).setUnlocalizedName("MM_NetherOres5").setHardness(2.1F);
 		netherOres6 = (CustomBlock) new CustomBlock(netherOres_ID6, Material.rock, CSArrays.addToAll(names6, "Nether ", " Ore"), CSArrays.addToAll(overlays6, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { netherOresTab }).setUnlocalizedName("MM_NetherOres6").setHardness(2.1F);
 		netherOres7 = (CustomBlock) new CustomBlock(netherOres_ID7, Material.rock, CSArrays.addToAll(names7, "Nether ", " Ore"), CSArrays.addToAll(overlays7, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { netherOresTab }).setUnlocalizedName("MM_NetherOres7").setHardness(2.1F);
 		netherOres8 = (CustomBlock) new CustomBlock(netherOres_ID8, Material.rock, CSArrays.addToAll(names8, "Nether ", " Ore"), CSArrays.addToAll(overlays8, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { netherOresTab }).setUnlocalizedName("MM_NetherOres8").setHardness(2.1F);
 		
 		endOres1 = (CustomBlock) new CustomBlock(endOres_ID1, Material.rock, CSArrays.addToAll(names1, "End ", " Ore"), CSArrays.addToAll(overlays1, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { endOresTab }).setUnlocalizedName("MM_EndOres1").setHardness(2.2F);
 		endOres2 = (CustomBlock) new CustomBlock(endOres_ID2, Material.rock, CSArrays.addToAll(names2, "End ", " Ore"), CSArrays.addToAll(overlays2, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { endOresTab }).setUnlocalizedName("MM_EndOres2").setHardness(2.2F);
 		endOres3 = (CustomBlock) new CustomBlock(endOres_ID3, Material.rock, CSArrays.addToAll(names3, "End ", " Ore"), CSArrays.addToAll(overlays3, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { endOresTab }).setUnlocalizedName("MM_EndOres3").setHardness(2.2F);
 		endOres4 = (CustomBlock) new CustomBlock(endOres_ID4, Material.rock, CSArrays.addToAll(names4, "End ", " Ore"), CSArrays.addToAll(overlays4, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { endOresTab }).setUnlocalizedName("MM_EndOres4").setHardness(2.2F);
 		endOres5 = (CustomBlock) new CustomBlock(endOres_ID5, Material.rock, CSArrays.addToAll(names5, "End ", " Ore"), CSArrays.addToAll(overlays5, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { endOresTab }).setUnlocalizedName("MM_EndOres5").setHardness(2.2F);
 		endOres6 = (CustomBlock) new CustomBlock(endOres_ID6, Material.rock, CSArrays.addToAll(names6, "End ", " Ore"), CSArrays.addToAll(overlays6, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { endOresTab }).setUnlocalizedName("MM_EndOres6").setHardness(2.2F);
 		endOres7 = (CustomBlock) new CustomBlock(endOres_ID7, Material.rock, CSArrays.addToAll(names7, "End ", " Ore"), CSArrays.addToAll(overlays7, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { endOresTab }).setUnlocalizedName("MM_EndOres7").setHardness(2.2F);
 		endOres8 = (CustomBlock) new CustomBlock(endOres_ID8, Material.rock, CSArrays.addToAll(names8, "End ", " Ore"), CSArrays.addToAll(overlays8, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { endOresTab }).setUnlocalizedName("MM_EndOres8").setHardness(2.2F);
 		
 		for (int i = 0; i < gemids.length; i++)
 		{
 			OreHelper.getOreFromMetadata(gemids[i], "stone").setDrops(gemids[i] % 16, new ItemStack(gems, 1, i));
 			OreHelper.getOreFromMetadata(gemids[i], "nether").setDrops(gemids[i] % 16, new ItemStack(gems, 2, i));
 			OreHelper.getOreFromMetadata(gemids[i], "end").setDrops(gemids[i] % 16, new ItemStack(gems, 2, i));
 		}
 		
 		dirtOres1 = (CustomBlock) new BlockDirtOre(dirtOres_ID1, Material.ground, CSArrays.addToAll(names1, "Dirt-Based ", " Ore"), CSArrays.addToAll(overlays1, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { dirtOresTab }).setUnlocalizedName("MM_DirtOres1").setHardness(1.7F);
 		dirtOres2 = (CustomBlock) new BlockDirtOre(dirtOres_ID2, Material.ground, CSArrays.addToAll(names2, "Dirt-Based ", " Ore"), CSArrays.addToAll(overlays2, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { dirtOresTab }).setUnlocalizedName("MM_DirtOres2").setHardness(1.7F);
 		dirtOres3 = (CustomBlock) new BlockDirtOre(dirtOres_ID3, Material.ground, CSArrays.addToAll(names3, "Dirt-Based ", " Ore"), CSArrays.addToAll(overlays3, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { dirtOresTab }).setUnlocalizedName("MM_DirtOres3").setHardness(1.7F);
 		dirtOres4 = (CustomBlock) new BlockDirtOre(dirtOres_ID4, Material.ground, CSArrays.addToAll(names4, "Dirt-Based ", " Ore"), CSArrays.addToAll(overlays4, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { dirtOresTab }).setUnlocalizedName("MM_DirtOres4").setHardness(1.7F);
 		dirtOres5 = (CustomBlock) new BlockDirtOre(dirtOres_ID5, Material.ground, CSArrays.addToAll(names5, "Dirt-Based ", " Ore"), CSArrays.addToAll(overlays5, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { dirtOresTab }).setUnlocalizedName("MM_DirtOres5").setHardness(1.7F);
 		dirtOres6 = (CustomBlock) new BlockDirtOre(dirtOres_ID6, Material.ground, CSArrays.addToAll(names6, "Dirt-Based ", " Ore"), CSArrays.addToAll(overlays6, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { dirtOresTab }).setUnlocalizedName("MM_DirtOres6").setHardness(1.7F);
 		dirtOres7 = (CustomBlock) new BlockDirtOre(dirtOres_ID7, Material.ground, CSArrays.addToAll(names7, "Dirt-Based ", " Ore"), CSArrays.addToAll(overlays7, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { dirtOresTab }).setUnlocalizedName("MM_DirtOres7").setHardness(1.7F);
 		dirtOres8 = (CustomBlock) new BlockDirtOre(dirtOres_ID8, Material.ground, CSArrays.addToAll(names8, "Dirt-Based ", " Ore"), CSArrays.addToAll(overlays8, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { dirtOresTab }).setUnlocalizedName("MM_DirtOres8").setHardness(1.7F);
 		
 		sandOres1 = (CustomBlock) new BlockSandOre(sandOres_ID1, Material.ground, CSArrays.addToAll(names1, "Sand-Based ", " Ore"), CSArrays.addToAll(overlays1, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { sandOresTab }).setUnlocalizedName("MM_SandOres1").setHardness(1.7F);
 		sandOres2 = (CustomBlock) new BlockSandOre(sandOres_ID2, Material.ground, CSArrays.addToAll(names2, "Sand-Based ", " Ore"), CSArrays.addToAll(overlays2, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { sandOresTab }).setUnlocalizedName("MM_SandOres2").setHardness(1.7F);
 		sandOres3 = (CustomBlock) new BlockSandOre(sandOres_ID3, Material.ground, CSArrays.addToAll(names3, "Sand-Based ", " Ore"), CSArrays.addToAll(overlays3, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { sandOresTab }).setUnlocalizedName("MM_SandOres3").setHardness(1.7F);
 		sandOres4 = (CustomBlock) new BlockSandOre(sandOres_ID4, Material.ground, CSArrays.addToAll(names4, "Sand-Based ", " Ore"), CSArrays.addToAll(overlays4, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { sandOresTab }).setUnlocalizedName("MM_SandOres4").setHardness(1.7F);
 		sandOres5 = (CustomBlock) new BlockSandOre(sandOres_ID5, Material.ground, CSArrays.addToAll(names5, "Sand-Based ", " Ore"), CSArrays.addToAll(overlays5, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { sandOresTab }).setUnlocalizedName("MM_SandOres5").setHardness(1.7F);
 		sandOres6 = (CustomBlock) new BlockSandOre(sandOres_ID6, Material.ground, CSArrays.addToAll(names6, "Sand-Based ", " Ore"), CSArrays.addToAll(overlays6, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { sandOresTab }).setUnlocalizedName("MM_SandOres6").setHardness(1.7F);
 		sandOres7 = (CustomBlock) new BlockSandOre(sandOres_ID7, Material.ground, CSArrays.addToAll(names7, "Sand-Based ", " Ore"), CSArrays.addToAll(overlays7, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { sandOresTab }).setUnlocalizedName("MM_SandOres7").setHardness(1.7F);
 		sandOres8 = (CustomBlock) new BlockSandOre(sandOres_ID8, Material.ground, CSArrays.addToAll(names8, "Sand-Based ", " Ore"), CSArrays.addToAll(overlays8, "", "overlay"), true, MMMClientProxy.oreRenderer, new CreativeTabs[] { sandOresTab }).setUnlocalizedName("MM_SandOres8").setHardness(1.7F);
 		
 		storageBlocks1 = (CustomBlock) new CustomBlock(storageBlocks_ID1, Material.iron, CSArrays.addToAll(names1, "Block of ", ""), CSArrays.addToAll(overlays1, "", "block"), new CreativeTabs[] { rawMaterialsTab }).setUnlocalizedName("MM_StorageBlocks1").setHardness(2.5F);
 		storageBlocks2 = (CustomBlock) new CustomBlock(storageBlocks_ID2, Material.iron, CSArrays.addToAll(names2, "Block of ", ""), CSArrays.addToAll(overlays2, "", "block"), new CreativeTabs[] { rawMaterialsTab }).setUnlocalizedName("MM_StorageBlocks2").setHardness(2.5F);
 		storageBlocks3 = (CustomBlock) new CustomBlock(storageBlocks_ID3, Material.iron, CSArrays.addToAll(names3, "Block of ", ""), CSArrays.addToAll(overlays3, "", "block"), new CreativeTabs[] { rawMaterialsTab }).setUnlocalizedName("MM_StorageBlocks3").setHardness(2.5F);
 		storageBlocks4 = (CustomBlock) new CustomBlock(storageBlocks_ID4, Material.iron, CSArrays.addToAll(names4, "Block of ", ""), CSArrays.addToAll(overlays4, "", "block"), new CreativeTabs[] { rawMaterialsTab }).setUnlocalizedName("MM_StorageBlocks4").setHardness(2.5F);
 		storageBlocks5 = (CustomBlock) new CustomBlock(storageBlocks_ID5, Material.iron, CSArrays.addToAll(names5, "Block of ", ""), CSArrays.addToAll(overlays5, "", "block"), new CreativeTabs[] { rawMaterialsTab }).setUnlocalizedName("MM_StorageBlocks5").setHardness(2.5F);
 		storageBlocks6 = (CustomBlock) new CustomBlock(storageBlocks_ID6, Material.iron, CSArrays.addToAll(names6, "Block of ", ""), CSArrays.addToAll(overlays6, "", "block"), new CreativeTabs[] { rawMaterialsTab }).setUnlocalizedName("MM_StorageBlocks6").setHardness(2.5F);
 		storageBlocks7 = (CustomBlock) new CustomBlock(storageBlocks_ID7, Material.iron, CSArrays.addToAll(names7, "Block of ", ""), CSArrays.addToAll(overlays7, "", "block"), new CreativeTabs[] { rawMaterialsTab }).setUnlocalizedName("MM_StorageBlocks7").setHardness(2.5F);
 		storageBlocks8 = (CustomBlock) new CustomBlock(storageBlocks_ID8, Material.iron, CSArrays.addToAll(names8, "Block of ", ""), CSArrays.addToAll(overlays8, "", "block"), new CreativeTabs[] { rawMaterialsTab }).setUnlocalizedName("MM_StorageBlocks8").setHardness(2.5F);
 		
 		oreCrusher = (BlockOreCrusher) new BlockOreCrusher(oreCrusher_ID, false).setHardness(2.0F).setStepSound(Block.soundMetalFootstep).setCreativeTab(rawMaterialsTab).setUnlocalizedName("MM_OreCrusher");
 		oreCrusherActive = (BlockOreCrusher) new BlockOreCrusher(oreCrusherActive_ID, false).setHardness(2.0F).setStepSound(Block.soundMetalFootstep).setUnlocalizedName("MM_OreCrusher");
 		
 		Block.oreCoal.setCreativeTab(stoneOresTab);
 		Block.oreDiamond.setCreativeTab(stoneOresTab);
 		Block.oreEmerald.setCreativeTab(stoneOresTab);
 		Block.oreGold.setCreativeTab(stoneOresTab);
 		Block.oreIron.setCreativeTab(stoneOresTab);
 		Block.oreLapis.setCreativeTab(stoneOresTab);
 		Block.oreRedstone.setCreativeTab(stoneOresTab);
 		
 		registerBlocks();
 		addCraftingRecipes();
 		addFurnaceRecipes();
 		addCrusherRecipes();
 		addOreDictionaryEntrys();
 		setHarvestLevelsAndHardnessess();
 		
 		stoneOresTab.setIconItemStack(new ItemStack(OreHelper.getOreFromMetadata(CSArrays.indexOf(allnames, "Magnesium"), "stone"), 1, CSArrays.indexOf(allnames, "Magnesium") % 16));
 		netherOresTab.setIconItemStack(new ItemStack(OreHelper.getOreFromMetadata(CSArrays.indexOf(allnames, "Uranium"), "nether"), 1, CSArrays.indexOf(allnames, "Uranium") % 16));
 		endOresTab.setIconItemStack(new ItemStack(OreHelper.getOreFromMetadata(CSArrays.indexOf(allnames, "Ruby"), "end"), 1, CSArrays.indexOf(allnames, "Ruby") % 16));
 		dirtOresTab.setIconItemStack(new ItemStack(OreHelper.getOreFromMetadata(CSArrays.indexOf(allnames, "Iridium"), "dirt"), 1, CSArrays.indexOf(allnames, "Iridium") % 16));
 		sandOresTab.setIconItemStack(new ItemStack(OreHelper.getOreFromMetadata(CSArrays.indexOf(allnames, "Beryllium"), "sand"), 1, CSArrays.indexOf(allnames, "Beryllium") % 16));
 		rawMaterialsTab.setIconItemStack(new ItemStack(ingots, 1, CSArrays.indexOf(allnames, "Aluminium")));
 		toolsTab.setIconItemStack(ItemDataTool.setToolMaterial(new ItemStack(dataPickaxe), silver));
 		
 		GameRegistry.registerTileEntity(TileEntityOreCrusher.class, "OreCrusher");
 		GameRegistry.registerWorldGenerator(new MMMOreGenerator());
 		NetworkRegistry.instance().registerGuiHandler(INSTANCE, proxy);
 		MinecraftForge.EVENT_BUS.register(this);
 		
 		addLocalizations();
 	}
 	
 	@ForgeSubscribe
 	public void playerJoined(EntityJoinWorldEvent event)
 	{
 		if (event.entity instanceof EntityPlayer)
 		{
			CSUpdate.doClashsoftUpdateCheck((EntityPlayer) event.entity, "More Minerals Mod", "mmm", VERSION);
 		}
 	}
 	
 	private void setupToolMaterials(DataToolSet dataToolSet)
 	{
 		adamantite = CSItems.addToolMaterial("Adamantite", 3, 2048, 14F, 4F, 20, 0x000000, null, dataToolSet);
 		cobalt = CSItems.addToolMaterial("Cobalt", 2, 1536, 10F, 3.2F, 10, 0x000000, null, dataToolSet);
 		demonite = CSItems.addToolMaterial("Demonite", 2, 800, 6F, 3.0F, 30, 0x000000, null, dataToolSet);
 		mythril = CSItems.addToolMaterial("Mythril", 1, 1024, 8F, 3.1F, 12, 0x000000, null, dataToolSet);
 		aluminium = CSItems.addToolMaterial("Aluminium", 2, 512, 8F, 2.2F, 10, 0x000000, null, dataToolSet);
 		chrome = CSItems.addToolMaterial("Chrome", 2, 256, 10F, 2.7F, 12, 0x000000, null, dataToolSet);
 		copper = CSItems.addToolMaterial("Copper", 1, 128, 4F, 1.8F, 8, 0x000000, null, dataToolSet);
 		silver = CSItems.addToolMaterial("Silver", 2, 512, 6F, 2.3F, 16, 0x000000, null, dataToolSet);
 		tin = CSItems.addToolMaterial("Tin", 2, 182, 5F, 1.75F, 9, 0x000000, null, dataToolSet);
 		titanium = CSItems.addToolMaterial("Titanium", 3, 2048, 16F, 2.9F, 13, 0x000000, null, dataToolSet);
 		emerald = CSItems.addToolMaterial("Emerald", 3, 1200, 8F, 2.95F, 17, 0x000000, new ItemStack(Item.emerald), dataToolSet);
 		ruby = CSItems.addToolMaterial("Ruby", 3, 1200, 8F, 2.4F, 17, 0x000000, null, dataToolSet);
 		sapphire = CSItems.addToolMaterial("Sapphire", 3, 1200, 8F, 2.4F, 17, 0x000000, null, dataToolSet);
 	}
 	
 	private void setHarvestLevelsAndHardnessess()
 	{
 		for (int i = 0; i < splitnames.length; i++)
 		{
 			for (int j = 0; j < splitnames[i].length; j++)
 			{
 				int gentype = overworldGen[j + (i * 16)];
 				int harvestLevel = gentype <= 12 ? 3 : (gentype <= 32 ? 2 : (gentype <= 64 ? 1 : 0));
 				MinecraftForge.setBlockHarvestLevel(OreHelper.getOreFromMetadata(j + (i * 16), "stone"), "pickaxe", harvestLevel);
 				MinecraftForge.setBlockHarvestLevel(OreHelper.getOreFromMetadata(j + (i * 16), "nether"), "pickaxe", harvestLevel > 0 ? harvestLevel - 1 : harvestLevel);
 				MinecraftForge.setBlockHarvestLevel(OreHelper.getOreFromMetadata(j + (i * 16), "end"), "pickaxe", harvestLevel);
 				MinecraftForge.setBlockHarvestLevel(OreHelper.getOreFromMetadata(j + (i * 16), "storage"), "pickaxe", harvestLevel);
 				MinecraftForge.setBlockHarvestLevel(OreHelper.getOreFromMetadata(j + (i * 16), "sand"), i, "shovel", 1);
 				MinecraftForge.setBlockHarvestLevel(OreHelper.getOreFromMetadata(j + (i * 16), "dirt"), i, "shovel", 1);
 				
 				float stoneHardness = Block.stone.blockHardness + (6.4F / gentype) + (gentype <= 24 ? 0.5F : 0F);
 				float netherHardness = Block.netherrack.blockHardness + (6.4F / gentype) + (gentype <= 24 ? 0.6F : 0F);
 				float endHardness = Block.whiteStone.blockHardness + (6.4F / gentype) + (gentype <= 24 ? 0.7F : 0F);
 				float dirtHardness = Block.dirt.blockHardness + (6.4F / gentype) + (gentype <= 24 ? 0.4F : 0F);
 				float sandHardness = Block.sand.blockHardness + (6.4F / gentype) + (gentype <= 24 ? 0.4F : 0F);
 				float storageHardness = Block.blockIron.blockHardness + (6.4F / gentype) + (gentype <= 24 ? 0.8F : 0F);
 				OreHelper.getOreFromMetadata(j + (i * 16), "stone").setHardness(j, stoneHardness);
 				OreHelper.getOreFromMetadata(j + (i * 16), "nether").setHardness(j, netherHardness);
 				OreHelper.getOreFromMetadata(j + (i * 16), "end").setHardness(j, endHardness);
 				OreHelper.getOreFromMetadata(j + (i * 16), "dirt").setHardness(j, dirtHardness);
 				OreHelper.getOreFromMetadata(j + (i * 16), "sand").setHardness(j, sandHardness);
 				OreHelper.getOreFromMetadata(j + (i * 16), "storage").setHardness(j, storageHardness);
 			}
 		}
 		for (int i = 0; i < 14; i++)
 		{
 			int vanillaOreType = i % 7;
 			int harvestLevel = 0;
 			if (i == 1 || i == 2 || i == 3 || i == 6) // Diamond, Emerald, Gold,
 														// Redstone
 				harvestLevel = 2; // Iron
 			else if (i == 4 || i == 5)
 				harvestLevel = 1; // Stone
 			MinecraftForge.setBlockHarvestLevel(vanillaSpecialOres1, i, "pickaxe", harvestLevel);
 			MinecraftForge.setBlockHarvestLevel(vanillaSpecialOres2, i, "shovel", harvestLevel > 0 ? harvestLevel - 1 : 0);
 		}
 	}
 	
 	public void addLocalizations()
 	{
 		CSLang.addLocalizationUS("itemGroup.MM_stoneores", "Minerals");
 		CSLang.addLocalizationUS("itemGroup.MM_netherores", "Nether Minerals");
 		CSLang.addLocalizationUS("itemGroup.MM_endores", "End Minerals");
 		CSLang.addLocalizationUS("itemGroup.MM_dirtores", "Dirt-Based Minerals");
 		CSLang.addLocalizationUS("itemGroup.MM_sandores", "Sand-Based Minerals");
 		CSLang.addLocalizationUS("itemGroup.MM_rawmaterials", "Mineral Materials");
 		CSLang.addLocalizationUS("itemGroup.MM_tools", "Mineral Tools");
 		
 		CSLang.addLocalizationUS("container.orecrusher", "Ore Crusher");
 		CSLang.addLocalizationDE("container.orecrusher", "Erz Crusher");
 		
 		LanguageRegistry.addName(oreCrusher, "Ore Crusher");
 		LanguageRegistry.addName(oreCrusherActive, "Ore Crusher");
 	}
 	
 	public void addOreDictionaryEntrys()
 	{
 		// Type 1 Ores
 		for (int i = 0; i < splitnames.length; i++)
 		{
 			for (int j = 0; j < splitnames[i].length; j++)
 			{
 				CSCrafting.registerOre("ore" + splitnames[i][j], new ItemStack(OreHelper.getOreFromMetadata(j + (i * 16), "stone"), 1, j));
 				CSCrafting.registerOre("oreNether" + splitnames[i][j], new ItemStack(OreHelper.getOreFromMetadata(j + (i * 16), "nether"), 1, j));
 				CSCrafting.registerOre("oreEnd" + splitnames[i][j], new ItemStack(OreHelper.getOreFromMetadata(j + (i * 16), "end"), 1, j));
 				CSCrafting.registerOre("oreDirt" + splitnames[i][j], new ItemStack(OreHelper.getOreFromMetadata(j + (i * 16), "dirt"), 1, j));
 				CSCrafting.registerOre("oreSand" + splitnames[i][j], new ItemStack(OreHelper.getOreFromMetadata(j + (i * 16), "sand"), 1, j));
 				CSCrafting.registerOre("block" + splitnames[i][j], new ItemStack(OreHelper.getOreFromMetadata(j + (i * 16), "storage"), 1, j));
 				
 				CSCrafting.registerOre("ingot" + allnames[j + (i * 16)], new ItemStack(ingots, 1, j + (i * 16)));
 				CSCrafting.registerOre("dust" + allnames[j + (i * 16)], new ItemStack(dusts, 1, j + (i * 16)));
 				CSCrafting.registerOre("nugget" + allnames[j + (i * 16)], new ItemStack(nuggets, 1, j + (i * 16)));
 			}
 		}
 		
 		// Gems
 		for (int i = 0; i < gemids.length; i++)
 		{
 			CSCrafting.registerOre("gem" + allnames[gemids[i]], new ItemStack(gems, 1, gemids[i]));
 		}
 		
 		// Vanilla Special Ores
 		for (int i = 0; i < (vanillanames.length * 4); i++)
 		{
 			if (i < 7) // Nether Ores
 			{
 				CSCrafting.registerOre("oreNether" + vanillanames[i], new ItemStack(vanillaSpecialOres1, 1, i));
 				CSCrafting.registerOre("ingot" + vanillanames[i], new ItemStack(vanillaSpecialItems, 1, i));
 			}
 			else if (i < 14) // End Ores
 			{
 				CSCrafting.registerOre("oreEnd" + vanillanames[i - 7], new ItemStack(vanillaSpecialOres1, 1, i));
 				CSCrafting.registerOre("dust" + vanillanames[i - 7], new ItemStack(vanillaSpecialItems, 1, i));
 			}
 			else if (i < 21) // Dirt Ores
 			{
 				CSCrafting.registerOre("oreDirt" + vanillanames[i - 14], new ItemStack(vanillaSpecialOres2, 1, i - 14));
 				CSCrafting.registerOre("nugget" + vanillanames[i - 14], new ItemStack(vanillaSpecialItems, 1, i));
 			}
 			else if (i < 28) // Sand Ores
 				CSCrafting.registerOre("oreSand" + vanillanames[i - 21], new ItemStack(vanillaSpecialOres2, 1, i - 14));
 		}
 	}
 	
 	public void addCraftingRecipes()
 	{
 		GameRegistry.addRecipe(new RepairDataTools());
 		
 		GameRegistry.addShapedRecipe(new ItemStack(oreCrusher, 1, 3), new Object[] { "tct", "cTc", "tct", 't', new ItemStack(ingots, 1, CSArrays.indexOf(allnames, "Tin")), 'c', new ItemStack(ingots, 1, CSArrays.indexOf(allnames, "Chrome")), 'T', new ItemStack(ingots, 1, CSArrays.indexOf(allnames, "Titanium")), });
 		
 		for (int i = 0; i < 128; i++)
 		{
 			ItemStack is = (CSArrays.contains(gemids, i) ? new ItemStack(gems, 1, CSArrays.indexOf(gemids, i)) : new ItemStack(ingots, 1, i));
 			GameRegistry.addShapelessRecipe(new ItemStack(OreHelper.getOreFromMetadata(i, "storage"), 1, i % 16), is, is, is, is, is, is, is, is, is);
 			GameRegistry.addShapelessRecipe(is, new ItemStack(nuggets, 1, i), new ItemStack(nuggets, 1, i), new ItemStack(nuggets, 1, i), new ItemStack(nuggets, 1, i), new ItemStack(nuggets, 1, i), new ItemStack(nuggets, 1, i), new ItemStack(nuggets, 1, i), new ItemStack(nuggets, 1, i), new ItemStack(nuggets, 1, i));
 			GameRegistry.addShapelessRecipe(new ItemStack(nuggets, 9, i), is);
 			GameRegistry.addShapelessRecipe(new ItemStack(is.getItem(), 9, is.getItemDamage()), new ItemStack(OreHelper.getOreFromMetadata(i, "storage"), 1, i % 16));
 		}
 		for (int i = 0; i < vanillanames.length; i++)
 		{
 			Block storageBlock = i == 1 ? Block.blockDiamond : (i == 2 ? Block.blockEmerald : (i == 3 ? Block.blockGold : (i == 4 ? Block.blockIron : (i == 5 ? Block.blockLapis : i == 6 ? Block.blockRedstone : null))));
 			if (storageBlock != null)
 				GameRegistry.addShapelessRecipe(new ItemStack(storageBlock), new ItemStack(vanillaSpecialItems, 1, i), new ItemStack(vanillaSpecialItems, 1, i), new ItemStack(vanillaSpecialItems, 1, i), new ItemStack(vanillaSpecialItems, 1, i), new ItemStack(vanillaSpecialItems, 1, i), new ItemStack(vanillaSpecialItems, 1, i), new ItemStack(vanillaSpecialItems, 1, i), new ItemStack(vanillaSpecialItems, 1, i), new ItemStack(vanillaSpecialItems, 1, i));
 			GameRegistry.addShapelessRecipe(new ItemStack(vanillaSpecialItems, 1, i), new ItemStack(vanillaSpecialItems, 1, i + 14), new ItemStack(vanillaSpecialItems, 1, i + 14), new ItemStack(vanillaSpecialItems, 1, i + 14), new ItemStack(vanillaSpecialItems, 1, i + 14), new ItemStack(vanillaSpecialItems, 1, i + 14), new ItemStack(vanillaSpecialItems, 1, i + 14), new ItemStack(vanillaSpecialItems, 1, i + 14), new ItemStack(vanillaSpecialItems, 1, i + 14), new ItemStack(vanillaSpecialItems, 1, i + 14));
 			GameRegistry.addShapelessRecipe(new ItemStack(vanillaSpecialItems, 9, i + 14), new ItemStack(vanillaSpecialItems, 1, i));
 		}
 		GameRegistry.addShapelessRecipe(new ItemStack(vanillaSpecialItems, 9, 14), new ItemStack(Item.coal));
 		GameRegistry.addShapelessRecipe(new ItemStack(vanillaSpecialItems, 9, 15), new ItemStack(Item.diamond));
 		GameRegistry.addShapelessRecipe(new ItemStack(vanillaSpecialItems, 9, 16), new ItemStack(Item.emerald));
 		GameRegistry.addShapelessRecipe(new ItemStack(vanillaSpecialItems, 9, 18), new ItemStack(Item.ingotIron));
 		GameRegistry.addShapelessRecipe(new ItemStack(vanillaSpecialItems, 9, 19), new ItemStack(Item.dyePowder, 1, 4));
 		GameRegistry.addShapelessRecipe(new ItemStack(vanillaSpecialItems, 9, 20), new ItemStack(Item.redstone));
 		
 		addToolCraftingRecipes();
 	}
 	
 	public void addToolCraftingRecipes()
 	{
 		for (int i = 0; i < 5; i++)
 		{
 			Item tool = i == 0 ? dataSword : (i == 1 ? dataSpade : (i == 2 ? dataPickaxe : (i == 3 ? dataAxe : dataHoe)));
 			
 			ItemStack toolStack = new ItemStack(tool);
 			
 			CSCrafting.addToolRecipe(ItemDataTool.setToolMaterial(toolStack, adamantite), new ItemStack(ingots, 1, CSArrays.indexOf(allnames, "Adamantite")), i);
 			CSCrafting.addToolRecipe(ItemDataTool.setToolMaterial(toolStack, cobalt), new ItemStack(ingots, 1, CSArrays.indexOf(allnames, "Cobalt")), i);
 			CSCrafting.addToolRecipe(ItemDataTool.setToolMaterial(toolStack, demonite), new ItemStack(ingots, 1, CSArrays.indexOf(allnames, "Demonite")), i);
 			CSCrafting.addToolRecipe(ItemDataTool.setToolMaterial(toolStack, mythril), new ItemStack(ingots, 1, CSArrays.indexOf(allnames, "Mythril")), i);
 			CSCrafting.addToolRecipe(ItemDataTool.setToolMaterial(toolStack, aluminium), new ItemStack(ingots, 1, CSArrays.indexOf(allnames, "Aluminium")), i);
 			CSCrafting.addToolRecipe(ItemDataTool.setToolMaterial(toolStack, chrome), new ItemStack(ingots, 1, CSArrays.indexOf(allnames, "Chrome")), i);
 			CSCrafting.addToolRecipe(ItemDataTool.setToolMaterial(toolStack, copper), new ItemStack(ingots, 1, CSArrays.indexOf(allnames, "Copper")), i);
 			CSCrafting.addToolRecipe(ItemDataTool.setToolMaterial(toolStack, silver), new ItemStack(ingots, 1, CSArrays.indexOf(allnames, "Silver")), i);
 			CSCrafting.addToolRecipe(ItemDataTool.setToolMaterial(toolStack, tin), new ItemStack(ingots, 1, CSArrays.indexOf(allnames, "Tin")), i);
 			CSCrafting.addToolRecipe(ItemDataTool.setToolMaterial(toolStack, titanium), new ItemStack(ingots, 1, CSArrays.indexOf(allnames, "Titanium")), i);
 			CSCrafting.addToolRecipe(ItemDataTool.setToolMaterial(toolStack, emerald), new ItemStack(vanillaSpecialItems, 1, CSArrays.indexOf(vanillanames, "Emerald")), i);
 			CSCrafting.addToolRecipe(ItemDataTool.setToolMaterial(toolStack, ruby), new ItemStack(gems, 1, CSArrays.indexOf(gemnames, "Ruby")), i);
 			CSCrafting.addToolRecipe(ItemDataTool.setToolMaterial(toolStack, sapphire), new ItemStack(gems, 1, CSArrays.indexOf(gemnames, "Sapphire")), i);
 		}
 	}
 	
 	public void addFurnaceRecipes()
 	{
 		for (int i = 0; i < splitnames.length; i++)
 		{
 			for (int j = 0; j < splitnames[i].length; j++)
 			{
 				ItemStack is = (CSArrays.contains(gemids, (j + (i * 16))) ? new ItemStack(gems, 1, CSArrays.indexOf(gemids, j + (i * 16))) : new ItemStack(ingots, 1, j + (i * 16)));
 				ItemStack is2 = new ItemStack(is.getItem(), 2, is.getItemDamage());
 				
 				CSCrafting.addSmelting(new ItemStack(OreHelper.getOreFromMetadata(j + (i * 16), "stone"), 1, j), is, 0.1F);
 				CSCrafting.addSmelting(new ItemStack(OreHelper.getOreFromMetadata(j + (i * 16), "nether"), 1, j), is2, 0.2F);
 				CSCrafting.addSmelting(new ItemStack(OreHelper.getOreFromMetadata(j + (i * 16), "end"), 1, j), is2, 0.2F);
 				CSCrafting.addSmelting(new ItemStack(OreHelper.getOreFromMetadata(j + (i * 16), "dirt"), 1, j), new ItemStack(nuggets, 2, j + (i * 16)), 0.01F);
 				CSCrafting.addSmelting(new ItemStack(OreHelper.getOreFromMetadata(j + (i * 16), "sand"), 1, j), new ItemStack(nuggets, 3, j + (i * 16)), 0.015F);
 				
 				CSCrafting.addSmelting(new ItemStack(dusts, 1, j + (i * 16)), is, 0.001F);
 			}
 		}
 		for (int i = 0; i < (vanillanames.length * 4); i++)
 		{
 			if (i < 7) // Nether Ores
 			{
 				ItemStack is = i == 3 ? new ItemStack(Item.ingotGold, 2) : (i == 4 ? new ItemStack(Item.ingotIron, 2) : new ItemStack(vanillaSpecialItems, 2, i));
 				CSCrafting.addSmelting(new ItemStack(vanillaSpecialOres1, 1, i), is, 0.1F);
 				CSCrafting.addSmelting(new ItemStack(vanillaSpecialItems, 1, i + 7), is, 0.1F);
 			}
 			else if (i < 14) // End Ores
 			{
 				ItemStack is = i % 7 == 3 ? new ItemStack(Item.ingotGold, 2) : (i % 7 == 4 ? new ItemStack(Item.ingotIron, 2) : new ItemStack(vanillaSpecialItems, 2, i - 7));
 				CSCrafting.addSmelting(new ItemStack(vanillaSpecialOres1, 1, i), new ItemStack(vanillaSpecialItems, 2, i - 7), 0.1F);
 			}
 			else if (i < 21) // Dirt Ores
 			{
 				ItemStack is = i % 7 == 3 ? new ItemStack(Item.ingotGold, 2) : (i % 7 == 4 ? new ItemStack(Item.ingotIron, 2) : new ItemStack(vanillaSpecialItems, 2, i - 7));
 				CSCrafting.addSmelting(new ItemStack(vanillaSpecialOres2, 1, i - 14), new ItemStack(vanillaSpecialItems, 2, i), 0.01F);
 			}
 			else if (i < 28) // Sand Ores
 			{
 				ItemStack is = i % 7 == 3 ? new ItemStack(Item.goldNugget, 3) : new ItemStack(vanillaSpecialItems, 3, i - 7);
 				CSCrafting.addSmelting(new ItemStack(vanillaSpecialOres2, 1, i - 14), new ItemStack(vanillaSpecialItems, 3, i - 7), 0.015F);
 			}
 		}
 	}
 	
 	public void addCrusherRecipes()
 	{
 		for (int i = 0; i < splitnames.length; i++)
 		{
 			for (int j = 0; j < splitnames[i].length; j++)
 			{
 				ItemStack is = (CSArrays.contains(gemids, j + (i * 16)) ? new ItemStack(gems, 1, CSArrays.indexOf(gemids, j + (i * 16))) : new ItemStack(ingots, 1, j + (i * 16)));
 				
 				OreCrusherRecipes.crushing().addCrushing(OreHelper.getOreFromMetadata(j + (i * 16), "stone").blockID, j, new ItemStack(dusts, 2, j + (i * 16)), 0.1F);
 				OreCrusherRecipes.crushing().addCrushing(OreHelper.getOreFromMetadata(j + (i * 16), "nether").blockID, j, new ItemStack(dusts, 4, j + (i * 16)), 0.2F);
 				OreCrusherRecipes.crushing().addCrushing(OreHelper.getOreFromMetadata(j + (i * 16), "end").blockID, j, new ItemStack(dusts, 4, j + (i * 16)), 0.2F);
 				
 				OreCrusherRecipes.crushing().addCrushing(is.itemID, is.getItemDamage(), new ItemStack(dusts, 1, j + (i * 16)), 0F);
 			}
 		}
 		for (int i = 0; i < vanillanames.length; i++)
 		{
 			OreCrusherRecipes.crushing().addCrushing(vanillaSpecialItems.itemID, i, new ItemStack(vanillaSpecialItems, 1, i + 7), 0F);
 			
 			OreCrusherRecipes.crushing().addCrushing(vanillaSpecialOres1.blockID, i, new ItemStack(vanillaSpecialItems, 4, i + 7), 0.2F);
 			OreCrusherRecipes.crushing().addCrushing(vanillaSpecialOres1.blockID, i + 7, new ItemStack(vanillaSpecialItems, 4, i + 7), 0.2F);
 		}
 		for (int i = 0; i < gemids.length; i++)
 		{
 			OreCrusherRecipes.crushing().addCrushing(dusts.itemID, gemids[i], new ItemStack(gems, 1, i), 0F);
 		}
 		OreCrusherRecipes.crushing().addCrushing(new ItemStack(Block.oreCoal), new ItemStack(vanillaSpecialItems, 2, 7), 0F);
 		OreCrusherRecipes.crushing().addCrushing(new ItemStack(Block.oreDiamond), new ItemStack(vanillaSpecialItems, 2, 8), 0F);
 		OreCrusherRecipes.crushing().addCrushing(new ItemStack(Block.oreEmerald), new ItemStack(vanillaSpecialItems, 2, 9), 0F);
 		OreCrusherRecipes.crushing().addCrushing(new ItemStack(Block.oreGold), new ItemStack(vanillaSpecialItems, 2, 10), 0F);
 		OreCrusherRecipes.crushing().addCrushing(new ItemStack(Block.oreIron), new ItemStack(vanillaSpecialItems, 2, 11), 0F);
 		OreCrusherRecipes.crushing().addCrushing(new ItemStack(Block.oreLapis), new ItemStack(vanillaSpecialItems, 2, 12), 0F);
 		OreCrusherRecipes.crushing().addCrushing(new ItemStack(Block.oreRedstone), new ItemStack(vanillaSpecialItems, 2, 13), 0F);
 		OreCrusherRecipes.crushing().addCrushing(new ItemStack(Item.coal), new ItemStack(vanillaSpecialItems, 1, 7), 0F);
 		OreCrusherRecipes.crushing().addCrushing(new ItemStack(Item.diamond), new ItemStack(vanillaSpecialItems, 1, 8), 0F);
 		OreCrusherRecipes.crushing().addCrushing(new ItemStack(Item.emerald), new ItemStack(vanillaSpecialItems, 1, 9), 0F);
 		OreCrusherRecipes.crushing().addCrushing(new ItemStack(Item.ingotGold), new ItemStack(vanillaSpecialItems, 1, 10), 0F);
 		OreCrusherRecipes.crushing().addCrushing(new ItemStack(Item.ingotIron), new ItemStack(vanillaSpecialItems, 1, 11), 0F);
 		OreCrusherRecipes.crushing().addCrushing(new ItemStack(Item.dyePowder, 1, 4), new ItemStack(vanillaSpecialItems, 1, 12), 0F);
 		OreCrusherRecipes.crushing().addCrushing(new ItemStack(Item.redstone), new ItemStack(vanillaSpecialItems, 9, 13), 0F);
 	}
 	
 	public void registerBlocks()
 	{
 		GameRegistry.registerBlock(vanillaSpecialOres1, ItemCustomBlock.class, "MoreMineralsSpecialVanillaOres");
 		GameRegistry.registerBlock(vanillaSpecialOres2, ItemCustomBlock.class, "MoreMineralsSpecialVanillaOres2");
 		vanillaSpecialOres1.addNames();
 		vanillaSpecialOres2.addNames();
 		for (int i = 0; i < 8; i++)
 		{
 			GameRegistry.registerBlock(OreHelper.getOreFromMetadata(i * 16, "stone"), ItemCustomBlock.class, "MoreMineralsStoneOres" + i);
 			GameRegistry.registerBlock(OreHelper.getOreFromMetadata(i * 16, "nether"), ItemCustomBlock.class, "MoreMineralsNetherOres" + i);
 			GameRegistry.registerBlock(OreHelper.getOreFromMetadata(i * 16, "end"), ItemCustomBlock.class, "MoreMineralsEndOres" + i);
 			GameRegistry.registerBlock(OreHelper.getOreFromMetadata(i * 16, "dirt"), ItemCustomBlock.class, "MoreMineralsDirtOres" + i);
 			GameRegistry.registerBlock(OreHelper.getOreFromMetadata(i * 16, "sand"), ItemCustomBlock.class, "MoreMineralsSandOres" + i);
 			GameRegistry.registerBlock(OreHelper.getOreFromMetadata(i * 16, "storage"), ItemCustomBlock.class, "MoreMineralsStorageBlocks" + i);
 			OreHelper.getOreFromMetadata(i * 16, "stone").addNames();
 			OreHelper.getOreFromMetadata(i * 16, "nether").addNames();
 			OreHelper.getOreFromMetadata(i * 16, "end").addNames();
 			OreHelper.getOreFromMetadata(i * 16, "dirt").addNames();
 			OreHelper.getOreFromMetadata(i * 16, "sand").addNames();
 			OreHelper.getOreFromMetadata(i * 16, "storage").addNames();
 		}
 		GameRegistry.registerBlock(oreCrusher, ItemBlock.class, "MoreMineralsOreCrusher");
 		GameRegistry.registerBlock(oreCrusherActive, ItemBlock.class, "MoreMineralsOreCrusherActive");
 	}
 }
