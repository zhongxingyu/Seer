 package no.runsafe.warpdrive.summoningstone;
 
 import no.runsafe.framework.configuration.IConfiguration;
 import no.runsafe.framework.event.IConfigurationChanged;
 import no.runsafe.framework.server.RunsafeLocation;
 import no.runsafe.framework.timer.IScheduler;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class SummoningEngine implements IConfigurationChanged
 {
 	public SummoningEngine(SummoningStoneRepository summoningStoneRepository, IScheduler scheduler)
 	{
 		this.summoningStoneRepository = summoningStoneRepository;
 		this.scheduler = scheduler;
 	}
 
 	public void registerPendingSummon(String playerName, int stoneID)
 	{
 		this.pendingSummons.put(playerName, stoneID);
 	}
 
 	public int getStoneAtLocation(RunsafeLocation location)
 	{
 		for (Map.Entry<Integer, SummoningStone> stone : this.stones.entrySet())
 		{
			RunsafeLocation stoneLocation = stone.getValue().getLocation();
			if (stoneLocation.getWorld().getName().equals(location.getWorld().getName()))
				if (stoneLocation.distance(location) == 0)
					return stone.getKey();
 		}
 		return -1;
 	}
 
 	public int registerExpireTimer(final int stoneID)
 	{
 		return this.scheduler.startSyncTask(new Runnable() {
 			@Override
 			public void run() {
 				summoningStoneExpire(stoneID);
 			}
 		}, this.stoneExpireTime);
 	}
 
 	public void registerStone(int stoneID, SummoningStone stone)
 	{
 		this.stones.put(stoneID, stone);
 	}
 
 	public void summoningStoneExpire(int stoneID)
 	{
 		// If this is false, we actually had a fault somewhere, but we check anyway.
 		if (this.stones.containsKey(stoneID))
 		{
 			// The stone expired, remove it from everything.
 			SummoningStone stone = this.stones.get(stoneID);
 			stone.remove();
 			this.summoningStoneRepository.deleteSummoningStone(stoneID);
 			this.stones.remove(stoneID);
 		}
 	}
 
 	public HashMap<Integer, SummoningStone> getLoadedStones()
 	{
 		return this.stones;
 	}
 
 	@Override
 	public void OnConfigurationChanged(IConfiguration config)
 	{
 		this.stoneExpireTime = config.getConfigValueAsInt("summoningStone.expire") * 60;
 	}
 
 	private int stoneExpireTime = 600;
 	private HashMap<Integer, SummoningStone> stones = new HashMap<Integer, SummoningStone>();
 	private HashMap<String, Integer> pendingSummons = new HashMap<String, Integer>();
 	private SummoningStoneRepository summoningStoneRepository;
 	private IScheduler scheduler;
 }
