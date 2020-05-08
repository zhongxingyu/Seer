 package edgruberman.bukkit.simplelocks;
 
 import org.bukkit.Material;
 import org.bukkit.event.Event.Result;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import edgruberman.bukkit.simplelocks.MessageManager.MessageLevel;
 
 public class PlayerListener extends org.bukkit.event.player.PlayerListener {
     
     public PlayerListener() {}
     
     @Override
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (event.isCancelled()) return;
         
         if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
         
         Lock lock = Lock.getLock(event.getClickedBlock());
         if (lock != null) {
             // Existing lock in place.
             
             if (!lock.hasAccess(event.getPlayer())) {
                 // Player does not have access, cancel interaction and notify player.
                 event.setCancelled(true);
                 Main.messageManager.send(event.getPlayer(), MessageLevel.RIGHTS
                         , "You do not have access to this lock.");
                 Main.messageManager.log(MessageLevel.FINER
                         , "Lock access denied to " + event.getPlayer().getName() + " at "
                         + " x:" + event.getClickedBlock().getX()
                         + " y:" + event.getClickedBlock().getY()
                         + " z:" + event.getClickedBlock().getZ()
                 );
                 return;
             }
             
             if (Lock.isLock(event.getClickedBlock())) {
                 // Player has access and they right clicked on the lock itself so give them information.
                 Main.messageManager.send(event.getPlayer(), MessageLevel.STATUS
                         , "You have access to this lock.");
                 
                 if (lock.isOwner(event.getPlayer()))
                     Main.messageManager.send(event.getPlayer(), MessageLevel.NOTICE
                             , "To modify: /lock (+|-) <Player>");
             }
             return;
         }
         
         // No existing lock, check to see if player is requesting a lock be created.
         if (event.getClickedBlock().getType().equals(Material.CHEST)) {
             if (event.getMaterial().equals(Material.SIGN)
                    && event.getClickedBlock().getRelative(event.getBlockFace()).getType().equals(Material.AIR)) {
                 // Right click on a chest with a sign to create lock automatically.
                 event.setUseInteractedBlock(Result.DENY); // Don't open the chest.
                 event.getPlayer().setItemInHand(null);    // Pay the piper.
                 
                 // Create lock.
                 new Lock(event.getClickedBlock().getRelative(event.getBlockFace())
                         , event.getBlockFace().getOppositeFace()
                         , event.getPlayer()
                 );
             }
         }
     }
 }
