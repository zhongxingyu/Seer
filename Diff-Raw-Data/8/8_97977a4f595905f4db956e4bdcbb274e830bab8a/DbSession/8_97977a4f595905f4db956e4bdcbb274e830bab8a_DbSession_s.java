 /**
  * This file is part of GoldenGate Project (named also GoldenGate or GG).
  * 
  * Copyright 2009, Frederic Bregier, and individual contributors by the @author
  * tags. See the COPYRIGHT.txt in the distribution for a full listing of
  * individual contributors.
  * 
  * All GoldenGate Project is free software: you can redistribute it and/or
  * modify it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  * 
  * GoldenGate is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * GoldenGate . If not, see <http://www.gnu.org/licenses/>.
  */
 package goldengate.common.database;
 
 import goldengate.common.logging.GgInternalLogger;
 import goldengate.common.logging.GgInternalLoggerFactory;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Savepoint;
 import java.util.LinkedList;
 import java.util.List;
 
 import goldengate.common.database.exception.GoldenGateDatabaseNoConnectionError;
 import goldengate.common.database.exception.GoldenGateDatabaseSqlError;
 import goldengate.common.database.model.DbModelFactory;
 
 // Notice, do not import com.mysql.jdbc.*
 // or you will have problems!
 
 /**
  * Class to handle session with the SGBD
  * 
  * @author Frederic Bregier
  * 
  */
 public class DbSession {
     /**
      * Internal Logger
      */
     private static final GgInternalLogger logger = GgInternalLoggerFactory
             .getLogger(DbSession.class);
 
     /**
      * DbAdmin referent object
      */
     public DbAdmin admin = null;
 
     /**
      * The internal connection
      */
     public Connection conn = null;
 
     /**
      * Is this connection Read Only
      */
     public boolean isReadOnly = true;
 
     /**
      * Is this session using AutoCommit (true by default)
      */
     public boolean autoCommit = true;
 
     /**
      * Internal Id
      */
     public long internalId;
 
     /**
      * Number of threads using this connection
      */
     public int nbThread = 0;
 
     /**
      * To be used when a local Channel is over
      */
     public boolean isDisconnected = true;
 
     /**
      * List all DbPrepareStatement with long term usage to enable the recreation
      * when the associated connection is reopened
      */
     private final List<DbPreparedStatement> listPreparedStatement = new LinkedList<DbPreparedStatement>();
 
     static synchronized void setInternalId(DbSession session) {
         session.internalId = System.currentTimeMillis();
         try {
             Thread.sleep(1);
         } catch (InterruptedException e) {
             Thread.currentThread().interrupt();
         }
     }
 
     /**
      * Create a session and connect the current object to the connect object
      * given as parameter.
      * 
      * The database access use auto commit.
      * 
      * If the initialize is not call before, call it with the default value.
      * 
      * @param connext
      * @param isReadOnly
      * @throws GoldenGateDatabaseNoConnectionError
      */
     public DbSession(Connection connext, boolean isReadOnly)
             throws GoldenGateDatabaseNoConnectionError {
         if (connext == null) {
             logger.error("Cannot set a null connection");
             throw new GoldenGateDatabaseNoConnectionError(
                     "Cannot set a null Connection");
         }
         conn = connext;
         try {
             conn.setAutoCommit(true);
             this.isReadOnly = isReadOnly;
             conn.setReadOnly(this.isReadOnly);
             isDisconnected = false;
             setInternalId(this);
         } catch (SQLException ex) {
             // handle any errors
             logger.error("Cannot set properties on connection!");
             error(ex);
             conn = null;
             isDisconnected = true;
             throw new GoldenGateDatabaseNoConnectionError(
                     "Cannot set properties on connection", ex);
         }
     }
 
     /**
      * Create a session and connect the current object to the server using the
      * string with the form for mysql for instance
      * jdbc:type://[host:port],[failoverhost:port]
      * .../[database][?propertyName1][
      * =propertyValue1][&propertyName2][=propertyValue2]...
      * 
      * By default (if server = null) :
      * "jdbc:mysql://localhost/r66 user=r66 password=r66"
      * 
      * The database access use auto commit.
      * 
      * If the initialize is not call before, call it with the default value.
      * 
      * @param server
      * @param user
      * @param passwd
      * @param isReadOnly
      * @throws GoldenGateDatabaseSqlError
      */
     public DbSession(String server, String user, String passwd,
             boolean isReadOnly) throws GoldenGateDatabaseNoConnectionError {
         if (!DbModelFactory.classLoaded) {
             throw new GoldenGateDatabaseNoConnectionError(
                     "DbAdmin not initialzed");
         }
         if (server == null) {
             conn = null;
             logger.error("Cannot set a null Server");
             throw new GoldenGateDatabaseNoConnectionError(
                     "Cannot set a null Server");
         }
         try {
             conn = DriverManager.getConnection(server, user, passwd);
             conn.setAutoCommit(true);
             this.isReadOnly = isReadOnly;
             conn.setReadOnly(this.isReadOnly);
             setInternalId(this);
             DbAdmin.addConnection(internalId, this);
             isDisconnected = false;
         } catch (SQLException ex) {
             // handle any errors
             isDisconnected = true;
             logger.error("Cannot create Connection");
             error(ex);
             conn = null;
             throw new GoldenGateDatabaseNoConnectionError(
                     "Cannot create Connection", ex);
         }
     }
 
     /**
      * Create a session and connect the current object to the server using the
      * DbAdmin object. The database access use auto commit.
      * 
      * If the initialize is not call before, call it with the default value.
      * 
      * @param admin
      * @param isReadOnly
      * @throws GoldenGateDatabaseSqlError
      */
     public DbSession(DbAdmin admin, boolean isReadOnly)
             throws GoldenGateDatabaseNoConnectionError {
         if (!DbModelFactory.classLoaded) {
             throw new GoldenGateDatabaseNoConnectionError(
                     "DbAdmin not initialzed");
         }
         try {
             conn = DriverManager.getConnection(admin.getServer(),
                     admin.getUser(), admin.getPasswd());
             conn.setAutoCommit(true);
             this.isReadOnly = isReadOnly;
             conn.setReadOnly(this.isReadOnly);
             setInternalId(this);
             DbAdmin.addConnection(internalId, this);
             this.admin = admin;
             isDisconnected = false;
         } catch (SQLException ex) {
             // handle any errors
             isDisconnected = true;
             logger.error("Cannot create Connection", ex);
             error(ex);
             conn = null;
             throw new GoldenGateDatabaseNoConnectionError(
                     "Cannot create Connection", ex);
         } catch (NullPointerException ex) {
             // handle any errors
             logger.error("Cannot create Connection:" + (admin==null), ex);
             conn = null;
             throw new GoldenGateDatabaseNoConnectionError(
                     "Cannot create Connection", ex);
         }
     }
 
     /**
      * Create a session and connect the current object to the server using the
      * string with the form for mysql for instance
      * jdbc:type://[host:port],[failoverhost:port]
      * .../[database][?propertyName1][
      * =propertyValue1][&propertyName2][=propertyValue2]...
      * 
      * By default (if server = null) :
      * "jdbc:mysql://localhost/r66 user=r66 password=r66"
      * 
      * 
      * If the initialize is not call before, call it with the default value.
      * 
      * @param server
      * @param user
      * @param passwd
      * @param isReadOnly
      * @param autoCommit
      * @throws GoldenGateDatabaseSqlError
      */
     public DbSession(String server, String user, String passwd,
             boolean isReadOnly, boolean autoCommit)
             throws GoldenGateDatabaseNoConnectionError {
         if (!DbModelFactory.classLoaded) {
             throw new GoldenGateDatabaseNoConnectionError(
                     "DbAdmin not initialzed");
         }
         if (server == null) {
             conn = null;
             logger.error("Cannot set a null Server");
             throw new GoldenGateDatabaseNoConnectionError(
                     "Cannot set a null Server");
         }
         try {
             this.autoCommit = autoCommit;
             conn = DriverManager.getConnection(server, user, passwd);
             conn.setAutoCommit(this.autoCommit);
             this.isReadOnly = isReadOnly;
             conn.setReadOnly(this.isReadOnly);
             setInternalId(this);
             DbAdmin.addConnection(internalId, this);
             isDisconnected = false;
         } catch (SQLException ex) {
             isDisconnected = true;
             // handle any errors
             logger.error("Cannot create Connection");
             error(ex);
             conn = null;
             throw new GoldenGateDatabaseNoConnectionError(
                     "Cannot create Connection", ex);
         }
     }
 
     /**
      * Create a session and connect the current object to the server using the
      * DbAdmin object.
      * 
      * If the initialize is not call before, call it with the default value.
      * 
      * @param admin
      * @param isReadOnly
      * @param autoCommit
      * @throws GoldenGateDatabaseSqlError
      */
     public DbSession(DbAdmin admin, boolean isReadOnly, boolean autoCommit)
             throws GoldenGateDatabaseNoConnectionError {
         if (!DbModelFactory.classLoaded) {
             throw new GoldenGateDatabaseNoConnectionError(
                     "DbAdmin not initialzed");
         }
         try {
             this.autoCommit = autoCommit;
             conn = DriverManager.getConnection(admin.getServer(),
                     admin.getUser(), admin.getPasswd());
             conn.setAutoCommit(this.autoCommit);
             this.isReadOnly = isReadOnly;
             conn.setReadOnly(this.isReadOnly);
             setInternalId(this);
             DbAdmin.addConnection(internalId, this);
             this.admin = admin;
             isDisconnected = false;
         } catch (SQLException ex) {
             // handle any errors
             isDisconnected = true;
             logger.error("Cannot create Connection");
             error(ex);
             conn = null;
             throw new GoldenGateDatabaseNoConnectionError(
                     "Cannot create Connection", ex);
         }
     }
 
     /**
      * Change the autocommit feature
      * 
      * @param autoCommit
      * @throws GoldenGateDatabaseNoConnectionError
      */
     public void setAutoCommit(boolean autoCommit)
             throws GoldenGateDatabaseNoConnectionError {
         if (conn != null) {
             this.autoCommit = autoCommit;
             try {
                 conn.setAutoCommit(autoCommit);
             } catch (SQLException e) {
                 // handle any errors
                 logger.error("Cannot create Connection");
                 error(e);
                 conn = null;
                 isDisconnected = true;
                 throw new GoldenGateDatabaseNoConnectionError(
                         "Cannot create Connection", e);
             }
         }
     }
 
     /**
      * @return the admin
      */
     public DbAdmin getAdmin() {
         return admin;
     }
 
     /**
      * @param admin
      *            the admin to set
      */
     public void setAdmin(DbAdmin admin) {
         this.admin = admin;
     }
 
     /**
      * Print the error from SQLException
      * 
      * @param ex
      */
     public static void error(SQLException ex) {
         // handle any errors
         logger.error("SQLException: " + ex.getMessage() + " SQLState: " +
                 ex.getSQLState() + "VendorError: " + ex.getErrorCode());
     }
 
     /**
      * To be called when a client will start to use this DbSession (once by
      * client)
      */
     public void useConnection() {
         nbThread ++;
     }
 
     /**
      * To be called when a client will stop to use this DbSession (once by
      * client)
      */
     public void endUseConnection() {
         nbThread --;
         if (nbThread <= 0) {
             disconnect();
         }
     }
 
     /**
      * Close the connection
      * 
      */
     public void disconnect() {
         if (conn == null) {
             logger.warn("Connection already closed");
             return;
         }
         try {
             Thread.sleep(DbAdmin.WAITFORNETOP);
         } catch (InterruptedException e1) {
             Thread.currentThread().interrupt();
         }
         if (nbThread > 0) {
             logger.info("Still some clients could use this Database Session: " +
                     nbThread);
         }
         removeLongTermPreparedStatements();
         DbAdmin.removeConnection(internalId);
         isDisconnected = true;
         try {
             conn.close();
         } catch (SQLException e) {
             logger.warn("Disconnection not OK");
             error(e);
         }
     }
 
     /**
      * Check the connection to the Database and try to reopen it if possible
      * 
      * @throws GoldenGateDatabaseNoConnectionError
      */
     public void checkConnection() throws GoldenGateDatabaseNoConnectionError {
         try {
             DbModelFactory.dbModel.validConnection(this);
             isDisconnected = false;
             if (admin != null)
                 admin.isConnected = true;
         } catch (GoldenGateDatabaseNoConnectionError e) {
             isDisconnected = true;
             if (admin != null)
                 admin.isConnected = false;
             throw e;
         }
     }
 
     /**
      * 
      * @return True if the connection was successfully reconnected
      */
     public boolean checkConnectionNoException() {
         try {
             checkConnection();
             return true;
         } catch (GoldenGateDatabaseNoConnectionError e) {
             return false;
         }
     }
 
     /**
      * Add a Long Term PreparedStatement
      * 
      * @param longterm
      */
     public void addLongTermPreparedStatement(DbPreparedStatement longterm) {
         this.listPreparedStatement.add(longterm);
     }
 
     /**
      * Due to a reconnection, recreate all associated long term
      * PreparedStatements
      * 
      * @throws GoldenGateDatabaseNoConnectionError
      * @throws GoldenGateDatabaseSqlError
      */
     public void recreateLongTermPreparedStatements()
             throws GoldenGateDatabaseNoConnectionError,
             GoldenGateDatabaseSqlError {
         GoldenGateDatabaseNoConnectionError elast = null;
         GoldenGateDatabaseSqlError e2last = null;
         logger.info("RecreateLongTermPreparedStatements: "+listPreparedStatement.size());
         for (DbPreparedStatement longterm: listPreparedStatement) {
             try {
                 longterm.recreatePreparedStatement();
             } catch (GoldenGateDatabaseNoConnectionError e) {
                 logger.warn(
                         "Error while recreation of Long Term PreparedStatement",
                         e);
                 elast = e;
             } catch (GoldenGateDatabaseSqlError e) {
                 logger.warn(
                         "Error while recreation of Long Term PreparedStatement",
                         e);
                 e2last = e;
             }
         }
         if (elast != null) {
             throw elast;
         }
         if (e2last != null) {
             throw e2last;
         }
     }
 
     /**
      * Remove all Long Term PreparedStatements (closing connection)
      */
     public void removeLongTermPreparedStatements() {
         for (DbPreparedStatement longterm: listPreparedStatement) {
             longterm.realClose();
         }
         listPreparedStatement.clear();
     }
 
     /**
      * Commit everything
      * 
      * @throws GoldenGateDatabaseSqlError
      * @throws GoldenGateDatabaseNoConnectionError
      */
     public void commit() throws GoldenGateDatabaseSqlError,
             GoldenGateDatabaseNoConnectionError {
         if (conn == null) {
             logger.warn("Cannot commit since connection is null");
             throw new GoldenGateDatabaseNoConnectionError(
                     "Cannot commit since connection is null");
         }
         if (isDisconnected) {
             checkConnection();
         }
         try {
             conn.commit();
         } catch (SQLException e) {
             logger.error("Cannot Commit");
             error(e);
             throw new GoldenGateDatabaseSqlError("Cannot commit", e);
         }
     }
 
     /**
      * Rollback from the savepoint or the last set if null
      * 
      * @param savepoint
      * @throws GoldenGateDatabaseNoConnectionError
      * @throws GoldenGateDatabaseSqlError
      */
     public void rollback(Savepoint savepoint)
             throws GoldenGateDatabaseNoConnectionError,
             GoldenGateDatabaseSqlError {
         if (conn == null) {
             logger.warn("Cannot rollback since connection is null");
             throw new GoldenGateDatabaseNoConnectionError(
                     "Cannot rollback since connection is null");
         }
         if (isDisconnected) {
             checkConnection();
         }
         try {
             if (savepoint == null) {
                 conn.rollback();
             } else {
                 conn.rollback(savepoint);
             }
         } catch (SQLException e) {
             logger.error("Cannot rollback");
             error(e);
             throw new GoldenGateDatabaseSqlError("Cannot rollback", e);
         }
     }
 
     /**
      * Make a savepoint
      * 
      * @return the new savepoint
      * @throws GoldenGateDatabaseNoConnectionError
      * @throws GoldenGateDatabaseSqlError
      */
     public Savepoint savepoint() throws GoldenGateDatabaseNoConnectionError,
             GoldenGateDatabaseSqlError {
         if (conn == null) {
             logger.warn("Cannot savepoint since connection is null");
             throw new GoldenGateDatabaseNoConnectionError(
                     "Cannot savepoint since connection is null");
         }
         if (isDisconnected) {
             checkConnection();
         }
         try {
             return conn.setSavepoint();
         } catch (SQLException e) {
             logger.error("Cannot savepoint");
             error(e);
             throw new GoldenGateDatabaseSqlError("Cannot savepoint", e);
         }
     }
 
     /**
      * Release the savepoint
      * 
      * @param savepoint
      * @throws GoldenGateDatabaseNoConnectionError
      * @throws GoldenGateDatabaseSqlError
      */
     public void releaseSavepoint(Savepoint savepoint)
             throws GoldenGateDatabaseNoConnectionError,
             GoldenGateDatabaseSqlError {
         if (conn == null) {
             logger.warn("Cannot release savepoint since connection is null");
             throw new GoldenGateDatabaseNoConnectionError(
                     "Cannot release savepoint since connection is null");
         }
         if (isDisconnected) {
             checkConnection();
         }
         try {
             conn.releaseSavepoint(savepoint);
         } catch (SQLException e) {
             logger.error("Cannot release savepoint");
             error(e);
             throw new GoldenGateDatabaseSqlError("Cannot release savepoint", e);
         }
     }
 }
