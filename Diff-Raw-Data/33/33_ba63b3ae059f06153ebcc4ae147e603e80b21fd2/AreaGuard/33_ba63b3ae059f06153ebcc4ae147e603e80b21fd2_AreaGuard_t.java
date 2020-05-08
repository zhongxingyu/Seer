 package com.zand.bukkit.areaguard;
 
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Cancellable;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 
 import com.zand.areaguard.*;
 import com.zand.areaguard.area.Area;
 import com.zand.areaguard.area.Cuboid;
 import com.zand.areaguard.area.error.ErrorArea;
import com.zand.bukkit.areaguard.command.AdminCommands;
 import com.zand.bukkit.areaguard.command.AreaCommands;
 import com.zand.bukkit.areaguard.command.CuboidCommands;
import com.zand.bukkit.areaguard.command.DebugCommands;
 import com.zand.bukkit.areaguard.command.MainCommands;
 import com.zand.bukkit.areaguard.listeners.AreaGuardBlockListener;
 import com.zand.bukkit.areaguard.listeners.AreaGuardEntityListener;
 import com.zand.bukkit.areaguard.listeners.AreaGuardPlayerListener;
 import com.zand.bukkit.areaguard.listeners.AreaGuardWorldListener;
 import com.zand.bukkit.util.Messager;
 import com.zand.bukkit.util.PermissionsWrapper;
 import com.zand.bukkit.util.TempBlocks;
 
 /**
  * AreaGuard for Bukkit
  *
  * @author zand
  */
 public class AreaGuard extends JavaPlugin {
 	static final private HashMap<CommandSender, Session> sessions = new HashMap<CommandSender, Session>();
 	
 	/**
 	* Gets the senders current session.
 	* @param sender	The sender to get the session for
 	* @return 		The current session for that sender
 	*/
 	public Session getSession(CommandSender sender) {
 		if (!sessions.containsKey(sender)) {
 			Session session;
 			if (sender instanceof Player) {
 				session = new PlayerSession((Player)sender);
 				session.select(Config.storage.getWorld(((Player)sender).getWorld().getName()));
 			}
 			else {
 				session = new Session(sender);
 				session.select(Config.storage.getWorld(getServer().getWorlds().get(0).getName()));
 			}
 			sessions.put(sender, session);
 		}
 		return sessions.get(sender);
 	}
 	
 	public void resetSessions() {
 		sessions.clear();
 	}
 	
 	private String name;
 	public String versionInfo;
 	private static Logger log = Logger.getLogger("Minecraft");
 	
 	// Are Listeners
 	private final AreaGuardWorldListener worldListener = new AreaGuardWorldListener(this);
 	private final AreaGuardPlayerListener playerListener = new AreaGuardPlayerListener(this);
     private final AreaGuardBlockListener blockListener = new AreaGuardBlockListener(this);
     private final AreaGuardEntityListener entityListener = new AreaGuardEntityListener(this);
     
     // Outside Plugins
     public PermissionsWrapper Security;
     //public Permissions Permissions = null;
     
     // Are data on each player
     protected final HashMap<String, PlayerSession> playerSessions = new HashMap<String, PlayerSession>();
 
     public void onEnable() {
     	Security = new PermissionsWrapper(this);
     	
     	Config.setup();
     	PluginDescriptionFile desc = getDescription();
         name = desc.getName();
         String authors = "";
         for (String author : desc.getAuthors()) authors += ", " + author;
         versionInfo = name + " version " + desc.getVersion() + 
         	(authors.isEmpty() ? "" : " by" + authors.substring(1));
         
        MainCommands main = new MainCommands(this);
        AreaCommands area = new AreaCommands(this);
        AdminCommands admin = new AdminCommands(this);
        CuboidCommands cuboid = new CuboidCommands(this);
        DebugCommands debug = new DebugCommands(this, main);
        
        getCommand("ag").setExecutor(main);
        getCommand("agmain").setExecutor(main);
        getCommand("area").setExecutor(area);
        getCommand("agarea").setExecutor(area);
        getCommand("agadmin").setExecutor(admin);
        getCommand("cuboid").setExecutor(cuboid);
        getCommand("agcuboid").setExecutor(cuboid);
        getCommand("agdebug").setExecutor(debug);
     	
     	registerEvents();
     	// setupOtherPlugins();
     	log.info( versionInfo + " is enabled!" );
     }
     
     public void onDisable() {
     	TempBlocks.deleteAll();
     }
     
     private void registerEvents() {
 		PluginManager pm = getServer().getPluginManager();
 		Priority preventPriority = Event.Priority.High;
 		
 		pm.registerEvent(Event.Type.CHUNK_UNLOADED, worldListener, preventPriority, this);
 		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, preventPriority, this);
 		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, preventPriority, this);
 		pm.registerEvent(Event.Type.ENTITY_DAMAGED, entityListener, preventPriority, this);
         pm.registerEvent(Event.Type.BLOCK_PLACED, blockListener, preventPriority, this);
         pm.registerEvent(Event.Type.BLOCK_DAMAGED, blockListener, preventPriority, this);
         pm.registerEvent(Event.Type.BLOCK_RIGHTCLICKED, blockListener, preventPriority, this);
         pm.registerEvent(Event.Type.BLOCK_INTERACT, blockListener, preventPriority, this);
     }
     
     /**
 	* Sends the player a message containing the area info.
 	* @param player	The player to send to
 	* @param area	The area to show
 	*/
     public void showAreaInfo(CommandSender sender, Area area) {
     	// TODO area info
     	sender.sendMessage(ChatColor.YELLOW + "Area ID: " + area.getId() + " Name: " + area.getName());
     }
     
     /**
 	* Sends the player a message containing the area info.
 	* @param player	The player to send to
 	* @param area	The area to show
 	*/
     public void showCuboidInfo(CommandSender sender, Cuboid cuboid) {
     	// TODO area info
     	showAreaInfo(sender, cuboid.getArea());
     	sender.sendMessage(ChatColor.YELLOW + "Cuboid ID: " + cuboid.getId() + " Status: " + (cuboid.isActive() ? "Active" : "Inactive"));
     }
     
     /**
 	* Checks if the player can modify the area settings.
 	* @param event	The area to check
 	* @param player	The player to check for
 	* @return if they can modify the area
 	*/
     public boolean canModify(Area area, Player player) {
     	return (area.isOwner(player.getName()) || Security.permission(player, "ag.create"));
     }
     
     /**
 	* Checks if the player can do the event and Cancels it he can't.
 	* It also sends	the player the Msgs for that event.
 	* @param event	The event to be canceled
 	* @param player	The player to check for
 	* @param lists	An array of list names for that event
 	* @param x		The x location of area to check
 	* @param y		The y location of area to check
 	* @param z		The z location of area to check
 	* @return if the event was canceled
 	*/
     public boolean checkEvent(Cancellable event, Player player, String[] lists, int x, int y, int z) {
     	// TODO fix
    	Cuboid cuboid = Config.storage.getWorld(player.getWorld().getName()).getCuboid(true, x, y, z);
     	if (cuboid != null && cuboid.exsists())
 		return checkEvent(event, player, lists, cuboid.getArea());
     	return true;
     }
 	
 	/**
 	* Checks if the player can do the event and Cancels it he can't.
 	* It also sends the player the Msgs for that event.
 	* @param event	The event to be canceled
 	* @param player	The player to check for
 	* @param lists	An array of list names for that event
 	* @param area	The area to check
 	* @return		If the event was canceled
 	*/
 	public boolean checkEvent(Cancellable event, Player player, String[] lists, Area area) {
 		if (area != null) {
 			
 			// In the event of an error
 			if (area instanceof ErrorArea) 
 				if (Security.permission(player, "ag.bypass.error")) {
 					Messager.error(player, "AreaGuard Failed! Bypassing error protection");
 					return false;
 				} else {
 					Messager.error(player, "AreaGuard Failed!");
 					event.setCancelled(true);
 					return false;
 				}
 			
 			// Check if the player can do the events 
 			if (playerCan(area, player, lists)) {
 				
 				// Send the allowed messages
 				for (String list : lists) {
 					String msg = area.getMsg(list).getMsg();
 					
 					// Debug msgs
 					if (getSession(player).isDebuging("msgs")) Messager.debug(player, "Sending msg " + list);
 					
 					if (!msg.isEmpty()) Messager.inform(player, msg); }
 			} else {
 				
 				// Bypass or Cancel the event
 				if (getSession(player).isBypassing())
 					Messager.warn(player, "Bypassing area permissions");
 				else event.setCancelled(true);
 				
 				// Send the not allowed messages
 				for (String list : lists) {
 					String msg = area.getMsg("no-"+list).getMsg();
 					
 					// Debug msgs
 					if (getSession(player).isDebuging("msgs")) Messager.debug(player, "Sending msg no-" + list);
 					
 					if (!msg.isEmpty()) Messager.warn(player, msg); }
 				return false;
 			}	
 		}
 		return true;
 	}
 	
 	// TODO fix
 	public boolean playerCan(Area area, Player player, String lists[]) {
 		boolean ret = true;
 		
 		// Find out if any of the events are restricted
 		for (String list : lists) if (area.getList("restrict").hasValue(list)) {
 			ret = false;
 			break;
 		}
 		
 		// Debug lists
 		if (getSession(player).isDebuging("lists")) 
 			Messager.debug(player, "Checking list restrict " + !ret);
 		
 		// Check the lists switching between them as seen fit
 		for (String list : lists) {
 			if (ret) {
 				if (area.getList("no-"+list).hasValue(player.getName())) ret = false;
 				
 				// Debug lists
 				if (getSession(player).isDebuging("lists")) 
 					Messager.debug(player, "Checking list no-" + list + " " + !ret);
 			} else { 
 				if (area.getList(list).hasValue(player.getName())) ret = true;
 				
 				// Debug lists
 				if (getSession(player).isDebuging("lists")) 
 					Messager.debug(player, "Checking list " + list + " " + ret);
 			}
 			
 		}
 		
 		return ret;
 	}
 
 	@Override
 	public void onLoad() {
 		// TODO Auto-generated method stub
 	}
 }
