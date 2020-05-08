 package net.Harmonion.block;
 
 import net.Harmonion.block.power.BlockMachine;
 import net.Harmonion.block.power.BlockMachinePanel;
 import net.Harmonion.block.power.BlockMicro;
 import net.Harmonion.creativetab.CreativeTabHarmonionB;
 import net.Harmonion.creativetab.CreativeTabHarmonionP;
 import net.Harmonion.item.ModItems;
 import net.Harmonion.item.power.ItemExtended;
 import net.Harmonion.item.power.ItemMachinePanel;
 import net.Harmonion.item.power.ItemMicro;
 import net.Harmonion.power.CoverLib;
 import net.Harmonion.power.MicroPlacementWire;
 import net.Harmonion.power.RedPowerLib;
 import net.Harmonion.power.TileBatteryBox;
 import net.Harmonion.power.TileBluewire;
 import net.Harmonion.power.TileCovered;
 import net.Harmonion.power.TileSolarPanel;
 import net.Harmonion.server.Harmonion;
 import net.Harmonion.util.BlockIds;
 import net.Harmonion.world.WorldProviderHarmonion;
 import net.Harmonion.world.gen.feature.WorldPopulator;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraftforge.common.DimensionManager;
 import net.minecraftforge.oredict.OreDictionary;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 public class ModBlocks {
 	
     /* Mod Block instances */
 	public static Block HarmonionOre;
 	public static Block HarmonionBlock;
 	public static BlockHarmonionPortal HarmonionPortal;
 	public static Block HarmonionFire;
 	public static Block HarmonionLog;
 	public static Block HarmonionLeaves;
 	public static Block HarmonionSapling;
 	public static Block HarmonionDoor;
 	public static Block HarmonionGlass;
 	
 	public static BlockMicro blockMicro;
 	public static BlockMachine blockMachine;
 	public static BlockMachinePanel blockMachinePanel;
 	public static int customBlockModel;
 	
 	public static final CreativeTabs tabHarmonioncraftB = new CreativeTabHarmonionB(CreativeTabs.getNextID(), "HarmonionB");
 	public static final CreativeTabs tabHarmonioncraftW = new CreativeTabHarmonionP(CreativeTabs.getNextID(), "HarmonionP");
 	
 	public static void init() {
 		
 		/* Initialize each mod block individually */
 		HarmonionOre = new BlockHarmonionOre(BlockIds.Harmonion, 0);
 		HarmonionBlock = new BlockHarmonionBlock(BlockIds.Harmonion_Block, 3);
 		HarmonionLog = new BlockHarmonionLog(BlockIds.Harmonion_Log);
 		HarmonionLeaves = new BlockHarmonionLeaves(BlockIds.Harmonion_Leaves);
 		HarmonionSapling = new BlockHarmonionSapling(BlockIds.Harmonion_Sapling, 13);
 		HarmonionDoor = new BlockHarmonionDoor(BlockIds.Harmonion_Door, 11, 12, Material.iron);
 		HarmonionGlass = new BlockHarmonionGlass(BlockIds.Harmonion_Glass, 4, Material.glass, false);
 		HarmonionPortal = (BlockHarmonionPortal)((BlockHarmonionPortal)(new BlockHarmonionPortal(BlockIds.Harmonion_Portal, 14)));
 		HarmonionFire = (new BlockHarmonionFire(BlockIds.Harmonion_Fire, Block.fire.blockIndexInTexture));
 		
 		blockMicro = new BlockMicro(609);
        blockMicro.setBlockName("Harmonionwire");
         GameRegistry.registerBlock(blockMicro, ItemMicro.class, "micro");
         blockMicro.addTileEntityMapping(0, TileCovered.class);
     	CoverLib.blockCoverPlate = blockMicro;
     	
     	blockMachine = new BlockMachine(610);
         blockMachine.setBlockName("rpmachine");
         GameRegistry.registerBlock(blockMachine, ItemExtended.class, "machine");
         blockMachine.setItemName(0, "rpbatbox");
        GameRegistry.registerTileEntity(TileBatteryBox.class, "HarmoionBatBox");
         blockMachine.addTileEntityMapping(0, TileBatteryBox.class);
         
         blockMachinePanel = new BlockMachinePanel(611);
         GameRegistry.registerBlock(blockMachinePanel, ItemMachinePanel.class, "machinePanel");
        GameRegistry.registerTileEntity(TileSolarPanel.class, "HarmonionSolar");
         blockMachinePanel.addTileEntityMapping(0, TileSolarPanel.class);
         blockMachinePanel.setItemName(0, "rpsolar");
         
         
         int var1;
         int var2;
         RedPowerLib.addCompatibleMapping(0, 1);
 
         for (var1 = 0; var1 < 16; ++var1)
         {
             RedPowerLib.addCompatibleMapping(0, 2 + var1);
             RedPowerLib.addCompatibleMapping(1, 2 + var1);
             RedPowerLib.addCompatibleMapping(65, 2 + var1);
 
             for (var2 = 0; var2 < 16; ++var2)
             {
                 RedPowerLib.addCompatibleMapping(19 + var2, 2 + var1);
             }
 
             RedPowerLib.addCompatibleMapping(18, 2 + var1);
             RedPowerLib.addCompatibleMapping(18, 19 + var1);
         }
 
         RedPowerLib.addCompatibleMapping(0, 65);
         RedPowerLib.addCompatibleMapping(1, 65);
         RedPowerLib.addCompatibleMapping(64, 65);
         RedPowerLib.addCompatibleMapping(64, 67);
         RedPowerLib.addCompatibleMapping(65, 67);
         RedPowerLib.addCompatibleMapping(66, 67);
 
 		/* Adds Blocks into the game */
 		OreDictionary.registerOre("HarmonionOre", HarmonionOre);
         GameRegistry.registerBlock(HarmonionOre, BlockOresMeta.class);
 		GameRegistry.registerBlock(HarmonionLeaves);
 		GameRegistry.registerBlock(HarmonionLog);
 		GameRegistry.registerBlock(HarmonionDoor);
 		GameRegistry.registerBlock(HarmonionGlass);
 		
 		GameRegistry.registerBlock(HarmonionBlock);
 		GameRegistry.registerBlock(HarmonionPortal);
 		GameRegistry.registerBlock(HarmonionFire);
 		
 		DimensionManager.registerProviderType(BlockIds.Harmonion_Dimension, WorldProviderHarmonion.class, false);
         DimensionManager.registerDimension(BlockIds.Harmonion_Dimension, BlockIds.Harmonion_Dimension);
 		
 		/* WorldGen */
 		GameRegistry.registerWorldGenerator(new WorldPopulator());
 		
 	}
 	
 	public static void initBlockSmelting() {
 		
 		FurnaceRecipes furnaceRecipes = FurnaceRecipes.smelting();
 		
 		/* Harmonon Ore Smelting. */
 		furnaceRecipes.addSmelting(ModBlocks.HarmonionOre.blockID, new ItemStack(ModItems.Refinedsoundstone), 5.0F);
 	}
 	
 }
