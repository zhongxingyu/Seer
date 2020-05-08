 package net.mms_projects.tostream;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Properties;
 
 public class Settings {
 
 	public Integer[] videoResolution = new Integer[2];
 
 	private Properties defaults = new Properties();
 
 	public static final String BITRATE = "bitrate";
 	public static final String BUFFER_SIZE = "bufferSize";
 	public static final String FRAME_RATE = "frameRate";
 	public static final String RESOLUTION = "resolution";
 	public static final String LOCATION = "location";
 	public static final String STREAM_URL = "streamUrl";
 	public static final String SHOW_DEBUGCONSOLE = "showDebugconsole";
 	public static final String DEFAULT_INTERFACE = "defaultInterface";
 
 	private Properties properties;
 
 	public Settings() {
 		defaults.setProperty(BITRATE, "1168k");
 		defaults.setProperty(BUFFER_SIZE, "1835k");
 		defaults.setProperty(FRAME_RATE, "30");
 		defaults.setProperty(RESOLUTION, "1024,768");
 		defaults.setProperty(LOCATION, "0,0");
 		defaults.setProperty(STREAM_URL, "");
 		defaults.setProperty(SHOW_DEBUGCONSOLE, "false");
 		defaults.setProperty(DEFAULT_INTERFACE, "swt");
 
 		properties = new Properties(defaults);
 	}
 
 	public String get(String key) {
 		return properties.getProperty(key);
 	}
 
 	public int getAsInteger(String key) {
 		return Integer.parseInt(get(key));
 	}
 
 	public boolean getAsBoolean(String key) {
 		return Boolean.parseBoolean(get(key));
 	}
 
 	public Integer[] getAsIntegerArray(String key) {
 		String[] tokens = get(key).split(",");
 		Integer[] array = new Integer[tokens.length];
 		for (int i = 0; i < tokens.length; i++) {
 			array[i] = Integer.parseInt(tokens[i]);
 		}
 		return array;
 	}
 
 	public void set(String key, String value) throws Exception {
 		if (!defaults.containsKey(key)) {
 			throw new Exception("Tried to set unknown setting");
 		}
 		properties.setProperty(key, value);
 		saveProperties();
 	}
 
 	public void set(String key, Integer value) throws Exception {
 		set(key, value.toString());
 	}
 
 	public void set(String key, Boolean value) throws Exception {
 		set(key, value.toString());
 	}
 
 	public void set(String key, Integer[] array) throws Exception {
 		String value = "";
 		
         for (int i = 0; i < array.length; i++) {
             value += array[i] + ",";
         }
        value = value.substring(0, value.length() - 1);
         
 		set(key, value);
 	}
 
 	public LinkedHashMap<String, String> getSettings() {
 		LinkedHashMap settings = new LinkedHashMap<String, String>();
 		Enumeration<Object> keys = properties.keys();
 		Enumeration<Object> values = properties.elements();
 		while (values.hasMoreElements()) {
 			String key = (String) keys.nextElement();
 			String value = (String) values.nextElement();
 			settings.put(key, value);
 		}
 		return settings;
 	}
 
 	public void loadProperties() {
 		BufferedInputStream stream;
 		try {
 			stream = new BufferedInputStream(new FileInputStream(
 					getConfigDirectory() + "options.properties"));
 			properties.load(stream);
 			stream.close();
 		} catch (FileNotFoundException e) {
 			// having no properties file is OK
 		} catch (IOException e) {
 			// something went wrong with the stream
 			e.printStackTrace();
 		}
 	}
 
 	public void saveProperties() {
 		BufferedOutputStream stream;
 		try {
 			File file = new File(getConfigDirectory() + "options.properties");
 			if (!file.exists()) {
 				System.out.println("File not there");
 				file.createNewFile();
 			}
 			stream = new BufferedOutputStream(new FileOutputStream(file));
 			// TODO describe properties in comments
 			String comments = "";
 			properties.store(stream, comments);
 		} catch (FileNotFoundException e) {
 			// we checked this first so this shouldn't occurs
 		} catch (IOException e) {
 			// something went wrong with the stream
 			e.printStackTrace();
 		}
 	}
 
 	public String getConfigDirectory() {
 		return "." + System.getProperty("file.separator");
 	}
 }
