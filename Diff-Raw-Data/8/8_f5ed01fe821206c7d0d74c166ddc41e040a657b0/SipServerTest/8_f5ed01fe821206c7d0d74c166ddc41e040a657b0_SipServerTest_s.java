 package org.lastbamboo.common.sip.server;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.net.Socket;
 
 import junit.framework.TestCase;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.lastbamboo.common.sip.proxy.SipProxy;
 import org.lastbamboo.common.sip.proxy.SipProxyImpl;
 import org.lastbamboo.common.sip.proxy.SipRegistrar;
 import org.lastbamboo.common.sip.proxy.SipRegistrarImpl;
 import org.lastbamboo.common.sip.proxy.SipRequestAndResponseForwarder;
 import org.lastbamboo.common.sip.proxy.stateless.StatelessSipProxy;
 import org.lastbamboo.common.sip.stack.message.SipMessageFactory;
 import org.lastbamboo.common.sip.stack.message.SipMessageFactoryImpl;
 import org.lastbamboo.common.sip.stack.message.header.SipHeaderFactory;
 import org.lastbamboo.common.sip.stack.message.header.SipHeaderFactoryImpl;
import org.lastbamboo.common.sip.stack.transaction.SipTransactionFactory;
import org.lastbamboo.common.sip.stack.transaction.SipTransactionFactoryImpl;
import org.lastbamboo.common.sip.stack.transaction.SipTransactionTracker;
import org.lastbamboo.common.sip.stack.transaction.SipTransactionTrackerImpl;
 import org.lastbamboo.common.sip.stack.transport.SipTcpTransportLayer;
 import org.lastbamboo.common.sip.stack.transport.SipTcpTransportLayerImpl;
 import org.lastbamboo.common.sip.stack.util.UriUtils;
 import org.lastbamboo.common.sip.stack.util.UriUtilsImpl;
 import org.lastbamboo.common.util.NetworkUtils;
 
 public class SipServerTest extends TestCase
     {
 
     private static final Log LOG = LogFactory.getLog(SipServerTest.class);
     
     private static final String REGISTER = 
         "REGISTER sip:lastbamboo.org SIP/2.0\r\n"+
         "To: Anonymous <sip:111111@lastbamboo.org>\r\n"+
         "Via: SIP/2.0/TCP 192.168.0.111;branch=z9hG4bK213e290\r\n"+
         "CSeq: 2 REGISTER\r\n"+
         "Content-Length: 0\r\n"+
         "From: Anonymous <sip:111111@lastbamboo.org>;tag=9a6c204b-a\r\n"+
         "Contact: <sip:111111@192.168.0.111>;+sip.instance=\"<urn:uuid:1f3d37a1-5821-43db-98d1-850df172a675>\"\r\n" +
         "\r\n";
     
     
     private static final String INVITE = 
         "INVITE sip:1@lastbamboo.org SIP/2.0\r\n" +
         "To: Anonymous <sip:1@lastbamboo.org>\r\n" +
         "Via: SIP/2.0/TCP 10.250.74.236;branch=z9hG4bK0363854\r\n" +
         "Content-Length: 160\r\n" +
         "CSeq: 9 INVITE\r\n" +
         "Contact: <sip:3@127.0.0.1>;+sip.instance=\"<urn:uuid:2d76276d-9b37-44b8-9658-7dadd4e5cbad>\"\r\n" +
         "From: Bob <sip:3@lastbamboo.org>;tag=63366469-4\r\n" +
         "Call-ID: 3a3c60b-\r\n" +
         "Max-Forwards: 70\r\n" +
         "Expires: 7200\r\n" +
         "\r\n" +
         "v=0\r\n" +
         "o=alice 53655765 2353687637 IN IP4 pc33.atlanta.com\r\n" +
         "s=Session SDP\r\n" +
         "t=0 0\r\n" +
         "c=IN IP4 pc33.atlanta.com\r\n" +
         "m=audio 3456 RTP/AVP 0 1 3 99\r\n" +
         "a=rtpmap:0 PCMU/8000\r\n";
     
     public void testServer() throws Exception
         {
         LOG.debug("Starting server..");
         startServerThread();
         LOG.debug("Started server...");
         Thread.sleep(300);
         final Socket sock = new Socket(NetworkUtils.getLocalHost(), 5060);
         LOG.debug("Connected...");
         final OutputStream os = sock.getOutputStream();
         
         final Writer writer = new OutputStreamWriter(os);
         writer.write(REGISTER);
         writer.flush();
         
         final InputStream is = sock.getInputStream();
         final BufferedReader reader = 
             new BufferedReader(new InputStreamReader(is));
         
         String line = reader.readLine();
         assertEquals("SIP/2.0 200 OK", line);
         
         while (!StringUtils.isBlank(line))
             {
             line = reader.readLine();
             }
         }
     
     private void startServerThread()
         {
         final Runnable runner = new Runnable()
             {
 
             public void run()
                 {
                 startServer();
                 }
             
             };
        
         final Thread server = new Thread(runner, "test server thread");
         server.setDaemon(true);
         server.start();
         }
 
     private void startServer()
         {
         final SipHeaderFactory headerFactory = new SipHeaderFactoryImpl();
         final SipMessageFactory messageFactory = 
             new SipMessageFactoryImpl(headerFactory);
         final SipTransactionTracker transactionTracker = 
             new SipTransactionTrackerImpl();
         final SipTransactionFactory transactionFactory = 
             new SipTransactionFactoryImpl(transactionTracker, messageFactory,47382);
         final SipTcpTransportLayer tcpTransport = 
             new SipTcpTransportLayerImpl(transactionFactory, headerFactory, messageFactory);
         final UriUtils uriUtils = new UriUtilsImpl();
         final SipRegistrar registrar = 
             new SipRegistrarImpl(messageFactory, tcpTransport);
         final SipRequestAndResponseForwarder forwarder = 
             new StatelessSipProxy(tcpTransport, registrar, null, null, uriUtils, messageFactory); 
         
         final SipProxy proxy = new SipProxyImpl(
             forwarder, registrar, headerFactory, messageFactory, tcpTransport);
         //final SipRegistrar registrar = 
           //  (SipRegistrar) this.m_appContext.getBean("sipRegistrar");
 
         proxy.start();
         LOG.debug("Started proxy...");
         }
     }
