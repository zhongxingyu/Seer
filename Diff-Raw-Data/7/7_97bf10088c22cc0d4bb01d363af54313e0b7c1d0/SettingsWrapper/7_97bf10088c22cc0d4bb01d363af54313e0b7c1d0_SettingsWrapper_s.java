 // Copyright © 2011 Martin Ueding <dev@martin-ueding.de>
 
 /*
  * This file is part of jscribble.
  *
  * jscribble is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 2 of the License, or (at your option)
  * any later version.
  *
  * jscribble is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * jscribble.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package jscribble.helpers;
 
 import java.awt.Color;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Properties;
 
 import jscribble.NoteBookProgram;
 
 /**
  * Serves as an interface to the config file. Provides several convenience
  * functions for various types in lookup.
  *
  * @author Martin Ueding <dev@martin-ueding.de>
  */
 public class SettingsWrapper {
 	/**
 	 * Main config used for user specific settings.
 	 */
 	private static Properties userConfig;
 
 	/**
 	 * Default config that is used if the user did not specify this in his
 	 * config file.
 	 */
 	private static Properties defaultConfig;
 
 	/**
 	 * Looks up a boolean value in the config file.
 	 *
 	 * @param key Key to look up.
 	 * @return Config value or default as boolean.
 	 */
 	public static boolean getBoolean(String key) {
 		return Boolean.parseBoolean(retrieve(key));
 	}
 
 	/**
 	 * Gets the default config, initializes if needed.
 	 *
 	 * @return Initialized config.
 	 */
 	private static Properties getDefaultConfig() {
 		if (defaultConfig == null) {
 			initDefaultConfig();
 		}
 
 		return defaultConfig;
 	}
 
 	/**
 	 * Looks up a integer value in the config file.
 	 *
 	 * @param key Key to look up.
 	 * @return Config value or default as integer.
 	 */
 	public static int getInteger(String key) {
 		return Integer.parseInt(retrieve(key));
 	}
 
 	/**
 	 * Looks up a hexadecimal integer value from the config and converts it
 	 * into a Color.
 	 *
 	 * @param key Key to look up.
 	 * @return Config value as Color.
 	 */
 	public static Color getColor(String key) {
 		int color = (int) Long.parseLong(retrieve(key), 16);
 		return new Color(color, isColorWithAlpha(color));
 	}
 
 	/**
 	 * Determines whether a given color has an alpha part.
 	 *
 	 * @param color The color to check for alpha part.
 	 * @return Whether there is an alpha part.
 	 */
 	private static boolean isColorWithAlpha(int color) {
 		return color > 0xFFFFFF || color < 0;
 	}
 
 	/**
 	 * Looks up a string value in the config file.
 	 *
 	 * @param key Key to look up.
 	 * @return Config value or default as string.
 	 */
 	public static String getString(String key) {
 		return retrieve(key);
 	}
 
 	/**
 	 * Gets the config, initializes if needed.
 	 *
 	 * @return Initialized config.
 	 */
 	private static Properties getUserConfig() {
 		if (userConfig == null) {
 			initUserConfig();
 		}
 
 		return userConfig;
 	}
 
 	/**
 	 * Loads the default config from the bundled file.
 	 */
 	private static void initDefaultConfig() {
 		defaultConfig = new Properties();
 		try {
 			defaultConfig.load(ClassLoader.getSystemResourceAsStream("jscribble/default_config.properties"));
 		}
 		catch (FileNotFoundException e) {
 			Logger.handleError(Localizer.get("Could not find the config file. (This should *not* happen!)"));
 			e.printStackTrace();
 		}
 		catch (IOException e) {
 			Logger.handleError(Localizer.get(
 			            "IO error while reading config file."));
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Loads the user config if there is one.
 	 */
 	private static void initUserConfig() {
 		userConfig = new Properties();
 		try {
 			File configfile = new File(NoteBookProgram.getDotDir() + File.separator +
 			        getString("user_config_filename"));
 			if (configfile.exists()) {
 				userConfig.load(new FileInputStream(configfile));
 			}
 		}
 		catch (FileNotFoundException e) {
 			Logger.handleError(Localizer.get("Could not find the config file. (This should *not* happen!)"));
 			e.printStackTrace();
 		}
 		catch (IOException e) {
 			Logger.handleError(Localizer.get(
 			            "IO error while reading config file."));
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Retrieves a string from either the user or default config.
 	 *
 	 * @param key Parameter name to look up.
 	 * @return Value.
 	 */
 	private static String retrieve(String key) {
 		if (getUserConfig().containsKey(key)) {
 			return getUserConfig().getProperty(key);
 		}
 		else if (getDefaultConfig().containsKey(key)) {
 			return getDefaultConfig().getProperty(key);
 		}
 		else {
 			Logger.handleError(String.format(
 			            Localizer.get("There is no default setting for %s."),
 			            key
 			        ));
 			return "";
 		}
 	}
 
 	/**
 	 * Wrapper to retrieve a double.
 	 *
 	 * @param key Parameter name to look up.
 	 * @return Value.
 	 */
 	public static double getDouble(String key) {
 		return Double.parseDouble(retrieve(key));
 	}
 
 	/**
 	 * Checks whether a pressed key should trigger a command.
 	 *
 	 * @param event Pressed key.
 	 * @param command Command in question.
 	 * @return Whether this should trigger.
 	 */
 	public static boolean isKeyForCommand(KeyEvent event, String command) {
 		String raw_array = retrieve(command);
 		String[] parts = raw_array.split(",");
 		for (String part : parts) {
 			if (part.length() == 1) {
 				if (event.getKeyChar() == part.charAt(0)) {
 					Logger.log(SettingsWrapper.class.getName(), String.format(Localizer.get("%c is a valid key for %s."), event.getKeyChar(), command));
 					return true;
 				}
 			}
 			else if (event.getKeyCode() == Integer.parseInt(part)) {
 				Logger.log(SettingsWrapper.class.getName(), String.format(Localizer.get("%s is a valid key for %s."), KeyEvent.getKeyText(event.getKeyChar()), command));
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Checks whether a pressed mouse button should trigger a command.
 	 *
 	 * @param event Pressed mouse button.
 	 * @param command Command in question.
 	 * @return Whether this should trigger.
 	 */
 	public static boolean isButtonForCommand(MouseEvent event, String command) {
 		String raw_array = retrieve(command);
 		String[] parts = raw_array.split(",");
 		for (String part : parts) {
 			if (event.getModifiersEx() == Integer.parseInt(part)) {
 				Logger.log(SettingsWrapper.class.getName(), String.format(Localizer.get("%s is a valid key for %s."), MouseEvent.getModifiersExText(event.getModifiersEx()), command));
 				return true;
 			}
 		}
 		return false;
 	}
 }
