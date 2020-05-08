 package cc.thedudeguy.xpinthejar.listeners;
 
 import net.minecraft.server.EntityPlayer;
 import net.minecraft.server.Packet18ArmAnimation;
 
 import org.bukkit.Material;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.player.PlayerExpChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerItemHeldEvent;
 import org.bukkit.inventory.ItemStack;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 import cc.thedudeguy.xpinthejar.XPInTheJar;
 import cc.thedudeguy.xpinthejar.util.Debug;
 
 public class BottleListener implements Listener {
 
 	/**
 	 * To show a chat message that a player clicked on an xp bottle
 	 * @param event
 	 */
 	@EventHandler
 	public void OnClickXpBottle(InventoryClickEvent event) {
 		
 		if (
 				event.getCurrentItem() != null &&
 				event.getCurrentItem().getType().equals(Material.GLASS_BOTTLE) &&
 				event.getCurrentItem().getDurability() > 0 &&
 				event.getWhoClicked() instanceof Player) {
 			
 			if(XPInTheJar.instance.spoutEnabled && ((SpoutPlayer)event.getWhoClicked()).isSpoutCraftEnabled()) {
 				((SpoutPlayer)event.getWhoClicked()).sendNotification("Exp Bottle", "Total: " + String.valueOf(event.getCurrentItem().getDurability()) + "xp", Material.GLASS_BOTTLE);
 			} else {
 				Player player = (Player)event.getWhoClicked();
 				player.sendMessage(" ");
 				player.sendMessage("-- Exp Bottle --");
 				player.sendMessage("-- Total: " + String.valueOf(event.getCurrentItem().getDurability()) + "xp");
 			}
 		}
 		
 	}
 	
 	/**
 	 * To show a chat message that a player is in fact holding an xp bottle
 	 * @param event
 	 */
 	@EventHandler
 	public void OnHoldXpBottle(PlayerItemHeldEvent event) {
 		if (
 				event.getPlayer().getInventory().getItem(event.getNewSlot()) != null &&
 				event.getPlayer().getInventory().getItem(event.getNewSlot()).getType().equals(Material.GLASS_BOTTLE) &&
 				event.getPlayer().getInventory().getItem(event.getNewSlot()).getDurability() > 0
 				) {
 			
 			if(XPInTheJar.instance.spoutEnabled && ((SpoutPlayer)event.getPlayer()).isSpoutCraftEnabled()) {
 				((SpoutPlayer)event.getPlayer()).sendNotification("Exp Bottle", "Total: " + String.valueOf(event.getPlayer().getInventory().getItem(event.getNewSlot()).getDurability()) + "xp", Material.GLASS_BOTTLE);
 			} else {
 				event.getPlayer().sendMessage(" ");
 				event.getPlayer().sendMessage("-- Exp Bottle --");
 				event.getPlayer().sendMessage("-- Total: " + String.valueOf(event.getPlayer().getInventory().getItem(event.getNewSlot()).getDurability()) + "xp");
 			}
 		}
 	}
 	
 	/**
 	 * Handle Bottling Experience when holding a bottle while picking up Experience
 	 * 
 	 * @param event
 	 */
 	@EventHandler
 	public void onBottleExp(PlayerExpChangeEvent event) {
 		
 		Debug.debug(event.getPlayer(), "Xp Pickup");
 		Debug.debug(event.getPlayer(), "Amount: ", event.getAmount());
 		
 		if (event.getPlayer().getItemInHand().getType().equals(Material.GLASS_BOTTLE)) {
 			
 			if (XPInTheJar.instance.getConfig().getBoolean("bottleRequireCrouch") && !event.getPlayer().isSneaking()) return;
 			
 			if ( event.getPlayer().getItemInHand().getAmount() > 1 ) {
 				event.getPlayer().sendMessage("Your holding too many bottles to collect XP, try holding just one.");
 				return;
 			}
 			
 			event.getPlayer().getItemInHand().setDurability((short)(event.getPlayer().getItemInHand().getDurability() + event.getAmount()));
 			
 			if(XPInTheJar.instance.spoutEnabled && ((SpoutPlayer)event.getPlayer()).isSpoutCraftEnabled()) {
 				((SpoutPlayer)event.getPlayer()).sendNotification( String.valueOf(event.getAmount()) + "xp Collected", String.valueOf(event.getPlayer().getItemInHand().getDurability()) + "xp", Material.GLASS_BOTTLE);
 			} else {
 				event.getPlayer().sendMessage("Collected " + String.valueOf(event.getAmount()) + "xp for a total of " + event.getPlayer().getItemInHand().getDurability());
 			}
 			
 			event.setAmount(0);
 			EntityPlayer p = ((CraftPlayer)event.getPlayer()).getHandle();
 			p.netServerHandler.sendPacket(new Packet18ArmAnimation(p, 1));
 			
 		}
 		
 	}
 	
 	/**
 	 * Handle Block interaction with a Bank Block or a Deposit Block.
 	 * @param event
 	 */
 	 @EventHandler
 	 public void onBlockInteract(PlayerInteractEvent event) {
 		 
 		 if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
 			 
 			/*
 			maybe theres an xp bottle in hand and
 			a player wants to consume it
 			*/
 			if (
 					event.getItem() != null &&
 					event.getItem().getType().equals(Material.GLASS_BOTTLE) &&
 					event.getItem().getDurability() > 0 &&
 					!event.isCancelled()
 					) {
 				if (event.getItem().getAmount() > 1) {
 					event.getPlayer().sendMessage("You are holding too many XP Bottles, try holding just one");
 				} else {
 					event.getPlayer().giveExp(event.getItem().getDurability());
 					
 					if(XPInTheJar.instance.spoutEnabled && ((SpoutPlayer)event.getPlayer()).isSpoutCraftEnabled()) {
 						((SpoutPlayer)event.getPlayer()).sendNotification( "Exp Bottle Emptied", String.valueOf(event.getItem().getDurability()) + "xp", Material.GLASS_BOTTLE);
 					} else {
 						event.getPlayer().sendMessage(String.valueOf(event.getItem().getDurability())+"xp emptied into your gut-hole");
 					}
 					
 					if (XPInTheJar.instance.getConfig().getBoolean("consumeBottleOnUse")) {
 						event.getPlayer().setItemInHand(new ItemStack(Material.AIR));
 					} else {
 						event.getItem().setDurability((short)0);
 					}
 				}
 			}
 		} 
 	}
 }
