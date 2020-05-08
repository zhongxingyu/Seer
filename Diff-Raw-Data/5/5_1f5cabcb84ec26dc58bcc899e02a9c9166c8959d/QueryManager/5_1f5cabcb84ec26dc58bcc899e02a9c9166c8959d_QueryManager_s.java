 package es.jafs.jaiberdroid;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 
 /**
  * Class that execute and control the querys.
  * @author  Jose Antonio Fuentes Santiago
  * @version 0.5
  * @todo    This class must receive only one query method, and analyzes whats method call.
  */
 final class QueryManager extends SQLiteOpenHelper {
 	/** Database version. */
 	private static int version = 0;
 	/** Database name. */
 	private static String name = "";
 
 	/** Instance of Entity Manager. */
 	private EntityManager entityManager;
 
 
 	/**
 	 * Default constructor of the class.
 	 * @throws JaiberdroidException 
 	 */
 	public QueryManager(final Context context, final EntityManager entityManager) throws JaiberdroidException {
 		super(context, name, null, version);
 		this.entityManager = entityManager;
 	}
 
 
 	/**
 	 * Called when the database is created for the first time.
 	 * @param  database  The database
 	 */
 	@Override
 	public void onCreate(final SQLiteDatabase database) {
 		if (!executeUpdates(entityManager.getCreateQueries(), true, database)) {
 			Log.e(JaiberdroidInstance.LOG_TAG, "Problem creating database.");
 		}
 	}
 
 
 	/**
 	 * Called when the database needs to be upgraded. This method executes within a transaction. If an
 	 * exception is thrown, all changes will automatically be rolled back.
 	 * @param  database    The database.
 	 * @param  oldVersion  Old version id.
 	 * @param  newVersion  New version id.
 	 */
 	@Override
 	public void onUpgrade(final SQLiteDatabase database, final int oldVersion, final int newVersion) {
 		// The false value in if executeUpdates call, is because this method creates automatically a
 		// transaction.
 		if (!executeUpdates(entityManager.getDropQueries(), false, database)) {
 			Log.e(JaiberdroidInstance.LOG_TAG, "Problem upgrading database.");
 		}
 		onCreate(database);
 	}
 
 
 	/**
 	 * Executes an update with received query.
 	 * @param  query  Query to execute.
 	 * @return Number of rows affected. -1 if there an error.
 	 * @throws JaiberdroidException 
 	 */
 	public long executeUpdate(final Query query) throws JaiberdroidException {
 		long rows = -1;
 
 		try {
 			final SQLiteDatabase database = getWritableDatabase();
 
 			if (query.isTransactional()) {
 				database.beginTransaction();
 			}
 
 			switch (query.getType()) {
 				// Inserts a value into the database.
 				case INSERT:
 					// Returns the row id of inserted data.
 					rows = (int) database.insert(query.getEntity().getTableName(), null, query.getValues());
 					if (-1 != rows) {
 						JaiberdroidReflection.executeSetMethod(JaiberdroidReflection.SET_ID, query.getObject(),
 															int.class, (int) rows);
 						rows = 1; // Affected 1 row.
 					}
 					break;
 
 				// Updates existing values into database.
 				case UPDATE:
 					rows = database.update(query.getEntity().getTableName(), query.getValues(),
 											query.getCondition(), query.getArgsArray());
 					break;
 
 				// Delete values of database.
 				case DELETE:
 					rows = database.delete(query.getEntity().getTableName(), query.getCondition(),
 										query.getArgsArray());
 					break;
 
 				default:
 					Log.w(JaiberdroidInstance.LOG_TAG, "Only Insert, Update, Delete are supported");
 			}
 
 			if (database.inTransaction()) {
 				if (rows != -1) {
 					database.setTransactionSuccessful();
 				}
 
 				database.endTransaction();
 			}
 		} catch (final SQLException e) {
 			Log.e(JaiberdroidInstance.LOG_TAG, "When executing update: " + e.getMessage(), e);
 			throw new JaiberdroidException("Executing SQL" + e.getMessage());
 		}
 
 		return rows;
 	}
 
 
 	/**
 	 * Execute a query in database.
 	 * @param       query        String with query to execute.
 	 * @param       database     Database into execute queries.
 	 * @deprecated  Not used in future versions. Please use new executeSql method.
 	 */
 	public void executeUpdate(final String query, final SQLiteDatabase database) throws SQLException {
 		database.execSQL(query);
 	}
 
 
 	/**
 	 * Execute a query in database.
 	 * @param  query        String with query to execute.
 	 * @return Object with results. Can be a List of String array or a single object.
 	 */
 	public List<String[]> executeSql(final String query) throws SQLException {
 		// TODO analyze the query
 		return executeSql(query, getWritableDatabase());
 	}
 
 
 	/**
 	 * Execute a query in database.
 	 * @param  query        String with query to execute.
 	 * @param  database     Database into execute queries.
 	 * @return Object with results. Can be a List of String array or a single object.
 	 */
 	private List<String[]> executeSql(final String query, final SQLiteDatabase database) throws SQLException {
 		final List<String[]> result = new ArrayList<String[]>();
 
 		try {
 			final Cursor cursor = database.rawQuery(query, null);
 			if (cursor.moveToFirst()) {
 				String[] row;
 
 				do {
 					row = new String[cursor.getColumnCount()];
 					for (int j = 0; j < cursor.getColumnCount(); ++j) {
 						row[j] = cursor.getString(j);
 					}
 					result.add(row);
 				} while (cursor.moveToNext());
 			}
 		} catch (final SQLException e) {
 			Log.e(JaiberdroidInstance.LOG_TAG, "Executing sql: " + e.getMessage(), e);
 			throw e;
 		}
 
 		return result;
 	}
 
 
 	/**
 	 * Execute a list of queries in database.
 	 * @param  database     Database into execute queries.
 	 * @param  queries      List of String with queries to execute.
 	 * @param  transaction  Boolean value that sets if the queries are executed in transacction. 
 	 */
 	private boolean executeUpdates(final List<String> queries, final boolean transaction,
 									final SQLiteDatabase database) {
 		boolean ok = false;
 
 		if (null != queries) {
 			try {
 				if (transaction) {
 					database.beginTransaction();
 				}
 	
 				try {
 					for (String query : queries) {
 						database.execSQL(query);
 					}
 	
					if (database.inTransaction()) {
 						database.setTransactionSuccessful();
 					}
 				} catch (final SQLException e) {
 					Log.e(JaiberdroidInstance.LOG_TAG, "When executing SQL: " + e.getMessage(), e);
 				}
 	
				if (database.inTransaction()) {
 					database.endTransaction();
 				}
 
 				ok = true;
 			} catch (final SQLException e) {
 				Log.e(JaiberdroidInstance.LOG_TAG, "Problem in update: " + e.getMessage(), e);
 			}
 		}
 
 		return ok;
 	}
 
 
 	/**
 	 * Executes a query that returns data of an entity.
 	 * @param  query  Query to execute.
 	 * @return List of results or null is there an error.
 	 * @throws JaiberdroidException 
 	 */
 	public List<Object> executeQueryEntity(final Query query) throws JaiberdroidException {
 		List<Object> results = null;
 
 		// Checks if query is SELECT type.
 		if (Query.Type.SELECT.equals(query.getType())) {
 			try {
 				final SQLiteDatabase database = getWritableDatabase();
 
 				final Cursor cursor = database.query(query.getEntity().getTableName(), query.getFields(),
 													query.getCondition(), query.getArgsArray(), null, null,
 													null);
 
 				if (cursor.moveToFirst()) {
 					results = new ArrayList<Object>();
 
 					do {
 						results.add(getObject(cursor, query.getEntity()));
 					} while (cursor.moveToNext());
 				}
 				cursor.close();
 			} catch (final SQLException e) {
 				Log.e(JaiberdroidInstance.LOG_TAG, "When executing a query: " + e.getMessage(), e);
 			}
 		}
 
 		return results;
 	}
 
 
 	/**
 	 * Executes a query that returns data of an entity.
 	 * @param  entity  Entity with table to count.
 	 * @return List of results or null is there an error.
 	 * @throws JaiberdroidException 
 	 */
 	public long executeCountQuery(final Entity entity) throws JaiberdroidException {
 		long count = 0;
 
 		try {
 			final SQLiteDatabase database = getWritableDatabase();
 
 			Cursor mCount= database.rawQuery(JaiberdroidSql.getCountSql(entity.getTableName()), null);
 			if (mCount.moveToFirst());
 			count= mCount.getLong(0);
 			mCount.close();
 		} catch (final SQLException e) {
 			Log.e(JaiberdroidInstance.LOG_TAG, "When executing a query: " + e.getMessage(), e);
 		}
 
 		return count;
 	}
 
 
 	@SuppressWarnings("rawtypes")
 	private Object getObject(final Cursor cursor, final Entity entity) throws JaiberdroidException {
 		Object result = null;
 
 		if (null != cursor && cursor.getCount() > 0) {
 			try {
 				result = entity.getReferenced().newInstance();
 				for (String column : cursor.getColumnNames()) {
 					Class type = entity.getFields().getFieldClass(column);
 					String name = JaiberdroidReflection.getMethodName(JaiberdroidReflection.SET_PREFIX, column);
 					int pos = cursor.getColumnIndex(column);
 
 					if (int.class.equals(type) || Integer.class.equals(type)) {
 						JaiberdroidReflection.executeSetMethod(name, result, type, cursor.getInt(pos));
 					} else if (long.class.equals(type) || Long.class.equals(type)) {
 						JaiberdroidReflection.executeSetMethod(name, result, type, cursor.getLong(pos));
 					} else if (String.class.equals(type)) {
 						JaiberdroidReflection.executeSetMethod(name, result, type, cursor.getString(pos));
 					} else if (float.class.getName().equals(type) || Float.class.getName().equals(type)) { 
 						JaiberdroidReflection.executeSetMethod(name, result, type, cursor.getFloat(pos));
 					} else if (double.class.getName().equals(type) || Double.class.getName().equals(type)) {
 						JaiberdroidReflection.executeSetMethod(name, result, type, cursor.getDouble(pos));
 					}
 				}
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return result;
 	}
 
 
 	/**
 	 * Gets the current version of database.
 	 * @return Integer with current version of database.
 	 */
 	public static final int getVersion() {
 		return version;
 	}
 
 
 	/**
 	 * Sets the current version of database.
 	 * @param  version  Integer with current version of database.
 	 */
 	public static final void setVersion(final int version) {
 		QueryManager.version = version;
 	}
 
 
 	/**
 	 * Gets a String with database's name.
 	 * @return String with database's name.
 	 */
 	public static final String getName() {
 		return name;
 	}
 
 
 	/**
 	 * Sets a String with database's name.
 	 * @param  name  String with database's name.
 	 */
 	public static final void setName(String name) {
 		QueryManager.name = name;
 	}
 }
