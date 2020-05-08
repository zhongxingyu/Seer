 /**
  * $RCSfile$
  * $Revision: 11616 $
  * $Date: 2010-02-09 07:40:11 -0500 (Tue, 09 Feb 2010) $
  *
  * Copyright 2003-2007 Jive Software.
  *
  * All rights reserved. Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.jivesoftware.smack;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.Writer;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.security.cert.CertificateException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Vector;
 
 import javax.net.ssl.SSLSocket;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.jivesoftware.smack.packet.XMPPError;
 import org.jivesoftware.smack.util.DNSUtil;
 import org.jivesoftware.smack.util.ObservableReader;
 import org.jivesoftware.smack.util.ObservableWriter;
 import org.jivesoftware.smack.util.PacketParserUtils;
 import org.jivesoftware.smack.util.WriterListener;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 /**
  * XMPP TCP transport, implementing TLS and compression. 
  */
 public class XMPPStreamTCP extends XMPPStream
 {
     private Socket socket = null; 
     private Reader reader = null;
     private Writer writer = null;
     
     private ObservableReader.ReadEvent readEvent;
     private ObservableWriter.WriteEvent writeEvent;
 
     private ObservableWriter.WriteEvent keepaliveMonitorWriteEvent;
 
     /** True if the connection is encrypted, whether or not the certificate is verified. */
     private boolean usingTLS = false;
 
     /** True if the connection is secure (encrypted with a verified certificate). */
     private boolean usingSecureConnection = false;
 
     /* True if XMPP compression is enabled.  If TLS compression is enabled, this is false. */
     private boolean usingXMPPCompression = false;
 
     /* True if TLS compression is enabled. */
     private boolean usingTLSCompression = false;
 
     private ConnectionConfiguration config;
     private String originalServiceName;
 
     private XmlPullParser parser;
     XMPPSSLSocketFactory sslSocketFactory;
 
     /** If true, the most recent <features/> advertised <starttls/>. */
     private boolean featureStartTLSReceived = false;
     
     /** The compression methods advertised in the most recent <features/>. */
     private List<String> featureCompressionMethods;
     
     /**
      * We can't tell that initialization is finished until we read a <features/> packet
      * to know whether we have any more initialization work to do, but the final features
      * packet that we don't need must be returned to the caller.  If this is non-null, it's
      * a buffered packet waiting to be returned by readPacket().
      */
     private Element bufferedPacket = null;
 
     public Writer getWriter() { return writer; }
     public boolean isSecureConnection() { return usingSecureConnection; }
     public boolean isUsingCompression() { return usingXMPPCompression || usingTLSCompression; }
 
     String connectionID;
     public String getConnectionID() { return connectionID; }
     
     public XMPPStreamTCP(ConnectionConfiguration config)
     {
         this.config = config;
         
         /* We update config.serviceName when we see the service name from the server,
          * but we need to retain the original value for TLS certificate checking. */
         originalServiceName = config.getServiceName();
         keepaliveMonitorWriteEvent = new ObservableWriter.WriteEvent();
     }
 
     private int discoveryIndex;
     public void setDiscoveryIndex(int index) {
         discoveryIndex = index;
     }
 
     public void setReadWriteEvents(ObservableReader.ReadEvent readEvent, ObservableWriter.WriteEvent writeEvent) {
         this.writeEvent = writeEvent;
         this.readEvent = readEvent;
     }
 
     /**
      * Begin the initial connection to the server.  Returns when the connection
      * is established.
      */
     public void initializeConnection() throws XMPPException {
         if(socket != null)
             throw new RuntimeException("The connection has already been initialized");
 
         String host = config.getHost();
         int port = config.getPort();
 
         // If no host was specified, look up the XMPP service name.
         // XXX: This should be cancellable.
         if(host == null) {
             // This will return the same results each time, because the weight
             // shuffling is cached.
             DNSUtil.XMPPDomainLookup lookup = new DNSUtil.XMPPDomainLookup(config.getServiceName(), true);
             Vector<DNSUtil.HostAddress> addresses = lookup.run();
             if(discoveryIndex >= addresses.size())
                 throw new XMPPException("No more servers to attempt (tried all " + addresses.size() + ")",
                         XMPPError.Condition.remote_server_not_found);
             host = addresses.get(discoveryIndex).getHost();
             port = addresses.get(discoveryIndex).getPort();
         }
 
         try {
             socket = config.getSocketFactory().createSocket(host, port);
             
             initReaderAndWriter();
         } catch (UnknownHostException e) {
             throw new XMPPException("Could not connect to " + host + ":" + port, e);
         } catch(IOException e) {
             throw new XMPPException("Could not connect to " + host + ":" + port, e);
         }
 
         /* Handle transport-level negotiation.  Read the <features/> packet to see if
          * we should set up TLS or compression, and repeat until we have nothing more
          * to negotiate. */
         while(true) {
             Element packet = readPacket();
             
             // If packet is null, the stream terminated normally.  The only way the
             // stream can terminate normally here is if disconnect() was called asynchronously;
             // report that as an error, akin to InterruptedException.
             if(packet == null)
                 throw new XMPPException("Disconnected by user");
 
             try {
                 if(processInitializationPacket(packet)) {
                     /* We're still initializing the connection, so don't return any packets. */
                     continue;
                 }
             } catch(IOException e) {
                 throw new XMPPException("I/O error establishing connection to server", e);
             }
 
             /* Stream initialization is complete.  The packet we just read wasn't consumed
              * by processInitializationPacket, so save it for the first real call to readPcket. */
             bufferedPacket = packet;
 
             // After a successful connect, fill in config with the host we actually connected
             // to.  This allows the client to detect what it's actually talking to, and is necessary
             // for SASL authentication.  Don't do this until we're actually connected, so later
             // autodiscovery attempts aren't modified.
             config.setHost(host);
             config.setPort(port);
 
             /* Start keepalives after TLS has been set up. */ 
             startKeepAliveProcess();
             return;
         }
     }
     
     /**
      * Return true if zlib (deflate) compression is supported.
      */
     private static boolean getZlibSupported() {
         try {
             Class.forName("com.jcraft.jzlib.ZOutputStream");
         }
         catch (ClassNotFoundException e) {
             // throw new IllegalStateException("Cannot use compression. Add smackx.jar to the classpath");
             return false;
         }
         return true;
     }
 
 
     /**
      * Attempt to negotiate a feature, based
      * 
      * @return true if a feature is being negotiated.
      * @throws XMPPException
      * @throws IOException
      */
     private boolean negotiateFeature() throws XMPPException, IOException {
         // If TLS is required but the server doesn't offer it, disconnect
         // from the server and throw an error. First check if we've already negotiated TLS
         // and are secure, however (features get parsed a second time after TLS is established).
         if (!isSecureConnection() && !featureStartTLSReceived &&
                 config.getSecurityMode() == ConnectionConfiguration.SecurityMode.required)
         {
             throw new XMPPException("Server does not support security (TLS), " +
                     "but security required by connection configuration.",
                     XMPPError.Condition.forbidden);
         }
 
         if(!isSecureConnection() && featureStartTLSReceived &&
                 config.getSecurityMode() != ConnectionConfiguration.SecurityMode.disabled) {
             
             // If we havn't yet set up sslSocketFactory, and encryption is available,
             // set it up. 
             if(sslSocketFactory == null)
                 sslSocketFactory = new XMPPSSLSocketFactory(config, originalServiceName);
 
             if(sslSocketFactory.isAvailable()) {
                 // The server is offering TLS, so enable it.
                 startTLSReceived();
 
                 /* Transport initialization is continuing; we should now receive <proceed/>. */
                 return true;
             }
             
             // Encryption was offered, but we weren't able to initialize it.  If the user required
             // TLS, fail.  We could handle this failure earlier, but it's a rare case.
             if(!sslSocketFactory.isAvailable() && config.getSecurityMode() == ConnectionConfiguration.SecurityMode.required) {
                 throw new XMPPException("System does not support encryption, " +
                         "but security is required by connection configuration.", XMPPError.Condition.forbidden);
             }
         }
 
         // Compression must be negotiated after encryption. 
         if(!usingXMPPCompression && config.isCompressionEnabled()) {
             // If we we share a supported compression method with the server, enable compression.
             if(getZlibSupported() && featureCompressionMethods.contains("zlib")) {
                 // Only attempt to negotiate a protocol once per <features/>.  If it fails
                 // we'll be notified by <failure/>, which will retry feature negotiation; if
                 // we don't do this, it'll just try to negotiate the same compressor over and
                 // over.
                 featureCompressionMethods.remove("zlib");
                 
                 enableCompressionMethod("zlib");
 
                 /* Transport initialization is continuing; we should now receive <compressed/>. */
                 return true;
             }
         }
         
         // We're not interested in the transport features of this connection, so
         // the transport negotiation is complete.  The <features/> we just received
         // must be returned to the application.
         return false;
     }
 
     /**
      * Process a packet during initialization.  Return true if the packet was
      * processed and we're still initializing.  Return false if initialization
      * is no longer taking place, and the packet should be returned to the user
      * via readPacket.
      * 
      * @param node The packet to process.
      * @return whether initialization continues.
      * @throws XMPPException
      * @throws IOException
      */
     private boolean processInitializationPacket(Element node)
         throws XMPPException, IOException
     {
         if (node.getNodeName().equals("features")) {
             featureStartTLSReceived = false;
             featureCompressionMethods = new ArrayList<String>();
 
             for (Node child: PacketParserUtils.getChildNodes(node)) {
                 if (!usingTLS && child.getNodeName().equals("starttls")) {
                     featureStartTLSReceived = true;
 
                     for (Node startTlsChild: PacketParserUtils.getChildNodes(child)) {
                         if (startTlsChild.getNodeName().equals("required")) {
                             if (config.getSecurityMode() == ConnectionConfiguration.SecurityMode.disabled) {
                                 throw new XMPPException(
                                     "TLS required by server but not allowed by connection configuration", XMPPError.Condition.forbidden);
                             }
                         }
                     }
                 }
                 else if (child.getNodeName().equals("compression")) {
                     for (Node compressionChild: PacketParserUtils.getChildNodes(child)) {
                         if (!compressionChild.getNodeName().equals("method"))
                             continue;
                         featureCompressionMethods.add(PacketParserUtils.getTextContent(compressionChild));
                     }
                 }
             }
 
             // We've received a new feature list; see if we can negotiate any of them. 
             return negotiateFeature();
         }
 
         else if(node.getNodeName().equals("proceed") && node.getNamespaceURI().equals("urn:ietf:params:xml:ns:xmpp-tls")) {
             // The server has acknowledged our <starttls/> request.  Enable TLS.
             try {
                 proceedTLSReceived();
             } catch(Exception e) {
                 e.printStackTrace();
                 throw new XMPPException("Error initializing TLS", e);
             }
 
             return true;
         }
         else if(node.getNodeName().equals("failure")) { 
             if(node.getNamespaceURI().equals("urn:ietf:params:xml:ns:xmpp-tls")) {
                 // The server offered STARTTLS, but responded with a failure when we
                 // tried to use it.  The stream will be closed by the server.  This is an
                 // abnormal condition.
                 throw new XMPPException("Server failed while initializing TLS");
             } else if(node.getNamespaceURI().equals("http://jabber.org/protocol/compress")) {
                 // The server offered compression, but failed when we tried to use it.
                 // This isn't a fatal error, so attempt to negotiate the next feature.
                 // XXX: test this (jabberd2 is always returning success, even on a bogus method)
                 return negotiateFeature();
             }
         }
         else if (node.getNodeName().equals("compressed") && node.getNamespaceURI().equals("http://jabber.org/protocol/compress")) {
             // Server confirmed that it's possible to use stream compression. Start
             // stream compression
             usingXMPPCompression = true;
             
             // Reinitialize the reader and writer with compression enabled.
             initReaderAndWriter();
             
             /* Don't return this packet.  The stream is restarting, and the new stream:features
              * received after the stream restart will be the first packet returned. */
             return true;
         }
 
         /* We received an initialization packet we don't know about.  Is it valid for
          * the server to send anything at all before <features/>? */
         return true;
     }
 
     /**
      * TLS is supported by the server.  If encryption is enabled, start TLS.  
      */
     private void startTLSReceived() throws IOException {
         writer.write("<starttls xmlns=\"urn:ietf:params:xml:ns:xmpp-tls\"/>");
         writer.flush();
     }
     
     /**
      * Starts using stream compression that will compress network traffic. Traffic can be
      * reduced up to 90%. Therefore, stream compression is ideal when using a slow speed network
      * connection. However, the server and the client will need to use more CPU time in order to
      * un/compress network data so under high load the server performance might be affected.<p>
      * <p/>
      * Stream compression has to have been previously offered by the server. Currently only the
      * zlib method is supported by the client. Stream compression negotiation has to be done
      * before authentication took place.<p>
      * <p/>
      * Note: to use stream compression the smackx.jar file has to be present in the classpath.
      */
     private void enableCompressionMethod(String method) throws IOException {
         writer.write("<compress xmlns='http://jabber.org/protocol/compress'>");
         writer.write("<method>" + method + "</method></compress>");
         writer.flush();
     }
 
     /**
      * Parse the <stream version> string.  If not present, treat the
      * version as "0.9" as required by spec.  If the string fails to
      * parse, raises an exception.
      * 
      * @return the version number multiplied by 100; version 1.5 is 150 
      */
     private static int parseVersionString(String version) throws XMPPException {
         if(version == null)
             version = "0.9";
 
         // Verify the string all at once, so we don't have to do it below.
         if(!version.matches("\\d+(\\.\\d*)?"))
             throw new XMPPException("Invalid version string from server: " + version);
 
         String[] parts = version.split("\\.");
         int major = Integer.parseInt(parts[0]);
         int minor = Integer.parseInt(parts[1]);
         if(minor < 10)
             minor *= 10;
 
         return major * 100 + (minor % 100);
     }
     
     /**
      * Forcibly disconnect the stream.  If readPacket() is waiting for input, it will
      * return end of stream immediately.  Note that we can't simply interrupt the thread
      * that's reading; the SSL layer doesn't always implement interruption.  
      */
     boolean connectionClosed = false;
     public void disconnect() {
         connectionClosed = true;
         try {
             // Closing the socket will cause any blocking readers and writers on the
             // socket to stop waiting and throw an exception.
             if(socket != null)
                 socket.close();
         } catch(IOException e) {
             throw new RuntimeException("Unexpected I/O error disconnecting socket", e);
         }
     }
 
     public Element readPacket() throws XMPPException {
         if(bufferedPacket != null) {
             Element packet = bufferedPacket;
             bufferedPacket = null;
             return packet;
         }
 
         if(connectionClosed)
             return null;
         
         try {
             /* This will only loop when the connection is newly opened or after an
              * RFC6120 4.3.3 stream restart.  At other times, this will simply read
              * a packet and return it. */
             while(true) {
                 /* If we havn't yet received the opening <stream> tag, wait until we get it. */
                 if(parser.getDepth() == 0) {
                     // Read the <stream> tag.
                     parser.next();
 
                     // If we get an END_DOCUMENT here, the stream closed without sending any
                     // data other than comments and other things XmlPullParser hides from us. 
                     if (parser.getEventType() == XmlPullParser.END_DOCUMENT)
                         return null;
 
                     if (parser.getEventType() != XmlPullParser.START_TAG)
                         throw new RuntimeException("Unexpected state from XmlPullParser: " + parser.getEventType());
 
                     // Check that the opening stream is what we expect.
                     if (!parser.getName().equals("stream") ||
                             !parser.getNamespace().equals("http://etherx.jabber.org/streams") ||
                             !parser.getNamespace(null).equals("jabber:client")) {
                         throw new XMPPException("Expected stream:stream");
                     }
 
                     // Save the connection id.
                     connectionID = parser.getAttributeValue("", "id");
 
                     // Save the service name.
                     String from = parser.getAttributeValue("", "from");
                     if(from != null)
                         config.setServiceName(from);
 
                     // If the server isn't version 1.0, then we'll assume it doesn't support
                     // <features/>.  Consider the connection established now.
 
                     // The version attribute is only present for version 1.0 and higher.
                     int protocolVersion = parseVersionString(parser.getAttributeValue("", "version"));
 
                     // If the protocol version is lower than 1.0, we may not receive <features/>.
                     // Disable compression and encryption, and establish the session now.
                     if(protocolVersion < 100) {
                         // Old versions of the protocol may not send <features/>.  Mask this to the
                         // user by returning a dummy <features/> node.
                         try {
                             DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
                             DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
                             Document doc = docBuilder.newDocument();
                             return doc.createElementNS("http://etherx.jabber.org/streams", "features");
                         } catch(ParserConfigurationException e) {
                             throw new RuntimeException("Unexpected error", e);
                         }
                     }
                 }
                 
                 // Read the next packet.
                 parser.next();
                 
                 /* If there are any text nodes between stanzas, ignore them. */
                 while (parser.getEventType() == XmlPullParser.TEXT)
                     parser.next();
 
                 /* If this is an END_TAG, then the </stream:stream> has been closed; the
                  * connection is closing.  XXX: If close() was called then this is expected,
                  * and we should return null. */
                 if (parser.getEventType() == XmlPullParser.END_TAG)
                     throw new XMPPException("Session terminated");
 
                 // END_DOCUMENT means the stream has ended.
                 if (parser.getEventType() == XmlPullParser.END_DOCUMENT)
                     return null;
 
                 assert(parser.getDepth() == 2);
 
                 /* We have an XMPP stanza.  Read the whole thing into a DOM node and return it. */
                 return ReadNodeFromXmlPull(parser);
             }
         }
         catch (XmlPullParserException e) {
             throw new XMPPException("XML error", e);
         }
         catch (IOException e) {
             if(connectionClosed)
                 return null;
             throw new XMPPException("I/O error", e);
         }
     }
     
     /**
      * Read a single complete XMPP stanza from parser, returning it as a DOM Element.
      */
     private Element ReadNodeFromXmlPull(XmlPullParser parser) throws XMPPException, IOException
     {
         try {
             // XXX: stash the DocumentBuilder; don't make a new one on every stanza
             DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
             dbfac.setNamespaceAware(true);
             DocumentBuilder docBuilder;
             try {
                 docBuilder = dbfac.newDocumentBuilder();
             } catch (ParserConfigurationException e) {
                 throw new RuntimeException("Unexpected parser error", e);
             }
             Document doc = docBuilder.newDocument();
 
             LinkedList<Node> documentTree = new LinkedList<Node>();
             while(true)
             {
                 switch(parser.getEventType())
                 {
                 case XmlPullParser.START_TAG:
                 {
                     Element tag = doc.createElementNS(parser.getNamespace(), parser.getName());
                     if(!documentTree.isEmpty())
                     {
                         Node parent = documentTree.getLast();
                         parent.appendChild(tag);
                     }
                     
                     /* Converting namespace declarations back and forth between DOM and XmlPullParser
                      * is annoying, because XmlPullParser only tells us the current list, not what's
                      * actually changed; we'd need to compare the current list against the parent's.
                      * This information is only needed to explicitly detecting namespace declarations,
                      * not to retain node namespaces, so for simplicity and efficiency, this isn't done. */
 /*
                         for(int i = 0; i < parser.getNamespaceCount(parser.getDepth()); ++i)
                         {
                             String prefix = parser.getNamespacePrefix(i);
                             String uri = parser.getNamespaceUri(i);
                             tag.setAttribute("xmlns:" + prefix, uri);
                         }
 */                      
                     for(int i = 0; i < parser.getAttributeCount(); ++i)
                     {
                         String name = parser.getAttributeName(i);
                         String namespace = parser.getAttributeNamespace(i);
 
                         /* For XmlPullParser, no namespace is "".  For DOM APIs, no namespace is null. */
                         if(namespace == "")
                             namespace = null;
                         
                         String value = parser.getAttributeValue(i);
 
                         Attr attr = doc.createAttributeNS(namespace, name);
                         attr.setValue(value);
                         tag.setAttributeNode(attr);
                     }
 
                     documentTree.add(tag);
                     break;
                 }
                 case XmlPullParser.END_TAG:
                 {
                     Node removed = documentTree.removeLast();
                     
                     /* If we popped the top-level node, then it's the final result. */
                     if(documentTree.isEmpty())
                         return (Element) removed;
                     break;
                 }
                 case XmlPullParser.TEXT:
                 {
                     Node tag = doc.createTextNode(parser.getText());
                     Node parent = documentTree.getLast();
                     parent.appendChild(tag);
                     break;
                 }
                 case XmlPullParser.END_DOCUMENT:
                     // Normally, we'll never receive END_DOCUMENT, because we're parsing a
                     // single sub-tree; we stop when we reach the end tag matching the open
                     // tag we started on, and never continue to receive END_DOCUMENT. If we
                     // receive END_DOCUMENT, that means the document ended mid-stanza.
                     throw new XMPPException("Stream closed unexpectedly");
                 }
 
                 parser.next();
             }
         }
         catch (XmlPullParserException e) {
             throw new XMPPException("XML error", e);
         }
     }
 
     /**
      * Resets the parser using the latest connection's reader. Reseting the parser is necessary
      * when the plain connection has been secured or when a new opening stream element is going
      * to be sent by the server.
      */
     private void resetParser()
     {
         try {
             parser = XmlPullParserFactory.newInstance().newPullParser();
             parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
             parser.setInput(reader);
         }
         catch (XmlPullParserException xppe) {
             xppe.printStackTrace();
         }
     }
 
     /**
      * Sends to the server a new stream element. This operation may be requested several times
      * so we need to encapsulate the logic in one place. This message will be sent while doing
      * TLS, SASL and resource binding.
      *
      * @throws IOException If an error occurs while sending the stanza to the server.
      */
     private void openStream() throws IOException {
         StringBuilder stream = new StringBuilder();
         stream.append("<stream:stream");
         stream.append(" to=\"").append(config.getServiceName()).append("\"");
         stream.append(" xmlns=\"jabber:client\"");
         stream.append(" xmlns:stream=\"http://etherx.jabber.org/streams\"");
         stream.append(" version=\"1.0\">");
         writer.write(stream.toString());
         writer.flush();
     }
 
     private void initReaderAndWriter() throws XMPPException, IOException
     {
         InputStream inputStream = socket.getInputStream(); 
         OutputStream outputStream = socket.getOutputStream(); 
 
         if(usingXMPPCompression) {
             try {
                 Class<?> zoClass = Class.forName("com.jcraft.jzlib.ZOutputStream");
                 Constructor<?> constructor =
                     zoClass.getConstructor(OutputStream.class, Integer.TYPE);
                 OutputStream compressed_out = (OutputStream) constructor.newInstance(outputStream, 9);
                 Method method = zoClass.getMethod("setFlushMode", Integer.TYPE);
                 method.invoke(compressed_out, 2); // Z_SYNC_FLUSH
     
                 Class<?> ziClass = Class.forName("com.jcraft.jzlib.ZInputStream");
                 constructor = ziClass.getConstructor(InputStream.class);
                 InputStream compressed_in = (InputStream) constructor.newInstance(inputStream);
                 method = ziClass.getMethod("setFlushMode", Integer.TYPE);
                 method.invoke(compressed_in, 2); // Z_SYNC_FLUSH
     
                 inputStream = compressed_in;
                 outputStream = compressed_out;
             }
             catch (Exception e) {
                 // If this fails, we can't continue; the other side is expecting
                 // compression.  This shouldn't fail, since we checked importing the
                 // class earlier.
                 e.printStackTrace();
                 throw new RuntimeException("Unexpected error initializing compression", e);
             }
         }        
         
         reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
         writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
         
         /* Send incoming and outgoing data to the read and write observers, if any. */
         if(readEvent != null) {
             ObservableReader obsReader = new ObservableReader(reader);
             obsReader.setReadEvent(readEvent);
             reader = obsReader;
         }
 
         if(writeEvent != null) {
             ObservableWriter obsWriter = new ObservableWriter(writer);
             obsWriter.setWriteEvent(writeEvent);
             writer = obsWriter;
         }
 
         // For keepalive purposes, add an observer wrapper to monitor writes.
         {
             ObservableWriter obsWriter = new ObservableWriter(writer);
             obsWriter.setWriteEvent(keepaliveMonitorWriteEvent);
             writer = obsWriter;
         }
         
         streamReset();
     }
     
     public void close()
     {
         /* Attempt to close the stream. */
         try {
             if(writer != null) {
                 writer.write("</stream:stream>");
                 writer.flush();
             }
         }
         catch (IOException e) {
             // Do nothing
         }
         
         try {
             if(socket != null)
                 socket.close();
         }
         catch (IOException ignore) { /* ignore */ }
 
         // Shut down the keepalive thread, if any.  Do this after closing the socket,
         // so it'll receive an IOException immediately if it's currently blocking to write,
         // guaranteeing it'll exit quickly.
         if(this.keepAlive != null) {
             this.keepAlive.close();
             this.keepAlive = null;
         }
         
         this.socket = null;
         
         if (reader != null) {
             try { reader.close(); } catch (IOException ignore) { /* ignore */ }
             reader = null;
         }
        
         if (writer != null) {
             try { writer.close(); } catch (IOException ignore) { /* ignore */ }
             writer = null;
         }
     }
 
     private void proceedTLSReceived() throws Exception
     {
         // Secure the plain connection
         SSLSocket sslSocket = (SSLSocket) sslSocketFactory.getSocketFactory().createSocket(socket, originalServiceName, socket.getPort(), true);
         sslSocket.setSoTimeout(0);
         
         // We have our own keepalive.  Don't enabling TCP keepalive, too.
         // sslSocket.setKeepAlive(true);
         
         // Proceed to do the handshake
         sslSocket.startHandshake();
 
         socket = sslSocket;
         
         // If usingSecureConnection is false, we're encrypted but we couldn't verify
         // the server's certificate.
         usingTLS = true;
         CertificateException insecureReason = sslSocketFactory.isInsecureConnection(sslSocket);
         usingSecureConnection = (insecureReason == null);
 
         // Record if TLS compression is active, so we won't try to negotiate XMPP
         // compression too.   
         if(sslSocketFactory.getCompressionMethod(sslSocket) != null)
             usingTLSCompression = true;
 
         // Initialize the reader and writer with the new secured version
         initReaderAndWriter();
         
         // If !usingSecureConnection, then we have an encrypted TLS connection but with
         // an unvalidated certificate.  If a secure connection was required, fail.
         if(!usingSecureConnection && config.getSecurityMode() == ConnectionConfiguration.SecurityMode.required) {
             throw new XMPPException("Server does not support security (TLS), " + 
                     "but the configuration requires a secure connection.",
                     XMPPError.Condition.forbidden, insecureReason);
         }
     }
 
     /**
      * When authentication is successful, we must open a new <stream:stream>.
      * This is step 12 in http://xmpp.org/extensions/xep-0178.html#c2s. 
      */
     public void streamReset() throws XMPPException
     {
         /* The <stream:stream> element will not be closed after a stream reset,
          * so reset the parser state. */
         resetParser();
 
         /* Send the stream:stream to start the new stream. */
         try {
             openStream();
         } catch(IOException e) {
             throw new XMPPException("Error resetting stream", e);
         }
     }
 
     /**
      * Starts the keepalive process. A white space (aka heartbeat) is going to be
      * sent to the server every 30 seconds (by default) since the last stanza was sent
      * to the server.
      */
     private KeepAliveTask keepAlive;
     private void startKeepAliveProcess() {
         // Schedule a keep-alive task to run if the feature is enabled. will write
         // out a space character each time it runs to keep the TCP/IP connection open.
         int keepAliveInterval = SmackConfiguration.getKeepAliveInterval();
         if (keepAliveInterval == 0)
             return;
         
         KeepAliveTask task = new KeepAliveTask(keepAliveInterval);
         keepAlive = task;
     }
 
     /**
      * A TimerTask that keeps connections to the server alive by sending a space
      * character on an interval.
      */
     private class KeepAliveTask implements Runnable {
         private int delay;
         private Thread thread;
         private boolean done = false;
 
         public KeepAliveTask(int delay) {
             this.delay = delay;
             
             Thread keepAliveThread = new Thread(this);
             keepAliveThread.setDaemon(true);
             keepAliveThread.setName("Smack Keepalive");
             keepAliveThread.start();
             this.thread = keepAliveThread;
         }
 
         /**
          * Close the keepalive thread. 
          */
         public void close() {
             synchronized (this) {
                 done = true;
                 thread.interrupt();
             }
 
             boolean interrupted = false;
             while(true) {
                 try {
                     thread.join();
                 } catch(InterruptedException e) {
                     // Defer interruptions until we're done.
                     interrupted = true;
                     continue;
                 }
                 break;
             }
             if(interrupted)
                 Thread.currentThread().interrupt();
         }
 
         long lastActive = System.currentTimeMillis();
         public void run() {
             // Add a write listener to track the time of the most recent write.  This is used
             // to only send heartbeats when the connection is idle.
             long lastActive[] = {System.currentTimeMillis()};
             WriterListener listener = new WriterListener() {
                 public void write(String str) {
                     KeepAliveTask.this.lastActive = System.currentTimeMillis();
                 }
             };
             keepaliveMonitorWriteEvent.addWriterListener(listener);
             
             while (true) {
                 Writer writer = getWriter(); 
                 synchronized (writer) {
                     // Send heartbeat if no packet has been sent to the server for a given time
                     if (System.currentTimeMillis() - KeepAliveTask.this.lastActive >= delay) {
                         try {
                             writer.write(" ");
                             writer.flush();
                         }
                         catch (IOException e) {
                             // Do nothing, and assume that whatever caused an error
                             // here will cause one in the main code path, too.  This
                             // will also happen if the write blocked and XMPPStreamTCP.disconnect
                             // closed the socket.
                         }
                     }
                 }
 
                 try {
                     synchronized (this) {
                         if (done)
                             break;
                         
                         // Sleep until we should write the next keep-alive.
                         wait(delay);
                     }
                 }
                 catch (InterruptedException ie) {
                     // close() interrupted us to shut down the thread.
                     break;
                 }
             }
 
             keepaliveMonitorWriteEvent.removeWriterListener(listener);
         }
     }
 };
