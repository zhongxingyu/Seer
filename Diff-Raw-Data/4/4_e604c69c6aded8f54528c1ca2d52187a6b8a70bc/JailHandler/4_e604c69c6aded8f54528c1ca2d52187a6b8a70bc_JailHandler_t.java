 package no.runsafe.runsafejail.handlers;
 
 import no.runsafe.framework.configuration.IConfiguration;
 import no.runsafe.framework.event.IConfigurationChanged;
 import no.runsafe.framework.output.IOutput;
 import no.runsafe.framework.server.RunsafeLocation;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.RunsafeWorld;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import no.runsafe.framework.timer.ITimer;
 import no.runsafe.runsafejail.objects.JailSentence;
 import no.runsafe.runsafejail.objects.JailedPlayer;
 import no.runsafe.runsafejail.database.JailedPlayerDatabaseObject;
 import no.runsafe.runsafejail.database.JailedPlayersDatabase;
 import no.runsafe.runsafejail.database.JailsDatabase;
 import no.runsafe.runsafejail.exceptions.JailException;
 import no.runsafe.runsafejail.exceptions.JailPlayerException;
import org.bukkit.entity.LivingEntity;
 import org.joda.time.DateTime;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Level;
 
 public class JailHandler implements IConfigurationChanged
 {
 	public JailHandler(JailsDatabase jailsDatabase, JailedPlayersDatabase jailedPlayersDatabase, IOutput console, JailSentenceFactory jailSentenceFactory)
 	{
 		this.jailsDatabase = jailsDatabase;
 		this.console = console;
 		this.jailedPlayersDatabase = jailedPlayersDatabase;
 		this.jailSentenceFactory = jailSentenceFactory;
 		this.jailSentenceFactory.setJailHandler(this);
 	}
 
 	@Override
 	public void OnConfigurationChanged(IConfiguration configuration)
 	{
 		this.loadJailsFromDatabase();
 		this.loadJailedPlayersFromDatabase();
 
 		this.jailTether = configuration.getConfigValueAsInt("tether");
 	}
 
 	private void loadJailsFromDatabase()
 	{
 		this.jails = this.jailsDatabase.getJails();
 		this.console.outputDebugToConsole("Loaded %s jails from the database.", Level.INFO,  this.jails.size());
 	}
 
 	private void loadJailedPlayersFromDatabase()
 	{
 		this.cancelAllJailTimers();
 		this.jailedPlayers = new HashMap<String, JailSentence>();
 		List<JailedPlayerDatabaseObject> jailedPlayers = this.jailedPlayersDatabase.getJailedPlayers();
 
 		for (JailedPlayerDatabaseObject playerData : jailedPlayers)
 		{
 			RunsafePlayer player = RunsafeServer.Instance.getPlayer(playerData.getPlayerName());
 
 			if (player != null)
 			{
 				JailedPlayer jailedPlayer = (JailedPlayer) player;
 				jailedPlayer.setReturnLocation(new RunsafeLocation(
 						new RunsafeWorld(playerData.getReturnWorld()),
 						playerData.getReturnX(),
 						playerData.getReturnY(),
 						playerData.getReturnZ()
 				));
 
 				try
 				{
 					this.jailPlayer(jailedPlayer, playerData.getJailName(), playerData.getSentenceEnd());
 				}
 				catch (JailPlayerException e)
 				{
 					this.console.outputDebugToConsole(
 							"Failed to load jail sentence for %s in jail %s: %s",
 							Level.WARNING,
 							jailedPlayer.getName(),
 							playerData.getJailName(),
 							e.getMessage()
 					);
 				}
 			}
 		}
 		this.console.outputDebugToConsole("Loaded %s jail sentences from the database.", Level.INFO, this.jailedPlayers.size());
 	}
 
 	private boolean jailExists(String jailName)
 	{
 		return this.jails.containsKey(jailName);
 	}
 
 	private boolean playerIsJailed(String playerName)
 	{
 		return this.jailedPlayers.containsKey(playerName);
 	}
 
 	private String getPlayerJail(String playerName) throws JailException
 	{
 		if (this.playerIsJailed(playerName))
 			return this.jailedPlayers.get(playerName).getJailName();
 
 		throw new JailException("That player is not in jail");
 	}
 
 	private void cancelAllJailTimers()
 	{
 		if (this.jailedPlayers != null)
 			for (JailSentence jailSentence : this.jailedPlayers.values())
 				jailSentence.getJailTimer().stop();
 	}
 
 	public void jailPlayer(RunsafePlayer player, String jailName, DateTime end) throws JailPlayerException
 	{
 		if (player != null)
 		{
 			if (this.jailExists(jailName))
 			{
 				String playerName = player.getName();
 				if (!this.playerIsJailed(playerName))
 				{
 					long remainingTime = DateTime.now().minus(end.getMillis()).getMillis();
 
					JailedPlayer jailedPlayer = new JailedPlayer(player.getRawPlayer());
 					if (!jailedPlayer.hasReturnLocation()) jailedPlayer.setReturnLocation();
 
 					JailSentence jailSentence = new JailSentence(jailName, end, this.jailSentenceFactory.create(
 							jailedPlayer,
 							remainingTime,
 							false
 					));
 
 					this.jailedPlayers.put(playerName, jailSentence);
 					this.console.outputDebugToConsole(
 							"Jailing player %s for %sMS", Level.INFO, playerName, remainingTime
 					);
 				}
 				else
 				{
 					throw new JailPlayerException("That player is already in jail.");
 				}
 			}
 			else
 			{
 				throw new JailPlayerException("The specified jail does not exist.");
 			}
 		}
 		else
 		{
 			throw new JailPlayerException("The specified player does not exist.");
 		}
 	}
 
 	public void unjailPlayer(RunsafePlayer player) throws JailPlayerException
 	{
 		String playerName = player.getName();
 		if (this.playerIsJailed(playerName))
 		{
 			ITimer jailTimer = this.jailedPlayers.get(playerName).getJailTimer();
 			jailTimer.ResetTicks(0L);
 		}
 		else
 		{
 			throw new JailPlayerException("The specified player is not in jail.");
 		}
 	}
 
 	public void removeJailSentence(String playerName)
 	{
 		if (this.playerIsJailed(playerName))
 		{
 			this.jailedPlayers.remove(playerName);
 			this.jailedPlayersDatabase.removeJailedPlayer(playerName);
 		}
 	}
 
 	public void checkTether(RunsafePlayer player) throws JailException
 	{
 		RunsafeLocation jailLocation = this.getJailLocation(this.getPlayerJail(player.getName()));
 		if (jailLocation.distance(player.getLocation()) > this.jailTether)
 		{
 			player.teleport(jailLocation);
 			this.console.outputDebugToConsole(
 					"%s was outside jail tether radius. Teleporting back.", Level.FINE, player.getName()
 			);
 		}
 	}
 
 	public RunsafeLocation getJailLocation(String jailName) throws JailException
 	{
 		if (this.jailExists(jailName))
 			return this.jails.get(jailName);
 		else
 			throw new JailException("The specified jail does not exist.");
 	}
 
 	private HashMap<String, RunsafeLocation> jails;
 	private HashMap<String, JailSentence> jailedPlayers;
 
 	private JailsDatabase jailsDatabase;
 	private JailedPlayersDatabase jailedPlayersDatabase;
 	private JailSentenceFactory jailSentenceFactory;
 
 	private IOutput console;
 
 	private int jailTether = 20;
 }
