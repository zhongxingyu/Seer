 package com.censoredsoftware.demigods.item;
 
 import com.censoredsoftware.demigods.Demigods;
 import com.censoredsoftware.demigods.data.DataManager;
 import com.censoredsoftware.demigods.helper.ConfigFile;
import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.inventory.ItemStack;
 
 import java.util.Map;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 
 public class DItemStack implements ConfigurationSerializable
 {
 	private UUID id;
 	private ItemStack item;
 
 	public DItemStack()
 	{}
 
 	public DItemStack(UUID id, ConfigurationSection conf)
 	{
 		this.id = id;
		if(conf.getValues(true) != null) item = ItemStack.deserialize(conf.getValues(true));
		else item = new ItemStack(Material.AIR);
 	}
 
 	@Override
 	public Map<String, Object> serialize()
 	{
 		return item.serialize();
 	}
 
 	public void generateId()
 	{
 		id = UUID.randomUUID();
 	}
 
 	public UUID getId()
 	{
 		return id;
 	}
 
 	public void setItem(ItemStack item)
 	{
		if(item == null) item = new ItemStack(Material.AIR);
 		this.item = item;
 	}
 
 	/**
 	 * Returns the DItemStack as an actual, usable ItemStack.
 	 * 
 	 * @return ItemStack
 	 */
 	public ItemStack toItemStack()
 	{
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
 			ConcurrentHashMap<UUID, DItemStack> map = new ConcurrentHashMap<UUID, DItemStack>();
 			for(String stringId : data.getKeys(false))
 				map.put(UUID.fromString(stringId), new DItemStack(UUID.fromString(stringId), data.getConfigurationSection(stringId)));
 			return map;
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
 			trackedItem.setItem(item);
 			return trackedItem;
 		}
 	}
 }
