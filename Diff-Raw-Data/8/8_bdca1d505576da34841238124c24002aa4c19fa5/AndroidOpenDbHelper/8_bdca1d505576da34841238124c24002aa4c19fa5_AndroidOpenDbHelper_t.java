 package edu.upenn.cis350;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.provider.BaseColumns;
 
 // A helper class to manage database creation and version management.
 public class AndroidOpenDbHelper extends SQLiteOpenHelper {
 	// Database attributes
 	public static final String DB_NAME = "wcomm_db";
 	public static final int DB_VERSION = 1;
 
 	// Table attributes
 	public static final String TABLE_NAME_EVENTS = "events_table";
 	public static final String COLUMN_NAME_EVENT_ID = "event_id_column";
 	public static final String COLUMN_NAME_EVENT_TITLE = "event_title_column";
 	public static final String COLUMN_NAME_EVENT_DESC = "event_desc_column";
 	public static final String COLUMN_NAME_EVENT_ACTIONS = "event_actions_column";
 	public static final String COLUMN_NAME_EVENT_START = "event_start_column";
 	public static final String COLUMN_NAME_EVENT_END = "event_end_column";
 	public static final String COLUMN_NAME_EVENT_AFFILS = "event_affils_column";
 	public static final String COLUMN_NAME_EVENT_SYSTEMS = "event_systems_column";
 	public static final String COLUMN_NAME_EVENT_CONTACT1 = "event_contact1_column";
 	public static final String COLUMN_NAME_EVENT_CONTACT2 = "event_contact2_column";
 	public static final String COLUMN_NAME_EVENT_SEVERITY = "event_severity_column";
 	public static final String COLUMN_NAME_EVENT_TYPE = "event_type_column";
 
 	//TODO (closen) add more columns
 
 	public static final String TABLE_NAME_COMMENTS = "comments_table";
 	public static final String COLUMN_NAME_COMMENT_MESSAGE_ID = "comment_message_id_column";
 	public static final String COLUMN_NAME_COMMENT_TEXT = "comment_text_column";
 	public static final String COLUMN_NAME_COMMENT_AUTHOR = "comment_author_column";
 	public static final String COLUMN_NAME_COMMENT_TIMESTAMP = "comment_timestamp_column";
 	public static final String[] COMMENT_COLUMNS_ALL = {COLUMN_NAME_COMMENT_MESSAGE_ID,
 		COLUMN_NAME_COMMENT_TEXT, COLUMN_NAME_COMMENT_AUTHOR, COLUMN_NAME_COMMENT_TIMESTAMP};
 	
 	public static final String TABLE_NAME_USERS = "users_table";
 	public static final String COLUMN_NAME_USER_NAME = "user_name_column";
 	public static final String COLUMN_NAME_USER_PW = "user_pw_column";
 	public static final String COLUMN_NAME_USER_SIGNUP_TIMESTAMP = "user_signup_timestamp_column";
 	
 	public static final String TABLE_NAME_MESSAGES = "messages_table";
 	public static final String COLUMN_NAME_MESSAGE_ID = "message_id_column";
 	public static final String COLUMN_NAME_MESSAGE_TEXT = "message_text_column";
 	public static final String COLUMN_NAME_MESSAGE_AUTHOR = "message_author_column";
 	public static final String COLUMN_NAME_MESSAGE_TIMESTAMP = "message_timestamp_column";
 	public static final String COLUMN_NAME_MESSAGE_EVENT = "message_event_column";
 
 	
 	public AndroidOpenDbHelper(Context context) {
 		super(context, DB_NAME, null, DB_VERSION);
 	}
 
 	// Called when the database is created for the first time.
 	//This is where the creation of tables and the initial population of the tables should happen.
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		createUsersTable(db);
 		createEventsTable(db);
 		createCommentsTable(db);
 	}
 
 	public void createUsersTable(SQLiteDatabase db) {
 		String sqlCreateCommentsTable = "create table if not exists " + TABLE_NAME_USERS + " ( " + BaseColumns._ID + " integer primary key autoincrement, "
 				+ COLUMN_NAME_USER_NAME + " text not null, "
 				+ COLUMN_NAME_USER_PW + " text not null, "
 				+ COLUMN_NAME_USER_SIGNUP_TIMESTAMP + " text not null);";
 
 		db.execSQL(sqlCreateCommentsTable);
 	}
 	
 	public void createEventsTable(SQLiteDatabase db) {
 		// We need to check whether table that we are going to create already exists.
 				// This method gets executed every time we creat an object of this class.
 				//"create table if not exists TABLE_NAME ( BaseColumns._ID integer primary key autoincrement, FIRST_COLUMN_NAME text not null, SECOND_COLUMN_NAME integer not null);"
 				String sqlCreateEventsTable = "create table if not exists " + TABLE_NAME_EVENTS + " ( " 
 						+ COLUMN_NAME_EVENT_ID + " integer primary key autoincrement, "
 						+ COLUMN_NAME_EVENT_TITLE + " text not null, "
 						+ COLUMN_NAME_EVENT_DESC + " text not null, "
 						+ COLUMN_NAME_EVENT_ACTIONS + " text not null, "
 						+ COLUMN_NAME_EVENT_START + " text not null, "
 						+ COLUMN_NAME_EVENT_END + " text not null, "
 						+ COLUMN_NAME_EVENT_AFFILS + " text not null, "
 						+ COLUMN_NAME_EVENT_SYSTEMS + " text not null, "
 						+ COLUMN_NAME_EVENT_CONTACT1 + " text not null, "
 						+ COLUMN_NAME_EVENT_CONTACT2 + " text not null, "
 						+ COLUMN_NAME_EVENT_TYPE + " text not null, "
 						+ COLUMN_NAME_EVENT_SEVERITY + " integer);";
 				
 				// Execute a single SQL statement that is NOT a SELECT or any other SQL statement that returns data.
 				db.execSQL(sqlCreateEventsTable);
 	}
 	
 	public void createCommentsTable(SQLiteDatabase db) {
 		String sqlCreateCommentsTable = "create table if not exists " + TABLE_NAME_COMMENTS + " ( " + BaseColumns._ID + " integer primary key autoincrement, "
				+ COLUMN_NAME_COMMENT_MESSAGE_ID + " text not null, "
 				+ COLUMN_NAME_COMMENT_TEXT + " text not null, "
 				+ COLUMN_NAME_COMMENT_AUTHOR + " text not null, "
 				+ COLUMN_NAME_COMMENT_TIMESTAMP + " text not null);";
 
 		db.execSQL(sqlCreateCommentsTable);
 	}
 	
 	public void createMessagesTable(SQLiteDatabase db) {
		String sqlCreateMessagesTable = "create table if not exists " + TABLE_NAME_MESSAGES + " ( " + COLUMN_NAME_MESSAGE_ID + " integer primary key autoincrement, "
 				+ COLUMN_NAME_MESSAGE_TEXT + " text not null, "
 				+ COLUMN_NAME_MESSAGE_AUTHOR + " text not null, "
 				+ COLUMN_NAME_MESSAGE_TIMESTAMP + " text not null," 
 				+ COLUMN_NAME_MESSAGE_EVENT + " text not null);";
 
		db.execSQL(sqlCreateMessagesTable);
 	}
 	
 	// onUpgrade method is used when we need to upgrade the database to a new version
 	//As an example, the first release of the app contains DB_VERSION = 1
 	//Then, the second release of the same app contains DB_VERSION = 2
 	//where you may have added some new tables or altered the existing ones
 	//Then we need check and do relevant actions to keep our passed data and move with the next structure
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		if(oldVersion == 1 && newVersion == 2){
 			// Upgrade the database
 		}
 	}
 }
