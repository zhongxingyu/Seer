 package com.ubempire.not.a.portal;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.block.BlockIgniteEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockPhysicsEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 
 public class PinappPortalCreate {
 	Pinapp p;
 
 	public PinappPortalCreate(Pinapp p) {
 		this.p = p;
 	}
 
 	public void register() {
 		BlockWatch b = new BlockWatch(p);
 		p.getServer().getPluginManager()
 				.registerEvent(Event.Type.BLOCK_PLACE, b, Priority.Normal, p);
 		p.getServer().getPluginManager()
 				.registerEvent(Event.Type.BLOCK_IGNITE, b, Priority.Normal, p);
 		p.getServer().getPluginManager()
 				.registerEvent(Event.Type.BLOCK_PHYSICS, b, Priority.Normal, p);
 		p.log("Registered BLOCK_PLACE, BLOCK_IGNITE, and BLOCK_PHYSICS events successfully.");
 	}
 }
 
 class BlockWatch extends BlockListener {
 	Pinapp p;
 
 	BlockWatch(Pinapp p) {
 		this.p = p;
 	}
 
 	public void onBlockPlace(BlockPlaceEvent event) {
 		if(event.isCancelled())
 			return;
 		Block block = event.getBlock().getRelative(0, -1, 0);
 		Player player = event.getPlayer();
 		if (player == null)
 			return;
 		if (!(player.hasPermission("pinapp.portal.create") || player.isOp()))
 			return;
 		if (event.getBlock().getTypeId() != 51)
 			return;
 		event.getBlock().setTypeId(0);
		CheckBlock check = new CheckBlock(block);
 		int orientation = check.check();
 		if (orientation == -1)
 			event.getBlock().setTypeId(51);
 		check.createPortal(orientation);
 	}
 
 	public void onBlockPhysics(BlockPhysicsEvent event) {
 		if(event.isCancelled())
 			return;
 		if (event.getBlock().getType() != Material.PORTAL)
 			return;
 		Block block = event.getBlock();
 		for (int i = 0; i <= 4; i++) {
 			if (block.getRelative(0, -i, 0).getType() != Material.PORTAL
 					&& block.getRelative(0, -1, 0).getType() != Material.AIR) {
 				block = block.getRelative(0, -i, 0);
 				break;
 			}
 		}
		CheckBlock b = new CheckBlock(block);
 		int orientation = b.check();
 		if (orientation >= 0)
 			event.setCancelled(true);
 	}
 
 	public void onBlockIgnite(BlockIgniteEvent event) {
 		if(event.isCancelled())
 			return;
 		Block block = event.getBlock();
 		Player player = event.getPlayer();
 		if (player == null)
 			return;
 		if (!(player.hasPermission("pinapp.portal.create") || player.isOp()))
 			return;
 		event.getBlock().getRelative(0, 1, 0).setTypeId(0);
		CheckBlock check = new CheckBlock(block);
 		int orientation = check.check();
 		check.createPortal(orientation);
 		if (orientation == -1)
 			event.getBlock().getRelative(0, 1, 0).setTypeId(51);
 	}
 }
