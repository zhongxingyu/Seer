 package com.pigote.dpfinal.db;
 
 import com.pigote.dpfinal.MWReaderXmlParser.Entry;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DBHandler extends SQLiteOpenHelper{
 	// Instance
 	private static DBHandler INSTANCE = null;
 	
 	// All Static variables
     // Database Version
     private static final int DATABASE_VERSION = 1;
  
     // Database Name
     private static final String DATABASE_NAME = "wordDb";
  
     // Contacts table name
     private static final String TABLE_DICTIONARY = "mydictionary";
  
     // Contacts Table Columns names
     private static final String KEY_ID = "id";
     private static final String KEY_WORD = "word";
     private static final String KEY_DEFINITION = "definition";
     private static final String KEY_URI = "fileloc";
  
     protected DBHandler(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
  
     // Creating Tables
     @Override
     public void onCreate(SQLiteDatabase db) {
         String CREATE_DICTIONARY_TABLE = "CREATE TABLE " + TABLE_DICTIONARY + "("
                 + KEY_ID + " INTEGER PRIMARY KEY," + KEY_WORD + " TEXT,"
                 + KEY_DEFINITION + " TEXT," + KEY_URI + " TEXT" + ")";
         db.execSQL(CREATE_DICTIONARY_TABLE);
     }
  
     // Upgrading database
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         // Drop older table if existed
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_DICTIONARY);
  
         // Create tables again
         onCreate(db);
     }
     
     // Adding new entry
     public void addEntry(Entry entry) {
     	SQLiteDatabase db = this.getWritableDatabase();
     	 
     	ContentValues values = new ContentValues();
     	values.put(KEY_WORD, entry.word); 
     	values.put(KEY_DEFINITION, entry.def);
     	values.put(KEY_URI, entry.sound.toString()); 
     	 
     	// Inserting Row
     	db.insert(TABLE_DICTIONARY, null, values);
     	db.close(); // Closing database connection
     }
      
     // Getting single entry
     public WordDbTableEntry getEntry(int id) {
     	SQLiteDatabase db = this.getReadableDatabase();
     	 
     	Cursor cursor = db.query(TABLE_DICTIONARY, new String[] { KEY_ID,
     	        KEY_WORD, KEY_DEFINITION, KEY_URI }, KEY_ID + "=?",
     	        new String[] { String.valueOf(id) }, null, null, null, null);
     	if (cursor != null)
     	    cursor.moveToFirst();
     	 
     	WordDbTableEntry entry = new WordDbTableEntry(Integer.parseInt(cursor.getString(0)),
     	        cursor.getString(1), cursor.getString(2), cursor.getString(3));
     	// return entry
     	return entry;
     }
      
     // Deleting single word
     public void deleteWord(WordDbTableEntry entry) {
     	SQLiteDatabase db = this.getWritableDatabase();
    	    db.delete(TABLE_DICTIONARY, KEY_ID + " = ?",
    	            new String[] { String.valueOf(entry.getID()) });
    	    db.close();    	
     }
     
    private boolean wordExists(String myWord){
 	    boolean exists = false;
 	    SQLiteDatabase db = this.getReadableDatabase();
	    String wordToUse = myWord;
	    //TODO start here, check database when apostrophe is used
	    if (myWord.contains("'"))
	    	wordToUse = myWord.replace("'", "\""); 
 	    Cursor cursor;
	    cursor = db.query(TABLE_DICTIONARY, new String []{ KEY_WORD },
						KEY_WORD + "=?",new String[] { wordToUse }, 
 						null, null, null, null);
 	    if (cursor.getCount()>0){
 	    	exists = true;
    		}
 	    return exists;
    }
    
    
     // returns an array of missing words
     public String[] getMissingWords(String words) {
     	String[] myWords = words.split(" ");
     	
     	for (int i = 0; i<myWords.length; i++){
     		if (wordExists(myWords[i]))
     			myWords[i] = "*";  
     	}
     	return myWords;
     }  
     
     //getters for singleton
     public static synchronized DBHandler getInstance(Context context)
     {
     	if (INSTANCE != null)
         return INSTANCE;
     	else return new DBHandler(context);
     }
 }
