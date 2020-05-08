 package com.wolflink289.util;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.ReentrantLock;
 
 /**
  * String localization utility.
  * 
  * @author Wolflink289
  */
 public class Local {
 	
 	// Constants
 	static private final String default_locale = "enUS";
 	static private final String default_path = "file://%/locale/";
 	static private final String default_strings = "strings.txt";
 	
 	// Variables
 	static private URL load_path = null;
 	static private String load_locale = null;
 	static private HashMap<String, String> lcal_string = null;
 	
 	static private final ReentrantLock sync = new ReentrantLock();
 	
 	// Static Method
 	static {
 		try {
 			setLoadPath(default_path);
 		} catch (Exception ex) {
 			System.err.println("Cannot set load path: " + ex.getLocalizedMessage());
 		}
 		
 		try {
 			setLocale(Locale.getDefault());
 		} catch (Exception ex) {
 			System.err.println("Cannot set locale: " + Locale.getDefault().getLanguage().toLowerCase() + Locale.getDefault().getCountry().toUpperCase());
 			load_locale = default_locale.replace("/", "");
 		}
 	}
 	
 	// Setup Methods
 	/**
 	 * Set the path of where the locale files are to be loaded from.<br>
 	 * The path may be one of the following protocols:<br>
 	 * <br>
 	 * <b>http://</b> - download the locale files from the internet.<br>
 	 * <b>file://</b> - load the locale files from the local disk.<br>
 	 * <b>file://%/</b> - load the locale files from the jar's resource path.
 	 * 
 	 * @param path the new resource loading path.
 	 * @throws java.net.MalformedURLException
 	 */
 	static public void setLoadPath(String path) throws java.net.MalformedURLException {
 		if (path == null) throw new java.net.MalformedURLException("The URL cannot be null");
 		
 		while (path.endsWith("/")) {
 			path = path.substring(0, path.length() - 1);
 		}
 		
 		URL temp = new URL(path);
 		if (!temp.getProtocol().equalsIgnoreCase("file") && !temp.getProtocol().equalsIgnoreCase("http")) { throw new java.net.MalformedURLException("Protocol unused"); }
 		
 		load_path = temp;
 		temp = null;
 	}
 	
 	/**
 	 * Get the path of where the locale files are to be loaded from.
 	 * 
 	 * @return the path as a string.
 	 */
 	static public String getLoadPath() {
 		return load_path.toString();
 	}
 	
 	/**
 	 * Set the localization.<br>
 	 * <b>Warning: </b>the localization name is not verified.
 	 * 
 	 * @param locale the locale as a string.
 	 */
 	static public void setLocale(String locale) {
 		if (locale == null) throw new NullPointerException("The locale cannot be null");
 		
 		locale = locale.replace("/", "");
 		load_locale = locale;
 	}
 	
 	/**
 	 * Set the localization.<br>
 	 * The Locale will be converted into the "languageCOUNTRY". For example, United States English will be "enUS" (without quotes)
 	 * 
 	 * @param locale the locale as a Locale object.
 	 */
 	static public void setLocale(Locale locale) {
 		if (locale == null) throw new NullPointerException("The locale cannot be null");
 		setLocale(locale.getLanguage().toLowerCase() + locale.getCountry().toUpperCase());
 	}
 	
 	/**
 	 * Get the localization.
 	 * 
 	 * @return the localization as a string.
 	 */
 	static public String getLocale() {
 		return load_locale;
 	}
 	
 	/**
 	 * Refresh the localized strings.
 	 */
 	static public void refresh() {
 		load();
 	}
 	
 	// Locale Methods
 	/**
 	 * Get a localized string.
 	 * 
 	 * @param key the localized string's name.
 	 * @return the localized string's value.
 	 */
 	static public String get(String key) {
 		// Sync
 		try {
 			sync.tryLock(Long.MAX_VALUE, TimeUnit.DAYS);
 		} catch (InterruptedException e) {}
 		
 		// Load
 		if (lcal_string == null) load();
 		
 		// Return
 		String val = lcal_string.get(key);
 		if (val == null) return key;
 		
 		return val;
 	}
 	
 	/**
 	 * Get the input stream of a localized resource.<br>
 	 * <br>
 	 * In the event that the localized resource isn't found, then it the default localization is loaded.<br>
 	 * If both localized resources aren't found, null is returned.
 	 * 
 	 * @param path the resource path.
 	 * @return the resource's stream, or null.
 	 */
 	static public InputStream getStream(String path) {
 		// Try set locale, then try default
 		InputStream is = getStream(path, load_locale);
 		if (is == null) is = getStream(path, default_locale);
 		
 		// Return locale
 		return is;
 	}
 	
 	/**
 	 * Put (set) a localized string.
 	 * 
 	 * @param key the localized string's name.
 	 * @param val the localized string's new value.
 	 */
 	static public void put(String key, String val) {
 		// Sync
 		try {
 			sync.tryLock(Long.MAX_VALUE, TimeUnit.DAYS);
 		} catch (InterruptedException e) {}
 		
 		// Put
 		lcal_string.put(key, val);
 	}
 	
 	/**
 	 * Put (set) a localized string if it is not already set.
 	 * 
 	 * @param key the localized string's name.
 	 * @param val the localized string's new value.
 	 */
 	static public void putDefault(String key, String val) {
 		// Sync
 		try {
 			sync.tryLock(Long.MAX_VALUE, TimeUnit.DAYS);
 		} catch (InterruptedException e) {}
 		
 		// Check
 		if (lcal_string.containsKey(key)) return;
 		
 		// Put
 		lcal_string.put(key, val);
 	}
 	
 	// Private Methods
 	/**
 	 * Get the input stream of a localized resource.
 	 * 
 	 * @param path the resource path.
 	 * @param locale the locale name.
 	 * @return the resource's stream, or null.
 	 */
 	static private InputStream getStream(String path, String locale) {
 		// Load HTTP
 		if (load_path.getProtocol().equalsIgnoreCase("http")) {
 			try {
 				URLConnection con = append(path, locale).openConnection();
 				return con.getInputStream();
 			} catch (Exception ex) {
 				System.err.println("Failed to load remote locale resource: " + path);
 				return null;
 			}
 		}
 		
 		// Load FILE/JAR
 		if (load_path.getProtocol().equalsIgnoreCase("file")) {
 			// Strip beginning /
 			while (path.startsWith("/"))
 				path = path.substring(1);
 			
 			// Determine location
 			String spath = load_path.getAuthority() + load_path.getPath() + "/" + locale + "/" + path;
 			if (spath.startsWith("%/")) {
 				// JAR
 				spath = spath.substring(1);
 				
 				if (Local.class.getResource(spath) == null) {
 					System.err.println("Failed to load local (jar) locale resource: " + path);
 					return null;
 				}
 				return Local.class.getResourceAsStream(spath);
 			} else {
 				// FILE
 				File sload = new File(spath);
 				
 				if (sload.exists() && sload.isFile()) {
 					try {
 						return new FileInputStream(sload);
 					} catch (FileNotFoundException e) {
 						System.err.println("Failed to load local (file) locale resource: " + path);
 						return null;
 					}
 				}
 				
 				System.err.println("Failed to load local (file) locale resource: " + path);
 				return null;
 			}
 		}
 		
 		// Load... ?!?!
 		return null;
 	}
 	
 	/**
 	 * Append a path to the load path.
 	 * 
 	 * @param path the path.
 	 * @return the new URL.
 	 * @throws java.net.MalformedURLException
 	 */
 	static private URL append(String path, String locale) throws java.net.MalformedURLException {
 		// Strip beginning /
 		while (path.startsWith("/")) {
 			path = path.substring(1);
 		}
 		
 		return new URL(load_path + "/" + locale + "/" + path);
 	}
 	
 	/**
 	 * Load the default locale data.
 	 */
 	static private void load() {
 		// Sync
 		try {
 			sync.lock();
 			
 			// Load
 			try {
 				// Load - Strings
 				lcal_string = new HashMap<String, String>();
 				
 				InputStream strings = getStream(default_strings);
 				if (strings != null) {
 					BufferedReader stringr = new BufferedReader(new InputStreamReader(strings));
 					String line;
 					while ((line = stringr.readLine()) != null) {
 						line = line.trim();
 						
 						// Comments/Ignore
 						if (line.isEmpty()) continue;
 						if (line.startsWith("#")) continue;
 						
 						// Read
 						int splid = line.indexOf(":");
 						if (splid == -1) continue; // Unreadable line
 							
 						// Put
 						lcal_string.put(line.substring(0, splid).trim(), line.substring(splid + 1).trim());
 					}
 					
 					stringr.close();
 					line = null;
 				}
 			} catch (Exception ex) {}
 			
 			// Release
 		} finally {
 			sync.unlock();
 		}
 	}
 }
