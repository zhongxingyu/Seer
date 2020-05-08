 package put.medicallocator.io.sqlite;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Arrays;
 import java.util.Map;
 
 import put.medicallocator.io.Facility;
 import put.medicallocator.io.IFacilityProvider;
 import put.medicallocator.io.sqlite.DatabaseContract.Tables;
 import android.content.Context;
 import android.content.res.AssetManager;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.location.Location;
 import android.os.Handler;
 import android.util.Log;
 
 /**
  * {@link DatabaseProvider} shall be used for querying, deleting, inserting the medical facilities
  * which are backed in SQLite database. This class implements {@link IFacilityProvider}.  
  */
 public class DatabaseProvider implements IFacilityProvider {
 
 	private static final String TAG = "DatabaseProvider";
 	
 	private DatabaseHelper dbHelper;
 	private Handler handler;
 	
 	public DatabaseProvider(Context context) {
 		dbHelper = new DatabaseHelper(context);
 	}
 
 	public void setAsyncParameters(Handler handler) {
 		this.handler = handler;
 	}
 	
 	public Cursor getFacilitiesWithinArea(AsyncQueryListener listener, 
 			Location lowerLeftLocation, Location upperRightLocation) 
 	throws Exception {
 		return getFacilitiesWithinArea(listener, lowerLeftLocation, upperRightLocation, null);
 	}
 	
 	public Cursor getFacilitiesWithinArea(AsyncQueryListener listener, 
 			Location lowerLeftLocation, Location upperRightLocation, String[] names) 
 	throws Exception {
 		String selection = 
 				Facility.Columns.LATITUDE + " > ? AND " +
 				Facility.Columns.LONGITUDE + " > ? AND " +
 				Facility.Columns.LATITUDE + " < ? AND " +
 				Facility.Columns.LONGITUDE + " < ?";
 		if (names != null && names.length > 0) {
 			selection += " AND " + prepareSQLClauseLike(Facility.Columns.NAME, names);
 		}
 				
 		final String[] selectionArgs = new String[] { 
 				Double.toString(lowerLeftLocation.getLatitude()),
 				Double.toString(lowerLeftLocation.getLongitude()),
 				Double.toString(upperRightLocation.getLatitude()),
 				Double.toString(upperRightLocation.getLongitude())
 		};
 
 		final Cursor cursor = 
 			queryDB(listener, 0, Tables.FACILITY, Facility.getDefaultProjection(), 
 					selection, selectionArgs, null);
 		
 		/* Return the Cursor */
 		return cursor;
 	}
 
 	public Cursor getFacilitiesWithinRadius(AsyncQueryListener listener, 
 			Location startLocation, int radius) 
 	throws Exception {
 		/* 
 		 * Due to lack of trigonometric function in SQLite side,
 		 * this can't be implemented. 
 		 */
 		throw new Exception("Unsupported operation in this provider.");
 	}
 	
 	public Cursor getFacilitiesWithinRadius(AsyncQueryListener listener, 
 			Location startLocation, int radius, String[] names) 
 	throws Exception {
 		/* 
 		 * Due to lack of trigonometric function in SQLite side,
 		 * this can't be implemented. 
 		 */
 		throw new Exception("Unsupported operation in this provider.");
 	}
 
 	public Cursor getFacilitiesWithinAddress(AsyncQueryListener listener, String address) {
 		final String selection = Facility.Columns.ADDRESS +" LIKE ?";
 		final String[] selectionArgs = new String[] { "%" + address + "%" };
 
 		final Cursor cursor = 
 			queryDB(listener, 0, Tables.FACILITY, Facility.getDefaultProjection(), 
 					selection, selectionArgs, null);
 		
 		/* Return the Cursor */
 		return cursor;
 	}
 
 	public Cursor getFacilities(AsyncQueryListener listener, String query)
 	throws Exception {
 		final String selection = 
 			Facility.Columns.ADDRESS +" LIKE ? OR " +
 			Facility.Columns.NAME +" LIKE ?";
 		final String[] selectionArgs = new String[] { 
 				"%" + query + "%",
 				"%" + query + "%"
 			};
 
 		final Cursor cursor = 
 			queryDB(listener, 0, Tables.FACILITY, Facility.getDefaultProjection(), 
 					selection, selectionArgs, Facility.Columns.ADDRESS);
 		
 		/* Return the Cursor */
 		return cursor;
 	}
 	
 	public Facility getFacility(Location location) {
 		final String selection = 
 				Facility.Columns.LATITUDE + " = ? AND " +
 				Facility.Columns.LONGITUDE + " = ?";
 		final String[] selectionArgs = new String[] { 
 				Double.toString(location.getLatitude()),
 				Double.toString(location.getLongitude()),
 		};
 		
 		final Cursor cursor = 
 			queryDB(null, 0, Tables.FACILITY, Facility.getDefaultProjection(), 
 					selection, selectionArgs, null);
 		
 		if (!cursor.moveToFirst()) return null;
 		if (cursor.getCount() > 1) {
 			Log.w(TAG, "Query returned " + cursor.getCount() + " rows, " +
 					"although it was meant to return single row. Returning first row.");
 		}
 		
 		final Map<String, Integer> columnMapping = Facility.getDefaultColumnMapping();
 		final Facility result = 
 			TypeConverter.getFacilityFromCursorCurrentPosition(columnMapping, cursor);
 		
 		return result;
 	}
 
 	public boolean insertFacility(Facility facility) {
 		return false;
 	}
 
 	public boolean removeFacility(Facility facility) {
 		return false;
 	}
 	
 	private Cursor queryDB(final AsyncQueryListener listener, final int token, final String table, 
 			final String[] projection, 
 			final String selection, final String[] selectionArgs, final String orderBy) {
 		Log.d(TAG, "Starting query -- " +
 				"table[" + table + "], " +
 				"projection[" + Arrays.toString(projection) + "], " +
 				"selection[" + selection + "], selectionArgs[" + Arrays.toString(selectionArgs) + "]");
 		
 		/* Sync/async? */
 		if (listener != null) {
 			/* Create and execute the new Thread */
 			new Thread(new Runnable() {
 				public void run() {
 					/* Query the database */
 					SQLiteDatabase db = dbHelper.getReadableDatabase();
 					final Cursor cursor = 
 						db.query(table, projection, selection, selectionArgs, null, null, orderBy);
 					
 					/* Make a callback from the handler's Thread */
 					final Map<String, Integer> mapping = Facility.getDefaultColumnMapping();
 					if (handler != null) {
 						handler.post(new Runnable() {
 							public void run() {
 								listener.onQueryComplete(token, cursor, mapping);
 							}
 						});
 					} else {
 						listener.onQueryComplete(token, cursor, mapping);
 					}
 				}
 			}).start();
 			
 			/* We have to return null, result will be returned by callback */
 			return null;
 		} else {
 			/* Query the database */
 			SQLiteDatabase db = dbHelper.getReadableDatabase();
 			final Cursor cursor = 
 				db.query(table, projection, selection, selectionArgs, null, null, null); 
 			
 			/* Return the Cursor */
 			return cursor;
 		}
 	}
 	
 	/**
 	 * Internal helper class to manage database connections.
 	 * TODO: Consider retrieving data from webservice instead of copying local-stored DB 
 	 * from the assets/ folder.
 	 */
 	private static class DatabaseHelper extends SQLiteOpenHelper {
 		private static final String TAG = "DatabaseHelper";
     	private static final String DB_PATH = "/data/data/put.medicallocator/databases/";
     	
     	private static final int MAX_CHUNK_COUNT = 10;
     	
 		private Context context;
 
 		public DatabaseHelper(Context context) {
 			super(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);
 			this.context = context;
 			createAndCopyDBIfDoesNotExist();
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			Log.d(TAG, "Executing onCreate()");
 			/* Due to copying the DB from the local assets/ folder, this is commented out. */
 			/*db.execSQL("CREATE TABLE " + Tables.FACILITY + " ("
 	                + Facility.Columns._ID + " " + FacilityColumnsParams.PARAM_ID + ","
 	                + Facility.Columns.NAME + " " + FacilityColumnsParams.PARAM_NAME + ","
 	                + Facility.Columns.ADDRESS + " " + FacilityColumnsParams.PARAM_ADDRESS + ","
 	                + Facility.Columns.PHONE + " " + FacilityColumnsParams.PARAM_PHONE + ","
 	                + Facility.Columns.EMAIL + " " + FacilityColumnsParams.PARAM_EMAIL + ","
 	                + Facility.Columns.LATITUDE + " " + FacilityColumnsParams.PARAM_LATITUDE + ","
 	                + Facility.Columns.LONGITUDE + " " + FacilityColumnsParams.PARAM_LONGITUDE + ","
 	                + ")");	*/
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			/* Due to copying the DB from the local assets/ folder, this is commented out. */
 			Log.d(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);
 			/*if (oldVersion != DatabaseContract.DATABASE_VERSION) {
 		        Log.d(TAG, "onUpgrade(): Dropping all tables!");
 				db.execSQL("DROP TABLE IF EXISTS " + Tables.FACILITY);
 				onCreate(db);
 			}*/
 		}
 		
 		/**
 	     * Creates an empty database on the system and rewrites it with your own database.
 	     */
 	    public void createAndCopyDBIfDoesNotExist() {
 	    	boolean dbExists = checkDB();
 	    	Log.d(TAG, "Does the DB already exists? Result: " + dbExists);
 	    	if (dbExists) {
 	    		// Do nothing - database already exists.
 	    	} else{
 	    		// By calling this method an empty database will be created into the default 
 	    		// application path and we'll be able to overwrite that database with other database.
 		    	Log.d(TAG, "Forcing the Android to create the empty DB.");
 	        	getReadableDatabase();
 	 
 	        	try {
 	    			copyDB();
 	    		} catch (IOException e) {
 	        		throw new Error("Error during copying the database.");
 	        	} finally {
 	        		close();
 	        	}
 	    	}
 	    }
 	 
 	    /**
 	     * Check if the database already exists to avoid re-copying the DB each time when the 
 	     * application is opened.
 	     */
 	    private boolean checkDB() {
 	    	SQLiteDatabase checkDB = null;
 	    	try{
 	    		String myPath = DB_PATH + DatabaseContract.DATABASE_NAME;
 	    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
 	    	} catch (SQLiteException e) {
 	    		// Database doesn't exist yet.
 	    	} finally {
 	    		if (checkDB != null)
 	    			checkDB.close();
 	    	}
 	    	return checkDB != null ? true : false;
 	    }
 	 
 	    /**
 	     * Copies your database from your local assets-folder to the recently created empty 
 	     * database in the default application directory. From there it can be managed as always.
 	     * The whole operation is done by transferring the bytestream.
 	     * */
 	    private void copyDB() throws IOException {
 	    	Log.d(TAG, "Overwriting the empty DB with the deployed along with the application one.");
 	    	
 	    	final AssetManager assetManager = context.getAssets();
 
 	    	// Path to the recently created empty DB.
 	    	String outFileName = DB_PATH + DatabaseContract.DATABASE_NAME;
 	 
 	    	// Open the empty DB as the output stream.
 	    	OutputStream myOutput = new FileOutputStream(outFileName);
 	    	byte[] buffer = new byte[1024];
 	    	int length;
 	    	
 	    	// Iterate through the DB chunks and append them to the output stream one after another.
 	    	String[] fileList = assetManager.list("");
 	    	Arrays.sort(fileList);
 	    	for (int i=0; i<MAX_CHUNK_COUNT; i++) {
 		    	// Open the local-stored DB chunk as the input stream.
 	    		String fileName = String.format(DatabaseContract.DATABASE_NAME + ".%d", i);
 	    		if (Arrays.binarySearch(fileList, fileName) < 0) break;
 	    		
 	    		InputStream myInput = assetManager.open(fileName);
 
 	    		// Transfer bytes from the input-stream to the output-stream.
 	    		while ((length = myInput.read(buffer))>0){
 		    		myOutput.write(buffer, 0, length);
 		    	}
 	    		
 	    		// Close the input stream.
 		    	myInput.close();
 	    	}
 	 
 	    	// Flush and close the output stream.
 	    	myOutput.flush();
 	    	myOutput.close();
 	    }
 	}
 	
 	private static String prepareSQLClauseLike(String quailfiedField, String[] values) {
 		StringBuilder builder = new StringBuilder();
 		final int count = values.length;
 		builder.append("(");
 		for (int i=0; i<count-1; i++) {
			builder.append(quailfiedField).append(" LIKE '% ").append(values[i]).append(" %' OR ");
 		}
		builder.append(quailfiedField).append(" LIKE '% ").append(values[count-1]).append(" %')");
 		return builder.toString();
 	}
 
 }
