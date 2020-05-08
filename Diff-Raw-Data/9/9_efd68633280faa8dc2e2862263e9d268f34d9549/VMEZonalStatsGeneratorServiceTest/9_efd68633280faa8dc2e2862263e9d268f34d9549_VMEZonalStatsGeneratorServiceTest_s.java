 package org.fao.fi.vme.geobatch;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import org.junit.Test;
 
 /**
  * 
  *@author Emmanuel Blondel (FAO) - emmanuel.blondel1@gmail.com |
  *        emmanuel.blondel@fao.org
  *
  */
 public class VMEZonalStatsGeneratorServiceTest {
 
 	private static VMEZonalStatsConfiguration CONFIGURATION = new VMEZonalStatsConfiguration("id", "name", "description");
	private VMEZonalStatsGeneratorService generatorService = new VMEZonalStatsGeneratorService("Ds2dsGeoServerGeneratorService");
 
 	@Test
 	public void testConfigurationIsGenerated() {		
 		assertTrue(generatorService.canCreateAction(
 				CONFIGURATION));
 		assertNotNull(generatorService.createAction(CONFIGURATION));
 		assertTrue(generatorService.createAction(CONFIGURATION) instanceof VMEZonalStatsAction);
 	}
 }
