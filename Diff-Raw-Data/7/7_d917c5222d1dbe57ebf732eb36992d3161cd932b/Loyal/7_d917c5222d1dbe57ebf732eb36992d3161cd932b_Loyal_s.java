 package eu.docserver.docplugin;
 
 import java.sql.SQLException;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitRunnable;
  
 public class Loyal extends BukkitRunnable {
  
     private final JavaPlugin plugin;
	private DatabaseFunc dbFunc;
     
    public Loyal(JavaPlugin plugin, DatabaseFunc db) throws SQLException {
         this.plugin = plugin;
     }
  
     public void run() {
     	Player[] list = plugin.getServer().getOnlinePlayers();
     	plugin.getLogger().info("Nb Players: " + list.length);
         for (int i = 0; i < list.length; i++) {
         	try {
 				dbFunc.addMoney(list[i].getDisplayName(), 1);
 				list[i].sendMessage(ChatColor.GOLD + "+ 1$ !");
 			} catch (SQLException e1) {
 				e1.printStackTrace();
 			}
         }
     }
 }
