 package it.polito.atlas.alea2.db;
 
 import static it.polito.atlas.alea2.core.Configuration.conf;
 import static it.polito.atlas.alea2.core.ConfigurationProperty.DB_PASSWORD;
 import static it.polito.atlas.alea2.core.ConfigurationProperty.DB_URL;
 import static it.polito.atlas.alea2.core.ConfigurationProperty.DB_USERNAME;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 public class MySqlInstance implements DBInstance {
 
 	@Override
 	public int update(String sql) throws SQLException {
 //		System.out.println(sql);
 		return getStatement().executeUpdate(sql);
 	}
 
 	@Override
 	public int insert(String sql) throws SQLException {
 //		System.out.println(sql);
 		return update(sql);
 	}
 
 	/**
 	 * @return the connection
 	 * @throws SQLException
 	 */
 
 	@Override
 	public Connection getConnection() throws SQLException {
 		return pool().get(0);
 	}
 
 	@Override
 	public Connection getConnection(String url, String user, String password) throws SQLException {
 		return pool().get(0);
 	}
 
 	@Override
 	public Statement getStatement() throws SQLException {
 		return getConnection().createStatement();
 	}
 
 	private List<Connection> pool;
 
 	private List<Connection> pool() throws SQLException {
 		if (pool == null) {
 			return pool(conf(DB_URL), conf(DB_USERNAME), conf(DB_PASSWORD));
 		}
 		return pool;
 	}
 
 	private List<Connection> pool(String url, String user, String password) throws SQLException {
 		if (pool == null) {
 			pool = new ArrayList<Connection>();
 			try {
 				Class.forName("com.mysql.jdbc.Driver").newInstance();
 				pool.add((Connection) DriverManager.getConnection(url, user, password));
				pool.get(0).createStatement().execute("SET sql_mode='NO_BACKSLASH_ESCAPES'");
 			} catch (Exception e) {
 				e.printStackTrace();
 				pool = null;
 			}
 		}
 		return pool;
 	}
 
 	@Override
 	public void dispose() {
 		try {
 			getConnection().close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			// ignore
 		}
 	}
 }
