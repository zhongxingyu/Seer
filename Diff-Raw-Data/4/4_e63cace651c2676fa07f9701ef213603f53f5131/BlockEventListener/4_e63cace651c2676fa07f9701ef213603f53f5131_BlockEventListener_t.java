 package com.codisimus.plugins.chestlock.listeners;
 
 import com.codisimus.plugins.chestlock.ChestLock;
 import com.codisimus.plugins.chestlock.LockedDoor;
 import com.codisimus.plugins.chestlock.Safe;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.material.Door;
 
 /**
  * Listens for griefing events
  * 
  * @author Codisimus
  */
 public class BlockEventListener extends BlockListener {
 
     /**
      * Blocks Players from opening locked doors with redstone
      * 
      * @param event The BlockRedstoneEvent that occurred
      */
     @Override
     public void onBlockRedstoneChange(BlockRedstoneEvent event) {
         //Return if the Block is not a LockedDoor
         LockedDoor lockedDoor = ChestLock.findDoor(event.getBlock());
         if (lockedDoor == null)
             return;
         
         //Return if the key is unlockable
         if (lockedDoor.key == 0)
             return;
         
         //Allow Redstone to close a Door but not open it
         if (!((Door)lockedDoor.block.getState().getData()).isOpen())
             event.setNewCurrent(event.getOldCurrent());
     }
 
     /**
      * Prevents Players from breaking owned Blocks
      * 
      * @param event The BlockBreakEvent that occurred
      */
     @Override
     public void onBlockBreak (BlockBreakEvent event) {
         Block block = event.getBlock();
         Player player = event.getPlayer();
         
         //Check if the Block is a LockedDoor
         LockedDoor door = ChestLock.findDoor(block);
         if (door != null) {
             //Cancel the event if the Player is not the Owner of the LockedDoor and does not have the admin node
             if (!player.getName().equals(door.owner) && !ChestLock.hasPermission(player, "admin")) {
                 event.setCancelled(true);
                 return;
             }
             
             //Delete the LockedDoor from the saved data
             ChestLock.doors.remove(door);
             return;
         }
         
         //Return if the Block is not a Safe
         Safe safe = ChestLock.findSafe(block);
         if (safe == null)
             return;
 
         //Cancel the event if the Player is not the Owner of the Safe
        if (!safe.isOwner(player)) {
             event.setCancelled(true);
             return;
         }
         
         //Delete the Safe from the saved data
         ChestLock.removeSafe(safe);
     }
 }
