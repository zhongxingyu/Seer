 package com.valfom.skydiving.altimeter;
 
 import android.content.ContentProvider;
 import android.content.ContentValues;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 
 public class AltimeterContentProvider extends ContentProvider {
 
 	public static final String AUTHORITY = "com.valfom.skydiving.altimeter.provider";
 
 	public static final Uri CONTENT_URI_TRACKS = Uri.parse("content://"
 			+ AUTHORITY + "/" + AltimeterDB.TABLE_TRACKS);
 	
 	public static final Uri CONTENT_URI_POINTS = Uri.parse("content://"
 			+ AUTHORITY + "/" + AltimeterDB.TABLE_POINTS);
 
 	private static final int URI_TRACKS = 1; // Get all tracks
 	private static final int URI_TRACK_ID = 2; // Get, delete track with ID
 	private static final int URI_TRACK = 3; // Insert track
 	private static final int URI_POINTS_ID = 4; // Get, delete all points with track ID
 	private static final int URI_POINT = 5; // Insert point with track ID
 
 	private static final UriMatcher uriMatcher;
 
 	static {
 		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
 		uriMatcher.addURI(AUTHORITY, AltimeterDB.TABLE_TRACKS, URI_TRACKS);
 		uriMatcher.addURI(AUTHORITY, AltimeterDB.TABLE_TRACKS + "/#", URI_TRACK_ID);
 		uriMatcher.addURI(AUTHORITY, AltimeterDB.TABLE_TRACKS + "/insert", URI_TRACK);
 		uriMatcher.addURI(AUTHORITY, AltimeterDB.TABLE_POINTS + "/#", URI_POINTS_ID);
 		uriMatcher.addURI(AUTHORITY, AltimeterDB.TABLE_POINTS, URI_POINT);
 	}
 
 	private AltimeterDB db;
 
 	public boolean onCreate() {
 
 		db = new AltimeterDB(getContext());
 
 		return true;
 	}
 
 	public String getType(Uri uri) {
 
 		return null;
 	}
 
 	public Cursor query(Uri uri, String[] projection, String selection,
 			String[] selectionArgs, String sortOrder) {
 
 		Cursor cursor = null;
 		SQLiteDatabase sqlDB = db.getReadableDatabase();
 		String id;
 		
 		switch (uriMatcher.match(uri)) {
 			
 			case URI_TRACKS:
 				cursor = db.getAllTracks();
 				break;
 			case URI_TRACK_ID:
 				id = uri.getLastPathSegment();
 				cursor = db.getTrack(Integer.valueOf(id));
 				break;
 			case URI_POINTS_ID:
 				id = uri.getLastPathSegment();
 				
 				String[] columns = new String[] { AltimeterDB.KEY_POINTS_ALTITUDE };
 		        String[] selArgs = new String[] { String.valueOf(id) };
 		        
 		        cursor = sqlDB.query(AltimeterDB.TABLE_POINTS, columns, AltimeterDB.KEY_POINTS_TRACK_ID + "=?",
 		        		selArgs, null, null, null, null);
 				break;
 			default:
 				throw new IllegalArgumentException("Wrong URI: " + uri);
 		}
 		
 		cursor.setNotificationUri(getContext().getContentResolver(), uri);
 		
 		return cursor;
 	}
 
 	public int delete(Uri uri, String selection, String[] selectionArgs) {
 
 		int rowsDeleted = 0;
 		SQLiteDatabase sqlDB = db.getWritableDatabase();
 		String id;
 		
 		switch(uriMatcher.match(uri)) {
 		
 		case URI_TRACK_ID:
 		
 			id = uri.getLastPathSegment();
 			
 			Cursor cursor = sqlDB.rawQuery("SELECT " + AltimeterDB.KEY_TRACKS_DATE + " FROM " + AltimeterDB.TABLE_TRACKS + 
 	    			" WHERE " + AltimeterDB.KEY_TRACKS_ID + "=" + id, null);
 			
 			cursor.moveToFirst();
 			
 			String date = cursor.getString(cursor.getColumnIndex(AltimeterDB.KEY_TRACKS_DATE));
 	    	
 			Cursor cursor1 = sqlDB.rawQuery("SELECT " + AltimeterDB.KEY_TRACKS_ID + ", " + AltimeterDB.KEY_TRACKS_TYPE + " FROM " + AltimeterDB.TABLE_TRACKS + 
 	    			" WHERE " + AltimeterDB.KEY_TRACKS_DATE + "=\"" + date + "\" AND " + AltimeterDB.KEY_TRACKS_ID + " <> " + id, null);
 			
 			if (cursor1.getCount() == 1) {
 				
 				cursor1.moveToFirst();
 				
 				if (cursor1.getInt(cursor1.getColumnIndex(AltimeterDB.KEY_TRACKS_TYPE)) == 2) {
 					
 					int sectionId = cursor1.getInt(cursor1.getColumnIndex(AltimeterDB.KEY_TRACKS_ID));
 					
 					sqlDB.delete(AltimeterDB.TABLE_TRACKS, AltimeterDB.KEY_TRACKS_ID + "=" + sectionId, null);
 				}
 			}
 			
 			rowsDeleted = sqlDB.delete(AltimeterDB.TABLE_TRACKS, AltimeterDB.KEY_TRACKS_ID + "=" + id, null);
 			
 		case URI_POINTS_ID:
 			
 			id = uri.getLastPathSegment();
 			rowsDeleted += sqlDB.delete(AltimeterDB.TABLE_POINTS, AltimeterDB.KEY_POINTS_TRACK_ID + "=" + id, null);
 			break;
 			
 		default:
 	    	throw new IllegalArgumentException("Unknown URI: " + uri);
 		}
 		
 		getContext().getContentResolver().notifyChange(uri, null);
 		
 		return rowsDeleted;
 	}
 
 	public Uri insert(Uri uri, ContentValues values) {
 
 		SQLiteDatabase sqlDB = db.getWritableDatabase();
 		long id = 0;
 		
 	    switch (uriMatcher.match(uri)) {
 	    
 	    case URI_TRACK:
 	    	
 	    	String date = values.getAsString(AltimeterDB.KEY_TRACKS_DATE);
 	    	
 	    	Cursor cursor = sqlDB.rawQuery("SELECT " + AltimeterDB.KEY_TRACKS_ID + " FROM " + AltimeterDB.TABLE_TRACKS + 
 	    			" WHERE " + AltimeterDB.KEY_TRACKS_DATE + "=\"" + date + "\"", null);
 	    	
 	    	if (cursor.getCount() == 0) {
 	    		
 	    		ContentValues vals = new ContentValues();
 	    		
 	    		vals.put(AltimeterDB.KEY_TRACKS_DATE, date);
 	    		vals.put(AltimeterDB.KEY_TRACKS_TYPE, 2);
 	    		
 	    		sqlDB.insert(AltimeterDB.TABLE_TRACKS, null, vals);
 	    	}
 	    	
 	    	id = sqlDB.insert(AltimeterDB.TABLE_TRACKS, null, values);
 	    	
 	    	break;
 	    	
 	    case URI_POINT:
 	    	id = sqlDB.insert(AltimeterDB.TABLE_POINTS, null, values);
 	    	break;
 	    
 	    default:
 	    	throw new IllegalArgumentException("Unknown URI: " + uri);
 	    }
 	    
 	    getContext().getContentResolver().notifyChange(uri, null);
 	    
 	    return Uri.parse(AltimeterDB.TABLE_TRACKS + "/" + id);
 	}
 
 	public int update(Uri uri, ContentValues values, String selection,
 			String[] selectionArgs) {
 
 		return 0;
 	}
 }
