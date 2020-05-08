 /*******************************************************************************
  * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
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
 
 package test;
 
 import java.io.File;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import org.openspaces.admin.Admin;
 import org.openspaces.admin.AdminFactory;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.ProcessingUnitInstance;
 
 import framework.utils.AssertUtils;
 import framework.utils.LogUtils;
 import framework.utils.AssertUtils.RepetitiveConditionProvider;
 import framework.utils.DumpUtils;
 
 
 public abstract class AbstractTestSupport {
 
 	public static final long DEFAULT_TEST_TIMEOUT = 15 * 60 * 1000;
 	public static final long OPERATION_TIMEOUT = 5 * 60 * 1000;
 	public static final String SUSPECTED = "SUSPECTED";
 	
	private static final int CREATE_ADMIN_TIMEOUT = 120 * 1000; // two minutes
 
 	protected Admin createAdmin() throws TimeoutException, InterruptedException {
 		
 		long endTime = System.currentTimeMillis() + CREATE_ADMIN_TIMEOUT;
 
 		while (System.currentTimeMillis() < endTime) {
 			Admin admin = createAdminFactory().create();
 
 			try {
 				if (!isFilteredAdmin()) {
					AssertUtils.assertTrue("Failed to discover lookup service even though admin was created", admin.getLookupServices().waitFor(1, 1, TimeUnit.MINUTES));
					AssertUtils.assertTrue("Failed to discover lookup service even though admin was created", admin.getProcessingUnits().waitFor("rest", 1, TimeUnit.MINUTES) != null);
 					return admin;
 				} else {
 					return admin; // filtered, cant wait for anything
 				}
 			} catch (final AssertionError ae) {
 				LogUtils.log("Failed to create admin succesfully --> " + ae.getMessage());
 				LogUtils.log("Closing admin and retrying");
 				if (admin != null) {
 					admin.close();
 					admin = null;
 				}
 				Thread.sleep(5000);
 			}
 
 		}
 		throw new TimeoutException("Timed out while creating admin");
 	}
 	
 	protected AdminFactory createAdminFactory() {
 		return new AdminFactory();
 	}
 	
 	protected boolean isFilteredAdmin() {
 		return false;
 	}
 	
 	protected File getTestFolder() {
 	    return DumpUtils.getTestFolder();
 	}
 
 	protected File getBuildFolder() {
 	    return DumpUtils.getTestFolder().getParentFile();    
 	}
 
 	public static void repetitiveAssertTrue(String message, RepetitiveConditionProvider condition, long timeoutMilliseconds) {
 		AssertUtils.repetitiveAssertTrue(message, condition, timeoutMilliseconds);
 	}
 
 	public static void assertEquals(int expected, int actual) {
 		AssertUtils.assertEquals(expected, actual);
 	}
 
 	public static void assertEquals(String msg, Object expected, Object actual) {
 		AssertUtils.assertEquals(msg, expected, actual);
 	}
 
 	public static void assertEquals(Object expected, Object actual) {
 		AssertUtils.assertEquals(expected, actual);
 	}
 
 	public static ProcessingUnitInstance[] repetitiveAssertNumberOfInstances(ProcessingUnit pu, int expectedNumberOfInstances) {
 		return AssertUtils.repetitiveAssertNumberOfInstances(pu,expectedNumberOfInstances);
 	}
 
 	public static void reptitiveCountdownLatchAwait(CountDownLatch latch, String name, long timeout, TimeUnit timeunit) {
 		AssertUtils.reptitiveCountdownLatchAwait(latch, name, timeout, timeunit);
 	}
 	public static void AssertFail(String msg) {
 		AssertUtils.assertFail(msg);
 	}
 
 	public static void AssertFail(String msg, Exception e) {
 		AssertUtils.assertFail(msg, e);
 	}
 
 	public static void assertNotNull(String msg, Object obj) {
 		AssertUtils.assertNotNull(msg, obj);
 	}
 
 	public static void assertNotNull(Object obj) {
 		AssertUtils.assertNotNull(obj);
 	}
 
 	public static void assertTrue(String msg, boolean cond) {
 		AssertUtils.assertTrue(msg, cond);
 	}
 
 	public static void assertTrue(boolean cond) {
 		AssertUtils.assertTrue(cond);
 	}
 
 	public static void assertEquals(String msg, int a, int b) {
 		AssertUtils.assertEquals(msg, a, b);
 	}
 
 	public static boolean sleep(long millisecs) {
 		try {
 			Thread.sleep(millisecs);
 			return true;
 		} catch (InterruptedException e) {
 			// no op
 		}
 		return false;		
 	}
 
 }
