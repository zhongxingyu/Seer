 package com.example.tomatroid.sql;
 
 import org.joda.time.DateTime;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class SQHelper extends SQLiteOpenHelper {
 
 	private static final String DATABASE_NAME = "pomodorodroid";
 	private static final int DATABASE_VERSION = 1;
 
 	// TABLE Dates
 	public static final String TABLE_DATES = "dates";
 	public static final String KEY_ROWID = "_id";
 	public static final String KEY_DATE_DAY = "day";
 	public static final String KEY_DATE_MONTH = "month";
 	public static final String KEY_DATE_YEAR = "year";
 	public static final String KEY_DATE_WEEKDAY = "weekday";
 	public static final String KEY_DATE_WEEKNUM = "weeknum";
 	public static final String KEY_DATE_START_HOUR = "starttime_hour";
 	public static final String KEY_DATE_START_MINUTE = "starttime_minute";
 	public static final String KEY_DATE_END_HOUR = "endtime_hour";
 	public static final String KEY_DATE_END_MINUTE = "endtime_minute";
 	public static final String KEY_DURATION = "duration";
 	public static final String KEY_STARTTIMEMILLIES = "starttimemillies";
 	public static final String KEY_ENDTIMEMILLIES = "endtimemillies";
 	public static final String KEY_TYPE = "type";
 	public static final String KEY_THEME = "theme";
 
 	static final String CREATE_DATES_TABLE = "create table " + TABLE_DATES
 			+ "(" + KEY_ROWID + " integer primary key autoincrement, "
 			+ KEY_DATE_DAY + " integer not null, " + KEY_DATE_MONTH
 			+ " integer not null," + KEY_DATE_YEAR + " integer not null,"
 			+ KEY_DATE_WEEKDAY + " integer not null," + KEY_DATE_WEEKNUM
 			+ " integer not null," + KEY_DATE_START_HOUR + " integer not null,"
 			+ KEY_DATE_START_MINUTE + " integer not null," + KEY_DATE_END_HOUR
 			+ " integer not null," + KEY_DATE_END_MINUTE + " integer not null,"
 			+ KEY_DURATION + " integer not null," + KEY_TYPE
 			+ " integer not null," + KEY_THEME + " integer,"
 			+ KEY_STARTTIMEMILLIES + " integer not null,"
 			+ KEY_ENDTIMEMILLIES + " integer not null);";
 
 	// TABLE Theme
 	public static final String TABLE_THEME = "themes";
 	// ROW ID
 	public static final String KEY_NAME = "name";
 	public static final String KEY_ITEMOF = "itemof";
 
 	static final String CREATE_THEME_TABLE = "create table " + TABLE_THEME
 			+ "(" + KEY_ROWID + " integer primary key autoincrement, "
 			+ KEY_ITEMOF + " integer," + KEY_NAME + " text not null);";
 
 	public static final int TYPE_POMODORO = 0;
 	public static final int TYPE_SHORTBREAK = 1;
 	public static final int TYPE_LONGBREAK = 2;
 	public static final int TYPE_TRACKING = 3;
 	public static final int TYPE_SLEEPING = 4;
 
 	public SQHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase database) {
 		Log.e("SQHepler", "onCreate");
 		createTables(database);
 	}
 
 	public void createTables(SQLiteDatabase database) {
 		database.execSQL(CREATE_DATES_TABLE);
 		database.execSQL(CREATE_THEME_TABLE);
 		addTheme("Kein Thema");
 		addTheme("Gaming");
 		addTheme("Pomodoro App");
 	}
 
 	public int getStartUpPomodoroCount() {
 		SQLiteDatabase db = getReadableDatabase();
 		DateTime dt = new DateTime();
 		int day = dt.getDayOfMonth();
 		int month = dt.getMonthOfYear();
 		int year = dt.getYear();
 
 		Cursor c = db.query(TABLE_DATES, null, KEY_DATE_DAY + " = " + day
 				+ " AND " + KEY_DATE_MONTH + " = " + month + " AND "
 				+ KEY_DATE_YEAR + " = " + year + " AND " + KEY_TYPE + " = "
 				+ TYPE_POMODORO, null, null, null, null);
 		int count = c.getCount();
 		Log.e("SQHelper", "pomodoro count " + count);
 		c.close();
 		db.close();
 		return count;
 	}
 
 	public int getStartUpPomodoroTime() {
 		SQLiteDatabase db = getReadableDatabase();
 		DateTime dt = new DateTime();
 		int day = dt.getDayOfMonth();
 		int month = dt.getMonthOfYear();
 		int year = dt.getYear();
 
 		Cursor c = db.query(TABLE_DATES, new String[] { KEY_DURATION, },
 				KEY_DATE_DAY + " = " + day + " AND " + KEY_DATE_MONTH + " = "
 						+ month + " AND " + KEY_DATE_YEAR + " = " + year
 						+ " AND " + KEY_TYPE + " = " + TYPE_POMODORO, null,
 				null, null, null);
 
 		int duration = calculateDuration(c);
 		Log.e("SQHelper",
 				"pomodoro Duration: " + duration + " ::" + c.getCount());
 		c.close();
 		db.close();
 		return duration;
 	}
 
 	public int getStartUpBreakTime() {
 		SQLiteDatabase db = getReadableDatabase();
 		DateTime dt = new DateTime();
 		int day = dt.getDayOfMonth();
 		int month = dt.getMonthOfYear();
 		int year = dt.getYear();
 
 		Cursor c = db.query(TABLE_DATES, new String[] { KEY_DURATION, },
 				KEY_DATE_DAY + " = " + day + " AND " + KEY_DATE_MONTH + " = "
 						+ month + " AND " + KEY_DATE_YEAR + " = " + year
 						+ " AND (" + KEY_TYPE + " = " + TYPE_SHORTBREAK
 						+ " OR " + KEY_TYPE + " = " + TYPE_LONGBREAK + ")",
 				null, null, null, null);
 
 		int duration = calculateDuration(c);
 		Log.e("SQHelper", "break Duration: " + duration + " ::" + c.getCount());
 		c.close();
 		db.close();
 		return duration;
 	}
 
 	public int calculateDuration(Cursor c) {
 		int duration = 0;
		int column = c.getColumnIndex(KEY_DURATION);
 		if (c.moveToFirst()) {
 			do {
				duration += c.getInt(column);
 			} while (c.moveToNext());
 		}
 		return duration;
 	}
 
 	public Cursor getLastXDays(int x) {
 		SQLiteDatabase db = getReadableDatabase();
 		DateTime dt = new DateTime();
 		int day = dt.getDayOfMonth();
 		int month = dt.getMonthOfYear();
 		int year = dt.getYear();
 
 		dt = dt.minusDays(x);
 		int old_day = dt.getDayOfMonth();
 		int old_month = dt.getMonthOfYear();
 		int old_year = dt.getYear();
 
 		return db.query(TABLE_DATES, new String[] { KEY_DATE_WEEKDAY, KEY_TYPE,
 				KEY_DURATION }, "(" + KEY_DATE_DAY + " <= " + day + " AND "
 				+ KEY_DATE_MONTH + " <= " + month + " AND " + KEY_DATE_YEAR
 				+ " <= " + year
 
 				+ " AND " + KEY_DATE_DAY + " >= " + old_day + " AND "
 				+ KEY_DATE_MONTH + " >= " + old_month + " AND " + KEY_DATE_YEAR
 				+ " >= " + old_year
 
 				+ ")", null, null, null, null);
 	}
 
 	public Cursor getAllThemes() {
 		SQLiteDatabase db = getReadableDatabase();
 		Cursor c = db.query(TABLE_THEME, new String[] { KEY_ROWID, KEY_NAME },
 				null, null, null, null, null);
 		// Cursor c = db.query(TABLE_THEME, null, null, null, null, null, null);
 		// db.close();
 		return c;
 	}
 
 	public void addTheme(String name) {
 		SQLiteDatabase db = getWritableDatabase();
 		ContentValues newContent = new ContentValues();
 		newContent.put(KEY_NAME, name);
 		db.insert(TABLE_THEME, null, newContent);
 		db.close();
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		Log.w(SQHelper.class.getName(), "Upgrading database from version "
 				+ oldVersion + " to " + newVersion
 				+ ", which will destroy all old data");
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATES);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_THEME);
 		onCreate(db);
 	}
 
 	public void renewTables() {
 		SQLiteDatabase db = getWritableDatabase();
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATES);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_THEME);
 		createTables(db);
 	}
 
 	public void insertDate(int tag, int minutesPast, String tracking) {
 		SQLiteDatabase db = getWritableDatabase();
 
 		DateTime endDate = new DateTime();
 		DateTime startDate = endDate.minusMinutes(minutesPast);
 
 		ContentValues newContent = new ContentValues();
 		newContent.put(KEY_DATE_DAY, startDate.getDayOfMonth());
 		newContent.put(KEY_DATE_MONTH, startDate.getMonthOfYear());
 		newContent.put(KEY_DATE_YEAR, startDate.getYear());
 		newContent.put(KEY_DATE_WEEKDAY, startDate.getDayOfWeek());
 		newContent.put(KEY_DATE_WEEKNUM, startDate.getWeekOfWeekyear());
 		newContent.put(KEY_DATE_START_HOUR, startDate.getHourOfDay());
 		newContent.put(KEY_DATE_START_MINUTE, startDate.getMinuteOfHour());
 		newContent.put(KEY_DATE_END_HOUR, endDate.getHourOfDay());
 		newContent.put(KEY_DATE_END_MINUTE, endDate.getMinuteOfHour());
 		newContent.put(KEY_TYPE, tag);
 		newContent.put(KEY_DURATION, minutesPast);
 		newContent.put(KEY_THEME, tracking);
 		newContent.put(KEY_STARTTIMEMILLIES, startDate.getMillis());
 		newContent.put(KEY_ENDTIMEMILLIES, endDate.getMillis());
 		long l = db.insert(TABLE_DATES, null, newContent);
 		Log.e("SQHelper", "row affected " + l);
 		db.close();
 	}
 
 }
