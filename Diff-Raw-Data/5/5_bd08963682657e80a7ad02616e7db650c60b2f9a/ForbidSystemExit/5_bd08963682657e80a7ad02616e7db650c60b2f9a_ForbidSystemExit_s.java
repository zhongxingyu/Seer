 import java.security.Permission;
 
 class ForbidSystemExit
 {
   public static class Exception extends SecurityException { }
 
   public static void apply() {
     final SecurityManager securityManager = new SecurityManager() {
       public void checkPermission( Permission permission ) {
        if( "exitVM".equals( permission.getName() ) ) {
           throw new Exception() ;
         }
       }
     } ;
     System.setSecurityManager( securityManager ) ;
   }
 
   public static void unapply() {
     System.setSecurityManager( null ) ;
   }
 }
