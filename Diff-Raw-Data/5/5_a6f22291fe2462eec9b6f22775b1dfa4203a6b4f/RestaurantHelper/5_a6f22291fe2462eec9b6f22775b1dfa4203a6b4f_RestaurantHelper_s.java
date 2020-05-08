 package apt.tutorial;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class RestaurantHelper extends SQLiteOpenHelper {
 	private static final String DATABASE_NAME = "lunchlist.db";
 	private static final int SCHEMA_VERSION = 3;
 	
 	// SQL
	private static final String ALL_COLUMNS = "*"; // replace with *?
 	private static final String GET_ALL_QUERY = "SELECT " + ALL_COLUMNS + " FROM restaurants ORDER BY ";
 	private static final String GET_BY_ID_QUERY = "SELECT " + ALL_COLUMNS + " FROM restaurants WHERE _ID=?";
	private static final String CREATE_TABLE = "CREATE TABLE restaurants (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, address TEXT, type TEXT, notes TEXT, feed TEXT)";
 	private static final String ADD_FEED_COLUMN = "ALTER TABLE restaurants ADD COLUMN feed TEXT";
 	private static final String ADD_LAT_COLUMN = "ALTER TABLE restaurants ADD COLUMN latitude REAL";
 	private static final String ADD_LON_COLUMN = "ALTER TABLE restaurants ADD COLUMN longitude REAL";
 	
 	private enum RestaurantColumns {
 		ID (0),
 		NAME (1),
 		ADDRESS (2),
 		TYPE (3),
 		NOTES (4),
 		FEED (5),
 		LATITUDE (6),
 		LONGITUDE (7);
 		
 		private int index;
 		
 		private RestaurantColumns(int index) {
 			this.index = index;
 		}
 		
 		public int getIndex() {
 			return index;
 		}
 	}
 	
 	public RestaurantHelper(Context context) {
 		super(context, DATABASE_NAME, null, SCHEMA_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL(CREATE_TABLE);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		/*
 		 * 1 => 2: Add feed column (text)
 		 * 2 => 3: Add lat (real) & lon (real) 
 		 */
 		
 		if (oldVersion < 2) {
 			db.execSQL(ADD_FEED_COLUMN);
 		}
 		
 		if (oldVersion < 3) {
 			db.execSQL(ADD_LAT_COLUMN);
 			db.execSQL(ADD_LON_COLUMN);
 		}
 	}
 	
 	public void insert(String name, String address, String type, String notes, String feed) {
 		ContentValues cv = new ContentValues();
 		
 		cv.put("name", name);
 		cv.put("address", address);
 		cv.put("type", type);
 		cv.put("notes", notes);
 		cv.put("feed", feed);
 		
 		getWritableDatabase().insert("restaurants", "name", cv);
 	}
 	
 	public void update(String id, String name, String address, String type, String notes, String feed) {
 		ContentValues cv = new ContentValues();
 		String[] args = { id };
 		
 		cv.put("name", name);
 		cv.put("address", address);
 		cv.put("type", type);
 		cv.put("notes", notes);
 		cv.put("feed", feed);
 		
 		getWritableDatabase().update("restaurants", cv,  "_ID=?", args);
 	}
 	
 	public void updateLocation(String id, double lat, double lon) {
 		ContentValues cv = new ContentValues();
 		String[] args = { id };
 		
 		cv.put("latitude", lat);
 		cv.put("longitude", lon);
 		
 		getWritableDatabase().update("restaurants", cv,  "_ID=?", args);
 	}
 	
 	public Cursor getAll(String orderBy) {
 		return getReadableDatabase().rawQuery(GET_ALL_QUERY + orderBy, null);
 	}
 	
 	public Cursor getById(String id) {
 		String[] args = { id };
 		
 		return getReadableDatabase().rawQuery(GET_BY_ID_QUERY, args);
 	}
 	
 	public String getName(Cursor c) {
 		return c.getString(RestaurantColumns.NAME.getIndex());
 	}
 	
 	public String getAddress(Cursor c) {
 		return c.getString(RestaurantColumns.ADDRESS.getIndex());
 	}
 	
 	public Restaurant.Type getType(Cursor c) {
 		return Restaurant.Type.valueOf(c.getString(RestaurantColumns.TYPE.getIndex()));
 	}
 	
 	public String getNotes(Cursor c) {
 		return c.getString(RestaurantColumns.NOTES.getIndex());
 	}
 	
 	public String getFeed(Cursor c) {
 		return c.getString(RestaurantColumns.FEED.getIndex());
 	}
 	
 	public double getLatitude(Cursor c) {
 		return c.getDouble(RestaurantColumns.LATITUDE.getIndex());
 	}
 	
 	public double getLongitude(Cursor c) {
 		return c.getDouble(RestaurantColumns.LONGITUDE.getIndex());
 	}
 }
