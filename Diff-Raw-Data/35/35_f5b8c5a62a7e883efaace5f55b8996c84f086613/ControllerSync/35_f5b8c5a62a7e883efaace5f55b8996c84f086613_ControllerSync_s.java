 package csheets.ext.database.controller;
 
 import java.io.FileNotFoundException;
 import java.sql.SQLException;
 import java.util.*;
 
 import csheets.core.Cell;
 import csheets.ext.database.core.*;
 
 /**
  * Creates a new controller for the sync function
  * 
  * @author Andre
  * 
  */
 public class ControllerSync {
 
 	/** list of databases */
 	List<Database> dbList;
 	/** facade patern for the database connection */
 	DatabaseFacade facade;
 
 	/**
 	 * Creates a new controller
 	 */
 	public ControllerSync() {
 		facade = new DatabaseFacade();
 	}
 
 	/**
 	 * Search for avaliables databases
 	 * 
 	 * @return a list with databases
 	 * @throws FileNotFoundException
 	 */
 	public String[][] getDBlist() throws FileNotFoundException {
 		/* new DBCsvReader */
 		DBCsvReader reader = new DBCsvReader();
 		/* instantiating a new arraylist and getting all databases */
 		dbList = new ArrayList<Database>();
 		dbList = reader.getDBList();
 
 		/* String array to store only the name of the databases */
 		String[][] driversName = new String[dbList.size()][2];
 		int i = 0;
 		for (; i < dbList.size(); i++) {
 			driversName[i][0] = dbList.get(i).getName();
 			driversName[i][1] = dbList.get(i).getUrl();
 		}
 		/* returns all names of supported databases */
 		return driversName;
 	}
 
 	/**
 	 * Start the sync between application and database
 	 * 
 	 * @param user
 	 *            username
 	 * @param pass
 	 *            username's password
 	 * @param cells
 	 *            cells to be sync
 	 * @param tableName
 	 *            name of the table
 	 * @param observer
 	 *            the observer object
 	 * 
 	 */
 	public void startSync(String user, String pass, Cell[][] cells,
			String tableName, Observer observer) {
		facade.startSync(user, pass, cells, tableName, observer);
 	}
 
 	/**
 	 * Creates a new connection to the database
 	 * 
 	 * @param url
 	 *            link of the database
 	 * @param user
 	 *            username
 	 * @param pass
 	 *            username's password
 	 * @param dbName
 	 *            database name
 	 */
 	public void connect(String url, String user, String pass, String dbName) {
 		try {
 			facade.createConnection(url, user, pass, dbName);
 		}
 		/* replace below with proper exceptions */
 		catch (SQLException e) {
 		} catch (ClassNotFoundException e) {
 		} catch (Exception e) {
 		}
 	}
 
 }
