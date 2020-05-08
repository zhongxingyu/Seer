 package net.year4000.eprotect;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderDragonPart;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBurnEvent;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.hanging.HangingBreakByEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 
 import com.sk89q.commandbook.CommandBook;
 import com.sk89q.minecraft.util.commands.CommandPermissionsException;
 
 public class ProtectEvents implements Listener {
     private CommandBook cmdbook = CommandBook.inst();
     private Configuration configuration = EProtect.inst().getConfiguration();
 
     /**
      * If the block is protected, stop the place.
      */
     @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
     public void onBlockPlace(BlockPlaceEvent event) {
         Player player = event.getPlayer();
         Block block = event.getBlock();
         Protected protect = new Protected(block);
 
         // Check if the area is protected.
         if (!protect.isProtected()) {
             return;
         }
 
         boolean results = checkPlayer(protect, player);
 
         event.setCancelled(results);
     }
 
     /**
      * If the block is protected, stop the break.
      */
     @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
     public void onBlockBreak(BlockBreakEvent event) {
         Player player = event.getPlayer();
         Block block = event.getBlock();
         Protected protect = new Protected(block);
 
         // Check if the area is protected.
         if (!protect.isProtected()) {
             return;
         }
         
         boolean results = checkPlayer(protect, player);
 
         event.setCancelled(results);
     }
 
     /**
      * If the entity is protected, stop the damage.
      */
     @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
     public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
         Entity entity = event.getEntity();
         Entity damager = event.getDamager();
         Protected protect = new Protected(entity);
         boolean results = true;
        boolean ignore = entity instanceof Monster
                || entity instanceof EnderDragon
                || entity instanceof EnderDragonPart;
 
        // Ignore specific entities with protection.
        if (ignore) {
             return;
         }
 
         // Check the player if the entity is a player.
         if (damager instanceof Player) {
             Player player = (Player) damager;
             boolean sneaking = player.isSneaking();
             results = checkPlayerAndSend(protect, player, sneaking);
         }
 
         event.setCancelled(results);
     }
 
     /**
      * If the entity is protected, block the interact.
      */
     @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
     public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
         Player player = event.getPlayer();
         Entity entity = event.getRightClicked();
         Protected protect = new Protected(entity);
 
         boolean sneaking = player.isSneaking();
         boolean results = checkPlayerAndSend(protect, player, sneaking);
 
         event.setCancelled(results);
     }
 
     /**
      * If the entity is protected, block it from breaking.
      */
     @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
     public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
         Entity entity = event.getEntity();
         Entity remover = event.getRemover();
         Protected protect = new Protected(entity);
         boolean results = true;
 
         // Check the player if the entity is a player.
         if (remover instanceof Player) {
             Player player = (Player) remover;
             boolean sneaking = player.isSneaking();
             results = checkPlayerAndSend(protect, player, sneaking);
         }
 
         event.setCancelled(results);
 
     }
 
     /**
      * If the block is being interacted by other than the members, block it.
      */
     @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
     public void onPlayerInteract(PlayerInteractEvent event) {
         Block block = event.getClickedBlock();
         Player player = event.getPlayer();
         Action action = event.getAction();
         Action right = Action.RIGHT_CLICK_BLOCK;
         Protected protect = new Protected(block);
 
         boolean sneaking = player.isSneaking() && action == right;
         boolean results = checkPlayerAndSend(protect, player, sneaking);
 
         event.setCancelled(results);
     }
 
     /**
      * If any of the blocks are protected, block the explosion.
      */
     @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
     public void onEntityExplode(EntityExplodeEvent event) {
         for (Block block : event.blockList()) {
             Protected protect = new Protected(block);
 
             if (protect.isProtected()) {
                 event.setCancelled(true);
                 break;
             }
         }
     }
 
     /**
      * If the block is protected, block it from burring.
      */
     @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
     public void onBlockBurn(BlockBurnEvent event) {
         Block block = event.getBlock();
         Protected protect = new Protected(block);
 
         if (protect.isProtected()) {
             event.setCancelled(true);
         }
     }
 
     /**
      * Gives users access to place protect signs.
      */
     @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
     public void onSignChange(SignChangeEvent event) {
         Block block = event.getBlock();
         Player player = event.getPlayer();
         Protected protect = new Protected(block);
 
         if (block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST) {
             if (event.getLine(0).equalsIgnoreCase(configuration.sign)) {
                 if (!protect.isProtected()) {
                     event.setLine(0, configuration.sign);
                     if (cmdbook.hasPermission(player, "eprotect.create.other")) {
                         if (event.getLine(1).equals("")) {
                             event.setLine(1, player.getName());
                         }
                     }
                     else {
                         event.setLine(1, player.getName());
                     }
                 }
                 else {
                     event.setCancelled(true);
                     player.sendMessage(ChatColor.GOLD + "This block is all ready protected.");
                 }
             }
         }
     }
 
     /**
      *  Sends the correct message to the player.
      *
      * @param protect The instance of protected.
      * @param player The player to send the messages.
      * @return true If we need to cancel the event.
      */
     private boolean checkPlayerAndSend(Protected protect, Player player, boolean sneaking) {
         boolean results = false;
         if (protect.isProtected()) {
             if (cmdbook.hasPermission(player, "eprotect.override")) {
                 if (sneaking)
                     player.sendMessage(protect.getMessage(2));
                 else if (!protect.isMember(player.getName()))
                     player.sendMessage(protect.getMessage(1));
             }
             else if (!protect.isMember(player.getName())) {
                 if (sneaking)
                     player.sendMessage(protect.getMessage(2));
                 else
                     player.sendMessage(protect.getMessage(0));
                 results = true;
             }
             else if (sneaking) {
                 player.sendMessage(protect.getMessage(2));
             }
         }
         else if (sneaking) {
             player.sendMessage(protect.getMessage(2));
         }
         return results;
     }
 
     /**
      *  Check if we should cancel the event for the player.
      *
      * @param protect The instance of protected.
      * @param player The player to send the messages.
      * @return true If we need to cancel the event.
      */
     private boolean checkPlayer(Protected protect, Player player) {
         boolean results = false;
         if (protect.isProtected()) {
             if (cmdbook.hasPermission(player, "eprotect.override")) {
                 results = false;
             }
             else if (!protect.isMember(player.getName())) {
                 results = true;
             }
         }
         return results;
     }
 
 }
