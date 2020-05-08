 package codeGenerator;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import jsonObjects.PlayerJson;
 
 public class PlayerSQLGenerator 
 {
 	public static void updatePlayers(ArrayList<PlayerJson> players, String path,
 			String userName, String password)
 	{
 		Connection conn;
 		PreparedStatement stmt;
 		ResultSet rs;
 		
 		Collections.sort(players, PlayerJson.COMPARE_BY_ID);
 		ArrayList<PlayerJson> newPlayers = new ArrayList<PlayerJson>(players);
 		
 		try 
 		{
 			Class.forName("com.mysql.jdbc.Driver");
 			conn = DriverManager.getConnection(path,userName,password);
 			
 			stmt = conn.prepareStatement("SELECT * FROM `nba2`.`player`");
 			rs = stmt.executeQuery();
 			
 			while(rs.next())
 		    {
 		    	newPlayers.remove(Collections.binarySearch(newPlayers,
 		    			new PlayerJson(rs.getInt("player_id"), "", true, 0, 0), PlayerJson.COMPARE_BY_ID));
 		    }
 			
 			for(PlayerJson player : newPlayers)
 			{
 				stmt = conn.prepareStatement("INSERT INTO `nba2`.`player` (`player_id`,`name`,`start_year`," +
 						"`end_year`, `active`) VALUES (?,?,?,?,?);");
 			    stmt.setInt(1, player.getPlayerID());
 			    stmt.setString(2, player.getPlayerName());
 			    stmt.setInt(3, player.getPlayerStartYear());
 			    stmt.setInt(4, player.getPlayerEndYear());
 			    stmt.setBoolean(5, player.isPlayerActive());
 			    stmt.executeUpdate();
 			}
 			
 			rs.close();
 			stmt.close();
 			conn.close();
 			
 		} 
 		catch (ClassNotFoundException e) 
 		{
 			e.printStackTrace();
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 		
 	}
 }
