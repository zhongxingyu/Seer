 package org.geoserver.gwc;
 
 import org.geoserver.data.test.MockData;
 import org.geoserver.test.GeoServerTestSupport;
 
 import com.mockrunner.mock.web.MockHttpServletResponse;
 
 public class GWCIntegrationTest extends GeoServerTestSupport {
 
     public void testPngIntegration() throws Exception {
        // Temporary patch to try to prevent intermittent failures on the build server,
        // Permanent fix is to change locking in TileLayerDispatcher, so that the lock
        // is acquired before the configuration parsing thread is launched.
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ie) {
            // We dont really care
        }
       
         String layerId = getLayerId(MockData.BASIC_POLYGONS);
         MockHttpServletResponse sr = getAsServletResponse("gwc/service/wmts?request=GetTile&layer="
                 + layerId
                 + "&format=image/png&tilematrixset=EPSG:4326&tilematrix=EPSG:4326:0&tilerow=0&tilecol=0");
         assertEquals(200, sr.getErrorCode());
         assertEquals("image/png", sr.getContentType());
     }
 }
