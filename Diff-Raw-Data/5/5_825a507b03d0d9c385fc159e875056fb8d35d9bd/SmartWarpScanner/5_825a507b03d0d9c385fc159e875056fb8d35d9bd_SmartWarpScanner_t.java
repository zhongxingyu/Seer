 package no.runsafe.warpdrive;
 
 import no.runsafe.framework.event.IPluginEnabled;
 import no.runsafe.framework.output.IOutput;
 import no.runsafe.framework.server.RunsafeLocation;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.RunsafeWorld;
 import no.runsafe.framework.timer.ForegroundWorker;
 import no.runsafe.framework.timer.IScheduler;
 import no.runsafe.warpdrive.database.SmartWarpChunkRepository;
 import no.runsafe.warpdrive.database.SmartWarpRepository;
 import org.bukkit.World;
 
 import java.util.HashMap;
 
 public class SmartWarpScanner extends ForegroundWorker<String, RunsafeLocation> implements IPluginEnabled
 {
 	public SmartWarpScanner(IScheduler scheduler, IOutput console, SmartWarpRepository warpRepository, SmartWarpChunkRepository chunkRepository, Engine engine)
 	{
 		super(scheduler);
 		this.console = console;
 		this.warpRepository = warpRepository;
 		this.chunkRepository = chunkRepository;
 		this.engine = engine;
 	}
 
 	public void Setup(RunsafeWorld world, String radius)
 	{
 		int r = Integer.valueOf(radius);
 		warpRepository.setRange(world.getName(), r);
 		range.put(world.getName(), r);
 		if (!progress.containsKey(world))
 			progress.put(world.getName(), 0D);
 		if (!isQueued(world.getName()))
 			ScheduleNext(world.getName());
 	}
 
 	@Override
 	public void OnPluginEnabled()
 	{
 		for (String world : warpRepository.getWorlds())
 		{
 			progress.put(world, warpRepository.getProgress(world));
 			range.put(world, warpRepository.getRange(world));
 			ScheduleNext(world);
 		}
 	}
 
 	@Override
 	public void process(String world, RunsafeLocation location)
 	{
 		location = engine.findTop(location);
 		if (engine.targetFloorIsSafe(location, true))
 			chunkRepository.saveTarget(location, true, false);
 		if (location.getWorld().getRaw().getEnvironment() != World.Environment.NETHER)
 		{
 			while (location.getBlockY() > 10)
 			{
 				location.decrementY(1);
 				if (engine.targetFloorIsSafe(location, true))
 					chunkRepository.saveTarget(location, true, true);
 			}
 		}
 		Double p = progress.get(world) + 1;
 		progress.put(world, p);
 		warpRepository.setProgress(world, p);
 		if (progress.get(world) % 100 == 0)
 		{
			double d = range.get(world) / 16;
 			console.writeColoured(
 				"Scanning location %.0f/%.0f in %s (%.2f%%)",
				p, d * d, world, 100D * p / (d * d)
 			);
 		}
 		ScheduleNext(world);
 	}
 
 	private void ScheduleNext(String world)
 	{
 		int d = range.get(world) / 16;
 		double target = d * d;
 		if (progress.get(world) >= target)
 		{
 			console.writeColoured("Nothing left to scan in %s, stopping!", world);
 			return;
 		}
 		Push(world, CalculateNextLocation(world));
 	}
 
 	private RunsafeLocation CalculateNextLocation(String world)
 	{
 		if (!worlds.containsKey(world))
 			worlds.put(world, RunsafeServer.Instance.getWorld(world));
 		int r = range.get(world);
 		double offset = r / 2;
 		double x = progress.get(world) % r;
 		double z = progress.get(world) / r;
 		return new RunsafeLocation(worlds.get(world), x * 16 - offset, 255, z * 16 - offset);
 	}
 
 	private HashMap<String, Double> progress = new HashMap<String, Double>();
 	private HashMap<String, Integer> range = new HashMap<String, Integer>();
 	private HashMap<String, RunsafeWorld> worlds = new HashMap<String, RunsafeWorld>();
 	private IOutput console;
 	private SmartWarpRepository warpRepository;
 	private SmartWarpChunkRepository chunkRepository;
 	private Engine engine;
 }
