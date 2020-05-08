 package net.wrap_trap.monganez;
 
 import java.io.Serializable;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 import java.util.regex.Pattern;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.bson.BSONObject;
 import org.bson.types.BSONTimestamp;
 import org.bson.types.Binary;
 import org.bson.types.Code;
 import org.bson.types.CodeWScope;
 import org.bson.types.ObjectId;
 import org.bson.types.Symbol;
 
 public class BSONObjectMapper {
 
 	/** CLASS_NAME */
 	public static final String CLASS_NAME = "class";
 	/** COLLECTION_CLASS_NAME */
 	public static final String COLLECTION_CLASS_NAME = "collectionClass";
 	/** COLLECTION_VALUE */
 	public static final String COLLECTION_VALUE = "collectionValue";
 
 	private Map<Object, BSONObject> cached = new HashMap<Object, BSONObject>();
 
 	private Set<Object> entries = new HashSet<Object>();
 	
 	private BSONObjectFactory factory = null;
 	
 	public BSONObjectMapper(){
 		this.factory = new DefaultBSONObjectFactory();
 	}
 	
 	public BSONObjectMapper(BSONObjectFactory factory){
 		this.factory = factory;
 	}
 
 	@SuppressWarnings("rawtypes")
 	public BSONObject encode(Object target) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException{
 		if(target instanceof Map){
 			return encodeMap((Map)target);	
 		}else if(target instanceof Iterable){
 			return encodeIterable((Iterable)target);
 		}else if(target instanceof Serializable){
 			return encodeSerializable((Serializable)target);
 		}
 		throw new RuntimeException(target.getClass().getName() + " is not (java.util.Map||java.util.Iterable||java.io.Serializable).");
 	}
 
 	@SuppressWarnings("rawtypes")
 	protected BSONObject encodeMap(Map map) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException{
 		if(entries.contains(map)){
 			return null;
 		}
 		entries.add(map);
 		BSONObject ret = factory.createBSONObject();
 		for(Object key : map.keySet()){
 			Object target = map.get(key);
 			if(isAcceptableValue(target)){
 				ret.put(key.toString(), target);
 			}else{
 				ret.put(key.toString(), encode(target));
 			}
 		}
 		return ret;
 	}
 	
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	protected BSONObject encodeIterable(Iterable target) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
 		if(entries.contains(target)){
 			return null;
 		}
 		entries.add(target);
 
 		List<Object> list = factory.createBSONList();
 		for(Object object : (Iterable<Object>)target){
 			if(isAcceptableValue(object)){
 				list.add(object);
 			}else{
 				list.add(encode(object));
 			}
 		}
 		BSONObject bsonObject = factory.createBSONObject();
 		bsonObject.put(COLLECTION_VALUE, list);
 		bsonObject.put(COLLECTION_CLASS_NAME, target.getClass().getName());
 		return bsonObject;
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	protected BSONObject encodeSerializable(Serializable target) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException{
 		if(entries.contains(target)){
 			return null;
 		}
 		entries.add(target);
 
 		Map nestedMap = PropertyUtils.describe(target);
 		nestedMap.put(CLASS_NAME, target.getClass().getName());
 		BSONObject bsonObject = encodeMap(nestedMap);
 		return bsonObject;	
 	}
 	
 	protected boolean isAcceptableValue(Object val){
         return (
     			val == null
     			|| val instanceof Date
     			|| val instanceof Number
     			|| val instanceof String
     			|| val instanceof ObjectId
     			|| val instanceof BSONObject
     			|| val instanceof Boolean
     			|| val instanceof Pattern
     			|| val instanceof byte[]
     			|| val instanceof Binary
     			|| val instanceof UUID
     			|| val.getClass().isArray()
     			|| val instanceof Symbol
     			|| val instanceof BSONTimestamp
     			|| val instanceof CodeWScope
     			|| val instanceof Code
     	);
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public Map toMap(BSONObject target) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
 		if(cached.containsKey(target)){
 			return (Map)cached.get(target);
 		}
 		
 		Map map = new HashMap();
 		for(String key : target.keySet()){
 			Object value = target.get(key);
 			if(value instanceof BSONObject){
 				BSONObject bsonObject = (BSONObject)value;
 				if(bsonObject.containsField(COLLECTION_CLASS_NAME)){
 					Collection restoredCollection = toCollection(bsonObject);
 					map.put(key, restoredCollection);
 				}else{
 					Object object = toObject((BSONObject)value);
 					map.put(key, object);
 				}
 			}else{
 				map.put(key, value);
 			}
 		}
 		return map;
 	}
 	
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public Collection toCollection(BSONObject target) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
 		if(cached.containsKey(target)){
 			return (Collection)cached.get(target);
 		}
 		List<Object> list = new ArrayList<Object>();
		for(Object o : (List<Object>)target.get(COLLECTION_VALUE)){
 			if(o instanceof BSONObject){
 				BSONObject bsonObject = (BSONObject)o;
 				if(bsonObject.containsField(COLLECTION_CLASS_NAME)){
 					Collection restoredCollection = toCollection(bsonObject);
 					list.add(restoredCollection);
 				}else{
 					Object object = toObject((BSONObject)o);
 					list.add(object);
 				}
 			}else{
 				list.add(o);
 			}
 		}
 		Class<?> collectionClass = Class.forName((String)target.get(COLLECTION_CLASS_NAME));
 		Collection collection = (Collection)collectionClass.newInstance();
 		collection.addAll(list);
 		return collection;
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public Object toObject(BSONObject bsonObject) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException, InstantiationException{
 		Map restoredMap = toMap((BSONObject)bsonObject);
 		if(restoredMap.containsKey(CLASS_NAME)){
 			String className = (String)restoredMap.get(CLASS_NAME);
 			Class<?> clazz = Class.forName(className);
 			Object restoredObject = (Serializable)clazz.newInstance();
 			PropertyUtils.copyProperties(restoredObject, restoredMap);
 			return restoredObject;
 		}
 		return restoredMap;
 	}
 }
