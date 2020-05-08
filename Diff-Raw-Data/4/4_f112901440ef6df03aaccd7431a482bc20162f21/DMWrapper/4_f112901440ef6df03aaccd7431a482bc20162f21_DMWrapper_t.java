 /**
  * 
  */
 package me.slaps.DMWrapper;
 
 import java.io.File;
 import java.util.logging.Logger;
 import java.util.HashMap;
 
 import org.bukkit.Server;
 import org.bukkit.Location;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginLoader;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 
 import com.gmail.haloinverse.DynamicMarket.DynamicMarket;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 
 /**
  * @author magik
  *
  */
 public class DMWrapper extends JavaPlugin {
 
 	public String name; // = "DMWrapper";
 	public String codename = "Rwanda";
 	public static String version; // = "0.02";
 	public static String directory; // = "DMWrapper" + File.separator;
 	
 	public final Logger log = Logger.getLogger("Minecraft");
 	public Server server;
 	
 	public static Permissions perms;
 	public static DynamicMarket sm;
 	
 	protected LocationManager locMgr;
 	protected HashMap<String, String> cmdMap = new HashMap<String, String>();
 	protected HashMap<String, ShopLocation> tmpShop = new HashMap<String, ShopLocation>();
 
 	public static DMWrapperBlockListener blockListener;
 	public static DMWrapperPlayerListener playerListener;
 		
 	
 	public DMWrapper(PluginLoader pluginLoader, Server instance,
 			PluginDescriptionFile desc, File folder, File plugin,
 			ClassLoader cLoader) {
 		super(pluginLoader, instance, desc, folder, plugin, cLoader);
 
 		folder.mkdir();
 		name = desc.getName();
 		version = desc.getVersion();
 		directory = getDataFolder() + File.separator;
 		
 		server = instance;
 		
 		locMgr = new LocationManager(this);
 				
 		log.info(name+" ("+version+") loading...");
 		
 		blockListener =  new DMWrapperBlockListener(this);
 		playerListener = new DMWrapperPlayerListener(this);
 		
         // rewrite some params for passing to DynamicMarket's constructor
 		File smFolder = new File("plugins"+File.separator+"DynamicMarket");
 		File smPlugin = new File("plugins"+File.separator+"DynamicMarket.jar");
 		PluginDescriptionFile smDesc = new PluginDescriptionFile("DynamicMarket", "0.4.3","");
 		
 		sm = new DynamicMarket(pluginLoader, instance, smDesc, smFolder, smPlugin, cLoader);
 	}
 
 	@Override
 	public void onDisable() {
 		sm.onDisable();
 		log.info(name+" ("+version+") disabled");
 	}
 
 	@Override
 	public void onEnable() {
 		setupPermissions();
 		cmdMap.clear();
 		sm.onEnable();
 		getServer().getPluginManager().registerEvent(Event.Type.BLOCK_RIGHTCLICKED, blockListener, Priority.Monitor, this);
 		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Monitor, this);
 		log.info(name+" ("+version+") enabled");
 	}
 	
 	public void setupPermissions()
 	{
 		Plugin test = getServer().getPluginManager().getPlugin("Permissions");
 	 
 		if (perms == null)
 			if (test != null) {
 				perms = (Permissions)test;
 			} else {
 				log.info( "["+name+"] Permission system not enabled. Disabling plugin.");
 				getServer().getPluginManager().disablePlugin(this);
 			}
 	}
 	
 	private boolean hasPermission(CommandSender sender, String permString)
 	{
 		//CHANGED: Added this to streamline permission checking code.
 		if (sender instanceof Player)
 			return Permissions.Security.permission((Player)sender, name.toLowerCase()+"."+permString);
 		return true;
 	}	
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		// only respond to player commands
		if ( !(sender instanceof Player) ) return false;
		
 		if (cmd.getName().toLowerCase().equals("shop")) {
 			
 			if ( (args.length > 0) && (args[0].equalsIgnoreCase("location")) ) {
 				if (!hasPermission(sender, "location")) {
 					return false;
 				}
 
 				String pname = ((Player)sender).getName();						
 				if ( args.length == 2 && args[1].equalsIgnoreCase("set") ) {
 					sender.sendMessage("please right click the 1st corner");
 					cmdMap.put(pname, "location set");
 					return true;
 				} else if ( args.length == 2 && args[1].equalsIgnoreCase("cancel") ) {
 					if ( cmdMap.get(pname) == null ) {
 						sender.sendMessage("No operation to cancel");
 					} else {
 						sender.sendMessage("Operation canceled");
 						cmdMap.remove(pname);
 					}
 					return true;
 				} else if ( args.length == 2 && args[1].equalsIgnoreCase("check") ) {
 					Integer id = locMgr.getShopID(((Player)sender).getLocation());
 					if ( id > 0 ) {
 						sender.sendMessage("Shop ID: "+id);
 					} else {
 						sender.sendMessage("No shop location found here");
 					}
 					return true;
 				} else if ( args.length == 3 && args[1].equalsIgnoreCase("remove") ) {
 					if ( locMgr.removeShopByID(Integer.parseInt(args[2])) ) { 
 						sender.sendMessage("Shop Location removed");	
 					} else {
 						sender.sendMessage("Could not remove Shop ID: "+Integer.parseInt(args[2]));
 					}
 					return true;
 				} else if ( args.length == 2 && args[1].equalsIgnoreCase("enable") ) {
 					sender.sendMessage("Shop Locations enabled");
 					locMgr.enableShopLocations();
 					return true;
 				} else if ( args.length == 2 && args[1].equalsIgnoreCase("disable") ) {
 					sender.sendMessage("Shop Locations disabled");
 					locMgr.disableShopLocations();
 					return true;
 				} else if ( args.length == 2 && args[1].equalsIgnoreCase("list") ) {
 					sender.sendMessage("Shop IDs: "+locMgr.listShops());
 					return true;
 				} else if ( args.length == 3 && args[1].equalsIgnoreCase("tp") ) {
 					if ( sender instanceof Player ) {
 						Location dest = locMgr.getCenterOfShop(Integer.parseInt(args[2]));
 						if ( dest == null ) {
 							sender.sendMessage("Could not find shop.");
 						} else {
 							((Player)sender).teleportTo(dest);
 						}
 					}
 					return true;
 				} else {
 					// send player message on how to use /shop loc
 					sender.sendMessage("Usage: /shop location set - starts the shop setup process");
 					sender.sendMessage("         /shop location cancel - cancels setting a shop location");
 					sender.sendMessage("         /shop location check - checks ID of current location");
 					sender.sendMessage("         /shop location remove <ID> - removes the shop location");
 					sender.sendMessage("         /shop location enable - enables location based shops");
 					sender.sendMessage("         /shop location disable - disable location based shops");
 					sender.sendMessage("         /shop location list - lists shop IDs");
 					sender.sendMessage("         /shop location tp <ID> - teleports to shop");
 					return true; 
 				}
 			}
 			
 			if ( args.length > 0 && locMgr.shopLocationsEnabled ) {
 				if ( args[0].equalsIgnoreCase("help") ) {
 					return sm.onCommand(sender,cmd,commandLabel,args);
 				} else if ( locMgr.inShopLoc(((Player)sender).getLocation()) ) {
 					return sm.onCommand(sender,cmd,commandLabel,args);
 				} else {
 					sender.sendMessage("Not in the shopping area!");
 					return true;
 				}
 			} else {
 				return sm.onCommand(sender,cmd,commandLabel,args);
 			}
 			
 		} else { 
 			
 			return false;
 			
 		}
 		
 	}
 	
 	
 	
 }
