 package se.kayarr.ircbot.database;
 
import java.sql.Connection;
import java.sql.ResultSet;
 import java.sql.SQLException;
import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import lombok.Getter;
 
 import com.google.common.base.Joiner;
 
 public class Table {
 	private static final String ID_COLUMN = "ID INT NOT NULL AUTO_INCREMENT PRIMARY KEY";
 	
 	@Getter private Database owner;
 	
 	@Getter private String name;
 	@Getter private int version;
 	
 	private TableHandler handler;
 	
 	Table(Database owner, String name, int version, TableHandler handler) {
 		this.owner = owner;
 		this.name = name;
 		this.version = version;
 		this.handler = handler;
 	}
 	
 	void initialize() {
 		try {
 			
 			if(handler != null) {
 				if(!owner.tableExists(name)) {
 					//Create table
 					System.out.println("Table '" + name + "' does not exist, calling onTableCreate...");
 					handler.onTableCreate(this);
 				}
 				else {
 					int oldVersion = owner.tableVersion(name);
 					
 					if(oldVersion < version) {
 						//Upgrade table
 						System.out.println("Table '" + name + "' needs an upgrade, calling onTableUpgrade...");
 						handler.onTableUpgrade(this, oldVersion, version);
 					}
 				}
 				
 				System.out.println("Table '" + name + "' is ready to go!");
 
 			}
 			
 		}
 		catch(SQLException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public void createIfNonexistant(String... columns) throws SQLException {
 		List<String> columnsList = new ArrayList<>( Arrays.asList(columns) );
 		columnsList.add(0, ID_COLUMN); //Add specs for ID column
 		
 		String columnsString = Joiner.on(", ").join(columnsList);
 		
 		owner.sql("CREATE TABLE IF NOT EXISTS " + name + " (" + columnsString + ")");
 	}
 	
 	public void select(String[] columns, String whereCond) throws SQLException {
 		//TODO Have this class return some object that contains a list of key-value mappings
 		
 //		Connection conn = owner.getConn();
 //		String columnsStr = Joiner.on(",").join(columns);
 //		
 //		Statement stmt = conn.createStatement();
 //		ResultSet rs = stmt.executeQuery("SELECT " + columnsStr + " FROM " + name + (whereCond == null ? "" : " WHERE " + whereCond));
 	};
 	
 	public void insert(String[] columns, Object[] values) {
 		//TODO Return a builder for inserting maybe
 	}
 	
 	public void update(String column, Object value, String whereCond) {
 		//TODO Return a builder for updating maybe
 	}
 	
 	public void delete(String whereCond) {
 		//TODO Implement this
 	}
 }
