 package org.geoserver.web.demo;
 
 import org.geoserver.web.GeoServerWicketTestSupport;
 
import org.apache.wicket.markup.html.link.ExternalLink;

 public class MapPreviewPageTest extends GeoServerWicketTestSupport {
     public void testValues() throws Exception {
         tester.startPage(MapPreviewPage.class);
        tester.assertComponent("layerContainer:layer:0:layerLink", ExternalLink.class);
     }
 }
