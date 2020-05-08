 package ch.almana.android.stillmeter.provider.db;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.net.Uri;
 import ch.almana.android.stillmeter.log.Logger;
 import ch.almana.android.stillmeter.provider.StillProvider;
 
 public interface DB {
 
 	public static final String DATABASE_NAME = "stillTimer";
 
 	public static final String NAME_ID = "_id";
 	public static final int INDEX_ID = 0;
 
 	public static final String[] PROJECTION_IDE = new String[] { NAME_ID };
 	public static final String SELECTION_BY_ID = NAME_ID + "=?";
 
 	public class OpenHelper extends SQLiteOpenHelper {
 
 		private static final int DATABASE_VERSION = 1;
 
 		private static final String CREATE_STILLTIME_TABLE = "create table if not exists " + StillTime.TABLE_NAME + " (" + DB.NAME_ID + " integer primary key, "
 				+ StillTime.NAME_SESSION + " long, " + StillTime.NAME_BREAST + " text,"
 				+ StillTime.NAME_TIME_START + " long," + StillTime.NAME_TIME_END + " long)";
 
 		private static final String CREATE_SESSION_TABLE = "create table if not exists " + Session.TABLE_NAME + " (" + DB.NAME_ID + " integer primary key, "
 				+ Session.NAME_DAY + " long, " + Session.NAME_TIME_START + " long," + Session.NAME_TIME_END + " long, " + Session.NAME_BREAST_LEFT_TIME + " long,"
 				+ Session.NAME_BREAST_RIGHT_TIME + " long," + Session.NAME_TOTAL_TIME + " long)";
 		
 
 		private static final String CREATE_DAY_TABLE = "create table if not exists " + Day.TABLE_NAME + " (" + DB.NAME_ID + " integer primary key, "
 				+ Day.NAME_DAY + " text, " + Day.NAME_TIME_START + " long)";
 		
 		public OpenHelper(Context context) {
 			super(context, DB.DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL(CREATE_STILLTIME_TABLE);
 			db.execSQL(CREATE_SESSION_TABLE);
 			db.execSQL(CREATE_DAY_TABLE);
 			db.execSQL("create index idx_stilltime_session on " + StillTime.TABLE_NAME + " (" + StillTime.NAME_SESSION + "); ");
 			db.execSQL("create index idx_session_day on " + Session.TABLE_NAME + " (" + Session.NAME_DAY + "); ");
 			Logger.i("Created tables ");
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			switch (oldVersion) {
 			case 1:
 				Logger.w("Upgrading to DB Version 2...");
 				// nobreak
 				
 			default:
 				Logger.w("Finished DB upgrading!");
 				break;
 			}
 		}
 	}
 
 	public interface StillTime {
 
 		public static final String TABLE_NAME = "stillTime";
 
 		public static final String CONTENT_ITEM_NAME = "stillTime";
 		public static String CONTENT_URI_STRING = "content://" + StillProvider.AUTHORITY + "/" + CONTENT_ITEM_NAME;
 		public static Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);
 
 		static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + StillProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;
 
 		static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + StillProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;
 
 		public static final String NAME_SESSION = "session";
 		public static final String NAME_BREAST = "breast";
 		public static final String NAME_TIME_START = "timeStart";
 		public static final String NAME_TIME_END = "timeEnd";
 
 		public static final int INDEX_SESSION = 1;
 		public static final int INDEX_BREAST = 2;
 		public static final int INDEX_TIME_START = 3;
 		public static final int INDEX_TIME_END = 4;
 
 		public static final String[] colNames = new String[] { NAME_ID, NAME_SESSION, NAME_BREAST, NAME_TIME_START, NAME_TIME_END };
 		public static final String[] PROJECTION_DEFAULT = colNames;
 		public static final String[] PROJECTION_BREAST = new String[] { NAME_ID, NAME_BREAST };
 
 		public static final String SORTORDER_DEFAULT = NAME_TIME_START + " DESC";
 		static final String SORTORDER_REVERSE = NAME_TIME_START + " ASC";
 
 		public static final String SELECTION_START_END = NAME_TIME_START + " > ? and " + NAME_TIME_START + " < ?";
 		public static final String SELECTION_BY_SESSION_ID = NAME_SESSION + " =?";
 
 	}
 
 	public interface Session {
 
 		public static final String TABLE_NAME = "session";
 
 		public static final String CONTENT_ITEM_NAME = "session";
 		public static String CONTENT_URI_STRING = "content://" + StillProvider.AUTHORITY + "/" + CONTENT_ITEM_NAME;
 		public static Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);
 
 		static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + StillProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;
 
 		static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + StillProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;
 
 		public static final String NAME_DAY = "day";
 		public static final String NAME_TIME_START = "timeStart";
 		public static final String NAME_TIME_END = "timeEnd";
 		public static final String NAME_BREAST_LEFT_TIME = "breastLeftTime";
 		public static final String NAME_BREAST_RIGHT_TIME = "breastRigtTime";
 		public static final String NAME_TOTAL_TIME = "totalTime";
 
 		public static final int INDEX_DAY = 1;
 		public static final int INDEX_TIME_START = 2;
 		public static final int INDEX_TIME_END = 3;
 		public static final int INDEX_BREAST_LEFT_TIME = 4;
 		public static final int INDEX_BREAST_RIGHT_TIME = 5;
 		public static final int INDEX_TOTAL_TIME = 6;
 
 		public static final String[] colNames = new String[] { NAME_ID, NAME_DAY, NAME_TIME_START, NAME_TIME_END, NAME_BREAST_LEFT_TIME, NAME_BREAST_RIGHT_TIME, NAME_TOTAL_TIME};
 		public static final String[] PROJECTION_DEFAULT = colNames;
 
 		public static final String SELECTION_BY_DAY = NAME_DAY + "=? and " + NAME_TIME_END + " > 0";
 
 		public static final String SORTORDER_DEFAULT = NAME_TIME_START + " DESC";
 
 		static final String SORTORDER_REVERSE = NAME_TIME_START + " ASC";
 
 	}
 	public interface Day {
 
 		public static final String TABLE_NAME = "day";
 
 		public static final String CONTENT_ITEM_NAME = "day";
 		public static String CONTENT_URI_STRING = "content://" + StillProvider.AUTHORITY + "/" + CONTENT_ITEM_NAME;
 		public static Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);
 
 		static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + StillProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;
 
 		static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + StillProvider.AUTHORITY + "." + CONTENT_ITEM_NAME;
 
 		public static final String NAME_DAY = "day";
 		public static final String NAME_TIME_START = "timeStart";
 
 		public static final int INDEX_DAY = 1;
 		public static final int INDEX_TIME_START = 2;
 
 		public static final String[] colNames = new String[] { NAME_ID, NAME_DAY, NAME_TIME_START};
 		public static final String[] PROJECTION_DEFAULT = colNames;
 
 		public static final String SELECTION_BY_DAY = NAME_DAY + "=?";
 
 		public static final String SORTORDER_DEFAULT = NAME_TIME_START + " DESC";
 
 		static final String SORTORDER_REVERSE = NAME_TIME_START + " ASC";
 
 	}
 }
