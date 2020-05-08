 package net.roguedraco.infobutton;
 
 import java.io.File;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 import net.roguedraco.infobutton.commands.GeneralCommands;
 import net.roguedraco.lang.Lang;
 import net.roguedraco.player.RDEvents;
 import net.roguedraco.player.RDPlayers;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.sk89q.bukkit.util.CommandsManagerRegistration;
 import com.sk89q.minecraft.util.commands.CommandException;
 import com.sk89q.minecraft.util.commands.CommandPermissionsException;
 import com.sk89q.minecraft.util.commands.CommandUsageException;
 import com.sk89q.minecraft.util.commands.CommandsManager;
 import com.sk89q.minecraft.util.commands.MissingNestedCommandException;
 import com.sk89q.minecraft.util.commands.WrappedCommandException;
 
 public class InfoButtonPlugin extends JavaPlugin {
 	private CommandsManager<CommandSender> commands;
 
 	public static Logger logger;
 
 	public static String pluginName;
 	public static String pluginVersion;
 
 	public static InfoButtonPlugin plugin = null;
 
 	public static Permission permission = null;
 	public static Economy economy = null;
 
 	public static Lang lang;
 
 	public void onEnable() {
 
 		this.getConfig().options().copyDefaults(true);
 		this.saveConfig();
 
 		InfoButtonPlugin.logger = Logger.getLogger("Minecraft");
 		InfoButtonPlugin.plugin = this;
 		InfoButtonPlugin.pluginName = this.getDescription().getName();
 		InfoButtonPlugin.pluginVersion = this.getDescription().getVersion();
 
 		InfoButtonPlugin.lang = new Lang(this);
 		lang.setupLanguage();
 
 		if (getServer().getPluginManager().getPlugin("Vault") != null) {
 			setupPermissions();
 			setupEconomy();
 		} else {
 			log(ChatColor.RED
 					+ "Missing dependency: Vault. Please install this for the plugin to work.");
 			getServer().getPluginManager().disablePlugin(plugin);
 			return;
 		}
 
 		// Create buttons folder
 		File theDir = new File(this.getDataFolder() + "/buttons/"
 				+ File.separatorChar);
 
 		// if the directory does not exist, create it
 		if (!theDir.exists()) {
 			boolean result = theDir.mkdir();
 			if (result) {
 				log("Buttons folder created.");
 			}
 		}
 
 		// Create players folder
 		theDir = new File(this.getDataFolder() + "/players/"
 				+ File.separatorChar);
 
 		// if the directory does not exist, create it
 		if (!theDir.exists()) {
 			boolean result = theDir.mkdir();
 			if (result) {
 				log("Players folder created.");
 			}
 		}
 
 		setupCommands();
 
 		PluginManager pm = getServer().getPluginManager();
 		Listener events = new Events();
 		pm.registerEvents(events, this);
 
 		Listener RDEvents = new RDEvents();
 		pm.registerEvents(RDEvents, this);
 		RDPlayers.loadAll();
 		InfoButtons.loadButtons();
 
 		log(Lang.get("plugin.enabled"));
 	}
 
 	public void onDisable() {
 		RDPlayers.saveAll();
 		InfoButtons.saveButtons();
		lang.saveLanguage();
 
 		log(Lang.get("plugin.disabled"));
 	}
 
 	public static JavaPlugin getPlugin() {
 		return plugin;
 	}
 
 	private boolean setupPermissions() {
 		RegisteredServiceProvider<Permission> permissionProvider = getServer()
 				.getServicesManager().getRegistration(
 						net.milkbowl.vault.permission.Permission.class);
 		if (permissionProvider != null) {
 			permission = permissionProvider.getProvider();
 		}
 		return (permission != null);
 	}
 
 	private boolean setupEconomy() {
 		RegisteredServiceProvider<Economy> economyProvider = getServer()
 				.getServicesManager().getRegistration(
 						net.milkbowl.vault.economy.Economy.class);
 		if (economyProvider != null) {
 			economy = economyProvider.getProvider();
 		}
 
 		return (economy != null);
 	}
 
 	private void setupCommands() {
 		this.commands = new CommandsManager<CommandSender>() {
 			@Override
 			public boolean hasPermission(CommandSender sender, String perm) {
 				return sender.hasPermission(perm);
 			}
 		};
 
 		CommandsManagerRegistration cmdRegister = new CommandsManagerRegistration(
 				this, this.commands);
 		cmdRegister.register(GeneralCommands.class);
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd,
 			String commandLabel, String[] args) {
 		try {
 			this.commands.execute(cmd.getName(), args, sender, sender);
 		} catch (CommandPermissionsException e) {
 			sender.sendMessage(ChatColor.RED
 					+ Lang.get("exceptions.noPermission"));
 		} catch (MissingNestedCommandException e) {
 			sender.sendMessage(ChatColor.RED + e.getUsage());
 		} catch (CommandUsageException e) {
 			sender.sendMessage(ChatColor.RED + e.getMessage());
 			sender.sendMessage(ChatColor.RED + e.getUsage());
 		} catch (WrappedCommandException e) {
 			if (e.getCause() instanceof NumberFormatException) {
 				sender.sendMessage(ChatColor.RED
 						+ Lang.get("exceptions.numExpected"));
 			} else {
 				sender.sendMessage(ChatColor.RED
 						+ Lang.get("exceptions.errorOccurred"));
 				e.printStackTrace();
 			}
 		} catch (CommandException e) {
 			sender.sendMessage(ChatColor.RED + e.getMessage());
 		}
 
 		return true;
 	}
 
 	public static void log(String message) {
 		log(Level.INFO, message);
 	}
 
 	public static void log(Level level, String message) {
 		if (plugin.getConfig().getBoolean("useFancyConsole") == true
 				&& level == Level.INFO) {
 			ConsoleCommandSender console = Bukkit.getServer()
 					.getConsoleSender();
 			console.sendMessage("[" + ChatColor.DARK_RED + pluginName
 					+ " v" + pluginVersion + ChatColor.GRAY + "] " + message);
 		} else {
 			InfoButtonPlugin.logger.log(level, "[" + pluginName + " v"
 					+ pluginVersion + "] " + message);
 		}
 	}
 
 	public static void debug(String message) {
 		if (plugin.getConfig().getBoolean("debug")) {
 			if (plugin.getConfig().getBoolean("useFancyConsole") == true) {
 				ConsoleCommandSender console = Bukkit.getServer()
 						.getConsoleSender();
 				console.sendMessage("[" + ChatColor.DARK_RED + pluginName
 						+ " v" + pluginVersion + " Debug" + ChatColor.GRAY
 						+ "] " + message);
 			} else {
 				System.out.println("[" + pluginName + " v" + pluginVersion
 						+ " Debug" + "] " + message);
 			}
 		}
 	}
 }
