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
  *         Pablo Orduña <pablo.orduna@deusto.es>
  *         Eduardo Castillejo <eduardo.castillejo@deusto.es>
  *
  */
 package piramide.interaction.reasoner.db;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Test;
 
 import piramide.interaction.reasoner.Geolocation;
 
 @SuppressWarnings("boxing")
 public class DatabaseManagerTest {
 
 	@Test
 	public void testGetResultsInt() throws Exception {
 		final IDatabaseManager manager = new WrappedDatabaseManager();
 		
 		final MobileDevices mobileDevices = manager.getResults();
 		final List<MobileDevice> devices = mobileDevices.getMobileDevices();
 		assertEquals(2, devices.size());
 		
 		final MobileDevice device1 = devices.get(0);
 		final MobileDevice device2 = devices.get(1);
 		
 		assertEquals("htc snap s523",        device1.getName());
 		assertEquals("sony ericsson w880iv", device2.getName());
 		
 		final Map<Number, Double> results2trends = mobileDevices.getValue2trend(DeviceCapability.reso_size);
 		assertEquals(2, results2trends.size());
 		assertEquals(0.0, results2trends.get(0), 0.01);
		assertEquals(22.5, results2trends.get(76800), 0.01);
 	}
 	
 	@Test
 	public void testGeo() throws Exception {
 		final IDatabaseManager manager = new WrappedDatabaseManager();
 		
 		final MobileDevices mobileDevicesAll = manager.getResults();
 		MobileDevice device1all = mobileDevicesAll.getMobileDevices().get(0);
 		assertEquals(25.0, device1all.getTotalTrend(), 0.0001);
 		
 		final MobileDevices mobileDevicesJapan = manager.getResults(new Geolocation("jp"));
 		MobileDevice device1japan = mobileDevicesJapan.getMobileDevices().get(0);
 		assertEquals(100.0, device1japan.getTotalTrend(), 0.0001);
 		
 		final MobileDevices mobileDevicesSpain = manager.getResults(new Geolocation("es"));
 		MobileDevice device1spain = mobileDevicesSpain.getMobileDevices().get(0);
 		assertEquals(50.0, device1spain.getTotalTrend(), 0.0001);
 	}
 }
