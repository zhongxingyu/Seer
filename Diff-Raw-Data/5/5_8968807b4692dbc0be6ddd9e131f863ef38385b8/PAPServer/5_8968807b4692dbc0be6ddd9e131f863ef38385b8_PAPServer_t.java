 package org.glite.authz.pap.server.standalone;
 
 import java.util.Collections;
 import java.util.Properties;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 import org.glite.authz.pap.common.PAPConfiguration;
 import org.mortbay.jetty.Connector;
 import org.mortbay.jetty.Handler;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.handler.DefaultHandler;
 import org.mortbay.jetty.handler.HandlerCollection;
 import org.mortbay.jetty.webapp.WebAppContext;
 import org.mortbay.thread.concurrent.ThreadPool;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The standalone PAP daemon
  */
 public final class PAPServer {
 
     /**
      * 
      * Useful defaults for the standalone service
      *
      */
     final class PAPStandaloneServiceDefaults {
         
         /**
          * The service hostname  
          */
         static final String HOSTNAME = "127.0.0.1";
 
         /**
          * The service port on which the pap will listen to request
          */
         static final int PORT = 8150;
 
         /**
          * The shutdown service port
          */
         static final int SHUTDOWN_PORT = 8151;
 
         /**
          * Max request queue size. -1 means unbounded queue is used.
          */
         static final int MAX_REQUEST_QUEUE_SIZE = -1;
 
         /**
          * Max concurrent connections.
          */
         static final int MAX_CONNECTIONS = 64;
         
         /**
          * Default certificate path for the service
          */
         static final String CERTIFICATE_PATH = "/etc/grid-security/hostcert.pem";
         
         /**
          * Default private key path for the service
          */
         static final String PRIVATE_KEY_PATH = "/etc/grid-security/hostkey.pem";
         
         /**
          * Default path for CA certificates
          */
         static final String CA_PATH = "/etc/grid-security/certificates";
         
         /**
          * Should CRLs be checked by trustmanager?
          */
         static final boolean CRL_ENABLED = true;
         
     } 
 
     private static final String DEFAULT_WAR_LOCATION = System.getProperty( "PAP_HOME" )
         + "/wars/pap-standalone.war";
     
     private static final Logger log = LoggerFactory.getLogger( PAPServer.class );
     
     /**
      * The option name used by callers to specify where the pap server should look for the configuration
      */
     private static final String CONF_DIR_OPTION_NAME = "conf-dir";
 
     /**
      * The pap configuration directory 
      */
     protected String papConfigurationDir;
     
     /**
      * The pap jetty http server 
      */
     protected Server papServer;
 
     /**
      * The jetty webapp context in which the pap wep application is configured
      */
     private WebAppContext webappContext;
 
     /**
      * Constructor.
      * Parses the configuration and starts the server.
      * 
      * @param args. the command line arguments as passed by the {@link #main(String[])} method
      */
     public PAPServer( String[] args ) {
 
         try {
 
             parseOptions( args );
 
             PAPConfiguration.initialize( papConfigurationDir);
 
             configurePAPServer();
 
             papServer.start();
 
             if ( webappContext.getUnavailableException() != null )
                 throw webappContext.getUnavailableException();
 
             papServer.join();
 
         } catch ( Throwable e ) {
 
             log
                     .error( "PAP encountered an error that could not be dealt with, shutting down!" );
             
             log.error( e.getMessage() );
             
            // Also print error message to standard error 
            
            System.err.println("PAP encountered an error that could not be dealt with, shutting down!");
            System.err.println("Error: "+e.getMessage());
            e.printStackTrace(System.err);
             
             if (log.isDebugEnabled()) 
                 log.error( e.getMessage(), e);
             
 
             try {
                 papServer.stop();
 
             } catch ( Exception e1 ) {
                 // Just ignore this
             }
 
             System.exit( 1 );
         }
 
     }
 
     /**
      * Utility method to map pap configuration property names to glite-security-trustmanager property names
      * @return the trustmanager properties
      */
     private Properties buildTrustmanagerConfiguration(){
         
         Properties tmProps = new Properties();
         
         tmProps.setProperty( "sslCertFile", getStringFromSecurityConfiguration( "certificate", PAPStandaloneServiceDefaults.CERTIFICATE_PATH ));
         tmProps.setProperty( "sslKey" ,  getStringFromSecurityConfiguration( "private_key", PAPStandaloneServiceDefaults.PRIVATE_KEY_PATH ));
         tmProps.setProperty( "crlEnabled", getStringFromSecurityConfiguration( "crl_enabled", String.valueOf(PAPStandaloneServiceDefaults.CRL_ENABLED)));
         
         return tmProps;
         
     }
 
     /**
      * Performs the jetty server configuration
      */
     private void configurePAPServer() {
 
         log.info("Configuring jetty PAP server...");
         
         int port = getIntFromStandaloneConfiguration( "port", PAPStandaloneServiceDefaults.PORT );
         
         
         papServer = new Server();
         
         int maxRequestQueueSize = getIntFromStandaloneConfiguration( "max_request_queue_size",
                 PAPStandaloneServiceDefaults.MAX_REQUEST_QUEUE_SIZE );
         
         log.debug("maxRequestQueueSize = {}", maxRequestQueueSize);
 
         int maxConnections = getIntFromStandaloneConfiguration( "max_connections",
                 PAPStandaloneServiceDefaults.MAX_CONNECTIONS );
         
         if (maxConnections <= 0){
             log.error("Please specify a positive value for the 'maxConnections' configuration parameter!");
             log.error( "Will use the hardcoded default '{}' instead...", PAPStandaloneServiceDefaults.MAX_CONNECTIONS );
             maxConnections = PAPStandaloneServiceDefaults.MAX_CONNECTIONS;
         }
             
         log.info("maxConnections = {}", maxConnections);
 
         papServer.setSendServerVersion( false );
         papServer.setSendDateHeader( false );
 
         BlockingQueue <Runnable> requestQueue;
 
         if ( maxRequestQueueSize < 1 ) {
             requestQueue = new LinkedBlockingQueue <Runnable>();
         } else {
             requestQueue = new ArrayBlockingQueue <Runnable>(
                     maxRequestQueueSize );
         }
 
         ThreadPool threadPool = new ThreadPool( 5, maxConnections, 60,
                 TimeUnit.SECONDS, requestQueue );
 
         papServer.setThreadPool( threadPool );
 
         TrustManagerSelectChannelConnector connector = new TrustManagerSelectChannelConnector(
                 buildTrustmanagerConfiguration() );
 
         connector.setPort( port );
         String host = getStringFromStandaloneConfiguration( "hostname", PAPStandaloneServiceDefaults.HOSTNAME );
         
         if (! host.equals( PAPStandaloneServiceDefaults.HOSTNAME ))
             connector.setHost( host );
         else
         	// Will listen on any IP on the local machine
         	connector.setHost("0.0.0.0");        	
                 
         log.info( "PAP service will listen on {}:{}", new Object[]{host,port} );
         papServer.setConnectors( new Connector[] { connector } );
 
         JettyShutdownCommand papShutdownCommand = new JettyShutdownCommand(
                 papServer );
 
         JettyShutdownService.startJettyShutdownService( 8151, Collections
                 .singletonList( (Runnable) papShutdownCommand ) );
         
         
         webappContext = new WebAppContext();
 
         webappContext.setContextPath( "/"+PAPConfiguration.DEFAULT_WEBAPP_CONTEXT );
         webappContext.setWar( DEFAULT_WAR_LOCATION );
         webappContext.setParentLoaderPriority(true);
 
         HandlerCollection handlers = new HandlerCollection();
         handlers.setHandlers( new Handler[] { webappContext,
                 new DefaultHandler() } );
 
         papServer.setHandler( handlers );
         
     }
 
     /**
      * Utility method to fetch an int configuration parameter out of the standalone-service configuration
      * 
      * @param key, the configuration parameter key
      * @param defaultValue, a default value in case the parameter is not defined
      * @return the configuration parameter value
      */
     private int getIntFromStandaloneConfiguration( String key, int defaultValue ) {
 
         PAPConfiguration conf = PAPConfiguration.instance();
         return conf.getInt( PAPConfiguration.STANDALONE_SERVICE_STANZA + "." + key, defaultValue );
     }
     
 
     /**
      * Utility method to fetch a string configuration parameter out of the security configuration
      * 
      * @param key, the configuration parameter key
      * @param defaultValue, a default value in case the parameter is not defined
      * @return the configuration parameter value
      * 
      */
     private String getStringFromSecurityConfiguration(String key, String defaultValue){
         
         PAPConfiguration conf = PAPConfiguration.instance();
         return conf.getString( PAPConfiguration.SECURITY_STANZA+"."+key, defaultValue);
     }
 
     /**
      * Utility method to fetch a string configuration parameter out of the security configuration
      * 
      * @param key, the configuration parameter key
      * @param defaultValue, a default value in case the parameter is not defined
      * @return the configuration parameter value
      */
     private String getStringFromStandaloneConfiguration( String key, String defaultValue){
         
         PAPConfiguration conf = PAPConfiguration.instance();
         return conf.getString( PAPConfiguration.STANDALONE_SERVICE_STANZA + "." + key, defaultValue );
         
     }
 
     /**
      * Parses command line options
      * 
      * @param args, the command line options
      */
     protected void parseOptions( String[] args ) {
 
         if ( args.length > 0 ) {
 
             int currentArg = 0;
 
             while ( currentArg < args.length ) {
 
                 if ( !args[currentArg].startsWith( "--" ) ) {
                     usage();
                 } else if ( args[currentArg].equalsIgnoreCase( "--"
                         + CONF_DIR_OPTION_NAME ) ) {
                     papConfigurationDir = args[currentArg + 1];
                     log.info( "Starting PAP with configuration dir: {}",
                             papConfigurationDir );
                     currentArg = currentArg + 2;
                     
                 }else
                     usage();
 
             }
 
         }
     }
 
     /**
      * Prints a usage message and exits with status 1
      */
     private void usage() {
 
         String usage = "PAPServer [--" + CONF_DIR_OPTION_NAME
                 + " <confDir>]";
 
         System.out.println( usage );
         System.exit( 1 );
     }
 
     /**
      * Runs the service
      * @param args, the command line arguments
      */
     public static void main( String[] args ) {
 
         new PAPServer( args );
 
     }
 
 }
