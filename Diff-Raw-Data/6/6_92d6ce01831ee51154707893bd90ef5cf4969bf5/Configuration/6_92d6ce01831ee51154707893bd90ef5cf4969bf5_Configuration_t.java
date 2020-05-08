 package com.github.davidmoten.logan.config;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.List;
import java.util.logging.Logger;
 
 import javax.xml.bind.annotation.XmlElement;
 
 import com.github.davidmoten.logan.util.PropertyReplacer;
 import com.github.davidmoten.logan.watcher.Main;
 import com.google.common.collect.Lists;
 
 /**
  * Configuration for log-persister.
  * 
  * @author dave
  * 
  */
 public class Configuration {
 
 	@XmlElement(required = false)
 	public Parser parser;
 	@XmlElement(required = true)
 	public List<Group> group = Lists.newArrayList();
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param parser
 	 * @param group
 	 */
 	public Configuration(Parser parser, List<Group> group) {
 		super();
 		this.parser = parser;
 		this.group = group;
 	}
 
 	/**
 	 * Constructor.
 	 */
 	public Configuration() {
 		// no-args constructor required by jaxb
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		builder.append("Configuration [");
 		builder.append("parser=");
 		builder.append(parser);
 		builder.append(", group=");
 		builder.append(group);
 		builder.append("]");
 		return builder.toString();
 	}
 
 	private static final String DEFAULT_CONFIGURATION_LOCATION = "/persister-configuration.xml";
 
	private static Logger log = Logger.getLogger(Configuration.class.getName());

 	public static Configuration getConfiguration() {
 		String configLocation = System.getProperty("logan.config",
 				DEFAULT_CONFIGURATION_LOCATION);
		log.info("config=" + configLocation);
 		InputStream is = Main.class.getResourceAsStream(configLocation);
 		if (is == null) {
 			File file = new File(configLocation);
 			if (file.exists())
 				try {
 					is = new FileInputStream(configLocation);
 				} catch (FileNotFoundException e) {
 					throw new RuntimeException(e);
 				}
 			else
 				throw new RuntimeException(
 						"configuration xml not found. Set property logan.config to a file on classpath or filesystem.");
 		}
 		InputStream is2 = PropertyReplacer.replaceSystemProperties(is);
 		Configuration configuration = new Marshaller().unmarshal(is2);
 		return configuration;
 	}
 
 }
