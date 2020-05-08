 import java.io.*;
 import java.util.Scanner;
 import java.util.NoSuchElementException;
 import java.sql.*;
 import java.util.zip.GZIPInputStream;
 import java.util.regex.*;
 
 /**
  * Apache-login parsija
  */
 public class Splitter {
 
     private static final String start = "INSERT weblog (server,service,ip,date,request,response,bytes,referer,browser) values ";
 
     private static final String filenameRegex =
	"^.*/(.*)/(.*)\\.\\d{4}-\\d{2}-\\d{2}\\.gz";
     
     public static void main(String args[]) throws Exception {
 	
 	Connection conn =
 	    DriverManager.getConnection("jdbc:mysql://130.234.169.15/ixonos?" +
 					"user=joell&password=hohfah3I");
 
 	Statement stmt = conn.createStatement();
 	SQLBuilder sqlstr = new SQLBuilder(start);
 	Pattern filenamePattern = Pattern.compile(filenameRegex);
 
 	for (String filename: args) {
	    System.out.println("Käsitellään tiedostoa "+filename + "...");
 
 	    Matcher matcher = filenamePattern.matcher(filename);
 	    if (!matcher.matches() || matcher.groupCount() != 2) {
 		throw new Exception("Filename pattern is not clear. Must be hostname/service.year-month-day.gz");
 	    }
 	    
 	    String server = matcher.group(1);
 	    String service = matcher.group(2);
 
 	    InputStream in =new GZIPInputStream(new FileInputStream(filename));
 	    Scanner scanner = new Scanner(in, "UTF-8");
 	    int linenum = 1;
 	    String line = "";
 	    
 	    try {
 		while (true) {
 		    line = scanner.nextLine();
 		    LogLine entry = new LogLine(server,service,line);
 
 		    sqlstr.addElement(entry);
 
 		    if ((linenum % 100) == 0) {
 			stmt.executeUpdate(sqlstr.toString());
 			sqlstr.clear();
 		    }
 		    
 		    linenum++;
 		}
 	    } catch (NoSuchElementException foo) {
 		// Tiedosto kaiketi loppu, kaikki ok.
 	    } catch (Exception e) {
 		System.err.println("Error at: "+filename+":"+linenum);
 		System.err.println("Content: "+line);
 		throw e;
 	    } finally {
 		scanner.close();
 
 		// One more time
 		stmt.executeUpdate(sqlstr.toString());
 		sqlstr.clear();
 
 	    }
 	}
     }
 
     private static ResultSet getEmptyResult(Statement stmt)
 	throws java.sql.SQLException {
 	
 	ResultSet koe = stmt.executeQuery("select * from weblog limit 0;");
 	koe.moveToInsertRow();
 	return koe;
     }
 }
