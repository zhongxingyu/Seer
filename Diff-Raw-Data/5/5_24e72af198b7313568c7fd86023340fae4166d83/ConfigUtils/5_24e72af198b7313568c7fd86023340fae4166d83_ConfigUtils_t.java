 package org.tsaikd.java.utils;
 
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 public class ConfigUtils {
 	protected static final String defaultResPath = "/config.properties";
 	protected static Map<String, Properties> confs = new HashMap<String, Properties>();
 
 	protected ConfigUtils() {
 	}
 
 	public static Properties getInstance(String resPath) throws Exception {
 		Properties property = confs.get(resPath);
 		if (property == null) {
 			property = new Properties();
			InputStream in = Thread.currentThread().getClass().getResourceAsStream(resPath);
 			property.load(in);
 			in.close();
 		}
 		return property;
 	}
 
 	public static Properties getInstance() throws Exception {
 		return getInstance(defaultResPath);
 	}
 
 	public static void set(final String key, final String value) throws Exception {
		System.setProperty(key, value);
 	}
 
 	public static String get(final String key, final String defaultValue) throws Exception {
 		String value = System.getProperty(key);
 		if (value == null) {
 			value = getInstance().getProperty(key);
 			if (value == null) {
 				value = defaultValue;
 			}
 		}
 		return value;
 	}
 
 	public static String get(final String key) throws Exception {
 		return get(key, null);
 	}
 
 	public static void set(final String key, final int value) throws Exception {
 		getInstance().setProperty(key, String.valueOf(value));
 	}
 
 	public static int getInt(final String key, final int defaultValue) throws Exception {
 		String value = get(key, null);
 		if (value == null) {
 			return defaultValue;
 		} else {
 			return Integer.parseInt(value);
 		}
 	}
 
 	public static int getInt(final String key) throws Exception {
 		return getInt(key, 0);
 	}
 
 	public static void set(final String key, final long value) throws Exception {
 		getInstance().setProperty(key, String.valueOf(value));
 	}
 
 	public static long getLong(final String key, final long defaultValue) throws Exception {
 		String value = get(key, null);
 		if (value == null) {
 			return defaultValue;
 		} else {
 			return Integer.parseInt(value);
 		}
 	}
 
 	public static long getLong(final String key) throws Exception {
 		return getLong(key, 0);
 	}
 
 	public static void set(final String key, final boolean value) throws Exception {
 		getInstance().setProperty(key, String.valueOf(value));
 	}
 
 	public static boolean getBool(final String key, final boolean defaultValue) throws Exception {
 		String value = get(key, null);
 		if (value == null) {
 			return defaultValue;
 		} else {
 			value = value.toLowerCase().trim();
 			if (value.equals("true"))
 				return true;
 			if (value.equals("yes"))
 				return true;
 			return false;
 		}
 	}
 
 	public static boolean getBool(final String key) throws Exception {
 		return getBool(key, false);
 	}
 
 }
