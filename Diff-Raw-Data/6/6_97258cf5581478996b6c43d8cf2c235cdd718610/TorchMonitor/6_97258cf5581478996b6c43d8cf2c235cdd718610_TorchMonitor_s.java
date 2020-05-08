 package edgruberman.bukkit.fixtorchdrop;
 
 import org.bukkit.Material;
 import org.bukkit.block.BlockState;
 import org.bukkit.event.Event;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockPhysicsEvent;
 import org.bukkit.material.RedstoneTorch;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 
 import edgruberman.bukkit.messagemanager.MessageLevel;
 
 final class TorchMonitor extends BlockListener {
     
     TorchMonitor(final Plugin plugin) {
         PluginManager pm = plugin.getServer().getPluginManager();
         pm.registerEvent(Event.Type.BLOCK_PHYSICS, this, Event.Priority.Normal, plugin);
     }
     
     @Override
     public void onBlockPhysics(final BlockPhysicsEvent event) {
         if (event.isCancelled()) return;
         
        Main.messageManager.log("BlockPhysicsEvent on Type ID: " + event.getChangedTypeId(), MessageLevel.FINEST);
        
         // Only check redstone torch related events further
         BlockState state = event.getBlock().getState();
         if (state.getTypeId() != Material.REDSTONE_TORCH_OFF.getId() && state.getTypeId() != Material.REDSTONE_TORCH_ON.getId()) return;
         
        Main.messageManager.log("BlockPhysicsEvent has MaterialData of RedstoneTorch: " + (state.getData() instanceof RedstoneTorch)
                + "; Raw Data = " + state.getRawData() + "; State Type ID = " + state.getTypeId(), MessageLevel.FINEST);
        
         // Let normality happen if the attached chunk is loaded
         RedstoneTorch redstoneTorch = (RedstoneTorch) state.getData();
         if (event.getBlock().getRelative(redstoneTorch.getAttachedFace()).getChunk().isLoaded()) return;
         
         Main.messageManager.log("Cancelling physics update for a redstone torch in [" + event.getBlock().getWorld().getName() + "] at"
                 + " x:" + event.getBlock().getX()
                 + " y:" + event.getBlock().getY()
                 + " z:" + event.getBlock().getZ()
                 + " since its attached chunk is not loaded yet."
                 , MessageLevel.FINE);
         
         // Attached chunk is not loaded, do not let physics adjust redstone torch yet
         event.setCancelled(true);
     }
 }
