 package org.codehaus.xfire.transport.http;
 
 import org.codehaus.xfire.addressing.AddressingInHandler;
 import org.codehaus.xfire.addressing.AddressingOutHandler;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.ServiceRegistry;
 import org.codehaus.xfire.test.AbstractServletTest;
 
 import com.meterware.httpunit.HttpNotFoundException;
 
 public class XFireConfigurableServletNewTest
     extends AbstractServletTest
 {
     public void testServlet()
         throws Exception
     {
         try
         {
             newClient().getResponse("http://localhost/services/");
         }
         catch(HttpNotFoundException e) {}
         
         ServiceRegistry reg = getXFire().getServiceRegistry();
 
         assertTrue(reg.hasService("Echo"));
         Service echo = reg.getService("Echo");
         assertNotNull(echo.getName());
         assertNotSame("", echo.getName().getNamespaceURI());
         
         assertTrue(reg.hasService("Echo1"));
         Service echo1 = reg.getService("Echo1");
         assertNotNull(echo1.getBinding(SoapHttpTransport.SOAP12_HTTP_BINDING));
 
        assertEquals(3, echo1.getInHandlers().size());
        assertTrue(echo1.getInHandlers().get(2) instanceof AddressingInHandler);
         assertEquals(2, echo1.getOutHandlers().size());
        assertTrue(echo1.getOutHandlers().get(1) instanceof AddressingOutHandler);   
         
         assertEquals(2, getXFire().getInHandlers().size());
     }
 
     protected String getConfiguration()
     {
         return "/org/codehaus/xfire/transport/http/configurable-web.xml";
     }
 
 }
