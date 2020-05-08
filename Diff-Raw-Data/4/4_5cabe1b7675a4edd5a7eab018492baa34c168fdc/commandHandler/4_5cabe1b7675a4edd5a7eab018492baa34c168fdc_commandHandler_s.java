 package com.Zolli.EnderCore.Commands;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 
 import com.Zolli.EnderCore.EnderCore;
 
 public class commandHandler implements CommandExecutor {
 	
 	/**
 	 * The map storing all comands handled by this class
 	 */
 	private Map<String, ECCommand> handledCommands = new HashMap<String, ECCommand>();
 	
 	/**
 	 * The main class instance
 	 */
 	private EnderCore plugin;
 	
 	/**
 	 * Constructor
 	 * @param instance Tha main class
 	 */
 	public commandHandler(EnderCore instance) {
 		this.plugin = instance;
 	}
 	
 	/**
 	 * Register a command for handling. Put it's name to handledComamnds map
 	 * @param name the command name
 	 * @param command The ECCommand object
 	 */
 	public void registerCommand(String name, ECCommand command) {
 		this.handledCommands.put(name, command);
 	}
 	
 	/**
 	 * Build arguments and chain the rest pieces of the array
 	 * The parameters length is defined by the ECCommand object itself on the
 	 * getArgsLength() function
 	 * @param str All arguments in a string array
 	 * @param length The length of required argument
 	 * @return String chained parameters
 	 */
 	private String buildArgs(String[] str, int length) {
 		int arrayLength = str.length;
 		String param = "";
 		for(int i = length+1 ; i < arrayLength ; i++ ) {
 			param = param + str[i] + " ";
 		}
 		
 		return param;
 	}
 	
 	/**
 	 * Check all defined permission for the command sender
 	 * @param nodes A list of permission nodes
 	 * @param sender CommandSender object
 	 * @return Boolean False if CommandSender does not have the defined permission
 	 */
 	private boolean checkPerm(List<String> nodes, CommandSender sender) {
 		for(String node : nodes) {
 			if(!this.plugin.permission.has(sender, node)) {
 				sender.sendMessage(this.plugin.local.getLocalizedString("commands.noPermission"));
 				this.sendPermissions(nodes, sender);
 				return false;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Send example commands defined in ECCOmmand class
 	 * @param examples Command examples
 	 * @param sender CommandSender object
 	 */
 	private void sendCommandExamples(List<String> examples, CommandSender sender) {
 		for(String s : examples) {
 			sender.sendMessage(" - " + s);
 		}
 	}
 	
 	private void sendPermissions(List<String> permissions, CommandSender sender) {
 		for(String s : permissions) {
 			sender.sendMessage(" - " + s);
 		}
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3) {
 		ECCommand command = this.handledCommands.get(arg3[0]);
 		String chainedParam = this.buildArgs(arg3, command.getArgsLength());
 		
 		if(command.getArgsLength() > arg3.length-1) {
 			sender.sendMessage(this.plugin.local.getLocalizedString("commands.badUsage"));
 			this.sendCommandExamples(command.getExample(), sender);
 		} else {
 			if(sender instanceof ConsoleCommandSender) {
 				if(command.isAccessibleFromConsole()) {
 					if(!this.checkPerm(command.getPermission(), sender)) {
 						return false;
 					}
 					command.execute(sender, arg3, chainedParam);
 				} else {
 					sender.sendMessage(this.plugin.local.getLocalizedString("commands.noConsole"));
 				}
 			} else {
 				if(!this.checkPerm(command.getPermission(), sender)) {
 					return false;
 				}
 				command.execute(sender, arg3, chainedParam);
 			}
 		}
 		return false;
 	}
 	
 	
 	
 }
