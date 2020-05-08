 package no.runsafe.vanishbridge;
 
 import no.runsafe.framework.hook.IPlayerDataProvider;
 import no.runsafe.framework.hook.IPlayerVisibility;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import org.joda.time.DateTime;
 import org.kitteh.vanish.VanishManager;
 import org.kitteh.vanish.VanishPlugin;
 
 import java.util.HashMap;
 
 public class PlayerVanishManager implements IPlayerDataProvider, IPlayerVisibility
 {
 	public PlayerVanishManager(VanishEvents hook)
 	{
 		VanishPlugin plugin = RunsafeServer.Instance.getPlugin("VanishNoPacket");
 		vanishNoPacket = plugin.getManager();
 		plugin.getHookManager().registerHook("runsafe", hook);
 	}
 
 	@Override
 	public HashMap<String, String> GetPlayerData(RunsafePlayer player)
 	{
 		if (player.getRawPlayer() != null && vanishNoPacket.isVanished(player.getRawPlayer()))
 		{
 			HashMap<String, String> response = new HashMap<String, String>();
 			response.put("vanished", "true");
 			return response;
 		}
 		return null;
 	}
 
 	@Override
 	public DateTime GetPlayerLogout(RunsafePlayer player)
 	{
 		return null;
 	}
 
 	@Override
 	public String GetPlayerBanReason(RunsafePlayer player)
 	{
 		return null;
 	}
 
 	@Override
 	public boolean canPlayerASeeB(RunsafePlayer a, RunsafePlayer b)
 	{
 		return !vanishNoPacket.isVanished(b.getRawPlayer()) || a.hasPermission("vanish.see");
 	}
 
 	@Override
 	public boolean isPlayerVanished(RunsafePlayer player)
 	{
		return vanishNoPacket.isVanished(player.getRawPlayer());
 	}
 
 	public void setVanished(RunsafePlayer player, boolean vanished)
 	{
 		if (vanishNoPacket.isVanished(player.getRawPlayer()) != vanished)
 			vanishNoPacket.toggleVanish(player.getRawPlayer());
 	}
 
 	private final VanishManager vanishNoPacket;
 }
