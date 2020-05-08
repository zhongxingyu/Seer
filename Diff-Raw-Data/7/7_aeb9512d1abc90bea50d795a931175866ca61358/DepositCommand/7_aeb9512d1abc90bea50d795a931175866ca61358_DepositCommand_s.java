 package musician101.itembank.commands.dwcommands;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import musician101.itembank.Config;
 import musician101.itembank.ItemBank;
 import musician101.itembank.exceptions.InvalidAliasException;
 import musician101.itembank.lib.Constants;
 import musician101.itembank.util.IBUtils;
 
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * The code used to run when the Deposit command is executed.
  * 
  * @author Musician101
  */
 public class DepositCommand implements CommandExecutor
 {
 	ItemBank plugin;
 	Config config;
 	
 	/**
 	 * @param plugin References the plugin's main class.
 	 * @param config References the config options.
 	 */
 	public DepositCommand(ItemBank plugin, Config config)
 	{
 		this.plugin = plugin;
 		this.config = config;
 	}
 	
 	/**
 	 * @param sender Who sent the command.
 	 * @param command Which command was executed.
 	 * @param label Alias of the command.
 	 * @param args Command parameters.
 	 * @return True if the command was successfully executed.
 	 */
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
 	{
 		if (command.getName().equalsIgnoreCase(Constants.DEPOSIT_CMD))
 		{
 			if (!sender.hasPermission(Constants.DEPOSIT_PERM))
 			{
 				sender.sendMessage(Constants.NO_PERMISSION);
 				return false;
 			}
 			
 			if (!(sender instanceof Player) && !args[0].equalsIgnoreCase(Constants.ADMIN_CMD))
 			{
 				sender.sendMessage(Constants.PLAYER_COMMAND_ONLY);
 				return false;
 			}
 			
 			if (args.length == 0)
 			{
 				ItemStack item = ((Player) sender).getItemInHand();
 				if (item == null || item.getType() == Material.AIR)
 				{
 					sender.sendMessage(Constants.AIR_BLOCK);
 					return false;
 				}
 				
 				/** Custom Item Check */
 				if (item.hasItemMeta() || item.getType() == Material.FIREWORK || item.getType() == Material.WRITTEN_BOOK || (item.getType() == Material.SKULL_ITEM && item.getDurability() == 3))
 					return CustomItem.deposit(plugin, item, (Player) sender);
 				
 				String itemPath = item.getType().toString().toLowerCase() + "." + item.getDurability();
 				plugin.playerFile = new File(plugin.playerDataDir + "/" + sender.getName().toLowerCase() + ".yml");
 				plugin.playerData = new YamlConfiguration();
 				try
 				{
 					plugin.playerData.load(plugin.playerFile);
 				}
 				catch (FileNotFoundException e)
 				{
 					sender.sendMessage(Constants.FILE_NOT_FOUND);
 					return false;
 				}
 				catch (IOException e)
 				{
 					sender.sendMessage(Constants.IO_EXCEPTION);
 					return false;
 				}
 				catch (InvalidConfigurationException e)
 				{
 					sender.sendMessage(Constants.YAML_EXCEPTION);
 					return false;
 				}
 				
 				int oldAmount = plugin.playerData.getInt(itemPath);
 				int amount = item.getAmount();
 				int newAmount = oldAmount + amount;
 				if (config.blacklist.isSet(itemPath))
 				{
 					int maxAmount = config.blacklist.getInt(itemPath);
 					if (maxAmount == 0)
 					{
 						sender.sendMessage(Constants.NO_DEPOSIT);
 						return false;
 					}
 					else if (maxAmount == oldAmount)
 					{
 						sender.sendMessage(Constants.getMaxedDepositMessage(item.getType().toString()));
 					}
 					else if (maxAmount < newAmount)
 					{
 						amount = maxAmount - oldAmount;
 						newAmount = oldAmount + amount;
 						sender.sendMessage(Constants.PARTIAL_DEPOSIT);
 					}
 				}
 				
 				plugin.playerData.set(itemPath, newAmount);
 				try
 				{
 					plugin.playerData.save(plugin.playerFile);
 				}
 				catch (IOException e)
 				{
 					sender.sendMessage(Constants.IO_EXCEPTION);
 					plugin.playerData.set(itemPath, oldAmount);
 					return false;
 				}
 				
 				item.setAmount(amount);
 				((Player) sender).getInventory().removeItem(item);
 				sender.sendMessage(Constants.getDepositSuccess(item.getType().toString(), item.getAmount()));
 				if (plugin.getEconomy().isEnabled() && config.enableVault)
 				{
 					sender.sendMessage(Constants.getTransactionFeeMessage(config.transactionCost));
 					plugin.getEconomy().takeMoney(sender.getName(), config.transactionCost);
 				}
 				return true;
 			}
 			
 			/** Admin Deposit Check */
 			if (args[0].equalsIgnoreCase(Constants.ADMIN_CMD))
 				return Admin.deposit(plugin, (Player) sender, args);
 			
 			/** Economy Check */
 			if (!IBUtils.checkEconomy(plugin, config, (Player) sender))
 				return false;
 			
 			/** Custom Item Check */
 			if (args[0].equalsIgnoreCase(Constants.CUSTOM_ITEM))
 				return CustomItem.deposit(plugin, ((Player) sender).getItemInHand(), (Player) sender);
 			
 			String name = args[0].toLowerCase();
 			int amount = 64;
 			if (args.length == 2)
 			{
 				try
 				{
 					amount = Integer.parseInt(args[1]);
 				}
 				catch (NumberFormatException e)
 				{
 					sender.sendMessage(Constants.NUMBER_FORMAT);
 					return false;
 				}
 			}
 			
 			ItemStack item = null;
 			try
 			{
 				item = IBUtils.getItemFromAlias(plugin, name, amount);
 			}
 			catch (InvalidAliasException e)
 			{
 				item = IBUtils.getItem(name, amount);
 			}
 			catch (NullPointerException e)
 			{
 				sender.sendMessage(Constants.NULL_POINTER);
 				return false;
 			}
 			
 			if (item == null)
 			{
 				sender.sendMessage(Constants.getAliasError(name));
 				return false;
 			}
 			
 			if (item.getType() == Material.AIR)
 			{
 				sender.sendMessage(Constants.AIR_BLOCK);
 				return false;
 			}
 			
 			String itemPath = item.getType().toString().toLowerCase() + "." + item.getDurability();
 			plugin.playerFile = new File(plugin.playerDataDir + "/" + sender.getName().toLowerCase() + ".yml");
 			plugin.playerData = new YamlConfiguration();
 			try
 			{
 				plugin.playerData.load(plugin.playerFile);
 			}
 			catch (FileNotFoundException e)
 			{
 				sender.sendMessage(Constants.FILE_NOT_FOUND);
 				return false;
 			}
 			catch (IOException e)
 			{
 				sender.sendMessage(Constants.IO_EXCEPTION);
 				return false;
 			}
 			catch (InvalidConfigurationException e)
 			{
 				sender.sendMessage(Constants.YAML_EXCEPTION);
 				return false;
 			}
 			
 			if (!((Player) sender).getInventory().contains(item))
 			{
 				sender.sendMessage(Constants.PREFIX + "Error: You do not have any of the specified item.");
 				return false;
 			}
 			
 			int oldAmount = plugin.playerData.getInt(itemPath);
 			int amountInInv = IBUtils.getAmount((Player) sender, item.getType(), item.getDurability());
 			if (amountInInv < amount)
 				amount = amountInInv;
 			
 			int newAmount = oldAmount + amount;
 			if (config.blacklist.isSet(itemPath))
 			{
 				int maxAmount = config.blacklist.getInt(itemPath);
 				if (maxAmount == 0)
 				{
 					sender.sendMessage(Constants.NO_DEPOSIT);
 					return false;
 				}
 				else if (maxAmount == oldAmount)
 				{
 					sender.sendMessage(Constants.getMaxedDepositMessage(item.getType().toString()));
 					return false;
 				}
 				else if (maxAmount < newAmount)
 				{
 					amount = maxAmount - oldAmount;
 					newAmount = oldAmount + amount;
 					sender.sendMessage(Constants.PARTIAL_DEPOSIT);
 				}
 			}
 			
 			plugin.playerData.set(itemPath, newAmount);
 			try
 			{
 				plugin.playerData.save(plugin.playerFile);
 			}
 			catch (IOException e)
 			{
 				sender.sendMessage(Constants.IO_EXCEPTION);
 				plugin.playerData.set(itemPath, oldAmount);
				if (plugin.getEconomy().isEnabled() && config.enableVault)
					plugin.getEconomy().giveMoney(sender.getName(), config.transactionCost);
 				return false;
 			}
 			
 			item.setAmount(amount);
 			((Player) sender).getInventory().removeItem(item);
 			sender.sendMessage(Constants.getDepositSuccess(item.getType().toString(), item.getAmount()));
 			if (plugin.getEconomy().isEnabled() && config.enableVault)
 				sender.sendMessage(Constants.getTransactionFeeMessage(config.transactionCost));
 			
 			return true;
 		}
 		return false;
 	}
 }
