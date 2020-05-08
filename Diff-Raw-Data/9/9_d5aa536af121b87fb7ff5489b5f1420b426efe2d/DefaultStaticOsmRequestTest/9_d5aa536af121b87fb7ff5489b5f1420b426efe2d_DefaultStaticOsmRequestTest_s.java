 package com.aciertoteam.osm.model;
 
 import org.junit.Test;
 
 import java.util.ResourceBundle;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  * @author Bogdan Nechyporenko
  */
 public class DefaultStaticOsmRequestTest {
 
     @Test
     public void createRequestTest() {
         String appKey = ResourceBundle.getBundle("aciertoteam-osm-test").getString("mapquest.application.key");
         String x = "40.0378";
         String y = "-76.305801";
         String url = DefaultStaticOsmRequest.createRequest(x, y).getUrl(appKey);
 
        assertEquals("http://open.mapquestapi.com/staticmap/v4/getmap?key=Fmjtd%7Cluubnu0and%2C72%3Do5-9u1sqf&size=200," +
                "150&zoom=15&center=40.0378,-76.305801&imageType=PNG&scalebar=false", url);
     }
 
     @Test
     public void createRequestWithMarkersTest() {
         String appKey = ResourceBundle.getBundle("aciertoteam-osm-test").getString("mapquest.application.key");
         String x = "40.0378";
         String y = "-76.305801";
 
         DefaultStaticOsmRequest osmRequest = DefaultStaticOsmRequest.createRequest(x, y);
         osmRequest.addMarker(Coordinate.createMarker("40.0378","-76.305801", "green"));
         osmRequest.addMarker(Coordinate.createMarker("40.0379","-76.305802", "yellow"));
 
         String url = osmRequest.getUrl(appKey);
         System.out.println(url);
        assertEquals("http://open.mapquestapi.com/staticmap/v4/getmap?key=Fmjtd%7Cluubnu0and%2C72%3Do5-9u1sqf&size=200," +
                "150&zoom=15&center=40.0378,-76.305801&imageType=PNG&scalebar=false&pois=green,40.0378,-76.305801|yellow,40.0379,-76.305802", url);
     }
 }
