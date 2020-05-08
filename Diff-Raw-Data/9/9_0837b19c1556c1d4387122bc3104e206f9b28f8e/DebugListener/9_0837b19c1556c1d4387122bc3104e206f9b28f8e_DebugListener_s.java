 package de.minestar.moneypit.manager;
 
 import java.util.Random;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 import de.minestar.moneypit.Core;
 import de.minestar.moneypit.data.BlockVector;
 import de.minestar.moneypit.data.Protection;
 import de.minestar.moneypit.data.ProtectionType;
 import de.minestar.moneypit.data.SubProtectionHolder;
 import de.minestar.moneypit.modules.Module;
 
 public class DebugListener implements Listener {
 
     private ModuleManager moduleManager;
     private ProtectionManager protectionManager;
     private BlockVector vector;
 
     public DebugListener() {
         this.moduleManager = Core.moduleManager;
         this.protectionManager = Core.protectionManager;
         this.vector = new BlockVector("", 0, 0, 0);
     }
 
     @EventHandler
     public void onBlockBreak(BlockBreakEvent event) {
         // event is already cancelled => return
         if (event.isCancelled()) {
             return;
         }
         
         // update the BlockVector
         this.vector.update(event.getBlock().getLocation());
         
         // Block is not protected => return
         if(!this.protectionManager.hasAnyProtection(vector)) {
             return;
         }
         
         // Block is protected => check: Protection OR SubProtection
         if(this.protectionManager.hasProtection(vector)) {
             // we have a regular protection => get the module (must be registered)
             Module module = this.moduleManager.getModule(event.getClickedBlock().getTypeId());
             if (module == null) {
                 PlayerUtils.sendError(event.getPlayer(), Core.NAME, "Module for block '" + event.getClickedBlock().getType().name() + "' is not registered!");
                 return;
             }
             
             // check permission
            if(!UtilPermissions.playerCanUseCommand(event.getPlayer(), "moneypit.protect." + module.getModuleName())) {
                 PlayerUtils.sendError(event.getPlayer(), Core.NAME, "You are not allowed to break this protected block.");
                 event.setCancelled(true);
                 return;
             }
             
             // remove protection
             this.protectionManager.removeProtection(vector);
             
             // send info
            PlayerUtils.sendSuccess(event.getPlayer(), Core.NAME, "Protection removed");
         }
         else {
             // we have a SubProtection => send error & cancel the event
             PlayerUtils.sendError(event.getPlayer(), Core.NAME, "This block has a subprotection and cannot be broken.");
             event.setCancelled(true);            
         }
     }
 
     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent event) {
         // event is already cancelled => return
         if (event.isCancelled()) {
             return;
         }
 
         Action action = event.getAction();
         if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
             this.vector.update(event.getClickedBlock().getLocation());
             // no sneak => print info about protections & return
             if (!event.getPlayer().isSneaking()) {
                 // CHECK: Protection?
                 Protection protection = this.protectionManager.getProtection(this.vector);
                 if (protection != null) {
                     PlayerUtils.sendInfo(event.getPlayer(), Core.NAME, "This block is protected!");
                     PlayerUtils.sendInfo(event.getPlayer(), Core.NAME, protection.toString());
                     return;
                 }
 
                 // CHECK: SubProtection?
                 SubProtectionHolder holder = this.protectionManager.getSubProtectionHolder(this.vector);
                 if (holder != null) {
                     PlayerUtils.sendInfo(event.getPlayer(), Core.NAME, "Block is a subprotection!");
                     for (int i = 0; i < holder.getSize(); i++) {
                         PlayerUtils.sendInfo(event.getPlayer(), Core.NAME, "#" + (i + 1) + " : " + holder.getProtections().get(i));
                     }
                     return;
                 }
 
                 PlayerUtils.sendInfo(event.getPlayer(), Core.NAME, "This block is not protected!");
                 return;
             }
 
             // the module must be registered
             Module module = this.moduleManager.getModule(event.getClickedBlock().getTypeId());
             if (module == null) {
                 PlayerUtils.sendError(event.getPlayer(), Core.NAME, "Module for block '" + event.getClickedBlock().getType().name() + "' is not registered!");
                 return;
             }
 
             boolean isLeftClick = (action == Action.LEFT_CLICK_BLOCK);
             // left click => try to add a protection
             // right click => try to remove the protection
             if (isLeftClick) {
                 if (!this.protectionManager.hasProtection(this.vector) && !this.protectionManager.hasSubProtectionHolder(this.vector)) {
                     Random random = new Random();
                     module.addProtection(random.nextInt(1000000), vector, event.getPlayer().getName(), ProtectionType.PRIVATE, event.getClickedBlock().getData());
                     PlayerUtils.sendSuccess(event.getPlayer(), Core.NAME, "This block is now protected!");
                 } else {
                     PlayerUtils.sendError(event.getPlayer(), Core.NAME, "This block is already protected!");
                 }
                 return;
             } else {
                 if (!this.protectionManager.hasProtection(this.vector)) {
                     PlayerUtils.sendError(event.getPlayer(), Core.NAME, "This block is not protected!");
                 } else {
                     this.protectionManager.removeProtection(this.vector);
                     PlayerUtils.sendError(event.getPlayer(), Core.NAME, "This block is no longer protected!");
                 }
             }
         }
     }
 }
