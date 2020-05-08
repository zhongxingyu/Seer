 package ccm.hephaestus.utils.registry;
 
 import ccm.hephaestus.Hephaestus;
 import ccm.hephaestus.block.enums.EnumModeled;
 import ccm.hephaestus.creativetab.HephaestusTabs;
 import ccm.hephaestus.utils.lib.Properties;
 import ccm.nucleum.omnium.block.sub.SubBlock;
 import ccm.nucleum.omnium.block.sub.SubModelled;
 import ccm.nucleum.omnium.block.tile.TileLogic;
 import ccm.nucleum.omnium.utils.handler.LogHandler;
import ccm.nucleum.omnium.utils.helper.enums.EnumHelper;
 
 final class BlockRegistry
 {
 
     protected static void registerBlocks()
     {
         LogHandler.finest(Hephaestus.instance, "Making Sub Blocks");
 
         registerCustomBlocks();
 
         registerCustomRenderBlocks();
     }
 
     static void registerCustomBlocks()
     {
         LogHandler.finest(Hephaestus.instance, "Making Sub Blocks, Stage 2");
     }
 
     static void registerCustomRenderBlocks()
     {
         LogHandler.finest(Hephaestus.instance, "Making Sub Blocks, Stage 3");
 
         SubBlock.setUp(EnumModeled.machineGrinder,
                new SubModelled(Properties.modeledBlockID, EnumModeled.machineGrinder.ordinal(), new TileLogic()).setTileEntity(EnumHelper.getTile(EnumModeled.machineGrinder))
                         .setCreativeTab(HephaestusTabs.tabHephaestusBlocks).setUnlocalizedName(EnumModeled.machineGrinder).setHardness(3.0F));
 
     }
 }
