 package net.robinjam.bukkit.eternalwolf;
 
 import net.robinjam.bukkit.util.PlayerUtil;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 
 /**
  *
  * @author robinjam
  */
 class PlayerListener extends org.bukkit.event.player.PlayerListener {
 
     EternalWolf plugin;
 
     public PlayerListener(EternalWolf instance) {
         plugin = instance;
     }
 
     @Override
     public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
         Player player = event.getPlayer();
         Entity target = event.getRightClicked();
 
         // If the player is giving a bone to a wolf
         if (player.getItemInHand().getType() == Material.BONE && 
                 event.getRightClicked() instanceof Wolf) {
 
             Wolf wolf = (Wolf) target;
 
             // If the wolf is wild and the player already owns too many wolves,
             // do not allow them to tame another one
             if (!wolf.isTamed() && playerHasTooManyWolves(player)) {
                 event.setCancelled(true);
                 player.sendMessage(ChatColor.RED + "You cannot tame more than " + plugin.maxWolves + " wolves!");
             } else if (wolf.isTamed() && wolf.getOwner() instanceof Player) {
                 Player owner = (Player)wolf.getOwner();
                if (!player.equals(owner)) {
                     // If the wolf is owned by another player, get that player's name
                     player.sendMessage(ChatColor.RED + "That wolf belongs to " + owner.getDisplayName());
                 }
             }
         }
     }
 
     protected boolean playerHasTooManyWolves(Player player) {
         // If max_wolves is -1, allow each player unlimited wolves
         if (plugin.maxWolves == -1) return false;
 
         return (PlayerUtil.getWolves(player).size() >= plugin.maxWolves);
     }
 }
