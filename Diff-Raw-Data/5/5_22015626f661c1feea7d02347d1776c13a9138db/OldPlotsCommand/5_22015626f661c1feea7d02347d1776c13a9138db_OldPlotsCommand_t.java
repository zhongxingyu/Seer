 package no.runsafe.creativetoolbox.command;
 
 import no.runsafe.PlayerData;
 import no.runsafe.PlayerDatabase;
 import no.runsafe.creativetoolbox.PlotFilter;
 import no.runsafe.creativetoolbox.database.ApprovedPlotRepository;
 import no.runsafe.framework.RunsafePlugin;
 import no.runsafe.framework.command.RunsafeAsyncCommand;
 import no.runsafe.framework.configuration.IConfiguration;
 import no.runsafe.framework.event.IConfigurationChanged;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.RunsafeWorld;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import no.runsafe.framework.timer.IScheduler;
 import no.runsafe.worldguardbridge.WorldGuardInterface;
 
 import java.util.*;
 
 public class OldPlotsCommand extends RunsafeAsyncCommand implements IConfigurationChanged
 {
 	public OldPlotsCommand(
 		ApprovedPlotRepository approvalRepository,
 		IConfiguration config,
 		PlotFilter filter,
 		WorldGuardInterface worldGuardInterface,
 		IScheduler scheduler
 	)
 	{
 		super("oldplots", scheduler);
 		repository = approvalRepository;
 		this.config = config;
 		plotFilter = filter;
 		worldGuard = worldGuardInterface;
 	}
 
 	@Override
 	public String requiredPermission()
 	{
 		return "runsafe.creative.scan.old-plots";
 	}
 
 	@Override
 	public String OnExecute(RunsafePlayer executor, String[] args)
 	{
 		if (!worldGuard.serverHasWorldGuard())
 			return "Unable to find WorldGuard!";
 
 		StringBuilder result = new StringBuilder();
 		Date now = new Date();
 		int count = 0;
 		ArrayList<String> banned = new ArrayList<String>();
 		List<String> approved;
 		approved = repository.getApprovedPlots();
 
 		HashMap<String, Long> seen = new HashMap<String, Long>();
 
 		if (!worldGuard.serverHasWorldGuard())
 			return "WorldGuard isn't active on the server.";
 
 		PlayerDatabase players = RunsafePlugin.Instances.get("RunsafeServices").getComponent(PlayerDatabase.class);
 		Map<String, Set<String>> checkList = worldGuard.getAllRegionsWithOwnersInWorld(getWorld());
 		long oldAfter = config.getConfigValueAsInt("old_after") * 1000;
 		int limit = config.getConfigValueAsInt("max_listed");
 
 		for (String region : plotFilter.apply(new ArrayList<String>(checkList.keySet())))
 		{
 			String info = null;
 			if (approved.contains(region))
 				continue;
 
 			boolean ok = false;
 			for (String owner : checkList.get(region))
 			{
 				owner = owner.toLowerCase();
 				if (!seen.containsKey(owner))
 				{
 					RunsafePlayer player = RunsafeServer.Instance.getPlayer(owner);
 					if (player.isOnline())
 					{
 						ok = true;
 						seen.put(owner, (long) 0);
 						break;
 					}
 					else
 					{
 						PlayerData data = players.get(owner);
 						if (data != null && data.getBanned() != null)
 							banned.add(owner);
 						if (data == null || (data.getLogin() == null && data.getLogout() == null))
 							seen.put(owner, null);
 						else if (data.getLogout() != null)
 							seen.put(owner, now.getTime() - data.getLogout().getTime());
 						else if (data.getLogin() != null)
 							seen.put(owner, now.getTime() - data.getLogin().getTime());
 					}
 				}
 
 				if (banned.contains(owner))
 				{
 					ok = false;
 					info = "banned";
 					break;
 				}
 
 				if (seen.get(owner) == null)
 					continue;
 
 				if (seen.get(owner) < oldAfter)
 				{
 					ok = true;
 				}
 				else
 					info = String.format("%.2f days", seen.get(owner) / 86400000.0);
 			}
 			if (!ok)
 			{
				if (count++ > limit && executor != null)
 				{
 					result.append(String.format("== configured limit reached =="));
 					break;
 				}
 				result.append(String.format("%s (%s)\n", region, info));
 			}
 		}
 		if (result.length() == 0)
 			return "No old plots found.";
		else if (count <= limit || executor == null)
 			result.append(String.format("%d plots found", count));
 		return result.toString();
 	}
 
 	@Override
 	public void OnConfigurationChanged()
 	{
 		world = RunsafeServer.Instance.getWorld(config.getConfigValueAsString("world"));
 	}
 
 	public RunsafeWorld getWorld()
 	{
 		if (world == null)
 			world = RunsafeServer.Instance.getWorld(config.getConfigValueAsString("world"));
 		return world;
 	}
 
 	private final ApprovedPlotRepository repository;
 	private final IConfiguration config;
 	private final PlotFilter plotFilter;
 	private RunsafeWorld world;
 	private final WorldGuardInterface worldGuard;
 }
