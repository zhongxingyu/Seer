 package com.operativus.senacrs.audit.properties;
 
 import java.io.IOException;
 import java.util.Arrays;
import java.util.List;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import com.operativus.senacrs.audit.exceptions.RuntimeExceptionFactory;
 
 /**
  * Override #get
  * 
  * @author fgka
  */
 public class ResourceCentral {
 
 	private static final String ADDING_RESOURCES_IN = "Adding resources in: ";
 	private static final String FAILED_TO_READ_RESOURCE_FILE = "Failed to read resource file ";
 	private final Logger logger;
 	private final PropertiesCentral central;
 
 	public ResourceCentral() {
 
 		super();
 
 		this.central = new PropertiesCentral();
 		this.logger = LogManager.getLogger(this.getClass());
 	}
 
 	public void populateCentral(final String... resources) {
 
 		if (resources == null) {
 			throw RuntimeExceptionFactory.getInstance().getNullArgumentException("resources");
 		}
 		this.logger.debug(ADDING_RESOURCES_IN + Arrays.toString(resources));
 		this.internPopulateCentral(resources);
 	}
 
 	private void internPopulateCentral(final String... resources) {
 
 		for (String r : resources) {
 			this.addResource(r);
 		}
 	}
 
 	private void addResource(final String resource) {
 
 		try {
 			this.central.addPropertiesFile(ResourceCentral.class.getResourceAsStream("/" + resource));
 		} catch (IOException e) {
 			this.logException(resource, e);
 		} catch (IllegalArgumentException e) {
 			this.logException(resource, e);
 		}
 	}
 
 	// protected for test
 	protected void logException(final String resource, final Throwable exception) {
 
 		this.logger.error(FAILED_TO_READ_RESOURCE_FILE + resource, exception);
 	}
 
 	public String getMessage(final PropertyKey key, final Object... arguments) {
 
 		return this.central.getMessage(key, arguments);
 	}
 
 	public boolean hasKey(final PropertyKey key) {
 
 		return this.central.hasKey(key);
 	}

	public List<String> getAvailableKeys() {

		return this.central.getAvailableKeys();
	}
 }
