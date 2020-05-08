 package no.runsafe.toybox.command;
 
 import no.runsafe.framework.command.player.PlayerCommand;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import org.bukkit.GameMode;
 
 import java.util.HashMap;
 
 public class Mode extends PlayerCommand
 {
 	public Mode()
 	{
 		super("mode", "Changes the game-mode of the player", "runsafe.toybox.mode", "player", "mode");
 	}
 
 	@Override
 	public String OnExecute(RunsafePlayer executor, HashMap<String, String> parameters)
 	{
 		RunsafePlayer target = RunsafeServer.Instance.getPlayer(parameters.get("player"));
 
 		if (target != null)
 		{
 			if (target.isOnline())
 			{
 				String mode = parameters.get("mode");
 
 				if (mode.equalsIgnoreCase("survival") || mode.equalsIgnoreCase("s"))
					executor.setGameMode(GameMode.SURVIVAL);
 
 				if (mode.equalsIgnoreCase("creative") || mode.equalsIgnoreCase("c"))
					executor.setGameMode(GameMode.CREATIVE);
 
 				if (mode.equalsIgnoreCase("adventure") || mode.equalsIgnoreCase("a"))
					executor.setGameMode(GameMode.ADVENTURE);
 
 				this.sendGameModeUpdateMessage(executor, target);
 			}
 			else
 			{
 				executor.sendMessage(String.format("The player %s is offline. Can't touch that.", target.getPrettyName()));
 			}
 		}
 		else
 		{
 			executor.sendColouredMessage(String.format("The player %s does not exist, you moron.", parameters.get("player")));
 		}
 
 		return null;
 	}
 
 	private void sendGameModeUpdateMessage(RunsafePlayer player, RunsafePlayer target)
 	{
 		String gameModeName = "unknown";
 
 		if (target.getGameMode() == GameMode.SURVIVAL)
 			gameModeName = "survival";
 
 		if (target.getGameMode() == GameMode.CREATIVE)
 			gameModeName = "creative";
 
 		if (target.getGameMode() == GameMode.ADVENTURE)
 			gameModeName = "adventure";
 
 		player.sendColouredMessage("%s now has the game mode %s.", target.getPrettyName(), gameModeName);
 	}
 }
 
