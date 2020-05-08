 package edu.unca.szhang.GameSpout;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 
 import org.bukkit.entity.Player;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.metadata.MetadataValue;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.inventory.SpoutItemStack;
 import org.getspout.spoutapi.inventory.SpoutShapedRecipe;
 import org.getspout.spoutapi.material.MaterialData;
 
 /*
  * This is the main class of the sample plug-in
  */
 public class GameSpout extends JavaPlugin {
 	public GameSpoutCommandExecutor executor;
 	
 	public Map <Player, Boolean> musicplayed;		
 
 	/*
 	 * This is called when your plug-in is enabled
 	 */
 	@Override
 	public void onEnable() {
        musicplayed = new HashMap<Player, Boolean>();		
 
 		getLogger().log(Level.INFO, "[Spout Item Test Plugin] Enabled!");
 
 		// save the configuration file
 		saveDefaultConfig();
 
 		// Create the SampleListener
 		new GameSpoutListener(this);
 
 		// set the command executor for sample
 		executor = new GameSpoutCommandExecutor(this);
 		this.getCommand("game").setExecutor(executor);
 	}
 
 	/*
 	 * This is called when your plug-in shuts down
 	 */
 	@Override
 	public void onDisable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		getLogger().log(Level.INFO, "[Spout Item Test Plugin] Disabled");
 	}
 
 	public void setMetadata(Player player, String key, Object value, GameSpout plugin) {
 		player.setMetadata(key, new FixedMetadataValue(plugin, value));
 	}
 		
 	public Object getMetadata(Player player, String key, GameSpout plugin) {
 		List<MetadataValue> values = player.getMetadata(key);
 		for (MetadataValue value : values) {
 			if (value.getOwningPlugin().getDescription().getName()
 			.equals(plugin.getDescription().getName())) {
 			return (value.asBoolean());
 			}
 		}
 		return null;
 	}
 
 }
