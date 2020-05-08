 package org.pescuma.buildhealth.prefs;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 
 public class PropertiesPreferencesStore implements DiskPreferencesStore {
 	
 	private static final String SEPARATOR = ".";
 	
 	private final File file;
 	private Properties properties;
 	private boolean changed = false;
 	
 	public PropertiesPreferencesStore(File file) {
 		this.file = file;
 	}
 	
 	private Properties props() {
 		if (properties == null)
 			properties = readFromFile(file);
 		
 		return properties;
 	}
 	
 	private String toSimpleKey(String[] key) {
 		StringBuilder result = new StringBuilder();
 		
 		for (String k : key) {
 			if (k.indexOf(SEPARATOR) >= 0)
 				throw new IllegalArgumentException("Key can't contain '" + SEPARATOR + "': " + key);
 			
 			if (result.length() > 0)
 				result.append(SEPARATOR);
 			
 			result.append(k);
 		}
 		
 		return result.toString();
 	}
 	
 	@Override
 	public void put(String value, String... key) {
 		props().put(toSimpleKey(key), value);
 		changed = true;
 	}
 	
 	@Override
 	public String get(String... key) {
 		return (String) props().get(toSimpleKey(key));
 	}
 	
 	@Override
 	public void remove(String... key) {
 		Object removed = props().remove(toSimpleKey(key));
 		if (removed != null)
 			changed = true;
 	}
 	
 	@Override
 	public void removeChildren(String... key) {
 		String sk = toSimpleKey(key) + SEPARATOR;
 		for (Iterator<Object> it = props().keySet().iterator(); it.hasNext();) {
 			String candidate = (String) it.next();
 			if (candidate.startsWith(sk)) {
 				it.remove();
 				changed = true;
 			}
 		}
 	}
 	
 	@Override
 	public List<String[]> getKeys(String... key) {
 		List<String[]> result = new ArrayList<String[]>();
 		
 		String sk = toSimpleKey(key);
 		String sks = sk + SEPARATOR;
 		for (Iterator<Object> it = props().keySet().iterator(); it.hasNext();) {
 			String candidate = (String) it.next();
 			if (key.length == 0 || candidate.equals(sk) || candidate.startsWith(sks))
				result.add(candidate.split(SEPARATOR));
 		}
 		
 		return Collections.unmodifiableList(result);
 	}
 	
 	@Override
 	public void saveToDisk() {
 		if (!changed)
 			return;
 		
 		saveToFile(file, properties);
 	}
 	
 	private static Properties readFromFile(File file) {
 		Properties result = new Properties();
 		
 		if (!file.exists())
 			return result;
 		
 		Reader reader = null;
 		try {
 			
 			reader = new FileReader(file);
 			result.load(reader);
 			
 		} catch (IOException e) {
 			throw new PreferencesException("Error reading file " + file, e);
 			
 		} finally {
 			IOUtils.closeQuietly(reader);
 		}
 		
 		return result;
 	}
 	
 	private static void saveToFile(File file, Properties props) {
 		Writer writer = null;
 		try {
 			
 			FileUtils.forceMkdir(file.getParentFile());
 			
 			writer = new FileWriter(file);
 			props.store(writer, null);
 			
 		} catch (IOException e) {
 			throw new PreferencesException("Error writing file " + file, e);
 			
 		} finally {
 			IOUtils.closeQuietly(writer);
 		}
 	}
 }
