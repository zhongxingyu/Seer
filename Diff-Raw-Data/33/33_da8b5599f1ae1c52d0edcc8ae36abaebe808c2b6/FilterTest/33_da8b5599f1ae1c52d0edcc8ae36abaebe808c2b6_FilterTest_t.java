 /*
  * Created on 25.10.2004
  *
  */
 package org.w3c.tidy.servlet;
 
 import java.util.Hashtable;
 import java.net.URL;
 
 import com.meterware.httpunit.WebImage;
 import com.meterware.httpunit.WebResponse;
 
 
 /**
  * Tests the JTidyFilter.
  * @author Vlad Skarzhevskyy <a href="mailto:skarzhevskyy@gmail.com">skarzhevskyy@gmail.com</a> 
  * @version $Revision$ ($Author$)
  */
 public class FilterTest extends TidyServletCase
 {
 
     public FilterTest(String name)
     {
         super(name);
     }
     
     /**
      * Returns the tested jsp name.
      * @return jsp name
      */
     public String getJspName()
     {
         return super.getJspName() + MockFilterSupport.FILTERED_EXTENSION;
     }
     
     /**
      * Define filter init Parameters
      * @param initParameters
      */
     public void setFilterInitParameters(Hashtable initParameters) 
     {
         super.setFilterInitParameters(initParameters);
         initParameters.put("config", "alt-text:Alternate");
     }
 
     /**
      * Check that alt in image set properly.
      * @throws Exception any axception thrown during test.
      */
     public void testImageAltText() throws Exception
     {
         WebResponse response =  getResponse();
 
         WebImage[] img = response.getImages();
 
         assertEquals("Expected 2 images in result.", 2, img.length);
         
         assertEquals("alt-text set in config", "Alternate", img[0].getAltText()); 
 
         WebResponse reportResponse =  getReportResponse(response);
         
         validateReport(reportResponse);
     }
     
     public void testNonHTML() throws Exception
     {
        WebResponse response =  getJSPResponse("servlet/FormatedByTagOK.jsp");
 
         WebImage[] img = response.getImages();
 
         assertEquals("Expected 1 images in result.", 1, img.length);
         String src = img[0].getSource();
         log.debug("src:" + src);
         URL url = new URL("http://localhost" + src);
         
         String filterd_src = url.getPath() + MockFilterSupport.FILTERED_EXTENSION + "?"+ url.getQuery();
 
     	WebResponse responseImg = getResponse(filterd_src);
     	assertEquals("Image type", "image/gif", responseImg.getContentType());
     	
     	String requestID = responseImg.getNewCookieValue(Consts.ATTRIBUTE_REQUEST_ID);
     	assertNull("Processed by filter", requestID);
 
     	
     }
 }
