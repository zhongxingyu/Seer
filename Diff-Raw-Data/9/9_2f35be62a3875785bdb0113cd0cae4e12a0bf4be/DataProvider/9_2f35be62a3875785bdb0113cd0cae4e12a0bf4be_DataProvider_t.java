 package org.cniska.foosball.android;
 
 import android.content.*;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 import android.provider.BaseColumns;
 import android.provider.ContactsContract;
 import android.text.TextUtils;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * This class provides the means for accessing data in the local SQLite database.
  */
 public class DataProvider extends ContentProvider {
 
 	private static final String TAG = "DataProvider";
 
 	// Static variables
 	// ----------------------------------------
 
 	private static final String DATABASE_NAME = "foosball.db";
 	private static final int DATABASE_VERSION = 1;
 
 	public static final int LIMIT_PATH_POSITION = 2;
 
 	// URI codes.
 	private static final int MATCHES			= 0;
 	private static final int MATCH_ID			= 1;
 	private static final int PLAYERS			= 2;
 	private static final int PLAYER_ID			= 3;
 	private static final int PLAYER_STATS 		= 4;
 	private static final int PLAYER_ID_RATING	= 5;
 	private static final int RATINGS			= 6;
 	private static final int STATS 				= 7;
 
 	private static UriMatcher sUriMatcher;
 	private static Map<String, String> sMatchesProjectionMap;
 	private static Map<String, String> sPlayersProjectionMap;
 	private static Map<String, String> sStatsProjectionMap;
 	private static Map<String, String> sRatingsProjectionMap;
 
 	static {
 		// Create the URI matcher and add the necessary URI so that they can be matched later on.
 		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
 		sUriMatcher.addURI(DataContract.AUTHORITY, DataContract.Matches.CONTENT_PATH, MATCHES);
 		sUriMatcher.addURI(DataContract.AUTHORITY, DataContract.Matches.CONTENT_PATH + "/#", MATCH_ID);
 		sUriMatcher.addURI(DataContract.AUTHORITY, DataContract.Players.CONTENT_PATH, PLAYERS);
 		sUriMatcher.addURI(DataContract.AUTHORITY, DataContract.Players.CONTENT_PATH + "/#", PLAYER_ID);
 		sUriMatcher.addURI(DataContract.AUTHORITY, DataContract.Players.CONTENT_PATH + "/#/stats", PLAYER_STATS);
 		sUriMatcher.addURI(DataContract.AUTHORITY, DataContract.Players.CONTENT_PATH + "/#/rating", PLAYER_ID_RATING);
 		sUriMatcher.addURI(DataContract.AUTHORITY, DataContract.Ratings.CONTENT_PATH, RATINGS);
 		sUriMatcher.addURI(DataContract.AUTHORITY, DataContract.Stats.CONTENT_PATH, STATS);
 
 		// Create the projection map for the match table.
 		sMatchesProjectionMap = new HashMap<String, String>();
 		sMatchesProjectionMap.put(DataContract.Matches._ID, DataContract.Matches._ID);
 		sMatchesProjectionMap.put(DataContract.Matches.CREATED, DataContract.Matches.CREATED);
 		sMatchesProjectionMap.put(DataContract.Matches.DURATION, DataContract.Matches.DURATION);
 		sMatchesProjectionMap.put(DataContract.Matches.RANKED, DataContract.Matches.RANKED);
 
 		// Create the projection map for the player table.
 		sPlayersProjectionMap = new HashMap<String, String>();
 		sPlayersProjectionMap.put(DataContract.Players._ID, DataContract.Players._ID);
 		sPlayersProjectionMap.put(DataContract.Players.CREATED, DataContract.Players.CREATED);
 		sPlayersProjectionMap.put(DataContract.Players.NAME, DataContract.Players.NAME);
 
 		// Create the projection map for the stats table.
 		sStatsProjectionMap = new HashMap<String, String>();
 		sStatsProjectionMap.put(DataContract.Stats._ID, DataContract.Stats._ID);
 		sStatsProjectionMap.put(DataContract.Stats.CREATED, DataContract.Stats.CREATED);
 		sStatsProjectionMap.put(DataContract.Stats.MATCH_ID, DataContract.Stats.MATCH_ID);
 		sStatsProjectionMap.put(DataContract.Stats.PLAYER_ID, DataContract.Stats.PLAYER_ID);
 		sStatsProjectionMap.put(DataContract.Stats.GOALS_FOR, DataContract.Stats.GOALS_FOR);
 		sStatsProjectionMap.put(DataContract.Stats.GOALS_AGAINST, DataContract.Stats.GOALS_AGAINST);
 		sStatsProjectionMap.put(DataContract.Stats.SCORE, DataContract.Stats.GOALS_AGAINST);
 		sStatsProjectionMap.put(DataContract.Stats.TEAM, DataContract.Stats.TEAM);
 
 		// Create the projection map for the rating table.
 		sRatingsProjectionMap = new HashMap<String, String>();
 		sRatingsProjectionMap.put(DataContract.Ratings._ID, DataContract.Ratings._ID);
 		sRatingsProjectionMap.put(DataContract.Ratings.CREATED, DataContract.Ratings.CREATED);
 		sRatingsProjectionMap.put(DataContract.Ratings.PLAYER_ID, DataContract.Ratings.PLAYER_ID);
 		sRatingsProjectionMap.put(DataContract.Ratings.RATING, DataContract.Ratings.RATING);
 
 		sRatingsProjectionMap.put(DataContract.Players.NAME, DataContract.Players.NAME);
 	}
 
 	// Member variables
 	// ----------------------------------------
 
 	private DatabaseHelper mDatabaseHelper;
 
 	// Inner classes
 	// ----------------------------------------
 
 	/**
 	 * This class handles the database connection to the local SQLite database.
 	 */
 	private class DatabaseHelper extends SQLiteOpenHelper {
 
 		private static final String TAG = "DataProvider.DatabaseHelper";
 
 		private static final String CREATE_TABLE_MATCH	= "CREATE TABLE " + DataContract.Matches.TABLE_NAME + " ("
 				+ DataContract.Matches._ID				+ " INTEGER PRIMARY KEY,"
 				+ DataContract.Matches.CREATED			+ " INTEGER,"
 				+ DataContract.Matches.DURATION			+ " INTEGER,"
 				+ DataContract.Matches.RANKED			+ " INTEGER)";
 
 		private static final String CREATE_TABLE_PLAYER	= "CREATE TABLE " + DataContract.Players.TABLE_NAME + " ("
 				+ DataContract.Players._ID				+ " INTEGER PRIMARY KEY,"
 				+ DataContract.Players.CREATED			+ " INTEGER,"
 				+ DataContract.Players.NAME				+ " TEXT)";
 
 		private static final String CREATE_TABLE_STATS	= "CREATE TABLE " + DataContract.Stats.TABLE_NAME + " ("
 				+ DataContract.Stats._ID				+ " INTEGER PRIMARY KEY, "
 				+ DataContract.Stats.CREATED			+ " INTEGER,"
 				+ DataContract.Stats.MATCH_ID			+ " INTEGER,"
 				+ DataContract.Stats.PLAYER_ID 			+ " INTEGER,"
 				+ DataContract.Stats.GOALS_FOR			+ " INTEGER,"
 				+ DataContract.Stats.GOALS_AGAINST		+ " INTEGER,"
 				+ DataContract.Stats.SCORE				+ " INTEGER,"
 				+ DataContract.Stats.TEAM	 			+ " INTEGER)";
 
 		private static final String CREATE_TABLE_RATING	= "CREATE TABLE " + DataContract.Ratings.TABLE_NAME + " ("
 				+ DataContract.Ratings._ID				+ " INTEGER PRIMARY KEY,"
 				+ DataContract.Ratings.CREATED			+ " INTEGER,"
 				+ DataContract.Ratings.PLAYER_ID		+ " INTEGER,"
 				+ DataContract.Ratings.RATING			+ " INTEGER)";
 
 		private static final String DROP_TABLE_MATCH	= "DROP TABLE IF EXISTS " + DataContract.Matches.TABLE_NAME;
 		private static final String DROP_TABLE_PLAYER	= "DROP TABLE IF EXISTS " + DataContract.Players.TABLE_NAME;
 		private static final String DROP_TABLE_STATS	= "DROP TABLE IF EXISTS " + DataContract.Stats.TABLE_NAME;
 		private static final String DROP_TABLE_RATING	= "DROP TABLE IF EXISTS " + DataContract.Ratings.TABLE_NAME;
 
 		/**
 		 * Creates a database helper.
 		 * @param context
 		 */
 		DatabaseHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL(CREATE_TABLE_MATCH);
 			db.execSQL(CREATE_TABLE_PLAYER);
 			db.execSQL(CREATE_TABLE_STATS);
 			db.execSQL(CREATE_TABLE_RATING);
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			Logger.warn(TAG, "Upgrading database from version " + oldVersion + " to "
 					+ newVersion + ", which will destroy all old data");
 
 			db.execSQL(DROP_TABLE_MATCH);
 			db.execSQL(DROP_TABLE_PLAYER);
 			db.execSQL(DROP_TABLE_STATS);
 			db.execSQL(DROP_TABLE_RATING);
 
 			onCreate(db);
 		}
 	}
 
 	// Methods
 	// ----------------------------------------
 
 	@Override
 	public boolean onCreate() {
 		mDatabaseHelper = new DatabaseHelper(getContext());
 		return true;
 	}
 
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
 		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
 		String groupBy = null;
 		String having = null;
 		String limit = null;
 
 		switch (sUriMatcher.match(uri)) {
 			// Query for multiple matches.
 			case MATCHES:
 				qb.setTables(DataContract.Matches.TABLE_NAME);
 				qb.setProjectionMap(sMatchesProjectionMap);
 				sortOrder = sortOrder != null ? sortOrder : DataContract.Matches.DEFAULT_SORT_ORDER;
 				break;
 
 			// Query for a single match.
 			case MATCH_ID:
 				qb.setTables(DataContract.Matches.TABLE_NAME);
 				qb.setProjectionMap(sMatchesProjectionMap);
 				qb.appendWhere(DataContract.Matches._ID + "="
 						+ uri.getPathSegments().get(DataContract.Matches.ID_PATH_POSITION));
 				sortOrder = sortOrder != null ? sortOrder : DataContract.Matches.DEFAULT_SORT_ORDER;
 				break;
 
 			// Query for multiple players.
 			case PLAYERS:
 				qb.setTables(DataContract.Players.TABLE_NAME);
 				qb.setProjectionMap(sPlayersProjectionMap);
 				sortOrder = sortOrder != null ? sortOrder : DataContract.Players.DEFAULT_SORT_ORDER;
 				break;
 
 			// Query for a single player.
 			case PLAYER_ID:
 				qb.setTables(DataContract.Players.TABLE_NAME);
 				qb.setProjectionMap(sPlayersProjectionMap);
 				qb.appendWhere(DataContract.Players._ID + "="
 						+ uri.getPathSegments().get(DataContract.Players.ID_PATH_POSITION));
 				sortOrder = sortOrder != null ? sortOrder : DataContract.Players.DEFAULT_SORT_ORDER;
 				break;
 
 			// Query for a single player's stats.
 			case PLAYER_STATS:
 				qb.setTables(DataContract.Stats.TABLE_NAME);
 				qb.setProjectionMap(sStatsProjectionMap);
 				qb.appendWhere(DataContract.Stats.PLAYER_ID + "="
 						+ uri.getPathSegments().get(DataContract.Players.ID_PATH_POSITION));
 				break;
 
 			// Query for a single player's current rating.
 			case PLAYER_ID_RATING:
 				qb.setTables(DataContract.Ratings.TABLE_NAME);
 				qb.setProjectionMap(sRatingsProjectionMap);
 				qb.appendWhere(DataContract.Ratings.PLAYER_ID + "="
 						+ uri.getPathSegments().get(DataContract.Players.ID_PATH_POSITION));
 				sortOrder = sortOrder != null ? sortOrder : DataContract.Ratings.DEFAULT_SORT_ORDER;
 				break;
 
 			case RATINGS:
 				qb.setTables(DataContract.Ratings.TABLE_NAME + " AS r "
 						+ "INNER JOIN " + DataContract.Players.TABLE_NAME + " AS p "
 						+ "ON (p." + DataContract.Players._ID + "= r." + DataContract.Ratings.PLAYER_ID + ")");
 				qb.setProjectionMap(sRatingsProjectionMap);
 				groupBy = "r.player_id";
				sortOrder = "r.rating DESC, r.created DESC";
 				break;
 
 			default:
 				throwUnknownUriException(uri);
 		}
 
 		return queryDb(qb, uri, projection, selection, selectionArgs, sortOrder, groupBy, having, limit);
 	}
 
 	@Override
 	public String getType(Uri uri) {
 		switch (sUriMatcher.match(uri)) {
 			case MATCHES:		return DataContract.Matches.CONTENT_TYPE;
 			case MATCH_ID:		return DataContract.Matches.CONTENT_ITEM_TYPE;
 			case PLAYERS:		return DataContract.Players.CONTENT_TYPE;
 			case PLAYER_ID:		return DataContract.Players.CONTENT_ITEM_TYPE;
 			case PLAYER_STATS:	return DataContract.Stats.CONTENT_ITEM_TYPE;
 			case PLAYER_ID_RATING:	return DataContract.Ratings.CONTENT_ITEM_TYPE;
 			case STATS:			return DataContract.Stats.CONTENT_TYPE;
 			case RATINGS:		return DataContract.Ratings.CONTENT_TYPE;
 			default:			throwUnknownUriException(uri);
 		}
 
 		return null;
 	}
 
 	@Override
 	public Uri insert(Uri uri, ContentValues values) {
 		boolean syncToNetwork = !callerIsSyncAdapter(uri);
 
 		if (values == null) {
 			values = new ContentValues();
 		}
 
 		if (!values.containsKey(DataContract.AuditColumns.CREATED)) {
 			values.put(DataContract.AuditColumns.CREATED, System.currentTimeMillis() / 1000L); // current unix time
 		}
 
 		switch(sUriMatcher.match(uri)) {
 			case MATCHES:
 				return insertDb(uri, values, DataContract.Matches.TABLE_NAME, syncToNetwork);
 
 			case PLAYERS:
 				return insertDb(uri, values, DataContract.Players.TABLE_NAME, syncToNetwork);
 
 			case STATS:
 				return insertDb(uri, values, DataContract.Stats.TABLE_NAME, syncToNetwork);
 
 			case RATINGS:
 				return insertDb(uri, values, DataContract.Ratings.TABLE_NAME, syncToNetwork);
 
 			default:
 				throwUnknownUriException(uri);
 		}
 
 		return null;
 	}
 
 	@Override
 	public int delete(Uri uri, String selection, String[] selectionArgs) {
 		boolean syncToNetwork = !callerIsSyncAdapter(uri);
 
 		switch (sUriMatcher.match(uri)) {
 			case MATCHES:
 				return deleteDb(uri, selection, selectionArgs, DataContract.Matches.TABLE_NAME, syncToNetwork);
 
 			case MATCH_ID:
 				selection = mergeWhereClause(selection, BaseColumns._ID + "="
 						+ uri.getPathSegments().get(DataContract.Matches.ID_PATH_POSITION));
 				return deleteDb(uri, selection, selectionArgs, DataContract.Matches.TABLE_NAME, syncToNetwork);
 
 			case PLAYERS:
 				return deleteDb(uri, selection, selectionArgs, DataContract.Players.TABLE_NAME, syncToNetwork);
 
 			case PLAYER_ID:
 				selection = mergeWhereClause(selection, BaseColumns._ID + "="
 						+ uri.getPathSegments().get(DataContract.Players.ID_PATH_POSITION));
 				return deleteDb(uri, selection, selectionArgs, DataContract.Players.TABLE_NAME, syncToNetwork);
 
 			default:
 				throwUnknownUriException(uri);
 		}
 
 		return -1;
 	}
 
 	/**
 	 * Runs a query to the database and returns a cursor with the result.
 	 * @param uri
 	 * @param projection
 	 * @param selection
 	 * @param selectionArgs
 	 * @param sortOrder
 	 * @param qb
 	 * @param limit
 	 * @return The cursor.
 	 */
 	private Cursor queryDb(SQLiteQueryBuilder qb, Uri uri, String[] projection, String selection,
 						   String[] selectionArgs, String sortOrder, String groupBy, String having, String limit) {
 		SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
 		Cursor cursor = qb.query(db, projection, selection, selectionArgs, groupBy, having, sortOrder, limit);
 		cursor.setNotificationUri(getContext().getContentResolver(), uri);
 		return cursor;
 	}
 
 	@Override
 	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
 		boolean syncToNetwork = !callerIsSyncAdapter(uri);
 
 		switch(sUriMatcher.match(uri)) {
 			case MATCHES:
 				return updateDb(uri, values, selection, selectionArgs, DataContract.Matches.TABLE_NAME, syncToNetwork);
 
 			case MATCH_ID:
 				selection = mergeWhereClause(selection, BaseColumns._ID + "="
 						+ uri.getPathSegments().get(DataContract.Matches.ID_PATH_POSITION));
 				return updateDb(uri, values, selection, selectionArgs, DataContract.Matches.TABLE_NAME, syncToNetwork);
 
 			case PLAYERS:
 				return updateDb(uri, values, selection, selectionArgs, DataContract.Players.TABLE_NAME, syncToNetwork);
 
 			case PLAYER_ID:
 				selection = mergeWhereClause(selection, BaseColumns._ID + "="
 						+ uri.getPathSegments().get(DataContract.Players.ID_PATH_POSITION));
 				return updateDb(uri, values, selection, selectionArgs, DataContract.Players.TABLE_NAME, syncToNetwork);
 
 			default:
 				throwUnknownUriException(uri);
 		}
 
 		return -1;
 	}
 
 	/**
 	 * Inserts content with the given values into the database.
 	 * @param uri
 	 * @param values
 	 * @param table
 	 * @param syncToNetwork
 	 * @return Insert id.
 	 */
 	private Uri insertDb(Uri uri, ContentValues values, String table, boolean syncToNetwork) {
 		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
 		long insertId = db.insert(table, null, values);
 
 		if (insertId > 0) {
 			Uri contentUri = ContentUris.withAppendedId(uri, insertId);
 			getContext().getContentResolver().notifyChange(contentUri, null, syncToNetwork);
 			return contentUri;
 		}
 
 		return null;
 	}
 
 	/**
 	 * Deletes content from the database.
 	 * @param uri
 	 * @param selection
 	 * @param selectionArgs
 	 * @param table
 	 * @param syncToNetwork
 	 * @return Number of affected rows.
 	 */
 	private int deleteDb(Uri uri, String selection, String[] selectionArgs, String table, boolean syncToNetwork) {
 		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
 		int numAffectedRows = db.delete(table, selection, selectionArgs);
 		getContext().getContentResolver().notifyChange(uri, null, syncToNetwork);
 		return numAffectedRows;
 	}
 
 	/**
 	 * Updates content in the database.
 	 * @param uri
 	 * @param values
 	 * @param selection
 	 * @param selectionArgs
 	 * @param table
 	 * @param syncToNetwork
 	 * @return Number of affected rows.
 	 */
 	private int updateDb(Uri uri, ContentValues values, String selection, String[] selectionArgs, String table,
 						 boolean syncToNetwork) {
 		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
 		int numAffectedRows = db.update(table, values, selection, selectionArgs);
 		getContext().getContentResolver().notifyChange(uri, null, syncToNetwork);
 		return numAffectedRows;
 	}
 
 	/**
 	 * Merges two where clauses using the AND operator.
 	 * @param where1 First where clause.
 	 * @param where2 Second where clause.
 	 * @return The result.
 	 */
 	private String mergeWhereClause(String where1, String where2) {
 		return !TextUtils.isEmpty(where1) ? "(" + where1 + ") AND (" + where2 + ")" : where2;
 	}
 
 	/**
 	 * Throws an unknown uri exception when called.
 	 * @param uri Unknown uri.
 	 * @throws IllegalArgumentException
 	 */
 	protected void throwUnknownUriException(Uri uri) throws IllegalArgumentException {
 		throw new IllegalArgumentException("Unknown URI: " + uri);
 	}
 
 	/**
 	 * Returns whether the caller for the given uri is a sync adapter.
 	 * @param uri Called URI.
 	 * @return The result.
 	 */
 	protected static boolean callerIsSyncAdapter(Uri uri) {
 		return Boolean.parseBoolean(uri.getQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER));
 	}
 }
