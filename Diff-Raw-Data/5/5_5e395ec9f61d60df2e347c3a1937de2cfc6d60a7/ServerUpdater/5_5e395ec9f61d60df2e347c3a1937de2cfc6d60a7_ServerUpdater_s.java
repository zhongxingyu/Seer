 package net.robbytu.banjoserver.framework.events;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 
 import net.robbytu.banjoserver.framework.Main;
 
 import org.bukkit.Bukkit;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 public class ServerUpdater implements Listener {
 	private Connection conn = Main.conn;
 	
 	/**
 	 * Updates server online status
 	 * @param online Whether the server is online or not
 	 */
 	public void setOnline(int online) {
 		// Update our Server entry in the database
 		try {
			this.conn.createStatement().executeQuery("UPDATE bs_servers SET online = " + online + " AND players = 0 WHERE servername = '" + Bukkit.getServer().getServerName() + "'");
 		}
 		catch (SQLException e) {
 			Bukkit.getLogger().warning("Framework could not update server online status.");
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Updates online player count
 	 */
 	private void updatePlayerCount() {
 		// Update online count
 		try {
 			int onlineCount = Bukkit.getServer().getOnlinePlayers().length;
			this.conn.createStatement().executeQuery("UPDATE bs_servers SET players = " + onlineCount + " WHERE servername = '" + Bukkit.getServer().getServerName() + "'");
 		}
 		catch (SQLException e) {
 			Bukkit.getLogger().warning("Framework could not update online player count.");
 			e.printStackTrace();
 		}
 	}
 	
     @EventHandler
     public void onPlayerLogin(PlayerLoginEvent event) {
         this.updatePlayerCount();
     }
     
     @EventHandler
     public void onPlayerQuit(PlayerQuitEvent event) {
         this.updatePlayerCount();
     }
 }
