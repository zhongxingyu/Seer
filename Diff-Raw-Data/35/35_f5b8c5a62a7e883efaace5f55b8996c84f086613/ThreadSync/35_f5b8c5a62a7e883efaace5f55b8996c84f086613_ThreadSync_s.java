 package csheets.ext.database.core;
 
 import java.util.Observer;
 
 import csheets.core.Cell;
 import csheets.ext.database.controller.ControllerSync;
 
 /**
  * Creates a new thread for the sync function
  * 
  * @author Andre
  * 
  */
 public class ThreadSync implements Runnable {
 	/** cells to be sync */
 	private final Cell[][] cells;
 	/** database details */
 	private final String url, user, pass, tableName, dbName;
 	/** observer object */
 	private final Observer observer;
 
 	/**
 	 * Creates a new thread for sync function
 	 * 
 	 * @param cells
 	 *            cells to be sync
 	 * @param url
 	 *            database url
 	 * @param user
 	 *            username
 	 * @param pass
 	 *            username's password
 	 * @param table
 	 *            table name
 	 * @param dbName
 	 *            database name
 	 * @param observer
 	 *            the observer object
 	 */
 	public ThreadSync(Cell[][] cells, String url, String user, String pass,
 			String table, String dbName, Observer observer) {
 		this.cells = cells;
 		this.url = url;
 		this.user = user;
 		this.pass = pass;
 		this.tableName = table;
 		this.dbName = dbName;
 		this.observer = observer;
 	}
 
 	@Override
 	public void run() {
 		ControllerSync sync = new ControllerSync();
 		sync.connect(url, user, pass, dbName);
		sync.startSync(user, pass, cells, tableName, observer);
 	}
 
 }
