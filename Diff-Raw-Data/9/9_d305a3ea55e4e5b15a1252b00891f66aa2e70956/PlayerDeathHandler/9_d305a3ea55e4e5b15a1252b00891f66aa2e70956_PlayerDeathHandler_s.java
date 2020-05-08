 package net.swagserv.andrew2060.swagservbounties;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 
 public class PlayerDeathHandler implements Listener {
 	Bounties plugin;
 	MySQLConnection sqlHandler;
 	public PlayerDeathHandler(Bounties bounties) {
 		this.plugin = bounties;
 		try {
 			this.sqlHandler = new MySQLConnection(plugin.dbHost, plugin.dbPort, plugin.dbDatabase, plugin.dbUser,
 					plugin.dbPass);
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		sqlHandler.connect();
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPLayerDeath(PlayerDeathEvent event) throws SQLException {
 		Player p = event.getEntity();
 		if(p.getKiller() == null) {
 			return;
 		}
 		if(p.getKiller() == p) {
 			return;
 		}
 		if(!checkExists(p.getName())) {
 			return;
 		}
 		Player k = p.getKiller();
 		String getResult = "Select * FROM bountiesplayer WHERE target = '" + p.getName() + "'";
 		ResultSet rs = plugin.sqlHandler.executeQuery(getResult, false);
 		rs.last();
 		double payout = rs.getDouble("amount");
 		if(payout == 0) {
 			return;
 		}
		plugin.economy.depositPlayer(p.getName(), payout);
 		Bukkit.getServer().broadcastMessage(ChatColor.AQUA + "[Bounties]: " + ChatColor.GREEN + k.getName() + ChatColor.GRAY + " has collected the " + ChatColor.GOLD + "$" + payout + ChatColor.GRAY + " bounty on " + ChatColor.GREEN + p.getName());
 		int id = rs.getInt("id");
 		String update = "DELETE FROM bountiesplayer WHERE id='" + id + "'";
 		plugin.sqlHandler.executeQuery(update, true);
 	}
 
 	private boolean checkExists(String wantedPlayerName) {
 		String checkExists = "SELECT * FROM bountiesplayer WHERE target = '"+wantedPlayerName + "'";
 		try {
 			ResultSet checkResult = plugin.sqlHandler.executeQuery(checkExists, false);
 			if(checkResult.last()) {
 				return true;
 			} else {
 				return false;
 			}
 		} catch (SQLException e) {
 				e.printStackTrace();
 		}
 		return false;
 	}
 
 }
