 package org.fao.fi.vme.geobatch;
 
 import it.geosolutions.geobatch.registry.AliasRegistrar;
 import it.geosolutions.geobatch.registry.AliasRegistry;
 
 /**
  * 
  * @author Emmanuel Blondel (FAO) - emmanuel.blondel1@gmail.com |
  *         emmanuel.blondel@fao.org
  * 
  */
 public class VMEIngestionAliasRegistrar extends AliasRegistrar {
 
 	public VMEIngestionAliasRegistrar(AliasRegistry registry) {
 		LOGGER.info(getClass().getSimpleName() + ": registering alias.");
 		
		registry.putAlias("IngestionConfiguration", VMEIngestionConfiguration.class);
 		
 	}
 	
 }
