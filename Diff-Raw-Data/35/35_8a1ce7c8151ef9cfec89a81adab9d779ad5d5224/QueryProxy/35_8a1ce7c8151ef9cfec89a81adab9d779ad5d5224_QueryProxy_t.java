 /*
  * Copyright (C) 2009-2010 School of Computer Science, University of St Andrews. All rights reserved.
  * Project Homepage: http://blogs.cs.st-andrews.ac.uk/h2o
  *
  * H2O is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * H2O is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with H2O.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.h2o.db.query;
 
 import java.io.Serializable;
 import java.rmi.RemoteException;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.h2.engine.Database;
 import org.h2.engine.Session;
 import org.h2.table.Table;
 import org.h2o.autonomic.settings.Settings;
 import org.h2o.db.id.TableInfo;
 import org.h2o.db.interfaces.DatabaseInstanceRemote;
 import org.h2o.db.interfaces.TableManagerRemote;
 import org.h2o.db.manager.TableManager;
 import org.h2o.db.manager.interfaces.ISystemTable;
 import org.h2o.db.manager.recovery.LocatorException;
 import org.h2o.db.manager.recovery.SystemTableAccessException;
 import org.h2o.db.query.asynchronous.AsynchronousQueryExecutor;
 import org.h2o.db.query.locking.LockType;
 import org.h2o.db.wrappers.DatabaseInstanceWrapper;
import org.h2o.test.H2OTest;
 import org.h2o.util.exceptions.MovedException;
 
 import uk.ac.standrews.cs.nds.util.ErrorHandling;
 
 /**
  * A proxy class used to make sending queries to multiple replicas easier. The query only needs to be sent to the query proxy, which handles
  * the rest of the transaction.
  * 
  * <p>
  * Query proxies are created by the Table Manager for a given table, and indicate permission to perform a given query. The level of this
  * permission is indicated by the type of lock granted (see lockGranted attribute).
  * 
  * @author Angus Macdonald (angus@cs.st-andrews.ac.uk)
  */
 public class QueryProxy implements Serializable {
 
 	/**
 	 * Generated serial version.
 	 */
 	private static final long serialVersionUID = -31853777345527026L;
 
 	private LockType lockGranted;
 
 	private TableInfo tableName;
 
 	private Map<DatabaseInstanceWrapper, Integer> allReplicas;
 
 	/**
 	 * Proxy for the Table Manager of this table. Used to release any locks held at the end of the transaction.
 	 */
 	private TableManagerRemote tableManager; // changed by al - this is either a
 												// local guy or an RMI reference
 
 	/**
 	 * The database instance making the request. This is used to request the lock (i.e. the lock for the given query is taken out in the
 	 * name of this database instance.
 	 */
 	private DatabaseInstanceWrapper requestingDatabase;
 
 	/**
 	 * ID assigned to this update by the Table Manager. This is returned to inform the Table Manager what replicas were updated.
 	 */
 	private int updateID;
 
 	/**
 	 * The type of lock that was requested. It may not have been granted
 	 * 
 	 * @see #lockGranted
 	 */
 	private LockType lockRequested;
 
 	/**
 	 * @param lockGranted
 	 *            The type of lock that has been granted
 	 * @param tableName
 	 *            Name of the table that is being used in the query
 	 * @param allReplicas
 	 *            Proxies for each of the replicas used in the query.
 	 * @param tableManager
 	 *            Proxy for the Table Manager of the table involved in the query (i.e. tableName).
 	 * @param updateID
 	 *            ID given to this update.
 	 */
 	public QueryProxy(LockType lockGranted, TableInfo tableName, Map<DatabaseInstanceWrapper, Integer> allReplicas, TableManager tableManager,
 			DatabaseInstanceWrapper requestingMachine, int updateID, LockType lockRequested) {
 		this.lockGranted = lockGranted;
 		this.lockRequested = lockRequested;
 		this.tableName = tableName;
 		this.allReplicas = allReplicas;
 		this.tableManager = tableManager;
 		this.requestingDatabase = requestingMachine;
 		this.updateID = updateID;
 	}
 
 	/**
 	 * Creates a dummy query proxy.
 	 * 
 	 * @see #getDummyQueryProxy(DatabaseInstanceRemote)
 	 * @param localDatabaseInstance
 	 */
 	public QueryProxy(DatabaseInstanceWrapper localDatabaseInstance) {
 		this.lockGranted = LockType.WRITE;
 		this.allReplicas = new HashMap<DatabaseInstanceWrapper, Integer>();
 		if (localDatabaseInstance != null) { // true when the DB hasn't started yet (management DB, etc.)
 			this.allReplicas.put(localDatabaseInstance, 0);
 		}
 		this.requestingDatabase = localDatabaseInstance;
 		this.tableName = new TableInfo("Dummy", "Table");
 	}
 
 	/**
 	 * Execute the given SQL update.
 	 * 
 	 * @param query
 	 *            The query to be executed
 	 * @param db
 	 *            Used to obtain proxies for remote database instances.
 	 * @throws SQLException
 	 */
 	public int executeUpdate(String query, String transactionNameForQuery, Session session) throws SQLException {
 
 		if (lockRequested == LockType.CREATE && (allReplicas == null || allReplicas.size() == 0)) {
 			/*
 			 * If we don't know of any replicas and this is a CREATE TABLE statement then we just run the query on the local DB instance.
 			 */
 			this.allReplicas = new HashMap<DatabaseInstanceWrapper, Integer>();
 			this.allReplicas.put(requestingDatabase, 0);
 		} else if (allReplicas == null || allReplicas.size() == 0) {
 			/*
 			 * If there are no replicas on which to execute the query.
 			 */
 			try {
 				tableManager.releaseLock(true, requestingDatabase, null, false);
 			} catch (RemoteException e) {
 				ErrorHandling.exceptionError(e, "Failed to release lock - couldn't contact the Table Manager");
 			} catch (MovedException e) {
 				ErrorHandling.hardError("This should never happen at this point. The migrating machine should have a lock taken out.");
 			}
 			throw new SQLException("No replicas found to perform update: " + query);
 		}
 
 		/*
 		 * Execute the query. Send the query to each DB instance holding a replica.
 		 */
 
 		AsynchronousQueryExecutor queryExecutor = new AsynchronousQueryExecutor(session.getDatabase());
 		boolean globalCommit = queryExecutor.executeQuery(query, transactionNameForQuery, allReplicas, tableName,  session, false);
 
 		H2OTest.rmiFailure(); // Test code to simulate the failure of DB
 								// instances at this point.
 
 		if (!globalCommit) {
 			throw new SQLException("Commit failed on one or more replicas. The query will be rolled back.");
 		}
 
 		return 0;
 	}
 
 	/**
 	 * Obtain a query proxy for the given table.
 	 * 
 	 * @param tableName
 	 *            Table involved in query.
 	 * @param lockType
 	 *            Lock required for query
 	 * @param db
 	 *            Local database instance - needed to inform DM of: the identity of the requesting machine, and to obtain the Table Manager
 	 *            for the given table.
 	 * @return
 	 * @throws SQLException
 	 */
 	public static QueryProxy getQueryProxyAndLock(Table table, LockType lockType, Database db) throws SQLException {
 
 		// if the table is temporary it only exists as part of this transaction
 		// - i.e. no lock is needed.
 		// if one of the reserved table names is used (SYSTEM_RANGE, for
 		// example) it isn't a proper table so won't have a Table Manager.
 		if (table != null && !table.getTemporary() && !Settings.reservedTableNames.contains(table.getName())) {
 			return getQueryProxyAndLock(db, table.getFullName(), lockType, db.getLocalDatabaseInstanceInWrapper());
 		} else {
 			return getDummyQueryProxy(db.getLocalDatabaseInstanceInWrapper());
 		}
 	}
 
 	/**
 	 * Returns a dummy query proxy which indicates that it is possible to execute the query and lists the local (requesting) database as the
 	 * only replica. Used in cases where a query won't have to be propagated, or where no particular table is specified in the query (e.g.
 	 * create schema), and so it isn't possible to lock on a particular table.
 	 * 
 	 * @param localDatabaseInstance
 	 * @return
 	 */
 	public static QueryProxy getDummyQueryProxy(DatabaseInstanceWrapper localDatabaseInstance) {
 		return new QueryProxy(localDatabaseInstance);
 	}
 
 	/**
 	 * Obtain a query proxy for the given table.
 	 * 
 	 * @param tableManager
 	 * @param requestingDatabase
 	 *            DB making the request.
 	 * @return Query proxy for a specific table within H20.
 	 * @throws SQLException
 	 */
 	public static QueryProxy getQueryProxyAndLock(TableManagerRemote tableManager, String tableName, Database db, LockType lockType,
 			DatabaseInstanceWrapper requestingDatabase, boolean alreadyCalled) throws SQLException {
 
 		if (requestingDatabase == null) {
 			ErrorHandling.hardError("A requesting database must be specified.");
 		}
 
 		try {
 			QueryProxy queryProxy = null;
 			try {
 				queryProxy = tableManager.getQueryProxy(lockType, requestingDatabase);
 			} catch (MovedException e) {
 				// Get an uncached Table Manager from the System Table
 				tableManager = db.getSystemTableReference().lookup(tableName, false);
 
 				queryProxy = tableManager.getQueryProxy(lockType, requestingDatabase);
 			}
 			return queryProxy;
 		} catch (java.rmi.NoSuchObjectException e) {
 			e.printStackTrace();
 			throw new SQLException("Table Manager could not be accessed. It may not have been exported to RMI correctly.");
 		} catch (RemoteException e) {
 
 			if (!alreadyCalled) {
 
 				ISystemTable systemTable = db.getSystemTable();
 
 				boolean systemTableActive = true;
 
 				if (systemTable == null) {
 					// reInstantiateSystemTable
 					try {
 						systemTable = db.getSystemTableReference().failureRecovery();
 					} catch (LocatorException e1) {
 						ErrorHandling.exceptionError(e1, "Failed to contact locator servers.");
 					} catch (SystemTableAccessException e1) {
 						systemTableActive = false;
 						throw new SQLException("The System Table could not be re-instantiated.");
 					}
 				}
 
 				if (systemTableActive) {
 					try {
 						TableManagerRemote newTableManager = db.getSystemTable().recreateTableManager(new TableInfo(tableName));
 						if (newTableManager != null) {
 							return getQueryProxyAndLock(newTableManager, tableName, db, lockType, requestingDatabase, true);
 						}
 					} catch (RemoteException e1) {
 						e1.printStackTrace();
 					} catch (MovedException e1) {
 						e1.printStackTrace();
 					}
 				}
 			}
 
 			throw new SQLException("Table Manager could not be accessed. The table is unavailable until the Table Manager is reactivated.");
 		} catch (MovedException e) {
 			throw new SQLException("Table Manager has moved and can't be accessed at this location.");
 		}
 	}
 
 	public static QueryProxy getQueryProxyAndLock(Database db, String tableName, LockType lockType,
 			DatabaseInstanceWrapper requestingDatabase) throws SQLException {
 		TableManagerRemote tableManager = db.getSystemTableReference().lookup(tableName, true);
 
 		if (tableManager == null) {
 			ErrorHandling.errorNoEvent("Table Manager proxy was null when requesting table.");
 			db.getSystemTableReference();
 			throw new SQLException("Table Manager not found for table.");
 		}
 
 		return getQueryProxyAndLock(tableManager, tableName, db, lockType, requestingDatabase, false);
 	}
 
 	public LockType getLockGranted() {
 		return lockGranted;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 
 		if (tableManager == null) {
 			/*
 			 * This is a dummy proxy.
 			 */
 
 			return "Dummy Query Proxy: Used for local query involving a system table or another entity entirely.";
 		}
 
 		String plural = (allReplicas.size() == 1) ? "" : "s";
 		return tableName + " (" + allReplicas.size() + " replica" + plural + "), with lock '" + lockGranted + "'";
 	}
 
 	/**
 	 * @return
 	 */
 	public Map<DatabaseInstanceWrapper, Integer> getReplicaLocations() {
 		return allReplicas;
 	}
 
 	/**
 	 * @return
 	 */
 	public Map<DatabaseInstanceWrapper, Integer> getRemoteReplicaLocations() {
 
 		Map<DatabaseInstanceWrapper, Integer> remoteReplicas = new HashMap<DatabaseInstanceWrapper, Integer>(allReplicas);
 		Integer removedItem = remoteReplicas.remove(requestingDatabase);
 
 		if (removedItem == null) {
 			ErrorHandling.errorNoEvent("Tried to remove the local replica from the set of all replicas, but failed. Possibly equality check problem.");
 		}
 
 		return remoteReplicas;
 	}
 
 	/**
 	 * @return
 	 */
 	public TableManagerRemote getTableManager() {
 		return tableManager;
 	}
 
 	/**
 	 * @return
 	 */
 	public int getUpdateID() {
 		return updateID;
 	}
 
 	/**
 	 * @param write
 	 */
 	public void setLockType(LockType write) {
 		this.lockGranted = write;
 	}
 
 	/**
 	 * @return the requestingDatabase
 	 */
 	public DatabaseInstanceWrapper getRequestingDatabase() {
 		return requestingDatabase;
 	}
 
 	/**
 	 * Checks whether there is only one database in the system. Currently used in CreateReplica to ensure the system doesn't try to create a
 	 * replica of something on the same instance.
 	 * 
 	 * @return
 	 */
 	public boolean isSingleDatabase(DatabaseInstanceRemote localDatabase) {
 		return (allReplicas != null && allReplicas.size() == 1) && getRequestingDatabase() == localDatabase;
 	}
 
 	/**
 	 * Name of the table this proxy holds locks for.
 	 * 
 	 * @return
 	 */
 	public TableInfo getTableName() {
 		return tableName;
 	}
 
 	/**
 	 * @return
 	 */
 	public int getNumberOfReplicas() {
 		return (allReplicas == null) ? 0 : allReplicas.size();
 	}
 
 }
