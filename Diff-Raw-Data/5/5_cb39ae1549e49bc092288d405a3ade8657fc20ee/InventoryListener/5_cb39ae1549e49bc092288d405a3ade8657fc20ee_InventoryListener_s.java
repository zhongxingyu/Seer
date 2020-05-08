 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of TheRock.
  * 
  * TheRock is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * TheRock is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with TheRock.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.therock.listener;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Dispenser;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 
 import com.bukkit.gemo.utils.InventoryUtils;
 
 import de.minestar.therock.Core;
 import de.minestar.therock.data.InventoryEventTypes;
 import de.minestar.therock.manager.MainConsumer;
 import de.minestar.therock.manager.MainManager;
 
 public class InventoryListener implements Listener {
 
     private MainManager mainManager;
     private MainConsumer mainConsumer;
     private StringBuilder queueBuilder;
 
     private HashMap<String, Location> openedInventories;
     private static final Set<Integer> checkableBlocks = new HashSet<Integer>(Arrays.asList(Material.CHEST.getId(), Material.DISPENSER.getId(), Material.FURNACE.getId(), Material.BURNING_FURNACE.getId(), Material.BREWING_STAND.getId()));
 
     public InventoryListener() {
         this.mainManager = Core.mainManager;
         this.mainConsumer = Core.mainConsumer;
         this.queueBuilder = new StringBuilder();
         this.openedInventories = new HashMap<String, Location>();
     }
 
     public boolean isContainerBlock(Block block) {
         return checkableBlocks.contains(block.getTypeId());
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerInteract(PlayerInteractEvent event) {
         // /////////////////////////////////
         // event cancelled => return
         // /////////////////////////////////
         if (event.isCancelled() || !this.mainManager.isWorldWatched(event.getPlayer().getWorld()) || !this.mainManager.getWorld(event.getPlayer().getWorld()).logInventoryChange())
             return;
 
         // rightclick on a block?
         if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
             return;
 
         // is the block an interactable block?
         if (!checkableBlocks.contains(event.getClickedBlock().getTypeId()))
             return;
 
         // add the player to the list
         this.openedInventories.put(event.getPlayer().getName(), event.getClickedBlock().getLocation());
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onInventoryClose(InventoryCloseEvent event) {
         // /////////////////////////////////
         // event cancelled => return
         // /////////////////////////////////
         if (!this.mainManager.isWorldWatched(event.getPlayer().getWorld()) || !this.mainManager.getWorld(event.getPlayer().getWorld()).logInventoryChange())
             return;
 
         // remove the player from the list
         this.openedInventories.remove(event.getPlayer().getName());
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onInventoryClick(InventoryClickEvent event) {
         // /////////////////////////////////
         // event cancelled => return
         // /////////////////////////////////
         if (event.isCancelled() || !this.mainManager.isWorldWatched(event.getWhoClicked().getWorld()) || !this.mainManager.getWorld(event.getWhoClicked().getWorld()).logInventoryChange())
             return;
 
         // get the player
         Player player = (Player) event.getWhoClicked();
 
         // does the user have a saved location?
         Location loc = this.openedInventories.get(player.getName());
         if (loc == null)
             return;
 
         // check the inventory type
         InventoryType type = event.getInventory().getType();
         if (type != InventoryType.CHEST && type != InventoryType.DISPENSER && type != InventoryType.FURNACE && type != InventoryType.BREWING) {
             return;
         }
 
         if (event.getRawSlot() < event.getInventory().getSize()) {
             // get the current item
             ItemStack inCursor = event.getCursor();
 
             // get the clicked item
             ItemStack inSlot = event.getInventory().getItem(event.getSlot());
 
             // get some vars
             boolean isShiftClick = event.isShiftClick();
             boolean isLeftClick = event.isLeftClick();
             boolean isRightClick = event.isRightClick();
             boolean cursorNull = (inCursor == null || inCursor.getTypeId() == Material.AIR.getId());
             boolean slotNull = (inSlot == null || inSlot.getTypeId() == Material.AIR.getId());
 
             // Cursor = null && Slot == null => nothing happens
             if (cursorNull && slotNull) {
                 return;
             }
 
             // Cursor == null && Slot != null => we only took something out
             if (cursorNull && !slotNull) {
                 Block block = loc.getBlock();
                 if (isShiftClick || isLeftClick) {
                     // shift OR left click => take all out
 
                     // /////////////////////////////////
                     // create data
                     // /////////////////////////////////
                     this.addInventoryChange(player.getName(), InventoryEventTypes.PLAYER_TOOK.getID(), player.getWorld().getName(), block.getX(), block.getY(), block.getZ(), inSlot.getTypeId(), inSlot.getDurability(), inSlot.getAmount());
                 } else if (isRightClick) {
                     // right click => take the half out
                     int amount = (int) (inSlot.getAmount() / 2);
                     if (inSlot.getAmount() % 2 != 0) {
                         amount = amount + 1;
                     }
 
                     // /////////////////////////////////
                     // create data
                     // /////////////////////////////////
                     this.addInventoryChange(player.getName(), InventoryEventTypes.PLAYER_TOOK.getID(), player.getWorld().getName(), block.getX(), block.getY(), block.getZ(), inSlot.getTypeId(), inSlot.getDurability(), amount);
                 }
                 return;
             }
 
             // Slot == null && Cursor != null => we only place
             if (slotNull && !cursorNull) {
                 Block block = loc.getBlock();
                 if (isShiftClick) {
                     // shift click => nothing happen
                     return;
                 } else if (isLeftClick) {
                     // /////////////////////////////////
                     // create data
                     // /////////////////////////////////
                     this.addInventoryChange(player.getName(), InventoryEventTypes.PLAYER_PLACED.getID(), player.getWorld().getName(), block.getX(), block.getY(), block.getZ(), inCursor.getTypeId(), inCursor.getDurability(), inCursor.getAmount());
                 } else if (isRightClick) {
                     // right click => only place 1
 
                     // /////////////////////////////////
                     // create data
                     // /////////////////////////////////
                     this.addInventoryChange(player.getName(), InventoryEventTypes.PLAYER_PLACED.getID(), player.getWorld().getName(), block.getX(), block.getY(), block.getZ(), inCursor.getTypeId(), inCursor.getDurability(), 1);
                 }
                 return;
             }
 
             // Slot != null && Cursor != null => we take first, and then place
             if (!slotNull && !cursorNull) {
                 Block block = loc.getBlock();
                 boolean itemsEqual = inSlot.getTypeId() == inCursor.getTypeId() && inSlot.getDurability() == inCursor.getDurability();
                 if (!itemsEqual) {
                     // items are not equal => take out and place
 
                     // /////////////////////////////////
                     // create data : taken item
                     // /////////////////////////////////
                     this.addInventoryChange(player.getName(), InventoryEventTypes.PLAYER_TOOK.getID(), player.getWorld().getName(), block.getX(), block.getY(), block.getZ(), inSlot.getTypeId(), inSlot.getDurability(), inSlot.getAmount());
 
                     // /////////////////////////////////
                     // create data : placed item
                     // /////////////////////////////////
                     this.addInventoryChange(player.getName(), InventoryEventTypes.PLAYER_PLACED.getID(), player.getWorld().getName(), block.getX(), block.getY(), block.getZ(), inCursor.getTypeId(), inCursor.getDurability(), inCursor.getAmount());
                 } else {
                     // items are equal => check the stacksizes
                     // maxStackSize already reached => return
                     if (inSlot.getAmount() >= inSlot.getMaxStackSize()) {
                         return;
                     }
 
                     if (isLeftClick) {
                         // left click => check stacksizes and queue
                         int wantedSize = inSlot.getAmount() + inCursor.getAmount();
                         if (wantedSize > inSlot.getMaxStackSize()) {
                             wantedSize = inSlot.getMaxStackSize() - inSlot.getAmount();
                             // /////////////////////////////////
                             // create data : placed item
                             // /////////////////////////////////
                             this.addInventoryChange(player.getName(), InventoryEventTypes.PLAYER_PLACED.getID(), player.getWorld().getName(), block.getX(), block.getY(), block.getZ(), inSlot.getTypeId(), inSlot.getDurability(), wantedSize);
                         } else {
                             // /////////////////////////////////
                             // create data : placed item
                             // /////////////////////////////////
                             this.addInventoryChange(player.getName(), InventoryEventTypes.PLAYER_PLACED.getID(), player.getWorld().getName(), block.getX(), block.getY(), block.getZ(), inSlot.getTypeId(), inSlot.getDurability(), wantedSize);
                         }
                         return;
                     } else if (isRightClick) {
                         // right click => check stacksizes and queue
                         int wantedSize = inSlot.getAmount() + 1;
                         if (wantedSize < inSlot.getMaxStackSize()) {
                             // /////////////////////////////////
                             // create data : placed item * 1
                             // /////////////////////////////////
                             this.addInventoryChange(player.getName(), InventoryEventTypes.PLAYER_PLACED.getID(), player.getWorld().getName(), block.getX(), block.getY(), block.getZ(), inSlot.getTypeId(), inSlot.getDurability(), 1);
                         }
                         return;
                     }
                 }
             }
         } else {
             // get the clicked item
             ItemStack inSlot = player.getInventory().getItem(event.getSlot());
 
             // get some vars
             boolean isShiftClick = event.isShiftClick();
             boolean slotNull = (inSlot == null || inSlot.getTypeId() == Material.AIR.getId());
 
             // no shift-click => return
             if (!isShiftClick) {
                 return;
             }
 
             // we need an item in the slot
             if (slotNull) {
                 return;
             }
             // get the current containerblock
             Block block = loc.getBlock();
             if (block.getTypeId() == Material.CHEST.getId()) {
                 // handle chest
                 Chest chest = (Chest) block.getState();
                 int freeSpace = InventoryUtils.countFreeSpace(chest.getInventory(), inSlot);
 
                 int amount = inSlot.getAmount();
                 // check the free amount
                 if (freeSpace < amount) {
                     amount = freeSpace;
                 }
 
                 // check the amount
                 if (amount < 1) {
                     return;
                 }
 
                 // /////////////////////////////////
                 // create data : placed item
                 // /////////////////////////////////
                 this.addInventoryChange(player.getName(), InventoryEventTypes.PLAYER_PLACED.getID(), player.getWorld().getName(), block.getX(), block.getY(), block.getZ(), inSlot.getTypeId(), inSlot.getDurability(), amount);
                 return;
             } else if (block.getTypeId() == Material.DISPENSER.getId()) {
                 // handle dispenser
                 Dispenser dispenser = (Dispenser) block.getState();
                 int freeSpace = InventoryUtils.countFreeSpace(dispenser.getInventory(), inSlot);
 
                 int amount = inSlot.getAmount();
                 // check the free amount
                 if (freeSpace < amount) {
                     amount = freeSpace;
                 }
 
                 // check the amount
                 if (amount < 1) {
                     return;
                 }
 
                 // /////////////////////////////////
                 // create data : placed item
                 // /////////////////////////////////
                 this.addInventoryChange(player.getName(), InventoryEventTypes.PLAYER_PLACED.getID(), player.getWorld().getName(), block.getX(), block.getY(), block.getZ(), inSlot.getTypeId(), inSlot.getDurability(), amount);
                 return;
             } else if (block.getTypeId() == Material.FURNACE.getId() || block.getTypeId() == Material.BURNING_FURNACE.getId()) {
                 // TODO: handle furnaces
                 // we have to wait for 1.3 for this... shiftclicking will
                 // currently end in a gamecrash
                 return;
             } else if (block.getTypeId() == Material.BREWING_STAND.getId()) {
                 // TODO: handle brewing stands
                 // we have to wait for 1.3 for this... shiftclicking will
                 // currently end in a gamecrash
                 return;
             }
         }
     }
 
     private void addInventoryChange(String reason, int eventType, String worldName, int blockX, int blockY, int blockZ, int ID, short Data, int Amount) {
         // "("
         this.queueBuilder.append("(");
         // "TIMESTAMP"
         this.queueBuilder.append(System.currentTimeMillis());
         // "REASON"
         this.queueBuilder.append(", ");
         this.queueBuilder.append("'" + reason + "'");
         // "EVENTTYPE"
         this.queueBuilder.append(", ");
         this.queueBuilder.append(eventType);
         // "POSITION: X"
         this.queueBuilder.append(", ");
         this.queueBuilder.append(blockX);
         // "POSITION: Y"
         this.queueBuilder.append(", ");
         this.queueBuilder.append(blockY);
         // "POSITION: Z"
         this.queueBuilder.append(", ");
         this.queueBuilder.append(blockZ);
         // "ID"
         this.queueBuilder.append(", ");
         this.queueBuilder.append(ID);
         // "SUBDATA"
         this.queueBuilder.append(", ");
         this.queueBuilder.append(Data);
         // "AMOUNT"
         this.queueBuilder.append(", ");
         this.queueBuilder.append(Amount);
         // ")"
         this.queueBuilder.append(")");
 
         // /////////////////////////////////
         // add to queue
         // /////////////////////////////////
         this.mainConsumer.appendInventoryEvent(worldName, this.queueBuilder);
 
         // /////////////////////////////////
         // reset data
         // /////////////////////////////////
         this.queueBuilder.setLength(0);
     }
 }
