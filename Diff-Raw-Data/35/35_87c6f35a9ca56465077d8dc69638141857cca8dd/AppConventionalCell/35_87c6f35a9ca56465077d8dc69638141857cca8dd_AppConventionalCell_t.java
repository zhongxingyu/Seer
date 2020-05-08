 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath" 
  * exception as provided by Sun in the License file that accompanied 
  * this code.
  */
 package org.jdesktop.wonderland.modules.appbase.client.cell;
 
 import org.jdesktop.wonderland.common.cell.CellID;
 import org.jdesktop.wonderland.common.cell.state.CellClientState;
 import org.jdesktop.wonderland.client.cell.CellCache;
 import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
 import org.jdesktop.wonderland.client.comms.WonderlandSession;
 import org.jdesktop.wonderland.client.login.ServerSessionManager;
 import org.jdesktop.wonderland.client.login.SessionLifecycleListener;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 import org.jdesktop.wonderland.modules.appbase.common.cell.AppConventionalCellClientState;
 import org.jdesktop.wonderland.modules.appbase.common.cell.AppConventionalCellSetConnectionInfoMessage;
 
 /**
  * The client-side cell for an 2D conventional application.
  *
  * @author deronj
  */
 @ExperimentalAPI
 public abstract class AppConventionalCell extends App2DCell {
 
     /** The session used by the cell cache of this cell to connect to the server */
     private WonderlandSession cellCacheSession;
     /** The user-visible app name */
     protected String appName;
     /** The connection info. */
     protected String connectionInfo;
     /** The App Conventional connection to the server. */
     private static AppConventionalConnection connection;
     /** The current primary session. */
     private static WonderlandSession currentPrimarySession;
     /** Indicates that this cell is a slave and has connectedToTheApp. */
     private boolean slaveStarted;
 
     /** 
      * Perform user client startup initialization for conventional apps.
      */
     static void initialize (ServerSessionManager loginInfo) {
         loginInfo.addLifecycleListener(new MySessionLifecycleListener());
     }
     
     /**
      * This listens for changes in the session life cycle.
      */
     private static class MySessionLifecycleListener implements SessionLifecycleListener {
 
         /**
          * {@inheritDoc}
          */
        public void sessionCreated(WonderlandSession session) {
 
            // TODO: HACK: For now, assume a non-federated environment
            if (connection != null) {
                logger.warning("Trying to create AppConventionalConnection when it already exists.");
                return;
             }
 
             // Create a new connection
             connection = new AppConventionalConnection(session);
 
             // Connect the connection
             try {
                 connection.connect(session);
             } catch (ConnectionFailureException ex) {
                 RuntimeException re = new RuntimeException("Cannot create App Conventional connection  exception = " + ex);
                 re.initCause(ex);
                 throw re;
             }
         }

        /**
         * {@inheritDoc}
         */
        public void primarySession(WonderlandSession session) {}
     }
     
     /** 
      * Creates a new instance of AppConventionalCell.
      *
      * @param cellID The ID of the cell.
      * @param cellCache the cell cache which instantiated, and owns, this cell.
      */
     public AppConventionalCell(CellID cellID, CellCache cellCache) {
         super(cellID, cellCache);
         cellCacheSession = cellCache.getSession();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void setClientState(CellClientState clientState) {
         super.setClientState(clientState);
 
         AppConventionalCellClientState state = (AppConventionalCellClientState) clientState;
         appName = state.getAppName();
 
         if (state.getLaunchLocation().equalsIgnoreCase("user") &&
             state.getLaunchUser().equals(cellCacheSession.getUserID().getUsername())) {
 
             // Master case
 
             connectionInfo = startMaster(appName, state.getCommand(), false);
             if (connectionInfo == null) {
                 logger.warning("Cannot launch app " + appName);
                 // TODO: what else to do? Delete the cell? If so, how?
                 return;
             }
             logger.info("AppConventional cellID " + getCellID() + " connectionInfo = " + connectionInfo);
 
             // Notify server and clients of the new connection info.
             AppConventionalCellSetConnectionInfoMessage msg =
                 new AppConventionalCellSetConnectionInfoMessage(getCellID(), connectionInfo);
 
             // Send the message to the server for broadcast to all slaves (and also this master
             // so we must self-ignore (see setConnectionInfo). Note that we cannot send this
             // message synchronously and wait for a response because we are already in a
             // darkstar message handler.
             connection.send(msg);
 
         } else {
 
             // Slave case
             //
             // Slaves must wait to connect until valid connection info is known. This can happen in one
             // of two ways. If the slave cell was loaded into this client AFTER the master app started 
             // the connection info will already be known (i.e. non-null). Otherwise, if the slave cell
             // was loaded into this client BEFORE the master app started this client will eventually
             // receive a SetConnectionInfo message whic contains the connection info.
 
             connectionInfo = state.getConnectionInfo();
             logger.severe("Initial connection info value for slave = " + connectionInfo);
             if (connectionInfo != null) {
                 logger.severe("Starting slave from setClientState");
                 startTheSlave(connectionInfo);
             }
         }
     }
 
     /**
      * This is called when the server sends the connection info.
      */
     synchronized void setConnectionInfo (String connInfo) {
 
         // If we already know the connection info then we can skip this.
         // Note: this will happen if we are the master, or if this cell was created after
         // the server learned of the connection info.
         if (connectionInfo != null) {
             return;
         }
 
         // Slave: If this message arrives after we are already connected, just ignore it.
         if (slaveStarted) return;
 
         if (connInfo != null) {
             connectionInfo = connInfo;
             logger.severe("Starting slave from setConnectionInfo");
             startTheSlave(connectionInfo);
         }
     }
 
     private void startTheSlave (String connectionInfo) {
         slaveStarted = true;
         startSlave(connectionInfo);
         if (app == null) {
             logger.warning("Could not connect to slave at " + connectionInfo);
             slaveStarted = false;
         } else {
             logger.info("Connected slave to app at " + connectionInfo);
         }
     }
 
     /** 
      * Launch a master client.
      * @param appName The name of the app.
      * @param command The command string which launches the master app program (used only by master).
      * @param initInBestView Force this cell to be initialized in approximately the best view
      * based on the viewer position at the time of client cell creation.
      * @return Subclass-specific data for making a peer-to-peer connection between master and slave.
      */
     protected abstract String startMaster(String appName, String command, boolean initInBestView);
 
     /** 
      * Launch a slave client.
      * @parem connectionInfo Subclass-specific data for making a peer-to-peer connection between 
      * master and slave.
      */
     protected abstract void startSlave(String connectionInfo);
 }
