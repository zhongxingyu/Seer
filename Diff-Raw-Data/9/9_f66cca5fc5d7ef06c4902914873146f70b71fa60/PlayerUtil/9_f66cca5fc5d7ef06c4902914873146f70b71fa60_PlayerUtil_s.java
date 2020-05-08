 package net.robinjam.bukkit.util;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 
 /**
  *
  * @author robinjam
  */
 public class PlayerUtil {
 
     public static List<Wolf> getWolves(Player player) {
         List<Wolf> wolves = new ArrayList<Wolf>();
         
         for (LivingEntity entity : player.getWorld().getLivingEntities()) {
             if (entity instanceof Wolf) {
                 Wolf wolf = (Wolf) entity;
 
                if (wolf.isTamed() && wolf.getOwner().equals(player))
                     wolves.add(wolf);
             }
         }
 
         return wolves;
     }
 
 }
