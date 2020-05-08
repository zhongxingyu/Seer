 package eu.play_project.dcep.distributedetalis.utils;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.objectweb.fractal.adl.ADLException;
 import org.objectweb.fractal.adl.Factory;
 import org.objectweb.fractal.api.Component;
 import org.objectweb.proactive.core.component.adl.FactoryFactory;
 import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
 import org.objectweb.proactive.core.config.PAProperty;
 import org.objectweb.proactive.core.config.PAPropertyBoolean;
 import org.objectweb.proactive.core.config.PAPropertyInteger;
 import org.objectweb.proactive.core.config.PAPropertyLong;
 import org.objectweb.proactive.core.config.PAPropertyString;
 import org.objectweb.proactive.extensions.pnp.PNPConfig;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import eu.play_project.dcep.constants.DcepConstants;
 
 /**
  * Utility class for using ProActive and GCM.
  * 
  * @author Stefan Obermeier
  * @author Roland Stühmer
  */
 public class ProActiveHelpers {
 
 	private static Logger logger = LoggerFactory.getLogger(ProActiveHelpers.class);
 
 	/**
 	 * Create a new {@link Component} using configuration properties from the
 	 * DCEP configuration files.
 	 */
 	public static Component newComponent(String componentName, Map<String, Object> context) throws ADLException {
 		
		final Integer PROACTIVE_PNP_PORT = Integer.valueOf(DcepConstants.getProperties().getProperty("dcep.proactive.pnp.port"));
		final Integer PROACTIVE_HTTP_PORT = Integer.valueOf(DcepConstants.getProperties().getProperty("dcep.proactive.http.port"));
		final Integer PROACTIVE_RMI_PORT = Integer.valueOf(DcepConstants.getProperties().getProperty("dcep.proactive.rmi.port"));
 
 		// Define ProActive Properties in a local Map:
 		@SuppressWarnings("serial")
 		Map<PAProperty, Object> proActiveProperties = Collections.unmodifiableMap(new HashMap<PAProperty, Object>() {{
 	        put(CentralPAPropertyRepository.PA_RUNTIME_PING, false);
 	        put(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL, "pnp");
 	        put(PNPConfig.PA_PNP_PORT, PROACTIVE_PNP_PORT);
 	        put(CentralPAPropertyRepository.PA_XMLHTTP_PORT, PROACTIVE_HTTP_PORT);
 	        put(CentralPAPropertyRepository.PA_RMI_PORT, PROACTIVE_RMI_PORT);
 	        put(CentralPAPropertyRepository.GCM_PROVIDER, org.objectweb.proactive.core.component.Fractive.class.getName());
 	        // ...
 	    }});
 		
 		// Set all properties in a loop:
 		for (Entry<PAProperty, Object> proActiveProperty : proActiveProperties.entrySet()) {
			logger.debug("Setting ProActive property '{}' to new value: ", proActiveProperty.getKey(), proActiveProperty.getValue());
 			setProperty(proActiveProperty.getKey(), proActiveProperty.getValue());
 		}
 		
 		Factory factory = FactoryFactory.getFactory();
 		
 		return (Component) factory.newComponent(componentName, context);
 	}
 	
 	/**
 	 * Create a new {@link Component} using configuration properties from the
 	 * DCEP configuration files.
 	 */
 	public static Component newComponent(String componentName) throws ADLException {
 		HashMap<String, Object> context = new HashMap<String, Object>();
 		return newComponent(componentName, context);
 	}
 
 	private static void setProperty(PAProperty key, Object value) {
 		// This is very cumbersone but ProActive is very strict about these types:
 		if (key instanceof PAPropertyBoolean) {
 			((PAPropertyBoolean)key).setValue((Boolean)value);
 		}
 		else if (key instanceof PAPropertyLong) {
 			((PAPropertyLong)key).setValue((Long)value);
 		}
 		else if (key instanceof PAPropertyInteger) {
 			((PAPropertyInteger)key).setValue((Integer)value);
 		}
 		else if (key instanceof PAPropertyString) {
 			((PAPropertyString)key).setValue((String)value);
 		}
 		else {
 			throw new IllegalArgumentException("An unsupported type of ProActive property was provided for property: " + key.getName());
 		}
 
 	}
 	
 }
