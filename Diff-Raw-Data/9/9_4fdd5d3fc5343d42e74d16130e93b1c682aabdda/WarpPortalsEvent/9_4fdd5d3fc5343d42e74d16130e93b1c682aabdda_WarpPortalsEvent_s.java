 package com.mccraftaholics.warpportals.api;
 
 import com.mccraftaholics.warpportals.objects.CoordsPY;
 import com.mccraftaholics.warpportals.objects.PortalInfo;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Cancellable;
 import org.bukkit.event.Event;
 import org.bukkit.event.HandlerList;
 
 /**
  *
  * An Event that fires when a player is teleported by a WarpPortal
  *
  * @see Event
  *
  */
 public class WarpPortalsEvent extends Event implements Cancellable {
 
 	private static final HandlerList handlers = new HandlerList();
 	private boolean cancelled;
 
     // Event data
     private Player player;
     private PortalInfo portal;
     private boolean hasPermission;
 
     // Modifiable event data
     private CoordsPY tpCoords;
 
 	/**
 	 * Constructor of a WorldSniperWandSelectionEvent * * Between pos1 and pos2
 	 * is the selection *
 	 *
 	 * @param p
 	 *            - Player that is selecting Something WIth the wand * @param
 	 *            pos1 - The First Position
 	 */
 	public WarpPortalsEvent(Player p, PortalInfo po, boolean hp) {
		this.cancelled = hp;
 
         player = p;
         portal = po.clone();
         hasPermission = hp;
 
         tpCoords = portal.tpCoords.clone();
 	}
 
     /* Bukkit Event API methods */
 
 	@Override
 	public HandlerList getHandlers() {
 		return handlers;
 	}
 
 	public static HandlerList getHandlerList() {
 		return handlers;
 	}
 
 	@Override
 	public boolean isCancelled() {
 		return cancelled;
 	}
 
 	@Override
 	public void setCancelled(boolean cancel) {
 		this.cancelled = cancel;
 	}
 
 
     /* WarpPortal Event API methods */
 
     /* Get event data */
     /**
      * Get the player that is entering the WarpPortal.
      *
      * @return Player associated with the event
      */
     public Player getPlayer() {
         return player;
     }
 
     /**
      * Get a copy of the WarpPortal that the player is entering. PortalInfo contains portal location data and teleportation data.
      *
      * @return PortalInfo - WarpPortal associated with the event
      */
     public PortalInfo getPortal() {
         return portal;
     }
 
     /**
      * Does the player have permission to enter the portal? Permission according to player.hasPermission("warpportal.enter")
      *
      * @return boolean
      */
     public boolean hasPermission() {
         return hasPermission;
     }
 
     /**
      * Get a copy of the Teleport Coordinates that the player is scheduled for. CoordsPY contains world, x, y, z, pitch and yaw data.
      *
      * @return CoordsPY - Teleport CoordsPY associated with the event
      */
     public CoordsPY getTeleportCoordsPY() {
         return tpCoords;
     }
 
     /* Set event data */
 
     /**
      * Modify the player's teleport destination.
      * The CoordsPY supplied to this method will override the original teleport destination, and WarpPortals will teleport the player to this new location.
      *
      * @param newTPC - CoordsPY of the new destination
      */
     public void setTeleportCoordsPY(CoordsPY newTPC) {
         tpCoords = newTPC;
     }
 
     /**
      * Modify the player's teleport destination.
      * The Location supplied to this method will override the original teleport destination, and WarpPortals will teleport the player to this new location.
      *
      * @param newLoc - CoordsPY of the new destination
      */
     public void setTeleportCoordsPY(Location newLoc) {
         setTeleportCoordsPY(new CoordsPY(newLoc));
     }
 }
