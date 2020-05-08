 package org.vfny.geoserver.wms.responses.map.kml;
 
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import org.apache.xerces.parsers.DOMParser;
 import org.vfny.geoserver.AbstractGeoserverHttpTest;
 import org.w3c.dom.Element;
 import org.xml.sax.InputSource;
 
 import com.meterware.httpunit.GetMethodWebRequest;
 import com.meterware.httpunit.WebConversation;
 import com.meterware.httpunit.WebRequest;
 import com.meterware.httpunit.WebResponse;
 
 public class KMLWriterTest extends AbstractGeoserverHttpTest {
 
	/*
 	public void testIntToHex() {
 		String result = KMLWriter.intToHex(0);
 		assertTrue( result.equals("00"));
 		// make sure that the zeros are prefixed
 		
 		result = KMLWriter.intToHex(90);
 		assertTrue( result.equals("5a"));
 	}
 	
 	public void testSimpleRequest() throws Exception{
 		WebConversation conversation = new WebConversation();
 		String requestParams = 
 				"bbox=-130,24,-66,50&"+
 				"styles=population&"+
 				"request=GetMap&"+
 				"layers=topp:states&"+
 				"width=550&height=250&"+
 				"srs=EPSG:4326&"+
 				"Format=kml";
 		
         WebRequest request = 
         	new GetMethodWebRequest(getBaseUrl()+"/wms?"+requestParams);
         
         WebResponse response = conversation.getResponse( request );
         assertNotNull(response);
         
         DOMParser parser = new DOMParser();
         parser.parse(new InputSource(response.getInputStream()));
         
         Element e = parser.getDocument().getDocumentElement();
         //System.out.println(e.getLocalName());
         assertEquals("kml", e.getLocalName());
 	}
 	
 	public void testRasterRequest() throws Exception{
 		WebConversation conversation = new WebConversation();
 		String requestParams = 
 				"bbox=-130,24,-66,50&"+
 				"styles=population&"+
 				"request=GetMap&"+
 				"layers=topp:states&"+
 				"width=550&height=250&"+
 				"srs=EPSG:4326&"+
 				"Format=kmz"+
 				"KMSCORE=0"; // set tolerance to zero. Guaranteed raster result
 		
         WebRequest request = 
         	new GetMethodWebRequest(getBaseUrl()+"/wms?"+requestParams);
         
         WebResponse response = conversation.getResponse( request );
         assertNotNull(response);
         //System.out.println(response.getContentType());
         assertEquals("application/vnd.google-earth.kmz",response.getContentType());
         ZipInputStream z = new ZipInputStream(response.getInputStream());
         ZipEntry entry = z.getNextEntry();
         //System.out.println(entry.getName());
         assertEquals("wms.kml", entry.getName());
 	}
	*/
 }
