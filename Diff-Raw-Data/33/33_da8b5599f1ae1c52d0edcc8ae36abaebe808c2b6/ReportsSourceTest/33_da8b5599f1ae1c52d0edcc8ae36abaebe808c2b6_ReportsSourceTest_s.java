 /*
  * Created on 26.09.2004
  *
  */
 package org.w3c.tidy.servlet;
 
 import com.meterware.httpunit.WebResponse;
 
 import org.w3c.tidy.servlet.Consts;
 
 /**
  *
  * @author Vlad Skarzhevskyy <a href="mailto:skarzhevskyy@gmail.com">skarzhevskyy@gmail.com</a>
  * @version $Revision$ ($Author$)
  */
 public class ReportsSourceTest extends TidyServletCase {
 
     /**
      * Instantiates a new test case.
      * @param name test name
      */
     public ReportsSourceTest(String name)
     {
         super(name);
     }
 
     public void testSourceResult() throws Exception
     {
    	WebResponse response =  getJSPResponse("servlet/formatedByTagOK.jsp");
 
         String requestID1 = response.getNewCookieValue(Consts.ATTRIBUTE_REQUEST_ID);
         
         WebResponse reportResponse =  getResponseQuery(new String[] {"requestID", requestID1});
 
         //assertEquals("Messages exists exists", 1, reportResponse.getElementsWithName("JTidyMessagesTable").length);
         assertTrue("Messages exists exists", reportResponse.getText().indexOf("JTidyMessagesTable") > 0);
         //assertEquals("Result Source code exists", 1, reportResponse.getElementsWithName("JTidyHtmlResult").length);
         assertTrue("Result Source code exists", reportResponse.getText().indexOf("JTidyHtmlResult") > 0);
         //assertEquals("Source code exists", 0, reportResponse.getElementsWithName("JTidyOriginalSource").length);
         assertTrue("Source code not exists", reportResponse.getText().indexOf("JTidyOriginalSource") == -1);
     }
 }
