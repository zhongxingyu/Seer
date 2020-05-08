 package mods.elysium;
 
 import java.applet.Applet;
 import java.applet.AudioClip;
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import mods.elysium.api.Plants;
 import mods.elysium.block.*;
 import mods.elysium.dimension.*;
 import mods.elysium.dimension.biome.ElysiumBiomeGenPlain;
 import mods.elysium.dimension.biome.ElysiumBiomeGenOcean;
 import mods.elysium.dimension.gen.WorldGenElysium;
 import mods.elysium.dimension.portal.ElysianBlockPortalCore;
 import mods.elysium.dimension.portal.ElysianTileEntityPortal;
 import mods.elysium.dimension.portal.ElysianTileEntityPortalRenderer;
 import mods.elysium.handlers.ElysianBonemealHandler;
 import mods.elysium.handlers.ElysianCreatureSpawnHandler;
 import mods.elysium.handlers.ElysianFuelHandler;
 import mods.elysium.handlers.ElysianSoundHandler;
 import mods.elysium.item.*;
 import mods.elysium.network.PacketHandler;
 import mods.elysium.proxy.ClientProxy;
 import mods.elysium.proxy.CommonProxy;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.Minecraft;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraftforge.common.DimensionManager;
 import net.minecraftforge.common.EnumHelper;
 import net.minecraftforge.common.ForgeHooks;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.common.Property;
 import net.minecraftforge.event.EventBus;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(name="The Elysium", version="1.0", useMetadata = false, modid = "TheElysium", dependencies="required-after:Forge@[7.8.0,)")
 @NetworkMod(channels = {DefaultProps.NET_CHANNEL_NAME}, packetHandler = PacketHandler.class, clientSideRequired = true, serverSideRequired = true)
 public class Elysium
 {
 	@Instance("The Elysium")
 	public static Elysium instance;
 	
 	public static ElysianConfiguration mainConfiguration;
 	
 	public static final CreativeTabs tabElysium = new ElysianTab(12, "The Elysium");
 	
 	/** Dimension ID **/
 	public static int DimensionID;
 	public static int MaxDragon;
 	
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
 	public static Block waterStill;
 	public static ElysianBlockLiquid waterMoving;
 	public static Block blockFloatingShell;
 	public static Block blockFloatingConch;
 	public static Block blockCobblePalestone;
 //	public static Block LeucosandBlock;
 //	public static Block LeucosandBlock;
 	
 	public static Block blockPortalCore;
 	
 
 	//Items
 	
 	public static Item itemGracePrism;
 
 
 	public static Item itemWhistle;
 	public static Item itemSeedsPepper;
 	public static Item itemAsphodelPetals;
 	public static Item itemBerly;
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
 	public static BiomeGenBase elysianBiomePlain = null;
 	public static BiomeGenBase elysianBiomeOcean = null;
 
 	public static AudioClip soundWhistle;
 	
 	@PreInit
 	public void loadConfiguration(FMLPreInitializationEvent evt)
 	{
 //		NetworkRegistry.instance().registerGuiHandler(this, guiHandler);
 //		GameRegistry.registerTileEntity(TileMixer.class, "Mixer");
 //		GameRegistry.registerTileEntity(TileCandyMaker.class, "Candy Maker");
 
 
 //		GameRegistry.addBiome(Halloween);
 
 //		Version.versionCheck();
 		try
 		{
 			soundWhistle = Applet.newAudioClip(new URL("file:mods/elysium/sound/FluteTrack.wav"));
 		}
 		catch (MalformedURLException e)
 		{
 			e.printStackTrace();
 		}
 
 		mainConfiguration = new ElysianConfiguration(new File(evt.getModConfigurationDirectory(), "Elysium.cfg"));
 		try
 		{
 			mainConfiguration.load();
 
 			
 			Property idDim = Elysium.mainConfiguration.get("other", "dimensionID", DefaultProps.DimensionID, "This is the id of the dimension change if needed!");
 			DimensionID = idDim.getInt();
 			Property MAX_DRAGON_IN_END = Elysium.mainConfiguration.get("other", "MAX_DRAGON_IN_END", DefaultProps.MAX_DRAGON_IN_END, "How many dragons can be spawned to the End at the same time!");
 			MaxDragon = MAX_DRAGON_IN_END.getInt();
 			
 			//Handlers
 			MinecraftForge.EVENT_BUS.register(new ElysianBonemealHandler());
 			MinecraftForge.EVENT_BUS.register(new ElysianCreatureSpawnHandler());
 			CommonProxy.proxy.addSoundHandler(new ElysianSoundHandler());
 			GameRegistry.registerFuelHandler(new ElysianFuelHandler());
 
 			// Block Registry
 			
 			Property idPalestoneBlock = Elysium.mainConfiguration.getTerrainBlock("terrainGen", "palestone.id", DefaultProps.idPalestoneBlock, null);
 			blockPalestone = (new ElysianBlockPalestone(idPalestoneBlock.getInt(), Material.rock)).setHardness(1.5F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("Palestone");
 			ClientProxy.proxy.registerBlock(blockPalestone);
 			LanguageRegistry.addName(blockPalestone, "Palestone");
 			
 			Property idSoilBlock = Elysium.mainConfiguration.getTerrainBlock("terrainGen", "ElysiumDirt.id", DefaultProps.idSoilBlock, null);
 			blockDirt = (new ElysianBlockDirt(idSoilBlock.getInt(), Material.ground)).setHardness(0.5F).setStepSound(Block.soundGravelFootstep).setUnlocalizedName("Gammasoil");
 			ClientProxy.proxy.registerBlock(blockDirt);
 			LanguageRegistry.addName(blockDirt, "Elysian Soil");
 
 			Property idGrassBlock = Elysium.mainConfiguration.getTerrainBlock("terrainGen", "ElysiumGrass.id", DefaultProps.idGrassBlock, null);
 			blockGrass = (new ElysianBlockGrass(idGrassBlock.getInt(), Material.ground)).setHardness(0.6F).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("Gammagrass");
 			ClientProxy.proxy.registerBlock(blockGrass);
 			LanguageRegistry.addName(blockGrass, "Elysian Grass");
 
 			Property idLeucosandBlock = Elysium.mainConfiguration.getTerrainBlock("terrainGen", "Leucogrit.id", DefaultProps.idLeucosandBlock, null);
 			blockLeucosand = (new ElysianBlockLeucosand(idLeucosandBlock.getInt(), Material.sand)).setHardness(0.5F).setStepSound(Block.soundSandFootstep).setUnlocalizedName("Leucogrit");
 			ClientProxy.proxy.registerBlock(blockLeucosand);
 			LanguageRegistry.addName(blockLeucosand, "Leucosand");
 
 			Property idRiltBlock = Elysium.mainConfiguration.getTerrainBlock("terrainGen", "Rilt.id", DefaultProps.idRiltBlock, null);
 			blockRilt = (new ElysianBlockRilt(idRiltBlock.getInt(), Material.sand)).setHardness(0.6F).setStepSound(Block.soundGravelFootstep).setUnlocalizedName("Rilt");
 			ClientProxy.proxy.registerBlock(blockRilt);
 			LanguageRegistry.addName(blockRilt, "Rilt Block");
 
 			Property idFostimberSaplingBlock = Elysium.mainConfiguration.getBlock("FostimberSaplingBlock.id", DefaultProps.idFostimberSaplingBlock);
 			blockSaplingFostimber = (new ElysianBlockSaplingFostimber(idFostimberSaplingBlock.getInt())).setHardness(0F).setUnlocalizedName("fostimber_sapling");
 			ClientProxy.proxy.registerBlock(blockSaplingFostimber);
 			LanguageRegistry.addName(blockSaplingFostimber, "Fostimber Sapling");
 			
 			Property idFostimberLogBlock = Elysium.mainConfiguration.getBlock("FostimberLog.id", DefaultProps.idFostimberLogBlock);
 			blockLogFostimber = (new ElysianBlockLogFostimber(idFostimberLogBlock.getInt(), Material.wood)).setHardness(2.0F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("Fostimber Log Top");
 			ClientProxy.proxy.registerBlock(blockLogFostimber);
 			LanguageRegistry.addName(blockLogFostimber, "Fostimber Log");
 
 			Property idFostimberLeavesBlock = Elysium.mainConfiguration.getBlock("FostimberLeavesBlock.id", DefaultProps.idFostimberLeavesBlock);
 			blockLeavesFostimber = (new ElysianBlockLeavesFostimber(idFostimberLeavesBlock.getInt(), Material.leaves)).setLightOpacity(1).setHardness(0.2F).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("fostimber_leaves");
 			ClientProxy.proxy.registerBlock(blockLeavesFostimber);
 			LanguageRegistry.addName(blockLeavesFostimber, "Fostimber Leaves");
 
 			Property idWoodBlock = Elysium.mainConfiguration.getBlock("idWoodBlock.id", DefaultProps.idWoodBlock);
 			blockPlanksFostimber = (new ElysianBlock(idWoodBlock.getInt(), Material.wood)).setHardness(0.2F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("fostimber_planks");
 			ClientProxy.proxy.registerBlock(blockPlanksFostimber);
 			LanguageRegistry.addName(blockPlanksFostimber, "Wooden Planks");
 			
 			Property idGastroShellBlock = Elysium.mainConfiguration.getBlock("idGastroShellBlock.id", DefaultProps.idGastroShellBlock);
 			blockGastroShell = (new ElysianBlockGastroShell(idGastroShellBlock.getInt(), Material.rock)).setHardness(0.2F).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("gastroshellTop");
 			ClientProxy.proxy.registerBlock(blockGastroShell);
 			LanguageRegistry.addName(blockGastroShell, "Gastro Shell");
 
 			Property idAsphodelFlowerBlock = Elysium.mainConfiguration.getBlock("idAsphodelFlowerBlock.id", DefaultProps.idAsphodelFlowerBlock);
 			blockFlowerAsphodel = (new ElysiumFlowerBlock(idAsphodelFlowerBlock.getInt())).setHardness(0.0F).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("asphodel_flower");
 			ClientProxy.proxy.registerBlock(blockFlowerAsphodel);
 			LanguageRegistry.addName(blockFlowerAsphodel, "Asphodel Flower");
 
 			Property idCurlgrassBlock = Elysium.mainConfiguration.getBlock("idCurlgrassBlock.id", DefaultProps.idCurlgrassBlock);
 			blockTallGrass = new ElysianBlockTallgrass(idCurlgrassBlock.getInt()).setHardness(0.0F).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("Curlgrass");
 			ClientProxy.proxy.registerBlock(blockTallGrass);
 			LanguageRegistry.addName(blockTallGrass, "Curlgrass");
 
 			Property idOreSulphurBlock = Elysium.mainConfiguration.getBlock("idOreSulphurBlock.id", DefaultProps.idOreSulphurBlock);
 			oreSulphure = new ElysianBlockOre(idOreSulphurBlock.getInt()).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreSulphur");
 			ClientProxy.proxy.registerBlock(oreSulphure);
 			LanguageRegistry.addName(oreSulphure, "Sulphur Ore");
 
 			Property idOreBerylBlock = Elysium.mainConfiguration.getBlock("idOreBerylBlock.id", DefaultProps.idOreBerylBlock);
 			oreBeryl = new ElysianBlockOre(idOreBerylBlock.getInt()).setHardness(3.0F).setResistance(5.0F).setLightValue(0.5F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreBeryl");
 			ClientProxy.proxy.registerBlock(oreBeryl);
 			LanguageRegistry.addName(oreBeryl, "Beryl Ore");
 			
 			Property idOreCobaltBlock = Elysium.mainConfiguration.getBlock("idOreCobaltBlock.id", DefaultProps.idOreCobaltBlock);
 			oreCobalt = new ElysianBlockOre(idOreCobaltBlock.getInt()).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreCobalt");
 			ClientProxy.proxy.registerBlock(oreCobalt);
 			LanguageRegistry.addName(oreCobalt, "Cobalt Ore");
 
 			Property idOreIridiumBlock = Elysium.mainConfiguration.getBlock("idOreIridiumBlock.id", DefaultProps.idOreIridiumBlock);
 			oreIridium = new ElysianBlockOre(idOreIridiumBlock.getInt()).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreIridium");
 			ClientProxy.proxy.registerBlock(oreIridium);
 			LanguageRegistry.addName(oreIridium, "Iridium Ore");
 			
 			Property idOreSiliconBlock = Elysium.mainConfiguration.getBlock("idOreSiliconBlock.id", DefaultProps.idOreSiliconBlock);
 			oreSilicon = new ElysianBlockOre(idOreSiliconBlock.getInt()).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreSilicon");
 			ClientProxy.proxy.registerBlock(oreSilicon);
 			LanguageRegistry.addName(oreSilicon, "Silicon Ore");
 			
 			Property idOreJadeBlock = Elysium.mainConfiguration.getBlock("idOreJadeBlock.id", DefaultProps.idOreJadeBlock);
 			oreJade = new ElysianBlockOre(idOreJadeBlock.getInt()).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreJade");
 			ClientProxy.proxy.registerBlock(oreJade);
 			LanguageRegistry.addName(oreJade, "Jade Ore");
 
 			Property idOreTourmalineBlock = Elysium.mainConfiguration.getBlock("idOreTourmalineBlock.id", DefaultProps.idOreTourmalineBlock);
 			oreTourmaline = new ElysianBlockOre(idOreTourmalineBlock.getInt()).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreTourmaline");
 			ClientProxy.proxy.registerBlock(oreTourmaline);
 			LanguageRegistry.addName(oreTourmaline, "Tourmaline Ore");
 
 			Property idWaterBlock = Elysium.mainConfiguration.getTerrainBlock("terrainGen", "idWaterBlock.id", DefaultProps.idWaterBlock, null);
 			waterStill = new ElysianBlockLiquidStationary(idWaterBlock.getInt(), Material.water).setHardness(100.0F).setLightOpacity(3).setUnlocalizedName("elysian_water");
 			ClientProxy.proxy.registerBlock(waterStill);
 			LanguageRegistry.addName(waterStill, "Elysium Water Still");
 			
 			Property idWaterFlowingBlock = Elysium.mainConfiguration.getTerrainBlock("terrainGen", "idWaterFlowingBlock.id", DefaultProps.idWaterFlowingBlock, null);
 			waterMoving = (ElysianBlockLiquid) new ElysianBlockLiquidFlowing(idWaterFlowingBlock.getInt(), Material.water).setHardness(100.0F).setLightOpacity(3).setUnlocalizedName("elysian_water_flow");
 			ClientProxy.proxy.registerBlock(waterMoving);
 			LanguageRegistry.addName(waterMoving, "Elysium Water Flowing");
 			
 			Property idPortalCoreBlock = Elysium.mainConfiguration.getBlock("idPortalCoreBlock.id", DefaultProps.idPortalCoreBlock);
 			blockPortalCore = new ElysianBlockPortalCore(idPortalCoreBlock.getInt(), Material.glass).setHardness(5F).setStepSound(Block.soundGlassFootstep).setUnlocalizedName("portalCore");
 			ClientProxy.proxy.registerBlock(blockPortalCore);
 			LanguageRegistry.addName(blockPortalCore, "Elysian Portal Block");
 
 			Property idShellsBlock = Elysium.mainConfiguration.getBlock("idShellsBlock.id", DefaultProps.idShellsBlock);
			blockFloatingShell = new ElysianBlockShell(idShellsBlock.getInt()).setHardness(0.0F).setStepSound(Block.soundGlassFootstep).setUnlocalizedName("shell");
 			ClientProxy.proxy.registerBlock(blockFloatingShell);
 			LanguageRegistry.addName(blockFloatingShell, "Shell");
 
 			Property idConchBlock = Elysium.mainConfiguration.getBlock("idConchBlock.id", DefaultProps.idConchBlock);
			blockFloatingConch = new ElysianBlockShell(idConchBlock.getInt()).setHardness(0.0F).setStepSound(Block.soundGlassFootstep).setUnlocalizedName("conch");
 			ClientProxy.proxy.registerBlock(blockFloatingConch);
 			LanguageRegistry.addName(blockFloatingConch, "Conch");
 
 			Property idPaleCobblestoneBlock = Elysium.mainConfiguration.getBlock("idPaleCobblestoneBlock.id", DefaultProps.idPaleCobblestoneBlock);
 			blockCobblePalestone = (new ElysianBlock(idPaleCobblestoneBlock.getInt(), Material.rock)).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("palecobblestone");
 			ClientProxy.proxy.registerBlock(blockCobblePalestone);
 			LanguageRegistry.addName(blockCobblePalestone, "Cobble Palestone");
 			
 			
 			Block.dragonEgg.setCreativeTab(tabElysium);
 
 
 			// Item Registry
 			Property idGracePrismItem = Elysium.mainConfiguration.getItem("idGracePrismItem.id", DefaultProps.idGracePrismItem);
 			itemGracePrism = new ElysianItemGracePrism(idGracePrismItem.getInt()).setUnlocalizedName("gracecrystal");
 			LanguageRegistry.addName(itemGracePrism, "Grace Prism");
 
 			Property idWhistleItem = Elysium.mainConfiguration.getItem("idWhistleItem.id", DefaultProps.idWhistleItem);
 			itemWhistle = new ElysianItemWhistle(idWhistleItem.getInt()).setUnlocalizedName("enderflute");
 			LanguageRegistry.addName(itemWhistle, "Ender Flute");
 		
 			Property idPepperSeedItem = Elysium.mainConfiguration.getItem("idPepperSeedItem.id", DefaultProps.idPepperSeedItem);
 			itemSeedsPepper = new ElysianItem(idPepperSeedItem.getInt()).setUnlocalizedName("seeds_pepper");
 			LanguageRegistry.addName(itemSeedsPepper, "Pepper Seed");
 
 			Property idOverkillItem = Elysium.mainConfiguration.getItem("idOverkillItem.id", DefaultProps.idOverkillItem);
 			itemOverKill = new ElysianItemOverkill(idOverkillItem.getInt()).setUnlocalizedName("asd");
 			LanguageRegistry.addName(itemOverKill, "Overkill Item");
 		
 			Property idAsphodelPetalsItem = Elysium.mainConfiguration.getItem("idAsphodelPetalsItem.id", DefaultProps.idAsphodelPetalsItem);
 			itemAsphodelPetals = new ElysianItem(idAsphodelPetalsItem.getInt()).setUnlocalizedName("asphodelpetal");
 			LanguageRegistry.addName(itemAsphodelPetals, "Asphodel Petals");
 			
 			Property idDebugItem = Elysium.mainConfiguration.getItem("idDebugItem.id", DefaultProps.idDebugItem);
 			itemDebug = new ElysianItemDebug(idDebugItem.getInt()).setUnlocalizedName("debug");
 			LanguageRegistry.addName(itemDebug, "Modder Item");
 			
 			Property idBerylItem = Elysium.mainConfiguration.getItem("idBerylItem.id", DefaultProps.idBerylItem);
 			itemBerly = new ElysianItem(idBerylItem.getInt()).setUnlocalizedName("Beryl");
 			LanguageRegistry.addName(itemBerly, "Beryl");
 			
 			Property idCobaltIngotItem = Elysium.mainConfiguration.getItem("idCobaltIngotItem.id", DefaultProps.idCobaltIngotItem);
 			itemIngotCobalt = new ElysianItem(idCobaltIngotItem.getInt()).setUnlocalizedName("ingotCobalt");
 			LanguageRegistry.addName(itemIngotCobalt, "Cobalt Ingot");
 			
 			Property idIridiumIngotItem = Elysium.mainConfiguration.getItem("idIridiumIngotItem.id", DefaultProps.idIridiumIngotItem);
 			itemIngotIridium = new ElysianItem(idIridiumIngotItem.getInt()).setUnlocalizedName("ingotIridium");
 			LanguageRegistry.addName(itemIngotIridium, "Iridium Ingot");
 			
 			Property idJadeItem = Elysium.mainConfiguration.getItem("idJadeItem.id", DefaultProps.idJadeItem);
 			itemJade = new ElysianItem(idJadeItem.getInt()).setUnlocalizedName("Jade");
 			LanguageRegistry.addName(itemJade, "Jade");
 			
 			Property idSiliconChunk = Elysium.mainConfiguration.getItem("idSiliconChunk.id", DefaultProps.idSiliconChunk);
 			itemSiliconChunk = new ElysianItem(idSiliconChunk.getInt()).setUnlocalizedName("siliconchunk");
 			LanguageRegistry.addName(itemSiliconChunk, "Silicon Chunk");
 
 			Property idSulphurItem = Elysium.mainConfiguration.getItem("idSulphurItem.id", DefaultProps.idSulphurItem);
 			itemSulphur = new ElysianItem(idSulphurItem.getInt()).setUnlocalizedName("Sulphur");
 			LanguageRegistry.addName(itemSulphur, "Sulphur");
 
 			Property idTourmalineItem = Elysium.mainConfiguration.getItem("idTourmalineItem.id", DefaultProps.idTourmalineItem);
 			itemTourmaline = new ElysianItem(idTourmalineItem.getInt()).setUnlocalizedName("Tourmaline");
 			LanguageRegistry.addName(itemTourmaline, "Tourmaline");
 			
 			Property idSturdyHideItem = Elysium.mainConfiguration.getItem("idSturdyHideItem.id", DefaultProps.idSturdyHideItem);
 			itemSturdyHide = new ElysianItem(idSturdyHideItem.getInt()).setUnlocalizedName("SturdyHide");
 			LanguageRegistry.addName(itemSturdyHide, "Sturdy Hide");
 			
 			
 			
 			//Tools
 			EnumToolMaterial FOSTIMBER_MAT = EnumHelper.addToolMaterial("FOSTIMBER", 0, 59, 2.0F, 0, 15);
 
 			Property idWoodSwordItem = Elysium.mainConfiguration.getItem("idWoodSwordItem.id", DefaultProps.idWoodSwordItem);
 			itemSwordFostimber = new ElysianItemSword(idWoodSwordItem.getInt(), FOSTIMBER_MAT).setUnlocalizedName("swordFostimber");
 			LanguageRegistry.addName(itemSwordFostimber, "Fostimber Sword");
 
 			Property idWoodPickaxeItem = Elysium.mainConfiguration.getItem("idWoodPickaxeItem.id", DefaultProps.idWoodPickaxeItem);
 			itemPickaxeFostimber = new ElysianItemPickaxe(idWoodPickaxeItem.getInt(), FOSTIMBER_MAT).setUnlocalizedName("pickaxeFostimber");
 			LanguageRegistry.addName(itemPickaxeFostimber, "Fostimber Pickaxe");
 
 			Property idWoodAxeItem = Elysium.mainConfiguration.getItem("idWoodAxeItem.id", DefaultProps.idWoodAxeItem);
 			itemAxeFostimber = new ElysianItemAxe(idWoodAxeItem.getInt(), FOSTIMBER_MAT).setUnlocalizedName("axeFostimber");
 			LanguageRegistry.addName(itemAxeFostimber, "Fostimber Axe");
 
 			Property idWoodShovelItem = Elysium.mainConfiguration.getItem("idWoodShovelItem.id", DefaultProps.idWoodShovelItem);
 			itemShovelFostimber = new ElysianItemShovel(idWoodShovelItem.getInt(), FOSTIMBER_MAT).setUnlocalizedName("shovelFostimber");
 			LanguageRegistry.addName(itemShovelFostimber, "Fostimber Shovel");
 			
 			Property idWoodHoeItem = Elysium.mainConfiguration.getItem("idWoodHoeItem.id", DefaultProps.idWoodHoeItem);
 			itemHoeFostimber = new ElysianItemHoe(idWoodHoeItem.getInt(), FOSTIMBER_MAT).setUnlocalizedName("hoeFostimber");
 			LanguageRegistry.addName(itemHoeFostimber, "Fostimber Hoe");
 
 			EnumToolMaterial STONE_MAT = EnumHelper.addToolMaterial("PALESTONE", 1, 131, 4.0F, 1, 5);
 
 			Property idStoneSwordItem = Elysium.mainConfiguration.getItem("idStoneSwordItem.id", DefaultProps.idStoneSwordItem);
 			itemSwordPalestone = new ElysianItemSword(idStoneSwordItem.getInt(), STONE_MAT).setUnlocalizedName("swordPalestone");
 			LanguageRegistry.addName(itemSwordPalestone, "Palestone Sword");
 
 			Property idStonePickaxeItem = Elysium.mainConfiguration.getItem("idStonePickaxeItem.id", DefaultProps.idStonePickaxeItem);
 			itemPickaxePalestone = new ElysianItemPickaxe(idStonePickaxeItem.getInt(), STONE_MAT).setUnlocalizedName("pickaxePalestone");
 			LanguageRegistry.addName(itemPickaxePalestone, "Palestone Pickaxe");
 
 			Property idStoneAxeItem = Elysium.mainConfiguration.getItem("idStoneAxeItem.id", DefaultProps.idStoneAxeItem);
 			itemAxePalestone = new ElysianItemAxe(idStoneAxeItem.getInt(), STONE_MAT).setUnlocalizedName("axePalestone");
 			LanguageRegistry.addName(itemAxePalestone, "Palestone Axe");
 
 			Property idStoneShovelItem = Elysium.mainConfiguration.getItem("idStoneShovelItem.id", DefaultProps.idStoneShovelItem);
 			itemSpadePalestone = new ElysianItemShovel(idStoneShovelItem.getInt(), STONE_MAT).setUnlocalizedName("shovelPalestone");
 			LanguageRegistry.addName(itemSpadePalestone, "Palestone Shovel");
 			
 			Property idStoneHoeItem = Elysium.mainConfiguration.getItem("idStoneHoeItem.id", DefaultProps.idStoneHoeItem);
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
 			
 			
 			// Crafting Registry
 			GameRegistry.addRecipe(new ItemStack(itemGracePrism), new Object[] {"SMS","MDM","SMS", Character.valueOf('S'), Block.whiteStone, Character.valueOf('M'), Item.bucketMilk, Character.valueOf('D'), Item.diamond});
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
 			
 			GameRegistry.addRecipe(new ItemStack(Item.stick), new Object[] {"X", "X", "X", Character.valueOf('X'), blockPlanksFostimber});
 			
 			GameRegistry.addShapelessRecipe(new ItemStack(itemAsphodelPetals, 2), new Object[] {blockFlowerAsphodel});
 			GameRegistry.addShapelessRecipe(new ItemStack(blockPlanksFostimber, 4), new Object[] {blockLogFostimber});
 
 			//Smelting Registry
 			GameRegistry.addSmelting(this.oreCobalt.blockID, new ItemStack(this.itemIngotCobalt), 0.7F);
 			GameRegistry.addSmelting(this.oreIridium.blockID, new ItemStack(this.itemIngotIridium), 1.0F);
 			
 
 			
 			// Entity Registry
 			GameRegistry.registerTileEntity(ElysianTileEntityPortal.class, "ElysiumTileEntityPortal");
 			
 			ClientProxy.proxy.RegisterRenders();
 		}
 		finally
 		{
 			mainConfiguration.save();
 		}
 	}
 	@Init
 	public void initialize(FMLInitializationEvent evt) {
 		
 		
 		Plants.addGrassPlant(blockTallGrass, 0, 30);
 		Plants.addGrassPlant(blockFlowerAsphodel, 0, 10);
 		Plants.addGrassSeed(new ItemStack(itemSeedsPepper), 10);
 		
 //		new LiquidStacks();
 //		CoreProxy.proxy.addAnimation();
 //		LiquidManager.liquids.add(new LiquidData(LiquidStacks.rawCandy, new ItemStack(rawCandyBucket), new ItemStack(Item.bucketEmpty)));
 //		LiquidManager.liquids.add(new LiquidData(LiquidStacks.milk, new ItemStack(Item.bucketMilk), new ItemStack(Item.bucketEmpty)));
 
 //		CoreProxy.proxy.initializeRendering();
 //		CoreProxy.proxy.initializeEntityRendering();
 		
 	
 		/** Register WorldProvider for Dimension **/
 		DimensionManager.registerProviderType(DimensionID, ElysiumWorldProvider.class, true);
 		DimensionManager.registerDimension(DimensionID, DimensionID);
 
 		
 		elysianBiomePlain = new ElysiumBiomeGenPlain(25);
 		elysianBiomeOcean = new ElysiumBiomeGenOcean(26);
 		
 		
 		GameRegistry.registerWorldGenerator(new WorldGenElysium());
 
 	}
 	
 	public static boolean isHeatWave()
 	{
		return true;
 	}
 }
