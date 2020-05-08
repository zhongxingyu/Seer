 package iggy.Economy;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class SignShops implements Listener{
 	Economy plugin;
 	SignShops (Economy instance) {
 		plugin = instance;
 	}
 	// this class does two things, allows admins to place shops
 	// and allows players to buy from shops
 	// sell signs and buy signs
 	@EventHandler (priority = EventPriority.NORMAL)
 	public void breakShop(BlockBreakEvent event) {
 		if (event.getBlock().getWorld() == Bukkit.getWorld("shopworld")) {
 			if (!event.getPlayer().isOp() && !event.getPlayer().hasPermission("economy.editshop")) {
 				event.setCancelled(true);
 			}
 		}
 	}
 	@EventHandler (priority = EventPriority.NORMAL)
 	public void placeShop(BlockPlaceEvent event) {
 		if (event.getBlock().getWorld() == Bukkit.getWorld("shopworld")) {
 			if (!event.getPlayer().isOp() && !event.getPlayer().hasPermission("economy.editshop")) {
 				event.setCancelled(true);
 			}
 		}
 	}
 	//
 	/********************************* PLACE SIGN *********************************\
 	| Still debuggin this function
 	\******************************************************************************/
 	@EventHandler (priority = EventPriority.NORMAL)
 	public void placeSign(SignChangeEvent event) {
 		//Sign placedSign = (Sign) event.getBlock().getState();
 		if (event.getLine(0).equalsIgnoreCase("[SHOP]") || event.getLine(0).equalsIgnoreCase("[SHOP-STACK]")){
 			if (event.getPlayer().isOp() || event.getPlayer().hasPermission("economy.makeshop")) {
 				Material foundMaterial = Material.matchMaterial(event.getLine(1));
 				if (foundMaterial == null){
 					event.setLine(2, "FOUND");
 					event.setLine(1, "NOT");
 					event.setLine(0, "");
 				}
 				else{
 					event.setLine(2, foundMaterial.name());
 				}
 			}
 			else {
 				event.setCancelled(true);
 			}
 		}
 		if (event.getLine(0).equalsIgnoreCase("[SELL]")) {
 			if (event.getPlayer().isOp() || event.getPlayer().hasPermission("economy.makeshop")) {
 				event.setLine(1, ""+ChatColor.GOLD);
 				event.setLine(2, "ALL THE THINGS");
 				event.setLine(3, ""+ChatColor.BLACK);
 			}
 			else {
 				event.setCancelled(true);
 			}
 		}
 	}
 	// click sign
 	/********************************* CLICK SIGN *********************************\
 	| Still debuggin this too
 	\******************************************************************************/
 	@EventHandler (priority = EventPriority.NORMAL)
 	public void clickSign(PlayerInteractEvent event){
 		if (event.getAction() != Action.LEFT_CLICK_BLOCK){
 			return;	
 		}
 		Block clickedBlock = event.getClickedBlock();
 		if (clickedBlock == null) {
 			return;
 		}
 		// this might need to be 'Material.SIGN'
 		if (clickedBlock.getType() == Material.SIGN_POST || clickedBlock.getType() == Material.WALL_SIGN) {
 			plugin.info("player click sign");
 			Sign clickedSign = (Sign) clickedBlock.getState();
 			if (clickedSign.getLine(0).equalsIgnoreCase("[SHOP]") || clickedSign.getLine(0).equalsIgnoreCase("[SHOP-STACK]")) {
 				
 				int quantity = 0;
 				if (clickedSign.getLine(0).equalsIgnoreCase("[SHOP-STACK]")) {
 					quantity = 64;
 				}
 				
 				Material purchaceMaterial = Material.matchMaterial(clickedSign.getLine(1));
 				//find price
 				long price;
 				if (plugin.blockPrices.containsKey(purchaceMaterial)){
 					price = plugin.blockPrices.get(purchaceMaterial) * quantity;
 				}
 				else {
 					event.getPlayer().sendMessage("There was an error with this sign, contact the administration");
 					return;
 				}
 				if (price < 0) {
 					event.getPlayer().sendMessage("This block cannot be bought");
 				}
 				else if (plugin.chargeMoney(event.getPlayer(), price)) {
 					ItemStack item = new ItemStack(purchaceMaterial, quantity);
 					event.getPlayer().getInventory().addItem(item);
					event.getPlayer().sendMessage("You just bought "+ quantity + " " +purchaceMaterial.name()+" for "+ChatColor.GREEN+"$"+price+ChatColor.WHITE);
 				}
 				else {
					event.getPlayer().sendMessage("You need "+ChatColor.GREEN+"$"+price+ChatColor.WHITE+" to buy "+quantity+" "+purchaceMaterial.name());
 				}
 				// cancel the event so nothing else happens
 				event.setCancelled(true);
 			}
 			else if (clickedSign.getLine(0).equalsIgnoreCase("[SELL]")){
 				//[create a global hash table for players and when they last clicked (maybe what item as well)]
 				// check to see when the last click was if it was over 500ms and less then 5000ms then it will sell
 				
 				// get the player clicking the sign
 				Player player = event.getPlayer();
 				// Set the default quantity of the item to be questioned
 				int amount = 1;
 				Material material;
 				
 				amount = player.getItemInHand().getAmount();
 				material = player.getItemInHand().getType();
 				
 				long blockPrice = plugin.blockPrices.get(material);
 				if (blockPrice == -1) {
 					player.sendMessage("We cannot buy this item");
 				}
 				else {
 					player.sendMessage("You sold "+amount+" "+material.toString()+" for "+ChatColor.GREEN+"$"+amount*blockPrice/2+ChatColor.WHITE);
 					plugin.giveMoney(player, amount*blockPrice/2);
 					ItemStack item = player.getItemInHand();
 					item.setType(Material.AIR);
 					item.setAmount(0);
 					player.setItemInHand(item);
 					
 				}
 				event.setCancelled(true);
 				
 			}
 		}
 	}
 	
 	
 }
