 package de.hikinggrass.WhoPlacedIt;
 
 import java.util.logging.Logger;
 
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 
 public class WhoPlacedItPlayerListener extends PlayerListener {
 
 	public static WhoPlacedIt plugin;
 	protected Logger log;
 
 	protected Management manager;
 
 	public WhoPlacedItPlayerListener(WhoPlacedIt instance, Logger log, Management manager) {
 		plugin = instance;
 		this.log = log;
 		this.manager = manager;
 	}
 
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
				&& (this.manager.getInHand().contains(event.getPlayer().getItemInHand().getTypeId()))
				|| this.manager.getInHand().isEmpty()) {
 			// log.info("right clicked on block" + event.getClickedBlock().getTypeId());
 			// log.info("block is located at: x:" + event.getClickedBlock().getX() + " y: "
 			// + event.getClickedBlock().getY() + " z: " + event.getClickedBlock().getZ());
 			// log.info("now looking up in the database if this block is placed by another player...");
 			for (BlockInfo name : this.manager.getBlockInfo(event.getClickedBlock(), event.getPlayer())) {
 				if (name != null) {
 					for (String line : name.getMessage().split("\n")) {
 						event.getPlayer().sendMessage(name.getColor() + line);
 					}
 				}
 			}
 		}
 	}
 
 }
