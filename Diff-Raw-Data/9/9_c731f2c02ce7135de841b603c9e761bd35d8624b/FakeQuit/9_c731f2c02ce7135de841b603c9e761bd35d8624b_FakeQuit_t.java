 package no.runsafe.vanishbridge.command;
 
 import no.runsafe.framework.command.player.PlayerCommand;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.event.player.RunsafePlayerQuitEvent;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import no.runsafe.vanishbridge.PlayerVanishManager;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import java.util.HashMap;
 
 public class FakeQuit extends PlayerCommand
 {
 	public FakeQuit(PlayerVanishManager playerVanishManager)
 	{
 		super("fakequit", "Fakes a quit event and vanishes", "runsafe.vanish.fakequit");
 		manager = playerVanishManager;
 	}
 
 	@Override
	public String OnExecute(RunsafePlayer player, HashMap<String, String> stringStringHashMap)
 	{
 		manager.setVanished(player, false);
 		RunsafePlayerQuitEvent fake = new RunsafePlayerQuitEvent(new PlayerQuitEvent(player.getRawPlayer(), null));
 		fake.Fire();
 		manager.setVanished(player, true);
 		RunsafeServer.Instance.broadcastMessage(fake.getQuitMessage());
 		return null;
 	}
 
 	final PlayerVanishManager manager;
 }
