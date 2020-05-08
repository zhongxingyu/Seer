 /**
  * SakuraCmd - Package: net.syamn.sakuracmd.listener.feature
  * Created: 2013/01/11 2:21:30
  */
 package net.syamn.sakuracmd.listener.feature;
 
 import net.syamn.sakuracmd.SakuraCmd;
import net.syamn.sakuracmd.permission.Perms;
 import net.syamn.utils.ItemUtil;
 import net.syamn.utils.Util;
 
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.entity.Creeper;
 import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * PassengerListener (PassengerListener.java)
  * @author syam(syamn)
  */
 public class PassengerListener implements Listener{
     private SakuraCmd plugin;
     public PassengerListener (final SakuraCmd plugin){
         this.plugin = plugin;
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onPlayerTeleport(final PlayerTeleportEvent event) {
         // Eject player if ride on the player
         final Player player = event.getPlayer();
         if (player.getPassenger() != null && (player.getPassenger() instanceof Player)) {
             player.eject();
         }
     }
     
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
         final Entity ent = event.getEntity();
         final Entity damager = event.getDamager();
                
        // Player attacking entity
        if (damager instanceof Player) {
             final Player player = (Player) damager;
             final ItemStack hand = player.getItemInHand();
             if (hand.getType() != Material.BONE){
                 return; // return if player inHand item is not bone
             }
             
            // Check player permission
            if (ent.getType().equals(EntityType.PLAYER)){
                if (!Perms.RIDE_PLAYER.has(player) && !Perms.RIDE_ALLENTITY.has(player)){
                    return;
                }
            }else{
                if (!Perms.RIDE_ALLENTITY.has(player)){
                    return;
                }
            }
            
             event.setDamage(0);
             event.setCancelled(true);
             final String targetStr = (ent instanceof Player) ? "プレイヤー" : ent.getType().getName();
             
             if (ent.getPassenger() == null){
                 if (!player.getGameMode().equals(GameMode.CREATIVE)){
                     player.setItemInHand(ItemUtil.decrementItem(hand, 1));
                 }
                 ent.setPassenger(player);
                 Util.message(player, "&b" + targetStr + "に乗りました！");
             }
             else if ((ent.getPassenger() instanceof Player) && (Player) ent.getPassenger() == player){
                 ent.eject();
                 Util.message(player, "&b" + targetStr + "から降りました！");
             }
             else{
                 Util.message(player, "&cその" + targetStr + "は既に他人が乗っています！");
             }
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void targetCancelOnRideMonster(final EntityTargetLivingEntityEvent event) {
         if (!(event.getTarget() instanceof Player)) { return; }
         
         final Player target = (Player) event.getTarget();
         
         if (target.getPassenger() != null && (target.getPassenger() instanceof Player)) {
             event.setCancelled(true);
         }
         if (target.getVehicle() != null && (target.getVehicle() instanceof Player)){
             event.setCancelled(true);
         }
     }
 }
