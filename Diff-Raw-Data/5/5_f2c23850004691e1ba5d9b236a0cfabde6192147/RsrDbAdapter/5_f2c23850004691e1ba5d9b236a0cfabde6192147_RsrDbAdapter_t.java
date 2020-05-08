 /*
  *  Copyright (C) 2012-2013 Stichting Akvo (Akvo Foundation)
  *
  *  This file is part of Akvo RSR.
  *
  *  Akvo RSR is free software: you can redistribute it and modify it under the terms of
  *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
  *  either version 3 of the License or any later version.
  *
  *  Akvo RSR is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *  See the GNU Affero General Public License included below for more details.
  *
  *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
  */
 
 package org.akvo.rsr.android.dao;
 
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.akvo.rsr.android.domain.Country;
 import org.akvo.rsr.android.domain.Project;
 import org.akvo.rsr.android.domain.Update;
 import org.akvo.rsr.android.domain.User;
 
 import android.annotation.SuppressLint;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 /**
  * Database class for the RSR db. It can create/upgrade the database as well
  * as select/insert/update.
  * 
  * @author Stellan Lagerstroem
  * 
  */
 public class RsrDbAdapter {
 	public static final String PK_ID_COL = "_id";
 	public static final String TITLE_COL = "title";
 	public static final String SUBTITLE_COL = "subtitle";
 	public static final String SUMMARY_COL = "summary";
 	public static final String FUNDS_COL = "funds";
 	public static final String THUMBNAIL_URL_COL = "thumbnail_url";
 	public static final String THUMBNAIL_FILENAME_COL = "thumbnail_fn";
 	public static final String PROJECT_COL = "project";
 	public static final String USER_COL = "userid";
 	public static final String TEXT_COL = "_text";
 	public static final String DRAFT_COL = "draft";
 	public static final String UNSENT_COL = "unsent"; //currently unused
 	public static final String HIDDEN_COL = "hidden";
 	public static final String CREATED_COL = "creation_date";
 	public static final String UUID_COL = "uuid";
 
 	public static final String LAT_COL = "latitude";
 	public static final String LON_COL = "longitude";
 	public static final String COUNTRY_COL = "country_id";
 	public static final String STATE_COL = "state";
 	public static final String CITY_COL = "city";
 
 	public static final String NAME_COL = "name";
 	public static final String CONTINENT_COL = "continent";
 	public static final String ISO_CODE_COL = "iso_code";
 	
 	public static final String USERNAME_COL = "username";
 	public static final String FIRST_NAME_COL = "first_name";
 	public static final String LAST_NAME_COL = "last_name";
 	public static final String EMAIL_COL = "email";
 	public static final String ORGANISATION_COL = "organisation";
 
 	private static final String TAG = "RsrDbAdapter";
 	private DatabaseHelper databaseHelper;
 	private SQLiteDatabase database;
 	
 	private static final String PROJECT_JOIN = "project LEFT OUTER JOIN country ON (project.country_id = country._id)";
 
 	/**
 	 * Database creation sql statements
 	 */
 	private static final String PROJECT_TABLE_CREATE =
 			"create table project (_id integer primary key, "+
 			"title text not null, subtitle text, summary text, funds real, "+
 			"thumbnail_url text, thumbnail_fn text," +
 			"longitude text, latitude text, country_id integer, state text, city text, hidden integer);";
 	private static final String UPDATE_TABLE_CREATE =
 			"create table _update (_id integer primary key, project integer not null, userid integer not null, "+
 			"title text not null, _text text, location text, uuid text,"+
 			"thumbnail_url text, thumbnail_fn text," +
 			"draft integer, unsent integer," +
 			CREATED_COL + " INTEGER NOT NULL DEFAULT (strftime('%s','now'))" +
 			");";
 	private static final String COUNTRY_TABLE_CREATE =
 			"create table country (_id integer primary key, "+
 			"name text not null, continent text, "+
 			"iso_code text);";
 	private static final String USER_TABLE_CREATE =
 			"create table user (_id integer primary key, "+
 			"username text, organisation integer, "+
 			"first_name text, last_name text, email text);";
 
 	private static final String[] DEFAULT_PROJECT_INSERTS = new String[] {
 //		"insert into project values(1,'Sample Proj1', 'Sample proj 1 subtitle', 'sum1', 4711.00, 'url1', 'fn1')",
 //		"insert into project values(2,'Sample Proj2', 'Sample proj 2 subtitle', 'sum2', 4712.00, 'url2', 'fn2')"
 		};
 
 	private static final String DATABASE_NAME = "rsrdata";
 	private static final String PROJECT_TABLE = "project";
 	private static final String UPDATE_TABLE  = "_update";
 	private static final String COUNTRY_TABLE = "country";
 	private static final String USER_TABLE    = "user";
 
 //	private static final int DATABASE_VERSION = 5;
 //	private static final int DATABASE_VERSION = 6; //added project columns:long, lat, country, state, city
 //	private static final int DATABASE_VERSION = 7; //added project.hidden
 //	private static final int DATABASE_VERSION = 8; //added country table
 //	private static final int DATABASE_VERSION = 9; //added update.creation_date
 //	private static final int DATABASE_VERSION = 10; //added update.user and user table
 //	private static final int DATABASE_VERSION = 11; //user columns attribute change
 	private static final int DATABASE_VERSION = 12; //uuid for updates
 
 	private final Context context;
 
 	/**
 	 * Helper class for creating the database tables and loading reference data
 	 * 
 	 * It is declared with package scope for VM optimizations
 	 * 
 	 * @author Stellan Lagerstroem
 	 * 
 	 */
 	static class DatabaseHelper extends SQLiteOpenHelper {
 
 		private static SQLiteDatabase database;
 		@SuppressLint("UseValueOf")
 		private static volatile Long LOCK_OBJ = new Long(1);
 		private volatile static int instanceCount = 0;
 
 		DatabaseHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL(PROJECT_TABLE_CREATE);
 			db.execSQL(UPDATE_TABLE_CREATE);
 			db.execSQL(COUNTRY_TABLE_CREATE);
 			db.execSQL(USER_TABLE_CREATE);
 			for (int i = 0; i < DEFAULT_PROJECT_INSERTS.length; i++) {
 				db.execSQL(DEFAULT_PROJECT_INSERTS[i]);
 			}			
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
 					+ newVersion);
 
 			
 			if (oldVersion < DATABASE_VERSION) { //start over fresh, consider everything to be a cache
 				db.execSQL("DROP TABLE IF EXISTS " + PROJECT_TABLE);
 				db.execSQL("DROP TABLE IF EXISTS " + UPDATE_TABLE);
 				db.execSQL("DROP TABLE IF EXISTS " + COUNTRY_TABLE);
 				db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
 				onCreate(db);
 			}			
 			
 			/*
 			if (oldVersion < 57) {
 				db.execSQL("DROP TABLE IF EXISTS " + RESPONSE_TABLE);
 				db.execSQL("DROP TABLE IF EXISTS " + RESPONDENT_TABLE);
 				db.execSQL("DROP TABLE IF EXISTS " + SURVEY_TABLE);
 				db.execSQL("DROP TABLE IF EXISTS " + PLOT_POINT_TABLE);
 				db.execSQL("DROP TABLE IF EXISTS " + PLOT_TABLE);
 				db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
 				db.execSQL("DROP TABLE IF EXISTS " + PREFERENCES_TABLE);
 				db.execSQL("DROP TABLE IF EXISTS " + POINT_OF_INTEREST_TABLE);
 				db.execSQL("DROP TABLE IF EXISTS " + TRANSMISSION_HISTORY_TABLE);
 				onCreate(db);
 			} else if (oldVersion < 75) {
 
 				// changes made in version 57
 				runSQL(TRANSMISSION_HISTORY_TABLE_CREATE, db);
 
 				// changes made in version 58
 				try {
 					String value = null;
 					Cursor cursor = db.query(PREFERENCES_TABLE, new String[] {
 							KEY_COL, VALUE_COL }, KEY_COL + " = ?",
 							new String[] { "survey.textsize" }, null, null,
 							null);
 					if (cursor != null) {
 						if (cursor.getCount() > 0) {
 							cursor.moveToFirst();
 							value = cursor.getString(cursor
 									.getColumnIndexOrThrow(VALUE_COL));
 						}
 						cursor.close();
 					}
 					if (value == null) {
 						runSQL("insert into preferences values('survey.textsize','LARGE')",
 								db);
 					}
 				} catch (Exception e) {
 					// swallow
 				}
 
 				// changes in version 63
 				runSQL("alter table survey_respondent add column exported_flag text",
 						db);
 
 				// changes in version 68
 				try {
 					runSQL("alter table survey_respondent add column uuid text",
 							db);
 					// also generate a uuid for all in-flight responses
 					Cursor cursor = db.query(RESPONDENT_JOIN, new String[] {
 							RESPONDENT_TABLE + "." + PK_ID_COL, DISP_NAME_COL,
 							SAVED_DATE_COL, SURVEY_FK_COL, USER_FK_COL,
 							SUBMITTED_DATE_COL, DELIVERED_DATE_COL, UUID_COL },
 							null, null, null, null, null);
 					if (cursor != null) {
 						cursor.moveToFirst();
 						do {
 							String uuid = cursor.getString(cursor
 									.getColumnIndex(UUID_COL));
 							if (uuid == null || uuid.trim().length() == 0) {
 								db.execSQL("update " + RESPONDENT_TABLE
 										+ " set " + UUID_COL + "= '"
 										+ UUID.randomUUID().toString() + "'");
 							}
 						} while (cursor.moveToNext());
 						cursor.close();
 					}
 				} catch (Exception e) {
 					// swallow
 				}
 				// changes made in version 69
 				runSQL("alter table user add column deleted_flag text", db);
 				runSQL("update user set deleted_flag = 'N' where deleted_flag <> 'Y'",
 						db);
 
 				runSQL("update survey set language = 'en' where language = 'english' or language is null",
 						db);
 				if (oldVersion < 74) {
 					runSQL("insert into preferences values('survey.checkforupdates','0')",
 							db);
 					runSQL("insert into preferences values('remoteexception.upload','0')",
 							db);
 				}
 			}
 */
 
 		}
 
 		
 		@Override
 		public SQLiteDatabase getWritableDatabase() {
 			synchronized (LOCK_OBJ) {
 
 				if (database == null || !database.isOpen()) {
 					database = super.getWritableDatabase();
 					instanceCount = 0;
 				}
 				instanceCount++;
 				return database;
 			}
 		}
 
 		
 		@Override
 		public void close() {
 			synchronized (LOCK_OBJ) {
 				instanceCount--;
 				if (instanceCount <= 0) {
 					// close the database held by the helper (if any)
 					super.close();
 					if (database != null && database.isOpen()) {
 						// we may be holding a different database than the
 						// helper so
 						// close that too if it's still open.
 						database.close();
 					}
 					database = null;
 				}
 			}
 		}
 	}
 
 	
 	/**
 	 * Constructor - takes the context to allow the database to be
 	 * opened/created
 	 * 
 	 * @param ctx
 	 *            the Context within which to work
 	 */
 	public RsrDbAdapter(Context ctx) {
 		this.context = ctx;
 	}
 
 	/**
 	 * Open or create the db
 	 * 
 	 * @throws SQLException
 	 *             if the database could be neither opened or created
 	 */
 	public RsrDbAdapter open() throws SQLException {
 		databaseHelper = new DatabaseHelper(context);
 		database = databaseHelper.getWritableDatabase();
 		return this;
 	}
 
 	/**
 	 * close the db
 	 */
 	public void close() {
 		databaseHelper.close();
 	}
 
 	
 	/**
 	 * Create a new project using the title and body provided. If the project is
 	 * successfully created return the new id, otherwise return a -1 to indicate
 	 * failure.
 	 * 
 	 * @param title
 	 *            project title
 	 * 
 	 * @return rowId or -1 if failed
 	 */
 	public long createProject(String title) {
 		ContentValues initialValues = new ContentValues();
 		initialValues.put(TITLE_COL, title);
 		initialValues.put(FUNDS_COL, "0");
 		return database.insert(PROJECT_TABLE, null, initialValues);
 	}
 
 	/**
 	* creates or updates a project in the db
 	*
 	* @param project
 	* @return
 	*/
 	public void saveProject(Project project) {
 		ContentValues updatedValues = new ContentValues();
 		updatedValues.put(PK_ID_COL, project.getId());
 		// updatedValues.put(SERVER_ID_COL, project.getServerId());
 		updatedValues.put(TITLE_COL, project.getTitle());
 		updatedValues.put(SUBTITLE_COL, project.getSubtitle());
 		updatedValues.put(SUMMARY_COL, project.getSummary());
 		updatedValues.put(FUNDS_COL, project.getFunds());
 		updatedValues.put(THUMBNAIL_URL_COL, project.getThumbnailUrl());
 		//not done here to preserve a cache connection
 //		updatedValues.put(THUMBNAIL_FILENAME_COL, project.getThumbnailFilename());
 		updatedValues.put(COUNTRY_COL, project.getCountry());
 		updatedValues.put(STATE_COL, project.getState());
 		updatedValues.put(CITY_COL, project.getCity());
 		updatedValues.put(LAT_COL, project.getLatitude());
 		updatedValues.put(LON_COL, project.getLongitude());
 		updatedValues.put(HIDDEN_COL, project.getHidden()?"1":"0");
 		
 		Cursor cursor = database.query(PROJECT_TABLE,
 		new String[] { PK_ID_COL },
 		PK_ID_COL + " = ?",
 		new String[] { project.getId(), },
 		null, null, null);
 		
 		if (cursor != null && cursor.getCount() > 0) {
 			// if we found an item, it's an update, otherwise, it's an insert
 			database.update(PROJECT_TABLE, updatedValues, PK_ID_COL + " = ?",
 					new String[] { project.getId() });
 		} else {
 			database.insert(PROJECT_TABLE, null, updatedValues);
 		}
 		
 		if (cursor != null) {
 			cursor.close();
 		}
 	}
 
 	
 	/*
 	 *  Update the local filename of a cached image
 	 */
 	public void updateProjectThumbnailFile(String id, String filename) {
 		ContentValues updatedValues = new ContentValues();
 		updatedValues.put(THUMBNAIL_FILENAME_COL, filename);
 		database.update(PROJECT_TABLE, updatedValues, PK_ID_COL + " = ?", new String[] { id });
 	}
 
 	
 	/*
 	 *  Clear the local filenames of all projects
 	 */
 	public void clearProjectThumbnailFiles() {
 		ContentValues updatedValues = new ContentValues();
 		updatedValues.putNull(THUMBNAIL_FILENAME_COL);
 		database.update(PROJECT_TABLE, updatedValues, null, null);
 	}
 
 
 	/*
 	 *  Update the local filename of a cached image
 	 */
 	public void setVisibleProjects(Set<String> ids) {
 		//Hide all
 		ContentValues updatedValues = new ContentValues();
 		updatedValues.put(HIDDEN_COL, 1);		
 		database.update(PROJECT_TABLE, updatedValues, null, null);
 		//Show selected
 		updatedValues = new ContentValues();
 		updatedValues.put(HIDDEN_COL, 0);
 	    Iterator<String> itr = ids.iterator();
 	    if (itr.hasNext()) {
 	    	String whereList = "";
 	    	whereList += PK_ID_COL + "=" + itr.next();
 	    	while(itr.hasNext()) {
 	    		whereList += " OR " + PK_ID_COL + "=" + itr.next();
 	    	}
 	    	database.update(PROJECT_TABLE, updatedValues, whereList, null);
 	    }
 	}
 
 	
 
 	/**
 	 * saves or updates an Update in the db
 	 * 
 	 * @param survey
 	 * @return
 	 */
 	public void saveUpdate(Update update, boolean saveFn) {
 		ContentValues updatedValues = new ContentValues();
 		updatedValues.put(PK_ID_COL, update.getId());
 		updatedValues.put(PROJECT_COL, update.getProjectId());
 		updatedValues.put(TITLE_COL, update.getTitle());
 		updatedValues.put(TEXT_COL, update.getText());
 		updatedValues.put(USER_COL, update.getUserId());
 		updatedValues.put(UUID_COL, update.getUuid());
 		updatedValues.put(THUMBNAIL_URL_COL, update.getThumbnailUrl());
 		//not always done here to preserve a cache connection
 		if (saveFn)
 			updatedValues.put(THUMBNAIL_FILENAME_COL, update.getThumbnailFilename());
 		updatedValues.put(DRAFT_COL, update.getDraft()?"1":"0");
 		updatedValues.put(UNSENT_COL, update.getUnsent()?"1":"0");
 		updatedValues.put(CREATED_COL, update.getDate().getTime()/1000); //1-second precision only
 
 		Cursor cursor = database.query(UPDATE_TABLE,
 				new String[] { PK_ID_COL },
 				PK_ID_COL + " = ?",
 				new String[] { update.getId(), },
 				null, null, null);
 
 		if (cursor != null && cursor.getCount() > 0) {
 			// if we found an item, it's an update, otherwise, it's an insert
 			database.update(UPDATE_TABLE, updatedValues, PK_ID_COL + " = ?",
 					new String[] { update.getId() });
 		} else {
 			database.insert(UPDATE_TABLE, null, updatedValues);
 		}
 
 		if (cursor != null) {
 			cursor.close();
 		}
 	}
 
 	/**
 	 * updates an update in the db, after the real ID is returned from the server
 	 * 
 	 * @param update
 	 * @param old_id
 	 * @return
 	 */
 	public void updateUpdateIdSent(Update update, String old_id) {
 		ContentValues updatedValues = new ContentValues();
 		updatedValues.put(PK_ID_COL, update.getId());
 		updatedValues.put(UNSENT_COL, update.getUnsent()?"1":"0");
		updatedValues.put(DRAFT_COL, update.getDraft()?"1":"0");
 
 		Cursor cursor = database.query(UPDATE_TABLE,
 				new String[] { PK_ID_COL },
 				PK_ID_COL + " = ?",
 				new String[] { old_id, },
 				null, null, null);
 
 		if (cursor != null && cursor.getCount() > 0) {
 			// if we found an item, it's an update, otherwise, it's an error
 			database.update(UPDATE_TABLE, updatedValues, PK_ID_COL + " = ?",
 					new String[] { old_id });
 		} else {
 			Log.e(TAG, "Tried to update id/sent sts of nonexistent update " + old_id);
 		}
 
 		if (cursor != null) {
 			cursor.close();
 		}
 	}
 
 	
 	/**
 	 * updates an update in the db, after the uuid verified by the server
 	 * 
 	 * @param update
 	 * @return
 	 */
 	public boolean updateUpdateVerifiedByUuid(Update update) {
 		ContentValues updatedValues = new ContentValues();
 		updatedValues.put(PK_ID_COL, update.getId());
 		updatedValues.put(UNSENT_COL, update.getUnsent()?"1":"0");
		updatedValues.put(DRAFT_COL, update.getDraft()?"1":"0");
 
 		// if we changed exactly one item, we are done
 		int rowsAffected = database.update(UPDATE_TABLE, updatedValues,
 					UUID_COL + " = ?",
 					new String[] { update.getUuid() }
 					);
 		if (rowsAffected == 1) {
 			return true;
 		} else {
 			Log.e(TAG, "Tried to update id/sent/draft sts of nonexistent update " + update.getUuid());
 			return false;
 		}
 
 	}
 	
 	/*
 	 *  Update the local filename of a cached image
 	 */
 	public void updateUpdateThumbnailFile(String id, String filename) {
 		ContentValues updatedValues = new ContentValues();
 		updatedValues.put(THUMBNAIL_FILENAME_COL, filename);
 		database.update(UPDATE_TABLE, updatedValues, PK_ID_COL + " = ?", new String[] { id });
 	}
 
 
 	/*
 	 *  Clear the local filenames of all updates
 	 */
 	public void clearUpdateThumbnailFiles() {
 		ContentValues updatedValues = new ContentValues();
 		updatedValues.putNull(THUMBNAIL_FILENAME_COL);
 		database.update(UPDATE_TABLE, updatedValues, null, null);
 	}
 
 
 	/*
 	 *  Delete an Update
 	 */
 	public void deleteUpdate(String id) {
 		database.delete(UPDATE_TABLE, PK_ID_COL + " = ?", new String[] { id });
 	}
 
 
 	/**
 	 * Gets all projects, all columns
 	 */
 	public Cursor listAllProjects() {
 		Cursor cursor = database.query(PROJECT_TABLE,
 										null,
 										null,
 										null,
 										null,
 										null,
 										null);
 		return cursor;
 	}
 
 	/**
 	 * Gets updates for a specific project, all columns
 	 */
 	public Cursor listAllVisibleProjects() {
 		Cursor cursor = database.query(PROJECT_TABLE,
 										null,
 										HIDDEN_COL + " = ?",
 										new String[] { "0" },
 										null,
 										null,
 										null);
 
 		return cursor;
 	}
 
 
 	/**
 	 * Gets all projects, all columns and country data
 	 */
 	public Cursor listAllProjectsWithCountry() {
 		Cursor cursor = database.query(PROJECT_JOIN,
 										null,
 										null,
 										null,
 										null,
 										null,
 										null);
 		return cursor;
 	}
 
 
 	/**
 	 * Gets all updates, all columns
 	 */
 	public Cursor listAllUpdates() {
 		Cursor cursor = database.query(UPDATE_TABLE,
 										null,
 										null,
 										null,
 										null,
 										null,
 										null);
 		return cursor;
 	}
 
 
 	/**
 	 * Gets updates for a specific project, all columns
 	 */
 	public Cursor listAllUpdatesFor(String _id) {
 		Cursor cursor = database.query(UPDATE_TABLE,
 										null,
 										PROJECT_COL + " = ?",
 										new String[] { _id },
 										null,
 										null,
 										null);
 
 		return cursor;
 	}
 
 	/**
 	 * Gets updates for a specific project, all columns
 	 */
 	public Cursor listAllUpdatesNewestFirstFor(String _id) {
 		Cursor cursor = database.query(UPDATE_TABLE,
 										null,
 										PROJECT_COL + " = ?",
 										new String[] { _id },
 										null,
 										null,
 										CREATED_COL + " DESC");
 
 		return cursor;
 	}
 
 	/**
 	 * Gets updates for a specific project, all columns
 	 */
 	public Cursor listAllUpdatesUnsent() {
 		Cursor cursor = database.query(UPDATE_TABLE,
 										null,
 										UNSENT_COL + " <> 0",
 										new String[] { },
 										null,
 										null,
 										null);
 
 		return cursor;
 	}
 
 
 	/**
 	 * Counts state sums for updates for a specific project
 	 */
 	public int[] countAllUpdatesFor(String _id) {
 		int draftCount = 0;
 		int unsentCount = 0;
 		int otherCount = 0;
 		Cursor cursor = listAllUpdatesFor(_id);
 		if (cursor !=null) {
 			if (cursor.getCount() > 0) {
 				int draftCol = cursor.getColumnIndexOrThrow(DRAFT_COL);
 				int unsentCol = cursor.getColumnIndexOrThrow(UNSENT_COL);//Not used
 				cursor.moveToFirst();
 				while (!cursor.isAfterLast()) {
 					if (cursor.getInt(draftCol) > 0) {
 						draftCount++;
 					} else	if (cursor.getInt(unsentCol) > 0) {
 						unsentCount++;
 					} else
 						otherCount++;
 					cursor.moveToNext();
 				}
 			}
 			cursor.close();
 		}	
 		return new int[] { draftCount, unsentCount, otherCount };
 	}
 
 
 	/**
 	 * Gets users that are referenced by updates but not loaded
 	 */
 	public Cursor listMissingUsers() {
 		Cursor cursor = database.query(true, //distinct
 										"_update LEFT JOIN user ON (userid = user._id)",
 										new String[] {"userid", "user._id"},
 //										null,//selection
 										"user._id IS NULL",
 										null,//selection vals
 //										new String[] { },
 										"userid",
 										null,
 										null,
 										null);
 
 		return cursor;
 	}
 
 
 	/**
 	 * Gets a single project from the db using its primary key
 	 */
 	public Project findProject(String _id) {
 		Project project = null;
 		Cursor cursor = database.query(PROJECT_JOIN,
 									   null,
 									   "project._id = ?",
 									   new String[] { _id }, null, null, null);
 		if (cursor != null) {
 			if (cursor.getCount() > 0) {
 				cursor.moveToFirst();
 				project = new Project();
 				project.setId(_id); //no confusion with country id
 				project.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(TITLE_COL)));
 				project.setSubtitle(cursor.getString(cursor.getColumnIndexOrThrow(SUBTITLE_COL)));
 				project.setSummary(cursor.getString(cursor.getColumnIndexOrThrow(SUMMARY_COL)));
 				project.setThumbnailUrl(cursor.getString(cursor.getColumnIndexOrThrow(THUMBNAIL_URL_COL)));
 				project.setThumbnail(cursor.getString(cursor.getColumnIndexOrThrow(THUMBNAIL_FILENAME_COL)));
 				project.setCountry(cursor.getString(cursor.getColumnIndexOrThrow(NAME_COL)));
 				project.setState(cursor.getString(cursor.getColumnIndexOrThrow(STATE_COL)));
 				project.setCity(cursor.getString(cursor.getColumnIndexOrThrow(CITY_COL)));
 				project.setLatitude(cursor.getString(cursor.getColumnIndexOrThrow(LAT_COL)));
 				project.setLongitude(cursor.getString(cursor.getColumnIndexOrThrow(LON_COL)));
 				project.setHidden(0 != cursor.getInt(cursor.getColumnIndexOrThrow(HIDDEN_COL)));
 				}
 			cursor.close();
 			}
 
 		return project;
 	}
 
 
 	/**
 	 * Gets a single update from the db using its primary key
 	 */
 	public Update findUpdate(String _id) {
 		Update update = null;
 		Cursor cursor = database.query(UPDATE_TABLE,
 										null,
 										PK_ID_COL + " = ?",
 										new String[] { _id }, null, null, null);
 		if (cursor != null) {
 			if (cursor.getCount() > 0) {
 				cursor.moveToFirst();
 				update = new Update();
 				update.setId(_id);
 				update.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(TITLE_COL)));
 				update.setProjectId(cursor.getString(cursor.getColumnIndexOrThrow(PROJECT_COL)));
 				update.setText(cursor.getString(cursor.getColumnIndexOrThrow(TEXT_COL)));
 				update.setUuid(cursor.getString(cursor.getColumnIndexOrThrow(UUID_COL)));
 				update.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(USER_COL)));
 				update.setThumbnailUrl(cursor.getString(cursor.getColumnIndexOrThrow(THUMBNAIL_URL_COL)));
 				update.setThumbnailFilename(cursor.getString(cursor.getColumnIndexOrThrow(THUMBNAIL_FILENAME_COL)));
 				update.setDraft(0 != cursor.getInt(cursor.getColumnIndexOrThrow(DRAFT_COL)));
 				update.setUnsent(0 != cursor.getInt(cursor.getColumnIndexOrThrow(UNSENT_COL)));
 				update.setDate(new Date(1000 * cursor.getLong(cursor.getColumnIndexOrThrow(CREATED_COL))));
 				}
 			cursor.close();
 		}
 
 		return update;
 	}
 
 
 	/**
 	 * Lists all non-deleted surveys from the database
 	 */
 /*	public ArrayList<Survey> listSurveys(String language) {
 		ArrayList<Survey> surveys = new ArrayList<Survey>();
 		String whereClause = DELETED_COL + " <> ?";
 		String[] whereParams = null;
 		if (language != null) {
 			whereClause += " and " + LANGUAGE_COL + " = ?";
 			whereParams = new String[] { ConstantUtil.IS_DELETED,
 					language.toLowerCase().trim() };
 		} else {
 			whereParams = new String[] { ConstantUtil.IS_DELETED };
 		}
 		Cursor cursor = database.query(SURVEY_TABLE, new String[] { PK_ID_COL,
 				DISP_NAME_COL, LOCATION_COL, FILENAME_COL, TYPE_COL,
 				LANGUAGE_COL, HELP_DOWNLOADED_COL, VERSION_COL }, whereClause,
 				whereParams, null, null, null);
 		if (cursor != null) {
 			if (cursor.getCount() > 0) {
 				cursor.moveToFirst();
 				do {
 					Survey survey = new Survey();
 					survey.setId(cursor.getString(cursor
 							.getColumnIndexOrThrow(PK_ID_COL)));
 					survey.setName(cursor.getString(cursor
 							.getColumnIndexOrThrow(DISP_NAME_COL)));
 					survey.setLocation(cursor.getString(cursor
 							.getColumnIndexOrThrow(LOCATION_COL)));
 					survey.setFileName(cursor.getString(cursor
 							.getColumnIndexOrThrow(FILENAME_COL)));
 					survey.setType(cursor.getString(cursor
 							.getColumnIndexOrThrow(TYPE_COL)));
 					survey.setHelpDownloaded(cursor.getString(cursor
 							.getColumnIndexOrThrow(HELP_DOWNLOADED_COL)));
 					survey.setLanguage(cursor.getString(cursor
 							.getColumnIndexOrThrow(LANGUAGE_COL)));
 					survey.setVersion(cursor.getDouble(cursor
 							.getColumnIndexOrThrow(VERSION_COL)));
 					surveys.add(survey);
 				} while (cursor.moveToNext());
 			}
 			cursor.close();
 		}
 		return surveys;
 	}
 */
 
 
 	/**
 	 * deletes all the projects from the database
 	 */
 	public void deleteAllProjects() {
 		database.delete(PROJECT_TABLE, null, null);
 		database.delete(UPDATE_TABLE, null, null);
 	}
 
 	
 	/**
 	 * Gets a single user from the db using its primary key
 	 */
 	public User findUser(String _id) {
 		User user = null;
 		Cursor cursor = database.query(USER_TABLE,
 									   null,
 									   "_id = ?",
 									   new String[] { _id }, null, null, null);
 		if (cursor != null) {
 			if (cursor.getCount() > 0) {
 				cursor.moveToFirst();
 				user = new User();
 				user.setId(_id); //no confusion with country id
 				user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(USERNAME_COL)));
 				user.setFirstname(cursor.getString(cursor.getColumnIndexOrThrow(FIRST_NAME_COL)));
 				user.setLastname(cursor.getString(cursor.getColumnIndexOrThrow(LAST_NAME_COL)));
 				user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(EMAIL_COL)));
 				user.setOrgId(cursor.getString(cursor.getColumnIndexOrThrow(ORGANISATION_COL)));
 				}
 			cursor.close();
 			}
 
 		return user;
 	}
 
 
 	/**
 	* creates or updates a user in the db
 	*
 	* @param user
 	* @return
 	*/
 	public void saveUser(User user) {
 		ContentValues updatedValues = new ContentValues();
 		updatedValues.put(PK_ID_COL, user.getId());
 		updatedValues.put(USERNAME_COL, user.getUsername());
 		updatedValues.put(FIRST_NAME_COL, user.getFirstname());
 		updatedValues.put(LAST_NAME_COL, user.getLastname());
 		updatedValues.put(EMAIL_COL, user.getEmail());
 		updatedValues.put(ORGANISATION_COL, user.getOrgId());
 		
 		Cursor cursor = database.query(USER_TABLE,
 										new String[] { PK_ID_COL },
 										PK_ID_COL + " = ?",
 										new String[] { user.getId(), },
 										null, null, null);
 		
 		if (cursor != null && cursor.getCount() > 0) {
 			// if we found an item, it's an update, otherwise, it's an insert
 			database.update(USER_TABLE, updatedValues, PK_ID_COL + " = ?",
 					new String[] { user.getId() });
 		} else {
 			database.insert(USER_TABLE, null, updatedValues);
 		}
 		
 		if (cursor != null) {
 			cursor.close();
 		}
 	}
 
 	
 
 	
 	
 
 
 	/**
 	 * executes a single insert/update/delete DML or any DDL statement without
 	 * any bind arguments.
 	 * 
 	 * @param sql
 	 */
 	public void executeSql(String sql) {
 		database.execSQL(sql);
 	}
 
 	/**
 	 * reinserts the test survey into the database. For debugging purposes only.
 	 * The survey xml must exist in the APK
 	 */
 	public void reinstallTestSurvey() {
 //		executeSql("insert into project values(999991,'Sample Survey', 1.0,'Survey','res','testsurvey','english','N','N')");
 	}
 
 	/**
 	 * permanently deletes all projects, updates
 	 * from the database
 	 */
 	public void clearAllData() {
 		executeSql("delete from project");
 		executeSql("delete from _update");
 //		executeSql("update preferences set value = '' where key = 'user.lastuser.id'");
 	}
 
 	public Cursor listAllCountries() {
 		Cursor cursor = database.query(COUNTRY_TABLE,
 				null,
 				null,
 				null,
 				null,
 				null,
 				null);
 		return cursor;
 	}
 
 	/**
 	 * saves or updates a Country in the db
 	 * 
 	 * @param country
 	 * @return
 	 */
 	public void saveCountry(Country country) {
 		ContentValues updatedValues = new ContentValues();
 		updatedValues.put(PK_ID_COL, country.getId());
 		updatedValues.put(NAME_COL, country.getName());
 		updatedValues.put(CONTINENT_COL, country.getContinent());
 		updatedValues.put(ISO_CODE_COL, country.getIsoCode());
 
 		Cursor cursor = database.query(COUNTRY_TABLE,
 				new String[] { PK_ID_COL },
 				PK_ID_COL + " = ?",
 				new String[] { country.getId(), },
 				null, null, null);
 
 		if (cursor != null && cursor.getCount() > 0) {
 			// if we found an item, it's an update, otherwise, it's an insert
 			database.update(COUNTRY_TABLE, updatedValues, PK_ID_COL + " = ?",
 					new String[] { country.getId() });
 		} else {
 			database.insert(COUNTRY_TABLE, null, updatedValues);
 		}
 
 		if (cursor != null) {
 			cursor.close();
 		}
 	}
 
 
 
 }
