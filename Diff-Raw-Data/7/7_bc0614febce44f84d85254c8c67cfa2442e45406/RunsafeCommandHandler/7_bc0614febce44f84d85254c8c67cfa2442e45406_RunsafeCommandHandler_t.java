 package no.runsafe.framework.command;
 
 import no.runsafe.framework.output.IOutput;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import java.io.Console;
 import java.util.ArrayList;
 import java.util.regex.Pattern;
 
 public class RunsafeCommandHandler implements CommandExecutor
 {
 	public RunsafeCommandHandler(ICommand command, IOutput output)
 	{
 		commandObject = command;
 		console = output;
 	}
 
 	public String getName()
 	{
 		return commandObject.getCommandName();
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] rawArgs)
 	{
 		String[] args = tokenizeArgs(rawArgs);
 
 		if (sender instanceof Player)
 		{
			console.write(String.format("[PLAYER_COMMAND] /%s %s", label, StringUtils.join(rawArgs, " ")));
 			if (commandObject.requiredPermission() != null && !sender.hasPermission(commandObject.requiredPermission()))
 			{
 				sender.sendMessage(ChatColor.RED + "No access to that command.");
 				return true;
 			}
 			return commandObject.Execute(new RunsafePlayer((Player) sender), args);
 		}
 		else
		{
			console.write(String.format("[CONSOLE_COMMAND] %s %s", label, StringUtils.join(rawArgs, " ")));
 			return commandObject.Execute(args);
		}
 	}
 
 	private String[] tokenizeArgs(String[] rawArgs)
 	{
 		ArrayList<String> args = new ArrayList<String>();
 		boolean combining = false;
 		String combined = "";
 		for(String arg : rawArgs)
 		{
 			if(combining)
 			{
 				if(arg.endsWith("\""))
 				{
 					arg = arg.substring(0, arg.length() - 1);
 					combining = false;
 				}
 				combined += " " + arg;
 				if(!combining)
 				{
 					args.add(combined);
 					combined = "";
 				}
 			}
 			else if(arg.startsWith("\""))
 			{
 				combining = true;
 				combined = arg.substring(1);
 			}
 			else
 			{
 				args.add(arg);
 			}
 		}
 		return args.toArray(new String[args.size()]);
 	}
 
 	private final ICommand commandObject;
 	private final IOutput console;
 }
