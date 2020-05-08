 package com.mccraftaholics.warpportals.bukkit;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockPhysicsEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerPortalEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.mccraftaholics.warpportals.helpers.Defaults;
 import com.mccraftaholics.warpportals.manager.PortalManager;
 import com.mccraftaholics.warpportals.objects.CoordsPY;
 
 public class BukkitEventListener implements Listener {
 	JavaPlugin mPlugin;
 	PortalManager mPortalManager;
 	YamlConfiguration mPortalConfig;
 	String mCC;
 	String mTPMessage;
 	String mTPC;
 	boolean mAllowNormalPortals;
 
 	public BukkitEventListener(JavaPlugin plugin, PortalManager portalManager, YamlConfiguration portalConfig) {
 		mPlugin = plugin;
 		mPortalManager = portalManager;
 		mPortalConfig = portalConfig;
 
 		mCC = mPortalConfig.getString("portals.general.textColor", Defaults.CHAT_COLOR);
 		mTPMessage = mPortalConfig.getString("portals.teleport.message", Defaults.TP_MESSAGE);
 		mTPC = mPortalConfig.getString("portals.teleport.messageColor", Defaults.TP_MSG_COLOR);
 		mAllowNormalPortals = mPortalConfig.getBoolean("portals.general.allowNormalPortals", Defaults.ALLOW_NORMAL_PORTALS);
 	}
 
 	long mSartTime = 0;
 	long mTimeTaken = 0;
 
	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPortalEnter(PlayerPortalEvent event) {
 		Player player = event.getPlayer();
 		CoordsPY tpCoords = mPortalManager.checkPlayerLoose(player.getLocation());
 		if (tpCoords != null) {
 			if (player.hasPermission("warpportal.enter")) {
 				// So event doesn't propagate to default handling
 				event.setCancelled(true);
 				// Handle WarpPortal Teleportation
 				player.sendMessage(mTPC + mTPMessage);
 				Location tpLoc = new Location(tpCoords.world, tpCoords.x, tpCoords.y, tpCoords.z);
 				tpLoc.setPitch(tpCoords.pitch);
 				tpLoc.setYaw(tpCoords.yaw);
 				player.teleport(tpLoc);
 			}
 		} else {
 			if (!mAllowNormalPortals)
 				event.setCancelled(true);
 		}
 	}
 
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent e) {
 		if (e.hasBlock() && e.hasItem() && e.getClickedBlock() != null && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
 			mPortalManager.playerItemRightClick(e);
 		}
 	}
 
 	@EventHandler
 	public void o(BlockPhysicsEvent e) {
 		if (e.getBlock().getType() == Material.PORTAL) {
 			e.setCancelled(true);
 		}
 	}
 
 }
