 package net.minecraft.src.redstoneExtended.Laser;
 
 import net.minecraft.src.*;
 import net.minecraft.src.redstoneExtended.IBlockWithOverlayEx;
 import net.minecraft.src.redstoneExtended.Util.*;
 
 import java.util.Random;
 
 public class BlockLaserFocusLens extends BlockContainer implements ILaserEmitter, IBlockWithOverlayEx {
     public BlockLaserFocusLens(int id) {
         super(id, Block.dispenser.blockIndexInTexture + 17, Material.rock);
     }
 
     public final static int textureFrontDefault = TextureManager.getInstance().getTerrainTexture("/laserEmitter/frontDefault.png");
     public final static int textureFrontDeadly = TextureManager.getInstance().getTerrainTexture("/laserEmitter/frontDeadly.png");
     public final static int textureFrontBridge = TextureManager.getInstance().getTerrainTexture("/laserEmitter/frontBridge.png");
     public final static int textureSideLaserStrip = TextureManager.getInstance().getTerrainTexture("/laserEmitter/sideLaserStrip.png");
     public final static int textureSideLaserStripOverlay = TextureManager.getInstance().getTerrainTexture("/laserEmitter/sideLaserStripOverlay.png");
     public final static int textureBack = TextureManager.getInstance().getTerrainTexture("/laserReceiver/back.png");
     public final static int textureBackOverlay = TextureManager.getInstance().getTerrainTexture("/laserReceiver/backOverlay.png");
 
     public final static int textureFrontInv = TextureManager.getInstance().getTerrainTexture("/laserEmitter/frontInv.png");
     public final static int textureSideInv = TextureManager.getInstance().getTerrainTexture("/laserEmitter/sideDisInv.png");
     public final static int textureSideRotInv = TextureManager.getInstance().getTerrainTexture("/laserEmitter/sideDisRotInv.png");
 
     public final static net.minecraft.src.redstoneExtended.Laser.LaserShape[] operatingModes;
 
     static {
         operatingModes = new net.minecraft.src.redstoneExtended.Laser.LaserShape[] {
                 LaserShapes.Default,
                 LaserShapes.Deadly,
                 LaserShapes.Bridge
         };
     }
 
     @Override
     public TileEntity getBlockEntity() {
         return new net.minecraft.src.redstoneExtended.Laser.TileEntityLaserFocusLens();
     }
 
     @Override
     public int getRenderType() {
         return mod_redstoneExtended.getInstance().renderStandardBlockWithOverlay;
     }
 
     @Override
     public boolean shouldOverlayBeRendered(IBlockAccess iBlockAccess, int x, int y, int z, int side, int layer) {
         return (layer == 1 && side == DirectionUtil.invertDirection(getOrientation(iBlockAccess, x, y, z)) &&
                 getOperatingMode(iBlockAccess, x, y, z) != 2) ||
                 (side != DirectionUtil.invertDirection(getOrientation(iBlockAccess, x, y, z)) &&
                         (layer == 1 || layer == 2));
     }
 
     @Override
     public int getBlockOverlayTexture(IBlockAccess iBlockAccess, int x, int y, int z, int side, int layer) {
         if (side == DirectionUtil.invertDirection(getOrientation(iBlockAccess, x, y, z)))
             switch (getOperatingMode(iBlockAccess, x, y, z)) {
                 case 0:
                     return textureFrontDefault;
                 case 1:
                     return textureFrontDeadly;
                 default:
                     return mod_redstoneExtended.getInstance().emptyTexture;
             }
         else if (side == getOrientation(iBlockAccess, x, y, z))
             if (layer == 1)
                 return textureBack;
             else
                 return textureBackOverlay;
         else if (layer == 1)
             return textureSideLaserStrip;
         else
             return textureSideLaserStripOverlay;
     }
 
     @Override
     public Vector3d getOverlayOffset(IBlockAccess iBlockAccess, int x, int y, int z, int side, int layer) {
         return new Vector3d(0D);
     }
 
     @Override
     public Vector3d getOverlayScale(IBlockAccess iBlockAccess, int x, int y, int z, int side, int layer) {
         return new Vector3d(1D);
     }
 
     @Override
     public double getOverlayRotation(IBlockAccess iBlockAccess, int x, int y, int z, int side, int layer) {
         if (side > 1)
             return 0D;
 
         int rotation = 0;
         int orientation = getOrientation(iBlockAccess, x, y, z);
 
         switch (orientation) {
             case 2:
                 rotation = 2;
                 break;
             case 3:
                 rotation = 0;
                 break;
             case 4:
                 rotation = 1;
                 break;
             case 5:
                 rotation = 3;
                 break;
         }
 
         return ((double)(rotation + 1) * 90D);
     }
 
     @Override
     public Vector2d getOverlayTextureOffset(IBlockAccess iBlockAccess, int x, int y, int z, int side, int layer) {
         return new Vector2d(0D);
     }
 
     @Override
     public Vector2d getOverlayTextureScale(IBlockAccess iBlockAccess, int x, int y, int z, int side, int layer) {
         return new Vector2d(1D);
     }
 
     @Override
     public ColorRGB getOverlayColorMultiplier(IBlockAccess iBlockAccess, int x, int y, int z, int side, int layer) {
         return (side != DirectionUtil.invertDirection(getOrientation(iBlockAccess, x, y, z)) && layer == 1 &&
                 getState(iBlockAccess, x, y, z)) ? getLaserMode(iBlockAccess, x, y, z).color : ColorRGB.Colors.Gray;
     }
 
     @Override
     public boolean shouldOverlayIgnoreLighting(IBlockAccess iBlockAccess, int x, int y, int z, int side, int layer) {
         return (side != DirectionUtil.invertDirection(getOrientation(iBlockAccess, x, y, z)) && layer == 1 &&
                 getState(iBlockAccess, x, y, z));
     }
 
     @Override
     public int tickRate() {
         return 1;
     }
 
     @Override
     public int getBlockTexture(IBlockAccess iBlockAccess, int x, int y, int z, int side) {
         int orientation = getOrientation(iBlockAccess, x, y, z);
         return (side == orientation && getOperatingMode(iBlockAccess, x, y, z) == 2) ? textureFrontBridge : blockIndexInTexture;
     }
 
     @Override
     public int getBlockTextureFromSide(int side) {
         switch (side) {
             case 0:
             case 1:
                 return textureSideRotInv;
             case 3:
                 return textureFrontInv;
             default:
                 return textureSideInv;
         }
     }
 
     @Override
     public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving creator) {
         setOrientation(world, x, y, z, DirectionUtil.getOrientationFromPlayer(creator));
     }
 
     @Override
     public void onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockId) {
         if (isBlockUpdateNecessary(world, x, y, z))
             world.scheduleBlockUpdate(x, y, z, blockID, tickRate());
     }
 
     private boolean isBlockUpdateNecessary(World world, int x, int y, int z) {
         return (getState(world, x, y, z) != updateInputState(world, x, y, z)) ||
                 net.minecraft.src.redstoneExtended.Laser.LaserUtil.isBlockUpdateForLaserInDirectionNecessary(world, x, y, z, getOrientation(world, x, y, z));
     }
 
     @Override
     public void updateTick(World world, int x, int y, int z, Random random) {
        if (getState(world, x, y, z) != updateInputState(world, x, y, z)) {
             setState(world, x, y, z, !getState(world, x, y, z));
            world.markBlocksDirty(x, y, z, x, y, z);
        }
 
         net.minecraft.src.redstoneExtended.Laser.LaserUtil.blockUpdateForLaserInDirection(world, x, y, z, getOrientation(world, x, y, z));
     }
 
     @Override
     public boolean blockActivated(World world, int x, int y, int z, EntityPlayer player) {
         setOperatingMode(world, x, y, z, getOperatingMode(world, x, y, z) >= 2 ? (byte)0 : (byte)(getOperatingMode(world, x, y, z) + 1));
 
         world.markBlocksDirty(x, y, z, x, y, z);
         world.scheduleBlockUpdate(x, y, z, blockID, tickRate());
         world.notifyBlocksOfNeighborChange(x, y, z, blockID);
 
         return true;
     }
 
     protected boolean updateInputState(IBlockAccess iBlockAccess, int x, int y, int z) {
         int orientation = getOrientation(iBlockAccess, x, y, z);
         net.minecraft.src.redstoneExtended.Util.Position sourcePos = new net.minecraft.src.redstoneExtended.Util.Position(x, y, z).positionMoveInDirection(DirectionUtil.invertDirection(orientation));
         int sourceBlockId = iBlockAccess.getBlockId(sourcePos.X, sourcePos.Y, sourcePos.Z);
         if ((Block.blocksList[sourceBlockId] instanceof net.minecraft.src.redstoneExtended.Laser.ILaserEmitter) && (((net.minecraft.src.redstoneExtended.Laser.ILaserEmitter)Block.blocksList[sourceBlockId]).isProvidingLaserPowerInDirection(iBlockAccess, sourcePos.X, sourcePos.Y, sourcePos.Z, orientation))) {
             setLaserMode(iBlockAccess, x, y, z, ((net.minecraft.src.redstoneExtended.Laser.ILaserEmitter)Block.blocksList[sourceBlockId]).getLaserModeProvidedInDirection(iBlockAccess, sourcePos.X, sourcePos.Y, sourcePos.Z, orientation).getClone());
             getLaserMode(iBlockAccess, x, y, z).shape = operatingModes[getOperatingMode(iBlockAccess, x, y, z)].getClone();
             setDistance(iBlockAccess, x, y, z, ((net.minecraft.src.redstoneExtended.Laser.ILaserEmitter)Block.blocksList[sourceBlockId]).getInitialDistanceProvidedInDirection(iBlockAccess, sourcePos.X, sourcePos.Y, sourcePos.Z, orientation));
             return true;
         }
         return false;
     }
 
     public static byte getOperatingMode(IBlockAccess iBlockAccess, int x, int y, int z) {
         return ((net.minecraft.src.redstoneExtended.Laser.TileEntityLaserFocusLens)iBlockAccess.getBlockTileEntity(x, y, z)).operatingMode;
     }
 
     public static void setOperatingMode(IBlockAccess iBlockAccess, int x, int y, int z, byte operatingMode) {
         ((net.minecraft.src.redstoneExtended.Laser.TileEntityLaserFocusLens)iBlockAccess.getBlockTileEntity(x, y, z)).operatingMode = operatingMode;
     }
 
     public static boolean getStateFromMetadata(int metadata) {
         return ((metadata & 0x8) >> 3) == 1;
     }
 
     public static byte getOrientationFromMetadata(int metadata) {
         return (byte)(metadata & 0x7);
     }
 
     public static int setStateInMetadata(int metadata, boolean state) {
         return ((metadata & 0x7) | ((state ? 1 : 0) << 3) & 0x8);
     }
 
     public static int setOrientationInMetadata(int metadata, int orientation) {
         return ((metadata & 0x8) | (orientation & 0x7));
     }
 
     public static boolean getState(IBlockAccess iBlockAccess, int x, int y, int z) {
         int metadata = iBlockAccess.getBlockMetadata(x, y, z);
         return getStateFromMetadata(metadata);
     }
 
     public static byte getOrientation(IBlockAccess iBlockAccess, int x, int y, int z) {
         int metadata = iBlockAccess.getBlockMetadata(x, y, z);
         return getOrientationFromMetadata(metadata);
     }
 
     public static void setState(World world, int x, int y, int z, boolean state) {
         int oldMetadata = world.getBlockMetadata(x, y, z);
         int newMetadata = setStateInMetadata(oldMetadata, state);
         world.setBlockMetadataWithNotify(x, y, z, newMetadata);
     }
 
     public static void setOrientation(World world, int x, int y, int z, int orientation) {
         int oldMetadata = world.getBlockMetadata(x, y, z);
         int newMetadata = setOrientationInMetadata(oldMetadata, orientation);
         world.setBlockMetadataWithNotify(x, y, z, newMetadata);
     }
 
     public static net.minecraft.src.redstoneExtended.Laser.LaserMode getLaserMode(IBlockAccess iBlockAccess, int x, int y, int z) {
         return ((net.minecraft.src.redstoneExtended.Laser.TileEntityLaserFocusLens)iBlockAccess.getBlockTileEntity(x, y, z)).mode;
     }
 
     public static void setLaserMode(IBlockAccess iBlockAccess, int x, int y, int z, net.minecraft.src.redstoneExtended.Laser.LaserMode laserMode) {
         ((net.minecraft.src.redstoneExtended.Laser.TileEntityLaserFocusLens)iBlockAccess.getBlockTileEntity(x, y, z)).mode = laserMode;
     }
 
     public static short getDistance(IBlockAccess iBlockAccess, int x, int y, int z) {
         return ((net.minecraft.src.redstoneExtended.Laser.TileEntityLaserFocusLens)iBlockAccess.getBlockTileEntity(x, y, z)).distance;
     }
 
     public static void setDistance(IBlockAccess iBlockAccess, int x, int y, int z, short distance) {
         ((net.minecraft.src.redstoneExtended.Laser.TileEntityLaserFocusLens)iBlockAccess.getBlockTileEntity(x, y, z)).distance = distance;
     }
 
     @Override
     public boolean canProvideLaserPowerInDirection(IBlockAccess iBlockAccess, int x, int y, int z, int direction) {
         return (getOrientation(iBlockAccess, x, y, z) == direction);
     }
 
     @Override
     public boolean isProvidingLaserPowerInDirection(IBlockAccess iBlockAccess, int x, int y, int z, int direction) {
         return canProvideLaserPowerInDirection(iBlockAccess, x, y, z, direction) && getState(iBlockAccess, x, y, z);
     }
 
     @Override
     public net.minecraft.src.redstoneExtended.Laser.LaserMode getLaserModeProvidedInDirection(IBlockAccess iBlockAccess, int x, int y, int z, int direction) {
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
