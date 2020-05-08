 package es.uc3m.setichat.utils;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import es.uc3m.setichat.utils.datamodel.Contact;
 import es.uc3m.setichat.utils.datamodel.Conversation;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DatabaseManager extends SQLiteOpenHelper{
 
 	 // Database Name
     private static final String DATABASE_NAME = "SetiChat";
     
  // Database Version
     private static final int DATABASE_VERSION = 1;
     
 	// Table names
 	private final String TABLE_CONVERSATIONS = "conversations";
 	private final String TABLE_PROFILE = "profile";
 	private final String TABLE_CONTACTS = "contacts";
 	
 	private static final String CONVERSATION_ID = "id";
     private static final String CONVERSATION_IDSOURCE = "idsource";
     private static final String CONVERATION_TEXT = "text";
     private static final String CONVERATION_DATE = "date";
     
     private static final String CONTACT_ID = "id";
     private static final String CONTACT_IDDESTINATION = "iddestination";
     private static final String CONTACT_NAME = "name";
     private static final String CONTACT_KEY = "publickey";
 	
 	// SQL creation statements
 	private final String conversationTable = "CREATE TABLE "+TABLE_CONVERSATIONS
 			+ "(" + CONVERSATION_ID + " INTEGER PRIMARY KEY," + CONVERSATION_IDSOURCE + " TEXT,"
			+ CONVERATION_TEXT + " TEXT," + CONVERATION_DATE + " TEXT)";
 	
 	private final String contactsTable = "CREATE TABLE "+ TABLE_CONTACTS
 			+ "(" + CONTACT_ID + " INTEGER PRIMARY KEY," + CONTACT_IDDESTINATION + " TEXT,"
 			+ CONTACT_NAME + " TEXT, "+CONTACT_KEY+ " BLOB)";
 	
 	//private final String profileTable = "CREATE TABLE "+TABLE_PROFILE;	
 	
 	
 	public DatabaseManager(Context context, String name, CursorFactory factory,
 			int version) {
 		super(context, name, factory, version);
 		// TODO Auto-generated constructor stub
 	}
 	
 	public DatabaseManager(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		//db.execSQL(profileTable);
 		db.execSQL(contactsTable);
 		db.execSQL(conversationTable);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		// TODO Auto-generated method stub
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONVERSATIONS);
 		//db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILE);
 		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
         onCreate(db);
 		
 	}
 	
 	
 	public void addContact(Contact contact) {
 	    SQLiteDatabase db = this.getWritableDatabase();
 	 
 	    ContentValues values = new ContentValues();	  
 	    values.put(CONTACT_ID, contact.getId());
 	    values.put(CONTACT_NAME, contact.getName()); // Contact Name
 	    values.put(CONTACT_IDDESTINATION, contact.getIdDestination());
 	 
 	    // Inserting Row
 	    db.insert(TABLE_CONTACTS, null, values);
 	    db.close(); // Closing database connection
 	}
 	
 	
 	public void addConversation(Conversation conv) {
 	    SQLiteDatabase db = this.getWritableDatabase();
 	 
 	    ContentValues values = new ContentValues();	  
 	    values.put(CONVERSATION_ID, conv.getID());
	    values.put(CONVERATION_TEXT, conv.getText()); // Contact Name
	    values.put(CONVERSATION_IDSOURCE, conv.getidsource());
 	    values.put(CONVERATION_DATE, conv.getDate());
 	 
 	    // Inserting Row
 	    db.insert(TABLE_CONVERSATIONS, null, values);
 	    db.close(); // Closing database connection
 	}
 	
 	 // Getting All Contacts
 	 public List<Contact> getAllContacts() {
 	    List<Contact> contactList = new ArrayList<Contact>();
 	    // Select All Query
 	    String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;
 	 
 	    SQLiteDatabase db = this.getWritableDatabase();
 	    Cursor cursor = db.rawQuery(selectQuery, null);
 	 
 	    // looping through all rows and adding to list
 	    if (cursor.moveToFirst()) {
 	        do {
 	            Contact contact = new Contact();
 	            contact.setId(Integer.parseInt(cursor.getString(0)));	            
 	            contact.setIdDestination(cursor.getString(1));
 	            contact.setName(cursor.getString(2));
 	            // Adding contact to list
 	            contactList.add(contact);
 	        } while (cursor.moveToNext());
 	    }
 	 
 	    // return contact list
 	    return contactList;
 	}
 	 
 	 // Getting All Contacts
 	 public List<Conversation> getAllConversations() {
 	    List<Conversation> convList = new ArrayList<Conversation>();
 	    // Select All Query
 	    String selectQuery = "SELECT  * FROM " + TABLE_CONVERSATIONS;
 	 
 	    SQLiteDatabase db = this.getWritableDatabase();
 	    Cursor cursor = db.rawQuery(selectQuery, null);
 	 
 	    // looping through all rows and adding to list
 	    if (cursor.moveToFirst()) {
 	        do {
 	            Conversation conv = new Conversation();
 	            conv.setID(Integer.parseInt(cursor.getString(0)));	            
 	            conv.setidsource(cursor.getString(1));
 	            conv.setText(cursor.getString(2));
 	            conv.setDate(cursor.getString(3));
 	            // Adding conv to list
 	            convList.add(conv);
 	        } while (cursor.moveToNext());
 	    }
 	 
 	    // return contact list
 	    return convList;
 	}
 	 
 	 // Getting All Contacts
 	 public List<Conversation> getAllConversations(String idsource) {
 	    List<Conversation> convList = new ArrayList<Conversation>();
 	    // Select All Query
 	    String selectQuery = "SELECT * FROM " + TABLE_CONVERSATIONS + " WHERE " + CONVERSATION_IDSOURCE
 	    		+ "='" + idsource +"'";
 	 
 	    SQLiteDatabase db = this.getWritableDatabase();
 	    Cursor cursor = db.rawQuery(selectQuery, null);
 	 
 	    // looping through all rows and adding to list
 	    if (cursor.moveToFirst()) {
 	        do {
 	            Conversation conv = new Conversation();
 	            conv.setID(Integer.parseInt(cursor.getString(0)));	            
 	            conv.setidsource(cursor.getString(1));
 	            conv.setText(cursor.getString(2));
 	            conv.setDate(cursor.getString(3));
 	            // Adding conv to list
 	            convList.add(conv);
 	        } while (cursor.moveToNext());
 	    }
 	 
 	    // return contact list
 	    return convList;
 	}	 
 	 
 	// Getting contacts Count
 	    public int getContactsCount() {
 	        String countQuery = "SELECT  * FROM " + TABLE_CONTACTS;
 	        SQLiteDatabase db = this.getReadableDatabase();
 	        Cursor cursor = db.rawQuery(countQuery, null);
 	        int count = cursor.getCount();
 	        cursor.close();
 	 
 	        // return count
 	        return count;
 	    }
 	    
 	 // Getting convesation Count
 	    public int getConversationsCount() {
 	        String countQuery = "SELECT  * FROM " + TABLE_CONVERSATIONS;
 	        SQLiteDatabase db = this.getReadableDatabase();
 	        Cursor cursor = db.rawQuery(countQuery, null);
 	        int count = cursor.getCount();
 	        cursor.close();
 	 
 	        // return count
 	        return count;
 	    }
 	    
 		 // Get contact info based on contact id
 	    public Contact getContact(int id) {
 	        String countQuery = "SELECT  * FROM " + TABLE_CONTACTS + " WHERE " + CONTACT_ID
 	        		+ " = " + id;
 	        SQLiteDatabase db = this.getReadableDatabase();
 	        Cursor cursor = db.rawQuery(countQuery, null);
 	        int count = cursor.getCount();
 	        Contact result = new Contact();
 	        if(count != 0){
 	        	cursor.moveToFirst();
 	        	result.setId(cursor.getInt(0));
 	        	result.setIdDestination(cursor.getString(1));
 	        	result.setName(cursor.getString(2));
 	        	//result.setPublicKey(cursor.getBlob(3));
 	        	//cursor.getString(0);
 	        }
 	        cursor.close();
 	 
 	        // return count
 	        return result;
 	    }
 	    
 		 // Get contact info based on idsource
 	    public Contact getContact(String idsource) {
 	        String countQuery = "SELECT  * FROM " + TABLE_CONTACTS + " WHERE " + CONTACT_IDDESTINATION
 	        		+ " ='" + idsource + "'";
 	        SQLiteDatabase db = this.getReadableDatabase();
 	        Cursor cursor = db.rawQuery(countQuery, null);
 	        int count = cursor.getCount();
 	        Contact result = new Contact();
 	        if(count != 0){
 	        	cursor.moveToFirst();
 	        	result.setId(cursor.getInt(0));
 	        	result.setIdDestination(cursor.getString(1));
 	        	result.setName(cursor.getString(2));
 	        	//cursor.getString(0);
 	        }else{
 	        	result = null;
 	        }
 	        cursor.close();
 	 
 	        // return count
 	        return result;
 	    }
 	
 	    public void dropDatabase(){
 	    	SQLiteDatabase db = this.getWritableDatabase();
 	    	db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONVERSATIONS);
 			db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
 			return;
 	    }
 }
