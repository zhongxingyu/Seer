 package com.alexrnl.commons.database;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Utility methods to build SQL queries.
  * @author Alex
  */
 public final class QueryGenerator {
 	/** Logger */
	private static Logger								lg			= Logger.getLogger(QueryGenerator.class.getName());
 	
 	/** Map between the entities and their id columns */
	private static Map<Class<? extends Entity>, Column>	idColumns	= new HashMap<>();
 	
 	/**
 	 * Default constructor.<br />
 	 */
 	private QueryGenerator () {
 		super();
 	}
 	
 	/**
 	 * Return the id {@link Column} of the entity.
 	 * @param object
 	 *        the object which represent the entity.
 	 * @return the {@link Column} which is defined as the unique (and identifying) column of the
 	 *         object.
 	 * @throws NoIdError
 	 *         if no such {@link Column} could be found.
 	 */
 	public static Column getIDColumn (final Entity object) throws NoIdError {
 		// If the id column has previously been found
 		if (idColumns.containsKey(object.getClass())) {
 			return idColumns.get(object.getClass());
 		}
 		// Searching the id column
 		for (final Column column : object.getEntityColumns().values()) {
 			if (column.isID()) {
 				if (lg.isLoggable(Level.FINE)) {
 					lg.fine("ID column found for entity " + object.getEntityName() + ": " + column.getName());
 				}
 				idColumns.put(object.getClass(), column);
 				return idColumns.get(object.getClass());
 			}
 		}
 		// If none, throw error
 		throw new NoIdError(object.getClass());
 	}
 	
 	/**
 	 * Escape the special characters of a String.
 	 * @param string
 	 *        the string to escape.
 	 * @return the escaped string.
 	 */
 	public static String escapeSpecialChars (final String string) {
 		String escapedString = string.replace("'", "''");
 		escapedString = escapedString.replace("\"", "\\\"");
 		return escapedString;
 	}
 	
 	/**
 	 * Generates the beginning of the insert query in SQL syntax:<br />
 	 * <code>INSERT INTO (<i>column names</i>) VALUES</code>
 	 * @param object
 	 *        the object which represent the entity.
 	 * @return the query ready for the entity specific data.
 	 * @see #insert(Entity,boolean) Method that allows to specify if the ID column
 	 *      should be in the query
 	 */
 	public static String insert (final Entity object) {
 		return insert(object, true);
 	}
 	
 	/**
 	 * Generates the beginning of the insert query in SQL syntax:<br />
 	 * <code>INSERT INTO (<i>column names</i>) VALUES</code><br />
 	 * The {@link Column}s will be inserted in the order of their declaration in the corresponding
 	 * enumeration.
 	 * @param object
 	 *        the object which represent the entity.
 	 * @param putID
 	 *        <code>true</code> if the {@link Column#isID() ID column} should be present in the
 	 *        query.
 	 * @return the query ready for the entity specific data.
 	 */
 	public static String insert (final Entity object, final boolean putID) {
 		final StringBuilder columnsNames = new StringBuilder();
 		
 		for (final Column currentColumn : object.getEntityColumns().values()) {
 			if (!putID && currentColumn.isID()) {
 				continue;
 			}
 			// Adding a comma between fields
 			if (columnsNames.length() > 0) {
 				columnsNames.append(",");
 			}
 			columnsNames.append("`");
 			columnsNames.append(currentColumn.getName());
 			columnsNames.append("`");
 		}
 		
 		return "INSERT INTO " + object.getEntityName() + "(" + columnsNames.toString() + ") VALUES ";
 	}
 	
 	/**
 	 * Generates a string with the delete statement for a SQL database.<br />
 	 * <code>DELETE FROM entity WHERE idColumn = value</code>
 	 * @param object
 	 *        the object which represent the entity.
 	 * @return the query which delete an object from the table.
 	 */
 	public static String delete (final Entity object) {
 		return delete(object, false);
 	}
 	
 	/**
 	 * Generates a string with the delete statement for a SQL database.<br />
 	 * <code>DELETE FROM entity WHERE idColumn = value</code>
 	 * @param object
 	 *        the object which represent the entity.
 	 * @param preparedStatement
 	 *        <code>true</code> if the query is for a prepared statement.
 	 * @return the query which delete an object from the table.
 	 */
 	public static String delete (final Entity object, final boolean preparedStatement) {
 		return "DELETE FROM " + object.getEntityName() + whereID(object, preparedStatement ? null : object.getID());
 	}
 	
 	/**
 	 * Generates the beginning of the update query of the entity. <code>UPDATE entity SET </code>
 	 * @param object
 	 *        the object which represent the entity.
 	 * @return the query ready for adding the entity specific data.
 	 */
 	public static String update (final Entity object) {
 		return "UPDATE " + object.getEntityName() + " SET ";
 	}
 	
 	/**
 	 * Generates the 'WHERE' part of a query which identifies it by its id.<br />
 	 * <code> WHERE idColumn = value</code>
 	 * @param object
 	 *        the object which represent the entity.
 	 * @return the WHERE clause of a query.
 	 */
 	public static String whereID (final Entity object) {
 		return whereID(object, object.getID());
 	}
 	
 	/**
 	 * Generates the 'WHERE' part of a query which identifies it by its id.<br />
 	 * <code> WHERE idColumn = value</code><br />
 	 * If the id is <code>null</code>, then a query for a prepared statement is generated (with <code>?</code>).
 	 * @param object
 	 *        the object which represent the entity.
 	 * @param id
 	 *        the id of the object to find.
 	 * @return the WHERE clause of a query.
 	 */
 	public static String whereID (final Entity object, final Object id) {
 		return where(getIDColumn(object), id);
 	}
 	
 	/**
 	 * Generates the 'WHERE' part of a query using a single column condition.<br />
 	 * <code> WHERE fieldName = value</code><br />
 	 * If the id is <code>null</code>, then a query for a prepared statement is generated (with
 	 * <code>?</code>).<br />
 	 * If the fieldName is <code>null</code> the field name will be replaced by <code>?</code> which
 	 * can be used in a prepared statement.<br />
 	 * @param field
 	 *        the name of the column to use.
 	 * @param value
 	 *        the value of the object to find.
 	 * @return the WHERE clause of a query.
 	 */
 	public static String where (final Column field, final Object value) {
 		return where(field, value, false);
 	}
 	
 	/**
 	 * Generates the 'WHERE' part of a query using a single column condition.<br />
 	 * <code> WHERE fieldName = value // like = false<br />
 	 *  WHERE LIKE fieldName = value% // like = true</code><br />
 	 * If the id is <code>null</code>, then a query for a prepared statement is generated (with
 	 * <code>?</code>).<br />
 	 * If the fieldName is <code>null</code> the field name will be replaced by <code>?</code> which
 	 * can be used in a prepared statement.<br />
 	 * The <code>like</code> flag allow to use the same method to generate queries with the LIKE operator
 	 * @param field
 	 *        the name of the column to use.
 	 * @param value
 	 *        the value of the object to find.
 	 * @param like
 	 *        <code>true</code> if the query generated should be used for incomplete values searches.
 	 * @return the WHERE clause of a query.
 	 */
 	public static String where (final Column field, final Object value, final boolean like) {
 		final String fieldName = field.getName();
 		final String operator =  like ? " LIKE " : " = ";
 		String fieldValue = value == null ? "?" : escapeSpecialChars(value.toString());
 		
 		if (value != null && field.getType().getSuperclass().equals(Number.class)) {
 			fieldValue = "'" + fieldValue + "'";
 		}
 		
 		return " WHERE " + fieldName + operator + fieldValue;
 	}
 	
 	/**
 	 * Generates the 'WHERE' part of a query using a single column condition with the like operator.<br />
 	 * <code> WHERE fieldName LIKE value%</code><br />
 	 * If the id is <code>null</code>, then a query for a prepared statement is generated (with
 	 * <code>?</code>).<br />
 	 * If the fieldName is <code>null</code> the field name will be replaced by <code>?</code> which
 	 * can be used in a prepared statement.<br />
 	 * @param field
 	 *        the name of the column to use.
 	 * @param value
 	 *        the value of the object to find.
 	 * @return the WHERE clause of a query.
 	 */
 	public static String whereLike (final Column field, final Object value) {
 		return where(field, value, true);
 	}
 	
 	/**
 	 * Generates the 'SELECT *' query for the current object.<br />
 	 * <code>SELECT * FROM entity</code>
 	 * @param object
 	 *        the object which represent the entity.
 	 * @return the SQL query for retrieving all the rows from an entity.
 	 */
 	public static String searchAll (final Entity object) {
 		return "SELECT * FROM " + object.getEntityName();
 	}
 	
 	/**
	 * Generates the insert prepared statement.<br />
 	 * <code>INSERT INTO entity (columns) VALUES (?, ?, ?)</code>
 	 * @param object
 	 *        the target entity.
 	 * @return the prepared query.
 	 */
 	public static String insertPrepared (final Entity object) {
 		final StringBuilder fields = new StringBuilder(insert(object, false)).append("(");
 		int columnNumber = object.getEntityColumns().size() - 1;
 		if (columnNumber < 1) {
 			return "";
 		}
 		while (columnNumber > 1) {
 			fields.append("?, ");
 			--columnNumber;
 		}
 		return fields.append("?)").toString();
 	}
 	
	/**
	 * Generate the update prepared statement.<br />
	 * <code>UPDATE entity SET columns = ? WHERE idColumn = ?</code>
	 * @param object
	 *        the target entity
	 * @return the prepared query
	 */
	public static String updatePrepared (final Entity object) {
		final StringBuilder update = new StringBuilder(update(object));
		
		for (final Column column : object.getEntityColumns().values()) {
			if (column.isID()) {
				// Don't set the id of a column
				continue;
			}
			update.append(column.getName()).append(" = ?, ");
		}
		update.delete(update.length() - 2, update.length());
		
		return update.append(whereID(object, null)).toString();
	}
	
}
