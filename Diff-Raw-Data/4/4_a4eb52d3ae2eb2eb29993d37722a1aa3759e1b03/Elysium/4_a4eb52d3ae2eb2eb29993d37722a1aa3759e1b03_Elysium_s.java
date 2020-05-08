 package mods.elysium;
 
 import java.io.File;
 import java.util.Calendar;
 import java.util.Date;
 
import me.dawars.CraftingPillars.CraftingPillars;
 import mods.elysium.api.Plants;
 import mods.elysium.api.temperature.RangedTemperature;
 import mods.elysium.api.temperature.TemperatureManager;
 import mods.elysium.block.ElysianBlock;
 import mods.elysium.block.ElysianBlockCrystal;
 import mods.elysium.block.ElysianBlockDirt;
 import mods.elysium.block.ElysianBlockExpeller;
 import mods.elysium.block.ElysianBlockGastroShell;
 import mods.elysium.block.ElysianBlockGrass;
 import mods.elysium.block.ElysianBlockHeatable;
 import mods.elysium.block.ElysianBlockLeavesFostimber;
 import mods.elysium.block.ElysianBlockLeucosand;
 import mods.elysium.block.ElysianBlockLogFostimber;
 import mods.elysium.block.ElysianBlockOre;
 import mods.elysium.block.ElysianBlockPalestone;
 import mods.elysium.block.ElysianBlockPalestonePillar;
 import mods.elysium.block.ElysianBlockRilt;
 import mods.elysium.block.ElysianBlockSaplingFostimber;
 import mods.elysium.block.ElysianBlockShell;
 import mods.elysium.block.ElysianBlockTallgrass;
 import mods.elysium.block.ElysianWaterBlock;
 import mods.elysium.block.ElysiumFlowerBlock;
 import mods.elysium.dimension.ElysiumWorldProvider;
 import mods.elysium.dimension.biome.ElysiumBiomeGenOcean;
 import mods.elysium.dimension.biome.ElysiumBiomeGenPlain;
 import mods.elysium.dimension.gen.WorldGenElysium;
 import mods.elysium.dimension.portal.ElysianBlockPortalCore;
 import mods.elysium.dimension.portal.ElysianTeleporter;
 import mods.elysium.dimension.portal.ElysianTileEntityPortal;
 import mods.elysium.entity.EntityCatorPillar;
 import mods.elysium.entity.EntityGerbil;
 import mods.elysium.fluids.ElysianWaterFluid;
 import mods.elysium.handlers.ElysianBonemealHandler;
 import mods.elysium.handlers.ElysianCreatureSpawnHandler;
 import mods.elysium.handlers.ElysianFuelHandler;
 import mods.elysium.item.ElysianItem;
 import mods.elysium.item.ElysianItemAxe;
 import mods.elysium.item.ElysianItemDebug;
 import mods.elysium.item.ElysianItemGracePrism;
 import mods.elysium.item.ElysianItemHoe;
 import mods.elysium.item.ElysianItemOverkill;
 import mods.elysium.item.ElysianItemPickaxe;
 import mods.elysium.item.ElysianItemShovel;
 import mods.elysium.item.ElysianItemSword;
 import mods.elysium.item.ElysianItemWhistle;
 import mods.elysium.network.PacketHandler;
 import mods.elysium.proxy.CommonProxy;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraftforge.common.DimensionManager;
 import net.minecraftforge.common.EnumHelper;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.common.Property;
 import net.minecraftforge.fluids.Fluid;
 import net.minecraftforge.fluids.FluidRegistry;
 import net.minecraftforge.oredict.OreDictionary;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.EntityRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 //import mods.elysium.client.gui.menu.ElysianMenu;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
