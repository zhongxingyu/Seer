 package com.nexus.users;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.logging.Level;
 
 import twitter4j.auth.AccessToken;
 import twitter4j.auth.RequestToken;
 
 import com.nexus.NexusServer;
 import com.nexus.json.IProvideJsonMap;
 import com.nexus.json.JSONPacket;
 import com.nexus.logging.NexusLog;
 import com.nexus.mysql.MySQLHelper;
 import com.nexus.mysql.TableList;
 import com.nexus.utils.Utils;
 
 public class User implements IProvideJsonMap{
 	
 	private int ID;
 	public String Username;
 	public String Fullname;
 	public String Email;
 	public String Role;
 	public String ImageURL = "";
 	
 	public boolean CanReceiveNotify = true;
 	
 	public UserPrivilleges Capabilities;
 	
 	public RequestToken TwitterRequestToken;
 	public AccessToken TwitterAccessToken;
 	
 	public static User FromID(int ID){
 		return NexusServer.instance().UserPool.GetUser(ID);
 	}
 	
 	public static User FromUsername(String Username){
 		return NexusServer.instance().UserPool.GetUser(Username);
 	}
 	
 	public static User FromResultSet(ResultSet rs){
 		User u = new User();
 		try{
 			u.ID = rs.getInt("UID");
 			u.Username = rs.getString("Username");
 			u.Fullname = rs.getString("FullName");
 			u.Email = rs.getString("Email");
 			u.Role = rs.getString("Role");
 			u.Capabilities = new UserPrivilleges(u.Role);
 			if(!rs.getString("TwitterAccessToken").isEmpty()){
 				u.TwitterAccessToken = new AccessToken(rs.getString("TwitterAccessToken"), rs.getString("TwitterAccessTokenSecret"));
 			}
 		}catch(SQLException e){
 			NexusLog.log(Level.SEVERE, e, "Error while constructing user from an ResultSet");
 			return null;
 		}
 		return u;
 	}
 	
 	@Override
 	public JSONPacket toJsonMap(){
 		JSONPacket Packet = new JSONPacket();
 		Packet.put("ID", this.ID);
 		Packet.put("Username", this.Username);
 		Packet.put("Email", this.Email);
 		Packet.put("Role", this.Role);
 		Packet.put("Fullname", this.Fullname);
 		return Packet;
 	}
 	
 	@Override
 	public String toString(){
 		return Utils.Gson.toJson(this.toJsonMap());
 	}
 	
 	public void SaveData(){
 		try{
 			Connection conn = MySQLHelper.GetConnection();
 			Statement stmt = conn.createStatement();
 			//@formatter:off
 			stmt.execute(String.format("UPDATE %s SET ", TableList.TABLE_USERS) +
 					"Username = '" + this.Username + "', " +
 					"Fullname = '" + this.Fullname + "', " +
 					"Email = '" + this.Email + "', " +
 					"Role = '" + this.Role +"', " +
					"TwitterAccessToken = '" + (this.TwitterAccessToken == null ? "" : this.TwitterAccessToken.getToken()) + "', " +
					"TwitterAccessTokenSecret = '" + (this.TwitterAccessToken == null ? "" : this.TwitterAccessToken.getTokenSecret()) +
 				"' WHERE UID='" + this.ID + "'");
 			//@formatter:on
 			stmt.close();
 			conn.close();
 		}catch(SQLException e){
 			e.printStackTrace();
 		}
 	}
 	
 	public int getID(){
 		return this.ID;
 	}
 	
 	@Override
 	@Deprecated
 	public boolean equals(Object obj){
 		if(obj instanceof User){
 			return this.Username.equals(((User) obj).Username);
 		}
 		return false;
 	}
 	
 	@Override
 	@Deprecated
 	public int hashCode(){
 		return this.Username.hashCode();
 	}
 }
