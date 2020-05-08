 /* This Source Code Form is subject to the terms of the Mozilla Public
  * License, v. 2.0. If a copy of the MPL was not distributed with this
  * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
 
 package org.shininet.bukkit.languagepack;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import net.minecraft.server.NBTTagCompound;
 import net.minecraft.server.NBTTagList;
 import net.minecraft.server.NBTTagString;
 
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.shininet.bukkit.languagepack.listeners.LanguagePackPacket;
 import org.shininet.bukkit.languagepack.listeners.LanguagePackPlayerJoin;
 
 import com.comphenix.protocol.ProtocolLibrary;
 import com.comphenix.protocol.ProtocolManager;
 
 public class LanguagePack extends JavaPlugin {
 	public Logger logger;
 	public FileConfiguration configFile;
 	private static boolean updateReady = false;
 	private static String updateName = "";
 	private static long updateSize = 0;
 	private static final String updateSlug = "languagepack";
 	private LanguagePackCommandExecutor commandExecutor;
 	private CommandExecutor oldCommandExecutor;
 	private LanguagePackPlayerJoin listenerPlayerJoin;
 	private LanguagePackPacket listenerPacket;
 	private ProtocolManager protocolManager;
 	public static enum configType {DOUBLE, BOOLEAN};
 	@SuppressWarnings("serial")
 	public static final Map<String, configType> configKeys = new HashMap<String, configType>(){
 		{
 			put("autoupdate", configType.BOOLEAN);
 		}
 	};
 	public static final String configKeysString = implode(configKeys.keySet(), ", ");
 	
 	@Override
 	public void onEnable(){
 		logger = getLogger();
 		configFile = getConfig();
 		configFile.options().copyDefaults(true);
 		saveConfig();
 		this.saveResource("config.example.yml", false);
 		try {
 		    Metrics metrics = new Metrics(this);
 		    metrics.start();
 		} catch (Exception e) {
 			logger.warning("Failed to start Metrics");
 		}
 		try {
 			if (configFile.getBoolean("autoupdate") && !(updateReady)) {
 				Updater updater = new Updater(this, updateSlug, this.getFile(), Updater.UpdateType.NO_DOWNLOAD, false); // Start Updater but just do a version check
 				updateReady = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE; // Determine if there is an update ready for us
 				updateName = updater.getLatestVersionString(); // Get the latest version
 				updateSize = updater.getFileSize(); // Get latest size
 			}
 		} catch (Exception e) {
 			logger.warning("Failed to start Updater");
 		}
 		
 		protocolManager = ProtocolLibrary.getProtocolManager();
 		listenerPacket = new LanguagePackPacket(this, protocolManager, logger);
 		
 		listenerPlayerJoin = new LanguagePackPlayerJoin(this);
 		getServer().getPluginManager().registerEvents(listenerPlayerJoin, this);
 		
 		oldCommandExecutor = getCommand("LanguagePack").getExecutor();
 		commandExecutor = new LanguagePackCommandExecutor(this);
 		getCommand("LanguagePack").setExecutor(commandExecutor);
 	}
 
 	@Override
 	public void onDisable() {
 		listenerPacket.unregister();
 		listenerPlayerJoin.unregister();
		if (oldCommandExecutor != null) {
			getCommand("LanguagePack").setExecutor(oldCommandExecutor);
		}
 	}
 
 	public boolean getUpdateReady() {
 		return updateReady;
 	}
 
 	public String getUpdateName() {
 		return updateName;
 	}
 
 	public long getUpdateSize() {
 		return updateSize;
 	}
 
 	public void update() {
 		new Updater(this, updateSlug, getFile(), Updater.UpdateType.NO_VERSION_CHECK, true);
 	}
 	
 	public static String implode(Set<String> input, String glue) {
 		int i = 0;
 		StringBuilder output = new StringBuilder();
 		for (String key : input) {
 			if (i++ != 0) output.append(glue);
 			output.append(key);
 		}
 		return output.toString();
 	}
 
 	private NBTTagString packName(int id, int damage) {
 		String output;
 		if (((output = configFile.getString("pack."+id+".all.name")) == null) &&
 				((output = configFile.getString("pack."+id+"."+damage+".name")) == null) &&
 				((output = configFile.getString("pack."+id+".other.name")) == null)) {
 			return null;
 		}
 		return new NBTTagString(null,"r"+output);
 	}
 	
 	private NBTTagList packLore(int id, int damage) {
 		List<String> output;
 		if (((output = configFile.getStringList("pack."+id+".all.lore")) == null) &&
 				((output = configFile.getStringList("pack."+id+"."+damage+".lore")) == null) &&
 				((output = configFile.getStringList("pack."+id+".other.lore")) == null)) {
 			return null;
 		}
 		NBTTagList tagList = new NBTTagList();
 		for (String line : output) {
 			tagList.add(new NBTTagString(null,line));
 		}
 		return tagList;
 	}
 
	public void process(net.minecraft.server.ItemStack input) {
		//CraftItemStack cis = ((CraftItemStack)input);
		//net.minecraft.server.ItemStack is = cis.getHandle();
		//net.minecraft.server.ItemStack itemStack = CraftItemStack.createNMSItemStack(input);
		NBTTagCompound tag = input.tag;
		int id = input.id;
		int damage = input.getData();
 		NBTTagCompound tagDisplay;
 		NBTTagString name = packName(id, damage);
 		NBTTagList lore = packLore(id, damage);
 		
 		if ((name == null) && (lore == null)) {
 			return;
 		}
 		if (tag == null) {
 			tag = new NBTTagCompound();
 		}
 		if (tag.hasKey("display")) {
 			tagDisplay = tag.getCompound("display");
 		} else {
 			tagDisplay = new NBTTagCompound();
 			tag.set("display", tagDisplay);
 		}
 		if ((!tagDisplay.hasKey("Name")) && (name != null)) {
 			tagDisplay.set("Name", name);
 		}
 		if ((!tagDisplay.hasKey("Lore")) && (lore != null)) {
 			tagDisplay.set("Lore", lore);
 		}
 	}
 	
	public void process(net.minecraft.server.ItemStack[] input) {
		for (net.minecraft.server.ItemStack stack : input) {
 			process(stack);
 		}
 	}
 }
