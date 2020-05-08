 package com.sparkedia.valrix.AutoReplace;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 public class AutoBlockListener extends BlockListener {
 	public AutoReplace plugin;
 	
 	public AutoBlockListener(AutoReplace plugin) {
 		this.plugin = plugin;
 	}
 
 	public void onBlockBreak(BlockBreakEvent e) {
 		if (!e.isCancelled()) {
 			Player player = e.getPlayer();
 			PlayerInventory inv = player.getInventory();
 			Material tool = Material.getMaterial(inv.getItemInHand().getTypeId());
 			// If the tool has broken, make sure it's not a block
 			if (!tool.isBlock() && inv.getItemInHand().getDurability() == tool.getMaxDurability()) {
 				int slot = inv.getHeldItemSlot();
 				int ID = inv.getItemInHand().getTypeId();
 				//replace it, otherwise they're out, message them
 				ItemStack[] items = inv.getContents();
 				for (int i = 0; i < items.length; i++) {
 					if (i != slot) {
 						ItemStack obj = items[i];
 						//for each item in inventory, check if it's what we want, then we can break
 						if ((obj != null) && (obj.getTypeId() == ID) && (obj.getAmount() > 0)) {
 							//we found what we need, now replace in-hand tool durability with found and clear found tool
 							inv.getItemInHand().setDurability((short) (obj.getDurability()-1));
 							inv.clear(i);
 							break;
 						}
 					}
 				}
 			}
 		}
 	}
 
 	@SuppressWarnings("deprecation") //This is because player.updateInventory() is depreciated
 	public void onBlockPlace(BlockPlaceEvent e) {
 		if (!e.isCancelled()) {
 			Player player = e.getPlayer(); // Set player variable
 			Block block = e.getBlockPlaced();
 			PlayerInventory inv = player.getInventory();
 			int count = (player.getItemInHand().getAmount()-1);
 			int slot = inv.getHeldItemSlot();
 			if (count < 1) {
 				//replace it, otherwise they're out, message them
 				ItemStack[] items = inv.getContents();
 				for (int i = 0; i < items.length; i++) {
 					if (i != slot) {
 						ItemStack obj = items[i];
 						//for each item in inventory, check if it's what we want, then we can break
 						if ((obj != null) && (obj.getTypeId() == block.getTypeId()) && (obj.getAmount() > 0)) {
 							//we found what we need, now change in-hand stack count to found and clear found item
 							inv.getItemInHand().setAmount(obj.getAmount()+1);
 							if (obj.getTypeId() == 35) {
								inv.getItemInHand().setDurability((short) obj.getDurability());
 							}
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
