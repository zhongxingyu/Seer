 import java.security.Permission;
 
// Copy-&-pasted (almost) from
// http://www.jroller.com/ethdsy/entry/disabling_system_exit
 class ForbidSystemExit
 {
   public static class Exception extends SecurityException { }
 
   public static void apply() {
     final SecurityManager securityManager = new SecurityManager() {
       public void checkPermission( Permission permission ) {
        if( permission.getName().startsWith("exitVM") ) {
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
