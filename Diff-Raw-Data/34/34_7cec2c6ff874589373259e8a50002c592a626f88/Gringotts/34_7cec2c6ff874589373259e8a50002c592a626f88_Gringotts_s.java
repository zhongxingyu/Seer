 package net.mcw.gringotts;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.DyeColor;
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 public class Gringotts extends JavaPlugin {
 
	Logger log = this.getLogger();
 	Map<AccountHolder, Account> accounts = new HashMap<AccountHolder, Account>();
 	
 	public static final ItemStack currency =  
 			new ItemStack(Material.INK_SACK, 1, (short) 0, DyeColor.BLUE.getData());
 	
 	public Gringotts() {
 	}
 	
 	public void onEnable() {
 		log.info("Gringotts enabled");
 	}
 	
 	public void onDisable() {
 		log.info("Gringotts disabled");
 	}
 	
 	// TODO add optional dependency to factions. how?
 	// TODO add support to vault
 	// TODO event handlers: chest/account creation, destruction
 	// 
 	/*
 	 * TODO various items
 	 * do we need permissions?
 	 * multiworld?
 	 */
}
