 package no.runsafe.mountmayhem;
 
 import no.runsafe.framework.command.ExecutableCommand;
 import no.runsafe.framework.server.ICommandExecutor;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.player.RunsafePlayer;
 
 import java.util.HashMap;
 
 public class DismountCommand extends ExecutableCommand
 {
 	public DismountCommand()
 	{
 		super("dismount", "Dismounts the given player", "mountmayhem.dismount", "player");
 	}
 
 	@Override
	public String OnExecute(ICommandExecutor executor, HashMap<String, String> parameters, String[] strings)
 	{
 		RunsafePlayer player = RunsafeServer.Instance.getPlayer(parameters.get("player"));
 		if (player != null)
 		{
 			player.leaveVehicle();
 			return String.format("%s has left the vehicle.", player.getPrettyName());
 		}
 		return null;
 	}
 }
