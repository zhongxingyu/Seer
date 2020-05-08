 package com.ghomerr.linkedchest;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.ghomerr.linkedchest.constants.Constants;
 import com.ghomerr.linkedchest.enums.Commands;
 import com.ghomerr.linkedchest.enums.Configurations;
 import com.ghomerr.linkedchest.enums.Messages;
 import com.ghomerr.linkedchest.listeners.BlockEventListener;
 import com.ghomerr.linkedchest.listeners.CommandsListener;
 import com.ghomerr.linkedchest.listeners.InventoryInteractionListener;
 import com.ghomerr.linkedchest.objects.VirtualInventory;
 import com.ghomerr.linkedchest.utils.ConfigurationUtils;
 import com.ghomerr.linkedchest.utils.DebugUtils;
 import com.ghomerr.linkedchest.utils.FileUtils;
 import com.ghomerr.linkedchest.utils.MessagesUtils;
 import com.ghomerr.linkedchest.utils.PermissionsUtils;
 import com.ghomerr.linkedchest.utils.StringUtils;
 import com.ghomerr.linkedchest.utils.WorldUtils;
 
 public class LinkedChest extends JavaPlugin
 {
 	private static final Logger _LOGGER = Logger.getLogger(Constants.MINECRAFT);
 
 	public InventoryInteractionListener invListener = null;
 	public BlockEventListener blockListener = null;
 
 	private Object _monitor = new Object();
 
 	private Properties _masterChestsData = null;
 	private Properties _linkedChestsData = null;
 
 	private HashMap<String, VirtualInventory> _virtualInventoriesMap = new HashMap<String, VirtualInventory>();
 	private HashMap<String, String> _masterChestsShortLocationsMap = new HashMap<String, String>();
 
 	public HashSet<String> playersWhoOpenChests = new HashSet<String>();
 
 	public long lastSaveInMillisecs = 0;
 
 	/* JAVAPLUGIN METHODS */
 
 	@Override
 	public void onEnable()
 	{
 		super.onEnable();
 
 		// Load LinkedChest Config
 		final boolean isConfigLoaded = ConfigurationUtils.loadConfiguration();
 
 		// Load LinkedChest Data
 		_masterChestsData = FileUtils.loadData(Constants.MASTERCHESTS_FILE_PATH);
 		_linkedChestsData = FileUtils.loadData(Constants.LINKEDCHESTS_FILE_PATH);
 		final boolean isDataLoaded = loadPluginData();
 
 		if (DebugUtils.isDebugEnabled())
 		{
 			_LOGGER.info(Constants.TAG + "configLoaded=" + isConfigLoaded);
 			_LOGGER.info(Constants.TAG + "_masterChestsData=" + _masterChestsData);
 			_LOGGER.info(Constants.TAG + "_linkedChestsData=" + _linkedChestsData);
 		}
 
 		if (isConfigLoaded && _masterChestsData != null && _linkedChestsData != null && isDataLoaded)
 		{
 			// Load Messages
 			MessagesUtils.loadMessages(getLanguage());
 
 			// Load Permissions
 			PermissionsUtils.loadPermissions(this);
 
 			// Register Commands
 			final CommandsListener cmdExec = new CommandsListener(this);
 			Commands.registerMainCommand(this, cmdExec);
 
 			// Register Events
 			invListener = new InventoryInteractionListener(this);
 			blockListener = new BlockEventListener(this);
 		}
 		else
 		{
 			_LOGGER.severe(Constants.TAG + "Failed to load Plugin data. Plugin disabled.");
 		}
 	}
 
 	@Override
 	public void onDisable()
 	{
 		super.onDisable();
 		FileUtils.saveFile(_masterChestsData, Constants.MASTERCHESTS_FILE_PATH);
 		FileUtils.saveFile(_linkedChestsData, Constants.LINKEDCHESTS_FILE_PATH);
 		ConfigurationUtils.saveConfiguration();
 		forceSaveAllWorlds("pluginDisable");
 	}
 
 	private void forceSaveAllWorlds(final String playerName)
 	{
 		_LOGGER.info(Constants.TAG + "Forcing save-all as " + playerName + " has closed a Linked/Master chest");
 		getServer().broadcastMessage("LinkedChest save...");
 		getServer().dispatchCommand(getServer().getConsoleSender(), "save-all");
 	}
 
 	public void saveWorldsIfNecessary(final String playerName)
 	{
 		final String timeToWaitInString = ConfigurationUtils.getConfigValueByType(Configurations.SAVEALL_PERIOD, String.class);
 
 		if (StringUtils.isNotBlank(timeToWaitInString))
 		{
 			if (!Constants.DISABLED.equalsIgnoreCase(timeToWaitInString)
 					&& timeToWaitInString.matches(Constants.NUMBER_PATTERN))
 			{
 				final long timeToWaitInLong = Long.parseLong(timeToWaitInString);
 				if (timeToWaitInLong > 0)
 				{
 					if (lastSaveInMillisecs == 0)
 					{
 						forceSaveAllWorlds(playerName);
 						lastSaveInMillisecs = System.currentTimeMillis();
 					}
 					else
 					{
 						final long timePastInMin =
 								(System.currentTimeMillis() - lastSaveInMillisecs) / Constants.ONE_MINUTE_IN_MILLISECS;
 						if (timePastInMin >= timeToWaitInLong)
 						{
 							forceSaveAllWorlds(playerName);
 							lastSaveInMillisecs = System.currentTimeMillis();
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/* PUBLIC METHODS */
 
 	public Inventory getVirtualInventory(final String name)
 	{
 		VirtualInventory virtualInv = null;
 
 		if (StringUtils.isValidChestName(name))
 		{
 			final String chestName = name.toLowerCase();
 			synchronized (_monitor)
 			{
 				virtualInv = _virtualInventoriesMap.get(chestName);
 			}
 
 			if (virtualInv != null)
 			{
 				if (virtualInv.singleLocation != null)
 				{
 					final Location masterChestLocation = virtualInv.singleLocation;
 					final World world = masterChestLocation.getWorld();
 					final Block block = world.getBlockAt(masterChestLocation);
 
 					// Useless...
 //					final Chunk chestChunk = block.getChunk();
 //					if (!chestChunk.isLoaded())
 //					{
 //						if (DebugUtils.isDebugEnabled())
 //						{
 //							_LOGGER.info(Constants.TAG + "Chunk of master chest " + chestName + " is not loaded. Loading...");
 //						}
 //						if (chestChunk.load())
 //						{
 //							if (DebugUtils.isDebugEnabled())
 //							{
 //								_LOGGER.info(Constants.TAG + "Chunk has been correctly loaded.");
 //							}
 //						}
 //						else
 //						{
 //							if (DebugUtils.isDebugEnabled())
 //							{
 //								_LOGGER.info(Constants.TAG + "Chunk has NOT been correctly loaded.");
 //							}
 //						}
 //					}
 
 					if (!WorldUtils.isChest(block))
 					{
 						_LOGGER.severe(Constants.TAG + "Master Chest " + chestName
 								+ " does not exist anymore. Removing it from data.");
 						removeVirtualInventory(chestName);
 					}
 					else
 					{
 //						if (DebugUtils.isDebugEnabled())
 //						{
 //							_LOGGER.info(Constants.TAG + "Getting Inventory " + chestName + " ref = " + virtualInv.inventory);
 //						}
 						// return virtualInv.inventory;
 						return ((Chest) block.getState()).getInventory();
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	public String getMasterChestNameFromBlock(final Block block)
 	{
 		Location loc = block.getLocation();
 
 		final String shortLoc = StringUtils.printShortLocation(loc);
 
 		String chestName = null;
 
 		synchronized (_monitor)
 		{
 			chestName = _masterChestsShortLocationsMap.get(shortLoc);
 		}
 
 		return chestName;
 	}
 
 	public String getLinkedChestNameFromBlock(final Block block)
 	{
 		Location loc = block.getLocation();
 
 		final String shortLoc = StringUtils.printShortLocation(loc);
 
 		String chestName = null;
 
 		synchronized (_monitor)
 		{
 			chestName = _linkedChestsData.getProperty(shortLoc);
 		}
 
 		return chestName;
 	}
 
 	public void setVirtualInventory(final String name, final VirtualInventory virtualInv)
 	{
 		synchronized (_monitor)
 		{
 			if (StringUtils.isValidChestName(name) && virtualInv != null)
 			{
 				_virtualInventoriesMap.put(name, virtualInv);
 				_masterChestsShortLocationsMap.put(virtualInv.singleShortLoc, name);
 				if (StringUtils.isNotBlank(virtualInv.doubleShortLoc))
 				{
 					_masterChestsShortLocationsMap.put(virtualInv.doubleShortLoc, name);
 				}
 			}
 		}
 	}
 
 	public boolean removeVirtualInventory(final String name)
 	{
 		VirtualInventory removedInv = null;
 		final String chestName = name.toLowerCase();
 
 		synchronized (_monitor)
 		{
 			removedInv = _virtualInventoriesMap.remove(chestName);
 			if (removedInv != null)
 			{
 				_masterChestsShortLocationsMap.remove(removedInv.singleShortLoc);
 				if (removedInv.doubleShortLoc != null)
 				{
 					_masterChestsShortLocationsMap.remove(removedInv.doubleShortLoc);
 				}
 
 				for (final String linkedChestShortLoc : removedInv.linkedChests)
 				{
 					_linkedChestsData.remove(linkedChestShortLoc);
 				}
 				removedInv.linkedChests.clear();
 			}
 
 			_masterChestsData.remove(chestName);
 			FileUtils.saveFile(_masterChestsData, Constants.MASTERCHESTS_FILE_PATH);
 			FileUtils.saveFile(_linkedChestsData, Constants.LINKEDCHESTS_FILE_PATH);
 		}
 
 		return (removedInv != null);
 	}
 
 	public boolean unlinkAllChestsOf(final String name)
 	{
 		if (StringUtils.isValidChestName(name))
 		{
 			final String chestName = name.toLowerCase();
 			synchronized (_monitor)
 			{
 				final VirtualInventory vInv = _virtualInventoriesMap.get(chestName);
 				for (final String shortLoc : vInv.linkedChests)
 				{
 					_linkedChestsData.remove(shortLoc);
 				}
 				vInv.linkedChests.clear();
 				FileUtils.saveFile(_linkedChestsData, Constants.LINKEDCHESTS_FILE_PATH);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean addLinkedChest(final Location loc, final String name)
 	{
 		return addLinkedChest(StringUtils.printShortLocation(loc), name);
 	}
 
 	public boolean addLinkedChest(final String shortLoc, final String name)
 	{
 		if (DebugUtils.isDebugEnabled())
 		{
 			_LOGGER.info(Constants.TAG + "A linked chest is added at '" + shortLoc + "' for the chest " + name);
 		}
 
 		if (StringUtils.isNotBlank(shortLoc) && StringUtils.isNotBlank(name))
 		{
 			final String chestName = name.toLowerCase();
 
 			if (hasChestName(chestName))
 			{
 				synchronized (_monitor)
 				{
 					_linkedChestsData.put(shortLoc, chestName);
 					final VirtualInventory vInv = _virtualInventoriesMap.get(chestName);
 					if (vInv != null)
 					{
 						vInv.linkedChests.add(shortLoc);
 					}
 					FileUtils.saveFile(_linkedChestsData, Constants.LINKEDCHESTS_FILE_PATH);
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean removeLinkedChestLocation(final String shortLoc)
 	{
 		if (DebugUtils.isDebugEnabled())
 		{
 			_LOGGER.info(Constants.TAG + "Linked chest at '" + shortLoc + "' has been removed.");
 		}
 
 		Object ret = null;
 		synchronized (_monitor)
 		{
 			final String chestName = _linkedChestsData.getProperty(shortLoc);
 			final VirtualInventory vInv = _virtualInventoriesMap.get(chestName);
 			if (vInv != null)
 			{
 				vInv.linkedChests.remove(shortLoc);
 			}
 			ret = _linkedChestsData.remove(shortLoc);
 			FileUtils.saveFile(_linkedChestsData, Constants.LINKEDCHESTS_FILE_PATH);
 		}
 		return (ret != null);
 	}
 
 	public boolean removeLinkedChestLocation(final Location loc)
 	{
 		if (loc != null)
 		{
 			return removeLinkedChestLocation(StringUtils.printShortLocation(loc));
 		}
 		else
 		{
 			return false;
 		}
 	}
 
 	public boolean addVirtualInventory(final String chestName, final Block singleBlock, final Block doubleBlock,
 			final boolean isAdmin)
 	{
 		if (StringUtils.isValidChestName(chestName))
 		{
 			//final Chest chest = (Chest) singleBlock.getState();
 			//final Inventory chestInv = chest.getInventory();
 //			if (DebugUtils.isDebugEnabled())
 //			{
 //				_LOGGER.info(Constants.TAG + "Adding Inventory " + chestName + " ref = " + chestInv);
 //			}
 
 			VirtualInventory virtualInv = null;
 			if (doubleBlock == null)
 			{
 				virtualInv = new VirtualInventory(/*chestInv, */singleBlock.getLocation(), isAdmin);
 			}
 			else
 			{
 				virtualInv = new VirtualInventory(/*chestInv, */singleBlock.getLocation(), doubleBlock.getLocation(), isAdmin);
 			}
 
 			final String lowerChestName = chestName.toLowerCase();
 			setVirtualInventory(lowerChestName, virtualInv);
 
 			synchronized (_monitor)
 			{
 				_masterChestsData.put(lowerChestName, virtualInv.getDataString());
 				FileUtils.saveFile(_masterChestsData, Constants.MASTERCHESTS_FILE_PATH);
 			}
 
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 
 	public boolean updateMasterChest(final String chestName, final Chest newMasterChest)
 	{
 		final String lowerChestName = chestName.toLowerCase();
 		final Block doubleChestBlock = WorldUtils.getChestNearby(newMasterChest.getBlock(), Block.class);
 		final VirtualInventory vInv = _virtualInventoriesMap.get(lowerChestName);
 
 		VirtualInventory newVirtualInv = null;
 		if (doubleChestBlock == null)
 		{
 			newVirtualInv = new VirtualInventory(newMasterChest.getLocation(), vInv.isAdmin);
 		}
 		else
 		{
 			newVirtualInv = new VirtualInventory(newMasterChest.getLocation(), doubleChestBlock.getLocation(), vInv.isAdmin);
 		}
 
 		try
 		{
     		synchronized (_monitor)
     		{
     			_masterChestsShortLocationsMap.remove(vInv.singleShortLoc);
     			if (StringUtils.isNotBlank(vInv.doubleShortLoc))
     			{
     				_masterChestsShortLocationsMap.remove(vInv.doubleShortLoc);
     			}
 
     			_masterChestsData.put(lowerChestName, newVirtualInv.getDataString());
     			FileUtils.saveFile(_masterChestsData, Constants.MASTERCHESTS_FILE_PATH);
     		}
     		setVirtualInventory(lowerChestName, newVirtualInv);
     		return true;
 		}
 		catch (final Throwable th)
 		{
 			_LOGGER.severe(Constants.TAG + "Error while updating master chest " + chestName);
 			th.printStackTrace();
 			return false;
 		}
 	}
 
 	public boolean openVirtualInventory(final Player player, final String chestName)
 	{
 		final Inventory virtualInv = getVirtualInventory(chestName);
 		if (virtualInv != null)
 		{
 			player.openInventory(virtualInv);
 			playersWhoOpenChests.add(player.getName());
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 
 	public String getLinkedChestsList(final String param)
 	{
 		Messages initListMessage = null ;
 		boolean displayAllChests = false;
 
 		if (StringUtils.isBlank(param) || param.equalsIgnoreCase(Constants.WILDCARD))
 		{
 			initListMessage = Messages.AVAILABLE_CHESTS;
 			displayAllChests = true;
 		}
 		else
 		{
 			initListMessage = Messages.FOUND_CHESTS;
 		}
 
 		final StringBuilder strBld = new StringBuilder(MessagesUtils.getWithColor(initListMessage,
 				ChatColor.YELLOW));
 
 		boolean isFirst = true;
 		Set<String> chestsNamesList = null;
 
 		synchronized (_monitor)
 		{
 			chestsNamesList = _virtualInventoriesMap.keySet();
 		}
 
 		for (final String chestName : chestsNamesList)
 		{
 			if (displayAllChests || chestName.startsWith(param))
 			{
 				if (!isFirst)
 				{
 					strBld.append(ChatColor.YELLOW).append(Constants.DELIMITER).append(Constants.EMPTY_STRING);
 				}
 				else
 				{
 					isFirst = false;
 				}
 				strBld.append(ChatColor.AQUA).append(chestName);
 			}
 		}
 
 		return strBld.toString();
 	}
 
 	public boolean isAdminChest(final String name)
 	{
 		final String chestName = name.toLowerCase();
 
 		VirtualInventory vInv = null;
 		synchronized (_monitor)
 		{
 			vInv = _virtualInventoriesMap.get(chestName);
 		}
 
 		if (vInv != null)
 		{
 			return vInv.isAdmin;
 		}
 		else
 		{
 			return false;
 		}
 	}
 
 	public boolean toggleAdminChest(final String name) throws IllegalArgumentException
 	{
 		final String chestName = name.toLowerCase();
 
 		VirtualInventory vInv = null;
 		synchronized (_monitor)
 		{
 			vInv = _virtualInventoriesMap.get(chestName);
 			if (vInv != null)
 			{
 				vInv.isAdmin = !vInv.isAdmin;
 				_masterChestsData.put(chestName, vInv.getDataString());
 				FileUtils.saveFile(_masterChestsData, Constants.MASTERCHESTS_FILE_PATH);
 				return vInv.isAdmin;
 			}
 			else
 			{
 				throw new IllegalArgumentException(chestName);
 			}
 		}
 	}
 
 	public boolean hasChestName(final String chestName)
 	{
 		synchronized (_monitor)
 		{
 			return _virtualInventoriesMap.containsKey(chestName.toLowerCase());
 		}
 	}
 
 	public String getLanguage()
 	{
 		return ConfigurationUtils.getConfigValueByType(Configurations.LANGUAGE, String.class);
 	}
 
 	public String getConfigList()
 	{
 		final StringBuilder strBld = new StringBuilder();
 
 		strBld.append(MessagesUtils.getWithColor(Messages.CONFIG_LIST, ChatColor.YELLOW));
 		boolean isFirst = true;
 
 		for (final Configurations conf : Configurations.values())
 		{
 			if (!isFirst)
 			{
 				strBld.append(ChatColor.YELLOW).append(Constants.DELIMITER).append(Constants.EMPTY_STRING);
 			}
 			else
 			{
 				isFirst = false;
 			}
 
 			appendConfigParam(conf, strBld);
 		}
 
 		return strBld.toString();
 	}
 
 	public void appendConfigParam(final Configurations conf, final StringBuilder strBld)
 	{
 		strBld.append(ChatColor.AQUA).append(conf.name).append(ChatColor.YELLOW).append(Constants.EQUAL);
 
 		if (conf.type == Boolean.class)
 		{
 			final boolean value = ConfigurationUtils.getConfigValueByType(conf, Boolean.class);
 			if (value)
 			{
 				strBld.append(ChatColor.GREEN);
 			}
 			else
 			{
 				strBld.append(ChatColor.RED);
 			}
 			strBld.append(value);
 		}
 		else
 		{
 			strBld.append(ChatColor.WHITE).append(ConfigurationUtils.getConfigValueByType(conf, String.class));
 		}
 	}
 
 	public void displayCompleteHelpMessage(final Player player)
 	{
 		player.sendMessage(MessagesUtils.getWithColor(Messages.HELP_MAIN, ChatColor.YELLOW));
 		player.sendMessage(MessagesUtils.getWithColor(Messages.HELP_ADMINS, ChatColor.YELLOW));
 		player.sendMessage(MessagesUtils.getWithColor(Messages.HELP_PLAYERS, ChatColor.YELLOW));
 		player.sendMessage(MessagesUtils.getWithColor(Messages.HELP_CHEST, ChatColor.YELLOW));
 	}
 
 	public String getAliasesListForCommand(final Commands command)
 	{
 		final StringBuilder strBld = new StringBuilder();
 		strBld.append(MessagesUtils.getWithColors(Messages.ALIASES_AVAILABLE, ChatColor.YELLOW, ChatColor.AQUA,
 				command.name()));
 		boolean isFirst = true;
 
 		for (final String str : command.list)
 		{
 			if (!isFirst)
 			{
 				strBld.append(Constants.DELIMITER).append(Constants.EMPTY_STRING);
 			}
 			else
 			{
 				isFirst = false;
 			}
 			strBld.append(ChatColor.AQUA).append(str).append(ChatColor.YELLOW);
 		}
 
 		return strBld.toString();
 	}
 
 	public String getChestDetails(final String name)
 	{
 		final String chestName = name.toLowerCase();
 		final StringBuilder strBld = new StringBuilder();
 
 		strBld.append(MessagesUtils.getWithColors(Messages.CHEST_DETAILS, ChatColor.YELLOW, ChatColor.AQUA, chestName));
 
 		VirtualInventory vInv = null;
 		synchronized (_monitor)
 		{
 			vInv = _virtualInventoriesMap.get(chestName);
 		}
 
 		if (vInv != null)
 		{
 			strBld.append(ChatColor.WHITE).append(vInv.getDataString());
 		}
 
 		return strBld.toString();
 	}
 
 	/* PRIVATE METHODS */
 
 	private boolean loadPluginData()
 	{
 		if (_masterChestsData != null)
 		{
 			if (!_masterChestsData.isEmpty())
 			{
 				final Set<Object> keys = _masterChestsData.keySet();
 				final List<String> chestsToRemove = new ArrayList<String>();
 
 				// LOAD MASTER CHESTS DATA
 				for (final Object key : keys)
 				{
 					final String chestName = String.valueOf(key);
 					if (StringUtils.isValidChestName(chestName))
 					{
 						final String lowerChestName = chestName.toLowerCase();
 						final String value = _masterChestsData.getProperty(lowerChestName);
 						if (StringUtils.isNotBlank(value))
 						{
 							Location singleLoc = null;
 							Location doubleLoc = null;
 							String options = null;
 							if (value.contains(Constants.SEPARATOR))
 							{
 								final String[] split = value.split(Constants.SEPARATOR);
 								String singleLocSting = split[0];
 								if (singleLocSting.contains(Constants.OPTION_DELIM))
 								{
 									final String[] splitSingleLoc = singleLocSting.split(Constants.OPTION_DELIM);
 									singleLocSting = splitSingleLoc[0];
 									options = splitSingleLoc[1];
 								}
 								singleLoc = StringUtils.parseLocation(this, singleLocSting);
 								doubleLoc = StringUtils.parseLocation(this, split[1]);
 							}
 							else
 							{
 								String singleLocSting = value;
 								if (singleLocSting.contains(Constants.OPTION_DELIM))
 								{
 									final String[] splitSingleLoc = singleLocSting.split(Constants.OPTION_DELIM);
 									singleLocSting = splitSingleLoc[0];
 									options = splitSingleLoc[1];
 								}
 								singleLoc = StringUtils.parseLocation(this, singleLocSting);
 							}
 
 							if (singleLoc != null)
 							{
 								boolean isAdminChest = false;
 								if (StringUtils.isNotBlank(options))
 								{
 									isAdminChest = Commands.ADMIN_CHEST.contains(options);
 								}
 
 								final Block chestBlock = singleLoc.getWorld().getBlockAt(singleLoc);
 								//Inventory inv = null;
 
 //								if (WorldUtils.isChest(chestBlock))
 //								{
 //									final Chest chest = (Chest) chestBlock.getState();
 //									inv = chest.getInventory();
 //								}
 
 								//if (inv == null)
 								if (!WorldUtils.isChest(chestBlock))
 								{
 									_LOGGER.warning(Constants.TAG + "Chest '" + lowerChestName
 											+ "' does not exists anymore. Removing this entry.");
 									chestsToRemove.add(lowerChestName);
 								}
 								else
 								{
 //									if (DebugUtils.isDebugEnabled())
 //									{
 //										_LOGGER.info(Constants.TAG + "Loading Inventory " + lowerChestName + " ref = " + inv);
 //									}
 
 									VirtualInventory virtualInv = null;
 									if (doubleLoc == null)
 									{
 										virtualInv = new VirtualInventory(/*inv, */singleLoc, isAdminChest);
 									}
 									else
 									{
 										virtualInv = new VirtualInventory(/*inv, */singleLoc, doubleLoc, isAdminChest);
 									}
 									setVirtualInventory(lowerChestName, virtualInv);
 								}
 							}
 							else
 							{
 								_LOGGER.severe("Invalid location format for chestName '" + lowerChestName + "' : "
 										+ value);
 							}
 						}
 						else
 						{
 							_LOGGER.warning("Ignoring blank value for entry : " + chestName);
 						}
 					}
 					else
 					{
 						_LOGGER.warning("Ignoring blank name entry : " + chestName);
 					}
 				}
 
 				// Remove not existing chests
 				for (final String chestToRemove : chestsToRemove)
 				{
 					removeVirtualInventory(chestToRemove);
 				}
 
 				// LOAD LINKED CHESTS DATA
 				if (DebugUtils.isDebugEnabled())
 				{
 					_LOGGER.info(Constants.TAG + "Loading linkedchest data. There is " 
 						+ _linkedChestsData.size() + " linkedchest lines to verify.");
 				}
 				if (_linkedChestsData != null && !_linkedChestsData.isEmpty())
 				{
 					final Set<Object> keySet = _linkedChestsData.keySet();
 					final HashSet<Object> objectsToRemove = new HashSet<Object>();
 					for (final Object key : keySet)
 					{
 						final String linkedChestShortLoc = String.valueOf(key);
 						if (DebugUtils.isDebugEnabled())
 						{
 							_LOGGER.info(Constants.TAG + "Checking line key = " + key 
 								+ " is equal to " + linkedChestShortLoc + " ?");
 						}
 						if (StringUtils.isNotBlank(linkedChestShortLoc))
 						{
							final String lowerShortLoc = linkedChestShortLoc.toLowerCase();
							final String value = _linkedChestsData.getProperty(lowerShortLoc);
 							if (StringUtils.isNotBlank(value) && hasChestName(value))
 							{
 								final String chestName = value.toLowerCase();
 								synchronized (_monitor)
 								{
 									final VirtualInventory vInv = _virtualInventoriesMap.get(chestName);
									vInv.linkedChests.add(lowerShortLoc);
 								}
 							}
 							else
 							{
 								if (DebugUtils.isDebugEnabled())
 								{
 									_LOGGER.info(Constants.TAG + "Removing entry for shortloc= " + key 
 										+ ", because 'value' is empty or no master chest found: value= " + value);
 								}
 								objectsToRemove.add(key);
 							}
 						}
 					}
 					for (final Object obj : objectsToRemove)
 					{
 						_linkedChestsData.remove(obj);
 					}
 				}
 				else
 				{
 					if (DebugUtils.isDebugEnabled())
 					{
 						_LOGGER.info(Constants.TAG + "No data in Linked Chests data file.");
 					}
 				}
 				if (DebugUtils.isDebugEnabled())
 				{
 					_LOGGER.info(Constants.TAG + "Loading linkedchest data. There is " 
 						+ _linkedChestsData.size() + " at the end of the verification.");
 				}
 			}
 			else
 			{
 				if (DebugUtils.isDebugEnabled())
 				{
 					_LOGGER.info(Constants.TAG + "No data in Master Chests data file.");
 				}
 			}
 
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 }
