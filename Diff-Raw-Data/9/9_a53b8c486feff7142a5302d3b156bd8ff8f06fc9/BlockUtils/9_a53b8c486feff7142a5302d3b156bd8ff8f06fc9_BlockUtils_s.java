 package us.fitzpatricksr.cownet.utils;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Simple utilities for checking block properties and putting down fences.
  */
 public class BlockUtils {
     public static final Set<Material> PASS_THROUGH_BLOCKS = new HashSet<Material>(
             Arrays.asList(
                     new Material[]{
                             Material.AIR,
 //                            Material.WATER,
 //                            Material.STATIONARY_WATER,
                             Material.LEAVES,
                             Material.SAPLING,
                             Material.WEB,
                             Material.FEATHER,
                            Material.FENCE,
                            Material.FENCE_GATE,
                             Material.LONG_GRASS,
                             Material.DEAD_BUSH,
                             Material.WOOL,
                             Material.YELLOW_FLOWER,
                             Material.RED_ROSE,
                             Material.BROWN_MUSHROOM,
                             Material.RED_MUSHROOM,
                             Material.TORCH,
                             Material.FIRE,
                             Material.CROPS,
                             Material.SNOW,
 //                            Material.ICE,
                             Material.SNOW_BLOCK,
                            Material.CACTUS,
                             Material.SUGAR_CANE_BLOCK,
                             Material.PUMPKIN,
                             Material.JACK_O_LANTERN,
                             Material.CAKE_BLOCK,
                             Material.MONSTER_EGGS,
                             Material.HUGE_MUSHROOM_1,
                             Material.HUGE_MUSHROOM_2,
                             Material.MELON_BLOCK,
                             Material.PUMPKIN_STEM,
                             Material.MELON_STEM,
                             Material.VINE,
                             Material.WATER_LILY
                     }));
 
     public static Location getHighestLandLocation(Location start) {
         Location loc = start.clone();
         for (int i = 254; i > 0; i--) {
             loc.setY(i);
             if (!BlockUtils.PASS_THROUGH_BLOCKS.contains(loc.getBlock().getType())) {
                 return loc;
             }
         }
         return start;
     }
 
 }
 
