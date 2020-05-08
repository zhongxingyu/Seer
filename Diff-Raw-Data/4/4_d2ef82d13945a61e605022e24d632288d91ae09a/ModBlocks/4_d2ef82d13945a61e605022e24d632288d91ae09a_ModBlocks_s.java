 package obt.block;
 
 import obt.lib.BlockIds;
 import obt.block.BlockOb;
 import obt.block.BlockTest;
 import obt.lib.Reference;
 import obt.lib.Strings;
 import net.arasaia.ExampleMods.blocks.ItemExampleModBlock;
 import net.arasaia.ExampleMods.blocks.OreExampleMods;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemStack;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 public class ModBlocks
 {
     public static Block testBlock;
     
     
     public static void init()
     {
         //Set names
         testBlock = (new BlockTest(BlockIds.TESTBLOCK, Material.ground))
         .setUnlocalizedName("blockTest");
         //Register Blocks
        GameRegistry.registerBlock(blockTest, ItemMod.class,
                Reference.MOD_ID+(blockTest).getUnlocalizedName().substring(5)));
         
     }
     
 }
     
