 package com.org.libmetasrv;
 
 
 import java.io.IOException;
 import java.net.SocketException;
 import java.util.ArrayList;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Tony Ivanov
  */
 public abstract class MetaServer extends Thread {
     //Options
     public int port = 5525;
     public int maxConnections = -1;
     public int workerThreads = 2;
     private boolean alive = false;    
     private ArrayList<Worker> workers = new ArrayList<Worker>();
     private static int workPos=0;
     private ArrayList<MetaClient> clients = new ArrayList<MetaClient>();
     public boolean noServer = false;
     public MetaServer(){
         
     }
 
     //Functions
     public void run(){
         //Start listening on port
         try {
             //Start listening on port
             java.net.ServerSocket server = null;
             try {
                 //Start the workers
                 for (int i = 0; i < workerThreads; i++) {
                     Worker w = new Worker();
                     w.start();
                     workers.add(w);
                 }
                 alive = true;
                 if(noServer)
                     return;
                 server = new java.net.ServerSocket(port);
                 System.out.println("Server started, listening on port: " + port);
                 
                 
             } catch (IOException e) {
                 System.err.println("Could not listen on port: " + port);
                 alive = false;
             }
 
 
             //server.setSoTimeout(1000); // Set socket timeout so it has a chance to die when people press stop server.
             //Accept incoming connections while "LISTENING" is true
             while (alive) {
                 java.net.Socket Ssock = server.accept();
                 if(maxConnections == -1 || clients.size() < maxConnections){
                    clients.add(newClient(Ssock));
                 }else{
                     Ssock.close();
                 }
                 sleep(100);
                 
             }
             server.close();
         } catch (SocketException ex) {
             Logger.getLogger(MetaServer.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
                     Logger.getLogger(MetaServer.class.getName()).log(Level.SEVERE, null, ex);
         }catch (InterruptedException ex) {
                     Logger.getLogger(MetaServer.class.getName()).log(Level.SEVERE, null, ex);
         }
                 
     }
     public boolean isServerAlive(){
         return alive;
     }  
     /**
      * Is automatically called upon a new connection to the server
      * Override this function and return a Class based on the libmetasrv.MetaClient
      * interface
      * @param sock Socket object of the new connection.
      * @return MetaClient Interface based class
      */
     public abstract MetaClient newClient(java.net.Socket sock);
     public boolean connectToRemote(String address,int port){
         try {
             java.net.Socket socker = new java.net.Socket();
             socker.connect(new java.net.InetSocketAddress(address, port));
             if(socker.isConnected()){
                  clients.add(newClient(socker));
                 return true;
             }
             
         } catch (IOException ex) {
             Logger.getLogger(MetaServer.class.getName()).log(Level.SEVERE, null, ex);
         }
         return false;
     }
     public void shutdown(){
         alive = false;
     }
    /**
     * Same as broadcast()
     * @param bytes
     */
    public void toAll(byte[] bytes) {
        broadcast(bytes);
    }
     public void broadcast(byte[] buffer){
         for(MetaClient mc : clients){
             try {
                 mc.socket.getOutputStream().write(buffer);
                 mc.socket.getOutputStream().flush();
             } catch (IOException ex) {
                 Logger.getLogger(MetaServer.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
     public void toAllExcept(MetaClient aThis, byte[] buffer) {
         MetaClient[] mcs = {aThis};
         toAllExcept(mcs,buffer);
     }
     public void toAllExcept(MetaClient[] exmcs,byte[] buffer){
         for(MetaClient mc :clients){
             for(MetaClient exmc : exmcs){
                 boolean doit =true;
                 if(mc.equals(exmc)){
                    doit = false; 
                 }
                 if(doit){
                     try {
                         mc.socket.getOutputStream().write(buffer);
                         mc.socket.getOutputStream().flush();
                     } catch (IOException ex) {
                         Logger.getLogger(MetaServer.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }
             }
         }
     }
     public void toOnly(MetaClient[] mcs,byte[] buffer){
         for(MetaClient mc: mcs){
             try {
                 mc.socket.getOutputStream().write(buffer);
                 mc.socket.getOutputStream().flush();
             } catch (IOException ex) {
                 Logger.getLogger(MetaServer.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
 
     class Worker extends Thread{
         
         @Override
         public void run(){
             while(alive){                
                 try {
                     if(clients.size()>0){
                         MetaClient mc = clients.get(workPos);
                         workPos++;
                         if (workPos >= clients.size()){
                             workPos=0;
                         }
 
                         if(!mc.isLocked()){
                             mc.lock();
                             mc.process();
                             mc.heartBeat = System.nanoTime();
                             mc.unlock();
                         }
                     }
                     sleep(10);
                 } catch (InterruptedException ex) {
                     Logger.getLogger(MetaServer.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
     }
 }
