 package net.minecraft.src.redstoneExtended;
 
 import net.minecraft.src.*;
 
 import java.util.Random;
 
 public class BlockRedstoneLightSensor extends BlockContainer {
     private final int textureSun = ModLoader.addOverride("/terrain.png", "/redstoneExtended/lightSensor/sun.png");
     private final int textureRain = ModLoader.addOverride("/terrain.png", "/redstoneExtended/lightSensor/rain.png");
     private final int textureMoon = ModLoader.addOverride("/terrain.png", "/redstoneExtended/lightSensor/moon.png");
     private final int textureDarkness = ModLoader.addOverride("/terrain.png", "/redstoneExtended/lightSensor/darkness.png");
 
     public BlockRedstoneLightSensor(int id) {
         super(id, Block.stairSingle.getBlockTextureFromSideAndMetadata(1, 0), Material.circuits);
         setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
     }
 
     @Override
     public TileEntity getBlockEntity() {
         return new TileEntityLightSensor();
     }
 
     @Override
     public void onBlockAdded(World world, int x, int y, int z) {
         super.onBlockAdded(world, x, y, z);
 
         world.notifyBlocksOfNeighborChange(x + 1, x, z, blockID);
         world.notifyBlocksOfNeighborChange(x - 1, y, z, blockID);
         world.notifyBlocksOfNeighborChange(x, y, z + 1, blockID);
         world.notifyBlocksOfNeighborChange(x, y, z - 1, blockID);
         world.notifyBlocksOfNeighborChange(x, y - 1, z, blockID);
         world.notifyBlocksOfNeighborChange(x, y + 1, z, blockID);
     }
 
     @Override
     public boolean isOpaqueCube() {
         return false;
     }
 
     @Override
     public boolean canPlaceBlockAt(World world, int x, int y, int z) {
         return world.isBlockOpaqueCube(x, y - 1, z) &&
                 super.canPlaceBlockAt(world, x, y, z);
     }
 
     @Override
     public boolean canBlockStay(World world, int x, int y, int z) {
         return world.isBlockOpaqueCube(x, y - 1, z) &&
                 super.canBlockStay(world, x, y, z);
     }
 
     @Override
     public void onNeighborBlockChange(World world, int x, int y, int z, int direction) {
         if (!canBlockStay(world, x, y, z)) {
             dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z));
             world.setBlockWithNotify(x, y, z, 0);
         }
     }
 
     @Override
     public boolean isIndirectlyPoweringTo(World world, int x, int y, int z, int direction) {
         return isPoweringTo(world, x, y, z, direction);
     }
 
     @Override
     public boolean isPoweringTo(IBlockAccess iBlockAccess, int x, int y, int z, int direction) {
         return getState(iBlockAccess, x, y, z);
     }
 
     @Override
     public boolean canProvidePower() {
         return true;
     }
 
     @Override
     public boolean renderAsNormalBlock() {
         return false;
     }
 
     @Override
     public int getRenderType() {
         return mod_redstoneExtended.getInstance().renderBlockRedstoneLightSensor;
     }
 
     @Override
     public int getBlockTextureFromSideAndMetadata(int side, int metadata) {
         switch (side) {
             case 6:
                 switch (getTriggerSettingFromMetadata(metadata)) {
                     case 0:
                         return textureSun;
                     case 1:
                         return Block.torchWood.getBlockTextureFromSide(2);
                     case 2:
                         return textureRain;
                     case 3:
                         return Block.torchRedstoneActive.getBlockTextureFromSide(2);
                     case 4:
                         return textureMoon;
                     case 5:
                         return textureDarkness;
                 }
             default:
                 return Block.stairSingle.getBlockTextureFromSideAndMetadata(side, 0);
         }
     }
 
     @Override
     public boolean blockActivated(World world, int x, int y, int z, EntityPlayer player) {
         byte oldTriggerSetting = getTriggerSetting(world, x, y, z);
         byte newTriggerSetting = (byte)((oldTriggerSetting >= 5) ? 0 : oldTriggerSetting + 1);
         setTriggerSetting(world, x, y, z, newTriggerSetting);
        world.markBlocksDirty(x, y, z, x, y, z);
 
         return true;
     }
 
     @Override
     public int idDropped(int i, Random random) {
         return mod_redstoneExtended.getInstance().itemRedstoneLightSensor.shiftedIndex;
     }
 
     public static void update(World world, int x, int y, int z) {
         int triggerSetting = getTriggerSetting(world, x, y, z);
         int lightLevel = world.getBlockLightValue(x, y, z);
         boolean isPowered;
         switch (triggerSetting) {
             case 0:
                 isPowered = (lightLevel == 15);
                 break;
             case 1:
                 isPowered = (lightLevel == 13);
                 break;
             case 2:
                 isPowered = (lightLevel == 12);
                 break;
             case 3:
                 isPowered = (lightLevel == 6);
                 break;
             case 4:
                 isPowered = (lightLevel == 4);
                 break;
             case 5:
                 isPowered = (lightLevel == 0);
                 break;
             default:
                 isPowered = false;
         }
        if (isPowered != getState(world, x, y, z)) {
             setState(world, x, y, z, isPowered);
            world.markBlocksDirty(x, y, z, x, y, z);
        }
     }
 
     private static boolean getStateFromMetadata(int metadata) {
         return ((metadata & 0x8) >> 3) == 1;
     }
 
     private static byte getTriggerSettingFromMetadata(int metadata) {
         return (byte)(metadata & 0x7);
     }
 
     private static int setStateInMetadata(int metadata, boolean state) {
         return ((metadata & 0x7) | ((state ? 1 : 0) << 3) & 0x8);
     }
 
     private static int setTriggerSettingInMetadata(int metadata, byte delaySetting) {
         return ((metadata & 0x8) | ((int)delaySetting & 0x7));
     }
 
     public static boolean getState(IBlockAccess iBlockAccess, int x, int y, int z) {
         int metadata = iBlockAccess.getBlockMetadata(x, y, z);
         return getStateFromMetadata(metadata);
     }
 
     private static byte getTriggerSetting(IBlockAccess iBlockAccess, int x, int y, int z) {
         int metadata = iBlockAccess.getBlockMetadata(x, y, z);
         return getTriggerSettingFromMetadata(metadata);
     }
 
     private static void setState(World world, int x, int y, int z, boolean state) {
         int oldMetadata = world.getBlockMetadata(x, y, z);
         int newMetadata = setStateInMetadata(oldMetadata, state);
         world.setBlockMetadataWithNotify(x, y, z, newMetadata);
     }
 
     private static void setTriggerSetting(World world, int x, int y, int z, byte delaySetting) {
         int oldMetadata = world.getBlockMetadata(x, y, z);
         int newMetadata = setTriggerSettingInMetadata(oldMetadata, delaySetting);
         world.setBlockMetadataWithNotify(x, y, z, newMetadata);
     }
 }
