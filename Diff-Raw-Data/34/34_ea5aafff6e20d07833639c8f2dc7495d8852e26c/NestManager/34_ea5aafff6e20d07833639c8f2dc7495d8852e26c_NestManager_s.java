 package org.fourdnest.androidclient;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.fourdnest.androidclient.comm.UnknownProtocolException;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.provider.BaseColumns;
 import android.util.Log;
 
 
 
 public class NestManager {
 	
 	private static final String TAG = NestDatabase.class.getSimpleName();
 	
 	static final String DB_NAME = "4dnest.nests.db";
	static final int DB_VERSION = 2;
 	
 	// Table columns
 	private static final String TABLE = "nest";
 	private static final String C_ID = BaseColumns._ID;
 	private static final String C_NAME = "name";
 	private static final String C_DESCRIPTION = "description";
 	private static final String C_ADDRESS = "address";
 	private static final String C_PROTOCOL = "protocol";
 	private static final String C_USERNAME = "username";
 	private static final String C_SECRETKEY = "secretkey";
 	
 	private static final String[] ALL_COLUMNS = new String[]{
 		C_ID, C_NAME, C_DESCRIPTION, C_ADDRESS,
 		C_PROTOCOL, C_USERNAME, C_SECRETKEY
 		
 	};
 	
 	/** Limit of number of Nests to return */
 	private static final String LIMIT = "100";
 	
 	private final NestDatabase nestDb;
 	
 	/**
 	 * Creates new NestManager with specified context
 	 * @param context
 	 */
 	public NestManager(Context context) {
 		this.nestDb = new NestDatabase(context);
 		
 		Log.d(TAG, "NestManager created");
 	}
 	
 	/**
 	 * 
 	 * @return ArrayList<Nest> List of saved nests
 	 */
 	public List<Nest> listNests() {
 		
 		SQLiteDatabase db = this.nestDb.getReadableDatabase();
 		
 		Cursor result = db.query(TABLE,
 				ALL_COLUMNS, // Columns
 				null, // No WHERE
 				null, // No arguments in selection
 				null, // No GROUP BY
 				null, // No HAVING
 				C_NAME, // Order by name
 				LIMIT);
 		
 		List<Nest> nests = new ArrayList<Nest>();
 		
 		if(result.getCount() > 0) {
 			result.moveToFirst();
 			
 			while(!result.isAfterLast()) {
 				// Populate nest with cursor columns in the order specified above
 				Nest nest = this.extractNestFromCursor(result);
 				if(nest != null) {
 					nests.add(nest);
 				}				
 				result.moveToNext();
 			} 
 		}
 		
 		return nests;
 	}
 	
 	/**
 	 * 
 	 * @param id of nest
 	 * @return Nest with specified id or null
 	 */
 	public Nest getNest(int id) {
 
 		SQLiteDatabase db = this.nestDb.getReadableDatabase();
 		Cursor result = db.query(TABLE,
 				ALL_COLUMNS, // Columns
 				C_ID + "==" + id, // Where
 				null, // No arguments in selection
 				null, // No GROUP BY
 				null, // No HAVING
 				null, //No ORDER BY
 				"1"); // Limit 1
 		
 		
 		Nest nest = null;
 		if(result.getCount() > 0) {
 			result.moveToFirst();
 			// Populate nest with cursor columns in the order specified above
 			nest = this.extractNestFromCursor(result);
 			
 		} else {
 			Log.d(TAG, "Nest with id " + id + " not found");
 		}
 		
 		
 		return nest;
 		
 	}
 	
 	private Nest extractNestFromCursor(Cursor cursor) {
 		if(cursor == null) {
 			return null;
 		}
 		
 		int id = cursor.getInt(0);
 		String name = cursor.getString(1);
 		String descr = cursor.getString(2);
 		
 		URI uri;
 		try {
 			uri = new URI(cursor.getString(3));
 		} catch(URISyntaxException exc) {
 			uri = null;
 		}
 		
 		int protocolId = cursor.getInt(4);
 		String userName = cursor.getString(5);
 		String secretKey = cursor.getString(6);
 	
 		try {
 			return new Nest(id, name, descr, uri, protocolId, userName, secretKey);
 		} catch(UnknownProtocolException upe) {
 			Log.d(TAG, "UnknownProtocolException in stored Nest. ProtocolId " + protocolId);
 		}
 		return null;
 	}
 	
 	/**
 	 * Saves Nest to database, updating existing Nest with same id
 	 * and creating new one if necessary 
 	 * @param nest object to save
 	 * @return long row id or -1 on failure
 	 */
 	public long saveNest(Nest nest) {
 		
 		
 		SQLiteDatabase db = this.nestDb.getWritableDatabase();
 		
 		// API level 8 would have insertWithOnConflict, have to work around it
 		// and check for conflict and then either insert or update
 
 		// Check if nest with id exists
 		Cursor result = db.query(TABLE,
 				new String[] {C_ID},
 				C_ID + "==" + nest.getId(),
 				null, // No selection args
 				null, // No GROUP BY
 				null, // No HAVING
 				null, // No ORDER BY
 				"1"); // LIMIT 1
 		
 		// Create ContentValues object for Nest
 		ContentValues values = new ContentValues();
 		values.put(C_ID, nest.getId());
 		values.put(C_NAME, nest.getName());
 		values.put(C_DESCRIPTION, nest.getDescription());
 		values.put(C_ADDRESS, nest.getBaseURI() != null ? nest.getBaseURI().toString() : null);
 		values.put(C_PROTOCOL, nest.getProtocolId());
 		values.put(C_USERNAME, nest.getUserName());
 		values.put(C_SECRETKEY, nest.getSecretKey());
 		
 		long rowid;
 		if(result.getCount() > 0) {
 			// Update existing
 			rowid = db.replace(TABLE, null, values);
 			
 			if(rowid < 0) {
 				throw new SQLiteException("Error replacing existing nest with id + "
 						+ nest.getId() + " in database");
 			}
 			
 			Log.d(TAG, "Updated Nest in db");
 			
 		} else {
 			// Insert new row			
 			rowid = db.insert(TABLE, null, values);
 			if(rowid < 0) {
 				throw new SQLiteException("Error inserting new nest to database");
 			}
 			
 			Log.d(TAG, "Inserted new Nest to db");
 		}
 		
 		return rowid;
 	}
 	
 	/**
 	 * Closes database
 	 */
 	public void close() {
 		Log.d(TAG, "db closed");
 		this.nestDb.close();
 	}
 	
 	
 	
 	/**
 	 *  Actual database handler inside NestManager
 	 */
 	private class NestDatabase extends SQLiteOpenHelper {
 		
 		private Context context;
 		
 		public NestDatabase(Context context) {
 			super(context, DB_NAME, null, DB_VERSION);			
 			this.context = context;
 			
 			Log.d(TAG, "NestDatabase created");
 		}
 
 		// Called when DB is created for the first time (does not exist)
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			
 			// Prepare SQL table creation query 
 			String tableCreateQuery = String.format(
 						"CREATE TABLE %s(" +
 						"%s int PRIMARY KEY," +
 						"%s text," +
 						"%s text," +
 						"%s text," +
 						"%s text," +
 						"%s text," + 
 						"%s text)",
 						TABLE,
 						C_ID,
 						C_NAME,
 						C_DESCRIPTION,
 						C_ADDRESS,
 						C_PROTOCOL,
 						C_USERNAME,
 						C_SECRETKEY
 			);
 			
 			db.execSQL(tableCreateQuery);
 			
 			Log.d(TAG, "onCreated SQL: " + tableCreateQuery);
 		}
 
 		// Called when DB version number has changed
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			// TODO Add proper alter scripts for production use when version changes
 			
 			// For now, just drop and recreate
 			String tableDropQuery = String.format("DROP TABLE IF EXISTS %s", TABLE);
 			db.execSQL(tableDropQuery);
 			Log.d(TAG, "onUpgrade: Dropped existing table");
 			
 			onCreate(db);		
 		}
 		
 	}
 	
 }
