 /**
  *  Copyright 2011 Tony Murray <murraytony@gmail.com>
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package com.splashmobileproductions.scorekeep.provider;
 
 import java.util.Random;
 import java.util.StringTokenizer;
 
 import com.splashmobileproductions.scorekeep.R;
 import com.splashmobileproductions.scorekeep.games.GameDefs;
 
 import android.content.ContentProvider;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.graphics.Color;
 import android.net.Uri;
 import android.provider.BaseColumns;
 import android.text.TextUtils;
 import android.util.Log;
 
 /**
  * @author Tony Murray <murraytony@gmail.com>
  *
  * This service provider provides scores for games that have been recored by ScoreKeep.
  */
 public final class ScoresProvider extends ContentProvider {
 	public static final String AUTHORITY = "com.splashmobileproductions.scoresprovider";
 	protected static final String SCHEME = "content://";
 	public static final Uri CONTENT_URI= Uri.parse(SCHEME+AUTHORITY);
 
 	private static final String DB_NAME = "games.db";
	private static final int DB_VERSION = 4;
 	
 	// Constants for the Uri Matcher
 	private static final int GAME = 1;
 	private static final int GAME_ID = 2;
 	private static final int PLAYER = 3;
 	private static final int PLAYER_ID = 4;
 	private static final int SCORE = 5;
 	private static final int SCORE_ID = 6;
 	
 	private static final UriMatcher sUriMatcher;
 	private static final String DEBUG_TAG = "ScoresProvider";
     static {
         /*
          * Creates and initializes the URI matcher
          */
         // Create a new instance
         sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
 
         // Add Uri patterns
         sUriMatcher.addURI(AUTHORITY, Game.PATH_GAME, GAME);
         sUriMatcher.addURI(AUTHORITY, Game.PATH_GAME_ID+"#", GAME_ID);
         sUriMatcher.addURI(AUTHORITY, Player.PATH_PLAYER, PLAYER);
         sUriMatcher.addURI(AUTHORITY, Player.PATH_PLAYER_ID+"#", PLAYER_ID);
         sUriMatcher.addURI(AUTHORITY, Score.PATH_SCORE, SCORE);
         sUriMatcher.addURI(AUTHORITY, Score.PATH_SCORE_ID+"#", SCORE_ID);
 
     }
     
 	private DatabaseHelper dbh;
 
 	/**
     *
     * Initializes the provider by creating a new DatabaseHelper. onCreate() is called
     * automatically when Android creates the provider in response to a resolver request from a
     * client.
     */
 	@Override
 	public boolean onCreate() {
 		dbh = new DatabaseHelper(getContext());
 		return true;
 	}
 	
 	/* (non-Javadoc)
 	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
 	 */
 	@Override
 	public int delete(Uri uri, String selection, String[] selectionArgs) {
 		String id = null;
 		String table;
 		SQLiteDatabase db = dbh.getWritableDatabase();
 		switch(sUriMatcher.match(uri)) {
 		case GAME_ID:
 			id = uri.getPathSegments().get(Game.ID_PATH_POSITION);
 			db.delete(Score.TABLE_NAME, Score.COLUMN_NAME_GAME_ID+"="+id, null);
 		case GAME:
 			table = Game.TABLE_NAME;
 			if(id==null) db.delete(Score.TABLE_NAME, null, null);
 			break;
 		case PLAYER_ID:
 			id = uri.getPathSegments().get(Player.ID_PATH_POSITION);
 			db.delete(Score.TABLE_NAME, Score.COLUMN_NAME_PLAYER_ID+"="+id, null);
 		case PLAYER:
 			table = Player.TABLE_NAME;
 			if(id==null) db.delete(Score.TABLE_NAME, null, null);
 			break;
 		case SCORE_ID:
 			id = uri.getPathSegments().get(Score.ID_PATH_POSITION);
 		case SCORE:
 			table = Score.TABLE_NAME;
 			break;
 		default:
 			// If the URI doesn't match any of the known patterns, throw an exception.
 			throw new IllegalArgumentException("Unknown URI " + uri);
 		}
 
 		String finalSelection;
 		// if we have an id from the uri, add it to the selection
 		if(id != null) {
 			finalSelection = BaseColumns._ID+" AND "+selection;
 		} else {
 			finalSelection = selection;
 		}
 		// Opens the database object in "write" mode.
 
 
 		int count = db.delete(table, finalSelection, selectionArgs);
 
 		/*Gets a handle to the content resolver object for the current context, and notifies it
 		 * that the incoming URI changed. The object passes this along to the resolver framework,
 		 * and observers that have registered themselves for the provider are notified.
 		 */
 		getContext().getContentResolver().notifyChange(uri, null);
 
 		// Returns the number of rows deleted.
 		return count;
 	}
 
 	/* (non-Javadoc)
 	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
 	 */
 	@Override
 	public Uri insert(Uri uri, ContentValues initialValues) {
         // A map to hold the new record's values.
         ContentValues values;
 
         // If the incoming values map is not null, uses it for the new values.
         if (initialValues != null) {
             values = new ContentValues(initialValues);
         } else {
             // Otherwise, create a new value map
             values = new ContentValues();
         }
 
         // Gets the current system time in milliseconds
         Long now = Long.valueOf(System.currentTimeMillis());
         long insertedRowId = 0;
         
 		switch(sUriMatcher.match(uri)) {
 		case GAME:
 			if(!values.containsKey(Game.COLUMN_NAME_PLAYER_IDS)) {
 				throw new IllegalArgumentException("Key "+Game.COLUMN_NAME_PLAYER_IDS+" required in ContentValues for URI: "+uri);
 			}
 			if(!values.containsKey(Game.COLUMN_NAME_TYPE)) {
 				values.put(Game.COLUMN_NAME_TYPE, GameDefs.DEFAULT);
 			}
 			if(!values.containsKey(Game.COLUMN_NAME_DESCRIPTION)) {
 				values.put(Game.COLUMN_NAME_DESCRIPTION, getContext().getString(R.string.game));
 			}
 			if(!values.containsKey(Game.COLUMN_NAME_CREATE_DATE)) {
 				values.put(Game.COLUMN_NAME_CREATE_DATE, now);	
 			}
 			if(!values.containsKey(Game.COLUMN_NAME_MODIFICATION_DATE)) {
 				values.put(Game.COLUMN_NAME_MODIFICATION_DATE, now);
 			}
 			SQLiteDatabase gdb = dbh.getWritableDatabase();
 			insertedRowId = gdb.insert(Game.TABLE_NAME, null, values);
 			break;
 		case PLAYER:
 			if(!values.containsKey(Player.COLUMN_NAME_NAME)) {
 				throw new IllegalArgumentException("Key "+Player.COLUMN_NAME_NAME+" required in ContentValues for URI: "+uri);
 			}
 			if(!values.containsKey(Player.COLUMN_NAME_COLOR)) {
 				Random rand = new Random();
 				//random color avoid extreme colors
 				values.put(Player.COLUMN_NAME_COLOR, Color.rgb(rand.nextInt(220)+20,rand.nextInt(220)+20,rand.nextInt(220)+20));
 			}
 			SQLiteDatabase pdb = dbh.getWritableDatabase();
 			insertedRowId = pdb.insert(Player.TABLE_NAME, null, values);
 			break;
 		case SCORE:
 			if(!values.containsKey(Score.COLUMN_NAME_GAME_ID)) {
 				throw new IllegalArgumentException("Key "+Score.COLUMN_NAME_GAME_ID+" required in ContentValues for URI: "+uri);
 			}
 			if(!values.containsKey(Score.COLUMN_NAME_PLAYER_ID)) {
 				throw new IllegalArgumentException("Key "+Score.COLUMN_NAME_PLAYER_ID+" required in ContentValues for URI: "+uri);
 			}
 			if( !(values.containsKey(Score.COLUMN_NAME_SCORE) || values.containsKey(Score.COLUMN_NAME_CONTEXT)) ) {
 				throw new IllegalArgumentException("Either key "+Score.COLUMN_NAME_SCORE+
 						" or "+Score.COLUMN_NAME_CONTEXT+" are required in ContentValues for URI: "+uri);
 			}
 			if(!values.containsKey(Score.COLUMN_NAME_CREATE_DATE)) {
 				values.put(Score.COLUMN_NAME_CREATE_DATE, now);	
 			}
 			SQLiteDatabase sdb = dbh.getWritableDatabase();
 			insertedRowId = sdb.insert(Score.TABLE_NAME, null, values);
 			break;
 		default:
 			throw new IllegalArgumentException("Unknown URI " + uri);
 		}
 		
         // If the insert succeeded, the row ID exists.
         if (insertedRowId > 0) {
             // Creates a URI with the id appended
             Uri insertedUri = ContentUris.withAppendedId(uri, insertedRowId);
 
             // Notifies observers registered against this provider that the data changed.
             getContext().getContentResolver().notifyChange(insertedUri, null);
             return insertedUri;
         }
 
         // If the insert didn't succeed, then the rowID is <= 0. Throws an exception.
         throw new SQLException("Failed to insert row into " + uri);
 	}
 
 	/* (non-Javadoc)
 	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
 	 */
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
 			Log.d("ScoresProvider", "Incoming Uri:" + uri.toString() + " Match " + sUriMatcher.match(uri));
 			
 	       // Constructs a new query builder and sets its table name
 	       SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
 	       String id = null;
 	       String orderBy;
 	       switch(sUriMatcher.match(uri)) {
 	       case GAME_ID:
 	    	   id = uri.getPathSegments().get(Game.ID_PATH_POSITION);
 	       case GAME:
 	    	   qb.setTables(Game.TABLE_NAME);
 	    	   orderBy = Game.DEFAULT_SORT_ORDER;
 	    	   break;
 	       case PLAYER_ID:
 	    	   id = uri.getPathSegments().get(Player.ID_PATH_POSITION);
 	       case PLAYER:
 	    	   qb.setTables(Player.TABLE_NAME);
 	    	   orderBy = Player.DEFAULT_SORT_ORDER;
 	    	   break;
 	       case SCORE_ID:
 	    	   id = uri.getPathSegments().get(Score.ID_PATH_POSITION);
 	       case SCORE:
 	    	   qb.setTables(Score.TABLE_NAME);
 	    	   orderBy = Score.DEFAULT_SORT_ORDER;
 	    	   break;
            default:
                // If the URI doesn't match any of the known patterns, throw an exception.
                throw new IllegalArgumentException("Unknown URI " + uri);
 	       }
 	       
 	       //if we have an id from the Uri, append it
 	       if(id != null) {
 	    	   qb.appendWhere(BaseColumns._ID+"="+id);
 	       }
 
 	       // if a sortOrder is specified, use it otherwise, use the default
 	       if(!TextUtils.isEmpty(sortOrder)) {
 	    	   orderBy = sortOrder;
 	       }
 
 	       SQLiteDatabase db = dbh.getReadableDatabase();
 
 	       /*
 	        * Performs the query. If no problems occur trying to read the database, then a Cursor
 	        * object is returned; otherwise, the cursor variable contains null. If no records were
 	        * selected, then the Cursor object is empty, and Cursor.getCount() returns 0.
 	        */
 	       Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
 	       Log.d(DEBUG_TAG, "Query result is: "+c);
 	       
 	       // Tells the Cursor what URI to watch, so it knows when its source data changes
 	       c.setNotificationUri(getContext().getContentResolver(), uri);
 	       return c;
 	}
 
 	/* (non-Javadoc)
 	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
 	 */
 	@Override
 	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
 		String id = null;
 		String table;
 		switch(sUriMatcher.match(uri)) {
 		case GAME_ID:
 			id = uri.getPathSegments().get(Game.ID_PATH_POSITION);
 		case GAME:
 			table = Game.TABLE_NAME;
 			break;
 		case PLAYER_ID:
 			id = uri.getPathSegments().get(Player.ID_PATH_POSITION);
 		case PLAYER:
 			table = Player.TABLE_NAME;
 			break;
 		case SCORE_ID:
 			id = uri.getPathSegments().get(Score.ID_PATH_POSITION);
 		case SCORE:
 			table = Score.TABLE_NAME;
 			break;
 		default:
 			// If the URI doesn't match any of the known patterns, throw an exception.
 			throw new IllegalArgumentException("Unknown URI " + uri);
 		}
 		
 		String finalSelection;
 		// if we have an id from the uri, add it to the selection
 		if(id != null) {
 			finalSelection = BaseColumns._ID+" AND "+selection;
 		} else {
 			finalSelection = selection;
 		}
 		
 		SQLiteDatabase db = dbh.getWritableDatabase();
         int count = db.update(table, values, finalSelection, selectionArgs);
 		
         /*Gets a handle to the content resolver object for the current context, and notifies it
          * that the incoming URI changed. The object passes this along to the resolver framework,
          * and observers that have registered themselves for the provider are notified.
          */
         getContext().getContentResolver().notifyChange(uri, null);
 
         // Returns the number of rows updated.
         return count;
 	}
 	
 	/* (non-Javadoc)
 	 * @see android.content.ContentProvider#getType(android.net.Uri)
 	 */
 	@Override
 	public String getType(Uri uri) {
 	       /**
 	        * Chooses the MIME type based on the incoming URI pattern
 	        */
 	       switch (sUriMatcher.match(uri)) {
 	           case GAME:
 	        	   return Game.CONTENT_TYPE;
 	           case GAME_ID:
 	               return Game.CONTENT_ITEM_TYPE;
 	           case PLAYER:
 	        	   return Player.CONTENT_TYPE;
 	           case PLAYER_ID:
 	               return Player.CONTENT_ITEM_TYPE;
 	           case SCORE:
 	        	   return Score.CONTENT_TYPE;
 	           case SCORE_ID:
 	               return Score.CONTENT_ITEM_TYPE;
 	           // If the URI pattern doesn't match any permitted patterns, throws an exception.
 	           default:
 	               throw new IllegalArgumentException("Unknown URI " + uri);
 	       }
 
 	}
 
 	private static final String DELIMITER = ",";
 	// ghetto serialize
 	public static String serializePlayers(long[] inputArray) {
 		if(inputArray.length == 0) return "";
 		StringBuffer sb = new StringBuffer();
 		for ( long num : inputArray ) {
 			sb.append(num);
 			sb.append(DELIMITER);
 		}
 		return sb.substring(0, sb.length()-1); // don't return the last delimiter
 	}
 	
 	// ghetto deserialize
 	public static long[] deserializePlayers(String inputString) {
 		StringTokenizer st = new StringTokenizer(inputString, DELIMITER);
 		long[] array = new long[st.countTokens()];
 		int n = 0;
 		while (st.hasMoreTokens()) {
 			array[n++] = Long.valueOf(st.nextToken());
 		}
 		return array;
 	}
 	
 	// Open helper to load/create DB.
 	private static class DatabaseHelper extends SQLiteOpenHelper {
 		DatabaseHelper(Context context) {
 			super(context, DB_NAME, null, DB_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL("create table " + Game.TABLE_NAME + " (" + 
 					Game._ID + " integer primary key autoincrement, " + Game.COLUMN_NAME_TYPE + " integer not null, " +
 					Game.COLUMN_NAME_DESCRIPTION + " text not null, " + Game.COLUMN_NAME_PLAYER_IDS + " text not null, " +
 					Game.COLUMN_NAME_CREATE_DATE + " integer not null, " + Game.COLUMN_NAME_MODIFICATION_DATE + " integer not null)");
 			db.execSQL("create table " + Player.TABLE_NAME + " (" + 
 					Player._ID + " integer primary key autoincrement, " + Player.COLUMN_NAME_NAME + " text not null, " + 
 					Player.COLUMN_NAME_COLOR + " integer not null)");
 			db.execSQL("create table " + Score.TABLE_NAME + " (" + 
 					Score._ID + " integer primary key autoincrement, " + Score.COLUMN_NAME_GAME_ID + " integer not null, " +
 					Score.COLUMN_NAME_PLAYER_ID + " integer not null, " + Score.COLUMN_NAME_SCORE + " integer, " +
 					Score.COLUMN_NAME_CONTEXT + " text, " + Score.COLUMN_NAME_CREATE_DATE + " integer not null)");
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			Log.w("ScoreKeep", "Upgrading database, this will drop tables and recreate.");
 			db.execSQL("DROP TABLE IF EXISTS " + Game.TABLE_NAME);
 			db.execSQL("DROP TABLE IF EXISTS " + Player.TABLE_NAME);
 			db.execSQL("DROP TABLE IF EXISTS " + Score.TABLE_NAME);
 			onCreate(db);
 		}
 	}
 }
