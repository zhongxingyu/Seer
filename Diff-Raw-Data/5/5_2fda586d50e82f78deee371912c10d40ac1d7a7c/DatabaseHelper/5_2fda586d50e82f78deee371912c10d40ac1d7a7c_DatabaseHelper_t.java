 package au.id.teda.broadband.usage.database;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DatabaseHelper extends SQLiteOpenHelper {
 	
 	// Debug tag
 	//private final String DEBUG_TAG = BaseActivity.DEBUG_TAG;
 	
 	// If you change the database schema, you must increment the database version.
     private static final int DATABASE_VERSION = 2;
     private static final String DATABASE_NAME = "iiNetUsage.db";
     
     private static final String DAILY_USAGE_TABLE_CREATE = 
 			"create table " + DailyDataTableAdapter.TABLE_NAME +
 			" (" + DailyDataTableAdapter.KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
 			+ DailyDataTableAdapter.ACCOUNT + " TEXT NOT NULL, "
 			+ DailyDataTableAdapter.MONTH + " TEXT NOT NULL, "
 			+ DailyDataTableAdapter.DAY + " INTEGER UNIQUE, "
             + DailyDataTableAdapter.ANYTIME + " INTEGER NOT NULL, "
 			+ DailyDataTableAdapter.PEAK + " INTEGER NOT NULL, "
 			+ DailyDataTableAdapter.OFFPEAK + " INTEGER NOT NULL, "
 			+ DailyDataTableAdapter.UPLOADS + " INTEGER NOT NULL, "
 			+ DailyDataTableAdapter.FREEZONE + " INTEGER NOT NULL);";
     
     private static final String HOURLY_USAGE_TABLE_CREATE = 
 			"create table " + HourlyDataTableAdapter.TABLE_NAME +
 			" (" + HourlyDataTableAdapter.KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
 			+ HourlyDataTableAdapter.ACCOUNT + " TEXT NOT NULL, "
 			+ HourlyDataTableAdapter.DAY + " TEXT UNIQUE, "
 			+ HourlyDataTableAdapter.HOUR + " TEXT NOT NULL, "
             + HourlyDataTableAdapter.ANYTIME + " INTEGER NOT NULL, "
             + HourlyDataTableAdapter.PEAK + " INTEGER NOT NULL, "
 			+ HourlyDataTableAdapter.OFFPEAK + " INTEGER NOT NULL, "
 			+ HourlyDataTableAdapter.UPLOADS + " INTEGER NOT NULL, "
 			+ HourlyDataTableAdapter.FREEZONE + " INTEGER NOT NULL);";
     
     private static final String UP_TIME_TABLE_CREATE = 
 			"create table " + UptimeTableAdapter.TABLE_NAME +
 			" (" + UptimeTableAdapter.KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
 			+ UptimeTableAdapter.ACCOUNT + " TEXT NOT NULL, "
 			+ UptimeTableAdapter.START + " INTEGER UNIQUE, "
 			+ UptimeTableAdapter.FINISH + " INTEGER NOT NULL, "
 			+ UptimeTableAdapter.IP + " TEXT NOT NULL);";
     
     private static final String HISTORICAL_TABLE_CREATE = 
 			"create table " + HistoricalMonthsTableAdapter.TABLE_NAME +
 			" (" + HistoricalMonthsTableAdapter.KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
 			+ HistoricalMonthsTableAdapter.ACCOUNT + " TEXT NOT NULL, "
 			+ HistoricalMonthsTableAdapter.MONTH + " INTEGER UNIQUE); ";
 
 	public DatabaseHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL(DAILY_USAGE_TABLE_CREATE);
 		db.execSQL(HOURLY_USAGE_TABLE_CREATE);
 		db.execSQL(UP_TIME_TABLE_CREATE);
 		db.execSQL(HISTORICAL_TABLE_CREATE);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
 
         // Loop through database upgrades
         for (int i = oldVersion; i < newVersion; i++) {
             switch(i) {
                 case 1:
                     // Add anytime column to daily table
                     database.execSQL("ALTER TABLE " + DailyDataTableAdapter.TABLE_NAME +
                             " ADD COLUMN " + DailyDataTableAdapter.ANYTIME +
                            " INTEGER NOT NULL DEFAULT '-1'");
 
                     // Add anytime column to hourly table
                     database.execSQL("ALTER TABLE " + HourlyDataTableAdapter.TABLE_NAME +
                             " ADD COLUMN " + HourlyDataTableAdapter.ANYTIME +
                            " INTEGER NOT NULL DEFAULT '-1'");
                     break;
             }
         }
 	}
 
 }
