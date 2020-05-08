 package ntnu.stud.valens.contentprovider;
 
 import android.content.ContentProvider;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.provider.BaseColumns;
 import android.text.TextUtils;
 
 public class ValensDataProvider extends ContentProvider {
 
 	// the underlying database
 	private SQLiteDatabase db = null;
 
 	@Override
 	public int delete(Uri uri, String selection, String[] selectionArgs) {
 		throw new IllegalArgumentException("Delete is not a valid operation!");
 	}
 
 	@Override
 	public String getType(Uri uri) {
 		switch (URI_MATCHER.match(uri)) {
 		case STEPS:
 			return Steps.CONTENT_TYPE;
 		case RAW_STEPS:
 			return RawSteps.CONTENT_TYPE;
 		case GAIT:
 			return Gait.CONTENT_TYPE;
 		case TESTS:
 			return Tests.CONTENT_TYPE;
 		default:
 			throw new IllegalArgumentException("Unsupported URI: " + uri);
 		}
 	}
 
 	@Override
 	public Uri insert(Uri uri, ContentValues values) {
 		// TODO Auto-generated method stub
 		switch (URI_MATCHER.match(uri)) {
 		case RAW_STEPS:
 			long timestamp = db.insert(DBSchema.RawSteps.TABLE_NAME, null,
 					values);
 			if (timestamp > 0) {
 				Uri itemUri = ContentUris.withAppendedId(uri, timestamp);
 				getContext().getContentResolver().notifyChange(itemUri, null);
 				return itemUri;
 			}
 			throw new SQLException("Problem while inserting into "
 					+ DBSchema.RawSteps.TABLE_NAME + ", uri: " + uri);
 		default:
 			throw new IllegalArgumentException(
 					"Unsupported URI for insertion: " + uri);
 		}
 	}
 
 	@Override
 	public boolean onCreate() {
 		this.db = new CPValensDB(this.getContext()).getWritableDatabase();
 		if (this.db == null) {
 			return false;
 		}
 		if (this.db.isReadOnly()) {
 			this.db.close();
 			this.db = null;
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection,
 			String[] selectionArgs, String sortOrder) {
 		Cursor cursor = null;
 		switch (URI_MATCHER.match(uri)) {
 		case STEPS:
 			if (TextUtils.isEmpty(sortOrder)) {
 				sortOrder = "ASC";
 			}
 			String querySteps = "";
 			if (selectionArgs.length == Steps.PROJECTION_ALL.length) {
 				querySteps = "select timestamp from " + DBSchema.RawSteps.TABLE_NAME
 						+ " where " + DBSchema.RawSteps.COLUMN_NAME_SOURCE
 						+ "=(select max("
 						+ DBSchema.RawSteps.COLUMN_NAME_SOURCE + ") from "
 						+ DBSchema.RawSteps.TABLE_NAME + ") and "
 						+ DBSchema.RawSteps.COLUMN_NAME_TIMESTAMP + ">"
 						+ selectionArgs[0] + " and "
 						+ DBSchema.RawSteps.COLUMN_NAME_TIMESTAMP + "<"
 						+ selectionArgs[1] + " order by "
 						+ DBSchema.RawSteps.COLUMN_NAME_TIMESTAMP
						+ " sortOrder";
 			} else {
 				querySteps = "select timestamp from " + DBSchema.RawSteps.TABLE_NAME
 						+ " where " + DBSchema.RawSteps.COLUMN_NAME_SOURCE
 						+ "=(select max("
 						+ DBSchema.RawSteps.COLUMN_NAME_SOURCE + ") from "
 						+ DBSchema.RawSteps.TABLE_NAME + ")" + " order by "
 						+ DBSchema.RawSteps.COLUMN_NAME_TIMESTAMP
						+ " sortOrder";
 			}
 			cursor = this.db.rawQuery(querySteps, selectionArgs);
 			cursor.setNotificationUri(getContext().getContentResolver(), uri);
 			return cursor;
 		case TESTS:
 			if (TextUtils.isEmpty(sortOrder)) {
 				sortOrder = Tests.SORT_ORDER_DEFAULT;
 			}
 			String queryTest = "select a." + DBSchema.Tests.COLUMN_NAME_TIMESTAMP
 					+ ", a." + DBSchema.Tests.COLUMN_NAME_SCORE + ", b."
 					+ DBSchema.TestTypes.COLUMN_NAME_NAME + ", b."
 					+ DBSchema.TestTypes.COLUMN_NAME_DESCRIPTION + ", b."
 					+ DBSchema.TestTypes.COLUMN_NAME_SCORE_DESCRIPTION
 					+ "from " + DBSchema.Tests.TABLE_NAME + " a, "
 					+ DBSchema.TestTypes.TABLE_NAME + " b where a."
 					+ DBSchema.Tests.COLUMN_NAME_TYPE_CODE_KEY + "=b."
 					+ DBSchema.TestTypes.COLUMN_NAME_CODE_KEY + " order by "
 					+ Tests.SORT_ORDER_DEFAULT;
 			cursor = this.db.rawQuery(queryTest, selectionArgs);
 			cursor.setNotificationUri(getContext().getContentResolver(), uri);
 			break;
 		default:
 			cursor=this.db.query(uri.getLastPathSegment(), projection, selection, selectionArgs, null, null, null);
 			break;
 		}
 		return cursor;
 	}
 
 	@Override
 	public int update(Uri uri, ContentValues values, String selection,
 			String[] selectionArgs) {
 		throw new IllegalArgumentException("Update is not a valid operation!");
 	}
 
 	// public constants for client development
 	public static final String AUTHORITY = "ntnu.stud.valens.contentprovider";
 	public static final Uri STEPS_CONTENT_URI = Uri.parse("content://"
 			+ AUTHORITY + "/" + Steps.CONTENT_PATH);
 	public static final Uri GAIT_CONTENT_URI = Uri.parse("content://"
 			+ AUTHORITY + "/" + Gait.CONTENT_PATH);
 	public static final Uri RAW_STEPS_CONTENT_URI = Uri.parse("content://"
 			+ AUTHORITY + "/" + RawSteps.CONTENT_PATH);
 	public static final Uri TESTS_CONTENT_URI = Uri.parse("content://"
 			+ AUTHORITY + "/" + Tests.CONTENT_PATH);
 
 	// helper constants for use with the UriMatcher
 	private static final int STEPS = 1;
 	private static final int GAIT = 2;
 	private static final int RAW_STEPS = 3;
 	private static final int TESTS = 4;
 	private static final UriMatcher URI_MATCHER = new UriMatcher(
 			UriMatcher.NO_MATCH);
 
 	/**
 	 * Column and content type definitions for the Steps.
 	 */
 	public static interface Steps extends BaseColumns {
 		public static final Uri CONTENT_URI = ValensDataProvider.STEPS_CONTENT_URI;
 		public static final String TIMESTAMP = "timestamp";
 		public static final String TIMESTAMP_START = "timestamp";
 		public static final String TIMESTAMP_END = "timestamp";
 		public static final String CONTENT_PATH = "steps";
 		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
 				+ "/vnd.valens.steps";
 		public static final String[] PROJECTION_ALL = { TIMESTAMP_START, TIMESTAMP_END };
 		public static final String SORT_ORDER_DEFAULT = TIMESTAMP + " DESC";
 	}
 
 	/**
 	 * Column and content type definitions for the Steps.
 	 */
 	public static interface RawSteps extends BaseColumns {
 		public static final Uri CONTENT_URI = ValensDataProvider.STEPS_CONTENT_URI;
 		public static final String TIMESTAMP = "timestamp";
 		public static final String SOURCE = "source";
 		public static final String CONTENT_PATH = "raw_steps";
 		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
 				+ "/vnd.valens.rawsteps";
 		public static final String[] PROJECTION_ALL = { TIMESTAMP, SOURCE };
 		public static final String SORT_ORDER_DEFAULT = TIMESTAMP + " DESC";
 	}
 
 	/**
 	 * Column and content type definitions for the Steps.
 	 */
 	public static interface Gait extends BaseColumns {
 		public static final Uri CONTENT_URI = ValensDataProvider.STEPS_CONTENT_URI;
 		public static final String INTERVAL_AVG = "interval_avg";
 		public static final String VARIABILITY = "variability";
 		public static final String START_TIMESPAN = "start_timespan";
 		public static final String END_TIMESPAN = "end_timespan";
 		public static final String CONTENT_PATH = "steps";
 		public static final String CONTENT_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
 				+ "/vnd.valens.gait";
 		public static final String[] PROJECTION_ALL = { INTERVAL_AVG,
 				VARIABILITY, START_TIMESPAN, END_TIMESPAN };
 		public static final String SORT_ORDER_DEFAULT = START_TIMESPAN
 				+ " DESC";
 	}
 
 	/**
 	 * Column and content type definitions for the Steps.
 	 */
 	public static interface Tests extends BaseColumns {
 		public static final Uri CONTENT_URI = ValensDataProvider.TESTS_CONTENT_URI;
 		public static final String TIMESTAMP = "timestamp";
 		public static final String SCORE = "score";
 		public static final String NAME = "name";
 		public static final String DESCRIPTION = "description";
 		public static final String SCORE_DESCRIPTION = "score_description";
 		public static final String CONTENT_PATH = "tests";
 		public static final String CONTENT_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
 				+ "/vnd.valens.gait";
 		public static final String[] PROJECTION_ALL = { TIMESTAMP, SCORE, NAME,
 				DESCRIPTION, SCORE_DESCRIPTION };
 		public static final String SORT_ORDER_DEFAULT = TIMESTAMP + " DESC";
 	}
 
 	// prepare the UriMatcher
 	static {
 		URI_MATCHER.addURI(AUTHORITY, Steps.CONTENT_PATH, STEPS);
 		URI_MATCHER.addURI(AUTHORITY, Gait.CONTENT_PATH, GAIT);
 		URI_MATCHER.addURI(AUTHORITY, RawSteps.CONTENT_PATH, RAW_STEPS);
 		URI_MATCHER.addURI(AUTHORITY, Tests.CONTENT_PATH, TESTS);
 	}
 
 }
