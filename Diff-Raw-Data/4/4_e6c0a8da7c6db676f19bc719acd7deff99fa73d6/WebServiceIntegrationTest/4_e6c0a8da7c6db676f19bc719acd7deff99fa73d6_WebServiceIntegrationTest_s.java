 package org.youfood.test;
 
 import com.meterware.httpunit.WebConversation;
 import com.meterware.httpunit.WebResponse;
 import org.apache.log4j.Logger;
 import org.junit.Before;
 import org.junit.Test;
 import org.xml.sax.SAXException;
 
 import java.io.IOException;
 
 import static org.junit.Assert.*;
 
 /**
  * @author: Antoine ROUAZE <antoine.rouaze AT zenika.com>
  */
 public class WebServiceIntegrationTest {
 
     private static final Logger LOGGER = Logger.getLogger(WebServiceIntegrationTest.class);
     public static final String URL = "http://localhost:8080/youfood/resources/helloworld/";
 
     private WebConversation webConversation;
 
     @Before
     public void setUp() {
         webConversation = new WebConversation();
     }
 
     @Test
     public void testDoGet() {
         try {
             WebResponse webResponse = webConversation.getResponse(URL);
             assertTrue(webResponse.getText().equals("Hello world !"));
         } catch (IOException e) {
             LOGGER.error("Unexpected exception in test. Is Jetty Running at: " + URL + "? ->", e);
         } catch (SAXException e) {
             LOGGER.error("Unexpected exception in test. Is Jetty Running at: " + URL + "? ->", e);
         }
     }
 }
