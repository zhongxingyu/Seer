 package net.betterverse;
 
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerPreLoginEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class BanManager extends JavaPlugin implements Listener {
 
     public static Connection conn;
 
     public void log(String message) {
         getLogger().info(message);
     }
 
     @Override
     public void onDisable() {
         try {
             conn.close();
         } catch (SQLException e) {
             Logger.getLogger(BanManager.class.getName()).log(Level.SEVERE, null, e);
         }
         log("v" + getDescription().getVersion() + " disabled.");
     }
 
     @Override
     public void onEnable() {
         if (this.getConfig() == null) {
             this.saveDefaultConfig();
         }
         getServer().getPluginManager().registerEvents(this, this);
         try {
             conn = DriverManager.getConnection("jdbc:mysql://" + this.getConfig().getString("host") + ":" + this.getConfig().getString("port") + "/" + this.getConfig().getString("database"), this.getConfig().getString("username"), this.getConfig().getString("password"));
             DatabaseMetaData dbm = conn.getMetaData();
 
             ResultSet tables = dbm.getTables(null, null, "bans", null);
             if (tables.next() == false) {
                 PreparedStatement st = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `bans` (`id` int(50) NOT NULL AUTO_INCREMENT,`username` varchar(50) NOT NULL,`reason` varchar(5000) NOT NULL,`closed` tinyint(1) NOT NULL DEFAULT '0',`banned_by` varchar(50) NOT NULL,PRIMARY KEY (`id`)) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=22 ;");
                 st.executeUpdate();
             }
         } catch (SQLException e) {
             log("Please check your configuration!");
         }
         log("v" + getDescription().getVersion() + " enabled.");
     }
 
     public String searchPlayer(String regex) {
         List<Player> players = getServer().matchPlayer(regex);
         if (!players.isEmpty()) {
             return players.get(0).getName();
         } else {
             return regex;
         }
     }
 
     public Boolean isBanned(String player) {
         try {
             PreparedStatement stmt = conn.prepareStatement("SELECT username FROM bans WHERE `username`=? AND `closed`=?");
             stmt.setString(1, player);
             stmt.setBoolean(2, false);
             ResultSet rs = stmt.executeQuery();
             if (rs.next()) {
                 rs.close();
                 stmt.close();
                 return true;
             } else {
                 rs.close();
                 stmt.close();
                 return false;
             }
 
         } catch (SQLException e) {
             Logger.getLogger(BanManager.class.getName()).log(Level.SEVERE, null, e);
             return null;
         }
 
     }
 
     public void banUser(String name, String reason, String banned_by) {
         try {
             String insertStatement = "Insert into bans (username,reason,banned_by) values (?,?,?)";
             PreparedStatement prepStmt = conn.prepareStatement(insertStatement);
             prepStmt.setString(1, name);
             prepStmt.setString(2, reason);
             prepStmt.setString(3, banned_by);
             prepStmt.executeUpdate();
             prepStmt.close();
         } catch (SQLException ex) {
             Logger.getLogger(BanManager.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void unbanUser(String player) {
         try {
             PreparedStatement prepStmt = conn.prepareStatement("UPDATE bans SET `closed`=? WHERE `username`=? AND `closed`=?");
             prepStmt.setBoolean(1, true);
             prepStmt.setString(2, player);
             prepStmt.setBoolean(3, false);
             prepStmt.executeUpdate();
             prepStmt.close();
         } catch (SQLException ex) {
             Logger.getLogger(BanManager.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPrePlayerLogin(PlayerPreLoginEvent event) {
         if (isBanned(event.getName())) {
             event.setResult(PlayerPreLoginEvent.Result.KICK_OTHER);
             event.disallow(event.getResult(), "You are banned from this server!");
         }
 
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         Player player = null;
         if (sender instanceof Player) {
             player = (Player) sender;
         }
         if (cmd.getName().equalsIgnoreCase("ban")) {
             if (args.length > 1) {
                 String name = searchPlayer(args[0]);
                 Player target = this.getServer().getPlayer(name);
                 if (isBanned(name)) {
                     sender.sendMessage("User is already banned!");
                 } else {
                     String reason = "";
                    for (int i = 1; i < args.length; i++) {
                        reason = reason + " " + args[i];
                     }
                     banUser(name, reason, sender.getName());
                     if (target != null) {
                         if (target.isOnline()) {
                             this.getServer().getPlayer(name).kickPlayer("You have been banned! " + reason);
                         }
                     }
                     sender.sendMessage("Banned user " + name);
 
                 }
                 return true;
             }
         } else if (cmd.getName().equalsIgnoreCase("unban")) {
             if (args.length == 1) {
                 String name = searchPlayer(args[0]);
                 Player target = this.getServer().getPlayer(name);
                 if (isBanned(name)) {
                     unbanUser(name);
                     sender.sendMessage("Unbanned user " + name);
                 } else {
                     sender.sendMessage("User " + name + " is not banned!");
                 }
                 return true;
             }
         }
         return false;
     }
 }
