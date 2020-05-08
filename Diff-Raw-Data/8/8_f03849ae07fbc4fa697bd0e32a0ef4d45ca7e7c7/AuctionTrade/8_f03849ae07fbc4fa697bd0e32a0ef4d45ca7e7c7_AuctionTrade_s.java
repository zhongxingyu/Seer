 /**
  * 
  */
 package me.ibhh.AuctionTrade;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * @author Simon
  * 
  */
 public class AuctionTrade extends JavaPlugin 
 {
 	public float Version = 0;
 	private SQLConnectionHandler con;
 	private PermissionsHandler Permission;
 	private String action = "";
 
 	/**
 	 * Called by Bukkit on stopping the server
 	 * 
 	 * @param
 	 * @return
 	 */
 	@Override
 	public void onDisable() {
 		con.CloseCon();
 		System.out.println("[AuctionTrade] disabled!");
 
 	}
 	
 	public boolean UpdateConfig() {
 		try {
 			getConfig().options().copyDefaults(true);
 			saveConfig();
 			reloadConfig();
 			System.out.println("[AuctionTrade] Config file found!");
 			return true;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	/**
 	 * Called by Bukkit on starting the server
 	 * 
 	 * @param
 	 * @return
 	 */
 	@Override
 	public void onEnable() 
 	{
 
 		UpdateConfig();
 		con = new SQLConnectionHandler(this);
 		Permission = new PermissionsHandler(this);
 		try {
 			con.createConnection();
 		} catch (Exception e) {
 			e.printStackTrace();
 			onDisable();
 			return;
 		}
 		try {
 			con.PrepareDB();
 		} catch (Exception e) {
 			e.printStackTrace();
 			onDisable();
 			return;
 		}
 		try {
 			aktuelleVersion();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		try {
 			iConomyHandler.iConomyversion();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		System.out.println("[AuctionTrade] Version: " + Version
 				+ " successfully enabled!");
 
 		String URL = "http://ibhh.de:80/aktuelleversionAuctionTrade.html";
 		if ((Update.UpdateAvailable(URL, Version) == true)) {
 			System.out.println("[AuctionTrade] New version: "
 					+ Update.getNewVersion(URL) + " found!");
 			System.out
 					.println("[AuctionTrade] ******************************************");
 			System.out
 					.println("[AuctionTrade] *********** Please update!!!! ************");
 			System.out
 					.println("[AuctionTrade] * http://ibhh.de/AuctionTrade.jar *");
 			System.out
 					.println("[AuctionTrade] ******************************************");
 			if (getConfig().getBoolean("autodownload") == true) {
 				try {
 					String path = getDataFolder().toString() + "/Update/";
 					Update.autoUpdate("http://ibhh.de/AuctionTrade.jar", path,
 							"AuctionTrade.jar");
 				} catch (Exception e) {
 					System.out
 							.println("[AuctionTrade] "
 									+ "Error on checking permissions with PermissionsEx!");
 					e.printStackTrace();
 					return;
 				}
 
 			} else {
 				System.out
 						.println("[AuctionTrade] Please type [AuctionTrade download] to download manual! ");
 			}
 		}
 	}
 
 	/**
 	 * Gets version.
 	 * 
 	 * @param
 	 * @return float: Version of the installed plugin.
 	 */
 	public float aktuelleVersion() {
 		try {
 			Version = Float.parseFloat(getDescription().getVersion());
 		} catch (Exception e) {
 			System.out
 					.println("[AuctionTrade]Could not parse version in float");
 		}
 		return Version;
 	}
 
 
 	/**
 	 * Called by Bukkit on reloading the server
 	 * 
 	 * @param
 	 * @return
 	 */
 	public void onReload() {
 		onEnable();
 	}
 
 	protected static boolean isConsole(CommandSender sender) {
 		return !(sender instanceof Player);
 	}
 
 	/**
 	 * Called by Bukkit if player posts a command
 	 * 
 	 * @param none
 	 *            , cause of Bukkit.
 	 * @return true if no errors happened else return false to Bukkit.
 	 */
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label,
 			String[] args) {
 		if (sender instanceof Player) {
 			Player player = (Player) sender;
 			action = args[0];
 			if (cmd.getName().equalsIgnoreCase("xpShop")) 
 			{
 				if(Permission.checkpermissions(player, action))
 				{
					
 				}
 			}
 		} else {
 			if (isConsole(sender)) {
 				if (cmd.getName().equalsIgnoreCase("AuctionTrade")) {
 					if (args.length == 1) {
 						if (args[0].equals("download")) {
 							String path = getDataFolder().toString()
 									+ "/Update/";
 							Update.autoUpdate(
 									"http://ibhh.de/AuctionTrade.jar", path,
 									"AuctionTrade.jar");
 						}
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 }
