 package com.censoredsoftware.demigods.data;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
import com.censoredsoftware.core.bukkit.ConfigFile;
 import com.censoredsoftware.demigods.Demigods;
 import com.censoredsoftware.demigods.ability.Ability;
 import com.censoredsoftware.demigods.battle.Battle;
 import com.censoredsoftware.demigods.language.Translation;
 import com.censoredsoftware.demigods.location.DLocation;
 import com.censoredsoftware.demigods.player.*;
 import com.censoredsoftware.demigods.structure.Structure;
 import com.google.common.collect.Maps;
 
 public class DataManager
 {
 	// Files
	private static ConfigFile itemStacksYAML;
	private static ConfigFile locationsYAML;
	private static ConfigFile bindsYAML;
	private static ConfigFile devotionYAML;
	private static ConfigFile characterMetasYAML;
	private static ConfigFile inventoriesYAML;
	private static ConfigFile charactersYAML;
	private static ConfigFile notificationsYAML;
	private static ConfigFile petsYAML;
	private static ConfigFile playersYAML;
	private static ConfigFile structuresYAML;
	private static ConfigFile battlesYAML;
	private static ConfigFile timedDataYAML;
 
 	// Data
 	public static Map<UUID, DItemStack> itemStacks;
 	public static Map<UUID, DLocation> locations;
 	public static Map<UUID, Ability.Bind> binds;
 	public static Map<UUID, Ability.Devotion> devotion;
 	public static Map<String, DPlayer> players;
 	public static Map<UUID, DCharacter> characters;
 	public static Map<UUID, Notification> notifications;
 	public static Map<UUID, Pet> pets;
 	public static Map<UUID, DCharacter.Meta> characterMetas;
 	public static Map<UUID, DCharacter.Inventory> inventories;
 	public static Map<UUID, Structure.Save> structures;
 	public static Map<UUID, Battle> battles;
 	public static Map<UUID, TimedData> timedData;
 
 	private static Map<String, HashMap<String, Object>> tempData;
 
 	public DataManager()
 	{
 		itemStacksYAML = new DItemStack.File();
 		locationsYAML = new DLocation.File();
 		bindsYAML = new Ability.Bind.File();
 		devotionYAML = new Ability.Devotion.File();
 		playersYAML = new DPlayer.File();
 		charactersYAML = new DCharacter.File();
 		characterMetasYAML = new DCharacter.Meta.File();
 		inventoriesYAML = new DCharacter.Inventory.File();
 		notificationsYAML = new Notification.File();
 		petsYAML = new Pet.File();
 		structuresYAML = new Structure.Save.File();
 		battlesYAML = new Battle.File();
 		timedDataYAML = new TimedData.File();
 
 		load();
 
 		tempData = Maps.newHashMap();
 	}
 
 	public static void load()
 	{
 		itemStacks = itemStacksYAML.loadFromFile();
 		locations = locationsYAML.loadFromFile();
 		binds = bindsYAML.loadFromFile();
 		devotion = devotionYAML.loadFromFile();
 		players = playersYAML.loadFromFile();
 		characters = charactersYAML.loadFromFile();
 		characterMetas = characterMetasYAML.loadFromFile();
 		inventories = inventoriesYAML.loadFromFile();
 		notifications = notificationsYAML.loadFromFile();
 		pets = petsYAML.loadFromFile();
 		structures = structuresYAML.loadFromFile();
 		battles = battlesYAML.loadFromFile();
 		timedData = timedDataYAML.loadFromFile();
 	}
 
 	public static void save()
 	{
 		itemStacksYAML.saveToFile();
 		locationsYAML.saveToFile();
 		bindsYAML.saveToFile();
 		devotionYAML.saveToFile();
 		playersYAML.saveToFile();
 		charactersYAML.saveToFile();
 		characterMetasYAML.saveToFile();
 		inventoriesYAML.saveToFile();
 		notificationsYAML.saveToFile();
 		petsYAML.saveToFile();
 		structuresYAML.saveToFile();
 		battlesYAML.saveToFile();
 		timedDataYAML.saveToFile();
 	}
 
 	public static void flushData()
 	{
 		// Kick everyone
 		for(Player player : Bukkit.getOnlinePlayers())
 			player.kickPlayer(ChatColor.GREEN + Demigods.language.getText(Translation.Text.DATA_RESET_KICK));
 
 		// Clear the data
 		itemStacks.clear();
 		locations.clear();
 		players.clear();
 		characters.clear();
 		characterMetas.clear();
 		inventories.clear();
 		binds.clear();
 		devotion.clear();
 		notifications.clear();
 		pets.clear();
 		structures.clear();
 		battles.clear();
 		timedData.clear();
 
 		tempData.clear();
 
 		save();
 
 		// Reload the plugin
 		Bukkit.getServer().getPluginManager().disablePlugin(Demigods.plugin);
 		Bukkit.getServer().getPluginManager().enablePlugin(Demigods.plugin);
 	}
 
 	public static boolean hasKeyTemp(String key, String subKey)
 	{
 		return tempData.containsKey(key) && tempData.get(key).containsKey(subKey);
 	}
 
 	public static Object getValueTemp(String key, String subKey)
 	{
 		if(tempData.containsKey(key)) return tempData.get(key).get(subKey);
 		else return null;
 	}
 
 	public static void saveTemp(String key, String subKey, Object value)
 	{
 		if(!tempData.containsKey(key)) tempData.put(key, new HashMap<String, Object>());
 		tempData.get(key).put(subKey, value);
 	}
 
 	public static void removeTemp(String key, String subKey)
 	{
 		if(tempData.containsKey(key) && tempData.get(key).containsKey(subKey)) tempData.get(key).remove(subKey);
 	}
 
 	public static void saveTimed(String key, String subKey, Object data, Integer seconds)
 	{
 		// Remove the data if it exists already
 		TimedData.Util.remove(key, subKey);
 
 		// Create and save the timed data
 		TimedData timedData = new TimedData();
 		timedData.generateId();
 		timedData.setKey(key);
 		timedData.setSubKey(subKey);
 		timedData.setData(data.toString());
 		timedData.setSeconds(seconds);
 		DataManager.timedData.put(timedData.getId(), timedData);
 	}
 
 	public static void removeTimed(String key, String subKey)
 	{
 		TimedData.Util.remove(key, subKey);
 	}
 
 	public static boolean hasTimed(String key, String subKey)
 	{
 		return TimedData.Util.find(key, subKey) != null;
 	}
 
 	public static Object getTimedValue(String key, String subKey)
 	{
 		return TimedData.Util.find(key, subKey).getData();
 	}
 }
