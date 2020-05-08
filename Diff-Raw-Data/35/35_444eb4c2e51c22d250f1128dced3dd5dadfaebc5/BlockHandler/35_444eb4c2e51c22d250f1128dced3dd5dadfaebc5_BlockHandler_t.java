 package common.blocks;
 
 import common.CreativeTab;
 import common.FaithCraft;
 import common.IDHandler;
 import common.Reference;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraftforge.common.Configuration;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class BlockHandler {
 
     static Block altar;
 
     public static void configureBlocks(Configuration config) {
        altar = new Altar(IDHandler.AltarID, Material.rock).setUnlocalizedName("blockAltar").setCreativeTab(FaithCraft.modTab);
 
         registerBlocks(new GameRegistry()); // keep this at the end spent an hour trying to figure out why the block wasn't there.. it wasn't called anywhere....
     }
 
     public static void registerBlocks(GameRegistry registry) {
         registry.registerBlock(altar, "blockAltar");
     }
 
     public static void addNames() {
         // en_US
         FaithCraft.addToNameList("Altar");
 
         // en_PT
         FaithCraft.addToNameList("Block of Worship");
     }
 
     public static void addToObjectsList() {
         FaithCraft.addToObjectList(altar);
     }
 }
