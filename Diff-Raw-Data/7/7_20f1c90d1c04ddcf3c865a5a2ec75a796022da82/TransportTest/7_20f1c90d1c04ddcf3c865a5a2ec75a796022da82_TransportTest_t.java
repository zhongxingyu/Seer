 package org.codehaus.xfire.xmpp;
 
 import javax.xml.stream.XMLStreamException;
 
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.aegis.AbstractXFireAegisTest;
 import org.codehaus.xfire.exchange.InMessage;
 import org.codehaus.xfire.exchange.OutMessage;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.soap.SoapSerializer;
 import org.codehaus.xfire.soap.SoapTransport;
 import org.codehaus.xfire.transport.Channel;
 import org.codehaus.xfire.transport.ChannelEndpoint;
 import org.codehaus.xfire.transport.Transport;
 import org.codehaus.xfire.util.YOMSerializer;
 import org.codehaus.xfire.wsdl.WSDLWriter;
 import org.codehaus.yom.Document;
 import org.codehaus.yom.stax.StaxBuilder;
 
 /**
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  */
 public class TransportTest
         extends AbstractXFireAegisTest
 {
     private Service echo;
 
     private Transport transport1;
     private Transport transport2;
 
     String username = "xfireTestServer";
     String password = "password1";
     String server = "bloodyxml.com";
     String id = username + "@" + server;
 
     public void setUp()
             throws Exception
     {
         super.setUp();
 
         echo = getServiceFactory().create(Echo.class);
 
         getServiceRegistry().register(echo);
 
         transport2 = SoapTransport.createSoapTransport(new XMPPTransport(getXFire(), server, username, password));
         transport1 = SoapTransport.createSoapTransport(new XMPPTransport(getXFire(), server, "xfireTestClient", "password2"));
         
         getXFire().getTransportManager().register(transport2);
         // XMPPConnection.DEBUG_ENABLED = true;
     }
 
     protected void tearDown()
         throws Exception
     {
         transport1.dispose();
         transport2.dispose();
         
         super.tearDown();
     }
 
     public void testTransport()
             throws Exception
     {
         String peer1 = "Peer1";
         String peer2 = "Peer2";
         
         Channel channel1 = transport1.createChannel(peer1);
 
         Channel channel2 = transport2.createChannel(peer2);
         channel2.setEndpoint(new YOMEndpoint());
         
         // Document to send
         StaxBuilder builder = new StaxBuilder();
         Document doc = builder.build(getResourceAsStream("/org/codehaus/xfire/xmpp/echo.xml"));
 
         MessageContext context = new MessageContext();
 
         OutMessage msg = new OutMessage(id + "/" + peer2);
         msg.setSerializer(new SoapSerializer(new YOMSerializer()));
         msg.setBody(doc);
 
         channel1.send(context, msg);
         channel1.send(context, msg);
         Thread.sleep(1000);
     }
 
     public void testService()
             throws Exception
     {
         String peer1 = "Peer1";
         
         Channel channel1 = transport1.createChannel(peer1);
 
         YOMEndpoint peer = new YOMEndpoint();
         channel1.setEndpoint(peer);
         
         Channel channel2 = transport2.createChannel("Echo");
 
         // Document to send
         StaxBuilder builder = new StaxBuilder();
         Document doc = builder.build(getResourceAsStream("/org/codehaus/xfire/xmpp/echo.xml"));
         
         MessageContext context = new MessageContext();
 
         OutMessage msg = new OutMessage(id + "/Echo");
         msg.setSerializer(new SoapSerializer(new YOMSerializer()));
         msg.setBody(doc);
 
         channel1.send(context, msg);
         channel1.send(context, msg);
        Thread.sleep(1000);
        if (peer.getCount() < 2) Thread.sleep(1000);
        if (peer.getCount() < 2) Thread.sleep(1000);
        if (peer.getCount() < 2) Thread.sleep(1000);

         assertEquals(2, peer.getCount());
     }
 
     public void testWSDL()
             throws Exception
     {
         Document wsdl = getWSDLDocument("Echo");
 
         addNamespace("wsdl", WSDLWriter.WSDL11_NS);
         addNamespace("swsdl", WSDLWriter.WSDL11_SOAP_NS);
 
         assertValid("//wsdl:binding[@name='EchoXMPPBinding'][@type='tns:EchoPortType']", wsdl);
         assertValid("//wsdl:binding[@name='EchoXMPPBinding']/swsdl:binding[@transport='" +
                     XMPPTransport.XMPP_TRANSPORT_NS + "']", wsdl);
 
         assertValid("//wsdl:service/wsdl:port[@binding='tns:EchoXMPPBinding'][@name='EchoXMPPPort']", wsdl);
         assertValid("//wsdl:service/wsdl:port[@binding='tns:EchoXMPPBinding'][@name='EchoXMPPPort']" +
                     "/swsdl:address[@location='xfireTestServer@bloodyxml.com/Echo']", wsdl);
     }
 
     public class YOMEndpoint
         implements ChannelEndpoint
     {
         private int count = 0;
         
         public void onReceive(MessageContext context, InMessage msg)
         {
             count++;
             
             StaxBuilder builder = new StaxBuilder();
             try
             {
                 Document doc = builder.build(msg.getXMLStreamReader());
                 System.out.println("Received message.");
             }
             catch (XMLStreamException e)
             {
                 e.printStackTrace();
             }
         }
         
         public int getCount()
         {
             return count;
         }
     }
 }
