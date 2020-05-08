 package spia1001.InvFall;
 
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /*
 InvFall Plugin
  
 @author Chris Lloyd (SPIA1001)
 */
 
 public class InvFall extends JavaPlugin {
 	private PlayerManager playerManager = new PlayerManager();
 	private InvFallPlayerListener playerListener = new InvFallPlayerListener(playerManager);
 	private InvFallBlockListener blockListener = new InvFallBlockListener(playerManager);
 	
 	public void onDisable()
 	{
 		playerManager.save();
 	}
 
 	public void onEnable() 
 	{
 		registerEvents();
		
 		getCommand("invfall").setExecutor(new InvFallPluginCommand(playerManager));
 		
 		PluginDescriptionFile pdfFile = this.getDescription();
	    System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
 	}
 	private void registerEvents()
 	{
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Monitor, this);
 		pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, playerListener, Priority.Monitor, this);
 		pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Monitor, this);
 	}
 }
