 package no.runsafe.warpdrive.commands;
 
 import no.runsafe.framework.command.IContextPermissionProvider;
 import no.runsafe.framework.command.player.PlayerCommand;
 import no.runsafe.framework.server.ICommandExecutor;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.player.RunsafeAmbiguousPlayer;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import no.runsafe.warpdrive.Engine;
 
 import java.util.Arrays;
 import java.util.HashMap;
 
 public class Teleport extends PlayerCommand implements IContextPermissionProvider
 {
 	public Teleport(Engine engine)
 	{
 		super("teleport", "Teleports you or another player to another player", null, "player");
 		this.engine = engine;
 	}
 
 	@Override
 	public String getPermission(ICommandExecutor executor, HashMap<String, String> parameters, String[] args)
 	{
		if (args.length > 0 && args[args.length - 1].equals("-f"))
		{
			args = Arrays.copyOfRange(args, 0, args.length - 1);
		}
 		if (executor instanceof RunsafePlayer)
 		{
 			RunsafePlayer teleporter = (RunsafePlayer) executor;
 			RunsafePlayer target;
 			if (args.length == 0)
 				target = RunsafeServer.Instance.getOnlinePlayer(teleporter, parameters.get("player"));
 			else
 				target = RunsafeServer.Instance.getOnlinePlayer(teleporter, args[0]);
 			if (target == null)
 				return null;
 
 			return "runsafe.teleport.world." + target.getWorld().getName();
 		}
 		return null;
 	}
 
 	@Override
 	public String OnExecute(RunsafePlayer player, HashMap<String, String> parameters, String[] args)
 	{
 		if (args == null)
 			args = new String[0];
 		String movePlayer;
 		RunsafePlayer move;
 		String toPlayer;
 		RunsafePlayer to;
 		boolean force = false;
 		if (args.length > 0 && args[args.length - 1].equals("-f"))
 		{
 			force = true;
 			args = Arrays.copyOfRange(args, 0, args.length - 1);
 		}
 		if (args.length > 0)
 		{
 			movePlayer = parameters.get("player");
 			move = RunsafeServer.Instance.getOnlinePlayer(player, movePlayer);
 			toPlayer = args[0];
 			to = RunsafeServer.Instance.getOnlinePlayer(player, toPlayer);
 		}
 		else
 		{
 			movePlayer = player.getName();
 			move = player;
 			toPlayer = parameters.get("player");
 			to = RunsafeServer.Instance.getOnlinePlayer(player, toPlayer);
 		}
 
 		if (move instanceof RunsafeAmbiguousPlayer)
 			return move.toString();
 
 		if (to instanceof RunsafeAmbiguousPlayer)
 			return to.toString();
 
 		if (move == null || !move.isOnline())
 			return String.format("Could not find player %s to teleport.", movePlayer);
 
 		if (to == null || !to.isOnline())
 			return String.format("Could not find destination player %s.", toPlayer);
 
 		if (to.getWorld().getName().equals(move.getWorld().getName()))
 		{
 			if (to.isCreative() && move.isCreative())
 			{
 				move.teleport(to.getLocation());
 				return null;
 			}
 		}
 		if (force)
 		{
 			move.teleport(to.getLocation());
 			return String.format("Performed unsafe teleport of %s to %s.", move.getPrettyName(), to.getPrettyName());
 		}
 		if (engine.safePlayerTeleport(to.getLocation(), move))
 			return null;
 
 		return String.format("Unable to safely teleport %1$s to %2$s, try /tp %1$s %2$s -f", move.getPrettyName(), to.getPrettyName());
 	}
 
 	@Override
 	public String OnExecute(RunsafePlayer player, HashMap<String, String> stringStringHashMap)
 	{
 		return null;
 	}
 
 	private final Engine engine;
 }
