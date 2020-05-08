 package no.runsafe.command;
 
import no.runsafe.framework.api.IDebug;
 import no.runsafe.framework.api.command.argument.RequiredArgument;
 import no.runsafe.framework.api.command.console.ConsoleCommand;
import no.runsafe.framework.internal.Debug;
 import no.runsafe.framework.internal.InjectionPlugin;
 
 import javax.annotation.Nonnull;
 import java.util.Map;
 import java.util.logging.Level;
 
 public class DebugLevelCommand extends ConsoleCommand
 {
 	public DebugLevelCommand()
 	{
 		super(
 			"debuglevel", "Changes the output debug level for plugins",
 			new RequiredArgument("plugin"), new RequiredArgument("level")
 		);
 	}
 
 	@Nonnull
 	@Override
 	public String getUsageCommandParams()
 	{
 		return " *|<plugin> OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST|ALL";
 	}
 
 	@Override
 	public String OnExecute(Map<String, String> parameters)
 	{
 		String pluginName = parameters.get("plugin");
 		Level level = Level.parse(parameters.get("level"));
 
 		if ("*".equals(pluginName))
 			Debug.Global().setDebugLevel(level);
 
 		StringBuilder result = new StringBuilder(
 			String.format("Setting debug level for %s to %s:\n", pluginName, level)
 		);
 		for (InjectionPlugin plugin : InjectionPlugin.Instances.values())
 		{
 			if ("*".equals(pluginName) || plugin.getName().startsWith(pluginName))
 			{
 				IDebug debugger = plugin.getComponent(IDebug.class);
 				debugger.setDebugLevel(level);
 				result.append(String.format("[%s] Debug level is %s.\n", plugin.getName(), debugger.getDebugLevel().getName()));
 			}
 		}
 		return result.toString();
 	}
 }
