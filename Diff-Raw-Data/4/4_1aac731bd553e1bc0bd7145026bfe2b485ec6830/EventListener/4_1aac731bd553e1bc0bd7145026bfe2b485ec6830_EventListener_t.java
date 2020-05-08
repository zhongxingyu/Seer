 package net.catharos.lib.events;
 
 import net.catharos.lib.menu.Menu;
 import net.catharos.lib.menu.MenuCloseBehaviour;
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
 import org.bukkit.inventory.Inventory;
 
 
 public class EventListener implements Listener {
 
 	@EventHandler
 	public void entityDeath(EntityDeathEvent event) {
 		Bukkit.getPluginManager().callEvent(EntityKilledEvent.createEvent(event));
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onMenuItemClicked(InventoryClickEvent event) {
 		Inventory inventory = event.getInventory();
 
 		if (inventory.getHolder() instanceof Menu) {
 			Menu menu = (Menu) inventory.getHolder();
 
 			if (event.getWhoClicked() instanceof Player) {
 				Player player = (Player) event.getWhoClicked();
 
 				int index = event.getRawSlot();
 
				if (event.getSlotType() != SlotType.OUTSIDE && index < inventory.getSize()) {
 					menu.selectMenuItem(player, index);
 				}
 			}
 
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onMenuClosed(InventoryCloseEvent event) {
 		if (event.getPlayer() instanceof Player) {
 			Inventory inventory = event.getInventory();
 
 			if (inventory.getHolder() instanceof Menu) {
 				Menu menu = (Menu) inventory.getHolder();
 
 				MenuCloseBehaviour menuCloseBehaviour = menu.getMenuCloseBehaviour();
 
 				if (menuCloseBehaviour != null) {
 					menuCloseBehaviour.onClose((Player) event.getPlayer());
 				}
 			}
 		}
 	}
 }
