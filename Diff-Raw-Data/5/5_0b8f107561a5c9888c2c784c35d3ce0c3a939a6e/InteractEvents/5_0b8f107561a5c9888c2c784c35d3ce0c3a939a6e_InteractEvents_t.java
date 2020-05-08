 package no.runsafe.creativetoolbox.events;
 
 import no.runsafe.creativetoolbox.PlotFilter;
 import no.runsafe.creativetoolbox.database.ApprovedPlotRepository;
 import no.runsafe.creativetoolbox.database.PlotApproval;
 import no.runsafe.framework.configuration.IConfiguration;
 import no.runsafe.framework.event.IAsyncEvent;
 import no.runsafe.framework.event.IConfigurationChanged;
 import no.runsafe.framework.event.player.IPlayerInteractEntityEvent;
 import no.runsafe.framework.event.player.IPlayerRightClickBlock;
 import no.runsafe.framework.server.RunsafeLocation;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.block.RunsafeBlock;
 import no.runsafe.framework.server.event.player.RunsafePlayerInteractEntityEvent;
 import no.runsafe.framework.server.item.RunsafeItemStack;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import no.runsafe.worldguardbridge.WorldGuardInterface;
 
 import java.util.List;
 import java.util.Set;
 
 public class InteractEvents implements IPlayerRightClickBlock, IPlayerInteractEntityEvent, IConfigurationChanged, IAsyncEvent
 {
 	public InteractEvents(
 		PlotFilter plotFilter,
 		WorldGuardInterface worldGuard,
 		ApprovedPlotRepository plotRepository
 	)
 	{
 		this.worldGuardInterface = worldGuard;
 		this.plotFilter = plotFilter;
 		this.plotRepository = plotRepository;
 	}
 
 	@Override
 	public boolean OnPlayerRightClick(RunsafePlayer player, RunsafeItemStack itemInHand, RunsafeBlock block)
 	{
 		if (itemInHand != null && itemInHand.getItemId() == listItem)
 		{
 			this.listPlotsByLocation(block.getLocation(), player);
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public void OnPlayerInteractEntityEvent(RunsafePlayerInteractEntityEvent event)
 	{
 		if (event.getRightClicked() instanceof RunsafePlayer && event.getPlayer().hasPermission("runsafe.creative.list"))
 		{
 			if (event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getItemId() == listItem)
 			{
 				this.listPlotsByPlayer((RunsafePlayer) event.getRightClicked(), event.getPlayer());
 				event.setCancelled(true);
 			}
 		}
 	}
 
 	@Override
 	public void OnConfigurationChanged(IConfiguration configuration)
 	{
 		listItem = configuration.getConfigValueAsInt("list_item");
 	}
 
 	private void listPlotsByPlayer(RunsafePlayer checkPlayer, RunsafePlayer triggerPlayer)
 	{
 		if (!this.worldGuardInterface.serverHasWorldGuard())
 		{
 			triggerPlayer.sendMessage("Error: No WorldGuard installed.");
 			return;
 		}
 
 		List<String> regions = plotFilter.apply(worldGuardInterface.getOwnedRegions(checkPlayer, checkPlayer.getWorld()));
 
 		if (!regions.isEmpty())
 			for (String regionName : regions)
 				this.listRegion(regionName, triggerPlayer, true);
 		else if (triggerPlayer.hasPermission("runsafe.creative.list.showname"))
 			triggerPlayer.sendMessage(String.format("%s does not own any regions.", checkPlayer.getPrettyName()));
 		else
 			triggerPlayer.sendMessage("No regions owned by this player.");
 	}
 
 	private void listPlotsByLocation(RunsafeLocation location, RunsafePlayer player)
 	{
 		if (!this.worldGuardInterface.serverHasWorldGuard())
 		{
 			player.sendMessage("Error: No WorldGuard installed.");
 			return;
 		}
 
 		List<String> regions = plotFilter.apply(worldGuardInterface.getRegionsAtLocation(location));
 
 		if (regions != null && !regions.isEmpty())
 			for (String regionName : regions)
 				this.listRegion(regionName, player, false);
 		else
 			player.sendMessage("No regions found at this point.");
 	}
 
 	private void listRegion(String regionName, RunsafePlayer player, Boolean simple)
 	{
 		if (player.hasPermission("runsafe.creative.approval.read"))
 		{
 			PlotApproval approval = plotRepository.get(regionName);
 			if (approval == null || approval.getApproved() == null)
 				player.sendColouredMessage("Region: " + regionName);
 			else
 				player.sendColouredMessage("Region: %s [Approved %s]", regionName, approval.getApproved());
 		}
 		else
 			player.sendMessage("Region: " + regionName);
 
 		if (!simple)
 		{
 			Set<String> owners = worldGuardInterface.getOwners(player.getWorld(), regionName);
 			Set<String> members = worldGuardInterface.getMembers(player.getWorld(), regionName);
 
 			for (String owner : owners)
 			{
 				RunsafePlayer theOwner = RunsafeServer.Instance.getPlayer(owner);
 				player.sendColouredMessage("     Owner: " + theOwner.getPrettyName());
 
 				if (player.hasPermission("runsafe.creative.list.seen"))
				{
					String seen = theOwner.getLastSeen(player);
					player.sendColouredMessage("     " + (seen == null ? "Player never seen" : seen));
				}
 			}
 
 			for (String member : members)
 				player.sendColouredMessage("     Member: " + RunsafeServer.Instance.getPlayer(member).getPrettyName());
 		}
 	}
 
 	private final WorldGuardInterface worldGuardInterface;
 	private int listItem;
 	private final PlotFilter plotFilter;
 	private final ApprovedPlotRepository plotRepository;
 }
