 package net.siriuser.expmanager.listeners;
 
 import net.siriuser.expmanager.ExpManager;
 import net.siriuser.expmanager.Helper;
 import net.siriuser.expmanager.storage.ConfigurationManager;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.inventory.FurnaceExtractEvent;
 
 public class BlockListener implements Listener {
     private ExpManager plugin;
     private ConfigurationManager config;
 
     public BlockListener(final ExpManager plugin) {
         this.plugin = plugin;
         this.config = Helper.getInstance().getConfig();
     }
 
     @EventHandler
     public void onBlockBreakEvent (final BlockBreakEvent event) {
         final Player player = event.getPlayer();
         final Block block = event.getBlock();
 
        int dropExp = config.getEntitySection().getInt(block.getType().toString(), -1);
         if (dropExp == -1) {
             dropExp = event.getExpToDrop();
         }
 
         if (Helper.getInstance().getConfig().getDirectExp()) {
             player.giveExp(dropExp);
             event.setExpToDrop(0);
         } else {
             event.setExpToDrop(dropExp);
         }
 
        ExpManager.Debug(block.getType().toString());
     }
 
     @EventHandler
     public void onFurnaceExtractEvent (final FurnaceExtractEvent event) {
         final Player player = event.getPlayer();
         final Block block = event.getBlock();
         int dropExp = event.getExpToDrop();
 
         if (Helper.getInstance().getConfig().getDirectExp()) {
             player.giveExp(dropExp);
             event.setExpToDrop(0);
         } else {
             event.setExpToDrop(dropExp);
         }
     }
 }
