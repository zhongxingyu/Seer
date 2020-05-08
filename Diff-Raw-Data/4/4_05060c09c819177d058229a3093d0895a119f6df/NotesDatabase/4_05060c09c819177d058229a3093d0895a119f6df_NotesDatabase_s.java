 package io.trigger.forge.android.modules.database;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 
 public class NotesDatabase extends FetchDB{
 
 	//static final String[] TABLE_NAMES = new String[]{"Notes","NoteTag","NoteContacts","NoteEmail","NoteURL"};
 	static final int MAIN = 0,
 	TAGS = 1,
 	CONTACTS = 2,
 	EMAILS = 3,
 	URLS = 4;
 
 	static final String CREATE = "CREATE TABLE ";
 
 	static final String TEXT_COL = "text",
 	HTML_COL = "html",
 	LOC_COL = "local_id",
 	SERV_COL = "server_id",
 	TIME_COL = "last_updated",
 	STATUS_COL = "sync_status",
 	TAG_COL = "tag",
 	CONTACT_COL = "contact",
 	EMAIL_COL = "email",
 	URL_COL = "url";
 
 
 	static final String LOC_ID = LOC_COL+" INTEGER PRIMARY KEY ";
 
 	static final String[] NOTE_COLS = new String[]{TEXT_COL,HTML_COL,LOC_COL,SERV_COL,TIME_COL,STATUS_COL};
 
 	static final int TEXT = 0,
 			HTML = 1,
 			LOCAL_ID = 2,
 			SERVER_ID = 3,
 			TIMESTAMP = 4,
 			STATUS = 5;
 
 	static final String[] SCHEMA = new String[]{'('+TEXT_COL+" TEXT, "+HTML_COL+" TEXT,"+LOC_ID+", "
 									+SERV_COL+" TEXT UNIQUE, "+TIME_COL+" TEXT, "+STATUS_COL+" TEXT )",
 		'('+LOC_COL+", "+TAG_COL+" TEXT)",
 		'('+LOC_COL+", "+CONTACT_COL+" TEXT)",
 		'('+LOC_COL+','+EMAIL_COL+" TEXT)",
 		'('+LOC_COL+','+URL_COL+" TEXT)",
 
 	};
 
 
 	//In all likelihood, none of this^ shit is needed
 	
 	private static String[] CREATE_TABLE_QUERIES = null;
 	private static String[] TABLE_NAMES = null;
 	
 	public static void setQueries(JSONArray schema) throws JSONException{
 		int length = schema.length();
 		TABLE_NAMES = new String[length];
 		CREATE_TABLE_QUERIES = new String[length];
 		for(int i = 0; i < length; i++){
 			JSONObject info = schema.getJSONObject(i);
 			TABLE_NAMES[i] = info.getString("name");
 			CREATE_TABLE_QUERIES[i] = "CREATE TABLE "+info.getString("name")+" "+info.getString("schema");
 			Log.e("tables init'ed", CREATE_TABLE_QUERIES[i]);
 		}
 	}
 	
 	public NotesDatabase(Context context) {
 		super(context,"Main");
 		Log.e("woot woot","called constructor!");
 		open();//won't be created until we do this!
 		close();
 	}
 
 
 	public void onCreate(SQLiteDatabase db) {
 		create_tables(db);
 	}
 	
 	public void createTables(JSONArray tables) throws SQLException, JSONException{
 		open();
 		Log.e("create tables","non-fresh create tables called");
 		for(int i = 0; i < tables.length(); i++){
 			JSONObject table = tables.getJSONObject(i);
 			db.execSQL("create table if not exists "+table.getString("name")+
 					' '+table.getString("schema"));
 		}
 		close();
 	}
 
 	private void create_tables(SQLiteDatabase db){
 		Log.e("create tables","create tables called");
 		for(String name : CREATE_TABLE_QUERIES) db.execSQL(name+';');
 		this.db = db;
 	}
 
 
 
 	public  void dropTables(JSONArray tables) throws SQLException, JSONException{
 		open();
 		for(String name:toArray(tables))db.execSQL("drop table "+name+';');
 		close();
 	}
 
 	
 
  	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
 		//LULZ
 	}
 
 
  	public synchronized JSONArray queryToObjects(String query) throws JSONException{
  		return queryToObjects(query, true);
  	}
 	
 	//Takes a string, returns a JSONArray of JSONObjects
 	public synchronized JSONArray queryToObjects(String query, boolean atomic) throws JSONException{
 		if(atomic) open();
 		Cursor c = db.rawQuery(query, null);//the actual querying happens
 		JSONArray notes = cursorToArray(c);
 		c.close();
 		if(atomic) close();
 		return notes;
 	}
 	
 	private String[] toArray(JSONArray strings) throws JSONException{
 		String[] results = new String[strings.length()];
 		for(int i = 0; i < results.length; i++) results[i] = strings.getString(i);
 		return results;
 		
 	}
 
 	public synchronized int writeQuery(String query, JSONArray args) throws SQLException, JSONException{
 		db.execSQL(query,toArray(args));
 		
 		String column= "last_insert_rowid()";
 		
 		Cursor c = db.rawQuery("SELECT "+column+" from Notes", null);
 										//this^ is the worst shit ever
 
 		c.moveToFirst();
 		int result = c.getInt(c.getColumnIndex(column));
 		
 		c.close();
 		return result;
 	}
 	
 	private Object get(Cursor c, int index) {
 		switch(c.getType(index)){
 			case Cursor.FIELD_TYPE_BLOB:
 				return c.getBlob(index);
 			case Cursor.FIELD_TYPE_FLOAT:
 				return c.getFloat(index);
 			case Cursor.FIELD_TYPE_INTEGER:
 				return c.getInt(index);
 			case Cursor.FIELD_TYPE_STRING:
 				return c.getString(index);
 			case Cursor.FIELD_TYPE_NULL:
 			default:
 				return null;
 		}
 	}
 	
 	private JSONArray cursorToArray(Cursor c) throws JSONException{
 		final String[] columnNames = c.getColumnNames();
 		JSONArray results = new JSONArray();
 		
 		for (c.moveToFirst();!c.isAfterLast();c.moveToNext()){
 			JSONObject object = new JSONObject();
 			for(String name : columnNames){
 				int index = c.getColumnIndex(name);
 				object.put(name, get(c, index));
 			}
 			results.put(object);
 		}
 		
 		return results;
 	}
 
 }
 
 	
