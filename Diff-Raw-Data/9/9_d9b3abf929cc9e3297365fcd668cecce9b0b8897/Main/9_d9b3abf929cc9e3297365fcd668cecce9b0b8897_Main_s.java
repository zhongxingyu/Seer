 package edgruberman.bukkit.fixtorchdrop;
 
 import org.bukkit.Chunk;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockPhysicsEvent;
 import org.bukkit.material.RedstoneTorch;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import edgruberman.bukkit.messagemanager.MessageLevel;
 import edgruberman.bukkit.messagemanager.MessageManager;
 
 public final class Main extends JavaPlugin implements Listener {
 
     static MessageManager messageManager;
 
     @Override
     public void onLoad() {
         Main.messageManager = new MessageManager(this);
         Main.messageManager.log("Version " + this.getDescription().getVersion());
     }
 
     @Override
     public void onEnable() {
         this.getServer().getPluginManager().registerEvents(this, this);
         Main.messageManager.log("Plugin Enabled");
     }
 
     @Override
     public void onDisable() {
         Main.messageManager.log("Plugin Disabled");
     }
 
     @EventHandler
     public void onBlockPhysics(final BlockPhysicsEvent event) {
         if (event.isCancelled()) return;
 
         // Only check redstone torch related events further
         if (event.getBlock().getTypeId() != Material.REDSTONE_TORCH_OFF.getId() && event.getBlock().getTypeId() != Material.REDSTONE_TORCH_ON.getId()) return;
 
         // Let normality happen if the attached chunk is loaded
         RedstoneTorch redstoneTorch = (RedstoneTorch) event.getBlock().getState().getData();
        Block attachedBlock = event.getBlock().getRelative(redstoneTorch.getAttachedFace());
         if (attachedBlock != null) {
             Chunk attachedChunk = attachedBlock.getChunk();
             if (attachedChunk != null && attachedChunk.isLoaded()) return;
         }
 
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
