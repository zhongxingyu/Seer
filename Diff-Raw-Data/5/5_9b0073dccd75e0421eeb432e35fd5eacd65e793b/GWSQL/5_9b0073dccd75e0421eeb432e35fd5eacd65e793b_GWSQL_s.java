 package net.loadingchunks.plugins.GuardWolf;
 
 import java.sql.*;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 
 /**
  * Handler for the /gw sample command.
  * @author Cue
  */
 public class GWSQL {
 	public Connection con;
 	public Statement stmt;
 	public final GuardWolf plugin;
 	
 	public GWSQL(GuardWolf plugin)
 	{
 		this.plugin = plugin;
 	}
 	
 	public void Connect()
 	{
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			this.con = DriverManager.getConnection(this.plugin.gwConfig.get("db_address"), this.plugin.gwConfig.get("db_user"), this.plugin.gwConfig.get("db_pass"));
 		} catch ( SQLException e )
 		{
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) { e.printStackTrace(); }
 	}
 	
 	public void Ban(String name, String banner, long time, String reason, int permanent)
 	{
 		Integer strike = 1;
 		try {
 			PreparedStatement stat = con.prepareStatement("INSERT INTO `" + this.plugin.gwConfig.get("db_table") + "`" +
 					"(`user`,`country`,`banned_at`,`expires_at`,`reason`,`banned_by`,`strike`,`strike_expires`,`unbanned`,`permanent`)" +
 					" VALUES ('" + name + "','?',NOW(),FROM_UNIXTIME(" + time + "),'" + reason + "','" + banner + "'," + strike + ",NOW(),0," + permanent + ")"
 					);
 			stat.execute();
 		} catch ( SQLException e ) { e.printStackTrace(); }
 	}
 	
 	public void UnBan(String name, String unbanner)
 	{
 		try {
 			PreparedStatement stat = con.prepareStatement("UPDATE `" + this.plugin.gwConfig.get("db_table") + "` SET `unbanned` = 0 WHERE `user` = '" + name + "' LIMIT 1");
 			stat.execute();
 		} catch ( SQLException e ) { e.printStackTrace(); }
  	}
 	
 	public void Stats()
 	{
 		try {
 			Statement stat = con.createStatement();
 			ResultSet result = stat.executeQuery("SELECT COUNT(*) as counter FROM `" + this.plugin.gwConfig.get("db_table") + "`");
 			result.next();
 			System.out.println("Ban Records: " + result.getInt("counter"));
 		} catch ( SQLException e ) { e.printStackTrace(); }
 	}
 	
 	public String CheckBan(String user)
 	{
 		System.out.println("[GW] Checking ban status...");
 		try {
 			PreparedStatement stat = con.prepareStatement("SELECT * FROM `mcusers_ban` WHERE (expires_at > NOW() OR `permanent` = 1) AND `user` = '" + user + "' AND `unbanned` = 0 ORDER BY id DESC");
 			ResultSet result = stat.executeQuery();
 			if(result.last())
 			{
 				if(result.getInt("permanent") == 1)
 					return result.getString("reason") + " (Permanent Ban)";
 				else
 					return result.getString("reason") + " (Expires " + result.getString("expires_at") + ")";
 			} else return null;
 		} catch ( SQLException e ) { e.printStackTrace(); }
 		return null;
 	}
 	
 	public String ListBan(int page, String user, CommandSender sender)
 	{
 		String returnString = "";
 		if(user.isEmpty())
 		{
 			try {
 				PreparedStatement stat = con.prepareStatement("SELECT *,COUNT(*) as c FROM `mcusers_ban` GROUP BY `user` ORDER BY `permanent`,`expires_at` DESC LIMIT " + ((page - 1)*(Integer.parseInt(this.plugin.gwConfig.get("per_page")))) + "," + (Integer.parseInt(this.plugin.gwConfig.get("per_page"))));
 				ResultSet result = stat.executeQuery();
 				
 				if(!result.last())
 					return ChatColor.RED + "No bans found.";
 				else {
 					result.first();
 					do
 					{
						returnString = returnString + "\n- " + result.getString("user") + " (" + result.getInt("c") + " bans found)";
 					} while(result.next());
 					return returnString;	
 				}
 			} catch ( SQLException e ) { e.printStackTrace(); }
 		} else {
 			try {
 				PreparedStatement stat = con.prepareStatement("SELECT * FROM `mcusers_ban` WHERE `user` = '" + user + "' ORDER BY `permanent`,`expires_at` DESC LIMIT " + ((page - 1)*(Integer.parseInt(this.plugin.gwConfig.get("per_page")))) + "," + (Integer.parseInt(this.plugin.gwConfig.get("per_page"))));
 				ResultSet result = stat.executeQuery();
 				
 				if(!result.last())
 					return ChatColor.RED + "No bans found for this user.";
 				else {
 					result.first();
 					do
 					{
 						System.out.print("Look what I found!");
						returnString = returnString + "\n- " + result.getString("reason");
 						if(result.getInt("permanent") == 1)
 							returnString = returnString + " (Permanent)";
 						else
 							returnString = returnString + " (Expires: " + result.getString("expires_at") + ")";
 					} while (result.next());
 					return returnString;	
 				}
 			} catch ( SQLException e ) { e.printStackTrace(); }
 		}
 		return "Error getting Ban List!";
 	}
 }
