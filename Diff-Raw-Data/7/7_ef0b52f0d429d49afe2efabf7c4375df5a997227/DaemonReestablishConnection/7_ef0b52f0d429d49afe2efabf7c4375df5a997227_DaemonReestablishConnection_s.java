 /*
  * DaemonReestablishConnection.java
  *
  * Created on 26 gennaio 2007, 22.28
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package gestionecassa.clients;
 
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
     }
     
     /**
      * Keeps alive connection
      */
     @Override
     public void run() {
         try {
             while (true) {
                /*sleeps for 10 minutes*/
                sleep(600000);
                 server.keepAlive(id);
                 
                logger.info("keepalive mandato al server.");
             }
         } catch (RemoteException ex) {
             logger.warn("RemoteException nel tentativo "+
                     "di inviar eil keepalive",ex);
             
         } catch (InterruptedException ex) {
             logger.warn("Thread refresh connessione "+
                     "interrotto",ex);
         }
     }
 }
