 package cz.duha.bioadresar.data;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Hashtable;
 import java.util.TreeSet;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class DatabaseHelper extends SQLiteOpenHelper {
 
 	private static String DB_PATH = "/data/data/cz.duha.bioadresar/databases/";
 
 	private static String DB_NAME = "bioadr";
 	
 	private static int DB_VERSION = 1;
 	
 	private static DatabaseHelper defaultDb = null;
 
 	private SQLiteDatabase db;
 
 	private static Context appContext = null;
 
 	public DatabaseHelper() {
 		super(appContext, DB_NAME, null, DB_VERSION);
 	}
 	
 	public static void setContext(Context context)
 	{
 		appContext = context;
 	}
 	
 	public static DatabaseHelper getDefaultDb()
 	{
 		if (defaultDb == null && appContext != null)
 		{
 			try {
 				defaultDb = new DatabaseHelper();
 				defaultDb.createDb();
				defaultDb.openDb();
			} catch (IOException e) {
				Log.e("db", "error opening db " + e.toString());
			}
 		}
 		return defaultDb; 
 	}
 
 	/**
 	 * Creates a empty database on the system and rewrites it with your own
 	 * database.
 	 * */
 	public void createDb() throws IOException {
 		boolean dbExist = checkDb();
 
 		if (dbExist) {
 			// do nothing - database already exist
 		} else {
 			this.getReadableDatabase();
 
 			try {
 				copyDb();
 			} catch (IOException e) {
 				throw new Error("Error copying database");
 			}
 		}
 
 	}
 
 	/**
 	 * Check if the database already exist to avoid re-copying the file each
 	 * time application is opened.
 	 * 
 	 * @return true if it exists, false if it doesn't
 	 */
 	private boolean checkDb() {
 		SQLiteDatabase checkDB = null;
 
 		try {
 			String myPath = DB_PATH + DB_NAME;
 			checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
 		} catch (SQLiteException e) {
 			// database does't exist yet.
 		}
 
 		if (checkDB != null) {
 			checkDB.close();
 		}
 
 		return checkDB != null ? true : false;
 	}
 
 	/**
 	 * Copies database from local assets-folder to the just created
 	 * empty database in the system folder, from where it can be accessed and
 	 * handled. This is done by transfering bytestream.
 	 * */
 	private void copyDb() throws IOException {
 		InputStream myInput = appContext.getAssets().open(DB_NAME);
 		String outFileName = DB_PATH + DB_NAME;
 		OutputStream myOutput = new FileOutputStream(outFileName);
 
 		// transfer bytes from the inputfile to the outputfile
 		byte[] buffer = new byte[1024];
 		int length;
 		while ((length = myInput.read(buffer)) > 0) {
 			myOutput.write(buffer, 0, length);
 		}
 
 		// Close the streams
 		myOutput.flush();
 		myOutput.close();
 		myInput.close();
 	}
 
 	public void openDb() throws SQLException {
 		String path = DB_PATH + DB_NAME;
 		db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
 	}
 
 	@Override
 	public synchronized void close() {
 		if (db != null)
 			db.close();
 
 		super.close();
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		// nothing to do
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		// nothing to do
 	}
 
 	public void setFilter(DataFilter filter) {
 		//TODO: implement this
 	}
 	
 	public void clearFilter() {
 		//TODO: implement this
 	}
 	
 	public Hashtable<Long, FarmInfo> getFarmsInRectangle(double lat1, double lon1, double lat2, double lon2) {
 		String[] columns = new String[] { "_id", "name", "gps_lat", "gps_long" };
 		String selection = "gps_lat >= ? AND gps_long >= ? AND gps_lat <= ? AND gps_long <= ?";
 		String[] args = new String[] {
 				Double.toString(lat1), Double.toString(lon1),
 				Double.toString(lat2), Double.toString(lon2)
 		};
 		Cursor c = db.query("farm", columns, selection, args, null, null, "gps_lat, gps_long");
 		Hashtable<Long, FarmInfo> result = new Hashtable<Long, FarmInfo>();
 		
 		c.moveToNext();
 		while (!c.isAfterLast()) {
 			FarmInfo farmInfo = new FarmInfo();
 
 			farmInfo.id = c.getLong(0);
 			farmInfo.name = c.getString(1);
 			farmInfo.lat = c.getDouble(2);
 			farmInfo.lon = c.getDouble(3);
 			
 			Log.d("farm info", farmInfo.id + "; " + farmInfo.name + "; " + farmInfo.lat + "; " + farmInfo.lon);
 			result.put(farmInfo.id, farmInfo);
 			c.moveToNext();
 		}
 		c.close();
 		
 		return result;
 	}
 	
 	public TreeSet<FarmInfo> getFarmInfoInDistance(double lat, double lon, int distanceInKm) {
 		//TODO: implement this
 		return null;
 	}
 	
 	public void fillDetails(FarmInfo info) {
 		// TODO: implement this
 	}
 
 }
