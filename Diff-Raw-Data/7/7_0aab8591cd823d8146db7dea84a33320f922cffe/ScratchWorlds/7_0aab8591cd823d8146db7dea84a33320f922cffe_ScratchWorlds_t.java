 package com.lithium3141.ScratchWorlds;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class ScratchWorlds extends JavaPlugin {
 	
 	// Logging items
 	public static final Logger LOG = Logger.getLogger("Minecraft");
 	public static final String LOG_PREFIX = "[ScratchWorlds] ";
 	
 	// Command name
 	public static final String COMMAND_NAME = "scratch";
 	
 	// Config info
 	public static final String CONFIG_FILE_NAME = "ScratchWorlds.yml";
 	public static final String CONFIG_WORLD_LIST_KEY = "worlds";
 	
 	// Active config variables
 	public Configuration swConfig;
 	public List<String> scratchWorldNames = new ArrayList<String>();
 	
 	// Permissions interface
 	public PermissionHandler permissionHandler;
 	public static final String PERMISSION_ROOT_NAME = "ScratchWorlds";
 
 	@Override
 	public void onDisable() {
 		// Save config
 		this.swConfig.setProperty(CONFIG_WORLD_LIST_KEY, this.scratchWorldNames);
 		if(!this.swConfig.save()) {
 			LOG.warning(LOG_PREFIX + "Couldn't save configuration file! Continuing anyway...");
 		}
 		
 		LOG.info(LOG_PREFIX + "Version " + this.getDescription().getVersion() + " disabled");
 	}
 
 	@Override
 	public void onEnable() {
 		// Initialize Permissions system
 		this.setupPermissions();
 		
 		// Set up configuration folder
 		this.getDataFolder().mkdirs();
 		
 		// Read configuration file
 		this.swConfig = new Configuration(new File(this.getDataFolder(), CONFIG_FILE_NAME));
 		this.swConfig.load();
 		this.scratchWorldNames = swConfig.getStringList("worlds", new ArrayList<String>());
 		
 		LOG.info(LOG_PREFIX + "Version " + this.getDescription().getVersion() + " enabled");
 	}
 	
 	private void setupPermissions() {
 	      Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
 
 	      if(this.permissionHandler == null) {
 	          if(permissionsPlugin != null) {
 	              this.permissionHandler = ((Permissions) permissionsPlugin).getHandler();
 	              LOG.info(LOG_PREFIX + "Hooked into Permissions version " + permissionsPlugin.getDescription().getVersion());
 	          } else {
 	              LOG.info(LOG_PREFIX + "Permissions system not detected; allowing all commands");
 	          }
 	      }
 	  }
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if(command.getName().equalsIgnoreCase(COMMAND_NAME)) {
 			if(args.length == 0) {
 				// TODO be more verbose about this error
 				sender.sendMessage(ChatColor.RED + "No subcommand given");
 				return true;
 			}
 			
 			// Parse out the command
 			String subcommand = args[0];
 			String[] subargs = new String[args.length - 1];
 			for(int i = 0; i < subargs.length; i++) {
 				subargs[i] = args[i + 1];
 			}
 			
 			// Check permission
 			if(!this.checkPermission(sender, PERMISSION_ROOT_NAME + "." + subcommand)) {
 				sender.sendMessage(ChatColor.RED + "You do not have the necessary permission to run /" + COMMAND_NAME + " " + subcommand);
 			}
 			
 			// Dispatch the command
 			try {
 				// Reflection magic: find the class for the invoked command, instantiate it, and call its execute() method
 				Class<?> klass = Class.forName("com.lithium3141.ScratchWorlds.commands.SW" + this.capitalize(subcommand) + "Command");
 				SWCommand commandInstance = (SWCommand)klass.getConstructor(Class.forName("com.lithium3141.ScratchWorlds.ScratchWorlds")).newInstance(this);
 				commandInstance.execute(sender, subargs);
 			} catch (ClassNotFoundException e) {
 				LOG.warning("Unknown /" + COMMAND_NAME + " subcommand: " + subcommand);
 			} catch (IllegalArgumentException e) {
 				LOG.severe(LOG_PREFIX + "Error passing arguments to command invocation! Is your plugin JAR corrupted?");
 			} catch (IllegalAccessException e) {
 				LOG.severe(LOG_PREFIX + "Access error invoking command! Is your plugin JAR corrupted?");
 			} catch (InvocationTargetException e) {
 				LOG.severe(LOG_PREFIX + "Target error invoking command! Is your plugin JAR corrupted?");
 			} catch (SecurityException e) {
 				LOG.severe(LOG_PREFIX + "Security error invoking command! Is your plugin JAR corrupted?");
 			} catch (InstantiationException e) {
 				LOG.severe(LOG_PREFIX + "Instantiation error invoking command! Is your plugin JAR corrupted?");
 			} catch (NoSuchMethodException e) {
 				LOG.severe(LOG_PREFIX + "Method location error invoking command! Is your plugin JAR corrupted?");
 			}
 			return true;
 		}
 		
 		return false;
 	}
 	
 	private String capitalize(String arg) {
 		return arg.substring(0, 1).toUpperCase() + arg.substring(1).toLowerCase();
 	}
 	
 	boolean checkPermission(CommandSender sender, String permission) {
 		if(sender instanceof Player) {
			if(this.permissionHandler == null) {
				// No permissions; allow
				return true;
			} else {
				return this.permissionHandler.has((Player)sender, permission);
			}
 		} else {
 			// Running from console; always allow
 			return true;
 		}
 	}
 
 }
