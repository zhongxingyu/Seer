 package com.nodinchan.ncbukkit.command;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandMap;
 import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.nodinchan.ncbukkit.command.Command.Executor;
 import com.nodinchan.ncbukkit.command.info.CommandAlias;
 import com.nodinchan.ncbukkit.command.info.CommandDescription;
 import com.nodinchan.ncbukkit.command.info.CommandInfo;
 import com.nodinchan.ncbukkit.command.info.CommandPermission;
 import com.nodinchan.ncbukkit.command.info.CommandUsage;
 
 /*     Copyright (C) 2012  Nodin Chan <nodinchan@live.com>
  * 
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  * 
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  * 
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 public final class CommandManager implements CommandExecutor {
 	
 	private final JavaPlugin plugin;
 	
 	private final CommandMap commandMap;
 	
 	private final List<Command> queue;
 	
 	private final Map<Command, Map<String, String>> aliases;
 	private final Map<String, Command> commands;
 	private final Map<Command, Map<String, Executor>> executors;
 	
 	public CommandManager(JavaPlugin plugin) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
 		this.plugin = plugin;
		Field field = CraftServer.class.getDeclaredField("commandMap");
 		field.setAccessible(true);
 		this.commandMap = (CommandMap) field.get(plugin.getServer());
 		this.queue = new ArrayList<Command>();
 		this.aliases = new LinkedHashMap<Command, Map<String, String>>();
 		this.commands = new LinkedHashMap<String, Command>();
 		this.executors = new LinkedHashMap<Command, Map<String, Executor>>();
 	}
 	
 	public Executor getExecutor(Command command, String name) {
 		if (command == null || name == null)
 			return null;
 		
 		String subName = aliases.get(command).get(name.toLowerCase());
 		
 		if (subName == null)
 			return null;
 		
 		return executors.get(command).get(subName.toLowerCase());
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
 		if (commands.get(cmd.getName()) != null) {
 			Command command = commands.get(cmd.getName());
 			
 			if (args.length == 0) {
 				command.main(sender);
 				return true;
 				
 			} else {
 				if (getExecutor(command, args[0]) != null) {
 					Executor executor = getExecutor(command, args[0]);
 					
 					if (args.length < command.getClass().getAnnotation(CommandInfo.class).minArgs()) {
 						command.invalidArgLength(sender, command);
 						return true;
 					}
 					
 					if (args.length - 1 < executor.getMinimumArgumentLength()) {
 						command.invalidArgLength(sender, command, args[0]);
 						return true;
 					}
 					
 					int to = (args.length > executor.getMaximumArgumentLength()) ? executor.getMaximumArgumentLength() : args.length;
 					
 					String[] arguments = Arrays.copyOfRange(args, 1, to);
 					
 					try {
 						executor.execute(sender, arguments);
 						return true;
 						
 					} catch (IllegalAccessException e) {
 						sender.sendMessage("[" + plugin.getName() + "] " + ChatColor.RED + "An error seems to have occured, please check console");
 						plugin.getLogger().log(Level.SEVERE, "[" + plugin.getName() + "] An IllgealAccessException has occured while using command: " + executor.getName());
 						e.printStackTrace();
 						
 					} catch (IllegalArgumentException e) {
 						sender.sendMessage("[" + plugin.getName() + "] " + ChatColor.RED + "An error seems to have occured, please check console");
 						plugin.getLogger().log(Level.SEVERE, "[" + plugin.getName() + "] An IllgealArgumentException has occured while using command: " + executor.getName());
 						e.printStackTrace();
 						
 					} catch (InvocationTargetException e) {
 						sender.sendMessage("[" + plugin.getName() + "] " + ChatColor.RED + "An error seems to have occured, please check console");
 						plugin.getLogger().log(Level.SEVERE, "[" + plugin.getName() + "] An InvocationTargetException has occured while using command: " + executor.getName());
 						e.printStackTrace();
 					}
 				}
 			}
 			
 			sender.sendMessage(command.invalidCommand(sender));
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public PluginCommand register(String cmd) {
 		PluginCommand command = new PluginCommand(cmd, plugin);
 		return command;
 	}
 	
 	public org.bukkit.command.Command regsiterCommand(Command cmd) {
 		CommandAlias alias = cmd.getClass().getAnnotation(CommandAlias.class);
 		CommandDescription description = cmd.getClass().getAnnotation(CommandDescription.class);
 		CommandInfo info = cmd.getClass().getAnnotation(CommandInfo.class);
 		CommandUsage usage = cmd.getClass().getAnnotation(CommandUsage.class);
 		CommandPermission permission = cmd.getClass().getAnnotation(CommandPermission.class);
 		
 		if (info == null)
 			return null;
 		
 		PluginCommand command = new PluginCommand(info.name(), plugin);
 		
 		if (alias != null)
 			command.setAliases(Arrays.asList(alias.aliases()));
 		
 		if (description != null)
 			command.setDescription(description.description());
 		
 		if (usage != null)
 			command.setUsage(usage.usage());
 		
 		if (permission != null)
 			command.setPermission(permission.permission());
 		
 		command.setExecutor(this);
 		
 		if (cmd.isMain())
 			commands.put(command.getName(), cmd);
 		
 		queue.add(cmd);
 		
 		for (Iterator<Command> commands = queue.iterator(); commands.hasNext();) {
 			if (this.commands.get(command.getName()) != null) {
 				register(this.commands.get(command.getName()), commands.next());
 				commands.remove();
 			}
 		}
 		
 		if (commandMap.register(command.getName(), plugin.getDescription().getPrefix(), command))
 			return command;
 		else
 			return plugin.getCommand(command.getName());
 	}
 	
 	private void register(Command main, Command cmd) {
 		for (Method method : cmd.getClass().getMethods()) {
 			if (method.getAnnotation(CommandInfo.class) == null)
 				continue;
 			
 			Executor executor = new Executor(cmd, method);
 			
 			if (aliases.get(main) == null)
 				aliases.put(main, new LinkedHashMap<String, String>());
 			
 			if (executors.get(main) == null)
 				executors.put(main, new LinkedHashMap<String, Executor>());
 			
 			aliases.get(main).put(executor.getName(), executor.getName());
 			
 			for (String cmdAlias : executor.getAliases())
 				aliases.get(main).put(cmdAlias, executor.getName());
 			
 			executors.get(main).put(executor.getName(), executor);
 		}
 	}
 }
