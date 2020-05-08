 package org.jcors.config;
 
 import java.io.InputStream;
 
 import javax.xml.XMLConstants;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.validation.SchemaFactory;
 
 import org.apache.log4j.Logger;
 import org.jcors.util.Constraint;
 
 /**
  * Class responsible for loading configurations
  * 
  * @author Diego Silveira
  */
 public class ConfigLoader {
 
 	public static final String CONFIG_FILE_NAME = "jcors.xml";
 	public static final String CONFIG_SCHEMA_FILE_NAME = "jcors-schema.xsd";
 
 	private static final Logger log = Logger.getLogger(ConfigLoader.class);
 
 	/**
 	 * Loads a configuration from jcors.xml in classpath. If this file is not found, uses a default configuration
 	 * 
 	 * @return
 	 */
 	public static JCorsConfig load() {
 
 		log.info("Initializing JCors...");
 		InputStream fileStream = findFileInClasspath(CONFIG_FILE_NAME);
 
 		JCorsConfig config = new JCorsConfig();
 
 		if (fileStream != null) {
 			config = loadFromFileStream(fileStream);
 			Constraint.ensureNotNull(config, "It was not possible to get a valid configuration instance");
 			log.info(String.format("Configuration loaded from classpath file '%s'", CONFIG_FILE_NAME));
 		} else {
 			log.info(String.format("No file '%s' found in classpath. Using default configurations.", CONFIG_FILE_NAME));
 		}
 
 		log.info(config);
 		return config;
 
 	}
 
 	/**
 	 * Finds a file by name in classpath
 	 * 
 	 * @param fileName
 	 * @return
 	 */
 	private static InputStream findFileInClasspath(String fileName) {
 
 		InputStream is = null;
 
 		try {
			
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
 			is = classLoader.getResourceAsStream(fileName);
			return is;
 
 		} catch (Exception ex) {
 
 			log.error(String.format("Error while reading file '%s' from classpath", fileName), ex);
 			return null;
 		}
 
 	}
 
 	/**
 	 * Builds a configuration class based on XML file
 	 * 
 	 * @param file
 	 * @return
 	 */
 	private static JCorsConfig loadFromFileStream(InputStream fileStream) {
 
 		try {
 
 			JAXBContext context = JAXBContext.newInstance(ConfigBuilder.class);
 			Unmarshaller unmarshaller = context.createUnmarshaller();
 
 			InputStream schemaFileStream = findFileInClasspath(CONFIG_SCHEMA_FILE_NAME);
 			Constraint.ensureNotNull(schemaFileStream, "JCors configuration schema not found");
 
 			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
 			unmarshaller.setSchema(schemaFactory.newSchema(new StreamSource(schemaFileStream)));
 
 			ConfigBuilder configBuilder = (ConfigBuilder) unmarshaller.unmarshal(fileStream);
 			Constraint.ensureNotNull(configBuilder, "It was not possible to get a valid configuration builder instance");
 
 			return configBuilder.buildConfig();
 
 		} catch (Exception e) {
 
 			log.error("Failed loading configuration file", e);
 			return null;
 		}
 
 	}
 
 }
