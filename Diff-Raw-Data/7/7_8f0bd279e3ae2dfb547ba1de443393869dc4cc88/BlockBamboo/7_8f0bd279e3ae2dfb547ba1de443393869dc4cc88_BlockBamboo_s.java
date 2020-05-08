 package ruby.bamboo.block;
 
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemHoe;
 import net.minecraft.item.ItemStack;
 import net.minecraft.stats.StatList;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import net.minecraft.world.chunk.Chunk;
 import ruby.bamboo.BambooCore;
 import ruby.bamboo.BambooInit;
 import ruby.bamboo.CustomRenderHandler;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class BlockBamboo extends Block {
     // 最大成長値(0も含まれる)
     private static final int MAX_BAMBOO_LENGTH = 9;
     private Icon parent;
     private Icon child;
     private final String chiledName;
 
     public BlockBamboo(int i, String chiledName) {
         super(i, MaterialBamboo.instance);
         this.chiledName = chiledName;
         setLightOpacity(0);
         setTickRandomly(true);
         setHardness(0F);
         setResistance(0F);
     }
 
     @Override
     public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
         ItemStack is = par5EntityPlayer.getCurrentEquippedItem();
 
         if (is != null && is.itemID == Item.dyePowder.itemID && is.getItemDamage() == 15) {
             if (!par5EntityPlayer.capabilities.isCreativeMode) {
                 is.stackSize--;
             }
 
             if (!par1World.isRemote) {
                 par1World.playAuxSFX(2005, par2, par3, par4, 0);
             }
 
             int y = par3 + 1;
 
             for (; par1World.getBlockId(par2, y, par4) == this.blockID; y++) {
                 ;
             }
 
             tryBambooGrowth(par1World, par2, y - 1, par4, 0.75F);
             return true;
         }
 
         return false;
     }
 
     @Override
     public Icon getIcon(int side, int meta) {
         if (meta != 15) {
             return parent;
         }
 
         return child;
     }
 
     @Override
     public void updateTick(World world, int i, int j, int k, Random random) {
        tryBambooGrowth(world, i, j, k, world.isRaining() ? 0.1875F : 0.125F);
     }
 
     private void tryBambooGrowth(World world, int x, int y, int z, float probability) {
         if (!world.isRemote) {
             if (world.isAirBlock(x, y + 1, z)) {
                 if (world.rand.nextFloat() < probability) {
                     int meta = world.getBlockMetadata(x, y, z);
 
                     if (meta != 15) {
                         if (meta < MAX_BAMBOO_LENGTH) {
                             world.setBlock(x, y + 1, z, blockID, meta + 1, 3);
                         } else {
                             if (world.isRaining() || world.rand.nextFloat() < probability) {
                                 tryChildSpawn(world, x, y, z);
                             }
                         }
                     } else {
                         if (getBlockLightValue(world, x, y, z) > 7) {
                             world.setBlockMetadataWithNotify(x, y, z, 0, 3);
                         }
                     }
                 }
             }
         }
     }
 
     private void tryChildSpawn(World world, int i, int j, int k) {
         if (!world.isRemote) {
             j -= MAX_BAMBOO_LENGTH;
 
             for (int i1 = -1; i1 <= 1; i1++) {
                 for (int j1 = -1; j1 <= 1; j1++) {
                     for (int k1 = -1; k1 <= 1; k1++) {
                         if (canChildSpawn(world, i + i1, j + j1, k + k1, world.rand)) {
                            world.setBlockMetadataWithNotify(i + i1, j + j1 - 1, k + k1, Block.dirt.blockID, 0);
                            world.setBlockMetadataWithNotify(i + i1, j + j1, k + k1, blockID, 15);
                         }
                     }
                 }
             }
         }
     }
 
     private int getBlockLightValue(World world, int i, int j, int k) {
         Chunk chunk = world.getChunkFromChunkCoords(i >> 4, k >> 4);
         i &= 0xf;
         k &= 0xf;
         return chunk.getBlockLightValue(i, j, k, 15);
     }
 
     private boolean canChildSpawn(World world, int i, int j, int k, Random random) {
         if (world.isAirBlock(i, j, k)) {
             // 天候・耕地確変
             if (random.nextFloat() < (world.isRaining() ? 0.4F : world.getBlockId(i, j - 1, k) == Block.tilledField.blockID ? 0.25F : 0.1F)) {
                 if ((world.getBlockId(i, j - 1, k) == Block.dirt.blockID || world.getBlockId(i, j - 1, k) == Block.grass.blockID || world.getBlockId(i, j - 1, k) == Block.tilledField.blockID)) {
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     @Override
     public void harvestBlock(World world, EntityPlayer entityplayer, int i, int j, int k, int l) {
         if (!world.isRemote) {
             if (l == 15) {
                 if (entityplayer.getCurrentEquippedItem() != null && (entityplayer.getCurrentEquippedItem().getItem() instanceof ItemHoe)) {
                     entityplayer.addStat(StatList.mineBlockStatArray[blockID], 1);
                     entityplayer.getCurrentEquippedItem().damageItem(1, entityplayer);
                     dropBlockAsItem_do(world, i, j, k, new ItemStack(BambooInit.takenokoIID, 1, 0));
                 }
             } else {
                 super.harvestBlock(world, entityplayer, i, j, k, l);
             }
         }
     }
 
     @Override
     public boolean canPlaceBlockAt(World world, int i, int j, int k) {
         return false;
     }
 
     @Override
     public void onNeighborBlockChange(World world, int i, int j, int k, int l) {
         checkBlockCoordValid(world, i, j, k);
     }
 
     @Override
     public void breakBlock(World par1World, int par2, int par3, int par4, int par5, int par6) {
         if (par1World.getBlockId(par2, par3 - par6, par4) == this.blockID) {
             dropBlockAsItem(par1World, par2, par3, par4, 0, BambooInit.bambooIID);
             par1World.setBlock(par2, par3 - par6, par4, 0, 0, 3);
         }
     }
 
     protected final void checkBlockCoordValid(World world, int i, int j, int k) {
         if (!canBlockStay(world, i, j, k)) {
             if (world.getBlockMetadata(i, j, k) != 15) {
                 dropBlockAsItem(world, i, j, k, 0, BambooInit.bambooIID);
             }
 
             world.setBlock(i, j, k, 0, 0, 3);
         }
     }
 
     @Override
     public boolean canBlockStay(World world, int i, int j, int k) {
         int l = world.getBlockId(i, j - 1, k);
 
         if (l == blockID && world.getBlockMetadata(i, j - 1, k) == 15) {
             return false;
         } else if (l == blockID) {
             return true;
         }
 
         if (l != Block.grass.blockID && l != Block.dirt.blockID) {
             return false;
         }
 
         return true;
     }
 
     @Override
     public int idDropped(int par1, Random par2Random, int par3) {
         return BambooInit.bambooIID;
     }
 
     @Override
     public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int i, int j, int k) {
         return null;
     }
 
     @Override
     public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int par2, int par3, int par4) {
         if (par1IBlockAccess.getBlockMetadata(par2, par3, par4) == 15) {
             setBlockBounds(0.3F, 0.0F, 0.3F, 0.7F, 0.5F, 0.7F);
         } else {
             setBlockBounds(0.5F - 0.375F, 0.0F, 0.5F - 0.375F, 0.5F + 0.375F, 1.0F, 0.5F + 0.375F);
         }
     }
 
     @SideOnly(Side.CLIENT)
     @Override
     public void registerIcons(IconRegister par1IconRegister) {
         this.parent = par1IconRegister.registerIcon(BambooCore.resourceDomain + "bamboo");
         this.child = par1IconRegister.registerIcon(BambooCore.resourceDomain + this.chiledName);
     }
 
     @Override
     public boolean isOpaqueCube() {
         return false;
     }
 
     @Override
     public boolean renderAsNormalBlock() {
         return false;
     }
 
     @Override
     public int getRenderType() {
         return CustomRenderHandler.bambooUID;
     }
 
     @Override
     public int getMobilityFlag() {
         return 1;
     }
 
     @Override
     public int idPicked(World par1World, int par2, int par3, int par4) {
         return BambooInit.bambooIID;
     }
 }
