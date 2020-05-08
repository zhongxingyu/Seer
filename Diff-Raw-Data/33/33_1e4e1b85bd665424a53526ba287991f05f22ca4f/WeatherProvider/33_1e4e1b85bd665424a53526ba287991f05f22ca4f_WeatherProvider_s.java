 package com.jsr.model;
 
 import android.content.ContentProvider;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 import android.provider.BaseColumns;
 
 public class WeatherProvider extends ContentProvider {
 	// Content URI
	public static final String AUTHORITY = "com.jsr.model";
 	// table weather name
 	public static final String TABLE_WIDGET = "weatherwidget";
 	// table weather detail name
 	private static final String TABLE_FORECAST = "weatherforecast";
 	private DatabaseHelper dbhelper;
 
 	public static class WeatherWidgets implements BaseColumns {
 		public static final Uri CONTENT_URI = Uri.parse("content://"
 				+ AUTHORITY + "/widgets");
 		public static final String FORECAST_END = "forecasts";
 
 		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/widgets";
 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/widgets";
 	}
 
 	public static class WeatherDetails implements BaseColumns {
 		public static final Uri CONTENT_URI = Uri.parse("content://"
 				+ AUTHORITY + "/forecasts");
 
 		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/forecasts";
 		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/forecasts";
 
 	}
 
 	private static class DatabaseHelper extends SQLiteOpenHelper {
 		private static final String DATABASE_NAME = "weather.db";
 		private static final int DATABASE_VERSION = 2;
 
 		public DatabaseHelper(Context context) {
 
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 			// TODO Auto-generated constructor stub
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			// TODO Auto-generated method stub
 			db.execSQL("CREATE TABLE " + TABLE_WIDGET + "(" + BaseColumns._ID
 					+ " INTEFER PRIMARY KEY," + DataWidget.CITY + " TEXT,"
 					+ DataWidget.UPDATEMILIS + " INTEGER,"
 					+ DataWidget.ISCONFIGURED + " INTEGER,"
 					+ DataWidget.POSTCODE + " TEXT," + DataWidget.FORECASTDATE
 					+ " INTEGER," + DataWidget.CONDITION + " TEXT,"
 					+ DataWidget.TEMPF + " INTEGER," + DataWidget.TEMPC
 					+ " INTEGER," + DataWidget.HUMIDITY + " TEXT,"
 					+ DataWidget.ICON + " TEXT," + DataWidget.WINDCONDITION
 					+ " TEXT," + DataWidget.LASTUPDATETIME + " INTEGER);");
 
 			db.execSQL("CREATE TABLE " + TABLE_FORECAST + " ("
 					+ BaseColumns._ID + " INTEGER PRIMARY KEY,"
 					+ DetailDateWidget.WIDGET_ID + " INTEGER,"
 					+ DetailDateWidget.DAYOFWEEK + " TEXT,"
 					+ DetailDateWidget.LOW + " INTEGER,"
 					+ DetailDateWidget.HIGHT + " INTEGER,"
 					+ DetailDateWidget.ICON + " TEXT,"
 					+ DetailDateWidget.CONDITION + " TEXT);");
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			// TODO Auto-generated method stub
 			int version = oldVersion;
 			if (version != DATABASE_VERSION) {
 				db.execSQL("DROP TABLE IF EXISTS" + TABLE_WIDGET);
 				db.execSQL("DROP TABLE IF EXISTS" + TABLE_FORECAST);
 				onCreate(db);
 			}
 		}
 	}
 
 	private static final UriMatcher uriMatcher = new UriMatcher(
 			UriMatcher.NO_MATCH);
 	private static final int WIDGETS = 101;
 	private static final int WIDGETS_ID = 102;
 	private static final int WIDGETS_FORECASTS = 103;
 
 	private static final int FORECASTS = 201;
 	private static final int FORECASTS_ID = 202;
 	static {
 		uriMatcher.addURI(AUTHORITY, "widgets", WIDGETS);
 		uriMatcher.addURI(AUTHORITY, "widgets/#", WIDGETS_ID);
 		uriMatcher.addURI(AUTHORITY, "widgets/#/forecasts", WIDGETS_FORECASTS);
 
 		uriMatcher.addURI(AUTHORITY, "forecasts", FORECASTS);
 		uriMatcher.addURI(AUTHORITY, "forecasts/#", FORECASTS_ID);
 	}
 
 	@Override
 	public int delete(Uri uri, String selection, String[] selectionArgs) {
 		// TODO Auto-generated method stub
 		SQLiteDatabase db = dbhelper.getReadableDatabase();
 
 		int count = 0;
 		switch (uriMatcher.match(uri)) {
 		case WIDGETS:{
 			count = db.delete(TABLE_WIDGET, selection, selectionArgs);
 			break;}
 		case WIDGETS_ID:{
 			long appWidgetId = Long.parseLong(uri.getPathSegments().get(1));
 			count = db.delete(TABLE_WIDGET,
 					BaseColumns._ID + "=" + appWidgetId, null);
 			count += db.delete(TABLE_FORECAST, DetailDateWidget.WIDGET_ID + "="
 					+ appWidgetId, null);
 			break;}
 		case WIDGETS_FORECASTS:{
 			long appWidgetId = Long.parseLong(uri.getPathSegments().get(1));
 			if(selection==null){
 				selection="";
 			}else{
 				selection="("+selection+")AND";
 			}
 			selection+=DetailDateWidget.WIDGET_ID+"="+appWidgetId;
 			count=db.delete(TABLE_FORECAST, selection, selectionArgs);
 			break;}
 		case FORECASTS:{
 			count=db.delete(TABLE_FORECAST, selection, selectionArgs);
 			
 			break;
 		}
 		case FORECASTS_ID:{
 			long forecastId=Long.parseLong(uri.getPathSegments().get(1));
 			count+=db.delete(TABLE_FORECAST, BaseColumns._ID+"="+forecastId, null);
 			break;
 		}
 		   default:
 			   throw new UnsupportedOperationException(); 
 		}
 		return count;
 	}
 
 	@Override
 	public String getType(Uri uri) {
 		// TODO Auto-generated method stub
 		switch (uriMatcher.match(uri)) {
         case WIDGETS:
             return WeatherWidgets.CONTENT_TYPE;
         case WIDGETS_ID:
             return WeatherWidgets.CONTENT_ITEM_TYPE;
         case WIDGETS_FORECASTS:
             return WeatherDetails.CONTENT_TYPE;
 		case FORECASTS:
 			return WeatherDetails.CONTENT_TYPE;
 		case FORECASTS_ID:
 			return WeatherDetails.CONTENT_ITEM_TYPE;
 		
 	}
 		throw new IllegalStateException();
 	}
 	@Override
 	public Uri insert(Uri uri, ContentValues values) {
 		// TODO Auto-generated method stub
 		SQLiteDatabase db = dbhelper.getWritableDatabase();
 
 		Uri resultUri = null;
 
 		switch (uriMatcher.match(uri)) {
 		case WIDGETS: {
             long rowId = db.insert(TABLE_WIDGET, DataWidget.POSTCODE, values);
             if (rowId != -1) {
                 resultUri = ContentUris.withAppendedId(WeatherWidgets.CONTENT_URI, rowId);
             }
             break;
         }
         case WIDGETS_FORECASTS: {
             // Insert a forecast into a specific widget
             long appWidgetId = Long.parseLong(uri.getPathSegments().get(1));
             values.put(DetailDateWidget.WIDGET_ID, appWidgetId);
             long rowId = db.insert(TABLE_FORECAST, DetailDateWidget.CONDITION, values);
             if (rowId != -1) {
                 resultUri = ContentUris.withAppendedId(WeatherDetails.CONTENT_URI, rowId);
             }
             break;
         }
         case FORECASTS: {
 			long rowId = db.insert(TABLE_FORECAST,
 					DetailDateWidget.CONDITION, values);
 			if (rowId != -1) {
 				resultUri = ContentUris.withAppendedId(WeatherDetails.CONTENT_URI,
 						rowId);
 			}
 			break;
 		}
 		default:
 			throw new UnsupportedOperationException();
 		}
 
 		return resultUri;
 	}
 
 	@Override
 	public boolean onCreate() {
 		// TODO Auto-generated method stub
 		dbhelper = new DatabaseHelper(getContext());
 		return true;
 		
 	}
 
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection,
 			String[] selectionArgs, String sortOrder) {
 		// TODO Auto-generated method stub
 		SQLiteDatabase db = dbhelper.getReadableDatabase();
 		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
 
 		String limit = null;
 
 		switch (uriMatcher.match(uri)) {
 		case WIDGETS: {
             qb.setTables(TABLE_WIDGET);
             break;
         }
         case WIDGETS_ID: {
             String appWidgetId = uri.getPathSegments().get(1);
             qb.setTables(TABLE_WIDGET);
             qb.appendWhere(BaseColumns._ID + "=" + appWidgetId);
             break;
         }
         case WIDGETS_FORECASTS: {
             // Pick all the forecasts for given widget, sorted by date and
             // importance
             String appWidgetId = uri.getPathSegments().get(1);
             qb.setTables(TABLE_FORECAST);
             qb.appendWhere(DetailDateWidget.WIDGET_ID + "=" + appWidgetId);
             sortOrder = BaseColumns._ID + " ASC";
             break;
         }case FORECASTS: {
 			qb.setTables(TABLE_FORECAST);
 			break;
 		}
 		case FORECASTS_ID: {
 			String forecastId = uri.getPathSegments().get(1);
 			qb.setTables(TABLE_FORECAST);
 			qb.appendWhere(BaseColumns._ID + "=" + forecastId);
 			break;
 		}
 		}
 
 		return qb.query(db, projection, selection, selectionArgs, null, null,
 				sortOrder, limit);
 	}
 
 	@Override
 	public int update(Uri uri, ContentValues values, String selection,
 			String[] selectionArgs) {
 		// TODO Auto-generated method stub
 		SQLiteDatabase db = dbhelper.getWritableDatabase();
 
 		switch (uriMatcher.match(uri)) {
 		case WIDGETS: {
             return db.update(TABLE_WIDGET, values, selection, selectionArgs);
         }
         case WIDGETS_ID: {
             long appWidgetId = Long.parseLong(uri.getPathSegments().get(1));
             return db.update(TABLE_WIDGET, values, BaseColumns._ID + "=" + appWidgetId,
                     null);
         }
         case FORECASTS: {
 			return db.update(TABLE_FORECAST, values, selection, selectionArgs);
 		}
 		}
 
 		throw new UnsupportedOperationException();
 	}
 
 }
