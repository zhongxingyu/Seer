 package dbc.opensearch.components.pti.tests.ptitest;
 
 
 import dbc.opensearch.components.pti.*;
 import dbc.opensearch.components.tools.*;
 
 // import java.io.ByteArrayInputStream;
 import java.io.File;
 // import java.io.IOException;
 import java.net.URL;
 // // import javax.xml.parsers.DocumentBuilderFactory;
 // // import javax.xml.parsers.DocumentBuilder;
 import org.compass.core.Compass;
 import org.compass.core.CompassSession;
 // import org.compass.core.CompassTransaction;
 import org.compass.core.config.CompassConfiguration;
 // import org.compass.core.config.CompassConfigurationFactory;
 // import org.compass.core.xml.AliasedXmlObject;
 // import org.compass.core.CompassException;
 
 
 // import org.compass.core.xml.dom4j.Dom4jAliasedXmlObject;
 // import org.compass.core.xml.javax.NodeAliasedXmlObject;
 
 import org.apache.log4j.Logger;
 
 // import org.dom4j.Document;
 // import org.dom4j.DocumentHelper;
 // import org.dom4j.DocumentException;
 // import org.dom4j.io.SAXReader;
 
 // import org.apache.commons.configuration.ConfigurationException;
 
 import dbc.opensearch.components.tools.tuple.Tuple;
 import dbc.opensearch.components.tools.tuple.Pair;
 
 public class PTItest{
     
     private static volatile Compass theCompass;
     
     //    private static volatile FedoraHandler fh;
         Logger log = Logger.getLogger("PTI test");
     
     public PTItest(){
 
         log.info( "\n\nPTI test" );
         
         
         log.info( "Configuring Compass" );
         CompassConfiguration conf = new CompassConfiguration();
 
         /** \todo: when we get more types of data (than the one we got now...) we should be able to load configurations at runtime. Or just initialize with all possible input-format? */
         URL cfg = getClass().getResource("/compass.cfg.xml");
         URL cpm = getClass().getResource("/xml.cpm.xml");
             
         log.debug( String.format( "Compass configuration=%s", cfg.getFile() ) );
         log.debug( String.format( "XSEM mappings file   =%s", cpm.getFile() ) );
             
         File cpmFile = new File( cpm.getFile() );
             
         conf.configure( cfg );
         conf.addFile( cpmFile );
             
         theCompass = conf.buildCompass();        
         
         log.info( "Getting Session" );
         CompassSession session = getSession();
         
         /////////////////////////////////
 
         Pair<String, Integer> handlePair;
         String fHandle;
         int queueID;
         
        //handlePair = queue.pop();
        //fHandle = Tuple.get1(handlePair);
 
 
         PTI pti = null;
         log.info( "Starting PTI thread" );
         try{
             pti = new PTI( session );
         }catch(Exception e){
             System.out.println( "Error: creating pti ");
             System.exit(0);
         }
         if ( pti == null ){
             System.out.println( "Error: pti = null");
             System.exit(0);
         }
         
 
         //float returnval = pti.call();
         //System.out.println( "returnval "+ returnval);
             
 
     }
     
 
 
 
 
     /**
      * returns a new session from the sessionpool, throwing an
      * exception if the session cannot be obtained.
      * \todo: We need a custom exception for depletion-messages from the pool
      * @returns a CompassSession from the SessionPool, if any are available.
      */
     public CompassSession getSession(){
         if( theCompass == null) {
             log.fatal( String.format( "Something very bad happened. getSession was called on an object that in the meantime went null. Aborting" ) );
             throw new RuntimeException( "Something very bad happened. getSession was called on an object that in the meantime went null. Aborting" );
         }
         CompassSession s = theCompass.openSession();
         return s;
     }
 
 
 }
