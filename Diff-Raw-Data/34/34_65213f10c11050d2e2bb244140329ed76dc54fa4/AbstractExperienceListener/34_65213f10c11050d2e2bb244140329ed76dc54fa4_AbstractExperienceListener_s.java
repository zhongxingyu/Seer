 package com.comphenix.xp.listeners;
 
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 
 import com.comphenix.xp.Configuration;
 import com.comphenix.xp.Presets;
 import com.comphenix.xp.parser.ParsingException;
 
 /**
  * Represents a listener with a reference to a map of configurations (presets).
  * @author Kristian
  */
 public abstract class AbstractExperienceListener implements Listener {
 
 	protected Presets presets;
 	
 	/**
 	 * Retrieves the current presents.
 	 * @return Current presets.
 	 */
 	public Presets getPresets() {
 		return presets;
 	}
 
 	/**
 	 * Sets the current presets.
 	 * @param presets - the new presets.
 	 */
 	public void setPresets(Presets presets) {
 		this.presets = presets;
 	}
 	
 	/**
 	 * Load the correct configuration for a given player.
	 * @param world - the given player.
 	 * @return The most relevant configuration, or NULL if none were found.
 	 */
 	public Configuration getConfiguration(Player player) {
 		try {
 			return presets.getConfiguration(player);
 			
 		} catch (ParsingException e) {
 			// We most likely have complained about this already
 			return null;
 		}
 	}
 	
 	/**
 	 * Load the correct configuration for general world events not associated with any player.
 	 * @param world - the world to look for.
 	 * @return The most relevant configuration, or NULL if none were found.
 	 */
 	public Configuration getConfiguration(World world) {
 		try {
 			return presets.getConfiguration(null, world.getName());
 			
 		} catch (ParsingException e) {
 			//debugger.printDebug(this, "Preset error: %s", e.getMessage());
 			return null;
 		}
 	}
 }
