 /*
  * This file is part of Plugin.
  *
  * Plugin is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Plugin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
 * along with Plugin.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /*
  * Author: Robert 'Bobby' Zenz
  * Website: http://www.bonsaimind.org
  * GitHub: https://github.com/RobertZenz/org.bonsaimind.bukkitplugins/tree/master/Plugin
  * E-Mail: bobby@bonsaimind.org
  */
 package org.bonsaimind.bukkitplugins.SaveStopper;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import org.yaml.snakeyaml.DumperOptions;
 import org.yaml.snakeyaml.Yaml;
 
 /**
  * Settings class.
  * @author Robert 'Bobby' Zenz
  */
 public class Settings {
 
 	private static final String DISABLE_ON_START = "disableOnStart";
 	private static final String SAVE_ALL = "saveAll";
 	private static final String WAIT = "wait";
 	private Map<String, Object> settings = null;
 
 	/**
 	 * Disable saving on start.
 	 * @return
 	 */
 	public boolean getDisableOnStart() {
 		return (Boolean) get(DISABLE_ON_START);
 	}
 
 	/**
 	 * Save the world when disabling.
 	 * @return
 	 */
 	public boolean getSaveAll() {
 		return (Boolean) get(SAVE_ALL);
 	}
 
 	/**
 	 * Timeout to wait until saving is diasbled (in seconds).
 	 * @return
 	 */
 	public long getWait() {
 		return (Long) get(WAIT);
 	}
 
 	/**
 	 * Initialize the settings. This will not overwrite anything,
 	 * but makes sure that the class can be safely used.
 	 */
 	public void init() {
 		if (settings == null) {
 			settings = new HashMap<String, Object>();
 		}
 
 		if (!settings.containsKey(DISABLE_ON_START)) {
 			settings.put(DISABLE_ON_START, true);
 		}
 		if (!settings.containsKey(SAVE_ALL)) {
 			settings.put(SAVE_ALL, true);
 		}
 		if (!settings.containsKey(WAIT)) {
 			settings.put(WAIT, 300L);
 		}
 	}
 
 	/**
 	 * Load the settings.
 	 * @param settingsFile Load from this file.
 	 */
 	public void load(String settingsFile) {
 		FileReader reader = null;
 		try {
 			reader = new FileReader(settingsFile);
 			settings = (Map<String, Object>) getYaml().load(reader);
 		} catch (FileNotFoundException ex) {
 			System.err.println(ex);
 			init();
 		} finally {
 			try {
 				reader.close();
 			} catch (IOException ex) {
 				System.err.println(ex);
 			}
 		}
 	}
 
 	/**
 	 * Save the settings.
 	 * @param settingsFile Save to this file.
 	 */
 	public void save(String settingsFile) {
 		// Init in case something went wrong so far.
 		init();
 
 		FileWriter writer = null;
 		try {
 			writer = new FileWriter(settingsFile);
 			getYaml().dump(settings);
 		} catch (IOException ex) {
 			System.err.println(ex);
 		} finally {
 			try {
 				writer.close();
 			} catch (IOException ex) {
 				System.err.println(ex);
 			}
 		}
 	}
 
 	private Object get(String name) {
 		if (settings == null || !settings.containsKey(name)) {
 			init();
 		}
 
 		return settings.get(name);
 	}
 
 	private Yaml getYaml() {
 		DumperOptions options = new DumperOptions();
 		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
 
 		return new Yaml(options);
 	}
 }
