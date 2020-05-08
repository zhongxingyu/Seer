 package de.softwarekollektiv.dbs.queries.complex;
 
 import java.io.PrintStream;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import de.softwarekollektiv.dbs.app.MenuItem;
 import de.softwarekollektiv.dbs.dbcon.DbConnection;
 
 class QueryE implements MenuItem {
 
 	private final PrintStream out;
 	private final DbConnection dbcon;
 
 	QueryE(PrintStream out, DbConnection dbcon) {
 		this.out = out;
 		this.dbcon = dbcon;
 	}
 
 	@Override
 	public String getTitle() {
 		return "Flat customers who paid more than they would have with starter.";
 	}
 
 	@Override
 	public String getDescription() {
 		return "Beantwortet Aufgabe 5.2.a:\nBestimmen Sie alle Kunden mit Preismodell Flat, welche\nbasierend auf ihren bisherigen Ausleihvorgängen im Modell\nStarter billiger weggekommen wären.";
 	}
 
 	
 
 	@Override
 	public boolean run() throws Exception {
 
 		Map<Integer, Double> flatCharges = new HashMap<Integer, Double>();
 		Map<Integer, Double> starterCharges = new HashMap<Integer, Double>();
 		
 		PreparedStatement flatCustomers = dbcon.getConnection()
 				.prepareStatement(
 						"SELECT DISTINCT cus_id FROM rentals WHERE type = 'flat'");
 
 		ResultSet rs = flatCustomers.executeQuery();
 		while (rs.next()) {
 			int cust_id = rs.getInt(1);
 			flatCharges.put(cust_id, totalCharge(cust_id));
 			starterCharges.put(cust_id, starterCharge(cust_id));
 		}
 		rs.close();
 
 		printIt(flatCharges, starterCharges);
 		
 		return true;
 	}
 
 	private void printIt(Map<Integer, Double> flatCharges,
 			Map<Integer, Double> starterCharges) {
 		
 		out.println(" cus_id | charge | starter | better");
 		out.println("--------+--------+---------+-------");		 
 		
 		for (Entry<Integer, Double> entry : flatCharges.entrySet()){
 			double starterCharge = starterCharges.get(entry.getKey());
 			out.println(String.format(" %6d | %6.2f | %7.2f | %s",
 					entry.getKey() , entry.getValue() ,starterCharge , (entry.getValue()>starterCharge)));
 		}
 		out.println("");
 		out.flush();
 	}
 
 	private Double starterCharge(int cust_id) throws SQLException {
 		PreparedStatement custStmt = dbcon.getConnection().prepareStatement(
 				"SELECT price_category, type, duration "+
 				"FROM rentals JOIN movies "+
 				"ON rentals.mov_id = movies.mov_id "+
				"WHERE cus_id = ?;");
 		
 		custStmt.setInt(1, cust_id);
 		ResultSet rs = custStmt.executeQuery();
 		double charge = 0;
 		while (rs.next()){
 			String priceCategory = rs.getString(1);
 			String type = rs.getString(2);
 			int duration = rs.getInt(3);
 			if (type.equals("flat") || type.equals("starter")){
 				if (priceCategory.equals("A")){
 					
 					charge += duration * 1.29;
 				}
 				else {
 					charge +=  duration * 0.79;
 				}
 			}
 			else {
 				if (priceCategory.equals("A")){
 					
 					charge += duration * 0.19;
 				}
 				else {
 					charge +=  duration * 0.15;
 				}		
 			}
 		}
 		
 		return charge;
 	}
 
 	private Double totalCharge(int cust_id) throws SQLException {
 		PreparedStatement totalCharge = dbcon.getConnection().prepareStatement(
				"SELECT * FROM totalcharges(?, -1, -1);");
 		totalCharge.setInt(1, cust_id);
 		ResultSet rs = totalCharge.executeQuery();
 		double result = 0.0;
 		while (rs.next()) {
 			result = rs.getDouble(1);
 		}
 
 		return result;
 	}
 
 }
