 /*
  * Copyright (C) 2010 PIRAmIDE-SP3 authors
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * This software consists of contributions made by many individuals, 
  * listed below:
  *
  * Author: Aitor Almeida <aitor.almeida@deusto.es>
 *         Pablo Ordu�a <pablo.orduna@deusto.es>
  *         Eduardo Castillejo <eduardo.castillejo@deusto.es>
  *
  */
 package piramide.interaction.reasoner;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.List;
 
 import org.junit.Test;
 
 import piramide.interaction.reasoner.db.MobileDevice;
 
 
 public class FuzzyReasonerFacadeTest {
 	
 	@Test
 	public void testGenerateFISobject() throws Exception{
 		FuzzyReasonerWizardFacade facade = new FuzzyReasonerWizardFacade();
 		MobileDevice device = facade.retrieveDeviceID("nokia 6630");
 		assertEquals("nokia 6630", device.getName());
		assertEquals(433.219999999, device.getTotalTrend(), 0.0001);
 		assertEquals(2, device.getCapabilities().size());
 	}
 
 
 	@Test
 	public void testSearchDeviceNames() throws Exception{
 		FuzzyReasonerWizardFacade facade = new FuzzyReasonerWizardFacade();
 		List<MobileDevice> devices = facade.searchDeviceNames("nokia", 1000);
		assertEquals(337, devices.size());
 	}
 
 	@Test
 	public void testGetGeoLocationRegions() throws Exception{
 		FuzzyReasonerWizardFacade facade = new FuzzyReasonerWizardFacade();
 		String[] locs = facade.getGeolocationRegions();
 		assertEquals(3, locs.length);
 	}
 	
 	// testGetInferredValues()
 	// en realidad es parecdo a lo hecho en FuzzyReasonerTest.testInferNewCapabilities()
 
 	
 }
