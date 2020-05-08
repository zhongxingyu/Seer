 package net.catharos.lib.command;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.catharos.lib.cLib;
 import net.catharos.lib.util.ArrayUtil;
 import net.catharos.lib.util.MessageUtil;
 
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 public class CommandManager implements CommandExecutor {
 	protected final Map<String, Command> cmds;
 	protected final Map<Command, Method> methods;
 	protected final Map<Method, Object> instances;
 	protected final Map<JavaPlugin, List<Command>> plugins;
 	
 	
 	public CommandManager() {
 		this.cmds = new HashMap<String, Command>();
 		this.methods = new HashMap<Command, Method>();
 		this.instances = new HashMap<Method, Object>();
 		this.plugins = new HashMap<JavaPlugin, List<Command>>();
 	}
 	
 	public boolean registerCommands(Class clazz) {
 		return registerCommands(clazz, null);
 	}
 	
 	public boolean registerCommands(Class clazz, JavaPlugin plugin) {
 		Object obj;
 		
 		try { obj = clazz.newInstance(); }
 		catch (Exception ex) { return false; }
 		
 		return registerCommands(obj);
 	}
 	
 	public boolean registerCommands(Object obj) {
 		return this.registerCommands(obj, null);
 	}
 		
 	public boolean registerCommands(Object obj, JavaPlugin plugin) {
 		boolean success = true;
 
 		for(Method method : obj.getClass().getMethods()) {
 			try {
 				Command cmd = method.getAnnotation(Command.class);
 				if(cmd == null) continue;
 
 				Type returnType = method.getReturnType();
 				if(returnType != Boolean.TYPE && returnType != Void.TYPE) {
 					throw new Exception("Unsupported return type: " + returnType.toString());
 				}
 
 				Class<?>[] params = method.getParameterTypes();
 				if(params.length != 2 || (params[0] != CommandSender.class) || !(params[1].isArray())) {
 					throw new Exception("Method " + method.getName() + " does not match the required parameters!");
 				}
 
 				cmds.put(cmd.name().toLowerCase(), cmd);
 
 				for(String s : cmd.aliases()) {
 					cmds.put(s.toLowerCase(), cmd);
 				}
 
 				methods.put(cmd, method);
 				instances.put(method, obj);
 
 				if(plugin != null) {
 					List<Command> cmdList = plugins.get(plugin);
 					
 					if(cmdList == null) {
 						cmdList = new ArrayList<Command>();
 						plugins.put(plugin, cmdList);
 					}
 					
 					cmdList.add(cmd);
 				}
 
 			} catch(Exception e) {
 				cLib.getInstance().getLogger().warning(e.getMessage());
 				success = false;
 				continue;
 			}
 		}
 		
 		return success;
 	}
 
 	public boolean onCommand(CommandSender cs, org.bukkit.command.Command cmnd, String label, String[] args) {
 		String str;
 		Command cmd;
 		
 		for (int args_left = args.length; args_left >= 0; args_left--) {
 			// Build command string
 			str = label + ((args_left > 0) ? " " + ArrayUtil.implode( Arrays.copyOfRange( args, 0, args_left ), " " ) : "");
 			
 			cmd = getCommand(str);
 			
 			if(cmd != null) try {
 				// Check for console / player
 				if (!(cs instanceof Player) && !cmd.consoleCmd()) {
 					throw new Exception("The command cannot be executed from console." );
 				}
 				
 				// Check permisisons
 				if (!hasPermission( cs, cmd.permission() )) {
 					throw new Exception("You are not allowed to use that command!" );
 				}
 				
 				boolean help = (args.length > 0 && args[args.length - 1].equalsIgnoreCase( "?" )) ? true : false;
 
 				if (!help) {
 					String[] cmd_args = Arrays.copyOfRange( args, args_left, args.length );
 					
 					if (cmd_args.length < cmd.minArgs() || (cmd.maxArgs() >= 0 && cmd_args.length > cmd.maxArgs())) {
 						help = true;
 					} else {
 						Method method = methods.get(cmd);
 						
 						if(method == null) throw new Exception("Something went wrong: Method not found. Please call an admin!");
 						
 						if(method.getReturnType() == Boolean.TYPE) {
							help = !((Boolean) method.invoke(getInstance(method), cs, cmd_args));
 						} else {
							method.invoke(getInstance(method), cs, cmd_args);
 						}
 					}
 				}
 
 				if (help) {
 					MessageUtil.sendMessage( cs, "&6----------------=[ &cHelp: &3%0&6]=----------------", cmd.name() );
 
 					String desc = cmd.description();
 					MessageUtil.sendMessage( cs, "&6Description&f: %0", desc.isEmpty() ? "&7(no description available)" : desc );
 
 					String usage = cmd.usage();
 					MessageUtil.sendMessage( cs, "&6Usage: &f%0", usage.isEmpty() ? "/" + cmd.name() : usage );
 					
 					if(cmd.aliases().length > 0) MessageUtil.sendMessage( cs, "&6Aliases: &f/%0", ArrayUtil.implode( cmd.aliases(), ", /" ) );
 				}
 				
 				return true;
 				
 			} catch( Exception ex ) {
 				MessageUtil.sendError( cs, ex.getMessage());
 				return true;
 			}
 		}
 		
 		// Send error message
 		if (args.length > 0) {
 			MessageUtil.sendError( cs, "Command not found: &6%0 &7(/%1 %2)", args[args.length - 1], label, ArrayUtil.implode( args, " " ) );
 		} else {
 			MessageUtil.sendError( cs, "Command not found: &6%0", label );
 		}
 		
 		return true;
 	}
 	
 	public static boolean hasPermission( CommandSender sender, String perm ) {
 		if ((!(sender instanceof Player)) || (perm == null) || (perm.isEmpty())) return true;
 
 		Player player = (Player) sender;
 		return player.isOp() || player.hasPermission(perm);
 	}
 	
 	public Command getCommand( String label ) {
 		return cmds.get(label);
 	}
 	
 	private Object getInstance( Method m ) {
 		return instances.get(m);
 	}
 	
 	public List<Command> getCommands( JavaPlugin plugin ) {
 		return plugins.get(plugin);
 	}
 	
 }
