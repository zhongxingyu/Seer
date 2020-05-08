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
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashSet;
 import java.util.logging.Level;
 
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
 
 	public int claim(Player claimant) {
 		Builddit.getInstance().getLogger().log(Level.INFO, "Claiming plot...");
 		if (!this.isOwned() || claimant.hasPermission("builddit.admin"))
 		{
 			Boolean dbsuccess = true;
 			if (!this.isOwned())
 			{
 				// No owner, INSERT into database table;
 				String querySavePlot = "INSERT INTO builddit_plot " +
 						"SET " +
 						"   world = \"" + this.getWorld().getName() + "\", " +
 						"   plotx = " + this.getPlotX() + ", " +
 						"   plotz = " + this.getPlotZ() + ", " +
 						"   owner = \"" + this.getOwner() + "\";";
 				Builddit.getInstance().getLogger().log(Level.INFO, "Query: " + querySavePlot);
 				if (Builddit.getInstance().database.runUpdateQuery(querySavePlot) == -1)
 				{
 					Builddit.getInstance().getLogger().log(Level.SEVERE, "INSERT failed. (dbsuccess = false)");
 					dbsuccess = false;
 				}
 				Builddit.getInstance().getLogger().log(Level.INFO, "Plot entry added");
 
 				// Fetch builddit_plot.id (pid) for model
 				String queryPID = "SELECT id FROM builddit_plot " +
 						"WHERE " +
 						"   world = \"" + this.getWorld().getName() + "\" " +
 						"   AND plotx = " + this.getPlotX() + " " +
 						"   AND plotz = " + this.getPlotZ() + " " +
 						"   AND owner = \"" + this.getOwner() + "\" " +
 						"LIMIT 1;";
 				Builddit.getInstance().getLogger().log(Level.INFO, "Query: " + queryPID);
 				try {
 					ResultSet rs = Builddit.getInstance().database.runSelectQuery(queryPID);
 					while (rs.next()) {
 						int pid = rs.getInt("id");
 						this.model.setPid(pid);
 						Builddit.getInstance().getLogger().log(Level.INFO, "PID set to: " + pid);
 					}
 				} catch (SQLException e) {
 					// Unable to get plot ID, utoh, database down?
 					Builddit.getInstance().getLogger().log(Level.SEVERE, "SQLException DB failure. (dbsuccess = false)");
 					dbsuccess = false;
 				} catch (NullPointerException e) {
 					// No result from database, bad transaction, not safe to continue
 					Builddit.getInstance().getLogger().log(Level.SEVERE, "NPE. (dbsuccess = false)");
 					dbsuccess = false;
 				}
 			} else {
 				// Previously owned, admin override, UPDATE record from database table;
 				String querySavePlot = "UPDATE builddit_plot " +
 						"SET " +
 						"   world = \"" + this.getWorld().getName() + "\", " +
 						"   plotx = " + this.getPlotX() + ", " +
 						"   plotz = " + this.getPlotZ() + ", " +
 						"   owner = \"" + this.getOwner() + "\" " +
 						"WHERE " +
 						"   id = " + this.model.getPid();
 				if (Builddit.getInstance().database.runUpdateQuery(querySavePlot) == -1)
 				{
 					dbsuccess = false;
 				}
 			}
 			if (dbsuccess) {
 				this.setOwner(claimant.getName());
 				return 1;
 			} else {
 				return -1;
 			}
 		}
 		return 0;
 	}
 
 	public int unclaim(Player unclaimant) {
 		if (this.getOwner().equals(unclaimant.getName()) || unclaimant.hasPermission("builddit.admin"))
 		{
 			// DELETE from database table
 			String querySavePlot = "DELETE FROM builddit_plot " +
 					"WHERE " +
 					"   id = " + this.model.getPid();
 			if (Builddit.getInstance().database.runUpdateQuery(querySavePlot) == -1)
 			{
 				return -1;
 			}
 			this.setOwner("");
 			return 1;
 		}
 		return 0;
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
 				// This should never happen as we've set vPos1 and vPos2 automatically in code.
 				return false;
 			}
 		}
 		return false;
 	}
 
 	public HashSet<Plot> getConnectedPlots() {
 		HashSet<Plot> connectedPlots = new HashSet<Plot>();
 		connectedPlots.addAll(getConnectedPlots(this, connectedPlots));
 		return connectedPlots;
 	}
 
 	public HashSet<Plot> getConnectedPlots(Plot rootPlot, HashSet<Plot> currentSet) {
 		// Almost "flood fill" like algorithm to find connected plots
 
 		// Note: String comparison is lighter than set.contains lookup for a large set, so we check owner first.
 		if (!this.getOwner().equals(rootPlot.getOwner()))
 		{
 			// This plot is not the same owner as the one we are checking for, we don't need to go further anymore.
 			return currentSet;
 		}
 
 		// If the set already know about this node, we don't need to scan again.
 		if (currentSet.contains(this)) {
 			return currentSet;
 		}
 
 		// This plot is the same owner as the one we are checking for, and not already in set, add itself to the set
 		currentSet.add(this);
 
 		// Look at our west, east, north, and south neighbour...
 		Plot west = BuildditPlot.getInstance().getPlotAt(this.getWorld(), this.getPlotX()-1, this.getPlotZ());
 		Plot east = BuildditPlot.getInstance().getPlotAt(this.getWorld(), this.getPlotX()+1, this.getPlotZ());
 		Plot north = BuildditPlot.getInstance().getPlotAt(this.getWorld(), this.getPlotX(), this.getPlotZ()+1);
 		Plot south = BuildditPlot.getInstance().getPlotAt(this.getWorld(), this.getPlotX(), this.getPlotZ()-1);
 
 		// ...and add their connected plots recursively as needed.
 		currentSet.addAll(west.getConnectedPlots(rootPlot,currentSet));
 		currentSet.addAll(east.getConnectedPlots(rootPlot,currentSet));
 		currentSet.addAll(north.getConnectedPlots(rootPlot,currentSet));
 		currentSet.addAll(south.getConnectedPlots(rootPlot,currentSet));
 
 		return currentSet;
 	}
 
 	public String toString() {
 		return "BuildditPlot{world=" + this.getWorld().getName() + ";plotX=" + this.getPlotX() + ";plotZ=" + this.getPlotZ() + ";owner='" + this.getOwner() + "'}";
 	}
 
 	public int load() {
 		// Attempt to load this Plot from MySQL
 		// Authorization is handled via authorize/unauthorize
 
 		/* Tables
 			builddit_plot
 			----------------------------------------------------------------------------------------------------
             CREATE TABLE `builddit_plot` (
 			 `id` int(10) NOT NULL AUTO_INCREMENT,
 			 `world` varchar(32) NOT NULL,
 			 `plotx` int(10) NOT NULL,
 			 `plotz` int(10) NOT NULL,
 			 `owner` varchar(24) NOT NULL,
 			 PRIMARY KEY (`id`),
 			 UNIQUE KEY `plot` (`world`,`plotx`,`plotz`)
 			) ENGINE=InnoDB DEFAULT CHARSET=utf8;
 
 			builddit_authorization
 			----------------------------------------------------------------------------------------------------
 			CREATE TABLE `builddit_authorization` (
 			 `id` int(10) NOT NULL AUTO_INCREMENT,
 			 `pid` int(10) NOT NULL,
 			 `player` varchar(24) NOT NULL,
 			 PRIMARY KEY (`id`),
 			 KEY `pid` (`pid`)
 			) ENGINE=InnoDB DEFAULT CHARSET=utf8;
 		 */
 
 		Builddit.getInstance().getLogger().log(Level.INFO, "Loading plot from database...");
 		String queryPlotInfo = "SELECT id, owner FROM builddit_plot " +
 								"WHERE " +
 								"   world = \"" + this.getWorld().getName() + "\"" +
 								"   AND plotx = " + this.getPlotX() + " " +
 								"   AND plotz = " + this.getPlotZ() + " " +
 								"LIMIT 1;";
 		Builddit.getInstance().getLogger().log(Level.INFO, "Query: " + queryPlotInfo);
 		try {
 			ResultSet rs = Builddit.getInstance().database.runSelectQuery(queryPlotInfo);
 			while (rs.next())
 			{
 				this.model.setPid(rs.getInt("id"));
 				this.setOwner(rs.getString("owner"));
 				Builddit.getInstance().getLogger().log(Level.INFO, "Got plot info; owner: '" + this.getOwner() + "'");
 			}
 		} catch (SQLException e) {
 			// Unable to access database, database server down?
 			return -1;
 		} catch (NullPointerException e) {
 			// Plot not in database, this was not previously claimed.
 			return 1;
 		}
 		return 1;
 	}
 
 	public HashSet<String> getAuthorized() {
 		return this.model.getAuthorized();
 	}
 
 	public void copyAuthFrom(Plot plot) {
		this.model.setAuthorized(plot.getAuthorized());
 	}
 }
