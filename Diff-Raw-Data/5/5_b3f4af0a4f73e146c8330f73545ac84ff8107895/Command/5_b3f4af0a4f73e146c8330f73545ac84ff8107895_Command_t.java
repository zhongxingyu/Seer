 package no.runsafe.framework.api.command;
 
 import com.google.common.base.Function;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import no.runsafe.framework.api.IOutput;
 import no.runsafe.framework.api.command.argument.IArgument;
 import no.runsafe.framework.api.command.argument.PlayerArgument;
 import no.runsafe.framework.api.command.argument.RequiredArgument;
 import no.runsafe.framework.api.command.argument.WorldArgument;
 import no.runsafe.framework.internal.command.prepared.PreparedSynchronousCommand;
 import no.runsafe.framework.text.ChatColour;
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.craftbukkit.libs.joptsimple.internal.Strings;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * The base command class of the framework
  * Create instances of this object for commands that only contain subcommands
  */
 public class Command implements ICommandHandler
 {
 	/**
 	 * Defines the command
 	 *
 	 * @param commandName The name of the command. For top level commands, this must be as defined in plugin.yml
 	 * @param description A short descriptive text of what the command does
 	 * @param permission  A permission String that a player must have to run the command or null to allow anyone to run it
 	 * @param arguments   Optional list of required command parameters
 	 */
 	public Command(
 		@Nonnull String commandName, @Nonnull String description, @Nullable String permission,
 		CharSequence... arguments
 	)
 	{
 		name = commandName;
 		this.permission = permission;
 		this.description = description;
 		List<IArgument> converted = new ArrayList<IArgument>(arguments.length);
 		for (CharSequence arg : arguments)
 		{
 			if (arg.equals("player"))
 				converted.add(new PlayerArgument(true));
 			else if (arg.equals("world"))
 				converted.add(new WorldArgument(true));
 			else
 				converted.add(arg instanceof IArgument ? (IArgument) arg : new RequiredArgument(arg.toString()));
 		}
 		argumentList = ImmutableList.copyOf(converted);
 	}
 
 	/**
 	 * Builds the usage message when an incomplete command is issued.
 	 *
 	 * @return A String to display to the player or console user
 	 */
 	@Nonnull
 	@Override
 	public String getUsage(@Nonnull ICommandExecutor executor)
 	{
 		Map<String, String> available = getAvailableSubCommands(executor);
 		List<String> usage = new ArrayList<String>(subCommands.size());
 		if (available.isEmpty())
			return description;
 
 		String format = "  %s%s%s: %s";
 		for (Map.Entry<String, String> stringStringEntry : available.entrySet())
 			usage.add(String.format(format, ChatColour.YELLOW, stringStringEntry.getKey(), ChatColour.RESET, stringStringEntry.getValue()));
 
 		return String.format(
 			"<%1$scommand%2$s>\nAvailable commands:\n%3$s",
 			ChatColour.YELLOW,
 			ChatColour.RESET,
 			Strings.join(usage, "\n")
 		);
 	}
 
 	/**
 	 * The command arguments listed in usage is built by this.
 	 * Override this if you have optional arguments
 	 *
 	 * @return List of arguments for inclusion in the command usage
 	 */
 	@Nonnull
 	@Override
 	public String getUsageCommandParams()
 	{
 		String part = ChatColour.BLUE + name + ChatColour.RESET;
 		if (!argumentList.isEmpty())
			part += ' ' + Strings.join(
 				Lists.transform(argumentList, new Function<IArgument, String>()
 				{
 					@Override
 					public String apply(@Nullable IArgument arg)
 					{
 						assert arg != null;
 						return String.format(
 							arg.isRequired() ? "<%s%s%s>%s" : "[%s%s%s]%s",
 							ChatColour.YELLOW, arg, ChatColour.RESET,
 							arg.isWhitespaceInclusive() ? "+" : ""
 						);
 					}
 				}),
 				" "
 			);
 		return part;
 	}
 
 	/**
 	 * @return The permission required to execute this command
 	 */
 	@Nullable
 	@Override
 	public final String getPermission()
 	{
 		return permission;
 	}
 
 	/**
 	 * Adds a subcommand to this command
 	 *
 	 * @param subCommand The subcommand to add to this command
 	 */
 	public final void addSubCommand(Command subCommand)
 	{
 		subCommands.put(subCommand.name, subCommand);
 	}
 
 	/**
 	 * Resolve a subcommand
 	 *
 	 * @param executor The player or console executing the command
 	 * @param name     The partial or full subcommand name
 	 * @return The selected subcommand or null if no matches
 	 */
 	@Nullable
 	public final Command getSubCommand(ICommandExecutor executor, String name)
 	{
 		if (subCommands.isEmpty())
 			return null;
 
 		if (subCommands.containsKey(name))
 			return subCommands.get(name);
 
 		String target = null;
 		for (String sub : subCommands.keySet())
 			if (sub.startsWith(name) && subCommands.get(sub).isExecutable(executor))
 			{
 				if (target != null)
 					return null;
 
 				target = sub;
 			}
 		if (target != null)
 			return subCommands.get(target);
 
 		return null;
 	}
 
 	@Nonnull
 	@Override
 	public final List<String> getSubCommands(ICommandExecutor executor)
 	{
 		List<String> available = new ArrayList<String>(subCommands.size());
 		for (Map.Entry<String, Command> stringCommandEntry : subCommands.entrySet())
 			if (stringCommandEntry.getValue().isExecutable(executor))
 				available.add(stringCommandEntry.getKey());
 		return available;
 	}
 
 	/**
 	 * @return The name of this command
 	 */
 	@Nonnull
 	@Override
 	public final String getName()
 	{
 		return name;
 	}
 
 	@Deprecated
 	@Override
 	public final boolean isCapturingTail()
 	{
 		return captureTail;
 	}
 
 	/**
 	 * Parses user input and returns a prepared command, ready to be executed
 	 *
 	 * @param executor The user or console executing the command
 	 * @param args     The passed argument list
 	 * @return A prepared command, ready to be executed
 	 */
 	@Nonnull
 	@Override
 	public final IPreparedCommand prepare(ICommandExecutor executor, @Nonnull String... args)
 	{
 		console.finer("Preparing command %s %s", name, StringUtils.join(args, " "));
 		return prepareCommand(executor, new HashMap<String, String>(args.length), args, new Stack<ICommandHandler>());
 	}
 
 	@Nonnull
 	@Override
 	public final IPreparedCommand prepareCommand(
 		@Nonnull ICommandExecutor executor,
 		@Nonnull Map<String, String> params,
 		@Nonnull String[] args,
 		@Nonnull Stack<ICommandHandler> stack
 	)
 	{
 		stack.add(this);
 		Map<String, String> myParams = parseParameters(args);
 		params.putAll(myParams);
 		if (!myParams.isEmpty())
 		{
 			args = captureTail || args.length <= myParams.size()
 				? new String[0] :
 				Arrays.copyOfRange(args, myParams.size(), args.length);
 		}
 		console.finer("Command %s has %d parameters and %d args", name, myParams.size(), args.length);
 		if (args.length > 0)
 		{
 			console.finer("Looking for subcommand %s", args[0]);
 			ICommandHandler subCommand = getSubCommand(executor, args[0]);
 			if (subCommand != null && subCommand.isExecutable(executor))
 			{
 				subCommand.setConsole(console);
 				args = Arrays.copyOfRange(args, 1, args.length);
 				console.finer("Preparing subcommand %s", executor.getName());
 				return subCommand.prepareCommand(executor, params, args, stack);
 			}
 		}
 		return stack.peek().createAction(executor, stack, args, params);
 	}
 
 	@Nonnull
 	@Override
 	public IPreparedCommand createAction(
 		@Nonnull ICommandExecutor executor,
 		@Nonnull Stack<ICommandHandler> stack,
 		@Nonnull String[] args,
 		@Nonnull Map<String, String> params
 	)
 	{
 		console.finer("Preparing Sync command with %d params and %d args", params.size(), args.length);
 		return new PreparedSynchronousCommand(executor, stack, args, params);
 	}
 
 	/**
 	 * Called by the framework to register a console object for debug output
 	 *
 	 * @param console The console to print debug information to
 	 */
 	@Override
 	public void setConsole(@Nonnull IOutput console)
 	{
 		console.finer("Setting console on command object.");
 		this.console = console;
 	}
 
 	@Nullable
 	@Override
 	public List<String> getParameterOptions(@Nonnull String parameter)
 	{
 		return null;
 	}
 
 	@Nullable
 	@Override
 	public List<String> getParameterOptionsPartial(@Nonnull String parameter, @Nonnull String arg)
 	{
 		return null;
 	}
 
 	@Nonnull
 	@Override
 	public List<IArgument> getParameters()
 	{
 		return Collections.unmodifiableList(argumentList);
 	}
 
 	@Override
 	public boolean isExecutable(@Nonnull ICommandExecutor executor)
 	{
 		if (permission == null)
 		{
 			for (Command subCommand : subCommands.values())
 				if (subCommand.isExecutable(executor))
 					return true;
 
 			return !getClass().equals(Command.class);
 		}
 		return checkPermission(executor);
 	}
 
 	/**
 	 * Call this method in your constructor if the final parameter should grab all tailing arguments
 	 * i.e. if you want to support spaces without "" for input to a command
 	 */
 	@Deprecated
 	protected final void captureTail()
 	{
 		captureTail = true;
 	}
 
 	private Map<String, String> getAvailableSubCommands(ICommandExecutor executor)
 	{
 		Map<String, String> available = new HashMap<String, String>(subCommands.size());
 		for (Command sub : subCommands.values())
 		{
 			if (sub.isExecutable(executor))
 				available.put(sub.name, String.format(" - %s", sub.description));
 		}
 		return available;
 	}
 
 	private boolean checkPermission(ICommandExecutor executor)
 	{
 		Matcher params = paramPermission.matcher(permission);
 		if (params.find())
 		{
 			Iterable<String> options = getParameterOptions(params.group());
 			if (options == null)
 				return true;
 			for (String value : options)
 				if (executor.hasPermission(params.replaceAll(value)))
 					return true;
 			return false;
 		}
 		return executor.hasPermission(permission);
 	}
 
 	private Map<String, String> parseParameters(String... args)
 	{
 		Map<String, String> parameters = new HashMap<String, String>(args.length);
 
 		int index = 0;
 		for (IArgument parameter : argumentList)
 		{
 			if (parameter.isWhitespaceInclusive())
 			{
 				if (args.length > index)
 				{
 					parameters.put(parameter.toString(), StringUtils.join(args, " ", index, args.length));
 					break;
 				}
 			}
 
 			String value = null;
 			if (args.length > index)
 				value = args[index];
 			index++;
 			if (parameter.isRequired() || value != null && !value.isEmpty())
 				parameters.put(parameter.toString(), value);
 		}
 		if (captureTail && args.length > index)
 			parameters.put(argumentList.get(index - 1).toString(), StringUtils.join(args, " ", index - 1, args.length));
 		return parameters;
 	}
 
 	protected IOutput console;
 	private final ImmutableList<IArgument> argumentList;
 	private final Map<String, Command> subCommands = new HashMap<String, Command>(0);
 	private final String name;
 	private final String permission;
 	private final String description;
 	private boolean captureTail;
 	private static final Pattern paramPermission = Pattern.compile("<(.*)>");
 }
