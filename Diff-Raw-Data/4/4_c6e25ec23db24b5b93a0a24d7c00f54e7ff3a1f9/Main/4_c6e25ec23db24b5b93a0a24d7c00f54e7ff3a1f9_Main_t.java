 package edgruberman.bukkit.waterfix;
 
import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockFromToEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public final class Main extends JavaPlugin implements Listener {
 
     private static final byte WATER_FULL = 0;
 
     @Override
     public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onBlockFromTo(final BlockFromToEvent event) {
         final Block from = event.getBlock();
         if (from.getTypeId() != Material.STATIONARY_WATER.getId() || from.getData() != Main.WATER_FULL) return;
 
         final Block to = event.getToBlock();
         if (to.getTypeId() != Material.AIR.getId()) return;
 
         // At least one additional, besides original from block, full water source must be directly adjacent
         for (final BlockFace direction : new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST }) {
             final int adjacentX = to.getX() + direction.getModX();
             final int adjacentY = to.getY() + direction.getModY();
             final int adjacentZ = to.getZ() + direction.getModZ();
             if (from.getX() == adjacentX && from.getY() == adjacentY && from.getZ() == adjacentZ) continue;
 
             if (to.getWorld().getBlockTypeIdAt(adjacentX, adjacentY, adjacentZ) != Material.WATER.getId()) continue;
 
             if (to.getRelative(direction).getData() != Main.WATER_FULL) continue;
 
             to.setTypeIdAndData(Material.STATIONARY_WATER.getId(), Main.WATER_FULL, true);
             event.setCancelled(true);
             break;
         }
 
     }
 
 }
