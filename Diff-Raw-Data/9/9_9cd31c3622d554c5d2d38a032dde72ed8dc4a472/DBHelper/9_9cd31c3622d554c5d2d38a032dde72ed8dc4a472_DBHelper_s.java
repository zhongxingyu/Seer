 /*
  * Copyright (C) 2008 Google Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package zebrafish.util;
 
 import java.util.ArrayList;
 
 import service.InviteTask;
 import service.ItemTask;
 import service.CustomListTask;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 import edu.pitt.cs1635group3.Activities.Classes.CustomList;
 import edu.pitt.cs1635group3.Activities.Classes.Invite;
 import edu.pitt.cs1635group3.Activities.Classes.Item;
 import edu.pitt.cs1635group3.Activities.Classes.User;
 
 public class DBHelper {
 
 	public static final String ITEM_TABLE = "item";
 	public static final String LIST_TABLE = "list";
 	public static final String MAP_LIST_USER_TABLE = "map_list_user";
 	public static final String USER_TABLE = "user";
 
 	public static final String KEY_ITEM_ID = "id";
 	public static final String KEY_PARENT_ID = "parent_id";
 	public static final String KEY_ITEM_NAME = "name";
 	public static final String KEY_ADDER_ID = "adder_id";
 	public static final String KEY_ADD_DATE = "add_date";
 	public static final String KEY_QUANTITY = "quantity";
 	public static final String KEY_ASSIGNED_TO = "assignee_id";
 	public static final String KEY_ASSIGNER_ID = "assigner_id";
 	public static final String KEY_NOTES = "notes";
 	public static final String KEY_COMPLETED = "completed";
 	public static final String KEY_COMPLETION_DATE = "completion_date";
 	public static final String KEY_ITEM_PREV = "prev_id";
 	public static final String KEY_ITEM_NEXT = "next_id";
 	public static final String KEY_ITEM_SELECTED = "selected";
 
 	public static final String KEY_LIST_ID = "id";
 	public static final String KEY_LIST_CUSTOM_ID = "custom_id";
 	public static final String KEY_LIST_NAME = "name";
 	public static final String KEY_CREATOR_ID = "creator_id";
 	public static final String KEY_CREATION_DATE = "creation_date";
 	public static final String KEY_NOTE = "notes";
 
 	public static final String KEY_MAP_LIST_USER_ID = "id";
 	public static final String KEY_MAP_LIST_ID = "list_id";
 	public static final String KEY_MAP_LIST_USERID = "user_id";
 	public static final String KEY_MAP_LIST_NAME = "list_name";
 	public static final String KEY_MAP_PENDING = "pending";
 	public static final String KEY_MAP_INVITE_DATE = "invite_date";
 
 	public static final String KEY_USER_ID = "id";
 	public static final String KEY_USER_FIRST = "first";
 	public static final String KEY_USER_LAST = "last";
 	public static final String KEY_USER_EMAIL = "email";
 	public static final String KEY_USER_DEVICE_TOKEN = "device_token";
 	public static final String KEY_USER_DEVICE_ID = "device_id";
 
 	private static final String TAG = "DB HELPER";
 	private DatabaseHelper mDbHelper;
 	private SQLiteDatabase db;
 
 	private static final boolean NO_PUSH_TO_CLOUD = false;
 	private static final boolean PUSH_TO_CLOUD = true;
 
 	/**
 	 * Database creation sql statement
 	 */
 	private static final String ITEM_CREATE = "create table item (id integer primary key autoincrement, "
 			+ "parent_id integer, "
 			+ "name text not null, "
 			+ "adder_id integer, "
 			+ "add_date date DEFAULT CURRENT_DATE, "
 			+ "quantity integer DEFAULT '1', "
 			+ "assignee_id integer, "
 			+ "assigner_id integer, "
 			+ "notes text, completed integer, "
 			+ "completion_date text, "
 			+ "prev_id integer, "
 			+ "next_id integer, "
 			+ "selected integer, "
 			+ "UNIQUE (id) ON CONFLICT IGNORE)";
 
 	private static final String LIST_CREATE = "create table list (id integer primary key autoincrement, "
 			+ "custom_id text, "
 			+ "name text not null, creator_id integer not null, "
 			+ "creation_date text DEFAULT CURRENT_DATE, UNIQUE(id) ON CONFLICT IGNORE)";
 
 	private static final String MAP_LIST_USER_CREATE = "create table map_list_user (id integer primary key autoincrement, "
 			+ "list_id integer, user_id integer, list_name text, invite_date text, pending integer)";
 
 	private static final String USER_CREATE = "create table user (id integer primary key autoincrement, "
 			+ "first text not null, last text not null, email text,"
 			+ "device_token text, device_id text)";
 
 	private static final String DATABASE_NAME = "socialist_db";
 	private static final int DATABASE_VERSION = 1;
 
 	private final Context mCtx;
 
 	/**
 	 * Constructor - takes the context to allow the database to be
 	 * opened/created
 	 * 
 	 * @param ctx
 	 *            the Context within which to work
 	 */
 	public DBHelper(Context ctx) {
 		this.mCtx = ctx;
 	}
 
 	private static class DatabaseHelper extends SQLiteOpenHelper {
 
 		public DatabaseHelper(Context context) {
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			db.execSQL(ITEM_CREATE);
 			db.execSQL(LIST_CREATE);
 			db.execSQL(MAP_LIST_USER_CREATE);
 			db.execSQL(USER_CREATE);
 			
 			String q = "delete from map_list_user where 1";
 			db.rawQuery(q, null);
 			
 
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
 					+ newVersion + ", which will destroy all old data");
 			onCreate(db);
 		}
 	} // end DatabaseHelper inner class
 
 	public DBHelper open() throws SQLException {
 
 		mDbHelper = new DatabaseHelper(mCtx);
 		db = mDbHelper.getWritableDatabase();
 		return this;
 	}
 
 	public void close() {
 		mDbHelper.close();
 	}
 
 	/*
 	 * USER METHODS
 	 */
 
 	public void insertOrUpdateUser(int listID, User i, boolean pushToCloud) {
  
 		String id = i.getID() + "";
 		String myQuery = "SELECT * FROM user WHERE id = " + id;
 		Log.i("insertingOrUpdateUser", "name " +i.getName());
 		Cursor c = db.rawQuery(myQuery, null);
 
 		if (c.getCount() > 0) {
 			// Item exists
 			c.close();
 			updateUser(i, pushToCloud);
 		} else {
 			c.close();
 			insertUser(listID, i, pushToCloud);
 		}
 	}
 
 	public boolean updateUser(User i, boolean pushToCloud) {
 		ContentValues args = new ContentValues();
 
 		args.put(KEY_USER_FIRST, i.getFirstName());
 		args.put(KEY_USER_LAST, i.getLastName());
 		args.put(KEY_USER_EMAIL, i.getEmail());
 		args.put(KEY_USER_DEVICE_TOKEN, i.getDeviceToken());
 		args.put(KEY_USER_DEVICE_ID, i.getDeviceID());
 
 		return db.update(USER_TABLE, args, KEY_USER_ID + "=?",
 				new String[] { String.valueOf(i.getID()) }) > 0;
 
 	}
 
 	public void insertUser(int listID, User u, boolean pushToCloud) {
 		ContentValues initialValues = new ContentValues();
 		initialValues.put(KEY_USER_ID, u.getID());
 		initialValues.put(KEY_USER_FIRST, u.getFirstName());
 		initialValues.put(KEY_USER_LAST, u.getLastName());
 		initialValues.put(KEY_USER_EMAIL, u.getEmail());
 		initialValues.put(KEY_USER_DEVICE_TOKEN, u.getDeviceToken());
 		initialValues.put(KEY_USER_DEVICE_ID, u.getDeviceID());
 		db.insert(USER_TABLE, null, initialValues);
 		
 		//list_id, user_id, list_name, invite_date, pending
 
 		
 		initialValues = new ContentValues();
 		//String listName = getListName(listID);
 		String listName= "test";
 		// also insert into the mapping table
 		initialValues.put(KEY_MAP_LIST_USERID, u.getID());
 		initialValues.put(KEY_MAP_LIST_ID, listID);
 		initialValues.put(KEY_MAP_LIST_NAME, listName);
 		//initialValues.put(KEY_MAP_INVITE_DATE, inv.getInviteDate());
 		initialValues.put(KEY_MAP_PENDING, 0); // by default, users attached to a list are not pending
 		Log.i(TAG, "map: " +initialValues.describeContents());
 
 		Log.i(TAG, "map insertion: " +db.insert(MAP_LIST_USER_TABLE, null, initialValues));		
 		
 	}
 
 	public void insertOrUpdateInvite(Context context, Invite inv, boolean pushToCloud) {
 
 		String id = inv.getListID() + "";
 		String myQuery = "SELECT * FROM map_list_user WHERE list_id = " + id;
 		Cursor c = db.rawQuery(myQuery, null);
 
 		if (c.getCount() > 0) {
 			// Invite exists
 			c.close();
 			updateInvite(context, inv, pushToCloud);
 		} else {
 			c.close();
 			insertInvite(inv, pushToCloud);
 		}
 	}
 
 	public void ignoreInvite(Context context, int id, boolean pushToCloud) {
 		// delete the invite
 		
 		if (pushToCloud) {
 			new InviteTask().ignoreInvite(context, id);
 		}
 
 		db.delete(MAP_LIST_USER_TABLE, KEY_MAP_LIST_USER_ID + "=?",
 				new String[] { String.valueOf(id) });
 
 	}
 	
 
 
 	public void acceptInvites(Context context,
 			ArrayList<Invite> selectedInvites, boolean pushToCloud) {
 
 		for (Invite inv : selectedInvites) {
 			inv.setPending(0);
 			updateInvite(context, inv, NO_PUSH_TO_CLOUD); // push all at once
 		}
 
 		if (pushToCloud) {
 			new InviteTask().updateInvites(context, selectedInvites);
 		}
 
 		
 	}
 
 	public boolean updateInvite(Context context, Invite inv, boolean pushToCloud) {
 
 		ContentValues args = new ContentValues();
 		args.put(KEY_MAP_LIST_ID, inv.getListID());
 		args.put(KEY_MAP_PENDING, inv.isPending());
 		args.put(KEY_MAP_INVITE_DATE, inv.getInviteDate());
 
 		if (pushToCloud) {
 			new InviteTask().updateInvite(context, inv);
 		}
 
 		Log.i(TAG, "Updating invite to have pending = " +inv.isPending());
 		
 		return db.update(MAP_LIST_USER_TABLE, args,
 				KEY_MAP_LIST_USER_ID + "=?",
 				new String[] { String.valueOf(inv.getID()) }) > 0;
 
 	}
 
 	public long insertInvite(Invite inv, boolean pushToCloud) {
 
 		Log.i(TAG, "insert invite called");
 
 		ContentValues initialValues = new ContentValues();
 		initialValues.put(KEY_MAP_LIST_USER_ID, inv.getID());
 		initialValues.put(KEY_MAP_LIST_ID, inv.getListID());
 		initialValues.put(KEY_MAP_LIST_NAME, inv.getListName());
 		initialValues.put(KEY_MAP_INVITE_DATE, inv.getInviteDate());
 		initialValues.put(KEY_MAP_PENDING, inv.isPending());
 
 		if (pushToCloud) {
 			Log.i(TAG, "InsertInvite: TODO pushToCloud");
 		}
 
 		return db.insert(MAP_LIST_USER_TABLE, null, initialValues);
 
 	}
 
 	public ArrayList<Invite> getUserInvites() {
 
 		ArrayList<Invite> invites = new ArrayList<Invite>();
 
 		String myQuery = "SELECT * FROM map_list_user WHERE pending=1";
 
 		Cursor c = db.rawQuery(myQuery, null);
 
 		if (c != null) {
 			invites = new ArrayList<Invite>(c.getCount());
 			c.moveToFirst();
 
 			while (!c.isAfterLast()) {
 				Invite inv = new Invite();
 				inv.setID(c.getInt(0));
 				inv.setListID(c.getInt(1));
 				inv.setListName(c.getString(3));
 				inv.setInviteDate(c.getString(4));
 				invites.add(inv);
 				c.moveToNext();
 			} // end while
 
 			c.close();
 		} else {
 			Log.i(TAG, "no user invites found in db");
 		}
 
 		return invites;
 	} // end getUserInvites
 
 	public CharSequence[] getUsersForDialog(int id) {
 		// as a param
 		String listID = ""+id;
 		CharSequence[] users;
 
 		String myQuery = "SELECT * FROM user, map_list_user WHERE user.first != null AND user.id = map_list_user.user_id AND map_list_user.list_id = " +listID;		
 		Cursor c = db.rawQuery(myQuery, null);
 
 		if (c != null)
 			c.moveToFirst();
 		
 		users = new CharSequence[c.getCount()]; // allow for number of users
 												// returned
 
 		c.moveToFirst();
 		int i = 0;
 		while (!c.isAfterLast()) {
 			users[i] = (CharSequence) c.getString(1); // user's name
 			c.moveToNext();
 			i++;
 		}
 		c.close();
 		return users;
 	}
 
 	public ArrayList<User> getAllUsers1() {
 
 		ArrayList<User> users = null;
 		String myQuery = "SELECT * FROM user";// WHERE parent_id = " + ID;
 		Cursor c = db.rawQuery(myQuery, null);
 
 		if (c != null) {
 			users = new ArrayList<User>(c.getCount());
 			c.moveToFirst();
 
 			while (!c.isAfterLast()) {
 				User u = new User();
 				u.setID(c.getInt(0));
 				u.setFirstName(c.getString(1));
 				users.add(u);
 				c.moveToNext();
 			}
 
 			c.close();
 
 		}
 		return users;
 
 	}
 
 	public ArrayList<User> getUsersForList(int ID) {
 		
 		String listID = "" + ID;
 		ArrayList<User> users = new ArrayList<User>();
 		String myQuery = "SELECT * FROM user, map_list_user WHERE user.first != null AND user.id = map_list_user.user_id AND map_list_user.list_id = " +listID;
 		
 		Cursor c = db.rawQuery(myQuery, null); 
 
 		if (c != null) {
 			users = new ArrayList<User>(c.getCount());
 			c.moveToFirst();
 			
 			while (!c.isAfterLast()) {
 				User u = new User();
 				u.setID(c.getInt(0));
 				u.setFirstName(c.getString(1));
 				users.add(u);
 				c.moveToNext();
 			}
 
 			c.close();
 
 		}
 		
 		return users;
 
 	}
 
 	public String getUserNameByID(int row) {
 		String myQuery = "SELECT * FROM user WHERE id = " + row;
 		Cursor c = db.rawQuery(myQuery, null);
 
 		if (c != null)
 			c.moveToFirst();
 
 		String name = c.getString(1) + " " + c.getString(2);
 		c.close();
 		return name;
 
 	}
 
 	public User getUserByID(int row) {
 
 		String myQuery = "SELECT * FROM user WHERE id = " + row;
 		Cursor c = db.rawQuery(myQuery, null);
 
 		if (c != null)
 			c.moveToFirst();
 
 		User u = new User(c.getInt(0), c.getString(1), c.getString(2),
 				c.getString(3));
 		// Log.i("DB USER", "Name is " +u.getName());
 		c.close();
 		return u;
 	}
 
 	public int getUserByName(String user) {
 
 		String[] pieces = user.split(" ");
 		String fname = pieces[0];
 		String lname = pieces[1];
 
 		String myQuery = "SELECT * FROM user WHERE first = ? AND last = ?";
 		Cursor c = db.rawQuery(myQuery, new String[] { fname, lname });
 
 		if (c != null)
 			c.moveToFirst();
 
 		int userID = c.getInt(0);
 		c.close();
 		return userID;
 	}
 
 	private User cursorToUser(Cursor c) {
 		// Log.d("DB", "c.getCount() is " +c.getCount());
 		User u = new User(c.getInt(0), c.getString(1), c.getString(2),
 				c.getString(3));
 		u.setDeviceToken(c.getString(4));
 		u.setDeviceID(c.getString(5));
 		c.close();
 		return u;
 	}
 
 	/*
 	 * LIST METHODS
 	 */
 
 	public void updateLocalListID(CustomList list, int newID) {
 		// approach 1: replace list ID
 		// approach 2: create identical new list with the new list ID. delete
 		// the old list.
 
 		ContentValues args = new ContentValues();
 
 		args.put(KEY_LIST_ID, newID);
 
 		// Log.d("REPLACED LOCAL LIST ID", "New ID is " + newID);
 
 		db.update(LIST_TABLE, args, KEY_LIST_ID + "=?",
 				new String[] { String.valueOf(list.getID()) });
 	}
 
 	public int getLocalListID() {
 		// when the user is creating a new list - before it goes to the web
 		// server, pull a local primary key.
 		// when it is pushed to the mysql server, update its local ID.
 
 		String q = "SELECT max(id) from list";
 		Cursor c = db.rawQuery(q, null);
 		if (c != null) {
 			c.moveToFirst();
 		}
 
 		int tempID = c.getInt(0) + 1; // make it unique
 		c.close();
 
 		// Log.i("LOCAL LIST ID", "Temp list ID is " + tempID);
 		return tempID;
 	}
 
 	public void deleteListAndChildren(CustomList list) {
 
 		ArrayList<Item> children = list.getItems();
 		for (Item item : children) {
 			JSONItem.deleteItem(item.getID());
 		}
 
 		JSONCustomList.deleteList(list.getID());
 
 		db.delete(LIST_TABLE, KEY_LIST_ID + "=?",
 				new String[] { String.valueOf(list.getID()) });
 	}
 
 	public boolean deleteList(CustomList list) {
 		JSONCustomList.deleteList(list.getID());
 
 		return db.delete(LIST_TABLE, KEY_LIST_ID + "=?",
 				new String[] { String.valueOf(list.getID()) }) > 0;
 	}
 
 	public boolean deleteListByID(int listID) {
 
 		JSONCustomList.deleteList(listID);
 		return db.delete(LIST_TABLE, KEY_LIST_ID + "=?",
 				new String[] { String.valueOf(listID) }) > 0;
 	}
 
 	public void insertOrUpdateList(CustomList i, boolean pushToCloud) {
 		Log.i(TAG, "InsertOrUpdateList: " +i.getName());
 		
 		String id = i.getID() + "";
 		String myQuery = "SELECT * FROM list WHERE id = " + id;
 		Cursor c = db.rawQuery(myQuery, null);
 
 		if (c.getCount() > 0) {
 			// Item exists
 			c.close();
 			updateList(i, pushToCloud);
 		} else {
 			c.close();
 			insertList(i, pushToCloud);
 		}
 	}
 
 	public void insertList(CustomList list, boolean pushToCloud) {
 		// In order to generate unique PKs that sync with the web server's db,
 		// PKs are pulled down from the server by allocating uninitialized lists
 		// on the web server.
 		// This means that instead of truly "inserting" the new list on the web
 		// server, we need to UPDATE
 		// the currently-null list item using the ID.
 
 		int userID = list.getCreator();
 
 		if (pushToCloud) {
 			new CustomListTask().createAsUpdate(list);
 		}
 
 		ContentValues initialValues = new ContentValues();
 		initialValues.put(KEY_LIST_ID, list.getID());
 		initialValues.put(KEY_LIST_CUSTOM_ID, list.getCustomID());
 		initialValues.put(KEY_LIST_NAME, list.getName());
 		initialValues.put(KEY_CREATOR_ID, userID);
 		initialValues.put(KEY_CREATION_DATE, list.getCreationDate());
 		db.insert(LIST_TABLE, null, initialValues);
 
 	}
 
 	public void updateList(CustomList list) {
 		// by default, push to server.
 		updateList(list, PUSH_TO_CLOUD);
 	}
 
 	public boolean updateList(CustomList i, boolean pushToCloud) {
 		ContentValues args = new ContentValues();
 
 		if (pushToCloud) {
 			new CustomListTask().update(i);
 		}
 
 		args.put(KEY_LIST_ID, i.getID());
 		args.put(KEY_LIST_CUSTOM_ID, i.getCustomID());
 		args.put(KEY_LIST_NAME, i.getName());
 		args.put(KEY_CREATOR_ID, i.getCreator());
 		args.put(KEY_CREATION_DATE, i.getCreationDate());
 
 		// Log.d("UPDATED LIST", "List ID: " + i.getID());
 
 		return db.update(LIST_TABLE, args, KEY_LIST_ID + "=?",
 				new String[] { String.valueOf(i.getID()) }) > 0;
 
 	}
 
 	public ArrayList<CustomList> getAllLists() {
 
 		ArrayList<CustomList> lists = null;
 		String myQuery = "SELECT * FROM list";// WHERE user_id = " + ID;
 		Cursor c = db.rawQuery(myQuery, null);
 
 		if (c != null) {
 			lists = new ArrayList<CustomList>(c.getCount());
 			c.moveToFirst();
 
 			while (!c.isAfterLast()) {
 				CustomList l = new CustomList();
 				l.setID(c.getInt(0));
 				l.setCustomID(c.getString(1));
 				l.setName(c.getString(2));
 				l.setCreator(c.getInt(3));
 				l.setCreationDate(c.getString(4));
 				lists.add(l);
 				c.moveToNext();
 			}
 		}
 		c.close();
 		return lists;
 
 	}
 
 	public CustomList getListByID(int i) {
 
 		String myQuery = "SELECT * FROM list WHERE id = " + i;
 		Cursor c = db.rawQuery(myQuery, null);
 
 		if (c != null) {
 			c.moveToFirst();
 		}
 
 		CustomList myList = new CustomList();
 		myList.setID(c.getInt(0));
 		myList.setCustomID(c.getString(1));
 		myList.setName(c.getString(2));
 		myList.setCreator(c.getInt(3));
 		myList.setCreationDate(c.getString(4));
 		c.close();
 		return myList;
 	}
 
 	public ArrayList<Item> getItemsForListByID(int ID) {
 
 		// Log.i("QUERY FOR LIST", "Based on list ID " + ID);
 		ArrayList<Item> items = null;
 		String myQuery = "SELECT * FROM item WHERE parent_id = " + ID;
 		Cursor c = db.rawQuery(myQuery, null);
 
 		if (c != null) {
 			items = new ArrayList<Item>(c.getCount());
 			c.moveToFirst();
 
 			while (!c.isAfterLast()) {
 				Item i = cursorToItem(c);
 				items.add(i);
 				c.moveToNext();
 			}
 		}
 		c.close();
 		return items;
 
 	}
 
 	public ArrayList<Item> getAllItemsByUserID(int uid) {
 		ArrayList<Item> items = null;
		String myQuery = "SELECT * FROM item WHERE assignee_id = " + "33";// uid;
 		Cursor c = db.rawQuery(myQuery, null);
 
 		if (c != null) {
 			items = new ArrayList<Item>(c.getCount());
 			c.moveToFirst();
 
 			while (!c.isAfterLast()) {
 				Item i = cursorToItem(c);
 				items.add(i);
 				c.moveToNext();
 			}
 		}
 		c.close();
 		return items;
 	}
 
 	public String getListName(int listID) {
 		// Log.v("QUERY FOR ITEM", "Based on item ID " + row);
 
 		String myQuery = "SELECT name FROM list WHERE id = " + listID;
 		Cursor c = db.rawQuery(myQuery, null);
 
 		if (c != null) {
 			c.moveToFirst();
 		}
 
 		String s = c.getString(0);
 		Log.d(TAG, s);
 		c.close();
 		return s;
 	}
 
 	/*
 	 * ITEM METHODS
 	 */
 
 	public void insertOrUpdateItem(Item i, boolean pushToCloud) {
 		String id = i.getID() + "";
 		String myQuery = "SELECT * FROM item WHERE id = " + id;
 		Cursor c = db.rawQuery(myQuery, null);
 
 		if (c.getCount() > 0) {
 			// Item exists
 			// Log.i(TAG,"insertOrUpdateItem UPDATE");
 			c.close();
 			updateItem(i, pushToCloud);
 		} else {
 			// Log.i(TAG,"insertOrUpdateItem Insert");
 			c.close();
 			insertItem(i, pushToCloud);
 		}
 	}
 
 	public void insertItem(Item i, boolean pushToCloud) {
 		ContentValues initialValues = new ContentValues();
 
 		long ret;
 
 		int isCompleted = 0;
 		if (i.isCompleted())
 			isCompleted = 1;
 
 		initialValues.put(KEY_ITEM_ID, i.getID());
 		initialValues.put(KEY_PARENT_ID, i.getParentID());
 		initialValues.put(KEY_ITEM_NAME, i.getName());
 		initialValues.put(KEY_ADDER_ID, i.getCreator());
 		initialValues.put(KEY_ADD_DATE, i.getCreationDate());
 		initialValues.put(KEY_QUANTITY, i.getQuantity());
 		initialValues.put(KEY_ASSIGNED_TO, i.getAssignee());
 		initialValues.put(KEY_ASSIGNER_ID, i.getAssigner());
 		initialValues.put(KEY_NOTES, i.getNotes());
 		initialValues.put(KEY_COMPLETED, isCompleted);
 		initialValues.put(KEY_COMPLETION_DATE, i.getCompletionDate());
 		initialValues.put(KEY_ITEM_PREV, i.getPrev());
 		initialValues.put(KEY_ITEM_NEXT, i.getNext());
 		initialValues.put(KEY_ITEM_SELECTED, 0);
 
 		ret = db.insert(ITEM_TABLE, null, initialValues);
 
 		Log.i(TAG, "db.insert returned = " + ret);
 
 		// In order to generate unique PKs that sync with the web server's db,
 		// PKs are pulled down from the server by allocating uninitialized items
 		// on the web server.
 		// This means that instead of truly "inserting" the new item on the web
 		// server, we need to UPDATE
 		// the currently-null item using the ID we already have. (Same for
 		// lists.)
 
 		if (pushToCloud) {
 			new ItemTask().update(i);
 		}
 
 	}
 
 	public boolean deleteItem(Item i) {
 		// Note - whenever this is called, be sure to update the encompassing
 		// list structure, since items are doubly-linked.
 
 		JSONItem.deleteItem(i.getID());
 		return db.delete(ITEM_TABLE, KEY_ITEM_ID + "=?",
 				new String[] { String.valueOf(i.getID()) }) > 0;
 	}
 
 	public Item getItem(int row) {
 		// Log.v("QUERY FOR ITEM", "Based on item ID " + row);
 
 		String myQuery = "SELECT * FROM item WHERE id = " + row;
 		Cursor c = db.rawQuery(myQuery, null);
 
 		if (c != null) {
 			c.moveToFirst();
 		}
 
 		Item i = cursorToItem(c);
 		c.close();
 		return i;
 	}
 
 	private Item cursorToItem(Cursor c) {
 		// Log.d("DB", "number of fields is " + c.getCount()+ ". Name is "
 		// +c.getString(2)+ " and ID is " +c.getInt(0));
 		Item i = new Item();
 		i.setID(c.getInt(0));
 		i.setParent(c.getInt(1));
 		i.setName(c.getString(2));
 		i.setCreator(c.getInt(3));
 		i.setCreationDate(c.getString(4));
 		i.setQuantity(c.getInt(5));
 		i.assignTo(c.getInt(6));
 		i.setAssigner(c.getInt(7));
 		i.setNotes(c.getString(8));
 		i.setCompleted(c.getInt(9));
 		i.setCompletionDate(c.getString(10));
 		i.setPrev(c.getInt(11));
 		i.setNext(c.getInt(12));
 		i.setSelected(c.getInt(13)); // when we're pulling items from the db,
 										// this is when we check if it's
 										// selected.
 
 		return i;
 	}
 
 	public boolean updateItem(Item i, boolean pushToCloud) {
 
 		ContentValues args = new ContentValues();
 
 		int isCompleted = 0, isSelected = 0;
 		if (i.isCompleted())
 			isCompleted = 1;
 		if (i.isSelected())
 			isSelected = 1;
 
 		args.put(KEY_PARENT_ID, i.getParentID());
 		args.put(KEY_ITEM_NAME, i.getName());
 		args.put(KEY_ADDER_ID, i.getCreator());
 		args.put(KEY_ADD_DATE, i.getCreationDate());
 		args.put(KEY_QUANTITY, i.getQuantity());
 		args.put(KEY_ASSIGNED_TO, i.getAssignee());
 		args.put(KEY_ASSIGNER_ID, i.getAssigner());
 		args.put(KEY_NOTES, i.getNotes());
 		args.put(KEY_COMPLETED, isCompleted);
 		args.put(KEY_COMPLETION_DATE, i.getCompletionDate());
 		args.put(KEY_ITEM_PREV, i.getPrev());
 		args.put(KEY_ITEM_NEXT, i.getNext());
 		args.put(KEY_ITEM_SELECTED, isSelected);
 
 		if (pushToCloud) {
 			new ItemTask().update(i);
 		}
 
 		return db.update(ITEM_TABLE, args, KEY_ITEM_ID + "=?",
 				new String[] { String.valueOf(i.getID()) }) > 0;
 	}
 
 	public boolean updateItem(Item i) {
 		return updateItem(i, PUSH_TO_CLOUD);
 
 	}
 
 }
