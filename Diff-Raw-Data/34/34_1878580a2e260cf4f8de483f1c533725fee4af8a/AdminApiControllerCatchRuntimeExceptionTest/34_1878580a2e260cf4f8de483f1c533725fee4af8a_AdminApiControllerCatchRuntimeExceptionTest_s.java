 package org.cloudifysource.quality.iTests.test.rest;
 
 import java.net.URL;
 
 import org.apache.http.client.HttpResponseException;
import org.testng.annotations.Test;

 import org.cloudifysource.quality.iTests.test.cli.cloudify.AbstractLocalCloudTest;
import iTests.framework.utils.AssertUtils;
import iTests.framework.utils.WebUtils;
 
 public class AdminApiControllerCatchRuntimeExceptionTest extends AbstractLocalCloudTest {
 
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 5, groups = "1", enabled = true)
 	public void arrayIlegalIndexTest() throws Exception {
 		String machinesUrl = restUrl + "/admin/Machines/Machines/10";
 			// try to access a none existing url
 		try {
			WebUtils.getURLContent(new URL(machinesUrl));
 			AssertUtils.assertFail("Should be able to run commands on a non existing url = " + machinesUrl);
 		} catch (HttpResponseException e) {
 			AssertUtils.assertTrue("Http exception should be HttpNotFound", e.getMessage().contains("Not Found"));
 		}
 	}
 }
