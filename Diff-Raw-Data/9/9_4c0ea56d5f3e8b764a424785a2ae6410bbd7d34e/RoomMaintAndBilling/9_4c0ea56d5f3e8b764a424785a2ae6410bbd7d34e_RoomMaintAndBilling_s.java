 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import com.mysql.jdbc.Connection;
 
 public class RoomMaintAndBilling {
 	public static boolean printBillsForDepartures(Connection conn) throws ParseException {
 		try {
 			// get booking and room details for departing guest on current date
 			String sql = "SELECT b.hotelID, b.roomNo, b.guestID, b.startDate, b.endDate, r.price " +
 						 "FROM booking b " +
 						 "    INNER JOIN Room r ON r.hotelID = b.hotelID AND r.roomNo = b.roomNo " +
 						 "WHERE endDate = CURRENT_DATE";
 			PreparedStatement ps = conn.prepareStatement(sql);			
 			ResultSet rs = ps.executeQuery();
 			
 			// display all the bills of departing guests
 			System.out.println("\nBilling Log");
 			System.out.println("-----------");
 			
 			while(rs.next()){
 				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); 
 				sdf.setLenient(false);
 				
 				// get number of days
 				Date startDate = sdf.parse(rs.getString(4));
 				Date endDate = sdf.parse(rs.getString(5));
 				int numDays = (int)( (endDate.getTime() - startDate.getTime() ) / (1000 * 60 * 60 * 24));
 			
 				// get total price
 				double totalPrice = Double.parseDouble(rs.getString(6)) * numDays;
 				
 				// print departures				
 				System.out.println("\nHotel ID: " + rs.getString(1) +
 								   "\nGuest ID: " + rs.getString(3) +
 								   "\nRoom number: " + rs.getString(2) +
								   "\nTotal number of days: " + numDays +
								   "\nTotal price: " + totalPrice 
								  ); 
				
 				// Save billing details in billing log
 				ps = conn.prepareStatement(
 						"SELECT billingID FROM BillingLog ORDER BY billingID DESC LIMIT 1");
 				ResultSet rs2 = ps.executeQuery();
 
 				int lastBillingID = 1; // if there are no billings logged in the table
 				// if there are billings logged in the table,
 				if(rs2.next()) {
 					// Increment
 					lastBillingID = Integer.parseInt(rs2.getString(1)) + 1;
 				} 
 				
 				// Insert into billing table
 				Statement s = conn.createStatement();
 				s.executeUpdate("INSERT INTO BillingLog (billingID, hotelID, guestID, roomNo, totalDays, totalPrice) " +
 						        "VALUES ( '" + formatID(lastBillingID) + "'," +
 						        "         '" + formatID(Integer.parseInt(rs.getString(1))) + "'," + 
 						        "         '" + formatID(Integer.parseInt(rs.getString(3))) + "'," +
 						        "         '" + formatID(Integer.parseInt(rs.getString(2))) + "'," + 
 						        "          " + numDays + ", " +
 						        "          " + totalPrice + ")");
 			}
 
 			return true;
 		} catch (Exception e) {
 			return false;
 		}
 	}
 	
 	public static boolean displayArrivalsAndDepartures(Connection conn) {
 		try {
 			// get all arrivals
 			String sql = "SELECT hotelID, roomNo, guestID, startDate, endDate FROM booking " +
 						 "WHERE startDate = CURRENT_DATE";
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ResultSet rs = ps.executeQuery();
 			
 			System.out.println("Arrivals:");
 			while(rs.next()){
 				System.out.println("\nHotel ID: " + rs.getString(1) +
 								   "\nRoom Number: " + rs.getString(2) +
 								   "\nGuest ID: " + rs.getString(3) +
 								   "\nStart Date: " + rs.getString(4) +
 								   "\nEnd Date: " + rs.getString(5)
 								  ); 
 			}
 			System.out.println("Done.");
 		} catch (SQLException e) {
 			return false;
 		}
 	
 		try {			
 			// get all departures
 			String sql = "SELECT hotelID, roomNo, guestID, startDate, endDate FROM booking " +
 						 "WHERE endDate = CURRENT_DATE";
 			PreparedStatement ps = conn.prepareStatement(sql);
 			ResultSet rs = ps.executeQuery();
 			
 			System.out.println("\nDepartures:");
 			while(rs.next()){
 				System.out.println("\nHotel ID: " + rs.getString(1) +
 								   "\nRoom Number: " + rs.getString(2) +
 								   "\nGuest ID: " + rs.getString(3) +
 								   "\nStart Date: " + rs.getString(4) +
 								   "\nEnd Date: " + rs.getString(5)
 								  ); 
 			}
 			
 			System.out.println("Done");
 		} catch (SQLException e) {
 			return false;
 		}
 		
 		return true;
 	}
 	
 // ----------------------------------- Format
 	public static String formatID(int id) {
 		String idStr = "";
 		
 		int first = (int)(id/1000)*1000;
 		int second = (int)((id - first)/100)*100;
 		int third = (int)((id - first - second)/10)*10;
 		int fourth = (int)(id - first - second - third);
 		
 		if ((int)(id/1000) == 0) {
 			idStr = idStr + "0";
 		} else {
 			idStr = idStr + (id/1000);
 		}
 		
 		if ((int)((id - first)/100) == 0) {
 			idStr = idStr + "0";
 		} else {
 			idStr = idStr + (int)((id - first)/100);
 		}
 		
 		if ((int)((id - first - second)/10) == 0) {
 			idStr = idStr + "0";
 		} else {
 			idStr = idStr + (int)((id - first - second)/10);
 		}
 		
 		if ((int)(id - first - second - third) == 0) {
 			idStr = idStr + "0";
 		} else {
 			idStr = idStr + (int)(id - first - second - third);
 		}
 		
 		return idStr;
 	}	
 }
