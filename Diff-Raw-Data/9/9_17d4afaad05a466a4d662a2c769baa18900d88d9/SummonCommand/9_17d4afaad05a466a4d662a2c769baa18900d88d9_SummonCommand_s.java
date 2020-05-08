 package org.nationsatwar.kitty.Commands;
 
 import java.io.File;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 
 import org.nationsatwar.kitty.Kitty;
 import org.nationsatwar.kitty.Sumo.SumoObject;
 import org.nationsatwar.kitty.Utility.AIUtility;
 import org.nationsatwar.kitty.Utility.ConfigHandler;
 
 public final class SummonCommand {
 
 	protected final Kitty plugin;
 	
 	public SummonCommand(Kitty plugin) {
 		
 		this.plugin = plugin;
 	}
 
 	/**
 	 * Handles the 'Summon' command.
 	 * <p>
 	 * Creates a new Sumo as specified from the (sumoName).yml FileConfiguration
 	 * 
 	 * @param player  Person sending the command
 	 * @param sumoName  The name of the Sumo to spawn
 	 */
 	public final void execute(Player player, String sumoName) {
 		
 		sumoName = sumoName.toLowerCase();
 		
 	    File sumoFile = new File(ConfigHandler.getFullSumoPath(sumoName));
 	    
 	    // Returns if the sumoFile does not exist
 	    if (!sumoFile.exists()) {
 	    	
 	    	player.sendMessage(ChatColor.YELLOW + "Sumo: " + sumoName + " does not exist. Please try again.");
 	    	return;
 	    }
 	    
 	    FileConfiguration sumoConfig = YamlConfiguration.loadConfiguration(sumoFile);
 	    
 	    EntityType entityType = EntityType.fromName(sumoConfig.getString(ConfigHandler.sumoEntityType));
 		
 	    // Returns if the Entity Type as specified in the config file is invalid
 		if (entityType == null) {
 	    	
 	    	player.sendMessage(ChatColor.YELLOW + "Entity Type for: " + sumoName + " does not exist.");
 	    	return;
 		}
 		
 		Location spawnLocation = AIUtility.randomizeLocation(player.getLocation(), 5);
 		
 		Entity entity = player.getWorld().spawnEntity(spawnLocation, entityType);
 		
 		SumoObject sumo = new SumoObject(plugin, entity, player, sumoName);
 		plugin.sumoManager.addSumo(player.getName(), sumo);
 	}
 }
