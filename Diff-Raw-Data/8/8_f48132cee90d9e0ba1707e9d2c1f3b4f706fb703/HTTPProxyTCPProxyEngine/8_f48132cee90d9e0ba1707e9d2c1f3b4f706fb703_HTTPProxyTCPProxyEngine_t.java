 // Copyright (C) 2000, 2001, 2002, 2003, 2004 Philip Aston
 // Copyright (C) 2000, 2001 Phil Dawes
 // Copyright (C) 2001 Paddy Spencer
 // Copyright (C) 2003 Bertrand Ave
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.tools.tcpproxy;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.InterruptedIOException;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.oro.text.regex.MalformedPatternException;
 import org.apache.oro.text.regex.MatchResult;
 import org.apache.oro.text.regex.Pattern;
 import org.apache.oro.text.regex.PatternCompiler;
 import org.apache.oro.text.regex.PatternMatcher;
 import org.apache.oro.text.regex.Perl5Compiler;
 import org.apache.oro.text.regex.Perl5Matcher;
 
 import net.grinder.common.GrinderBuild;
 import net.grinder.common.Logger;
 import net.grinder.util.StreamCopier;
 import net.grinder.util.html.HTMLElement;
 
 
 /**
  * HTTP/HTTPS proxy implementation.
  *
  * <p>A HTTPS proxy client first send a CONNECT message to the proxy
  * port. The proxy accepts the connection responds with a 200 OK,
  * which is the client's queue to send SSL data to the proxy. The
  * proxy just forwards it on to the server identified by the CONNECT
  * message.</p>
  *
  * <p>The Java API presents a particular challenge: it allows sockets
  * to be either SSL or not SSL, but doesn't let them change their
  * stripes midstream. (In fact, if the JSSE support was stream
  * oriented rather than socket oriented, a lot of problems would go
  * away). To hack around this, we accept the CONNECT then blindly
  * proxy the rest of the stream through a special
  * TCPProxyEngineImplementation which instantiated to handle SSL.</p>
  *
  * @author Paddy Spencer
  * @author Philip Aston
  * @author Bertrand Ave
  * @version $Revision$
  */
 public final class HTTPProxyTCPProxyEngine extends AbstractTCPProxyEngine {
 
   private final PatternMatcher m_matcher = new Perl5Matcher();
   private final Pattern m_httpConnectPattern;
   private final Pattern m_httpsConnectPattern;
   private final ProxySSLEngine m_proxySSLEngine;
   private final EndPoint m_chainedHTTPProxy;
   private final HTTPSProxySocketFactory m_httpsProxySocketFactory;
   private final EndPoint m_proxyAddress;
 
   /**
    * Constructor.
    *
    * @param sslSocketFactory Factory for SSL sockets.
    * @param requestFilter Request filter.
    * @param responseFilter Response filter.
    * @param logger Logger.
    * @param localEndPoint Local host and port.
    * @param useColour Whether to use colour.
    * @param timeout Timeout for server socket in milliseconds.
    * @param chainedHTTPProxy HTTP proxy which output should be routed
    * through, or <code>null</code> for no proxy.
    * @param chainedHTTPSProxy HTTP proxy which output should be routed
    * through, or <code>null</code> for no proxy.
    *
    * @exception IOException If an I/O error occurs
    * @exception MalformedPatternException If a regular expression
    * error occurs.
    */
   public HTTPProxyTCPProxyEngine(TCPProxySSLSocketFactory sslSocketFactory,
                                  TCPProxyFilter requestFilter,
                                  TCPProxyFilter responseFilter,
                                  Logger logger,
                                  EndPoint localEndPoint,
                                  boolean useColour,
                                  int timeout,
                                  EndPoint chainedHTTPProxy,
                                  EndPoint chainedHTTPSProxy)
     throws IOException, MalformedPatternException {
 
     // We set this engine up for handling plain connections. We
     // delegate HTTPS to a proxy engine.
     super(new TCPProxySocketFactoryImplementation(), requestFilter,
           responseFilter, logger, localEndPoint, useColour, timeout);
 
     m_proxyAddress = localEndPoint;
     m_chainedHTTPProxy = chainedHTTPProxy;
 
     final PatternCompiler compiler = new Perl5Compiler();
 
     m_httpConnectPattern = compiler.compile(
       "^([A-Z]+)[ \\t]+http://([^/:]+):?(\\d*)(/.*)",
       Perl5Compiler.MULTILINE_MASK  | Perl5Compiler.READ_ONLY_MASK);
 
     m_httpsConnectPattern = compiler.compile(
       "^CONNECT[ \\t]+([^:]+):(\\d+)",
       Perl5Compiler.MULTILINE_MASK | Perl5Compiler.READ_ONLY_MASK);
 
     // When handling HTTPS proxies, we use our plain socket to accept
     // connections on. We suck the bit we understand off the front and
     // forward the rest through our proxy engine. The proxy engine
     // listens for connection attempts (which come from us), then sets
     // up a thread pair which pushes data back and forth until either
     // the server closes the connection, or we do (in response to our
     // client closing the connection). The engine handles multiple
     // connections by spawning multiple thread pairs.
     if (chainedHTTPSProxy != null) {
       m_httpsProxySocketFactory =
         new HTTPSProxySocketFactory(sslSocketFactory, chainedHTTPSProxy);
 
       m_proxySSLEngine =
         new ProxySSLEngine(m_httpsProxySocketFactory, requestFilter,
                            responseFilter, logger, useColour);
     }
     else {
       m_httpsProxySocketFactory = null;
 
       m_proxySSLEngine =
         new ProxySSLEngine(sslSocketFactory, requestFilter, responseFilter,
                            logger, useColour);
     }
 
     new Thread(m_proxySSLEngine, "HTTPS proxy SSL engine").start();
   }
 
   /**
    * Main event loop.
    */
   public void run() {
 
     // Should be more than adequate.
     final byte[] buffer = new byte[4096];
 
     try {
       while (true) {
         final Socket localSocket = accept();
 
         // Grab the first upstream packet and grep it for the
         // remote server and port in the method line.
         final BufferedInputStream in =
           new BufferedInputStream(localSocket.getInputStream(), buffer.length);
 
         in.mark(buffer.length);
 
         // Read a buffer full.
         final int bytesRead = in.read(buffer);
 
         final String line =
           bytesRead > 0 ? new String(buffer, 0, bytesRead, "US-ASCII") : "";
 
         if (m_matcher.contains(line, m_httpConnectPattern)) {
           // HTTP proxy request.
 
           // Reset stream to beginning of request.
           in.reset();
 
           new Thread(
             getStreamHandlerThreadGroup(),
             new HTTPProxyStreamDemultiplexer(
               in, localSocket, EndPoint.clientEndPoint(localSocket)),
               "HTTPProxyStreamDemultiplexer for " + localSocket).start();
         }
         else if (m_matcher.contains(line, m_httpsConnectPattern)) {
           // HTTPS proxy request.
 
           final MatchResult match = m_matcher.getMatch();
 
           // Match.group(2) must be a port number by specification.
           final EndPoint remoteEndPoint =
             new EndPoint(match.group(1), Integer.parseInt(match.group(2)));
 
           if (m_httpsProxySocketFactory != null) {
             // Read additional data from the client (HTTP protocol
             // version and headers) and pass it to our
             // HTTPSProxySocketFactory for use in the establishment of
             // the chained proxy connection.
 
             final ByteArrayOutputStream additionalRequestBytes =
               new ByteArrayOutputStream();
 
             additionalRequestBytes.write(
               line.substring(match.end(2)).getBytes());
 
             int n;
 
            while ((n = in.read(buffer)) > 0) {
               additionalRequestBytes.write(buffer, 0, n);
             }
 
             m_httpsProxySocketFactory.setAdditionalRequestBytes(
               additionalRequestBytes.toByteArray());
           }
           else {
             // Discard anything else the client has to say.
            while (in.read(buffer) > 0) {
               // Skip..
             }
           }
 
           // Set our proxy engine up to create connections to the
           // remoteEndPoint.
           m_proxySSLEngine.setConnectionDetails(
             EndPoint.clientEndPoint(localSocket), remoteEndPoint);
 
           // Create a new proxy connection to the proxy engine.
           final Socket sslProxySocket =
             getSocketFactory().createClientSocket(
               m_proxySSLEngine.getListenEndPoint());
 
           // Set up a couple of threads to punt everything we receive
           // over localSocket to sslProxySocket, and vice versa.
           new Thread(
             getStreamHandlerThreadGroup(),
             new StreamCopier(4096, true)
             .getRunnable(in, sslProxySocket.getOutputStream()),
             "Copy to proxy engine for " + remoteEndPoint).start();
 
           final OutputStream out = localSocket.getOutputStream();
 
           new Thread(
             getStreamHandlerThreadGroup(),
             new StreamCopier(4096, true)
             .getRunnable(sslProxySocket.getInputStream(), out),
             "Copy from proxy engine for " + remoteEndPoint).start();
 
           if (m_httpsProxySocketFactory != null) {
             // Chuck the chained proxy's response back as our own.
             out.write(m_httpsProxySocketFactory.getResponseBytes());
             out.flush();
           }
           else {
             // Send a 200 response to send to client. Client
             // will now start sending SSL data to localSocket.
             final StringBuffer response = new StringBuffer();
             response.append("HTTP/1.0 200 OK\r\n");
             response.append("Host: ");
             response.append(remoteEndPoint);
             response.append("\r\n");
             response.append("Proxy-agent: The Grinder/");
             response.append(GrinderBuild.getVersionString());
             response.append("\r\n");
             response.append("\r\n");
 
             out.write(response.toString().getBytes());
             out.flush();
           }
         }
         else {
           final HTMLElement message = new HTMLElement();
 
           message.addElement("p").addText(
             "Failed to determine proxy destination from message. ");
 
           final HTMLElement paragraph1 = message.addElement("p");
           paragraph1.addText(
             "Do not type TCPProxy address into your browser. ");
           paragraph1.addText("The browser proxy settings should be set to " +
                              "the TCPProxy address (");
           paragraph1.addElement("code").addText(m_proxyAddress.toString());
           paragraph1.addText("), and you should type the address of the " +
                            "target server into the browser.");
 
           message.addElement("p").addText("Text of received message follows:");
 
           message.addElement("pre").addElement("blockquote").addText(line);
 
           sendHTTPErrorResponse(message, "400 Bad Request",
                                 localSocket.getOutputStream());
 
           localSocket.close();
         }
       }
     }
     catch (IOException e) {
       logIOException(e);
     }
   }
 
   private void sendHTTPErrorResponse(HTMLElement message, String status,
                                      OutputStream outputStream)
     throws IOException {
     getLogger().error(message.toText());
 
     final HTTPResponse response = new HTTPResponse();
     response.setStatus(status);
     response.setMessage(status, message);
 
     outputStream.write(response.toByteArray());
   }
 
   /**
    * Runnable that actively reads from an Input stream, greps every
    * outgoing packet, and directs appropriately. This is necessary to
    * support HTTP/1.1 between the browser and TCPProxy.
    */
   private final class HTTPProxyStreamDemultiplexer implements Runnable {
 
     private final InputStream m_in;
     private final Socket m_localSocket;
     private final EndPoint m_clientEndPoint;
     private final PatternMatcher m_matcher = new Perl5Matcher();
     private final Map m_remoteStreamMap = new HashMap();
     private OutputStreamFilterTee m_lastRemoteStream;
 
     HTTPProxyStreamDemultiplexer(InputStream in, Socket localSocket,
                                  EndPoint clientEndPoint) {
       m_in = in;
       m_localSocket = localSocket;
       m_clientEndPoint = clientEndPoint;
     }
 
     public void run() {
 
       final byte[] buffer = new byte[4096];
 
       try {
         while (true) {
           // Read a buffer full. We're relying on the world conspiring
           // to place request at start of buffer.
           final int bytesRead = m_in.read(buffer);
 
           if (bytesRead == -1) {
             break;
           }
 
           final String bytesReadAsString =
             new String(buffer, 0, bytesRead, "US-ASCII");
 
           if (m_matcher.contains(bytesReadAsString, m_httpConnectPattern)) {
 
             final MatchResult match = m_matcher.getMatch();
             final String remoteHost = match.group(2);
 
             int remotePort = 80;
 
             try {
               remotePort = Integer.parseInt(match.group(3));
             }
             catch (NumberFormatException e) {
               // remotePort = 80;
             }
 
             final EndPoint remoteEndPoint =
               new EndPoint(remoteHost, remotePort);
 
             final String key = remoteEndPoint.toString();
 
             m_lastRemoteStream =
               (OutputStreamFilterTee) m_remoteStreamMap.get(key);
 
             if (m_lastRemoteStream == null) {
 
               // New connection.
 
               final Socket remoteSocket;
               final TCPProxyFilter requestFilter;
 
               if (m_chainedHTTPProxy != null) {
                 // When running through a chained HTTP proxy, we still
                 // create a new thread pair to handle each target
                 // server. This allows us to reuse
                 // FilteredStreamThread and OutputStreamFilterTee to
                 // log the correct connection details. It may also be
                 // beneficial for performance.
                 remoteSocket =
                   getSocketFactory().createClientSocket(m_chainedHTTPProxy);
 
                 requestFilter =
                   new HTTPMethodAbsoluteURIFilterDecorator(
                     new HTTPMethodRelativeURIFilterDecorator(
                       getRequestFilter()), remoteEndPoint);
               }
               else {
                 remoteSocket =
                   getSocketFactory().createClientSocket(remoteEndPoint);
 
                 requestFilter =
                   new HTTPMethodRelativeURIFilterDecorator(getRequestFilter());
               }
 
               final ConnectionDetails connectionDetails =
                 new ConnectionDetails(m_clientEndPoint, remoteEndPoint, false);
 
               m_lastRemoteStream =
                 new OutputStreamFilterTee(connectionDetails,
                                           remoteSocket.getOutputStream(),
                                           requestFilter,
                                           getRequestColour());
 
               m_lastRemoteStream.connectionOpened();
 
               m_remoteStreamMap.put(key, m_lastRemoteStream);
 
               // Spawn a thread to handle everything coming back from
               // the remote server.
               new FilteredStreamThread(
                 remoteSocket.getInputStream(),
                 new OutputStreamFilterTee(connectionDetails.getOtherEnd(),
                                           m_localSocket.getOutputStream(),
                                           getResponseFilter(),
                                           getResponseColour()));
             }
           }
           else if (m_lastRemoteStream == null) {
             throw new IOException("Assertion failure - no last stream");
           }
 
           // Should do filtering etc.
           m_lastRemoteStream.handle(buffer, bytesRead);
         }
       }
       catch (IOException e) {
         // Perhaps we should decorate the OutputStreamFilterTee's so
         // that we can return exceptions as some simple HTTP error
         // page?
 
         logIOException(e);
       }
       finally {
         // When exiting, close all our outgoing streams. This will
         // force all the FilteredStreamThreads we've launched to
         // handle the paired streams to shut down.
         final Iterator iterator = m_remoteStreamMap.values().iterator();
 
         while (iterator.hasNext()) {
           ((OutputStreamFilterTee)iterator.next()).connectionClosed();
         }
 
         // We may not have any FilteredStreamThreads, so ensure the
         // local socket is closed. The local socket is shutdown on any
         // error, any browser using us will open up a new connection
         // for new work.
         try {
           m_localSocket.close();
         }
         catch (IOException e) {
           // Ignore.
         }
       }
     }
   }
 
   private final class ProxySSLEngine extends AbstractTCPProxyEngine {
 
     private EndPoint m_clientEndPoint;
     private EndPoint m_remoteEndPoint;
 
     ProxySSLEngine(TCPProxySocketFactory socketFactory,
                    TCPProxyFilter requestFilter,
                    TCPProxyFilter responseFilter,
                    Logger logger,
                    boolean useColour)
     throws IOException {
       super(socketFactory, requestFilter, responseFilter, logger,
             new EndPoint(InetAddress.getByName(null), 0), useColour, 0);
     }
 
     public void run() {
 
       while (true) {
         try {
           final Socket localSocket = accept();
 
           launchThreadPair(
             localSocket, m_remoteEndPoint, m_clientEndPoint, true);
         }
         catch (IOException e) {
           logIOException(e);
         }
       }
     }
 
     /**
      * Set the ProxySSLEngine up so that the next connection will be
      * wired through to <code>remoteHost:remotePort</code>.
      */
     public void setConnectionDetails(EndPoint clientEndPoint,
                                      EndPoint remoteEndPoint) {
       m_clientEndPoint = clientEndPoint;
       m_remoteEndPoint = remoteEndPoint;
     }
   }
 
   /**
    * SocketFactory decorator that sets up HTTPS proxy connections on
    * client sockets it returns.
    */
   private final class HTTPSProxySocketFactory
     implements TCPProxySocketFactory {
 
     private final TCPProxySSLSocketFactory m_delegate;
     private final EndPoint m_httpsProxy;
 
     private byte[] m_additionalRequestBytes;
     private byte[] m_responseBytes;
 
     /**
      * Constructor.
      *
      * @param delegate Socket factory to decorate.
      * @param chainedHTTPSProxy HTTPS proxy to direct connections
      * through.
      */
     public HTTPSProxySocketFactory(TCPProxySSLSocketFactory delegate,
                                    EndPoint chainedHTTPSProxy) {
 
       m_delegate = delegate;
       m_httpsProxy = chainedHTTPSProxy;
     }
 
     /**
      * Factory method for server sockets. We do nothing special here.
      *
      * @param localEndPoint Local host and port.
      * @param timeout Socket timeout.
      * @return A new <code>ServerSocket</code>.
      * @exception IOException If an error occurs.
      */
     public ServerSocket createServerSocket(EndPoint localEndPoint, int timeout)
       throws IOException {
       return m_delegate.createServerSocket(localEndPoint, timeout);
     }
 
     /**
      * Set additional header bytes to send after the CONNECT header
      * when establishing the next client socket.
      *
      * @param additionalRequestBytes The bytes.
      */
     public void setAdditionalRequestBytes(byte[] additionalRequestBytes) {
       m_additionalRequestBytes = additionalRequestBytes;
     }
 
     /**
      * The response returned from the chained proxy during the last
      * client socket establishment. Blocks until bytes are available.
      *
      * @return The response bytes.
      * @throws IOException If thread interrupted whilst waiting.
      */
     public byte[] getResponseBytes() throws IOException {
       synchronized (this) {
         try {
           while (m_responseBytes == null) {
             try {
               wait();
             }
             catch (InterruptedException e) {
               throw new InterruptedIOException("Thread interrupted");
             }
           }
 
           return m_responseBytes;
         }
         finally {
           // Reset for next time.
           m_responseBytes = null;
         }
       }
     }
 
     /**
      * Factory method for client sockets.
      *
      * @param remoteEndPoint Remote host and port.
      * @return A new <code>Socket</code>.
      * @exception IOException If an error occurs.
      */
     public Socket createClientSocket(EndPoint remoteEndPoint)
       throws IOException {
 
       final Socket socket =
         new Socket(m_httpsProxy.getHost(), m_httpsProxy.getPort());
 
       final OutputStream outputStream = socket.getOutputStream();
       final InputStream inputStream = socket.getInputStream();
 
       outputStream.write(("CONNECT " + remoteEndPoint).getBytes());
       outputStream.write(m_additionalRequestBytes);
       outputStream.flush();
 
       // Wait for, and discard response.
       final byte[] buffer = new byte[1024];
 
       final long timeout =
         Long.getLong("tcpproxy.httpsproxy.connecttimeout", 5000).longValue();
 
       for (int i = 0; i < timeout && inputStream.available() == 0; ++i) {
         try {
           Thread.sleep(10);
         }
         catch (InterruptedException e) {
           throw new InterruptedIOException("Thread interrupted");
         }
       }
 
       if (inputStream.available() == 0) {
         throw new IOException(
           "HTTPS proxy " + m_httpsProxy + " failed to respond after " +
           timeout + " ms");
       }
 
       // We've got a live one. Read its response.
       final ByteArrayOutputStream responseBytes = new ByteArrayOutputStream();
       int n;
 
      while ((n = inputStream.read(buffer)) > 0) {
         responseBytes.write(buffer, 0, n);
       }
 
       synchronized (this) {
         m_responseBytes = responseBytes.toByteArray();
         notifyAll();
       }
 
       // HTTPS proxy protocol complete, now build the SSL socket.
       return m_delegate.createClientSocket(socket, m_httpsProxy);
     }
   }
 }
