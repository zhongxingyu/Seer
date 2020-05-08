 package org.cloudifysource.quality.iTests.test.cli.cloudify;
 
 import java.io.IOException;
 
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.testng.annotations.Test;
 
 public class RestErrorExceptionsTest extends AbstractLocalCloudTest{
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, groups = "1", enabled = true)
 	public void testCommandsErrorMessages() throws IOException, InterruptedException {
 
 		String invokeOutput = CommandTestUtils.runCommandExpectedFail("connect " + this.restUrl + ";invoke someService CommandName");
 		AbstractTestSupport.assertTrue("Expecting output to contain \"Failed to locate service someService\"",
                 invokeOutput.contains("Failed to locate service someService"));
 
 		String uninstallOutput = CommandTestUtils.runCommandExpectedFail("connect " + this.restUrl + "; uninstall-application someApplication");
		AbstractTestSupport.assertTrue("Expecting output to contain \"Application someApplication could not be found\", but uninstallOutput = " 
				+ uninstallOutput, uninstallOutput.contains("Application someApplication could not be found"));
 
 		String tailOutput = CommandTestUtils.runCommandExpectedFail("connect " + this.restUrl + "; tail someService 20");
 		AbstractTestSupport.assertTrue("Expecting output to contain \"Failed to locate service someService\"",
                 tailOutput.contains("Failed to locate service someService"));
 
 		String listServicesOutput = CommandTestUtils.runCommandExpectedFail("connect " + this.restUrl + ";use-application someApp;list-services");
 		AbstractTestSupport.assertTrue("Expecting output to contain \"Application someApp could not be found or has no services deployed\"",
                 listServicesOutput.contains("Application someApp could not be found or has no services deployed"));
 
 	}
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, groups = "1", enabled = true)
 	public void testRestErrorHttpStatus() throws ClientProtocolException, IOException {
 		DefaultHttpClient httpClient = new DefaultHttpClient();
 
 		String badInvokeUrl = "/service/applications/someAppName/services/someServiceName/beans/someBean/invoke";
         int invokeHttpStatus = getPostResponseHttpStatus(badInvokeUrl, httpClient);
         AbstractTestSupport.assertTrue("Expecting an internal server error response with http status 500. Got status: " + invokeHttpStatus,
                 HttpStatus.SC_INTERNAL_SERVER_ERROR == invokeHttpStatus);
 
         String badListServicesUrl = "/service/applications/someAppName/services";
         int listServicesHttpStatus = getGetResponseHttpStatus(badListServicesUrl, httpClient);
         AbstractTestSupport.assertTrue("Expecting an internal server error response with http status 500. Got status: " + listServicesHttpStatus,
                 HttpStatus.SC_INTERNAL_SERVER_ERROR == listServicesHttpStatus);
 	}
 
 	private int getGetResponseHttpStatus(String relativeUrl, DefaultHttpClient httpClient) throws ClientProtocolException, IOException {
 		final String url = this.restUrl + relativeUrl;
         final HttpGet httpMethod = new HttpGet(url);
         HttpResponse response = null;
         try {
         	response = httpClient.execute(httpMethod);
         	return response.getStatusLine().getStatusCode();
         } finally {
         	if (response != null) {
         		EntityUtils.consume(response.getEntity());
         	}
         }
 	}
 
 	private int getPostResponseHttpStatus(String relativeUrl, DefaultHttpClient httpClient) throws ClientProtocolException, IOException {
 		final String url = this.restUrl + relativeUrl;
         final HttpPost httpMethod = new HttpPost(url);
         HttpResponse response = null;
         try {
         	response = httpClient.execute(httpMethod);
         	return response.getStatusLine().getStatusCode();
         } finally {
         	if (response != null) {
         		EntityUtils.consume(response.getEntity());
         	}
         }
 	}
 
 }
