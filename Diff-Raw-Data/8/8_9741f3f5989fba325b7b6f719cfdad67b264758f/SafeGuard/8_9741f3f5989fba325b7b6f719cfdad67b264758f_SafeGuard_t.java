 package com.xdrapor.safeguard;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.xdrapor.safeguard.core.ICore;
 import com.xdrapor.safeguard.core.command.SGCommandManager;
 import com.xdrapor.safeguard.core.configuration.SGConfig;
 import com.xdrapor.safeguard.core.logging.SGLogManager;
 import com.xdrapor.safeguard.core.permissions.SGPermissibles;
 import com.xdrapor.safeguard.event.SGEventManager;
 import com.xdrapor.safeguard.player.SGPlayerManager;
 
 public class SafeGuard extends JavaPlugin implements ICore 
 {
 	
 	/** The instance of SGLogManager. */
 	public SGLogManager sgLogManager;
 
 	/** The instance of SGPermissions. */
 	public SGPermissibles sgPermissions;
 
 	/** The instance of SGConfig. */
 	public SGConfig sgConfig;
 
 	/** The instance of SGPlayerManager. */
 	public SGPlayerManager sgPlayerManager;
 
 	/** The instance of SGEventManager. */
 	public SGEventManager sgEventManager;
 	
 	/** The instance of SGCommandManager. */
 	public SGCommandManager sgCommandManager;
 	/**
 	 * Executed when SafeGuard is enabled.
 	 * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
 	 */
 	@Override
	public void onEnable()  {
 
 		sgLogManager		= new SGLogManager();
 		sgPermissions		= new SGPermissibles();
 		sgConfig			= new SGConfig();
 		sgPlayerManager		= new SGPlayerManager();
 		sgEventManager		= new SGEventManager();
 		
		//Load all online players.
		sgPlayerManager.loadOnlinePlayers();
		
		//Gets the SafeGuard command.
 		getCommand("safeguard").setExecutor(sgCommandManager = new SGCommandManager());
 		
 		// Log completion to console.
 		sgLogManager.getConsoleLogger().logInfo(sgPrefix + sgStringSeparator + "has been enabled.");
 	}
 
 	/**
 	 * Executed when SafeGuard is disabled.
 	 * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
 	 */
 	@Override
 	public void onDisable()
 	{
 		sgEventManager.cleanUpListeners();
 		sgLogManager.fileHandler.close();
 		sgLogManager.getConsoleLogger().logInfo(sgPrefix + sgStringSeparator + "has been disabled!");
 	}
 	
 	/** Returns the version of SafeGuard. */
 	public String getVersion() {	
 
 		InputStream stream = getClass().getResourceAsStream("/version.prop");
 		
 		if (stream == null) return "UNKNOWN";
 		
 		Properties props = new Properties();
 		
 		try {
 			
 			props.load(stream);
 			stream.close();
 			
 			return (String)props.get("version");
 			
 		} catch (IOException e) {
 			return "UNKNOWN";
 		}
 	}
 }
