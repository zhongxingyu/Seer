 package ip.industrialProcessing.machines.treetap;
 
 import ip.industrialProcessing.IndustrialProcessing;
 import ip.industrialProcessing.config.ConfigBlocks;
 import ip.industrialProcessing.config.ConfigMachineBlocks;
 import ip.industrialProcessing.config.ConfigRenderers;
 import ip.industrialProcessing.decoration.trees.BlockRubberLog;
 import ip.industrialProcessing.machines.BlockMachine;
 import ip.industrialProcessing.machines.BlockMachineRendered;
 import ip.industrialProcessing.machines.IRotateableEntity;
 import net.minecraft.block.StepSound;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 
 public class BlockManualTreeTap extends BlockMachineRendered {
 
     public BlockManualTreeTap() {
         super(ConfigMachineBlocks.getManualTreeTapBlockID(), Material.iron, 1.0f, soundMetalFootstep, "Manual Tree Tap", IndustrialProcessing.tabOreProcessing);
         func_111022_d(IndustrialProcessing.TEXTURE_NAME_PREFIX + "manualTreeTap");
     }
 
     @Override
     public boolean canPlaceBlockOnSide(World par1World, int par2, int par3, int par4, int par5) {
 
         ForgeDirection side = ForgeDirection.getOrientation(par5).getOpposite();
         int id = par1World.getBlockId(par2 + side.offsetX, par3 + side.offsetY, par4 + side.offsetZ);
         if (id == ConfigBlocks.getRubberLogID()) {
             return BlockRubberLog.isCarved(par1World, par2 + side.offsetX, par3 + side.offsetY, par4 + side.offsetZ, par5);
         } else
             return false;
     }
 
     @Override
     public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack) {
         // super.onBlockPlacedBy(world, x, y, z, entityLivingBase, itemStack);
 
         TileEntity entity = world.getBlockTileEntity(x, y, z);
         if (entity instanceof IRotateableEntity) {
 
             int dir = MathHelper.floor_double((double) ((entityLivingBase.rotationYaw * 4F) / 360F) + 0.5D) & 3;
             for (int i = dir; i < dir + 4; i++) {
                 int d = i % 4;
                 ForgeDirection direction = BlockMachine.getForwardFromMetadata(d);
 
                 boolean isConnected = canStayAt(world, x, y, z, direction);
                 if (isConnected) {
                     ((IRotateableEntity) entity).setForwardDirection(direction);
                     break;
                 }
             }
 
         }
     }
 
     private static boolean canStayAt(World world, int x, int y, int z, ForgeDirection direction) {
         int id = world.getBlockId(x - direction.offsetX, y - direction.offsetY, z - direction.offsetZ);
         if (id == ConfigBlocks.getRubberLogID()) {
             if (BlockRubberLog.isCarved(world, x - direction.offsetX, y - direction.offsetY, z - direction.offsetZ, direction.ordinal())) {
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public boolean canBlockStay(World par1World, int par2, int par3, int par4) {
         TileEntity entity = par1World.getBlockTileEntity(par2, par3, par4);
         ForgeDirection dir = BlockMachine.getForwardFromEntity(entity);
         return canStayAt(par1World, par2, par3, par4, dir);
     }
 
     /**
      * Called when a block is placed using its ItemBlock. Args: World, X, Y, Z,
      * side, hitX, hitY, hitZ, block metadata
      */
     @Override
     public int onBlockPlaced(World par1World, int par2, int par3, int par4, int par5, float par6, float par7, float par8, int par9) {
 
         return super.onBlockPlaced(par1World, par2, par3, par4, par5, par6, par7, par8, par9);
     }
 
     @Override
     public void setBlockBoundsBasedOnState(IBlockAccess par1iBlockAccess, int par2, int par3, int par4) {
         ForgeDirection direction = BlockMachine.getForwardFromEntity(par1iBlockAccess.getBlockTileEntity(par2, par3, par4));
         switch (direction) {
         case NORTH:
             setBlockBounds(2 / 16f, 2 / 16f, 12 / 16f, 8 / 16f, 6 / 16f, 1f);
             break;
         case EAST:
             setBlockBounds(0, 2 / 16f, 2 / 16f, 4 / 16f, 6 / 16f, 8 / 16f);
             break;
         case SOUTH:
             setBlockBounds(8 / 16f, 2 / 16f, 0, 14 / 16f, 6 / 16f, 4 / 16f);
             break;
         case WEST:
             setBlockBounds(12 / 16f, 2 / 16f, 8 / 16f, 1f, 6 / 16f, 14 / 16f);
             break;
         default:
             break;
         }
 
         super.setBlockBoundsBasedOnState(par1iBlockAccess, par2, par3, par4);
     }
 
     @Override
     public TileEntity createNewTileEntity(World world) {
         return new TileEntityManualTreeTap();
     }
 
     @Override
     public int getRenderType() {
         return ConfigRenderers.getRendererManualTreeTapID();
     }
 }
