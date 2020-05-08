 /*
  * Copyright (c) 2012 Helsinki Institute of Physics All rights reserved.
  * See LICENCE file for licensing information.
  */
 
 package eu.emi.security.canl.axis2;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.net.UnknownHostException;
 import java.security.KeyStoreException;
 import java.security.PrivateKey;
 import java.security.cert.CertificateException;
 import java.util.ArrayList;
 import java.util.Properties;
 
 import javax.net.ssl.SSLSocket;
 import javax.net.ssl.SSLSocketFactory;
 
 import org.apache.commons.httpclient.params.HttpConnectionParams;
 import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
 import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
 
 import eu.emi.security.authn.x509.CrlCheckingMode;
 import eu.emi.security.authn.x509.NamespaceCheckingMode;
 import eu.emi.security.authn.x509.OCSPParametes;
 import eu.emi.security.authn.x509.ProxySupport;
 import eu.emi.security.authn.x509.RevocationParameters;
 import eu.emi.security.authn.x509.RevocationParameters.RevocationCheckingOrder;
 import eu.emi.security.authn.x509.StoreUpdateListener;
 import eu.emi.security.authn.x509.X509Credential;
 import eu.emi.security.authn.x509.impl.CertificateUtils;
 import eu.emi.security.authn.x509.impl.CertificateUtils.Encoding;
 import eu.emi.security.authn.x509.impl.KeyAndCertCredential;
 import eu.emi.security.authn.x509.impl.OpensslCertChainValidator;
 import eu.emi.security.authn.x509.impl.PEMCredential;
 import eu.emi.security.authn.x509.impl.SocketFactoryCreator;
 import eu.emi.security.authn.x509.impl.ValidatorParams;
 
 /**
  * AXIS2SocketFactory.java
  * 
  * @author Joni Hahkala Created on 2012-06-20 - based on glite trustmanager
  *         AXISSocketFactory.java
  */
 public class CANLAXIS2SocketFactory implements ProtocolSocketFactory {
     // private static final Logger LOGGER =
     // Logger.getLogger("org.glite.security.trustmanager.axis2.AXIS2SocketFactory");
 
     public static final String KEY_STRING = "canl.key";
     public static final String PASSWORD_STRING = "canl.password";
     public static final String CERT_STRING = "canl.cert";
     public static final String PROXY_STRING = "canl.proxy";
     public static final String UPDATEINTERVAL_STRING = "canl.updateinterval";
     public static final String NAMESPACE_STRING = "canl.namespace";
     public static final String TRUSTSTORE_STRING = "canl.truststore";
     public static final String PROXY_SUPPORT_STRING = "canl.proxysupport";
     public static final String CRL_CHEKING_MODE_STRING = "canl.crlcheckingmode";
     public static final String SSL_TIMEOUT_SETTING = "canl.timeout";
     public static final String TIMEOUT_DEFAULT = "30000";
 
     /** Thread local storage for the thread specific client properties. */
     private static ThreadLocal<Properties> theAXIS2SocketFactoryProperties = new ThreadLocal<Properties>();
 
     /**
      * Holds the socket factory in case the constructor with already created
      * socket factory is used.
      */
     private SSLSocketFactory _factory = null;
 
     /**
      * Gets the threadlocal properties object if it is set, otherwise returns
      * System properties object.
      * 
      * @return java.util.Properties with the settings of the current thread.
      */
     public static Properties getCurrentProperties() {
         Properties thisProperties = (Properties) theAXIS2SocketFactoryProperties.get();
 
         // if nothing was set, then fall back to global settings
         if (thisProperties == null) {
             thisProperties = System.getProperties();
         }
 
         return thisProperties;
     }
 
     /** Clears the thread specific properties object. */
     public static void clearCurrentProperties() {
         theAXIS2SocketFactoryProperties.set(null);
     }
 
     /**
      * @param cp the Properties associated with the current thread
      */
     public static void setCurrentProperties(Properties cp) {
         theAXIS2SocketFactoryProperties.set(cp);
     }
 
     /**
      * Creates a new instance of AXIS2SocketFactory that will be configured
      * either by setting current properties or if not set, it tries to read
      * properties from system properties.
      */
     public CANLAXIS2SocketFactory() {
         // do nothing
     }
 
     /**
      * Creates a new instance of AXIS2SocketFactory that uses the given
      * SSLSocketFactory
      */
     public CANLAXIS2SocketFactory(SSLSocketFactory factory) {
         _factory = factory;
     }
 
     /** Creates a new SSLSocket bound to ContextWrapper **/
     private Socket createSocket() throws IOException {
 
         if (_factory == null) {
             Properties attributes = getCurrentProperties();
             StoreUpdateListener listener = new StoreUpdateListener() {
                 public void loadingNotification(String location, String type, Severity level, Exception cause) {
                     if (level != Severity.NOTIFICATION) {
                         System.out.println("Error when creating or using SSL socket. Type " + type + " level: " + level
                                 + " cause: " + cause.getClass() + ":" + cause.getMessage());
                     } else {
                         // log successful (re)loading
                     }
                 }
             };
 
             ArrayList<StoreUpdateListener> listenerList = new ArrayList<StoreUpdateListener>();
             listenerList.add(listener);
 
             RevocationParameters revParam = new RevocationParameters(CrlCheckingMode.REQUIRE, new OCSPParametes(),
                     false, RevocationCheckingOrder.CRL_OCSP);
 
             String crlCheckingMode = (String) attributes.get(CRL_CHEKING_MODE_STRING);
             if (crlCheckingMode != null) {
                 if (crlCheckingMode.equalsIgnoreCase("ifvalid")) {
                     revParam = new RevocationParameters(CrlCheckingMode.IF_VALID, new OCSPParametes(), false,
                             RevocationCheckingOrder.CRL_OCSP);
                 } else {
                     if (crlCheckingMode.equalsIgnoreCase("ignore")) {
                         revParam = new RevocationParameters(CrlCheckingMode.IGNORE, new OCSPParametes(), false,
                                 RevocationCheckingOrder.CRL_OCSP);
                     }
                 }
             }
 
             ProxySupport proxySupport = ProxySupport.ALLOW;
             String proxySupportString = (String) attributes.get(PROXY_SUPPORT_STRING);
             if (proxySupportString != null) {
                 if (proxySupportString.equalsIgnoreCase("no") || proxySupportString.equalsIgnoreCase("false")) {
                     proxySupport = ProxySupport.DENY;
                 }
             }
 
             ValidatorParams validatorParams = new ValidatorParams(revParam, proxySupport, listenerList);
 
             String trustStoreLocation = (String) attributes.get(TRUSTSTORE_STRING);
             if (trustStoreLocation == null) {
                 throw new IOException(
                         "No truststore defined, unable to load CA certificates and thus create SSL socket.");
             }
 
             String namespaceModeString = (String) attributes.get(NAMESPACE_STRING);
             NamespaceCheckingMode namespaceMode = NamespaceCheckingMode.EUGRIDPMA_AND_GLOBUS;
             if (namespaceModeString != null) {
                 if (namespaceModeString.equalsIgnoreCase("no") || namespaceModeString.equalsIgnoreCase("false")
                         || namespaceModeString.equalsIgnoreCase("off")) {
                     namespaceMode = NamespaceCheckingMode.IGNORE;
                 } else {
                     if (namespaceModeString.equalsIgnoreCase("require")) {
                         namespaceMode = NamespaceCheckingMode.EUGRIDPMA_AND_GLOBUS_REQUIRE;
                     }
                 }
 
             }
 
             String intervalString = (String) attributes.get(UPDATEINTERVAL_STRING);
             long intervalMS = 3600000; // update ever hour
             if (intervalString != null) {
                 intervalMS = Long.parseLong(intervalString);
             }
 
             OpensslCertChainValidator validator = new OpensslCertChainValidator(trustStoreLocation, namespaceMode,
                     intervalMS, validatorParams);
 
             X509Credential credentials = null;
 
             String proxyLoc = (String) attributes.get(PROXY_STRING);
             if (proxyLoc != null) {
                 try {
                    credentials = new PEMCredential(proxyLoc, null);
                 } catch (KeyStoreException e) {
                     throw new IOException("Error opening proxy from " + proxyLoc + ": ", e);
                 } catch (CertificateException e) {
                     throw new IOException("Error reading proxy from " + proxyLoc + ": ", e);
                 }
             } else {
 
                 String hostCertLoc = (String) attributes.get(CERT_STRING);
                 if (hostCertLoc == null) {
                     throw new IOException(
                             "Variable hostcert undefined, cannot start server with SSL/TLS without host certificate.");
                 }
                 java.security.cert.X509Certificate[] hostCertChain = CertificateUtils.loadCertificateChain(
                         new FileInputStream(hostCertLoc), Encoding.PEM);
 
                 String password = (String) attributes.get(PASSWORD_STRING);
                 String hostKeyLoc = (String) attributes.get(KEY_STRING);
                 if (hostKeyLoc == null) {
                     throw new IOException(
                             "Variable hostkey undefined, cannot start server with SSL/TLS without host private key.");
                 }
                 PrivateKey hostKey = CertificateUtils.loadPrivateKey(new FileInputStream(hostKeyLoc), Encoding.PEM,
                         password == null ? null : password.toCharArray());
 
                 try {
                     credentials = new KeyAndCertCredential(hostKey, hostCertChain);
                 } catch (KeyStoreException e) {
                     throw new IOException("Error while creating keystore: " + e + ": " + e.getMessage(), e);
                 }
 
             }
             SSLSocketFactory newFactory = SocketFactoryCreator.getSocketFactory(credentials, validator);
             SSLSocket socket = (SSLSocket) newFactory.createSocket();
             return socket;
         } else {
             return _factory.createSocket();
         }
     }
 
     /**
      * Creates a new SSLSocket bound to ContextWrapper and layered over an
      * existing socket. UNIMPLEMENTED
      **/
     private Socket createSocket(Socket s, boolean autoclose) throws IOException {
         // LOGGER.fatal("createSocket(s, ac): function unimplemented");
         throw new IOException("createSocket(Socket s, boolean autoclose) unimplemented");
     }
 
     /**
      * Connect socket to remote host.
      * 
      * @param socket the socket to be connected
      * @param remoteaddr remote host and port
      * @param localaddr optional local host and port
      * @param timeout optional timeout, default if used if timeout == 0
      * 
      * @return original socket
      **/
     private final Socket connectSocket(Socket socket, SocketAddress remoteaddr, SocketAddress localaddr, int timeout)
             throws IOException {
         int newTimeout = timeout;
         if (localaddr != null) {
             socket.bind(localaddr);
         }
 
         // if no timeout is given, see if the property is set and use that
         if (timeout == 0) {
             String timeoutString = getCurrentProperties().getProperty(SSL_TIMEOUT_SETTING, TIMEOUT_DEFAULT);
             newTimeout = Integer.parseInt(timeoutString);
         }
 
         socket.setSoTimeout(newTimeout);
         socket.connect(remoteaddr, newTimeout);
 
         return socket;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.apache.commons.httpclient.protocol.ProtocolSocketFactory#createSocket
      * (java.lang.String, int, java.net.InetAddress, int,
      * org.apache.commons.httpclient.params.HttpConnectionParams)
      */
     public Socket createSocket(final String host, final int port, final InetAddress localHost, final int localPort,
             final HttpConnectionParams params) throws IOException, UnknownHostException {
         Socket socket = createSocket();
 
         int timeout = params.getConnectionTimeout();
         SocketAddress remoteaddr = new InetSocketAddress(host, port);
         SocketAddress localaddr = new InetSocketAddress(localHost, localPort);
 
         connectSocket(socket, remoteaddr, localaddr, timeout);
         return socket;
     }
 
     /**
      * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int,java.net.InetAddress,int)
      **/
     public Socket createSocket(final String host, final int port, final InetAddress localHost, final int localPort)
             throws IOException, UnknownHostException {
         Socket socket = createSocket();
 
         SocketAddress remoteaddr = new InetSocketAddress(host, port);
         SocketAddress localaddr = new InetSocketAddress(localHost, localPort);
 
         connectSocket(socket, remoteaddr, localaddr, 0);
         return socket;
     }
 
     /**
      * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int)
      **/
     public Socket createSocket(final String host, final int port) throws IOException, UnknownHostException {
         Socket socket = createSocket();
 
         SocketAddress remoteaddr = new InetSocketAddress(host, port);
 
         connectSocket(socket, remoteaddr, null, 0);
         return socket;
     }
 
     /**
      * @see SecureProtocolSocketFactory#createSocket(java.net.Socket,java.lang.String,int,boolean)
      **/
     public Socket createSocket(final Socket s, final String host, final int port, final boolean autoClose)
             throws IOException, UnknownHostException {
         Socket socket = createSocket(s, autoClose);
 
         SocketAddress remoteaddr = new InetSocketAddress(host, port);
 
         connectSocket(socket, remoteaddr, null, 0);
         return socket;
     }
 
 }
