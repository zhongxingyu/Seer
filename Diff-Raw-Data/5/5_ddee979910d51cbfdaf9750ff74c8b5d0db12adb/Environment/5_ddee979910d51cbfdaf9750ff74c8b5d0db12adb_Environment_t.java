 /*
  * Created On: Aug 5, 2006 10:16:01 AM
  */
 package com.thinkparity.codebase.model.session;
 
 import com.thinkparity.codebase.NetworkUtil;
 
 /**
  * @author raymond@thinkparity.com
  * @version 1.1.2.0
  */
 public enum Environment {
 
     /** The demo environment. */
     DEMO("thinkparity.dyndns.org", 5231, Boolean.TRUE, "thinkparity.net",
             "thinkparity.dyndns.org", 20004, Boolean.TRUE,
             "thinkparity.dyndns.org", 80, Boolean.TRUE),
 
     /** A localhost demo environment. */
     DEMO_LOCALHOST("localhost", 5231, Boolean.TRUE, "thinkparity.net",
             "localhost", 20004, Boolean.TRUE,
             "localhost", 80, Boolean.TRUE),
 
     /** A localhost development environment. */
     DEVELOPMENT_LOCALHOST("localhost", 5227, Boolean.TRUE, "thinkparity.net",
             "localhost", 20002, Boolean.TRUE,
             "localhost", 80, Boolean.FALSE),
 
     /** Raymond's development environment. */
     DEVELOPMENT_RAYMOND("thinkparity.dyndns.org", 5226, Boolean.FALSE, "thinkparity.net",
             "thinkparity.dyndns.org", 20002, Boolean.TRUE,
             "thinkparity.dyndns.org", 80, Boolean.FALSE),
 
     /** Robert's development environment. */
     DEVELOPMENT_ROBERT("thinkparity.dyndns.org", 5229, Boolean.TRUE, "thinkparity.net",
             "thinkparity.dyndns.org", 20003, Boolean.TRUE,
             "thinkparity.dyndns.org", 80, Boolean.TRUE),
 
     /** Production environment. */
     PRODUCTION("thinkparity.net", 5223, Boolean.TRUE, "thinkparity.net",
             "yvr.thinkparity.net", 20000, Boolean.TRUE,
            "thinkparity.com", 80, Boolean.FALSE),
 
     /** Testing environment. */
     TESTING("thinkparity.dyndns.org", 5225, Boolean.TRUE, "thinkparity.net",
             "thinkparity.dyndns.org", 20001, Boolean.TRUE,
            "thinkparity.com", 80, Boolean.FALSE),
 
     /** A localhost testing environment. */
     TESTING_LOCALHOST("localhost", 5225, Boolean.TRUE, "thinkparity.net",
             "localhost", 20001, Boolean.TRUE,
             "localhost", 80, Boolean.TRUE);
 
     /** The stream server host. */
     private final transient String streamHost;
 
     /** The stream server port. */
     private final transient Integer streamPort;
 
     /** The stream service tls enabled <code>Boolean</code> flag. */
     private final transient Boolean streamTLSEnabled;
 
     /** The xmpp host. */
     private final transient String xmppHost;
 
     /** The xmpp port. */
     private final transient Integer xmppPort;
 
     /** The xmpp service <code>String</code>. */
     private final transient String xmppService;
 
     /** The xmpp protocol. */
     private final transient Boolean xmppTLSEnabled;
 
     /** The web host. */
     private final transient String webHost;
 
     /** The web port. */
     private final transient Integer webPort;
 
     /** The web tls enabled <code>Boolean</code> flag. */
     private final transient Boolean webTLSEnabled;
 
     /**
      * Create Environment.
      * 
      * @param xmppHost
      *            The xmpp server host <code>String</code>.
      * @param xmppPort
      *            The xmpp server port <code>Integer</code>.
      * @param xmppProtocol
      *            The <code>XMPPProtocol</code>.
      * @param xmppService
      *            The xmpp service <code>String</code>.
      * @param streamHost
      *            The stream server host.
      * @param streamPort
      *            The stream server port
      * @param streamProtocol
      *            The stream server protocol.
      * @param webHost
      *            The web host.
      * @param webPort
      *            The web port
      * @param webTLSEnabled
      *            The web TLS enabled flag.
      */
     private Environment(final String xmppHost, final Integer xmppPort,
             final Boolean xmppTLSEnabled, final String xmppService,
             final String streamHost, final Integer streamPort,
             final Boolean streamTLSEnabled,
             final String webHost, final Integer webPort,
             final Boolean webTLSEnabled) {
         this.xmppHost = xmppHost;
         this.xmppPort = xmppPort;
         this.xmppTLSEnabled = xmppTLSEnabled;
         this.xmppService = xmppService;
         this.streamHost = streamHost;
         this.streamPort = streamPort;
         this.streamTLSEnabled = streamTLSEnabled;
         this.webHost = webHost;
         this.webPort = webPort;
         this.webTLSEnabled = webTLSEnabled;
     }
 
     /**
      * Obtain the streamHost
      * 
      * @return The stream host <code>String</code>.
      */
     public String getStreamHost() {
         return streamHost;
     }
 
     /**
      * Obtain the streamPort
      *
      * @return The Integer.
      */
     public Integer getStreamPort() {
         return streamPort;
     }
 
     /**
      * Obtain the webHost
      * 
      * @return The web host <code>String</code>.
      */
     public String getWebHost() {
         return webHost;
     }
 
     /**
      * Obtain the webPort
      *
      * @return The Integer.
      */
     public Integer getWebPort() {
         return webPort;
     }
 
     /**
      * Obtain the xmppHost
      *
      * @return The String.
      */
     public String getXMPPHost() {
         return xmppHost;
     }
 
     /**
      * Obtain the xmppPort
      *
      * @return The port.
      */
     public Integer getXMPPPort() {
         return xmppPort;
     }
 
     /**
      * Obtain the xmpp service.
      * 
      * @return An xmpp service <code>String</code>.
      */
     public String getXMPPService() {
         return xmppService;
     }
 
     /**
      * Determine whether or not all services within the environment are
      * reachable.
      * 
      * @return True if the environment is reachable; false otherwise.
      */
     public Boolean isReachable() {
         return isXMPPReachable() && isStreamReachable();
     }
 
     /**
      * Determine whether or not the stream service within the environment is
      * reachable.
      * 
      * @return True if it is reachable.
      */
     public Boolean isStreamReachable() {
         return NetworkUtil.isTargetReachable(streamHost, streamPort);
     }
 
     /**
      * Obtain the streamProtocol
      *
      * @return The StreamProtocol.
      */
     public Boolean isStreamTLSEnabled() {
         return streamTLSEnabled;
     }
 
     /**
      * Obtain the webProtocol
      *
      * @return The WebProtocol.
      */
     public Boolean isWebTLSEnabled() {
         return webTLSEnabled;
     }
 
     /**
      * Determine whether or not the xmpp service within the environment is
      * reachable.
      * 
      * @return True if it is reachable.
      */
     public Boolean isXMPPReachable() {
         return NetworkUtil.isTargetReachable(xmppHost, xmppPort);
     }
 
     /**
      * Obtain the xmppProtocol
      *
      * @return The XMPPProtocol.
      */
     public Boolean isXMPPTLSEnabled() {
         return xmppTLSEnabled;
     }
 
     /**
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() {
         return new StringBuffer(getClass().getName()).append("//")
                 .append(xmppHost)
                 .append("/").append(xmppPort)
                 .append("/").append(xmppTLSEnabled)
                 .append("/").append(xmppService)
                 .append("/").append(streamHost)
                 .append("/").append(streamPort)
                 .append("/").append(streamTLSEnabled)
                 .toString();
     }
 }
