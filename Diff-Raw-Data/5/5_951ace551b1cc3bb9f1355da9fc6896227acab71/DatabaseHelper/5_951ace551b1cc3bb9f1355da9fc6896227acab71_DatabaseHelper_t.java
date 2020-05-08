 package com.quackware.tric.database;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import com.quackware.tric.database.SelectType.StatType;
 import com.quackware.tric.database.SelectType.TimeFrame;
 import com.quackware.tric.stats.Stats;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DatabaseHelper extends SQLiteOpenHelper{
 	
 	private static final String TAG = "DatabaseHelper";
 	
 	private static final String DATABASE_NAME = "tricDB";
 	private static final int DATABASE_VERSION = 2;
 	
 	private static final boolean rebuild = true;
 	
 	private static final String TABLE_CREATE_START = "CREATE TABLE IF NOT EXISTS ";
 	private static final String TABLE_CREATE_END = " (_id INTEGER PRIMARY KEY, DATA STRING, TIMESTAMP DATE);";
 	private static final String TABLE_DROP = "DROP TABLE IF EXISTS ";
 	
 	public DatabaseHelper(Context context)
 	{
 		super(context,DATABASE_NAME,null,DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		//Add check for all database tables here.
 		for(int i = 0;i<Stats.mStatsNames.size();i++)
 		{
 			db.execSQL(TABLE_CREATE_START + Stats.mStatsNames.get(i) + TABLE_CREATE_END);
 		}
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
 	{
 		if (oldVersion < DATABASE_VERSION && rebuild)
 		{
 			//In the future this should be changed to add new tables in instead of just dropping
 			this.dropTables(db);
 			onCreate(db);
 		}
 	}
 	
 	public ArrayList<StatData> selectStats(String pName,StatType pType, TimeFrame pTimeFrame)
 	{
 		SQLiteDatabase db = getWritableDatabase();
 		String dataFilter = getDataTypeFilter(pType);
 		String selection = getTimeFrameFilter(pTimeFrame);
 
 		Cursor c = db.query(
 				pName, 
 				new String[] {dataFilter,"TIMESTAMP"},
 				selection,
 				null,
 				null,
 				null,
 				"DATA DESC");
 		
 		c.moveToFirst();
 		String data = c.getString(0);
 		String dateTime = c.getString(1);
 		c.close();
 		db.close();
 		ArrayList<StatData> sdList = new ArrayList<StatData>();
 		StatData sd = new StatData();
 		sd.mData = data;
 		sd.mTimestamp = dateTime;
 		sdList.add(sd);
 		return sdList;
 	}
 	
 	private String getTimeFrameFilter(TimeFrame pTimeFrame)
 	{
 		String selection = null;
 		if(pTimeFrame != null)
 		{
 			switch(pTimeFrame)
 			{
 			case DAY:
 				selection = "WHERE TIMESTAMP BETWEEN datetime('now','start of day') AND datetime('now','localtime')";
 				break;
 			case WEEK:
 				selection = "WHERE TIMESTAMP BETWEEN datetime('now',-6 days') AND datetime('now','localtime')";
 				break;
 			case MONTH:
 				selection = "WHERE TIMESTAMP BETWEEN datetime('now','start of month') AND datetime('now','localtime')";
 				break;
 			case YEAR:
 				selection = "WHERE TIMESTAMP BETWEEN datetime('now','start of year' AND datetime('now','localtime')";
 				break;
 			case ALLTIME:
 				break;
 			default:
 				break;
 			}
 		}
 		return selection;
 	}
 	
 	private String getDataTypeFilter(StatType pType)
 	{
 		String dataFilter = null;
 		if(pType != null)
 		{
 			switch(pType)
 			{
 			case HIGHEST:
 				dataFilter = "MAX(DATA)";
 				break;
 			case LOWEST:
 				dataFilter = "MIN(DATA)";
 				break;
 			case AVERAGE:
 				dataFilter = "AVG(DATA)";
 				break;
 			case MEDIAN:
 				//change later
 				dataFilter = "DATA";
 				break;
 			case STDEV:
 				//change later
 				dataFilter = "DATA";
 				break;
 			default:
 				dataFilter = "DATA";
 				break;
 			}
 		}
 		return dataFilter;
 	}
 	
 	public void insertNewStat(Stats pStats)
 	{
 		SQLiteDatabase db = getWritableDatabase();
 		ContentValues values = new ContentValues();
 		
 		values.put("DATA",pStats.getStats().toString());
 		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		Date date = new Date();
 		values.put("TIMESTAMP",dateFormat.format(date));
 		
 		db.insert(pStats.getName(), null, values);
		
 		db.close();
 	}
 	
 	private void dropTables(SQLiteDatabase db)
 	{
 		for(int i = 0;i<Stats.mStatsNames.size();i++)
 		{
 			db.execSQL(TABLE_DROP + Stats.mStatsNames.get(i));
 		}
 	}
 	
 	public class StatData
 	{
 		public String mData;
 		public String mTimestamp;
 	}
 
 }
