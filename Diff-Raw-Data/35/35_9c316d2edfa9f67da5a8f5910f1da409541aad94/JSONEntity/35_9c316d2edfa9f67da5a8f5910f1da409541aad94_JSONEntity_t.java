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
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 
import net.smart_json_database.tools.Util;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class JSONEntity {
 
 	public static final String DEFAULT_TYPE = "jsonEntity";
 	
 	private JSONObject data;
 	private int uid;
 	private Date creationDate;
 	private Date updateDate;
 	private String type;
 	private TagRelation tags;
 	private HashMap<String, HasMany> hasManyRelations;
 	private HashMap<String, BelongsTo> belongsToRelations;
 	
 	public JSONEntity()
 	{
 		uid = -1;
 		creationDate = new Date();
 		updateDate = new Date();
 		tags = new TagRelation();
 		data = new JSONObject();
 		type = DEFAULT_TYPE;
 		hasManyRelations = new HashMap<String, HasMany>();
 		belongsToRelations = new HashMap<String, BelongsTo>();
 	}
 	
 	public JSONEntity(String type)
 	{
 		uid = -1;
 		creationDate = new Date();
 		updateDate = new Date();
 		tags = new TagRelation();
 		data = new JSONObject();
 		this.type = type;
 		hasManyRelations = new HashMap<String, HasMany>();
 		belongsToRelations = new HashMap<String, BelongsTo>();
 	}
 	
 	protected JSONEntity(JSONObject data, int uid, Date creationDate,
 			Date updateDate, TagRelation tags, String type) {
 		super();
 		this.data = data;
 		this.uid = uid;
 		this.creationDate = creationDate;
 		this.updateDate = updateDate;
 		this.tags = tags;
 		this.type = type;
 		hasManyRelations = new HashMap<String, HasMany>();
 		belongsToRelations = new HashMap<String, BelongsTo>();
 	}
 	
 	protected void setHasManyRelations(HashMap<String, HasMany> hasManyRelations)
 	{
 		this.hasManyRelations = hasManyRelations;
 	}
 	
 	protected void setBelongsToRelations(HashMap<String, BelongsTo> belongsToRelations)
 	{
 		this.belongsToRelations = belongsToRelations;
 	}
 	
 	protected  HashMap<String, HasMany> getHasManyRelations()
 	{
 		return hasManyRelations;
 	}
 	
 	protected HashMap<String, BelongsTo> getBelongsToRelations()
 	{
 		return belongsToRelations;
 	}
 	
 	/**
 	 * 
 	 * @param relName Name of the relation
 	 * @return a list of related entities ids
 	 */
 	public Collection<Integer> hasMany(String relName)
 	{
 		if(hasManyRelations.containsKey(relName)){
 			return hasManyRelations.get(relName).getAll().toCollection();
 		}else
 		{
 			return new ArrayList<Integer>();
 		}
 	}
 	
 	public void addIdToHasMany(String relName, Integer id)
 	{
 		if(hasManyRelations.containsKey(relName)){	
 			hasManyRelations.get(relName).add(id);
 		}else
 		{
 			hasManyRelations.put(relName, new HasMany(relName));
 			hasManyRelations.get(relName).add(id);
 		}
 	}
 	
 	public void removeIdFromHasMany(String relName, Integer id)
 	{
 		if(hasManyRelations.containsKey(relName)){	
 			hasManyRelations.get(relName).remove(id);
 		}
 	}
 	
 	public boolean containsHasManyRelation(String relName)
 	{
 		return hasManyRelations.containsKey(relName);
 	}
 
 	/**
 	 * 
 	 * @param relName Name of the relation
 	 * @return a list of related entities ids
 	 */
 	public Collection<Integer> belongsTo(String relName)
 	{
 		if(belongsToRelations.containsKey(relName)){
 			return belongsToRelations.get(relName).getAll().toCollection();
 		}else
 		{
 			return new ArrayList<Integer>();
 		}
 	}
 	
 	public void addIdToBelongsTo(String relName, Integer id)
 	{
 		if(belongsToRelations.containsKey(relName)){	
 			belongsToRelations.get(relName).add(id);
 		}else
 		{
 			belongsToRelations.put(relName, new BelongsTo(relName));
 			belongsToRelations.get(relName).add(id);
 		}
 	}
 	
 	public void removeIdFromBelongsTo(String relName, Integer id)
 	{
 		if(belongsToRelations.containsKey(relName)){	
 			belongsToRelations.get(relName).remove(id);
 		}
 	}
 	
 	public boolean containsBelongsToRelation(String relName)
 	{
 		return belongsToRelations.containsKey(relName);
 	}	
 	
 	
 	public TagRelation getTags() {
 		return tags;
 	}
 
 	protected void setTags(TagRelation tags) {
 		this.tags = tags;
 	}
 
 	public int getUid() {
 		return uid;
 	}
 
 	protected void setUid(int uid) {
 		this.uid = uid;
 	}
 	
 	
 
 	public String getType() {
 		
 		if(Util.IsNullOrEmpty(type))
 			return DEFAULT_TYPE;
 		
 		return type;
 	}
 
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	public Date getCreationDate() {
 		return creationDate;
 	}
 
 	protected void setCreationDate(Date creationDate) {
 		this.creationDate = creationDate;
 	}
 
 	public Date getUpdateDate() {
 		return updateDate;
 	}
 
 	protected void setUpdateDate(Date updateDate) {
 		this.updateDate = updateDate;
 	}
 
 	public boolean getBoolean(String key) throws JSONException {
 		return data.getBoolean(key);
 	}
 
 	public double getDouble(String key) throws JSONException {
 		return data.getDouble(key);
 	}
 
 	public int getInt(String key) throws JSONException {
 		return data.getInt(key);
 	}
 
 	public JSONArray getJSONArray(String key) throws JSONException {
 		return data.getJSONArray(key);
 	}
 
 	public JSONObject getJSONObject(String key) throws JSONException {
 		return data.getJSONObject(key);
 	}
 
 	public long getLong(String key) throws JSONException {
 		return data.getLong(key);
 	}
 
 	public String getString(String key) throws JSONException {
 		return data.getString(key);
 	}
 
 	public boolean has(String key) {
 		return data.has(key);
 	}
 
 	public boolean isNull(String key) {
 		return data.isNull(key);
 	}
 
 	public Collection<String> dataKeys() {
 		ArrayList<String> keys = new ArrayList<String>();
 		Iterator<?> it = data.keys();
 		while(it.hasNext())
 		{
 			keys.add(it.next().toString());
 		}
 		return keys;
 	}
 
 	public JSONObject put(String key, boolean value) throws JSONException {
 		return data.put(key, value);
 	}
 
 	public JSONObject put(String key, double value) throws JSONException {
 		return data.put(key, value);
 	}
 
 	public JSONObject put(String key, int value) throws JSONException {
 		return data.put(key, value);
 	}
 
 	public JSONObject put(String key, long value) throws JSONException {
 		return data.put(key, value);
 	}
 
 	public JSONObject put(String key, Object value) throws JSONException {
 		if(value == null)
 		{
 			throw new JSONException("value is null");
 		}
 		return data.put(key, value);
 	}
 	
 	public JSONObject put(String key, String value) throws JSONException {
 		if(value == null)
 		{
 			throw new JSONException("value is null");
 		}
 		return data.put(key, value);
 	}
 
 	public Object remove(String key) {
 		return data.remove(key);
 	}
 
 	public JSONObject getData() {
 		return data;
 	}
 
 	protected void setData(JSONObject data) {
 		this.data = data;
 	}
 	
 	@Override
 	public String toString() {
 		// TODO Auto-generated method stub
 		JSONObject object = new JSONObject();
 		try {
 			object.put("uid", uid);
 			object.put("type", type);
 			object.put("creationDate", creationDate.getTime());
 			object.put("updateDate", updateDate.getTime());
 			object.put("data", data);
 			object.put("tags", new JSONArray(tags.getAll().toCollection()));
 			
 			ArrayList<JSONObject> relationsArray = new ArrayList<JSONObject>();
 			for(HasMany hasMany : hasManyRelations.values())
 			{
 				JSONObject relObj = new JSONObject();
 				relObj.put("name",hasMany.getName());
 				relObj.put(hasMany.getName(), new JSONArray(hasMany.getAll().toCollection()));
 				relationsArray.add(relObj);
 			}
 			
 			object.put("hasMany", new JSONArray(relationsArray));
 	
 			relationsArray = new ArrayList<JSONObject>();
 			for(BelongsTo belongsTo : belongsToRelations.values())
 			{
 				JSONObject relObj = new JSONObject();
 				relObj.put("name", belongsTo.getName());
 				relObj.put(belongsTo.getName(), new JSONArray(belongsTo.getAll().toCollection()));
 				relationsArray.add(relObj);
 			}
 			
 			object.put("belongsTo", new JSONArray(relationsArray));
 			
 			return object.toString();
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			return null;
 		}
 	}
 	
 	public static JSONEntity ParseFromString(String s) throws JSONException
 	{
 		JSONObject object = new JSONObject(s);
 		JSONEntity jsonEntity = new JSONEntity(object.getString("type"));
 		jsonEntity.setUid(object.getInt("uid"));
 		jsonEntity.setCreationDate(new Date(object.getLong("creationDate")));
 		jsonEntity.setUpdateDate(new Date(object.getLong("updateDate")));
 		jsonEntity.setData(new JSONObject(object.getString("data")));
 		if(object.has("tags"))
 		{
 			JSONArray jsonArray = object.getJSONArray("tags");
 			for(int i = 0; i < jsonArray.length(); i++)
 			{
 				jsonEntity.tags.put(jsonArray.getString(i));
 			}
 		}
 		
 		if(object.has("hasMany"))
 		{
 			JSONArray jsonArray = object.getJSONArray("hasMany");
 			for(int i = 0; i < jsonArray.length(); i++)
 			{
 				
 				JSONObject relObj = jsonArray.getJSONObject(i);
 				String name = relObj.getString("name");
 				JSONArray relArray = relObj.getJSONArray(name);
 				if(!jsonEntity.hasManyRelations.containsKey(name))
 				{
 					jsonEntity.hasManyRelations.put(name, new HasMany(name));
 				}
 				for(int j = 0; j < relArray.length(); j++)
 				{
 					jsonEntity.hasManyRelations.get(name).put(relArray.getInt(j));
 				}
 			}
 		}
 		
 		if(object.has("belongsTo"))
 		{
 			JSONArray jsonArray = object.getJSONArray("belongsTo");
 			for(int i = 0; i < jsonArray.length(); i++)
 			{
 				JSONObject relObj = jsonArray.getJSONObject(i);
 				String name = relObj.getString("name");
 				JSONArray relArray = relObj.getJSONArray(name);
 				if(!jsonEntity.belongsToRelations.containsKey(name))
 				{
 					jsonEntity.belongsToRelations.put(name, new BelongsTo(name));
 				}
 				for(int j = 0; j < relArray.length(); j++)
 				{
 					jsonEntity.belongsToRelations.get(name).put(relArray.getInt(j));
 				}
 			}
 		}
 		
 		return jsonEntity;
 	}
 	
 }
