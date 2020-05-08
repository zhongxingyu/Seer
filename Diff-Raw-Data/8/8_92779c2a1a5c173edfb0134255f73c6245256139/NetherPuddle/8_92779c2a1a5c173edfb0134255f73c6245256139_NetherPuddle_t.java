 package bstramke.NetherStuffs.Blocks;
 
 import java.util.List;
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import bstramke.NetherStuffs.Common.CommonProxy;
 import bstramke.NetherStuffs.Common.NetherPuddleMaterial;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class NetherPuddle extends Block {
 	public static final int hellfire = 0;
 	public static final int acid = 1;
 	public static final int death = 2;
 
 	public NetherPuddle(int par1, int par2) {
 		super(par1, par2, NetherPuddleMaterial.netherPuddle);
 		this.setCreativeTab(CreativeTabs.tabBlock);
 		this.setRequiresSelfNotify();
 		this.setLightOpacity(0);
 		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.015625F, 1.0F);
 		this.setTickRandomly(true);
 		this.setCreativeTab(CreativeTabs.tabDecorations);
 	}
 
 	@Override
 	public int getRenderType() {
 		return 0;
 	}
 
 	@Override
 	public String getTextureFile() {
 		return CommonProxy.PUDDLES_PNG;
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public boolean shouldSideBeRendered(IBlockAccess par1iBlockAccess, int par2, int par3, int par4, int par5) {
 		return true;
 	}
 
 	public static void placePuddleWithType(World par1World, int par2, int par3, int par4, int type) {
 		par1World.setBlockAndMetadataWithNotify(par2, par3, par4, NetherBlocks.netherPuddle.blockID, type);
 	}
 
 	public int getMetadataSize() {
 		return NetherPuddleItemBlock.blockNames.length;
 	}
 
 	protected boolean canThisPlantGrowOnThisBlockID(int par1) {
 		return par1 == Block.netherrack.blockID || par1 == Block.slowSand.blockID || par1 == NetherBlocks.netherOre.blockID;
 	}
 
 	@Override
 	public boolean canPlaceBlockAt(World par1World, int par2, int par3, int par4) {
 		int blockId = par1World.getBlockId(par2, par3 - 1, par4);
 		return canBlockStay(par1World, par2, par3, par4) && canThisPlantGrowOnThisBlockID(blockId);
 	}
 
 	private static final int METADATA_BITMASK = 0x3; // Differences between
 	// Hellfire, Acid, Death
 	private static final int METADATA_SIZEBITMASK = 0xc; // Bits 3&4 to Store
 	// the Size Value
 	// (0-3)
 	private static final int METADATA_SIZEBITSOFFSET = 0x2; // the Shifting
 	// Amount to convert
 	// the Size from 0-3
 	// to 0/8/16/24
 
 	private static final int METADATA_CLEARSIZEBITMASK = -METADATA_SIZEBITMASK - 1;
 
 	private static int clearSizeOnMetadata(int metadata) {
 		return metadata & METADATA_CLEARSIZEBITMASK;
 	}
 
 	private static int getSizeForMetadata(int size) {
 		return size << METADATA_SIZEBITSOFFSET;
 	}
 
 	public static int getSizeFromMetadata(int metadata) {
 		return (metadata & METADATA_SIZEBITMASK) >> METADATA_SIZEBITSOFFSET;
 	}
 
 	public static int setMetadataSize(int metadata, int size) {
 		return metadata | (METADATA_SIZEBITMASK & getSizeForMetadata(size));
 	}
 
 	public static int unmarkedMetadata(int metadata) {
 		return metadata & METADATA_BITMASK;
 	}
 
 	@Override
 	public int getBlockTextureFromSideAndMetadata(int side, int meta) {
 		switch (clearSizeOnMetadata(meta)) {
 		case hellfire:
 			return hellfire * 16 + getSizeFromMetadata(meta);
 		case acid:
 			return acid * 16 + getSizeFromMetadata(meta);
 		case death:
 			return death * 16 + getSizeFromMetadata(meta);
 		default:
 			return hellfire * 16 + getSizeFromMetadata(meta);
 		}
 	}
 
 	public static void removePuddle(World par1World, int par2, int par3, int par4) {
 		// int metadata = unmarkedMetadata(par1World.getBlockMetadata(par2,
 		// par3, par4));
 		par1World.setBlockWithNotify(par2, par3, par4, 0);
 		// dropBlockAsItem(par1World, par2, par3, par4, metadata, 0);
 	}
 
 	@Override
 	public void updateTick(World par1World, int par2, int par3, int par4, Random par5Random) {
 		if (!par1World.isRemote) {
 			if (!canBlockStay(par1World, par2, par3, par4, true)) {
 				removePuddle(par1World, par2, par3, par4);
 			}
 		}
 	}
 
 	public static void growPuddle(World par1World, int par2, int par3, int par4) {
 		if (par1World.provider.isHellWorld) {
 			int metadata = par1World.getBlockMetadata(par2, par3, par4);
 			int type = unmarkedMetadata(metadata);
 			int size = getSizeFromMetadata(metadata) + 1;
 			if (size < 4)
 				par1World.setBlockMetadataWithNotify(par2, par3, par4, setMetadataSize(metadata, size));
 		}
 	}
 
	@Override
   public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
   {
       return null;
   }
	
 	protected static boolean canBlockStay(World par1World, int par2, int par3, int par4, int puddleMeta, boolean checkPuddleMeta) {
 		boolean bValid = true;
 		if (!par1World.provider.isHellWorld) // only allow in Nether
 			return false;
 		if (par3 >= 0 && par3 < 256) {
 			if (par1World.getBlockId(par2, par3 - 1, par4) != Block.netherrack.blockID) // If its anything else than netherrack as base it wont work
 				return false;
 
 			for (int y = par3 + 1; y < 256 && bValid; y++) { // search for netherleaves
 				if (!par1World.isAirBlock(par2, y, par4)) {
 					if (par1World.getBlockId(par2, y, par4) == NetherBlocks.netherLeaves.blockID) {
 						if (checkPuddleMeta) {
 							if (NetherLeaves.unmarkedMetadata(par1World.getBlockMetadata(par2, y, par4)) != puddleMeta)
 								bValid = false;
 							else
 								break;
 						} else
 							break; // we found a valid Top for the Puddle
 					} else
 						bValid = false;
 				}
 			}
 
 			return bValid;
 
 		} else
 			return false;
 	}
 
 	@Override
 	public boolean canBlockStay(World par1World, int par2, int par3, int par4) {
 		if (par1World.isAirBlock(par2, par3 - 1, par4))
 			return false;
 		int puddleMeta = NetherPuddle.unmarkedMetadata(par1World.getBlockMetadata(par2, par3, par4));
 		return canBlockStay(par1World, par2, par3, par4, puddleMeta, false);
 	}
 
 	public static boolean canBlockStay(World par1World, int par2, int par3, int par4, boolean CheckPuddleMeta) {
 		if (par1World.isAirBlock(par2, par3 - 1, par4))
 			return false;
 
 		boolean bFoundLeaves = false;
 		for (int yCoord = par3; yCoord < 256 && bFoundLeaves == false; yCoord++) {
 			if (par1World.getBlockId(par2, yCoord, par4) == NetherBlocks.netherLeaves.blockID) {
 				bFoundLeaves = true;
 				break;
 			}
 		}
 
 		if (bFoundLeaves == false)
 			return false;
 
 		int puddleMeta = NetherPuddle.unmarkedMetadata(par1World.getBlockMetadata(par2, par3, par4));
 		return canBlockStay(par1World, par2, par3, par4, puddleMeta, CheckPuddleMeta);
 	}
 
 	@Override
 	public int damageDropped(int meta) {
 		return meta;
 	}
 
 	@Override
 	public boolean isOpaqueCube() {
 		return false;
 	}
 
 	/**
 	 * If this block doesn't render as an ordinary block it will return False (examples: signs, buttons, stairs, etc)
 	 */
 	@Override
 	public boolean renderAsNormalBlock() {
 		return false;
 	}
 
 	@SideOnly(Side.CLIENT)
 	public void getSubBlocks(int par1, CreativeTabs tab, List list) {
 		for (int metaNumber = 0; metaNumber < getMetadataSize(); metaNumber++) {
 			list.add(new ItemStack(par1, 1, metaNumber));
 		}
 	}
 }
