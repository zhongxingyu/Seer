 package no.runsafe.usermonitor.scoreboard;
 
 import no.runsafe.framework.api.IConfiguration;
 import no.runsafe.framework.api.IScheduler;
 import no.runsafe.framework.api.IServer;
 import no.runsafe.framework.api.event.player.IPlayerJoinEvent;
 import no.runsafe.framework.api.event.player.IPlayerQuitEvent;
 import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
 import no.runsafe.framework.api.event.plugin.IPluginEnabled;
import no.runsafe.framework.api.log.IConsole;
 import no.runsafe.framework.api.player.IPlayer;
 import no.runsafe.framework.minecraft.event.player.RunsafePlayerJoinEvent;
 import no.runsafe.framework.minecraft.event.player.RunsafePlayerQuitEvent;
 import no.runsafe.framework.timer.Worker;
 import no.runsafe.usermonitor.Plugin;
 
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
 		IServer server,
		IConsole console
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
 
 			for (IPlayer online : server.getOnlinePlayers())
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
 				console.writeColoured(
 					"&acreated new scoreboard %s.&r",
 					Level.INFO,
 					scoreBoard.getAbsolutePath()
 				);
 			}
 			Writer output = new BufferedWriter(new FileWriter(scoreBoard));
 			for (Map.Entry<String, Boolean> playerOnline : playerIsOnline.entrySet())
 			{
 				if (playerOnline.getValue())
 				{
 					server.getPlayer(playerOnline.getKey());
 					output.write(String.format("%s\n", playerOnline.getKey()));
 				}
 			}
 			output.close();
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	private final HashMap<String, Boolean> playerIsOnline = new HashMap<String, Boolean>();
 	private final IServer server;
 	private final File scoreBoard;
	private final IConsole console;
 }
