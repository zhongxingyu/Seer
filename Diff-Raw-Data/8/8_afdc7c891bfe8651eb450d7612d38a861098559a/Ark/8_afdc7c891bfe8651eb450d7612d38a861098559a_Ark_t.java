 package com.daveayan.ark;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.daveayan.mirage.ReflectionUtils;
 /**
  * What is Ark?
  * 
  * 
  * @author daveayan
  *
  */
 public class Ark {
 	private Object object_to_work_with;
 	private String field_name_to_work_with;
 	
 	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Object construct_from_map(Map<String, Object> map) {
 		if(! construct_from_this_object_Q(map)) {
 			return map;
 		}
 		String class_name = (String) map.get("class_name");
 		Object instantiated_object = ReflectionUtils.objectForClassForcibly(class_name);
 		for(String key: map.keySet()) {
 			if(StringUtils.equalsIgnoreCase(key, "class_name")) {
 				continue;
 			}
 			Object value = map.get(key);
 			if(construct_from_this_object_Q(value)) {
				Object complex_object = construct_from_map((Map) value);
 				ReflectionUtils.set_value_on_field(instantiated_object, key, complex_object);
 			} else if (value instanceof List){
 				String collection_type = (String) ((Map) ((List) value).get(0)).get("collection_type");
 				Collection collection = (Collection) ReflectionUtils.objectFor(collection_type);
 				for(int i = 1; i < ((List) value).size(); i++) {
 					if(construct_from_this_object_Q(((List) value).get(i))) {
						collection.add(construct_from_map((Map) ((List) value).get(i)));
 					} else {
 						collection.add(((List) value).get(i));
 					}
 				}
 				ReflectionUtils.set_value_on_field(instantiated_object, key, collection);
 			} else {
 				ReflectionUtils.set_value_on_field(instantiated_object, key, value);
 			}
 		}
 		return instantiated_object;
 	}
 	
 	public static Ark with(Object target) {
 		return Ark.on(target);
 	}
 	
 	public static Ark on(Object target) {
 		Ark a = new Ark();
 		a.object_to_work_with = target;
 		return a;
 	}
 	
 	public Ark set(String field_name) {
 		this.field_name_to_work_with = field_name;
 		return this;
 	}
 	
 	public Ark value(Object value_to_set) {
 		ReflectionUtils.set_value_on_field(object_to_work_with, field_name_to_work_with, value_to_set);
 		return this;
 	}
 	
 	public Object get_value_on(String field_name) {
 		return ReflectionUtils.get_value_on_field(object_to_work_with, field_name);
 	}
 	
 	public Object get_final_object() {
 		return object_to_work_with;
 	}
 	
 	public Object instantiate(Class<?> clazz) {
 		return ReflectionUtils.objectForClassForcibly(clazz);
 	}
 	
 	public Object instantiate(String full_package_class_name) {
 		return ReflectionUtils.objectForClassForcibly(full_package_class_name);
 	}
 	
 	private static boolean construct_from_this_object_Q(Object object) {
 		if(object == null) {
 			return false;
 		}
 		if(object instanceof Map) {
 			Map< ?, ? > m = (Map< ?, ? >) object;
 			Object value = m.get("class_name");
 			if(value instanceof String) {
 				return true;
 			}
 		}
 		return false;
 	}
 }
