 /*
  * Copyright (c) 2013 Andrew Fontaine, James Finlay, Jesse Tucker, Jacob Viau, and
  * Evan DeGraff
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in
  * the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
  * the Software, and to permit persons to whom the Software is furnished to do so,
  * subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
  * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
  * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
  * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package ca.cmput301f13t03.adventure_datetime.model;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.provider.BaseColumns;
 import android.util.Log;
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 
 import java.io.ByteArrayOutputStream;
 import java.lang.reflect.Type;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 
 /**
  * The local database containing all stories, fragments, bookmarks, and subscriptions
  *
  * @author Andrew Fontaine
  * @version 1.0
  * @since 28/10/13
  */
 public class StoryDB implements BaseColumns {
 
 	private static final String TAG = "StoryDB";
 
 	public static final String COLUMN_GUID = "GUID";
 
 	public static final String STORY_TABLE_NAME = "Story";
 	public static final String STORY_COLUMN_AUTHOR = "Author";
 	public static final String STORY_COLUMN_HEAD_FRAGMENT = "HeadFragment";
 	public static final String STORY_COLUMN_TIMESTAMP = "Timestamp";
 	public static final String STORY_COLUMN_SYNOPSIS = "Synopsis";
 	public static final String STORY_COLUMN_THUMBNAIL = "Thumbnail";
 	public static final String STORY_COLUMN_TITLE = "Title";
 
 	public static final String STORYFRAGMENT_TABLE_NAME = "StoryFragment";
 	public static final String STORYFRAGMENT_COLUMN_STORYID = "StoryID";
 	public static final String STORYFRAGMENT_COLUMN_CONTENT = "Content";
 	public static final String STORYFRAGMENT_COLUMN_CHOICES = "Choices";
 
 	public static final String BOOKMARK_TABLE_NAME = "Bookmark";
 	public static final String BOOKMARK_COLUMN_STORYID = "StoryID";
 	public static final String BOOKMARK_COLUMN_FRAGMENTID = "FragmentID";
 	public static final String BOOKMARK_COLUMN_DATE = "Date";
 
 	private StoryDBHelper mDbHelper;
 
 	public StoryDB(Context context) {
 		mDbHelper = new StoryDBHelper(context);
 	}
 
 	/**
 	 * Grabs a story with the given id from the local database
 	 *
 	 * @param id The GUID of the story
 	 *
 	 * @return The Story object or null if the id doesn't exist in the database
 	 */
 	public Story getStory(String id) {
 		SQLiteDatabase db = mDbHelper.getReadableDatabase();
 
 		Cursor cursor = db.query(STORY_TABLE_NAME,
				new String[]{_ID, COLUMN_GUID, STORY_COLUMN_TITLE, STORY_COLUMN_AUTHOR,
 						STORY_COLUMN_HEAD_FRAGMENT, STORY_COLUMN_SYNOPSIS, STORY_COLUMN_TIMESTAMP, STORY_COLUMN_THUMBNAIL},
 				COLUMN_GUID + " = ?",
 				new String[] {id},
 				null,
 				null,
 				null,
 				"1");
 
 		Story story;
 
 		if (cursor.moveToFirst()) {
 			story = createStory(cursor);
 		}
 		else
 			story = null;
 
 		cursor.close();
 		db.close();
 		return story;
 	}
 
 	/**
 	 * Retrieves all stories located on local storage
 	 *
 	 * @return Collection of all stories on local storage. Collection is empty if there are no stories
 	 */
 	public ArrayList<Story> getStories() {
 		SQLiteDatabase db = mDbHelper.getReadableDatabase();
 
 		Cursor cursor = db.query(STORY_TABLE_NAME,
				new String[] {_ID, COLUMN_GUID, STORY_COLUMN_AUTHOR,
 						STORY_COLUMN_HEAD_FRAGMENT, STORY_COLUMN_SYNOPSIS, STORY_COLUMN_TIMESTAMP, STORY_COLUMN_THUMBNAIL},
 				null,
 				null,
 				null,
 				null,
 				null);
 		ArrayList<Story> stories = new ArrayList<Story>();
 
 		if(cursor.moveToFirst()) {
 			do {
 				stories.add(createStory(cursor));
 			} while(cursor.moveToNext());
 		}
 
 		cursor.close();
 		db.close();
 		return stories;
 	}
 
 	/**
 	 * Retrieves all stories located on local storage by an author
 	 *
 	 * @return Collection of all stories on local storage by an author. Collection is empty if there are no stories by
 	 *         author.
 	 *
 	 * @return Collection of all stories on local storage by an author. Collection is empty if there are no stories by
 	 *         author.
 	 */
 	public ArrayList<Story> getStoriesAuthoredBy(String author) {
 		SQLiteDatabase db = mDbHelper.getReadableDatabase();
 
 		Cursor cursor = db.query(STORY_TABLE_NAME,
 				new String[]{_ID, COLUMN_GUID, STORY_COLUMN_AUTHOR,
 						STORY_COLUMN_HEAD_FRAGMENT, STORY_COLUMN_SYNOPSIS, STORY_COLUMN_TIMESTAMP, STORY_COLUMN_THUMBNAIL},
 				STORY_COLUMN_AUTHOR + " = ?",
 				new String[] {author},
 				null,
 				null,
 				null);
 		ArrayList<Story> stories = new ArrayList<Story>();
 
 		if (cursor.moveToFirst()) {
 			do {
 				stories.add(createStory(cursor));
 			} while (cursor.moveToNext());
 		}
 
 		cursor.close();
 		db.close();
 		return stories;
 	}
 
 	/**
 	 * Retrieves story fragments by specific ID from local storage
 	 *
 	 * @param id The GUID of the fragment
 	 *
 	 * @return StoryFragment instance or null
 	 */
 	public StoryFragment getStoryFragment(String id) {
 		SQLiteDatabase db = mDbHelper.getReadableDatabase();
 
 		Cursor cursor = db.query(STORYFRAGMENT_TABLE_NAME,
 				new String[] {_ID, COLUMN_GUID, STORYFRAGMENT_COLUMN_STORYID, STORYFRAGMENT_COLUMN_CHOICES, STORYFRAGMENT_COLUMN_CONTENT},
 				COLUMN_GUID + " = ?",
 				new String[] {id},
 				null,
 				null,
 				"1");
 
 		StoryFragment frag;
 		if(cursor.moveToFirst())
 			frag = createStoryFragment(cursor);
 		else
 			frag = null;
 		cursor.close();
 		db.close();
 		return frag;
 	}
 
 	/**
 	 * Retrieves all StoryFragments for a particular Story's _ID
 	 *
 	 * @param storyid The _ID for a story
 	 *
 	 * @return A Collection of StoryFragments. Empty if none exist.
 	 */
 	public ArrayList<StoryFragment> getStoryFragments(String storyid) {
 		SQLiteDatabase db = mDbHelper.getReadableDatabase();
 
 		Cursor cursor = db.query(STORYFRAGMENT_TABLE_NAME,
 				new String[]{_ID, COLUMN_GUID, STORYFRAGMENT_COLUMN_STORYID, STORYFRAGMENT_COLUMN_CHOICES, STORYFRAGMENT_COLUMN_CONTENT},
 				STORYFRAGMENT_COLUMN_STORYID + " = ?",
 				new String[]{storyid},
 				null,
 				null,
 				null);
 
 		ArrayList<StoryFragment> fragments = new ArrayList<StoryFragment>();
 
 		if(cursor.moveToFirst())
 		{
 			do
 			{
 				fragments.add(createStoryFragment(cursor));
 			}while(cursor.moveToNext());
 		}
 
 		cursor.close();
 		db.close();
 		return fragments;
 	}
 
 	/**
 	 * Get a Bookmark from the Story ID and Fragment ID
 	 * @param storyid The _ID of the story
 	 * @param fragmentid The _ID of the story fragment
 	 * @return a Bookmark object from the database
 	public Bookmark getBookmark(long storyid, long fragmentid) {
 		SQLiteDatabase db = mDbHelper.getReadableDatabase();
 
 		Cursor cursor = db.query(BOOKMARK_TABLE_NAME,
 				new String[] {_ID, BOOKMARK_COLUMN_STORYID, BOOKMARK_COLUMN_FRAGMENTID},
 				BOOKMARK_COLUMN_FRAGMENTID + " = ? AND " + BOOKMARK_COLUMN_STORYID + " = ?",
 				new String[] {String.valueOf(fragmentid), String.valueOf(storyid)},
 				null,
 				null,
 				null);
 
 		Bookmark bookmark;
 
 		if(cursor.moveToFirst())
 			bookmark = createBookmark(cursor);
 		else
 			bookmark = null;
 
 		cursor.close();
 		db.close();
 		return bookmark;
 	}*/
 
 	/**
 	 * Gets the Bookmark from the story ID
 	 *
 	 * @param storyid The _ID of the story
 	 *
 	 * @return The Bookmark object
 	 */
 	public Bookmark getBookmark(String storyid) {
 		SQLiteDatabase db = mDbHelper.getReadableDatabase();
 
 		Cursor cursor = db.query(BOOKMARK_TABLE_NAME,
 				new String[] {_ID, BOOKMARK_COLUMN_STORYID, BOOKMARK_COLUMN_FRAGMENTID, BOOKMARK_COLUMN_DATE},
 				BOOKMARK_COLUMN_STORYID + " = ?",
 				new String[] {storyid},
 				null,
 				null,
 				null);
 
 		Bookmark bookmark;
 
 		if(cursor.moveToFirst())
 			bookmark = createBookmark(cursor);
 		else
 			bookmark = null;
 
 		cursor.close();
 		db.close();
 		return bookmark;
 	}
 
 	/**
 	 * Gets all Bookmarks in the database
 	 *
 	 * @return An ArrayList containing all bookmarks
 	 */
 	public ArrayList<Bookmark> getAllBookmarks() {
 		SQLiteDatabase db = mDbHelper.getReadableDatabase();
 		Cursor cursor = db.query(BOOKMARK_TABLE_NAME,
 				new String[] {BOOKMARK_COLUMN_STORYID, BOOKMARK_COLUMN_FRAGMENTID, BOOKMARK_COLUMN_DATE},
 				null,
 				null,
 				null,
 				null,
 				null);
 
 		ArrayList<Bookmark> bookmarks = new ArrayList<Bookmark>();
 		if(cursor.moveToFirst()) {
 			do {
 				bookmarks.add(createBookmark(cursor));
 			} while(cursor.moveToNext());
 		}
 
 		return bookmarks;
 	}
 
 	/**
 	 * Inserts or updates a bookmark into the Database
 	 *
 	 * @param bookmark The updated bookmark to be inserted
 	 *
 	 * @return True if successful, false if not.
 	 */
 	public boolean setBookmark(Bookmark bookmark) {
 		SQLiteDatabase db = mDbHelper.getWritableDatabase();
 
 		Cursor cursor = db.query(BOOKMARK_TABLE_NAME,
 				new String[] {_ID, BOOKMARK_COLUMN_FRAGMENTID, BOOKMARK_COLUMN_STORYID, BOOKMARK_COLUMN_DATE},
 				BOOKMARK_COLUMN_STORYID + " = ?",
 				new String[] {bookmark.getStoryID()},
 				null,
 				null,
 				null);
 
 		ContentValues values = new ContentValues();
 
 		values.put(BOOKMARK_COLUMN_STORYID, bookmark.getStoryID());
 		values.put(BOOKMARK_COLUMN_FRAGMENTID, bookmark.getFragmentID());
 		values.put(BOOKMARK_COLUMN_DATE, bookmark.getDate().getTime()/1000);
 
 		long updated;
 		if(cursor.moveToFirst()) {
 			Bookmark bookmark1 = createBookmark(cursor);
 			if(bookmark.getDate().compareTo(bookmark1.getDate()) > 0) {
 				updated = db.update(BOOKMARK_TABLE_NAME,values,BOOKMARK_COLUMN_STORYID + " = ?",
 						new String[] {BOOKMARK_COLUMN_STORYID});
 				cursor.close();
 				db.close();
 				return updated == 1;
 			}
 			cursor.close();
 			db.close();
 			return false;
 		}
 		updated = db.insert(BOOKMARK_TABLE_NAME, null, values);
 		cursor.close();
 		db.close();
 		return updated != -1;
 	}
 
 	/**
 	 * Inserts or Updates a Story in the Database
 	 *
 	 * @param story The story to push into the Database
 	 *
 	 * @return True if successful, false if not
 	 */
 	public boolean setStory(Story story) {
 		int size = story.getThumbnail().getByteCount();
 		ByteArrayOutputStream blob = new ByteArrayOutputStream();
 		story.getThumbnail().compress(Bitmap.CompressFormat.PNG, 0, blob);
 		byte[] bytes = blob.toByteArray();
 
 		SQLiteDatabase db = mDbHelper.getWritableDatabase();
 		ContentValues values = new ContentValues();
 		values.put(STORY_COLUMN_TITLE, story.getTitle());
 		values.put(STORY_COLUMN_AUTHOR, story.getAuthor());
 		values.put(STORY_COLUMN_HEAD_FRAGMENT, story.getHeadFragmentId());
 		values.put(STORY_COLUMN_SYNOPSIS, story.getSynopsis());
 		values.put(STORY_COLUMN_TIMESTAMP, story.getTimestamp());
 		values.put(STORY_COLUMN_THUMBNAIL, bytes);
 		values.put(COLUMN_GUID, story.getId());
 
 		Cursor cursor = db.query(STORY_TABLE_NAME,
 				new String[] {_ID},
 				COLUMN_GUID + " = ?",
 				new String[] {story.getId()},
 				null,
 				null,
 				null);
 		if(cursor.moveToFirst()) {
 			int updated;
 			updated = db.update(STORY_TABLE_NAME, values, COLUMN_GUID + " = ?",
 					new String [] {story.getId()});
 			cursor.close();
 			db.close();
 			return updated == 1;
 		}
 		cursor.close();
 		long inserted;
 		inserted = db.insert(STORY_TABLE_NAME, null, values);
 		db.close();
 		return inserted != -1;
 	}
 
 	/**
 	 * Inserts or pushes a story fragment to the database
 	 *
 	 * @param frag The fragment to insert or update
 	 *
 	 * @return True if successful, false if not
 	 */
 	public boolean setStoryFragment(StoryFragment frag) {
 		SQLiteDatabase db = mDbHelper.getWritableDatabase();
 		ContentValues values = new ContentValues();
 		values.put(STORYFRAGMENT_COLUMN_STORYID, frag.getStoryID());
 		values.put(STORYFRAGMENT_COLUMN_CONTENT, frag.getStoryText());
 		values.put(STORYFRAGMENT_COLUMN_CHOICES, frag.getChoicesInJson());
 		values.put(COLUMN_GUID, frag.getFragmentID());
 
 		Cursor cursor = db.query(STORYFRAGMENT_TABLE_NAME,
 				new String[] {_ID, STORYFRAGMENT_COLUMN_STORYID},
 				COLUMN_GUID + " = ? AND " + STORYFRAGMENT_COLUMN_STORYID + " = ?",
 				new String[] {frag.getFragmentID(), frag.getStoryID()},
 				null,
 				null,
 				null);
 		if(cursor.moveToFirst()) {
 			int updated;
 			updated = db.update(STORYFRAGMENT_TABLE_NAME, values, COLUMN_GUID + " = ? AND " + STORYFRAGMENT_COLUMN_STORYID + " = ?",
 					new String[] {frag.getFragmentID(), frag.getStoryID()});
 			cursor.close();
 			db.close();
 			return updated == 1;
 		}
 		long inserted;
 		inserted = db.insert(STORYFRAGMENT_TABLE_NAME, null, values);
 		db.close();
 		return inserted != -1;
 
 	}
 
 	/**
 	 * Creates story from a cursor
 	 *
 	 * @param cursor A Cursor pointing to a Story
 	 *
 	 * @return A Story instance from the Database
 	 */
 	private Story createStory(Cursor cursor) {
 		String title, author, synopsis;
 		String headFragmentId, id;
 		long timestamp;
 		Bitmap thumbnail;
 
 		id = cursor.getString(cursor.getColumnIndex(StoryDB.COLUMN_GUID));
 		title = cursor.getString(cursor.getColumnIndex(StoryDB.STORY_COLUMN_TITLE));
 		headFragmentId = cursor.getString(cursor.getColumnIndex(StoryDB.STORY_COLUMN_HEAD_FRAGMENT));
 		author = cursor.getString(cursor.getColumnIndex(StoryDB.STORY_COLUMN_AUTHOR));
 		synopsis = cursor.getString(cursor.getColumnIndex(StoryDB.STORY_COLUMN_SYNOPSIS));
 		byte[] thumb = cursor.getBlob(cursor.getColumnIndex(StoryDB.STORY_COLUMN_THUMBNAIL));
 		thumbnail = BitmapFactory.decodeByteArray(thumb, 0, thumb.length);
 		timestamp = cursor.getLong(cursor.getColumnIndex(StoryDB.STORY_COLUMN_TIMESTAMP));
 		
 		Story newStory = new Story(headFragmentId, id, author, timestamp, synopsis, thumbnail, title);
 		
 		ArrayList<StoryFragment> referencedFragments = this.getStoryFragments(id);
 		
 		for(StoryFragment frag : referencedFragments)
 		{
 			newStory.addFragment(frag);
 		}
 		
 		return newStory;
 	}
 
 	/**
 	 * Creates a StoryFragment from a cursor
 	 *
 	 * @param cursor A Cursor pointing to a StoryFragment
 	 *
 	 * @return A StoryFragment instance from the Database
 	 */
 	private StoryFragment createStoryFragment(Cursor cursor) {
 		String storyID, fragmentID;
 		String storyText;
 		ArrayList<Choice> choices;
 		storyID = cursor.getString(cursor.getColumnIndex(StoryDB.STORYFRAGMENT_COLUMN_STORYID));
 		fragmentID = cursor.getString(cursor.getColumnIndex(StoryDB.COLUMN_GUID));
 		storyText = cursor.getString(cursor.getColumnIndex(StoryDB.STORYFRAGMENT_COLUMN_CONTENT));
 		/* TODO figure out JSON serialization for choices and media */
 		String json = cursor.getString(cursor.getColumnIndex(StoryDB.STORYFRAGMENT_COLUMN_CHOICES));
 		Gson gson = new Gson();
 		Type collectionType = new TypeToken<Collection<Choice>>(){}.getType();
 		choices = gson.fromJson(json, collectionType);
 
 		return new StoryFragment(choices, storyID, fragmentID, storyText);
 	}
 
 	/**
 	 * Creates a Bookmark from a cursor
 	 *
 	 * @param cursor A Cursor pointing to a Bookmark
 	 *
 	 * @return A Bookmark instance from the Database
 	 */
 	private Bookmark createBookmark(Cursor cursor) {
 		String fragmentID, storyID;
 		Date date;
 
 		fragmentID = cursor.getString(cursor.getColumnIndex(StoryDB.BOOKMARK_COLUMN_FRAGMENTID));
 		storyID = cursor.getString(cursor.getColumnIndex(StoryDB.BOOKMARK_COLUMN_STORYID));
 		long unix = cursor.getLong(cursor.getColumnIndex(StoryDB.BOOKMARK_COLUMN_DATE));
 		Calendar cal = Calendar.getInstance();
 		cal.setTimeInMillis(unix);
 		date = cal.getTime();
 
 		return new Bookmark(fragmentID, storyID, date);
 	}
 
 	public class StoryDBHelper extends SQLiteOpenHelper {
 
 		public static final int DATABASE_VERSION = 3;
 		public static final String DATABASE_NAME = "adventure.database";
 
 		private static final String TAG = "StoryDBHelper";
 
 		private static final String CREATE_STORY_TABLE =
 				"CREATE TABLE " + STORY_TABLE_NAME + " ("
 				+ _ID + " INTEGER PRIMARY KEY, "
 				+ COLUMN_GUID + " TEXT, "
 				+ STORY_COLUMN_TITLE + " TEXT, "
 				+ STORY_COLUMN_AUTHOR + " TEXT, "
 				+ STORY_COLUMN_SYNOPSIS + " TEXT, "
 				+ STORY_COLUMN_HEAD_FRAGMENT + " INTEGER, "
 				+ STORY_COLUMN_TIMESTAMP + " INTEGER, "
 				+ STORY_COLUMN_THUMBNAIL + " BLOB, "
 				+ "FOREIGN KEY(" + STORY_COLUMN_HEAD_FRAGMENT
 				+ ") REFERENCES " + STORYFRAGMENT_TABLE_NAME
 				+ "(" +  COLUMN_GUID + ") )";
 
 		private static final String CREATE_STORYFRAGMENT_TABLE =
 				"CREATE TABLE " + STORYFRAGMENT_TABLE_NAME + " ("
 				+ _ID + " INTEGER PRIMARY KEY, "
 				+ COLUMN_GUID + " TEXT, "
 				+ STORYFRAGMENT_COLUMN_STORYID + " INTEGER, "
 				+ STORYFRAGMENT_COLUMN_CONTENT + " TEXT, "
 				+ STORYFRAGMENT_COLUMN_CHOICES + " BLOB, "
 				+ "FOREIGN KEY(" + STORYFRAGMENT_COLUMN_STORYID
 				+ ") REFERENCES " + STORY_TABLE_NAME + "("
 				+ COLUMN_GUID + "))";
 
 		private static final String CREATE_BOOKMARK_TABLE =
 				"CREATE TABLE " + BOOKMARK_TABLE_NAME + " ("
 				+ _ID + " INTEGER PRIMARY KEY, "
 				+ BOOKMARK_COLUMN_FRAGMENTID + " INTEGER, "
 				+ BOOKMARK_COLUMN_STORYID + " INTEGER, "
 				+ BOOKMARK_COLUMN_DATE + " INTEGER, "
 				+ "FOREIGN KEY(" + BOOKMARK_COLUMN_FRAGMENTID
 				+ ") REFERENCES " + STORYFRAGMENT_TABLE_NAME
 				+ "(" + COLUMN_GUID + "), FOREIGN KEY (" + BOOKMARK_COLUMN_STORYID
 				+ ") REFERENCES " + STORY_TABLE_NAME + "(" + COLUMN_GUID
 				+ "))";
 
 		private static final String DELETE_STORY_TABLE =
 				"DROP TABLE IF EXISTS " + STORY_TABLE_NAME;
 
 		private static final String DELETE_STORYFRAGMENT_TABLE =
 				"DROP TABLE IF EXISTS " + STORYFRAGMENT_TABLE_NAME;
 
 		private static final String DELETE_BOOKMARK_TABLE =
 				"DROP TABLE IF EXISTS " + BOOKMARK_TABLE_NAME;
 
 		public StoryDBHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			Log.v(TAG, "Creating DB");
 			db.execSQL(CREATE_STORY_TABLE);
 			db.execSQL(CREATE_STORYFRAGMENT_TABLE);
 			db.execSQL(CREATE_BOOKMARK_TABLE);
 			populateDB(db);
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			db.execSQL(DELETE_STORYFRAGMENT_TABLE);
 			db.execSQL(DELETE_STORY_TABLE);
 			db.execSQL(DELETE_BOOKMARK_TABLE);
 		}
 
 		@Override
 		public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			onUpgrade(db, oldVersion, newVersion);
 
 		}
 
 		private void populateDB(SQLiteDatabase db) {
 			Bitmap bit = Bitmap.createBitmap(new int[]{Color.BLACK}, 1, 1, Bitmap.Config.ARGB_8888);
 			Story story = new Story("5582f797-29b8-4d9d-83bf-88c434c1944a", "fc662870-5d6a-4ae2-98f6-0cdfe36013bb",
 					"Andrew", 706232100, "A tale of romance and might",
 					bit, "Super Kewl Story, GUIZ");
 			String storyText = "You wake up. The room is spinning very gently round your head. Or at least it would be "
 					+ "if you could see it which you can't";
 			StoryFragment frag = new StoryFragment(story.getId(), "5582f797-29b8-4d9d-83bf-88c434c1944a", storyText,
 					new ArrayList<String>(), new ArrayList<Choice>());
 			StoryFragment frag2 = new StoryFragment(story.getId(), "b10ef8ca-1180-44f6-b11b-170fef5ec071", "You break" +
 					" your neck in the dark.", new ArrayList<String>(), new ArrayList<Choice>());
 			Choice choice = new Choice("Get out of bed", frag2.getFragmentID());
 			frag.addChoice(choice);
 			story.addFragment(frag);
 			story.addFragment(frag2);
 			Calendar cal = Calendar.getInstance();
 			cal.setTimeInMillis(1383652800 * 1000);
 			Bookmark bookmark = new Bookmark(frag2.getFragmentID(), story.getId(), cal.getTime());
 
 			db.beginTransaction();
 			long inserted = 0;
 			int size = story.getThumbnail().getByteCount();
 			ByteArrayOutputStream blob = new ByteArrayOutputStream();
 			story.getThumbnail().compress(Bitmap.CompressFormat.PNG, 0, blob);
 			byte[] bytes = blob.toByteArray();
 
 			ContentValues values = new ContentValues();
 			values.put(STORY_COLUMN_TITLE, story.getTitle());
 			values.put(STORY_COLUMN_AUTHOR, story.getAuthor());
 			values.put(STORY_COLUMN_HEAD_FRAGMENT, story.getHeadFragmentId());
 			values.put(STORY_COLUMN_SYNOPSIS, story.getSynopsis());
 			values.put(STORY_COLUMN_TIMESTAMP, story.getTimestamp());
 			values.put(STORY_COLUMN_THUMBNAIL, bytes);
 			values.put(COLUMN_GUID, story.getId());
 			inserted = db.insert(STORY_TABLE_NAME, null, values);
 			Log.d(TAG, String.valueOf(inserted));
 			inserted = 0;
 			values = new ContentValues();
 			values.put(STORYFRAGMENT_COLUMN_STORYID, frag.getStoryID());
 			values.put(STORYFRAGMENT_COLUMN_CONTENT, frag.getStoryText());
 			values.put(STORYFRAGMENT_COLUMN_CHOICES, frag.getChoicesInJson());
 			values.put(COLUMN_GUID, frag.getFragmentID());
 			inserted = db.insert(STORYFRAGMENT_TABLE_NAME, null, values);
 			Log.d(TAG, String.valueOf(inserted));
 			inserted = 0;
 			values = new ContentValues();
 			values.put(STORYFRAGMENT_COLUMN_STORYID, frag2.getStoryID());
 			values.put(STORYFRAGMENT_COLUMN_CONTENT, frag2.getStoryText());
 			values.put(STORYFRAGMENT_COLUMN_CHOICES, frag2.getChoicesInJson());
 			values.put(COLUMN_GUID, frag2.getFragmentID());
 			inserted = db.insert(STORYFRAGMENT_TABLE_NAME, null, values);
 			Log.d(TAG, String.valueOf(inserted));
 			inserted = 0;
 			values = new ContentValues();
 			values.put(BOOKMARK_COLUMN_STORYID, bookmark.getStoryID());
 			values.put(BOOKMARK_COLUMN_FRAGMENTID, bookmark.getFragmentID());
 			values.put(BOOKMARK_COLUMN_DATE, bookmark.getDate().getTime() / 1000L);
 			inserted = db.insert(BOOKMARK_TABLE_NAME, null, values);
 			Log.d(TAG, String.valueOf(inserted));
 			db.setTransactionSuccessful();
 			db.endTransaction();
 		}
 	}
 }
