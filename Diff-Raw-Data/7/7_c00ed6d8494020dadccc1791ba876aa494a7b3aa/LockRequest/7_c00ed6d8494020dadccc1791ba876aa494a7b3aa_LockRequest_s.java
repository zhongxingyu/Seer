 package org.h2o.db.query.locking;
 
 import java.io.Serializable;
 
 import org.h2.engine.Session;
 import org.h2o.db.wrappers.DatabaseInstanceWrapper;
 
 public class LockRequest implements Serializable {
 
     private static final long serialVersionUID = 468660463533445063L;
 
     private final DatabaseInstanceWrapper databaseMakingRequest;
    private Session session = null;
     private final int sessionID;
 
     /**
      * @param sessionID this should be the value from session.getSerialID().
      */
     public LockRequest(final DatabaseInstanceWrapper databaseMakingRequest, final int sessionID) {
 
         this.databaseMakingRequest = databaseMakingRequest;
         this.sessionID = sessionID;
     }
 
     public LockRequest(final Session session) {
 
         this(session.getDatabase().getLocalDatabaseInstanceInWrapper(), session.getSessionId());
        this.session = session;
     }
 
     public DatabaseInstanceWrapper getRequestLocation() {
 
         return databaseMakingRequest;
     }
 
     @Override
     public int hashCode() {
 
         final int prime = 31;
         int result = 1;
         result = prime * result + (databaseMakingRequest == null ? 0 : databaseMakingRequest.hashCode());
         result = prime * result + sessionID;
         return result;
     }
 
     @Override
     public boolean equals(final Object other) {
 
         try {
             final LockRequest other_request = (LockRequest) other;
 
             return other_request != null && (databaseMakingRequest == other_request.databaseMakingRequest || databaseMakingRequest != null && databaseMakingRequest.equals(other_request.databaseMakingRequest)) && sessionID == other_request.sessionID;
         }
         catch (final ClassCastException e) {
             return false;
         }
     }
 
     @Override
     public String toString() {
 
        final String user = session != null ? session.getUser().getName() : "unknown";
         return "LockRequest [databaseMakingRequest=" + databaseMakingRequest + ", sessionID=" + sessionID + ", user=" + user + "]";
     }
 }
