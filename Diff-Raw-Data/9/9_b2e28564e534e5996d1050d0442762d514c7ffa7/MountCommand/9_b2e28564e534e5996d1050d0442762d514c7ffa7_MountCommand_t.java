 package no.runsafe.mountmayhem;
 
 import no.runsafe.framework.command.ExecutableCommand;
 import no.runsafe.framework.server.ICommandExecutor;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.player.RunsafePlayer;
 
 import java.util.HashMap;
 
 public class MountCommand extends ExecutableCommand
 {
 	public MountCommand()
 	{
 		super("mount", "", "mountmayhem.mount", "passenger", "mount");
 	}
 
 	@Override
	public String OnExecute(ICommandExecutor executor, HashMap<String, String> parameters)
 	{
 		RunsafePlayer passenger = RunsafeServer.Instance.getPlayer(parameters.get("passenger"));
 		RunsafePlayer mount = RunsafeServer.Instance.getPlayer(parameters.get("mount"));
 
 		if (passenger != null && mount != null)
 		{
 			mount.setPassenger(passenger);
 			return String.format("Mounting %s on %s", passenger.getPrettyName(), mount.getPrettyName());
 		}
 		return null;
 	}
 }
