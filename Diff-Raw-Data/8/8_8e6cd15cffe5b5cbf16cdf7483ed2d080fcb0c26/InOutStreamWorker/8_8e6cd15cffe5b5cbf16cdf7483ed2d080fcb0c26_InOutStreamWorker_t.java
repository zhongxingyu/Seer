 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package indabalance;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.net.SocketException;
 import java.util.Date;
 
 /**
  *
  * @author sash
  */
 public class InOutStreamWorker extends Thread {
 
     private Socket inSocket;
     private Socket outSocket;
     private boolean directionToServer;
     private Worker worker;
 
     public InOutStreamWorker(Worker worker, boolean directionToServer) {
         super();
         this.directionToServer = directionToServer;
         this.worker = worker;
         this.inSocket = null;
         this.outSocket = null;
         start();
     }
 
     public synchronized void setSockets(Socket inSocket, Socket outSocket) {
         this.inSocket = inSocket;
         this.outSocket = outSocket;
         notify();
     }
 
     public void reset() {
         try {
             outSocket.getOutputStream().close();
         } catch (Exception e) {
         }
 
         try {
             inSocket.getInputStream().close();
         } catch (Exception e) {
         }
 
         inSocket = null;
         outSocket = null;
     }
 
     public void close() {
         if (outSocket != null) {
             try {
                 outSocket.shutdownOutput();
             } catch (Exception e) {
             }
         }
     }
 
     private synchronized void waitForSocketsToBeSet() {
         info("waitForSocketsToBeSet");
         worker.setStatus("available");
 
         while ((inSocket == null) || (outSocket == null)) {
            if ((inSocket == null) ^ (outSocket == null)) {
                if (directionToServer) {
                    worker.setStatus("to server - only one socket set");
                } else {
                    worker.setStatus("to client - only one socket set");
                }
            }
            
             try {
                 wait();
             } catch (InterruptedException ie) {
             }
         }
 
         info("waitForSocketsToBeSet finished");
     }
 
     @Override
     public void run() {
         byte[] buffer = new byte[10240]; // 10kB
 
         // TODO add stopping
         while (true) {
             waitForSocketsToBeSet();
 
             try {
                 int bytesRead;
 
                 //System.out.println((directionToServer ? "IN " : "OUT ") + "reading bytes");
                 if ((inSocket == null) || (outSocket == null)) {
                     System.err.println(new Date().toString() + "   " + worker.getName() + " socket null");
                 }
                 
                 if (buffer == null) {
                     System.err.println(new Date().toString() + "   " + worker.getName() + " buffer null");
                 }
                 
                 while ((bytesRead = inSocket.getInputStream().read(buffer)) >= 0) {
                     //System.out.println((directionToServer ? "IN " : "OUT ") + "Read " + bytesRead + " bytes");
                     try {
                         //System.out.println((directionToServer ? "IN " : "OUT ") + "writing bytes");
                         outSocket.getOutputStream().write(buffer, 0, bytesRead);
                         //System.out.println((directionToServer ? "IN " : "OUT ") + "Wrote " + bytesRead + " bytes");
                     } catch (IOException ioe) {
                         error();
                         ioe.printStackTrace();
                         
                         if (directionToServer) {
                             worker.setStatus("error sending data to server: " + ioe.toString());
                             worker.notifyError();
                         }
                     }
                 }
             } catch (SocketException se) {
                 // Exception will be raised almost every time close() is called
             } catch (IOException ioe) {
                 error();
                 ioe.printStackTrace();
             } catch (Throwable t) {
                 error();
                 t.printStackTrace();
             }
 
             worker.notifyDone();
             reset();
         }
     }
     
     public boolean isReady() {
         return (inSocket == null) && (outSocket == null);
     }
     
     private void info() {
         info("");
     }
     
     private void info(String message) {
         System.out.println(new Date().toString() + "   " + worker.getName() + (directionToServer ? " - to server" : " - to client") + (message.equals("") ? "" : " - " + message));
     }
     
     private void error() {
         error("");
     }
     
     private void error(String message) {
         System.err.println(new Date().toString() + "   " + worker.getName() + (directionToServer ? " - to server" : " - to client") + (message.equals("") ? "" : " - " + message));
     }
 }
