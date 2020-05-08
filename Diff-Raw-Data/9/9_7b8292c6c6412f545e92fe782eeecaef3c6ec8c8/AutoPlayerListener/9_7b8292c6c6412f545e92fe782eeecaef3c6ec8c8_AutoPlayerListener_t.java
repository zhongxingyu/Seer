 package com.sparkedia.valrix.AutoReplace;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 public class AutoPlayerListener extends PlayerListener {
 	public AutoReplace plugin;
 	
 	public AutoPlayerListener(AutoReplace plugin) {
 		this.plugin = plugin;
 	}
 	
 	@SuppressWarnings("deprecation")
 	public void onPlayerInteract(PlayerInteractEvent e) {
		if (!e.isCancelled() && e.getAction().toString().equalsIgnoreCase("RIGHT_CLICK_BLOCK") && e.hasBlock() && e.hasItem() && !e.isBlockInHand()) {
 			Player player = e.getPlayer();
 			PlayerInventory inv = player.getInventory();
 			ItemStack item = e.getItem();
 			int count = (player.getItemInHand().getAmount()-1);
 			int slot = inv.getHeldItemSlot();
 			if (count < 1) {
 				ItemStack[] items = inv.getContents();
 				for (int i = 0; i < items.length; i++) {
 					if (i != slot) {
 						ItemStack obj = items[i];
 						if ((obj != null) && (obj.getTypeId() == item.getTypeId()) && (obj.getAmount() > 0)) {
 							inv.getItemInHand().setAmount(obj.getAmount()+1);
 							inv.clear(i);
 							player.updateInventory();
 							break;
 						}
 					}
 				}
 			}
 		}
 	}
 }
