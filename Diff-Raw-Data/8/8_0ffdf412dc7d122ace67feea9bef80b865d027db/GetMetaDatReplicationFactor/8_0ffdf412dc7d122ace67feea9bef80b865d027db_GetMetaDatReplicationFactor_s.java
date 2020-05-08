 package org.h2.command.h2o;
 
 import java.sql.SQLException;
 import java.util.Map;
 import java.util.Set;
 
 import org.h2.command.Prepared;
 import org.h2.engine.Session;
 import org.h2.result.LocalResult;
 import org.h2o.db.id.DatabaseID;
 import org.h2o.db.id.TableInfo;
 import org.h2o.db.query.TableProxyManager;
 import org.h2o.util.exceptions.MovedException;
 
 import uk.ac.standrews.cs.nds.rpc.RPCException;
 
 /**
  * Returns the replication factor of a given table.
  * 
  * Syntax: GET META-REPLICATION FACTOR <table_name>  -- table name optional.
  * @author angus
  *
  */
 public class GetMetaDatReplicationFactor extends Prepared {
 
     private final String tableName;
 
     public GetMetaDatReplicationFactor(final Session session, final String tableName) {
 
         super(session, true);
         this.tableName = tableName;
     }
 
     @Override
     public int update() throws SQLException, RPCException {
 
         try {
            if (tableName == null) {
                 final int currentReplicationFactor = session.getDatabase().getSystemTable().getCurrentSystemTableReplication();
 
                 return currentReplicationFactor;
             }
             else {
                 final TableInfo ti = new TableInfo("PUBLIC." + tableName);
 
                 final Map<TableInfo, Set<DatabaseID>> replicaLocations = session.getDatabase().getSystemTable().getReplicaLocations();
                 final Set<DatabaseID> replicaLocationsForTi = replicaLocations.get(ti);
 
                 int currentReplicationFactor = 0;
 
                 if (replicaLocationsForTi != null) {
                     currentReplicationFactor = replicaLocationsForTi.size();
                 }
 
                 return currentReplicationFactor;
             }
         }
         catch (final MovedException e) {
             e.printStackTrace();
         }
 
         return -1; //indicating failure
     }
 
     @Override
     public void acquireLocks(final TableProxyManager tableProxyManager) throws SQLException {
 
         //No lock required.
     }
 
     /*
      * (non-Javadoc)
      * @see org.h2.command.Prepared#isTransactionCommand()
      */
     @Override
     public boolean isTransactionCommand() {
 
         return true;
     }
 
     @Override
     public boolean isTransactional() {
 
         return false;
     }
 
     @Override
     public LocalResult queryMeta() throws SQLException {
 
         // TODO Auto-generated method stub
         return null;
     }
 
 }
