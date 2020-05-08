 /*
  *    
    Copyright 2011 Andreas Hohnholt
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    
    */
 package net.smart_json_database;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.xml.sax.SAXException;
 
import net.smart_json_database.tools.Util;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.res.AssetManager;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class JSONDatabase {
 
 	public static final String CONFIG_XML = "JSONDatabaseConfiguration.xml";
 	
 //	public static final String DB_NAME = "JSONDatabase.db"; 
 //	public static final int DB_VERSION = 1; 
 	
 	public static final String TABLE_Meta = "MetaInformations";
 	public static final String TABLE_TAG = "Tag";
 	public static final String TABLE_JSON_DATA = "JsonData";
 	public static final String TABLE_REL_TAG_JSON_DATA = "Rel_Tag_JsonData";
 	public static final String TABLE_REL_JSON_DATA_JSON_DATA = "Rel_JsonData_JsonData";
 	
 	private static final String TABLE_Meta_CreateSkript = "CREATE TABLE IF NOT EXISTS " +TABLE_Meta+
 	" (key varchar(100), value varchar(255));";
 	
 	private static final String TAG_DB_CREATE_SCRIPTE = "CREATE TABLE IF NOT EXISTS " + TABLE_TAG +
 	" (tag_uid integer primary key autoincrement, name varchar(100));";
 	
 	private static final String JSONDATA_DB_CREATE_SCRIPTE = "CREATE TABLE IF NOT EXISTS " +TABLE_JSON_DATA+
 	" (json_uid integer primary key autoincrement, createDate date, updateDate date, type varchar(100) DEFAULT " + JSONEntity.DEFAULT_TYPE + ", data text);";
 	
 	private static final String Rel_TAG_JSONDATA_DB_CREATE_SCRIPTE = "CREATE TABLE IF NOT EXISTS " +TABLE_REL_TAG_JSON_DATA+
 	" (from_id integer, to_id integer);";
 	
 	private static final String REL_JSON_DATA_JSON_DATA_DB_CREATE_SCRIPTE = "CREATE TABLE IF NOT EXISTS " +TABLE_REL_JSON_DATA_JSON_DATA+
 	" (from_id integer, to_id integer, rel_name varchar(100));";
 	
 	private static final String Fetch_BY_ID_SCRIPTE = "SELECT * FROM " + TABLE_JSON_DATA + " WHERE json_uid = ?";
 	private static final String Fetch_BY_TAG_SCRIPTE = "SELECT * FROM "+TABLE_JSON_DATA+", "+TABLE_REL_TAG_JSON_DATA+", "+TABLE_TAG+" WHERE name = ? AND from_id = tag_uid AND to_id = json_uid";
 	
 	private static JSONDatabase database = null;
 	
 	public static JSONDatabase GetDatabase(Context context) throws InitJSONDatabaseExcepiton
 	{
 		if(database == null)
 		{
 			database = new JSONDatabase(context);
 		}
 		
 		return database;
 	}
 	
 	private DBHelper dbHelper = null;
 	
 	private HashMap<String,Integer> tags = null;
 	private HashMap<Integer,String> invertedTags = null;
 	private ArrayList<IDatabaseChangeListener> listeners = null;
 	
 	private String mDbName;
 	private int mDbVersion;
 	private String mUpgradeClassPath;
 	
 	private static IUpgrade dbUpgrade;
 	
 	private JSONDatabase(Context context) throws InitJSONDatabaseExcepiton
 	{
 		
 		AssetManager assetManager = context.getAssets();			
 		InputStream stream = null;
 		
 		try {
         	
             stream = assetManager.open(CONFIG_XML);
 
         } catch (IOException e) {
         	throw new InitJSONDatabaseExcepiton("Could not load asset " + CONFIG_XML);
         }finally {
         	if (stream != null) {
         		
         		
     			
         		SAXParserFactory factory = SAXParserFactory.newInstance();
     			XMLConfigHandler handler = new XMLConfigHandler();
               	SAXParser saxparser;
 				try {
 					saxparser = factory.newSAXParser();
 					saxparser.parse(stream, handler);
 				} catch (ParserConfigurationException e) {
 					// TODO Auto-generated catch block
 					throw new InitJSONDatabaseExcepiton("Parser-Error while reading the " + CONFIG_XML,e);
 				} catch (SAXException e) {
 					// TODO Auto-generated catch block
 					throw new InitJSONDatabaseExcepiton("SAX-Error while reading the " + CONFIG_XML,e);
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					throw new InitJSONDatabaseExcepiton("IO-Error while reading the " + CONFIG_XML,e);
 				}
  	
               	mDbName = handler.getDbName();
               	  	
               	if(Util.IsNullOrEmpty(mDbName))
               	  	throw new InitJSONDatabaseExcepiton("db name is empty check the xml configuration");
               	  	
               	mDbVersion = handler.getDbVersion();
               	  	
               	if(mDbVersion < 1)
               	  	throw new InitJSONDatabaseExcepiton("db version must be 1 or greater -  check the xml configuration");
               	  	
               	mUpgradeClassPath = handler.getUpgradeClass();
         		
             	if(!Util.IsNullOrEmpty(mUpgradeClassPath))
             	{
             		try{
         	    		Class<?> x =  Class.forName(mUpgradeClassPath);
         	    		dbUpgrade = (IUpgrade)x.newInstance();
             		}catch(Exception e){}
             	}
 
             	dbHelper = new DBHelper(context, mDbName, mDbVersion);
         		listeners = new ArrayList<IDatabaseChangeListener>();
         		tags = new HashMap<String, Integer>();
         		invertedTags = new HashMap<Integer, String>();
         		updateTagMap();
         		
         	}else{
         		throw new InitJSONDatabaseExcepiton(CONFIG_XML + " is empty");
         	}
         }	
 	}
 	
 	private void updateTagMap()
 	{
 		String sql = "SELECT * FROM " + TABLE_TAG;
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		Cursor c = db.rawQuery(sql, new String[]{});
 		if(c.getCount() > 0)
 		{
 			int col_id = c.getColumnIndex("tag_uid");
 			int col_name = c.getColumnIndex("name");
 			
 			c.moveToFirst();
 			do{
 				tags.put(c.getString(col_name), new Integer(c.getInt(col_id)));
 				invertedTags.put( new Integer(c.getInt(col_id)), c.getString(col_name));
 			}while(c.moveToNext());
 		}
 		c.close();
 		db.close();
 	}
 	
 	public boolean addListener(IDatabaseChangeListener listener)
 	{
 		return listeners.add(listener);
 	}
 	
 	public boolean removeListener(IDatabaseChangeListener listener)
 	{
 		return listeners.remove(listener);
 	}
 	
 	private void notifyListenersOnEntityChange(int _id, int _changeType)
 	{
 		for(IDatabaseChangeListener listener : listeners)
 		{
 			listener.onEntityChange(_id, _changeType);
 		}
 	}
 	
 	private void notifyListenersOnTagChange(String _id, int _changeType)
 	{
 		for(IDatabaseChangeListener listener : listeners)
 		{
 			listener.onTagChange(_id, _changeType);
 		}
 	}
 	
 	public JSONEntity fetchById(int id)
 	{
 		
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		ArrayList<JSONEntity> list = fetchByRawSQL(db,Fetch_BY_ID_SCRIPTE,new String[]{""+id});
 		db.close();
 		if(list.size() > 0)
 		{
 			return list.get(0);
 		}
 		return null;
 	}
 	
 	public Collection<JSONEntity> fetchByTag(String tag)
 	{
 		
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		return fetchByRawSQL(db,Fetch_BY_TAG_SCRIPTE,new String[]{tag});
 	}
 	
 	public Collection<JSONEntity> fetchByFields(SearchFields search)
 	{
		return fetchByFields(search, null);
	}
	
	
	public Collection<JSONEntity> fetchByFields(SearchFields search, Order order)
	{
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
		ArrayList<JSONEntity> result; 
		
		if(order != null){
			result = fetchByRawSQL(db,"SELECT * FROM " + TABLE_JSON_DATA + search.toString() + order.toString(),new String[]{});
		} else {
			result = fetchByRawSQL(db,"SELECT * FROM " + TABLE_JSON_DATA + search.toString(),new String[]{});
		}
		return result;
 	}
 	
 	public Collection<JSONEntity> fetchAllEntities()
 	{
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		return fetchByRawSQL(db,"SELECT * FROM " + TABLE_JSON_DATA,new String[]{});
 	}
 	
 	public Collection<JSONEntity> fetchByType(String type)
 	{
 		if(Util.IsNullOrEmpty(type))
 			return new ArrayList<JSONEntity>();
 		
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		return fetchByRawSQL(db,"SELECT * FROM " + TABLE_JSON_DATA + " WHERE type = '" + type + "'",new String[]{});
 	}
 	
 	public Collection<JSONEntity> fetchManyByIds(Collection<Integer> ids)
 	{
 		if(ids == null)
 			return new ArrayList<JSONEntity>();
 		
 		if(ids.isEmpty())
 			return new ArrayList<JSONEntity>();
 		
 		
 		String[] whereArgs = new String[ids.size()];
 		StringBuilder builder = new StringBuilder(1000);
 		int counter = 0;
 		builder.append(" WHERE ");
 		for(Integer id : ids)
 		{
 			builder.append("json_uid = ?");
 			whereArgs[counter] = String.valueOf(id);
 			counter++;
 			if(counter < whereArgs.length)
 			{
 				builder.append(" OR ");
 			}
 		}
 		
 		
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		return fetchByRawSQL(db,"SELECT * FROM " + TABLE_JSON_DATA + builder.toString(),whereArgs);
 	}
 	
 	public int store(JSONEntity entity)
 	{
 		if(entity.getUid() == -1)
 		{
 			return insert(entity);
 		}else{
 			return update(entity);
 		}
 	}
 	
 	public int insert(JSONEntity entity)
 	{
 		
 		int returnValue = -1;
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		try{
 			db.beginTransaction();
 			ContentValues values = new ContentValues();
 			values.put("createDate", Util.DateToString(entity.getCreationDate()));
 			values.put("updateDate", Util.DateToString(entity.getUpdateDate()));
 			values.put("data", entity.getData().toString());
 			values.put("type", entity.getType());
 			int uid = Util.LongToInt(db.insert(TABLE_JSON_DATA, null, values));
 			returnValue = uid;
 			//entity.setUid(uid);
 			for(String name : entity.getTags().getToAdd())
 			{
 				int tagid = -1;
 				if(!tags.containsKey(name)){
 					tagid = insertTag(name, db);
 				}else
 				{
 					tagid = tags.get(name);
 				}
 				if(relateTagWithJsonEntity(tagid, uid, db) == -1)
 				{
 					throw new Exception("could not relate entity with tags");
 				}
 				
 				
 				for(HasMany hasMany : entity.getHasManyRelations().values())
 				{
 //					for(Integer id : hasMany.getToRemove())
 //					{
 //						deleteRelation(hasMany.getName(), uid, id, db);
 //					}
 					
 					for(Integer id : hasMany.getToAdd())
 					{
 						insertRelation(hasMany.getName(), uid, id, db);
 					}
 				}
 				
 				
 				for(BelongsTo belongsTo : entity.getBelongsToRelations().values())
 				{
 //					for(Integer id : belongsTo.getToRemove())
 //					{
 //						deleteRelation(belongsTo.getName(), id ,uid,  db);
 //					}
 					
 					for(Integer id : belongsTo.getToAdd())
 					{
 						insertRelation(belongsTo.getName(),id, uid, db);
 					}
 				}
 				
 			}
 			db.setTransactionSuccessful();
 			notifyListenersOnEntityChange(returnValue, IDatabaseChangeListener.CHANGETYPE_INSERT);
 		}catch(Exception e)
 		{
 			returnValue = -1;
 		}finally{
 			db.endTransaction();
 			db.close();
 		}
 		return returnValue;
 	}
 	
 	public int update(JSONEntity entity)
 	{
 		int returnValue = -1;
 		if(entity.getUid() == -1)
 		{
 			return returnValue;
 		}
 		
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		
 		try{
 			db.beginTransaction();	
 			entity.setUpdateDate(new Date());
 			ContentValues values = new ContentValues();
 			values.put("data", entity.getData().toString());
 			values.put("updateDate", Util.DateToString(entity.getUpdateDate()));
 			values.put("type", entity.getType());
 			String[] params = new String[]{"" + entity.getUid()};
 			db.update(TABLE_JSON_DATA, values, "json_uid = ?", params);
 			for(String name : entity.getTags().getToAdd())
 			{
 				int tagid = -1;
 				if(!tags.containsKey(name)){
 					tagid = insertTag(name, db);
 				}else
 				{
 					tagid = tags.get(name);
 				}
 				if(relateTagWithJsonEntity(tagid, entity.getUid(), db) == -1)
 				{
 					throw new Exception("could not relate");
 				}
 				
 			}
 			for(String name : entity.getTags().getToRemove())
 			{
 				int tagid = -1;
 				if(!tags.containsKey(name)){
 					continue;
 				}else
 				{
 					tagid = tags.get(name);
 				}
 				
 				db.delete(TABLE_REL_TAG_JSON_DATA, "to_id = ?", new String[]{"" + tagid});
 			}
 			
 			for(HasMany hasMany : entity.getHasManyRelations().values())
 			{
 				for(Integer id : hasMany.getToRemove())
 				{
 					deleteRelation(hasMany.getName(), entity.getUid(), id, db);
 				}
 				
 				for(Integer id : hasMany.getToAdd())
 				{
 					insertRelation(hasMany.getName(), entity.getUid(), id, db);
 				}
 			}
 			
 			
 			for(BelongsTo belongsTo : entity.getBelongsToRelations().values())
 			{
 				for(Integer id : belongsTo.getToRemove())
 				{
 					deleteRelation(belongsTo.getName(), id ,entity.getUid(),  db);
 				}
 				
 				for(Integer id : belongsTo.getToAdd())
 				{
 					insertRelation(belongsTo.getName(),id, entity.getUid(), db);
 				}
 			}
 			
 			db.setTransactionSuccessful();
 			returnValue = entity.getUid();
 			notifyListenersOnEntityChange(returnValue, IDatabaseChangeListener.CHANGETYPE_UPDATE);
 		}catch(Exception e)
 		{
 			returnValue = -1;
 		}finally{
 			db.endTransaction();
 			db.close();
 		}
 		return returnValue;
 
 	}
 	
 	public boolean delete(JSONEntity entity)
 	{
 		boolean returnValue = false;
 		if(entity.getUid() == -1)
 		{
 			return returnValue;
 		}
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		try{
 			db.beginTransaction();
 			String[] params = new String[]{"" + entity.getUid()};
 			db.delete(TABLE_REL_JSON_DATA_JSON_DATA, "from_id = ?", params);
 			db.delete(TABLE_REL_JSON_DATA_JSON_DATA, "to_id = ?", params);
 			db.delete(TABLE_REL_TAG_JSON_DATA, "to_id = ?", params);
 			db.delete(TABLE_JSON_DATA, "json_uid = ?", params);
 			db.setTransactionSuccessful();
 			notifyListenersOnEntityChange(entity.getUid(), IDatabaseChangeListener.CHANGETYPE_DELETE);
 			returnValue = true;
 		}catch(Exception e)
 		{
 				returnValue = false;
 		}finally{
 				db.endTransaction();
 				db.close();
 		}
 			
 		return returnValue;
 	}
 	
 	public Collection<String> getTagNames()
 	{
 		return tags.keySet();
 	}
 	
 	private ArrayList<JSONEntity> fetchByRawSQL(SQLiteDatabase db, String sql, String[] params)
 	{
 		ArrayList<JSONEntity> list = new ArrayList<JSONEntity>();
 		Cursor c = db.rawQuery(sql, params);
 		if(c.getCount() > 0)
 		{
 			int col_id = c.getColumnIndex("json_uid");
 			int col_createDate = c.getColumnIndex("createDate");
 			int col_updateDate = c.getColumnIndex("updateDate");
 			int col_data = c.getColumnIndex("data");
 			int col_type = c.getColumnIndex("type");
 			c.moveToFirst();
 			do{
 				try {
 					JSONEntity entity = new JSONEntity();
 					entity.setUid(c.getInt(col_id));
 					entity.setCreationDate(Util.ParseDateFromString(c.getString(col_createDate)));
 					entity.setUpdateDate(Util.ParseDateFromString(c.getString(col_updateDate)));
 					entity.setData(new JSONObject(c.getString(col_data)));
 					entity.setType(c.getString(col_type));
 					list.add(entity);
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					//e.printStackTrace();
 				}
 			}while(c.moveToNext());
 		}
 		c.close();
 		for(JSONEntity entity : list)
 		{
 			getTagsForJSONEntity(entity,db);
 			getHasManyRelationsForJSONEntity(entity,db);
 			getBelongsToRelationsForJSONEntity(entity,db);
 		}
 		return list;
 	}
 	
 	private void getHasManyRelationsForJSONEntity(JSONEntity entity, SQLiteDatabase db)
 	{
 		HashMap<String, HasMany> hasManyRelations = new HashMap<String, HasMany>();
 		String sql = "SELECT * FROM " +TABLE_REL_JSON_DATA_JSON_DATA+" WHERE from_id = ?";
 		
 		Cursor c = db.rawQuery(sql, new String[]{""+entity.getUid()});
 		if(c.getCount() > 0)
 		{
 			String name = "";
 			c.moveToFirst();
 			int col_from_id = c.getColumnIndex("to_id");
 			int col_rel_name = c.getColumnIndex("rel_name");
 			do{ 
 				name = c.getString(col_rel_name);
 				if(hasManyRelations.containsKey(name))
 				{
 					hasManyRelations.get(name).put(c.getInt(col_from_id));
 				}else
 				{
 					hasManyRelations.put(name, new HasMany(name));
 					hasManyRelations.get(name).put(c.getInt(col_from_id));
 				}
 			}while(c.moveToNext());
 		}
 		c.close();
 		entity.setHasManyRelations(hasManyRelations);
 	}
 	
 	private int insertRelation(String relName, int from_id,int to_id, SQLiteDatabase db)
 	{
 		int returnValue = -1;
 		ContentValues values = new ContentValues();
 		values.put("from_id", from_id);
 		values.put("to_id", to_id);
 		values.put("rel_name", relName);
 		returnValue = Util.LongToInt(db.insert(TABLE_REL_JSON_DATA_JSON_DATA, null, values));
 		return returnValue;
 	}
 	
 	private int deleteRelation(String relName, int from_id,int to_id, SQLiteDatabase db)
 	{
 		return db.delete(TABLE_REL_JSON_DATA_JSON_DATA, "from_id = ? AND to_id = ? AND rel_name = ?", new String[]{"" + from_id, "" + to_id, relName});
 	}
 	
 	
 	private void getBelongsToRelationsForJSONEntity(JSONEntity entity, SQLiteDatabase db)
 	{
 		HashMap<String, BelongsTo> belongsToRelations = new HashMap<String, BelongsTo>();
 		String sql = "SELECT * FROM " +TABLE_REL_JSON_DATA_JSON_DATA+" WHERE to_id = ?";
 		
 		Cursor c = db.rawQuery(sql, new String[]{""+entity.getUid()});
 		if(c.getCount() > 0)
 		{
 			String name = "";
 			c.moveToFirst();
 			int col_from_id = c.getColumnIndex("from_id");
 			int col_rel_name = c.getColumnIndex("rel_name");
 			do{ 
 				name = c.getString(col_rel_name);
 				if(belongsToRelations.containsKey(name))
 				{
 					belongsToRelations.get(name).put(c.getInt(col_from_id));
 				}else
 				{
 					belongsToRelations.put(name, new BelongsTo(name));
 					belongsToRelations.get(name).put(c.getInt(col_from_id));
 				}
 			}while(c.moveToNext());
 		}
 		c.close();
 		entity.setBelongsToRelations(belongsToRelations);
 		
 	}
 	
 	private void getTagsForJSONEntity(JSONEntity entity, SQLiteDatabase db)
 	{
 		ArrayList<String> names = new ArrayList<String>();
 		String sql = "SELECT * FROM " +TABLE_REL_TAG_JSON_DATA+" WHERE to_id = ?";
 		Cursor c = db.rawQuery(sql, new String[]{""+entity.getUid()});
 		if(c.getCount() > 0)
 		{
 			String name = "";
 			c.moveToFirst();
 			int col_from_id = c.getColumnIndex("from_id");
 			do{ 
 				name = invertedTags.get(new Integer(c.getInt(col_from_id)));
 				if(name == null)
 				{
 					continue;
 				}
 				if(names.contains(name))
 				{
 					continue;
 				}
 				names.add(name);
 			}while(c.moveToNext());
 		}
 		c.close();
 		if(names.size() > 0){
 			TagRelation relation = new TagRelation();
 			relation.init(names);
 			entity.setTags(relation);
 		}
 	}
 	
 	private int relateTagWithJsonEntity(int from, int to, SQLiteDatabase db)
 	{
 		int returnValue = -1;
 		ContentValues values = new ContentValues();
 		values.put("from_id", from);
 		values.put("to_id", to);
 		returnValue = Util.LongToInt(db.insert(TABLE_REL_TAG_JSON_DATA, null, values));
 		return returnValue;
 	}
 	
 
 	public int insertTag(String name)
 	{
 		int returnValue = -1;
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		
 		try{
 			db.beginTransaction();
 			returnValue = insertTag(name,db);
 			db.setTransactionSuccessful();
 			notifyListenersOnTagChange(name, IDatabaseChangeListener.CHANGETYPE_INSERT);
 		}catch(Exception e)
 		{
 		}finally{
 			db.endTransaction();
 			db.close();
 		}
 		return returnValue;
 	}
 	
 	private int insertTag(String name, SQLiteDatabase db)
 	{
 		int returnValue = -1;
 		
 		ContentValues value = new ContentValues();
 		value.put("name", name);
 		returnValue = Util.LongToInt(db.insert(TABLE_TAG, null, value));
 		tags.put(name, new Integer(returnValue));
 		invertedTags.put(new Integer(returnValue), name);
 		
 		return returnValue;
 	}
 	
 	public boolean deleteTag(String name)
 	{
 		boolean returnValue = false;
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		try{
 			db.beginTransaction();
 			returnValue = deleteTag(name,db);
 			db.setTransactionSuccessful();
 			notifyListenersOnTagChange(name, IDatabaseChangeListener.CHANGETYPE_DELETE);
 		}catch(Exception e)
 		{
 		}finally{
 			db.endTransaction();
 			db.close();
 		}
 		return returnValue;
 	}
 	
 	private boolean deleteTag(String name, SQLiteDatabase db) {
 		
 		int id = -1;
 		if(!tags.containsKey(name)){
 			return false;
 		}else
 		{
 			id = tags.get(name);
 		}
 		db.delete(TABLE_REL_TAG_JSON_DATA, "from_id = ?", new String[]{"" + id});
 		db.delete(TABLE_TAG, "tag_uid = ?", new String[]{"" + id});
 		tags.remove(name);
 		invertedTags.remove(new Integer(id));
 		return true;
 	}
 
 	/**
 	 * Deletes all content without the meta data
 	 * 
 	 * @return true if all delte operations are succesfull
 	 */
 	public boolean clearAllTables()
 	{
 		boolean returnValue = true;
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		try{
 			db.beginTransaction();
 			//db.execSQL("DELETE FROM " + TABLE_Meta);
 			db.execSQL("DELETE FROM " + TABLE_TAG);
 			db.execSQL("DELETE FROM " + TABLE_JSON_DATA);
 			db.execSQL("DELETE FROM " + TABLE_REL_TAG_JSON_DATA);
 			db.setTransactionSuccessful();
 		}catch(Exception e)
 		{
 			returnValue = false;
 		}finally{
 			db.endTransaction();
 			db.close();
 		}
 		return returnValue;
 	}
 	
 	public Collection<String> getPropertyKeys()
 	{
 		ArrayList<String> arrayList = new ArrayList<String>();
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		Cursor c = db.rawQuery("SELECT * FROM " + TABLE_Meta, new String[]{});
 		
 		if(c.getCount() > 0)
 		{
 			int key_col = c.getColumnIndex("key");
 			c.moveToFirst();
 	        if (c != null) {
 	          if (c.isFirst()) {
 	                 do {  
 	                	 arrayList.add(c.getString(key_col));
 	                 }while(c.moveToNext());
 	          }
 	        }
 		}
 		c.close();
 		
 		return arrayList;
 	}
 	
 	/**
 	 * Insert or update a property to db
 	 * 
 	 * @param key
 	 * @param value
 	 * @return 
 	 */
 	public long setProperty(String key, String value)
 	{
 	
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 				
 		String checkKey = Util.DateToString(new Date());
 		
 		ContentValues values = new ContentValues();
 		
 		values.put("value", value);
 		long i = -1;
 		if(checkKey.equals(getPropterty(db, key, checkKey)))
 		{
 			values.put("key",key);
 			i = db.insert(TABLE_Meta, null, values);
 		}else
 		{
 			i = db.update(TABLE_Meta, values, "key = ?", new String[]{key});
 		}
 
 		db.close();
 		
 		return i;
 	}
 	
 	/**
 	 * 
 	 * Get a property from db
 	 * 
 	 * @param key
 	 * @return a property from db or a empty string if no property is found for the given key
 	 */
 	public String getPropterty(String key)
 	{
 		return getPropterty( key, "");
 	}
 	
 	/**
 	 * Get a property from db or the defaultValue
 	 * 
 	 * @param key
 	 * @param defaultValue
 	 * @return a property from db
 	 */
 	public String getPropterty(String key, String defaultValue)
 	{
 		String returnValue = defaultValue;
 		
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		
 		returnValue = getPropterty(db,key,defaultValue);
 		
 		db.close();
 		
 		return returnValue;
 	}
 	
 	/*
 	 * return: the property or the defaultValue
 	 */
 	private String getPropterty(SQLiteDatabase db, String key, String defaultValue)
 	{
 		String returnValue = defaultValue;
 		
 		Cursor c = db.rawQuery("SELECT * FROM " + TABLE_Meta + " WHERE key = ?", new String[]{key});
 		
 		if(c.getCount() > 0)
 		{
 			//int key_col = c.getColumnIndex("key");
 			int value_col = c.getColumnIndex("value");
 			
 			c.moveToFirst();
 			  
 	        if (c != null) {
 	          if (c.isFirst()) {
 	                 do {  
 	                	 returnValue = c.getString(value_col);
 	                	 if(Util.IsNullOrEmpty(returnValue))
 	                	 {
 	                		 returnValue = defaultValue;
 	                	 }
 	                	 break;
 	                 }while(c.moveToNext());
 	          }
 	        }
 		}
 		c.close();
 				
 		return returnValue;
 	}
 	
 	
 	private class DBHelper extends SQLiteOpenHelper{
 		
 		
 
 		public DBHelper(Context context, String name,
 				int version) {
 			super(context, name, null, version);
 			// TODO Auto-generated constructor stub
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db) {
 			// TODO Auto-generated method stub
 			db.execSQL(TABLE_Meta_CreateSkript);
 			db.execSQL(TAG_DB_CREATE_SCRIPTE);
 			db.execSQL(JSONDATA_DB_CREATE_SCRIPTE);
 			db.execSQL(Rel_TAG_JSONDATA_DB_CREATE_SCRIPTE);
 			db.execSQL(REL_JSON_DATA_JSON_DATA_DB_CREATE_SCRIPTE);
 			//Check is firsttime
 			
 			Cursor c = db.rawQuery("SELECT key FROM " + TABLE_Meta, new String[0]);
 			
 			boolean found = false;
 			
 			if(c.getCount() > 0)
 			{
 				found = true;
 			}
 			
 			if(!found)
 			{
 				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
 				Date date = new Date();
 				ContentValues values = new ContentValues();
 				values.put("key","CreationTime");
 				values.put("value", dateFormat.format(date));
 				db.insert(TABLE_Meta, null, values);
 			}
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			// TODO Auto-generated method stub
 			if(dbUpgrade != null)
 			{
 				dbUpgrade.doUpgrade(db, oldVersion, newVersion);
 			}
 		}
 		
 	}
 }
