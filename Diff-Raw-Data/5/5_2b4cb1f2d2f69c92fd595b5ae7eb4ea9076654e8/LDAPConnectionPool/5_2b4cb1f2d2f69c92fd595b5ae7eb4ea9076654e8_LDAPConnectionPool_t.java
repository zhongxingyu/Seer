 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: LDAPConnectionPool.java,v 1.5 2007-04-19 02:51:41 goodearth Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.common;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.StringTokenizer;
 import com.iplanet.am.util.SystemProperties;
 import com.iplanet.services.ldap.DSConfigMgr;
 import com.iplanet.services.ldap.LDAPServiceException;
 import com.sun.identity.common.FallBackManager;
 import com.sun.identity.shared.debug.Debug;
 import netscape.ldap.LDAPConnection;
 import netscape.ldap.LDAPException;
 import netscape.ldap.LDAPSearchConstraints;
 
 /**
  * Class to maintain a pool of individual connections to the
  * same server. Specify the initial size and the max size
  * when constructing a pool. Call getConnection() to obtain
  * a connection from the pool and close() to return it. If
  * the pool is fully extended and there are no free connections,
  * getConnection() blocks until a connection has been returned
  * to the pool.<BR>
  * Call destroy() to release all connections.
  *<BR><BR>Example:<BR>
  *<PRE>
  * LDAPConnectionPool pool = null;
  * try {
  *     pool = new LDAPConnectionPool("test", 10, 30,
  *                                "foo.acme.com",389,
  *                                "uid=me, o=acme.com",
  *                                "password" );
  * } catch ( LDAPException e ) {
  *    System.err.println( "Unable to create connection pool" );
  *    System.exit( 1 );
  * }
  * while ( clientsKnocking ) {
  *     String filter = getSearchFilter();
  *     LDAPConnection ld = pool.getConnection();
  *     try {
  *         LDAPSearchResults res = ld.search( BASE, ld.SCOPE_SUB,
  *                                            filter, attrs,
  *                                            false );
  *         pool.close( ld );
  *         while( res.hasMoreElements() ) {
  *             ...
  *</PRE>
  */
 
 /**
  * LDAPConnection pool, typically used by a server to avoid creating
  * a new connection for each client
  *
  **/
 public class LDAPConnectionPool {
 
     private static ArrayList hostArrList = new ArrayList();
     private static Debug debug;  // Debug object
     private static HashSet retryErrorCodes = new HashSet();
     private static final String LDAP_CONNECTION_ERROR_CODES =
         "com.iplanet.am.ldap.connection.ldap.error.codes.retries";
  
     /**
      * Constructor for specifying all parameters
      *
      * @param name name of connection pool
      * @param min initial number of connections
      * @param max maximum number of connections
      * @param host hostname of LDAP server
      * @param port port number of LDAP server
      * @param authdn DN to authenticate as
      * @param authpw password for authentication
      * @exception LDAPException on failure to create connections
      */
     public LDAPConnectionPool(
         String name,
         int min,
         int max,
         String host,
         int port,
         String authdn,
         String authpw
     ) throws LDAPException {
         /////this(name, min, max, host, port, authdn, authpw, null);
         this(name, min, max, host, port, authdn, authpw, null, null);
     }
 
     /**
      * Constructor for specifying all parameters, anonymous
      * identity
      *
      * @param name name of connection pool
      * @param min initial number of connections
      * @param max maximum number of connections
      * @param host hostname of LDAP server
      * @param port port number of LDAP server
      * @exception LDAPException on failure to create connections
      */
     public LDAPConnectionPool(
         String name,
         int min,
         int max,
         String host,
         int port
     ) throws LDAPException {
         this(name, min, max, host, port, "", ""); 
     }
 
     /**
      * Constructor for specifying connection option parameters in addition
      * to all other parameters.
      *
      * @param name name of connection pool
      * @param min initial number of connections
      * @param max maximum number of connections
      * @param host hostname of LDAP server
      * @param port port number of LDAP server
      * @param authdn DN to authenticate as
      * @param authpw password for authentication
      * @param connOptions connection option parameters set at serviceconfig
      * @exception LDAPException on failure to create connections
      */
     public LDAPConnectionPool(
         String name, 
         int min, 
         int max,
         String host, 
         int port,
         String authdn, 
         String authpw,
         HashMap connOptions
     ) throws LDAPException {
         this(name, min, max, host, port, authdn, authpw, null, connOptions);
     }
 
     /**
      * Constructor for using default parameters, anonymous identity
      *
      * @param name name of connection pool
      * @param host hostname of LDAP server
      * @param port port number of LDAP server
      * @exception LDAPException on failure to create connections
      */
     public LDAPConnectionPool(String name, String host, int port)
         throws LDAPException
     {
         // poolsize=10,max=20,host,port,
         // noauth,nopswd
         this(name, 10, 20, host, port, "", "", null);
     }
 
     /** 
      * Constructor for using an existing connection to clone
      * from.
      * <P>
      * The connection to clone must be already established and
      * the user authenticated.
      * 
      * @param name name of connection pool
      * @param min initial number of connections
      * @param max maximum number of connections
      * @param ldc connection to clone 
      * @exception LDAPException on failure to create connections 
      */ 
     public LDAPConnectionPool(
         String name,
         int min,
         int max,
         LDAPConnection ldc
     ) throws LDAPException {
         this(name, min, max, ldc.getHost(), ldc.getPort(),
               ldc.getAuthenticationDN(), ldc.getAuthenticationPassword(),
               (LDAPConnection)ldc.clone(), null);
     }
 
     /* 
      * Constructor for using an existing connection to clone
      * from
      * 
      * @param name name of connection pool
      * @param min initial number of connections
      * @param max maximum number of connections
      * @param host hostname of LDAP server
      * @param port port number of LDAP server
      * @param authdn DN to authenticate as
      * @param authpw password for authentication
      * @param ldc connection to clone 
      * @param connOptions connection option parameters set at serviceconfig
      * @exception LDAPException on failure to create connections 
      */ 
    public LDAPConnectionPool(
         String name,
         int min,
         int max,
         String host,
         int port,
         String authdn,
         String authpw,
         LDAPConnection ldc,
         HashMap connOptions
     ) throws LDAPException {
         this(name, min, max, host, port,
              authdn, authpw, ldc, getIdleTime(name), connOptions);
     }
 
     private static final int getIdleTime(String poolName) {
         String idleStr =
             SystemProperties.get(Constants.LDAP_CONN_IDLE_TIME_IN_SECS);
         int idleTimeInSecs = 0;
         if (idleStr != null && idleStr.length() > 0) {
             try {
                 idleTimeInSecs = Integer.parseInt(idleStr);
             } catch(NumberFormatException nex) {
                 debug.error("LDAPConnection pool: " + poolName +
                             ": Cannot parse idle time: " + idleStr +
                             " Connection reaping is disabled.");
             }
         }
         return idleTimeInSecs;
     }
 
 
     /**
      * Most generic constructor which initializes all variables.
      */
     private LDAPConnectionPool(
         String name,
         int min,
         int max,
         String host,
         int port,
         String authdn,
         String authpw,
         LDAPConnection ldc,
         int idleTimeInSecs,
         HashMap connOptions
     ) throws LDAPException {
         this.name = name;
         this.minSize = min;
         this.maxSize  = max;
         //createHostList and assign the first one to the this.host & this.port
         createHostList(host);
         this.authdn = authdn;
         this.authpw = authpw;
         this.ldc = ldc;
         this.idleTime = idleTimeInSecs * 1000;
         this.stayAlive = true;
         this.defunct = false;
 
         createPool();
         if (debug.messageEnabled()) {
             debug.message("LDAPConnection pool: " + name +
                           ": successfully created: Min:" + minSize +
                           " Max:" + maxSize + " Idle time:" + idleTimeInSecs);
         }
         createIdleCleanupThread();
     }
 
     /**
      * Destroy the whole pool - called during a shutdown
      */
     public void destroy() {
         stayAlive = false;
         // if idle timeout property is not set, cleanupThread will be
         // null. null checked here to avoid NPE.
         if (cleanupThread != null) {
             cleanupThread.interrupt();
             while (cleanupThread.isAlive()) {
                 try {
                     Thread.sleep(1000);
                 } catch(InterruptedException iex) {}
             }
         }
         destroyPool(pool);
     }
 
     /**
      * Gets a connection from the pool
      *
      * If no connections are available, the pool will be
      * extended if the number of connections is less than
      * the maximum; if the pool cannot be extended, the method
      * blocks until a free connection becomes available.
      *
      * @return an active connection.
      */
     public LDAPConnection getConnection() {
         return getConnection(0);
     }
 
     /**
      * Gets a connection from the pool within a time limit.
      *
      * If no connections are available, the pool will be
      * extended if the number of connections is less than
      * the maximum; if the pool cannot be extended, the method
      * blocks until a free connection becomes available or the
      * time limit is exceeded. 
      *
      * @param timeout timeout in milliseconds
      * @return an active connection or <CODE>null</CODE> if timed out. 
      */
     public synchronized LDAPConnection getConnection(int timeout) {
         LDAPConnection con = null;
         long waitTime = 0;
         while ((con = getConnFromPool()) == null ) {
             long t0 = System.currentTimeMillis();
 
             if (timeout < 0) {
                 break;
             }
 
             synchronized (pool) {
                 try {
                     if (defunct) return con;
                     pool.wait(timeout);
                 } catch (InterruptedException e) {
                     return null;
                 }
             }
 
             waitTime += System.currentTimeMillis() - t0;
             timeout -= (timeout > 0) ? waitTime : 0;
         }
         return con;
     }
 
     /**
      * Gets a connection from the pool
      *
      * If no connections are available, the pool will be
      * extended if the number of connections is less than
      * the maximum; if the pool cannot be extended, the method
      * returns null.
      *
      * @return an active connection or null.
      */
     protected LDAPConnection getConnFromPool() {
         LDAPConnection con = null;
         LDAPConnectionObject ldapconnobj = null;
 
         // Get an available connection
         for (int i = 0; i < ((pool.size()< maxSize)?pool.size():maxSize); ++i) {
             // Get the ConnectionObject from the pool
             LDAPConnectionObject co = (LDAPConnectionObject)pool.get(i);
             synchronized (co) {
                 if (co.isAvailable()) {  // Conn available?
                     ldapconnobj = co;
                     co.setInUse(true);
                     break;
                 }
             }
         }
 
         if ((ldapconnobj == null) && (pool.size() < maxSize)) {
 	    /*
              * If there there were no conns in pool, can we grow
              * the pool?
              */
             synchronized (pool) {
 
                 if ((maxSize < 0) || ((maxSize > 0) &&
                                       (pool.size() < maxSize))) {
         
                     // Yes we can grow it
                     ldapconnobj = addConnection();
         
                     // If a new connection was created, use it
                     if (ldapconnobj != null) {
                         ldapconnobj.setInUse(true);
                         pool.add(ldapconnobj);
                     }
                 } else {
                     debug.message("LDAPConnection pool:" + name +
                                   ":All pool connections in use");
                 }
             }
         }
 
         if (ldapconnobj != null) {
             con = ldapconnobj.getLDAPConn();
         }
         return con;
     }
 
     /**
      * This is our soft close - all we do is mark
      * the connection as available for others to use.
      * We also reset the auth credentials in case
      * they were changed by the caller.
      *
      * @param ld a connection to return to the pool
      */
     public void close (LDAPConnection ld) {
         if (find(deprecatedPool, ld) != -1) {
             removeFromPool(deprecatedPool, ld);
         } else {
             removeFromPool(pool, ld);
         }
     }
 
     /**
      * This is our soft close - all we do is mark
      * the connection as available for others to use.
      * We also reset the auth credentials in case
      * they were changed by the caller.
      *
      * @param ld a connection to return to the pool
      */
     public void close( LDAPConnection ld , int errCode) {
         if (debug.messageEnabled()) {
             debug.message("LDAPConnectionPool:close(): errCode "+errCode);
         }
         // Do manual failover to the secondary server, if ldap error code is
         // 80 or 81 or 91.
         if (retryErrorCodes.contains(Integer.toString(errCode))) {
             failOver(ld);
             if ( (LDAPConnPoolUtils.connectionPoolsStatus != null)
                 && (!LDAPConnPoolUtils.connectionPoolsStatus.isEmpty()) ) {
                 // Initiate the fallback to primary server by invoking a
                 // FallBackManager thread which pings if the primary is up.
                 if (fMgr == null || !fMgr.isAlive()) {
                     fMgr = new FallBackManager();
                     fMgr.start();
                 }
             }
         }
         if(find(deprecatedPool, ld) != -1) {
             removeFromPool(deprecatedPool, ld);
         } else {
             removeFromPool(pool, ld);
         }
     }
 
     private void removeFromPool(ArrayList thePool, LDAPConnection ld) {
         int index = find(thePool, ld);
         if (index != -1) {
             LDAPConnectionObject co = 
                 (LDAPConnectionObject)thePool.get(index);
 
             co.setInUse (false);  // Mark as available
             synchronized (thePool) {
                 thePool.notify();
             }
         }
     }
   
     private void disconnect(LDAPConnectionObject ldapconnObject) {
         if (ldapconnObject != null) {
             if (debug.messageEnabled()) {
                 debug.message("In LDAPConnectionPool:disconnect()");
             }
             if (ldapconnObject.isAvailable()) {
                 ldapconnObject.setAsDestroyed();
                 LDAPConnection ld = ldapconnObject.getLDAPConn();
                 if ( (ld != null) && (ld.isConnected()) ) {
                     try {
                         ld.disconnect();
                     } catch (LDAPException e) {
                         debug.error("LDAPConnection pool:" + name +
                                     ":Error during disconnect.", e);
                     }
                  }
                  ldapconnObject.setLDAPConn(null); // Clear conn
             }
         }
     }
  
     private void createPool() throws LDAPException {
         // Called by the constructors
         if (minSize <= 0) {
             throw new LDAPException("LDAPConnection pool:" + name +
                                     ":ConnectionPoolSize invalid");
         }
         if (maxSize < minSize) {
             debug.error("LDAPConnection pool:" + name +
                         ":ConnectionPoolMax is invalid, set to " +
                         minSize);
             maxSize = minSize;
         }
 
         if (debug.messageEnabled()) {
             StringBuffer buf = new StringBuffer();
             buf.append("");
             buf.append("New Connection pool name =" + name);
             buf.append(" LDAP host =").append(host);
             buf.append(" Port =").append(port);
             buf.append(" Min =").append(minSize);
             buf.append(" Max =").append(maxSize);
             debug.message("LDAPConnectionPool:createPool(): buf.toString()" +
                 buf.toString());
         }
 
         // To avoid resizing we set the size to twice the max pool size.
         pool = new ArrayList(maxSize * 2); 
         deprecatedPool = new ArrayList(maxSize * 2);
         setUpPool (minSize); // Initialize it
     }
 
     private LDAPConnectionObject addConnection() {
         LDAPConnectionObject ldapconnobj = null;
 
         if (defunct) {
             debug.error("LDAPConnection pool:" + name +
                           ":Defunct connection pool object.  " +
                           "Cannot add connections.");
             return ldapconnobj;
         }
         try {
             ldapconnobj = 
                 createConnection(LDAPConnPoolUtils.connectionPoolsStatus);
         } catch (Exception ex) {
             debug.error("LDAPConnection pool:" + name +
                         ":Error while adding a connection.", ex);
         }
         if (ldapconnobj != null) {
             debug.message("LDAPConnection pool:" + name +
                           ":adding a connection to pool...");
         }
         return ldapconnobj;
     }
   
     private void setUpPool (int size) throws LDAPException {
         synchronized (pool) {
             // Loop on creating connections
             while (pool.size() < size) {
                 pool.add(createConnection(
                     LDAPConnPoolUtils.connectionPoolsStatus));
             }
         }
     }
 
     private LDAPConnectionObject createConnection(
         HashMap aConnectionPoolsStatus
     ) throws LDAPException {
 
         LDAPConnectionObject co = new LDAPConnectionObject();
         // Make LDAP connection, using template if available
         LDAPConnection newConn =
             (ldc != null) ? (LDAPConnection)ldc.clone() :
             new LDAPConnection();
         co.setLDAPConn(newConn);
         String key = name + ":" + host + ":" + port + ":" + authdn;
         try {
             if (newConn.isConnected()) {
                 /*
                  * If using a template, then reconnect
                  * to create a separate physical connection
                  */
                 newConn.reconnect();
                 if (debug.messageEnabled()) {
                     debug.message("LDAPConnectionPool: "+
                         "createConnection(): with template primary host: " +
                          host + "primary port: " + port);
                 }
             } else {
                 /*
                  * Not using a template, so connect with
                  * simple authentication using ldap v3
                  */
                 try { 
                     newConn.connect (3, host, port, authdn, authpw); 
                    if (debug.messageEnabled()) {
                        debug.message("LDAPConnectionPool: "+
                            "createConnection():No template primary host: " +
                            host + "primary port: " + port);
                    }
                 } catch (LDAPException connEx) {
                     // fallback to ldap v2 if v3 is not supported
                     if (connEx.getLDAPResultCode() ==
                         LDAPException.PROTOCOL_ERROR)
                     {
                         newConn.connect (2, host, port, authdn, authpw); 
                         if (debug.messageEnabled()) {
                             debug.message("LDAPConnectionPool: "+
                             "createConnection():No template primary host: " +
                             host + "primary port: with v2 " + port);
                         }
                     } else {
                         // Mark the host to be down and failover
                         // to the next server in line.
                         if (aConnectionPoolsStatus != null) {
                             synchronized(aConnectionPoolsStatus) {
                                 aConnectionPoolsStatus.put(key, this);
                             }
                         }
                         if (debug.messageEnabled()) {
                             debug.message("LDAPConnectionPool: "+
                                 "createConnection():primary host" + host +
                                     "primary port-" + port + " :is down."+
                                     "Failover to the secondary server.");
                         }
                     }
                 }
             }
         } catch (LDAPException le) {
             debug.error("LDAPConnection pool:createConnection():" + 
                 "Error while Creating pool.", le);
             // Mark the host to be down and failover
             // to the next server in line.
             if (aConnectionPoolsStatus != null) {
                 synchronized(aConnectionPoolsStatus) {
                     aConnectionPoolsStatus.put(key, this);
                 }
             }
             throw le;
         }
         co.setInUse (false); // Mark not in use
         return co;
     }
 
     private int find(ArrayList list, LDAPConnection con ) {
         // Find the matching Connection in the pool
         if (con != null) {
             for (int i = 0; i < list.size(); i++) {
                 LDAPConnectionObject co = 
                     (LDAPConnectionObject)list.get(i);
                 if (((Object)co.getLDAPConn()).equals(con)) {
                     return i;
                 }
             }
         }
         return -1;
     }
 
     /**
      * Wrapper for LDAPConnection object in pool
      */
     class LDAPConnectionObject implements java.lang.Comparable {
         LDAPConnectionObject() {
             inUse = false;
             destroyed = false;
         }
 
         /**
          * Returns the associated LDAPConnection.
          *
          * @return the LDAPConnection.
          * 
          */
         LDAPConnection getLDAPConn() {
             return !destroyed ? this.ld : null;
         }
 
         /**
          * Sets the associated LDAPConnection
          *
          * @param ld the LDAPConnection
          * 
          */
         void setLDAPConn (LDAPConnection ld) {
             this.ld = ld;
         }
 
         /**
          * Marks a connection in use or available
          *
          * @param inUse <code>true</code> to mark in use, <code>false</code>
          * if available
          * 
          */
         void setInUse (boolean inUse) {
             this.inUse = inUse;
             if (inUse) {
                 expirationTime = Long.MAX_VALUE;
             } else {
                 expirationTime = System.currentTimeMillis() + idleTime;
             }
         }
 
         /**
          * Used by comparator to sort before cleanup.
          */
         public synchronized int compareTo (Object l) {
             return (int)
                 (((LDAPConnectionObject)l).expirationTime-this.expirationTime);
         }
 
         /**
          * Method called by purge thread to check
          * if this connection can be reaped.
          *
          * @param currTime given current time, this method will return
          * if the connection has been idle too long.
          */
         boolean canPurge (long currTime) {
             return destroyed || (!inUse && (currTime >= expirationTime));
         }
 
         /**
          * Returns whether the connection is available
          * for use by another user.
          *
          * @return <code>true</code> if available.
          */
         boolean isAvailable() {
             return !destroyed && !inUse;
         }
   
         /**
          * Set the methoed as destroyed so that it will
          * not return a LDAP Connection
          */
         public void setAsDestroyed() {
             this.destroyed = true; 
         }
 
         /**
          * Debug method
          *
          * @returns user-friendly rendering of the object.
          */
         public String toString() {
             return "LDAPConnection=" + ld + ",inUse=" + inUse +
                    " IsDestroyed=" + destroyed;
         }
 
         private LDAPConnection ld;   // LDAP Connection
         private boolean inUse;       // In use? (true = yes)
         private long expirationTime; // time in future when
                                      // when connection considered stale
         private boolean destroyed;   // destroyed is set when the connection
                                      // has been cleaned up.
     }
 
     private void createIdleCleanupThread() {
         if (idleTime > 0) {
             cleaner = new CleanupTask(pool);
             cleanupThread = new Thread(cleaner, name + "-cleanupThread");
             cleanupThread.start();
             if (debug.messageEnabled()) {
                 debug.message("LDAPConnection pool: " + name +
                               ": Cleanup thread created successfully.");
             }
         }
         return;
     }
 
     static {
         debug = Debug.getInstance("LDAPConnectionPool");
         String retryErrs = SystemProperties.get(LDAP_CONNECTION_ERROR_CODES);
         if (retryErrs != null) {
             StringTokenizer stz = new StringTokenizer(retryErrs, ",");
             while(stz.hasMoreTokens()) {
                 retryErrorCodes.add(stz.nextToken().trim());
             }
         }
         if (debug.messageEnabled()) {
             debug.message("LDAPConnectionPool: retry error codes = " +
                              retryErrorCodes);
         }
     }
 
     private void createHostList(String hostNameFromConfig) {
         StringTokenizer st = new StringTokenizer(hostNameFromConfig);
         while(st.hasMoreElements()) {
             String str = (String)st.nextToken();
             if (str != null && str.length() != 0) {
                 if (debug.messageEnabled()) {
                     debug.message("LDAPConnectionPool:createHostList():" +
                         "host name:"+str);
                 }
                 hostArrList.add(str);
             }
         }
         String hpName = (String) hostArrList.get(0);
         StringTokenizer stn = new StringTokenizer(hpName,":");
         this.host = (String)stn.nextToken();
         this.port = (Integer.valueOf((String)stn.nextToken())).intValue();
     }
 
     /**
      * Reinitialize the connection pool with a new connection
      * template.  This method will reap all existing connections
      * and create new connections with the master connection passed
      * in this parameter.
      *
      * @param ld master LDAP connection with new parameters.
      */
     public synchronized void reinitialize(LDAPConnection ld)
         throws LDAPException
     {
         synchronized (pool) {
             synchronized (deprecatedPool) {
                 deprecatedPool.addAll(pool);
                 stayAlive = false;
                 // if idle timeout property is not set, cleanupThread will be
                 // null. null checked here to avoid NPE.
                 if (cleanupThread != null) {
                     cleanupThread.interrupt();
                     while (cleanupThread.isAlive()) {
                         try {
                             Thread.sleep(1000);
                         } catch (InterruptedException iex) {}
                     }
                 }
                 
                 pool.clear();
                 pool = new ArrayList();
                 this.host = ld.getHost();
                 this.port = ld.getPort();
                 this.authdn = ld.getAuthenticationDN();
                 this.authpw = ld.getAuthenticationPassword();
                 this.ldc = (LDAPConnection)ld.clone();
                 if (debug.messageEnabled()) {
                     debug.message("LDAPConnection pool: " + name +
                                   ": reinitializing connection pool: Host:" +
                                   host + " Port:" + port + "Auth DN:" + authdn);
                 }
                 createPool();
                 createIdleCleanupThread();
                 if (debug.messageEnabled()) {
                     debug.message("LDAPConnection pool: " + name +
                                   ": reinitialized successfully.");
                 }
             }
         }
     }
 
     private void destroyPool (ArrayList connPool) {
         synchronized (connPool) {
             this.defunct = true;
             while (pool.size() > 0) {
                 for (int i = 0; i < connPool.size(); ++i) {
                     LDAPConnectionObject lObj =
                         (LDAPConnectionObject)pool.get(i);
                     synchronized (lObj) {
                         if (lObj.isAvailable()) {
                             pool.remove(lObj);
                             disconnect(lObj);
                         }
                     }
                 }
 
                 // sleep for a second before retrying
                 if (pool.size() > 0) {
                     try {
                         Thread.sleep(1000);
                     } catch (InterruptedException iex) {
                         debug.error ("LDAPConnection pool:" + name +
                                     ":Interrupted in destroy method while " +
                                     "waiting for connections to be released.");
                     }
                 }
             }
         }
     }
 
     /**
      * Set minimum and maximum connnections that is maintained by
      * the connection pool object.
      *
      * @param min minimum number
      * @param max maximum number
      */
     public synchronized void resetPoolLimits(int min, int max) {
         if ((maxSize > 0) && (maxSize != max) && (min < max)) {
             if (debug.messageEnabled()) {
                 debug.message ("LDAPConnection pool:" + name +
                                ": is being resized: Old Min/Old Max:" +
                                minSize + '/' + maxSize + ": New Min/Max:" +
                                min + '/' + max);
             }
 
             int oldSize = this.maxSize;
             this.minSize = min;
             this.maxSize = max;
 
             synchronized (pool) {
                 if (oldSize > max) {
                     // if idle time is not set
                     if (cleaner == null) {
                         int diff = oldSize - max;
                         while (diff > 0) {
                             for (int i = 0; i < pool.size() &&
                                         (pool.size() > maxSize); ++i)
                             {
                                 LDAPConnectionObject ldO =
                                         (LDAPConnectionObject)pool.get(i);
                                 synchronized (ldO) {
                                     if (ldO.isAvailable()) {
                                         pool.remove(i);
                                         disconnect(ldO);
                                         --diff;
                                     }
                                 }
                             }
                             // wait for one second and retry till diff
                             // connections are removed.
                             try {
                                 Thread.sleep(1000);
                             } catch (InterruptedException iex) {}
                         }
                     }
                 } else {
                     if (debug.messageEnabled()) {
                         debug.message("LDAPConnection pool:" + name +
                                ":Ensuring pool buffer capacity to:" +
                                max * 2);
                     }
                     pool.ensureCapacity(max * 2);
                 }
             }
         }
     }
 
     public class CleanupTask implements Runnable {
         CleanupTask(ArrayList cleanupPool) {
             this.cleanupPool = cleanupPool;
         }
 
         private void checkDeprecatedConnections() {
             synchronized (deprecatedPool) {
                 if (debug.messageEnabled()) {
                     debug.message("LDAPConnection pool:" + name +
                            ": found " + deprecatedPool.size() +
                            " connection(s) to clean.");
                 }
 
                 for (int i = 0; i < deprecatedPool.size(); ++i) {
                     LDAPConnectionObject lObj =
                             (LDAPConnectionObject)deprecatedPool.get(i);
                     synchronized(lObj) {
                         if (lObj.isAvailable()) {
                             deprecatedPool.remove(i);
                             disconnect(lObj);
                         }
                     }
                 }
             }
         }
 
         public void run() {
             int sleepTime = (int)(idleTime/2);
             while (stayAlive) {
                 try {
                     Thread.sleep(sleepTime);
                 } catch(InterruptedException iex) {
                     continue;
                 }
 
                 if (debug.messageEnabled()) {
                     debug.message("LDAPConnection pool: " + name +
                                   ": starting cleanup.");
                 }
 
                 int startPoolSize = cleanupPool.size();
 
                 // check on connections deprecated earlier
                 if (deprecatedPool.size() > 0)
                     checkDeprecatedConnections();
 
                 synchronized (cleanupPool) {
                     Collections.sort(cleanupPool);
 
                     // Case: max size is reset to a lower limit
                     // Action: Move excess connections to deprecatedPool
                     // if they are currently used.  If not, remove
                     // them and disconnect.
                     if (maxSize < cleanupPool.size()) {
                         int diff = cleanupPool.size() - maxSize;
                         while (diff-- > 0) {
                             LDAPConnectionObject lObj =
                                 (LDAPConnectionObject)cleanupPool.get(0);
                             synchronized (lObj) {
                                 cleanupPool.remove(0);
                                 if (!lObj.isAvailable()) {
                                     synchronized (deprecatedPool) {
                                         deprecatedPool.add(lObj);
                                     }
                                 } else {
                                     disconnect(lObj);
                                 }
                             }
                         }
                     }
 
                     /*
                      * for now this code removes all code that's
                      * timed out.  The pool could shrink to 0, but
                      * on a request for new connection, it would
                      * clone and grow as per demand.
                      */
                     long now = System.currentTimeMillis();
                     for (int i = 0; i < cleanupPool.size(); ++i) {
                         LDAPConnectionObject ldO =
                             (LDAPConnectionObject)cleanupPool.get(i);
                         synchronized (ldO) {
                             if (ldO.canPurge(now)) {
                                 cleanupPool.remove(i);
                                 LDAPConnection ld = ldO.getLDAPConn();
                                 try {
                                     ld.disconnect();
                                 } catch (LDAPException e) {
                                     debug.error("LDAPConnection pool:" + name +
                                                 ":Error during disconnect.", e);
                                 }
                             }
                         }
                     }
                 }
                 if (debug.messageEnabled()) {
                     debug.message("LDAPConnection pool: " + name +
                                 ": finished cleanup: Start pool size:" +
                                 startPoolSize + ": end pool size:" +
                                 cleanupPool.size());
                 }
             }
         }
 
         private ArrayList cleanupPool = null;
     }
 
     /**
      * Sets a valid ldapconnection after fallback to primary server.
      * @param con ldapconnection
      */
     public void fallBack(LDAPConnection con) {
 
         /*
          * Logic here is first get the HashMap key and value where the key
          * is the hostname:portnumber and value is this LDAPConnection pool
          * object, for each server configured in the serverconfig.xml.
          * Then go through the arraylist which has the list of servers
          * configured in serverconfig.xml and check the status of the primary
          * server and connect to that server if it up as well create a new
          * pool for this connection. Also remove the key from the HashMap if
          * primary is up and if there is a successfull fallback.
          */
 
         // If primary is up, do not fallback to any other server,eventhough
         // there are servers that are down in the HashMap.
         if (!isPrimaryUP()) {
 
             LDAPConnection newConn = new LDAPConnection();
             int sze = hostArrList.size();
 
             for (int i = 0; i < sze; i++) {
                 String hpName = (String) hostArrList.get(i);
                 StringTokenizer stn = new StringTokenizer(hpName,":");
                 String upHost = (String)stn.nextToken();
                 String upPort = (String)stn.nextToken();
 
                 /*
                  * This 'if' check is to ensure that the shutdown server from
                  * the incoming LDAPConnection from the FallBackManager
                  * thread which got pinged and succeeded by the FallBackManager
                  * thread, matches with the host array list and gets connected
                  * for fallback to happen exactly for that shutdown server.
                  */
                 if ( (upHost != null) && (upHost.length() != 0)
                     && (upPort != null) && (upPort.length() != 0)
                     && ((con.getHost()!=null) && (con.getHost().
                         equalsIgnoreCase(upHost))) ) {
                     newConn =
                         failoverAndfallback(upHost,upPort,newConn,"fallback");
                     break;
                 }
             }
             reinit(newConn);
         }
     }
 
     /**
      * Sets a valid ldapconnection after failover to secondary server.
      * @param ld ldapconnection
      */
     public void failOver(LDAPConnection ld) {
 
         /* Since we are supporting fallback in FallBackManager class,
          * do the failover here, instead of relying on
          * jdk's failover mechanism.
          * Logic is look for retry error codes from releaseConnection()
          * and in the close() api,
          * from LDAPException and do the failover by calling this api.
          * Then go through the arraylist which has the list of servers
          * configured in serverconfig.xml and check the status of the
          * secondary server and connect to that server as well create a new
          * pool for this connection.
          * Also update the HashMap with the key and this LDAPConnectionPool
          * object if the server is down.
          */
         LDAPConnection newConn = new LDAPConnection();
         // Update the HashMap with the key and this LDAPConnectionPool
         // object if the server is down.
         String downKey = name + ":" + ld.getHost() + ":" +
             ld.getPort() + ":" + authdn;
         if (LDAPConnPoolUtils.connectionPoolsStatus != null) {
             synchronized(LDAPConnPoolUtils.connectionPoolsStatus) {
                 LDAPConnPoolUtils.connectionPoolsStatus.put(downKey, this);
             }
         }
 
         int size = hostArrList.size();
         for (int i = 0; i < size; i++) {
             String hpName = (String) hostArrList.get(i);
             StringTokenizer stn = new StringTokenizer(hpName,":");
             String upHost = (String)stn.nextToken();
             String upPort = (String)stn.nextToken();
 
             /*
              * This 'if' check is to ensure that the shutdown server from
              * the incoming LDAPConnection from the close() method
              * do not get tried to failover.
              * failover is to happen for the next available server in line.
              */
             if ((upHost != null) && (upHost.length() != 0)
                 && (upPort != null) && (upPort.length() != 0)
                 && ((ld.getHost() !=null) && (!ld.getHost().
                     equalsIgnoreCase(upHost)))) {
                 newConn =
                     failoverAndfallback(upHost,upPort,newConn,"failover");
                 break;
             }
         }
         reinit(newConn);
     }
 
     private LDAPConnection failoverAndfallback(
         String upHost,
         String upPort,
         LDAPConnection newConn,
         String caller) {
 
         if (debug.messageEnabled()) {
             debug.message("In LDAPConnectionPool:failoverAndfallback()");
         }
         int intPort = (Integer.valueOf(upPort)).intValue();
         String upKey = name + ":" + upHost + ":" +upPort + ":" + authdn;
         try {
             newConn.connect(3, upHost, intPort, authdn, authpw);
             // After successful connection, remove the key/value
             // from the hashmap to denote that the server which was
             // down earlier is up now.
             if (LDAPConnPoolUtils.connectionPoolsStatus != null) {
                 synchronized(LDAPConnPoolUtils.connectionPoolsStatus) {
                     if (LDAPConnPoolUtils.connectionPoolsStatus.containsKey(
                         upKey)) {
                         LDAPConnPoolUtils.connectionPoolsStatus.remove(upKey);
                     }
                 }
             }
             if (debug.messageEnabled()) {
                 if (caller.equalsIgnoreCase("fallback")) {
                     debug.message("LDAPConnectionPool.failoverAndfallback()"+
                         "fall back successfully to primary host- " + upHost +
                         " primary port: " + upPort);
 
                 } else {
                     debug.message("LDAPConnectionPool.failoverAndfallback()"+
                         "fail over success to secondary host- " + upHost +
                         " secondary port: " + upPort );
                 }
             }
             return (newConn);
         } catch (LDAPException connEx) {
             // fallback to ldap v2 if v3 is not
             // supported
             if (connEx.getLDAPResultCode() == LDAPException.PROTOCOL_ERROR) {
                 try {
                     newConn.connect(2, upHost, intPort, authdn, authpw);
                 } catch (LDAPException conn2Ex) {
                     if (debug.messageEnabled()) {
                         if (caller.equalsIgnoreCase("fallback")) {
                             debug.message("LDAPConnectionPool."+
                                 "failoverAndfallback():fallback failed.");
                         } else {
                             // Mark the host to be down and failover
                             // to the next server in line.
                             if (LDAPConnPoolUtils.
                                 connectionPoolsStatus != null) {
                                 synchronized(LDAPConnPoolUtils.
                                     connectionPoolsStatus) {
                                     LDAPConnPoolUtils.
                                     connectionPoolsStatus.put(upKey, this);
                                 }
                             }
                             debug.message("LDAPConnectionPool."+
                                 "failoverAndfallback():primary host-" +
                                 upHost +" primary port-" + upPort +
                                 " :is down. Failover to the"+
                                 " secondary server. in catch1 ");
                         }
                     }
                 }
             } else {
                 if (debug.messageEnabled()) {
                     if (caller.equalsIgnoreCase("fallback")) {
                          debug.message("LDAPConnectionPool."+
                              "failoverAndfallback():continue fallback"+
                              " to next server");
                     } else {
                          // Mark the host to be down and failover
                          // to the next server in line.
                          if (LDAPConnPoolUtils.connectionPoolsStatus != null) {
                              synchronized(LDAPConnPoolUtils.
                                  connectionPoolsStatus) {
                                  LDAPConnPoolUtils.connectionPoolsStatus.put(
                                  upKey, this);
                              }
                          }
                          debug.message("LDAPConnectionPool. "+
                              "failoverAndfallback():primary host-" + upHost +
                              "primary port-" + upPort +
                              " :is down. Failover to the" +
                              " secondary server. in else");
                     }
                 }
             }
         }
         return (newConn);
     }
 
     private boolean isPrimaryUP() {
         boolean retVal = false;
         String hpName = (String) hostArrList.get(0);
         StringTokenizer stn = new StringTokenizer(hpName,":");
         String upHost = (String)stn.nextToken();
         String upPort = (String)stn.nextToken();
         if ( (upHost != null) && (upHost.length() != 0)
             && (upPort != null) && (upPort.length() != 0) ) {
             String upKey = name + ":" + upHost + ":" +upPort + ":" + authdn;
             if (LDAPConnPoolUtils.connectionPoolsStatus != null) {
                 synchronized(LDAPConnPoolUtils.connectionPoolsStatus) {
                     if (!LDAPConnPoolUtils.connectionPoolsStatus.
                         containsKey(upKey)) {
                         retVal = true;
                     }
                 }
             }
         }
         return (retVal);
     }
 
     private void reinit(LDAPConnection newConn) {
         try {
             reinitialize(newConn);
             /*
              * Set the following default LDAPConnection options for failover
              * and fallback servers/connections.
              * searchConstraints, LDAPConnection.MAXBACKLOG,
              * LDAPConnection.REFERRALS
              */
             if ( (connOptions != null) && (!connOptions.isEmpty()) ) {
                 Iterator itr = connOptions.keySet().iterator();
                 while (itr.hasNext()) {
                     String optName = (String) itr.next();
                     if (optName.equalsIgnoreCase("maxbacklog")) {
                         newConn.setOption(newConn.MAXBACKLOG,
                             connOptions.get(optName));
                     }
                     if (optName.equalsIgnoreCase("referrals")) {
                         newConn.setOption(newConn.REFERRALS,
                             connOptions.get(optName));
                     }
                     if (optName.equalsIgnoreCase("searchconstraints")) {
                         newConn.setSearchConstraints(
                             (LDAPSearchConstraints)connOptions.get(optName));
                     }
                 }
             }
         } catch ( LDAPException lde ) {
             debug.error("LDAPConnectionPool:reinit()" +
                 ":Error while reinitializing connection from pool.", lde);
         }
         LDAPConnectionObject ldapco = new LDAPConnectionObject();
         ldapco.setLDAPConn(newConn);
         //Since reinitialize is cloning the LDAPConnection, disconnect the
         //original one here to avoid memory leak.
         disconnect(ldapco);
     }
 
     private String name;          // name of connection pool;
     private int minSize;          // Min pool size
     private int maxSize;          // Max pool size
     private String host;          // LDAP host
     private int port;             // Port to connect at
     private String authdn;        // Identity of connections
     private String authpw;        // Password for authdn
     // Following default LDAPConnection options are sent by
     // DataLayer/SMDataLayer thru constructor and are in the HashMap
     // 'connOptions'.
     // searchConstraints,LDAPConnection.MAXBACKLOG,LDAPConnection.REFERRALS
     private HashMap connOptions;
     private LDAPConnection ldc = null;          // Connection to clone
     private java.util.ArrayList pool;           // the actual pool
     private java.util.ArrayList deprecatedPool; // the pool to be purged.
     private long idleTime;        // idle time in milli seconds
     private boolean stayAlive;    // boolean flag to exit cleanup loop
     private boolean defunct;      // becomes true after calling destroy
     private Thread cleanupThread; // cleanup thread
     private CleanupTask cleaner;  // cleaner object
     static FallBackManager fMgr;
 }
 
