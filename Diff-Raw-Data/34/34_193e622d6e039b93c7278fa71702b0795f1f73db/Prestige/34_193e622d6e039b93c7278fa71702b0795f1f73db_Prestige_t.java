 package me.silentdojo.prestige;
 
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.permission.Permission;
 
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.RegisteredServiceProvider;
 
 
 
 public class Prestige extends JavaPlugin {
 	
 	public Permission permission = null;
 	public final Logger log = Logger.getLogger("Minecraft");
 	private PrestigeCommandExecutor myExecutor;
 	
     @Override
     public void onDisable() {
     	this.log.info(" You feel Prestigous!?");
     }
     
     @Override
     public void onEnable() {
     	PluginDescriptionFile pdf = this.getDescription();
     	RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
         permission = rsp.getProvider();
         this.log.info("[" + pdf.getName() + "]" + " Prestige Bitch");
	    
 		myExecutor = new PrestigeCommandExecutor(this, permission);
 		getCommand("prestige").setExecutor(myExecutor);
 		getCommand("test").setExecutor(myExecutor);
 		getCommand("powerup").setExecutor(myExecutor);
     }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent evt) {
    	Location loc = evt.getPlayer().getLocation();
    	World w = loc.getWorld();
    	loc.setY(loc.getY() + 5);
    	Block b = w.getBlockAt(loc);
    	b.setTypeId(1);
    }
//    public void onPlayerMove(PlayerMoveEvent evt) {
//    	Player player = evt.getPlayer();
//    	int hunger = player.getFoodLevel();
//    	if(player.getAllowFlight() == true && hunger < 10){
//    		player.setAllowFlight(false);
//    	}
    }
