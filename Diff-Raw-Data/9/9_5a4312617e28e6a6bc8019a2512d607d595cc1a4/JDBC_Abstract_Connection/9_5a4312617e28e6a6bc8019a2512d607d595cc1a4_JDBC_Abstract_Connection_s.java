 package utilities;
 
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 import objects.DatabaseTable;
 import objects.QueryResult;
 import edu.gatech.sqltutor.Utils;
 import edu.gatech.sqltutor.entities.UserQuery;
 
 
 public abstract class JDBC_Abstract_Connection {
 	protected final String DB_NAME_SYSTEM = "sqltutor";
 	protected final String DB_NAME_SCHEMAS = "sqltutorschemas";
 	protected final String DB_MANAGER_USERNAME = "DB_Manager";
 	protected final String DB_READONLY_USERNAME = "readonly_user";
 	protected final String DB_PASSWORD = "SQLTutor!!!";
 	
 	/*
 	 * This method gets passed to the child, where it must be implemented with their connection string/driver.
 	 */
 	protected abstract Connection getConnection(String databaseName);
 	
 	/*
 	 * This method gets passed to the child, where it must be implemented with their connection string/driver.
 	 */
 	protected abstract Connection getConnection(String databaseName, String databaseUsername);
 	
 	public void saveUserQuery(UserQuery query) {
 		Connection conn = null;
 		PreparedStatement statement = null;
 		
 		try {
 			conn = getConnection(DB_NAME_SYSTEM);
 			
 			Long id = query.getId();
 			if( id == null ) {
 				final String INSERT = "INSERT INTO query " + 
 					"(username, schema, sql, user_description, source, created) VALUES (?, ?, ?, ?, ?, ?)";
 				statement = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
 			} else {
 				final String UPDATE = 
 						"UPDATE query SET username=?, schema=?, sql=?, user_description=?, source=?, created=? WHERE id=?";
 				statement = conn.prepareStatement(UPDATE);
 				statement.setLong(7, id);
 			}
 			statement.setString(1, query.getUsername());
 			statement.setString(2, query.getSchema());
 			statement.setString(3, query.getQuery());
 			statement.setString(4, query.getUserDescription());
 			statement.setString(5, query.getSource());
 			statement.setDate(6, new Date(query.getTime().getTime()));
 
 			
 			statement.executeUpdate();
 			
 			if( id == null ) {
 				// get the generated id for inserts
 				ResultSet keySet = statement.getGeneratedKeys();
 				if( !keySet.next() )
 					throw new IllegalStateException("No id generated for query.");
 				query.setId(keySet.getLong(1));
 			}
 		} catch( Exception e ) {
 			if( e instanceof RuntimeException )
 				throw (RuntimeException)e;
 			throw new RuntimeException(e); // FIXME exception type
 		} finally {
 			Utils.tryClose(statement);
 			Utils.tryClose(conn);
 		}
 	}
 	
 	public List<UserQuery> getUserQueries() {
 		Connection conn = null;
 		Statement statement = null;
 		
 		try {
 			conn = getConnection(DB_NAME_SYSTEM);
 			
 			final String SELECT = 
 					"SELECT * FROM query";
 			statement = conn.createStatement();
 			ResultSet result = statement.executeQuery(SELECT);
 			
 			List<UserQuery> userQueries = new ArrayList<UserQuery>();
 			while( result.next() ) {
 				UserQuery query = new UserQuery();
 				query.setId(result.getLong("id"));
 				// FIXME the bean?
 				query.setUsername(result.getString("username"));
 				query.setSchema(result.getString("schema"));
 				query.setQuery(result.getString("sql"));
 				query.setUserDescription(result.getString("user_description"));
 				query.setTime(result.getDate("created"));
 				query.setSource(result.getString("source"));
 				
 				userQueries.add(query);
 			}
 			return userQueries;
 			
 		} catch( Exception e ) {
 			throw new RuntimeException(e); // FIXME exception type
 		} finally {
 			Utils.tryClose(statement);
 			Utils.tryClose(conn);
 		}
 	}
 	
 	public List<UserQuery> getUserQueries(String schemaName) {
 		Connection conn = null;
 		Statement statement = null;
 		
 		try {
 			conn = getConnection(DB_NAME_SYSTEM);
 			
 			final String SELECT = 
 					"SELECT * FROM query WHERE schema='" + schemaName + "'";
 			statement = conn.createStatement();
 			ResultSet result = statement.executeQuery(SELECT);
 			
 			List<UserQuery> userQueries = new ArrayList<UserQuery>();
 			while( result.next() ) {
 				UserQuery query = new UserQuery();
 				query.setId(result.getLong("id"));
 				// FIXME the bean?
 				query.setUsername(result.getString("username"));
 				query.setSchema(result.getString("schema"));
 				query.setQuery(result.getString("sql"));
 				query.setUserDescription(result.getString("user_description"));
 				query.setTime(result.getDate("created"));
 				query.setSource(result.getString("source"));
 				
 				userQueries.add(query);
 			}
 			return userQueries;
 			
 		} catch( Exception e ) {
 			throw new RuntimeException(e); // FIXME exception type
 		} finally {
 			Utils.tryClose(statement);
 			Utils.tryClose(conn);
 		}
 	}
 	
 	/*
 	 * 
 	 */
 	public void log(String userQuery, String schemaName, String username) {
 		
 		Connection connection = null;
 		PreparedStatement preparedStatement = null;
 		
 		try {
 			
 			connection = getConnection(DB_NAME_SYSTEM);
 			final String update = "INSERT INTO \"log\" (\"query\", \"schema\", \"user\") VALUES (?, ?, ?)";
 			preparedStatement = connection.prepareStatement(update);
 			preparedStatement.setString(1, userQuery);
 			preparedStatement.setString(2, schemaName);
 			preparedStatement.setString(3, username);
 			preparedStatement.executeUpdate();
 			 
 		} catch (Exception e) {
 			
 			System.err.println("Exception: " + e.getMessage());
 			
 		} finally {
 			Utils.tryClose(preparedStatement);
 			Utils.tryClose(connection);
 		}
 	}
 	
 	/*
 	 * 
 	 */
 	public boolean isPasswordCorrect(String username, String attemptedPassword) {
 
 		Connection connection = null;
 		PreparedStatement preparedStatement = null;
 		ResultSet resultSet = null;
 		
 		try {
 			connection = getConnection(DB_NAME_SYSTEM);
 			
 			// get the user's encryption salt
 			String query = "SELECT \"salt\", \"password\" FROM \"user\" WHERE \"username\" = ?";
 			
 			preparedStatement = connection.prepareStatement(query);
 			preparedStatement.setString(1, username);
 			resultSet = preparedStatement.executeQuery();
 			
 			if (resultSet.next()) {
 				byte[] salt = resultSet.getBytes(1);
 				byte[] encryptedPassword = resultSet.getBytes(2);
 				
 				// use the password hasher to authenticate
 				return PasswordHasher.authenticate(attemptedPassword, encryptedPassword, salt);
 			} 
 			 
 		} catch (Exception e) {
 			
 			System.err.println("Exception: " + e.getMessage());
 			
 		} finally {
 			Utils.tryClose(preparedStatement);
 			Utils.tryClose(connection);
 			Utils.tryClose(resultSet);
 		}
 		return false;
 	}
 	
 	/*
 	 * 
 	 */
 	public boolean isUsernameRegistered(String username) {
 		
 		Connection connection = null;
 		PreparedStatement preparedStatement = null;
 		ResultSet resultSet = null;
 
 		try {
 			connection = getConnection(DB_NAME_SYSTEM);
 			final String query = "SELECT 1 FROM \"user\" WHERE \"username\" = ?";
 			
 			preparedStatement = connection.prepareStatement(query);
 			preparedStatement.setString(1, username);
 			resultSet = preparedStatement.executeQuery();
 
 			if (resultSet.next()) {
 				return true;
 			}
 			
 		} catch (Exception e) {
 			
 			System.err.println("Exception: " + e.getMessage());
 			
 		} finally {
 			Utils.tryClose(preparedStatement);
 			Utils.tryClose(connection);
 			Utils.tryClose(resultSet);
 		}
 		return false;
 	}
 	
 	/*
 	 * 
 	 */
 	public void registerUser(String username, String password) {
 		
 		Connection connection = null;
 		PreparedStatement preparedStatement = null;
 		
 		try {
 			
 			connection = getConnection(DB_NAME_SYSTEM);
 			
 			final String update = "INSERT INTO \"user\" (\"username\", \"password\", \"salt\") VALUES (?, ?, ?)";
 			
 			preparedStatement = connection.prepareStatement(update);
 			
 			// generate the user's encryption salt and password
 			byte[] salt = PasswordHasher.generateSalt();
 			byte[] encryptedPassword = PasswordHasher.getEncryptedPassword(password, salt);
 			
 			preparedStatement.setString(1, username);
 			preparedStatement.setBytes(2, encryptedPassword);
 			preparedStatement.setBytes(3, salt);
 			
 			preparedStatement.executeUpdate();
 
 		} catch (Exception e) {
 			
 			System.err.println("Exception: " + e.getMessage());
 			
 		} finally {
 			Utils.tryClose(preparedStatement);
 			Utils.tryClose(connection);
 		}
 	}
 	
 	public List<DatabaseTable> getTables(String schemaName) {
 		Connection connection = null;
 		ResultSet resultSet = null;
 		
 		try {
 			connection = getConnection(DB_NAME_SCHEMAS, DB_READONLY_USERNAME);
 
 			DatabaseMetaData metadata = connection.getMetaData();
 			resultSet = metadata.getTables(null, schemaName, "%", new String[] {"TABLE"});
 			ArrayList<DatabaseTable> tables = new ArrayList<DatabaseTable>();
 			while(resultSet.next()) {
 				// the API tells us the third element is the TABLE_NAME string.
 				tables.add(new DatabaseTable(resultSet.getString(3)));
 			}
 			for(int i=0; i < tables.size(); i++) {
				resultSet = metadata.getColumns(null, null, tables.get(i).getTableName(), null);
 				ArrayList<String> columns = new ArrayList<String>();
 				while(resultSet.next()) {
 					columns.add(resultSet.getString(4));
 				}
 				tables.get(i).setColumns(columns);
 			}
 			
 			return tables;
 		} catch (Exception e) {
 			
 			System.err.println("Exception: " + e.getMessage());
 			
 		} finally {
 			Utils.tryClose(resultSet);
 			Utils.tryClose(connection);
 		}
 		return null;
 	}
 	
 	public QueryResult getQueryResult(String schemaName, String query) throws SQLException {
 		Connection connection = getConnection(DB_NAME_SCHEMAS, DB_READONLY_USERNAME);
 		Statement statement = connection.createStatement();
 		statement.execute("SET CURRENT_SCHEMA=" + schemaName);
 		ResultSet resultSet = statement.executeQuery(query);
 		ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
 	
 		int columnCount = resultSetMetaData.getColumnCount();
 		ArrayList<List<String>> queryData = new ArrayList<List<String>>();
 		ArrayList<String> columnNames = new ArrayList<String>();
 		for(int i = 1; i <=  columnCount; i++) {
 			columnNames.add(resultSetMetaData.getColumnName(i));
 		}
 		while(resultSet.next()) {
 			ArrayList<String> rowData = new ArrayList<String>();
 			for (int i = 1; i <= columnCount; i++) {
 				rowData.add(resultSet.getString(i));
 			}
 			queryData.add(rowData);
 		}
 		// return the query result object
 		QueryResult queryResult = new QueryResult(columnNames, queryData);
 		Utils.tryClose(resultSet);
 		Utils.tryClose(connection);
 		Utils.tryClose(statement);
 		return queryResult;
 	}
 	
 	public void verifyQuery(String schemaName, String query) throws SQLException {
 		Connection connection = getConnection(DB_NAME_SCHEMAS, DB_READONLY_USERNAME);
 		Statement statement = connection.createStatement();
 		statement.execute("SET CURRENT_SCHEMA=" + schemaName);
 		statement.executeQuery(query);
 		Utils.tryClose(connection);
 		Utils.tryClose(statement);
 	}
 }
