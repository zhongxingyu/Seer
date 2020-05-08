 package org.minyanmate.minyanmate.contentprovider;
 
 import org.minyanmate.minyanmate.database.MinyanContactsTable;
 import org.minyanmate.minyanmate.database.MinyanEventsTable;
 import org.minyanmate.minyanmate.database.MinyanGoersTable;
 import org.minyanmate.minyanmate.database.MinyanMateDatabaseHelper;
 import org.minyanmate.minyanmate.database.MinyanSchedulesTable;
 import org.minyanmate.minyanmate.database.MinyanSubscriptionsTable;
 import org.minyanmate.minyanmate.services.MinyanRegistrar;
 
 import android.content.ContentProvider;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.UriMatcher;
 import android.database.Cursor;
 import android.database.CursorJoiner;
 import android.database.MatrixCursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.provider.ContactsContract.Contacts;
 import android.provider.ContactsContract.Contacts.Data;
 
 public class MinyanMateContentProvider extends ContentProvider {
 
 	private MinyanMateDatabaseHelper database;
 	
 	// used for the UriMatcher
 	private static final int SCHEDULES = 1;
 	private static final int SCHEDULE_ID = 2;
 	private static final int CONTACTS = 3;
 	private static final int CONTACT_ID = 4;
 	private static final int EVENTS = 5;
 	private static final int EVENT_ID = 6;
 	private static final int GOERS = 7;
 	private static final int GOER_ID = 8;
 	
 	
 	private static final String AUTHORITY = "org.minyanmate.minyanmate.contentprovider";
 	private static final String PATH_TIMES = "times";
 	private static final String PATH_CONTACTS = "contacts";
 	private static final String PATH_EVENTS = "events";
 	private static final String PATH_GOERS = "goers";
 	
 	public static final Uri CONTENT_URI_TIMES = Uri.parse("content://" + AUTHORITY + "/" + PATH_TIMES);
 	public static final Uri CONTENT_URI_CONTACTS = Uri.parse("content://" + AUTHORITY + "/" + PATH_CONTACTS);
 	public static final Uri CONTENT_URI_EVENTS = Uri.parse("content://" + AUTHORITY + "/" + PATH_EVENTS);
 	public static final Uri CONTENT_URI_EVENT_GOERS = Uri.parse("content://" + AUTHORITY + "/" + PATH_GOERS);
 	
 	// Not really sure what these do but they seem to be used for passing MIME types for intents...
 	public static final String CONTENT_TYPE_TIME = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.minyanmate.times";
 	public static final String CONTENT_ITEM_TYPE_TIME = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.minyanmate.time";
 	public static final String CONTENT_TYPE_CONTACT = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.minyanmate.contacts";
 	public static final String CONTENT_ITEM_TYPE_CONTACT = ContentResolver.CURSOR_ITEM_BASE_TYPE + "vnd.minyanmate.contact";
 	public static final String CONTENT_TYPE_EVENT = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.minyanmate.events";
 	public static final String CONTENT_ITEM_TYPE_EVENT = ContentResolver.CURSOR_ITEM_BASE_TYPE + "vnd.minyanmate.event";
 	public static final String CONTENT_TYPE_EVENT_GOER = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.minyanmates.goers";
 	public static final String CONTENT_ITEM_TYPE_GOER = ContentResolver.CURSOR_ITEM_BASE_TYPE + "vnd.minyanmate.goer";
 	
 	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
 	static {
 		sURIMatcher.addURI(AUTHORITY, PATH_CONTACTS, CONTACTS);
 		sURIMatcher.addURI(AUTHORITY, PATH_CONTACTS + "/*", CONTACT_ID);
 		sURIMatcher.addURI(AUTHORITY, PATH_TIMES, SCHEDULES);
 		sURIMatcher.addURI(AUTHORITY, PATH_TIMES + "/#", SCHEDULE_ID);
 		sURIMatcher.addURI(AUTHORITY, PATH_EVENTS, EVENTS);
 		sURIMatcher.addURI(AUTHORITY, PATH_EVENTS + "/#", EVENT_ID);
 		sURIMatcher.addURI(AUTHORITY, PATH_GOERS, GOERS);
 		sURIMatcher.addURI(AUTHORITY, PATH_GOERS + "/*", GOER_ID);
 	}
 	
 	@Override
 	public boolean onCreate() {
 		database = new MinyanMateDatabaseHelper(getContext());
 		return false;
 	}	
 	
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
 			String sortOrder) {
 		
 		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
 		SQLiteDatabase db = database.getWritableDatabase();
 		Cursor cursor;
 		
 		int uriType = sURIMatcher.match(uri);
 		
 		switch (uriType) {
 		
 			case SCHEDULE_ID:
 				queryBuilder.appendWhere(MinyanSchedulesTable.COLUMN_ID + "=" + uri.getLastPathSegment());
 				// Fall through
 			case SCHEDULES:
 				queryBuilder.setTables(MinyanSchedulesTable.TABLE_MINYAN_SCHEDULES);
 				cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
 				cursor.setNotificationUri(getContext().getContentResolver(), uri);
 				return cursor;
 
 
 			case CONTACT_ID:
 				queryBuilder.appendWhere(MinyanContactsTable.COLUMN_ID + "=" + uri.getLastPathSegment());
 				// fall through
 			case CONTACTS:
 				
 				/*
 				 * use CursorJoiner and MatrixCursor to create a cursor of a data abstraction
 				 * representing the JOIN of the stored contact keys and the phone's contact info
 				 */
 				
 				queryBuilder.setTables(MinyanContactsTable.TABLE_MINYAN_CONTACTS);
 				cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, 
 						MinyanContactsTable.COLUMN_CONTACT_LOOKUP_KEY + " asc");
 				cursor.setNotificationUri(getContext().getContentResolver(), uri);		
 				
 				Cursor phoneContacts = getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
 						ContactMatrix.queryProj, null, null, Contacts.LOOKUP_KEY + " asc");
 				
 				MatrixCursor minyanContactsMatrix = new MatrixCursor(ContactMatrix.queryProj);
 				
 				CursorJoiner joiner = new CursorJoiner(cursor, 
 						new String[] { MinyanContactsTable.COLUMN_CONTACT_LOOKUP_KEY }, 
 						phoneContacts, new String[] { Contacts.LOOKUP_KEY });
 				
 				for (CursorJoiner.Result joinerResult : joiner) {
 					switch (joinerResult) {
 					case LEFT: // Ignore LEFT JOIN
 						break;
 					case RIGHT: // Ignore RIGHT JOIN
 						break;
 					case BOTH: // Only do things on inner joins
 						
 						minyanContactsMatrix.addRow(new Object[] {
 							phoneContacts.getLong(phoneContacts.getColumnIndex(Contacts._ID)),
 							phoneContacts.getString(phoneContacts.getColumnIndex(Contacts.PHOTO_THUMBNAIL_URI)),
 							phoneContacts.getString(phoneContacts.getColumnIndex(Contacts.DISPLAY_NAME)),
 							phoneContacts.getString(phoneContacts.getColumnIndex(Data.DATA1)),
 							phoneContacts.getString(phoneContacts.getColumnIndex(Contacts.LOOKUP_KEY))
 						});
 						
 						break;
 					}
 				}
 				
 				minyanContactsMatrix.setNotificationUri(getContext().getContentResolver(), uri);
 				
 				return minyanContactsMatrix;
 			
 			case EVENT_ID:
 				queryBuilder.appendWhere(MinyanEventsTable.COLUMN_ID + "=" + uri.getLastPathSegment());
 				// fall through
 			case EVENTS:
 				
 				queryBuilder.setTables(MinyanEventsTable.TABLE_MINYAN_EVENTS);
 				cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, 
 						sortOrder);
 				cursor.setNotificationUri(getContext().getContentResolver(), uri);
 				return cursor;
 				
 			case GOER_ID:
 				queryBuilder.appendWhere("(" + MinyanGoersTable.COLUMN_LOOKUP_KEY + "=" + uri.getLastPathSegment()
 						+ " OR " + MinyanGoersTable.COLUMN_GENERAL_NAME + "=" + uri.getLastPathSegment() + ")");
 				// fall through
 			case GOERS:
 				
 				/*
 				 * use CursorJoiner and MatrixCursor to create a cursor of a data abstraction
 				 * representing the JOIN of the stored contact keys and the phone's contact info
 				 */
 				
 				queryBuilder.setTables(MinyanGoersTable.TABLE_MINYAN_INVITEES);
 				cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, 
 						MinyanGoersTable.COLUMN_LOOKUP_KEY + " asc");
 				
				int count = cursor.getCount();
				
 				cursor.setNotificationUri(getContext().getContentResolver(), uri);		
 				
 				Cursor phoneContacts2 = getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
 						GoerMatrix.queryProj, null, null, Contacts.LOOKUP_KEY + " asc");
 				
 				MatrixCursor goers = new MatrixCursor(GoerMatrix.matrixAttrs);
 				
 				CursorJoiner goersJoiner = new CursorJoiner(cursor, 
 						new String[] { MinyanGoersTable.COLUMN_LOOKUP_KEY}, 
 						phoneContacts2, new String[] { Contacts.LOOKUP_KEY });
 				
 				for (CursorJoiner.Result joinerResult : goersJoiner) {
 					// TODO fix the issue where the eventId isn't being saved
 					switch (joinerResult) {
 					case LEFT: // Left join is result of a random attendee and should be recorded as such
 						
 						goers.addRow(new Object[] {
 							cursor.getInt(cursor.getColumnIndex(MinyanGoersTable.COLUMN_ID)),
 							cursor.getInt(cursor.getColumnIndex(MinyanGoersTable.COLUMN_IS_INVITED)),
 							cursor.getInt(cursor.getColumnIndex(MinyanGoersTable.COLUMN_MINYAN_EVENT_ID)),
 							cursor.getInt(cursor.getColumnIndex(MinyanGoersTable.COLUMN_INVITE_STATUS)),
 							null,
 							null,
 							cursor.getString(cursor.getColumnIndex(MinyanGoersTable.COLUMN_GENERAL_NAME)),
 							null,
 							null
 						});
 						
 						break;
 					case RIGHT: // Ignore RIGHT JOIN
 						break;
 					case BOTH: // Only do things on inner joins
 						
 						goers.addRow(new Object[] {
 							cursor.getInt(cursor.getColumnIndex(MinyanGoersTable.COLUMN_ID)),
 							cursor.getInt(cursor.getColumnIndex(MinyanGoersTable.COLUMN_IS_INVITED)),
 							cursor.getInt(cursor.getColumnIndex(MinyanGoersTable.COLUMN_MINYAN_EVENT_ID)),
 							cursor.getInt(cursor.getColumnIndex(MinyanGoersTable.COLUMN_INVITE_STATUS)),
 							phoneContacts2.getLong(phoneContacts2.getColumnIndex(Contacts._ID)),
 							phoneContacts2.getString(phoneContacts2.getColumnIndex(Contacts.PHOTO_THUMBNAIL_URI)),
 							phoneContacts2.getString(phoneContacts2.getColumnIndex(Contacts.DISPLAY_NAME)),
 							phoneContacts2.getString(phoneContacts2.getColumnIndex(Data.DATA1)),
 							phoneContacts2.getString(phoneContacts2.getColumnIndex(Contacts.LOOKUP_KEY))
 						});
 						
 						break;
 					}
 				}
 				goers.setNotificationUri(getContext().getContentResolver(), uri);
 				
 				return goers;
 				
 			default:
 				throw new IllegalArgumentException("Unknown URI: " + uri);
 		}
 	}
 	
 	
 	@Override
 	public int delete(Uri uri, String selection, String[] selectionArgs) {
 		int uriType = sURIMatcher.match(uri);
 		SQLiteDatabase db = database.getWritableDatabase();
 		int rowsDeleted = 0;
 		switch (uriType) {
 			case CONTACTS:
 				rowsDeleted = db.delete(MinyanContactsTable.TABLE_MINYAN_CONTACTS, 
 						selection, selectionArgs);
 				break;
 				
 			case CONTACT_ID: // may not be reachable
 				rowsDeleted = db.delete(MinyanContactsTable.TABLE_MINYAN_CONTACTS, 
 						MinyanContactsTable.COLUMN_ID + "=?", new String[] { uri.getLastPathSegment()});
 				break;
 				
 			case EVENTS:
 				rowsDeleted = db.delete(MinyanEventsTable.TABLE_MINYAN_EVENTS,
 						selection, selectionArgs);
 				break;
 				
 			case EVENT_ID:
 				rowsDeleted = db.delete(MinyanEventsTable.TABLE_MINYAN_EVENTS,
 						MinyanEventsTable.COLUMN_ID + "=?", new String[] { uri.getLastPathSegment()});
 				break;
 				
 			case GOERS:
 				rowsDeleted = db.delete(MinyanGoersTable.TABLE_MINYAN_INVITEES,
 						selection, selectionArgs);
 				break;
 			case GOER_ID:
 				rowsDeleted = db.delete(MinyanGoersTable.TABLE_MINYAN_INVITEES,
 						MinyanGoersTable.COLUMN_ID + "=?", new String[] { uri.getLastPathSegment()});
 				break;
 				
 			default:
 				throw new IllegalArgumentException("Unknown URI: " + uri);
 		}
 		
 		getContext().getContentResolver().notifyChange(uri, null);
 		return rowsDeleted;
 	}
 
 	// TODO eventually implementing this could be a good idea
 	@Override
 	public String getType(Uri uri) {
 		return null;
 	}
 
 	@Override
 	public Uri insert(Uri uri, ContentValues values) {
 		int uriType = sURIMatcher.match(uri);
 		SQLiteDatabase sqlDb = database.getWritableDatabase();
 		long id = 0;
 		switch(uriType) {
 		case SCHEDULES:
 			// TODO delete this, shouldn't be able to add times
 			// id = sqlDb.insert(MinyanTimesTable.TABLE_MINYAN_TIMES, null, values);	
 //			getContext().getContentResolver().notifyChange(uri, null);
 //			return Uri.parse(PATH_CONTACTS + "/" + id);
 			
 		case CONTACTS:
 			// If desubscribed, do not allow adding
 			Cursor subscrier = sqlDb.query(MinyanSubscriptionsTable.TABLE_SUBSCRIPTIONS, 
 					null, MinyanSubscriptionsTable.COLUMN_CONTACT_LOOKUP_KEY + "=?", 
 					new String[] { (String) values.get(MinyanContactsTable.COLUMN_CONTACT_LOOKUP_KEY) }, 
 					null, null, null);
 			
 			boolean isSubscribed = true;
 			
 			if (subscrier.moveToFirst()) {
 				
 				isSubscribed = (subscrier.getInt(subscrier.getColumnIndex(MinyanSubscriptionsTable.COLUMN_IS_SUBSCRIBED)) 
 						== 1) ? true : false;
 			}
 			
 			
 			if (isSubscribed) {
 				id = sqlDb.insert(MinyanContactsTable.TABLE_MINYAN_CONTACTS, null, values);
 				getContext().getContentResolver().notifyChange(uri, null);
 				return Uri.parse(PATH_CONTACTS + "/" + id);
 			} else
 				return null;
 		
 		case EVENTS:
 			id = sqlDb.insert(MinyanEventsTable.TABLE_MINYAN_EVENTS, null, values);
 			getContext().getContentResolver().notifyChange(uri, null);
 			return Uri.parse(PATH_EVENTS + "/" + id);
 			
 		case GOERS:
 			id = sqlDb.insert(MinyanGoersTable.TABLE_MINYAN_INVITEES, null, values);
 			getContext().getContentResolver().notifyChange(uri, null);
 			return Uri.parse(PATH_GOERS + "/" + id);
 			
 		default:
 			throw new IllegalArgumentException("Unknown URI: " + uri);
 		}
 	}
 
 
 	@Override
 	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
 		int uriType = sURIMatcher.match(uri);
 		SQLiteDatabase db = database.getWritableDatabase();
 		int rowsUpdated = 0;
 		switch (uriType) {
 			case SCHEDULES:
 				rowsUpdated = db.update(MinyanSchedulesTable.TABLE_MINYAN_SCHEDULES,
 						values, selection, selectionArgs);
 				
 				// Update the MinyanRegistrar
 				updateMinyanRegistrar();
 				break;
 			case SCHEDULE_ID:
 				// TODO implement this
 				rowsUpdated = db.update(MinyanSchedulesTable.TABLE_MINYAN_SCHEDULES, values,
 						MinyanSchedulesTable.COLUMN_ID + "=?", new String[] { uri.getLastPathSegment() });
 				
 				// Update the MinyanRegistrar
 				updateMinyanRegistrar();
 				break;
 		
 			case EVENTS:
 				rowsUpdated = db.update(MinyanEventsTable.TABLE_MINYAN_EVENTS, values, selection, selectionArgs);
 				break;
 				
 			case EVENT_ID:
 				rowsUpdated = db.update(MinyanEventsTable.TABLE_MINYAN_EVENTS, values, 
 						MinyanEventsTable.COLUMN_ID + "=?", new String[] { uri.getLastPathSegment() });
 				break;
 				
 			case GOERS:
 				rowsUpdated = db.update(MinyanGoersTable.TABLE_MINYAN_INVITEES, values, selection, selectionArgs);
 				break;
 				
 			case GOER_ID: // TODO define a better mapping MatrixCursor so that the id of attendees is freely available
 				rowsUpdated = db.update(MinyanGoersTable.TABLE_MINYAN_INVITEES, values, 
 						MinyanGoersTable.COLUMN_ID + "=?", new String[] { uri.getLastPathSegment()});
 				break;
 				
 			default:
 				throw new IllegalArgumentException("Unknown URI: " + uri);
 		}
 		
 		getContext().getContentResolver().notifyChange(uri, null);
 		return rowsUpdated;
 	}
 	
 	/**
 	 * Used with {@link MinyanRegistrar#registerMinyanEvents(android.content.Context, Cursor)}
 	 * to brute-force re-register and/or cancel existing minyans on any update of the {@link #SCHEDULES}
 	 * family of data. 
 	 * @param context
 	 * @param cursor
 	 */
 	private void updateMinyanRegistrar() {
 		 Cursor cursor = database.getReadableDatabase().query(
 				 MinyanSchedulesTable.TABLE_MINYAN_SCHEDULES, 
 				 null, null, null, null, null, null);
 		 
 		 MinyanRegistrar.registerMinyanEvents(getContext(), cursor);
 		 cursor.close();
 	}
 	
 	
 	/**
 	 * Contains the projection/cursormatrix column properties in 
 	 * {@link #queryProj} and has other properties.
 	 */
 	public static class ContactMatrix {
 		public static final String[] queryProj = new String[] {
 					Contacts._ID,
 					Contacts.PHOTO_THUMBNAIL_URI,
 					Contacts.DISPLAY_NAME,
 					Phone.NUMBER,
 					Contacts.LOOKUP_KEY
 		};
 		
 		public static final int ID = 0;
 		public static final int THUMBNAIL_PHOTO_URI = 1;
 		public static final int NAME = 2;
 		public static final int NUM = 3;
 		public static final int KEY = 4;
 	}
 	
 	/**
 	 * Contains the projection in {@link #queryProj} and the matrixcursor
 	 * column definitions in {@link #matrixAttrs}.
 	 */
 	public static class GoerMatrix {
 		public static final String[] queryProj = new String[] {
 			Contacts._ID,
 			Contacts.PHOTO_THUMBNAIL_URI,
 			Contacts.DISPLAY_NAME,
 			Phone.NUMBER,
 			Contacts.LOOKUP_KEY
 		};
 		
 		/**
 		 * Similar to {@link ContactMatrix#queryProj}, however because
 		 * this result set is polymorphic on {@link #IS_INVITED}, need a separate
 		 * string array with that additional property.
 		 */
 		public static final String[] matrixAttrs = new String[] {
 			MinyanGoersTable.COLUMN_ID,
 			MinyanGoersTable.COLUMN_IS_INVITED,
 			MinyanGoersTable.COLUMN_MINYAN_EVENT_ID,
 			MinyanGoersTable.COLUMN_INVITE_STATUS,
 			Contacts._ID,
 			Contacts.PHOTO_THUMBNAIL_URI,
 			Contacts.DISPLAY_NAME,
 			Phone.NUMBER,
 			Contacts.LOOKUP_KEY
 		};
 		
 		public static final int GOER_ID = 0;
 		public static final int IS_INVITED = 1;
 		public static final int EVENT_ID = 2;
 		public static final int INVITE_STATUS = 3;
 		public static final int CONTACT_ID = 4;
 		public static final int THUMBNAIL_PHOTO_URI =5;
 		public static final int NAME = 6;
 		public static final int NUM = 7;
 		public static final int KEY = 8;
 	}
 }
