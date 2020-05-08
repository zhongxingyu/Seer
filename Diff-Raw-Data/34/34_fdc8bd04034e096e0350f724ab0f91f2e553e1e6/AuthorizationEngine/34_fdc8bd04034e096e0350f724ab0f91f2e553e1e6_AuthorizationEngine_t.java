 package org.glite.authz.pap.authz;
 
 import java.io.File;
import java.security.cert.X509Certificate;
 
 import org.glite.authz.pap.common.PAPConfiguration;
 import org.glite.authz.pap.common.exceptions.PAPConfigurationException;
import org.glite.voms.VOMSValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 public class AuthorizationEngine {
    
    public static final Logger logger = LoggerFactory.getLogger( AuthorizationEngine.class );
 
     private boolean initialized = false;
 
     private static AuthorizationEngine instance;
 
     private PAPContext globalContext;
 
     private AuthorizationEngine( String papConf ) {
 
         File papConfFile = new File( papConf );
 
         if ( !papConfFile.exists() )
             throw new PAPConfigurationException(
                     "PAP Authorization configuration file not found: "
                             + papConfFile.getAbsolutePath() );
 
         globalContext = PAPContext.instance( "global-context" );
 
         // Parse ACL from configuration file
         AuthzConfigurationParser confParser = AuthzConfigurationParser
                 .instance();
 
         confParser.parse( papConfFile );
 
         globalContext.setAcl( confParser.getParsedACL() );
        
        CurrentAdmin.validator = new VOMSValidator((X509Certificate)null);
        
     }
 
     public static AuthorizationEngine initialize( String papAuthzConfFile ) {
 
         if ( instance == null )
             instance = new AuthorizationEngine( papAuthzConfFile );
 
         return instance;
     }
 
     public static AuthorizationEngine instance() {
 
         if ( instance == null )
            logger.warn( "Authorization engine hasn't been properly initialized!" );
            
 
         return instance;
     }
 
     public void saveConfiguration() {
 
         String confFileName = PAPConfiguration.instance()
                 .getPapAuthzConfigurationFileName();
 
         AuthzConfigurationParser confParser = AuthzConfigurationParser
                 .instance();
 
         confParser.save( new File( confFileName ), getGlobalContext().getAcl() );
     }
 
     public boolean isInitialized() {
 
         return initialized;
     }
 
     public PAPContext getGlobalContext() {
 
         return globalContext;
     }
     
     public void shutdown(){
         
         // Cleanup VOMS lib - this will not be required after future voms api refactoring
         CurrentAdmin.validator.cleanup();
     }
 
 }
