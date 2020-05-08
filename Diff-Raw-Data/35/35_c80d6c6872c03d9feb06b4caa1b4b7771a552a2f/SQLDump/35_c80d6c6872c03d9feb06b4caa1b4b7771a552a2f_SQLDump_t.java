 package net.aparsons.sqldump.db;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
import org.apache.commons.io.FileUtils;

 import au.com.bytecode.opencsv.CSVWriter;
 
 import net.aparsons.sqldump.db.Connectors.Connector;
 
 
 public final class SQLDump implements Runnable {
 
	public static final String VERSION = "0.3";
 	
 	private final Connector driver;
 	private final String url, username, password, sql;
 	
 	private boolean headers = false;
 	private File file;
 	
 
 	public SQLDump(Connector driver, String url, String username, String password, String sql) {
 		this.driver = driver;
 		this.url = url;
 		this.username = username;
 		this.password = password;
 		this.sql = sql;
 	}
 		
 	public void setFile(File file) {
 		this.file = file;
 	}
 
 	public void setHeaders(boolean headers) {
 		this.headers = headers;
 	}
 	
 	@Override
 	public void run() {		
 		// Load Driver
 		try {
 			Connectors.load(driver);
 		} catch (ClassNotFoundException cnfe) {
 			Logger.getLogger(SQLDump.class.getName()).log(Level.SEVERE, "Database driver not found", cnfe);
 			return;
 		}
 		
 		// Establish Connection
 		Connection conn = null;
 		try {
 			Logger.getLogger(SQLDump.class.getName()).log(Level.INFO, "Connecting to database [" + url + "]");
 			conn = DriverManager.getConnection(url, username, password);
 			if (conn != null) {
 				Logger.getLogger(SQLDump.class.getName()).log(Level.INFO, "Successfully connected to database [" + url + "]");
 				
				// Init Files
				if (file == null) {
					file = new File(Long.toString(System.currentTimeMillis()) + ".csv");
				}
				
				if (file.exists()) {
					throw new IOException("File already exists [" + file.getName() + "]");
				}
				
				File tempFile = new File(file + ".tmp");

				if (file.exists()) {
					throw new IOException("File already exists [" + tempFile.getName() + "]");
				}
				
 				// Get statement from the connection
 				Statement stmt = conn.createStatement();
 				
 				// Execute the query
 				Logger.getLogger(SQLDump.class.getName()).log(Level.INFO, "Executing statment [" + sql + "]");
 				ResultSet rs = stmt.executeQuery(sql);
 				
 				// Write to file
 				CSVWriter writer = null;
 				try {
 					writer = new CSVWriter(new FileWriter(tempFile));
 					
 					Logger.getLogger(SQLDump.class.getName()).log(Level.INFO, "Writing to file [" + tempFile + "]");
 					writer.writeAll(rs, headers);
 					
 					Logger.getLogger(SQLDump.class.getName()).log(Level.INFO, "Renaming file [" + tempFile + "] to [" + file + "]");
					FileUtils.moveFile(tempFile, file);
 				} catch (IOException ioe) {
 					Logger.getLogger(SQLDump.class.getName()).log(Level.SEVERE, "Error writing to file", ioe);
 				} finally {
 					if (writer != null) {
 						try {
 							writer.close();
 						} catch (IOException ioe) { }
 					}
 				}
 				
 				// Close result set and statement
 				rs.close();
 				stmt.close();
 			}
 		} catch (SQLException sqle) {
 			Logger.getLogger(SQLDump.class.getName()).log(Level.SEVERE, "Database connection failed", sqle);
		} catch (IOException ioe) {
			Logger.getLogger(SQLDump.class.getName()).log(Level.SEVERE, "IO Error", ioe);
 		} finally {
 			// Close database connection
 			try {
 				Logger.getLogger(SQLDump.class.getName()).log(Level.INFO, "Disconnecting from database [" + url + "]");
 				conn.close(); 
 			} catch (SQLException sqle) { }
 		}
 
 	}
 	
 	@Override
 	public String toString() {
 		return "SQLDump-" + VERSION + " [url=" + url + ", username=" + username + ", sql=" + sql + "]";
 	}
 
 }
