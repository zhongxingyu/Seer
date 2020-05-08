 package org.geoserver.wms;
 
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 
 import javax.imageio.ImageIO;
 
 import org.geoserver.test.GeoServerTestSupport;
 
 public class SLDWithInlineFeatureTest extends GeoServerTestSupport {
    
     public void testSLDWithInlineFeatureWMS() throws Exception {
         BufferedReader reader = 
         new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("SLDWithInlineFeature.xml")));
         String line;
         StringBuilder builder = new StringBuilder();
 
         while ((line = reader.readLine()) != null){
             builder.append(line);
         }
         
         assertStatusCodeForPost(200, "wms", builder.toString(), "text/xml");
                 
         // this is the test; an exception will be thrown if no image was rendered
         BufferedImage image = ImageIO.read(
                 getBinaryInputStream(
                     postAsServletResponse("wms", builder.toString())
                     )
                 );
 
         assertNotNull(image);
     }
 }
