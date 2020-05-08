 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of VinciCode.
  * 
  * VinciCode is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * VinciCode is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with VinciCode.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.vincicode.listener;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.event.Event;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerItemHeldEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 import de.minestar.minestarlibrary.bookapi.MinestarBook;
 import de.minestar.minestarlibrary.messages.Message;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 import de.minestar.vincicode.core.VinciCodeCore;
 import de.minestar.vincicode.data.MailBox;
 import de.minestar.vincicode.util.BookHelper;
 
 public class ActionListener implements Listener {
 
     private void swapItems(Inventory inventory, int oldSlot, int newSlot) {
         // SWAP ITEMS
         ItemStack newItem = inventory.getItem(newSlot);
         ItemStack oldItem = inventory.getItem(oldSlot);
         if (newItem != null && !newItem.getType().equals(Material.AIR)) {
             newItem = newItem.clone();
         }
 
         // finally swap
         inventory.setItem(newSlot, oldItem);
         inventory.setItem(oldSlot, newItem);
     }
 
     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
             return;
         }
 
         ItemStack itemStack = event.getPlayer().getItemInHand();
         if (itemStack == null || !itemStack.getType().equals(Material.WRITTEN_BOOK))
             return;
 
         MinestarBook book = MinestarBook.loadBook(itemStack);
         if (book.getAuthor().equalsIgnoreCase(MailBox.MAIL_BOX_HEAD)) {
             event.setCancelled(true);
             event.setUseInteractedBlock(Event.Result.DENY);
             event.setUseItemInHand(Event.Result.DENY);
 
             // get mailbox
             MailBox mailBox = VinciCodeCore.messageManger.getMailBox(event.getPlayer().getName());
             if (mailBox == null) {
                 PlayerUtils.sendError(event.getPlayer(), VinciCodeCore.NAME, "Du hast keine Nachrichten!");
                 return;
             }
 
             Message message = mailBox.getCurrentMessage();
             if (message != null && !message.isRead()) {
                 mailBox.markAsRead(message);
                 if (VinciCodeCore.dbHandler.setMessageRead(message)) {
                     PlayerUtils.sendSuccess(event.getPlayer(), VinciCodeCore.NAME, "Die Nachricht wurde als gelesen markiert.");
                 }
             }
         }
     }
 
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event) {
         if (VinciCodeCore.messageManger.hasNewMessage(event.getPlayer().getName())) {
             int newMessages = VinciCodeCore.messageManger.getNewMessageCount(event.getPlayer().getName());
             String message = "Du hast " + ChatColor.GOLD + ChatColor.BOLD + newMessages + ChatColor.RESET + ChatColor.GRAY + " neue Nachricht";
             if (newMessages != 1) {
                 message += "en";
             }
             message += ".";
             PlayerUtils.sendInfo(event.getPlayer(), VinciCodeCore.NAME, message);
         }
     }
 
     @EventHandler
     public void onItemHeldChange(PlayerItemHeldEvent event) {
         if (!event.getPlayer().isSneaking())
             return;
 
         ItemStack itemStack = event.getPlayer().getInventory().getItem(event.getPreviousSlot());
         if (itemStack == null || !itemStack.getType().equals(Material.WRITTEN_BOOK))
             return;
 
         MinestarBook book = MinestarBook.loadBook(itemStack);
         if (book.getAuthor().equalsIgnoreCase(MailBox.MAIL_BOX_HEAD)) {
             // get mailbox
             MailBox mailBox = VinciCodeCore.messageManger.getMailBox(event.getPlayer().getName());
             if (mailBox == null) {
                 PlayerUtils.sendError(event.getPlayer(), VinciCodeCore.NAME, "Du hast keine Nachrichten!");
 
                 // SWAP ITEMS
                 this.swapItems(event.getPlayer().getInventory(), event.getNewSlot(), event.getPreviousSlot());
                 return;
             }
 
            boolean forward = (event.getNewSlot() == 0 && event.getPreviousSlot() == 8) || (event.getNewSlot() > event.getPreviousSlot() && !(event.getPreviousSlot() == 0 && event.getNewSlot() == 8));
 
             if (forward) {
                 if (mailBox.hasNext()) {
                     Message message = mailBox.next();
                     book.setPages(BookHelper.format(message));
 
                     String text = "Nachricht ";
                     if (message.isRead()) {
                         text += "" + ChatColor.GOLD + (mailBox.getIndex() + 1) + ChatColor.GRAY;
                     } else {
                         text += "" + ChatColor.RED + (mailBox.getIndex() + 1) + ChatColor.GRAY;
                     }
                     text += " von " + mailBox.getMessageCount();
                     PlayerUtils.sendInfo(event.getPlayer(), VinciCodeCore.NAME, text);
                 } else {
                     PlayerUtils.sendError(event.getPlayer(), VinciCodeCore.NAME, "Keine weiteren Nachrichten.");
                 }
             } else {
                 if (mailBox.hasPrev()) {
                     Message message = mailBox.prev();
                     book.setPages(BookHelper.format(message));
                     String text = "Nachricht ";
                     if (message.isRead()) {
                        text += "" + ChatColor.GOLD + (mailBox.getIndex() + 1) + ChatColor.GRAY;
                     } else {
                        text += "" + ChatColor.RED + (mailBox.getIndex() + 1) + ChatColor.GRAY;
                     }
                     text += " von " + mailBox.getMessageCount();
                     PlayerUtils.sendInfo(event.getPlayer(), VinciCodeCore.NAME, text);
                 } else {
                     PlayerUtils.sendError(event.getPlayer(), VinciCodeCore.NAME, "Keine vorherigen Nachrichten.");
                 }
             }
 
             // SWAP ITEMS
             this.swapItems(event.getPlayer().getInventory(), event.getNewSlot(), event.getPreviousSlot());;
         }
     }
 }
