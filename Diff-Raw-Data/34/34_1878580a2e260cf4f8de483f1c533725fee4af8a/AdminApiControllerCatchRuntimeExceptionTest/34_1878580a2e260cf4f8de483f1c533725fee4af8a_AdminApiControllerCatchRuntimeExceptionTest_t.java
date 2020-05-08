 package org.cloudifysource.quality.iTests.test.rest;
 
import iTests.framework.utils.AssertUtils;

 import java.net.URL;
 
import org.apache.http.client.HttpClient;
 import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.AbstractLocalCloudTest;
import org.testng.annotations.Test;
 
 public class AdminApiControllerCatchRuntimeExceptionTest extends AbstractLocalCloudTest {
 
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 5, groups = "1", enabled = true)
 	public void arrayIlegalIndexTest() throws Exception {
 		String machinesUrl = restUrl + "/admin/Machines/Machines/10";
 			// try to access a none existing url
 		try {
			getURLContent(new URL(machinesUrl));
 			AssertUtils.assertFail("Should be able to run commands on a non existing url = " + machinesUrl);
 		} catch (HttpResponseException e) {
 			AssertUtils.assertTrue("Http exception should be HttpNotFound", e.getMessage().contains("Not Found"));
 		}
 	}
	
	public static String getURLContent(final URL url) throws Exception {
		HttpClient client = null; 
		try {
			client = new DefaultHttpClient();
			HttpGet get = new HttpGet(url.toURI());
			return client.execute(get, new BasicResponseHandler());

		}
		finally {
			if (client != null) {
				client.getConnectionManager().shutdown();
			}
		}        
	}

 }
