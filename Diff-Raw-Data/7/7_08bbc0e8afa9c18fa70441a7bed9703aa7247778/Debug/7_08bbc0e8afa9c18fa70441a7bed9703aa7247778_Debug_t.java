 package no.runsafe.framework.internal;
 
 import no.runsafe.framework.api.IDebug;
 import no.runsafe.framework.internal.wrapper.item.BukkitItemStack;
 import no.runsafe.framework.text.ConsoleColour;
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 import java.util.logging.Level;
 
 public class Debug extends Console implements IDebug
 {
 	public Debug()
 	{
 		this(null);
 	}
 
 	public Debug(InjectionPlugin plugin)
 	{
 		super(plugin);
		if (plugin != null)
		{
			debugLevel = DefaultDebugLevel(plugin.getName());
			debugFine("Setting debug level to %s", debugLevel.getName());
		}
 	}
 
 	// Sends the supplied String to the console/log the output handler has if the debug level is high enough
 	@Override
 	public final void outputDebugToConsole(String message, Level messageLevel, Object... params)
 	{
 		if (debugLevel != null && messageLevel.intValue() >= debugLevel.intValue())
 			InternalDebugger.log(Level.INFO, formatDebugMessage(message, messageLevel, params), this);
 	}
 
 	// Gets the current debug output level
 	@Override
 	public Level getDebugLevel()
 	{
 		return debugLevel;
 	}
 
 	// Sets the debug output level
 	@Override
 	public void setDebugLevel(Level level)
 	{
 		debugLevel = level;
 	}
 
 	@Override
 	public void debugSevere(String message, Object... params)
 	{
 		outputDebugToConsole(message, Level.SEVERE, params);
 	}
 
 	@Override
 	public void debugWarning(String message, Object... params)
 	{
 		outputDebugToConsole(message, Level.WARNING, params);
 	}
 
 	@Override
 	public void debugInfo(String message, Object... params)
 	{
 		outputDebugToConsole(message, Level.INFO, params);
 	}
 
 	@Override
 	public void debugConfig(String message, Object... params)
 	{
 		outputDebugToConsole(message, Level.CONFIG, params);
 	}
 
 	@Override
 	public void debugFine(String message, Object... params)
 	{
 		outputDebugToConsole(message, Level.FINE, params);
 	}
 
 	@Override
 	public void debugFiner(String message, Object... params)
 	{
 		outputDebugToConsole(message, Level.FINER, params);
 	}
 
 	@Override
 	public void debugFinest(String message, Object... params)
 	{
 		outputDebugToConsole(message, Level.FINEST, params);
 	}
 
 	@Override
 	public void debugDump(Object object, Level messageLevel)
 	{
 		if (debugLevel != null && messageLevel.intValue() >= debugLevel.intValue())
 			if (object instanceof BukkitItemStack)
 				dumpData(((BukkitItemStack) object).getRaw(), messageLevel);
 	}
 
 	private String formatDebugMessage(String message, Level messageLevel, Object... params)
 	{
 		String formatted = String.format(
 			"[%s%s%s] %s",
 			ConsoleColour.DARK_GREEN,
 			messageLevel.getName(),
 			ConsoleColour.RESET,
 			String.format(message, params)
 		);
 
 		if (debugLevel.intValue() <= Level.FINEST.intValue())
 			formatted = String.format("%s\nat %s", formatted, getStackTrace());
 
 		return formatted;
 	}
 
 	private void dumpData(ConfigurationSerializable raw, Level level)
 	{
 		outputDebugToConsole("Dumping instance of %s", level, raw.getClass().getCanonicalName());
 		Map<String, Object> values = raw.serialize();
 		for (Map.Entry<String, Object> entry : values.entrySet())
 			outputDebugToConsole(" - %s: %s", level, entry.getKey(), entry.getValue());
 	}
 
 	private static String getStackTrace()
 	{
 		int skip = 5;
 		Collection<String> stack = new ArrayList<String>(5);
 		for (StackTraceElement element : Thread.currentThread().getStackTrace())
 		{
 			if (skip < 1)
 				stack.add(element.toString());
 			else
 				skip--;
 		}
 		return StringUtils.join(stack, "\n\t");
 	}
 
 	protected Level debugLevel;
 }
