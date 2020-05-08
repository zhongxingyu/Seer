 package com.akzia.googleapi.geocoding;
 
 import com.akzia.googleapi.AbstractResponse;
 import com.akzia.googleapi.AbstractTest;
 import com.akzia.googleapi.common.Bounds;
 import com.akzia.googleapi.common.GeoPoint;
 import com.google.gson.Gson;
 import org.junit.Test;
 
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.net.URISyntaxException;
 
 import static org.junit.Assert.assertTrue;
 
 public class InBoundsTest extends AbstractTest {
 
     @Test
     public void testInBounds() throws Exception {
         test();
     }
 
     @Override
     protected String buildRequest() throws UnsupportedEncodingException, URISyntaxException {
         GoogleGeocodeRequest request = new GoogleGeocodeRequest(false, "sumskaya");
         request.setLanguage("ru");
         request.setBounds(new Bounds(new GeoPoint(56.150398, 38.279438), new GeoPoint(55.449532, 37.059955)));
         return request.buildRequest();
     }
 
     @Override
     protected AbstractResponse parseResponse(Gson gson, Reader reader) {
         return gson.fromJson(reader, GoogleGeocodeResponse.class);
     }
 
     @Override
     protected void advancedAsserts(AbstractResponse response) {
         GoogleGeocodeResponse googleGeocodeResponse = (GoogleGeocodeResponse) response;
        String formattedAddress = googleGeocodeResponse.getResults().get(0).getFormattedAddress();
        try {
            assertTrue(formattedAddress.contains(new String("Москва".getBytes("UTF-8"))));
        } catch (UnsupportedEncodingException e) {

        }
     }
 }
