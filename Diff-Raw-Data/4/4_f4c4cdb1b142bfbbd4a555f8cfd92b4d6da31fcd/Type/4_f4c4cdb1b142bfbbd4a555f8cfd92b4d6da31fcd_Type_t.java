 package org.chai.kevin.data;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.Stack;
 import java.util.TreeMap;
 
 import javax.persistence.Embeddable;
 import javax.persistence.Transient;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONException;
 import net.sf.json.JSONNull;
 import net.sf.json.JSONObject;
 
 import org.apache.commons.lang.NotImplementedException;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.math.NumberUtils;
 import org.chai.kevin.json.JSONValue;
 import org.chai.kevin.util.Utils;
 import org.chai.kevin.value.Value;
 
 @Embeddable
 public class Type extends JSONValue {
 	
 	private static final String TYPE_STRING = "type";
 	private static final String ENUM_CODE = "enum_code";
 	private static final String LIST_TYPE = "list_type";
 	private static final String ELEMENT_TYPE = "element_type";
 	private static final String ELEMENTS = "elements";
 	private static final String KEY_NAME = "name";
 	
 	private static final String[] KEYWORDS = new String[]{TYPE_STRING, ENUM_CODE, LIST_TYPE, ELEMENT_TYPE, ELEMENTS, KEY_NAME};
 	
 	public enum ValueType {NUMBER, BOOL, STRING, TEXT, DATE, ENUM, LIST, MAP}
 
 	public Type() {super();}
 	
 	public Type(String jsonValue) {
 		super(jsonValue);
 	}
 	
 	private Type listType = null;
 	private Map<String, Type> elementMap = null;
 	private String enumCode = null;
 	private ValueType valueType = null;
 	
 	@Override
 	protected void clearCache() {
 //		throw new NotImplementedException();
 		listType = null;
 		elementMap = null;
 		enumCode = null;
 		valueType = null;
 	}
 	
 	@Override
 	@Transient
 	protected String[] getReservedKeywords() {
 		return KEYWORDS;
 	}
 	
 	@Transient
 	@Deprecated
 	public boolean isComplexType() {
 		return getType() == ValueType.LIST || getType() == ValueType.MAP;
 	}
 	
 	@Transient
 	public ValueType getType() {
 		if (valueType == null) {
 			try {
 				if (getJsonObject() != null) {
 					valueType = ValueType.valueOf(getJsonObject().getString(TYPE_STRING).toUpperCase());
 				}
 			} catch (JSONException e) {
 				valueType = null;
 			} catch (IllegalArgumentException e) {
 				valueType = null;
 			}
 		}
 		return valueType;
 	}
 	
 	@Transient
 	public String getEnumCode() {
 		if (enumCode == null) {
 			// TODO think that through
 			if (!getType().equals(ValueType.ENUM)) throw new IllegalStateException();
 			try {
 				enumCode = getJsonObject().getString(ENUM_CODE);
 			} catch (JSONException e) {
 				enumCode = null;
 			}
 		}
 		return enumCode;
 	}
 	
 	@Transient
 	public Type getListType() {
 		if (listType == null) {
 			if (!getType().equals(ValueType.LIST)) throw new IllegalStateException();
 			try {
 				listType = new Type(getJsonObject().getString(LIST_TYPE));
 			} catch (JSONException e) {
 				listType = null;
 			}
 		}
 		return listType;
 	}
 	
 	@Transient
 	public Map<String, Type> getElementMap() {
 		if (elementMap == null) {
 			if (!getType().equals(ValueType.MAP)) throw new IllegalStateException();
 			Map<String, Type> result = new LinkedHashMap<String, Type>();
 			try {
 				JSONArray array = getJsonObject().getJSONArray(ELEMENTS);
 				for (int i = 0; i < array.size(); i++) {
 					JSONObject object = array.getJSONObject(i);
 					result.put(object.getString(KEY_NAME), new Type(object.getString(ELEMENT_TYPE)));
 				}
 				elementMap = result;
 			} catch (JSONException e) {
 				elementMap = null;
 			}
 		}
 		return elementMap;
 	}
 	
 	@Transient
 	public boolean isValid() {
 		if (getType() == null) return false;
 		switch (getType()) {
 		case LIST:
 			return getListType().isValid();
 		case MAP:
 			for (Type type : getElementMap().values()) {
 				if (!type.isValid()) return false;
 			}
 			break;
 		case ENUM:
 			return getEnumCode() != null;
 		}
 		return true;
 	}
 	
 	@Transient
 	public Value getPlaceHolderValue() {
 		try {
 			JSONObject object = new JSONObject();
 			switch (getType()) {
 				case NUMBER:
 					object.put(Value.VALUE_STRING, 0);
 					break;
 				case BOOL:
 					object.put(Value.VALUE_STRING, true);
 					break;
 				case STRING:
 				case TEXT:
 					object.put(Value.VALUE_STRING, "0");
 					break;
 				case DATE:
 					object.put(Value.VALUE_STRING, "01-01-1970");
 					break;
 				case ENUM:
 					object.put(Value.VALUE_STRING, "0");
 					break;
 				case LIST:
 					JSONArray array1 = new JSONArray();
 					array1.add(getListType().getPlaceHolderValue().getJsonObject());
 					object.put(Value.VALUE_STRING, array1);
 					break;
 				case MAP:
 					Map<String, Type> elementMap = getElementMap();
 					JSONArray array = new JSONArray();
 					for (Entry<String, Type> entry : elementMap.entrySet()) {
 						JSONObject element = new JSONObject();
 						element.put(Value.MAP_KEY, entry.getKey());
 						element.put(Value.MAP_VALUE, elementMap.get(entry.getKey()).getPlaceHolderValue().getJsonObject());
 						array.add(element);
 					}
 					object.put(Value.VALUE_STRING, array);
 					break;
 				default:
 					throw new NotImplementedException();
 			}
 			return new Value(object);
 		} catch (JSONException e) {
 			return null;
 		}
 	}
 	
 	// example
 	// getType("[_].personal_information.id_number")
 	// getType(".personal_information.id_number")
 	// getType(".id_number")
 	public Type getType(String prefix) {
 		if (prefix.equals("")) return this;
 		switch (getType()) {
 		case NUMBER:
 		case BOOL:
 		case STRING:
 		case TEXT:
 		case ENUM:
 		case DATE:
 			throw new IllegalArgumentException();
 		case LIST:
			if (!prefix.matches("^\\[(\\d*|_)\\].*")) throw new IllegalArgumentException("Prefix "+prefix+" not found in type: "+this);
			return getListType().getType(prefix.replaceAll("^\\[(\\d*|_)\\]", ""));
 		case MAP:
 			boolean found = false;
 			for (Entry<String, Type> entry : getElementMap().entrySet()) {
 				if (prefix.matches("\\."+entry.getKey()+"$") 
 					|| prefix.matches("\\."+entry.getKey()+"\\..*") 
 					|| prefix.matches("\\."+entry.getKey()+"\\[_\\].*")) {
 					found = true;
 					return entry.getValue().getType(prefix.substring(entry.getKey().length()+1));
 				}
 			}
 			if (!found) throw new IllegalArgumentException("Prefix "+prefix+" not found in type: "+this);
 		default:
 			throw new NotImplementedException();
 		}
 	}
 	
 	public Object getValueToSet(String fieldValue){
 		return null;
 		
 	}
 	
 	@SuppressWarnings("unchecked")
 	private static List<Integer> getIndexList(Map<String, Object> map, String key) {
 		List<String> stringIndexList = new ArrayList<String>();
 		if (map.get(key) instanceof String[]) stringIndexList.addAll(Arrays.asList((String[])map.get(key)));
 		else if (map.get(key) instanceof Collection) stringIndexList.addAll((Collection<String>)map.get(key));
 		else if (map.get(key) instanceof String) stringIndexList.add((String)map.get(key));
 
 		List<Integer> filteredIndexList = new ArrayList<Integer>();
 		for (String suffixInBracket : stringIndexList) {
 			String index = suffixInBracket.replace("[", "").replace("]", "");
 			if (NumberUtils.isDigits(index)) filteredIndexList.add(Integer.valueOf(index));
 		}
 		return filteredIndexList;
 	}
 		
 	public interface Sanitizer {
 		
 		/**
 		 * Takes an object and returns the value corresponding to the type.
 		 * 
 		 * NUMBER -> java.lang.Number
 		 * BOOL -> java.lang.Bool
 		 * STRING -> java.lang.String
 		 * TEXT -> java.lang.String
 		 * ENUM -> java.lang.String
 		 * DATE -> java.lang.Date
 		 * 
 		 * If the value cannot be sanitized, this method should return null.
 		 * 
 		 * @param value
 		 * @param type
 		 * @param prefix
 		 * @return 
 		 */
 		public Object sanitizeValue(Object value, Type type, String prefix, String genericPrefix);
 		
 	}
 	
 	@Transient
 
 	// TODO write javadoc
 	public Value mergeValueFromMap(Value oldValue, Map<String, Object> map, String prefix, Set<String> attributes, Sanitizer sanitizer) {
 		return mergeValueFromMap(oldValue, map, prefix, prefix, attributes, sanitizer);
 	}
 	
 	@Transient
 	// TODO write javadoc
 	private Value mergeValueFromMap(Value oldValue, Map<String, Object> map, String prefix, String genericPrefix, Set<String> attributes, Sanitizer sanitizer) {
 
 		try {
 			// first we construct the jsonobject containing the value only
 			JSONObject object = new JSONObject();
 			switch (getType()) {
 				case NUMBER:
 				case BOOL:
 				case STRING:
 				case TEXT:
 				case ENUM:
 				case DATE:
 					if (!map.containsKey(prefix)) {
 						if (oldValue.isNull()) object.put(Value.VALUE_STRING, JSONNull.getInstance());
 						else object.put(Value.VALUE_STRING, oldValue.getJsonObject().get(Value.VALUE_STRING));
 					}
 					else {
 						object.put(Value.VALUE_STRING, jsonFromString(sanitizer.sanitizeValue(map.get(prefix), this, prefix, genericPrefix)));
 					}
 					break;
 				case LIST:
 					JSONArray array1 = new JSONArray();
 					if (!map.containsKey(prefix)) {
 						// we modify existing lines
 						// we don't modify the list but merge the values inside it
 						if (!oldValue.isNull()) { 
 
 							List<Integer> indexList = Type.getIndexList(map, prefix+".indexes");
 							for (int i = 0; i < oldValue.getListValue().size(); i++) {
 								if (indexList.size() > i) array1.add(getListType().mergeValueFromMap(oldValue.getListValue().get(i), map, prefix+"["+indexList.get(i)+"]", genericPrefix+"[_]", attributes, sanitizer).getJsonObject());
 							}
 						}
 					}
 					else {
 						// we add a new line to the list
 						// the list gets modified with the new indexes
 						List<Integer> filteredIndexList = Type.getIndexList(map, prefix);
 						
 						for (Integer index : filteredIndexList) {
 							Value oldListValue = null;
 							if (oldValue.isNull()) oldListValue = Value.NULL_INSTANCE();
 							else {
 								if (index < oldValue.getListValue().size()) oldListValue = oldValue.getListValue().get(index);
 								else oldListValue = Value.NULL_INSTANCE();
 							}
 							array1.add(getListType().mergeValueFromMap(oldListValue, map, prefix+"["+index+"]", genericPrefix+"[_]", attributes, sanitizer).getJsonObject());
 						}
 					}
 					if (array1.size() == 0) object.put(Value.VALUE_STRING, JSONNull.getInstance());
 					else object.put(Value.VALUE_STRING, array1);
 					break;
 				case MAP:
 					Map<String, Type> elementMap = getElementMap();
 					JSONArray array = new JSONArray();
 					for (Entry<String, Type> entry : elementMap.entrySet()) {
 						JSONObject element = new JSONObject();
 						element.put(Value.MAP_KEY, entry.getKey());
 						Value oldMapValue = null;
 						if (oldValue.isNull()) oldMapValue = Value.NULL_INSTANCE();
 						else {
 							oldMapValue = oldValue.getMapValue().get(entry.getKey());
 							if (oldMapValue == null) oldMapValue = Value.NULL_INSTANCE();
 						}
 						element.put(Value.MAP_VALUE, elementMap.get(entry.getKey()).mergeValueFromMap(oldMapValue, map, prefix+"."+entry.getKey(), genericPrefix+"."+entry.getKey(), attributes, sanitizer).getJsonObject());
 						array.add(element);
 					}
 					object.put(Value.VALUE_STRING, array);
 					break;
 				default:
 					throw new NotImplementedException();
 			}
 			
 			// then we construct a new value object and set the attributes on it
 			Value value = new Value(object);
 			for (String attribute : attributes) {
 				if (!map.containsKey(prefix)) {
 					value.setAttribute(attribute, oldValue.getAttribute(attribute));
 				}
 				else {
 					Object attributeValue = map.get(prefix+"["+attribute+"]");
 					String attributeString = String.valueOf(attributeValue);
 					if (attributeValue != null && !attributeString.isEmpty()) value.setAttribute(attribute, attributeString);
 				}
 			}
 			return value;
 		} catch (JSONException e) {
 			return null;
 		}
 	}
 	
 	private Object jsonFromString(Object value) {
 			if (value == null) return JSONNull.getInstance();
 			switch(getType()){
 			case NUMBER:
 				if(value instanceof Number)
 					return ((Number)value).doubleValue();
 				else
 					return null;
 			case BOOL:
 				if(value instanceof Boolean)
 					return (Boolean)value;
 				else
 					return null;
 			case ENUM:
 			case STRING:
 			case TEXT:
 				if(value instanceof String)
 					return escape((String)value);
 				else
 					return null;
 			case DATE:
 				if(value instanceof Date)
 					return Utils.formatDate((Date)value);
 				else
 					return null;
 			case LIST:
 			case MAP:
 				throw new IllegalArgumentException();				
 			default:
 				throw new NotImplementedException();
 			}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Transient
 	public Value getValue(Object value) {
 		if (value == null) return Value.NULL_INSTANCE();
 		try {
 			JSONObject object = new JSONObject();
 			switch (getType()) {
 				case NUMBER:
 					object.put(Value.VALUE_STRING, (Number)value);
 					break;
 				case BOOL:
 					object.put(Value.VALUE_STRING, (Boolean)value);
 					break;
 				case STRING:
 				case TEXT:
 					object.put(Value.VALUE_STRING, (String)value);
 					break;
 				case DATE:
 					object.put(Value.VALUE_STRING, (Utils.formatDate((Date)value)));
 					break;
 				case ENUM:
 					object.put(Value.VALUE_STRING, (String)value);
 					break;
 				case LIST:
 					JSONArray array1 = new JSONArray();
 					for (Object item : (List<?>)value) {
 						array1.add(getListType().getValue(item).getJsonObject());
 					}
 					if (array1.size() == 0) object.put(Value.VALUE_STRING, JSONNull.getInstance());
 					else object.put(Value.VALUE_STRING, array1);
 					break;
 				case MAP:
 					Map<String, Type> elementMap = getElementMap();
 					JSONArray array = new JSONArray();
 					for (Entry<String, Object> entry : ((Map<String, Object>)value).entrySet()) {
 						JSONObject element = new JSONObject();
 						element.put(Value.MAP_KEY, entry.getKey());
 						element.put(Value.MAP_VALUE, elementMap.get(entry.getKey()).getValue(entry.getValue()).getJsonObject());
 						array.add(element);
 					}
 					object.put(Value.VALUE_STRING, array);
 					break;
 				default:
 					throw new NotImplementedException();
 			}
 			return new Value(object);
 		} catch (JSONException e) {
 			throw new IllegalArgumentException("object "+value+" does not correspond to type "+getJsonObject(), e);
 		}
 	}
 	
 	public Value getValueFromJaql(String jaqlString) {
 		if (jaqlString == null || jaqlString.equals("null") || jaqlString.equals("\"null\"")) return Value.NULL_INSTANCE();
 		try {
 			JSONObject object = new JSONObject();
 			switch (getType()) {
 				case NUMBER:
 					if (!NumberUtils.isNumber(jaqlString))
 						throw new IllegalArgumentException("jaql string is not a number: "+jaqlString);
 					object.put(Value.VALUE_STRING, Double.parseDouble(jaqlString));
 					break;
 				case BOOL:
 					if (!jaqlString.equals("true") && !jaqlString.equals("false")) 
 						throw new IllegalArgumentException("jaql string is not a boolean: "+jaqlString);
 					object.put(Value.VALUE_STRING, Boolean.parseBoolean(jaqlString));
 					break;
 				case STRING:
 				case TEXT:
 					object.put(Value.VALUE_STRING, StringUtils.strip(jaqlString, "\""));
 					break;
 				case DATE:
 					try {
 						Date date = Utils.parseDate(StringUtils.strip(jaqlString, "\""));
 						object.put(Value.VALUE_STRING, Utils.formatDate(date));
 					} catch (ParseException e) {
 						throw new IllegalArgumentException("jaql string is not a date: "+jaqlString, e);
 					}
 					break;
 				case ENUM:
 					object.put(Value.VALUE_STRING, StringUtils.strip(jaqlString, "\""));
 					break;
 				case LIST:
 					JSONArray values = new JSONArray();
 					JSONArray array = JSONArray.fromObject(jaqlString);
 					for (int i = 0; i < array.size(); i++) {
 						String itemJaqlString = array.getString(i);
 						values.add(getListType().getValueFromJaql(itemJaqlString).getJsonObject());
 					}
 					if (values.size() == 0) object.put(Value.VALUE_STRING, JSONNull.getInstance());
 					else object.put(Value.VALUE_STRING, values);
 					break;
 				case MAP:
 					JSONObject jaqlObject = JSONObject.fromObject(jaqlString);
 					
 					Map<String, Type> elementMap = getElementMap();
 					JSONArray array1 = new JSONArray();
 					for (Entry<String, Type> entry : elementMap.entrySet()) {
 						JSONObject element = new JSONObject();
 						element.put(Value.MAP_KEY, entry.getKey());
 						element.put(Value.MAP_VALUE, entry.getValue().getValueFromJaql(jaqlObject.getString(entry.getKey())).getJsonObject());
 						array1.add(element);
 					}
 					object.put("value", array1);
 					break;
 				default:
 					throw new NotImplementedException();
 			}
 			return new Value(object);
 		} catch (JSONException e) {
 			throw new IllegalArgumentException("jaql value does not correspond to type", e);
 		}
 	}
 	
 	public String getJaqlValue(Value value) {
 		StringBuilder result = new StringBuilder();
 		if (value.isNull()) result.append("null");
 		else {
 			switch (getType()) {
 				case NUMBER:
 					result.append(value.getNumberValue().toString());
 					break;
 				case BOOL:
 					result.append(value.getBooleanValue().toString());
 					break;
 				case STRING:
 				case TEXT:
 					result.append("\""+value.getStringValue()+"\"");
 					break;
 				case DATE:
 					result.append("\""+Utils.formatDate(value.getDateValue())+"\"");
 					break;
 				case ENUM:
 					result.append("\""+value.getEnumValue()+"\"");
 					break;
 				case LIST:
 					result.append("[");
 					for (Value item : value.getListValue()) {
 						result.append(getListType().getJaqlValue(item));
 						result.append(',');
 					}
 					result.append("]");
 					break;
 				case MAP:
 					result.append("{");
 					for (Entry<String, Value> entry : value.getMapValue().entrySet()) {
 						if (getElementMap().containsKey(entry.getKey())) {
 							result.append("\""+entry.getKey()+"\"");
 							result.append(":");
 							result.append(getElementMap().get(entry.getKey()).getJaqlValue(entry.getValue()));
 							result.append(",");
 						}
 					}
 					result.append("}");
 					break;
 				default:
 					throw new NotImplementedException();
 			}
 		}
 		return result.toString();
 	}
 	
 	public void setAttribute(final Value value, final String prefix, final String attribute, final String text) {
 		// TODO throw exception if prefix does not exist
 		transformValue(value, "", new ValuePredicate() {
 			@Override
 			public boolean transformValue(Value currentValue, Type currentType, String currentPrefix) {
 				if (currentPrefix.equals(prefix)) {
 					currentValue.setAttribute(attribute, text);
 					return true;
 				}
 				return false;
 			}
 		});
 	}
 	
 	public void setValue(Value value, final String prefix, final Value toSet) {
 		// TODO throw exception if prefix does not exist
 		transformValue(value, "", new ValuePredicate() {
 			@Override
 			public boolean transformValue(Value currentValue, Type currentType, String currentPrefix) {
 				if (currentPrefix.equals(prefix)) {
 					currentValue.setJsonValue(toSet.getJsonValue());
 					return true;
 				}
 				return false;
 			}
 		});
 	}
 	
 	public String getAttribute(Value value, String prefix, String attribute) {
 		JSONValue prefixedValue = getValue(value, prefix);
 		if (prefixedValue == null) return null;
 		return prefixedValue.getAttribute(attribute);
 	}
 	
 	public Value getValue(Value value, String prefix) {
 		Value prefixedValue = getValue(prefix, value, "");
 //		if (prefixedValue == null) throw new IndexOutOfBoundsException("prefix "+prefix+" not found in value "+value);
 		return prefixedValue;
 	}
 	
 //	public	 boolean hasPrefix(Value value, String prefix) {
 //		return getValue(prefix, value, "") != null;
 //	}
 	
 	private Value getValue(String prefix, Value currentValue, String currentPrefix) {
 		if (prefix.equals(currentPrefix)) return currentValue;
 		else if (!currentValue.isNull()) {
 			switch (getType()) {
 				case LIST:
 					List<Value> values = currentValue.getListValue();
 					Type listType = getListType();
 					for (int i = 0; i < values.size(); i++) {
 						if (prefix.startsWith(currentPrefix+"["+i+"]")) return listType.getValue(prefix, values.get(i), currentPrefix+"["+i+"]");
 					}
 					break;
 				case MAP:
 					Map<String, Type> typeMap = getElementMap();
 					Map<String, Value> valueMap = currentValue.getMapValue();
 					for (Entry<String, Value> entry : valueMap.entrySet()) {
 						if (prefix.startsWith(currentPrefix+"."+entry.getKey())) return typeMap.get(entry.getKey()).getValue(prefix, entry.getValue(), currentPrefix+"."+entry.getKey());
 					}
 					break;
 				default:
 					break;
 			}
 		}
 		return null;
 	}
 	
 	public static interface ValuePredicate {
 		public boolean transformValue(Value currentValue, Type currentType, String currentPrefix);
 	}
 	
 	// depth-first transform
 	// TODO change name
 	public void transformValue(Value currentValue, ValuePredicate predicate) {
 		transformValue(currentValue, "", predicate);
 	}
 	
 	private boolean transformValue(Value currentValue, String currentPrefix, ValuePredicate predicate) {
 		boolean changed = false;
 		if (!currentValue.isNull()) {
 			try {
 				switch (getType()) {
 					case NUMBER:
 					case STRING:
 					case TEXT:
 					case DATE:
 					case ENUM:
 					case BOOL:
 //						value = currentValue;
 						break;
 					case LIST:
 						Type listType = getListType();
 						
 						List<Value> listValues = currentValue.getListValue();
 						for (int i = 0; i < listValues.size(); i++) {
 							changed = changed | listType.transformValue(listValues.get(i), currentPrefix+"["+i+"]", predicate);
 						}
 					
 						if (changed) {
 							JSONObject object1 = JSONObject.fromObject(currentValue.getJsonValue());
 							JSONArray array1 = new JSONArray();
 							for (int i = 0; i < listValues.size(); i++) {
 								array1.add(i, listValues.get(i).getJsonObject());
 							}
 							object1.put(Value.VALUE_STRING, array1);
 							currentValue.setJsonObject(object1);
 						}
 						break;
 					case MAP:
 						Map<String, Type> typeMap = getElementMap();
 						
 						Map<String, Value> mapValues = currentValue.getMapValue();
 						for (Entry<String, Value> entry : mapValues.entrySet()) {
 							Type type = typeMap.get(entry.getKey());
 							if (type != null) changed = changed | typeMap.get(entry.getKey()).transformValue(entry.getValue(), currentPrefix+"."+entry.getKey(), predicate);
 						}
 						
 						if (changed) {
 							JSONObject object2 = JSONObject.fromObject(currentValue.getJsonValue());
 							JSONArray array2 = new JSONArray();
 							for (Entry<String, Value> entry : mapValues.entrySet()) {
 								JSONObject element = new JSONObject();
 								element.put(Value.MAP_KEY, entry.getKey());
 								element.put(Value.MAP_VALUE, mapValues.get(entry.getKey()).getJsonObject());
 								array2.add(element);
 							}
 							object2.put(Value.VALUE_STRING, array2);
 							currentValue.setJsonObject(object2);
 						}
 						break;
 					default:
 						throw new NotImplementedException();
 				}
 				
 			}
 			catch(JSONException e) {
 				throw new IllegalArgumentException();
 			}
 		}
 		return changed | predicate.transformValue(currentValue, this, currentPrefix);
 	}
 
 	// use visit() instead
 	@Deprecated
 	public void getCombinations(Value value, List<String> strings, Set<List<String>> combinations, String prefix) {
 		switch (getType()) {
 			case NUMBER:
 			case BOOL:
 			case STRING:
 			case TEXT:
 			case DATE:
 			case ENUM:
 				combinations.add(strings);
 				break;
 			case LIST:
 				if (!value.isNull()) {
 					List<Value> values = value.getListValue();
 					Type listType = getListType();
 					for (int i = 0; i < values.size(); i++) {
 						combinations.add(replace(strings, prefix+"[_]", prefix+"["+i+"]"));
 						listType.getCombinations(values.get(i), strings, combinations, prefix+"["+i+"]");
 						listType.getCombinations(values.get(i), strings, combinations, prefix+"[]");
 					}
 				}
 				break;
 			case MAP:
 				if (!value.isNull()) {
 					Map<String, Type> typeMap = getElementMap();
 					for (Entry<String, Value> entry : value.getMapValue().entrySet()) {
 						typeMap.get(entry.getKey()).getCombinations(entry.getValue(), strings, combinations, prefix+"."+entry.getKey());
 					}
 					break;
 				}
 			default:
 				throw new NotImplementedException();
 		}
 	}
 
 	public static abstract class ValueVisitor {
 		
 		private SortedMap<String, Type> types = new TreeMap<String, Type>();
 		private SortedMap<String, Type> genericTypes = new TreeMap<String, Type>();
 		private Stack<Type> typeStack = new Stack<Type>();
 		
 		public Type getParent() {
 			if (typeStack.size() >= 2) return typeStack.get(typeStack.size() - 2);
 			return null;
 		}
 		
 		protected void addType(String prefix, String genericPrefix, Type type) {
 			types.put(prefix, type);
 			genericTypes.put(genericPrefix, type);
 			typeStack.add(type);	
 		}
 		
 		protected void removeType(String prefix, String genericPrefix) {
 			types.remove(prefix);
 			genericTypes.remove(genericPrefix);
 			typeStack.pop();
 		}
 		
 		public SortedMap<String, Type> getTypes() {
 			return types;
 		}
 		
 		public SortedMap<String, Type> getGenericTypes(){
 			return genericTypes;
 		}
 		
 		/**
 		 * Visitor handle method.
 		 * 
 		 * @param type
 		 * @param value is never null, can be Value.NULL_INSTANCE
 		 * @param prefix
 		 * @param genericPrefix
 		 */
 		public abstract void handle(Type type, Value value, String prefix, String genericPrefix);		
 	}
 	
 	public void visit(Value value, ValueVisitor valueVisitor) {
 		visit(value, "", "", valueVisitor);
 	}	
 	
 //	public void listVisit(int i, Value listValue, ValueVisitor valueVisitor) {
 //		String prefix = "";
 //		String genericPrefix = "";
 ////		valueVisitor.addType(prefix, genericPrefix, this);
 //		visit(listValue, prefix+"["+i+"]", genericPrefix+"[_]", valueVisitor);
 //	}
 	
 	private void visit(Value value, String prefix, String genericPrefix, ValueVisitor valueVisitor) {
 		valueVisitor.addType(prefix, genericPrefix, this);
 		if (value != null) valueVisitor.handle(this, value, prefix, genericPrefix);
 		if (value != null && !value.isNull()) {
 			switch (getType()) {
 				case NUMBER:
 				case BOOL:
 				case STRING:
 				case TEXT:
 				case DATE:
 				case ENUM:
 					break;
 				case LIST:
 					Type listType = getListType();
 					for (int i = 0; i < value.getListValue().size(); i++) {
 						listType.visit(value.getListValue().get(i), prefix+"["+i+"]", genericPrefix+"[_]", valueVisitor);
 					}
 					break;
 				case MAP:
 					for (Entry<String, Type> entry : getElementMap().entrySet()) {
 						entry.getValue().visit(value.getMapValue().get(entry.getKey()), prefix+"."+entry.getKey(), genericPrefix+"."+entry.getKey(), valueVisitor);
 					}
 					break;
 				default:
 					throw new NotImplementedException();
 			}
 		}
 		valueVisitor.removeType(prefix, genericPrefix);
 	}
 	
 	public static abstract class TypeVisitor {
 		private Stack<Type> typeStack = new Stack<Type>();
 		
 		public Stack<Type> getParents() {
 			return typeStack;
 		}
 		
 		public Type getParent() {
 			if (typeStack.size() >= 2) return typeStack.get(typeStack.size() - 2);
 			return null;
 		}
 		
 		protected void addType(String prefix, Type type) {
 			typeStack.add(type);	
 		}
 		
 		protected void removeType(String prefix) {
 			typeStack.pop();
 		}
 		
 		public abstract void handle(Type type, String prefix);
 	}
 	
 	public void visit(TypeVisitor typeVisitor) {
 		visit("", typeVisitor);
 	}
 	
 	private void visit(String prefix, TypeVisitor typeVisitor) {
 		typeVisitor.addType(prefix, this);
 		typeVisitor.handle(this, prefix);
 		switch (getType()) {
 			case NUMBER:
 			case BOOL:
 			case STRING:
 			case TEXT:
 			case DATE:
 			case ENUM:
 				break;
 			case LIST:
 				getListType().visit(prefix+"[_]", typeVisitor);
 				break;
 			case MAP:
 				for (Entry<String, Type> entry : getElementMap().entrySet()) {
 					entry.getValue().visit(prefix+"."+entry.getKey(), typeVisitor);
 				}
 				break;
 			default:
 				throw new NotImplementedException();
 		}
 		typeVisitor.removeType(prefix);
 	}
 	
 	@Deprecated
 	public Map<String, Value> getPrefixes(Value value, PrefixPredicate predicate) {
 		visit(value, predicate);
 		return predicate.prefixes;
 	}
 	
 	@Deprecated
 	public static abstract class PrefixPredicate extends ValueVisitor {
 		
 		Map<String, Value> prefixes = new HashMap<String, Value>();
 		
 		public abstract boolean holds(Type type, Value value, String prefix);
 		
 		public void handle(Type type, Value value, String prefix, String genericPrefix) {
 			if (holds(type, value, prefix)) prefixes.put(prefix, value);
 		}
 	}
 	
 	private Object escape(String string) {
 		return string.replace("\\", "").replace("\"", "");
 	}
 	
 	public String getDisplayValue(Value value) {
 		// TODO implement this
 		return getJaqlValue(value);
 	}
 	
 	private List<String> replace(List<String> strings, String toReplace, String replaceWith) {
 		List<String> result = new ArrayList<String>();
 		for (String string : strings) {
 			result.add(string.replace(toReplace, replaceWith));
 		}
 		return result;
 	}
 	
 	public String getDisplayedValue(int indent, Integer numberOfLines) {
 		return getDisplayedValue(indent, 0, numberOfLines, 1);
 	}
 		
 	private String getDisplayedValue(final int indent, int currentIndent, final Integer numberOfLines, Integer currentNumberOfLines) {
 		StringBuilder builder = new StringBuilder();
 		
 		String typeName = null;
 		switch (getType()) {
 			case NUMBER:
 			case BOOL:
 			case STRING:
 			case TEXT:
 			case DATE:
 			case LIST:
 			case MAP:
 				typeName = getType().name().toLowerCase();
 				break;
 			case ENUM:
 				typeName = getType().name().toLowerCase()+"("+getEnumCode()+")";
 				break;
 			default:
 				throw new NotImplementedException();
 		}
 		
 		builder.append(typeName);
 		
 		switch (getType()) {
 			case LIST:
 				builder.append(" : ");
 				builder.append(getListType().getDisplayedValue(indent, currentIndent+indent, numberOfLines, currentNumberOfLines));
 				break;
 			case MAP:
 				for (Entry<String, Type> entry : getElementMap().entrySet()) {
 					if (numberOfLines == null || numberOfLines > currentNumberOfLines) {
 						currentNumberOfLines = currentNumberOfLines + 1;
 						builder.append("\n");
 						builder.append(StringUtils.leftPad(entry.getKey()+" : ", entry.getKey().length()+3+currentIndent+indent));
 						builder.append(entry.getValue().getDisplayedValue(indent, currentIndent+indent, numberOfLines, currentNumberOfLines));
 					}
 					else {
 						builder.append(" ...");
 						break;
 					}
 				}
 				break;
 			default:
 				break;
 		}
 		
 		return builder.toString();
 	}
 	
 	public static Type TYPE_DATE() { return new Type("{\""+TYPE_STRING+"\":\"date\"}");}
 	public static Type TYPE_STRING() { return new Type("{\""+TYPE_STRING+"\":\"string\"}");}
 	public static Type TYPE_TEXT() { return new Type("{\""+TYPE_STRING+"\":\"text\"}");}
 	public static Type TYPE_NUMBER() { return new Type("{\""+TYPE_STRING+"\":\"number\"}");}
 	public static Type TYPE_BOOL() { return new Type("{\""+TYPE_STRING+"\":\"bool\"}");}
 	
 	public static Type TYPE_MAP (Map<String, Type> map) {
 		return TYPE_MAP(map, null);
 	}
 	
 	public static Type TYPE_MAP (Map<String, Type> map, Boolean isBlock) {
 		StringBuilder builder = new StringBuilder();
 		for (Entry<String, Type> entry : map.entrySet()) {
 			builder.append("{\""+KEY_NAME+"\":\""+entry.getKey()+"\", \""+ELEMENT_TYPE+"\":"+entry.getValue().toString()+"}");
 			builder.append(',');
 		}
 		Type type = new Type("{\""+TYPE_STRING+"\":\"map\", \""+ELEMENTS+"\":["+builder.toString()+"]}");
 		if (isBlock != null) type.setAttribute("block", isBlock.toString());
 		return type;
 	}
 
 	public static Type TYPE_LIST (Type listType) {
 		return new Type("{\""+TYPE_STRING+"\":\"list\", \""+LIST_TYPE+"\":"+listType.toString()+"}");
 	}
 
 	public static Type TYPE_ENUM (String enumCode) {
 		return new Type("{\""+TYPE_STRING+"\":\"enum\", \""+ENUM_CODE+"\":\""+enumCode+"\"}");
 	}
 
 }
