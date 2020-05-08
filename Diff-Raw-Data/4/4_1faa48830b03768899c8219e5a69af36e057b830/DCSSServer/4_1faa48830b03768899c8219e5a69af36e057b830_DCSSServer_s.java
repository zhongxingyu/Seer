 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dcss.server;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Alex
  */
 public class DCSSServer {
     public static final int PMODULE_SIZE = 16;
     public static final int MAX_CONNECTIONS = 32;
     
    ExecutorService processingService;   
     public int id;
     public String host;
     public int port;
     public String type;
 
     public DCSSServer(int size) {
         this.processingService = Executors.newFixedThreadPool(size);        
     }
     
     public void finishProcessingAndDestroy() {
         this.processingService.shutdown();
         try {
             this.processingService.awaitTermination(5, TimeUnit.MINUTES);
         } catch (InterruptedException ex) {
             Logger.getLogger(ProcessingModule.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws IOException {
         int sizeWorkpool = PMODULE_SIZE;
         
         DCSSServer serverInstance = new DCSSServer(sizeWorkpool);
         ServerGroup sg = null;
         try {
             BufferedReader cfgIn = new BufferedReader(new FileReader(args[0]));                
             try {
                 String cfgLine = cfgIn.readLine();
                 while (cfgLine != null) {
                     String[] parts = cfgLine.split(":");
                     switch (parts[0]) {
                         case "id":
                             serverInstance.id = Integer.parseInt(parts[1]);
                             break;
                         case "ip":
                             serverInstance.host = parts[2];
                             break;
                         case "port":
                             serverInstance.port = Integer.parseInt(parts[1]);
                             break;
                         case "comm":
                             serverInstance.type = parts[1];
                             switch(parts[1]) {
                                 case "TCP":
                                     sg = new TCPServerGroup();
                                     break;
                                 default:
                                     Logger.getLogger(DCSSServer.class.getName()).log(Level.WARNING, "Server does not support this type of connection: {0}", parts[1]);
                                     break;
                             }
                             break;
                         case "n":
                             sg.addToGroup(parts[1], Integer.parseInt(parts[2]));
                             break;
                         default :
                             Logger.getLogger(DCSSServer.class.getName()).log(Level.WARNING, "Configuration parameter not supported: {0}", parts[1]);
                             break;
                     }
                 }
             } catch (IOException ex) {
                 Logger.getLogger(DCSSServer.class.getName()).log(Level.SEVERE, null, ex);
             } finally {
                 cfgIn.close();
             }
             
             switch (serverInstance.type) {
                 case "TCP":
                     ServerSocket sock = new ServerSocket(serverInstance.port);
                     int conn = 0;
                     while(conn < MAX_CONNECTIONS) {
                         Socket s = sock.accept();
                         TCPServiceThread newConn = new TCPServiceThread(serverInstance.processingService, serverInstance.id, s, sg);
                         newConn.start();
                         conn++;
                     }
                     break;
                 default:
                     Logger.getLogger(DCSSServer.class.getName()).log(Level.SEVERE, "This communication method is not supported: {0}", serverInstance.type);
                     break;
             }
         } catch (FileNotFoundException ex) {
             Logger.getLogger(DCSSServer.class.getName()).log(Level.WARNING, "Missing config file");
         } finally {
             serverInstance.finishProcessingAndDestroy();
             return;
         }
     }
 }
