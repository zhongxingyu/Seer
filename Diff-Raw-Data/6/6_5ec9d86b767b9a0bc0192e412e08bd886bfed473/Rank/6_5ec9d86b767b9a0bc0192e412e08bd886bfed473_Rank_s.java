 package no.runsafe.UserControl.command;
 
 import no.runsafe.framework.api.IConfiguration;
 import no.runsafe.framework.api.command.ExecutableCommand;
 import no.runsafe.framework.api.command.ICommandExecutor;
 import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.OnlinePlayerRequired;
 import no.runsafe.framework.api.command.argument.UserGroupArgument;
 import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
 import no.runsafe.framework.api.player.IPlayer;
 
 import java.util.Map;
 
 public class Rank extends ExecutableCommand implements IConfigurationChanged
 {
 	public Rank()
 	{
 		super(
 			"rank", "Sets a players rank", "runsafe.usercontrol.rank.<rank>",
			new OnlinePlayerRequired(), new UserGroupArgument("rank", true)
 		);
 	}
 
 	@Override
 	public String OnExecute(ICommandExecutor executor, IArgumentList parameters)
 	{
 		IPlayer player = parameters.getPlayer("player");
 
 		if (player == null)
 			return String.format("Unable to locate a player named %s", parameters.get("player"));
 
 		if (player.getName().equals(executor.getName()))
 			return "&cYou may not change your own rank.";
 
 		for (String group : player.getGroups())
 		{
 			String permission = String.format("runsafe.usercontrol.rank.%s", group);
 			if (!executor.hasPermission(permission))
 				return String.format("&cYou may not change the group of %s", player.getPrettyName());
 		}
 
 		String rank = parameters.get("rank");
 
 		if (isInGroup(player, rank))
 			return "&cThat player is already that rank.";
 
 		if (!player.setGroup(rank))
 			return "Group set failed, somehow..";
 		if (this.messages.containsKey(rank) && player.isOnline())
 			player.sendColouredMessage(this.messages.get(rank));
 
 		return String.format("&2%s set to %s.", player.getPrettyName(), rank);
 	}
 
 	@Override
 	public void OnConfigurationChanged(IConfiguration configuration)
 	{
 		this.messages = configuration.getConfigValuesAsMap("rankMessages");
 	}
 
 	private boolean isInGroup(IPlayer player, String group)
 	{
 		for (String playerGroup : player.getGroups())
 			if (playerGroup.equalsIgnoreCase(group))
 				return true;
 
 		return false;
 	}
 
 	private Map<String, String> messages;
 }
