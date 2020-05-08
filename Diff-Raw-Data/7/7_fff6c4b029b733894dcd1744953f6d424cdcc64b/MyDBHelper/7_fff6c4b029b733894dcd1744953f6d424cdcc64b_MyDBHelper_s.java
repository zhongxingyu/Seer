 package com.mh.util;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
import android.widget.Toast;
 
 public class MyDBHelper {
 	private static final String TAG = "MyDBHelper";
 	public static final String KEY_ID = "id"; // 数据表字段，主键
 	public static final String KEY_WEEK = "week"; // 数据表字段,星期
 	public static final String KEY_SEQUENCE = "sequence"; // 数据表字段，第几节
 	public static final String KEY_SUBJECT = "subject"; // 数据表字段，课程
 	public static final String KEY_PLACE = "place"; // 数据表字段，教室（上课地点）
 	public static final String KEY_TEACHER = "teacher"; // 数据表字段，老师
 	public static final String KEY_TIME = "startTime"; // 数据表字段，时间
 	public static final String KEY_INFO = "info"; // 数据表字段，备注
 
 	private DatabaseHelper dbHelper; // SQLiteOpenHelper实例对象
 	private SQLiteDatabase db; // 数据库实例对象
 
 	// 数据表创建语句,week、sequence是联合主键
 	private static final String DATABASE_CREATE = "create table shedule (id integer primary key autoincrement, week varchar(30) not null, sequence varchar(30) not null, subject varchar(30) not null, place varchar(30), teacher varchar(30), startTime time, info text);";
 
 	private static final String DATABASE_NAME = "data"; // 数据库名
 	private static final String DATABASE_TABLE = "shedule"; // 数据库表名
 	private static final int DATABASE_VERSION = 2; // 数据库版本号
 
 	private final Context context; // 上下文实例
 
 	private static class DatabaseHelper extends SQLiteOpenHelper { // 数据库辅助类
 		DatabaseHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL(DATABASE_CREATE);
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			db.execSQL("DROP TABLE IF EXISTS shedule");
 			onCreate(db);
 		}
 
 	}
 
 	public MyDBHelper(Context ctx) {
 		this.context = ctx;
 	}
 
 	public MyDBHelper open() throws SQLException {
 		dbHelper = new DatabaseHelper(context);
 		db = dbHelper.getWritableDatabase();
 		return this;
 	}
 
 	public void close() {
 		dbHelper.close();
 	}
 
 	public void onUpgrade() {
 		dbHelper.onUpgrade(db, 2, 2);
 	}
 
 	public long insert(String week, String sequence, String subject,
 			String place, String teacher, String time, String info) {
 		ContentValues initialValues = new ContentValues();
 		initialValues.put(KEY_WEEK, week);
 		initialValues.put(KEY_SEQUENCE, sequence);
 		initialValues.put(KEY_SUBJECT, subject);
 		initialValues.put(KEY_PLACE, place);
 		initialValues.put(KEY_TEACHER, teacher);
 		initialValues.put(KEY_TIME, time);
 		initialValues.put(KEY_INFO, info);
 		return db.insert(DATABASE_TABLE, null, initialValues);
 	}
 
 	public boolean delete(String week, String sequence) {
 		return db.delete(DATABASE_TABLE, KEY_WEEK + "='" +week + "' and " + KEY_SEQUENCE
 				+ "='" + sequence + "'", null) > 0;
 	}
 
 	public Cursor queryALL() {
 		return db.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_WEEK, KEY_SEQUENCE,
 				KEY_SUBJECT, KEY_PLACE, KEY_TEACHER, KEY_TIME, KEY_INFO },
 				null, null, null, null, null);
 	}
 
 	public Cursor query(String week, String sequence) throws SQLException {
 		Log.v(TAG, "query");
 		Cursor mCursor = db.query(false, DATABASE_TABLE, new String[] { KEY_ID, 
 				KEY_WEEK, KEY_SEQUENCE, KEY_SUBJECT, KEY_PLACE, KEY_TEACHER,
 				KEY_TIME, KEY_INFO }, KEY_WEEK + "='" + week + "' and "
 				+ KEY_SEQUENCE + "='" + sequence + "'", null, null, null, null,
 				null);
 		if (mCursor != null) {
 			mCursor.moveToFirst();
 		}
 		return mCursor;
 	}
 /*
 	public void test() {
 		//select * from shedule where strftime('%H:%M', 'now', 'localtime')-time<1
 		//String sql = "select (strftime('%m', 'startTime') - strftime('%m', '8:00')) from shedule";
 		String sql = "select * from shedule where strftime('%H:%M', 'now', 'localtime')-startTime<8";
 		Cursor cursor = db.rawQuery(sql, null);
 		
 		if (cursor != null && cursor.getCount() > 0) {
 			int count = cursor.getCount();
 			Log.v(TAG, "count = " + count);
 			cursor.moveToFirst();
 			String result = cursor.getString(0);
 			Log.v(TAG, "result = " + result);
 			
 			int index = cursor.getColumnIndex("id");
 			Log.v(TAG, "index = " + index);
 			int columnCount = cursor.getColumnCount();
 			Log.v(TAG, "columnCount = " + columnCount);
 			String[] names = cursor.getColumnNames();
 			String sss = "";
 			for (int j = 0; j < names.length; j++) {
 				Log.v(TAG, "ColumnName = " + names[j]);
 				sss = sss + " " + names[j];
 			}
 			Toast.makeText(context, sss, Toast.LENGTH_LONG).show();
 		} else {
 			Log.v(TAG, "cursor = null or count = 0");
 		}
 	}*/
 	
 	public Cursor findCurrentClass(String week) {
 		String sql = "select * from shedule where strftime('%H:%M', 'now', 'localtime')-startTime=0 and week=" + "'" + week + "'";
 		Cursor cursor = db.rawQuery(sql, null);
 		if (cursor != null) {
 			cursor.moveToFirst();
 		} 
 		return cursor;
 	}
 	public Cursor getTodayLeftClass(String week) {
 		String sql = "select * from shedule where startTime > strftime('%H:%M', 'now', 'localtime') and week=" + "'" + week + "' order by startTime asc limit 1";
 		Cursor cursor = db.rawQuery(sql, null);
 		if (cursor != null) {
 			cursor.moveToFirst();
 		} 
 		return cursor;
 	}
 	public boolean update(String week, String sequence, String subject,
 			String place, String teacher, String time, String info) {
 		ContentValues args = new ContentValues();
 		args.put(KEY_SUBJECT, subject);
 		args.put(KEY_PLACE, place);
 		args.put(KEY_TEACHER, teacher);
 		args.put(KEY_TIME, time);
 		args.put(KEY_INFO, info);
 		return db.update(DATABASE_TABLE, args, KEY_WEEK + "='" + week
 				+ "' and " + KEY_SEQUENCE + "='" + sequence + "'", null) > 0;
 	}
 }
