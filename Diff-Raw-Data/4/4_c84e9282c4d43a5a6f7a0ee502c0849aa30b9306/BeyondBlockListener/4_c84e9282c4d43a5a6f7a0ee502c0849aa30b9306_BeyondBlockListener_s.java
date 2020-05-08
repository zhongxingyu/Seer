 package Lihad.Conflict.Listeners;
 
 
 import java.util.List;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockDamageEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import Lihad.Conflict.*;
 
 public class BeyondBlockListener implements Listener {
 	public BeyondBlockListener() { }
     
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event){
         Player player = event.getPlayer();
 		if(event.getBlock().getWorld().getName().equals("survival")){
             if (((!Conflict.Abatton.getMayors().contains(player.getName()) && Conflict.Abatton.getLocation().distance(event.getBlock().getLocation()) < Conflict.Abatton.getProtectionRadius())
 					|| (!Conflict.Oceian.getMayors().contains(player.getName()) && Conflict.Oceian.getLocation().distance(event.getBlock().getLocation()) < Conflict.Oceian.getProtectionRadius())
 					|| (!Conflict.Savania.getMayors().contains(player.getName()) && Conflict.Savania.getLocation().distance(event.getBlock().getLocation()) < Conflict.Savania.getProtectionRadius())
 			)&& !player.isOp() && !Conflict.handler.has(player, "conflict.debug")){
 				event.setCancelled(true);
                 return;
 			}
             
             if(!player.isOp() && !Conflict.handler.has(player, "conflict.debug")) {
                 for (Node n : Conflict.nodes) {
                     if (n.isBlockProtected() && n.isInRadius(event.getBlock().getLocation())) {
                         event.setCancelled(true);
                         return;
                     }
                 }
             }
 		}
 	}
 	@EventHandler
 	public void onBlockPlace(BlockPlaceEvent event){
         Player player = event.getPlayer();
 		if(event.getBlock().getWorld().getName().equals("survival")){
             if(((!Conflict.Abatton.getMayors().contains(player.getName()) && Conflict.Abatton.getLocation().distance(event.getBlock().getLocation()) < Conflict.Abatton.getProtectionRadius())
 					|| (!Conflict.Oceian.getMayors().contains(player.getName()) && Conflict.Oceian.getLocation().distance(event.getBlock().getLocation()) < Conflict.Oceian.getProtectionRadius())
 					|| (!Conflict.Savania.getMayors().contains(player.getName()) && Conflict.Savania.getLocation().distance(event.getBlock().getLocation()) < Conflict.Savania.getProtectionRadius()))
 					&& !player.isOp() && !Conflict.handler.has(player, "conflict.debug")){
 				event.setCancelled(true);
                 return;
 			}
             if(!player.isOp() && !Conflict.handler.has(player, "conflict.debug")) {
                 for (Node n : Conflict.nodes) {
                     if (n.isBlockProtected() && n.isInRadius(event.getBlock().getLocation())) {
                         event.setCancelled(true);
                         return;
                     }
                 }
 			}
 		}
 	}	
 	@EventHandler
 	public void onEntityExplode(EntityExplodeEvent event){
 		if(event.getLocation().getWorld().getName().equals("survival")){
             for (Node n : Conflict.nodes) {
                 if (n.isBlockProtected() && n.isInRadius(event.getLocation())) {
                     event.setCancelled(true);
                     return;
                 }
             }
         }
     }
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event){
         org.bukkit.inventory.ItemStack item = event.getPlayer().getItemInHand();
		if(event.getPlayer().getWorld().getName().equals("survival") && item != null && (item.getType() == Material.LAVA_BUCKET || item.getType() == Material.WATER_BUCKET)) {
             for (Node n : Conflict.nodes) {
                 if (n.isBlockProtected() && n.isInRadius(event.getClickedBlock().getLocation())) {
                     event.setCancelled(true);
                     return;
                 }
             }
 		}
 	}
 }
