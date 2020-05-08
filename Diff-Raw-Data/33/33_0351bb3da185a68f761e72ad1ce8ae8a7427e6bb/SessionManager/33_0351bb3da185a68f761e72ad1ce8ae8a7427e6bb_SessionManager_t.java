 /*
  *
  *  JMoney - A Personal Finance Manager
  *  Copyright (c) 2004 Nigel Westbury <westbury@users.sourceforge.net>
  *
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  *
  */
 
 package net.sf.jmoney.jdbcdatastore;
 
 import java.lang.ref.WeakReference;
 import java.lang.reflect.InvocationTargetException;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.Vector;
 
 import net.sf.jmoney.JMoneyPlugin;
 import net.sf.jmoney.model2.Account;
 import net.sf.jmoney.model2.CapitalAccount;
 import net.sf.jmoney.model2.Commodity;
 import net.sf.jmoney.model2.CurrencyAccount;
 import net.sf.jmoney.model2.DatastoreManager;
 import net.sf.jmoney.model2.Entry;
 import net.sf.jmoney.model2.ExtendableObject;
 import net.sf.jmoney.model2.ExtendablePropertySet;
 import net.sf.jmoney.model2.ExtensionPropertySet;
 import net.sf.jmoney.model2.IEntryQueries;
 import net.sf.jmoney.model2.IListManager;
 import net.sf.jmoney.model2.IObjectKey;
 import net.sf.jmoney.model2.IValues;
 import net.sf.jmoney.model2.ListKey;
 import net.sf.jmoney.model2.ListPropertyAccessor;
 import net.sf.jmoney.model2.PropertyAccessor;
 import net.sf.jmoney.model2.PropertySet;
 import net.sf.jmoney.model2.ReferencePropertyAccessor;
 import net.sf.jmoney.model2.ScalarPropertyAccessor;
 import net.sf.jmoney.model2.Session;
 import net.sf.jmoney.model2.SessionInfo;
 import net.sf.jmoney.model2.Transaction;
 
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IPersistableElement;
 import org.eclipse.ui.IWorkbenchWindow;
 
 /**
  * Manages a session that is held in a JDBC database.
  *
  * @author Nigel Westbury
  */
 public class SessionManager extends DatastoreManager implements IEntryQueries {
 	
 	/**
 	 * Date format used for embedding dates in SQL statements:
 	 * yyyy-MM-dd
 	 */
 	private static SimpleDateFormat dateFormat = (SimpleDateFormat) DateFormat.getDateInstance();
 	static {
 		dateFormat.applyPattern("yyyy-MM-dd");
 	}
 
 	private boolean isHsqldb = false;
 	
 	private Connection connection;
 	
 	private Statement reusableStatement;
 	
 	private IDatabaseRowKey sessionKey;
 	
 	/**
 	 * For each <code>PropertySet</code> for which objects are required
 	 * to be cached, map the <code>PropertySet</code> to a <Map>.
 	 * Each map is itself a map of integer ids to extendable objects.
 	 * <P>
 	 * If a PropertySet is cached then so are all property sets
 	 * derived from that property set.  However, derived property
 	 * sets do not have their own map.  Instead objects of the
 	 * derived property set are put in the map for the base property
 	 * set.
 	 */
 	private Map<ExtendablePropertySet<?>, Map<Integer, WeakReference<ExtendableObject>>> objectMaps = new HashMap<ExtendablePropertySet<?>, Map<Integer, WeakReference<ExtendableObject>>>();
 	
 	private class ParentList {
 		ExtendablePropertySet<?> parentPropertySet;
 		ListPropertyAccessor listProperty;
 
 		ParentList(ExtendablePropertySet<?> parentPropertySet, ListPropertyAccessor listProperty) {
 			this.parentPropertySet = parentPropertySet;
 			this.listProperty = listProperty;
 		}
 		
 		public String getColumnName() {
 			return listProperty.getName().replace('.', '_');
 		}
 	}
 	
 	/**
 	 * A map of PropertySet objects to non-null Vector objects.
 	 * Each Vector object is a list of ParentList objects.
 	 * An entry exists in this map for all property sets that are
 	 * not extension property sets.  For each property set, the
 	 * vector contains a list of the list properties in all property
 	 * sets that contain a list of objects of the property set.
 	 */
 	private Map<ExtendablePropertySet<?>, Vector<ParentList>> tablesMap = null;
 	
 	public SessionManager(Connection connection) throws SQLException {
 		this.connection = connection;
 
 		/*
 		 * Set on the flag that indicates if we are operating with a database that
 		 * has non-standard features that affect us.
 		 */
 		String databaseProductName = connection.getMetaData().getDatabaseProductName();
 		if (databaseProductName.equals("HSQL Database Engine")) {
 			isHsqldb = true;
 		}
 
 		// Create a weak reference map for every base property set.
 		for (ExtendablePropertySet<?> propertySet: PropertySet.getAllExtendablePropertySets()) { 
 			if (propertySet.getBasePropertySet() == null) {
 				objectMaps.put(propertySet, new HashMap<Integer, WeakReference<ExtendableObject>>());
 			}
 		}
 		
 		try {
 			this.reusableStatement = connection.createStatement();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new RuntimeException("SQL Exception: " + e.getMessage());
 		}
 	
 		// Create a statement for our use.
 		Statement stmt = connection.createStatement();
 
 		/*
 		 * Find all properties in any property set that are a list of objects
 		 * with the type as this property set. A column must exist in this table
 		 * for each such property that exists in another property set.
 		 * 
 		 * The exception is that no such column exists if the list property is
 		 * in the session object (not including extentions of the session
 		 * object). This is an optimization that saves columns. The optimization
 		 * works because we know there is only ever a single session object.
 		 * 
 		 * The reason why extensions are not included is because we do not know
 		 * what lists may be added in extensions. A list may be added that
 		 * contains the same object type as one of the session lists. For
 		 * example, an extension to the session object may contain a list of
 		 * currencies. A parent column must exist in the currency table to
 		 * indicate if a currency is in such a list. Otherwise we would know the
 		 * currency is in a list property of the session object but we would not
 		 * know which list.
 		 */
 		tablesMap = new Hashtable<ExtendablePropertySet<?>, Vector<ParentList>>();
 		for (ExtendablePropertySet<?> propertySet: PropertySet.getAllExtendablePropertySets()) {
 			Vector<ParentList> list = new Vector<ParentList>();  // List of PropertyAccessors
 			for (ExtendablePropertySet<?> propertySet2: PropertySet.getAllExtendablePropertySets()) {
 				for (ListPropertyAccessor<?> listAccessor: propertySet2.getListProperties2()) {
 					if (listAccessor.getPropertySet() != SessionInfo.getPropertySet()) {
 						if (propertySet.getImplementationClass() == listAccessor.getElementPropertySet().getImplementationClass()) {
 							// Add to the list of possible parents.
 							list.add(new ParentList(propertySet2, listAccessor));
 						}
 					}
 				}
 			}
 			tablesMap.put(propertySet, list);
 		}
 		
 		// Check that all the required tables and columns exist.
 		// Any missing tables or columns are created at this time.
 		checkDatabase(connection, stmt);
 		
 		/*
 		 * Create the single row in the session table, if it does not
 		 * already exist.  Create this row with default values for
 		 * the session properties. 
 		 */
 		String sql3 = "SELECT * FROM " 
 			+ SessionInfo.getPropertySet().getId().replace('.', '_');
 		System.out.println(sql3);
 		ResultSet rs3 = stmt.executeQuery(sql3);
 		if (!rs3.next()) {
 
 			String sql = "INSERT INTO " 
 				+ SessionInfo.getPropertySet().getId().replace('.', '_')
 				+ " (";
 
 			String columnNames = "";
 			String columnValues = "";
 			String separator = "";
 
 			for (ScalarPropertyAccessor<?> propertyAccessor: SessionInfo.getPropertySet().getScalarProperties2()) {
 				String columnName = getColumnName(propertyAccessor);
 				Object value = propertyAccessor.getDefaultValue();
 				
 				columnNames += separator + "\"" + columnName + "\"";
 				columnValues += separator + valueToSQLText(value);
 
 				separator = ", ";
 			}
 
 			sql += columnNames + ") VALUES(" + columnValues + ")";
 
 			try {
 				System.out.println(sql);
 				stmt.execute(sql);
 			} catch (SQLException e) {
 				// TODO Handle this properly
 				e.printStackTrace();
 				throw new RuntimeException("internal error");
 			}
 
 			// Now try again to get the session row.
 			rs3 = stmt.executeQuery(sql3);
 			rs3.next();
 		}
 		
 		// Now create the session object from the session database row
 		this.sessionKey = new ObjectKey(rs3, SessionInfo.getPropertySet(), null, this);
 		
 		rs3.close();
 		stmt.close();
 	}
 
 	@Override
 	public Session getSession() {
 		return (Session)sessionKey.getObject();
 	}
 	
 	/**
 	 * @return
 	 */
 	public IDatabaseRowKey getSessionKey() {
 		return sessionKey;
 	}
 
 	/**
 	 * @return
 	 */
 	public Connection getConnection() {
 		return connection;
 	}
 	
 	/**
 	 * This method provides a statement object to consumers
 	 * which need a statement object and can guarantee that
 	 * they will have finished with the statement before
 	 * another call to this method is made.  Realistically
 	 * that means this method should only be called when the
 	 * caller has closed the result set before the calling
 	 * method returns and, furthermore, no calls are made to
 	 * methods that may make SQL queries.
 	 */
 	public Statement getReusableStatement() {
 		return reusableStatement;
 	}
 	
 	@Override
 	public boolean canClose(IWorkbenchWindow window) {
 		// A JDBC database can always be closed without further
 		// input from the user.
 		return true;
 	}
 	
 	@Override
 	public void close() {
 		try {
 			reusableStatement.close();
 			connection.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new RuntimeException("SQL Exception: " + e.getMessage());
 		}
 	}
 	
 	@Override
 	public String getBriefDescription() {
 		// TODO: improve this implementation to give more
 		// details of the database.
 		return "JDBC database";
 	}
 
 	private IPersistableElement persistableElement 
 	= new IPersistableElement() {
 		public String getFactoryId() {
 			return "net.sf.jmoney.jdbcdatastore.SessionFactory";
 		}
 		public void saveState(IMemento memento) {
 			/*
 			 * The open session must be using the database as
 			 * specified in the preference pages.  Therefore there
 			 * is no need to save anything further here.
 			 * 
 			 * If we were to give the user the option at 'open' time
 			 * to open a database other than the database specified in
 			 * the prefence page then we would have to save that information
 			 * here.
 			 */
 		}
 	};
 	
 	public Object getAdapter(Class adapter) {
 		if (adapter == IPersistableElement.class) {
 			return persistableElement;
 		}
 		
 		if (adapter == IEntryQueries.class) {
 			return this;
 		}
 		
 		return null;
 	}
 	
 	public <E extends ExtendableObject> E getObjectIfMaterialized(ExtendablePropertySet<E> basemostPropertySet, int id) {
 		Map<Integer, WeakReference<ExtendableObject>> result = objectMaps.get(basemostPropertySet);
 		WeakReference<ExtendableObject> object = result.get(id);
 		if (object == null) {
 			// Indicate that the object is not cached.
 			return null;
 		} else {
 			return basemostPropertySet.getImplementationClass().cast(object.get());
 		}
 	}
 
 	public <E extends ExtendableObject> void setMaterializedObject(ExtendablePropertySet<E> basemostPropertySet, int id, E extendableObject) {
 		Map<Integer, WeakReference<ExtendableObject>> result = objectMaps.get(basemostPropertySet);
 		result.put(id, new WeakReference<ExtendableObject>(extendableObject));
 	}
 
 	/**
 	 * This method builds a select statement that joins the table for a given
 	 * property set with all the ancestor tables.  This results in a result set
 	 * that contains all the columns necessary to construct the objects.
 	 * 
 	 * The caller would normally append a WHERE clause to the returned statement
 	 * before executing it.
 	 * 
 	 * @param propertySet
 	 * @return
 	 * @throws SQLException
 	 */
 	String buildJoins(ExtendablePropertySet<?> finalPropertySet) {
 		/*
 		 * Some databases (e.g. HDBSQL) execute queries faster if JOIN ON is
 		 * used rather than a WHERE clause, and will also execute faster if the
 		 * smallest table is put first. The smallest table in this case is the
 		 * table represented by typedPropertySet and the larger tables are the
 		 * base tables.
 		 */
 		String tableName = finalPropertySet.getId().replace('.', '_');
 		String sql = "SELECT * FROM " + tableName;
 		for (ExtendablePropertySet<?> propertySet2 = finalPropertySet.getBasePropertySet(); propertySet2 != null; propertySet2 = propertySet2.getBasePropertySet()) {
 			String tableName2 = propertySet2.getId().replace('.', '_');
 			sql += " JOIN " + tableName2
 					+ " ON " + tableName + "._ID = " + tableName2 + "._ID";
 		}
 
 		return sql;
 	}
 	
 	/**
 	 * This method creates a new statement on each call.  This allows callers
 	 * to have multiple result sets active at the same time.
 	 * 
 	 * listProperty may be a list of a derivable property set.  We thus will not
 	 * know the exact property set of each element in advance.  The caller must
 	 * pass an the actual property set (a final property set) and this method will
 	 * return a result set containing only elements of that property set and containing
 	 * columns for all properties of that result set.
 	 * 
 	 * @param parentKey
 	 * @param listProperty
 	 * @param finalPropertySet
 	 * @return
 	 * @throws SQLException
 	 */
 	ResultSet executeListQuery(DatabaseListKey<?> listKey, ExtendablePropertySet<?> finalPropertySet) throws SQLException {
 		String sql = buildJoins(finalPropertySet);
 		
 		/*
 		 * Add the WHERE clause. There is a parent column with the same name as
 		 * the name of the list property. Only if the number in this column is
 		 * the same as the id of the owner of this list is the object in this
 		 * list.
 		 * 
 		 * Note that there is an optimization. If the parent object is the
 		 * session object then no such column will exist. We instead check that
 		 * all the other parent columns (if any) are null.
 		 */
 		if (listKey.listPropertyAccessor.getPropertySet() == SessionInfo.getPropertySet()) {
 			String whereClause = "";
 			String separator = "";
 			for (ExtendablePropertySet<?> propertySet = finalPropertySet; propertySet != null; propertySet = propertySet.getBasePropertySet()) {
 				Vector<ParentList> possibleContainingLists = tablesMap.get(propertySet);
 				for (ParentList parentList: possibleContainingLists) {
 					whereClause += separator + "\"" + parentList.getColumnName() + "\" IS NULL";
 					separator = " AND";
 				}
 			}
 			if (whereClause.length() != 0) {
 				sql += " WHERE " + whereClause;
 			}
 
 			Statement stmt = connection.createStatement();
 			System.out.println(sql);
 			return stmt.executeQuery(sql);
 		} else {
 			/*
 			 * Add a WHERE clause that limits the result set to those rows
 			 * that are in the appropriate list in the appropriate parent object.
 			 */
 			sql += " WHERE \"" + listKey.listPropertyAccessor.getName().replace('.', '_') + "\" = ?";
 
 			System.out.println(sql + " : " + listKey.parentKey.getRowId());
 			PreparedStatement stmt = connection.prepareStatement(sql);
 			stmt.setInt(1, listKey.parentKey.getRowId());
 			return stmt.executeQuery();
 		}
 	}
 	
 	/**
 	 * @param propertySet
 	 * @param values
 	 * @param listProperty
 	 * @param parent
 	 * @param sessionManager
 	 * 
 	 * @return The id of the inserted row
 	 */
 	public int insertIntoDatabase(ExtendablePropertySet<?> propertySet, ExtendableObject newObject, DatabaseListKey<?> listKey) {
 		int rowId = -1;
 
 		// We must insert into the base table first, then the table for the objects
 		// derived from the base and so on.  The reason is that each derived table
 		// has a primary key field that is a foreign key into its base table.
 		// We can get the chain of property sets only by starting at the given 
 		// property set and repeatedly getting the base property set.  We must
 		// therefore store these so that we can loop through the property sets in
 		// the reverse order.
 		
 		Vector<ExtendablePropertySet<?>> propertySets = new Vector<ExtendablePropertySet<?>>();
 		for (ExtendablePropertySet<?> propertySet2 = propertySet; propertySet2 != null; propertySet2 = propertySet2.getBasePropertySet()) {
 			propertySets.add(propertySet2);
 		}
 		
 		for (int index = propertySets.size()-1; index >= 0; index--) {
 			ExtendablePropertySet<?> propertySet2 = propertySets.get(index);
 			
 			String sql = "INSERT INTO " 
 				+ propertySet2.getId().replace('.', '_')
 				+ " (";
 			
 			String columnNames = "";
 			String columnValues = "";
 			String separator = "";
 			
 			/*
 			 * If this is a basemost property set then the _ID column will be
 			 * auto-generated by the database.  If this is a derived property
 			 * set then we must insert the id that had been assigned when the
 			 * row in the basemost table was inserted.
 			 */
 			if (index != propertySets.size()-1) {
 				columnNames += separator + "_ID";
 				columnValues += separator + Integer.toString(rowId);
 				separator = ", ";
 			}
 			
 			for (ScalarPropertyAccessor<?> propertyAccessor: propertySet2.getScalarProperties2()) {
 				String columnName = getColumnName(propertyAccessor);
 
 				// Get the value from the passed property value array.
 				Object value = newObject.getPropertyValue(propertyAccessor);
 
 				columnNames += separator + "\"" + columnName + "\"";
 				columnValues += separator + valueToSQLText(value);
 
 				separator = ", ";
 			}
 
 			/* Set the parent id in the appropriate column.
 			 * 
 			 * If the containing list property is a property in one of the three
 			 * lists in the session object
 			 * then, as an optimization, there is no parent column.
 			 */
 			if (listKey.listPropertyAccessor.getElementPropertySet() == propertySet2
 					&& listKey.listPropertyAccessor.getPropertySet() != SessionInfo.getPropertySet()) {
 				String valueString = Integer.toString(listKey.parentKey.getRowId());
 				String parentColumnName = listKey.listPropertyAccessor.getName().replace('.', '_');
 				columnNames += separator + "\"" + parentColumnName + "\"";
 				columnValues += separator + valueString;
 				separator = ", ";
 			}
 
 			/*
 			 * If the base-most property set and it is derivable, the
 			 * _PROPERTY_SET column must be set.
 			 */
 			if (propertySet2.getBasePropertySet() == null
 			 && propertySet2.isDerivable()) {
 				columnNames += separator + "_PROPERTY_SET";
 				// Set to the id of the final
 				// (non-derivable) property set for this object.
 				ExtendablePropertySet<?> finalPropertySet = propertySets.get(0); 
 				columnValues += separator + "\'" + finalPropertySet.getId() + "\'";
 				separator = ", ";
 			}
 			
 			sql += columnNames + ") VALUES(" + columnValues + ")";
 			
 			try {
 				System.out.println(sql);
 				/*
 				 * Insert the row and, if this is a basemost table, get the
 				 * value of the auto-generated key.
 				 */
 				if (index == propertySets.size()-1) {
 					ResultSet rs;
 					if (isHsqldb) {
 						/*
 						 * HSQLDB does not, as of 1.8.0.7, support the JDBC
 						 * standard way of getting the generated key. We must do
 						 * things slightly differently.
 						 */
 						reusableStatement.execute(sql);
 						rs = reusableStatement.executeQuery("CALL IDENTITY()");
 					} else {
 						reusableStatement.execute(sql, Statement.RETURN_GENERATED_KEYS);
 						rs = reusableStatement.getGeneratedKeys();
 					}
 					rs.next();
 					rowId = rs.getInt(1);
 					rs.close();
 				} else {
 					reusableStatement.execute(sql);
 				}
 			} catch (SQLException e) {
 				// TODO Handle this properly
 				e.printStackTrace();
 				throw new RuntimeException("internal error");
 			}
 		}
 
 		return rowId;
 	}
 
 	public <E extends ExtendableObject> void reparentInDatabase(E extendableObject, DatabaseListKey<E> newListKey) {
 		IDatabaseRowKey objectKey = (IDatabaseRowKey)extendableObject.getObjectKey();
 		ListKey originalListKey = extendableObject.getParentListKey();
 		IDatabaseRowKey originalParentKey = (IDatabaseRowKey)originalListKey.getParentKey();
 		
 		try {
 			boolean priorAutocommitState = connection.getAutoCommit();
 			connection.setAutoCommit(false);
 
 			/*
 			 * We may need one or two updates, depending on whether the column containing
 			 * the original parent and the column containing the new parent are in the same
 			 * table.
 			 */
 			try {
 
 				/*
 				 * If the containing list property is a property in one of the three
 				 * lists in the session object then, as an optimization, there is no
 				 * parent column.
 				 * 
 				 * Clear out the original parent id (unless the containing list is one
 				 * of the three lists in the extendable session object).
 				 */
 				if (originalListKey.getListPropertyAccessor().getPropertySet() != SessionInfo.getPropertySet()) {
 					String parentColumnName = originalListKey.getListPropertyAccessor().getName().replace('.', '_');
 					String sql = "UPDATE "
 						+ originalListKey.getListPropertyAccessor().getElementPropertySet().getId().replace('.', '_')
 						+ " SET " + parentColumnName + "=NULL"
 						+ " WHERE _ID=" + objectKey.getRowId()
 						+ " AND " + parentColumnName + "=" + originalParentKey.getRowId();
 
 					System.out.println(sql);
 					int numberUpdated = reusableStatement.executeUpdate(sql);
 					if (numberUpdated != 1) {
 						throw new RuntimeException("internal error");
 					}
 
 					connection.commit();
 				}
 
 				/*
 				 * Set the new parent id (unless the containing list is one of the three
 				 * lists in the extendable session object).
 				 */
 				if (newListKey.listPropertyAccessor.getPropertySet() != SessionInfo.getPropertySet()) {
 					String parentColumnName = newListKey.listPropertyAccessor.getName().replace('.', '_');
 					String sql = "UPDATE "
 						+ newListKey.listPropertyAccessor.getElementPropertySet().getId().replace('.', '_')
 						+ " SET " + parentColumnName + "=" + newListKey.parentKey.getRowId()
 						+ " WHERE _ID=" + objectKey.getRowId()
 						+ " AND " + parentColumnName + " IS NULL";
 
 					System.out.println(sql);
 					int numberUpdated = reusableStatement.executeUpdate(sql);
 					if (numberUpdated != 1) {
 						throw new RuntimeException("internal error");
 					}
 				}
 			} finally {
 				connection.setAutoCommit(priorAutocommitState);
 			}
 		} catch (SQLException e) {
 			// TODO Handle this properly
 			e.printStackTrace();
 			throw new RuntimeException("internal error");
 		}
 	}
 	
 	/**
 	 * Execute SQL UPDATE statements to update the database with
 	 * the new values of the properties of an object.
 	 * <P>
 	 * The SQL statements will verify the old values of the properties
 	 * in the WHERE clause.  If the database does not contain an object
 	 * with the expected old property values that an exception is raised,
 	 * causing the transaction to be rolled back.
 	 * 
 	 * @param rowId
 	 * @param oldValues
 	 * @param newValues
 	 * @param sessionManager
 	 */
 	public void updateProperties(ExtendablePropertySet<?> propertySet, int rowId, Object[] oldValues, Object[] newValues) {
 		Statement stmt = getReusableStatement();
 
 		// The array of property values contains the properties from the
 		// base table first, then the table derived from that and so on.
 		// We therefore process the tables starting with the base table
 		// first.  This requires first copying the property sets into
 		// an array so that we can iterate them in reverse order.
 		Vector<ExtendablePropertySet<?>> propertySets = new Vector<ExtendablePropertySet<?>>();
 		for (ExtendablePropertySet<?> propertySet2 = propertySet; propertySet2 != null; propertySet2 = propertySet2.getBasePropertySet()) {
 			propertySets.add(propertySet2);
 		}
 		
 		int propertyIndex = 0;
 
 		for (int index = propertySets.size()-1; index >= 0; index--) {
 			ExtendablePropertySet<?> propertySet2 = propertySets.get(index);
 			
 			String sql = "UPDATE " 
 				+ propertySet2.getId().replace('.', '_')
 				+ " SET ";
 			
 			String updateClauses = "";
 			String whereTerms = "";
 			String separator = "";
 			
 			for (ScalarPropertyAccessor<?> propertyAccessor: propertySet2.getScalarProperties2()) {
 
 				if (propertyAccessor.getIndexIntoScalarProperties() != propertyIndex) {
 					throw new RuntimeException("index mismatch");
 				}
 				// See if the value of the property has changed.
 				Object oldValue = oldValues[propertyIndex];
 				Object newValue = newValues[propertyIndex];
 				propertyIndex++;
 
 				if (!JMoneyPlugin.areEqual(oldValue, newValue)) {
 					String columnName = getColumnName(propertyAccessor);
 
 					updateClauses += separator + "\"" + columnName + "\"=" + valueToSQLText(newValue);
 					
 					if (oldValue != null) {
 						whereTerms += " AND \"" + columnName + "\"=" + valueToSQLText(oldValue);
 					} else {
 						whereTerms += " AND \"" + columnName + "\" IS NULL";
 					}
 					separator = ", ";
 				}
 			}
 			
 			// If no properties have been updated in a table then no update
 			// statement should be executed.
 			
 			if (!separator.equals("")) {
 				sql += updateClauses + " WHERE _ID=" + rowId + whereTerms;
 				
 				try {
 					System.out.println(sql);
 					int numberUpdated = stmt.executeUpdate(sql);
 					if (numberUpdated != 1) {
 						// This could happen if a column in the table contains a string
 						// serialization of a custom object, and the column contained a string
 						// that failed to construct a value.  In that case, the prior property value will
 						// be null be the value in the database will be non-null.
 						throw new RuntimeException("Update failed.  Row with expected data was not found.");
 					}
 				} catch (SQLException e) {
 					// TODO Handle this properly
 					e.printStackTrace();
 					throw new RuntimeException("internal error");
 				}
 			}
 		}
 	}
 
 	/**
 	 * Given a value of a property as an Object, return the text that 
 	 * represents the value in an SQL statement.
 	 * 
 	 * @param newValue
 	 * @return
 	 */
 	// TODO: If we always used prepared statements with parameters
 	// then we may not need this method at all.
 	private static String valueToSQLText(Object value) {
 		String valueString;
 		
 		if (value != null) {
 			Class<?> valueClass = value.getClass();
 			if (valueClass == String.class
 					|| valueClass == char.class
 					|| valueClass == Character.class) {
 				valueString = '\'' + value.toString().replaceAll("'", "''") + '\'';
 			} else if (value instanceof Date) {
 				Date date = (Date)value;
 				valueString = '\'' + dateFormat.format(date) + '\'';
 			} else if (value instanceof Boolean) {
 				// MS SQL does not allow true and false,
 				// even though HSQL does.  So we cannot use toString.
 				Boolean bValue = (Boolean)value;
 				valueString = bValue.booleanValue() ? "1" : "0";
 			} else if (ExtendableObject.class.isAssignableFrom(valueClass)) {
 				ExtendableObject extendableObject = (ExtendableObject)value;
 				IDatabaseRowKey key = (IDatabaseRowKey)extendableObject.getObjectKey();
 				valueString = Integer.toString(key.getRowId());
 			} else if (Number.class.isAssignableFrom(valueClass)) {
 				valueString = value.toString();
 			} else {
 				/*
 				 * All other objects are serialized to a string.
 				 */
 				valueString = '\'' + value.toString().replaceAll("'", "''") + '\'';
 			}
 		} else {
 			valueString = "NULL";
 		}
 
 		return valueString;
 	}
 
 	/**
 	 * Execute SQL DELETE statements to remove the given object
 	 * from the database.
 	 * 
 	 * @param objectKey
      * @return true if the object was deleted, false if the object
      * 				was not not deleted because there are references to it
      * 				in the database
      * @throws RuntimeException if either an SQLException occurs or if
      * 				the object did not exist in the database or if some
      * 				other integrity violation was found in the database
 	 */
 	public boolean deleteFromDatabase(IDatabaseRowKey objectKey) {
 		ExtendablePropertySet<?> propertySet = PropertySet.getPropertySet(objectKey.getObject().getClass()); 
 		
 		Statement stmt = getReusableStatement();
 
 		/*
 		 * Because we cannot always use CASCADE, we must first delete objects
 		 * in list properties contained in this object.  This is a recursive
 		 * process.
 		 */
 		deleteListElements(objectKey);
 		
 		/*
 		 * Because each table for a derived class contains a foreign key
 		 * constraint to the table for the base class, we must delete the rows
 		 * starting with the most derived table and ending with the base-most
 		 * table.
 		 * 
 		 * Alternatively, we could have set the 'CASCADE' option for delete in
 		 * the database and just delete the row in the base-most table. However,
 		 * it is perhaps safer not to use 'CASCADE'.
 		 */
 		for (ExtendablePropertySet<?> propertySet2 = propertySet; propertySet2 != null; propertySet2 = propertySet2.getBasePropertySet()) {
 			
 			String sql = "DELETE FROM " 
 				+ propertySet2.getId().replace('.', '_')
 				+ " WHERE _ID=" + objectKey.getRowId();
 			
 			try {
 				System.out.println(sql);
 				int rowCount = stmt.executeUpdate(sql);
 				if (rowCount != 1) {
 					if (rowCount == 0
 							&& propertySet2 == propertySet) {
 						/*
 						 * The object does not exist in the database. It is
 						 * possible that another process deleted it so we ignore
 						 * this condition.
 						 */
 					} else {
 						throw new RuntimeException("database is inconsistent");
 					}
 				}
 			} catch (SQLException e) {
 				if (e.getSQLState().equals("23000")) {
 					/*
 					 * An attempt has been made to delete an object that has
 					 * references to it. This particular error, unlike all other
 					 * SQL errors, is treated as a valid result, not an error.
 					 * This is because the caller may legitimately attempt to
 					 * delete such rows because this saves the caller from
 					 * having to check for references itself (not a trivial
 					 * task, so why not let the database do the check).
 					 */
 					return false;
 				}
 				// TODO Handle this properly
 				e.printStackTrace();
 				throw new RuntimeException("internal error");
 			}
 		}
 		
 		return true;
 	}
 
 	/**
 	 * This method deletes the child elements of a given object (objects
 	 * contained in list properties of the given object). This method is
	 * recursive, so all descendant objects are deleted.
 	 * 
 	 * We could have used ON DELETE CASCADE to delete these objects. However,
 	 * not all databases fully support this. For example, Microsoft SQL Server
 	 * does not support ON DELETE CASCADE when a column in a table is
 	 * referencing another row in the same table. This makes it unusable for us
 	 * because columns can reference other rows in the same table (for example,
 	 * an account can have sub-accounts which are rows in the same table).
 	 * 
 	 * @param rowId
 	 * @param extendableObject
 	 * @param propertySet
 	 */
 	private void deleteListElements(IDatabaseRowKey objectKey) {
 		ExtendableObject extendableObject = objectKey.getObject();
 		ExtendablePropertySet<?> propertySet = PropertySet.getPropertySet(extendableObject.getClass());
 		
 		
 		for (ListPropertyAccessor<?> listProperty: propertySet.getListProperties3()) {
 			/*
 			 * Find all elements in the list. The child elements will almost
 			 * certainly already be cached in memory so this is unlikely to
 			 * result in any queries being sent to the database.
 			 */
 			for (ExtendableObject child: extendableObject.getListPropertyValue(listProperty)) {
				deleteFromDatabase((IDatabaseRowKey)child.getObjectKey());
 			}
 		}
 	}
 
 	/**
 	 * Construct an object with default property values.
 	 * 
 	 * @param propertySet
 	 * @param objectKey The key to this object.  This is required by this
 	 * 			method because it must be passed to the constructor.
 	 * 			This method does not call the setObject or setRowId
 	 * 			methods on this key.  It is the caller's responsibility
 	 * 			to call these methods.
 	 * @param parent
 	 * @return
 	 */
 	public <E extends ExtendableObject> E constructExtendableObject(ExtendablePropertySet<E> propertySet, IDatabaseRowKey objectKey, ListKey<? super E> listKey) {
 		E extendableObject = propertySet.constructDefaultImplementationObject(objectKey, listKey);
 		
 		setMaterializedObject(getBasemostPropertySet(propertySet), objectKey.getRowId(), extendableObject);
 		
 		return extendableObject;
 	}
 
 	private <E2 extends ExtendableObject> IListManager<E2> createListManager(DatabaseListKey<E2> listKey) {
 		return new ListManagerCached<E2>(this, listKey, true);
 	}
 
 	/**
 	 * Construct an object with the given property values.
 	 * 
 	 * @param propertySet
 	 * @param objectKey The key to this object.  This is required by this
 	 * 			method because it must be passed to the constructor.
 	 * 			This method does not call the setObject or setRowId
 	 * 			methods on this key.  It is the caller's responsibility
 	 * 			to call these methods.
 	 * @param parent
 	 * @param values the values of the scalar properties to be set into this object,
 	 * 			with ExtendableObject properties having the object key in this array 
 	 * @return
 	 */
 	public <E extends ExtendableObject> E constructExtendableObject(ExtendablePropertySet<E> propertySet, IDatabaseRowKey objectKey, DatabaseListKey<? super E> listKey, IValues values) {
 		E extendableObject = propertySet.constructImplementationObject(objectKey, constructListKey(listKey), values);
 
 		setMaterializedObject(getBasemostPropertySet(propertySet), objectKey.getRowId(), extendableObject);
 		
 		return extendableObject;
 	}
 
 	/**
 	 * Materialize an object from a row of data.
 	 * <P>
 	 * This version of this method should be called when
 	 * the caller knows the parent of the object to
 	 * be materialized (or at least, the key to the parent).
 	 * The parent key is passed to this method by the caller
 	 * and that saves this method from needing to build a
 	 * new parent key from the object's data in the database.
 	 *
 	 * @param rs
 	 * @param propertySet
 	 * @param objectKey
 	 * @param parentKey
 	 * @return
 	 * @throws SQLException
 	 */ 
 	<E extends ExtendableObject> E materializeObject(final ResultSet rs, final ExtendablePropertySet<E> propertySet, final IDatabaseRowKey objectKey, ListKey<? super E> listKey) throws SQLException {
 		/**
 		 * The list of parameters to be passed to the constructor
 		 * of this object.
 		 */
 		IValues values = new IValues() {
 
 			public <V> V getScalarValue(ScalarPropertyAccessor<V> propertyAccessor) {
 				String columnName = getColumnName(propertyAccessor);
 
 				try {
 				Class<V> valueClass = propertyAccessor.getClassOfValueObject(); 
 				if (valueClass == Character.class) {
 					return valueClass.cast(rs.getString(columnName).charAt(0));
 				} else if (valueClass == Long.class) {
 					return valueClass.cast(rs.getLong(columnName));
 				} else if (valueClass == Integer.class) {
 					return valueClass.cast(rs.getInt(columnName));
 				} else if (valueClass == String.class) {
 					return valueClass.cast(rs.getString(columnName));
 				} else if (valueClass == Boolean.class) {
 					return valueClass.cast(rs.getBoolean(columnName));
 				} else if (valueClass == Date.class) {
 					return valueClass.cast(rs.getDate(columnName));
 				} else {
 					/*
 					 * Must be a user defined object.  Construct it using
 					 * the string constructor.
 					 */
 					String text = rs.getString(columnName);
 					if (rs.wasNull() || text.length() == 0) {
 						return null;
 					} else {
 						/*
 						 * The property value is an class that is in none of the
 						 * above categories. We therefore use the string
 						 * constructor to construct the object.
 						 */
 						try {
 							return valueClass
 							.getConstructor( new Class [] { String.class } )
 							.newInstance(new Object [] { text });
 						} catch (InvocationTargetException e) {
 							/*
 							 * An exception was thrown in the constructor. Log
 							 * the original exception. We then set the value to
 							 * null and continue on the basis that it is better
 							 * for the user to lose the value (which was
 							 * probably corrupted anyway) than to have the
 							 * entire accounting datastore unreadable.
 							 */ 
 							 // TODO: There is a problem with this.  Optimistic locking
 							 // fails.  Any attempt to update this row in the database
 							 // will generate an update that tests for the value being
 							 // null.  However, the value was in actual fact not null.
 							e.getCause().printStackTrace();
 							return null;
 						} catch (Exception e) {
 							/*
 							 * The classes used in the data model should be
 							 * checked when the PropertySet and PropertyAccessor
 							 * static fields are initialized. Therefore other
 							 * plug-ins should not be able to cause an error
 							 * here. 
 							 */
 							 // TODO: put the above mentioned check into
 							 // the initialization code.
 							e.printStackTrace();
 							throw new RuntimeException("internal error");
 						}
 					}
 				}
 				} catch (SQLException e) {
 					e.printStackTrace();
 					throw new RuntimeException("database error");
 				}
 			}
 			
 			public IObjectKey getReferencedObjectKey(ReferencePropertyAccessor<?> propertyAccessor) {
 				String columnName = getColumnName(propertyAccessor);
 				try {
 					int rowIdOfProperty = rs.getInt(columnName);
 					if (rs.wasNull()) {
 						return null;
 					} else {
 						ExtendablePropertySet<? extends ExtendableObject> propertySetOfProperty = PropertySet.getPropertySet(propertyAccessor.getClassOfValueObject());
 
 						/*
 						 * We must obtain an object key.  However, we do not have to create
 						 * the object or obtain a reference to the object itself at this time.
 						 * Nor do we want to for performance reasons.
 						 */
 						return new ObjectKey(rowIdOfProperty, propertySetOfProperty, SessionManager.this);
 					}
 				} catch (SQLException e) {
 					e.printStackTrace();
 					throw new RuntimeException("database error");
 				}
 			}
 
 			public <E2 extends ExtendableObject> IListManager<E2> getListManager(IObjectKey listOwnerKey, ListPropertyAccessor<E2> listAccessor) {
 				return objectKey.constructListManager(listAccessor);
 			}
 
 			public Collection<ExtensionPropertySet<?>> getNonDefaultExtensions() {
 				/*
 				 * In order to find out which extensions have non-default values, we have to read the property
 				 * values from the rowset and compare against the default values given by the accessor.
 				 * Note that the values may be null so we use the utility method to do the comparison.
 				 */
 				Collection<ExtensionPropertySet<?>> nonDefaultExtensions = new Vector<ExtensionPropertySet<?>>();
 				outerLoop: for (ExtensionPropertySet<?> extensionPropertySet: propertySet.getExtensionPropertySets()) {
 					boolean nonDefaultValueFound = false;
 					for (ScalarPropertyAccessor accessor: extensionPropertySet.getScalarProperties1()) {
 						// TODO: Complete this implementation, if it is worth it.  On the other hand, perhaps it
 						// is not unacceptable to always create extensions.
 						if (true) {
 //						if (!JMoneyPlugin.areEqual(getValue(rs, accessor), accessor.getDefaultValue())) {
 							nonDefaultExtensions.add(extensionPropertySet);
 							continue outerLoop;
 						}
 					}
 					for (ListPropertyAccessor accessor: extensionPropertySet.getListProperties1()) {
 						// For time being, always create an extension if there is a list property in it.
 						nonDefaultExtensions.add(extensionPropertySet);
 						continue outerLoop;
 					}
 				}
 				
 				return nonDefaultExtensions;
 			}
 
 		};
 		
 		E extendableObject = propertySet.constructImplementationObject(objectKey, listKey, values);
 		
 		setMaterializedObject(getBasemostPropertySet(propertySet), objectKey.getRowId(), extendableObject);
 		
 		return extendableObject;
 	}
 
 	/**
 	 * Given a property, return the name of the database column that holds the
 	 * values of the property.
 	 * 
 	 * Unless the property is an extension property, we simply use the
 	 * unqualified name. That must be unique within the property set and so the
 	 * column name will be unique within the table. If the property is an
 	 * extension property, however, then the name must be fully qualified
 	 * because two plug-ins may use the same name for an extension property and
 	 * these two properties cannot have the same column name as they are in the
 	 * same table. The the dots are replaced by underscores to keep the names
 	 * SQL compliant.
 	 * 
 	 * @param propertyAccessor
 	 * @return an SQL compliant column name, guaranteed to be unique within the
 	 *         table
 	 */
 	String getColumnName(ScalarPropertyAccessor<?> propertyAccessor) {
 		if (propertyAccessor.getPropertySet().isExtension()) {
 			return propertyAccessor.getName().replace('.', '_');
 		} else {
 			return propertyAccessor.getLocalName();
 		}
 	}
 
 	/**
 	 * Materialize an object from a row of data.
 	 * <P>
 	 * This version of this method should be called when the caller does not
 	 * know the parent of the object to be materialized. This is the situation
 	 * if one object has a reference to another object (ie. the referenced
 	 * object is not in a list property) and we need to materialize the
 	 * referenced object.
 	 * <P>
 	 * The parent key is built from data in the row.
 	 * 
 	 * @param rs
 	 * @param propertySet
 	 * @param key
 	 * @return
 	 * @throws SQLException
 	 */
 	<E extends ExtendableObject> E materializeObject(ResultSet rs, ExtendablePropertySet<E> propertySet, IDatabaseRowKey key) throws SQLException {
 		/*
 		 * We need to obtain the key for the containing list.  We do this by
 		 * creating one from the data in the result set.
 		 */ 
 		DatabaseListKey<? super E> parentListKey = buildParentKey(rs, propertySet);
 		
 		ListKey<? super E> listKey = constructListKey(parentListKey);
 		
 		E extendableObject = materializeObject(rs, propertySet, key, listKey);
 		
 		return extendableObject;
 	}
 
 	/**
 	 * Helper method.
 	 */
 	<E extends ExtendableObject> ListKey<E> constructListKey(DatabaseListKey<E> parentListKey) {
 		return new ListKey<E>(parentListKey.parentKey, parentListKey.listPropertyAccessor);
 	}
 	
 	/**
 	 * Used for returning result from following method, as Java does not allow methods to
 	 * return more than a single value.
 	 */
 	static class DatabaseListKey<E extends ExtendableObject> {
 		IDatabaseRowKey parentKey;
 		ListPropertyAccessor<E> listPropertyAccessor;
 		
 		public static <E extends ExtendableObject> DatabaseListKey<E> construct(IDatabaseRowKey parentKey, ListPropertyAccessor<E> listProperty) {
 			return new DatabaseListKey<E>(parentKey, listProperty);
 		}
 
 		public DatabaseListKey(IDatabaseRowKey parentKey, ListPropertyAccessor<E> listPropertyAccessor) {
 			this.parentKey = parentKey;
 			this.listPropertyAccessor = listPropertyAccessor;
 		}
 	}
 	
 	/*
 	 * We need to obtain the key for the parent object.  We do this by
 	 * creating one from the data in the result set.
 	 * 
 	 * The property set of the parent object may not be known without
 	 * looking at the row data. For example, the parent of an account may be
 	 * another account (if the account is a sub-account) or may be the
 	 * session.
 	 */
 	<E extends ExtendableObject> DatabaseListKey<? super E> buildParentKey(ResultSet rs, ExtendablePropertySet<E> propertySet) throws SQLException {
 		/* 
 		 * A column exists in this table for each list which can contain objects
 		 * of this type. Only one of these columns can be non-null so we must
 		 * find that column. The value of that column will be the integer id of
 		 * the parent.
 		 * 
 		 * An optimization allows the column to be absent when the parent
 		 * object is the session object (as only one session object may exist).
 		 * 
 		 * For each list that may contain this object, see if the appropriate
 		 * column is non-null.
 		 */
 		ParentList matchingParentList = null;
 		int parentId = -1;
 		boolean nonNullValueFound = false;
 		
 		ExtendablePropertySet<? super E> propertySet2 = propertySet;
 		do {
 			Vector<ParentList> list = tablesMap.get(propertySet2);
 
 			/*
 			 * Find all properties in any property set that are a list of objects
 			 * with the type as this property set. A column must exist in this table
 			 * for each such property that exists in another property set.
 			 */
 			for (ParentList parentList: list) {
 				parentId = rs.getInt(parentList.getColumnName());
 				if (!rs.wasNull()) {		
 					matchingParentList = parentList;
 					nonNullValueFound = true;
 					break;
 				}
 			}	
 			propertySet2 = propertySet2.getBasePropertySet();
 		} while (propertySet2 != null);	
 			
 		DatabaseListKey<? super E> listKey;
 
 		if (!nonNullValueFound) {
 			/*
 			 * A database optimization causes no parent column to exist for the
 			 * case where the parent object is the session.
 			 */
 			ListPropertyAccessor<? super E> listProperty;
 			if (Commodity.class.isAssignableFrom(propertySet.getImplementationClass())) {
 				listProperty = (ListPropertyAccessor<? super E>)SessionInfo.getCommoditiesAccessor();
 			} else if (Account.class.isAssignableFrom(propertySet.getImplementationClass())) {
 				listProperty = (ListPropertyAccessor<? super E>)SessionInfo.getAccountsAccessor();
 			} else if (Transaction.class.isAssignableFrom(propertySet.getImplementationClass())) {
 				listProperty = (ListPropertyAccessor<? super E>)SessionInfo.getTransactionsAccessor();
 			} else {
 				throw new RuntimeException("bad case");
 			}
 			listKey = DatabaseListKey.construct(sessionKey, listProperty);
 		} else {
 			IDatabaseRowKey parentKey = new ObjectKey(parentId, matchingParentList.parentPropertySet, this);
 			ListPropertyAccessor<? super E> listProperty = matchingParentList.listProperty;
 			listKey = DatabaseListKey.construct(parentKey, listProperty);
 		}
 		
 		return listKey;
 	}
 
 	class ColumnInfo {
 		String columnName;
 		String columnDefinition;
 		ExtendablePropertySet<?> foreignKeyPropertySet = null;
 		ColumnNature nature;
 	}
 	
 	private enum ColumnNature {
 		PARENT,
 		SCALAR_PROPERTY
 	}
 	
 	/**
 	 * Build a list of columns that we must have in the table that
 	 * holds the data for a particular property set.
 	 * <P>
 	 * The list will depend on the set of installed plug-ins.
 	 * <P>
 	 * The "_ID" column is required in all tables as a primary
 	 * key and is not returned by this method.
 	 * 
 	 * @return A Vector containing objects of class 
 	 * 		<code>ColumnInfo</code>.
 	 */
 	private Vector<ColumnInfo> buildColumnList(ExtendablePropertySet<?> propertySet) {
 		Vector<ColumnInfo> result = new Vector<ColumnInfo>();
 		
 		/*
 		 * The parent column requirements depend on which other property sets
 		 * have lists. A parent column exists in the table for property set A
 		 * for each list property (in any property set) that contains elements
 		 * of type A.
 		 * 
 		 * If there is a single place where the property set is listed and that
 		 * place is in the session object (or an extension thereof) then no
 		 * parent column is necessary because there is only one session object.
 		 * 
 		 * If there is a single place where the property set is listed and that
 		 * place is not in the session object (or an extension thereof) then a
 		 * parent column is created with the name being the same as the fully
 		 * qualified name of the property that lists these objects. The column
 		 * will not allow null values.
 		 * 
 		 * If there are multiple places where a property set is listed then a
 		 * column is created for each place (but if one of the places is the
 		 * session object that no column is created for that place). The columns
 		 * will allow null values. At most one of the columns may be non-null.
 		 * If all the columns are null then the parent is the session object.
 		 * The names of the columns are the fully qualified names of the
 		 * properties that list these objects.
 		 */
 		Vector<ParentList> list = tablesMap.get(propertySet);
 		
 		/*
 		 * Find all properties in any property set that are a list of objects
 		 * with the element type as this property set. A column must exist in
 		 * this table for each such property that exists in another property
 		 * set.
 		 * 
 		 * These columns default to NULL so that, when objects are inserted,
 		 * we need only to set the parent id into the appropriate column and
 		 * not worry about the other parent columns.
 		 * 
 		 * If there is only one list property of a type in which an object could
 		 * be placed, then we could make the column NOT NULL.  However, we would
 		 * need more code to adjust the database schema (altering columns to be
 		 * NULL or NOT NULL) if plug-ins are added to create other lists in which
 		 * the object could be placed. 
 		 */
 		for (ParentList parentList: list) {
 			ColumnInfo info = new ColumnInfo();
 			info.nature = ColumnNature.PARENT;
 			info.columnName = parentList.getColumnName();
 			info.columnDefinition = "INT DEFAULT NULL NULL";
 			info.foreignKeyPropertySet = parentList.parentPropertySet;
 			result.add(info);
 		}
 		
 		// The columns for each property in this property set
 		// (including the extension property sets).
 		for (ScalarPropertyAccessor<?> propertyAccessor: propertySet.getScalarProperties2()) {
 			ColumnInfo info = new ColumnInfo();
 
 			info.nature = ColumnNature.SCALAR_PROPERTY;
 			info.columnName = getColumnName(propertyAccessor);
 
 			Class<?> valueClass = propertyAccessor.getClassOfValueObject();
 			if (valueClass == Integer.class) {
 				info.columnDefinition = "INT";
 			} else if (valueClass == Long.class) {
 				info.columnDefinition = "BIGINT";
 			} else if (valueClass == Character.class) {
 				info.columnDefinition = "CHAR";
 			} else if (valueClass == Boolean.class) {
 				info.columnDefinition = "BIT";
 			} else if (valueClass == String.class) {
 				/*
 				 * HSQLDB is fine with just VARCHAR, but MS SQL will default the
 				 * maximum length to 1 which is obviously no good. We therefore
 				 * specify the maximum length as 255.
 				 */
 				info.columnDefinition = "VARCHAR(255)";
 			} else if (valueClass == Date.class) {
 				/*
 				 * Although some databases support date types that may be
 				 * better suited for dates without times (MS SQL has SMALLDATETIME
 				 * and HSQLDB has DATE), only DATETIME is standard and should
 				 * be supported by all JDBC implementations.
 				 */
 				info.columnDefinition = "DATETIME";  
 			} else if (ExtendableObject.class.isAssignableFrom(valueClass)) {
 				info.columnDefinition = "INT";
 
 				// This call does not work.  The method works only when the class
 				// is a class of an actual object and only non-derivable property
 				// sets are returned.
 				// info.foreignKeyPropertySet = PropertySet.getPropertySet(valueClass);
 
 				// This works.
 				// The return type from a getter for a property that is a reference
 				// to an extendable object must be the getter interface.
 				info.foreignKeyPropertySet = null;
 				for (ExtendablePropertySet<?> propertySet2: PropertySet.getAllExtendablePropertySets()) {
 					if (propertySet2.getImplementationClass() == valueClass) {
 						info.foreignKeyPropertySet = propertySet2;
 						break;
 					}
 				}
 			} else { 
 				// All other types are stored as a string by 
 				// using the String constructor and
 				// the toString method for conversion.
 				
 				// HSQL is fine with just VARCHAR, but MS SQL will default
 				// the maximum length to 1 which is obviously no good.
 				info.columnDefinition = "VARCHAR(255)";
 			}
 
 			// If the property is an extension property then we set
 			// a default value.  This saves us from having to set default
 			// value in every insert statement and is a better solution
 			// if other applications (outside JMoney) access the database.
 
 			if (propertyAccessor.getPropertySet().isExtension()) {
 				Object defaultValue = propertyAccessor.getDefaultValue();
 				info.columnDefinition +=
 					" DEFAULT " + valueToSQLText(defaultValue);
 			}
 
 			if (propertyAccessor.isNullAllowed()) {
 				info.columnDefinition += " NULL";
 			} else {
 				info.columnDefinition += " NOT NULL";
 			}
 			result.add(info);
 		}
 		
 		/*
 		 * If the property set is a derivable property set and is the base-most
 		 * property set then we must have a column called _PROPERTY_SET. This
 		 * column contains the id of the actual (non-derivable) property set of
 		 * this object. This column is required because otherwise we would not
 		 * know which further tables need to be joined to get the complete set
 		 * of properties with which we can construct the object.
 		 */
 		if (propertySet.getBasePropertySet() == null
 		 && propertySet.isDerivable()) {
 			ColumnInfo info = new ColumnInfo();
 			info.columnName = "_PROPERTY_SET";
 			// 200 should be enough for property set ids.
 			info.columnDefinition = "VARCHAR(200) NOT NULL";
 			result.add(info);
 		}
 		
 		return result;
 	}
 	
 	private static String[] tableOnlyType = new String[] { "TABLE" };
 
 	private void traceResultSet(ResultSet rs) {
 		if (JDBCDatastorePlugin.DEBUG) {
 			try {
 				String x = "";		
 				ResultSetMetaData rsmd = rs.getMetaData();
 				int cols = rsmd.getColumnCount();
 				for (int i = 1; i <= cols; i++) {
 					x += rsmd.getColumnLabel(i) + ", ";
 				}
 				System.out.println(x);
 
 				while (rs.next()) {
 					x = "";
 					for (int i = 1; i <= cols; i++) {
 						x += rs.getString(i) + ", ";
 					}
 					System.out.println(x);
 				}
 			} catch (Exception SQLException) {
 				throw new RuntimeException("database error");
 			}
 			System.out.println("");
 		}
 	}
 
 	/**
 	 * Check the tables and columns in the database.
 	 * If a required table does not exist it will be created.
 	 * If a table exists but it does not contain all the required
 	 * columns, then the required columns will be added to the table.
 	 * <P>
 	 * There may be additional tables and there may be
 	 * additional columns in the required tables.  These are
 	 * ignored and the data in them are left alone.
 	 */
 	private void checkDatabase(Connection con, Statement stmt) throws SQLException {
 		
 		DatabaseMetaData dmd = con.getMetaData();
 		
 		for (ExtendablePropertySet<?> propertySet: PropertySet.getAllExtendablePropertySets()) {
 			String tableName = propertySet.getId().replace('.', '_');
 
 			// Check that the table exists.
 			ResultSet tableResultSet = dmd.getTables(null, null, tableName.toUpperCase(), tableOnlyType);
 
 			if (tableResultSet.next()) {
 				Vector<ColumnInfo> columnInfos = buildColumnList(propertySet);
 				for (ColumnInfo columnInfo: columnInfos) {
 					ResultSet columnResultSet = dmd.getColumns(null, null, tableName.toUpperCase(), columnInfo.columnName);
 					if (columnResultSet.next()) {
 						// int dataType = columnResultSet.getInt("DATA_TYPE");
 						// String typeName = columnResultSet.getString("TYPE_NAME");
 						// TODO: Check that the column information is
 						// correct.  Display a fatal error if it is not.
 					} else {
 						// The column does not exist so we add it.
 						String sql = 
 							"ALTER TABLE " + tableName
 							+ " ADD \"" + columnInfo.columnName
 							+ "\" " + columnInfo.columnDefinition;
 						System.out.println(sql);
 						stmt.execute(sql);	
 					}
 					columnResultSet.close();
 				}
 			} else {
 				// Table does not exist, so create it.
 				createTable(propertySet, stmt);
 			}
 
 			tableResultSet.close();
 		}
 		
 		/*
 		 * Having ensured that all the tables exist, now create the foreign key
 		 * constraints. This must be done in a second pass because otherwise we
 		 * might try to create a foreign key constraint before the foreign key
 		 * has been created.
 		 */
 		for (ExtendablePropertySet<?> propertySet: PropertySet.getAllExtendablePropertySets()) {
 			String tableName = propertySet.getId().replace('.', '_');
 
 			/*
 			 * Check the foreign keys in derived tables that point to the base
 			 * table row.
 			 */
 			if (propertySet.getBasePropertySet() != null) {
 				String primaryTableName = propertySet.getBasePropertySet().getId().replace('.', '_');
 				checkForeignKey(dmd, stmt, tableName, "_ID", primaryTableName, true);
 			}
 
 			/*
 			 * Check the foreign keys for columns that reference other objects.
 			 * 
 			 * These may be:
 			 *  - objects that contain references (as scalar properties) to
 			 * other objects.
 			 *  - the columns that contain the id of the parent object
 			 */
 			Vector<ColumnInfo> columnInfos = buildColumnList(propertySet);
 			for (ColumnInfo columnInfo: columnInfos) {
 				if (columnInfo.foreignKeyPropertySet != null) {
 					String primaryTableName = columnInfo.foreignKeyPropertySet.getId().replace('.', '_');
 					// TODO: check if HSQL allows ON CASCADE DELETE with a self-referencing table
 					checkForeignKey(dmd, stmt, tableName, columnInfo.columnName, primaryTableName, false/*columnInfo.nature == ColumnNature.PARENT*/);
 				}
 			}
 		}		
 	}
 
 	/**
 	 * Checks that the given foreign key exists.  If it does not, it is
 	 * added.
 	 * <P>
 	 * There may be other foreign keys between the two tables.  That is
 	 * ok.
 	 * 
 	 * @param stmt
 	 * @param dmd
 	 * @param tableName
 	 * @param columnName
 	 * @param primaryTableName
 	 */
 	private void checkForeignKey(DatabaseMetaData dmd, Statement stmt, String tableName, String columnName, String primaryTableName, boolean onDeleteCascade) throws SQLException {
 		ResultSet columnResultSet2 = dmd.getCrossReference(null, null, primaryTableName.toUpperCase(), null, null, tableName.toUpperCase());
 		traceResultSet(columnResultSet2);
 		columnResultSet2.close();
 
 		ResultSet columnResultSet = dmd.getCrossReference(null, null, primaryTableName.toUpperCase(), null, null, tableName.toUpperCase());
 		try {
 			while (columnResultSet.next()) {
 
 				if (columnResultSet.getString("FKCOLUMN_NAME").equalsIgnoreCase(columnName)) {
 
 					// TODO: There seems to be a mixture of _id and _ID.  SQL is not case sensitive
 					// so do a case insensitive comparison.
 					if (columnResultSet.getString("PKCOLUMN_NAME").equalsIgnoreCase("_ID")) {
 						// Foreign key found, so we are done.
 						return;
 					} else {
 						throw new RuntimeException("The database schema is invalid.  "
 								+ "Table " + tableName.toUpperCase() + " contains a foreign key column called " + columnName
 								+ " but it is not constrained to primary key _ID in table " + primaryTableName.toUpperCase() 
 								+ " as it should be.");
 					}
 				}
 			}
 
 			// The foreign key constraint does not exist so we add it.
 			String sql = 
 				"ALTER TABLE " + tableName
 				+ " ADD FOREIGN KEY (\"" + columnName
 				+ "\") REFERENCES " + primaryTableName + "(_ID)";
 
 			if (onDeleteCascade) {
 				sql += " ON DELETE CASCADE";
 			}
 
 			System.out.println(sql);
 			stmt.execute(sql);	
 		} finally {
 			columnResultSet.close();
 		}
 	}
 
 	/**
 	 * Create a table.  This method should be called when
 	 * a new database is being initialized or when a new
 	 * table is needed because a new extendable property 
 	 * set has been added.
 	 *
 	 * This method does not create any foreign keys.  This is because
 	 * the referenced table may be yet exist.  The caller must create
 	 * the foreign keys in a second pass.
 	 * 
 	 * @param propertySet The property set whose table is to
 	 * 			be created.  This property set must not be an
 	 * 			extension property set.  (No tables exist for extension
 	 * 			property sets.  Extension property sets are supported
 	 * 			by adding columns to the tables for the property sets
 	 * 			which they extend).
 	 * @param stmt A <code>Statement</code> object 
 	 * 			that is to be used by this method
 	 * 			to submit the 'CREATE TABLE' command.
 	 * @throws SQLException
 	 */
 	void createTable(ExtendablePropertySet<?> propertySet, Statement stmt) throws SQLException {
 		/*
 		 * The _ID column is always a primary key. However, it has automatically
 		 * generated values only for the base tables. Derived tables contain ids
 		 * that match the base table.
 		 * 
 		 * HSQLDB requires only IDENTITY be specified for the _ID column and it
 		 * is by default a primary key. MS SQL requires that PRIMARY KEY be
 		 * specifically specified. HSQLDB allows PRIMARY KEY provided it appears
 		 * after IDENTITY. We can keep both databases happy by specifically
 		 * including PRIMARY KEY after IDENTITY.
 		 */
 		String sql = "CREATE TABLE "
 			+ propertySet.getId().replace('.', '_') 
 			+ " (_id INT";
 		
 		if (propertySet.getBasePropertySet() == null) {
 			sql += " IDENTITY";
 		}
 		
 		sql += " PRIMARY KEY";
 		
 		Vector<ColumnInfo> columnInfos = buildColumnList(propertySet);
 		for (ColumnInfo columnInfo: columnInfos) {
 			sql += ", \"" + columnInfo.columnName + "\" " + columnInfo.columnDefinition;
 		}
 		sql += ")";
 		
 		System.out.println(sql);
 		stmt.execute(sql);
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sf.jmoney.model2.ISessionManager#hasEntries(net.sf.jmoney.model2.Account)
 	 */
 	@Override
 	public boolean hasEntries(Account account) {
 		// TODO: improve efficiency of this??????
 		// or should hasEntries be removed altogether and make caller
 		// call getEntries().isEmpty() ??????
 		// As long as collections are not being copied unneccessarily,
 		// this is probably better.
 		return !(new AccountEntriesList(this, (IDatabaseRowKey)account.getObjectKey()).isEmpty());
 	}
 
 	@Override
 	public Collection<Entry> getEntries(Account account) {
 		return new AccountEntriesList(this, (IDatabaseRowKey)account.getObjectKey());
 	}
 
 	/**
 	 * @see net.sf.jmoney.model2.IEntryQueries#sumOfAmounts(net.sf.jmoney.model2.CurrencyAccount, java.util.Date, java.util.Date)
 	 */
 	public long sumOfAmounts(CurrencyAccount account, Date fromDate, Date toDate) {
 		IDatabaseRowKey proxy = (IDatabaseRowKey)account.getObjectKey();
 		
 		try {
 			String sql = "SELECT SUM(amount) FROM net_sf_jmoney_entry, net_sf_jmoney_transaction"
 				+ " WHERE account = " + proxy.getRowId()
 				+ " AND date >= " + fromDate
 				+ " AND date <= " + toDate;
 			System.out.println(sql);
 			ResultSet rs = getReusableStatement().executeQuery(sql);
 			rs.next();
 			return rs.getLong(0);
 		} catch (SQLException e) {
 			throw new RuntimeException("SQL statement failed");
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sf.jmoney.model2.IEntryQueries#getSortedReadOnlyCollection(net.sf.jmoney.model2.CapitalAccount, net.sf.jmoney.model2.PropertyAccessor, boolean)
 	 */
 	public Collection<Entry> getSortedEntries(CapitalAccount account, PropertyAccessor sortProperty, boolean descending) {
 		// TODO implement this.
 		throw new RuntimeException("must implement");
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sf.jmoney.model2.IEntryQueries#getEntryTotalsByMonth(int, int, int, boolean)
 	 */
 	public long[] getEntryTotalsByMonth(CapitalAccount account, int startYear, int startMonth, int numberOfMonths, boolean includeSubAccounts) {
 		IDatabaseRowKey proxy = (IDatabaseRowKey)account.getObjectKey();
 		
 		String startDateString = '\''
 			+ new Integer(startYear).toString() + "-"
 			+ new Integer(startMonth).toString() + "-"
 			+ new Integer(1).toString()
 			+ '\'';
 
 		int endMonth = startMonth + numberOfMonths;
 		int years = (endMonth - 1) / 12;
 		endMonth -= years * 12;
 		int endYear = startYear + years;
 		
 		String endDateString = '\''
 			+ new Integer(endYear).toString() + "-"
 			+ new Integer(endMonth).toString() + "-"
 			+ new Integer(1).toString()
 			+ '\'';
 
 		String accountList = "(" + proxy.getRowId();
 		if (includeSubAccounts) {
 			ArrayList<Integer> accountIds = new ArrayList<Integer>();
 			addEntriesFromSubAccounts(account, accountIds);
 			for (Integer accountId: accountIds) {
 				accountList += "," + accountId; 
 			}
 		}
 		accountList += ")";
 		
 		try {
 			String sql = "SELECT SUM(amount), DateSerial(Year(date),Month(date),1) FROM net_sf_jmoney_entry, net_sf_jmoney_transaction"
 				+ " GROUP BY DateSerial(Year(date),Month(date),1)"
 				+ " WHERE account IN " + accountList
 				+ " AND date >= " + startDateString
 				+ " AND date < " + endDateString
 				+ " ORDER BY DateSerial(Year(date),Month(date),1)";
 			System.out.println(sql);
 			ResultSet rs = getReusableStatement().executeQuery(sql);
 			
 			long [] totals = new long[numberOfMonths];
 			for (int i = 0; i < numberOfMonths; i++) {
 				rs.next();
 				totals[i] = rs.getLong(0);
 			}
 			return totals;
 		} catch (SQLException e) {
 			throw new RuntimeException("SQL statement failed");
 		}
 	}
 	
 	private void addEntriesFromSubAccounts(CapitalAccount account, ArrayList<Integer> accountIds) {
 		for (CapitalAccount subAccount: account.getSubAccountCollection()) {
 			IDatabaseRowKey proxy = (IDatabaseRowKey)subAccount.getObjectKey();
 			accountIds.add(proxy.getRowId());
 			addEntriesFromSubAccounts(subAccount, accountIds);
 		}
 	}
 
 	@Override
 	public void startTransaction() {
 		try {
 			connection.setAutoCommit(false);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void commitTransaction() {
 		try {
 			connection.commit();
 		} catch (SQLException e) {
 			// TODO We need a mechanism to log and report errors
 			e.printStackTrace();
 		}
 
 		/*
 		 * Note that we want to turn on auto-commit even if
 		 * the above commit failed.
 		 */
 		try {
 			connection.setAutoCommit(true);
 		} catch (SQLException e) {
 			// TODO We need a mechanism to log and report errors
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * This is a helper method to get the base-most property set for
 	 * a given property set.
 	 * 
 	 * @param propertySet
 	 * @return
 	 */
 	public static <E extends ExtendableObject> ExtendablePropertySet<? super E> getBasemostPropertySet(ExtendablePropertySet<E> propertySet) {
 		ExtendablePropertySet<? super E> basePropertySet = propertySet; 
 		while (basePropertySet.getBasePropertySet() != null) {
 			basePropertySet = basePropertySet.getBasePropertySet();
 		}
 		return basePropertySet;
 	}
 }
