 package bzb.se.update;
 
 import java.io.*;
 import java.sql.*;
 import java.util.*;
 import java.net.URL;
 
 
 /*
  *	Gets data from Darryn's MySQL database and dumps it to a local XML file
  */
 
 public class DumpData {
 
 	public static void main(String[] args) {
 
 		if (args[0].equals("0")) {
 			System.out.println("Getting data from MySQL DB");
 			grabByMySQL();
 		} else if (args[0].equals("1")) {
 			System.out.println("Getting data from DB via PHP");
 			grabByPHP();
 		}
 	}
 
 	public static void grabByMySQL() {
 
         Connection conn = null;
         Statement stmt = null;
         ResultSet rs = null;
 
         try {
             String dbURL = "jdbc:mysql://mysql.mitussis.net:3306/mitussis_earth";
             String username = "bedwell";
             String password = "growlingflythrough";
 
             Class.forName("com.mysql.jdbc.Driver");
 
 	System.out.println("Connecting to database");
 
             conn =
                 DriverManager.getConnection(dbURL, username, password);
 
 	System.out.println("Connected");
 
             stmt = conn.createStatement();
 
 	System.out.println("Reading database structure");
 
 		ArrayList columns = new ArrayList();
 
             if (stmt.execute("select column_name,data_type from information_schema.columns where table_name like 'bedwell_flythrough';")) {
                 rs = stmt.getResultSet();
             } else {
                 System.err.println("select failed");
             }
 
             while (rs.next()) {
 		String columnName = rs.getString("column_name");
 		columns.add(columnName);
 		System.out.println("Found column " + columnName);
             }
 
 		Collections.sort(columns);
 
 		stmt = conn.createStatement();
 
 	System.out.println("Reading data");
 
             if (stmt.execute("select * from bedwell_flythrough;")) {
                 rs = stmt.getResultSet();
             } else {
                 System.err.println("select failed");
             }
 
 		ArrayList records = new ArrayList();
 
             while (rs.next()) {
 		Record r = new Record();
 		Iterator j = columns.iterator();
 		while (j.hasNext()) {
 			String columnName = (String) j.next();
 			String data = rs.getString(columnName);
 			if (data != null) {
 				r.addData(columnName, rs.getString(columnName));
 			}
 		}
 		records.add(r);
             }
 
 		Iterator i = records.iterator();
 		String xml = "<?xml version=\"1.0\"?>\n<markers>\n";
 		while (i.hasNext()) {
 			Record r = (Record) i.next();
 			xml += "\t<marker>\n";
 			Set thisColumns = r.getColumns();
 			Iterator k = thisColumns.iterator();
 			while (k.hasNext()) {
 				String columnName = (String) k.next();
 				columnName = "marker" + columnName.substring(0,0).toUpperCase() + columnName.substring(1);
 				try {
 					xml += "\t\t<" + columnName + ">" + r.getData(columnName) + "</" + columnName + ">\n";
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			xml += "\t</marker>\n";
 		}
 		xml += "</records>\n";
 
 		writeToFile(xml);
 
         } catch (ClassNotFoundException ex) {
             System.err.println("Failed to load mysql driver");
             System.err.println(ex);
         } catch (SQLException ex) {
             System.out.println("SQLException: " + ex.getMessage()); 
             System.out.println("SQLState: " + ex.getSQLState()); 
             System.out.println("VendorError: " + ex.getErrorCode()); 
         } finally {
             if (rs != null) {
                 try {
                     rs.close();
                 } catch (SQLException ex) { /* ignore */ }
                 rs = null;
             }
             if (stmt != null) {
                 try {
                     stmt.close();
                 } catch (SQLException ex) { /* ignore */ }
                 stmt = null;
             }
             if (conn != null) {
                 try {
                     conn.close();
                 } catch (SQLException ex) { /* ignore */ }
                 conn = null;
             }
         }
     }
 
 	public static void grabByPHP () {
 		try {
 			BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("http://mitussis.net/earth/china/xml.php").openStream()));
 			String line = reader.readLine();
 			String xml = line;
 			while (line != null) {
 				line = reader.readLine();
 				if (line != null) {
 					xml += line + "\n";
 				}
 			}
 			writeToFile(xml);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void writeToFile (String xml) {
 		try {
 			System.out.println("Writing data to file");
			BufferedWriter out = new BufferedWriter(new FileWriter("markers.xml"));
 			out.write(xml);
 			out.close();
 			System.out.println("Written");
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 	}
 }
