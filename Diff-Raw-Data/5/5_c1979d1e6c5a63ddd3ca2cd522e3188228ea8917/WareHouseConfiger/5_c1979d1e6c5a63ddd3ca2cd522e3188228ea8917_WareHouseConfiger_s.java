 package data_access;
 
 
 import what.sp_parser.DataEntry;
 //import what.sp_config.ConfigWrap;
 
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 
 
 public class WareHouseConfiger {
 	
 	static private CreateDBForWH db;
 	//private ConfigWrap config;
 	
 	public WareHouseConfiger() {
 		
 		db = new CreateDBForWH("olapwhat", "root", "password");
 		
 	}
 	
 	
 	
 	
 	
 	public boolean buildWareHouse(DataEntry loadMe) {
 	
 		 int time_id = 0;
 	     int location_id = 0;
 	     int db_id = 0 ;
 		 
 		try {		
 
 				this.db.insertINTable(
 						(int) loadMe.getInfo(0), (int) loadMe.getInfo(1), (int) loadMe.getInfo(2),
 						
 						(int) loadMe.getInfo(3), (int) loadMe.getInfo(4), (int) loadMe.getInfo(5),
 						
 						(String) loadMe.getInfo(6), (String) loadMe.getInfo(7), (String) loadMe.getInfo(8),
 						
 						(float) loadMe.getInfo(9), (float) loadMe.getInfo(10), (int) loadMe.getInfo(11), 
 						
 						(String) loadMe.getInfo(13), (String) loadMe.getInfo(14)
 						);
 				
 			   
 		
 	    String query_id = "SELECT time_id FROM time_dim;";
 		ResultSet set = createStatement(query_id);
 		
 		try {
 			while (set.next()) {
 				time_id = new Integer(set.getString("time_id").toString());
 			}	
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		query_id = "SELECT location_id FROM location_dim;";
 	    set = createStatement(query_id);
 		
 		try {
 			while (set.next()) {
 				location_id = new Integer(set.getString("location_id").toString());
 			}	
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	    query_id = "SELECT db_id FROM db_dim;";
 	    set = createStatement(query_id);
 		
 		try {
 			while (set.next()) {
 				db_id = new Integer(set.getString("db_id").toString());
 			}	
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
         this.db.insertIDs(time_id, location_id, db_id);
 		
 		
 		try {
 			this.db.getConnection().close();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return true;
 	 } catch (Exception e) {
 		 return false;
 	 }
 		
 	}
 	
 	
 	
 	private ResultSet createStatement(String query) {
 		try {
 			 Statement stmt_id = (Statement) this.db.getConnection().createStatement();
 			 stmt_id.close();
 			 this.db.getConnection().close();
 			return stmt_id.executeQuery(query);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			return null;
 		}
 
 	}
         
         
        public void closeConnectionMySQL() {
         	
        	this.db.getConnection.close();
         	
         }
 	 
 
 }
