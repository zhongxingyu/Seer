 /*******************************************************************************
  Copyright (c) 2013 James Richardson.
 
  HelpCommand.java is part of BukkitUtilities.
 
  BukkitUtilities is free software: you can redistribute it and/or modify it
  under the terms of the GNU General Public License as published by the Free
  Software Foundation, either version 3 of the License, or (at your option) any
  later version.
 
  BukkitUtilities is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License along with
  BukkitUtilities. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package name.richardson.james.bukkit.utilities.command;
 
 import java.lang.ref.WeakReference;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.PluginDescriptionFile;
 
 import name.richardson.james.bukkit.utilities.colours.ColourScheme;
 import name.richardson.james.bukkit.utilities.colours.CoreColourScheme;
 import name.richardson.james.bukkit.utilities.command.argument.CommandArgument;
 import name.richardson.james.bukkit.utilities.command.argument.InvalidArgumentException;
 import name.richardson.james.bukkit.utilities.localisation.LocalisedCoreColourScheme;
 
 @CommandArguments(arguments = {CommandArgument.class})
 public class HelpCommand extends AbstractCommand {
 
 	final static private ChatColor REQUIRED_ARGUMENT_COLOUR = ChatColor.YELLOW;
 	final static private ChatColor OPTIONAL_ARGUMENT_COLOUR = ChatColor.GREEN;
 	private final String label;
 	private final ColourScheme localisedScheme;
 	private final String pluginDescription;
 	private final String pluginName;
 	private final ColourScheme scheme;
 	private String commandName;
 	private Map<String, Command> commands = new HashMap<String, Command>();
 	private WeakReference<CommandSender> sender;
 
 	public HelpCommand(final String label, final PluginDescriptionFile description) {
 		this.label = label;
 		this.pluginName = description.getFullName();
 		this.pluginDescription = description.getDescription();
 		this.scheme = new CoreColourScheme();
 		this.localisedScheme = new LocalisedCoreColourScheme(this.getResourceBundle());
 	}
 
 	public void execute(final List<String> arguments, final CommandSender sender) {
 		this.sender = new WeakReference<CommandSender>(sender);
 		this.parseArguments(arguments);
 		if (commands.containsKey(commandName) && commands.get(commandName).isAuthorized(sender)) {
 			Command command = commands.get(commandName);
 			String message = this.scheme.format(ColourScheme.Style.HEADER, command.getDescription());
 			this.sender.get().sendMessage(message);
 			message = this.localisedScheme.format(ColourScheme.Style.ERROR, "list-item", this.label, command.getName(), this.colouriseUsage(this.getUsage()));
 			this.sender.get().sendMessage(message);
 		} else {
 			String message = this.scheme.format(ColourScheme.Style.HEADER, this.pluginName);
 			this.sender.get().sendMessage(message);
 			this.sender.get().sendMessage(ChatColor.AQUA + this.pluginDescription);
			message = this.localisedScheme.format(ColourScheme.Style.WARNING, "usage-hint", "/" + this.label, this.getName());
 			this.sender.get().sendMessage(message);
 			for (final Command command : this.commands.values()) {
 				if (!command.isAuthorized(sender)) continue;
				message = this.localisedScheme.format(ColourScheme.Style.ERROR, "list-item",  "/" + this.label, command.getName(), this.colouriseUsage(this.getUsage()));
 				this.sender.get().sendMessage(message);
 			}
 		}
 	}
 
 	public void setCommands(Map<String, Command> commands) {
 		this.commands = commands;
 	}
 
 	protected void parseArguments(List<String> arguments) {
 		try {
 			super.parseArguments(arguments);
 			this.commandName = (String) this.getArguments().get(0).getValue();
 		} catch (InvalidArgumentException e) {
 			String message = this.scheme.format(ColourScheme.Style.ERROR, e.getMessage());
 			this.sender.get().sendMessage(message);
 		}
 	}
 
 	protected void setArguments() {
 		super.setArguments();
 		this.getArguments().get(0).setRequired(false);
 	}
 
 	private String colouriseUsage(String usage) {
 		usage = usage.replaceAll("<", HelpCommand.REQUIRED_ARGUMENT_COLOUR + "<");
 		usage = usage.replaceAll("\\[", HelpCommand.OPTIONAL_ARGUMENT_COLOUR + "\\[");
 		return usage;
 	}
 }
