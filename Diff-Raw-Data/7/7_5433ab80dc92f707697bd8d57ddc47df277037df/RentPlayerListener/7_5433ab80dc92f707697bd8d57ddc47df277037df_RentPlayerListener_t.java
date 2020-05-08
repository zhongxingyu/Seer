 package me.krotn.Rent;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerLoginEvent;
 
 /**
  * This class listens for player events from Bukkit.
  * 
  * Currently it receives only PlayerJoin events.
  * 
  * @see org.bukkit.event.player
  * @see org.bukkit.event.player.PlayerJoinEvent
  */
 
 public class RentPlayerListener extends PlayerListener{
 	
 	private final Rent plugin;
 	private final RentLogManager logManager;
 	private RentDatabaseManager dbMan;
 	private RentPropertiesManager propMan;
 	private RentCalculationsManager calcMan;
 	private RentPermissionsManager permMan;
 	
 	public RentPlayerListener(Rent plugin){
 		this.plugin = plugin;
 		this.logManager = this.plugin.getLogManager();
 		this.dbMan = this.plugin.getDatabaseManager();
 		this.propMan = this.plugin.getPropertiesManager();
 		this.calcMan = this.plugin.getCalculationsManager();
 		this.permMan = this.plugin.getPermissionsManager();
 	}
 	
 	@Override
 	public void onPlayerLogin(PlayerLoginEvent event){
		plugin.getDateUtils().sanityCheck();
 		boolean denyLogin = new Boolean(propMan.getProperty("banOnNonpayment")).booleanValue();
 		double banLevel = new Double(propMan.getProperty("banThreshold")).doubleValue();
 		if(!denyLogin){
 			return;
 		}
 		if(dbMan.playerExists(event.getPlayer().getName())){
 			if(calcMan.getAmountPlayerOwes(dbMan.getPlayerID(event.getPlayer().getName()))>banLevel&&!(permMan.checkPermission(event.getPlayer(), "neverban"))){
 				event.disallow(PlayerLoginEvent.Result.KICK_OTHER, propMan.getProperty("nonPayBanMessage"));
 				//Above line denies players for nonpayment.
 				logManager.info("Denied "+event.getPlayer().getName()+" for nonpayment!");
 				return;
 			}
 		}
 	}
 	
 	@Override
 	public void onPlayerJoin(PlayerJoinEvent event){
 		logManager.info("Logged entry for: "+event.getPlayer().getName()+".");
 		String playerName = event.getPlayer().getName();
 		Player player = event.getPlayer();
 		if(!dbMan.playerExists(playerName)){
 			dbMan.addPlayer(playerName);
 			logManager.info("New player, "+playerName+" added to the database.");
 		}
 		plugin.getDateUtils().sanityCheck(); //Make sure that current month exists.
 		int monthID = dbMan.getMonthID(plugin.getDateUtils().getCurrentMonth());
 		int playerID = dbMan.getPlayerID(event.getPlayer().getName().toLowerCase());
 		boolean playerNotTracked = permMan.checkPermission(player, "untracked");
 		if(!dbMan.hasPlayerLoggedIn(dbMan.getPlayerID(event.getPlayer().getName().toLowerCase()), monthID)&&!playerNotTracked){
 			dbMan.addLogin(playerID, monthID);
 			logManager.info("First monthly login for: "+event.getPlayer().getName()+".");
 			String loginMessage = propMan.getProperty("firstMonthlyLoginMessage");
 			player.sendRawMessage(ChatColor.RED+loginMessage);
 		}
 	}
 	
 }
