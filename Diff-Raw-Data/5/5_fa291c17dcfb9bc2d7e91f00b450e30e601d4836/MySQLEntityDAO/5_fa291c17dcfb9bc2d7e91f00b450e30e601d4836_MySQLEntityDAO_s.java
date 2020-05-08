 package uk.ac.warwick.dcs.boss.model.dao.impl;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Vector;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import uk.ac.warwick.dcs.boss.model.dao.DAOException;
 import uk.ac.warwick.dcs.boss.model.dao.IEntityDAO;
 import uk.ac.warwick.dcs.boss.model.dao.beans.Entity;
 
 /**
  * This DAO uses subclassing for specific operations
  * @author davidbyard
  *
  */
 public abstract class MySQLEntityDAO<E extends Entity> implements IEntityDAO<E> {
 	
 	protected Connection connection;
 		
 	public MySQLEntityDAO(Connection connection) throws DAOException {
 		this.setConnection(connection);
 	}
 	
 //	public MySQLEntityDAO() {
 //		connection = null;
 //	}
 	
 	public Long createPersistentCopy(E entity)
 			throws DAOException {		
 		// Build the dynamic parts of the query.	
 		StringBuffer namesBuffer = new StringBuffer("id");
 		StringBuffer valuesBuffer = new StringBuffer("?");
 		
 		for (String s : getDatabaseFieldNames()) {
 			namesBuffer.append(",");
 			namesBuffer.append(s);
 			
 			valuesBuffer.append(",?");
 		}
 		
 		// Begin the transaction
 		try {
 			// Construct the statement.
 			String statementString = "INSERT INTO " + getTableName()
 				+ " (" + namesBuffer + ")"
 				+ " VALUES (" + valuesBuffer + ")";
 			PreparedStatement statementObject = getConnection().prepareStatement(statementString);
 			
 			statementObject.setObject(1, null);
 			
 			int i = 2;
 			for ( Object o : getDatabaseValues(entity) ) {
 				statementObject.setObject(i++, o);
 			}
 			
 			// Execute the statement.
 			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
 			statementObject.execute();
 			statementObject.close();
 			
 			// Done.
 			return retrieveLastInsertIdentifier(connection);
 		} catch (SQLException e) {
 			throw new DAOException("SQL error", e);
 		}
 	}
 
 	public void deletePersistentEntity(Long databaseIdentifier)
 			throws DAOException {
 		// Begin the transaction
 		try {
 			// Construct the statement.
 			String statementString = "DELETE FROM " + getTableName()
 				+ " WHERE id=?";
 			PreparedStatement statementObject = getConnection().prepareStatement(statementString);
 			statementObject.setLong(1, databaseIdentifier);
 			
 			// Execute the statement.
 			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
 			statementObject.execute();		
 			statementObject.close();
 		} catch (SQLException e) {
 			throw new DAOException("SQL error", e);
 		}
 		
 	}
 
 	public Collection<E> retrieveAllPersistentEntities() throws DAOException {
 //		Build the dynamic parts of the query.
 		StringBuffer namesBuffer = new StringBuffer("id");
 
 		for (String s : getDatabaseFieldNames()) {
 			namesBuffer.append(",");
 			namesBuffer.append(s);
 		}
 
 //		Begin the transaction
 		try {
 			// Construct the statement.
 			String statementString = "SELECT " + namesBuffer
 			+ " FROM " + getTableName()
 			+ " ORDER BY " + getTableName() + "." + getMySQLSortingString();
 			PreparedStatement statementObject = getConnection().prepareStatement(statementString);
 
 			// Execute the statement.
 			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
 			ResultSet rs = statementObject.executeQuery();
 			Vector<E> result = new Vector<E>();
 			while (rs.next()) {
 				E e = createInstanceFromDatabaseValues(getTableName(), rs);				
 				e.setId(rs.getLong("id"));
 				result.add(e);
 			}
 			
 			rs.close();
 			statementObject.close();
 
 			// Done
 			return result;
 		} catch (SQLException e) {
 			throw new DAOException("SQL error", e);
 		}
 	}
 	
 	public Collection<E> findPersistentEntitiesByExample(E entity)
 			throws DAOException {		
 		return performSelectForFind(entity, false);
 	}
 
 	public Collection<E> findPersistentEntitiesByWildcards(E entity)
 	throws DAOException {
 		return performSelectForFind(entity, true);
 	}
 	
 	public E retrievePersistentEntity(Long databaseIdentifier)
 			throws DAOException {
 		// Build list of names
 		StringBuffer namesBuffer = new StringBuffer("id");
 			
 		for (String s : getDatabaseFieldNames()) {
 			namesBuffer.append(",");
 			namesBuffer.append(s);	
 		}
 		
 		// Begin the transaction
 		try {
 			// Construct the statement.
 			String statementString = "SELECT " + namesBuffer
 				+ " FROM " + getTableName()
 				+ " WHERE id=?";
 			PreparedStatement statementObject = getConnection().prepareStatement(statementString);
 			
 			statementObject.setLong(1, databaseIdentifier);
 			
 			// Execute the statement.
 			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
 			ResultSet rs = statementObject.executeQuery();
 			
 			// Bundle the results into a thingy
 			if (!rs.first()) {
 				throw new DAOException("record " + databaseIdentifier + " does not exist");
 			}
 
 			E e = createInstanceFromDatabaseValues(getTableName(), rs);			
 			e.setId(rs.getLong("id"));
 
 			rs.close();
 			statementObject.close();
 			
 			// Done	
 			return e;
 		} catch (SQLException e) {
 			throw new DAOException("SQL error", e);
 		}
 
 	}
 
 	public Collection<E> retrievePersistentEntities(Collection<Long> databaseIdentifiers)
 	throws DAOException {
 //		Build list of names
 		StringBuffer namesBuffer = new StringBuffer("id");
 
 		for (String s : getDatabaseFieldNames()) {
 			namesBuffer.append(",");
 			namesBuffer.append(s);	
 		}
 
 //		Build list of ids
 		StringBuffer idsBuffer = new StringBuffer();
 
 		for (Long id : databaseIdentifiers) {
 			idsBuffer.append(id);
 			idsBuffer.append(", ");
 		}
 		idsBuffer.setLength(idsBuffer.length() - 2);
 		
 //		Begin the transaction
 		try {
 			// Construct the statement.
 			String statementString = "SELECT " + namesBuffer
 			+ " FROM " + getTableName()
 			+ " WHERE id IN (" + idsBuffer + ")"
 			+ " ORDER BY " + getMySQLSortingString();
 			PreparedStatement statementObject = getConnection().prepareStatement(statementString);
 
 			// Execute the statement.
 			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
 			ResultSet rs = statementObject.executeQuery();
 
 			// Bundle the results into a thingy
 			Collection<E> result = new LinkedList<E>();
 			while (rs.next()) {
 				E e = createInstanceFromDatabaseValues(getTableName(), rs);			
 				e.setId(rs.getLong("id"));
 				result.add(e);
 			}
 
 			rs.close();
 			statementObject.close();
 			
 			// Done	
 			return result;
 		} catch (SQLException e) {
 			throw new DAOException("SQL error", e);
 		}
 
 	}
 
 	
 	public void updatePersistentEntity(E entity)
 			throws DAOException {
 		if (entity.getId() == null) {
 			throw new DAOException("Entity has no database identifier!");
 		}
 		
 		// Build the dynamic parts of the query.	
 		StringBuffer valuesBuffer = new StringBuffer();	
 		
 		for (String s : getDatabaseFieldNames()) {
 			valuesBuffer.append(s);
 			valuesBuffer.append("=?,");
 		}
 		
 		// Remove the trailing commas.
 		valuesBuffer.setLength(valuesBuffer.length() - 1);
 
 		// Begin the transaction
 		try {
 			// Construct the statement.
 			String statementString = "UPDATE " + getTableName() 
 				+ " SET " + valuesBuffer
 				+ " WHERE id=?";
 			PreparedStatement statementObject = getConnection().prepareStatement(statementString);
 			
 			int i = 1;
 			for (Object value : getDatabaseValues(entity)) {
 				statementObject.setObject(i++, value);
 			}
 			
 			statementObject.setLong(i++, entity.getId());
 			
 			// Execute the statement.
 			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
 			statementObject.executeUpdate();
 			
 			// Clear
 			statementObject.close();
 		} catch (SQLException e) {
 			throw new DAOException("SQL error", e);
 		}		
 	}
 
 	private Long retrieveLastInsertIdentifier(Connection sqlConnection) throws SQLException {
 		Logger.getLogger("mysql").log(Level.TRACE, "Executing: SELECT LAST_INSERT_ID()");
 		ResultSet rs = sqlConnection.createStatement().executeQuery("SELECT LAST_INSERT_ID() AS last_insert_identifier");
 		rs.first();
 		Long result = rs.getLong("last_insert_identifier");
 		rs.close();
 		return result;
 	}
 
 	protected Collection<E> fetchEntitiesFromJoin(String joinTable, String key, Object value, String joinTableJoinKey, String localTableJoinKey)
 		throws DAOException {
 //		Build list of names
 		StringBuffer namesBuffer = new StringBuffer(getTableName() + ".id");
 
 		for (String s : getDatabaseFieldNames()) {
 			namesBuffer.append(",");
 			namesBuffer.append(getTableName());
 			namesBuffer.append(".");
 			namesBuffer.append(s);
 			namesBuffer.append(" AS ");
 			namesBuffer.append(s);
 		}
 		
 		
 //		Begin the transaction
 		try {
 			// Construct the statement.
 			String statementString = "SELECT " + namesBuffer
 				+ " FROM " + getTableName() + "," + joinTable
 				+ " WHERE " + joinTable + "." + key + "=?"
 				+ " AND " + getTableName() + "." + localTableJoinKey + " = " + joinTable + "." + joinTableJoinKey
 				+ " ORDER BY " + getTableName() + "." + getMySQLSortingString();
 			PreparedStatement statementObject = getConnection().prepareStatement(statementString);
 			statementObject.setObject(1, value);
 
 			// Execute the statement.
 			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
 			ResultSet rs = statementObject.executeQuery();
 
 			// Bundle the results into a thingy
 			LinkedList<E> result = new LinkedList<E>();
 
 			while (rs.next()) {
 				E e = createInstanceFromDatabaseValues(getTableName(), rs);				
 				e.setId(rs.getLong("id"));
 				result.add(e);				
 			}
 
 			rs.close();
 			statementObject.close();
 			
 			// Done		
 			return result;
 		} catch (SQLException e) {
 			throw new DAOException("SQL error", e);
 		}
 	}
 
 	public void setConnection(Connection connection) {
 		this.connection = connection;
 	}
 
 	protected Connection getConnection() {
 		return connection;
 	} 
 
 	/**
 	 * Perform an SQL selection based on a given example,
 	 * using either a LIKE or an =
 	 */
 	private Collection<E> performSelectForFind(E entity, boolean useLike)
 	throws DAOException {		
 //		Build the dynamic parts of the query.
 		StringBuffer namesBuffer = new StringBuffer("id");
 
 		for (String s : getDatabaseFieldNames()) {
 			namesBuffer.append(",");
 			namesBuffer.append(s);
 		}
 
 
 		Collection<String> fieldNames = getDatabaseFieldNames();
 		Collection<Object> fieldValues = getDatabaseValues(entity);
 
 		Iterator<String> fieldNameIterator = fieldNames.iterator();
 		Iterator<Object> fieldValueIterator = fieldValues.iterator();
 
 		LinkedList<Object> retainedValues = new LinkedList<Object>();
 
 		StringBuffer valuesBuffer = new StringBuffer();	
 
 		while (fieldNameIterator.hasNext()) {
 			String fieldName = fieldNameIterator.next();
 			Object fieldValue = fieldValueIterator.next();
 
 			if (fieldValue != null) {
 				valuesBuffer.append(fieldName);
 				
 				if (useLike) {
 					valuesBuffer.append(" LIKE ? AND ");	
 				} else {
 					valuesBuffer.append("=? AND ");
 				}
 
 				retainedValues.add(fieldValue);
 			}
 		}
 
 //		Remove the trailing commas.
 		valuesBuffer.setLength(valuesBuffer.length() - 5);
 
 //		Begin the transaction
 		try {
 			// Construct the statement.
 			String statementString = "SELECT " + namesBuffer
 			+ " FROM " + getTableName()
 			+ " WHERE " + valuesBuffer
 			+ " ORDER BY " + getTableName() + "." + getMySQLSortingString();
 			PreparedStatement statementObject = getConnection().prepareStatement(statementString);
 
 			int i = 1;
 			for (Object value : retainedValues) {
 				statementObject.setObject(i++, value);
 			}
 
 			// Execute the statement.
 			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
 			ResultSet rs = statementObject.executeQuery();
 			Vector<E> result = new Vector<E>();
			System.out.println("TNT: " + rs);
 			while (rs.next()) {
 				E e = createInstanceFromDatabaseValues(getTableName(), rs);				
 				e.setId(rs.getLong("id"));
				System.out.println("TNT: " + e);
 				result.add(e);
 			}
 
 			rs.close();
 			statementObject.close();
			System.out.println("TNT: result size " + result.size());
 			// Done
 			return result;
 		} catch (SQLException e) {
 			throw new DAOException("SQL error", e);
 		}
 	}
 
 	
 	/**
 	 * Get the name of the table that this DAO deals with
 	 */
 	abstract public String getTableName();
 	
 	/**
 	 * Get the database field names this DAO deals with
 	 * @return collection of field names this DAO deals with
 	 */
 	abstract public Collection<String> getDatabaseFieldNames();
 	
 	/**
 	 * Get the values (mapping to the field names of getDatabaseFieldNames)
 	 * from the entity this DAO is dealing with
 	 * @return collection of JDBC-serializable objects
 	 */
 	abstract public Collection<Object> getDatabaseValues(E entity);
 	
 	/**
 	 * Create an instance of the entity this DAO deals with from a query
 	 * that has been executed on the database
 	 * @param databaseValues is the resultset to create from
 	 * @return a ModelEntity instance
 	 */
 	abstract public E createInstanceFromDatabaseValues(String tableName, ResultSet databaseValues) throws SQLException, DAOException;
 
 	/**
 	 * Get the sorting string
 	 * @return sorting string (e.g., x DESC, y ASC)
 	 */
 	abstract public String getMySQLSortingString();
 }
