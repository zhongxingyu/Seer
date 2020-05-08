 package net.worldoftomorrow.nala.ni.listeners;
 
 import net.worldoftomorrow.nala.ni.EventTypes;
 import net.worldoftomorrow.nala.ni.Log;
 import net.worldoftomorrow.nala.ni.Perms;
 import net.worldoftomorrow.nala.ni.StringHelper;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryType.SlotType;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class InventoryListener implements Listener {
 	@EventHandler
 	public void onInventoryClick(InventoryClickEvent event) {
 		Player p = Bukkit.getPlayer(event.getWhoClicked().getName());
 		Inventory inv = event.getInventory();
 		
 		Log.debug("InventoryClickEvent\nPlayer: " + p.getName() + "\nSlot: "
 				+ event.getSlot() + "\nRawSlot: " + event.getRawSlot() + "\nInvType: "
 				+ inv.getType().name() + "\nSlotType: "
 				+ event.getSlotType().name());
 		
 		switch (inv.getType()) {
 		case CRAFTING:
 			this.handleCrafting(event, p);
 			// handle nocraft, nowear, nohold
 			break;
 		case BREWING:
 			this.handleBrewing(event, p);
 			// handle brewing, nohold
 			break;
 		case WORKBENCH:
 			this.handleWorkbench(event, p);
 			// handle nocraft, nohold
 			break;
 		case FURNACE:
 			this.handleFurnace(event, p);
 			break;
 		case ENCHANTING:
 			this.handleEnchanting(event, p);
 			break;
 		case CHEST:
 			this.handleChest(event, p);
 			break;
 		case DISPENSER:
 			this.handleDispenser(event, p);
			break;
 		default:
 			this.handleGenericInv(event, p);
 			break;
 		}
 	}
 
 	private void handleCrafting(InventoryClickEvent event, Player p) {
 		int rs = event.getRawSlot();
 		Inventory inv = event.getInventory();
 		ItemStack stack = null;
 		if (rs >= 0) {
 			stack = inv.getItem(rs);
 		}
 		SlotType st = event.getSlotType();
 		// NoCraft
 		if (st == SlotType.RESULT && stack != null
 				&& Perms.NOCRAFT.has(p, stack)) {
 			event.setCancelled(true);
 			StringHelper.notifyPlayer(p, EventTypes.CRAFT, stack.getTypeId());
 			StringHelper.notifyAdmin(p, EventTypes.CRAFT, stack);
 			return;
 		}
 		// NoWear
 		if (event.getSlotType() == SlotType.ARMOR) {
 			ItemStack oncur = p.getItemOnCursor();
 			if (oncur != null) {
				int id = oncur.getTypeId();
 				if (Perms.NOWEAR.has(p, oncur)) {
 					event.setCancelled(true);
 					StringHelper.notifyPlayer(p, EventTypes.WEAR, id);
 					StringHelper.notifyAdmin(p, EventTypes.WEAR, oncur);
 					return;
 				}
 			}
 		}
 		//NoHold
 		this.handleNoHold(event, p);
 	}
 
 	private void handleBrewing(InventoryClickEvent event, Player p) {
 		int rs = event.getRawSlot();
 		ItemStack oncur = p.getItemOnCursor();
 		Inventory inv = event.getInventory();
 
 		// NoBrew
 		if (rs == 3 && oncur != null) {
 			if (!this.checkCanBrew(inv, oncur, p)) {
 				event.setCancelled(true);
 				return;
 			}
 		} else if (rs < 3 && rs >= 0) {
 			ItemStack ing = inv.getItem(3);
 			if (ing != null && oncur != null) {
				int potiondv = oncur.getDurability();
 				if (Perms.NOBREW.has(p, potiondv, ing.getTypeId())) {
 					event.setCancelled(true);
 					String recipe = potiondv + ":" + ing.getTypeId();
 					StringHelper.notifyPlayer(p, recipe);
 					StringHelper.notifyAdmin(p, recipe);
 					return;
 				}
 			}
 		}
 
 		// NoHold
 		this.handleNoHold(event, p);
 	}
 
 	private void handleWorkbench(InventoryClickEvent event, Player p) {
 		SlotType st = event.getSlotType();
 		Inventory inv = event.getInventory();
 
 		if (st == SlotType.RESULT) {
 			if (inv.getItem(0) != null) {
 				ItemStack stack = inv.getItem(0);
 				if (Perms.NOCRAFT.has(p, stack)) {
 					event.setCancelled(true);
 					StringHelper.notifyPlayer(p, EventTypes.CRAFT,
 							stack.getTypeId());
 					StringHelper.notifyAdmin(p, EventTypes.CRAFT, stack);
 				}
 			}
 		}
 
 		// NoHold
 		this.handleNoHold(event, p);
 	}
 
 	private void handleDispenser(InventoryClickEvent event, Player p) {
 		// NoHold
 		this.handleNoHold(event, p);
 	}
 
 	private void handleChest(InventoryClickEvent event, Player p) {
 		// NoHold
 		this.handleNoHold(event, p);
 	}
 
 	private void handleEnchanting(InventoryClickEvent event, Player p) {
 		// NoHold
 		this.handleNoHold(event, p);
 	}
 
 	private void handleFurnace(InventoryClickEvent event, Player p) {
 		int rs = event.getRawSlot();
 		ItemStack oncur = p.getItemOnCursor();
 		if(rs == 0 && oncur != null){
 			if(Perms.NOCOOK.has(p, oncur)){
 				event.setCancelled(true);
 				StringHelper.notifyPlayer(p, EventTypes.COOK, oncur.getTypeId());
 				StringHelper.notifyAdmin(p, EventTypes.COOK, oncur);
 				return;
 			}
 		}
 		// NoHold
 		this.handleNoHold(event, p);
 	}
 
 	private void handleGenericInv(InventoryClickEvent event, Player p) {
 		// Crafting table III: 149
 		this.handleNoHold(event, p);
 	}
 
 	private boolean checkCanBrew(Inventory inventory, ItemStack oncur, Player p) {
 
 		int count = 0;
 		for (ItemStack stack : inventory.getContents()) {
 			if (count >= 3)
 				break; // If we have checked all potion slots
 			if (stack != null) {
 				int dv = stack.getDurability();
 				if (Perms.NOBREW.has(p, dv, oncur.getTypeId())) {
 					String recipe = dv + ":" + oncur.getTypeId();
 					StringHelper.notifyPlayer(p, recipe);
 					StringHelper.notifyAdmin(p, recipe);
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 	
 	private void handleNoHold(InventoryClickEvent event, Player p){
 		ItemStack oncur = p.getItemOnCursor();
 		// NoHold
 		if (event.getSlotType() == SlotType.QUICKBAR) {
 			int qbsel = p.getInventory().getHeldItemSlot();
 			if (oncur != null && event.getSlot() == qbsel) {
 				if (Perms.NOHOLD.has(p, oncur)) {
 					event.setCancelled(true);
 					StringHelper.notifyPlayer(p, EventTypes.HOLD,
 							oncur.getTypeId());
 					StringHelper.notifyAdmin(p, EventTypes.HOLD, oncur);
 					return;
 				}
 			}
 		}
 	}
 }
