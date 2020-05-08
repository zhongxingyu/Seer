 // Copyright (C) 2005, 2006 Philip Aston
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
 // COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.tools.tcpproxy;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.net.ssl.SSLSocket;
 
 import junit.framework.TestCase;
 import net.grinder.common.Logger;
 import net.grinder.common.LoggerStubFactory;
 import net.grinder.common.UncheckedInterruptedException;
 import net.grinder.testutility.AssertUtilities;
 import net.grinder.util.StreamCopier;
 import net.grinder.util.TerminalColour;
 
 
 /**
  * Unit test case for {@link HTTPProxyTCPProxyEngine}.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public class TestHTTPProxyTCPProxyEngine extends TestCase {
 
   private final List m_echoers = new java.util.LinkedList();
 
   private final MyFilterStubFactory m_requestFilterStubFactory =
     new MyFilterStubFactory();
   private final TCPProxyFilter m_requestFilter =
     m_requestFilterStubFactory.getFilter();
 
   private final MyFilterStubFactory m_responseFilterStubFactory =
     new MyFilterStubFactory();
   private final TCPProxyFilter m_responseFilter =
     m_responseFilterStubFactory.getFilter();
 
   private final LoggerStubFactory m_loggerStubFactory =
     new LoggerStubFactory();
   private final Logger m_logger = m_loggerStubFactory.getLogger();
 
   private EndPoint m_localEndPoint;
 
   private TCPProxySSLSocketFactory m_sslSocketFactory;
 
   private EndPoint createFreeLocalEndPoint() throws IOException {
     final ServerSocket serverSocket = new ServerSocket(0);
     final EndPoint result =
       new EndPoint("localhost", serverSocket.getLocalPort());
     serverSocket.close();
     return result;
   }
 
   protected void setUp() throws Exception {
     m_localEndPoint = createFreeLocalEndPoint();
 
     m_sslSocketFactory = new TCPProxySSLSocketFactoryImplementation();
 
     // Set the filters not to generate random output.
     m_requestFilterStubFactory.setResult(null);
     m_responseFilterStubFactory.setResult(null);
 
     // Speed things up.
     System.setProperty("tcpproxy.connecttimeout", "500");
   }
 
   protected void tearDown() throws Exception {
     final Iterator iterator = m_echoers.iterator();
 
     while (iterator.hasNext()) {
       ((AcceptAndEcho)iterator.next()).shutdown();
     }
   }
 
   public void testBadLocalPort() throws Exception {
     try {
       new HTTPProxyTCPProxyEngine(null,
                                   m_requestFilter,
                                   m_responseFilter,
                                   m_logger,
                                   new EndPoint("unknownhost", 222),
                                   false,
                                   1000,
                                   null,
                                   null);
       fail("Expected UnknownHostException");
     }
     catch (UnknownHostException e) {
     }
   }
 
   public void testTimeOut() throws Exception {
 
     final TCPProxyEngine engine =
       new HTTPProxyTCPProxyEngine(m_sslSocketFactory,
                                   m_requestFilter,
                                   m_responseFilter,
                                   m_logger,
                                   m_localEndPoint,
                                   false,
                                   10,
                                   null,
                                   null);
 
     m_loggerStubFactory.resetCallHistory();
 
     // If this ends up spinning its probably because
     // some other test has not terminated all of its filter
     // threads correctly.
     engine.run();
 
     m_loggerStubFactory.assertSuccess("error", "Listen time out");
     m_loggerStubFactory.assertNoMoreCalls();
   }
 
   /**
    * Read a response from a socket until it matches a given regular expression.
    *
    * @param clientSocket
    *          Socket to read from.
    * @param terminalExpression
    *          The expression, or <code>null</code> to return the first buffer
    *          full.
    * @return The response.
    * @throws IOException If a IO problem occurs.
    * @throws InterruptedException If we're interrupted.
    */
   private String readResponse(final Socket clientSocket,
                               String terminalExpression)
     throws IOException, InterruptedException {
     final InputStream clientInputStream = clientSocket.getInputStream();
 
     if (clientSocket instanceof SSLSocket) {
       // Another reason to hate JSSE: available() returns 0 until the
       // first read after the server has sent something; reading nothing
       // works around this.
       clientSocket.getInputStream().read(new byte[0]);
     }
 
     while (clientInputStream.available() <= 0) {
       Thread.sleep(10);
     }
 
     final ByteArrayOutputStream response = new ByteArrayOutputStream();
 
     // Don't use a StreamCopier because it will block reading the
     // input stream.
     final byte[] buffer = new byte[100];
 
     final Pattern terminalPattern =
       Pattern.compile(terminalExpression != null ? terminalExpression : ".*");
 
     while (true) {
       while (clientInputStream.available() > 0) {
         final int bytesRead = clientInputStream.read(buffer, 0, buffer.length);
         response.write(buffer, 0, bytesRead);
       }
 
      final String s = response.toString();
       final Matcher matcher = terminalPattern.matcher(s);
 
       if (matcher.find()) {
         return s;
       }
 
       final long RETRIES = 100;
 
       for (int i=0; i<RETRIES && clientInputStream.available() == 0; ++i) {
         Thread.sleep(10);
       }
 
       if (clientInputStream.available() == 0) {
         fail("Stream has been idle for " + (RETRIES * 10/1000d) +
              " seconds and the terminal expression '" + terminalExpression +
              "' does not match received data:\n" + s);
       }
     }
   }
 
   private void waitUntilAllStreamThreadsStopped(AbstractTCPProxyEngine engine)
     throws InterruptedException {
 
     for (int i = 0;
          i < 10 && engine.getStreamThreadGroup().activeCount() > 0;
          ++i) {
       Thread.sleep(50);
     }
 
     assertEquals("Failed waiting for all stream threads to stop",
                  0, engine.getStreamThreadGroup().activeCount());
   }
 
   private void httpProxyEngineBadRequestTests(AbstractTCPProxyEngine engine)
     throws Exception {
 
     final Socket clientSocket =
       new Socket(engine.getListenEndPoint().getHost(),
                  engine.getListenEndPoint().getPort());
 
     final PrintWriter clientWriter =
       new PrintWriter(clientSocket.getOutputStream(), true);
 
     final String message = "This is not a valid HTTP message";
     clientWriter.print(message);
     clientWriter.flush();
 
     final String response = readResponse(clientSocket, null);
 
     AssertUtilities.assertStartsWith(response, "HTTP/1.0 400 Bad Request");
     AssertUtilities.assertContainsHeader(response, "Connection", "close");
     AssertUtilities.assertContainsHeader(response, "Content-type", "text/html");
     AssertUtilities.assertContains(response, message);
 
     clientSocket.close();
 
     m_requestFilterStubFactory.assertNoMoreCalls();
     m_responseFilterStubFactory.assertNoMoreCalls();
 
     m_loggerStubFactory.assertSuccess("error", String.class);
     m_loggerStubFactory.assertNoMoreCalls();
 
     final Socket clientSocket2 =
       new Socket(engine.getListenEndPoint().getHost(),
                  engine.getListenEndPoint().getPort());
 
     clientSocket2.shutdownOutput();
 
     try {
       readResponse(clientSocket, null);
       fail("Expected IOException");
     }
     catch (IOException e) {
     }
 
     clientSocket2.close();
 
     m_loggerStubFactory.waitUntilCalled(10000);
     m_loggerStubFactory.assertSuccess("error", String.class);
 
     final Socket clientSocket3 =
       new Socket(engine.getListenEndPoint().getHost(),
                  engine.getListenEndPoint().getPort());
 
     final byte[] hugeBunchOfCrap = new byte[50000];
     clientSocket3.getOutputStream().write(hugeBunchOfCrap);
 
     final String response3 = readResponse(clientSocket3, null);
 
     AssertUtilities.assertStartsWith(response3, "HTTP/1.0 400 Bad Request");
     AssertUtilities.assertContainsHeader(response3, "Connection", "close");
     AssertUtilities.assertContainsHeader(response3, "Content-type", "text/html");
 
     clientSocket3.close();
 
     waitUntilAllStreamThreadsStopped(engine);
 
     m_loggerStubFactory.waitUntilCalled(10000);
     m_loggerStubFactory.assertSuccess("error", String.class);
 
     m_requestFilterStubFactory.assertNoMoreCalls();
     m_responseFilterStubFactory.assertNoMoreCalls();
 
     m_loggerStubFactory.assertNoMoreCalls();
   }
 
   private void httpProxyEngineGoodRequestTests(AbstractTCPProxyEngine engine)
     throws Exception {
 
     final AcceptAndEcho echoer = new AcceptAndEcho();
 
     final Socket clientSocket =
       new Socket(engine.getListenEndPoint().getHost(),
                  engine.getListenEndPoint().getPort());
 
     final PrintWriter clientWriter =
       new PrintWriter(clientSocket.getOutputStream(), true);
 
     final String message0 =
       "GET http://" + echoer.getEndPoint() + "/foo HTTP/1.1\r\n" +
       "foo: bah\r\n" +
       "\r\n" +
       "A \u00e0 message";
     clientWriter.print(message0);
     clientWriter.flush();
 
     final String response0 = readResponse(clientSocket, "message$");
 
     AssertUtilities.assertStartsWith(response0, "GET /foo HTTP/1.1\r\n");
     AssertUtilities.assertContainsHeader(response0, "foo", "bah");
     AssertUtilities.assertContainsPattern(response0,
                                           "\r\n\r\nA \u00e0 message$");
 
     m_requestFilterStubFactory.assertSuccess("connectionOpened",
                                              ConnectionDetails.class);
     m_requestFilterStubFactory.assertSuccess("handle",
                                              ConnectionDetails.class,
                                              new byte[0].getClass(),
                                              Integer.class);
     m_requestFilterStubFactory.assertNoMoreCalls();
 
     m_responseFilterStubFactory.assertSuccess("connectionOpened",
                                               ConnectionDetails.class);
     m_responseFilterStubFactory.assertSuccess("handle",
                                               ConnectionDetails.class,
                                               new byte[0].getClass(),
                                               Integer.class);
     m_responseFilterStubFactory.assertNoMoreCalls();
 
     m_loggerStubFactory.assertNoMoreCalls();
 
     final String message1Headers =
       "POST http://" + echoer.getEndPoint() + "/blah?x=123&y=99 HTTP/1.0\r\n" +
       "\r\n" +
       "Another message";
     clientWriter.print(message1Headers);
     clientWriter.flush();
 
     final String message1PostBody = "Some data, lah 0x810x820x830x84 dah";
     clientWriter.print(message1PostBody);
     clientWriter.flush();
 
     final String response1 = readResponse(clientSocket, "dah$");
 
     AssertUtilities.assertStartsWith(response1,
                                      "POST /blah?x=123&y=99 HTTP/1.0\r\n");
     AssertUtilities.assertContainsPattern(response1,
                                           "\r\n\r\nAnother message" +
                                           message1PostBody + "$");
 
     // Do again, but force engine to handle body in two parts.
     clientWriter.print(message1Headers);
     clientWriter.flush();
 
     final String response2a = readResponse(clientSocket, "Another message$");
 
     AssertUtilities.assertStartsWith(response2a,
                                      "POST /blah?x=123&y=99 HTTP/1.0\r\n");
 
     clientWriter.print(message1PostBody);
     clientWriter.flush();
 
     final String response2b = readResponse(clientSocket, "dah$");
     assertNotNull(response2b);
 
     clientSocket.close();
 
     waitUntilAllStreamThreadsStopped(engine);
 
     assertTrue(m_requestFilterStubFactory.getCallHistory().indexOf(
                "connectionClosed") != -1 &&
                m_responseFilterStubFactory.getCallHistory().indexOf(
                "connectionClosed") != -1);
 
     m_requestFilterStubFactory.assertSuccess("handle",
                                              ConnectionDetails.class,
                                              new byte[0].getClass(),
                                              Integer.class);
     m_requestFilterStubFactory.assertSuccess("handle",
                                              ConnectionDetails.class,
                                              new byte[0].getClass(),
                                              Integer.class);
     m_requestFilterStubFactory.assertSuccess("handle",
                                              ConnectionDetails.class,
                                              new byte[0].getClass(),
                                              Integer.class);
 
     // handle() must have been called at least 3 times, but can be called
     // more.
     while (m_requestFilterStubFactory.peekFirst() != null &&
            m_requestFilterStubFactory.peekFirst().getMethod().getName()
            .equals("handle")) {
       m_requestFilterStubFactory.assertSuccess("handle",
                                                ConnectionDetails.class,
                                                new byte[0].getClass(),
                                                Integer.class);
     }
 
     m_responseFilterStubFactory.assertSuccess("handle",
                                               ConnectionDetails.class,
                                               new byte[0].getClass(),
                                               Integer.class);
     m_responseFilterStubFactory.assertSuccess("handle",
                                               ConnectionDetails.class,
                                               new byte[0].getClass(),
                                               Integer.class);
     m_responseFilterStubFactory.assertSuccess("handle",
                                               ConnectionDetails.class,
                                               new byte[0].getClass(),
                                               Integer.class);
 
     while (m_responseFilterStubFactory.peekFirst() != null &&
            m_responseFilterStubFactory.peekFirst().getMethod().getName()
            .equals("handle")) {
       m_responseFilterStubFactory.assertSuccess("handle",
                                                 ConnectionDetails.class,
                                                 new byte[0].getClass(),
                                                 Integer.class);
     }
 
     m_requestFilterStubFactory.assertSuccess("connectionClosed",
                                              ConnectionDetails.class);
     m_requestFilterStubFactory.assertNoMoreCalls();
 
     m_responseFilterStubFactory.assertSuccess("connectionClosed",
                                               ConnectionDetails.class);
     m_responseFilterStubFactory.assertNoMoreCalls();
 
     m_loggerStubFactory.assertNoMoreCalls();
   }
 
   private void httpsProxyEngineGoodRequestTest(AbstractTCPProxyEngine engine)
     throws Exception {
 
     final AcceptAndEcho echoer = new SSLAcceptAndEcho();
 
     final Socket clientPlainSocket =
       new Socket(engine.getListenEndPoint().getHost(),
                  engine.getListenEndPoint().getPort());
 
     final PrintWriter clientWriter =
       new PrintWriter(clientPlainSocket.getOutputStream(), true);
     clientWriter.print("CONNECT " + echoer.getEndPoint() + "\r\n\r\n");
     clientWriter.flush();
 
     final String response = readResponse(clientPlainSocket, "Proxy-agent");
 
     AssertUtilities.assertStartsWith(response, "HTTP/1.0 200 OK\r\n");
     AssertUtilities.assertContainsHeader(response,
                                          "Proxy-agent",
                                          "The Grinder.*");
 
     final Socket clientSSLSocket =
       m_sslSocketFactory.createClientSocket(clientPlainSocket,
                                             echoer.getEndPoint());
 
     final PrintWriter secureClientWriter =
       new PrintWriter(clientSSLSocket.getOutputStream(), true);
 
     // No URL decoration should take place. Feed an absolute URL
     // to be difficult.
     final String message0 =
       "GET http://galafray/foo HTTP/1.1\r\n" +
       "foo: bah\r\n" +
       "\r\n" +
       "A \u00e0 message";
     secureClientWriter.print(message0);
     secureClientWriter.flush();
 
     final String response0 = readResponse(clientSSLSocket, "message$");
 
     AssertUtilities.assertStartsWith(response0,
                                      "GET http://galafray/foo HTTP/1.1\r\n");
     AssertUtilities.assertContainsHeader(response0, "foo", "bah");
     AssertUtilities.assertContainsPattern(response0,
                                           "\r\n\r\nA \u00e0 message$");
 
     m_requestFilterStubFactory.assertSuccess("connectionOpened",
                                              ConnectionDetails.class);
     m_requestFilterStubFactory.assertSuccess("handle",
                                              ConnectionDetails.class,
                                              new byte[0].getClass(),
                                              Integer.class);
     m_requestFilterStubFactory.assertNoMoreCalls();
 
     m_responseFilterStubFactory.assertSuccess("connectionOpened",
                                               ConnectionDetails.class);
     m_responseFilterStubFactory.assertSuccess("handle",
                                               ConnectionDetails.class,
                                               new byte[0].getClass(),
                                               Integer.class);
     m_responseFilterStubFactory.assertNoMoreCalls();
 
     clientSSLSocket.close();
 
     waitUntilAllStreamThreadsStopped(engine);
 
     m_requestFilterStubFactory.waitUntilCalled(5000);
 
     m_requestFilterStubFactory.assertSuccess("connectionClosed",
                                              ConnectionDetails.class);
     m_requestFilterStubFactory.assertNoMoreCalls();
 
     m_responseFilterStubFactory.waitUntilCalled(5000);
 
     m_responseFilterStubFactory.assertSuccess("connectionClosed",
                                               ConnectionDetails.class);
     m_responseFilterStubFactory.assertNoMoreCalls();
 
     m_loggerStubFactory.assertNoMoreCalls();
   }
 
   public void testHTTPProxyEngine() throws Exception {

     final AbstractTCPProxyEngine engine =
       new HTTPProxyTCPProxyEngine(m_sslSocketFactory,
                                   m_requestFilter,
                                   m_responseFilter,
                                   m_logger,
                                   m_localEndPoint,
                                   false,
                                   100000,
                                   null,
                                   null);
 
     final Thread engineThread = new Thread(engine, "Run engine");
     engineThread.start();
 
     m_responseFilterStubFactory.assertNoMoreCalls();
     m_requestFilterStubFactory.assertNoMoreCalls();
 
     assertEquals(m_localEndPoint, engine.getListenEndPoint());
     assertNotNull(engine.getSocketFactory());
     m_requestFilterStubFactory.assertIsWrappedBy(engine.getRequestFilter());
     m_responseFilterStubFactory.assertIsWrappedBy(engine.getResponseFilter());
     assertEquals("", engine.getRequestColour());
     assertEquals("", engine.getResponseColour());
 
     m_loggerStubFactory.resetCallHistory();
     m_requestFilterStubFactory.resetCallHistory();
     m_responseFilterStubFactory.resetCallHistory();
 
     httpProxyEngineBadRequestTests(engine);
     httpProxyEngineGoodRequestTests(engine);
     httpsProxyEngineGoodRequestTest(engine);
 
     engine.stop();
     engineThread.join();
 
     // Stopping engine again doesn't do anything.
     engine.stop();
 
     m_requestFilterStubFactory.assertNoMoreCalls();
     m_responseFilterStubFactory.assertNoMoreCalls();
   }
 
   public void testColourHTTPProxyEngine() throws Exception {
 
     final AbstractTCPProxyEngine engine =
       new HTTPProxyTCPProxyEngine(m_sslSocketFactory,
                                   m_requestFilter,
                                   m_responseFilter,
                                   m_logger,
                                   m_localEndPoint,
                                   true,
                                   100000,
                                   null,
                                   null);
 
     final Thread engineThread = new Thread(engine, "Run engine");
     engineThread.start();
 
     m_responseFilterStubFactory.assertNoMoreCalls();
     m_requestFilterStubFactory.assertNoMoreCalls();
 
     assertEquals(m_localEndPoint, engine.getListenEndPoint());
     assertNotNull(engine.getSocketFactory());
     m_requestFilterStubFactory.assertIsWrappedBy(engine.getRequestFilter());
     m_responseFilterStubFactory.assertIsWrappedBy(engine.getResponseFilter());
     assertEquals(TerminalColour.RED, engine.getRequestColour());
     assertEquals(TerminalColour.BLUE, engine.getResponseColour());
 
     m_loggerStubFactory.resetCallHistory();
     m_requestFilterStubFactory.resetCallHistory();
     m_responseFilterStubFactory.resetCallHistory();
 
     httpProxyEngineBadRequestTests(engine);
     httpProxyEngineGoodRequestTests(engine);
 
     engine.stop();
     engineThread.join();
 
     // Stopping engine again doesn't do anything.
     engine.stop();
 
     m_requestFilterStubFactory.assertNoMoreCalls();
     m_responseFilterStubFactory.assertNoMoreCalls();
   }
 
   public void testWithChainedHTTPProxy() throws Exception {
     final AcceptAndEcho echoer = new AcceptAndEcho();
 
     final EndPoint chainedProxyEndPoint = createFreeLocalEndPoint();
 
     final AbstractTCPProxyEngine chainedProxy =
       new HTTPProxyTCPProxyEngine(m_sslSocketFactory,
                                   m_requestFilter,
                                   m_responseFilter,
                                   m_logger,
                                   chainedProxyEndPoint,
                                   true,
                                   100000,
                                   null,
                                   null);
 
     final Thread chainedProxyThread =
       new Thread(chainedProxy, "Run chained proxy engine");
     chainedProxyThread.start();
 
     final AbstractTCPProxyEngine engine =
       new HTTPProxyTCPProxyEngine(m_sslSocketFactory,
                                   new NullFilter(),
                                   new NullFilter(),
                                   m_logger,
                                   m_localEndPoint,
                                   true,
                                   100000,
                                   chainedProxyEndPoint,
                                   null);
     final Thread engineThread = new Thread(engine, "Run engine");
     engineThread.start();
 
     m_loggerStubFactory.resetCallHistory();
     m_requestFilterStubFactory.resetCallHistory();
     m_responseFilterStubFactory.resetCallHistory();
 
     final Socket clientSocket =
       new Socket(engine.getListenEndPoint().getHost(),
                  engine.getListenEndPoint().getPort());
 
     final PrintWriter clientWriter =
       new PrintWriter(clientSocket.getOutputStream(), true);
 
     final String message0 =
       "GET http://" + echoer.getEndPoint() + "/ HTTP/1.1\r\n" +
       "foo: bah\r\n" +
       "\r\n" +
       "Proxy me";
     clientWriter.print(message0);
     clientWriter.flush();
 
     final String response0 = readResponse(clientSocket, "Proxy me$");
 
     AssertUtilities.assertStartsWith(response0, "GET / HTTP/1.1\r\n");
     AssertUtilities.assertContainsHeader(response0, "foo", "bah");
     AssertUtilities.assertContainsPattern(response0, "\r\n\r\nProxy me$");
 
     m_requestFilterStubFactory.assertSuccess("connectionOpened",
                                              ConnectionDetails.class);
     m_requestFilterStubFactory.assertSuccess("handle",
                                              ConnectionDetails.class,
                                              new byte[0].getClass(),
                                              Integer.class);
     m_requestFilterStubFactory.assertNoMoreCalls();
 
     m_responseFilterStubFactory.assertSuccess("connectionOpened",
                                               ConnectionDetails.class);
     m_responseFilterStubFactory.assertSuccess("handle",
                                               ConnectionDetails.class,
                                               new byte[0].getClass(),
                                               Integer.class);
     m_responseFilterStubFactory.assertNoMoreCalls();
 
     chainedProxy.stop();
     chainedProxyThread.join();
 
     engine.stop();
     engineThread.join();
 
     waitUntilAllStreamThreadsStopped(engine);
 
     m_requestFilterStubFactory.assertSuccess(
       "connectionClosed", ConnectionDetails.class);
     m_responseFilterStubFactory.assertSuccess(
       "connectionClosed", ConnectionDetails.class);
 
     m_loggerStubFactory.assertNoMoreCalls();
 
     // Stopping engine again doesn't do anything.
     engine.stop();
 
     m_requestFilterStubFactory.assertNoMoreCalls();
     m_responseFilterStubFactory.assertNoMoreCalls();
   }
 
   public void testWithChainedHTTPSProxy() throws Exception {
     final AcceptAndEcho echoer = new SSLAcceptAndEcho();
 
     final EndPoint chainedProxyEndPoint = createFreeLocalEndPoint();
 
     final AbstractTCPProxyEngine chainedProxy =
       new HTTPProxyTCPProxyEngine(m_sslSocketFactory,
                                   m_requestFilter,
                                   m_responseFilter,
                                   m_logger,
                                   chainedProxyEndPoint,
                                   true,
                                   100000,
                                   null,
                                   null);
 
     final Thread chainedProxyThread =
       new Thread(chainedProxy, "Run chained proxy engine");
     chainedProxyThread.start();
 
     final AbstractTCPProxyEngine engine =
       new HTTPProxyTCPProxyEngine(m_sslSocketFactory,
                                   new NullFilter(),
                                   new NullFilter(),
                                   m_logger,
                                   m_localEndPoint,
                                   true,
                                   100000,
                                   null,
                                   chainedProxyEndPoint);
 
     final Thread engineThread = new Thread(engine, "Run engine");
     engineThread.start();
 
     m_loggerStubFactory.resetCallHistory();
     m_requestFilterStubFactory.resetCallHistory();
     m_responseFilterStubFactory.resetCallHistory();
 
 
     final Socket clientPlainSocket =
       new Socket(engine.getListenEndPoint().getHost(),
                  engine.getListenEndPoint().getPort());
 
     final PrintWriter clientWriter =
       new PrintWriter(clientPlainSocket.getOutputStream(), true);
     clientWriter.print("CONNECT " + echoer.getEndPoint() + "\r\n\r\n");
     clientWriter.flush();
 
     final String response = readResponse(clientPlainSocket, "Proxy-agent");
 
     AssertUtilities.assertStartsWith(response, "HTTP/1.0 200 OK\r\n");
     AssertUtilities.assertContainsHeader(response,
                                          "Proxy-agent",
                                          "The Grinder.*");
 
     final Socket clientSSLSocket =
       m_sslSocketFactory.createClientSocket(clientPlainSocket,
                                             echoer.getEndPoint());
 
     final PrintWriter secureClientWriter =
       new PrintWriter(clientSSLSocket.getOutputStream(), true);
 
     // No URL decoration should take place. Feed an absolute URL
     // to be difficult.
     final String message0 =
       "GET http://galafray/foo HTTP/1.1\r\n" +
       "foo: bah\r\n" +
       "\r\n" +
       "A \u00e0 message";
     secureClientWriter.print(message0);
     secureClientWriter.flush();
 
     final String response0 = readResponse(clientSSLSocket, "message$");
 
     AssertUtilities.assertStartsWith(response0,
                                      "GET http://galafray/foo HTTP/1.1\r\n");
     AssertUtilities.assertContainsHeader(response0, "foo", "bah");
     AssertUtilities.assertContainsPattern(response0,
                                           "\r\n\r\nA \u00e0 message$");
 
     m_requestFilterStubFactory.assertSuccess("connectionOpened",
                                              ConnectionDetails.class);
     m_requestFilterStubFactory.assertSuccess("handle",
                                              ConnectionDetails.class,
                                              new byte[0].getClass(),
                                              Integer.class);
     m_requestFilterStubFactory.assertNoMoreCalls();
 
     m_responseFilterStubFactory.assertSuccess("connectionOpened",
                                               ConnectionDetails.class);
     m_responseFilterStubFactory.assertSuccess("handle",
                                               ConnectionDetails.class,
                                               new byte[0].getClass(),
                                               Integer.class);
     m_responseFilterStubFactory.assertNoMoreCalls();
 
     m_loggerStubFactory.assertNoMoreCalls();
 
     engine.stop();
     engineThread.join();
 
     chainedProxy.stop();
     chainedProxyThread.join();
 
     waitUntilAllStreamThreadsStopped(engine);
 
     m_requestFilterStubFactory.setIgnoreCallOrder(true);
     m_responseFilterStubFactory.setIgnoreCallOrder(true);
 
     m_requestFilterStubFactory.assertSuccess(
       "connectionClosed", ConnectionDetails.class);
     m_responseFilterStubFactory.assertSuccess(
       "connectionClosed", ConnectionDetails.class);
 
     // Sometimes log an SSL exception when shutting down.
     // m_loggerStubFactory.assertNoMoreCalls();
 
     // Stopping engine or filter again doesn't do anything.
     engine.stop();
 
     m_requestFilterStubFactory.assertNoMoreCalls();
     m_responseFilterStubFactory.assertNoMoreCalls();
   }
 
 
   public void testStopWithBlockedFilterThreads() throws Exception {
 
     final AcceptAndEcho echoer = new AcceptAndEcho();
 
     // I wanted to implement this test so that a filter thread blocked
     // on some hung connection. However, it's hard to simulate a connection
     // timeout in a single JVM; I thought it could be done by connected to a
     // socket that never accepted but the client side of the connection was
     // established just fine. Instead, we use an evil filter that blocks
     // when it recevies the connection opened event.
 
     final AbstractTCPProxyEngine engine =
       new HTTPProxyTCPProxyEngine(m_sslSocketFactory,
                                   new HungFilter(),
                                   m_responseFilter,
                                   m_logger,
                                   m_localEndPoint,
                                   false,
                                   100000,
                                   null,
                                   null);
 
     final Thread engineThread = new Thread(engine, "Run engine");
     engineThread.start();
 
     final ServerSocket serverSocket = new ServerSocket(0);
 
     final Socket clientSocket =
       new Socket(engine.getListenEndPoint().getHost(),
                  engine.getListenEndPoint().getPort());
 
     final PrintWriter clientWriter =
       new PrintWriter(clientSocket.getOutputStream(), true);
 
     final String message0 =
       "GET http://" + echoer.getEndPoint() + "/foo HTTP/1.1\r\n" +
       "foo: bah\r\n" +
       "\r\n" +
       "A \u00e0 message";
     clientWriter.print(message0);
     clientWriter.flush();
 
     // Wait until the filter thread is spinning so that there's
     // a good chancce it's hung.
     for (int i = 0;
          i < 10 && engine.getStreamThreadGroup().activeCount() != 1;
          ++i) {
       Thread.sleep(50);
     }
 
     assertEquals(1, engine.getStreamThreadGroup().activeCount());
 
     engine.stop();
     engineThread.join();
 
     serverSocket.close();
 
     waitUntilAllStreamThreadsStopped(engine);
   }
 
   private static class HungFilter implements TCPProxyFilter {
 
     public void connectionClosed(ConnectionDetails connectionDetails)
       throws FilterException {
     }
 
     public void connectionOpened(ConnectionDetails connectionDetails)
       throws FilterException {
 
       try {
         synchronized (this) {
           wait();
         }
       }
       catch (InterruptedException e) {
         throw new UncheckedInterruptedException(e);
       }
     }
 
     public byte[] handle(ConnectionDetails connectionDetails,
                          byte[] buffer,
                          int bytesRead) throws FilterException {
       return null;
     }
   }
 
 
   private class AcceptAndEcho implements Runnable {
     private final ServerSocket m_serverSocket;
 
     public AcceptAndEcho() throws IOException {
       this(new ServerSocket(0));
     }
 
     protected AcceptAndEcho(ServerSocket serverSocket) throws IOException {
       m_serverSocket = serverSocket;
       new Thread(this, getClass().getName()).start();
       m_echoers.add(this);
     }
 
     public EndPoint getEndPoint() {
       return EndPoint.serverEndPoint(m_serverSocket);
     }
 
     public void run() {
       try {
         while (true) {
           final Socket socket = m_serverSocket.accept();
 
           new Thread(
             new StreamCopier(1000, true).getRunnable(socket.getInputStream(),
                                                      socket.getOutputStream()),
             "Echo thread").start();
         }
       }
       catch (SocketException e) {
         // Ignore - probably shutdown.
       }
       catch (IOException e) {
         fail("Got a " + e.getClass());
       }
     }
 
     public void shutdown() throws IOException {
       m_serverSocket.close();
     }
   }
 
   private class SSLAcceptAndEcho extends AcceptAndEcho {
     public SSLAcceptAndEcho() throws IOException {
       super(
         m_sslSocketFactory.createServerSocket(createFreeLocalEndPoint(), 0));
     }
   }
 }