@Mod(name = Elysium.name, version = Elysium.version, useMetadata = false, modid = Elysium.id, dependencies="required-after:Forge@[9.10.0,),GalacticraftCore")
 @NetworkMod(channels = {DefaultProps.NET_CHANNEL_NAME}, packetHandler = PacketHandler.class, clientSideRequired = true, serverSideRequired = true)
 public class Elysium
 {
 
 	@Instance(Elysium.id)
 	private static Elysium instance;
 	
 	public static Elysium getInstance()
 	{
 		return instance;
 	}
 	
 	@SidedProxy(clientSide = "mods.elysium.proxy.ClientProxy", serverSide = "mods.elysium.proxy.CommonProxy")
 	public static CommonProxy proxy;
 	
 	public static ElysianConfiguration config;
 	public static final CreativeTabs tabElysium = new CreativeTabs("CraftingPillars")
 	{
 		@SideOnly(Side.CLIENT)
 		public int getTabIconItemIndex()
 		{
 			return Elysium.blockPalestone.blockID;
 		}
 	};
 	
 	public static final String version = "1.0.0";
 	public static final String name = "The Elysium";
 	public static final String id = "TheElysium";
 	public static final String consolePrefix = "[Elysium] ";
 	public static boolean isAprilFools;
 	
 	
 	
 	/** Dimension ID **/
 	public static int DimensionID;
 	public static int MaxDragon;
 
 	//Rendering ids
 	public static int fancyWorkbenchRenderID;
 	public static int fancyTankRenderID;
 	public static int crystalBlockRenderID;
 
 	//Fluids
 	public static Fluid elysianWaterFluid;
 
 	
 	//Blocks
 	public static Block blockPalestone;
 	/** It means elysian dirt. **/
 	public static Block blockDirt;
 	/** It means elysian grass. **/
 	public static Block blockGrass;
 	public static Block blockLeucosand;
 	public static Block blockRilt;
 	public static Block blockLogFostimber;
 	public static Block blockLeavesFostimber;
 	public static Block blockGastroShell;
 	public static Block blockSaplingFostimber;
 	public static Block blockPlanksFostimber;
 	public static Block blockFlowerAsphodel;
 	/** It means elysian grass overlay. **/
 	public static Block blockTallGrass;
 	
 	public static Block oreSulphure;
 	public static Block oreCobalt;
 	public static Block oreIridium;
 	public static Block oreSilicon;
 	public static Block oreJade;
 	public static Block oreTourmaline;
 	public static Block oreBeryl;
 	
 	public static Block elysianWater;
 	
 	public static Block blockFloatingShell;
 	public static Block blockFloatingConch;
 	
 	public static Block blockCobblePalestone;
 	public static Block blockCobblePalestoneMossy;
 	
 	public static Block blockPalestoneBrick;
 	public static Block blockPalestoneBrickCracked;
 	public static Block blockPalestoneBrickMossy;
 	public static Block blockPalestonePillar;
 	public static Block blockPalestoneBrickChiseld;
 	
 	public static Block blockPortalCore;
 	
 	public static Block expeller;
 	
 	public static Block blockSulphure;
 	public static Block blockCobalt;
 	public static Block blockIridium;
 	public static Block blockSilicon;
 	public static Block blockJade;
 	public static Block blockTourmaline;
 	public static Block blockBeryl;
 	
 	public static Block blockFancyWorkbench;
 	public static Block blockFancyTank;
 	
 	public static Block blockCrystal;
 	
 	//Items
 	
 	public static Item itemGracePrism;
 	public static Item itemWhistle;
 	
 	public static Item itemSeedsPepper;
 	public static Item itemAsphodelPetals;
 	
 	public static Item itemBeryl;
 	public static Item itemIngotCobalt;
 	public static Item itemIngotIridium;
 	public static Item itemJade;
 	public static Item itemSiliconChunk;
 	public static Item itemSturdyHide;
 	public static Item itemSulphur;
 	public static Item itemTourmaline;
 	
 	public static Item itemSwordFostimber;
 	public static Item itemPickaxeFostimber;
 	public static Item itemAxeFostimber;
 	public static Item itemShovelFostimber;
 	public static Item itemHoeFostimber;
 	
 	public static Item itemSwordPalestone;
 	public static Item itemPickaxePalestone;
 	public static Item itemAxePalestone;
 	public static Item itemSpadePalestone;
 	public static Item itemHoePalestone;
 
 	public static Item itemOverKill;
 	public static Item itemDebug;
 	
 	
 	/** Biome's **/
 	public static BiomeGenBase biomePlain = null;
 	public static BiomeGenBase biomeOcean = null;
 	
 
 	@EventHandler
 	public void load(FMLPreInitializationEvent evt)
 	{
 //		GalacticraftRegistry.registerTeleportType(ElysiumWorldProvider.class, new ElysianTeleporter()));
 //		GalacticraftRegistry.registerCelestialBody(new ElysiumPlanet());
 		
 		elysianWaterFluid = new ElysianWaterFluid("Elysian Water");
 		 
 		Calendar calendar = Calendar.getInstance();
 		calendar.setTime(new Date());
 		isAprilFools = (calendar.get(2)+1 == 4) && (calendar.get(5) == 1);
 		
 		config = new ElysianConfiguration(new File(evt.getModConfigurationDirectory(), "Elysium.cfg"));
 		try
 		{
 			config.load();
 			
 			Property idDim = Elysium.config.get("other", "dimensionID", DefaultProps.DimensionID, "This is the id of the dimension change if needed!");
 			DimensionID = idDim.getInt();
 			Property MAX_DRAGON_IN_END = Elysium.config.get("other", "MAX_DRAGON_IN_END", DefaultProps.MAX_DRAGON_IN_END, "How many dragons can be spawned to the End at the same time!");
 			MaxDragon = MAX_DRAGON_IN_END.getInt();
 			
 			//Handlers
 			MinecraftForge.EVENT_BUS.register(new ElysianBonemealHandler());
 			MinecraftForge.EVENT_BUS.register(new ElysianCreatureSpawnHandler());
 			GameRegistry.registerFuelHandler(new ElysianFuelHandler());
 			
 			
 			
 			//Block Registering
 			
 			Property idPalestoneBlock = Elysium.config.getTerrainBlock("terrainGen", "palestone.id", DefaultProps.idPalestoneBlock, null);
 			blockPalestone = (new ElysianBlockPalestone(idPalestoneBlock.getInt(), Material.rock)).setHardness(1.5F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("palestone");
 			registerBlock(blockPalestone, "Palestone");
 			
 			Property idSoilBlock = Elysium.config.getTerrainBlock("terrainGen", "ElysiumDirt.id", DefaultProps.idSoilBlock, null);
 			blockDirt = (new ElysianBlockDirt(idSoilBlock.getInt(), Material.ground)).setHardness(0.5F).setStepSound(Block.soundGravelFootstep).setUnlocalizedName("elysium_dirt");
 			registerBlock(blockDirt, "Elysian Soil");
 
 			Property idGrassBlock = Elysium.config.getTerrainBlock("terrainGen", "ElysiumGrass.id", DefaultProps.idGrassBlock, null);
 			blockGrass = (new ElysianBlockGrass(idGrassBlock.getInt(), Material.ground)).setHardness(0.6F).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("elysium_grass");
 			registerBlock(blockGrass, "Elysian Grass");
 
 			Property idLeucosandBlock = Elysium.config.getTerrainBlock("terrainGen", "Leucogrit.id", DefaultProps.idLeucosandBlock, null);
 			blockLeucosand = (new ElysianBlockLeucosand(idLeucosandBlock.getInt(), Material.sand)).setHardness(0.5F).setStepSound(Block.soundSandFootstep).setUnlocalizedName("elysium_sand");
 			registerBlock(blockLeucosand, "Leucosand");
 
 			Property idRiltBlock = Elysium.config.getTerrainBlock("terrainGen", "Rilt.id", DefaultProps.idRiltBlock, null);
 			blockRilt = (new ElysianBlockRilt(idRiltBlock.getInt(), Material.sand)).setHardness(0.6F).setStepSound(Block.soundGravelFootstep).setUnlocalizedName("rilt");
 			registerBlock(blockRilt, "Rilt Block");
 
 			Property idFostimberSaplingBlock = Elysium.config.getBlock("FostimberSaplingBlock.id", DefaultProps.idFostimberSaplingBlock);
 			blockSaplingFostimber = (new ElysianBlockSaplingFostimber(idFostimberSaplingBlock.getInt())).setHardness(0F).setUnlocalizedName("fostimber_sapling");
 			registerBlock(blockSaplingFostimber, "Fostimber Sapling");
 			
 			Property idFostimberLogBlock = Elysium.config.getBlock("FostimberLog.id", DefaultProps.idFostimberLogBlock);
 			blockLogFostimber = (new ElysianBlockLogFostimber(idFostimberLogBlock.getInt(), Material.wood)).setHardness(2.0F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("fostimber_log");
 			registerBlock(blockLogFostimber, "Fostimber Log");
 
 			Property idFostimberLeavesBlock = Elysium.config.getBlock("FostimberLeavesBlock.id", DefaultProps.idFostimberLeavesBlock);
 			blockLeavesFostimber = (new ElysianBlockLeavesFostimber(idFostimberLeavesBlock.getInt(), Material.leaves)).setLightOpacity(1).setHardness(0.2F).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("fostimber_leaves");
 			registerBlock(blockLeavesFostimber, "Fostimber Leaves");
 
 			Property idWoodBlock = Elysium.config.getBlock("idWoodBlock.id", DefaultProps.idWoodBlock);
 			blockPlanksFostimber = (new ElysianBlock(idWoodBlock.getInt(), Material.wood)).setHardness(0.2F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("fostimber_planks");
 			registerBlock(blockPlanksFostimber, "Wooden Planks");
 			
 			Property idGastroShellBlock = Elysium.config.getBlock("idGastroShellBlock.id", DefaultProps.idGastroShellBlock);
 			blockGastroShell = (new ElysianBlockGastroShell(idGastroShellBlock.getInt(), Material.rock)).setHardness(0.2F).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("gastroshell");
 			registerBlock(blockGastroShell, "Gastro Shell");
 
 			Property idAsphodelFlowerBlock = Elysium.config.getBlock("idAsphodelFlowerBlock.id", DefaultProps.idAsphodelFlowerBlock);
 			blockFlowerAsphodel = (new ElysiumFlowerBlock(idAsphodelFlowerBlock.getInt())).setHardness(0.0F).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("asphodel_flower");
 			registerBlock(blockFlowerAsphodel, "Asphodel Flower");
 
 			Property idCurlgrassBlock = Elysium.config.getBlock("idCurlgrassBlock.id", DefaultProps.idCurlgrassBlock);
 			blockTallGrass = new ElysianBlockTallgrass(idCurlgrassBlock.getInt()).setHardness(0.0F).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("curlgrass");
 			registerBlock(blockTallGrass, "Curlgrass");
 
 			Property idOreSulphurBlock = Elysium.config.getBlock("idOreSulphurBlock.id", DefaultProps.idOreSulphurBlock);
 			oreSulphure = new ElysianBlockOre(idOreSulphurBlock.getInt()).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreSulphur");
 			registerBlock(oreSulphure, "Sulphur Ore");
 
 			Property idOreBerylBlock = Elysium.config.getBlock("idOreBerylBlock.id", DefaultProps.idOreBerylBlock);
 			oreBeryl = new ElysianBlockOre(idOreBerylBlock.getInt()).setHardness(3.0F).setResistance(5.0F).setLightValue(0.5F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreBeryl");
 			registerBlock(oreBeryl, "Beryl Ore");
 			
 			Property idOreCobaltBlock = Elysium.config.getBlock("idOreCobaltBlock.id", DefaultProps.idOreCobaltBlock);
 			oreCobalt = new ElysianBlockOre(idOreCobaltBlock.getInt()).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreCobalt");
 			registerBlock(oreCobalt, "Cobalt Ore");
 
 			Property idOreIridiumBlock = Elysium.config.getBlock("idOreIridiumBlock.id", DefaultProps.idOreIridiumBlock);
 			oreIridium = new ElysianBlockOre(idOreIridiumBlock.getInt()).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreIridium");
 			registerBlock(oreIridium, "Iridium Ore");
 			
 			Property idOreSiliconBlock = Elysium.config.getBlock("idOreSiliconBlock.id", DefaultProps.idOreSiliconBlock);
 			oreSilicon = new ElysianBlockOre(idOreSiliconBlock.getInt()).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreSilicon");
 			registerBlock(oreSilicon, "Silicon Ore");
 			
 			Property idOreJadeBlock = Elysium.config.getBlock("idOreJadeBlock.id", DefaultProps.idOreJadeBlock);
 			oreJade = new ElysianBlockOre(idOreJadeBlock.getInt()).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreJade");
 			registerBlock(oreJade, "Jade Ore");
 
 			Property idOreTourmalineBlock = Elysium.config.getBlock("idOreTourmalineBlock.id", DefaultProps.idOreTourmalineBlock);
 			oreTourmaline = new ElysianBlockOre(idOreTourmalineBlock.getInt()).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreTourmaline");
 			registerBlock(oreTourmaline, "Tourmaline Ore");
 			
 			Property idWaterBlock = Elysium.config.getTerrainBlock("terrainGen", "idWaterBlock.id", DefaultProps.idWaterBlock, null);
 			elysianWater = new ElysianWaterBlock(idWaterBlock.getInt()).setHardness(100.0F).setLightOpacity(3).setUnlocalizedName("elysian_water");
 			registerBlock(elysianWater, "Elysium Water");
 
 			Property idPortalCoreBlock = Elysium.config.getBlock("idPortalCoreBlock.id", DefaultProps.idPortalCoreBlock);
 			blockPortalCore = new ElysianBlockPortalCore(idPortalCoreBlock.getInt(), Material.glass).setHardness(5F).setStepSound(Block.soundGlassFootstep).setUnlocalizedName("portalCore");
 			registerBlock(blockPortalCore, "Elysian Portal Block");
 
 			Property idShellsBlock = Elysium.config.getBlock("idShellsBlock.id", DefaultProps.idShellsBlock);
 			blockFloatingShell = new ElysianBlockShell(idShellsBlock.getInt()).setHardness(0.0F).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("shell");
 			registerBlock(blockFloatingShell, "Shell");
 
 			Property idConchBlock = Elysium.config.getBlock("idConchBlock.id", DefaultProps.idConchBlock);
 			blockFloatingConch = new ElysianBlockShell(idConchBlock.getInt()).setHardness(0.0F).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("conch");
 			registerBlock(blockFloatingConch, "Conch");
 			
 			Property idPaleCobblestoneBlock = Elysium.config.getBlock("idPaleCobblestoneBlock.id", DefaultProps.idPaleCobblestone);
 			blockCobblePalestone = (new ElysianBlock(idPaleCobblestoneBlock.getInt(), Material.rock)).setHardness(1.0F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("palestone_cobble");
 			registerBlock(blockCobblePalestone, "Cobble Palestone");
 			
 			Property idPaleCobblestoneMossyBlock = Elysium.config.getBlock("idPaleCobblestoneMossyBlock.id", DefaultProps.idPaleCobblestoneMossy);
 			blockCobblePalestoneMossy = (new ElysianBlockHeatable(idPaleCobblestoneMossyBlock.getInt(), Material.rock, -273, 300)).setHardness(1.0F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("palestone_cobble_mossy");
 			registerBlock(blockCobblePalestoneMossy, "Mossy Cobble Palestone");
 			
 			Property idPalestoneBrickBlock = Elysium.config.getBlock("idPalestoneBrickBlock.id", DefaultProps.idPalestoneBrick);
 			blockPalestoneBrick = (new ElysianBlockHeatable(idPalestoneBrickBlock.getInt(), Material.rock, -273, 300)).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("palestone_brick");
 			registerBlock(blockPalestoneBrick, "Palestone Brick");
 			
 			Property idPalestoneBrickCrackedBlock = Elysium.config.getBlock("idPalestoneBrickCrackedBlock.id", DefaultProps.idPalestoneBrickCracked);
 			blockPalestoneBrickCracked = (new ElysianBlockHeatable(idPalestoneBrickCrackedBlock.getInt(), Material.rock, -273, 300)).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("palestone_brick_cracked");
 			registerBlock(blockPalestoneBrickCracked, "Cracked Palestone Brick");
 			
 			Property idPalestoneBrickMossyBlock = Elysium.config.getBlock("idPalestoneBrickMossyBlock.id", DefaultProps.idPalestoneBrickMossy);
 			blockPalestoneBrickMossy = (new ElysianBlockHeatable(idPalestoneBrickMossyBlock.getInt(), Material.rock, -273, 300)).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("palestone_brick_mossy");
 			registerBlock(blockPalestoneBrickMossy, "Mossy Palestone Brick");
 			
 			Property idPalestoneChiseldBrickBlock = Elysium.config.getBlock("idPalestoneChiseldBrickBlock.id", DefaultProps.idPalestoneBrickChiseld);
 			blockPalestoneBrickChiseld = (new ElysianBlockHeatable(idPalestoneChiseldBrickBlock.getInt(), Material.rock, -273, 300)).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("palestone_brick_chiseld");
 			registerBlock(blockPalestoneBrickChiseld, "Chiseld Palestone Brick");
 			
 			Property idPalestonePillarBlock = Elysium.config.getBlock("idPalestonePillarBlock.id", DefaultProps.idPalestonePillar);
 			blockPalestonePillar = (new ElysianBlockPalestonePillar(idPalestonePillarBlock.getInt(), Material.rock)).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("palestone_pillar");
 			registerBlock(blockPalestonePillar, "Palestone Pillar");
 			
 			Block.dragonEgg.setCreativeTab(tabElysium);
 			
 			Property idExpeller = Elysium.config.getBlock("idExpeller.id", DefaultProps.idExpeller);
 			expeller = new ElysianBlockExpeller(idExpeller.getInt(), Material.iron).setUnlocalizedName("expeller");
 			registerBlock(expeller, "Expeller");
 			
 			
 			
 			Property idBlockSulphurBlock = Elysium.config.getBlock("idBlockSulphurBlock.id", DefaultProps.idBlockSulphur);
 			blockSulphure = new ElysianBlock(idBlockSulphurBlock.getInt(), Material.rock).setHardness(3F).setResistance(5F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("blockSulphur");
 			registerBlock(blockSulphure, "Sulphur Block");
 
 			Property idBlockBerylBlock = Elysium.config.getBlock("idBlockBerylBlock.id", DefaultProps.idBlockBeryl);
 			blockBeryl = new ElysianBlock(idBlockBerylBlock.getInt(), Material.iron).setHardness(3F).setResistance(5F).setLightValue(0.5F).setStepSound(Block.soundMetalFootstep).setUnlocalizedName("blockBeryl");
 			registerBlock(blockBeryl, "Beryl Block");
 			
 			Property idBlockCobalt = Elysium.config.getBlock("idBlockCobalt.id", DefaultProps.idBlockCobalt);
 			blockCobalt = new ElysianBlock(idBlockCobalt.getInt(), Material.iron).setHardness(3F).setResistance(5F).setStepSound(Block.soundMetalFootstep).setUnlocalizedName("blockCobalt");
 			registerBlock(blockCobalt, "Cobalt Block");
 
 			Property idBlockIridium = Elysium.config.getBlock("idBlockIridium.id", DefaultProps.idBlockIridium);
 			blockIridium = new ElysianBlock(idBlockIridium.getInt(), Material.iron).setHardness(3F).setResistance(5F).setStepSound(Block.soundMetalFootstep).setUnlocalizedName("blockIridium");
 			registerBlock(blockIridium, "Iridium Block");
 			
 			Property idBlockSilicon = Elysium.config.getBlock("idBlockSilicon.id", DefaultProps.idBlockSilicon);
 			blockSilicon = new ElysianBlock(idBlockSilicon.getInt(), Material.iron).setHardness(3F).setResistance(5F).setStepSound(Block.soundMetalFootstep).setUnlocalizedName("blockSilicon");
 			registerBlock(blockSilicon, "Silicon Block");
 			
 			Property idBlockJade = Elysium.config.getBlock("idBlockJade.id", DefaultProps.idBlockJade);
 			blockJade = new ElysianBlock(idBlockJade.getInt(), Material.rock).setHardness(3F).setResistance(5F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("blockJade");
 			registerBlock(blockJade, "Jade Block");
 
 			Property idBlockTourmaline = Elysium.config.getBlock("idBlockTourmaline.id", DefaultProps.idBlockTourmaline);
 			blockTourmaline = new ElysianBlock(idBlockTourmaline.getInt(), Material.iron).setHardness(3F).setResistance(5F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("blockTourmaline");
 			registerBlock(blockTourmaline, "Tourmaline Block");
 
 			Property idCrystalBlock = Elysium.config.getBlock("idCrystalBlock.id", DefaultProps.idBlockCrystal);
 			blockCrystal = new ElysianBlockCrystal(idCrystalBlock.getInt()).setHardness(2F).setResistance(5F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("crystalBlock");
 			registerBlock(blockCrystal, "Crystal block");
 
 			
 			
 			
 			//Item Registering
 			
 			Property idGracePrismItem = Elysium.config.getItem("idGracePrismItem.id", DefaultProps.idGracePrismItem);
 			itemGracePrism = new ElysianItemGracePrism(idGracePrismItem.getInt()).setUnlocalizedName("gracecrystal");
 			LanguageRegistry.addName(itemGracePrism, "Grace Prism");
 
 			Property idWhistleItem = Elysium.config.getItem("idWhistleItem.id", DefaultProps.idWhistleItem);
 			itemWhistle = new ElysianItemWhistle(idWhistleItem.getInt()).setUnlocalizedName("enderflute");
 			LanguageRegistry.addName(itemWhistle, "Ender Flute");
 		
 			Property idPepperSeedItem = Elysium.config.getItem("idPepperSeedItem.id", DefaultProps.idPepperSeedItem);
 			itemSeedsPepper = new ElysianItem(idPepperSeedItem.getInt()).setUnlocalizedName("seeds_pepper");
 			LanguageRegistry.addName(itemSeedsPepper, "Pepper Seed");
 
 			Property idOverkillItem = Elysium.config.getItem("idOverkillItem.id", DefaultProps.idOverkillItem);
 			itemOverKill = new ElysianItemOverkill(idOverkillItem.getInt()).setUnlocalizedName("overkill");
 			LanguageRegistry.addName(itemOverKill, "Overkill Item");
 		
 			Property idAsphodelPetalsItem = Elysium.config.getItem("idAsphodelPetalsItem.id", DefaultProps.idAsphodelPetalsItem);
 			itemAsphodelPetals = new ElysianItem(idAsphodelPetalsItem.getInt()).setUnlocalizedName("asphodelpetal");
 			LanguageRegistry.addName(itemAsphodelPetals, "Asphodel Petals");
 			
 			Property idDebugItem = Elysium.config.getItem("idDebugItem.id", DefaultProps.idDebugItem);
 			itemDebug = new ElysianItemDebug(idDebugItem.getInt()).setUnlocalizedName("debug");
 			LanguageRegistry.addName(itemDebug, "Modder Item");
 			
 			Property idBerylItem = Elysium.config.getItem("idBerylItem.id", DefaultProps.idBerylItem);
 			itemBeryl = new ElysianItem(idBerylItem.getInt()).setUnlocalizedName("beryl");
 			LanguageRegistry.addName(itemBeryl, "Beryl");
 			
 			Property idCobaltIngotItem = Elysium.config.getItem("idCobaltIngotItem.id", DefaultProps.idCobaltIngotItem);
 			itemIngotCobalt = new ElysianItem(idCobaltIngotItem.getInt()).setUnlocalizedName("ingotCobalt");
 			LanguageRegistry.addName(itemIngotCobalt, "Cobalt Ingot");
 			
 			Property idIridiumIngotItem = Elysium.config.getItem("idIridiumIngotItem.id", DefaultProps.idIridiumIngotItem);
 			itemIngotIridium = new ElysianItem(idIridiumIngotItem.getInt()).setUnlocalizedName("ingotIridium");
 			LanguageRegistry.addName(itemIngotIridium, "Iridium Ingot");
 			
 			Property idJadeItem = Elysium.config.getItem("idJadeItem.id", DefaultProps.idJadeItem);
 			itemJade = new ElysianItem(idJadeItem.getInt()).setUnlocalizedName("jade");
 			LanguageRegistry.addName(itemJade, "Jade");
 			
 			Property idSiliconChunk = Elysium.config.getItem("idSiliconChunk.id", DefaultProps.idSiliconChunk);
 			itemSiliconChunk = new ElysianItem(idSiliconChunk.getInt()).setUnlocalizedName("siliconchunk");
 			LanguageRegistry.addName(itemSiliconChunk, "Silicon Chunk");
 
 			Property idSulphurItem = Elysium.config.getItem("idSulphurItem.id", DefaultProps.idSulphurItem);
 			itemSulphur = new ElysianItem(idSulphurItem.getInt()).setUnlocalizedName("sulphur");
 			LanguageRegistry.addName(itemSulphur, "Sulphur");
 
 			Property idTourmalineItem = Elysium.config.getItem("idTourmalineItem.id", DefaultProps.idTourmalineItem);
 			itemTourmaline = new ElysianItem(idTourmalineItem.getInt()).setUnlocalizedName("tourmaline");
 			LanguageRegistry.addName(itemTourmaline, "Tourmaline");
 			
 			Property idSturdyHideItem = Elysium.config.getItem("idSturdyHideItem.id", DefaultProps.idSturdyHideItem);
 			itemSturdyHide = new ElysianItem(idSturdyHideItem.getInt()).setUnlocalizedName("sturdyHide");
 			LanguageRegistry.addName(itemSturdyHide, "Sturdy Hide");
 			
 			
 			
 			//Tool Registering
 			
 			EnumToolMaterial FOSTIMBER_MAT = EnumHelper.addToolMaterial("FOSTIMBER", 0, 59, 2.0F, 0, 15);
 
 			Property idWoodSwordItem = Elysium.config.getItem("idWoodSwordItem.id", DefaultProps.idWoodSwordItem);
 			itemSwordFostimber = new ElysianItemSword(idWoodSwordItem.getInt(), FOSTIMBER_MAT).setUnlocalizedName("swordFostimber");
 			LanguageRegistry.addName(itemSwordFostimber, "Fostimber Sword");
 
 			Property idWoodPickaxeItem = Elysium.config.getItem("idWoodPickaxeItem.id", DefaultProps.idWoodPickaxeItem);
 			itemPickaxeFostimber = new ElysianItemPickaxe(idWoodPickaxeItem.getInt(), FOSTIMBER_MAT).setUnlocalizedName("pickaxeFostimber");
 			LanguageRegistry.addName(itemPickaxeFostimber, "Fostimber Pickaxe");
 
 			Property idWoodAxeItem = Elysium.config.getItem("idWoodAxeItem.id", DefaultProps.idWoodAxeItem);
 			itemAxeFostimber = new ElysianItemAxe(idWoodAxeItem.getInt(), FOSTIMBER_MAT).setUnlocalizedName("axeFostimber");
 			LanguageRegistry.addName(itemAxeFostimber, "Fostimber Axe");
 
 			Property idWoodShovelItem = Elysium.config.getItem("idWoodShovelItem.id", DefaultProps.idWoodShovelItem);
 			itemShovelFostimber = new ElysianItemShovel(idWoodShovelItem.getInt(), FOSTIMBER_MAT).setUnlocalizedName("shovelFostimber");
 			LanguageRegistry.addName(itemShovelFostimber, "Fostimber Shovel");
 			
 			Property idWoodHoeItem = Elysium.config.getItem("idWoodHoeItem.id", DefaultProps.idWoodHoeItem);
 			itemHoeFostimber = new ElysianItemHoe(idWoodHoeItem.getInt(), FOSTIMBER_MAT).setUnlocalizedName("hoeFostimber");
 			LanguageRegistry.addName(itemHoeFostimber, "Fostimber Hoe");
 
 			EnumToolMaterial STONE_MAT = EnumHelper.addToolMaterial("PALESTONE", 1, 131, 4.0F, 1, 5);
 
 			Property idStoneSwordItem = Elysium.config.getItem("idStoneSwordItem.id", DefaultProps.idStoneSwordItem);
 			itemSwordPalestone = new ElysianItemSword(idStoneSwordItem.getInt(), STONE_MAT).setUnlocalizedName("swordPalestone");
 			LanguageRegistry.addName(itemSwordPalestone, "Palestone Sword");
 
 			Property idStonePickaxeItem = Elysium.config.getItem("idStonePickaxeItem.id", DefaultProps.idStonePickaxeItem);
 			itemPickaxePalestone = new ElysianItemPickaxe(idStonePickaxeItem.getInt(), STONE_MAT).setUnlocalizedName("pickaxePalestone");
 			LanguageRegistry.addName(itemPickaxePalestone, "Palestone Pickaxe");
 
 			Property idStoneAxeItem = Elysium.config.getItem("idStoneAxeItem.id", DefaultProps.idStoneAxeItem);
 			itemAxePalestone = new ElysianItemAxe(idStoneAxeItem.getInt(), STONE_MAT).setUnlocalizedName("axePalestone");
 			LanguageRegistry.addName(itemAxePalestone, "Palestone Axe");
 
 			Property idStoneShovelItem = Elysium.config.getItem("idStoneShovelItem.id", DefaultProps.idStoneShovelItem);
 			itemSpadePalestone = new ElysianItemShovel(idStoneShovelItem.getInt(), STONE_MAT).setUnlocalizedName("shovelPalestone");
 			LanguageRegistry.addName(itemSpadePalestone, "Palestone Shovel");
 			
 			Property idStoneHoeItem = Elysium.config.getItem("idStoneHoeItem.id", DefaultProps.idStoneHoeItem);
 			itemHoePalestone = new ElysianItemHoe(idStoneHoeItem.getInt(), STONE_MAT).setUnlocalizedName("hoePalestone");
 			LanguageRegistry.addName(itemHoePalestone, "Palestone Hoe");
 			
 			MinecraftForge.setToolClass(itemPickaxeFostimber, "pickaxe", 0);
 	        MinecraftForge.setToolClass(itemAxeFostimber, "axe", 0);
 	        MinecraftForge.setToolClass(itemShovelFostimber, "shovel", 0);
 	        MinecraftForge.setToolClass(itemPickaxePalestone, "pickaxe", 1);
 	        MinecraftForge.setToolClass(itemAxePalestone, "axe", 1);
 	        MinecraftForge.setToolClass(itemSpadePalestone, "shovel", 1);
 
 			MinecraftForge.setBlockHarvestLevel(oreSulphure, "pickaxe", 0);
 			MinecraftForge.setBlockHarvestLevel(oreCobalt, "pickaxe", 1);
 			MinecraftForge.setBlockHarvestLevel(oreSilicon, "pickaxe", 2);
 			MinecraftForge.setBlockHarvestLevel(oreIridium, "pickaxe", 2);
 			MinecraftForge.setBlockHarvestLevel(oreJade, "pickaxe", 2);
 			MinecraftForge.setBlockHarvestLevel(oreBeryl, "pickaxe", 2);
 			MinecraftForge.setBlockHarvestLevel(oreTourmaline, "pickaxe", 3);
 			
 			MinecraftForge.setBlockHarvestLevel(blockFancyWorkbench, "pickaxe", 0);
 
 			MinecraftForge.setBlockHarvestLevel(blockSulphure, "pickaxe", 0);
 			MinecraftForge.setBlockHarvestLevel(blockCobalt, "pickaxe", 1);
 			MinecraftForge.setBlockHarvestLevel(blockSilicon, "pickaxe", 2);
 			MinecraftForge.setBlockHarvestLevel(blockIridium, "pickaxe", 2);
 			MinecraftForge.setBlockHarvestLevel(blockJade, "pickaxe", 2);
 			MinecraftForge.setBlockHarvestLevel(blockBeryl, "pickaxe", 2);
 			MinecraftForge.setBlockHarvestLevel(blockTourmaline, "pickaxe", 3);
 			
 			MinecraftForge.setBlockHarvestLevel(blockGrass, "shovel", 0);
 			MinecraftForge.setBlockHarvestLevel(blockDirt, "shovel", 0);
 			MinecraftForge.setBlockHarvestLevel(blockLogFostimber, "axe", 0);
 			MinecraftForge.setBlockHarvestLevel(blockPlanksFostimber, "axe", 0);
 			
 			//Crafting Registering
 
 			GameRegistry.addRecipe(new ItemStack(itemGracePrism), new Object[] {"SSS","SDT","TTT", Character.valueOf('S'), Item.sugar, Character.valueOf('T'), Item.ghastTear, Character.valueOf('D'), Item.diamond});
 			GameRegistry.addRecipe(new ItemStack(itemGracePrism), new Object[] {"SSS","SDT","TTT", Character.valueOf('T'), Item.sugar, Character.valueOf('S'), Item.ghastTear, Character.valueOf('D'), Item.diamond});
 			GameRegistry.addRecipe(new ItemStack(itemGracePrism), new Object[] {"SST","SDT","STT", Character.valueOf('S'), Item.sugar, Character.valueOf('T'), Item.ghastTear, Character.valueOf('D'), Item.diamond});
 			GameRegistry.addRecipe(new ItemStack(itemGracePrism), new Object[] {"SST","SDT","STT", Character.valueOf('T'), Item.sugar, Character.valueOf('S'), Item.ghastTear, Character.valueOf('D'), Item.diamond});
 
 
 			GameRegistry.addRecipe(new ItemStack(itemPickaxeFostimber), new Object[] {"WW "," SW","S W", Character.valueOf('S'), Item.stick, Character.valueOf('W'), blockPlanksFostimber});
 			GameRegistry.addRecipe(new ItemStack(itemPickaxePalestone), new Object[] {"WW "," SW","S W", Character.valueOf('S'), Item.stick, Character.valueOf('W'), blockCobblePalestone});
 			GameRegistry.addRecipe(new ItemStack(itemShovelFostimber), new Object[] {" WW"," SW","S  ", Character.valueOf('S'), Item.stick, Character.valueOf('W'), blockPlanksFostimber});
 			GameRegistry.addRecipe(new ItemStack(itemSpadePalestone), new Object[] {" WW"," SW","S  ", Character.valueOf('S'), Item.stick, Character.valueOf('W'), blockCobblePalestone});
 			GameRegistry.addRecipe(new ItemStack(itemHoeFostimber), new Object[] {"WWW"," S ","S  ", Character.valueOf('S'), Item.stick, Character.valueOf('W'), blockPlanksFostimber});
 			GameRegistry.addRecipe(new ItemStack(itemHoePalestone), new Object[] {"WWW"," S ","S  ", Character.valueOf('S'), Item.stick, Character.valueOf('W'), blockCobblePalestone});
 			GameRegistry.addRecipe(new ItemStack(itemAxeFostimber), new Object[] {"WW ","WS ", "S  ", Character.valueOf('S'), Item.stick, Character.valueOf('W'), blockPlanksFostimber});
 			GameRegistry.addRecipe(new ItemStack(itemAxePalestone), new Object[] {"WW ","WS ", "S  ", Character.valueOf('S'), Item.stick, Character.valueOf('W'), blockCobblePalestone});
 			GameRegistry.addRecipe(new ItemStack(itemSwordFostimber), new Object[] {"  W"," W ", "S  ", Character.valueOf('S'), Item.stick, Character.valueOf('W'), blockPlanksFostimber});
 			GameRegistry.addRecipe(new ItemStack(itemSwordPalestone), new Object[] {"  W"," W ", "S  ", Character.valueOf('S'), Item.stick, Character.valueOf('W'), blockCobblePalestone});
 			GameRegistry.addRecipe(new ItemStack(itemWhistle), new Object[] {" OO","O O", "EO ", Character.valueOf('O'), Block.obsidian, Character.valueOf('E'), Item.eyeOfEnder});
 
 			GameRegistry.addRecipe(new ItemStack(blockPalestonePillar), new Object[] {"X", "X", Character.valueOf('X'), blockPalestone});
 
 
 			GameRegistry.addRecipe(new ItemStack(blockSulphure), new Object[] {"XXX", "XXX", "XXX", Character.valueOf('X'), itemSulphur});
 			GameRegistry.addShapelessRecipe(new ItemStack(itemSulphur, 9), new Object[] {blockSulphure});
 			GameRegistry.addRecipe(new ItemStack(blockBeryl), new Object[] {"XXX", "XXX", "XXX", Character.valueOf('X'), itemBeryl});
 			GameRegistry.addShapelessRecipe(new ItemStack(itemBeryl, 9), new Object[] {blockBeryl});
 			GameRegistry.addRecipe(new ItemStack(blockCobalt), new Object[] {"XXX", "XXX", "XXX", Character.valueOf('X'), itemIngotCobalt});
 			GameRegistry.addShapelessRecipe(new ItemStack(itemIngotCobalt, 9), new Object[] {blockCobalt});
 			GameRegistry.addRecipe(new ItemStack(blockIridium), new Object[] {"XXX", "XXX", "XXX", Character.valueOf('X'), itemIngotIridium});
 			GameRegistry.addShapelessRecipe(new ItemStack(itemIngotIridium, 9), new Object[] {blockIridium});
 			GameRegistry.addRecipe(new ItemStack(blockSilicon), new Object[] {"XXX", "XXX", "XXX", Character.valueOf('X'), itemSiliconChunk});
 			GameRegistry.addShapelessRecipe(new ItemStack(itemSiliconChunk, 9), new Object[] {blockSilicon});
 			GameRegistry.addRecipe(new ItemStack(blockJade), new Object[] {"XXX", "XXX", "XXX", Character.valueOf('X'), itemJade});
 			GameRegistry.addShapelessRecipe(new ItemStack(itemJade, 9), new Object[] {blockJade});
 			GameRegistry.addRecipe(new ItemStack(blockTourmaline), new Object[] {"XXX", "XXX", "XXX", Character.valueOf('X'), itemTourmaline});
 			GameRegistry.addShapelessRecipe(new ItemStack(itemTourmaline, 9), new Object[] {blockTourmaline});
 
 			GameRegistry.addShapelessRecipe(new ItemStack(itemAsphodelPetals, 2), new Object[] {blockFlowerAsphodel});
 			GameRegistry.addShapelessRecipe(new ItemStack(blockPlanksFostimber, 4), new Object[] {blockLogFostimber});
 
 			//Ore registry
 			OreDictionary.registerOre("dyePink", itemAsphodelPetals);
             OreDictionary.registerOre("logWood", blockLogFostimber);
             OreDictionary.registerOre("plankWood", blockPlanksFostimber);
             OreDictionary.registerOre("treeSapling", blockSaplingFostimber);
             OreDictionary.registerOre("treeLeaves", blockLeavesFostimber);
             
             OreDictionary.registerOre("oreIridium", oreIridium);
             OreDictionary.registerOre("oreSulphure", oreSulphure);
             OreDictionary.registerOre("oreBeryl", oreBeryl);
             OreDictionary.registerOre("oreCobalt", oreCobalt);
             OreDictionary.registerOre("oreJade", oreJade);
             OreDictionary.registerOre("oreSilicon", oreSilicon);
             OreDictionary.registerOre("oreTourmaline", oreTourmaline);
             
             //OreDictionary recipes
             CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(Item.stick, 6), new Object[] {"X", "X", "X", Character.valueOf('X'), "plankWood"}));
 
 
 			//Smelting Registering
 			
 			GameRegistry.addSmelting(this.oreCobalt.blockID, new ItemStack(this.itemIngotCobalt), 0.7F);
 			GameRegistry.addSmelting(this.oreIridium.blockID, new ItemStack(this.itemIngotIridium), 1.0F);
 			
 			
 			
 			//Entity Registering
 			
 			GameRegistry.registerTileEntity(ElysianTileEntityPortal.class, "ElysianTileEntityPortal");
 			
 //			TickRegistry.registerTickHandler(new TemperatureTickHandler(), Side.SERVER);
 			proxy.registerRenderers();
 			proxy.installSounds();
 			
 			//Temperature API
 			TemperatureManager.addBlockTemperature(new RangedTemperature(blockPalestone.blockID, -273, 300));
 		}
 		finally
 		{
 			config.save();
 		}
 	}
 	
 	@EventHandler
 	public void initialize(FMLInitializationEvent evt)
 	{
 		Plants.addGrassPlant(blockTallGrass, 0, 30);
 		Plants.addGrassPlant(blockFlowerAsphodel, 0, 10);
 		Plants.addGrassSeed(new ItemStack(itemSeedsPepper), 10);
 		
 //		FluidRegistry.registerFluid(FLUID_ELYSIAN_WATER);
 
 		
 		/** Register WorldProvider for Dimension **/
 		DimensionManager.registerProviderType(DimensionID, ElysiumWorldProvider.class, true);
 		DimensionManager.registerDimension(DimensionID, DimensionID);
 		
 		
 		biomePlain = new ElysiumBiomeGenPlain(25);
 		biomeOcean = new ElysiumBiomeGenOcean(26);
 		
 		
 		GameRegistry.registerWorldGenerator(new WorldGenElysium());
 
 		
 		EntityRegistry.registerGlobalEntityID(EntityCatorPillar.class, "CatorPillar", EntityRegistry.findGlobalUniqueEntityId(), 0x646464, 0x3A3A3A);
 		EntityRegistry.registerGlobalEntityID(EntityGerbil.class, "Gerbil", EntityRegistry.findGlobalUniqueEntityId(), 0x646464, 0x3A3A3A);
 		
 		//		cpw.mods.fml.common.registry.EntityRegistry.registerModEntity(EntityCatorPillar.class, "CatorPillar", 0, Elysium.instance, 64, 1, true);
 
 		LanguageRegistry.instance().addStringLocalization("entity.CatorPillar.name", "en_US", "Cator Pillar");
 		LanguageRegistry.instance().addStringLocalization("entity.Gerbil.name", "en_US", "Atmogerbil");
 		
 	}
 	
 	public static void registerBlock(Block block, String name)
 	{
 		GameRegistry.registerBlock(block, DefaultProps.modId+":"+block.getUnlocalizedName().substring(5));
 		LanguageRegistry.addName(block, name);
 	}
 	
 	public static boolean isHeatWave()
 	{
 		return false;
 	}
 }
