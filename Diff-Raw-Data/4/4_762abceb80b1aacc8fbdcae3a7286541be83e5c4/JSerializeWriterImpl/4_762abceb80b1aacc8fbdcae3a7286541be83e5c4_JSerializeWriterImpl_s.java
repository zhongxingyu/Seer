 package exesoft;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.lang.Boolean;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeMap;
 
 import java.util.Iterator;
 
 public final class JSerializeWriterImpl implements JSerializeWriter {
 
 private JModel parses = new JModelImpl();
 
 	private static Map<String, String> knownHashes = new HashMap<String, String>();
 
 	private Map<String, String> aliases = new HashMap<String, String>(); // to be considered
 	
 															
 	private Map<String, String>fieldsToConsider = new HashMap<String, String>();
 	
 	public JSerializeWriterImpl() {
 		fieldsToConsider.put(String.class.getName(), "value");
 		fieldsToConsider.put(List.class.getName(), "elementData");
 		fieldsToConsider.put(ArrayList.class.getName(), "elementData");
 		fieldsToConsider.put(Boolean.class.getName(), "valueboolean");
 		fieldsToConsider.put(ArrayDeque.class.getName(), "elements");
 		fieldsToConsider.put(HashMap.class.getName(), "table");
 		fieldsToConsider.put(LinkedList.class.getName(), "first,next,prev");
 		fieldsToConsider.put(TreeMap.class.getName(), "root,parent,right,left");
 		fieldsToConsider.put(TreeMap.class.getName() + "$Entry",
 				"root,parent,right,left,key,value");
 		fieldsToConsider.put(HashSet.class.getName(), "map");
 		fieldsToConsider.put(Integer.class.getName(), "value");
 
 		// fieldsToConsider.put(LinkedList.class.getName()+"#Node",
 		// "first,next,prev");
 		// ADD MORE COMMON TYPES USED IN JAVA AND THEIR FIELDS
 	}
 	
 	
 
 	public Map<String, Object> prepareMap(final Object ob) {
 		Map<String, Object> map = new HashMap<String, Object>();
 		if (ob != null) {
 			map = toMap(ob);
 			map.put("#JSerializeMetaData#RootClassName", ob.getClass()
 					.getName());
 		} else {
 			map.put("0", "0"); // represenation of null references
 		}
 		return map;
 	}
 	public Map<String , String> getAliases() { 
 		return aliases;  
 		}
 	
 	public Map<String, String> getKnownHashes() { 
 		return knownHashes;  
 		}
 	public Map<String, String> getFieldsToConsider() {
 		return fieldsToConsider;
 	}
 	public JModel getParser() { 
 		return parses; 
 		}
 	
 	
 			
 
 	Map<String, Object> toMap(final Object ob) {
 
 		Map<String, Object> map = new HashMap<String, Object>();
 		if (ob != null) {
 			@SuppressWarnings("rawtypes")
 			Class c = ob.getClass();
 			for (Field field : getFields(c)) {
 				setPublic(field);
 				Object value;
 
 				if (ob instanceof List
 						|| ob instanceof Map
 						|| ob instanceof LinkedList
 						|| getTypeName(field).equals(
 								"java.util.LinkedList$Node")
 						|| ob instanceof Set) {
 					if (shouldISkipThisField(c, field, false)) {
 						continue;
 					}
 				} else if (shouldISkipThisField(c, field, true)) {
 					continue;
 				}
 
 				try {
 					value = field.get(ob);
 				} catch (Exception e1) {
 					continue;
 				}
 
 				if (isPrimitive(field)) {
 					map.put(field.getName() + "#" + getTypeName(field), value);
 				} else {
 
 					int valueHash = System.identityHashCode(value);
 					String hashString = Integer.toString(valueHash);
 					String typeName = getTypeName(field);
 					if (typeName.startsWith("[I")) {
 						List<Integer> lista = new ArrayList<Integer>();
 						for (int i = 0; i < ((int[]) value).length; i++) {
 							lista.add((Integer) (((int[]) value))[i]);
 						}
 						if (knownHashes.containsKey(hashString)) {
 							map.put(field.getName() + "#intArray",
 									knownHashes.get(hashString));
 						} else {
 							knownHashes.put(hashString, field.getName());
 							map.put(field.getName() + "#intArray", lista);
 						}
 						continue;
 					}
 
 					if (typeName.startsWith("[S")) {
 						List<Short> lista = new ArrayList<Short>();
 						for (int i = 0; i < ((short[]) value).length; i++) {
 							lista.add((Short) (((short[]) value))[i]);
 						}
 						if (knownHashes.containsKey(hashString)) {
 							map.put(field.getName() + "#shortArray",
 									knownHashes.get(hashString));
 						} else {
 							knownHashes.put(hashString, field.getName());
 							map.put(field.getName() + "#shortArray", lista);
 						}
 						continue;
 					}
 
 					if (typeName.startsWith("[B")) {
 						List<Boolean> lista = new ArrayList<Boolean>();
 						for (int i = 0; i < ((boolean[]) value).length; i++) {
 							lista.add((Boolean) (((boolean[]) value))[i]);
 						}
 						if (knownHashes.containsKey(hashString)) {
 							map.put(field.getName() + "#booleanArray",
 									knownHashes.get(hashString));
 						} else {
 							knownHashes.put(hashString, field.getName());
 							map.put(field.getName() + "#booleanArray", lista);
 						}
 						continue;
 					}
 
 					if (typeName.startsWith("[J")) {
 						List<Long> lista = new ArrayList<Long>();
 						for (int i = 0; i < ((long[]) value).length; i++) {
 							lista.add((Long) (((long[]) value))[i]);
 						}
 						if (knownHashes.containsKey(hashString)) {
 							map.put(field.getName() + "#longArray",
 									knownHashes.get(hashString));
 						} else {
 							knownHashes.put(hashString, field.getName());
 							map.put(field.getName() + "#longArray", lista);
 						}
 						continue;
 					}
 
 					if (typeName.startsWith("[F")) {
 						List<Float> lista = new ArrayList<Float>();
 						for (int i = 0; i < ((float[]) value).length; i++) {
 							lista.add((Float) (((float[]) value))[i]);
 						}
 						if (knownHashes.containsKey(hashString)) {
 							map.put(field.getName() + "#floatArray",
 									knownHashes.get(hashString));
 						} else {
 							knownHashes.put(hashString, field.getName());
 							map.put(field.getName() + "#floatArray", lista);
 						}
 						continue;
 					}
 
 					if (typeName.startsWith("[C")) {
 						List<Character> lista = new ArrayList<Character>();
 						for (int i = 0; i < ((char[]) value).length; i++) {
 							lista.add((Character) (((char[]) value))[i]);
 						}
 						if (knownHashes.containsKey(hashString)) {
 							map.put(field.getName() + "#charArray",
 									knownHashes.get(hashString));
 						} else {
 							knownHashes.put(hashString, field.getName());
 							map.put(field.getName() + "#charArray", lista);
 						}
 						continue;
 					}
 
 					if (typeName.startsWith("[L")) {
 
 						if (ob instanceof Map) {
 							Map<Object, Object> mapa = new HashMap<Object, Object>();
 
 							@SuppressWarnings({ "unchecked", "rawtypes" })
 							Iterator<Entry<Object, Object>> it = ((Map) ob)
 									.entrySet().iterator();
 							while (it.hasNext()) {
 								@SuppressWarnings("rawtypes")
 								Map.Entry pairs = (Map.Entry) it.next();
 								mapa.put(toMap(pairs.getKey()),
 										toMap(pairs.getValue()));
 								it.remove();
 							}
 							if (knownHashes.containsKey(hashString)) {
 								map.put(field.getName() + "#"
 										+ getTypeName(field),
 										knownHashes.get(hashString));
 							} else {
 								knownHashes.put(hashString, field.getName());
 								map.put(field.getName() + "#"
 										+ getTypeName(field) + "Array", mapa);
 							}
 
 							continue;
 						} else {
 
 							List<Object> lista = new ArrayList<Object>();
 
 							for (int i = 0; i < ((Object[]) value).length; 
 									i++) {
 
 								try {
 									String type = ((Object[]) value)[0]
 											.getClass().getName();
									lista.add(toMap((Class.forName(type)
											.cast(((Object[]) value)[i]))));
 								} catch (ClassNotFoundException e) {
 									e.printStackTrace();
 								} catch (NullPointerException e) {
 									
 								}
 								
 							}
 							if (knownHashes.containsKey(hashString)) {
 								map.put(field.getName() + "#"
 										+ getTypeName(field),
 										knownHashes.get(hashString));
 							} else {
 								knownHashes.put(hashString, field.getName());
 								map.put(field.getName() + "#"
 										+ getTypeName(field) + "Array", lista);
 							}
 							continue;
 						}
 					}
 
 					if (knownHashes.containsKey(hashString)) {
 						map.put(field.getName() + "#" + getTypeName(field),
 								knownHashes.get(hashString));
 					} else {
 						knownHashes.put(hashString, field.getName());
 						map.put(field.getName() + "#" + getTypeName(field),
 								toMap(value));
 					}
 				}
 
 			}
 		} else {
 			map.put("0", "0"); // represenation of null references
 		}
 
 		return map;
 	}
 
 	boolean isPrimitive(final Field field) {
 		return field.getType().isPrimitive();
 	}
 
 	void setPublic(final Field field) {
 		if (!field.isAccessible()) {
 			field.setAccessible(true);
 		}
 	}
 
 	@SuppressWarnings("rawtypes")
 	Field[] getFields(final Class c) {
 		return c.getDeclaredFields();
 	}
 
 	String getTypeName(final Field field) {
 		return field.getType().getName();
 	}
 
 	@Override
 	public boolean writeObject(final OutputStream os, final Object ob) {
 		try {
 			BufferedWriter _os = new BufferedWriter(new OutputStreamWriter(os,
 					"UTF-8"));
 			_os.write(parses.encode((prepareMap(ob))));
 			_os.flush();
 			_os.close();
 			return true;
 		} catch (UnsupportedEncodingException e) {
 			System.err.println("Unsupported encoding");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	// FOR TESTING PURPOSES ONLY
 	public void printMap(final Map<String, Object> map) {
 
 		Iterator<Entry<String, Object>> it = map.entrySet().iterator();
 		while (it.hasNext()) {
 			@SuppressWarnings("rawtypes")
 			Map.Entry pairs = (Map.Entry) it.next();
 			System.out.println(pairs.getKey() + " = " + pairs.getValue());
 			it.remove();
 		}
 	}
 
 	void processData(final String hashString, final String typeName) {
 
 	}
 
 	boolean shouldISkipThisField(@SuppressWarnings("rawtypes") final Class c,
 			final Field field, final boolean checkTransient) {
 		boolean shouldI = true;
 		String s = new String();
 		if ((s = fieldsToConsider.get(c.getName())) != null) {
 			if (s.contains(field.getName())) {
 				shouldI = false;
 			} else {
 				shouldI = true;
 			}
 		} else {
 			shouldI = false;
 		}
 
 			if (checkTransient) {
 			if (Modifier.isTransient(field.getModifiers())) {
 				shouldI = true;
 			}
 			}
 		return shouldI;
 	}
 }
