 package com.c45y.CutePVP.util;
 
 import java.util.HashSet;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Sound;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 // ----------------------------------------------------------------------------
 /**
  * Utility functions for reading the configuration file.
  */
 public class ConfigHelper {
 	/**
 	 * Constructor.
 	 * 
 	 * @param logger the logger for messages.
 	 */
 	public ConfigHelper(Logger logger) {
 		_logger = logger;
 	}
 
 	// ------------------------------------------------------------------------
 	/**
 	 * Return the child section of the specified parent at path.
 	 * 
 	 * Log a severe error if the child section is not present.
 	 * 
 	 * @param parent some configuration section.
 	 * @param path path of the child configuration section relative to parent.
 	 * @return the sub-section of the specified parent at path.
 	 */
 	public ConfigurationSection requireSection(ConfigurationSection parent, String path) {
 		ConfigurationSection section = parent.getConfigurationSection(path);
 		if (section == null) {
 			_logger.severe("Missing configuration section: " + getFullPath(parent, path));
 		}
 		return section;
 	}
 
 	// ------------------------------------------------------------------------
 	/**
 	 * Load a Location under the specified
 	 * 
 	 * @param parent some configuration section.
 	 * @param path the path of the configuration section containing the
 	 *        coordinates, relative to parent.
 	 * @return the sub-section of the specified parent at path.
 	 */
 	public Location loadLocation(ConfigurationSection parent, String path) {
 		ConfigurationSection locSection = requireSection(parent, path);
 		try {
 			double x = locSection.getDouble("x");
 			double y = locSection.getDouble("y");
 			double z = locSection.getDouble("z");
 			double pitch = locSection.getDouble("pitch");
 			double yaw = locSection.getDouble("yaw");
 			String world = locSection.getString("world", "world");
 			return new Location(Bukkit.getWorld(world), x, y, z, (float) yaw, (float) pitch);
 		} catch (Exception ex) {
 			_logger.severe("Error loading location from " + getFullPath(parent, path));
 		}
 		return null;
 	}
 
 	// ------------------------------------------------------------------------
 	/**
 	 * Save a Location exactly, with pitch and yaw angles.
 	 * 
 	 * @param section some configuration section under which location attributes
 	 *        are stored.
 	 * @param location the Location to save.
 	 */
 	public void saveLocation(ConfigurationSection section, Location location) {
		section.set("x", location.getBlockX());
		section.set("y", location.getBlockY());
		section.set("z", location.getBlockZ());
 		section.set("pitch", location.getPitch());
 		section.set("yaw", location.getYaw());
 		section.set("world", location.getWorld().getName());
 	}
 
 	// ------------------------------------------------------------------------
 	/**
 	 * Save a Location, rounded to the coordinates of the nearest Block.
 	 * 
 	 * Pitch and yaw angles are omitted.
 	 * 
 	 * @param section some configuration section under which location attributes
 	 *        are stored.
 	 * @param location the Location to save.
 	 */
 	public void saveBlockLocation(ConfigurationSection section, Location location) {
 		section.set("x", location.getBlockX());
 		section.set("y", location.getBlockY());
 		section.set("z", location.getBlockZ());
 		section.set("world", location.getWorld().getName());
 	}
 
 	// ------------------------------------------------------------------------
 	/**
 	 * Load the potion from the ConfigurationSection that is the child of parent
 	 * at the specified path.
 	 * 
 	 * The referenced section should take the form:
 	 * 
 	 * <pre>
 	 * fire_resistance:
 	 *   amplifier: 0
 	 *   duration: 2147483647
 	 *   ambient: false
 	 * </pre>
 	 * 
 	 * The section name must be a valid case-insensitive PotionEffectType value.
 	 * The example values above for amplifier, duration and ambient are the
 	 * defaults. Note that the amplifier value is one less than the conventional
 	 * potion strength number. For example, this encodes a Potion of Strength
 	 * II:
 	 * 
 	 * <pre>
 	 * increase_damage:
 	 *   amplifier: 1
 	 * </pre>
 	 * 
 	 * You can omit all attributes for a particular potion using the following
 	 * YAML notation:
 	 * 
 	 * <pre>
 	 * fire_resistance: !!map {}
 	 * </pre>
 	 * 
 	 * @param parent the parent section containing the section named after the
 	 *        PotionEffectType.
 	 * @param path the path to the section, from the parent.
 	 * @return a PotionEffect; on error, log a severe error message and return
 	 *         null.
 	 */
 	public PotionEffect loadPotion(ConfigurationSection parent, String path) {
 		ConfigurationSection potionSection = requireSection(parent, path);
 		if (potionSection == null) {
 			return null;
 		}
 
 		PotionEffectType type = PotionEffectType.getByName(potionSection.getName());
 		if (type == null) {
 			_logger.severe(potionSection.getName() + " is not a valid potion type in " + getFullPath(parent, path) + ".");
 			return null;
 		}
 
 		int duration = Math.max(0, potionSection.getInt("duration", Integer.MAX_VALUE));
 		int amplifier = Math.max(0, potionSection.getInt("amplifier", 0));
 		boolean ambient = potionSection.getBoolean("ambient", false);
 		return new PotionEffect(type, duration, amplifier, ambient);
 	} // loadPotion
 
 	// ------------------------------------------------------------------------
 	/**
 	 * Load all of the potions under the configuration section that is the child
 	 * of parent specified by path.
 	 * 
 	 * Return the non-null empty set if that section is missing or empty.
 	 * 
 	 * @param parent the parent section containing the section specified by
 	 *        path.
 	 * @param path the path relative to parent of the configuration section
 	 *        containing multiple potions in {@link #loadPotion()} format.
 	 * @param warnIfEmpty if true, log a warning if the returned set of potions
 	 *        is empty.
 	 * @return a non-null set of PotionEffect instances.
 	 */
 	public HashSet<PotionEffect> loadPotions(ConfigurationSection parent, String path, boolean warnIfEmpty) {
 		HashSet<PotionEffect> potions = new HashSet<PotionEffect>();
 		ConfigurationSection potionsSection = requireSection(parent, path);
 		if (potionsSection != null) {
 			for (String potionPath : potionsSection.getKeys(false)) {
 				PotionEffect potion = loadPotion(potionsSection, potionPath);
 				if (potion != null) {
 					potions.add(potion);
 				}
 			}
 		}
 		if (warnIfEmpty && potions.size() == 0) {
 			_logger.warning("No potions specified in " + getFullPath(parent, path) + ".");
 		}
 		return potions;
 	} // loadPotions
 
 	// ------------------------------------------------------------------------
 	/**
 	 * Load a sound whose name is specified as a string at the specified path in
 	 * the specified section.
 	 * 
 	 * @param section the section containing path.
 	 * @param path the path to the string value naming the sound, relative to
 	 *        section.
 	 * @param def the default value to use on error or if the sound is
 	 *        unspecified.
 	 * @param warnIfMissing if true, a warning is logged if the value is missing
 	 *        from the configuration.
 	 */
 	public Sound loadSound(ConfigurationSection section, String path, Sound def, boolean warnIfMissing) {
 		if (section == null) {
 			if (warnIfMissing) {
 				_logger.warning("Missing configuration section for sound at " + path + ".");
 			}
 			return def;
 		}
 
 		String name = section.getString(path);
 		if (name == null) {
 			if (warnIfMissing) {
 				_logger.warning("Missing value for sound at " + getFullPath(section, path) + ".");
 			}
 			return def;
 		}
 
 		Sound sound = Sound.valueOf(name.toUpperCase());
 		if (sound == null) {
 			_logger.severe("Invalid name \"" + name + "\" for sound at " + getFullPath(section, path) + ".");
 			return def;
 		}
 		return sound;
 	} // loadSound
 
 	// ------------------------------------------------------------------------
 	/**
 	 * Format the parent section and the path relative to it into a full path
 	 * from the root.
 	 * 
 	 * @param parent the parent section.
 	 * @param path the path within that section.
 	 * @return the full absolute path.
 	 */
 	public static String getFullPath(ConfigurationSection parent, String path) {
 		return (parent == parent.getRoot()) ? path : parent.getCurrentPath() + "." + path;
 	}
 
 	// ------------------------------------------------------------------------
 	/**
 	 * The logger.
 	 */
 	protected Logger _logger;
 } // class ConfigHelper
