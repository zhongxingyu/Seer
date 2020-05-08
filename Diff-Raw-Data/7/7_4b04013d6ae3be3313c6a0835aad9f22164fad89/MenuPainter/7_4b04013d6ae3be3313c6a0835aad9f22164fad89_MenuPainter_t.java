 package com.m0pt0pmatt.GUIM;
 
 import java.util.ArrayList;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 public class MenuPainter {
 	
 	/**
 	 * Returns a place in the inventory, right-aligned to the offset
 	 * 
 	 * @param inv
 	 *            the Inventory
 	 * @param offset
 	 *            the offset after the alignment
 	 * @return a place in the inventory, right-aligned to the offset
 	 */
 	public static int getRight(Inventory inv, int offset) {
 		return inv.getSize() - 1 - offset;
 	}
 
 	/**
 	 * Returns a place in the inventory, left-aligned to the offset
 	 * 
 	 * @param inv
 	 *            the Inventory
 	 * @param offset
 	 *            the offset after the alignment
 	 * @return a place in the inventory, left-aligned
 	 */
 	public static int getLeft(Inventory inv, int offset) {
 		return inv.getSize() - 9 + offset;
 	}
 	
 	private static void paintMainMenu(Player player){
 		Inventory inv = GUIM.getPlayerInfo(player.getName()).inventory;
 		
 		inv.clear();
 		inv.setItem(getLeft(inv, 0), nameItem(getButton(1), "Market Items"));
 		inv.setItem(getLeft(inv, 1), nameItem(getButton(2), "Requested Items"));
 		inv.setItem(getLeft(inv, 2), nameItem(getButton(3), "Free Items"));
 		inv.setItem(getLeft(inv, 3), nameItem(getButton(4), "Options"));
 		inv.setItem(getLeft(inv, 4), nameItem(getButton(5), "Admin Menu"));
 		inv.setItem(getLeft(inv, 5), nameItem(getButton(11), "Help"));
 	}
 	
 	private static void paintViewItemMenu(Player player) {
 
 		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getName());
 		Inventory inv = playerInfo.inventory;
 		
 		// clear inventory
 		inv.clear();	
 
 		// add menu buttons
 		// add next buttons
 		inv.setItem(getLeft(inv, 0), nameItem(getButton(1), "Scroll Left"));
 		inv.setItem(getRight(inv, 0), nameItem(getButton(1), "Scroll Right"));
 		inv.setItem(getRight(inv, 2), nameItem(getButton(6), "Back"));
 		inv.setItem(getLeft(inv, 3), nameItem(getButton(11), "Help"));
 
 		// get which list of items ans setup custom buttons
 		Market market = GUIM.marketNames.get(playerInfo.currentMarket);
 		ArrayList<MarketSale> items;
 		switch(playerInfo.menu.split(":")[0]){
 		case "market":
 			items = market.marketItems;
 			inv.setItem(getRight(inv, 1), nameItem(getButton(14), "Sell Items"));
 			break;
 		case "request":
 			items = market.requestedItems;
 			break;	
 		case "free":
 			items = market.freeItems;
 			inv.setItem(getRight(inv, 1), nameItem(getButton(14), "Add Items"));
 			break;
 		default:
 			items = market.marketItems;
 			break;
 		}
 		
 		//check if the index is in bounds
 		if (playerInfo.index * 45 >= items.size()){
 			return;
 		}
 		
 		//display items
 		for (int j = 0; j < java.lang.Math.min((inv.getSize() - 9),items.size() - (playerInfo.index*45)); j++) {
 			ItemStack item = items.get((playerInfo.index * 45) + j).getItems().getFirst();
 			inv.setItem(j, item);
 		}
 
 	}
 	
 	private static void paintBuyMenu(Player player) {
 		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getName());
 		Inventory inv = playerInfo.inventory;
 
 		// clear inventory
 		inv.clear();
 		
 		// add menu buttons
 		// add buy button
 		inv.setItem(getRight(inv, 1), nameItem(getButton(5), "Purchase Item(s)"));
 
 		// add back button
 		inv.setItem(getRight(inv, 0), nameItem(getButton(2), "Go Back"));
 		inv.setItem(getLeft(inv, 3), nameItem(getButton(11), "Help"));
 		//add price button
 		inv.setItem(getRight(inv, 36), nameItem(getButton(8), "Sale is $" + playerInfo.temp.getUnitPrice() + " per unit"));
 		inv.setItem(getRight(inv, 37), nameItem(getButton(8), playerInfo.temp.getAvailiableUnits() + " bulks left"));
 		inv.setItem(getRight(inv, 38), nameItem(getButton(8), "Seller: " + playerInfo.temp.getSeller()));
 		
 		// add items
 		int i = 0;
 		for (ItemStack item: playerInfo.temp.getItems()){
 			inv.setItem(i, item);
 			inv.getItem(i).setAmount(playerInfo.temp.getNumPerUnits());
 			i++;
 		}
 		
 		//add increment and decrement buttons
 		for (i = 0; i < 9; i++){
 			inv.setItem(getRight(inv, 27 + i), nameItem(getButton(3), ""));
 			inv.setItem(getRight(inv, 18 + i), nameItem(getButton(4), ""));
 		}
 
 		// add price
 		int price = playerInfo.unitQuantity;
 		for (i = 0; i < 9; i++) {
 			int j = 0;
 			while ((price % (java.lang.Math.pow(10, i + 1))) != 0) {
 				j++;
 				price -= java.lang.Math.pow(10, i);
 			}
 			// j = 0. make a zero
 			if (j == 0) {
 
 			}
 			// j is not zero. make sticks
 			else {
 				inv.setItem(getRight(inv, 9 + i), (new ItemStack(Material.STICK, j)));
 			}
 		}
 
 		
 	}
 	
 	private static void paintConfirmMenu(Player player) {
 		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getName());
 		Inventory inv = playerInfo.inventory;
 
 		// clear inventory
 		inv.clear();
 		
 		// add menu buttons
 		// add buy button
 		inv.setItem(getRight(inv, 1), nameItem(getButton(5), "Ask for these items"));
 
 		// add back button
 		inv.setItem(getRight(inv, 0), nameItem(getButton(2), "Go Back"));
 		
 		//add price button
 		inv.setItem(getRight(inv, 36), nameItem(getButton(8), "Sale is $" + playerInfo.temp.getUnitPrice() + " per unit"));
 		inv.setItem(getRight(inv, 37), nameItem(getButton(8), playerInfo.temp.getUnitQuantity() + " bulks total"));
 		inv.setItem(getRight(inv, 38), nameItem(getButton(8), "Seller: " + playerInfo.temp.getSeller()));
 		
 		// add items
 		int i = 0;
 		for (ItemStack item: playerInfo.temp.getItems()){
 			inv.setItem(i, item);
 			inv.getItem(i).setAmount(playerInfo.temp.getNumPerUnits());
 			i++;
 		}
 		
 	}
 	
 	private static void paintSellMenu(Player player) {
 		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getName());
 		Inventory inv = playerInfo.inventory;
 
 		// clear inventory
 		inv.clear();
 
 		// add menu buttons
 		// add buy button
 		inv.setItem(getRight(inv, 1), nameItem(getButton(5), "Go Forward"));
 		inv.setItem(getLeft(inv, 3), nameItem(getButton(11), "Help"));
 
 		// add back button
 		inv.setItem(getRight(inv, 0), nameItem(getButton(2), "Go Back"));
 
 		//add increment and decrement buttons
 		for (int i = 0; i < 9; i++){
 			inv.setItem(getRight(inv, 27 + i), nameItem(getButton(3), ""));
 			inv.setItem(getRight(inv, 18 + i), nameItem(getButton(4), ""));
 		}
 		
 		// add the item if it exists
 		if (playerInfo.temp == null){
 			playerInfo.temp = new MarketSale(player.getName());
 		}
 		int i = 0;
 		for (ItemStack item: playerInfo.temp.getItems()) {
 			inv.setItem(i, item);
 			i++;
 		}
 		
 		//finish if there is not a price (item is free)
 		if (playerInfo.menu.split(":")[1].equals("free")){
 			return;
 		}
 		
 		
 		// add price
 		int amount = 0;
 		String stage = playerInfo.menu.split(":")[2];
 		switch(stage){
 		case "quantity":
 			amount = playerInfo.temp.getUnitQuantity();
 			break;
 		case "bulk":
 			amount = playerInfo.temp.getNumPerUnits();
 			break;
 		case "price":
 			amount = playerInfo.temp.getUnitPrice();
 			break;
 		}
 		
 		for (i = 0; i < 9; i++) {
 			int j = 0;
 			while ((amount % (java.lang.Math.pow(10, i + 1))) != 0) {
 				j++;
 				amount -= java.lang.Math.pow(10, i);
 			}
 			// j = 0. make a zero
 			if (j == 0) {
 
 			}
 			// j is not zero. make sticks
 			else {
 				inv.setItem(getRight(inv, 9 + i), new ItemStack(Material.STICK, j));
 			}
 		}
 	}
 	
 	
 	
 	private static void paintOptionsMenu(Player player){
 		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getName());
 		Inventory inv = playerInfo.inventory;
 
 		// clear inventory
 		inv.clear();
 
 		// add menu buttons
 
 		// add back button
 		inv.setItem(getRight(inv, 0), nameItem(getButton(2), "Go Back"));
 	}
 	
 	/**
 	 * paints the admin menu.
 	 * @param player
 	 */
 	private static void paintAdminMenu(Player player){
 		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getName());
 		Inventory inv = playerInfo.inventory;
 
 		// clear inventory
 		inv.clear();
 
 		// add menu buttons
 
 		// add back button
 		inv.setItem(getRight(inv, 0), nameItem(getButton(2), "Go Back"));
 	}
 
 	/**
 	 * Paints the chest correctly, depending on the players current menu.
 	 * Eventually, this will act as the sole interface for painting menus.
 	 * @param player
 	 */
 	public static void paintMenu(Player player) {
		if (player == null){
			return;
		}
 		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getName());
		if (playerInfo == null){
			return;
		}
 		String menu = playerInfo.menu;
 		if (menu == null){
 			return;
 		}
 		
 		Market market = GUIM.marketNames.get(playerInfo.currentMarket);
 		if (market == null){
 			return;
 		}
 		
 		String[] parts = menu.split(":");
 		
 		switch (parts[0]){
 		case "main":
 			paintMainMenu(player);
 			break;
 		case "market":
 			switch (parts[1]){
 			case "view":
 				paintViewItemMenu(player);
 				break;
 			case "buy":
 				paintBuyMenu(player);
 				break;
 			case "sell":
 				paintSellMenu(player);
 				break;
 			}
 			break;
 		case "request":
 			switch (parts[1]){
 			case "view":
 				paintViewItemMenu(player);
 				break;
 			case "buy":
 				paintBuyMenu(player);
 				break;
 			case "confirm":
 				paintConfirmMenu(player);
 				break;
 			}
 			break;
 		case "free":
 			switch (parts[1]){
 			case "view":
 				paintViewItemMenu(player);
 				break;
 			case "buy":
 				paintTakeMenu(player);
 				break;
 			case "sell":
 				paintSellMenu(player);
 				break;
 			}
 			break;
 		case "options":
 			paintOptionsMenu(player);
 			break;
 		case "admin":
 			paintAdminMenu(player);
 			break;
 		}
 		
 	}
 	
 	private static void paintTakeMenu(Player player) {
 		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getName());
 		Inventory inv = playerInfo.inventory;
 
 		// clear inventory
 		inv.clear();
 		
 		// add menu buttons
 		// add buy button
 		inv.setItem(getRight(inv, 1), nameItem(getButton(5), "Take these items"));
 
 		// add back button
 		inv.setItem(getRight(inv, 0), nameItem(getButton(2), "Go Back"));
 		
 		//add price button
 		inv.setItem(getRight(inv, 37), nameItem(getButton(8), playerInfo.temp.getAvailiableUnits() + " bulks left"));
 		inv.setItem(getRight(inv, 38), nameItem(getButton(8), "Seller: " + playerInfo.temp.getSeller()));
 		
 		// add items
 		int i = 0;
 		for (ItemStack item: playerInfo.temp.getItems()){
 			inv.setItem(i, item);
 			inv.getItem(i).setAmount(playerInfo.temp.getNumPerUnits());
 			i++;
 		}
 		
 		//add increment and decrement buttons
 		for (i = 0; i < 9; i++){
 			inv.setItem(getRight(inv, 27 + i), nameItem(getButton(3), ""));
 			inv.setItem(getRight(inv, 18 + i), nameItem(getButton(4), ""));
 		}
 
 		// add price
 		int price = playerInfo.unitQuantity;
 		for (i = 0; i < 9; i++) {
 			int j = 0;
 			while ((price % (java.lang.Math.pow(10, i + 1))) != 0) {
 				j++;
 				price -= java.lang.Math.pow(10, i);
 			}
 			// j = 0. make a zero
 			if (j == 0) {
 
 			}
 			// j is not zero. make sticks
 			else {
 				inv.setItem(getRight(inv, 9 + i), (new ItemStack(Material.STICK, j)));
 			}
 		}
 		
 	}
 
 	
 
 	/**
 	 * This method is a placeholder for when buttons can be configurable itemstacks. For now, it just returns a block of wool in the color you specify
 	 * @param whichButton
 	 * @return
 	 */
 	private static ItemStack getButton(int whichButton){
 		return new ItemStack(Material.WOOL, 1, (byte) whichButton);
 	}
 	
 	private static ItemStack nameItem(ItemStack item, String name){
 		ItemMeta meta = item.getItemMeta();
 		meta.setDisplayName(name);
 		item.setItemMeta(meta);
 		return item;
 	}
 }
