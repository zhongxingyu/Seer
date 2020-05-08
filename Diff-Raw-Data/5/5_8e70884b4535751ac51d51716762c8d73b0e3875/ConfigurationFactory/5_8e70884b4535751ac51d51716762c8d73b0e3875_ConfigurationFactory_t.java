 package com.operativus.senacrs.audit.model.config;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import com.operativus.senacrs.audit.messages.MessagesCentral;
 
 public final class ConfigurationFactory {
 
 	private static final String PWD = System.getProperty("user.dir");
 	private static final String DEFAULT_CONFIGURATION_FILENAME = PWD
 			+ File.separator + "configuration.properties";
 	private static final Logger logger = LogManager.getLogger(ConfigurationFactory.class.getClass());
 
 	protected static enum ConfigKey {
 
 		BASE_URL("base_url"),
 		VERSION("version"),
 		USERNAME("username"),
 		PASSWORD("password"), 
 		LOG_FILE("log_file"), 
 		;
 
 		private String key;
 
 		private ConfigKey(final String key) {
 
 			this.key = key;
 		}
 
 		public String getKey() {
 
 			return this.key;
 		}
 	}
 
 	private ConfigurationFactory() {
 
 		super();
 	}
 
 	public static Configuration createConfiguration() throws IOException {
 
 		return createConfiguration(DEFAULT_CONFIGURATION_FILENAME);
 	}
 
 	public static Configuration createConfiguration(final String filename) throws IOException {
 
 		Configuration result = null;
 		Properties props = null;
 		
 		logger.info(MessagesCentral.getMessage(ConfigMessagesEnum.LOG_CONFIG_BEGIN, filename));
 		props = getProperties(filename);
 		result = read(props);
		checkMinimalNonNullValues(filename, result);
 		logger.debug(MessagesCentral.getMessage(ConfigMessagesEnum.LOG_CONFIG_END, filename));
 
 		return result;
 	}
 
 	private static Properties getProperties(final String filename) throws IOException {
 
 		Properties result = null;
 		InputStream in = null;
 
 		in = new FileInputStream(filename);
 		result = new Properties();
 		result.load(in);
 
 		return result;
 	}
 
 	private static Configuration read(final Properties props) {
 
 		Configuration result = null;
 
 		result = new Configuration();
 		for (ConfigKey k : ConfigKey.values()) {
 			setValue(result, props, k);
 		}
 
 		return result;
 	}
 
 	private static void setValue(final Configuration result, final Properties props, final ConfigKey key) {
 
 		String value = null;
 
 		value = props.getProperty(key.getKey());
 		switch (key) {
 		case BASE_URL:
 			result.setBaseUrl(value);
 			break;
 		case PASSWORD:
 			result.setPassword(value);
 			break;
 		case LOG_FILE:
 			result.setLogFile(value);
 			break;
 		case USERNAME:
 			result.setUsername(value);
 			break;
 		case VERSION:
 			result.setVersion(value);
 			break;
 		default:
 			throw new IllegalArgumentException(String.valueOf(key));
 		}
 	}
 
	private static void checkMinimalNonNullValues(final String filename, final Configuration result) {
 
 		if (!isValueOk(result.getBaseUrl())) {
 			throw new MissingMinimalConfigurationEntryException(filename, ConfigKey.BASE_URL.getKey());
 		}
 		if (!isValueOk(result.getUsername())) {
 			throw new MissingMinimalConfigurationEntryException(filename, ConfigKey.USERNAME.getKey());
 		}
 		if (!isValueOk(result.getVersion())) {
 			throw new MissingMinimalConfigurationEntryException(filename, ConfigKey.VERSION.getKey());
 		}
 	}
 
 	private static boolean isValueOk(final String value) {
 
 		return (value != null) && !value.isEmpty();
 	}
 }
