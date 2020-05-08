 package me.tehbeard.BeardStat.listeners;
 
 import java.util.List;
 
 import me.tehbeard.BeardStat.containers.PlayerStatManager;
 
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockPlaceEvent;
 
 
 public class StatBlockListener implements Listener{
 
     List<String> worlds;
 
     private PlayerStatManager playerStatManager;
 
     public StatBlockListener(List<String> worlds,	PlayerStatManager playerStatManager){
         this.worlds = worlds;
         this.playerStatManager = playerStatManager;
 
     }
 
 
     @EventHandler(priority=EventPriority.MONITOR)
     public void onBlockPlace(BlockPlaceEvent event) {
         if(event.isCancelled()==false && !worlds.contains(event.getPlayer().getWorld().getName())){
             playerStatManager.getPlayerBlob(event.getPlayer().getName()).getStat("stats","totalblockcreate").incrementStat(1);
             MetaDataCapture.saveMetaDataStat(playerStatManager.getPlayerBlob(event.getPlayer().getName()), 
                     "blockcreate", 
                     event.getBlock().getType(), 
                     event.getBlock().getData(), 
                     1);
             
         }
     }
 
     @EventHandler(priority=EventPriority.MONITOR)
     public void onBlockBreak(BlockBreakEvent event) {
         if(event.isCancelled()==false && !worlds.contains(event.getPlayer().getWorld().getName())){
             playerStatManager.getPlayerBlob(event.getPlayer().getName()).getStat("stats","totalblockdestroy").incrementStat(1);
             MetaDataCapture.saveMetaDataStat(playerStatManager.getPlayerBlob(event.getPlayer().getName()), 
                     "blockdestroy", 
                     event.getBlock().getType(), 
                     event.getBlock().getData(), 
                     1);
         }
     }
 
 
 
 }
