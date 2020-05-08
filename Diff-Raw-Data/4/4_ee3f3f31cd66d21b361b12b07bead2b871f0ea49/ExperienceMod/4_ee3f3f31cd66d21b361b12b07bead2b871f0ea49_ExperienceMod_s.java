 /*
  *  ExperienceMod - Bukkit server plugin for modifying the experience system in Minecraft.
  *  Copyright (C) 2012 Kristian S. Stangeland
  *
  *  This program is free software; you can redistribute it and/or modify it under the terms of the 
  *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
  *  the License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
  *  See the GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License along with this program; 
  *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
  *  02111-1307 USA
  */
 
 package com.comphenix.xp;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Collection;
 import java.util.List;
 import java.util.MissingResourceException;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.chat.Chat;
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.ServicesManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.comphenix.xp.commands.CommandExperienceMod;
 import com.comphenix.xp.commands.CommandSpawnExp;
 import com.comphenix.xp.expressions.ParameterProviderSet;
 import com.comphenix.xp.expressions.StandardPlayerService;
 import com.comphenix.xp.extra.Permissions;
 import com.comphenix.xp.extra.Service;
 import com.comphenix.xp.extra.ServiceProvider;
 import com.comphenix.xp.history.HawkeyeService;
 import com.comphenix.xp.history.HistoryProviders;
 import com.comphenix.xp.history.LogBlockService;
 import com.comphenix.xp.history.MemoryService;
 import com.comphenix.xp.listeners.*;
 import com.comphenix.xp.lookup.*;
 import com.comphenix.xp.messages.ChannelChatService;
 import com.comphenix.xp.messages.ChannelProvider;
 import com.comphenix.xp.messages.HeroService;
 import com.comphenix.xp.messages.MessageFormatter;
 import com.comphenix.xp.messages.StandardService;
 import com.comphenix.xp.metrics.AutoUpdate;
 import com.comphenix.xp.metrics.DataCollector;
 import com.comphenix.xp.mods.BlockResponse;
 import com.comphenix.xp.mods.CustomBlockProviders;
 import com.comphenix.xp.mods.StandardBlockService;
 import com.comphenix.xp.parser.ParsingException;
 import com.comphenix.xp.parser.Utility;
 import com.comphenix.xp.rewards.*;
 import com.comphenix.xp.rewards.items.RewardDrops;
 import com.comphenix.xp.rewards.xp.RewardEconomy;
 import com.comphenix.xp.rewards.xp.RewardExperience;
 import com.comphenix.xp.rewards.xp.RewardVirtual;
 import com.google.common.base.Function;
 import com.google.common.base.Objects;
 import com.google.common.collect.Lists;
 
 public class ExperienceMod extends JavaPlugin implements Debugger {
 	
 	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
 	
 	private Logger currentLogger;
 	private PluginManager manager;
 	
 	// Scheduling
 	private PlayerScheduler playerScheduler;
 
 	// VAULT only
 	private Economy economy;
 	private Chat chat;
 	private PlayerGroupMembership playerGroups;
 	
 	private ExperienceBlockListener xpBlockListener;
 	private ExperienceItemListener xpItemListener;
 	private ExperienceMobListener xpMobListener;
 	private ExperienceEnhancementsListener xpEnchancer;
 	private ExperienceCleanupListener xpCleanup;
 	private ExperienceLevelListener xpLevel;
 	
 	private ExperienceInformerListener informer;
 	private ItemRewardListener itemListener;
 	private PlayerInteractionListener interactionListener;
 
 	// Allows for plugin injection
 	private RewardProvider rewardProvider;
 	private ChannelProvider channelProvider;
 	private CustomBlockProviders customProvider;
 	private HistoryProviders historyProviders;
 	private ParameterProviderSet parameterProviders;
 	private StandardPlayerService standardPlayerService;
 	private RewardEconomy rewardEconomy;
 	
 	private GlobalSettings globalSettings;
 	private ConfigurationLoader configLoader;
 	private Presets presets;
 	
 	// Metrics!
 	private DataCollector dataCollector;
 	private AutoUpdate autoUpdate;
 	
 	// Repeating task
 	private static final int TICK_DELAY = 4; // 50 ms * 4 = 200 ms
 	private int serverTickTask;
 
 	// Error reporter
 	private ErrorReporting report = ErrorReporting.DEFAULT;
 	
 	// Commands
 	private CommandExperienceMod commandExperienceMod;
 	private CommandSpawnExp commandSpawn;
 	
 	private boolean debugEnabled;
 	
 	@Override
 	public void onLoad() {
 		try {
 			currentLogger = this.getLogger();
 			
 			// Initialize scheduler
 			playerScheduler = new PlayerScheduler(Bukkit.getScheduler(), this);
 			manager = getServer().getPluginManager();
 			
 			// Informs about negative events
 			informer = new ExperienceInformerListener(this, getServer());
 			
 			// Initialize rewards
 			rewardProvider = new RewardProvider();
 					
 			// Load history
 			historyProviders = new HistoryProviders();
 			
 			// Load reward types
 			rewardProvider.register(new RewardExperience());
 			rewardProvider.register(new RewardVirtual());
 			rewardProvider.register(new RewardDrops());
 			rewardProvider.setDefaultReward(RewardTypes.EXPERIENCE);
 			
 			// Initialize channel providers
 			channelProvider = new ChannelProvider();
 			channelProvider.setMessageFormatter(new MessageFormatter());
 			
 			// Load channel providers if we can
 			if (HeroService.exists()) {
 				channelProvider.register(new HeroService());
 				channelProvider.setDefaultName(HeroService.NAME);
 				currentLogger.info("Using HeroChat for channels.");
 			
 			} else if (ChannelChatService.exists()) {
 				channelProvider.register(new ChannelChatService());
 				channelProvider.setDefaultName(ChannelChatService.NAME);
 				currentLogger.info("Using ChannelChat for channels.");
 				
 			} else {
 				channelProvider.register(new StandardService( getServer() ));
 				channelProvider.setDefaultName(StandardService.NAME);
 				currentLogger.info("Using standard chat.");
 			}
 			
 			// Initialize block providers
 			customProvider = new CustomBlockProviders();
 			customProvider.register(new StandardBlockService());
 			
 			// Initialize parameter providers
 			parameterProviders = new ParameterProviderSet();
 			standardPlayerService = new StandardPlayerService();
 			parameterProviders.registerPlayer(standardPlayerService);
 			
 			// Initialize configuration loader
 			configLoader = new ConfigurationLoader(getDataFolder(), this, 
 								rewardProvider, channelProvider, parameterProviders);
 		
 			// Initialize error reporter
 			report.setErrorCount(0);
 			report.clearGlobalParameters();
 			report.addGlobalParameter("rewardProvider", rewardProvider);
 			report.addGlobalParameter("historyProvider", historyProviders);
 			report.addGlobalParameter("channelProvider", channelProvider);
 			report.addGlobalParameter("customProvider", customProvider);
 			report.addGlobalParameter("playerProviders", parameterProviders.getPlayerParameters());
 			report.addGlobalParameter("entityProviders", parameterProviders.getEntityParameters());
 			report.addGlobalParameter("blockProviders", parameterProviders.getBlockParameters());
 			report.addGlobalParameter("itemProviders", parameterProviders.getItemParameters());
 			
 		} catch (Exception e) {
 			// Well, this is bad.
 			report.reportError(this, this, e);
 			throw new IllegalStateException("An exception occored.", e);
 		}
 	}
 
 	@Override
 	public void onEnable() {
 		try {
			playerGroups = new PlayerGroupMembership(chat);
 			interactionListener = new PlayerInteractionListener(this);
 			
 			// Commands
 			commandExperienceMod = new CommandExperienceMod(this);
 			commandSpawn = new CommandSpawnExp(this);
 			
 			// Block provider
 			customProvider.setLastInteraction(interactionListener);
 			
 			// Load economy, if it exists
 			try {
 				if (!hasEconomy())
 					economy = getRegistration(Economy.class, null);
 				if (!hasChat())
 					chat = getRegistration(Chat.class, new Function<Chat, Boolean>() {
 						public Boolean apply(Chat element) {
 							// Try not to use mChatSuite if we don't have to
 							return element.getName().equalsIgnoreCase("mChatSuite");
 						}
 					});
 			
 			} catch (NoClassDefFoundError e) {
 			} catch (NullPointerException e) {
 				e.printStackTrace();
 			}
 			
 			// Don't register economy rewards unless we can
 			if (hasEconomy()) {
 				itemListener = new ItemRewardListener(this);
 				rewardEconomy = new RewardEconomy(economy, this, itemListener);
 				
 				// Associate everything
 				rewardProvider.register(rewardEconomy);
 				itemListener.setReward(rewardEconomy);
 				standardPlayerService.setEconomy(rewardEconomy);
 				
 				// Inform the player
 				currentLogger.info("Economy enabled. Using " + economy.getName() + ".");
 				
 				// Register listener
 				manager.registerEvents(itemListener, this);
 				
 			} else {
 				
 				// Damn it
 				currentLogger.info("Economy not registered.");
 			}
 			
 			// Display chat hook
 			if (hasChat()) {
 				currentLogger.info("Hooked " + chat.getName() + " for chat options.");
 			}
 			
 			try {
 				// Initialize configuration
 				loadDefaults(false);
 				
 				// Register listeners
 				manager.registerEvents(interactionListener, this);
 				manager.registerEvents(xpBlockListener, this);
 				manager.registerEvents(xpItemListener, this);
 				manager.registerEvents(xpMobListener, this);
 				manager.registerEvents(xpEnchancer, this);
 				manager.registerEvents(xpCleanup, this);
 				manager.registerEvents(xpLevel, this);
 				manager.registerEvents(informer, this);
 			
 			} catch (IOException e) {
 				currentLogger.severe("IO error when loading configurations: " + e.getMessage());
 			}
 			
 			xpMobListener.setEconomy(rewardEconomy);
 			
 			// Create memory history
 			historyProviders.register(new MemoryService(
 					globalSettings.getMaxBlocksInHistory(), 
 					globalSettings.getMaxAgeInHistory()
 			));
 		
 			registerHistoryServices();
 			
 			// Collect data, if enabled
 			if (globalSettings.isUseMetrics()) {
 				dataCollector = new DataCollector(this);
 			}
 			
 			// Register commands
 			getCommand(CommandExperienceMod.COMMAND_RELOAD).setExecutor(commandExperienceMod);
 			getCommand(CommandSpawnExp.COMMAND_SPAWN_XP).setExecutor(commandSpawn);
 			
 			// Begin server tick
 			serverTickTask = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 				public void run() {
 					onServerTick();
 				}
 			}, TICK_DELAY, TICK_DELAY); 
 			
 			// Inform of this problem
 			if (serverTickTask < 0)
 				printWarning(this, "Could not start repeating task for sending messages.");
 			
 		} catch (Exception e) {
 			report.reportError(this, this, e);
 			
 			// Let Bukkit know about this too.
 			throw new IllegalStateException("An exception occored.", e);
 		}
 	}
 	
 	private void registerHistoryServices() {
 		
 		// Register log block if exists
 		if (LogBlockService.exists(manager)) {
 			if (!historyProviders.containsService(LogBlockService.NAME)) {
 				historyProviders.register(LogBlockService.create(manager));
 			}
 			
 			currentLogger.info("Connected to LogBlock.");
 		} else {
 			currentLogger.info("Cannot connect to LogBlock.");
 		}
 		
 		// Register Hawkeye if it exists
 		if (manager.getPlugin("HawkEye") != null) {
 			try {
 				if (!historyProviders.containsService(HawkeyeService.NAME)) {
 					historyProviders.register(new HawkeyeService(this));
 				}
 
 				currentLogger.info("Connected to Hawkeye.");
 				
 			} catch (NoClassDefFoundError e) {
 				// Occurs if HawkEye disables itself, usually because of database problems.
 				currentLogger.info("Cannot connect to Hawkeye. Database connection not found.");
 			}
 			
 		} else {
 			currentLogger.info("Cannot connect to Hawkeye.");
 		}
 	}
 	
 	/**
 	 * Disable every service in the given list of services.
 	 * @param provider - registry of services.
 	 * @param serviceNames - services to disable.
 	 */
 	private <TService extends Service> void disableServices(ServiceProvider<TService> provider, List<String> serviceNames) {
 		
 		provider.enableAll();
 		
 		// Disable all such services
 		for (String name : serviceNames) {
 			String enumName = Utility.getEnumName(name);
 			
 			if (provider.containsService(enumName)) {
 				provider.setEnabled(enumName, false);
 			} else {
 				// We should really complain about this
 				printWarning(this, "Cannot disable %s: Service doesn't exist.", name);
 			}
 		}
 	}
 	
 	@Override
 	public void onDisable() {
 
 		// Cancel server tick
 		if (serverTickTask >= 0)
 			getServer().getScheduler().cancelTask(serverTickTask);
 	}
 	
 	public YamlConfiguration loadConfig(String name, String createMessage) throws IOException {
 		
 		File savedFile = new File(getDataFolder(), name);
 		File directory = savedFile.getParentFile();
 		
 		// Reload the saved configuration
 		if (!savedFile.exists()) {
 
 			InputStream input = null;
 			OutputStream output = null;
 			
 			try {
 			
 				// Get the default file
 				input = ExperienceMod.class.getResourceAsStream("/" + name);
 				
 				// See if we found it
 				if (input == null) {
 					throw new MissingResourceException(
 							"Cannot find built in resource file.", "ExperienceMod", name);
 				}
 				
 				// Make sure the directory exists 
 				if (!directory.exists()) {
 					directory.mkdirs();
 					
 					if (!directory.exists())
 						throw new IOException("Could not create the directory " + directory.getAbsolutePath());
 				}
 	
 				// Copy content
 				output = new FileOutputStream(savedFile);
 				copyLarge(input, output);
 			
 			} catch (IOException e) {
 				throw e;
 			} finally {
 				// Clean up
 				if (input != null)
 					input.close();
 				if (output != null)
 					output.close();
 			}
 		}
 		
 		// Retrieve the saved file
 		return YamlConfiguration.loadConfiguration(savedFile);
 	}
 	
 	/**
 	 * Reloads (if reload is TRUE) configurations. There's no need to call this after adding reward providers.
 	 * @param reload - if TRUE; reload configuration.
 	 * @throws IOException An I/O error occurred.
 	 */
 	public void loadDefaults(boolean reload) throws IOException {
 
 		// Read from disk again
 		if (reload || presets == null) {
 			
 			if (reload) {
 				// Reset warnings if this is the second time around
 				informer.clearMessages();
 			}
 				
 			// Remove previously loaded files
 			configLoader.clearCache();
 			
 			// Initialize parser
 			if (rewardProvider.containsReward(RewardTypes.DROPS)) {
 				RewardDrops drops = (RewardDrops) rewardProvider.getByEnum(RewardTypes.DROPS);
 				drops.setItemNameParser(configLoader.getNameParser());
 			}
 			
 			// Load globals
 			YamlConfiguration globalConfig = loadConfig("global.yml", "Creating default global settings.");
 			globalSettings = new GlobalSettings(this);
 			globalSettings.loadFromConfig(globalConfig);
 			Permissions.setGlobalSettings(globalSettings);
 			
 			if (autoUpdate == null) {
 				try {
 					autoUpdate = new AutoUpdate(this, globalConfig);
 				} catch (Exception e) {
 					throw new FileNotFoundException(e.getMessage());
 				}
 			} else {
 				// Update updater
 				autoUpdate.setConfig(globalConfig);
 			}
 			
 			// Disable stuff
 			disableServices(historyProviders, globalSettings.getDisabledServices());
 			disableServices(channelProvider, globalSettings.getDisabledServices());
 			disableServices(rewardProvider, globalSettings.getDisabledServices());
 			disableServices(customProvider, globalSettings.getDisabledServices());
 			
 			// Load parts of the configuration
 			YamlConfiguration presetList = loadConfig("presets.yml", "Creating default preset list.");
 			loadConfig("config.yml", "Creating default configuration.");
 			
 			// Load it
 			presets = new Presets(presetList, configLoader, globalSettings.getPresetCacheTimeout(), 
 						 		  this, chat);
 			setPresets(presets);
 			
 			// Vault is required here
 			if (chat == null && presets.usesPresetParameters()) {
 				printWarning(this, "Cannot use presets. VAULT plugin was not found");
 				
 			} else {
 				
 				// Show potentially more warnings
 				checkIllegalPresets();
 			}
 		}
 	}
 	
 	/**
 	 * Invoked every server tick.
 	 */
 	public void onServerTick() {
 	
 		try {
 			// Send messages
 			if (presets != null)
 				presets.onTick();
 			
 		} catch (Exception e) {
 			report.reportError(this, this, e, presets);
 		}
 	}
 	
 	// Check for illegal presets
 	private void checkIllegalPresets() {
 
 		// With no Vault this is impossible
 		if (chat == null)
 			return;
 		
 		String possibleOption = "";
 		
 		for (String group : chat.getGroups()) {
 			for (World world : getServer().getWorlds()) {
 				
 				String worldName = world.getName();
 				
 				try {
 					// We have to be careful here - the plugin might throw an error
 					possibleOption = chat.getGroupInfoString(worldName, 
 						group, Presets.OPTION_PRESET_SETTING, null);
 					
 					if (!Utility.isNullOrIgnoreable(possibleOption) && 
 						!presets.containsPreset(possibleOption, worldName)) {
 						
 						// Complain about this too. Is likely an error.
 						printWarning(this, 
 								"Could not find preset %s. Please check spelling.", possibleOption);
 					}
 					
 				} catch (ParsingException e) {
 					printWarning(this, "Preset '%s' causes error: %s", possibleOption, e.getMessage());
 
 				} catch (Exception e) {
 					// mChat seems to throw this a lot
 					if (!presets.ignorableException(e))
 						printWarning(this, "Preset '%s' threw exception: %s", possibleOption, e.toString());
 				}
 			}
 		}
 	}
 
 	private <TClass> TClass getRegistration(Class<TClass> type, Function<TClass, Boolean> isLowPriority)
     {
 		ServicesManager manager = getServer().getServicesManager();
 		
         Collection<RegisteredServiceProvider<TClass>> registry = manager.getRegistrations(type);
         List<TClass> lower = Lists.newArrayList();
         List<TClass> higher = Lists.newArrayList();
         
         if (registry != null) {
         	
         	// Sort by priority
         	for (RegisteredServiceProvider<TClass> provider : registry) {
         		if (provider.getPlugin() != null) {
         			TClass element = provider.getProvider();
         			
         			// Use our "lambda"
         			if (isLowPriority != null && isLowPriority.apply(element)) {
         				lower.add(provider.getProvider());
         			} else {
         				higher.add(provider.getProvider());
         			}
         		}
         	}
         	
         	// Take the first highest, if possible
         	if (higher.size() > 0)
         		return higher.get(0);
         	else if (lower.size() > 0)
         		return lower.get(0);
         	else
         		// Nothing found!
         		return null;
         	
         } else {
         	return null;
         }
     }
 	
 	private boolean hasEconomy() {
 		return economy != null;
 	}
 	
 	private boolean hasChat() {
 		return chat != null;
 	}
 	
 	@Override	
 	public boolean isDebugEnabled() {
 		return debugEnabled;
 	}
 	
 	/**
 	 * Toggles debug messages.
 	 */
 	public void toggleDebug() {
 		debugEnabled = !debugEnabled;
 	}
 
 	public Chat getChat() {
 		return chat;
 	}
 	
 	public Economy getEconomy() {
 		return economy;
 	}
 	
 	public ExperienceInformerListener getInformer() {
 		return informer;
 	}
 	
 	public RewardProvider getRewardProvider() {
 		return rewardProvider;
 	}
 
 	public ChannelProvider getChannelProvider() {
 		return channelProvider;
 	}
 	
 	public CustomBlockProviders getCustomBlockProvider() {
 		return customProvider;
 	}
 	
 	/**
 	 * Retrieves the object responsible for parsing and loading configuration files.
 	 * @return The current configuration loader.
 	 */
 	public ConfigurationLoader getConfigLoader() {
 		return configLoader;
 	}
 
 	/**
 	 * Sets the object responsible for parsing and loading configuration files.
 	 * @param configLoader - the new configuration loader.
 	 */
 	public void setConfigLoader(ConfigurationLoader configLoader) {
 		this.configLoader = configLoader;
 	}
 
 	/**
 	 * Gets the registry of history plugins.
 	 * @return Registry of history plugins.
 	 */
 	public HistoryProviders getHistoryProviders() {
 		return historyProviders;
 	}
 
 	/**
 	 * Sets the registry of history plugins.
 	 * @param historyProviders - new registry of history plugins.
 	 */
 	public void setHistoryProviders(HistoryProviders historyProviders) {
 		this.historyProviders = historyProviders;
 	}
 	
 	/**
 	 * Gets the registry of parameter providers.
 	 * @return Registry of parameter providers.
 	 */
 	public ParameterProviderSet getParameterProviders() {
 		return parameterProviders;
 	}
 
 	/**
 	 * Sets the registry of parameter providers.
 	 * @param parameterProviders - new registry of the parameter providers.
 	 */
 	public void setParameterProviders(ParameterProviderSet parameterProviders) {
 		this.parameterProviders = parameterProviders;
 	}
 
 	public ItemRewardListener getItemListener() {
 		return itemListener;
 	}
 
 	public Presets getPresets() {
 		return presets;
 	}
 		
 	public PlayerScheduler getPlayerScheduler() {
 		return playerScheduler;
 	}
 	
 	/**
 	 * Retrieves the global plugin settings.
 	 * @return Global settings.
 	 */
 	public GlobalSettings getGlobalSettings() {
 		return globalSettings;
 	}
 	
 	/**
 	 * Retrieves the current registered action types.
 	 * @return Registry of action types.
 	 */
 	public ActionTypes getActionTypes() {
 		return configLoader.getActionTypes();
 	}
 
 	public DataCollector getDataCollector() {
 		return dataCollector;
 	}
 
 	/**
 	 * Retrieves the object responsible for notifying of new updates.
 	 * @return The object keeping track of new updates on BukkitDev.
 	 */
 	public AutoUpdate getAutoUpdate() {
 		return autoUpdate;
 	}
 
 	/**
 	 * Retrieves a list of action rewards that applies when a mob is killed, either by the environment (when KILLER is NULL), 
 	 * or by a player. 
 	 * <p>
 	 * Note that the returned list contains every possible reward that matches the given mob. In reality, only the 
 	 * first item will be awarded.
 	 * 
 	 * @param killer - the player that killed this mob, or NULL if the mob died naturally.
 	 * @param query - query representing the mob that was killed.
 	 * @return A list of possible rewards. Only the first item will be chosen when rewards are actually awarded.
 	 * @throws ParsingException If the stored preset option associated with the killer is malformed.
 	 */
 	public List<Action> getMobReward(Player killer, MobQuery query) throws ParsingException {
 		
 		Configuration config = getPresets().getConfiguration(killer);
 		
 		// Mirror the function below
 		return config.getExperienceDrop().getAllRanked(query);
 	}
 	
 	/**
 	 * Retrieves a list of action rewards that applies when a player performs a given action to the item or block
 	 * specified by the query.
 	 * <p>
 	 * The query must be a ItemQuery for every trigger except brewing, where it also can be a PotionQuery.
 	 * <p>
 	 * Also note that this list contains every possible reward that matches the given parameters. In reality, only the 
 	 * first item will be awarded.
 	 * 
 	 * @param player - player performing the given action, or NULL if the default configuration file should be used.
 	 * @param trigger - action the player performs. 
 	 * @param query - query representing the item or block that was the target of the action.
 	 * @return A list of possible rewards. Only the first item will be chosen when rewards are actually awarded.
 	 * @throws ParsingException If the stored preset option associated with this player is malformed.
 	 */
 	public List<Action> getPlayerReward(Player player, Integer trigger, Query query) throws ParsingException {
 		
 		Configuration config = getPresets().getConfiguration(player);
 		
 		Integer brewing = getActionTypes().getType(ActionTypes.BREWING);
 		ItemTree current = config.getActionReward(trigger);
 		
 		// Special brewing type
 		if (Objects.equal(trigger, brewing) && query instanceof PotionQuery) {
 			
 			// Use the complex brewing reward rules
 			return config.getComplexBrewingReward().getAllRanked((PotionQuery) query);
 			
 		} else {
 			// Check for incorrect action types/triggers
 			if (current == null) {
 				throw new IllegalArgumentException(String.format("Unknown trigger ID: %s", trigger));
 			}
 			
 			// Standard item reward rules
 			return current.getAllRanked((ItemQuery) query);
 		}
 	}
 	
 	/**
 	 * Handles the given inventory event using the default behavior for the given inventory type.
 	 * @param event - inventory click event.
 	 * @param response - block response detailing how to process the inventory.
 	 */
 	public void processInventoryClick(InventoryClickEvent event, BlockResponse response) {
 		if (xpItemListener == null)
 			throw new RuntimeException("ExperienceMod isn't loaded yet.");
 
 		xpItemListener.processInventory(event, response);
 	}
 	
 	private void setPresets(Presets presets) {
 		
 		// Create a new listener if necessary
 		if (xpBlockListener == null || xpItemListener == null || xpMobListener == null) {
 			xpItemListener = new ExperienceItemListener(this, playerScheduler, customProvider, presets);
 			xpBlockListener = new ExperienceBlockListener(this, presets, historyProviders);
 			xpMobListener = new ExperienceMobListener(this, playerGroups, presets);
 			xpEnchancer = new ExperienceEnhancementsListener(this, presets);
 			xpLevel = new ExperienceLevelListener(this, presets);
 			xpCleanup = new ExperienceCleanupListener(presets, interactionListener, playerScheduler);
 			
 		} else {
 			xpEnchancer.setPresets(presets);
 			xpItemListener.setPresets(presets);
 			xpBlockListener.setPresets(presets);
 			xpMobListener.setPresets(presets);
 			xpLevel.setPresets(presets);
 			xpCleanup.setPlayerCleanupListeners(presets, interactionListener, playerScheduler);
 		}
 	}
 	
 	@Override
 	public void printDebug(Object sender, String message, Object... params) {
 		if (debugEnabled) {
 			
 			String formattedMessage = String.format("[ExperienceMod] " + message, params);
 			
 			// Every player with the info permission will also see this message
 			getServer().broadcast(formattedMessage, Permissions.INFO);
 		}
 	}
 
 	public void respond(CommandSender sender, String message) {
 		if (sender == null) // Sent by the console
 			currentLogger.info(message);
 		else
 			sender.sendMessage(message);
 	}
 
 	@Override
 	public void printWarning(Object sender, String message, Object... params) {
 		String formatted = String.format(message, params);
 		String warningMessage = ChatColor.RED + "Warning: " + formatted;
 		
 		if (debugEnabled) {
 			currentLogger.warning(String.format("Warning sent from %s.", sender));
 		}
 		
 		// Print immediately
 		if (currentLogger == null)
 			System.err.println(warningMessage);
 		else
 			currentLogger.warning(warningMessage);
 		
 		// Add to list of warnings
 		if (informer != null) {
 		    informer.addWarningMessage(formatted);
 		    informer.broadcastWarning(formatted);
 		}
 	}
 	
 	// Taken from Apache Commons-IO
 	private static long copyLarge(InputStream input, OutputStream output) throws IOException {
 		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
 		long count = 0;
 		int n = 0;
 		
 		while (-1 != (n = input.read(buffer))) {
 			output.write(buffer, 0, n);
 			count += n;
 		}
 		return count;
 	}
 }
