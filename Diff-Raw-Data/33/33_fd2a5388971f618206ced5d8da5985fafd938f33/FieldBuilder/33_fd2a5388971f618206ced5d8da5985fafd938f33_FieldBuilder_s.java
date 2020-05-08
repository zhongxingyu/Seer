 package com.sk.util.data;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonPrimitive;
 import com.sk.util.NameComparison;
 
 public class FieldBuilder {
 
 	private Map<String, StringBuilder> builders = new HashMap<>();
 
 	public void addTo(Map<String, String> data) {
 		for (Entry<String, StringBuilder> entry : builders.entrySet()) {
 			StringBuilder builder = entry.getValue();
 			if (!builder.toString().matches("[|]*")) {
 				data.put(entry.getKey(), builder.substring(0, builder.length() - 1));
 			}
 		}
 	}
 
 	public String[] get(String key) {
 		if (builders.containsKey(key))
 			return builders.get(key).toString().split("[|]");
 		else
 			return new String[0];
 	}
 
 	public void put(String key, Object value) {
 		StringBuilder cur = builders.containsKey(key) ? builders.get(key) : new StringBuilder();
 		if (value != null)
 			cur.append(value);
 		cur.append("|");
 		builders.put(key, cur);
 	}
 
 	public void putAll(String key, Collection<?> values) {
 		for (Object o : values) {
 			put(key, o);
 		}
 	}
 
	public void put(JsonObject source, String skey, String dkey) {
		if (source.has(skey)) {
			JsonElement value = source.get(skey);
 			if (!value.isJsonPrimitive())
				put(dkey, null);
 			if (value.isJsonPrimitive()) {
 				JsonPrimitive primitiveValue = value.getAsJsonPrimitive();
 				if (primitiveValue.isBoolean())
					put(dkey, primitiveValue.getAsBoolean());
 				else if (primitiveValue.isNumber())
					put(dkey, primitiveValue.getAsNumber());
 				else if (primitiveValue.isString())
					put(dkey, primitiveValue.getAsString());
 				else
 					throw new IllegalArgumentException();
 			}
 		} else
			put(dkey, null);
 	}
 
	public void put(JsonObject user, String key) {
		put(user, key, key);
 	}
 
 	public void joinNames() {
 		String[] first = get("firstName"), last = get("lastName");
 		for (int i = 0; i < first.length && i < last.length; ++i) {
 			put("name", first[i] + " " + last[i]);
 		}
 	}
 
 	public boolean compareNames(String... names) {
 		if (names.length != 2)
 			return false;
 		NameComparison nameUtil = NameComparison.get();
 		String[] firsts = get("firstName"), lasts = get("lastName"), boths = get("name");
 		if (firsts.length == 0 || lasts.length == 0) {
 			for (String both : boths) {
 				if (nameUtil.isSameFullName(nameUtil.parseName(both), names))
 					return true;
 			}
 		} else {
 			for (int i = 0; i < firsts.length && i < lasts.length; ++i) {
 				if (nameUtil.isSameFullName(new String[] { firsts[i], lasts[i] }, names))
 					return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public String toString() {
 		Map<String, String> tmp = new HashMap<>();
 		addTo(tmp);
 		return String.format("FieldBuilder: %s", tmp);
 	}
 
 	public boolean isEmpty() {
 		Map<String, String> tmp = new HashMap<>();
 		addTo(tmp);
 		return tmp.size() == 0;
 	}
 
 }
