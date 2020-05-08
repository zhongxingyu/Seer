 package com.flashofsilver.emeraldmarket;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 public class EmeraldmarketEventListener implements Listener {
 
 	// pointer back to the main class
 	private Emeraldmarket plugin;
 
 	// constructor
 	public EmeraldmarketEventListener(Emeraldmarket plugin) {
 		this.plugin = plugin;
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		// check if they have any deals outstanding
 		plugin.notify(event.getPlayer());
 		// #XXX ^ fix problems with the above method
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerInteract(PlayerInteractEvent event) {
 
 		Block block = event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN);
 
 		if ((block.getType() == Material.SOIL) || (block.getType() == Material.CROPS))
 
 			if (event.hasItem()) {
 
 				if (event.getItem().getTypeId() == 1) {
 					return;
 				}
 			}
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onOfferAccepted(OfferAcceptedEvent event) {
		// debug message
		if (plugin.verbose) {
			plugin.logger.info("OfferAccepted event called.");
		}
 		// we need to inform the first party and give them the loot
 		// this can only be done when the player is online
 		Player firstParty = (Bukkit.getServer().getPlayer(event.getFirstParty()));
 		// if they are online, then getPlayer returns non-null
 		if (firstParty != null) {
 			plugin.notify(firstParty);
 			// some debug messages
 			if (plugin.verbose == true) {
 				plugin.logger.info(event.getSecondParty() + " acccepted an offer from "
 						+ event.getFirstParty() + ".");
 			}
 		} else {
 			// if they're offline, then we'll just wait until they come online.
 			if (plugin.verbose == true) {
 				plugin.logger.info(event.getSecondParty() + " acccepted an offer from "
 						+ event.getFirstParty() + " but " + event.getFirstParty()
 						+ " was not online to accept it.");
 			}
 		}
 		// we need to inform the second party and give them the loot
 		// this can only be done when the player is online
 		Player secondParty = (Bukkit.getServer().getPlayer(event.getSecondParty()));
 		// if they are online, then getPlayer returns non-null
 		if (secondParty != null) {
 			plugin.notify(secondParty);
 			// some debug messages
 			if (plugin.verbose == true) {
 				plugin.logger.info(event.getSecondParty() + " acccepted an offer from "
 						+ event.getFirstParty() + ".");
 			}
 		} else {
 			// if they're offline, then we'll just wait until they come online.
 			if (plugin.verbose == true) {
 				plugin.logger.info(event.getSecondParty() + " acccepted an offer from "
 						+ event.getFirstParty() + " but " + event.getSecondParty()
 						+ " were not online to accept it.");
 			}
 		}
		// finally, run the database cleanup
		plugin.checkDatabase();
 	}
 }
