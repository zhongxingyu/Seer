 package siramnot.mods.dmi;
 
 import net.minecraft.block.Block;
 import siramnot.mods.dmi.blocks.BlockAqueousCrystalOre;
 import siramnot.mods.dmi.blocks.BlockIgneousCrystal;
 import siramnot.mods.dmi.blocks.BlockAqueousCrystal;
 import siramnot.mods.dmi.blocks.BlockConsistoriumCrystal;
 import siramnot.mods.dmi.blocks.BlockConsistoriumCrystalOre;
 import siramnot.mods.dmi.blocks.BlockIgneousCrystalOre;
 import siramnot.mods.dmi.blocks.BlockLiquidator;
 import siramnot.mods.dmi.blocks.BlockWorkstation;
 import siramnot.mods.dmi.blocks.DMIOre;
 import siramnot.mods.dmi.blocks.tileeents.TileEntityLiquidator;
 import siramnot.mods.dmi.blocks.tileeents.TileEntityWorkStationBlockEntity;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 /**
  * Dominus ex Magica et Industria mod. (C) Copyright SirAmNot 2013
  * 
  * 
  * @author SirAmNot, Jay
  * 
  */
 public class DMIBlockManager {
 
 	//Blocks
 	public static Block liquidator;
 	public static Block workStation;
 	public static DMIOre aqueousOre;
 	public static DMIOre igneousOre;
 	public static Block aqueousBlock;
 	public static Block igneousBlock;
 	public static Block consistoriumOre;
 	public static Block consistoriumBlock;
 
 	// Load the blocks
 	public static void load() {
 		// workStation = new TileEntityWorkStationBlock(IDManager.WORK_STATION); Doesn't work
 		liquidator = new BlockLiquidator(IDManager.LIQUIDATOR, false);
 		workStation = new BlockWorkstation(IDManager.WORK_STATION_BLOCK);
 
 		aqueousOre = new BlockAqueousCrystalOre(IDManager.AQUEOUS_CRYSTAL_ORE);
 		igneousOre = new BlockIgneousCrystalOre(IDManager.IGNEOUS_CRYSTAL_ORE);
 
 		aqueousBlock = new BlockAqueousCrystal(IDManager.AQUEOUS_CRYSTAL_BLOCK);
		workStation = new BlockWorkstation(IDManager.WORK_STATION_BLOCK);
		igneousBlock = new BlockIgneousCrystal(IDManager.IGNEOUS_CRYSTAL_BLOCK);
		consistoriumOre = new BlockConsistoriumCrystalOre(IDManager.CONSISTORIUM_CRYSTAL_ORE);
 		consistoriumBlock = new BlockConsistoriumCrystal(IDManager.CONSISTORIUM_CRYSTAL_BLOCK);

 		igneousBlock = new BlockIgneousCrystal(IDManager.IGNEOUS_CRYSTAL_BLOCK);
 
 		languageRegistry(LanguageRegistry.instance());
 		gameRegistry();
 
 	}
 
 	// Register the blocks, gives them mod-unique names
 	private static void gameRegistry() {
		// GameRegistry.registerBlock(liquidator, "Liquidator");
		// GameRegistry.registerBlock(workStation, "Work Station");
 
 		GameRegistry.registerBlock(aqueousOre, "Aqueous Ore");
 		GameRegistry.registerBlock(igneousOre, "Igneous Ore");
 
 		GameRegistry.registerBlock(aqueousBlock, "Aqueous Crystal Block");
		GameRegistry.registerBlock(igneousBlock, "Igneous Crystal Block");
		GameRegistry.registerBlock(consistoriumOre, "Consistorium Ore");
 		GameRegistry.registerBlock(consistoriumBlock, "Consistorium Block");
 
 		GameRegistry.registerTileEntity(TileEntityLiquidator.class, "Tile Entity Liquidator");
 		GameRegistry.registerTileEntity(TileEntityWorkStationBlockEntity.class, "Tile Entity Workstation");
 	}
 
 	// Name the blocks in-game
 	private static void languageRegistry(LanguageRegistry lr) {
 		lr.addName(liquidator, "Liquidator");
 		lr.addName(workStation, "Work Station");
		
		 lr.addName(aqueousOre, "Aqueous Ore");
		 lr.addName(consistoriumOre, "Consistorium Ore");
		 lr.addName(igneousOre, "Igneous Ore");
 
		 lr.addName(aqueousBlock, "Aqueous Block");
		 lr.addName(consistoriumBlock, "Consistorium Block");
		 lr.addName(igneousOre, "Igneous Ore");
 	}
 }
