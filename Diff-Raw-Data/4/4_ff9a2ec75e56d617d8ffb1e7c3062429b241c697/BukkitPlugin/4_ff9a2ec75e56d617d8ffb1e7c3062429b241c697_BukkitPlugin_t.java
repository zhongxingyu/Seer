 package denizen.me.engraver;
 
 import net.aufdemrand.denizen.utilities.debugging.dB;
 
 import org.bukkit.event.entity.ItemDespawnEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class BukkitPlugin extends JavaPlugin {
 
 	public static EngraverNBT engraverNBT;
 	public EngraverEnforcer enforcer;
 	public EngraverReplaceables replacer;
 	
	// Run this on Bukkit's enable sequence.. the perfect place to register commands, events, etc.
 	@Override
 	public void onEnable() {
 
 		// Load EngraverNBT methods statically.
 		engraverNBT = new EngraverNBT();
 
 		// Register ENGRAVE command with Denizen
 		new EngraveCommand().activate().as("ENGRAVE").withOptions("(SET|REMOVE) (TARGET:player_name)", 0);
 
 		// Register new enforcer/replacer and add to bukkit listener registry
 		enforcer = new EngraverEnforcer(this);
 		getServer().getPluginManager().registerEvents(enforcer, this);
 		replacer = new EngraverReplaceables(this);
 		getServer().getPluginManager().registerEvents(replacer, this);
 		
 		// Alert the log.
 		dB.log("Loaded ENGRAVER Add-on for Denizen/C2!");
 	}
 
	// Part of Bukkit's disable sequence.
 	@Override
 	public void onDisable() {
 		// unregister events in case of a /reload
 		PlayerPickupItemEvent.getHandlerList().unregister(this);
 		ItemDespawnEvent.getHandlerList().unregister(this);
 	}
 	
 }
