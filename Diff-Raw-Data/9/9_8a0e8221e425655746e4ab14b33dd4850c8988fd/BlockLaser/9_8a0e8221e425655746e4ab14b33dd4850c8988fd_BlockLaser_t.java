 package net.minecraft.src.redstoneExtended;
 
 import net.minecraft.src.*;
 
 import java.util.Random;
 
 public class BlockLaser extends BlockContainer implements ILaserEmitter {
     public BlockLaser(int id) {
         super(id, Block.glass.blockIndexInTexture, Materials.laser);
     }
 
     @Override
     public int idDropped(int metadata, Random random) {
         return -1;
     }
 
     @Override
     public int tickRate() {
         return 1;
     }
 
     @Override
     public boolean isOpaqueCube() {
         return false;
     }
 
     @Override
     public void setBlockBoundsBasedOnState(IBlockAccess iBlockAccess, int x, int y, int z) {
         byte orientation = ((TileEntityLaser)iBlockAccess.getBlockTileEntity(x, y, z)).orientation;
         float width = ((TileEntityLaser)iBlockAccess.getBlockTileEntity(x, y, z)).mode.width;
         float min = 0.5F - (width / 2F);
         float max = 0.5F + (width / 2F);
         switch (orientation) {
             case 0:
             case 1:
                 setBlockBounds(min, 0.0f, min, max, 1.0f, max);
                 break;
             case 2:
             case 3:
                 setBlockBounds(min, min, 0.0f, max, max, 1.0f);
                 break;
             case 4:
             case 5:
                 setBlockBounds(0.0f, min, min, 1.0f, max, max);
                 break;
         }
     }
 
     @Override
     public int colorMultiplier(IBlockAccess iBlockAccess, int x, int y, int z) {
         TileEntityLaser tileEntityLaser = (TileEntityLaser)iBlockAccess.getBlockTileEntity(x, y, z);
        return ((tileEntityLaser.mode.colorR & 0xff) << 16) | ((tileEntityLaser.mode.colorG & 0xff) << 8) | (tileEntityLaser.mode.colorB & 0xff);
     }
 
     @Override
     public int getBlockTexture(IBlockAccess iBlockAccess, int x, int y, int z, int side) {
         return ((TileEntityLaser)iBlockAccess.getBlockTileEntity(x, y, z)).mode.texture;
     }
 
     @Override
     public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
         if (((TileEntityLaser)world.getBlockTileEntity(x, y, z)).mode.collision)
             return super.getCollisionBoundingBoxFromPool(world, x, y, z);
         else
             return null;
     }
 
     @Override
     public boolean canBlockStay(World world, int x, int y, int z) {
         int orientation = ((TileEntityLaser)world.getBlockTileEntity(x, y, z)).orientation;
         Position parentPos = new Position(x, y, z).positionMoveInDirection(Util.invertDirection(orientation));
         int parentBlockId = world.getBlockId(parentPos.X, parentPos.Y, parentPos.Z);
         return ((Block.blocksList[parentBlockId] instanceof ILaserEmitter) && ((ILaserEmitter)Block.blocksList[parentBlockId]).isProvidingLaserPowerInDirection(world, parentPos.X, parentPos.Y, parentPos.Z, orientation));
     }
 
     @Override
     public void onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockId) {
         if (isBlockUpdateNecessary(world, x, y, z))
             world.scheduleBlockUpdate(x, y, z, blockID, tickRate());
     }
 
     private boolean isBlockUpdateNecessary(World world, int x, int y, int z) {
         if (!canBlockStay(world, x, y, z))
             return true;
 
         Position parentPos = new Position(x, y, z).positionMoveInDirection(Util.invertDirection(getOrientation(world, x, y, z)));
         int parentBlockId = world.getBlockId(parentPos.X, parentPos.Y, parentPos.Z);
 
         return (!((ILaserEmitter)Block.blocksList[parentBlockId]).getLaserModeProvidedInDirection(world, parentPos.X, parentPos.Y, parentPos.Z, getOrientation(world, x, y, z)).equals(getLaserMode(world, x, y, z))) ||
                 LaserUtils.isBlockUpdateForLaserInDirectionNecessary(world, x, y, z, getOrientation(world, x, y, z));
     }
 
     private boolean hasNotMaximumLength(IBlockAccess iBlockAccess, int x, int y, int z) {
         return getDistance(iBlockAccess, x, y, z) < 30;
     }
 
     public static byte getOrientation(IBlockAccess iBlockAccess, int x, int y, int z) {
         return ((TileEntityLaser)iBlockAccess.getBlockTileEntity(x, y, z)).orientation;
     }
 
     public static void setOrientation(World world, int x, int y, int z, byte orientation) {
         ((TileEntityLaser)world.getBlockTileEntity(x, y, z)).orientation = orientation;
     }
 
     public static short getDistance(IBlockAccess iBlockAccess, int x, int y, int z) {
         return ((TileEntityLaser)iBlockAccess.getBlockTileEntity(x, y, z)).distance;
     }
 
     public static void setDistance(World world, int x, int y, int z, short distance) {
         ((TileEntityLaser)world.getBlockTileEntity(x, y, z)).distance = distance;
     }
 
     public static LaserMode getLaserMode(IBlockAccess iBlockAccess, int x, int y, int z) {
         return ((TileEntityLaser)iBlockAccess.getBlockTileEntity(x, y, z)).mode;
     }
 
     public static void setLaserMode(World world, int x, int y, int z, LaserMode laserMode) {
         ((TileEntityLaser)world.getBlockTileEntity(x, y, z)).mode = laserMode;
     }
 
     @Override
     public void onBlockAdded(World world, int x, int y, int z) {
         super.onBlockAdded(world, x, y, z);
 
         world.scheduleBlockUpdate(x, y, z, blockID, tickRate());
     }
 
     @Override
     public void onBlockRemoval(World world, int x, int y, int z) {
         super.onBlockRemoval(world, x, y, z);
 
         world.notifyBlocksOfNeighborChange(x, y, z, blockID);
     }
 
     @Override
     public void updateTick(World world, int x, int y, int z, Random random) {
         if (!canBlockStay(world, x, y, z)) {
             world.setBlockWithNotify(x, y, z, 0);
             return;
         }
 
         Position parentPos = new Position(x, y, z).positionMoveInDirection(Util.invertDirection(getOrientation(world, x, y, z)));
         int parentBlockId = world.getBlockId(parentPos.X, parentPos.Y, parentPos.Z);
         if (!((ILaserEmitter)Block.blocksList[parentBlockId]).getLaserModeProvidedInDirection(world, parentPos.X, parentPos.Y, parentPos.Z, getOrientation(world, x, y, z)).equals(getLaserMode(world, x, y, z))) {
             setLaserMode(world, x, y, z, ((ILaserEmitter)Block.blocksList[parentBlockId]).getLaserModeProvidedInDirection(world, parentPos.X, parentPos.Y, parentPos.Z, getOrientation(world, x, y, z)));
         }
 
         LaserUtils.blockUpdateForLaserInDirection(world, x, y, z, getOrientation(world, x, y, z));
     }
 
     @Override
     public float getBlockBrightness(IBlockAccess iBlockAccess, int x, int y, int z) {
         return 1f;
     }
 
     @Override
     public TileEntity getBlockEntity() {
         return new TileEntityLaser();
     }
 
     @Override
     public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
         short damage = ((TileEntityLaser)world.getBlockTileEntity(x, y, z)).mode.damage;
         if (damage > 0)
             entity.attackEntityFrom(null, damage);
     }
 
     @Override
     public boolean canProvideLaserPowerInDirection(IBlockAccess iBlockAccess, int x, int y, int z, int direction) {
         return (getOrientation(iBlockAccess, x, y, z) == direction);
     }
 
     @Override
     public boolean isProvidingLaserPowerInDirection(IBlockAccess iBlockAccess, int x, int y, int z, int direction) {
         return (getOrientation(iBlockAccess, x, y, z) == direction) && hasNotMaximumLength(iBlockAccess, x, y, z);
     }
 
     @Override
     public LaserMode getLaserModeProvidedInDirection(IBlockAccess iBlockAccess, int x, int y, int z, int direction) {
         if (!isProvidingLaserPowerInDirection(iBlockAccess, x, y, z, direction))
             return null;
 
         return getLaserMode(iBlockAccess, x, y, z);
     }
 
     @Override
     public short getInitialDistanceProvidedInDirection(IBlockAccess iBlockAccess, int x, int y, int z, int direction) {
         if (!isProvidingLaserPowerInDirection(iBlockAccess, x, y, z, direction))
             return 0;
 
         return (short)(getDistance(iBlockAccess, x, y, z) + 1);
     }
 }
