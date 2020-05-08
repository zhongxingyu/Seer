 package teamm.mods.virtious.lib;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.common.Property;
 import teamm.mods.virtious.Config;
 import teamm.mods.virtious.Virtious;
 import teamm.mods.virtious.block.BlockVirtianGrass;
 import teamm.mods.virtious.block.DeepStone;
 import teamm.mods.virtious.block.PortalBlock;
 import teamm.mods.virtious.block.VirtiousBlock;
 
 public class VirtiousBlocks 
 {
 	public static Block deepStone;
 	public static Block portalBlock;
 	public static Block virtianstone;
 	public static Block virtianGrass;
 	public static Block virtianSoil;
 	public static Block deepStoneMossy;
 	public static Block oreVIron;
 	public static Block oreTak;
 	
 	/**
 	 * Loads all block objects
 	 */
 	public VirtiousBlocks()
 	{
 		Property idDeedStone = Virtious.config.getBlock("idDeepStone", Config.idDeedStone);
 		int deepStoneID = idDeedStone.getInt();
 		deepStone = new DeepStone(deepStoneID, Material.rock).setHardness(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("DeepStone");
 		registerBlock(deepStone, "Deep Stone");
 		
 		Property idPortalBlock = Virtious.config.getBlock("idPortalBlock", Config.idPortal);
 		int portalBlockID = idPortalBlock.getInt();
 		portalBlock = new PortalBlock(portalBlockID).setHardness(1.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("vPortal");
 		registerBlock(portalBlock, "Portal");
 		
 		Property idVirtianstone = Virtious.config.getBlock("Virtianstone Id", Config.idVirtianstone);
 		int virtianstoneID = idVirtianstone.getInt();
 		virtianstone = new VirtiousBlock(virtianstoneID, Material.rock).setHardness(1.5F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("Virtianstone");
 		registerBlock(virtianstone, "Virtianstone");
 		
 		Property idVirtianGrass = Virtious.config.getBlock("Virtian Grass ID", Config.idVirtianGrass);
 		int virtianGrassId = idVirtianGrass.getInt();
 		virtianGrass = new BlockVirtianGrass(virtianGrassId).setHardness(0.6F).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("virtianGrass");
 		registerBlock(virtianGrass, "Virtian Grass");
 		
 		Property idVirtianSoil = Virtious.config.getBlock("Virtian Soil ID", Config.idVirtianSoil);
 		int virtianSoilId = idVirtianSoil.getInt();
 		virtianSoil = new VirtiousBlock(virtianSoilId, Material.ground).setHardness(0.6F).setUnlocalizedName("VirtianSoil").setStepSound(Block.soundGravelFootstep);
 		registerBlock(virtianSoil, "Virtian Soil");
 		
 		Property iddeepStoneMossy = Virtious.config.getBlock("Mossy Deep Stone ID", Config.iddeepStoneMossy);
 		int deepStoneMossyId = iddeepStoneMossy.getInt();
 		deepStoneMossy = new VirtiousBlock(deepStoneMossyId, Material.rock).setHardness(5.0F).setResistance(8.5F).setUnlocalizedName("MossyDeepStone");
 		registerBlock(deepStoneMossy, "Mossy Deep Stone");
 		
 		Property idoreVIron = Virtious.config.getBlock("Virtian Iron Ore ID", Config.idoreVIron);
 		int oreVIronId = idoreVIron.getInt();
 		oreVIron = new VirtiousBlock(oreVIronId, Material.rock).setHardness(3.0F).setResistance(5.0F).setUnlocalizedName("VirtianIronOre");
 		registerBlock(oreVIron, "Virtian Iron Ore");
 		
 		Property idoreTak = Virtious.config.getBlock("Tak Ore Id", Config.idoreTak);
 		int oreTakId = idoreTak.getInt();
 		oreTak = new VirtiousBlock(oreTakId, Material.rock).setHardness(3.2F).setResistance(5.2F).setUnlocalizedName("TakOre");
		registerBlock(oreTak,"Tak Ore");
		
		MinecraftForge.setBlockHarvestLevel(oreTak, "pickaxe", 1);
 		
 		//TODO add all block objects here
 	}
 	
 	public void registerBlock(Block block, String name)
 	{
 		GameRegistry.registerBlock(block, name);
 		LanguageRegistry.addName(block, name);
 	}
 }
