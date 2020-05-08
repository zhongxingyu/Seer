 /*
  * Copyright 2010 the original author or authors.
  * Copyright 2009 Paxxis Technology LLC
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 package com.paxxis.cornerstone.database;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import com.paxxis.cornerstone.common.AbstractBlockingObjectPool;
 import com.paxxis.cornerstone.common.PasswordGenerator;
 
 /**
  * Manages a pool of DatabaseConnection connections.
  *    
  * @author Robert Englander
  */ 
 public class DatabaseConnectionPool extends AbstractBlockingObjectPool<DatabaseConnection> {
     private static final Logger LOGGER = Logger.getLogger(DatabaseConnectionPool.class);
 
     private static final long ONEMINUTE = 60000L;
     private static final long DEFAULTIDLETHRESHOLD = 5L * ONEMINUTE;
     private static final long DEFAULTSWEEPCYCLE = ONEMINUTE;
 
     // the database parameters
     private String dbSid = null;
     private String _dbHostname = null;
     private Integer _dbPort = null;
     private String _dbUsername = null;
     private String _dbPassword = null;
     private String _dbName = null;
     private String _dbDriver = null;
     private String _dbUrlPrefix = null;
     private String timestampFormat = null;
     private String catalog = "???";
     private String extraParameters = "";
 
     private boolean autoCommit = false;
     private DatabaseConnection.TransactionIsolation transactionIsolation = 
             DatabaseConnection.TransactionIsolation.TRANSACTION_READ_UNCOMMITTED;
 
     private DatabaseConnectionProvider typeProvider = null;
 
     // the maximum number of instances in the pool
     private int _maximum = 1;
 
     // the minimum number of instances in the pool
     private int _minimum = 1;
 
     private long _idleThreshold = DEFAULTIDLETHRESHOLD;
 
     private long _sweepCycle = DEFAULTSWEEPCYCLE;
 
     private PasswordGenerator passwordGenerator = null;
 
     private boolean ensureConnected = true;
     private String ensureConnectedStatment = "select 1";
 
     //FIXME this is a work around to keep the api for borrowing connections the same in Chime
     //will be removed on future refactorings...
     private Map<DatabaseConnection, PoolEntry> activeConnections = 
             new HashMap<DatabaseConnection, PoolEntry>();
 
     public static class PoolEntry extends AbstractBlockingObjectPool.PoolEntry<DatabaseConnection>
     {
         private long timestamp;
 
         public PoolEntry(DatabaseConnection db) {
             super(db);
             this.timestamp = System.currentTimeMillis();
         }
 
         public long getTimestamp() {
             return this.timestamp;
         }
 
         @Override
         public void shutdown() {
             getObject().disconnect();
             super.shutdown();
         }
 
         @Override
         public void onReturn() {
             getObject().close();
         }       
 
     }
 
     //FIXME this should probably be pushed down into a more generic object pool...
     private static int _sweeperCounter = 0;
     class Sweeper extends Thread
     {
         boolean terminate = false;
 
         public Sweeper()
         {
             setName("Cornerstone Database Connection Pool Sweeper - " + _sweeperCounter++);
         }
 
         public synchronized void terminate()
         {
             terminate = true;
             interrupt();
         }
 
         @Override
         public void run()
         {
             while (!terminate)
             {
                 try
                 {
                     Thread.sleep(_sweepCycle);
                 }
                 catch (InterruptedException e)
                 {
                 }
 
                 if (!terminate) {
                     sweep();
                 }
             }
         }
     }
 
     private Sweeper _sweeper = new Sweeper();
 
 
     private void sweep()
     {
         long now = System.currentTimeMillis();
 
         synchronized (getSemaphore())
         {
             Map<AbstractBlockingObjectPool.PoolEntry<DatabaseConnection>, Object> activePool = getActivePool();
             int active = activePool.size();
 
             List<AbstractBlockingObjectPool.PoolEntry<DatabaseConnection>> freePool = getFreePool();
             int free = freePool.size();
 
             int total = active + free;
 
             if (free > 0 && total > _minimum)
             {
                 // how many should we remove?
                 int over = total - _minimum;
                 int target = free - over;
                 if (free > target)
                 {
                     int count = free - target;
 
                     if (count > 0)
                     {
                         int removed = 0;
                         int last = free - 1;
                         for (int i = last; i >= 0; i--)
                         {
                             PoolEntry entry = (PoolEntry) freePool.get(i);
                             if ((now - entry.timestamp) >= _idleThreshold)
                             {
                                 entry.getObject().disconnect();
                                 freePool.remove(i);
                                 removed++;
 
                                 if (removed == count)
                                 {
                                     break;
                                 }
                             }
                         }
                     }
                 }
             }
 
             // reconnect any closed connections in the free pool
             for (AbstractBlockingObjectPool.PoolEntry<DatabaseConnection> entry : freePool)
             {
                 try {
                     ensureConnection(entry.getObject());
                 } catch (DatabaseException e) {
                 }
             }
         }
     }
 
     public void checkReachable() throws DatabaseException, IOException {
         int port;
         if (_dbPort != null) {
             port = _dbPort;
         } else {
             port = this.getTypeProvider().getDefaultPort();
         }
 
         SecurityManager manager = System.getSecurityManager();
         if (manager != null) {
             try {
                 manager.checkConnect(_dbHostname, port);
             } catch (SecurityException se) {
                 throw new DatabaseException(se);
             }
         }
 
         Socket s = null;
         try {
             s = new Socket();
             s.setReuseAddress(true);
             SocketAddress sa = new InetSocketAddress(this._dbHostname, port);
             s.connect(sa, 3000);
         } catch (IOException e) {
             // that's just fine
             throw e;
         } catch (Throwable e) {
             throw new DatabaseException(e);
         } finally {
             if (s != null) {
                 try {
                     s.close();
                 } catch (IOException e) {
                 }
             }
         }
     }
     
     public void ensureConnection(DatabaseConnection connection) throws DatabaseException {
         boolean force = false;
         try {
             try {
                 checkReachable();
             } catch (DatabaseException de) {
             } catch (IOException ioe) {
                 force = true;
                 String url = typeProvider.getConnectionUrl(this);
                 throw new Exception(url + " is unreachable");
             }
 
             // just send any old statement; essentially like doing a ping
             connection.executeStatement(getEnsureConnectedStatment());
         }
         catch (Exception e)
         {
             connection.disconnect(force);
         }
 
         // if after the ping above we find the connection is closed,
         // try to reconnect it
         if (!connection.isConnected())
         {
             connect(connection);
         }
     }
 
     /**
      * Return a database connection
      * 
      * @param borrower
      * @return
      */
     public DatabaseConnection borrowInstance(Object borrower) throws DatabaseException {
         try {
             PoolEntry entry = borrow(borrower);
             if (entry.getObject().isConnected()) {
                 activeConnections.put(entry.getObject(), entry);
                 return entry.getObject();
             } else {
                 throw new Exception("Unable to borrow broken connection.");
             }
         } catch (Throwable t) {
             throw new DatabaseException(t);
         }
     }
 
     public void returnInstance(DatabaseConnection connection, Object borrower) {
         returnInstance(activeConnections.remove(connection));
     }
 
     /**
      * Hook to validate the pool entry before returning it to borrower
      * @param entry
      * @return a valid pool entry
      */
     @Override
     protected <P extends AbstractBlockingObjectPool.PoolEntry<DatabaseConnection>> P validatePoolEntry(P entry) {
         if (isEnsureConnected()) {
             try {
                 ensureConnection(entry.getObject());
             } catch (DatabaseException e) {
             }
         }
         return super.validatePoolEntry(entry);
     }
 
     public void shutdown() {
         try {
             super.shutdown();
         } finally {
             typeProvider.onShutdown(this);
             _sweeper.terminate();
             activeConnections.clear();
         }
     }
 
 
     @Override
     public void initialize()
     {
         if (typeProvider == null) {
             throw new RuntimeException("typeProvider can't be null.");
         }
 
         _dbPassword = passwordGenerator.encryptPassword(_dbPassword);
         setPoolSize(_minimum);
         super.initialize();     	
         _sweeper.start();
     }
 
     private void initDriver() {
         try {
             Class.forName(_dbDriver);
         } catch (ClassNotFoundException ex) {
             throw new RuntimeException(ex);
         }
     }
 
     @SuppressWarnings("unchecked")
     @Override
     protected PoolEntry createPoolEntry() {
         return new PoolEntry(create());
     }
 
     public void setPasswordGenerator(PasswordGenerator generator) {
         passwordGenerator = generator;
     }
 
     public void setCatalog(String cat) {
         catalog = cat;
     }
 
     public String getCatalog() {
         return catalog;
     }
 
     public void connect(DatabaseConnection database) throws DatabaseException
     {
         try
         {
             database.setDriverName(_dbDriver);
             database.setCatalog(catalog);
             database.setAutoCommit(autoCommit);
             database.setTransactionIsolation(transactionIsolation);
 
             String url = typeProvider.getConnectionUrl(this);
             boolean tryIt = true;
             try {
                 checkReachable();
             } catch (DatabaseException de) {
                 tryIt = false;
             }
 
             if (tryIt) {
                 database.connect(url, _dbUsername, _dbPassword);
             }
        } catch (Exception e) {
             LOGGER.error(e);
             throw new DatabaseException(e);
         }
     }
 
     private DatabaseConnection create() 
     {
         DatabaseConnection database = new DatabaseConnection();
         try {
             connect(database);
         } catch (DatabaseException e) {
             throw new RuntimeException(e);
         }
         return database;
     }
 
     public void setExtraParameters(String params) {
         extraParameters = params;
     }
 
     public String getExtraParameters() {
         return extraParameters;
     }
 
     public void setTimestampFormat(String fmt) {
         timestampFormat = fmt;
     }
 
     public String getTimestampFormat() {
         return timestampFormat;
     }
 
     public void setDbSid(String sid) {
         dbSid = sid;
     }
 
     public String getDbSid() {
         return dbSid;
     }
 
     public void setDbDriver(String driver)
     {
         _dbDriver = driver;
         initDriver();
     }
 
     public String getDbDriver()
     {
         return _dbDriver;
     }
 
     public void setDbUrlPrefix(String prefix)
     {
         _dbUrlPrefix = prefix;
     }
 
     public String getDbUrlPrefix()
     {
         return _dbUrlPrefix;
     }
 
     /**
      * Set the host name for the configuration database.
      *
      * @param name the host name
      */
     public void setDbHostname(String name)
     {
         _dbHostname = name;
     }
 
     public String getDbHostname()
     {
         return _dbHostname;
     }
 
     public void setDbPort(int port) {
         _dbPort = port;
     }
 
     public Integer getDbPort() {
         return _dbPort;
     }
 
     /**
      * Set the user name to use for logging in to the configuration database.
      *
      * @param name the user name
      */
     public void setDbUsername(String name)
     {
         _dbUsername = name;
     }
 
     public String getDbUsername()
     {
         return _dbUsername;
     }
     /**
      * Set the user password for logging in to the configuration database
      *
      * @param pw the password
      */
     public void setDbPassword(String pw)
     {
         _dbPassword = pw;
     }
 
     public String getDbPassword()
     {
         return _dbPassword;
     }
 
     /**
      * Set the name of the configuration database
      *
      * @param name the configuration database name
      */
     public void setDbName(String name)
     {
         _dbName = name;
     }
 
     public String getDbName()
     {
         return _dbName;
     }
 
     public void setMinPoolSize(int minimum)
     {
         _minimum = minimum;
     }
 
     public int getMinPoolSize()
     {
         return _minimum;
     }
 
     public void setMaxPoolSize(int maximum)
     {
         _maximum = maximum;
     }
 
     public int getMaxPoolSize()
     {
         return _maximum;
     }
 
     public void setIdleThreshold(float minutes)
     {
         _idleThreshold = (long)(minutes * (float)ONEMINUTE);
     }
 
     public void setSweepCycle(float minutes)
     {
         _sweepCycle = (long)(minutes * (float)ONEMINUTE);
     }
 
     public void setEnsureConnected(boolean ensureConnected) {
         this.ensureConnected = ensureConnected;
     }
 
     public boolean isEnsureConnected() {
         return this.ensureConnected;
     }
 
     public void setEnsureConnectedStatment(String ensureConnectedStatment) {
         this.ensureConnectedStatment = ensureConnectedStatment;
     }
 
     public String getEnsureConnectedStatment() {
         return ensureConnectedStatment;
     }
 
     public DatabaseConnection.TransactionIsolation getTransactionIsolation() {
         return transactionIsolation;
     }
 
     public void setTransactionIsolationLevel(String level) {
         DatabaseConnection.TransactionIsolation tlevel = DatabaseConnection.TransactionIsolation.valueOf(level);
         setTransactionIsolation(tlevel);
     }
 
     public void setTransactionIsolation(DatabaseConnection.TransactionIsolation transactionIsolation) {
         this.transactionIsolation = transactionIsolation;
     }
 
     public boolean isAutoCommit() {
         return autoCommit;
     }
 
     public void setAutoCommit(boolean autoCommit) {
         this.autoCommit = autoCommit;
     }
 
     public DatabaseConnectionProvider getTypeProvider() {
         return typeProvider;
     }
 
     public void setTypeProvider(DatabaseConnectionProvider typeProvider) {
         this.typeProvider = typeProvider;
     }
 }
