 package org.glite.authz.pap.authz;
 
 import java.io.File;
 
import org.glite.authz.pap.authz.exceptions.PAPAuthzException;
 import org.glite.authz.pap.common.PAPConfiguration;
 import org.glite.authz.pap.common.exceptions.PAPConfigurationException;
 
 public class AuthorizationEngine {
 
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
     }
 
     public static AuthorizationEngine initialize( String papAuthzConfFile ) {
 
         if ( instance == null )
             instance = new AuthorizationEngine( papAuthzConfFile );
 
         return instance;
     }
 
     public static AuthorizationEngine instance() {
 
         if ( instance == null )
            throw new PAPAuthzException(
                    "Please initialize the authorization engine properly using the initialize method!" );
 
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
