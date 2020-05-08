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
 package org.gots.garden.sql;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class GardenSQLite extends SQLiteOpenHelper {
 	// ************************ DATABASE **************
 	private static final int DATABASE_VERSION = 11;
 	private static String DATABASE_NAME = "garden";
 	public final static String AUTHORITY = "org.gots.providers.garden";
 
 	private static final String TAG = "GardenDatabase";
 
 	// ************************ GARDEN TABLE **************
 	public static final String GARDEN_TABLE_NAME = "garden";
 
 	public static final String GARDEN_ID = "_id";
 	public static final String GARDEN_LATITUDE = "latitude";
 	public static final String GARDEN_LONGITUDE = "longitude";
 	public static final String GARDEN_ALTITUDE = "altitude";
 	public static final String GARDEN_LOCALITY = "locality";
 	public static final String GARDEN_ADMINAREA = "adminarea";
 	public static final String GARDEN_COUNTRYNAME = "countryname";
 
 	public static final String GARDEN_LAST_SYNCHRO = "last_synchro";
 
 	//@formatter:off
 		public static final String CREATE_TABLE_GARDEN = "CREATE TABLE " + GARDEN_TABLE_NAME 
 				+ " (" + GARDEN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
 				+ GARDEN_LOCALITY + " STRING,"
 				+ GARDEN_ADMINAREA + " STRING,"
 				+ GARDEN_COUNTRYNAME + " STRING,"
 				+ GARDEN_ALTITUDE+ " INTEGER,"		
 				+ GARDEN_LATITUDE + " INTEGER,"		
 				+ GARDEN_LONGITUDE + " INTEGER,"
 				+ GARDEN_LAST_SYNCHRO + " INTEGER"
 				+ ");";
 	//@formatter:on
 
 	// ************************ ACTION TABLE **************
 	public static final String ACTION_TABLE_NAME = "action";
 	// public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
 	// + "/action");
 	// public static final String CONTENT_TYPE =
 	// "vnd.android.cursor.dir/vnd.gots.action";
 
 	// public static final String GROWINGSEED_ID = "growingseed_id";
 	public static final String ACTION_ID = "_id";
 	public static final String ACTION_NAME = "name";
 	public static final String ACTION_DESCRIPTION = "description";
 	public static final String ACTION_DURATION = "duration";
 
 	//@formatter:off
 		public static final String CREATE_TABLE_ACTION = "CREATE TABLE " + ACTION_TABLE_NAME 
 				+ " (" + ACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
 				+ ACTION_NAME + " STRING,"
 				+ ACTION_DESCRIPTION + " STRING,"
 				+ ACTION_DURATION + " INTEGER"			
 				+ ");";
 	//@formatter:on
 
 	public GardenSQLite(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL(CREATE_TABLE_GARDEN);
 		db.execSQL(CREATE_TABLE_ACTION);
 
		db.execSQL(CREATE_TABLE_ACTION);
 		populateActions(db);
		
		Log.i(TAG, "onCreate");
 
 	}
 
 	
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
 				+ ", which will destroy all old data");
 		if (oldVersion<=10) {
 			db.execSQL(CREATE_TABLE_ACTION);
 			populateActions(db);
 
 		}
 	}
 
 	private void populateActions(SQLiteDatabase db) {
 		db.execSQL("Insert into " + ACTION_TABLE_NAME + "(" + ACTION_NAME + ") VALUES ('sow')");
 		db.execSQL("Insert into " + ACTION_TABLE_NAME + "(" + ACTION_NAME + ") VALUES ('water')");
 		db.execSQL("Insert into " + ACTION_TABLE_NAME + "(" + ACTION_NAME + ") VALUES ('hoe')");
 		db.execSQL("Insert into " + ACTION_TABLE_NAME + "(" + ACTION_NAME + ") VALUES ('beak')");
 		db.execSQL("Insert into " + ACTION_TABLE_NAME + "(" + ACTION_NAME + ") VALUES ('cut')");
 		db.execSQL("Insert into " + ACTION_TABLE_NAME + "(" + ACTION_NAME + ") VALUES ('lighten')");
 		db.execSQL("Insert into " + ACTION_TABLE_NAME + "(" + ACTION_NAME + ") VALUES ('harvest')");
 	}
 }
