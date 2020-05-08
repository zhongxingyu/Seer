 package at.grevinelveck.skylab;
 
 import java.util.logging.Logger;
 
import at.grevinelveck.slfunctions.*;
 
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SkyLab extends JavaPlugin {
 	static SkyLabCommandExecutor skyLabKill;
 
 	public SkyLab() {
 	}
 
 	public final Logger logger = Logger.getLogger("Minecraft");
 
 	@Override
 	public void onDisable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		this.logger.info(pdfFile.getName() + " is offnline");
 	}
 
 	@Override
 	public void onEnable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		this.logger.info(pdfFile.getName() + " is online");
 		skyLabKill = new SkyLabCommandExecutor(this);
 		getCommand("SkyLab").setExecutor(skyLabKill);
 	}
 
 	public static void main(String[] args) {
 
 	}
 }
