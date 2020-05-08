 package com.hoccer.api;
 
import static org.junit.Assert.assertEquals;
 
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
 
 public class TestRESTfulApi {
 
     DefaultHttpClient mHttpClient = new DefaultHttpClient();
 
     @Test
     public void testReadingNonexistentClientId() throws Exception {
 
         HttpPost request = new HttpPost(ClientConfig.getLinccerBaseUri()
                 + "/c278d820-d1f0-11df-bd3b-0800200c9a66");
         request.setEntity(new StringEntity("{}"));
         HttpResponse response = mHttpClient.execute(request);
         assertEquals("should get 'precondition failed' error for nonexisten client uri", 404,
                 response.getStatusLine().getStatusCode());
     }
 
     @Test(timeout = 1000)
     public void lonlyReceive() throws Exception {
 
         HttpPut envUpdate = new HttpPut(ClientConfig.getLinccerBaseUri()
                 + "/clients/c278d820-d1f0-11df-bd3b-0800200c9a66/environment");
         envUpdate.setEntity(new StringEntity(
                "{\"gps\": {\"longitude\": 13, \"latitude\": 50, \"accuracy\": 100} }"));
         HttpResponse response = mHttpClient.execute(envUpdate);
         assertEquals("should have updated the environment but server said "
                 + EntityUtils.toString(response.getEntity()), 201, response.getStatusLine()
                 .getStatusCode());
         envUpdate.abort();
 
         HttpGet receive = new HttpGet(ClientConfig.getLinccerBaseUri()
                 + "/clients/c278d820-d1f0-11df-bd3b-0800200c9a66/action/one-to-one");
         response = mHttpClient.execute(receive);
         String body = (response.getEntity() != null) ? EntityUtils.toString(response.getEntity())
                 : "<no body>";
         assertEquals("should have been told about 'no data' but server said " + body, 204, response
                 .getStatusLine().getStatusCode());
     }
 }
