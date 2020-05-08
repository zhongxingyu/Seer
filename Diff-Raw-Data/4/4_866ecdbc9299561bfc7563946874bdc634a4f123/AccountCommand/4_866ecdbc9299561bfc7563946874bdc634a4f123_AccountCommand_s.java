 package musician101.itembank.commands.ibcommand;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import musician101.itembank.ItemBank;
 import musician101.itembank.lib.Constants;
 
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.MemorySection;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.inventory.meta.SkullMeta;
 
 public class AccountCommand
 {
 	private static Map<String, Object> amounts = new HashMap<String, Object>();
 	
 	public static boolean execute(ItemBank plugin, CommandSender sender, String[] args)
 	{
 		if (!sender.hasPermission(Constants.ACCOUNT_PERM))
 		{
 			sender.sendMessage(Constants.NO_PERMISSION);
 			return false;
 		}
 		
 		if (args.length == 1)
 		{
 			if (!(sender instanceof Player))
 			{
 				sender.sendMessage(Constants.PLAYER_COMMAND_ONLY);
 				return false;
 			}
 			
 			if (!getAccount(plugin, (Player) sender, sender.getName()))
 				return false;
 			
 			return displayAccount(plugin, sender, args);
 		}
 		else if (args.length == 2)
 		{
 			if (!(sender instanceof Player))
 			{
 				sender.sendMessage(Constants.PLAYER_COMMAND_ONLY);
 				return false;
 			}
 			
 			if (!getAccount(plugin, (Player) sender, sender.getName()))
 				return false;
 			
 			if (args[1].equalsIgnoreCase(Constants.ADMIN_CMD))
 			{
 				if (!sender.hasPermission(Constants.ADMIN_PERM))
 					sender.sendMessage(Constants.NO_PERMISSION);
 				else
 					sender.sendMessage(Constants.NOT_ENOUGH_ARGUMENTS);
 				
 				return false;
 			}
 			
 			String name = args[1];
 			if (!plugin.playerData.isSet(name))
 			{
 				sender.sendMessage(Constants.PREFIX + "You have 0 " + name + ".");
 				return true;
 			}
 			
 			for (Map.Entry<String, Object> entry : amounts.entrySet())
 			{
 				int amount = Integer.valueOf(entry.getValue().toString());
 				String[] pathSplit = entry.getKey().split("\\.");
 				ItemStack item = null;
 				short durability = 0;
 				
 				if (pathSplit[0].equals(name))
 				{
 					try
 					{
 						durability = Short.valueOf(pathSplit[1]);
 					}
 					catch (NumberFormatException e)
 					{
 						if (pathSplit[1].contains("amount"))
 						{
 							if (plugin.playerData.isSet(pathSplit[0] + ".durability"))
 								durability = Short.valueOf(plugin.playerData.getString(pathSplit[0] + ".durability"));
 						}
 						else
 						{
 							sender.sendMessage(Constants.getFileDurabilityError(pathSplit[0]));
 							return false;
 						}
 					}
 					
 					if (Material.getMaterial(pathSplit[0].toUpperCase()) != null)
 						item = new ItemStack(Material.getMaterial(pathSplit[0].toUpperCase()), amount, durability);
 					else
 					{
 						item = new ItemStack(Material.getMaterial(plugin.playerData.getString(pathSplit[0] + ".material")), amount, durability);
 						if (item.getType() != Material.getMaterial(pathSplit[0].toUpperCase()))
 						{
 							ItemMeta meta = item.getItemMeta();
 							meta.setDisplayName(pathSplit[0].replace("_", " "));
 							item.setItemMeta(meta);
 						}
 					}
 					sender.sendMessage(Constants.PREFIX + getName(item) + ChatColor.WHITE + ": " + item.getAmount());
 				}
 			}
 			return true;
 		}
 		else if (args.length == 3)
 		{
 			if (args[1].equalsIgnoreCase(Constants.ADMIN_CMD))
 			{
 				if (!sender.hasPermission(Constants.ADMIN_PERM))
 				{
 					sender.sendMessage(Constants.NO_PERMISSION);
 					return false;
 				}
 				
 				if (!getAccount(plugin, sender, args[2]))
 					return false;
 				
 				return displayAccount(plugin, sender, args);
 			}
 		}
 		return false;
 	}
 	
 	public static boolean displayAccount(ItemBank plugin, CommandSender sender, String[] args)
 	{
		if (args[1].equalsIgnoreCase(Constants.ADMIN_CMD))
 			sender.sendMessage("--------" + ChatColor.DARK_RED + args[2] + "'s ItemBank Account" + ChatColor.WHITE + "--------");
 		else
 			sender.sendMessage("--------" + ChatColor.DARK_RED + "Your ItemBank Account" + ChatColor.WHITE + "--------");
 		for (Map.Entry<String, Object> entry : amounts.entrySet())
 		{
 			int amount = 0;
 			
 			try
 			{
 				amount = Integer.valueOf(entry.getValue().toString());
 			}
 			catch (NumberFormatException e)
 			{
 				sender.sendMessage(Constants.getFileAmountError(entry.getKey().toUpperCase()));
 				return false;
 			}
 			
 			if (amount != 0)
 			{					
 				String[] pathSplit = entry.getKey().split("\\.");					
 				ItemStack item = null;
 				short durability = 0;
 				try
 				{
 					durability = Short.valueOf(pathSplit[1]);
 				}
 				catch (NumberFormatException e)
 				{
 					if (pathSplit[1].contains("amount"))
 					{
 						if (plugin.playerData.isSet(pathSplit[0] + ".durability"))
 							durability = Short.valueOf(plugin.playerData.getString(pathSplit[0] + ".durability"));
 					}
 					else
 					{
 						sender.sendMessage(Constants.getFileDurabilityError(pathSplit[0]));
 						return false;
 					}
 				}
 				
 				if (Material.getMaterial(pathSplit[0].toUpperCase()) != null)
 					item = new ItemStack(Material.getMaterial(pathSplit[0].toUpperCase()), amount, durability);
 				else
 				{
 					item = new ItemStack(Material.getMaterial(plugin.playerData.getString(pathSplit[0] + ".material")), amount, durability);
 					if (item.getType() != Material.getMaterial(pathSplit[0].toUpperCase()))
 					{
 						ItemMeta meta = item.getItemMeta();
 						meta.setDisplayName(pathSplit[0].replace("_", " "));
 						item.setItemMeta(meta);
 					}
 				}
 				sender.sendMessage(ChatColor.DARK_RED + getName(item) + ChatColor.WHITE + ": " + item.getAmount());
 			}
 		}
 		
 		return true;
 	}
 	
 	public static boolean getAccount(ItemBank plugin, CommandSender sender, String playerName)
 	{
 		plugin.playerFile = new File(plugin.playerDataDir + "/" + playerName.toLowerCase() + ".yml");
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
 		
 		for (Map.Entry<String, Object> entry : plugin.playerData.getValues(true).entrySet())
 		{
 			if (!(entry.getValue() instanceof MemorySection))
 			{
 				if (Material.getMaterial(entry.getKey().split("\\.")[0].toUpperCase()) == null)
 				{
 					if (entry.getKey().contains("amount"))
 						amounts.put(entry.getKey(), entry.getValue());
 				}
 				else if (Material.getMaterial(entry.getKey().split("\\.")[0].toUpperCase()) != null)
 				{
 					try
 					{
 						if (Short.valueOf(entry.getKey().split("\\.")[1]) >= 0)
 							amounts.put(entry.getKey(), entry.getValue());
 					}
 					catch (NumberFormatException e)
 					{
 						List<String> children = new ArrayList<String>(Arrays.asList("material", "durability", "enchantments", "lore", "power", "effects"));
 						if (entry.getKey().contains("amount"))
 							amounts.put(entry.getKey(), entry.getValue());
 						else if (!children.contains(entry.getKey().split("\\.")[1]))
 						{
 							sender.sendMessage(Constants.getFileAmountError(entry.getKey().split("\\.")[0]));
 							return false;
 						}
 					}
 				}
 			}
 		}
 		return true;
 	}
 	
 	public static String getName(ItemStack item)
 	{
 		List<Material> generalBlocksItems = new ArrayList<Material>(Arrays.asList(Material.SANDSTONE, Material.LONG_GRASS, Material.STEP, Material.SMOOTH_BRICK, Material.ANVIL,
 			Material.QUARTZ_BLOCK, Material.COAL, Material.GOLDEN_APPLE, Material.INK_SACK, Material.POTION, Material.SKULL_ITEM));
 		List<Material> coloredBlocks = new ArrayList<Material>(Arrays.asList(Material.WOOL, Material.STAINED_CLAY, Material.CARPET));
 		List<Material> woodBlocks = new ArrayList<Material>(Arrays.asList(Material.WOOD, Material.SAPLING, Material.LOG, Material.LEAVES, Material.WOOD_STEP));
 		List<Material> durabilityItems = new ArrayList<Material>(Arrays.asList(Material.IRON_SPADE, Material.IRON_AXE, Material.FLINT_AND_STEEL, Material.BOW, Material.IRON_SWORD,
 			Material.WOOD_SWORD, Material.WOOD_SPADE,Material.WOOD_PICKAXE, Material.WOOD_AXE, Material.STONE_SWORD, Material.STONE_SPADE, Material.STONE_PICKAXE, Material.STONE_AXE, Material.DIAMOND_SWORD,
 			Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, Material.GOLD_SWORD, Material.GOLD_SPADE, Material.GOLD_PICKAXE, Material.WOOD_HOE, Material.STONE_HOE,
 			Material.IRON_HOE, Material.DIAMOND_HOE, Material.GOLD_HOE, Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS,
 			Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS, Material.IRON_HELMET, Material.IRON_CHESTPLATE,
 			Material.IRON_LEGGINGS, Material.IRON_BOOTS, Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,
 			Material.GOLD_HELMET, Material.GOLD_CHESTPLATE, Material.GOLD_LEGGINGS, Material.GOLD_BOOTS, Material.FISHING_ROD, Material.CARROT_STICK));
 		String name = item.getType().toString().charAt(0) + item.getType().toString().substring(1).toLowerCase();
 		if (name.contains("_"))
 		{
 			if (StringUtils.countMatches(name, "_") == 2)
 				name = name.charAt(0) + name.substring(1, name.indexOf("_")).toLowerCase() + " " + name.toUpperCase().charAt(name.indexOf("_") + 1) + name.substring(name.indexOf("_") + 2, name.lastIndexOf("_")).toLowerCase() +
 					" " + name.toUpperCase().charAt(name.lastIndexOf("_") + 1) + name.substring(name.lastIndexOf("_") + 2, name.length()).toLowerCase();
 			else
 				name = name.charAt(0) + name.substring(1, name.indexOf("_")).toLowerCase() + " " + name.toUpperCase().charAt(name.indexOf("_") + 1) + name.substring(name.indexOf("_") + 2, name.length()).toLowerCase();
 		}
 		
 		if (generalBlocksItems.contains(item.getType()) || woodBlocks.contains(woodBlocks) || coloredBlocks.contains(item.getType()))
 		{
 			if (coloredBlocks.contains(item.getType()))
 			{
 				switch (item.getDurability())
 				{
 					case 0:
 						name = name + " (White)";
 						break;
 					case 1:
 						name = name + " (Orange)";
 						break;
 					case 2:
 						name = name + " (Magenta)";
 						break;
 					case 3:
 						name = name + " (Light Blue)";
 						break;
 					case 4:
 						name = name + " (Yellow)";
 						break;
 					case 5:
 						name = name + " (Lime)";
 						break;
 					case 6:
 						name = name + " (Pink)";
 						break;
 					case 7:
 						name = name + " (Dark Gray)";
 						break;
 					case 8:
 						name = name + " (Gray)";
 						break;
 					case 9:
 						name = name + " (Cyan)";
 						break;
 					case 10:
 						name = name + " (Purple)";
 						break;
 					case 11:
 						name = name + " (Blue)";
 						break;
 					case 12:
 						name = name + " (Brown)";
 						break;
 					case 13:
 						name = name + " (Green)";
 						break;
 					case 14:
 						name = name + " (Red)";
 						break;
 					case 15:
 						name = name + " (Black)";
 						break;
 					default:
 						name = name + " (" + item.getDurability() + ")";
 						break;
 				}
 			}
 			else if (woodBlocks.contains(item.getType()))
 			{
 				switch (item.getDurability())
 				{
 					case 0:
 						name = name + " (Oak)";
 						break;
 					case 1:
 						name = name + " (Spruce)";
 						break;
 					case 2:
 						name = name + " (Brich)";
 						break;
 					case 3:
 						name = name + " (Jungle)";
 						break;
 					default:
 						name = name + " (" + item.getDurability() + ")";
 						break;
 				}
 			}
 			else if (item.getType() == Material.SANDSTONE)
 			{
 				switch (item.getDurability())
 				{
 					case 0:
 						break; //Do nothing to preserve block name
 					case 1:
 						name = name + " (Chiseled)";
 						break;
 					case 2:
 						name = name + " (Smooth)";
 						break;
 					default:
 						name = name + " (" + item.getDurability() + ")";
 						break;
 				}
 			}
 			else if (item.getType() == Material.LONG_GRASS)
 			{
 				switch (item.getDurability())
 				{
 					case 0:
 						name = name + " (Shrub)";
 						break;
 					case 1:
 						name = name + " (Grass)";
 						break;
 					case 2:
 						name = name + " (Fern)";
 						break;
 					default:
 						name = name + " (" + item.getDurability() + ")";
 						break;
 				}
 			}
 			else if (item.getType() == Material.STEP)
 			{
 				switch (item.getDurability())
 				{
 					case 0:
 						name = name + " (Stone)";
 						break;
 					case 1:
 						name = name + " (Sandstone)";
 						break;
 					case 3:
 						name = name + " (Cobblestone)";
 						break;
 					case 4:
 						name = name + " (Brick)";
 						break;
 					case 5:
 						name = name + " (Stone Brick)";
 						break;
 					case 6:
 						name = name + " (Nether Brick)";
 						break;
 					case 7:
 						name = name + " (Quartz)";
 						break;
 					default:
 						name = name + " (" + item.getDurability() + ")";
 						break;
 				}
 			}
 			else if (item.getType() == Material.SMOOTH_BRICK)
 			{
 				switch (item.getDurability())
 				{
 					case 0:
 						break; //Do nothing to preserve block name
 					case 1:
 						name = name + " (Mossy)";
 						break;
 					case 2:
 						name = name + " (Cracked)";
 						break;
 					case 3:
 						name = name + " (Chiseled)";
 						break;
 					default:
 						name = name + " (" + item.getDurability() + ")";
 						break;
 				}
 			}
 			else if (item.getType() == Material.ANVIL)
 			{
 				switch (item.getDurability())
 				{
 					case 0:
 						break; //Do nothing to preserve block name
 					case 1:
 						name = name + " (Slightly Damaged)";
 						break;
 					case 2:
 						name = name + " (Very Damaged)";
 						break;
 					default:
 						name = name + " (" + item.getDurability() + ")";
 						break;
 				}
 			}
 			else if (item.getType() == Material.QUARTZ_BLOCK)
 			{
 				switch (item.getDurability())
 				{
 					case 0:
 						break; //Do nothing to preserve block name
 					case 1:
 						name = name + " (Chiseled)";
 						break;
 					case 2:
 						name = name + " (Pillar)";
 						break;
 					default:
 						name = name + " (" + item.getDurability() + ")";
 						break;
 				}
 			}
 			else if (item.getType() == Material.COAL)
 			{
 				switch (item.getDurability())
 				{
 					case 0:
 						break; //Do nothing to preserve block name
 					case 1:
 						name = "Charcoal";
 						break;
 					default:
 						name = name + " (" + item.getDurability() + ")";
 						break;
 				}
 			}
 			else if (item.getType() == Material.GOLDEN_APPLE)
 			{
 				switch (item.getDurability())
 				{
 					case 0:
 						break; //Do nothing to preserve item name
 					case 1:
 						name = name + " (Enchanted)";
 						break;
 					default:
 						name = name + " (" + item.getDurability() + ")";
 						break;
 				}
 			}
 			else if (item.getType() == Material.INK_SACK)
 			{
 				switch (item.getDurability())
 				{
 					case 0:
 						break; //Do nothing to preserve item name
 					case 1:
 						name = name + " (Rose Red)";
 						break;
 					case 2:
 						name = name + " (Cactus Green)";
 						break;
 					case 3:
 						name = name + " (Cocoa Beans)";
 						break;
 					case 4:
 						name = name + " (Lapis Lazuli)";
 						break;
 					case 5:
 						name = name + " (Purple Dye)";
 						break;
 					case 6:
 						name = name + " (Cyan Dye)";
 						break;
 					case 7:
 						name = name + " (Light Gray Dye)";
 						break;
 					case 8:
 						name = name + " (Gray Dye)";
 						break;
 					case 9:
 						name = name + " (Pink Dye)";
 						break;
 					case 10:
 						name = name + " (Lime Dye)";
 						break;
 					case 11:
 						name = name + " (Dandelion Yellow)";
 						break;
 					case 12:
 						name = name + " (Light Blue Dye)";
 						break;
 					case 13:
 						name = name + " (Magenta Dye)";
 						break;
 					case 14:
 						name = name + " (Orange Dye)";
 						break;
 					case 15:
 						name = name + " (Bone Meal)";
 						break;
 					default:
 						name = name + " (" + item.getDurability() + ")";
 						break;
 				}
 			}
 			else if (item.getType() == Material.POTION)
 			{
 				switch (item.getDurability())
 				{
 					case 0:
 						name = "Water Bottle";
 						break;
 					case 16:
 						name = "Awkward Potion";
 						break;
 					case 32:
 						name = "Thick Potion";
 						break;
 					case 64:
 						name = "Mundane Potion (Extended)";
 						break;
 					case 8192:
 						name = "Mundane Potion";
 						break;
 					case 8193:
 						name = "Potion of Regeneration";
 						break;
 					case 8257:
 						name = "Potion of Regeneration (Extended)";
 						break;
 					case 8225:
 						name = "Potion of Regeneration II";
 						break;
 					case 8194:
 						name = "Potion of Swiftness";
 						break;
 					case 8258:
 						name = "Potion of Swiftness (Extended)";
 						break;
 					case 8226:
 						name = "Potion of Swiftness II";
 						break;
 					case 8195:
 						name = "Potion of Fire Resistance";
 						break;
 					case 8259:
 						name = "Potion of Fire Resistance (Extended)";
 						break;
 					case 8197:
 						name = "Potion of Healing";
 						break;
 					case 8229:
 						name = "Potion of Healing II";
 						break;
 					case 8198:
 						name = "Potion of Night Vision";
 						break;
 					case 8262:
 						name = "Potion of Night Vision (Extended)";
 						break;
 					case 8201:
 						name = "Potion of Strength";
 						break;
 					case 8265:
 						name = "Potion of Strength (Extended)";
 						break;
 					case 8233:
 						name = "Potion of Strength II";
 						break;
 					case 8206:
 						name = "Potion of Invisibility";
 						break;
 					case 8270:
 						name = "Potion of Invisibility (Extended)";
 						break;
 					case 8196:
 						name = "Potion of Poison";
 						break;
 					case 8260:
 						name = "Potion of Poison (Extended)";
 						break;
 					case 8228:
 						name = "Potion of Posion II";
 						break;
 					case 8200:
 						name = "Potion of Weakness";
 						break;
 					case 8264:
 						name = "Potion of Weakness (Extended)";
 						break;
 					case 8202:
 						name = "Potion of Slowness";
 						break;
 					case 8266:
 						name = "Potion of Slowness (Extended)";
 						break;
 					case 8204:
 						name = "Potion of Harming";
 						break;
 					case 8236:
 						name = "Potion of Harming II";
 						break;
 					default:
 						name = name + " (" + item.getDurability() + ")";
 						break;
 				}
 			}
 			else if (item.getType() == Material.SKULL_ITEM)
 			{
 				switch (item.getDurability())
 				{
 					case 0:
 						name = "Skeleteon Skull";
 						break;
 					case 1:
 						name = "Wither Skeleton Skull";
 						break;
 					case 2:
 						name = "Zombie Head";
 						break;
 					case 3:
 						name = "Player Head";
 						break;
 					case 4:
 						name = "Creeper Head";
 						break;
 					default:
 						name = name + " (" + item.getDurability() + ")";
 						break;
 				}
 			}
 		}
 		
 		if (durabilityItems.contains(item.getType()))
 			name = name + " (Uses: " + item.getDurability() + ")";
 		
 		if (item.hasItemMeta())
 		{
 			name = item.getItemMeta().getDisplayName() + " (Material: " + item.getType();
 			if (durabilityItems.contains(item.getType()))
 			{
 				name = name + " Uses: " + item.getDurability() + ")";
 			}
 			else
 			{
 				name = name + ")";
 			}
 		}
 		
 		if (item.getType() == Material.SKULL_ITEM && ((SkullMeta) item.getItemMeta()).hasOwner())
 			name = name + "'s Head";
 		
 		return name;
 	}
 }
