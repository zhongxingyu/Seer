 package eu.europeana.uim.store.memory;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import eu.europeana.uim.MetaDataRecord;
 import eu.europeana.uim.TKey;
 import eu.europeana.uim.store.Request;
 
 
 /** Core and generic representation of a meta data record. 
  * 
  * @author andreas.juffinger@kb.nl
  *
  * @param <N>
  */
 public class MemoryMetaDataRecord implements MetaDataRecord {
 
 	private HashMap<TKey<?,?>, Object> fields = new HashMap<TKey<?,?>, Object>();
 	private HashMap<TKey<?,?>, Map<String, Object>> qFields = new HashMap<TKey<?,?>, Map<String, Object>>();
 
 	private long id;
 	private Request request;
 	private String identifier;
 
 	public MemoryMetaDataRecord() {
 
 	}
 
 	public MemoryMetaDataRecord(long id) {
 		this.id = id;
 	}
 
 
 	public long getId() {
 		return id;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 
 
 	@Override
 	public Request getRequest() {
 		return request;
 	}
 
 
 	public void setRequest(Request request) {
 		this.request = request;
 	}
 
 
 	@Override
 	public String getIdentifier() {
 		return identifier;
 	}
 
 	public void setIdentifier(String identifier) {
 		this.identifier = identifier;
 	}
 
 	/**
 	 * @param key
 	 * @param value
 	 */
 	@Override
 	public <N, T extends Serializable> void setFirstField(TKey<N,T> key, T value){
 		if (!fields.containsKey(key)) {
 			fields.put(key, new ArrayList<T>());
 		}
 		if (((ArrayList<T>)fields.get(key)).isEmpty()) {
 			((ArrayList<T>)fields.get(key)).add(value);
 		} else {
 			((ArrayList<T>)fields.get(key)).set(0, value);
 		}
 	}
 
 
 	@Override
 	public <N, T extends Serializable> T getFirstField(TKey<N,T> nttKey) {
 		List<T> list = getField(nttKey);
 		if (list != null && !list.isEmpty()) {
 			return list.get(0);
 		}
 		return null;
 	}
 
 	/**
 	 * @param key
 	 * @param value
 	 */
 	@Override
 	public <N, T extends Serializable> void setFirstQField(TKey<N,T> key, String qualifier, T value){
 		if (!qFields.containsKey(key)) {
 			qFields.put(key, new HashMap<String, Object>());
 		}
 		if (((ArrayList<T>)qFields.get(key)).isEmpty()) {
 			((ArrayList<T>)qFields.get(key).get(qualifier)).add(value);
 		} else {
 			((ArrayList<T>)qFields.get(key).get(qualifier)).set(0,value);
 		}
 	}
 
 
 
 	@Override
 	public <N, T extends Serializable> T getFirstQField(TKey<N,T> nttKey, String qualifier) {
 		List<T> list = getQField(nttKey, qualifier);
 		if (list != null && !list.isEmpty()) {
 			return list.get(0);
 		}
 		return null;
 	}
 
 	
 	/**
 	 * @param key
 	 * @param value
 	 */
 	@Override
 	@SuppressWarnings("unchecked")
 	public <N, T extends Serializable> void addField(TKey<N, T> key, T value){
 		if (!fields.containsKey(key)) {
 			fields.put(key, new ArrayList<T>());
 		}
 		((ArrayList<T>)fields.get(key)).add(value);
 	}
 
 
 
 	@Override
 	public <N, T extends Serializable> List<T> getField(TKey<N, T> nttKey) {
 		List<T> result = new ArrayList<T>();
 		if (fields.containsKey(nttKey)) {
 			result.addAll(((ArrayList<T>) fields.get(nttKey)));
 		}
 
 		for (TKey<?, ?> qkey : qFields.keySet()) {
 			if (qkey.equals(nttKey)) {
				Collection<Object> values = qFields.get(qkey).values();
				for (Object object : values) {
					result.add((T)object);
				}
 			}
 		} 
 		return result; 
 	}
 
 	/**
 	 * @param key
 	 * @param value
 	 */
 	@Override
 	@SuppressWarnings("unchecked")
 	public <N, T extends Serializable> void addQField(TKey<N, T> key, String qualifier, T value){
 		if (!qFields.containsKey(key)) {
 			qFields.put(key, new HashMap<String, Object>());
 		}
 
 		if (!qFields.get(key).containsKey(qualifier)) {
 			qFields.get(key).put(qualifier, new ArrayList<T>());
 		}
 		((ArrayList<T>)qFields.get(key).get(qualifier)).add(value);
 	}
 
 	@Override
 	public <N, T extends Serializable> List<T> getQField(TKey<N, T> nttKey, String qualifier) {
 		return (ArrayList<T>)qFields.get(nttKey).get(qualifier);
 	}
 
 }
