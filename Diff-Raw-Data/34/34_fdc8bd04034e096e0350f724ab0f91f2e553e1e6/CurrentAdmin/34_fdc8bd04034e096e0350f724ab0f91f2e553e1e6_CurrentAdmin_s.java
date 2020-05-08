 package org.glite.authz.pap.authz;
 
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.glite.security.SecurityContext;
 import org.glite.voms.VOMSAttribute;
 import org.glite.voms.VOMSValidator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class CurrentAdmin {
 
     static Logger log = LoggerFactory.getLogger( CurrentAdmin.class );
     
    static VOMSValidator validator = new VOMSValidator((X509Certificate)null);
 
     private PAPAdmin papAdmin;
 
     private VOMSFQAN[] fqans;
 
     protected CurrentAdmin( PAPAdmin admin, VOMSFQAN[] fqans ) {
 
         this.papAdmin = admin;
         this.fqans = fqans;
     }
 
     public static CurrentAdmin instance() {
 
         SecurityContext theContext = SecurityContext.getCurrentContext();
 
         String adminDN = theContext.getClientName();
 
         X509Principal papAdmin = PAPAdminFactory.getDn( adminDN );
 
         if ( papAdmin == null )
             papAdmin = PAPAdminFactory.getAnyAuthenticatedUserAdmin();
 
         // Get VOMS attributes here
         validator.setClientChain( theContext
                 .getClientCertChain() );
         try {
 
             validator.validate();
             
             List<VOMSAttribute> attrs = validator.getVOMSAttributes();
             
             List <VOMSFQAN> myFQANs = new ArrayList <VOMSFQAN>();
             
             for ( VOMSAttribute voAttr : attrs ) {
 
                 List <String> fqanAttrs = voAttr.getFullyQualifiedAttributes();
 
                 if ( fqanAttrs.size() > 0 ) {
 
                     for ( String f : fqanAttrs )
                         myFQANs.add( PAPAdminFactory.getFQAN( f ) );
 
                     if ( log.isDebugEnabled() ) {
 
                         String fqansString = StringUtils.join( myFQANs, "," );
                         log.debug( "X509Principal: '{}' has valid fqans: '{}'",
                                 papAdmin, fqansString );
                     }
 
                 }
 
             }
             
             if (myFQANs.size() > 0)    
                 return new CurrentAdmin(papAdmin, (VOMSFQAN[])myFQANs.toArray( new VOMSFQAN[0] ));
             else
                 log.debug( "No VOMS AC found in client certificate chain" );
 
         } catch ( Throwable t ) {
 
             log.warn(
                     "Error validating voms attributes out of the cert chain:",
                     t.getMessage() );
         }
 
         return new CurrentAdmin( papAdmin, null );
     }
 
     public boolean hasPermissions( PAPContext context,
             PAPPermission requiredPerms ) {
 
         PAPPermissionList currentAdminPermList = PAPPermissionList.instance();
 
         PAPPermission adminPerms = context.getAcl().getPermissions().get(
                 papAdmin );
         PAPPermission anyUserPerms = context.getAcl()
                 .getAnyAuthenticatedUserPermissions();
 
         currentAdminPermList.addPermission( adminPerms );
         currentAdminPermList.addPermission( anyUserPerms );
 
         if ( fqans != null ) {
 
             for ( VOMSFQAN fqan : fqans ) {
 
                 PAPPermission fqanPerms = context.getAcl().getPermissions()
                         .get( fqan );
 
                 if ( fqanPerms != null ) {
                     currentAdminPermList.addPermission( fqanPerms );
                     log.debug( "Adding permissions '{}' for fqan '{}'",
                             fqanPerms, fqan );
                 }
 
             }
 
         }
 
         if ( log.isDebugEnabled() ) {
             log.debug( String.format(
                     "Checking perms '%s' for admin '%s' in context '%s'",
                     requiredPerms, papAdmin, context ) );
             log.debug( String.format( "CurrrentAdminPerms: '%s'",
                     currentAdminPermList ) );
         }
 
         return currentAdminPermList.satisfies( requiredPerms );
 
     }
 
 }
