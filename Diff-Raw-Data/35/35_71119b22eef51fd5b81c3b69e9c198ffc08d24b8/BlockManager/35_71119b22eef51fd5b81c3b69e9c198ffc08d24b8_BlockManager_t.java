 package toldea.romecraft.managers;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.item.Item;
 import toldea.romecraft.block.BlockBellows;
 import toldea.romecraft.block.BlockBloomery;
 import toldea.romecraft.block.BlockMarbleMosaicSlab;
 import toldea.romecraft.block.BlockRomanAnvil;
 import toldea.romecraft.block.BlockRomanBrickSlab;
 import toldea.romecraft.block.BlockRomanVillageForum;
 import toldea.romecraft.block.RomeCraftBlock;
 import toldea.romecraft.block.RomeCraftBlockHalfSlab;
 import toldea.romecraft.block.RomeCraftBlockStairs;
 import toldea.romecraft.block.RomeCraftMultiSidedBlock;
 import toldea.romecraft.item.RomeCraftItemSlab;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class BlockManager {
 	public static Block blockRomanBricks = null;
 	public static RomeCraftBlockHalfSlab blockRomanBricksHalfSlab = null;
 	public static RomeCraftBlockHalfSlab blockRomanBricksDoubleSlab = null;
 	public static Block blockRomanBricksStairs = null;
 	public static Block blockRomanBricksChiseled = null;
 
 	public static Block blockMarble = null;
 	public static Block blockMarbleMosaic = null;
 	public static RomeCraftBlockHalfSlab blockMarbleMosaicHalfSlab = null;
 	public static RomeCraftBlockHalfSlab blockMarbleMosaicDoubleSlab = null;
 	public static Block blockMarbleMosaicStairs = null;
 
 	public static Block blockRomanVillageForum = null;
 
 	public static Block blockBloomery = null;
 	public static Block blockBellows = null;
 	
 	public static Block blockRomanAnvil = null;
 
 	public static void registerBlocks() {
 		// Roman Bricks
 		blockRomanBricks = new RomeCraftBlock(ConfigManager.blockRomanBricksId, Material.rock).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep)
 				.setUnlocalizedName("blockRomanBricks").setTextureName("romecraft:romanbrick");
 		GameRegistry.registerBlock(blockRomanBricks, "blockRomanBricks");
 		LanguageRegistry.addName(blockRomanBricks, "Roman Bricks");
 
 		blockRomanBricksHalfSlab = (RomeCraftBlockHalfSlab) new BlockRomanBrickSlab(ConfigManager.blockRomanBricksHalfSlabId, false).setHardness(2.0F).setResistance(10.0F)
 				.setStepSound(Block.soundStoneFootstep).setUnlocalizedName("blockRomanBricksSlab").setTextureName("romecraft:romanbrick");
 		GameRegistry.registerBlock(blockRomanBricksHalfSlab, "blockRomanBricksHalfSlab");
 		LanguageRegistry.instance().addStringLocalization(((BlockRomanBrickSlab) blockRomanBricksHalfSlab).getFullSlabName(0) + ".name", "Roman Bricks Slab");
 
 		blockRomanBricksDoubleSlab = (RomeCraftBlockHalfSlab) new BlockRomanBrickSlab(ConfigManager.blockRomanBricksDoubleSlabId, true).setHardness(2.0F).setResistance(10.0F)
 				.setStepSound(Block.soundStoneFootstep).setUnlocalizedName("blockRomanBricksSlab").setTextureName("romecraft:romanbrick");
 		GameRegistry.registerBlock(blockRomanBricksDoubleSlab, "blockRomanBricksDoubleSlab");
 		LanguageRegistry.addName(blockRomanBricksDoubleSlab, "Roman Bricks Double Slab");
 
 		Item.itemsList[blockRomanBricksHalfSlab.blockID] = (new RomeCraftItemSlab(blockRomanBricksHalfSlab.blockID - 256,
 				(RomeCraftBlockHalfSlab) blockRomanBricksHalfSlab, (RomeCraftBlockHalfSlab) blockRomanBricksDoubleSlab, false));
 
 		blockRomanBricksStairs = (new RomeCraftBlockStairs(ConfigManager.blockRomanBricksStairsId, blockRomanBricks, 0)).setUnlocalizedName("blockRomanBricksStairs");
 		GameRegistry.registerBlock(blockRomanBricksStairs, "blockRomanBricksStairs");
 		LanguageRegistry.addName(blockRomanBricksStairs, "Roman Brick Stairs");
 		/*
 		blockRomanBricksChiseled = new RomeCraftBlock(ConfigManager.blockRomanBricksChiseledId, Material.rock).setHardness(2.0F).setResistance(10.0F)
 				.setStepSound(Block.soundStoneFootstep).setUnlocalizedName("blockRomanBricksChiseled").setTextureName("romecraft:romanbrick_chiseled");
 		GameRegistry.registerBlock(blockRomanBricksChiseled, "blockRomanBricksChiseled");
 		LanguageRegistry.addName(blockRomanBricksChiseled, "Chiseled Roman Bricks");
 		*/
 		// Marble Block
 		blockMarble = new RomeCraftMultiSidedBlock(ConfigManager.blockMarbleId, Material.rock).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep)
 				.setUnlocalizedName("blockMarble").setTextureName("romecraft:marble_block");
 		GameRegistry.registerBlock(blockMarble, "blockMarble");
 		LanguageRegistry.addName(blockMarble, "Marble");
 
 		blockMarbleMosaic = new RomeCraftBlock(ConfigManager.blockMarbleMosaicId, Material.rock).setHardness(2.0F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep)
 				.setUnlocalizedName("blockMarbleMosaic").setTextureName("romecraft:marble_mosaic");
 		GameRegistry.registerBlock(blockMarbleMosaic, "blockMarbleMosaic");
 		LanguageRegistry.addName(blockMarbleMosaic, "Marble Mosaic");
 
 		blockMarbleMosaicHalfSlab = (RomeCraftBlockHalfSlab) new BlockMarbleMosaicSlab(ConfigManager.blockMarbleMosaicHalfSlabId, false).setHardness(2.0F).setResistance(10.0F)
 				.setStepSound(Block.soundStoneFootstep).setUnlocalizedName("blockMarbleMosaicHalfSlab").setTextureName("romecraft:marble_mosaic");
 		GameRegistry.registerBlock(blockMarbleMosaicHalfSlab, "blockMarbleMosaicSlab");
 		LanguageRegistry.instance().addStringLocalization(((BlockMarbleMosaicSlab) blockMarbleMosaicHalfSlab).getFullSlabName(0) + ".name", "Marble Mosaic Slab");
 
 		blockMarbleMosaicDoubleSlab = (RomeCraftBlockHalfSlab) new BlockMarbleMosaicSlab(ConfigManager.blockMarbleMosaicDoubleSlabId, true).setHardness(2.0F).setResistance(10.0F)
 				.setStepSound(Block.soundStoneFootstep).setUnlocalizedName("blockMarbleMosaicSlab").setTextureName("romecraft:marble_mosaic");
 		GameRegistry.registerBlock(blockMarbleMosaicDoubleSlab, "blockMarbleMosaicDoubleSlab");
 		LanguageRegistry.addName(blockMarbleMosaicDoubleSlab, "Marble Mosaic Double Slab");
 
 		Item.itemsList[blockMarbleMosaicHalfSlab.blockID] = (new RomeCraftItemSlab(blockMarbleMosaicHalfSlab.blockID - 256,
 				(RomeCraftBlockHalfSlab) blockMarbleMosaicHalfSlab, (RomeCraftBlockHalfSlab) blockMarbleMosaicDoubleSlab, false));
 
 		blockMarbleMosaicStairs = (new RomeCraftBlockStairs(ConfigManager.blockMarbleMosaicStairsId, blockMarbleMosaic, 0)).setUnlocalizedName("blockMarbleMosaicStairs");
 		GameRegistry.registerBlock(blockMarbleMosaicStairs, "blockMarbleMosaicStairs");
 		LanguageRegistry.addName(blockMarbleMosaicStairs, "Marble Mosaic Stairs");
 
 		// RomanVillageForum Block
		blockRomanVillageForum = new BlockRomanVillageForum(ConfigManager.blockRomanVillageForumId, Material.rock).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("romanVillageForum").setTextureName("romecraft:forum");
 		GameRegistry.registerBlock(blockRomanVillageForum, "blockRomanVillageForum");
 		LanguageRegistry.addName(blockRomanVillageForum, "Forum");
 
 		// Bloomery Block
		blockBloomery = new BlockBloomery(ConfigManager.blockBloomeryId, Material.clay).setHardness(1.25F).setResistance(7.0f).setStepSound(Block.soundStoneFootstep)
 				.setUnlocalizedName("bloomery").setTextureName("romecraft:bloomery");
 		GameRegistry.registerBlock(blockBloomery, "blockBloomery");
 		LanguageRegistry.addName(blockBloomery, "Bloomery Block");
 
 		// Bellows Block
 		blockBellows = new BlockBellows(ConfigManager.blockBellowsId, Material.wood).setUnlocalizedName("blockBellows").setTextureName("romecraft:bellows");
 		GameRegistry.registerBlock(blockBellows, "blockBellows");
 		LanguageRegistry.addName(blockBellows, "Bellows");
 		
 		// Roman Anvil Block
		blockRomanAnvil = new BlockRomanAnvil(ConfigManager.blockRomanAnvilId, Material.iron).setHardness(5.0F).setStepSound(Block.soundAnvilFootstep).setResistance(2000.0F).setUnlocalizedName("blockRomanAnvil").setTextureName("romecraft:romananvil_base");
 		GameRegistry.registerBlock(blockRomanAnvil, "blockRomanAnvil");
 		LanguageRegistry.addName(blockRomanAnvil, "Roman Anvil");
 	}
 }
