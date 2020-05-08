 package ch.inftec.ju.db;
 
 import java.io.BufferedReader;
 import java.sql.Clob;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
 import javax.sql.DataSource;
 
 import org.apache.commons.dbutils.DbUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
 import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
 import org.springframework.jdbc.support.DatabaseMetaDataCallback;
 import org.springframework.jdbc.support.JdbcUtils;
 import org.springframework.jdbc.support.MetaDataAccessException;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.transaction.support.TransactionSynchronizationManager;
 
 import ch.inftec.ju.util.IOUtil;
 import ch.inftec.ju.util.JuObjectUtils;
 
 /**
  * DB utility class. Mainly contains wrapper method for Apache commons DbUtils
  * to encapsulate the SQLExceptions and throw JuDbExceptions or to provide
  * log information on failed methods.
  * 
  * @author tgdmemae
  *
  */
 public class JuDbUtils {
 	// TODO: Refactor, remove Apache DbUtils...
 	static Logger log = LoggerFactory.getLogger(JuDbUtils.class);
 
 	@Autowired
 	private DataSource dataSource;	
 	
 	@Autowired
 	private ConnectionInfo connectionInfo;
 	
	private EntityManagerFactory emf;
	
	@Required
	public void setEntityManagerFactory(EntityManagerFactory emf) {
		this.emf = emf;
	}
	
 	/**
 	 * Gets whether a Spring transaction is active in our current context.
 	 * @return True if we are within a Spring transaction, false if we're not.
 	 */
 	public static boolean isSpringTransactionActive() {
 		return TransactionSynchronizationManager.isActualTransactionActive();
 	}
 	
 	/**
 	 * Gets the specified JpaRepository interface.
 	 * @param em Backing EntityManager
 	 * @param repositoryClass Repository interface class
 	 * @return JPA repository interface
 	 */
 	public static <T> T getJpaRepository(EntityManager em, Class<T> repositoryClass) {
 		JpaRepositoryFactory repositoryFactory = new JpaRepositoryFactory(em);
 		return repositoryFactory.getRepository(repositoryClass);
 	}
 
 	/**
 	 * Creates JPA default tables for the current EntityManager.
 	 * <p>
 	 * Only works with EclipseLink currently.
 	 */
 	@Transactional
 	public void createDefaultTables() {
		throw new UnsupportedOperationException("Hibernate refactoring...");
//		EntityManager em = this.emf.createEntityManager();
//		ServerSession s = em.unwrap(ServerSession.class);
//		SchemaManager sm = new SchemaManager(s);
//		sm.createDefaultTables(true);
 	}
 	
 	/**
 	 * Commits and closes a connection.
 	 * @param conn Connection
 	 * @throws JuDbException If the commit and/or close fails
 	 */
 	public static void commitAndClose(Connection conn) throws JuDbException {
 		try {
 			log.debug("Commiting and closing connection [ID=" + JuObjectUtils.getIdentityString(conn) + "]");
 			DbUtils.commitAndClose(conn);
 		} catch (SQLException ex) {
 			throw new JuDbException("Couldn't commit and close connection", ex);
 		}
 	}
 	
 	/**
 	 * Rolls back and closes a connection.
 	 * @param conn Connection
 	 * @throws JuDbException If the rollback and/or close fails
 	 */
 	public static void rollbackAndClose(Connection conn) throws JuDbException {
 		try {
 			log.debug("Rolling back and closing connection [ID=" + JuObjectUtils.getIdentityString(conn) + "]");
 			DbUtils.rollbackAndClose(conn);
 		} catch (SQLException ex) {
 			throw new JuDbException("Couldn't roll back and close connection", ex);
 		}
 	}
 	
 	/**
 	 * Closes the specified connection, catching any SQLException that might be thrown.
 	 * @param conn Connection to be closed
 	 */
 	public static void closeQuietly(Connection conn) {
 		try {
 			log.debug("Closing connection [ID=" + JuObjectUtils.getIdentityString(conn) + "]");
 			DbUtils.close(conn);
 		} catch (SQLException ex) {
 			log.error("Couldn't close connection", ex);
 		}
 	}
 	
 	/**
 	 * Closes the specified result set, catching any SQLException that might be thrown.
 	 * @param rs ResultSet to be closed
 	 */
 	public static void closeQuietly(ResultSet rs) {
 		try {
 			DbUtils.close(rs);
 		} catch (SQLException ex) {
 			log.error("Couldn't close connection", ex);
 		}
 	}
 	
 	/**
 	 * Converts the specified Clob into a String
 	 * @param clob Database Clob
 	 * @return String
 	 * @throws JuDbException If the conversion fails
 	 */
 	public static String getClobString(Clob clob) throws JuDbException {
 		if (clob == null) return null;
 		
 		StringBuilder sb = new StringBuilder();
 		BufferedReader reader = null;
 		
 		try {
 			reader = new BufferedReader(clob.getCharacterStream());
 			String line;
 			while ((line = reader.readLine()) != null) sb.append(line);
 		} catch (Exception ex) {
 			throw new JuDbException("Couldn't convert CLOB to String", ex);
 		} finally {
 			IOUtil.close(reader);
 		}
 		
 		return sb.toString();
 	}
 
 	/**
 	 * Gets a list of all table names of the DB. Table names are all upper case.
 	 * @return List of Table names
 	 * @throws JuDbException If the list cannot be evaluated
 	 */
 	public List<String> getTableNames() throws JuDbException {
 		try {
 			@SuppressWarnings("unchecked")
 			List<String> tableNames = (List<String>) JdbcUtils.extractDatabaseMetaData(dataSource, new DatabaseMetaDataCallback() {
 				@Override
 				public Object processMetaData(DatabaseMetaData dbmd)
 						throws SQLException, MetaDataAccessException {
 					
 					ResultSet rs = dbmd.getTables(connectionInfo.getSchema(), null, null, new String[]{"TABLE"});
 					
 					List<String> tableNames = new ArrayList<>();
 					while (rs.next()) {
 						String tableName = rs.getString("TABLE_NAME").toUpperCase();
 						// We check if the TableName already exists in the list as
 						// Oracle seems to return the same table names multiple times on some
 						// Schemas...
 						if (!tableNames.contains(tableName)) {
 							tableNames.add(tableName);
 						}
 					}
 					rs.close();
 					
 					Collections.sort(tableNames);
 					
 					return tableNames;
 				}
 			});
 			
 			return tableNames;
 		} catch (Exception ex) {
 			throw new JuDbException("Couldn't evaluate table names", ex);
 		}
 	}
 	
 	/**
 	 * Gets the name of the table's primary key column. Multi-Column primary keys are not supported.
 	 * Column names are upper case.
 	 * @param tableName Table name
 	 * @return Name of the table's primary key
 	 * @throws JuDbException If the primary key cannot be evaluated
 	 */
 	public String getPrimaryColumnName(final String tableName) throws JuDbException {
 		try {
 			String columnName = (String) JdbcUtils.extractDatabaseMetaData(dataSource, new DatabaseMetaDataCallback() {
 				@Override
 				public Object processMetaData(DatabaseMetaData dbmd)
 						throws SQLException, MetaDataAccessException {
 					
 					ResultSet rs = dbmd.getPrimaryKeys(null, null, tableName.toUpperCase());
 					
 					String columnName = null;
 					if (rs.next()) columnName = rs.getString("COLUMN_NAME");
 					else throw new JuDbException("Couldn't evaluate primary key for table " + tableName);
 					
 					if (rs.next()) {
 						throw new JuDbException("Driver returned multiple primary keys for table " + tableName);
 					}
 					rs.close();
 					
 					return columnName.toUpperCase();
 				}
 			});
 			
 			return columnName;
 		} catch (Exception ex) {
 			throw new JuDbException("Couldn't evaluate primary column name", ex);
 		}
 	}
 	
 	/**
 	 * Gets all column names of the specified table in the order they are defined, or rather in the
 	 * order the driver returns them. Column names are upper case.
 	 * @param tableName Table name
 	 * @return Column names of the table
 	 * @throws JuDbException If the column names cannot be evaluated
 	 */
 	public List<String> getColumnNames(final String tableName) throws JuDbException {
 		try {
 			@SuppressWarnings("unchecked")
 			List<String> columnNames = (List<String>) JdbcUtils.extractDatabaseMetaData(dataSource, new DatabaseMetaDataCallback() {
 				@Override
 				public Object processMetaData(DatabaseMetaData dbmd)
 						throws SQLException, MetaDataAccessException {
 					
 					ResultSet rs = dbmd.getColumns(null, null, tableName.toUpperCase(), null);
 					
 					List<String> columnNames = new ArrayList<>();
 					while (rs.next()) {
 						String columnName = rs.getString("COLUMN_NAME");
 						columnNames.add(columnName.toUpperCase());
 					}
 					rs.close();
 					
 					if (columnNames.size() == 0) {
 						throw new JuDbException("Couldn't evaluate column names for table " + tableName + ": Driver returned empty ResultSet.");
 					}
 					
 					return columnNames;
 				}
 			});
 			
 			return columnNames;
 		} catch (Exception ex) {
 			throw new JuDbException("Couldn't evaluate primary column name", ex);
 		}
 	}
 
 }
