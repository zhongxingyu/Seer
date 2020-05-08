 package com.mccraftaholics.warpportals.bukkit;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockFromToEvent;
 import org.bukkit.event.block.BlockPhysicsEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPortalEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.mccraftaholics.warpportals.api.WarpPortalsEvent;
 import com.mccraftaholics.warpportals.api.WarpPortalsPlayerBlockMoveEvent;
 import com.mccraftaholics.warpportals.api.WarpPortalsTeleportEvent;
 import com.mccraftaholics.warpportals.helpers.Defaults;
 import com.mccraftaholics.warpportals.manager.PortalManager;
 import com.mccraftaholics.warpportals.objects.CoordsPY;
 import com.mccraftaholics.warpportals.objects.PortalInfo;
 
 public class BukkitEventListener implements Listener {
 	JavaPlugin mPlugin;
 	PortalManager mPortalManager;
 	YamlConfiguration mPortalConfig;
 	ChatColor mCC;
 	boolean mAllowNormalPortals;
 
 	public BukkitEventListener(JavaPlugin plugin, PortalManager portalManager, YamlConfiguration portalConfig) {
 		mPlugin = plugin;
 		mPortalManager = portalManager;
 		mPortalConfig = portalConfig;
 
 		mCC = ChatColor.getByChar(mPortalConfig.getString("portals.general.textColor", Defaults.CHAT_COLOR));
 		mAllowNormalPortals = mPortalConfig.getBoolean("portals.general.allowNormalPortals", Defaults.ALLOW_NORMAL_PORTALS);
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onPlayerMove(PlayerMoveEvent e) {
 		/*
 		 * Watch all player move events and simplify them down to block move
 		 * events. Block move events can be listened for as
 		 * WarpPortalsPlayerBlockMoveEvent
 		 */
 		// Check if player is moving between blocks
 		if (!e.getFrom().getBlock().equals(e.getTo().getBlock())) {
 			// Create event with previous block-course location and new
 			// block-course location
 			WarpPortalsPlayerBlockMoveEvent playerBlockMoveEvent = new WarpPortalsPlayerBlockMoveEvent(e.getPlayer(), e.getFrom(), e.getTo());
 			// Call event
 			Bukkit.getPluginManager().callEvent(playerBlockMoveEvent);
 			// Update PlayerMoveEvent status to match
 			// WarpPortalsPlayerBlockMoveEvent
 			e.setCancelled(playerBlockMoveEvent.isCancelled());
 		}
 	}
 
 	@EventHandler
 	public void onPlayerBlockMove(WarpPortalsPlayerBlockMoveEvent event) {
 		Player player = event.getPlayer();
 		// Check if player is in a WarpPortal
 		PortalInfo portal = mPortalManager.isLocationInsidePortal(event.getTo());
 		// If player is in a WarpPortal
 		if (portal != null) {
 			// Check player permissions to use portal
			boolean hasPermission = player.hasPermission("warpportal.enter");
 
 			// Create WarpPortalsEvent
 			WarpPortalsEvent wpEvent = new WarpPortalsEvent(player, portal, hasPermission);
 			// Call WarpPortalsEvent
 			Bukkit.getPluginManager().callEvent(wpEvent);
 
 			// Check if event has been cancelled
 			// Event status defaults to player permissions to use the portal
 			if (!wpEvent.isCancelled()) {
 				// Get (possibly modified) teleportation data
 				CoordsPY tpCoords = wpEvent.getTeleportCoordsPY();
 
 				// Save player's current location
 				Location preTPLocation = player.getLocation();
 
 				/* Teleport player */
 				Location tpLoc = new Location(tpCoords.world, tpCoords.x, tpCoords.y, tpCoords.z);
 				tpLoc.setPitch(tpCoords.pitch);
 				tpLoc.setYaw(tpCoords.yaw);
 				player.teleport(tpLoc);
 
 				WarpPortalsTeleportEvent wpTPEvent = new WarpPortalsTeleportEvent(player, preTPLocation);
 				// Call WarpPortalsTeleportEvent
 				Bukkit.getPluginManager().callEvent(wpTPEvent);
 
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPortalEnter(PlayerPortalEvent event) {
 		// Get player involved in the event
 		Player player = event.getPlayer();
 		// Check if player is in a WarpPortal or normal portal
 		/*
 		 * If the player is in a WarpPortal, let the onPlayerBlockMoveEvent
 		 * listener handle it.
 		 */
 		if (mPortalManager.isLocationInsidePortal(player.getLocation()) == null) {
 			// The player did not enter a WarpPortal
 			/*
 			 * Check to see if the server is configured to allow normal portal
 			 * events
 			 */
 			if (!mAllowNormalPortals)
 				// If not allowed, cancel the event
 				event.setCancelled(true);
 		} else
 			// If in a WarpPortal, cancel the event
 			event.setCancelled(true);
 	}
 
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent e) {
 		if (e.hasBlock() && e.hasItem() && e.getClickedBlock() != null && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
 			mPortalManager.playerItemRightClick(e);
 		}
 	}
 
 	/**
 	 * Used to allow PORTAL blocks to face any direction, with a contiguous
 	 * direction as its adjacent portal blocks.
 	 * 
 	 * @param e
 	 */
 	@EventHandler
 	public void onBlockPhysicsEvent(BlockPhysicsEvent e) {
 		if (e.getBlock().getType() == Material.PORTAL) {
 			e.setCancelled(true);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockFromTo(BlockFromToEvent event) {
 		Block block = event.getBlock();
 		if (mPortalManager.isLocationInsidePortal(block.getLocation()) != null) {
 			event.setCancelled(true);
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockBreak(BlockBreakEvent event) {
 		Block block = event.getBlock();
 		if (mPortalManager.isLocationInsidePortal(block.getLocation()) != null) {
 			if (!event.getPlayer().hasPermission("warpportals.admin.breakblock"))
 				event.setCancelled(true);
 		}
 	}
 
 }
