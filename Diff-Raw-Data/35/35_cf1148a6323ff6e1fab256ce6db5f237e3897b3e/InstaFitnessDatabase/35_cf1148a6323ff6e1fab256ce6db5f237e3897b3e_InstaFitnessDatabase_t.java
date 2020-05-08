 package com.moe.instafitness.database;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.moe.instafitness.libraries.CSVReader;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.util.Log;
 
 public class InstaFitnessDatabase {
 	private static volatile InstaFitnessDatabase instance = null;
 	
 	private static final String TAG = "InstaFitnessDatabase";
 	
 	private static final String DATABASE_NAME = "instafitness";
 	private static final String WORKOUT_TABLE_NAME = "workout";
 	private static final String MUSCLE_TABLE_NAME = "muscles";
    private static final String JUNCTION_W_M_TABLE_NAME = WORKOUT_TABLE_NAME + "_" + MUSCLE_TABLE_NAME;
 	private static final String PICTURE_TABLE_NAME = "picture";
 	private static final String DIFFICULTY_TABLE_NAME = "difficulty";
 	private static final String PERSONAL_INFO_TABLE_NAME = "personal_info";
 	private static final int DATABASE_VERSION = 1;
 	
 	private final InstaFitnessOpenHelper mDatabaseOpenHelper;
 
 	/**
      * Constructor
      * @param context The Context within which to work, used to create the DB
      */
     public InstaFitnessDatabase(Context context) {
     	mDatabaseOpenHelper = new InstaFitnessOpenHelper(context);
     }
     
     public static InstaFitnessDatabase getInstance(Context context) {
     	if (instance == null) {
         	synchronized (InstaFitnessDatabase.class){
         		if (instance == null) {
         			instance = new InstaFitnessDatabase(context);
                 }
         	}
         }
         return instance;
     }
 
     /**
      * Insert les informations personnelles en bdd
      * @param personalInfo
      * @return
      */
     public long insertPersonalInfo(Map<String, String> personalInfo) {
         mDatabaseOpenHelper.getWritableDatabase();
         return mDatabaseOpenHelper.insertPersonalInfo(personalInfo);
     }
 
     /**
     * SQLiteQuery: SELECT * FROM workout
     * LEFT OUTER JOIN workout_muscles ON workout.id=workout_muscles.id_workout
     * JOIN muscles ON workout_muscles.id_muscle=muscles.id
     *
      * @return la Cursor contenant tous les workout
      */
    public Cursor selectAllWorkouts() {
        String table = WORKOUT_TABLE_NAME
                +" LEFT OUTER JOIN "+JUNCTION_W_M_TABLE_NAME
                +" ON "+WORKOUT_TABLE_NAME+".id="+JUNCTION_W_M_TABLE_NAME+".id_workout"
                +" JOIN "+MUSCLE_TABLE_NAME
                +" ON "+JUNCTION_W_M_TABLE_NAME+".id_muscle="+MUSCLE_TABLE_NAME+".id";
         String selection = null;
         String[] selectionArgs = null;
         String[] columns = {"*"};
         String having = null;
         String setOrder = null;
 
         return this.query(table, selection, selectionArgs, columns, having, setOrder);
     }
 
     /**
      * Performs a database query.
      * @param table
      * @param selection The selection clause
      * @param selectionArgs Selection arguments for "?" components in the selection
      * @param columns The columns to return
      * @param having
      * @param setOrder
      * @return A Cursor over all rows matching the query
      */
     private Cursor query(String table, String selection, String[] selectionArgs, String[] columns, String having, String setOrder) {
         /* The SQLiteBuilder provides a map for all possible columns requested to
          * actual columns in the database, creating a simple column alias mechanism
          * by which the ContentProvider does not need to know the real column names
          */
         SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
         builder.setTables(table);
 
         Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(),
                 columns, selection, selectionArgs, having, setOrder, null);
 
         if (cursor == null) {
             return null;
         } else if (!cursor.moveToFirst()) {
             cursor.close();
             return null;
         }
         return cursor;
     }
     
     /**
      * This creates/opens the database.
      */
     private static class InstaFitnessOpenHelper extends SQLiteOpenHelper {
     	
     	private final Context mHelperContext;
         private SQLiteDatabase mDatabase;
         
         private static final String[] DATABASE_TABLES_CREATE = {
 	        "CREATE TABLE " + WORKOUT_TABLE_NAME + " (" +
 	        "id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL UNIQUE , " +
 	        "name TEXT, " +
 	        "description TEXT, " +
 	        "icon TEXT, " +
 	        "difficulty INTEGER, " +
 	        "duration INTEGER, " +
 	        "sets INTEGER, " +
 	        "repetition INTEGER, " +
 	        "type TEXT, " +
 	        "material_needed INTEGER);",
 	        
 	        "CREATE TABLE " + MUSCLE_TABLE_NAME + " (" +
 			"id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE , " +
 			"muscle TEXT);",
 			
			"CREATE TABLE " + JUNCTION_W_M_TABLE_NAME + " (" +
 			"id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE , " +
 			"id_workout INTEGER NOT NULL, " +
 			"id_muscle INTEGER NOT NULL);",
 			
 			"CREATE TABLE " + PICTURE_TABLE_NAME + " (" +
 	        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE , " +
 	        "id_workout INTEGER NOT NULL, " +
 	        "path TEXT);", 
 	        
 	        "CREATE TABLE " + DIFFICULTY_TABLE_NAME + " (" +
 	        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE , " +
 	        "id_workout INTEGER NOT NULL, " +
 	        "note INTEGER, " +
 	        "timestamp DATETIME);", 
 	        
 	        "CREATE TABLE " + PERSONAL_INFO_TABLE_NAME + " (" +
 	        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE , " +
 	        "name TEXT, " +
 	        "surname TEXT, " +
 	        "age INTEGER, " +
 	        "objectif_date DATETIME, " +
 	        "weight FLOAT, " +
 	        "how_many_time_week INTEGER);"
 		};
     	
     	InstaFitnessOpenHelper(Context context) {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
             mHelperContext = context;
         }
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			mDatabase = db;
 			for(String i : DATABASE_TABLES_CREATE) {
 				 mDatabase.execSQL(i);
 			}
             loadCSV();
 		}
 		
 		private void loadCSV() {
 			String index[] = {}, next[];
 	        //List<String[]> list = new ArrayList<String[]>();
 
 	        try {
 	            CSVReader reader = new CSVReader(new InputStreamReader(mHelperContext.getAssets().open("test2.csv")));
 	            int i = 0;
 	            while(true) {
 	            	if(i == 0) {
 	            		index = reader.readNext();
 	            	}
 	                next = reader.readNext();
 	                if(next != null) {
 	                	Map<String, String> workout = new HashMap<String, String>();
                         String[] muscles = new String[]{};
 	                	for (int j = 0 ; j < index.length ; j++) {
 	                		String[] column = index[j].split(":");
 	                		if (column[0].equals("workout")) {
 	                			workout.put(column[1], next[j]);
 	                		} else if (column[0].equals("muscle")) {
                                 muscles = next[j].split(",");
 	                		}
 	                	}
 	                	
 	                	// si le workout possede un nom et une description ==> insert
 	                	if (!workout.get("name").equals("") && !workout.get("description").equals("")) {
 	                		long workoutId = insertWorkout(workout);
                             // si le workout a bien été ajouté dans la bdd
                             // et que qu'on a les types de muscles ==> insert
                             if (workoutId > 0 && muscles.length > 0) {
                                 for (String muscle : muscles) {
                                     long muscleId = insertMuscle(muscle);
                                     // si l'insert c'est bien passé on met a jour la table de liaison
                                     if(muscleId > 0) {
                                         insertJoinWorkoutMuscle(workoutId, muscleId);
                                     }
                                 }
                             }
 	                	}
 	                    //list.add(next);
 	                } else {
 	                    break;
 	                }
 	                i++;
 	            }
 	        } catch (IOException e) {
 	            e.printStackTrace();
 	        }
         }
 
         /**
          * Insert d'un workout
          * @param workout
          * @return workoutId
          */
 		public long insertWorkout(Map<String, String> workout) {
             ContentValues workoutValues = new ContentValues();
             
             for (Map.Entry<String, String> entry : workout.entrySet()) {
                 String key = entry.getKey();
                 String value = entry.getValue();
                 workoutValues.put(key, value);
             }
 
             return mDatabase.insert(WORKOUT_TABLE_NAME, null, workoutValues);
         }
 
         /**
          * Insert des informations personnelles
          * @param personalInfo
          * @return personalInfoId
          */
         public long insertPersonalInfo(Map<String, String> personalInfo) {
             ContentValues personalInfoValues = new ContentValues();
 
             for (Map.Entry<String, String> entry : personalInfo.entrySet()) {
                 String key = entry.getKey();
                 String value = entry.getValue();
                 personalInfoValues.put(key, value);
             }
 
             return mDatabase.insert(PERSONAL_INFO_TABLE_NAME, null, personalInfoValues);
         }
 
         /**
          * Insert du muscle
          * @param muscle
          * @return muscleId
          */
         public long insertMuscle(String muscle) {
             ContentValues muscleValues = new ContentValues();
             muscleValues.put("muscle", muscle);
 
             return mDatabase.insert(MUSCLE_TABLE_NAME, null, muscleValues);
         }
 
         /**
          * Insert dans la table faisant la jointure entre le workout et ses muscles
          * @param workoutId
          * @param muscleId
          * @return
          */
         public long insertJoinWorkoutMuscle(long workoutId, long muscleId) {
             ContentValues values = new ContentValues();
             values.put("id_workout", (int) workoutId);
             values.put("id_muscle", (int) muscleId);
 
            return mDatabase.insert(JUNCTION_W_M_TABLE_NAME, null, values);
         }
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                     + newVersion + ", which will destroy all old data");
 			for(String i : DATABASE_TABLES_CREATE) {
 				db.execSQL("DROP TABLE IF EXISTS " + i);
 			}
             onCreate(db);
 		}
     }
 
 }
