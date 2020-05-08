 package me.sololinux0.HardBan;
 
 import java.util.logging.Logger;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class HardBan extends JavaPlugin{
 	
 	Logger log;
 	
 	public void onEnable(){
 		log = Logger.getLogger("Minecraft");
 		log.info("HardBan is enabled!");
 	}
 
 	public void onDisable() {
		log - Logger.getLogger("Minecraft")
		log.info("HardBan disabed")
 	}
 }
