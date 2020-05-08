 package com.ryanmichela.toxicskies;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 
 /**
  */
 public abstract class SkyFinder {
     public abstract boolean canSeeSky(Location startLoc, int distance);
 
     protected boolean solidBlock(Block b) {
         return solidBlock(b.getTypeId());
     }
 
     protected boolean solidBlock(Location l) {
         return solidBlock(l.getWorld().getBlockTypeIdAt(l));
     }
 
     protected boolean solidBlock(int typeId) {
          return  typeId != Material.AIR.getId() &&
                  typeId != Material.TORCH.getId() &&
                  typeId != Material.SUGAR_CANE_BLOCK.getId() &&
                  typeId != Material.SAPLING.getId() &&
                  typeId != Material.WEB.getId() &&
                  typeId != Material.LONG_GRASS.getId() &&
                  typeId != Material.DEAD_BUSH.getId() &&
                  typeId != Material.YELLOW_FLOWER.getId() &&
                  typeId != Material.RED_ROSE.getId() &&
                  typeId != Material.BROWN_MUSHROOM.getId() &&
                  typeId != Material.RED_MUSHROOM.getId() &&
                  typeId != Material.REDSTONE_TORCH_ON.getId() &&
                  typeId != Material.REDSTONE_TORCH_OFF.getId() &&
                  typeId != Material.REDSTONE_WIRE.getId() &&
                  typeId != Material.VINE.getId() &&
                  typeId != Material.CAKE_BLOCK.getId() &&
                  typeId != Material.LADDER.getId() &&
                  typeId != Material.FENCE.getId() &&
                  typeId != Material.FENCE_GATE.getId() &&
                  typeId != Material.RAILS.getId() &&
                  typeId != Material.POWERED_RAIL.getId() &&
                  typeId != Material.DETECTOR_RAIL.getId() &&
                  typeId != Material.SNOW.getId();
     }
 
     protected boolean blockSeesSky(Block b) {
         return blockSeesSky(b.getX(), b.getY(), b.getZ(), b.getWorld());
     }
 
     protected boolean blockSeesSky(Location l) {
         return blockSeesSky(l.getBlockX(), l.getBlockY(), l.getBlockZ(), l.getWorld());
     }
 
     protected boolean blockSeesSky(int x, int y, int z, World w) {
         if(y >= w.getHighestBlockYAt(x, z)) {
            for(int yy = y; y < 128; y++) {
                if(solidBlock(w.getBlockAt(x, yy, z))) {
                     return false;
                 }
             }
             return true;
         } else {
             return false;
         }
     }
 
     protected String loc2str(Location l, int d, String r) {
         String s = r + " ";
         for(int i = 0; i < 10-d; i++) s += "..";
         s += "x:" + (int)Math.floor(l.getX()) + " y:" + (int)Math.floor(l.getY()) + " z:" + (int)Math.floor(l.getZ());
         return s;
     }
 }
