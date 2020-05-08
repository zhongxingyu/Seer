 package es.weso.acota.core;
 
 import org.apache.commons.configuration.CompositeConfiguration;
 
 import es.weso.acota.core.exceptions.AcotaConfigurationException;
 
 /**
  * @author César Luis Alvargonzález
  *
  */
 public interface Configuration {
 	/**
 	 * Loads Acota's configuration properties files
 	 * 
 	 * @throws AcotaConfigurationException Any exception that occurs 
 	 * while initializing a Configuration object
 	 */
 	public void loadsConfiguration(CompositeConfiguration configuration) throws AcotaConfigurationException;
 }
