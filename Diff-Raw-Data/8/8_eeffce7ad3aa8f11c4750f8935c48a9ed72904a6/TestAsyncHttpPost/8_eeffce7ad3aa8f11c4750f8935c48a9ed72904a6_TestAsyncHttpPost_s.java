 package com.artcom.y60.http;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.http.client.params.HttpClientParams;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 
 import com.artcom.y60.TestHelper;
 
 public class TestAsyncHttpPost extends HttpTestCase {
     
     private static final String LOG_TAG = "TestAsyncHttpPost";
     AsyncHttpPost               mHttpPost;
     
     private void assertRequestIsDone() throws Exception {
         TestHelper.blockUntilTrue("request should have been performed by now", 3000,
                 new TestHelper.Condition() {
                     
                     @Override
                     public boolean isSatisfied() throws Exception {
                         return mHttpPost.isDone();
                     }
                 });
     }
     
     public void testExecution() throws Exception {
         
         getServer().setResponseDelay(200);
         mHttpPost = new AsyncHttpPost(getServer().getUri());
         mHttpPost.start();
         
         TestHelper.blockUntilTrue("request should have started by now", 1000,
                 new TestHelper.Condition() {
                     @Override
                     public boolean isSatisfied() throws Exception {
                         return mHttpPost.getProgress() > 0;
                     }
                 });
         assertTrue("request should have started, but progress is " + mHttpPost.getProgress() + "%",
                 mHttpPost.isRunning());
         
         TestHelper.blockUntilTrue("request should have got the response by now", 4000,
                 new TestHelper.Condition() {
                     @Override
                     public boolean isSatisfied() throws Exception {
                         return mHttpPost.getProgress() > 1;
                     }
                 });
         
         assertRequestIsDone();
     }
     
     public void testEmptyPost() throws Exception {
         
         mHttpPost = new AsyncHttpPost(getServer().getUri());
         mHttpPost.start();
         
         TestHelper.blockUntilEquals("request should have been performed by now", 2000,
                "no message was given", new TestHelper.Measurement() {
                     
                     @Override
                     public Object getActualValue() throws Exception {
                         return mHttpPost.getBodyAsString();
                     }
                 });
     }
     
     public void testPostWithUrlEncodedParametersAsString() throws Exception {
         
         mHttpPost = new AsyncHttpPost(getServer().getUri());
        mHttpPost.setBody("message=test post");
         mHttpPost.start();
         assertRequestIsDone();
         assertEquals("should receive what was posted", "test post", mHttpPost.getBodyAsString());
     }
     
     public void testPostWithUrlEncodedParametersAsHashMap() throws Exception {
         
         mHttpPost = new AsyncHttpPost(getServer().getUri());
         Map<String, String> params = new HashMap<String, String>();
         params.put("message", "test post with hash map");
         mHttpPost.setBody(params);
         mHttpPost.start();
         assertRequestIsDone();
         assertEquals("should receive what was posted", "test post with hash map", mHttpPost
                 .getBodyAsString());
     }
     
     public void testGettingBodyWithoutPerformingTheRequest() throws Exception {
         
         mHttpPost = new AsyncHttpPost(getServer().getUri());
         TestHelper.blockUntilEquals("request should give empty body before started", 1000, "",
                 new TestHelper.Measurement() {
                     
                     @Override
                     public Object getActualValue() throws Exception {
                         return mHttpPost.getBodyAsString();
                     }
                 });
     }
     
     public void testGettingNoifiedAbooutSuccessViaResponseHandler() throws Exception {
         
         mHttpPost = new AsyncHttpPost(getServer().getUri());
         ResponseHandlerForTesting requestStatus = new ResponseHandlerForTesting();
         mHttpPost.registerResponseHandler(requestStatus);
         mHttpPost.start();
         TestHelper.blockUntilTrue("request should have started", 1000, new TestHelper.Condition() {
             
             @Override
             public boolean isSatisfied() throws Exception {
                 return mHttpPost.isRunning();
             }
         });
         assertTrue("should be connecting", requestStatus.isConnecting);
         assertRequestIsDone();
         assertTrue("should be successful", requestStatus.wasSuccessful);
         assertNotNull("should have an response body", requestStatus.body);
        assertEquals("response should come from mocked server", "no message was given",
                 requestStatus.body.toString());
     }
     
     public void testGettingNoifiedAbooutFailureViaResponseHandler() throws Exception {
         
         mHttpPost = new AsyncHttpPost(getServer().getUri() + "/not-a-valid-address");
         final ResponseHandlerForTesting requestStatus = new ResponseHandlerForTesting();
         mHttpPost.registerResponseHandler(requestStatus);
         mHttpPost.start();
         TestHelper.blockUntilTrue("request should have called onError", 2000,
                 new TestHelper.Condition() {
                     
                     @Override
                     public boolean isSatisfied() throws Exception {
                         return requestStatus.hasError;
                     }
                 });
         assertFalse("should not be successful", requestStatus.wasSuccessful);
         assertNotNull("should have no response body", requestStatus.body);
         assertEquals("response from mocked server should describe 404", "Not Found",
                 requestStatus.body.toString());
     }
     
     public void testCustomHttpClientToSupressRedirectionAfterPost() throws Exception {
         HttpParams httpParams = new BasicHttpParams();
         HttpClientParams.setRedirecting(httpParams, false);
         DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
         mHttpPost = new AsyncHttpPost(getServer().getUri(), httpClient);
         mHttpPost.start();
         assertRequestIsDone();
         assertTrue("should have an location header", mHttpPost.getHeader("Location").contains(
                 "http://localhost"));
         TestHelper.assertIncludes("response from mocked server should describe 303",
                 "see other: http://localhost", mHttpPost.getBodyAsString().toString());
     }
     
     public void testUriOfSolvableRequest() throws Exception {
         mHttpPost = new AsyncHttpPost(getServer().getUri());
         assertEquals("should get uri of creation", getServer().getUri(), mHttpPost.getUri());
         mHttpPost.start();
         assertEquals("should get uri of creation", getServer().getUri(), mHttpPost.getUri());
         assertRequestIsDone();
         assertEquals(
                 "uri should now point to resource whre the post request got redirected indirectly",
                 getServer().getUri() + getServer().getLastRequest().uri, mHttpPost.getUri());
     }
     
     public void testUriOf404Request() throws Exception {
         String uri = getServer().getUri() + "/not-existing";
         mHttpPost = new AsyncHttpPost(uri);
         assertEquals("should get uri of creation", uri, mHttpPost.getUri());
         mHttpPost.start();
         assertEquals("should get uri of creation", uri, mHttpPost.getUri());
         assertRequestIsDone();
         assertEquals("should get uri of creation", uri, mHttpPost.getUri());
     }
 }
