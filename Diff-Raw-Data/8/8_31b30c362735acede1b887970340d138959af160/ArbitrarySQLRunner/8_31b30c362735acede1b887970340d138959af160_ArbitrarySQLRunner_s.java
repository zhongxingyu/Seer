 package com.nearme;
 
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Scanner;
 
 import javax.sql.DataSource;
 
 /**
  * I got to the point in my tests where I was testing we're writing things in and out of a
  * database correctly, and needed (a) a quick way to reset the database to a known good
  * state, and (b) a means to run arbitrary SQL against it quickly and check things like
  * row counts. This class provides a quick means to do that sort of thing.
  * 
  * @author twhume
  *
  */
 
 public class ArbitrarySQLRunner {
 
 	private DataSource dataSource = null;
 	
 	public ArbitrarySQLRunner(DataSource d) {
 		this.dataSource = d;
 	}
 	
 	/**
 	 * Read a file full of SQL from an InputStream, and run it.
 	 * 
 	 * @param is
 	 */
 	
 	public void runSQL(InputStream is) throws SQLException {
 		Scanner scanner = new Scanner(is).useDelimiter(";");
 		Connection conn = dataSource.getConnection();
 		while (scanner.hasNext()) {
 			String sql = scanner.next();
 			PreparedStatement pst = conn.prepareStatement(sql);
 			pst.executeUpdate();
 			pst.close();
 		}
 		conn.close();
 	}
 	
 	/**
 	 * Runs the supplied SQL and returns the integer returned.
 	 * Obviously, the SQL must be set up to return an integer... (!)
 	 * 
 	 * @param sql
 	 * @return
 	 * @throws SQLException
 	 */
 	
 	public int runQuery(String sql) throws SQLException {
 		int i = -1;
 		Connection conn = dataSource.getConnection();
 		PreparedStatement pst = conn.prepareStatement(sql);
 		ResultSet rs = pst.executeQuery();
		if (rs.next()) i = rs.getInt(0);
 		rs.close();
 		pst.close();
 		conn.close();
 		return i;
 	}
 	
 }
