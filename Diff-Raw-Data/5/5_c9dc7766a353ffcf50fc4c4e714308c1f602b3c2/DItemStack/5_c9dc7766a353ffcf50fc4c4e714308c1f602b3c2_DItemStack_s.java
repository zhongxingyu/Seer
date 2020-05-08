 package com.censoredsoftware.demigods.item;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.bukkit.Color;
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.*;
 
 import com.censoredsoftware.demigods.Demigods;
 import com.censoredsoftware.demigods.data.DataManager;
 import com.censoredsoftware.demigods.helper.ConfigFile;
 import com.google.common.collect.Maps;
 
 public class DItemStack implements ConfigurationSerializable // TODO: This whole thing could be swapped out with automated JSON serialization. I just need to learn how. Also this doesn't save firework meta and I doubt I add it. I'll switch it to JSON before going through all of that.
 {
 	private UUID id;
 	private int typeId;
 	private byte byteId;
 	private int amount;
 	private short durability;
 	private Map<String, Object> enchantments; // Format: Map<ENCHANTMENT_ID, LEVEL>
 	private Map<String, Object> storedEnchantments; // Format: Map<ENCHANTMENT_ID, LEVEL>
 	private String name;
 	private List<String> lore;
 	private String author;
 	private String title;
 	private List<String> pages;
 	private String skullOwner;
 	private int leatherColor;
 
 	public DItemStack()
 	{}
 
 	public DItemStack(UUID id, ConfigurationSection conf)
 	{
 		this.id = id;
 		typeId = conf.getInt("typeId");
 		byteId = (byte) conf.getInt("byteId");
 		amount = conf.getInt("amount");
 		durability = (short) conf.getInt("durability");
 		if(conf.getConfigurationSection("enchantments") != null) enchantments = conf.getConfigurationSection("enchantments").getValues(false);
 		if(conf.getConfigurationSection("storedEnchantments") != null) storedEnchantments = conf.getConfigurationSection("storedEnchantments").getValues(false);
 		if(conf.getString("name") != null) name = conf.getString("name");
 		if(conf.getString("lore") != null) lore = conf.getStringList("lore");
 		if(conf.getString("author") != null) author = conf.getString("author");
 		if(conf.getString("title") != null) title = conf.getString("title");
 		if(conf.getStringList("pages") != null) pages = conf.getStringList("pages");
 		if(conf.getString("skullOwner") != null) skullOwner = conf.getString("skullOwner");
 		leatherColor = conf.getInt("leatherColor");
 	}
 
 	@Override
 	public Map<String, Object> serialize()
 	{
 		return new HashMap<String, Object>()
 		{
 			{
 				// Standard
 				put("typeId", typeId);
 				put("byteId", (int) byteId);
 				put("amount", amount);
 				put("durability", (int) durability);
 				if(enchantments != null) put("enchantments", enchantments);
 				if(lore != null) put("lore", lore);
 
 				// Book
 				if(author != null) put("author", author);
 				if(title != null) put("title", title);
 				if(pages != null) put("pages", pages);
 
 				// Skull
 				if(skullOwner != null) put("skullOwner", skullOwner);
 
 				// Leather
 				if(leatherColor != 0) put("leatherColor", leatherColor);
 
 				// Enchanted book
 				if(storedEnchantments != null) put("storedEnchantments", storedEnchantments);
 			}
 		};
 	}
 
 	public void generateId()
 	{
 		id = UUID.randomUUID();
 	}
 
 	void setTypeId(int typeId)
 	{
 		this.typeId = typeId;
 	}
 
 	void setByteId(byte byteId)
 	{
 		this.byteId = byteId;
 	}
 
 	void setAmount(int amount)
 	{
 		this.amount = amount;
 	}
 
 	void setDurability(short durability)
 	{
 		this.durability = durability;
 	}
 
 	void setName(String name)
 	{
 		this.name = name;
 	}
 
 	void setLore(List<String> lore)
 	{
 		this.lore = lore;
 	}
 
 	void setEnchantments(ItemStack item)
 	{
 		// If it has enchantments then save them
 		if(item.hasItemMeta() && item.getItemMeta().hasEnchants())
 		{
 			// Create the new HashMap
 			enchantments = Maps.newHashMap();
 
 			for(Map.Entry<Enchantment, Integer> ench : item.getEnchantments().entrySet())
 			{
 				enchantments.put(String.valueOf(ench.getKey().getId()), ench.getValue());
 			}
 		}
 	}
 
 	void setBookMeta(ItemStack item)
 	{
 		// If it's a written book then save the book-specific information
 		if(item.getType().equals(Material.WRITTEN_BOOK))
 		{
 			// Define the book meta
 			BookMeta bookMeta = (BookMeta) item.getItemMeta();
 
 			// Save the book meta
 			title = bookMeta.getTitle();
 			author = bookMeta.getAuthor();
 			pages = bookMeta.getPages();
 		}
 	}
 
 	void setLeatherMeta(ItemStack item)
 	{
 		// If it's leather armor then save the color information
 		if(item.getType().equals(Material.LEATHER_HELMET) || item.getType().equals(Material.LEATHER_CHESTPLATE) || item.getType().equals(Material.LEATHER_LEGGINGS) || item.getType().equals(Material.LEATHER_BOOTS))
 		{
 			// Define the book meta
 			LeatherArmorMeta leatherMeta = (LeatherArmorMeta) item.getItemMeta();
 
 			// Save the meta
 			leatherColor = leatherMeta.getColor().asRGB();
 		}
 	}
 
 	void setSkullMeta(ItemStack item)
 	{
 		// If it's leather armor then save the color information
		if(item.getType().equals(Material.SKULL) || item.getType().equals(Material.SKULL_ITEM))
 		{
 			// Define the book meta
 			SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
 
 			// Save the meta
 			skullOwner = skullMeta.getOwner();
 		}
 	}
 
 	void setStoredEnchantments(ItemStack item)
 	{
 		// If it's leather armor then save the color information
 		if(item.getType().equals(Material.ENCHANTED_BOOK))
 		{
 			// Initialize the map
 			storedEnchantments = Maps.newHashMap();
 
 			// Define the book meta
 			EnchantmentStorageMeta enchantmentMeta = (EnchantmentStorageMeta) item.getItemMeta();
 
 			// Save the meta
 			if(enchantmentMeta.hasStoredEnchants())
 			{
 				for(Map.Entry<Enchantment, Integer> ench : enchantmentMeta.getStoredEnchants().entrySet())
 				{
 					storedEnchantments.put(String.valueOf(ench.getKey().getId()), ench.getValue());
 				}
 			}
 		}
 	}
 
 	public UUID getId()
 	{
 		return id;
 	}
 
 	/**
 	 * Returns the DItemStack as an actual, usable ItemStack.
 	 * 
 	 * @return ItemStack
 	 */
 	public ItemStack toItemStack()
 	{
 		// Create the first instance of the item
 		ItemStack item = new ItemStack(this.typeId, this.byteId);
 
 		// Set main values
 		item.setAmount(this.amount);
 		item.setDurability(this.durability);
 
 		// Define the item meta
 		ItemMeta itemMeta = item.getItemMeta();
 
 		// Set the meta
 		if(this.name != null) itemMeta.setDisplayName(this.name);
 		if(this.lore != null && !this.lore.isEmpty()) itemMeta.setLore(this.lore);
 
 		// Save the meta
 		item.setItemMeta(itemMeta);
 
 		// Apply enchantments if they exist
 		if(enchantments != null && !enchantments.isEmpty())
 		{
 			for(Map.Entry<String, Object> ench : this.enchantments.entrySet())
 			{
 				item.addUnsafeEnchantment(Enchantment.getById(Integer.parseInt(ench.getKey())), Integer.parseInt(ench.getValue().toString()));
 			}
 		}
 
 		if(Material.getMaterial(typeId).equals(Material.WRITTEN_BOOK)) // If it's a book, apply the information
 		{
 			// Get the book meta
 			BookMeta bookMeta = (BookMeta) item.getItemMeta();
 
 			bookMeta.setTitle(this.title);
 			bookMeta.setAuthor(this.author);
 			bookMeta.setPages(this.pages);
 
 			item.setItemMeta(bookMeta);
 		}
		else if(Material.getMaterial(typeId).equals(Material.SKULL)) // If it's a skull, apply the data
 		{
 			// Get the skull meta
 			SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
 
 			skullMeta.setOwner(skullOwner);
 
 			item.setItemMeta(skullMeta);
 		}
 		else if(Material.getMaterial(typeId).equals(Material.LEATHER_HELMET) || Material.getMaterial(typeId).equals(Material.LEATHER_CHESTPLATE) || Material.getMaterial(typeId).equals(Material.LEATHER_LEGGINGS) || Material.getMaterial(typeId).equals(Material.LEATHER_BOOTS)) // If it's leather, apply the color
 		{
 			// Get the skull meta
 			LeatherArmorMeta leatherMeta = (LeatherArmorMeta) item.getItemMeta();
 
 			leatherMeta.setColor(Color.fromRGB(leatherColor));
 
 			item.setItemMeta(leatherMeta);
 		}
 		else if(Material.getMaterial(typeId).equals(Material.ENCHANTED_BOOK)) // If it's an enchanted book, store the enchants
 		{
 			// Define the book meta
 			EnchantmentStorageMeta enchantmentMeta = (EnchantmentStorageMeta) item.getItemMeta();
 
 			// Save the meta
 			for(Map.Entry<String, Object> ench : this.storedEnchantments.entrySet())
 			{
 				enchantmentMeta.addStoredEnchant(Enchantment.getById(Integer.parseInt(ench.getKey())), Integer.parseInt(ench.getValue().toString()), true);
 			}
 
 			item.setItemMeta(enchantmentMeta);
 		}
 
 		// Return that sucka
 		return item;
 	}
 
 	public static class File extends ConfigFile
 	{
 		private static String SAVE_PATH;
 		private static final String SAVE_FILE = "itemStacks.yml";
 
 		public File()
 		{
 			super(Demigods.plugin);
 			SAVE_PATH = Demigods.plugin.getDataFolder() + "/data/";
 		}
 
 		@Override
 		public ConcurrentHashMap<UUID, DItemStack> loadFromFile()
 		{
 			final FileConfiguration data = getData(SAVE_PATH, SAVE_FILE);
 			return new ConcurrentHashMap<UUID, DItemStack>()
 			{
 				{
 					for(String stringId : data.getKeys(false))
 						put(UUID.fromString(stringId), new DItemStack(UUID.fromString(stringId), data.getConfigurationSection(stringId)));
 				}
 			};
 		}
 
 		@Override
 		public boolean saveToFile()
 		{
 			FileConfiguration saveFile = getData(SAVE_PATH, SAVE_FILE);
 			Map<UUID, DItemStack> currentFile = loadFromFile();
 
 			for(UUID id : DataManager.itemStacks.keySet())
 				if(!currentFile.keySet().contains(id) || !currentFile.get(id).equals(DataManager.itemStacks.get(id))) saveFile.createSection(id.toString(), Util.load(id).serialize());
 
 			for(UUID id : currentFile.keySet())
 				if(!DataManager.itemStacks.keySet().contains(id)) saveFile.set(id.toString(), null);
 
 			return saveFile(SAVE_PATH, SAVE_FILE, saveFile);
 		}
 	}
 
 	public static class Util
 	{
 		public static void save(DItemStack itemStack)
 		{
 			DataManager.itemStacks.put(itemStack.getId(), itemStack);
 		}
 
 		public static void delete(UUID id)
 		{
 			DataManager.itemStacks.remove(id);
 		}
 
 		public static DItemStack load(UUID id)
 		{
 			return DataManager.itemStacks.get(id);
 		}
 
 		public static DItemStack create(ItemStack item)
 		{
 			DItemStack trackedItem = new DItemStack();
 			trackedItem.generateId();
 			trackedItem.setTypeId(item.getTypeId());
 			trackedItem.setByteId(item.getData().getData());
 			trackedItem.setAmount(item.getAmount());
 			trackedItem.setDurability(item.getDurability());
 			if(item.hasItemMeta())
 			{
 				if(item.getItemMeta().hasDisplayName()) trackedItem.setName(item.getItemMeta().getDisplayName());
 				if(item.getItemMeta().hasLore()) trackedItem.setLore(item.getItemMeta().getLore());
 			}
 			trackedItem.setEnchantments(item);
 			trackedItem.setBookMeta(item);
 			trackedItem.setLeatherMeta(item);
 			trackedItem.setSkullMeta(item);
 			trackedItem.setStoredEnchantments(item);
 			save(trackedItem);
 			return trackedItem;
 		}
 	}
 }
