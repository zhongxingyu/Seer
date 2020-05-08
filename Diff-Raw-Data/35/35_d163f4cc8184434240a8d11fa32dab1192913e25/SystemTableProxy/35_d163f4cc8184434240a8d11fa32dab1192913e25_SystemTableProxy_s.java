 package org.h2o.db.manager;
 
 import java.net.InetSocketAddress;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 import java.util.SortedSet;
 
 import org.h2o.autonomic.decision.ranker.metric.Metric;
 import org.h2o.autonomic.numonic.ranking.MachineMonitoringData;
 import org.h2o.db.H2OMarshaller;
 import org.h2o.db.id.DatabaseID;
 import org.h2o.db.id.TableInfo;
 import org.h2o.db.interfaces.IDatabaseInstanceRemote;
 import org.h2o.db.interfaces.ITableManagerRemote;
 import org.h2o.db.manager.interfaces.ISystemTable;
 import org.h2o.db.manager.interfaces.ISystemTableMigratable;
 import org.h2o.db.wrappers.DatabaseInstanceWrapper;
 import org.h2o.db.wrappers.TableManagerWrapper;
 import org.h2o.util.exceptions.MigrationException;
 import org.h2o.util.exceptions.MovedException;
 import org.json.JSONWriter;
 
 import uk.ac.standrews.cs.nds.rpc.RPCException;
 import uk.ac.standrews.cs.nds.rpc.stream.Connection;
 import uk.ac.standrews.cs.nds.rpc.stream.JSONReader;
 import uk.ac.standrews.cs.nds.rpc.stream.Marshaller;
 import uk.ac.standrews.cs.nds.rpc.stream.StreamProxy;
 import uk.ac.standrews.cs.nds.util.ErrorHandling;
 import uk.ac.standrews.cs.stachord.interfaces.IChordRemoteReference;
 
 public class SystemTableProxy extends StreamProxy implements ISystemTableMigratable {
 
     private static final Map<InetSocketAddress, SystemTableProxy> proxy_map;
     private final H2OMarshaller marshaller;
 
     static {
         proxy_map = new HashMap<InetSocketAddress, SystemTableProxy>();
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     protected SystemTableProxy(final InetSocketAddress node_address) {
 
         super(node_address);
         marshaller = new H2OMarshaller();
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     public static synchronized SystemTableProxy getProxy(final InetSocketAddress proxy_address) {
 
         SystemTableProxy proxy = proxy_map.get(proxy_address);
         if (proxy == null) {
             proxy = new SystemTableProxy(proxy_address);
             proxy_map.put(proxy_address, proxy);
         }
         return proxy;
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     @Override
     public Marshaller getMarshaller() {
 
         return marshaller;
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     @Override
     public TableManagerWrapper lookup(final TableInfo ti) throws RPCException, MovedException {
 
         try {
 
             final Connection connection = (Connection) startCall("lookup");
             final JSONWriter jw = connection.getJSONwriter();
 
             marshaller.serializeTableInfo(ti, jw);
 
             final JSONReader reader = makeCall(connection);
             final TableManagerWrapper result = marshaller.deserializeTableManagerWrapper(reader);
             finishCall(connection);
             return result;
         }
         catch (final MovedException e) {
             throw e;
         }
         catch (final Exception e) {
             ErrorHandling.exceptionError(e, "Error looking up a table manager location.");
             dealWithException(e);
             return null; // not reached
         }
     }
 
     @Override
     public boolean exists(final TableInfo ti) throws RPCException, MovedException {
 
         try {
             final Connection connection = (Connection) startCall("exists");
             final JSONWriter jw = connection.getJSONwriter();
 
             marshaller.serializeTableInfo(ti, jw);
 
             final JSONReader reader = makeCall(connection);
             final boolean result = reader.booleanValue();
             finishCall(connection);
             return result;
         }
         catch (final MovedException e) {
             throw e;
         }
         catch (final Exception e) {
             dealWithException(e);
             return false; // not returned
         }
     }
 
     @Override
     public boolean addTableInformation(final ITableManagerRemote tableManager, final TableInfo tableDetails, final Set<DatabaseInstanceWrapper> replicaLocations) throws RPCException, MovedException, SQLException {
 
         try {
             final Connection connection = (Connection) startCall("addTableInformation");
             final JSONWriter jw = connection.getJSONwriter();
             marshaller.serializeITableManagerRemote(tableManager, jw);
             marshaller.serializeTableInfo(tableDetails, jw);
             marshaller.serializeCollectionDatabaseInstanceWrapper(replicaLocations, jw);
             final JSONReader reader = makeCall(connection);
             final boolean result = reader.booleanValue();
             finishCall(connection);
             return result;
         }
         catch (final MovedException e) {
             throw e;
         }
         catch (final SQLException e) {
             throw e;
         }
         catch (final Exception e) {
             dealWithException(e);
             return false; // not returned
         }
     }
 
     @Override
     public boolean removeTableInformation(final TableInfo ti) throws RPCException, MovedException {
 
         try {
             final Connection connection = (Connection) startCall("removeTableInformation");
             final JSONWriter jw = connection.getJSONwriter();
             marshaller.serializeTableInfo(ti, jw);
             final JSONReader reader = makeCall(connection);
             final boolean result = reader.booleanValue();
             finishCall(connection);
             return result;
         }
         catch (final MovedException e) {
             throw e;
         }
         catch (final Exception e) {
             dealWithException(e);
             return false; // not reached
         }
     }
 
     @Override
     public int addConnectionInformation(final DatabaseID databaseID, final DatabaseInstanceWrapper databaseInstanceWrapper) throws RPCException, MovedException, SQLException {
 
         try {
             final Connection connection = (Connection) startCall("addConnectionInformation");
             final JSONWriter jw = connection.getJSONwriter();
             marshaller.serializeDatabaseID(databaseID, jw);
             marshaller.serializeDatabaseInstanceWrapper(databaseInstanceWrapper, jw);
             final JSONReader reader = makeCall(connection);
             final int result = reader.intValue();
             finishCall(connection);
             return result;
         }
         catch (final MovedException e) {
             throw e;
         }
         catch (final SQLException e) {
             throw e;
         }
         catch (final Exception e) {
             dealWithException(e);
             return -1; // not returned
         }
     }
 
     @Override
     public int getNewTableSetNumber() throws RPCException, MovedException {
 
         try {
             final Connection connection = (Connection) startCall("getNewTableSetNumber");
             final JSONReader reader = makeCall(connection);
             final int result = reader.intValue();
             finishCall(connection);
             return result;
         }
         catch (final MovedException e) {
             throw e;
         }
         catch (final Exception e) {
             dealWithException(e);
             return -1; // not returned
         }
     }
 
     @Override
     public Set<String> getAllTablesInSchema(final String schemaName) throws RPCException, MovedException {
 
         try {
             final Connection connection = (Connection) startCall("getAllTablesInSchema");
             final JSONWriter jw = connection.getJSONwriter();
             jw.value(schemaName);
             final JSONReader reader = makeCall(connection);
             final Set<String> result = marshaller.deserializeSetString(reader);
             finishCall(connection);
             return result;
         }
         catch (final MovedException e) {
             throw e;
         }
         catch (final Exception e) {
             dealWithException(e);
             return null; // not returned
         }
     }
 
     @Override
     public void recreateSystemTable(final ISystemTable otherSystemTable) throws RPCException, MovedException, SQLException {
 
         if (otherSystemTable instanceof ISystemTableMigratable) {
             final ISystemTableMigratable migratableSystemTable = (ISystemTableMigratable) otherSystemTable;
 
             try {
                 final Connection connection = (Connection) startCall("recreateSystemTable");
                 final JSONWriter jw = connection.getJSONwriter();
                 marshaller.serializeISystemTableMigratable(migratableSystemTable, jw);
                 handleVoidCall(makeCall(connection));
                 finishCall(connection);
             }
             catch (final MovedException e) {
                 throw e;
             }
             catch (final SQLException e) {
                 throw e;
             }
             catch (final Exception e) {
                 dealWithException(e);
             }
         }
         else {
             throw new RPCException("Tried to serialize an implementing class of ISystemTable that was not migratable.");
         }
     }
 
     @Override
     public void recreateInMemorySystemTableFromLocalPersistedState() throws RPCException, MovedException, SQLException {
 
         try {
             final Connection connection = (Connection) startCall("recreateInMemorySystemTableFromLocalPersistedState");
             handleVoidCall(makeCall(connection));
             finishCall(connection);
         }
         catch (final MovedException e) {
             throw e;
         }
         catch (final SQLException e) {
             throw e;
         }
         catch (final Exception e) {
             dealWithException(e);
         }
     }
 
     @Override
     public Map<DatabaseID, DatabaseInstanceWrapper> getConnectionInformation() throws RPCException, MovedException, SQLException {
 
         try {
             final Connection connection = (Connection) startCall("getConnectionInformation");
             final JSONReader reader = makeCall(connection);
 
             final Map<DatabaseID, DatabaseInstanceWrapper> result = marshaller.deserializeMapDatabaseIDDatabaseInstanceWrapper(reader);
             finishCall(connection);
             return result;
         }
         catch (final MovedException e) {
             throw e;
         }
         catch (final SQLException e) {
             throw e;
         }
         catch (final Exception e) {
             dealWithException(e);
             return null; //not reached
         }
     }
 
     @Override
     public Map<TableInfo, TableManagerWrapper> getTableManagers() throws RPCException, MovedException {
 
         try {
             final Connection connection = (Connection) startCall("getTableManagers");
             final JSONReader reader = makeCall(connection);
 
             final Map<TableInfo, TableManagerWrapper> result = marshaller.deserializeMapTableInfoTableManagerWrapper(reader);
             finishCall(connection);
             return result;
         }
         catch (final MovedException e) {
             throw e;
         }
         catch (final Exception e) {
             dealWithException(e);
             return null; //not reached
         }
     }
 
     @Override
     public Map<TableInfo, Set<DatabaseID>> getReplicaLocations() throws RPCException, MovedException {
 
         try {
             final Connection connection = (Connection) startCall("getReplicaLocations");
             final JSONReader reader = makeCall(connection);
 
             final Map<TableInfo, Set<DatabaseID>> result = marshaller.deserializeMapTableInfoSetDatabaseID(reader);
             finishCall(connection);
             return result;
         }
         catch (final MovedException e) {
             throw e;
         }
         catch (final Exception e) {
             dealWithException(e);
             return null; //not reached
         }
     }
 
     @Override
     public void removeAllTableInformation() throws RPCException, MovedException {
 
         try {
             final Connection connection = (Connection) startCall("removeAllTableInformation");
             handleVoidCall(makeCall(connection));
 
             finishCall(connection);
         }
         catch (final MovedException e) {
             throw e;
         }
         catch (final Exception e) {
             dealWithException(e);
         }
     }
 
     @Override
     public IDatabaseInstanceRemote getDatabaseInstance(final DatabaseID databaseID) throws RPCException, MovedException {
 
         try {
             final Connection connection = (Connection) startCall("getDatabaseInstance");
             final JSONWriter jw = connection.getJSONwriter();
             marshaller.serializeDatabaseID(databaseID, jw);
            
             final JSONReader reader = makeCall(connection);
             final IDatabaseInstanceRemote result = marshaller.deserializeIDatabaseInstanceRemote(reader);
 
             finishCall(connection);
             return result;
         }
         catch (final MovedException e) {
             throw e;
         }
         catch (final Exception e) {
             dealWithException(e);
             return null; //not reached
         }
     }
 
     @Override
     public Set<DatabaseInstanceWrapper> getDatabaseInstances() throws RPCException, MovedException {
 
         try {
             final Connection connection = (Connection) startCall("getDatabaseInstances");
             final JSONReader reader = makeCall(connection);
 
             final Set<DatabaseInstanceWrapper> result = marshaller.deserializeSetDatabaseInstanceWrapper(reader);
             finishCall(connection);
             return result;
         }
         catch (final MovedException e) {
             throw e;
         }
         catch (final Exception e) {
             dealWithException(e);
             return null; //not reached
         }
     }
 
     @Override
     public void removeConnectionInformation(final IDatabaseInstanceRemote localDatabaseInstance) throws RPCException, MovedException {
 
         try {
             final Connection connection = (Connection) startCall("removeConnectionInformation");
             final JSONWriter jw = connection.getJSONwriter();
             marshaller.serializeIDatabaseInstanceRemote(localDatabaseInstance, jw);
             handleVoidCall(makeCall(connection));
 
             finishCall(connection);
         }
         catch (final MovedException e) {
             throw e;
         }
         catch (final Exception e) {
             dealWithException(e);
         }
     }
 
     @Override
     public Set<TableManagerWrapper> getLocalDatabaseInstances(final DatabaseID localMachineLocation) throws RPCException, MovedException {
 
         try {
             final Connection connection = (Connection) startCall("getLocalDatabaseInstances");
             final JSONWriter jw = connection.getJSONwriter();
             marshaller.serializeDatabaseID(localMachineLocation, jw);
             final JSONReader reader = makeCall(connection);
 
             final Set<TableManagerWrapper> result = marshaller.deserializeSetTableManagerWrapper(reader);
             finishCall(connection);
             return result;
         }
         catch (final MovedException e) {
             throw e;
         }
         catch (final Exception e) {
             dealWithException(e);
             return null; //not reached
         }
     }
 
     @Override
     public void changeTableManagerLocation(final ITableManagerRemote stub, final TableInfo tableInfo) throws RPCException, MovedException {
 
         try {
             final Connection connection = (Connection) startCall("changeTableManagerLocation");
             final JSONWriter jw = connection.getJSONwriter();
             marshaller.serializeITableManagerRemote(stub, jw);
             marshaller.serializeTableInfo(tableInfo, jw);
             handleVoidCall(makeCall(connection));
 
             finishCall(connection);
         }
         catch (final MovedException e) {
             throw e;
         }
         catch (final Exception e) {
             dealWithException(e);
         }
     }
 
     @Override
     public void addTableManagerStateReplica(final TableInfo table, final DatabaseID replicaLocation, final DatabaseID primaryLocation, final boolean active) throws RPCException, MovedException {
 
         try {
             final Connection connection = (Connection) startCall("addTableManagerStateReplica");
             final JSONWriter jw = connection.getJSONwriter();
             marshaller.serializeTableInfo(table, jw);
             marshaller.serializeDatabaseID(replicaLocation, jw);
             marshaller.serializeDatabaseID(primaryLocation, jw);
             jw.value(active);
             handleVoidCall(makeCall(connection));
 
             finishCall(connection);
         }
         catch (final MovedException e) {
             throw e;
         }
         catch (final Exception e) {
             dealWithException(e);
         }
     }
 
     @Override
     public Map<TableInfo, DatabaseID> getPrimaryLocations() throws RPCException, MovedException {
 
         try {
             final Connection connection = (Connection) startCall("getPrimaryLocations");
             final JSONReader reader = makeCall(connection);
 
             final Map<TableInfo, DatabaseID> result = marshaller.deserializeMapTableInfoDatabaseID(reader);
             finishCall(connection);
             return result;
         }
         catch (final MovedException e) {
             throw e;
         }
         catch (final Exception e) {
             dealWithException(e);
             return null; //not reached
         }
     }
 
     @Override
     public void removeTableManagerStateReplica(final TableInfo table, final DatabaseID replicaLocation) throws RPCException, MovedException {
 
         try {
             final Connection connection = (Connection) startCall("removeTableManagerStateReplica");
             final JSONWriter jw = connection.getJSONwriter();
             marshaller.serializeTableInfo(table, jw);
             marshaller.serializeDatabaseID(replicaLocation, jw);
             handleVoidCall(makeCall(connection));
 
             finishCall(connection);
         }
         catch (final MovedException e) {
             throw e;
         }
         catch (final Exception e) {
             dealWithException(e);
         }
     }
 
     @Override
     public ITableManagerRemote recreateTableManager(final TableInfo table) throws RPCException, MovedException {
 
         try {
             final Connection connection = (Connection) startCall("recreateTableManager");
             final JSONWriter jw = connection.getJSONwriter();
             marshaller.serializeTableInfo(table, jw);
             final JSONReader reader = makeCall(connection);
 
             final ITableManagerRemote result = marshaller.deserializeITableManagerRemote(reader);
             finishCall(connection);
             return result;
         }
         catch (final MovedException e) {
             throw e;
         }
         catch (final Exception e) {
             dealWithException(e);
             return null; //not reached
         }
     }
 
     @Override
     public boolean checkTableManagerAccessibility() throws RPCException, MovedException {
 
         try {
             final Connection connection = (Connection) startCall("checkTableManagerAccessibility");
             final JSONReader reader = makeCall(connection);
 
             final boolean result = reader.booleanValue();
             finishCall(connection);
             return result;
         }
         catch (final MovedException e) {
             throw e;
         }
         catch (final Exception e) {
             dealWithException(e);
             return false; // not reached
         }
     }
 
     @Override
     public Queue<DatabaseInstanceWrapper> getAvailableMachines(final Metric typeOfRequest) throws RPCException, MovedException {
 
         try {
             final Connection connection = (Connection) startCall("getAvailableMachines");
             final JSONWriter jw = connection.getJSONwriter();
            marshaller.serializeActionRequest(typeOfRequest, jw); 
             final JSONReader reader = makeCall(connection);
 
             final Queue<DatabaseInstanceWrapper> result = marshaller.deserializeQueueDatabaseInstanceWrapper(reader);
             finishCall(connection);
             return result;
         }
         catch (final MovedException e) {
             throw e;
         }
         catch (final Exception e) {
             dealWithException(e);
             return null; //not reached
         }
     }
 
     @Override
     public void prepareForMigration(final String newLocation) throws RPCException, MigrationException, MovedException {
 
         try {
             final Connection connection = (Connection) startCall("prepareForMigration");
             final JSONWriter jw = connection.getJSONwriter();
             jw.value(newLocation);
             handleVoidCall(makeCall(connection));
            
             finishCall(connection);
         }
         catch (final MovedException e) {
             throw e;
         }
         catch (final Exception e) {
             dealWithException(e);
         }
     }
 
     @Override
     public void checkConnection() throws RPCException, MovedException {
 
         try {
             final Connection connection = (Connection) startCall("checkConnection");
             handleVoidCall(makeCall(connection));
             finishCall(connection);
         }
         catch (final MovedException e) {
             throw e;
         }
         catch (final Exception e) {
             dealWithException(e);
         }
     }
 
     @Override
     public void completeMigration() throws RPCException, MovedException, MigrationException {
 
         try {
             final Connection connection = (Connection) startCall("completeMigration");
             handleVoidCall(makeCall(connection));
             finishCall(connection);
         }
         catch (final MovedException e) {
             throw e;
         }
         catch (final Exception e) {
             dealWithException(e);
         }
     }
 
     @Override
     public void shutdown(final boolean shutdown) throws RPCException, MovedException {
 
         try {
             final Connection connection = (Connection) startCall("shutdown");
             final JSONWriter jw = connection.getJSONwriter();
             jw.value(shutdown);
            handleVoidCall( makeCall(connection));
             finishCall(connection);
         }
         catch (final MovedException e) {
             throw e;
         }
         catch (final Exception e) {
             dealWithException(e);
         }
     }
 
     @Override
     public IChordRemoteReference getChordReference() throws RPCException {
 
         try {
             final Connection connection = (Connection) startCall("getChordReference");
             final JSONReader reader = makeCall(connection);
             final IChordRemoteReference result = marshaller.deserializeChordRemoteReference(reader);
             finishCall(connection);
 
             return result;
         }
         catch (final Exception e) {
             dealWithException(e);
             return null; // not reached
         }
     }
 
     @Override
     public boolean isAlive() throws RPCException, MovedException {
 
         try {
             final Connection connection = (Connection) startCall("isAlive");
             final JSONReader reader = makeCall(connection);
             final boolean result = reader.booleanValue();
             finishCall(connection);
 
             return result;
         }
         catch (final Exception e) {
             dealWithException(e);
             return false; // not reached
         }
     }
 
     @Override
     public InetSocketAddress getAddress() throws RPCException {
 
         return super.node_address;
     }
 
     @Override
     public void addMonitoringSummary(final MachineMonitoringData summary) throws RPCException, MovedException {
 
         try {
             final Connection connection = (Connection) startCall("addMonitoringSummary");
             final JSONWriter jw = connection.getJSONwriter();
             marshaller.serializeMachineMonitoringData(summary, jw);
            handleVoidCall( makeCall(connection));
             finishCall(connection);
         }
         catch (final MovedException e) {
             throw e;
         }
         catch (final Exception e) {
             dealWithException(e);
         }
     }
 
     @Override
     public SortedSet<MachineMonitoringData> getRankedListOfInstances() {
 
         // TODO Auto-generated method stub
         return null;
     }
 }
