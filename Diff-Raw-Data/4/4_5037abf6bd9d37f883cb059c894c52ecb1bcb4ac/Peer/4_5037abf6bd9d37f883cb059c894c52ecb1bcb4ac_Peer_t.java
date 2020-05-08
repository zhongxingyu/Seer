 package com.hstefan.distrib.t2;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.InetAddress;
 import java.net.MulticastSocket;
 import java.net.UnknownHostException;
 
 /**
  * {@link IPeer} implementation, calls method {@link receiveMessage} whenever a 
  * {@link ReceiverThread} receives a message from the multi-cast group.
  * @author hstefan
  */
 public abstract class Peer implements IPeer {
     private MulticastSocket mMulSocket;
     private InetAddress mGroupAdress;
     private ReceiverThread mReceiverThread;
    
     protected int mPort;
     protected String mHost;
     
     public static final int BUFFER_SIZE = 1024;
     
     public Peer(String host, int port) {
         try {
             mHost = host;
             mPort = port;
             
             mGroupAdress = InetAddress.getByName(host);
             mMulSocket = new MulticastSocket(port);
            mReceiverThread = null;
         } catch (UnknownHostException ex) {
             //TODO
         } catch (IOException ex) {
             //TODO
         }
     }
 
     public void registraPeer() throws IOException {
         mMulSocket.joinGroup(mGroupAdress);
        mReceiverThread = new ReceiverThread();
         mReceiverThread.start();
         posRegistroPeer();
     }
 
     public void removePeer() throws IOException {
         mMulSocket.leaveGroup(mGroupAdress);
         mReceiverThread.stopThread();
         posRemocaoPeer();
     }
 
     public void broadcastMensagem(String msg) throws IOException {
         DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.length(),
                 mPort);
         mMulSocket.send(packet);
     }
     
     public abstract void receiveMessage(DatagramPacket message);
 
     protected void posRegistroPeer() {
         //does nothing by default
     }
 
     protected void posRemocaoPeer() {
         //does nothing by default
     }
     
     private class ReceiverThread extends Thread {
         public static final int SLEEP_MS = 1000;
         private boolean mContinue;
        
         public void stopThread() {
             mContinue = false;
         }
    
         @Override
         public void start() {
             mContinue = true;
             super.start();
         }
         
         @Override
         public void run() {
             while(!mContinue) {
                 try {
                     byte[] buf = new byte[BUFFER_SIZE];
                     DatagramPacket packet = new DatagramPacket(buf, buf.length);
                     mMulSocket.receive(packet);
                     
                     receiveMessage(packet);
                 } catch (IOException ex) {
                     //TODO
                 }
             }
         }
     }
 }
