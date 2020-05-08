 // Copyright (C) 2000 Phil Dawes
 // Copyright (C) 2000, 2001, 2002, 2003, 2004 Philip Aston
 // Copyright (C) 2001 Paddy Spencer
 // Copyright (C) 2003, 2004, 2005 Bertrand Ave
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
 
 package net.grinder;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 import net.grinder.common.GrinderException;
 import net.grinder.common.Logger;
 import net.grinder.plugin.http.HTTPPluginTCPProxyFilter;
 import net.grinder.plugin.http.HTTPPluginTCPProxyFilter2;
 import net.grinder.plugin.http.HTTPPluginTCPProxyResponseFilter;
 import net.grinder.plugin.http.HTTPPluginTCPProxyResponseFilter2;
 import net.grinder.tools.tcpproxy.CompositeFilter;
 import net.grinder.tools.tcpproxy.ConnectionDetails;
 import net.grinder.tools.tcpproxy.EchoFilter;
 import net.grinder.tools.tcpproxy.EndPoint;
 import net.grinder.tools.tcpproxy.HTTPProxyTCPProxyEngine;
 import net.grinder.tools.tcpproxy.NullFilter;
 import net.grinder.tools.tcpproxy.PortForwarderTCPProxyEngine;
 import net.grinder.tools.tcpproxy.TCPProxyConsole;
 import net.grinder.tools.tcpproxy.TCPProxyEngine;
 import net.grinder.tools.tcpproxy.TCPProxyFilter;
 import net.grinder.tools.tcpproxy.TCPProxySSLSocketFactory;
 import net.grinder.tools.tcpproxy.TCPProxySSLSocketFactoryImplementation;
 import net.grinder.util.JVM;
 import net.grinder.util.SimpleLogger;
 
 
 /**
  * This is the entry point of The TCPProxy process.
  *
  * @author Phil Dawes
  * @author Philip Aston
  * @author Bertrand Ave
  * @version $Revision$
  */
 public final class TCPProxy {
 
   /**
    * Entry point.
    *
    * @param args Command line arguments.
    * @throws GrinderException on a unexpected fatal error.
    */
   public static void main(String[] args) throws GrinderException {
     final Logger logger = new SimpleLogger("tcpproxy",
                                            new PrintWriter(System.out),
                                            new PrintWriter(System.err));
 
     if (!JVM.getInstance().haveRequisites(logger)) {
       return;
     }
 
     try {
       final TCPProxy tcpProxy = new TCPProxy(args, logger);
       tcpProxy.run();
     }
     catch (LoggedInitialisationException e) {
       System.exit(1);
     }
     catch (Throwable e) {
       logger.error("Could not initialise engine:");
      e.printStackTrace(logger.getErrorLogWriter());
       System.exit(2);
     }
 
     logger.output("Engine exited");
     System.exit(0);
   }
 
   private LoggedInitialisationException barfError(String message) {
     m_logger.error("Error: " + message);
     return new LoggedInitialisationException(message);
   }
 
   private LoggedInitialisationException barfUsage() {
     return barfError(
       "unrecognised or invalid option." +
       "\n\n" +
       "Usage: " +
       "\n java " + TCPProxy.class + " <options>" +
       "\n" +
       "\n Commonly used options:" +
       "\n   [-requestfilter <filter>]    Add a request filter." +
       "\n   [-responsefilter <filter>]   Add a response filter." +
       "\n   [-httpplugin]                See below." +
       "\n   [-properties <file>]         Properties to pass to the filters." +
       "\n   [-localhost <host name/ip>]  Default is localhost." +
       "\n   [-localport <port>]          Default is 8001." +
       "\n   [-keystore <file>]           Key store details for" +
       "\n   [-keystorepassword <pass>]   SSL certificates." +
       "\n   [-keystoretype <type>]       Default is JSSE dependent." +
       "\n" +
       "\n Other options:" +
       "\n   [-remotehost <host name>]    Default is localhost." +
       "\n   [-remoteport <port>]         Default is 7001." +
       "\n   [-timeout <seconds>]         Proxy engine timeout." +
       "\n   [-colour]                    Be pretty on ANSI terminals." +
       "\n   [-console]                   Display the console." +
       "\n   [-httpproxy <host> <port>]   Route via HTTP/HTTPS proxy." +
       "\n   [-httpsproxy <host> <port>]  Override -httpproxy settings for" +
       "\n                                HTTPS." +
       "\n   [-ssl]                       Use SSL when port forwarding." +
       "\n" +
       "\n <filter> can be the name of a class that implements " +
       TCPProxyFilter.class.getName() + " or one of NONE, ECHO. The default " +
       "is ECHO. Multiple filters can be specified for each stream." +
       "\n\n" +
       "If neither -remotehost nor -remoteport is specified, the " +
       "TCPProxy listens as an HTTP/HTTPS Proxy on <localhost:localport>." +
       "\n\n" +
       "If either -remotehost or -remoteport is specified, the TCPProxy " +
       "acts a simple port forwarder between <localhost:localport> and " +
       "<remotehost:remoteport>. Specify -ssl for SSL support." +
       "\n\n" +
       "-httpplugin sets the request and response filters to produce a " +
       "test script suitable for use with the HTTP plugin. New versions " +
       "of these filters are currently under development; use " +
       "-newhttpplugin to try these out." +
       "\n\n" +
       "-timeout is how long the TCPProxy will wait for a request " +
       "before timing out and freeing the local port. The TCPProxy will " +
       "not time out if there are active connections." +
       "\n\n" +
       "-console displays a simple console that allows the TCPProxy " +
       "to be shutdown cleanly." +
       "\n\n" +
       "-httpproxy and -httpsproxy allow output to be directed through " +
       "another HTTP/HTTPS proxy; this may help you reach the Internet. " +
       "These options are not supported in port forwarding mode." +
       "\n"
       );
   }
 
   private final TCPProxyEngine m_proxyEngine;
   private final Logger m_logger;
 
   private TCPProxy(String[] args, Logger logger) throws Exception {
     m_logger = logger;
     final PrintWriter outputWriter = m_logger.getOutputLogWriter();
 
     // Default values.
     TCPProxyFilter requestFilter = new EchoFilter(outputWriter);
     TCPProxyFilter responseFilter = new EchoFilter(outputWriter);
     int localPort = 8001;
     String remoteHost = "localhost";
     String localHost = "localhost";
     int remotePort = 7001;
     boolean useSSLPortForwarding = false;
     File keyStoreFile = null;
     char[] keyStorePassword = null;
     String keyStoreType = null;
     boolean isHTTPProxy = true;
     boolean console = false;
     EndPoint chainedHTTPProxy = null;
     EndPoint chainedHTTPSProxy = null;
 
     int timeout = 0;
 
     boolean useColour = false;
 
     try {
       // Parse 1.
       for (int i = 0; i < args.length; i++) {
         if (args[i].equalsIgnoreCase("-properties")) {
           final Properties properties = new Properties();
           properties.load(new FileInputStream(new File(args[++i])));
           System.getProperties().putAll(properties);
         }
         else if (args[i].equalsIgnoreCase("-initialtest")) {
           final String argument = i + 1 < args.length ? args[++i] : "123";
 
           throw barfError("-initialTest is no longer supported. " +
                           "Use -DHTTPPlugin.initialTest=" + argument +
                           " or the -properties option instead.");
         }
       }
 
       // Parse 2.
       for (int i = 0; i < args.length; i++) {
         if (args[i].equalsIgnoreCase("-requestfilter")) {
           requestFilter =
             addFilter(requestFilter,
                       instantiateFilter(args[++i], outputWriter));
         }
         else if (args[i].equalsIgnoreCase("-responsefilter")) {
           responseFilter =
             addFilter(responseFilter,
                       instantiateFilter(args[++i], outputWriter));
         }
         else if (args[i].equalsIgnoreCase("-httpplugin")) {
           requestFilter =
             addFilter(requestFilter,
                       new HTTPPluginTCPProxyFilter(outputWriter));
 
           responseFilter =
             addFilter(responseFilter,
                       new HTTPPluginTCPProxyResponseFilter(outputWriter));
         }
         else if (args[i].equalsIgnoreCase("-newhttpplugin")) {
           requestFilter =
             addFilter(requestFilter,
                       new HTTPPluginTCPProxyFilter2(outputWriter));
 
           responseFilter =
             addFilter(responseFilter,
                       new HTTPPluginTCPProxyResponseFilter2(outputWriter));
         }
         else if (args[i].equalsIgnoreCase("-localhost")) {
           localHost = args[++i];
         }
         else if (args[i].equalsIgnoreCase("-localport")) {
           localPort = Integer.parseInt(args[++i]);
         }
         else if (args[i].equalsIgnoreCase("-remotehost")) {
           remoteHost = args[++i];
           isHTTPProxy = false;
         }
         else if (args[i].equalsIgnoreCase("-remoteport")) {
           remotePort = Integer.parseInt(args[++i]);
           isHTTPProxy = false;
         }
         else if (args[i].equalsIgnoreCase("-ssl")) {
           useSSLPortForwarding = true;
         }
         else if (args[i].equalsIgnoreCase("-keystore")) {
           keyStoreFile = new File(args[++i]);
         }
         else if (args[i].equalsIgnoreCase("-keystorepassword") ||
                  args[i].equalsIgnoreCase("-storepass")) {
           keyStorePassword = args[++i].toCharArray();
         }
         else if (args[i].equalsIgnoreCase("-keystoretype") ||
                  args[i].equalsIgnoreCase("-storetype")) {
           keyStoreType = args[++i];
         }
         else if (args[i].equalsIgnoreCase("-timeout")) {
           timeout = Integer.parseInt(args[++i]) * 1000;
         }
         else if (args[i].equalsIgnoreCase("-output")) {
           // -output is used by the TCPProxy web app only
           // and is not publicised, users are expected to
           // use shell redirection.
           final String outputFile = args[++i];
           System.setOut(new PrintStream(
                           new FileOutputStream(outputFile + ".out"), true));
           System.setErr(new PrintStream(
                           new FileOutputStream(outputFile + ".err"), true));
         }
         else if (args[i].equalsIgnoreCase("-console")) {
           console = true;
         }
         else if (args[i].equalsIgnoreCase("-colour") ||
                  args[i].equalsIgnoreCase("-color")) {
           useColour = true;
         }
         else if (args[i].equalsIgnoreCase("-properties")) {
           /* Already handled */
           ++i;
         }
         else if (args[i].equalsIgnoreCase("-httpproxy")) {
           chainedHTTPProxy =
             new EndPoint(args[++i], Integer.parseInt(args[++i]));
         }
         else if (args[i].equalsIgnoreCase("-httpsproxy")) {
           chainedHTTPSProxy =
             new EndPoint(args[++i], Integer.parseInt(args[++i]));
         }
         else {
           throw barfUsage();
         }
       }
     }
     catch (FileNotFoundException fnfe) {
         throw barfError(fnfe.getMessage());
     }
     catch (IndexOutOfBoundsException e) {
       throw barfUsage();
     }
     catch (NumberFormatException e) {
       throw barfUsage();
     }
 
     if (timeout < 0) {
       throw barfError("timeout must be non-negative.");
     }
 
     final EndPoint localEndPoint = new EndPoint(localHost, localPort);
     final EndPoint remoteEndPoint = new EndPoint(remoteHost, remotePort);
 
     if (chainedHTTPSProxy == null && chainedHTTPProxy != null) {
       chainedHTTPSProxy = chainedHTTPProxy;
     }
 
     if (chainedHTTPSProxy != null && !isHTTPProxy) {
       throw barfError("routing through a HTTP/HTTPS proxy is not supported " +
                       "in port forwarding mode.");
     }
 
     final StringBuffer startMessage = new StringBuffer();
 
     startMessage.append("Initialising as ");
 
     if (isHTTPProxy) {
       startMessage.append("an HTTP/HTTPS proxy");
     }
     else {
       if (useSSLPortForwarding) {
         startMessage.append("an SSL port forwarder");
       }
       else {
         startMessage.append("a TCP port forwarder");
       }
     }
 
     startMessage.append(" with the parameters:");
     startMessage.append("\n   Request filters:    ");
     appendFilterList(startMessage, requestFilter);
     startMessage.append("\n   Response filters:   ");
     appendFilterList(startMessage, responseFilter);
     startMessage.append("\n   Local address:      " + localEndPoint);
 
     if (!isHTTPProxy) {
       startMessage.append("\n   Remote address:     " + remoteEndPoint);
     }
 
     if (chainedHTTPProxy != null) {
       startMessage.append("\n   HTTP proxy:         " + chainedHTTPProxy);
     }
 
     if (chainedHTTPSProxy != null) {
       startMessage.append("\n   HTTPS proxy:        " + chainedHTTPSProxy);
     }
 
     if (keyStoreFile != null) {
       startMessage.append("\n   Key store:          ");
       startMessage.append(keyStoreFile.toString());
 
       // Key store password is optional.
       if (keyStorePassword != null) {
         startMessage.append("\n   Key store password: ");
         for (int i = 0; i < keyStorePassword.length; ++i) {
           startMessage.append('*');
         }
       }
 
       // Key store type can be null => use whatever
       // KeyStore.getDefaultType() says (we can't print the default
       // here without loading the JSSE).
       if (keyStoreType != null) {
         startMessage.append("\n   Key store type:     " + keyStoreType);
       }
     }
 
     m_logger.error(startMessage.toString());
 
     final TCPProxySSLSocketFactory sslSocketFactory =
       keyStoreFile != null ?
       new TCPProxySSLSocketFactoryImplementation(keyStoreFile,
                                                  keyStorePassword,
                                                  keyStoreType) :
       new TCPProxySSLSocketFactoryImplementation();
 
     if (isHTTPProxy) {
       m_proxyEngine =
         new HTTPProxyTCPProxyEngine(
           sslSocketFactory,
           requestFilter, responseFilter,
           logger,
           localEndPoint,
           useColour,
           timeout,
           chainedHTTPProxy, chainedHTTPSProxy);
     }
     else {
       if (useSSLPortForwarding) {
         m_proxyEngine =
           new PortForwarderTCPProxyEngine(
             sslSocketFactory,
             requestFilter, responseFilter,
             logger,
             new ConnectionDetails(localEndPoint, remoteEndPoint, true),
             useColour,
             timeout);
       }
       else {
         m_proxyEngine =
           new PortForwarderTCPProxyEngine(
             requestFilter, responseFilter,
             logger,
             new ConnectionDetails(localEndPoint, remoteEndPoint, false),
             useColour,
             timeout);
       }
     }
 
     if (console) {
       new TCPProxyConsole(m_proxyEngine);
     }
 
     m_logger.error("Engine initialised, listening on port " + localPort);
   }
 
   private TCPProxyFilter instantiateFilter(
     String filterClassName, PrintWriter outputWriter) throws Exception {
 
     if (filterClassName.equals("NONE")) {
       return new NullFilter(outputWriter);
     }
     else if (filterClassName.equals("ECHO")) {
       return new EchoFilter(outputWriter);
     }
 
     final Class filterClass;
 
     try {
       filterClass = Class.forName(filterClassName);
     }
     catch (ClassNotFoundException e) {
       throw barfError("class '" + filterClassName + "' not found.");
     }
 
     if (!TCPProxyFilter.class.isAssignableFrom(filterClass)) {
       throw barfError("the class '" + filterClass.getName() +
                       "' does not implement the interface: '" +
                       TCPProxyFilter.class.getName() + "'.");
     }
 
     // Instantiate a filter.
     try {
       final Constructor constructor =
         filterClass.getConstructor(new Class[] {PrintWriter.class});
 
       return (TCPProxyFilter)constructor.newInstance(
         new Object[] {outputWriter});
     }
     catch (NoSuchMethodException e) {
       throw barfError("the class '" + filterClass.getName() + "' does not " +
                       "have a constructor that takes a PrintWriter.");
     }
     catch (IllegalAccessException e) {
       throw barfError("the constructor of class '" + filterClass.getName() +
                       "' is not public.");
     }
     catch (InstantiationException e) {
       throw barfError("the class '" + filterClass.getName() +
                       "' is abstract.");
     }
   }
 
   private TCPProxyFilter addFilter(TCPProxyFilter existingFilter,
                                    TCPProxyFilter newFilter) {
 
     if (existingFilter instanceof CompositeFilter) {
       ((CompositeFilter) existingFilter).add(newFilter);
       return existingFilter;
     }
     else {
       // Discard the default filter.
       final CompositeFilter result = new CompositeFilter();
       result.add(newFilter);
       return result;
     }
   }
 
   private void appendFilterList(StringBuffer buffer, TCPProxyFilter filter) {
 
     final TCPProxyFilter[] filters =
       (TCPProxyFilter[]) getFilterList(filter).toArray(new TCPProxyFilter[0]);
 
     for (int i = 0; i < filters.length; ++i) {
       if (i != 0) {
         buffer.append(", ");
       }
 
       final String fullName = filters[i].getClass().getName();
       final int lastDot = fullName.lastIndexOf(".");
       final String shortName =
         lastDot > 0 ? fullName.substring(lastDot + 1) : fullName;
 
       buffer.append(shortName);
     }
   }
 
   private List getFilterList(TCPProxyFilter filter) {
 
     if (filter instanceof CompositeFilter) {
 
       final List result = new ArrayList();
 
       final Iterator iterator  =
         ((CompositeFilter)filter).getFilters().iterator();
 
       while (iterator.hasNext()) {
         result.addAll(getFilterList((TCPProxyFilter) iterator.next()));
       }
 
       return result;
     }
     else {
       return Collections.singletonList(filter);
     }
   }
 
   private void run() {
     Runtime.getRuntime().addShutdownHook(
       new Thread() {
         public void run() { m_proxyEngine.stop(); }
       });
 
     m_proxyEngine.run();
   }
 
   private class LoggedInitialisationException extends GrinderException {
     public LoggedInitialisationException(String message) {
       super(message);
     }
   }
 }
