 package org.codehaus.xfire.xmpp;
 
 import org.codehaus.xfire.aegis.AbstractXFireAegisTest;
 import org.codehaus.xfire.client.Client;
 import org.codehaus.xfire.service.OperationInfo;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.binding.ObjectInvoker;
 import org.codehaus.xfire.service.binding.ObjectServiceFactory;
 import org.codehaus.xfire.test.Echo;
 import org.codehaus.xfire.test.EchoImpl;
 import org.codehaus.xfire.transport.Channel;
 import org.codehaus.xfire.transport.DefaultTransportManager;
 import org.codehaus.xfire.transport.Transport;
import org.codehaus.xfire.transport.TransportManager;
 
 
 /**
  * XFireTest
  *
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  */
 public class XMPPClientTest
         extends AbstractXFireAegisTest
 {
     private Transport clientTrans;
     private Transport serverTrans;
 
     String username = "xfireTestServer";
     String password = "password1";
     String server = "bloodyxml.com";
     String id = username + "@" + server;
     
     public void setUp()
             throws Exception
     {
         super.setUp();
 
         clientTrans = new XMPPTransport(getXFire(), server, "xfireTestClient", "password2");
         serverTrans = new XMPPTransport(getXFire(), server, username, password);
         
         getXFire().getTransportManager().register(serverTrans);
 
         ObjectServiceFactory factory = (ObjectServiceFactory) getServiceFactory();
         factory.addSoap11Transport(XMPPTransport.BINDING_ID);
         
         Service service = factory.create(Echo.class);
         service.setProperty(ObjectInvoker.SERVICE_IMPL_CLASS, EchoImpl.class);
         getServiceRegistry().register(service);
         // XMPPConnection.DEBUG_ENABLED = true;       
     }
     
     protected void tearDown()
         throws Exception
     {
         clientTrans.dispose();
         serverTrans.dispose();
         
         super.tearDown();
     }
 
     public void testInvoke()
             throws Exception
     {
         Channel serverChannel = serverTrans.createChannel("Echo");
 
        TransportManager tm = new DefaultTransportManager();
         tm.register(clientTrans);
        
         ObjectServiceFactory sf = new ObjectServiceFactory(tm);
         sf.addSoap11Transport(XMPPTransport.BINDING_ID);
         Service serviceModel = sf.create(Echo.class);
         Client client = new Client(clientTrans, serviceModel, id + "/Echo");
         client.setTimeout(10000);
      
         OperationInfo op = serviceModel.getServiceInfo().getOperation("echo");
         Object[] response = client.invoke(op, new Object[] {"hello"});
 
         assertNotNull(response);
         assertEquals(1, response.length);
         
         String resString = (String) response[0];
         assertEquals("hello", resString);
     }
 }
