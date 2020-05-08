 package com.teammetallurgy.agriculture;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.fluids.Fluid;
 import net.minecraftforge.fluids.FluidRegistry;
 import net.minecraftforge.oredict.OreDictionary;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 
 import com.teammetallurgy.agriculture.crops.BlockPeanut;
 import com.teammetallurgy.agriculture.crops.BlockSpice;
 import com.teammetallurgy.agriculture.crops.BlockStrawberry;
 import com.teammetallurgy.agriculture.machines.brewer.BlockBrewer;
 import com.teammetallurgy.agriculture.machines.brewer.TileEntityBrewer;
 import com.teammetallurgy.agriculture.machines.counter.BlockCounter;
 import com.teammetallurgy.agriculture.machines.counter.TileEntityCounter;
 import com.teammetallurgy.agriculture.machines.frier.BlockFrier;
 import com.teammetallurgy.agriculture.machines.frier.TileEntityFrier;
 import com.teammetallurgy.agriculture.machines.icebox.BlockIcebox;
 import com.teammetallurgy.agriculture.machines.icebox.TileEntityIcebox;
 import com.teammetallurgy.agriculture.machines.oven.BlockOven;
 import com.teammetallurgy.agriculture.machines.oven.TileEntityOven;
 import com.teammetallurgy.agriculture.machines.processor.BlockProcessor;
 import com.teammetallurgy.agriculture.machines.processor.TileEntityProcessor;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class AgricultureBlocks {
     public static Block brewer;
     public static Block cinnamon;
     public static Block counter;
     public static Block frier;
     public static Block icebox;
     public static Block oven;
 
     public static Block peanut;
     public static Block processor;
     public static Block salt;
     public static Block strawberry;
     public static Block vanilla;
 
     public static void addNames()
     {
         LanguageRegistry.addName(AgricultureBlocks.oven, "Oven");
         LanguageRegistry.addName(AgricultureBlocks.counter, "Counter");
         LanguageRegistry.addName(AgricultureBlocks.processor, "Processor");
         LanguageRegistry.addName(AgricultureBlocks.brewer, "Brewer");
         LanguageRegistry.addName(AgricultureBlocks.cinnamon, "Cinnamon Plant");
         LanguageRegistry.addName(AgricultureBlocks.vanilla, "Vanilla Plant");
         LanguageRegistry.addName(AgricultureBlocks.salt, "Salt Ore");
         LanguageRegistry.addName(AgricultureBlocks.icebox, "Ice Box");
         LanguageRegistry.addName(AgricultureBlocks.frier, "Frier");
     }
 
     public static void init()
     {
         // oven = new BlockOven(3000,
         // Material.wood).setUnlocalizedName("agriculture:oven").setCreativeTab(Agriculture.tab);
 
         int def = 3000;
         AgricultureBlocks.oven = new BlockOven(ConfigHandler.getBlockId("Oven", def++), Material.wood).setUnlocalizedName("agriculture:oven").setCreativeTab(Agriculture.tab);
         AgricultureBlocks.counter = new BlockCounter(ConfigHandler.getBlockId("Counter", def++), Material.wood).setUnlocalizedName("agriculture:counter").setCreativeTab(Agriculture.tab);
         AgricultureBlocks.processor = new BlockProcessor(ConfigHandler.getBlockId("Processor", def++), Material.wood).setUnlocalizedName("agriculture:processor").setCreativeTab(Agriculture.tab);
         AgricultureBlocks.salt = new BlockSalt(ConfigHandler.getBlockId("Salt", def++), Material.rock).setHardness(1.5F).setUnlocalizedName("agriculture:salt").setTextureName("agriculture:Salt").setCreativeTab(Agriculture.tab);
         AgricultureBlocks.cinnamon = new BlockSpice(ConfigHandler.getBlockId("Cinnamon", def++)).setDrop(AgricultureItems.cinnamon.getItemStack()).setHardness(1.0f).setUnlocalizedName("agriculture:cinnamon").setTextureName("agriculture:cinnamon").setCreativeTab(Agriculture.tab);
 
         AgricultureBlocks.brewer = new BlockBrewer(ConfigHandler.getBlockId("Brewer", def++), Material.wood).setUnlocalizedName("agriculture:brewer").setCreativeTab(Agriculture.tab);
         AgricultureBlocks.icebox = new BlockIcebox(ConfigHandler.getBlockId("Icebox", def++), Material.wood).setUnlocalizedName("agriculture:icebox").setCreativeTab(Agriculture.tab);
         AgricultureBlocks.frier = new BlockFrier(ConfigHandler.getBlockId("Frier", def++), Material.wood).setUnlocalizedName("agriculture:frier").setCreativeTab(Agriculture.tab);
 
         AgricultureBlocks.vanilla = new BlockSpice(ConfigHandler.getBlockId("Vanilla", def++)).setDrop(AgricultureItems.vanilla.getItemStack()).setHardness(1.0f).setUnlocalizedName("agriculture:vanilla").setTextureName("agriculture:vanilla").setCreativeTab(Agriculture.tab);
         AgricultureBlocks.peanut = new BlockPeanut(ConfigHandler.getBlockId("Peanut", def++)).setHardness(0.1f).setUnlocalizedName("agriculture:peanut").setTextureName("agriculture:peanut").setCreativeTab(Agriculture.tab);
         AgricultureBlocks.strawberry = new BlockStrawberry(ConfigHandler.getBlockId("Strawberry", def++)).setHardness(0.1f).setUnlocalizedName("agriculture:strawberry").setTextureName("agriculture:strawberry").setCreativeTab(Agriculture.tab);
 
         // brewer = new BlockBrewer(ConfigHandler.getBlockId("Brewer", def++),
         // Material.wood).setUnlocalizedName("agriculture:brewer").setCreativeTab(Agriculture.tab);
         // icebox = new BlockIcebox(ConfigHandler.getBlockId("Icebox", def++),
         // Material.wood).setUnlocalizedName("agriculture:icebox").setCreativeTab(Agriculture.tab);
         // frier = new BlockFrier(ConfigHandler.getBlockId("Frier", def++),
         // Material.wood).setUnlocalizedName("agriculture:frier").setCreativeTab(Agriculture.tab);
 
         FluidRegistry.registerFluid(new Fluid("milk"));
         FluidRegistry.registerFluid(new Fluid("beer"));
         FluidRegistry.registerFluid(new Fluid("hotcocoa"));
         FluidRegistry.registerFluid(new Fluid("vinegar"));
         FluidRegistry.registerFluid(new Fluid("cookingOil"));
         FluidRegistry.registerFluid(new Fluid("vodka"));
         FluidRegistry.registerFluid(new Fluid("cider"));
 
         GameRegistry.registerBlock(AgricultureBlocks.oven, "AgricultureOvenBlock");
         GameRegistry.registerBlock(AgricultureBlocks.counter, "AgricultureCounterBlock");
         GameRegistry.registerBlock(AgricultureBlocks.processor, "AgricultureProcessorBlock");
         GameRegistry.registerBlock(AgricultureBlocks.salt, "AgricultureSalt");
         GameRegistry.registerBlock(AgricultureBlocks.cinnamon, "AgricultureCinnamon");
         GameRegistry.registerBlock(AgricultureBlocks.vanilla, "AgricultureVanilla");
         GameRegistry.registerBlock(AgricultureBlocks.brewer, "AgricultureBrewerBlock");
         GameRegistry.registerBlock(AgricultureBlocks.icebox, "AgricultureIcebox");
         GameRegistry.registerBlock(AgricultureBlocks.frier, "AgricultureFrier");
        GameRegistry.registerBlock(strawberry, ItemBlock.class, "AgricultureStrawberry");
        GameRegistry.registerBlock(peanut, ItemBlock.class, "AgriculturePeanut");
         GameRegistry.registerTileEntity(TileEntityOven.class, "AgricultureOvenTileEntity");
         GameRegistry.registerTileEntity(TileEntityCounter.class, "AgricultureCounterTileEntity");
         GameRegistry.registerTileEntity(TileEntityProcessor.class, "AgricultureProcessorTileEntity");
         GameRegistry.registerTileEntity(TileEntityBrewer.class, "AgricultureBrewerTileEntity");
         GameRegistry.registerTileEntity(TileEntityIcebox.class, "AgricultureIceboxTileEntity");
         GameRegistry.registerTileEntity(TileEntityFrier.class, "AgricultureFrierTileEntity");
 
         GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(AgricultureBlocks.oven), "WWW", "BFB", "BBB", 'W', "plankWood", 'F', Block.furnaceIdle, 'B', Item.brick));
         GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(AgricultureBlocks.brewer), "BBB", "BGB", "BIB", 'G', Block.glass, 'I', Item.ingotIron, 'B', Item.brick));
         GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(AgricultureBlocks.counter), "WWW", "BCB", "BBB", 'W', "plankWood", 'C', Block.chest, 'B', Item.brick));
         GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(AgricultureBlocks.processor), "WIW", "BFB", "BBB", 'W', "plankWood", 'I', Item.ingotIron, 'F', Block.furnaceIdle, 'B', Item.brick));
         GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(AgricultureBlocks.icebox), "BIB", "BBB", "BIB", 'I', Item.ingotIron, 'B', Item.brick));
         GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(AgricultureBlocks.frier), "BIB", "BGB", "BIB", 'I', Item.ingotIron, 'G', Block.glass, 'B', Item.brick));
 
         AgricultureBlocks.addNames();
         AgricultureBlocks.registerOreDictionary();
     }
 
     public static void registerOreDictionary()
     {
         OreDictionary.registerOre("oven", new ItemStack(AgricultureBlocks.oven));
         OreDictionary.registerOre("counter", new ItemStack(AgricultureBlocks.counter));
         OreDictionary.registerOre("processor", new ItemStack(AgricultureBlocks.processor));
         OreDictionary.registerOre("brewer", new ItemStack(AgricultureBlocks.brewer));
         OreDictionary.registerOre("cropCinnamon", new ItemStack(AgricultureBlocks.cinnamon));
         OreDictionary.registerOre("cropVanilla", new ItemStack(AgricultureBlocks.vanilla));
         OreDictionary.registerOre("oreSalt", new ItemStack(AgricultureBlocks.salt));
         OreDictionary.registerOre("icebox", new ItemStack(AgricultureBlocks.icebox));
         OreDictionary.registerOre("frier", new ItemStack(AgricultureBlocks.frier));
         OreDictionary.registerOre("cropStrawberry", new ItemStack(AgricultureBlocks.strawberry));
         OreDictionary.registerOre("cropPeanut", new ItemStack(AgricultureBlocks.peanut));
 
     }
 }
