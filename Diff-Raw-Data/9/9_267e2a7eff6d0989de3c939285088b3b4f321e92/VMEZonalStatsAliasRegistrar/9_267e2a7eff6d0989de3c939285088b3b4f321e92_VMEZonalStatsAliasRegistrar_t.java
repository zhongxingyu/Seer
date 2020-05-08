 package org.fao.fi.vme.geobatch;
 
 import it.geosolutions.geobatch.registry.AliasRegistrar;
 import it.geosolutions.geobatch.registry.AliasRegistry;
 
 /**
  * 
  *@author Emmanuel Blondel (FAO) - emmanuel.blondel1@gmail.com |
  *        emmanuel.blondel@fao.org
  *
  */
 public class VMEZonalStatsAliasRegistrar extends AliasRegistrar {
 
 	public VMEZonalStatsAliasRegistrar(AliasRegistry registry) {
 		LOGGER.info(getClass().getSimpleName() + ": registering alias.");
 		
		registry.putAlias("VMEZonalStatsConfiguration", VMEZonalStatsConfiguration.class);
 		
 	}
 	
 }
