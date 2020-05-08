 package nickrak.doc;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Random;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.DyeColor;
 import org.bukkit.Material;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitScheduler;
 
 public class DaysOfChristmas extends JavaPlugin
 {
 	public static DaysOfChristmas instance;
 
 	public final static void log(final String msg)
 	{
 		Logger.getLogger("DaysOfChristmas").info("[DaysOfChristmas] " + msg);
 	}
 
 	public final static void err(final String msg)
 	{
 		Logger.getLogger("DaysOfChristmas").severe("[DaysOfChristmas] " + msg);
 	}
 
 	public DaysOfChristmas()
 	{
 		instance = this;
 	}
 
 	@Override
 	public void onDisable()
 	{
 		log("Disabled");
 	}
 
 	@Override
 	public void onEnable()
 	{
 		final PluginManager pm = this.getServer().getPluginManager();
 		final int delay = 20 * 60 * 60;
 		//final int delay = 20 * 60;
 
 		final PlayerListener pl = new PlayerListener()
 		{
 			private final BukkitScheduler sched = Bukkit.getScheduler();
 
 			@Override
 			public void onPlayerJoin(PlayerJoinEvent event)
 			{
 				final String p = event.getPlayer().getName();
 				final Calendar cal = Calendar.getInstance();
 				final int mon = cal.get(Calendar.MONTH) + 1;
 				final int day = cal.get(Calendar.DAY_OF_MONTH);
 				final int doc = day - 13;
 				
 				if (mon == 12 && doc >= 1 && doc <= 12)
 				{
 					event.getPlayer().sendMessage(
 							ChatColor.GREEN + "It's the " + doc + getOrdinalExtension(doc) + " day of Christmas!");
 
 					if (!DaysOfChristmas.this.playerday.containsKey(p))
 					{
 						DaysOfChristmas.this.playerday.put(p, -1);
 					}
 
 					if (!DaysOfChristmas.this.playerrecv.containsKey(p))
 					{
 						DaysOfChristmas.this.playerrecv.put(p, new ArrayList<Integer>());
 					}
 					
 					if (!DaysOfChristmas.this.partialday.containsKey(p))
 					{
 						DaysOfChristmas.this.partialday.put(p, -1);
 					}
 					
 					if (!DaysOfChristmas.this.playerinvest.containsKey(p))
 					{
 						DaysOfChristmas.this.playerinvest.put(p, 0);
 					}
					
					if (DaysOfChristmas.this.partialday.get(p) != doc)
					{
					    DaysOfChristmas.this.playerinvest.put(p, 0);
					}
 
 					if (DaysOfChristmas.this.playerday.get(p) != doc)
 					{
 						final Random rnd = new Random();
 						int r;
 
 						do
 						{
 							r = rnd.nextInt(gifts.size());
 						} while (DaysOfChristmas.this.playerrecv.get(p).contains(r));
 
 						final RunnableGift rg = new RunnableGift(gifts.get(r), doc, p);
						final long myDelay = delay - DaysOfChristmas.this.playerinvest.get(p);
 						int tid = Bukkit.getScheduler().scheduleSyncDelayedTask(DaysOfChristmas.this, rg, myDelay);
 						
 						DaysOfChristmas.this.playertasks.put(p, tid);
 						DaysOfChristmas.this.partialtime.put(p, System.currentTimeMillis() / 1000);
 						DaysOfChristmas.this.partialday.put(p, doc);
 					}
 					
 					saveStuff();
 				}
 			}
 
 			@Override
 			public void onPlayerQuit(PlayerQuitEvent event)
 			{
 				final String p = event.getPlayer().getName();
 				final Integer tid = DaysOfChristmas.this.playertasks.remove(p);
 
 				if (tid != null)
 				{
 					this.sched.cancelTask(tid);
 
 					final long startTime = DaysOfChristmas.this.partialtime.get(p);
 					final long timespent = (System.currentTimeMillis() / 1000) - startTime;
 					final int timesum = (int) ((timespent * 20) + DaysOfChristmas.this.playerinvest.get(p));
 					DaysOfChristmas.this.playerinvest.put(p, timesum);
 				}
 				
 				saveStuff();
 			}
 		};
 
 		this.loadStuff();
 
 		pm.registerEvent(Type.PLAYER_JOIN, pl, Priority.Monitor, this);
 		pm.registerEvent(Type.PLAYER_QUIT, pl, Priority.Monitor, this);
 
 		log("Enabled version " + this.getDescription().getVersion());
 	}
 
 	public final void saveStuff()
 	{
 		final FileConfiguration fc = this.getConfig();
 		for (final String player : this.playerday.keySet())
 		{
 			fc.set("RECVDITEMS" + player, this.playerrecv.get(player));
 			fc.set("DAYCOUNTER" + player, this.playerday.get(player));
 			fc.set("INVEST" + player, this.playerinvest.get(player));
 			fc.set("PARTIALDAY" + player, this.partialday.get(player));
 		}
 		fc.set("players", new ArrayList<String>(this.playerday.keySet()));
 		this.saveConfig();
 	}
 
 	public final void loadStuff()
 	{
 		final FileConfiguration fc = this.getConfig();
 		List<String> names = fc.getStringList("players");
 		if (names == null)
 		{
 			names = new ArrayList<String>();
 		}
 
 		for (final String player : names)
 		{
 			List<Integer> recvs = fc.getIntegerList("RECVDITEMS" + player);
 			if (recvs == null)
 			{
 				recvs = new ArrayList<Integer>();
 			}
 
 			final int pday = fc.getInt("DAYCOUNTER" + player, -1);
 			final int inv = fc.getInt("INVEST" + player, 0);
 			final int iday = fc.getInt("PARTIALDAY" + player, 0);
 
 			this.playerrecv.put(player, new ArrayList<Integer>(recvs));
 			this.playerday.put(player, pday);
 			this.playerinvest.put(player, inv);
 			this.partialday.put(player, iday);
 		}
 	}
 
 	protected final ConcurrentHashMap<String, Integer> playertasks = new ConcurrentHashMap<String, Integer>();
 	protected final ConcurrentHashMap<String, ArrayList<Integer>> playerrecv = new ConcurrentHashMap<String, ArrayList<Integer>>();
 	protected final ConcurrentHashMap<String, Integer> playerday = new ConcurrentHashMap<String, Integer>();
 	protected final ConcurrentHashMap<String, Integer> playerinvest = new ConcurrentHashMap<String, Integer>();
 	protected final ConcurrentHashMap<String, Integer> partialday = new ConcurrentHashMap<String, Integer>();
 	protected final ConcurrentHashMap<String, Long> partialtime = new ConcurrentHashMap<String, Long>();
 
 	protected final static ArrayList<Gift> gifts;
 
 	static
 	{
 		gifts = new ArrayList<Gift>();
 		gifts.add(new ItemGift(Material.IRON_INGOT));
 		gifts.add(new ItemGift(Material.GOLD_INGOT));
 		gifts.add(new ItemGift(Material.COAL));
 		gifts.add(new ItemGift(Material.DIAMOND));
 		gifts.add(new ItemGift(Material.SNOW_BLOCK));
 		gifts.add(new ItemGift(Material.CAKE));
 		gifts.add(new ItemGift(Material.COOKIE));
 		gifts.add(new ItemGift(Material.COOKED_CHICKEN));
 		gifts.add(new ItemGift(Material.SAPLING));
 		gifts.add(new ItemGift(Material.EGG));
 		gifts.add(new SheepGift(DyeColor.RED));
 		gifts.add(new SheepGift(DyeColor.GREEN));
 	}
 
 	public final void playerGifted(String player, Gift g, int count)
 	{
 		final int giftID = gifts.indexOf(g);
 		this.playertasks.remove(player);
 		this.playerrecv.get(player).add(giftID);
 		this.playerday.put(player, count);
 
 		this.saveStuff();
 	}
 
 	public final static String getOrdinalExtension(int number)
 	{
 		StringBuilder sb = new StringBuilder();
 		switch (number % 100)
 		{
 		case 1:
 			sb.append("st");
 			break;
 		case 2:
 			sb.append("nd");
 			break;
 		case 3:
 			sb.append("rd");
 			break;
 		default:
 			sb.append("th");
 			break;
 		}
 
 		return sb.toString();
 	}
 }
