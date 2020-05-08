 /**
  * Small plugin to enable the storage of experience points in an item.
  * 
  * Rewrite of the original PearlXP created by Nebual of nebtown.info in March 2012.
  * 
  * rewrite by: Marex, Zonta.
  * 
  * contact us at : plugins@x-dns.org
  * 
  * Copyright (C) 2012 belongs to their respective owners
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package info.nebtown.PearlXP;
 
 import info.nebtown.PearlXP.PearlXP.MsgKeys;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Result;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 public class PearlXPListener implements Listener {
 
 	private static final ChatColor TEXT_COLOR = ChatColor.BLUE;
 	private static final ChatColor INFO_COLOR = ChatColor.AQUA;
 
 	private static final int QUICKBAR_SLOT_NB = 9;
 
 	// messages
 	private String infoXpMsg;
 	private String infoXpEmptyMsg;
 	private String imbueXpMsg;
 	private String restoreXpMsg;
 
 	public PearlXPListener(PearlXP plugin) {
 		plugin.getServer().getPluginManager().registerEvents(this, plugin);
 
 		this.infoXpMsg = plugin.getMessage(MsgKeys.INFO_XP);
 		this.infoXpEmptyMsg = plugin.getMessage(MsgKeys.INFO_XP_EMPTY);
 		this.imbueXpMsg = plugin.getMessage(MsgKeys.IMBUE_XP);
 		this.restoreXpMsg = plugin.getMessage(MsgKeys.RESTORE_XP);
 
 	}
 
 
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		XpContainer gem;
 		Action action = event.getAction();
 
 		int xp = 0;
 		double xpTaxed = 0;
 
 		Player player = event.getPlayer();
 
 		if (event.hasItem() && XpContainer.isAnXpContainer(event.getItem())) {
 
 			gem = new XpContainer(event.getItem());
 
 			if (gem.canStoreXp() && gem.getStoredXp() == 0) { // The item possess no XP
 
 				if (action == Action.RIGHT_CLICK_BLOCK) {
 					// Show the amount of XP stored
 
 					event.setUseItemInHand(Result.DENY); //Don't throw the item!
 
 					// the item is empty and the player clicked "on is feet"
 					sendInfo(infoXpEmptyMsg, INFO_COLOR, player, gem);
 
				} else if (player.getTotalExperience() > 0
 						&& (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) {
 					// Store some XP in the item
 
 					if (player.getTotalExperience() > XpContainer.getmaxExp() + XpContainer.getmaxExp() * XpContainer.getXpTax()) {
 
 						xp = XpContainer.getmaxExp();
 						xpTaxed = xp * XpContainer.getXpTax();
 					} else {
 
 						xp = player.getTotalExperience();
 						xpTaxed = xp * XpContainer.getXpTax();
						xp = xp - (int)(xpTaxed);
 					}
 
 					gem = storeXp(xp, gem, player);
 					removePlayerXp((int) (xp + xpTaxed), player);
 
 					// Friendly message !
 					sendInfo(imbueXpMsg, player, gem);
 
 					// Visual and sound effects
 					player.getWorld().playEffect(player.getEyeLocation(), Effect.ENDER_SIGNAL, 0);
 					player.playEffect(player.getEyeLocation(), Effect.EXTINGUISH, 0);
 				}
 
 			} else if (gem.canContainXp()) {
 
 				if (gem.getStoredXp() > 0 && action == Action.RIGHT_CLICK_AIR 
 						|| action == Action.RIGHT_CLICK_BLOCK) {
 					// Show the stored XP...
 
 					event.setUseItemInHand(Result.DENY); //Don't throw the item!
 
 					if (gem.getStoredXp() == 0) {
 						sendInfo(infoXpEmptyMsg, INFO_COLOR, player, gem);
 					} else {
 						sendInfo(infoXpMsg, INFO_COLOR, player, gem);
 					}
 
 
 				} else if (gem.getStoredXp() > 0 
 						&& (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) {
 					// Restore XP to the player
 
 					xp = gem.getStoredXp();
 
 					// Remove all Stored XP
 					storeXp(0, gem, player);
 
 					// give the player the XP
 					player.giveExp(xp);
 					sendInfo(restoreXpMsg, player, gem);
 
 					// Special effects!
 					player.playEffect(player.getEyeLocation(), Effect.GHAST_SHOOT, 0);
 					player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2, 1));
 					player.getWorld().playEffect(player.getEyeLocation(), Effect.SMOKE, BlockFace.SELF);
 				}
 			}
 
 		}
 
 	} //onPlayerInteract
 
 	@EventHandler
 	public void onInventoryClicked(InventoryClickEvent event) {
 		XpContainer clickedGem;
 		XpContainer cursorGem;
 		Inventory inv;
 		int transfertQty;
 		int startSlot;
 		int endSlot;
 
 		// we ignore throwing items and non soul gems items...
 		if(XpContainer.isAnXpContainer(event.getCurrentItem()) 
 				&& event.getSlotType() != InventoryType.SlotType.OUTSIDE) {
 
 			inv = event.getInventory();
 			clickedGem = new XpContainer(event.getCurrentItem());
 
 			if (event.isShiftClick()) {
 				// Transfert stacks to the other "section" of an inventory
 				event.setCancelled(true);
 
 				if (inv.getType() == InventoryType.CHEST 
 						|| inv.getType() == InventoryType.DISPENSER) {
 					// In chest and dispenser we switch between player and entity inventory
 					// TODO Reverse iterate to match minecraft implementation better
 
 					if (event.getRawSlot() <= inv.getSize()) {
 						// The player clicked outside is own inventory
 						// We transfer in it...
 						inv = event.getWhoClicked().getInventory();
 					}
 
 					startSlot = 0;
 					endSlot = inv.getSize();
 
 					stackGems(clickedGem, event, inv, startSlot, endSlot);
 
 				} else {
 					// Use the player inventory
 					inv = event.getWhoClicked().getInventory();
 
 					// We alternate between the Quickbar and the main inventory block
 					if (event.getSlotType() == InventoryType.SlotType.QUICKBAR) {
 						startSlot = QUICKBAR_SLOT_NB;
 						endSlot = inv.getSize();
 					} else {
 						startSlot = 0;
 						endSlot = QUICKBAR_SLOT_NB;
 					}
 
 					// Stack all gems and transfer the rest in empty slots...
 					stackGems(clickedGem, event, inv, startSlot, endSlot);
 				}
 
 
 
 			} else if (XpContainer.isAnXpContainer(event.getCursor())) {
 				// the gem is click with another gem
 				event.setCancelled(true);
 				cursorGem = new XpContainer(event.getCursor());
 
 				//check if stacking is possible and stack, leftover are on cursor, if not switch the item
 				if (event.isLeftClick()) {
 					transfertQty = cursorGem.getAmount();
 				} else { // right clicked
 					transfertQty = 1;
 				}
 
 				if (cursorGem.equals(clickedGem)) {
 					transfertGems(cursorGem, clickedGem, inv, event, transfertQty, true);
 
 				} else {
 					// Switch items
 					event.setCursor(clickedGem);
 					event.setCurrentItem(cursorGem);
 				}
 			}
 
 		}
 
 	}
 
 	@EventHandler
 	public void onGemPickUp(PlayerPickupItemEvent event) {
 		ItemStack pickUpItem = event.getItem().getItemStack();
 		Inventory inv;
 		XpContainer pickUpGem;
 		XpContainer similarGem;
 
 		if (XpContainer.isAnXpContainer(pickUpItem)) {
 			event.setCancelled(true);
 			pickUpGem = new XpContainer(pickUpItem);
 			inv = event.getPlayer().getInventory();
 
 			// find a stack to add on top or puts it in an empty space,
 			// otherwise let it on the ground
 			similarGem = findSimilarStack(pickUpGem, inv);
 			if (similarGem != null) {
 
 				if (pickUpGem.getAmount() == 1) {
 					similarGem.setAmount(similarGem.getAmount() + 1);
 					event.getItem().remove();
 				}
 
 			} else if (inv.firstEmpty() >= 0) {
 				inv.setItem(inv.firstEmpty(), pickUpItem);
 				event.getItem().remove();
 			}
 		}
 	}
 
 
 	/**
 	 * Format the message to add variables values
 	 * @param msg message
 	 * @param xp item xp
 	 * @param playerXp player total xp
 	 * @return the modified string
 	 */
 	private String formatMsg(String msg, int xp, int playerXp) {
 		String[] values = { XpContainer.getItemName(),
 				String.valueOf(xp),
 				String.valueOf(playerXp) };
 
 		return formatMsg(msg, values);
 	}
 
 	/**
 	 * Format the message to add variables values
 	 * @param msg message
 	 * @param values Array of the values to display
 	 * @return the modified string
 	 */
 	private String formatMsg(String msg, String[] values) {
 		String[] keys = { "item_name", "xp", "player_xp" };
 
 		if (msg != null) {
 			for (int i = 0; i < keys.length && i < values.length; i++) {
 				msg = msg.replaceAll("\\$\\{" + keys[i] + "\\}", values[i]);
 			}
 		}
 
 		return msg;
 	}
 
 
 	/**
 	 * Send the player an information message with the default text color.
 	 * @param s message
 	 * @param p player to inform
 	 */
 	private void sendInfo(String msg, Player p, XpContainer i) {
 		if (msg != null) {
 			sendInfo(msg, TEXT_COLOR, p, i);
 		}
 	}
 
 	/**
 	 * Send the player an information message with the specified text color.
 	 * @param s message
 	 * @param p player to inform
 	 */
 	private void sendInfo(String msg, ChatColor c, Player p, XpContainer i) {
 		if (msg != null) {
 			p.sendMessage(c + formatMsg(msg, i.getStoredXp(), p.getTotalExperience()));
 		}
 	}
 
 	/**
 	 * Remove a number of XP from a given player
 	 * @param xp the XP to remove
 	 * @param p player
 	 */
 	private void removePlayerXp(int xp, Player p) {
 		int currentXp = p.getTotalExperience();
 
 		// Reset level to fix update bug
 		p.setTotalExperience(0);
 		p.setExp(0);
 		p.setLevel(0);
 
 		p.giveExp(currentXp - xp);
 
 	}
 
 	/**
 	 * Find first not full stack with the same property. Return null if nothing
 	 * found.
 	 * 
 	 * @param stack {@link XpContainer} with the property looking for
 	 * @param inv inventory
 	 * @return ItemStack found
 	 */
 	private XpContainer findSimilarStack(XpContainer stack, Inventory inv) {
 		return findSimilarStack(stack, inv, 0, inv.getSize());
 	}
 
 
 	/**
 	 * Find the first not full stack of XpContainer with the same property starting at start
 	 * and ending at the index stop.
 	 * 
 	 * @param stack {@link XpContainer} with the property looking for
 	 * @param inv inventory
 	 * @param start the index to start the search
 	 * @param stop
 	 * @return the XpContainer found or null if nothing found
 	 */
 	private XpContainer findSimilarStack(XpContainer stack, Inventory inv, int start, int stop) {
 		ItemStack[] items = inv.getContents();
 		XpContainer gem = null;
 		boolean found = false;
 
 		if (stop > items.length) stop = items.length;
 
 		for (int i = start; i < stop && !found; i++) {
 			if (items[i] != null) {
 				gem = new XpContainer(items[i]);
 
 				if (gem.getAmount() < gem.getMaxStackSize() && gem.equals(stack)) {
 					found = true;
 				}
 			}
 		}
 
 		return found ? gem : null;
 	}
 
 	/**
 	 * Store the given amount of XP in the item. If other uncompleted stack
 	 * exists with the correct XP the method stack them together.
 	 * 
 	 * @param item ItemStack to store XP
 	 * @param xp experience points
 	 * @param inv inventory of the player
 	 */
 	private XpContainer storeXp(int xp, XpContainer item,  Player player) {
 		XpContainer similarStack;
 		XpContainer newGem;
 		PlayerInventory inv = player.getInventory();
 		int slot = inv.firstEmpty();
 		Block lookingBlock;
 
 		newGem = new XpContainer(item.clone());
 		newGem.setStoredXp(xp);
 		similarStack = findSimilarStack(newGem, inv);
 
 		if (item.getAmount() == 1 && similarStack == null) {
 
 			inv.setItemInHand(newGem);
 
 		} else { // We can unstack stuff!
 
 			if (similarStack != null) {
 				// Stack on top of
 
 				similarStack.setAmount(similarStack.getAmount() + 1);
 
 			} else { // no similar stack
 
 				// Only create one item...
 				newGem.setAmount(1);
 
 				if (slot >= 0) {
 
 					inv.setItem(slot, newGem);
 
 				} else {
 					// The item is in a stack and cannot be unstack
 					// We drop the item where the player is looking
 					lookingBlock = inv.getHolder().getLastTwoTargetBlocks(null, 2).get(0);
 					new PlayerDropItemEvent(player, player.getWorld().dropItem(lookingBlock.getLocation(), newGem));
 				}
 			}
 
 			// Remove the item used
 			if (item.getAmount() == 1) {
 				inv.setItemInHand(null);
 			} else {
 				item.setAmount(item.getAmount() - 1);
 
 			}
 		}
 
 		return newGem;
 
 	}
 
 
 	/**
 	 * Transfert all possible gems in the gemToTransfert stack into another stack of gems.
 	 * @param gemToTransfert
 	 * @param gemStack
 	 * @param inv inventory
 	 * @param event {@link InventoryClickEvent} that triggered the transfert
 	 * @param onCursor if the item to transfert is on the cursor
 	 * @return true if all the items where put into the gemStack
 	 */
 	private boolean transfertGems(XpContainer gemToTransfert, XpContainer gemStack, Inventory inv, InventoryClickEvent event, boolean onCursor) {
 		return transfertGems(gemToTransfert, gemStack, inv, event, gemToTransfert.getAmount(), onCursor);
 	}
 
 
 	/**
 	 * Transfert all possible gems in the gemToTransfert stack into another stack of gems.
 	 * @param gemToTransfert
 	 * @param gemStack
 	 * @param inv inventory
 	 * @param event {@link InventoryClickEvent} that triggered the transfert
 	 * @param quantity quantity to transfert
 	 * @param onCursor if the item to transfert is on the cursor
 	 * @return true if all the items where put into the gemStack
 	 */
 	private boolean transfertGems(XpContainer gemToTransfert, XpContainer gemStack, Inventory inv, InventoryClickEvent event, int quantity ,boolean onCursor) {
 		int transfertQty = 0;
 		boolean removedAll = false;
 
 
 		// Check the maximum possible to transfert
 		if (gemStack.getAmount() + quantity <= gemStack.getMaxStackSize()) {
 
 			transfertQty = quantity;
 			if (quantity == gemToTransfert.getAmount()) {
 				// We remove the stack completly
 				removedAll = true;
 
 				if (onCursor) {
 					event.setCursor(null);
 				} else {
 					event.setCurrentItem(null);
 				}
 			}
 
 		} else {
 			transfertQty = gemStack.getMaxStackSize() - gemStack.getAmount();
 		}
 
 		if (!removedAll) {
 			gemToTransfert.setAmount(gemToTransfert.getAmount() - transfertQty);
 		}
 
 		gemStack.setAmount(gemStack.getAmount() + transfertQty);
 
 		return removedAll;
 	}
 
 
 
 	/**
 	 * Find the first empty slot between the start and end index as start <= x < end
 	 * @param inv inventory
 	 * @param start index to start iterate
 	 * @param end index to end the iteration
 	 * @return the fist empty slot of the inventory
 	 */
 	private int firstEmptySlot(Inventory inv, int start, int end) {
 		ItemStack[] items = inv.getContents();
 		int slot = -1;
 		int i = start;
 
 		while(slot == -1 && i < end) {
 			if (items[i] == null) {
 				slot = i;
 			}
 
 			i++;
 		}
 
 		return slot;
 	}
 
 
 	/**
 	 * Stack gemToStack gems into all possible stack between the start and end 
 	 * index (start <= x < end); if no stack is available the stack is placed in
 	 * first empty space found.
 	 * 
 	 * @param gemToStack
 	 * @param event {@link InventoryClickEvent} that triggered this action
 	 * @param inv inventory
 	 * @param start start index 
 	 * @param end end index
 	 */
 	private void stackGems(XpContainer gemToStack, InventoryClickEvent event, Inventory inv, int start, int end) {
 		boolean finish = false;
 		XpContainer similarGem;
 		int emptySlot = firstEmptySlot(inv, start, end);
 
 		while (!finish) {
 			similarGem = findSimilarStack(gemToStack, inv, start, end);
 
 			if (similarGem != null) {
 				finish = transfertGems(gemToStack, similarGem, inv, event, false);
 
 			} else {
 				if (emptySlot >= 0) {
 					inv.setItem(emptySlot, gemToStack);
 					event.setCurrentItem(null); // remove the transfered gem
 				}
 
 				finish = true;
 			}
 		}
 	}
 
 } //class
