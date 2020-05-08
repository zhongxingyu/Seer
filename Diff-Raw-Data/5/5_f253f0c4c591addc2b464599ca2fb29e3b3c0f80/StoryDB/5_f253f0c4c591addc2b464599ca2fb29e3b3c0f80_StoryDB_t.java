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
 import android.graphics.Color;
 import android.provider.BaseColumns;
 import android.util.Log;
 import ca.cmput301f13t03.adventure_datetime.model.Interfaces.ILocalStorage;
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 
 import java.lang.reflect.Type;
 import java.util.*;
 
 /**
  * The local database containing all stories, fragments, bookmarks, and subscriptions
  *
  * @author Andrew Fontaine
  * @version 1.0
  * @since 28/10/13
  */
 public class StoryDB implements BaseColumns, ILocalStorage {
 
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
     public static final String STORYFRAGMENT_COLUMN_IMAGES = "Images";
 
 	public static final String BOOKMARK_TABLE_NAME = "Bookmark";
 	public static final String BOOKMARK_COLUMN_STORYID = "StoryID";
 	public static final String BOOKMARK_COLUMN_FRAGMENTID = "FragmentID";
 	public static final String BOOKMARK_COLUMN_DATE = "Date";
 
     public static final String AUTHORED_STORY_TABLE_NAME = "AuthoredStory";
 
     public static final String STORY_IMAGE_TABLE_NAME = "StoryImage";
     public static final String STORY_IMAGE_COLUMN_IMAGE = "Image";
 
 	private StoryDBHelper mDbHelper;
 
 	public StoryDB(Context context) {
 		mDbHelper = new StoryDBHelper(context);
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.cmput301f13t03.adventure_datetime.model.ILocalDatabase#getStory(java.util.UUID)
 	 */
 	@Override
 	public Story getStory(UUID id) {
 		SQLiteDatabase db = mDbHelper.getReadableDatabase();
 
 		Cursor cursor = db.query(STORY_TABLE_NAME,
 				new String[]{_ID, COLUMN_GUID, STORY_COLUMN_TITLE, STORY_COLUMN_AUTHOR, STORY_COLUMN_TITLE,
 						STORY_COLUMN_HEAD_FRAGMENT, STORY_COLUMN_SYNOPSIS, STORY_COLUMN_TIMESTAMP, STORY_COLUMN_THUMBNAIL},
 				COLUMN_GUID + " = ?",
 				new String[] {id.toString()},
 				null,
 				null,
 				null,
 				"1");
 
 		Story story;
 
 		if (cursor.moveToFirst()) {
             Log.v(TAG, "Story with UUID " + id + " retrieved");
 			story = createStory(cursor);
 		}
 		else {
 			story = null;
             Log.v(TAG, "No story found");
         }
 
 		cursor.close();
 		db.close();
 		return story;
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.cmput301f13t03.adventure_datetime.model.ILocalDatabase#getStories()
 	 */
 	@Override
 	public ArrayList<Story> getStories() {
 		SQLiteDatabase db = mDbHelper.getReadableDatabase();
 
 		Cursor cursor = db.query(STORY_TABLE_NAME,
 				new String[] {_ID, COLUMN_GUID, STORY_COLUMN_AUTHOR, STORY_COLUMN_TITLE,
 						STORY_COLUMN_HEAD_FRAGMENT, STORY_COLUMN_SYNOPSIS, STORY_COLUMN_TIMESTAMP, STORY_COLUMN_THUMBNAIL},
 				null,
 				null,
 				null,
 				null,
 				null);
 		ArrayList<Story> stories = new ArrayList<Story>();
 
 		if(cursor.moveToFirst()) {
 			do {
                 Log.v(TAG, "Story with id " + cursor.getString(cursor.getColumnIndex(COLUMN_GUID)) + " retrieved");
 				stories.add(createStory(cursor));
 			} while(cursor.moveToNext());
 		}
 
         Log.v(TAG, stories.size() + " stories retrieved");
 
 		cursor.close();
 		db.close();
 		return stories;
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.cmput301f13t03.adventure_datetime.model.ILocalDatabase#getStoriesAuthoredBy(java.lang.String)
 	 */
 	@Override
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
 
         if(cursor.moveToFirst()) {
             do {
                 Log.v(TAG, "Story with id " + cursor.getString(cursor.getColumnIndex(COLUMN_GUID)) + " retrieved");
                 stories.add(createStory(cursor));
             } while(cursor.moveToNext());
         }
 
         Log.v(TAG, stories.size() + " stories retrieved");
 
 		cursor.close();
 		db.close();
 		return stories;
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.cmput301f13t03.adventure_datetime.model.ILocalDatabase#getStoryFragment(java.util.UUID)
 	 */
 	@Override
 	public StoryFragment getStoryFragment(UUID id) {
 		SQLiteDatabase db = mDbHelper.getReadableDatabase();
 
 		Cursor cursor = db.query(STORYFRAGMENT_TABLE_NAME,
 				new String[] {_ID, COLUMN_GUID, STORYFRAGMENT_COLUMN_STORYID, STORYFRAGMENT_COLUMN_CHOICES, STORYFRAGMENT_COLUMN_CONTENT, STORYFRAGMENT_COLUMN_IMAGES},
 				COLUMN_GUID + " = ?",
 				new String[] {id.toString()},
 				null,
 				null,
 				"1");
 
 		StoryFragment frag;
 		if(cursor.moveToFirst()) {
             Log.v(TAG, "StoryFragment " + id + " retrieved");
 			frag = createStoryFragment(cursor);
         }
 		else {
 			frag = null;
             Log.v(TAG, "No fragment found");
         }
 		cursor.close();
 		db.close();
 		return frag;
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.cmput301f13t03.adventure_datetime.model.ILocalDatabase#getStoryFragments(java.util.UUID)
 	 */
 	@Override
 	public ArrayList<StoryFragment> getStoryFragments(UUID storyid) {
 		SQLiteDatabase db = mDbHelper.getReadableDatabase();
 
 		Cursor cursor = db.query(STORYFRAGMENT_TABLE_NAME,
 				new String[]{_ID, COLUMN_GUID, STORYFRAGMENT_COLUMN_STORYID, STORYFRAGMENT_COLUMN_CHOICES, STORYFRAGMENT_COLUMN_CONTENT, STORYFRAGMENT_COLUMN_IMAGES},
 				STORYFRAGMENT_COLUMN_STORYID + " = ?",
 				new String[]{storyid.toString()},
 				null,
 				null,
 				null);
 
 		ArrayList<StoryFragment> fragments = new ArrayList<StoryFragment>();
 
 		if(cursor.moveToFirst()) {
 			do {
                 Log.v(TAG, "StoryFragment with id " + cursor.getString(cursor.getColumnIndex(COLUMN_GUID))
                         + " retrieved");
 				fragments.add(createStoryFragment(cursor));
 			} while(cursor.moveToNext());
 		}
 
         Log.v(TAG, fragments.size() + " StoryFragments retrieved");
 
 		cursor.close();
 		db.close();
 		return fragments;
 	}
 
     private ArrayList<UUID> getStoryFragmentIDs(UUID storyID) {
         SQLiteDatabase db = mDbHelper.getReadableDatabase();
 
         Cursor cursor = db.query(STORYFRAGMENT_TABLE_NAME,
                 new String[]{COLUMN_GUID},
                 STORYFRAGMENT_COLUMN_STORYID + " = ?",
                 new String[]{storyID.toString()},
                 null,
                 null,
                 null);
 
         ArrayList<UUID> fragmentIDs = new ArrayList<UUID>();
 
         if(cursor.moveToFirst()) {
             do {
                 Log.v(TAG, "StoryFragment with id " + cursor.getString(cursor.getColumnIndex(COLUMN_GUID))
                         + " retrieved");
                 fragmentIDs.add(UUID.fromString(cursor.getString(cursor.getColumnIndex(COLUMN_GUID))));
             } while(cursor.moveToNext());
         }
 
         Log.v(TAG, fragmentIDs.size() + " StoryFragments retrieved");
 
         cursor.close();
         db.close();
         return fragmentIDs;
     }
 
 	/* (non-Javadoc)
 	 * @see ca.cmput301f13t03.adventure_datetime.model.ILocalDatabase#getBookmark(java.util.UUID)
 	 */
 	@Override
 	public Bookmark getBookmark(UUID storyid) {
 		SQLiteDatabase db = mDbHelper.getReadableDatabase();
 
 		Cursor cursor = db.query(BOOKMARK_TABLE_NAME,
 				new String[] {_ID, BOOKMARK_COLUMN_STORYID, BOOKMARK_COLUMN_FRAGMENTID, BOOKMARK_COLUMN_DATE},
 				BOOKMARK_COLUMN_STORYID + " = ?",
 				new String[] {storyid.toString()},
 				null,
 				null,
 				null);
 
 		Bookmark bookmark;
 
 		if(cursor.moveToFirst()) {
            Log.v(TAG, "Bookmark with Story id " + cursor.getString(cursor.getColumnIndex(BOOKMARK_COLUMN_STORYID)) +
            "retrieved");
 			bookmark = createBookmark(cursor);
         }
 		else {
             Log.v(TAG, "No bookmark found");
 			bookmark = null;
         }
 
 		cursor.close();
 		db.close();
 		return bookmark;
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.cmput301f13t03.adventure_datetime.model.ILocalDatabase#getAllBookmarks()
 	 */
 	@Override
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
                 Log.v(TAG, "Bookmark with Story id " + cursor.getString(cursor.getColumnIndex(BOOKMARK_COLUMN_STORYID))
                         + " retrieved");
 				bookmarks.add(createBookmark(cursor));
 			} while(cursor.moveToNext());
 		}
 
         Log.v(TAG, bookmarks.size() + " bookmarks retrieved");
 
 		return bookmarks;
 	}
 
     @Override
     public boolean getAuthoredStory(UUID storyId) {
         SQLiteDatabase db = mDbHelper.getReadableDatabase();
         Cursor cursor = db.query(AUTHORED_STORY_TABLE_NAME,
                 new String[] {COLUMN_GUID},
                 COLUMN_GUID + " = ?",
                 new String[] {storyId.toString()},
                 null,
                 null,
                 null);
 
         boolean authoredStory = cursor.moveToFirst();
         cursor.close();
         db.close();
 
         return authoredStory;
     }
 
     @Override
     public ArrayList<UUID> getAuthoredStories() {
         SQLiteDatabase db = mDbHelper.getReadableDatabase();
         Cursor cursor = db.query(AUTHORED_STORY_TABLE_NAME,
                 new String[] {COLUMN_GUID},
                 null,
                 null,
                 null,
                 null,
                 null);
 
         ArrayList<UUID> authoredStories = new ArrayList<UUID>();
 
         if(cursor.moveToFirst()) {
             do {
                 authoredStories.add(UUID.fromString(cursor.getString(cursor.getColumnIndex(COLUMN_GUID))));
             } while(cursor.moveToNext());
         }
         cursor.close();
         db.close();
 
         return authoredStories;
     }
 
     public Image getImage(UUID imageID) {
         Cursor cursor = getImageCursor(imageID);
 
         Image image;
         if(cursor.moveToFirst()) {
             image = createImage(cursor);
         }
         else {
             image = null;
         }
 
         cursor.close();
         return image;
     }
 
     private Cursor getImageCursor(UUID imageID) {
         SQLiteDatabase db = mDbHelper.getReadableDatabase();
         return db.query(STORY_IMAGE_TABLE_NAME,
                 new String[] {COLUMN_GUID, STORY_IMAGE_COLUMN_IMAGE},
                 COLUMN_GUID + " = ?",
                 new String[] {imageID.toString()},
                 null,
                 null,
                 null);
     }
 
     public ArrayList<Image> getImages(ArrayList<UUID> imageIDs) {
         ArrayList<Image> images = new ArrayList<Image>();
         if(imageIDs == null)
             return images;
         Cursor cursor;
 
         for(UUID imageID : imageIDs) {
             cursor = getImageCursor(imageID);
             if(cursor.moveToFirst())
                 images.add(createImage(cursor));
             cursor.close();
         }
 
         return images;
     }
 
 	/* (non-Javadoc)
 	 * @see ca.cmput301f13t03.adventure_datetime.model.ILocalDatabase#setBookmark(ca.cmput301f13t03.adventure_datetime.model.Bookmark)
 	 */
 	@Override
 	public boolean setBookmark(Bookmark bookmark) {
 		SQLiteDatabase db = mDbHelper.getWritableDatabase();
 
 		Cursor cursor = db.query(BOOKMARK_TABLE_NAME,
 				new String[] {_ID, BOOKMARK_COLUMN_FRAGMENTID, BOOKMARK_COLUMN_STORYID, BOOKMARK_COLUMN_DATE},
 				BOOKMARK_COLUMN_STORYID + " = ?",
 				new String[] {bookmark.getStoryID().toString()},
 				null,
 				null,
 				null);
 
 		ContentValues values = new ContentValues();
 
 		values.put(BOOKMARK_COLUMN_STORYID, bookmark.getStoryID().toString());
 		values.put(BOOKMARK_COLUMN_FRAGMENTID, bookmark.getFragmentID().toString());
 		values.put(BOOKMARK_COLUMN_DATE, bookmark.getTimestamp() / 1000);
 
 		long updated;
 		if(cursor.moveToFirst()) {
 			Bookmark bookmark1 = createBookmark(cursor);
 			if(bookmark.getTimestamp() > bookmark1.getTimestamp()) {
 				updated = db.update(BOOKMARK_TABLE_NAME,values,BOOKMARK_COLUMN_STORYID + " = ?",
 						new String[] {BOOKMARK_COLUMN_STORYID});
                 Log.v(TAG, updated + " Bookmarks updated");
 				cursor.close();
 				db.close();
 				return updated == 1;
 			}
             Log.v(TAG, "No Bookmarks updated");
 			cursor.close();
 			db.close();
 			return false;
 		}
 		updated = db.insert(BOOKMARK_TABLE_NAME, null, values);
         Log.v(TAG, updated + " Bookmark inserted");
 		cursor.close();
 		db.close();
 		return updated != -1;
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.cmput301f13t03.adventure_datetime.model.ILocalDatabase#setStory(ca.cmput301f13t03.adventure_datetime.model.Story)
 	 */
 	@Override
 	public boolean setStory(Story story) {
 
 		SQLiteDatabase db = mDbHelper.getWritableDatabase();
 		ContentValues values = new ContentValues();
 		values.put(STORY_COLUMN_TITLE, story.getTitle());
 		values.put(STORY_COLUMN_AUTHOR, story.getAuthor());
 		values.put(STORY_COLUMN_HEAD_FRAGMENT, story.getHeadFragmentId().toString());
 		values.put(STORY_COLUMN_SYNOPSIS, story.getSynopsis());
 		values.put(STORY_COLUMN_TIMESTAMP, story.getTimestamp());
 		values.put(STORY_COLUMN_THUMBNAIL, (story.getThumbnail() == null ? null : story.getThumbnail().getId().toString()));
 		values.put(COLUMN_GUID, story.getId().toString());
 
 		Cursor cursor = db.query(STORY_TABLE_NAME,
 				new String[] {_ID},
 				COLUMN_GUID + " = ?",
 				new String[] {story.getId().toString()},
 				null,
 				null,
 				null);
 		if(cursor.moveToFirst()) {
 			int updated;
 			updated = db.update(STORY_TABLE_NAME, values, COLUMN_GUID + " = ?",
 					new String [] {story.getId().toString()});
             Log.v(TAG, updated + " stories updated");
 			cursor.close();
 			db.close();
 			return updated == 1 && setImage(story.getThumbnail());
 		}
 		cursor.close();
 		long inserted;
 		inserted = db.insert(STORY_TABLE_NAME, null, values);
         Log.v(TAG, inserted + " story inserted");
 		db.close();
 		return inserted != -1 && setImage(story.getThumbnail());
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.cmput301f13t03.adventure_datetime.model.ILocalDatabase#setStoryFragment(ca.cmput301f13t03.adventure_datetime.model.StoryFragment)
 	 */
 	@Override
 	public boolean setStoryFragment(StoryFragment frag) {
 		SQLiteDatabase db = mDbHelper.getWritableDatabase();
 		ContentValues values = new ContentValues();
 		values.put(STORYFRAGMENT_COLUMN_STORYID, frag.getStoryID().toString());
 		values.put(STORYFRAGMENT_COLUMN_CONTENT, frag.getStoryText());
 		values.put(STORYFRAGMENT_COLUMN_CHOICES, frag.getChoicesInJson());
 		values.put(COLUMN_GUID, frag.getFragmentID().toString());
         values.put(STORYFRAGMENT_COLUMN_IMAGES, frag.getStoryMediaInJson());
 
 		Cursor cursor = db.query(STORYFRAGMENT_TABLE_NAME,
 				new String[] {_ID, STORYFRAGMENT_COLUMN_STORYID},
 				COLUMN_GUID + " = ? AND " + STORYFRAGMENT_COLUMN_STORYID + " = ?",
 				new String[] {frag.getFragmentID().toString(), frag.getStoryID().toString()},
 				null,
 				null,
 				null);
 		if(cursor.moveToFirst()) {
 			int updated;
 			updated = db.update(STORYFRAGMENT_TABLE_NAME, values, COLUMN_GUID + " = ? AND " + STORYFRAGMENT_COLUMN_STORYID + " = ?",
 					new String[] {frag.getFragmentID().toString(), frag.getStoryID().toString()});
             Log.v(TAG, updated + " fragments updated");
 			cursor.close();
 			db.close();
 			return updated == 1 && setImages(frag.getStoryMedia());
 		}
 		long inserted;
 		inserted = db.insert(STORYFRAGMENT_TABLE_NAME, null, values);
         Log.v(TAG, inserted + " fragment inserted");
 		db.close();
 		return inserted != -1 && setImages(frag.getStoryMedia());
 
 	}
 
     @Override
     public boolean setAuthoredStory(Story story) {
         Story story2 = getStory(story.getId());
 
         if(story2 == null) {
             Log.v(TAG, "Story doesn't exist in local DB!");
             return false;
         }
 
         if(getAuthoredStory(story.getId())) {
             Log.v(TAG, "Story already in AuthoredStory table");
             return true;
         }
 
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

         ContentValues values = new ContentValues();
 
         values.put(COLUMN_GUID, story.getId().toString());
 
         long insert = db.insert(AUTHORED_STORY_TABLE_NAME, null, values);
 
         db.close();
 
         return insert != -1;
     }
 
     public boolean setImage(Image image) {
         SQLiteDatabase db = mDbHelper.getWritableDatabase();
         Image image2 = getImage(image.getId());
         ContentValues values = new ContentValues();
         values.put(COLUMN_GUID, image.getId().toString());
         values.put(STORY_IMAGE_COLUMN_IMAGE, image.getEncodedBitmap());
         long inserted;
         if(image2 == null) {
             inserted = db.insert(STORY_IMAGE_TABLE_NAME, null, values);
             db.close();
             return inserted != -1;
         }
         else {
             inserted = db.update(STORY_IMAGE_TABLE_NAME, values, COLUMN_GUID + " = ?",
                     new String[] {image.getId().toString()});
             db.close();
             return inserted == 1;
         }
     }
 
     public boolean setImages(ArrayList<Image> images) {
         boolean result = true;
         for(Image image : images) {
             result &= setImage(image);
         }
 
         return result;
     }
 
 	/* (non-Javadoc)
 	 * @see ca.cmput301f13t03.adventure_datetime.model.ILocalDatabase#deleteStory(java.util.UUID)
 	 */
     public boolean deleteStory(UUID id) {
         boolean fragments;
         fragments = deleteStoryFragments(id);
         deleteBookmarkByStory(id);
         deleteAuthoredStory(id);
         SQLiteDatabase db = mDbHelper.getWritableDatabase();
         int story;
         story = db.delete(STORY_TABLE_NAME, COLUMN_GUID + " = ?", new String[] {id.toString()});
         Log.v(TAG, story + " deleted, had UUID " + id);
         db.close();
         return story == 1 && fragments;
     }
 
 	/* (non-Javadoc)
 	 * @see ca.cmput301f13t03.adventure_datetime.model.ILocalDatabase#deleteStoryFragments(java.util.UUID)
 	 */
     public boolean deleteStoryFragments(UUID storyID) {
         int fragments;
         deleteBookmarkByStory(storyID);
         SQLiteDatabase db = mDbHelper.getWritableDatabase();
         fragments = db.delete(STORYFRAGMENT_TABLE_NAME, STORYFRAGMENT_COLUMN_STORYID + " = ?", new String[] {storyID.toString()});
         Log.v(TAG, fragments + " deleted from DB, all with StoryID " + storyID);
         db.close();
 
         return fragments > 0;
     }
 
     /* (non-Javadoc)
 	 * @see ca.cmput301f13t03.adventure_datetime.model.ILocalDatabase#deleteStoryFragment(java.util.UUID)
 	 */
     public boolean deleteStoryFragment(UUID fragmentID) {
         int fragment;
         deleteBookmarkByFragment(fragmentID);
         SQLiteDatabase db = mDbHelper.getWritableDatabase();
         fragment = db.delete(STORYFRAGMENT_TABLE_NAME, COLUMN_GUID + " = ?", new String[] {fragmentID.toString()});
         Log.v(TAG, fragment + " fragment deleted, with fragmentID " + fragmentID);
         db.close();
 
         return fragment == 1;
     }
 
     /* (non-Javadoc)
 	 * @see ca.cmput301f13t03.adventure_datetime.model.ILocalDatabase#deleteBookmarkByStory(java.util.UUID)
 	 */
     public boolean deleteBookmarkByStory(UUID storyID) {
         int bookmark;
         SQLiteDatabase db = mDbHelper.getWritableDatabase();
         bookmark = db.delete(BOOKMARK_TABLE_NAME, BOOKMARK_COLUMN_STORYID + " = ?", new String[] {storyID.toString()});
         Log.v(TAG, bookmark + " bookmark deleted, with storyID " + storyID);
         db.close();
         return bookmark == 1;
     }
 
     /* (non-Javadoc)
 	 * @see ca.cmput301f13t03.adventure_datetime.model.ILocalDatabase#deleteBookmarkByFragment(java.util.UUID)
 	 */
     public boolean deleteBookmarkByFragment(UUID fragmentID) {
         int bookmark;
         SQLiteDatabase db = mDbHelper.getWritableDatabase();
         bookmark = db.delete(BOOKMARK_TABLE_NAME, BOOKMARK_COLUMN_FRAGMENTID + " = ?", new String[] {fragmentID.toString()});
         Log.v(TAG, bookmark + " bookmark deleted, with fragmentID " + fragmentID);
         db.close();
         return bookmark == 1;
     }
 
     @Override
     public boolean deleteAuthoredStory(UUID storyID) {
         int authoredStory;
         SQLiteDatabase db = mDbHelper.getWritableDatabase();
         authoredStory = db.delete(AUTHORED_STORY_TABLE_NAME, COLUMN_GUID + " = ?", new String[] {storyID.toString()});
         Log.v(TAG, authoredStory + " authored story deleted, with storyId " + storyID);
         db.close();
         return authoredStory == 1;
     }
 
     public boolean deleteImages(UUID id) {
         int images;
         SQLiteDatabase db = mDbHelper.getWritableDatabase();
         images = db.delete(STORY_IMAGE_TABLE_NAME, COLUMN_GUID + " = ?", new String[] {id.toString()});
         Log.v(TAG, images + " images deleted, with UUID " + id);
 
         return images != 0;
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
 		UUID headFragmentId, id;
 		long timestamp;
         Image thumbnail;
 
 		id = UUID.fromString(cursor.getString(cursor.getColumnIndex(StoryDB.COLUMN_GUID)));
 		title = cursor.getString(cursor.getColumnIndex(StoryDB.STORY_COLUMN_TITLE));
 		headFragmentId = UUID.fromString(cursor.getString(cursor.getColumnIndex(StoryDB.STORY_COLUMN_HEAD_FRAGMENT)));
 		author = cursor.getString(cursor.getColumnIndex(StoryDB.STORY_COLUMN_AUTHOR));
 		synopsis = cursor.getString(cursor.getColumnIndex(StoryDB.STORY_COLUMN_SYNOPSIS));
 		timestamp = cursor.getLong(cursor.getColumnIndex(StoryDB.STORY_COLUMN_TIMESTAMP));
 
         thumbnail = getImage(id);
 		
 		Story newStory = new Story(headFragmentId, id, author, timestamp, synopsis, thumbnail, title);
 		
 		ArrayList<UUID> referencedFragments = this.getStoryFragmentIDs(id);
 		
 		for(UUID frag : referencedFragments)
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
 		UUID storyID, fragmentID;
 		String storyText;
 		ArrayList<Choice> choices;
         ArrayList<Image> images;
         ArrayList<UUID> uuids;
 		storyID = UUID.fromString(cursor.getString(cursor.getColumnIndex(StoryDB.STORYFRAGMENT_COLUMN_STORYID)));
 		fragmentID = UUID.fromString(cursor.getString(cursor.getColumnIndex(StoryDB.COLUMN_GUID)));
 		storyText = cursor.getString(cursor.getColumnIndex(StoryDB.STORYFRAGMENT_COLUMN_CONTENT));
 		String json = cursor.getString(cursor.getColumnIndex(StoryDB.STORYFRAGMENT_COLUMN_CHOICES));
 		Gson gson = new Gson();
 		Type collectionType = new TypeToken<Collection<Choice>>(){}.getType();
 		choices = gson.fromJson(json, collectionType);
         json = cursor.getString(cursor.getColumnIndex(STORYFRAGMENT_COLUMN_IMAGES));
         collectionType = new TypeToken<Collection<UUID>>(){}.getType();
         uuids = gson.fromJson(json, collectionType);
         images = getImages(uuids);
 
 		return new StoryFragment(storyID, fragmentID, storyText, images, choices);
 	}
 
 	/**
 	 * Creates a Bookmark from a cursor
 	 *
 	 * @param cursor A Cursor pointing to a Bookmark
 	 *
 	 * @return A Bookmark instance from the Database
 	 */
 	private Bookmark createBookmark(Cursor cursor) {
 		UUID fragmentID, storyID;
 		Date date;
 
 		fragmentID = UUID.fromString(cursor.getString(cursor.getColumnIndex(StoryDB.BOOKMARK_COLUMN_FRAGMENTID)));
 		storyID = UUID.fromString(cursor.getString(cursor.getColumnIndex(StoryDB.BOOKMARK_COLUMN_STORYID)));
 		long unix = cursor.getLong(cursor.getColumnIndex(StoryDB.BOOKMARK_COLUMN_DATE));
 		Calendar cal = Calendar.getInstance();
 		cal.setTimeInMillis(unix);
 		date = cal.getTime();
 
 		return new Bookmark(fragmentID, storyID, date);
 	}
 
     private Image createImage(Cursor cursor) {
         UUID id;
         String bitmap;
         id = UUID.fromString(cursor.getString(cursor.getColumnIndex(COLUMN_GUID)));
         bitmap = cursor.getString(cursor.getColumnIndex(STORY_IMAGE_COLUMN_IMAGE));
 
         return new Image(id, bitmap);
     }
 
 	public class StoryDBHelper extends SQLiteOpenHelper {
 
 		public static final int DATABASE_VERSION = 6;
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
 				+ STORY_COLUMN_THUMBNAIL + " TEXT, "
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
                 + STORYFRAGMENT_COLUMN_IMAGES + " BLOB, "
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
 
         private static final String CREATE_AUTHORED_STORY_TABLE =
                 "CREATE TABLE " + AUTHORED_STORY_TABLE_NAME + " ("
                 + _ID + " INTEGER PRIMARY KEY, "
                 + COLUMN_GUID + " TEXT, "
                 + "FOREIGN KEY(" + COLUMN_GUID + ") REFERENCES "
                 + STORY_TABLE_NAME + "(" + COLUMN_GUID + "))";
 
         private static final String CREATE_STORY_IMAGE_TABLE =
                 "CREATE TABLE " + STORY_IMAGE_TABLE_NAME + " ("
                 + _ID + " INTEGER PRIMARY KEY, "
                 + COLUMN_GUID + " TEXT, "
                 + STORY_IMAGE_COLUMN_IMAGE + " TEXT)";
 
 		private static final String DELETE_STORY_TABLE =
 				"DROP TABLE IF EXISTS " + STORY_TABLE_NAME;
 
 		private static final String DELETE_STORYFRAGMENT_TABLE =
 				"DROP TABLE IF EXISTS " + STORYFRAGMENT_TABLE_NAME;
 
 		private static final String DELETE_BOOKMARK_TABLE =
 				"DROP TABLE IF EXISTS " + BOOKMARK_TABLE_NAME;
 
         private static final String DELETE_AUTHORED_STORY_TABLE =
                 "DROP TABLE IF EXISTS " + AUTHORED_STORY_TABLE_NAME;
 
         private static final String DELETE_STORY_IMAGE_TABLE =
                 "DROP TABLE IF EXISTS " + STORY_IMAGE_TABLE_NAME;
 
 		public StoryDBHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			Log.v(TAG, "Creating DB");
 			db.execSQL(CREATE_STORY_TABLE);
 			db.execSQL(CREATE_STORYFRAGMENT_TABLE);
 			db.execSQL(CREATE_BOOKMARK_TABLE);
             db.execSQL(CREATE_AUTHORED_STORY_TABLE);
             db.execSQL(CREATE_STORY_IMAGE_TABLE);
 			populateDB(db);
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			db.execSQL(DELETE_STORYFRAGMENT_TABLE);
 			db.execSQL(DELETE_STORY_TABLE);
 			db.execSQL(DELETE_BOOKMARK_TABLE);
             db.execSQL(DELETE_AUTHORED_STORY_TABLE);
             db.execSQL(DELETE_STORY_IMAGE_TABLE);
 		}
 
 		@Override
 		public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			onUpgrade(db, oldVersion, newVersion);
 
 		}
 
 		private void populateDB(SQLiteDatabase db) {
 			Bitmap bit = Bitmap.createBitmap(new int[]{Color.BLACK}, 1, 1, Bitmap.Config.ARGB_8888);
 			Story story = new Story(UUID.fromString("5582f797-29b8-4d9d-83bf-88c434c1944a"), UUID.fromString("fc662870-5d6a-4ae2-98f6-0cdfe36013bb"),
 					"Andrew", 706232100, "A tale of romance and might",
 					bit, "Super Kewl Story, GUIZ");
 			String storyText = "You wake up. The room is spinning very gently round your head. Or at least it would be "
 					+ "if you could see it which you can't";
 			StoryFragment frag = new StoryFragment(story.getId(), UUID.fromString("5582f797-29b8-4d9d-83bf-88c434c1944a"), storyText,
 					new ArrayList<Image>(), new ArrayList<Choice>());
 			StoryFragment frag2 = new StoryFragment(story.getId(), UUID.fromString("b10ef8ca-1180-44f6-b11b-170fef5ec071"), "You break" +
 					" your neck in the dark.", new ArrayList<Image>(), new ArrayList<Choice>());
 			Choice choice = new Choice("Get out of bed", frag2.getFragmentID());
 			frag.addChoice(choice);
 			story.addFragment(frag);
 			story.addFragment(frag2);
 			Calendar cal = Calendar.getInstance();
 			cal.setTimeInMillis(1383652800L * 1000L);
 			Bookmark bookmark = new Bookmark(frag2.getFragmentID(), story.getId(), cal.getTime());
 
 			db.beginTransaction();
 			long inserted;
 
 			ContentValues values = new ContentValues();
 			values.put(STORY_COLUMN_TITLE, story.getTitle());
 			values.put(STORY_COLUMN_AUTHOR, story.getAuthor());
 			values.put(STORY_COLUMN_HEAD_FRAGMENT, story.getHeadFragmentId().toString());
 			values.put(STORY_COLUMN_SYNOPSIS, story.getSynopsis());
 			values.put(STORY_COLUMN_TIMESTAMP, story.getTimestamp());
 			values.put(STORY_COLUMN_THUMBNAIL, story.getThumbnail().getId().toString());
 			values.put(COLUMN_GUID, story.getId().toString());
 			inserted = db.insert(STORY_TABLE_NAME, null, values);
             Log.d(TAG, String.valueOf(inserted));
             values = new ContentValues();
             values.put(COLUMN_GUID, story.getThumbnail().getId().toString());
             values.put(STORY_IMAGE_COLUMN_IMAGE, story.getThumbnail().getEncodedBitmap());
             inserted = db.insert(STORY_IMAGE_TABLE_NAME, null, values);
 			Log.d(TAG, String.valueOf(inserted));
 			values = new ContentValues();
 			values.put(STORYFRAGMENT_COLUMN_STORYID, frag.getStoryID().toString());
 			values.put(STORYFRAGMENT_COLUMN_CONTENT, frag.getStoryText());
 			values.put(STORYFRAGMENT_COLUMN_CHOICES, frag.getChoicesInJson());
 			values.put(COLUMN_GUID, frag.getFragmentID().toString());
 			inserted = db.insert(STORYFRAGMENT_TABLE_NAME, null, values);
 			Log.d(TAG, String.valueOf(inserted));
 			values = new ContentValues();
 			values.put(STORYFRAGMENT_COLUMN_STORYID, frag2.getStoryID().toString());
 			values.put(STORYFRAGMENT_COLUMN_CONTENT, frag2.getStoryText());
 			values.put(STORYFRAGMENT_COLUMN_CHOICES, frag2.getChoicesInJson());
 			values.put(COLUMN_GUID, frag2.getFragmentID().toString());
 			inserted = db.insert(STORYFRAGMENT_TABLE_NAME, null, values);
 			Log.d(TAG, String.valueOf(inserted));
 			values = new ContentValues();
 			values.put(BOOKMARK_COLUMN_STORYID, bookmark.getStoryID().toString());
 			values.put(BOOKMARK_COLUMN_FRAGMENTID, bookmark.getFragmentID().toString());
 			values.put(BOOKMARK_COLUMN_DATE, bookmark.getTimestamp() / 1000L);
 			inserted = db.insert(BOOKMARK_TABLE_NAME, null, values);
 			Log.d(TAG, String.valueOf(inserted));
             values = new ContentValues();
             values.put(COLUMN_GUID, story.getId().toString());
             inserted = db.insert(AUTHORED_STORY_TABLE_NAME, null, values);
             Log.d(TAG, String.valueOf(inserted));
 			db.setTransactionSuccessful();
 			db.endTransaction();
 		}
 	}
 }
