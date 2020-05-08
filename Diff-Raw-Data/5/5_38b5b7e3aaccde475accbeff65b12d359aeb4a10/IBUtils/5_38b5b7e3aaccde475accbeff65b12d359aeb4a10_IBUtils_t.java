 package musician101.itembank.util;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Collection;
 
 import musician101.itembank.Config;
 import musician101.itembank.ItemBank;
 import musician101.itembank.exceptions.InvalidAliasException;
 import musician101.itembank.lib.Messages;
 import musician101.itembank.listeners.PlayerListener;
 
 import org.bukkit.Material;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * @author Musician101
  */
 public class IBUtils
 {
 	/**
 	 * Method for finding specific blocks/items in a player's inventory.
 	 * 
 	 * @param player Player who's inventory is being checked.
 	 * @param material The material that is being searched for.
 	 * @param data The damage value of the material (i.e. if material = oak wood then dmg = 1).
 	 * @return The amount of the material in the player's inventory.
 	 */
 	public static int getAmount(Player player, Material material, short data)
 	{
 		int has = 0;
 		for (ItemStack item : player.getInventory().getContents())
 		{
 			if ((item != null) && (item.getType() == material) && (item.getAmount() > 0) && (item.getDurability() == data))
 				has += item.getAmount();
 		}
 		return has;
 	}
 	
 	/**
 	 * Find's a material's ID and Damage value.
 	 * 
 	 * @param name The alias or Material of an ItemStack.
 	 * @param amount The amount of the ItemStack.
 	 * @return item
 	 */
 	public static ItemStack getItem(String name, int amount)
 	{
 		if (name == null) return null;
 		short data;
 		String datas = null;
 		name = name.trim().toUpperCase();
 		if (name.contains(":"))
 		{
 			if (name.split(":").length < 2)
 			{
 				datas = null;
 				name = name.split(":")[0];
 			}
 			else
 			{
 				datas = name.split(":")[1];
 				name = name.split(":")[0];
 			}
 		}
 		
 		try
 		{
 			data = Short.valueOf(datas);
 		}
 		catch (NumberFormatException e)
 		{
 			if (datas != null) return null;
 			else data = 0;
 		}
 		
 		Material material = Material.getMaterial(name);
 		if (material == null) return null;
 		ItemStack item = new ItemStack(material, amount);
 		if (data != 0) item.setDurability(data);
 		return item;
 	}
 	
 	/**
 	 * Find's a material's ID and Damage value from an alias.
 	 * 
 	 * @param plugin Reference the plugin's main class.
 	 * @param name The alias of the ItemStack.
 	 * @param amount The amount of the ItemStack.
 	 * @return item
 	 * @throws InvalidAliasException Alias not recognized.
 	 * @throws NullPointerException Translator failed to load on startup.
 	 */
 	public static ItemStack getItemFromAlias(ItemBank plugin, String alias, int amount) throws InvalidAliasException, NullPointerException
 	{
 		ItemStack item;
 		if (plugin.translator == null) throw new NullPointerException("Error: ItemTranslator is not loaded.");
 		item = plugin.translator.getItemStackFromAlias(alias);
 		if (item == null) throw new InvalidAliasException(alias + " is not a valid alias!");
 		
 		item.setAmount(amount);
 		return item;
 	}
 	
 	/**
 	 * Creates a file for the specified player.
 	 * 
 	 * @param plugin Reference's the plugin's main class.
 	 * @param player The player who's having their data file created.
 	 */
 	public static void createPlayerFile(ItemBank plugin, File file)
 	{
 		if (!file.exists())
 		{
 			try
 			{
 				file.createNewFile();
 				BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
 				bw.write(PlayerListener.template);
 				bw.close();
 			}
 			catch (IOException e)
 			{
 				plugin.getLogger().warning(Messages.IO_EXCEPTION);
 			}
 		}
 	}
 	
 	/**
 	 * A loop for creating player data files.
 	 * 
 	 * @param plugin Reference's the plugin's main class.
 	 * @param players A list of players who are online when the method is executed.
 	 */
 	public static void createPlayerFiles(ItemBank plugin, Player[] players)
 	{
 		if (players.length > 0)
 		{
 			for (Player player : players)
 			{
 				createPlayerFile(plugin, new File(plugin.playerDataDir + "/" + player.getName().toLowerCase() + ".yml"));
 			}
 		}
 	}
 	
 	/**
 	 * Check if the player has enough money.
 	 * 
 	 * @param plugin Reference's the main class.
 	 * @param config Provides access to the config options.
 	 * @param player The player involved.
 	 * @return false if the player does not have enough money, else true.
 	 */
 	public static boolean checkEconomy(ItemBank plugin, Config config, Player player)
 	{
 		if (!(plugin.getEconomy().isEnabled() && config.enableVault))
 			return true;
 			
 		double money = plugin.getEconomy().getMoney(player.getName());
 		double cost = config.transactionCost;
 		if (money < cost)
 			return false;
 		
 		return true;
 	}
 	
 	/**
 	 * Takes a list and joins it into a string separated by the given separator.
 	 * 
 	 * @param separator The character/characters used to separator the contents of a list.
 	 * @param list The list to be turned into a single string.
 	 * @return
 	 */
 	public static String joinList(String separator, Object... list)
 	{
 		StringBuilder sb = new StringBuilder();
 		for (Object object : list)
 		{
 			if (sb.length() > 0)
 				sb.append(separator);
 			
 			if (object instanceof Collection)
 				sb.append(joinList(separator, ((Collection<?>) object).toArray()));
 			else
 				sb.append(object.toString());
 		}
 		
 		return sb.toString();
 	}
 	
 	/**
 	 * Check if a command sender is a player or the console.
 	 * 
 	 * @param sender Who sent the command.
 	 * @return false if the sender is the console, else true.
 	 */
 	public static boolean isPlayer(CommandSender sender)
 	{
 		if (!(sender instanceof Player))
 		{
 			sender.sendMessage(Messages.PLAYER_COMMAND_ONLY);
 			return false;
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Loads a player's data file.
 	 * 
 	 * @param plugin Plugin instance.
 	 * @param player Player modifying the account.
 	 * @param playerName Player who's account is being modified.
 	 * @return false if an exception is thrown, else true.
 	 */
 	public static boolean loadPlayerFile(ItemBank plugin, Player player, String playerName)
 	{
 		plugin.playerFile = new File(plugin.playerDataDir + "/" + playerName + ".yml");
 		plugin.playerData = new YamlConfiguration();
 		try
 		{
 			plugin.playerData.load(plugin.playerFile);
 		}
 		catch (FileNotFoundException e)
 		{
 			player.sendMessage(Messages.FILE_NOT_FOUND);
 			return false;
 		}
 		catch (IOException e)
 		{
 			player.sendMessage(Messages.IO_EXCEPTION);
 			return false;
 		}
 		catch (InvalidConfigurationException e)
 		{
 			player.sendMessage(Messages.YAML_EXCEPTION);
 			return false;
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Saves the player's data file.
 	 * 
 	 * @param plugin Plugin instance.
 	 * @param player Player who's modifying the account.
 	 * @param path The ItemPath, in case an exception occurs.
 	 * @param amount The old amount, in case an exception occurs.
 	 * @return false if an exception is thrown, else true.
 	 */
 	public static boolean savePlayerFile(ItemBank plugin, Player player, String path, int amount)
 	{
 		try
 		{
 			plugin.playerData.save(plugin.playerFile);
 		}
 		catch (IOException e)
 		{
 			player.sendMessage(Messages.IO_EXCEPTION);
 			plugin.playerData.set(path, amount);
 			return false;
 		}
 		
 		return true;
 	}
 }
