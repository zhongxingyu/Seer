 package main.model;
 
 import main.util.Log;
 
 import java.sql.*;
 import java.util.Map;
 
 /**
  * The Model in the booking-system. There can only be one instance of the model which
  * is accessed through the <pre>getInstance()</pre> method.
  */
 public class MySQLModel extends Model {
 	
 	/**
 	 * The SQL connection to the database.
 	 */
 	private Connection connection;
 	
 	/**
 	 * The SQL statement from the database connection.
 	 */
 	private Statement statement;
 	
 	/**
 	 * The concrete instance of the MySQLModel.
 	 * Implemented as initialization on demand Bill Pugh suggested, 
 	 * mentioned on <a href="en.wikipedia.org/wiki/Singleton_pattern">wikipedia</a>.
 	 */
 	private static class MySQLModelHolder {
 		public static final MySQLModel instance = new MySQLModel();
 	}
 	
 	/**
 	 * Constructs a model and connects to a database.
 	 */
 	private MySQLModel() {
 		try {
 			// Retrieve the database driver
 			Class.forName("com.mysql.jdbc.Driver").newInstance();
 			
 			// Initialize connection with database, user name and password
 			connection = DriverManager.getConnection("jdbc:mysql://mysql.itu.dk/jegp_bgpp", "jegp_bgpp", "ldo8tf6o");
 			
 			// Retrieve statement
 			statement = connection.createStatement();
 			
 			// Log the success
 			Log.info("Successfully connected to MySQL database at itu.dk");
 		} catch (Exception e) {
 			// Print the error in case the connection failed
 			Log.error("Cannot connect to database: " + e);
 		}
 	}
 	
 	/**
 	 * Builds a select query from a table with an optional condition.
 	 * @param table  The table to query
 	 * @param condition  The condition the query must meet.
 	 * @param selector  The fields to select.
 	 * @param join  An optional join statement.
 	 * @return String  The sql statement.
 	 */
 	private String buildSelectQuery(String table, String condition, String selector, String join) {
 		String sql = "SELECT " + selector + " FROM " + table;
 		
 		// Add join
 		if (join.length() > 0)
 			sql += " JOIN " + join;
 		
 		// Add condition
 		if (condition.length() > 0)
 			sql += " WHERE " + condition;
 		
 		return sql;
 	}
 	
 	/**
 	 * Builds a select query from a table map with keys associated to their valuesj.
 	 * @param table  The table to query.
 	 * @param fields  The map with keys associated with a given value, tested by the logic equals.
 	 * @param selector  The fields to select.
 	 * @param join  An optional join statement.
 	 * @return String  The sql statement.
 	 */
 	private String buildSelectQuery(String table, Map<String, String> fields, String selector, String join) {
 		// Build the condition for the query
 		String condition = "";
 		
 		// Iterate through the fields to add the conditions.
 		int n = 1;
 		for (Map.Entry<String, String> e : fields.entrySet()) {
 			condition += e.getKey() + " = '" + e.getValue() + "' ";
 			if (n < fields.size())
 				condition += "AND ";
 			n++;
 		}
 		
 		// Return the statement.
 		return buildSelectQuery(table, condition, selector, join);
 	}
 
 	public boolean delete(String table, int id) {
 		String sql = "DELETE from " + table + " WHERE id = '" + id + "'";
 		
 		// Execute
 		try {
 			statement.executeUpdate(sql);
 			
 			// Return true
 			return true;
 		} catch (SQLException e) {
 			Log.error("Unable to delete entity from table " + table + " with id " + id + ": " + e);
 			// Return failure
 			return false;
 		}
 	}
 
 	/**
 	 * Executes a given query and returns the ResultSet.
 	 * @param sql  The query to perform.
 	 * @return  Returns the result. Can be null.
 	 */
 	private ResultSet executeAndReturnResult(String sql) {
 		try {
 			statement.execute(sql);
 			return statement.getResultSet();
 		} catch (SQLException e) {
 			Log.error("Unable to perform query " + sql + ": " + e);
 			// Return failure.
 			return null;
 		}
 	}
 
 	public ResultSet get(String table, String condition) {
 		// Build the query
 		String sql = buildSelectQuery(table, condition, "*", "");
 
 		// Return the result
 		return executeAndReturnResult(sql);
 	}
 	
 	public ResultSet get(String table, Map<String, String> fields) {
 		// Build the query
 		String sql = buildSelectQuery(table, fields, "*", "");
 
 		// Return the result
 		return executeAndReturnResult(sql);
 	}
 	
 	public ResultSet get(String table, String condition, String joinTable, String joinKey1, String joinKey2) {
 		// Build the query
		String sql = buildSelectQuery(table, condition, "*", joinTable + " ON " + joinKey1 + " = " + joinKey2);
 
 		// Return the result
 		return executeAndReturnResult(sql);
 	}
 	
 	public ResultSet get(String table, int id) {
 		// Build the query
 		String sql = "SELECT * from " + table + " WHERE id = '" + id + "'";
 
 		// Return the result
 		return executeAndReturnResult(sql);
 	}
 	
 	/**
 	 * Retrieves an instance of the model.
 	 * @return An instance of the Model.
 	 */
 	public static MySQLModel getInstance() {
 		return MySQLModelHolder.instance;
 	}
 
 	public int save(String table, Map<String, String> fields) {
 		// Check that the entity exists.
 		String sql = buildSelectQuery(table, fields, "id", "");
 		
 		// Execute the query and examine the result.
 		try {
 			statement.execute(sql);
 			ResultSet res = statement.getResultSet();
 			if (res != null && res.next()) {
 				int id = res.getInt(1);
 				if (id != 0) return id;
 			}
 		} catch (SQLException e){
 			Log.error("Unable to perform select query from " + table + ": " + e);
 		}
 		
 		// If nothing exists in the database then store the information.
 		sql = "INSERT INTO " + table + " ";
 		String columns = "(";
 		String values  = " VALUES (";
 		
 		int n = 1;
 		for (Map.Entry<String, String> e : fields.entrySet()) {
 			columns += e.getKey();
 			values += "'" + e.getValue() + "'";
 			if (n < fields.size()) {
 				columns += ", ";
 				values  += ", ";
 			}
 			n++;
 		}
 		
 		// Finish the query
 		columns += ")"; 
 		values += ")";
 		sql += columns + " " + values;
 		
 		// Execute insert statement.
 		try {
 			statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
 			
 			// Retrieve the latest key and return it
 			ResultSet res = statement.getGeneratedKeys();
 			if (res != null && res.next()) {
 				return res.getInt(1);
 			}
 		} catch (SQLException e) {
 			Log.error("Unable to insert entity in " + table + ": " + e);
 		}
 		
 		// If nothing has been returned so far we're unsuccessful. Return 0.
 		return 0;
 	}
 
 	public ResultSet update(String table, int id, Map<String, String> fields) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 }
