 package org.omo.core;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class IntrospectiveMetadata<K, V> implements Metadata<K, V> {
 
 	private final Class<?> clazz;
 	private final List<String> attributeNames;
 	private int keyIndex;
 	//private final List<Method> getters;
 	
 	public IntrospectiveMetadata(Class<?> clazz, String keyName) throws SecurityException, NoSuchMethodException {
 		this.clazz = clazz;
 		keyName = "get"+keyName;
 		attributeNames = new ArrayList<String>();
 		//getters = new ArrayList<Method>();
 		for (Method method : clazz.getMethods()) {
 			// is it a getter ?
 			String name = method.getName();
 			Class<?>[] parameterTypes = method.getParameterTypes();
 			if (name.startsWith("get") &&
 				!method.getDeclaringClass().equals(Object.class) &&
 				(parameterTypes==null || parameterTypes.length==0)) {
 				if (name.equals(keyName))
 					keyIndex = attributeNames.size();
 				attributeNames.add(name.substring(3));
 				//getters.add(method);
 			}
 		}
 	}
 	
 	@Override
 	public V getAttributeValue(V value, int index) {
 		try {
 			//Method method = getters.get(index);
			Method method = clazz.getMethod("get"+attributeNames.get(index), null);
			return (V)method.invoke(value, null);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public List<String> getAttributeNames() {
 		return attributeNames;
 	}
 
 	@Override
 	public K getKey(V value) {
 		try {
 			return (K)getAttributeValue(value, keyIndex);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 }
