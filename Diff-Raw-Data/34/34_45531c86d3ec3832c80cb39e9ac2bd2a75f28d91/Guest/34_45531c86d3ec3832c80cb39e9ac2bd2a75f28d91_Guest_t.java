 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import com.mysql.jdbc.Connection;
 
 public class Guest {
 	public static void add(Connection conn, String name, String address) {
 		try {
 			// get the last guest ID from the table
 			PreparedStatement ps = conn.prepareStatement(
 					"SELECT guestID FROM guest ORDER BY guestID DESC LIMIT 1");
 			ResultSet rs = ps.executeQuery();
 
 			int lastGuestID = 1; // if there are no guests in the table
 			// if there are guests in the table,
 			if(rs.next()) {
 				// Increment
 				lastGuestID = Integer.parseInt(rs.getString(1)) + 1;
 			} 
 			
 			// Insert guest into table
 			Statement s = conn.createStatement();
 			s.executeUpdate("INSERT INTO guest (guestID, guestAddress, guestName) " +
 					        "VALUES (" + lastGuestID + ", '" + address + "', '" + name + "')");	
 		} catch (SQLException e) {
 			System.out.println("Error: Could not add guest.");
 			e.printStackTrace();
 			return;
 		}
 		System.out.println("Successfully added guest.");
 	}
 	
 	public static void update(Connection conn, String id, String name, String address){
 		try {			
 			Statement s = conn.createStatement();
 			s.executeUpdate("UPDATE guest SET " +
 							"guestName = '" + name + "', " +
 					        "guestAddress = '" + address + "' " +
 					  		"WHERE guestID = " + id);	
 		} catch (SQLException e) {
 			System.out.println("Error: Could not update guest.");
 			e.printStackTrace();
 			return;
 		}
 		System.out.println("Successfully updated guest.");
 	}
 	
 	public static void delete(Connection conn, String id){
 		try {			
 			Statement s = conn.createStatement();
 			s.executeUpdate("DELETE FROM guest " +
 					  		"WHERE guestID = " + id);	
 		} catch (SQLException e) {
 			System.out.println("Error: Could not delete guest.");
 			e.printStackTrace();
 			return;
 		}
 		System.out.println("Successfully deleted guest.");
 	}	
	
	public static boolean isNameValid(String name) {
		// make sure name is not empty or more than 30 characters
		if (name.trim().length() == 0 || name.trim().length() > 30) {
			return false;
		}
		return true;
	}
	
	public static boolean isGuestIdValid(Connection conn, String id) {
		try {
			// make sure id is an integer
			int guestID = Integer.parseInt(id);
			
			// make sure guest id is a 4 digit number
			if (guestID / 10000 > 1) {
				return false;
			}
						
			// get the last guest ID from the table
			PreparedStatement ps = conn.prepareStatement(
					"SELECT * FROM guest WHERE guestID = " + guestID);
			ResultSet rs = ps.executeQuery();
			
			// if id exists in database,
			if (rs.next()) {
				return true;
			} else { // if id does not exist in database,
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}
 }
