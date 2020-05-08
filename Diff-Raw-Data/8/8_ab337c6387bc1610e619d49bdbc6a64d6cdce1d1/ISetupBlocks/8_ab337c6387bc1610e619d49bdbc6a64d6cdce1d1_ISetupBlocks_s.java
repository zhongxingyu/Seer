 package ip.industrialProcessing.config;
 
 import ip.industrialProcessing.IndustrialProcessing;
 import ip.industrialProcessing.buildingBlock.BlockBase;
 import ip.industrialProcessing.decoration.BlockPlatform;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockOre;
 import net.minecraft.block.material.Material;
 
 public interface ISetupBlocks {
     public static final Block blockCopperOre = (new BlockOre(ConfigBlocks.BlockCopperOreID())).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("blockCopperOre").func_111022_d(IndustrialProcessing.TEXTURE_NAME_PREFIX + "blockCopperOre").setCreativeTab(IndustrialProcessing.tabOres);;
     public static final Block blockTinOre = (new BlockOre(ConfigBlocks.BlockTinOreID())).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("blockTinOre").func_111022_d(IndustrialProcessing.TEXTURE_NAME_PREFIX + "blockTinOre").setCreativeTab(IndustrialProcessing.tabOres);
     public static final Block blockGalenaOre = (new BlockOre(ConfigBlocks.BlockGalenaOreID())).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("blockGalenaOre").func_111022_d(IndustrialProcessing.TEXTURE_NAME_PREFIX + "blockGalenaOre").setCreativeTab(IndustrialProcessing.tabOres);
    public static final Block blockRutileOre = (new BlockOre(ConfigBlocks.BlockGalenaOreID())).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("blockRutileOre").func_111022_d(IndustrialProcessing.TEXTURE_NAME_PREFIX + "blockRutileOre").setCreativeTab(IndustrialProcessing.tabOres);
    public static final Block blockChromiteOre = (new BlockOre(ConfigBlocks.BlockGalenaOreID())).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("blockChromiteOre").func_111022_d(IndustrialProcessing.TEXTURE_NAME_PREFIX + "blockChromiteOre").setCreativeTab(IndustrialProcessing.tabOres);
    public static final Block blockCinnebarOre = (new BlockOre(ConfigBlocks.BlockGalenaOreID())).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("blockCinnebarOre").func_111022_d(IndustrialProcessing.TEXTURE_NAME_PREFIX + "blockCinnebarOre").setCreativeTab(IndustrialProcessing.tabOres);
    public static final Block blockTaliaOre = (new BlockOre(ConfigBlocks.BlockGalenaOreID())).setHardness(2.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("blockTaliaOre").func_111022_d(IndustrialProcessing.TEXTURE_NAME_PREFIX + "blockTaliaOre").setCreativeTab(IndustrialProcessing.tabOres);
 
     public final static BlockBase blockIronFlat = new BlockBase(ConfigBlocks.getInstance().blockIronFlat(), "ip.block.ironflat", 5.0f, Material.iron, Block.soundMetalFootstep);
     
     public final static BlockPlatform blockPlatform = new BlockPlatform();
 }
