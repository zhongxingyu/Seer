 package fr.frozentux.craftguard2.list;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import fr.frozentux.craftguard2.CraftGuardPlugin;
 
 public class ListLoader {
 	
 	private CraftGuardPlugin plugin;
 	
 	private File configurationFile;
 	private FileConfiguration configuration;
 	
 	public ListLoader(CraftGuardPlugin plugin, FileConfiguration fileConfiguration, File file){
 		this.plugin = plugin;
 		this.configurationFile = file;
 		this.configuration = fileConfiguration;
 	}
 	
 	/**
 	 * Loads every list from the file given to the object.
 	 * @return	A Map of the loaded Lists
 	 */
 	public HashMap<String, List> load(){
 		HashMap<String, List> lists = new HashMap<String, List>();
 		
 		if(!configurationFile.exists()){
 			configuration.set("example.permission", "permission");
 			ArrayList<String> ids = new ArrayList<String>();
 			ids.add("24");
 			ids.add("32:2");
 			configuration.set("example.ids", ids);
 			configuration.options().header("CraftGuard 2.X by FrozenTux\nhttp://dev.bukkit.org/server-mods/craftguard").copyHeader(true);
 			
 			try {
 				configuration.save(configurationFile);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		try {
 			configuration = new YamlConfiguration();
 			configuration.load(configurationFile);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
		plugin.getCraftGuardLogger().debug("Start loading; Lists : " + configuration.getKeys(false).size());
 		Iterator<String> it = configuration.getKeys(false).iterator();
 		
 		int ignoredCount = 0;
 		
 		while(it.hasNext()){	//This loop will be run for each list
			plugin.getCraftGuardLogger().debug("===LISTE===");
 			String name = it.next();
 			String permission = configuration.getString(name + ".permission");
 			String parentName = configuration.getString(name + ".parent");
			java.util.List<String> ids = configuration.getStringList(name + ".ids");
			plugin.getCraftGuardLogger().debug(name + ";" + permission + ";" + parentName);
 			if(ids != null){
				plugin.getCraftGuardLogger().debug(String.valueOf(ids.size()));
 				lists.put(name, new List(name, permission, parentName, ids, plugin.getListManager()));
 				//Type lists loading
 				Set<String> customLists = configuration.getConfigurationSection(name).getKeys(false);
 				customLists.remove("ids");
 				Iterator<String> customListsIt = customLists.iterator();
 				while(customListsIt.hasNext()){
 					String element = customListsIt.next();
 					if(configuration.isList(name + "." + element)) lists.get(name).addTypeList(element, configuration.getStringList(name + "." + element));
 				}
 			}
 			else {
 				plugin.getCraftGuardLogger().warning("List " + name + " has no ids defined ! Ignoring it");
 				ignoredCount++;
 			}
 		}
 		
 		if(ignoredCount == 0) plugin.getCraftGuardLogger().info("Succesfully loaded " + lists.size() + " lists");
 		else plugin.getCraftGuardLogger().info("Succesfully loaded " + (lists.size() - ignoredCount) + " lists out of " + lists.size() + " lists (" + ignoredCount + " ignored)");
 		
 		return lists;
 	}
 	
 	public void writeList(List list, boolean save){
 		String path = list.getName() + ".";
 		if(!list.getPermission().equals(list.getName()))configuration.set(path + "permission", list.getPermission());
 		if(list.getParent() != null)configuration.set(path + "parent", list.getPermission());
 		configuration.set(path + "ids", list.idsToStringSet());
 		
 		if(list.hasTypeLists()){
 			Iterator<String> it = list.typeListsNames().iterator();
 			while(it.hasNext()){
 				String type = it.next();
 				Iterator<Id> idIterator = list.getTypeList(type, false).values().iterator();
 				ArrayList<String> rawList = new ArrayList<String>();
 				
 				while(idIterator.hasNext()) rawList.add(idIterator.next().toString());
 				
 				configuration.set(path + type, rawList);
 			}
 		}
 		
 		if(save)
 			try {
 				configuration.save(configurationFile);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 	}
 	
 	public void writeAllLists(){
 		Iterator<String> it = plugin.getListManager().getListsNames().iterator();
 		while(it.hasNext()){
 			this.writeList(plugin.getListManager().getList(it.next()), false);
 		}
 		try {
 			configuration.save(configurationFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 }
