 package org.fao.fi.vme.geobatch;
 
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import org.fao.fi.vme.geobatch.VMEIngestionAction;
 import org.fao.fi.vme.geobatch.VMEIngestionConfiguration;
 import org.fao.fi.vme.geobatch.VMEIngestionGeneratorService;
 import org.junit.Test;
 
 /**
  * 
  * @author Emmanuel Blondel (FAO) - emmanuel.blondel1@gmail.com |
  *         emmanuel.blondel@fao.org
  * 
  */
 public class VMEIngestionGeneratorServiceTest {
 
 	private static VMEIngestionConfiguration CONFIGURATION = new VMEIngestionConfiguration("id", "name", "description");
	private VMEIngestionGeneratorService generatorService = new VMEIngestionGeneratorService("VMEIngestionGeneratorService");
 
 	@Test
 	public void testConfigurationIsGenerated() {		
 		assertTrue(generatorService.canCreateAction(
 				CONFIGURATION));
 		assertNotNull(generatorService.createAction(CONFIGURATION));
 		assertTrue(generatorService.createAction(CONFIGURATION) instanceof VMEIngestionAction);
 	}
 }
