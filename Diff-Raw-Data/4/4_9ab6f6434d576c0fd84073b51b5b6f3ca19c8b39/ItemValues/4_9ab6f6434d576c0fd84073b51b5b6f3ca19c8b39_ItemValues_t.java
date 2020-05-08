 package com.censoredsoftware.demigods.util;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 
 import com.censoredsoftware.demigods.data.TributeData;
 
 public class ItemValues
 {
 	/**
 	 * Initialized the tribute map with some base data. This prevents fresh data from being out of whack.
 	 */
 	public static void initializeTributeTracking()
 	{
 		for(Material material : Material.values())
 		{
 			// Fill it with random data
 			String category = getCategory(material);
			if(TributeData.Util.find(category, material) == null) TributeData.Util.save(category, material, Randoms.generateIntRange(10, 64));
 		}
 	}
 
 	/**
 	 * Returns all saved tribute data with attached current value.
 	 * 
 	 * @return a Map of all tribute data.
 	 */
 	public static Map<Material, Integer> getTributeValuesMap()
 	{
 		return new HashMap<Material, Integer>()
 		{
 			{
 				for(TributeData data : TributeData.Util.getAll())
 				{
 					put(data.getMaterial(), getValue(data.getMaterial()));
 				}
 			}
 		};
 	}
 
 	/**
 	 * Returns the total number of tributes for the entire server.
 	 * 
 	 * @return the total server tributes.
 	 */
 	public static int getTotalTributes()
 	{
 		int total = 1;
 		for(TributeData data : TributeData.Util.getAll())
 			total += data.getAmount();
 		return total;
 	}
 
 	/**
 	 * Returns the number of tributes for the <code>material</code>.
 	 * 
 	 * @param material the material to check.
 	 * @return the total number of tributes.
 	 */
 	public static int getTributes(Material material)
 	{
 		TributeData data = TributeData.Util.find(getCategory(material), material);
 		if(data != null) return data.getAmount();
 		else return 1;
 	}
 
 	/**
 	 * Returns the number of tributes for the <code>category</code>.
 	 * 
 	 * @param category the category to check.
 	 * @return the total number of tributes.
 	 */
 	public static int getTributesForCategory(String category)
 	{
 		int total = 1;
 		for(TributeData data : TributeData.Util.findByCategory(category))
 			total += data.getAmount();
 		return total;
 	}
 
 	/**
 	 * Saves the <code>item</code> amount into the server tribute stats.
 	 * 
 	 * @param item the item whose amount to save.
 	 */
 	public static void saveTribute(ItemStack item)
 	{
 		TributeData data = TributeData.Util.find(getCategory(item.getType()), item.getType());
 
 		if(data != null)
 		{
 			data.setAmount(data.getAmount() + item.getAmount());
 		}
 		else
 		{
 			TributeData.Util.save(getCategory(item.getType()), item.getType(), item.getAmount());
 		}
 	}
 
 	/**
 	 * Returns the value for a <code>material</code>.
 	 */
 	public static int getValue(Material material)
 	{
 		return getValue(new ItemStack(material));
 	}
 
 	/**
 	 * Returns the value for the <code>item</code> based on current tribute stats.
 	 * 
 	 * @param item the item whose value to calculate.
 	 * @return the value of the item.
 	 */
 	public static int getValue(ItemStack item)
 	{
 		// Define values for reference
 		int categoryTributes = getTributesForCategory(getCategory(item.getType()));
 		double baseValue = getBaseTributeValue(item.getType());
 		int totalItemTributes = getTributes(item.getType());
 
 		// Calculate value
 		double multiplier = categoryTributes / totalItemTributes;
 		if(multiplier < baseValue) multiplier = baseValue;
 
 		// Return the value
 		return (int) Math.ceil(multiplier * item.getAmount());
 	}
 
 	/**
 	 * Returns the category of the <code>material</code>.
 	 * 
 	 * @param material the material whose category to check
 	 * @return the category
 	 */
 	public static String getCategory(Material material)
 	{
 		switch(material)
 		{
 			case DIAMOND:
 			case EMERALD:
 			case GOLD_INGOT:
 			case IRON_INGOT:
 				return "clean_ore";
 			case DIAMOND_ORE:
 			case IRON_ORE:
 			case GOLD_ORE:
 			case LAPIS_ORE:
 				return "raw_ore";
 			case DIRT:
 			case GRASS:
 			case STONE:
 			case COBBLESTONE:
 			case SAND:
 			case SANDSTONE:
 			case WOOD:
 			case LOG:
 				return "building_block";
 			default:
 				return "default";
 		}
 	}
 
 	/**
 	 * Called when actually tributing the <code>item</code>.
 	 * 
 	 * @param item the item to process.
 	 * @return the value of the item.
 	 */
 	public static int processTribute(ItemStack item)
 	{
 		// Grab the value before
 		int value = getValue(item);
 
 		// Save the tribute to be used in calculations later
 		saveTribute(item);
 
 		// Return the value
 		return value;
 	}
 
 	/**
 	 * Returns the base value of the <code>material</code>.
 	 * 
 	 * @param material the material whose value to return.
 	 * @return the base value of the item.
 	 */
 	public static double getBaseTributeValue(Material material)
 	{
 		// TODO: BALANCE THIS SHIT.
 
 		double value;
 		switch(material)
 		{
 			case ENDER_PORTAL_FRAME:
 				value = 23;
 				break;
 			case CAULDRON_ITEM:
 				value = 84;
 				break;
 			case LAVA_BUCKET:
 				value = 36.5;
 				break;
 			case MILK_BUCKET:
 				value = 36.5;
 				break;
 			case WATER_BUCKET:
 				value = 36.5;
 				break;
 			case NETHER_WARTS:
 				value = 13.2;
 				break;
 			case NETHER_STAR:
 				value = 350;
 				break;
 			case BEACON:
 				value = 385;
 				break;
 			case SADDLE:
 				value = 5.3;
 				break;
 			case EYE_OF_ENDER:
 				value = 18;
 				break;
 			case STONE:
 				value = 0.5;
 				break;
 			case COBBLESTONE:
 				value = 0.3;
 				break;
 			case LOG:
 				value = 1;
 				break;
 			case WOOD:
 				value = 0.23;
 				break;
 			case STICK:
 				value = 0.11;
 				break;
 			case GLASS:
 				value = 1.5;
 				break;
 			case LAPIS_BLOCK:
 				value = 85;
 				break;
 			case SANDSTONE:
 				value = 0.9;
 				break;
 			case GOLD_BLOCK:
 				value = 144;
 				break;
 			case IRON_BLOCK:
 				value = 110;
 				break;
 			case BRICK:
 				value = 10;
 				break;
 			case TNT:
 				value = 10;
 				break;
 			case MOSSY_COBBLESTONE:
 				value = 10;
 				break;
 			case OBSIDIAN:
 				value = 10;
 				break;
 			case DIAMOND_BLOCK:
 				value = 180;
 				break;
 			case CACTUS:
 				value = 1.7;
 				break;
 			case YELLOW_FLOWER:
 				value = 0.1;
 				break;
 			case SEEDS:
 				value = 0.3;
 				break;
 			case PUMPKIN:
 				value = 3;
 				break;
 			case CAKE:
 				value = 15;
 				break;
 			case APPLE:
 				value = 3;
 				break;
 			case CARROT:
 			case POTATO:
 				value = 1.7;
 				break;
 			case COAL:
 				value = 2.5;
 				break;
 			case DIAMOND:
 				value = 20;
 				break;
 			case IRON_ORE:
 				value = 7;
 				break;
 			case GOLD_ORE:
 				value = 13;
 				break;
 			case IRON_INGOT:
 				value = 12;
 				break;
 			case GOLD_INGOT:
 				value = 16;
 				break;
 			case GOLD_NUGGET:
 				value = 2;
 				break;
 			case STRING:
 				value = 2.4;
 				break;
 			case WHEAT:
 				value = 0.6;
 				break;
 			case BREAD:
 				value = 3;
 				break;
 			case RAW_FISH:
 			case PORK:
 				value = 2.5;
 				break;
 			case COOKED_FISH:
 			case GRILLED_PORK:
 				value = 4;
 				break;
 			case GOLDEN_APPLE:
 				value = 148;
 				break;
 			case GOLDEN_CARROT:
 				value = 17;
 				break;
 			case GOLD_RECORD:
 				value = 60;
 				break;
 			case GREEN_RECORD:
 				value = 60;
 				break;
 			case GLOWSTONE:
 				value = 1.7;
 				break;
 			case REDSTONE:
 				value = 3.3;
 				break;
 			case REDSTONE_BLOCK:
 				value = 27.7;
 				break;
 			case EGG:
 				value = 0.3;
 				break;
 			case SUGAR:
 				value = 1.2;
 				break;
 			case BONE:
 				value = 3;
 				break;
 			case ENDER_PEARL:
 				value = 1.7;
 				break;
 			case SULPHUR:
 				value = 1.2;
 				break;
 			case COCOA:
 				value = 0.6;
 				break;
 			case ROTTEN_FLESH:
 				value = 3;
 				break;
 			case RAW_CHICKEN:
 				value = 2;
 				break;
 			case COOKED_CHICKEN:
 				value = 2.6;
 				break;
 			case RAW_BEEF:
 				value = 2;
 				break;
 			case COOKED_BEEF:
 				value = 2.7;
 				break;
 			case MELON:
 				value = 0.8;
 				break;
 			case COOKIE:
 				value = 0.45;
 				break;
 			case VINE:
 				value = 1.2;
 				break;
 			case EMERALD:
 				value = 17;
 				break;
 			case EMERALD_BLOCK:
 				value = 153;
 				break;
 			case DRAGON_EGG:
 				value = 1000;
 				break;
 			default:
 				value = 0.1;
 				break;
 		}
 
 		// Return
 		return value;
 	}
 }
