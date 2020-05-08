 package com.chalmers.feedlr.database;
 
 import java.util.ArrayList;
 
 import com.chalmers.feedlr.model.Feed;
 import com.chalmers.feedlr.model.TwitterItem;
 import com.chalmers.feedlr.model.User;
 
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.database.sqlite.SQLiteDatabase;
 import android.test.AndroidTestCase;
 
 public class DatabaseHelperTest extends AndroidTestCase {
 
 	DatabaseHelper dbHelper;
 	SQLiteDatabase db;
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		getContext().deleteDatabase("feedlrDatabase");
 		dbHelper = new DatabaseHelper(getContext());
 		db = dbHelper.getWritableDatabase();
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	public void testAddFeed() {
 		// Testing if the feed gets added to the table.
 		String feedName = "testFeed";
 		Feed f1 = new Feed(feedName);
 		dbHelper.addFeed(f1);
 		long feedTableSize = DatabaseUtils.queryNumEntries(db,
 				DatabaseHelper.TABLE_FEED);
 		assertTrue(feedTableSize == 1);
 
 		// Checking the name if the name is saved correctly.
 		Cursor c = db
 				.query(DatabaseHelper.TABLE_FEED,
 						new String[] { DatabaseHelper.FEED_COLUMN_NAME },
 						DatabaseHelper.FEED_COLUMN_ID + " = ?",
 						new String[] { dbHelper.getFeed_id(f1) + "" }, null,
 						null, null);
 		c.moveToFirst();
 		assertTrue(c.getString(0).equals(feedName));
 
 		// Checking if adding the same feed again is possible.
 		assertFalse(dbHelper.addFeed(new Feed(feedName)));
 	}
 
 	public void testRemoveFeed() {
 		String feedName = "testFeed";
 		Feed f1 = new Feed(feedName);
 		dbHelper.addFeed(f1);
 		assertTrue(dbHelper.removeFeed(f1));
 
 		// Checks that no feeds are left.
 		long feedTableSize = DatabaseUtils.queryNumEntries(db,
 				DatabaseHelper.TABLE_FEED);
 		assertTrue(feedTableSize == 0);
 
 		// Trying to remove a feed that does not exist.
 		assertFalse(dbHelper.removeFeed(f1));
 	}
 
 	public void testClearFeeds() {
 		String feedName = "testFeed";
 		Feed f1 = new Feed(feedName);
 		dbHelper.addFeed(f1);
 		dbHelper.clearFeeds();
 		assertTrue(DatabaseUtils.queryNumEntries(db, DatabaseHelper.TABLE_FEED) == 0);
 	}
 
 	public void getFeedTableSize() {
 		Feed f1 = new Feed("feed1");
 		Feed f2 = new Feed("feed2");
 		Feed f3 = new Feed("feed3");
 		dbHelper.addFeed(f1);
 		dbHelper.addFeed(f2);
 		assertTrue(DatabaseUtils.queryNumEntries(db, DatabaseHelper.TABLE_FEED) == dbHelper.getFeedTableSize());
 		dbHelper.addFeed(f3);
 		assertTrue(DatabaseUtils.queryNumEntries(db, DatabaseHelper.TABLE_FEED) == dbHelper.getFeedTableSize());
 		
 		// Testing so remove feed affects tablesize aswell.
 		dbHelper.removeFeed(f1);
 		assertTrue(DatabaseUtils.queryNumEntries(db, DatabaseHelper.TABLE_FEED) == dbHelper.getFeedTableSize());
 	}
 
 	public void testListFeeds() {
 		dbHelper.addFeed(new Feed("Feed1"));
 		dbHelper.addFeed(new Feed("Feed2"));
 		ArrayList<String> feeds = dbHelper.listFeeds();
 		assertTrue(feeds.get(0).equals("Feed1") && feeds.get(1).equals("Feed2"));
 	}
 
 	public void testAddUser() {
 		String username = "Olle";
 		String id = "507";
 		String URL = "pictureurl";
 		String source = "twitter";
 
 		User u1 = new User(id, username);
 		u1.setProfileImageURL(URL);
 		u1.setSource(source);
 
 		dbHelper.addUser(u1);
 
 		Cursor c = db.query(DatabaseHelper.TABLE_USER, null,
 				DatabaseHelper.USER_COLUMN_USERID + " = ?", new String[] { id
 						+ "" }, null, null, null);
 		c.moveToFirst();
 
 		String dbUsername = c.getString(1);
 		String dbId = c.getString(2);
 		String dbURL = c.getString(3);
 		String dbSource = c.getString(4);
 
 		assertTrue(dbUsername.equals(username));
 		assertTrue(dbId.equals(id + ""));
 		assertTrue(dbURL.equals(URL));
 		assertTrue(dbSource.equals(source));
 
 		// Testing to update a user;
 		String newUsername = "Daniel";
 		u1.setUserName(newUsername);
 		dbHelper.addUser(u1);
 
 		c = db.query(DatabaseHelper.TABLE_USER, null,
 				DatabaseHelper.USER_COLUMN_USERID + " = ?", new String[] { id
 						+ "" }, null, null, null);
 		c.moveToFirst();
 
 		assertTrue(c.getString(1).equals(newUsername));
 
 		// Trying to addUser with no id or username
 		assertFalse(dbHelper.addUser(new User(null, null)));
 	}
 
 	public void testRemoveUser() {
 		User u1 = new User("507", "Daniel");
 		u1.setSource("twitter");
 		dbHelper.addUser(u1);
 		dbHelper.removeUser(u1);
 		Cursor c = db.query(DatabaseHelper.TABLE_USER,
 				new String[] { DatabaseHelper.USER_COLUMN_USERID },
 				DatabaseHelper.USER_COLUMN_USERID + " = ?",
 				new String[] { u1.getId() + "" }, null, null, null);
 		assertTrue(c.getCount() == 0);
 
 		// Trying to remove a user that doesn't exist
 		assertFalse(dbHelper.removeUser(new User("509", "Daniel")));
 	}
 
 	public void testUpdateUser() {
 		String id = "507";
 		String newUsername = "Daniel Larsson";
 		User u1 = new User(id, "Daniel");
 		u1.setSource("twitter");
 		dbHelper.addUser(u1);
 		u1.setUserName(newUsername);
 		assertTrue(dbHelper.updateUser(u1));
 
 		Cursor c = db.query(DatabaseHelper.TABLE_USER, null,
 				DatabaseHelper.USER_COLUMN_USERID + " = ?", new String[] { id
 						+ "" }, null, null, null);
 		c.moveToFirst();
 
 		assertTrue(c.getString(1).equals(newUsername));
 	}
 
 	public void testUserExist() {
 		User u1 = new User("507", "Daniel");
 		u1.setSource("twitter");
 		dbHelper.addUser(u1);
 		assertTrue(dbHelper.userExist(u1));
 
 		// Trying a user that don't exist.
 		assertFalse(dbHelper.userExist(new User("104", "David")));
 	}
 
 	public void testClearUserTable() {
 		User u1 = new User("507", "Daniel");
 		u1.setSource("twitter");
 		dbHelper.addUser(u1);
 		dbHelper.clearUserTable();
 		assertTrue(DatabaseUtils.queryNumEntries(db, DatabaseHelper.TABLE_USER) == 0);
 	}
 
 	public void testGetUserTableSize() {
 		User u1 = new User("507", "Daniel");
 		u1.setSource("twitter");
 		dbHelper.addUser(u1);
 		User u2 = new User("509", "David");
 		u2.setSource("twitter");
 		dbHelper.addUser(u2);
 		assertTrue(DatabaseUtils.queryNumEntries(db, DatabaseHelper.TABLE_USER) == dbHelper.getUserTableSize());
 		
 		// Updating user too make sure size doesn't increase. 
 		u1.setUserName("Olle");
 		dbHelper.addUser(u1);
 		assertTrue(DatabaseUtils.queryNumEntries(db, DatabaseHelper.TABLE_USER) == dbHelper.getUserTableSize());
 	}
 	
 	public void testAddItem(){
 
 	User u1 = new User("507", "Daniel");
 	u1.setSource("twitter");
 	
 	long id = 13001;
 	String text = "test item";
 	String timestamp = "Wed Jul 04 13:37:11 +0100 2012";
 	TwitterItem i1 = new TwitterItem();
 	i1.setId(id + "");
 	i1.setText(text);
 	i1.setUser(u1);
 	i1.setTimestamp(timestamp);
 
 	assertTrue(dbHelper.addItem(i1));
 	
 	Cursor c = db.query(DatabaseHelper.TABLE_ITEM, null,
 			DatabaseHelper.ITEM_COLUMN_ITEMID + " = ?", new String[] { id
 					+ "" }, null, null, null);
 	c.moveToFirst();
 	
 	String dbId = c.getString(1);
 	String dbText = c.getString(2);
 
 	assertTrue(dbId.equals(id + ""));
 	assertTrue(dbText.equals(text));
 
 	// Testing to update the same item:
 	
 	String dbNewText = "new test item";
 	i1.setText(dbNewText);
 	
 	assertTrue(dbHelper.addItem(i1));
 	
 	c = db.query(DatabaseHelper.TABLE_ITEM, null,
 			DatabaseHelper.ITEM_COLUMN_ITEMID + " = ?", new String[] { id
 					+ "" }, null, null, null);
 	c.moveToFirst();
 	assertTrue(dbNewText.equals(c.getString(2)));
 
 	// Trying to add a item with no ID;
 	assertFalse(dbHelper.addItem(new TwitterItem()));
 	}
 	
 	public void testUpdateItem(){
 		User u1 = new User("507", "Daniel");
 		u1.setSource("twitter");
 		
 		long id = 13001;
 		String text = "test item";
 		String updatedText = "updated test item";
 		String timestamp = "Wed Jul 04 13:37:11 +0100 2012";
 		
 		TwitterItem i1 = new TwitterItem();
 		i1.setId(id + "");
 		i1.setText(text);
 		i1.setUser(u1);
 		i1.setTimestamp(timestamp);
 		
 		dbHelper.addItem(i1);
 		i1.setText(updatedText);
 		assertTrue(dbHelper.updateItem(i1));
 
 		Cursor c = db.query(DatabaseHelper.TABLE_ITEM, null,
 				DatabaseHelper.ITEM_COLUMN_ITEMID + " = ?", new String[] { id
 						+ "" }, null, null, null);
 		c.moveToFirst();
 		
 		String dbText = c.getString(2);
 		
 		assertTrue(dbText.equals(updatedText));
 		i1.setId(null);
 		
 		// Trying to update a item without id.
 		assertFalse(dbHelper.updateItem(i1));
 	}
 	public void testItemExist(){
 		User u1 = new User("507", "Daniel");
 		u1.setSource("twitter");
 		
 		long id = 13001;
 		String text = "test item";
 		String timestamp = "Wed Jul 04 13:37:11 +0100 2012";
 		
 		TwitterItem i1 = new TwitterItem();
 		i1.setId(id + "");
 		i1.setText(text);
 		i1.setUser(u1);
 		i1.setTimestamp(timestamp);
 		
 		dbHelper.addItem(i1);
 		// Checking if itemExist adds 
 		assertTrue(dbHelper.itemExist(i1));
 		
 		TwitterItem i2 = new TwitterItem();
 		i2.setId(13002 + "");
 		i2.setText(text);
 		i2.setUser(u1);
 		i2.setTimestamp(timestamp);
 		
 		// Checking if i2 exists.
 		assertFalse(dbHelper.itemExist(i2));
 		
 		// Checking if a empty TwitterItem
 		assertFalse(dbHelper.itemExist(new TwitterItem()));
 	}
 }
