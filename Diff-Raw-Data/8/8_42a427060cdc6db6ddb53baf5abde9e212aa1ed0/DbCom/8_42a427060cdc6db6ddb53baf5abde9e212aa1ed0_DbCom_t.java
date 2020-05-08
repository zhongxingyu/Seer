 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package carrental;
 import java.sql.*;
 import java.util.ArrayList;
 
 /**
  * Communicator object for controller and database
  * @author CNN
  * @version 2011-12-02
  */
 public class DbCom {
     private Connection conn;
     private static Statement stm;
     
     public DbCom(){
     	openConnection();
     }
     
     /**
      * Get the first element matching the query string as an array of strings.
      * The array can easily be inserted into an object of the fitting type.
      * @param query string to match
      * @return entry matching string
      */
     public ArrayList<String> getFirstMatch(String query) {
         ArrayList<String> result = new ArrayList<>();
         try {
             if(newStatement().execute(query)) {
                 int columns = stm.getResultSet().getMetaData().getColumnCount();
                 stm.getResultSet().next();
                 for(int i = 0; i < columns; i++) {
                     result.add(stm.getResultSet().getString(i + 1));
                 }
             }
             else CarRental.getInstance().appendLog("Could not get matches, query execution failed.");
         }
         catch (SQLException e) {
             CarRental.getInstance().appendLog("Error while getting matches from database.",e);
         }
         return result;
     }
     
     /**
      * Get an array of all the matches of the query string as string arrays.
      * Each arraylist of strings represents and entry in the table.
      * @param query string to match
      * @return entries matching string
      */
     public ArrayList<ArrayList<String>> getMatches(String query) {
         ArrayList<ArrayList<String>> results = new ArrayList<>();
         ArrayList<String> record;
         try {
             if(newStatement().execute(query)) {
                ResultSet rs = stm.getResultSet();
                int columns = rs.getMetaData().getColumnCount();
                String table = rs.getMetaData().getTableName(1);
                
                while(rs.next()) {
                    record = new ArrayList<>();
                    
                    for(int i = 0; i < columns; i++) {
                        if(!table.equals(rs.getMetaData().getTableName(i + 1))) {
                            table = rs.getMetaData().getTableName(i + 1);
                            results.add(record);
                            record = new ArrayList<>();
                        }
                        record.add(rs.getString(i + 1));
                    }
                    results.add(record);
                }
                return results;
             }
             else CarRental.getInstance().appendLog("Could not get matches, query execution failed.");
         }
         catch (SQLException e) {
             CarRental.getInstance().appendLog("Error while getting matches from database.",e);
         }
         return null;
     }
     
     /**
      * Save the object to the database. If the object exists (id found) the
      * entry is updated, if not it is inserted.
      * @param table the table to save to
      * @param object the collumns to save, ordered as the collumns in the table
      */
     public void saveArray(String table, ArrayList<String> object) {
         //find types
         try {
             if(newStatement().execute("SELECT * FROM "+table+" LIMIT 1")) {
                 ResultSetMetaData meta = stm.getResultSet().getMetaData();
                 if(object.size() <= meta.getColumnCount()) {
                     boolean exists = false;
                     newStatement().execute("SELECT * FROM "+table+" WHERE id='"+object.get(0)+"'");
                     String query = "";
                     if(stm.getResultSet().next()) {
                         exists = true;
                         query = "UPDATE "+table+" SET ";
                     }
                     else {
                         query = "INSERT INTO "+table+" VALUES (";
                     }
                     for(int i = 0; i < meta.getColumnCount(); i++) {
                         String name = meta.getColumnName(i + 1);
                         String value = object.get(i);
                         if(exists) {
                             if(i != 0) query += ", ";
                             query += name+"='"+value+"'";
                         }
                         else {
                             if(i != 0) query += ", ";
                             query += "'"+value+"'";
                         }
                     }
                     if(exists) {
                         query += " WHERE id='"+object.get(0)+"'";
                     }
                     else query += ")";
                     newStatement().execute(query);
                     CarRental.getInstance().appendLog("Saved entry #"+object.get(0)+" to table "+table+" in database");
                 }
                 else CarRental.getInstance().appendLog("Object given is larger than table size in database");
             }
             else CarRental.getInstance().appendLog("Failed to get collumn names from database");
         }
         catch (SQLException e) {
             CarRental.getInstance().appendLog("Failed to save an object to database",e);
         }
     }
     
     public void deleteMatch(String table, String where) {
         try {
            newStatement().executeUpdate("DELETE FROM "+table+" WHERE "+where);
            CarRental.getInstance().appendLog("Succesfully deleted from "+table+" rows matching "+where+".");
         }
         catch (SQLException e) {
             CarRental.getInstance().appendLog("Failed to delete from database table "+table+".",e);
         }
     }
     
     /**
      * Gets the highest id from a given table. Useful when wanting to add a new
      * object.
      * @param table the table to find the highest id in
      * @return the highest id in table
      */
     public int getHighestId(String table) {
         try {
             if(newStatement().execute("SELECT * FROM "+table+" ORDER BY id DESC LIMIT 1")) {
                 if(stm.getResultSet().next()) return stm.getResultSet().getInt(1);
                 else CarRental.getInstance().appendLog("Tried to get highest id from "+table+", but it has no entries");
             }
         }
         catch (SQLException e) {
             CarRental.getInstance().appendLog("Failed to get highest id from table "+table,e);
         }
         return 0;
     }
     
     /**
      * Creates a new statement, leaving the previous behind.
      * @return 
      */
     private Statement newStatement() {
         try {
             stm = conn.createStatement();
         } catch (SQLException e) {
             CarRental.getInstance().appendLog("Failed to create a new statement.",e);
         }
         return stm;
     }
     
    
     
     /**
      * Connect to the database.
      */
     private void openConnection() {
         try {
             // Fra mysql-connector-java-5.1.5-bin.jar, lagt i /program files/java/jdk1.7.0_01/jre/lib/ext/
             Class.forName("com.mysql.jdbc.Driver");
             conn = DriverManager.getConnection("jdbc:mysql://localhost/carrental", "carrental", "");
             CarRental.getInstance().appendLog("Connected to database.");
         }
         catch (SQLException e) {
             CarRental.getInstance().appendLog("Could not open database connection.",e);
         }
         catch (ClassNotFoundException e) {
             CarRental.getInstance().appendLog("Cannot find database",e);
         }
     }
     
     /**
      * Close the connection to the database
      */
     public void closeConnection(){
         try {
             if(conn != null) conn.close();   
         }
         catch (SQLException e){ 
              CarRental.getInstance().appendLog("Kan ikke lukke databaseforbindelsen",e);
         }
     }
 }
