 package org.learnnavi.app;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.MatrixCursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class EntryDBAdapter extends SQLiteOpenHelper {
 	private static final String DB_PATH = "/data/data/org.learnnavi.app/databases/";
 	private static final String DB_NAME = "dictionary.sqlite";
 	private SQLiteDatabase myDataBase;
 	private final Context myContext;
 	private int mRefCount;
 	private String mDbVersion;
 	
 	// Column names for use by other classes
     public static final String KEY_WORD = "word";
     public static final String KEY_DEFINITION = "definition";
     public static final String KEY_ROWID = "_id";
     public static final String KEY_IPA = "ipa";
     public static final String KEY_LETTER = "letter";
     public static final String KEY_PART = "part_of_speech";
 
     // Undo ->j and ->b substitution for the Na'vi word
     private static final String QUERY_PART_NAVI_WORD = "replace(replace(replace(replace(entries.entry_name, 'b', ''), 'j', ''), 'B', ''), 'J', '')";
     // Undo ->j and ->b substitution for the Na'vi letter
     private static final String QUERY_PART_NAVI_LETTER = "replace(replace(alpha, 'B', ''), 'J', '')";
     
     // Basic query by Na'vi word
     private static final String QUERY_PART_NAVI_START = "SELECT _id, " + QUERY_PART_NAVI_WORD + " AS word, " + QUERY_PART_NAVI_LETTER + " AS letter, entries.english_definition AS definition FROM entries ";
     private static final String QUERY_PART_NAVI_END = "ORDER BY alpha COLLATE UNICODE, entries.entry_name COLLATE UNICODE";
 
     // Query the first letter of Na'vi words
     private static final String QUERY_PART_NAVI_LETTER_START = "SELECT MIN(_id) AS _id, COUNT(*) AS _count, ' ' || " + QUERY_PART_NAVI_LETTER + " || ' ' AS letter FROM entries ";
     private static final String QUERY_PART_NAVI_LETTER_END = "GROUP BY alpha ORDER BY alpha COLLATE UNICODE";
 
     // Filter Na'vi word query
     private static final String QUERY_PART_NAVI_FILTER_WHERE = "entries.entry_name LIKE ? ";
 
     // Basic query by translation
     private static final String QUERY_PART_TO_NAVI_START = "SELECT _id, " + QUERY_PART_NAVI_WORD + " AS definition, entries.english_definition AS word, beta AS letter FROM entries ";
     private static final String QUERY_PART_TO_NAVI_END = "ORDER BY beta COLLATE UNICODE, entries.english_definition COLLATE UNICODE";
     
     // Query the first letter of translation
     private static final String QUERY_PART_TO_NAVI_LETTER_START = "SELECT MIN(_id) AS _id, COUNT(*) AS _count, ' ' || beta || ' ' AS letter FROM entries ";
     private static final String QUERY_PART_TO_NAVI_LETTER_END = "GROUP BY beta ORDER BY beta COLLATE UNICODE";
     
     // Filter translated word query
     private static final String QUERY_PART_TO_NAVI_FILTER_WHERE = "entries.english_definition LIKE ? ";
     
     // Query a single entry by ID
     private static final String QUERY_ENTRY = "SELECT _id, " + QUERY_PART_NAVI_WORD + " AS word, entries.english_definition AS definition, ipa, fps.description as part_of_speech FROM entries LEFT JOIN fancy_parts_of_speech fps USING (part_of_speech) WHERE _id = ?";
 
     // Query used by the search suggest when neither to or from Na'vi is requested
     private static final String QUERY_FOR_SUGGEST = "SELECT _id, _id AS suggest_intent_data, " + QUERY_PART_NAVI_WORD + " AS suggest_text_1, entries.english_definition AS suggest_text_2 FROM entries WHERE entries.entry_name LIKE ? OR entries.english_definition LIKE ? ORDER BY LENGTH(entries.entry_name), beta COLLATE UNICODE, entries.english_definition COLLATE UNICODE LIMIT 25";
     // Query used by the search suggest when Na'vi words are being searched
     private static final String QUERY_FOR_SUGGEST_NAVI = "SELECT _id, _id AS suggest_intent_data, " + QUERY_PART_NAVI_WORD + " AS suggest_text_1, entries.english_definition AS suggest_text_2 FROM entries WHERE entries.entry_name LIKE ? ORDER BY LENGTH(entries.entry_name), alpha COLLATE UNICODE, entries.english_definition COLLATE UNICODE LIMIT 25";
     // Query used by the search suggest when English words are being searched
     private static final String QUERY_FOR_SUGGEST_NATIVE = "SELECT _id, _id AS suggest_intent_data, " + QUERY_PART_NAVI_WORD + " AS suggest_text_2, entries.english_definition AS suggest_text_1 FROM entries WHERE entries.english_definition LIKE ? ORDER BY LENGTH(entries.english_definition), beta COLLATE UNICODE, entries.english_definition COLLATE UNICODE LIMIT 25";
     
     // Part of speech filter clauses
     public static final String FILTER_ALL = null;
     public static final String FILTER_NOUN = "(part_of_speech LIKE '%^prop.n.^%' OR part_of_speech LIKE '%^n.^%') ";
     public static final String FILTER_PNOUN = "(part_of_speech LIKE '%^pn.^%') ";
    public static final String FILTER_VERB = "(part_of_speech LIKE '%^sv%' OR part_of_speech LIKE '%^v%') ";
     public static final String FILTER_ADJ = "(part_of_speech LIKE '%^adj.^%') ";
     public static final String FILTER_ADV = "(part_of_speech LIKE '%^adv.^%') ";
     
     // Construct a query from the parts for the desired result
     private static String createQuery(boolean queryNavi, boolean queryLetter, boolean queryFilter, String queryPOS)
     {
     	StringBuffer ret = new StringBuffer();
     	
     	if (queryNavi)
     	{
     		if (queryLetter)
     			ret.append(QUERY_PART_NAVI_LETTER_START);
     		else
     			ret.append(QUERY_PART_NAVI_START);
     		if (queryFilter)
    			ret.append("WHERE " + QUERY_PART_NAVI_FILTER_WHERE);
     		if (queryPOS != null)
     		{
     			if (queryFilter)
     				ret.append("AND ");
       			else
     				ret.append("WHERE ");
 				ret.append(queryPOS);
     		}
     		if (queryLetter)
     			ret.append(QUERY_PART_NAVI_LETTER_END);
     		else
     			ret.append(QUERY_PART_NAVI_END);
     	}
     	else
     	{
     		if (queryLetter)
     			ret.append(QUERY_PART_TO_NAVI_LETTER_START);
     		else
     			ret.append(QUERY_PART_TO_NAVI_START);
     		if (queryFilter)
     			ret.append("WHERE " + QUERY_PART_TO_NAVI_FILTER_WHERE);
     		if (queryPOS != null)
     		{
     			if (queryFilter)
     				ret.append("AND ");
       			else
     				ret.append("WHERE ");
 				ret.append(queryPOS);
     		}
     		if (queryLetter)
     			ret.append(QUERY_PART_TO_NAVI_LETTER_END);
     		else
     			ret.append(QUERY_PART_TO_NAVI_END);
     	}
     	
     	return ret.toString();
     }
 
     // Private only, operated on a single instance
 	private EntryDBAdapter(Context context) {
     	super(context, DB_NAME, null, 1);
         this.myContext = context;
     }
 
 	private static EntryDBAdapter instance;
 	// Return the singleton instance
 	public static EntryDBAdapter getInstance(Context c)
 	{
 		if (instance == null)
 			reloadDB(c);
 		return instance;
 	}
 
 	// Open the DB from the source file
 	public static void reloadDB(Context c)
 	{
 		if (instance != null)
 			instance.close();
 		// Always open on the application context, otherwise the first calling activity will be leaked
 		instance = new EntryDBAdapter(c.getApplicationContext());
 		try
 		{
 			// Store the DB version once the DB is loaded
 			instance.mDbVersion = instance.createDataBase();
 		}
 		catch (Exception ex)
 		{
 			// Store a dummy version - shouldn't ever happen
 			instance.mDbVersion = "Unk";
 		}
 	}
 	
 	public String getDBVersion()
 	{
 		return mDbVersion;
 	}
 	
 	// Copy the database from the distribution if it doesn't exist, return DB version
 	private String createDataBase() throws IOException{
 		// If a DB version is returned, it's fine
     	String ret = checkDataBase();
  
     	if(ret != null){
     		//do nothing - database already exist
     	}else{
     		//By calling this method and empty database will be created into the default system path
                //of your application so we are gonna be able to overwrite that database with our database.
         	this.getReadableDatabase();
         	try {
         		// Copy the file over top of the database data
     			copyDataBase();
     			// Check again for a valid version
     			ret = checkDataBase();
     			if (ret == null)
     				ret = "Unk";
     		} catch (IOException e) {
         		throw new Error("Error copying database");
         	}
     	}
     	
     	return ret;
     }
 	
 	// Check if the database exists, returning the version if it does
     private String checkDataBase(){
     	String ret = null;
     	SQLiteDatabase checkDB = null;
 
     	try{
     		String myPath = DB_PATH + DB_NAME;
     		// Open a temporary database by path, don't allow it to create an empty database
     		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
     	}catch(SQLiteException e){
     		//database does't exist yet.
     	}
 
     	// If the database was opened, check the version
     	if(checkDB != null){
     		try
     		{
     			ret = queryDatabaseVersion(checkDB);
     		}
     		catch(SQLiteException e){
     			// An error means the version table doesn't exist, so it's not a valid DB
         		checkDB.close();
     			checkDB = null;
     		}
     	}
  
     	return ret;
     }
     
     private static String queryDatabaseVersion(SQLiteDatabase db) throws SQLiteException
     {
     	// Simple query of the database version
     	String ret;
     	Cursor c = db.rawQuery("SELECT version FROM version", null);
     	if (c.moveToFirst())
     		ret = c.getString(0);
     	else
     		ret = "Unk";
     	c.close();
     	return ret;
     }
     
     // Copy the database from the distribution
     private void copyDataBase() throws IOException{
     	 
     	//Open your local db as the input stream
     	InputStream myInput = myContext.getAssets().open(DB_NAME);
  
     	// Path to the just created empty db
     	String outFileName = DB_PATH + DB_NAME;
  
     	//Open the empty db as the output stream
     	OutputStream myOutput = new FileOutputStream(outFileName);
  
     	//transfer bytes from the inputfile to the outputfile
     	byte[] buffer = new byte[1024];
     	int length;
     	while ((length = myInput.read(buffer))>0){
     		myOutput.write(buffer, 0, length);
     	}
  
     	//Close the streams
     	myOutput.flush();
     	myOutput.close();
     	myInput.close();
     }
 
     // Open the database
     public void openDataBase() throws SQLException{
     	if (myDataBase != null)
     	{
     		mRefCount++;
     		return;
     	}
     	mRefCount = 1;
     	//Open the database
         String myPath = DB_PATH + DB_NAME;
     	myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
     }
 
     // Change passed in text into an appropriate DB filter string
     private String fixFilterString(String filter)
     {
     	return "%" + filter.toLowerCase().replace('', 'b').replace('', 'j') + "%";
     }
 
     // Perform full or filtered query, null filter returns full query
     private Cursor queryAllOrFilter(boolean naviQuery, boolean letterQuery, String filter, String partOfSpeech)
     {
     	if (filter != null)
     	{
     		return myDataBase.rawQuery(createQuery(naviQuery, letterQuery, true, partOfSpeech), new String[] { fixFilterString(filter) });
     	}
     	return myDataBase.rawQuery(createQuery(naviQuery, letterQuery, false, partOfSpeech), null);
     }
 
     // Query on all words, optionally applying a filter
     public Cursor queryAllEntries(String filter, String partOfSpeech)
     {
     	return queryAllOrFilter(true, false, filter, partOfSpeech);
     }
 
     // Query on letters for words, optionally applying a filter
     public Cursor queryAllEntryLetters(String filter, String partOfSpeech)
     {
     	return queryAllOrFilter(true, true, filter, partOfSpeech);
     }
     
     // Query on all words for X > Na'vi dictionary, optionally applying a filter
     public Cursor queryAllEntriesToNavi(String filter, String partOfSpeech)
     {
     	return queryAllOrFilter(false, false, filter, partOfSpeech);
     }
     
     // Query on English letters for words, optionally applying a filter
     public Cursor queryAllEntryToNaviLetters(String filter, String partOfSpeech)
     {
     	return queryAllOrFilter(false, true, filter, partOfSpeech);
     }
     
     // Perform a simple query to offer results for suggest
     public Cursor queryForSuggest(String filter, Boolean type)
     {
     	if (type == null) // Unspecified (Global search, or unified search)
     		return myDataBase.rawQuery(QUERY_FOR_SUGGEST, new String[] { fixFilterString(filter), "%" + filter + "%" });
     	else if (type) // Native to Na'vi
     		return myDataBase.rawQuery(QUERY_FOR_SUGGEST_NATIVE, new String[] { fixFilterString(filter) });
     	else // Na'vi to native
     		return myDataBase.rawQuery(QUERY_FOR_SUGGEST_NAVI, new String[] { "%" + filter + "%" });
     }
 
     // Return the fields for a single dictionary entry
     public Cursor querySingleEntry(int rowId)
     {
     	return myDataBase.rawQuery(QUERY_ENTRY, new String[] { Integer.toString(rowId) });
     }
     
     // Perform a refcounted close operation
     // ** This should, perhaps, be based on a subclass reference,
     //    so GC cleanup can trigger closes
     @Override
 	public synchronized void close() {
     	mRefCount--;
     	if (mRefCount > 0)
     		return;
 
     	if(myDataBase != null)
     		myDataBase.close();
 
     	instance = null;
     	super.close();
 	}
     
     @Override
 	public void onCreate(SQLiteDatabase db) {
     	// Empty database
 	}
  
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		// Probably should delete the dataabse and re-copy it
 	}
 
 	// Return if the DB is open
 	public boolean isOpen()
 	{
 		return (instance != null);
 	}
 
 	// Perform a query that intentionally returns nothing,
 	// for suggest searches without an open DB
 	public Cursor queryNull()
 	{
 		return new MatrixCursor(new String[] { "_id" });
 	}
 }
