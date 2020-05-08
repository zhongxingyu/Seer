 package hudson.plugins.buckminster.command;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class CaseInsensitiveMap extends HashMap<String, String> {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 8544614957380938140L;
 
 	
 	public CaseInsensitiveMap() {
 		super();
 	}
 
 	public CaseInsensitiveMap(int initialCapacity, float loadFactor) {
 		super(initialCapacity, loadFactor);
 	}
 
 	public CaseInsensitiveMap(int initialCapacity) {
 		super(initialCapacity);
 	}
 
 	public CaseInsensitiveMap(Map<? extends String, ? extends String> m) {
		//super(m) would bypass the overridden put method and we would end up with lower case keys 
		super(m.size());
		putAll(m);
 	}
 
 	@Override
 	public String put(String key, String value) {
 		return super.put(key.toUpperCase(), value);
 	}
 	
 	@Override
 	public String get(Object key) {
 		return super.get(key.toString().toUpperCase());
 	}
 	
 	@Override
 	public boolean containsKey(Object key) {
 		return super.containsKey(key.toString().toUpperCase());
 	}
 	
 	
 	
 }
