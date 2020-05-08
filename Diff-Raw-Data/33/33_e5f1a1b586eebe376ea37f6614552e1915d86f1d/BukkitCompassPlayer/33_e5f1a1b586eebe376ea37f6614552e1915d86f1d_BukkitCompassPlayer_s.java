 package net.betterverse.compass.bukkit.player;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import net.betterverse.compass.bukkit.BukkitCompass;
 import net.betterverse.compass.bukkit.hooks.MyChunksManager;
 import net.betterverse.compass.bukkit.hooks.TownsManager;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 
 /**
  * Implementation of CompassWarpPlayer for a Bukkit-based server. Cannot
  * be instantiated outside of its own package for security and avoiding
  * concurrent monitoring of one single player through multiple objects.
  * 
  * @author Julian Trust
  * @modified 1/8/12
  */
 public final class BukkitCompassPlayer {
     
     private final Player player;
     private int cooldown;
     
     public BukkitCompassPlayer(Player player) {
         this.player = player;
         this.cooldown = 0;
     }
     
     public void setCooldown(int cooldown) {
         this.cooldown = cooldown;
     }
     
     public int getCooldown() {
         return cooldown;
     }
     
     public boolean hasPermission(String permission) {
         return player.hasPermission(permission);
     }
     
     public void teleport(int x, int y, int z) {
         Location loc = new Location(
                 player.getWorld(),
                 x + 0.5,
                 y,
                 z + 0.5,
                 player.getLocation().getYaw(),
                 player.getLocation().getPitch());
         
         boolean myChunksWarpTo = MyChunksManager.canWarpTo(player, loc);
         boolean myChunksWarpFrom = MyChunksManager.canWarpFrom(player);
         
         if(!myChunksWarpFrom) {
             player.sendMessage("§eYou cannot warp from this chunk!");
             return;
         }
         
         if(!myChunksWarpTo) {
             player.sendMessage("§eYou cannot warp to this chunk!");
             return;
         }
         
         boolean townyWarpTo = TownsManager.canWarpTo(player, loc);
         boolean townyWarpFrom = TownsManager.canWarpFrom(player);
         
         if(!townyWarpFrom) {
             player.sendMessage("§eYou cannot warp from this town!");
             return;
         }
         
         if(!townyWarpTo) {
             player.sendMessage("§eYou cannot warp to this town!");
             return;
         }
         
         player.teleport(loc);
     }
     
     public void warpToTarget() {
         Location loc = getToLocation(player);
         
         if(loc == null) {
             player.sendMessage("§eNo block in sight (or too far)!");
             return;
         }
         
         BukkitCompassPlayer cwPlayer = BukkitCompass.instance.getPlayer(player.getName());
         
         cwPlayer.teleport(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
     }
     
     public void warpThroughTarget() {
         Location loc = getThroughLocation(player);
         
         if(loc == null) {
             player.sendMessage("§eNothing to pass through!");
             return;
         }
         
         BukkitCompassPlayer cwPlayer = BukkitCompass.instance.getPlayer(player.getName());
         
         cwPlayer.teleport(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
     }
     
     private static Location getThroughLocation(Player player) {
         Block bl = null;
         try {
             boolean solid = false;
             boolean thru = false;
             List<Block> bls = new ArrayList<Block>();
             Location loc = player.getLocation();
             World w = loc.getWorld();
             double x = loc.getX();
             double y = loc.getY() + 1;
             double z = loc.getZ();
             bls.add(loc.getBlock());
             double dist = loc.distance(new Location(w,
                     x += loc.getDirection().getX(),
                     y += loc.getDirection().getY(),
                     z += loc.getDirection().getZ()));
             while(dist <= 100) {
                 if(!bls.contains(w.getBlockAt((int) x, (int) y, (int) z)))
                     bls.add(w.getBlockAt((int) x, (int) y, (int) z));
                 dist = loc.distance(new Location(w,
                 x += loc.getDirection().getX(),
                 y += loc.getDirection().getY(),
                 z += loc.getDirection().getZ()));
             }
             if(!trans.contains((byte) bls.get(0).getTypeId()))
                 solid = true;
             for(Block b : bls) {
                 if(!thru && !solid) {
                     if(!trans.contains((byte) b.getTypeId()))
                         solid = true;
                 } else if(!thru && solid) {
                     if(trans.contains((byte) b.getTypeId())) {
                         solid = false;
                         thru = true;
                         if(trans.contains((byte) b.getTypeId()) &&
                                 trans.contains((byte) b.getRelative(0, 1, 0).getTypeId()) &&
                                 !trans.contains((byte) b.getRelative(0, -1, 0).getTypeId())) {
                             bl = b;
                             break;
                         } 
                     }
                 } else if(thru && !solid) {
                     if(trans.contains((byte) b.getTypeId()) &&
                             trans.contains((byte) b.getRelative(0, 1, 0).getTypeId()) &&
                             !trans.contains((byte) b.getRelative(0, -1, 0).getTypeId())) {
                         bl = b;
                         break;
                     } else if(!trans.contains((byte) b.getTypeId())) {
                         solid = true;
                         thru = false;
                     }
                 }
             }
         } catch(Exception ex) {
             return null;
         }
         if(bl == null) return null;
         if(bl.getLocation().distance(player.getLocation()) > 100) return null;
         if(!trans.contains((byte) bl.getTypeId()) &&
                 !trans.contains((byte) bl.getRelative(0, 1, 0).getTypeId()))
             return null;
         if(bl.getLocation().getBlockY() < 1) return null;
         if(trans.contains((byte) bl.getRelative(0, -1, 0).getTypeId()))
             return null;
         if(unsafe.contains((byte) bl.getTypeId()) ||
                 unsafe.contains((byte) bl.getRelative(0, 1, 0).getTypeId()))
             return null;
         return new Location(bl.getLocation().getWorld(),
                 bl.getLocation().getX(),
                 bl.getLocation().getY(),
                 bl.getLocation().getZ(),
                 player.getLocation().getYaw(),
                 player.getLocation().getPitch());
     }
     
     private static Location getToLocation(Player player) {
         Block bl;
         try {
             bl = player.getTargetBlock(trans, 100);
         } catch(Exception ex) {
             return null;
         }
        if(bl.getLocation().getBlockY() == 0) return null;
         if(bl == null) return null;
         if(bl.getLocation().distance(player.getLocation()) > 100) return null;
         World w = bl.getWorld();
         int x = bl.getX();
         int y = bl.getY();
         int z = bl.getZ();
         while(!trans.contains((byte) w.getBlockAt(x, y, z).getTypeId()) &&
                 !trans.contains((byte) w.getBlockAt(x, y + 1, z).getTypeId()) && y<w.getMaxHeight())
             y++;
         if(trans.contains((byte) w.getBlockAt(x, y, z).getTypeId()))
             return null;
         if(unsafe.contains((byte) w.getBlockAt(x, y + 1, z).getTypeId()) ||
                 unsafe.contains((byte) w.getBlockAt(x, y + 2, z).getTypeId()))
             return null;
         return new Location(w, x, y + 1, z, player.getLocation().getYaw(), player.getLocation().getPitch());
     }
     
    private static final HashSet trans;
     static {
         {
            trans = new HashSet();
             trans.add((byte) 0);
             trans.add((byte) 8);
             trans.add((byte) 9);
             trans.add((byte) 10);
             trans.add((byte) 11);
             trans.add((byte) 78);
         }
     }
     
    private static final HashSet unsafe;
     static {
         {
            unsafe = new HashSet();
             unsafe.add((byte) 10);
             unsafe.add((byte) 11);
         }
     }
 }
