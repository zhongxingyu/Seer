 package org.glite.authz.pap.authz;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.glite.authz.pap.authz.exceptions.PAPAuthzException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Base operation implementing all the authorization steps and checks needed for
  * authorized management operations on the PAP.
  * 
 * Subclasses should implement the operation behaviour in the {@link #doExecute()}
  * method, that is called after all authorization checks have been done.
  * 
  * Required permission for operations should be defined by overriding the abstract
  * {@link #setupPermissions()} method and using the {@link #addRequiredPermission(PAPPermission)}
  * method.
  * 
  * @param <T>, the type of the returned value of the execution of this operation
  */
 public abstract class BasePAPOperation<T> implements PAPOperation <T> {
 
     protected Logger log = LoggerFactory.getLogger( BasePAPOperation.class );
 
     /** The map containing the required permissions for this operation **/
     protected Map <PAPContext, PAPPermission> requiredPermission;
 
     /** Constructor */
     protected BasePAPOperation() {
 
         requiredPermission = new HashMap <PAPContext, PAPPermission>();
     }
 
     /** Simple convenience method to check whether required permissions have been
      * initialized for this operation 
      * 
      */
     private boolean permissionsInitialized() {
 
         if ( requiredPermission == null || requiredPermission.isEmpty() )
             return false;
 
         return true;
 
     }
 
     /**
      * 
      * {@inheritDoc}
      * 
      * 
      */
     public final T execute() {
 
         logOperation();
 
         if ( !isAllowed() ) {
 
             log.info( "Insufficient privileges to perform operation '"
                     + getName() + "'" );
 
             throw new PAPAuthzException(
                     "Insufficient privileges to perform operation '"
                             + getName() + "'." );
         }
 
         return doExecute();
     }
 
     /**
      * {@inheritDoc}
      */
     public Map <PAPContext, PAPPermission> getRequiredPermission() {
 
         if ( !permissionsInitialized() )
             setupPermissions();
 
         return requiredPermission;
     }
 
     /**
      * The operation behaviour must be implemented ovverriding this method. 
      * 
      * @return the result of the operation
      */
     protected abstract T doExecute();
 
     /**
      * Adds a required permission for this operation
      * @param context, the context in which the permission is required
      * @param perms, the required permission
      */
     protected final void addRequiredPermission( PAPContext context,
             PAPPermission perms ) {
 
         assert context != null : "Cannot add permission for a null context!";
         assert perms != null : "Cannot add null permissions for a context!";
 
         requiredPermission.put( context, perms );
 
     }
 
     /**
      * Adds a required permission for this operation in the PAP default context
      * (ie, the global context).
      * 
      * @param perms, the required permission
      */
     protected final void addRequiredPermission( PAPPermission perms ) {
 
         assert perms != null : "Cannot add null permissions for the global context!";
         assert AuthorizationEngine.instance().isInitialized() : "Authz engine not initialized! Cannot get global context!";
 
         requiredPermission.put( AuthorizationEngine.instance()
                 .getGlobalContext(), perms );
     }
 
     /**
      * Returns a string representation of the name of the operation.
      * 
      * @return a string representation of the name of the operation.
      */
     protected String getName() {
 
         return this.getClass().getSimpleName();
 
     }
 
     /**
      * Checks that the execution of this operation is allowed for the {@link CurrentAdmin}
      * trying to execute it.
      * 
      * This method (together with the {@link CurrentAdmin#hasPermissions(PAPContext, PAPPermission)} method
      * is the heart of the PAP authorization engine, so be careful when putting your hands in it.
      * 
      * Basically the implementation is very simple. The {@link CurrentAdmin} permissions
      * are checked so that they match the operation's required permissions in every context.
      * 
      * If all checks are successful, <code>true</code> is returned, otherwise <code>false</code>.
      * 
      * @return <code>true</code> if the operation is allowed, <code>false</code> otherwise.
      */
     public final boolean isAllowed() {
 
         CurrentAdmin admin = CurrentAdmin.instance();
 
         if ( !permissionsInitialized() )
             setupPermissions();
 
         if ( requiredPermission.isEmpty() )
             throw new PAPAuthzException(
                     "No required permissions defined for operation '"
                             + getName() + "'." );
 
         for ( Map.Entry <PAPContext, PAPPermission> entry : requiredPermission
                 .entrySet() ) {
 
             PAPContext context = entry.getKey();
             ACL acl = entry.getKey().getAcl();
             PAPPermission perms = entry.getValue();
 
             if ( acl == null )
                 throw new PAPAuthzException( "No ACL defined for context '"
                         + entry.getKey() + "'." );
 
             if ( !admin.hasPermissions( context, perms ) )
                 return false;
 
         }
 
         return true;
 
     }
 
     /**
      * Logs messages about the operation execution
      */
     protected void logOperation() {
 
         // TODO: implement me!
     }
 
     /**
      * Sets up the required permissions for this operation. 
      */
     protected abstract void setupPermissions();
 
 }
