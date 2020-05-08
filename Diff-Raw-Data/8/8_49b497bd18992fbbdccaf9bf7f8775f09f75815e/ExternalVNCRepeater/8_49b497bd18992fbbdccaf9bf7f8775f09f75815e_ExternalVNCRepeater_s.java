 package com.novell.cm.ui.server.vnc;
 
 
 import java.io.PrintStream;
 
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 
 import com.netiq.websockify.WebsockifyServer;
 import com.netiq.websockify.WebsockifyServer.SSLSetting;
 import com.netiq.websockify.WebsockifySslContext;
 
 
 
 public class ExternalVNCRepeater
 {   
    @Option(name="--help",usage="show this help message and quit")
    private boolean showHelp = false;
    
    @Option( name = "--enable-ssl", usage = "enable SSL" )
    private boolean       enableSSL  = false;
 
    @Option( name = "--ssl-only", usage = "disallow non-encrypted connections" )
    private boolean       requireSSL = false;
    
    @Option(name="--keystore",usage="path to a java keystore file. Required for SSL.")
    private String keystore = null;
    
    @Option(name="--keystore-password",usage="password to the java keystore file. Required for SSL.")
    private String keystorePassword = null;
    
    @Option(name="--keystore-key-password",usage="password to the private key in the java keystore file. If not specified the keystore-password value will be used.")
    private String keystoreKeyPassword = null;
 
    @Argument( index = 0, metaVar = "source_port", usage = "(required) local port the websockify server will listen on", required = true )
    private int           sourcePort;
 
    @Argument( index = 1, metaVar = "cmas_base_url", usage = "(required) the base URL of the CMAS server", required = true )
    private String        cmasBaseUrl;
 
    private CmdLineParser parser;
 
 
    public ExternalVNCRepeater()
    {
       parser = new CmdLineParser ( this );
    }
 
 
    public void printUsage( PrintStream out )
    {
       out.println ( "Usage:" );
       out.println ( " java -jar external-vnc-repeater.jar [options] source_port cmas_base_url" );
       out.println ( );
       out.println ( "Options:" );
       parser.printUsage ( out );
       out.println ( );
       out.println ( "Example:" );
       out.println ( " java -jar external-vnc-repeater.jar 5900 https://cloud.acmecloud.demo" );
    }
    
    public static void main(String[] args) throws Exception {
      new ExternalVNCRepeater().doMain(args);
    }
    
    public void doMain(String[] args) throws Exception
    {
       parser.setUsageWidth ( 80 );
 
       // parse the command line arguments
       try
       {
          parser.parseArgument ( args );
       }
       // if there's a problem show the error and command line usage help
       catch ( CmdLineException e )
       {
          System.err.println ( e.getMessage ( ) );
          printUsage ( System.err );
          return;
       }
 
       // if we were asked for help show it and exit
       if ( showHelp )
       {
          printUsage ( System.out );
          return;
       }
       
       // set the SSL setting based on the command line params
       SSLSetting sslSetting = SSLSetting.OFF;
       if ( requireSSL ) sslSetting = SSLSetting.REQUIRED;
       else if ( enableSSL ) sslSetting = SSLSetting.ON;
 
       // if we are doing SSL
       if ( sslSetting != SSLSetting.OFF ) {
          // make sure there is a keystore path specified
           if (keystore == null || keystore.isEmpty()) {
               System.err.println("No keystore specified.");
           printUsage(System.err);
               System.exit(1);
           }
 
           // and make sure there is a keystore password specified
           if (keystorePassword == null || keystorePassword.isEmpty()) {
               System.err.println("No keystore password specified.");
           printUsage(System.err);
               System.exit(1);
           }
           
           // if there's no keystore key password, use the keystore password
           if (keystoreKeyPassword == null || keystoreKeyPassword.isEmpty()) {
              keystoreKeyPassword = keystorePassword;
           }
           
           // and validate the keystore settings - this actually starts up an SSL
           // context and lets us know if there were exceptions starting it
           // this doesn't happen in the current thread when the server is started
           // so we only know about it in worker threads and put it out to the logger
           try
           {
              WebsockifySslContext.validateKeystore(keystore, keystorePassword, keystoreKeyPassword);
           }
           catch ( Exception e )
           {
              System.err.println("Error validating keystore: " + e.getMessage() );
              printUsage(System.err);
              System.exit(2);
           }
       }
 
       System.out.println ( "Proxying *:" + sourcePort + " to workloads defined by CMAS at " + cmasBaseUrl + " ..." );
       if(sslSetting != SSLSetting.OFF) System.out.println("SSL is " + (sslSetting == SSLSetting.REQUIRED ? "required." : "enabled."));
 
       WebsockifyServer ws = new WebsockifyServer ( );
       ws.connect ( sourcePort, new CMASRestResolver ( cmasBaseUrl ), sslSetting, keystore, keystorePassword, keystoreKeyPassword, null );
 
    }
 }
