 package no.runsafe.dropparty;
 
 import no.runsafe.framework.configuration.IConfiguration;
 import no.runsafe.framework.event.IConfigurationChanged;
 import no.runsafe.framework.output.IOutput;
 import no.runsafe.framework.server.RunsafeLocation;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.RunsafeWorld;
 import no.runsafe.framework.server.item.RunsafeItemStack;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import no.runsafe.framework.timer.IScheduler;
 import org.bukkit.Effect;
 import org.bukkit.Material;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 public class DropHandler implements IConfigurationChanged
 {
 	public DropHandler(IScheduler scheduler, IOutput output)
 	{
 		this.scheduler = scheduler;
 		this.output = output;
 	}
 
 	public void addItem(RunsafeItemStack item)
 	{
 		this.output.fine(String.format("%sx %s added to drop loot.", item.getAmount(), item.getNormalName()));
 		this.items.add(item);
 	}
 
 	public void clearItems()
 	{
 		this.output.fine("Drop loot cleared.");
 		this.items.clear();
 	}
 
 	public boolean dropIsRunning()
 	{
 		return this.running;
 	}
 
 	public void initiateDrop(RunsafePlayer player)
 	{
 		RunsafeServer.Instance.broadcastMessage(String.format(this.eventMessage, (player == null ? "" : player.getPrettyName())));
 
 		this.droppingItems.addAll(this.items);
 		this.items.clear();
 
 		this.scheduler.startSyncTask(new Runnable() {
 			@Override
 			public void run() {
 				dropNext();
 			}
		}, this.spawnTimer);
 		this.running = true;
 	}
 
 	public void dropNext()
 	{
 		this.output.fine("Item drop iteration...");
 		if (!this.droppingItems.isEmpty())
 		{
 			this.output.fine("Items remaining, dropping random one.");
 			RunsafeLocation location = null;
 			while (location == null)
 			{
 				RunsafeLocation randomLocation = this.getRandomLocation();
 				if (randomLocation.getBlock().getTypeId() == Material.AIR.getId())
 					location = randomLocation;
 			}
 
 			RunsafeWorld world = location.getWorld();
 			world.dropItem(location, this.droppingItems.get(0));
 			world.playEffect(location, Effect.POTION_BREAK, 16417);
 			this.droppingItems.remove(0);
 
 			this.scheduler.startSyncTask(new Runnable() {
 				@Override
 				public void run() {
 					dropNext();
 				}
			}, 3);
 		}
 		else
 		{
 			this.output.fine("Out of items, cancelled");
 			this.running = false;
 		}
 	}
 
 	private RunsafeLocation getRandomLocation()
 	{
 		int highX = this.dropLocation.getBlockX() + this.dropRadius;
 		int highZ = this.dropLocation.getBlockZ() + this.dropRadius;
 		int lowX = this.dropLocation.getBlockX() - this.dropRadius;
 		int lowZ = this.dropLocation.getBlockZ() - this.dropRadius;
 
 		return new RunsafeLocation(
 				this.dropLocation.getWorld(),
 				this.getRandom(lowX, highX),
 				this.dropLocation.getBlockY(),
 				this.getRandom(lowZ, highZ)
 		);
 	}
 
 	private int getRandom(int low, int high)
 	{
 		return low + (int)(Math.random() * ((high - low) + 1));
 	}
 
 	public boolean hasLoot()
 	{
 		return !this.items.isEmpty();
 	}
 
 	@Override
 	public void OnConfigurationChanged(IConfiguration configuration)
 	{
 		this.eventMessage = configuration.getConfigValueAsString("eventMessage");
 		this.dropRadius = configuration.getConfigValueAsInt("dropRadius");
 		Map<String, String> configLocation = configuration.getConfigValuesAsMap("dropLocation");
 		this.spawnTimer = configuration.getConfigValueAsInt("spawnTimer");
 
 		this.dropLocation = new RunsafeLocation(
 				RunsafeServer.Instance.getWorld(configLocation.get("world")),
 				Integer.valueOf(configLocation.get("x")),
 				Integer.valueOf(configLocation.get("y")),
 				Integer.valueOf(configLocation.get("z"))
 		);
 	}
 
 	private List<RunsafeItemStack> items = new ArrayList<RunsafeItemStack>();
 	private List<RunsafeItemStack> droppingItems = new ArrayList<RunsafeItemStack>();
 	private RunsafeLocation dropLocation;
 	private int dropRadius;
 	private IScheduler scheduler;
 	private boolean running = false;
 	private IOutput output;
 	private String eventMessage;
 	private long spawnTimer;
 }
