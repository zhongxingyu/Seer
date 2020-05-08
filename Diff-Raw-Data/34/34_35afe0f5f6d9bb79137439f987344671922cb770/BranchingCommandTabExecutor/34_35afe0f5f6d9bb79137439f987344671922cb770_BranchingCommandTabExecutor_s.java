 package no.runsafe.framework.internal.command;
 
 import com.google.common.collect.ImmutableList;
 import no.runsafe.framework.api.command.ICommandExecutor;
 import no.runsafe.framework.api.command.ICommandHandler;
 import no.runsafe.framework.api.command.IPreparedCommand;
 import no.runsafe.framework.api.log.IConsole;
 import no.runsafe.framework.api.log.IDebug;
 import no.runsafe.framework.internal.wrapper.ObjectWrapper;
 import no.runsafe.framework.text.ChatColour;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.craftbukkit.libs.joptsimple.internal.Strings;
 import org.bukkit.entity.Player;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 public final class BranchingCommandTabExecutor implements ITabExecutor
 {
 	public BranchingCommandTabExecutor(ICommandHandler command, ICommandExecutor console, IDebug debug, IConsole consoleLog)
 	{
 		name = command.getName();
 		this.console = console;
 		debugger = debug;
 		this.consoleLog = consoleLog;
 		addBranch(command);
 	}
 
 	public void addBranch(ICommandHandler command)
 	{
 		branches.put(command.getParameters().size(), command);
 	}
 
 	@Override
 	public String getName()
 	{
 		return name;
 	}
 
 	@Override
 	public ICommandHandler getHandler()
 	{
 		return null;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
 	{
 		try
 		{
 			if (args == null)
 				executeCommand(sender, new String[0]);
 			else
 				executeCommand(sender, args);
 		}
 		catch (Exception e)
 		{
 			consoleLog.logException(e);
 			if (sender != null)
 				sender.sendMessage(ChatColour.ToMinecraft("&cCommand threw an exception!"));
 		}
 		return true;
 	}
 
 	@Nullable
 	@Override
 	public List<String> onTabComplete(CommandSender commandSender, Command command, String alias, String[] args)
 	{
 		debugger.debugFine("Handling tabcomplete for command '%s %s'", alias, Strings.join(args, " "));
 		Iterable<String> alternatives = tabCompleteCommand(commandSender, args);
 		return alternatives == null ? null : ImmutableList.copyOf(alternatives);
 	}
 
 	private Iterable<String> tabCompleteCommand(CommandSender sender, String... args)
 	{
 		IPreparedCommand preparedCommand = preparedCommand(sender, true, args);
 		Iterable<String> options = preparedCommand.tabComplete(args);
 		debugger.debugFine("Tab completion options to return: %s", options);
 		return options;
 	}
 
 	private void executeCommand(CommandSender sender, String... args)
 	{
 		IPreparedCommand preparedCommand = preparedCommand(sender, false, args);

 		String permission = preparedCommand.getRequiredPermission();
 		if (!(sender instanceof Player) || permission == null || sender.hasPermission(permission))
 		{
 			String feedback = preparedCommand.execute();
 			if (feedback != null)
 			{
 				if (sender instanceof Player)
 					sender.sendMessage(ChatColour.ToMinecraft(feedback));
 				else
 					console.sendColouredMessage(feedback);
 			}
 		}
 		else
 		{
 			sender.sendMessage(
 				ChatColour.ToMinecraft(
 					String.format("&4You do not have the required permission &c%s&4!", permission)
 				)
 			);
 		}
 	}
 
 	private IPreparedCommand preparedCommand(CommandSender sender, boolean skipLast, String... args)
 	{
		return getBranch(args).prepare(
			sender instanceof Player ? ObjectWrapper.convert((OfflinePlayer) sender) : console,
			skipLast ? Arrays.copyOfRange(args, 0, args.length - 1) : args
		);
 	}
 
	@Nonnull
 	private ICommandHandler getBranch(String... args)
 	{
 		debugger.debugFine("Looking up branch of %s with %d arguments", name, args.length);
 		return branches.get(args.length);
 	}
 
 	private final String name;
 	private final ICommandExecutor console;
 	private final IDebug debugger;
 	private final IConsole consoleLog;
 	private final Map<Integer, ICommandHandler> branches = new ConcurrentHashMap<Integer, ICommandHandler>(2);
 }
