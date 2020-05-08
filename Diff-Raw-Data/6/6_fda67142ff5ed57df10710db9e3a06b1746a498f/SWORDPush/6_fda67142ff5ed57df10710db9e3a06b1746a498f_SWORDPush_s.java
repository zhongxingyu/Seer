 package com.k_int.euinside.client.module.sword;
 
 import com.k_int.euinside.client.module.CommandLineArguments;
 import com.k_int.euinside.client.module.sword.client.PostMessageByte;
 import com.k_int.euinside.client.module.sword.client.SWClient;
 import org.apache.commons.io.IOUtils;
 import org.purl.sword.base.DepositResponse;
 import org.purl.sword.base.SWORDErrorDocument;
 import org.purl.sword.base.SWORDException;
 import org.purl.sword.base.ServiceDocument;
 import org.purl.sword.client.ClientConstants;
 import org.purl.sword.client.SWORDClientException;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 public class SWORDPush {
 
     private String serverLoc;
     private String username;
     private String password;
     private String onBehalfOf;
     private String collectionId;
     private SWClient swordClient;
     private PostMessageByte postMessage;
     private int successCount = 0;
     private int errorCount = 0;
     private int requestTimeout = 60000;
 
     public static final String DATA_TYPE_XML = "application/xml";
     public static final String DATA_TYPE_ZIP = "application/zip";
     public static final String PACKAGING_PEOPLES_RECORDS= "http://www.peoplesnetwork.gov.uk/sword-types/peoplesrecord-zip";
 
     public SWORDPush( String serverLoc, String username, String password, String onBehalfOf, String collectionId ) {
         this.serverLoc = serverLoc;
         this.username = username;
         this.password = password;
         this.onBehalfOf = onBehalfOf;
         this.collectionId = collectionId;
     }
 
     public static void main( String[] args ) {
         if ( args[0].equals( "-help" ) ) {
             System.out.println( "Arguments required: -swordURL -u -p -onBehalf -collectionId -filePath -recordType ." );
             System.exit( 0 );
         }
         CommandLineArguments arg = new CommandLineArguments( args );
         SWORDPush swordPush = new SWORDPush( arg.getSwordURL(),
                 arg.getUsername(), arg.getPassword(),
                 arg.getOnBehalfOf(), arg.getCollectionId() );
 
         String errors = "No response received.";
         try {
             File rec = new File( arg.getFilePath() );
             FileInputStream fis = new FileInputStream( rec );
             byte[] record = IOUtils.toByteArray( fis );
 //             if used with DPP sword we should only accept these two types
 //            String fileType = rec.getName().contains( ".zip" )? DATA_TYPE_ZIP : DATA_TYPE_XML;
             errors = swordPush.pushData( record, arg.getRecordType(), rec.getName() );
         } catch ( IOException e ) {
             System.out.print( "There was an error reading the provided file. " + e.getMessage() );
         }
 
         System.out.println( "Successful deposits: " + swordPush.getSuccessful() );
         System.out.println( "Failed deposits: " + swordPush.getFailed() );
         System.out.println( "Error messages: " + errors );
     }
 
     /**
      * Initialize a SWORD client using the details provided on SWORDPush constructor. Client will
      * be used for executing dataPush methods. A default PostMessage is generated as well.
      *
      * @return If successful, the SWORDClient will be returned.
      */
     public SWClient initializeSWClient() throws SWORDClientException, MalformedURLException {
 
         if ( swordClient != null ) return swordClient;
         serverLoc=serverLoc.trim();
         if ( !serverLoc.endsWith( "/" ) ) serverLoc+=( "/" );
         if ( !serverLoc.startsWith( "http://" ) ) serverLoc = "http://" + serverLoc;
 
         String swordLocation = serverLoc + collectionId;
         SWClient swordClient = new SWClient();
 
         initialiseServer( swordLocation, username, password, swordClient );
 
         if ( username != null && username.length() > 0
                 && password != null
                 && password.length() > 0 ) {
 
             swordClient.setCredentials( username, password );
         } else {
             swordClient.clearCredentials();
         }
 
 //        get the service document. This describes the sword server. We could use this to set attributes like useMD5, noOp etc.
 //        serviceDoc = swordClient.getServiceDocument( serverLoc + "servicedocument", onBehalfOf );
 
         swordClient.setSocketTimeout( requestTimeout );
         this.swordClient = swordClient;
         // A default post message. Data will be added during pushData procedure.
         postMessage = createMessage( swordLocation, onBehalfOf, null );
 
         return swordClient;
     }
 
     /**
      * Push data to the SWORD server, using the details provided in SWORDPush constructor.
      * A default PostMessage is used, including data and dataType provided
      *
      * @param data     The byte array that will be pushed to the SWORD server
      * @param dataType The type of data within byte array. dpp SWORD server accepts xml and zip.
      * @return A String representation of any errors that occurred during the push process.
      */
 
     public String pushData( byte[] data, String dataType ) {
         return pushData( data, dataType, null );
     }
 
 
     /**
      * Push data to the SWORD server, using the details provided in SWORDPush constructor.
      * A default PostMessage is used, including data, dataType and package format provided.
      *
      * @param data     A byte array that will be pushed to the SWORD server
      * @param dataType The type of data within byte array. dpp SWORD server accepts xml and zip.
      * @param packageFormat The packaging format of the data.
      * @return A String representation of any errors that occurred during the push process.
      */
 
     public String pushData( byte[] data, String dataType, String packageFormat ) {
         if ( swordClient == null ) {
             try {
                 initializeSWClient();
             } catch ( SWORDClientException e ) {
                 return "Error initializing SWClient: " + e.getMessage();
             } catch ( MalformedURLException e ) {
                 return "Error initializing SWClient: " + e.getMessage();
             }
         }
         StringBuilder errors = new StringBuilder( "" );
 
         try {
             postMessage.setRecord( data );
             postMessage.setFiletype( dataType );
             postMessage.setFormatNamespace( packageFormat );
             DepositResponse response = swordClient.postFile( postMessage );
             evaluateResponse( errors, response );
         } catch ( SWORDClientException e ) {
             errors.append( "There was an error: " ).append( e.getMessage() );
         }
         return errors.toString();
 
     }
 
     /**
      * Push data to the SWORD server, using the details provided in SWORDPush constructor.
      * A PostMessageByte should be provided, which is forwarded to SWORD server
      *
      * @param customMessage A PostMessage containing details about deposit configuration
      * @return A textual representation of any errors that occurred during the push process.
      */
 
     public String pushData( PostMessageByte customMessage ) {
         if ( swordClient == null ) {
             try {
                 initializeSWClient();
             } catch ( SWORDClientException e ) {
                 return "Error initializing SWClient: " + e.getMessage();
             } catch ( MalformedURLException e ) {
                 return "Error initializing SWClient: " + e.getMessage();
             }
         }
         StringBuilder errors = new StringBuilder( "" );
 
         try {
             DepositResponse response = swordClient.postFile( customMessage );
             evaluateResponse( errors, response );
         } catch ( SWORDClientException e ) {
             errors.append( "There was an error: " ).append( e.getMessage() );
         }
         return errors.toString();
     }
 
     private void evaluateResponse(StringBuilder errors, DepositResponse response ) {
         if ( response.getHttpResponse() == 201 || response.getHttpResponse() == 202 ) {
             successCount++;
         } else {
             errorCount++;
             try {
                 SWORDErrorDocument errorDoc = response.getErrorDocument();
 
                errors.append( " The Error URI is: " )
                        .append( errorDoc.getErrorURI() )
                        .append( " Summary is: " )
                        .append( errorDoc.getSummary() )
                         .append( "\n" );
             } catch ( SWORDException se ) {
                 errors.append( se.getMessage() ).append( "\n" );
             }
         }
     }
 
     /**
      * Creates a PostMessage, which contains all the information that will be included in the HTTP request.
      * SWORD library formats these information to be read by a SWORD server.
      */
     private PostMessageByte createMessage( String location, String onBehalfOf, ServiceDocument serviceDoc ) {
 
         PostMessageByte message = new PostMessageByte();
         message.setDestination( location );
         message.setVerbose( true );    // provide detailed log
         message.setOnBehalfOf( onBehalfOf );
         message.setUserAgent( "ECK SWORD Client" );
         message.setNoOp( false );        //noOp means the deposit is simulated and wont actually happen.
         message.setUseMD5(false);
         message.setChecksumError(false);  // True if the item should include a checksum error.
         message.setCorruptRequest( false );  // True if the item should corrupt the POST header.
 
         return message;
     }
 
 
     /**
      * Initialise the server connection information. If there is a username and
      * password, the basic credentials will also be set. Otherwise, the
      * credentials will be cleared.
      *
      * @param location The location to connect to. This is a URL, of the format,
      *                 http://a.host.com:port/. The host name and port number will be
      *                 extracted. If the port is not specified, a default port of 80
      *                 will be used.
      * @param username The username. If this is null or an empty string, the basic
      *                 credentials will be cleared.
      * @param password The password. If this is null or an empty string, the basic
      *                 credentials will be cleared.
      * @throws java.net.MalformedURLException if there is an error processing the URL.
      */
     private void initialiseServer( String location, String username,
                                    String password, SWClient swordClient ) throws MalformedURLException {
         URL url = new URL( location );
         int port = url.getPort();
         if ( port == -1 ) {
             port = 80;
         }
 
         swordClient.setServer( url.getHost(), port );
 
         if ( username != null && username.length() > 0 && password != null
                 && password.length() > 0 ) {
             swordClient.setCredentials( username, password );
         } else {
             swordClient.clearCredentials();
         }
         swordClient.setUserAgent( ClientConstants.SERVICE_NAME );
     }
 
     public String getServerLoc() {
         return serverLoc;
     }
 
     public void setServerLoc( String serverLoc ) {
         this.serverLoc = serverLoc;
     }
 
     public String getUsername() {
         return username;
     }
 
     public void setUsername( String username ) {
         this.username = username;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword( String password ) {
         this.password = password;
     }
 
     public String getOnBehalfOf() {
         return onBehalfOf;
     }
 
     public void setOnBehalfOf( String onBehalfOf ) {
         this.onBehalfOf = onBehalfOf;
     }
 
     public String getCollectionId() {
         return collectionId;
     }
 
     public void setCollectionId( String collectionId ) {
         this.collectionId = collectionId;
     }
 
     public PostMessageByte getPostMessage() {
         return postMessage;
     }
 
     public void setPostMessage( PostMessageByte postMessage ) {
         this.postMessage = postMessage;
     }
 
     public int getSuccessful() {
         return successCount;
     }
 
     public int getFailed() {
         return errorCount;
     }
     public void setRequestTimeout(int time){
         requestTimeout=time;
         if(swordClient!=null){
             swordClient.setSocketTimeout( time );
         }
     }
 
 } 
