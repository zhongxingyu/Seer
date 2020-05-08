 package com.legit2.Demigods;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.inventory.ItemStack;
 
 import com.massivecraft.factions.Board;
 import com.massivecraft.factions.FLocation;
 import com.massivecraft.factions.Faction;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.flags.DefaultFlag;
 
 public class DUtil
 {
 	public static Demigods plugin;
 	
 	// Define variables
 	private static FileConfiguration customConfig = null;
 	private static File customConfigFile = null;
 	private static String plugin_name = "Demigods";
 	private static Logger log = Logger.getLogger("Minecraft");
 	
 	public DUtil(Demigods instance)
 	{
 		plugin = instance;
 	}
 
 	/*
 	 *  getPlugin() : Returns an instance of the plugin.
 	 */
 	public static Demigods getPlugin()
 	{
 		return plugin;
 	}
 	
 	/*
 	 *  getDeityClass() : Returns the string of the (String)deity's classpath.
 	 */
 	public static String getDeityClass(String deity)
 	{
 		String toReturn = (String) DSave.getData("deity_classes_temp", deity);
 		return toReturn;
 	}
 	
 	/*
 	 *  InvokeDeityMethod() : Invokes a static method (with no paramaters) from inside a deity class.
 	 */
 	@SuppressWarnings("rawtypes")
 	public static Object invokeDeityMethod(String deityClass, String method) throws NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
 	{		
 		// No Paramaters
 		Class noparams[] = {};
 		
 		// Creates a new instance of the deity class
 		Object obj = Class.forName(deityClass, true, plugin.getClass().getClassLoader()).newInstance();
 		
 		// Load everything else for the Deity (Listener, etc.)
 		Method toInvoke = Class.forName(deityClass, true, plugin.getClass().getClassLoader()).getMethod(method, noparams);
 		
 		Object toReturn = toInvoke.invoke(obj, (Object[])null);
 		
 		return toReturn;
 	}
 	
 	/*
 	 *  InvokeDeityMethodWithString() : Invokes a static method (with no paramaters) from inside a deity class.
 	 */
 	public static Object invokeDeityMethodWithString(String deityClass, String method, String paramater) throws NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
 	{			
 		// Creates a new instance of the deity class
 		Object obj = Class.forName(deityClass, true, plugin.getClass().getClassLoader()).newInstance();
 		
 		// Load everything else for the Deity (Listener, etc.)
 		Method toInvoke = Class.forName(deityClass, true, plugin.getClass().getClassLoader()).getMethod(method, String.class);
 		
 		Object toReturn = toInvoke.invoke(obj, paramater);
 		
 		return toReturn;
 	}
 	
 	/*
 	 *  getLoadedDeityNames() : Returns a ArrayList<String> of all the loaded deities' names.
 	 */
 	public static ArrayList<String> getLoadedDeityNames()
 	{
 		ArrayList<String> toReturn = new ArrayList<String>();
 		
 		for(String deity : DSave.getAllData().get("deity_alliances_temp").keySet())
 		{
 			toReturn.add(deity);
 		}
 		
 		return toReturn;
 	}
 	
 	/*
 	 *  getLoadedDeityAlliances() : Returns a ArrayList<String> of all the loaded deities' alliances.
 	 */
 	public static ArrayList<String> getLoadedDeityAlliances()
 	{
 		ArrayList<String> toReturn = new ArrayList<String>();
 		
 		for(Object alliance : DSave.getAllData().get("deity_alliances_temp").values().toArray())
 		{
 			if(toReturn.contains((String) alliance)) continue;
 			toReturn.add((String) alliance);
 		}
 		
 		return toReturn;
 	}
 	
 	/*
 	 *  getDeityAlliance() : Returns a String of a loaded (String)deity's alliance.
 	 */
 	public static String getDeityAlliance(String deity)
 	{
 		String toReturn = (String) DSave.getData("deity_alliances_temp", deity);
 		return toReturn;
 	}
 	
 	/*
 	 *  getDeityClaimItems() : Returns an ArrayList<Material> of a loaded (String)deity's claim items.
 	 */
 	@SuppressWarnings("unchecked")
 	public static ArrayList<Material> getDeityClaimItems(String deity)
 	{
 		ArrayList<Material> toReturn = (ArrayList<Material>) DSave.getData("deity_claim_items_temp", deity);
 		return toReturn;
 	}
 	
 	/*
 	 *  getAllDeitiesInAlliance() : Returns a ArrayList<String> of all the loaded deities' names.
 	 */
 	public static ArrayList<String> getAllDeitiesInAlliance(String alliance)
 	{
 		ArrayList<String> toReturn = new ArrayList<String>();
 		
 		for(String deity : DSave.getAllData().get("deity_alliances_temp").keySet())
 		{
 			if(!(getDeityAlliance(deity)).equalsIgnoreCase(alliance)) continue;
 			toReturn.add(deity);
 		}
 		
 		return toReturn;
 	}
 	
 	/*
 	 *  getOnlinePlayers() : Returns a string array of all online players.
 	 */
 	public static Player[] getOnlinePlayers()
 	{
 		return Bukkit.getOnlinePlayers();
 	}
 	
 	/*
 	 *  definePlayer() : Defines the player from (CommandSender)sender.
 	 */
 	public static Player definePlayer(CommandSender sender)
 	{
 		// Define the Player
 		Player player = null;
 		if(sender instanceof Player) player = (Player) sender;
 		
 		return player;
 	}
 	
 	/*
 	 *  areAllied() : Returns true if (String)player is allied with (String)otherPlayer.
 	 */
 	public static boolean areAllied(String player, String otherPlayer)
 	{
 		String playerAlliance = getAlliance(player);
 		String otherPlayerAlliance = getAlliance(otherPlayer);
 		
 		if(playerAlliance.equals(otherPlayerAlliance)) return true;
 		else return false;
 	}
 	
 	/*
 	 *  isEnabledAbility() : Returns a boolean for if (String)ability is enabled for (String)username.
 	 */
 	public static boolean isEnabledAbility(String username, String deity, String ability)
 	{
 		if(getDeityData(username, deity, ability.toLowerCase() + "_boolean") != null)
 		{
 			return toBoolean(getDeityData(username, deity, ability.toLowerCase() + "_boolean"));
 		}
 		else return false;
 	}
 	
 	/*
 	 *  enableAbility() : Enables (String)ability for (String)player.
 	 */
 	public static void enableAbility(String username, String deity, String ability)
 	{
 		if(!isEnabledAbility(username, deity, ability))
 		{
 			setDeityData(username, deity, ability.toLowerCase() + "_boolean", true);
 		}
 	}
 	
 	/*
 	 *  disableAbility() : Disables (String)ability for (String)player.
 	 */
 	public static void disableAbility(String username, String deity, String ability)
 	{
 		if(isEnabledAbility(username, deity, ability))
 		{
 			setDeityData(username, deity, ability.toLowerCase() + "_boolean", false);
 		}
 	}
 	
 	/*
 	 *  isCooledDown() : Returns a boolean for is (String)ability is cooled down.
 	 */
 	public static boolean isCooledDown(Player player, String ability, long ability_time, boolean sendMsg)
 	{
 		if(ability_time > System.currentTimeMillis())
 		{
 			if(sendMsg) player.sendMessage(ChatColor.RED + ability + " has not cooled down!");
 			return false;
 		}
 		else return true;
 	}
 	
 	/*
 	 *  getNumberOfSouls() : Returns the number of souls (Player)player has in their inventory.
 	 */
 	public static int getNumberOfSouls(Player player)
 	{
 		// Define inventory contents & other variables
 		ItemStack[] inventory = player.getInventory().getContents();
 		ArrayList<ItemStack> allSouls = DSouls.returnAllSouls();
 		int numberOfSouls = 0;
 		
 		for(ItemStack soul : allSouls)
 		{
 			for(ItemStack inventoryItem : inventory)
 			{
 				if(inventoryItem != null && inventoryItem.isSimilar(soul))
 				{
 					// Find amount of souls and subtract 1 upon use
 					int amount = inventoryItem.getAmount();
 					
 					numberOfSouls = numberOfSouls + amount;
 				}
 			}
 		}
 		return numberOfSouls;
 	}
 	
 	/*
 	 *  useSoul() : Uses first soul found in (Player)player's inventory.
 	 */
 	public static ItemStack useSoul(Player player)
 	{	
 		if(getNumberOfSouls(player) == 0) return null;
 		// Define inventory contents
 		ItemStack[] inventory = player.getInventory().getContents();
 		ArrayList<ItemStack> allSouls = DSouls.returnAllSouls();
 		
 		for(ItemStack soul : allSouls)
 		{
 			for(ItemStack inventoryItem : inventory)
 			{
 				if(inventoryItem != null && inventoryItem.isSimilar(soul))
 				{
 					// Find amount of souls and subtract 1 upon use
 					int amount = inventoryItem.getAmount();
 					player.getInventory().removeItem(inventoryItem);
 					inventoryItem.setAmount(amount - 1);
 					player.getInventory().addItem(inventoryItem);
 					
 					return inventoryItem;
 				}
 			}
 		}
 		return null;
 	}
 	
 	/*
 	 *  getBind() : Returns the bind for (String)username's (String)ability.
 	 */
 	public static Material getBind(String username, String deity, String ability)
 	{
 		if(getDeityData(username, deity, ability + "_bind") != null)
 		{
 			Material material = (Material) getDeityData(username, deity, ability + "_bind");
 			return material;
 		}
 		else return null;
 	}
 	
 	/*
 	 *  getBindings() : Returns all bindings for (Player)player.
 	 */
 	@SuppressWarnings("unchecked")
 	public static ArrayList<Material> getBindings(String username, String deity)
 	{	
 		// Set variables
 		username = username.toLowerCase();
 	
 		if(DSave.hasPlayerData(username, "deities"))
 		{
 			return (ArrayList<Material>) getDeityData(username, deity, "bindings");
 		}
 		else return new ArrayList<Material>();
 	}
 	
 	/*
 	 *  setBound() : Sets (Material)material to be bound for (Player)player.
 	 */
 	public static boolean setBound(String username, String deity, String ability, Material material)
 	{
 		// Set variables
 		Player player = Bukkit.getPlayer(username);
 		username = username.toLowerCase();
 		
 		if(DUtil.getDeityData(username, deity, ability + "_bind") == null)
 		{
 			if(player.getItemInHand().getType() == Material.AIR)
 			{
 				player.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
 			}
 			else
 			{
 				if(isBound(username, deity, material))
 				{
 					player.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
 					return false;
 				}
 				else if(material == Material.AIR)
 				{
 					player.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
 					return false;
 				}
 				else
 				{			
 					if(DSave.hasPlayerData(username, "bindings"))
 					{
 						ArrayList<Material> bindings = getBindings(username, deity);
 						
 						if(!bindings.contains(material)) bindings.add(material);
 						
 						DSave.saveDeityData(username, deity, "bindings", bindings);
 					}
 					else
 					{
 						ArrayList<Material> bindings = new ArrayList<Material>();
 						
 						bindings.add(material);
 						DSave.saveDeityData(username, deity, "bindings", bindings);
 					}
 					
 					setDeityData(username, deity, ability + "_bind", material);
 					player.sendMessage(ChatColor.YELLOW + ability + " is now bound to: " + material.name().toUpperCase());
 					return true;
 				}
 			}
 		}
 		else
 		{
 			DUtil.removeBind(username, deity, ability, ((Material) DUtil.getDeityData(username, deity, ability + "_bind")));
 			player.sendMessage(ChatColor.YELLOW + ability + "'s bind has been removed.");
 		}
 		return false;
 	}
 	
 	/*
 	 *  isBound() : Checks if (Material)material is bound for (Player)player.
 	 */
 	public static boolean isBound(String username, String deity, Material material)
 	{
 		if(getBindings(username, deity) != null && getBindings(username, deity).contains(material)) return true;
 		else return false;
 	}
 	
 	/*
 	 *  removeBind() : Checks if (Material)material is bound for (Player)player.
 	 */
 	public static boolean removeBind(String username, String deity, String ability, Material material)
 	{
 		username = username.toLowerCase();
 		ArrayList<Material> bindings = null;
 
 		if(DSave.hasPlayerData(username, "bindings"))
 		{
 			bindings = getBindings(username, deity);
 			
 			if(bindings != null && bindings.contains(material)) bindings.remove(material);
 		}
 		
 		DSave.saveDeityData(username, deity, "bindings", bindings);
 		DSave.removeDeityData(username, deity, ability + "_bind");
 
 		return true;
 	}
 	
 	/*
 	 *  getImmortalList() : Gets if the player is immortal or not.
 	 */
 	public static ArrayList<String> getImmortalList()
 	{		
 		// Define variables
 		ArrayList<String> immortalList = new ArrayList<String>();
 		HashMap<String, HashMap<String, Object>> players = getAllPlayersData();
 		
 		for(Map.Entry<String, HashMap<String, Object>> player : players.entrySet())
 		{
 			String username = player.getKey().toLowerCase();
 			if(getDeities(username) != null) immortalList.add(username);
 		}
 		
 		return immortalList;
 	}
 	
 	/*
 	 *  isImmortal() : Gets if the player is immortal or not.
 	 */
 	public static Boolean isImmortal(String username)
 	{
 		// Set variables
 		username = username.toLowerCase();
 				
 		if(getPlayerData(username, "immortal") == null) return false;
 		else return toBoolean(getPlayerData(username, "immortal"));
 	}
 	
 	/*
 	 *  setImmortal() : Gets if the player is immortal or not.
 	 */
 	public static void setImmortal(String username, Boolean option)
 	{
 		setPlayerData(username, "immortal", toBoolean(option));
 	}
 	
 	/*
 	 *  getFavorCap() : Returns the favor cap for (String)username.
 	 */
 	public static int getFavorCap(String username)
 	{
 		return DConfig.getSettingInt("max_favor"); //TODO
 	}
 	
 	/*
 	 *  getFavor() : Returns the (String)username's favor.
 	 */
 	public static int getFavor(String username)
 	{
 		if(getPlayerData(username, "favor") != null) return Integer.parseInt(getPlayerData(username, "favor").toString());
 		else return -1;
 	}
 	
 	/*
 	 *  setFavor() : Sets the (String)username's favor to (int)amount.
 	 */
 	public static void setFavor(String username, int amount)
 	{
 		setPlayerData(username, "favor", amount);
 	}
 	
 	/*
 	 *  subtractFavor() : Subtracts (int)amount from the (String)username's favor.
 	 */
 	public static void subtractFavor(String username, int amount)
 	{
 		setFavor(username, getFavor(username) - amount);
 	}
 	
 	/*
 	 *  giveFavor() : Gives (int)amount favor to (String)username.
 	 */
 	public static void giveFavor(String username, int amount)
 	{
 		// Define variables
 		int favor;
 		
 		// Perform favor cap check
 		if((getFavor(username) + amount) > DConfig.getSettingInt("max_favor"))
 		{
 			favor = DConfig.getSettingInt("max_favor");
 		}
 		else favor = getFavor(username) + amount;
 		
 		setPlayerData(username, "favor", favor);
 	}
 
 	/*
 	 *  getAscensions() : Returns the (String)username's ascensions.
 	 */
 	public static int getAscensions(String username)
 	{
 		if(getPlayerData(username, "ascensions") != null) return Integer.parseInt(getPlayerData(username, "ascensions").toString());
 		else return -1;
 	}
 	
 	/*
 	 *  setAscensions() : Sets the (String)username's ascensions to (int)amount.
 	 */
 	public static void setAscensions(String username, int amount)
 	{
 		setPlayerData(username, "ascensions", amount);
 	}
 
 	/*
 	 *  subtractAscensions() : Subtracts (int)amount from the (String)username's ascensions.
 	 */
 	public static void subtractAscensions(String username, int amount)
 	{
 		setAscensions(username, getAscensions(username) - amount);
 	}
 	
 	/*
 	 *  giveAscensions() : Gives (int)amount ascensions to (String)username.
 	 */
 	public static void giveAscensions(String username, int amount)
 	{
 		setPlayerData(username, "ascensions", getAscensions(username) + amount);
 	}
 	
 	/*
 	 *  getDevotion() : Returns the (String)username's devotion for (String)deity.
 	 */
 	public static int getDevotion(String username, String deity)
 	{
 		if(getDeityData(username, deity, "devotion") != null) return Integer.parseInt(getDeityData(username, deity, "devotion").toString());
 		return -1;
 	}
 	
 	/*
 	 *  setDevotion() : Sets the (String)username's devotion to (int)amount for (String)deity.
 	 */
 	public static void setDevotion(String username, String deity, int amount)
 	{
 		setDeityData(username, deity, "devotion", amount);
 	}
 
 	/*
 	 *  subtractDevotion() : Subtracts (int)amount from the (String)username's (String)deity devotion.
 	 */
 	public static void subtractDevotion(String username, String deity, int amount)
 	{
 		setDevotion(username, deity, getDevotion(username, deity) - amount);
 	}
 	
 	/*
 	 *  giveDevotion() : Gives (int)amount devotion to (String)username for (String)deity.
 	 */
 	public static void giveDevotion(String username, String deity, int amount)
 	{
 		setDeityData(username, deity, "devotion", getDevotion(username, deity) + amount);
 	}
 	
 	/*
 	 *  getAlliance() : Returns the alliance of a player.
 	 */
 	public static String getAlliance(String username)
 	{
		return (String) getPlayerData(username, "alliance");
 	}
 	
 	/*
 	 *  setAlliance() : Sets the (String)username's alliance to (String)alliance.
 	 */
 	public static void setAlliance(String username, String alliance)
 	{
 		setPlayerData(username, "alliance", alliance);
 	}
 	
 	/*
 	 *  getKills() : Returns (int)kills for (String)username.
 	 */
 	public static int getKills(String username)
 	{
 		if(getPlayerData(username, "kills") != null) return Integer.parseInt(getPlayerData(username, "kills").toString());
 		return -1;
 	}
 	
 	/*
 	 *  setKills() : Sets the (String)username's kills to (int)amount.
 	 */
 	public static void setKills(String username, int amount)
 	{
 		setPlayerData(username, "kills", amount);
 	}
 	
 	/*
 	 *  addKill() : Gives (String)username 1 kill.
 	 */
 	public static void addKill(String username)
 	{
 		setPlayerData(username, "kills", getKills(username) + 1);
 	}
 	
 	/*
 	 *  getDeaths() : Returns (int)deaths for (String)username.
 	 */
 	public static int getDeaths(String username)
 	{
 		if(getPlayerData(username, "deaths") != null) return Integer.parseInt(getPlayerData(username, "deaths").toString());
 		return -1;
 	}
 	
 	/*
 	 *  setDeaths() : Sets the (String)username's deaths to (int)amount.
 	 */
 	public static void setDeaths(String username, int amount)
 	{
 		setPlayerData(username, "deaths", amount);
 	}
 	
 	/*
 	 *  addDeath() : Gives (String)username 1 death.
 	 */
 	public static void addDeath(String username)
 	{
 		setPlayerData(username, "deaths", getDeaths(username) + 1);
 	}
 	
 	/*
 	 *  getDeites() : Returns an ArrayList<String> of (Player)player's deities.
 	 */
 	@SuppressWarnings("unchecked")
 	public static ArrayList<String> getDeities(String username)
 	{		
 		// Set variables
 		username = username.toLowerCase();
 		//ArrayList<String> deities = new ArrayList<String>();
 
 		if(DSave.hasPlayerData(username, "deities"))
 		{
 			return (ArrayList<String>) DSave.getPlayerData(username, "deities");
 		}
 		else return null;
 	}
 	
 	/*
 	 *  hasDeity() : Checks if (Player)player has (String)deity.
 	 */
 	public static boolean hasDeity(String username, String deity)
 	{
 		if(getDeities(username) != null && getDeities(username).contains(deity.toLowerCase())) return true;
 		else return false;
 	}
 	
 	/*
 	 *  giveDeity() : Gives (String)username the (String)deity.
 	 */
 	public static boolean giveDeity(String username, String deity)
 	{	
 		// Set variables
 		username = username.toLowerCase();
 		deity.toLowerCase();
 			
 		if(DSave.hasPlayerData(username, "deities"))
 		{
 			ArrayList<String> deities = getDeities(username);
 			
 			if(deities == null || !deities.contains(deity)) deities.add(deity.toLowerCase());
 			
 			setPlayerData(username, "deities", deities);
 		}
 		else
 		{
 			ArrayList<String> deities = new ArrayList<String>();
 			deities.add(deity.toLowerCase());
 			setPlayerData(username, "deities", (ArrayList<String>)deities);
 		}
 		
 		return true;
 	}
 	
 	/*
 	 *  getPlayerData() : Returns the data with id (String)key for (Player)player.
 	 */
 	public static Object getPlayerData(String username, String id)
 	{
 		return (Object) DSave.getPlayerData(username, id);
 	}
 	
 	/*
 	 *  setPlayerData() : Sets the data with id (String)key for (Player)player.
 	 */
 	public static boolean setPlayerData(String username, String id, Object data)
 	{
 		return DSave.savePlayerData(username, id, data);
 	}
 	
 	/*
 	 *  getDeityData() : Returns the deity data with id (String)key for (Player)player.
 	 */
 	public static Object getDeityData(String username, String deity, String id)
 	{
 		return (Object) DSave.getDeityData(username.toLowerCase(), deity.toLowerCase(), id.toLowerCase());
 	}
 	
 	/*
 	 *  setDeityData() : Sets the data with id (String)key for (Player)player's (String)deity.
 	 */
 	public static boolean setDeityData(String username, String deity, String id, Object data)
 	{
 		return DSave.saveDeityData(username.toLowerCase(), deity.toLowerCase(), id.toLowerCase(), data);
 	}
 	
 	/*
 	 *  removeDeityData() : Sets the data with id (String)key for (Player)player's (String)deity.
 	 */
 	public static boolean removeDeityData(String username, String deity, String id)
 	{
 		return DSave.removeDeityData(username.toLowerCase(), deity.toLowerCase(), id.toLowerCase());
 	}
 	
 	/*
      *  regenerateAllFavor() : Regenerates favor for every player based on their stats.
      */
 	public static void regenerateAllFavor()
 	{
 		Player[] onlinePlayers = getOnlinePlayers();
 		
 		for(Player player : onlinePlayers)
 		{
 			String username = player.getName();
 			int regenRate = DUtil.getAscensions(username);
 			if (regenRate < 1) regenRate = 1;
 			giveFavor(username, regenRate);
 		}
 	}
 	
 	/*
 	 *  getAllPlayersData() : Returns all saved HashMaps.
 	 */
 	public static HashMap<String, HashMap<String, Object>> getAllPlayersData()
 	{
 		return DSave.getAllPlayersData();
 	}
 	
 	/*
 	 *  getAllPlayerData() : Returns all saved HashMaps.
 	 */
 	public static HashMap<String, Object> getAllPlayerData(String username)
 	{
 		return DSave.getAllPlayerData(username);
 	}
 	
 	/*
 	 *  getAllDeityData() : Returns all saved HashMaps.
 	 */
 	public static HashMap<String, HashMap<String, Object>> getAllDeityData(String username)
 	{
 		return DSave.getAllDeityData(username);
 	}
 	
 	/*
 	 *  toInteger() : Returns an object as an integer.
 	 */
 	public static int toInteger(Object object)
 	{
 		return Integer.parseInt(object.toString());
 	}
 	
 	/*
 	 *  toBoolean() : Returns an object as a boolean.
 	 */
 	public static boolean toBoolean(Object object)
 	{
 		if(object instanceof Boolean)
 		{
 			return (Boolean) object;
 		}
 		else if(object instanceof Integer)
 		{
 			if((Integer) object == 1) return true;
 			else if((Integer) object == 0) return false;
 		}
 		return Boolean.parseBoolean(object.toString());
 	}
 	
 	/*
 	 *  customDamage() : Creates custom damage for (LivingEntity)target from (LivingEntity)source with ammount (int)amount.
 	 */
 	public static void customDamage(LivingEntity source, LivingEntity target, int amount, DamageCause cause)
 	{
 		if(target instanceof Player)
 		{
 			if(source instanceof Player)
 			{
 				target.setLastDamageCause(new EntityDamageByEntityEvent(source, target, cause, amount));
 			}
 			else target.damage(amount);
 		}
 		else target.damage(amount);
 	}
 	
 	/*
 	 *  taggedMessage() : Sends tagged message (String)msg to the (CommandSender)sender.
 	 */
 	public static void taggedMessage(CommandSender sender, String msg)
 	{
 		sender.sendMessage(ChatColor.YELLOW + "[" + plugin_name + "] " + ChatColor.RESET + msg);
 	}
 	
 	/*
 	 *  info() : Sends console message with "info" tag.
 	 */
 	public static void info(String msg)
 	{
 		log.info("[" + plugin_name + "] " + msg);
 	}
 	
 	/*
 	 *  warning() : Sends console message with "warning" tag.
 	 */
 	public static void warning(String msg)
 	{
 		log.warning("[" + plugin_name + "] " + msg);
 	}
 	
 	/*
 	 *  severe() : Sends console message with "severe" tag.
 	 */
 	public static void severe(String msg)
 	{
 		log.severe("[" + plugin_name + "] " + msg);
 	}
 	
 	/*
 	 *  serverMsg() : Send (String)msg to the server chat.
 	 */
 	public static void serverMsg(String msg)
 	{
 		plugin.getServer().broadcastMessage(msg);
 	}
 
 	/*
 	 *  saveCustomConfig() : Saves the custom configuration (String)name to file system.
 	 */
 	public static void saveCustomConfig(String name)
 	{
 		if(customConfig == null || customConfigFile == null) return; 
 		
 		try
 		{
 			getCustomConfig(name).save(customConfigFile);
 		}
 		catch(IOException e)
 		{
 			severe("There was an error when saving file: " + name + ".yml");
 			severe("Error: " + e);
 		}
 	}
 
 	/*
 	 *  saveDefaultCustomConfig() : Saves the defaults for custom configuration (String)name to file system.
 	 */
 	public static void saveDefaultCustomConfig(String name)
 	{
 		customConfigFile = new File(plugin.getDataFolder(), name + ".yml");
 		if(!customConfigFile.exists())
 		{
 			plugin.saveResource(name + ".yml", false);
 		}
 	}
 	
 	/*
 	 *  getCustomConfig() : Grabs the custom configuration file from file system.
 	 */
 	public static FileConfiguration getCustomConfig(String name)
 	{
 		if(customConfig == null)
 		{
 			reloadCustomConfig(name);
 		}
 		return customConfig;
 	}
 	
 	/*
 	 *  reloadCustomConfig() : Reloads the custom configuration (String)name to refresh values.
 	 */
 	public static void reloadCustomConfig(String name)
 	{
 		if(customConfigFile == null)
 		{
 			customConfigFile = new File(plugin.getDataFolder(), name + ".yml");
 		}
 		customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
 
 		// Look for defaults in the jar
 		InputStream defConfigStream = plugin.getResource(name + ".yml");
 		if(defConfigStream != null)
 		{
 			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
 			customConfig.setDefaults(defConfig);
 		}
 	}
 	
 	/*
 	 *  hasPermission() : Checks if (Player)player has permission (String)permission.
 	 */
 	public static boolean hasPermission(Player player, String permission)
 	{
 		if(player == null) return true;
 		return player.hasPermission(permission);
 	}
 	
 	/*
 	 *  hasPermissionOrOP() : Checks if (Player)player has permission (String)permission, or is OP.
 	 */
 	public static boolean hasPermissionOrOP(Player player, String permission)
 	{
 		if(player == null) return true;
 		if(player.isOp()) return true;
 		return player.hasPermission(permission);
 	}
 	
 	/*
 	 *  noPermission() : Command sender does not have permission to run command.
 	 */
 	public static boolean noPermission(Player player)
 	{
 		player.sendMessage(ChatColor.RED + "You do not have permission to run this command.");
 		return true;
 	}
 	
 	/*
 	 *  noConsole() : Sends a permission denial message to the console.
 	 */
 	public static boolean noConsole(CommandSender sender)
 	{
 		sender.sendMessage("This command can only be executed by a player.");
 		return true;
 	}
 	
 	/*
 	 *  noPlayer() : Sends a permission denial message to the console.
 	 */
 	public static boolean noPlayer(CommandSender sender)
 	{
 		sender.sendMessage("This command can only be executed by the console.");
 		return true;
 	}
 	
 	/*
 	 *  canUseDeity() : Checks is a player can use a specfic deity.
 	 */
 	public static boolean canUseDeity(Player player, String deity)
 	{		
 		// Check the player for DEITYNAME
 		if(!DUtil.hasDeity(player.getName(), deity))
 		{
 			player.sendMessage(ChatColor.RED + "You haven't even claimed " + deity + "! You can't do that!");
 			return false;
 		}
 		else if(!DUtil.isImmortal(player.getName()))
 		{
 			player.sendMessage(ChatColor.RED + "You can't do that, mortal!");
 			return false;
 		}
 		return true;
 	}
 	
 	/*
 	 *  canUseDeity() : Checks is a player can use a specfic deity.
 	 */
 	public static boolean canUseDeitySilent(String username, String deity)
 	{		
 		// Check the player for DEITYNAME
 		if(!DUtil.hasDeity(username, deity)) return false;
 		else if(!DUtil.isImmortal(username)) return false;
 		return true;
 	}
 	
 	/*
 	 *  canPVP() : Checks if PVP is allowed in (Location)location.
 	 */
     public static boolean canPVP(Location location)
     {
         if(DConfig.getSettingBoolean("allow_skills_anywhere")) return true;
         
         if(canWorldGuardPVP(location) && canFactionsPVP(location)) return true;
         else return false;
     }
 	
     /*
      *  WORLDGUARD SUPPORT START
      */
     @SuppressWarnings("static-access")
     public static boolean canWorldGuardPVP(Location location)
     {
 	    if(DConfig.getSettingBoolean("allow_skills_anywhere")) return true;
 	    if(plugin.WORLDGUARD == null) return true;
 	    
 	    ApplicableRegionSet set = plugin.WORLDGUARD.getRegionManager(location.getWorld()).getApplicableRegions(location);
 	    return set.allows(DefaultFlag.PVP);
     }
 
     @SuppressWarnings("static-access")
     public static boolean canWorldGuardBuild(Player player, Location location)
     {
         if(plugin.WORLDGUARD == null) return true;
         
         return plugin.WORLDGUARD.canBuild(player, location);
     }
 
     @SuppressWarnings("static-access")
     public static boolean canWorldGuardDamage(Location location)
     {
         if(plugin.WORLDGUARD == null) return true;
         
         ApplicableRegionSet set = plugin.WORLDGUARD.getRegionManager(location.getWorld()).getApplicableRegions(location);
         return !set.allows(DefaultFlag.INVINCIBILITY);
     }
             
     /*
      *  WORLDGUARD SUPPORT END
      */
     @SuppressWarnings("static-access")
     public static boolean canFactionsPVP(Location location)
     {
         if(DConfig.getSettingBoolean("allow_skills_anywhere")) return true;
         
         if(plugin.FACTIONS == null) return true;
         Faction faction = Board.getFactionAt(new FLocation(location.getBlock()));
         return !(faction.isPeaceful() || faction.isSafeZone());
     }
 }
