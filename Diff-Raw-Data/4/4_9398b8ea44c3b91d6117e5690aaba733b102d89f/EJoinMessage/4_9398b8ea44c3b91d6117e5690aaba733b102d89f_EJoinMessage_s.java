 package net.year4000.ejoinmessage;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.sk89q.commandbook.CommandBook;
 import com.zachsthings.libcomponents.ComponentInformation;
 import com.zachsthings.libcomponents.bukkit.BukkitComponent;
 import com.zachsthings.libcomponents.config.ConfigurationBase;
 import com.zachsthings.libcomponents.config.Setting;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 @ComponentInformation(friendlyName = "eJoinMessage", desc = "Login messages that depends on last join.")
 public class EJoinMessage extends BukkitComponent implements Listener {
 	
 	private String component = "[eJoinMessage]";
 	private String version = this.getClass().getPackage().getImplementationVersion();
 	private Logger logger = Logger.getLogger(component);
 	private LocalConfiguration config;
 	
     public void enable() {
         config = configure(new LocalConfiguration());
         CommandBook.registerEvents(this);
         logger.log(Level.INFO, component + " version " + version + " has been enabled.");
     }
 
     public void reload() {
         super.reload();
         configure(config);
         logger.log(Level.INFO, component + " has been reloaded.");
     }
 
     public static class LocalConfiguration extends ConfigurationBase {
     	@Setting("first-join") public String firstJoin = "&a%player% has joined the game for the first time.";
     	@Setting("normal-join") public String normalJoin = "&a%player% has joined the game.";
     	@Setting("normal-leave") public String normalLeave = "&a%player% has left the game.";
     	@Setting("break-join") public String breakJoin = "&a%player% is back from a break.";
     	@Setting("break-time") public Long breakTime = (long) 1209600000;
     }
 
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onJoin(PlayerJoinEvent event) {
     	Player player = event.getPlayer();
     	String message = config.normalJoin;
 
     	if(player.getLastPlayed() == 0){
     		message = config.firstJoin;
     	} else if((player.getLastPlayed()+config.breakTime) < System.currentTimeMillis()){
     		message = config.breakJoin;
     	} else{
     		message = config.normalJoin;
     	}
     	event.setJoinMessage(replaceVars(message, player.getName(), player.getWorld().getName()));
     }
 
     public void onLeave(PlayerQuitEvent event) {
     	Player player = event.getPlayer();
     	String message = config.normalLeave;
     	event.setQuitMessage(replaceVars(message, player.getName(), player.getWorld().getName()));
     }
 
     private String replaceVars(String msgFormat, String playerName, String playerWorld) {
     	msgFormat = msgFormat.replace("%player%", playerName);
     	msgFormat = msgFormat.replace("%world%", playerWorld);
 
     	for (ChatColor c : ChatColor.values()) {
     		msgFormat = msgFormat.replaceAll("&" + c.getChar(), c.toString()); 
     	}
 
     	return msgFormat;
     }
 }
