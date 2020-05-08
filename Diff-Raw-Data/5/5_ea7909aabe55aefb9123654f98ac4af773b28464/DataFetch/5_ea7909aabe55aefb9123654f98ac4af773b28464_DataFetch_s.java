 /**
  * DataFetch.java
  * 
  */
 package data;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Vector;
 import javax.swing.JOptionPane;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableModel;
 
 import view.GUIEntryPoint;
 
 
 /**
  * This class will be used for the majority of Database IO
  * @author JimiHFord jhf3617
  */
 public class DataFetch {
 
 	/*
 	 * static data
 	 */
 	private static final String driver = "org.postgresql.Driver"; 
 	private static final String url = "jdbc:postgresql://reddwarf.cs.rit.edu/";
 
 	/*
 	 * instance variables
 	 */
 	private Statement stmt;
 	private Connection con;
 	private GUIEntryPoint listener;
 
 	/**
 	 * Singleton Wrapper class 
 	 * @author JimiHFord jhf3617
 	 *
 	 */
 	private static class SingletonWrapper {
 		private static DataFetch INSTANCE = new DataFetch();
 	}
 
 	/**
 	 * Use as an alternative to a constructor. This will ensure singleton
 	 * behavior
 	 * @return	the singleton instance of DataFetch
 	 */
 	public static DataFetch getInstance() {
 		return SingletonWrapper.INSTANCE;
 	}
 
 	/**
 	 * Private constructor ensures no extraneous DataFetchers will be created
 	 */
 	private DataFetch() {
 		listener = null;
 	}
 
 	/**
 	 * Sets the "listener" for error messages to be displayed
 	 * @param listener	the component to be updated with error messages
 	 */
 	public void setListener(GUIEntryPoint listener) {
 		if(this.listener == null) {
 			this.listener = listener;
 		} else {
 			System.err.println("Listener has already been set.");
 		}
 	}
 
 	public DefaultTableModel getMainTrainerTableModel() {
 		boolean error = false;
 		ResultSet rs = null;
 		try {
 			rs = stmt.executeQuery("select * from trainer;");
 		} catch (SQLException e) {
 			error = true;
 			displayError(e.getMessage(), "SQLException");
 		}
 		return error ? new DefaultTableModel() : buildTableModel(rs);
 	}
 	
 	public void removeTrainer(String trainerNumber) {
 		try {
 			stmt.execute("delete from trainer where t_id = " + trainerNumber);
 		} catch (SQLException e) {
 			displayError(e.getMessage(), "SQLException");
 		}
 	}
 	
 	public void addTrainer(String user) {
 		try {
 			stmt.execute("insert into trainer (t_name) values ('" +
 					user + "');");
 		} catch (SQLException e) {
 			displayError(e.getMessage(), "SQLException");
 		}
 	}
 	
 	public DefaultTableModel getTeamPanelModel(String user) {
 		boolean error = false;
 		ResultSet rs = null;
 		try {
 			rs = stmt.executeQuery(
 					"select "+
 			"national_id, english, type1, type2" +
 			" from explicit_party where t_name = '"
 					+ user + "';");
 		} catch (SQLException e) {
 			error = true;
 			System.err.println(e.getMessage());
 			displayError(e.getMessage(), "SQLException");
 		}
 		return error ? new DefaultTableModel() : buildTableModel(rs);
 	}
 	
 	public void connectToRIT(String user, String pass) throws SQLException {
 		this.establishConnection(url, user, pass);
 		this.createStatement();
 	}
 	
 	
 	
 	
 	public ArrayList<ArrayList<Object>> iterate(ResultSet rs) {
 		ArrayList<Object> columnNames = new ArrayList<Object>();
 		ArrayList<ArrayList<Object>> data = new ArrayList<ArrayList<Object>>();
 		try {
 			ResultSetMetaData metaData = rs.getMetaData();
 
 			// names of columns
 
 			int columnCount = metaData.getColumnCount();
 			for (int column = 1; column <= columnCount; column++) {
 				columnNames.add(metaData.getColumnName(column));
 			}
 
 			// data of the table
 
 			while (rs.next()) {
 				ArrayList<Object> vector = new ArrayList<Object>();
 				for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
 					vector.add(rs.getObject(columnIndex));
 				}
 				data.add(vector);
 			}
 		} catch (SQLException e) {
 			System.err.println(e.getMessage());
 			displayError(e.getMessage(), "SQLException");
 		} catch (NullPointerException e) {
 			displayError(e.getMessage(), "SQLException");
 		}
 		return data;
 	}
 	
 	/**
 	 * 
 	 * @param rs
 	 * @return
 	 */
 	public DefaultTableModel buildTableModel(ResultSet rs) {
 		boolean error = false;
 		Vector<String> columnNames = new Vector<String>();
 		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
 		try {
 			ResultSetMetaData metaData = rs.getMetaData();
 
 			// names of columns
 
 			int columnCount = metaData.getColumnCount();
 			for (int column = 1; column <= columnCount; column++) {
 				columnNames.add(metaData.getColumnName(column));
 			}
 
 			// data of the table
 
 			while (rs.next()) {
 				Vector<Object> vector = new Vector<Object>();
 				for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
 					vector.add(rs.getObject(columnIndex));
 				}
 				data.add(vector);
 			}
 		} catch (SQLException e) {
 			error = true;
 			System.err.println(e.getMessage());
 			displayError(e.getMessage(), "SQLException");
 		} catch (NullPointerException e) {
 			error = true;
 		}
 		return error ? new DefaultTableModel() : new DefaultTableModel(data, columnNames);
 	}
 
 	/**
 	 * Displays the error message that occurred
 	 * @param msg 	the message 
 	 * @param title	the title of the error message
 	 */
 	private void displayError(String msg, String title) {
 		if(listener == null) {
 			JOptionPane.showMessageDialog(null, msg, title,
 					JOptionPane.ERROR_MESSAGE, null);
 		} else {
 			listener.showError(msg, title);
 		}
 	}
 
 
 	/**
 	 * 
 	 * @param url	full url to database including database name (not needed though)
 	 * @param user	username of the psql account
 	 * @param pass	that user's password
 	 * @throws SQLException				if something is wrong with the connection
 	 * @throws ClassNotFoundException	if the driver for psql can not be found
 	 */	
 	public void establishConnection(String url, String user, String pass) 
 			throws SQLException {
 		try {
 			Class.forName(driver);
 		} catch (ClassNotFoundException e) {
 			displayError(e.getMessage(), "ClassNotFoundExcpetion");
 		}
 		this.con = DriverManager.getConnection(url + user, user, pass);
 	}
 
 	/**
 	 * Creates a statement for the DataFetch object to execute queries with.
 	 * @throws SQLException
 	 */
 	public void createStatement() throws SQLException {
 		this.stmt = con.createStatement();
 	}
 
 	/**
 	 * @return the statement that manages queries
 	 */
 	public Statement getStatement() {
 		return this.stmt;
 	}
 
 	/**
 	 * Tests the connection of our database
 	 * @throws SQLException
 	 */
 	public void executeAndPrintTestQuery() throws SQLException {
 		ResultSet rs = stmt.executeQuery("select * from pokemon_name;");
 		while(rs.next()) {
 			System.out.println("Pokemon ID: " + rs.getInt("national_id") + '\t' + rs.getString("english"));
 		}
 	}
 
 	/**
 	 * tests queries
 	 * @param args	command line arguments
 	 * 		args[0]	username
 	 * 		args[1] password
 	 */
 	public static void main(String[] args) {
 		String user = "";
 		String pass = "";
 
 		try {
 			if(args.length < 2) {	
 				InputStreamReader inReader = new InputStreamReader(System.in);
 				BufferedReader br = new BufferedReader(inReader);
 				System.out.print("Enter user id: ");
 				user = br.readLine();
 				System.out.print("This is bad, but may I have your password? - ");
 				pass = br.readLine();
 
 			} else {
 				user = args[0];
 				pass = args[1];
 			}
 		} catch(IOException e) {
 			System.err.println(e.getMessage());
 			return;
 		}
 
 		DataFetch df = DataFetch.getInstance();
 		try {
 			df.establishConnection(url, user, pass);
 			df.createStatement();
 			df.executeAndPrintTestQuery();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public TableModel getSearchPokemonModel(String search) {
 		boolean error = false;
 		ResultSet rs = null;
 		try {
 			rs = stmt.executeQuery(
 					"select * from name_both_types where english like '"+
 					search + "%' union "+
 					"select * from name_both_types where type1 like '"+
 					search + "%' union "+
 					"select * from name_both_types where type2 like '"+
					search + "%';"
 					);
 		} catch (SQLException e) {
 			error = true;
 			displayError(e.getMessage(), "SQLError");
 		}
 		return error ? new DefaultTableModel() : buildTableModel(rs);
 	}
 	
 	public TableModel getDefaultPokemonModel() {
 		boolean error = false;
 		ResultSet rs = null;
 		try {
			rs = stmt.executeQuery("select * from name_both_types;");
 		} catch (SQLException e) {
 			error = true;
 			System.err.println(e.getMessage());
 			displayError(e.getMessage(), "SQLException");
 		}
 		return error ? new DefaultTableModel() : buildTableModel(rs);
 	}
 }
