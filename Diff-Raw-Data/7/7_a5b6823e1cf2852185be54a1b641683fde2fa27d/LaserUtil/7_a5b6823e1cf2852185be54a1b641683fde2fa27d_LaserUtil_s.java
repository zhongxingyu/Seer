 package net.minecraft.src.lasers;
 
 import net.minecraft.src.Block;
 import net.minecraft.src.World;
 import net.minecraft.src.mod_lasers;
 import net.minecraft.src.util.Position;
 
 class LaserUtil {
     public static boolean isBlockUpdateForLaserInDirectionNecessary(World world, int x, int y, int z, int direction) {
         int blockId = world.getBlockId(x, y, z);
         Block block = Block.blocksList[blockId];
         ILaserEmitter laserEmitter = (ILaserEmitter)block;
 
         if (!laserEmitter.canProvideLaserPowerInDirection(world, x, y, z, direction))
             return false;
 
         Position laserPos = new Position(x, y, z).moveInDirection(direction);
         int laserBlockId = world.getBlockId(laserPos.X, laserPos.Y, laserPos.Z);
 
         if (laserEmitter.isProvidingLaserPowerInDirection(world, x, y, z, direction)) {
             if ((laserBlockId == 0) ||
                     ((Block.blocksList[laserBlockId].blockMaterial.func_27283_g()) &&
                             (Block.blocksList[laserBlockId].blockMaterial != net.minecraft.src.lasers.LaserMaterials.laser)))
                 return true;
 
             if ((laserBlockId == mod_lasers.getInstance().blockLaser.blockID) &&
                     net.minecraft.src.lasers.BlockLaser.getOrientation(world, laserPos.X, laserPos.Y, laserPos.Z) == direction &&
                     (!laserEmitter.getLaserModeProvidedInDirection(world, x, y, z, direction).equals(net.minecraft.src.lasers.BlockLaser.getLaserMode(world, laserPos.X, laserPos.Y, laserPos.Z)) ||
                             (net.minecraft.src.lasers.BlockLaser.getDistance(world, laserPos.X, laserPos.Y, laserPos.Z) != laserEmitter.getInitialDistanceProvidedInDirection(world, x, y, z, direction))))
                 return true;
         } else if ((laserBlockId == mod_lasers.getInstance().blockLaser.blockID) &&
                 (net.minecraft.src.lasers.BlockLaser.getOrientation(world, laserPos.X, laserPos.Y, laserPos.Z) == direction))
             return true;
 
         return false;
     }
 
     public static void blockUpdateForLaserInDirection(World world, int x, int y, int z, int direction) {
         int blockId = world.getBlockId(x, y, z);
         Block block = Block.blocksList[blockId];
         ILaserEmitter laserEmitter = (ILaserEmitter)block;
 
         if (!laserEmitter.canProvideLaserPowerInDirection(world, x, y, z, direction))
             return;
 
         Position laserPos = new Position(x, y, z).moveInDirection(direction);
         int laserBlockId = world.getBlockId(laserPos.X, laserPos.Y, laserPos.Z);
         boolean blockUpdateNecessary = false;
 
         if (laserEmitter.isProvidingLaserPowerInDirection(world, x, y, z, direction)) {
             if ((laserBlockId == 0) ||
                     ((Block.blocksList[laserBlockId].blockMaterial.func_27283_g()) &&
                             (Block.blocksList[laserBlockId].blockMaterial != net.minecraft.src.lasers.LaserMaterials.laser))) {
                 world.setBlock(laserPos.X, laserPos.Y, laserPos.Z, mod_lasers.getInstance().blockLaser.blockID);
 
                 blockUpdateNecessary = true;
                 laserBlockId = world.getBlockId(laserPos.X, laserPos.Y, laserPos.Z);
 
                 net.minecraft.src.lasers.BlockLaser.setOrientation(world, laserPos.X, laserPos.Y, laserPos.Z, direction);
             }
 
             if ((laserBlockId == mod_lasers.getInstance().blockLaser.blockID) &&
                     (net.minecraft.src.lasers.BlockLaser.getOrientation(world, laserPos.X, laserPos.Y, laserPos.Z) == direction)) {
                 if (net.minecraft.src.lasers.BlockLaser.getDistance(world, laserPos.X, laserPos.Y, laserPos.Z) != laserEmitter.getInitialDistanceProvidedInDirection(world, x, y, z, direction)) {
                     net.minecraft.src.lasers.BlockLaser.setDistance(world, laserPos.X, laserPos.Y, laserPos.Z, laserEmitter.getInitialDistanceProvidedInDirection(world, x, y, z, direction));
                     blockUpdateNecessary = true;
                 }
 
                 if (!laserEmitter.getLaserModeProvidedInDirection(world, x, y, z, direction).equals(net.minecraft.src.lasers.BlockLaser.getLaserMode(world, laserPos.X, laserPos.Y, laserPos.Z))) {
                     net.minecraft.src.lasers.BlockLaser.setLaserMode(world, laserPos.X, laserPos.Y, laserPos.Z, laserEmitter.getLaserModeProvidedInDirection(world, x, y, z, direction).getClone());
                     blockUpdateNecessary = true;
                 }
             }
         } else if ((laserBlockId == mod_lasers.getInstance().blockLaser.blockID) &&
                 (net.minecraft.src.lasers.BlockLaser.getOrientation(world, laserPos.X, laserPos.Y, laserPos.Z) == direction)) {
 
             world.setBlock(laserPos.X, laserPos.Y, laserPos.Z, 0);
             blockUpdateNecessary = true;
         }
 
         if (blockUpdateNecessary) {
             world.markBlockAsNeedsUpdate(laserPos.X, laserPos.Y, laserPos.Z);
             world.notifyBlocksOfNeighborChange(laserPos.X, laserPos.Y, laserPos.Z, laserBlockId);
         }
     }
 }
