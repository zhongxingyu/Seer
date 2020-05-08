 package Lihad.Conflict.Listeners;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Villager;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.player.PlayerExpChangeEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPortalEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.inventory.ItemStack;
 
 import Lihad.Conflict.*;
 import Lihad.Conflict.Perk.*;
 import Lihad.Conflict.Util.BeyondUtil;
 
 public class BeyondPlayerListener implements Listener {
 	public BeyondPlayerListener() {	}
     
 	@EventHandler
 	public static void onPlayerMove(PlayerMoveEvent event){
 		if((event.getFrom().getBlockX() != event.getTo().getBlockX()
 				|| event.getFrom().getBlockY() != event.getTo().getBlockY()
 				|| event.getFrom().getBlockZ() != event.getTo().getBlockZ())
 				&& event.getTo().getWorld().getName().equals("survival")) {
 			if(Conflict.isUnassigned(event.getPlayer().getName())){
 				event.setTo(event.getFrom());
 				event.getPlayer().sendMessage(Conflict.NOTICECOLOR+"You need to join a Capital before continuing...");
 				event.getPlayer().sendMessage(Conflict.TEXTCOLOR+"Type "+ChatColor.GRAY+"/join <cityname> confirm");
 				event.getPlayer().sendMessage(Conflict.TEXTCOLOR+"You can choose one of: " + Conflict.CITYCOLOR + java.util.Arrays.asList(Conflict.cities).toString());
 			}
 			for (City city: Conflict.cities)
 			{
 				if (city.getLocation().distance(event.getTo()) < 500.0
 						&& city.getLocation().distance(event.getFrom()) >= 500.0){
 					event.getPlayer().sendMessage(Conflict.NOTICECOLOR + "You have entered the City of " + Conflict.CITYCOLOR + city.getName());
 				}
 				else if(city.getLocation().distance(event.getTo()) > 500.0
 						&& city.getLocation().distance(event.getFrom()) <= 500.0){
 					event.getPlayer().sendMessage(Conflict.NOTICECOLOR + "You have left the City of " + Conflict.CITYCOLOR + city.getName());
 				}
 			}
 			if(event.getPlayer().getWorld().getSpawnLocation().distance(event.getTo()) > 5000.0
 					&& event.getPlayer().getWorld().getSpawnLocation().distance(event.getFrom()) <= 5000.0){
 				event.getPlayer().sendMessage(Conflict.NOTICECOLOR + "You have entered the Wilderness");
 			}
 			else if(event.getPlayer().getWorld().getSpawnLocation().distance(event.getTo()) < 5000.0
 					&& event.getPlayer().getWorld().getSpawnLocation().distance(event.getFrom()) >= 5000.0){
 				event.getPlayer().sendMessage(Conflict.NOTICECOLOR + "You have entered the Cityscape");
 			}
 		}
 	}
 	@EventHandler
 	public static void onPlayerJoin(PlayerJoinEvent event){
 		if(Conflict.getPlayerCity(event.getPlayer().getName()) == null && !Conflict.handler.inGroup(event.getPlayer().getWorld().getName(), event.getPlayer().getName(), "Drifter")){
 			if(!Conflict.isUnassigned(event.getPlayer().getName()))
 				Conflict.addUnassigned(event.getPlayer().getName());
 		}
         
         Conflict.checkPlayerCityPermissions(event.getPlayer());
 	}
 
     // MystPortal code
 	// @EventHandler(priority = EventPriority.HIGHEST)
 	// public static void onPlayerPortal(PlayerPortalEvent event){
 		// if(event.getCause().equals(TeleportCause.NETHER_PORTAL) && event.getFrom().getWorld().getName().equals("survival") && Conflict.MystPortal.getNode().getLocation().distance(event.getFrom()) < 10){
 			// if( Conflict.playerCanUsePerk(event.getPlayer(), Conflict.MystPortal) ) {
 				// event.getPlayer().sendMessage("Shaaaaazaaam!");
 				// event.getPlayer().teleport(new Location(Bukkit.getServer().getWorld("mystworld"), 0.0, 0.0, 0.0));
 				// event.setTo(new Location(Bukkit.getServer().getWorld("mystworld"), 0.0, 0.0, 0.0));
 				// event.setCancelled(true);
 			// }else{
 				// event.getPlayer().sendMessage("Epic Fail");
 			// }
 		// }
 	// }
     
 	@EventHandler   
 	public static void onPlayerTeleport(PlayerTeleportEvent event){
 
 		Location dest = event.getTo();
 		Location src = event.getFrom();
 		Player player = event.getPlayer();
 
 		if (Conflict.war != null) {
 			// During wartime, cancel any tp in or out of nodes.
 			if (Conflict.war.getPlayerTeam(player) != null) {
 				for (Node n : Conflict.nodes) {
 					//Try to fix exception spew.
 					if (n.getLocation().getWorld().equals(dest.getWorld()) && dest.getWorld().equals(src.getWorld())
 							&& (n.isInRadius(dest) || n.isInRadius(src))) {
 						System.out.println("Attempting to cancel tp from " + src + " to " + dest + " for node at " + n.getLocation());
 						event.setCancelled(true);
 						return;
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler
 	/**
 	 * Think this adds xp to players near any player in the same city whose xp changes??
 	 * @param event
 	 */
 	public static void onPlayerExpChange(PlayerExpChangeEvent event){
 		if(event.getAmount() > 0){
 			List<Entity> entities = event.getPlayer().getNearbyEntities(20, 20, 20);
 			for(int i=0;i<entities.size();i++){
 				if(entities.get(i) instanceof Player) {
 					Player player = (Player)entities.get(i);
 					if (Conflict.getPlayerCity(player.getName()) != null
 							&& Conflict.getPlayerCity(event.getPlayer().getName()) != null) {
 						player.giveExp(event.getAmount());
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler
 	public static void onPlayerInteract(PlayerInteractEvent event){
 
 		if (event.getClickedBlock() == null) {
 			// WTF, Bukkit!
 			return;
 		}
 		Block block = event.getClickedBlock();
 		Player player = event.getPlayer();
 
         for (Node n : Conflict.nodes) {
             if (!block.getLocation().equals(n.getLocation())) { continue; }
             for (Perk p : n.getPerks()) {
                 if (!(p instanceof BlockPerk)) { continue; }
                 if (!Conflict.playerCanUsePerk(event.getPlayer(), p)) { 
                     event.getPlayer().sendMessage(Conflict.NOTICECOLOR + "You can't use this perk!");
                     continue;
                 }
                 ((BlockPerk)p).activate(event.getPlayer());
             }
         }
 		for (City city:Conflict.cities) {
 
 			if(block.getLocation().equals(city.getLocation().getBlock().getLocation())){
 				if(player.getItemInHand().getType() == Material.GOLD_INGOT){
 					int number = 1;
 					if(player.getItemInHand().getAmount() == 1){
 						player.getInventory().setItemInHand(null);
 					}else if(player.isSneaking()){
 						number = player.getItemInHand().getAmount();
 						player.getInventory().setItemInHand(null);
 					}else{
 						player.getItemInHand().setAmount(player.getItemInHand().getAmount()-1);
 						player.getInventory().setItemInHand(player.getItemInHand());
 					}
 					city.addMoney(number);
 					player.sendMessage("You gave " + Conflict.MONEYCOLOR + number + " gold bar(s) to " + Conflict.CITYCOLOR + city.getName());
 					player.updateInventory();
 				}
 				player.sendMessage(Conflict.CITYCOLOR + city.getName() + Conflict.TEXTCOLOR + " has "
 						+ Conflict.MONEYCOLOR + city.getMoney() + " gold!");
 			} else if(block.getLocation().equals(city.getSpongeLocation().getBlock().getLocation())){
 				try{
 					if(Conflict.handler.inGroup(player.getWorld().getName(), player.getName(), "Drifter")
 							&& !Conflict.handler.inGroup(player.getWorld().getName(), player.getName(), "Peasant")){
 						boolean successful = Conflict.joinCity(player, player.getName(), city.getName(), false);
						if (!successful){
 							player.sendMessage(Conflict.NOTICECOLOR + "Sorry you couldn't join a city, ask for help with the above error messages?");
							return;
						}
 						System.out.println("-------------------UPGRADE-DEBUG-----------");
 						System.out.println("Sponge hit by player: "+player);
 						Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "pex promote "+player.getName());
 						if(city.getSpawn() != null) player.teleport(city.getSpawn());
 						player.sendMessage(Conflict.NOTICECOLOR + "Congrats!  You've been accepted!!! You can leave this area now!");
 						player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF,10));
 						player.getInventory().addItem(new ItemStack(Material.IRON_SWORD,1));
 						player.getInventory().addItem(new ItemStack(Material.WOOD,64));
 						player.getInventory().addItem(new ItemStack(Material.TORCH,20));
 						player.getInventory().addItem(new ItemStack(Material.IRON_PICKAXE,1));
 						player.getInventory().addItem(new ItemStack(Material.STONE,64));
 						player.getInventory().addItem(new ItemStack(Material.COBBLESTONE,64));
 						System.out.println("----------------------------------------------");
 					}
 				}catch(NullPointerException e)
 				{
					System.out.println("EXCEPTION - Player trying to join a city failed");
					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	@EventHandler
 	public static void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
 		Player player = (Player)event.getPlayer();
 		if (event.getView().getType() == InventoryType.CRAFTING) {
 			if (!player.isOp() && !Conflict.handler.has(player, "conflict.debug")) {
 				BeyondUtil.nerfOverenchantedPlayerInventory(player);
 			}
 		}
 		else if (event.getView().getType() == InventoryType.CHEST) {
 			if (!player.isOp() && !Conflict.handler.has(player, "conflict.debug")) {
 				BeyondUtil.nerfOverenchantedInventory(event.getInventory());
 			}
 		}
 	}
 
 	@EventHandler
 	public static void onPlayerItemPickup(org.bukkit.event.player.PlayerPickupItemEvent event) {
 		Player player = (Player)event.getPlayer();
 		if (!player.isOp() && !Conflict.handler.has(player, "conflict.debug")) {
 			ItemStack i = event.getItem().getItemStack();
 			BeyondUtil.nerfOverenchantedItem(i);
 		}
 	}
 }
