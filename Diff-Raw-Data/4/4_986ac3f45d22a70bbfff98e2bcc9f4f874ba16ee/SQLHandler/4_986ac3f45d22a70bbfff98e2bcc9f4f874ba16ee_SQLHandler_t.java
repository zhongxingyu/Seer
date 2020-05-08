 package to.joe.bungee;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.*;
 
 import net.md_5.bungee.ChatColor;
 import net.md_5.bungee.Permission;
 
 import to.joe.bungee.SQLManager.SQLConnection;
 
 public class SQLHandler {
 
     private static SQLHandler instance = null;
 
     public static void banIP(String ip, String reason, String admin) throws SQLException {
         final SQLConnection con = SQLHandler.instance().manager.getQueryConnection();
         final PreparedStatement ban = con.getConnection().prepareStatement("INSERT INTO `ipbans` (`ip`,`reason`,`admin`,`timeofban`) VALUES(?,?,?,?)");
         ban.setString(1, ip);
         ban.setString(2, reason);
         ban.setString(3, admin);
         ban.setLong(4, new Date().getTime() / 1000);
         ban.executeUpdate();
         con.myWorkHereIsDone();
     }
 
     public static List<String> iplookup(String username) {
         final ArrayList<String> list = new ArrayList<String>();
         try {
             final SQLConnection con = SQLHandler.instance().manager.getQueryConnection();
             PreparedStatement statement = con.getConnection().prepareStatement("SELECT `IP` FROM `alias` WHERE `Name`=? ORDER BY `Time` desc LIMIT 1");
             statement.setString(1, username);
             ResultSet resultset = statement.executeQuery();
             String ip = null;
             if (resultset.next()) {
                 ip = resultset.getString("IP");
             } else {
                 list.add(ChatColor.RED + "No result for " + username);
             }
             if (ip != null) {
                 list.add(ChatColor.AQUA + "IPLookup on " + ChatColor.WHITE + username + ChatColor.AQUA + "\'s last IP: " + ChatColor.WHITE + ip);
                 statement = con.getConnection().prepareStatement("SELECT `Name`, `Time` FROM `alias` WHERE `IP`=? ORDER BY `Time` DESC LIMIT 5");
                 statement.setString(1, ip);
                 resultset = statement.executeQuery();
                 while (resultset.next()) {
                    list.add(ChatColor.AQUA + resultset.getString("Name") + " : " + ChatColor.BLUE + new Date(resultset.getTimestamp("Time").getTime()));
                 }
             }
             con.myWorkHereIsDone();
         } catch (final Exception e) {
             list.add(ChatColor.RED + "Error on lookup.");
         }
         return list;
     }
 
     public static boolean isAllowed(String info, boolean nameBan) throws SQLException {
         final SQLConnection con = SQLHandler.instance().manager.getQueryConnection();
         final String query;
         if (nameBan) {
             query = "SELECT `id` FROM `j2bans` WHERE `name`=? AND `unbanned`=0";
         } else {
             query = "SELECT * FROM `ipbans` WHERE `ip`=? AND `unbanned`=0";
         }
         final PreparedStatement statement = con.getConnection().prepareStatement(query);
         statement.setString(1, info);
         final boolean ret = !statement.executeQuery().first();
         con.myWorkHereIsDone();
         return ret;
     }
 
     public static Map<Permission, List<String>> loadAdmins() {
         final List<String> admins = new ArrayList<String>();
         final List<String> srstaff = new ArrayList<String>();
         try {
             final SQLConnection con = SQLHandler.instance().manager.getQueryConnection();
             final PreparedStatement query = con.getConnection().prepareStatement("SELECT `name`,`group` from `users` where `group`='admins' or `group`='srstaff';");
             final ResultSet resultSet = query.executeQuery();
             if (resultSet != null) {
                 while (resultSet.next()) {
                     final String name = resultSet.getString("name").toLowerCase();
                     admins.add(name);
                     if (resultSet.getString("group").equals("srstaff")) {
                         srstaff.add(name);
                     }
                 }
             }
             con.myWorkHereIsDone();
         } catch (final SQLException e) {
         }
         final Map<Permission, List<String>> map = new HashMap<>();
         map.put(Permission.MODERATOR, admins);
         map.put(Permission.ADMIN, srstaff);
         return map;
     }
 
     public static void track(String username, String ip) throws SQLException {
         final SQLConnection con = SQLHandler.instance().manager.getQueryConnection();
         final PreparedStatement ps = con.getConnection().prepareStatement("SELECT * FROM `alias` WHERE Name=? AND IP=?");
         ps.setString(1, username);
         ps.setString(2, ip);
         final ResultSet rs = ps.executeQuery();
         if (rs.next()) {
             int count = rs.getInt("Logins");
             count++;
             final PreparedStatement increment = con.getConnection().prepareStatement("UPDATE `alias` SET `logins`=?, Time=now() WHERE `Name`=? AND `IP`=?");
             increment.setInt(1, count);
             increment.setString(2, username);
             increment.setString(3, ip);
             increment.executeUpdate();
         } else {
             final PreparedStatement insertIntoAlias = con.getConnection().prepareStatement("INSERT INTO `alias` (`Name`,`IP`,`Time`,`Logins`) VALUES(?,?,now(),?)");
             insertIntoAlias.setString(1, username);
             insertIntoAlias.setString(2, ip);
             insertIntoAlias.setInt(3, 1);
             insertIntoAlias.executeUpdate();
         }
         con.myWorkHereIsDone();
     }
 
     public static void unbanIP(String ip) throws SQLException {
         final SQLConnection con = SQLHandler.instance().manager.getQueryConnection();
         final PreparedStatement unban = con.getConnection().prepareStatement("UPDATE `ipbans` SET `unbanned`=1 WHERE `ip`=?");
         unban.setString(1, ip);
         unban.executeUpdate();
         con.myWorkHereIsDone();
     }
 
     private static SQLHandler instance() {
         if (SQLHandler.instance == null) {
             throw new RuntimeException("Not loaded!");
         }
         return SQLHandler.instance;
     }
 
     static void start(String host, int port, String user, String pass, String db) {
         try {
             Class.forName("com.mysql.jdbc.Driver");
         } catch (final ClassNotFoundException e1) {
             throw new RuntimeException("Couldn't find driver.");
         }
         SQLHandler.instance = new SQLHandler(host, port, user, pass, db);
     }
 
     private SQLManager manager;
 
     public SQLHandler() {
         throw new RuntimeException("Stop right there, criminal scum");
     }
 
     private SQLHandler(String host, int port, String user, String pass, String db) {
         try {
             this.manager = new SQLManager("jdbc:mysql://" + host + ":" + port + "/" + db + "?autoReconnect=true&user=" + user + "&password=" + pass);
         } catch (final Exception e) {
             throw new RuntimeException("SQL connection failure!", e);
         }
     }
 
 }
