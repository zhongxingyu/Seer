 /* Copyright 2013 Kevin Seiden. All rights reserved.
 
  This works is licensed under the Creative Commons Attribution-NonCommercial 3.0
 
  You are Free to:
     to Share: to copy, distribute and transmit the work
     to Remix: to adapt the work
 
  Under the following conditions:
     Attribution: You must attribute the work in the manner specified by the author (but not in any way that suggests that they endorse you or your use of the work).
     Non-commercial: You may not use this work for commercial purposes.
 
  With the understanding that:
     Waiver: Any of the above conditions can be waived if you get permission from the copyright holder.
     Public Domain: Where the work or any of its elements is in the public domain under applicable law, that status is in no way affected by the license.
     Other Rights: In no way are any of the following rights affected by the license:
         Your fair dealing or fair use rights, or other applicable copyright exceptions and limitations;
         The author's moral rights;
         Rights other persons may have either in the work itself or in how the work is used, such as publicity or privacy rights.
 
  Notice: For any reuse or distribution, you must make clear to others the license terms of this work. The best way to do this is with a link to this web page.
  http://creativecommons.org/licenses/by-nc/3.0/
  */
 
 package io.github.alshain01.Flags;
 
 import io.github.alshain01.Flags.Updater.UpdateResult;
 import io.github.alshain01.Flags.commands.Command;
 import io.github.alshain01.Flags.data.*;
 import io.github.alshain01.Flags.events.PlayerChangedAreaEvent;
 import io.github.alshain01.Flags.importer.GPFImport;
 import io.github.alshain01.Flags.metrics.MetricsManager;
 
 import java.util.List;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredListener;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitRunnable;
 
 /**
  * Flags
  * 
  * @author Alshain01
  */
 public class Flags extends JavaPlugin {
 	protected static CustomYML messageStore;
 	protected static System currentSystem = System.WORLD;
 	private static DataStore dataStore;
 	private static Updater updater = null;
 	private static Economy economy = null;
 	private static boolean debugOn = false;
 
 	private static Registrar flagRegistrar = new Registrar();
 
 	private static boolean borderPatrol = false;
 	
 	/**
 	 * Called when this plug-in is enabled
 	 */
 	@Override
 	public void onEnable() {
 		// Create the configuration file if it doesn't exist
 		saveDefaultConfig();
 		debugOn = getConfig().getBoolean("Flags.Debug");
 
 		if (getConfig().getBoolean("Flags.Update.Check")) {
             debug("Enabling Update Scheduler");
 			new UpdateScheduler().runTaskTimer(this, 0, 1728000);
 		}
 
 		borderPatrol = getConfig().getBoolean("Flags.BorderPatrol.Enable");
 
 		// Create the specific implementation of DataStore
 		(messageStore = new CustomYML(this, "message.yml")).saveDefaultConfig();
 
 		// Find the first available land management system
 		currentSystem = findSystem(getServer().getPluginManager());
 		log(currentSystem == System.WORLD ? "No system detected. Only world flags will be available."
 						: currentSystem.getDisplayName() + " detected. Enabling integrated support.");
 
 		// Check for older database and import as necessary.
 		if (currentSystem == System.GRIEF_PREVENTION
 				&& !getServer().getPluginManager().isPluginEnabled("GriefPreventionFlags")) {
 			GPFImport.importGPF();
 		}
 
         dataStore = DataStoreType.getType(getConfig().getString("Flags.Database.Url")).getDataStore(this);
 
         // New installation
         if (!dataStore.create(this)) {
             severe("Failed to create database schema. Shutting down Flags.");
             getServer().getPluginManager().disablePlugin(this);
             return;
         }
 		dataStore.update(this);
 
 		// Enable Vault support
 		setupEconomy();
 
 		// Load Mr. Clean
 		MrClean.enable(this);
 
 		// Load Border Patrol
 		if (borderPatrol) {
 			debug("Registering for PlayerMoveEvent");
 			BorderPatrol bp = new BorderPatrol(getConfig().getInt("Flags.BorderPatrol.EventDivisor"), getConfig().getInt("Flags.BorderPatrol.TimeDivisor"));
 			getServer().getPluginManager().registerEvents(bp, this);
 		}
 
         // Tell Bukkit about the existing bundles
         Bundle.registerPermissions();
 
 		// Schedule tasks to perform after server is running
 		new onServerEnabledTask(this.getConfig().getBoolean("Flags.Metrics.Enabled")).runTask(this);
 		log("Flags Has Been Enabled.");
 	}
 
 	/**
 	 * Executes the given command, returning its success
 	 * 
 	 * @param sender
 	 *            Source of the command
 	 * @param cmd
 	 *            Command which was executed
 	 * @param label
 	 *            Alias of the command which was used
 	 * @param args
 	 *            Passed command arguments
 	 * @return true if a valid command, otherwise false
 	 * 
 	 */
 	@Override
 	public boolean onCommand(CommandSender sender,
 			org.bukkit.command.Command cmd, String label, String[] args) {
         if (cmd.getName().equalsIgnoreCase("flags")) {
             if(args.length < 1) {
                 return false;
             }
 
             if(args[0].equalsIgnoreCase("reload")) {
                 this.reload();
             }
 
             if(args[0].equalsIgnoreCase("import")) {
                 if(dataStore instanceof SQLDataStore) {
                     ((SQLDataStore)dataStore).importDB();
                     return true;
                 }
                 sender.sendMessage(Message.SQLDatabaseError.get());
                 return true;
             }
 
             /*
             if(args[0].equalsIgnoreCase("export")) {
                 if(dataStore instanceof SQLDataStore) {
                     ((SQLDataStore)dataStore).exportDB();
                     return true;
                 }
                 sender.sendMessage(Message.SQLDatabaseError.get());
                 return true;
             }
             */
 
             return false;
         }
 		if (cmd.getName().equalsIgnoreCase("flag")) {
 			return Command.onFlagCommand(sender, args);
 		}
 
 		return (cmd.getName().equalsIgnoreCase("bundle") && Command.onBundleCommand(sender, args));
 	}
 
 	/**
 	 * Called when this plug-in is disabled
 	 */
 	@Override
 	public void onDisable() {
 		if(dataStore instanceof SQLDataStore) {
              ((SQLDataStore)dataStore).close();
         }
 		
 		// Static cleanup
 		economy = null;
 		dataStore = null;
 		updater = null;
 		messageStore = null;
 		flagRegistrar = null;
 		currentSystem = null;
 
 		log("Flags Has Been Disabled.");
 	}
 	
 	/*
 	 * Contains event listeners required for plugin maintenance.
 	 */
 	private class FlagsListener implements Listener {
 		// Update listener
 		@EventHandler(ignoreCancelled = true)
 		private void onPlayerJoin(PlayerJoinEvent e) {
 			if (e.getPlayer().hasPermission("flags.admin.notifyupdate")) {
 				if(updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
 					e.getPlayer().sendMessage(ChatColor.DARK_PURPLE
 							+ "The version of Flags that this server is running is out of date. "
 							+ "Please consider updating to the latest version at dev.bukkit.org/bukkit-plugins/flags/.");
 				} else if(updater.getResult() == UpdateResult.SUCCESS) {
 					e.getPlayer().sendMessage("[Flags] " + ChatColor.DARK_PURPLE
 							+ "An update to Flags has been downloaded and will be installed when the server is reloaded.");
 				}
 			}
 		}
 	}
 
 	/*
 	 * Tasks that must be run only after the entire sever has loaded. Runs on
 	 * first server tick.
 	 */
 	private class onServerEnabledTask extends BukkitRunnable {
         private onServerEnabledTask(boolean mcStats) {
             this.mcStats = mcStats;
         }
 
         private final boolean mcStats;
 
 		@Override
 		public void run() {
 			for (final String b : Flags.getDataStore().readBundles()) {
 				debug("Registering Bundle Permission:" + b);
 				final Permission perm = new Permission("flags.bundle." + b,
 						"Grants ability to use the bundle " + b,
 						PermissionDefault.FALSE);
 				perm.addParent("flags.bundle", true);
 				Bukkit.getServer().getPluginManager().addPermission(perm);
 			}
 
 			if (mcStats && !debugOn && checkAPI("1.3.2")) {
 				MetricsManager.StartMetrics(Bukkit.getServer().getPluginManager().getPlugin("Flags"));
 			}
 
 			// Check the handlers to see if anything is registered for Border
 			// Patrol
 			final RegisteredListener[] listeners = PlayerChangedAreaEvent.getHandlerList().getRegisteredListeners();
 			if (borderPatrol && (listeners == null || listeners.length == 0)) {
                 PlayerMoveEvent.getHandlerList().unregister(Bukkit.getPluginManager().getPlugin("Flags"));
 				Flags.log("No plugins have registered for Flags' Border Patrol listener. Unregistering PlayerMoveEvent.");
 			}
 		}
 	}
 	
 	private class UpdateScheduler extends BukkitRunnable {
 		@Override
 		public void run() {
 			// Update script
 			final String key = getConfig().getString("Flags.Update.ServerModsAPIKey");
 			final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Flags");
 			updater = (getConfig().getBoolean("Flags.Update.Download"))
 				? new Updater(plugin, 65024, getFile(), Updater.UpdateType.DEFAULT, key, true)
 				: new Updater(plugin, 65024, getFile(), Updater.UpdateType.NO_DOWNLOAD, key, false);
 
 			if (updater.getResult() == UpdateResult.UPDATE_AVAILABLE) {
 				Bukkit.getServer().getConsoleSender()
 					.sendMessage("[Flags] "	+ ChatColor.DARK_PURPLE
 						+ "The version of Flags that this server is running is out of date. "
 						+ "Please consider updating to the latest version at dev.bukkit.org/bukkit-plugins/flags/.");
 			} else if (updater.getResult() == UpdateResult.SUCCESS) {
 				Bukkit.getServer().getConsoleSender()
 				.sendMessage("[Flags] "	+ ChatColor.DARK_PURPLE
 					+ "An update to Flags has been downloaded and will be installed when the server is reloaded.");
 			}
 			getServer().getPluginManager().registerEvents(new FlagsListener(), plugin);
 		}
 	}
 
     private void reload() {
         this.reloadConfig();
         debugOn = getConfig().getBoolean("Flags.Debug");
 
         messageStore.reload();
         dataStore.reload();
         log("Flag Database Reloaded");
     }
 
 	/**
 	 * Checks if the provided string represents a version number that is equal
 	 * to or lower than the current Bukkit API version.
 	 * 
 	 * String should be formatted with 3 numbers: x.y.z
 	 * 
 	 * @return true if the version provided is compatible
 	 */
 	public static boolean checkAPI(String version) {
 		final float APIVersion = Float.valueOf(Bukkit.getServer().getBukkitVersion().substring(0, 3));
 		final float CompareVersion = Float.valueOf(version.substring(0, 3));
 		final int APIBuild = Integer.valueOf(Bukkit.getServer().getBukkitVersion().substring(4, 5));
 		final int CompareBuild = Integer.valueOf(version.substring(4, 5));
 
 		return (APIVersion > CompareVersion 
 				|| APIVersion == CompareVersion	&& APIBuild >= CompareBuild);
 	}
 
     /**
 	 * Sends a severe message through the Flags logger.
 	 * 
 	 * @param message
 	 *            The message
 	 */
 	public static void severe(String message) {
 		Bukkit.getServer().getPluginManager().getPlugin("Flags").getLogger().severe(message);
 	}
 	
 	/**
 	 * Sends a warning message through the Flags logger.
 	 * 
 	 * @param message
 	 *            The message
 	 */
 	public static void warn(String message) {
 		Bukkit.getServer().getPluginManager().getPlugin("Flags").getLogger().warning(message);
 	}
 	
 	/**
 	 * Sends a log message through the Flags logger.
 	 * 
 	 * @param message
 	 *            The message
 	 */
 	public static void log(String message) {
         Bukkit.getServer().getPluginManager().getPlugin("Flags").getLogger().info(message);
 	}
 
     /**
      * Sends a debug message through the Flags logger.
      *
      * @param message
      *            The message
      */
     public static void debug(String message) {
         if(debugOn) {
             Bukkit.getServer().getPluginManager().getPlugin("Flags").getLogger().info("[DEBUG] " + message);
         }
     }
 
 	/**
 	 * Gets the status of the border patrol event listener. (i.e
 	 * PlayerChangedAreaEvent)
 	 * 
 	 * @return The status of the border patrol listener
 	 */
 	public static boolean getBorderPatrolEnabled() {
 		return borderPatrol;
 	}
 
 	/**
 	 * Gets the DataStore used by Flags. In most cases, plugins should not
 	 * attempt to access this directly.
 	 * 
 	 * @return The dataStore object in Flags.
 	 */
 	public static DataStore getDataStore() {
 		return dataStore;
 	}
 
 	/**
 	 * Gets the vault economy for this instance of Flags.
 	 * 
 	 * @return The vault economy.
 	 */
 	public static Economy getEconomy() {
 		return economy;
 	}
 
 	/**
 	 * Gets the registrar for this instance of Flags.
 	 * 
 	 * @return The flag registrar.
 	 */
 	public static Registrar getRegistrar() {
 		return flagRegistrar;
 	}
 
 	/*
 	 * Acquires the land management plugin.
 	 */
 	private System findSystem(PluginManager pm) {
 		final List<?> pluginList = getConfig().getList("Flags.AreaPlugins");
 
 		for(Object o : pluginList) {
 			debug("Testing Plugin: " + o);
 			if (pm.isPluginEnabled((String) o)) {
 				debug("Plugin Found: " + o);
 				return System.getByName((String) o);
 			}
 		}
 		return System.WORLD;
 	}
 
 	/*
 	 * Register with the Vault economy plugin.
 	 * 
 	 * @return True if the economy was successfully configured.
 	 */
 	private static boolean setupEconomy() {
 		if (!Bukkit.getServer().getPluginManager().isPluginEnabled("Vault")) {
 			return false;
 		}
 		final RegisteredServiceProvider<Economy> economyProvider = Bukkit
 				.getServer().getServicesManager()
 				.getRegistration(net.milkbowl.vault.economy.Economy.class);
 		if (economyProvider != null) {
 			economy = economyProvider.getProvider();
 		}
 
 		return economy != null;
 	}
 }
