 package no.runsafe.usermonitor.scoreboard;
 
 import no.runsafe.framework.configuration.IConfiguration;
 import no.runsafe.framework.event.IConfigurationChanged;
 import no.runsafe.framework.event.IPluginEnabled;
 import no.runsafe.framework.event.player.IPlayerJoinEvent;
 import no.runsafe.framework.event.player.IPlayerQuitEvent;
 import no.runsafe.framework.output.IOutput;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.event.player.RunsafePlayerJoinEvent;
 import no.runsafe.framework.server.event.player.RunsafePlayerQuitEvent;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import no.runsafe.framework.timer.IScheduler;
 import no.runsafe.framework.timer.Worker;
 import no.runsafe.usermonitor.Plugin;
 import org.bukkit.ChatColor;
 
 import java.io.*;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 
 public class PlayerCounters extends Worker<String, String>
 	implements IPlayerJoinEvent, IPlayerQuitEvent, IPluginEnabled, IConfigurationChanged
 {
 	public PlayerCounters(
 		Plugin monitor,
 		IScheduler scheduler,
 		RunsafeServer server,
 		IOutput console
 	)
 	{
 		super(scheduler);
 		this.server = server;
 		this.scoreBoard = new File(String.format("plugins/%s/scoreboard.txt", monitor.getName()));
 		this.console = console;
 	}
 
 	@Override
 	public void OnPlayerJoinEvent(RunsafePlayerJoinEvent event)
 	{
 		Push(event.getPlayer().getName(), "join");
 	}
 
 	@Override
 	public void OnPlayerQuit(RunsafePlayerQuitEvent event)
 	{
 		Push(event.getPlayer().getName(), "quit");
 	}
 
 	@Override
 	public void OnPluginEnabled()
 	{
 		// Use the worker to ensure concurrency.
 		Push("#event", "startup");
 	}
 
 	@Override
 	public void process(String player, String event)
 	{
 		if (event.equals("startup"))
 		{
 			for (String key : playerIsOnline.keySet())
 				playerIsOnline.put(key, false);
 
 			for (RunsafePlayer online : server.getOnlinePlayers())
 				playerIsOnline.put(online.getName(), true);
 		}
 		else if (event.equals("join"))
 			playerIsOnline.put(player, true);
 
 		else if (event.equals("quit"))
 			playerIsOnline.put(player, false);
 	}
 
 	@Override
 	public void OnConfigurationChanged(IConfiguration config)
 	{
 		setInterval(config.getConfigValueAsInt("scoreboard.update.delay"));
 	}
 
 	@Override
 	protected void onWorkerDone()
 	{
 		try
 		{
 			if (scoreBoard.createNewFile())
 			{
 				console.outputColoredToConsole(
 					String.format(
 						"%screated new scoreboard %s.%s",
 						ChatColor.GREEN,
 						scoreBoard.getAbsolutePath(),
 						ChatColor.RESET
 					),
 					Level.INFO
 				);
 			}
 			Writer output = new BufferedWriter(new FileWriter(scoreBoard));
 			for (Map.Entry<String, Boolean> playerOnline : playerIsOnline.entrySet())
 			{
 				if (playerOnline.getValue())
 					output.write(String.format("%s\n", playerOnline.getKey()));
 			}
 			output.close();
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	private final HashMap<String, Boolean> playerIsOnline = new HashMap<String, Boolean>();
 	private final RunsafeServer server;
 	private final File scoreBoard;
 	private final IOutput console;
 }
