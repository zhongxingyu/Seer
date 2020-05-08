 package suite.util;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * Converts (supposedly) any Java structures to a recursive map.
  * 
  * @author ywsing
  */
 public class InspectUtil {
 
 	private static InspectUtil instance = new InspectUtil();
 
 	private Map<Class<?>, List<Field>> fieldsByClass = new ConcurrentHashMap<>();
 
 	private InspectUtil() {
 	}
 
 	public static Object mapify(Object object) {
 		Object result;
 
 		if (object != null) {
 			Class<?> clazz = object.getClass();
 
 			if (clazz.isPrimitive() //
 					|| clazz.isEnum() //
 					|| clazz == Boolean.class //
 					|| clazz == String.class //
 					|| Number.class.isAssignableFrom(clazz))
 				result = object;
 			else {
 				Map<Object, Object> map;
 
 				if (clazz.isArray())
 					map = instance.mapifyCollection(Arrays.asList((Object[]) object));
 				else if (Set.class.isAssignableFrom(clazz))
 					map = instance.mapifyCollection(new TreeSet<>((Set<?>) object));
 				else if (Collection.class.isAssignableFrom(clazz))
 					map = instance.mapifyCollection((Collection<?>) object);
 				else if (Map.class.isAssignableFrom(clazz)) {
 					map = new HashMap<>();
 					for (Entry<?, ?> entry : ((Map<?, ?>) object).entrySet())
 						map.put(mapify(entry.getKey()), mapify(entry.getValue()));
 				} else {
 					map = new HashMap<>();
 					for (Field field : instance.getFields(object))
 						try {
 							map.put(mapify(field.getName()), mapify(field.get(object)));
 						} catch (IllegalAccessException ex) {
 							throw new RuntimeException(ex);
 						}
 				}
 
 				map.put("@class", clazz.getCanonicalName());
 				result = map;
 			}
 		} else
 			result = null;
 
 		return result;
 	}
 
 	public static Object unmapify(Class<?> targetClazz, Object object) {
 		Object result;
 
 		if (object != null) {
 			Class<?> clazz = object.getClass();
 
 			if (clazz.isPrimitive() //
 					|| clazz.isEnum() //
 					|| clazz == Boolean.class //
 					|| clazz == String.class //
 					|| Number.class.isAssignableFrom(clazz))
 				result = targetClazz.cast(object);
 			else
 				try {
 					@SuppressWarnings("unchecked")
 					Map<Object, Object> map = (Map<Object, Object>) object;
					Class<?> targetClazz1 = targetClazz != null ? targetClazz : Class.forName(map.get("@class").toString());
 
 					if (targetClazz1.isArray()) {
 						ArrayList<Object> list = new ArrayList<>();
 						instance.unmapifyCollection(map, list);
 						result = list.toArray(new Object[list.size()]);
 					} else if (Collection.class.isAssignableFrom(targetClazz1)) {
 						@SuppressWarnings("unchecked")
 						Collection<Object> col = (Collection<Object>) targetClazz1.newInstance();
 						instance.unmapifyCollection(map, col);
 						result = col;
 					} else if (Map.class.isAssignableFrom(targetClazz1)) {
 						Map<Object, Object> map1 = new HashMap<>();
 						for (Entry<?, ?> entry : ((Map<?, ?>) object).entrySet())
							map1.put(unmapify(null, entry.getKey()), unmapify(null, entry.getValue()));
 						result = map1;
 					} else {
 						result = targetClazz1.newInstance();
 						for (Field field : instance.getFields(result))
 							field.set(result, map.get(unmapify(field.getType(), field.getName())));
 					}
 				} catch (ReflectiveOperationException ex) {
 					throw new RuntimeException(ex);
 				}
 		} else
 			result = null;
 
 		return result;
 	}
 
 	private Map<Object, Object> mapifyCollection(Collection<?> col) {
 		int i = 0;
 		Map<Object, Object> map = new HashMap<>();
 		for (Object elem : col)
 			map.put(i++, mapify(elem));
 		return map;
 	}
 
 	private void unmapifyCollection(Map<Object, Object> map, Collection<Object> col) {
 		int i = 0;
 		while (map.containsKey(i))
 			col.add(unmapify(null, map.get(i++)));
 	}
 
 	public static <T> boolean equals(T o0, T o1) {
 		boolean result;
 		if (o0 != o1)
 			result = o0 != null && o1 != null //
 					&& o0.getClass() == o1.getClass() //
 					&& Util.equals(instance.list(o0), instance.list(o1));
 		else
 			result = true;
 		return result;
 	}
 
 	public static int hashCode(Object object) {
 		return instance.list(object).hashCode();
 	}
 
 	private List<Object> list(Object object) {
 		List<Object> list = new ArrayList<>();
 
 		for (Field field : getFields(object))
 			try {
 				list.add(field.get(object));
 			} catch (IllegalAccessException ex) {
 				throw new RuntimeException(ex);
 			}
 
 		return list;
 	}
 
 	private List<Field> getFields(Object object) {
 		return getFields(object.getClass());
 	}
 
 	private List<Field> getFields(Class<?> clazz) {
 		List<Field> ci = fieldsByClass.get(clazz);
 		if (ci == null)
 			fieldsByClass.put(clazz, ci = getFields0(clazz));
 		return ci;
 	}
 
 	private List<Field> getFields0(Class<?> clazz) {
 		Class<?> superClass = clazz.getSuperclass();
 		List<Field> parentFields = superClass != null ? getFields(superClass) : Collections.<Field> emptyList();
 		List<Field> fields = new ArrayList<>(parentFields);
 
 		for (Field field : clazz.getDeclaredFields()) {
 			int modifiers = field.getModifiers();
 
 			if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
 				field.setAccessible(true);
 				fields.add(field);
 			}
 		}
 
 		return fields;
 	}
 
 }
