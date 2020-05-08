 package no.runsafe.nchat.command;
 
 import no.runsafe.framework.api.command.ExecutableCommand;
 import no.runsafe.framework.api.command.ICommandExecutor;
 import no.runsafe.framework.api.command.argument.AnyPlayerRequired;
 import no.runsafe.framework.api.command.argument.IArgumentList;
 import no.runsafe.framework.api.log.IConsole;
 import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.api.player.IPlayerVisibility;
 import no.runsafe.nchat.chat.MuteHandler;
 
 public class MuteCommand extends ExecutableCommand
 {
 	public MuteCommand(IConsole console, MuteHandler muteHandler)
 	{
 		super(
 			"mute", "Suppress chat messages from a player", "runsafe.nchat.mute",
 			new AnyPlayerRequired()
 		);
 		this.console = console;
 		this.muteHandler = muteHandler;
 	}
 
 	@Override
 	public String OnExecute(ICommandExecutor executor, IArgumentList parameters)
 	{
 		IPlayer player = executor instanceof IPlayer ? (IPlayer) executor : null;
 		String mutePlayerName = parameters.get("player");
 
 		if (mutePlayerName.equalsIgnoreCase("server"))
 		{
 			if (player != null && !player.hasPermission("runsafe.nchat.mute.server"))
 				return "&cYou do not have permission to do that";
 
 			muteHandler.muteServer();
 			return "&bGlobal chat has been muted, you monster.";
 		}
 		IPlayer mutePlayer = parameters.getPlayer("player");
 

 		if (mutePlayer == null)
 			return "&cThat player does not exist.";
 
		if (player != null && (!mutePlayer.isOnline() || player.shouldNotSee(mutePlayer)))
			return "&cThat player is currently offline.";

 		if (player != null && mutePlayer.hasPermission("runsafe.nchat.mute.exempt"))
 			return "&cNice try, but you cannot mute that player."; // Unless you are the console ^w^
 
 		console.logInformation(String.format("%s muted %s", executor.getName(), mutePlayer.getName()));
 		muteHandler.mutePlayer(mutePlayer);
 		return String.format("&bMuted %s.", mutePlayer.getPrettyName());
 	}
 
 	private final IConsole console;
 	private final MuteHandler muteHandler;
 }
