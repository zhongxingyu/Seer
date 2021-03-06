 package com.earth2me.essentials;
 
 import java.util.*;
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.command.PluginCommandYamlParser;
 import org.bukkit.plugin.Plugin;
 
 
 public class AlternativeCommandsHandler
 {
 	private final transient Map<String, List<PluginCommand>> altcommands = new HashMap<String, List<PluginCommand>>();
 	private final transient IEssentials ess;
 	
 	public AlternativeCommandsHandler(final IEssentials ess)
 	{
 		this.ess = ess;
 		for (Plugin plugin : ess.getServer().getPluginManager().getPlugins())
 		{
 			if (plugin.isEnabled()) {
 				addPlugin(plugin);
 			}
 		}
 	}
 	
 	public final void addPlugin(final Plugin plugin)
 	{
 		if (plugin.getDescription().getMain().contains("com.earth2me.essentials"))
 		{
 			return;
 		}
 		final List<Command> commands = PluginCommandYamlParser.parse(plugin);
 		final String pluginName = plugin.getDescription().getName().toLowerCase(Locale.ENGLISH);
 
 		for (Command command : commands)
 		{
 			final PluginCommand pc = (PluginCommand)command;
 			final List<String> labels = new ArrayList<String>(pc.getAliases());
 			labels.add(pc.getName());
 
 			PluginCommand reg = ess.getServer().getPluginCommand(pluginName + ":" + pc.getName().toLowerCase(Locale.ENGLISH));
 			if (reg == null)
 			{
				reg = Bukkit.getServer().getPluginCommand(pc.getName().toLowerCase(Locale.ENGLISH));
 			}
 			for (String label : labels)
 			{
 				List<PluginCommand> plugincommands = altcommands.get(label.toLowerCase(Locale.ENGLISH));
 				if (plugincommands == null)
 				{
 					plugincommands = new ArrayList<PluginCommand>();
 					altcommands.put(label.toLowerCase(Locale.ENGLISH), plugincommands);
 				}
 				boolean found = false;
 				for (PluginCommand pc2 : plugincommands)
 				{
 					if (pc2.getPlugin().equals(plugin))
 					{
 						found = true;
 					}
 				}
 				if (!found)
 				{
 					plugincommands.add(reg);
 				}
 			}
 		}
 	}
 
 	public void removePlugin(final Plugin plugin)
 	{
 		final Iterator<Map.Entry<String, List<PluginCommand>>> iterator = altcommands.entrySet().iterator();
 		while (iterator.hasNext())
 		{
 			final Map.Entry<String, List<PluginCommand>> entry = iterator.next();
 			final Iterator<PluginCommand> pcIterator = entry.getValue().iterator();
 			while (pcIterator.hasNext())
 			{
 				final PluginCommand pc = pcIterator.next();
 				if (pc.getPlugin() == null || pc.getPlugin().equals(plugin))
 				{
 					pcIterator.remove();
 				}
 			}
 			if (entry.getValue().isEmpty())
 			{
 				iterator.remove();
 			}
 		}
 	}
 
 	public PluginCommand getAlternative(final String label)
 	{
 		final List<PluginCommand> commands = altcommands.get(label);
 		if (commands == null || commands.isEmpty())
 		{
 			return null;
 		}
 		if (commands.size() == 1)
 		{
 			return commands.get(0);
 		}
 		// return the first command that is not an alias
 		for (PluginCommand command : commands) {
 			if (command.getName().equalsIgnoreCase(label)) {
 				return command;
 			}
 		}
 		// return the first alias
 		return commands.get(0);
 	}
 }
