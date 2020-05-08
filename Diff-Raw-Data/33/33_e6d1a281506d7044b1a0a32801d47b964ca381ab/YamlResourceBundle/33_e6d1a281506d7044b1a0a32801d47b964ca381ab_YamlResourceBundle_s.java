 package com.thedemgel.yamlresourcebundle;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map.Entry;
 import java.util.ResourceBundle;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import org.yaml.snakeyaml.Yaml;
 import sun.util.ResourceBundleEnumeration;
 
 public class YamlResourceBundle extends ResourceBundle {
 
 	private static File dataDir = null;
 	private ConcurrentMap<String, String> lookup = new ConcurrentHashMap<>();
 
 	public static class Control extends ResourceBundle.Control {
 
 		@Override
 		public List<String> getFormats(String baseName) {
 			if (baseName == null) {
 				throw new NullPointerException();
 			}
 			return Arrays.asList("yml", "java.properties");
 		}
 
 		@Override
 		public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {
 			if (baseName == null || locale == null
 				|| format == null || loader == null) {
 				throw new NullPointerException();
 			}
 
 			String bundleName = toBundleName(baseName, locale);
 
 			ResourceBundle bundle = null;
 			if (format.equals("yml")) {
 				String resourceName = toResourceName(bundleName, format);
 
 				// Try to get files first
 				if (dataDir != null) {
 					FileInputStream fis = null;
 					InputStreamReader reader = null;
 
 					try {
 
 						File file = new File(dataDir, resourceName);
 						if (file.isFile()) { // Also checks for existance
 							fis = new FileInputStream(file);
 
 							reader = new InputStreamReader(fis, "UTF-8");
 
 							bundle = new YamlResourceBundle(reader);
 						}
 					} finally {
 						if (reader != null) {
 							reader.close();
 						}
 						if (fis != null) {
 							fis.close();
 						}
 					}
 				}
 
 				if (bundle == null) {
 					InputStream stream = null;
 					if (reload) {
 						URL url = loader.getResource(resourceName);
 						if (url != null) {
 							URLConnection connection = url.openConnection();
 							if (connection != null) {
 								// Disable caches to get fresh data for
 								// reloading.
 								connection.setUseCaches(false);
 								stream = connection.getInputStream();
 							}
 						}
 					} else {
 						stream = loader.getResourceAsStream(resourceName);
 					}
 
 					if (stream != null) {
 						try (BufferedInputStream bis = new BufferedInputStream(stream)) {
 							bundle = new YamlResourceBundle(bis);
 						}
 					}
 				}
 			} else {
 				bundle = super.newBundle(bundleName, locale, format, loader, reload);
 			}
 			return bundle;
 		}
 	}
 
 	public static ResourceBundle getBundle(String bundle, Locale loc, File dataDir) {
 
 		YamlResourceBundle.dataDir = dataDir;
 
 		return getBundle(bundle, loc, new YamlResourceBundle.Control());
 	}
 
 	public YamlResourceBundle(InputStream stream) throws IOException {
 		Yaml loader = new Yaml();
 		for (Object item : ((LinkedHashMap) loader.load(stream)).entrySet()) {
			Entry<String, String> itemEntry = (Entry) item;
			lookup.put(itemEntry.getKey(), itemEntry.getValue());
 		}
 	}
 
 	public YamlResourceBundle(InputStreamReader stream) throws IOException {
 		Yaml loader = new Yaml();
 		for (Object item : ((LinkedHashMap) loader.load(stream)).entrySet()) {
			Entry<String, String> itemEntry = (Entry) item;
			lookup.put(itemEntry.getKey(), itemEntry.getValue());
 		}
 	}
 
 	public YamlResourceBundle() {
 	}
 
 	@Override
 	protected Object handleGetObject(String key) {
 		return lookup.get(key);
 	}
 
 	@Override
 	public Enumeration<String> getKeys() {
 		return new ResourceBundleEnumeration(lookup.keySet(), (parent != null) ? parent.getKeys() : null);
 	}
 }
