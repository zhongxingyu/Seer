 package com.isawabird.db;
 
 import java.util.Date;
 import java.util.ArrayList;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 import com.isawabird.BirdList;
 import com.isawabird.Consts;
 import com.isawabird.ISawABirdException;
 import com.isawabird.Sighting;
 import com.isawabird.Species;
 import com.isawabird.Utils;
 import com.isawabird.parse.ParseUtils;
 
 public class DBHandler extends SQLiteOpenHelper {
 
 	private static DBHandler mInstance = null; 
 	private SQLiteDatabase db;
 
 
 	public static synchronized  DBHandler getInstance(Context ctx) {
 
 		// Use the application context, which will ensure that you 
 		// don't accidentally leak an Activity's context.
 		// See this article for more information: http://bit.ly/6LRzfx
 		if (mInstance == null) {
 			mInstance = new DBHandler(ctx.getApplicationContext());
 		}
 		return mInstance;
 	}
 
 	private DBHandler(Context ctx) {
 		super(ctx, DBConsts.DATABASE_NAME, null, DBConsts.DATABASE_VERSION);
 
 		if(db == null) db = getWritableDatabase();
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase database) {
 
 		this.db = database;
 
 		Log.i(Consts.TAG, "in onCreate db");
 		Log.i(Consts.TAG, DBConsts.CREATE_LIST);
 		Log.i(Consts.TAG, DBConsts.CREATE_SIGHTING);
 		try {
 			db.beginTransaction();
 			db.execSQL(DBConsts.CREATE_LIST);
 			db.execSQL(DBConsts.CREATE_SIGHTING);
 			db.setTransactionSuccessful();
 		} catch (Exception e) {
 			Log.e(Consts.TAG, "exception: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 			db.endTransaction();
 		}
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/* Get all sightings for a given list */
 	public ArrayList<Sighting> getSightingsByListName(String listName, String username) {
 
 		if(!db.isOpen()) db = getWritableDatabase();
 
 		Cursor result = db.rawQuery(
 				DBConsts.QUERY_SIGHTINGS_BY_LISTNAME, 
 				new String [] { listName});
 
 		if(result.getColumnCount() <= 0) return null;
 
 		ArrayList<Sighting> sightings = new ArrayList<Sighting>();
 
 		while(result.moveToNext()){
 			Sighting s = new Sighting(result.getString(result.getColumnIndexOrThrow(DBConsts.SIGHTING_SPECIES)));
 			s.setId(result.getLong(result.getColumnIndexOrThrow(DBConsts.ID)));
 			s.setDate(new Date(result.getInt(result.getColumnIndexOrThrow(DBConsts.SIGHTING_DATE))));
 			s.setListName(result.getString(result.getColumnIndexOrThrow(DBConsts.LIST_NAME)));
 			s.setLatitude(result.getFloat(result.getColumnIndexOrThrow(DBConsts.SIGHTING_LATITUDE)));
 			s.setLongitude(result.getFloat(result.getColumnIndexOrThrow(DBConsts.SIGHTING_LONGITUDE)));
 			s.setParseObjectID(result.getString(result.getColumnIndexOrThrow(DBConsts.PARSE_OBJECT_ID)));
 
 			sightings.add(s);
 		}
 
 		return sightings;
 	}
 
 	/* Get all sightings for current list */
 	//TODO: do we really need this method?
 	public ArrayList<Sighting> getSightingsForCurrentList(){
 		return getSightingsByListName(Utils.getCurrentListName(), ParseUtils.getCurrentUsername());
 	}
 
 	/* Add a sighting to a given list */
 	public long addSighting(Sighting sighting, long listId, String username) throws ISawABirdException { 
 		if(!db.isOpen()) db = getWritableDatabase();
 
 		if(sighting == null) {
 			throw new RuntimeException("Sighting = " + sighting + ", listId = " + listId);
 		}
 
 		long result = -1;
 		if(!isSightingExist(sighting.getSpecies().getFullName(), listId, username)) {
 			try {
 				Log.i(Consts.TAG, "Adding new species to table: " + sighting.getSpecies().getFullName());
 
 				ContentValues values = new ContentValues();
 				values.put(DBConsts.SIGHTING_SPECIES, sighting.getSpecies().getFullName());
 				values.put(DBConsts.SIGHTING_LIST_ID, listId);
 				values.put(DBConsts.SIGHTING_DATE, sighting.getDate().getTime());
 				values.put(DBConsts.SIGHTING_LATITUDE, sighting.getLatitude());
 				values.put(DBConsts.SIGHTING_LONGITUDE, sighting.getLongitude());								
 				values.put(DBConsts.SIGHTING_NOTES, sighting.getNotes());
 				values.put(DBConsts.PARSE_IS_UPLOAD_REQUIRED, DBConsts.TRUE);
 				values.put(DBConsts.PARSE_IS_DELETE_MARKED, DBConsts.FALSE);
 
 				result = db.insertOrThrow(DBConsts.TABLE_SIGHTING, null, values);
 
 			} catch(SQLiteException ex) {
 				throw new ISawABirdException(ex.getMessage());
 			}
 		} else{
 			Log.w(Consts.TAG, sighting.getSpecies() + " not added to list with listID: " + listId + ", usrename: " + Utils.getCurrentListName());
 			throw new ISawABirdException(ISawABirdException.ERR_SIGHTING_ALREADY_EXISTS); 
 		}
 		return result;
 	}
 
 	/* Add a sighting to the current active list */
 	//TODO: do we really need this method?
 	public long addSightingToCurrentList(String species) throws ISawABirdException{
 		Sighting sighting = new Sighting(new Species(species));
 		try{
 			return addSighting(sighting, Utils.getCurrentListID(), ParseUtils.getCurrentUsername());
 		}catch(ISawABirdException ex){
 			throw ex; 
 		}
 	}
 
 	public boolean isSightingExist(String species, long listId,
 			String username) {
 		if(!db.isOpen()) db = getWritableDatabase();
 
 		Cursor result = db.rawQuery(
 				DBConsts.QUERY_IS_SIGHTINGS_EXIST, 
 				new String [] { Long.toString(listId), species });
 		Log.i(Consts.TAG, "isSightingExist: " + result.getCount());
 		return (result.getCount() != 0);
 	}
 
 	/* Create a new list for this user */
 	public long  addBirdList(BirdList birdList, boolean setCurrentList) throws ISawABirdException{
 		Log.i(Consts.TAG, " >> addBirdList"); 
 		if(!db.isOpen()) db = getWritableDatabase();
 
 		ContentValues values = new ContentValues();
 		values.put(DBConsts.LIST_NAME, birdList.getListName()); 
 		values.put(DBConsts.LIST_USER, birdList.getUsername());
 		values.put(DBConsts.LIST_DATE, birdList.getDate().getTime());
 		values.put(DBConsts.LIST_NOTES, birdList.getNotes());
 		values.put(DBConsts.PARSE_IS_UPLOAD_REQUIRED, 1);
 		values.put(DBConsts.PARSE_IS_DELETE_MARKED, 0); 
 		
 		long result = -1;
 		try{
 			result = db.insertOrThrow(DBConsts.TABLE_LIST, null, values);
 
 			if (result == -1){
 				Log.e(Consts.TAG, "Error occurred");
 				return result; 
 			}
 			
 			if (setCurrentList){
 				Utils.setCurrentList(birdList.getListName(), result);
 			}
 		}catch(SQLiteException ex){
 			Log.e(Consts.TAG, "Error occurred adding a new table " + ex.getMessage());
 			throw new ISawABirdException("Unable to create a new list. Perhaps, a list by the name already exists ?");
 		}
 		return result;
 	}
 
 	public long getBirdCountByListId(long listId) {
 		if(!db.isOpen()) db = getWritableDatabase();
 
 		return DatabaseUtils.queryNumEntries(db, DBConsts.TABLE_SIGHTING,
				DBConsts.SIGHTING_LIST_ID + "=?", new String[] {Long.toString(listId)});
 	}
 
 	public long getBirdCountForCurrentList() {
 		return getBirdCountByListId(Utils.getCurrentListID());
 	}
 	/* Get the lists for the current user */
 	public ArrayList<BirdList> getBirdLists(String username){
 
 		if(!db.isOpen()) db = getWritableDatabase();
 
 		// TODO: use username
 		Cursor result = db.rawQuery(
 				DBConsts.QUERY_LIST, null);
 
 		if(result.getColumnCount() <= 0) return null;
 
 		ArrayList<BirdList> birdList = new ArrayList<BirdList>();
 
 		while(result.moveToNext()) {
 			BirdList temp = new BirdList(result.getString(result.getColumnIndexOrThrow(DBConsts.LIST_NAME)));
 			Log.v(Consts.TAG, "Found list " + result.getString(result.getColumnIndexOrThrow(DBConsts.LIST_NAME)));
 			temp.setDate(new Date(result.getInt(result.getColumnIndexOrThrow(DBConsts.LIST_DATE))));
 			temp.setNotes(result.getString(result.getColumnIndexOrThrow(DBConsts.LIST_NOTES)));
 			temp.setUsername(username);
 			temp.setParseObjectID(result.getString(result.getColumnIndexOrThrow(DBConsts.PARSE_OBJECT_ID)));
 			temp.setId(result.getLong(result.getColumnIndexOrThrow(DBConsts.ID)));
 			birdList.add(temp);
 		}
 
 		return birdList;
 	}
 
 	public ArrayList<BirdList> getBirdListToSync(String username) {
 
 		if(!db.isOpen()) db = getWritableDatabase();
 
 		Cursor result = db.rawQuery(DBConsts.QUERY_LIST_SYNC, null);
 
 		if(result.getColumnCount() <= 0) return null;
 
 		ArrayList<BirdList> birdList = new ArrayList<BirdList>();
 
 		while(result.moveToNext()) {
 			BirdList temp = new BirdList(result.getString(result.getColumnIndexOrThrow(DBConsts.LIST_NAME)));
 			temp.setDate(new Date(result.getInt(result.getColumnIndexOrThrow(DBConsts.LIST_DATE))));
 			temp.setNotes(result.getString(result.getColumnIndexOrThrow(DBConsts.LIST_NOTES)));
 			temp.setUsername(username);
 			temp.setId(result.getInt(result.getColumnIndexOrThrow(DBConsts.ID)));
 			temp.setParseObjectID(result.getString(result.getColumnIndexOrThrow(DBConsts.PARSE_OBJECT_ID)));
 			temp.setMarkedForDelete(result.getInt(result.getColumnIndexOrThrow(DBConsts.PARSE_IS_DELETE_MARKED)) == 1);
 			temp.setMarkedForUpload(result.getInt(result.getColumnIndexOrThrow(DBConsts.PARSE_IS_UPLOAD_REQUIRED)) == 1);
 			birdList.add(temp);
 		}
 		
 		Log.i(Consts.TAG, "We have " + birdList.size() + " lists to sync");
 		return birdList;
 	}
 
 	public ArrayList<Sighting> getSightingsToSync(String username) {
 
 		if(!db.isOpen()) db = getWritableDatabase();
 
 		Cursor result = db.rawQuery(DBConsts.QUERY_SIGHTINGS_SYNC, null);
 
 		if(result.getColumnCount() <= 0) return null;
 
 		ArrayList<Sighting> sightings = new ArrayList<Sighting>();
 
 		while(result.moveToNext()) {
 			Sighting temp = new Sighting(result.getString(result.getColumnIndexOrThrow(DBConsts.SIGHTING_SPECIES)));
 			temp.setDate(new Date(result.getInt(result.getColumnIndexOrThrow(DBConsts.SIGHTING_DATE))));
 			temp.setNotes(result.getString(result.getColumnIndexOrThrow(DBConsts.SIGHTING_NOTES)));
 			temp.setId(result.getInt(result.getColumnIndexOrThrow(DBConsts.ID)));
 			// TODO : Add list name instead of list ID 
 			temp.setListName(result.getString(result.getColumnIndexOrThrow(DBConsts.SIGHTING_LIST_ID))); 
 			temp.setLatitude(result.getDouble(result.getColumnIndexOrThrow(DBConsts.SIGHTING_LATITUDE)));
 			temp.setLongitude(result.getDouble(result.getColumnIndexOrThrow(DBConsts.SIGHTING_LONGITUDE)));
 			temp.setParseObjectID(result.getString(result.getColumnIndexOrThrow(DBConsts.PARSE_OBJECT_ID)));
 			temp.setMarkedForDelete(result.getInt(result.getColumnIndexOrThrow(DBConsts.PARSE_IS_DELETE_MARKED)) == 1);
 			temp.setMarkedForUpload(result.getInt(result.getColumnIndexOrThrow(DBConsts.PARSE_IS_UPLOAD_REQUIRED)) == 1);
 			sightings.add(temp);
 		}
 		
 		Log.i(Consts.TAG, "We have " + sightings.size() + " sightings to sync");
 		return sightings;
 	}
 
 	public void deleteList(String listName){
 		
 		try{
 			long listId = getListIDByName(listName);
 			
 			if(!db.isOpen()) db = getWritableDatabase();
 			
 			/* Do not actually delete. Just mark isMarkedDelete = 1(true) */
 			ContentValues values = new ContentValues();
 			values.put(DBConsts.PARSE_IS_DELETE_MARKED, 1); 
 			db.update(DBConsts.TABLE_SIGHTING, values, DBConsts.SIGHTING_LIST_ID + "=" + listId , null); 
 			
 			/* Next delete the list from the LIST table */ 
 			db.update(DBConsts.TABLE_LIST, values, DBConsts.ID + "=" + listId, null);
 			
 			if (listId == Utils.getCurrentListID()){
 				Utils.setCurrentList("", -1);
 			}
 		}catch(ISawABirdException ex){
 			// TODO Handle properly
 			ex.printStackTrace(); 
 		}
 		
 	}
 	
 	public void deleteSightingFromCurrentList(String species){
 		if(!db.isOpen()) db = getWritableDatabase();
 		ContentValues values = new ContentValues();
 		values.put(DBConsts.PARSE_IS_DELETE_MARKED, 1);
 		db.update(DBConsts.TABLE_SIGHTING, values, DBConsts.QUERY_DELETE_SIGHTING, 
 				new String[] {species, String.valueOf(Utils.getCurrentListID()) });
 	}
 	
 	public void deleteSightingFromList(String species, String listName ){
 		if(!db.isOpen()) db = getWritableDatabase();
 		
 		try{
 			long listId = getListIDByName(listName);
 			
 			ContentValues values = new ContentValues();
 			values.put(DBConsts.PARSE_IS_DELETE_MARKED, 1);
 			db.update(DBConsts.TABLE_SIGHTING, values, DBConsts.QUERY_DELETE_SIGHTING, 
 					new String[] {species, String.valueOf(listId) });
 			
 		}catch(ISawABirdException ex){
 			// TODO : Handle properly. No list by the name is found 
 			ex.printStackTrace(); 
 		}
 	}
 	
 	public long getListIDByName(String listName) throws ISawABirdException{
 		if(!db.isOpen()) db = getWritableDatabase();
 		
 		String query = DBConsts.LIST_NAME + "=\"" + listName + "\""; 
 		Cursor result = db.query(DBConsts.TABLE_LIST, new String[] { DBConsts.ID} , query , null,null, null, null); 
 		/* List name is unique */ 
 		if (result.moveToNext()){
 			Log.i(Consts.TAG, "ID of list " + listName + " is " + result.getLong(0)); 
 			return result.getLong(0); // hard code because we query for only one column
 		}else{
 			throw new ISawABirdException("No list found in the database"); 
 		}
 	}
 	
 	public boolean updateParseObjectID(String tableName, long id, String parseObjectId){
 		if(!db.isOpen()) db = getWritableDatabase();
 		
 		try{
 			ContentValues values = new ContentValues(); 
 			values.put(DBConsts.PARSE_OBJECT_ID, parseObjectId); 
 			values.put(DBConsts.PARSE_IS_UPLOAD_REQUIRED, 0);
 			Log.i(Consts.TAG, " Updating Parse object id for " + tableName + " id = " + id);
 			db.update(tableName, values, DBConsts.ID + "=" + id, null);
 			return true; 
 		}catch(Exception ex){
 			//TODO : Handle exception
 			ex.printStackTrace();
 		}
 		return false;
 	}
 
 	
 	public boolean resetUploadRequiredFlag(String tableName, long id){ 
 		if(!db.isOpen()) db = getWritableDatabase();
 		
 		try{
 			ContentValues values = new ContentValues(); 
 			values.put(DBConsts.PARSE_IS_UPLOAD_REQUIRED, 0);
 			
 			db.update(tableName, values, DBConsts.ID + "=" + id, null);
 			return true; 
 		}catch(Exception ex){
 			//TODO : Handle exception
 			ex.printStackTrace();
 		}
 		return false;
 	}
 	
 	public boolean deleteLocally(String tableName, long id){ 
 		if(!db.isOpen()) db = getWritableDatabase();
 		
 		try{
 			db.delete(tableName, DBConsts.ID + "=" + id, null);
 			return true; 
 		}catch(Exception ex){
 			//TODO : Handle exception
 			ex.printStackTrace();
 		}
 		return false;
 	}
 	
 	/*public ArrayList<BirdList> getBirdListToSync(boolean toCreate, String username) {
 		if(!db.isOpen()) db = getWritableDatabase();
 
 		String query = null;
 		if(toCreate) {
 			query = DBConsts.QUERY_LIST_SYNC_CREATE;
 		} else {
 			query = DBConsts.QUERY_LIST_SYNC_UPDATE;
 		}
 
 		Cursor result = db.rawQuery(query, null);
 
 		if(result.getColumnCount() <= 0) return null;
 
 		ArrayList<BirdList> birdList = new ArrayList<BirdList>();
 
 		while(result.moveToNext()) {
 			BirdList temp = new BirdList(result.getString(result.getColumnIndexOrThrow(DBConsts.LIST_NAME)));
 			temp.setDate(new Date(result.getInt(result.getColumnIndexOrThrow(DBConsts.LIST_DATE))));
 			temp.setNotes(result.getString(result.getColumnIndexOrThrow(DBConsts.LIST_NOTES)));
 			temp.setUsername(username);
 			temp.setId(result.getInt(result.getColumnIndexOrThrow(DBConsts.ID)));
 			if(!toCreate) {
 				temp.setParseObjectID(result.getString(result.getColumnIndexOrThrow(DBConsts.PARSE_OBJECT_ID)));
 			}
 
 			birdList.add(temp);
 		}
 		return birdList;
 	}*/
 	
 	public void dumpTable(String tableName){
 		if(!db.isOpen()) db = getWritableDatabase();
 		Cursor res = db.query(tableName, null, null, null, null, null, null);
 		
 		String dumpString = ""; 
 		for (int i = 0 ; i < res.getColumnCount(); i++){
 			dumpString += res.getColumnName(i) + " | " ;  
 		}
 		Log.i(Consts.TAG, "Dumping contents of table " + tableName);
 		Log.i(Consts.TAG, dumpString); 
 
 		while (res.moveToNext()){
 			dumpString = "" ;
 			for (int i = 0 ; i < res.getColumnCount(); i++){
 				int type = res.getType(i); 
 				switch (type){
 				case Cursor.FIELD_TYPE_STRING:
 					dumpString += res.getString(i) + " | ";
 					break;
 				case Cursor.FIELD_TYPE_INTEGER:
 					dumpString += res.getInt(i)+ " | ";
 					break; 
 				case Cursor.FIELD_TYPE_FLOAT:
 					dumpString += res.getFloat(i) + " | ";
 					break;
 				default:
 					break;
 				}
 			}
 			Log.i(Consts.TAG, dumpString); 
 		}
 	}
 	
 	public void clearTable(String  tableName){
 		if(!db.isOpen()) db = getWritableDatabase();
 
 		db.delete(tableName, null, null);
 	}
 }
