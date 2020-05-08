 /*
  * Created on 26.09.2004
  *
  */
 package org.w3c.tidy.servlet;
 
 import com.meterware.httpunit.WebResponse;
 import com.meterware.httpunit.WebImage;
 
 import org.w3c.tidy.servlet.Consts;
 
 /**
  *
  * @author Vlad Skarzhevskyy <a href="mailto:skarzhevskyy@gmail.com">skarzhevskyy@gmail.com</a> 
  * @version $Revision$ ($Author$)
  */
 public class ReportsTest extends TidyServletCase {
 
     /**
      * Instantiates a new test case.
      * @param name test name
      */
     public ReportsTest(String name)
     {
         super(name);
     }
 
     public void validateReportFromJSP(String jsp) throws Exception
     {
         WebResponse response =  getJSPResponse(jsp);
 
         WebImage[] img = response.getImages();
         assertEquals("Expected 1 image in result.", 1, img.length);
 
         String requestID1 = response.getNewCookieValue(Consts.ATTRIBUTE_REQUEST_ID);
         
         WebResponse reportResponse =  getReportResponse(response);
         
         validateReport(reportResponse);
     }
 
     /**
      * Check that HTML is correct and validation is fine.
      * Also verify theat Reports are generated.
      * @param jspName jsp name, with full path
      * @throws Exception any axception thrown during test.
      */
     public void testServletOK() throws Exception
 	{
    	validateReportFromJSP("servlet/formatedByTagOK.jsp");
 	}
     
     public void testServletWarnings() throws Exception
 	{
     	validateReportFromJSP("servlet/FormatedByTagWarnings.jsp");
 	}
 	
     public void testTag() throws Exception
     {
    	WebResponse response =  getJSPResponse("servlet/formatedByTagOK.jsp");
 
         String requestID1 = response.getNewCookieValue(Consts.ATTRIBUTE_REQUEST_ID);
         
         WebResponse reportResponse =  getResponseQuery(new String[] {"requestID", requestID1});
 
         validateReport(reportResponse);
     }
     
     public void testNoData() throws Exception
 	{
     	WebResponse reportResponse =  getResponse();
     	assertTrue("No data should be found", reportResponse.getText().indexOf("No data") > 0);
     	
     	reportResponse =  getResponseQuery(new String[] {"requestID", "1100"});
     	assertTrue("No data should be found", reportResponse.getText().indexOf("No data") > 0);
 	}
 }
