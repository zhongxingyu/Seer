 import java.sql.*;
 import java.io.*;
 import java.util.*;
 

 // A simple JDBC client that works with MySQL. With some tiny changes,
 // this could work with any system that supports JDBC.
 //
 // Jesper Larsson, jesper.larsson@mah.se
 //
 // 2014-10-28: Deployed in teaching.
 // 2014-10-31: Left justify string types.
 
 /** A simple JDBC client that works with MySQL. */
 public class MySQLClient {
     /** Prompts the user for something, with an optional default. */
     static String prompt(String what, String dflt) {
         if (dflt == null) {
             return System.console().readLine("%s: ", what).trim();
         } else {
             String s = System.console().readLine("%s [%s]: ", what, dflt).trim();
             return s.length() > 0 ? s : dflt;
         }
     }
 
     /** Runs the client. Arguments are host, user,  database, and
       * password, in that order. If less than four arguments are
       * given, the user is prompted for what is missing. */
     public static void main(String[] args) {
         Console cons = System.console();
 
         String defaultHost = "195.178.235.60";
 
         String host = args.length > 0 ? args[0] : prompt("Host", "195.178.235.60");
         String user = args.length > 1 ? args[1] : prompt("User", null);
         String database = args.length > 2 ? args[2] : prompt("Database", user);
         String password = args.length > 3 ? args[3] : new String(System.console().readPassword("Password: "));
         
         try {
             Class.forName("com.mysql.jdbc.Driver").newInstance();
         } catch (ReflectiveOperationException ex) {
             System.err.println("Can't load JDBC driver, " + ex);
             System.exit(1);
         }
     
         try {
             Statement stmt;
             try {
                 Connection conn = DriverManager
                     .getConnection("jdbc:mysql://" + host + "/" + database + "?user=" + user + "&password=" + password);
                 stmt = conn.createStatement();
             } catch (SQLException ex) {
                 System.err.println("Terrible! Problem with connection: " + ex);
                 System.exit(1);
                 throw new IllegalStateException("Exit returned");
             }
             while (true) {
                 try {
                     if (!promptEval(stmt, "sql> ")) { break; }
                 } catch (SQLException ex) {
                     String className = ex.getClass().getName();
                     System.err.println(className.substring(className.lastIndexOf('.')+1) + ": " + ex.getMessage());
                 }
             }
         } catch (IOException ex) {
             System.err.println("Terrible! I/O problem: " + ex);
             System.exit(1);
         }
     }
     
     static boolean promptEval(Statement stmt, String prompt) throws SQLException, IOException {
         Console cons = System.console();
         String s = cons.readLine("%s", prompt);
         if (s == null) { return false; }
         s = s.trim();
         if (s.length() == 0) { return true; }
         long startTime = System.currentTimeMillis();
         boolean itsARs = stmt.execute(s);
         if (itsARs) {
             ResultSet rs = stmt.getResultSet();
             ResultSetMetaData md = rs.getMetaData();
             int cols = md.getColumnCount();
             
             int[] widths = new int[cols];
             StringBuilder headB = new StringBuilder(), sepB = new StringBuilder(), dataB = new StringBuilder();
             String[] leftJust = new String[cols];
             for (int i = 0; i < cols; i++) {
                 widths[i] = md.getColumnName(i+1).length();
                 int colType = md.getColumnType(i+1);
                 leftJust[i] = colType == Types.CHAR || colType == Types.VARCHAR || colType == Types.LONGVARCHAR ? "-" : ""; 
             }
             List<Object[]> batch = getBatch(rs, cols);
             for (Object[] row : batch) {
                 for (int i = 0; i < cols; i++) {
                     widths[i] = Math.max(widths[i], row[i] == null ? 4 : row[i].toString().length() + 2);
                 }
             }
             for (int i = 0; i < cols; i++) {
                 headB.append("| %-").append(Integer.toString(widths[i])).append("s ");
                 dataB.append("| %").append(leftJust[i]).append(Integer.toString(widths[i])).append("s ");
                 sepB.append("+");
                 for (int j = 0; j < widths[i]+2; j++) { sepB.append("-"); }
             }
             String headFmt = headB.append("|\n").toString();
             String sepFmt = sepB.append("+\n").toString();
             String dataFmt = dataB.append("|\n").toString();
 
             cons.format(sepFmt);
             Object[] colNames = new String[cols];
             for (int i = 0; i < cols; i++) { colNames[i] = md.getColumnName(i+1); }
             cons.format(headFmt, colNames);
             cons.format(sepFmt);
             
             int ct = 0;
             while (batch.size() > 0) {
                 for (Object[] row : batch) { cons.format(dataFmt, row); }
                 ct += batch.size();
                 batch = getBatch(rs, cols);
             }
             cons.format(sepFmt);
             cons.format("%d row%s", ct, ct == 1 ? "" : "s");
         } else {
             int ct = stmt.getUpdateCount();
             if (ct >= 0) cons.format("%d row%s affected", ct, ct == 1 ? "" : "s");
             else cons.format("ok");
         }
         long time = System.currentTimeMillis() - startTime;
         cons.format(" (%.2f seconds)\n", time / 1000.0);
         cons.flush();
         return true;
     }
 
     private static List<Object[]> getBatch(ResultSet rs, int cols) throws SQLException {
         ArrayList<Object[]> batch = new ArrayList<Object[]>();
         for (int j = 0; j < 100 && rs.next(); j++) {
             Object[] row = new String[cols];
             for (int i = 0; i < cols; i++) { row[i] = rs.getString(i+1); }
             batch.add(row);
         }
         return batch;
     }
 }
