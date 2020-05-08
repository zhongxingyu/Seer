 /*
  * DaemonReestablishConnection.java
  *
  * Created on 26 gennaio 2007, 22.28
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package gestionecassa.clients;
 
 import gestionecassa.exceptions.NotExistingSessionException;
 import java.rmi.RemoteException;
 import gestionecassa.server.ServerRMICommon;
 import org.apache.log4j.Logger;
 
 /**
  * This class has the only scope to keep alive connection.
  * It's a daemon so it dies when the app dies.
  *
  * @author ben
  */
 public class DaemonReestablishConnection extends Thread {
 
     /**
      * Right log to use.
      */
     private Logger logger;
 
     /**
      * Id of the client.
      */
     private int id;
 
     /**
      * Server
      */
     private ServerRMICommon server;
 
     /**
      * Error state of the daemon
      */
     private DaemonErrorState errorState;
 
     /**
      * Semaphore for the error state of the daemon
      *
      * NOTE: it's randozed to avoid the JVM to make optimizations, which could
      * lead the threads to share the same semaphore.
      */
     private static final String errorStateSemaphore =
             "ErrorStateSemaphore" + System.currentTimeMillis();
     
     /**
      * Creates a new instance of DaemonReestablishConnection
      *
      * @param server 
      * @param id
      * @param logger
      */
     public DaemonReestablishConnection(ServerRMICommon server, int id, Logger logger) {
         /*The only way to destroy it, without having care of anything*/
         this.setDaemon(true);
 
         this.logger = logger;
         this.id = id;
         this.server = server;
        this.errorState = DaemonErrorState.NoError;
     }
     
     /**
      * Keeps alive connection
      */
     @Override
     public void run() {
         try {
             while (true) {
                 /*sleeps for 30 seconds*/
                 Thread.sleep(30000);
                 server.keepAlive(id); //FIXME Add hash handling
                 
 //                logger.info("keepalive mandato al server.");
             }
         } catch (NotExistingSessionException ex) {
             synchronized (errorStateSemaphore) {
                 errorState = DaemonErrorState.NotExistingSessionError;
             }
             logger.warn("KeepAlive not possible: the session in the server "
                     + "has expired",ex);
             
         } catch (RemoteException ex) {
             synchronized (errorStateSemaphore) {
                 errorState = DaemonErrorState.RemoteError;
             }
             logger.warn("RemoteException nel tentativo "+
                     "di inviar eil keepalive",ex);
             
         } catch (InterruptedException ex) {
             logger.warn("Thread refresh connessione "+
                     "interrotto",ex);
         }
     }
 
     public DaemonErrorState getErrorState() {
         synchronized (errorStateSemaphore) {
             return errorState;
         }
     }
 
     public enum DaemonErrorState {
         NoError,
         NotExistingSessionError,
         RemoteError,
     }
 }
