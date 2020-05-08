 /*******************************************************************************
  * Copyright (c) 2012 sfleury.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     sfleury - initial API and implementation
  ******************************************************************************/
 package org.gots;
 
 import java.util.ArrayList;
 
 import org.gots.action.AbstractActionSeed;
 import org.gots.garden.sql.GardenSQLite;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.net.Uri;
 import android.util.Log;
 
 public class DatabaseHelper extends SQLiteOpenHelper {
 	// ************************ DATABASE **************
 	private static final int DATABASE_VERSION = 13;
 	private static String DATABASE_NAME = "gots0";
 	public final static String AUTHORITY = "org.gots.providers.seeds";
 
 	private static final String TAG = "DatabaseHelper";
 
 	// ************************ FAMILY TABLE **************
 	public static final String FAMILY_TABLE_NAME = "family";
 
 	public static final String FAMILY_ID = "family_id";
 	public static final String FAMILY_NAME = "family_name";
 
 	//@formatter:off
 		private static final String CREATE_TABLE_FAMILY = "CREATE TABLE " + FAMILY_TABLE_NAME 
 				+ " ("+ FAMILY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
 				+ FAMILY_NAME + " VARCHAR(255)" 
 				
 				+ ");";
 	//@formatter:on
 
 	// ************************ SPECIE TABLE **************
 
 	public static final String SPECIE_TABLE_NAME = "specie";
 
 	public static final String SPECIE_ID = "specie_id";
 	public static final String SPECIE_FAMILY_ID = "specie_family_id";
 	public static final String SPECIE_NAME = "specie_name";
 
 	//@formatter:off
 			private static final String CREATE_TABLE_SPECIE = "CREATE TABLE " + SPECIE_TABLE_NAME 
 					+ " ("+ SPECIE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
 					+ SPECIE_FAMILY_ID + " INTEGER," 
 					+ SPECIE_NAME + " VARCHAR(255)" 					
 					+ ");";
 		//@formatter:on
 	// ************************ SEEDS TABLE **************
 	public static final String SEEDS_TABLE_NAME = "seeds";
 	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/seeds");
 	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.gots.seeds";
 
 	public static final String SEED_ID = "_id";
 	public static final String SEED_UUID = "uuid";
 	public static final String SEED_BARECODE = "barcode";
 	public static final String SEED_NAME = "name";
 	public static final String SEED_DESCRIPTION_GROWTH = "description_growth";
 	public static final String SEED_DESCRIPTION_CULTIVATION = "description_cultivation";
 	public static final String SEED_DESCRIPTION_DISEASES = "description_diseases";
 	public static final String SEED_DESCRIPTION_HARVEST = "description_harvest";
 
 	public static final String SEED_ORDER = "botanicorder";
 	public static final String SEED_FAMILY = "botanicfamily";
 	public static final String SEED_GENUS = "botanicgenus";
 	public static final String SEED_SPECIE = "botanicspecie";
 	public static final String SEED_VARIETY = "variety";
 	public static final ArrayList<AbstractActionSeed> actionToDo = new ArrayList<AbstractActionSeed>();
 	public static final ArrayList<AbstractActionSeed> actionDone = new ArrayList<AbstractActionSeed>();
 	public static final String SEED_DATESOWINGMIN = "datesowingmin";
 	public static final String SEED_DATESOWINGMAX = "datesowingmax";
 	public static final String SEED_DURATIONMIN = "durationmin";
 	public static final String SEED_DURATIONMAX = "durationmax";
 	public static final String SEED_URLDESCRIPTION = "urldescription";
 	public static final String SEED_ACTION1 = "action1";
 	public static final String SEED_NBSACHET = "nbsachet";
 
 	//@formatter:off
 
 	public static final String CREATE_TABLE_SEEDS = "CREATE TABLE " + SEEDS_TABLE_NAME 
 			+ " (" + SEED_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT," 
 			+ SEED_NAME + " VARCHAR(255)," 
 			+ SEED_UUID + " VARCHAR(255),"
 			+ SEED_SPECIE + " VARCHAR(255)," 
 			+ SEED_DESCRIPTION_GROWTH + " VARCHAR(255),"
 			+ SEED_DESCRIPTION_DISEASES + " VARCHAR(255)," 
 			+ SEED_DESCRIPTION_CULTIVATION + " VARCHAR(255)," 
 			+ SEED_DESCRIPTION_HARVEST + " VARCHAR(255)," 
 			+ SEED_BARECODE + " VARCHAR(255),"
 			+ SEED_FAMILY + " VARCHAR(255)," 
 			+ SEED_GENUS + " VARCHAR(255)," 
 			+ SEED_ORDER + " VARCHAR(255),"
 			+ SEED_ACTION1 + " VARCHAR(255)," 
 			+ SEED_VARIETY + " VARCHAR(255)," 
 			+ SEED_URLDESCRIPTION + " VARCHAR(255)," 
 			+ SEED_DATESOWINGMIN + " INTEGER,"
 			+ SEED_DATESOWINGMAX + " INTEGER," 
 			+ SEED_DURATIONMIN + " INTEGER," 
 			+ SEED_DURATIONMAX + " INTEGER,"
 			+ SEED_NBSACHET + " INTEGER"
 			+ ");";
 	//@formatter:on
 
 	// ************************ GROWINGSEEDS TABLE **************
 	public static final String GROWINGSEEDS_TABLE_NAME = "growseeds";
 	// public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
 	// + "/growseeds");
 	// public static final String CONTENT_TYPE =
 	// "vnd.android.cursor.dir/vnd.gots.growseeds";
 
 	public static final String GROWINGSEED_ID = "_id";
 	public static final String GROWINGSEED_SEED_ID = "seed_id";
 	public static final String GROWINGSEED_ALLOTMENT_ID = "allotment_id";
 	public static final String GROWINGSEED_DATESOWING = "datesowing";
 	public static final String GROWINGSEED_DATELASTWATERING = "datelastwatering";
 
 	//@formatter:off
 	public static final String CREATE_TABLE_GROWINDSEEDS = "CREATE TABLE " + GROWINGSEEDS_TABLE_NAME 
 			+ " ("+ GROWINGSEED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
 			+ GROWINGSEED_SEED_ID + " INTEGER NOT NULL,"
 			+ GROWINGSEED_ALLOTMENT_ID + " INTEGER NOT NULL," 
 			+ GROWINGSEED_DATESOWING + " INTEGER,"
 			+ GROWINGSEED_DATELASTWATERING + " DATETIME"
 			// + "  FOREIGN KEY(" + SEED_ID
 			// + ") REFERENCES " + SeedSQLite.SEEDS_TABLE_NAME + "(" +
 			// SeedSQLite.SEED_ID + ")"
 			+ ");";
 	//@formatter:on
 
 	// ************************ ACTIONSEED TABLE **************
 	public static final String ACTIONSEEDS_TABLE_NAME = "actionseed";
 	// public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
 	// + "/actionseed");
 	// public static final String CONTENT_TYPE =
 	// "vnd.android.cursor.dir/vnd.gots.actionseed";
 	public static final String ACTIONSEED_ID = "_id";
 	public static final String ACTIONSEED_GROWINGSEED_ID = "growingseed_id";
 	public static final String ACTIONSEED_ACTION_ID = "action_id";
 	public static final String ACTIONSEED_DURATION = "action_seed_duration";
 	public static final String ACTIONSEED_DATA = "data";
 
 	// public static final String SEED_ID = "seed_id";
 
 	//@formatter:off
 	public static final String ACTIONSEED_DATEACTIONDONE = "dateactiondone";
 	private static final String CREATE_TABLE_ACTIONSEED = "CREATE TABLE " + ACTIONSEEDS_TABLE_NAME 
 			+ " ("+ACTIONSEED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
 			+ACTIONSEED_GROWINGSEED_ID + " INTEGER," 
 			+ ACTIONSEED_ACTION_ID + " INTEGER," 
 			+ ACTIONSEED_DATEACTIONDONE	+ " INTEGER,"
 			+ ACTIONSEED_DURATION+ " INTEGER,"
 			+ ACTIONSEED_DATA+ " VARCHAR(255)"
 
 			// + DATE_LAST_WATERING + " DATETIME"
 			// + "  FOREIGN KEY(" + SEED_ID
 			// + ") REFERENCES " + SeedSQLite.SEEDS_TABLE_NAME + "(" +
 			// SeedSQLite.SEED_ID + ")"
 			+ ");";
 	//@formatter:on
 
 	// ************************ WEATHER TABLE **************
 	public static final String WEATHER_TABLE_NAME = "weather";
 
 	public static final String WEATHER_ID = "_id";
 	public static final String WEATHER_DAYOFYEAR = "dayofyear";
 	public static final String WEATHER_YEAR = "year";
 	public static final String WEATHER_TEMPCELCIUSMIN = "tempcelciusmin";
 	public static final String WEATHER_TEMPCELCIUSMAX = "tempcelciusmax";
 	public static final String WEATHER_TEMPFAHRENHEIT = "tempfahrenheit";
 	public static final String WEATHER_CONDITION = "condition";
 	public static final String WEATHER_WINDCONDITION = "windcondition";
 	public static final String WEATHER_HUMIDITY = "humidity";
 	public static final String WEATHER_ICONURL = "iconurl";
 	public static final String WEATHER_DATE = "date";
 
 	//@formatter:off
 		public static final String CREATE_TABLE_WEATHER = "CREATE TABLE " + WEATHER_TABLE_NAME 
 				+ " (" + WEATHER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
 				+ GardenSQLite.ACTION_NAME + " STRING,"
 				+ WEATHER_CONDITION + " STRING,"
 				+ WEATHER_WINDCONDITION + " STRING,"
 				+ WEATHER_DAYOFYEAR + " INTEGER,"	
 				+ WEATHER_YEAR + " INTEGER,"		
 				+ WEATHER_TEMPCELCIUSMIN + " INTEGER,"
 				+ WEATHER_TEMPCELCIUSMAX + " INTEGER,"
 				+ WEATHER_TEMPFAHRENHEIT + " INTEGER,"
 				+ WEATHER_HUMIDITY + " INTEGER,"
 				+ WEATHER_ICONURL + " String,"
 				+ WEATHER_DATE + " INTEGER"
 				+ ");";
 	//@formatter:on
 
 	// ************************ ALLOTMENT TABLE **************
 	public static final String ALLOTMENT_TABLE_NAME = "allotment";
 	// public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
 	// + "/action");
 	// public static final String CONTENT_TYPE =
 	// "vnd.android.cursor.dir/vnd.gots.action";
 
 	// public static final String GROWINGSEED_ID = "growingseed_id";
 	public static final String ALLOTMENT_ID = "_id";
 	public static final String ALLOTMENT_NAME = "name";
 	//@formatter:off
 		public static final String CREATE_TABLE_ALLOTMENT = "CREATE TABLE " + ALLOTMENT_TABLE_NAME 
 				+ " (" + ALLOTMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
 				+ ALLOTMENT_NAME + " STRING"
 					+ ");";
 		//@formatter:on
 
 	public DatabaseHelper(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	public void setDatabase(int databaseId) {
 		DATABASE_NAME = "gots" + databaseId;
 		Log.d("setDatabase",DATABASE_NAME);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL(CREATE_TABLE_ALLOTMENT);
 		db.execSQL(CREATE_TABLE_SEEDS);
 		db.execSQL(CREATE_TABLE_GROWINDSEEDS);
 		db.execSQL(CREATE_TABLE_ACTIONSEED);
 		db.execSQL(CREATE_TABLE_WEATHER);
 		db.execSQL(CREATE_TABLE_FAMILY);
 		db.execSQL(CREATE_TABLE_SPECIE);
 
 		db.execSQL("Insert into " + FAMILY_TABLE_NAME + "(" + FAMILY_NAME + ") VALUES ('apiaceae')");
 		db.execSQL("Insert into " + FAMILY_TABLE_NAME + "(" + FAMILY_NAME + ") VALUES ('asteracae')");
 		db.execSQL("Insert into " + FAMILY_TABLE_NAME + "(" + FAMILY_NAME + ") VALUES ('brassicaceae')");
 		db.execSQL("Insert into " + FAMILY_TABLE_NAME + "(" + FAMILY_NAME + ") VALUES ('Cucurbitaceae')");
 		db.execSQL("Insert into " + FAMILY_TABLE_NAME + "(" + FAMILY_NAME + ") VALUES ('Fabaceae')");
 		db.execSQL("Insert into " + FAMILY_TABLE_NAME + "(" + FAMILY_NAME + ") VALUES ('Lamiaceae')");
 		db.execSQL("Insert into " + FAMILY_TABLE_NAME + "(" + FAMILY_NAME + ") VALUES ('Liliaceae')");
 		db.execSQL("Insert into " + FAMILY_TABLE_NAME + "(" + FAMILY_NAME + ") VALUES ('Solanaceae')");
 		db.execSQL("Insert into " + FAMILY_TABLE_NAME + "(" + FAMILY_NAME + ") VALUES ('aliaceae')");
 
 		db.execSQL("Insert into " + SPECIE_TABLE_NAME + "(" + SPECIE_NAME + ", " + SPECIE_FAMILY_ID
 				+ ") VALUES ('Brassica oleracea',3)");
 		db.execSQL("Insert into " + SPECIE_TABLE_NAME + "(" + SPECIE_NAME + ", " + SPECIE_FAMILY_ID
 				+ ") VALUES ('Daucus carota',1)");
 		db.execSQL("Insert into " + SPECIE_TABLE_NAME + "(" + SPECIE_NAME + ", " + SPECIE_FAMILY_ID
 				+ ") VALUES ('Solanum lycopersicum',8)");
 		db.execSQL("Insert into " + SPECIE_TABLE_NAME + "(" + SPECIE_NAME + ", " + SPECIE_FAMILY_ID
 				+ ") VALUES ('Cucumis sativus',4)");
 		db.execSQL("Insert into " + SPECIE_TABLE_NAME + "(" + SPECIE_NAME + ", " + SPECIE_FAMILY_ID
 				+ ") VALUES ('Lactuca sativa',2)");
 		db.execSQL("Insert into " + SPECIE_TABLE_NAME + "(" + SPECIE_NAME + ", " + SPECIE_FAMILY_ID
 				+ ") VALUES ('Brassica rapa',3)");
 		db.execSQL("Insert into " + SPECIE_TABLE_NAME + "(" + SPECIE_NAME + ", " + SPECIE_FAMILY_ID
 				+ ") VALUES ('Allium cepa',7)");
 		db.execSQL("Insert into " + SPECIE_TABLE_NAME + "(" + SPECIE_NAME + ", " + SPECIE_FAMILY_ID
 				+ ") VALUES ('Solanum tuberosum',8)");
 		db.execSQL("Insert into " + SPECIE_TABLE_NAME + "(" + SPECIE_NAME + ", " + SPECIE_FAMILY_ID
 				+ ") VALUES ('Raphanus sativus',3)");
 		db.execSQL("Insert into " + SPECIE_TABLE_NAME + "(" + SPECIE_NAME + ", " + SPECIE_FAMILY_ID
 				+ ") VALUES ('Allium porrum',9)");
 		db.execSQL("Insert into " + SPECIE_TABLE_NAME + "(" + SPECIE_NAME + ", " + SPECIE_FAMILY_ID
 				+ ") VALUES ('Phaseolus vulgaris',5)");
 
 		Log.i(TAG, "onCreate");
 
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
 				+ ", which will destroy all old data");
 		if (oldVersion == 8 && newVersion == 9) {
 			db.execSQL("ALTER TABLE " + WEATHER_TABLE_NAME + " ADD COLUMN " + WEATHER_YEAR + " INTEGER;");
 		} else if (oldVersion == 9 && newVersion == 10) {
 			db.execSQL("ALTER TABLE " + SEEDS_TABLE_NAME + " ADD COLUMN " + SEED_DESCRIPTION_CULTIVATION
 					+ " VARCHAR(255);");
 			db.execSQL("ALTER TABLE " + SEEDS_TABLE_NAME + " ADD COLUMN " + SEED_DESCRIPTION_DISEASES
 					+ " VARCHAR(255);");
 			db.execSQL("ALTER TABLE " + SEEDS_TABLE_NAME + " ADD COLUMN " + SEED_DESCRIPTION_HARVEST + " VARCHAR(255);");
 
 		} else if (oldVersion == 10 && newVersion == 11) {
 			db.execSQL("DROP TABLE IF EXISTS action");
 
 		}
 		
 		if (oldVersion < 12) {
 			db.execSQL("CREATE TEMPORARY TABLE backup" 
 			+ " ("+ACTIONSEED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
 			+ACTIONSEED_GROWINGSEED_ID + " INTEGER," 
 			+ ACTIONSEED_ACTION_ID + " INTEGER," 
 			+ ACTIONSEED_DATEACTIONDONE	+ " INTEGER,"
 			+ ACTIONSEED_DURATION+ " INTEGER"
 			+ ");");
 			db.execSQL("INSERT INTO backup (growingseed_id, action_id,  dateactiondone, action_seed_duration) SELECT growingseed_id, action_id,  dateactiondone, action_seed_duration FROM "+ACTIONSEEDS_TABLE_NAME);
 			
 			db.execSQL("DROP TABLE "+ACTIONSEEDS_TABLE_NAME);
 			db.execSQL("CREATE TABLE "+ ACTIONSEEDS_TABLE_NAME
 					+ " ("+ACTIONSEED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
 					+ACTIONSEED_GROWINGSEED_ID + " INTEGER," 
 					+ ACTIONSEED_ACTION_ID + " INTEGER," 
 					+ ACTIONSEED_DATEACTIONDONE	+ " INTEGER,"
 					+ ACTIONSEED_DURATION+ " INTEGER"
 					+ ");");
 			db.execSQL("INSERT INTO "+ACTIONSEEDS_TABLE_NAME+" SELECT _id, growingseed_id, action_id,  dateactiondone, action_seed_duration FROM backup");
 			db.execSQL("DROP TABLE backup");
 
 
 		} 		if (oldVersion < 13) {
 			db.execSQL("ALTER TABLE " + ACTIONSEEDS_TABLE_NAME + " ADD COLUMN " + ACTIONSEED_DATA + " VARCHAR(255)");
 
 		}else {
 			db.execSQL("DROP TABLE IF EXISTS " + SEEDS_TABLE_NAME);
 			db.execSQL("DROP TABLE IF EXISTS " + GROWINGSEEDS_TABLE_NAME);
 			db.execSQL("DROP TABLE IF EXISTS " + ALLOTMENT_TABLE_NAME);
 			db.execSQL("DROP TABLE IF EXISTS " + ACTIONSEEDS_TABLE_NAME);
 			db.execSQL("DROP TABLE IF EXISTS " + WEATHER_TABLE_NAME);
 			db.execSQL("DROP TABLE IF EXISTS " + FAMILY_TABLE_NAME);
 			onCreate(db);
 
 		}
 	}
 }
