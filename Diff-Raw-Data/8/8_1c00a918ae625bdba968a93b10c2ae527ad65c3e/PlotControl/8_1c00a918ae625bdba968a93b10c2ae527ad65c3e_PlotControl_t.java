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
 import org.bukkit.conversations.ConversationFactory;
 import org.bukkit.conversations.Prompt;
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
 	
 	private void checkRegionCount(Player player, PlotWorld world) throws PlotControlException {
 
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
 		
 	}
 	
 	public void buy(final Player player) throws PlotControlException {
 		// get targeted sign
 		Sign sign = getTargetSign(player);
 		if(sign == null) {
 			throw new PlotControlException("You're not looking at a wooden sign.");
 		}
 		
 		BlockVector coords = sign.getLocation().toVector().toBlockVector();
 		final PlotWorld plotWorld = mgr.getPlotWorld(player.getWorld().getName());
 		final Plot plot;
 		IPlotSign plotSign = null;
 		
 		try {
 			// try to get the plot-object via wooden sign, 
 			// the sign should probably have the region name on the last 2 lines
 			plot = plotWorld.getPlot(sign);
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
 		final ProtectedRegion region = plot.getRegion();
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
 		
 		checkRegionCount(player, plotWorld);
 		int regionCount = getRegionCountOfPlayer(plotWorld.getWorld(), player.getName());
 		
 		// get region cost
 		final double cost = plot.getSellCost();
 		
 		// 
 		// check if it's a free region, 
 		// and if it's reserved, 
 		// and if player already has a region
 		// 
 		boolean reserve = plotWorld.getConfig().isReserveFreeRegionsEnabled();
 		if(reserve && cost <= 0 && regionCount > 0) {
 			throw new PlotControlException("Free regions are reserved for new players.");
 		}
 		
 		
 		
 		// 
 		// Check if players would become homless after sale.
 		// This is part of preventing cheating with free regions.
 		// 
 		final Set<String> owners = region.getOwners().getPlayers();
 		// get members for later
 		final Set<String> members = region.getMembers().getPlayers();
 		
 		if(reserve) {
 			Set<String> homeless = getPotentialHomeless(plotWorld.getWorld(), owners);
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
 		
 		final boolean bypassCost = player.hasPermission(Permission.BUY_BYPASSCOST);
 		double balance = mgr.getEconomy().getBalance(player.getName());
 		if(!bypassCost && cost > balance) {
 			throw new PlotControlException("You only have "+mgr.getEconomy().format(balance)+". You still require " + mgr.getEconomy().format(cost - balance) + ".");
 		}
 		
 		// create YesNoPrompt
 		YesNoPrompt prompt = new YesNoPrompt() {
 			
 			@Override
 			protected Prompt onYes() {
 				if(!bypassCost) {
 					try {
 						mgr.getEconomy().withdraw(player.getName(), cost);
 					} catch (EconomyException e) {
 						player.sendMessage(ChatColor.RED + e.getMessage());
 						return Prompt.END_OF_CONVERSATION;
 					}
 				}
 				
 
 		        double share = cost;
 		        
 		        // --------------------
 		        // TAX BEGIN
 		        // --------------------
 
 
 		        String taxAccount = plotWorld.getConfig().getTaxAccount();
 		        double percentageTax = plotWorld.getConfig().getTaxPercent();
 		        double percentage = 0;
 		        if(cost >= plotWorld.getConfig().getTaxFromPrice()) {
 		                
 		                percentage = percentageTax * cost / 100;
 		                share -= percentage;
 		                mgr.getEconomy().deposit(taxAccount, percentage);
 		        }
 		        
 		        // --------------------
 		        // TAX END
 		        // --------------------
 				
 				
 				// calc share and pay owners their share
 				share = share / Math.max(1, owners.size());
 				for (String ownerName : owners) {
 					mgr.getEconomy().deposit(ownerName, share);
 				}
 				
 				// remove owners, add buyer as owner
 				DefaultDomain newOwnerDomain = new DefaultDomain();
 				newOwnerDomain.addPlayer(player.getName());
 				region.setOwners(newOwnerDomain);
 				// save region owner changes
 				try {
 					plotWorld.getRegionManager().save();
 				} catch (ProtectionDatabaseException e) {
 					String msg = "Failed to save region changes to world \"" + plotWorld.getName() + "\", using WorldGuard.";
 					mgr.getPlugin().getLogger().log(Level.WARNING, ChatColor.RED + msg, e);
 				}
 
 				// break all for sale signs
 				Collection<IPlotSignData> forSaleSigns = plot.getSigns(PlotSignType.FOR_SALE);
 				for (IPlotSignData data : forSaleSigns) {
 					BlockVector vec = data.getBlockVector();
 					plot.removeSign(vec);
 				}
 				
 				
 				
 				// delete plot-info if possible, otherwise just save changes
 				// (a plot can't be deleted when there's still active renters)
 				if(!plot.delete()) {
 					plot.save();
 				}
 				mgr.messages.bought(plot.getRegionId(), player, cost, owners, members, share, taxAccount, percentage);
 				return Prompt.END_OF_CONVERSATION;
 			}
 			
 			@Override
 			protected Prompt onNo() {
 				player.sendMessage(ChatColor.RED + "Did not buy region " + plot.getRegionId() + ".");
 				return Prompt.END_OF_CONVERSATION;
 			}
 		};
 
 		// ask the question
 		if(bypassCost) {
 			player.sendMessage(
 					ChatColor.GREEN + "You have permission to skip payment. "
 							+ "The owners still receive money.");
 		}
 		player.sendMessage(
 				ChatColor.GREEN + "Are you sure you want to buy region " 
 						+ ChatColor.WHITE + region.getId() 
 						+ ChatColor.GREEN + " for " + ChatColor.WHITE 
 						+ mgr.getEconomy().format(cost) + ChatColor.GREEN + "?");
 		// prompt for yes or no
 		new ConversationFactory(mgr.getPlugin())
 		.withFirstPrompt(prompt)
 		.withLocalEcho(false)
 		.withModality(false)
 		.buildConversation(player)
 		.begin();
 		
 	}
 	
 	public void rent(final Player player) throws PlotControlException {
 		// get targeted sign
 		Sign sign = getTargetSign(player);
 		if(sign == null) {
 			throw new PlotControlException("You're not looking at a wooden sign.");
 		}
 		
 		BlockVector coords = sign.getLocation().toVector().toBlockVector();
 		final PlotWorld plotWorld = mgr.getPlotWorld(player.getWorld().getName());
 		final Plot plot;
 		IPlotSign plotSign = null;
 		
 		try {
 			// try to get the plot-object via wooden sign, 
 			// the sign should probably have the region name on the last 2 lines
 			plot = plotWorld.getPlot(sign);
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
 		
 		if(plotSign.getType() != PlotSignType.FOR_RENT) {
 			// plot-sign is not a for-rent sign
 			throw new PlotControlException("You're not looking at a for-rent sign.");
 		}
 		
 		// get ProtectedRegion
 		final ProtectedRegion region = plot.getRegion();
 		if(region == null) {
 			throw new PlotControlException("Sorry, the region doesn't exist anymore.");
 		}
 
 		// not for rent?
 		if(!plot.isForRent()) {
 			throw new PlotControlException("Sorry, region \"" + plot.getRegionId() + "\" isn't for rent. This is probably an old sign.");
 		}
 
 		// already owner?
 		if(region.isMember(player.getName())) {
 			throw new PlotControlException("You're already member of this region.");
 		}
 		
 		// get owners for later
 		final Set<String> owners = region.getOwners().getPlayers();
 		// get members for later
 		final Set<String> members = region.getMembers().getPlayers();
 		
 		
 		// TODO get rent cost, and time
 		// get rent cost
 		final double cost = plot.getRentCost();
 		final String timeString = null;
 
 		
 		// check bypasscost || pay for region
 		
 		final boolean bypassCost = player.hasPermission(Permission.RENT_BYPASSCOST);
 		double balance = mgr.getEconomy().getBalance(player.getName());
 		if(!bypassCost && cost > balance) {
 			throw new PlotControlException("You only have "+mgr.getEconomy().format(balance)+". You still require " + mgr.getEconomy().format(cost - balance) + ".");
 		}
 		
 		// create YesNoPrompt
 		YesNoPrompt prompt = new YesNoPrompt() {
 			
 			@Override
 			protected Prompt onYes() {
 				if(!bypassCost) {
 					try {
 						mgr.getEconomy().withdraw(player.getName(), cost);
 					} catch (EconomyException e) {
 						player.sendMessage(ChatColor.RED + e.getMessage());
 						return Prompt.END_OF_CONVERSATION;
 					}
 				}
 				
 
 		        double share = cost;
 		        
 		        
 		        // no tax for renting out
 				
 				// calc share and pay owners their share
 				share = share / Math.max(1, owners.size());
 				for (String ownerName : owners) {
 					mgr.getEconomy().deposit(ownerName, share);
 				}
 				
 				// add renter as member
 				region.getOwners().addPlayer(player.getName());
 				
 				// save region owner changes
 				try {
 					plotWorld.getRegionManager().save();
 				} catch (ProtectionDatabaseException e) {
 					String msg = "Failed to save region changes to world \"" + plotWorld.getName() + "\", using WorldGuard.";
 					mgr.getPlugin().getLogger().log(Level.WARNING, ChatColor.RED + msg, e);
 				}
 				
 				// TODO put player's name on the sign...
 				// TODO put rent time on the sign. and update every minute using a timer
 				// timer will be for every plot that has renters.
 				// TODO restart timers when the server restarts
 				
 				mgr.messages.rented(player, owners, members, plot.getRegionId(), cost, timeString);
 				return Prompt.END_OF_CONVERSATION;
 			}
 			
 			@Override
 			protected Prompt onNo() {
				player.sendMessage(ChatColor.RED + "Did not rent region " + plot.getRegionId() + ".");
 				return Prompt.END_OF_CONVERSATION;
 			}
 		};
 
 		// ask the question
 		if(bypassCost) {
 			player.sendMessage(
 					ChatColor.GREEN + "You have permission to skip payment. "
 							+ "The owners still receive money.");
 		}
 		player.sendMessage(
				ChatColor.GREEN + "Are you sure you want to rent region " 
 						+ ChatColor.WHITE + region.getId() 
 						+ ChatColor.GREEN + " for " + ChatColor.WHITE 
						+ mgr.getEconomy().format(cost) + ChatColor.GREEN + "?");// TODO add time to message
 		// prompt for yes or no
 		new ConversationFactory(mgr.getPlugin())
 		.withFirstPrompt(prompt)
 		.withLocalEcho(false)
 		.withModality(false)
 		.buildConversation(player)
 		.begin();
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
 			if(ty > maxY) {
 				throw new PlotBoundsException(
 						PlotBoundsException.Type.SELECTION_TOO_HIGH, 
 						ty, by, minY, maxY);
 			}
 			// check minY
 			if(by < minY) {
 				throw new PlotBoundsException(
 						PlotBoundsException.Type.SELECTION_TOO_LOW, 
 						ty, by, minY, maxY);
 			}
 		}
 		
 		
 		
 		
 		
 		
 		Location min = sel.getMinimumPoint();
 		Location max = sel.getMaximumPoint();
 		// create protected region
 		ProtectedCuboidRegion region = new ProtectedCuboidRegion(
 				regionId, 
 				new com.sk89q.worldedit.BlockVector(min.getBlockX(), by, min.getBlockZ()), 
 				new com.sk89q.worldedit.BlockVector(max.getBlockX(), ty, max.getBlockZ()));
 		
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
 	
 	public void define(final Player player, final String regionId, int bottomY, int topY) throws PlotControlException {
 		/*
 		 * exists already? invalid name? can't afford?
 		 * call method defineRegion
 		 * set region owner(s) to player or default
 		 * player pays money
 		 * save region
 		 * send info
 		 */
 		// get player's selection
 		Selection sel = getSelection(player);
 		// get plot-world information
 		final PlotWorld plotWorld = mgr.getPlotWorld(sel.getWorld().getName());
 		
 		
 		
 		// get world's RegionManager of WorldGuard
 		final RegionManager regionManager = plotWorld.getRegionManager();
 		
 		// check region existance
 		if(regionManager.hasRegion(regionId)) {
 			throw new PlotControlException("Region \"" + regionId + "\" already exists.");
 		}
 		// check if valid region name, just like WorldGuard
 		if(!isValidRegionName(regionId)) {
 			throw new PlotControlException("Invalid region name \"" + regionId + "\". Try a different name.");
 		}
 		
 		// create region
 		final ProtectedRegion region = defineRegion(plotWorld, player, regionId, sel, bottomY, topY);
 		// cost must be configured and bypass not permitted
 		final boolean enableCost = plotWorld.getConfig().isCreateCostEnabled() && !player.hasPermission(Permission.CREATE_BYPASSCOST);
 		// calculate cost
 		final double cost = getWorth(region, plotWorld.getConfig().getBlockWorth());
 		// check balance
 		double balance = mgr.getEconomy().getBalance(player.getName());
 		if(enableCost && balance < cost) {
 			throw new PlotControlException(ChatColor.RED + "You can't afford to create region " + ChatColor.WHITE + regionId + ChatColor.RED + ". You only have " + ChatColor.WHITE + mgr.getEconomy().format(balance) + ChatColor.RED + ", but it costs " + ChatColor.WHITE + mgr.getEconomy().format(cost) + ChatColor.RED + ".");
 		}
 		
 		// get default owners from config
 		List<String> ownerList = plotWorld.getConfig().getDefaultOwners();
 		// who will be the region owner?
 		final DefaultDomain ownersDomain = new DefaultDomain();
 		// let's create the prompt first
 		
 		// 
 		// create the YesNoPrompt
 		// we override onYes and onNo
 		// 
 		YesNoPrompt prompt = new YesNoPrompt() {
 			
 			@Override
 			protected Prompt onYes() {
 				// set region's owners
 				region.setOwners(ownersDomain);
 				
 				// pay money
 				if(enableCost) {
 					try {
 						mgr.getEconomy().withdraw(player.getName(), cost);
 					} catch (EconomyException e) {
 						player.sendMessage(ChatColor.RED + "Failed to pay for the region: " + e.getMessage());
 						return Prompt.END_OF_CONVERSATION;
 					}
 				}
 				
 				//save
 				try {
 					regionManager.addRegion(region);
 					regionManager.save();
 				} catch (ProtectionDatabaseException e) {
 					player.sendMessage(ChatColor.RED + "Failed to save new region with id \"" + region.getId() + "\": " + e.getMessage());
 					return Prompt.END_OF_CONVERSATION;
 				}
 				// send region info to indicate it was successful
 				plotWorld.getPlot(regionId).sendInfo(player);
 				return Prompt.END_OF_CONVERSATION;
 			}
 			
 			@Override
 			protected Prompt onNo() {
 				player.sendMessage(ChatColor.RED + "Did not create a region.");
 				return Prompt.END_OF_CONVERSATION;
 			}
 		};
 		
 		// 
 		// add owners, and 
 		// run YesNoPrompt
 		// 
 		if (enableCost) {
 			// cost is enabled, player will be owner
 			checkRegionCount(player, plotWorld);
 			ownersDomain.addPlayer(player.getName());
 			// ask question
 			player.sendMessage(ChatColor.GREEN + "Are you sure you want to pay " + ChatColor.WHITE + mgr.getEconomy().format(cost) + ChatColor.GREEN + ", ");
 			player.sendMessage(ChatColor.GREEN + "and create region '" + ChatColor.WHITE + region.getId() + ChatColor.GREEN + "?");
 			
 			// run YesNoPrompt
 			new ConversationFactory(mgr.getPlugin())
 			.withLocalEcho(false)
 			.withModality(false)
 			.withFirstPrompt(prompt)
 			.buildConversation(player)
 			.begin();
 			
 		} else {
 			// cost is not enabled
 			// who will be owner depends on config
 			if (ownerList == null || ownerList.size() < 1) {
 				// no owners in config, owner is player
 				checkRegionCount(player, plotWorld);
 				ownersDomain.addPlayer(player.getName());
 			} else {
 				// owners are in config
 				// owners from cronfig will be owners
 				for (Object ownerName : ownerList) {
 					ownersDomain.addPlayer(ownerName.toString().trim());
 				}
 			}
 			// save
 			prompt.onYes();
 		}
 	}
 	
 	
 	
 	public void redefine(Player player, String regionId) throws PlotControlException {
 		PlotWorld plotWorld = mgr.getPlotWorld(player.getWorld().getName());
 		redefine(player, regionId, plotWorld.getConfig().getDefaultBottomY(), plotWorld.getConfig().getDefaultTopY());
 	}
 	
 	public void redefine(final Player player, final String regionId, int bottomY, int topY) throws PlotControlException {
 		/*
 		 * doesn't exist? different owner?
 		 * store old size, etc
 		 * call method defineRegion
 		 * calculate cost/refund
 		 * costs player if larger, refunds owners if smaller
 		 */
 		
 		// get player's selection
 		Selection sel = getSelection(player);
 		// get plot-world information
 		PlotWorld plotWorld = mgr.getPlotWorld(sel.getWorld().getName());
 		
 		final RegionManager regionManager = plotWorld.getRegionManager();
 		ProtectedRegion region = regionManager.getRegion(regionId);
 		
 		if(region == null) {
 			throw new PlotControlException("Region \"" + regionId + "\" doesn't exist.");
 		}
 		else if(!region.isOwner(player.getName()) && !player.hasPermission(Permission.REDEFINE_ANYREGION)) {
 			// must be owner
 			throw new PlotControlException("You can only redefine your own regions.");
 		}
 		
 		// get old values
 		double blockWorth = plotWorld.getConfig().getBlockWorth();
 		final double oldWorth = getWorth(region, blockWorth);
 		final int oldWidth = Math.abs(region.getMaximumPoint().getBlockX() - region.getMinimumPoint().getBlockX()) + 1;
 		final int oldLength = Math.abs(region.getMaximumPoint().getBlockZ() - region.getMinimumPoint().getBlockZ()) + 1;
 		final int oldHeight = Math.abs(region.getMaximumPoint().getBlockY() - region.getMinimumPoint().getBlockY()) + 1;
         
 		// redefine region
 		final ProtectedRegion regionNew = defineRegion(plotWorld, player, regionId, sel, bottomY, topY, region);
 		
 		// get new values
 		final double newWorth = getWorth(regionNew, blockWorth);
 		final int newWidth = Math.abs(regionNew.getMaximumPoint().getBlockX() - regionNew.getMinimumPoint().getBlockX()) + 1;
 		final int newLength = Math.abs(regionNew.getMaximumPoint().getBlockZ() - regionNew.getMinimumPoint().getBlockZ()) + 1;
 		final int newHeight = Math.abs(regionNew.getMaximumPoint().getBlockY() - regionNew.getMinimumPoint().getBlockY()) + 1;
         
 
 		// calculate cost. refund if < 0
 		final double cost = newWorth - oldWorth;
 		// get owners
 		final Set<String> ownerList = region.getOwners().getPlayers();
 		
 		// cost must be configured and bypass must not be permitted
 		final boolean enableCost = plotWorld.getConfig().isCreateCostEnabled() 
 				&& cost != 0 
 				&& !player.hasPermission(Permission.CREATE_BYPASSCOST);
 		
 		// 
 		// Ask the question
 		// 
 		if(cost > 0) {
 			// larger region
 			
             if(enableCost) {
             	// check balance
             	double balance = mgr.getEconomy().getBalance(player.getName()); 
             	if(balance < cost) {
             		throw new PlotControlException(ChatColor.RED + "You can't afford to resize region " + ChatColor.WHITE + regionId + ChatColor.RED + "' from " + ChatColor.RED + oldWidth + "x" + oldLength + "x" + oldHeight + ChatColor.RED + " to " + ChatColor.WHITE + newWidth + "x" + newLength + "x" + newHeight + ChatColor.RED + ". You only have " + ChatColor.WHITE + mgr.getEconomy().format(balance) + ChatColor.RED + ", but it costs " + ChatColor.WHITE + mgr.getEconomy().format(cost) + ChatColor.RED + ".");
             	}
             	// send cost info 
             	player.sendMessage(ChatColor.GREEN + "Are you sure you want to pay " + ChatColor.WHITE + mgr.getEconomy().format(cost) + ChatColor.GREEN + " and ");
             }
             else {
             	// no cost
             	player.sendMessage(ChatColor.GREEN + "Are you sure you want to ");
             }
             // ... the rest of the message
             player.sendMessage(ChatColor.GREEN + "resize region '" + ChatColor.WHITE + region.getId() + ChatColor.GREEN + "' from " + ChatColor.WHITE + oldWidth + "x" + oldLength + "x" + oldHeight + ChatColor.GREEN + " to " + ChatColor.WHITE + newWidth + "x" + newLength + "x" + newHeight + ChatColor.GREEN + "?");
 	    }
 	    else {
 	    	// smaller region
 	    	player.sendMessage(ChatColor.GREEN + "Are you sure you want to resize region '" + ChatColor.WHITE + region.getId() + ChatColor.GREEN + "' from " + ChatColor.WHITE + oldWidth + "x" + oldLength + "x" + oldHeight + ChatColor.GREEN + " to " + ChatColor.WHITE + newWidth + "x" + newLength + "x" + newHeight + ChatColor.GREEN + "?");
 	    	
 	    	if(enableCost) {
 		    	// get comma seperated string of owner names
 	    		// like: bob, john, hank
 	            String ownerNames = "";
 	            for (String name : ownerList) {
 	                    ownerNames += ", " + name;
 	            }
 	            if(ownerNames.isEmpty()) {
 	                    ownerNames = "nobody";
 	            }
 	            else {
 	                    ownerNames = ownerNames.substring(2);
 	            }
 	            
 	            // send info about refund
             	player.sendMessage(ChatColor.GREEN + "The refund of " + ChatColor.WHITE + mgr.getEconomy().format(Math.abs(cost)) + ChatColor.GREEN + " will be shared");
                 player.sendMessage(ChatColor.GREEN + "amongst " + ChatColor.WHITE + ownerNames);
             }
 	    }
 
 		// 
 		// create YesNoPrompt object
 		// we override onYes and onNo
 		// 
 		YesNoPrompt prompt = new YesNoPrompt() {
 
 			@Override
 			protected Prompt onYes() {
 				if (enableCost) {
 					try {
 						if (cost > 0) {
 							// larger region, cost money
 							mgr.getEconomy().withdraw(player.getName(),
 									Math.abs(cost));
 						} else {
 							// smaller region, refunds money to the owners
 							
 							// TODO should this refund money to the player instead?? 
 							// if so, the messages should be edited
 							
 							// calculate share
 							double share = Math.abs(cost) / Math.max(1, ownerList.size());
 							// refund equal share to owners
 							for (String name : ownerList) {
 								mgr.getEconomy().deposit(name, share);
 							}
 							
 						}
 					} catch (EconomyException e) {
 						// don't save
 						player.sendMessage(ChatColor.RED + e.getMessage());
 						return Prompt.END_OF_CONVERSATION;
 					}
 
 				}
 				try {
 					//save
 					regionManager.addRegion(regionNew);
 					regionManager.save();
 					
 					// send info to the player and owners and members
 					mgr.messages.resized(player, 
 							regionNew.getOwners().getPlayers(), 
 							regionNew.getMembers().getPlayers(),
 							regionId, 
 							(enableCost ? oldWorth : 0), 
 							(enableCost ? newWorth : 0), 
 							oldWidth, oldLength, oldHeight, 
 							newWidth, newLength, newHeight);
 
 				} catch (ProtectionDatabaseException e) {
 					// i think your server has bigger problems
 					String msg = ChatColor.RED
 							+ "Failed to save new region with id \""
 							+ regionNew.getId() + "\": " + e.getMessage();
 					player.sendMessage(msg);
 					mgr.getPlugin().getLogger().log(Level.WARNING, msg, e);
 				}
 
 				return Prompt.END_OF_CONVERSATION;
 			}
 
 			@Override
 			protected Prompt onNo() {
 				player.sendMessage(ChatColor.RED
 						+ "Did not resize the region.");
 				return Prompt.END_OF_CONVERSATION;
 			}
 		};
 
 		// 
 		// run YesNoPrompt
 		// 
 		new ConversationFactory(mgr.getPlugin())
 		.withLocalEcho(false)
 		.withModality(false)
 		.withFirstPrompt(prompt)
 		.buildConversation(player)
 		.begin();
 		
 	}
 	
 	public void delete(final CommandSender player, World world, String regionId) throws PlotControlException {
 		final PlotWorld plotWorld = mgr.getPlotWorld(world.getName());
 		// doesn't exist?
 		final Plot plot = plotWorld.getPlot(regionId);
 		if(plot == null) {
 			throw new PlotControlException("Region \"" + regionId + "\" doesn't exist.");
 		}
 		
 		final ProtectedRegion region = plot.getRegion();
 		
 		if(plotWorld.getConfig().isReserveFreeRegionsEnabled()) {
 			
 			// Can't allow players to become homeless when 
 			// there are free regions reserved for the homeless!
 			// Because they would be able to get a free region, delete it,
 			// get another free region, delete it.. etc
 			
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
 		
 		
 		final boolean costEnabled = plotWorld.getConfig().isCreateCostEnabled() && !player.hasPermission(Permission.CREATE_BYPASSCOST);
 		
 		
 
 		// TODO check if there are still renters
 		
 		Set<String> ownerList;
 		if(region != null) {
 			ownerList = region.getOwners().getPlayers();
 		}
 		else {
 			// avoid null pointer errors
 			ownerList = new HashSet<String>();
 		}
 		final double refund;
 		final double share;
 		if(costEnabled) {
 			// calculate percentage of total worth
 			refund  = plotWorld.getConfig().getDeleteRefundPercent() * plot.getWorth() / 100;
 			// calculate how much each owner gets
 			share = refund / Math.max(1, ownerList.size());
 			
 			// console will not get this message
 			if(player instanceof Player) {
 				String nameString = "";
 				for (String name : ownerList) {
 					nameString += ", " + name;
 				}
 				if(!nameString.isEmpty()) {
 					nameString = nameString.substring(2);
 				}
 				else {
 					nameString = ChatColor.RED + "nobody";
 				}
 				
 				// send refund question message
 				player.sendMessage(
 						ChatColor.GREEN + "Are you sure you want to delete region '" 
 						+ ChatColor.WHITE + regionId 
 						+ ChatColor.GREEN + "' and share the refund of " 
 						+ ChatColor.WHITE + mgr.getEconomy().format(refund) 
 						+ ChatColor.GREEN + " amongst '" 
 						+ ChatColor.WHITE + nameString 
 						+ ChatColor.GREEN + "'?");
 			}
 			
 		}
 		else {
 			// setting share to zero, otherwise the final variable will give a warning
 			share=0;
 			refund=0;
 			
 			// console will not get this message
 			if(player instanceof Player) {
 				// send normal message
 				player.sendMessage(
 						ChatColor.GREEN + "Are you sure you want to delete region '" 
 						+ ChatColor.WHITE + regionId
 						+ ChatColor.GREEN + "'?");				
 			}
 			
 			
 		}
 		
 		// 
 		// create YesNoPrompt object
 		// 
 		YesNoPrompt prompt = new YesNoPrompt() {
 			
 			@Override
 			protected Prompt onYes() {
 				if(!plot.delete()) {
 					player.sendMessage(ChatColor.RED + "Failed to delete region \"" + plot.getRegionId() + "\". There might still be players renting that region.");
 				}
 				else {
 					try {
 						RegionManager regionManager = plotWorld.getRegionManager();
 						Set<String> owners;
 						Set<String> members;
 						if(region != null) {
 							regionManager.removeRegion(plot.getRegionId());
 							regionManager.save();
 							
 							owners = region.getOwners().getPlayers();
 							members = region.getMembers().getPlayers();
 						}
 						else {
 							// avoid null pointer errors
 							owners = new HashSet<String>();
 							members = new HashSet<String>();
 						}
 						
 						
 						// break all for sale signs
 						Collection<IPlotSignData> forSaleSigns = plot.getSigns(PlotSignType.FOR_SALE);
 						for (IPlotSignData data : forSaleSigns) {
 							BlockVector vec = data.getBlockVector();
 							plot.removeSign(vec);
 						}
 
 						// refund, now we know it's deleted
 						if(costEnabled) {
 							for (String name : owners) {
 								mgr.getEconomy().deposit(name, share);
 							}
 						}
 						// send messages to everyone involved
 						mgr.messages.removed(player, owners, members, plot.getRegionId(), refund);
 						
 					} catch (ProtectionDatabaseException e) {
 						player.sendMessage(ChatColor.RED + "Failed to delete region with id \"" + plot.getRegionId() + "\": " + e.getMessage());
 					}
 				}
 				return Prompt.END_OF_CONVERSATION;
 			}
 			
 			@Override
 			protected Prompt onNo() {
 				player.sendMessage(ChatColor.RED + "Region '" + plot.getRegionId() + "' was not deleted.");
 				return Prompt.END_OF_CONVERSATION;
 			}
 		};
 		
 
 		if(!(player instanceof Player)) {
 			prompt.onYes();
 			return;
 		}
 		
 		// run YesNoPrompt
 		new ConversationFactory(mgr.getPlugin())
 		.withFirstPrompt(prompt)
 		.withLocalEcho(false)
 		.withModality(false)
 		.buildConversation((Player)player)
 		.begin();
 		
 		
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
