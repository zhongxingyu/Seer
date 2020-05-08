 package com.mtihc.regionselfservice.v2.plots;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.util.BlockVector;
 
 import com.mtihc.regionselfservice.v2.plots.exceptions.EconomyException;
 import com.mtihc.regionselfservice.v2.plots.exceptions.PlotBoundsException;
 import com.mtihc.regionselfservice.v2.plots.exceptions.PlotControlException;
 import com.mtihc.regionselfservice.v2.plots.exceptions.SignException;
 import com.mtihc.regionselfservice.v2.plots.signs.PlotSignType;
 import com.sk89q.worldedit.bukkit.selections.Selection;
 import com.sk89q.worldguard.LocalPlayer;
 import com.sk89q.worldguard.domains.DefaultDomain;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;
 
 public class PlotControl {
 
 	private PlotManager mgr;
 
 	public PlotControl(PlotManager manager) {
 		this.mgr = manager;
 	}
 	
 	public PlotManager getPlotManager() {
 		return mgr;
 	}
 	
 	public int getRegionCountOfPlayer(World world, String playerName) {
 		// get WorldGuard's region manager
 		RegionManager regionManager = mgr.getPlotWorld(world.getName()).getRegionManager();
 		
 		// get online player
 		Player p = Bukkit.getPlayerExact(playerName);
 		if(p != null) {
 			// when player is online, use WorldGuard's method of counting regions
 			return regionManager.getRegionCountOfPlayer(mgr.getWorldGuard().wrapPlayer(p)); 
 		}
 		
 		// player is offline
 		
 		// get all regions
 		Collection<ProtectedRegion> regions = regionManager.getRegions().values();
 		if(regions == null || regions.isEmpty()) {
 			return 0;
 		}
 		
 		// count owned regions
 		int count = 0;
 		for (ProtectedRegion region : regions) {
 			if(region.isOwner(playerName.toLowerCase())) {
 				count++;
 			}
 		}
 		return count;
 		
 	}
 	
 	private static final HashSet<Byte> invisibleBlocks = new HashSet<Byte>();
 	
 	public static HashSet<Byte> getInvisibleBlocks() {
 		if(invisibleBlocks.isEmpty()) {
 			invisibleBlocks.add((byte) Material.AIR.getId());
 			invisibleBlocks.add((byte) Material.WATER.getId());
 			invisibleBlocks.add((byte) Material.STATIONARY_WATER.getId());
 			invisibleBlocks.add((byte) Material.LAVA.getId());
 			invisibleBlocks.add((byte) Material.STATIONARY_LAVA.getId());
 			invisibleBlocks.add((byte) Material.SNOW.getId());
 			invisibleBlocks.add((byte) Material.LONG_GRASS.getId());
 		}
 		return invisibleBlocks;
 	}
 	
 	public static Sign getTargetSign(Player player) {
 		// get targeted block
 		Block block = player.getTargetBlock(getInvisibleBlocks(), 8);
 		
 		// check if block is a wooden sign, 
 		// return null otherwise
 		if(block.getState() instanceof Sign) {
 			return (Sign) block.getState();
 		}
 		else {
 			return null;
 		}
 	}
 	
 	public Set<String> getPotentialHomeless(World world, Set<String> names) {
 		HashSet<String> result = new HashSet<String>();
 		if(!names.isEmpty()) {
 			// region has owners, 
 			// iterate over owners
 			for (String ownerName : names) {
 				// count regions of owner
 				int ownerRegionCount = getRegionCountOfPlayer(world, ownerName);
 				if(ownerRegionCount - 1 == 0) {
 					// player would become homeless
 					result.add(ownerName);
 				}
 				
 			}
 			
 		}
 		return result;
 	}
 	
 	public void buy(Player player) throws PlotControlException {
 		// get targeted sign
 		Sign sign = getTargetSign(player);
 		if(sign == null) {
 			throw new PlotControlException("You're not looking at a wooden sign.");
 		}
 		
 		BlockVector coords = sign.getLocation().toVector().toBlockVector();
 		PlotWorld world = mgr.getPlotWorld(player.getWorld().getName());
 		Plot plot;
 		IPlotSign plotSign = null;
 		
 		try {
 			// try to get the plot-object via wooden sign, 
 			// the sign should probably have the region name on the last 2 lines
 			plot = world.getPlot(sign);
 		} catch (SignException e) {
 			throw new PlotControlException("You're not looking at a valid sign: " + e.getMessage(), e);
 		}
 		
 		
 		if(plot != null) {
 			// couldn't find plot-object using the targeted sign.
 			// The plot-data was probably deleted.
 			plotSign = (IPlotSign) plot.getSign(coords);
 		}
 		else {
 			throw new PlotControlException("Couldn't find plot information.");
 		}
 		
 		
 		if(plotSign == null) {
 			throw new PlotControlException("Couldn't find plot-sign information.");
 		}
 		
 		if(plotSign.getType() != PlotSignType.FOR_SALE) {
 			// plot-sign is not a for-sale sign
 			throw new PlotControlException("You're not looking at a for-sale sign.");
 		}
 		
 		// get ProtectedRegion
 		ProtectedRegion region = plot.getRegion();
 		if(region == null) {
 			throw new PlotControlException("Sorry, the region doesn't exist anymore.");
 		}
 
 		// not for sale?
 		if(!plot.isForSale()) {
 			throw new PlotControlException("Sorry, region \"" + plot.getRegionId() + "\" isn't for sale. This is probably an old sign.");
 		}
 
 		// already owner?
 		if(region.isOwner(player.getName())) {
 			throw new PlotControlException("You already own this region.");
 		}
 		
 
 		// 
 		// Check if player has too many regions
 		// or special permission
 		// 
 		int regionCount = getRegionCountOfPlayer(world.getWorld(), player.getName());
 		int regionMax = world.getConfig().getMaxRegionCount();
 		boolean bypassMax = player.hasPermission(Permission.BYPASSMAX_REGIONS);
 		
 		if(!bypassMax && regionCount >= regionMax) {
 			throw new PlotControlException("You already own " + regionCount + " regions (max: " + regionMax + ").");
 		}
 		
 		
 		
 		
 		// get region cost
 		double cost = plot.getSellCost();
 		
 		// 
 		// check if it's a free region, 
 		// and if it's reserved, 
 		// and if player already has a region
 		// 
 		boolean reserve = world.getConfig().isReserveFreeRegionsEnabled();
 		if(reserve && cost <= 0 && regionCount > 0) {
 			throw new PlotControlException("Free regions are reserved for new players.");
 		}
 		
 		
 		
 		// 
 		// Check if players would become homless after sale.
 		// This is part of preventing cheating with free regions.
 		// 
 		Set<String> owners = region.getOwners().getPlayers();
 		// get members for later
 		Set<String> members = region.getMembers().getPlayers();
 		
 		int ownerCount = owners.size();
 		
 		if(reserve) {
 			Set<String> homeless = getPotentialHomeless(world.getWorld(), owners);
 			if(!homeless.isEmpty()) {
 				String homelessString = "";
 				for (String string : homeless) {
 					homelessString += ", " + string;
 				}
 				homelessString = homelessString.substring(2);// remove comma and space
 				throw new PlotControlException("Sorry, you can't buy this region. The following players would become homeless: " + homelessString);
 			}
 		}
 		
 		// check bypasscost || pay for region
 		
 		boolean bypassCost = player.hasPermission(Permission.BUY_BYPASSCOST);
 		
 		if(!bypassCost) {
 			try {
 				mgr.getEconomy().withdraw(player.getName(), cost);
 			} catch (EconomyException e) {
 				throw new PlotControlException("Failed to pay for region: " + e.getMessage());
 			}
 		}
 		
 
         double share = cost;
         
         // --------------------
         // TAX BEGIN
         // --------------------
 
 
         String taxAccount = world.getConfig().getTaxAccount();
         double percentageTax = world.getConfig().getTaxPercent();
         double percentage = 0;
         if(cost >= world.getConfig().getTaxFromPrice()) {
                 
                 percentage = percentageTax * cost / 100;
                 share -= percentage;
                 mgr.getEconomy().deposit(taxAccount, percentage);
         }
         
         // --------------------
         // TAX END
         // --------------------
 		
 		
 		// calc share and pay owners their share
 		share = share / Math.max(1, ownerCount);
 		for (String ownerName : owners) {
 			mgr.getEconomy().deposit(ownerName, share);
 		}
 		
 		// remove owners, add buyer as owner
 		DefaultDomain newOwnerDomain = new DefaultDomain();
 		newOwnerDomain.addPlayer(player.getName());
 		region.setOwners(newOwnerDomain);
 		// save region owner changes
 		try {
 			world.getRegionManager().save();
 		} catch (ProtectionDatabaseException e) {
 			String msg = "Failed to save region changes to world \"" + world.getName() + "\", using WorldGuard.";
 			mgr.getPlugin().getLogger().log(Level.SEVERE, msg, e);
 			throw new PlotControlException(msg + " " + e.getMessage(), e);
 		}
 		
 		// break all for sale signs
 		Collection<IPlotSignData> forSaleSigns = plot.getSigns(PlotSignType.FOR_SALE);
 		for (IPlotSignData data : forSaleSigns) {
 			BlockVector vec = data.getBlockVector();
 			Block block = vec.toLocation(world.getWorld()).getBlock();
 			if(block.getState() instanceof Sign) {
 				block.breakNaturally();
 			}
 			plot.removeSign(vec);
 		}
 		
 		
 		
 		// delete plot-info if possible, otherwise just save changes
 		// (a plot can't be deleted when there's still active renters)
 		if(!plot.delete()) {
 			plot.save();
 		}
 		mgr.messages.bought(region.getId(), player, cost, owners, members, share, taxAccount, percentage);
 		
 	}
 	
 	public void rent(Player player) {
 		// TODO
 		
 		// mgr.messages.rented
 	}
 	
 	private Selection getSelection(Player player) throws PlotControlException {
 		Selection sel = mgr.getWorldEdit().getSelection(player);
 		if(sel == null || sel.getMaximumPoint() == null || sel.getMinimumPoint() == null) {
 			throw new PlotControlException("Select a region first. Use WorldEdit's command: " + ChatColor.LIGHT_PURPLE + "//wand");
 		}
 		return sel;
 	}
 	
 	private ProtectedRegion defineRegion(PlotWorld plotWorld, Player player, String regionId, Selection sel, int bottomY, int topY) throws PlotControlException {
 		return defineRegion(plotWorld, player, regionId, sel, bottomY, topY, null);
 	}
 	
 	private ProtectedRegion defineRegion(PlotWorld plotWorld, Player player, String regionId, Selection sel, int bottomY, int topY, ProtectedRegion existing) throws PlotControlException {
 		int by;
 		int ty;
 		// If value is -1, use exact selection, 
 		// otherwise use specified value.
 		// Specified value will be default value from config, or arguments from command
 		if(bottomY <= -1) {
 			by = sel.getMinimumPoint().getBlockY();
 		}
 		else {
 			by = bottomY;
 		}
 		if(topY <= -1) {
 			ty = sel.getMaximumPoint().getBlockY();
 		}
 		else {
 			ty = topY;
 		}
 		
 		// switch values if necessary
 		if(ty < by) {
 			int y = ty;
 			ty = by;
 			by = y;
 		}
 		
 		if(!player.hasPermission(Permission.CREATE_ANYSIZE)) {
 			
 			int width = sel.getWidth();
 			int length = sel.getLength();
 			int height = sel.getHeight();
 			
 			int minY = plotWorld.getConfig().getMinimumY();
 			int maxY = plotWorld.getConfig().getMaximumY();
 			int minHeight = plotWorld.getConfig().getMinimumHeight();
 			int maxHeight = plotWorld.getConfig().getMaximumHeight();
 			int minWidthLength = plotWorld.getConfig().getMinimumWidthLength();
 			int maxWidthLength = plotWorld.getConfig().getMaximumWidthLength();
 			
 			// check min width/length/height
 			if(width < minWidthLength || length < minWidthLength || height < minHeight) {
 				throw new PlotBoundsException(
 						PlotBoundsException.Type.SELECTION_TOO_SMALL, 
 						width, length, height, minWidthLength, maxWidthLength, minHeight, maxHeight);
 			}
 			// check max width/length/height
 			else if(width > maxWidthLength || length > maxWidthLength || height > maxHeight) {
 				throw new PlotBoundsException(
 						PlotBoundsException.Type.SELECTION_TOO_BIG, 
 						width, length, height, maxWidthLength, maxWidthLength, minHeight, maxHeight);
 			}
 			// check maxY
 			if(topY > maxY) {
 				throw new PlotBoundsException(
 						PlotBoundsException.Type.SELECTION_TOO_HIGH, 
 						topY, bottomY, minY, maxY);
 			}
 			// check minY
 			if(bottomY < minY) {
 				throw new PlotBoundsException(
 						PlotBoundsException.Type.SELECTION_TOO_LOW, 
 						topY, bottomY, minY, maxY);
 			}
 		}
 		
 		
 		
 		
 		
 		
 		Location min = sel.getMinimumPoint();
 		Location max = sel.getMaximumPoint();
 		// create protected region
 		ProtectedCuboidRegion region = new ProtectedCuboidRegion(
 				regionId, 
 				new com.sk89q.worldedit.BlockVector(min.getBlockX(), bottomY, min.getBlockZ()), 
 				new com.sk89q.worldedit.BlockVector(max.getBlockX(), topY, max.getBlockZ()));
 		
 		if(existing != null) {
 			// redefining region, so keep existing values
 			region.setFlags(existing.getFlags());
 			region.setMembers(existing.getMembers());
 			region.setOwners(existing.getOwners());
 			region.setPriority(existing.getPriority());
 			try {
 				region.setParent(existing.getParent());
 			} catch (CircularInheritanceException e) {
 				// ignore error
 			}
 		}
 		
 		
 		
 		
 		
 		boolean allowOverlap = plotWorld.getConfig().isOverlapUnownedRegionAllowed();
 		if(!allowOverlap && overlapsUnownedRegion(region, plotWorld.getWorld(), player)) {
 			// overlapping is not allowed
 			throw new PlotControlException("Your selection overlaps with someone else's region.");
 		}
 		
 		// TODO this needs another look-over? 
 		// Why not do automatic parent, outside else-statement?
 		// What's up with that permission?
 		else {
 			// not overlapping or it's allowed to overlap
 			
 			boolean doAutomaticParent = plotWorld.getConfig().isAutomaticParentEnabled();
 			boolean allowAnywhere = player.hasPermission(Permission.CREATE_ANYWHERE);
 			
 			ProtectedRegion parentRegion;
 			if(!allowAnywhere || doAutomaticParent) {
 				// we need a parent
 				parentRegion = getAutomaticParentRegion(region, plotWorld.getWorld(), player);
 				
 				if(parentRegion == null) {
 					if(!allowAnywhere) {
 						// automatic parent was not found, but it's required...
 						// because player can only create regions inside owned existing regions.
 						throw new PlotControlException("You can only claim regions inside existing regions that you own");
 					}
 				}
 				else if(doAutomaticParent) {
 					// found parent region,
 					// and according to the configuration,
 					// we should do automatic parenting
 					try {
 						region.setParent(parentRegion);
 					} catch (CircularInheritanceException e) {
 					}
 				}
 			}
 		}
 		return region;
 	}
 	
 	public void define(Player player, String regionId) throws PlotControlException {
 		// get player's selection
 		Selection sel = getSelection(player);
 		// get plot-world information
 		PlotWorld plotWorld = mgr.getPlotWorld(sel.getWorld().getName());
 		
 		
 		
 		// define, using default bottom y and top y
 		define(player, regionId, plotWorld.getConfig().getDefaultBottomY(), plotWorld.getConfig().getDefaultTopY());
 	}
 	
 	public void define(Player player, String regionId, int bottomY, int topY) throws PlotControlException {
 		// get player's selection
 		Selection sel = getSelection(player);
 		// get plot-world information
 		PlotWorld plotWorld = mgr.getPlotWorld(sel.getWorld().getName());
 		
 		
 		
 		// get world's RegionManager of WorldGuard
 		RegionManager regionManager = plotWorld.getRegionManager();
 		
 		// check region existance
 		if(regionManager.hasRegion(regionId)) {
 			throw new PlotControlException("Region \"" + regionId + "\" already exists.");
 		}
 		// check if valid region name, just like WorldGuard
 		if(!isValidRegionName(regionId)) {
 			throw new PlotControlException("Invalid region name \"" + regionId + "\". Try a different name.");
 		}
 		
 		ProtectedRegion region = defineRegion(plotWorld, player, regionId, sel, bottomY, topY);
 		
 		boolean enableCost = plotWorld.getConfig().isCreateCostEnabled();
 		boolean bypassCost = !enableCost;
 		if (!bypassCost
 				&& player.hasPermission(Permission.CREATE_BYPASSCOST)) {
 			bypassCost = true;
 		}
 		//-----------------------------------
 		double cost = getWorth(region, plotWorld.getConfig().getBlockWorth());
 		
 		if(!bypassCost) {
 			double bal = mgr.getEconomy().getBalance(player.getName());
 			if(bal < cost) {
 				throw new PlotControlException("You don't have enough money to create a region this big. You have " + mgr.getEconomy().format(bal) + ", but you require " + mgr.getEconomy().format(cost) + ".");
 			}
 		}
 		
 		
 		// who will get the money ?
 		Set<String> depositTo = new HashSet<String>();
 		// who are the default owners in the config ?
 		List<String> ownerList = plotWorld.getConfig().getDefaultOwners();
 		
 		DefaultDomain ownersDomain;
 		
 		if (enableCost) {
 			// cost is enabled
 			ownersDomain = new DefaultDomain();
 			// player will be owner
 			ownersDomain.addPlayer(player.getName());
 			// owners in config will get money, if there are any
 			if (ownerList != null && ownerList.size() > 0) {
 				// owners in config will get money
 				for (String ownerName : ownerList) {
 					depositTo.add(ownerName);
 				}
 			}
 		} else {
 			// cost is not enabled
 			// who will be owner depends on config
 			if (ownerList == null || ownerList.size() < 1) {
 				// no owners in config, owner is sender
 				ownersDomain = new DefaultDomain();
 				ownersDomain.addPlayer(player.getName());
 			} else {
 				// owners are in config
 				// owners from cronfig will be owners
 				ownersDomain = new DefaultDomain();
 				for (Object ownerName : ownerList) {
 					ownersDomain.addPlayer(ownerName.toString().trim());
 				}
 			}
 		}
 		
 		// TODO accept cost and stuff, otherwise don't save... use conversation API
 		region.setOwners(ownersDomain);
 		
 		try {
			mgr.getEconomy().withdraw(player.getName(), cost);
 		} catch (EconomyException e) {
 			throw new PlotControlException("Failed to pay for the region: " + e.getMessage(), e);
 		}
 		
 
		if(depositTo != null && depositTo.size() != 0) {
 			double share = Math.abs(cost) / depositTo.size();
 			for (String account : depositTo) {
 				mgr.getEconomy().deposit(account, share);
 			}
 		}
 		
 		try {
 			regionManager.addRegion(region);
 			regionManager.save();
 		} catch (ProtectionDatabaseException e) {
 			throw new PlotControlException("Failed to save new region with id \"" + region.getId() + "\": " + e.getMessage(), e);
 		}
 		// send region info to indicate it was successful
 		plotWorld.getPlot(regionId).sendInfo(player);
 	}
 	
 	
 	
 	public void redefine(Player player, String regionId) throws PlotControlException {
 		PlotWorld plotWorld = mgr.getPlotWorld(player.getWorld().getName());
 		redefine(player, regionId, plotWorld.getConfig().getDefaultBottomY(), plotWorld.getConfig().getDefaultTopY());
 	}
 	
 	public void redefine(Player player, String regionId, int bottomY, int topY) throws PlotControlException {
 		// get player's selection
 		Selection sel = getSelection(player);
 		// get plot-world information
 		PlotWorld plotWorld = mgr.getPlotWorld(sel.getWorld().getName());
 				
 		RegionManager regionManager = plotWorld.getRegionManager();
 		ProtectedRegion region = regionManager.getRegion(regionId);
 		
 		if(region == null) {
 			throw new PlotControlException("Region \"" + regionId + "\" doesn't exist.");
 		}
 		else if(!region.isOwner(player.getName()) && !player.hasPermission(Permission.REDEFINE_ANYREGION)) {
 			// must be owner
 			throw new PlotControlException("You can only redefine you own regions.");
 		}
 		
 		double blockWorth = plotWorld.getConfig().getBlockWorth();
 		double oldWorth = getWorth(region, blockWorth);
 		int oldWidth = Math.abs(region.getMaximumPoint().getBlockX() - region.getMinimumPoint().getBlockX()) + 1;
         int oldLength = Math.abs(region.getMaximumPoint().getBlockZ() - region.getMinimumPoint().getBlockZ()) + 1;
         int oldHeight = Math.abs(region.getMaximumPoint().getBlockY() - region.getMinimumPoint().getBlockY()) + 1;
         
 		region = defineRegion(plotWorld, player, regionId, sel, bottomY, topY, region);
 		
 		double newWorth = getWorth(region, blockWorth);
 		int newWidth = Math.abs(region.getMaximumPoint().getBlockX() - region.getMinimumPoint().getBlockX()) + 1;
         int newLength = Math.abs(region.getMaximumPoint().getBlockZ() - region.getMinimumPoint().getBlockZ()) + 1;
         int newHeight = Math.abs(region.getMaximumPoint().getBlockY() - region.getMinimumPoint().getBlockY()) + 1;
         
 		
 		boolean enableCost = plotWorld.getConfig().isCreateCostEnabled();
 		boolean bypassCost = !enableCost;
 		if (!bypassCost
 				&& player.hasPermission(Permission.CREATE_BYPASSCOST)) {
 			bypassCost = true;
 		}
 		
 		// TODO pay or refund.. 
 		// TODO accept refund/payment with conversation API, otherwise, don't save
 		
 		try {
 			regionManager.addRegion(region);
 			regionManager.save();
 			
 			mgr.messages.resized(player, 
 					region.getOwners().getPlayers(), 
 					region.getMembers().getPlayers(), 
 					regionId, oldWorth, newWorth, oldWidth, oldLength, oldHeight, newWidth, newLength, newHeight);
 			
 		} catch (ProtectionDatabaseException e) {
 			throw new PlotControlException("Failed to save new region with id \"" + region.getId() + "\": " + e.getMessage(), e);
 		}
 	}
 	
 	public void delete(CommandSender sender, World world, String regionId) throws PlotControlException {
 		PlotWorld plotWorld = mgr.getPlotWorld(world.getName());
 		Plot plot = plotWorld.getPlot(regionId);
 		if(plot == null) {
 			throw new PlotControlException("Region \"" + regionId + "\" doesn't exist.");
 		}
 		
 		if(plotWorld.getConfig().isReserveFreeRegionsEnabled()) {
 			ProtectedRegion region = plot.getRegion();
 			if(region != null) {
 				Set<String> owners = region.getOwners().getPlayers();
 				Set<String> homeless = getPotentialHomeless(world, owners);
 				if(!homeless.isEmpty()) {
 					String homelessString = "";
 					for (String string : homeless) {
 						homelessString += ", " + string;
 					}
 					homelessString = homelessString.substring(2);//remove comma and space
 					throw new PlotControlException("Sorry, you can't delete this region. The following players would become homeless: " + homelessString);
 				}
 			}
 		}
 		
 		
 		// TODO accept delete, with conversation API
 		if(!plot.delete()) {
 			throw new PlotControlException("Failed to delete region \"" + regionId + "\". There might still be players renting that region.");
 		}
 		else {
 			try {
 				RegionManager regionManager = plotWorld.getRegionManager();
 				ProtectedRegion region = plot.getRegion();
 				Set<String> owners = region.getOwners().getPlayers();
 				Set<String> members = region.getMembers().getPlayers();
 				regionManager.removeRegion(regionId);
 				regionManager.save();
 				
 				// TODO refund after delete
 				double refund = 0;
 				mgr.messages.removed(sender, owners, members, regionId, refund);
 				
 			} catch (ProtectionDatabaseException e) {
 				throw new PlotControlException("Failed to delete region with id \"" + regionId + "\": " + e.getMessage(), e);
 			}
 		}
 	}
 	
 	public void sendRegionCount(CommandSender sender, OfflinePlayer owner, World world) {
 		int count = getRegionCountOfPlayer(world, owner.getName());
 
 		String countString = String.valueOf(count);
 		if (count < mgr.getPlotWorld(world.getName()).getConfig().getMaxRegionCount()) {
 			countString = ChatColor.WHITE + countString;
 		} else {
 			countString = ChatColor.RED + countString;
 		}
 		
 		sender.sendMessage(ChatColor.GREEN + "Player " + ChatColor.WHITE + "'"
 				+ owner.getName() + "'" + ChatColor.GREEN + " owns "
 				+ countString + ChatColor.GREEN + " regions in world "
 				+ ChatColor.WHITE + "'" + world.getName() + "'"
 				+ ChatColor.GREEN + ".");
 		
 	}
 	
 	public void sendWorth(CommandSender sender, String regionId, World world) {
 		PlotWorld plotWorld = mgr.getPlotWorld(world.getName());
 		RegionManager regionManager = plotWorld.getRegionManager();
 		ProtectedRegion region = regionManager.getRegion(regionId);
 		if(region == null) {
 			sender.sendMessage(ChatColor.RED + "Region '" + regionId + "' doesn't exist in world '" + world.getName() + "'.");
 			return;
 		}
 		
 		int width = Math.abs(region.getMaximumPoint().getBlockX() - region.getMinimumPoint().getBlockX()) + 1;
 		int length = Math.abs(region.getMaximumPoint().getBlockZ() - region.getMinimumPoint().getBlockZ()) + 1;
 		sender.sendMessage(ChatColor.GREEN + "Region " + ChatColor.WHITE + region.getId() + ChatColor.GREEN + " with a size of "
 				+ ChatColor.WHITE + String.valueOf(width) + "x"
 				+ String.valueOf(length) + ChatColor.GREEN + " blocks, in world \"" + world.getName() + "\" ");
 		double cost = getWorth(width, length, plotWorld.getConfig().getBlockWorth());
 		sender.sendMessage(ChatColor.GREEN + "is worth about "
 				+ ChatColor.WHITE + mgr.getEconomy().format(cost)
 				+ ChatColor.GREEN + ", based on the region's size.");
 	}
 	
 	public void sendWorth(CommandSender sender, int width, int length, World world) {
 		PlotWorld plotWorld = mgr.getPlotWorld(world.getName());
 		double cost = getWorth(width, length, plotWorld.getConfig().getBlockWorth());
 		sender.sendMessage(ChatColor.GREEN + "For a region with a size of "
 				+ ChatColor.WHITE + String.valueOf(width) + "x"
 				+ String.valueOf(length) + ChatColor.GREEN + " blocks, in world \"" + world.getName() + "\" ");
 		sender.sendMessage(ChatColor.GREEN + "you would pay about "
 				+ ChatColor.WHITE + mgr.getEconomy().format(cost)
 				+ ChatColor.GREEN + ".");
 	}
 	
 	public void sendWorth(CommandSender sender, double money, World world) {
 		PlotWorld plotWorld = mgr.getPlotWorld(world.getName());
 		int size = getSizeByWorth(money, plotWorld.getConfig().getBlockWorth());
 		sender.sendMessage(ChatColor.GREEN + "For " + ChatColor.WHITE
 				+ mgr.getEconomy().format(money) + ChatColor.GREEN + ", ");
 		sender.sendMessage(ChatColor.GREEN
 				+ "you can get a region with a size of about "
 				+ ChatColor.WHITE + String.valueOf(size) + "x"
 				+ String.valueOf(size) + ChatColor.GREEN + " blocks, in world \"" + world.getName() + "\".");
 	}
 	
 	
 
 	public static int getSizeByWorth(double money, double blockWorth) {
 		return (int) Math.sqrt(money / blockWorth);
 	}
 	
 	
 	public static double getWorth(ProtectedRegion region, double blockWorth) {
 		if(region == null) {
 			return 0;
 		}
 		
 		int width = region.getMaximumPoint().getBlockX() - region.getMinimumPoint().getBlockX();
 		width = Math.abs(width) + 1;
 		
 		int length = region.getMaximumPoint().getBlockZ() - region.getMinimumPoint().getBlockZ();
 		length = Math.abs(length) + 1;
 		
 		return getWorth(width, length, blockWorth);
 	}
 	
 	public static double getWorth(int width, int length, double blockWorth) {
 		return width * length * blockWorth;
 	}
 	
 	
 	public boolean overlapsUnownedRegion(ProtectedRegion region, World world, Player player) {
 		return mgr.getWorldGuard().getRegionManager(world).overlapsUnownedRegion(region, mgr.getWorldGuard().wrapPlayer(player));
 	}
 	
 	public static boolean isValidRegionName(String regionName) {
 		if (regionName == null || !ProtectedRegion.isValidId(regionName)
 				|| regionName.equalsIgnoreCase("__GLOBAL__")
 				|| regionName.matches("\\d")) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 
 	public ProtectedRegion getAutomaticParentRegion(ProtectedRegion region, World world, Player player) {
 		RegionManager regionManager = mgr.getWorldGuard().getRegionManager(world);
 		LocalPlayer localPlayer = mgr.getWorldGuard().wrapPlayer(player);
 		
 		// get the regions in which the first corner exists
 		ApplicableRegionSet regions = regionManager.getApplicableRegions(region.getMinimumPoint());
 		
 		List<ProtectedRegion> ownedApplicableRegions = new ArrayList<ProtectedRegion>();
 		
 		// find regions that are cuboid, and owned by the player
 		for (ProtectedRegion element : regions) {
 			if(!element.getTypeName().equalsIgnoreCase("cuboid")) {
 				continue;
 			}
 			if(!element.isOwner(localPlayer)) {
 				continue;
 			}
 			// add owned, cuboid, region
 			ownedApplicableRegions.add(element);
 		}
 		
 		// the first corner is not in an owned, cuboid region
 		if(ownedApplicableRegions.size() == 0) {
 			return null;
 		}
 		
 		// like before, get the regions in which the second corner exists
 		regions = regionManager.getApplicableRegions(region.getMaximumPoint());
 		
 		ProtectedRegion automaticParent = null;
 		
 		// see of the first corner is also in one of these regions
 		// and determine which will be the parent
 		for (ProtectedRegion element : regions) {
 			if(ownedApplicableRegions.contains(element)) {
 				// found a region with both corners in it!
 				if(automaticParent == null) {
 					// we didn't find one yet, so this is it for now
 					automaticParent = element;
 				}
 				else {
 					// we already found one, so we need to compare
 					if(element.getPriority() >= automaticParent.getPriority()) {
 						// priority is higher
 						automaticParent = element;
 					}
 					else if(automaticParent.getPriority() == element.getPriority()) {
 						// priorities are equal
 						if(element.volume() <= automaticParent.volume()) {
 							// has less volume
 							automaticParent = element;
 						}
 					}
 					
 				}
 			}
 		}
 		
 		return automaticParent;
 	}
 }
