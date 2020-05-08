 package com.artcom.y60.http;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.http.HttpResponse;
 
 import android.net.Uri;
 
 import com.artcom.y60.Logger;
 
 public class HttpHelperTest extends HttpTestCase {
     
     private static final String LOG_TAG = "HttpHelperTest";
     
     public void test404() throws Exception {
         
         try {
             HttpResponse response = HttpHelper.get(getServer().getUri() + "/not-existing");
             Logger.v(LOG_TAG, response.getStatusLine().getStatusCode(), " - ", response
                     .getStatusLine());
             fail("expected a 404 exception!");
             
         } catch (HttpClientException ex) {
             
             assertEquals("expected a 404 exception", 404, ex.getStatusCode());
             Logger.v(LOG_TAG, ex.getMessage());
         }
     }
     
     public void testSimpleUrlEncoding() {
         assertEquals("a+funky+test", URLEncoder.encode("a funky test"));
     }
     
     public void testUrlEncodingAHashMap() throws UnsupportedEncodingException {
         Map<String, String> map = new HashMap<String, String>();
         map.put("key1", "A & B");
         map.put("key%", "C & D");
         
         String encoded = HttpHelper.urlEncodeKeysAndValues(map);
         String[] keyValuePairs = encoded.split("&");
         Map<String, String> decodedMap = new HashMap<String, String>();
         int counter;
         for (counter = 0; counter < keyValuePairs.length; counter++) {
         	String decoded = URLDecoder.decode(keyValuePairs[counter], "UTF-8");
         	String[] keyValuePair;
         	keyValuePair = decoded.split("=");
         	decodedMap.put(keyValuePair[0], keyValuePair[1]);
         }
         assertEquals("A & B", decodedMap.get("key1"));
         assertEquals("C & D", decodedMap.get("key%"));
     }
     
     public void testUrlEncodingTheValuesOfAHashMap() throws UnsupportedEncodingException {
         Map<String, String> map = new HashMap<String, String>();
         map.put("key[1]", "A & B");
         map.put("key%", "C & D");
         
         String encoded = HttpHelper.urlEncodeValues(map);
         String[] keyValuePairs = encoded.split("&");
         Map<String, String> decodedMap = new HashMap<String, String>();
         int counter;
         for (counter = 0; counter < keyValuePairs.length; counter++) {
         	String[] keyValuePair = keyValuePairs[counter].split("=");
         	keyValuePair[1] = URLDecoder.decode(keyValuePair[1], "UTF-8");
         	decodedMap.put(keyValuePair[0], keyValuePair[1]);
         }
         assertEquals("A & B", decodedMap.get("key[1]"));
         assertEquals("C & D", decodedMap.get("key%"));
     }
     
     public void testParsingSomeUrls() {
         assertParsable("http://localhost:4000");
         assertParsable("http://localhost:4000/test");
         assertParsable("http://localhost:4000/test-heee");
         assertParsable("http://localhost/test?message=hello");
         assertParsable("http://localhost:4000?message=hello");
         assertParsable("http://localhost:4000&message=hello");
         assertNotParsable("http://localhost:4000?message=hello world");
         assertNotParsable("http://%%ocalhost:4000§§ &m  []  = =");
     }
     
     private void assertParsable(String uri) {
         assertEquals(Uri.parse(uri).toString(), URI.create(uri).toString());
     }
     
     private void assertNotParsable(String uri) {
         boolean parsable = true;
         try {
             assertEquals(Uri.parse(uri).toString(), URI.create(uri).toString());
         } catch (IllegalArgumentException e) {
             parsable = false;
         }
         assertFalse(uri + "should not be parsable", parsable);
     }
 }
