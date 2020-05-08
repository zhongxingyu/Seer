 package com.archmageinc.RealStore;
 
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockDamageEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryOpenEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.inventory.InventoryType.SlotType;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 
 public class StoreListener implements Listener {
 
 	private RealStore plugin;
 	
 	public StoreListener(RealStore instance){
 		plugin	=	instance;
 	}
 	
 	@EventHandler
 	public void onInventoryClick(InventoryClickEvent event){
 		//Don't handle non-player inventory interactions
 		if(!(event.getWhoClicked() instanceof Player)) 
 			return;
 		
 		//Don't handle non-chest interactions
 		if(!event.getInventory().getType().equals(InventoryType.CHEST)) 
 			return;
 		
 		Player player	=	(Player) event.getWhoClicked();
 		Chest chest		=	(Chest) event.getInventory().getHolder();		
 		
 		//Don't handle non-store interactions
 		if(!plugin.isStore(chest)) 
 			return;
 		
 		//Don't handle store owner interactions
 		if(plugin.getStoreOwner(chest).equals(player)) 
 			return;
 		
 		//Don't handle modifications to player's inventory (Except shift click as that would put stuff in the store)
 		if(event.getRawSlot()>=event.getInventory().getSize() && !event.isShiftClick()) 
 			return;
 		
 		//Cancel store interactions with nothing under the cursor
 		if(event.getCurrentItem()==null || event.getCurrentItem().getType().equals(Material.AIR)){
 			event.setCancelled(true);
 			return;
 		}
 		
 		//Cancel store interactions with the outside (i.e. dropping items)
 		if(event.getSlotType().equals(SlotType.OUTSIDE)){
 			event.setCancelled(true);
 			return;
 		}
 		
 		//Cancel player inventory shift click events
 		if(event.getRawSlot()>=event.getInventory().getSize() && event.isShiftClick()){
 			event.setCancelled(true);
 			return;
 		}
 		
 		/**
 		 * By this point the player has clicked on an item in the store
 		 */
 		
 		MaterialData data	=	event.getCurrentItem().getData();
 		Integer price		=	plugin.getPrice(chest, data);
 		
 		//If they didn't click with currency on the cursor, tell them the price
 		if(!Currency.isCurrency(event.getCursor())){
 			plugin.sendPlayerMessage(player, ChatColor.DARK_GREEN+"Cost: "+ChatColor.WHITE+Currency.getValueString(price,false));
 			event.setCancelled(true);
 			return;
 		}
 		
 		/**
 		 * The code below is correct, but there is an error somewhere in bukkit see 
 		 * BUKKIT-1043
 		 * Until this is fixed, we can't count the currency on the cursor.
 		 */
 		/*
 		player.getInventory().addItem(event.getCursor());
 		event.setCursor(new ItemStack(Material.AIR));
 		*/
 		
 		HashMap<Integer,ItemStack> currency	=	Currency.getCurrency(player.getInventory());
 		
 		//If there is null change for the transaction, the player doesn't have enough money
 		if(Currency.getChange(price, currency, false)==null){
 			plugin.sendPlayerMessage(player, ChatColor.DARK_RED+"NSF: "+ChatColor.WHITE+"You do not have enough money!");
 			event.setCancelled(true);
 			return;
 		}
 		
 		/**
 		 * By this point they have committed to purchasing the item!
 		 */
 		
 		HashMap<Integer,ItemStack> change	=	Currency.getChange(price, currency, Currency.hasDiamond(currency));
 		
 		//Iterate through all of the player's money and remove it
 		Iterator<ItemStack> citr			=	currency.values().iterator();
 		while(citr.hasNext()){
 			HashMap<Integer,ItemStack> remainder	=	player.getInventory().removeItem(citr.next());
 			if(remainder.size()>0){
 				/**
 				 * Something went bad during the transaction and not all of the money they
 				 * had was available!
 				 * TODO: Do something about it
 				 */
 			}
 		}
 		
 		//Iterate through all of their change and put it in their inventory
 		Iterator<ItemStack> chitr			=	change.values().iterator();
 		while(chitr.hasNext()){
 			HashMap<Integer,ItemStack> uChange	=	player.getInventory().addItem(chitr.next());
 			if(uChange.size()>0){
 				/**
 				 * There was not enough room in their inventory to fit all of the change. Spill it on the ground
 				 */
 				plugin.sendPlayerMessage(player, ChatColor.BLUE+"Warning: "+ChatColor.WHITE+"There wasn't enough room in your inventory for your change. You dropped it on the ground.");
 				Iterator<ItemStack> ucitr	=	uChange.values().iterator();
 				while(ucitr.hasNext()){
 					player.getWorld().dropItemNaturally(player.getLocation(), ucitr.next());
 				}
 			}
 		}
 		
 		plugin.sendPlayerMessage(player, ChatColor.GREEN+"You purchased "+ChatColor.WHITE+event.getCurrentItem().getType().toString()+ChatColor.GREEN+" for "+ChatColor.WHITE+price+ChatColor.GREEN+" gold nuggets.");
 		ItemStack sold	=	event.getCurrentItem().clone();
 		sold.setAmount(1);
 		
 		/**
 		 * Remove the purchased item from the store
 		 */
 		//If the current amount is 1 the entire stack must be removed 
 		if(event.getCurrentItem().getAmount()==1)
 			event.getInventory().setItem(event.getSlot(),new ItemStack(Material.AIR));
 		//Otherwise just reduce the amount by 1
 		else
 			event.getCurrentItem().setAmount(event.getCurrentItem().getAmount()-1);
 		
 		/**
 		 * Place the purchased item in the player's inventory
 		 */
 		HashMap<Integer,ItemStack> remainder	=	player.getInventory().addItem(sold);
 		if(remainder.size()>0){
 			//Iterate through any returned items (Currently this should only ever be 1 item)
 			Iterator<ItemStack> itr	=	remainder.values().iterator();
 			while(itr.hasNext()){
 				/**
 				 * There wasn't enough room in their inventory for the item, spill it
 				 */
 				plugin.sendPlayerMessage(player, ChatColor.BLUE+"Warning: "+ChatColor.WHITE+"There wasn't enough room in your inventory for your item. You dropped it on the ground.");
 				player.getWorld().dropItemNaturally(player.getLocation(), itr.next());
 			}
 		}
 		
 		/**
 		 * Deposit the money in the store owner's coffers
 		 */
 		OfflinePlayer owner	=	plugin.getStoreOwner(chest);
 		plugin.deposit(owner, price);
 		event.setCancelled(true);
 		
 	}
 	
 	@EventHandler
 	public void onBlockDamage(BlockDamageEvent event){
 		if(!(event.getBlock().getState() instanceof Chest))
 			return;
 		
 		Chest chest	=	(Chest) event.getBlock().getState();
 		
 		if(!plugin.isStore(chest) && !plugin.isCoffer(chest))
 			return;
 		
 		/**
 		 * We don't want to spam the player's messages if they are setting prices or adding/removing stores/coffers
 		 */
 		if(!plugin.isSetting(event.getPlayer())){
 			if(plugin.isStore(chest))
 				plugin.sendPlayerMessage(event.getPlayer(), ChatColor.DARK_RED+"That is a store! "+ChatColor.WHITE+"You are not allowed to break it!");
 			
 			if(plugin.isCoffer(chest))
 				plugin.sendPlayerMessage(event.getPlayer(),ChatColor.DARK_RED+"That is a coffer! "+ChatColor.WHITE+"You are not allowed to break it!");
 		}else{
 			plugin.clearSetting(event.getPlayer());
 		}
 		
 		event.setCancelled(true);
 	}
 	
 	@EventHandler
 	public void onInventoryOpen(InventoryOpenEvent event){
 		if(!event.getInventory().getType().equals(InventoryType.CHEST))
 			return;
 		if(event.getInventory().getHolder()==null)
 			return;
 		if(!(event.getInventory().getHolder() instanceof Chest))
 			return;
 		
 		if(!(event.getPlayer() instanceof Player))
 			return;
 		
 		Chest chest		=	(Chest) event.getInventory().getHolder();
 		Player player	=	(Player) event.getPlayer();
 		
 		if(!plugin.isCoffer(chest))
 			return;
 		
 		if(plugin.getCofferOwner(chest).equals(player))
 			return;
 		
 		plugin.sendPlayerMessage(player, ChatColor.DARK_RED+"Thief! "+ChatColor.WHITE+"That is not your coffer!");
 		event.setCancelled(true);
 	}
 	
 	@EventHandler
 	public void onEntityExplode(EntityExplodeEvent event){
 		Iterator<Block> itr	=	event.blockList().iterator();
 		while(itr.hasNext()){
 			Block block	=	itr.next();
 			if(!(block.getState() instanceof Chest))
 				continue;
 			
 			Chest chest	=	(Chest) block.getState();
 			if(!plugin.isCoffer(chest) && !plugin.isStore(chest))
 				continue;
 			
 			if(plugin.isStore(chest)){
 				OfflinePlayer oOwner	=	plugin.getStoreOwner(chest);
 				if(!plugin.removeStore(chest))
 					event.setCancelled(true);
 				else{
 					
 					if(oOwner.isOnline()){
 						Player owner = oOwner.getPlayer();
 						plugin.sendPlayerMessage(owner, ChatColor.DARK_RED+"Alert! "+ChatColor.WHITE+" One of your stores has been robbed!");
 					}
 				}
 			}
 			
 			if(plugin.isCoffer(chest)){
 				OfflinePlayer oOwner	=	plugin.getCofferOwner(chest);
 				if(!plugin.removeCoffer(chest))
 					event.setCancelled(true);
 				else{
 					if(oOwner.isOnline()){
 						Player owner = oOwner.getPlayer();
 						plugin.sendPlayerMessage(owner, ChatColor.DARK_RED+"Alert! "+ChatColor.WHITE+" One of your coffers has been robbed!");
 					}
 				}
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event){
 		if(!(event.getBlock().getState() instanceof Chest))
 			return;
 		Chest chest		=	(Chest) event.getBlock().getState();
 		
 		if(!plugin.isCoffer(chest) && !plugin.isStore(chest))
 			return;
 		
 		if(plugin.isStore(chest)){
 			OfflinePlayer oOwner	=	plugin.getStoreOwner(chest);
 			if(!plugin.removeStore(chest))
 				event.setCancelled(true);
 			else{
 				
 				if(oOwner.isOnline()){
 					Player owner = oOwner.getPlayer();
 					plugin.sendPlayerMessage(owner, ChatColor.DARK_RED+"Alert! "+ChatColor.WHITE+" One of your stores has been robbed!");
 				}
 			}
 		}
 		if(plugin.isCoffer(chest)){
 			OfflinePlayer oOwner	=	plugin.getCofferOwner(chest);
 			if(!plugin.removeCoffer(chest))
 				event.setCancelled(true);
 			else{
 				if(oOwner.isOnline()){
 					Player owner = oOwner.getPlayer();
 					plugin.sendPlayerMessage(owner, ChatColor.DARK_RED+"Alert! "+ChatColor.WHITE+" One of your coffers has been robbed!");
 				}
 			}
 		}
 	}
 }
