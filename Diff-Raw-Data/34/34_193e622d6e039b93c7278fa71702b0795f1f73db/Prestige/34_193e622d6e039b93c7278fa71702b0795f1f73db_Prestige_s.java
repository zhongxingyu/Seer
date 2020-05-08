 package me.silentdojo.prestige;
 
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.permission.Permission;
 
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
    
}
