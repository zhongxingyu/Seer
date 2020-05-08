 package persistency;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import exceptions.DeleteNonExistingException;
 import exceptions.UpdateNonExistingException;
 
 import utils.Query;
 
 public class DatabaseConnection {
 
 	private Connection conn;
 	private Statement stmt;
 
 	public DatabaseConnection(String dbFile) {
 		try {
 			Class.forName("org.sqlite.JDBC");
 
 			this.conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
 			this.stmt = this.conn.createStatement();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public int create(String table, String[] columns, String[] values) {
 		int id = -1;
 		try {
 			this.execUpdate(Query.insertInto(table, columns, values));
 
 			// Get the id of the inserted row
 			ResultSet rs = this.execQuery(Query.select("last_insert_rowid()"));
 			if (rs.next()) {
 				id = rs.getInt("last_insert_rowid()");
 			}
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return id;
 	}
 	
 	
 	public ResultSet execQuery(Query query) {
 		return this.execQuery(query.end());
 	}
 
 	private ResultSet execQuery(String query) {
 		//System.out.println(query);
 		try {
 			return this.stmt.executeQuery(query);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public int execUpdate(String query)  {
 		//System.out.println(query);
 		try {
 			this.stmt.executeUpdate(query);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return -1;
 	}
 
 	public int execUpdate(Query query)  {
 		return this.execUpdate(query.end());
 	}
 	 
 	public ResultSet readWhereEquals(String table, String key, String value){
 		return this.execQuery(Query.selectAllFrom(table).whereEquals(key, value));
 	}
 	 
 	public ResultSet readWhereEquals(String table, String key, int value){
 		return this.execQuery(Query.selectAllFrom(table).whereEquals(key, value));
 	}
 
 	public ResultSet readByID(String table, int id)  {
 		return this.readWhereEquals(table, "id", id);
 	}
 
 	public ResultSet readAll(String table)  {
 		return this.execQuery(Query.selectAllFrom(table));
 	}
 
 	public ResultSet readAllWhere(String table, String key, String value) {
 		return this.execQuery(Query.selectAllFrom(table).whereEquals(key, value));
 	}
 	
 	public boolean update(String table, int id, String column, String value) {
 		if (!this.exists(table, id)) return false;
 		this.execUpdate(Query.update(table, column, value).whereEquals("id", id));
 		return true;
 	}
 	
 	public boolean update(String table, int id, String[] columns, String[] values) 
 			throws UpdateNonExistingException {
 		if (!this.exists(table, id)) throw new UpdateNonExistingException();
 		this.execUpdate(Query.update(table, columns, values).whereEquals("id", id));
 		return true;
 	}
 	
 	public boolean delete(String table, int id) throws DeleteNonExistingException {
 		if (!this.exists(table, id)) throw new DeleteNonExistingException();
 		this.execUpdate(Query.deleteFrom(table).whereEquals("id", id));
 		return true;
 	}
 	
 	public int count(String table) {
 		ResultSet rs = this.execQuery(Query.count(table));
 		int count = 0;
 		try {
 			while (rs.next()) {
 				count = rs.getInt(1);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return count;
 	}
 	
 	public boolean exists(String table, int id) {
 		ResultSet rs = this.execQuery(Query.exists(table, id));
		boolean exists = false;
 		try {
 			while (rs.next()) {
				exists = rs.getBoolean(1);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
		return exists;
 	}
 }
