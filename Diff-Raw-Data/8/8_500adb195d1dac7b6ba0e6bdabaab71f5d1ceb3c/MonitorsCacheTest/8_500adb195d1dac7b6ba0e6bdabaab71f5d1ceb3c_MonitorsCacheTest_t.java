 /*******************************************************************************
  * Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *******************************************************************************/
 package test.cli.cloudify;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 
 import net.jini.core.discovery.LookupLocator;
 
 import org.cloudifysource.dsl.internal.DSLException;
 import org.cloudifysource.dsl.internal.packaging.PackagingException;
 import org.openspaces.admin.Admin;
 import org.openspaces.admin.AdminFactory;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.ProcessingUnitInstance;
 import org.openspaces.pu.service.ServiceMonitors;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import framework.utils.LogUtils;
 
 /***************************
  * Checks that the USM caches monitors results instead of calculating them each time. This is done by creating a second
  * admin instance, with the same setting as the admin from the base class. The results from both admin monitor results
  * should be the same almost always.
  * 
  * @author barakme
  * 
  */
 public class MonitorsCacheTest extends AbstractLocalCloudTest {
 
 	// install a service with 2 instances.
 	// test polling for lifecycle events on same service for both instances.
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void test()
 			throws IOException, InterruptedException, PackagingException, DSLException {
 
 		//this.isDevEnv = true;
 		final String usmServicePath = getUsmServicePath("simpleGroovy");
 		CommandTestUtils.runCommandAndWait("connect " + restUrl
 				+ "; install-service "
 				+ usmServicePath + "; exit;");
 
 		final AdminFactory factory = new AdminFactory();
 		for (final String group : this.admin.getGroups()) {
 			factory.addGroup(group);
 		}
 
 		for (final LookupLocator locator : this.admin.getLocators()) {
 			factory.addLocator(locator.toString());
 		}
 
 		final Admin admin2 = factory.createAdmin();
 		try {
 			int successCount = 0;
 
 			final long start = System.currentTimeMillis();
 					
 			Set<Long> values = new HashSet<Long>(); 
 			for (int i = 0; i < 10; ++i) {
 				final long valueFromAdmin1 = getDateMonitorFromAdmin(this.admin);
 				LogUtils.log("Value1 is: " + valueFromAdmin1);
 				final long valueFromAdmin2 = getDateMonitorFromAdmin(admin2);
 				LogUtils.log("Value2 is: " + valueFromAdmin2);
 				if (valueFromAdmin1 == valueFromAdmin2) {
					++successCount; // got identical values from different admin instances.
 				}
				values.add(valueFromAdmin1);
 				
				Thread.sleep(3000);
 			}
 			
 			final long end = System.currentTimeMillis();
 			final long intervalMillis = (end - start);
 			final long intervalSeconds = intervalMillis / 1000;
 			
 			final long expectedNumberOfValues = (intervalSeconds / 5) - 1;
 			
 			Assert.assertTrue(values.size() >= expectedNumberOfValues, "Expected to get at least " + expectedNumberOfValues + " two different timestamp values, got: " + values.size());
 			
 			Assert.assertTrue(successCount >= 4, "Expected service monitor values from admin instances to be the same at least 4 times out of 10, got: " + successCount); 
 			
 					
 
 		} finally {
 			admin2.close();
 		}
 
 		CommandTestUtils.runCommandAndWait("connect " + restUrl
 				+ "; uninstall-service groovy; exit;");
 
 	}
 
 	private long getDateMonitorFromAdmin(final Admin adminToUse) {
 		ProcessingUnit pu = adminToUse.getProcessingUnits().waitFor("default.groovy", 1, TimeUnit.MINUTES);
 		if (pu == null) {
 			AssertFail("Could not find processing unit 'groovy' in Admin");
 		}
 
 		boolean result = pu.waitFor(1, 1, TimeUnit.MINUTES);
 		if (!result) {
 			AssertFail("Could not find an instance of PU 'groovy' in Admin");
 		}
 
 		ProcessingUnitInstance pui = pu.getInstances()[0];
 
 		Collection<ServiceMonitors> allSserviceMonitors = pui.getStatistics()
 				.getMonitors().values();
 		Map<String, Object> allMonitors = new HashMap<String, Object>();
 		for (ServiceMonitors serviceMonitors : allSserviceMonitors) {
 			allMonitors.putAll(serviceMonitors.getMonitors());
 		}
 
 		Object temp = allMonitors.get("time");
 		assertNotNull("Could not find service monitor with key: 'time'", temp);
 		final long val = (Long) temp;
 		return val;
 
 	}
 
 	private String getUsmServicePath(final String dirOrFilename) {
 		return CommandTestUtils.getPath("apps/USM/usm/" + dirOrFilename);
 	}
 
 }
