 package csheets.ext.database.core;
 
 import csheets.core.Cell;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 /**
  * Database connection interface
  * (applying the adapter pattern)
  * @author João Carreira
  */
 public interface DBConnectionStrategy 
 {
     /**
      * creates a connection to a database driver
      * @param url path to driver
      * @param user username
      * @param pass password
      */
     public void createConnection(String url, String user, String pass) throws ClassNotFoundException, SQLException;
     
     /**
      * creates a new table in the database
      * @param tableName table's name
      * @param cells cells to be added to the table
      */
     public void createTable(String tableName, Cell [][]cells);
     
     /**
      * closes connection from the database
      */
     public void disconnet();
     
     /**
      * saves the result of a SQL expression into an arraylist 
      * @param str SQL expression
      * @return arraylist with statement objects
      */
    public ArrayList queryToArray(String str);
     
     /**
      * returns all the tables availabe int he database
      * @return array with table list
      */
     public String[] getTableList(ArrayList list);
     
     /**
      * returns the data of a given table
      */
     public void getTableContent();
     
     /**
      * updates a given table
      */
     public void updateTable();
 }
