 package com.comphenix.xp.mods;
 
 import org.bukkit.Material;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.inventory.InventoryType.SlotType;
 import org.bukkit.inventory.ItemStack;
 
 import com.comphenix.xp.ActionTypes;
 import com.comphenix.xp.extra.Permissions;
 import com.comphenix.xp.lookup.ItemQuery;
 
 public class StandardBlockService implements BlockService {
 	/**
 	 * Name of this service.
 	 */
 	public static String NAME = "STANDARD";
 	
 	@Override
 	public BlockResponse processClickEvent(InventoryClickEvent event, ItemQuery block) {
 
 		// Crafting, smelting and potion check
 		boolean isCraftResult = event.getSlotType() == SlotType.RESULT;
		boolean isPotionResult = event.getRawSlot() < 3;
		
 		InventoryType type = event.getInventory().getType();
 		ItemStack toCraft = event.getCurrentItem();
 		
 		// Empty slots are invalid
 		if (!ItemQuery.hasItems(toCraft))
 			return BlockResponse.FAILURE;
 		
 		// Handle different types
 		switch (type) {
 			case BREWING:
 				// Make sure this is a potion result slot
 				if (isPotionResult && match(block, Material.BREWING_STAND)) {
 					return new BlockResponse(type, ActionTypes.BREWING, Permissions.REWARDS_BREWING);
 				}
 				break;
 				
 			case CRAFTING:		
 				// Player crafting - meaning that the block query is irrelevant
 				if (isCraftResult) {
 					return new BlockResponse(type, ActionTypes.CRAFTING, Permissions.REWARDS_CRAFTING);
 				}
 				break;
 				
 			case WORKBENCH:
 				if (isCraftResult && match(block, Material.WORKBENCH)) {
 					return new BlockResponse(type, ActionTypes.CRAFTING, Permissions.REWARDS_CRAFTING);
 				}
 				break;
 				
 			case FURNACE:
 				if (isCraftResult && match(block, Material.FURNACE, Material.BURNING_FURNACE)) {
 					return new BlockResponse(type, ActionTypes.SMELTING, Permissions.REWARDS_SMELTING);
 				}
 				break;
 				
 			default:
 				// Invalid
 				break;
 		}
 		
 		// Unable to process block
 		return BlockResponse.FAILURE;
 	}
 	
 	// Check the given block
 	private boolean match(ItemQuery block, Material... materials) {
 		// If we don't have a block, we've failed to record the last interaction - but we'll still
 		// accept standard actions.
 		if (block == null)
 			return true;
 		
 		// Find a match
 		for (Material mat : materials) {
 			if (block.match(mat))
 				return true;
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public String getServiceName() {
 		return NAME;
 	}
 }
