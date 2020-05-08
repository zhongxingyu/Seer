 package org.jivesoftware.smack;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.Socket;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.security.cert.CertificateException;
 import java.util.Vector;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import javax.net.ssl.SSLSocket;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
 import org.jivesoftware.smack.packet.XMPPError;
 import org.jivesoftware.smack.util.DNSUtil;
 import org.jivesoftware.smack.util.ObservableReader;
 import org.jivesoftware.smack.util.ObservableWriter;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import com.kenai.jbosh.AbstractBody;
 import com.kenai.jbosh.BOSHClient;
 import com.kenai.jbosh.BOSHClientConfig;
 import com.kenai.jbosh.BOSHClientConnEvent;
 import com.kenai.jbosh.BOSHClientConnListener;
 import com.kenai.jbosh.BOSHClientRequestListener;
 import com.kenai.jbosh.BOSHClientResponseListener;
 import com.kenai.jbosh.BOSHException;
 import com.kenai.jbosh.BOSHMessageEvent;
 import com.kenai.jbosh.BodyQName;
 import com.kenai.jbosh.ComposableBody;
 
 public class XMPPStreamBOSH extends XMPPStream
 {
     private URI uri = null;
 
     // bosh_client must only be accessed while synchronized, and only when
     // connectionClosed is false.
     private BOSHClient bosh_client;
     private boolean connectionClosed = false;
 
     /** Return true if the connection is secure (encrypted with a verified certificate). */
     public boolean isSecureConnection() { return usingSecureConnection; }
     private boolean usingSecureConnection = false;
 
     public void writePacket(String packet) throws IOException {
         if(connectionClosed)
             throw new IOException("Wrote a packet while the connection was closed");
 
         try {
             // Note that this will block if the packet can't be sent immediately.
             bosh_client.send(createBoshPacket(packet).build());
         } catch(BOSHException e) {
             IOException io = new IOException("Error writing BOSH packet");
             io.initCause(e); // IOException lacks a (message, cause) constructor
             throw io;
         }
     }
 
     // Although compression may or may not be in use by the HTTP stream, that can
     // vary from connection to connection, and we won't have any meaningful response
     // if the connection is inactive.  Just return false.
     public boolean isUsingCompression() { return false; }
 
     private ObservableReader.ReadEvent readEvent;
     private ObservableWriter.WriteEvent writeEvent;
 
     String authID;
     public String getConnectionID() { return authID; }
 
     public void setReadWriteEvents(ObservableReader.ReadEvent readEvent, ObservableWriter.WriteEvent writeEvent) {
         this.writeEvent = writeEvent;
         this.readEvent = readEvent;
     }
 
     ConnectionConfiguration config;
 
     public XMPPStreamBOSH(ConnectionConfiguration config)
     {
         this.uri = config.getBoshURI();
         this.config = config;
         connectionClosed = true;
     }
 
     private int discoveryIndex;
     public void setDiscoveryIndex(int index) {
         discoveryIndex = index;
     }
 
     public void initializeConnection() throws XMPPException
     {
         if(bosh_client != null)
             throw new RuntimeException("The connection has already been initialized");
 
         // If BOSH is disabled, then stop attempting this transport.
         if(this.uri == null)
             throw new XMPPException("BOSH is disabled", XMPPError.Condition.remote_server_not_found);
 
         if(this.uri == ConnectionConfiguration.AUTO_DETECT_BOSH) {
             // This will return the same results each time, because the weight
             // shuffling is cached.
             // XXX: Figure out how BOSH discovery is supposed to be secured. 
             DNSUtil.XMPPConnectLookup lookup = new DNSUtil.XMPPConnectLookup(config.getServiceName(), "_xmpp-client-xbosh");
             Vector<String> addresses = lookup.run();
             if(addresses.isEmpty())
                 throw new XMPPException("No BOSH servers discovered", XMPPError.Condition.remote_server_not_found);
 
             if(discoveryIndex >= addresses.size())
                 throw new XMPPException("No more BOSH servers to attempt (tried all " + addresses.size() + ")",
                         XMPPError.Condition.remote_server_not_found);
 
             String url = addresses.get(discoveryIndex);
             try {
                 uri = new URI(url);
             } catch (URISyntaxException e) {
                 throw new XMPPException("Discovered BOSH server has bad URL: " + url);
             }
 
             // Only allow HTTP and HTTPS.
             if(!uri.getScheme().equalsIgnoreCase("http") && !uri.getScheme().equalsIgnoreCase("https"))
                 throw new XMPPException("Discovered BOSH server has invalid scheme: " + url);
 
             // We'll catch this down below, but we throw a different message here.
             if(config.getSecurityMode() == SecurityMode.required && !uri.getScheme().equalsIgnoreCase("https"))
                 throw new XMPPException("Discovered BOSH server is not HTTPS, but security required by connection configuration.",
                         XMPPError.Condition.forbidden);
         }
 
         BOSHClientConfig.Builder cfgBuilder = BOSHClientConfig.Builder.create(uri, config.getServiceName());
         cfgBuilder.setSocketFactory(config.getProxyInfo().getSocketFactory());
 
         final XMPPSSLSocketFactory xmppSocketFactory = new XMPPSSLSocketFactory(config, config.getServiceName());
         cfgBuilder.setSSLConnector(new com.kenai.jbosh.SSLConnector() {
             public SSLSocket attachSSLConnection(Socket socket, String host, int port) throws IOException {
                 return xmppSocketFactory.attachSSLConnection(socket, host, port);
             }
         });
 
         if(uri.getScheme().equals("http")) {
             if(config.getSecurityMode() == SecurityMode.required)
                 throw new XMPPException("BOSH server is not HTTPS, but security required by connection configuration.",
                         XMPPError.Condition.forbidden);
             usingSecureConnection = false;
         }
 
         bosh_client = BOSHClient.create(cfgBuilder.build());
         bosh_client.addBOSHClientResponseListener(new ResponseListener());
         bosh_client.addBOSHClientConnListener(new ConnectionListener());
 
         // Pass on requests and responses to observers.
         bosh_client.addBOSHClientResponseListener(new BOSHClientResponseListener() {
             public void responseReceived(BOSHMessageEvent event) {
                 if (event.getBody() == null)
                     return;
                 readEvent.notifyListeners(event.getBody().toXML());
             }
         });
 
         bosh_client.addBOSHClientRequestListener(new BOSHClientRequestListener() {
             public void requestSent(BOSHMessageEvent event) {
                 if (event.getBody() == null)
                     return;
                 writeEvent.notifyListeners(event.getBody().toXML());
             }
         });
 
         try {
             // Send the session creation request and receive the response.
             startupConnection();
         } catch(XMPPException e) {
             /* If an exception occurs while we're starting the connection, close it.
              * The connection should only be running on a successful return. */
             // android.util.Log.w("SMACK", "XMPPStreamBOSH: initializeConnection: shutting down client due to error");
             disconnect();
             throw e;
         }
 
         // We've received the first response, so the first connection has been established.
         // We can now tell if the connection was made insecurely.  We don't need to check
         // SecurityMode here: if it's set to required, an exception was thrown when we first
         // connected the socket.  Note that usingSecureConnection may already be false for
         // other reasons.
         if(xmppSocketFactory != null) {
             CertificateException detail = xmppSocketFactory.getSeenInsecureConnection();
             if(detail != null)
                 usingSecureConnection = false;
         }
 
         connectionClosed = false;
     }
 
     private void startupConnection() throws XMPPException {
         // android.util.Log.w("SMACK", "XMPPStreamBOSH: initializeConnectionInternal: send");
         try {
             bosh_client.send(ComposableBody.builder()
                         .setNamespaceDefinition("xmpp", "urn:xmpp:xbosh")
                         .setAttribute(BodyQName.createWithPrefix("urn:xmpp:xbosh", "version", "xmpp"), "1.0")
                         .build());
         }
         catch(BOSHException e)
         {
             throw new XMPPException("Error connecting to BOSH server", e);
         }
 
         // Read the response to the first packet.
         AbstractBody packet = readAbstractPacket();
         if(packet == null)
             throw new XMPPException("Connection to BOSH server closed prematurely");
 
         // Check that secure='1' was sent in the response, indicating that the BOSH->XMPP connection
         // is also secure.
         String secure = packet.getAttribute(BodyQName.create("http://jabber.org/protocol/httpbind", "secure"));
         if(secure == null || (!secure.equalsIgnoreCase("true") && !secure.equals("1"))) {
             usingSecureConnection = false;
 
             if(config.getSecurityMode() == ConnectionConfiguration.SecurityMode.required) {
                 throw new XMPPException("The BOSH->XMPP session is not secure, " +
                         "but the configuration requires a secure connection.",
                         XMPPError.Condition.forbidden);
             }
         }
 
         // Over BOSH, XMPP's <body id> is transmitted as authid.
         authID = packet.getAttribute(BodyQName.create("http://jabber.org/protocol/httpbind", "authid"));
 
         // Update the service name from <body from>, if provided.
         String from = packet.getAttribute(BodyQName.create("http://jabber.org/protocol/httpbind", "from"));
         if(from != null)
             config.setServiceName(from);
 
         /* We still need to return this from readPacket, so queue it. */
         read_queue.add(new QueuedResponse(packet));
         // android.util.Log.w("SMACK", "XMPPStreamBOSH: initializeConnection: done");
     }
 
     public void gracefulDisconnect(String packet)
     {
         // android.util.Log.w("SMACK", "XMPPStreamBOSH: close()");
 
         try {
             // If any stanzas are waiting to be sent, send them in the disconnect message.
             bosh_client.disconnect(createBoshPacket(packet).build());
         }
         catch(BOSHException e)
         {
             // If the disconnect() fails for some reason, forcibly close the connection.
             // android.util.Log.w("SMACK", "XMPPStreamBOSH: sending disconnect(): error");
             e.printStackTrace();
             disconnect();
             return;
         }
 
        // We've sent the terminate packet.  Wait for the stream to close; disconnect()
        // will set connectionClosed.
         // android.util.Log.w("SMACK", "XMPPStreamBOSH: waiting for disconnect");
         long waitUntil = System.currentTimeMillis() + SmackConfiguration.getPacketReplyTimeout();
         synchronized(this) {
            while(!read_queue.isEmpty()) {
                 long ms = waitUntil - System.currentTimeMillis();
                 if(ms <= 0)
                     break;
                 try {
                     wait(ms);
                 } catch (InterruptedException e) {
                     Thread.currentThread().interrupt();
                     break;
                 }
             }
         }
 
         // android.util.Log.w("SMACK", "XMPPStreamBOSH: done waiting, " + (connectionClosed? "connection is closed":"connection is not closed"));
 
         // In case we timed out above and the stream isn't closed yet, forcibly close it.
         disconnect();
     }
 
     public void disconnect() {
         synchronized(this) {
             if(connectionClosed)
                 return;
 
             // android.util.Log.w("SMACK", "XMPPStreamBOSH: disconnect (queueing QueuedEnd)");
 
             boolean added = read_queue.offer(new QueuedEnd());
 
             // The queue doesn't have any limit set, so this should always succeed.
             if(!added)
                 throw new AssertionError("Queueing packet failed");
 
             bosh_client.close();
 
             connectionClosed = true;
             notify();
         }
     }
 
     /**
      * A stream reset is required, usually due to SASL authentication completing.
      * Instruct the BOSH server to perform the restart.
      */
     public void streamReset() throws XMPPException {
         // We're supposed to check for <body restartlogic='true'>, which tells us that the server supports
         // stream restarts.  If it's not present, we can't restart the stream, which means we can't
         // authenticate via SASL.  However, the spec also notes correctly that older BOSH implementations
         // don't advertise restartlogic (despite supporting it).  If we refuse to authenticate
         // when restartlogic isn't present, then we'll break logins on older servers.  This really
         // isn't very useful: a BOSH server that we effectively can't authenticate through is useless,
         // so for now just pretend they all support stream restarts.
 
         try {
             /* Make sure any written data is flushed and sent to the server.  According
              * to XEP-0206, any stanzas we put in a <body restart='true'> packet will be
              * ignored. */
             if(connectionClosed)
                 throw new XMPPException("Connection has closed");
 
             bosh_client.send(ComposableBody.builder()
                     .setNamespaceDefinition("xmpp", "urn:xmpp:xbosh")
                     .setAttribute(BodyQName.createWithPrefix("urn:xmpp:xbosh", "version", "xmpp"), "1.0")
                     .setAttribute(BodyQName.createWithPrefix("urn:xmpp:xbosh", "restart", "xmpp"), "true")
                     .build());
         }
         catch(BOSHException e) {
             throw new XMPPException("BOSH error while sending xmpp:restart", e);
         }
     }
 
     /* A queue of packets received from the server. */
     private static abstract interface QueuedMessage { };
     private static class QueuedResponse implements QueuedMessage {
         public AbstractBody body;
         QueuedResponse(AbstractBody body) { this.body = body; }
     };
     private static class QueuedError implements QueuedMessage {
         QueuedError(XMPPException error) { this.error = error; }
         XMPPException error;
     };
     private static class QueuedEnd implements QueuedMessage { };
 
     LinkedBlockingQueue<QueuedMessage> read_queue = new LinkedBlockingQueue<QueuedMessage>();
 
     public AbstractBody readAbstractPacket() throws XMPPException {
         /* There should never be XML packets in this.nodes waiting to be delivered
          * if we're requesting a new <body/>. */
         assert(nodes == null || nodesNextIndex == nodes.getLength());
 
         QueuedMessage msg = null;
         try {
             msg = read_queue.take();
         }
         catch(InterruptedException e)
         {
             throw new XMPPException("Interrupted while waiting for a packet", e);
         }
 
         // Null queued as a message indicates that the stream has terminated
         // normally.
         if(msg instanceof QueuedEnd)
             return null;
 
         if(msg instanceof QueuedError)
             throw ((QueuedError) msg).error;
         else
             return ((QueuedResponse) msg).body;
     }
 
     NodeList nodes;
     int nodesNextIndex = 0;
     public Element readPacket() throws InterruptedException, XMPPException {
         while(true) {
             try {
                 if(nodesNextIndex == -1)
                     return null;
 
                 // Return the next queued packet, if any.
                 while(nodes != null && nodesNextIndex < nodes.getLength()) {
                     Node node = nodes.item(nodesNextIndex++);
                     if(!(node instanceof Element))
                         continue;
 
                     return (Element) node;
                 }
 
                 // We don't have any nodes queued to return, so block and wait for the
                 // next one.
                 AbstractBody body = readAbstractPacket();
 
                 // If body is null, the stream is closed and we've flushed all packets.
                 if(body == null) {
                     // Set nodesNextIndex as a sentinel, so all future calls to readPacket will
                     // continue to return null.
                     nodesNextIndex = -1;
                     return null;
                 }
 
                 String xml = body.toXML();
 
                 DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
                 dbfac.setNamespaceAware(true);
                 DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
                 Document doc = docBuilder.parse(new InputSource(new StringReader(xml)));
 
                 // Retrieve <body>.
                 Element bodyNode = (Element) doc.getFirstChild();
 
                 // The children of <body> are the XMPP payloads, so queue them.
                 nodes = bodyNode.getChildNodes();
                 nodesNextIndex = 0;
             }
             catch(IOException e) {
                 throw new XMPPException("Error reading packet", e);
             }
             catch(SAXException e) {
                 throw new XMPPException("Error reading packet", e);
             }
             catch(ParserConfigurationException e) {
                 throw new XMPPException("Error reading packet", e);
             }
         }
     }
 
     private static ComposableBody.Builder createBoshPacket(String packet) {
         ComposableBody.Builder builder = ComposableBody.builder()
             .setNamespaceDefinition("xmpp", "urn:xmpp:xbosh")
             .setAttribute(BodyQName.createWithPrefix("urn:xmpp:xbosh", "version", "xmpp"), "1.0");
 
         if(packet != null) {
             builder.setPayloadXML(packet);
         }
 
         return builder;
     }
 
     private class ResponseListener implements BOSHClientResponseListener
     {
         public synchronized void responseReceived(BOSHMessageEvent event)
         {
             try {
                 AbstractBody packet = event.getBody();
                 read_queue.put(new QueuedResponse(packet));
             }
             catch(InterruptedException e)
             {
                 throw new RuntimeException("Interrupted while queuing received packet", e);
             }
         }
     }
 
     private class ConnectionListener implements BOSHClientConnListener
     {
         public synchronized void connectionEvent(BOSHClientConnEvent connEvent)
         {
             try {
                 Throwable be = connEvent.getCause();
 
                 // This is a hack: when the session is forcibly closed by calling
                 // bosh_client.close(), jbosh triggers an error, but that shouldn't
                 // actually be treated as one.  jbosh should use a separate exception
                 // class for this, so we can distinguish this case sanely, or just
                 // send this case as a disconnection instead of disconnection error.
                 boolean ignoredError = be != null && be.getMessage().equals("Session explicitly closed by caller");
 
                 if(connEvent.isError() && !ignoredError) {
                     // Queue the error for delivery by readPacket.
                     read_queue.put(new QueuedError(new XMPPException(be)));
                 }
 
                 if(!connEvent.isConnected() && !connectionClosed) {
                     disconnect();
                 }
             } catch(InterruptedException ie) {
                 throw new RuntimeException("Interrupted while queuing error packet", ie);
             }
         }
     }
 };
