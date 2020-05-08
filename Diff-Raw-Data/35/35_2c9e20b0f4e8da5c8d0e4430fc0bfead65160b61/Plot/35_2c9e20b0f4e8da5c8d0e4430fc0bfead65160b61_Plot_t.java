 package net.chiisana.builddit.controller;
 
 import com.sk89q.worldedit.*;
 import com.sk89q.worldedit.bukkit.WorldEditAPI;
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 import com.sk89q.worldedit.masks.Mask;
 import com.sk89q.worldedit.regions.Region;
 import com.sk89q.worldedit.regions.RegionSelector;
 import net.chiisana.builddit.Builddit;
 import net.chiisana.builddit.model.PlotConfiguration;
 import net.chiisana.builddit.model.PlotModel;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 public class Plot {
 	private PlotModel model;
 
 	public Plot() {
 		this.model = new PlotModel();
 	}
 
 	public int getPlotX() {
 		return this.model.getPlotX();
 	}
 
 	public int getPlotZ() {
 		return this.model.getPlotZ();
 	}
 
 	public void setPlotXZ(int plotX, int plotZ) {
 		this.model.setPlotX(plotX);
 		this.model.setPlotZ(plotZ);
 	}
 
 	public void setWorld(World world) {
 		this.model.setWorld(world);
 	}
 
 	public World getWorld() {
 		return this.model.getWorld();
 	}
 
 	public Location getBottom() {
 		int xCord = this.getPlotX() > 0 ? this.getPlotX() - 1 : this.getPlotX();    // We don't actually have 0
 		int zCord = this.getPlotZ() > 0 ? this.getPlotZ() - 1 : this.getPlotZ();
 		Location location = new Location(
 					this.getWorld(),
 					xCord  * PlotConfiguration.intPlotCalculatedSize,
 					0,
 					zCord * PlotConfiguration.intPlotCalculatedSize
 		);
 		return location;
 	}
 
 	public Location getTop() {
 		int xCord = this.getPlotX() > 0 ? this.getPlotX() - 1 : this.getPlotX();    // We don't actually have 0
 		int zCord = this.getPlotZ() > 0 ? this.getPlotZ() - 1 : this.getPlotZ();
 		Location location = new Location(
 					this.getWorld(),
 					((xCord+1) * PlotConfiguration.intPlotCalculatedSize) - 1,
 					this.getWorld().getMaxHeight(),
 					((zCord+1) * PlotConfiguration.intPlotCalculatedSize) - 1
 		);
 		return location;
 	}
 
 	public boolean claim(String owner) {
 		if (this.isOwned())
 		{
 			return false;
 		} else {
 			this.setOwner(owner);
 			return true;
 		}
 	}
 
 	public boolean unclaim(Player unclaimant) {
 		if (this.getOwner().equals(unclaimant.getName()) || unclaimant.hasPermission("builddit.admin"))
 		{
 			this.setOwner("");
 			return true;
 		}
 		return false;
 	}
 
 	public String getOwner() {
 		return this.model.getOwner();
 	}
 
 	public void setOwner(String owner) {
 		this.model.setOwner(owner);
 	}
 
 	public boolean isOwned() {
 		return this.model.isOwned();
 	}
 
 	public boolean isAuthorizedFor(String user) {
 		return this.model.isAuthorizedFor(user);
 	}
 
 	public boolean authorize(String user, Player requester) {
 		if (this.getOwner().equals(requester.getName()) || requester.hasPermission("builddit.admin"))
 		{
 			this.model.authorize(user);
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public boolean unauthorize(String user, Player requester) {
 		if (this.getOwner().equals(requester.getName()) || requester.hasPermission("builddit.admin"))
 		{
 			this.model.unauthorize(user);
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public boolean clear(Player clearer) {
 		if (this.getOwner().equals(clearer.getName()) || clearer.hasPermission("builddit.admin"))
 		{
 			WorldEditPlugin wePlugin = Builddit.getInstance().wePlugin;
 			WorldEditAPI weAPI = Builddit.getInstance().weAPI;
 
 			LocalPlayer player = wePlugin.wrapPlayer(clearer);
 
 			Location location1 = this.getBottom();
 			Location location2 = this.getTop();
 
 			Vector vPos1 = new Vector(location1.getX(), location1.getY(), location1.getZ());
 			Vector vPos2 = new Vector(location2.getX(), location2.getY(), location2.getZ());
 
 			LocalSession session = weAPI.getSession(clearer);
 			EditSession editSession = session.createEditSession(player);
 			RegionSelector regionSelector = session.getRegionSelector(editSession.getWorld());
 
 			regionSelector.selectPrimary(vPos1);
 			regionSelector.explainPrimarySelection(player, session, vPos1);
 			regionSelector.selectSecondary(vPos2);
 			regionSelector.explainSecondarySelection(player, session, vPos2);
 
 			try {
 				// Note: Lifted code from WorldEdit as there is no API for regenerating a selection
 				Region region = session.getSelection(player.getWorld());
 				Mask mask = session.getMask();
 				session.setMask(null);
 				player.getWorld().regenerate(region, editSession);
 				session.setMask(mask);
 				return true;
 			} catch (IncompleteRegionException e) {
 				// This should never happen.
 				return false;
 			}
 		}
 		return false;
 	}
 }
