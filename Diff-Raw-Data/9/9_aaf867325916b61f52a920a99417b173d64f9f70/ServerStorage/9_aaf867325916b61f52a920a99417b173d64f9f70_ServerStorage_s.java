 package ch.cern.atlas.apvs.server;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.PropertiesConfiguration;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ServerStorage {
 	private static final String APVS_SERVER_SETTINGS_FILE = "APVS.properties";
 	private static final String comment = "APVS Server Settings";
 	private static Logger log = LoggerFactory.getLogger(ServerStorage.class
 			.getName());
 
 	private static ServerStorage instance;
 
 	private PropertiesConfiguration config;
 	private boolean readOnly;
 
 	public ServerStorage() throws FileNotFoundException, IOException {
 		try {
 			File file = new File(APVS_SERVER_SETTINGS_FILE);
 			if (!file.exists()) {
				file.createNewFile();
				log.warn("File " + APVS_SERVER_SETTINGS_FILE
						+ " not found, created a NEW one.");
 			}
 			config = new PropertiesConfiguration(file);
 			try {
 				config.setHeader(comment + "\n" + (new Date()).toString());
 				config.save();
 				// FIXME-654 Do not use until commons-configuration 2.0 is out
 				// which solves the many concurrent modification exceptions. See
 				// https://issues.apache.org/jira/browse/CONFIGURATION-330
 				// config.setAutoSave(true);
 			} catch (ConfigurationException e) {
 				log.warn("Configuration file is READ ONLY");
 				config = new PropertiesConfiguration();
 				readOnly = true;
 			}
 		} catch (ConfigurationException e) {
 			log.warn("Configuration File Problem", e);
 		}
 	}
 
 	public static ServerStorage getLocalStorageIfSupported() {
 		try {
 			if (instance == null) {
 				instance = new ServerStorage();
 			}
 			return instance;
 		} catch (IOException e) {
 			log.warn("Server Settings Storage problem", e);
 		}
 		return null;
 	}
 
 	public String getString(String name) {
 		return config.getString(name);
 	}
 
 	public Boolean getBoolean(String name) {
		return config.getBoolean(name);
 	}
 
 	public void setItem(String name, Object value) {
 		config.setProperty(name, value);
 
 		// FIXME-654
 		if (readOnly)
 			return;
 
 		try {
 			config.save();
 		} catch (ConfigurationException e) {
 			log.error("Cannot store settings");
 			readOnly = true;
 		}
 	}
 
 	public Set<String> getKeys(final String prefix) {
 		Set<String> keys = new HashSet<String>();
 		
 		for (Iterator<String> i = config.getKeys(prefix); i.hasNext(); ) {
 			String key = i.next().substring(prefix.length()+1);
 			int dot = key.indexOf('.');
 			if (dot >= 0) {
 				key = key.substring(0, dot);
 			}
 			keys.add(key);
 		}
 		
 		return keys;
 	}
 }
