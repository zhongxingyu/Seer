 package com.comphenix.xp;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import net.milkbowl.vault.chat.Chat;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 
 import com.comphenix.xp.lookup.PresetQuery;
 import com.comphenix.xp.lookup.PresetTree;
 import com.comphenix.xp.parser.ParsingException;
 import com.comphenix.xp.parser.primitives.StringParser;
 import com.comphenix.xp.parser.text.ParameterParser;
 import com.comphenix.xp.parser.text.PresetParser;
 import com.google.common.collect.Lists;
 
 /**
  * Contains every loaded configuration preset.
  */
 public class Presets {
 
 	public static final String optionPreset = "experiencePreset";
 	
 	private static final String settingImportFile = "file";
 	private static final String settingLocal = "local";
 	
 	// Mapping of preset name and configuration
 	private PresetTree presets;
 	
 	// Parser
 	private PresetParser presetParser = new PresetParser();
 	private ParameterParser<String> stringParser = new ParameterParser<String>(new StringParser());
 	
 	private Debugger logger;
 	
 	// Chat
 	private Chat chat;
 	
 	public Presets(ConfigurationSection config, Debugger logger, Chat chat, ConfigurationLoader loader) {
 		
 		this.presets = new PresetTree();
 		this.logger = logger;
 		this.chat = chat;
 		
 		if (config != null)
 			loadPresets(config, loader);
 	}
 	
 	/**
 	 * Retrieves a stored configuration from a key value. Note that while NULL in rules will match 
 	 * any query, a query with null will NOT match any rule. In that case, it will only match rules with
 	 * null in the corresponding parameters.
 	 * 
	 * @param keyName - key value(s) of the configuration to retrieve.
 	 * @param worldName - name of the world the preset is associated with. 
 	 * @return The stored configuration, or NULL if no configuration exists.
 	 * @throws ParsingException If the given list of keys is malformed.
 	 */
 	public Configuration getConfiguration(String presetNames, String worldName) throws ParsingException {
 		
 		List<String> names = stringParser.parseExact(presetNames);
 		
 		PresetQuery query = PresetQuery.fromExact(names, worldName);
 		Configuration result = presets.get(query);
 	
 		// Determine what to return
 		if (result != null)
 			return result;
 		
 		// Error
 		return null;
 	}
 	
 	public Configuration getConfiguration(CommandSender sender) throws ParsingException {
 		
 		String preset = null;
 		String world = null;
 		
 		if (chat != null && sender instanceof Player) {
 			Player player = (Player) sender;
 			preset = chat.getPlayerInfoString(player, optionPreset, null);
 			world = player.getWorld().getName();
 		}
 		
 		return getConfiguration(preset, world);
 	}
 	
 	public boolean usesPresetParameters() {
 		return presets.usesPresetNames();
 	}
 	
 	private void loadPresets(ConfigurationSection section, ConfigurationLoader loader) {
 		for (String key : section.getKeys(false)) { 
 			
 			// Load query
 			try {
 				PresetQuery query = presetParser.parse(key);
 			
 				// Load section
 				Configuration data = loadPreset(
 						section.getConfigurationSection(key), loader);
 				
 				// Remember if we have presets or not
 				data.setPreset(query.hasPresetNames());
 				
 				if (data != null)
 					presets.put(query, data);
 			
 			} catch (ParsingException ex) {
 				if (logger != null)
 					logger.printWarning(this, "Cannot parse preset - %s", ex.getMessage());
 			}
 		}
 	}
 	
 	private Configuration loadPreset(ConfigurationSection data, ConfigurationLoader loader) {
 		
 		List<Configuration> files = getConfigurations(data, loader);
 		Configuration local = getLocal(data, loader);
 		Configuration result = null;
 		
 		// Local configuration has the highest priority
 		if (local != null) {
 			files.add(local);
 		}
 		
 		// Make sure there is anything to return
 		if (files.isEmpty())
 			result = new Configuration(logger);
 		else
 			result = Configuration.fromMultiple(files, logger);
 		
 		return result;
 	}
 	
 	public boolean containsPreset(String preset, String world) throws ParsingException {
 		Configuration result = getConfiguration(preset, world);
 		
 		// Whether or not the matching rule has specified presets
 		return result.hasPreset();
 	}
 	
 	private Configuration getLocal(ConfigurationSection data, ConfigurationLoader loader) {
 		
 		// Retrieve using the configuration section
 		if (data.isConfigurationSection(settingLocal)) {
 			return loader.getFromSection(data.getConfigurationSection(settingLocal));
 		} else {
 			return null;
 		}
 	}
 	
 	private List<Configuration> getConfigurations(ConfigurationSection data, ConfigurationLoader loader) {
 		
 		List<Configuration> result = new ArrayList<Configuration>();
 		
 		for (String path : getFiles(data)) {
 		
 			// Load from folder
 			Configuration config = loader.getFromPath(path);
 			
 			if (config != null)
 				result.add(config);
 			else if (logger != null)
 				logger.printWarning(this, "Cannot find configuration file %s.", path);
 		}
 		
 		return result;
 	}
 	
 	private List<String> getFiles(ConfigurationSection data) {
 		
 		if (data.isString(settingImportFile))
 			return Lists.newArrayList(data.getString(settingImportFile));
 		else if (data.isList(settingImportFile))
 			return data.getStringList(settingImportFile);
 		else
 			return Lists.newArrayList();
 	}
 
 	public Collection<Configuration> getConfigurations() {
 		return presets.getValues();
 	}
 	
 	/**
 	 * Removes a given player from being referenced by any preset node. Must be called when a player logs out.
 	 * @param player - player to remove.
 	 */
 	public void removePlayer(Player player) {
 		// Make sure messages are being sent
 		if (presets != null) {
 			for (Configuration config : presets.getValues()) {
 				config.removePlayer(player);
 			}	
 		}
 	}
 	
 	public void onTick() {
 		
 		// Make sure messages are being sent
 		if (presets != null) {
 			for (Configuration config : presets.getValues()) {
 				config.onTick();
 			}		
 		}
 	}
 }
